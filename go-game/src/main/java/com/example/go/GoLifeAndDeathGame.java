package com.example.go;

import java.util.ArrayList;
import java.util.List;

/**
 * 死活棋模块：提供预设死活题并使用KataGo进行对弈
 */
public class GoLifeAndDeathGame extends GoGame {
    private final List<GoLifeAndDeathProblem> problems = new ArrayList<>();
    private final KataGoAI ai;

    public GoLifeAndDeathGame() {
        super();
        ai = new KataGoAI(3);
        loadDefaultProblems();
    }

    private void loadDefaultProblems() {
        int[][] problem1 = new int[BOARD_SIZE][BOARD_SIZE];
        // 一个简单的角部死活棋题示例
        problem1[0][0] = BLACK;
        problem1[1][0] = WHITE;
        problem1[0][1] = WHITE;
        problems.add(new GoLifeAndDeathProblem(problem1, BLACK));
    }

    /**
     * 开始指定的死活题
     */
    public void startProblem(int index) {
        if (index < 0 || index >= problems.size()) {
            throw new IllegalArgumentException("Invalid problem index");
        }
        GoLifeAndDeathProblem problem = problems.get(index);
        loadPosition(problem.getBoard(), problem.getStartingPlayer());
    }

    /**
     * 让AI落子
     */
    public GoPosition aiMove() {
        if (!ai.initializeEngine()) {
            return null;
        }
        return ai.calculateBestMove(getBoard(), getCurrentPlayer());
    }

    public int getProblemCount() {
        return problems.size();
    }
}
