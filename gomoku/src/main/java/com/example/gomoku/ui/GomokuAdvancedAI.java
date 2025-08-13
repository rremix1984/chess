package com.example.gomoku.ui;

import com.example.gomoku.core.GomokuBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 五子棋高级AI类
 * 使用Minimax算法配合Alpha-Beta剪枝实现强力AI
 * 参考了开源项目的最佳实践，提供更强的对弈能力
 */
public class GomokuAdvancedAI {
    
    private static final int MAX_DEPTH = 6; // 最大搜索深度
    private static final int INFINITY = 1000000;
    private static final int WIN_SCORE = 100000;
    
    // 棋形评分表
    private static final int FIVE = 100000;     // 五连
    private static final int OPEN_FOUR = 10000; // 活四
    private static final int FOUR = 1000;       // 冲四
    private static final int OPEN_THREE = 1000; // 活三
    private static final int THREE = 100;       // 眠三
    private static final int OPEN_TWO = 100;    // 活二
    private static final int TWO = 10;          // 眠二
    
    private String thinking = "";
    private String difficulty;
    
    public GomokuAdvancedAI(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public int[] getNextMove(GomokuBoard board) {
        thinking = "🤖 高级AI正在思考...\n";
        
        // 根据难度设置搜索深度
        int searchDepth = getSearchDepth();
        thinking += "搜索深度: " + searchDepth + "层\n";
        
        long startTime = System.currentTimeMillis();
        
        // 使用Minimax算法配合Alpha-Beta剪枝
        MinimaxResult result = minimax(board, searchDepth, -INFINITY, INFINITY, true);
        
        long endTime = System.currentTimeMillis();
        thinking += "思考时间: " + (endTime - startTime) + "ms\n";
        thinking += "评估分数: " + result.score + "\n";
        
        if (result.bestMove != null) {
            char moveChar = (char)('A' + result.bestMove[1]);
            thinking += "选择走法: " + moveChar + (result.bestMove[0] + 1) + "\n";
            thinking += "走法分析: " + analyzeMove(board, result.bestMove[0], result.bestMove[1]) + "\n";
        }
        
        return result.bestMove;
    }
    
    /**
     * 根据难度获取搜索深度
     */
    private int getSearchDepth() {
        switch (difficulty) {
            case "简单": return 2;
            case "普通": return 4;
            case "困难": return 6;
            case "专家": return 8;
            case "大师": return 10;
            default: return 4;
        }
    }
    
    /**
     * Minimax算法配合Alpha-Beta剪枝
     */
    private MinimaxResult minimax(GomokuBoard board, int depth, int alpha, int beta, boolean isMaximizing) {
        // 终止条件：达到最大深度或游戏结束
        if (depth == 0 || board.getGameState() != com.example.gomoku.core.GameState.PLAYING) {
            int score = evaluateBoard(board);
            return new MinimaxResult(score, null);
        }
        
        List<int[]> possibleMoves = getPossibleMoves(board);
        int[] bestMove = null;
        
        if (isMaximizing) {
            int maxScore = -INFINITY;
            
            for (int[] move : possibleMoves) {
                // 尝试这个走法
                if (board.placePiece(move[0], move[1])) {
                    MinimaxResult result = minimax(board, depth - 1, alpha, beta, false);
                    
                    // 撤销走法
                    undoMove(board, move[0], move[1]);
                    
                    if (result.score > maxScore) {
                        maxScore = result.score;
                        bestMove = move;
                    }
                    
                    alpha = Math.max(alpha, result.score);
                    
                    // Alpha-Beta剪枝
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            
            return new MinimaxResult(maxScore, bestMove);
        } else {
            int minScore = INFINITY;
            
            for (int[] move : possibleMoves) {
                // 尝试这个走法
                if (board.placePiece(move[0], move[1])) {
                    MinimaxResult result = minimax(board, depth - 1, alpha, beta, true);
                    
                    // 撤销走法
                    undoMove(board, move[0], move[1]);
                    
                    if (result.score < minScore) {
                        minScore = result.score;
                        bestMove = move;
                    }
                    
                    beta = Math.min(beta, result.score);
                    
                    // Alpha-Beta剪枝
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            
            return new MinimaxResult(minScore, bestMove);
        }
    }
    
    /**
     * 获取所有可能的走法
     */
    private List<int[]> getPossibleMoves(GomokuBoard board) {
        List<int[]> moves = new ArrayList<>();
        
        // 如果棋盘为空，返回中心位置
        if (isEmpty(board)) {
            moves.add(new int[]{GomokuBoard.BOARD_SIZE / 2, GomokuBoard.BOARD_SIZE / 2});
            return moves;
        }
        
        // 获取所有空位置，但只考虑有棋子邻近的位置
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) == ' ' && hasNeighbor(board, row, col)) {
                    moves.add(new int[]{row, col});
                }
            }
        }
        
        return moves;
    }
    
    /**
     * 检查棋盘是否为空
     */
    private boolean isEmpty(GomokuBoard board) {
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) != ' ') {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 检查位置是否有邻近的棋子
     */
    private boolean hasNeighbor(GomokuBoard board, int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int newRow = row + dr;
                int newCol = col + dc;
                if (newRow >= 0 && newRow < GomokuBoard.BOARD_SIZE && 
                    newCol >= 0 && newCol < GomokuBoard.BOARD_SIZE && 
                    board.getPiece(newRow, newCol) != ' ') {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 评估单个走法的分数
     */
    private int evaluateMove(GomokuBoard board, int row, int col) {
        // 临时放置棋子进行评估
        char currentPiece = board.isBlackTurn() ? GomokuBoard.BLACK : GomokuBoard.WHITE;
        
        // 模拟放置棋子
        if (board.placePiece(row, col)) {
            int score = evaluatePosition(board, row, col, currentPiece);
            // 撤销走法
            undoMove(board, row, col);
            return score;
        }
        
        return 0;
    }
    
    /**
     * 获取排序后的走法列表（启发式搜索优化）
     */
    private List<int[]> getOrderedMoves(GomokuBoard board) {
        List<int[]> moves = getPossibleMoves(board);
        
        // 按评估分数排序，优先搜索好的走法
        moves.sort((a, b) -> {
            int scoreA = evaluateMove(board, a[0], a[1]);
            int scoreB = evaluateMove(board, b[0], b[1]);
            return Integer.compare(scoreB, scoreA); // 降序排列
        });
        
        // 限制搜索的走法数量，避免搜索过慢
        int maxMoves = Math.min(moves.size(), 20);
        return moves.subList(0, maxMoves);
    }
    
    /**
     * 撤销走法
     */
    private void undoMove(GomokuBoard board, int row, int col) {
        // 通过反射或其他方式撤销走法
        // 这里简化处理，实际应该有更好的实现
        try {
            java.lang.reflect.Field boardField = board.getClass().getDeclaredField("board");
            boardField.setAccessible(true);
            char[][] boardArray = (char[][]) boardField.get(board);
            boardArray[row][col] = ' ';
            
            // 切换回合
            java.lang.reflect.Field turnField = board.getClass().getDeclaredField("isBlackTurn");
            turnField.setAccessible(true);
            boolean currentTurn = turnField.getBoolean(board);
            turnField.setBoolean(board, !currentTurn);
        } catch (Exception e) {
            // 如果反射失败，忽略错误
        }
    }
    
    /**
     * 评估整个棋盘的分数
     */
    private int evaluateBoard(GomokuBoard board) {
        int score = 0;
        
        // 遍历棋盘上的每个位置
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                char piece = board.getPiece(row, col);
                if (piece != ' ') {
                    int pieceScore = evaluatePosition(board, row, col, piece);
                    
                    // AI的棋子得正分，对手的棋子得负分
                    char aiPiece = board.isBlackTurn() ? GomokuBoard.BLACK : GomokuBoard.WHITE;
                    if (piece == aiPiece) {
                        score += pieceScore;
                    } else {
                        score -= pieceScore;
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * 评估某个位置的棋子价值
     */
    private int evaluatePosition(GomokuBoard board, int row, int col, char piece) {
        int score = 0;
        
        // 评估四个方向
        score += evaluateDirectionAdvanced(board, row, col, 1, 0, piece);   // 水平
        score += evaluateDirectionAdvanced(board, row, col, 0, 1, piece);   // 垂直
        score += evaluateDirectionAdvanced(board, row, col, 1, 1, piece);   // 主对角线
        score += evaluateDirectionAdvanced(board, row, col, 1, -1, piece);  // 副对角线
        
        return score;
    }
    
    /**
     * 高级方向评估（识别各种棋形）
     */
    private int evaluateDirectionAdvanced(GomokuBoard board, int row, int col, int deltaRow, int deltaCol, char piece) {
        int count = 1; // 当前位置的棋子
        int openEnds = 0;
        int blocks = 0;
        
        // 向正方向搜索
        int r = row + deltaRow;
        int c = col + deltaCol;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
            if (board.getPiece(r, c) == piece) {
                count++;
            } else if (board.getPiece(r, c) == ' ') {
                openEnds++;
                break;
            } else {
                blocks++;
                break;
            }
            r += deltaRow;
            c += deltaCol;
        }
        
        // 向负方向搜索
        r = row - deltaRow;
        c = col - deltaCol;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
            if (board.getPiece(r, c) == piece) {
                count++;
            } else if (board.getPiece(r, c) == ' ') {
                openEnds++;
                break;
            } else {
                blocks++;
                break;
            }
            r -= deltaRow;
            c -= deltaCol;
        }
        
        // 根据连子数和开放端数评分
        return getPatternScore(count, openEnds, blocks);
    }
    
    /**
     * 根据棋形模式获取分数
     */
    private int getPatternScore(int count, int openEnds, int blocks) {
        if (count >= 5) {
            return FIVE; // 五连
        }
        
        if (count == 4) {
            if (openEnds == 2) {
                return OPEN_FOUR; // 活四
            } else if (openEnds == 1) {
                return FOUR; // 冲四
            }
        }
        
        if (count == 3) {
            if (openEnds == 2) {
                return OPEN_THREE; // 活三
            } else if (openEnds == 1) {
                return THREE; // 眠三
            }
        }
        
        if (count == 2) {
            if (openEnds == 2) {
                return OPEN_TWO; // 活二
            } else if (openEnds == 1) {
                return TWO; // 眠二
            }
        }
        
        return 0;
    }
    
    /**
     * 分析走法
     */
    private String analyzeMove(GomokuBoard board, int row, int col) {
        int score = evaluateMove(board, row, col);
        
        if (score >= WIN_SCORE) {
            return "必胜走法！";
        } else if (score >= OPEN_FOUR) {
            return "形成活四";
        } else if (score >= FOUR) {
            return "形成冲四";
        } else if (score >= OPEN_THREE) {
            return "形成活三";
        } else if (score >= THREE) {
            return "形成眠三";
        } else if (score >= OPEN_TWO) {
            return "形成活二";
        } else {
            return "常规走法";
        }
    }
    
    public String getThinking() {
        return thinking;
    }
    
    /**
     * Minimax算法结果类
     */
    private static class MinimaxResult {
        int score;
        int[] bestMove;
        
        MinimaxResult(int score, int[] bestMove) {
            this.score = score;
            this.bestMove = bestMove;
        }
    }
}