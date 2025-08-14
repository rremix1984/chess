package com.example.chinesechess.debug;

import com.example.chinesechess.ui.BoardPanel;
import com.example.chinesechess.ui.GameFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 棋盘可见性监控器
 * 用于诊断和解决棋盘首次登入不可见的问题
 */
public class BoardVisibilityMonitor {
    
    private static final String TAG = "🔍 [棋盘监控]";
    private final ScheduledExecutorService scheduler;
    private final GameFrame gameFrame;
    private final BoardPanel boardPanel;
    private boolean isMonitoring = false;
    private int checkCount = 0;
    
    // 监控状态
    private static class MonitorState {
        boolean frameVisible;
        boolean boardVisible;
        boolean boardPainted;
        Dimension frameSize;
        Dimension boardSize;
        Point boardLocation;
        String layoutInfo;
        long timestamp;
        
        @Override
        public String toString() {
            return String.format("[%d] Frame可见:%s, Board可见:%s, 已绘制:%s, Frame尺寸:%s, Board尺寸:%s, Board位置:%s", 
                timestamp, frameVisible, boardVisible, boardPainted, frameSize, boardSize, boardLocation);
        }
    }
    
    public BoardVisibilityMonitor(BoardPanel boardPanel, GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        this.boardPanel = boardPanel;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // 添加组件监听器
        addComponentListeners();
    }
    
