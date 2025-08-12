package com.tankbattle;

/**
 * 简单的JSON解析工具类
 * 当没有外部JSON库时使用
 */
public class SimpleJsonParser {
    
    /**
     * 从JSON字符串中提取指定键的值
     */
    public static String extractValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }
        
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }
        
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        
        if (endIndex == -1) {
            return null;
        }
        
        return json.substring(startIndex, endIndex);
    }
    
    /**
     * 检查JSON字符串中是否包含指定键
     */
    public static boolean hasKey(String json, String key) {
        if (json == null || key == null) {
            return false;
        }
        
        String searchKey = "\"" + key + "\":";
        return json.contains(searchKey);
    }
}
