package com.example.gomoku.ai;

import com.example.gomoku.core.GomokuBoard;
import com.example.gomoku.core.GameState;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.Timer;

/**
 * 五子棋AI引擎 - 统一的AI接口
 * 支持多种AI策略：基础AI、高级AI、神经网络AI、大模型AI
 */
public class GomokuAIEngine {
    
    public enum AIType {
        BASIC,      // 基础AI
        ADVANCED,   // 高级AI (Minimax + Alpha-Beta)
        NEURAL,     // 神经网络AI
        LLM         // 大语言模型AI
    }
    
    public enum Difficulty {
        EASY(1, 4, 500),        // 简单：搜索深度4，500ms
        MEDIUM(2, 6, 1000),     // 普通：搜索深度6，1s
        HARD(3, 8, 2000),       // 困难：搜索深度8，2s
        EXPERT(4, 10, 3000),    // 专家：搜索深度10，3s
        MASTER(5, 12, 5000);    // 大师：搜索深度12，5s
        
        public final int level;
        public final int searchDepth;
        public final int thinkTimeMs;
        
        Difficulty(int level, int searchDepth, int thinkTimeMs) {
            this.level = level;
            this.searchDepth = searchDepth;
            this.thinkTimeMs = thinkTimeMs;
        }
        
        public static Difficulty fromString(String difficultyStr) {
            switch (difficultyStr) {
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
    
    // AI思考回调
    public interface ThinkingCallback {
        void onThinking(String message);
        void onMoveDecision(int[] move, String analysis);
    }
    
    private ThinkingCallback thinkingCallback;
    
    public GomokuAIEngine(AIType aiType, Difficulty difficulty, String modelName) {
        this.aiType = aiType;
        this.difficulty = difficulty;
        this.modelName = modelName;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GomokuAI-Thread");
            t.setDaemon(true);
            return t;
        });
    }
    
    public GomokuAIEngine(String aiTypeStr, String difficultyStr, String modelName) {
        this(parseAIType(aiTypeStr), Difficulty.fromString(difficultyStr), modelName);
    }
    
    private static AIType parseAIType(String aiTypeStr) {
        switch (aiTypeStr) {
            case "基础AI": return AIType.BASIC;
            case "高级AI": return AIType.ADVANCED;
            case "神经网络AI": return AIType.NEURAL;
            case "大模型AI": return AIType.LLM;
            default: return AIType.ADVANCED;
        }
    }
    
    /**
     * 获取下一步走法（异步）
     */
    public CompletableFuture<int[]> getNextMoveAsync(GomokuBoard board) {
        return CompletableFuture.supplyAsync(() -> getNextMove(board), executorService);
    }
    
    /**
     * 获取下一步走法（同步）
     */
    public int[] getNextMove(GomokuBoard board) {
        System.out.println("🔍 GomokuAIEngine.getNextMove() 被调用");
        
        if (board.getGameState() != GameState.PLAYING) {
            System.out.println("⚠️ AI退出: 游戏状态不是PLAYING: " + board.getGameState());
            return null;
        }
        
        System.out.println("🔥 AI类型: " + aiType + ", 难度: " + difficulty);
        notifyThinking("🤖 AI正在分析棋局...");
        
        long startTime = System.currentTimeMillis();
        int[] move = null;
        String analysis = "";
        
        try {
            switch (aiType) {
                case BASIC:
                    move = getBasicMove(board);
                    analysis = "基础策略走法";
                    break;
                case ADVANCED:
                    move = getAdvancedMove(board);
                    analysis = "高级算法分析";
                    break;
                case NEURAL:
                    move = getNeuralMove(board);
                    analysis = "神经网络预测";
                    break;
                case LLM:
                    move = getLLMMove(board);
                    analysis = "大模型分析走法";
                    break;
            }
        } catch (Exception e) {
            System.err.println("AI计算出错: " + e.getMessage());
            // 降级到基础AI
            move = getBasicMove(board);
            analysis = "降级基础走法";
        }
        
        long endTime = System.currentTimeMillis();
        String finalAnalysis = analysis + " (用时: " + (endTime - startTime) + "ms)";
        
        if (thinkingCallback != null && move != null) {
            thinkingCallback.onMoveDecision(move, finalAnalysis);
        }
        
        return move;
    }
    
