package com.example.chinesechess.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 中国象棋GUI版主启动类
 * 
 * @author AI助手
 * @version 1.0
 */
public class ChessGameMain {
    
    public static void main(String[] args) {
        // 设置字体，确保中文显示正常
        Font font = new Font("宋体", Font.PLAIN, 12);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("TitledBorder.font", font);
        
        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建并显示游戏窗口
                    GameFrame frame = new GameFrame();
                    frame.setVisible(true);
                    
                    // 显示欢迎信息
                    showWelcomeMessage();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "启动游戏时发生错误：" + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    /**
     * 显示欢迎信息
     */
    private static void showWelcomeMessage() {
        String message = "🎮 欢迎使用中国象棋AI对弈版！\n\n" +
                        "🎯 功能特色：\n" +
                        "• 🎨 精美的图形界面\n" +
                        "• 🤖 多种AI引擎（传统AI、增强AI、大模型AI、混合AI）\n" +
                        "• 💬 智能聊天助手\n" +
                        "• 🎮 人机对弈\n" +
                        "• 📊 实时状态显示\n\n" +
                        "💡 使用提示：\n" +
                        "1. 点击棋子选择，再点击目标位置移动\n" +
                        "2. 在顶部面板选择AI类型和难度\n" +
                        "3. 点击'启用AI对弈'开始人机对战\n" +
                        "4. 使用右侧聊天面板与AI交流棋局\n\n" +
                        "🚀 祝您游戏愉快！";
        
        JOptionPane.showMessageDialog(null, 
            message, 
            "🏮 中国象棋 - 欢迎", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}