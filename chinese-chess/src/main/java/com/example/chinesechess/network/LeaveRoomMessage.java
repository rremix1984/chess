package com.example.chinesechess.network;

/**
 * 离开房间消息
 */
public class LeaveRoomMessage extends NetworkMessage {
    private String roomId;
    
    public LeaveRoomMessage(String senderId, String roomId) {
        super(MessageType.LEAVE_ROOM, senderId);
        this.roomId = roomId;
    }
    
    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}
