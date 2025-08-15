package com.example.chinesechess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 加入房间对话框
 * 用于输入房间密码或快速加入房间
 */
public class JoinRoomDialog extends JDialog {
    
    private JTextField roomIdField;
    private JPasswordField passwordField;
    private JLabel roomNameLabel;
    private JLabel roomInfoLabel;
    private JCheckBox rememberPasswordCheckBox;
    
    private boolean confirmed = false;
    private boolean needPassword = false;
    private String roomName = "";
    
    /**
     * 构造方法 - 通过房间名称加入（需要密码验证）
     */
    public JoinRoomDialog(Frame parent, String roomName, boolean needPassword) {
        super(parent, "🚪 加入房间", true);
        this.roomName = roomName;
        this.needPassword = needPassword;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        setSize(400, needPassword ? 280 : 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 设置默认焦点
        if (needPassword) {
            passwordField.requestFocus();
        }
    }
    
    /**
     * 构造方法 - 通过房间ID直接加入
     */
    public JoinRoomDialog(Frame parent) {
        super(parent, "🚪 加入房间", true);
        this.needPassword = false;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        setSize(400, 220);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        roomIdField.requestFocus();
    }
    
    private void initComponents() {
        // 房间ID输入（仅在直接加入模式显示）
        roomIdField = new JTextField(15);
        
        // 房间信息显示
        if (!roomName.isEmpty()) {
            roomNameLabel = new JLabel("房间: " + roomName);
            roomNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        }
        
        // 房间信息
        roomInfoLabel = new JLabel();
        if (needPassword) {
            roomInfoLabel.setText("此房间需要密码才能加入");
            roomInfoLabel.setForeground(new Color(139, 69, 19)); // 棕色
        } else {
            roomInfoLabel.setText("请输入要加入的房间ID");
            roomInfoLabel.setForeground(new Color(60, 60, 60)); // 深灰色
        }
        
        // 密码输入
        passwordField = new JPasswordField(15);
        
        // 记住密码选项
        rememberPasswordCheckBox = new JCheckBox("记住此房间密码", false);
        
        // 设置组件样式
        styleComponents();
    }
    
    private void styleComponents() {
        Font font = new Font("微软雅黑", Font.PLAIN, 12);
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 11);
        
        if (roomIdField != null) roomIdField.setFont(font);
        if (passwordField != null) passwordField.setFont(font);
        if (rememberPasswordCheckBox != null) rememberPasswordCheckBox.setFont(labelFont);
        if (roomInfoLabel != null) roomInfoLabel.setFont(labelFont);
        
        // 设置密码框提示
        if (needPassword) {
            passwordField.setToolTipText("请输入房间密码");
        }
        
        if (roomIdField != null) {
            roomIdField.setToolTipText("输入6位房间ID，如: 123456");
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        
        JLabel titleLabel = new JLabel("🚪 加入房间");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // 房间名称显示（如果有）
        if (roomNameLabel != null) {
            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            mainPanel.add(roomNameLabel, gbc);
            gbc.gridwidth = 1;
        }
        
        // 房间信息
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(roomInfoLabel, gbc);
        gbc.gridwidth = 1;
        
        // 房间ID输入（仅在直接加入模式）
        if (roomName.isEmpty()) {
            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
            mainPanel.add(new JLabel("房间ID:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            mainPanel.add(roomIdField, gbc);
            row++;
        }
        
        // 密码输入（如果需要）
        if (needPassword) {
            gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            mainPanel.add(new JLabel("房间密码:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            mainPanel.add(passwordField, gbc);
            row++;
            
            // 记住密码选项
            gbc.gridx = 1; gbc.gridy = row++; gbc.fill = GridBagConstraints.NONE;
            mainPanel.add(rememberPasswordCheckBox, gbc);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton joinButton = new JButton("加入房间");
        JButton cancelButton = new JButton("取消");
        
        // 设置按钮样式
        styleButton(joinButton);
        styleButton(cancelButton);
        
        joinButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        // 设置按钮颜色
        joinButton.setBackground(new Color(100, 149, 237)); // 蓝色
        joinButton.setForeground(Color.WHITE);
        
        panel.add(cancelButton);
        panel.add(joinButton);
        
        // 按钮事件
        joinButton.addActionListener(e -> confirmJoin());
        cancelButton.addActionListener(e -> cancelJoin());
        
        return panel;
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.getBackground().equals(new Color(100, 149, 237))) {
                    button.setBackground(new Color(65, 105, 225)); // 更深的蓝色
                } else {
                    button.setBackground(originalColor.brighter());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }
    
    private void setupEventHandlers() {
        // 键盘事件
        setupKeyBindings();
        
        // 房间ID输入验证（如果存在）
        if (roomIdField != null) {
            roomIdField.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                        e.consume(); // 只允许数字
                    }
                    
                    // 限制长度为6位
                    if (roomIdField.getText().length() >= 6 && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                        e.consume();
                    }
                }
            });
        }
    }
    
    private void setupKeyBindings() {
        // ESC键取消
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelJoin();
            }
        });
        
        // Enter键确认
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKeyStroke, "ENTER");
        getRootPane().getActionMap().put("ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmJoin();
            }
        });
    }
    
    private void confirmJoin() {
        // 验证房间ID（如果需要）
        if (roomIdField != null && roomIdField.isVisible()) {
            String roomId = roomIdField.getText().trim();
            if (roomId.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "请输入房间ID！", 
                    "输入错误", 
                    JOptionPane.WARNING_MESSAGE);
                roomIdField.requestFocus();
                return;
            }
            
            if (roomId.length() != 6) {
                JOptionPane.showMessageDialog(this, 
                    "房间ID必须是6位数字！", 
                    "输入错误", 
                    JOptionPane.WARNING_MESSAGE);
                roomIdField.requestFocus();
                return;
            }
        }
        
        // 验证密码（如果需要）
        if (needPassword) {
            String password = new String(passwordField.getPassword());
            if (password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "请输入房间密码！", 
                    "输入错误", 
                    JOptionPane.WARNING_MESSAGE);
                passwordField.requestFocus();
                return;
            }
        }
        
        confirmed = true;
        dispose();
    }
    
    private void cancelJoin() {
        confirmed = false;
        dispose();
    }
    
    // Getter方法
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getRoomId() {
        if (roomIdField != null) {
            return roomIdField.getText().trim();
        }
        return "";
    }
    
    public String getPassword() {
        if (needPassword) {
            return new String(passwordField.getPassword());
        }
        return "";
    }
    
    public boolean shouldRememberPassword() {
        return needPassword && rememberPasswordCheckBox.isSelected();
    }
}
