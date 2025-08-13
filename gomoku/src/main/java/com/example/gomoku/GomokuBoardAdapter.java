package com.example.gomoku;

import com.example.gomoku.core.*;

/**
 * 五子棋棋盘适配器
 * 将GomokuBoard适配为Board接口，使ChatPanel能够处理五子棋棋盘
 */
public class GomokuBoardAdapter implements Board {
    
    private com.example.gomoku.core.GomokuBoard gomokuBoard;
    
    public GomokuBoardAdapter(com.example.gomoku.core.GomokuBoard gomokuBoard) {
        this.gomokuBoard = gomokuBoard;
    }
    
    @Override
    public Piece getPieceAt(Position position) {
        char piece = gomokuBoard.getPiece(position.getX(), position.getY());
        if (piece == com.example.gomoku.core.GomokuBoard.BLACK) {
            return new Piece(PieceColor.BLACK, position);
        } else if (piece == com.example.gomoku.core.GomokuBoard.WHITE) {
            return new Piece(PieceColor.WHITE, position);
        }
        return null;
    }
    
    @Override
    public boolean isEmpty(Position position) {
        char piece = gomokuBoard.getPiece(position.getX(), position.getY());
        return piece == ' ';
    }
    
    @Override
    public boolean placePiece(Position position, Piece piece) {
        // 五子棋通过placePiece方法放置棋子
        return gomokuBoard.placePiece(position.getX(), position.getY());
    }
    
    @Override
    public void removePiece(Position position) {
        // 五子棋支持移除棋子（用于悔棋）
        gomokuBoard.removePiece(position.getX(), position.getY());
    }
    
    @Override
    public int getSize() {
        return com.example.gomoku.core.GomokuBoard.BOARD_SIZE;
    }
    
    @Override
    public boolean isValidPosition(Position position) {
        int x = position.getX();
        int y = position.getY();
        return x >= 0 && x < com.example.gomoku.core.GomokuBoard.BOARD_SIZE && 
               y >= 0 && y < com.example.gomoku.core.GomokuBoard.BOARD_SIZE;
    }
    
    /**
     * 获取原始的五子棋棋盘对象
     */
    public com.example.gomoku.core.GomokuBoard getGomokuBoard() {
        return gomokuBoard;
    }
}