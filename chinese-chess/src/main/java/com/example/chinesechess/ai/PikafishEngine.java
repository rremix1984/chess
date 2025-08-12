package com.example.chinesechess.ai;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pikafish引擎Java接口类
 * 提供与Pikafish UCI引擎的通信功能
 */
public class PikafishEngine {
    private Process engineProcess;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean isInitialized = false;
    private String enginePath;
    private static final int DEFAULT_TIMEOUT = 10000; // 10秒超时
    
    // 日志回调接口
    public interface LogCallback {
        void log(String message);
    }
    
    private LogCallback logCallback;
    
    /**
     * 构造函数
     * @param enginePath Pikafish引擎可执行文件路径
     */
    public PikafishEngine(String enginePath) {
        this.enginePath = enginePath;
    }
    
    /**
     * 设置日志回调
     * @param callback 日志回调接口
     */
    public void setLogCallback(LogCallback callback) {
        this.logCallback = callback;
    }
    
    /**
     * 记录日志
     * @param message 日志消息
     */
    private void log(String message) {
        if (logCallback != null) {
            logCallback.log(message);
        }
    }
    
    /**
     * 初始化引擎
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            // 检查引擎文件是否存在
            File engineFile = new File(enginePath);
            if (!engineFile.exists()) {
                System.err.println("Pikafish引擎文件不存在: " + enginePath);
                return false;
            }
            
            // 启动引擎进程
            ProcessBuilder pb = new ProcessBuilder(enginePath);
            pb.redirectErrorStream(true);
            engineProcess = pb.start();
            
            // 设置输入输出流
            writer = new BufferedWriter(new OutputStreamWriter(engineProcess.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            
            // 发送UCI初始化命令
            sendCommand("uci");
            
            // 等待uciok响应
            if (!waitForResponse("uciok", DEFAULT_TIMEOUT)) {
                System.err.println("引擎UCI初始化超时");
                return false;
            }
            
            // 设置引擎选项
            sendCommand("setoption name Threads value 2"); // 减少线程数以提高兼容性
            sendCommand("setoption name Hash value 64");    // 减少内存使用
            
            // 尝试设置神经网络文件路径
            File nnueFile = new File("pikafish.nnue");
            if (nnueFile.exists()) {
                String nnuePath = nnueFile.getAbsolutePath();
                sendCommand("setoption name EvalFile value " + nnuePath);
                System.out.println("设置神经网络文件: " + nnuePath);
            } else {
                System.out.println("警告: 未找到pikafish.nnue文件，尝试创建空文件");
                try {
                    // 创建一个空的神经网络文件来绕过检查
                    nnueFile.createNewFile();
                    String nnuePath = nnueFile.getAbsolutePath();
                    sendCommand("setoption name EvalFile value " + nnuePath);
                    System.out.println("创建并设置空神经网络文件: " + nnuePath);
                } catch (IOException e) {
                    System.out.println("无法创建神经网络文件，引擎可能无法正常工作");
                }
            }
            
            sendCommand("isready");
            
            // 等待readyok响应
            if (!waitForResponse("readyok", DEFAULT_TIMEOUT)) {
                System.err.println("引擎准备超时或失败");
                // 检查引擎进程是否还在运行
                if (engineProcess != null && !engineProcess.isAlive()) {
                    System.err.println("引擎进程已终止，可能是因为缺少神经网络文件");
                    cleanup();
                    return false;
                }
                return false;
            }
            
            isInitialized = true;
            System.out.println("Pikafish引擎初始化成功");
            return true;
            
        } catch (IOException e) {
            System.err.println("初始化Pikafish引擎失败: " + e.getMessage());
            cleanup();
            return false;
        }
    }
    
    /**
     * 发送命令到引擎
     * @param command 要发送的命令
     */
    private void sendCommand(String command) throws IOException {
        if (writer != null) {
            writer.write(command + "\n");
            writer.flush();
            System.out.println("发送命令: " + command);
        }
    }
    
