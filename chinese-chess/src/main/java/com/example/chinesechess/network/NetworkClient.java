package com.example.chinesechess.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/**
 * 网络客户端类
 * 负责与游戏服务器的连接、消息发送接收、事件处理等
 */
public class NetworkClient {
    
    private static final String CLIENT_VERSION = "1.0.0";
    private static final int HEARTBEAT_INTERVAL = 30000; // 30秒心跳间隔
    private static final int CONNECTION_TIMEOUT = 10000; // 10秒连接超时
    
    // 网络连接相关
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean isConnected = false;
    private volatile ConnectionState connectionState = ConnectionState.DISCONNECTED;
    
    // 客户端信息
    private String playerId;
    private String playerName;
    private String serverHost;
    private int serverPort;
    
    // 线程管理
    private ExecutorService executorService;
    private ScheduledExecutorService heartbeatScheduler;
    private Future<?> messageListenerTask;
    
    // 事件监听器
    private ClientEventListener eventListener;
    
    /**
     * 客户端事件监听器接口
     */
    public interface ClientEventListener {
        void onConnected();
        void onDisconnected(String reason);
        void onConnectionError(String error);
        void onMessageReceived(NetworkMessage message);
        void onRoomCreated(String roomId);
        void onRoomJoined(String roomId, String opponentName);
        void onRoomListReceived(java.util.List<RoomInfo> rooms);
        void onGameStarted(String redPlayer, String blackPlayer, String yourColor);
        void onMoveReceived(int fromRow, int fromCol, int toRow, int toCol);
        void onGameEnded(String winner, String reason);
        void onGameStateUpdate(String gameState, String currentPlayer, boolean isGameOver, String winner);
        void onError(String error);
    }
    
    /**
     * 构造函数
     */
    public NetworkClient() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "NetworkClient-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Heartbeat-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * 设置事件监听器
     */
    public void setEventListener(ClientEventListener listener) {
        this.eventListener = listener;
    }
    
    /**
     * 连接到服务器
     */
    public void connect(String host, int port, String playerName) {
        if (connectionState != ConnectionState.DISCONNECTED) {
            notifyError("Already connecting or connected");
            return;
        }

        connectionState = ConnectionState.CONNECTING;
        this.serverHost = host;
        this.serverPort = port;
        this.playerName = playerName;

        executorService.submit(() -> {
            try {
                System.out.println("🌐 正在连接服务器: " + host + ":" + port);
                
                // 创建socket连接
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                
                // 创建输入输出流
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                
                isConnected = true;
                connectionState = ConnectionState.CONNECTED;

                // 发送连接请求
                sendConnectionRequest();

                // 启动消息监听
                startMessageListener();

                // 启动心跳
                startHeartbeat();

                System.out.println("✅ 服务器连接成功");
                
            } catch (IOException e) {
                String error = "连接服务器失败: " + e.getMessage();
                System.err.println("❌ " + error);
                notifyConnectionError(error);
                cleanup();
            }
        });
    }
    
    /**
     * 发送连接请求
     */
    private void sendConnectionRequest() {
        ConnectRequestMessage request = new ConnectRequestMessage(
            generateClientId(), playerName, CLIENT_VERSION);
        sendMessage(request);
    }
    
