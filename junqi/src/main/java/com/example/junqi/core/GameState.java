package com.example.junqi.core;

/**
 * 军棋游戏状态枚举
 */
public enum GameState {
    PLAYING("游戏中"),
    RED_WINS("红方获胜"),
    BLACK_WINS("黑方获胜"),
    DRAW("平局");
    
    private final String description;
    
    GameState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
