package com.example.common.config;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理器 - 管理应用程序的配置设置
 * Configuration Manager for handling application settings
 */
public class ConfigurationManager {
    private static ConfigurationManager instance;
    private final Map<String, String> configurations = new ConcurrentHashMap<>();
    private final String configFilePath;

    // 默认配置值
    private static final Map<String, String> DEFAULT_CONFIGS = new HashMap<>();
    
    static {
        // AI 相关配置
        DEFAULT_CONFIGS.put("ai.deepseek.api.url", "https://api.deepseek.com/v1");
        DEFAULT_CONFIGS.put("ai.deepseek.model", "deepseek-chat");
        DEFAULT_CONFIGS.put("ai.deepseek.timeout", "30000");
        DEFAULT_CONFIGS.put("ai.deepseek.max_tokens", "1000");
        DEFAULT_CONFIGS.put("ai.deepseek.temperature", "0.7");
        
        // Ollama 相关配置
        DEFAULT_CONFIGS.put("ai.ollama.api.url", "http://localhost:11434");
        DEFAULT_CONFIGS.put("ai.ollama.model", "llama3");
        DEFAULT_CONFIGS.put("ai.ollama.timeout", "60000");
        
        // 游戏配置
        DEFAULT_CONFIGS.put("game.difficulty", "medium");
        DEFAULT_CONFIGS.put("game.auto_save", "true");
        DEFAULT_CONFIGS.put("game.sound_enabled", "true");
        
        // UI 配置
        DEFAULT_CONFIGS.put("ui.theme", "default");
        DEFAULT_CONFIGS.put("ui.language", "zh_CN");
        DEFAULT_CONFIGS.put("ui.font_size", "14");
        
        // 日志配置
        DEFAULT_CONFIGS.put("logging.level", "INFO");
        DEFAULT_CONFIGS.put("logging.file_enabled", "true");
        DEFAULT_CONFIGS.put("logging.console_enabled", "true");
        
        // 性能配置
        DEFAULT_CONFIGS.put("performance.thread_pool_size", "4");
        DEFAULT_CONFIGS.put("performance.cache_size", "1000");
        
        // AI 思考配置
        DEFAULT_CONFIGS.put("ai.think_time_ms", "3000");
        
        // Pikafish 配置
        DEFAULT_CONFIGS.put("ai.pikafish.path", "/usr/local/bin/pikafish");
        DEFAULT_CONFIGS.put("ai.pikafish.depth", "15");
        DEFAULT_CONFIGS.put("ai.pikafish.timeout", "30000");
        
        // HTTP 配置
        DEFAULT_CONFIGS.put("http.timeout", "30000");
        DEFAULT_CONFIGS.put("http.max_connections", "100");
        DEFAULT_CONFIGS.put("http.keep_alive", "true");
    }

