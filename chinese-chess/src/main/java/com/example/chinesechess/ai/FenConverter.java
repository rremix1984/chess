package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;

/**
 * FEN格式转换工具类
 * 用于中国象棋棋盘与FEN字符串之间的相互转换
 */
public class FenConverter {
    
    /**
     * 将棋盘转换为FEN字符串
     * @param board 棋盘对象
     * @param currentPlayer 当前行棋方
     * @return FEN字符串
     */
    public static String boardToFen(Board board, PieceColor currentPlayer) {
        System.out.println("🔍 [FEN生成] 开始转换棋盘为FEN字符串");
        System.out.println("🔍 [FEN生成] 当前行棋方: " + (currentPlayer == PieceColor.RED ? "红方(w)" : "黑方(b)"));
        
        StringBuilder fen = new StringBuilder();
        
        // 1. 棋盘布局（从第0行到第9行）
        System.out.println("🔍 [FEN生成] 扫描棋盘布局:");
        for (int row = 0; row < 10; row++) {
            int emptyCount = 0;
            StringBuilder rowDebug = new StringBuilder("  第" + row + "行: ");
            
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                    rowDebug.append("[空]");
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    char fenChar = pieceToFenChar(piece);
                    fen.append(fenChar);
                    rowDebug.append("[").append(fenChar).append("]");
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 9) {
                fen.append('/');
            }
            System.out.println(rowDebug.toString());
        }
        
        // 2. 当前行棋方（红方用w，黑方用b）
        fen.append(' ');
        fen.append(currentPlayer == PieceColor.RED ? 'w' : 'b');
        
        // 3. 其他信息（中国象棋不需要易位和吃过路兵）
        fen.append(" - - 0 1");
        
        String result = fen.toString();
        System.out.println("🔍 [FEN生成] 最终FEN字符串: " + result);
        
        // 验证生成的FEN
        if (isValidFen(result)) {
            System.out.println("✅ [FEN生成] FEN字符串格式有效");
        } else {
            System.out.println("❌ [FEN生成] FEN字符串格式无效!");
        }
        
