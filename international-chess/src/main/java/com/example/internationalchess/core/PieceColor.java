package com.example.internationalchess.core;

/**
 * 棋子颜色枚举
 * 定义国际象棋中的棋子颜色
 */
public enum PieceColor {
    WHITE("白方"),
    BLACK("黑方"),
    RED("红方"); // 用于某些特殊AI模式
    
    private final String displayName;
    
    PieceColor(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取对手颜色
     */
    public PieceColor getOpponent() {
        switch (this) {
            case WHITE:
                return BLACK;
            case BLACK:
                return WHITE;
            case RED:
                return BLACK; // 默认红方对黑方
            default:
                return BLACK;
        }
    }
    
    /**
     * 转换为字符表示
     */
    public char toChar() {
        switch (this) {
            case WHITE:
                return 'W';
            case BLACK:
                return 'B';
            case RED:
                return 'R';
            default:
                return 'B';
        }
    }
    
    /**
     * 从字符创建PieceColor
     */
    public static PieceColor fromChar(char c) {
        switch (c) {
            case 'W':
                return WHITE;
            case 'B':
                return BLACK;
            case 'R':
                return RED;
            default:
                return BLACK;
        }
    }
}