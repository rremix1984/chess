package com.example.common.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Ollama模型管理工具类
 */
public class OllamaModelManager {
    
    /**
     * 获取可用的模型列表
     */
    public static List<String> getAvailableModels() throws Exception {
        // 返回默认模型列表
        // 在实际应用中，这里应该调用ollama list命令获取可用模型
        return Arrays.asList(
            "qwen2.5:7b",
            "llama3:8b", 
            "gemma2:9b"
        );
    }
}
