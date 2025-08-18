package com.example.flightchess.core;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Board coordinates and rule helpers for a 15x15 grid.
 * Generates 52-ring path and four 6-cell home lanes.
 */
public class Board {
    public static final int R = 0, G = 1, B = 2, Y = 3;
    public final int ringSize = 52;
    public final int gridN = 15;
    private final Point[] ring = new Point[ringSize];
    private final Map<Integer, Point[]> homeLanes = new HashMap<>();
    private final int[] startIndex = {0, 13, 26, 39};
    private final int[] entryIndex = new int[4];
    private final int[] accelIndex = new int[4];

    public Board() {
        buildRing();
        buildHomeLanes();
        for (int c = 0; c < 4; c++) {
            entryIndex[c] = stepOnRing(startIndex[c], 12);
            accelIndex[c] = stepOnRing(startIndex[c], 4);
        }
    }

    private void buildRing() {
        int i = 0;
        for (int x = 1; x <= 13; x++) ring[i++] = new Point(x, 14);
        for (int y = 13; y >= 1; y--) ring[i++] = new Point(14, y);
        for (int x = 13; x >= 1; x--) ring[i++] = new Point(x, 0);
        for (int y = 1; y <= 13; y++) ring[i++] = new Point(0, y);
        if (i != ringSize) throw new IllegalStateException("Ring size error: " + i);
    }

    private void buildHomeLanes() {
        homeLanes.put(R, new Point[]{
            new Point(7,13), new Point(7,12), new Point(7,11),
            new Point(7,10), new Point(7,9), new Point(7,8)
        });
        homeLanes.put(G, new Point[]{
            new Point(13,7), new Point(12,7), new Point(11,7),
            new Point(10,7), new Point(9,7), new Point(8,7)
        });
        homeLanes.put(B, new Point[]{
            new Point(7,1), new Point(7,2), new Point(7,3),
            new Point(7,4), new Point(7,5), new Point(7,6)
        });
        homeLanes.put(Y, new Point[]{
            new Point(1,7), new Point(2,7), new Point(3,7),
            new Point(4,7), new Point(5,7), new Point(6,7)
        });
    }

    public int startIndex(int color) { return startIndex[color]; }
    public int entryIndex(int color) { return entryIndex[color]; }
    public int accelIndex(int color) { return accelIndex[color]; }
    public Point[] homeLane(int color) { return homeLanes.get(color); }

    public int stepOnRing(int from, int steps) {
        int v = (from + steps) % ringSize;
        if (v < 0) v += ringSize;
        return v;
    }

    public Point ringCell(int ringIdx) { return ring[ringIdx]; }

    /**
     * Simple jump rule: landing on accelIndex allows jumping +4.
     */
    public Integer getJumpTarget(int color, int atRingIndex) {
        if (atRingIndex == accelIndex(color)) {
            return stepOnRing(atRingIndex, 4);
        }
        return null;
    }

    public int margin = 20;
    public int cell = 30;
    public int widthPx() { return margin * 2 + cell * gridN; }
    public int heightPx() { return margin * 2 + cell * gridN; }
    public Point gridToPixel(Point g) {
        int x = margin + g.x * cell;
        int y = margin + g.y * cell;
        return new Point(x, y);
    }
}
