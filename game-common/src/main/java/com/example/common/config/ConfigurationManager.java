package com.example.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * 配置管理器 - 类似SpringBoot的@ConfigurationProperties功能
 * 负责加载和管理应用程序配置
 */
public class ConfigurationManager {
    private static ConfigurationManager instance;
    private Properties properties;
    private Map<String, String> systemProperties;
    
    private ConfigurationManager() {
        loadProperties();
        loadSystemProperties();
    }
    
    /**
     * 获取配置管理器实例（单例模式）
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    /**
     * 加载配置文件
     */
    private void loadProperties() {
        properties = new Properties();
        
        // 加载默认配置
        loadPropertiesFromResource("application.properties");
        
        // 加载环境特定配置（如果存在）
        String environment = System.getProperty("app.environment", "default");
        if (!"default".equals(environment)) {
            loadPropertiesFromResource("application-" + environment + ".properties");
        }
        
        // 加载本地配置文件（如果存在，用于开发者覆盖）
        loadPropertiesFromResource("application-local.properties");
    }
    
    /**
     * 从资源文件加载配置
     */
    private void loadPropertiesFromResource(String filename) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                Properties tempProps = new Properties();
                tempProps.load(input);
                properties.putAll(tempProps);
                System.out.println("✅ 已加载配置文件: " + filename);
            }
        } catch (IOException e) {
            System.err.println("⚠️ 无法加载配置文件: " + filename + " - " + e.getMessage());
        }
    }
    
    /**
     * 加载系统属性，用于替换占位符
     */
    private void loadSystemProperties() {
        systemProperties = new HashMap<>();
        systemProperties.put("user.dir", System.getProperty("user.dir"));
        systemProperties.put("user.home", System.getProperty("user.home"));
        systemProperties.put("java.home", System.getProperty("java.home"));
        systemProperties.put("os.name", System.getProperty("os.name"));
        systemProperties.put("os.arch", System.getProperty("os.arch"));
        systemProperties.put("file.separator", System.getProperty("file.separator"));
    }
    
    /**
     * 获取字符串配置值
     */
    public String getString(String key) {
        return getString(key, null);
    }
    
    /**
     * 获取字符串配置值，带默认值
     */
    public String getString(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        return resolvePlaceholders(value);
    }
    
    /**
     * 获取整数配置值
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }
    
    /**
     * 获取整数配置值，带默认值
     */
    public int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠️ 配置值格式错误: " + key + "=" + value + ", 使用默认值: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取长整数配置值
     */
    public long getLong(String key) {
        return getLong(key, 0L);
    }
    
    /**
     * 获取长整数配置值，带默认值
     */
    public long getLong(String key, long defaultValue) {
        String value = getString(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠️ 配置值格式错误: " + key + "=" + value + ", 使用默认值: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取双精度浮点数配置值
     */
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }
    
    /**
     * 获取双精度浮点数配置值，带默认值
     */
    public double getDouble(String key, double defaultValue) {
        String value = getString(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠️ 配置值格式错误: " + key + "=" + value + ", 使用默认值: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置值
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    /**
     * 获取布尔配置值，带默认值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
    
    /**
     * 解析占位符，如 ${user.dir} 
     */
    private String resolvePlaceholders(String value) {
        if (value == null) {
            return null;
        }
        
        String resolved = value;
        
        // 解析系统属性占位符
        for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            resolved = resolved.replace(placeholder, entry.getValue());
        }
        
        // 解析配置属性占位符
        while (resolved.contains("${") && resolved.contains("}")) {
            int start = resolved.indexOf("${");
            int end = resolved.indexOf("}", start);
            if (start != -1 && end != -1) {
                String placeholder = resolved.substring(start, end + 1);
                String key = resolved.substring(start + 2, end);
                String replacement = properties.getProperty(key, placeholder);
                resolved = resolved.replace(placeholder, replacement);
                
                // 防止无限循环
                if (replacement.equals(placeholder)) {
                    break;
                }
            } else {
                break;
            }
        }
        
        return resolved;
    }
    
    /**
     * 设置配置值（运行时动态配置）
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * 检查配置键是否存在
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * 获取所有配置键
     */
    public java.util.Set<String> getAllKeys() {
        return properties.stringPropertyNames();
    }
    
    /**
     * 获取以指定前缀开头的所有配置
     */
    public Properties getPropertiesWithPrefix(String prefix) {
        Properties result = new Properties();
        String prefixWithDot = prefix.endsWith(".") ? prefix : prefix + ".";
        
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefixWithDot)) {
                result.setProperty(key, getString(key));
            }
        }
        
        return result;
    }
    
    // ========================================
    // 便捷方法：常用配置的快速访问
    // ========================================
    
    /**
     * 获取应用信息
     */
    public AppConfig getAppConfig() {
        return new AppConfig(
            getString("app.name", "Multi-Game Platform"),
            getString("app.version", "1.0.0"),
            getString("app.author", "Game Development Team"),
            getString("app.description", "A comprehensive gaming platform")
        );
    }
    
    /**
     * 获取Ollama配置
     */
    public OllamaConfig getOllamaConfig() {
        return new OllamaConfig(
            getString("ollama.host", "localhost"),
            getInt("ollama.port", 11434),
            getString("ollama.base.url"),
            getString("ollama.api.generate", "/api/generate"),
            getString("ollama.api.tags", "/api/tags"),
            getString("ollama.api.pull", "/api/pull")
        );
    }
    
    /**
     * 获取Pikafish配置
     */
    public PikafishConfig getPikafishConfig() {
        return new PikafishConfig(
            getString("pikafish.engine.path", "pikafish"),
            getString("pikafish.engine.neural.network.path"),
            getInt("pikafish.engine.threads", 2),
            getInt("pikafish.engine.hash.size", 64)
        );
    }
    
    /**
     * 获取DeepSeek配置
     */
    public DeepSeekConfig getDeepSeekConfig() {
        return new DeepSeekConfig(
            getString("deepseek.model.name", "deepseek-r1:7b"),
            getDouble("deepseek.model.temperature", 0.1),
            getBoolean("deepseek.model.stream", false)
        );
    }
    
    /**
     * 获取AI思考时间数组
     */
    public int[] getAIThinkTimes() {
        int[] thinkTimes = new int[10];
        for (int i = 1; i <= 10; i++) {
            thinkTimes[i - 1] = getInt("ai.think.time." + i, 1000 * i);
        }
        return thinkTimes;
    }
    
    /**
     * 获取HTTP客户端配置
     */
    public HttpClientConfig getHttpClientConfig() {
        return new HttpClientConfig(
            getInt("http.client.connect.timeout", 5000),
            getInt("http.client.read.timeout", 15000),
            getInt("http.client.write.timeout", 5000)
        );
    }
    
    /**
     * 获取KataGo配置
     */
    public KataGoConfig getKataGoConfig() {
        return new KataGoConfig(
            getString("katago.engine.path", "/usr/local/bin/katago"),
            getString("katago.model.path", "models/kata1-b40c256-s11840935168-d2898845681.bin.gz"),
            getString("katago.config.path", "configs/gtp_example.cfg"),
            getInt("katago.threads", 4),
            getDouble("katago.time.per.move", 5.0),
            getDouble("katago.resign.threshold", -0.9)
        );
    }
    
    /**
     * 根据难度获取围棋AI访问数
     */
    public int getGoAIVisits(int difficulty) {
        return getInt("go.ai.visits." + difficulty, Math.max(100, difficulty * 200));
    }
    
    // ========================================
    // 配置类定义
    // ========================================
    
    public static class AppConfig {
        public final String name;
        public final String version;
        public final String author;
        public final String description;
        
        public AppConfig(String name, String version, String author, String description) {
            this.name = name;
            this.version = version;
            this.author = author;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return String.format("%s v%s by %s", name, version, author);
        }
    }
    
    public static class OllamaConfig {
        public final String host;
        public final int port;
        public final String baseUrl;
        public final String generateEndpoint;
        public final String tagsEndpoint;
        public final String pullEndpoint;
        
        public OllamaConfig(String host, int port, String baseUrl, 
                          String generateEndpoint, String tagsEndpoint, String pullEndpoint) {
            this.host = host;
            this.port = port;
            this.baseUrl = baseUrl;
            this.generateEndpoint = generateEndpoint;
            this.tagsEndpoint = tagsEndpoint;
            this.pullEndpoint = pullEndpoint;
        }
        
        public String getFullUrl(String endpoint) {
            return baseUrl + endpoint;
        }
    }
    
    public static class PikafishConfig {
        public final String enginePath;
        public final String neuralNetworkPath;
        public final int threads;
        public final int hashSize;
        
        public PikafishConfig(String enginePath, String neuralNetworkPath, int threads, int hashSize) {
            this.enginePath = enginePath;
            this.neuralNetworkPath = neuralNetworkPath;
            this.threads = threads;
            this.hashSize = hashSize;
        }
    }
    
    public static class DeepSeekConfig {
        public final String modelName;
        public final double temperature;
        public final boolean stream;
        
        public DeepSeekConfig(String modelName, double temperature, boolean stream) {
            this.modelName = modelName;
            this.temperature = temperature;
            this.stream = stream;
        }
    }
    
    public static class HttpClientConfig {
        public final int connectTimeout;
        public final int readTimeout;
        public final int writeTimeout;
        
        public HttpClientConfig(int connectTimeout, int readTimeout, int writeTimeout) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            this.writeTimeout = writeTimeout;
        }
    }

    public static class KataGoConfig {
        public final String enginePath;
        public final String modelPath;
        public final String configPath;
        public final int threads;
        public final double timePerMove;
        public final double resignThreshold;

        public KataGoConfig(String enginePath, String modelPath, String configPath, int threads, double timePerMove, double resignThreshold) {
            this.enginePath = enginePath;
            this.modelPath = modelPath;
            this.configPath = configPath;
            this.threads = threads;
            this.timePerMove = timePerMove;
            this.resignThreshold = resignThreshold;
        }
    }
    
    /**
     * 重新加载配置文件
     */
    public void reload() {
        loadProperties();
        loadSystemProperties();
        System.out.println("✅ 配置文件已重新加载");
    }
    
    /**
     * 打印所有配置信息（调试用）
     */
    public void printAllConfigurations() {
        System.out.println("========================================");
        System.out.println("应用配置信息:");
        System.out.println("========================================");
        
        properties.entrySet().stream()
            .sorted((entry1, entry2) -> String.valueOf(entry1.getKey()).compareTo(String.valueOf(entry2.getKey())))
            .forEach(entry -> {
                String key = entry.getKey().toString();
                String value = resolvePlaceholders(entry.getValue().toString());
                
                // 敏感信息脱敏
                if (key.toLowerCase().contains("password") || 
                    key.toLowerCase().contains("secret") ||
                    key.toLowerCase().contains("token")) {
                    value = "***";
                }
                
                System.out.println(key + " = " + value);
            });
        
        System.out.println("========================================");
    }
}
