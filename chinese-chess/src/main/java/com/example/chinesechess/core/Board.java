package com.example.chinesechess.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Board {
    private final Piece[][] pieces = new Piece[10][9];
    
    // 局面历史记录，用于检测重复局面
    private final List<String> positionHistory = new ArrayList<>();
    
    // 重复局面阈值（三次重复判和）
    private static final int REPETITION_THRESHOLD = 3;

    public Board() {
        initializeBoard();
    }

    public Piece getPiece(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 9) {
            return null;
        }
        return pieces[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (row >= 0 && row < 10 && col >= 0 && col < 9) {
            pieces[row][col] = piece;
        }
    }
    
    /**
     * 移除指定位置的棋子
     * @param row 行
     * @param col 列
     */
    public void removePiece(int row, int col) {
        if (row >= 0 && row < 10 && col >= 0 && col < 9) {
            pieces[row][col] = null;
        }
    }

    public void movePiece(Position start, Position end) {
        Piece piece = getPiece(start.getX(), start.getY());
        setPiece(end.getX(), end.getY(), piece);
        setPiece(start.getX(), start.getY(), null);
    }

    public void makeMove(Move move) {
        movePiece(move.getStart(), move.getEnd());
        // 移动后记录当前局面
        recordCurrentPosition();
    }
    
    /**
     * 记录当前局面到历史记录中
     */
    public void recordCurrentPosition() {
        String currentFen = generateSimpleFen();
        positionHistory.add(currentFen);
        
        // 限制历史记录长度，避免内存过度使用
        if (positionHistory.size() > 100) {
            positionHistory.remove(0);
        }
    }
    
    /**
     * 生成简化的FEN字符串（只包含棋盘状态）
     * @return 简化的FEN字符串
     */
    private String generateSimpleFen() {
        StringBuilder fen = new StringBuilder();
        
        for (int row = 0; row < 10; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 9; col++) {
                Piece piece = getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(getFenChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 9) {
                fen.append('/');
            }
        }
        
        return fen.toString();
    }
    
    /**
     * 获取棋子的FEN字符表示
     * @param piece 棋子
     * @return FEN字符
     */
    private char getFenChar(Piece piece) {
        char baseChar;
        if (piece instanceof General) baseChar = 'k';
        else if (piece instanceof Advisor) baseChar = 'a';
        else if (piece instanceof Elephant) baseChar = 'b';
        else if (piece instanceof Horse) baseChar = 'n';
        else if (piece instanceof Chariot) baseChar = 'r';
        else if (piece instanceof Cannon) baseChar = 'c';
        else if (piece instanceof Soldier) baseChar = 'p';
        else baseChar = '?';
        
        // 红方棋子用大写，黑方棋子用小写
        return piece.getColor() == PieceColor.RED ? Character.toUpperCase(baseChar) : baseChar;
    }
    
    /**
     * 检查是否存在重复局面
     * @return true如果存在重复局面，false否则
     */
    public boolean hasRepetition() {
        if (positionHistory.size() < REPETITION_THRESHOLD) {
            return false;
        }
        
        String currentPosition = generateSimpleFen();
        int count = Collections.frequency(positionHistory, currentPosition);
        
        // 如果当前局面已经出现了REPETITION_THRESHOLD次，则判定为重复
        return count >= REPETITION_THRESHOLD;
    }
    
    /**
     * 清空局面历史记录
     */
    public void clearPositionHistory() {
        positionHistory.clear();
    }
    
    /**
     * 获取局面历史记录的副本
     * @return 局面历史记录
     */
    public List<String> getPositionHistory() {
        return new ArrayList<>(positionHistory);
    }

    @Override
    public Board clone() {
        Board newBoard = new Board();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                if (this.pieces[i][j] != null) {
                    newBoard.pieces[i][j] = this.pieces[i][j].clone();
                }
            }
        }
        return newBoard;
    }

    public void initializeBoard() {
        // 首先清空整个棋盘
        clearBoard();
        
        // 清空局面历史记录
        clearPositionHistory();
        
        // Black pieces
        setPiece(0, 0, new Chariot(PieceColor.BLACK));
        setPiece(0, 1, new Horse(PieceColor.BLACK));
        setPiece(0, 2, new Elephant(PieceColor.BLACK));
        setPiece(0, 3, new Advisor(PieceColor.BLACK));
        setPiece(0, 4, new General(PieceColor.BLACK));
        setPiece(0, 5, new Advisor(PieceColor.BLACK));
        setPiece(0, 6, new Elephant(PieceColor.BLACK));
        setPiece(0, 7, new Horse(PieceColor.BLACK));
        setPiece(0, 8, new Chariot(PieceColor.BLACK));
        setPiece(2, 1, new Cannon(PieceColor.BLACK));
        setPiece(2, 7, new Cannon(PieceColor.BLACK));
        for (int i = 0; i < 9; i += 2) {
            setPiece(3, i, new Soldier(PieceColor.BLACK));
        }

        // Red pieces
        setPiece(9, 0, new Chariot(PieceColor.RED));
        setPiece(9, 1, new Horse(PieceColor.RED));
        setPiece(9, 2, new Elephant(PieceColor.RED));
        setPiece(9, 3, new Advisor(PieceColor.RED));
        setPiece(9, 4, new General(PieceColor.RED));
        setPiece(9, 5, new Advisor(PieceColor.RED));
        setPiece(9, 6, new Elephant(PieceColor.RED));
        setPiece(9, 7, new Horse(PieceColor.RED));
        setPiece(9, 8, new Chariot(PieceColor.RED));
        setPiece(7, 1, new Cannon(PieceColor.RED));
        setPiece(7, 7, new Cannon(PieceColor.RED));
        for (int i = 0; i < 9; i += 2) {
            setPiece(6, i, new Soldier(PieceColor.RED));
        }
    }
    
    /**
     * 清空整个棋盘
     */
    public void clearBoard() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                pieces[row][col] = null;
            }
        }
    }

    public void printBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                Piece piece = pieces[i][j];
                if (piece == null) {
                    System.out.print("口 ");
                } else {
                    System.out.print(piece.getChineseName() + " ");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * 查找指定颜色的将军位置
     * @param color 棋子颜色
     * @return 将军的位置，如果没找到返回null
     */
    public Position findGeneral(PieceColor color) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = getPiece(row, col);
                if (piece instanceof General && piece.getColor() == color) {
                    return new Position(row, col);
                }
            }
        }
        return null; // 将军不存在，游戏应该结束
    }
    
    /**
     * 检查指定颜色的将军是否被将军
     * @param color 要检查的将军颜色
     * @return true如果被将军，false否则
     */
    public boolean isInCheck(PieceColor color) {
        Position generalPos = findGeneral(color);
        if (generalPos == null) {
            return false; // 将军不存在
        }
        
        // 检查对方所有棋子是否能攻击到将军
        PieceColor opponentColor = (color == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = getPiece(row, col);
                if (piece != null && piece.getColor() == opponentColor) {
                    Position start = new Position(row, col);
                    if (piece.isValidMove(this, start, generalPos)) {
                        return true; // 找到能攻击将军的棋子
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 检查游戏是否结束
     * @param currentPlayerColor 当前玩家颜色
     * @return 游戏状态
     */
    public GameState checkGameState(PieceColor currentPlayerColor) {
        // 首先检查将军是否还存在
        Position redGeneral = findGeneral(PieceColor.RED);
        Position blackGeneral = findGeneral(PieceColor.BLACK);
        
        if (redGeneral == null) {
            return GameState.BLACK_WINS; // 红方将军被吃，黑方获胜
        }
        if (blackGeneral == null) {
            return GameState.RED_WINS; // 黑方将军被吃，红方获胜
        }
        
        // 检查是否存在重复局面（三次重复判和）
        if (hasRepetition()) {
            System.out.println("🔄 检测到重复局面，游戏判和");
            return GameState.DRAW;
        }
        
        // 检查当前玩家是否被将军
        boolean inCheck = isInCheck(currentPlayerColor);
        
        // 检查当前玩家是否有合法移动
        boolean hasValidMoves = hasValidMoves(currentPlayerColor);
        
        if (!hasValidMoves) {
            if (inCheck) {
                // 被将军且无法移动 = 将死
                return (currentPlayerColor == PieceColor.RED) ? GameState.BLACK_WINS : GameState.RED_WINS;
            } else {
                // 无法移动但未被将军 = 困毙（和棋）
                return GameState.DRAW;
            }
        }
        
        if (inCheck) {
            return GameState.IN_CHECK; // 被将军但还有合法移动
        }
        
        return GameState.PLAYING; // 游戏继续
    }
    
    /**
     * 检查指定颜色的玩家是否有合法移动
     * @param color 玩家颜色
     * @return true如果有合法移动，false否则
     */
    private boolean hasValidMoves(PieceColor color) {
        for (int startRow = 0; startRow < 10; startRow++) {
            for (int startCol = 0; startCol < 9; startCol++) {
                Piece piece = getPiece(startRow, startCol);
                if (piece != null && piece.getColor() == color) {
                    Position start = new Position(startRow, startCol);
                    
                    // 检查这个棋子是否有任何合法移动
                    for (int endRow = 0; endRow < 10; endRow++) {
                        for (int endCol = 0; endCol < 9; endCol++) {
                            Position end = new Position(endRow, endCol);
                            if (piece.isValidMove(this, start, end)) {
                                // 模拟移动，检查是否会导致己方将军被将军
                                if (isMoveSafe(start, end, color)) {
                                    return true; // 找到至少一个安全的合法移动
                                }
                            }
                        }
                    }
                }
            }
        }
        return false; // 没有找到任何合法移动
    }
    
    /**
     * 检查移动是否安全（不会导致己方将军被将军）
     * @param start 起始位置
     * @param end 结束位置
     * @param playerColor 玩家颜色
     * @return true如果移动安全，false否则
     */
    public boolean isMoveSafe(Position start, Position end, PieceColor playerColor) {
        // 保存原始状态
        Piece movingPiece = getPiece(start.getX(), start.getY());
        Piece capturedPiece = getPiece(end.getX(), end.getY());
        
        // 执行临时移动
        setPiece(end.getX(), end.getY(), movingPiece);
        setPiece(start.getX(), start.getY(), null);
        
        // 检查移动后是否被将军
        boolean safe = !isInCheck(playerColor);
        
        // 恢复原始状态
        setPiece(start.getX(), start.getY(), movingPiece);
        setPiece(end.getX(), end.getY(), capturedPiece);
        
        return safe;
    }

    public Piece[][] getPieces() {
        Piece[][] copy = new Piece[10][9];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                if (pieces[i][j] != null) {
                    copy[i][j] = pieces[i][j].clone();
                }
            }
        }
        return copy;
    }

    public void setPieces(Piece[][] pieces) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                if (pieces[i][j] != null) {
                    this.pieces[i][j] = pieces[i][j].clone();
                } else {
                    this.pieces[i][j] = null;
                }
            }
        }
    }
}