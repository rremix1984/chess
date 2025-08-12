package com.example.gomoku.ai;

/**
 * AI结果封装类
 */
public class GomokuAIResult {
    private int row;
    private int col;
    private double score;
    private String thinking;

    public GomokuAIResult(int row, int col, double score, String thinking) {
        this.row = row;
        this.col = col;
        this.score = score;
        this.thinking = thinking;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public double getScore() { return score; }
    public String getThinking() { return thinking; }
}
