package com.example.chinesechess.network;

/**
 * 房间列表请求消息
 */
public class RoomListRequestMessage extends NetworkMessage {
    private String gameType;

    public RoomListRequestMessage(String senderId, String gameType) {
        super(MessageType.ROOM_LIST_REQUEST, senderId);
        this.gameType = gameType;
    }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }
}
