package com.example.chinesechess.core;

/**
 * 游戏状态枚举
 */
public enum GameState {
    PLAYING,        // 游戏进行中
    IN_CHECK,       // 被将军但游戏继续
    RED_WINS,       // 红方获胜
    BLACK_WINS,     // 黑方获胜
    DRAW            // 和棋
}