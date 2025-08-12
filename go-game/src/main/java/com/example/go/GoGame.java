package com.example.go;

import java.util.*;

/**
 * 围棋游戏核心类
 */
public class GoGame {
    public static final int BOARD_SIZE = 19; // 标准围棋棋盘大小
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    
    private int[][] board;
    private int currentPlayer;
    private List<GoMove> moveHistory;
    private Set<String> capturedGroups;
    private int blackCaptured;
    private int whiteCaptured;
    private boolean gameEnded;
    private int consecutivePasses;
    
    public GoGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = BLACK; // 黑棋先行
        moveHistory = new ArrayList<>();
        capturedGroups = new HashSet<>();
        blackCaptured = 0;
        whiteCaptured = 0;
        gameEnded = false;
        consecutivePasses = 0;
        initializeBoard();
    }
    
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }
    
    /**
     * 下棋
     */
    public boolean makeMove(int row, int col) {
        if (gameEnded || !isValidMove(row, col)) {
            return false;
        }
        
        // 重置连续弃权计数
        consecutivePasses = 0;
        
        // 放置棋子
        board[row][col] = currentPlayer;
        
        // 检查并移除被吃掉的对方棋子
        int opponent = (currentPlayer == BLACK) ? WHITE : BLACK;
        List<GoPosition> capturedStones = new ArrayList<>();
        
        // 检查四个方向的对方棋子群
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (isValidPosition(newRow, newCol) && board[newRow][newCol] == opponent) {
                Set<GoPosition> group = getGroup(newRow, newCol);
                if (hasNoLiberties(group)) {
                    capturedStones.addAll(group);
                }
            }
        }
        
        // 移除被吃掉的棋子
        for (GoPosition pos : capturedStones) {
            board[pos.row][pos.col] = EMPTY;
            if (opponent == BLACK) {
                blackCaptured++;
            } else {
                whiteCaptured++;
            }
        }
        
        // 检查自杀规则（如果自己的棋子群没有气，则不能下）
        Set<GoPosition> myGroup = getGroup(row, col);
        if (hasNoLiberties(myGroup) && capturedStones.isEmpty()) {
            // 撤销移动
            board[row][col] = EMPTY;
            return false;
        }
        
        // 记录移动
        GoMove move = new GoMove(new GoPosition(row, col), currentPlayer);
        move.capturedStones.addAll(capturedStones);
        moveHistory.add(move);
        
        // 切换玩家
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        
        return true;
    }
    
    /**
     * 弃权
     */
    public void pass() {
        consecutivePasses++;
        if (consecutivePasses >= 2) {
            gameEnded = true;
        }
        
        GoMove move = new GoMove(null, currentPlayer);
        moveHistory.add(move);
        
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
    }
    
    /**
     * 悔棋
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }
        
        GoMove lastMove = moveHistory.remove(moveHistory.size() - 1);
        
        // 如果是弃权，直接切换玩家
        if (lastMove.position == null) {
            currentPlayer = lastMove.player;
            consecutivePasses = Math.max(0, consecutivePasses - 1);
            return true;
        }
        
        // 移除最后下的棋子
        board[lastMove.position.row][lastMove.position.col] = EMPTY;
        
        // 恢复被吃掉的棋子
        for (GoPosition pos : lastMove.capturedStones) {
            int capturedPlayer = (lastMove.player == BLACK) ? WHITE : BLACK;
            board[pos.row][pos.col] = capturedPlayer;
            
            if (capturedPlayer == BLACK) {
                blackCaptured--;
            } else {
                whiteCaptured--;
            }
        }
        
        // 切换回上一个玩家
        currentPlayer = lastMove.player;
        gameEnded = false;
        consecutivePasses = 0;
        
        return true;
    }
    
    /**
     * 检查移动是否有效
     */
    public boolean isValidMove(int row, int col) {
        return isValidPosition(row, col) && board[row][col] == EMPTY;
    }
    
    /**
     * 检查位置是否在棋盘内
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
    
    /**
     * 获取棋子群
     */
    private Set<GoPosition> getGroup(int row, int col) {
        Set<GoPosition> group = new HashSet<>();
        Set<GoPosition> visited = new HashSet<>();
        int color = board[row][col];
        
        if (color == EMPTY) {
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
    
    /**
     * 检查棋子群是否没有气
     */
    private boolean hasNoLiberties(Set<GoPosition> group) {
        for (GoPosition pos : group) {
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newRow = pos.row + dir[0];
                int newCol = pos.col + dir[1];
                
                if (isValidPosition(newRow, newCol) && board[newRow][newCol] == EMPTY) {
                    return false; // 找到气
                }
            }
        }
        return true; // 没有气
    }
    
    // Getters
    public int[][] getBoard() {
        return board;
    }
    
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    
    public boolean isGameEnded() {
        return gameEnded;
    }
    
    public int getBlackCaptured() {
        return blackCaptured;
    }
    
    public int getWhiteCaptured() {
        return whiteCaptured;
    }
    
    public List<GoMove> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
    
    public int getConsecutivePasses() {
        return consecutivePasses;
    }
    
    /**
     * 重新开始游戏
     */
    public void restart() {
        initializeBoard();
        currentPlayer = BLACK;
        moveHistory.clear();
        capturedGroups.clear();
        blackCaptured = 0;
        whiteCaptured = 0;
        gameEnded = false;
        consecutivePasses = 0;
    }
}