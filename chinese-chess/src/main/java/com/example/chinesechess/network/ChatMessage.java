package com.example.chinesechess.network;

/**
 * 聊天消息
 */
public class ChatMessage extends NetworkMessage {
    private String content;
    private String targetType; // "room", "private", "global"
    private String targetId; // room ID or player ID
    
    public ChatMessage(String senderId, String content, String targetType, String targetId) {
        super(MessageType.CHAT, senderId);
        this.content = content;
        this.targetType = targetType;
        this.targetId = targetId;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}
