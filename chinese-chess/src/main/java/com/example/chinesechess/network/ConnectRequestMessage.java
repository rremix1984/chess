package com.example.chinesechess.network;

/**
 * 连接请求消息
 */
public class ConnectRequestMessage extends NetworkMessage {
    private String playerName;
    private String clientVersion;
    
    public ConnectRequestMessage(String senderId, String playerName, String clientVersion) {
        super(MessageType.CONNECT_REQUEST, senderId);
        this.playerName = playerName;
        this.clientVersion = clientVersion;
    }
    
    // Getters and Setters
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getClientVersion() {
        return clientVersion;
    }
    
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }
}
