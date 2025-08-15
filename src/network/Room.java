package network;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏房间类
 * 管理房间状态、玩家信息和游戏逻辑
 */
public class Room {
    private final String roomId;
    private final String roomName;
    private final String password;
    private final Player host;
    private Player guest;
    private GameState gameState;
    private final Map<String, Player> players;
    private final long createTime;
    private boolean isGameStarted;
    
    /**
     * 房间状态枚举
     */
    public enum RoomStatus {
        WAITING,    // 等待玩家
        FULL,       // 房间已满
        PLAYING,    // 游戏中
        FINISHED    // 游戏结束
    }
    
    /**
     * 玩家类
     */
    public static class Player {
        private final String playerId;
        private final String playerName;
        private String color;  // "RED" 或 "BLACK"
        private boolean isReady;
        
        public Player(String playerId, String playerName) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.isReady = false;
        }
        
        // Getters and setters
        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public boolean isReady() { return isReady; }
        public void setReady(boolean ready) { isReady = ready; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Player player = (Player) obj;
            return Objects.equals(playerId, player.playerId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(playerId);
        }
    }
    
    /**
     * 游戏状态类
     */
    public static class GameState {
        private String currentPlayer;
        private boolean isGameOver;
        private String winner;
        private String gameBoard;  // 棋盘状态的字符串表示
        private int moveCount;
        
        public GameState() {
            this.currentPlayer = "RED";  // 红方先走
            this.isGameOver = false;
            this.moveCount = 0;
        }
        
        // Getters and setters
        public String getCurrentPlayer() { return currentPlayer; }
        public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
        public boolean isGameOver() { return isGameOver; }
        public void setGameOver(boolean gameOver) { isGameOver = gameOver; }
        public String getWinner() { return winner; }
        public void setWinner(String winner) { this.winner = winner; }
        public String getGameBoard() { return gameBoard; }
        public void setGameBoard(String gameBoard) { this.gameBoard = gameBoard; }
        public int getMoveCount() { return moveCount; }
        public void setMoveCount(int moveCount) { this.moveCount = moveCount; }
    }
    
    /**
     * 构造函数
     */
    public Room(String roomId, String roomName, String password, Player host) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.password = password;
        this.host = host;
        this.players = new ConcurrentHashMap<>();
        this.players.put(host.getPlayerId(), host);
        this.createTime = System.currentTimeMillis();
        this.gameState = new GameState();
        this.isGameStarted = false;
        
        // 房主默认是红方
        host.setColor("RED");
    }
    
    /**
     * 玩家加入房间
     */
    public synchronized boolean addPlayer(Player player) {
        if (players.size() >= 2) {
            return false;  // 房间已满
        }
        
        if (guest == null) {
            guest = player;
            player.setColor("BLACK");  // 客人默认是黑方
            players.put(player.getPlayerId(), player);
            return true;
        }
        
        return false;
    }
    
    /**
     * 玩家离开房间
     */
    public synchronized boolean removePlayer(String playerId) {
        Player player = players.remove(playerId);
        if (player == null) {
            return false;
        }
        
        if (player.equals(guest)) {
            guest = null;
        }
        
        // 如果房主离开，房间解散
        if (player.equals(host)) {
            return true;  // 表示房间需要解散
        }
        
        // 如果游戏正在进行中，游戏结束
        if (isGameStarted && !gameState.isGameOver()) {
            gameState.setGameOver(true);
            gameState.setWinner(host.equals(player) ? (guest != null ? guest.getPlayerName() : null) : host.getPlayerName());
        }
        
        return false;
    }
    
    /**
     * 开始游戏
     */
    public synchronized boolean startGame() {
        if (players.size() != 2 || isGameStarted) {
            return false;
        }
        
        // 检查所有玩家是否准备就绪
        for (Player player : players.values()) {
            if (!player.isReady()) {
                return false;
            }
        }
        
        isGameStarted = true;
        gameState = new GameState();
        return true;
    }
    
    /**
     * 处理玩家移动
     */
    public synchronized boolean processMove(String playerId, int fromRow, int fromCol, int toRow, int toCol) {
        if (!isGameStarted || gameState.isGameOver()) {
            return false;
        }
        
        Player player = players.get(playerId);
        if (player == null) {
            return false;
        }
        
        // 检查是否轮到该玩家
        if (!player.getColor().equals(gameState.getCurrentPlayer())) {
            return false;
        }
        
        // 这里可以添加具体的移动验证逻辑
        // 现在简单地切换到下一个玩家
        gameState.setCurrentPlayer(gameState.getCurrentPlayer().equals("RED") ? "BLACK" : "RED");
        gameState.setMoveCount(gameState.getMoveCount() + 1);
        
        return true;
    }
    
    /**
     * 结束游戏
     */
    public synchronized void endGame(String winner, String reason) {
        gameState.setGameOver(true);
        gameState.setWinner(winner);
        isGameStarted = false;
    }
    
    /**
     * 获取房间状态
     */
    public RoomStatus getStatus() {
        if (gameState.isGameOver()) {
            return RoomStatus.FINISHED;
        }
        if (isGameStarted) {
            return RoomStatus.PLAYING;
        }
        if (players.size() == 2) {
            return RoomStatus.FULL;
        }
        return RoomStatus.WAITING;
    }
    
    /**
     * 验证房间密码
     */
    public boolean validatePassword(String inputPassword) {
        if (password == null || password.isEmpty()) {
            return true;  // 没有密码
        }
        return password.equals(inputPassword);
    }
    
    /**
     * 获取对手信息
     */
    public Player getOpponent(String playerId) {
        if (host.getPlayerId().equals(playerId)) {
            return guest;
        } else if (guest != null && guest.getPlayerId().equals(playerId)) {
            return host;
        }
        return null;
    }
    
    // Getters
    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public Player getHost() { return host; }
    public Player getGuest() { return guest; }
    public GameState getGameState() { return gameState; }
    public Map<String, Player> getPlayers() { return players; }
    public long getCreateTime() { return createTime; }
    public boolean isGameStarted() { return isGameStarted; }
    public boolean hasPassword() { return password != null && !password.isEmpty(); }
    
    @Override
    public String toString() {
        return String.format("Room[%s] %s (%d/2) Status: %s", 
            roomId, roomName, players.size(), getStatus());
    }
}
