package com.example.chinesechess.core;

public class General extends Piece {

    public General(PieceColor color) {
        super(color);
    }

    @Override
    public String getChineseName() {
        return this.color == PieceColor.RED ? "帅" : "将";
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

        // 检查是否只移动一格（横向或纵向）
        int dx = Math.abs(startX - endX);
        int dy = Math.abs(startY - endY);
        if (dx + dy != 1) {
            return false;
        }

        // 检查王对王规则
        return isKingSafe(board, start, end);
    }

    @Override
    public Piece clone() {
        return new General(this.color);
    }

    private boolean isKingSafe(Board board, Position start, Position end) {
        // 找到对方将的位置
        Piece opponentKing = null;
        Position opponentKingPosition = null;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                Piece p = board.getPiece(i, j);
                if (p instanceof General && p.getColor() != this.color) {
                    opponentKing = p;
                    opponentKingPosition = new Position(i, j);
                    break;
                }
            }
            if (opponentKing != null) {
                break;
            }
        }

        if (opponentKing == null) {
            // 理论上不应该发生，但作为防御性编程
            return true;
        }

        // 如果移动后在同一列
        if (end.getY() == opponentKingPosition.getY()) {
            // 检查中间是否有其他棋子
            int minY = Math.min(end.getX(), opponentKingPosition.getX());
            int maxY = Math.max(end.getX(), opponentKingPosition.getX());
            for (int i = minY + 1; i < maxY; i++) {
                if (board.getPiece(i, end.getY()) != null) {
                    return true; // 中间有子，安全
                }
            }
            return false; // 中间无子，危险
        }

        return true;
    }
}