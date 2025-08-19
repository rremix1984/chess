package com.example.gomoku;

import com.example.common.config.GameConfig;
import com.example.common.utils.ExceptionHandler;
import com.example.common.utils.OllamaModelManager;
import com.example.gomoku.core.GameState;
import com.example.gomoku.core.GomokuGameManager;
import com.example.gomoku.core.GomokuGameManager.GameMode;
import com.example.gomoku.core.GomokuGameManager.PlayerType;
import com.example.gomoku.ui.*; // 导入所有UI包中的类
import com.example.gomoku.ChatPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.example.common.ui.BoardWithFloatButton;
import com.example.common.ui.FullscreenToggler;
import java.util.List;

/**
 * 五子棋游戏界面
 */
public class GomokuFrame extends JFrame {

    private JLabel statusLabel;
    private GomokuBoardPanelAdapter boardPanel;
    private BoardWithFloatButton boardContainer;
    private ChatPanel chatPanel;
    private JTextArea aiLogArea;
    private JButton aiToggleButton;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> playerColorComboBox;
    private JComboBox<String> aiTypeComboBox;
    private JComboBox<String> modelComboBox;
    
    // 游戏模式相关
    private ButtonGroup gameModeGroup;
    private JRadioButton playerVsAIRadio;
    private JRadioButton aiVsAIRadio;
    private JRadioButton playerVsPlayerRadio;
    private JButton startButton;
    private JButton pauseButton;
    private String currentGameMode = "玩家对玩家";
    private boolean isAIvsAIMode = false;
    private Object blackAI;
    private Object whiteAI;

    private JPanel controlPanel;
    private JPanel rightPanel;
    private FullscreenToggler fullscreenToggler;
    
    // 游戏管理器
    private GomokuGameManager gameManager;
    
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

    public GomokuFrame() {
        setTitle(GameConfig.WINDOW_TITLE);
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout());
        
