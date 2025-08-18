package com.example.go.lad;

/**
 * Represents a stone placed on the board.
 */
public class GoStone {
    public final int x;
    public final int y;
    public final GoColor c;

    public GoStone(int x, int y, GoColor c) {
        this.x = x;
        this.y = y;
        this.c = c;
    }
}
