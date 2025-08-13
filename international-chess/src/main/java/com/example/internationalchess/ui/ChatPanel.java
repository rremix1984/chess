package com.example.internationalchess.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 聊天面板
 * 用于与AI进行对话交流
 */
public class ChatPanel extends JPanel {
    
    private JTextArea chatDisplayArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton clearButton;
    private JScrollPane scrollPane;
    private SimpleDateFormat timeFormat;
    
    /**
     * 构造函数
     */
    public ChatPanel() {
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
        setBorder(new TitledBorder("AI对话"));
        setPreferredSize(new Dimension(300, 250));
        
        // 创建聊天显示区域
        chatDisplayArea = new JTextArea();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        chatDisplayArea.setBackground(new Color(248, 248, 248));
        chatDisplayArea.setLineWrap(true);
        chatDisplayArea.setWrapStyleWord(true);
        
        // 创建滚动面板
        scrollPane = new JScrollPane(chatDisplayArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 创建输入框
        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        inputField.setPreferredSize(new Dimension(200, 25));
        
        // 创建发送按钮
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sendButton.setPreferredSize(new Dimension(60, 25));
        
        // 创建清除按钮
        clearButton = new JButton("清除");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearButton.setPreferredSize(new Dimension(60, 25));
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 添加聊天显示区域
        add(scrollPane, BorderLayout.CENTER);
        
        // 底部输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 发送按钮事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // 清除按钮事件
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearChat();
            }
        });
        
        // 输入框回车事件
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }
    
    /**
     * 发送消息
     */
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            addUserMessage(message);
            inputField.setText("");
            
            // 这里可以添加发送给AI的逻辑
            // 暂时添加一个简单的回复
            SwingUtilities.invokeLater(() -> {
                addAIMessage("收到您的消息：" + message);
            });
        }
    }
    
    /**
     * 添加用户消息
     */
    public void addUserMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            String formattedMessage = String.format("[%s] 用户: %s\n", timestamp, message);
            chatDisplayArea.append(formattedMessage);
            scrollToBottom();
        });
    }
    
    /**
     * 添加AI消息
     */
    public void addAIMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            String formattedMessage = String.format("[%s] AI: %s\n", timestamp, message);
            chatDisplayArea.append(formattedMessage);
            scrollToBottom();
        });
    }
    
    /**
     * 添加系统消息
     */
    public void addSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            String formattedMessage = String.format("[%s] 系统: %s\n", timestamp, message);
            chatDisplayArea.append(formattedMessage);
            scrollToBottom();
        });
    }
    
    /**
     * 添加聊天消息
     */
    public void addChatMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            String formattedMessage = String.format("[%s] %s: %s\n", timestamp, sender, message);
            chatDisplayArea.append(formattedMessage);
            scrollToBottom();
        });
    }
    
    /**
     * 清除聊天记录
     */
    public void clearChat() {
        SwingUtilities.invokeLater(() -> {
            chatDisplayArea.setText("");
            addSystemMessage("聊天记录已清除");
        });
    }
    
    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
    }
    
    /**
     * 获取聊天内容
     */
    public String getChatContent() {
        return chatDisplayArea.getText();
    }
    
    /**
     * 设置聊天内容
     */
    public void setChatContent(String content) {
        SwingUtilities.invokeLater(() -> {
            chatDisplayArea.setText(content);
            scrollToBottom();
        });
    }
    
    /**
     * 设置输入框启用状态
     */
    public void setInputEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            inputField.setEnabled(enabled);
            sendButton.setEnabled(enabled);
        });
    }
    
    /**
     * 获取输入框文本
     */
    public String getInputText() {
        return inputField.getText();
    }
    
    /**
     * 设置输入框文本
     */
    public void setInputText(String text) {
        SwingUtilities.invokeLater(() -> {
            inputField.setText(text);
        });
    }
    
    /**
     * 聚焦到输入框
     */
    public void focusInput() {
        SwingUtilities.invokeLater(() -> {
            inputField.requestFocus();
        });
    }
    
    /**
     * 保存聊天记录到文件
     */
    public void saveChatToFile(String filePath) {
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filePath), 
                getChatContent().getBytes("UTF-8")
            );
            addSystemMessage("聊天记录已保存到: " + filePath);
        } catch (Exception e) {
            addSystemMessage("保存聊天记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 从文件加载聊天记录
     */
    public void loadChatFromFile(String filePath) {
        try {
            String content = new String(
                java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)), 
                "UTF-8"
            );
            setChatContent(content);
            addSystemMessage("聊天记录已从文件加载: " + filePath);
        } catch (Exception e) {
            addSystemMessage("加载聊天记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置模型名称
     */
    public void setModelName(String modelName) {
        // TODO: 实现模型名称设置逻辑
        addSystemMessage("使用模型: " + modelName);
    }
}
