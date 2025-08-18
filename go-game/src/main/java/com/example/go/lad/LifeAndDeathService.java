package com.example.go.lad;

public interface LifeAndDeathService {
    void load(GoLifeAndDeathProblem p, GoGame game);
    JudgeResult judge(GoGame game, GoLifeAndDeathProblem p);
    String nextHint(GoLifeAndDeathProblem p, String currentHintsText);
}
