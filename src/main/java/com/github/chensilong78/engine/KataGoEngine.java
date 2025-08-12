package com.github.chensilong78.engine;

import java.io.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

/**
 * KataGo AI引擎接口类
 * 负责与KataGo进程通信，提供围棋AI功能
 */
public class KataGoEngine {
    private Process katagoProcess;
    private BufferedWriter writer;
    private BufferedReader reader;
    private ExecutorService executor;
    private boolean isInitialized = false;
    
    // KataGo配置参数
    private static final String KATAGO_BINARY = "/opt/homebrew/bin/katago";
    private static final String MODEL_PATH = "/Users/wangxiaozhe/.katago/models/kata1-b20c256x2-s5303129600-d1228401921.bin";
    private static final String CONFIG_PATH = "/Users/wangxiaozhe/.katago/default_gtp.cfg";
    
    public KataGoEngine() {
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 初始化KataGo引擎
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            // 构建KataGo启动命令
            ProcessBuilder pb = new ProcessBuilder(
                KATAGO_BINARY, "gtp",
                "-model", MODEL_PATH,
                "-config", CONFIG_PATH
            );
            
            pb.redirectErrorStream(true);
            katagoProcess = pb.start();
            
            // 设置输入输出流
            writer = new BufferedWriter(new OutputStreamWriter(katagoProcess.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(katagoProcess.getInputStream()));
            
            // 等待KataGo初始化完成
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("GTP ready")) {
                    isInitialized = true;
                    break;
                }
            }
            
            return isInitialized;
        } catch (IOException e) {
            System.err.println("初始化KataGo失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送GTP命令并获取响应
     * @param command GTP命令
     * @return 响应结果
     */
    public String sendCommand(String command) {
        if (!isInitialized) {
            return "ERROR: KataGo未初始化";
        }
        
        try {
            // 发送命令
            writer.write(command + "\n");
            writer.flush();
            
            // 读取响应
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    break;
                }
                response.append(line).append("\n");
            }
            
            return response.toString().trim();
        } catch (IOException e) {
            return "ERROR: 通信失败 - " + e.getMessage();
        }
    }
    
    /**
     * 设置棋盘大小
     * @param size 棋盘大小 (通常为19)
     * @return 设置是否成功
     */
    public boolean setBoardSize(int size) {
        String response = sendCommand("boardsize " + size);
        return response.startsWith("=");
    }
    
    /**
     * 清空棋盘
     * @return 操作是否成功
     */
    public boolean clearBoard() {
        String response = sendCommand("clear_board");
        return response.startsWith("=");
    }
    
    /**
     * 下棋
     * @param color 颜色 (black/white)
     * @param position 位置 (如 \"D4\")
     * @return 操作是否成功
     */
    public boolean playMove(String color, String position) {
        String response = sendCommand("play " + color + " " + position);
        return response.startsWith("=");
    }
    
    /**
     * 生成AI下一步棋
     * @param color 颜色 (black/white)
     * @return AI建议的落子位置
     */
    public String generateMove(String color) {
        String response = sendCommand("genmove " + color);
        if (response.startsWith("= ")) {
            return response.substring(2).trim();
        }
        return null;
    }
    
    /**
     * 分析当前局面
     * @param interval 分析间隔 (秒)
     * @return 分析结果
     */
    public String analyzePosition(double interval) {
        String response = sendCommand("kata-analyze interval " + interval);
        return response;
    }
    
    /**
     * 获取当前胜率
     * @return 胜率信息
     */
    public String getWinrate() {
        String response = sendCommand("kata-get-property winrate");
        return response;
    }
    
    /**
     * 设置访问次数限制
     * @param visits 访问次数
     * @return 设置是否成功
     */
    public boolean setVisits(int visits) {
        String response = sendCommand("kata-set-param maxVisits " + visits);
        return response.startsWith("=");
    }
    
    /**
     * 关闭KataGo引擎
     */
    public void shutdown() {
        try {
            if (isInitialized) {
                sendCommand("quit");
            }
            
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (katagoProcess != null) {
                katagoProcess.destroy();
                katagoProcess.waitFor(5, TimeUnit.SECONDS);
            }
            if (executor != null) {
                executor.shutdown();
            }
            
            isInitialized = false;
        } catch (Exception e) {
            System.err.println("关闭KataGo时出错: " + e.getMessage());
        }
    }
    
    /**
     * 检查引擎是否可用
     * @return 是否可用
     */
    public boolean isAvailable() {
        return isInitialized && katagoProcess != null && katagoProcess.isAlive();
    }
    
    /**
     * 获取KataGo版本信息
     * @return 版本信息
     */
    public String getVersion() {
        String response = sendCommand("version");
        if (response.startsWith("= ")) {
            return response.substring(2).trim();
        }
        return "Unknown";
    }
}
