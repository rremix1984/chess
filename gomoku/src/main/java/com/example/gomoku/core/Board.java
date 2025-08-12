package com.example.gomoku.core;

/**
 * 棋盘接口
 */
public interface Board {
    /**
     * 获取指定位置的棋子
     */
    Piece getPieceAt(Position position);
    
    /**
     * 检查指定位置是否为空
     */
    boolean isEmpty(Position position);
    
    /**
     * 在指定位置放置棋子
     */
    boolean placePiece(Position position, Piece piece);
    
    /**
     * 移除指定位置的棋子
     */
    void removePiece(Position position);
    
    /**
     * 获取棋盘大小
     */
    int getSize();
    
    /**
     * 检查位置是否在棋盘范围内
     */
    boolean isValidPosition(Position position);
}
