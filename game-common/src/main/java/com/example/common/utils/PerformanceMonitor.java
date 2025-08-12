package com.example.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 性能监控工具
 * 用于监控AI计算、UI响应和网络请求的性能
 */
public class PerformanceMonitor {
    
    private static final Logger logger = Logger.getLogger(PerformanceMonitor.class.getName());
    private static final Map<String, PerformanceMetric> metrics = new ConcurrentHashMap<>();
    private static final Map<String, Long> activeTimers = new ConcurrentHashMap<>();
    private static final boolean ENABLE_MONITORING = true; // 可通过配置控制
    
    /**
     * 性能指标数据结构
     */
    public static class PerformanceMetric {
        private final String name;
        private final List<Long> executionTimes;
        private long totalTime;
        private long minTime;
        private long maxTime;
        private int count;
        
        public PerformanceMetric(String name) {
            this.name = name;
            this.executionTimes = Collections.synchronizedList(new ArrayList<>());
            this.totalTime = 0;
            this.minTime = Long.MAX_VALUE;
            this.maxTime = 0;
            this.count = 0;
        }
        
        public synchronized void addTime(long time) {
            executionTimes.add(time);
            totalTime += time;
            minTime = Math.min(minTime, time);
            maxTime = Math.max(maxTime, time);
            count++;
            
            // 保持最近100次记录
            if (executionTimes.size() > 100) {
                long removedTime = executionTimes.remove(0);
                totalTime -= removedTime;
                count--;
            }
        }
        
        public double getAverageTime() {
            return count > 0 ? (double) totalTime / count : 0;
        }
        
        public long getMinTime() {
            return minTime == Long.MAX_VALUE ? 0 : minTime;
        }
        
        public long getMaxTime() {
            return maxTime;
        }
        
        public int getCount() {
            return count;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return String.format("%s: 平均=%.2fms, 最小=%dms, 最大=%dms, 次数=%d", 
                    name, getAverageTime(), getMinTime(), getMaxTime(), getCount());
        }
    }
    
    /**
     * 开始计时
     */
    public static void startTimer(String operationName) {
        if (!ENABLE_MONITORING) return;
        
        String timerKey = Thread.currentThread().getId() + "_" + operationName;
        activeTimers.put(timerKey, System.currentTimeMillis());
    }
    
