package com.example.internationalchess;

import javax.swing.SwingUtilities;

/**
 * 国际象棋游戏主启动类
 */
public class InternationalChessMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在启动国际象棋游戏...");
                InternationalChessFrame frame = new InternationalChessFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("启动国际象棋游戏失败: " + e.getMessage());
            }
        });
    }
}