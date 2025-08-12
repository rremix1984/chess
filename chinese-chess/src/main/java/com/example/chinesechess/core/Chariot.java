package com.example.chinesechess.core;

public class Chariot extends Piece {

    public Chariot(PieceColor color) {
        super(color);
    }

    @Override
    public String getChineseName() {
        return "车";
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

        // 检查是否在同一直线
        if (startX != endX && startY != endY) {
            return false;
        }

        // 检查路径上是否有障碍物
        if (startX == endX) { // 纵向移动
            int minY = Math.min(startY, endY);
            int maxY = Math.max(startY, endY);
            for (int i = minY + 1; i < maxY; i++) {
                if (board.getPiece(startX, i) != null) {
                    return false;
                }
            }
        } else { // 横向移动
            int minX = Math.min(startX, endX);
            int maxX = Math.max(startX, endX);
            for (int i = minX + 1; i < maxX; i++) {
                if (board.getPiece(i, startY) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Piece clone() {
        return new Chariot(this.color);
    }
}