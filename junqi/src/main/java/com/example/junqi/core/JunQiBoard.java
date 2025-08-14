package com.example.junqi.core;

import java.util.*;

/**
 * 军棋棋盘类
 * 5x6的棋盘，上半部分是红方，下半部分是黑方
 */
public class JunQiBoard {
    public static final int BOARD_WIDTH = 5;   // 棋盘宽度
    public static final int BOARD_HEIGHT = 6;  // 棋盘高度
    
    private final JunQiPiece[][] board;
    private boolean isRedTurn;                  // 当前是否红方回合
    private GameState gameState;
    private int moveCount;                      // 移动步数
    private List<Move> moveHistory;             // 移动历史
    
    public JunQiBoard() {
        this.board = new JunQiPiece[BOARD_HEIGHT][BOARD_WIDTH];
        this.isRedTurn = true; // 红方先手
        this.gameState = GameState.PLAYING;
        this.moveCount = 0;
        this.moveHistory = new ArrayList<>();
        initializeBoard();
    }
    
    /**
     * 初始化棋盘
     */
    private void initializeBoard() {
        // 清空棋盘
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = null;
            }
        }
        
        // 创建红方棋子
        List<JunQiPiece> redPieces = createPieces(true);
        
        // 创建黑方棋子
        List<JunQiPiece> blackPieces = createPieces(false);
        
        // 随机放置红方棋子（上半部分，行0-2）
        Collections.shuffle(redPieces);
        int pieceIndex = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (pieceIndex < redPieces.size()) {
                    board[row][col] = redPieces.get(pieceIndex++);
                }
            }
        }
        
        // 随机放置黑方棋子（下半部分，行3-5）
        Collections.shuffle(blackPieces);
        pieceIndex = 0;
        for (int row = 3; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (pieceIndex < blackPieces.size()) {
                    board[row][col] = blackPieces.get(pieceIndex++);
                }
            }
        }
    }
    
    /**
     * 创建一方的所有棋子
     */
    private List<JunQiPiece> createPieces(boolean isRed) {
        List<JunQiPiece> pieces = new ArrayList<>();
        
        for (PieceType type : PieceType.values()) {
            int count = type.getCount();
            for (int i = 0; i < count; i++) {
                pieces.add(new JunQiPiece(type, isRed));
            }
        }
        
        return pieces;
    }
    
    /**
     * 获取指定位置的棋子
     */
    public JunQiPiece getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }
    
    /**
     * 判断位置是否有效
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH;
    }
    
    /**
     * 判断位置是否为空
     */
    public boolean isEmpty(int row, int col) {
        return getPiece(row, col) == null;
    }
    
    /**
     * 获取可移动的位置列表
     */
    public List<int[]> getValidMoves(int fromRow, int fromCol) {
        List<int[]> validMoves = new ArrayList<>();
        JunQiPiece piece = getPiece(fromRow, fromCol);
        
        if (piece == null || !piece.isAlive() || !piece.getType().canMove()) {
            return validMoves;
        }
        
        // 检查当前回合
        if ((isRedTurn && !piece.isRed()) || (!isRedTurn && piece.isRed())) {
            return validMoves;
        }
        
        // 四个方向：上、下、左、右
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newRow = fromRow + dir[0];
            int newCol = fromCol + dir[1];
            
            if (isValidPosition(newRow, newCol)) {
                JunQiPiece targetPiece = getPiece(newRow, newCol);
                
                if (targetPiece == null) {
                    // 空位可以移动
                    validMoves.add(new int[]{newRow, newCol});
                } else if (targetPiece.isRed() != piece.isRed() && targetPiece.isAlive()) {
                    // 可以攻击对方棋子
                    validMoves.add(new int[]{newRow, newCol});
                }
            }
        }
        
        return validMoves;
    }
    
    /**
     * 执行移动
     */
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        
        JunQiPiece movingPiece = getPiece(fromRow, fromCol);
        JunQiPiece targetPiece = getPiece(toRow, toCol);
        
        Move move = new Move(fromRow, fromCol, toRow, toCol, movingPiece, targetPiece);
        
        if (targetPiece != null) {
            // 攻击
            JunQiPiece.AttackResult result = movingPiece.attack(targetPiece);
            move.setAttackResult(result);
            
            // 翻棋：攻击时双方棋子都变为可见
            movingPiece.setVisible(true);
            targetPiece.setVisible(true);
            
            switch (result) {
                case WIN:
                    board[toRow][toCol] = movingPiece;
                    board[fromRow][fromCol] = null;
                    break;
                case LOSE:
                    board[fromRow][fromCol] = null;
                    break;
                case DRAW:
                    board[fromRow][fromCol] = null;
                    board[toRow][toCol] = null;
                    break;
                default:
                    return false;
            }
        } else {
            // 普通移动
            board[toRow][toCol] = movingPiece;
            board[fromRow][fromCol] = null;
        }
        
        // 记录移动
        moveHistory.add(move);
        moveCount++;
        
        // 切换回合
        isRedTurn = !isRedTurn;
        
        // 检查游戏状态
        checkGameState();
        
        return true;
    }
    
    /**
     * 翻棋（点击暗棋使其可见）
     */
    public boolean flipPiece(int row, int col) {
        JunQiPiece piece = getPiece(row, col);
        
        if (piece == null || piece.isVisible() || !piece.isAlive()) {
            return false;
        }
        
        // 翻棋必须轮到对应玩家
        if ((isRedTurn && !piece.isRed()) || (!isRedTurn && piece.isRed())) {
            return false;
        }
        
        piece.setVisible(true);
        
        // 翻棋后切换回合
        isRedTurn = !isRedTurn;
        moveCount++;
        
        return true;
    }
    
    /**
     * 验证移动是否有效
     */
    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidPosition(fromRow, fromCol) || !isValidPosition(toRow, toCol)) {
            return false;
        }
        
        if (fromRow == toRow && fromCol == toCol) {
            return false; // 不能原地不动
        }
        
        JunQiPiece piece = getPiece(fromRow, fromCol);
        if (piece == null || !piece.isAlive() || !piece.getType().canMove()) {
            return false;
        }
        
        // 检查是否轮到这个玩家
        if ((isRedTurn && !piece.isRed()) || (!isRedTurn && piece.isRed())) {
            return false;
        }
        
        // 只能移动到相邻位置
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (rowDiff + colDiff != 1) {
            return false; // 必须是相邻位置
        }
        
        JunQiPiece targetPiece = getPiece(toRow, toCol);
        if (targetPiece != null) {
            // 目标位置有棋子，检查是否可以攻击
            return piece.canAttack(targetPiece);
        }
        
        return true; // 移动到空位
    }
    
    /**
     * 检查游戏状态
     */
    private void checkGameState() {
        boolean redHasFlag = false;
        boolean blackHasFlag = false;
        boolean redHasMovablePieces = false;
        boolean blackHasMovablePieces = false;
        
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                JunQiPiece piece = getPiece(row, col);
                if (piece != null && piece.isAlive()) {
                    if (piece.getType() == PieceType.FLAG) {
                        if (piece.isRed()) {
                            redHasFlag = true;
                        } else {
                            blackHasFlag = true;
                        }
                    }
                    
                    if (piece.getType().canMove()) {
                        if (piece.isRed()) {
                            redHasMovablePieces = true;
                        } else {
                            blackHasMovablePieces = true;
                        }
                    }
                }
            }
        }
        
        // 军旗被吃掉则失败
        if (!redHasFlag) {
            gameState = GameState.BLACK_WINS;
        } else if (!blackHasFlag) {
            gameState = GameState.RED_WINS;
        }
        // 没有可移动棋子则失败
        else if (!redHasMovablePieces) {
            gameState = GameState.BLACK_WINS;
        } else if (!blackHasMovablePieces) {
            gameState = GameState.RED_WINS;
        }
        
        // TODO: 添加更多结束条件（如步数限制等）
    }
    
    /**
     * 重置棋盘
     */
    public void reset() {
        this.isRedTurn = true;
        this.gameState = GameState.PLAYING;
        this.moveCount = 0;
        this.moveHistory.clear();
        initializeBoard();
    }
    
    // Getters
    public boolean isRedTurn() { return isRedTurn; }
    public GameState getGameState() { return gameState; }
    public int getMoveCount() { return moveCount; }
    public List<Move> getMoveHistory() { return new ArrayList<>(moveHistory); }
    public int getBoardWidth() { return BOARD_WIDTH; }
    public int getBoardHeight() { return BOARD_HEIGHT; }
    
    /**
     * 移动记录类
     */
    public static class Move {
        private final int fromRow, fromCol, toRow, toCol;
        private final JunQiPiece movingPiece;
        private final JunQiPiece targetPiece;
        private JunQiPiece.AttackResult attackResult;
        
        public Move(int fromRow, int fromCol, int toRow, int toCol, 
                   JunQiPiece movingPiece, JunQiPiece targetPiece) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.movingPiece = movingPiece;
            this.targetPiece = targetPiece;
        }
        
        // Getters
        public int getFromRow() { return fromRow; }
        public int getFromCol() { return fromCol; }
        public int getToRow() { return toRow; }
        public int getToCol() { return toCol; }
        public JunQiPiece getMovingPiece() { return movingPiece; }
        public JunQiPiece getTargetPiece() { return targetPiece; }
        public JunQiPiece.AttackResult getAttackResult() { return attackResult; }
        public void setAttackResult(JunQiPiece.AttackResult result) { this.attackResult = result; }
        
        @Override
        public String toString() {
            if (targetPiece != null) {
                return String.format("%s 从 (%d,%d) 攻击 (%d,%d) 的 %s，结果：%s",
                    movingPiece.getDisplayName(), fromRow, fromCol, toRow, toCol,
                    targetPiece.getDisplayName(), attackResult);
            } else {
                return String.format("%s 从 (%d,%d) 移动到 (%d,%d)",
                    movingPiece.getDisplayName(), fromRow, fromCol, toRow, toCol);
            }
        }
    }
}
