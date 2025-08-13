package com.example.internationalchess.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AI日志面板
 * 用于显示AI的思考过程和决策日志
 */
public class AILogPanel extends JPanel {
    
    private JTextArea logTextArea;
    private JScrollPane scrollPane;
    private JButton clearButton;
    private SimpleDateFormat timeFormat;
    
    /**
     * 构造函数
     */
    public AILogPanel() {
        timeFormat = new SimpleDateFormat("HH:mm:ss");
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 设置面板边框
        setBorder(new TitledBorder("AI思考日志"));
        setPreferredSize(new Dimension(300, 200));
        
        // 创建文本区域
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logTextArea.setBackground(new Color(248, 248, 248));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        
        // 创建滚动面板
        scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 创建清除按钮
        clearButton = new JButton("清除日志");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearButton.setPreferredSize(new Dimension(80, 25));
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 添加组件
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(clearButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });
    }
    
    /**
     * 添加日志
     */
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            String logEntry = String.format("[%s] %s\n", timestamp, message);
            
            logTextArea.append(logEntry);
            
            // 自动滚动到底部
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            
            // 限制日志长度，避免内存溢出
            limitLogLength();
        });
    }
    
    /**
     * 添加带颜色的日志（通过HTML格式）
     */
    public void addColoredLog(String message, Color color) {
        // 简化版本，直接添加普通日志
        addLog(message);
    }
    
    /**
     * 添加错误日志
     */
    public void addErrorLog(String message) {
        addLog("[错误] " + message);
    }
    
    /**
     * 添加警告日志
     */
    public void addWarningLog(String message) {
        addLog("[警告] " + message);
    }
    
    /**
     * 添加信息日志
     */
    public void addInfoLog(String message) {
        addLog("[信息] " + message);
    }
    
    /**
     * 清除日志
     */
    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            logTextArea.setText("");
            addLog("日志已清除");
        });
    }
    
    /**
     * 限制日志长度
     */
    private void limitLogLength() {
        String text = logTextArea.getText();
        String[] lines = text.split("\n");
        
        // 如果超过1000行，保留最后800行
        if (lines.length > 1000) {
            StringBuilder sb = new StringBuilder();
            for (int i = lines.length - 800; i < lines.length; i++) {
                sb.append(lines[i]).append("\n");
            }
            logTextArea.setText(sb.toString());
            addLog("日志已自动清理，保留最近800条记录");
        }
    }
    
    /**
     * 获取日志内容
     */
    public String getLogContent() {
        return logTextArea.getText();
    }
    
    /**
     * 设置日志内容
     */
    public void setLogContent(String content) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.setText(content);
        });
    }
    
    /**
     * 保存日志到文件
     */
    public void saveLogToFile(String filePath) {
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filePath), 
                getLogContent().getBytes("UTF-8")
            );
            addLog("日志已保存到: " + filePath);
        } catch (Exception e) {
            addErrorLog("保存日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 从文件加载日志
     */
    public void loadLogFromFile(String filePath) {
        try {
            String content = new String(
                java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)), 
                "UTF-8"
            );
            setLogContent(content);
            addLog("日志已从文件加载: " + filePath);
        } catch (Exception e) {
            addErrorLog("加载日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置字体
     */
    public void setLogFont(Font font) {
        logTextArea.setFont(font);
    }
    
    /**
     * 设置背景颜色
     */
    public void setLogBackground(Color color) {
        logTextArea.setBackground(color);
    }
    
    /**
     * 设置前景颜色
     */
    public void setLogForeground(Color color) {
        logTextArea.setForeground(color);
    }
}