package com.example.launcher;

import com.example.chinesechess.network.*;
import com.example.chinesechess.network.ConnectionState;
import com.example.chinesechess.ui.GameFrame;
import com.example.common.game.GameContext;
import com.example.launcher.util.GameDisplay;
import com.example.launcher.util.GameIconFactory;
import com.example.go.lad.LifeAndDeathFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.BindException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * 游戏中心界面，左侧显示游戏列表，右侧为房间管理
 */
public class GameCenterFrame extends JFrame implements NetworkClient.ClientEventListener {

    private static final Map<String, String> GAME_MAP;
    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("中国象棋", "chinese-chess");
        map.put("国际象棋", "international-chess");
        map.put("五子棋", "gomoku");
        map.put("围棋", "go-game");
        map.put("围棋死活", "go-life-and-death");
        map.put("坦克大战", "tank-battle-game");
        map.put("大富翁", "monopoly");
        GAME_MAP = Collections.unmodifiableMap(map);
    }

    private NetworkClient networkClient;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTextField playerNameField;
    private JLabel connectionStatusLabel;
    private JLabel serverStatusLabel;
    private JButton serverControlButton;
    private JButton connectButton;
    private JPanel serverAlertPanel;
    private ChessGameServer localServer;
    private Thread serverThread;
    private String selectedGameType;
    private Map<String, Icon> gameIcons = new HashMap<>();
    private Timer roomRefreshTimer;

    public GameCenterFrame() {
        setTitle("游戏中心");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        networkClient = new NetworkClient();
        networkClient.setEventListener(this);

        selectedGameType = GAME_MAP.values().iterator().next();

        initUI();
        setupEventHandlers();
        autoStartServerWithDetection();
        startRoomRefreshTimer();
        SwingUtilities.invokeLater(this::connectToServer);
    }

    private void initUI() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);

        // Left game list
        JList<String> gameList = new JList<>(GAME_MAP.keySet().toArray(new String[0]));
        for (Map.Entry<String, String> entry : GAME_MAP.entrySet()) {
            gameIcons.put(entry.getKey(), GameIconFactory.icon(entry.getValue(), 32));
        }
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setSelectedIndex(0);
        gameList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String key = value.toString();
                label.setIcon(gameIcons.get(key));
                label.setHorizontalAlignment(JLabel.LEFT);
                label.setIconTextGap(8);
                return label;
            }
        });
        gameList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selected = gameList.getSelectedValue();
                    if ("围棋死活".equals(selected)) {
                        SwingUtilities.invokeLater(() -> {
                            new LifeAndDeathFrame().setVisible(true);
                            gameList.setSelectedIndex(0);
                        });
                        return;
                    }
                    selectedGameType = GAME_MAP.get(selected);
                    refreshRoomList();
                }
            }
        });
        splitPane.setLeftComponent(new JScrollPane(gameList));

        // Right room panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel topPanel = createConnectionPanel();
        serverAlertPanel = createServerAlertPanel();
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(serverAlertPanel, BorderLayout.SOUTH);

        JPanel centerPanel = createRoomListPanel();
        rightPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createButtonPanel();
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane);
    }

    private void startRoomRefreshTimer() {
        roomRefreshTimer = new Timer(2000, e -> {
            if (networkClient.isConnected()) {
                refreshRoomList();
            }
        });
        roomRefreshTimer.start();
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("服务器连接"));

        panel.add(new JLabel("玩家名:"));
        playerNameField = new JTextField("Player" + (int)(Math.random() * 1000), 10);
        panel.add(playerNameField);

        connectButton = new JButton("连接");
        connectButton.addActionListener(e -> connectToServer());
        panel.add(connectButton);

        serverControlButton = new JButton("启动服务器");
        serverControlButton.addActionListener(e -> toggleServer());
        panel.add(serverControlButton);

        serverStatusLabel = new JLabel("服务器: 未运行");
        panel.add(serverStatusLabel);

        connectionStatusLabel = new JLabel("未连接");
        panel.add(connectionStatusLabel);

        return panel;
    }

    private JPanel createServerAlertPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(255, 228, 181));
        JLabel label = new JLabel("本地服务器未运行。点击‘启动服务器’一键开启。");
        JButton startBtn = new JButton("启动服务器");
        startBtn.addActionListener(e -> toggleServer());
        panel.add(label);
        panel.add(startBtn);
        panel.setVisible(false);
        return panel;
    }

    private JPanel createRoomListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("房间列表"));

        String[] columnNames = {"房间ID", "房间名", "游戏", "人数", "主机"};
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

        JButton singlePlayerButton = new JButton("单人游戏");
        singlePlayerButton.addActionListener(e -> {
            GameContext.setSinglePlayer(true);
            startSelectedGame();
        });
        panel.add(singlePlayerButton);

        return panel;
    }

    private void setupEventHandlers() {
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
        if (networkClient.getConnectionState() != ConnectionState.DISCONNECTED) {
            return;
        }
        connectionStatusLabel.setText("连接中...");
        connectButton.setEnabled(false);
        networkClient.connect("localhost", 8080, playerNameField.getText().trim());
    }

    private void refreshRoomList() {
        if (!networkClient.isConnected()) {
            return;
        }
        connectionStatusLabel.setText("刷新房间列表...");
        networkClient.requestRoomList();
    }

    private void showCreateRoomDialog() {
        if (!networkClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String defaultName = playerNameField.getText().trim() + "的房间";
        String roomName = (String) JOptionPane.showInputDialog(
                this,
                "请输入房间名:",
                "创建房间",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                defaultName);
        if (roomName != null && !roomName.trim().isEmpty()) {
            networkClient.createRoom(roomName.trim(), "", selectedGameType);
        }
    }

    private void joinSelectedRoom() {
        if (!networkClient.isConnected()) {
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

    private void toggleServer() {
        if (localServer == null) {
            autoStartServerWithDetection();
        } else {
            stopLocalServer();
        }
    }

    private void autoStartServerWithDetection() {
        try {
            localServer = new ChessGameServer(8080);
            serverThread = new Thread(() -> localServer.start());
            serverThread.start();
            serverStatusLabel.setText("服务器: 运行中");
            serverAlertPanel.setVisible(false);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof BindException) {
                serverStatusLabel.setText("服务器: 运行中");
                serverAlertPanel.setVisible(false);
            } else {
                serverStatusLabel.setText("服务器: 启动失败");
                serverAlertPanel.setVisible(true);
            }
            localServer = null;
        }
    }

    private void stopLocalServer() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
            serverStatusLabel.setText("服务器: 未运行");
            serverAlertPanel.setVisible(true);
        }
    }

    // ============ NetworkClient callbacks ============

    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            connectionStatusLabel.setText("已连接");
            connectButton.setEnabled(false);
            refreshRoomList();
        });
    }

    @Override
    public void onDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            connectionStatusLabel.setText("未连接");
            connectButton.setEnabled(true);
        });
    }

    @Override
    public void onConnectionError(String error) {
        SwingUtilities.invokeLater(() -> {
            connectionStatusLabel.setText("连接错误: " + error);
            connectButton.setEnabled(true);
        });
    }

    @Override
    public void onMessageReceived(NetworkMessage message) {}

    @Override
    public void onRoomCreated(String roomId) {
        SwingUtilities.invokeLater(() -> {
            refreshRoomList();
            if ("chinese-chess".equals(selectedGameType)) {
                startNetworkGame(roomId, "");
            } else {
                startSelectedGame();
            }
        });
    }

    @Override
    public void onRoomJoined(String roomId, String opponentName) {
        SwingUtilities.invokeLater(() -> {
            refreshRoomList();
            if ("chinese-chess".equals(selectedGameType)) {
                startNetworkGame(roomId, opponentName);
            } else {
                startSelectedGame();
            }
        });
    }

    @Override
    public void onRoomListReceived(List<RoomInfo> rooms) {
        SwingUtilities.invokeLater(() -> {
            String selectedId = null;
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedId = (String) tableModel.getValueAt(selectedRow, 0);
            }

            tableModel.setRowCount(0);
            int rowIndex = 0;
            int toSelect = -1;
            for (RoomInfo room : rooms) {
                Object[] row = {
                        room.getRoomId(),
                        room.getRoomName(),
                        GameDisplay.name(room.getGameType()),
                        room.getCurrentPlayers() + "/" + room.getMaxPlayers(),
                        room.getHostName()
                };
                tableModel.addRow(row);
                if (selectedId != null && selectedId.equals(room.getRoomId())) {
                    toSelect = rowIndex;
                }
                rowIndex++;
            }
            if (toSelect >= 0) {
                roomTable.setRowSelectionInterval(toSelect, toSelect);
            }
            connectionStatusLabel.setText("房间列表已更新");
        });
    }

    @Override
    public void onGameStarted(String redPlayer, String blackPlayer, String yourColor) {}

    @Override
    public void onMoveReceived(int fromRow, int fromCol, int toRow, int toCol) {}

    @Override
    public void onGameEnded(String winner, String reason) {}

    @Override
    public void onGameStateUpdate(String gameState, String currentPlayer, boolean isGameOver, String winner) {}

    @Override
    public void onError(String error) {
        JOptionPane.showMessageDialog(this, error, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private void startSelectedGame() {
        switch (selectedGameType) {
            case "international-chess":
                startInternationalChess();
                break;
            case "gomoku":
                startGomoku();
                break;
            case "go-game":
                startGoGame();
                break;
            case "tank-battle-game":
                startTankBattle();
                break;
            case "monopoly":
                startMonopoly();
                break;
            case "chinese-chess":
            default:
                startChineseChess();
                break;
        }
    }

    private void startChineseChess() {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new GameFrame();
                showGameWindow(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startInternationalChess() {
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> gameFrameClass = Class.forName("com.example.internationalchess.InternationalChessFrame");
                JFrame frame = (JFrame) gameFrameClass.getDeclaredConstructor().newInstance();
                showGameWindow(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startGomoku() {
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> gameFrameClass = Class.forName("com.example.gomoku.GomokuFrame");
                JFrame frame = (JFrame) gameFrameClass.getDeclaredConstructor().newInstance();
                showGameWindow(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startGoGame() {
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> gameFrameClass = Class.forName("com.example.go.GoFrame");
                JFrame frame = (JFrame) gameFrameClass.getDeclaredConstructor().newInstance();
                showGameWindow(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startTankBattle() {
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> gameClass = Class.forName("com.tankbattle.TankBattleGame");
                try {
                    java.lang.reflect.Constructor<?> ctor = gameClass.getDeclaredConstructor(Runnable.class);
                    GameCenterFrame.this.setVisible(false);
                    Runnable onExit = () -> SwingUtilities.invokeLater(() -> {
                        GameContext.setSinglePlayer(false);
                        GameCenterFrame.this.setVisible(true);
                        GameCenterFrame.this.toFront();
                        refreshRoomList();
                    });
                    ctor.newInstance(onExit);
                } catch (NoSuchMethodException ex) {
                    gameClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startMonopoly() {
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> gameFrameClass = Class.forName("com.example.monopoly.MonopolyFrame");
                JFrame frame = (JFrame) gameFrameClass.getDeclaredConstructor().newInstance();
                showGameWindow(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startNetworkGame(String roomId, String opponentName) {
        try {
            GameFrame gameFrame = new GameFrame();
            gameFrame.setNetworkMode(true);
            gameFrame.setRoomInfo(roomId, "房间" + roomId);
            gameFrame.setLocalPlayerName(playerNameField.getText().trim());
            gameFrame.setNetworkClient(networkClient);
            GameStateSyncRequestMessage syncReq = new GameStateSyncRequestMessage(
                    networkClient.getPlayerId(), roomId, "listener_ready");
            networkClient.sendNetworkMessage(syncReq);
            gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    networkClient.leaveRoomSafely();
                    GameContext.setSinglePlayer(false);
                    GameCenterFrame.this.setVisible(true);
                    GameCenterFrame.this.toFront();
                    refreshRoomList();
                }
            });
            gameFrame.setVisible(true);
            setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGameWindow(JFrame frame) {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                GameContext.setSinglePlayer(false);
                GameCenterFrame.this.setVisible(true);
                GameCenterFrame.this.toFront();
                refreshRoomList();
            }
        });
        GameCenterFrame.this.setVisible(false);
        frame.setVisible(true);
    }
}

