package com.example.go;

import javax.swing.SwingUtilities;

/**
 * 围棋游戏主启动类
 */
public class GoMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在启动围棋游戏...");
                GoFrame frame = new GoFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("启动围棋游戏失败: " + e.getMessage());
            }
        });
    }
}