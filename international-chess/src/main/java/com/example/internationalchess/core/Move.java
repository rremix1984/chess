package com.example.internationalchess.core;

/**
 * 移动类
 * 表示国际象棋中的一步棋
 */
public class Move {
    
    private Position from;
    private Position to;
    private char piece;
    private char capturedPiece;
    private char promotionPiece;
    private boolean isCastling;
    private boolean isEnPassant;
    private String notation;
    
    /**
     * 构造函数
     * @param from 起始位置
     * @param to 目标位置
     */
    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
        this.piece = ' ';
        this.capturedPiece = ' ';
        this.promotionPiece = ' ';
        this.isCastling = false;
        this.isEnPassant = false;
        this.notation = "";
    }
    
    /**
     * 构造函数
     * @param fromRow 起始行
     * @param fromCol 起始列
     * @param toRow 目标行
     * @param toCol 目标列
     */
    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(new Position(fromRow, fromCol), new Position(toRow, toCol));
    }
    
    /**
     * 完整构造函数
     * @param from 起始位置
     * @param to 目标位置
     * @param piece 移动的棋子
     * @param capturedPiece 被吃掉的棋子
     */
    public Move(Position from, Position to, char piece, char capturedPiece) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.promotionPiece = ' ';
        this.isCastling = false;
        this.isEnPassant = false;
        this.notation = "";
    }
    
    /**
     * 获取起始位置
     * @return 起始位置
     */
    public Position getFrom() {
        return from;
    }
    
    /**
     * 设置起始位置
     * @param from 起始位置
     */
    public void setFrom(Position from) {
        this.from = from;
    }
    
    /**
     * 获取目标位置
     * @return 目标位置
     */
    public Position getTo() {
        return to;
    }
    
    /**
     * 设置目标位置
     * @param to 目标位置
     */
    public void setTo(Position to) {
        this.to = to;
    }
    
    /**
     * 获取起始位置（别名）
     * @return 起始位置
     */
    public Position getStart() {
        return from;
    }
    
    /**
     * 获取结束位置（别名）
     * @return 结束位置
     */
    public Position getEnd() {
        return to;
    }
    
    /**
     * 获取移动的棋子
     * @return 棋子
     */
    public char getPiece() {
        return piece;
    }
    
    /**
     * 设置移动的棋子
     * @param piece 棋子
     */
    public void setPiece(char piece) {
        this.piece = piece;
    }
    
    /**
     * 获取被吃掉的棋子
     * @return 被吃掉的棋子
     */
    public char getCapturedPiece() {
        return capturedPiece;
    }
    
    /**
     * 设置被吃掉的棋子
     * @param capturedPiece 被吃掉的棋子
     */
    public void setCapturedPiece(char capturedPiece) {
        this.capturedPiece = capturedPiece;
    }
    
    /**
     * 获取升变棋子
     * @return 升变棋子
     */
    public char getPromotionPiece() {
        return promotionPiece;
    }
    
    /**
     * 设置升变棋子
     * @param promotionPiece 升变棋子
     */
    public void setPromotionPiece(char promotionPiece) {
        this.promotionPiece = promotionPiece;
    }
    
    /**
     * 是否为王车易位
     * @return 是否为王车易位
     */
    public boolean isCastling() {
        return isCastling;
    }
    
    /**
     * 设置是否为王车易位
     * @param castling 是否为王车易位
     */
    public void setCastling(boolean castling) {
        isCastling = castling;
    }
    
    /**
     * 是否为吃过路兵
     * @return 是否为吃过路兵
     */
    public boolean isEnPassant() {
        return isEnPassant;
    }
    
    /**
     * 设置是否为吃过路兵
     * @param enPassant 是否为吃过路兵
     */
    public void setEnPassant(boolean enPassant) {
        isEnPassant = enPassant;
    }
    
    /**
     * 获取移动记号
     * @return 移动记号
     */
    public String getNotation() {
        return notation;
    }
    
    /**
     * 设置移动记号
     * @param notation 移动记号
     */
    public void setNotation(String notation) {
        this.notation = notation;
    }
    
    /**
     * 是否为吃子
     * @return 是否为吃子
     */
    public boolean isCapture() {
        return capturedPiece != ' ' && capturedPiece != '\0';
    }
    
    /**
     * 是否为升变
     * @return 是否为升变
     */
    public boolean isPromotion() {
        return promotionPiece != ' ' && promotionPiece != '\0';
    }
    
    /**
     * 转换为数组格式
     * @return 数组格式 [fromRow, fromCol, toRow, toCol]
     */
    public int[] toArray() {
        return new int[]{from.getRow(), from.getCol(), to.getRow(), to.getCol()};
    }
    
    /**
     * 从数组创建移动
     * @param moveArray 移动数组 [fromRow, fromCol, toRow, toCol]
     * @return 移动对象
     */
    public static Move fromArray(int[] moveArray) {
        if (moveArray == null || moveArray.length < 4) {
            throw new IllegalArgumentException("Invalid move array");
        }
        return new Move(moveArray[0], moveArray[1], moveArray[2], moveArray[3]);
    }
    
    /**
     * 转换为UCI格式
     * @return UCI格式字符串
     */
    public String toUCI() {
        String uci = from.toChessNotation() + to.toChessNotation();
        if (isPromotion()) {
            uci += Character.toLowerCase(promotionPiece);
        }
        return uci;
    }
    
    /**
     * 从UCI格式创建移动
     * @param uci UCI格式字符串
     * @return 移动对象
     */
    public static Move fromUCI(String uci) {
        if (uci == null || uci.length() < 4) {
            throw new IllegalArgumentException("Invalid UCI format: " + uci);
        }
        
        Position from = Position.fromChessNotation(uci.substring(0, 2));
        Position to = Position.fromChessNotation(uci.substring(2, 4));
        Move move = new Move(from, to);
        
        if (uci.length() == 5) {
            move.setPromotionPiece(Character.toUpperCase(uci.charAt(4)));
        }
        
        return move;
    }
    
    /**
     * 检查两个移动是否相等
     * @param obj 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return from.equals(move.from) && to.equals(move.to) && 
               piece == move.piece && promotionPiece == move.promotionPiece;
    }
    
    /**
     * 获取哈希码
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return from.hashCode() * 64 + to.hashCode();
    }
    
    /**
     * 转换为字符串
     * @return 字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(from.toChessNotation()).append("-").append(to.toChessNotation());
        
        if (isCapture()) {
            sb.append("x");
        }
        
        if (isPromotion()) {
            sb.append("=").append(promotionPiece);
        }
        
        if (isCastling) {
            sb.append(" (O-O)");
        }
        
        if (isEnPassant) {
            sb.append(" (e.p.)");
        }
        
        return sb.toString();
    }
    
    /**
     * 创建移动的副本
     * @return 移动副本
     */
    public Move copy() {
        Move copy = new Move(from.copy(), to.copy(), piece, capturedPiece);
        copy.setPromotionPiece(promotionPiece);
        copy.setCastling(isCastling);
        copy.setEnPassant(isEnPassant);
        copy.setNotation(notation);
        return copy;
    }
}