package com.example.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 资源管理工具类
 * 管理线程池、定时器等系统资源
 */
public class ResourceManager {
    
    private static ExecutorService executorService;
    private static ScheduledExecutorService scheduledExecutorService;
    
    private static final int THREAD_POOL_SIZE = 4;
    private static final int SCHEDULED_THREAD_POOL_SIZE = 2;
    
    static {
        initializeExecutors();
    }
    
    /**
     * 初始化线程池
     */
    private static void initializeExecutors() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        scheduledExecutorService = Executors.newScheduledThreadPool(SCHEDULED_THREAD_POOL_SIZE);
    }
    
    /**
     * 获取通用线程池
     * @return ExecutorService
     */
    public static ExecutorService getExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            synchronized (ResourceManager.class) {
                if (executorService == null || executorService.isShutdown()) {
                    executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                }
            }
        }
        return executorService;
    }
    
    /**
     * 获取定时任务线程池
     * @return ScheduledExecutorService
     */
    public static ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService == null || scheduledExecutorService.isShutdown()) {
            synchronized (ResourceManager.class) {
                if (scheduledExecutorService == null || scheduledExecutorService.isShutdown()) {
                    scheduledExecutorService = Executors.newScheduledThreadPool(SCHEDULED_THREAD_POOL_SIZE);
                }
            }
        }
        return scheduledExecutorService;
    }
    
    /**
     * 提交异步任务
     * @param task 要执行的任务
     */
    public static void submitTask(Runnable task) {
        getExecutorService().submit(task);
    }
    
    /**
     * 提交延迟任务
     * @param task 要执行的任务
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public static void scheduleTask(Runnable task, long delay, TimeUnit unit) {
        getScheduledExecutorService().schedule(task, delay, unit);
    }
    
    /**
     * 提交定期执行任务
     * @param task 要执行的任务
     * @param initialDelay 初始延迟
     * @param period 执行间隔
     * @param unit 时间单位
     */
    public static void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        getScheduledExecutorService().scheduleAtFixedRate(task, initialDelay, period, unit);
    }
    
    /**
     * 关闭所有资源
     */
    public static void shutdown() {
        shutdownExecutor(executorService, "ExecutorService");
        shutdownExecutor(scheduledExecutorService, "ScheduledExecutorService");
        
        System.out.println("✅ [资源管理] 所有线程池已关闭");
    }
    
    /**
     * 关闭指定的线程池
     * @param executor 要关闭的线程池
     * @param name 线程池名称
     */
    private static void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                // 等待正在执行的任务完成
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    // 再次等待任务完成
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("⚠️ [资源管理] " + name + " 未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 获取线程池状态信息
     * @return 状态信息字符串
     */
    public static String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== 资源管理器状态 ===\n");
        
        if (executorService != null) {
            status.append("通用线程池: ").append(executorService.isShutdown() ? "已关闭" : "运行中").append("\n");
        } else {
            status.append("通用线程池: 未初始化\n");
        }
        
        if (scheduledExecutorService != null) {
            status.append("定时任务线程池: ").append(scheduledExecutorService.isShutdown() ? "已关闭" : "运行中").append("\n");
        } else {
            status.append("定时任务线程池: 未初始化\n");
        }
        
        status.append("====================");
        return status.toString();
    }
    
    /**
     * 添加JVM关闭钩子，确保资源正确释放
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔄 [资源管理] JVM关闭，正在清理资源...");
            shutdown();
        }));
    }
}
