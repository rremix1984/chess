package com.example.chinesechess.core;

public class Cannon extends Piece {

    public Cannon(PieceColor color) {
        super(color);
    }

    @Override
    public String getChineseName() {
        return "炮";
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

        // 计算路径上的棋子数量
        int piecesInBetween = 0;
        if (startX == endX) { // 纵向
            int minY = Math.min(startY, endY);
            int maxY = Math.max(startY, endY);
            for (int i = minY + 1; i < maxY; i++) {
                if (board.getPiece(startX, i) != null) {
                    piecesInBetween++;
                }
            }
        } else { // 横向
            int minX = Math.min(startX, endX);
            int maxX = Math.max(startX, endX);
            for (int i = minX + 1; i < maxX; i++) {
                if (board.getPiece(i, startY) != null) {
                    piecesInBetween++;
                }
            }
        }

        // 根据目标位置是否有棋子和中间棋子数量判断
        if (destinationPiece == null) { // 移动
            return piecesInBetween == 0;
        } else { // 吃子
            return piecesInBetween == 1;
        }
    }

    @Override
    public Piece clone() {
        return new Cannon(this.color);
    }
}