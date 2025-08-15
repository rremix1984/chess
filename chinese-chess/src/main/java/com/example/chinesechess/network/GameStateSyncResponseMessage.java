package com.example.chinesechess.network;

/**
 * 游戏状态同步响应消息
 * 服务器用此消息响应客户端的游戏状态同步请求
 */
public class GameStateSyncResponseMessage extends NetworkMessage {
    private String roomId;
    private boolean success;
    private String errorMessage; // 如果同步失败，包含错误信息
    
    // 游戏状态信息（如果成功）
    private String redPlayer;
    private String blackPlayer;
    private String yourColor; // 客户端在游戏中的颜色："RED" 或 "BLACK"
    private String currentPlayer; // 当前轮到的玩家："RED" 或 "BLACK"
    private String gameState; // "playing", "check", "checkmate", "stalemate", "waiting_for_opponent"
    private boolean isGameStarted; // 游戏是否已开始
    private boolean isGameOver; // 游戏是否结束
    private String winner; // 获胜者（如果游戏结束）
    
    /**
     * 默认构造函数（用于序列化）
     */
    public GameStateSyncResponseMessage() {
        // 用于序列化
    }
    
    /**
     * 成功响应的构造函数
     * @param senderId 发送者ID（通常是服务器）
     * @param roomId 房间ID
     * @param redPlayer 红方玩家名称
     * @param blackPlayer 黑方玩家名称
     * @param yourColor 请求客户端的颜色
     * @param currentPlayer 当前轮到的玩家
     * @param gameState 当前游戏状态
     * @param isGameStarted 游戏是否已开始
     * @param isGameOver 游戏是否结束
     * @param winner 获胜者
     */
    public GameStateSyncResponseMessage(String senderId, String roomId, String redPlayer, String blackPlayer,
                                       String yourColor, String currentPlayer, String gameState,
                                       boolean isGameStarted, boolean isGameOver, String winner) {
        super(MessageType.GAME_STATE_SYNC_RESPONSE, senderId);
        this.roomId = roomId;
        this.success = true;
        this.errorMessage = null;
        this.redPlayer = redPlayer;
        this.blackPlayer = blackPlayer;
        this.yourColor = yourColor;
        this.currentPlayer = currentPlayer;
        this.gameState = gameState;
        this.isGameStarted = isGameStarted;
        this.isGameOver = isGameOver;
        this.winner = winner;
    }
    
    /**
     * 错误响应的构造函数
     * @param senderId 发送者ID（通常是服务器）
     * @param roomId 房间ID
     * @param errorMessage 错误信息
     */
    public GameStateSyncResponseMessage(String senderId, String roomId, String errorMessage) {
        super(MessageType.GAME_STATE_SYNC_RESPONSE, senderId);
        this.roomId = roomId;
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getRedPlayer() {
        return redPlayer;
    }
    
    public void setRedPlayer(String redPlayer) {
        this.redPlayer = redPlayer;
    }
    
    public String getBlackPlayer() {
        return blackPlayer;
    }
    
    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    }
    
    public String getYourColor() {
        return yourColor;
    }
    
    public void setYourColor(String yourColor) {
        this.yourColor = yourColor;
    }
    
    public String getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
    
    public String getGameState() {
        return gameState;
    }
    
    public void setGameState(String gameState) {
        this.gameState = gameState;
    }
    
    public boolean isGameStarted() {
        return isGameStarted;
    }
    
    public void setGameStarted(boolean gameStarted) {
        isGameStarted = gameStarted;
    }
    
    public boolean isGameOver() {
        return isGameOver;
    }
    
    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public void setWinner(String winner) {
        this.winner = winner;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("GameStateSyncResponseMessage[success=true, roomId=%s, yourColor=%s, currentPlayer=%s, gameState=%s, %s]",
                    roomId, yourColor, currentPlayer, gameState, super.toString());
        } else {
            return String.format("GameStateSyncResponseMessage[success=false, roomId=%s, error=%s, %s]",
                    roomId, errorMessage, super.toString());
        }
    }
}
