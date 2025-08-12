package com.example.chinesechess.core;

public class Elephant extends Piece {

    public Elephant(PieceColor color) {
        super(color);
    }

    @Override
    public String getChineseName() {
        return "象";
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

        // 检查是否过河
        if (this.color == PieceColor.RED) {
            if (endX < 5) {
                return false;
            }
        } else { // BLACK
            if (endX > 4) {
                return false;
            }
        }

        // 检查是否走“田”字
        int dx = Math.abs(startX - endX);
        int dy = Math.abs(startY - endY);
        if (dx != 2 || dy != 2) {
            return false;
        }

        // 检查是否塞象眼
        int eyeX = (startX + endX) / 2;
        int eyeY = (startY + endY) / 2;
        if (board.getPiece(eyeX, eyeY) != null) {
            return false;
        }

        return true;
    }

    @Override
    public Piece clone() {
        return new Elephant(this.color);
    }
}