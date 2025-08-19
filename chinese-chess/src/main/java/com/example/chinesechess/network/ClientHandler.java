package com.example.chinesechess.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.chinesechess.util.RateLimitedLogger;

/**
 * 客户端处理器
 * 负责处理单个客户端的连接、消息接收和发送
 */
public class ClientHandler implements Runnable {
    
    private final Socket socket;
    private final ChessGameServer server;
    private BufferedReader reader;
    private PrintWriter writer;
    private String playerId;
    private String playerName;
    private boolean isConnected = false;
    
    public ClientHandler(Socket socket, ChessGameServer server) {
        this.socket = socket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            // 初始化输入输出流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            isConnected = true;
            
            // 消息循环
            String line;
            while (isConnected && (line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    processMessage(line);
                }
            }
            
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("⚠️ 客户端连接中断: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }
    
    /**
     * 处理接收到的消息
     */
    private void processMessage(String jsonMessage) {
        try {
            NetworkMessage message = NetworkMessage.fromJson(jsonMessage);
            RateLimitedLogger.log("client-recv-" + playerName + "-" + message.getType(),
                    "📨 收到客户端消息: " + message.getType() + " from " + playerName);
            
            handleMessage(message);
            
        } catch (Exception e) {
            System.err.println("❌ 消息处理失败: " + e.getMessage());
            System.err.println("原始消息: " + jsonMessage);
            sendError("INVALID_MESSAGE", "消息格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理具体的消息类型
     */
    private void handleMessage(NetworkMessage message) {
        try {
            switch (message.getType()) {
                case CONNECT_REQUEST:
                    handleConnectRequest((ConnectRequestMessage) message);
                    break;
                case CREATE_ROOM_REQUEST:
                    handleCreateRoomRequest((CreateRoomRequestMessage) message);
                    break;
                case JOIN_ROOM_REQUEST:
                    handleJoinRoomRequest((JoinRoomRequestMessage) message);
                    break;
                case ROOM_LIST_REQUEST:
                    handleRoomListRequest((RoomListRequestMessage) message);
                    break;
                case MOVE:
                    handleMoveMessage((MoveMessage) message);
                    break;
                case GAME_STATE_SYNC_REQUEST:
                    handleGameStateSyncRequest((GameStateSyncRequestMessage) message);
                    break;
                case LEAVE_ROOM:
                    handleLeaveRoomMessage((LeaveRoomMessage) message);
                    break;
                case DISCONNECT:
                    handleDisconnectMessage((DisconnectMessage) message);
                    break;
                case HEARTBEAT:
                    handleHeartbeatMessage((HeartbeatMessage) message);
                    break;
                case CHAT:
                    handleChatMessage((ChatMessage) message);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("❌ 处理消息时出错: " + e.getMessage());
            e.printStackTrace();
            sendError("PROCESSING_ERROR", "处理消息时出错: " + e.getMessage());
        }
    }
    
    /**
     * 处理连接请求
     */
    private void handleConnectRequest(ConnectRequestMessage request) {
        this.playerId = request.getSenderId();
        this.playerName = request.getPlayerName();
        
        // 注册客户端到服务器
        server.registerClient(playerId, this);
        
        // 发送连接成功响应
        ConnectResponseMessage response = new ConnectResponseMessage("server", true, playerId, "1.0.0");
        sendMessage(response);
    }
    
    /**
     * 处理创建房间请求
     */
    private void handleCreateRoomRequest(CreateRoomRequestMessage request) {
        String roomId = server.createRoom(playerId, request.getRoomName(), request.getPassword(), request.getGameType());
        
        if (roomId != null) {
            CreateRoomResponseMessage response = new CreateRoomResponseMessage("server", roomId);
            sendMessage(response);
        } else {
            CreateRoomResponseMessage response = CreateRoomResponseMessage.createErrorResponse("server", "创建房间失败");
            sendMessage(response);
        }
    }
    
    /**
     * 处理加入房间请求
     */
    private void handleJoinRoomRequest(JoinRoomRequestMessage request) {
        boolean success = server.joinRoom(playerId, request.getRoomId(), request.getPassword());
        
        if (success) {
            // 获取对手信息
            String opponentName = getOpponentName(request.getRoomId());
            JoinRoomResponseMessage response = new JoinRoomResponseMessage("server", true, request.getRoomId(), opponentName);
            sendMessage(response);
        } else {
            JoinRoomResponseMessage response = new JoinRoomResponseMessage("server", false, "加入房间失败");
            sendMessage(response);
        }
    }
    
    /**
     * 处理房间列表请求
     */
    private void handleRoomListRequest(RoomListRequestMessage request) {
        var roomList = server.getRoomList(request.getGameType());
        RoomListResponseMessage response = new RoomListResponseMessage(roomList);
        sendMessage(response);
        RateLimitedLogger.log("room-list-send-" + playerName,
                "📋 发送房间列表给: " + playerName + " (共" + roomList.size() + "个房间)");
    }
    
    /**
     * 处理移动消息
     */
    private void handleMoveMessage(MoveMessage move) {
        // 转发给对手
        server.forwardMove(playerId, move);
    }
    
    /**
     * 处理离开房间消息
     */
    private void handleLeaveRoomMessage(LeaveRoomMessage message) {
        server.leaveRoom(playerId, message.getRoomId());
    }
    
    /**
     * 处理断开连接消息
     */
    private void handleDisconnectMessage(DisconnectMessage message) {
        disconnect("客户端主动断开");
    }
    
    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(HeartbeatMessage message) {
        // 发送心跳响应
        HeartbeatMessage response = new HeartbeatMessage(playerId);
        sendMessage(response);
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(ChatMessage message) {
        // TODO: 实现聊天消息转发
    }
    
    /**
     * 发送消息给客户端
     */
    public synchronized void sendMessage(NetworkMessage message) {
        if (!isConnected || writer == null) {
            System.err.println("❌ 无法发送消息给 " + playerName + ": 连接已断开");
            return;
        }
        
        try {
            String json = message.toJson();
            writer.println(json);
            writer.flush();
            RateLimitedLogger.log("send-" + playerName + "-" + message.getType(),
                    "📤 发送消息给 " + playerName + ": " + message.getType());
        } catch (Exception e) {
            System.err.println("❌ 发送消息失败给 " + playerName + ": " + e.getMessage());
            disconnect("发送消息失败");
        }
    }
    
    /**
     * 发送错误消息
     */
    private void sendError(String errorCode, String errorMessage) {
        ErrorMessage error = new ErrorMessage("server", errorCode, errorMessage);
        sendMessage(error);
    }
    
    /**
     * 获取对手名称
     */
    private String getOpponentName(String roomId) {
        // TODO: 从服务器获取房间内的对手信息
        return "对手"; // 临时返回
    }
    
    /**
     * 断开连接
     */
    public void disconnect(String reason) {
        if (isConnected) {
            isConnected = false;
        }
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        isConnected = false;
        
        // 从服务器移除客户端
        if (playerId != null) {
            server.removeClient(playerId);
        }
        
        // 关闭IO流
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            // 忽略关闭异常
        }
        
        if (writer != null) {
            writer.close();
        }
        
        // 关闭socket
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // 忽略关闭异常
        }
    }
    

    /**
     * 处理游戏状态同步请求
     */
    private void handleGameStateSyncRequest(GameStateSyncRequestMessage request) {
        try {
            GameStateSyncResponseMessage resp = server.buildSyncResponse(playerId, request.getRoomId());
            sendMessage(resp);
        } catch (Exception e) {
            System.err.println("❌ 同步请求处理失败: " + e.getMessage());
            GameStateSyncResponseMessage err = new GameStateSyncResponseMessage("server", request.getRoomId(), "服务器内部错误");
            sendMessage(err);
        }
    }


    // ==================== Getters ====================
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public String getRemoteAddress() {
        return socket != null ? socket.getRemoteSocketAddress().toString() : "unknown";
    }
}