    private ConfigurationManager() {
        this.configFilePath = System.getProperty("user.home") + "/.chinese-chess/config.properties";
        loadDefaultConfigurations();
        loadConfigurationFile();
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    /**
     * 加载默认配置
     */
    private void loadDefaultConfigurations() {
        configurations.putAll(DEFAULT_CONFIGS);
    }

    /**
     * 从文件加载配置
     */
    private void loadConfigurationFile() {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            // 创建配置目录
            configFile.getParentFile().mkdirs();
            // 保存默认配置到文件
            saveConfigurationFile();
            return;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(input);
            
            for (String key : properties.stringPropertyNames()) {
                configurations.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration file: " + e.getMessage());
        }
    }

    /**
     * 保存配置到文件
     */
    private void saveConfigurationFile() {
        try {
            File configFile = new File(configFilePath);
            configFile.getParentFile().mkdirs();
            
            try (OutputStream output = new FileOutputStream(configFile)) {
                Properties properties = new Properties();
                configurations.forEach(properties::setProperty);
                properties.store(output, "Chinese Chess Configuration");
            }
        } catch (IOException e) {
            System.err.println("Error saving configuration file: " + e.getMessage());
        }
    }

    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值，如果不存在返回null
     */
    public String getConfiguration(String key) {
        return configurations.get(key);
    }

    /**
     * 获取配置值，如果不存在返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public String getConfiguration(String key, String defaultValue) {
        return configurations.getOrDefault(key, defaultValue);
    }

    /**
     * 获取整数配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 整数配置值
     */
    public int getIntConfiguration(String key, int defaultValue) {
        try {
            String value = getConfiguration(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取布尔配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 布尔配置值
     */
    public boolean getBooleanConfiguration(String key, boolean defaultValue) {
        String value = getConfiguration(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * 获取浮点数配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 浮点数配置值
     */
    public double getDoubleConfiguration(String key, double defaultValue) {
        try {
            String value = getConfiguration(key);
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 设置配置值
     * @param key 配置键
     * @param value 配置值
     */
    public void setConfiguration(String key, String value) {
        configurations.put(key, value);
    }

    /**
     * 设置整数配置值
     * @param key 配置键
     * @param value 整数值
     */
    public void setConfiguration(String key, int value) {
        setConfiguration(key, String.valueOf(value));
    }

    /**
     * 设置布尔配置值
     * @param key 配置键
     * @param value 布尔值
     */
    public void setConfiguration(String key, boolean value) {
        setConfiguration(key, String.valueOf(value));
    }

    /**
     * 设置浮点数配置值
     * @param key 配置键
     * @param value 浮点数值
     */
    public void setConfiguration(String key, double value) {
        setConfiguration(key, String.valueOf(value));
    }

    /**
     * 删除配置
     * @param key 配置键
     */
    public void removeConfiguration(String key) {
        configurations.remove(key);
    }

    /**
     * 检查配置是否存在
     * @param key 配置键
     * @return 是否存在
     */
    public boolean hasConfiguration(String key) {
        return configurations.containsKey(key);
    }

    /**
     * 获取所有配置键
     * @return 配置键集合
     */
    public Set<String> getConfigurationKeys() {
        return new HashSet<>(configurations.keySet());
    }

    /**
     * 获取所有配置
     * @return 配置映射的副本
     */
    public Map<String, String> getAllConfigurations() {
        return new HashMap<>(configurations);
    }

    /**
     * 保存当前配置到文件
     */
    public void saveConfigurations() {
        saveConfigurationFile();
    }

    /**
     * 重新加载配置文件
     */
    public void reloadConfigurations() {
        configurations.clear();
        loadDefaultConfigurations();
        loadConfigurationFile();
    }

    /**
     * 重置为默认配置
     */
    public void resetToDefaults() {
        configurations.clear();
        loadDefaultConfigurations();
        saveConfigurationFile();
    }

    /**
     * 获取配置文件路径
     * @return 配置文件路径
     */
    public String getConfigFilePath() {
        return configFilePath;
    }
    
    // 添加 AI 相关的特殊配置方法
    
    /**
     * 获取 AI 思考时间配置
     * @return AI 思考时间数组（毫秒） - 不同难度级别对应的思考时间
     */
    public int[] getAIThinkTimes() {
        // 返回不同难度级别的思考时间（从简1到箝10级）
        // 增强了高难度级别的思考时间，提升AI棋力
        return new int[]{1500, 3000, 4500, 7000, 10000, 15000, 22000, 30000, 40000, 50000};
    }
    
    /**
     * 获取 Ollama 配置
     * @return OllamaConfig 对象
     */
    public OllamaConfig getOllamaConfig() {
        return new OllamaConfig(
            getConfiguration("ai.ollama.api.url", "http://localhost:11434"),
            getConfiguration("ai.ollama.model", "llama3"),
            getIntConfiguration("ai.ollama.timeout", 60000)
        );
    }
    
    /**
     * 获取 DeepSeek 配置
     * @return DeepSeekConfig 对象
     */
    public DeepSeekConfig getDeepSeekConfig() {
        return new DeepSeekConfig(
            getConfiguration("ai.deepseek.api.url", "https://api.deepseek.com/v1"),
            getConfiguration("ai.deepseek.model", "deepseek-chat"),
            getIntConfiguration("ai.deepseek.timeout", 30000),
            getIntConfiguration("ai.deepseek.max_tokens", 1000),
            getDoubleConfiguration("ai.deepseek.temperature", 0.7)
        );
    }
    
    /**
     * 获取 Pikafish 配置
     * @return PikafishConfig 对象
     */
    public PikafishConfig getPikafishConfig() {
        return new PikafishConfig(
            getConfiguration("ai.pikafish.path", "/usr/local/bin/pikafish"),
            getIntConfiguration("ai.pikafish.depth", 15),
            getIntConfiguration("ai.pikafish.timeout", 30000)
        );
    }
    
    /**
     * 获取 HTTP 客户端配置
     * @return HttpClientConfig 对象
     */
    public HttpClientConfig getHttpClientConfig() {
        return new HttpClientConfig(
            getIntConfiguration("http.timeout", 30000),
            getIntConfiguration("http.max_connections", 100),
            getBooleanConfiguration("http.keep_alive", true)
        );
    }
    
    // 配置类定义
    
    /**
     * Ollama 配置类
     */
    public static class OllamaConfig {
        private final String apiUrl;
        private final String model;
        private final int timeout;
        public final String baseUrl;
        public final String generateEndpoint;
        
        public OllamaConfig(String apiUrl, String model, int timeout) {
            this.apiUrl = apiUrl;
            this.model = model;
            this.timeout = timeout;
            this.baseUrl = apiUrl;
            this.generateEndpoint = apiUrl + "/api/generate";
        }
        
        public String getApiUrl() { return apiUrl; }
        public String getModel() { return model; }
        public int getTimeout() { return timeout; }
    }
    
    /**
     * DeepSeek 配置类
     */
    public static class DeepSeekConfig {
        private final String apiUrl;
        private final String model;
        private final int timeout;
        private final int maxTokens;
        private final double temperature;
        public final String modelName;
        
        public DeepSeekConfig(String apiUrl, String model, int timeout, int maxTokens, double temperature) {
            this.apiUrl = apiUrl;
            this.model = model;
            this.timeout = timeout;
            this.maxTokens = maxTokens;
            this.temperature = temperature;
            this.modelName = model;
        }
        
        public String getApiUrl() { return apiUrl; }
        public String getModel() { return model; }
        public int getTimeout() { return timeout; }
        public int getMaxTokens() { return maxTokens; }
        public double getTemperature() { return temperature; }
    }
    
    /**
     * Pikafish 配置类
     */
    public static class PikafishConfig {
        private final String executablePath;
        private final int searchDepth;
        private final int timeout;
        public final String enginePath;
        public final String neuralNetworkPath;
        
        public PikafishConfig(String executablePath, int searchDepth, int timeout) {
            this.executablePath = executablePath;
            this.searchDepth = searchDepth;
            this.timeout = timeout;
            this.enginePath = executablePath;
            this.neuralNetworkPath = "/usr/local/share/pikafish/pikafish.nnue";
        }
        
        public String getExecutablePath() { return executablePath; }
        public int getSearchDepth() { return searchDepth; }
        public int getTimeout() { return timeout; }
    }
    
    /**
     * HTTP 客户端配置类
     */
    public static class HttpClientConfig {
        private final int timeout;
        private final int maxConnections;
        private final boolean keepAlive;
        public final int connectTimeout;
        public final int readTimeout;
        public final int writeTimeout;
        
        public HttpClientConfig(int timeout, int maxConnections, boolean keepAlive) {
            this.timeout = timeout;
            this.maxConnections = maxConnections;
            this.keepAlive = keepAlive;
            this.connectTimeout = timeout;
            this.readTimeout = timeout;
            this.writeTimeout = timeout;
        }
        
        public int getTimeout() { return timeout; }
        public int getMaxConnections() { return maxConnections; }
        public boolean isKeepAlive() { return keepAlive; }
    }
}
