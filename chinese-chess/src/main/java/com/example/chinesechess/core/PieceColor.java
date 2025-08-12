package com.example.chinesechess.core;

public enum PieceColor {
    RED, BLACK;

    public PieceColor getOpposite() {
        return this == RED ? BLACK : RED;
    }
}