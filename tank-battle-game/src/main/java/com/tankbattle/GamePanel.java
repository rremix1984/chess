package com.tankbattle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

/**
 * 游戏面板，负责渲染和游戏逻辑
 */
public class GamePanel extends JPanel implements KeyListener, NetworkManager.GameMessageListener {
    private boolean multiplayer;
    private NetworkManager networkManager;
    private boolean isHost;
    private List<Tank> tanks;
    private Tank playerTank;
    private Timer gameTimer;
    private Set<Integer> pressedKeys;
    private boolean gameRunning;
    private int gameWidth = 1000;
    private int gameHeight = 800;
    private List<Obstacle> obstacles;
    private AIDecisionService aiDecisionService;
    private ExecutorService aiExecutor;
    private Map<String, Future<AIDecisionService.AIDecision>> pendingAIDecisions;
    private Map<String, AIDecisionService.AIDecision> lastAIDecisions;
    private Map<String, Long> aiDecisionTimes; // 记录每个AI的决策时间
    private static final int AI_THREAD_POOL_SIZE = 8; // AI线程池大小
    private List<Explosion> explosions;
    private BackgroundRenderer backgroundRenderer;
    private List<PowerUp> powerUps;
    private long lastPowerUpSpawn = 0;
    private static final long POWER_UP_SPAWN_INTERVAL = 8000; // 8秒生成一个道具
    private AStarPathfinder pathfinder;
    private boolean timeStopActive = false;
    private long timeStopEndTime = 0;
    
    public GamePanel(boolean multiplayer) {
        this.multiplayer = multiplayer;
        initializeGame();
    }
    
    public GamePanel(boolean multiplayer, NetworkManager networkManager, boolean isHost) {
        this(multiplayer);
        this.networkManager = networkManager;
        this.isHost = isHost;
        if (networkManager != null) {
            networkManager.setMessageListener(this);
        }
    }

    private void initializeGame() {
        setFocusable(true);
        // 移除静态背景色，使用动态背景渲染器
        setBackground(Color.BLACK); // 设置为黑色作为后备
        addKeyListener(this);
        
        // 初始化背景渲染器
        backgroundRenderer = new BackgroundRenderer(gameWidth, gameHeight);
        
        tanks = new CopyOnWriteArrayList<>();
        pressedKeys = new HashSet<>();
        obstacles = new ArrayList<>();
        aiDecisionService = new AIDecisionService();
        // 使用固定大小的线程池，提高并发处理能力
        aiExecutor = Executors.newFixedThreadPool(AI_THREAD_POOL_SIZE, r -> {
            Thread t = new Thread(r, "AI-Decision-Thread");
            t.setDaemon(true); // 设置为守护线程
            return t;
        });
        pendingAIDecisions = new ConcurrentHashMap<>();
        lastAIDecisions = new ConcurrentHashMap<>();
        aiDecisionTimes = new ConcurrentHashMap<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();
        
        // 创建障碍物
        createObstacles();
        
        // 初始化寻路器
        pathfinder = new AStarPathfinder(gameWidth / 20, gameHeight / 20, obstacles);
        
        // 创建游戏循环定时器
        gameTimer = new Timer(16, e -> {
            if (gameRunning) {
                updateGame();
                repaint();
            }
        });
    }
    
    private void createObstacles() {
        // 清空已有障碍物
        obstacles.clear();
        
        // 创建简化的迷宫地图
        createSimpleMaze();
    }
    
    /**
     * 创建简化的迷宫风格地图
     */
    private void createSimpleMaze() {
        // 创建边界墙
        createBorderWalls();
        
        // 创建中央核心区域
        createCentralCore();
        
        // 创建四个角落的堡垒
        createCornerFortresses();
        
        // 创建迷宫通道
        createMazeCorridors();
        
        // 创建散布的防御点
        createScatteredDefenses();
    }
    
    /**
     * 创建中央核心区域
     */
    private void createCentralCore() {
        int centerX = gameWidth / 2;
        int centerY = gameHeight / 2;
        
        // 中央不可摧毁的核心
        obstacles.add(new Obstacle(centerX - 40, centerY - 40, 80, 80, Obstacle.Type.STEEL));
        
        // 四个方向的防御塔
        obstacles.add(new Obstacle(centerX - 20, centerY - 120, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX - 20, centerY + 80, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX - 120, centerY - 20, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX + 80, centerY - 20, 40, 40, Obstacle.Type.BRICK));
    }
    
    /**
     * 创建四个角落的堡垒
     */
    private void createCornerFortresses() {
        // 左上角堡垒
        createCornerFortress(80, 80, Obstacle.Type.STEEL);
        // 右上角堡垒
        createCornerFortress(gameWidth - 160, 80, Obstacle.Type.STEEL);
        // 左下角堡垒
        createCornerFortress(80, gameHeight - 160, Obstacle.Type.STEEL);
        // 右下角堡垒
        createCornerFortress(gameWidth - 160, gameHeight - 160, Obstacle.Type.STEEL);
    }
    
    /**
     * 创建迷宫通道
     */
    private void createMazeCorridors() {
        // 水平通道
        for (int x = 200; x < gameWidth - 200; x += 120) {
            if (Math.random() < 0.7) {
                obstacles.add(new Obstacle(x, 200, 40, 40, Obstacle.Type.BRICK));
                obstacles.add(new Obstacle(x, gameHeight - 240, 40, 40, Obstacle.Type.BRICK));
            }
        }
        
        // 垂直通道
        for (int y = 200; y < gameHeight - 200; y += 120) {
            if (Math.random() < 0.7) {
                obstacles.add(new Obstacle(200, y, 40, 40, Obstacle.Type.BRICK));
                obstacles.add(new Obstacle(gameWidth - 240, y, 40, 40, Obstacle.Type.BRICK));
            }
        }
    }
    
