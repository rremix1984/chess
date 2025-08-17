package com.example.chinesechess.ui;

import com.example.common.utils.ExceptionHandler;
import com.example.chinesechess.config.ChineseChessConfig;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI决策日志面板
 * 用于显示AI的思考过程和决策日志
 */
public class AILogPanel extends JPanel {
    
    private JTextPane logArea;
    private StyledDocument document;
    private boolean enabled = false;
    private final AtomicInteger logCount = new AtomicInteger(0);
    private static final int MAX_LOG_ENTRIES = 1000; // 最大日志条目数
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public AILogPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("🤖 AI决策日志"));
        setPreferredSize(ChineseChessConfig.CONTROL_PANEL_SIZE);
        
        // 创建日志显示区域
        logArea = new JTextPane();
        logArea.setEditable(false);
        logArea.setFont(ChineseChessConfig.DEFAULT_FONT);
        logArea.setBackground(ChineseChessConfig.CHAT_BACKGROUND_COLOR);
        
        document = logArea.getStyledDocument();
        
        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // 创建控制按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton clearButton = new JButton("清空日志");
        clearButton.setPreferredSize(new Dimension(80, 25));
        clearButton.setFont(ChineseChessConfig.DEFAULT_FONT);
        clearButton.addActionListener(e -> clearLog());
        buttonPanel.add(clearButton);
        
        JButton exportButton = new JButton("导出日志");
        exportButton.setPreferredSize(new Dimension(80, 25));
        exportButton.setFont(ChineseChessConfig.DEFAULT_FONT);
        exportButton.addActionListener(e -> exportLog());
        buttonPanel.add(exportButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 初始状态为禁用
        setEnabled(false);
        
        // 添加初始提示
        addLogMessage("AI决策日志", "等待AI启用...", Color.GRAY);
    }
    
    /**
     * 添加日志消息
     */
    public void addLogMessage(String title, String message, Color color) {
        if (!enabled && !title.equals("AI决策日志")) {
            return; // 如果未启用且不是初始消息，则不显示
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                // 检查日志条目数量，防止内存溢出
                if (logCount.get() >= MAX_LOG_ENTRIES) {
                    clearOldLogs();
                }
                
                // 创建样式
                SimpleAttributeSet titleStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(titleStyle, color);
                StyleConstants.setBold(titleStyle, true);
                
                SimpleAttributeSet messageStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(messageStyle, color);
                
                // 添加时间戳
                String timestamp = LocalTime.now().format(TIME_FORMATTER);
                
                // 插入日志内容
                if (document.getLength() > 0) {
                    document.insertString(document.getLength(), "\n", messageStyle);
                }
                
                document.insertString(document.getLength(), 
                    "[" + timestamp + "] " + title + ": ", titleStyle);
                document.insertString(document.getLength(), message, messageStyle);
                
                // 增加日志计数
                logCount.incrementAndGet();
                
                // 自动滚动到底部
                logArea.setCaretPosition(document.getLength());
                
            } catch (BadLocationException e) {
                ExceptionHandler.handleException(e, "添加日志消息", false);
            }
        });
    }
    
    /**
     * 智能添加AI决策日志（自动识别红方/黑方并使用对应颜色）
     */
    public void addAIDecisionWithColorDetection(String message) {
        if (!enabled) {
            return;
        }
        
        String title;
        Color textColor;
        
        // 自动检测消息中的方色信息
        if (message.contains("红方") || message.contains("红")) {
            title = "红方AI";
            textColor = Color.RED; // 红方用红色
        } else if (message.contains("黑方") || message.contains("黑")) {
            title = "黑方AI";
            textColor = Color.BLACK; // 黑方用黑色
        } else {
            title = "AI决策";
            textColor = new Color(0, 153, 0); // 其他信息用绿色
        }
        
        addLogMessage(title, message, textColor);
    }
    
    /**
     * 添加红方AI决策日志
     */
    public void addRedPlayerDecision(String message) {
        addLogMessage("红方AI", message, Color.RED);
    }
    
    /**
     * 添加黑方AI决策日志
     */
    public void addBlackPlayerDecision(String message) {
        addLogMessage("黑方AI", message, Color.BLACK);
    }
    
    /**
     * 添加一般信息（绿色）
     */
    public void addGeneralInfo(String message) {
        addLogMessage("系统信息", message, new Color(0, 153, 0));
    }
    
    /**
     * 清理旧日志条目
     */
    private void clearOldLogs() {
        try {
            // 保留最后500条日志
            String text = document.getText(0, document.getLength());
            String[] lines = text.split("\n");
            
            if (lines.length > 500) {
                document.remove(0, document.getLength());
                
                // 重新插入最后500条日志
                for (int i = lines.length - 500; i < lines.length; i++) {
                    if (i > lines.length - 500) {
                        document.insertString(document.getLength(), "\n", null);
                    }
                    document.insertString(document.getLength(), lines[i], null);
                }
                
                logCount.set(500);
                ExceptionHandler.logInfo("清理了旧日志条目，保留最新500条", "日志面板");
            }
        } catch (BadLocationException e) {
            ExceptionHandler.handleException(e, "清理旧日志", false);
        }
    }
    
    /**
     * 导出日志
     */
    private void exportLog() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出AI决策日志");
            fileChooser.setSelectedFile(new java.io.File("ai_log_" + 
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
                    writer.write(document.getText(0, document.getLength()));
                    ExceptionHandler.logInfo("日志已导出到: " + file.getAbsolutePath(), "日志面板");
                    JOptionPane.showMessageDialog(this, "日志导出成功！\n文件位置: " + file.getAbsolutePath(), 
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "导出日志", true);
        }
    }
    
    /**
     * 添加AI思考日志
     */
    public void addAIThinking(String thinking) {
        addLogMessage("AI思考", thinking, new Color(0, 102, 204));
    }
    
    /**
     * 添加AI决策日志
     */
    public void addAIDecision(String decision) {
        addLogMessage("AI决策", decision, new Color(0, 153, 0));
    }
    
    /**
     * 添加错误日志
     */
    public void addError(String error) {
        addLogMessage("错误", error, new Color(204, 0, 0));
    }
    
    /**
     * 添加系统日志
     */
    public void addSystemLog(String log) {
        addLogMessage("系统", log, new Color(102, 102, 102));
    }
    
    /**
     * 清空日志
     */
    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            try {
                document.remove(0, document.getLength());
                logCount.set(0);
                if (enabled) {
                    addLogMessage("系统", "日志已清空", Color.GRAY);
                } else {
                    addLogMessage("AI决策日志", "等待AI启用...", Color.GRAY);
                }
                ExceptionHandler.logInfo("AI决策日志已清空", "日志面板");
            } catch (BadLocationException e) {
                ExceptionHandler.handleException(e, "清空日志", false);
            }
        });
    }
    
    /**
     * 启用/禁用日志面板
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        super.setEnabled(enabled);
        
        if (enabled) {
            logArea.setBackground(Color.WHITE);
            addSystemLog("AI决策日志已启用");
            ExceptionHandler.logInfo("AI决策日志面板已启用", "日志面板");
        } else {
            logArea.setBackground(ChineseChessConfig.CHAT_BACKGROUND_COLOR);
            clearLog();
            ExceptionHandler.logInfo("AI决策日志面板已禁用", "日志面板");
        }
    }
    
    /**
     * 获取当前日志条目数
     */
    public int getLogCount() {
        return logCount.get();
    }
    
    /**
     * 获取日志文本内容
     */
    public String getLogText() {
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException e) {
            ExceptionHandler.handleException(e, "获取日志文本", false);
            return "";
        }
    }
    
    /**
     * 检查是否已启用
     */
    public boolean isLogEnabled() {
        return enabled;
    }
}