package com.example.gomoku.ai;

import com.example.gomoku.core.GomokuBoard;
import java.util.*;

/**
 * 五子棋威胁检测系统
 * 用于检测和评估棋盘上的威胁模式，如活四、冲四、活三等
 */
public class ThreatDetector {
    
    // 威胁级别常量
    public static final int THREAT_WIN = 1000000;      // 五连 - 必胜
    public static final int THREAT_FOUR_OPEN = 100000; // 活四 - 必胜
    public static final int THREAT_FOUR_HALF = 10000;  // 冲四 - 很强
    public static final int THREAT_THREE_OPEN = 5000;  // 活三 - 强威胁
    public static final int THREAT_THREE_HALF = 500;   // 眠三 - 中等威胁
    public static final int THREAT_TWO_OPEN = 200;     // 活二 - 弱威胁
    public static final int THREAT_TWO_HALF = 50;      // 眠二 - 很弱威胁
    
    /**
     * 威胁信息类
     */
    public static class ThreatInfo {
        public final int row, col;        // 威胁位置
        public final int level;           // 威胁级别
        public final String description;  // 威胁描述
        public final char player;         // 威胁玩家
        
        public ThreatInfo(int row, int col, int level, String description, char player) {
            this.row = row;
            this.col = col;
            this.level = level;
            this.description = description;
            this.player = player;
        }
    }
    
    /**
     * 检测棋盘上的所有威胁
     */
    public static List<ThreatInfo> detectThreats(GomokuBoard board) {
        List<ThreatInfo> threats = new ArrayList<>();
        
        // 先检查棋盘是否为空
        boolean isEmpty = true;
        for (int row = 0; row < GomokuBoard.BOARD_SIZE && isEmpty; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE && isEmpty; col++) {
                if (board.getPiece(row, col) != ' ') {
                    isEmpty = false;
                }
            }
        }
        
        // 如果棋盘为空，返回空威胁列表
        if (isEmpty) {
            return threats;
        }
        
