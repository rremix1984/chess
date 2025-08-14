import com.example.gomoku.core.GomokuGameManager;
import com.example.gomoku.core.GameState;

public class QuickTestAI {
    public static void main(String[] args) {
        System.out.println("🧪 快速测试AI对AI模式...");
        
        // 创建游戏管理器
        GomokuGameManager gameManager = new GomokuGameManager();
        
        // 设置回调以监控游戏状态
        gameManager.setGameCallback(new GomokuGameManager.GameCallback() {
            @Override
            public void onGameStateChanged(GameState newState, String winner) {
                System.out.println("🏁 游戏状态变更: " + newState + ", 获胜者: " + winner);
            }
            
            @Override
            public void onTurnChanged(boolean isBlackTurn, GomokuGameManager.PlayerType currentPlayerType) {
                System.out.println("🔄 回合变更: " + (isBlackTurn ? "黑方" : "白方") + " (" + currentPlayerType.displayName + ")");
            }
            
            @Override
            public void onAIThinking(String message) {
                System.out.println("💭 " + message);
            }
            
            @Override
            public void onAIMove(int row, int col, String analysis) {
                System.out.println("🎯 " + analysis + " -> (" + row + ", " + col + ")");
            }
            
            @Override
            public void onGameModeChanged(GomokuGameManager.GameMode newMode) {
                System.out.println("📋 模式切换为: " + newMode.displayName);
            }
            
            @Override
            public void onError(String error) {
                System.out.println("❌ 错误: " + error);
            }
        });
        
        // 设置AI对AI模式
        gameManager.setGameMode(GomokuGameManager.GameMode.AI_VS_AI, "大模型AI", "困难", "deepseek-r1:7b");
        
        // 启动游戏
        gameManager.startGame();
        
        // 等待几秒让AI下完几步棋
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 关闭资源
        gameManager.shutdown();
        System.out.println("✅ 测试完成");
    }
}
