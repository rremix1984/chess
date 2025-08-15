package com.example.chinesechess.ui;

import com.example.chinesechess.network.NetworkClient;
import com.example.chinesechess.network.Room;
import com.example.chinesechess.network.NetworkMessageHandler;
import com.example.chinesechess.network.protocol.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * 网络房间主界面
 * 用于显示房间列表、创建房间、加入房间等网络功能
 */
public class NetworkRoomFrame extends JFrame implements NetworkMessageHandler {
    
    private NetworkClient networkClient;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTextField playerNameField;
    private JTextField serverAddressField;
    private JTextField serverPortField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    private JLabel connectionStatusLabel;
    
    private boolean isConnected = false;
    private String playerName = "";
    
    public NetworkRoomFrame() {
        setTitle("🌐 中国象棋 - 网络对弈房间");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        // 窗口关闭时断开连接
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
    }
    
    private void initializeComponents() {
        // 服务器连接组件
        serverAddressField = new JTextField("localhost", 15);
        serverPortField = new JTextField("12345", 8);
        playerNameField = new JTextField("玩家" + (int)(Math.random() * 1000), 15);
        
        connectButton = new JButton("连接服务器");
        disconnectButton = new JButton("断开连接");
        disconnectButton.setEnabled(false);
        
        // 房间操作按钮
        createRoomButton = new JButton("创建房间");
        joinRoomButton = new JButton("加入房间");
        refreshButton = new JButton("刷新列表");
        
        // 初始状态下禁用房间操作按钮
        createRoomButton.setEnabled(false);
        joinRoomButton.setEnabled(false);
        refreshButton.setEnabled(false);
        
        // 房间列表表格
        String[] columnNames = {"房间ID", "房间名称", "创建者", "玩家数", "状态", "需要密码"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止编辑
            }
        };
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 设置列宽
        TableColumnModel columnModel = roomTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // 房间ID
        columnModel.getColumn(1).setPreferredWidth(200); // 房间名称
        columnModel.getColumn(2).setPreferredWidth(120); // 创建者
        columnModel.getColumn(3).setPreferredWidth(80);  // 玩家数
        columnModel.getColumn(4).setPreferredWidth(80);  // 状态
        columnModel.getColumn(5).setPreferredWidth(80);  // 需要密码
        
        // 状态标签
        connectionStatusLabel = new JLabel("🔴 未连接");
        statusLabel = new JLabel("请连接服务器开始网络对弈");
        
