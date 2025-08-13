package com.example.internationalchess;

import com.example.internationalchess.ui.InternationalBoardPanel;
import com.example.internationalchess.ui.ChatPanel;
import com.example.internationalchess.ui.AILogPanel;
import com.example.internationalchess.ui.StockfishLogPanel;
import com.example.common.utils.OllamaModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * 国际象棋游戏界面
 */
public class InternationalChessFrame extends JFrame {

    private JLabel statusLabel;
    private InternationalBoardPanel boardPanel;
    private JButton aiToggleButton;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> playerColorComboBox;
    private JComboBox<String> aiTypeComboBox;
    private JComboBox<String> modelComboBox;
    private ChatPanel chatPanel;
    private AILogPanel aiLogPanel;
    private StockfishLogPanel stockfishLogPanel;
    
    // 游戏模式相关
    private ButtonGroup gameModeGroup;
    private JRadioButton playerVsAIRadio;
    private JRadioButton aiVsAIRadio;
    private JRadioButton playerVsPlayerRadio;
    private JButton startButton;
    private JButton pauseButton;
    private JButton quitButton;
    private String currentGameMode = "玩家对AI";
    private boolean isAIvsAIMode = false;
    private boolean isPaused = false;
    private Timer aiVsAiTimer;

    public InternationalChessFrame() {
        setTitle("♟️ 国际象棋 - AI对弈版");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout());

        // 创建组件
        boardPanel = new InternationalBoardPanel();
        chatPanel = new ChatPanel();
        aiLogPanel = new AILogPanel();
        stockfishLogPanel = new StockfishLogPanel();
        
