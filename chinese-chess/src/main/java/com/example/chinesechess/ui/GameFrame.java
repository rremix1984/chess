package com.example.chinesechess.ui;

import com.example.chinesechess.core.Board;
import com.example.chinesechess.core.PieceColor;
import com.example.common.utils.OllamaModelManager;
import com.example.common.game.GameContext;
import com.example.chinesechess.VictoryAnimation;
import com.example.chinesechess.ui.AILogPanel;
import com.example.chinesechess.ui.BoardPanel;
import com.example.chinesechess.ui.ChatPanel;
import com.example.chinesechess.network.NetworkClient;
// import com.example.chinesechess.network.NetworkMessageHandler;
// import com.example.chinesechess.network.protocol.*;
// 移除了棋盘监控相关的导入


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * 中国象棋游戏主界面
 */
public class GameFrame extends JFrame {

    private BoardPanel boardPanel;
    private ChatPanel chatPanel;
    private AILogPanel aiLogPanel;
    private JLabel statusLabel;
    private JComboBox<String> playerColorComboBox;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> aiTypeComboBox;
    private JComboBox<String> modelComboBox;

    private static final int FIXED_AI_WIDTH = 320;
    
    // 对弈模式按钮
    private JRadioButton playerVsAIRadio;
    private JRadioButton aiVsAIRadio;
    private JRadioButton playerVsPlayerRadio;
    private ButtonGroup gameModeGroup;
    
    // 游戏状态管理
    private boolean isGameRunning = false;
    private boolean isGamePaused = false;
    private JButton startGameButton;
    private JButton pauseGameButton;
    
    // 游戏模式枚举
    public enum GameMode {
        PLAYER_VS_AI,    // 玩家对AI
        AI_VS_AI,        // AI对AI
        PLAYER_VS_PLAYER // 玩家对玩家
    }
    
    private GameMode currentGameMode = GameMode.PLAYER_VS_AI; // 默认模式
    
    // 残局功能相关组件
    private JButton endgameButton;
    private JComboBox<String> endgameAIColorComboBox;
    private JComboBox<String> endgameFirstMoveComboBox; // 谁先手选择
    private JComboBox<String> endgamePlayerModeComboBox; // 玩家模式选择
    private JButton startEndgameButton;
    private JButton aiVsAiEndgameButton;
    private boolean isInEndgameSetup = false;
    
    // AI对AI配置面板相关组件
    private JPanel aiVsAiConfigPanel;
    private JComboBox<String> redAIDifficultyComboBox;
    private JComboBox<String> redAIModelComboBox;
    private JComboBox<String> redAIEngineComboBox;
    private JComboBox<String> blackAIDifficultyComboBox;
    private JComboBox<String> blackAIModelComboBox;
    private JComboBox<String> blackAIEngineComboBox;
    private boolean isAiVsAiConfigVisible = false;
    
    // 神经网络文件选择器相关组件
    private JLabel neuralNetworkLabel;
    private JComboBox<String> neuralNetworkComboBox;  // 替换原有的文本框和浏览按钮
    private JTextField neuralNetworkPathField;  // 保留用于自定义文件路径
    private JButton neuralNetworkBrowseButton;  // 保留用于浏览自定义文件
    private JLabel redNeuralNetworkLabel;
    private JComboBox<String> redNeuralNetworkComboBox;
    private JTextField redNeuralNetworkPathField;
    private JButton redNeuralNetworkBrowseButton;
    private JLabel blackNeuralNetworkLabel;
    private JComboBox<String> blackNeuralNetworkComboBox;
    private JTextField blackNeuralNetworkPathField;
    private JButton blackNeuralNetworkBrowseButton;
    
    // 已移除棋盘监控功能
    
    // 网络模式相关字段
    private boolean isNetworkMode = false;
    private NetworkClient networkClient;
    private String roomId = "";
    private String roomName = "";
    private String localPlayerName = "";
    private JLabel networkStatusLabel;

