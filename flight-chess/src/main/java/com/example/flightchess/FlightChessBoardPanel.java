package com.example.flightchess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 华丽的飞行棋棋盘面板
 * 参考传统飞行棋设计，支持多种对战模式
 */
public class FlightChessBoardPanel extends JPanel {
    private static final int BOARD_SIZE = 800;
    private static final int CELL_SIZE = 25;
    private static final int PLANE_SIZE = 18;
    private static final int HOME_AREA_SIZE = 140;
    private static final int CENTER_SIZE = 120;
    
    private FlightChessGame game;
    private FlightChessAI[] aiPlayers;
    private boolean[] isAIPlayer;
    private int diceValue;
    private boolean waitingForMove;
    private List<Integer> movablePlanes;
    
    // 状态更新回调
    private Runnable onStateUpdate;
    
    public FlightChessBoardPanel() {
        this.game = new FlightChessGame();
        this.aiPlayers = new FlightChessAI[4];
        this.isAIPlayer = new boolean[4];
        this.diceValue = 0;
        this.waitingForMove = false;
        
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }
    
    public void setOnStateUpdate(Runnable callback) {
        this.onStateUpdate = callback;
    }
    
    public void setAIPlayer(int player, boolean isAI, int difficulty) {
        isAIPlayer[player] = isAI;
        if (isAI) {
            aiPlayers[player] = new FlightChessAI(difficulty);
        } else {
            aiPlayers[player] = null;
        }
    }
    
    public void rollDice() {
        if (game.isGameOver()) {
            return;
        }
        
        diceValue = game.rollDice();
        movablePlanes = game.getMovablePlanes(game.getCurrentPlayer(), diceValue);
        
        if (movablePlanes.isEmpty()) {
            // 无法移动，切换到下一个玩家
            game.nextPlayer();
            waitingForMove = false;
            
            // 如果下一个玩家是AI，自动进行回合
            SwingUtilities.invokeLater(this::checkAITurn);
        } else {
            waitingForMove = true;
            
            // 如果当前玩家是AI，自动选择移动
            if (isAIPlayer[game.getCurrentPlayer()]) {
                SwingUtilities.invokeLater(this::performAIMove);
            }
        }
        
        updateState();
        repaint();
    }
    
    private void performAIMove() {
        if (!waitingForMove || game.isGameOver()) {
            return;
        }
        
        int currentPlayer = game.getCurrentPlayer();
        if (isAIPlayer[currentPlayer] && aiPlayers[currentPlayer] != null) {
            int planeIndex = aiPlayers[currentPlayer].getBestMove(game, currentPlayer, diceValue);
            if (planeIndex >= 0) {
                makeMove(planeIndex);
            }
        }
    }
    
    private void makeMove(int planeIndex) {
        if (!waitingForMove || game.isGameOver()) {
            return;
        }
        
        boolean moved = game.movePlane(game.getCurrentPlayer(), planeIndex, diceValue);
        if (moved) {
            waitingForMove = false;
            
            // 检查是否获胜
            if (game.isGameOver()) {
                updateState();
                repaint();
                return;
            }
            
            // 如果投掷6点，可以再次投掷
            if (diceValue != 6) {
                game.nextPlayer();
            }
            
            // 如果下一个玩家是AI，自动进行回合
            SwingUtilities.invokeLater(this::checkAITurn);
        }
        
        updateState();
        repaint();
    }
    
