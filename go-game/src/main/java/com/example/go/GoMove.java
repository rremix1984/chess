package com.example.go;

import java.util.List;
import java.util.ArrayList;

/**
 * 围棋移动记录类
 */
public class GoMove {
    public final GoPosition position;  // 落子位置，null表示弃权
    public final int player;          // 玩家（黑棋或白棋）
    public final List<GoPosition> capturedStones; // 被吃掉的棋子位置
    
    public GoMove(GoPosition position, int player) {
        this.position = position;
        this.player = player;
        this.capturedStones = new ArrayList<>();
    }
    
    public boolean isPass() {
        return position == null;
    }
    
    @Override
    public String toString() {
        if (isPass()) {
            return "弃权";
        }
        return "(" + (position.row + 1) + "," + (position.col + 1) + ")";
    }
}