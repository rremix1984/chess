package com.example.chinesechess.test;

import com.example.chinesechess.core.Board;
import com.example.chinesechess.ui.BoardPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 棋盘渲染测试工具
 * 用于测试和验证棋盘绘制功能
 */
public class BoardRenderTest {
    
    private static final String TAG = "🎨 [渲染测试]";
    
    /**
     * 测试棋盘基本渲染
     */
    public static void testBasicRender() {
        System.out.println(TAG + " 开始基本渲染测试...");
        
        try {
            // 创建棋盘和面板
            Board board = new Board();
            BoardPanel boardPanel = new BoardPanel(board);
            
            // 设置面板尺寸
            boardPanel.setSize(800, 800);
            boardPanel.setPreferredSize(new Dimension(800, 800));
            
            // 创建测试窗口
            JFrame testFrame = new JFrame("棋盘渲染测试");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.add(boardPanel);
            testFrame.pack();
            testFrame.setLocationRelativeTo(null);
            testFrame.setVisible(true);
            
            // 等待渲染完成
            SwingUtilities.invokeAndWait(() -> {
                boardPanel.revalidate();
                boardPanel.repaint();
            });
            
            System.out.println(TAG + " ✅ 基本渲染测试完成");
            
            // 5秒后自动关闭
            Timer closeTimer = new Timer(5000, e -> testFrame.dispose());
            closeTimer.setRepeats(false);
            closeTimer.start();
            
        } catch (Exception e) {
            System.err.println(TAG + " ❌ 基本渲染测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试棋盘截图功能
     */
    public static void testBoardScreenshot() {
        System.out.println(TAG + " 开始截图测试...");
        
        try {
            // 创建棋盘和面板
            Board board = new Board();
            BoardPanel boardPanel = new BoardPanel(board);
            
            // 设置面板尺寸
            Dimension size = new Dimension(800, 800);
            boardPanel.setSize(size);
            boardPanel.setPreferredSize(size);
            
            // 创建离屏图像
            BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // 启用抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 设置白色背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 800, 800);
            
            // 绘制棋盘
            boardPanel.paint(g2d);
            g2d.dispose();
            
            // 保存截图
            String filename = "board_screenshot_" + System.currentTimeMillis() + ".png";
            File outputFile = new File(filename);
            ImageIO.write(image, "PNG", outputFile);
            
            System.out.println(TAG + " ✅ 截图已保存: " + outputFile.getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println(TAG + " ❌ 截图测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试不同尺寸的棋盘渲染
     */
    public static void testDifferentSizes() {
        System.out.println(TAG + " 开始多尺寸渲染测试...");
        
        int[] sizes = {400, 600, 800, 1000};
        
        for (int size : sizes) {
            try {
                System.out.println(TAG + " 测试尺寸: " + size + "x" + size);
                
                Board board = new Board();
                BoardPanel boardPanel = new BoardPanel(board);
                
                Dimension dimension = new Dimension(size, size);
                boardPanel.setSize(dimension);
                boardPanel.setPreferredSize(dimension);
                
                // 创建测试窗口
                JFrame testFrame = new JFrame("尺寸测试 " + size + "x" + size);
                testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                testFrame.add(boardPanel);
                testFrame.pack();
                testFrame.setLocationRelativeTo(null);
                testFrame.setVisible(true);
                
                // 等待2秒后关闭
                Timer closeTimer = new Timer(2000, e -> {
                    testFrame.dispose();
                    System.out.println(TAG + " 尺寸 " + size + " 测试完成");
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
                
                // 等待关闭
                Thread.sleep(2500);
                
            } catch (Exception e) {
                System.err.println(TAG + " 尺寸 " + size + " 测试失败: " + e.getMessage());
            }
        }
        
        System.out.println(TAG + " ✅ 多尺寸渲染测试完成");
    }
    
    /**
     * 性能测试
     */
    public static void testRenderPerformance() {
        System.out.println(TAG + " 开始性能测试...");
        
        try {
            Board board = new Board();
            BoardPanel boardPanel = new BoardPanel(board);
            
            Dimension size = new Dimension(800, 800);
            boardPanel.setSize(size);
            boardPanel.setPreferredSize(size);
            
            BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
            
            int renderCount = 100;
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < renderCount; i++) {
                Graphics2D g2d = image.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, 800, 800);
                
                boardPanel.paint(g2d);
                g2d.dispose();
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double avgTime = (double) totalTime / renderCount;
            
            System.out.println(TAG + " 性能测试结果:");
            System.out.println(TAG + " - 总渲染次数: " + renderCount);
            System.out.println(TAG + " - 总用时: " + totalTime + "ms");
            System.out.println(TAG + " - 平均用时: " + String.format("%.2f", avgTime) + "ms");
            System.out.println(TAG + " - 预估FPS: " + String.format("%.1f", 1000.0 / avgTime));
            
        } catch (Exception e) {
            System.err.println(TAG + " ❌ 性能测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 内存使用测试
     */
    public static void testMemoryUsage() {
        System.out.println(TAG + " 开始内存使用测试...");
        
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存
        System.gc(); // 强制垃圾回收
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println(TAG + " 初始内存使用: " + formatMemory(initialMemory));
        
        try {
            // 创建多个棋盘实例
            int boardCount = 10;
            BoardPanel[] panels = new BoardPanel[boardCount];
            
            for (int i = 0; i < boardCount; i++) {
                Board board = new Board();
                panels[i] = new BoardPanel(board);
                panels[i].setSize(800, 800);
            }
            
            // 记录创建后内存
            System.gc();
            long afterCreateMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println(TAG + " 创建" + boardCount + "个棋盘后内存使用: " + formatMemory(afterCreateMemory));
            System.out.println(TAG + " 增加内存: " + formatMemory(afterCreateMemory - initialMemory));
            
            // 进行渲染测试
            BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
            
            for (int i = 0; i < boardCount; i++) {
                for (int j = 0; j < 10; j++) {
                    Graphics2D g2d = image.createGraphics();
                    panels[i].paint(g2d);
                    g2d.dispose();
                }
            }
            
            // 记录渲染后内存
            System.gc();
            long afterRenderMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println(TAG + " 渲染测试后内存使用: " + formatMemory(afterRenderMemory));
            
            // 清理资源
            panels = null;
            System.gc();
            
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println(TAG + " 清理后内存使用: " + formatMemory(finalMemory));
            
        } catch (Exception e) {
            System.err.println(TAG + " ❌ 内存测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 格式化内存大小显示
     */
    private static String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        System.out.println(TAG + " 🚀 开始运行所有渲染测试...");
        System.out.println("==================================================");
        
        // 基本渲染测试
        testBasicRender();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 截图测试
        testBoardScreenshot();
        
        // 性能测试
        testRenderPerformance();
        
        // 内存测试
        testMemoryUsage();
        
        // 多尺寸测试
        testDifferentSizes();
        
        System.out.println("==================================================");
        System.out.println(TAG + " 🎉 所有渲染测试完成!");
    }
    
    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            runAllTests();
        });
    }
}