    /**
     * 从引擎读取一行响应
     * @return 响应字符串
     */
    private String readLine() throws IOException {
        if (reader != null) {
            return reader.readLine();
        }
        return null;
    }
    
    /**
     * 等待特定响应
     * @param expectedResponse 期望的响应
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否收到期望响应
     */
    private boolean waitForResponse(String expectedResponse, int timeoutMs) {
        long startTime = System.currentTimeMillis();
        try {
            String response;
            while ((response = readLine()) != null) {
                System.out.println("引擎响应: " + response);
                if (response.equals(expectedResponse)) {
                    return true;
                }
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("读取引擎响应时发生错误: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 获取最佳走法
     * @param fen 当前局面的FEN字符串
     * @param thinkTime 思考时间（毫秒）
     * @return 最佳走法的UCI格式字符串，如"h2e2"
     */
    public String getBestMove(String fen, int thinkTime) {
        List<String> bestMoves = getBestMoves(fen, thinkTime, 1);
        if (bestMoves != null && !bestMoves.isEmpty()) {
            return bestMoves.get(0);
        }
        return null;
    }

    /**
     * 获取多个推荐走法及其分析
     * @param fen 当前局面的FEN字符串
     * @param numPV 要获取的最佳走法数量
     * @param thinkTime 思考时间（毫秒）
     * @return 包含多个走法分析结果的列表
     */
    public List<String> getMultiPVAnalysis(String fen, int numPV, int thinkTime) {
        List<String> analysisResults = new ArrayList<>();
        if (!isInitialized) {
            log("Pikafish engine not initialized.");
            return analysisResults;
        }

        try {
            sendCommand("position fen " + fen);
            sendCommand("setoption name MultiPV value " + numPV);
            sendCommand("go movetime " + thinkTime);

            long startTime = System.currentTimeMillis();
            String line;
            while ((line = readLine()) != null) {
                log("引擎响应 (MultiPV): " + line);
                if (line.startsWith("info depth")) {
                    // 提取并格式化分析结果
                    // 示例: info depth 1 seldepth 1 multipv 1 score cp 100 nodes 200 time 10 pv e2e4
                    // 提取 multipv, score, pv
                    String pvInfo = extractPVInfo(line);
                    if (!pvInfo.isEmpty()) {
                        analysisResults.add(pvInfo);
                    }
                }
                if (line.startsWith("bestmove")) {
                    break; // 收到bestmove表示分析结束
                }
                if (System.currentTimeMillis() - startTime > thinkTime + 2000) { // 额外2秒容错
                    log("MultiPV分析超时");
                    break;
                }
            }
            sendCommand("setoption name MultiPV value 1"); // 恢复默认值
        } catch (IOException e) {
            log("获取MultiPV分析失败: " + e.getMessage());
        }
        return analysisResults;
    }

    private String extractPVInfo(String line) {
        Pattern pattern = Pattern.compile("multipv (\\d+) score (cp|mate) ([-]?\\d+)(?: nodes (\\d+))? time (\\d+) pv (.+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String multiPV = matcher.group(1);
            String scoreType = matcher.group(2);
            String scoreValue = matcher.group(3);
            String pvMoves = matcher.group(6);

            String formattedScore;
            if (scoreType.equals("cp")) {
                formattedScore = String.format("Score: %.2f", Double.parseDouble(scoreValue) / 100.0);
            } else {
                formattedScore = "Mate in " + scoreValue;
            }
            return String.format("Rank %s: %s, Moves: %s", multiPV, formattedScore, pvMoves);
        }
        return "";
    }

    /**
     * 获取最佳走法
     * @param fen 当前局面的FEN字符串
     * @param thinkTime 思考时间（毫秒）
     * @param numPV 要获取的最佳走法数量
     * @return 最佳走法的UCI格式字符串列表
     */
    public List<String> getBestMoves(String fen, int thinkTime, int numPV) {
        if (!isAvailable()) {
            log("Pikafish引擎不可用");
            System.err.println("引擎不可用");
            return null;
        }
        
        try {
            log("设置局面: " + fen);
            // 设置局面
            sendCommand("position fen " + fen);
            
            // 设置多PV模式
            sendCommand("setoption name MultiPV value " + numPV);

            // 根据思考时间计算搜索深度，增加AI智能
            int searchDepth = calculateSearchDepth(thinkTime);
            log("开始计算，思考时间: " + thinkTime + "ms, 搜索深度: " + searchDepth);
            
            // 使用深度搜索而不是时间限制，提高AI决策质量
            if (searchDepth > 0) {
                sendCommand("go depth " + searchDepth);
            } else {
                sendCommand("go movetime " + thinkTime);
            }
            
            // 读取响应，寻找bestmove
            long startTime = System.currentTimeMillis();
            String response;
            
            StringBuilder analysisInfo = new StringBuilder();
            List<String> moves = new ArrayList<>();
            while ((response = readLine()) != null) {
                System.out.println("引擎响应: " + response);
                
                // 保存分析信息
                if (response.startsWith("info")) {
                    analysisInfo.append(response).append("\n");
                    if (response.contains(" pv ")) {
                        String[] parts = response.split(" pv ");
                        if (parts.length > 1) {
                            String[] moveParts = parts[1].split(" ");
                            if (moveParts.length > 0) {
                                if (!moves.contains(moveParts[0])) {
                                    moves.add(moveParts[0]);
                                }
                            }
                        }
                    }
                }
                
                if (response.startsWith("bestmove")) {
                    String[] parts = response.split(" ");
                    if (parts.length >= 2 && !parts[1].equals("(none)")) {
                        if (!moves.contains(parts[1])) {
                           moves.add(parts[1]);
                        }
                        lastAnalysisInfo = analysisInfo.toString();
                        return moves;
                    }
                    break;
                }
                // 防止无限等待 - 优化超时机制
                if (System.currentTimeMillis() - startTime > thinkTime + 1000) {
                    log("计算超时，已用时: " + (System.currentTimeMillis() - startTime) + "ms");
                    System.err.println("获取最佳走法超时");
                    break;
                }
            }
            
        } catch (IOException e) {
            log("计算过程发生错误: " + e.getMessage());
            System.err.println("获取最佳走法时发生错误: " + e.getMessage());
        }
        
        return null;
    }

    
    /**
     * 根据思考时间计算搜索深度
     * @param thinkTime 思考时间（毫秒）
     * @return 搜索深度
     */
    private int calculateSearchDepth(int thinkTime) {
        // 根据思考时间映射到搜索深度，大幅提高AI智能水平
        if (thinkTime <= 300) {
            return 12;  // 难度1：深度12
        } else if (thinkTime <= 800) {
            return 14;  // 难度2：深度14
        } else if (thinkTime <= 1500) {
            return 16;  // 难度3：深度16
        } else if (thinkTime <= 2500) {
            return 18;  // 难度4：深度18
        } else if (thinkTime <= 4000) {
            return 20;  // 难度5：深度20
        } else if (thinkTime <= 6000) {
            return 22;  // 难度6：深度22
        } else if (thinkTime <= 10000) {
            return 24;  // 难度7：深度24
        } else if (thinkTime <= 15000) {
            return 26;  // 难度8：深度26
        } else if (thinkTime <= 25000) {
            return 28;  // 难度9：深度28
        } else {
            return 30;  // 最高难度：深度30
        }
    }
    
    /**
     * 获取局面评估分数
     * @param fen 当前局面的FEN字符串
     * @param depth 搜索深度
     * @return 评估分数（厘兵为单位）
     */
    public int getEvaluation(String fen, int depth) {
        if (!isAvailable()) {
            System.err.println("引擎不可用");
            return 0;
        }
        
        try {
            // 设置局面
            sendCommand("position fen " + fen);
            
            // 开始分析
            sendCommand("go depth " + depth);
            
            // 读取响应，寻找评估信息
            String response;
            int lastScore = 0;
            long startTime = System.currentTimeMillis();
            
            while ((response = readLine()) != null) {
                System.out.println("引擎响应: " + response);
                if (response.startsWith("info") && response.contains("score cp")) {
                    // 解析评估分数
                    String[] parts = response.split(" ");
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (parts[i].equals("cp")) {
                            try {
                                lastScore = Integer.parseInt(parts[i + 1]);
                            } catch (NumberFormatException e) {
                                // 忽略解析错误
                            }
                            break;
                        }
                    }
                } else if (response.startsWith("bestmove")) {
                    break;
                }
                
                // 防止无限等待
                if (System.currentTimeMillis() - startTime > 30000) {
                    System.err.println("获取评估分数超时");
                    break;
                }
            }
            
            return lastScore;
            
        } catch (IOException e) {
            System.err.println("获取评估分数时发生错误: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * 检查引擎是否可用
     * @return 是否可用
     */
    public boolean isAvailable() {
        return isInitialized && engineProcess != null && engineProcess.isAlive();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (engineProcess != null) {
                engineProcess.destroyForcibly();
                engineProcess = null;
            }
        } catch (IOException e) {
            System.err.println("清理资源时发生错误: " + e.getMessage());
        }
        isInitialized = false;
    }
    
    /**
     * 关闭引擎
     */
    public void quit() {
        try {
            if (isInitialized && writer != null) {
                sendCommand("quit");
                // 给引擎一些时间来正常退出
                Thread.sleep(1000);
            }
            
            if (engineProcess != null) {
                // 等待进程结束，最多等待3秒
                if (!engineProcess.waitFor(3, TimeUnit.SECONDS)) {
                    System.out.println("强制终止引擎进程");
                    engineProcess.destroyForcibly();
                }
            }
            
        } catch (IOException | InterruptedException e) {
            System.err.println("关闭引擎时发生错误: " + e.getMessage());
        } finally {
            cleanup();
            System.out.println("Pikafish引擎已关闭");
        }
    }
    
    /**
     * 获取引擎信息
     * @return 引擎信息字符串
     */
    public String getEngineInfo() {
        if (!isAvailable()) {
            return "引擎不可用";
        }
        
        try {
            sendCommand("uci");
            
            StringBuilder info = new StringBuilder();
            String response;
            long startTime = System.currentTimeMillis();
            
            while ((response = readLine()) != null) {
                if (response.startsWith("id name") || response.startsWith("id author")) {
                    info.append(response).append("\n");
                } else if (response.equals("uciok")) {
                    break;
                }
                
                // 防止无限等待
                if (System.currentTimeMillis() - startTime > 5000) {
                    break;
                }
            }
            
            return info.length() > 0 ? info.toString() : "无法获取引擎信息";
            
        } catch (IOException e) {
            return "获取引擎信息失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取引擎状态信息
     * @return 状态信息
     */
    public String getStatus() {
        if (!isInitialized) {
            return "未初始化";
        } else if (!isAvailable()) {
            return "引擎进程已停止";
        } else {
            return "运行中";
        }
    }
    
    /**
     * 设置棋盘局面
     * @param fen FEN格式的局面字符串
     */
    public void setPosition(String fen) {
        if (!isAvailable()) {
            log("引擎不可用，无法设置局面");
            return;
        }
        
        try {
            log("设置局面: " + fen);
            sendCommand("position fen " + fen);
        } catch (IOException e) {
            log("设置局面失败: " + e.getMessage());
        }
    }
    
    private String lastAnalysisInfo = "";
    
    /**
     * 获取最后一次分析的详细信息
     * @return 分析信息字符串
     */
    public String getLastAnalysisInfo() {
        return lastAnalysisInfo;
    }
}