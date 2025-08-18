package com.example.flightchess.core;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public final int color;
    public final List<Piece> pieces = new ArrayList<>(4);
    public boolean isAI = false;

    public Player(int color) {
        this.color = color;
        for (int i = 0; i < 4; i++) {
            pieces.add(new Piece(color));
        }
    }
}