        // 添加窗口关闭监听器，确保资源正确释放
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });

        // 创建游戏管理器
        gameManager = new GomokuGameManager();
        
        // 创建棋盘
        boardPanel = new GomokuBoardPanelAdapter(gameManager);
        
        // 创建聊天面板
        chatPanel = new ChatPanel();
        
        // 设置BoardPanel的聊天面板引用
        boardPanel.setChatPanel(chatPanel);
        
        // 设置ChatPanel的五子棋棋盘引用
        chatPanel.setGomokuBoard(boardPanel.getBoard());
        
        // 创建主要内容面板（棋盘+右侧面板）
        JPanel mainPanel = new JPanel(new BorderLayout());
        boardContainer = new BoardWithFloatButton(boardPanel);
        mainPanel.add(boardContainer, BorderLayout.CENTER);

        // 创建右侧面板（AI日志+聊天）
        rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);

        // 创建控制面板
        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // 创建状态栏
        statusLabel = new JLabel("⚫ 当前玩家: 黑方", JLabel.CENTER);
        statusLabel.setFont(GameConfig.TITLE_FONT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel.setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, 30));
        add(statusLabel, BorderLayout.SOUTH);

        fullscreenToggler = new FullscreenToggler(this, controlPanel, rightPanel);
        boardContainer.getFullscreenButton().addActionListener(e -> {
            if (!fullscreenToggler.isFullscreen()) {
                fullscreenToggler.toggle();
            }
        });
        boardContainer.getExitButton().addActionListener(e -> {
            if (fullscreenToggler.isFullscreen()) {
                fullscreenToggler.toggle();
            }
        });

        // 初始化游戏管理器
        initializeGameManager();
        
        // 设置BoardPanel的状态更新回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 初始化游戏模式设置
        updateGameModeSettings();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🎮 五子棋对弈控制"));
        panel.setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, 120)); // 增加高度以容纳更多按钮

        // 左侧：基本设置（紧凑布局）
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // 游戏模式选择
        leftPanel.add(new JLabel("模式:"));
        gameModeGroup = new ButtonGroup();
        playerVsPlayerRadio = new JRadioButton("玩家对玩家", true);
        playerVsAIRadio = new JRadioButton("玩家对AI");
        aiVsAIRadio = new JRadioButton("AI对AI");
        
        playerVsPlayerRadio.setFont(GameConfig.DEFAULT_FONT);
        playerVsAIRadio.setFont(GameConfig.DEFAULT_FONT);
        aiVsAIRadio.setFont(GameConfig.DEFAULT_FONT);
        
        playerVsPlayerRadio.addActionListener(e -> updateGameModeSettings());
        playerVsAIRadio.addActionListener(e -> updateGameModeSettings());
        aiVsAIRadio.addActionListener(e -> updateGameModeSettings());
        
        gameModeGroup.add(playerVsPlayerRadio);
        gameModeGroup.add(playerVsAIRadio);
        gameModeGroup.add(aiVsAIRadio);
        
        leftPanel.add(playerVsPlayerRadio);
        leftPanel.add(playerVsAIRadio);
        leftPanel.add(aiVsAIRadio);
        
        // 玩家颜色选择
        leftPanel.add(new JLabel("颜色:"));
        playerColorComboBox = new JComboBox<>(GameConfig.PLAYER_COLORS);
        playerColorComboBox.setPreferredSize(new Dimension(60, 25));
        playerColorComboBox.setFont(GameConfig.DEFAULT_FONT);
        playerColorComboBox.addActionListener(e -> updatePlayerColor());
        leftPanel.add(playerColorComboBox);

        // AI类型选择
        leftPanel.add(new JLabel("AI:"));
        aiTypeComboBox = new JComboBox<>(GameConfig.AI_TYPES);
        aiTypeComboBox.setSelectedIndex(2); // 默认选择神经网络AI (GomokuZero)
        aiTypeComboBox.setPreferredSize(new Dimension(100, 25));
        aiTypeComboBox.setFont(GameConfig.DEFAULT_FONT);
        aiTypeComboBox.addActionListener(e -> updateModelComboBox());
        leftPanel.add(aiTypeComboBox);

        // AI难度选择
        leftPanel.add(new JLabel("难度:"));
        difficultyComboBox = new JComboBox<>(GameConfig.DIFFICULTY_LEVELS);
        difficultyComboBox.setSelectedIndex(2); // 默认困难难度
        difficultyComboBox.setPreferredSize(new Dimension(60, 25));
        difficultyComboBox.setFont(GameConfig.DEFAULT_FONT);
        leftPanel.add(difficultyComboBox);
        
        // 模型选择
        leftPanel.add(new JLabel("模型:"));
        // 动态获取ollama模型列表
        try {
            List<String> availableModels = OllamaModelManager.getAvailableModels();
            modelComboBox = new JComboBox<>(availableModels.toArray(new String[0]));
            if (!availableModels.isEmpty()) {
                modelComboBox.setSelectedIndex(0); // 默认选择第一个模型
            }
            ExceptionHandler.logInfo("成功加载 " + availableModels.size() + " 个AI模型", "五子棋界面");
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "加载AI模型列表", false);
            modelComboBox = new JComboBox<>(GameConfig.DEFAULT_MODELS);
            modelComboBox.setSelectedIndex(0);
        }
        modelComboBox.setPreferredSize(new Dimension(200, 25));
        modelComboBox.setFont(GameConfig.DEFAULT_FONT);
        leftPanel.add(modelComboBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：控制按钮（使用两行布局）
        JPanel rightPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 第一行按钮
        // 启动游戏按钮
        startButton = new JButton("启动游戏");
        startButton.setFont(GameConfig.BUTTON_FONT);
        startButton.setPreferredSize(new Dimension(80, 30));
        startButton.addActionListener(e -> startGame());
        styleButton(startButton);
        rightPanel.add(startButton);
        
        // 暂停游戏按钮
        pauseButton = new JButton("暂停游戏");
        pauseButton.setFont(GameConfig.BUTTON_FONT);
        pauseButton.setPreferredSize(new Dimension(80, 30));
        pauseButton.addActionListener(e -> pauseGame());
        pauseButton.setEnabled(false); // 初始状态禁用
        styleButton(pauseButton);
        rightPanel.add(pauseButton);
        
        // 启用/禁用AI按钮
        aiToggleButton = new JButton("启用AI");
        aiToggleButton.setFont(GameConfig.BUTTON_FONT);
        aiToggleButton.setPreferredSize(new Dimension(80, 30));
        aiToggleButton.addActionListener(e -> toggleAI());
        styleButton(aiToggleButton);
        rightPanel.add(aiToggleButton);
        
        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.setFont(GameConfig.BUTTON_FONT);
        undoButton.setPreferredSize(new Dimension(80, 30));
        undoButton.addActionListener(e -> boardPanel.undoLastMove());
        styleButton(undoButton);
        rightPanel.add(undoButton);
        
        // 第二行按钮
        // 重新开始按钮
        JButton restartButton = new JButton("重新开始");
        restartButton.setFont(GameConfig.BUTTON_FONT);
        restartButton.setPreferredSize(new Dimension(80, 30));
        restartButton.addActionListener(e -> restartGame());
        styleButton(restartButton);
        rightPanel.add(restartButton);
        
        // 退出游戏按钮
        JButton exitButton = new JButton("退出游戏");
        exitButton.setFont(GameConfig.BUTTON_FONT);
        exitButton.setPreferredSize(new Dimension(80, 30));
        exitButton.addActionListener(e -> exitGame());
        styleButton(exitButton);
        rightPanel.add(exitButton);
        
        // 返回选择按钮
        JButton backButton = new JButton("返回选择");
        backButton.setFont(GameConfig.BUTTON_FONT);
        backButton.setPreferredSize(new Dimension(80, 30));
        backButton.addActionListener(e -> returnToSelection());
        styleButton(backButton);
        rightPanel.add(backButton);
        
        // 占位按钮（保持布局对齐）
        JLabel spacer = new JLabel("");
        rightPanel.add(spacer);
        
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
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("📊 棋局状态统计"));
        panel.setBackground(Color.LIGHT_GRAY);
        
        // 对战双方信息
        playerInfoLabel = new JLabel("⚫：玩家   ⚪：AI");
        playerInfoLabel.setFont(GameConfig.DEFAULT_FONT);
        playerInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 手数统计
        moveCountLabel = new JLabel("手数：0");
        moveCountLabel.setFont(GameConfig.DEFAULT_FONT);
        moveCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 优势分析
        advantageLabel = new JLabel("优势：均势");
        advantageLabel.setFont(GameConfig.DEFAULT_FONT);
        advantageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 游戏状态
        gameStatsLabel = new JLabel("状态：等待开始");
        gameStatsLabel.setFont(GameConfig.DEFAULT_FONT);
        gameStatsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(playerInfoLabel);
        panel.add(moveCountLabel);
        panel.add(advantageLabel);
        panel.add(gameStatsLabel);
        
        return panel;
    }
    
    /**
     * 创建右侧面板（AI日志+聊天）
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(GameConfig.CHAT_PANEL_WIDTH, GameConfig.CHAT_PANEL_HEIGHT));
        
        // AI日志面板
        JPanel aiLogPanel = new JPanel(new BorderLayout());
        aiLogPanel.setBorder(BorderFactory.createTitledBorder("🤖 AI分析日志"));
        aiLogPanel.setPreferredSize(new Dimension(GameConfig.CHAT_PANEL_WIDTH, 200));
        
        aiLogArea = new JTextArea();
        aiLogArea.setEditable(false);
        aiLogArea.setFont(GameConfig.DEFAULT_FONT);
        aiLogArea.setBackground(GameConfig.CHAT_BACKGROUND_COLOR);
        aiLogArea.setText("等待AI启用...\n");
        
        JScrollPane aiLogScrollPane = new JScrollPane(aiLogArea);
        aiLogPanel.add(aiLogScrollPane, BorderLayout.CENTER);
        
        // 聊天面板
        chatPanel.setPreferredSize(new Dimension(GameConfig.CHAT_PANEL_WIDTH, 400));
        
        panel.add(aiLogPanel, BorderLayout.NORTH);
        panel.add(chatPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 更新状态栏
     */
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus() {
        if (boardPanel != null) {
            String currentPlayer = boardPanel.getBoard().isBlackTurn() ? "黑方" : "白方";
            String modeInfo = " [" + currentGameMode + "]";
            String aiStatus = "";
            
            if ("玩家对AI".equals(currentGameMode)) {
                // 检查AI是否启用的逻辑需要根据实际情况调整
                aiStatus = " (AI已启用)";
            } else if ("AI对AI".equals(currentGameMode)) {
                aiStatus = " (AI自动对弈)";
            }
            
            statusLabel.setText("当前轮到: " + currentPlayer + modeInfo + aiStatus);
        }
    }
    
    /**
     * 更新游戏模式设置
     */
    private void updateGameModeSettings() {
        GameMode selectedMode;
        if (playerVsPlayerRadio.isSelected()) {
            currentGameMode = "玩家对玩家";
            selectedMode = GameMode.PLAYER_VS_PLAYER;
        } else if (playerVsAIRadio.isSelected()) {
            currentGameMode = "玩家对AI";
            selectedMode = GameMode.PLAYER_VS_AI;
        } else {
            currentGameMode = "AI对AI";
            selectedMode = GameMode.AI_VS_AI;
        }
        
        // 通过GameManager设置游戏模式
        String aiType = (String) aiTypeComboBox.getSelectedItem();
        String difficulty = (String) difficultyComboBox.getSelectedItem();
        String modelName = (String) modelComboBox.getSelectedItem();
        
        if (gameManager != null) {
            gameManager.setGameMode(selectedMode, aiType, difficulty, modelName);
        }
        
        // 更新UI状态
        switch (currentGameMode) {
            case "玩家对玩家":
                isAIvsAIMode = false;
                aiToggleButton.setEnabled(false);
                playerColorComboBox.setEnabled(true);
                difficultyComboBox.setEnabled(false);
                aiTypeComboBox.setEnabled(false);
                modelComboBox.setEnabled(false);
                break;
                
            case "玩家对AI":
                isAIvsAIMode = false;
                aiToggleButton.setEnabled(true);
                playerColorComboBox.setEnabled(true);
                difficultyComboBox.setEnabled(true);
                aiTypeComboBox.setEnabled(true);
                modelComboBox.setEnabled(true);
                break;
                
            case "AI对AI":
                isAIvsAIMode = true;
                aiToggleButton.setEnabled(false);
                playerColorComboBox.setEnabled(false);
                difficultyComboBox.setEnabled(true);
                aiTypeComboBox.setEnabled(true);
                modelComboBox.setEnabled(true);
                break;
        }
        
        updateStatus();
        updatePlayerInfoLabel();
    }
    
    /**
     * 初始化AI对AI模式
     */
    private void initializeAIvsAI() {
        try {
            String aiType = (String) aiTypeComboBox.getSelectedItem();
            String difficulty = (String) difficultyComboBox.getSelectedItem();
            String model = (String) modelComboBox.getSelectedItem();
            
            // 统一使用GomokuAdvancedAI
            blackAI = new GomokuAdvancedAI(difficulty);
            whiteAI = new GomokuAdvancedAI(difficulty);
            
            // 如果当前是黑方回合且是AI对AI模式，立即开始AI思考
            if (boardPanel.getBoard().isBlackTurn()) {
                executeAIvsAIMove();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "AI初始化失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 执行AI对AI移动
     */
    private void executeAIvsAIMove() {
        if (!isAIvsAIMode || boardPanel.getBoard().getGameState() != GameState.PLAYING) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                Object currentAI = boardPanel.getBoard().isBlackTurn() ? blackAI : whiteAI;
                if (currentAI != null) {
                    int[] move = null;
                    if (currentAI instanceof GomokuAdvancedAI) {
                        move = ((GomokuAdvancedAI) currentAI).getNextMove(boardPanel.getBoard());
                    }
                    
                    if (move != null && move.length == 2) {
                        // 直接在棋盘上落子
                        if (boardPanel.getBoard().placePiece(move[0], move[1])) {
                            boardPanel.repaint();
                            updateStatus();
                            
                            // 如果游戏未结束，继续下一步AI移动
                            if (boardPanel.getBoard().getGameState() == GameState.PLAYING && isAIvsAIMode) {
                                Timer timer = new Timer(1000, e -> executeAIvsAIMove());
                                timer.setRepeats(false);
                                timer.start();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 切换AI启用状态
     */
    private void toggleAI() {
        try {
            boolean newState = !aiToggleButton.getText().equals("启用AI");
            aiToggleButton.setText(newState ? "启用AI" : "禁用AI");
            
            // 更新AI设置
            if (!newState) {
                String aiType = (String) aiTypeComboBox.getSelectedItem();
                String difficulty = (String) difficultyComboBox.getSelectedItem();
                String modelName = (String) modelComboBox.getSelectedItem();
                
                if (aiType == null || difficulty == null) {
                    ExceptionHandler.logWarning("AI配置不完整，无法启用AI", "五子棋界面");
                    return;
                }
                
                // 通过GameManager设置AI配置
                // TODO: 添加GameManager的AI配置方法
                
                // 启用聊天面板（当使用大模型AI或混合AI时）
                boolean enableChat = "大模型AI".equals(aiType) || "混合AI".equals(aiType);
                if (enableChat) {
                    if (modelName != null && !modelName.trim().isEmpty()) {
                        chatPanel.setEnabled(true);
                        chatPanel.setModelName(modelName);
                        ExceptionHandler.logInfo("启用AI聊天功能，模型: " + modelName, "五子棋界面");
                    } else {
                        ExceptionHandler.logWarning("未选择有效模型，无法启用聊天功能", "五子棋界面");
                        chatPanel.setEnabled(false);
                    }
                } else {
                    chatPanel.setEnabled(false);
                }
                
                ExceptionHandler.logInfo("AI已启用 - 类型: " + aiType + ", 难度: " + difficulty, "五子棋界面");
            } else {
                // 禁用聊天面板
                chatPanel.setEnabled(false);
                ExceptionHandler.logInfo("AI已禁用", "五子棋界面");
            }
            
            // AI状态现在由GameManager管理
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "切换AI状态", true);
        }
    }
    
    /**
     * 更新模型选择框状态
     */
    private void updateModelComboBox() {
        String aiType = (String) aiTypeComboBox.getSelectedItem();
        boolean isLLMEnabled = "大模型AI".equals(aiType) || "混合AI".equals(aiType);
        modelComboBox.setEnabled(isLLMEnabled);
        
        // 如果AI已启用，更新AI类型
        if (aiToggleButton.getText().equals("禁用AI")) {
            String difficulty = (String) difficultyComboBox.getSelectedItem();
            String modelName = (String) modelComboBox.getSelectedItem();
            // 通过GameManager设置AI配置
            // TODO: 添加GameManager的setAIConfiguration方法
            
            // 更新聊天面板状态
            boolean enableChat = "大模型AI".equals(aiType) || "混合AI".equals(aiType);
            if (enableChat) {
                chatPanel.setEnabled(true);
                chatPanel.setModelName(modelName);
            } else {
                chatPanel.setEnabled(false);
            }
        }
    }
    
    /**
     * 更新玩家颜色
     */
    private void updatePlayerColor() {
        boolean isPlayerBlack = playerColorComboBox.getSelectedIndex() == 0;
        // 通过GameManager设置玩家颜色
        if (gameManager != null) {
            gameManager.setPlayerColor(isPlayerBlack);
        }
    }
    
    /**
     * 重新开始游戏
     */
    private void restartGame() {
        boardPanel.resetGame();
        
        // 根据当前游戏模式重新初始化
        if ("AI对AI".equals(currentGameMode)) {
            initializeAIvsAI();
        }
        
        // 重置游戏后更新ChatPanel的五子棋棋盘引用
        chatPanel.setGomokuBoard(boardPanel.getBoard());
        updateStatus();
        System.out.println("🔄 五子棋游戏已重新开始 - 模式: " + currentGameMode);
    }
    
    /**
     * 返回游戏选择界面
     */
    private void returnToSelection() {
        try {
            // 停止AI对AI模式
            if (isAIvsAIMode) {
                // 通过GameManager禁用AI
                if (gameManager != null) {
                    gameManager.setGameMode(GomokuGameManager.GameMode.PLAYER_VS_PLAYER);
                }
                isAIvsAIMode = false;
            }
            
            // 清理资源
            if (chatPanel != null) {
                chatPanel.setEnabled(false);
            }
            
            // 关闭当前窗口
            dispose();
            
            // TODO: 添加返回游戏选择界面的逻辑
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
            ExceptionHandler.logInfo("正在关闭五子棋游戏窗口...", "五子棋界面");
            
            // 禁用AI和聊天功能
            if (boardPanel != null) {
                // 通过GameManager禁用AI
                if (gameManager != null) {
                    gameManager.setGameMode(GomokuGameManager.GameMode.PLAYER_VS_PLAYER);
                }
            }
            if (chatPanel != null) {
                chatPanel.setEnabled(false);
            }
            
            // 关闭窗口
            dispose();
            
            // 如果这是最后一个窗口，退出应用程序
            if (Window.getWindows().length <= 1) {
                ExceptionHandler.logInfo("应用程序即将退出，正在清理资源...", "五子棋界面");
                System.exit(0);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "关闭窗口", false);
            System.exit(1);
        }
    }
    
    /**
     * 初始化默认AI
     */
    private void initializeDefaultAI() {
        // 设置默认AI类型为大模型AI
        String aiType = "大模型AI";
        String difficulty = (String) difficultyComboBox.getSelectedItem();
        String modelName = (String) modelComboBox.getSelectedItem(); // 使用实际选中的模型
        
        // 通过GameManager设置AI配置
        if (gameManager != null) {
            // TODO: 添加GameManager的setAIConfiguration方法
            gameManager.setGameMode(GomokuGameManager.GameMode.PLAYER_VS_AI);
        }
        aiToggleButton.setText("禁用AI");
        
        // 启用聊天面板（因为默认使用大模型AI）
        chatPanel.setEnabled(true);
        chatPanel.setModelName(modelName);
        
        // 更新状态栏
        statusLabel.setText("⚫ 当前玩家: 黑方 (AI已启用)");
    }
    
    /**
     * 自动启用AI（用于命令行参数启动）
     */
    public void autoEnableAI() {
        if (aiToggleButton.getText().equals("启用AI")) {
            toggleAI();
        }
    }
    
    /**
     * 启动游戏
     */
    private void startGame() {
        if (gameManager != null) {
            gameManager.startGame();
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            
            ExceptionHandler.logInfo("游戏已启动", "五子棋界面");
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
            ExceptionHandler.logInfo("游戏已暂停", "五子棋界面");
        }
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
     * 初始化游戏管理器
     */
    private void initializeGameManager() {
        // gameManager 已在构造函数中创建，不需要重复创建
        // gameManager = new GomokuGameManager();
        
        // 将GameManager的棋盘设置给棋盘面板
        if (boardPanel != null) {
            boardPanel.setBoard(gameManager.getBoard());
        }
        
        // 设置游戏回调
        gameManager.setGameCallback(new GomokuGameManager.GameCallback() {
            @Override
            public void onGameStateChanged(GameState newState, String winner) {
                SwingUtilities.invokeLater(() -> {
                    String status;
                    switch (newState) {
                        case BLACK_WINS:
                            status = "⚫ 黑方获胜！";
                            break;
                        case RED_WINS: // 在五子棋中代表白方
                            status = "⚪ 白方获胜！";
                            break;
                        case DRAW:
                            status = "🤝 和棋！";
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
                    }
                });
            }
            
            @Override
            public void onTurnChanged(boolean isBlackTurn, PlayerType currentPlayerType) {
                SwingUtilities.invokeLater(() -> {
                    String currentPlayer = isBlackTurn ? "黑方" : "白方";
                    String playerType = currentPlayerType == PlayerType.HUMAN ? "玩家" : "AI";
                    String aiStatus = gameManager.isAIThinking() ? " (AI思考中...)" : "";
                    
                    String status = String.format("轮到: %s%s [%s]%s", 
                                                 isBlackTurn ? "⚫" : "⚪", 
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
            public void onAIMove(int row, int col, String analysis) {
                SwingUtilities.invokeLater(() -> {
                    if (aiLogArea != null) {
                        String moveStr = String.format("🎯 AI落子: (%d, %d) - %s\n", row, col, analysis);
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
            public void onGameModeChanged(GameMode newMode) {
                SwingUtilities.invokeLater(() -> {
                    // 更新UI显示
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
                    JOptionPane.showMessageDialog(GomokuFrame.this, 
                                                error, 
                                                "游戏错误", 
                                                JOptionPane.ERROR_MESSAGE);
                    
                    if (aiLogArea != null) {
                        aiLogArea.append("❌ 错误: " + error + "\n");
                        aiLogArea.setCaretPosition(aiLogArea.getDocument().getLength());
                    }
                });
            }
        });
        
        System.out.println("🎮 游戏管理器初始化完成");
    }
    
    /**
     * 更新游戏统计信息
     */
    private void updateGameStats() {
        if (gameManager != null) {
            String stats = gameManager.getGameStats();
            
            // 更新手数
            int moveCount = 0;
            if (gameManager.getBoard() != null) {
                for (int row = 0; row < gameManager.getBoard().getBoardSize(); row++) {
                    for (int col = 0; col < gameManager.getBoard().getBoardSize(); col++) {
                        if (gameManager.getBoard().getPiece(row, col) != ' ') {
                            moveCount++;
                        }
                    }
                }
            }
            
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
            
            // 优势分析（简化实现）
            if (moveCount < 5) {
                advantageLabel.setText("优势：开局阶段");
            } else if (gameManager.getBoard() != null && gameManager.getBoard().getGameState() != GameState.PLAYING) {
                GameState state = gameManager.getBoard().getGameState();
                if (state == GameState.BLACK_WINS) {
                    advantageLabel.setText("优势：黑方胜利");
                } else if (state == GameState.RED_WINS) {
                    advantageLabel.setText("优势：白方胜利");
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
            GameMode mode = gameManager.getCurrentMode();
            PlayerType blackPlayer = gameManager.getBlackPlayer();
            PlayerType whitePlayer = gameManager.getWhitePlayer();
            
            String blackInfo = blackPlayer == PlayerType.HUMAN ? "玩家" : "AI";
            String whiteInfo = whitePlayer == PlayerType.HUMAN ? "玩家" : "AI";
            
            playerInfoLabel.setText(String.format("⚫：%s   ⚪：%s", blackInfo, whiteInfo));
        }
    }
    
    /**
     * 主方法，启动五子棋游戏
     */
    public static void main(String[] args) {
        try {
            // 设置系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "设置系统外观", false);
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                ExceptionHandler.logInfo("正在启动五子棋游戏...", "主程序");
                new GomokuFrame().setVisible(true);
                ExceptionHandler.logInfo("五子棋游戏启动成功", "主程序");
            } catch (Exception e) {
                ExceptionHandler.handleException(e, "启动五子棋游戏", true);
                System.exit(1);
            }
        });
    }
}