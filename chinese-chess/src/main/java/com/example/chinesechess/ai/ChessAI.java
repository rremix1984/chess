package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;
import java.util.*;

/**
 * 象棋AI引擎
 * 实现基于评估函数和极小极大算法的AI对弈
 */
public class ChessAI {
    
    private final PieceColor aiColor;
    private final int maxDepth;
    
    // 棋子价值表
    private static final Map<Class<? extends Piece>, Integer> PIECE_VALUES = new HashMap<>();
    static {
        PIECE_VALUES.put(General.class, 10000);   // 将/帅
        PIECE_VALUES.put(Advisor.class, 200);     // 士
        PIECE_VALUES.put(Elephant.class, 200);    // 象
        PIECE_VALUES.put(Horse.class, 400);       // 马
        PIECE_VALUES.put(Chariot.class, 900);     // 车
        PIECE_VALUES.put(Cannon.class, 450);      // 炮
        PIECE_VALUES.put(Soldier.class, 100);     // 兵/卒
    }
    
    public ChessAI(PieceColor aiColor, int difficulty) {
        this.aiColor = aiColor;
        this.maxDepth = Math.max(1, Math.min(difficulty, 4)); // 限制搜索深度1-4
    }
    
    /**
     * 获取AI的颜色
     */
    public PieceColor getColor() {
        return aiColor;
    }
    
    /**
     * 获取AI的最佳移动
     */
    public Move getBestMove(Board board) {
        List<Move> possibleMoves = getAllPossibleMoves(board, aiColor);
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Move move : possibleMoves) {
            // 模拟移动
            Board tempBoard = copyBoard(board);
            tempBoard.movePiece(move.getStart(), move.getEnd());
            
            // 使用极小极大算法评估
            int score = minimax(tempBoard, maxDepth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * 极小极大算法实现（带Alpha-Beta剪枝）
     */
    private int minimax(Board board, int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0) {
            return evaluateBoard(board);
        }
        
        PieceColor currentColor = isMaximizing ? aiColor : getOpponentColor(aiColor);
        List<Move> moves = getAllPossibleMoves(board, currentColor);
        
        if (moves.isEmpty()) {
            // 无法移动，游戏结束
            return isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        
        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board tempBoard = copyBoard(board);
                tempBoard.movePiece(move.getStart(), move.getEnd());
                
                int eval = minimax(tempBoard, depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                if (beta <= alpha) {
                    break; // Alpha-Beta剪枝
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board tempBoard = copyBoard(board);
                tempBoard.movePiece(move.getStart(), move.getEnd());
                
                int eval = minimax(tempBoard, depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if (beta <= alpha) {
                    break; // Alpha-Beta剪枝
                }
            }
            return minEval;
        }
    }
    
    /**
     * 评估棋盘局面
     */
    private int evaluateBoard(Board board) {
        int score = 0;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    int pieceValue = PIECE_VALUES.getOrDefault(piece.getClass(), 0);
                    
                    // 位置加分
                    pieceValue += getPositionBonus(piece, row, col);
                    
                    if (piece.getColor() == aiColor) {
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
     * 获取棋子位置加分
     */
    private int getPositionBonus(Piece piece, int row, int col) {
        // 简单的位置评估
        if (piece instanceof Soldier) {
            // 兵越过河界加分
            if (piece.getColor() == PieceColor.RED && row < 5) {
                return 50;
            } else if (piece.getColor() == PieceColor.BLACK && row > 4) {
                return 50;
            }
        } else if (piece instanceof Horse || piece instanceof Cannon) {
            // 马和炮在中心位置加分
            if (col >= 2 && col <= 6 && row >= 2 && row <= 7) {
                return 30;
            }
        }
        return 0;
    }
    
    /**
     * 获取所有可能的移动
     */
    private List<Move> getAllPossibleMoves(Board board, PieceColor color) {
        List<Move> moves = new ArrayList<>();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    Position start = new Position(row, col);
                    
                    // 检查所有可能的目标位置
                    for (int targetRow = 0; targetRow < 10; targetRow++) {
                        for (int targetCol = 0; targetCol < 9; targetCol++) {
                            Position end = new Position(targetRow, targetCol);
                            if (piece.isValidMove(board, start, end)) {
                                moves.add(new Move(start, end));
                            }
                        }
                    }
                }
            }
        }
        
        return moves;
    }
    
    /**
     * 复制棋盘
     */
    private Board copyBoard(Board original) {
        Board copy = new Board();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = original.getPiece(row, col);
                if (piece != null) {
                    copy.setPiece(row, col, clonePiece(piece));
                }
            }
        }
        return copy;
    }
    
    /**
     * 克隆棋子
     */
    private Piece clonePiece(Piece piece) {
        if (piece instanceof General) return new General(piece.getColor());
        if (piece instanceof Advisor) return new Advisor(piece.getColor());
        if (piece instanceof Elephant) return new Elephant(piece.getColor());
        if (piece instanceof Horse) return new Horse(piece.getColor());
        if (piece instanceof Chariot) return new Chariot(piece.getColor());
        if (piece instanceof Cannon) return new Cannon(piece.getColor());
        if (piece instanceof Soldier) return new Soldier(piece.getColor());
        return null;
    }
    
    /**
     * 获取对手颜色
     */
    private PieceColor getOpponentColor(PieceColor color) {
        return color == PieceColor.RED ? PieceColor.BLACK : PieceColor.RED;
    }
}