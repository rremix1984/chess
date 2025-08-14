package com.example.flightchess;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 飞行棋游戏核心逻辑类
 * 支持多种对战模式：玩家vs玩家、玩家vsAI、AIvsAI
 */
public class FlightChessGame {
    // 游戏常量
    public static final int BOARD_SIZE = 15;
    public static final int HOME_CELLS = 6;
    public static final int FINISH_CELLS = 6;
    public static final int PLANES_PER_PLAYER = 4;
    public static final int MAIN_TRACK_SIZE = 52;
    
    // 玩家颜色
    public static final java.awt.Color[] PLAYER_COLORS = {
        new Color(220, 20, 60),   // 深红色
        new Color(30, 144, 255),  // 道奇蓝
        new Color(255, 215, 0),   // 金黄色
        new Color(50, 205, 50)    // 亮绿色
    };
    
    // 玩家名称
    public static final String[] PLAYER_NAMES = {
        "红色", "蓝色", "黄色", "绿色"
    };
    
    // 游戏状态
    private int currentPlayer;
    private int playerCount;
    private boolean[] aiPlayers;
    private int[] aiDifficulty;
    private Plane[][] planes;
    private Random random;
    private boolean gameOver;
    private int winner;
    private List<GameMove> moveHistory;
    private int consecutiveSixes;
    private boolean canRollAgain;
    private GameMode gameMode;
    
    public FlightChessGame() {
        this(4);
    }
    
    public FlightChessGame(int playerCount) {
        this.playerCount = playerCount;
        this.currentPlayer = 0;
        this.aiPlayers = new boolean[4];
        this.planes = new Plane[4][PLANES_PER_PLAYER];
        this.random = new Random();
        this.gameOver = false;
        this.winner = -1;
        this.moveHistory = new ArrayList<>();
        
        initializePlanes();
    }
    
    /**
     * 初始化所有飞机
     */
    private void initializePlanes() {
        for (int player = 0; player < 4; player++) {
            for (int i = 0; i < PLANES_PER_PLAYER; i++) {
                planes[player][i] = new Plane();
            }
        }
    }
    
    /**
     * 设置AI玩家
     */
    public void setAIPlayer(int player, boolean isAI) {
        if (player >= 0 && player < 4) {
            aiPlayers[player] = isAI;
        }
    }
    
    /**
     * 投掷骰子
     */
    public int rollDice() {
        return random.nextInt(6) + 1;
    }
    
    /**
     * 获取可移动的飞机列表
     */
    public List<Integer> getMovablePlanes(int player, int diceValue) {
        List<Integer> movablePlanes = new ArrayList<>();
        
        for (int i = 0; i < PLANES_PER_PLAYER; i++) {
            if (canMovePlane(player, i, diceValue)) {
                movablePlanes.add(i);
            }
        }
        
        return movablePlanes;
    }
    
