package com.example.common.utils;

import com.example.common.utils.ExceptionHandler;
import com.example.common.config.GameConfig;
import com.example.common.utils.PerformanceMonitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Ollama模型管理器
 * 用于动态获取本地可用的ollama模型列表
 */
public class OllamaModelManager {
    
    // 使用GameConfig中的默认模型列表
    private static final List<String> DEFAULT_MODELS = GameConfig.DEFAULT_MODELS;
    
    // 命令执行超时时间（秒）
    private static final int COMMAND_TIMEOUT_SECONDS = 10;
    
    /**
     * 获取可用的ollama模型列表
     * @return 模型名称列表
     */
    public static List<String> getAvailableModels() {
        PerformanceMonitor.startTimer("ollama_model_list");
        
        try {
            // 尝试执行ollama list命令
            ProcessBuilder pb = new ProcessBuilder("ollama", "list");
            pb.redirectErrorStream(true); // 合并错误流和输出流
            Process process = pb.start();
            
            List<String> allModels = new ArrayList<>();
            
            // 使用try-with-resources确保资源正确关闭
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean isFirstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    // 跳过标题行
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    
                    // 解析模型名称（第一列）
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        allModels.add(parts[0]);
                    }
                }
            }
            
            // 等待进程结束，设置超时
            boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                ExceptionHandler.logWarning("Ollama命令执行超时，强制终止进程", "模型管理器");
                return DEFAULT_MODELS;
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode == 0 && !allModels.isEmpty()) {
                // 过滤和排序模型，优先选择对话生成模型
                List<String> sortedModels = sortModelsByPriority(allModels);
                ExceptionHandler.logInfo("成功获取到 " + allModels.size() + " 个ollama模型", "模型管理器");
                ExceptionHandler.logInfo("优先排序后的模型列表: " + sortedModels, "模型管理器");
                return sortedModels;
            } else {
                ExceptionHandler.logWarning("Ollama命令执行失败（退出码: " + exitCode + "），使用默认模型列表", "模型管理器");
                return DEFAULT_MODELS;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ExceptionHandler.handleException(e, "获取Ollama模型列表被中断", false);
            return DEFAULT_MODELS;
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "获取Ollama模型列表", false);
            return DEFAULT_MODELS;
        } finally {
            PerformanceMonitor.endTimer("ollama_model_list");
        }
    }
    
    /**
     * 按优先级排序模型，对话生成模型优先
     * @param models 原始模型列表
     * @return 排序后的模型列表
     */
    private static List<String> sortModelsByPriority(List<String> models) {
        List<String> chatModels = new ArrayList<>();
        List<String> embedModels = new ArrayList<>();
        List<String> otherModels = new ArrayList<>();
        
        for (String model : models) {
            String lowerModel = model.toLowerCase();
            if (lowerModel.contains("embed")) {
                // 嵌入模型放到最后
                embedModels.add(model);
            } else if (lowerModel.contains("deepseek") || 
                      lowerModel.contains("qwen") || 
                      lowerModel.contains("llama") || 
                      lowerModel.contains("mistral") || 
                      lowerModel.contains("chess")) {
                // 对话生成模型优先
                chatModels.add(model);
            } else {
                // 其他模型
                otherModels.add(model);
            }
        }
        
        // 合并列表：对话模型 -> 其他模型 -> 嵌入模型
        List<String> result = new ArrayList<>();
        result.addAll(chatModels);
        result.addAll(otherModels);
        result.addAll(embedModels);
        
        return result;
    }
    
    /**
     * 检查ollama服务是否可用
     * @return true如果ollama服务可用
     */
    public static boolean isOllamaAvailable() {
        PerformanceMonitor.startTimer("ollama_availability_check");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("ollama", "list");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 设置超时，避免长时间等待
            boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                ExceptionHandler.logWarning("Ollama可用性检查超时", "模型管理器");
                return false;
            }
            
            int exitCode = process.exitValue();
            boolean available = exitCode == 0;
            
            if (available) {
                ExceptionHandler.logInfo("Ollama服务可用", "模型管理器");
            } else {
                ExceptionHandler.logWarning("Ollama服务不可用（退出码: " + exitCode + "）", "模型管理器");
            }
            
            return available;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ExceptionHandler.logWarning("Ollama可用性检查被中断", "模型管理器");
            return false;
        } catch (Exception e) {
            ExceptionHandler.logWarning("Ollama可用性检查失败: " + e.getMessage(), "模型管理器");
            return false;
        } finally {
            PerformanceMonitor.endTimer("ollama_availability_check");
        }
    }
    
    /**
     * 获取默认模型名称
     * @return 默认模型名称
     */
    public static String getDefaultModel() {
        List<String> models = getAvailableModels();
        return models.isEmpty() ? "deepseek-r1:7b" : models.get(0);
    }
}