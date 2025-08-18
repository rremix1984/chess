package com.example.go.lad;

import java.util.List;

public interface ProblemRepository {
    List<GoLifeAndDeathProblem> list(ProblemFilter f);
    GoLifeAndDeathProblem get(String id);
}
