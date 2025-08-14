package com.example.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 性能监控工具类
 * 用于监控应用程序的性能指标
 */
public class PerformanceMonitor {
    
    private static final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> totalTimes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> callCounts = new ConcurrentHashMap<>();
    
    private static boolean enabled = true;
    
    /**
     * 开始计时
     * @param operation 操作名称
     */
    public static void startTimer(String operation) {
        if (!enabled) return;
        
        startTimes.put(operation, System.currentTimeMillis());
    }
    
    /**
     * 结束计时并记录
     * @param operation 操作名称
     */
    public static void endTimer(String operation) {
        if (!enabled) return;
        
        Long startTime = startTimes.remove(operation);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            totalTimes.merge(operation, duration, Long::sum);
            callCounts.merge(operation, 1, Integer::sum);
            
            // 如果操作耗时较长，输出日志
            if (duration > 1000) {
                System.out.println("🐌 [性能监控] " + operation + " 耗时: " + duration + "ms");
            }
        }
    }
    
    /**
     * 监控UI操作性能
     * @param operationName 操作名称
     * @param operation 要执行的操作
     */
    public static void monitorUIOperation(String operationName, Runnable operation) {
        if (!enabled) {
            operation.run();
            return;
        }
        
        startTimer(operationName);
        try {
            operation.run();
        } finally {
            endTimer(operationName);
        }
    }
    
    /**
     * 获取操作的平均耗时
     * @param operation 操作名称
     * @return 平均耗时（毫秒）
     */
    public static double getAverageTime(String operation) {
        Long totalTime = totalTimes.get(operation);
        Integer count = callCounts.get(operation);
        
        if (totalTime != null && count != null && count > 0) {
            return (double) totalTime / count;
        }
        
        return 0.0;
    }
    
    /**
     * 获取操作的总耗时
     * @param operation 操作名称
     * @return 总耗时（毫秒）
     */
    public static long getTotalTime(String operation) {
        return totalTimes.getOrDefault(operation, 0L);
    }
    
    /**
     * 获取操作的调用次数
     * @param operation 操作名称
     * @return 调用次数
     */
    public static int getCallCount(String operation) {
        return callCounts.getOrDefault(operation, 0);
    }
    
    /**
     * 打印性能统计报告
     */
    public static void printReport() {
        if (!enabled) return;
        
        System.out.println("\n=== 性能监控报告 ===");
        for (String operation : totalTimes.keySet()) {
            long totalTime = getTotalTime(operation);
            int callCount = getCallCount(operation);
            double avgTime = getAverageTime(operation);
            
            System.out.printf("📊 %s: 总计%dms, 调用%d次, 平均%.2fms\n", 
                operation, totalTime, callCount, avgTime);
        }
        System.out.println("========================\n");
    }
    
    /**
     * 清除所有性能统计数据
     */
    public static void clear() {
        startTimes.clear();
        totalTimes.clear();
        callCounts.clear();
    }
    
    /**
     * 启用或禁用性能监控
     * @param enable 是否启用
     */
    public static void setEnabled(boolean enable) {
        enabled = enable;
    }
    
    /**
     * 检查性能监控是否启用
     * @return 是否启用
     */
    public static boolean isEnabled() {
        return enabled;
    }
}
