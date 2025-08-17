package com.example.go;

/**
 * 表示一个死活棋题，包括初始棋盘和先手方
 */
public class GoLifeAndDeathProblem {
    private final int[][] board;
    private final int startingPlayer;

    public GoLifeAndDeathProblem(int[][] board, int startingPlayer) {
        this.board = board;
        this.startingPlayer = startingPlayer;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getStartingPlayer() {
        return startingPlayer;
    }
}
