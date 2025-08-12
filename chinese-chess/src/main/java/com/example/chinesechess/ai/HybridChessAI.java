package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 混合象棋AI引擎
 * 结合增强传统AI的精确计算和大模型AI的战略思维
 */
public class HybridChessAI {
    
    private final PieceColor aiColor;
    private final int difficulty;
    private final String modelName;
    private final EnhancedChessAI enhancedAI;
    private final LLMChessAI llmAI;
    private final ExecutorService executorService;
    
    // 策略权重
    private final double llmWeight;
    private final double enhancedWeight;
    
    // 阶段判断阈值
    private static final int OPENING_PIECE_THRESHOLD = 28;  // 开局阶段棋子数量
    private static final int ENDGAME_PIECE_THRESHOLD = 16;  // 残局阶段棋子数量
    private static final int LLM_TIMEOUT_SECONDS = 30;      // 大模型超时时间
    
    public HybridChessAI(PieceColor aiColor, int difficulty, String modelName) {
        this.aiColor = aiColor;
        this.difficulty = Math.max(1, Math.min(5, difficulty));
        this.modelName = modelName;
        
        // 初始化增强AI
        this.enhancedAI = new EnhancedChessAI(aiColor, difficulty);
        
        // 初始化LLM AI
        this.llmAI = new LLMChessAI(aiColor, modelName, difficulty);
        
        // 初始化线程池
        this.executorService = Executors.newCachedThreadPool();
        
        // 根据难度调整策略权重
        double tempLlmWeight, tempEnhancedWeight;
        switch (this.difficulty) {
            case 1: // 简单 - 主要依赖传统AI
                tempLlmWeight = 0.3;
                tempEnhancedWeight = 0.7;
                break;
            case 2: // 普通 - 平衡使用
                tempLlmWeight = 0.5;
                tempEnhancedWeight = 0.5;
                break;
            case 3: // 困难 - 更多依赖LLM的战略思维
                tempLlmWeight = 0.7;
                tempEnhancedWeight = 0.3;
                break;
            case 4: // 专家 - 主要依赖LLM，传统AI作为验证
                tempLlmWeight = 0.8;
                tempEnhancedWeight = 0.2;
                break;
            case 5: // 大师 - 完全依赖LLM，传统AI仅作紧急备用
                tempLlmWeight = 0.9;
                tempEnhancedWeight = 0.1;
                break;
            default:
                tempLlmWeight = 0.5;
                tempEnhancedWeight = 0.5;
                break;
        }
        this.llmWeight = tempLlmWeight;
        this.enhancedWeight = tempEnhancedWeight;
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
        System.out.println("\n🤖 混合AI分析中...");
        long startTime = System.currentTimeMillis();
        
        GamePhase phase = determineGamePhase(board);
        System.out.println("📊 当前阶段: " + getPhaseDescription(phase));
        
        Move finalMove = null;
        
        switch (phase) {
            case OPENING:
                finalMove = getOpeningMove(board);
                break;
            case MIDDLE_GAME:
                finalMove = getMiddlegameMove(board);
                break;
            case ENDGAME:
                finalMove = getEndgameMove(board);
                break;
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("⏱️  总思考时间: " + (endTime - startTime) + "ms");
        System.out.println("🎯 混合AI最终决策: " + formatMove(finalMove));
        
        return finalMove;
    }
    
    /**
     * 开局阶段决策
     */
    private Move getOpeningMove(Board board) {
        System.out.println("📚 开局阶段 - 优先使用增强AI");
        
        // 开局阶段主要依赖增强AI的开局库和快速计算
        Move enhancedMove = enhancedAI.getBestMove(board);
        
        // 如果时间允许，也获取大模型的建议作为参考
        try {
            CompletableFuture<Move> strategicFuture = CompletableFuture
                .supplyAsync(() -> llmAI.getBestMove(board), executorService);
            
            Move strategicMove = strategicFuture.get(15, TimeUnit.SECONDS);
            
            if (strategicMove != null && isReasonableMove(board, strategicMove)) {
                System.out.println("💡 大模型建议: " + formatMove(strategicMove));
                
                // 如果大模型建议的走法在增强AI的候选列表中，优先考虑
                List<Move> candidates = getTopCandidates(board, 5);
                if (candidates.contains(strategicMove)) {
                    System.out.println("✅ 采用大模型建议（在候选列表中）");
                    return strategicMove;
                }
            }
        } catch (TimeoutException e) {
            System.out.println("⏰ 大模型超时，使用增强AI结果");
        } catch (Exception e) {
            System.out.println("❌ 大模型异常: " + e.getMessage());
        }
        
        return enhancedMove;
    }
    
    /**
     * 中局阶段决策
     */
    private Move getMiddlegameMove(Board board) {
        System.out.println("⚔️  中局阶段 - 混合决策");
        
        // 并行计算两种AI的建议
        CompletableFuture<Move> enhancedFuture = CompletableFuture
            .supplyAsync(() -> enhancedAI.getBestMove(board), executorService);
        
        CompletableFuture<Move> strategicFuture = CompletableFuture
            .supplyAsync(() -> llmAI.getBestMove(board), executorService);
        
        try {
            // 等待增强AI结果（通常很快）
            Move enhancedMove = enhancedFuture.get(10, TimeUnit.SECONDS);
            System.out.println("🧮 增强AI建议: " + formatMove(enhancedMove));
            
            try {
                // 等待大模型结果（有超时限制）
                Move strategicMove = strategicFuture.get(LLM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                System.out.println("🧠 大模型建议: " + formatMove(strategicMove));
                
                // 混合决策逻辑
                return combineDecisions(board, enhancedMove, strategicMove);
                
            } catch (TimeoutException e) {
                System.out.println("⏰ 大模型超时，使用增强AI结果");
                return enhancedMove;
            }
            
        } catch (Exception e) {
            System.out.println("❌ 增强AI异常，尝试大模型: " + e.getMessage());
            
            try {
                return strategicFuture.get(LLM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e2) {
                System.out.println("❌ 所有AI都失败，使用备用逻辑");
                return getEmergencyMove(board);
            }
        }
    }
    
    /**
     * 残局阶段决策
     */
    private Move getEndgameMove(Board board) {
        System.out.println("🏁 残局阶段 - 精确计算优先");
        
        // 残局阶段主要依赖增强AI的精确计算
        Move enhancedMove = enhancedAI.getBestMove(board);
        
        // 检查是否有明显的获胜走法
        if (isWinningMove(board, enhancedMove)) {
            System.out.println("🎉 发现获胜走法!");
            return enhancedMove;
        }
        
        // 如果没有明显获胜走法，考虑大模型的战略建议
        try {
            CompletableFuture<Move> strategicFuture = CompletableFuture
                .supplyAsync(() -> llmAI.getBestMove(board), executorService);
            
            Move strategicMove = strategicFuture.get(20, TimeUnit.SECONDS);
            
            if (strategicMove != null && isReasonableMove(board, strategicMove)) {
                System.out.println("💭 大模型残局建议: " + formatMove(strategicMove));
                
                // 在残局阶段，如果大模型建议合理，可以考虑采用
                int enhancedScore = evaluateMove(board, enhancedMove);
                int strategicScore = evaluateMove(board, strategicMove);
                
                if (Math.abs(strategicScore - enhancedScore) < 50) {
                    System.out.println("🤔 两种建议评分接近，采用大模型建议");
                    return strategicMove;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  大模型残局分析失败: " + e.getMessage());
        }
        
        return enhancedMove;
    }
    
    /**
     * 混合决策逻辑
     */
    private Move combineDecisions(Board board, Move enhancedMove, Move strategicMove) {
        if (enhancedMove == null) return strategicMove;
        if (strategicMove == null) return enhancedMove;
        
        // 如果两个AI建议相同，直接采用
        if (enhancedMove.equals(strategicMove)) {
            System.out.println("🎯 两个AI建议一致!");
            return enhancedMove;
        }
        
        // 评估两个走法的质量
        int enhancedScore = evaluateMove(board, enhancedMove);
        int strategicScore = evaluateMove(board, strategicMove);
        
        System.out.println("📊 增强AI评分: " + enhancedScore + ", 大模型评分: " + strategicScore);
        
        // 如果评分差距很大，选择高分的
        if (Math.abs(enhancedScore - strategicScore) > 100) {
            Move betterMove = enhancedScore > strategicScore ? enhancedMove : strategicMove;
            String aiType = enhancedScore > strategicScore ? "增强AI" : "大模型";
            System.out.println("📈 评分差距较大，采用" + aiType + "建议");
            return betterMove;
        }
        
        // 如果评分接近，检查是否有战术威胁
        if (hasTacticalThreat(board, strategicMove)) {
            System.out.println("⚡ 大模型建议包含战术威胁，优先采用");
            return strategicMove;
        }
        
        if (hasTacticalThreat(board, enhancedMove)) {
            System.out.println("⚡ 增强AI建议包含战术威胁，优先采用");
            return enhancedMove;
        }
        
        // 默认情况下，信任增强AI的计算
        System.out.println("🧮 默认采用增强AI的精确计算");
        return enhancedMove;
    }
    
    /**
     * 判断游戏阶段
     */
    private GamePhase determineGamePhase(Board board) {
        int pieceCount = countPieces(board);
        
        if (pieceCount >= OPENING_PIECE_THRESHOLD) {
            return GamePhase.OPENING;
        } else if (pieceCount <= ENDGAME_PIECE_THRESHOLD) {
            return GamePhase.ENDGAME;
        } else {
            return GamePhase.MIDDLE_GAME;
        }
    }
    
    /**
     * 计算棋盘上的棋子数量
     */
    private int countPieces(Board board) {
        int count = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                if (board.getPiece(row, col) != null) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * 获取候选走法
     */
    private List<Move> getTopCandidates(Board board, int count) {
        List<Move> allMoves = getAllPossibleMoves(board, aiColor);
        
        // 简单排序（可以使用更复杂的评估）
        allMoves.sort((m1, m2) -> {
            int score1 = evaluateMove(board, m1);
            int score2 = evaluateMove(board, m2);
            return Integer.compare(score2, score1);
        });
        
        return allMoves.subList(0, Math.min(count, allMoves.size()));
    }
    
    /**
     * 评估单个走法
     */
    private int evaluateMove(Board board, Move move) {
        if (move == null) return Integer.MIN_VALUE;
        
        Board tempBoard = copyBoard(board);
        tempBoard.movePiece(move.getStart(), move.getEnd());
        
        // 使用简化的评估函数
        return evaluateBoard(tempBoard);
    }
    
    /**
     * 简化的棋盘评估
     */
    private int evaluateBoard(Board board) {
        int score = 0;
        Map<Class<? extends Piece>, Integer> pieceValues = getPieceValues();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    int value = pieceValues.getOrDefault(piece.getClass(), 0);
                    if (piece.getColor() == aiColor) {
                        score += value;
                    } else {
                        score -= value;
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * 检查是否是合理的走法
     */
    private boolean isReasonableMove(Board board, Move move) {
        if (move == null) return false;
        
        Position start = move.getStart();
        Position end = move.getEnd();
        
        // 检查坐标范围
        if (!isValidPosition(start) || !isValidPosition(end)) {
            return false;
        }
        
        // 检查起始位置是否有己方棋子
        Piece piece = board.getPiece(start.getX(), start.getY());
        if (piece == null || piece.getColor() != aiColor) {
            return false;
        }
        
        // 检查移动规则
        return piece.isValidMove(board, start, end) && 
               board.isMoveSafe(start, end, aiColor);
    }
    
    /**
     * 检查是否是获胜走法
     */
    private boolean isWinningMove(Board board, Move move) {
        if (move == null) return false;
        
        Board tempBoard = copyBoard(board);
        tempBoard.movePiece(move.getStart(), move.getEnd());
        
        // 检查是否将死对方
        return isCheckmate(tempBoard, getOpponentColor(aiColor));
    }
    
    /**
     * 检查是否有战术威胁
     */
    private boolean hasTacticalThreat(Board board, Move move) {
        if (move == null) return false;
        
        Board tempBoard = copyBoard(board);
        tempBoard.movePiece(move.getStart(), move.getEnd());
        
        // 检查是否将军
        if (isInCheck(tempBoard, getOpponentColor(aiColor))) {
            return true;
        }
        
        // 检查是否吃子
        Piece targetPiece = board.getPiece(move.getEnd().getX(), move.getEnd().getY());
        if (targetPiece != null && targetPiece.getColor() != aiColor) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 紧急走法（当所有AI都失败时）
     */
    private Move getEmergencyMove(Board board) {
        List<Move> moves = getAllPossibleMoves(board, aiColor);
        if (!moves.isEmpty()) {
            return moves.get(0); // 返回第一个有效走法
        }
        return null;
    }
    
    // 辅助方法
    private List<Move> getAllPossibleMoves(Board board, PieceColor color) {
        List<Move> moves = new ArrayList<>();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    Position start = new Position(row, col);
                    
                    for (int targetRow = 0; targetRow < 10; targetRow++) {
                        for (int targetCol = 0; targetCol < 9; targetCol++) {
                            Position end = new Position(targetRow, targetCol);
                            if (piece.isValidMove(board, start, end) && 
                                board.isMoveSafe(start, end, color)) {
                                moves.add(new Move(start, end));
                            }
                        }
                    }
                }
            }
        }
        
        return moves;
    }
    
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
    
    private boolean isValidPosition(Position pos) {
        return pos.getX() >= 0 && pos.getX() < 10 && 
               pos.getY() >= 0 && pos.getY() < 9;
    }
    
    private boolean isInCheck(Board board, PieceColor color) {
        // 找到将/帅的位置
        Position kingPos = null;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece instanceof General && piece.getColor() == color) {
                    kingPos = new Position(row, col);
                    break;
                }
            }
        }
        
        if (kingPos == null) return false;
        
        // 检查是否被攻击
        return countAttackers(board, kingPos, getOpponentColor(color)) > 0;
    }
    
    private boolean isCheckmate(Board board, PieceColor color) {
        if (!isInCheck(board, color)) return false;
        
        // 检查是否有任何走法可以解除将军
        List<Move> moves = getAllPossibleMoves(board, color);
        for (Move move : moves) {
            Board tempBoard = copyBoard(board);
            tempBoard.movePiece(move.getStart(), move.getEnd());
            if (!isInCheck(tempBoard, color)) {
                return false; // 找到解除将军的走法
            }
        }
        
        return true; // 无法解除将军，被将死
    }
    
    private int countAttackers(Board board, Position target, PieceColor color) {
        int count = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    Position from = new Position(row, col);
                    if (piece.isValidMove(board, from, target)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    private PieceColor getOpponentColor(PieceColor color) {
        return color == PieceColor.RED ? PieceColor.BLACK : PieceColor.RED;
    }
    
    private Map<Class<? extends Piece>, Integer> getPieceValues() {
        Map<Class<? extends Piece>, Integer> values = new HashMap<>();
        values.put(General.class, 10000);
        values.put(Advisor.class, 200);
        values.put(Elephant.class, 200);
        values.put(Horse.class, 400);
        values.put(Chariot.class, 900);
        values.put(Cannon.class, 450);
        values.put(Soldier.class, 100);
        return values;
    }
    
    private String formatMove(Move move) {
        if (move == null) return "无效移动";
        return String.format("从(%d,%d)到(%d,%d)", 
            move.getStart().getX(), move.getStart().getY(),
            move.getEnd().getX(), move.getEnd().getY());
    }
    
    private String getPhaseDescription(GamePhase phase) {
        switch (phase) {
            case OPENING: return "开局阶段";
            case MIDDLE_GAME: return "中局阶段";
            case ENDGAME: return "残局阶段";
            default: return "未知阶段";
        }
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (llmAI != null) {
            llmAI.close();
        }
    }
    
    // 枚举类
    private enum GamePhase {
        OPENING,     // 开局
        MIDDLE_GAME, // 中局
        ENDGAME      // 残局
    }
}