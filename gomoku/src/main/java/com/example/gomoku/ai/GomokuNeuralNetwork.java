package com.example.gomoku.ai;

import com.example.gomoku.core.GomokuBoard;
import java.util.Random;

/**
 * GomokuZero 神经网络类
 * 简化版本的神经网络，提供位置评估和先验概率
 * 在没有实际训练好的神经网络时，使用启发式规则代替
 */
public class GomokuNeuralNetwork {
    
    private Random random = new Random();
    
    // 棋形模式权重
    private static final double FIVE_IN_A_ROW = 100000.0;      // 五连
    private static final double OPEN_FOUR = 10000.0;          // 活四
    private static final double CLOSED_FOUR = 5000.0;         // 冲四
    private static final double OPEN_THREE = 1000.0;          // 活三
    private static final double CLOSED_THREE = 500.0;         // 眠三
    private static final double OPEN_TWO = 100.0;             // 活二
    private static final double CLOSED_TWO = 50.0;            // 眠二
    private static final double SINGLE = 1.0;                 // 单子
    
    /**
     * 获取走法的先验概率
     * 基于启发式规则评估位置的重要性
     */
    public double getPrior(GomokuZeroAI.BoardState state, int move) {
        int row = move / GomokuBoard.BOARD_SIZE;
        int col = move % GomokuBoard.BOARD_SIZE;
        
        // 基础概率
        double prior = 0.1;
        
        // 位置重要性评估
        prior += evaluatePosition(state, row, col);
        
        // 归一化到 [0, 1]
        return Math.max(0.01, Math.min(1.0, prior / 1000.0));
    }
    
    /**
     * 评估位置价值
     */
    public double evaluatePosition(GomokuZeroAI.BoardState state, int row, int col) {
        double score = 0.0;
        
        // 中心位置权重更高
        int centerDistance = Math.abs(row - GomokuBoard.BOARD_SIZE/2) + 
                           Math.abs(col - GomokuBoard.BOARD_SIZE/2);
        score += Math.max(0, 15 - centerDistance) * 5;
        
        // 评估四个方向的棋形
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            score += evaluateDirection(state, row, col, dir[0], dir[1], true);  // 当前玩家
            score += evaluateDirection(state, row, col, dir[0], dir[1], false) * 0.9; // 对手
        }
        
        // 邻近已有棋子的位置更重要
        score += countNeighbors(state, row, col) * 10;
        
