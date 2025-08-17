package com.example.chinesechess.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 中国象棋网络游戏服务器
 * 负责管理客户端连接、房间创建与管理、游戏匹配等
 */
public class ChessGameServer {
    
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_CLIENTS = 100;
    
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private ExecutorService clientThreadPool;
    
    // 客户端管理
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    
    // 房间管理
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final AtomicInteger roomIdCounter = new AtomicInteger(1000);
    
    public ChessGameServer() {
        this(DEFAULT_PORT);
    }
    
    public ChessGameServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
            System.out.println("🚀 象棋游戏服务器启动，端口: " + port);
        } catch (IOException e) {
            System.err.println("❌ 服务器启动失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        isRunning = true;
        System.out.println("🌟 服务器开始监听客户端连接...");
        
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getRemoteSocketAddress().toString();
                System.out.println("🔗 新客户端连接: " + clientIP);
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientThreadPool.submit(clientHandler);
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("⚠️ 接受客户端连接失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        isRunning = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // 关闭所有客户端连接
            for (ClientHandler client : clients.values()) {
                client.disconnect("服务器关闭");
            }
            clients.clear();
            
            // 关闭线程池
            if (clientThreadPool != null) {
                clientThreadPool.shutdown();
                try {
                    if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        clientThreadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    clientThreadPool.shutdownNow();
                }
            }
            
            System.out.println("🛑 服务器已停止");
        } catch (IOException e) {
            System.err.println("⚠️ 停止服务器时发生错误: " + e.getMessage());
        }
    }
    
    // ==================== 客户端管理 ====================
    
    /**
     * 注册客户端
     */
    public void registerClient(String playerId, ClientHandler client) {
        clients.put(playerId, client);
        System.out.println("✅ 玩家注册: " + playerId + " (" + client.getPlayerName() + ")");
    }
    
    /**
     * 移除客户端
     */
    public void removeClient(String playerId) {
        ClientHandler client = clients.remove(playerId);
        if (client != null) {
            System.out.println("👋 玩家离线: " + playerId + " (" + client.getPlayerName() + ")");
            
            // 如果玩家在房间中，处理离开房间
            GameRoom room = findPlayerRoom(playerId);
            if (room != null) {
                leaveRoom(playerId, room.getRoomId());
            }
        }
    }
    
    /**
     * 获取客户端
     */
    public ClientHandler getClient(String playerId) {
        return clients.get(playerId);
    }
    
    // ==================== 房间管理 ====================
    
    /**
     * 创建房间
     */
    public String createRoom(String hostPlayerId, String roomName, String password, String gameType) {
        ClientHandler host = clients.get(hostPlayerId);
        if (host == null) {
            return null;
        }
        
        String roomId = "room_" + roomIdCounter.getAndIncrement();
        GameRoom room = new GameRoom(roomId, roomName, password, hostPlayerId, host.getPlayerName(), gameType);
        rooms.put(roomId, room);
        
        System.out.println("🏠 房间创建: " + roomId + " (" + roomName + ") by " + host.getPlayerName());
        return roomId;
    }
    
    /**
     * 加入房间
     */
    public boolean joinRoom(String playerId, String roomId, String password) {
        GameRoom room = rooms.get(roomId);
        ClientHandler player = clients.get(playerId);
        
        if (room == null || player == null) {
            return false;
        }
        
        if (!room.getPassword().isEmpty() && !room.getPassword().equals(password)) {
            return false; // 密码错误
        }
        
        if (room.isFull()) {
            return false; // 房间已满
        }
        
        // 加入房间
        boolean joined = room.addPlayer(playerId, player.getPlayerName());
        if (joined) {
            System.out.println("🚪 玩家加入房间: " + player.getPlayerName() + " -> " + roomId);
            
            // 如果房间满员，开始游戏
            if (room.isFull()) {
                startGame(room);
            }
        }
        
        return joined;
    }
    
    /**
     * 离开房间
     */
    public void leaveRoom(String playerId, String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            ClientHandler player = clients.get(playerId);
            String playerName = player != null ? player.getPlayerName() : playerId;
            
            room.removePlayer(playerId);
            System.out.println("🚪 玩家离开房间: " + playerName + " <- " + roomId);
            
            // 通知房间内其他玩家
            notifyRoomPlayers(room, new DisconnectMessage(playerId, "玩家离开房间"));
            
            // 如果房间为空，删除房间
            if (room.isEmpty()) {
                rooms.remove(roomId);
                System.out.println("🗑️ 删除空房间: " + roomId);
            }
        }
    }
    
    /**
     * 获取房间列表
     */
    public List<RoomInfo> getRoomList(String gameType) {
        List<RoomInfo> roomList = new ArrayList<>();
        for (GameRoom room : rooms.values()) {
            if (gameType == null || gameType.isEmpty() || gameType.equals(room.getGameType())) {
                RoomInfo info = new RoomInfo(
                    room.getRoomId(),
                    room.getRoomName(),
                    room.getHostName(),
                    room.getPlayerCount(),
                    2, // 最大玩家数
                    false, // 没有密码
                    room.getGameState(),
                    room.getGameType()
                );
                roomList.add(info);
            }
        }
        return roomList;
    }
    
    /**
     * 查找玩家所在房间
     */
    private GameRoom findPlayerRoom(String playerId) {
        for (GameRoom room : rooms.values()) {
            if (room.hasPlayer(playerId)) {
                return room;
            }
        }
        return null;
    }
    
    // ==================== 游戏逻辑 ====================
    
    /**
     * 开始游戏
     */
    private void startGame(GameRoom room) {
        if (!room.isFull()) {
            return;
        }
        
        room.setGameState("PLAYING");
        
        // 房主为红方，后加入者为黑方
        String redPlayer = room.getHostId();  // 房主执红方
        String blackPlayer = null;
        
        // 找到非房主的玩家作为黑方
        for (String playerId : room.getPlayerIds()) {
            if (!playerId.equals(redPlayer)) {
                blackPlayer = playerId;
                break;
            }
        }
        
        room.setRedPlayer(redPlayer);
        room.setBlackPlayer(blackPlayer);
        
        // 调试信息：确认颜色分配
        System.out.println("🎯 颜色分配确认: 红方(房主)=" + redPlayer + ", 黑方(后加入)=" + blackPlayer);
        
        // 通知所有玩家游戏开始
        ClientHandler redClient = clients.get(redPlayer);
        ClientHandler blackClient = clients.get(blackPlayer);
        
        if (redClient != null && blackClient != null) {
            GameStartMessage redMessage = new GameStartMessage(
                "server", redClient.getPlayerName(), blackClient.getPlayerName(), "RED"
            );
            GameStartMessage blackMessage = new GameStartMessage(
                "server", redClient.getPlayerName(), blackClient.getPlayerName(), "BLACK"
            );
            
            redClient.sendMessage(redMessage);
            blackClient.sendMessage(blackMessage);
            
            System.out.println("🎮 游戏开始: " + redClient.getPlayerName() + "(红) vs " + blackClient.getPlayerName() + "(黑)");
        }
    }
    
    /**
     * 转发移动消息
     */
    public void forwardMove(String fromPlayerId, MoveMessage moveMessage) {
        GameRoom room = findPlayerRoom(fromPlayerId);
        if (room == null || !room.getGameState().equals("PLAYING")) {
            return;
        }
        
        // 转发给对手
        for (String playerId : room.getPlayerIds()) {
            if (!playerId.equals(fromPlayerId)) {
                ClientHandler opponent = clients.get(playerId);
                if (opponent != null) {
                    opponent.sendMessage(moveMessage);
                }
                break;
            }
        }
    }
    
    /**
     * 通知房间内所有玩家
     */
    private void notifyRoomPlayers(GameRoom room, NetworkMessage message) {
        for (String playerId : room.getPlayerIds()) {
            ClientHandler client = clients.get(playerId);
            if (client != null) {
                client.sendMessage(message);
            }
        }
    }
    
    // ==================== 游戏房间内部类 ====================
    
    /**
     * 游戏房间
     */
    private static class GameRoom {
        private final String roomId;
        private final String roomName;
        private final String password;
        private final String hostId;
        private final String hostName;
        private final List<String> playerIds;
        private final List<String> playerNames;
        private String gameState;
        private String redPlayer;
        private String blackPlayer;
        private final String gameType;
        
        public GameRoom(String roomId, String roomName, String password, String hostId, String hostName, String gameType) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.password = password != null ? password : "";
            this.hostId = hostId;
            this.hostName = hostName;
            this.gameType = gameType;
            this.playerIds = new ArrayList<>();
            this.playerNames = new ArrayList<>();
            this.gameState = "WAITING";
            
            // 主机自动加入房间
            addPlayer(hostId, hostName);
        }
        
        public synchronized boolean addPlayer(String playerId, String playerName) {
            if (playerIds.size() >= 2 || playerIds.contains(playerId)) {
                return false;
            }
            
            playerIds.add(playerId);
            playerNames.add(playerName);
            return true;
        }
        
        public synchronized void removePlayer(String playerId) {
            int index = playerIds.indexOf(playerId);
            if (index != -1) {
                playerIds.remove(index);
                playerNames.remove(index);
            }
        }
        
        public boolean hasPlayer(String playerId) {
            return playerIds.contains(playerId);
        }
        
        public boolean isFull() {
            return playerIds.size() >= 2;
        }
        
        public boolean isEmpty() {
            return playerIds.isEmpty();
        }
        
        public int getPlayerCount() {
            return playerIds.size();
        }
        
        // Getters
        public String getRoomId() { return roomId; }
        public String getRoomName() { return roomName; }
        public String getPassword() { return password; }
        public String getHostId() { return hostId; }
        public String getHostName() { return hostName; }
        public List<String> getPlayerIds() { return new ArrayList<>(playerIds); }
        public List<String> getPlayerNames() { return new ArrayList<>(playerNames); }
        public String getGameState() { return gameState; }
        public String getRedPlayer() { return redPlayer; }
        public String getBlackPlayer() { return blackPlayer; }
        public String getGameType() { return gameType; }
        
        public void setGameState(String gameState) { this.gameState = gameState; }
        public void setRedPlayer(String redPlayer) { this.redPlayer = redPlayer; }
        public void setBlackPlayer(String blackPlayer) { this.blackPlayer = blackPlayer; }
    }
    
    // ==================== 主方法 ====================
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("⚠️ 端口号格式错误，使用默认端口: " + DEFAULT_PORT);
            }
        }
        
        ChessGameServer server = new ChessGameServer(port);
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 正在关闭服务器...");
            server.stop();
        }));
        
        try {
            server.start();
        } catch (Exception e) {
            System.err.println("❌ 服务器运行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 构建给定玩家的房间状态快照（用于同步）
     */
    public GameStateSyncResponseMessage buildSyncResponse(String requesterPlayerId, String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room == null || !room.hasPlayer(requesterPlayerId)) {
            return new GameStateSyncResponseMessage(
                "server",
                roomId,
                room == null ? "房间不存在" : "玩家不在该房间内"
            );
        }

        // 获取红黑双方玩家ID
        String redPlayerId = room.getRedPlayer();
        String blackPlayerId = room.getBlackPlayer();

        // 通过客户端映射获取玩家名称
        String redPlayerName = redPlayerId != null && clients.get(redPlayerId) != null
                ? clients.get(redPlayerId).getPlayerName() : "";
        String blackPlayerName = blackPlayerId != null && clients.get(blackPlayerId) != null
                ? clients.get(blackPlayerId).getPlayerName() : "";

        // 计算请求者颜色
        String yourColor = null;
        if (requesterPlayerId.equals(redPlayerId)) {
            yourColor = "RED";
        } else if (requesterPlayerId.equals(blackPlayerId)) {
            yourColor = "BLACK";
        } else {
            // 不是对局双方（旁观），暂不支持
            return new GameStateSyncResponseMessage("server", roomId, "当前仅支持对局双方同步");
        }

        // 当前服务器未跟踪轮到谁，开局默认红方先手
        String currentPlayer = "RED";

        // 游戏开始/结束状态
        boolean isGameStarted = "PLAYING".equalsIgnoreCase(room.getGameState());
        boolean isGameOver = false; // 服务器暂不跟踪胜负
        String winner = null;

        return new GameStateSyncResponseMessage(
            "server",
            room.getRoomId(),
            redPlayerName,
            blackPlayerName,
            yourColor,
            currentPlayer,
            room.getGameState(),
            isGameStarted,
            isGameOver,
            winner
        );
    }

}