    /**
     * 结束计时并记录性能数据
     */
    public static void endTimer(String operationName) {
        if (!ENABLE_MONITORING) return;
        
        String timerKey = Thread.currentThread().getId() + "_" + operationName;
        Long startTime = activeTimers.remove(timerKey);
        
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            recordMetric(operationName, duration);
            
            // 如果执行时间过长，记录警告
            if (duration > getWarningThreshold(operationName)) {
                ExceptionHandler.logWarning(
                    String.format("操作 '%s' 执行时间过长: %dms", operationName, duration),
                    "性能监控"
                );
            }
        }
    }
    
    /**
     * 记录性能指标
     */
    private static void recordMetric(String operationName, long duration) {
        metrics.computeIfAbsent(operationName, PerformanceMetric::new)
               .addTime(duration);
    }
    
    /**
     * 获取警告阈值
     */
    private static long getWarningThreshold(String operationName) {
        switch (operationName.toLowerCase()) {
            case "ai_calculation":
            case "ai_thinking":
                return 5000; // AI计算超过5秒警告
            case "ui_update":
            case "board_repaint":
                return 100;  // UI更新超过100ms警告
            case "network_request":
            case "ollama_request":
                return 3000; // 网络请求超过3秒警告
            case "file_operation":
                return 1000; // 文件操作超过1秒警告
            default:
                return 2000; // 默认2秒警告
        }
    }
    
    /**
     * 监控AI计算性能
     */
    public static void monitorAICalculation(String aiType, Runnable calculation) {
        String operationName = aiType + "_AI_calculation";
        startTimer(operationName);
        try {
            calculation.run();
        } finally {
            endTimer(operationName);
        }
    }
    
    /**
     * 监控网络请求性能
     */
    public static <T> T monitorNetworkRequest(String requestType, java.util.concurrent.Callable<T> request) throws Exception {
        String operationName = requestType + "_network_request";
        startTimer(operationName);
        try {
            return request.call();
        } finally {
            endTimer(operationName);
        }
    }
    
    /**
     * 监控UI操作性能
     */
    public static void monitorUIOperation(String operationType, Runnable operation) {
        String operationName = operationType + "_ui_operation";
        startTimer(operationName);
        try {
            operation.run();
        } finally {
            endTimer(operationName);
        }
    }
    
    /**
     * 获取性能报告
     */
    public static String getPerformanceReport() {
        if (metrics.isEmpty()) {
            return "暂无性能数据";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=== 性能监控报告 ===\n");
        
        // 按操作类型分组显示
        Map<String, List<PerformanceMetric>> groupedMetrics = groupMetricsByType();
        
        for (Map.Entry<String, List<PerformanceMetric>> entry : groupedMetrics.entrySet()) {
            report.append("\n【").append(entry.getKey()).append("】\n");
            for (PerformanceMetric metric : entry.getValue()) {
                report.append("  ").append(metric.toString()).append("\n");
            }
        }
        
        // 添加性能建议
        report.append("\n").append(getPerformanceSuggestions());
        
        return report.toString();
    }
    
    /**
     * 按类型分组性能指标
     */
    private static Map<String, List<PerformanceMetric>> groupMetricsByType() {
        Map<String, List<PerformanceMetric>> grouped = new ConcurrentHashMap<>();
        
        for (PerformanceMetric metric : metrics.values()) {
            String type = getMetricType(metric.getName());
            grouped.computeIfAbsent(type, k -> new ArrayList<>()).add(metric);
        }
        
        return grouped;
    }
    
    /**
     * 获取指标类型
     */
    private static String getMetricType(String metricName) {
        String name = metricName.toLowerCase();
        if (name.contains("ai") || name.contains("calculation")) {
            return "AI计算";
        } else if (name.contains("ui") || name.contains("repaint") || name.contains("update")) {
            return "UI操作";
        } else if (name.contains("network") || name.contains("request") || name.contains("ollama")) {
            return "网络请求";
        } else if (name.contains("file") || name.contains("io")) {
            return "文件操作";
        } else {
            return "其他操作";
        }
    }
    
    /**
     * 获取性能建议
     */
    private static String getPerformanceSuggestions() {
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("=== 性能优化建议 ===\n");
        
        for (PerformanceMetric metric : metrics.values()) {
            double avgTime = metric.getAverageTime();
            String name = metric.getName();
            
            if (name.toLowerCase().contains("ai") && avgTime > 3000) {
                suggestions.append("• AI计算较慢，建议降低搜索深度或优化算法\n");
            } else if (name.toLowerCase().contains("ui") && avgTime > 50) {
                suggestions.append("• UI响应较慢，建议优化绘制逻辑或使用异步更新\n");
            } else if (name.toLowerCase().contains("network") && avgTime > 2000) {
                suggestions.append("• 网络请求较慢，建议检查网络连接或增加超时设置\n");
            }
        }
        
        if (suggestions.length() == "=== 性能优化建议 ===\n".length()) {
            suggestions.append("• 当前性能表现良好，无需特别优化\n");
        }
        
        return suggestions.toString();
    }
    
    /**
     * 清除性能数据
     */
    public static void clearMetrics() {
        metrics.clear();
        activeTimers.clear();
        ExceptionHandler.logInfo("性能监控数据已清除", "性能监控");
    }
    
    /**
     * 获取特定操作的性能指标
     */
    public static PerformanceMetric getMetric(String operationName) {
        return metrics.get(operationName);
    }
    
    /**
     * 检查是否有性能问题
     */
    public static boolean hasPerformanceIssues() {
        for (PerformanceMetric metric : metrics.values()) {
            if (metric.getAverageTime() > getWarningThreshold(metric.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 记录内存使用情况
     */
    public static void logMemoryUsage(String context) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        String memoryInfo = String.format(
            "内存使用 [%s]: 已用=%.1fMB, 总计=%.1fMB, 最大=%.1fMB, 使用率=%.1f%%",
            context,
            usedMemory / 1024.0 / 1024.0,
            totalMemory / 1024.0 / 1024.0,
            maxMemory / 1024.0 / 1024.0,
            (double) usedMemory / maxMemory * 100
        );
        
        logger.log(Level.INFO, memoryInfo);
        
        // 如果内存使用率过高，发出警告
        if ((double) usedMemory / maxMemory > 0.8) {
            ExceptionHandler.logWarning("内存使用率过高: " + (usedMemory * 100 / maxMemory) + "%", context);
        }
    }
}