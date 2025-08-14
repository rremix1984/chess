package com.example.chinesechess.debug;

import com.example.chinesechess.core.Board;
import com.example.chinesechess.ui.BoardPanel;
import com.example.chinesechess.ui.GameFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化健康检查工具
 * 用于检测游戏启动过程中的各种问题
 */
public class InitializationHealthCheck {
    
    private static final String TAG = "🏥 [健康检查]";
    private final List<String> issues = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> info = new ArrayList<>();
    private final Map<String, Long> timestamps = new HashMap<>();
    
    /**
     * 执行完整的健康检查
     */
    public HealthCheckReport performFullHealthCheck() {
        System.out.println(TAG + " 🩺 开始执行完整健康检查...");
        
        issues.clear();
        warnings.clear();
        info.clear();
        timestamps.clear();
        
        // 记录开始时间
        recordTimestamp("健康检查开始");
        
        // 检查系统环境
        checkSystemEnvironment();
        
        // 检查Java环境
        checkJavaEnvironment();
        
        // 检查Swing环境
        checkSwingEnvironment();
        
        // 检查内存状况
        checkMemoryStatus();
        
        // 检查图形环境
        checkGraphicsEnvironment();
        
        // 检查游戏组件初始化
        checkGameComponentInitialization();
        
        // 检查UI组件层次结构
        checkUIComponentHierarchy();
        
        recordTimestamp("健康检查结束");
        
        HealthCheckReport report = new HealthCheckReport(issues, warnings, info, timestamps);
        
        System.out.println(TAG + " ✅ 健康检查完成");
        System.out.println(TAG + " 发现问题: " + issues.size() + " 个");
        System.out.println(TAG + " 发现警告: " + warnings.size() + " 个");
        
        return report;
    }
    
    /**
     * 检查系统环境
     */
    private void checkSystemEnvironment() {
        recordTimestamp("系统环境检查开始");
        
        try {
            // 检查操作系统
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            info.add("操作系统: " + osName + " " + osVersion);
            
            // 检查屏幕分辨率
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            info.add("屏幕分辨率: " + screenSize.width + "x" + screenSize.height);
            
            if (screenSize.width < 1024 || screenSize.height < 768) {
                warnings.add("屏幕分辨率较小，可能影响游戏显示");
            }
            
            // 检查显示器数量
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            info.add("显示器数量: " + screens.length);
            
        } catch (Exception e) {
            issues.add("系统环境检查失败: " + e.getMessage());
        }
        
        recordTimestamp("系统环境检查结束");
    }
    
    /**
     * 检查Java环境
     */
    private void checkJavaEnvironment() {
        recordTimestamp("Java环境检查开始");
        
        try {
            // 检查Java版本
            String javaVersion = System.getProperty("java.version");
            info.add("Java版本: " + javaVersion);
            
            // 检查Java供应商
            String javaVendor = System.getProperty("java.vendor");
            info.add("Java供应商: " + javaVendor);
            
            // 检查类路径
            String classPath = System.getProperty("java.class.path");
            if (classPath.length() > 1000) {
                info.add("类路径长度: " + classPath.length() + " 字符");
            } else {
                info.add("类路径: " + classPath);
            }
            
            // 检查可用处理器
            int processors = Runtime.getRuntime().availableProcessors();
            info.add("可用处理器数量: " + processors);
            
            if (processors < 2) {
                warnings.add("处理器数量较少，可能影响性能");
            }
            
        } catch (Exception e) {
            issues.add("Java环境检查失败: " + e.getMessage());
        }
        
        recordTimestamp("Java环境检查结束");
    }
    
