package com.example.chinesechess.network;

/**
 * 创建房间请求消息
 */
public class CreateRoomRequestMessage extends NetworkMessage {
    private String roomName;
    private String password;
    private int maxPlayers;
    private String gameType;

    public CreateRoomRequestMessage(String senderId, String roomName, String password,
                                    int maxPlayers, String gameType) {
        super(MessageType.CREATE_ROOM_REQUEST, senderId);
        this.roomName = roomName;
        this.password = password;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;
    }
    
    // Getters and Setters
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }
}
