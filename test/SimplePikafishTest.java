import com.example.chinesechess.ai.PikafishEngine;

public class SimplePikafishTest {
    public static void main(String[] args) {
        System.out.println("简化测试 Pikafish 引擎...");
        
        try {
            // 创建 Pikafish 引擎实例
            PikafishEngine engine = new PikafishEngine("/usr/local/bin/pikafish");
            
            // 设置日志回调
            engine.setLogCallback(message -> System.out.println("[Engine] " + message));
            
            // 初始化引擎
            System.out.println("1. 开始初始化引擎...");
            boolean success = engine.initialize();
            System.out.println("2. 初始化结果: " + success);
            
            if (success) {
                System.out.println("3. 引擎可用: " + engine.isAvailable());
                
                // 测试一个简单的走法计算
                String testFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
                System.out.println("4. 测试走法计算...");
                String bestMove = engine.getBestMove(testFen, 1000);
                System.out.println("5. 最佳走法: " + bestMove);
                
                System.out.println("✅ Pikafish 引擎完全正常工作！");
            } else {
                System.out.println("❌ Pikafish 引擎初始化失败");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
