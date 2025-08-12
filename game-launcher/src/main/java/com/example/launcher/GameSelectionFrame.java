package com.example.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 游戏选择界面，让用户选择玩中国象棋或国际象棋
 */
public class GameSelectionFrame extends JFrame {

    public GameSelectionFrame() {
        setTitle("游戏中心");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        
        // 设置背景色
        getContentPane().setBackground(new Color(245, 245, 220)); // 米色背景
        
        // 使用BorderLayout布局
        setLayout(new BorderLayout());
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // 创建选择面板
        JPanel selectionPanel = createSelectionPanel();
        add(selectionPanel, BorderLayout.CENTER);
        
        // 创建底部面板
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 245, 220)); // 与主背景相同
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("欢迎来到游戏中心");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 32));
        titleLabel.setForeground(new Color(139, 69, 19)); // 棕色文字
        
        panel.add(titleLabel);
        return panel;
    }
    
    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 20, 30)); // 增加列数和调整间距
        panel.setBackground(new Color(245, 245, 220)); // 与主背景相同
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40)); // 增加边距
        
        // 中国象棋选项
        JPanel chineseChessPanel = createGameOptionPanel(
            "🏮 中国象棋", 
            "传统的中国象棋游戏，支持与AI对弈",
            "/images/chinese_chess_icon.png",
            e -> startChineseChess()
        );
        
        // 国际象棋选项 - 暂时不可用
        JPanel internationalChessPanel = createGameOptionPanel(
            "♟️ 国际象棋", 
            "经典的国际象棋游戏（开发中）",
            "/images/international_chess_icon.png",
            e -> showComingSoon("国际象棋")
        );
        
        // 五子棋选项 - 暂时不可用
        JPanel gomokuPanel = createGameOptionPanel(
            "⚫⚪ 五子棋", 
            "经典的五子棋游戏（开发中）",
            "/images/gomoku_icon.png",
            e -> showComingSoon("五子棋")
        );
        
        // 围棋选项
        JPanel goPanel = createGameOptionPanel(
            "⚫⚪ 围棋",
            "古老的策略棋盘游戏",
            "/images/go_icon.png",
            e -> startGo()
        );

        // 飞行棋选项
        JPanel aeroplaneChessPanel = createGameOptionPanel(
            "✈️ 飞行棋",
            "有趣的家庭友好型桌面游戏",
            "/images/aeroplane_chess_icon.png",
            e -> startAeroplaneChess()
        );
        
        // 坦克大战选项
        JPanel tankBattlePanel = createGameOptionPanel(
            "🚗 坦克大战",
            "经典的坦克对战游戏，支持AI对手",
            "/images/tank_battle_icon.png",
            e -> startTankBattle()
        );
        
        // 街头霸王选项 - 暂时不可用
        JPanel streetFighterPanel = createGameOptionPanel(
            "👊 街头霸王",
            "激烈的格斗游戏（开发中）",
            "/images/street_fighter_icon.png",
            e -> showComingSoon("街头霸王")
        );
        
        panel.add(chineseChessPanel);
        panel.add(internationalChessPanel);
        panel.add(gomokuPanel);
        panel.add(goPanel);
        panel.add(aeroplaneChessPanel);
        panel.add(tankBattlePanel);
        panel.add(streetFighterPanel);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(245, 245, 220));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        JButton closeButton = new JButton("关闭游戏");
        closeButton.setFont(new Font("宋体", Font.BOLD, 16));
        closeButton.setBackground(new Color(220, 20, 60)); // 更深的红色
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        closeButton.setPreferredSize(new Dimension(120, 40));
        
        // 添加鼠标悬停效果
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(new Color(178, 34, 34)); // 悬停时更深
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(new Color(220, 20, 60)); // 恢复原色
            }
        });
        
        closeButton.addActionListener(e -> System.exit(0));
        panel.add(closeButton);
        return panel;
    }
    
    private JPanel createGameOptionPanel(String title, String description, String imagePath, ActionListener actionListener) {
        // 创建一个可点击的按钮面板
        JButton gameButton = new JButton();
        gameButton.setLayout(new BoxLayout(gameButton, BoxLayout.Y_AXIS));
        gameButton.setBackground(new Color(255, 250, 240)); // 稍微浅一点的背景
        gameButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        gameButton.setFocusPainted(false);
        gameButton.setContentAreaFilled(true);
        gameButton.addActionListener(actionListener);
        
        // 图片（如果有）
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            // 调整图片大小
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(img));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            gameButton.add(imageLabel);
            gameButton.add(Box.createVerticalStrut(10));
        } catch (Exception e) {
            // 如果图片加载失败，使用文字替代
            JLabel placeholderLabel = new JLabel(getGameIcon(title));
            placeholderLabel.setFont(new Font("宋体", Font.BOLD, 36));
            placeholderLabel.setPreferredSize(new Dimension(80, 80));
            placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
            placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            placeholderLabel.setOpaque(true);
            placeholderLabel.setBackground(new Color(222, 184, 135));
            placeholderLabel.setForeground(Color.WHITE);
            gameButton.add(placeholderLabel);
            gameButton.add(Box.createVerticalStrut(10));
        }
        
        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(139, 69, 19)); // 棕色文字
        gameButton.add(titleLabel);
        gameButton.add(Box.createVerticalStrut(8));
        
        // 描述
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>"+description+"</div></html>");
        descLabel.setFont(new Font("宋体", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setForeground(new Color(105, 105, 105)); // 灰色文字
        gameButton.add(descLabel);
        
        // 添加鼠标悬停效果
        gameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                gameButton.setBackground(new Color(240, 248, 255)); // 悬停时浅蓝色
                gameButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(30, 144, 255), 3, true),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                gameButton.setBackground(new Color(255, 250, 240)); // 恢复原色
                gameButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(139, 69, 19), 2, true),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
        });
        
        // 将按钮包装在面板中以便布局
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(245, 245, 220));
        wrapperPanel.add(gameButton, BorderLayout.CENTER);
        
        return wrapperPanel;
    }
    
    // 获取游戏图标的辅助方法
    private String getGameIcon(String title) {
        if (title.contains("中国象棋")) return "♜";
        if (title.contains("国际象棋")) return "♛";
        if (title.contains("五子棋")) return "●";
        if (title.contains("围棋")) return "○";
        if (title.contains("飞行棋")) return "✈";
        if (title.contains("坦克大战")) return "🚗";
        if (title.contains("街头霸王")) return "👊";
        return "🎮";
    }
    
    private void startChineseChess() {
        try {
            dispose(); // 关闭选择界面
            SwingUtilities.invokeLater(() -> {
                try {
                    // 使用反射来动态加载中国象棋游戏界面
                    Class<?> gameFrameClass = Class.forName("com.example.chinesechess.ui.GameFrame");
                    Object frame = gameFrameClass.getDeclaredConstructor().newInstance();
                    gameFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "启动中国象棋失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    // 重新显示选择界面
                    new GameSelectionFrame().setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "启动中国象棋失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showComingSoon(String gameName) {
        JOptionPane.showMessageDialog(this, gameName + "游戏正在开发中，敬请期待...", "敬请期待", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startGo() {
        try {
            dispose(); // 关闭选择界面
            SwingUtilities.invokeLater(() -> {
                try {
                    // 使用反射来动态加载围棋游戏界面
                    Class<?> gameFrameClass = Class.forName("com.example.go.GoFrame");
                    Object frame = gameFrameClass.getDeclaredConstructor().newInstance();
                    gameFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "启动围棋失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    // 重新显示选择界面
                    new GameSelectionFrame().setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "启动围棋失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startAeroplaneChess() {
        try {
            dispose(); // 关闭选择界面
            SwingUtilities.invokeLater(() -> {
                try {
                    // 使用反射来动态加载飞行棋游戏界面
                    Class<?> gameFrameClass = Class.forName("com.example.flightchess.FlightChessFrame");
                    Object frame = gameFrameClass.getDeclaredConstructor().newInstance();
                    gameFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "启动飞行棋失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    // 重新显示选择界面
                    new GameSelectionFrame().setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "启动飞行棋失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startTankBattle() {
        try {
            dispose(); // 关闭选择界面
            SwingUtilities.invokeLater(() -> {
                try {
                    // 使用反射来动态加载坦克大战游戏
                    Class<?> gameClass = Class.forName("com.tankbattle.TankBattleGame");
                    Object[] args = {};
                    gameClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "启动坦克大战失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    // 重新显示选择界面
                    new GameSelectionFrame().setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "启动坦克大战失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startStreetFighter() {
        try {
            dispose(); // 关闭选择界面
            // JavaFX 应用需要在单独的线程中启动，不能在 Swing EDT 中
            new Thread(() -> {
                try {
                    // 使用JavaFX版本的街头霸王游戏（Java 11兼容）
                    Class<?> gameClass = Class.forName("com.example.gameproject.startGame");
                    Object[] args = {};
                    gameClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "启动街头霸王失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        // 重新显示选择界面
                        new GameSelectionFrame().setVisible(true);
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "启动街头霸王失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 主方法，启动游戏选择界面
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new GameSelectionFrame().setVisible(true);
        });
    }
}