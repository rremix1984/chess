package com.example.chinesechess.network;

/**
 * 游戏开始消息
 */
public class GameStartMessage extends NetworkMessage {
    private String redPlayer;
    private String blackPlayer;
    private String yourColor; // "RED" or "BLACK"
    
    public GameStartMessage(String senderId, String redPlayer, String blackPlayer, String yourColor) {
        super(MessageType.GAME_START, senderId);
        this.redPlayer = redPlayer;
        this.blackPlayer = blackPlayer;
        this.yourColor = yourColor;
    }
    
    // Getters and Setters
    public String getRedPlayer() { return redPlayer; }
    public void setRedPlayer(String redPlayer) { this.redPlayer = redPlayer; }
    
    public String getBlackPlayer() { return blackPlayer; }
    public void setBlackPlayer(String blackPlayer) { this.blackPlayer = blackPlayer; }
    
    public String getYourColor() { return yourColor; }
    public void setYourColor(String yourColor) { this.yourColor = yourColor; }
}