        // 首先创建状态栏
        statusLabel = new JLabel("⚪ 当前玩家: 白方", JLabel.CENTER);
        statusLabel.setFont(new Font("宋体", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel.setPreferredSize(new Dimension(1300, 30));
        add(statusLabel, BorderLayout.SOUTH);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 创建带坐标的棋盘面板
        JPanel boardWithCoordinates = createBoardWithCoordinates();
        mainPanel.add(boardWithCoordinates, BorderLayout.CENTER);
        
        // 右侧面板 - 显示Stockfish日志和AI分析面板
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(stockfishLogPanel, BorderLayout.CENTER);
        
        // AI分析按钮
        JButton aiAnalysisButton = new JButton("AI分析");
        aiAnalysisButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        aiAnalysisButton.addActionListener(e -> performAIAnalysis());
        styleButton(aiAnalysisButton);
        
        JPanel aiAnalysisPanel = new JPanel(new FlowLayout());
        aiAnalysisPanel.add(new JLabel("🤖 智能分析:"));
        aiAnalysisPanel.add(aiAnalysisButton);
        rightPanel.add(aiAnalysisPanel, BorderLayout.SOUTH);
        
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);

        // 创建控制面板（现在statusLabel已经初始化）
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // 设置BoardPanel的状态更新回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        boardPanel.setChatPanel(chatPanel);
        boardPanel.setStockfishLogPanel(stockfishLogPanel);
        
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
            Color hoverColor = originalColor.brighter();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                button.setBackground(originalColor);
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker());
                button.setBorder(BorderFactory.createLoweredBevelBorder());
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (button.contains(evt.getPoint())) {
                    button.setBackground(hoverColor);
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
        panel.setBorder(BorderFactory.createTitledBorder("♟️ 国际象棋对弈控制"));
        panel.setPreferredSize(new Dimension(1000, 80));

        // 左侧：基本设置（紧凑布局）
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // 游戏模式选择
        leftPanel.add(new JLabel("模式:"));
        gameModeGroup = new ButtonGroup();
        playerVsPlayerRadio = new JRadioButton("玩家对玩家", false);
        playerVsAIRadio = new JRadioButton("玩家对AI", true);
        aiVsAIRadio = new JRadioButton("AI对AI", false);
        
        playerVsPlayerRadio.setFont(new Font("宋体", Font.PLAIN, 12));
        playerVsAIRadio.setFont(new Font("宋体", Font.PLAIN, 12));
        aiVsAIRadio.setFont(new Font("宋体", Font.PLAIN, 12));
        
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
        playerColorComboBox = new JComboBox<>(new String[]{"白方", "黑方"});
        playerColorComboBox.setPreferredSize(new Dimension(60, 25));
        playerColorComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
        playerColorComboBox.addActionListener(e -> updatePlayerColor());
        leftPanel.add(playerColorComboBox);

        // AI类型选择
        leftPanel.add(new JLabel("AI:"));
        aiTypeComboBox = new JComboBox<>(new String[]{"Stockfish", "传统AI", "增强AI", "大模型AI", "混合AI"});
        aiTypeComboBox.setSelectedIndex(0); // 默认选择Stockfish
        aiTypeComboBox.setPreferredSize(new Dimension(80, 25));
        aiTypeComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
        aiTypeComboBox.addActionListener(e -> updateModelComboBox());
        leftPanel.add(aiTypeComboBox);

        // AI难度选择
        leftPanel.add(new JLabel("难度:"));
        difficultyComboBox = new JComboBox<>(new String[]{"简单", "普通", "困难", "专家", "大师", "特级", "超级", "顶级", "传奇", "神级"});
        difficultyComboBox.setSelectedIndex(2); // 默认困难难度
        difficultyComboBox.setPreferredSize(new Dimension(60, 25));
        difficultyComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
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
        } catch (Exception e) {
            modelComboBox = new JComboBox<>(new String[]{"deepseek-r1:7b"});
            modelComboBox.setSelectedIndex(0);
        }
        modelComboBox.setPreferredSize(new Dimension(150, 25));
        modelComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
        leftPanel.add(modelComboBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：控制按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        // 启动游戏按钮
        startButton = new JButton("启动游戏");
        startButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        startButton.setPreferredSize(new Dimension(80, 30));
        startButton.addActionListener(e -> startGame());
        styleButton(startButton);
        rightPanel.add(startButton);
        
        // 暂停游戏按钮
        pauseButton = new JButton("暂停游戏");
        pauseButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        pauseButton.setPreferredSize(new Dimension(80, 30));
        pauseButton.addActionListener(e -> pauseGame());
        pauseButton.setEnabled(false); // 初始状态禁用
        styleButton(pauseButton);
        rightPanel.add(pauseButton);
        
        // 启用/禁用AI按钮
        aiToggleButton = new JButton("启用AI对弈");
        aiToggleButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        aiToggleButton.setPreferredSize(new Dimension(80, 30)); // 增加宽度以适应较长的文本
        aiToggleButton.addActionListener(e -> toggleAI());
        styleButton(aiToggleButton);
        rightPanel.add(aiToggleButton);
        
        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        undoButton.setPreferredSize(new Dimension(60, 30));
        undoButton.addActionListener(e -> {
            if (boardPanel.canUndo()) {
                boardPanel.undoMove();
            } else {
                JOptionPane.showMessageDialog(this, "无法悔棋！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        styleButton(undoButton);
        rightPanel.add(undoButton);

        // 重新开始按钮
        JButton restartButton = new JButton("重新开始");
        restartButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        restartButton.setPreferredSize(new Dimension(80, 30));
        restartButton.addActionListener(e -> startNewGame());
        styleButton(restartButton);
        rightPanel.add(restartButton);
        
        // 退出游戏按钮
        quitButton = new JButton("退出游戏");
        quitButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        quitButton.setPreferredSize(new Dimension(80, 30));
        quitButton.addActionListener(e -> quitGame());
        styleButton(quitButton);
        rightPanel.add(quitButton);
        
        // 返回选择按钮
        JButton backButton = new JButton("返回选择");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        backButton.setPreferredSize(new Dimension(80, 30));
        backButton.addActionListener(e -> returnToSelection());
        styleButton(backButton);
        rightPanel.add(backButton);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        // 初始化游戏模式设置
        updateGameModeSettings();

        return panel;
    }
    
    /**
     * 创建控制面板（不自动调用updateGameModeSettings）
     * 用于重新创建界面时避免重置游戏模式
     */
    private JPanel createControlPanelWithoutInit() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("♟️ 国际象棋对弈控制"));
        panel.setPreferredSize(new Dimension(1000, 80));

        // 左侧：基本设置（紧凑布局）
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // 游戏模式选择
        leftPanel.add(new JLabel("模式:"));
        gameModeGroup = new ButtonGroup();
        playerVsPlayerRadio = new JRadioButton("玩家对玩家", false);
        playerVsAIRadio = new JRadioButton("玩家对AI", true);
        aiVsAIRadio = new JRadioButton("AI对AI", false);
        
        playerVsPlayerRadio.setFont(new Font("宋体", Font.PLAIN, 12));
        playerVsAIRadio.setFont(new Font("宋体", Font.PLAIN, 12));
        aiVsAIRadio.setFont(new Font("宋体", Font.PLAIN, 12));
        
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
        playerColorComboBox = new JComboBox<>(new String[]{"白方", "黑方"});
        playerColorComboBox.setPreferredSize(new Dimension(60, 25));
        playerColorComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
        playerColorComboBox.addActionListener(e -> updatePlayerColor());
        leftPanel.add(playerColorComboBox);

        // AI类型选择
        leftPanel.add(new JLabel("AI:"));
        aiTypeComboBox = new JComboBox<>(new String[]{"Stockfish", "传统AI", "增强AI", "大模型AI", "混合AI"});
        aiTypeComboBox.setSelectedIndex(0); // 默认选择Stockfish
        aiTypeComboBox.setPreferredSize(new Dimension(80, 25));
        aiTypeComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
        aiTypeComboBox.addActionListener(e -> updateModelComboBox());
        leftPanel.add(aiTypeComboBox);

        // AI难度选择
        leftPanel.add(new JLabel("难度:"));
        difficultyComboBox = new JComboBox<>(new String[]{"简单", "普通", "困难", "专家", "大师", "特级", "超级", "顶级", "传奇", "神级"});
        difficultyComboBox.setSelectedIndex(2); // 默认困难难度
        difficultyComboBox.setPreferredSize(new Dimension(60, 25));
        difficultyComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
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
        } catch (Exception e) {
            modelComboBox = new JComboBox<>(new String[]{"deepseek-r1:7b"});
            modelComboBox.setSelectedIndex(0);
        }
        modelComboBox.setPreferredSize(new Dimension(150, 25));
        modelComboBox.setFont(new Font("宋体", Font.PLAIN, 12));
        leftPanel.add(modelComboBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：控制按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        // 启动游戏按钮
        startButton = new JButton("启动游戏");
        startButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        startButton.setPreferredSize(new Dimension(80, 30));
        startButton.addActionListener(e -> startGame());
        styleButton(startButton);
        rightPanel.add(startButton);
        
        // 暂停游戏按钮
        pauseButton = new JButton("暂停游戏");
        pauseButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        pauseButton.setPreferredSize(new Dimension(80, 30));
        pauseButton.addActionListener(e -> pauseGame());
        pauseButton.setEnabled(false); // 初始状态禁用
        styleButton(pauseButton);
        rightPanel.add(pauseButton);
        
        // 启用/禁用AI按钮
        aiToggleButton = new JButton("启用AI对弈");
        aiToggleButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        aiToggleButton.setPreferredSize(new Dimension(80, 30)); // 增加宽度以适应较长的文本
        aiToggleButton.addActionListener(e -> toggleAI());
        styleButton(aiToggleButton);
        rightPanel.add(aiToggleButton);
        
        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        undoButton.setPreferredSize(new Dimension(60, 30));
        undoButton.addActionListener(e -> {
            if (boardPanel.canUndo()) {
                boardPanel.undoMove();
            } else {
                JOptionPane.showMessageDialog(this, "无法悔棋！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        styleButton(undoButton);
        rightPanel.add(undoButton);

        // 重新开始按钮
        JButton restartButton = new JButton("重新开始");
        restartButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        restartButton.setPreferredSize(new Dimension(80, 30));
        restartButton.addActionListener(e -> startNewGame());
        styleButton(restartButton);
        rightPanel.add(restartButton);
        
        // 退出游戏按钮
        quitButton = new JButton("退出游戏");
        quitButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        quitButton.setPreferredSize(new Dimension(80, 30));
        quitButton.addActionListener(e -> quitGame());
        styleButton(quitButton);
        rightPanel.add(quitButton);
        
        // 返回选择按钮
        JButton backButton = new JButton("返回选择");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        backButton.setPreferredSize(new Dimension(80, 30));
        backButton.addActionListener(e -> returnToSelection());
        styleButton(backButton);
        rightPanel.add(backButton);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        // 注意：不调用 updateGameModeSettings() 来避免重置游戏模式

        return panel;
    }
    
    private void updateModelComboBox() {
        int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
        // Stockfish=0, 传统AI=1, 增强AI=2, 大模型AI=3, 混合AI=4
        boolean needsModel = (aiTypeIndex == 3) || (aiTypeIndex == 4); // 大模型AI 或 混合AI
        modelComboBox.setEnabled(needsModel);
        
        if (needsModel) {
            modelComboBox.setBackground(Color.WHITE);
        } else {
            modelComboBox.setBackground(Color.LIGHT_GRAY);
        }
    }

    private void toggleAI() {
        if (aiToggleButton.getText().equals("启用AI对弈")) {
            // 启用AI
            aiToggleButton.setText("禁用AI对弈");
            playerColorComboBox.setEnabled(false);
            difficultyComboBox.setEnabled(false);
            aiTypeComboBox.setEnabled(false);
            modelComboBox.setEnabled(false);
            
            // 启用聊天面板（在使用大模型AI或混合AI时）
            int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
            // Stockfish=0, 传统AI=1, 增强AI=2, 大模型AI=3, 混合AI=4
            boolean enableChat = (aiTypeIndex == 3) || (aiTypeIndex == 4);
            
            if (enableChat) {
                chatPanel.setEnabled(true);
                String modelName = (String) modelComboBox.getSelectedItem();
                chatPanel.setModelName(modelName);
            }
            
            aiLogPanel.setEnabled(false); // 国际象棋不使用AI日志
            
            // 实际启用AI并设置人类玩家颜色
            boardPanel.setAIEnabled(true);
            
            // 根据玩家选择设置人类玩家颜色
            String colorStr = (String) playerColorComboBox.getSelectedItem();
            char humanColor = colorStr.equals("白方") ? 'W' : 'B';
            boardPanel.setHumanPlayer(humanColor);
            
            // 设置AI类型
            String[] aiTypes = {"Stockfish", "传统AI", "增强AI", "大模型AI", "混合AI"};
            String aiType = aiTypes[aiTypeIndex];
            int difficulty = difficultyComboBox.getSelectedIndex() + 1;
            String modelName = (String) modelComboBox.getSelectedItem();
            boardPanel.setAIType(aiType, difficulty, modelName);
        } else {
            // 禁用AI
            aiToggleButton.setText("启用AI对弈");
            playerColorComboBox.setEnabled(true);
            difficultyComboBox.setEnabled(true);
            aiTypeComboBox.setEnabled(true);
            updateModelComboBox(); // 恢复模型选择状态
            
            // 禁用聊天面板和AI日志面板
            chatPanel.setEnabled(false);
            aiLogPanel.setEnabled(false);
            
            // 实际禁用AI
            boardPanel.setAIEnabled(false);
        }
    }

    private void startNewGame() {
        // 保存当前AI状态和游戏模式
        boolean wasAIEnabled = (boardPanel != null && aiToggleButton.getText().equals("禁用AI对弈"));
        String currentAIType = null;
        int currentDifficulty = difficultyComboBox.getSelectedIndex() + 1;
        String currentModelName = (String) modelComboBox.getSelectedItem();
        char currentHumanColor = 'W';
        
        // 保存当前游戏模式状态
        String savedGameMode = currentGameMode;
        boolean savedAIvsAIMode = isAIvsAIMode;
        boolean savedPlayerVsPlayerSelected = playerVsPlayerRadio.isSelected();
        boolean savedPlayerVsAISelected = playerVsAIRadio.isSelected();
        boolean savedAIvsAISelected = aiVsAIRadio.isSelected();
        
        System.out.println("💾 保存游戏模式: " + savedGameMode + ", isAIvsAI: " + savedAIvsAIMode);
        
        if (boardPanel != null && wasAIEnabled) {
            // 保存当前设置
            String colorStr = (String) playerColorComboBox.getSelectedItem();
            currentHumanColor = colorStr.equals("白方") ? 'W' : 'B';
            String[] aiTypes = {"Stockfish", "传统AI", "增强AI", "大模型AI", "混合AI"};
            currentAIType = aiTypes[aiTypeComboBox.getSelectedIndex()];
        }
        
        // 移除旧的棋盘面板
        getContentPane().removeAll();
        
        // 创建新的棋盘面板
        boardPanel = new InternationalBoardPanel();
        
        // 重新创建聊天面板和日志面板
        chatPanel = new ChatPanel();
        stockfishLogPanel = new StockfishLogPanel();
        
        // 设置BoardPanel的引用
        boardPanel.setChatPanel(chatPanel);
        boardPanel.setStockfishLogPanel(stockfishLogPanel);
        
        // 重新构建界面布局
        recreateLayout();
        
        // 恢复保存的游戏模式状态
        SwingUtilities.invokeLater(() -> {
            System.out.println("🔄 恢复游戏模式状态: " + savedGameMode);
            
            // 恢复单选按钮状态
            playerVsPlayerRadio.setSelected(savedPlayerVsPlayerSelected);
            playerVsAIRadio.setSelected(savedPlayerVsAISelected);
            aiVsAIRadio.setSelected(savedAIvsAISelected);
            
            // 恢复模式变量
            currentGameMode = savedGameMode;
            isAIvsAIMode = savedAIvsAIMode;
            
            // 显式调用游戏模式设置更新（传递真实状态）
            updateGameModeSettingsWithState(savedGameMode, savedAIvsAIMode);
            
            System.out.println("✅ 游戏模式已恢复: " + currentGameMode + ", isAIvsAI: " + isAIvsAIMode);
        });
        
        // 设置回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 根据当前游戏模式恢复设置
        if (!savedAIvsAIMode) {
            if (wasAIEnabled && currentAIType != null) {
                // 创建final变量供lambda使用
                final String finalAIType = currentAIType;
                final char finalHumanColor = currentHumanColor;
                final String finalModelName = currentModelName;
                final int finalDifficulty = currentDifficulty;
                
                // 恢复AI状态
                SwingUtilities.invokeLater(() -> {
                    // 先设置所有参数，然后再启用AI，避免多次初始化
                    boardPanel.setHumanPlayer(finalHumanColor);
                    boardPanel.setAIType(finalAIType, finalDifficulty, finalModelName);
                    
                    // 最后启用AI，这样只会初始化一次
                    boardPanel.setAIEnabled(true);
                    
                    // 恢复UI状态
                    aiToggleButton.setText("禁用AI对弈");
                    playerColorComboBox.setEnabled(false);
                    difficultyComboBox.setEnabled(false);
                    aiTypeComboBox.setEnabled(false);
                    modelComboBox.setEnabled(false);
                    
                    // 启用聊天面板（如果需要）
                    int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
                    boolean enableChat = (aiTypeIndex == 3) || (aiTypeIndex == 4);
                    if (enableChat) {
                        chatPanel.setEnabled(true);
                        chatPanel.setModelName(finalModelName);
                    }
                    
                    updateStatus("游戏重新开始 - AI已恢复 (" + finalAIType + ")");
                });
            } else {
                // 非AI vs AI模式，重置AI按钮状态
                SwingUtilities.invokeLater(() -> {
                    aiToggleButton.setText("启用AI对弈");
                    playerColorComboBox.setEnabled(true);
                    difficultyComboBox.setEnabled(true);
                    aiTypeComboBox.setEnabled(true);
                    updateModelComboBox(); // 恢复模型选择状态
                    
                    // 确保AI被禁用（非AI vs AI模式）
                    boardPanel.setAIEnabled(false);
                    updateStatus("游戏重新开始 - 当前玩家: 白方");
                });
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                updateStatus("游戏重新开始 - 当前玩家: 白方");
            });
        }
        
        // 刷新界面
        revalidate();
        repaint();
    }
    
    /**
     * 重新创建界面布局
     */
    private void recreateLayout() {
        // 创建控制面板（但不自动初始化游戏模式）
        JPanel controlPanel = createControlPanelWithoutInit();
        add(controlPanel, BorderLayout.NORTH);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 创建带坐标的棋盘面板
        JPanel boardWithCoordinates = createBoardWithCoordinates();
        mainPanel.add(boardWithCoordinates, BorderLayout.CENTER);
        
        // 右侧面板 - 显示Stockfish日志和AI分析面板
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(stockfishLogPanel, BorderLayout.CENTER);
        
        // AI分析按钮
        JButton aiAnalysisButton = new JButton("AI分析");
        aiAnalysisButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        aiAnalysisButton.addActionListener(e -> performAIAnalysis());
        styleButton(aiAnalysisButton);
        
        JPanel aiAnalysisPanel = new JPanel(new FlowLayout());
        aiAnalysisPanel.add(new JLabel("🤖 智能分析:"));
        aiAnalysisPanel.add(aiAnalysisButton);
        rightPanel.add(aiAnalysisPanel, BorderLayout.SOUTH);
        
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);
        
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void returnToSelection() {
        dispose(); // 关闭当前窗口
        System.exit(0); // 暂时直接退出，等待实现GameSelectionFrame
    }

    /**
     * 初始化默认AI设置
     */
    private void initializeDefaultAI() {
        // 延迟执行，确保界面完全初始化
        SwingUtilities.invokeLater(() -> {
            // 自动启用大模型AI
            aiToggleButton.setText("禁用AI对弈");
            playerColorComboBox.setEnabled(false);
            difficultyComboBox.setEnabled(false);
            aiTypeComboBox.setEnabled(false);
            modelComboBox.setEnabled(false);

            // 默认人类玩家为白方，AI为黑方
            char humanColor = 'W'; // 白方
            
            // 设置AI类型为Stockfish
            aiTypeComboBox.setSelectedIndex(0); // "Stockfish"
            int difficulty = difficultyComboBox.getSelectedIndex() + 1; // 难度级别
            String modelName = (String) modelComboBox.getSelectedItem();
            
            // 先设置所有参数，然后再初始化AI（只调用一次）
            boardPanel.setHumanPlayer(humanColor);
            boardPanel.setAIType("Stockfish", difficulty, modelName);
            boardPanel.setAIEnabled(true);

            updateStatus("AI对弈已启用 - Stockfish");
        });
    }

    /**
     * 创建带坐标的棋盘面板
     */
    private JPanel createBoardWithCoordinates() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 获取棋盘的实际尺寸
        int boardSize = 8 * 70; // CELL_SIZE = 70
        
        // 创建顶部列标签 (a-h) - 确保宽度与棋盘一致
        JPanel topLabels = new JPanel(new GridLayout(1, 8));
        topLabels.setPreferredSize(new Dimension(boardSize, 25));
        topLabels.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        for (char c = 'a'; c <= 'h'; c++) {
            JLabel label = new JLabel(String.valueOf(c), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setForeground(new Color(101, 67, 33));
            topLabels.add(label);
        }
        
        // 创建底部列标签 (a-h) - 确保宽度与棋盘一致
        JPanel bottomLabels = new JPanel(new GridLayout(1, 8));
        bottomLabels.setPreferredSize(new Dimension(boardSize, 25));
        bottomLabels.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        for (char c = 'a'; c <= 'h'; c++) {
            JLabel label = new JLabel(String.valueOf(c), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setForeground(new Color(101, 67, 33));
            bottomLabels.add(label);
        }
        
        // 创建左侧行标签 (8-1) - 确保高度与棋盘一致
        JPanel leftLabels = new JPanel(new GridLayout(8, 1));
        leftLabels.setPreferredSize(new Dimension(25, boardSize));
        leftLabels.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        for (int i = 8; i >= 1; i--) {
            JLabel label = new JLabel(String.valueOf(i), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setForeground(new Color(101, 67, 33));
            leftLabels.add(label);
        }
        
        // 创建右侧行标签 (8-1) - 确保高度与棋盘一致
        JPanel rightLabels = new JPanel(new GridLayout(8, 1));
        rightLabels.setPreferredSize(new Dimension(25, boardSize));
        rightLabels.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        for (int i = 8; i >= 1; i--) {
            JLabel label = new JLabel(String.valueOf(i), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setForeground(new Color(101, 67, 33));
            rightLabels.add(label);
        }
        
        // 创建角落占位符
        JPanel topLeftCorner = new JPanel();
        topLeftCorner.setPreferredSize(new Dimension(25, 25));
        topLeftCorner.setBackground(Color.WHITE);
        
        JPanel topRightCorner = new JPanel();
        topRightCorner.setPreferredSize(new Dimension(25, 25));
        topRightCorner.setBackground(Color.WHITE);
        
        JPanel bottomLeftCorner = new JPanel();
        bottomLeftCorner.setPreferredSize(new Dimension(25, 25));
        bottomLeftCorner.setBackground(Color.WHITE);
        
        JPanel bottomRightCorner = new JPanel();
        bottomRightCorner.setPreferredSize(new Dimension(25, 25));
        bottomRightCorner.setBackground(Color.WHITE);
        
        // 创建顶部面板 (包含角落和列标签)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(topLeftCorner, BorderLayout.WEST);
        topPanel.add(topLabels, BorderLayout.CENTER);
        topPanel.add(topRightCorner, BorderLayout.EAST);
        
        // 创建底部面板 (包含角落和列标签)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(bottomLeftCorner, BorderLayout.WEST);
        bottomPanel.add(bottomLabels, BorderLayout.CENTER);
        bottomPanel.add(bottomRightCorner, BorderLayout.EAST);
        
        // 组合棋盘和坐标
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(leftLabels, BorderLayout.WEST);
        panel.add(rightLabels, BorderLayout.EAST);
        panel.add(boardPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 执行AI分析
     */
    private void performAIAnalysis() {
        if (stockfishLogPanel != null) {
            stockfishLogPanel.addAIDecision("📊 AI分析开始...");
            
            // 获取当前棋局信息
            String currentPlayer = boardPanel.isWhiteTurn() ? "白方" : "黑方";
            stockfishLogPanel.addAIDecision("当前轮到: " + currentPlayer);
            
            // 提供建议移动（简化版）
            String[] suggestions = {
                "建议1: 控制中心格子 (e4, d4, e5, d5)",
                "建议2: 开发较小棋子 (马、象)",
                "建议3: 保护国王安全 (王车易位)",
                "建议4: 找寻战术机会 (双重攻击, 铉住等)"
            };
            
            for (String suggestion : suggestions) {
                stockfishLogPanel.addAIDecision("💡 " + suggestion);
            }
            
            stockfishLogPanel.addAIDecision("🏆 分析完成！请根据建议考虑下一步移动。");
        }
    }
    
    public void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * 更新游戏模式设置
     */
    private void updateGameModeSettings() {
        if (playerVsPlayerRadio.isSelected()) {
            currentGameMode = "玩家对玩家";
            isAIvsAIMode = false;
        } else if (playerVsAIRadio.isSelected()) {
            currentGameMode = "玩家对AI";
            isAIvsAIMode = false;
        } else if (aiVsAIRadio.isSelected()) {
            currentGameMode = "AI对AI";
            isAIvsAIMode = true;
        }
        
        System.out.println("🗺️ 游戏模式切换: " + currentGameMode + ", isAIvsAIMode: " + isAIvsAIMode);
        
        switch (currentGameMode) {
            case "玩家对玩家":
                if (boardPanel != null) {
                    boardPanel.setAIEnabled(false);
                    boardPanel.setAIvsAIMode(false);
                }
                aiToggleButton.setEnabled(false);
                playerColorComboBox.setEnabled(true);
                difficultyComboBox.setEnabled(false);
                aiTypeComboBox.setEnabled(false);
                modelComboBox.setEnabled(false);
                break;
                
            case "玩家对AI":
                if (boardPanel != null) {
                    boardPanel.setAIvsAIMode(false);
                }
                aiToggleButton.setEnabled(true);
                playerColorComboBox.setEnabled(true);
                difficultyComboBox.setEnabled(true);
                aiTypeComboBox.setEnabled(true);
                modelComboBox.setEnabled(true);
                break;
                
            case "AI对AI":
                if (boardPanel != null) {
                    boardPanel.setAIvsAIMode(true);
                }
                aiToggleButton.setEnabled(false);
                playerColorComboBox.setEnabled(false);
                difficultyComboBox.setEnabled(true);
                aiTypeComboBox.setEnabled(true);
                updateModelComboBox();
                // 不立即初始化，等待用户点击“启动游戏”
                updateStatus("请点击“启动游戏”开始AI对AI模式");
                break;
        }
        
        updateStatusDisplay();
    }
    
    /**
     * 根据指定状态更新游戏模式设置（用于恢复状态）
     */
    private void updateGameModeSettingsWithState(String gameMode, boolean aiVsAIMode) {
        // 直接设置状态变量
        currentGameMode = gameMode;
        isAIvsAIMode = aiVsAIMode;
        
        System.out.println("🔧 恢复游戏模式设置: " + currentGameMode + ", isAIvsAIMode: " + isAIvsAIMode);
        
        switch (currentGameMode) {
            case "玩家对玩家":
                if (boardPanel != null) {
                    boardPanel.setAIEnabled(false);
                    boardPanel.setAIvsAIMode(false);
                }
                aiToggleButton.setEnabled(false);
                playerColorComboBox.setEnabled(true);
                difficultyComboBox.setEnabled(false);
                aiTypeComboBox.setEnabled(false);
                modelComboBox.setEnabled(false);
                break;
                
            case "玩家对AI":
                if (boardPanel != null) {
                    boardPanel.setAIvsAIMode(false);
                }
                aiToggleButton.setEnabled(true);
                playerColorComboBox.setEnabled(true);
                difficultyComboBox.setEnabled(true);
                aiTypeComboBox.setEnabled(true);
                modelComboBox.setEnabled(true);
                break;
                
            case "AI对AI":
                if (boardPanel != null) {
                    boardPanel.setAIvsAIMode(true);
                }
                aiToggleButton.setEnabled(false);
                playerColorComboBox.setEnabled(false);
                difficultyComboBox.setEnabled(true);
                aiTypeComboBox.setEnabled(true);
                updateModelComboBox();
                // 不立即初始化，等待用户点击“启动游戏”
                updateStatus("请点击“启动游戏”开始AI对AI模式");
                break;
        }
        
        updateStatusDisplay();
    }
    
    /**
     * 初始化AI对AI模式
     */
    private void initializeAIvsAI() {
        try {
            // 启用AI对AI模式
            boardPanel.setAIvsAIMode(true);
            
            // 设置AI类型和难度
            String[] aiTypes = {"Stockfish", "传统AI", "增强AI", "大模型AI", "混合AI"};
            String selectedAIType = aiTypes[aiTypeComboBox.getSelectedIndex()];
            int difficulty = difficultyComboBox.getSelectedIndex() + 1;
            String modelName = (String) modelComboBox.getSelectedItem();
            
            // 初始化双方AI
            boardPanel.initializeAIvsAI(selectedAIType, difficulty, modelName);
            
            updateStatus("🤖🆚🤖 AI对AI模式已初始化 - AI类型: " + selectedAIType);
            
            // 延迟2秒后开始AI对战
            Timer startTimer = new Timer(2000, e -> {
                if (boardPanel != null && isAIvsAIMode && !isPaused) {
                    boardPanel.startAIvsAI();
                    updateStatus("🎆 AI对战开始！");
                }
            });
            startTimer.setRepeats(false);
            startTimer.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "AI对AI初始化失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 更新玩家颜色
     */
    private void updatePlayerColor() {
        String colorStr = (String) playerColorComboBox.getSelectedItem();
        char humanColor = colorStr.equals("白方") ? 'W' : 'B';
        if (boardPanel != null) {
            boardPanel.setHumanPlayer(humanColor);
        }
    }
    
    /**
     * 启动游戏
     */
    private void startGame() {
        if (boardPanel != null) {
            System.out.println("🎮 启动游戏: " + currentGameMode + ", isAIvsAIMode: " + isAIvsAIMode);
            System.out.println("🎮 aiVsAIRadio.isSelected(): " + aiVsAIRadio.isSelected());
            
            // 重置游戏状态
            startNewGame();
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            isPaused = false;
            
            // 延迟执行以确保棋盘已重新创建
            SwingUtilities.invokeLater(() -> {
                // 如果是AI对AI模式，初始化AI对AI
                if (isAIvsAIMode || aiVsAIRadio.isSelected()) {
                    System.out.println("🤖 初始化AI对AI模式...");
                    initializeAIvsAI();
                } else if (playerVsAIRadio.isSelected()) {
                    // 自动启用AI对弈
                    if (aiToggleButton.getText().equals("启用AI对弈")) {
                        toggleAI();
                    }
                }
            });
            
            updateStatus("游戏已启动 - 当前模式: " + currentGameMode);
        }
    }

    /**
     * 暂停游戏
     */
    private void pauseGame() {
        if (boardPanel != null) {
            isPaused = !isPaused;
            if (isPaused) {
                pauseButton.setText("继续游戏");
                startButton.setEnabled(false);
                
                // 调用棋盘面板的暂停功能
                boardPanel.pauseGame();
                
                // 停止AI对AI定时器
                if (aiVsAiTimer != null) {
                    aiVsAiTimer.stop();
                }
                
                System.out.println("⏸️ 游戏已暂停");
            } else {
                pauseButton.setText("暂停游戏");
                startButton.setEnabled(false);
                
                // 调用棋盘面板的恢复功能
                boardPanel.resumeGame();
                
                System.out.println("▶️ 游戏已恢复");
            }
        }
    }

    /**
     * 退出游戏
     */
    private void quitGame() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "确定要退出游戏吗？",
            "退出确认",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // 停止所有定时器
            if (aiVsAiTimer != null) {
                aiVsAiTimer.stop();
            }
            
            // 清理资源
            if (chatPanel != null) {
                chatPanel.setEnabled(false);
            }
            
            // 关闭窗口
            dispose();
            System.exit(0);
        }
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatusDisplay() {
        String modeInfo = " [" + currentGameMode + "]";
        String aiStatus = "";
        
        if ("玩家对AI".equals(currentGameMode)) {
            aiStatus = " (AI已启用)";
        } else if ("AI对AI".equals(currentGameMode)) {
            aiStatus = " (AI自动对弈)";
        }
        
        statusLabel.setText("当前游戏" + modeInfo + aiStatus);
    }
    
    /**
     * 自动启用AI对弈（供外部调用）
     */
    public void autoEnableAI() {
        // 延迟执行，确保界面完全初始化
        SwingUtilities.invokeLater(() -> {
            toggleAI(); // 调用toggleAI方法启用AI
        });
    }
    
    /**
     * 主方法 - 启动国际象棋游戏
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InternationalChessFrame frame = new InternationalChessFrame();
            frame.setVisible(true);
        });
    }
}