    private void checkAITurn() {
        if (!game.isGameOver() && !waitingForMove && isAIPlayer[game.getCurrentPlayer()]) {
            // AI玩家自动投掷骰子
            Timer timer = new Timer(1000, e -> rollDice());
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    private void handleMouseClick(int x, int y) {
        if (!waitingForMove || game.isGameOver() || isAIPlayer[game.getCurrentPlayer()]) {
            return;
        }
        
        // 检查点击的是否是可移动的飞机
        int currentPlayer = game.getCurrentPlayer();
        FlightChessGame.Plane[] planes = game.getPlayerPlanes(currentPlayer);
        
        for (int i = 0; i < planes.length; i++) {
            if (movablePlanes.contains(i)) {
                Point planePos = getPlaneScreenPosition(currentPlayer, i);
                if (Math.abs(x - planePos.x) <= PLANE_SIZE/2 && Math.abs(y - planePos.y) <= PLANE_SIZE/2) {
                    makeMove(i);
                    break;
                }
            }
        }
    }
    
    private Point getPlaneScreenPosition(int player, int planeIndex) {
        FlightChessGame.Plane plane = game.getPlayerPlanes(player)[planeIndex];
        
        if (plane.position == -1) {
            // 在家园
            return getHomePosition(player, planeIndex);
        } else if (plane.position >= FlightChessGame.MAIN_TRACK_SIZE) {
            // 在终点跑道
            return getFinishPosition(player, plane.position - FlightChessGame.MAIN_TRACK_SIZE);
        } else {
            // 在主跑道
            return getMainTrackPosition(plane.position);
        }
    }
    
    private Point getHomePosition(int player, int planeIndex) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        int homeOffset = HOME_AREA_SIZE / 2;
        
        int homeX, homeY;
        switch (player) {
            case 0: // 红色，左上
                homeX = centerX - homeOffset;
                homeY = centerY - homeOffset;
                break;
            case 1: // 蓝色，右上
                homeX = centerX + homeOffset;
                homeY = centerY - homeOffset;
                break;
            case 2: // 黄色，右下
                homeX = centerX + homeOffset;
                homeY = centerY + homeOffset;
                break;
            case 3: // 绿色，左下
                homeX = centerX - homeOffset;
                homeY = centerY + homeOffset;
                break;
            default:
                homeX = centerX;
                homeY = centerY;
        }
        
        // 在家园区域内排列飞机
        int offsetX = (planeIndex % 2) * 30 - 15;
        int offsetY = (planeIndex / 2) * 30 - 15;
        
        return new Point(homeX + offsetX, homeY + offsetY);
    }
    
    private Point getMainTrackPosition(int position) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        int trackRadius = 200;
        
        // 计算角度（顺时针）
        double angle = (position * 360.0 / FlightChessGame.MAIN_TRACK_SIZE) * Math.PI / 180;
        
        int x = centerX + (int)(trackRadius * Math.cos(angle));
        int y = centerY + (int)(trackRadius * Math.sin(angle));
        
        return new Point(x, y);
    }
    
    private Point getFinishPosition(int player, int finishPosition) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        int finishOffset = 50 + finishPosition * 20;
        