        // 额外验证：检查棋盘上是否有两个将
        int redGeneralCount = 0, blackGeneralCount = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece instanceof General) {
                    if (piece.getColor() == PieceColor.RED) {
                        redGeneralCount++;
                    } else {
                        blackGeneralCount++;
                    }
                }
            }
        }
        System.out.println("🔍 [FEN生成] 将军统计: 红将=" + redGeneralCount + ", 黑将=" + blackGeneralCount);
        
        return result;
    }
    
    /**
     * 将FEN字符串转换为棋盘
     * @param fen FEN字符串
     * @param board 要设置的棋盘对象
     * @return 当前行棋方
     */
    public static PieceColor fenToBoard(String fen, Board board) {
        String[] parts = fen.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("无效的FEN字符串: " + fen);
        }
        
        // 清空棋盘
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                board.setPiece(row, col, null);
            }
        }
        
        // 1. 解析棋盘布局
        String[] rows = parts[0].split("/");
        if (rows.length != 10) {
            throw new IllegalArgumentException("FEN棋盘行数不正确: " + rows.length);
        }
        
        for (int row = 0; row < 10; row++) {
            int col = 0;
            String rowStr = rows[row];
            
            for (int i = 0; i < rowStr.length(); i++) {
                char c = rowStr.charAt(i);
                if (Character.isDigit(c)) {
                    // 空格数量
                    int emptyCount = Character.getNumericValue(c);
                    col += emptyCount;
                } else {
                    // 棋子
                    Piece piece = fenCharToPiece(c);
                    if (piece != null && col < 9) {
                        board.setPiece(row, col, piece);
                    }
                    col++;
                }
                
                if (col > 9) {
                    throw new IllegalArgumentException("FEN第" + (row + 1) + "行列数超出范围");
                }
            }
        }
        
        // 2. 解析当前行棋方
        PieceColor currentPlayer = parts[1].equals("w") ? PieceColor.RED : PieceColor.BLACK;
        
        return currentPlayer;
    }
    
    /**
     * 将棋子转换为FEN字符
     * @param piece 棋子对象
     * @return FEN字符
     */
    private static char pieceToFenChar(Piece piece) {
        char baseChar;
        
        // 根据棋子类型确定基础字符
        if (piece instanceof General) {
            baseChar = 'k';  // 将/帅
        } else if (piece instanceof Advisor) {
            baseChar = 'a';  // 士
        } else if (piece instanceof Elephant) {
            baseChar = 'b';  // 象/相
        } else if (piece instanceof Horse) {
            baseChar = 'n';  // 马
        } else if (piece instanceof Chariot) {
            baseChar = 'r';  // 车
        } else if (piece instanceof Cannon) {
            baseChar = 'c';  // 炮
        } else if (piece instanceof Soldier) {
            baseChar = 'p';  // 兵/卒
        } else {
            return '?';
        }
        
        // 红方用大写，黑方用小写
        return piece.getColor() == PieceColor.RED ? Character.toUpperCase(baseChar) : baseChar;
    }
    
    /**
     * 将FEN字符转换为棋子
     * @param c FEN字符
     * @return 棋子对象
     */
    private static Piece fenCharToPiece(char c) {
        PieceColor color = Character.isUpperCase(c) ? PieceColor.RED : PieceColor.BLACK;
        char lowerChar = Character.toLowerCase(c);
        
        switch (lowerChar) {
            case 'k': return new General(color);    // 将/帅
            case 'a': return new Advisor(color);    // 士
            case 'b': return new Elephant(color);   // 象/相
            case 'n': return new Horse(color);      // 马
            case 'r': return new Chariot(color);    // 车
            case 'c': return new Cannon(color);     // 炮
            case 'p': return new Soldier(color);    // 兵/卒
            default: 
                System.err.println("未知的FEN字符: " + c);
                return null;
        }
    }
    
    /**
     * 将移动转换为UCI格式
     * @param from 起始位置
     * @param to 目标位置
     * @return UCI格式字符串，如"h2e2"
     */
    public static String moveToUci(Position from, Position to) {
        return positionToUci(from) + positionToUci(to);
    }
    
    /**
     * 将UCI格式转换为移动
     * @param uci UCI格式字符串
     * @return 移动数组 [from, to]
     */
    public static Position[] uciToMove(String uci) {
        if (uci == null || uci.length() != 4) {
            return null;
        }
        
        try {
            Position from = uciToPosition(uci.substring(0, 2));
            Position to = uciToPosition(uci.substring(2, 4));
            
            if (from == null || to == null) {
                return null;
            }
            
            return new Position[]{from, to};
        } catch (Exception e) {
            System.err.println("解析UCI移动失败: " + uci + ", " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 将位置转换为UCI格式
     * @param pos 位置对象
     * @return UCI格式字符串，如"h2"
     */
    public static String positionToUci(Position pos) {
        if (pos == null) {
            return null;
        }
        
        // 列：a-i (0-8)
        char file = (char)('a' + pos.getY());
        // 行：0-9 (从上到下，UCI中用9-0表示)
        char rank = (char)('0' + (9 - pos.getX()));
        
        return "" + file + rank;
    }
    
    /**
     * 将UCI格式转换为位置
     * @param uci UCI格式字符串，如"h2"
     * @return 位置对象
     */
    public static Position uciToPosition(String uci) {
        if (uci == null || uci.length() != 2) {
            System.err.println("[UCI转换] 无效UCI格式: " + uci + " (长度必须为2)");
            return null;
        }
        
        try {
            char fileChar = uci.charAt(0);
            char rankChar = uci.charAt(1);
            
            System.out.println("[UCI转换] 解析UCI: " + uci + " (文件列: " + fileChar + ", 排行: " + rankChar + ")");
            
            // 检查字符范围
            if (fileChar < 'a' || fileChar > 'i') {
                System.err.println("[UCI转换] UCI列字符超出范围: " + fileChar + " (必须在a-i之间)");
                return null;
            }
            if (rankChar < '0' || rankChar > '9') {
                System.err.println("[UCI转换] UCI行字符超出范围: " + rankChar + " (必须在0-9之间)");
                return null;
            }
            
            int col = fileChar - 'a';  // 0-8
            int row = 9 - (rankChar - '0');  // 9-0 -> 0-9
            
            System.out.println("[UCI转换] 转换结果: " + uci + " -> 棋盘坐标(" + row + "," + col + ")");
            
            // 检查坐标范围
            if (row < 0 || row > 9 || col < 0 || col > 8) {
                System.err.println("[UCI转换] 棋盘坐标超出范围: row=" + row + ", col=" + col + " (行:0-9, 列:0-8)");
                return null;
            }
            
            return new Position(row, col);
        } catch (Exception e) {
            System.err.println("[UCI转换] 解析UCI位置失败: " + uci + ", 异常: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取初始局面的FEN字符串
     * @return 初始FEN字符串
     */
    public static String getInitialFen() {
        // 标准中国象棋开局FEN
        return "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
    }
    
    /**
     * 验证FEN字符串是否有效
     * @param fen FEN字符串
     * @return 是否有效
     */
    public static boolean isValidFen(String fen) {
        if (fen == null || fen.trim().isEmpty()) {
            return false;
        }
        
        try {
            String[] parts = fen.trim().split(" ");
            if (parts.length < 2) {
                return false;
            }
            
            // 检查棋盘部分
            String[] rows = parts[0].split("/");
            if (rows.length != 10) {
                return false;
            }
            
            // 检查每一行
            for (String row : rows) {
                int colCount = 0;
                for (char c : row.toCharArray()) {
                    if (Character.isDigit(c)) {
                        colCount += Character.getNumericValue(c);
                    } else if (isValidFenChar(c)) {
                        colCount++;
                    } else {
                        return false;
                    }
                }
                if (colCount != 9) {
                    return false;
                }
            }
            
            // 检查行棋方
            if (!parts[1].equals("w") && !parts[1].equals("b")) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查字符是否为有效的FEN棋子字符
     * @param c 字符
     * @return 是否有效
     */
    private static boolean isValidFenChar(char c) {
        char lower = Character.toLowerCase(c);
        return lower == 'k' || lower == 'a' || lower == 'b' || 
               lower == 'n' || lower == 'r' || lower == 'c' || lower == 'p';
    }
    
    /**
     * 创建测试用的FEN字符串
     * @return 测试FEN
     */
    public static String getTestFen() {
        // 一个中局局面的FEN，用于测试
        return "r1ba1a3/4k4/2n1b4/p1p1p1p1p/9/9/P1P1P1P1P/1C2B1N2/9/RNBAKAB1R w - - 0 1";
    }
    
    /**
     * 测试UCI转换功能
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("🔍 测试UCI转换功能");
        
        // 测试初始FEN
        String initialFen = getInitialFen();
        System.out.println("初始局面FEN: " + initialFen);
        
        // 测试几个UCI走法
        String[] testMoves = {"d2d4", "e2e4", "c3c4", "g2g4", "h2h4", "a3a4"};
        
        for (String uciMove : testMoves) {
            System.out.println("\n测试UCI走法: " + uciMove);
            Position[] positions = uciToMove(uciMove);
            if (positions != null) {
                Position start = positions[0];
                Position end = positions[1];
                System.out.println("  转换结果: (" + start.getX() + "," + start.getY() + ") -> (" + end.getX() + "," + end.getY() + ")");
                
                // 反向转换验证
                String backConverted = moveToUci(start, end);
                System.out.println("  反向转换: " + backConverted);
                System.out.println("  一致性: " + uciMove.equals(backConverted));
            } else {
                System.out.println("  转换失败！");
            }
        }
        
        // 测试坐标范围
        System.out.println("\n测试坐标范围:");
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Position pos = new Position(row, col);
                String uci = positionToUci(pos);
                Position converted = uciToPosition(uci);
                boolean match = converted != null && 
                    converted.getX() == row && converted.getY() == col;
                if (!match) {
                    System.out.println("  坐标转换错误: (" + row + "," + col + ") -> " + uci + " -> " + 
                        (converted != null ? "(" + converted.getX() + "," + converted.getY() + ")" : "null"));
                }
            }
        }
        System.out.println("坐标转换测试完成");
    }
}