    public GameFrame() {
        setTitle("🌎 中国象棋 - AI对弈版");
        setSize(1400, 1000); // 进一步增加窗口尺寸，确保棋盘有足够空间
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 900)); // 设置最小尺寸

        // 创建棋盘
        Board board = new Board();
        boardPanel = new BoardPanel(board);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        
        // 创建聊天面板
        chatPanel = new ChatPanel();
        chatPanel.setBoard(board);
        
        // 创建AI决策日志面板
        aiLogPanel = new AILogPanel();
        
        // 设置BoardPanel的聊天面板和AI日志面板引用
        boardPanel.setChatPanel(chatPanel);
        boardPanel.setAILogPanel(aiLogPanel);
        
        // 设置ChatPanel的BoardPanel引用，以便访问AI实例
        chatPanel.setBoardPanel(boardPanel);
        
        // 设置ChatPanel的GameFrame引用，以便获取神经网络文件路径
        chatPanel.setGameFrame(this);
        
        // 创建右侧面板（聊天+AI日志）
        JTabbedPane rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("AI分析", aiLogPanel);
        rightTabbedPane.addTab("与AI对话", chatPanel);
        
        // 设置标签页字体颜色为黑色
        rightTabbedPane.setForeground(Color.BLACK);
        rightTabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        // 创建右侧面板容器
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(rightTabbedPane, BorderLayout.CENTER);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        rightPanel.setPreferredSize(new Dimension(FIXED_AI_WIDTH, 400));
        rightPanel.setMinimumSize(new Dimension(FIXED_AI_WIDTH, 400));
        rightPanel.setMaximumSize(new Dimension(FIXED_AI_WIDTH, Integer.MAX_VALUE));
        
        
        // 创建主要内容面板（棋盘+右侧面板）
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boardPanel, rightPanel);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(1.0);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerSize(6);
        splitPane.setDividerLocation(getWidth() - FIXED_AI_WIDTH);
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            int fixed = splitPane.getWidth() - FIXED_AI_WIDTH;
            if ((int)evt.getNewValue() != fixed) {
                splitPane.setDividerLocation(fixed);
            }
        });
        add(splitPane, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = splitPane.getWidth();
                splitPane.setDividerLocation(Math.max(0, w - FIXED_AI_WIDTH));
            }
        });

        // 创建控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // 创建AI对AI配置面板
        createAIvsAIConfigPanel();

        // 创建状态栏
        statusLabel = new JLabel("🔴 当前玩家: 红方", JLabel.CENTER);
        statusLabel.setFont(new Font("宋体", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel.setPreferredSize(new Dimension(1300, 30)); // 减小状态栏高度，为棋盘留出更多空间
        add(statusLabel, BorderLayout.SOUTH);

        // 设置BoardPanel的状态更新回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 默认启用大模型AI
        initializeDefaultAI();
    }
    
    /**
     * 设置按钮样式
     */
    private void styleButton(JButton button) {
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
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
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // 按下时的效果：更暗的颜色和凹陷边框
                button.setBackground(originalColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLoweredBevelBorder(),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
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
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
            }
        });
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🎮 AI对弈控制"));
        panel.setPreferredSize(new Dimension(0, 80)); // 只固定高度

        // 左侧：基本设置（紧凑布局）
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));

        // 返回按钮
        JButton backButton = new JButton("返回大厅");
        styleButton(backButton);
        backButton.addActionListener(e -> dispose());
        leftPanel.add(backButton);

        // 玩家颜色选择
        leftPanel.add(new JLabel("颜色:"));
        playerColorComboBox = new JComboBox<>(new String[]{"红方", "黑方"});
        playerColorComboBox.setPreferredSize(new Dimension(60, 25));
        leftPanel.add(playerColorComboBox);

        // AI类型选择
        leftPanel.add(new JLabel("AI:"));
        aiTypeComboBox = new JComboBox<>(new String[]{"传统AI", "增强AI", "大模型AI", "混合AI", "DeepSeek+Pikafish", "Fairy-Stockfish", "Pikafish"});
        aiTypeComboBox.setSelectedIndex(3); // 默认选择混合AI
        aiTypeComboBox.setPreferredSize(new Dimension(80, 25));
        aiTypeComboBox.addActionListener(e -> {
            updateModelComboBox();
            updateNeuralNetworkVisibility();
        });
        leftPanel.add(aiTypeComboBox);

        // AI难度选择
        leftPanel.add(new JLabel("难度:"));
        difficultyComboBox = new JComboBox<>(new String[]{"简单", "普通", "困难", "专家", "大师", "特级", "超级", "顶级", "传奇", "神级"});
        difficultyComboBox.setSelectedIndex(2); // 默认困难难度
        difficultyComboBox.setPreferredSize(new Dimension(60, 25));
        leftPanel.add(difficultyComboBox);
        
        // 模型选择
        leftPanel.add(new JLabel("模型:"));
        // 动态获取ollama模型列表
        List<String> availableModels = OllamaModelManager.getAvailableModels();
        modelComboBox = new JComboBox<>(availableModels.toArray(new String[0]));
        modelComboBox.setSelectedIndex(0); // 默认选择第一个模型
        modelComboBox.setPreferredSize(new Dimension(150, 25));
        leftPanel.add(modelComboBox);
        
        // 神经网络文件选择器（仅FairyStockfish需要） - 改为下拉菜单
        neuralNetworkLabel = new JLabel("神经网络:");
        neuralNetworkLabel.setVisible(false);
        leftPanel.add(neuralNetworkLabel);
        
        // 使用下拉菜单替代文本框和浏览按钮
        String[] nnueOptions = {
            "默认（不使用NNUE）",
            "fairy-nnue/network1.nnue",
            "fairy-nnue/network2.nnue",
            "自定义文件..."
        };
        neuralNetworkComboBox = new JComboBox<>(nnueOptions);
        neuralNetworkComboBox.setPreferredSize(new Dimension(150, 25));
        neuralNetworkComboBox.setToolTipText("选择FairyStockfish神经网络文件");
        neuralNetworkComboBox.setVisible(false);
        neuralNetworkComboBox.addActionListener(e -> handleNeuralNetworkSelection());
        leftPanel.add(neuralNetworkComboBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：控制按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        
        // 对弈模式选择面板
        JPanel gameModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        gameModePanel.setPreferredSize(new Dimension(300, 35));
        gameModePanel.setBorder(BorderFactory.createTitledBorder("对弈模式"));
        
        // 创建单选框组
        gameModeGroup = new ButtonGroup();
        
        // 玩家对AI单选框
        playerVsAIRadio = new JRadioButton("玩家对AI", true);
        playerVsAIRadio.setToolTipText("玩家与AI对弈");
        playerVsAIRadio.addActionListener(e -> selectGameMode(GameMode.PLAYER_VS_AI));
        gameModeGroup.add(playerVsAIRadio);
        gameModePanel.add(playerVsAIRadio);
        
        // AI对AI单选框
        aiVsAIRadio = new JRadioButton("AI对AI");
        aiVsAIRadio.setToolTipText("AI对AI对弈，AI同时操控红黑双方");
        aiVsAIRadio.addActionListener(e -> selectGameMode(GameMode.AI_VS_AI));
        gameModeGroup.add(aiVsAIRadio);
        gameModePanel.add(aiVsAIRadio);
        
        // 玩家对玩家单选框
        playerVsPlayerRadio = new JRadioButton("玩家对玩家");
        playerVsPlayerRadio.setToolTipText("玩家对玩家对弈，无AI参与");
        playerVsPlayerRadio.addActionListener(e -> selectGameMode(GameMode.PLAYER_VS_PLAYER));
        gameModeGroup.add(playerVsPlayerRadio);
        gameModePanel.add(playerVsPlayerRadio);
        
        rightPanel.add(gameModePanel);
        
        // 游戏控制面板
        JPanel gameControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        gameControlPanel.setPreferredSize(new Dimension(200, 35));
        
        // 启动游戏按钮
        startGameButton = new JButton("启动游戏");
        startGameButton.setPreferredSize(new Dimension(80, 30));
        startGameButton.setToolTipText("启动选定模式的游戏");
        startGameButton.addActionListener(e -> startGame());
        styleButton(startGameButton);
        gameControlPanel.add(startGameButton);
        
        // 暂停游戏按钮
        pauseGameButton = new JButton("暂停游戏");
        pauseGameButton.setPreferredSize(new Dimension(80, 30));
        pauseGameButton.setToolTipText("暂停当前游戏并保存棋局");
        pauseGameButton.setEnabled(false);
        pauseGameButton.addActionListener(e -> pauseGame());
        styleButton(pauseGameButton);
        gameControlPanel.add(pauseGameButton);
        
        rightPanel.add(gameControlPanel);

        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.setPreferredSize(new Dimension(60, 30));
        undoButton.addActionListener(e -> boardPanel.undoLastMove());
        styleButton(undoButton);
        rightPanel.add(undoButton);
        
        // 棋盘翻转按钮
        JButton flipButton = new JButton("翻转");
        flipButton.setPreferredSize(new Dimension(60, 30));
        flipButton.setToolTipText("翻转棋盘视角");
        flipButton.addActionListener(e -> boardPanel.flipBoard());
        styleButton(flipButton);
        rightPanel.add(flipButton);

        // 新游戏按钮
        JButton newGameButton = new JButton("新游戏");
        newGameButton.setPreferredSize(new Dimension(80, 30));
        newGameButton.addActionListener(e -> startNewGame());
        styleButton(newGameButton);
        rightPanel.add(newGameButton);
        
        // 残局按钮
        endgameButton = new JButton("残局");
        endgameButton.setPreferredSize(new Dimension(60, 30));
        endgameButton.setToolTipText("进入残局设置模式");
        endgameButton.addActionListener(e -> toggleEndgameSetup());
        styleButton(endgameButton);
        rightPanel.add(endgameButton);
        
        // 残局先手选择（初始隐藏）
        endgameFirstMoveComboBox = new JComboBox<>(new String[]{"红方先手", "黑方先手"});
        endgameFirstMoveComboBox.setPreferredSize(new Dimension(80, 30));
        endgameFirstMoveComboBox.setVisible(false);
        rightPanel.add(endgameFirstMoveComboBox);
        
        // 残局玩家模式选择（初始隐藏）
        endgamePlayerModeComboBox = new JComboBox<>(new String[]{"玩家对AI", "AI对AI", "玩家对玩家"});
        endgamePlayerModeComboBox.setPreferredSize(new Dimension(100, 30));
        endgamePlayerModeComboBox.setVisible(false);
        endgamePlayerModeComboBox.addActionListener(e -> updateEndgameAIOptions());
        rightPanel.add(endgamePlayerModeComboBox);
        
        // 残局AI颜色选择（初始隐藏）
        endgameAIColorComboBox = new JComboBox<>(new String[]{"AI执红", "AI执黑"});
        endgameAIColorComboBox.setPreferredSize(new Dimension(80, 30));
        endgameAIColorComboBox.setVisible(false);
        rightPanel.add(endgameAIColorComboBox);
        
        // 开始残局游戏按钮（初始隐藏）
        startEndgameButton = new JButton("开始");
        startEndgameButton.setPreferredSize(new Dimension(60, 30));
        startEndgameButton.setToolTipText("开始残局游戏");
        startEndgameButton.setVisible(false);
        startEndgameButton.addActionListener(e -> startEndgameGame());
        styleButton(startEndgameButton);
        rightPanel.add(startEndgameButton);
        
        // AI对AI残局按钮（初始隐藏）
        aiVsAiEndgameButton = new JButton("AI对AI残局");
        aiVsAiEndgameButton.setPreferredSize(new Dimension(100, 30));
        aiVsAiEndgameButton.setToolTipText("开始AI对AI残局游戏");
        aiVsAiEndgameButton.setVisible(false);
        aiVsAiEndgameButton.addActionListener(e -> startAIvsAIEndgameGame());
        styleButton(aiVsAiEndgameButton);
        rightPanel.add(aiVsAiEndgameButton);
        
        // 网络对战按钮
        JButton networkGameButton = new JButton("🌐 网络对战");
        networkGameButton.setToolTipText("启动网络对弈模式");
        networkGameButton.setPreferredSize(new Dimension(100, 30));
        networkGameButton.addActionListener(e -> startNetworkGame());
        styleButton(networkGameButton);
        if (GameContext.isSinglePlayer()) {
            networkGameButton.setVisible(false);
        }
        rightPanel.add(networkGameButton);
        
        // 返回主菜单按钮
        JButton returnButton = new JButton("⬅️");
        returnButton.setToolTipText("返回主菜单");
        returnButton.setPreferredSize(new Dimension(40, 30));
        returnButton.addActionListener(e -> returnToSelection());
        styleButton(returnButton);
        rightPanel.add(returnButton);
        
        panel.add(rightPanel, BorderLayout.EAST);

        // 初始化模型选择状态
        updateModelComboBox();

        return panel;
    }
    
    /**
     * 创建AI对AI配置面板
     */
    private void createAIvsAIConfigPanel() {
        // 创建AI对AI配置面板
        aiVsAiConfigPanel = new JPanel();
        aiVsAiConfigPanel.setBorder(BorderFactory.createTitledBorder("🤖 AI对AI配置"));
        aiVsAiConfigPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        aiVsAiConfigPanel.setPreferredSize(new Dimension(1300, 60));
        
        // 红方AI配置
        aiVsAiConfigPanel.add(new JLabel("🔴红方:"));
        
        aiVsAiConfigPanel.add(new JLabel("引擎:"));
        String[] engineOptions = {"FairyStockfish", "Pikafish"};
        redAIEngineComboBox = new JComboBox<>(engineOptions);
        redAIEngineComboBox.setSelectedIndex(0); // 默认FairyStockfish
        redAIEngineComboBox.setPreferredSize(new Dimension(100, 25));
        redAIEngineComboBox.addActionListener(e -> updateAIvsAINeuralNetworkVisibility());
        aiVsAiConfigPanel.add(redAIEngineComboBox);
        
        aiVsAiConfigPanel.add(new JLabel("难度:"));
        String[] difficultyOptions = {"简单", "普通", "困难", "专家", "大师", "特级", "超级", "顶级", "传奇", "神级"};
        redAIDifficultyComboBox = new JComboBox<>(difficultyOptions);
        redAIDifficultyComboBox.setSelectedIndex(2); // 默认困难
        redAIDifficultyComboBox.setPreferredSize(new Dimension(60, 25));
        aiVsAiConfigPanel.add(redAIDifficultyComboBox);
        
        aiVsAiConfigPanel.add(new JLabel("模型:"));
        List<String> availableModels = OllamaModelManager.getAvailableModels();
        redAIModelComboBox = new JComboBox<>(availableModels.toArray(new String[0]));
        redAIModelComboBox.setSelectedIndex(0); // 默认第一个模型
        redAIModelComboBox.setPreferredSize(new Dimension(120, 25));
        aiVsAiConfigPanel.add(redAIModelComboBox);
        
        // 分隔符
        aiVsAiConfigPanel.add(new JLabel("  |  "));
        
        // 黑方AI配置
        aiVsAiConfigPanel.add(new JLabel("⚫黑方:"));
        
        aiVsAiConfigPanel.add(new JLabel("引擎:"));
        blackAIEngineComboBox = new JComboBox<>(engineOptions);
        blackAIEngineComboBox.setSelectedIndex(1); // 默认Pikafish (与红方不同)
        blackAIEngineComboBox.setPreferredSize(new Dimension(100, 25));
        blackAIEngineComboBox.addActionListener(e -> updateAIvsAINeuralNetworkVisibility());
        aiVsAiConfigPanel.add(blackAIEngineComboBox);
        
        aiVsAiConfigPanel.add(new JLabel("难度:"));
        blackAIDifficultyComboBox = new JComboBox<>(difficultyOptions);
        blackAIDifficultyComboBox.setSelectedIndex(2); // 默认困难
        blackAIDifficultyComboBox.setPreferredSize(new Dimension(60, 25));
        aiVsAiConfigPanel.add(blackAIDifficultyComboBox);
        
        aiVsAiConfigPanel.add(new JLabel("模型:"));
        blackAIModelComboBox = new JComboBox<>(availableModels.toArray(new String[0]));
        blackAIModelComboBox.setSelectedIndex(0); // 默认第一个模型
        blackAIModelComboBox.setPreferredSize(new Dimension(120, 25));
        aiVsAiConfigPanel.add(blackAIModelComboBox);
        
        // 黑方神经网络文件选择器（仅FairyStockfish需要）- 改为下拉菜单
        blackNeuralNetworkLabel = new JLabel("神经网络:");
        blackNeuralNetworkLabel.setVisible(false); // 默认隐藏，因为默认选择Pikafish
        aiVsAiConfigPanel.add(blackNeuralNetworkLabel);
        
        // 使用下拉菜单替代文本框和浏览按钮
        String[] blackNnueOptions = {
            "默认（不使用NNUE）",
            "fairy-nnue/network1.nnue",
            "fairy-nnue/network2.nnue",
            "自定义文件..."
        };
        blackNeuralNetworkComboBox = new JComboBox<>(blackNnueOptions);
        blackNeuralNetworkComboBox.setPreferredSize(new Dimension(120, 25));
        blackNeuralNetworkComboBox.setToolTipText("选择黑方FairyStockfish神经网络文件");
        blackNeuralNetworkComboBox.setVisible(false);
        blackNeuralNetworkComboBox.addActionListener(e -> handleBlackNeuralNetworkSelection());
        aiVsAiConfigPanel.add(blackNeuralNetworkComboBox);
        
        // 保留文本框和浏览按钮用于备用（隐藏）
        blackNeuralNetworkPathField = new JTextField();
        blackNeuralNetworkPathField.setPreferredSize(new Dimension(80, 25));
        blackNeuralNetworkPathField.setEditable(false);
        blackNeuralNetworkPathField.setToolTipText("选择黑方FairyStockfish神经网络文件（可选）");
        blackNeuralNetworkPathField.setVisible(false);
        aiVsAiConfigPanel.add(blackNeuralNetworkPathField);
        
        blackNeuralNetworkBrowseButton = new JButton("浏览...");
        blackNeuralNetworkBrowseButton.setPreferredSize(new Dimension(50, 25));
        blackNeuralNetworkBrowseButton.setFont(new Font("微软雅黑", Font.PLAIN, 9));
        blackNeuralNetworkBrowseButton.setToolTipText("浏览选择黑方神经网络文件");
        blackNeuralNetworkBrowseButton.addActionListener(e -> browseBlackNeuralNetworkFile());
        blackNeuralNetworkBrowseButton.setVisible(false);
        styleButton(blackNeuralNetworkBrowseButton);
        aiVsAiConfigPanel.add(blackNeuralNetworkBrowseButton);
        
        // 红方神经网络文件选择器（仅FairyStockfish需要）- 改为下拉菜单
        redNeuralNetworkLabel = new JLabel("神经网络:");
        redNeuralNetworkLabel.setVisible(true); // 默认显示，因为默认选择FairyStockfish
        aiVsAiConfigPanel.add(redNeuralNetworkLabel);
        
        // 使用下拉菜单替代文本框和浏览按钮
        String[] redNnueOptions = {
            "默认（不使用NNUE）",
            "fairy-nnue/network1.nnue",
            "fairy-nnue/network2.nnue",
            "自定义文件..."
        };
        redNeuralNetworkComboBox = new JComboBox<>(redNnueOptions);
        redNeuralNetworkComboBox.setPreferredSize(new Dimension(120, 25));
        redNeuralNetworkComboBox.setToolTipText("选择红方FairyStockfish神经网络文件");
        redNeuralNetworkComboBox.setVisible(true);
        redNeuralNetworkComboBox.addActionListener(e -> handleRedNeuralNetworkSelection());
        aiVsAiConfigPanel.add(redNeuralNetworkComboBox);
        
        // 保留文本框和浏览按钮用于备用（隐藏）
        redNeuralNetworkPathField = new JTextField();
        redNeuralNetworkPathField.setPreferredSize(new Dimension(80, 25));
        redNeuralNetworkPathField.setEditable(false);
        redNeuralNetworkPathField.setToolTipText("选择红方FairyStockfish神经网络文件（可选）");
        redNeuralNetworkPathField.setVisible(false); // 隐藏文本框，改用下拉菜单
        aiVsAiConfigPanel.add(redNeuralNetworkPathField);
        
        redNeuralNetworkBrowseButton = new JButton("浏览...");
        redNeuralNetworkBrowseButton.setPreferredSize(new Dimension(50, 25));
        redNeuralNetworkBrowseButton.setFont(new Font("微软雅黑", Font.PLAIN, 9));
        redNeuralNetworkBrowseButton.setToolTipText("浏览选择红方神经网络文件");
        redNeuralNetworkBrowseButton.addActionListener(e -> browseRedNeuralNetworkFile());
        redNeuralNetworkBrowseButton.setVisible(false); // 隐藏浏览按钮，改用下拉菜单
        styleButton(redNeuralNetworkBrowseButton);
        aiVsAiConfigPanel.add(redNeuralNetworkBrowseButton);
        
        // 初始隐藏面板
        aiVsAiConfigPanel.setVisible(false);
        
        // 不要在这里添加到主布局，会在需要时动态添加
    }
    
    /**
     * 显示或隐藏AI对AI配置面板
     */
    private void toggleAIvsAIConfigPanel(boolean visible) {
        if (aiVsAiConfigPanel != null) {
            aiVsAiConfigPanel.setVisible(visible);
            isAiVsAiConfigVisible = visible;
            
            // 调整主面板布局
            if (visible) {
                // 移除现有的主面板
                Component[] components = getContentPane().getComponents();
                JPanel existingMainPanel = null;
                for (Component comp : components) {
                    if (comp instanceof JPanel && comp != aiVsAiConfigPanel) {
                        // 查找包含棋盘的主面板
                        Container container = (Container) comp;
                        for (Component subComp : container.getComponents()) {
                            if (subComp instanceof BoardPanel) {
                                existingMainPanel = (JPanel) comp;
                                break;
                            }
                        }
                    }
                }
                
                if (existingMainPanel != null) {
                    // 创建新的中央面板，包含AI配置面板和原有主面板
                    JPanel newCenterPanel = new JPanel(new BorderLayout());
                    newCenterPanel.add(aiVsAiConfigPanel, BorderLayout.NORTH);
                    newCenterPanel.add(existingMainPanel, BorderLayout.CENTER);
                    
                    // 移除旧的主面板并添加新的中央面板
                    remove(existingMainPanel);
                    add(newCenterPanel, BorderLayout.CENTER);
                }
            } else {
                // 隐藏时恢复原有布局
                aiVsAiConfigPanel.setVisible(false);
            }
            
            // 刷新界面
            revalidate();
            repaint();
        }
    }
    
    private void updateModelComboBox() {
        int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
        boolean needsModel = (aiTypeIndex == 2) || (aiTypeIndex == 3) || (aiTypeIndex == 4); // 大模型AI、混合AI或DeepSeek+Pikafish
        modelComboBox.setEnabled(needsModel);
        
        if (needsModel) {
            modelComboBox.setBackground(Color.WHITE);
        } else {
            modelComboBox.setBackground(Color.LIGHT_GRAY);
        }
    }
    
    /**
     * 更新残局AI选项的可见性
     */
    private void updateEndgameAIOptions() {
        String selectedMode = (String) endgamePlayerModeComboBox.getSelectedItem();
        boolean showAIOptions = "玩家对AI".equals(selectedMode);
        endgameAIColorComboBox.setVisible(showAIOptions);
        
        // 刷新界面
        revalidate();
        repaint();
    }



    private void startNewGame() {
        // 检查是否已经有棋盘面板
        if (boardPanel != null) {
            // 重置现有棋盘，而不是创建新的
            boardPanel.getBoard().initializeBoard();
            boardPanel.restartGame();
            
            // 重置游戏模式为玩家对AI
            setGameMode(GameMode.PLAYER_VS_AI);
            
            // 更新状态
            updateStatus("当前玩家: 红方");
            
            // 刷新界面
            revalidate();
            repaint();
            return;
        }
        
        // 如果没有棋盘面板（首次运行），则创建新的
        Board newBoard = new Board();
        
        // 移除旧的棋盘面板
        getContentPane().removeAll();
        
        // 创建新的棋盘面板
        boardPanel = new BoardPanel(newBoard);
        
        // 重新创建聊天面板和AI日志面板
        chatPanel = new ChatPanel();
        chatPanel.setBoard(newBoard);
        aiLogPanel = new AILogPanel();
        
        // 设置BoardPanel的聊天面板和AI日志面板引用
        boardPanel.setChatPanel(chatPanel);
        boardPanel.setAILogPanel(aiLogPanel);
        
        // 设置ChatPanel的BoardPanel引用，以便访问AI实例
        chatPanel.setBoardPanel(boardPanel);
        
        // 重新添加组件
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // 创建右侧面板（聊天+AI日志）
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // 设置聊天面板的固定高度
        chatPanel.setPreferredSize(new Dimension(350, 350));
        rightPanel.add(chatPanel, BorderLayout.NORTH);
        
        // AI日志面板占用剩余空间
        rightPanel.add(aiLogPanel, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(350, 700));
        
        // 创建主要内容面板（棋盘+右侧面板）
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);
        
        add(statusLabel, BorderLayout.SOUTH);
        
        // 设置回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 重置游戏模式
        setGameMode(GameMode.PLAYER_VS_AI);
        playerColorComboBox.setEnabled(true);
        difficultyComboBox.setEnabled(true);
        
        // 刷新界面
        revalidate();
        repaint();
        
        updateStatus("当前玩家: 红方");
    }


    private void initializeDefaultAI() {
        // 延迟执行，确保界面完全初始化
        SwingUtilities.invokeLater(() -> {
            // 设置AI类型为DeepSeek+Pikafish
            aiTypeComboBox.setSelectedIndex(4);
            
            // 设置模型
            String modelName = "deepseek-r1:7b"; // 更新为可用的模型
            modelComboBox.setSelectedItem(modelName);
            
            // 确保玩家对AI单选框被选中
            playerVsAIRadio.setSelected(true);
            currentGameMode = GameMode.PLAYER_VS_AI;
            
            // 确保棋盘面板可见和正确配置
            if (boardPanel != null) {
                boardPanel.setVisible(true);
                // 使用计算出的尺寸，确保棋盘有足够空间
                Dimension calculatedSize = boardPanel.calculateBoardSize();
                boardPanel.setPreferredSize(calculatedSize);
                boardPanel.setMinimumSize(calculatedSize);
                boardPanel.revalidate();
                boardPanel.repaint();
                
                // 确保棋盘有正确的边界和大小
                System.out.println("✅ 棋盘面板尺寸: " + boardPanel.getSize());
                System.out.println("✅ 棋盘面板可见性: " + boardPanel.isVisible());
                System.out.println("✅ 计算出的棋盘尺寸: " + calculatedSize);
            }
            
            // 立即启动默认游戏模式，确保棋盘可见
            try {
                setupPlayerVsAIMode();
                System.out.println("✅ 默认启动玩家对AI模式完成");
            } catch (Exception e) {
                System.err.println("⚠️ 启动默认模式失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 强制刷新整个界面，确保棋盘显示
            revalidate();
            repaint();
            
            // 额外的延迟确保界面完全加载
            Timer visibilityTimer = new Timer(100, e -> {
                if (boardPanel != null) {
                    boardPanel.setVisible(true);
                    boardPanel.repaint();
                }
                revalidate();
                repaint();
            });
            visibilityTimer.setRepeats(false);
            visibilityTimer.start();
        });
    }
    
    /**
     * 自动启用AI对弈（供外部调用）
     */
    public void autoEnableAI() {
        // 延迟执行，确保界面完全初始化
        SwingUtilities.invokeLater(() -> {
            setGameMode(GameMode.PLAYER_VS_AI); // 设置为玩家对AI模式
        });
    }

    public void updateStatus(String status) {
        statusLabel.setText(status);
        
        // 更新窗口标题显示当前下棋方
        updateWindowTitle(status);
    }
    
    /**
     * 更新窗口标题显示当前下棋方
     */
    private void updateWindowTitle(String status) {
        String baseTitle = "🏮 中国象棋 - AI对弈版";
        
        // 从状态信息中提取当前玩家信息
        String currentPlayerInfo = "";
        if (status.contains("红方")) {
            currentPlayerInfo = " - 🔴 红方下棋";
        } else if (status.contains("黑方")) {
            currentPlayerInfo = " - ⚫ 黑方下棋";
        }
        
        setTitle(baseTitle + currentPlayerInfo);
    }
    
    /**
     * 返回游戏选择界面
     */
    private void returnToSelection() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出当前游戏吗？",
            "退出游戏",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0); // 退出程序
        }
    }
    
    /**
     * 切换残局设置模式
     */
    private void toggleEndgameSetup() {
        if (!isInEndgameSetup) {
            // 进入残局设置模式
            isInEndgameSetup = true;
            boardPanel.enterEndgameSetupMode();
            
            // 更新UI
            endgameButton.setText("退出残局");
            endgameButton.setToolTipText("退出残局设置模式");
            endgameFirstMoveComboBox.setVisible(true);
            endgamePlayerModeComboBox.setVisible(true);
            updateEndgameAIOptions(); // 根据选择的模式显示AI选项
            startEndgameButton.setVisible(true);
            aiVsAiEndgameButton.setVisible(false); // 隐藏旧的AI对AI按钮，使用新的模式选择
            
            // 禁用其他按钮
            playerColorComboBox.setEnabled(false);
            difficultyComboBox.setEnabled(false);
            aiTypeComboBox.setEnabled(false);
            modelComboBox.setEnabled(false);
            
            updateStatus("🎯 残局设置模式 - 右键点击棋盘放置/移除棋子");
            
        } else {
            // 退出残局设置模式
            isInEndgameSetup = false;
            boardPanel.exitEndgameSetupMode();
            
            // 更新UI
            endgameButton.setText("残局");
            endgameButton.setToolTipText("进入残局设置模式");
            endgameFirstMoveComboBox.setVisible(false);
            endgamePlayerModeComboBox.setVisible(false);
            endgameAIColorComboBox.setVisible(false);
            startEndgameButton.setVisible(false);
            aiVsAiEndgameButton.setVisible(false);
            
            // 恢复其他按钮
            playerColorComboBox.setEnabled(true);
            difficultyComboBox.setEnabled(true);
            aiTypeComboBox.setEnabled(true);
            updateModelComboBox();
            
            updateStatus("🔴 当前玩家: 红方");
        }
        
        // 刷新界面
        revalidate();
        repaint();
    }
    
    /**
     * 开始残局游戏
     */
    private void startEndgameGame() {
        if (!isInEndgameSetup) {
            return;
        }
        
        // 获取先手选择
        String firstMoveSelection = (String) endgameFirstMoveComboBox.getSelectedItem();
        PieceColor firstMoveColor = firstMoveSelection.equals("红方先手") ? PieceColor.RED : PieceColor.BLACK;
        
        // 获取玩家模式选择
        String playerModeSelection = (String) endgamePlayerModeComboBox.getSelectedItem();
        
        // 获取当前AI设置
        int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
        int difficulty = difficultyComboBox.getSelectedIndex() + 1;
        String modelName = (String) modelComboBox.getSelectedItem();
        
        // 根据选择的模式启动不同的残局游戏
        switch (playerModeSelection) {
            case "玩家对AI":
                startPlayerVsAIEndgame(firstMoveColor, aiTypeIndex, difficulty, modelName);
                break;
            case "AI对AI":
                startAIVsAIEndgame(firstMoveColor, aiTypeIndex, difficulty, modelName);
                break;
            case "玩家对玩家":
                startPlayerVsPlayerEndgame(firstMoveColor);
                break;
        }
        
        // 退出残局设置模式
        isInEndgameSetup = false;
        
        // 更新UI
        endgameButton.setText("残局");
        endgameButton.setToolTipText("进入残局设置模式");
        endgameFirstMoveComboBox.setVisible(false);
        endgamePlayerModeComboBox.setVisible(false);
        endgameAIColorComboBox.setVisible(false);
        startEndgameButton.setVisible(false);
        aiVsAiEndgameButton.setVisible(false);
        
        // 刷新界面
        revalidate();
        repaint();
    }
    
    /**
     * 开始玩家对AI残局游戏
     */
    private void startPlayerVsAIEndgame(PieceColor firstMoveColor, int aiTypeIndex, int difficulty, String modelName) {
        // 获取AI颜色选择
        String aiColorSelection = (String) endgameAIColorComboBox.getSelectedItem();
        PieceColor aiColor = aiColorSelection.equals("AI执红") ? PieceColor.RED : PieceColor.BLACK;
        
        // 设置先手
        boardPanel.setCurrentPlayer(firstMoveColor);
        
        // 启动残局游戏
        boardPanel.startEndgameGame(aiColor);
        
        // 根据AI类型启用相应的AI
        switch (aiTypeIndex) {
            case 0: // 传统AI
                boardPanel.enableAI(aiColor, difficulty, false, null);
                break;
            case 1: // 增强AI
                boardPanel.enableEnhancedAI(aiColor, difficulty);
                break;
            case 2: // 大模型AI
                boardPanel.enableAI(aiColor, difficulty, true, modelName);
                break;
            case 3: // 混合AI
                boardPanel.enableHybridAI(aiColor, difficulty, modelName);
                break;
            case 4: // DeepSeek+Pikafish
                boardPanel.enableDeepSeekPikafishAI(aiColor, difficulty, modelName);
                break;
        }
        
        // 设置游戏模式为玩家对AI
        currentGameMode = GameMode.PLAYER_VS_AI;
        
        // 启用聊天面板和AI日志面板（在使用大模型AI、混合AI或DeepSeek+Pikafish时）
        boolean enableLogPanel = (aiTypeIndex == 2) || (aiTypeIndex == 3) || (aiTypeIndex == 4);
        if (enableLogPanel) {
            chatPanel.setEnabled(true);
            chatPanel.setModelName(modelName);
            aiLogPanel.setEnabled(true);
        }
        
        // 禁用AI相关控件
        playerColorComboBox.setEnabled(false);
        difficultyComboBox.setEnabled(false);
        aiTypeComboBox.setEnabled(false);
        modelComboBox.setEnabled(false);
        
        String aiColorName = (aiColor == PieceColor.RED) ? "红方" : "黑方";
        String humanColorName = (aiColor == PieceColor.RED) ? "黑方" : "红方";
        String firstMoveName = (firstMoveColor == PieceColor.RED) ? "红方" : "黑方";
        updateStatus("🎯 残局游戏开始 - AI执" + aiColorName + "，玩家执" + humanColorName + "，" + firstMoveName + "先手");
        
        System.out.println("🎯 玩家对AI残局游戏开始:");
        System.out.println("   - AI颜色: " + aiColorName);
        System.out.println("   - 玩家颜色: " + humanColorName);
        System.out.println("   - 先手: " + firstMoveName);
        System.out.println("   - AI类型: " + aiTypeComboBox.getSelectedItem());
    }
    
    /**
     * 开始AI对AI残局游戏
     */
    private void startAIVsAIEndgame(PieceColor firstMoveColor, int aiTypeIndex, int difficulty, String modelName) {
        // 设置先手
        boardPanel.setCurrentPlayer(firstMoveColor);
        
        // 启动AI对AI残局游戏
        boardPanel.startAIvsAIEndgameGame();
        
        // 设置游戏模式为AI对AI
        currentGameMode = GameMode.AI_VS_AI;
        
        // 禁用AI相关控件
        playerColorComboBox.setEnabled(false);
        difficultyComboBox.setEnabled(false);
        aiTypeComboBox.setEnabled(false);
        modelComboBox.setEnabled(false);
        
        String firstMoveName = (firstMoveColor == PieceColor.RED) ? "红方" : "黑方";
        updateStatus("🤖 AI对AI残局游戏开始 - " + firstMoveName + "先手");
        
        System.out.println("🤖 AI对AI残局游戏开始:");
        System.out.println("   - 红方: AI");
        System.out.println("   - 黑方: AI");
        System.out.println("   - 先手: " + firstMoveName);
        System.out.println("   - AI类型: DeepSeek+Pikafish");
    }
    
    /**
     * 开始玩家对玩家残局游戏
     */
    private void startPlayerVsPlayerEndgame(PieceColor firstMoveColor) {
        // 设置先手
        boardPanel.setCurrentPlayer(firstMoveColor);
        
        // 启动玩家对玩家残局游戏
        boardPanel.startPlayerVsPlayerEndgame();
        
        // 设置游戏模式为玩家对玩家
        currentGameMode = GameMode.PLAYER_VS_PLAYER;
        
        // 禁用AI相关控件
        playerColorComboBox.setEnabled(false);
        difficultyComboBox.setEnabled(false);
        aiTypeComboBox.setEnabled(false);
        modelComboBox.setEnabled(false);
        
        String firstMoveName = (firstMoveColor == PieceColor.RED) ? "红方" : "黑方";
        updateStatus("👥 玩家对玩家残局游戏开始 - " + firstMoveName + "先手");
        
        System.out.println("👥 玩家对玩家残局游戏开始:");
        System.out.println("   - 红方: 玩家");
        System.out.println("   - 黑方: 玩家");
        System.out.println("   - 先手: " + firstMoveName);
    }
    
    /**
     * 开始AI对AI残局游戏
     */
    private void startAIvsAIEndgameGame() {
        if (!isInEndgameSetup) {
            return;
        }
        
        // 启动AI对AI残局游戏
        boardPanel.startAIvsAIEndgameGame();
        
        // 退出残局设置模式
        isInEndgameSetup = false;
        
        // 更新UI
        endgameButton.setText("残局");
        endgameButton.setToolTipText("进入残局设置模式");
        endgameAIColorComboBox.setVisible(false);
        startEndgameButton.setVisible(false);
        aiVsAiEndgameButton.setVisible(false);
        
        // 禁用AI相关控件（因为已经启用了AI对AI）
        playerColorComboBox.setEnabled(false);
        difficultyComboBox.setEnabled(false);
        aiTypeComboBox.setEnabled(false);
        modelComboBox.setEnabled(false);
        
        updateStatus("🤖 AI对AI残局游戏开始 - 红方AI vs 黑方AI");
        
        // 刷新界面
        revalidate();
        repaint();
        
        System.out.println("🤖 AI对AI残局游戏开始:");
        System.out.println("   - 红方: AI");
        System.out.println("   - 黑方: AI");
        System.out.println("   - AI类型: DeepSeek+Pikafish");
    }
    
    /**
     * 设置游戏模式
     */
    private void setGameMode(GameMode mode) {
        // 先清理当前模式
        cleanupCurrentMode();
        
        // 设置新模式
        currentGameMode = mode;
        
        // 更新游戏模式单选框状态
        updateGameModeRadios();
        
        // 根据新模式进行设置
        switch (mode) {
            case PLAYER_VS_AI:
                setupPlayerVsAIMode();
                break;
            case AI_VS_AI:
                setupAIvsAIMode();
                break;
            case PLAYER_VS_PLAYER:
                setupPlayerVsPlayerMode();
                break;
        }
        
        System.out.println("🔄 游戏模式切换为: " + getModeDisplayName(mode));
    }
    
    /**
     * 清理当前模式
     */
    private void cleanupCurrentMode() {
        // 禁用所有AI
        boardPanel.disableAI();
        if (boardPanel.isAIvsAIMode()) {
            boardPanel.disableAIvsAI();
        }
        
        // 重新启用所有控件
        aiTypeComboBox.setEnabled(true);
        difficultyComboBox.setEnabled(true);
        playerColorComboBox.setEnabled(true);
        updateModelComboBox();
    }
    
    // 选择游戏模式（不立即启动）
    private void selectGameMode(GameMode mode) {
        currentGameMode = mode;
        updateGameModeRadios();
        
        // 根据选择的模式显示/隐藏AI对AI配置面板
        if (mode == GameMode.AI_VS_AI) {
            toggleAIvsAIConfigPanel(true);
        } else {
            toggleAIvsAIConfigPanel(false);
        }
    }
    
    // 启动游戏
    private void startGame() {
        if (isGameRunning && !isGamePaused) {
            // 游戏已在运行，不需要重复启动
            updateStatus("游戏已在运行中");
            return;
        }
        
        // 保存当前棋局状态（如果需要）
        if (isGamePaused) {
            updateStatus("游戏继续...");
        } else {
            updateStatus("启动" + getModeDisplayName(currentGameMode) + "模式...");
        }
        
        // 设置游戏状态
        isGameRunning = true;
        isGamePaused = false;
        if (boardPanel != null) {
            boardPanel.setGamePaused(false); // 恢复棋盘
        }
        
        // 更新按钮状态
        startGameButton.setEnabled(false);
        pauseGameButton.setEnabled(true);
        
        // 根据选择的模式启动游戏
        setGameMode(currentGameMode);
        
        // 判断当前轮到谁走棋并继续游戏
        continueGameFromCurrentState();
    }
    
    // 暂停游戏
    private void pauseGame() {
        if (!isGameRunning) {
            return;
        }
        
        isGamePaused = true;
        isGameRunning = false;
        
        // 更新按钮状态
        startGameButton.setEnabled(true);
        pauseGameButton.setEnabled(false);
        
        // 停止AI思考（如果正在进行）
        if (boardPanel != null) {
            boardPanel.setGamePaused(true); // 暂停棋盘
            // 禁用AI对AI模式
            if (boardPanel.isAIvsAIMode()) {
                boardPanel.disableAIvsAI();
            }
            // 禁用普通AI
            if (boardPanel.isAIEnabled()) {
                boardPanel.disableAI();
            }
        }
        
        updateStatus("游戏已暂停，棋局已保存");
    }
    
    // 从当前状态继续游戏
    private void continueGameFromCurrentState() {
        if (boardPanel == null || boardPanel.getBoard() == null) {
            return;
        }
        
        // 检查游戏是否已经结束，如果结束则不重新启用AI
        if (boardPanel.getGameState() != com.example.chinesechess.core.GameState.PLAYING) {
            updateStatus("游戏已结束，无法继续");
            return;
        }
        
        // 获取当前玩家
        PieceColor currentPlayer = boardPanel.getCurrentPlayer();
        boolean isRedTurn = (currentPlayer == PieceColor.RED);
        String currentPlayerName = isRedTurn ? "红方" : "黑方";
        
        switch (currentGameMode) {
            case PLAYER_VS_AI:
                // 启用玩家对AI模式
                PieceColor playerColor = getPlayerColor();
                int difficulty = getDifficulty();
                String modelName = getSelectedModel();
                
                // 根据AI类型启用相应的AI
                 int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
                 if (aiTypeIndex == 6) { // Pikafish
                     boardPanel.enablePikafishAI(playerColor, difficulty);
                 } else if (aiTypeIndex == 5) { // Fairy-Stockfish
                     boardPanel.enableFairyStockfishAI(playerColor, difficulty);
                 } else if (aiTypeIndex == 4) { // DeepSeek+Pikafish
                     boardPanel.enableDeepSeekPikafishAI(playerColor, difficulty, modelName);
                 } else if (aiTypeIndex == 3) { // 混合AI
                     boardPanel.enableHybridAI(playerColor, difficulty, modelName);
                 } else if (aiTypeIndex == 2) { // 大模型AI
                     boardPanel.enableAI(playerColor, difficulty, true, modelName);
                 } else if (aiTypeIndex == 1) { // 增强AI
                     boardPanel.enableEnhancedAI(playerColor, difficulty);
                 } else { // 传统AI
                     boardPanel.enableAI(playerColor, difficulty, false, null);
                 }
                
                // 启用聊天面板和AI日志面板（在使用大模型AI、混合AI或DeepSeek+Pikafish时）
                boolean enableLogPanel = (aiTypeIndex == 2) || (aiTypeIndex == 3) || (aiTypeIndex == 4);
                if (enableLogPanel) {
                    chatPanel.setEnabled(true);
                    chatPanel.setModelName(modelName);
                    aiLogPanel.setEnabled(true);
                } else {
                    // 传统AI和增强AI也应该有基本的AI日志
                    aiLogPanel.setEnabled(true);
                }
                
                updateStatus("玩家对AI模式 - 当前玩家: " + currentPlayerName);
                break;
                
            case AI_VS_AI:
                // 启用AI对AI模式，使用配置面板中的设置
                int redDifficulty = redAIDifficultyComboBox.getSelectedIndex() + 1;
                String redModelName = (String) redAIModelComboBox.getSelectedItem();
                int blackDifficulty = blackAIDifficultyComboBox.getSelectedIndex() + 1;
                String blackModelName = (String) blackAIModelComboBox.getSelectedItem();
                
                boardPanel.enableAIvsAI(redDifficulty, redModelName, blackDifficulty, blackModelName);
                
                // 启用聊天面板和AI日志面板
                chatPanel.setEnabled(true);
                aiLogPanel.setEnabled(true);
                
                updateStatus("AI对AI模式 - 当前玩家: " + currentPlayerName);
                break;
                
            case PLAYER_VS_PLAYER:
                // 禁用所有AI
                boardPanel.disableAI();
                boardPanel.disableAIvsAI();
                
                // 禁用聊天面板和AI日志面板
                chatPanel.setEnabled(false);
                aiLogPanel.setEnabled(false);
                
                updateStatus("玩家对玩家模式 - 当前玩家: " + currentPlayerName);
                break;
        }
    }
    

    
    /**
     * 设置玩家对AI模式
     */
    private void setupPlayerVsAIMode() {
        // 获取当前设置
        int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
        int difficulty = difficultyComboBox.getSelectedIndex() + 1;
        String modelName = (String) modelComboBox.getSelectedItem();
        String playerColorStr = (String) playerColorComboBox.getSelectedItem();
        PieceColor humanColor = playerColorStr.equals("红方") ? PieceColor.RED : PieceColor.BLACK;
        
        // 启用对应的AI
        switch (aiTypeIndex) {
            case 0: // 传统AI
                boardPanel.enableAI(humanColor, difficulty, false, null);
                break;
            case 1: // 增强AI
                boardPanel.enableEnhancedAI(humanColor, difficulty);
                break;
            case 2: // 大模型AI
                boardPanel.enableAI(humanColor, difficulty, true, modelName);
                break;
            case 3: // 混合AI
                boardPanel.enableHybridAI(humanColor, difficulty, modelName);
                break;
            case 4: // DeepSeek+Pikafish
                boardPanel.enableDeepSeekPikafishAI(humanColor, difficulty, modelName);
                break;
            case 5: // Fairy-Stockfish
                boardPanel.enableFairyStockfishAI(humanColor, difficulty);
                break;
            case 6: // Pikafish
                boardPanel.enablePikafishAI(humanColor, difficulty);
                break;
        }
        
        // 始终启用聊天面板（用于Pikafish和Fairy-Stockfish评估功能）
        chatPanel.setEnabled(true);
        chatPanel.setModelName(modelName);
        
        // 只有在使用大模型AI、混合AI或DeepSeek+Pikafish时才启用AI日志面板
        boolean enableLogPanel = (aiTypeIndex == 2) || (aiTypeIndex == 3) || (aiTypeIndex == 4);
        if (enableLogPanel) {
            aiLogPanel.setEnabled(true);
        } else {
            aiLogPanel.setEnabled(false);
        }
        
        updateStatus("🔴 玩家对AI模式已启用");
    }
    
    /**
     * 设置AI对AI模式
     */
    private void setupAIvsAIMode() {
        // 使用配置面板的设置启动AI vs AI模式
        int redDifficulty = redAIDifficultyComboBox.getSelectedIndex() + 1;
        String redModelName = (String) redAIModelComboBox.getSelectedItem();
        String redEngine = (String) redAIEngineComboBox.getSelectedItem();
        int blackDifficulty = blackAIDifficultyComboBox.getSelectedIndex() + 1;
        String blackModelName = (String) blackAIModelComboBox.getSelectedItem();
        String blackEngine = (String) blackAIEngineComboBox.getSelectedItem();
        
        // 启动AI vs AI模式（传入红方和黑方的配置）
        boardPanel.enableAIvsAIWithEngines(redDifficulty, redModelName, redEngine, blackDifficulty, blackModelName, blackEngine);
        
        // 禁用相关控件（AI对AI模式下不需要用户选择）
        aiTypeComboBox.setEnabled(false);
        difficultyComboBox.setEnabled(false);
        modelComboBox.setEnabled(false);
        playerColorComboBox.setEnabled(false);
        
        // 启用聊天面板和AI日志面板
        chatPanel.setEnabled(true);
        aiLogPanel.setEnabled(true);
        
        String redDifficultyName = getDifficultyName(redDifficulty);
        String blackDifficultyName = getDifficultyName(blackDifficulty);
        updateStatus("🤖 AI对AI模式 - 🔴红方AI(" + redEngine + ", " + redDifficultyName + ") vs ⚫黑方AI(" + blackEngine + ", " + blackDifficultyName + ")");
    }
    
    /**
     * 设置玩家对玩家模式
     */
    private void setupPlayerVsPlayerMode() {
        // 玩家对玩家模式下不需要AI，所有AI都已在cleanupCurrentMode中禁用
        
        // 禁用AI相关控件
        aiTypeComboBox.setEnabled(false);
        difficultyComboBox.setEnabled(false);
        modelComboBox.setEnabled(false);
        
        // 玩家颜色选择仍然有效（用于界面显示）
        playerColorComboBox.setEnabled(true);
        
        // 禁用聊天面板和AI日志面板
        chatPanel.setEnabled(false);
        aiLogPanel.setEnabled(false);
        
        updateStatus("👥 玩家对玩家模式已启用");
    }
    
    /**
     * 获取模式显示名称
     */
    private String getModeDisplayName(GameMode mode) {
        switch (mode) {
            case PLAYER_VS_AI: return "玩家对AI";
            case AI_VS_AI: return "AI对AI";
            case PLAYER_VS_PLAYER: return "玩家对玩家";
            default: return "未知模式";
        }
    }
    
    /**
     * 切换AI vs AI对弈模式（保留兼容性）
     */
    private void toggleAIvsAI() {
        if (currentGameMode == GameMode.AI_VS_AI) {
            setGameMode(GameMode.PLAYER_VS_AI);
        } else {
            setGameMode(GameMode.AI_VS_AI);
        }
    }
    
    // 获取玩家颜色
    private PieceColor getPlayerColor() {
        String selectedColor = (String) playerColorComboBox.getSelectedItem();
        return "红方".equals(selectedColor) ? PieceColor.RED : PieceColor.BLACK;
    }
    
    // 获取AI难度
    private int getDifficulty() {
        return difficultyComboBox.getSelectedIndex() + 1;
    }
    
    // 获取选择的模型
    private String getSelectedModel() {
        Object selectedModel = modelComboBox.getSelectedItem();
        return selectedModel != null ? selectedModel.toString() : "deepseek-coder";
    }
    
    // 更新游戏模式单选框状态
    private void updateGameModeRadios() {
        switch (currentGameMode) {
            case PLAYER_VS_AI:
                playerVsAIRadio.setSelected(true);
                break;
            case AI_VS_AI:
                aiVsAIRadio.setSelected(true);
                break;
            case PLAYER_VS_PLAYER:
                playerVsPlayerRadio.setSelected(true);
                break;
        }
    }
    
    /**
     * 显示AI对AI配置对话框
     */
    private AIvsAIConfigDialog showAIvsAIConfigDialog() {
        AIvsAIConfigDialog dialog = new AIvsAIConfigDialog(this);
        
        // 设置默认值（从当前界面获取）
        int currentDifficulty = difficultyComboBox.getSelectedIndex() + 1;
        String currentModel = (String) modelComboBox.getSelectedItem();
        
        dialog.setDefaultDifficulty(currentDifficulty);
        dialog.setDefaultModel(currentModel);
        
        // 显示对话框
        dialog.setVisible(true);
        
        return dialog;
    }
    
    /**
     * 获取难度名称
     */
    private String getDifficultyName(int difficulty) {
        String[] difficultyNames = {"简单", "普通", "困难", "专家", "大师", "特级", "超级", "顶级", "传奇", "神级"};
        if (difficulty >= 1 && difficulty <= difficultyNames.length) {
            return difficultyNames[difficulty - 1];
        }
        return "未知";
    }
    
    /**
     * AI对AI配置对话框内部类
     */
    private static class AIvsAIConfigDialog extends JDialog {
        private JComboBox<String> difficultyComboBox;
        private JComboBox<String> modelComboBox;
        private boolean confirmed = false;
        
        public AIvsAIConfigDialog(JFrame parent) {
            super(parent, "🤖 AI对AI对弈配置", true);
            initComponents();
            setupLayout();
            setupEventHandlers();
            
            setSize(400, 200);
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
        
        private void initComponents() {
            // 创建难度选择下拉框
            String[] difficultyOptions = {"简单", "普通", "困难", "专家", "大师", "特级", "超级", "顶级", "传奇", "神级"};
            difficultyComboBox = new JComboBox<>(difficultyOptions);
            difficultyComboBox.setSelectedIndex(2); // 默认困难
            
            // 创建模型选择下拉框
            List<String> availableModels = OllamaModelManager.getAvailableModels();
            modelComboBox = new JComboBox<>(availableModels.toArray(new String[0]));
            if (!availableModels.isEmpty()) {
                modelComboBox.setSelectedIndex(0); // 默认第一个模型
            }
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout(10, 10));
            
            // 顶部标题
            JLabel titleLabel = new JLabel("配置AI对AI对弈参数", JLabel.CENTER);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
            add(titleLabel, BorderLayout.NORTH);
            
            // 中央配置面板
            JPanel configPanel = new JPanel(new GridBagLayout());
            configPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            
            // AI难度配置
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 0, 5, 10);
            configPanel.add(new JLabel("AI难度:"), gbc);
            
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            configPanel.add(difficultyComboBox, gbc);
            
            // AI模型配置
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(new JLabel("AI模型:"), gbc);
            
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            configPanel.add(modelComboBox, gbc);
            
            // 添加说明文字
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 0, 0, 0);
            JLabel noteLabel = new JLabel("<html><div style='text-align: center; color: #666;'><small>※ 红黑双方AI将使用相同的难度和模型</small></div></html>");
            noteLabel.setHorizontalAlignment(JLabel.CENTER);
            configPanel.add(noteLabel, gbc);
            
            add(configPanel, BorderLayout.CENTER);
            
            // 底部按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            
            JButton confirmButton = new JButton("开始对弈");
            confirmButton.setPreferredSize(new Dimension(100, 30));
            confirmButton.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            
            JButton cancelButton = new JButton("取消");
            cancelButton.setPreferredSize(new Dimension(100, 30));
            cancelButton.addActionListener(e -> {
                confirmed = false;
                dispose();
            });
            
            // 设置按钮样式
            styleDialogButton(confirmButton);
            styleDialogButton(cancelButton);
            
            buttonPanel.add(confirmButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            // ESC键取消
            KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
            getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    confirmed = false;
                    dispose();
                }
            });
            
            // Enter键确认
            KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKeyStroke, "ENTER");
            getRootPane().getActionMap().put("ENTER", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    confirmed = true;
                    dispose();
                }
            });
        }
        
        private void styleDialogButton(JButton button) {
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createRaisedBevelBorder());
            button.setBackground(new Color(245, 245, 245));
            button.setForeground(Color.BLACK);
            
            // 添加鼠标悬停效果
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(230, 230, 230));
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(245, 245, 245));
                }
            });
        }
        
        // Getter方法
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public int getDifficulty() {
            return difficultyComboBox.getSelectedIndex() + 1;
        }
        
        public String getModelName() {
            Object selectedModel = modelComboBox.getSelectedItem();
            return selectedModel != null ? selectedModel.toString() : "deepseek-coder";
        }
        
        // Setter方法（用于设置默认值）
        public void setDefaultDifficulty(int difficulty) {
            if (difficulty >= 1 && difficulty <= difficultyComboBox.getItemCount()) {
                difficultyComboBox.setSelectedIndex(difficulty - 1);
            }
        }
        
        public void setDefaultModel(String modelName) {
            if (modelName != null) {
                modelComboBox.setSelectedItem(modelName);
            }
        }
    }
    
    /**
     * 执行 Pikafish 分析
     */
    private void performPikafishAnalysis() {
        if (boardPanel == null || boardPanel.getBoard() == null) {
            JOptionPane.showMessageDialog(this, "请先开始游戏！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 在后台线程中执行分析，避免阻塞UI
        SwingWorker<String, Void> analysisWorker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                updateStatus("🐟 Pikafish 正在分析当前局面...");
                
                try {
                    // 获取当前局面的最佳走法
                    String bestMove = boardPanel.getBestMoveFromPikafish();
                    
                    if (bestMove != null && !bestMove.isEmpty()) {
                        return "🐟 Pikafish 推荐走法: " + bestMove;
                    } else {
                        return "🐟 Pikafish 未能找到推荐走法";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "🐟 Pikafish 分析出错: " + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    
                    // 在AI日志面板中显示分析结果
                    if (aiLogPanel != null) {
                        aiLogPanel.addAIDecision(result);
                    }
                    
                    // 更新状态
                    String currentPlayer = boardPanel.getCurrentPlayer() == PieceColor.RED ? "红方" : "黑方";
                    updateStatus("🔴 当前玩家: " + currentPlayer + " | " + result);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    updateStatus("🐟 Pikafish 分析失败: " + e.getMessage());
                    if (aiLogPanel != null) {
                        aiLogPanel.addError("🐟 Pikafish 分析失败: " + e.getMessage());
                    }
                }
            }
        };
        
        analysisWorker.execute();
    }
    
    /**
     * 执行 Fairy-Stockfish 分析
     */
    private void performFairyAnalysis() {
        if (boardPanel == null || boardPanel.getBoard() == null) {
            JOptionPane.showMessageDialog(this, "请先开始游戏！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 在后台线程中执行分析，避免阻塞UI
        SwingWorker<String, Void> analysisWorker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                updateStatus("🧚 Fairy-Stockfish 正在分析当前局面...");
                
                try {
                    // 获取当前局面的最佳走法
                    String bestMove = boardPanel.getBestMoveFromFairyStockfish();
                    
                    if (bestMove != null && !bestMove.isEmpty()) {
                        return "🧚 Fairy-Stockfish 推荐走法: " + bestMove;
                    } else {
                        return "🧚 Fairy-Stockfish 未能找到推荐走法";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "🧚 Fairy-Stockfish 分析出错: " + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    
                    // 在AI日志面板中显示分析结果
                    if (aiLogPanel != null) {
                        aiLogPanel.addAIDecision(result);
                    }
                    
                    // 更新状态
                    String currentPlayer = boardPanel.getCurrentPlayer() == PieceColor.RED ? "红方" : "黑方";
                    updateStatus("🔴 当前玩家: " + currentPlayer + " | " + result);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    updateStatus("🧚 Fairy-Stockfish 分析失败: " + e.getMessage());
                    if (aiLogPanel != null) {
                        aiLogPanel.addError("🧚 Fairy-Stockfish 分析失败: " + e.getMessage());
                    }
                }
            }
        };
        
        analysisWorker.execute();
    }
    
    /**
     * 浏览选择神经网络文件（玩家对AI模式）
     */
    private void browseNeuralNetworkFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择FairyStockfish神经网络文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".nnue") || f.getName().toLowerCase().endsWith(".bin");
            }
            
            @Override
            public String getDescription() {
                return "神经网络文件 (*.nnue, *.bin)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            neuralNetworkPathField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * 浏览选择红方神经网络文件（AI对AI模式）
     */
    private void browseRedNeuralNetworkFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择红方FairyStockfish神经网络文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".nnue") || f.getName().toLowerCase().endsWith(".bin");
            }
            
            @Override
            public String getDescription() {
                return "神经网络文件 (*.nnue, *.bin)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            redNeuralNetworkPathField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * 浏览选择黑方神经网络文件（AI对AI模式）
     */
    private void browseBlackNeuralNetworkFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择黑方FairyStockfish神经网络文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".nnue") || f.getName().toLowerCase().endsWith(".bin");
            }
            
            @Override
            public String getDescription() {
                return "神经网络文件 (*.nnue, *.bin)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            blackNeuralNetworkPathField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * 处理神经网络选择事件
     */
    private void handleNeuralNetworkSelection() {
        if (neuralNetworkComboBox == null) return;
        
        String selection = (String) neuralNetworkComboBox.getSelectedItem();
        if ("自定义文件...".equals(selection)) {
            // 打开文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择FairyStockfish神经网络文件");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(java.io.File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".nnue") || f.getName().toLowerCase().endsWith(".bin");
                }
                
                @Override
                public String getDescription() {
                    return "神经网络文件 (*.nnue, *.bin)";
                }
            });
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                // 动态添加到下拉列表
                String customPath = selectedFile.getAbsolutePath();
                neuralNetworkComboBox.addItem(customPath);
                neuralNetworkComboBox.setSelectedItem(customPath);
            } else {
                // 取消选择，恢复为默认
                neuralNetworkComboBox.setSelectedIndex(0);
            }
        }
        
        System.out.println("📶 神经网络选择: " + selection);
    }
    
    /**
     * 处理红方神经网络选择事件（AI对AI模式）
     */
    private void handleRedNeuralNetworkSelection() {
        if (redNeuralNetworkComboBox == null) return;
        
        String selection = (String) redNeuralNetworkComboBox.getSelectedItem();
        if ("自定义文件...".equals(selection)) {
            // 打开文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择红方FairyStockfish神经网络文件");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(java.io.File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".nnue") || f.getName().toLowerCase().endsWith(".bin");
                }
                
                @Override
                public String getDescription() {
                    return "神经网络文件 (*.nnue, *.bin)";
                }
            });
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                // 动态添加到下拉列表
                String customPath = selectedFile.getAbsolutePath();
                redNeuralNetworkComboBox.addItem(customPath);
                redNeuralNetworkComboBox.setSelectedItem(customPath);
                // 同时更新备用的文本框
                redNeuralNetworkPathField.setText(customPath);
            } else {
                // 取消选择，恢复为默认
                redNeuralNetworkComboBox.setSelectedIndex(0);
            }
        } else {
            // 将选择的路径同步到文本框，保持兼容性
            String path = "默认（不使用NNUE）".equals(selection) ? "" : selection;
            redNeuralNetworkPathField.setText(path);
        }
        
        System.out.println("📶 红方神经网络选择: " + selection);
    }
    
    /**
     * 处理黑方神经网络选择事件（AI对AI模式）
     */
    private void handleBlackNeuralNetworkSelection() {
        if (blackNeuralNetworkComboBox == null) return;
        
        String selection = (String) blackNeuralNetworkComboBox.getSelectedItem();
        if ("自定义文件...".equals(selection)) {
            // 打开文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择黑方FairyStockfish神经网络文件");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(java.io.File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".nnue") || f.getName().toLowerCase().endsWith(".bin");
                }
                
                @Override
                public String getDescription() {
                    return "神经网络文件 (*.nnue, *.bin)";
                }
            });
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                // 动态添加到下拉列表
                String customPath = selectedFile.getAbsolutePath();
                blackNeuralNetworkComboBox.addItem(customPath);
                blackNeuralNetworkComboBox.setSelectedItem(customPath);
                // 同时更新备用的文本框
                blackNeuralNetworkPathField.setText(customPath);
            } else {
                // 取消选择，恢复为默认
                blackNeuralNetworkComboBox.setSelectedIndex(0);
            }
        } else {
            // 将选择的路径同步到文本框，保持兼容性
            String path = "默认（不使用NNUE）".equals(selection) ? "" : selection;
            blackNeuralNetworkPathField.setText(path);
        }
        
        System.out.println("📶 黑方神经网络选择: " + selection);
    }
    
    /**
     * 更新神经网络文件选择器的可见性（玩家对AI模式）
     */
    private void updateNeuralNetworkVisibility() {
        int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
        boolean isFairyStockfish = (aiTypeIndex == 5); // Fairy-Stockfish
        
        neuralNetworkLabel.setVisible(isFairyStockfish);
        neuralNetworkComboBox.setVisible(isFairyStockfish);
        
        // 刷新界面
        revalidate();
        repaint();
    }
    
    /**
     * 更新AI对AI模式中神经网络文件选择器的可见性
     */
    private void updateAIvsAINeuralNetworkVisibility() {
        // 更新红方神经网络选择器可见性
        String redEngine = (String) redAIEngineComboBox.getSelectedItem();
        boolean redIsFairyStockfish = "FairyStockfish".equals(redEngine);
        redNeuralNetworkLabel.setVisible(redIsFairyStockfish);
        redNeuralNetworkComboBox.setVisible(redIsFairyStockfish);
        // 保持文本框和浏览按钮隐藏，现在使用下拉菜单
        redNeuralNetworkPathField.setVisible(false);
        redNeuralNetworkBrowseButton.setVisible(false);
        
        // 更新黑方神经网络选择器可见性
        String blackEngine = (String) blackAIEngineComboBox.getSelectedItem();
        boolean blackIsFairyStockfish = "FairyStockfish".equals(blackEngine);
        blackNeuralNetworkLabel.setVisible(blackIsFairyStockfish);
        blackNeuralNetworkComboBox.setVisible(blackIsFairyStockfish);
        // 保持文本框和浏览按钮隐藏，现在使用下拉菜单
        blackNeuralNetworkPathField.setVisible(false);
        blackNeuralNetworkBrowseButton.setVisible(false);
        
        // 刷新界面
        revalidate();
        repaint();
    }
    
    /**
     * 获取选择的神经网络文件路径（玩家对AI模式）
     */
    public String getSelectedNeuralNetworkPath() {
        if (neuralNetworkComboBox != null && neuralNetworkComboBox.getSelectedItem() != null) {
            String selection = (String) neuralNetworkComboBox.getSelectedItem();
            if ("默认（不使用NNUE）".equals(selection) || "自定义文件...".equals(selection)) {
                return null; // 不使用神经网络或未选择
            }
            return selection; // 返回选中的文件路径
        }
        // 备用方案：从文本框获取
        if (neuralNetworkPathField != null) {
            String path = neuralNetworkPathField.getText();
            return (path != null && !path.trim().isEmpty()) ? path.trim() : null;
        }
        return null;
    }
    
    /**
     * 获取红方选择的神经网络文件路径（AI对AI模式）
     */
    public String getRedNeuralNetworkPath() {
        // 优先从下拉菜单获取
        if (redNeuralNetworkComboBox != null && redNeuralNetworkComboBox.getSelectedItem() != null) {
            String selection = (String) redNeuralNetworkComboBox.getSelectedItem();
            if ("默认（不使用NNUE）".equals(selection) || "自定义文件...".equals(selection)) {
                return null; // 不使用神经网络或未选择
            }
            return selection; // 返回选中的文件路径
        }
        // 备用方案：从文本框获取
        String path = redNeuralNetworkPathField.getText();
        return (path != null && !path.trim().isEmpty()) ? path.trim() : null;
    }
    
    /**
     * 获取黑方选择的神经网络文件路径（AI对AI模式）
     */
    public String getBlackNeuralNetworkPath() {
        // 优先从下拉菜单获取
        if (blackNeuralNetworkComboBox != null && blackNeuralNetworkComboBox.getSelectedItem() != null) {
            String selection = (String) blackNeuralNetworkComboBox.getSelectedItem();
            if ("默认（不使用NNUE）".equals(selection) || "自定义文件...".equals(selection)) {
                return null; // 不使用神经网络或未选择
            }
            return selection; // 返回选中的文件路径
        }
        // 备用方案：从文本框获取
        String path = blackNeuralNetworkPathField.getText();
        return (path != null && !path.trim().isEmpty()) ? path.trim() : null;
    }
    
    /**
     * 主方法，启动中国象棋游戏
     */
    public static void main(String[] args) {
        System.out.println("🏮 启动中国象棋游戏...");
        
        // 设置系统属性
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "中国象棋");
        
        // 在EDT中创建和显示界面
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // 创建游戏窗口
                GameFrame gameFrame = new GameFrame();
                gameFrame.setVisible(true);
                
                System.out.println("✅ 中国象棋游戏界面已启动");
                
            } catch (Exception e) {
                System.err.println("⚠️ 启动游戏失败: " + e.getMessage());
                e.printStackTrace();
                
                // 在出现错误时显示错误对话框
                JOptionPane.showMessageDialog(null, 
                    "启动游戏失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    // ======================== 网络模式相关方法 ========================
    
    /**
     * 设置网络模式
     */
    public void setNetworkMode(boolean networkMode) {
        this.isNetworkMode = networkMode;
        
        if (networkMode) {
            // 更新窗口标题
            setTitle("🌐 中国象棋 - 网络对弈");
            
            // 禁用AI相关功能（网络模式下不使用AI）
            aiTypeComboBox.setEnabled(false);
            difficultyComboBox.setEnabled(false);
            modelComboBox.setEnabled(false);
            
            // 设置为玩家对玩家模式
            playerVsPlayerRadio.setSelected(true);
            currentGameMode = GameMode.PLAYER_VS_PLAYER;
            
            // 添加网络状态标签
            if (networkStatusLabel == null) {
                networkStatusLabel = new JLabel("🔴 未连接");
                networkStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            }
            
            // 如果已经有控制面板，添加网络状态标签
            Component[] components = getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel && ((JPanel) comp).getBorder() != null) {
                    String title = ((JPanel) comp).getBorder().toString();
                    if (title.contains("AI对弈控制")) {
                        ((JPanel) comp).setBorder(BorderFactory.createTitledBorder("🌐 网络对弈控制"));
                        
                        // 添加网络状态到左侧面板
                        Component[] subComponents = ((JPanel) comp).getComponents();
                        for (Component subComp : subComponents) {
                            if (subComp instanceof JPanel) {
                                JPanel leftPanel = (JPanel) subComp;
                                if (leftPanel.getLayout() instanceof FlowLayout) {
                                    leftPanel.add(new JLabel("网络:"));
                                    leftPanel.add(networkStatusLabel);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        // 设置棋盘面板的网络模式
        if (boardPanel != null) {
            boardPanel.setNetworkMode(networkMode);
        }
    }
    
    /**
     * 设置网络客户端
     */
    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
        
        if (boardPanel != null) {
            boardPanel.setNetworkClient(networkClient);
        }
    }
    
    /**
     * 设置房间信息
     */
    public void setRoomInfo(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        
        // 更新状态
        if (networkStatusLabel != null) {
            networkStatusLabel.setText("🟢 房间: " + roomName);
        }
        
        // 更新窗口标题
        setTitle("🌐 中国象棋 - " + roomName + " (ID: " + roomId + ")");
        
        // 更新状态栏
        updateStatus("已加入房间: " + roomName + " - 等待对手加入...");
    }
    
    /**
     * 设置本地玩家名称
     */
    public void setLocalPlayerName(String playerName) {
        this.localPlayerName = playerName;
    }
    
    /**
     * 获取网络模式状态
     */
    public boolean isNetworkMode() {
        return isNetworkMode;
    }
    
    /**
     * 获取网络客户端
     */
    public NetworkClient getNetworkClient() {
        return networkClient;
    }
    
    /**
     * 获取房间ID
     */
    public String getRoomId() {
        return roomId;
    }
    
    /**
     * 获取房间名称
     */
    public String getRoomName() {
        return roomName;
    }
    
    /**
     * 获取本地玩家名称
     */
    public String getLocalPlayerName() {
        return localPlayerName;
    }
    
    /**
     * 更新网络连接状态
     */
    public void updateNetworkStatus(String status) {
        if (networkStatusLabel != null) {
            networkStatusLabel.setText(status);
        }
    }
    
    /**
     * 处理网络游戏开始
     */
    public void onNetworkGameStart(String opponentName, PieceColor playerColor) {
        SwingUtilities.invokeLater(() -> {
            // 设置玩家颜色
            String colorText = (playerColor == PieceColor.RED) ? "红方" : "黑方";
            playerColorComboBox.setSelectedItem(colorText);
            
            // 更新状态
            String colorEmoji = (playerColor == PieceColor.RED) ? "🔴" : "⚫";
            updateStatus(colorEmoji + " 你执" + colorText + " vs " + opponentName + " - 游戏开始！");
            
            // 更新窗口标题
            setTitle("🌐 中国象棋 - " + roomName + " (你:" + colorText + " vs " + opponentName + ")");
            
            // 重要：设置棋盘面板的本地玩家颜色
            if (boardPanel != null) {
                String colorString = (playerColor == PieceColor.RED) ? "RED" : "BLACK";
                boardPanel.setLocalPlayerColor(colorString);
                boardPanel.setOpponentName(opponentName);
                
                System.out.println("🌐 设置网络游戏参数: 本地玩家=" + colorString + ", 对手=" + opponentName);
            }
        });
    }
    
    /**
     * 处理对手断开连接
     */
    public void onOpponentDisconnected() {
        SwingUtilities.invokeLater(() -> {
            updateStatus("⚠️ 对手已断开连接 - 游戏暂停");
            
            int option = JOptionPane.showConfirmDialog(
                this,
                "对手已断开连接，是否返回房间列表？",
                "对手断开",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (option == JOptionPane.YES_OPTION) {
                // 返回房间列表
                setVisible(false);
                dispose();
            }
        });
    }
    
    /**
     * 启动网络对战
     */
    private void startNetworkGame() {
        System.out.println("🌐 启动网络对战模式...");
        
        try {
            // 隐藏当前游戏界面
            setVisible(false);
            
            // 创建网络房间界面
            UnifiedNetworkRoomFrame networkFrame = new UnifiedNetworkRoomFrame();
            networkFrame.setVisible(true);
            
            // 关闭当前游戏界面
            dispose();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "启动网络对战失败：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
            // 如果失败，重新显示当前界面
            setVisible(true);
        }
    }

}
