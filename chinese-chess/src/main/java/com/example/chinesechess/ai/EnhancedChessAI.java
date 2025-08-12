package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强版象棋AI引擎
 * 实现更强的评估函数、更深的搜索和战术识别
 */
public class EnhancedChessAI {
    
    private final PieceColor aiColor;
    private final int maxDepth;
    private final TranspositionTable transTable;
    private final OpeningBook openingBook;
    private final Map<Class<? extends Piece>, Integer> pieceValues;
    private final Map<Class<? extends Piece>, int[][]> positionTables;
    
    // 评估权重
    private static final int MOBILITY_WEIGHT = 10;
    private static final int SAFETY_WEIGHT = 15;
    private static final int CONTROL_WEIGHT = 20;
    private static final int TACTICAL_WEIGHT = 50;
    
    public EnhancedChessAI(PieceColor aiColor, int difficulty) {
        this.aiColor = aiColor;
        this.maxDepth = Math.max(6, Math.min(difficulty * 2, 14)); // 搜索深度6-14层
        this.transTable = new TranspositionTable();
        this.openingBook = new OpeningBook();
        this.pieceValues = initializePieceValues();
        this.positionTables = initializePositionTables();
    }
    
    /**
     * 获取AI的颜色
     */
    public PieceColor getColor() {
        return aiColor;
    }
    
