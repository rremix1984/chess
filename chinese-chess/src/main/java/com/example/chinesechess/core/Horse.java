package com.example.chinesechess.core;

public class Horse extends Piece {

    public Horse(PieceColor color) {
        super(color);
    }

    @Override
    public String getChineseName() {
        return "马";
    }

    @Override
    public boolean isValidMove(Board board, Position start, Position end) {
        int startX = start.getX();
        int startY = start.getY();
        int endX = end.getX();
        int endY = end.getY();

        // 检查目标位置是否有己方棋子
        Piece destinationPiece = board.getPiece(endX, endY);
        if (destinationPiece != null && destinationPiece.getColor() == this.color) {
            return false;
        }

        // 检查是否走“日”字
        int dx = Math.abs(startX - endX);
        int dy = Math.abs(startY - endY);
        if (!((dx == 1 && dy == 2) || (dx == 2 && dy == 1))) {
            return false;
        }

        // 检查是否蹩马腿
        if (dx == 2) { // 横向日字
            int legX = (startX + endX) / 2;
            if (board.getPiece(legX, startY) != null) {
                return false;
            }
        } else { // 纵向日字
            int legY = (startY + endY) / 2;
            if (board.getPiece(startX, legY) != null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Piece clone() {
        return new Horse(this.color);
    }
}