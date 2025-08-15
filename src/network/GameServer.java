package network;

import network.Messages.*;
import network.Room.Player;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/**
 * 游戏服务器类
 * 处理客户端连接、房间管理和消息转发
 */
public class GameServer {
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, Room> rooms;
    private final Map<String, ClientHandler> clients;
    private final ExecutorService threadPool;
    private boolean isRunning;
    private final Object lock = new Object();
    
    // 服务器事件监听器接口
    public interface ServerEventListener {
        void onServerStarted(int port);
        void onServerStopped();
        void onClientConnected(String clientId, String playerName);
        void onClientDisconnected(String clientId);
        void onRoomCreated(String roomId, String roomName);
        void onRoomClosed(String roomId);
        void onError(String error);
        void onMessage(String message);
    }
    
    private ServerEventListener eventListener;
    
    public GameServer(int port) {
        this.port = port;
        this.rooms = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.isRunning = false;
    }
    
    public void setEventListener(ServerEventListener listener) {
        this.eventListener = listener;
    }
    
    /**
     * 启动服务器
     */
    public void start() throws IOException {
        if (isRunning) {
            return;
        }
        
        serverSocket = new ServerSocket(port);
        isRunning = true;
        
        if (eventListener != null) {
            eventListener.onServerStarted(port);
        }
        
        // 启动接受连接的线程
        threadPool.submit(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewClient(clientSocket);
                } catch (IOException e) {
                    if (isRunning) {
                        if (eventListener != null) {
                            eventListener.onError("接受客户端连接失败: " + e.getMessage());
                        }
                    }
                }
            }
        });
        
        // 启动心跳检测线程
        threadPool.submit(this::heartbeatTask);
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        isRunning = false;
        
        // 通知所有客户端服务器关闭
        synchronized (lock) {
            for (ClientHandler client : clients.values()) {
                client.sendMessage(new Disconnect("服务器关闭"));
                client.close();
            }
            clients.clear();
            rooms.clear();
        }
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            if (eventListener != null) {
                eventListener.onError("关闭服务器socket失败: " + e.getMessage());
            }
        }
        
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        
        if (eventListener != null) {
            eventListener.onServerStopped();
        }
    }
    
    /**
     * 处理新客户端连接
     */
    private void handleNewClient(Socket clientSocket) {
        try {
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            threadPool.submit(clientHandler);
        } catch (IOException e) {
            if (eventListener != null) {
                eventListener.onError("创建客户端处理器失败: " + e.getMessage());
            }
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 心跳检测任务
     */
    private void heartbeatTask() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(30000); // 30秒检测一次
                
                synchronized (lock) {
                    long currentTime = System.currentTimeMillis();
                    Iterator<ClientHandler> iterator = clients.values().iterator();
                    
                    while (iterator.hasNext()) {
                        ClientHandler client = iterator.next();
                        if (currentTime - client.getLastHeartbeat() > 60000) { // 60秒超时
                            if (eventListener != null) {
                                eventListener.onMessage("客户端 " + client.getPlayerId() + " 心跳超时，断开连接");
                            }
                            client.close();
                            iterator.remove();
                        } else {
                            // 发送心跳包
                            client.sendMessage(new Heartbeat());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * 客户端处理器
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final ObjectInputStream input;
        private final ObjectOutputStream output;
        private String playerId;
        private String playerName;
        private String currentRoomId;
        private long lastHeartbeat;
        
        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
            this.lastHeartbeat = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            try {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    NetworkMessage message = (NetworkMessage) input.readObject();
                    handleMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (isRunning) {
                    if (eventListener != null) {
                        eventListener.onMessage("客户端连接异常: " + e.getMessage());
                    }
                }
            } finally {
                disconnect();
            }
        }
        
        /**
         * 处理客户端消息
         */
        private void handleMessage(NetworkMessage message) {
            try {
                switch (message.getType()) {
                    case CONNECT_REQUEST:
                        handleConnectRequest((ConnectRequest) message);
                        break;
                    case CREATE_ROOM_REQUEST:
                        handleCreateRoomRequest((CreateRoomRequest) message);
                        break;
                    case JOIN_ROOM_REQUEST:
                        handleJoinRoomRequest((JoinRoomRequest) message);
                        break;
                    case LEAVE_ROOM:
                        handleLeaveRoom((LeaveRoom) message);
                        break;
                    case MOVE:
                        handleMove((MoveMessage) message);
                        break;
                    case HEARTBEAT:
                        handleHeartbeat((Heartbeat) message);
                        break;
                    case DISCONNECT:
                        handleDisconnect((Disconnect) message);
                        break;
                    default:
                        if (eventListener != null) {
                            eventListener.onError("未知消息类型: " + message.getType());
                        }
                        break;
                }
            } catch (Exception e) {
                if (eventListener != null) {
                    eventListener.onError("处理消息失败: " + e.getMessage());
                }
                sendMessage(new ErrorMessage("HANDLE_ERROR", "处理消息失败: " + e.getMessage()));
            }
        }
        
        /**
         * 处理连接请求
         */
        private void handleConnectRequest(ConnectRequest request) {
            this.playerName = request.getPlayerName();
            this.playerId = UUID.randomUUID().toString();
            
            synchronized (lock) {
                clients.put(playerId, this);
            }
            
            sendMessage(new ConnectResponse(true, "连接成功", playerId));
            
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onClientConnected(playerId, playerName));
            }
        }
        
        /**
         * 处理创建房间请求
         */
        private void handleCreateRoomRequest(CreateRoomRequest request) {
            if (playerId == null) {
                sendMessage(new CreateRoomResponse(false, "请先连接到服务器", null));
                return;
            }
            
            String roomId = UUID.randomUUID().toString().substring(0, 8);
            Player host = new Player(playerId, playerName);
            Room room = new Room(roomId, request.getRoomName(), request.getPassword(), host);
            
            synchronized (lock) {
                rooms.put(roomId, room);
            }
            
            this.currentRoomId = roomId;
            sendMessage(new CreateRoomResponse(true, "房间创建成功", roomId));
            
            if (eventListener != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onRoomCreated(roomId, request.getRoomName()));
            }
        }
        
        /**
         * 处理加入房间请求
         */
        private void handleJoinRoomRequest(JoinRoomRequest request) {
            if (playerId == null) {
                sendMessage(new JoinRoomResponse(false, "请先连接到服务器", null, null));
                return;
            }
            
            synchronized (lock) {
                Room room = rooms.get(request.getRoomId());
                if (room == null) {
                    sendMessage(new JoinRoomResponse(false, "房间不存在", null, null));
                    return;
                }
                
                if (!room.validatePassword(request.getPassword())) {
                    sendMessage(new JoinRoomResponse(false, "房间密码错误", null, null));
                    return;
                }
                
                if (room.getStatus() != Room.RoomStatus.WAITING) {
                    sendMessage(new JoinRoomResponse(false, "房间已满或游戏已开始", null, null));
                    return;
                }
                
                Player player = new Player(playerId, playerName);
                if (room.addPlayer(player)) {
                    this.currentRoomId = request.getRoomId();
                    String hostName = room.getHost().getPlayerName();
                    sendMessage(new JoinRoomResponse(true, "加入房间成功", request.getRoomId(), hostName));
                    
                    // 通知房主有新玩家加入
                    ClientHandler hostClient = clients.get(room.getHost().getPlayerId());
                    if (hostClient != null) {
                        hostClient.sendMessage(new JoinRoomResponse(true, playerName + " 加入了房间", request.getRoomId(), playerName));
                    }
                    
                    // 两个玩家都在房间时，可以开始游戏
                    if (room.getPlayers().size() == 2) {
                        // 自动设置玩家为准备状态
                        room.getHost().setReady(true);
                        room.getGuest().setReady(true);
                        
                        if (room.startGame()) {
                            // 通知双方游戏开始
                            GameStart gameStartHost = new GameStart(room.getHost().getPlayerName(), 
                                room.getGuest().getPlayerName(), "RED");
                            GameStart gameStartGuest = new GameStart(room.getHost().getPlayerName(), 
                                room.getGuest().getPlayerName(), "BLACK");
                                
                            hostClient.sendMessage(gameStartHost);
                            sendMessage(gameStartGuest);
                            
                            if (eventListener != null) {
                                SwingUtilities.invokeLater(() -> 
                                    eventListener.onMessage("房间 " + request.getRoomId() + " 游戏开始"));
                            }
                        }
                    }
                } else {
                    sendMessage(new JoinRoomResponse(false, "加入房间失败", null, null));
                }
            }
        }
        
        /**
         * 处理离开房间
         */
        private void handleLeaveRoom(LeaveRoom message) {
            if (currentRoomId == null) {
                return;
            }
            
            synchronized (lock) {
                Room room = rooms.get(currentRoomId);
                if (room != null) {
                    boolean shouldCloseRoom = room.removePlayer(playerId);
                    if (shouldCloseRoom) {
                        rooms.remove(currentRoomId);
                        if (eventListener != null) {
                            SwingUtilities.invokeLater(() -> 
                                eventListener.onRoomClosed(currentRoomId));
                        }
                    } else {
                        // 通知对手玩家离开
                        Player opponent = room.getOpponent(playerId);
                        if (opponent != null) {
                            ClientHandler opponentClient = clients.get(opponent.getPlayerId());
                            if (opponentClient != null) {
                                opponentClient.sendMessage(new GameEnd(opponent.getPlayerName(), "对手离开游戏"));
                            }
                        }
                    }
                }
            }
            
            this.currentRoomId = null;
        }
        
        /**
         * 处理玩家移动
         */
        private void handleMove(MoveMessage message) {
            if (currentRoomId == null) {
                sendMessage(new ErrorMessage("NO_ROOM", "不在任何房间中"));
                return;
            }
            
            synchronized (lock) {
                Room room = rooms.get(currentRoomId);
                if (room == null) {
                    sendMessage(new ErrorMessage("ROOM_NOT_FOUND", "房间不存在"));
                    return;
                }
                
                if (room.processMove(playerId, message.getFromRow(), message.getFromCol(), 
                                  message.getToRow(), message.getToCol())) {
                    
                    // 转发移动消息给对手
                    Player opponent = room.getOpponent(playerId);
                    if (opponent != null) {
                        ClientHandler opponentClient = clients.get(opponent.getPlayerId());
                        if (opponentClient != null) {
                            opponentClient.sendMessage(message);
                        }
                    }
                    
                    // 发送游戏状态同步
                    Room.GameState gameState = room.getGameState();
                    GameStateSync stateSync = new GameStateSync(
                        gameState.getGameBoard(),
                        gameState.getCurrentPlayer(),
                        gameState.isGameOver(),
                        gameState.getWinner()
                    );
                    
                    // 发送给房间内的所有玩家
                    for (Player player : room.getPlayers().values()) {
                        ClientHandler client = clients.get(player.getPlayerId());
                        if (client != null) {
                            client.sendMessage(stateSync);
                        }
                    }
                } else {
                    sendMessage(new ErrorMessage("INVALID_MOVE", "无效的移动"));
                }
            }
        }
        
        /**
         * 处理心跳包
         */
        private void handleHeartbeat(Heartbeat message) {
            this.lastHeartbeat = System.currentTimeMillis();
            // 回复心跳包
            sendMessage(new Heartbeat());
        }
        
        /**
         * 处理断开连接
         */
        private void handleDisconnect(Disconnect message) {
            disconnect();
        }
        
        /**
         * 发送消息给客户端
         */
        public synchronized void sendMessage(NetworkMessage message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                if (eventListener != null) {
                    eventListener.onError("发送消息失败: " + e.getMessage());
                }
            }
        }
        
        /**
         * 断开连接
         */
        private void disconnect() {
            // 离开当前房间
            if (currentRoomId != null) {
                handleLeaveRoom(new LeaveRoom(playerId));
            }
            
            // 从客户端列表中移除
            synchronized (lock) {
                clients.remove(playerId);
            }
            
            close();
            
            if (eventListener != null && playerId != null) {
                SwingUtilities.invokeLater(() -> 
                    eventListener.onClientDisconnected(playerId));
            }
        }
        
        /**
         * 关闭连接
         */
        public void close() {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
        
        // Getters
        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public long getLastHeartbeat() { return lastHeartbeat; }
    }
    
    // Getters
    public boolean isRunning() { return isRunning; }
    public int getPort() { return port; }
    public int getClientCount() { return clients.size(); }
    public int getRoomCount() { return rooms.size(); }
    
    /**
     * 获取服务器状态信息
     */
    public String getServerInfo() {
        return String.format("服务器运行中 - 端口: %d, 客户端: %d, 房间: %d", 
            port, clients.size(), rooms.size());
    }
}
