package com.example.chinesechess.ui;

import com.example.chinesechess.core.Board;
import com.example.chinesechess.core.GameState;
import com.example.chinesechess.core.Piece;
import com.example.chinesechess.core.PieceColor;
import com.example.chinesechess.core.Position;
// 移除了Gson和OkHttp相关的import

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AI聊天面板 - 用于与AI讨论棋局
 */
public class ChatPanel extends JPanel {
    
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private Object board;
    // 移除了五子棋棋盘引用
    private JComboBox<String> pikafishDifficultyComboBox; // Pikafish难度选择
    private JComboBox<String> fairyStockfishDifficultyComboBox; // Fairy-Stockfish难度选择
    // 棋盘面板引用已移除，简化实现
    private Object boardPanel; // 棋盘面板引用，用于显示推荐走法标记
    private Object gameFrame; // GameFrame引用，用于获取神经网络文件路径
    private String modelName;
    private boolean isEnabled;
    
    // HTTP客户端配置
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String GENERATE_ENDPOINT = "/api/generate";
    
    public ChatPanel() {
        initializeUI();
        setEnabled(false); // 默认禁用
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        // 象棋模式：右侧布局，设置合适的宽度和高度
        setPreferredSize(new Dimension(300, 400));
        
        // 创建标题边框
        TitledBorder border = BorderFactory.createTitledBorder("AI棋局讨论");
        border.setTitleFont(new Font("宋体", Font.BOLD, 12));
        setBorder(border);
        
        // 聊天显示区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(Color.WHITE);
        chatArea.setText("🤖 AI助手：你好！我是你的象棋AI助手，可以和我讨论棋局、分析局面、推荐走法等。\n");
        
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        add(scrollPane, BorderLayout.CENTER);
        
        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        inputField.addActionListener(e -> sendMessage());
        
        // AI分析面板 - 纵向布局
        JPanel analysisPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        analysisPanel.setBorder(BorderFactory.createTitledBorder("AI引擎分析"));
        
        // Pikafish分析面板
        JPanel pikafishPanel = createEngineAnalysisPanel(
            "🐟 Pikafish", new Color(30, 144, 255), 
            "让Pikafish引擎评估当前棋局并给出建议",
            e -> requestPikafishEvaluation()
        );
        
        // 创建Pikafish难度选择
        pikafishDifficultyComboBox = new JComboBox<>(new String[]{
            "1-简单", "2-普通", "3-困难", "4-专家", "5-大师",
            "6-特级", "7-超级", "8-顶级", "9-传奇", "10-神级"
        });
        pikafishDifficultyComboBox.setSelectedIndex(2); // 默认选择困难
        pikafishDifficultyComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        pikafishDifficultyComboBox.setPreferredSize(new Dimension(75, 22));
        pikafishDifficultyComboBox.setToolTipText("选择Pikafish引擎的思考深度，难度越高计算越深入");
        pikafishPanel.add(pikafishDifficultyComboBox);
        
        // Fairy-Stockfish分析面板
        JPanel fairyPanel = createEngineAnalysisPanel(
            "🧚 Fairy", new Color(34, 139, 34), 
            "让Fairy-Stockfish引擎评估当前棋局并给出建议",
            e -> requestFairyStockfishEvaluation()
        );
        
        // 创建Fairy-Stockfish难度选择
        fairyStockfishDifficultyComboBox = new JComboBox<>(new String[]{
            "1-简单", "2-普通", "3-困难", "4-专家", "5-大师",
            "6-特级", "7-超级", "8-顶级", "9-传奇", "10-神级"
        });
        fairyStockfishDifficultyComboBox.setSelectedIndex(2); // 默认选择困难
        fairyStockfishDifficultyComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        fairyStockfishDifficultyComboBox.setPreferredSize(new Dimension(75, 22));
        fairyStockfishDifficultyComboBox.setToolTipText("选择Fairy-Stockfish引擎的思考深度，难度越高搜索越深");
        fairyPanel.add(fairyStockfishDifficultyComboBox);
        
        // 添加两个分析面板
        analysisPanel.add(pikafishPanel);
        analysisPanel.add(fairyPanel);
        
        // 聊天发送面板
        JPanel chatPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sendButton.setPreferredSize(new Dimension(60, 25));
        sendButton.addActionListener(e -> sendMessage());
        chatPanel.add(sendButton);
        
        // 按钮总面板
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(analysisPanel, BorderLayout.NORTH);
        buttonPanel.add(chatPanel, BorderLayout.SOUTH);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        
        // 状态提示
        JLabel statusLabel = new JLabel("请先启用AI对弈功能", JLabel.CENTER);
        statusLabel.setFont(new Font("宋体", Font.ITALIC, 10));
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
        }
        
