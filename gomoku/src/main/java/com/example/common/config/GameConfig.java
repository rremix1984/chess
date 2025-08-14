package com.example.common.config;

import java.awt.Color;
import java.awt.Font;

/**
 * 游戏配置常量类
 */
public class GameConfig {
    // 窗口标题
    public static final String WINDOW_TITLE = "五子棋游戏";
    
    // 字体配置
    public static final Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 12);
    public static final Font BOARD_FONT = new Font("微软雅黑", Font.PLAIN, 16);
    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 18);
    
    // 界面尺寸配置
    public static final int BOARD_SIZE = 600;
    public static final int CHAT_PANEL_WIDTH = 300;
    public static final int CHAT_PANEL_HEIGHT = 600;
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 700;
    
    // 颜色配置
    public static final Color BOARD_BACKGROUND_COLOR = new Color(222, 184, 135);
    public static final Color BOARD_LINE_COLOR = Color.BLACK;
    public static final Color BLACK_PIECE_COLOR = Color.BLACK;
    public static final Color WHITE_PIECE_COLOR = Color.WHITE;
    public static final Color CHAT_BACKGROUND_COLOR = Color.WHITE;
    
    // 棋盘配置
    public static final int GOMOKU_BOARD_SIZE = 15;
    public static final int CELL_SIZE = 40;
    public static final int PIECE_SIZE = 36;
    
    // AI配置
    public static final int AI_THINK_TIME_MS = 1000;
    public static final String[] AI_TYPES = {"基础AI", "高级AI", "神经网络AI", "大模型AI"};
    public static final String[] AI_DIFFICULTIES = {"简单", "普通", "困难", "专家", "大师"};
    public static final String[] DIFFICULTY_LEVELS = {"简单", "普通", "困难", "专家", "大师"};
    public static final String[] AI_MODELS = {"qwen2.5:7b", "llama3:8b", "gemma2:9b"};
    public static final String[] DEFAULT_MODELS = {"qwen2.5:7b", "llama3:8b", "gemma2:9b"};
    
    // 玩家选择配置
    public static final String[] PLAYER_COLORS = {"我是黑方", "我是白方"};
}
