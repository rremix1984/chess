package com.example.gomoku.ai;

import com.example.common.utils.ExceptionHandler;
import com.example.gomoku.core.GomokuBoard;
import com.example.gomoku.core.GameState;

import java.util.*;
import java.util.concurrent.*;

/**
 * GomokuZero AI引擎
 * 基于蒙特卡洛树搜索(MCTS)和神经网络的五子棋AI
 * 参考AlphaZero的思想，但简化为适合五子棋的版本
 */
public class GomokuZeroAI {
    
    private static final String LOG_TAG = "GomokuZero";
    
    // MCTS参数
    private static final int SIMULATIONS = 1600; // 默认模拟次数
    private static final double C_PUCT = 1.0; // 探索常数
    private static final double TEMPERATURE = 1.0; // 温度参数
    
    // 神经网络相关
    private GomokuNeuralNetwork neuralNetwork;
    private int difficulty;
    private int simulations;
    private String thinkingProcess = "";
    
    // 性能统计
    private long totalThinkTime = 0;
    private int totalMoves = 0;
    
    public GomokuZeroAI(int difficulty) {
        this.difficulty = Math.max(1, Math.min(10, difficulty));
        this.simulations = getSimulationsForDifficulty(difficulty);
        this.neuralNetwork = new GomokuNeuralNetwork();
        
        ExceptionHandler.logInfo(LOG_TAG, "🔧 GomokuZero AI初始化完成:");
        ExceptionHandler.logInfo(LOG_TAG, "   - 难度: " + difficulty);
        ExceptionHandler.logInfo(LOG_TAG, "   - 模拟次数: " + simulations);
    }
    
    /**
     * 根据难度获取模拟次数
     */
    private int getSimulationsForDifficulty(int difficulty) {
        switch (difficulty) {
            case 1: case 2: return 800;   // 简单 - 提升至800
            case 3: case 4: return 1600;  // 普通 - 提升至1600
            case 5: case 6: return 3200;  // 困难 - 提升至3200
            case 7: case 8: return 4800;  // 专家 - 提升至4800
            case 9: case 10: return 6400; // 大师 - 提升至6400
            default: return 1600;
        }
    }
    
    /**
     * 计算最佳移动
     */
    public int[] getBestMove(GomokuBoard board) {
        if (board == null || board.getGameState() != GameState.PLAYING) {
            System.out.println("🚫 GomokuZeroAI.getBestMove() 退出: 棋盘状态无效");
            return null;
        }
        
        System.out.println("🧠🧠🧠 ===========================================\n" + 
                         "🧠🧠🧠 GomokuZero AI 开始分析 (难度级别: " + difficulty + ")\n" +
                         "🧠🧠🧠 ===========================================");
        
        long startTime = System.currentTimeMillis();
        thinkingProcess = "🧠 GomokuZero AI 开始思考...\n";
        
        // 获取当前玩家
        boolean isBlackTurn = board.isBlackTurn();
        System.out.println("🧠 当前玩家: " + (isBlackTurn ? "黑棋 (●)" : "白棋 (○)"));
        thinkingProcess += "当前玩家: " + (isBlackTurn ? "黑棋" : "白棋") + "\n";
        
        try {
            // 如果是第一步，选择中心点
            if (isFirstMove(board)) {
                int center = GomokuBoard.BOARD_SIZE / 2;
                thinkingProcess += "开局选择: 天元(" + (char)('A' + center) + (center + 1) + ")\n";
                return new int[]{center, center};
            }
            
            // 运行MCTS搜索
            MCTSNode root = new MCTSNode(null, -1, -1, 1.0);
            BoardState currentState = new BoardState(board);
            
            thinkingProcess += "开始MCTS搜索，模拟次数: " + simulations + "\n";
            
            for (int i = 0; i < simulations; i++) {
                MCTSNode node = root;
                BoardState state = currentState.copy();
                List<MCTSNode> path = new ArrayList<>();
                
                // Selection - 选择阶段
                while (!node.children.isEmpty() && !state.isTerminal()) {
                    node = selectBestChild(node);
                    path.add(node);
                    if (node.move != -1) {
                        int row = node.move / GomokuBoard.BOARD_SIZE;
                        int col = node.move % GomokuBoard.BOARD_SIZE;
                        state.makeMove(row, col);
                    }
                }
                
                // Expansion - 扩展阶段
                if (!state.isTerminal() && node.visitCount > 0) {
                    expandNode(node, state);
                    if (!node.children.isEmpty()) {
                        node = node.children.get(new Random().nextInt(node.children.size()));
                        path.add(node);
                        if (node.move != -1) {
                            int row = node.move / GomokuBoard.BOARD_SIZE;
                            int col = node.move % GomokuBoard.BOARD_SIZE;
                            state.makeMove(row, col);
                        }
                    }
                }
                
                // Simulation - 模拟阶段
                double value = simulate(state);
                
                // Backpropagation - 反向传播阶段
                backpropagate(path, value);
                root.update(value);
                
                // 进度报告（每100次模拟）
                if ((i + 1) % 100 == 0) {
                    thinkingProcess += "进度: " + (i + 1) + "/" + simulations + " (" + 
                                     String.format("%.1f", (i + 1) * 100.0 / simulations) + "%)\n";
                }
            }
            
            // 选择最佳移动
            MCTSNode bestChild = getBestChild(root);
            if (bestChild != null && bestChild.move != -1) {
                int row = bestChild.move / GomokuBoard.BOARD_SIZE;
                int col = bestChild.move % GomokuBoard.BOARD_SIZE;
                
                long thinkTime = System.currentTimeMillis() - startTime;
                totalThinkTime += thinkTime;
                totalMoves++;
                
                thinkingProcess += "最佳走法: " + (char)('A' + col) + (row + 1) + "\n";
                thinkingProcess += "访问次数: " + bestChild.visitCount + "\n";
                thinkingProcess += "胜率: " + String.format("%.2f", bestChild.getWinRate() * 100) + "%\n";
                thinkingProcess += "思考时间: " + thinkTime + "ms\n";
                
                ExceptionHandler.logInfo(LOG_TAG, "✅ 选择走法: " + (char)('A' + col) + (row + 1) + 
                                       " (用时: " + thinkTime + "ms, 胜率: " + 
                                       String.format("%.1f", bestChild.getWinRate() * 100) + "%)");
                
                return new int[]{row, col};
            }
            
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "计算最佳移动失败: " + e.getMessage());
            thinkingProcess += "计算失败，使用备用算法\n";
            return getFallbackMove(board);
        }
        
