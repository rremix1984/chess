import com.example.chinesechess.ai.PikafishEngine;

public class PikafishEngineTest {
    public static void main(String[] args) {
        System.out.println("🧪 测试Pikafish引擎改进效果");
        
        // 使用模拟引擎路径 - 创建一个bash脚本来启动Python
        String mockEnginePath = System.getProperty("user.dir") + "/run_mock.sh";
        System.out.println("使用模拟引擎: " + mockEnginePath);
        
        PikafishEngine engine = new PikafishEngine(mockEnginePath);
        
        // 设置日志回调
        engine.setLogCallback(message -> System.out.println("[Engine] " + message));
        
        // 初始化引擎
        System.out.println("\n🔧 初始化引擎...");
        boolean initialized = engine.initialize();
        
        if (initialized) {
            System.out.println("✅ 引擎初始化成功");
            
            // 测试获取最佳走法，使用标准开局FEN
            String testFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1";
            System.out.println("\n🎯 测试局面: " + testFen);
            
            // 测试不同思考时间
            int[] testTimes = {500, 1000, 2000, 5000};
            
            for (int thinkTime : testTimes) {
                System.out.println("\n--- 测试思考时间: " + thinkTime + "ms ---");
                
                long startTime = System.currentTimeMillis();
                String bestMove = engine.getBestMove(testFen, thinkTime);
                long endTime = System.currentTimeMillis();
                
                System.out.println("结果: " + (bestMove != null ? bestMove : "无结果"));
                System.out.println("实际用时: " + (endTime - startTime) + "ms");
                
                // 显示分析信息
                String analysisInfo = engine.getLastAnalysisInfo();
                if (analysisInfo != null && !analysisInfo.trim().isEmpty()) {
                    System.out.println("分析信息长度: " + analysisInfo.length() + " 字符");
                    // 只显示前200个字符
                    String preview = analysisInfo.length() > 200 ? 
                        analysisInfo.substring(0, 200) + "..." : analysisInfo;
                    System.out.println("分析预览: " + preview.replace("\n", " | "));
                } else {
                    System.out.println("⚠️ 无分析信息");
                }
            }
            
            System.out.println("\n🔄 关闭引擎...");
            engine.quit();
            System.out.println("✅ 测试完成");
            
        } else {
            System.out.println("❌ 引擎初始化失败");
        }
    }
}
