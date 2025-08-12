package com.example.common.utils;

import com.example.common.utils.ExceptionHandler;
import com.example.common.config.GameConfig;
import okhttp3.OkHttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.io.IOException;

/**
 * 资源管理器
 * 统一管理应用程序的资源，确保正确释放，防止内存泄漏
 */
public class ResourceManager {
    
    private static ResourceManager instance;
    private final List<OkHttpClient> httpClients = new ArrayList<>();
    private final List<ExecutorService> executorServices = new ArrayList<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    
    private ResourceManager() {
        // 注册JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (Exception e) {
                ExceptionHandler.handleException(e, "JVM关闭钩子执行", false);
            }
        }));
        ExceptionHandler.logInfo("资源管理器已初始化", "资源管理器");
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * 注册HTTP客户端
     */
    public synchronized void registerHttpClient(OkHttpClient client) {
        if (!isShutdown.get() && client != null) {
            httpClients.add(client);
            ExceptionHandler.logInfo("HTTP客户端已注册，当前总数: " + httpClients.size(), "资源管理器");
        } else if (isShutdown.get()) {
            ExceptionHandler.logWarning("资源管理器已关闭，无法注册HTTP客户端", "资源管理器");
        }
    }
    
    /**
     * 注册线程池
     */
    public synchronized void registerExecutorService(ExecutorService executor) {
        if (!isShutdown.get() && executor != null) {
            executorServices.add(executor);
            ExceptionHandler.logInfo("线程池已注册，当前总数: " + executorServices.size(), "资源管理器");
        } else if (isShutdown.get()) {
            ExceptionHandler.logWarning("资源管理器已关闭，无法注册线程池", "资源管理器");
        }
    }
    
    /**
     * 创建并注册HTTP客户端
     */
    public OkHttpClient createHttpClient() {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(GameConfig.NETWORK_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(GameConfig.NETWORK_READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(GameConfig.NETWORK_WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            registerHttpClient(client);
            ExceptionHandler.logInfo("HTTP客户端创建成功", "资源管理器");
            return client;
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "创建HTTP客户端", true);
            throw new RuntimeException("无法创建HTTP客户端", e);
        }
    }
    
    /**
     * 创建并注册线程池
     */
    public ExecutorService createExecutorService(int threadCount) {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            registerExecutorService(executor);
            ExceptionHandler.logInfo("线程池创建成功，线程数: " + threadCount, "资源管理器");
            return executor;
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "创建线程池", true);
            throw new RuntimeException("无法创建线程池", e);
        }
    }
    
    /**
     * 创建并注册调度线程池
     */
    public ScheduledExecutorService createScheduledExecutorService(int corePoolSize) {
        try {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(corePoolSize);
            registerExecutorService(executor);
            ExceptionHandler.logInfo("调度线程池创建成功，核心线程数: " + corePoolSize, "资源管理器");
            return executor;
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "创建调度线程池", true);
            throw new RuntimeException("无法创建调度线程池", e);
        }
    }
    
    /**
     * 关闭所有资源
     */
    public synchronized void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            ExceptionHandler.logInfo("开始关闭资源管理器...", "资源管理器");
            
            int shutdownThreads = 0;
            int shutdownClients = 0;
            
            // 关闭所有线程池
            for (ExecutorService executor : executorServices) {
                try {
                    executor.shutdown();
                    if (!executor.awaitTermination(GameConfig.RESOURCE_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                        ExceptionHandler.logWarning("线程池未在超时时间内关闭，强制关闭", "资源管理器");
                        executor.shutdownNow();
                        // 再次等待一段时间确保强制关闭完成
                        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                            ExceptionHandler.logWarning("线程池强制关闭失败", "资源管理器");
                        }
                    }
                    shutdownThreads++;
                } catch (InterruptedException e) {
                    ExceptionHandler.logWarning("关闭线程池时被中断: " + e.getMessage(), "资源管理器");
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    ExceptionHandler.handleException(e, "关闭线程池", false);
                }
            }
            
            // 关闭所有HTTP客户端
            for (OkHttpClient client : httpClients) {
                try {
                    if (client.dispatcher() != null) {
                        client.dispatcher().executorService().shutdown();
                        try {
                            if (!client.dispatcher().executorService().awaitTermination(2, TimeUnit.SECONDS)) {
                                client.dispatcher().executorService().shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            client.dispatcher().executorService().shutdownNow();
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (client.connectionPool() != null) {
                        client.connectionPool().evictAll();
                    }
                    if (client.cache() != null) {
                        try {
                            client.cache().close();
                        } catch (IOException e) {
                            ExceptionHandler.logWarning("关闭HTTP缓存时出错: " + e.getMessage(), "资源管理器");
                        }
                    }
                    shutdownClients++;
                } catch (Exception e) {
                    ExceptionHandler.handleException(e, "关闭HTTP客户端", false);
                }
            }
            
            // 清空列表
            executorServices.clear();
            httpClients.clear();
            
            ExceptionHandler.logInfo(String.format("资源管理器已关闭 - 线程池: %d, HTTP客户端: %d", 
                    shutdownThreads, shutdownClients), "资源管理器");
        } else {
            ExceptionHandler.logWarning("资源管理器已经关闭", "资源管理器");
        }
    }
    
    /**
     * 检查是否已关闭
     */
    public boolean isShutdown() {
        return isShutdown.get();
    }
    
    /**
     * 获取当前注册的资源数量
     */
    public synchronized int getResourceCount() {
        return httpClients.size() + executorServices.size();
    }
    
    /**
     * 获取资源状态信息
     */
    public synchronized String getResourceStatus() {
        return String.format("HTTP客户端: %d, 线程池: %d, 已关闭: %s", 
                httpClients.size(), executorServices.size(), isShutdown.get());
    }
    
    /**
     * 获取默认的HTTP客户端
     */
    public static OkHttpClient getHttpClient() {
        return getInstance().createHttpClient();
    }
    
    /**
     * 获取默认的线程池
     */
    public static ExecutorService getExecutorService() {
        return getInstance().createExecutorService(GameConfig.THREAD_POOL_CORE_SIZE);
    }
    
    /**
     * 强制关闭所有资源（紧急情况使用）
     */
    public synchronized void forceShutdown() {
        try {
            ExceptionHandler.logWarning("执行强制关闭所有资源", "资源管理器");
            
            // 强制关闭所有线程池
            for (ExecutorService executor : executorServices) {
                try {
                    executor.shutdownNow();
                } catch (Exception e) {
                    ExceptionHandler.handleException(e, "强制关闭线程池", false);
                }
            }
            
            // 强制关闭所有HTTP客户端
            for (OkHttpClient client : httpClients) {
                try {
                    if (client.dispatcher() != null) {
                        client.dispatcher().executorService().shutdownNow();
                    }
                    if (client.connectionPool() != null) {
                        client.connectionPool().evictAll();
                    }
                } catch (Exception e) {
                    ExceptionHandler.handleException(e, "强制关闭HTTP客户端", false);
                }
            }
            
            executorServices.clear();
            httpClients.clear();
            isShutdown.set(true);
            
            ExceptionHandler.logInfo("强制关闭完成", "资源管理器");
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "强制关闭资源", false);
        }
    }
}