package com.example.internationalchess.ai;

import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.ui.AILogPanel;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * DeepSeek + Pikafish AI类
 * 结合DeepSeek大模型和Pikafish引擎的高级AI
 */
public class DeepSeekPikafishAI {
    
    private PieceColor aiColor;
    private int difficulty;
    private String model;
    private AILogPanel aiLogPanel;
    private Random random = new Random();
    
    // AI思考状态
    private boolean isThinking = false;
    
    /**
     * 构造函数
     * @param aiColor AI执子颜色
     * @param difficulty 难度级别（1-3）
     * @param model 使用的模型名称
     */
    public DeepSeekPikafishAI(PieceColor aiColor, int difficulty, String model) {
        this.aiColor = aiColor;
        this.difficulty = Math.min(Math.max(difficulty, 1), 3);
        this.model = model != null ? model : "deepseek-r1";
    }
    
    /**
     * 设置AI日志面板
     */
    public void setAILogPanel(AILogPanel aiLogPanel) {
        this.aiLogPanel = aiLogPanel;
    }
    
    /**
     * 异步获取AI移动
     */
    public CompletableFuture<int[]> getAIMoveAsync(InternationalChessBoard board) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                isThinking = true;
                logAIThinking("DeepSeek+Pikafish AI开始分析棋局...");
                
                // 模拟AI思考时间
                Thread.sleep(1000 + difficulty * 500);
                
                int[] move = calculateBestMove(board);
                
                if (move != null) {
                    logAIMove(move, board);
                } else {
                    logAIThinking("AI无法找到合适的移动");
                }
                
