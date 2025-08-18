package com.example.flightchess.core;

import java.util.ArrayList;
import java.util.List;

public class Move {
    public Piece piece;
    public int dice;
    public List<Cell> path = new ArrayList<>();
    public List<Piece> kicked = new ArrayList<>();
}
