package com.example.chinesechess.demo;

import com.example.chinesechess.ui.AILogPanel;
import javax.swing.*;
import java.awt.*;

/**
 * AI决策日志颜色区分功能演示
 * 展示如何为红方、黑方和一般信息使用不同的颜色
 */
public class AILogColorDemo {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowDemo());
    }
    
    private static void createAndShowDemo() {
        JFrame frame = new JFrame("象棋AI决策日志颜色演示");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        // 创建AI日志面板
        AILogPanel aiLogPanel = new AILogPanel();
        aiLogPanel.setEnabled(true); // 启用日志面板
        
        // 创建控制按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton redMoveButton = new JButton("红方AI思考");
        redMoveButton.addActionListener(e -> demonstrateRedPlayerDecision(aiLogPanel));
        
        JButton blackMoveButton = new JButton("黑方AI思考");
        blackMoveButton.addActionListener(e -> demonstrateBlackPlayerDecision(aiLogPanel));
        
        JButton autoDetectButton = new JButton("智能颜色检测");
        autoDetectButton.addActionListener(e -> demonstrateAutoColorDetection(aiLogPanel));
        
        JButton generalInfoButton = new JButton("一般信息");
        generalInfoButton.addActionListener(e -> demonstrateGeneralInfo(aiLogPanel));
        
        JButton clearButton = new JButton("清空日志");
        clearButton.addActionListener(e -> aiLogPanel.clearLog());
        
        buttonPanel.add(redMoveButton);
        buttonPanel.add(blackMoveButton);
        buttonPanel.add(autoDetectButton);
        buttonPanel.add(generalInfoButton);
        buttonPanel.add(clearButton);
        
        // 创建说明面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("颜色说明"));
        
        JLabel redLabel = new JLabel("🔴 红方AI决策 - 红色字体");
        redLabel.setForeground(Color.RED);
        
        JLabel blackLabel = new JLabel("⚫ 黑方AI决策 - 黑色字体");
        blackLabel.setForeground(Color.BLACK);
        
        JLabel greenLabel = new JLabel("🟢 一般信息 - 绿色字体");
        greenLabel.setForeground(new Color(0, 153, 0));
        
        infoPanel.add(redLabel);
        infoPanel.add(blackLabel);
        infoPanel.add(greenLabel);
        
        // 布局
        frame.setLayout(new BorderLayout());
        frame.add(aiLogPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(infoPanel, BorderLayout.SOUTH);
        
        // 添加初始演示日志
        addInitialDemoLogs(aiLogPanel);
        
        frame.setVisible(true);
    }
    
    /**
     * 演示红方AI决策
     */
    private static void demonstrateRedPlayerDecision(AILogPanel aiLogPanel) {
        String[] redDecisions = {
            "我选择炮二平五，控制中路",
            "马二进三，发展子力", 
            "车一平二，准备横车",
            "兵三进一，抢占先手",
            "将六进一，避开攻击"
        };
        
        String decision = redDecisions[(int)(Math.random() * redDecisions.length)];
        aiLogPanel.addRedPlayerDecision(decision);
    }
    
    /**
     * 演示黑方AI决策
     */
    private static void demonstrateBlackPlayerDecision(AILogPanel aiLogPanel) {
        String[] blackDecisions = {
            "我选择炮2平5，与红方炮对峙",
            "马8进7，巩固防守",
            "车9平8，准备反击", 
            "卒7进1，试探对方反应",
            "将5进1，加强王的安全"
        };
        
        String decision = blackDecisions[(int)(Math.random() * blackDecisions.length)];
        aiLogPanel.addBlackPlayerDecision(decision);
    }
    
    /**
     * 演示智能颜色检测
     */
    private static void demonstrateAutoColorDetection(AILogPanel aiLogPanel) {
        String[] mixedDecisions = {
            "红方AI正在分析当前局面...",
            "黑方AI考虑反击策略",
            "红方选择了最优走法",
            "黑方决定保守防守",
            "游戏进行到中局阶段",
            "AI计算完成，准备出手"
        };
        
        String decision = mixedDecisions[(int)(Math.random() * mixedDecisions.length)];
        aiLogPanel.addAIDecisionWithColorDetection(decision);
    }
    
    /**
     * 演示一般信息
     */
    private static void demonstrateGeneralInfo(AILogPanel aiLogPanel) {
        String[] generalInfo = {
            "游戏开始，双方布阵完毕",
            "AI引擎已加载完成",
            "当前回合：第15手",
            "剩余时间：5分30秒",
            "局面评估：微弱优势",
            "建议难度：专家级"
        };
        
        String info = generalInfo[(int)(Math.random() * generalInfo.length)];
        aiLogPanel.addGeneralInfo(info);
    }
    
    /**
     * 添加初始演示日志
     */
    private static void addInitialDemoLogs(AILogPanel aiLogPanel) {
        aiLogPanel.addGeneralInfo("象棋AI决策日志颜色演示系统启动");
        aiLogPanel.addGeneralInfo("请点击上方按钮查看不同颜色的日志效果");
        
        // 添加一些示例日志
        aiLogPanel.addRedPlayerDecision("开局选择中炮对屏风马");
        aiLogPanel.addBlackPlayerDecision("应对中炮，采用屏风马防守");
        aiLogPanel.addGeneralInfo("双方进入经典开局模式");
        
        aiLogPanel.addAIDecisionWithColorDetection("红方AI深度思考中...");
        aiLogPanel.addAIDecisionWithColorDetection("黑方AI评估最佳应手");
        aiLogPanel.addAIDecisionWithColorDetection("系统提示：局面趋于平衡");
    }
}