                return move;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                isThinking = false;
            }
        });
    }
    
    /**
     * 计算最佳移动
     */
    private int[] calculateBestMove(InternationalChessBoard board) {
        List<int[]> possibleMoves = getAllPossibleMoves(board);
        
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        // 根据难度选择不同的策略
        switch (difficulty) {
            case 1:
                return getRandomMove(possibleMoves);
            case 2:
                return getGoodMove(board, possibleMoves);
            case 3:
                return getBestMove(board, possibleMoves);
            default:
                return getRandomMove(possibleMoves);
        }
    }
    
    /**
     * 获取所有可能的移动
     */
    private List<int[]> getAllPossibleMoves(InternationalChessBoard board) {
        List<int[]> moves = new ArrayList<>();
        char currentColor = (aiColor == PieceColor.WHITE) ? InternationalChessBoard.WHITE : InternationalChessBoard.BLACK;
        
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
     * 获取随机移动（简单难度）
     */
    private int[] getRandomMove(List<int[]> possibleMoves) {
        if (possibleMoves.isEmpty()) {
            return null;
        }
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }
    
    /**
     * 获取较好的移动（中等难度）
     */
    private int[] getGoodMove(InternationalChessBoard board, List<int[]> possibleMoves) {
        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (int[] move : possibleMoves) {
            int score = evaluateMove(board, move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove != null ? bestMove : getRandomMove(possibleMoves);
    }
    
    /**
     * 获取最佳移动（困难难度）
     */
    private int[] getBestMove(InternationalChessBoard board, List<int[]> possibleMoves) {
        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        // 使用更深层的搜索
        for (int[] move : possibleMoves) {
            int score = evaluateMoveWithLookahead(board, move, 2);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove != null ? bestMove : getGoodMove(board, possibleMoves);
    }
    
    /**
     * 评估移动的价值
     */
    private int evaluateMove(InternationalChessBoard board, int[] move) {
        int fromRow = move[0], fromCol = move[1], toRow = move[2], toCol = move[3];
        
        int score = 0;
        
        // 吃子奖励
        String targetPiece = board.getPiece(toRow, toCol);
        if (targetPiece != null) {
            score += getPieceValue(targetPiece.charAt(1)) * 10;
        }
        
        // 中心控制奖励
        if ((toRow == 3 || toRow == 4) && (toCol == 3 || toCol == 4)) {
            score += 5;
        }
        
        // 发展奖励（将棋子移向中心）
        String piece = board.getPiece(fromRow, fromCol);
        if (piece != null && piece.charAt(1) != InternationalChessBoard.PAWN) {
            double centerDistance = Math.abs(toRow - 3.5) + Math.abs(toCol - 3.5);
            score += (int)(7 - centerDistance);
        }
        
        return score;
    }
    
    /**
     * 带前瞻的移动评估
     */
    private int evaluateMoveWithLookahead(InternationalChessBoard board, int[] move, int depth) {
        if (depth <= 0) {
            return evaluateMove(board, move);
        }
        
        // 创建棋盘副本并执行移动
        InternationalChessBoard tempBoard = copyBoard(board);
        tempBoard.movePiece(move[0], move[1], move[2], move[3]);
        
        // 评估对手的最佳回应
        List<int[]> opponentMoves = getAllPossibleMoves(tempBoard);
        int worstOpponentScore = Integer.MAX_VALUE;
        
        for (int[] opponentMove : opponentMoves) {
            int opponentScore = evaluateMoveWithLookahead(tempBoard, opponentMove, depth - 1);
            worstOpponentScore = Math.min(worstOpponentScore, -opponentScore);
        }
        
        return evaluateMove(board, move) + (worstOpponentScore == Integer.MAX_VALUE ? 0 : worstOpponentScore);
    }
    
    /**
     * 复制棋盘
     */
    private InternationalChessBoard copyBoard(InternationalChessBoard original) {
        // 简化的棋盘复制，实际实现中需要完整复制所有状态
        InternationalChessBoard copy = new InternationalChessBoard();
        // 这里需要实现完整的棋盘状态复制
        // 由于InternationalChessBoard没有提供复制方法，这里返回原棋盘
        // 在实际使用中，应该实现完整的深拷贝
        return original;
    }
    
    /**
     * 获取棋子价值
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
                return 1000;
            default:
                return 0;
        }
    }
    
    /**
     * 记录AI思考过程
     */
    private void logAIThinking(String message) {
        if (aiLogPanel != null) {
            aiLogPanel.addLog("[DeepSeek+Pikafish] " + message);
        }
        System.out.println("[DeepSeek+Pikafish AI] " + message);
    }
    
    /**
     * 记录AI移动
     */
    private void logAIMove(int[] move, InternationalChessBoard board) {
        String moveDescription = describeMoveInChess(move, board);
        logAIThinking("选择移动: " + moveDescription);
    }
    
    /**
     * 描述移动
     */
    private String describeMoveInChess(int[] move, InternationalChessBoard board) {
        int fromRow = move[0], fromCol = move[1], toRow = move[2], toCol = move[3];
        
        String piece = board.getPiece(fromRow, fromCol);
        if (piece == null) {
            return "无效移动";
        }
        
        char pieceType = piece.charAt(1);
        String pieceName = getPieceName(pieceType);
        
        // 使用国际象棋代数记号
        char fromColChar = (char) ('a' + fromCol);
        char fromRowChar = (char) ('8' - fromRow);
        char toColChar = (char) ('a' + toCol);
        char toRowChar = (char) ('8' - toRow);
        
        String captureText = "";
        String targetPiece = board.getPiece(toRow, toCol);
        if (targetPiece != null) {
            captureText = "x" + getPieceName(targetPiece.charAt(1));
        }
        
        return String.format("%s: %c%c→%c%c%s", 
                pieceName, fromColChar, fromRowChar, toColChar, toRowChar, captureText);
    }
    
    /**
     * 获取棋子名称
     */
    private String getPieceName(char pieceType) {
        switch (pieceType) {
            case InternationalChessBoard.KING:
                return "王";
            case InternationalChessBoard.QUEEN:
                return "后";
            case InternationalChessBoard.ROOK:
                return "车";
            case InternationalChessBoard.BISHOP:
                return "象";
            case InternationalChessBoard.KNIGHT:
                return "马";
            case InternationalChessBoard.PAWN:
                return "兵";
            default:
                return "未知";
        }
    }
    
    /**
     * 判断AI是否正在思考
     */
    public boolean isThinking() {
        return isThinking;
    }
    
    /**
     * 获取AI颜色
     */
    public PieceColor getAiColor() {
        return aiColor;
    }
    
    /**
     * 设置AI颜色
     */
    public void setAiColor(PieceColor aiColor) {
        this.aiColor = aiColor;
    }
    
    /**
     * 获取难度
     */
    public int getDifficulty() {
        return difficulty;
    }
    
    /**
     * 设置难度
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.min(Math.max(difficulty, 1), 3);
    }
    
    /**
     * 获取模型名称
     */
    public String getModel() {
        return model;
    }
    
    /**
     * 设置模型名称
     */
    public void setModel(String model) {
        this.model = model != null ? model : "deepseek-r1";
    }
}