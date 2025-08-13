package com.example.flightchess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 飞行棋主界面
 */
public class FlightChessFrame extends JFrame {
    private FlightChessBoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel diceLabel;
    private JButton rollDiceButton;
    private JButton restartButton;
    private JCheckBox[] aiCheckBoxes;
    private JComboBox<String>[] difficultyComboBoxes;
    private JLabel[] playerLabels;
    
    public FlightChessFrame() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        updateStatus();
        
        setTitle("飞行棋游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * 设置按钮样式
     */
    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(Color.BLACK);
        button.setOpaque(true);
        
        // 添加鼠标交互效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = new Color(240, 240, 240);
            Color hoverColor = originalColor.brighter();
            Color pressedColor = originalColor.darker();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLoweredBevelBorder(),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.setBackground(pressedColor);
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void initializeComponents() {
        boardPanel = new FlightChessBoardPanel();
        boardPanel.setOnStateUpdate(this::updateStatus);
        
        statusLabel = new JLabel("当前玩家：红色");
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        
        diceLabel = new JLabel("骰子：-");
        diceLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        rollDiceButton = new JButton("投掷骰子");
        rollDiceButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        styleButton(rollDiceButton);
        
        restartButton = new JButton("重新开始");
        restartButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        styleButton(restartButton);
        
        // 玩家设置
        String[] playerNames = {"红色", "蓝色", "黄色", "绿色"};
        Color[] playerColors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
        aiCheckBoxes = new JCheckBox[4];
        difficultyComboBoxes = new JComboBox[4];
        playerLabels = new JLabel[4];
        
        String[] difficulties = {"简单", "容易", "中等", "困难", "专家"};
        
        for (int i = 0; i < 4; i++) {
            playerLabels[i] = new JLabel(playerNames[i] + "玩家");
            playerLabels[i].setForeground(playerColors[i]);
            playerLabels[i].setFont(new Font("微软雅黑", Font.BOLD, 12));
            
            aiCheckBoxes[i] = new JCheckBox("AI");
            aiCheckBoxes[i].setFont(new Font("微软雅黑", Font.PLAIN, 12));
            
            difficultyComboBoxes[i] = new JComboBox<>(difficulties);
            difficultyComboBoxes[i].setSelectedIndex(2); // 默认中等难度
            difficultyComboBoxes[i].setFont(new Font("微软雅黑", Font.PLAIN, 12));
            difficultyComboBoxes[i].setEnabled(false);
            
            final int playerIndex = i;
            aiCheckBoxes[i].addActionListener(e -> {
                boolean isAI = aiCheckBoxes[playerIndex].isSelected();
                difficultyComboBoxes[playerIndex].setEnabled(isAI);
                boardPanel.setAIPlayer(playerIndex, isAI, 
                    difficultyComboBoxes[playerIndex].getSelectedIndex() + 1);
            });
            
            difficultyComboBoxes[i].addActionListener(e -> {
                if (aiCheckBoxes[playerIndex].isSelected()) {
                    boardPanel.setAIPlayer(playerIndex, true, 
                        difficultyComboBoxes[playerIndex].getSelectedIndex() + 1);
                }
            });
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 主面板
        add(boardPanel, BorderLayout.CENTER);
        
        // 控制面板
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // 状态面板
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(diceLabel);
        controlPanel.add(statusPanel, BorderLayout.NORTH);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(rollDiceButton);
        buttonPanel.add(restartButton);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // 玩家设置面板
        JPanel playerPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        playerPanel.setBorder(BorderFactory.createTitledBorder("玩家设置"));
        
        for (int i = 0; i < 4; i++) {
            playerPanel.add(playerLabels[i]);
            playerPanel.add(aiCheckBoxes[i]);
            playerPanel.add(difficultyComboBoxes[i]);
        }
        
        controlPanel.add(playerPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.EAST);
        
        // 菜单栏
        setJMenuBar(createMenuBar());
    }
    
    private void setupEventHandlers() {
        rollDiceButton.addActionListener(e -> {
            boardPanel.rollDice();
        });
        
        restartButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要重新开始游戏吗？",
                "确认",
                JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                boardPanel.restartGame();
            }
        });
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 游戏菜单
        JMenu gameMenu = new JMenu("游戏");
        gameMenu.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        JMenuItem newGameItem = new JMenuItem("新游戏");
        newGameItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        newGameItem.addActionListener(e -> boardPanel.restartGame());
        
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        exitItem.addActionListener(e -> System.exit(0));
        
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        JMenuItem rulesItem = new JMenuItem("游戏规则");
        rulesItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rulesItem.addActionListener(e -> showGameRules());
        
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(rulesItem);
        helpMenu.add(aboutItem);
        
        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void updateStatus() {
        FlightChessGame game = boardPanel.getGame();
        
        if (game.isGameOver()) {
            statusLabel.setText("游戏结束！获胜者：" + getPlayerName(game.getWinner()));
            rollDiceButton.setEnabled(false);
        } else {
            String currentPlayerName = getPlayerName(game.getCurrentPlayer());
            statusLabel.setText("当前玩家：" + currentPlayerName);
            rollDiceButton.setEnabled(!boardPanel.isWaitingForMove());
        }
        
        int diceValue = boardPanel.getDiceValue();
        if (diceValue > 0) {
            diceLabel.setText("骰子：" + diceValue);
        } else {
            diceLabel.setText("骰子：-");
        }
    }
    
    private String getPlayerName(int player) {
        String[] names = {"红色", "蓝色", "黄色", "绿色"};
        return names[player];
    }
    
    private void showGameRules() {
        String rules = "飞行棋游戏规则：\n\n" +
            "1. 游戏目标：\n" +
            "   - 将所有4架飞机从家园移动到终点\n\n" +
            "2. 基本规则：\n" +
            "   - 投掷骰子决定移动步数\n" +
            "   - 投掷6点可以起飞一架飞机\n" +
            "   - 投掷6点后可以再次投掷\n" +
            "   - 飞机可以击落其他玩家的飞机\n" +
            "   - 被击落的飞机返回家园\n\n" +
            "3. 安全位置：\n" +
            "   - 某些位置是安全的，不会被击落\n" +
            "   - 家园和终点跑道是安全的\n\n" +
            "4. 获胜条件：\n" +
            "   - 第一个将所有飞机移动到终点的玩家获胜\n\n" +
            "5. AI设置：\n" +
            "   - 可以设置任意玩家为AI\n" +
            "   - AI有5个难度等级可选择";
        
        JTextArea textArea = new JTextArea(rules);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(getBackground());
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "游戏规则", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAbout() {
        String about = "飞行棋游戏 v1.0\n\n" +
            "这是一个经典的飞行棋游戏实现，支持：\n" +
            "- 1-4人游戏\n" +
            "- AI对手（5个难度等级）\n" +
            "- 完整的游戏规则\n" +
            "- 美观的图形界面\n\n" +
            "开发者：AI助手";
        
        JOptionPane.showMessageDialog(this, about, "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new FlightChessFrame().setVisible(true);
        });
    }
}