        switch (player) {
            case 0: // 红色，向中心
                return new Point(centerX - finishOffset, centerY);
            case 1: // 蓝色，向中心
                return new Point(centerX, centerY - finishOffset);
            case 2: // 黄色，向中心
                return new Point(centerX + finishOffset, centerY);
            case 3: // 绿色，向中心
                return new Point(centerX, centerY + finishOffset);
            default:
                return new Point(centerX, centerY);
        }
    }
    
    public void restartGame() {
        game.restart();
        diceValue = 0;
        waitingForMove = false;
        movablePlanes = null;
        updateState();
        repaint();
        
        // 如果第一个玩家是AI，开始AI回合
        checkAITurn();
    }
    
    public FlightChessGame getGame() {
        return game;
    }
    
    public int getDiceValue() {
        return diceValue;
    }
    
    public boolean isWaitingForMove() {
        return waitingForMove;
    }
    
    private void updateState() {
        if (onStateUpdate != null) {
            onStateUpdate.run();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBoard(g2d);
        drawPlanes(g2d);
        
        g2d.dispose();
    }
    
    private void drawBoard(Graphics2D g2d) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        
        // 绘制温暖的浅黄色背景
        g2d.setColor(new Color(255, 248, 220)); // 温暖的浅黄色，像柔和的阳光
        g2d.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);
        
        // 绘制四个彩色角落区域
        drawColorfulCorners(g2d);
        
        // 绘制四个飞机图案
        drawAirplanePatterns(g2d);
        
        // 绘制四个机库区域
        drawHangars(g2d);
        
        // 绘制外圈彩色跑道
        drawColorfulOuterTracks(g2d);
        
        // 绘制终点跑道
        drawFinishTracks(g2d);
        
        // 绘制中央彩色终点区
        drawCentralTarget(g2d, centerX, centerY);
        
        // 绘制外边框记录区
        drawProgressBorders(g2d);
    }
    
    /**
     * 绘制华丽的传统飞行棋背景
     */
    private void drawTraditionalBackground(Graphics2D g2d) {
        // 创建渐变背景
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(255, 248, 220), // 米色
            BOARD_SIZE, BOARD_SIZE, new Color(245, 222, 179) // 小麦色
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);
        
        // 绘制传统图案装饰
        drawTraditionalPatterns(g2d);
        
        // 绘制中心装饰图案
        drawCenterDecoration(g2d);
    }
    
    /**
     * 绘制传统图案
     */
    private void drawTraditionalPatterns(Graphics2D g2d) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        
        // 设置半透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        
        // 绘制四个角的传统云纹图案
        g2d.setColor(new Color(205, 133, 63)); // 秘鲁色
        drawCloudPattern(g2d, 80, 80);
        drawCloudPattern(g2d, BOARD_SIZE - 80, 80);
        drawCloudPattern(g2d, 80, BOARD_SIZE - 80);
        drawCloudPattern(g2d, BOARD_SIZE - 80, BOARD_SIZE - 80);
        
        // 恢复不透明
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * 绘制云纹图案
     */
    private void drawCloudPattern(Graphics2D g2d, int x, int y) {
        g2d.setStroke(new BasicStroke(2));
        int size = 40;
        
        // 绘制螺旋云纹
        for (int i = 0; i < 3; i++) {
            int radius = size - i * 10;
            for (double angle = 0; angle < Math.PI * 4; angle += Math.PI / 8) {
                double spiralRadius = radius * (1 - angle / (Math.PI * 4));
                int px = x + (int)(spiralRadius * Math.cos(angle));
                int py = y + (int)(spiralRadius * Math.sin(angle));
                g2d.fillOval(px - 2, py - 2, 4, 4);
            }
        }
    }
    
    /**
     * 绘制中心装饰图案
     */
    private void drawCenterDecoration(Graphics2D g2d) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        
        // 绘制外圆环装饰
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(new Color(184, 134, 11)); // 暗金色
        for (int i = 0; i < 3; i++) {
            int radius = 280 + i * 15;
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
        
        // 绘制八卦图案风格的装饰
        drawBaguaDecoration(g2d, centerX, centerY);
    }
    
    /**
     * 绘制八卦风格装饰
     */
    private void drawBaguaDecoration(Graphics2D g2d, int centerX, int centerY) {
        int radius = 350;
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(139, 69, 19)); // 马鞍棕色
        
        // 绘制八个方位的装饰线
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int x1 = centerX + (int)((radius - 20) * Math.cos(angle));
            int y1 = centerY + (int)((radius - 20) * Math.sin(angle));
            int x2 = centerX + (int)((radius + 20) * Math.cos(angle));
            int y2 = centerY + (int)((radius + 20) * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
            
            // 在每个方位绘制小装饰点
            int dotX = centerX + (int)(radius * Math.cos(angle));
            int dotY = centerY + (int)(radius * Math.sin(angle));
            g2d.fillOval(dotX - 3, dotY - 3, 6, 6);
        }
    }
    
    /**
     * 绘制装饰性边框
     */
    private void drawDecorativeBorder(Graphics2D g2d) {
        // 外边框
        g2d.setStroke(new BasicStroke(6));
        g2d.setColor(new Color(160, 82, 45)); // 马鞍棕色
        g2d.drawRect(10, 10, BOARD_SIZE - 20, BOARD_SIZE - 20);
        
        // 内边框
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(new Color(205, 133, 63)); // 秘鲁色
        g2d.drawRect(20, 20, BOARD_SIZE - 40, BOARD_SIZE - 40);
        
        // 绘制边框装饰图案
        drawBorderDecorations(g2d);
    }
    
    /**
     * 绘制边框装饰
     */
    private void drawBorderDecorations(Graphics2D g2d) {
        g2d.setColor(new Color(184, 134, 11));
        g2d.setStroke(new BasicStroke(2));
        
        // 上边装饰
        for (int i = 50; i < BOARD_SIZE - 50; i += 40) {
            drawDiamondPattern(g2d, i, 15);
        }
        
        // 下边装饰
        for (int i = 50; i < BOARD_SIZE - 50; i += 40) {
            drawDiamondPattern(g2d, i, BOARD_SIZE - 15);
        }
        
        // 左边装饰
        for (int i = 50; i < BOARD_SIZE - 50; i += 40) {
            drawDiamondPattern(g2d, 15, i);
        }
        
        // 右边装饰
        for (int i = 50; i < BOARD_SIZE - 50; i += 40) {
            drawDiamondPattern(g2d, BOARD_SIZE - 15, i);
        }
    }
    
    /**
     * 绘制菱形装饰图案
     */
    private void drawDiamondPattern(Graphics2D g2d, int x, int y) {
        int size = 8;
        int[] xPoints = {x, x + size, x, x - size};
        int[] yPoints = {y - size, y, y + size, y};
        g2d.fillPolygon(xPoints, yPoints, 4);
    }
    
    /**
     * 绘制主跑道
     */
    private void drawMainTrack(Graphics2D g2d, int centerX, int centerY) {
        // 主跑道外圈
        g2d.setColor(new Color(139, 69, 19)); // 马鞍棕色
        g2d.setStroke(new BasicStroke(8));
        int trackRadius = 200;
        g2d.drawOval(centerX - trackRadius - 15, centerY - trackRadius - 15, 
                    (trackRadius + 15) * 2, (trackRadius + 15) * 2);
        
        // 主跑道内圈
        g2d.setColor(new Color(205, 133, 63)); // 秘鲁色
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - trackRadius + 15, centerY - trackRadius + 15, 
                    (trackRadius - 15) * 2, (trackRadius - 15) * 2);
        
        // 跑道装饰线
        g2d.setColor(new Color(184, 134, 11)); // 暗金色
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(centerX - trackRadius, centerY - trackRadius, trackRadius * 2, trackRadius * 2);
    }
    
    /**
     * 绘制四个彩色角落区域
     * 左上红色，右上黄色，右下绿色，左下蓝色
     */
    private void drawColorfulCorners(Graphics2D g2d) {
        int cornerSize = 180;
        Color[] cornerColors = {
            new Color(220, 50, 50),   // 热烈的红色 - 左上
            new Color(255, 215, 0),   // 明亮的黄色 - 右上  
            new Color(50, 205, 50),   // 充满生机的绿色 - 右下
            new Color(30, 144, 255)   // 深邃清爽的蓝色 - 左下
        };
        
        int[][] cornerPositions = {
            {60, 60},                           // 左上
            {BOARD_SIZE - cornerSize - 60, 60}, // 右上
            {BOARD_SIZE - cornerSize - 60, BOARD_SIZE - cornerSize - 60}, // 右下
            {60, BOARD_SIZE - cornerSize - 60}  // 左下
        };
        
        for (int i = 0; i < 4; i++) {
            g2d.setColor(cornerColors[i]);
            g2d.fillRect(cornerPositions[i][0], cornerPositions[i][1], cornerSize, cornerSize);
            
            // 添加边框
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(cornerPositions[i][0], cornerPositions[i][1], cornerSize, cornerSize);
        }
    }
    
    /**
     * 绘制四个飞机图案
     * 每个角落的最外侧绘制大型飞机图案
     */
    private void drawAirplanePatterns(Graphics2D g2d) {
        Color[] planeColors = {
            new Color(220, 50, 50),   // 红色
            new Color(255, 215, 0),   // 黄色
            new Color(50, 205, 50),   // 绿色
            new Color(30, 144, 255)   // 蓝色
        };
        
        int[][] planePositions = {
            {150, 150},  // 左上
            {650, 150},  // 右上
            {650, 650},  // 右下
            {150, 650}   // 左下
        };
        
        double[] rotations = {-Math.PI/4, Math.PI/4, 3*Math.PI/4, -3*Math.PI/4}; // 机头朝向中心
        
        for (int i = 0; i < 4; i++) {
            g2d.setColor(planeColors[i]);
            drawAirplane(g2d, planePositions[i][0], planePositions[i][1], 40, rotations[i]);
        }
    }
    
    /**
     * 绘制飞机形状
     */
    private void drawAirplane(Graphics2D g2d, int x, int y, int size, double rotation) {
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.translate(x, y);
        g2dRotated.rotate(rotation);
        
        // 机身
        g2dRotated.fillOval(-size/6, -size/2, size/3, size);
        
        // 机翼
        g2dRotated.fillOval(-size/2, -size/8, size, size/4);
        
        // 机头
        int[] xPoints = {0, -size/8, size/8};
        int[] yPoints = {-size/2 - size/4, -size/2, -size/2};
        g2dRotated.fillPolygon(xPoints, yPoints, 3);
        
        // 机尾
        g2dRotated.fillOval(-size/8, size/2, size/4, size/6);
        
        g2dRotated.dispose();
    }
    
    /**
     * 绘制四个机库区域
     * 每个颜色区域内的四个圆形停机坪
     */
    private void drawHangars(Graphics2D g2d) {
        Color[] hangarColors = {
            new Color(220, 50, 50),   // 红色
            new Color(255, 215, 0),   // 黄色
            new Color(50, 205, 50),   // 绿色
            new Color(30, 144, 255)   // 蓝色
        };
        
        int[][] hangarCenters = {
            {150, 150},  // 左上
            {650, 150},  // 右上
            {650, 650},  // 右下
            {150, 650}   // 左下
        };
        
        for (int player = 0; player < 4; player++) {
            g2d.setColor(hangarColors[player].brighter());
            
            // 绘制四个机库格子
            for (int i = 0; i < 4; i++) {
                Point pos = getHangarPosition(player, i, hangarCenters[player][0], hangarCenters[player][1]);
                
                // 填充圆形停机坪
                g2d.fillOval(pos.x - 15, pos.y - 15, 30, 30);
                
                // 绘制边框
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(pos.x - 15, pos.y - 15, 30, 30);
                g2d.setColor(hangarColors[player].brighter());
            }
        }
    }
    
    /**
     * 获取机库格子位置
     */
    private Point getHangarPosition(int player, int hangarIndex, int centerX, int centerY) {
        int spacing = 25;
        double offsetX = ((hangarIndex % 2) - 0.5) * spacing;
        double offsetY = ((hangarIndex / 2) - 0.5) * spacing;
        
        return new Point((int)(centerX + offsetX), (int)(centerY + offsetY));
    }
    
    /**
     * 绘制外圈彩色跑道
     * 四条颜色跑道沿着棋盘四边环绕
     */
    private void drawColorfulOuterTracks(Graphics2D g2d) {
        Color[] trackColors = {
            new Color(220, 50, 50),   // 红色跑道
            new Color(255, 215, 0),   // 黄色跑道
            new Color(50, 205, 50),   // 绿色跑道
            new Color(30, 144, 255)   // 蓝色跑道
        };
        
        int trackWidth = 40;
        int margin = 280;
        
        // 绘制四边的跑道
        // 上边 - 黄色跑道
        g2d.setColor(trackColors[1]);
        g2d.fillRect(margin, margin - trackWidth/2, BOARD_SIZE - 2*margin, trackWidth);
        
        // 右边 - 绿色跑道  
        g2d.setColor(trackColors[2]);
        g2d.fillRect(BOARD_SIZE - margin - trackWidth/2, margin, trackWidth, BOARD_SIZE - 2*margin);
        
        // 下边 - 蓝色跑道
        g2d.setColor(trackColors[3]);
        g2d.fillRect(margin, BOARD_SIZE - margin - trackWidth/2, BOARD_SIZE - 2*margin, trackWidth);
        
        // 左边 - 红色跑道
        g2d.setColor(trackColors[0]);
        g2d.fillRect(margin - trackWidth/2, margin, trackWidth, BOARD_SIZE - 2*margin);
        
        // 绘制跑道上的格子和箭头
        drawTrackCells(g2d, trackColors);
    }
    
    /**
     * 绘制跑道格子和箭头
     */
    private void drawTrackCells(Graphics2D g2d, Color[] trackColors) {
        int cellSize = 25;
        int trackWidth = 40;
        int margin = 280;
        
        g2d.setStroke(new BasicStroke(2));
        
        // 绘制上边跑道格子
        for (int i = 0; i < 12; i++) {
            int x = margin + 40 + i * 35;
            int y = margin;
            
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - cellSize/2, y - cellSize/2, cellSize, cellSize);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - cellSize/2, y - cellSize/2, cellSize, cellSize);
            
            // 每隔几格画箭头
            if (i % 3 == 1) {
                drawArrow(g2d, x, y, 0); // 向右箭头
            }
        }
        
        // 类似地绘制其他三边的跑道格子...
        // 为简化，这里只展示上边的实现
    }
    
    /**
     * 绘制箭头
     */
    private void drawArrow(Graphics2D g2d, int x, int y, double angle) {
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.translate(x, y);
        g2dRotated.rotate(angle);
        
        g2dRotated.setColor(Color.BLACK);
        int[] xPoints = {8, -4, -4};
        int[] yPoints = {0, -4, 4};
        g2dRotated.fillPolygon(xPoints, yPoints, 3);
        
        g2dRotated.dispose();
    }
    
    /**
     * 绘制终点跑道
     * 从外圈跑道分出的专属终点跑道
     */
    private void drawFinishTracks(Graphics2D g2d) {
        Color[] trackColors = {
            new Color(220, 50, 50),   // 红色
            new Color(255, 215, 0),   // 黄色
            new Color(50, 205, 50),   // 绿色
            new Color(30, 144, 255)   // 蓝色
        };
        
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        int trackLength = 120;
        int cellSize = 18;
        
        // 绘制四条终点跑道
        for (int player = 0; player < 4; player++) {
            g2d.setColor(trackColors[player]);
            
            for (int i = 0; i < 6; i++) { // 6个格子的终点跑道
                Point pos = getFinishTrackPosition(player, i, centerX, centerY, trackLength);
                
                g2d.fillOval(pos.x - cellSize/2, pos.y - cellSize/2, cellSize, cellSize);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(pos.x - cellSize/2, pos.y - cellSize/2, cellSize, cellSize);
                g2d.setColor(trackColors[player]);
            }
        }
    }
    
    /**
     * 获取终点跑道格子位置
     */
    private Point getFinishTrackPosition(int player, int cellIndex, int centerX, int centerY, int trackLength) {
        int distance = 80 - cellIndex * 12; // 从外向内
        
        switch (player) {
            case 0: // 红色 - 从左向中心
                return new Point(centerX - distance, centerY);
            case 1: // 黄色 - 从上向中心
                return new Point(centerX, centerY - distance);
            case 2: // 绿色 - 从右向中心
                return new Point(centerX + distance, centerY);
            case 3: // 蓝色 - 从下向中心
                return new Point(centerX, centerY + distance);
            default:
                return new Point(centerX, centerY);
        }
    }
    
    /**
     * 绘制中央彩色终点区
     * 四个彩色三角形拼成的正方形花朵
     */
    private void drawCentralTarget(Graphics2D g2d, int centerX, int centerY) {
        Color[] targetColors = {
            new Color(220, 50, 50),   // 红色
            new Color(255, 215, 0),   // 黄色
            new Color(50, 205, 50),   // 绿色
            new Color(30, 144, 255)   // 蓝色
        };
        
        int size = 50;
        
        // 绘制四个三角形组成花朵
        for (int i = 0; i < 4; i++) {
            g2d.setColor(targetColors[i]);
            
            int[] xPoints, yPoints;
            switch (i) {
                case 0: // 左三角
                    xPoints = new int[]{centerX - size, centerX, centerX};
                    yPoints = new int[]{centerY, centerY - size, centerY + size};
                    break;
                case 1: // 上三角
                    xPoints = new int[]{centerX, centerX - size, centerX + size};
                    yPoints = new int[]{centerY - size, centerY, centerY};
                    break;
                case 2: // 右三角
                    xPoints = new int[]{centerX + size, centerX, centerX};
                    yPoints = new int[]{centerY, centerY - size, centerY + size};
                    break;
                case 3: // 下三角
                    xPoints = new int[]{centerX, centerX - size, centerX + size};
                    yPoints = new int[]{centerY + size, centerY, centerY};
                    break;
                default:
                    continue;
            }
            
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawPolygon(xPoints, yPoints, 3);
        }
    }
    
    /**
     * 绘制外边框记录区
     * 四条边的长方形小旗记录进度
     */
    private void drawProgressBorders(Graphics2D g2d) {
        Color[] borderColors = {
            new Color(220, 50, 50),   // 红色
            new Color(255, 215, 0),   // 黄色
            new Color(50, 205, 50),   // 绿色
            new Color(30, 144, 255)   // 蓝色
        };
        
        int flagWidth = 30;
        int flagHeight = 20;
        int margin = 20;
        
        // 绘制四边的进度小旗
        for (int side = 0; side < 4; side++) {
            g2d.setColor(borderColors[side]);
            
            for (int i = 0; i < 10; i++) {
                int x, y;
                switch (side) {
                    case 0: // 上边
                        x = margin + 60 + i * 65;
                        y = margin;
                        g2d.fillRect(x, y, flagWidth, flagHeight);
                        break;
                    case 1: // 右边
                        x = BOARD_SIZE - margin - flagHeight;
                        y = margin + 60 + i * 65;
                        g2d.fillRect(x, y, flagHeight, flagWidth);
                        break;
                    case 2: // 下边
                        x = BOARD_SIZE - margin - 60 - i * 65;
                        y = BOARD_SIZE - margin - flagHeight;
                        g2d.fillRect(x, y, flagWidth, flagHeight);
                        break;
                    case 3: // 左边
                        x = margin;
                        y = BOARD_SIZE - margin - 60 - i * 65;
                        g2d.fillRect(x, y, flagHeight, flagWidth);
                        break;
                }
                
                // 绘制小旗边框
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                switch (side) {
                    case 0:
                        g2d.drawRect(margin + 60 + i * 65, margin, flagWidth, flagHeight);
                        break;
                    case 1:
                        g2d.drawRect(BOARD_SIZE - margin - flagHeight, margin + 60 + i * 65, flagHeight, flagWidth);
                        break;
                    case 2:
                        g2d.drawRect(BOARD_SIZE - margin - 60 - i * 65, BOARD_SIZE - margin - flagHeight, flagWidth, flagHeight);
                        break;
                    case 3:
                        g2d.drawRect(margin, BOARD_SIZE - margin - 60 - i * 65, flagHeight, flagWidth);
                        break;
                }
                g2d.setColor(borderColors[side]);
            }
        }
    }
    
    private void drawPlanes(Graphics2D g2d) {
        Color[] playerColors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
        
        for (int player = 0; player < 4; player++) {
            FlightChessGame.Plane[] planes = game.getPlayerPlanes(player);
            g2d.setColor(playerColors[player]);
            
            for (int i = 0; i < planes.length; i++) {
                Point pos = getPlaneScreenPosition(player, i);
                
                // 高亮可移动的飞机
                if (waitingForMove && player == game.getCurrentPlayer() && 
                    movablePlanes != null && movablePlanes.contains(i)) {
                    g2d.setStroke(new BasicStroke(3));
                    g2d.setColor(Color.ORANGE);
                    g2d.drawOval(pos.x - PLANE_SIZE/2 - 2, pos.y - PLANE_SIZE/2 - 2, 
                                PLANE_SIZE + 4, PLANE_SIZE + 4);
                    g2d.setColor(playerColors[player]);
                }
                
                // 绘制飞机
                g2d.fillOval(pos.x - PLANE_SIZE/2, pos.y - PLANE_SIZE/2, PLANE_SIZE, PLANE_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(pos.x - PLANE_SIZE/2, pos.y - PLANE_SIZE/2, PLANE_SIZE, PLANE_SIZE);
                
                // 绘制飞机编号
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                String text = String.valueOf(i + 1);
                int textX = pos.x - fm.stringWidth(text) / 2;
                int textY = pos.y + fm.getAscent() / 2;
                g2d.drawString(text, textX, textY);
            }
        }
    }
}