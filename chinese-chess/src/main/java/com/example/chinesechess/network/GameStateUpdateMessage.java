package com.example.chinesechess.network;

/**
 * 游戏状态更新消息
 */
public class GameStateUpdateMessage extends NetworkMessage {
    private String gameState; // "playing", "check", "checkmate", "stalemate"
    private String currentPlayer; // "RED" or "BLACK"
    private boolean isGameOver;
    private String winner; // null if game not over
    
    public GameStateUpdateMessage(String senderId, String gameState, String currentPlayer, boolean isGameOver, String winner) {
        super(MessageType.GAME_STATE_UPDATE, senderId);
        this.gameState = gameState;
        this.currentPlayer = currentPlayer;
        this.isGameOver = isGameOver;
        this.winner = winner;
    }
    
    // Getters and Setters
    public String getGameState() { return gameState; }
    public void setGameState(String gameState) { this.gameState = gameState; }
    
    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
    
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }
    
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
}
