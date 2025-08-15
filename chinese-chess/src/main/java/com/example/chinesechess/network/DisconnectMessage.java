package com.example.chinesechess.network;

/**
 * 断开连接消息
 */
public class DisconnectMessage extends NetworkMessage {
    private String reason;
    
    public DisconnectMessage(String senderId, String reason) {
        super(MessageType.DISCONNECT, senderId);
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
