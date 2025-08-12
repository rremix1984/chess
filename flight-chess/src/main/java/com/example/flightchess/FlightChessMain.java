package com.example.flightchess;

import javax.swing.SwingUtilities;

/**
 * 飞行棋游戏主启动类
 */
public class FlightChessMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在启动飞行棋游戏...");
                FlightChessFrame frame = new FlightChessFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("启动飞行棋游戏失败: " + e.getMessage());
            }
        });
    }
}