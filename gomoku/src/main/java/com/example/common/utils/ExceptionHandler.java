package com.example.common.utils;

/**
 * 异常处理工具类
 */
public class ExceptionHandler {
    
    /**
     * 处理异常
     */
    public static void handleException(Exception e, String operation, boolean showDialog) {
        System.err.println("❌ 异常发生在操作: " + operation);
        e.printStackTrace();
        
        if (showDialog) {
            // 这里可以添加对话框显示
            System.err.println("严重错误: " + e.getMessage());
        }
    }
    
    /**
     * 记录信息日志
     */
    public static void logInfo(String message, String source) {
        System.out.println("ℹ️ [" + source + "] " + message);
    }
    
    /**
     * 记录警告日志
     */
    public static void logWarning(String message, String source) {
        System.out.println("⚠️ [" + source + "] " + message);
    }
    
    /**
     * 记录错误日志
     */
    public static void logError(String message, String source) {
        System.err.println("❌ [" + source + "] " + message);
    }
}
