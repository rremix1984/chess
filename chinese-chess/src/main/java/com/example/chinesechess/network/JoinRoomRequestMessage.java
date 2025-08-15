package com.example.chinesechess.network;

/**
 * 加入房间请求消息
 */
public class JoinRoomRequestMessage extends NetworkMessage {
    private String roomId;
    private String password;
    
    public JoinRoomRequestMessage(String senderId, String roomId, String password) {
        super(MessageType.JOIN_ROOM_REQUEST, senderId);
        this.roomId = roomId;
        this.password = password;
    }
    
    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
