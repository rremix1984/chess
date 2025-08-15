package network;

/**
 * 各种具体的网络消息类实现
 */
public class Messages {
    
    /**
     * 连接请求消息
     */
    public static class ConnectRequest implements NetworkMessage {
        private final String playerName;
        
        public ConnectRequest(String playerName) {
            this.playerName = playerName;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.CONNECT_REQUEST;
        }
    }
    
    /**
     * 连接响应消息
     */
    public static class ConnectResponse implements NetworkMessage {
        private final boolean success;
        private final String message;
        private final String playerId;
        
        public ConnectResponse(boolean success, String message, String playerId) {
            this.success = success;
            this.message = message;
            this.playerId = playerId;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getPlayerId() {
            return playerId;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.CONNECT_RESPONSE;
        }
    }
    
    /**
     * 创建房间请求消息
     */
    public static class CreateRoomRequest implements NetworkMessage {
        private final String roomName;
        private final String password;
        
        public CreateRoomRequest(String roomName, String password) {
            this.roomName = roomName;
            this.password = password;
        }
        
        public String getRoomName() {
            return roomName;
        }
        
        public String getPassword() {
            return password;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.CREATE_ROOM_REQUEST;
        }
    }
    
    /**
     * 创建房间响应消息
     */
    public static class CreateRoomResponse implements NetworkMessage {
        private final boolean success;
        private final String message;
        private final String roomId;
        
        public CreateRoomResponse(boolean success, String message, String roomId) {
            this.success = success;
            this.message = message;
            this.roomId = roomId;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getRoomId() {
            return roomId;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.CREATE_ROOM_RESPONSE;
        }
    }
    
    /**
     * 加入房间请求消息
     */
    public static class JoinRoomRequest implements NetworkMessage {
        private final String roomId;
        private final String password;
        
        public JoinRoomRequest(String roomId, String password) {
            this.roomId = roomId;
            this.password = password;
        }
        
        public String getRoomId() {
            return roomId;
        }
        
        public String getPassword() {
            return password;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.JOIN_ROOM_REQUEST;
        }
    }
    
    /**
     * 加入房间响应消息
     */
    public static class JoinRoomResponse implements NetworkMessage {
        private final boolean success;
        private final String message;
        private final String roomId;
        private final String opponentName;
        
        public JoinRoomResponse(boolean success, String message, String roomId, String opponentName) {
            this.success = success;
            this.message = message;
            this.roomId = roomId;
            this.opponentName = opponentName;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getRoomId() {
            return roomId;
        }
        
        public String getOpponentName() {
            return opponentName;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.JOIN_ROOM_RESPONSE;
        }
    }
    
    /**
     * 棋子移动消息
     */
    public static class MoveMessage implements NetworkMessage {
        private final int fromRow;
        private final int fromCol;
        private final int toRow;
        private final int toCol;
        private final long timestamp;
        
        public MoveMessage(int fromRow, int fromCol, int toRow, int toCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.timestamp = System.currentTimeMillis();
        }
        
        public int getFromRow() {
            return fromRow;
        }
        
        public int getFromCol() {
            return fromCol;
        }
        
        public int getToRow() {
            return toRow;
        }
        
        public int getToCol() {
            return toCol;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.MOVE;
        }
    }
    
    /**
     * 游戏状态同步消息
     */
    public static class GameStateSync implements NetworkMessage {
        private final String gameState;
        private final String currentPlayer;
        private final boolean isGameOver;
        private final String winner;
        
        public GameStateSync(String gameState, String currentPlayer, boolean isGameOver, String winner) {
            this.gameState = gameState;
            this.currentPlayer = currentPlayer;
            this.isGameOver = isGameOver;
            this.winner = winner;
        }
        
        public String getGameState() {
            return gameState;
        }
        
        public String getCurrentPlayer() {
            return currentPlayer;
        }
        
        public boolean isGameOver() {
            return isGameOver;
        }
        
        public String getWinner() {
            return winner;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.GAME_STATE_SYNC;
        }
    }
    
    /**
     * 游戏开始消息
     */
    public static class GameStart implements NetworkMessage {
        private final String redPlayerName;
        private final String blackPlayerName;
        private final String yourColor;
        
        public GameStart(String redPlayerName, String blackPlayerName, String yourColor) {
            this.redPlayerName = redPlayerName;
            this.blackPlayerName = blackPlayerName;
            this.yourColor = yourColor;
        }
        
        public String getRedPlayerName() {
            return redPlayerName;
        }
        
        public String getBlackPlayerName() {
            return blackPlayerName;
        }
        
        public String getYourColor() {
            return yourColor;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.GAME_START;
        }
    }
    
    /**
     * 游戏结束消息
     */
    public static class GameEnd implements NetworkMessage {
        private final String winner;
        private final String reason;
        
        public GameEnd(String winner, String reason) {
            this.winner = winner;
            this.reason = reason;
        }
        
        public String getWinner() {
            return winner;
        }
        
        public String getReason() {
            return reason;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.GAME_END;
        }
    }
    
    /**
     * 心跳包消息
     */
    public static class Heartbeat implements NetworkMessage {
        private final long timestamp;
        
        public Heartbeat() {
            this.timestamp = System.currentTimeMillis();
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.HEARTBEAT;
        }
    }
    
    /**
     * 错误消息
     */
    public static class ErrorMessage implements NetworkMessage {
        private final String errorCode;
        private final String errorMessage;
        
        public ErrorMessage(String errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.ERROR;
        }
    }
    
    /**
     * 文本消息
     */
    public static class TextMessage implements NetworkMessage {
        private final String sender;
        private final String content;
        private final long timestamp;
        
        public TextMessage(String sender, String content) {
            this.sender = sender;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getSender() {
            return sender;
        }
        
        public String getContent() {
            return content;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.TEXT_MESSAGE;
        }
    }
    
    /**
     * 断开连接消息
     */
    public static class Disconnect implements NetworkMessage {
        private final String reason;
        
        public Disconnect(String reason) {
            this.reason = reason;
        }
        
        public String getReason() {
            return reason;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.DISCONNECT;
        }
    }
    
    /**
     * 离开房间消息
     */
    public static class LeaveRoom implements NetworkMessage {
        private final String playerId;
        
        public LeaveRoom(String playerId) {
            this.playerId = playerId;
        }
        
        public String getPlayerId() {
            return playerId;
        }
        
        @Override
        public MessageType getType() {
            return MessageType.LEAVE_ROOM;
        }
    }
}
