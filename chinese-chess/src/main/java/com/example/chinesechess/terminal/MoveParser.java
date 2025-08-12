package com.example.chinesechess.terminal;

import com.example.chinesechess.core.*;
import com.example.chinesechess.core.Move;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 自然语言走法解析器
 * 支持多种格式的走法输入，包括中文描述、坐标等
 */
public class MoveParser {
    
    // 棋子名称映射
    private static final Map<String, Set<String>> PIECE_ALIASES = new HashMap<>();
    static {
        PIECE_ALIASES.put("将", new HashSet<>(Arrays.asList("将", "帅", "王", "老将", "老帅")));
        PIECE_ALIASES.put("士", new HashSet<>(Arrays.asList("士", "仕", "护卫", "侍卫")));
        PIECE_ALIASES.put("象", new HashSet<>(Arrays.asList("象", "相", "大象")));
        PIECE_ALIASES.put("马", new HashSet<>(Arrays.asList("马", "馬", "骑兵", "马儿")));
        PIECE_ALIASES.put("车", new HashSet<>(Arrays.asList("车", "車", "战车", "车子")));
        PIECE_ALIASES.put("炮", new HashSet<>(Arrays.asList("炮", "砲", "大炮", "炮兵")));
        PIECE_ALIASES.put("兵", new HashSet<>(Arrays.asList("兵", "卒", "小兵", "士兵")));
    }
    
    // 数字映射
    private static final Map<String, Integer> NUMBER_MAP = new HashMap<>();
    static {
        // 中文数字
        NUMBER_MAP.put("一", 1); NUMBER_MAP.put("二", 2); NUMBER_MAP.put("三", 3);
        NUMBER_MAP.put("四", 4); NUMBER_MAP.put("五", 5); NUMBER_MAP.put("六", 6);
        NUMBER_MAP.put("七", 7); NUMBER_MAP.put("八", 8); NUMBER_MAP.put("九", 9);
        NUMBER_MAP.put("十", 10);
        
        // 阿拉伯数字
        for (int i = 1; i <= 10; i++) {
            NUMBER_MAP.put(String.valueOf(i), i);
        }
        
        // 其他表示
        NUMBER_MAP.put("前", 1); NUMBER_MAP.put("中", 5); NUMBER_MAP.put("后", 9);
        NUMBER_MAP.put("左", 1); NUMBER_MAP.put("右", 9);
    }
    
    // 动作词映射
    private static final Map<String, String> ACTION_MAP = new HashMap<>();
    static {
        ACTION_MAP.put("进", "进"); ACTION_MAP.put("前进", "进"); ACTION_MAP.put("向前", "进");
        ACTION_MAP.put("退", "退"); ACTION_MAP.put("后退", "退"); ACTION_MAP.put("向后", "退");
        ACTION_MAP.put("平", "平"); ACTION_MAP.put("横移", "平"); ACTION_MAP.put("平移", "平");
        ACTION_MAP.put("到", "到"); ACTION_MAP.put("去", "到"); ACTION_MAP.put("移动到", "到");
    }
    
    private Board board;
    
    public MoveParser(Board board) {
        this.board = board;
    }
    
    /**
     * 解析走法字符串
     */
    public Move parseMove(String input, PieceColor playerColor) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        input = input.trim().toLowerCase();
        
        // 尝试不同的解析策略
        Move move = null;
        
        // 1. 坐标格式
        move = parseCoordinateFormat(input);
        if (move != null) return move;
        
        // 2. 标准记谱法
        move = parseStandardNotation(input, playerColor);
        if (move != null) return move;
        
        // 3. 自然语言描述
        move = parseNaturalLanguage(input, playerColor);
        if (move != null) return move;
        
        // 4. 简化格式
        move = parseSimplifiedFormat(input, playerColor);
        if (move != null) return move;
        
