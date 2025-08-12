package com.example.chinesechess.core;

public class Soldier extends Piece {

    public Soldier(PieceColor color) {
        super(color);
    }

    @Override
    public String getChineseName() {
        return "兵";
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

        int dx = endX - startX;
        int dy = Math.abs(startY - endY);

        if (this.color == PieceColor.RED) {
            // 红兵不能后退
            if (dx > 0) {
                return false;
            }

            // 过河前只能向前
            if (startX > 4) {
                if (dy != 0 || dx != -1) {
                    return false;
                }
            } else { // 过河后可以横走
                if (!((dx == -1 && dy == 0) || (dx == 0 && dy == 1))) {
                    return false;
                }
            }
        } else { // BLACK
            // 黑兵不能后退
            if (dx < 0) {
                return false;
            }

            // 过河前只能向前
            if (startX < 5) {
                if (dy != 0 || dx != 1) {
                    return false;
                }
            } else { // 过河后可以横走
                if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Piece clone() {
        return new Soldier(this.color);
    }
}