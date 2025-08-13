import com.example.go.KataGoAI;
import com.example.common.utils.ExceptionHandler;

public class TestKataGoAI {
    public static void main(String[] args) {
        System.out.println("测试 KataGo AI 初始化...");
        
        try {
            // 创建 KataGo AI 实例
            KataGoAI kataGoAI = new KataGoAI(3);
            
            System.out.println("1. 初始化 KataGo 引擎...");
            boolean success = kataGoAI.initializeEngine();
            
            if (success) {
                System.out.println("✅ KataGo AI 初始化成功！");
                System.out.println("引擎是否就绪: " + kataGoAI.isEngineReady());
                
                // 获取引擎信息
                System.out.println("2. 引擎信息:");
                System.out.println(kataGoAI.getEngineInfo());
                
                // 关闭引擎
                System.out.println("3. 关闭引擎...");
                kataGoAI.shutdownEngine();
                System.out.println("✅ 测试完成！");
            } else {
                System.out.println("❌ KataGo AI 初始化失败");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
