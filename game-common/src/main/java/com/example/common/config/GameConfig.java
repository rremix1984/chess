package com.example.common.config;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 游戏全局配置管理器
 */
public class GameConfig {
    private static final GameConfig INSTANCE = new GameConfig();
    private final Properties properties = new Properties();
    
    // 窗口配置
    public static final String WINDOW_TITLE = "多游戏平台";
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final Dimension CONTROL_PANEL_SIZE = new Dimension(300, 600);
    public static final Dimension BUTTON_SIZE = new Dimension(80, 30);
    
    // 棋盘配置
    public static final int BOARD_CELL_SIZE = 60;
    public static final int BOARD_MARGIN = 50;
    
    // UI配置
    public static final Color CHAT_BACKGROUND_COLOR = new Color(248, 248, 248);
    public static final int CHAT_PANEL_WIDTH = 300;
    public static final int CHAT_PANEL_HEIGHT = 600;
    
    // 字体配置
    public static final Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 18);
    public static final Font BUTTON_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    
    // AI配置
    public static final String[] PLAYER_COLORS = {"黑子", "白子"};
    public static final String[] AI_TYPES = {"传统AI", "Stockfish", "大模型AI", "混合AI", "GomokuZero"};
    public static final String[] DIFFICULTY_LEVELS = {"简单", "中等", "困难", "专家", "大师"};
    public static final int AI_CALCULATION_TIMEOUT_SECONDS = 30;
    
    // 默认模型列表
    public static final List<String> DEFAULT_MODELS = Arrays.asList(
        "deepseek-chat", "qwen2.5:7b", "llama3.1:8b", "gemma2:9b"
    );
    
    // 网络配置
    public static final int NETWORK_CONNECT_TIMEOUT = 10;
    public static final int NETWORK_READ_TIMEOUT = 30;
    public static final int NETWORK_WRITE_TIMEOUT = 30;
    
    // 资源配置
    public static final int RESOURCE_SHUTDOWN_TIMEOUT = 5;
    public static final int THREAD_POOL_CORE_SIZE = 4;
    
    private GameConfig() {
        loadConfig();
    }
    
    public static GameConfig getInstance() {
        return INSTANCE;
    }
    
    private void loadConfig() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }
    
    public String getStockfishPath() {
        return properties.getProperty("stockfish.path", "stockfish");
    }
    
    public String getPikafishPath() {
        return properties.getProperty("pikafish.path", "pikafish");
    }
    
    public String findNnueFile() {
        // 首先检查用户当前的具体路径
        String specificPath = "/Users/rremixwang/workspace/chinese/international-chess/nn-1c0000000000.nnue";
        if (Files.exists(Paths.get(specificPath))) {
            return specificPath;
        }
        
        String fileName = properties.getProperty("nnue.file.name", "nn-1c0000000000.nnue");
        String projectDir = properties.getProperty("nnue.project.dir", "international-chess");
        String userDir = properties.getProperty("nnue.user.dir", ".stockfish");
        
        // 检查项目目录
        Path projectPath = Paths.get(System.getProperty("user.dir")).resolve(projectDir).resolve(fileName);
        if (Files.exists(projectPath)) {
            return projectPath.toString();
        }
        
        // 检查项目根目录下的international-chess文件夹
        Path rootProjectPath = Paths.get(System.getProperty("user.dir")).getParent()
                              .resolve("chinese-chess-game").resolve(projectDir).resolve(fileName);
        if (Files.exists(rootProjectPath)) {
            return rootProjectPath.toString();
        }
        
        // 检查用户目录
        Path userPath = Paths.get(System.getProperty("user.home")).resolve(userDir).resolve(fileName);
        if (Files.exists(userPath)) {
            return userPath.toString();
        }
        
        System.out.println("警告: 未找到NNUE文件，将使用Stockfish默认评估");
        return null;
    }
    
    public boolean isLogEngineOutput() {
        return Boolean.parseBoolean(properties.getProperty("log.engine.output", "false"));
    }
    
    public boolean isLogAiThinking() {
        return Boolean.parseBoolean(properties.getProperty("log.ai.thinking", "false"));
    }
    
    public int getDefaultSkillLevel() {
        return Integer.parseInt(properties.getProperty("default.skill.level", "12"));
    }
    
    public int getDefaultThinkingTime() {
        return Integer.parseInt(properties.getProperty("default.thinking.time", "1500"));
    }
    
    public static int getAIThinkingDelay() {
        return 1000; // 默认1秒延迟
    }
}