    /**
     * 开始监控棋盘可见性
     */
    public void startMonitoring() {
        if (isMonitoring) {
            return;
        }
        
        isMonitoring = true;
        checkCount = 0;
        
        System.out.println(TAG + " 🚀 开始监控棋盘可见性...");
        
        // 立即检查
        checkBoardVisibility();
        
        // 每秒检查一次，持续30秒
        scheduler.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                checkCount++;
                checkBoardVisibility();
                
                // 30次检查后停止
                if (checkCount >= 30) {
                    stopMonitoring();
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 停止监控
     */
    public void stopMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        isMonitoring = false;
        System.out.println(TAG + " ⏹️ 监控已停止");
    }
    
    /**
     * 检查棋盘可见性状态
     */
    private void checkBoardVisibility() {
        MonitorState state = collectMonitorState();
        
        // 输出监控信息
        System.out.println(TAG + " " + state.toString());
        
        // 检查潜在问题
        checkForIssues(state);
        
        // 如果发现棋盘不可见，尝试修复
        if (shouldAttemptFix(state)) {
            attemptFix();
        }
    }
    
    /**
     * 收集监控状态
     */
    private MonitorState collectMonitorState() {
        MonitorState state = new MonitorState();
        state.timestamp = System.currentTimeMillis() / 1000;
        
        try {
            // 检查GameFrame状态
            state.frameVisible = gameFrame != null && gameFrame.isVisible() && gameFrame.isDisplayable();
            state.frameSize = gameFrame != null ? gameFrame.getSize() : new Dimension(0, 0);
            
            // 检查BoardPanel状态
            if (boardPanel != null) {
                state.boardVisible = boardPanel.isVisible() && boardPanel.isDisplayable();
                state.boardSize = boardPanel.getSize();
                state.boardLocation = boardPanel.getLocation();
                state.boardPainted = boardPanel.getWidth() > 0 && boardPanel.getHeight() > 0;
                
                // 获取布局信息
                Container parent = boardPanel.getParent();
                if (parent != null) {
                    LayoutManager layout = parent.getLayout();
                    state.layoutInfo = layout != null ? layout.getClass().getSimpleName() : "null";
                }
            } else {
                state.boardVisible = false;
                state.boardPainted = false;
                state.boardSize = new Dimension(0, 0);
                state.boardLocation = new Point(0, 0);
                state.layoutInfo = "BoardPanel为null";
            }
        } catch (Exception e) {
            System.err.println(TAG + " ❌ 收集状态时发生错误: " + e.getMessage());
        }
        
        return state;
    }
    
    /**
     * 检查潜在问题
     */
    private void checkForIssues(MonitorState state) {
        if (!state.frameVisible) {
            System.out.println(TAG + " ⚠️ 主窗口不可见");
        }
        
        if (!state.boardVisible) {
            System.out.println(TAG + " ⚠️ 棋盘面板不可见");
        }
        
        if (!state.boardPainted) {
            System.out.println(TAG + " ⚠️ 棋盘尺寸为0，可能未正确布局");
        }
        
        if (state.frameSize.width < 800 || state.frameSize.height < 600) {
            System.out.println(TAG + " ⚠️ 窗口尺寸过小: " + state.frameSize);
        }
        
        if (state.boardSize.width < 400 || state.boardSize.height < 400) {
            System.out.println(TAG + " ⚠️ 棋盘尺寸过小: " + state.boardSize);
        }
    }
    
    /**
     * 判断是否应该尝试修复
     */
    private boolean shouldAttemptFix(MonitorState state) {
        // 如果主窗口可见但棋盘不可见，或者棋盘尺寸为0
        return state.frameVisible && (!state.boardVisible || !state.boardPainted);
    }
    
    /**
     * 尝试修复棋盘可见性问题
     */
    private void attemptFix() {
        System.out.println(TAG + " 🔧 尝试修复棋盘可见性问题...");
        
        try {
            // 修复1: 强制设置棋盘可见
            if (boardPanel != null) {
                boardPanel.setVisible(true);
                boardPanel.setOpaque(true);
                
                // 设置合理的最小尺寸
                Dimension minSize = new Dimension(600, 600);
                boardPanel.setMinimumSize(minSize);
                boardPanel.setPreferredSize(minSize);
                
                System.out.println(TAG + " ✅ 设置棋盘可见性和尺寸");
            }
            
            // 修复2: 刷新布局
            if (gameFrame != null) {
                gameFrame.revalidate();
                gameFrame.repaint();
                System.out.println(TAG + " ✅ 刷新主窗口布局");
            }
            
            // 修复3: 强制重绘棋盘
            if (boardPanel != null) {
                boardPanel.revalidate();
                boardPanel.repaint();
                System.out.println(TAG + " ✅ 重绘棋盘");
            }
            
            // 修复4: 延迟再次检查
            Timer delayedCheck = new Timer(500, e -> {
                System.out.println(TAG + " 🔄 修复后状态检查:");
                checkBoardVisibility();
            });
            delayedCheck.setRepeats(false);
            delayedCheck.start();
            
        } catch (Exception e) {
            System.err.println(TAG + " ❌ 修复过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 添加组件监听器
     */
    private void addComponentListeners() {
        if (gameFrame != null) {
            gameFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    System.out.println(TAG + " 📺 主窗口已显示");
                }
                
                @Override
                public void componentResized(ComponentEvent e) {
                    System.out.println(TAG + " 📐 主窗口尺寸变化: " + e.getComponent().getSize());
                }
            });
        }
        
        if (boardPanel != null) {
            boardPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    System.out.println(TAG + " 🏮 棋盘已显示");
                }
                
                @Override
                public void componentResized(ComponentEvent e) {
                    System.out.println(TAG + " 🏮 棋盘尺寸变化: " + e.getComponent().getSize());
                }
                
                @Override
                public void componentMoved(ComponentEvent e) {
                    System.out.println(TAG + " 🏮 棋盘位置变化: " + e.getComponent().getLocation());
                }
            });
        }
    }
    
    /**
     * 创建诊断报告
     */
    public String generateDiagnosticReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 棋盘可见性诊断报告 ===\n");
        
        MonitorState state = collectMonitorState();
        report.append("当前状态: ").append(state.toString()).append("\n");
        
        // 系统信息
        report.append("系统信息:\n");
        report.append("  - Java版本: ").append(System.getProperty("java.version")).append("\n");
        report.append("  - 操作系统: ").append(System.getProperty("os.name")).append("\n");
        report.append("  - 屏幕尺寸: ");
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            report.append(screenSize.width).append("x").append(screenSize.height).append("\n");
        } catch (Exception e) {
            report.append("获取失败\n");
        }
        
        // 组件树信息
        if (gameFrame != null) {
            report.append("组件树:\n");
            appendComponentTree(report, gameFrame, "  ");
        }
        
        return report.toString();
    }
    
    /**
     * 递归添加组件树信息
     */
    private void appendComponentTree(StringBuilder report, Component component, String indent) {
        if (component == null) return;
        
        report.append(indent)
              .append(component.getClass().getSimpleName())
              .append(" [可见:").append(component.isVisible())
              .append(", 尺寸:").append(component.getSize())
              .append("]\n");
        
        if (component instanceof Container && indent.length() < 12) { // 限制递归深度
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                appendComponentTree(report, child, indent + "  ");
            }
        }
    }
    
    /**
     * 执行可见性检查（公共方法）
     */
    public void performVisibilityCheck() {
        checkBoardVisibility();
    }
    
    /**
     * 尝试修复可见性问题（公共方法）
     */
    public void attemptFixes() {
        attemptFix();
    }
    
    /**
     * 关闭监控器
     */
    public void shutdown() {
        stopMonitoring();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
