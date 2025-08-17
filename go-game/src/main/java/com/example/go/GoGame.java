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
    
    // 劫争检测相关
    private String lastBoardState;
    private GoPosition lastCapturePosition;
    
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
        
        // 保存当前棋盘状态用于劫争检测
        String currentBoardState = getBoardStateString();
        
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
        
        // 劫争检测：如果这步棋导致棋盘状态回到上一步的状态，则禁止
        String newBoardState = getBoardStateString();
        if (capturedStones.size() == 1 && lastBoardState != null && newBoardState.equals(lastBoardState)) {
            // 撤销移动
            board[row][col] = EMPTY;
            // 恢复被吃掉的棋子
            for (GoPosition pos : capturedStones) {
                board[pos.row][pos.col] = opponent;
                if (opponent == BLACK) {
                    blackCaptured--;
                } else {
                    whiteCaptured--;
                }
            }
            return false; // 劫争，禁止此步
        }
        
        // 记录移动
        GoMove move = new GoMove(new GoPosition(row, col), currentPlayer);
        move.capturedStones.addAll(capturedStones);
        moveHistory.add(move);
        
        // 更新劫争检测状态
        lastBoardState = currentBoardState;
        if (capturedStones.size() == 1) {
            lastCapturePosition = new GoPosition(row, col);
        } else {
            lastCapturePosition = null;
        }
        
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
    
    /**
     * 获取棋盘状态的字符串表示，用于劫争检测
     */
    private String getBoardStateString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                sb.append(board[i][j]);
            }
        }
        return sb.toString();
    }

    /**
     * 载入一个预设的棋盘位置，用于死活题等特殊模式
     *
     * @param newBoard      预设棋盘数组
     * @param startingPlayer 先手棋色
     */
    public void loadPosition(int[][] newBoard, int startingPlayer) {
        initializeBoard();
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(newBoard[i], 0, board[i], 0, BOARD_SIZE);
        }
        this.currentPlayer = startingPlayer;
        moveHistory.clear();
        capturedGroups.clear();
        blackCaptured = 0;
        whiteCaptured = 0;
        gameEnded = false;
        consecutivePasses = 0;
        lastBoardState = null;
        lastCapturePosition = null;
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
     * 计算围棋目数和胜负结果
     */
    public GoGameResult calculateGameResult() {
        if (!gameEnded) {
            return null; // 游戏未结束
        }
        
        // 计算领地
        int[][] territory = calculateTerritory();
        
        // 计算目数
        int blackTerritory = 0;
        int whiteTerritory = 0;
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (territory[row][col] == BLACK) {
                    blackTerritory++;
                } else if (territory[row][col] == WHITE) {
                    whiteTerritory++;
                }
            }
        }
        
        // 计算总分：领地 + 吃掉的对方棋子
        double blackScore = blackTerritory + whiteCaptured;
        double whiteScore = whiteTerritory + blackCaptured + 6.5; // 白棋贴目6.5
        
        // 判断胜负
        int winner;
        double scoreDifference;
        if (blackScore > whiteScore) {
            winner = BLACK;
            scoreDifference = blackScore - whiteScore;
        } else {
            winner = WHITE;
            scoreDifference = whiteScore - blackScore;
        }
        
        return new GoGameResult(winner, blackScore, whiteScore, scoreDifference, 
                               blackTerritory, whiteTerritory, blackCaptured, whiteCaptured);
    }
    
    /**
     * 计算领地归属
     * 返回数组：EMPTY=中性区域，BLACK=黑棋领地，WHITE=白棋领地
     */
    private int[][] calculateTerritory() {
        int[][] territory = new int[BOARD_SIZE][BOARD_SIZE];
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == EMPTY && !visited[row][col]) {
                    // 找到一个空白区域，计算其归属
                    Set<GoPosition> emptyArea = new HashSet<>();
                    Set<Integer> surroundingColors = new HashSet<>();
                    
                    floodFillTerritory(row, col, emptyArea, surroundingColors, visited);
                    
                    // 判断这个空白区域的归属
                    int owner = EMPTY; // 默认中性
                    if (surroundingColors.size() == 1) {
                        // 只被一种颜色的棋子包围，属于该颜色
                        owner = surroundingColors.iterator().next();
                    }
                    // 如果被两种颜色包围，则为中性区域
                    
                    // 标记这个区域的归属
                    for (GoPosition pos : emptyArea) {
                        territory[pos.row][pos.col] = owner;
                    }
                }
            }
        }
        
        return territory;
    }
    
    /**
     * 洪水填充算法计算连通的空白区域
     */
    private void floodFillTerritory(int row, int col, Set<GoPosition> emptyArea, 
                                   Set<Integer> surroundingColors, boolean[][] visited) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || visited[row][col]) {
            return;
        }
        
        if (board[row][col] != EMPTY) {
            // 遇到棋子，记录颜色
            surroundingColors.add(board[row][col]);
            return;
        }
        
        // 标记为已访问
        visited[row][col] = true;
        emptyArea.add(new GoPosition(row, col));
        
        // 递归访问四个方向
        floodFillTerritory(row - 1, col, emptyArea, surroundingColors, visited);
        floodFillTerritory(row + 1, col, emptyArea, surroundingColors, visited);
        floodFillTerritory(row, col - 1, emptyArea, surroundingColors, visited);
        floodFillTerritory(row, col + 1, emptyArea, surroundingColors, visited);
    }
    
    /**
     * 强制结束游戏并计算结果
     */
    public void forceEndGame() {
        gameEnded = true;
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
        lastBoardState = null;
        lastCapturePosition = null;
    }
    
    /**
     * 围棋游戏结果类
     */
    public static class GoGameResult {
        public final int winner; // BLACK 或 WHITE
        public final double blackScore;
        public final double whiteScore;
        public final double scoreDifference;
        public final int blackTerritory;
        public final int whiteTerritory;
        public final int blackCaptured;
        public final int whiteCaptured;
        
        public GoGameResult(int winner, double blackScore, double whiteScore, double scoreDifference,
                           int blackTerritory, int whiteTerritory, int blackCaptured, int whiteCaptured) {
            this.winner = winner;
            this.blackScore = blackScore;
            this.whiteScore = whiteScore;
            this.scoreDifference = scoreDifference;
            this.blackTerritory = blackTerritory;
            this.whiteTerritory = whiteTerritory;
            this.blackCaptured = blackCaptured;
            this.whiteCaptured = whiteCaptured;
        }
        
        public String getWinnerName() {
            return winner == BLACK ? "黑棋" : "白棋";
        }
        
        public String getResultDescription() {
            return String.format("%s胜 %.1f目", getWinnerName(), scoreDifference);
        }
    }
}