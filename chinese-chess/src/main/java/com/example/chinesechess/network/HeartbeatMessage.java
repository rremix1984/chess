package com.example.chinesechess.network;

/**
 * 心跳消息
 */
public class HeartbeatMessage extends NetworkMessage {
    private long clientTime;
    
    public HeartbeatMessage(String senderId) {
        super(MessageType.HEARTBEAT, senderId);
        this.clientTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public long getClientTime() { return clientTime; }
    public void setClientTime(long clientTime) { this.clientTime = clientTime; }
}
