package com.example.chinesechess;

import com.example.chinesechess.ui.GameFrame;
import javax.swing.SwingUtilities;

/**
 * 中国象棋游戏主启动类
 */
public class ChineseChessMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在启动中国象棋游戏...");
                GameFrame frame = new GameFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("启动中国象棋游戏失败: " + e.getMessage());
            }
        });
    }
}