package com.example.common.utils;

import com.example.common.config.ConfigurationManager;

/**
 * 简化的日志工具类
 * 提供统一的日志输出控制，减少控制台噪音
 */
public class Logger {
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    
    public static void info(String message) {
        if (config.getBoolean("logging.enabled", true) && 
            isLevelEnabled("INFO")) {
            System.out.println("[INFO] " + message);
        }
    }
    
    public static void error(String message) {
        if (config.getBoolean("logging.enabled", true) && 
            isLevelEnabled("ERROR")) {
            System.err.println("[ERROR] " + message);
        }
    }
    
    public static void debug(String message) {
        if (config.getBoolean("debug.enabled", false) && 
            isLevelEnabled("DEBUG")) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    public static void warn(String message) {
        if (config.getBoolean("logging.enabled", true) && 
            isLevelEnabled("WARN")) {
            System.out.println("[WARN] " + message);
        }
    }
    
    private static boolean isLevelEnabled(String level) {
        String configLevel = config.getString("logging.level", "INFO");
        switch (configLevel.toUpperCase()) {
            case "DEBUG": return true;
            case "INFO": return !"DEBUG".equals(level);
            case "WARN": return "WARN".equals(level) || "ERROR".equals(level);
            case "ERROR": return "ERROR".equals(level);
            default: return true;
        }
    }
    
    // AI相关日志，可以独立控制
    public static void aiDebug(String message) {
        if (config.getBoolean("ai.logging.enabled", false)) {
            System.out.println("[AI] " + message);
        }
    }
    
    // 引擎输出日志，可以独立控制
    public static void engineDebug(String message) {
        if (config.getBoolean("log.engine.output", false)) {
            System.out.println("[ENGINE] " + message);
        }
    }
}
