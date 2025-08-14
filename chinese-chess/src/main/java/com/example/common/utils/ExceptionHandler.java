package com.example.common.utils;

/**
 * 异常处理工具类
 * 提供统一的异常处理和日志记录功能
 */
public class ExceptionHandler {
    
    private static boolean enableVerboseLogging = false;
    
    /**
     * 处理异常，记录日志
     * @param exception 异常对象
     * @param context 异常上下文描述
     */
    public static void handleException(Exception exception, String context) {
        handleException(exception, context, true);
    }
    
    /**
     * 处理异常，记录日志
     * @param exception 异常对象
     * @param context 异常上下文描述
     * @param printStackTrace 是否打印堆栈跟踪
     */
    public static void handleException(Exception exception, String context, boolean printStackTrace) {
        System.err.println("❌ [异常处理] " + context + ": " + exception.getMessage());
        
        if (printStackTrace && enableVerboseLogging) {
            exception.printStackTrace();
        }
    }
    
    /**
     * 处理AI相关异常
     * @param exception 异常对象
     * @param aiType AI类型
     */
    public static void handleAIException(Exception exception, String aiType) {
        System.err.println("🤖❌ [AI异常] " + aiType + " 发生错误: " + exception.getMessage());
        
        if (enableVerboseLogging) {
            exception.printStackTrace();
        }
    }
    
    /**
     * 记录信息日志
     * @param message 日志信息
     * @param context 上下文
     */
    public static void logInfo(String message, String context) {
        System.out.println("ℹ️ [" + context + "] " + message);
    }
    
    /**
     * 记录警告日志
     * @param message 警告信息
     * @param context 上下文
     */
    public static void logWarning(String message, String context) {
        System.out.println("⚠️ [" + context + "] " + message);
    }
    
    /**
     * 记录错误日志
     * @param message 错误信息
     * @param context 上下文
     */
    public static void logError(String message, String context) {
        System.err.println("❌ [" + context + "] " + message);
    }
    
    /**
     * 启用或禁用详细日志记录
     * @param enable 是否启用
     */
    public static void setVerboseLogging(boolean enable) {
        enableVerboseLogging = enable;
    }
}
