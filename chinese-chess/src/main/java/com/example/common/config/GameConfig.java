package com.example.common.config;

/**
 * 游戏配置类，包含所有游戏相关的常量和配置
 */
public class GameConfig {
    
    // 棋盘显示相关配置
    public static final int BOARD_CELL_SIZE = 60;  // 棋盘格子大小（像素）
    public static final int BOARD_MARGIN = 50;     // 棋盘边距（像素）
    
    // AI计算相关配置
    public static final int AI_CALCULATION_TIMEOUT_SECONDS = 30;  // AI计算超时时间（秒）
    private static int aiThinkingDelay = 800;  // AI思考延迟（毫秒）
    
    // 游戏界面配置
    public static final int DEFAULT_WINDOW_WIDTH = 1400;   // 默认窗口宽度
    public static final int DEFAULT_WINDOW_HEIGHT = 1000;  // 默认窗口高度
    public static final int MIN_WINDOW_WIDTH = 1200;       // 最小窗口宽度
    public static final int MIN_WINDOW_HEIGHT = 900;       // 最小窗口高度
    
    // 动画和效果配置
    public static final int MOVE_ANIMATION_DURATION = 300;  // 移动动画持续时间（毫秒）
    public static final int SELECTION_PULSE_SPEED = 8;      // 选择脉冲动画速度
    
    // 音效配置
    public static final boolean ENABLE_SOUND_EFFECTS = true;  // 是否启用音效
    public static final float SOUND_VOLUME = 0.7f;           // 音效音量（0.0-1.0）
    
    /**
     * 获取AI思考延迟
     * @return AI思考延迟时间（毫秒）
     */
    public static int getAIThinkingDelay() {
        return aiThinkingDelay;
    }
    
    /**
     * 设置AI思考延迟
     * @param delay 延迟时间（毫秒）
     */
    public static void setAIThinkingDelay(int delay) {
        aiThinkingDelay = Math.max(0, Math.min(5000, delay)); // 限制在0-5000毫秒之间
    }
    
    /**
     * 计算棋盘总宽度（包括边距和坐标显示空间）
     * @return 棋盘总宽度
     */
    public static int calculateBoardTotalWidth() {
        return 8 * BOARD_CELL_SIZE + 2 * BOARD_MARGIN + 40; // 8列格子 + 左右边距 + 坐标空间
    }
    
    /**
     * 计算棋盘总高度（包括边距和坐标显示空间）
     * @return 棋盘总高度
     */
    public static int calculateBoardTotalHeight() {
        return 9 * BOARD_CELL_SIZE + 2 * BOARD_MARGIN + 80; // 9行格子 + 上下边距 + 坐标空间
    }
    
    /**
     * 获取棋盘中心X坐标
     * @return 中心X坐标
     */
    public static int getBoardCenterX() {
        return BOARD_MARGIN + (8 * BOARD_CELL_SIZE) / 2;
    }
    
    /**
     * 获取棋盘中心Y坐标
     * @return 中心Y坐标
     */
    public static int getBoardCenterY() {
        return BOARD_MARGIN + (9 * BOARD_CELL_SIZE) / 2;
    }
    
    // 调试模式配置
    public static final boolean DEBUG_MODE = false;  // 是否启用调试模式
    public static final boolean VERBOSE_LOGGING = false;  // 是否启用详细日志
    
    // UI界面相关配置
    public static final java.awt.Dimension CONTROL_PANEL_SIZE = new java.awt.Dimension(400, 120); // 控制面板尺寸
    public static final java.awt.Font DEFAULT_FONT = new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, 12); // 默认字体
    public static final java.awt.Color CHAT_BACKGROUND_COLOR = new java.awt.Color(248, 248, 255); // 聊天背景色
    
    // 性能监控配置
    public static final boolean ENABLE_PERFORMANCE_MONITORING = true;  // 是否启用性能监控
    public static final int PERFORMANCE_LOG_INTERVAL = 10000;  // 性能日志输出间隔（毫秒）
}
