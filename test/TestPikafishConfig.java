import com.example.common.config.ConfigurationManager;
import com.example.chinesechess.ai.PikafishEngine;

public class TestPikafishConfig {
    public static void main(String[] args) {
        System.out.println("测试 Pikafish 配置...");
        
        try {
            // 获取配置管理器
            ConfigurationManager config = ConfigurationManager.getInstance();
            ConfigurationManager.PikafishConfig pikafishConfig = config.getPikafishConfig();
            
            System.out.println("1. Pikafish 配置信息:");
            System.out.println("   引擎路径: " + pikafishConfig.enginePath);
            System.out.println("   神经网络路径: " + pikafishConfig.neuralNetworkPath);
            System.out.println("   线程数: " + pikafishConfig.threads);
            System.out.println("   哈希大小: " + pikafishConfig.hashSize);
            
            // 验证文件存在
            System.out.println("2. 验证文件存在:");
            java.io.File engineFile = new java.io.File(pikafishConfig.enginePath);
            System.out.println("   引擎文件存在: " + engineFile.exists());
            
            java.io.File nnueFile = new java.io.File(pikafishConfig.neuralNetworkPath);
            System.out.println("   神经网络文件存在: " + nnueFile.exists());
            
            // 测试引擎初始化
            System.out.println("3. 测试引擎初始化:");
            PikafishEngine engine = new PikafishEngine(pikafishConfig.enginePath);
            engine.setLogCallback(message -> System.out.println("   [Engine] " + message));
            
            boolean success = engine.initialize();
            System.out.println("   初始化结果: " + success);
            
            if (success) {
                System.out.println("   引擎可用: " + engine.isAvailable());
                System.out.println("✅ Pikafish 测试成功！");
            } else {
                System.out.println("❌ Pikafish 初始化失败");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
