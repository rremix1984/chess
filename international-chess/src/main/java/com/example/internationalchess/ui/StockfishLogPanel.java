package com.example.internationalchess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Stockfish决策日志面板
 * 显示Stockfish引擎的思考过程和决策信息
 */
public class StockfishLogPanel extends JPanel {
    
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private JButton clearButton;
    private boolean isEnabled;
    
    public StockfishLogPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 0));
        setBorder(BorderFactory.createTitledBorder("🤖 Stockfish 决策日志"));
        
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        // 创建日志文本区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 248, 255));
        logArea.setForeground(new Color(25, 25, 112));
        logArea.setMargin(new Insets(5, 5, 5, 5));
        logArea.setWrapStyleWord(true);
        logArea.setLineWrap(true);
        
        // 创建滚动面板
        scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 创建清除按钮
        clearButton = new JButton("清除日志");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });
        
        // 初始化状态
        isEnabled = true;
    }
    
    private void setupLayout() {
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(clearButton);
        
        // 添加组件
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 添加日志消息
     */
    public void addLog(String message) {
        if (!isEnabled) return;
        
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String logEntry = String.format("[%s] %s%n", timestamp, message);
            logArea.append(logEntry);
            
            // 自动滚动到底部
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * 添加Stockfish引擎输出日志
     */
    public void addEngineOutput(String output) {
        if (!isEnabled) return;
        
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            // 解析不同类型的引擎输出
            if (output.startsWith("info depth")) {
                // 只记录重要深度信息（深度 >= 10 且是5的倍数，或者最终深度）
                String[] parts = output.split(" ");
                int depth = -1;
                
                // 先获取深度值
                for (int i = 0; i < parts.length; i++) {
                    if ("depth".equals(parts[i]) && i + 1 < parts.length) {
                        try {
                            depth = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            // 忽略解析错误
                        }
                        break;
                    }
                }
                
                // 只记录重要深度：5的倍数且 >= 10
                if (depth > 0 && (depth >= 10 && depth % 5 == 0)) {
                    StringBuilder info = new StringBuilder();
                    
                    for (int i = 0; i < parts.length; i++) {
                        String part = parts[i];
                        switch (part) {
                            case "depth":
                                if (i + 1 < parts.length) {
                                    info.append("深度: ").append(parts[i + 1]).append(" ");
                                }
                                break;
                            case "score":
                                if (i + 2 < parts.length && "cp".equals(parts[i + 1])) {
                                    double score = Integer.parseInt(parts[i + 2]) / 100.0;
                                    info.append("评分: ").append(String.format("%.2f", score)).append(" ");
                                } else if (i + 2 < parts.length && "mate".equals(parts[i + 1])) {
                                    info.append("将死: ").append(parts[i + 2]).append("步 ");
                                }
                                break;
                            case "pv":
                                // 主要变化（只显示前3步）
                                StringBuilder pv = new StringBuilder("主线: ");
                                for (int j = i + 1; j < Math.min(i + 4, parts.length); j++) {
                                    pv.append(parts[j]).append(" ");
                                }
                                info.append(pv.toString().trim());
                                break;
                        }
                    }
                    
                    if (info.length() > 0) {
                        String logEntry = String.format("[%s] 🧠 %s%n", timestamp, info.toString().trim());
                        logArea.append(logEntry);
                    }
                }
                
            } else if (output.startsWith("bestmove")) {
                // 最佳移动
                String[] parts = output.split(" ");
                if (parts.length >= 2) {
                    String bestMove = parts[1];
                    String logEntry = String.format("[%s] ✅ 最佳移动: %s%n", timestamp, bestMove);
                    logArea.append(logEntry);
                }
                
            } else if (output.contains("uciok")) {
                String logEntry = String.format("[%s] 🔗 Stockfish引擎已连接%n", timestamp);
                logArea.append(logEntry);
                
            } else if (output.contains("readyok")) {
                String logEntry = String.format("[%s] ⚡ Stockfish引擎就绪%n", timestamp);
                logArea.append(logEntry);
                
            } else if (output.startsWith("Stockfish")) {
                String logEntry = String.format("[%s] 🏆 %s%n", timestamp, output);
                logArea.append(logEntry);
                
            } else {
                // 其他输出
                String logEntry = String.format("[%s] 📋 %s%n", timestamp, output);
                logArea.append(logEntry);
            }
            
            // 自动滚动到底部
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * 添加系统状态日志
     */
    public void addStatusLog(String status) {
        if (!isEnabled) return;
        
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = String.format("[%s] 🔔 %s%n", timestamp, status);
        addLog(logEntry);
    }
    
    /**
     * 添加错误日志
     */
    public void addErrorLog(String error) {
        if (!isEnabled) return;
        
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String logEntry = String.format("[%s] ❌ 错误: %s%n", timestamp, error);
            logArea.append(logEntry);
            
            // 自动滚动到底部
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * 清除日志
     */
    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            logArea.setText("");
            addLog("日志已清除");
        });
    }
    
    /**
     * 设置面板启用状态
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        super.setEnabled(enabled);
        logArea.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        
        if (enabled) {
            setBorder(BorderFactory.createTitledBorder("🤖 Stockfish 决策日志 [启用]"));
            addLog("Stockfish日志面板已启用");
        } else {
            setBorder(BorderFactory.createTitledBorder("🤖 Stockfish 决策日志 [禁用]"));
        }
    }
    
    /**
     * 检查是否启用
     */
    public boolean isLogEnabled() {
        return isEnabled;
    }
    
    /**
     * 添加游戏事件日志
     */
    public void addGameEvent(String event) {
        if (!isEnabled) return;
        
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = String.format("[%s] 🎮 %s%n", timestamp, event);
        addLog(logEntry);
    }
    
    /**
     * 添加AI决策日志
     */
    public void addAIDecision(String decision) {
        if (!isEnabled) return;
        
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = String.format("[%s] 🤖 %s%n", timestamp, decision);
        addLog(logEntry);
    }
}
