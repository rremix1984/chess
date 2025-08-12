package com.example.chinesechess.core;

public abstract class Piece {
    protected PieceColor color;

    public Piece(PieceColor color) {
        this.color = color;
    }

    public PieceColor getColor() {
        return color;
    }

    public abstract String getChineseName();

    public abstract boolean isValidMove(Board board, Position start, Position end);

    public abstract Piece clone();
}