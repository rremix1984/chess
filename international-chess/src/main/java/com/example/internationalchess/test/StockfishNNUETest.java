package com.example.internationalchess.test;

import com.example.internationalchess.ai.StockfishAI;
import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.Move;
import com.example.internationalchess.core.PieceColor;

/**
 * Stockfish神经网络AI测试类
 * 用于验证NNUE神经网络是否正确加载和工作
 */
public class StockfishNNUETest {
    
    public static void main(String[] args) {
        System.out.println("🧪 开始测试Stockfish NNUE AI集成");
        System.out.println("=" + "=".repeat(50));
        
        // 测试不同难度级别的AI
        testDifferentDifficultyLevels();
        
        // 测试AI移动计算
        testAIMoveCalculation();
        
        System.out.println("=" + "=".repeat(50));
        System.out.println("✅ Stockfish NNUE AI测试完成");
    }
    
    /**
     * 测试不同难度级别的AI初始化
     */
    private static void testDifferentDifficultyLevels() {
        System.out.println("\n📊 测试不同难度级别:");
        
        String[] difficulties = {"简单", "中等", "困难"};
        
        for (String difficulty : difficulties) {
            try {
                System.out.println("\n🎯 初始化" + difficulty + "级别AI...");
                StockfishAI ai = new StockfishAI(difficulty, PieceColor.BLACK);
                
                if (ai.isReady()) {
                    System.out.println("✅ " + difficulty + "级别AI初始化成功");
                    System.out.println("ℹ️  引擎信息: " + ai.getEngineInfo());
                } else {
                    System.out.println("❌ " + difficulty + "级别AI初始化失败");
                }
                
                // 关闭AI释放资源
                ai.shutdown();
                System.out.println("🔄 " + difficulty + "级别AI已关闭");
                
            } catch (Exception e) {
                System.err.println("❌ 初始化" + difficulty + "级别AI时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 测试AI移动计算
     */
    private static void testAIMoveCalculation() {
        System.out.println("\n🎲 测试AI移动计算:");
        
        try {
            // 创建中等难度的AI
            System.out.println("\n🤖 创建中等难度AI...");
            StockfishAI ai = new StockfishAI("中等", PieceColor.WHITE);
            
            if (!ai.isReady()) {
                System.out.println("❌ AI未就绪，跳过移动测试");
                return;
            }
            
            // 创建新的棋盘
            InternationalChessBoard board = new InternationalChessBoard();
            System.out.println("📋 创建标准国际象棋开局棋盘");
            
            // 打印棋盘状态（简化版本）
            printSimpleBoardState(board);
            
            // 让AI计算开局移动
            System.out.println("\n🤔 AI正在计算开局移动...");
            long startTime = System.currentTimeMillis();
            
            Move bestMove = ai.calculateMove(board, PieceColor.WHITE);
            
            long endTime = System.currentTimeMillis();
            long thinkingTime = endTime - startTime;
            
            if (bestMove != null) {
                System.out.println("✅ AI找到最佳移动: " + moveToString(bestMove));
                System.out.println("⏱️  计算时间: " + thinkingTime + "ms");
                
                // 验证这是一个合理的开局移动
                if (isReasonableOpeningMove(bestMove)) {
                    System.out.println("🎯 这是一个合理的开局移动");
                } else {
                    System.out.println("⚠️  这可能不是最佳的开局移动");
                }
                
            } else {
                System.out.println("❌ AI未能找到有效移动");
            }
            
            // 关闭AI
            ai.shutdown();
            System.out.println("🔄 AI已关闭");
            
        } catch (Exception e) {
            System.err.println("❌ 测试AI移动计算时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印简化的棋盘状态
     */
    private static void printSimpleBoardState(InternationalChessBoard board) {
        System.out.println("\n♟️  当前棋盘状态:");
        System.out.println("   a b c d e f g h");
        
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " ");
            for (int col = 0; col < 8; col++) {
                String piece = board.getPiece(row, col);
                if (piece == null) {
                    System.out.print(" .");
                } else {
                    // 简化棋子表示
                    char pieceChar = piece.charAt(1);
                    char colorChar = piece.charAt(0);
                    System.out.print(" " + (colorChar == 'W' ? Character.toUpperCase(pieceChar) : Character.toLowerCase(pieceChar)));
                }
            }
            System.out.println(" " + (8 - row));
        }
        System.out.println("   a b c d e f g h");
    }
    
    /**
     * 将移动转换为易读的字符串
     */
    private static String moveToString(Move move) {
        if (move == null) return "null";
        
        char fromFile = (char) ('a' + move.getFrom().getY());
        int fromRank = 8 - move.getFrom().getX();
        char toFile = (char) ('a' + move.getTo().getY());
        int toRank = 8 - move.getTo().getX();
        
        return "" + fromFile + fromRank + "-" + toFile + toRank;
    }
    
    /**
     * 检查是否是合理的开局移动
     */
    private static boolean isReasonableOpeningMove(Move move) {
        if (move == null) return false;
        
        int fromRow = move.getFrom().getX();
        int fromCol = move.getFrom().getY();
        int toRow = move.getTo().getX();
        int toCol = move.getTo().getY();
        
        // 检查是否是白方兵的开局移动 (从第7行到第6行或第5行)
        if (fromRow == 6 && (toRow == 5 || toRow == 4)) {
            return true;
        }
        
        // 检查是否是马的开局移动
        if (fromRow == 7 && (fromCol == 1 || fromCol == 6)) {
            return true;
        }
        
        return false;
    }
}
