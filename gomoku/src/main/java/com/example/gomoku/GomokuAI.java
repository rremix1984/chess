package com.example.gomoku.ui;

import com.example.gomoku.core.GomokuBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 五子棋AI类
 * 实现基本的AI逻辑
 */
public class GomokuAI {

    protected String difficulty;
    protected Random random;
    
    /**
     * 构造函数
     */
    public GomokuAI(String difficulty) {
        this.difficulty = difficulty;
        this.random = new Random();
    }
    
    /**
     * 获取AI的下一步走法
     * @param board 当前棋盘状态
     * @return 走法坐标 [row, col]
     */
    public int[] getNextMove(GomokuBoard board) {
        // 获取所有可能的走法
        List<int[]> possibleMoves = getPossibleMoves(board);
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        // 根据难度选择不同的策略
        switch (difficulty) {
            case "简单":
                return getRandomMove(possibleMoves);
            case "普通":
                return getWeightedRandomMove(board, possibleMoves);
            case "困难":
            case "专家":
            case "大师":
                return getBestMove(board, possibleMoves);
            default:
                return getRandomMove(possibleMoves);
        }
    }
    
    /**
     * 获取所有可能的走法
     */
    protected List<int[]> getPossibleMoves(GomokuBoard board) {
        List<int[]> moves = new ArrayList<>();
        
        // 只考虑已有棋子周围的空位
        boolean[][] considered = new boolean[GomokuBoard.BOARD_SIZE][GomokuBoard.BOARD_SIZE];
        
        // 遍历棋盘找出所有已有棋子
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) != ' ') {
                    // 检查周围8个方向的空位
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            
                            int newRow = row + dr;
                            int newCol = col + dc;
                            
                            // 检查位置是否有效且未被考虑过
                            if (newRow >= 0 && newRow < GomokuBoard.BOARD_SIZE && 
                                newCol >= 0 && newCol < GomokuBoard.BOARD_SIZE && 
                                board.getPiece(newRow, newCol) == ' ' && 
                                !considered[newRow][newCol]) {
                                
                                moves.add(new int[]{newRow, newCol});
                                considered[newRow][newCol] = true;
                            }
                        }
                    }
                }
            }
        }
        
        // 如果没有找到任何可能的走法（棋盘为空），则选择天元位置
        if (moves.isEmpty()) {
            moves.add(new int[]{GomokuBoard.BOARD_SIZE / 2, GomokuBoard.BOARD_SIZE / 2});
        }
        
        return moves;
    }
    
    /**
     * 随机选择一个走法
     */
    protected int[] getRandomMove(List<int[]> possibleMoves) {
        int index = random.nextInt(possibleMoves.size());
        return possibleMoves.get(index);
    }
    
    /**
     * 根据权重随机选择一个走法
     */
    protected int[] getWeightedRandomMove(GomokuBoard board, List<int[]> possibleMoves) {
        // 计算每个走法的权重
        int[] weights = new int[possibleMoves.size()];
        int totalWeight = 0;
        
        for (int i = 0; i < possibleMoves.size(); i++) {
            int[] move = possibleMoves.get(i);
            int weight = evaluateMove(board, move[0], move[1]);
            weights[i] = weight;
            totalWeight += weight;
        }
        
        // 根据权重随机选择
        int randomValue = random.nextInt(totalWeight + 1);
        int cumulativeWeight = 0;
        
        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue <= cumulativeWeight) {
                return possibleMoves.get(i);
            }
        }
        
        // 默认返回第一个走法
        return possibleMoves.get(0);
    }
    
    /**
     * 选择最佳走法
     */
    protected int[] getBestMove(GomokuBoard board, List<int[]> possibleMoves) {
        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (int[] move : possibleMoves) {
            int score = evaluateMove(board, move[0], move[1]);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * 评估走法的分数
     */
    protected int evaluateMove(GomokuBoard board, int row, int col) {
        // 创建一个临时棋盘进行模拟
        char[][] tempBoard = new char[GomokuBoard.BOARD_SIZE][GomokuBoard.BOARD_SIZE];
        for (int r = 0; r < GomokuBoard.BOARD_SIZE; r++) {
            for (int c = 0; c < GomokuBoard.BOARD_SIZE; c++) {
                tempBoard[r][c] = board.getPiece(r, c);
            }
        }
        
        // 模拟AI落子
        char aiPiece = board.isBlackTurn() ? GomokuBoard.BLACK : GomokuBoard.WHITE;
        tempBoard[row][col] = aiPiece;
        
        // 评估四个方向的棋形
        int score = 0;
        score += evaluateDirection(tempBoard, row, col, 1, 0, aiPiece); // 水平方向
        score += evaluateDirection(tempBoard, row, col, 0, 1, aiPiece); // 垂直方向
        score += evaluateDirection(tempBoard, row, col, 1, 1, aiPiece); // 左上到右下
        score += evaluateDirection(tempBoard, row, col, 1, -1, aiPiece); // 右上到左下
        
        // 模拟对手落子，评估防守价值
        char opponentPiece = aiPiece == GomokuBoard.BLACK ? GomokuBoard.WHITE : GomokuBoard.BLACK;
        tempBoard[row][col] = opponentPiece;
        
        int defenseScore = 0;
        defenseScore += evaluateDirection(tempBoard, row, col, 1, 0, opponentPiece); // 水平方向
        defenseScore += evaluateDirection(tempBoard, row, col, 0, 1, opponentPiece); // 垂直方向
        defenseScore += evaluateDirection(tempBoard, row, col, 1, 1, opponentPiece); // 左上到右下
        defenseScore += evaluateDirection(tempBoard, row, col, 1, -1, opponentPiece); // 右上到左下
        
        // 进攻比防守更重要，但不能完全忽视防守
        score = score * 2 + defenseScore;
        
        // 根据难度调整随机性
        if (difficulty.equals("困难")) {
            score += random.nextInt(10); // 添加少量随机性
        } else if (difficulty.equals("专家")) {
            score += random.nextInt(5); // 添加极少量随机性
        }
        
        return score;
    }
    
    /**
     * 评估某个方向的棋形
     */
    protected int evaluateDirection(char[][] board, int row, int col, int rowDelta, int colDelta, char piece) {
        // 计算连续棋子数量
        int count = 1; // 当前位置已有一个棋子
        int openEnds = 0; // 开放端数量
        
        // 向一个方向检查
        int r = row + rowDelta;
        int c = col + colDelta;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board[r][c] == piece) {
            count++;
            r += rowDelta;
            c += colDelta;
        }
        
        // 检查这个方向是否是开放端
        if (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board[r][c] == ' ') {
            openEnds++;
        }
        
        // 向相反方向检查
        r = row - rowDelta;
        c = col - colDelta;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board[r][c] == piece) {
            count++;
            r -= rowDelta;
            c -= colDelta;
        }
        
        // 检查这个方向是否是开放端
        if (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board[r][c] == ' ') {
            openEnds++;
        }
        
        // 根据连续棋子数量和开放端数量评分
        if (count >= 5) return 100000; // 五连珠，必胜
        if (count == 4 && openEnds >= 1) return 10000; // 活四或冲四
        if (count == 3 && openEnds == 2) return 1000; // 活三
        if (count == 3 && openEnds == 1) return 100; // 冲三
        if (count == 2 && openEnds == 2) return 10; // 活二
        
        return count; // 基础分数
    }
    
    /**
     * 获取AI的思考过程（用于大模型AI）
     */
    public String getThinking() {
        return null; // 基础AI没有思考过程
    }
}