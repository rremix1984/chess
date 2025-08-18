package com.example.flightchess.core;

public class Piece {
    public final int color; // R/G/B/Y
    public boolean inHangar = true;
    public boolean inHomeLane = false;
    public int ringIndex = -1;
    public int lanePos = -1;
    public boolean finished = false;

    public Piece(int color) {
        this.color = color;
    }
}
