package com.example.go.lad;

public interface ProgressStore {
    ProblemProgress get(String id);
    void markPassed(String id, int moves, long timeMs, int usedHints);
}
