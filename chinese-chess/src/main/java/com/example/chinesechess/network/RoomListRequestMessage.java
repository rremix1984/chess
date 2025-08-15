package com.example.chinesechess.network;

/**
 * 房间列表请求消息
 */
public class RoomListRequestMessage extends NetworkMessage {
    public RoomListRequestMessage(String senderId) {
        super(MessageType.ROOM_LIST_REQUEST, senderId);
    }
}
