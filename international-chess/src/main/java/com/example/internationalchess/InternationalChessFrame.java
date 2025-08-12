package com.example.internationalchess.ui;

import com.example.utils.OllamaModelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 国际象棋游戏界面
 */
public class InternationalChessFrame extends JFrame {

    private JLabel statusLabel;
    private InternationalBoardPanel boardPanel;
    private ChatPanel chatPanel;
    private AILogPanel aiLogPanel;
    private JButton aiToggleButton;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> playerColorComboBox;
    private JComboBox<String> aiTypeComboBox;
    private JComboBox<String> modelComboBox;

    public InternationalChessFrame() {
        setTitle("♟️ 国际象棋 - AI对弈版");
        setSize(1300, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout());

        // 创建棋盘
        boardPanel = new InternationalBoardPanel();
        
        // 创建聊天面板
        chatPanel = new ChatPanel();
        
        // 创建AI日志面板
        aiLogPanel = new AILogPanel();
        
        // 设置BoardPanel的聊天面板和AI日志面板引用
        boardPanel.setChatPanel(chatPanel);
        boardPanel.setAILogPanel(aiLogPanel);
        
        // 创建右侧面板（聊天+AI日志）
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatPanel, BorderLayout.NORTH);
        rightPanel.add(aiLogPanel, BorderLayout.CENTER);
        
        // 创建主要内容面板（棋盘+右侧面板）
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);

        // 创建控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // 创建状态栏
        statusLabel = new JLabel("⚪ 当前玩家: 白方", JLabel.CENTER);
        statusLabel.setFont(new Font("宋体", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel.setPreferredSize(new Dimension(1300, 30));
        add(statusLabel, BorderLayout.SOUTH);

        // 设置BoardPanel的状态更新回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 默认启用大模型AI
        initializeDefaultAI();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🎮 AI对弈控制"));
        panel.setPreferredSize(new Dimension(1300, 80));

        // 左侧：基本设置（紧凑布局）
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // 玩家颜色选择
        leftPanel.add(new JLabel("颜色:"));
        playerColorComboBox = new JComboBox<>(new String[]{"白方", "黑方"});
        playerColorComboBox.setPreferredSize(new Dimension(60, 25));
        leftPanel.add(playerColorComboBox);

        // AI类型选择
        leftPanel.add(new JLabel("AI:"));
        aiTypeComboBox = new JComboBox<>(new String[]{"传统AI", "增强AI", "大模型AI", "混合AI"});
        aiTypeComboBox.setSelectedIndex(2); // 默认选择大模型AI
        aiTypeComboBox.setPreferredSize(new Dimension(120, 25));
        aiTypeComboBox.addActionListener(e -> updateModelComboBox());
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
        modelComboBox.setPreferredSize(new Dimension(200, 25));
        leftPanel.add(modelComboBox);
        
        panel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：控制按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        
        // AI开关按钮
        aiToggleButton = new JButton("启用AI对弈");
        aiToggleButton.setPreferredSize(new Dimension(100, 30));
        aiToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleAI();
            }
        });
        rightPanel.add(aiToggleButton);
        
        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.setPreferredSize(new Dimension(60, 30));
        undoButton.addActionListener(e -> {
            if (boardPanel.canUndo()) {
                boardPanel.undoMove();
            } else {
                JOptionPane.showMessageDialog(this, "无法悔棋！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        rightPanel.add(undoButton);

        // 新游戏按钮
        JButton newGameButton = new JButton("新游戏");
        newGameButton.setPreferredSize(new Dimension(80, 30));
        newGameButton.addActionListener(e -> startNewGame());
        rightPanel.add(newGameButton);
        
        // 返回按钮
        JButton backButton = new JButton("返回选择");
        backButton.setPreferredSize(new Dimension(100, 30));
        backButton.addActionListener(e -> returnToSelection());
        rightPanel.add(backButton);
        
        panel.add(rightPanel, BorderLayout.EAST);

        // 初始化模型选择状态
        updateModelComboBox();

        return panel;
    }
    
    private void updateModelComboBox() {
        int aiTypeIndex = aiTypeComboBox.getSelectedIndex();
        boolean needsModel = (aiTypeIndex == 2) || (aiTypeIndex == 3); // 大模型AI 或 混合AI
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
            boolean enableChat = (aiTypeIndex == 2) || (aiTypeIndex == 3);
            
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
            String[] aiTypes = {"传统AI", "增强AI", "大模型AI", "混合AI"};
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
        // 移除旧的棋盘面板
        getContentPane().removeAll();
        
        // 创建新的棋盘面板
        boardPanel = new InternationalBoardPanel();
        
        // 重新创建聊天面板
        chatPanel = new ChatPanel();
        
        // 设置BoardPanel的聊天面板引用
        boardPanel.setChatPanel(chatPanel);
        
        // 重新添加组件
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // 创建主要内容面板（棋盘+聊天）
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(chatPanel, BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);
        
        add(statusLabel, BorderLayout.SOUTH);
        
        // 设置回调
        boardPanel.setStatusUpdateCallback(this::updateStatus);
        
        // 重置AI按钮状态
        aiToggleButton.setText("启用AI对弈");
        playerColorComboBox.setEnabled(true);
        difficultyComboBox.setEnabled(true);
        aiTypeComboBox.setEnabled(true);
        updateModelComboBox(); // 恢复模型选择状态
        
        // 确保AI被禁用
        boardPanel.setAIEnabled(false);
        
        // 刷新界面
        revalidate();
        repaint();
        
        updateStatus("当前玩家: 白方");
    }
    
    private void returnToSelection() {
        dispose(); // 关闭当前窗口
        SwingUtilities.invokeLater(() -> {
            GameSelectionFrame frame = new GameSelectionFrame();
            frame.setVisible(true);
        });
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

            // 启用聊天面板
            chatPanel.setEnabled(true);
            String modelName = (String) modelComboBox.getSelectedItem();
            chatPanel.setModelName(modelName);

            // 实际启用AI并设置人类玩家颜色
            boardPanel.setAIEnabled(true);
            // 默认人类玩家为白方，AI为黑方
            char humanColor = 'W'; // 白方
            boardPanel.setHumanPlayer(humanColor);

            // 设置AI类型为大模型AI
            aiTypeComboBox.setSelectedIndex(2); // "大模型AI"
            int difficulty = difficultyComboBox.getSelectedIndex() + 1; // 难度级别
            boardPanel.setAIType("大模型AI", difficulty, modelName);

            updateStatus("AI对弈已启用 - 大模型AI");
        });
    }

    public void updateStatus(String status) {
        statusLabel.setText(status);
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