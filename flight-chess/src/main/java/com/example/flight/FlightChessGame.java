package com.example.flight;

import java.util.*;

/**
 * 飞行棋游戏核心类
 */
public class FlightChessGame {
    public static final int BOARD_SIZE = 52; // 主跑道格子数
    public static final int HOME_SIZE = 6;   // 家园格子数
    public static final int FINAL_SIZE = 6;  // 终点跑道格子数
    public static final int PLANES_PER_PLAYER = 4; // 每个玩家的飞机数
    
    // 玩家颜色
    public static final int RED = 0;
    public static final int YELLOW = 1;
    public static final int BLUE = 2;
    public static final int GREEN = 3;
    
    // 飞机状态
    public static final int IN_HANGAR = 0;    // 在机库
    public static final int ON_BOARD = 1;     // 在跑道上
    public static final int IN_FINAL = 2;     // 在终点跑道
    public static final int FINISHED = 3;     // 已到达终点
    
    private int currentPlayer;
    private int playerCount;
    private boolean[] isAI;
    private Plane[][] planes; // [玩家][飞机编号]
    private Random random;
    private boolean gameEnded;
    private int winner;
    private List<GameMove> moveHistory;
    
    // 特殊位置
    private static final int[] SAFE_POSITIONS = {9, 22, 35, 48}; // 安全点
    private static final int[] START_POSITIONS = {0, 13, 26, 39}; // 起飞点
    private static final int[] FINAL_ENTRANCES = {51, 12, 25, 38}; // 终点跑道入口
    
    public FlightChessGame(int playerCount) {
        this.playerCount = Math.max(2, Math.min(4, playerCount));
        this.currentPlayer = RED;
        this.isAI = new boolean[4];
        this.planes = new Plane[4][PLANES_PER_PLAYER];
        this.random = new Random();
        this.gameEnded = false;
        this.winner = -1;
        this.moveHistory = new ArrayList<>();
        
        initializePlanes();
    }
    
    /**
     * 初始化所有飞机
     */
    private void initializePlanes() {
        for (int player = 0; player < 4; player++) {
            for (int plane = 0; plane < PLANES_PER_PLAYER; plane++) {
                planes[player][plane] = new Plane(player, plane);
            }
        }
    }
    