    /**
     * 检查Swing环境
     */
    private void checkSwingEnvironment() {
        recordTimestamp("Swing环境检查开始");
        
        try {
            // 检查当前线程是否为EDT
            boolean isEDT = SwingUtilities.isEventDispatchThread();
            info.add("当前是否为事件派发线程: " + isEDT);
            
            if (!isEDT) {
                warnings.add("健康检查不在事件派发线程中执行，可能影响UI检查结果");
            }
            
            // 检查Look and Feel
            String lookAndFeel = UIManager.getLookAndFeel().getName();
            info.add("当前外观: " + lookAndFeel);
            
            // 检查系统外观是否可用
            try {
                String systemLAF = UIManager.getSystemLookAndFeelClassName();
                info.add("系统外观: " + systemLAF);
            } catch (Exception e) {
                warnings.add("无法获取系统外观信息: " + e.getMessage());
            }
            
            // 检查字体设置
            Font defaultFont = UIManager.getFont("Label.font");
            if (defaultFont != null) {
                info.add("默认字体: " + defaultFont.getName() + " " + defaultFont.getSize());
            } else {
                warnings.add("无法获取默认字体信息");
            }
            
        } catch (Exception e) {
            issues.add("Swing环境检查失败: " + e.getMessage());
        }
        
        recordTimestamp("Swing环境检查结束");
    }
    
    /**
     * 检查内存状况
     */
    private void checkMemoryStatus() {
        recordTimestamp("内存状况检查开始");
        
        try {
            Runtime runtime = Runtime.getRuntime();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // 堆内存
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            long heapUsed = heapMemory.getUsed();
            long heapMax = heapMemory.getMax();
            long heapCommitted = heapMemory.getCommitted();
            
            info.add("堆内存使用: " + formatMemory(heapUsed) + " / " + formatMemory(heapMax));
            info.add("堆内存已分配: " + formatMemory(heapCommitted));
            
            // 非堆内存
            MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
            long nonHeapUsed = nonHeapMemory.getUsed();
            long nonHeapMax = nonHeapMemory.getMax();
            
            info.add("非堆内存使用: " + formatMemory(nonHeapUsed) + " / " + formatMemory(nonHeapMax));
            
            // 检查内存使用率
            double heapUsagePercent = (double) heapUsed / heapMax * 100;
            if (heapUsagePercent > 80) {
                warnings.add("堆内存使用率过高: " + String.format("%.1f%%", heapUsagePercent));
            } else if (heapUsagePercent > 60) {
                info.add("堆内存使用率: " + String.format("%.1f%%", heapUsagePercent));
            }
            
            // 检查可用内存
            long freeMemory = runtime.freeMemory();
            long totalMemory = runtime.totalMemory();
            long maxMemory = runtime.maxMemory();
            
            info.add("JVM内存: 已用 " + formatMemory(totalMemory - freeMemory) + 
                    " / 总计 " + formatMemory(totalMemory) + 
                    " / 最大 " + formatMemory(maxMemory));
            
            if (maxMemory < 256 * 1024 * 1024) { // 小于256MB
                warnings.add("JVM最大内存较小，可能影响游戏性能");
            }
            
        } catch (Exception e) {
            issues.add("内存状况检查失败: " + e.getMessage());
        }
        
        recordTimestamp("内存状况检查结束");
    }
    
    /**
     * 检查图形环境
     */
    private void checkGraphicsEnvironment() {
        recordTimestamp("图形环境检查开始");
        
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            
            // 检查是否为无头环境
            if (ge.isHeadlessInstance()) {
                issues.add("当前为无头环境，无法显示图形界面");
                return;
            }
            
            // 检查默认屏幕设备
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            info.add("默认屏幕设备: " + defaultScreen.getIDstring());
            
            // 检查颜色深度
            DisplayMode displayMode = defaultScreen.getDisplayMode();
            info.add("显示模式: " + displayMode.getWidth() + "x" + displayMode.getHeight() + 
                    " " + displayMode.getBitDepth() + "位 " + displayMode.getRefreshRate() + "Hz");
            
            if (displayMode.getBitDepth() < 16) {
                warnings.add("颜色深度较低，可能影响显示效果");
            }
            
            // 检查可用字体
            String[] availableFontNames = ge.getAvailableFontFamilyNames();
            info.add("可用字体数量: " + availableFontNames.length);
            
            // 检查中文字体
            boolean hasChineseFont = false;
            for (String fontName : availableFontNames) {
                if (fontName.contains("宋体") || fontName.contains("SimSun") || 
                    fontName.contains("微软雅黑") || fontName.contains("Microsoft YaHei")) {
                    hasChineseFont = true;
                    info.add("找到中文字体: " + fontName);
                    break;
                }
            }
            
            if (!hasChineseFont) {
                warnings.add("未找到常见的中文字体，可能影响中文显示");
            }
            
        } catch (Exception e) {
            issues.add("图形环境检查失败: " + e.getMessage());
        }
        
