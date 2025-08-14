package com.example.junqi.ai;

import com.example.junqi.core.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 军棋AI引擎
 * 支持多种AI策略和难度级别
 */
public class JunQiAIEngine {
    
    public enum AIType {
        BASIC("基础AI"),
        ADVANCED("高级AI"),
        NEURAL("神经网络AI"),
        LLM("大模型AI");
        
        public final String displayName;
        
        AIType(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public enum Difficulty {
        EASY(1, 2, 500),      // 简单：搜索深度2，500ms
        MEDIUM(2, 4, 1000),   // 普通：搜索深度4，1s
        HARD(3, 6, 2000),     // 困难：搜索深度6，2s
        EXPERT(4, 8, 3000),   // 专家：搜索深度8，3s
        MASTER(5, 10, 5000);  // 大师：搜索深度10，5s
        
        public final int level;
        public final int searchDepth;
        public final int maxThinkTime;
        
        Difficulty(int level, int searchDepth, int maxThinkTime) {
            this.level = level;
            this.searchDepth = searchDepth;
            this.maxThinkTime = maxThinkTime;
        }
        
        public static Difficulty fromString(String diffStr) {
            switch (diffStr) {
                case "简单": return EASY;
                case "普通": return MEDIUM;
                case "困难": return HARD;
                case "专家": return EXPERT;
                case "大师": return MASTER;
                default: return MEDIUM;
            }
        }
    }
    
    private final AIType aiType;
    private final Difficulty difficulty;
    private final String modelName;
    private final ExecutorService executorService;
    private ThinkingCallback thinkingCallback;
    private final Random random;
    
    // 思考回调接口
    public interface ThinkingCallback {
        void onThinking(String message);
        void onMoveDecision(int fromRow, int fromCol, int toRow, int toCol, String analysis);
    }
    
    public JunQiAIEngine(String aiTypeStr, String difficultyStr, String modelName) {
        this.aiType = parseAIType(aiTypeStr);
        this.difficulty = Difficulty.fromString(difficultyStr);
        this.modelName = modelName;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "JunQi-AI-Thread");
            t.setDaemon(true);
            return t;
        });
        this.random = new Random();
    }
    
    /**
     * 解析AI类型字符串为枚举值
     */
    private AIType parseAIType(String aiTypeStr) {
        if (aiTypeStr.contains("基础")) return AIType.BASIC;
        if (aiTypeStr.contains("高级")) return AIType.ADVANCED;
        if (aiTypeStr.contains("神经网络")) return AIType.NEURAL;
        if (aiTypeStr.contains("大模型")) return AIType.LLM;
        // 默认使用基础AI
        return AIType.BASIC;
    }
    
    /**
     * 设置思考回调
     */
    public void setThinkingCallback(ThinkingCallback callback) {
        this.thinkingCallback = callback;
    }
    
    /**
     * 异步获取下一步移动
     */
    public CompletableFuture<AIMove> getNextMoveAsync(JunQiBoard board) {
        return CompletableFuture.supplyAsync(() -> getNextMove(board), executorService);
    }
    
    /**
     * 同步获取下一步移动
     */
    public AIMove getNextMove(JunQiBoard board) {
        if (thinkingCallback != null) {
            thinkingCallback.onThinking("正在分析棋局...");
        }
        
        long startTime = System.currentTimeMillis();
        AIMove bestMove = null;
        
        try {
            switch (aiType) {
                case BASIC:
                    bestMove = getBasicAIMove(board);
                    break;
                case ADVANCED:
                    bestMove = getAdvancedAIMove(board);
                    break;
                case NEURAL:
                    bestMove = getNeuralAIMove(board);
                    break;
                case LLM:
                    bestMove = getLLMAIMove(board);
                    break;
                default:
                    bestMove = getBasicAIMove(board);
            }
            
            if (bestMove != null && thinkingCallback != null) {
                long thinkTime = System.currentTimeMillis() - startTime;
                String analysis = String.format("思考时间: %dms, 策略: %s", thinkTime, aiType.displayName);
                
                int[] moveData = bestMove.getMoveData();
                if (moveData.length == 4) {
                    thinkingCallback.onMoveDecision(moveData[0], moveData[1], moveData[2], moveData[3], analysis);
                } else if (moveData.length == 2) {
                    thinkingCallback.onMoveDecision(moveData[0], moveData[1], -1, -1, "翻棋 - " + analysis);
                }
            }
            
        } catch (Exception e) {
            if (thinkingCallback != null) {
                thinkingCallback.onThinking("AI思考出现错误: " + e.getMessage());
            }
            // 降级到基础AI
            bestMove = getBasicAIMove(board);
        }
        
        return bestMove;
    }
    
    /**
     * 基础AI策略：随机选择合法移动
     */
    private AIMove getBasicAIMove(JunQiBoard board) {
        List<AIMove> allMoves = getAllPossibleMoves(board);
        
        if (allMoves.isEmpty()) {
            return null;
        }
        
        // 优先翻棋，如果有可翻的棋子
        List<AIMove> flipMoves = new ArrayList<>();
        List<AIMove> regularMoves = new ArrayList<>();
        
        for (AIMove move : allMoves) {
            if (move.isFlip()) {
                flipMoves.add(move);
            } else {
                regularMoves.add(move);
            }
        }
        
        // 30%概率优先翻棋
        if (!flipMoves.isEmpty() && random.nextDouble() < 0.3) {
            return flipMoves.get(random.nextInt(flipMoves.size()));
        }
        
        // 否则随机选择移动
        if (!regularMoves.isEmpty()) {
            return regularMoves.get(random.nextInt(regularMoves.size()));
        }
        
        // 最后考虑翻棋
        if (!flipMoves.isEmpty()) {
            return flipMoves.get(random.nextInt(flipMoves.size()));
        }
        
        return allMoves.get(random.nextInt(allMoves.size()));
    }
    
    /**
     * 高级AI策略：评估移动质量
     */
    private AIMove getAdvancedAIMove(JunQiBoard board) {
        List<AIMove> allMoves = getAllPossibleMoves(board);
        
        if (allMoves.isEmpty()) {
            return null;
        }
        
        AIMove bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        boolean isRedAI = board.isRedTurn();
        
        if (thinkingCallback != null) {
            thinkingCallback.onThinking("评估 " + allMoves.size() + " 个可能的移动...");
        }
        
        for (AIMove move : allMoves) {
            double score = evaluateMove(board, move, isRedAI);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * 神经网络AI（预留接口）
     */
    private AIMove getNeuralAIMove(JunQiBoard board) {
        if (thinkingCallback != null) {
            thinkingCallback.onThinking("神经网络AI暂未实现，使用高级AI策略");
        }
        return getAdvancedAIMove(board);
    }
    
    /**
     * 大语言模型AI（预留接口）
     */
    private AIMove getLLMAIMove(JunQiBoard board) {
        if (thinkingCallback != null) {
            thinkingCallback.onThinking("大模型AI暂未实现，使用高级AI策略");
        }
        return getAdvancedAIMove(board);
    }
    
    /**
     * 获取所有可能的移动
     */
    private List<AIMove> getAllPossibleMoves(JunQiBoard board) {
        List<AIMove> moves = new ArrayList<>();
        boolean isRedTurn = board.isRedTurn();
        
        for (int row = 0; row < board.getBoardHeight(); row++) {
            for (int col = 0; col < board.getBoardWidth(); col++) {
                JunQiPiece piece = board.getPiece(row, col);
                
                if (piece != null && piece.isAlive()) {
                    // 检查翻棋
                    if (!piece.isVisible() && ((isRedTurn && piece.isRed()) || (!isRedTurn && !piece.isRed()))) {
                        moves.add(new AIMove(row, col)); // 翻棋
                    }
                    
                    // 检查移动
                    if (piece.isVisible() && piece.getType().canMove() && 
                        ((isRedTurn && piece.isRed()) || (!isRedTurn && !piece.isRed()))) {
                        
                        List<int[]> validMoves = board.getValidMoves(row, col);
                        for (int[] move : validMoves) {
                            moves.add(new AIMove(row, col, move[0], move[1])); // 移动
                        }
                    }
                }
            }
        }
        
        return moves;
    }
    
    /**
     * 评估移动的质量
     */
    private double evaluateMove(JunQiBoard board, AIMove move, boolean isRedAI) {
        double score = 0.0;
        
        if (move.isFlip()) {
            // 翻棋评分
            score += 10.0; // 基础翻棋分数
            
            // 边角位置翻棋分数更低（可能是地雷）
            int row = move.getMoveData()[0];
            int col = move.getMoveData()[1];
            if (isCornerOrEdge(board, row, col)) {
                score -= 5.0;
            }
            
        } else {
            // 移动评分
            int fromRow = move.getMoveData()[0];
            int fromCol = move.getMoveData()[1];
            int toRow = move.getMoveData()[2];
            int toCol = move.getMoveData()[3];
            
            JunQiPiece movingPiece = board.getPiece(fromRow, fromCol);
            JunQiPiece targetPiece = board.getPiece(toRow, toCol);
            
            if (movingPiece != null) {
                // 基础移动分数
                score += 5.0;
                
                if (targetPiece != null) {
                    // 攻击评分
                    if (movingPiece.canAttack(targetPiece)) {
                        score += targetPiece.getType().getPower() * 10; // 根据目标价值评分
                        
                        // 攻击军旗得最高分
                        if (targetPiece.getType() == PieceType.FLAG) {
                            score += 1000;
                        }
                    } else {
                        score -= 100; // 不能攻击的目标，扣分
                    }
                }
                
                // 位置评分
                score += evaluatePosition(board, toRow, toCol, movingPiece);
                
                // 安全性评分
                score -= evaluateDanger(board, toRow, toCol, isRedAI) * 5;
            }
        }
        
        // 添加随机因素，避免完全确定性
        score += (random.nextDouble() - 0.5) * 2;
        
        return score;
    }
    
    /**
     * 评估位置价值
     */
    private double evaluatePosition(JunQiBoard board, int row, int col, JunQiPiece piece) {
        double score = 0.0;
        
        // 中心位置更有价值
        int centerRow = board.getBoardHeight() / 2;
        int centerCol = board.getBoardWidth() / 2;
        double distanceToCenter = Math.sqrt(Math.pow(row - centerRow, 2) + Math.pow(col - centerCol, 2));
        score += (5.0 - distanceToCenter);
        
        // 前进奖励（朝对方阵地移动）
        if (piece.isRed() && row > centerRow) {
            score += 2.0; // 红方向下前进
        } else if (!piece.isRed() && row < centerRow) {
            score += 2.0; // 黑方向上前进
        }
        
        return score;
    }
    
    /**
     * 评估位置危险程度
     */
    private double evaluateDanger(JunQiBoard board, int row, int col, boolean isRedAI) {
        double danger = 0.0;
        
        // 检查周围是否有敌方棋子
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (board.isValidPosition(newRow, newCol)) {
                JunQiPiece nearbyPiece = board.getPiece(newRow, newCol);
                if (nearbyPiece != null && nearbyPiece.isAlive() && nearbyPiece.isVisible()) {
                    // 敌方棋子
                    if ((isRedAI && !nearbyPiece.isRed()) || (!isRedAI && nearbyPiece.isRed())) {
                        danger += nearbyPiece.getType().getPower();
                    }
                }
            }
        }
        
        return danger;
    }
    
    /**
     * 判断是否是边角位置
     */
    private boolean isCornerOrEdge(JunQiBoard board, int row, int col) {
        return row == 0 || row == board.getBoardHeight() - 1 || 
               col == 0 || col == board.getBoardWidth() - 1;
    }
    
    /**
     * 获取AI信息
     */
    public String getAIInfo() {
        return String.format("%s (难度: %s)", aiType.displayName, difficulty.name());
    }
    
    /**
     * 关闭AI引擎
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * AI移动类
     */
    public static class AIMove {
        private final int[] moveData;
        private final boolean isFlip;
        
        // 翻棋构造函数
        public AIMove(int row, int col) {
            this.moveData = new int[]{row, col};
            this.isFlip = true;
        }
        
        // 移动构造函数
        public AIMove(int fromRow, int fromCol, int toRow, int toCol) {
            this.moveData = new int[]{fromRow, fromCol, toRow, toCol};
            this.isFlip = false;
        }
        
        public int[] getMoveData() {
            return moveData.clone();
        }
        
        public boolean isFlip() {
            return isFlip;
        }
        
        @Override
        public String toString() {
            if (isFlip) {
                return String.format("翻棋 (%d,%d)", moveData[0], moveData[1]);
            } else {
                return String.format("移动 (%d,%d) -> (%d,%d)", 
                    moveData[0], moveData[1], moveData[2], moveData[3]);
            }
        }
    }
}
