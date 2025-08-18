package com.example.go.lad;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for a life-and-death problem.
 */
public class GoLifeAndDeathProblem {
    public String id;
    public int size;
    public GoColor toPlay;
    public String goal;
    public List<GoStone> initial = new ArrayList<>();
    public List<String> hints = List.of();
    public List<GoPoint> answer = List.of();
}
