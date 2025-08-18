package com.example.flightchess.core;

import java.util.Random;

public class Rng {
    private final Random rand;
    public Rng() { this.rand = new Random(); }
    public Rng(long seed) { this.rand = new Random(seed); }
    public int next1to6() { return rand.nextInt(6) + 1; }
}
