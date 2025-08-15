package com.example.chinesechess;

import com.example.chinesechess.ui.GameFrame;
import com.example.chinesechess.ui.NetworkRoomFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 中国象棋游戏主启动类
 */
public class ChineseChessMain {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("正在启动中国象棋游戏...");
                
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // 显示游戏模式选择对话框
                showGameModeSelection();
                
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("启动中国象棋游戏失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 显示游戏模式选择对话框
     */
    private static void showGameModeSelection() {
        JDialog modeDialog = new JDialog((Frame)null, "🏮 中国象棋 - 选择游戏模式", true);
        modeDialog.setSize(450, 300);
        modeDialog.setLocationRelativeTo(null);
        modeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        modeDialog.setLayout(new BorderLayout());
        
        // 添加窗口关闭监听器
        modeDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        
        // 创建标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("🏮 中国象棋");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(139, 69, 19)); // 棕色
        titlePanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("请选择游戏模式");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(60, 60, 60));
        titlePanel.add(subtitleLabel);
        
        modeDialog.add(titlePanel, BorderLayout.NORTH);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // 单机游戏按钮
        JButton singlePlayerButton = createModeButton(
            "🎮 单机游戏", 
            "与AI对弈，离线游戏",
            new Color(100, 149, 237)
        );
        gbc.gridx = 0; gbc.gridy = 0;
        buttonPanel.add(singlePlayerButton, gbc);
        
        // 网络对弈按钮
        JButton networkGameButton = createModeButton(
            "🌐 网络对弈", 
            "与其他玩家在线对弈",
            new Color(34, 139, 34)
        );
        gbc.gridx = 0; gbc.gridy = 1;
        buttonPanel.add(networkGameButton, gbc);
        
        // 退出游戏按钮
        JButton exitButton = createModeButton(
            "❌ 退出游戏", 
            "关闭应用程序",
            new Color(178, 34, 34)
        );
        gbc.gridx = 0; gbc.gridy = 2;
        buttonPanel.add(exitButton, gbc);
        
        modeDialog.add(buttonPanel, BorderLayout.CENTER);
        
        // 按钮事件处理
        singlePlayerButton.addActionListener(e -> {
            modeDialog.dispose();
            startSinglePlayerGame();
        });
        
        networkGameButton.addActionListener(e -> {
            modeDialog.dispose();
            startNetworkGame();
        });
        
        exitButton.addActionListener(e -> {
            System.exit(0);
        });
        
        // 创建版本信息面板
        JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        versionPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        JLabel versionLabel = new JLabel("版本 v1.0 - AI对弈增强版");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        versionLabel.setForeground(Color.GRAY);
        versionPanel.add(versionLabel);
        
        modeDialog.add(versionPanel, BorderLayout.SOUTH);
        
        modeDialog.setVisible(true);
    }
    
    /**
     * 创建模式选择按钮
     */
    private static JButton createModeButton(String title, String description, Color color) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setPreferredSize(new Dimension(300, 50));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setFocusPainted(false);
        
        // 标题标签
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(color);
        button.add(titleLabel, BorderLayout.WEST);
        
        // 描述标签
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        descLabel.setForeground(Color.GRAY);
        button.add(descLabel, BorderLayout.SOUTH);
        
        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter().brighter());
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        return button;
    }
    
    /**
     * 启动单机游戏
     */
    private static void startSinglePlayerGame() {
        System.out.println("🎮 启动单机游戏模式...");
        GameFrame frame = new GameFrame();
        frame.setVisible(true);
    }
    
    /**
     * 启动网络游戏
     */
    private static void startNetworkGame() {
        System.out.println("🌐 启动网络对弈模式...");
        NetworkRoomFrame networkFrame = new NetworkRoomFrame();
        networkFrame.setVisible(true);
    }
}
