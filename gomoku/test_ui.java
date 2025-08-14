import com.example.gomoku.core.*;
import com.example.gomoku.ui.*;
import javax.swing.*;

/**
 * 测试UI是否正确显示棋子
 */
public class test_ui {
    public static void main(String[] args) throws Exception {
        System.out.println("🖼️ 开始测试UI棋子绘制...");
        
        // 创建游戏管理器
        GomokuGameManager gameManager = new GomokuGameManager();
        
        // 手动在棋盘上放几个棋子
        GomokuBoard board = gameManager.getBoard();
        board.setPiece(7, 7, 'B'); // 黑子
        board.setPiece(7, 8, 'W'); // 白子
        board.setPiece(8, 7, 'B'); // 黑子
        
        System.out.println("📋 手动放置棋子完成:");
        for (int row = 6; row <= 9; row++) {
            for (int col = 6; col <= 9; col++) {
                char piece = board.getPiece(row, col);
                if (piece != ' ') {
                    System.out.println("  - (" + row + ", " + col + "): '" + piece + "'");
                }
            }
        }
        
        // 创建棋盘面板
        GomokuBoardPanelAdapter boardPanel = new GomokuBoardPanelAdapter(gameManager);
        
        // 创建窗口
        JFrame frame = new JFrame("UI测试");
        frame.add(boardPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);
        
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            System.out.println("🎯 窗口已显示，开始强制重绘...");
            boardPanel.repaint();
        });
        
        // 等待几秒让用户看到
        Thread.sleep(5000);
        System.out.println("✅ 测试完成，请检查窗口中是否显示了棋子");
    }
}
