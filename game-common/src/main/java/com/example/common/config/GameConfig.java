package com.example.common.config;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * 游戏配置类
 * 包含所有游戏的通用配置参数
 */
public class GameConfig {
    
    // 窗口配置
    public static final String WINDOW_TITLE = "多游戏平台";
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    
    // 棋盘配置
    public static final int BOARD_WIDTH = 9;
    public static final int BOARD_HEIGHT = 10;
    public static final int BOARD_CELL_SIZE = 60;
    public static final int BOARD_MARGIN = 50;
    
    // 聊天面板配置
    public static final int CHAT_PANEL_WIDTH = 300;
    public static final int CHAT_PANEL_HEIGHT = 400;
    public static final Color CHAT_BACKGROUND_COLOR = new Color(248, 248, 248);
    
    // 控制面板配置
    public static final Dimension CONTROL_PANEL_SIZE = new Dimension(300, 600);
    
    // 字体配置
    public static final Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 18);
    public static final Font BUTTON_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    
    // 按钮配置
    public static final Dimension BUTTON_SIZE = new Dimension(80, 30);
    
    // AI配置
    public static final String[] PLAYER_COLORS = {"黑子", "白子"};
    public static final String[] AI_TYPES = {"传统AI", "LLM AI", "混合AI"};
    public static final String[] DIFFICULTY_LEVELS = {"简单", "中等", "困难", "专家"};
    public static final int AI_CALCULATION_TIMEOUT_SECONDS = 30;
    
    // 默认模型列表
    public static final List<String> DEFAULT_MODELS = Arrays.asList(
        "deepseek-chat",
        "qwen2.5:7b",
        "llama3.1:8b",
        "gemma2:9b"
    );
    
    // 网络配置
    public static final int NETWORK_CONNECT_TIMEOUT = 10;
    public static final int NETWORK_READ_TIMEOUT = 30;
    public static final int NETWORK_WRITE_TIMEOUT = 30;
    
    // 资源配置
    public static final int RESOURCE_SHUTDOWN_TIMEOUT = 5;
    public static final int THREAD_POOL_CORE_SIZE = 4;
    
    // AI思考延迟配置
    private static int aiThinkingDelay = 1000; // 默认1秒
    
    public static int getAIThinkingDelay() {
        return aiThinkingDelay;
    }
    
    public static void setAIThinkingDelay(int delay) {
        aiThinkingDelay = Math.max(0, delay);
    }
    
    // AI权重配置
    public static class AIWeights {
        public static final double POSITION_WEIGHT = 0.3;
        public static final double MOBILITY_WEIGHT = 0.2;
        public static final double SAFETY_WEIGHT = 0.3;
        public static final double MATERIAL_WEIGHT = 0.2;
    }
}