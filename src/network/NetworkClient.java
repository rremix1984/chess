package network;

import network.Messages.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/**
 * 网络客户端类
 * 处理与服务器的连接、消息发送接收和断线重连
 */
public class NetworkClient {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean isConnected;
    private boolean shouldReconnect;
    private ExecutorService executor;
    private final Object lock = new Object();
    
    // 客户端事件监听器接口
    public interface ClientEventListener {
        void onConnected();
        void onDisconnected(String reason);
        void onConnectionError(String error);
        void onMessageReceived(NetworkMessage message);
        void onRoomCreated(String roomId);
        void onRoomJoined(String roomId, String opponentName);
        void onGameStarted(String redPlayer, String blackPlayer, String yourColor);
        void onGameEnded(String winner, String reason);
        void onMoveReceived(int fromRow, int fromCol, int toRow, int toCol);
        void onGameStateUpdate(String gameState, String currentPlayer, boolean isGameOver, String winner);
        void onError(String error);
    }
    
    private ClientEventListener eventListener;
    private String playerId;
    private String playerName;
    private String currentRoomId;
    private long lastHeartbeat;
    
    public NetworkClient() {
        this.executor = Executors.newCachedThreadPool();
        this.isConnected = false;
        this.shouldReconnect = false;
        this.lastHeartbeat = System.currentTimeMillis();
    }
    
    public void setEventListener(ClientEventListener listener) {
        this.eventListener = listener;
    }
    
    /**
     * 连接到服务器
     */
    public void connect(String host, int port, String playerName) {
        if (isConnected) {
            return;
        }
        
        this.serverHost = host;
        this.serverPort = port;
        this.playerName = playerName;
        this.shouldReconnect = true;
        
        executor.submit(this::connectInternal);
    }
    
    /**
     * 内部连接方法
     */
    private void connectInternal() {
        try {
            socket = new Socket(serverHost, serverPort);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            synchronized (lock) {
                isConnected = true;
            }
            
            // 发送连接请求
            sendMessage(new ConnectRequest(playerName));
            
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> eventListener.onConnected());
            }
            
            // 启动消息接收线程
            startMessageReceiver();
            
