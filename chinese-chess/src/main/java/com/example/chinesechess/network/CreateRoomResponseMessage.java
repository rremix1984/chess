package com.example.chinesechess.network;

/**
 * 创建房间响应消息
 */
public class CreateRoomResponseMessage extends NetworkMessage {
    private boolean success;
    private String roomId;
    private String errorMessage;
    
    // 成功情况的构造函数
    public CreateRoomResponseMessage(String senderId, String roomId) {
        super(MessageType.CREATE_ROOM_RESPONSE, senderId);
        this.success = true;
        this.roomId = roomId;
    }
    
    // 失败情况的构造函数
    public static CreateRoomResponseMessage createErrorResponse(String senderId, String errorMessage) {
        CreateRoomResponseMessage response = new CreateRoomResponseMessage();
        response.setType(MessageType.CREATE_ROOM_RESPONSE);
        response.setSenderId(senderId);
        response.setMessageId("msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000));
        response.setTimestamp(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }
    
    // 私有默认构造函数，供工厂方法使用
    private CreateRoomResponseMessage() {
        super();
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
