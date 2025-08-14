package com.example.chinesechess.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 棋盘状态类 - 用于悔棋功能
 * 完整记录某个时刻的棋盘状态，包括所有棋子位置、当前玩家、移动历史等
 */
public class BoardState implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1L;
    
    // 棋盘状态 - 10x9的棋盘
    private final Piece[][] pieces;
    
    // 当前轮到的玩家
    private final PieceColor currentPlayer;
    
    // 游戏状态
    private final GameState gameState;
    
    // 上一步移动的起始位置（用于移动标记显示）
    private final Position lastMoveStart;
    
    // 上一步移动的结束位置（用于移动标记显示）
    private final Position lastMoveEnd;
    
    // 局面历史记录（用于检测重复局面）
    private final List<String> positionHistory;
    
    // 状态创建时间戳（用于调试）
    private final long timestamp;
    
    // 状态序号（用于调试）
    private final int stateIndex;
    
    /**
     * 构造函数 - 创建棋盘状态快照
     * @param pieces 当前棋盘上的所有棋子
     * @param currentPlayer 当前轮到的玩家
     * @param gameState 当前游戏状态
     * @param lastMoveStart 上一步移动的起始位置
     * @param lastMoveEnd 上一步移动的结束位置
     * @param positionHistory 局面历史记录
     * @param stateIndex 状态序号
     */
    public BoardState(Piece[][] pieces, PieceColor currentPlayer, GameState gameState,
                     Position lastMoveStart, Position lastMoveEnd, 
                     List<String> positionHistory, int stateIndex) {
        
        this.currentPlayer = currentPlayer;
        this.gameState = gameState;
        this.lastMoveStart = lastMoveStart != null ? new Position(lastMoveStart.getX(), lastMoveStart.getY()) : null;
        this.lastMoveEnd = lastMoveEnd != null ? new Position(lastMoveEnd.getX(), lastMoveEnd.getY()) : null;
        this.positionHistory = new ArrayList<>(positionHistory != null ? positionHistory : new ArrayList<>());
        this.stateIndex = stateIndex;
        this.timestamp = System.currentTimeMillis();
        
        // 深度复制棋盘状态
        this.pieces = new Piece[10][9];
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                if (pieces[row][col] != null) {
                    this.pieces[row][col] = pieces[row][col].clone();
                } else {
                    this.pieces[row][col] = null;
                }
            }
        }
    }
    
    /**
     * 简化构造函数 - 只传入核心参数
     */
    public BoardState(Piece[][] pieces, PieceColor currentPlayer, int stateIndex) {
        this(pieces, currentPlayer, GameState.PLAYING, null, null, null, stateIndex);
    }
    
    /**
     * 获取棋盘状态的深度拷贝
     * @return 棋盘状态的拷贝
     */
    public Piece[][] getPiecesCopy() {
        Piece[][] copy = new Piece[10][9];
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                if (pieces[row][col] != null) {
                    copy[row][col] = pieces[row][col].clone();
                } else {
                    copy[row][col] = null;
                }
            }
        }
        return copy;
    }
    
    /**
     * 获取当前玩家
     */
    public PieceColor getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * 获取游戏状态
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * 获取上一步移动的起始位置
     */
    public Position getLastMoveStart() {
        return lastMoveStart != null ? new Position(lastMoveStart.getX(), lastMoveStart.getY()) : null;
    }
    
    /**
     * 获取上一步移动的结束位置
     */
    public Position getLastMoveEnd() {
        return lastMoveEnd != null ? new Position(lastMoveEnd.getX(), lastMoveEnd.getY()) : null;
    }
    
    /**
     * 获取局面历史记录的拷贝
     */
    public List<String> getPositionHistoryCopy() {
        return new ArrayList<>(positionHistory);
    }
    
    /**
     * 获取状态序号
     */
    public int getStateIndex() {
        return stateIndex;
    }
    
    /**
     * 获取时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 验证棋盘状态的完整性
     * @return true如果状态有效，false否则
     */
    public boolean isValid() {
        try {
            // 检查棋盘数组是否正确
            if (pieces == null || pieces.length != 10) {
                return false;
            }
            for (int row = 0; row < 10; row++) {
                if (pieces[row] == null || pieces[row].length != 9) {
                    return false;
                }
            }
            
            // 检查是否有将军存在
            boolean hasRedGeneral = false;
            boolean hasBlackGeneral = false;
            
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 9; col++) {
                    Piece piece = pieces[row][col];
                    if (piece instanceof General) {
                        if (piece.getColor() == PieceColor.RED) {
                            hasRedGeneral = true;
                        } else {
                            hasBlackGeneral = true;
                        }
                    }
                }
            }
            
            return hasRedGeneral && hasBlackGeneral;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 生成状态摘要信息（用于调试）
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("BoardState#").append(stateIndex);
        summary.append(" [").append(currentPlayer == PieceColor.RED ? "红方" : "黑方").append("回合]");
        
        // 统计棋子数量
        int redCount = 0, blackCount = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                if (pieces[row][col] != null) {
                    if (pieces[row][col].getColor() == PieceColor.RED) {
                        redCount++;
                    } else {
                        blackCount++;
                    }
                }
            }
        }
        
        summary.append(" 棋子数: 红").append(redCount).append(" 黑").append(blackCount);
        
        if (lastMoveStart != null && lastMoveEnd != null) {
            summary.append(" 上步: (").append(lastMoveStart.getX()).append(",").append(lastMoveStart.getY())
                   .append(")→(").append(lastMoveEnd.getX()).append(",").append(lastMoveEnd.getY()).append(")");
        }
        
        return summary.toString();
    }
    
    @Override
    public BoardState clone() {
        return new BoardState(pieces, currentPlayer, gameState, lastMoveStart, lastMoveEnd, positionHistory, stateIndex);
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}
