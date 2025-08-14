import com.example.gomoku.core.*;
import com.example.gomoku.ai.*;

/**
 * 测试AI vs AI模式
 */
public class test_ai_vs_ai {
    public static void main(String[] args) {
        System.out.println("🎮 开始测试AI vs AI模式...");
        
        try {
            // 创建游戏管理器
            GomokuGameManager gameManager = new GomokuGameManager();
            
            // 设置游戏回调
            gameManager.setGameCallback(new GomokuGameManager.GameCallback() {
                @Override
                public void onGameStateChanged(GameState newState, String winner) {
                    System.out.println("🏁 游戏状态变更: " + newState + ", 获胜者: " + winner);
                }
                
                @Override
                public void onTurnChanged(boolean isBlackTurn, GomokuGameManager.PlayerType currentPlayerType) {
                    System.out.println("🔄 轮次变更: " + (isBlackTurn ? "黑方" : "白方") + " (" + currentPlayerType.displayName + ")");
                }
                
                @Override
                public void onAIThinking(String message) {
                    System.out.println("🤔 AI思考: " + message);
                }
                
                @Override
                public void onAIMove(int row, int col, String analysis) {
                    System.out.println("🎯 AI落子: (" + row + ", " + col + ") - " + analysis);
                }
                
                @Override
                public void onGameModeChanged(GomokuGameManager.GameMode newMode) {
                    System.out.println("📋 游戏模式变更: " + newMode.displayName);
                }
                
                @Override
                public void onError(String error) {
                    System.err.println("❌ 错误: " + error);
                }
            });
            
            // 设置为AI vs AI模式
            gameManager.setGameMode(GomokuGameManager.GameMode.AI_VS_AI, "高级AI", "普通", "qwen2.5:7b");
            
            // 开始游戏
            gameManager.startGame();
            
            // 等待游戏进行
            System.out.println("⏰ 等待AI游戏进行...");
            Thread.sleep(10000); // 等待10秒让AI进行几步
            
            // 打印棋盘状态
            printBoard(gameManager.getBoard());
            
            System.out.println("✅ AI vs AI测试完成");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ 测试失败: " + e.getMessage());
        }
    }
    
    private static void printBoard(GomokuBoard board) {
        System.out.println("📋 当前棋盘状态:");
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                char piece = board.getPiece(row, col);
                if (piece == ' ') {
                    System.out.print("· ");
                } else if (piece == 'B') {
                    System.out.print("● ");
                } else {
                    System.out.print("○ ");
                }
            }
            System.out.println();
        }
    }
}