    /**
     * 基础AI走法
     */
    private int[] getBasicMove(GomokuBoard board) {
        notifyThinking("使用基础AI策略...");
        System.out.println("📈 基础AI开始分析...");
        
        // 优先级策略
        int[] move;
        
        // 1. 检查是否可以直接获胜
        System.out.println("🎯 检查获胜走法...");
        move = findWinningMove(board, getCurrentPlayer(board));
        if (move != null) {
            notifyThinking("发现获胜走法！");
            System.out.println("✅ 找到获胜走法: (" + move[0] + ", " + move[1] + ")");
            return move;
        }
        
        // 2. 检查是否需要防守
        System.out.println("🛡️ 检查防守走法...");
        char opponent = getOpponent(board);
        move = findWinningMove(board, opponent);
        if (move != null) {
            notifyThinking("防守对手获胜走法");
            System.out.println("🛡️ 找到防守走法: (" + move[0] + ", " + move[1] + ")");
            return move;
        }
        
        // 3. 寻找最佳位置
        System.out.println("🎲 寻找最佳位置...");
        move = findBestMove(board);
        if (move != null) {
            System.out.println("📍 找到最佳位置: (" + move[0] + ", " + move[1] + ")");
            return move;
        }
        
        // 4. 随机选择
        System.out.println("🎲 使用随机走法...");
        move = getRandomMove(board);
        System.out.println("🎲 随机走法: (" + (move != null ? move[0] + ", " + move[1] : "null") + ")");
        return move;
    }
    
    /**
     * 高级AI走法
     */
    private int[] getAdvancedMove(GomokuBoard board) {
        notifyThinking("使用高级AI策略 (Minimax + Alpha-Beta)...");
        
        MinimaxAI minimaxAI = new MinimaxAI(difficulty.searchDepth);
        return minimaxAI.getBestMove(board);
    }
    
    /**
     * 神经网络AI走法 - 使用GomokuZero AI
     */
    private int[] getNeuralMove(GomokuBoard board) {
        notifyThinking("使用GomokuZero神经网络AI...");
        System.out.println("🧠 调用GomokuZero AI引擎...");
        
        try {
            GomokuZeroAI gomokuZero = new GomokuZeroAI(difficulty.level);
            int[] move = gomokuZero.getBestMove(board);
            
            if (move != null) {
                String thinkingProcess = gomokuZero.getThinkingProcess();
                notifyThinking("GomokuZero分析: " + thinkingProcess);
                System.out.println("🧠 GomokuZero返回走法: (" + move[0] + ", " + move[1] + ")");
                return move;
            } else {
                System.out.println("⚠️ GomokuZero返回null，降级到高级AI");
                return getAdvancedMove(board);
            }
        } catch (Exception e) {
            System.err.println("❌ GomokuZero AI执行失败: " + e.getMessage());
            e.printStackTrace();
            // 降级到高级AI
            return getAdvancedMove(board);
        }
    }
    
    /**
     * 大语言模型AI走法
     */
    private int[] getLLMMove(GomokuBoard board) {
        notifyThinking("使用大语言模型分析...");
        System.out.println("🤖 LLM AI开始思考...");
        
        // TODO: 集成实际的大语言模型API
        // 为了避免 Minimax 复杂度问题，临时使用基础AI
        System.out.println("🔄 使用基础AI代替大模型AI...");
        return getBasicMove(board);
    }
    
