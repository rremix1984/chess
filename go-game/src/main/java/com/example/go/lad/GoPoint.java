package com.example.go.lad;

/**
 * Simple coordinate on the Go board.
 */
public class GoPoint {
    public final int x;
    public final int y;

    public GoPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
