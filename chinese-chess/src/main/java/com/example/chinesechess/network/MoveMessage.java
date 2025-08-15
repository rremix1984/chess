package com.example.chinesechess.network;

/**
 * 棋子移动消息
 */
public class MoveMessage extends NetworkMessage {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private String moveNotation; // 标准象棋记谱法
    
    public MoveMessage(String senderId, int fromRow, int fromCol, int toRow, int toCol) {
        super(MessageType.MOVE, senderId);
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }
    
    public MoveMessage(String senderId, int fromRow, int fromCol, int toRow, int toCol, String moveNotation) {
        super(MessageType.MOVE, senderId);
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.moveNotation = moveNotation;
    }
    
    // Getters and Setters
    public int getFromRow() { return fromRow; }
    public void setFromRow(int fromRow) { this.fromRow = fromRow; }
    
    public int getFromCol() { return fromCol; }
    public void setFromCol(int fromCol) { this.fromCol = fromCol; }
    
    public int getToRow() { return toRow; }
    public void setToRow(int toRow) { this.toRow = toRow; }
    
    public int getToCol() { return toCol; }
    public void setToCol(int toCol) { this.toCol = toCol; }
    
    public String getMoveNotation() { return moveNotation; }
    public void setMoveNotation(String moveNotation) { this.moveNotation = moveNotation; }
}