        // 检查每个空位置的威胁潜力
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) == ' ') {
                    // 检查黑子在此位置的威胁
                    ThreatInfo blackThreat = analyzePosition(board, row, col, GomokuBoard.BLACK);
                    if (blackThreat != null && blackThreat.level > 0) {
                        threats.add(blackThreat);
                    }
                    
                    // 检查白子在此位置的威胁
                    ThreatInfo whiteThreat = analyzePosition(board, row, col, GomokuBoard.WHITE);
                    if (whiteThreat != null && whiteThreat.level > 0) {
                        threats.add(whiteThreat);
                    }
                }
            }
        }
        
        // 按威胁级别排序（从高到低）
        threats.sort((a, b) -> Integer.compare(b.level, a.level));
        
        return threats;
    }
    
    /**
     * 分析指定位置对指定玩家的威胁级别
     */
    public static ThreatInfo analyzePosition(GomokuBoard board, int row, int col, char player) {
        if (board.getPiece(row, col) != ' ') {
            return null; // 位置已占用
        }
        
        int maxThreat = 0;
        String maxDescription = "";
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        // 检查四个方向
        for (int[] dir : directions) {
            PatternAnalysis analysis = analyzeDirection(board, row, col, dir[0], dir[1], player);
            if (analysis.threatLevel > maxThreat) {
                maxThreat = analysis.threatLevel;
                maxDescription = analysis.description;
            }
        }
        
        if (maxThreat > 0) {
            return new ThreatInfo(row, col, maxThreat, maxDescription, player);
        }
        
        return null;
    }
    
    /**
     * 分析特定方向的模式
     */
    private static PatternAnalysis analyzeDirection(GomokuBoard board, int row, int col, int dr, int dc, char player) {
        // 临时放置棋子进行分析
        board.setPiece(row, col, player);
        
        int count = 1; // 包括刚放置的棋子
        int leftEmpty = 0, rightEmpty = 0;
        boolean leftBlocked = false, rightBlocked = false;
        
        // 向正方向分析
        int r = row + dr, c = col + dc;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
            char piece = board.getPiece(r, c);
            if (piece == player) {
                count++;
            } else if (piece == ' ') {
                rightEmpty++;
                break;
            } else {
                rightBlocked = true;
                break;
            }
            r += dr; c += dc;
        }
        
        if (r < 0 || r >= GomokuBoard.BOARD_SIZE || c < 0 || c >= GomokuBoard.BOARD_SIZE) {
            rightBlocked = true;
        }
        
        // 向负方向分析
        r = row - dr; c = col - dc;
        while (r >= 0 && r < GomokuBoard.BOARD_SIZE && c >= 0 && c < GomokuBoard.BOARD_SIZE) {
            char piece = board.getPiece(r, c);
            if (piece == player) {
                count++;
            } else if (piece == ' ') {
                leftEmpty++;
                break;
            } else {
                leftBlocked = true;
                break;
            }
            r -= dr; c -= dc;
        }
        
        if (r < 0 || r >= GomokuBoard.BOARD_SIZE || c < 0 || c >= GomokuBoard.BOARD_SIZE) {
            leftBlocked = true;
        }
        
        // 恢复棋盘状态
        board.setPiece(row, col, ' ');
        
        return evaluatePattern(count, leftEmpty, rightEmpty, leftBlocked, rightBlocked);
    }
    
    /**
     * 根据模式特征评估威胁级别
     */
    private static PatternAnalysis evaluatePattern(int count, int leftEmpty, int rightEmpty, 
                                                 boolean leftBlocked, boolean rightBlocked) {
        int openEnds = (leftBlocked ? 0 : 1) + (rightBlocked ? 0 : 1);
        int totalSpace = leftEmpty + rightEmpty + count;
        
        // 五连或更多 - 必胜
        if (count >= 5) {
            return new PatternAnalysis(THREAT_WIN, "五连必胜");
        }
        
        // 四连
        if (count == 4) {
            if (openEnds == 2) {
                return new PatternAnalysis(THREAT_FOUR_OPEN, "活四必胜");
            } else if (openEnds == 1) {
                return new PatternAnalysis(THREAT_FOUR_HALF, "冲四");
            }
        }
        
        // 三连
        if (count == 3) {
            if (openEnds == 2) {
                return new PatternAnalysis(THREAT_THREE_OPEN, "活三");
            } else if (openEnds == 1 && totalSpace >= 5) {
                return new PatternAnalysis(THREAT_THREE_HALF, "眠三");
            }
        }
        
        // 二连
        if (count == 2) {
            if (openEnds == 2 && totalSpace >= 5) {
                return new PatternAnalysis(THREAT_TWO_OPEN, "活二");
            } else if (openEnds == 1 && totalSpace >= 5) {
                return new PatternAnalysis(THREAT_TWO_HALF, "眠二");
            }
        }
        
        return new PatternAnalysis(0, "无威胁");
    }
    
    /**
     * 寻找最紧急的威胁（需要立即防守的）
     */
    public static List<ThreatInfo> findUrgentThreats(GomokuBoard board) {
        List<ThreatInfo> allThreats = detectThreats(board);
        List<ThreatInfo> urgentThreats = new ArrayList<>();
        
        for (ThreatInfo threat : allThreats) {
            // 活四和五连是最紧急的威胁
            if (threat.level >= THREAT_FOUR_HALF) {
                urgentThreats.add(threat);
            }
        }
        
        return urgentThreats;
    }
    
    /**
     * 寻找最佳的攻击位置
     */
    public static ThreatInfo findBestAttackMove(GomokuBoard board, char player) {
        List<ThreatInfo> allThreats = detectThreats(board);
        
        // 寻找该玩家的最强威胁
        for (ThreatInfo threat : allThreats) {
            if (threat.player == player) {
                return threat;
            }
        }
        
        return null;
    }
    
    /**
     * 寻找最佳的防守位置
     */
    public static ThreatInfo findBestDefenseMove(GomokuBoard board, char player) {
        char opponent = (player == GomokuBoard.BLACK) ? GomokuBoard.WHITE : GomokuBoard.BLACK;
        List<ThreatInfo> urgentThreats = findUrgentThreats(board);
        
        // 寻找对手的最强威胁进行防守
        for (ThreatInfo threat : urgentThreats) {
            if (threat.player == opponent) {
                return threat;
            }
        }
        
        return null;
    }
    
    /**
     * 检查是否存在双重威胁（双三、双四等）
     */
    public static boolean hasDoubleThreat(GomokuBoard board, int row, int col, char player) {
        // 临时放置棋子
        board.setPiece(row, col, player);
        
        List<ThreatInfo> threats = detectThreats(board);
        int playerThreats = 0;
        
        // 计算该玩家的威胁数量
        for (ThreatInfo threat : threats) {
            if (threat.player == player && threat.level >= THREAT_THREE_HALF) {
                playerThreats++;
                if (playerThreats >= 2) {
                    board.setPiece(row, col, ' '); // 恢复
                    return true;
                }
            }
        }
        
        board.setPiece(row, col, ' '); // 恢复
        return false;
    }
    
    /**
     * 模式分析结果类
     */
    private static class PatternAnalysis {
        final int threatLevel;
        final String description;
        
        PatternAnalysis(int threatLevel, String description) {
            this.threatLevel = threatLevel;
            this.description = description;
        }
    }
}
