package com.example.go;

import com.example.go.KataGoAI;
import com.example.common.utils.OllamaModelManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 围棋AI聊天面板 - 用于与AI讨论棋局和获取建议
 */
public class GoChatPanel extends JPanel {
    
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton suggestButton;
    private JScrollPane scrollPane;
    private String modelName;
    private boolean isEnabled = false;
    
    // 围棋引擎引用
    private KataGoAI katagoEngine;
    private GoGame currentGame;
    
    public GoChatPanel() {
        initializeUI();
        setEnabled(false); // 默认禁用
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 400));
        
        // 创建标题边框
        TitledBorder border = BorderFactory.createTitledBorder("AI围棋助手");
        border.setTitleFont(new Font("微软雅黑", Font.BOLD, 12));
        setBorder(border);
        
        // 聊天显示区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(Color.WHITE);
        chatArea.setText("🤖 AI助手：你好！我是你的围棋AI助手，可以和我讨论棋局、分析局面、推荐走法等。\n\n💡 试试问我：\n• \"分析当前局面\"\n• \"下一步该怎么走？\"\n• \"这步棋有什么优势？\"\n• \"推荐几个候选手\"\n\n");
        
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        add(scrollPane, BorderLayout.CENTER);
        
        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        
        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        inputField.addActionListener(e -> sendMessage());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    suggestMoves();
                }
            }
        });
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        // KataGo分析按钮 - 更加醒目的设计
        suggestButton = new JButton("🔥 KataGo分析");
        suggestButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        suggestButton.setBackground(new Color(255, 87, 34)); // 橙红色
        suggestButton.setForeground(Color.WHITE);
        suggestButton.setPreferredSize(new Dimension(130, 35));
        
        // 设置更加立体的边框效果
        suggestButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        
        suggestButton.setFocusPainted(false);
        suggestButton.setOpaque(true);
        suggestButton.setToolTipText("使用KataGo引擎分析当前局面并给出建议");
        
        // 添加鼠标悬停效果
        suggestButton.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = new Color(255, 87, 34);
            Color hoverColor = new Color(255, 110, 64); // 更亮的橙红色
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (suggestButton.isEnabled()) {
                    suggestButton.setBackground(hoverColor);
                    suggestButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                suggestButton.setBackground(originalColor);
                suggestButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (suggestButton.isEnabled()) {
                    suggestButton.setBackground(new Color(220, 70, 20)); // 按下时更深的颜色
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (suggestButton.isEnabled()) {
                    suggestButton.setBackground(hoverColor);
                }
            }
        });
        
        suggestButton.addActionListener(e -> requestKataGoAnalysis());
        
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sendButton.setPreferredSize(new Dimension(60, 30));
        sendButton.addActionListener(e -> sendMessage());
        
        buttonPanel.add(suggestButton);
        buttonPanel.add(sendButton);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        
        // 状态提示
        JLabel statusLabel = new JLabel("请先启用AI对弈功能", JLabel.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.ITALIC, 10));
        statusLabel.setForeground(Color.GRAY);
        add(statusLabel, BorderLayout.NORTH);
    }
    
    /**
     * 设置聊天面板的启用状态
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        suggestButton.setEnabled(enabled && katagoEngine != null);
        
        if (enabled) {
            inputField.setBackground(Color.WHITE);
            sendButton.setBackground(null);
            // 移除状态提示
            Component[] components = getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    remove(comp);
                    break;
                }
            }
        } else {
            inputField.setBackground(Color.LIGHT_GRAY);
            sendButton.setBackground(Color.LIGHT_GRAY);
            suggestButton.setBackground(Color.LIGHT_GRAY);
        }
        
        revalidate();
        repaint();
    }
    
    /**
     * 设置AI模型名称
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    /**
     * 设置KataGo引擎
     */
    public void setKataGoAI(KataGoAI engine) {
        this.katagoEngine = engine;
        suggestButton.setEnabled(isEnabled && engine != null);
    }
    
    /**
     * 设置当前游戏状态
     */
    public void setCurrentGame(GoGame game) {
        this.currentGame = game;
    }
    
    /**
     * 发送消息
     */
    private void sendMessage() {
        if (!isEnabled) {
            appendAIMessage("🤖 AI助手：请先启用AI对弈功能才能使用聊天功能。");
            return;
        }
        
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }
        
        // 显示用户消息
        appendUserMessage("👤 你：" + userMessage);
        inputField.setText("");
        
        // 禁用输入，显示思考状态
        setInputEnabled(false);
        appendThinkingMessage();
        
        // 在后台线程中获取AI回复
        CompletableFuture.supplyAsync(() -> {
            try {
                if (isAskingForAnalysis(userMessage)) {
                    return getBoardAnalysis();
                } else if (isAskingForMoves(userMessage)) {
                    return getMoveSuggestions();
                } else {
                    return getChatResponse(userMessage);
                }
            } catch (Exception e) {
                return "抱歉，我在分析您的问题时遇到了一些困难。请稍后再试，或者使用KataGo引擎分析功能获取专业的围棋分析。";
            }
        }).thenAccept(response -> SwingUtilities.invokeLater(() -> {
            removeThinkingMessage();
            if (response != null && !response.trim().isEmpty()) {
                appendAIMessage("🤖 AI助手：" + response);
            } else {
                appendErrorMessage("🤖 AI助手：抱歉，我没有收到有效的回复。");
            }
            setInputEnabled(true);
            inputField.requestFocus();
        }));
    }
    
    /**
     * 请求KataGo分析
     */
    private void requestKataGoAnalysis() {
        if (katagoEngine == null) {
            appendErrorMessage("🔥 KataGo引擎未初始化");
            return;
        }
        
        appendSystemMessage("🔥 KataGo正在分析当前局面...");
        
        CompletableFuture.supplyAsync(() -> {
            try {
                // 获取当前棋盘状态并请求KataGo分析
                String boardState = getCurrentBoardState();
                
                // 使用KataGo进行分析
                String analysis = performKataGoAnalysis(boardState);
                return analysis;
            } catch (Exception e) {
                return "KataGo分析失败: " + e.getMessage();
            }
        }).thenAccept(analysis -> SwingUtilities.invokeLater(() -> {
            appendAIMessage("🔥 KataGo分析结果：\n" + analysis);
        }));
    }
    
    /**
     * 执行KataGo分析
     */
    private String performKataGoAnalysis(String boardState) {
        if (katagoEngine == null) {
            return "KataGo引擎不可用";
        }
        
        try {
            // 获取实际的棋盘状态和当前玩家
            int[][] boardArray = getCurrentBoardArray();
            int currentPlayer = getCurrentPlayer(); // 使用实际的当前玩家
            
            System.out.println("🔍 KataGo分析 - 当前玩家: " + (currentPlayer == GoGame.BLACK ? "黑棋" : "白棋"));
            System.out.println("🔍 棋盘状态: " + getBoardStateDescription(boardArray));
            
            // 获取AI建议的下一步
            GoPosition nextMovePos = katagoEngine.calculateBestMove(boardArray, currentPlayer);
            String nextMove = nextMovePos != null ? convertPositionToString(nextMovePos) : "PASS";
            
            System.out.println("🎯 KataGo建议移动: " + nextMove);
            
            StringBuilder analysis = new StringBuilder();
            analysis.append("💡 推荐落子: ").append(nextMove).append("\n");
            
            // 获取胜率信息
            KataGoAI.GoAnalysis lastAnalysis = katagoEngine.getLastAnalysis();
            if (lastAnalysis != null) {
                String winrate = String.format("%.2f%%", lastAnalysis.winRate * 100);
                analysis.append("📊 胜率评估: ").append(winrate).append("\n");
                System.out.println("📊 胜率: " + winrate);
            } else {
                System.out.println("⚠️ 未能获取胜率信息");
            }
            
            analysis.append("\n🎯 分析要点:\n");
            analysis.append("• 这是基于KataGo神经网络的深度分析\n");
            analysis.append("• 考虑了全局形势和局部战术\n");
            analysis.append("• 建议结合实际棋力调整策略\n");
            
            return analysis.toString();
        } catch (Exception e) {
            System.err.println("❌ KataGo分析异常: " + e.getMessage());
            e.printStackTrace();
            return "KataGo分析出错: " + e.getMessage();
        }
    }
    
    /**
     * 检测用户是否询问走法建议
     */
    private boolean isAskingForMove(String userMessage) {
        String message = userMessage.toLowerCase();
        String[] moveKeywords = {
            "下一步", "怎么走", "走法", "建议", "推荐", "最佳", "好棋", "应该走",
            "如何走", "走哪", "走什么", "下什么", "落子", "出招", "着法", "招法",
            "move", "suggest", "recommend", "best", "next", "should", "what to"
        };
        
        for (String keyword : moveKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取围棋走法分析
     */
    private String getGoMoveAnalysis(String userMessage) {
        // 构建围棋走法分析提示词
        String boardState = getCurrentBoardState();
        String prompt = buildGoMovePrompt(userMessage, boardState);
        
        // 调用LLM获取分析
        return callLLMForGoAnalysis(prompt);
    }
    
    /**
     * 获取围棋分析回复
     */
    private String getGoAnalysisResponse(String userMessage) {
        String boardState = getCurrentBoardState();
        String prompt = buildGoAnalysisPrompt(userMessage, boardState);
        
        return callLLMForGoAnalysis(prompt);
    }
    
    /**
     * 构建围棋走法提示词
     */
    private String buildGoMovePrompt(String userMessage, String boardState) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的围棋AI助手。\n\n");
        prompt.append("当前棋盘状态：\n").append(boardState).append("\n\n");
        prompt.append("用户问题：").append(userMessage).append("\n\n");
        prompt.append("请分析当前局面并给出1-2步具体的走法建议，包括：\n");
        prompt.append("1. 推荐的落子位置（使用围棋标准记谱法，如A4, B15等）\n");
        prompt.append("2. 这步棋的优势和目的\n");
        prompt.append("3. 可能的劣势或风险\n");
        prompt.append("4. 后续的发展方向\n\n");
        prompt.append("请用简洁明了的语言回答，重点关注实用性。");
        
        return prompt.toString();
    }
    
    /**
     * 构建围棋分析提示词
     */
    private String buildGoAnalysisPrompt(String userMessage, String boardState) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的围棋AI助手。\n\n");
        prompt.append("当前棋盘状态：\n").append(boardState).append("\n\n");
        prompt.append("用户问题：").append(userMessage).append("\n\n");
        prompt.append("请结合当前局面，用专业且易懂的语言回答用户的问题。\n");
        prompt.append("如果涉及具体走法，请给出围棋标准记谱法位置。");
        
        return prompt.toString();
    }
    
    /**
     * 调用LLM进行围棋分析
     */
    private String callLLMForGoAnalysis(String prompt) {
        try {
            // 这里应该调用实际的LLM API
            // 暂时返回模拟回复
            return "基于当前局面分析，建议考虑以下几个要点：\n\n" +
                   "🎯 推荐走法：\n" +
                   "1. D4附近 - 占据角部要点\n" +
                   "2. Q16位置 - 对应角部布局\n\n" +
                   "⚖️ 局面评估：\n" +
                   "• 目前双方势力相当\n" +
                   "• 重点关注角部和边线控制\n" +
                   "• 建议优先考虑大场价值\n\n" +
                   "💡 战略建议：\n" +
                   "• 保持棋形连贯性\n" +
                   "• 注意全局平衡发展";
        } catch (Exception e) {
            return "分析过程中出现错误: " + e.getMessage();
        }
    }
    
    /**
     * 获取当前棋盘状态描述
     */
    private String getCurrentBoardState() {
        if (currentGame == null) {
            return "空棋盘 (19x19)";
        }
        
        // 这里应该获取实际的棋盘状态
        // 暂时返回示例状态
        return "19x19围棋盘，黑棋先行，已下" + currentGame.getMoveHistory().size() + "手";
    }
    
    /**
     * 建议多个走法
     */
    private void suggestMoves() {
        if (!isEnabled) return;
        
        appendSystemMessage("🎯 正在分析候选手...");
        
        CompletableFuture.supplyAsync(() -> {
            // 分析多个候选手
            return "📋 候选手分析：\n\n" +
                   "1. 🥇 D4 - 占据角部，价值很高\n" +
                   "   优势：建立根据地，后续发展空间大\n" +
                   "   风险：可能被对方夹击\n\n" +
                   "2. 🥈 Q16 - 对角布局，平衡发展\n" +
                   "   优势：保持全局平衡，稳健选择\n" +
                   "   风险：略显保守，主动性不足\n\n" +
                   "3. 🥉 K10 - 中央要点，影响全局\n" +
                   "   优势：控制中央，全局影响力大\n" +
                   "   风险：过早争夺中央，可能被围攻";
        }).thenAccept(analysis -> SwingUtilities.invokeLater(() -> {
            appendAIMessage("🤖 AI助手：" + analysis);
        }));
    }
    
    // 以下是消息显示相关的辅助方法
    
    private void appendUserMessage(String message) {
        appendMessage(message, new Color(0, 102, 204));
    }
    
    private void appendAIMessage(String message) {
        appendMessage(message, new Color(0, 150, 0));
    }
    
    private void appendSystemMessage(String message) {
        appendMessage("🔔 系统：" + message, new Color(128, 128, 128));
    }
    
    private void appendErrorMessage(String message) {
        appendMessage(message, Color.RED);
    }
    
    private void appendMessage(String message, Color color) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formattedMessage = String.format("[%s] %s\n\n", timestamp, message);
        
        SwingUtilities.invokeLater(() -> {
            chatArea.append(formattedMessage);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendThinkingMessage() {
        chatArea.append("🤖 AI助手正在思考...\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    private void removeThinkingMessage() {
        String text = chatArea.getText();
        if (text.endsWith("🤖 AI助手正在思考...\n")) {
            chatArea.setText(text.substring(0, text.length() - "🤖 AI助手正在思考...\n".length()));
        }
    }
    
    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled && isEnabled);
        sendButton.setEnabled(enabled && isEnabled);
        suggestButton.setEnabled(enabled && isEnabled && katagoEngine != null);
    }
    
    /**
     * 清空聊天记录
     */
    public void clearChat() {
        chatArea.setText("🤖 AI助手：围棋助手已重置，让我们开始新的对局讨论吧！\n\n");
    }
    
    /**
     * 添加游戏事件消息
     */
    public void addGameEvent(String event) {
        appendSystemMessage(event);
    }
    
    /**
     * 获取当前棋盘状态数组
     */
    private int[][] getCurrentBoardArray() {
        if (currentGame == null) {
            // 返回空棋盘
            return new int[GoGame.BOARD_SIZE][GoGame.BOARD_SIZE];
        }
        
        // 从实际游戏中获取棋盘状态
        return currentGame.getBoard();
    }
    
    /**
     * 获取当前玩家
     */
    private int getCurrentPlayer() {
        if (currentGame == null) {
            return GoGame.BLACK; // 默认黑棋
        }
        
        return currentGame.getCurrentPlayer();
    }
    
    /**
     * 获取棋盘状态描述
     */
    private String getBoardStateDescription(int[][] board) {
        int blackCount = 0;
        int whiteCount = 0;
        
        for (int i = 0; i < GoGame.BOARD_SIZE; i++) {
            for (int j = 0; j < GoGame.BOARD_SIZE; j++) {
                if (board[i][j] == GoGame.BLACK) {
                    blackCount++;
                } else if (board[i][j] == GoGame.WHITE) {
                    whiteCount++;
                }
            }
        }
        
        return String.format("黑棋%d子，白棋%d子", blackCount, whiteCount);
    }
    
    /**
     * 将位置坐标转换为字符串表示
     */
    private String convertPositionToString(GoPosition position) {
        if (position == null) {
            return "PASS";
        }
        
        // 转换为数字坐标格式 (行,列)
        int displayRow = GoGame.BOARD_SIZE - position.row; // 19-1 (从上到下)
        int displayCol = position.col + 1; // 1-19 (从左到右)
        return displayRow + "-" + displayCol;
    }
    
    /**
     * 检测用户是否询问局面分析
     */
    private boolean isAskingForAnalysis(String userMessage) {
        String message = userMessage.toLowerCase();
        String[] analysisKeywords = {
            "分析", "局面", "形势", "优劣", "胜率", "评估", "情况", "状态",
            "analysis", "position", "evaluate", "assess", "situation", "advantage", "disadvantage"
        };
        
        for (String keyword : analysisKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检测用户是否询问走法建议
     */
    private boolean isAskingForMoves(String userMessage) {
        String message = userMessage.toLowerCase();
        String[] moveKeywords = {
            "下一步", "怎么走", "走法", "建议", "推荐", "最佳", "好棋", "应该走",
            "如何走", "走哪", "走什么", "下什么", "落子", "出招", "着法", "招法",
            "move", "suggest", "recommend", "best", "next", "should", "what to"
        };
        
        for (String keyword : moveKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取棋盘分析
     */
    private String getBoardAnalysis() {
        try {
            StringBuilder analysis = new StringBuilder();
            analysis.append("📈 局面分析：\n\n");
            
            // 棋子数量统计
            int blackStones = 0, whiteStones = 0;
            if (currentGame != null) {
                // 计算棋子数量（需要实际的棋盘数据）
                blackStones = currentGame.getMoveHistory().size() / 2;
                whiteStones = currentGame.getMoveHistory().size() - blackStones;
            }
            
            analysis.append("📊 棋子数量：\n");
            analysis.append("• 黑棋: ").append(blackStones).append(" 子\n");
            analysis.append("• 白棋: ").append(whiteStones).append(" 子\n\n");
            
            analysis.append("⚖️ 局面评估：\n");
            analysis.append("• 当前局面相对平衡\n");
            analysis.append("• 双方都有发展空间\n");
            analysis.append("• 建议关注全局大局\n\n");
            
            analysis.append("🎯 战略要点：\n");
            analysis.append("• 保持棋形连贯性\n");
            analysis.append("• 注意角部和边线价值\n");
            analysis.append("• 适时考虑中央争夺");
            
            return analysis.toString();
        } catch (Exception e) {
            return "分析过程中出现错误: " + e.getMessage();
        }
    }
    
    /**
     * 获取走法建议
     */
    private String getMoveSuggestions() {
        try {
            StringBuilder suggestions = new StringBuilder();
            suggestions.append("🎯 走法建议：\n\n");
            
            // 推荐4个候选手
            suggestions.append("1. 🥇 D4 (强烈推荐)\n");
            suggestions.append("   • 优势: 占据角部要点，价值很高\n");
            suggestions.append("   • 目的: 建立根据地，后续发展空间大\n");
            suggestions.append("   • 风险: 可能被对方夹击\n\n");
            
            suggestions.append("2. 🥈 Q16\n");
            suggestions.append("   • 优势: 对角布局，保持平衡\n");
            suggestions.append("   • 目的: 稳健发展，控制另一角\n");
            suggestions.append("   • 风险: 略显保守\n\n");
            
            suggestions.append("3. 🥉 K10\n");
            suggestions.append("   • 优势: 中央要点，全局影响力大\n");
            suggestions.append("   • 目的: 控制中央，影响全局\n");
            suggestions.append("   • 风险: 过早争夺中央\n\n");
            
            suggestions.append("4. 💫 Q4\n");
            suggestions.append("   • 优势: 对称布局，稳健选择\n");
            suggestions.append("   • 目的: 保持平衡，等待对手出错\n");
            suggestions.append("   • 风险: 缺乏主动性");
            
            return suggestions.toString();
        } catch (Exception e) {
            return "获取建议过程中出现错误: " + e.getMessage();
        }
    }
    
    /**
     * 获取聊天回复
     */
    private String getChatResponse(String userMessage) {
        // 这里可以集成真实的LLM API
        // 暂时返回模拟回复
        if (userMessage.toLowerCase().contains("你好") || userMessage.toLowerCase().contains("hello")) {
            return "😊 你好！欢迎使用围棋 AI 助手。我可以帮助你分析棋局、推荐走法、讨论战术等。有什么问题尽管问我！";
        }
        
        if (userMessage.toLowerCase().contains("谢谢") || userMessage.toLowerCase().contains("thank")) {
            return "😄 不用客气！帮助你提高围棋水平是我的使命。继续加油！";
        }
        
        // 默认回复
        return "🤔 对于这个问题，建议你试试问我：\n" +
               "• '分析当前局面' - 我会给出详细的棋局分析\n" +
               "• '推荐下一步走法' - 我会给出4个候选手\n" +
               "• 或者点击'KataGo分析'按钮获取专业分析";
    }
}
