package test.java;

import com.example.chinesechess.ai.PikafishEngine;

/**
 * 测试修改后的Pikafish引擎（跳过EvalFile设置）
 */
public class TestPikafishAfterFix {
    public static void main(String[] args) {
        System.out.println("=== 测试修改后的Pikafish引擎 ===");
        
        // 使用系统PATH中的pikafish
        PikafishEngine engine = new PikafishEngine("pikafish");
        
        // 设置日志回调
        engine.setLogCallback(message -> System.out.println("[LOG] " + message));
        
        // 初始化引擎
        System.out.println("初始化引擎...");
        boolean initSuccess = engine.initialize();
        System.out.println("初始化结果: " + (initSuccess ? "成功" : "失败"));
        
        if (initSuccess) {
            // 获取引擎信息
            System.out.println("\n引擎信息:");
            System.out.println(engine.getEngineInfo());
            
            // 测试简单局面的最佳走法
            System.out.println("\n测试获取最佳走法...");
            String fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
            System.out.println("测试局面FEN: " + fen);
            
            String bestMove = engine.getBestMove(fen, 2000); // 2秒思考时间
            if (bestMove != null && !bestMove.isEmpty()) {
                System.out.println("✅ 成功获取最佳走法: " + bestMove);
            } else {
                System.out.println("❌ 未能获取最佳走法");
            }
            
            // 获取引擎状态
            System.out.println("\n引擎状态: " + engine.getStatus());
        }
        
        // 关闭引擎
        System.out.println("\n关闭引擎...");
        engine.quit();
        System.out.println("测试完成");
    }
}
