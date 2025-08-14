package com.example.go;

import com.example.common.utils.ExceptionHandler;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 围棋AI决策日志面板
 * 实时显示AI的思考过程和决策分析
 */
public class GoAILogPanel extends JPanel {
    
    private JTextPane logTextPane;
    private StyledDocument logDocument;
    private JScrollPane logScrollPane;
    private JButton clearButton;
    private JButton exportButton;
    private JButton pauseButton;
    private JCheckBox autoScrollCheckBox;
    
    // 日志样式
    private Style normalStyle;
    private Style infoStyle;
    private Style errorStyle;
    private Style debugStyle;
    private Style highlightStyle;
    
    // 日志缓存和状态控制
    private final ConcurrentLinkedQueue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isEnabled = new AtomicBoolean(true);
    
    // 日志统计
    private int totalLogs = 0;
    private int infoLogs = 0;
    private int errorLogs = 0;
    private int debugLogs = 0;
    
    private Timer updateTimer;
    
    public GoAILogPanel() {
        initializeComponents();
        initializeStyles();
        setupLayout();
        setupEventHandlers();
        startUpdateTimer();
        
        // 初始化消息
        addLogEntry("📋 围棋AI决策日志面板已启用", LogLevel.INFO);
        addLogEntry("🤖 等待AI开始思考...", LogLevel.INFO);
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 创建日志文本面板
        logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        logTextPane.setFont(new Font("Consolas", Font.PLAIN, 11));
        logTextPane.setBackground(new Color(248, 248, 248));
        logDocument = logTextPane.getStyledDocument();
        
        // 创建滚动面板
        logScrollPane = new JScrollPane(logTextPane);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setPreferredSize(new Dimension(350, 400));
        
        // 创建控制按钮
        clearButton = new JButton("清空");
        clearButton.setToolTipText("清空所有日志");
        clearButton.setPreferredSize(new Dimension(60, 25));
        
        exportButton = new JButton("导出");
        exportButton.setToolTipText("导出日志到文件");
        exportButton.setPreferredSize(new Dimension(60, 25));
        
        pauseButton = new JButton("暂停");
        pauseButton.setToolTipText("暂停/继续日志显示");
        pauseButton.setPreferredSize(new Dimension(60, 25));
        
        // 自动滚动复选框
        autoScrollCheckBox = new JCheckBox("自动滚动", true);
        autoScrollCheckBox.setToolTipText("自动滚动到最新日志");
    }
    
    /**
     * 初始化文本样式
     */
    private void initializeStyles() {
        // 普通样式
        normalStyle = logTextPane.addStyle("normal", null);
        StyleConstants.setForeground(normalStyle, Color.BLACK);
        StyleConstants.setFontFamily(normalStyle, "Consolas");
        StyleConstants.setFontSize(normalStyle, 11);
        
        // 信息样式
        infoStyle = logTextPane.addStyle("info", normalStyle);
        StyleConstants.setForeground(infoStyle, new Color(0, 100, 0));
        
        // 错误样式
        errorStyle = logTextPane.addStyle("error", normalStyle);
        StyleConstants.setForeground(errorStyle, Color.RED);
        StyleConstants.setBold(errorStyle, true);
        
        // 调试样式
        debugStyle = logTextPane.addStyle("debug", normalStyle);
        StyleConstants.setForeground(debugStyle, Color.BLUE);
        StyleConstants.setItalic(debugStyle, true);
        
        // 高亮样式
        highlightStyle = logTextPane.addStyle("highlight", normalStyle);
        StyleConstants.setForeground(highlightStyle, new Color(128, 0, 128));
        StyleConstants.setBold(highlightStyle, true);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("🧠 AI决策分析"));
        
        // 主要内容区域
        add(logScrollPane, BorderLayout.CENTER);
        
