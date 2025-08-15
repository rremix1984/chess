package com.example.chinesechess.network;

/**
 * 错误消息
 */
public class ErrorMessage extends NetworkMessage {
    private String errorCode;
    private String errorMessage;
    private String details;
    
    public ErrorMessage(String senderId, String errorCode, String errorMessage) {
        super(MessageType.ERROR, senderId);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public ErrorMessage(String senderId, String errorCode, String errorMessage, String details) {
        super(MessageType.ERROR, senderId);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
    
    // Getters and Setters
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
