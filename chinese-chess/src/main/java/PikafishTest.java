import com.example.chinesechess.ai.DeepSeekPikafishAI;
import com.example.chinesechess.core.PieceColor;

/**
 * 测试Pikafish引擎是否正常工作
 */
public class PikafishTest {
    public static void main(String[] args) {
        System.out.println("🧪 开始测试Pikafish引擎集成");
        System.out.println("=" + "=".repeat(50));
        
        try {
            // 创建DeepSeek-Pikafish混合AI
            System.out.println("🤖 创建DeepSeek-Pikafish混合AI...");
            DeepSeekPikafishAI ai = new DeepSeekPikafishAI(PieceColor.RED, 3, "deepseek-r1:7b");
            
            // 检查Pikafish引擎是否可用
            System.out.println("🔍 检查Pikafish引擎状态...");
            boolean isAvailable = ai.isPikafishAvailable();
            
            if (isAvailable) {
                System.out.println("✅ Pikafish引擎可用");
                System.out.println("ℹ️ " + ai.getEngineStatus());
                
                // 测试API调用
                System.out.println("\n🧪 测试DeepSeek API调用...");
                String testPrompt = "这是一个测试提示，请回复'测试成功'";
                String response = ai.callDeepSeekAPI(testPrompt);
                
                if (response != null && !response.trim().isEmpty()) {
                    System.out.println("✅ DeepSeek API响应: " + response.substring(0, Math.min(100, response.length())) + "...");
                } else {
                    System.out.println("⚠️ DeepSeek API无响应（可能服务未启动）");
                }
                
            } else {
                System.out.println("❌ Pikafish引擎不可用");
                System.out.println("ℹ️ " + ai.getEngineStatus());
                System.out.println("💡 请检查：");
                System.out.println("   1. pikafish命令是否在PATH中可用");
                System.out.println("   2. pikafish.nnue神经网络文件是否存在");
                System.out.println("   3. 配置文件路径是否正确");
            }
            
            // 测试记谱格式建议
            System.out.println("\n📝 记谱格式建议:");
            ai.getNotationFormatSuggestions().forEach(suggestion -> 
                System.out.println("   • " + suggestion)
            );
            
            // 关闭AI释放资源
            ai.shutdown();
            System.out.println("\n🔄 已释放AI资源");
            
        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=" + "=".repeat(50));
        System.out.println("🏁 Pikafish引擎测试完成");
    }
}
