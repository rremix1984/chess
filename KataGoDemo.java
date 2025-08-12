import com.github.chensilong78.engine.KataGoEngine;

/**
 * KataGo演示程序
 */
public class KataGoDemo {
    public static void main(String[] args) {
        System.out.println("=== KataGo AI 引擎演示 ===");
        
        KataGoEngine engine = new KataGoEngine();
        
        try {
            System.out.println("正在初始化KataGo引擎...");
            boolean initialized = engine.initialize();
            
            if (!initialized) {
                System.out.println("❌ KataGo初始化失败！");
                System.out.println("请检查:");
                System.out.println("1. KataGo是否正确安装: /opt/homebrew/bin/katago");
                System.out.println("2. 模型文件是否存在: ~/.katago/models/");
                System.out.println("3. 配置文件是否正确: ~/.katago/default_gtp.cfg");
                return;
            }
            
            System.out.println("✅ KataGo引擎初始化成功！");
            
            // 获取版本信息
            String version = engine.getVersion();
            System.out.println("📋 版本信息: " + version);
            
            // 设置棋盘
            System.out.println("🎯 设置19x19棋盘...");
            engine.setBoardSize(19);
            engine.clearBoard();
            
            // 模拟一局简单的游戏
            System.out.println("🎮 开始模拟游戏...");
            
            // 黑棋先手
            System.out.println("⚫ 黑棋下在D4");
            engine.playMove("black", "D4");
            
            // 白棋回应
            System.out.println("⚪ 白棋下在Q16");
            engine.playMove("white", "Q16");
            
            // AI建议黑棋下一步
            System.out.println("🤖 正在计算AI建议的下一步...");
            engine.setVisits(400); // 设置较少的搜索次数以加快速度
            
            String aiMove = engine.generateMove("black");
            if (aiMove != null && !aiMove.equals("resign")) {
                System.out.println("💡 AI建议黑棋下在: " + aiMove);
            } else {
                System.out.println("⚠️  AI无法生成有效着法");
            }
            
            System.out.println("✨ 演示完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 运行过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理资源
            System.out.println("🔧 正在关闭KataGo引擎...");
            engine.shutdown();
            System.out.println("👋 再见！");
        }
    }
}
