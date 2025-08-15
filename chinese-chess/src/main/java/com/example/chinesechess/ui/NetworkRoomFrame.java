package com.example.chinesechess.ui;

import com.example.chinesechess.network.NetworkClient;
import com.example.chinesechess.network.RoomInfo;
import com.example.chinesechess.network.RoomListRequestMessage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 网络房间主界面
 * 管理服务器连接、房间列表、创建和加入房间等功能
 */
public class NetworkRoomFrame extends JFrame implements NetworkClient.ClientEventListener {
    
    private NetworkClient networkClient;
    private JLabel connectionStatusLabel;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTextField serverHostField;
    private JTextField serverPortField;
    private JTextField playerNameField;
    
    // 连接状态
    private boolean isConnected = false;
    
    public NetworkRoomFrame() {
        initializeUI();
        setupEventHandlers();
        
        // 创建网络客户端
        networkClient = new NetworkClient();
        networkClient.setEventListener(this);
    }
    
    private void initializeUI() {
        setTitle("🌐 中国象棋 - 网络对弈");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // 创建顶部连接面板
        JPanel connectionPanel = createConnectionPanel();
        add(connectionPanel, BorderLayout.NORTH);
        
        // 创建中央房间列表面板
        JPanel roomListPanel = createRoomListPanel();
        add(roomListPanel, BorderLayout.CENTER);
        
        // 创建底部面板（包含按钮和状态栏）
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // 创建状态栏
        connectionStatusLabel = new JLabel("🔴 未连接到服务器");
        connectionStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.add(connectionStatusLabel, BorderLayout.SOUTH);
        
        // 添加底部面板到主界面
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("服务器连接"));
        
        panel.add(new JLabel("服务器:"));
        serverHostField = new JTextField("localhost", 15);
        panel.add(serverHostField);
        
        panel.add(new JLabel("端口:"));
        serverPortField = new JTextField("8080", 6);
        panel.add(serverPortField);
        
        panel.add(new JLabel("玩家名:"));
        playerNameField = new JTextField("Player" + (int)(Math.random() * 1000), 10);
        panel.add(playerNameField);
        
        JButton connectButton = new JButton("连接");
        connectButton.addActionListener(e -> connectToServer());
        panel.add(connectButton);
        
        JButton disconnectButton = new JButton("断开");
        disconnectButton.addActionListener(e -> disconnectFromServer());
        panel.add(disconnectButton);
        
