package com.example.internationalchess.core;

/**
 * 游戏状态枚举
 * 定义国际象棋游戏的各种状态
 */
public enum GameState {
    PLAYING("游戏进行中"),
    WHITE_WIN("白方获胜"),
    BLACK_WIN("黑方获胜"),
    DRAW("平局"),
    STALEMATE("僵局"),
    WHITE_CHECK("白方被将军"),
    BLACK_CHECK("黑方被将军"),
    WHITE_CHECKMATE("白方被将死"),
    BLACK_CHECKMATE("黑方被将死"),
    PAUSED("游戏暂停"),
    STOPPED("游戏停止"),
    RED_WINS("红方获胜"),
    BLACK_WINS("黑方获胜");
    
    private final String displayName;
    
    GameState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 判断游戏是否结束
     */
    public boolean isGameOver() {
        return this == WHITE_WIN || this == BLACK_WIN || this == DRAW || 
               this == STALEMATE || this == WHITE_CHECKMATE || this == BLACK_CHECKMATE;
    }
    
    /**
     * 判断是否为将军状态
     */
    public boolean isCheck() {
        return this == WHITE_CHECK || this == BLACK_CHECK;
    }
    
    /**
     * 判断是否为将死状态
     */
    public boolean isCheckmate() {
        return this == WHITE_CHECKMATE || this == BLACK_CHECKMATE;
    }
    
    /**
     * 获取获胜方
     */
    public PieceColor getWinner() {
        switch (this) {
            case WHITE_WIN:
            case BLACK_CHECKMATE:
                return PieceColor.WHITE;
            case BLACK_WIN:
            case WHITE_CHECKMATE:
                return PieceColor.BLACK;
            default:
                return null;
        }
    }
}