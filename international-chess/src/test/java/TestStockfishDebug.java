import com.example.internationalchess.ai.StockfishEngine;
import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.PieceColor;

/**
 * 用于调试Stockfish引擎问题的测试类
 */
public class TestStockfishDebug {
    
    public static void main(String[] args) {
        System.out.println("🧪 开始测试Stockfish引擎...");
        
        try {
            // 创建引擎实例
            System.out.println("1. 创建StockfishEngine实例...");
            StockfishEngine engine = new StockfishEngine(12, 1000);
            
            // 检查引擎状态
            System.out.println("2. 检查引擎状态: " + engine.isReady());
            
            // 创建棋盘并生成FEN
            System.out.println("3. 创建棋盘并生成FEN...");
            InternationalChessBoard board = new InternationalChessBoard();
            String fen = engine.debugBoardToFEN(board, PieceColor.WHITE);
            System.out.println("4. 生成的FEN: " + fen);
            
            // 检查FEN是否有效
            if (fen == null || fen.trim().isEmpty()) {
                System.err.println("❌ FEN字符串为空!");
                return;
            }
            
            // 尝试获取移动前再次检查状态
            System.out.println("5. 获取移动前引擎状态: " + engine.isReady());
            
            // 获取一步移动
            System.out.println("6. 调用getBestMove...");
            var move = engine.getBestMove(board, PieceColor.WHITE);
            System.out.println("7. 获得的移动: " + (move != null ? move.toString() : "null"));
            
            // 立即检查状态
            System.out.println("8. 获取移动后立即检查引擎状态: " + engine.isReady());
            
            // 如果引擎死了，尝试重新创建一个
            if (!engine.isReady()) {
                System.out.println("9. 引擎已死，尝试创建新引擎...");
                StockfishEngine engine2 = new StockfishEngine(12, 1000);
                System.out.println("10. 新引擎状态: " + engine2.isReady());
                
                // 用新引擎尝试获取移动
                var move2 = engine2.getBestMove(board, PieceColor.WHITE);
                System.out.println("11. 新引擎移动: " + (move2 != null ? move2.toString() : "null"));
                System.out.println("12. 新引擎状态: " + engine2.isReady());
                
                engine2.shutdown();
            }
            
            // 手动关闭原引擎
            System.out.println("13. 手动关闭原引擎...");
            if (engine.isReady()) {
                engine.shutdown();
            }
            
            System.out.println("✅ 测试完成!");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
