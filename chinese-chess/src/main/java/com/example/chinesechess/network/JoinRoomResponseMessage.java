package com.example.chinesechess.network;

/**
 * 加入房间响应消息
 */
public class JoinRoomResponseMessage extends NetworkMessage {
    private boolean success;
    private String roomId;
    private String opponentName;
    private String errorMessage;
    
    public JoinRoomResponseMessage(String senderId, boolean success, String roomId, String opponentName) {
        super(MessageType.JOIN_ROOM_RESPONSE, senderId);
        this.success = success;
        this.roomId = roomId;
        this.opponentName = opponentName;
    }
    
    public JoinRoomResponseMessage(String senderId, boolean success, String errorMessage) {
        super(MessageType.JOIN_ROOM_RESPONSE, senderId);
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