        return panel;
    }
    
    private JPanel createRoomListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("房间列表"));
        
        // 创建表格
        String[] columnNames = {"房间ID", "房间名", "主机", "玩家数", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(roomTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 刷新按钮
        JButton refreshButton = new JButton("刷新房间列表");
        refreshButton.addActionListener(e -> refreshRoomList());
        panel.add(refreshButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton createRoomButton = new JButton("创建房间");
        createRoomButton.addActionListener(e -> showCreateRoomDialog());
        panel.add(createRoomButton);
        
        JButton joinRoomButton = new JButton("加入房间");
        joinRoomButton.addActionListener(e -> joinSelectedRoom());
        panel.add(joinRoomButton);
        
        JButton backButton = new JButton("返回主菜单");
        backButton.addActionListener(e -> returnToMainMenu());
        panel.add(backButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // 表格双击事件
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
        if (isConnected) {
            updateStatus("已连接到服务器");
            return;
        }
        
        String host = serverHostField.getText().trim();
        String portStr = serverPortField.getText().trim();
        String playerName = playerNameField.getText().trim();
        
        if (host.isEmpty() || portStr.isEmpty() || playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有连接信息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            updateStatus("正在连接到服务器...");
            networkClient.connect(host, port, playerName);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口号格式错误", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void disconnectFromServer() {
        if (networkClient != null) {
            networkClient.disconnect();
        }
        isConnected = false;
        updateStatus("🔴 已断开连接");
        
        // 清空房间列表
        tableModel.setRowCount(0);
    }
    
    private void refreshRoomList() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 请求房间列表
        updateStatus("正在刷新房间列表...");
        RoomListRequestMessage request = new RoomListRequestMessage(networkClient.getPlayerId());
        networkClient.sendNetworkMessage(request);
    }
    
    private void showCreateRoomDialog() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 简单的创建房间对话框
        String roomName = JOptionPane.showInputDialog(this, "请输入房间名:", "创建房间", JOptionPane.PLAIN_MESSAGE);
        if (roomName != null && !roomName.trim().isEmpty()) {
            networkClient.createRoom(roomName.trim(), "");
        }
    }
    
    private void joinSelectedRoom() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择一个房间", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String roomId = (String) tableModel.getValueAt(selectedRow, 0);
        networkClient.joinRoom(roomId, "");
    }
    
    private void returnToMainMenu() {
        disconnectFromServer();
        setVisible(false);
        dispose();
        System.exit(0);
    }
    
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            connectionStatusLabel.setText(status);
        });
    }
    
    // NetworkClient.ClientEventListener 实现
    
    @Override
    public void onConnected() {
        isConnected = true;
        updateStatus("🟢 已连接到服务器");
        refreshRoomList();
    }
    
    @Override
    public void onDisconnected(String reason) {
        isConnected = false;
        updateStatus("🔴 连接断开: " + reason);
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
        });
    }
    
    @Override
    public void onConnectionError(String error) {
        isConnected = false;
        updateStatus("🔴 连接错误: " + error);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "连接失败: " + error, "连接错误", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    @Override
    public void onMessageReceived(com.example.chinesechess.network.NetworkMessage message) {
        // 处理接收到的消息
    }
    
    @Override
    public void onRoomCreated(String roomId) {
        updateStatus("房间创建成功: " + roomId);
        SwingUtilities.invokeLater(() -> {
            // 进入游戏界面
            startNetworkGame(roomId, "");
        });
    }
    
    @Override
    public void onRoomJoined(String roomId, String opponentName) {
        updateStatus("成功加入房间: " + roomId);
        SwingUtilities.invokeLater(() -> {
            // 进入游戏界面
            startNetworkGame(roomId, opponentName);
        });
    }
    
    @Override
    public void onRoomListReceived(List<RoomInfo> rooms) {
        updateStatus("房间列表已更新 (" + rooms.size() + "个房间)");
        SwingUtilities.invokeLater(() -> {
            // 清空当前表格数据
            tableModel.setRowCount(0);
            
            // 添加新的房间数据
            for (RoomInfo room : rooms) {
                Object[] rowData = {
                    room.getRoomId(),
                    room.getRoomName(),
                    room.getHostName(),
                    room.getCurrentPlayers() + "/" + room.getMaxPlayers(),
                    room.getGameStatus()
                };
                tableModel.addRow(rowData);
            }
        });
    }
    
    @Override
    public void onGameStarted(String redPlayer, String blackPlayer, String yourColor) {
        updateStatus("游戏开始: " + redPlayer + " vs " + blackPlayer);
    }
    
    @Override
    public void onMoveReceived(int fromRow, int fromCol, int toRow, int toCol) {
        // 处理对手的移动
    }
    
    @Override
    public void onGameEnded(String winner, String reason) {
        updateStatus("游戏结束: " + winner + " 获胜 (" + reason + ")");
    }
    
    @Override
    public void onGameStateUpdate(String gameState, String currentPlayer, boolean isGameOver, String winner) {
        // 处理游戏状态更新
    }
    
    @Override
    public void onError(String error) {
        updateStatus("错误: " + error);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, error, "错误", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    private void startNetworkGame(String roomId, String opponentName) {
        System.out.println("🌐 开始创建网络游戏界面...");
        
        try {
            // Step 1: 创建游戏界面（但先不显示）
            GameFrame gameFrame = new GameFrame();
            System.out.println("📋 游戏界面已创建");
            
            // Step 2: 设置网络模式和基本信息
            gameFrame.setNetworkMode(true);
            gameFrame.setRoomInfo(roomId, "房间" + roomId);
            gameFrame.setLocalPlayerName(playerNameField.getText().trim());
            System.out.println("⚙️ 网络模式和房间信息已设置");
            
            // Step 3: 关键 - 在显示游戏界面之前先设置网络客户端
            // 这样可以确保BoardPanel的监听器在游戏开始消息到达之前就已经被设置
            gameFrame.setNetworkClient(networkClient);
            System.out.println("📡 网络客户端已转移给GameFrame和BoardPanel");
            
            // Step 4: 延迟显示游戏界面，确保监听器完全设置完成
            SwingUtilities.invokeLater(() -> {
                // 设置完成后再显示游戏界面
                gameFrame.setVisible(true);
                System.out.println("👁️ 游戏界面已显示");
                
                // 隐藏并关闭房间界面
                setVisible(false);
                dispose();
                
                System.out.println("✅ 网络游戏界面完全设置完成，监听器转移成功");
            });
            
        } catch (Exception e) {
            System.err.println("❌ 创建网络游戏界面失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "创建游戏界面失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
