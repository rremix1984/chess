package com.example.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Ollama模型管理工具类
 * 管理可用的大语言模型列表
 */
public class OllamaModelManager {
    
    // 默认可用的模型列表
    private static final List<String> DEFAULT_MODELS = Arrays.asList(
        "deepseek-r1:7b",
        "deepseek-coder:7b", 
        "qwen2.5:7b",
        "llama3.1:8b",
        "gemma2:9b",
        "mistral:7b",
        "phi3.5:3.8b",
        "codeqwen:7b"
    );
    
    // 缓存的模型列表
    private static List<String> cachedModels = null;
    
    /**
     * 获取可用的模型列表
     * @return 模型名称列表
     */
    public static List<String> getAvailableModels() {
        if (cachedModels != null) {
            return new ArrayList<>(cachedModels);
        }
        
        // 尝试从Ollama API获取模型列表
        List<String> models = fetchModelsFromOllama();
        
        // 如果获取失败，使用默认列表
        if (models.isEmpty()) {
            models = new ArrayList<>(DEFAULT_MODELS);
            System.out.println("⚠️ [模型管理] 无法从Ollama获取模型列表，使用默认列表");
        }
        
        cachedModels = models;
        return new ArrayList<>(models);
    }
    
    /**
     * 尝试从Ollama API获取模型列表
     * 这里是一个简化实现，实际应该调用Ollama API
     * @return 模型列表
     */
    private static List<String> fetchModelsFromOllama() {
        try {
            // 这里应该实现实际的Ollama API调用
            // 目前返回默认列表
            System.out.println("🔍 [模型管理] 正在检查可用模型...");
            
            // 模拟检查过程
            Thread.sleep(100);
            
            // 返回默认模型列表作为示例
            return new ArrayList<>(DEFAULT_MODELS);
            
        } catch (Exception e) {
            System.err.println("❌ [模型管理] 获取模型列表失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 检查指定模型是否可用
     * @param modelName 模型名称
     * @return 是否可用
     */
    public static boolean isModelAvailable(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        
        List<String> availableModels = getAvailableModels();
        return availableModels.contains(modelName.trim());
    }
    
    /**
     * 获取默认模型
     * @return 默认模型名称
     */
    public static String getDefaultModel() {
        List<String> models = getAvailableModels();
        return models.isEmpty() ? "deepseek-r1:7b" : models.get(0);
    }
    
    /**
     * 获取推荐的编程模型
     * @return 推荐的编程模型列表
     */
    public static List<String> getCodeModels() {
        List<String> codeModels = new ArrayList<>();
        List<String> allModels = getAvailableModels();
        
        for (String model : allModels) {
            if (model.toLowerCase().contains("code") || 
                model.toLowerCase().contains("deepseek") ||
                model.toLowerCase().contains("codeqwen")) {
                codeModels.add(model);
            }
        }
        
        // 如果没有找到编程模型，返回所有模型
        return codeModels.isEmpty() ? allModels : codeModels;
    }
    
    /**
     * 刷新模型列表缓存
     */
    public static void refreshModelList() {
        cachedModels = null;
        System.out.println("🔄 [模型管理] 已清除模型列表缓存");
    }
    
    /**
     * 获取模型的显示名称
     * @param modelName 模型内部名称
     * @return 用于显示的友好名称
     */
    public static String getDisplayName(String modelName) {
        if (modelName == null) {
            return "未知模型";
        }
        
        // 简单的显示名称映射
        switch (modelName.toLowerCase()) {
            case "deepseek-r1:7b":
                return "DeepSeek R1 (7B)";
            case "deepseek-coder:7b":
                return "DeepSeek Coder (7B)";
            case "qwen2.5:7b":
                return "Qwen 2.5 (7B)";
            case "llama3.1:8b":
                return "Llama 3.1 (8B)";
            case "gemma2:9b":
                return "Gemma 2 (9B)";
            case "mistral:7b":
                return "Mistral (7B)";
            case "phi3.5:3.8b":
                return "Phi 3.5 (3.8B)";
            case "codeqwen:7b":
                return "CodeQwen (7B)";
            default:
                return modelName;
        }
    }
    
    /**
     * 获取模型统计信息
     * @return 统计信息字符串
     */
    public static String getModelStats() {
        List<String> models = getAvailableModels();
        List<String> codeModels = getCodeModels();
        
        return String.format("📊 [模型管理] 总计%d个模型，其中%d个编程专用模型", 
                           models.size(), codeModels.size());
    }
}
