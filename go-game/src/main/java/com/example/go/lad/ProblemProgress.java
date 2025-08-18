package com.example.go.lad;

/** Progress information for a solved problem. */
public class ProblemProgress {
    public String id;
    public boolean passed;
    public int bestMoves;
    public long bestTimeMs;
    public int usedHints;

    public ProblemProgress(String id) {
        this.id = id;
    }
}