    /**
     * 获取AI的最佳移动
     */
    public Move getBestMove(Board board) {
        System.out.println("🧠 增强AI思考中...");
        long startTime = System.currentTimeMillis();
        
        // 1. 检查开局库
        Move openingMove = openingBook.getOpeningMove(board, aiColor);
        if (openingMove != null) {
            System.out.println("📚 使用开局库走法: " + formatMove(openingMove));
            return openingMove;
        }
        
        // 2. 迭代加深搜索
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (int depth = 1; depth <= maxDepth; depth++) {
            System.out.print("🔍 搜索深度 " + depth + "...");
            
            SearchResult result = alphaBetaSearch(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            if (result.move != null) {
                bestMove = result.move;
                bestScore = result.score;
                System.out.println(" 最佳走法: " + formatMove(bestMove) + " (评分: " + bestScore + ")");
            } else {
                System.out.println(" 无有效走法");
                break;
            }
            
            // 如果找到必胜走法，提前结束
            if (Math.abs(bestScore) > 9000) {
                System.out.println("🎯 发现决定性走法，提前结束搜索");
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("⏱️  思考时间: " + (endTime - startTime) + "ms");
        System.out.println("💡 最终决策: " + formatMove(bestMove) + " (评分: " + bestScore + ")");
        
        return bestMove;
    }
    
    /**
     * Alpha-Beta搜索算法
     */
    private SearchResult alphaBetaSearch(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        long boardHash = getBoardHash(board);
        
        // 查询置换表
        TranspositionEntry entry = transTable.probe(boardHash);
        if (entry != null && entry.depth >= depth) {
            return new SearchResult(entry.move, entry.score);
        }
        
        if (depth == 0) {
            int score = enhancedEvaluateBoard(board);
            return new SearchResult(null, score);
        }
        
        PieceColor currentColor = isMaximizing ? aiColor : getOpponentColor(aiColor);
        List<Move> moves = getAllPossibleMoves(board, currentColor);
        
        if (moves.isEmpty()) {
            // 无法移动，游戏结束
            int score = isMaximizing ? Integer.MIN_VALUE + depth : Integer.MAX_VALUE - depth;
            return new SearchResult(null, score);
        }
        
        // 移动排序优化
        moves = sortMoves(moves, board);
        
        Move bestMove = null;
        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        
        for (Move move : moves) {
            Board tempBoard = copyBoard(board);
            tempBoard.movePiece(move.getStart(), move.getEnd());
            
            SearchResult result = alphaBetaSearch(tempBoard, depth - 1, alpha, beta, !isMaximizing);
            
            if (isMaximizing) {
                if (result.score > bestScore) {
                    bestScore = result.score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                if (result.score < bestScore) {
                    bestScore = result.score;
                    bestMove = move;
                }
                beta = Math.min(beta, bestScore);
            }
            
            if (beta <= alpha) {
                break; // Alpha-Beta剪枝
            }
        }
        
        // 存储到置换表
        transTable.store(boardHash, depth, bestScore, bestMove);
        
        return new SearchResult(bestMove, bestScore);
    }
    
    /**
     * 增强版评估函数
     */
    private int enhancedEvaluateBoard(Board board) {
        int score = 0;
        
        // 1. 基础棋子价值和位置价值
        score += calculatePieceAndPositionValues(board);
        
        // 2. 机动性评估
        score += calculateMobility(board) * MOBILITY_WEIGHT;
        
        // 3. 安全性评估
        score += calculateSafety(board) * SAFETY_WEIGHT;
        
        // 4. 控制力评估
        score += calculateControl(board) * CONTROL_WEIGHT;
        
        // 5. 战术模式识别
        score += recognizeTacticalPatterns(board) * TACTICAL_WEIGHT;
        
        return score;
    }
    
    /**
     * 计算棋子价值和位置价值
     */
    private int calculatePieceAndPositionValues(Board board) {
        int score = 0;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    int pieceValue = pieceValues.getOrDefault(piece.getClass(), 0);
                    int positionValue = getPositionValue(piece, row, col);
                    int totalValue = pieceValue + positionValue;
                    
                    if (piece.getColor() == aiColor) {
                        score += totalValue;
                    } else {
                        score -= totalValue;
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * 计算机动性（可移动步数）
     */
    private int calculateMobility(Board board) {
        int aiMobility = getAllPossibleMoves(board, aiColor).size();
        int opponentMobility = getAllPossibleMoves(board, getOpponentColor(aiColor)).size();
        return aiMobility - opponentMobility;
    }
    
    /**
     * 计算安全性（棋子受保护程度）
     */
    private int calculateSafety(Board board) {
        int score = 0;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    Position pos = new Position(row, col);
                    int attackers = countAttackers(board, pos, getOpponentColor(piece.getColor()));
                    int defenders = countAttackers(board, pos, piece.getColor());
                    
                    int safetyScore = defenders - attackers;
                    if (piece.getColor() == aiColor) {
                        score += safetyScore;
                    } else {
                        score -= safetyScore;
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * 计算控制力（控制关键位置）
     */
    private int calculateControl(Board board) {
        int score = 0;
        
        // 中心控制
        int[] centerRows = {4, 5};
        int[] centerCols = {3, 4, 5};
        
        for (int row : centerRows) {
            for (int col : centerCols) {
                Position pos = new Position(row, col);
                int aiControl = countAttackers(board, pos, aiColor);
                int opponentControl = countAttackers(board, pos, getOpponentColor(aiColor));
                score += (aiControl - opponentControl) * 5;
            }
        }
        
        // 河界控制
        for (int col = 0; col < 9; col++) {
            Position pos1 = new Position(4, col);
            Position pos2 = new Position(5, col);
            
            int aiControl = countAttackers(board, pos1, aiColor) + countAttackers(board, pos2, aiColor);
            int opponentControl = countAttackers(board, pos1, getOpponentColor(aiColor)) + 
                                countAttackers(board, pos2, getOpponentColor(aiColor));
            score += (aiControl - opponentControl) * 3;
        }
        
        return score;
    }
    
    /**
     * 识别战术模式
     */
    private int recognizeTacticalPatterns(Board board) {
        int score = 0;
        
        // 检查将军威胁
        if (isInCheck(board, getOpponentColor(aiColor))) {
            score += 100;
        }
        if (isInCheck(board, aiColor)) {
            score -= 100;
        }
        
        // 检查双重攻击
        score += findForks(board, aiColor) * 50;
        score -= findForks(board, getOpponentColor(aiColor)) * 50;
        
        // 检查牵制
        score += findPins(board, aiColor) * 30;
        score -= findPins(board, getOpponentColor(aiColor)) * 30;
        
        // 检查闪击
        score += findDiscoveredAttacks(board, aiColor) * 40;
        score -= findDiscoveredAttacks(board, getOpponentColor(aiColor)) * 40;
        
        return score;
    }
    
    /**
     * 移动排序优化
     */
    private List<Move> sortMoves(List<Move> moves, Board board) {
        return moves.stream()
                .sorted((m1, m2) -> {
                    int score1 = getMoveScore(m1, board);
                    int score2 = getMoveScore(m2, board);
                    return Integer.compare(score2, score1); // 降序排列
                })
                .collect(ArrayList::new, (list, move) -> list.add(move), ArrayList::addAll);
    }
    
    /**
     * 计算移动分数（用于排序）
     */
    private int getMoveScore(Move move, Board board) {
        int score = 0;
        
        Piece movingPiece = board.getPiece(move.getStart().getX(), move.getStart().getY());
        Piece targetPiece = board.getPiece(move.getEnd().getX(), move.getEnd().getY());
        
        // 吃子优先
        if (targetPiece != null) {
            score += pieceValues.getOrDefault(targetPiece.getClass(), 0);
            score -= pieceValues.getOrDefault(movingPiece.getClass(), 0) / 10; // 避免用大子吃小子
        }
        
        // 将军优先
        Board tempBoard = copyBoard(board);
        tempBoard.movePiece(move.getStart(), move.getEnd());
        if (isInCheck(tempBoard, getOpponentColor(aiColor))) {
            score += 200;
        }
        
        // 中心移动优先
        int endRow = move.getEnd().getX();
        int endCol = move.getEnd().getY();
        if (endRow >= 3 && endRow <= 6 && endCol >= 2 && endCol <= 6) {
            score += 10;
        }
        
        return score;
    }
    
    // 辅助方法实现
    private Map<Class<? extends Piece>, Integer> initializePieceValues() {
        Map<Class<? extends Piece>, Integer> values = new HashMap<>();
        values.put(General.class, 10000);
        values.put(Advisor.class, 200);
        values.put(Elephant.class, 200);
        values.put(Horse.class, 400);
        values.put(Chariot.class, 900);
        values.put(Cannon.class, 450);
        values.put(Soldier.class, 100);
        return values;
    }
    
    private Map<Class<? extends Piece>, int[][]> initializePositionTables() {
        Map<Class<? extends Piece>, int[][]> tables = new HashMap<>();
        
        // 兵的位置价值表
        int[][] soldierTable = {
            {0,  0,  0,  0,  0,  0,  0,  0,  0},
            {0,  0,  0,  0,  0,  0,  0,  0,  0},
            {0,  0,  0,  0,  0,  0,  0,  0,  0},
            {0,  0,  0,  0,  0,  0,  0,  0,  0},
            {0,  0,  0,  0,  0,  0,  0,  0,  0},
            {10, 20, 30, 40, 50, 40, 30, 20, 10},
            {20, 30, 40, 50, 60, 50, 40, 30, 20},
            {30, 40, 50, 60, 70, 60, 50, 40, 30},
            {40, 50, 60, 70, 80, 70, 60, 50, 40},
            {50, 60, 70, 80, 90, 80, 70, 60, 50}
        };
        tables.put(Soldier.class, soldierTable);
        
        // 马的位置价值表
        int[][] horseTable = {
            {0,  5,  10, 15, 20, 15, 10, 5,  0},
            {5,  10, 20, 25, 30, 25, 20, 10, 5},
            {10, 20, 30, 35, 40, 35, 30, 20, 10},
            {15, 25, 35, 40, 45, 40, 35, 25, 15},
            {20, 30, 40, 45, 50, 45, 40, 30, 20},
            {20, 30, 40, 45, 50, 45, 40, 30, 20},
            {15, 25, 35, 40, 45, 40, 35, 25, 15},
            {10, 20, 30, 35, 40, 35, 30, 20, 10},
            {5,  10, 20, 25, 30, 25, 20, 10, 5},
            {0,  5,  10, 15, 20, 15, 10, 5,  0}
        };
        tables.put(Horse.class, horseTable);
        
        // 其他棋子的位置价值表...
        
        return tables;
    }
    
    private int getPositionValue(Piece piece, int row, int col) {
        int[][] table = positionTables.get(piece.getClass());
        if (table != null) {
            // 黑方需要翻转坐标
            if (piece.getColor() == PieceColor.BLACK) {
                row = 9 - row;
            }
            return table[row][col];
        }
        return 0;
    }
    
    private int countAttackers(Board board, Position target, PieceColor color) {
        int count = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    Position from = new Position(row, col);
                    if (piece.isValidMove(board, from, target)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    private boolean isInCheck(Board board, PieceColor color) {
        // 找到将/帅的位置
        Position kingPos = null;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece instanceof General && piece.getColor() == color) {
                    kingPos = new Position(row, col);
                    break;
                }
            }
        }
        
        if (kingPos == null) return false;
        
        // 检查是否被攻击
        return countAttackers(board, kingPos, getOpponentColor(color)) > 0;
    }
    
    private int findForks(Board board, PieceColor color) {
        // 简化的双重攻击检测
        int forks = 0;
        List<Move> moves = getAllPossibleMoves(board, color);
        
        for (Move move : moves) {
            Board tempBoard = copyBoard(board);
            tempBoard.movePiece(move.getStart(), move.getEnd());
            
            Piece movingPiece = tempBoard.getPiece(move.getEnd().getX(), move.getEnd().getY());
            if (movingPiece != null) {
                int targets = 0;
                for (int row = 0; row < 10; row++) {
                    for (int col = 0; col < 9; col++) {
                        Piece target = tempBoard.getPiece(row, col);
                        if (target != null && target.getColor() != color) {
                            Position targetPos = new Position(row, col);
                            if (movingPiece.isValidMove(tempBoard, move.getEnd(), targetPos)) {
                                targets++;
                            }
                        }
                    }
                }
                if (targets >= 2) forks++;
            }
        }
        
        return forks;
    }
    
    private int findPins(Board board, PieceColor color) {
        // 简化的牵制检测
        return 0; // 暂时返回0，可以后续实现
    }
    
    private int findDiscoveredAttacks(Board board, PieceColor color) {
        // 简化的闪击检测
        return 0; // 暂时返回0，可以后续实现
    }
    
    private List<Move> getAllPossibleMoves(Board board, PieceColor color) {
        List<Move> moves = new ArrayList<>();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    Position start = new Position(row, col);
                    
                    for (int targetRow = 0; targetRow < 10; targetRow++) {
                        for (int targetCol = 0; targetCol < 9; targetCol++) {
                            Position end = new Position(targetRow, targetCol);
                            if (piece.isValidMove(board, start, end) && 
                                board.isMoveSafe(start, end, color)) {
                                moves.add(new Move(start, end));
                            }
                        }
                    }
                }
            }
        }
        
        return moves;
    }
    
    private Board copyBoard(Board original) {
        Board copy = new Board();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = original.getPiece(row, col);
                if (piece != null) {
                    copy.setPiece(row, col, clonePiece(piece));
                }
            }
        }
        return copy;
    }
    
    private Piece clonePiece(Piece piece) {
        if (piece instanceof General) return new General(piece.getColor());
        if (piece instanceof Advisor) return new Advisor(piece.getColor());
        if (piece instanceof Elephant) return new Elephant(piece.getColor());
        if (piece instanceof Horse) return new Horse(piece.getColor());
        if (piece instanceof Chariot) return new Chariot(piece.getColor());
        if (piece instanceof Cannon) return new Cannon(piece.getColor());
        if (piece instanceof Soldier) return new Soldier(piece.getColor());
        return null;
    }
    
    private PieceColor getOpponentColor(PieceColor color) {
        return color == PieceColor.RED ? PieceColor.BLACK : PieceColor.RED;
    }
    
    private long getBoardHash(Board board) {
        // 简化的哈希函数
        long hash = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    hash = hash * 31 + piece.hashCode() + row * 9 + col;
                }
            }
        }
        return hash;
    }
    
    private String formatMove(Move move) {
        if (move == null) return "无效移动";
        return String.format("从(%d,%d)到(%d,%d)", 
            move.getStart().getX(), move.getStart().getY(),
            move.getEnd().getX(), move.getEnd().getY());
    }
    
    // 内部类
    private static class SearchResult {
        final Move move;
        final int score;
        
        SearchResult(Move move, int score) {
            this.move = move;
            this.score = score;
        }
    }
    
    private static class TranspositionEntry {
        final int depth;
        final int score;
        final Move move;
        
        TranspositionEntry(int depth, int score, Move move) {
            this.depth = depth;
            this.score = score;
            this.move = move;
        }
    }
    
    private static class TranspositionTable {
        private final Map<Long, TranspositionEntry> table = new ConcurrentHashMap<>();
        
        void store(long hash, int depth, int score, Move move) {
            table.put(hash, new TranspositionEntry(depth, score, move));
        }
        
        TranspositionEntry probe(long hash) {
            return table.get(hash);
        }
        
        void clear() {
            table.clear();
        }
    }
    
    private static class OpeningBook {
        private final Map<String, List<Move>> redOpenings = new HashMap<>();
        private final Map<String, List<Move>> blackOpenings = new HashMap<>();
        
        OpeningBook() {
            initializeOpenings();
        }
        
        private void initializeOpenings() {
            // 红方开局
            List<Move> redCannonOpening = Arrays.asList(
                new Move(new Position(7, 1), new Position(7, 4)), // 炮二平五
                new Move(new Position(9, 1), new Position(7, 2)), // 马二进三
                new Move(new Position(6, 4), new Position(5, 4))  // 兵五进一
            );
            redOpenings.put("start", redCannonOpening);
            
            // 黑方开局（对应红方的开局）
            List<Move> blackCannonOpening = Arrays.asList(
                new Move(new Position(2, 7), new Position(2, 5)), // 炮8平5
                new Move(new Position(0, 7), new Position(2, 6)), // 马8进7
                new Move(new Position(3, 4), new Position(4, 4))  // 卒5进1
            );
            blackOpenings.put("start", blackCannonOpening);
        }
        
        Move getOpeningMove(Board board, PieceColor aiColor) {
            // 简化的开局检测
            int pieceCount = 0;
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 9; col++) {
                    if (board.getPiece(row, col) != null) {
                        pieceCount++;
                    }
                }
            }
            
            // 如果是开局阶段（棋子数量接近初始状态）
            if (pieceCount >= 30) {
                Map<String, List<Move>> openings = (aiColor == PieceColor.RED) ? redOpenings : blackOpenings;
                List<Move> moves = openings.get("start");
                if (moves != null && !moves.isEmpty()) {
                    // 按顺序检查每个开局走法，找到第一个有效的
                    for (Move move : moves) {
                        if (isValidOpeningMove(board, move, aiColor)) {
                            return move;
                        }
                    }
                }
            }
            
            return null;
        }
        
        private boolean isValidOpeningMove(Board board, Move move, PieceColor aiColor) {
            Position start = move.getStart();
            Position end = move.getEnd();
            
            // 检查起始位置是否有棋子
            Piece piece = board.getPiece(start.getX(), start.getY());
            if (piece == null) {
                return false;
            }
            
            // 检查棋子颜色是否匹配
            if (piece.getColor() != aiColor) {
                return false;
            }
            
            // 检查移动是否有效
            if (!piece.isValidMove(board, start, end)) {
                return false;
            }
            
            // 检查移动是否安全
            return board.isMoveSafe(start, end, aiColor);
        }
    }
}