    /**
     * 生成客户端ID
     */
    private String generateClientId() {
        return "client_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 启动消息监听线程
     */
    private void startMessageListener() {
        messageListenerTask = executorService.submit(() -> {
            try {
                String line;
                while (isConnected && (line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        processMessage(line);
                    }
                }
            } catch (IOException e) {
                if (isConnected) {
                    String reason = "消息监听中断: " + e.getMessage();
                    System.err.println("⚠️ " + reason);
                    notifyDisconnected(reason);
                }
            } finally {
                cleanup();
            }
        });
    }
    
    /**
     * 处理接收到的消息
     */
    private void processMessage(String jsonMessage) {
        try {
            NetworkMessage message = NetworkMessage.fromJson(jsonMessage);
            System.out.println("📨 收到消息: " + message.getType());
            
            SwingUtilities.invokeLater(() -> {
                handleMessage(message);
            });
            
        } catch (Exception e) {
            System.err.println("❌ 消息解析失败: " + e.getMessage());
            System.err.println("原始消息: " + jsonMessage);
        }
    }
    
    /**
     * 处理具体的消息类型
     */
    private void handleMessage(NetworkMessage message) {
        try {
            switch (message.getType()) {
                case CONNECT_RESPONSE:
                    handleConnectResponse((ConnectResponseMessage) message);
                    break;
                case CREATE_ROOM_RESPONSE:
                    handleCreateRoomResponse((CreateRoomResponseMessage) message);
                    break;
                case JOIN_ROOM_RESPONSE:
                    handleJoinRoomResponse((JoinRoomResponseMessage) message);
                    break;
                case ROOM_LIST_RESPONSE:
                    handleRoomListResponse((RoomListResponseMessage) message);
                    break;
                case GAME_START:
                    handleGameStart((GameStartMessage) message);
                    break;
                case MOVE:
                    handleMove((MoveMessage) message);
                    break;
                case GAME_END:
                    handleGameEnd((GameEndMessage) message);
                    break;
                case GAME_STATE_UPDATE:
                    handleGameStateUpdate((GameStateUpdateMessage) message);
                    break;
                case GAME_STATE_SYNC_REQUEST:
                    handleGameStateSyncRequest((GameStateSyncRequestMessage) message);
                    break;
                case GAME_STATE_SYNC_RESPONSE:
                    handleGameStateSyncResponse((GameStateSyncResponseMessage) message);
                    break;
                case ERROR:
                    handleError((ErrorMessage) message);
                    break;
                case HEARTBEAT:
                    // 心跳响应，无需特殊处理
                    break;
                default:
                    if (eventListener != null) {
                        eventListener.onMessageReceived(message);
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("❌ 消息处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理连接响应
     */
    private void handleConnectResponse(ConnectResponseMessage response) {
        if (response.isSuccess()) {
            this.playerId = response.getPlayerId();
            System.out.println("✅ 连接认证成功，玩家ID: " + playerId);
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> eventListener.onConnected());
            }
        } else {
            String error = "连接认证失败: " + response.getErrorMessage();
            System.err.println("❌ " + error);
            notifyError(error);
            disconnect();
        }
    }
    
    /**
     * 处理创建房间响应
     */
    private void handleCreateRoomResponse(CreateRoomResponseMessage response) {
        if (response.isSuccess()) {
            String roomId = response.getRoomId();
            System.out.println("🏠 房间创建成功: " + roomId);
            if (eventListener != null) {
                eventListener.onRoomCreated(roomId);
            }
        } else {
            String error = "房间创建失败: " + response.getErrorMessage();
            System.err.println("❌ " + error);
            notifyError(error);
        }
    }
    
    /**
     * 处理加入房间响应
     */
    private void handleJoinRoomResponse(JoinRoomResponseMessage response) {
        if (response.isSuccess()) {
            String roomId = response.getRoomId();
            String opponentName = response.getOpponentName();
            System.out.println("🚪 加入房间成功: " + roomId + ", 对手: " + opponentName);
            if (eventListener != null) {
                eventListener.onRoomJoined(roomId, opponentName);
            }
        } else {
            String error = "加入房间失败: " + response.getErrorMessage();
            System.err.println("❌ " + error);
            notifyError(error);
        }
    }
    
    /**
     * 处理房间列表响应
     */
    private void handleRoomListResponse(RoomListResponseMessage response) {
        System.out.println("📋 收到房间列表，共 " + response.getRooms().size() + " 个房间");
        if (eventListener != null) {
            eventListener.onRoomListReceived(response.getRooms());
        }
    }
    
    /**
     * 处理游戏开始消息
     */
    private void handleGameStart(GameStartMessage message) {
        System.out.println("🎮 NetworkClient 收到游戏开始消息: 红方=" + message.getRedPlayer() + 
                          ", 黑方=" + message.getBlackPlayer() + 
                          ", 我的颜色=" + message.getYourColor());
        System.out.println("📡 当前事件监听器类型: " + (eventListener != null ? eventListener.getClass().getSimpleName() : "null"));
        
        if (eventListener != null) {
            System.out.println("🔄 调用 eventListener.onGameStarted...");
            eventListener.onGameStarted(message.getRedPlayer(), 
                                      message.getBlackPlayer(), 
                                      message.getYourColor());
            System.out.println("✅ eventListener.onGameStarted 调用完成");
        } else {
            System.err.println("⚠️ 无法处理游戏开始消息：事件监听器为null！");
        }
    }
    
    /**
     * 处理移动消息
     */
    private void handleMove(MoveMessage message) {
        System.out.println("♟️ 收到对手移动: (" + message.getFromRow() + "," + message.getFromCol() + 
                          ") -> (" + message.getToRow() + "," + message.getToCol() + ")");
        if (eventListener != null) {
            eventListener.onMoveReceived(message.getFromRow(), message.getFromCol(),
                                       message.getToRow(), message.getToCol());
        }
    }
    
    /**
     * 处理游戏结束消息
     */
    private void handleGameEnd(GameEndMessage message) {
        System.out.println("🏁 游戏结束: 获胜者=" + message.getWinner() + ", 原因=" + message.getReason());
        if (eventListener != null) {
            eventListener.onGameEnded(message.getWinner(), message.getReason());
        }
    }
    
    /**
     * 处理游戏状态更新消息
     */
    private void handleGameStateUpdate(GameStateUpdateMessage message) {
        System.out.println("🔄 游戏状态更新: " + message.getGameState() + 
                          ", 当前玩家: " + message.getCurrentPlayer());
        if (eventListener != null) {
            eventListener.onGameStateUpdate(message.getGameState(), 
                                           message.getCurrentPlayer(),
                                           message.isGameOver(), 
                                           message.getWinner());
        }
    }
    
    /**
     * 处理游戏状态同步请求消息
     */
    private void handleGameStateSyncRequest(GameStateSyncRequestMessage message) {
        System.out.println("🔄 收到游戏状态同步请求: 房间ID=" + message.getRoomId() + ", 原因: " + message.getReason());
        // 客户端通常不处理请求消息，这是服务器端的事情
        // 如果需要，可以在这里添加适当的日志记录
        if (eventListener != null) {
            eventListener.onMessageReceived(message);
        }
    }
    
    /**
     * 处理游戏状态同步响应消息
     */
    private void handleGameStateSyncResponse(GameStateSyncResponseMessage response) {
        System.out.println("🔄 收到游戏状态同步响应: 房间ID=" + response.getRoomId() + ", 成功: " + response.isSuccess());
        
        if (response.isSuccess()) {
            // 同步成功，更新本地游戏状态
            System.out.println("✅ 游戏状态同步成功:");
            System.out.println("   - 红方: " + response.getRedPlayer());
            System.out.println("   - 黑方: " + response.getBlackPlayer());
            System.out.println("   - 您的颜色: " + response.getYourColor());
            System.out.println("   - 当前玩家: " + response.getCurrentPlayer());
            System.out.println("   - 游戏状态: " + response.getGameState());
            System.out.println("   - 游戏已开始: " + response.isGameStarted());
            System.out.println("   - 游戏结束: " + response.isGameOver());
            
            // 触发游戏开始事件（如果游戏已开始）
            if (response.isGameStarted() && eventListener != null) {
                System.out.println("🎮 通过状态同步触发游戏开始事件...");
                eventListener.onGameStarted(response.getRedPlayer(), 
                                          response.getBlackPlayer(), 
                                          response.getYourColor());
            }
            
            // 如果游戏结束，触发游戏结束事件
            if (response.isGameOver() && eventListener != null) {
                System.out.println("🏁 通过状态同步触发游戏结束事件...");
                eventListener.onGameEnded(response.getWinner(), "game_sync_recovered");
            }
            
        } else {
            // 同步失败
            String error = "游戏状态同步失败: " + response.getErrorMessage();
            System.err.println("❌ " + error);
            if (eventListener != null) {
                eventListener.onError(error);
            }
        }
    }
    
    /**
     * 处理错误消息
     */
    private void handleError(ErrorMessage message) {
        String error = "服务器错误 [" + message.getErrorCode() + "]: " + message.getErrorMessage();
        System.err.println("⚠️ " + error);
        notifyError(error);
    }
    
    /**
     * 启动心跳
     */
    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (isConnected && playerId != null) {
                sendHeartbeat();
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        HeartbeatMessage heartbeat = new HeartbeatMessage(playerId);
        sendMessage(heartbeat);
    }
    
    /**
     * 发送消息
     */
    private synchronized void sendMessage(NetworkMessage message) {
        if (!isConnected || writer == null) {
            System.err.println("❌ 无法发送消息: 未连接到服务器");
            return;
        }
        
        try {
            String json = message.toJson();
            writer.println(json);
            writer.flush();
            System.out.println("📤 发送消息: " + message.getType());
        } catch (Exception e) {
            System.err.println("❌ 发送消息失败: " + e.getMessage());
            notifyError("发送消息失败: " + e.getMessage());
        }
    }
    
    // ==================== 公共API方法 ====================
    
    /**
     * 创建房间
     */
    public void createRoom(String roomName, String password) {
        createRoom(roomName, password, "chinese-chess");
    }

    public void createRoom(String roomName, String password, String gameType) {
        if (!isConnected || playerId == null) {
            notifyError("未连接到服务器");
            return;
        }

        CreateRoomRequestMessage request = new CreateRoomRequestMessage(
            playerId, roomName, password, 2, gameType);
        sendMessage(request);
    }
    
    /**
     * 加入房间
     */
    public void joinRoom(String roomId, String password) {
        if (!isConnected || playerId == null) {
            notifyError("未连接到服务器");
            return;
        }
        
        JoinRoomRequestMessage request = new JoinRoomRequestMessage(
            playerId, roomId, password);
        sendMessage(request);
    }
    
    /**
     * 离开房间
     */
    public void leaveRoom() {
        if (!isConnected || playerId == null) {
            return;
        }
        
        LeaveRoomMessage message = new LeaveRoomMessage(playerId, null);
        sendMessage(message);
    }

    public void requestRoomList(String gameType) {
        if (!isConnected || playerId == null) {
            notifyError("未连接到服务器");
            return;
        }

        RoomListRequestMessage request = new RoomListRequestMessage(playerId, gameType);
        sendMessage(request);
    }

    public void requestRoomList() {
        requestRoomList(null);
    }

    /**
     * Leave room and swallow exceptions.
     */
    public void leaveRoomSafely() {
        try {
            leaveRoom();
        } catch (Exception ignored) {
        }
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
    
    /**
     * 发送移动
     */
    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isConnected || playerId == null) {
            notifyError("未连接到服务器");
            return;
        }
        
        MoveMessage move = new MoveMessage(playerId, fromRow, fromCol, toRow, toCol);
        sendMessage(move);
    }
    
    /**
     * 发送聊天消息
     */
    public void sendChatMessage(String content, String targetType, String targetId) {
        if (!isConnected || playerId == null) {
            notifyError("未连接到服务器");
            return;
        }
        
        ChatMessage chat = new ChatMessage(playerId, content, targetType, targetId);
        sendMessage(chat);
    }
    
    /**
     * 发送消息（公共方法）
     */
    public void sendNetworkMessage(NetworkMessage message) {
        sendMessage(message);
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (connectionState == ConnectionState.DISCONNECTED) {
            return;
        }
        if (isConnected && playerId != null) {
            DisconnectMessage message = new DisconnectMessage(playerId, "client_disconnect");
            sendMessage(message);
        }
        notifyDisconnected("客户端主动断开");
        cleanup();
    }
    
    /**
     * 关闭客户端
     */
    public void shutdown() {
        disconnect();
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
        }
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED &&
                socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    /**
     * 获取玩家ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * 获取玩家名称
     */
    public String getPlayerName() {
        return playerName;
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 清理资源
     */
    private void cleanup() {
        isConnected = false;
        connectionState = ConnectionState.DISCONNECTED;
        
        // 取消消息监听任务
        if (messageListenerTask != null && !messageListenerTask.isDone()) {
            messageListenerTask.cancel(true);
        }
        
        // 关闭网络连接
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } catch (Exception e) {
            // 忽略关闭异常
        }
        
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (Exception e) {
            // 忽略关闭异常
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            // 忽略关闭异常
        }
        
        playerId = null;
    }
    
    /**
     * 通知连接错误
     */
    private void notifyConnectionError(String error) {
        connectionState = ConnectionState.DISCONNECTED;
        SwingUtilities.invokeLater(() -> {
            if (eventListener != null) {
                eventListener.onConnectionError(error);
            }
        });
    }
    
    /**
     * 通知断开连接
     */
    private void notifyDisconnected(String reason) {
        connectionState = ConnectionState.DISCONNECTED;
        SwingUtilities.invokeLater(() -> {
            if (eventListener != null) {
                eventListener.onDisconnected(reason);
            }
        });
    }
    
    /**
     * 通知错误
     */
    private void notifyError(String error) {
        SwingUtilities.invokeLater(() -> {
            if (eventListener != null) {
                eventListener.onError(error);
            }
        });
    }
}
