package com.example.gomoku.core;

public enum PieceColor {
    BLACK, WHITE;

    public PieceColor getOpposite() {
        return this == BLACK ? WHITE : BLACK;
    }
}