        // 设置组件样式
        styleComponents();
    }
    
    private void styleComponents() {
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 12);
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 11);
        
        // 设置按钮样式
        JButton[] buttons = {connectButton, disconnectButton, createRoomButton, 
                            joinRoomButton, refreshButton};
        for (JButton button : buttons) {
            button.setFont(buttonFont);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        
        // 设置标签样式
        connectionStatusLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);
        
        // 设置表格样式
        roomTable.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        roomTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 11));
        roomTable.setRowHeight(25);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 顶部连接面板
        JPanel connectionPanel = createConnectionPanel();
        add(connectionPanel, BorderLayout.NORTH);
        
        // 中央房间列表面板
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部状态面板
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("服务器连接"));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        panel.add(new JLabel("服务器地址:"));
        panel.add(serverAddressField);
        
        panel.add(new JLabel("端口:"));
        panel.add(serverPortField);
        
        panel.add(new JLabel("玩家名称:"));
        panel.add(playerNameField);
        
        panel.add(connectButton);
        panel.add(disconnectButton);
        panel.add(connectionStatusLabel);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("房间列表"));
        
        // 房间列表表格
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setPreferredSize(new Dimension(850, 350));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(createRoomButton);
        buttonPanel.add(joinRoomButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }
    
    private void setupEventHandlers() {
        // 连接按钮
        connectButton.addActionListener(e -> connectToServer());
        
        // 断开连接按钮
        disconnectButton.addActionListener(e -> disconnect());
        
        // 创建房间按钮
        createRoomButton.addActionListener(e -> showCreateRoomDialog());
        
        // 加入房间按钮
        joinRoomButton.addActionListener(e -> joinSelectedRoom());
        
        // 刷新按钮
        refreshButton.addActionListener(e -> refreshRoomList());
        
        // 表格双击加入房间
        roomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    joinSelectedRoom();
                }
            }
        });
    }
    
    private void connectToServer() {
        String serverAddress = serverAddressField.getText().trim();
        String portText = serverPortField.getText().trim();
        playerName = playerNameField.getText().trim();
        
        if (serverAddress.isEmpty() || portText.isEmpty() || playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "请填写完整的服务器信息和玩家名称！", 
                "输入错误", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            
            // 创建网络客户端
            networkClient = new NetworkClient(this);
            
            // 在后台线程中连接服务器
            SwingWorker<Boolean, Void> connectWorker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    updateStatus("正在连接服务器...");
                    return networkClient.connect(serverAddress, port, playerName);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean connected = get();
                        if (connected) {
                            onConnectedToServer();
                        } else {
                            updateStatus("连接服务器失败！");
                            connectionStatusLabel.setText("🔴 连接失败");
                        }
                    } catch (Exception e) {
                        updateStatus("连接服务器失败: " + e.getMessage());
                        connectionStatusLabel.setText("🔴 连接失败");
                        e.printStackTrace();
                    }
                }
            };
            
            connectWorker.execute();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "端口号必须是数字！", 
                "输入错误", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onConnectedToServer() {
        isConnected = true;
        connectionStatusLabel.setText("🟢 已连接");
        updateStatus("已连接到服务器，玩家: " + playerName);
        
        // 更新按钮状态
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(true);
        createRoomButton.setEnabled(true);
        joinRoomButton.setEnabled(true);
        refreshButton.setEnabled(true);
        
        // 禁用服务器设置
        serverAddressField.setEnabled(false);
        serverPortField.setEnabled(false);
        playerNameField.setEnabled(false);
        
        // 刷新房间列表
        refreshRoomList();
    }
    
    private void disconnect() {
        if (networkClient != null) {
            networkClient.disconnect();
            networkClient = null;
        }
        
        isConnected = false;
        connectionStatusLabel.setText("🔴 未连接");
        updateStatus("已断开连接");
        
        // 更新按钮状态
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        createRoomButton.setEnabled(false);
        joinRoomButton.setEnabled(false);
        refreshButton.setEnabled(false);
        
        // 启用服务器设置
        serverAddressField.setEnabled(true);
        serverPortField.setEnabled(true);
        playerNameField.setEnabled(true);
        
        // 清空房间列表
        tableModel.setRowCount(0);
    }
    
    private void showCreateRoomDialog() {
        CreateRoomDialog dialog = new CreateRoomDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            String roomName = dialog.getRoomName();
            String password = dialog.getPassword();
            
            // 发送创建房间请求
            if (networkClient != null) {
                CreateRoomMessage createMsg = new CreateRoomMessage(roomName, password, playerName);
                networkClient.sendMessage(createMsg);
                updateStatus("正在创建房间: " + roomName);
            }
        }
    }
    
    private void joinSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "请先选择一个房间！", 
                "提示", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String roomId = (String) tableModel.getValueAt(selectedRow, 0);
        String roomName = (String) tableModel.getValueAt(selectedRow, 1);
        String needPassword = (String) tableModel.getValueAt(selectedRow, 5);
        
        String password = "";
        if ("是".equals(needPassword)) {
            JoinRoomDialog dialog = new JoinRoomDialog(this, roomName, true);
            dialog.setVisible(true);
            
            if (!dialog.isConfirmed()) {
                return; // 用户取消了
            }
            
            password = dialog.getPassword();
        }
        
        // 发送加入房间请求
        if (networkClient != null) {
            JoinRoomMessage joinMsg = new JoinRoomMessage(roomId, password, playerName);
            networkClient.sendMessage(joinMsg);
            updateStatus("正在加入房间: " + roomName);
        }
    }
    
    private void refreshRoomList() {
        if (networkClient != null) {
            RoomListRequestMessage requestMsg = new RoomListRequestMessage();
            networkClient.sendMessage(requestMsg);
            updateStatus("正在刷新房间列表...");
        }
    }
    
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
    
    private void updateRoomList(List<Room> rooms) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            
            for (Room room : rooms) {
                Object[] rowData = {
                    room.getId(),
                    room.getName(),
                    room.getCreatorName(),
                    room.getPlayerCount() + "/" + room.getMaxPlayers(),
                    room.getStatus(),
                    room.hasPassword() ? "是" : "否"
                };
                tableModel.addRow(rowData);
            }
            
            updateStatus("房间列表已更新 (" + rooms.size() + "个房间)");
        });
    }
    
    // NetworkMessageHandler 接口实现
    @Override
    public void onMessageReceived(NetworkMessage message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case ROOM_LIST_RESPONSE:
                    RoomListResponseMessage roomListMsg = (RoomListResponseMessage) message;
                    updateRoomList(roomListMsg.getRooms());
                    break;
                    
                case ROOM_CREATED:
                    RoomCreatedMessage createdMsg = (RoomCreatedMessage) message;
                    updateStatus("房间创建成功: " + createdMsg.getRoomName());
                    // 进入游戏房间
                    enterGameRoom(createdMsg.getRoomId(), createdMsg.getRoomName());
                    break;
                    
                case ROOM_JOINED:
                    RoomJoinedMessage joinedMsg = (RoomJoinedMessage) message;
                    updateStatus("成功加入房间: " + joinedMsg.getRoomName());
                    // 进入游戏房间
                    enterGameRoom(joinedMsg.getRoomId(), joinedMsg.getRoomName());
                    break;
                    
                case ERROR:
                    ErrorMessage errorMsg = (ErrorMessage) message;
                    updateStatus("错误: " + errorMsg.getErrorMessage());
                    JOptionPane.showMessageDialog(this, 
                        errorMsg.getErrorMessage(), 
                        "服务器错误", 
                        JOptionPane.ERROR_MESSAGE);
                    break;
                    
                default:
                    System.out.println("收到未处理的消息类型: " + message.getType());
                    break;
            }
        });
    }
    
    @Override
    public void onConnectionClosed() {
        SwingUtilities.invokeLater(() -> {
            updateStatus("与服务器的连接已断开");
            disconnect();
        });
    }
    
    @Override
    public void onConnectionError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("连接错误: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "连接错误: " + e.getMessage(), 
                "网络错误", 
                JOptionPane.ERROR_MESSAGE);
            disconnect();
        });
    }
    
    private void enterGameRoom(String roomId, String roomName) {
        // 隐藏房间列表窗口
        setVisible(false);
        
        // 创建网络游戏窗口
        SwingUtilities.invokeLater(() -> {
            GameFrame gameFrame = new GameFrame();
            
            // 设置网络模式
            gameFrame.setNetworkMode(true);
            gameFrame.setNetworkClient(networkClient);
            gameFrame.setRoomInfo(roomId, roomName);
            
            // 显示游戏窗口
            gameFrame.setVisible(true);
            
            // 设置游戏窗口关闭时回到房间列表
            gameFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // 返回房间列表
                    setVisible(true);
                    refreshRoomList();
                }
            });
        });
    }
    
    /**
     * 获取网络客户端
     */
    public NetworkClient getNetworkClient() {
        return networkClient;
    }
    
    /**
     * 获取玩家名称
     */
    public String getPlayerName() {
        return playerName;
    }
}
