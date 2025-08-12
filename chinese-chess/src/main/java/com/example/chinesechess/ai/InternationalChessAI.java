package com.example.chinesechess.ai;

import com.example.chinesechess.core.InternationalChessBoard;
import com.example.chinesechess.core.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 国际象棋AI类
 * 实现基本的AI对弈功能
 */
public class InternationalChessAI {

    private Random random = new Random();
    private int difficulty; // 1-3，对应简单、中等、困难
    private char aiColor; // 'W'白方，'B'黑方
    
    /**
     * 构造函数
     * @param difficulty AI难度级别（1-3）
     * @param aiColor AI执子颜色
     */
    public InternationalChessAI(int difficulty, char aiColor) {
        this.difficulty = Math.min(Math.max(difficulty, 1), 3); // 确保难度在1-3之间
        this.aiColor = aiColor;
    }
    
    /**
     * 计算AI的下一步移动
     * @param board 当前棋盘状态
     * @return 移动数组 [fromRow, fromCol, toRow, toCol]
     */
    public int[] calculateNextMove(InternationalChessBoard board) {
        // 如果不是AI的回合，返回null
        boolean isWhiteTurn = board.isWhiteTurn();
        if ((isWhiteTurn && aiColor != InternationalChessBoard.WHITE) || 
            (!isWhiteTurn && aiColor != InternationalChessBoard.BLACK)) {
            return null;
        }
        
        // 获取所有可能的移动
        List<int[]> possibleMoves = getAllPossibleMoves(board);
        if (possibleMoves.isEmpty()) {
            return null; // 没有可行的移动
        }
        
        // 根据难度选择不同的策略
        switch (difficulty) {
            case 1: // 简单：随机移动
                return randomMove(possibleMoves);
            case 2: // 中等：简单评估后移动
                return betterMove(board, possibleMoves);
            case 3: // 困难：使用极小化极大算法
                return bestMove(board, possibleMoves);
            default:
                return randomMove(possibleMoves);
        }
    }
    
