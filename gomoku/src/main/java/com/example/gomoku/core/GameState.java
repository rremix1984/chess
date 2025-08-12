package com.example.gomoku.core;

/**
 * 游戏状态枚举
 */
public enum GameState {
    PLAYING,        // 游戏进行中
    BLACK_WINS,     // 黑方获胜
    WHITE_WINS,     // 白方获胜  
    RED_WINS,       // 红方获胜（为了兼容现有代码）
    DRAW            // 和棋
}