        return getFallbackMove(board);
    }
    
    /**
     * 检查是否是第一步
     */
    private boolean isFirstMove(GomokuBoard board) {
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
     * 选择最佳子节点（UCB1公式）
     */
    private MCTSNode selectBestChild(MCTSNode node) {
        double bestValue = Double.NEGATIVE_INFINITY;
        MCTSNode bestChild = null;
        
        for (MCTSNode child : node.children) {
            double ucb1Value = child.getWinRate() + 
                             C_PUCT * Math.sqrt(Math.log(node.visitCount) / child.visitCount);
            
            if (ucb1Value > bestValue) {
                bestValue = ucb1Value;
                bestChild = child;
            }
        }
        
        return bestChild;
    }
    
    /**
     * 扩展节点
     */
    private void expandNode(MCTSNode node, BoardState state) {
        List<Integer> legalMoves = state.getLegalMoves();
        for (int move : legalMoves) {
            MCTSNode child = new MCTSNode(node, move, 0, neuralNetwork.getPrior(state, move));
            node.children.add(child);
        }
    }
    
    /**
     * 模拟游戏到结束
     */
    private double simulate(BoardState state) {
        BoardState simState = state.copy();
        Random random = new Random();
        
        while (!simState.isTerminal()) {
            List<Integer> legalMoves = simState.getLegalMoves();
            if (legalMoves.isEmpty()) break;
            
            int randomMove = legalMoves.get(random.nextInt(legalMoves.size()));
            int row = randomMove / GomokuBoard.BOARD_SIZE;
            int col = randomMove % GomokuBoard.BOARD_SIZE;
            simState.makeMove(row, col);
        }
        
        return simState.getValue();
    }
    
    /**
     * 反向传播
     */
    private void backpropagate(List<MCTSNode> path, double value) {
        for (MCTSNode node : path) {
            node.update(value);
            value = -value; // 交替玩家的价值相反
        }
    }
    
    /**
     * 获取最佳子节点
     */
    private MCTSNode getBestChild(MCTSNode root) {
        if (root.children.isEmpty()) return null;
        
        MCTSNode bestChild = null;
        int maxVisits = -1;
        
        for (MCTSNode child : root.children) {
            if (child.visitCount > maxVisits) {
                maxVisits = child.visitCount;
                bestChild = child;
            }
        }
        
        return bestChild;
    }
    
    /**
     * 备用移动算法（当MCTS失败时使用）
     */
    private int[] getFallbackMove(GomokuBoard board) {
        // 寻找可能的获胜位置或防御位置
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) == ' ') {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }
    
    /**
     * 获取思考过程
     */
    public String getThinkingProcess() {
        return thinkingProcess;
    }
    
    /**
     * 获取平均思考时间
     */
    public double getAverageThinkTime() {
        return totalMoves > 0 ? (double) totalThinkTime / totalMoves : 0;
    }
    
    /**
     * 获取AI统计信息
     */
    public String getStats() {
        return String.format("GomokuZero统计 - 总步数: %d, 平均用时: %.1fms, 难度: %d", 
                           totalMoves, getAverageThinkTime(), difficulty);
    }
    
    /**
     * MCTS节点类
     */
    private static class MCTSNode {
        MCTSNode parent;
        List<MCTSNode> children;
        int move; // -1 表示根节点
        int visitCount;
        double totalValue;
        double prior;
        
        MCTSNode(MCTSNode parent, int move, int visitCount, double prior) {
            this.parent = parent;
            this.children = new ArrayList<>();
            this.move = move;
            this.visitCount = visitCount;
            this.totalValue = 0;
            this.prior = prior;
        }
        
        void update(double value) {
            visitCount++;
            totalValue += value;
        }
        
        double getWinRate() {
            return visitCount > 0 ? totalValue / visitCount : 0;
        }
    }
    
    /**
     * 棋盘状态类
     */
    public static class BoardState {
        private char[][] board;
        private boolean isBlackTurn;
        private GameState gameState;
        
        BoardState(GomokuBoard gomokuBoard) {
            this.board = new char[GomokuBoard.BOARD_SIZE][GomokuBoard.BOARD_SIZE];
            for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
                for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                    this.board[row][col] = gomokuBoard.getPiece(row, col);
                }
            }
            this.isBlackTurn = gomokuBoard.isBlackTurn();
            this.gameState = gomokuBoard.getGameState();
        }
        
        BoardState copy() {
            BoardState copy = new BoardState(new GomokuBoard());
            for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
                System.arraycopy(this.board[row], 0, copy.board[row], 0, GomokuBoard.BOARD_SIZE);
            }
            copy.isBlackTurn = this.isBlackTurn;
            copy.gameState = this.gameState;
            return copy;
        }
        
        boolean isTerminal() {
            return gameState != GameState.PLAYING;
        }
        
        List<Integer> getLegalMoves() {
            List<Integer> moves = new ArrayList<>();
            for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
                for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                    if (board[row][col] == ' ') {
                        moves.add(row * GomokuBoard.BOARD_SIZE + col);
                    }
                }
            }
            return moves;
        }
        
        void makeMove(int row, int col) {
            if (row >= 0 && row < GomokuBoard.BOARD_SIZE && 
                col >= 0 && col < GomokuBoard.BOARD_SIZE && 
                board[row][col] == ' ') {
                
                board[row][col] = isBlackTurn ? GomokuBoard.BLACK : GomokuBoard.WHITE;
                
                // 简单胜负判断
                if (checkWin(row, col)) {
                    gameState = isBlackTurn ? GameState.BLACK_WINS : GameState.RED_WINS;
                }
                
                isBlackTurn = !isBlackTurn;
            }
        }
        
        boolean checkWin(int row, int col) {
            char piece = board[row][col];
            int[][] directions = {{0,1}, {1,0}, {1,1}, {1,-1}};
            
            for (int[] dir : directions) {
                int count = 1;
                
                // 正向计数
                for (int i = 1; i < 5; i++) {
                    int newRow = row + dir[0] * i;
                    int newCol = col + dir[1] * i;
                    if (newRow >= 0 && newRow < GomokuBoard.BOARD_SIZE &&
                        newCol >= 0 && newCol < GomokuBoard.BOARD_SIZE &&
                        board[newRow][newCol] == piece) {
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
                        board[newRow][newCol] == piece) {
                        count++;
                    } else {
                        break;
                    }
                }
                
                if (count >= 5) return true;
            }
            return false;
        }
        
        double getValue() {
            if (gameState == GameState.BLACK_WINS) {
                return isBlackTurn ? -1.0 : 1.0;
            } else if (gameState == GameState.RED_WINS) {
                return isBlackTurn ? 1.0 : -1.0;
            } else {
                return 0.0; // 平局或未结束
            }
        }
        
        /**
         * 获取棋盘状态，供神经网络使用
         */
        public char[][] getBoard() {
            return board;
        }
        
        /**
         * 获取当前玩家
         */
        public boolean isBlackTurn() {
            return isBlackTurn;
        }
    }
}
