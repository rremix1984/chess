package com.example.go;

import com.example.common.config.ConfigurationManager;
import com.example.common.utils.ExceptionHandler;
import com.example.common.utils.OllamaModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.example.common.ui.BoardWithFloatButton;
import com.example.common.ui.FullscreenToggler;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 围棋游戏主界面 - 专业版
 */
public class GoFrame extends JFrame {
    private GoBoardPanel boardPanel;
    private BoardWithFloatButton boardContainer;
    private GoAILogPanel aiLogPanel;
    private GoChatPanel chatPanel;
    private JLabel statusLabel;
    private JLabel captureLabel;
    
    // 棋局状态统计信息
    private JLabel gameStatsLabel;
    private JLabel playerInfoLabel;
    private JLabel moveCountLabel;
    private JLabel advantageLabel;
    
    // 游戏模式单选按钮
    private JRadioButton playerVsAIRadio;
    private JRadioButton aiVsAIRadio;
    private JRadioButton playerVsPlayerRadio;
    private ButtonGroup gameModeGroup;
    
    // 游戏控制按钮
    private JButton startGameButton;
    private JButton pauseGameButton;
    private JButton restartButton;
    private JButton undoButton;
    private JButton passButton;
    
    // AI设置
    private JComboBox<String> aiPlayerCombo;
    private JComboBox<String> difficultyCombo;
    private JComboBox<String> aiTypeCombo;
    private JComboBox<String> modelCombo;
    
    // 显示设置
    private JCheckBox coordinatesCheckBox;
    
    // AI引擎
    private KataGoAI katagoAI;
    private GoAI fallbackAI;
    
    // 配置管理
    private ConfigurationManager config;
    
    // 游戏状态
    private boolean isGameRunning = false;
    private boolean isGamePaused = false;
    private GameMode currentGameMode = GameMode.PLAYER_VS_AI;

    private JPanel topControlPanel;
    private JTabbedPane rightTabbedPane;
    private FullscreenToggler fullscreenToggler;
    
    // 游戏模式枚举
    public enum GameMode {
        PLAYER_VS_AI,    // 玩家对AI
        AI_VS_AI,        // AI对AI
        PLAYER_VS_PLAYER // 玩家对玩家
    }
    
