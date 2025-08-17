package com.example.chinesechess.network;

/**
 * 房间信息类
 */
public class RoomInfo {
    private String roomId;
    private String roomName;
    private String hostName;
    private int currentPlayers;
    private int maxPlayers;
    private boolean hasPassword;
    private String gameStatus; // "waiting", "playing", "finished"
    private String gameType;   // 游戏类型
    
    public RoomInfo(String roomId, String roomName, String hostName, int currentPlayers,
                    int maxPlayers, boolean hasPassword, String gameStatus, String gameType) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.hostName = hostName;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.hasPassword = hasPassword;
        this.gameStatus = gameStatus;
        this.gameType = gameType;
    }
    
    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    
    public int getCurrentPlayers() { return currentPlayers; }
    public void setCurrentPlayers(int currentPlayers) { this.currentPlayers = currentPlayers; }
    
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    
    public boolean isHasPassword() { return hasPassword; }
    public void setHasPassword(boolean hasPassword) { this.hasPassword = hasPassword; }
    
    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String gameStatus) { this.gameStatus = gameStatus; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }
}
