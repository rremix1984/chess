package com.example.chinesechess.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 国际象棋棋盘类
 * 负责管理棋盘状态和规则
 */
public class InternationalChessBoard {

    // 棋盘大小常量
    public static final int BOARD_SIZE = 8;
    
    // 棋子类型常量
    public static final char PAWN = 'P';    // 兵
    public static final char ROOK = 'R';     // 车
    public static final char KNIGHT = 'N';   // 马
    public static final char BISHOP = 'B';   // 象
    public static final char QUEEN = 'Q';    // 后
    public static final char KING = 'K';     // 王
    
    // 棋子颜色常量
    public static final char WHITE = 'W';
    public static final char BLACK = 'B';
    
    // 棋盘数组，存储棋子
    private String[][] board;
    
    // 当前回合
    private boolean isWhiteTurn;
    
    // 游戏状态
    private GameState gameState;
    
    /**
     * 构造函数
     */
    public InternationalChessBoard() {
        board = new String[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        isWhiteTurn = true; // 白方先行
        gameState = GameState.PLAYING;
    }
    
    /**
     * 初始化棋盘
     */
    public void initializeBoard() {
        // 初始化空棋盘
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                board[row][col] = null;
            }
        }
        
        // 放置黑方棋子（上方）
        board[0][0] = BLACK + "" + ROOK;   // 车
        board[0][1] = BLACK + "" + KNIGHT; // 马
        board[0][2] = BLACK + "" + BISHOP; // 象
        board[0][3] = BLACK + "" + QUEEN;  // 后
        board[0][4] = BLACK + "" + KING;   // 王
        board[0][5] = BLACK + "" + BISHOP; // 象
        board[0][6] = BLACK + "" + KNIGHT; // 马
        board[0][7] = BLACK + "" + ROOK;   // 车
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = BLACK + "" + PAWN; // 兵
        }
        
        // 放置白方棋子（下方）
        board[7][0] = WHITE + "" + ROOK;   // 车
        board[7][1] = WHITE + "" + KNIGHT; // 马
        board[7][2] = WHITE + "" + BISHOP; // 象
        board[7][3] = WHITE + "" + QUEEN;  // 后
        board[7][4] = WHITE + "" + KING;   // 王
        board[7][5] = WHITE + "" + BISHOP; // 象
        board[7][6] = WHITE + "" + KNIGHT; // 马
        board[7][7] = WHITE + "" + ROOK;   // 车
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[6][i] = WHITE + "" + PAWN; // 兵
        }
    }
    
    /**
     * 获取指定位置的棋子
     */
    public String getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }
    
    /**
     * 设置指定位置的棋子
     */
    public void setPiece(int row, int col, String piece) {
        if (isValidPosition(row, col)) {
            board[row][col] = piece;
        }
    }
    
    /**
     * 移动棋子
     * @return 移动是否成功
     */
    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        return movePiece(fromRow, fromCol, toRow, toCol, QUEEN); // 默认升变为后
    }
    
    /**
     * 移动棋子（支持兵的升变）
     * @param promotionPiece 兵升变的目标棋子类型
     * @return 移动是否成功
     */
    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol, char promotionPiece) {
        // 检查起始位置是否有棋子
        String piece = getPiece(fromRow, fromCol);
        if (piece == null) {
            return false;
        }
        
        // 检查是否是当前回合方的棋子
        char pieceColor = piece.charAt(0);
        if ((isWhiteTurn && pieceColor != WHITE) || (!isWhiteTurn && pieceColor != BLACK)) {
            return false;
        }
        
        // 检查移动是否合法
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        
        // 重置吃过路兵标记
        lastMoveWasPawnDoubleStep = false;
        
        // 检查是否是兵走了两格（用于吃过路兵规则）
        if (piece.charAt(1) == PAWN) {
            int startRow = (pieceColor == WHITE) ? 6 : 1;
            if (fromRow == startRow && Math.abs(fromRow - toRow) == 2) {
                lastMoveWasPawnDoubleStep = true;
                enPassantCol = toCol;
                enPassantRow = (pieceColor == WHITE) ? toRow + 1 : toRow - 1;
            }
            
            // 处理吃过路兵
            if (Math.abs(fromCol - toCol) == 1 && getPiece(toRow, toCol) == null && 
                    toCol == enPassantCol && fromRow == enPassantRow) {
                // 移除被吃的过路兵
                int capturedPawnRow = (pieceColor == WHITE) ? toRow + 1 : toRow - 1;
                setPiece(capturedPawnRow, toCol, null);
            }
        }
        
        // 检查是否是王车易位
        boolean isCastling = false;
        if (piece.charAt(1) == KING && Math.abs(fromCol - toCol) == 2) {
            isCastling = true;
            int rookFromCol = (toCol > fromCol) ? 7 : 0; // 右侧车或左侧车
            int rookToCol = (toCol > fromCol) ? 5 : 3;  // 车的目标位置
            
            // 移动车
            String rookPiece = getPiece(fromRow, rookFromCol);
            setPiece(fromRow, rookToCol, rookPiece);
            setPiece(fromRow, rookFromCol, null);
        }
        
        // 更新王和车的移动状态
        if (piece.charAt(1) == KING) {
            if (pieceColor == WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        } else if (piece.charAt(1) == ROOK) {
            if (pieceColor == WHITE) {
                if (fromCol == 0) whiteRookLeftMoved = true;
                if (fromCol == 7) whiteRookRightMoved = true;
            } else {
                if (fromCol == 0) blackRookLeftMoved = true;
                if (fromCol == 7) blackRookRightMoved = true;
            }
        }
        
        // 执行移动
        String capturedPiece = getPiece(toRow, toCol);
        setPiece(toRow, toCol, piece);
        setPiece(fromRow, fromCol, null);
        
        // 处理兵的升变
        if (piece.charAt(1) == PAWN) {
            int promotionRow = (pieceColor == WHITE) ? 0 : 7; // 白兵到达第0行，黑兵到达第7行
            if (toRow == promotionRow) {
                // 升变为指定的棋子（不能升变为王）
                if (promotionPiece != KING) {
                    String promotedPiece = "" + pieceColor + promotionPiece;
                    setPiece(toRow, toCol, promotedPiece);
                }
            }
        }
        
        // 检查是否将军或将杀
        updateGameState();
        
        // 切换回合
        isWhiteTurn = !isWhiteTurn;
        
        return true;
    }
    
    /**
     * 检查移动是否合法
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // 检查位置是否在棋盘范围内
        if (!isValidPosition(fromRow, fromCol) || !isValidPosition(toRow, toCol)) {
            return false;
        }
        
        // 获取起始位置的棋子
        String piece = getPiece(fromRow, fromCol);
        if (piece == null) {
            return false;
        }
        
        // 检查目标位置是否是己方棋子
        String targetPiece = getPiece(toRow, toCol);
        if (targetPiece != null && targetPiece.charAt(0) == piece.charAt(0)) {
            return false;
        }
        
        // 根据棋子类型检查移动是否合法
        char pieceType = piece.charAt(1);
        switch (pieceType) {
            case PAWN:
                return isValidPawnMove(fromRow, fromCol, toRow, toCol, piece.charAt(0));
            case ROOK:
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case KNIGHT:
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case BISHOP:
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case QUEEN:
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case KING:
                return isValidKingMove(fromRow, fromCol, toRow, toCol);
            default:
                return false;
        }
    }
    
    // 记录上一次移动的兵是否前进了两格（用于吃过路兵规则）
    private boolean lastMoveWasPawnDoubleStep = false;
    private int enPassantCol = -1; // 可以被吃过路兵吃掉的兵所在的列
    private int enPassantRow = -1; // 可以被吃过路兵吃掉的兵所在的行
    
    /**
     * 检查兵的移动是否合法
     */
    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol, char color) {
        int direction = (color == WHITE) ? -1 : 1; // 白兵向上移动，黑兵向下移动
        int startRow = (color == WHITE) ? 6 : 1;   // 白兵起始行是第6行，黑兵起始行是第1行
        
        // 前进一格
        if (fromCol == toCol && toRow == fromRow + direction && getPiece(toRow, toCol) == null) {
            return true;
        }
        
        // 起始位置可以前进两格
        if (fromCol == toCol && fromRow == startRow && toRow == fromRow + 2 * direction 
                && getPiece(fromRow + direction, fromCol) == null && getPiece(toRow, toCol) == null) {
            return true;
        }
        
        // 斜向吃子
        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction && getPiece(toRow, toCol) != null 
                && getPiece(toRow, toCol).charAt(0) != color) {
            return true;
        }
        
        // 吃过路兵规则
        if (lastMoveWasPawnDoubleStep && Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction 
                && toCol == enPassantCol && fromRow == enPassantRow) {
            return true;
        }
        
        // TODO: 实现特殊规则：升变
        
        return false;
    }
    
    /**
     * 检查车的移动是否合法
     */
    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        // 车只能直线移动（横向或纵向）
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }
        
        // 检查路径上是否有其他棋子
        if (fromRow == toRow) { // 横向移动
            int step = (fromCol < toCol) ? 1 : -1;
            for (int col = fromCol + step; col != toCol; col += step) {
                if (getPiece(fromRow, col) != null) {
                    return false;
                }
            }
        } else { // 纵向移动
            int step = (fromRow < toRow) ? 1 : -1;
            for (int row = fromRow + step; row != toRow; row += step) {
                if (getPiece(row, fromCol) != null) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 检查马的移动是否合法
     */
    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        // 马走"日"字，即一格直线加一格斜线
        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);
        
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    
    /**
     * 检查象的移动是否合法
     */
    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        // 象只能沿对角线移动
        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);
        
        if (rowDiff != colDiff) {
            return false;
        }
        
        // 检查路径上是否有其他棋子
        int rowStep = (toRow > fromRow) ? 1 : -1;
        int colStep = (toCol > fromCol) ? 1 : -1;
        
        int row = fromRow + rowStep;
        int col = fromCol + colStep;
        while (row != toRow && col != toCol) {
            if (getPiece(row, col) != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        
        return true;
    }
    
    /**
     * 检查后的移动是否合法
     */
    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        // 后的移动结合了车和象的移动规则
        return isValidRookMove(fromRow, fromCol, toRow, toCol) || 
               isValidBishopMove(fromRow, fromCol, toRow, toCol);
    }
    
    // 记录王和车是否移动过（用于王车易位规则）
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookLeftMoved = false;
    private boolean whiteRookRightMoved = false;
    private boolean blackRookLeftMoved = false;
    private boolean blackRookRightMoved = false;
    
    /**
     * 检查王的移动是否合法
     */
    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        // 获取王的颜色
        String piece = getPiece(fromRow, fromCol);
        char color = piece.charAt(0);
        
        // 王只能移动一格（横向、纵向或对角线）
        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);
        
        // 普通移动
        if (rowDiff <= 1 && colDiff <= 1 && (rowDiff != 0 || colDiff != 0)) {
            return true;
        }
        
        // 王车易位规则
        // 检查是否是王车易位
        if (rowDiff == 0 && colDiff == 2 && fromRow == (color == WHITE ? 7 : 0) && fromCol == 4) {
            // 检查王是否移动过
            if ((color == WHITE && whiteKingMoved) || (color == BLACK && blackKingMoved)) {
                return false;
            }
            
            // 短易位（王向右移动两格）
            if (toCol == 6) {
                // 检查右侧车是否移动过
                if ((color == WHITE && whiteRookRightMoved) || (color == BLACK && blackRookRightMoved)) {
                    return false;
                }
                
                // 检查王和车之间的格子是否为空
                if (getPiece(fromRow, 5) != null || getPiece(fromRow, 6) != null) {
                    return false;
                }
                
                // 检查王经过的格子是否被攻击
                if (isSquareAttacked(fromRow, 4, color) || isSquareAttacked(fromRow, 5, color) || isSquareAttacked(fromRow, 6, color)) {
                    return false;
                }
                
                return true;
            }
            // 长易位（王向左移动两格）
            else if (toCol == 2) {
                // 检查左侧车是否移动过
                if ((color == WHITE && whiteRookLeftMoved) || (color == BLACK && blackRookLeftMoved)) {
                    return false;
                }
                
                // 检查王和车之间的格子是否为空
                if (getPiece(fromRow, 1) != null || getPiece(fromRow, 2) != null || getPiece(fromRow, 3) != null) {
                    return false;
                }
                
                // 检查王经过的格子是否被攻击
                if (isSquareAttacked(fromRow, 2, color) || isSquareAttacked(fromRow, 3, color) || isSquareAttacked(fromRow, 4, color)) {
                    return false;
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查指定格子是否被对方攻击
     */
    private boolean isSquareAttacked(int row, int col, char defendingColor) {
        char attackingColor = (defendingColor == WHITE) ? BLACK : WHITE;
        
        // 检查所有对方棋子是否可以攻击到该格子
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                String piece = getPiece(r, c);
                if (piece != null && piece.charAt(0) == attackingColor) {
                    // 检查该棋子是否可以移动到目标格子
                    // 注意：这里不能直接调用isValidMove，因为会导致递归调用
                    char pieceType = piece.charAt(1);
                    boolean canAttack = false;
                    
                    switch (pieceType) {
                        case PAWN:
                            // 兵只能斜向前方攻击
                            int direction = (attackingColor == WHITE) ? -1 : 1;
                            canAttack = (row == r + direction && Math.abs(col - c) == 1);
                            break;
                        case ROOK:
                            canAttack = isValidRookMove(r, c, row, col);
                            break;
                        case KNIGHT:
                            canAttack = isValidKnightMove(r, c, row, col);
                            break;
                        case BISHOP:
                            canAttack = isValidBishopMove(r, c, row, col);
                            break;
                        case QUEEN:
                            canAttack = isValidQueenMove(r, c, row, col);
                            break;
                        case KING:
                            // 王只能攻击相邻的格子
                            int rowDiff = Math.abs(row - r);
                            int colDiff = Math.abs(col - c);
                            canAttack = rowDiff <= 1 && colDiff <= 1 && (rowDiff != 0 || colDiff != 0);
                            break;
                    }
                    
                    if (canAttack) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查位置是否在棋盘范围内
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
    
    /**
     * 更新游戏状态
     * 检查王是否被吃掉，以及其他游戏结束条件
     */
    private void updateGameState() {
        // 首先检查王是否被吃掉
        boolean whiteKingExists = false;
        boolean blackKingExists = false;
        
        // 遍历棋盘查找王
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = board[row][col];
                if (piece != null && piece.charAt(1) == KING) {
                    if (piece.charAt(0) == WHITE) {
                        whiteKingExists = true;
                    } else if (piece.charAt(0) == BLACK) {
                        blackKingExists = true;
                    }
                }
            }
        }
        
        // 如果有一方的王不存在，游戏结束
        if (!whiteKingExists) {
            gameState = GameState.BLACK_WINS; // 白王被吃，黑方获胜
            return;
        }
        if (!blackKingExists) {
            gameState = GameState.RED_WINS; // 黑王被吃，白方获胜
            return;
        }
        
        // TODO: 实现更复杂的将军、将杀和和棋的逻辑
        // 暂时保持游戏状态为PLAYING
        gameState = GameState.PLAYING;
    }
    
    /**
     * 获取当前回合方
     */
    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }
    
    /**
     * 获取游戏状态
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * 获取棋盘的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = board[row][col];
                if (piece == null) {
                    sb.append(".. ");
                } else {
                    sb.append(piece).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}