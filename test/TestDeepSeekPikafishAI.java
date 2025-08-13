import com.example.chinesechess.ai.DeepSeekPikafishAI;
import com.example.chinesechess.core.PieceColor;

public class TestDeepSeekPikafishAI {
    public static void main(String[] args) {
        System.out.println("测试 DeepSeekPikafishAI 初始化...");
        
        try {
            // 创建 DeepSeekPikafishAI 实例
            DeepSeekPikafishAI ai = new DeepSeekPikafishAI(
                PieceColor.RED, 
                3, 
                "deepseek-r1:7b"
            );
            
            System.out.println("✅ DeepSeekPikafishAI 创建成功！");
            
        } catch (Exception e) {
            System.out.println("❌ DeepSeekPikafishAI 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
