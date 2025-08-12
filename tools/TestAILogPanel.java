import com.example.ui.AILogPanel;
import com.example.ai.SemanticTranslatorService;
import javax.swing.*;
import java.awt.*;

/**
 * 测试AI日志面板功能
 * 验证python-chinese-chess日志是否能正确输出到AI决策日志面板
 */
public class TestAILogPanel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 创建测试窗口
            JFrame frame = new JFrame("AI日志面板测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            
            // 创建AI日志面板
            AILogPanel aiLogPanel = new AILogPanel();
            aiLogPanel.setEnabled(true); // 启用日志面板
            
            // 创建语义翻译服务
            SemanticTranslatorService semanticTranslator = new SemanticTranslatorService();
            semanticTranslator.setAILogPanel(aiLogPanel);
            
            // 添加到窗口
            frame.add(aiLogPanel, BorderLayout.CENTER);
            
            // 创建测试按钮
            JPanel buttonPanel = new JPanel();
            JButton testButton = new JButton("测试python-chinese-chess日志");
            testButton.addActionListener(e -> {
                // 在后台线程中执行测试
                new Thread(() -> {
                    System.out.println("\n🧪 [测试] 开始测试python-chinese-chess日志输出...");
                    
                    // 测试智能解析功能
                    java.util.Map<String, Object> result = semanticTranslator.smartParse("车二进一");
                    
                    System.out.println("🧪 [测试] 测试完成，请查看AI决策日志面板");
                }).start();
            });
            
            buttonPanel.add(testButton);
            frame.add(buttonPanel, BorderLayout.SOUTH);
            
            // 显示窗口
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("🧪 [测试] AI日志面板测试程序已启动");
            System.out.println("🧪 [测试] 点击按钮测试python-chinese-chess日志输出功能");
        });
    }
}