        // 控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        controlPanel.add(clearButton);
        controlPanel.add(pauseButton);
        controlPanel.add(exportButton);
        controlPanel.add(autoScrollCheckBox);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // 统计面板
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);
    }
    
    /**
     * 创建统计面板
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel statsLabel = new JLabel("日志统计: 总计 0, 信息 0, 错误 0, 调试 0");
        statsLabel.setFont(new Font("宋体", Font.PLAIN, 10));
        panel.add(statsLabel);
        
        return panel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 清空按钮
        clearButton.addActionListener(e -> clearLogs());
        
        // 暂停按钮
        pauseButton.addActionListener(e -> togglePause());
        
        // 导出按钮
        exportButton.addActionListener(e -> exportLogs());
    }
    
    /**
     * 启动更新定时器
     */
    private void startUpdateTimer() {
        updateTimer = new Timer(100, e -> updateLogDisplay());
        updateTimer.start();
    }
    
    /**
     * 更新日志显示
     */
    private void updateLogDisplay() {
        if (isPaused.get() || logQueue.isEmpty()) {
            return;
        }
        
        boolean shouldScroll = autoScrollCheckBox.isSelected();
        int processedCount = 0;
        final int MAX_BATCH_SIZE = 10; // 批量处理以提高性能
        
        while (!logQueue.isEmpty() && processedCount < MAX_BATCH_SIZE) {
            LogEntry entry = logQueue.poll();
            if (entry != null) {
                appendLogEntry(entry);
                processedCount++;
                
                // 更新统计
                switch (entry.level) {
                    case INFO: infoLogs++; break;
                    case ERROR: errorLogs++; break;
                    case DEBUG: debugLogs++; break;
                }
            }
        }
        
        if (shouldScroll && processedCount > 0) {
            scrollToBottom();
        }
        
        // 更新统计信息
        updateStats();
    }
    
    /**
     * 添加日志条目（线程安全）
     */
    public void addLogEntry(String message, LogLevel level) {
        if (!isEnabled.get()) {
            return;
        }
        
        LogEntry entry = new LogEntry(message, level, System.currentTimeMillis());
        logQueue.offer(entry);
        
        // 通知ExceptionHandler记录日志
        switch (level) {
            case INFO:
                ExceptionHandler.logInfo("围棋AI", message);
                break;
            case ERROR:
                ExceptionHandler.logError("围棋AI", message);
                break;
            case DEBUG:
                ExceptionHandler.logDebug("围棋AI", message);
                break;
        }
    }
    
    /**
     * 附加日志条目到文本面板
     */
    private void appendLogEntry(LogEntry entry) {
        try {
            String timestamp = String.format("%tT", entry.timestamp);
            String fullMessage = String.format("[%s] %s\n", timestamp, entry.message);
            
            Style style = getStyleForLevel(entry.level);
            logDocument.insertString(logDocument.getLength(), fullMessage, style);
            
            // 限制日志长度
            limitLogLength();
            
        } catch (BadLocationException e) {
            System.err.println("添加日志条目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取日志级别对应的样式
     */
    private Style getStyleForLevel(LogLevel level) {
        switch (level) {
            case INFO: return infoStyle;
            case ERROR: return errorStyle;
            case DEBUG: return debugStyle;
            case HIGHLIGHT: return highlightStyle;
            default: return normalStyle;
        }
    }
    
    /**
     * 限制日志长度
     */
    private void limitLogLength() {
        try {
            final int MAX_LOG_LENGTH = 50000; // 最大字符数
            if (logDocument.getLength() > MAX_LOG_LENGTH) {
                int removeLength = logDocument.getLength() - MAX_LOG_LENGTH + 1000;
                logDocument.remove(0, removeLength);
                
                // 添加截断提示
                logDocument.insertString(0, "[日志已截断...保留最近记录]\n", normalStyle);
            }
        } catch (BadLocationException e) {
            System.err.println("限制日志长度失败: " + e.getMessage());
        }
    }
    
    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> 
            logTextPane.setCaretPosition(logDocument.getLength())
        );
    }
    
    /**
     * 清空日志
     */
    private void clearLogs() {
        try {
            logDocument.remove(0, logDocument.getLength());
            logQueue.clear();
            resetStats();
            addLogEntry("日志已清空", LogLevel.INFO);
        } catch (BadLocationException e) {
            System.err.println("清空日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换暂停状态
     */
    private void togglePause() {
        boolean paused = isPaused.get();
        isPaused.set(!paused);
        pauseButton.setText(paused ? "暂停" : "继续");
        addLogEntry(paused ? "日志显示已恢复" : "日志显示已暂停", LogLevel.INFO);
    }
    
    /**
     * 导出日志
     */
    private void exportLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("go_ai_log.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    writer.print(logTextPane.getText());
                }
                addLogEntry("日志已导出到: " + file.getAbsolutePath(), LogLevel.INFO);
            } catch (Exception e) {
                addLogEntry("导出日志失败: " + e.getMessage(), LogLevel.ERROR);
            }
        }
    }
    
    /**
     * 更新统计信息
     */
    private void updateStats() {
        totalLogs = infoLogs + errorLogs + debugLogs;
        
        // 获取统计面板中的标签
        JPanel statsPanel = (JPanel) getComponent(1); // North component
        Component[] components = statsPanel.getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            JLabel statsLabel = (JLabel) components[0];
            statsLabel.setText(String.format("日志统计: 总计 %d, 信息 %d, 错误 %d, 调试 %d", 
                totalLogs, infoLogs, errorLogs, debugLogs));
        }
    }
    
    /**
     * 重置统计信息
     */
    private void resetStats() {
        totalLogs = infoLogs = errorLogs = debugLogs = 0;
        updateStats();
    }
    
    /**
     * 启用/禁用日志面板
     */
    public void setLogEnabled(boolean enabled) {
        isEnabled.set(enabled);
        
        // 更新边框标题
        TitledBorder border = (TitledBorder) getBorder();
        border.setTitle(enabled ? "🧠 AI决策分析" : "🧠 AI决策分析 (已禁用)");
        repaint();
        
        if (enabled) {
            addLogEntry("AI决策日志面板已启用", LogLevel.INFO);
        } else {
            addLogEntry("AI决策日志面板已禁用", LogLevel.INFO);
        }
    }
    
    /**
     * 添加AI思考过程日志（简化版）
     */
    public void logAIThinking(String message) {
        // 只记录关键思考信息，过滤掉过于详细的分析
        if (message.contains("开始分析") || message.contains("胜率") || message.contains("推荐变化")) {
            addLogEntry("🧠 " + message, LogLevel.HIGHLIGHT);
        }
    }
    
    /**
     * 添加AI决策日志（简化版）
     */
    public void logAIDecision(String move, long thinkTime, String analysis) {
        // 简化决策日志，只显示最终结果
        String timeStr = thinkTime > 1000 ? String.format("%.1fs", thinkTime / 1000.0) : thinkTime + "ms";
        addLogEntry(String.format("🎯 %s (%s)", move, timeStr), LogLevel.INFO);
        
        // 只在分析包含胜率信息时显示
        if (analysis != null && analysis.contains("胜率")) {
            addLogEntry("📊 " + analysis, LogLevel.DEBUG);
        }
    }
    
    /**
     * 添加引擎状态日志
     */
    public void logEngineStatus(String status) {
        // 只记录重要的引擎状态变化
        if (!status.contains("思考中") && !status.contains("计算")) {
            addLogEntry("⚙️ " + status, LogLevel.INFO);
        }
    }
    
    /**
     * 添加简化的游戏状态日志
     */
    public void logGameMove(String player, String move, String aiType) {
        String icon = player.contains("黑") ? "⚫" : "⚪";
        String typeInfo = aiType != null ? " (" + aiType + ")" : "";
        addLogEntry(String.format("%s %s: %s%s", icon, player, move, typeInfo), LogLevel.INFO);
    }
    
    /**
     * 清空日志历史
     */
    public void clearLog() {
        clearLogs();
    }
    
    /**
     * 停止面板（清理资源）
     */
    public void shutdown() {
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
        logQueue.clear();
    }
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        INFO, ERROR, DEBUG, HIGHLIGHT
    }
    
    /**
     * 日志条目类
     */
    private static class LogEntry {
        final String message;
        final LogLevel level;
        final long timestamp;
        
        LogEntry(String message, LogLevel level, long timestamp) {
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }
    }
}