    /**
     * 寻找获胜走法
     */
    private int[] findWinningMove(GomokuBoard board, char player) {
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) == ' ') {
                    // 尝试在这里放棋子
                    board.setPiece(row, col, player);
                    
                    if (isWinning(board, row, col, player)) {
                        board.setPiece(row, col, ' '); // 恢复
                        return new int[]{row, col};
                    }
                    
                    board.setPiece(row, col, ' '); // 恢复
                }
            }
        }
        return null;
    }
    
    /**
     * 检查指定位置是否能获胜
     */
    private boolean isWinning(GomokuBoard board, int row, int col, char player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int count = 1;
            
            // 向正方向统计
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 0 && r < GomokuBoard.BOARD_SIZE && 
                   c >= 0 && c < GomokuBoard.BOARD_SIZE && 
                   board.getPiece(r, c) == player) {
                count++;
                r += dir[0];
                c += dir[1];
            }
            
            // 向负方向统计
            r = row - dir[0];
            c = col - dir[1];
            while (r >= 0 && r < GomokuBoard.BOARD_SIZE && 
                   c >= 0 && c < GomokuBoard.BOARD_SIZE && 
                   board.getPiece(r, c) == player) {
                count++;
                r -= dir[0];
                c -= dir[1];
            }
            
            if (count >= 5) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 寻找最佳走法
     */
    private int[] findBestMove(GomokuBoard board) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        
        List<int[]> candidates = getCandidatePositions(board);
        
        for (int[] pos : candidates) {
            int row = pos[0];
            int col = pos[1];
            
            int score = evaluatePosition(board, row, col);
            if (score > bestScore) {
                bestScore = score;
                bestMove = new int[]{row, col};
            }
        }
        
        return bestMove;
    }
    
    /**
     * 获取候选位置
     */
    private List<int[]> getCandidatePositions(GomokuBoard board) {
        List<int[]> candidates = new ArrayList<>();
        
        // 优先考虑已有棋子周围的位置
        boolean[][] visited = new boolean[GomokuBoard.BOARD_SIZE][GomokuBoard.BOARD_SIZE];
        
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) != ' ') {
                    // 添加周围的空位置
                    for (int dr = -2; dr <= 2; dr++) {
                        for (int dc = -2; dc <= 2; dc++) {
                            int newRow = row + dr;
                            int newCol = col + dc;
                            if (newRow >= 0 && newRow < GomokuBoard.BOARD_SIZE &&
                                newCol >= 0 && newCol < GomokuBoard.BOARD_SIZE &&
                                !visited[newRow][newCol] &&
                                board.getPiece(newRow, newCol) == ' ') {
                                candidates.add(new int[]{newRow, newCol});
                                visited[newRow][newCol] = true;
                            }
                        }
                    }
                }
            }
        }
        
        // 如果没有候选位置（空棋盘），选择中心
        if (candidates.isEmpty()) {
            candidates.add(new int[]{GomokuBoard.BOARD_SIZE / 2, GomokuBoard.BOARD_SIZE / 2});
        }
        
        return candidates;
    }
    
    /**
     * 评估位置分数
     */
    private int evaluatePosition(GomokuBoard board, int row, int col) {
        char currentPlayer = getCurrentPlayer(board);
        char opponent = getOpponent(board);
        
        int myScore = evaluateForPlayer(board, row, col, currentPlayer);
        int opponentScore = evaluateForPlayer(board, row, col, opponent);
        
        return myScore * 2 + opponentScore; // 自己的分数权重更高
    }
    
    /**
     * 为特定玩家评估位置
     */
    private int evaluateForPlayer(GomokuBoard board, int row, int col, char player) {
        int totalScore = 0;
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            totalScore += evaluateDirection(board, row, col, dir[0], dir[1], player);
        }
        
        return totalScore;
    }
    
    /**
     * 评估特定方向的分数
     */
    private int evaluateDirection(GomokuBoard board, int row, int col, int dr, int dc, char player) {
        int count = 1;
        int blocks = 0;
        
        // 向正方向
        int r = row + dr;
        int c = col + dc;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
            if (board.getPiece(r, c) == player) {
                count++;
            } else if (board.getPiece(r, c) == ' ') {
                break;
            } else {
                blocks++;
                break;
            }
            r += dr;
            c += dc;
        }
        
        // 向负方向
        r = row - dr;
        c = col - dc;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
            if (board.getPiece(r, c) == player) {
                count++;
            } else if (board.getPiece(r, c) == ' ') {
                break;
            } else {
                blocks++;
                break;
            }
            r -= dr;
            c -= dc;
        }
        
        return getPatternScore(count, blocks);
    }
    
    /**
     * 根据棋形获取分数
     */
    private int getPatternScore(int count, int blocks) {
        if (blocks == 2) return 0; // 被两端封死
        
        switch (count) {
            case 5: return 100000; // 五连
            case 4: return blocks == 0 ? 10000 : 1000; // 活四 vs 冲四
            case 3: return blocks == 0 ? 1000 : 100;   // 活三 vs 眠三
            case 2: return blocks == 0 ? 100 : 10;     // 活二 vs 眠二
            case 1: return 1;
            default: return 0;
        }
    }
    
    /**
     * 获取随机走法
     */
    private int[] getRandomMove(GomokuBoard board) {
        List<int[]> emptyPositions = new ArrayList<>();
        
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) == ' ') {
                    emptyPositions.add(new int[]{row, col});
                }
            }
        }
        
        if (!emptyPositions.isEmpty()) {
            Random random = new Random();
            return emptyPositions.get(random.nextInt(emptyPositions.size()));
        }
        
        return null;
    }
    
    /**
     * 获取当前玩家
     */
    private char getCurrentPlayer(GomokuBoard board) {
        return board.isBlackTurn() ? GomokuBoard.BLACK : GomokuBoard.WHITE;
    }
    
    /**
     * 获取对手
     */
    private char getOpponent(GomokuBoard board) {
        return board.isBlackTurn() ? GomokuBoard.WHITE : GomokuBoard.BLACK;
    }
    
    /**
     * 通知思考状态
     */
    private void notifyThinking(String message) {
        if (thinkingCallback != null) {
            thinkingCallback.onThinking(message);
        }
    }
    
    /**
     * 设置思考回调
     */
    public void setThinkingCallback(ThinkingCallback callback) {
        this.thinkingCallback = callback;
    }
    
    /**
     * 获取AI信息
     */
    public String getAIInfo() {
        return String.format("AI类型: %s, 难度: %s, 搜索深度: %d", 
                           aiType, difficulty, difficulty.searchDepth);
    }
    
    /**
     * 释放资源
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
     * 内部Minimax AI实现
     */
    private static class MinimaxAI {
        private final int maxDepth;
        private static final int INFINITY = 1000000;
        
        public MinimaxAI(int maxDepth) {
            this.maxDepth = maxDepth;
        }
        
        public int[] getBestMove(GomokuBoard board) {
            MinimaxResult result = minimax(board, maxDepth, -INFINITY, INFINITY, true);
            return result.bestMove;
        }
        
        private MinimaxResult minimax(GomokuBoard board, int depth, int alpha, int beta, boolean maximizing) {
            if (depth == 0 || board.getGameState() != GameState.PLAYING) {
                return new MinimaxResult(evaluateBoard(board), null);
            }
            
            List<int[]> moves = getCandidateMoves(board);
            int[] bestMove = null;
            
            if (maximizing) {
                int maxEval = -INFINITY;
                
                for (int[] move : moves) {
                    if (makeMove(board, move[0], move[1])) {
                        MinimaxResult result = minimax(board, depth - 1, alpha, beta, false);
                        unmakeMove(board, move[0], move[1]);
                        
                        if (result.score > maxEval) {
                            maxEval = result.score;
                            bestMove = move;
                        }
                        
                        alpha = Math.max(alpha, result.score);
                        if (beta <= alpha) break;
                    }
                }
                
                return new MinimaxResult(maxEval, bestMove);
            } else {
                int minEval = INFINITY;
                
                for (int[] move : moves) {
                    if (makeMove(board, move[0], move[1])) {
                        MinimaxResult result = minimax(board, depth - 1, alpha, beta, true);
                        unmakeMove(board, move[0], move[1]);
                        
                        if (result.score < minEval) {
                            minEval = result.score;
                            bestMove = move;
                        }
                        
                        beta = Math.min(beta, result.score);
                        if (beta <= alpha) break;
                    }
                }
                
                return new MinimaxResult(minEval, bestMove);
            }
        }
        
        private List<int[]> getCandidateMoves(GomokuBoard board) {
            List<int[]> moves = new ArrayList<>();
            
            // 检查是否是空棋盘
            boolean isEmpty = true;
            for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
                for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                    if (board.getPiece(row, col) != ' ') {
                        isEmpty = false;
                        break;
                    }
                }
                if (!isEmpty) break;
            }
            
            if (isEmpty) {
                // 空棋盘时，选择中心位置
                moves.add(new int[]{GomokuBoard.BOARD_SIZE / 2, GomokuBoard.BOARD_SIZE / 2});
                return moves;
            }
            
            // 非空棋盘时，选择相邻的空位置
            for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
                for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                    if (board.getPiece(row, col) == ' ' && hasNeighbor(board, row, col)) {
                        moves.add(new int[]{row, col});
                    }
                }
            }
            
            return moves;
        }
        
        private boolean hasNeighbor(GomokuBoard board, int row, int col) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int r = row + dr;
                    int c = col + dc;
                    if (r >= 0 && r < GomokuBoard.BOARD_SIZE && 
                        c >= 0 && c < GomokuBoard.BOARD_SIZE && 
                        board.getPiece(r, c) != ' ') {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private boolean makeMove(GomokuBoard board, int row, int col) {
            return board.placePiece(row, col);
        }
        
        private void unmakeMove(GomokuBoard board, int row, int col) {
            board.setPiece(row, col, ' ');
            board.switchTurn();
        }
        
        private int evaluateBoard(GomokuBoard board) {
            return evaluateFullBoard(board);
        }
        
        /**
         * 完整的棋盘评估函数
         */
        private int evaluateFullBoard(GomokuBoard board) {
            int score = 0;
            char currentPlayer = board.isBlackTurn() ? GomokuBoard.BLACK : GomokuBoard.WHITE;
            char opponent = board.isBlackTurn() ? GomokuBoard.WHITE : GomokuBoard.BLACK;
            
            // 遍历棋盘的每个位置
            for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
                for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                    char piece = board.getPiece(row, col);
                    
                    if (piece == currentPlayer) {
                        score += evaluatePositionForPiece(board, row, col, piece);
                    } else if (piece == opponent) {
                        score -= evaluatePositionForPiece(board, row, col, piece);
                    }
                }
            }
            
            return score;
        }
        
        /**
         * 评估特定位置的棋子价值
         */
        private int evaluatePositionForPiece(GomokuBoard board, int row, int col, char piece) {
            int totalScore = 0;
            int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
            
            for (int[] dir : directions) {
                PatternInfo pattern = analyzePattern(board, row, col, dir[0], dir[1], piece);
                totalScore += getPatternValue(pattern);
            }
            
            return totalScore;
        }
        
        /**
         * 分析棋形模式
         */
        private PatternInfo analyzePattern(GomokuBoard board, int row, int col, int dr, int dc, char piece) {
            int count = 1; // 包括当前位置
            int leftEmpty = 0;
            int rightEmpty = 0;
            int leftBlocked = 0;
            int rightBlocked = 0;
            
            // 向正方向分析
            int r = row + dr, c = col + dc;
            while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
                if (board.getPiece(r, c) == piece) {
                    count++;
                } else if (board.getPiece(r, c) == ' ') {
                    rightEmpty++;
                    if (rightEmpty == 1) { // 只看第一个空位后的情况
                        r += dr; c += dc;
                        if (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board.getPiece(r, c) == piece) {
                            // 跳跃连接
                            int jumpCount = 0;
                            while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board.getPiece(r, c) == piece) {
                                jumpCount++;
                                r += dr; c += dc;
                            }
                            count += jumpCount;
                        }
                    }
                    break;
                } else {
                    rightBlocked = 1;
                    break;
                }
                r += dr; c += dc;
            }
            
            if (r < 0 || r >= GomokuBoard.BOARD_SIZE || c < 0 || c >= GomokuBoard.BOARD_SIZE) {
                rightBlocked = 1; // 边界视为阻挡
            }
            
            // 向负方向分析
            r = row - dr; c = col - dc;
            while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
                if (board.getPiece(r, c) == piece) {
                    count++;
                } else if (board.getPiece(r, c) == ' ') {
                    leftEmpty++;
                    if (leftEmpty == 1) { // 只看第一个空位后的情况
                        r -= dr; c -= dc;
                        if (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board.getPiece(r, c) == piece) {
                            // 跳跃连接
                            int jumpCount = 0;
                            while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE && board.getPiece(r, c) == piece) {
                                jumpCount++;
                                r -= dr; c -= dc;
                            }
                            count += jumpCount;
                        }
                    }
                    break;
                } else {
                    leftBlocked = 1;
                    break;
                }
                r -= dr; c -= dc;
            }
            
            if (r < 0 || r >= GomokuBoard.BOARD_SIZE || c < 0 || c >= GomokuBoard.BOARD_SIZE) {
                leftBlocked = 1; // 边界视为阻挡
            }
            
            return new PatternInfo(count, leftEmpty, rightEmpty, leftBlocked, rightBlocked);
        }
        
        /**
         * 根据棋形模式获取价值分数
         */
        private int getPatternValue(PatternInfo pattern) {
            int count = pattern.count;
            int openEnds = (pattern.leftBlocked == 0 ? 1 : 0) + (pattern.rightBlocked == 0 ? 1 : 0);
            
            if (count >= 5) {
                return 500000; // 五连或更多，必胜
            }
            
            switch (count) {
                case 4:
                    if (openEnds == 2) return 50000;    // 活四
                    if (openEnds == 1) return 10000;    // 冲四
                    return 0;                            // 死四
                    
                case 3:
                    if (openEnds == 2) return 5000;     // 活三
                    if (openEnds == 1) return 500;      // 眠三
                    return 0;                            // 死三
                    
                case 2:
                    if (openEnds == 2) return 300;      // 活二
                    if (openEnds == 1) return 50;       // 眠二
                    return 0;                            // 死二
                    
                case 1:
                    if (openEnds == 2) return 15;       // 单子有发展空间
                    if (openEnds == 1) return 5;        // 单子一端被堵
                    return 0;                            // 死子
                    
                default:
                    return 0;
            }
        }
        
        /**
         * 棋形模式信息类
         */
        private static class PatternInfo {
            final int count;        // 连子数
            final int leftEmpty;    // 左侧空位数
            final int rightEmpty;   // 右侧空位数
            final int leftBlocked;  // 左侧是否被阻挡
            final int rightBlocked; // 右侧是否被阻挡
            
            PatternInfo(int count, int leftEmpty, int rightEmpty, int leftBlocked, int rightBlocked) {
                this.count = count;
                this.leftEmpty = leftEmpty;
                this.rightEmpty = rightEmpty;
                this.leftBlocked = leftBlocked;
                this.rightBlocked = rightBlocked;
            }
        }
        
        private static class MinimaxResult {
            final int score;
            final int[] bestMove;
            
            MinimaxResult(int score, int[] bestMove) {
                this.score = score;
                this.bestMove = bestMove;
            }
        }
    }
}
