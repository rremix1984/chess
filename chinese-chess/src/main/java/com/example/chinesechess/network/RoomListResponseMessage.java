package com.example.chinesechess.network;

import java.util.List;

/**
 * 房间列表响应消息
 */
public class RoomListResponseMessage extends NetworkMessage {
    private List<RoomInfo> rooms;
    
    public RoomListResponseMessage(String senderId, List<RoomInfo> rooms) {
        super(MessageType.ROOM_LIST_RESPONSE, senderId);
        this.rooms = rooms;
    }
    
    public RoomListResponseMessage(List<RoomInfo> rooms) {
        super(MessageType.ROOM_LIST_RESPONSE, "server");
        this.rooms = rooms;
    }
    
    // Getters and Setters
    public List<RoomInfo> getRooms() { return rooms; }
    public void setRooms(List<RoomInfo> rooms) { this.rooms = rooms; }
}
