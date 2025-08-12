package com.example.gomoku.ui;

import com.example.gomoku.core.*;

/**
 * 五子棋棋盘适配器
 * 将GomokuBoard适配为Board接口，使ChatPanel能够处理五子棋棋盘
 */
public class GomokuBoardAdapter extends Board {
    
    private com.example.gomoku.core.GomokuBoard gomokuBoard;
    
    public GomokuBoardAdapter(com.example.gomoku.core.GomokuBoard gomokuBoard) {
        this.gomokuBoard = gomokuBoard;
    }
    
    @Override
    public Piece getPiece(int row, int col) {
        // 将五子棋的棋子转换为象棋的Piece对象
        char piece = gomokuBoard.getPiece(row, col);
        if (piece == com.example.gomoku.core.GomokuBoard.BLACK) {
            // 创建一个虚拟的黑色棋子
            return new VirtualPiece(PieceColor.BLACK, "黑");
        } else if (piece == com.example.gomoku.core.GomokuBoard.WHITE) {
            // 创建一个虚拟的白色棋子
            return new VirtualPiece(PieceColor.RED, "白"); // 使用RED代表白子
        }
        return null; // 空位置
    }
    
    @Override
    public void setPiece(int row, int col, Piece piece) {
        // 五子棋不支持直接设置棋子，这个方法留空
    }
    
    @Override
    public void movePiece(Position start, Position end) {
        // 五子棋不支持移动棋子，这个方法留空
    }
    
    @Override
    public void initializeBoard() {
        // 五子棋的初始化由GomokuBoard自己处理
    }
    
    @Override
    public void printBoard() {
        // 打印五子棋棋盘状态
        System.out.println("五子棋棋盘状态:");
        for (int i = 0; i < com.example.gomoku.core.GomokuBoard.BOARD_SIZE; i++) {
            for (int j = 0; j < com.example.gomoku.core.GomokuBoard.BOARD_SIZE; j++) {
                char piece = gomokuBoard.getPiece(i, j);
                if (piece == com.example.gomoku.core.GomokuBoard.BLACK) {
                    System.out.print("⚫ ");
                } else if (piece == com.example.gomoku.core.GomokuBoard.WHITE) {
                    System.out.print("⚪ ");
                } else {
                    System.out.print("+ ");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * 获取原始的五子棋棋盘对象
     */
    public com.example.gomoku.core.GomokuBoard getGomokuBoard() {
        return gomokuBoard;
    }
    
    /**
     * 虚拟棋子类，用于适配五子棋棋子到象棋Piece接口
     */
    private static class VirtualPiece extends Piece {
        private String name;
        
        public VirtualPiece(PieceColor color, String name) {
            super(color);
            this.name = name;
        }
        
        @Override
        public String getChineseName() {
            return name;
        }
        
        @Override
        public boolean isValidMove(Board board, Position start, Position end) {
            // 五子棋棋子不能移动
            return false;
        }

        @Override
        public Piece clone() {
            return new VirtualPiece(this.color, this.name);
        }
    }
}