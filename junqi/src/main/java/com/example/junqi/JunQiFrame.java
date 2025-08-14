package com.example.junqi;

import com.example.common.config.GameConfig;
import com.example.common.utils.ExceptionHandler;
import com.example.common.utils.OllamaModelManager;
import com.example.junqi.core.*;
import com.example.junqi.ui.JunQiBoardPanelAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 军棋游戏主界面
 */
public class JunQiFrame extends JFrame {

    private JLabel statusLabel;
    private JunQiBoardPanelAdapter boardPanel;
    private JTextArea aiLogArea;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> playerColorComboBox;
    private JComboBox<String> aiTypeComboBox;
    private JComboBox<String> modelComboBox;
    
    // 游戏模式相关
    private ButtonGroup gameModeGroup;
    private JRadioButton playerVsAIRadio;
    private JRadioButton playerVsPlayerRadio;
    private JButton startButton;
    private JButton pauseButton;
    
    // 游戏管理器
    private JunQiGameManager gameManager;
    
    // 棋局状态统计信息
    private JLabel gameStatsLabel;
    private JLabel playerInfoLabel;
    private JLabel moveCountLabel;
    private JLabel advantageLabel;

    /**
     * 统一按钮样式和点击效果
     */
    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UIManager.getColor("Button.background"));
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().darker());
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(UIManager.getColor("Button.background"));
            }
        });
    }

    public JunQiFrame() {
        setTitle("军棋游戏 - 人机对战");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // 添加窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });

        // 创建游戏管理器
        gameManager = new JunQiGameManager();
        
        // 创建棋盘
        boardPanel = new JunQiBoardPanelAdapter(gameManager);
        
        // 创建主要内容面板（棋盘+右侧面板）
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        
        // 创建右侧面板（AI日志）
        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);

        // 创建控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // 创建状态栏
        statusLabel = new JLabel("⚫ 当前轮到: 红方", JLabel.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel.setPreferredSize(new Dimension(getWidth(), 35));
        add(statusLabel, BorderLayout.SOUTH);

        // 初始化游戏管理器
        initializeGameManager();
        
        // 设置BoardPanel的状态更新回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 初始化游戏模式设置
        updateGameModeSettings();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🎮 军棋对战控制"));
        panel.setPreferredSize(new Dimension(getWidth(), 120));

        // 左侧：基本设置
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // 游戏模式选择
        leftPanel.add(new JLabel("模式:"));
        gameModeGroup = new ButtonGroup();
        playerVsPlayerRadio = new JRadioButton("玩家对玩家", true);
        playerVsAIRadio = new JRadioButton("玩家对AI");
        
        playerVsPlayerRadio.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        playerVsAIRadio.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        playerVsPlayerRadio.addActionListener(e -> updateGameModeSettings());
        playerVsAIRadio.addActionListener(e -> updateGameModeSettings());
        
        gameModeGroup.add(playerVsPlayerRadio);
        gameModeGroup.add(playerVsAIRadio);
        
        leftPanel.add(playerVsPlayerRadio);
        leftPanel.add(playerVsAIRadio);
        
        // 玩家颜色选择
        leftPanel.add(new JLabel("颜色:"));
        playerColorComboBox = new JComboBox<>(new String[]{"红方", "黑方"});
        playerColorComboBox.setPreferredSize(new Dimension(80, 25));
        playerColorComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        playerColorComboBox.addActionListener(e -> updatePlayerColor());
        leftPanel.add(playerColorComboBox);

        // AI类型选择
        leftPanel.add(new JLabel("AI:"));
        aiTypeComboBox = new JComboBox<>(new String[]{"基础AI", "高级AI", "神经网络AI", "大模型AI"});
        aiTypeComboBox.setSelectedIndex(1); // 默认选择高级AI
        aiTypeComboBox.setPreferredSize(new Dimension(100, 25));
        aiTypeComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(aiTypeComboBox);

        // AI难度选择
        leftPanel.add(new JLabel("难度:"));
        difficultyComboBox = new JComboBox<>(new String[]{"简单", "普通", "困难", "专家", "大师"});
        difficultyComboBox.setSelectedIndex(1); // 默认普通难度
        difficultyComboBox.setPreferredSize(new Dimension(80, 25));
        difficultyComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(difficultyComboBox);
        
        // 模型选择
        leftPanel.add(new JLabel("模型:"));
        try {
            List<String> availableModels = OllamaModelManager.getAvailableModels();
            modelComboBox = new JComboBox<>(availableModels.toArray(new String[0]));
            if (!availableModels.isEmpty()) {
                modelComboBox.setSelectedIndex(0);
            }
            ExceptionHandler.logInfo("成功加载 " + availableModels.size() + " 个AI模型", "军棋界面");
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "加载AI模型列表", false);
            modelComboBox = new JComboBox<>(new String[]{"qwen2.5:7b", "llama3.1:8b"});
            modelComboBox.setSelectedIndex(0);
        }
        modelComboBox.setPreferredSize(new Dimension(150, 25));
        modelComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(modelComboBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：控制按钮
        JPanel rightPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 第一行按钮
        startButton = new JButton("开始游戏");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        startButton.addActionListener(e -> startGame());
        styleButton(startButton);
        rightPanel.add(startButton);
        
        pauseButton = new JButton("暂停游戏");
        pauseButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        pauseButton.addActionListener(e -> pauseGame());
        pauseButton.setEnabled(false);
        styleButton(pauseButton);
        rightPanel.add(pauseButton);
        
        JButton restartButton = new JButton("重新开始");
        restartButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        restartButton.addActionListener(e -> restartGame());
        styleButton(restartButton);
        rightPanel.add(restartButton);
        
        // 第二行按钮
        JButton exitButton = new JButton("退出游戏");
        exitButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        exitButton.addActionListener(e -> exitGame());
        styleButton(exitButton);
        rightPanel.add(exitButton);
        
        JButton backButton = new JButton("返回选择");
        backButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        backButton.addActionListener(e -> returnToSelection());
        styleButton(backButton);
        rightPanel.add(backButton);
        
        // 占位
        rightPanel.add(new JLabel(""));
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        // 添加棋局状态统计面板
        JPanel gameStatsPanel = createGameStatsPanel();
        panel.add(gameStatsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建棋局状态统计面板
     */
    private JPanel createGameStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("📊 棋局状态"));
        panel.setBackground(Color.LIGHT_GRAY);
        
        // 对战双方信息
        playerInfoLabel = new JLabel("🔴：玩家   ⚫：玩家");
        playerInfoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        playerInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 手数统计
        moveCountLabel = new JLabel("手数：0");
        moveCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        moveCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 优势分析
        advantageLabel = new JLabel("优势：均势");
        advantageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        advantageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 游戏状态
        gameStatsLabel = new JLabel("状态：等待开始");
        gameStatsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gameStatsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(playerInfoLabel);
        panel.add(moveCountLabel);
        panel.add(advantageLabel);
        panel.add(gameStatsLabel);
        
        return panel;
    }
    
    /**
     * 创建右侧面板（AI日志）
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 600));
        
        // AI日志面板
        JPanel aiLogPanel = new JPanel(new BorderLayout());
        aiLogPanel.setBorder(BorderFactory.createTitledBorder("🤖 AI思考日志"));
        
        aiLogArea = new JTextArea();
        aiLogArea.setEditable(false);
        aiLogArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        aiLogArea.setBackground(new Color(248, 248, 255));
        aiLogArea.setText("等待游戏开始...\n");
        
        JScrollPane aiLogScrollPane = new JScrollPane(aiLogArea);
        aiLogPanel.add(aiLogScrollPane, BorderLayout.CENTER);
        
        panel.add(aiLogPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 更新状态栏
     */
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * 更新游戏模式设置
     */
    private void updateGameModeSettings() {
        JunQiGameManager.GameMode selectedMode;
        if (playerVsPlayerRadio.isSelected()) {
            selectedMode = JunQiGameManager.GameMode.PLAYER_VS_PLAYER;
        } else {
            selectedMode = JunQiGameManager.GameMode.PLAYER_VS_AI;
        }
        
        // 通过GameManager设置游戏模式
        String aiType = (String) aiTypeComboBox.getSelectedItem();
        String difficulty = (String) difficultyComboBox.getSelectedItem();
        String modelName = (String) modelComboBox.getSelectedItem();
        
        if (gameManager != null) {
            gameManager.setGameMode(selectedMode, aiType, difficulty, modelName);
        }
        
        // 更新UI状态
        if (selectedMode == JunQiGameManager.GameMode.PLAYER_VS_PLAYER) {
            playerColorComboBox.setEnabled(false);
            difficultyComboBox.setEnabled(false);
            aiTypeComboBox.setEnabled(false);
            modelComboBox.setEnabled(false);
        } else {
            playerColorComboBox.setEnabled(true);
            difficultyComboBox.setEnabled(true);
            aiTypeComboBox.setEnabled(true);
            modelComboBox.setEnabled(true);
        }
        
        updatePlayerInfoLabel();
    }
    
    /**
     * 更新玩家颜色
     */
    private void updatePlayerColor() {
        boolean isPlayerRed = playerColorComboBox.getSelectedIndex() == 0;
        if (gameManager != null) {
            gameManager.setPlayerColor(isPlayerRed);
        }
        updatePlayerInfoLabel();
    }
    
    /**
     * 开始游戏
     */
    private void startGame() {
        if (gameManager != null) {
            gameManager.startGame();
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            
            ExceptionHandler.logInfo("军棋游戏已启动", "军棋界面");
        }
    }
    
    /**
     * 暂停游戏
     */
    private void pauseGame() {
        if (gameManager != null) {
            gameManager.pauseGame();
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            ExceptionHandler.logInfo("军棋游戏已暂停", "军棋界面");
        }
    }
    
    /**
     * 重新开始游戏
     */
    private void restartGame() {
        boardPanel.resetGame();
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        
        System.out.println("🔄 军棋游戏已重新开始");
    }
    
    /**
     * 退出游戏
     */
    private void exitGame() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "确定要退出游戏吗？",
            "退出确认",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            handleWindowClosing();
        }
    }
    
    /**
     * 返回游戏选择界面
     */
    private void returnToSelection() {
        try {
            // 清理资源
            if (gameManager != null) {
                gameManager.shutdown();
            }
            
            dispose();
            
            System.out.println("返回游戏选择界面");
            
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "返回游戏选择界面", false);
        }
    }
    
    /**
     * 处理窗口关闭事件
     */
    private void handleWindowClosing() {
        try {
            ExceptionHandler.logInfo("正在关闭军棋游戏窗口...", "军棋界面");
            
            if (gameManager != null) {
                gameManager.shutdown();
            }
            
            dispose();
            
            if (Window.getWindows().length <= 1) {
                ExceptionHandler.logInfo("应用程序即将退出...", "军棋界面");
                System.exit(0);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "关闭窗口", false);
            System.exit(1);
        }
    }
    
    /**
     * 初始化游戏管理器
     */
    private void initializeGameManager() {
        gameManager = new JunQiGameManager();
        
        // 设置游戏回调
        gameManager.setGameCallback(new JunQiGameManager.GameCallback() {
            @Override
            public void onGameStateChanged(GameState newState, String winner) {
                SwingUtilities.invokeLater(() -> {
                    String status;
                    switch (newState) {
                        case RED_WINS:
                            status = "🔴 红方获胜！";
                            break;
                        case BLACK_WINS:
                            status = "⚫ 黑方获胜！";
                            break;
                        case DRAW:
                            status = "🤝 平局！";
                            break;
                        default:
                            status = "游戏进行中...";
                            break;
                    }
                    
                    updateStatus(status);
                    gameStatsLabel.setText("状态：" + status);
                    
                    // 更新统计信息
                    updateGameStats();
                    
                    // 如果游戏结束，重新启用开始按钮
                    if (newState != GameState.PLAYING) {
                        startButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                        
                        // 显示游戏结束对话框
                        JOptionPane.showMessageDialog(JunQiFrame.this,
                            "游戏结束：" + winner,
                            "军棋游戏",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
            
            @Override
            public void onTurnChanged(boolean isRedTurn, JunQiGameManager.PlayerType currentPlayerType) {
                SwingUtilities.invokeLater(() -> {
                    String currentPlayer = isRedTurn ? "红方" : "黑方";
                    String playerType = currentPlayerType == JunQiGameManager.PlayerType.HUMAN ? "玩家" : "AI";
                    String aiStatus = gameManager.isAIThinking() ? " (AI思考中...)" : "";
                    
                    String status = String.format("轮到: %s%s [%s]%s", 
                                                 isRedTurn ? "🔴" : "⚫", 
                                                 currentPlayer, 
                                                 gameManager.getCurrentMode().displayName,
                                                 aiStatus);
                    updateStatus(status);
                    
                    // 更新棋盘显示
                    if (boardPanel != null) {
                        boardPanel.repaint();
                    }
                    
                    // 更新统计信息
                    updateGameStats();
                });
            }
            
            @Override
            public void onAIThinking(String message) {
                SwingUtilities.invokeLater(() -> {
                    if (aiLogArea != null) {
                        aiLogArea.append("🤔 " + message + "\n");
                        aiLogArea.setCaretPosition(aiLogArea.getDocument().getLength());
                    }
                });
            }
            
            @Override
            public void onAIMove(int fromRow, int fromCol, int toRow, int toCol, String analysis) {
                SwingUtilities.invokeLater(() -> {
                    if (aiLogArea != null) {
                        String moveStr;
                        if (toRow == -1 && toCol == -1) {
                            moveStr = String.format("🎯 AI翻棋: (%d, %d) - %s\n", fromRow, fromCol, analysis);
                        } else {
                            moveStr = String.format("🎯 AI移动: (%d,%d)->(%d,%d) - %s\n", 
                                                  fromRow, fromCol, toRow, toCol, analysis);
                        }
                        aiLogArea.append(moveStr);
                        aiLogArea.setCaretPosition(aiLogArea.getDocument().getLength());
                    }
                    
                    // 更新棋盘显示
                    if (boardPanel != null) {
                        boardPanel.repaint();
                    }
                });
            }
            
            @Override
            public void onPieceFlipped(int row, int col, JunQiPiece piece) {
                SwingUtilities.invokeLater(() -> {
                    if (aiLogArea != null) {
                        aiLogArea.append(String.format("🔄 翻棋: (%d,%d) %s\n", 
                                                      row, col, piece.getDisplayName()));
                        aiLogArea.setCaretPosition(aiLogArea.getDocument().getLength());
                    }
                    
                    // 更新棋盘显示
                    if (boardPanel != null) {
                        boardPanel.repaint();
                    }
                });
            }
            
            @Override
            public void onGameModeChanged(JunQiGameManager.GameMode newMode) {
                SwingUtilities.invokeLater(() -> {
                    updatePlayerInfoLabel();
                    
                    if (aiLogArea != null) {
                        aiLogArea.append("📋 游戏模式切换为: " + newMode.displayName + "\n");
                        aiLogArea.setCaretPosition(aiLogArea.getDocument().getLength());
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(JunQiFrame.this, 
                                                error, 
                                                "游戏错误", 
                                                JOptionPane.ERROR_MESSAGE);
                    
                    if (aiLogArea != null) {
                        aiLogArea.append("❌ 错误: " + error + "\n");
                        aiLogArea.setCaretPosition(aiLogArea.getDocument().getLength());
                    }
                });
            }
            
            @Override
            public void onPieceSelected(int row, int col) {
                SwingUtilities.invokeLater(() -> {
                    if (boardPanel != null) {
                        boardPanel.repaint();
                    }
                });
            }
            
            @Override
            public void onPieceDeselected() {
                SwingUtilities.invokeLater(() -> {
                    if (boardPanel != null) {
                        boardPanel.repaint();
                    }
                });
            }
        });
        
        System.out.println("🎮 军棋游戏管理器初始化完成");
    }
    
    /**
     * 更新游戏统计信息
     */
    private void updateGameStats() {
        if (gameManager != null) {
            // 更新手数
            int moveCount = gameManager.getBoard().getMoveCount();
            moveCountLabel.setText("手数：" + moveCount);
            
            // 更新游戏状态
            String gameStatus;
            if (gameManager.isGameRunning()) {
                if (gameManager.isGamePaused()) {
                    gameStatus = "状态：暂停";
                } else if (gameManager.isAIThinking()) {
                    gameStatus = "状态：AI思考中";
                } else {
                    gameStatus = "状态：进行中";
                }
            } else {
                gameStatus = "状态：未开始";
            }
            
            gameStatsLabel.setText(gameStatus);
            
            // 优势分析
            if (moveCount < 10) {
                advantageLabel.setText("优势：开局阶段");
            } else if (gameManager.getBoard().getGameState() != GameState.PLAYING) {
                GameState state = gameManager.getBoard().getGameState();
                if (state == GameState.RED_WINS) {
                    advantageLabel.setText("优势：红方胜利");
                } else if (state == GameState.BLACK_WINS) {
                    advantageLabel.setText("优势：黑方胜利");
                } else {
                    advantageLabel.setText("优势：平局");
                }
            } else {
                advantageLabel.setText("优势：均势");
            }
        }
    }
    
    /**
     * 更新玩家信息标签
     */
    private void updatePlayerInfoLabel() {
        if (gameManager != null) {
            JunQiGameManager.GameMode mode = gameManager.getCurrentMode();
            JunQiGameManager.PlayerType redPlayer = gameManager.getRedPlayer();
            JunQiGameManager.PlayerType blackPlayer = gameManager.getBlackPlayer();
            
            String redInfo = redPlayer == JunQiGameManager.PlayerType.HUMAN ? "玩家" : "AI";
            String blackInfo = blackPlayer == JunQiGameManager.PlayerType.HUMAN ? "玩家" : "AI";
            
            playerInfoLabel.setText(String.format("🔴：%s   ⚫：%s", redInfo, blackInfo));
        }
    }
    
    /**
     * 主方法，启动军棋游戏
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "设置系统外观", false);
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                ExceptionHandler.logInfo("正在启动军棋游戏...", "主程序");
                new JunQiFrame().setVisible(true);
                ExceptionHandler.logInfo("军棋游戏启动成功", "主程序");
            } catch (Exception e) {
                ExceptionHandler.handleException(e, "启动军棋游戏", true);
                System.exit(1);
            }
        });
    }
}
