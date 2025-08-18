package com.example.go.lad;

/**
 * Minimal Go game state for life-and-death training.
 */
public class GoGame {
    private GoColor[][] board;
    private GoColor toPlay = GoColor.BLACK;
    private int moves = 0;

    public GoGame(int size) {
        board = new GoColor[size + 1][size + 1]; // 1-based index
    }

    public void resetTo(GoLifeAndDeathProblem p) {
        board = new GoColor[p.size + 1][p.size + 1];
        for (GoStone s : p.initial) {
            if (s.x >= 1 && s.x < board.length && s.y >= 1 && s.y < board.length) {
                board[s.x][s.y] = s.c;
            }
        }
        toPlay = p.toPlay;
        moves = 0;
    }

    public void play(GoPoint pt) {
        if (pt.x >= 1 && pt.x < board.length && pt.y >= 1 && pt.y < board.length) {
            if (board[pt.x][pt.y] == null) {
                board[pt.x][pt.y] = toPlay;
                toPlay = (toPlay == GoColor.BLACK) ? GoColor.WHITE : GoColor.BLACK;
                moves++;
            }
        }
    }

    public GoColor get(int x, int y) {
        if (x >= 1 && x < board.length && y >= 1 && y < board.length) {
            return board[x][y];
        }
        return null;
    }

    public int getSize() {
        return board.length - 1;
    }

    public GoColor getToPlay() {
        return toPlay;
    }

    public int getMoveCount() {
        return moves;
    }
}