        recordTimestamp("图形环境检查结束");
    }
    
    /**
     * 检查游戏组件初始化
     */
    private void checkGameComponentInitialization() {
        recordTimestamp("游戏组件初始化检查开始");
        
        try {
            // 测试Board创建
            Board testBoard = new Board();
            if (testBoard.getPiece(0, 0) == null) {
                issues.add("Board初始化失败，棋盘为空");
            } else {
                info.add("Board初始化成功");
            }
            
            // 测试BoardPanel创建
            BoardPanel testBoardPanel = new BoardPanel(testBoard);
            if (testBoardPanel.getSize().width == 0) {
                warnings.add("BoardPanel初始尺寸为0");
            } else {
                info.add("BoardPanel创建成功，初始尺寸: " + testBoardPanel.getSize());
            }
            
            // 测试基本绘制功能
            try {
                java.awt.image.BufferedImage testImage = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D testGraphics = testImage.createGraphics();
                testBoardPanel.paint(testGraphics);
                testGraphics.dispose();
                info.add("棋盘绘制功能正常");
            } catch (Exception e) {
                issues.add("棋盘绘制功能异常: " + e.getMessage());
            }
            
        } catch (Exception e) {
            issues.add("游戏组件初始化检查失败: " + e.getMessage());
        }
        
        recordTimestamp("游戏组件初始化检查结束");
    }
    
    /**
     * 检查UI组件层次结构
     */
    private void checkUIComponentHierarchy() {
        recordTimestamp("UI组件层次结构检查开始");
        
        try {
            // 创建测试窗口来验证布局
            JFrame testFrame = new JFrame("健康检查测试窗口");
            testFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            
            Board testBoard = new Board();
            BoardPanel testBoardPanel = new BoardPanel(testBoard);
            
            testFrame.setLayout(new BorderLayout());
            testFrame.add(testBoardPanel, BorderLayout.CENTER);
            
            // 设置窗口大小
            testFrame.setSize(800, 600);
            
            // 检查组件层次
            Container contentPane = testFrame.getContentPane();
            Component[] components = contentPane.getComponents();
            
            if (components.length == 0) {
                issues.add("测试窗口内容面板中没有组件");
            } else {
                info.add("测试窗口包含 " + components.length + " 个组件");
                
                for (Component comp : components) {
                    info.add("组件: " + comp.getClass().getSimpleName() + 
                            " 可见:" + comp.isVisible() + 
                            " 尺寸:" + comp.getSize());
                }
            }
            
            // 验证布局管理器
            LayoutManager layout = contentPane.getLayout();
            if (layout != null) {
                info.add("布局管理器: " + layout.getClass().getSimpleName());
            } else {
                warnings.add("内容面板没有布局管理器");
            }
            
            // 模拟显示过程
            testFrame.pack();
            Dimension packedSize = testFrame.getSize();
            info.add("Pack后窗口尺寸: " + packedSize);
            
            if (packedSize.width < 400 || packedSize.height < 400) {
                warnings.add("Pack后窗口尺寸过小，可能存在布局问题");
            }
            
            // 清理测试窗口
            testFrame.dispose();
            
        } catch (Exception e) {
            issues.add("UI组件层次结构检查失败: " + e.getMessage());
        }
        
        recordTimestamp("UI组件层次结构检查结束");
    }
    
    /**
     * 记录时间戳
     */
    private void recordTimestamp(String event) {
        timestamps.put(event, System.currentTimeMillis());
    }
    
    /**
     * 格式化内存大小
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 健康检查报告类
     */
    public static class HealthCheckReport {
        private final List<String> issues;
        private final List<String> warnings;
        private final List<String> info;
        private final Map<String, Long> timestamps;
        private final long totalTime;
        
        public HealthCheckReport(List<String> issues, List<String> warnings, List<String> info, Map<String, Long> timestamps) {
            this.issues = new ArrayList<>(issues);
            this.warnings = new ArrayList<>(warnings);
            this.info = new ArrayList<>(info);
            this.timestamps = new HashMap<>(timestamps);
            
            Long startTime = timestamps.get("健康检查开始");
            Long endTime = timestamps.get("健康检查结束");
            this.totalTime = (startTime != null && endTime != null) ? endTime - startTime : 0;
        }
        
        public List<String> getIssues() { return issues; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getInfo() { return info; }
        public Map<String, Long> getTimestamps() { return timestamps; }
        public long getTotalTime() { return totalTime; }
        
        public boolean hasIssues() { return !issues.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        
        /**
         * 生成详细报告
         */
        public String generateDetailedReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== 游戏初始化健康检查报告 ===\n");
            report.append("检查时间: ").append(new java.util.Date()).append("\n");
            report.append("总耗时: ").append(totalTime).append("ms\n\n");
            
            if (!issues.isEmpty()) {
                report.append("🚨 发现问题 (").append(issues.size()).append("个):\n");
                for (String issue : issues) {
                    report.append("  ❌ ").append(issue).append("\n");
                }
                report.append("\n");
            }
            
            if (!warnings.isEmpty()) {
                report.append("⚠️  警告信息 (").append(warnings.size()).append("个):\n");
                for (String warning : warnings) {
                    report.append("  ⚠️  ").append(warning).append("\n");
                }
                report.append("\n");
            }
            
            if (!info.isEmpty()) {
                report.append("ℹ️  系统信息 (").append(info.size()).append("个):\n");
                for (String infoItem : info) {
                    report.append("  ℹ️  ").append(infoItem).append("\n");
                }
                report.append("\n");
            }
            
            // 时间戳信息
            if (!timestamps.isEmpty()) {
                report.append("⏱️  时间戳:\n");
                timestamps.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> {
                        report.append("  - ").append(entry.getKey())
                              .append(": ").append(entry.getValue()).append("\n");
                    });
            }
            
            return report.toString();
        }
        
        /**
         * 打印报告到控制台
         */
        public void printReport() {
            System.out.println(generateDetailedReport());
        }
    }
    
    /**
     * 创建带GUI的健康检查窗口
     */
    public static void showHealthCheckWindow() {
        SwingUtilities.invokeLater(() -> {
            InitializationHealthCheck healthCheck = new InitializationHealthCheck();
            HealthCheckReport report = healthCheck.performFullHealthCheck();
            
            // 创建报告窗口
            JFrame reportFrame = new JFrame("游戏初始化健康检查报告");
            reportFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            reportFrame.setSize(800, 600);
            reportFrame.setLocationRelativeTo(null);
            
            JTextArea reportArea = new JTextArea(report.generateDetailedReport());
            reportArea.setEditable(false);
            reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(reportArea);
            reportFrame.add(scrollPane);
            
            reportFrame.setVisible(true);
        });
    }
    
    /**
     * 主方法，用于独立运行健康检查
     */
    public static void main(String[] args) {
        System.out.println("启动游戏初始化健康检查...");
        
        if (args.length > 0 && "gui".equals(args[0])) {
            showHealthCheckWindow();
        } else {
            SwingUtilities.invokeLater(() -> {
                InitializationHealthCheck healthCheck = new InitializationHealthCheck();
                HealthCheckReport report = healthCheck.performFullHealthCheck();
                report.printReport();
                
                // 如果有严重问题，退出码为1
                if (report.hasIssues()) {
                    System.exit(1);
                }
            });
        }
    }
}
