package com.example.gomoku.ui;

import com.example.common.utils.ExceptionHandler;
import com.example.launcher.GameSelectionFrame;
import com.example.common.utils.ResourceManager;
import com.example.common.utils.OllamaModelManager;
import com.example.gomoku.core.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 五子棋游戏界面
 */
public class GomokuFrame extends JFrame {

    private JLabel statusLabel;
    private GomokuBoardPanel boardPanel;
    private ChatPanel chatPanel;
    private JButton aiToggleButton;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> playerColorComboBox;
    private JComboBox<String> aiTypeComboBox;
    private JComboBox<String> modelComboBox;
    
    // 游戏模式相关
    private JComboBox<String> gameModeComboBox;
    private JButton startGameButton;
    private String currentGameMode = "玩家对玩家";
    private boolean isAIvsAIMode = false;
    private GomokuAI blackAI;
    private GomokuAI whiteAI;

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

        // 创建棋盘
        boardPanel = new GomokuBoardPanel();
        
        // 创建聊天面板
        chatPanel = new ChatPanel();
        
        // 设置BoardPanel的聊天面板引用
        boardPanel.setChatPanel(chatPanel);
        
        // 设置ChatPanel的五子棋棋盘引用
        chatPanel.setGomokuBoard(boardPanel.getBoard());
        
        // 创建主要内容面板（棋盘+聊天）
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        
        // 将聊天面板放在右侧
        chatPanel.setPreferredSize(new Dimension(GameConfig.CHAT_PANEL_WIDTH, GameConfig.CHAT_PANEL_HEIGHT));
        mainPanel.add(chatPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);

        // 创建控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // 创建状态栏
        statusLabel = new JLabel("⚫ 当前玩家: 黑方", JLabel.CENTER);
        statusLabel.setFont(GameConfig.TITLE_FONT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel.setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, 30));
        add(statusLabel, BorderLayout.SOUTH);

        // 设置BoardPanel的状态更新回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 默认启用大模型AI
        initializeDefaultAI();
        
        // 初始化游戏模式设置
        updateGameModeSettings();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🎮 五子棋对弈控制"));
        panel.setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, 80));

        // 左侧：基本设置（紧凑布局）
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // 游戏模式选择
        leftPanel.add(new JLabel("模式:"));
        String[] gameModes = {"玩家对玩家", "玩家对AI", "AI对AI"};
        gameModeComboBox = new JComboBox<>(gameModes);
        gameModeComboBox.setPreferredSize(new Dimension(100, 25));
        gameModeComboBox.setFont(GameConfig.DEFAULT_FONT);
        gameModeComboBox.addActionListener(e -> updateGameModeSettings());
        leftPanel.add(gameModeComboBox);
        
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
        aiTypeComboBox.setSelectedIndex(2); // 默认选择大模型AI
        aiTypeComboBox.setPreferredSize(new Dimension(80, 25));
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
            modelComboBox = new JComboBox<>(GameConfig.DEFAULT_MODELS.toArray(new String[0]));
            modelComboBox.setSelectedIndex(0);
        }
        modelComboBox.setPreferredSize(new Dimension(200, 25));
        modelComboBox.setFont(GameConfig.DEFAULT_FONT);
        leftPanel.add(modelComboBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：控制按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        // 启用/禁用AI按钮
        aiToggleButton = new JButton("启用AI");
        aiToggleButton.setFont(GameConfig.BUTTON_FONT);
        aiToggleButton.setPreferredSize(GameConfig.BUTTON_SIZE);
        aiToggleButton.addActionListener(e -> toggleAI());
        rightPanel.add(aiToggleButton);
        
        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.setFont(GameConfig.BUTTON_FONT);
        undoButton.setPreferredSize(GameConfig.BUTTON_SIZE);
        undoButton.addActionListener(e -> boardPanel.undoLastMove());
        rightPanel.add(undoButton);
        
        // 重新开始按钮
        JButton restartButton = new JButton("重新开始");
        restartButton.setFont(GameConfig.BUTTON_FONT);
        restartButton.setPreferredSize(GameConfig.BUTTON_SIZE);
        restartButton.addActionListener(e -> restartGame());
        rightPanel.add(restartButton);
        
        // 返回按钮
        JButton backButton = new JButton("返回选择");
        backButton.setFont(GameConfig.BUTTON_FONT);
        backButton.setPreferredSize(GameConfig.BUTTON_SIZE);
        backButton.addActionListener(e -> returnToSelection());
        rightPanel.add(backButton);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
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
        currentGameMode = (String) gameModeComboBox.getSelectedItem();
        
        switch (currentGameMode) {
            case "玩家对玩家":
                isAIvsAIMode = false;
                boardPanel.setAIEnabled(false);
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
                boardPanel.setAIEnabled(false); // 禁用原有AI，使用新的AI对AI逻辑
                aiToggleButton.setEnabled(false);
                playerColorComboBox.setEnabled(false);
                difficultyComboBox.setEnabled(true);
                aiTypeComboBox.setEnabled(true);
                modelComboBox.setEnabled(true);
                initializeAIvsAI();
                break;
        }
        
        updateStatus();
    }
    
    /**
     * 初始化AI对AI模式
     */
    private void initializeAIvsAI() {
        try {
            String aiType = (String) aiTypeComboBox.getSelectedItem();
            String difficulty = (String) difficultyComboBox.getSelectedItem();
            String model = (String) modelComboBox.getSelectedItem();
            
            if ("大模型AI".equals(aiType)) {
                blackAI = new GomokuLLMAI(difficulty, model); // 黑方
                whiteAI = new GomokuLLMAI(difficulty, model); // 白方
            } else {
                blackAI = new GomokuAI(difficulty);
                whiteAI = new GomokuAI(difficulty);
            }
            
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
                GomokuAI currentAI = boardPanel.getBoard().isBlackTurn() ? blackAI : whiteAI;
                if (currentAI != null) {
                    int[] move = currentAI.getNextMove(boardPanel.getBoard());
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
                
                boardPanel.setAIType(aiType, difficulty, modelName);
                
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
            
            boardPanel.setAIEnabled(!newState);
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
            boardPanel.setAIType(aiType, difficulty, modelName);
            
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
        boardPanel.setPlayerColor(isPlayerBlack);
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
            handleWindowClosing(); // 确保资源正确释放
            SwingUtilities.invokeLater(() -> {
                try {
                    GameSelectionFrame frame = new GameSelectionFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    ExceptionHandler.handleException(e, "打开游戏选择界面", true);
                }
            });
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "返回游戏选择", true);
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
                boardPanel.setAIEnabled(false);
            }
            if (chatPanel != null) {
                chatPanel.setEnabled(false);
            }
            
            // 关闭窗口
            dispose();
            
            // 如果这是最后一个窗口，退出应用程序
            if (Window.getWindows().length <= 1) {
                ExceptionHandler.logInfo("应用程序即将退出，正在清理资源...", "五子棋界面");
                ResourceManager.getInstance().shutdown();
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
        
        boardPanel.setAIType(aiType, difficulty, modelName);
        boardPanel.setAIEnabled(true);
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