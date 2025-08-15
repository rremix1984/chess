package com.example.chinesechess.network;

/**
 * 游戏结束消息
 */
public class GameEndMessage extends NetworkMessage {
    private String winner; // "RED", "BLACK", or "DRAW"
    private String reason; // "checkmate", "resign", "timeout", "disconnect"
    
    public GameEndMessage(String senderId, String winner, String reason) {
        super(MessageType.GAME_END, senderId);
        this.winner = winner;
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
