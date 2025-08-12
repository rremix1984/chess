package com.example.launcher;

import javax.swing.*;

/**
 * App - 游戏平台主入口
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // 检查是否有命令行参数
            if (args.length > 0) {
                if ("chinese-chess".equalsIgnoreCase(args[0]) || "ai".equalsIgnoreCase(args[0])) {
                    // 直接启动中国象棋
                    startChineseChess();
                } else {
                    // 启动游戏选择界面
                    new GameSelectionFrame().setVisible(true);
                }
            } else {
                // 启动游戏选择界面
                new GameSelectionFrame().setVisible(true);
            }
        });
    }
    
    private static void startChineseChess() {
        try {
            // 使用反射来动态加载中国象棋游戏界面
            Class<?> gameFrameClass = Class.forName("com.example.chinesechess.ui.GameFrame");
            Object frame = gameFrameClass.getDeclaredConstructor().newInstance();
            gameFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "启动中国象棋失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            // 启动游戏选择界面作为后备
            new GameSelectionFrame().setVisible(true);
        }
    }
}
