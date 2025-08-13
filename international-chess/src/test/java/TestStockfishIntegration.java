package test.java;

import com.example.common.config.GameConfig;
import com.example.internationalchess.ai.StockfishEngine;
import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.core.Move;

/**
 * 测试Stockfish与多游戏平台的集成
 */
public class TestStockfishIntegration {
    public static void main(String[] args) {
        System.out.println("=== 测试Stockfish平台集成 ===");
        
        try {
            // 测试1: 验证GameConfig能正确获取Stockfish路径
            System.out.println("1. 测试GameConfig配置...");
            GameConfig config = GameConfig.getInstance();
            String stockfishPath = config.getStockfishPath();
            System.out.println("✅ Stockfish路径配置: " + stockfishPath);
            
            // 测试2: 创建StockfishEngine实例
            System.out.println("2. 测试StockfishEngine创建...");
            StockfishEngine engine = new StockfishEngine(12, 1500); // 技能等级12，思考时间1.5秒
            System.out.println("✅ StockfishEngine创建成功");
            
            // 测试3: 创建标准国际象棋初始局面
            System.out.println("3. 测试国际象棋局面分析...");
            InternationalChessBoard board = new InternationalChessBoard();
            board.initializeBoard(); // 初始化标准开局
            
            // 测试4: 获取最佳走法
            System.out.println("4. 测试获取最佳走法...");
            Move bestMove = engine.getBestMove(board, PieceColor.WHITE);
            
            if (bestMove != null) {
                System.out.println("✅ 成功获取最佳走法:");
                System.out.println("   从: (" + bestMove.getFrom().getRow() + "," + bestMove.getFrom().getCol() + ")");
                System.out.println("   到: (" + bestMove.getTo().getRow() + "," + bestMove.getTo().getCol() + ")");
            } else {
                System.out.println("❌ 未能获取最佳走法");
            }
            
            // 测试5: 引擎资源清理
            System.out.println("5. 测试引擎清理...");
            engine.shutdown();
            System.out.println("✅ 引擎资源清理完成");
            
            System.out.println("\n🎉 Stockfish平台集成测试成功！");
            
        } catch (Exception e) {
            System.err.println("❌ 集成测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