        return score;
    }
    
    /**
     * 评估某个方向的棋形价值
     */
    private double evaluateDirection(GomokuZeroAI.BoardState state, int row, int col, 
                                   int deltaRow, int deltaCol, boolean forCurrentPlayer) {
        
        char[][] board = state.getBoard();
        boolean isBlackTurn = state.isBlackTurn();
        char targetPiece = forCurrentPlayer ? 
                          (isBlackTurn ? GomokuBoard.BLACK : GomokuBoard.WHITE) :
                          (isBlackTurn ? GomokuBoard.WHITE : GomokuBoard.BLACK);
        
        // 分析该方向的棋形
        PatternInfo pattern = analyzePattern(board, row, col, deltaRow, deltaCol, targetPiece);
        return calculatePatternScore(pattern.consecutiveCount, pattern.hasOpenEnd, forCurrentPlayer);
    }
    
    /**
     * 计算邻近棋子数量
     */
    private int countNeighbors(GomokuZeroAI.BoardState state, int row, int col) {
        int count = 0;
        char[][] board = state.getBoard();
        
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                
                int newRow = row + dr;
                int newCol = col + dc;
                
                if (newRow >= 0 && newRow < GomokuBoard.BOARD_SIZE &&
                    newCol >= 0 && newCol < GomokuBoard.BOARD_SIZE &&
                    board[newRow][newCol] != ' ') {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * 计算棋形分数
     */
    private double calculatePatternScore(int consecutiveCount, boolean openEnds, boolean myPiece) {
        double baseScore = 0;
        
        switch (consecutiveCount) {
            case 5: baseScore = FIVE_IN_A_ROW; break;
            case 4: baseScore = openEnds ? OPEN_FOUR : CLOSED_FOUR; break;
            case 3: baseScore = openEnds ? OPEN_THREE : CLOSED_THREE; break;
            case 2: baseScore = openEnds ? OPEN_TWO : CLOSED_TWO; break;
            case 1: baseScore = SINGLE; break;
            default: baseScore = 0;
        }
        
        // 对手的威胁需要特别关注
        if (!myPiece && consecutiveCount >= 3) {
            baseScore *= 1.2; // 防守权重稍微提高
        }
        
        return baseScore;
    }
    
    /**
     * 检查某个方向上的连续棋子
     */
    private PatternInfo analyzePattern(char[][] board, int row, int col, 
                                     int deltaRow, int deltaCol, char targetPiece) {
        int consecutiveCount = 0;
        int openEnds = 0;
        
        // 向正方向检查
        int positiveCount = 0;
        for (int i = 1; i < 6; i++) {
            int newRow = row + deltaRow * i;
            int newCol = col + deltaCol * i;
            
            if (newRow < 0 || newRow >= GomokuBoard.BOARD_SIZE ||
                newCol < 0 || newCol >= GomokuBoard.BOARD_SIZE) {
                break;
            }
            
            if (board[newRow][newCol] == targetPiece) {
                positiveCount++;
            } else {
                if (board[newRow][newCol] == ' ') {
                    openEnds++;
                }
                break;
            }
        }
        
        // 向负方向检查
        int negativeCount = 0;
        for (int i = 1; i < 6; i++) {
            int newRow = row - deltaRow * i;
            int newCol = col - deltaCol * i;
            
            if (newRow < 0 || newRow >= GomokuBoard.BOARD_SIZE ||
                newCol < 0 || newCol >= GomokuBoard.BOARD_SIZE) {
                break;
            }
            
            if (board[newRow][newCol] == targetPiece) {
                negativeCount++;
            } else {
                if (board[newRow][newCol] == ' ') {
                    openEnds++;
                }
                break;
            }
        }
        
        consecutiveCount = 1 + positiveCount + negativeCount; // 包含当前位置
        
        return new PatternInfo(consecutiveCount, openEnds >= 1);
    }
    
    /**
     * 棋形信息类
     */
    private static class PatternInfo {
        final int consecutiveCount;
        final boolean hasOpenEnd;
        
        PatternInfo(int consecutiveCount, boolean hasOpenEnd) {
            this.consecutiveCount = consecutiveCount;
            this.hasOpenEnd = hasOpenEnd;
        }
    }
    
    /**
     * 获取棋盘的整体评估值
     */
    public double evaluateBoard(char[][] board, boolean isBlackTurn) {
        double score = 0;
        
        char currentPlayer = isBlackTurn ? GomokuBoard.BLACK : GomokuBoard.WHITE;
        char opponent = isBlackTurn ? GomokuBoard.WHITE : GomokuBoard.BLACK;
        
        // 评估整个棋盘
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board[row][col] != ' ') {
                    boolean isMyPiece = board[row][col] == currentPlayer;
                    
                    // 评估四个方向
                    int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
                    for (int[] dir : directions) {
                        PatternInfo pattern = analyzePattern(board, row, col, 
                                                           dir[0], dir[1], board[row][col]);
                        double patternScore = calculatePatternScore(
                            pattern.consecutiveCount, pattern.hasOpenEnd, isMyPiece);
                        
                        score += isMyPiece ? patternScore : -patternScore * 0.9;
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * 检查是否有立即获胜的机会
     */
    public int[] findWinningMove(char[][] board, char player) {
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board[row][col] == ' ') {
                    // 临时放置棋子
                    board[row][col] = player;
                    
                    // 检查是否获胜
                    if (checkWin(board, row, col, player)) {
                        board[row][col] = ' '; // 恢复
                        return new int[]{row, col};
                    }
                    
                    board[row][col] = ' '; // 恢复
                }
            }
        }
        return null;
    }
    
    /**
     * 检查指定位置是否能形成五连
     */
    private boolean checkWin(char[][] board, int row, int col, char player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int count = 1;
            
            // 正向计数
            for (int i = 1; i < 5; i++) {
                int newRow = row + dir[0] * i;
                int newCol = col + dir[1] * i;
                if (newRow >= 0 && newRow < GomokuBoard.BOARD_SIZE &&
                    newCol >= 0 && newCol < GomokuBoard.BOARD_SIZE &&
                    board[newRow][newCol] == player) {
                    count++;
                } else {
                    break;
                }
            }
            
            // 反向计数
            for (int i = 1; i < 5; i++) {
                int newRow = row - dir[0] * i;
                int newCol = col - dir[1] * i;
                if (newRow >= 0 && newRow < GomokuBoard.BOARD_SIZE &&
                    newCol >= 0 && newCol < GomokuBoard.BOARD_SIZE &&
                    board[newRow][newCol] == player) {
                    count++;
                } else {
                    break;
                }
            }
            
            if (count >= 5) return true;
        }
        
        return false;
    }
}
