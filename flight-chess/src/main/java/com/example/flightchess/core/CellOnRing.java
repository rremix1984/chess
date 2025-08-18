package com.example.flightchess.core;

/** Cell on the main ring path. */
public class CellOnRing extends Cell {
    public final int ringIndex;
    public CellOnRing(int idx) { this.ringIndex = idx; }
}