            // 启动心跳发送线程
            startHeartbeatSender();
            
        } catch (IOException e) {
            synchronized (lock) {
                isConnected = false;
            }
            
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onConnectionError("连接服务器失败: " + e.getMessage()));
            }
            
            // 尝试重连
            if (shouldReconnect) {
                scheduleReconnect();
            }
        }
    }
    
    /**
     * 启动消息接收线程
     */
    private void startMessageReceiver() {
        executor.submit(() -> {
            try {
                while (isConnected && !Thread.currentThread().isInterrupted()) {
                    NetworkMessage message = (NetworkMessage) input.readObject();
                    handleMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isConnected) {
                    handleDisconnection("接收消息异常: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 启动心跳发送线程
     */
    private void startHeartbeatSender() {
        executor.submit(() -> {
            while (isConnected && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000); // 30秒发送一次心跳
                    if (isConnected) {
                        sendMessage(new Heartbeat());
                        
                        // 检查心跳超时
                        if (System.currentTimeMillis() - lastHeartbeat > 120000) { // 2分钟超时
                            handleDisconnection("心跳超时");
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    /**
     * 处理收到的消息
     */
    private void handleMessage(NetworkMessage message) {
        try {
            switch (message.getType()) {
                case CONNECT_RESPONSE:
                    handleConnectResponse((ConnectResponse) message);
                    break;
                case CREATE_ROOM_RESPONSE:
                    handleCreateRoomResponse((CreateRoomResponse) message);
                    break;
                case JOIN_ROOM_RESPONSE:
                    handleJoinRoomResponse((JoinRoomResponse) message);
                    break;
                case GAME_START:
                    handleGameStart((GameStart) message);
                    break;
                case GAME_END:
                    handleGameEnd((GameEnd) message);
                    break;
                case MOVE:
                    handleMove((MoveMessage) message);
                    break;
                case GAME_STATE_SYNC:
                    handleGameStateSync((GameStateSync) message);
                    break;
                case HEARTBEAT:
                    handleHeartbeat((Heartbeat) message);
                    break;
                case ERROR:
                    handleError((ErrorMessage) message);
                    break;
                case DISCONNECT:
                    handleServerDisconnect((Disconnect) message);
                    break;
                default:
                    if (eventListener != null) {
                        SwingUtilities.invokeLater(() -> 
                            eventListener.onMessageReceived(message));
                    }
                    break;
            }
        } catch (Exception e) {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError("处理消息失败: " + e.getMessage()));
            }
        }
    }
    
    /**
     * 处理连接响应
     */
    private void handleConnectResponse(ConnectResponse response) {
        if (response.isSuccess()) {
            this.playerId = response.getPlayerId();
        } else {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onConnectionError(response.getMessage()));
            }
        }
    }
    
    /**
     * 处理创建房间响应
     */
    private void handleCreateRoomResponse(CreateRoomResponse response) {
        if (response.isSuccess()) {
            this.currentRoomId = response.getRoomId();
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onRoomCreated(response.getRoomId()));
            }
        } else {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError(response.getMessage()));
            }
        }
    }
    
    /**
     * 处理加入房间响应
     */
    private void handleJoinRoomResponse(JoinRoomResponse response) {
        if (response.isSuccess()) {
            this.currentRoomId = response.getRoomId();
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onRoomJoined(response.getRoomId(), response.getOpponentName()));
            }
        } else {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError(response.getMessage()));
            }
        }
    }
    
    /**
     * 处理游戏开始消息
     */
    private void handleGameStart(GameStart message) {
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> 
                eventListener.onGameStarted(
                    message.getRedPlayerName(), 
                    message.getBlackPlayerName(), 
                    message.getYourColor()));
        }
    }
    
    /**
     * 处理游戏结束消息
     */
    private void handleGameEnd(GameEnd message) {
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> 
                eventListener.onGameEnded(message.getWinner(), message.getReason()));
        }
    }
    
    /**
     * 处理移动消息
     */
    private void handleMove(MoveMessage message) {
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> 
                eventListener.onMoveReceived(
                    message.getFromRow(), 
                    message.getFromCol(), 
                    message.getToRow(), 
                    message.getToCol()));
        }
    }
    
    /**
     * 处理游戏状态同步消息
     */
    private void handleGameStateSync(GameStateSync message) {
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> 
                eventListener.onGameStateUpdate(
                    message.getGameState(), 
                    message.getCurrentPlayer(), 
                    message.isGameOver(), 
                    message.getWinner()));
        }
    }
    
    /**
     * 处理心跳包
     */
    private void handleHeartbeat(Heartbeat message) {
        this.lastHeartbeat = System.currentTimeMillis();
    }
    
    /**
     * 处理错误消息
     */
    private void handleError(ErrorMessage message) {
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> 
                eventListener.onError(message.getErrorMessage()));
        }
    }
    
    /**
     * 处理服务器断开连接消息
     */
    private void handleServerDisconnect(Disconnect message) {
        handleDisconnection("服务器断开连接: " + message.getReason());
    }
    
    /**
     * 发送消息
     */
    public synchronized void sendMessage(NetworkMessage message) {
        if (!isConnected || output == null) {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError("未连接到服务器"));
            }
            return;
        }
        
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError("发送消息失败: " + e.getMessage()));
            }
            handleDisconnection("发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建房间
     */
    public void createRoom(String roomName, String password) {
        if (!isConnected) {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError("未连接到服务器"));
            }
            return;
        }
        sendMessage(new CreateRoomRequest(roomName, password));
    }
    
    /**
     * 加入房间
     */
    public void joinRoom(String roomId, String password) {
        if (!isConnected) {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError("未连接到服务器"));
            }
            return;
        }
        sendMessage(new JoinRoomRequest(roomId, password));
    }
    
    /**
     * 离开房间
     */
    public void leaveRoom() {
        if (!isConnected) {
            return;
        }
        if (currentRoomId != null) {
            sendMessage(new LeaveRoom(playerId));
            currentRoomId = null;
        }
    }
    
    /**
     * 发送移动消息
     */
    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isConnected) {
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onError("未连接到服务器"));
            }
            return;
        }
        sendMessage(new MoveMessage(fromRow, fromCol, toRow, toCol));
    }
    
    /**
     * 处理断开连接
     */
    private void handleDisconnection(String reason) {
        synchronized (lock) {
            if (!isConnected) {
                return;
            }
            isConnected = false;
        }
        
        closeConnection();
        
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> 
                eventListener.onDisconnected(reason));
        }
        
        // 尝试重连
        if (shouldReconnect) {
            scheduleReconnect();
        }
    }
    
    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        executor.schedule(() -> {
            if (shouldReconnect && !isConnected) {
                connectInternal();
            }
        }, 5, TimeUnit.SECONDS);
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        shouldReconnect = false;
        
        if (isConnected) {
            sendMessage(new Disconnect("客户端主动断开"));
        }
        
        synchronized (lock) {
            isConnected = false;
        }
        
        closeConnection();
        
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> 
                eventListener.onDisconnected("主动断开连接"));
        }
    }
    
    /**
     * 关闭连接
     */
    private void closeConnection() {
        try {
            if (input != null) {
                input.close();
                input = null;
            }
            if (output != null) {
                output.close();
                output = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            // 忽略关闭异常
        }
        
        this.playerId = null;
        this.currentRoomId = null;
    }
    
    /**
     * 关闭客户端
     */
    public void shutdown() {
        disconnect();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    // Getters
    public boolean isConnected() { return isConnected; }
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public String getCurrentRoomId() { return currentRoomId; }
    public String getServerHost() { return serverHost; }
    public int getServerPort() { return serverPort; }
}