    /**
     * 获取所有可能的移动
     */
    private List<int[]> getAllPossibleMoves(InternationalChessBoard board) {
        List<int[]> moves = new ArrayList<>();
        char currentColor = board.isWhiteTurn() ? InternationalChessBoard.WHITE : InternationalChessBoard.BLACK;
        
        // 遍历整个棋盘
        for (int fromRow = 0; fromRow < InternationalChessBoard.BOARD_SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < InternationalChessBoard.BOARD_SIZE; fromCol++) {
                String piece = board.getPiece(fromRow, fromCol);
                
                // 如果该位置有当前回合方的棋子
                if (piece != null && piece.charAt(0) == currentColor) {
                    // 检查所有可能的目标位置
                    for (int toRow = 0; toRow < InternationalChessBoard.BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < InternationalChessBoard.BOARD_SIZE; toCol++) {
                            // 如果移动合法，添加到列表中
                            if (board.isValidMove(fromRow, fromCol, toRow, toCol)) {
                                moves.add(new int[]{fromRow, fromCol, toRow, toCol});
                            }
                        }
                    }
                }
            }
        }
        
        return moves;
    }
    
    /**
     * 随机选择一个移动（简单难度）
     */
    private int[] randomMove(List<int[]> possibleMoves) {
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }
    
    /**
     * 选择一个相对较好的移动（中等难度）
     * 简单评估：吃子优先，特别是吃高价值的子
     */
    private int[] betterMove(InternationalChessBoard board, List<int[]> possibleMoves) {
        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();
        
        for (int[] move : possibleMoves) {
            int fromRow = move[0];
            int fromCol = move[1];
            int toRow = move[2];
            int toCol = move[3];
            
            // 计算移动的分数
            int score = evaluateMove(board, fromRow, fromCol, toRow, toCol);
            
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }
        
        // 从最佳移动中随机选择一个
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }
    
    /**
     * 使用极小化极大算法选择最佳移动（困难难度）
     */
    private int[] bestMove(InternationalChessBoard board, List<int[]> possibleMoves) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        
        for (int[] move : possibleMoves) {
            // 创建棋盘副本并执行移动
            InternationalChessBoard boardCopy = cloneBoard(board);
            boardCopy.movePiece(move[0], move[1], move[2], move[3]);
            
            // 计算该移动的分数
            int score = minimax(boardCopy, 2, false); // 深度为2
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * 极小化极大算法
     * @param board 棋盘状态
     * @param depth 搜索深度
     * @param isMaximizing 是否是最大化玩家
     * @return 评估分数
     */
    private int minimax(InternationalChessBoard board, int depth, boolean isMaximizing) {
        // 如果达到搜索深度或游戏结束，返回评估分数
        if (depth == 0 || board.getGameState() != GameState.PLAYING) {
            return evaluateBoard(board);
        }
        
        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            List<int[]> possibleMoves = getAllPossibleMoves(board);
            
            for (int[] move : possibleMoves) {
                InternationalChessBoard boardCopy = cloneBoard(board);
                boardCopy.movePiece(move[0], move[1], move[2], move[3]);
                
                int score = minimax(boardCopy, depth - 1, false);
                maxScore = Math.max(maxScore, score);
            }
            
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            List<int[]> possibleMoves = getAllPossibleMoves(board);
            
            for (int[] move : possibleMoves) {
                InternationalChessBoard boardCopy = cloneBoard(board);
                boardCopy.movePiece(move[0], move[1], move[2], move[3]);
                
                int score = minimax(boardCopy, depth - 1, true);
                minScore = Math.min(minScore, score);
            }
            
            return minScore;
        }
    }
    
    /**
     * 评估移动的价值
     * 主要考虑是否能吃子，以及吃的子的价值
     */
    private int evaluateMove(InternationalChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        String targetPiece = board.getPiece(toRow, toCol);
        
        // 如果目标位置有对方的棋子，计算吃子的价值
        if (targetPiece != null) {
            return getPieceValue(targetPiece.charAt(1));
        }
        
        // 如果是中心位置，给予一定的奖励
        if ((toRow == 3 || toRow == 4) && (toCol == 3 || toCol == 4)) {
            return 1;
        }
        
        return 0;
    }
    
    /**
     * 评估棋盘状态
     * 计算双方棋子的总价值差异
     */
    private int evaluateBoard(InternationalChessBoard board) {
        int score = 0;
        
        for (int row = 0; row < InternationalChessBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < InternationalChessBoard.BOARD_SIZE; col++) {
                String piece = board.getPiece(row, col);
                if (piece != null) {
                    int pieceValue = getPieceValue(piece.charAt(1));
                    if (piece.charAt(0) == aiColor) {
                        score += pieceValue;
                    } else {
                        score -= pieceValue;
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * 获取棋子的价值
     */
    private int getPieceValue(char pieceType) {
        switch (pieceType) {
            case InternationalChessBoard.PAWN:
                return 1;
            case InternationalChessBoard.KNIGHT:
            case InternationalChessBoard.BISHOP:
                return 3;
            case InternationalChessBoard.ROOK:
                return 5;
            case InternationalChessBoard.QUEEN:
                return 9;
            case InternationalChessBoard.KING:
                return 100; // 王的价值非常高
            default:
                return 0;
        }
    }
    
    /**
     * 克隆棋盘
     */
    private InternationalChessBoard cloneBoard(InternationalChessBoard original) {
        InternationalChessBoard clone = new InternationalChessBoard();
        
        // 复制棋盘状态
        for (int row = 0; row < InternationalChessBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < InternationalChessBoard.BOARD_SIZE; col++) {
                clone.setPiece(row, col, original.getPiece(row, col));
            }
        }
        
        // TODO: 复制其他状态（如当前回合、游戏状态等）
        
        return clone;
    }
}