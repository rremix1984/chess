package com.example.launcher;

import javax.swing.SwingUtilities;

/**
 * 多游戏平台主启动器
 */
public class GameLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在启动多游戏平台...");
                GameCenterFrame frame = new GameCenterFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("启动游戏平台失败: " + e.getMessage());
            }
        });
    }
}