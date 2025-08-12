package com.example.go;

import java.util.*;

/**
 * 围棋AI类
 */
public class GoAI {
    private int aiPlayer;
    private int difficulty;
    private Random random;
    
    public GoAI(int aiPlayer, int difficulty) {
        this.aiPlayer = aiPlayer;
        this.difficulty = Math.max(1, Math.min(5, difficulty)); // 限制难度在1-5之间
        this.random = new Random();
    }
    
    /**
     * 计算AI的下一步移动
     */
    public GoPosition calculateMove(GoGame game) {
        int[][] board = game.getBoard();
        List<GoPosition> validMoves = getValidMoves(board);
        
        if (validMoves.isEmpty()) {
            return null; // 弃权
        }
        
        switch (difficulty) {
            case 1:
                return getRandomMove(validMoves);
            case 2:
                return getBasicMove(game, validMoves);
            case 3:
                return getIntermediateMove(game, validMoves);
            case 4:
                return getAdvancedMove(game, validMoves);
            case 5:
                return getExpertMove(game, validMoves);
            default:
                return getRandomMove(validMoves);
        }
    }
    
    /**
     * 获取所有有效移动
     */
    private List<GoPosition> getValidMoves(int[][] board) {
        List<GoPosition> moves = new ArrayList<>();
        for (int i = 0; i < GoGame.BOARD_SIZE; i++) {
            for (int j = 0; j < GoGame.BOARD_SIZE; j++) {
                if (board[i][j] == GoGame.EMPTY) {
                    moves.add(new GoPosition(i, j));
                }
            }
        }
        return moves;
    }
    
    /**
     * 随机移动（难度1）
     */
    private GoPosition getRandomMove(List<GoPosition> validMoves) {
        return validMoves.get(random.nextInt(validMoves.size()));
    }
    
    /**
     * 基础移动（难度2）- 优先考虑角落和边
     */
    private GoPosition getBasicMove(GoGame game, List<GoPosition> validMoves) {
        // 优先选择角落
        for (GoPosition move : validMoves) {
            if (isCorner(move)) {
                return move;
            }
        }
        
        // 其次选择边
        for (GoPosition move : validMoves) {
            if (isEdge(move)) {
                return move;
            }
        }
        
        // 否则随机选择
        return getRandomMove(validMoves);
    }
    
    /**
     * 中级移动（难度3）- 考虑基本战术
     */
    private GoPosition getIntermediateMove(GoGame game, List<GoPosition> validMoves) {
        int[][] board = game.getBoard();
        
        // 1. 寻找可以吃子的位置
        for (GoPosition move : validMoves) {
            if (canCapture(board, move, aiPlayer)) {
                return move;
            }
        }
        
        // 2. 寻找可以救自己棋子的位置
        for (GoPosition move : validMoves) {
            if (canSaveOwnStones(board, move, aiPlayer)) {
                return move;
            }
        }
        
        // 3. 选择星位点
        for (GoPosition move : validMoves) {
            if (isStarPoint(move)) {
                return move;
            }
        }
        
        return getBasicMove(game, validMoves);
    }
    
