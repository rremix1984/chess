package com.example.flightchess;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 飞行棋游戏核心逻辑类
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
        java.awt.Color.RED, java.awt.Color.BLUE, java.awt.Color.YELLOW, java.awt.Color.GREEN
    };
    
    // 游戏状态
    private int currentPlayer;
    private int playerCount;
    private boolean[] aiPlayers;
    private Plane[][] planes;
    private Random random;
    private boolean gameOver;
    private int winner;
    private List<GameMove> moveHistory;
    
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
        
        public GameMove(int player, int planeIndex, int fromPosition, int toPosition, 
                       int diceValue, List<Plane> capturedPlanes) {
            this.player = player;
            this.planeIndex = planeIndex;
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
            this.diceValue = diceValue;
            this.capturedPlanes = capturedPlanes;
        }
    }
    

}