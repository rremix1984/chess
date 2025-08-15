package com.example.chinesechess.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 网络消息基类
 * 定义了网络通信中所有消息的基本结构和序列化/反序列化方法
 */
public abstract class NetworkMessage {
    
    // JSON序列化工具（使用紧凑格式避免换行问题）
    private static final Gson gson = new GsonBuilder()
            .create();
    
    // 消息类型枚举
    public enum MessageType {
        // 连接相关
        CONNECT_REQUEST,        // 连接请求
        CONNECT_RESPONSE,       // 连接响应
        DISCONNECT,             // 断开连接
        
        // 房间相关
        CREATE_ROOM_REQUEST,    // 创建房间请求
        CREATE_ROOM_RESPONSE,   // 创建房间响应
        JOIN_ROOM_REQUEST,      // 加入房间请求
        JOIN_ROOM_RESPONSE,     // 加入房间响应
        LEAVE_ROOM,             // 离开房间
        ROOM_LIST_REQUEST,      // 房间列表请求
        ROOM_LIST_RESPONSE,     // 房间列表响应
        
        // 游戏相关
        GAME_START,             // 游戏开始
        GAME_END,               // 游戏结束
        MOVE,                   // 棋子移动
        GAME_STATE_UPDATE,      // 游戏状态更新
        GAME_STATE_SYNC_REQUEST, // 游戏状态同步请求
        GAME_STATE_SYNC_RESPONSE, // 游戏状态同步响应
        
        // 其他
        HEARTBEAT,              // 心跳
        ERROR,                  // 错误消息
        CHAT                    // 聊天消息
    }
    
    // 消息基本属性
    private MessageType type;
    private String messageId;
    private String timestamp;
    private String senderId;
    
    /**
     * 默认构造函数（用于序列化）
     */
    protected NetworkMessage() {
        // 用于序列化和工厂方法
    }
    
    /**
     * 构造函数
     */
    public NetworkMessage(MessageType type, String senderId) {
        this.type = type;
        this.senderId = senderId;
        this.messageId = generateMessageId();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * 生成唯一消息ID
     */
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters and Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    /**
     * 将消息序列化为JSON字符串
     */
    public String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * 从JSON字符串反序列化消息
     */
    public static NetworkMessage fromJson(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String typeStr = jsonObject.get("type").getAsString();
            MessageType type = MessageType.valueOf(typeStr);
            
            switch (type) {
                case CONNECT_REQUEST:
                    return gson.fromJson(json, ConnectRequestMessage.class);
                case CONNECT_RESPONSE:
                    return gson.fromJson(json, ConnectResponseMessage.class);
                case DISCONNECT:
                    return gson.fromJson(json, DisconnectMessage.class);
                case CREATE_ROOM_REQUEST:
                    return gson.fromJson(json, CreateRoomRequestMessage.class);
                case CREATE_ROOM_RESPONSE:
                    return gson.fromJson(json, CreateRoomResponseMessage.class);
                case JOIN_ROOM_REQUEST:
                    return gson.fromJson(json, JoinRoomRequestMessage.class);
                case JOIN_ROOM_RESPONSE:
                    return gson.fromJson(json, JoinRoomResponseMessage.class);
                case LEAVE_ROOM:
                    return gson.fromJson(json, LeaveRoomMessage.class);
                case ROOM_LIST_REQUEST:
                    return gson.fromJson(json, RoomListRequestMessage.class);
                case ROOM_LIST_RESPONSE:
                    return gson.fromJson(json, RoomListResponseMessage.class);
                case GAME_START:
                    return gson.fromJson(json, GameStartMessage.class);
                case GAME_END:
                    return gson.fromJson(json, GameEndMessage.class);
                case MOVE:
                    return gson.fromJson(json, MoveMessage.class);
                case GAME_STATE_UPDATE:
                    return gson.fromJson(json, GameStateUpdateMessage.class);
                case GAME_STATE_SYNC_REQUEST:
                    return gson.fromJson(json, GameStateSyncRequestMessage.class);
                case GAME_STATE_SYNC_RESPONSE:
                    return gson.fromJson(json, GameStateSyncResponseMessage.class);
                case HEARTBEAT:
                    return gson.fromJson(json, HeartbeatMessage.class);
                case ERROR:
                    return gson.fromJson(json, ErrorMessage.class);
                case CHAT:
                    return gson.fromJson(json, ChatMessage.class);
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize message: " + json, e);
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s[type=%s, id=%s, sender=%s, time=%s]", 
                getClass().getSimpleName(), type, messageId, senderId, timestamp);
    }
}