    /**
     * 设置AI玩家
     */
    public void setAIPlayer(int player, boolean ai) {
        if (player >= 0 && player < 4) {
            isAI[player] = ai;
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
    public List<Plane> getMovablePlanes(int diceValue) {
        List<Plane> movablePlanes = new ArrayList<>();
        
        for (Plane plane : planes[currentPlayer]) {
            if (canMovePlane(plane, diceValue)) {
                movablePlanes.add(plane);
            }
        }
        
        return movablePlanes;
    }
    
    /**
     * 检查飞机是否可以移动
     */
    private boolean canMovePlane(Plane plane, int diceValue) {
        switch (plane.status) {
            case IN_HANGAR:
                return diceValue == 6; // 只有投到6才能起飞
                
            case ON_BOARD:
                int newPosition = (plane.position + diceValue) % BOARD_SIZE;
                // 检查是否应该进入终点跑道
                if (shouldEnterFinal(plane, diceValue)) {
                    int finalPosition = diceValue - getDistanceToFinalEntrance(plane);
                    return finalPosition <= FINAL_SIZE;
                }
                return true;
                
            case IN_FINAL:
                return plane.position + diceValue <= FINAL_SIZE;
                
            case FINISHED:
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * 移动飞机
     */
    public boolean movePlane(Plane plane, int diceValue) {
        if (!canMovePlane(plane, diceValue)) {
            return false;
        }
        
        GameMove move = new GameMove(currentPlayer, plane.id, plane.position, 
                                   plane.status, diceValue);
        
        switch (plane.status) {
            case IN_HANGAR:
                takeOff(plane);
                break;
                
            case ON_BOARD:
                if (shouldEnterFinal(plane, diceValue)) {
                    enterFinalTrack(plane, diceValue);
                } else {
                    moveOnBoard(plane, diceValue);
                }
                break;
                
            case IN_FINAL:
                moveInFinal(plane, diceValue);
                break;
        }
        
        move.newPosition = plane.position;
        move.newStatus = plane.status;
        moveHistory.add(move);
        
        // 检查是否获胜
        checkWin();
        
        return true;
    }
    
    /**
     * 起飞
     */
    private void takeOff(Plane plane) {
        plane.status = ON_BOARD;
        plane.position = START_POSITIONS[plane.player];
        
        // 检查起飞点是否有其他飞机，如果有则击落
        checkAndCrash(plane.position, plane.player);
    }
    
    /**
     * 在主跑道上移动
     */
    private void moveOnBoard(Plane plane, int diceValue) {
        plane.position = (plane.position + diceValue) % BOARD_SIZE;
        
        // 检查是否撞到其他飞机
        if (!isSafePosition(plane.position)) {
            checkAndCrash(plane.position, plane.player);
        }
    }
    
    /**
     * 进入终点跑道
     */
    private void enterFinalTrack(Plane plane, int diceValue) {
        plane.status = IN_FINAL;
        plane.position = diceValue - getDistanceToFinalEntrance(plane);
    }
    
    /**
     * 在终点跑道移动
     */
    private void moveInFinal(Plane plane, int diceValue) {
        plane.position += diceValue;
        if (plane.position >= FINAL_SIZE) {
            plane.status = FINISHED;
            plane.position = FINAL_SIZE;
        }
    }
    
    /**
     * 检查并击落其他飞机
     */
    private void checkAndCrash(int position, int currentPlayerColor) {
        for (int player = 0; player < 4; player++) {
            if (player == currentPlayerColor) continue;
            
            for (Plane plane : planes[player]) {
                if (plane.status == ON_BOARD && plane.position == position) {
                    // 击落飞机
                    plane.status = IN_HANGAR;
                    plane.position = -1;
                }
            }
        }
    }
    
    /**
     * 检查是否应该进入终点跑道
     */
    private boolean shouldEnterFinal(Plane plane, int diceValue) {
        int distanceToEntrance = getDistanceToFinalEntrance(plane);
        return diceValue >= distanceToEntrance;
    }
    
    /**
     * 获取到终点跑道入口的距离
     */
    private int getDistanceToFinalEntrance(Plane plane) {
        int entrance = FINAL_ENTRANCES[plane.player];
        if (plane.position <= entrance) {
            return entrance - plane.position;
        } else {
            return BOARD_SIZE - plane.position + entrance;
        }
    }
    
    /**
     * 检查是否是安全位置
     */
    private boolean isSafePosition(int position) {
        for (int safePos : SAFE_POSITIONS) {
            if (position == safePos) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 下一个玩家
     */
    public void nextPlayer() {
        do {
            currentPlayer = (currentPlayer + 1) % 4;
        } while (currentPlayer >= playerCount);
    }
    
    /**
     * 检查是否获胜
     */
    private void checkWin() {
        for (int player = 0; player < playerCount; player++) {
            int finishedCount = 0;
            for (Plane plane : planes[player]) {
                if (plane.status == FINISHED) {
                    finishedCount++;
                }
            }
            
            if (finishedCount == PLANES_PER_PLAYER) {
                gameEnded = true;
                winner = player;
                break;
            }
        }
    }
    
    /**
     * 重新开始游戏
     */
    public void restart() {
        currentPlayer = RED;
        gameEnded = false;
        winner = -1;
        moveHistory.clear();
        initializePlanes();
    }
    
    /**
     * 获取玩家颜色名称
     */
    public static String getPlayerColorName(int player) {
        switch (player) {
            case RED: return "红色";
            case YELLOW: return "黄色";
            case BLUE: return "蓝色";
            case GREEN: return "绿色";
            default: return "未知";
        }
    }
    
    /**
     * 获取玩家颜色
     */
    public static Color getPlayerColor(int player) {
        switch (player) {
            case RED: return new Color(255, 82, 82);
            case YELLOW: return new Color(255, 235, 59);
            case BLUE: return new Color(66, 165, 245);
            case GREEN: return new Color(102, 187, 106);
            default: return Color.GRAY;
        }
    }
    
    // Getter方法
    public int getCurrentPlayer() { return currentPlayer; }
    public int getPlayerCount() { return playerCount; }
    public boolean[] getIsAI() { return isAI; }
    public Plane[][] getPlanes() { return planes; }
    public boolean isGameEnded() { return gameEnded; }
    public int getWinner() { return winner; }
    public List<GameMove> getMoveHistory() { return moveHistory; }
    
    /**
     * 飞机类
     */
    public static class Plane {
        public int player;
        public int id;
        public int position;
        public int status;
        
        public Plane(int player, int id) {
            this.player = player;
            this.id = id;
            this.position = -1; // -1表示在机库
            this.status = IN_HANGAR;
        }
        
        public Plane(Plane other) {
            this.player = other.player;
            this.id = other.id;
            this.position = other.position;
            this.status = other.status;
        }
        
        @Override
        public String toString() {
            return String.format("飞机[%s-%d]", 
                               getPlayerColorName(player), id);
        }
    }
    
    /**
     * 游戏移动记录类
     */
    public static class GameMove {
        public int player;
        public int planeId;
        public int oldPosition;
        public int oldStatus;
        public int newPosition;
        public int newStatus;
        public int diceValue;
        
        public GameMove(int player, int planeId, int oldPosition, 
                       int oldStatus, int diceValue) {
            this.player = player;
            this.planeId = planeId;
            this.oldPosition = oldPosition;
            this.oldStatus = oldStatus;
            this.diceValue = diceValue;
        }
    }
    
    // 内部Color类（简化版）
    public static class Color {
        public int r, g, b;
        
        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
        
        public static final Color GRAY = new Color(128, 128, 128);
    }
}