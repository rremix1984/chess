package com.example.common.utils;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 统一异常处理器
 * 提供友好的错误提示和完整的错误日志记录
 */
public class ExceptionHandler {
    
    private static final Logger logger = Logger.getLogger(ExceptionHandler.class.getName());
    private static final Map<Class<? extends Throwable>, String> errorMessages = new ConcurrentHashMap<>();
    
    static {
        // 初始化错误消息映射
        initializeErrorMessages();
    }
    
    /**
     * 初始化错误消息映射
     */
    private static void initializeErrorMessages() {
        errorMessages.put(java.net.ConnectException.class, "无法连接到AI服务，请检查Ollama是否正在运行");
        errorMessages.put(java.net.SocketTimeoutException.class, "AI服务响应超时，请稍后重试");
        errorMessages.put(java.io.IOException.class, "网络连接出现问题，请检查网络设置");
        errorMessages.put(InterruptedException.class, "操作被中断，请重新尝试");
        errorMessages.put(IllegalArgumentException.class, "输入参数不正确，请检查输入内容");
        errorMessages.put(NullPointerException.class, "程序内部错误，请重启应用");
        errorMessages.put(OutOfMemoryError.class, "内存不足，请关闭其他程序后重试");
        errorMessages.put(RuntimeException.class, "程序运行时出现错误，请重试");
    }
    
    /**
     * 处理异常并显示用户友好的错误信息
     */
    public static void handleException(Exception e, String context) {
        handleException(e, context, true);
    }
    
    /**
     * 处理异常
     * @param e 异常对象
     * @param context 异常发生的上下文
     * @param showDialog 是否显示错误对话框
     */
    public static void handleException(Exception e, String context, boolean showDialog) {
        // 记录详细的错误日志
        logException(e, context);
        
        if (showDialog) {
            // 显示用户友好的错误信息
            String userMessage = getUserFriendlyMessage(e, context);
            showErrorDialog(userMessage, e);
        }
    }
    
    /**
     * 处理AI相关异常
     */
    public static void handleAIException(Exception e, String aiType) {
        String context = aiType + "AI计算";
        String userMessage = String.format("%s出现问题：%s\n\n建议：\n1. 检查网络连接\n2. 重启AI服务\n3. 切换到其他AI类型", 
                aiType, getUserFriendlyMessage(e, context));
        
        logException(e, context);
        showErrorDialog(userMessage, e);
    }
    
    /**
     * 处理网络异常
     */
    public static void handleNetworkException(Exception e, String operation) {
        String context = "网络操作: " + operation;
        String userMessage = String.format("网络连接失败：%s\n\n建议：\n1. 检查网络连接\n2. 确认服务器状态\n3. 稍后重试", 
                getUserFriendlyMessage(e, context));
        
        logException(e, context);
        showErrorDialog(userMessage, e);
    }
    
    /**
     * 处理文件操作异常
     */
    public static void handleFileException(Exception e, String fileName) {
        String context = "文件操作: " + fileName;
        String userMessage = String.format("文件操作失败：%s\n\n建议：\n1. 检查文件权限\n2. 确认文件路径\n3. 检查磁盘空间", 
                getUserFriendlyMessage(e, context));
        
        logException(e, context);
        showErrorDialog(userMessage, e);
    }
    
    /**
     * 记录异常日志
     */
    private static void logException(Exception e, String context) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        
        String logMessage = String.format("异常发生在: %s\n异常类型: %s\n异常消息: %s\n堆栈跟踪:\n%s", 
                context, e.getClass().getSimpleName(), e.getMessage(), sw.toString());
        
        logger.log(Level.SEVERE, logMessage);
        
        // 同时输出到控制台（开发调试用）
        System.err.println("❌ " + context + " 发生异常: " + e.getMessage());
        if (logger.isLoggable(Level.FINE)) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取用户友好的错误消息
     */
    private static String getUserFriendlyMessage(Exception e, String context) {
        // 首先尝试从映射中获取特定的错误消息
        String message = errorMessages.get(e.getClass());
        if (message != null) {
            return message;
        }
        
        // 尝试父类匹配
        for (Map.Entry<Class<? extends Throwable>, String> entry : errorMessages.entrySet()) {
            if (entry.getKey().isAssignableFrom(e.getClass())) {
                return entry.getValue();
            }
        }
        
        // 根据异常消息生成友好提示
        String originalMessage = e.getMessage();
        if (originalMessage != null) {
            if (originalMessage.contains("Connection refused")) {
                return "服务连接被拒绝，请检查服务是否启动";
            } else if (originalMessage.contains("timeout")) {
                return "操作超时，请稍后重试";
            } else if (originalMessage.contains("permission")) {
                return "权限不足，请检查文件权限";
            } else if (originalMessage.contains("not found")) {
                return "找不到指定的资源或文件";
            }
        }
        
        // 默认消息
        return "操作失败，请重试。如果问题持续存在，请联系技术支持";
    }
    
    /**
     * 显示错误对话框
     */
    private static void showErrorDialog(String message, Exception e) {
        SwingUtilities.invokeLater(() -> {
            String title = "操作失败";
            
            // 创建详细信息（可选显示）
            String details = String.format("技术详情：\n异常类型: %s\n异常消息: %s", 
                    e.getClass().getSimpleName(), e.getMessage());
            
            // 显示基本错误信息
            Object[] options = {"确定", "查看详情"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    message,
                    title,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            
            // 如果用户选择查看详情
            if (choice == 1) {
                JOptionPane.showMessageDialog(
                        null,
                        details,
                        "错误详情",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }
    
    /**
     * 显示警告信息
     */
    public static void showWarning(String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title != null ? title : "警告",
                    JOptionPane.WARNING_MESSAGE
            );
        });
    }
    
    /**
     * 显示信息提示
     */
    public static void showInfo(String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title != null ? title : "提示",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
    
    /**
     * 记录警告日志
     */
    public static void logWarning(String message, String context) {
        String logMessage = String.format("警告 - %s: %s", context, message);
        logger.log(Level.WARNING, logMessage);
        System.out.println("⚠️ " + logMessage);
    }
    
    /**
     * 记录信息日志
     */
    public static void logInfo(String message, String context) {
        String logMessage = String.format("信息 - %s: %s", context, message);
        logger.log(Level.INFO, logMessage);
        System.out.println("ℹ️ " + logMessage);
    }
    
    /**
     * 记录错误日志
     */
    public static void logError(String message, String context) {
        String logMessage = String.format("错误 - %s: %s", context, message);
        logger.log(Level.SEVERE, logMessage);
        System.out.println("❌ " + logMessage);
    }
    
    /**
     * 记录调试日志
     */
    public static void logDebug(String context, String message) {
        String logMessage = String.format("调试 - %s: %s", context, message);
        logger.log(Level.FINE, logMessage);
        if (logger.isLoggable(Level.FINE)) {
            System.out.println("🔍 " + logMessage);
        }
    }
    
    /**
     * 添加自定义错误消息映射
     */
    public static void addErrorMessage(Class<? extends Throwable> exceptionClass, String message) {
        errorMessages.put(exceptionClass, message);
    }
}