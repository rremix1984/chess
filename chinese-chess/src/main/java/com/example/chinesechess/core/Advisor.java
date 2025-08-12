package com.example.chinesechess.core;

public class Advisor extends Piece {

    public Advisor(PieceColor color) {
        super(color);
    }

    @Override
    public String getChineseName() {
        return "士";
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

        // 检查是否在九宫格内移动
        if (this.color == PieceColor.RED) {
            if (endX < 7 || endX > 9 || endY < 3 || endY > 5) {
                return false;
            }
        } else { // BLACK
            if (endX < 0 || endX > 2 || endY < 3 || endY > 5) {
                return false;
            }
        }

        // 检查是否沿对角线移动一格
        int dx = Math.abs(startX - endX);
        int dy = Math.abs(startY - endY);

        return dx == 1 && dy == 1;
    }

    @Override
    public Piece clone() {
        return new Advisor(this.color);
    }
}