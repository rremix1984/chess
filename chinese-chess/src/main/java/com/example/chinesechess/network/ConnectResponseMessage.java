package com.example.chinesechess.network;

/**
 * 连接响应消息
 */
public class ConnectResponseMessage extends NetworkMessage {
    private boolean success;
    private String playerId;
    private String serverVersion;
    private String errorMessage;
    
    public ConnectResponseMessage(String senderId, boolean success, String playerId, String serverVersion) {
        super(MessageType.CONNECT_RESPONSE, senderId);
        this.success = success;
        this.playerId = playerId;
        this.serverVersion = serverVersion;
    }
    
    public ConnectResponseMessage(String senderId, boolean success, String errorMessage) {
        super(MessageType.CONNECT_RESPONSE, senderId);
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getServerVersion() {
        return serverVersion;
    }
    
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