        revalidate();
        repaint();
    }
    
    /**
     * 创建引擎分析面板
     */
    private JPanel createEngineAnalysisPanel(String engineName, Color bgColor, String tooltip, ActionListener actionListener) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        
        // 创建按钮
        JButton button = new JButton(engineName);
        button.setFont(new Font("微软雅黑", Font.BOLD, 11));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(85, 25));
        button.addActionListener(actionListener);
        
        panel.add(button);
        return panel;
    }
    
    /**
     * 设置棋盘引用
     */
    public void setBoard(Board board) {
        this.board = board;
    }
    
    // 移除了五子棋棋盘引用相关方法
    
    /**
     * 更新棋盘状态（当棋盘发生变化时调用）
     */
    public void updateBoardState(Board newBoard) {
        this.board = newBoard;
        // 可以在这里添加其他需要在棋盘状态更新时执行的逻辑
    }
    
    /**
     * 设置AI模型名称
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    /**
     * 发送消息
     */
    private void sendMessage() {
        if (!isEnabled || board == null || modelName == null || modelName.isEmpty()) {
            System.out.println("警告: 聊天功能未启用或配置不完整");
            return;
        }
        
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }
        
        System.out.println("发送用户消息: " + userMessage);
        
        // 显示用户消息
        appendUserMessage("👤 你：" + userMessage);
        inputField.setText("");
        
        // 禁用输入，显示思考状态
        setInputEnabled(false);
        appendThinkingMessage();
        
        // 在后台线程中获取AI回复
        new Thread(() -> {
            try {
                // 开始AI响应计时
                String response = getAIResponse(userMessage);
                // 结束AI响应计时
                
                SwingUtilities.invokeLater(() -> {
                    removeThinkingMessage();
                    if (response != null && !response.trim().isEmpty()) {
                        appendAIMessage("🤖 AI助手：" + response);
                        System.out.println("收到AI回复: " + response.substring(0, Math.min(50, response.length())) + "...");
                    } else {
                        appendErrorMessage("🤖 AI助手：抱歉，我没有收到有效的回复。");
                    }
                    setInputEnabled(true);
                    inputField.requestFocus();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    removeThinkingMessage();
                    handleChatError(e);
                    setInputEnabled(true);
                    inputField.requestFocus();
                });
            }
        }).start();
    }
    
    /**
     * 处理聊天错误
     */
    private void handleChatError(Throwable throwable) {
        String errorMessage = "🤖 AI助手：抱歉，我现在无法回复。";
        
        if (throwable instanceof java.net.ConnectException) {
            errorMessage += "请检查Ollama服务是否正在运行。";
            System.err.println("网络异常: " + throwable.getMessage());
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            errorMessage += "请求超时，请稍后重试。";
            System.err.println("网络超时: " + throwable.getMessage());
        } else {
            errorMessage += "请检查网络连接和AI服务状态。";
            System.err.println("聊天AI请求异常: " + throwable.getMessage());
        }
        
        appendErrorMessage(errorMessage);
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
     * 获取AI回复
     */
    private String getAIResponse(String userMessage) throws IOException {
        try {
            // 检查用户是否询问走法建议
            if (isAskingForMove(userMessage)) {
                return getDeepMoveAnalysis(userMessage);
            }
            
            // 获取当前棋盘状态
            String boardState = getBoardStateDescription();
            
            // 构建深度分析提示词
            String prompt = buildDeepAnalysisPrompt(userMessage, boardState);
            
            // 调用DeepSeek模型进行深度分析
            return callDeepSeekForAnalysis(prompt);
            
        } catch (Exception e) {
            System.err.println("AI回复生成失败: " + e.getMessage());
            return "抱歉，我在分析您的问题时遇到了一些困难。请稍后再试，或者使用Pikafish引擎评估功能获取专业的棋局分析。";
        }
    }
    
    /**
     * 获取深度走法分析
     */
    private String getDeepMoveAnalysis(String userMessage) {
        if (board == null) {
            return "请先开始游戏，我才能为您分析走法。";
        }
        
        try {
            // 创建DeepSeekPikafishAI实例进行深度分析
            com.example.chinesechess.ai.DeepSeekPikafishAI analyzer = new com.example.chinesechess.ai.DeepSeekPikafishAI(
                com.example.chinesechess.core.PieceColor.RED, // 默认颜色
                8, // 高难度分析
                "deepseek-r1:7b" // 使用DeepSeek模型
            );
            
            // 获取Pikafish引擎分析
            String pikafishAnalysis = analyzer.evaluateGameAndGiveAdvice(
                (com.example.chinesechess.core.Board) board, 
                com.example.chinesechess.core.PieceColor.RED
            );
            
            // 获取当前棋盘状态
            String boardState = getBoardStateDescription();
            
            // 构建深度走法分析提示词
            String prompt = String.format(
                "你是一位象棋大师，正在为玩家提供深度的走法分析。\n\n" +
                "当前棋盘状态：\n%s\n\n" +
                "Pikafish引擎分析：\n%s\n\n" +
                "玩家问题：%s\n\n" +
                "请基于Pikafish引擎的专业分析，为玩家提供深度的走法建议，包括：\n" +
                "1. 🎯 **推荐走法**：详细说明最佳的2-3个走法选择\n" +
                "2. 🧠 **战略思考**：解释每个走法背后的战略意图\n" +
                "3. ✅ **优势分析**：说明选择这些走法的优势和好处\n" +
                "4. ⚠️ **风险评估**：指出可能的风险和需要注意的地方\n" +
                "5. 🔮 **后续计划**：预测对手可能的应对和我方的后续策略\n" +
                "6. 💡 **学习要点**：从这个局面中可以学到的象棋原理\n\n" +
                "请用专业但易懂的语言，让玩家不仅知道怎么走，更要理解为什么这样走。",
                boardState, pikafishAnalysis, userMessage
            );
            
            // 调用DeepSeek模型
            String deepAnalysis = callDeepSeekForAnalysis(prompt);
            
            // 清理资源
            analyzer.shutdown();
            
            return "🤖 **AI象棋大师深度分析**\n\n" + deepAnalysis + 
                   "\n\n💡 **提示**：以上分析结合了Pikafish引擎的精确计算和AI的战略思维，为您提供最专业的走法建议。";
            
        } catch (Exception e) {
            System.err.println("深度走法分析失败: " + e.getMessage());
            return "抱歉，深度分析功能暂时不可用。请使用下方的'🐟 Pikafish评估'按钮获取专业的引擎分析。";
        }
    }
    
    /**
     * 构建深度分析提示词
     */
    private String buildDeepAnalysisPrompt(String userMessage, String boardState) {
        return String.format(
            "你是一位象棋大师，正在为玩家提供深度的棋局分析。\n\n" +
            "当前棋盘状态：\n%s\n\n" +
            "玩家问题：%s\n\n" +
            "请提供深度的分析，包括：\n" +
            "1. 🎯 **局面评估**：当前局面的优劣势分析，子力对比\n" +
            "2. 🧠 **战略思考**：接下来的战略方向，是攻是守\n" +
            "3. ✅ **战术机会**：寻找战术组合和攻击机会\n" +
            "4. ⚠️ **防守要点**：需要注意的防守薄弱环节\n" +
            "5. 🔮 **形势判断**：预测局面的发展趋势\n" +
            "6. 💡 **学习要点**：从当前局面学到的象棋原理\n\n" +
            "请使用标准象棋术语，用专业但易懂的语言，帮助玩家深入理解象棋的精髓。",
            boardState, userMessage
        );
    }
    
    /**
     * 调用DeepSeek模型进行分析
     */
    private String callDeepSeekForAnalysis(String prompt) {
        try {
            // 创建临时的DeepSeekPikafishAI实例来调用DeepSeek模型
            com.example.chinesechess.ai.DeepSeekPikafishAI tempAI = new com.example.chinesechess.ai.DeepSeekPikafishAI(
                com.example.chinesechess.core.PieceColor.RED,
                5, // 中等难度即可
                "deepseek-r1:7b"
            );
            
            // 使用AI的内部方法调用DeepSeek（需要添加公共方法）
            String response = tempAI.callDeepSeekAPI(prompt);
            
            // 清理资源
            tempAI.shutdown();
            
            if (response != null && !response.trim().isEmpty()) {
                return response;
            } else {
                return getFallbackResponse(prompt);
            }
            
        } catch (Exception e) {
            System.err.println("DeepSeek API调用失败: " + e.getMessage());
            return getFallbackResponse(prompt);
        }
    }
    
    /**
     * 获取备用回复（当DeepSeek不可用时）
     */
    private String getFallbackResponse(String prompt) {
        // 根据提示词内容提供智能的备用回复
        if (prompt.contains("走法") || prompt.contains("下一步")) {
            return "🤖 **AI分析**\n\n" +
                   "当前我无法连接到DeepSeek模型，但我建议您：\n\n" +
                   "1. 🎯 **使用Pikafish引擎**：点击下方的'🐟 Pikafish评估'按钮获取专业的引擎分析\n" +
                   "2. 🧠 **基本原则**：优先考虑将帅安全，然后寻找攻击机会\n" +
                   "3. ✅ **稳妥策略**：在不确定时选择稳健的走法\n" +
                   "4. ⚠️ **避免失误**：仔细检查是否会被对方反击\n\n" +
                   "💡 Pikafish引擎能为您提供最准确的走法计算和局面评估。";
        } else {
            return "🤖 **AI助手**\n\n" +
                   "抱歉，我暂时无法提供深度分析。不过我可以为您提供一些基本建议：\n\n" +
                   "• 📚 **学习建议**：多研究经典棋谱，提高棋感\n" +
                   "• 🎯 **实战技巧**：注重子力协调，避免孤军深入\n" +
                   "• 🛡️ **防守要点**：时刻关注将帅安全\n" +
                   "• ⚔️ **攻击原则**：寻找对方薄弱环节\n\n" +
                   "如需专业分析，请使用Pikafish引擎评估功能。";
        }
    }
    
    /**
     * 构建聊天提示词
     */
    private String buildChatPrompt(String userMessage, String boardState) {
        // 仅支持中国象棋模式
        boolean isGomoku = false;
        
        if (isGomoku) {
            return String.format(
                "你是一个专业的五子棋AI助手，正在和玩家讨论当前的棋局。\n\n" +
                "当前棋盘状态：\n%s\n\n" +
                "玩家问题：%s\n\n" +
                "请以友好、专业的语气回答玩家的问题。你的回答应该：\n" +
                "1. 针对当前五子棋棋局给出具体分析\n" +
                "2. 用通俗易懂的语言解释五子棋策略和技巧\n" +
                "3. 分析攻防要点，如连三、活四、冲四等\n" +
                "4. 保持对话的连贯性和趣味性\n" +
                "5. 回答长度控制在200字以内\n" +
                "6. 注意：如果玩家询问具体走法建议，请提醒他们使用专门的Pikafish引擎评估功能获取更准确的走法分析\n\n" +
                "请直接回答，不要重复问题。",
                boardState, userMessage);
        } else {
            return String.format(
                "你是一个专业的中国象棋AI助手，正在和玩家讨论当前的棋局。\n\n" +
                "当前棋盘状态：\n%s\n\n" +
                "玩家问题：%s\n\n" +
                "请严格使用标准中国象棋术语回答，你的回答应该：\n" +
                "1. 使用正确的记谱格式：棋子名称 + 纵线位置 + 移动方向(进/退/平) + 目标数字\n" +
                "   - 红方使用汉字纵线：一、二、三、四、五、六、七、八、九(从右到左)\n" +
                "   - 黑方使用数字纵线：1、2、3、4、5、6、7、8、9(从左到右)\n" +
                "   - 直线棋子(车、炮、兵、将)：进/退表示步数，平表示目标纵线\n" +
                "   - 斜线棋子(马、相、仕)：数字直接表示落点纵线\n" +
                "2. 同线多子区分：使用'前'、'后'、'中'、'二'、'三'等区分同一纵线的相同棋子\n" +
                "3. 棋子移动规则术语：\n" +
                "   - 车：沿直线走任意格，'进退'指步数，'平'指目标纵线\n" +
                "   - 马：走'日'字形，注意'绊马脚'，数字表示落点纵线\n" +
                "   - 象/相：走'田'字形，不能过河，注意'塞象眼'，数字表示落点纵线\n" +
                "   - 士/仕：宫内斜走一步，数字表示落点纵线\n" +
                "   - 炮：平时如车，吃子需'炮架'，'进退'指步数，'平'指目标纵线\n" +
                "   - 兵/卒：未过河只能进，过河可进可平，数字含义同车\n" +
                "   - 将/帅：宫内直横走一格，不能'将对将'\n" +
                "4. 使用专业术语分析局面：如'攻杀'、'防守'、'子力配置'、'阵型'等\n" +
                "5. 保持对话的连贯性和教学性，回答长度控制在250字以内\n" +
                "6. 重要提醒：如果玩家询问具体走法建议，请明确告知他们应该使用专门的Pikafish引擎评估功能，因为只有Pikafish引擎才能提供准确的最佳走法计算，而不是依赖大模型的推测\n\n" +
                "请直接回答，严格遵循中国象棋标准术语体系。",
                boardState, userMessage);
        }
    }
    
    /**
     * 获取棋盘状态描述
     */
    private String getBoardStateDescription() {
        // 仅支持中国象棋
        
        if (board == null) {
            return "棋盘状态未知";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 添加标准象棋术语说明
        sb.append("=== 中国象棋棋盘状态 ===\n");
        sb.append("纵线标记：红方(九八七六五四三二一) 黑方(1 2 3 4 5 6 7 8 9)\n");
        sb.append("横线标记：1-10(从上到下)\n\n");
        
        // 纵线标记(红方视角从右到左)
        sb.append("   九八七六五四三二一\n");
        sb.append("   9 8 7 6 5 4 3 2 1\n");
        
        for (int row = 0; row < 10; row++) {
            sb.append(String.format("%2d ", row + 1));
            for (int col = 0; col < 9; col++) {
                try {
                    if (board instanceof com.example.chinesechess.core.Board) {
                        Piece piece = ((com.example.chinesechess.core.Board) board).getPiece(row, col);
                        if (piece == null) {
                            sb.append("口 ");
                        } else {
                            String name = piece.getChineseName();
                            if (piece.getColor() == PieceColor.RED) {
                                sb.append("红").append(name);
                            } else {
                                sb.append("黑").append(name);
                            }
                            sb.append(" ");
                        }
                    } else {
                        sb.append("口 ");
                    }
                } catch (Exception e) {
                    sb.append("? ");
                }
            }
            sb.append("\n");
        }
        
        // 添加当前局面的术语描述
        sb.append("\n=== 局面分析要点 ===\n");
        sb.append("请使用标准记谱格式分析走法：棋子+纵线+方向+数字\n");
        sb.append("如：马二进三、车一平四、炮八进二、兵六进一等\n");
        
        return sb.toString();
    }
    
    // 移除了五子棋棋盘状态描述方法
    
    /**
     * 添加消息到聊天区域
     */
    private void appendMessage(String message) {
        if (!isEnabled()) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = chatArea.getDocument();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, new Color(51, 51, 51));
                
                if (doc.getLength() == 0) {
                    doc.insertString(0, message, attributes);
                } else {
                    doc.insertString(doc.getLength(), "\n\n" + message, attributes);
                }
                
                // 滚动到底部
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 添加用户消息
     */
    private void appendUserMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = chatArea.getDocument();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, new Color(51, 51, 51));
                StyleConstants.setBold(attributes, false);
                
                String formattedMessage = (doc.getLength() == 0 ? "" : "\n\n") + message;
                doc.insertString(doc.getLength(), formattedMessage, attributes);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 添加AI思考中消息
     */
    private void appendThinkingMessage() {
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = chatArea.getDocument();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, new Color(128, 128, 128));
                StyleConstants.setItalic(attributes, true);
                
                doc.insertString(doc.getLength(), "\n\n🤖 AI助手：正在分析棋局...", attributes);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 添加AI回复消息
     */
    private void appendAIMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = chatArea.getDocument();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, new Color(0, 102, 204));
                StyleConstants.setBold(attributes, false);
                
                doc.insertString(doc.getLength(), "\n\n" + message, attributes);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 添加错误消息
     */
    private void appendErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = chatArea.getDocument();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, new Color(204, 0, 0));
                StyleConstants.setBold(attributes, false);
                
                doc.insertString(doc.getLength(), "\n\n" + message, attributes);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 移除思考中消息
     */
    private void removeThinkingMessage() {
        SwingUtilities.invokeLater(() -> {
            try {
                String text = chatArea.getText();
                String thinkingText1 = "🤖 AI助手：正在分析棋局...";
                String thinkingText2 = "🐟 Pikafish引擎：正在深度分析棋局...";
                
                int lastIndex1 = text.lastIndexOf(thinkingText1);
                int lastIndex2 = text.lastIndexOf(thinkingText2);
                int lastIndex = Math.max(lastIndex1, lastIndex2);
                
                if (lastIndex != -1) {
                    // 查找前面的换行符
                    int startIndex = lastIndex;
                    while (startIndex > 0 && text.charAt(startIndex - 1) == '\n') {
                        startIndex--;
                    }
                    chatArea.setText(text.substring(0, startIndex));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 设置输入控件的启用状态
     */
    private void setInputEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            inputField.setEnabled(enabled && isEnabled);
            sendButton.setEnabled(enabled && isEnabled);
        });
    }
    
    /**
     * 清空聊天记录
     */
    public void clearChat() {
        chatArea.setText("🤖 AI助手：你好！我是你的象棋AI助手。\n" +
                        "你可以和我讨论当前的棋局，我会为你分析局面、\n" +
                        "推荐走法、解释策略等。\n\n" +
                        "💡 提示：你可以问我：\n" +
                        "• 当前局面如何？\n" +
                        "• 我下一步应该怎么走？\n" +
                        "• 这步棋有什么风险？\n" +
                        "• 对方可能的策略是什么？\n\n");
    }
    
    /**
     * 添加消息到聊天区域（公开方法）
     */
    public void addChatMessage(String sender, String message) {
        if (!isEnabled()) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = chatArea.getDocument();
                // 优化消息格式，使其在底部布局中更易读
                String formattedMessage = "[" + sender + "]: " + message + "\n";
                
                // 根据发送者设置不同样式
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                if ("AI".equals(sender)) {
                    StyleConstants.setForeground(attributes, new Color(0, 102, 204)); // 蓝色
                    StyleConstants.setBold(attributes, true);
                } else {
                    StyleConstants.setForeground(attributes, new Color(51, 51, 51)); // 深灰色
                }
                
                doc.insertString(doc.getLength(), formattedMessage, attributes);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 添加消息到聊天区域（公开方法，单参数版本）
     */
    public void addChatMessage(String message) {
        appendMessage(message);
    }
    
    /**
     * 请求Pikafish评估
     */
    private void requestPikafishEvaluation() {
        if (!isEnabled || board == null) {
            appendErrorMessage("🐟 Pikafish评估：请先启用AI对弈功能并开始游戏。");
            return;
        }
        
        // 仅支持中国象棋
        
        // 获取用户选择的难度
        int selectedDifficulty = pikafishDifficultyComboBox.getSelectedIndex() + 1; // 1-10级难度
        String difficultyName = (String) pikafishDifficultyComboBox.getSelectedItem();
        
        System.out.println("用户请求Pikafish评估，难度: " + difficultyName);
        
        // 显示评估开始消息
        appendUserMessage("👤 你：请Pikafish引擎评估当前棋局（难度: " + difficultyName + "）");
        
        // 禁用输入，显示分析状态
        setInputEnabled(false);
        appendThinkingMessage("🐟 Pikafish引擎：正在进行" + difficultyName + "深度分析棋局...");
        
        // 在后台线程中处理Pikafish评估
        new Thread(() -> {
            try {
                // 实际调用DeepSeekPikafishAI进行分析
                if (board instanceof com.example.chinesechess.core.Board) {
                    // 获取神经网络文件路径
                    String neuralNetworkPath = null;
                    if (gameFrame != null) {
                        try {
                            java.lang.reflect.Method getSelectedNeuralNetworkPathMethod = 
                                gameFrame.getClass().getMethod("getSelectedNeuralNetworkPath");
                            neuralNetworkPath = (String) getSelectedNeuralNetworkPathMethod.invoke(gameFrame);
                            
                            if (neuralNetworkPath != null && !neuralNetworkPath.isEmpty()) {
                                System.out.println("🤖 Pikafish评估使用神经网络: " + neuralNetworkPath);
                            }
                        } catch (Exception e) {
                            System.err.println("无法获取神经网络文件路径: " + e.getMessage());
                        }
                    }
                    
                    // 创建DeepSeekPikafishAI实例，使用用户选择的难度
                    com.example.chinesechess.ai.DeepSeekPikafishAI analyzer = new com.example.chinesechess.ai.DeepSeekPikafishAI(
                        com.example.chinesechess.core.PieceColor.RED, // 默认颜色
                        selectedDifficulty, // 用户选择的难度
                        "deepseek-r1:7b" // 默认模型
                    );
                    
                    // 如果有神经网络文件，设置到Pikafish引擎
                    if (neuralNetworkPath != null && !neuralNetworkPath.isEmpty()) {
                        try {
                            // 获取DeepSeekPikafishAI的Pikafish引擎实例
                            java.lang.reflect.Field pikafishEngineField = analyzer.getClass().getDeclaredField("pikafishEngine");
                            pikafishEngineField.setAccessible(true);
                            Object pikafishEngine = pikafishEngineField.get(analyzer);
                            
                            if (pikafishEngine != null) {
                                java.lang.reflect.Method setNeuralNetworkPathMethod = 
                                    pikafishEngine.getClass().getMethod("setNeuralNetworkPath", String.class);
                                setNeuralNetworkPathMethod.invoke(pikafishEngine, neuralNetworkPath);
                                System.out.println("✅ 已设置Pikafish神经网络文件: " + neuralNetworkPath);
                            }
                        } catch (Exception e) {
                            System.err.println("设置Pikafish神经网络文件失败: " + e.getMessage());
                        }
                    }
                    
                    // 获取当前玩家颜色，如果无法确定则使用红方
                    com.example.chinesechess.core.PieceColor currentPlayer = com.example.chinesechess.core.PieceColor.RED;
                    // 注意：Board类没有getCurrentPlayer方法，这里使用默认的红方颜色
                    
                    // 使用新的详细评估方法
                    com.example.chinesechess.ai.DeepSeekPikafishAI.EvaluationResult result = 
                        analyzer.evaluateGameWithDetails((com.example.chinesechess.core.Board) board, currentPlayer);
                    
                    SwingUtilities.invokeLater(() -> {
                        removeThinkingMessage();
                        if (result != null && result.getRecommendedMoves() != null && !result.getRecommendedMoves().isEmpty()) {
                            // 获取最佳推荐走法
                            var bestMove = result.getRecommendedMoves().get(0);
                            String moveDescription = bestMove.getDescription();
                            
                            // 简化的分析结果，只显示核心建议
                            String simplifiedAnalysis = "🐟 **Pikafish引擎推荐**\n\n" +
                                                       "📍 **建议走法**: " + moveDescription;
                            
                            // 如果有多个候选走法，显示前3个
                            if (result.getRecommendedMoves().size() > 1) {
                                simplifiedAnalysis += "\n\n🎯 **其他选择**:";
                                int showCount = Math.min(3, result.getRecommendedMoves().size());
                                for (int i = 1; i < showCount; i++) {
                                    var alternativeMove = result.getRecommendedMoves().get(i);
                                    simplifiedAnalysis += "\n• " + alternativeMove.getDescription();
                                }
                            }
                            
                            appendAIMessage(simplifiedAnalysis);
                            
                            // 显示推荐走法的视觉标记
                            showRecommendedMoveHighlights(result);
                            
                            System.out.println("Pikafish评估完成，推荐走法数量: " + result.getRecommendedMoves().size());
                        } else {
                            appendErrorMessage("🐟 Pikafish引擎：抱歉，无法获取有效的评估结果。请确保引擎正常运行。");
                        }
                        
                        setInputEnabled(true);
                        inputField.requestFocus();
                    });
                    
                    // 清理资源
                    analyzer.shutdown();
                } else {
                    SwingUtilities.invokeLater(() -> {
                        removeThinkingMessage();
                        appendErrorMessage("🐟 Pikafish引擎：当前棋盘类型不支持Pikafish分析。");
                        setInputEnabled(true);
                        inputField.requestFocus();
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    removeThinkingMessage();
                    handlePikafishEvaluationError(e);
                    setInputEnabled(true);
                    inputField.requestFocus();
                });
            }
        }).start();
    }
    
    /**
     * 显示推荐走法的视觉标记
     */
    private void showRecommendedMoveHighlights(com.example.chinesechess.ai.DeepSeekPikafishAI.EvaluationResult result) {
        if (result == null || result.getRecommendedMoves().isEmpty() || boardPanel == null) {
            return;
        }
        
        // 获取最佳推荐走法（排名第一的）
        com.example.chinesechess.ai.DeepSeekPikafishAI.RecommendedMove bestMove = 
            result.getRecommendedMoves().get(0);
        
        if (bestMove.getStartPosition() != null && bestMove.getEndPosition() != null) {
            // 调用棋盘面板的AI建议标记功能
            try {
                // 通过反射调用BoardPanel的setAISuggestion方法
                java.lang.reflect.Method setAISuggestionMethod = boardPanel.getClass().getMethod(
                    "setAISuggestion", 
                    com.example.chinesechess.core.Position.class, 
                    com.example.chinesechess.core.Position.class
                );
                
                setAISuggestionMethod.invoke(boardPanel, 
                    bestMove.getStartPosition(), bestMove.getEndPosition());
                
                System.out.println("💡 显示推荐走法标记: " + bestMove.getDescription());
                
                // 在聊天面板中也添加一条提示消息
                appendAIMessage("💡 **走法提示**: 棋盘上已用蓝色和绿色标记显示推荐走法：" + bestMove.getDescription() + 
                              "\n🔹 蓝色圆圈标记需要移动的棋子\n🔸 绿色圆圈标记目标位置\n标记将在30秒后自动消失。");
                
            } catch (Exception e) {
                System.err.println("❌ 无法显示推荐走法标记: " + e.getMessage());
                // 降级方案：只在文字中提示
                appendAIMessage("💡 **推荐走法**: " + bestMove.getDescription() + 
                              " (起始位置: (" + (bestMove.getStartPosition().getX() + 1) + "," + 
                              (bestMove.getStartPosition().getY() + 1) + ") → 目标位置: (" + 
                              (bestMove.getEndPosition().getX() + 1) + "," + 
                              (bestMove.getEndPosition().getY() + 1) + "))");
            }
        }
    }
    
    /**
     * 处理Pikafish评估错误
     */
    private void handlePikafishEvaluationError(Throwable throwable) {
        String errorMessage = "🐟 Pikafish引擎：抱歉，评估过程中出现问题。";
        
        if (throwable instanceof InterruptedException) {
            errorMessage += "评估被中断，请稍后重试。";
        } else {
            errorMessage += "请检查引擎状态。";
        }
        
        appendErrorMessage(errorMessage);
        System.err.println("Pikafish评估错误: " + throwable.getMessage());
    }
    
    /**
     * 设置棋盘面板引用
     */
    public void setBoardPanel(Object boardPanel) {
        this.boardPanel = boardPanel;
        System.out.println("🎯 设置棋盘面板引用: " + (boardPanel != null ? boardPanel.getClass().getSimpleName() : "null"));
    }
    
    /**
     * 设置GameFrame引用
     */
    public void setGameFrame(Object gameFrame) {
        this.gameFrame = gameFrame;
        System.out.println("🎯 设置GameFrame引用: " + (gameFrame != null ? gameFrame.getClass().getSimpleName() : "null"));
    }
    
    /**
     * 添加思考中消息（带自定义文本）
     */
    private void appendThinkingMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = chatArea.getDocument();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, new Color(128, 128, 128));
                StyleConstants.setItalic(attributes, true);
                
                doc.insertString(doc.getLength(), "\n\n" + message, attributes);
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 请求Fairy-Stockfish评估
     */
    private void requestFairyStockfishEvaluation() {
        if (!isEnabled || board == null) {
            appendErrorMessage("🧚 Fairy-Stockfish评估：请先启用AI对弈功能并开始游戏。");
            return;
        }
        
        // 获取用户选择的难度
        int selectedDifficulty = fairyStockfishDifficultyComboBox.getSelectedIndex() + 1; // 1-10级难度
        String difficultyName = (String) fairyStockfishDifficultyComboBox.getSelectedItem();
        
        System.out.println("用户请求Fairy-Stockfish评估，难度: " + difficultyName);
        
        // 显示评估开始消息
        appendUserMessage("👤 你：请Fairy-Stockfish引擎分析当前棋局（难度: " + difficultyName + "）");
        
        // 禁用输入，显示分析状态
        setInputEnabled(false);
        appendThinkingMessage("🧚 Fairy-Stockfish引擎：正在进行" + difficultyName + "深度分析象棋棋局...");
        
        // 在后台线程中处理Fairy-Stockfish评估
        new Thread(() -> {
            try {
                // 实际调用FairyStockfishAI进行分析
                if (board instanceof com.example.chinesechess.core.Board) {
                    // 获取神经网络文件路径
                    String neuralNetworkPath = null;
                    if (gameFrame != null) {
                        try {
                            java.lang.reflect.Method getSelectedNeuralNetworkPathMethod = 
                                gameFrame.getClass().getMethod("getSelectedNeuralNetworkPath");
                            neuralNetworkPath = (String) getSelectedNeuralNetworkPathMethod.invoke(gameFrame);
                            
                            if (neuralNetworkPath != null && !neuralNetworkPath.isEmpty()) {
                                System.out.println("🧚 Fairy-Stockfish评估使用神经网络: " + neuralNetworkPath);
                            }
                        } catch (Exception e) {
                            System.err.println("无法获取神经网络文件路径: " + e.getMessage());
                        }
                    }
                    
                    // 创建FairyStockfishAI实例，使用用户选择的难度和神经网络文件
                    com.example.chinesechess.ai.FairyStockfishAI analyzer = new com.example.chinesechess.ai.FairyStockfishAI(
                        com.example.chinesechess.core.PieceColor.RED, // 默认颜色
                        selectedDifficulty, // 用户选择的难度
                        neuralNetworkPath // 神经网络文件路径
                    );
                    
                    if (neuralNetworkPath != null && !neuralNetworkPath.isEmpty()) {
                        System.out.println("✅ Fairy-Stockfish使用神经网络文件: " + neuralNetworkPath);
                    }
                    
                    // 获取当前玩家颜色，如果无法确定则使用红方
                    com.example.chinesechess.core.PieceColor currentPlayer = com.example.chinesechess.core.PieceColor.RED;
                    
                    // 获取最佳走法
                    com.example.chinesechess.core.Move bestMove = analyzer.getBestMove(
                        (com.example.chinesechess.core.Board) board
                    );
                    
                    SwingUtilities.invokeLater(() -> {
                        removeThinkingMessage();
                        if (bestMove != null) {
                            String moveDescription = formatMoveToChineseNotation(bestMove);
                            String analysis = "🧚 **Fairy-Stockfish引擎分析**\n\n" +
                                           "📍 **推荐走法**: " + moveDescription;
                            
                            appendAIMessage(analysis);
                            
                            // 显示推荐走法的视觉标记
                            showFairyStockfishMoveHighlights(bestMove);
                            
                            System.out.println("Fairy-Stockfish评估完成，推荐走法: " + moveDescription);
                        } else {
                            appendErrorMessage("🧚 Fairy-Stockfish引擎：抱歉，无法获取有效的评估结果。请确保引擎正常运行。");
                        }
                        
                        setInputEnabled(true);
                        inputField.requestFocus();
                    });
                    
                    // 清理资源
                    analyzer.cleanup();
                } else {
                    SwingUtilities.invokeLater(() -> {
                        removeThinkingMessage();
                        appendErrorMessage("🧚 Fairy-Stockfish引擎：当前棋盘类型不支持Fairy-Stockfish分析。");
                        setInputEnabled(true);
                        inputField.requestFocus();
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    removeThinkingMessage();
                    handleFairyStockfishEvaluationError(e);
                    setInputEnabled(true);
                    inputField.requestFocus();
                });
            }
        }).start();
    }
    
    /**
     * 格式化走法描述
     */
    private String formatMoveToChineseNotation(com.example.chinesechess.core.Move move) {
        if (move == null || move.getStart() == null || move.getEnd() == null || board == null) {
            return "未知走法";
        }

        try {
            if (board instanceof com.example.chinesechess.core.Board) {
                return ((com.example.chinesechess.core.Board) board).toChineseNotation(move);
            } else {
                return formatMoveDescription(move);
            }
        } catch (Exception e) {
            System.err.println("格式化为中国象棋记谱法时出错: " + e.getMessage());
            return formatMoveDescription(move); // 降级为简单描述
        }
    }

    private String formatMoveDescription(com.example.chinesechess.core.Move move) {
        if (move == null || move.getStart() == null || move.getEnd() == null) {
            return "未知走法";
        }
        
        try {
            // 获取起始和目标位置
            com.example.chinesechess.core.Position from = move.getStart();
            com.example.chinesechess.core.Position to = move.getEnd();
            
            // 简单的坐标描述
            String fromDesc = "(" + (from.getX() + 1) + "," + (from.getY() + 1) + ")";
            String toDesc = "(" + (to.getX() + 1) + "," + (to.getY() + 1) + ")";
            
            return fromDesc + " → " + toDesc;
        } catch (Exception e) {
            return "走法解析出错";
        }
    }
    
    /**
     * 显示Fairy-Stockfish推荐走法的视觉标记
     */
    private void showFairyStockfishMoveHighlights(com.example.chinesechess.core.Move move) {
        if (move == null || move.getStart() == null || move.getEnd() == null || boardPanel == null) {
            return;
        }
        
        try {
            // 通过反射调用BoardPanel的setAISuggestion方法
            java.lang.reflect.Method setAISuggestionMethod = boardPanel.getClass().getMethod(
                "setAISuggestion", 
                com.example.chinesechess.core.Position.class, 
                com.example.chinesechess.core.Position.class
            );
            
            setAISuggestionMethod.invoke(boardPanel, move.getStart(), move.getEnd());
            
            String moveDesc = formatMoveDescription(move);
            System.out.println("💡 显示Fairy-Stockfish推荐走法标记: " + moveDesc);
            
            // 简化后的提示信息
            // 不再显示额外的提示消息，只在棋盘上显示标记
            
        } catch (Exception e) {
            System.err.println("❌ 无法显示Fairy-Stockfish推荐走法标记: " + e.getMessage());
            // 降级方案：只在文字中提示
            String moveDesc = formatMoveDescription(move);
            appendAIMessage("💡 **推荐走法**: " + moveDesc);
        }
    }
    
    /**
     * 处理Fairy-Stockfish评估错误
     */
    private void handleFairyStockfishEvaluationError(Throwable throwable) {
        String errorMessage = "🧚 Fairy-Stockfish引擎：抱歉，评估过程中出现问题。";
        
        if (throwable instanceof InterruptedException) {
            errorMessage += "评估被中断，请稍后重试。";
        } else {
            errorMessage += "请检查引擎状态和安装。";
        }
        
        appendErrorMessage(errorMessage);
        System.err.println("Fairy-Stockfish评估错误: " + throwable.getMessage());
    }
    
    // 移除了五子棋棋盘适配器
}
