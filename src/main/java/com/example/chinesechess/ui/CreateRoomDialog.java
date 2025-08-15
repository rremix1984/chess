package com.example.chinesechess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 创建房间对话框
 * 允许玩家设置房间名称、密码等参数
 */
public class CreateRoomDialog extends JDialog {
    
    private JTextField roomNameField;
    private JPasswordField passwordField;
    private JCheckBox needPasswordCheckBox;
    private JComboBox<String> gameTypeComboBox;
    private JComboBox<String> timeLimitComboBox;
    private JTextArea descriptionArea;
    
    private boolean confirmed = false;
    
    public CreateRoomDialog(Frame parent) {
        super(parent, "🏠 创建房间", true);
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 设置默认焦点
        roomNameField.requestFocus();
    }
    
    private void initComponents() {
        // 房间名称
        roomNameField = new JTextField("玩家的房间", 20);
        
        // 密码设置
        needPasswordCheckBox = new JCheckBox("设置房间密码", false);
        passwordField = new JPasswordField(20);
        passwordField.setEnabled(false);
        
        // 游戏类型
        String[] gameTypes = {"经典象棋", "快速对弈", "等级匹配"};
        gameTypeComboBox = new JComboBox<>(gameTypes);
        
        // 时间限制
        String[] timeLimits = {"无限制", "10分钟", "15分钟", "30分钟", "60分钟"};
        timeLimitComboBox = new JComboBox<>(timeLimits);
        timeLimitComboBox.setSelectedIndex(2); // 默认15分钟
        
        // 房间描述
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setText("欢迎加入我的象棋房间！");
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        // 设置组件样式
        styleComponents();
    }
    
    private void styleComponents() {
        Font font = new Font("微软雅黑", Font.PLAIN, 12);
        
        roomNameField.setFont(font);
        passwordField.setFont(font);
        needPasswordCheckBox.setFont(font);
        gameTypeComboBox.setFont(font);
        timeLimitComboBox.setFont(font);
        descriptionArea.setFont(font);
        
        // 设置边框
        descriptionArea.setBorder(BorderFactory.createLoweredBevelBorder());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 房间名称
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("房间名称:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mainPanel.add(roomNameField, gbc);
        
        // 密码设置
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(needPasswordCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("房间密码:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);
        
        // 游戏类型
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("游戏类型:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mainPanel.add(gameTypeComboBox, gbc);
        
        // 时间限制
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("时间限制:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        mainPanel.add(timeLimitComboBox, gbc);
        
        // 房间描述
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("房间描述:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(descScrollPane, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton confirmButton = new JButton("创建房间");
        JButton cancelButton = new JButton("取消");
        
        // 设置按钮样式
        styleButton(confirmButton);
        styleButton(cancelButton);
        
        confirmButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        panel.add(cancelButton);
        panel.add(confirmButton);
        
        // 按钮事件
        confirmButton.addActionListener(e -> confirmCreate());
        cancelButton.addActionListener(e -> cancelCreate());
        
        return panel;
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setBackground(new Color(240, 240, 240));
        
        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }
    
    private void setupEventHandlers() {
        // 密码复选框事件
        needPasswordCheckBox.addActionListener(e -> {
            boolean needPassword = needPasswordCheckBox.isSelected();
            passwordField.setEnabled(needPassword);
            if (needPassword) {
                passwordField.requestFocus();
            } else {
                passwordField.setText("");
            }
        });
        
        // 键盘事件
        setupKeyBindings();
    }
    
    private void setupKeyBindings() {
        // ESC键取消
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelCreate();
            }
        });
        
        // Ctrl+Enter确认
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKeyStroke, "CTRL_ENTER");
        getRootPane().getActionMap().put("CTRL_ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmCreate();
            }
        });
    }
    
    private void confirmCreate() {
        String roomName = roomNameField.getText().trim();
        
        // 验证输入
        if (roomName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "请输入房间名称！", 
                "输入错误", 
                JOptionPane.WARNING_MESSAGE);
            roomNameField.requestFocus();
            return;
        }
        
        if (roomName.length() > 50) {
            JOptionPane.showMessageDialog(this, 
                "房间名称不能超过50个字符！", 
                "输入错误", 
                JOptionPane.WARNING_MESSAGE);
            roomNameField.requestFocus();
            return;
        }
        
        if (needPasswordCheckBox.isSelected()) {
            String password = new String(passwordField.getPassword());
            if (password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "请输入房间密码！", 
                    "输入错误", 
                    JOptionPane.WARNING_MESSAGE);
                passwordField.requestFocus();
                return;
            }
            if (password.length() > 20) {
                JOptionPane.showMessageDialog(this, 
                    "房间密码不能超过20个字符！", 
                    "输入错误", 
                    JOptionPane.WARNING_MESSAGE);
                passwordField.requestFocus();
                return;
            }
        }
        
        String description = descriptionArea.getText().trim();
        if (description.length() > 200) {
            JOptionPane.showMessageDialog(this, 
                "房间描述不能超过200个字符！", 
                "输入错误", 
                JOptionPane.WARNING_MESSAGE);
            descriptionArea.requestFocus();
            return;
        }
        
        confirmed = true;
        dispose();
    }
    
    private void cancelCreate() {
        confirmed = false;
        dispose();
    }
    
    // Getter方法
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getRoomName() {
        return roomNameField.getText().trim();
    }
    
    public String getPassword() {
        if (needPasswordCheckBox.isSelected()) {
            return new String(passwordField.getPassword());
        }
        return "";
    }
    
    public boolean needPassword() {
        return needPasswordCheckBox.isSelected();
    }
    
    public String getGameType() {
        return (String) gameTypeComboBox.getSelectedItem();
    }
    
    public String getTimeLimit() {
        return (String) timeLimitComboBox.getSelectedItem();
    }
    
    public String getDescription() {
        return descriptionArea.getText().trim();
    }
}
