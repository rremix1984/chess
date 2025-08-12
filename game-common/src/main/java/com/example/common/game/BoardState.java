package com.example.common.game;

import java.io.Serializable;

/**
 * 棋盘状态类，用于保存游戏历史记录
 */
public class BoardState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Object[][] pieces;
    private final Object currentPlayer;
    private final int moveNumber;
    
    /**
     * 构造函数
     * @param pieces 棋盘上的棋子状态
     * @param currentPlayer 当前玩家
     * @param moveNumber 移动编号
     */
    public BoardState(Object[][] pieces, Object currentPlayer, int moveNumber) {
        // 深拷贝棋盘状态
        this.pieces = new Object[pieces.length][];
        for (int i = 0; i < pieces.length; i++) {
            this.pieces[i] = new Object[pieces[i].length];
            System.arraycopy(pieces[i], 0, this.pieces[i], 0, pieces[i].length);
        }
        this.currentPlayer = currentPlayer;
        this.moveNumber = moveNumber;
    }
    
    /**
     * 获取棋盘状态
     * @return 棋盘状态
     */
    public Object[][] getPieces() {
        // 返回深拷贝，防止外部修改
        Object[][] copy = new Object[pieces.length][];
        for (int i = 0; i < pieces.length; i++) {
            copy[i] = new Object[pieces[i].length];
            System.arraycopy(pieces[i], 0, copy[i], 0, pieces[i].length);
        }
        return copy;
    }
    
    /**
     * 获取棋盘状态（别名方法，兼容性）
     * @return 棋盘状态
     */
    public Object[][] getBoard() {
        return getPieces();
    }
    
    /**
     * 获取当前玩家
     * @return 当前玩家
     */
    public Object getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * 获取移动编号
     * @return 移动编号
     */
    public int getMoveNumber() {
        return moveNumber;
    }
    
    @Override
    public String toString() {
        return "BoardState{" +
                "moveNumber=" + moveNumber +
                ", currentPlayer=" + currentPlayer +
                '}';
    }
}