    /**
     * 检查飞机是否可以移动
     */
    public boolean canMovePlane(int player, int planeIndex, int diceValue) {
        Plane plane = planes[player][planeIndex];
        
        // 在家园的飞机只有投掷6点才能起飞
        if (plane.position == -1) {
            return diceValue == 6;
        }
        
        // 已在跑道上的飞机
        if (plane.position >= 0) {
            int newPosition = plane.position + diceValue;
            
            // 检查是否超出终点跑道
            if (newPosition >= MAIN_TRACK_SIZE + FINISH_CELLS) {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 移动飞机
     */
    public boolean movePlane(int player, int planeIndex, int diceValue) {
        if (!canMovePlane(player, planeIndex, diceValue)) {
            return false;
        }
        
        Plane plane = planes[player][planeIndex];
        int oldPosition = plane.position;
        List<Plane> capturedPlanes = new ArrayList<>();
        
        if (plane.position == -1) {
            // 起飞
            plane.position = 0;
        } else {
            // 在主跑道或终点跑道移动
            int newPosition = plane.position + diceValue;
            
            if (newPosition >= MAIN_TRACK_SIZE) {
                // 进入终点跑道
                if (shouldEnterFinishTrack(player, plane.position)) {
                    plane.position = newPosition;
                } else {
                    // 继续在主跑道移动
                    plane.position = newPosition % MAIN_TRACK_SIZE;
                }
            } else {
                plane.position = newPosition;
            }
            
            // 检查并击落其他飞机
            if (plane.position < MAIN_TRACK_SIZE) {
                capturedPlanes = checkAndCaptureOtherPlanes(player, plane.position);
            }
        }
        
        // 记录移动
        GameMove move = new GameMove(player, planeIndex, oldPosition, plane.position, diceValue, capturedPlanes);
        moveHistory.add(move);
        
        return true;
    }
    
    /**
     * 检查并击落其他飞机
     */
    private List<Plane> checkAndCaptureOtherPlanes(int currentPlayer, int position) {
        List<Plane> capturedPlanes = new ArrayList<>();
        
        if (isSafePosition(position)) {
            return capturedPlanes;
        }
        
        for (int player = 0; player < playerCount; player++) {
            if (player != currentPlayer) {
                for (Plane plane : planes[player]) {
                    if (plane.position == position) {
                        plane.position = -1; // 返回家园
                        capturedPlanes.add(plane);
                    }
                }
            }
        }
        
        return capturedPlanes;
    }
    
    /**
     * 检查是否应该进入终点跑道
     */
    private boolean shouldEnterFinishTrack(int player, int currentPosition) {
        int finishTrackEntrance = getDistanceToFinishTrackEntrance(player, currentPosition);
        return finishTrackEntrance <= 6;
    }
    
    /**
     * 获取到终点跑道入口的距离
     */
    private int getDistanceToFinishTrackEntrance(int player, int currentPosition) {
        int finishTrackStart = player * 13; // 每个玩家的终点跑道起始位置
        
        if (currentPosition <= finishTrackStart) {
            return finishTrackStart - currentPosition;
        } else {
            return MAIN_TRACK_SIZE - currentPosition + finishTrackStart;
        }
    }
    
    /**
     * 检查是否是安全位置
     */
    public boolean isSafePosition(int position) {
        // 简化的安全位置：每隔13个位置有一个安全点
        return position % 13 == 0;
    }
    
    /**
     * 切换到下一个玩家
     */
    public void nextPlayer() {
        currentPlayer = (currentPlayer + 1) % playerCount;
    }
    
    /**
     * 检查是否有玩家获胜
     */
    public boolean checkWin() {
        for (int player = 0; player < playerCount; player++) {
            boolean allPlanesFinished = true;
            for (Plane plane : planes[player]) {
                if (plane.position < MAIN_TRACK_SIZE) {
                    allPlanesFinished = false;
                    break;
                }
            }
            
            if (allPlanesFinished) {
                gameOver = true;
                winner = player;
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 重新开始游戏
     */
    public void restart() {
        currentPlayer = 0;
        gameOver = false;
        winner = -1;
        moveHistory.clear();
        initializePlanes();
    }
    
    // Getter方法
    public int getCurrentPlayer() { return currentPlayer; }
    public int getPlayerCount() { return playerCount; }
    public boolean isAIPlayer(int player) { return aiPlayers[player]; }
    public Plane[] getPlayerPlanes(int player) { return planes[player]; }
    public boolean isGameOver() { return gameOver; }
    public int getWinner() { return winner; }
    public List<GameMove> getMoveHistory() { return moveHistory; }
    
    /**
     * 获取玩家颜色名称
     */
    public String getPlayerColorName(int player) {
        String[] names = {"红色", "蓝色", "黄色", "绿色"};
        return names[player];
    }
    
    /**
     * 获取玩家颜色
     */
    public java.awt.Color getPlayerColor(int player) {
        return PLAYER_COLORS[player];
    }
    
    /**
     * 飞机类
     */
    public static class Plane {
        public int position; // -1表示在家园，0-51表示主跑道，52+表示终点跑道
        
        public Plane() {
            this.position = -1;
        }
    }
    
    /**
     * 游戏移动记录类
     */
    public static class GameMove {
        public int player;
        public int planeIndex;
        public int fromPosition;
        public int toPosition;
        public int diceValue;
        public List<Plane> capturedPlanes;
        public long timestamp;
        
        public GameMove(int player, int planeIndex, int fromPosition, int toPosition, 
                       int diceValue, List<Plane> capturedPlanes) {
            this.player = player;
            this.planeIndex = planeIndex;
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
            this.diceValue = diceValue;
            this.capturedPlanes = capturedPlanes;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * 游戏模式枚举
     */
    public enum GameMode {
        PLAYER_VS_PLAYER,    // 玩家对玩家
        PLAYER_VS_AI,        // 玩家对AI
        AI_VS_AI            // AI对AI
    }
    
    /**
     * 设置游戏模式
     */
    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }
    
    /**
     * 获取游戏模式
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * 设置AI玩家和难度
     */
    public void setAIPlayer(int player, boolean isAI, int difficulty) {
        if (player >= 0 && player < 4) {
            aiPlayers[player] = isAI;
            if (isAI) {
                if (aiDifficulty == null) {
                    aiDifficulty = new int[4];
                }
                aiDifficulty[player] = difficulty;
            }
        }
    }
    
    /**
     * 获取AI难度
     */
    public int getAIDifficulty(int player) {
        return aiDifficulty != null ? aiDifficulty[player] : 1;
    }
    
    /**
     * 检查是否可以再次投掷骰子
     */
    public boolean canRollAgain() {
        return canRollAgain;
    }
    
    /**
     * 设置是否可以再次投掷
     */
    public void setCanRollAgain(boolean canRoll) {
        this.canRollAgain = canRoll;
    }
    
    /**
     * 获取连续投掷6的次数
     */
    public int getConsecutiveSixes() {
        return consecutiveSixes;
    }
    
    /**
     * 增强的投掷骰子方法
     */
    public int rollDiceEnhanced() {
        int value = rollDice();
        
        if (value == 6) {
            consecutiveSixes++;
            canRollAgain = true;
            
            // 如果连续投掷3次6，则跳过此回合
            if (consecutiveSixes >= 3) {
                consecutiveSixes = 0;
                canRollAgain = false;
                nextPlayer();
            }
        } else {
            consecutiveSixes = 0;
            canRollAgain = false;
        }
        
        return value;
    }
    
    /**
     * 获取玩家在跑道上的飞机数量
     */
    public int getPlanesOnTrack(int player) {
        int count = 0;
        for (Plane plane : planes[player]) {
            if (plane.position >= 0 && plane.position < MAIN_TRACK_SIZE) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取玩家在家园的飞机数量
     */
    public int getPlanesAtHome(int player) {
        int count = 0;
        for (Plane plane : planes[player]) {
            if (plane.position == -1) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取玩家到达终点的飞机数量
     */
    public int getPlanesFinished(int player) {
        int count = 0;
        for (Plane plane : planes[player]) {
            if (plane.position >= MAIN_TRACK_SIZE) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取游戏统计信息
     */
    public String getGameStats(int player) {
        return String.format(
            "玩家 %s - 家园: %d, 跑道: %d, 终点: %d",
            PLAYER_NAMES[player],
            getPlanesAtHome(player),
            getPlanesOnTrack(player),
            getPlanesFinished(player)
        );
    }
}
