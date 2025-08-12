package com.example.gomoku;

import javax.swing.SwingUtilities;

/**
 * 五子棋游戏主启动类
 */
public class GomokuMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在启动五子棋游戏...");
                GomokuFrame frame = new GomokuFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("启动五子棋游戏失败: " + e.getMessage());
            }
        });
    }
}