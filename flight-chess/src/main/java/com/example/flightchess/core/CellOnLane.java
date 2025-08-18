package com.example.flightchess.core;

/** Cell on player's home lane (approach to goal). */
public class CellOnLane extends Cell {
    public final int color;
    public final int lanePos; // 0..5
    public CellOnLane(int color, int lanePos) {
        this.color = color;
        this.lanePos = lanePos;
    }
}
