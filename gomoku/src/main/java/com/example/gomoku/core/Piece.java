package com.example.gomoku.core;

/**
 * 五子棋棋子类
 */
public class Piece {
    private PieceColor color;
    private Position position;

    public Piece(PieceColor color, Position position) {
        this.color = color;
        this.position = position;
    }

    public PieceColor getColor() {
        return color;
    }

    public void setColor(PieceColor color) {
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "color=" + color +
                ", position=" + position +
                '}';
    }
}
