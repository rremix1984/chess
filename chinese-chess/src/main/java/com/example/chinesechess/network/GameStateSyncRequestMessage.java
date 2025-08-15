package com.example.chinesechess.network;

/**
 * 游戏状态同步请求消息
 * 客户端用此消息向服务器请求当前游戏状态的同步
 */
public class GameStateSyncRequestMessage extends NetworkMessage {
    private String roomId;
    private String reason; // 请求同步的原因，用于调试和日志记录
    
    /**
     * 默认构造函数（用于序列化）
     */
    public GameStateSyncRequestMessage() {
        // 用于序列化
    }
    
    /**
     * 构造函数
     * @param senderId 发送者ID
     * @param roomId 房间ID
     * @param reason 请求同步的原因
     */
    public GameStateSyncRequestMessage(String senderId, String roomId, String reason) {
        super(MessageType.GAME_STATE_SYNC_REQUEST, senderId);
        this.roomId = roomId;
        this.reason = reason;
    }
    
    /**
     * 便利构造函数，自动设置默认原因
     * @param senderId 发送者ID
     * @param roomId 房间ID
     */
    public GameStateSyncRequestMessage(String senderId, String roomId) {
        this(senderId, roomId, "client_missed_game_start");
    }
    
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    @Override
    public String toString() {
        return String.format("GameStateSyncRequestMessage[roomId=%s, reason=%s, %s]", 
                roomId, reason, super.toString());
    }
}