    /**
     * 高级移动（难度4）- 考虑领地和形状
     */
    private GoPosition getAdvancedMove(GoGame game, List<GoPosition> validMoves) {
        int[][] board = game.getBoard();
        GoPosition bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (GoPosition move : validMoves) {
            double score = evaluateMove(board, move, aiPlayer);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove != null ? bestMove : getIntermediateMove(game, validMoves);
    }
    
    /**
     * 专家移动（难度5）- 使用简化的蒙特卡洛树搜索
     */
    private GoPosition getExpertMove(GoGame game, List<GoPosition> validMoves) {
        // 简化的MCTS实现
        GoPosition bestMove = null;
        double bestWinRate = 0.0;
        
        for (GoPosition move : validMoves) {
            double winRate = simulateMove(game, move);
            if (winRate > bestWinRate) {
                bestWinRate = winRate;
                bestMove = move;
            }
        }
        
        return bestMove != null ? bestMove : getAdvancedMove(game, validMoves);
    }
    
    /**
     * 检查是否是角落
     */
    private boolean isCorner(GoPosition pos) {
        return (pos.row == 0 || pos.row == GoGame.BOARD_SIZE - 1) &&
               (pos.col == 0 || pos.col == GoGame.BOARD_SIZE - 1);
    }
    
    /**
     * 检查是否是边
     */
    private boolean isEdge(GoPosition pos) {
        return pos.row == 0 || pos.row == GoGame.BOARD_SIZE - 1 ||
               pos.col == 0 || pos.col == GoGame.BOARD_SIZE - 1;
    }
    
    /**
     * 检查是否是星位点
     */
    private boolean isStarPoint(GoPosition pos) {
        int[] starPoints = {3, 9, 15}; // 19路棋盘的星位
        for (int r : starPoints) {
            for (int c : starPoints) {
                if (pos.row == r && pos.col == c) {
                    return true;
                }
            }
        }
        return pos.row == 9 && pos.col == 9; // 天元
    }
    
    /**
     * 检查是否可以吃子
     */
    private boolean canCapture(int[][] board, GoPosition move, int player) {
        // 模拟下棋，检查是否能吃掉对方棋子
        int opponent = (player == GoGame.BLACK) ? GoGame.WHITE : GoGame.BLACK;
        
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newRow = move.row + dir[0];
            int newCol = move.col + dir[1];
            
            if (isValidPosition(newRow, newCol) && board[newRow][newCol] == opponent) {
                // 检查这个对方棋子群是否会被吃掉
                Set<GoPosition> group = getGroup(board, newRow, newCol);
                if (wouldHaveNoLiberties(board, group, move, player)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查是否可以救自己的棋子
     */
    private boolean canSaveOwnStones(int[][] board, GoPosition move, int player) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newRow = move.row + dir[0];
            int newCol = move.col + dir[1];
            
            if (isValidPosition(newRow, newCol) && board[newRow][newCol] == player) {
                Set<GoPosition> group = getGroup(board, newRow, newCol);
                if (hasOnlyOneLibertyAt(board, group, move)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 评估移动的分数
     */
    private double evaluateMove(int[][] board, GoPosition move, int player) {
        double score = 0.0;
        
        // 基础位置价值
        if (isCorner(move)) score += 10;
        else if (isEdge(move)) score += 5;
        else if (isStarPoint(move)) score += 8;
        
        // 吃子价值
        if (canCapture(board, move, player)) score += 20;
        
        // 救子价值
        if (canSaveOwnStones(board, move, player)) score += 15;
        
        // 影响力评估（简化）
        score += calculateInfluence(board, move, player);
        
        return score;
    }
    
    /**
     * 计算影响力
     */
    private double calculateInfluence(int[][] board, GoPosition move, int player) {
        double influence = 0.0;
        int range = 3; // 影响范围
        
        for (int i = Math.max(0, move.row - range); 
             i <= Math.min(GoGame.BOARD_SIZE - 1, move.row + range); i++) {
            for (int j = Math.max(0, move.col - range); 
                 j <= Math.min(GoGame.BOARD_SIZE - 1, move.col + range); j++) {
                
                int distance = Math.abs(i - move.row) + Math.abs(j - move.col);
                if (distance <= range) {
                    if (board[i][j] == player) {
                        influence += (range - distance + 1) * 0.5;
                    } else if (board[i][j] != GoGame.EMPTY) {
                        influence -= (range - distance + 1) * 0.3;
                    }
                }
            }
        }
        
        return influence;
    }
    
    /**
     * 模拟移动并计算胜率
     */
    private double simulateMove(GoGame game, GoPosition move) {
        // 简化的模拟：进行少量随机对局
        int simulations = 50;
        int wins = 0;
        
        for (int i = 0; i < simulations; i++) {
            GoGame simGame = copyGame(game);
            if (simGame.makeMove(move.row, move.col)) {
                int result = playRandomGame(simGame);
                if (result == aiPlayer) {
                    wins++;
                }
            }
        }
        
        return (double) wins / simulations;
    }
    
    /**
     * 复制游戏状态
     */
    private GoGame copyGame(GoGame original) {
        GoGame copy = new GoGame();
        int[][] originalBoard = original.getBoard();
        int[][] copyBoard = copy.getBoard();
        
        for (int i = 0; i < GoGame.BOARD_SIZE; i++) {
            System.arraycopy(originalBoard[i], 0, copyBoard[i], 0, GoGame.BOARD_SIZE);
        }
        
        return copy;
    }
    
    /**
     * 进行随机对局
     */
    private int playRandomGame(GoGame game) {
        int moves = 0;
        int maxMoves = 100; // 限制最大移动数
        
        while (!game.isGameEnded() && moves < maxMoves) {
            List<GoPosition> validMoves = getValidMoves(game.getBoard());
            if (validMoves.isEmpty() || random.nextDouble() < 0.1) {
                game.pass();
            } else {
                GoPosition randomMove = validMoves.get(random.nextInt(validMoves.size()));
                game.makeMove(randomMove.row, randomMove.col);
            }
            moves++;
        }
        
        // 简化的胜负判断：比较被吃掉的棋子数
        if (game.getBlackCaptured() > game.getWhiteCaptured()) {
            return GoGame.WHITE;
        } else if (game.getWhiteCaptured() > game.getBlackCaptured()) {
            return GoGame.BLACK;
        } else {
            return random.nextBoolean() ? GoGame.BLACK : GoGame.WHITE;
        }
    }
    
    // 辅助方法
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < GoGame.BOARD_SIZE && col >= 0 && col < GoGame.BOARD_SIZE;
    }
    
    private Set<GoPosition> getGroup(int[][] board, int row, int col) {
        Set<GoPosition> group = new HashSet<>();
        Set<GoPosition> visited = new HashSet<>();
        int color = board[row][col];
        
        if (color == GoGame.EMPTY) {
            return group;
        }
        
        Queue<GoPosition> queue = new LinkedList<>();
        queue.offer(new GoPosition(row, col));
        visited.add(new GoPosition(row, col));
        
        while (!queue.isEmpty()) {
            GoPosition current = queue.poll();
            group.add(current);
            
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                GoPosition newPos = new GoPosition(newRow, newCol);
                
                if (isValidPosition(newRow, newCol) && 
                    !visited.contains(newPos) && 
                    board[newRow][newCol] == color) {
                    
                    queue.offer(newPos);
                    visited.add(newPos);
                }
            }
        }
        
        return group;
    }
    
    private boolean wouldHaveNoLiberties(int[][] board, Set<GoPosition> group, GoPosition newMove, int player) {
        for (GoPosition pos : group) {
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newRow = pos.row + dir[0];
                int newCol = pos.col + dir[1];
                
                if (isValidPosition(newRow, newCol)) {
                    if (board[newRow][newCol] == GoGame.EMPTY && 
                        !(newRow == newMove.row && newCol == newMove.col)) {
                        return false; // 还有其他气
                    }
                }
            }
        }
        return true;
    }
    
    private boolean hasOnlyOneLibertyAt(int[][] board, Set<GoPosition> group, GoPosition libertyPos) {
        int libertyCount = 0;
        for (GoPosition pos : group) {
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newRow = pos.row + dir[0];
                int newCol = pos.col + dir[1];
                
                if (isValidPosition(newRow, newCol) && board[newRow][newCol] == GoGame.EMPTY) {
                    if (newRow == libertyPos.row && newCol == libertyPos.col) {
                        continue; // 这是我们要下的位置
                    }
                    libertyCount++;
                    if (libertyCount > 0) {
                        return false; // 有其他气
                    }
                }
            }
        }
        return libertyCount == 0;
    }
}