    public GoFrame() {
        // 初始化配置
        config = ConfigurationManager.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeAI();
        
        setTitle("🏮 围棋对弈 - 专业版");
        setSize(1400, 1000);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToSelection();
            }
        });

        ExceptionHandler.logInfo("围棋游戏", "🚀 专业级围棋游戏启动完成");
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 创建棋盘面板
        boardPanel = new GoBoardPanel();
        boardPanel.setGameStateCallback(new GoBoardPanel.GameStateCallback() {
            @Override
            public void onGameStateChanged(String status) {
                statusLabel.setText("状态: " + status);
            }
            
            @Override
            public void onMoveCount(int blackCaptured, int whiteCaptured) {
                captureLabel.setText(String.format("被吃棋子 - 黑: %d, 白: %d", 
                                                  blackCaptured, whiteCaptured));
            }
            
            @Override
            public void onTitleUpdateNeeded() {
                updateWindowTitle();
            }
            
            @Override
            public void onGameStatsUpdate() {
                updateGameStats();
            }
        });
        
        // 创建AI日志面板
        aiLogPanel = new GoAILogPanel();
        
        // 设置AI日志面板到棋盘面板
        boardPanel.setAILogPanel(aiLogPanel);
        
        // 创建聊天面板
        chatPanel = new GoChatPanel();
        
        // 设置聊天面板的棋盘引用，以便显示视觉标记
        chatPanel.setBoardPanel(boardPanel);
        chatPanel.setCurrentGame(boardPanel.getGame());
        
        // 状态标签
        statusLabel = new JLabel("状态: 黑棋 回合");
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        // 被吃棋子标签
        captureLabel = new JLabel("被吃棋子 - 黑: 0, 白: 0");
        captureLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 棋局状态统计标签
        gameStatsLabel = new JLabel("棋局状态统计");
        gameStatsLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        
        playerInfoLabel = new JLabel("●：玩家   ○：AI");
        playerInfoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        
        moveCountLabel = new JLabel("手数: 0");
        moveCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        
        advantageLabel = new JLabel("当前: 黑棋回合");
        advantageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        
        // 游戏模式单选按钮
        gameModeGroup = new ButtonGroup();
        playerVsAIRadio = new JRadioButton("玩家对AI", true);
        aiVsAIRadio = new JRadioButton("AI对AI");
        playerVsPlayerRadio = new JRadioButton("玩家对玩家");
        
        gameModeGroup.add(playerVsAIRadio);
        gameModeGroup.add(aiVsAIRadio);
        gameModeGroup.add(playerVsPlayerRadio);
        
        // 游戏控制按钮
        startGameButton = new JButton("启动游戏");
        pauseGameButton = new JButton("暂停游戏");
        pauseGameButton.setEnabled(false);
        
        restartButton = new JButton("重新开始");
        undoButton = new JButton("悔棋");
        passButton = new JButton("弃权");
        
        // AI设置
        aiPlayerCombo = new JComboBox<>(new String[]{"白棋", "黑棋"});
        difficultyCombo = new JComboBox<>(new String[]{
            "简单", "容易", "中等", "困难", "专家"
        });
        difficultyCombo.setSelectedIndex(2); // 默认中等难度
        
        aiTypeCombo = new JComboBox<>(new String[]{
            "KataGo AI", "传统AI", "混合AI"
        });
        
        // 模型选择
        List<String> availableModels = OllamaModelManager.getAvailableModels();
        modelCombo = new JComboBox<>(availableModels.toArray(new String[0]));
        if (!availableModels.isEmpty()) {
            modelCombo.setSelectedIndex(0);
        }
        
        // 显示设置
        coordinatesCheckBox = new JCheckBox("显示坐标", true);
        
        // 设置按钮样式
        styleButton(startGameButton, new Color(76, 175, 80));
        styleButton(pauseGameButton, new Color(255, 152, 0));
        styleButton(restartButton, new Color(33, 150, 243));
        styleButton(undoButton, new Color(255, 193, 7));
        styleButton(passButton, new Color(96, 125, 139));
    }
    
    /**
     * 设置按钮样式
     */
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setFocusPainted(false);
        
        // 设置醒目的边框
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        button.setPreferredSize(new Dimension(90, 35));
        button.setOpaque(true);
        
        // 添加鼠标交互效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = color;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.brighter());
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
                button.setCursor(Cursor.getDefaultCursor());
                // 恢复正常边框
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // 按下时的效果：更暗的颜色和凹陷边框
                button.setBackground(originalColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLoweredBevelBorder(),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                // 释放时恢复悬停效果
                if (button.contains(evt.getPoint())) {
                    button.setBackground(originalColor.brighter());
                } else {
                    button.setBackground(originalColor);
                }
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 创建顶部控制面板
        topControlPanel = createTopControlPanel();
        add(topControlPanel, BorderLayout.NORTH);
        
        // 创建主面板（棋盘+右侧面板）
        JPanel mainPanel = new JPanel(new BorderLayout());
        boardContainer = new BoardWithFloatButton(boardPanel);
        mainPanel.add(boardContainer, BorderLayout.CENTER);
        
        // 创建右侧面板（AI日志+聊天）
        rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("AI分析", aiLogPanel);
        rightTabbedPane.addTab("与AI对话", chatPanel);
        rightTabbedPane.setPreferredSize(new Dimension(300, 600));

        // 设置标签页字体颜色为黑色
        rightTabbedPane.setForeground(Color.BLACK);
        rightTabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        mainPanel.add(rightTabbedPane, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);
        
        // 状态栏
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        fullscreenToggler = new FullscreenToggler(this, topControlPanel, rightTabbedPane);
        boardContainer.getButton().addActionListener(e -> {
            fullscreenToggler.toggle();
            boardContainer.getButton().setTextLabel(fullscreenToggler.isFullscreen() ? "取消全屏 ✕" : "全屏 ⛶");
        });
    }
    
    /**
     * 创建顶部控制面板
     */
    private JPanel createTopControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🎮 围棋对弈控制"));
        panel.setPreferredSize(new Dimension(1400, 160)); // 增加高度以容纳状态统计
        
        // 左侧：基本设置
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // AI颜色选择
        leftPanel.add(new JLabel("AI执子:"));
        aiPlayerCombo.setPreferredSize(new Dimension(80, 25));
        leftPanel.add(aiPlayerCombo);
        
        // AI类型选择
        leftPanel.add(new JLabel("AI类型:"));
        aiTypeCombo.setPreferredSize(new Dimension(120, 25));
        leftPanel.add(aiTypeCombo);
        
        // AI难度选择
        leftPanel.add(new JLabel("难度:"));
        difficultyCombo.setPreferredSize(new Dimension(80, 25));
        leftPanel.add(difficultyCombo);
        
        // 模型选择
        leftPanel.add(new JLabel("模型:"));
        modelCombo.setPreferredSize(new Dimension(150, 25));
        leftPanel.add(modelCombo);
        
        // 显示设置
        leftPanel.add(new JLabel(" | "));
        leftPanel.add(coordinatesCheckBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);
        
        // 右侧：游戏模式和控制按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        
        // 游戏模式选择面板
        JPanel gameModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        gameModePanel.setPreferredSize(new Dimension(300, 35));
        gameModePanel.setBorder(BorderFactory.createTitledBorder("对弈模式"));
        
        playerVsAIRadio.setToolTipText("玩家与AI对弈");
        aiVsAIRadio.setToolTipText("AI对AI对弈，AI同时操控黑白双方");
        playerVsPlayerRadio.setToolTipText("玩家对玩家对弈，无AI参与");
        
        gameModePanel.add(playerVsAIRadio);
        gameModePanel.add(aiVsAIRadio);
        gameModePanel.add(playerVsPlayerRadio);
        
        rightPanel.add(gameModePanel);
        
        // 游戏控制面板
        JPanel gameControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        gameControlPanel.setPreferredSize(new Dimension(200, 35));
        
        startGameButton.setPreferredSize(new Dimension(80, 30));
        startGameButton.setToolTipText("启动选定模式的游戏");
        gameControlPanel.add(startGameButton);
        
        pauseGameButton.setPreferredSize(new Dimension(80, 30));
        pauseGameButton.setToolTipText("暂停当前游戏并保存棋局");
        gameControlPanel.add(pauseGameButton);
        
        rightPanel.add(gameControlPanel);
        
        // 操作按钮
        restartButton.setPreferredSize(new Dimension(80, 30));
        rightPanel.add(restartButton);
        
        undoButton.setPreferredSize(new Dimension(60, 30));
        rightPanel.add(undoButton);
        
        passButton.setPreferredSize(new Dimension(60, 30));
        rightPanel.add(passButton);
        
        // 返回按钮
        JButton returnButton = new JButton("⬅️");
        returnButton.setToolTipText("返回主菜单");
        returnButton.setPreferredSize(new Dimension(40, 30));
        returnButton.addActionListener(e -> returnToSelection());
        styleButton(returnButton, new Color(158, 158, 158));
        rightPanel.add(returnButton);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        // 底部：棋局状态统计信息栏
        JPanel statsPanel = createGameStatsPanel();
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建棋局状态统计面板
     */
    private JPanel createGameStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📊 棋局状态统计"));
        panel.setPreferredSize(new Dimension(1400, 40));
        
        // 左侧：对战双方信息
        JPanel leftStatsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftStatsPanel.add(playerInfoLabel);
        panel.add(leftStatsPanel, BorderLayout.WEST);
        
        // 中间：手数统计
        JPanel centerStatsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        centerStatsPanel.add(moveCountLabel);
        panel.add(centerStatsPanel, BorderLayout.CENTER);
        
        // 右侧：优势分析
        JPanel rightStatsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightStatsPanel.add(advantageLabel);
        panel.add(rightStatsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 游戏模式选择
        playerVsAIRadio.addActionListener(e -> selectGameMode(GameMode.PLAYER_VS_AI));
        aiVsAIRadio.addActionListener(e -> selectGameMode(GameMode.AI_VS_AI));
        playerVsPlayerRadio.addActionListener(e -> selectGameMode(GameMode.PLAYER_VS_PLAYER));
        
        // 游戏控制按钮
        startGameButton.addActionListener(e -> startGame());
        pauseGameButton.addActionListener(e -> pauseGame());
        
        // 重新开始按钮
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    GoFrame.this,
                    "确定要重新开始游戏吗？",
                    "确认",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    boardPanel.restartGame();
                }
            }
        });
        
        // 悔棋按钮
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardPanel.undoMove();
            }
        });
        
        // 弃权按钮
        passButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardPanel.pass();
            }
        });
        
        // AI设置
        aiPlayerCombo.addActionListener(e -> updateAISettings());
        difficultyCombo.addActionListener(e -> updateAISettings());
        aiTypeCombo.addActionListener(e -> updateModelComboBox());
        modelCombo.addActionListener(e -> updateAISettings());
        
        // 坐标显示设置
        coordinatesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardPanel.setShowCoordinates(coordinatesCheckBox.isSelected());
            }
        });
    }
    
    /**
     * 选择游戏模式
     */
    private void selectGameMode(GameMode mode) {
        currentGameMode = mode;
        updateGameModeUI();
    }
    
    /**
     * 更新游戏模式UI
     */
    private void updateGameModeUI() {
        switch (currentGameMode) {
            case PLAYER_VS_AI:
                aiPlayerCombo.setEnabled(true);
                difficultyCombo.setEnabled(true);
                aiTypeCombo.setEnabled(true);
                updateModelComboBox();
                chatPanel.setEnabled(true);
                aiLogPanel.setEnabled(true);
                break;
            case AI_VS_AI:
                aiPlayerCombo.setEnabled(false);
                difficultyCombo.setEnabled(true);
                aiTypeCombo.setEnabled(true);
                updateModelComboBox();
                chatPanel.setEnabled(true);
                aiLogPanel.setEnabled(true);
                break;
            case PLAYER_VS_PLAYER:
                aiPlayerCombo.setEnabled(false);
                difficultyCombo.setEnabled(false);
                aiTypeCombo.setEnabled(false);
                modelCombo.setEnabled(false);
                chatPanel.setEnabled(false);
                aiLogPanel.setEnabled(false);
                break;
        }
    }
    
    /**
     * 更新模型选择框状态
     */
    private void updateModelComboBox() {
        int aiTypeIndex = aiTypeCombo.getSelectedIndex();
        boolean needsModel = (aiTypeIndex == 2); // 混合AI
        modelCombo.setEnabled(needsModel && currentGameMode != GameMode.PLAYER_VS_PLAYER);
        
        if (needsModel && currentGameMode != GameMode.PLAYER_VS_PLAYER) {
            modelCombo.setBackground(Color.WHITE);
        } else {
            modelCombo.setBackground(Color.LIGHT_GRAY);
        }
    }
    
    /**
     * 启动游戏
     */
    private void startGame() {
        if (!isGameRunning) {
            isGameRunning = true;
            isGamePaused = false;
            startGameButton.setText("停止游戏");
            pauseGameButton.setEnabled(true);
            
            // 根据选定的模式启动游戏
            switch (currentGameMode) {
                case PLAYER_VS_AI:
                    startPlayerVsAI();
                    break;
                case AI_VS_AI:
                    startAIVsAI();
                    break;
                case PLAYER_VS_PLAYER:
                    startPlayerVsPlayer();
                    break;
            }
            
            updateStatus("游戏已启动 - " + getModeDescription());
        } else {
            stopGame();
        }
    }
    
    /**
     * 停止游戏
     */
    private void stopGame() {
        isGameRunning = false;
        isGamePaused = false;
        startGameButton.setText("启动游戏");
        pauseGameButton.setEnabled(false);
        pauseGameButton.setText("暂停游戏");
        updateStatus("游戏已停止");
    }
    
    /**
     * 暂停游戏
     */
    private void pauseGame() {
        if (isGameRunning && !isGamePaused) {
            isGamePaused = true;
            pauseGameButton.setText("继续游戏");
            updateStatus("游戏已暂停");
        } else if (isGameRunning && isGamePaused) {
            isGamePaused = false;
            pauseGameButton.setText("暂停游戏");
            updateStatus("游戏已继续");
        }
    }
    
    /**
     * 启动玩家对AI模式
     */
    private void startPlayerVsAI() {
        int aiPlayer = aiPlayerCombo.getSelectedIndex() == 0 ? GoGame.WHITE : GoGame.BLACK;
        int difficulty = difficultyCombo.getSelectedIndex() + 1;
        String aiType = (String) aiTypeCombo.getSelectedItem();
        String modelName = (String) modelCombo.getSelectedItem();
        
        // 设置AI
        setupAIEngine(aiType, aiPlayer, difficulty, modelName);
        
        // 设置聊天面板的游戏状态
        chatPanel.setCurrentGame(boardPanel.getGame());
        
        // 设置聊天面板
        if ("KataGo AI".equals(aiType) || "混合AI".equals(aiType)) {
            chatPanel.setEnabled(true);
            chatPanel.setModelName(modelName);
        }
        
        aiLogPanel.setEnabled(true);
    }
    
    /**
     * 启动AI对AI模式
     */
    private void startAIVsAI() {
        int difficulty = difficultyCombo.getSelectedIndex() + 1;
        String aiType = (String) aiTypeCombo.getSelectedItem();
        String modelName = (String) modelCombo.getSelectedItem();
        
        // 禁用单个AI模式
        boardPanel.setAIEnabled(false, GoGame.WHITE, 1);
        
        // 启用AI对AI模式
        boardPanel.enableAIvsAI(difficulty, aiType);
        
        // 启用相关面板
        chatPanel.setEnabled(true);
        aiLogPanel.setEnabled(true);
        
        System.out.println("🤖 AI对AI模式已启动 - 难度: " + difficulty + ", AI类型: " + aiType);
    }
    
    /**
     * 启动玩家对玩家模式
     */
    private void startPlayerVsPlayer() {
        // 禁用AI
        boardPanel.setAIEnabled(false, GoGame.WHITE, 1);
        chatPanel.setEnabled(false);
        aiLogPanel.setEnabled(false);
    }
    
    /**
     * 设置AI引擎
     */
    private void setupAIEngine(String aiType, int aiPlayer, int difficulty, String modelName) {
        System.out.println("🎯 设置AI引擎 - 类型: " + aiType + ", 玩家: " + (aiPlayer == GoGame.BLACK ? "黑棋" : "白棋") + ", 难度: " + difficulty);
        
        switch (aiType) {
            case "KataGo AI":
                if (katagoAI == null) {
                    katagoAI = new KataGoAI(difficulty);
                }
                if (katagoAI.initializeEngine()) {
                    System.out.println("✅ 使用KataGo AI引擎");
                    boardPanel.setKataGoAI(katagoAI);
                    boardPanel.setAIEnabled(true, aiPlayer, difficulty, true);
                    return;
                } else {
                    System.out.println("⚠️ KataGo不可用，回退到传统AI");
                }
                // 如果KataGo不可用，继续使用传统AI
            case "传统AI":
                System.out.println("⚙️ 使用传统AI");
                GoAI traditionalAI = new GoAI(aiPlayer, difficulty);
                boardPanel.setGoAI(traditionalAI);
                break;
            case "混合AI":
                // 设置混合AI（KataGo + LLM）
                // TODO: 实现混合AI
                System.out.println("🤖 混合AI暂未实现，使用传统AI");
                GoAI mixedAI = new GoAI(aiPlayer, difficulty);
                boardPanel.setGoAI(mixedAI);
                break;
        }

        boardPanel.setAIEnabled(true, aiPlayer, difficulty);
    }
    
    /**
     * 获取模式描述
     */
    private String getModeDescription() {
        switch (currentGameMode) {
            case PLAYER_VS_AI:
                return "玩家对AI";
            case AI_VS_AI:
                return "AI对AI";
            case PLAYER_VS_PLAYER:
                return "玩家对玩家";
            default:
                return "未知模式";
        }
    }
    
    /**
     * 返回游戏中心界面
     */
    private void returnToSelection() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> centerFrameClass = Class.forName("com.example.launcher.GameCenterFrame");
                JFrame centerFrame = (JFrame) centerFrameClass.getDeclaredConstructor().newInstance();
                centerFrame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.logError("GoFrame", "返回游戏中心失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus(String status) {
        statusLabel.setText("状态: " + status);
    }
    
    /**
     * 更新窗口标题以显示当前下棋方
     */
    public void updateWindowTitle() {
        if (boardPanel != null && boardPanel.getGame() != null) {
            String currentPlayerName = (boardPanel.getGame().getCurrentPlayer() == GoGame.BLACK) ? "黑棋" : "白棋";
            String modeDescription = getModeDescription();
            
            if (boardPanel.getGame().isGameEnded()) {
                setTitle("🏆 围棋对弈 - 游戏结束 - " + modeDescription);
            } else if (boardPanel.isAIvsAIMode()) {
                setTitle("🤖 围棋对弈 - AI对AI - 轮到" + currentPlayerName + " - " + modeDescription);
            } else {
                setTitle("🏆 围棋对弈 - 轮到" + currentPlayerName + " - " + modeDescription);
            }
        } else {
            setTitle("🏆 围棋对弈 - 专业版");
        }
    }
    
    /**
     * 更新棋局状态统计
     */
    private void updateGameStats() {
        if (boardPanel == null || boardPanel.getGame() == null) {
            return;
        }
        
        GoGame game = boardPanel.getGame();
        
        // 更新手数统计
        int totalMoves = game.getMoveHistory().size();
        moveCountLabel.setText("手数: " + totalMoves);
        
        // 更新当前回合
        if (game.isGameEnded()) {
            GoGame.GoGameResult result = game.calculateGameResult();
            if (result != null) {
                advantageLabel.setText("结果: " + result.getResultDescription());
            } else {
                advantageLabel.setText("游戏结束");
            }
        } else {
            String currentPlayer = (game.getCurrentPlayer() == GoGame.BLACK) ? "黑棋" : "白棋";
            advantageLabel.setText("当前: " + currentPlayer + "回合");
        }
        
        // 更新玩家信息（根据游戏模式）
        switch (currentGameMode) {
            case PLAYER_VS_AI:
                int aiPlayer = aiPlayerCombo.getSelectedIndex() == 0 ? GoGame.WHITE : GoGame.BLACK;
                if (aiPlayer == GoGame.BLACK) {
                    playerInfoLabel.setText("●：AI   ○：玩家");
                } else {
                    playerInfoLabel.setText("●：玩家   ○：AI");
                }
                break;
            case AI_VS_AI:
                playerInfoLabel.setText("●：AI   ○：AI");
                break;
            case PLAYER_VS_PLAYER:
                playerInfoLabel.setText("●：玩家   ○：玩家");
                break;
        }
    }
    
    /**
     * 初始化AI引擎
     */
    private void initializeAI() {
        try {
            // 初始化KataGo引擎
            int difficulty = difficultyCombo.getSelectedIndex() + 1;
            katagoAI = new KataGoAI(difficulty);
            
            // 在后台线程中初始化引擎，避免阻塞UI
            CompletableFuture.supplyAsync(() -> {
                return katagoAI.initializeEngine();
            }).thenAccept(success -> SwingUtilities.invokeLater(() -> {
                if (success) {
                    ExceptionHandler.logInfo("围棋游戏", "✅ KataGo引擎初始化成功");
                    // 设置棋盘面板的KataGo引擎
                    boardPanel.setKataGoAI(katagoAI);
                    // 设置聊天面板的KataGo引擎
                    chatPanel.setKataGoAI(katagoAI);
                } else {
                    ExceptionHandler.logInfo("围棋游戏", "⚠️ KataGo引擎初始化失败，使用备用AI");
                    // 初始化备用AI并设置到棋盘
                    fallbackAI = new GoAI(1, difficulty); // 1代表WHITE
                    boardPanel.setGoAI(fallbackAI);
                    // 备用AI不支持聊天功能
                    chatPanel.setKataGoAI(null);
                    chatPanel.setEnabled(false);
                }
            }));

        } catch (Exception e) {
            ExceptionHandler.logError("围棋游戏", "AI初始化失败: " + e.getMessage());
            // 使用备用AI并设置到棋盘
            int difficulty = difficultyCombo.getSelectedIndex() + 1;
            fallbackAI = new GoAI(1, difficulty);
            boardPanel.setGoAI(fallbackAI);
            chatPanel.setKataGoAI(null);
            chatPanel.setEnabled(false);
        }
    }
    
    /**
     * 更新AI设置
     */
    private void updateAISettings() {
        // 根据当前游戏模式更新AI设置
        if (currentGameMode == GameMode.PLAYER_VS_PLAYER) {
            return; // 玩家对玩家模式不需要AI
        }
        
        int aiPlayer = aiPlayerCombo.getSelectedIndex() == 0 ? 1 : 2; // 1=WHITE, 2=BLACK
        int difficulty = difficultyCombo.getSelectedIndex() + 1;
        String aiType = (String) aiTypeCombo.getSelectedItem();
        String modelName = (String) modelCombo.getSelectedItem();
        
        // 设置AI引擎
        switch (aiType) {
            case "KataGo AI":
                if (katagoAI != null) {
                    // 设置KataGo参数
                    katagoAI.setDifficulty(difficulty); // 根据难度调整访问次数
                    // TODO: 实现boardPanel.setKataGoAI方法
                }
                break;
            case "传统AI":
                fallbackAI = new GoAI(aiPlayer, difficulty);
                // TODO: 实现boardPanel.setGoAI方法
                break;
            case "混合AI":
                // TODO: 实现混合AI
                break;
        }
        
        // 设置聊天面板
        if ("KataGo AI".equals(aiType) || "混合AI".equals(aiType)) {
            chatPanel.setEnabled(true);
            chatPanel.setModelName(modelName);
            if (katagoAI != null) {
                chatPanel.setKataGoAI(katagoAI);
            }
        } else {
            chatPanel.setEnabled(false);
        }
        
        // 启用AI日志面板
        aiLogPanel.setEnabled(true);
    }
    
    /**
     * 显示游戏规则
     */
    private void showGameRules() {
        String rules = "围棋游戏规则：\n\n" +
                      "1. 黑棋先行，双方轮流在棋盘交叉点上落子\n" +
                      "2. 棋子落下后不能移动，只能通过吃子移除\n" +
                      "3. 当一块棋子没有气（相邻的空点）时被吃掉\n" +
                      "4. 不能下自杀棋（除非能同时吃掉对方棋子）\n" +
                      "5. 不能重复之前的棋局状态（劫争规则）\n" +
                      "6. 双方连续弃权时游戏结束\n\n" +
                      "操作说明：\n" +
                      "• 点击交叉点落子\n" +
                      "• 点击'悔棋'撤销上一步\n" +
                      "• 点击'弃权'跳过当前回合\n" +
                      "• 可以启用AI对弈，选择难度";
        
        JOptionPane.showMessageDialog(this, rules, "围棋规则", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 游戏菜单
        JMenu gameMenu = new JMenu("游戏");
        
        JMenuItem newGameItem = new JMenuItem("新游戏");
        newGameItem.addActionListener(e -> boardPanel.restartGame());
        
        JMenuItem rulesItem = new JMenuItem("游戏规则");
        rulesItem.addActionListener(e -> showGameRules());
        
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> dispose());
        
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(rulesItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }
    
    /**
     * 启动围棋游戏
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                GoFrame frame = new GoFrame();
                frame.createMenuBar();
                frame.setVisible(true);
            }
        });
    }
}