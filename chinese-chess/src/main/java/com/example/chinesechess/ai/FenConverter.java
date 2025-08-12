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
        StringBuilder fen = new StringBuilder();
        
        // 1. 棋盘布局（从第0行到第9行）
        for (int row = 0; row < 10; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToFenChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 9) {
                fen.append('/');
            }
        }
        
        // 2. 当前行棋方（红方用w，黑方用b）
        fen.append(' ');
        fen.append(currentPlayer == PieceColor.RED ? 'w' : 'b');
        
        // 3. 其他信息（中国象棋不需要易位和吃过路兵）
        fen.append(" - - 0 1");
        
        return fen.toString();
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
            return null;
        }
        
        try {
            char fileChar = uci.charAt(0);
            char rankChar = uci.charAt(1);
            
            // 检查字符范围
            if (fileChar < 'a' || fileChar > 'i') {
                System.err.println("UCI列字符超出范围: " + fileChar);
                return null;
            }
            if (rankChar < '0' || rankChar > '9') {
                System.err.println("UCI行字符超出范围: " + rankChar);
                return null;
            }
            
            int col = fileChar - 'a';  // 0-8
            int row = 9 - (rankChar - '0');  // 9-0 -> 0-9
            
            // 检查坐标范围
            if (row < 0 || row > 9 || col < 0 || col > 8) {
                System.err.println("UCI坐标超出棋盘范围: row=" + row + ", col=" + col);
                return null;
            }
            
            return new Position(row, col);
        } catch (Exception e) {
            System.err.println("解析UCI位置失败: " + uci + ", " + e.getMessage());
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
}