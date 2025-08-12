package com.example.gomoku.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 五子棋棋盘类
 * 负责管理棋盘状态和规则
 */
public class GomokuBoard {

    // 棋盘大小常量
    public static final int BOARD_SIZE = 15;
    
    // 棋子类型常量
    public static final char BLACK = 'B'; // 黑子
    public static final char WHITE = 'W'; // 白子
    
    // 棋盘数组，存储棋子
    private char[][] board;
    
    // 当前回合
    private boolean isBlackTurn;
    
    // 游戏状态
    private GameState gameState;
    
    // 最后一步棋的位置，用于UI高亮显示
    private int lastMoveRow = -1;
    private int lastMoveCol = -1;
    
    /**
     * 构造函数
     */
    public GomokuBoard() {
        board = new char[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        isBlackTurn = true; // 黑方先行
        gameState = GameState.PLAYING;
    }
    
    /**
     * 初始化棋盘
     */
    public void initializeBoard() {
        // 初始化空棋盘
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                board[row][col] = ' ';
            }
        }
    }
    
    /**
     * 获取指定位置的棋子
     */
    public char getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return ' ';
    }
    
    /**
     * 设置指定位置的棋子
     */
    public void setPiece(int row, int col, char piece) {
        if (isValidPosition(row, col)) {
            board[row][col] = piece;
        }
    }
    
    /**
     * 落子
     * @return 落子是否成功
     */
    public boolean placePiece(int row, int col) {
        // 检查位置是否在棋盘范围内
        if (!isValidPosition(row, col)) {
            return false;
        }
        
        // 检查位置是否已有棋子
        if (board[row][col] != ' ') {
            return false;
        }
        
        // 放置棋子
        board[row][col] = isBlackTurn ? BLACK : WHITE;
        
        // 记录最后一步棋的位置
        lastMoveRow = row;
        lastMoveCol = col;
        
        // 检查是否获胜
        updateGameState(row, col);
        
        // 切换回合
        if (gameState == GameState.PLAYING) {
            isBlackTurn = !isBlackTurn;
        }
        
        return true;
    }
    
    /**
     * 检查位置是否在棋盘范围内
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
    
    /**
     * 更新游戏状态
     * 检查是否有一方获胜
     */
    private void updateGameState(int row, int col) {
        char currentPiece = board[row][col];
        
        // 检查横向
        if (checkLine(row, col, 0, 1, currentPiece)) {
            gameState = currentPiece == BLACK ? GameState.BLACK_WINS : GameState.RED_WINS;
            return;
        }
        
        // 检查纵向
        if (checkLine(row, col, 1, 0, currentPiece)) {
            gameState = currentPiece == BLACK ? GameState.BLACK_WINS : GameState.RED_WINS;
            return;
        }
        
        // 检查左上到右下对角线
        if (checkLine(row, col, 1, 1, currentPiece)) {
            gameState = currentPiece == BLACK ? GameState.BLACK_WINS : GameState.RED_WINS;
            return;
        }
        
        // 检查右上到左下对角线
        if (checkLine(row, col, 1, -1, currentPiece)) {
            gameState = currentPiece == BLACK ? GameState.BLACK_WINS : GameState.RED_WINS;
            return;
        }
        
        // 检查是否和棋（棋盘已满）
        boolean isFull = true;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] == ' ') {
                    isFull = false;
                    break;
                }
            }
            if (!isFull) break;
        }
        
        if (isFull) {
            gameState = GameState.DRAW;
        }
    }
    
    /**
     * 检查一条线上是否有五子连珠
     * @param row 起始行
     * @param col 起始列
     * @param rowDelta 行方向增量
     * @param colDelta 列方向增量
     * @param piece 棋子类型
     * @return 是否有五子连珠
     */
    private boolean checkLine(int row, int col, int rowDelta, int colDelta, char piece) {
        // 计算连续棋子数量
        int count = 1; // 当前位置已有一个棋子
        
        // 向一个方向检查
        int r = row + rowDelta;
        int c = col + colDelta;
        while (isValidPosition(r, c) && board[r][c] == piece) {
            count++;
            r += rowDelta;
            c += colDelta;
        }
        
        // 向相反方向检查
        r = row - rowDelta;
        c = col - colDelta;
        while (isValidPosition(r, c) && board[r][c] == piece) {
            count++;
            r -= rowDelta;
            c -= colDelta;
        }
        
        // 五子连珠
        return count >= 5;
    }
    
    /**
     * 获取当前回合方
     */
    public boolean isBlackTurn() {
        return isBlackTurn;
    }
    
    /**
     * 获取游戏状态
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * 获取最后一步棋的行
     */
    public int getLastMoveRow() {
        return lastMoveRow;
    }
    
    /**
     * 获取最后一步棋的列
     */
    public int getLastMoveCol() {
        return lastMoveCol;
    }
    
    /**
     * 移除指定位置的棋子（用于悔棋功能）
     */
    public void removePiece(int row, int col) {
        if (isValidPosition(row, col)) {
            board[row][col] = ' ';
            // 重置游戏状态为进行中（如果游戏已结束）
            if (gameState != GameState.PLAYING) {
                gameState = GameState.PLAYING;
            }
            // 重置最后一步棋的位置
            lastMoveRow = -1;
            lastMoveCol = -1;
        }
    }
    
    /**
     * 切换回合（用于悔棋功能）
     */
    public void switchTurn() {
        isBlackTurn = !isBlackTurn;
    }
    
    /**
     * 获取棋盘的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                sb.append(board[row][col]).append(' ');
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}