    private void createBorderWalls() {
        // 顶部和底部边框
        for (int x = 0; x < gameWidth; x += 40) {
            obstacles.add(new Obstacle(x, 0, 40, 20, Obstacle.Type.STEEL));
            obstacles.add(new Obstacle(x, gameHeight - 20, 40, 20, Obstacle.Type.STEEL));
        }
        // 左右边框
        for (int y = 20; y < gameHeight - 20; y += 40) {
            obstacles.add(new Obstacle(0, y, 20, 40, Obstacle.Type.STEEL));
            obstacles.add(new Obstacle(gameWidth - 20, y, 20, 40, Obstacle.Type.STEEL));
        }
    }
    
    /**
     * 创建复杂的中央城堡区域
     */
    private void createCentralCastle() {
        int centerX = gameWidth / 2;
        int centerY = gameHeight / 2;
        
        // 外层防御墙 - 钢铁结构
        for (int i = -80; i <= 80; i += 40) {
            for (int j = -80; j <= 80; j += 40) {
                if (Math.abs(i) == 80 || Math.abs(j) == 80) {
                    obstacles.add(new Obstacle(centerX + i, centerY + j, 40, 40, Obstacle.Type.STEEL));
                }
            }
        }
        
        // 内层核心 - 不可摧毁的钢铁
        obstacles.add(new Obstacle(centerX - 20, centerY - 20, 40, 40, Obstacle.Type.STEEL));
        
        // 四个入口的守卫塔
        obstacles.add(new Obstacle(centerX - 60, centerY - 140, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX + 20, centerY - 140, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX - 60, centerY + 100, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX + 20, centerY + 100, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX - 140, centerY - 60, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX - 140, centerY + 20, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX + 100, centerY - 60, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(centerX + 100, centerY + 20, 40, 40, Obstacle.Type.BRICK));
    }
    
    /**
     * 创建多层迷宫系统
     */
    private void createComplexMazeSystem() {
        // 左上角复杂迷宫
        createQuadrantMaze(100, 100, 350, 250);
        
        // 右上角迷宫
        createQuadrantMaze(550, 100, 350, 250);
        
        // 左下角迷宫  
        createQuadrantMaze(100, 450, 350, 250);
        
        // 右下角迷宫
        createQuadrantMaze(550, 450, 350, 250);
    }
    
    private void createQuadrantMaze(int startX, int startY, int width, int height) {
        // 创建迷宫骨架
        for (int x = startX; x < startX + width; x += 80) {
            for (int y = startY; y < startY + height; y += 80) {
                if (Math.random() < 0.6) { // 60%概率放置障碍物
                    Obstacle.Type type = Math.random() < 0.7 ? Obstacle.Type.BRICK : Obstacle.Type.STEEL;
                    obstacles.add(new Obstacle(x, y, 40, 40, type));
                }
            }
        }
        
        // 添加一些连接通道
        for (int x = startX + 40; x < startX + width - 40; x += 120) {
            obstacles.add(new Obstacle(x, startY + height/2, 80, 40, Obstacle.Type.BRICK));
        }
    }
    
    /**
     * 创建战术防御点
     */
    private void createTacticalDefenses() {
        // 角落堡垒
        createCornerFortress(60, 60, Obstacle.Type.STEEL);
        createCornerFortress(gameWidth - 100, 60, Obstacle.Type.STEEL);
        createCornerFortress(60, gameHeight - 100, Obstacle.Type.STEEL);
        createCornerFortress(gameWidth - 100, gameHeight - 100, Obstacle.Type.STEEL);
        
        // 中间防线
        for (int x = 200; x < gameWidth - 200; x += 100) {
            obstacles.add(new Obstacle(x, 200, 40, 80, Obstacle.Type.BRICK));
            obstacles.add(new Obstacle(x, gameHeight - 280, 40, 80, Obstacle.Type.BRICK));
        }
        
        for (int y = 300; y < gameHeight - 300; y += 100) {
            obstacles.add(new Obstacle(200, y, 80, 40, Obstacle.Type.BRICK));
            obstacles.add(new Obstacle(gameWidth - 280, y, 80, 40, Obstacle.Type.BRICK));
        }
    }
    
    private void createCornerFortress(int x, int y, Obstacle.Type type) {
        obstacles.add(new Obstacle(x, y, 40, 40, type));
        obstacles.add(new Obstacle(x + 40, y, 40, 40, type));
        obstacles.add(new Obstacle(x, y + 40, 40, 40, type));
    }
    
    /**
     * 创建自然地形（河流、湖泊）
     */
    private void createNaturalTerrain() {
        // 中央大湖
        createLake(gameWidth/2 - 60, gameHeight/2 + 150, 120, 80);
        
        // 四个小湖泊
        createLake(150, 300, 60, 60);
        createLake(750, 200, 80, 60);
        createLake(200, 600, 60, 80);
        createLake(700, 550, 80, 60);
        
        // 蜿蜒河流
        createRiver();
    }
    
    private void createLake(int x, int y, int width, int height) {
        for (int i = 0; i < width; i += 20) {
            for (int j = 0; j < height; j += 20) {
                obstacles.add(new Obstacle(x + i, y + j, 20, 20, Obstacle.Type.WATER));
            }
        }
    }
    
    private void createRiver() {
        // 从左到右的蜿蜒河流
        int riverY = gameHeight / 3;
        for (int x = 100; x < gameWidth - 100; x += 20) {
            int waveOffset = (int)(Math.sin(x * 0.02) * 30);
            obstacles.add(new Obstacle(x, riverY + waveOffset, 20, 40, Obstacle.Type.WATER));
        }
    }
    
    /**
     * 创建掩体群
     */
    private void createCoverClusters() {
        // 随机分布的掩体群
        for (int i = 0; i < 15; i++) {
            int x = (int)(Math.random() * (gameWidth - 120)) + 60;
            int y = (int)(Math.random() * (gameHeight - 120)) + 60;
            
            // 确保不在中央城堡区域
            if (Math.abs(x - gameWidth/2) < 150 && Math.abs(y - gameHeight/2) < 150) {
                continue;
            }
            
            createCoverCluster(x, y);
        }
    }
    
    private void createCoverCluster(int centerX, int centerY) {
        Obstacle.Type type = Math.random() < 0.6 ? Obstacle.Type.BRICK : Obstacle.Type.STEEL;
        int size = (int)(Math.random() * 3) + 2; // 2-4个障碍物
        
        for (int i = 0; i < size; i++) {
            int offsetX = (int)(Math.random() * 80) - 40;
            int offsetY = (int)(Math.random() * 80) - 40;
            obstacles.add(new Obstacle(centerX + offsetX, centerY + offsetY, 40, 40, type));
        }
    }
    
    /**
     * 创建狙击点
     */
    private void createSniperPoints() {
        // 高地狙击点（用钢铁围成的小房间）
        createSniperNest(150, 150);
        createSniperNest(gameWidth - 190, 150);
        createSniperNest(150, gameHeight - 190);
        createSniperNest(gameWidth - 190, gameHeight - 190);
        
        // 中间的观察塔
        createWatchTower(gameWidth/2, 100);
        createWatchTower(gameWidth/2, gameHeight - 140);
        createWatchTower(100, gameHeight/2);
        createWatchTower(gameWidth - 140, gameHeight/2);
    }
    
    private void createSniperNest(int x, int y) {
        // L型掩体
        obstacles.add(new Obstacle(x, y, 40, 80, Obstacle.Type.STEEL));
        obstacles.add(new Obstacle(x, y, 80, 40, Obstacle.Type.STEEL));
    }
    
    private void createWatchTower(int x, int y) {
        // 十字形观察塔
        obstacles.add(new Obstacle(x - 20, y, 40, 80, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(x - 40, y + 20, 80, 40, Obstacle.Type.BRICK));
    }
    
    /**
     * 创建通道和桥梁
     */
    private void createPassagesAndBridges() {
        // 确保主要通道畅通
        clearMainPassages();
        
        // 在水域上建桥
        createBridge(gameWidth/2 - 20, gameHeight/3 - 20, 40, 80);
        createBridge(300, 350, 80, 40);
    }
    
    private void clearMainPassages() {
        // 清理十字形主通道
        for (int x = gameWidth/2 - 30; x <= gameWidth/2 + 30; x += 20) {
            for (int y = 100; y < gameHeight - 100; y += 20) {
                removeObstacleAt(x, y);
            }
        }
        
        for (int y = gameHeight/2 - 30; y <= gameHeight/2 + 30; y += 20) {
            for (int x = 100; x < gameWidth - 100; x += 20) {
                removeObstacleAt(x, y);
            }
        }
    }
    
    private void createBridge(int x, int y, int width, int height) {
        // 移除水域，创建可通行的桥梁
        for (int i = 0; i < width; i += 20) {
            for (int j = 0; j < height; j += 20) {
                removeObstacleAt(x + i, y + j);
            }
        }
    }
    
    private void removeObstacleAt(int x, int y) {
        obstacles.removeIf(obstacle -> 
            obstacle.getX() <= x && x < obstacle.getX() + obstacle.getWidth() &&
            obstacle.getY() <= y && y < obstacle.getY() + obstacle.getHeight()
        );
    }
    
    private void createMazeArea() {
        // 左上迷宫
        obstacles.add(new Obstacle(150, 100, 40, 120, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(250, 80, 40, 80, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(350, 120, 80, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(200, 240, 120, 40, Obstacle.Type.BRICK));
        
        // 右上迷宫
        obstacles.add(new Obstacle(600, 80, 40, 100, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(720, 100, 40, 120, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(680, 180, 80, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(550, 220, 120, 40, Obstacle.Type.BRICK));
        
        // 左下迷宫
        obstacles.add(new Obstacle(120, 500, 80, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(180, 580, 40, 80, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(280, 520, 40, 120, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(350, 600, 80, 40, Obstacle.Type.BRICK));
        
        // 右下迷宫
        obstacles.add(new Obstacle(650, 550, 80, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(720, 500, 40, 100, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(600, 620, 40, 80, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(550, 580, 80, 40, Obstacle.Type.BRICK));
    }
    
    private void createScatteredDefenses() {
        // 随机分布的小型防御工事
        obstacles.add(new Obstacle(80, 300, 40, 40, Obstacle.Type.STEEL));
        obstacles.add(new Obstacle(880, 150, 40, 40, Obstacle.Type.STEEL));
        obstacles.add(new Obstacle(150, 650, 40, 40, Obstacle.Type.STEEL));
        obstacles.add(new Obstacle(800, 650, 40, 40, Obstacle.Type.STEEL));
        
        // 一些砖块群组
        obstacles.add(new Obstacle(450, 120, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(490, 120, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(470, 160, 40, 40, Obstacle.Type.BRICK));
        
        obstacles.add(new Obstacle(200, 400, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(240, 400, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(220, 440, 40, 40, Obstacle.Type.BRICK));
        
        obstacles.add(new Obstacle(750, 300, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(790, 300, 40, 40, Obstacle.Type.BRICK));
        obstacles.add(new Obstacle(770, 340, 40, 40, Obstacle.Type.BRICK));
    }
    
    private void createWaterAreas() {
        // 创建水域（使用特殊的障碍物类型）
        obstacles.add(new Obstacle(400, 450, 80, 80, Obstacle.Type.WATER));
        obstacles.add(new Obstacle(520, 450, 80, 80, Obstacle.Type.WATER));
        obstacles.add(new Obstacle(460, 530, 80, 60, Obstacle.Type.WATER));
    }

    public void startGame() {
        // 询问玩家AI数量
        String input = JOptionPane.showInputDialog(this, 
            "请输入AI敌人数量 (1-8):", 
            "设置敌人数量", 
            JOptionPane.QUESTION_MESSAGE);
        
        int aiTankCount = 3; // 默认3个
        try {
            if (input != null && !input.trim().isEmpty()) {
                aiTankCount = Integer.parseInt(input.trim());
                aiTankCount = Math.max(1, Math.min(8, aiTankCount)); // 限制在1-8之间
            }
        } catch (NumberFormatException e) {
            aiTankCount = 3;
        }
        
        startGame(aiTankCount);
    }
    
    public void startGame(int aiTankCount) {
        // 创建玩家坦克
        if (multiplayer) {
            if (isHost) {
                playerTank = new Tank(100, 100, Color.BLUE, true, "host");
            } else {
                playerTank = new Tank(800, 600, Color.RED, true, "client");
            }
        } else {
            // 随机选择玩家出生点
            Point playerSpawn = getRandomSafeSpawnPoint();
            playerTank = new Tank(playerSpawn.x, playerSpawn.y, Color.BLUE, true, "player1");
            
            // 创建指定数量的AI坦克
            Color[] aiColors = {Color.RED, Color.GREEN, Color.YELLOW, Color.ORANGE, 
                               Color.PINK, Color.CYAN, Color.MAGENTA, Color.LIGHT_GRAY,
                               Color.DARK_GRAY, new Color(128, 0, 128)}; // 紫色
            
            for (int i = 0; i < aiTankCount && i < 8; i++) { // 最多8个AI坦克
                Point spawnPoint = getRandomSafeSpawnPoint();
                // 确保与其他坦克保持距离
                while (isTooCloseToOtherTanks(spawnPoint)) {
                    spawnPoint = getRandomSafeSpawnPoint();
                }
                
                Color aiColor = aiColors[i % aiColors.length];
                Tank aiTank = new Tank(spawnPoint.x, spawnPoint.y, aiColor, false, "ai" + (i + 1));
                aiTank.heal(10);
                tanks.add(aiTank);
            }
        }
        
        tanks.add(playerTank);
        gameRunning = true;
        gameTimer.start();
        
        System.out.println("[游戏开始] 玩家坦克位置: (" + playerTank.getX() + ", " + playerTank.getY() + ")");
        System.out.println("[游戏开始] 创建了 " + (tanks.size() - 1) + " 个AI坦克");
    }
    
    /**
     * 获取随机安全出生点
     */
    private Point getRandomSafeSpawnPoint() {
        int maxAttempts = 100;
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            int x = 60 + (int)(Math.random() * (gameWidth - 120));
            int y = 60 + (int)(Math.random() * (gameHeight - 120));
            
            // 检查是否与障碍物冲突
            Rectangle spawnArea = new Rectangle(x - 20, y - 20, 80, 80);
            boolean safe = true;
            
            for (Obstacle obstacle : obstacles) {
                if (obstacle.isDestroyed()) continue;
                Rectangle obstacleArea = new Rectangle(
                    obstacle.getX(), obstacle.getY(),
                    obstacle.getWidth(), obstacle.getHeight()
                );
                
                if (spawnArea.intersects(obstacleArea)) {
                    safe = false;
                    break;
                }
            }
            
            // 避免在中央城堡核心区域出生
            int centerX = gameWidth / 2;
            int centerY = gameHeight / 2;
            if (Math.abs(x - centerX) < 100 && Math.abs(y - centerY) < 100) {
                safe = false;
            }
            
            if (safe) {
                return new Point(x, y);
            }
            
            attempts++;
        }
        
        // 如果找不到安全位置，返回默认位置
        System.out.println("[警告] 无法找到安全出生点，使用默认位置");
        return new Point(100, 100);
    }
    
    /**
     * 检查出生点是否与其他坦克太近
     */
    private boolean isTooCloseToOtherTanks(Point spawnPoint) {
        for (Tank tank : tanks) {
            double distance = Math.sqrt(
                Math.pow(spawnPoint.x - tank.getX(), 2) + 
                Math.pow(spawnPoint.y - tank.getY(), 2)
            );
            if (distance < 120) { // 最小距离120像素
                return true;
            }
        }
        return false;
    }
    
    private void updateGame() {
        // 更新背景渲染器
        if (backgroundRenderer != null) {
            backgroundRenderer.update();
        }
        
        // 处理按键输入
        handleInput();
        
        // 更新所有坦克
        for (Tank tank : tanks) {
            tank.updateBullets();
            tank.updatePowerUpEffects();
        }
        
        // 并行处理AI逻辑（仅单人模式）
        if (!multiplayer) {
            updateAllAIParallel();
        }
        
        // 检查子弹碰撞
        checkBulletCollisions();
        
        // 更新爆炸效果
        updateExplosions();
        
        // 更新道具系统
        updatePowerUps();
        
        // 更新时间停止效果
        updateTimeStop();
        
        // 移除死亡的坦克
        tanks.removeIf(tank -> tank.isDead() && !tank.isPlayer());
        
        // 检查游戏结束条件
        if (playerTank.isDead()) {
            gameOver("游戏结束！");
        } else if (!multiplayer && tanks.size() == 1) {
            gameOver("胜利！");
        }
    }
    
    private void handleInput() {
        if (playerTank.isDead()) return;
        
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) {
            if (playerTank.tryMove(Tank.Direction.UP, obstacles)) {
                sendNetworkMessage("MOVE:" + playerTank.getPlayerId() + ":UP:" + playerTank.getX() + ":" + playerTank.getY());
            }
        }
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) {
            if (playerTank.tryMove(Tank.Direction.DOWN, obstacles)) {
                sendNetworkMessage("MOVE:" + playerTank.getPlayerId() + ":DOWN:" + playerTank.getX() + ":" + playerTank.getY());
            }
        }
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) {
            if (playerTank.tryMove(Tank.Direction.LEFT, obstacles)) {
                sendNetworkMessage("MOVE:" + playerTank.getPlayerId() + ":LEFT:" + playerTank.getX() + ":" + playerTank.getY());
            }
        }
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            if (playerTank.tryMove(Tank.Direction.RIGHT, obstacles)) {
                sendNetworkMessage("MOVE:" + playerTank.getPlayerId() + ":RIGHT:" + playerTank.getX() + ":" + playerTank.getY());
            }
        }
        if (pressedKeys.contains(KeyEvent.VK_SPACE)) {
            Bullet bullet = playerTank.fire();
            if (bullet != null) {
                sendNetworkMessage("FIRE:" + playerTank.getPlayerId() + ":" + bullet.getX() + ":" + bullet.getY() + ":" + bullet.getDirection());
            }
        }
    }
    
    private void sendNetworkMessage(String message) {
        if (multiplayer && networkManager != null) {
            if (isHost) {
                networkManager.broadcastMessage(message);
            } else {
                networkManager.sendToServer(message);
            }
        }
    }
    
    private void checkBulletCollisions() {
        for (Tank tank : tanks) {
            for (Bullet bullet : tank.getBullets()) {
                if (!bullet.isActive()) continue;
                
                // 检查与其他坦克的碰撞
                for (Tank otherTank : tanks) {
                    if (bullet.checkCollision(otherTank)) {
                        otherTank.takeDamage(bullet.getDamage());
                        // 创建爆炸效果
                        explosions.add(new Explosion(bullet.getX() + bullet.getWidth()/2, bullet.getY() + bullet.getHeight()/2));
                        bullet.destroy();
                        break;
                    }
                }
                
                // 检查与道具的碰撞
                for (PowerUp powerUp : powerUps) {
                    if (powerUp.checkCollision(tanks.get(0))) { // 只有玩家可以拾取道具
                        powerUp.applyEffect(tanks.get(0), this);
                        System.out.println("[道具] 玩家获得: " + powerUp.getType().getName());
                        break;
                    }
                }
                
                // 检查与障碍物的碰撞
                for (Obstacle obstacle : obstacles) {
                    if (obstacle.checkCollision(bullet)) {
                        // 创建爆炸效果
                        explosions.add(new Explosion(bullet.getX() + bullet.getWidth()/2, bullet.getY() + bullet.getHeight()/2));
                        bullet.destroy();
                        if (obstacle.getType() == Obstacle.Type.BRICK) {
                            obstacle.takeDamage(bullet.getDamage());
                        }
                        break;
                    }
                }
            }
        }
        
        // 移除被摧毁的障碍物
        obstacles.removeIf(Obstacle::isDestroyed);
    }
    
    /**
     * 更新爆炸效果
     */
    private void updateExplosions() {
        Iterator<Explosion> iterator = explosions.iterator();
        while (iterator.hasNext()) {
            Explosion explosion = iterator.next();
            explosion.update();
            if (!explosion.isActive()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * 并行更新所有AI
     */
    private void updateAllAIParallel() {
        // 获取所有AI坦克
        List<Tank> aiTanks = tanks.stream()
                .filter(tank -> !tank.isPlayer() && !tank.isDead())
                .collect(java.util.stream.Collectors.toList());
        
        // 并行处理每个AI坦克的决策
        aiTanks.parallelStream().forEach(this::updateAI);
        
        // 处理已完成的AI决策
        processCompletedAIDecisions();
    }
    
    /**
     * 处理已完成的AI决策
     */
    private void processCompletedAIDecisions() {
        for (Map.Entry<String, Future<AIDecisionService.AIDecision>> entry : pendingAIDecisions.entrySet()) {
            String tankId = entry.getKey();
            Future<AIDecisionService.AIDecision> future = entry.getValue();
            
            if (future.isDone()) {
                try {
                    AIDecisionService.AIDecision decision = future.get(1, TimeUnit.MILLISECONDS);
                    Tank aiTank = findTankById(tankId);
                    if (aiTank != null && !aiTank.isDead()) {
                        executeAIDecisionWithFallback(aiTank, decision);
                        lastAIDecisions.put(tankId, decision);
                        aiTank.updateDecisionTime();
                        
                        // 记录决策间隔时间
                        long currentTime = System.currentTimeMillis();
                        Long lastDecisionTime = aiDecisionTimes.get(tankId);
                        if (lastDecisionTime != null) {
                            double intervalSeconds = (currentTime - lastDecisionTime) / 1000.0;
                            System.out.println("[AI间隔] 坦克 " + tankId + " 下次决策间隔: " + String.format("%.3f", intervalSeconds) + "秒");
                        }
                        aiDecisionTimes.put(tankId, currentTime);
                    }
                } catch (Exception e) {
                    Tank aiTank = findTankById(tankId);
                    if (aiTank != null && !aiTank.isDead()) {
                        updateTraditionalAI(aiTank);
                    }
                } finally {
                    pendingAIDecisions.remove(tankId);
                }
            }
        }
    }
    
    /**
     * 根据ID查找坦克
     */
    private Tank findTankById(String tankId) {
        return tanks.stream()
                .filter(tank -> tank.getPlayerId().equals(tankId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 智能AI逻辑 - 使用大模型决策（改进版）
     */
    private void updateAI(Tank aiTank) {
        if (playerTank == null || playerTank.isDead()) return;
        
        // 为每个AI坦克初始化状态数据（如果不存在）
        if (!aiTank.hasAIState()) {
            aiTank.initAIState();
        }
        
        // 更新AI状态
        aiTank.updateAIState();
        
        // 检测是否被卡住（连续几帧没有移动）
        detectAndHandleStuckAI(aiTank);
        
        // 更加频繁的AI决策（每1-2秒决策一次）
        if (aiTank.canMakeDecision()) {
            String tankId = aiTank.getPlayerId();
            
            // 如果没有正在进行的决策，立即启动新的异步决策
            if (!pendingAIDecisions.containsKey(tankId)) {
                Future<AIDecisionService.AIDecision> future = aiExecutor.submit(() -> {
                    try {
                        return aiDecisionService.getDecision(aiTank, playerTank, obstacles);
                    } catch (Exception e) {
                        System.err.println("[AI错误] AI决策异常: " + e.getMessage());
                        // 返回默认决策
                        return new AIDecisionService.AIDecision(AIDecisionService.AIDecision.ActionType.WAIT, null);
                    }
                });
                pendingAIDecisions.put(tankId, future);
            }
        } else {
            // 在等待期间执行更积极的行为
            if (Math.random() < 0.1) { // 10%概率执行即时行动
                handleImmediateThreats(aiTank);
            }
            // 继续执行上一次的决策（如果有的话）
            AIDecisionService.AIDecision lastDecision = lastAIDecisions.get(aiTank.getPlayerId());
            if (lastDecision != null && Math.random() < 0.3) { // 30%概率重复上次决策
                executeAIDecisionWithFallback(aiTank, lastDecision);
            }
        }
    }
    
    /**
     * 检测并处理被卡住的AI
     */
    private void detectAndHandleStuckAI(Tank aiTank) {
        // 这里需要在Tank类中添加位置记录功能
        // 如果发现AI在同一位置停留太久，强制改变方向
        if (isAIStuck(aiTank)) {
            forceAIDirectionChange(aiTank);
        }
    }
    
    /**
     * 判断AI是否被卡住
     */
    private boolean isAIStuck(Tank aiTank) {
        // 简单的被卡检测：如果坦克周围都是障碍物
        int blockedDirections = 0;
        for (Tank.Direction dir : Tank.Direction.values()) {
            if (wouldCollideWithObstacle(aiTank, dir)) {
                blockedDirections++;
            }
        }
        return blockedDirections >= 3; // 如果3个或更多方向被阻挡，认为被卡住
    }
    
    /**
     * 强制AI改变方向
     */
    private void forceAIDirectionChange(Tank aiTank) {
        // 寻找唯一可行的方向
        for (Tank.Direction dir : Tank.Direction.values()) {
            if (aiTank.tryMove(dir, obstacles)) {
                aiTank.resetMoveCooldown(120); // 强制较长的移动冷却
                System.out.println("[AI反卡] 坦克 " + aiTank.getPlayerId() + " 强制移动到方向: " + dir);
                return;
            }
        }
    }
    
    /**
     * 判断AI是否在障碍物前（用于碰撞预测）
     */
    private boolean wouldCollideWithObstacle(Tank tank, Tank.Direction direction) {
        int futureX = tank.getX();
        int futureY = tank.getY();
        int speed = 1; // 坦克速度
        
        switch (direction) {
            case UP:
                futureY -= speed * 5; // 预测5帧后的位置
                break;
            case DOWN:
                futureY += speed * 5;
                break;
            case LEFT:
                futureX -= speed * 5;
                break;
            case RIGHT:
                futureX += speed * 5;
                break;
        }
        
        // 检查是否会与障碍物碰撞
        Rectangle futureBounds = new Rectangle(futureX, futureY, tank.getWidth(), tank.getHeight());
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isDestroyed()) continue;
            Rectangle obstacleBounds = new Rectangle(
                obstacle.getX(), obstacle.getY(),
                obstacle.getWidth(), obstacle.getHeight()
            );
            if (futureBounds.intersects(obstacleBounds)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理紧急威胁（如即将碰撞）
     */
    private void handleImmediateThreats(Tank aiTank) {
        // 如果当前方向会导致碰撞，立即改变方向
        if (wouldCollideWithObstacle(aiTank, aiTank.getDirection())) {
            findSafeDirection(aiTank);
        }
    }
    
    /**
     * 执行AI决策（原版本）
     */
    private void executeAIDecision(Tank aiTank, AIDecisionService.AIDecision decision) {
        switch (decision.getActionType()) {
            case MOVE:
                if (decision.getDirection() != null) {
                    if (aiTank.tryMove(decision.getDirection(), obstacles)) {
                        aiTank.resetMoveCooldown(60 + (int)(Math.random() * 40));
                    } else {
                        // 如果指定方向会碰撞，尝试其他安全方向
                        findSafeDirection(aiTank);
                    }
                }
                break;
            case SHOOT:
                attackPlayer(aiTank);
                break;
            case WAIT:
                // 什么都不做，等待下一次决策
                break;
        }
    }
    
    /**
     * 执行AI决策（改进版本，带回退机制）
     */
    private void executeAIDecisionWithFallback(Tank aiTank, AIDecisionService.AIDecision decision) {
        switch (decision.getActionType()) {
            case MOVE:
                if (decision.getDirection() != null) {
                    if (aiTank.tryMove(decision.getDirection(), obstacles)) {
                        aiTank.resetMoveCooldown(120 + (int)(Math.random() * 60)); // 增加冷却时间
                    } else {
                        // 使用传统逻辑作为后备
                        moveTowardPlayerSmooth(aiTank);
                    }
                } else {
                    // 如果没有指定方向，使用传统移动逻辑
                    moveTowardPlayerSmooth(aiTank);
                }
                break;
            case SHOOT:
                attackPlayer(aiTank);
                break;
            case WAIT:
                // 等待的时候做一些微调整
                if (Math.random() < 0.3) { // 30%概率执行微移动
                    handleImmediateThreats(aiTank);
                }
                break;
        }
    }
    
    /**
     * 寻找安全的移动方向
     */
    private void findSafeDirection(Tank aiTank) {
        Tank.Direction[] directions = Tank.Direction.values();
        for (Tank.Direction dir : directions) {
            if (aiTank.tryMove(dir, obstacles)) {
                aiTank.resetMoveCooldown(80 + (int)(Math.random() * 40));
                return;
            }
        }
    }
    
    /**
     * 传统AI逻辑（作为后备方案）
     */
    private void updateTraditionalAI(Tank aiTank) {
        // 计算与玩家的距离
        double distanceToPlayer = Math.sqrt(
            Math.pow(aiTank.getX() - playerTank.getX(), 2) + 
            Math.pow(aiTank.getY() - playerTank.getY(), 2)
        );
        
        // 根据距离和状态决定行为
        if (distanceToPlayer < 250) {
            // 距离较近时，优先攻击
            if (aiTank.shouldShoot()) {
                attackPlayer(aiTank);
            }
            // 但也要继续移动以保持或获得更好位置
            if (aiTank.shouldMove()) {
                moveTowardPlayerSmooth(aiTank);
            }
        } else {
            // 距离较远时，主要移动靠近玩家
            if (aiTank.shouldMove()) {
                moveTowardPlayerSmooth(aiTank);
            }
            // 偶尔进行远程射击
            if (aiTank.shouldShoot() && Math.random() < 0.3) {
                attackPlayer(aiTank);
            }
        }
    }

    /**
     * AI攻击玩家
     */
    private void attackPlayer(Tank aiTank) {
        if (playerTank == null || playerTank.isDead()) return;
        
        // 计算朝向玩家的方向
        int dx = playerTank.getX() - aiTank.getX();
        int dy = playerTank.getY() - aiTank.getY();
        
        Tank.Direction targetDirection;
        if (Math.abs(dx) > Math.abs(dy)) {
            targetDirection = dx > 0 ? Tank.Direction.RIGHT : Tank.Direction.LEFT;
        } else {
            targetDirection = dy > 0 ? Tank.Direction.DOWN : Tank.Direction.UP;
        }
        
        // 转向目标方向
        aiTank.setDirection(targetDirection);
        
        // 射击
        if (Math.random() < 0.6) { // 60%概率射击
            Bullet bullet = aiTank.fire();
            if (bullet != null) {
                aiTank.resetShootCooldown(40 + (int)(Math.random() * 30)); // 40-70帧后再射击
            }
        }
    }
    
    /**
     * AI移动朝向玩家
     */
    private void moveTowardPlayer(Tank aiTank) {
        if (playerTank == null || playerTank.isDead()) return;
        
        int dx = playerTank.getX() - aiTank.getX();
        int dy = playerTank.getY() - aiTank.getY();
        
        // 选择主要移动方向
        Tank.Direction moveDirection;
        if (Math.abs(dx) > Math.abs(dy)) {
            moveDirection = dx > 0 ? Tank.Direction.RIGHT : Tank.Direction.LEFT;
        } else {
            moveDirection = dy > 0 ? Tank.Direction.DOWN : Tank.Direction.UP;
        }
        
        // 检查移动是否会撞到障碍物
        if (!wouldCollideWithObstacle(aiTank, moveDirection)) {
            aiTank.move(moveDirection);
        } else {
            // 如果会撞到障碍物，尝试其他方向
            Tank.Direction[] alternatives = getAlternativeDirections(moveDirection);
            for (Tank.Direction alt : alternatives) {
                if (!wouldCollideWithObstacle(aiTank, alt)) {
                    aiTank.move(alt);
                    break;
                }
            }
        }
    }
    
    /**
     * AI平滑移动朝向玩家
     */
    private void moveTowardPlayerSmooth(Tank aiTank) {
        if (playerTank == null || playerTank.isDead()) return;
        
        int dx = playerTank.getX() - aiTank.getX();
        int dy = playerTank.getY() - aiTank.getY();
        
        // 先尝试水平移动，再尝试垂直移动（或相反）
        Tank.Direction primaryDirection = null;
        Tank.Direction secondaryDirection = null;
        
        if (Math.abs(dx) > Math.abs(dy)) {
            // 水平距离更大，优先水平移动
            primaryDirection = dx > 0 ? Tank.Direction.RIGHT : Tank.Direction.LEFT;
            secondaryDirection = dy > 0 ? Tank.Direction.DOWN : Tank.Direction.UP;
        } else {
            // 垂直距离更大或相等，优先垂直移动
            primaryDirection = dy > 0 ? Tank.Direction.DOWN : Tank.Direction.UP;
            secondaryDirection = dx > 0 ? Tank.Direction.RIGHT : Tank.Direction.LEFT;
        }
        
        // 尝试主方向
        if (!wouldCollideWithObstacle(aiTank, primaryDirection)) {
            aiTank.move(primaryDirection);
            aiTank.resetMoveCooldown(60 + (int)(Math.random() * 40)); // 60-100帧后再移动
            return;
        }
        
        // 尝试次方向
        if (!wouldCollideWithObstacle(aiTank, secondaryDirection)) {
            aiTank.move(secondaryDirection);
            aiTank.resetMoveCooldown(80 + (int)(Math.random() * 40)); // 80-120帧后再移动
            return;
        }
        
        // 都不行，尝试其他方向
        Tank.Direction[] alternatives = getAlternativeDirections(primaryDirection);
        for (Tank.Direction alt : alternatives) {
            if (!wouldCollideWithObstacle(aiTank, alt)) {
                aiTank.move(alt);
                aiTank.resetMoveCooldown(100 + (int)(Math.random() * 50)); // 100-150帧后再移动
                return;
            }
        }
    }
    
    /**
     * AI避免障碍物
     */
    private void avoidObstacles(Tank aiTank) {
        // 检查当前方向是否会撞到障碍物
        if (wouldCollideWithObstacle(aiTank, aiTank.getDirection())) {
            // 寻找安全的移动方向
            Tank.Direction[] directions = Tank.Direction.values();
            for (Tank.Direction dir : directions) {
                if (!wouldCollideWithObstacle(aiTank, dir)) {
                    aiTank.move(dir);
                    return;
                }
            }
        }
    }
    
    
    /**
     * 获取替代移动方向
     */
    private Tank.Direction[] getAlternativeDirections(Tank.Direction primary) {
        switch (primary) {
            case UP:
                return new Tank.Direction[]{Tank.Direction.LEFT, Tank.Direction.RIGHT, Tank.Direction.DOWN};
            case DOWN:
                return new Tank.Direction[]{Tank.Direction.LEFT, Tank.Direction.RIGHT, Tank.Direction.UP};
            case LEFT:
                return new Tank.Direction[]{Tank.Direction.UP, Tank.Direction.DOWN, Tank.Direction.RIGHT};
            case RIGHT:
                return new Tank.Direction[]{Tank.Direction.UP, Tank.Direction.DOWN, Tank.Direction.LEFT};
            default:
                return Tank.Direction.values();
        }
    }
    
    private void gameOver(String message) {
        gameRunning = false;
        gameTimer.stop();
        
        // 关闭AI执行器
        if (aiExecutor != null) {
            aiExecutor.shutdownNow();
            try {
                if (!aiExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.err.println("AI执行器未能在1秒内关闭");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        JOptionPane.showMessageDialog(this, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 开启抗锯齿和高质量渲染
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // 渲染动态背景
        if (backgroundRenderer != null) {
            backgroundRenderer.render(g2d);
        }
        
        // 绘制障碍物
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g);
        }
        
        // 绘制所有坦克
        for (Tank tank : tanks) {
            tank.draw(g);
        }
        
        // 绘制道具
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g);
        }
        
        // 绘制爆炸效果
        for (Explosion explosion : explosions) {
            explosion.draw(g);
        }
        
        // 绘制UI信息
        drawUI(g);
    }
    
    private void drawUI(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("宋体", Font.BOLD, 16));
        
        if (playerTank != null) {
            g.drawString("生命值: " + playerTank.getHealth(), 10, 25);
            g.drawString("存活坦克: " + tanks.size(), 10, 50);
        }
        
        if (multiplayer) {
            g.drawString("多人模式 - " + (isHost ? "房主" : "客户端"), 10, 75);
        }
        
        // 绘制操作提示
        g.setFont(new Font("宋体", Font.PLAIN, 12));
        g.drawString("WASD/方向键: 移动", gameWidth - 150, 25);
        g.drawString("空格键: 射击", gameWidth - 150, 45);
    }

    @Override
    public void onMessageReceived(String message) {
        // 处理网络消息
        String[] parts = message.split(":");
        if (parts.length < 2) return;
        
        String command = parts[0];
        String playerId = parts[1];
        
        switch (command) {
            case "MOVE":
                if (parts.length >= 5) {
                    Tank.Direction direction = Tank.Direction.valueOf(parts[2]);
                    int x = Integer.parseInt(parts[3]);
                    int y = Integer.parseInt(parts[4]);
                    
                    // 更新对应玩家的坦克位置
                    for (Tank tank : tanks) {
                        if (tank.getPlayerId().equals(playerId)) {
                            tank.setX(x);
                            tank.setY(y);
                            tank.setDirection(direction);
                            break;
                        }
                    }
                    
                    // 如果是新玩家，创建新坦克
                    boolean found = false;
                    for (Tank tank : tanks) {
                        if (tank.getPlayerId().equals(playerId)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Color color = playerId.equals("host") ? Color.BLUE : Color.RED;
                        Tank newTank = new Tank(x, y, color, false, playerId);
                        tanks.add(newTank);
                    }
                }
                break;
                
            case "FIRE":
                if (parts.length >= 6) {
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    Tank.Direction direction = Tank.Direction.valueOf(parts[4]);
                    
                    // 为对应玩家创建子弹
                    for (Tank tank : tanks) {
                        if (tank.getPlayerId().equals(playerId)) {
                            Bullet bullet = new Bullet(x, y, direction, playerId);
                            tank.getBullets().add(bullet);
                            break;
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
    
    /**
     * 更新道具系统
     */
    private void updatePowerUps() {
        // 检查道具拾取
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.update();
            
            // 检查玩家是否拾取道具
            if (powerUp.checkCollision(playerTank)) {
                powerUp.applyEffect(playerTank, this);
                System.out.println("[道具] 玩家获得: " + powerUp.getType().getName());
                powerUpIterator.remove();
                continue;
            }
            
            // 移除过期道具
            if (!powerUp.isActive()) {
                powerUpIterator.remove();
            }
        }
        
        // 定期生成新道具
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpSpawn > POWER_UP_SPAWN_INTERVAL) {
            spawnRandomPowerUp();
            lastPowerUpSpawn = currentTime;
        }
    }
    
    /**
     * 生成随机道具
     */
    private void spawnRandomPowerUp() {
        if (powerUps.size() >= 3) return; // 最多3个道具同时存在
        
        // 找一个安全的生成位置
        Point spawnPoint = getRandomSafeSpawnPoint();
        
        // 随机道具类型
        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type randomType = types[(int)(Math.random() * types.length)];
        
        PowerUp powerUp = new PowerUp(spawnPoint.x, spawnPoint.y, randomType);
        powerUps.add(powerUp);
        
        System.out.println("[道具生成] 生成道具: " + randomType.getName() + " 位置: (" + spawnPoint.x + ", " + spawnPoint.y + ")");
    }
    
    /**
     * 更新时间停止效果
     */
    private void updateTimeStop() {
        if (timeStopActive && System.currentTimeMillis() > timeStopEndTime) {
            timeStopActive = false;
            System.out.println("[道具效果] 时间停止效果结束");
        }
    }
    
    /**
     * 激活时间停止效果
     */
    public void activateTimeStop(long duration) {
        timeStopActive = true;
        timeStopEndTime = System.currentTimeMillis() + duration;
        System.out.println("[道具效果] 时间停止效果激活，持续 " + (duration/1000) + " 秒");
    }
    
    /**
     * 检查时间停止是否激活
     */
    public boolean isTimeStopActive() {
        return timeStopActive;
    }
    
    /**
     * 在指定位置创建爆炸效果
     */
    public void createExplosionAt(int x, int y, int radius, int damage) {
        explosions.add(new Explosion(x, y));
        
        // 对范围内的坦克造成伤害
        for (Tank tank : tanks) {
            double distance = Math.sqrt(
                Math.pow(tank.getX() - x, 2) + 
                Math.pow(tank.getY() - y, 2)
            );
            if (distance <= radius) {
                tank.takeDamage(damage);
            }
        }
        
        // 摧毁范围内的障碍物
        for (Obstacle obstacle : obstacles) {
            double distance = Math.sqrt(
                Math.pow(obstacle.getX() + obstacle.getWidth()/2 - x, 2) + 
                Math.pow(obstacle.getY() + obstacle.getHeight()/2 - y, 2)
            );
            if (distance <= radius && obstacle.getType() == Obstacle.Type.BRICK) {
                obstacle.takeDamage(damage);
            }
        }
        
        System.out.println("[爆炸] 在 (" + x + ", " + y + ") 创建爆炸，范围: " + radius + ", 伤害: " + damage);
    }
    
    /**
     * 应用时间停止效果（别名方法）
     */
    public void applyTimeStop(long duration) {
        activateTimeStop(duration);
    }
}