        return null;
    }
    
    /**
     * 解析坐标格式
     * 支持：(1,2)到(3,4)、1,2->3,4、从1,2到3,4等
     */
    private Move parseCoordinateFormat(String input) {
        // 多种坐标格式的正则表达式
        String[] patterns = {
            "从?\\s*\\(?\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)?\\s*到\\s*\\(?\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)?",
            "\\(?\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)?\\s*->\\s*\\(?\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)?",
            "(\\d+)\\s*,\\s*(\\d+)\\s+to\\s+(\\d+)\\s*,\\s*(\\d+)",
            "(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
        };
        
        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(input);
            
            if (matcher.find()) {
                try {
                    int startRow = Integer.parseInt(matcher.group(1));
                    int startCol = Integer.parseInt(matcher.group(2));
                    int endRow = Integer.parseInt(matcher.group(3));
                    int endCol = Integer.parseInt(matcher.group(4));
                    
                    // 转换坐标系（用户输入1-based，转换为0-based）
                    Position start = new Position(10 - startRow, startCol - 1);
                    Position end = new Position(10 - endRow, endCol - 1);
                    
                    if (isValidPosition(start) && isValidPosition(end)) {
                        return new Move(start, end);
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 解析标准记谱法
     * 支持：马二进三、车1进1、炮八平五等
     */
    private Move parseStandardNotation(String input, PieceColor playerColor) {
        // 标准记谱法正则：棋子名+位置+动作+目标
        Pattern pattern = Pattern.compile("([将帅士仕象相马車车炮砲兵卒])([一二三四五六七八九十1-9])([进退平])([一二三四五六七八九十1-9])");
        Matcher matcher = pattern.matcher(input);
        
        if (!matcher.find()) {
            System.out.println("🔍 调试：正则表达式未匹配到标准记谱法格式");
            return null;
        }
        
        String pieceName = matcher.group(1);
        String fromPos = matcher.group(2);
        String action = matcher.group(3);
        String toPos = matcher.group(4);
        
        // 查找棋子
        String normalizedPieceName = normalizePieceName(pieceName);
        List<Position> candidates = findPiecesByName(normalizedPieceName, playerColor);
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        // 根据位置筛选
        Integer fromCol = NUMBER_MAP.get(fromPos);
        if (fromCol == null) {
            return null;
        }
        
        Position startPos = null;
        for (Position pos : candidates) {
            if (pos.getY() == fromCol - 1) { // 转换为0-based
                startPos = pos;
                break;
            }
        }
        
        if (startPos == null) {
            return null;
        }
        
        // 计算目标位置
        Position endPos = calculateTargetPosition(startPos, action, toPos, playerColor);
        if (endPos == null || !isValidPosition(endPos)) {
            return null;
        }
        return new Move(startPos, endPos);
    }
    
    /**
     * 解析自然语言描述
     * 支持：马走日字、车直走、炮打将等
     */
    private Move parseNaturalLanguage(String input, PieceColor playerColor) {
        // 提取棋子名称
        String pieceName = extractPieceName(input);
        if (pieceName == null) return null;
        
        // 查找该类型的棋子
        List<Position> candidates = findPiecesByName(pieceName, playerColor);
        if (candidates.isEmpty()) return null;
        
        // 如果只有一个候选，尝试解析目标位置
        if (candidates.size() == 1) {
            Position start = candidates.get(0);
            Position target = extractTargetPosition(input, start, playerColor);
            if (target != null && isValidPosition(target)) {
                return new Move(start, target);
            }
        }
        
        // 多个候选时，需要更精确的位置描述
        Position start = selectPieceByDescription(input, candidates, playerColor);
        if (start != null) {
            Position target = extractTargetPosition(input, start, playerColor);
            if (target != null && isValidPosition(target)) {
                return new Move(start, target);
            }
        }
        
        return null;
    }
    
    /**
     * 解析简化格式
     * 支持：马23、车11、炮85等（起始位置+目标位置）
     */
    private Move parseSimplifiedFormat(String input, PieceColor playerColor) {
        Pattern pattern = Pattern.compile("([将帅士仕象相马車车炮砲兵卒])([1-9])([1-9])");
        Matcher matcher = pattern.matcher(input);
        
        if (!matcher.find()) {
            return null;
        }
        
        String pieceName = matcher.group(1);
        int fromCol = Integer.parseInt(matcher.group(2));
        int toCol = Integer.parseInt(matcher.group(3));
        
        // 查找棋子
        String normalizedPieceName = normalizePieceName(pieceName);
        List<Position> candidates = findPiecesByName(normalizedPieceName, playerColor);
        
        Position startPos = null;
        for (Position pos : candidates) {
            if (pos.getY() == fromCol - 1) {
                startPos = pos;
                break;
            }
        }
        
        if (startPos == null) return null;
        
        // 简单的目标位置计算（同行移动到目标列）
        Position endPos = new Position(startPos.getX(), toCol - 1);
        if (isValidPosition(endPos)) {
            return new Move(startPos, endPos);
        }
        
        return null;
    }
    
    /**
     * 标准化棋子名称
     */
    private String normalizePieceName(String name) {
        for (Map.Entry<String, Set<String>> entry : PIECE_ALIASES.entrySet()) {
            if (entry.getValue().contains(name)) {
                return entry.getKey();
            }
        }
        return name;
    }
    
    /**
     * 从输入中提取棋子名称
     */
    private String extractPieceName(String input) {
        for (Map.Entry<String, Set<String>> entry : PIECE_ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                if (input.contains(alias)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    /**
     * 根据名称查找棋子位置
     */
    private List<Position> findPiecesByName(String pieceName, PieceColor color) {
        List<Position> positions = new ArrayList<>();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    String chineseName = piece.getChineseName();
                    if (chineseName.contains(pieceName)) {
                        positions.add(new Position(row, col));
                    }
                }
            }
        }
        
        return positions;
    }
    
    /**
     * 计算目标位置
     */
    private Position calculateTargetPosition(Position start, String action, String toPos, PieceColor color) {
        Integer targetNum = NUMBER_MAP.get(toPos);
        if (targetNum == null) return null;
        
        int startRow = start.getX();
        int startCol = start.getY();
        
        // 获取起始位置的棋子
        Piece piece = board.getPiece(startRow, startCol);
        if (piece == null) return null;
        
        // 马的移动需要特殊处理，因为马不能使用进退平的记谱法
        if (piece instanceof Horse) {
            return null; // 马不支持进退平记谱法
        }
        
        switch (action) {
            case "进":
                if (color == PieceColor.RED) {
                    return new Position(startRow - targetNum, startCol);
                } else {
                    return new Position(startRow + targetNum, startCol);
                }
            case "退":
                if (color == PieceColor.RED) {
                    return new Position(startRow + targetNum, startCol);
                } else {
                    return new Position(startRow - targetNum, startCol);
                }
            case "平":
                return new Position(startRow, targetNum - 1);
            default:
                return null;
        }
    }
    
    /**
     * 从描述中提取目标位置
     */
    private Position extractTargetPosition(String input, Position start, PieceColor color) {
        // 获取起始位置的棋子
        Piece piece = board.getPiece(start.getX(), start.getY());
        
        // 如果是马，且输入包含"进退平"等标准记谱法词汇，则拒绝解析
        if (piece instanceof Horse && (input.contains("进") || input.contains("退") || input.contains("平"))) {
            return null;
        }
        
        // 查找数字坐标
        Pattern coordPattern = Pattern.compile("(\\d+)\\s*,\\s*(\\d+)");
        Matcher coordMatcher = coordPattern.matcher(input);
        if (coordMatcher.find()) {
            try {
                int row = Integer.parseInt(coordMatcher.group(1));
                int col = Integer.parseInt(coordMatcher.group(2));
                return new Position(10 - row, col - 1);
            } catch (NumberFormatException e) {
                // 忽略
            }
        }
        
        // 查找方向词（但排除马的情况）
        if (input.contains("前") || input.contains("进")) {
            int steps = extractSteps(input);
            if (color == PieceColor.RED) {
                return new Position(start.getX() - steps, start.getY());
            } else {
                return new Position(start.getX() + steps, start.getY());
            }
        }
        
        if (input.contains("后") || input.contains("退")) {
            int steps = extractSteps(input);
            if (color == PieceColor.RED) {
                return new Position(start.getX() + steps, start.getY());
            } else {
                return new Position(start.getX() - steps, start.getY());
            }
        }
        
        if (input.contains("左")) {
            int steps = extractSteps(input);
            return new Position(start.getX(), start.getY() - steps);
        }
        
        if (input.contains("右")) {
            int steps = extractSteps(input);
            return new Position(start.getX(), start.getY() + steps);
        }
        
        return null;
    }
    
    /**
     * 根据描述选择特定的棋子
     */
    private Position selectPieceByDescription(String input, List<Position> candidates, PieceColor color) {
        // 根据位置描述筛选
        if (input.contains("前") && candidates.size() > 1) {
            return candidates.stream()
                .min(Comparator.comparingInt(p -> color == PieceColor.RED ? p.getX() : 9 - p.getX()))
                .orElse(null);
        }
        
        if (input.contains("后") && candidates.size() > 1) {
            return candidates.stream()
                .max(Comparator.comparingInt(p -> color == PieceColor.RED ? p.getX() : 9 - p.getX()))
                .orElse(null);
        }
        
        if (input.contains("左") && candidates.size() > 1) {
            return candidates.stream()
                .min(Comparator.comparingInt(Position::getY))
                .orElse(null);
        }
        
        if (input.contains("右") && candidates.size() > 1) {
            return candidates.stream()
                .max(Comparator.comparingInt(Position::getY))
                .orElse(null);
        }
        
        // 默认返回第一个
        return candidates.isEmpty() ? null : candidates.get(0);
    }
    
    /**
     * 从输入中提取步数
     */
    private int extractSteps(String input) {
        for (Map.Entry<String, Integer> entry : NUMBER_MAP.entrySet()) {
            if (input.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 1; // 默认1步
    }
    
    /**
     * 检查位置是否有效
     */
    private boolean isValidPosition(Position pos) {
        return pos.getX() >= 0 && pos.getX() < 10 && pos.getY() >= 0 && pos.getY() < 9;
    }
}