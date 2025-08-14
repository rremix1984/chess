package com.example.chinesechess.ai;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Fairy-Stockfish 引擎接口类
 * Fairy-Stockfish 是一个支持多种棋类变体的引擎，包括中国象棋
 */
public class FairyStockfishEngine {
    
    private Process engineProcess;
    private BufferedWriter engineInput;
    private BufferedReader engineOutput;
    private boolean isInitialized = false;
    private boolean isAvailable = false;
    private String enginePath;
    private Consumer<String> logCallback;
    private String engineInfo = "";
    
    public FairyStockfishEngine(String enginePath) {
        this.enginePath = enginePath != null ? enginePath : "fairy-stockfish";
    }
    
    /**
     * 设置日志回调
     */
    public void setLogCallback(Consumer<String> logCallback) {
        this.logCallback = logCallback;
    }
    
    /**
     * 记录日志
     */
    private void log(String message) {
        System.out.println("[FairyStockfish] " + message);
        if (logCallback != null) {
            logCallback.accept("[FairyStockfish] " + message);
        }
    }
    
    /**
     * 初始化引擎
     */
    public boolean initialize() {
        try {
            log("正在启动 Fairy-Stockfish 引擎...");
            
            // 启动引擎进程
            ProcessBuilder pb = new ProcessBuilder(enginePath);
            pb.redirectErrorStream(true);
            engineProcess = pb.start();
            
            // 获取输入输出流
            engineInput = new BufferedWriter(new OutputStreamWriter(engineProcess.getOutputStream()));
            engineOutput = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            
            // 发送 UCI 命令
            sendCommand("uci");
            
            // 读取引擎响应并等待 uciok
            String line;
            StringBuilder infoBuilder = new StringBuilder();
            boolean receivedUciOk = false;
            
            long startTime = System.currentTimeMillis();
            while ((line = engineOutput.readLine()) != null) {
                infoBuilder.append(line).append("\n");
                
                if (line.startsWith("id name")) {
                    engineInfo = line.substring(8).trim();
                    log("引擎名称: " + engineInfo);
                } else if (line.startsWith("id author")) {
                    log("引擎作者: " + line.substring(10).trim());
                } else if (line.equals("uciok")) {
                    receivedUciOk = true;
                    break;
                }
                
                // 超时保护
                if (System.currentTimeMillis() - startTime > 10000) {
                    log("引擎初始化超时");
                    break;
                }
            }
            
            if (!receivedUciOk) {
                log("引擎未正确响应 UCI 协议");
                cleanup();
                return false;
            }
            
            // 设置中国象棋变体
            sendCommand("setoption name UCI_Variant value xiangqi");
            
            // 配置引擎强度 - 增强游戏水平
            // 设置Hash表大小为256MB，提升搜索效率
            sendCommand("setoption name Hash value 256");
            
            // 设置线程数为4，提升多核CPU性能
            sendCommand("setoption name Threads value 4");
            
            // 启用Ponder（思考对手时间），增强分析深度
            sendCommand("setoption name Ponder value true");
            
            // 设置技能等级为最高（20级），确保最强棋力
            sendCommand("setoption name Skill Level value 20");
            
            // 禁用随机性，确保最佳走法
            sendCommand("setoption name UCI_LimitStrength value false");
            
            // 设置更深的搜索策略
            sendCommand("setoption name Contempt value 0");
            
            // 等待引擎准备就绪
            sendCommand("isready");
            while ((line = engineOutput.readLine()) != null) {
                if (line.equals("readyok")) {
                    break;
                }
                // 超时保护
                if (System.currentTimeMillis() - startTime > 15000) {
                    log("引擎准备就绪超时");
                    break;
                }
            }
            
            isInitialized = true;
            isAvailable = true;
            log("Fairy-Stockfish 引擎初始化成功");
            log("支持中国象棋变体 (xiangqi)");
            
            return true;
            
        } catch (IOException e) {
            log("引擎初始化失败: " + e.getMessage());
            cleanup();
            return false;
        } catch (Exception e) {
            log("引擎初始化异常: " + e.getMessage());
            cleanup();
            return false;
        }
    }
    
    /**
     * 发送命令到引擎
     */
    private void sendCommand(String command) throws IOException {
        if (engineInput != null) {
            engineInput.write(command + "\n");
            engineInput.flush();
            // 只记录重要命令，避免日志过多
            if (command.equals("uci") || command.equals("isready") || command.startsWith("go ")) {
                log("发送命令: " + command);
            }
        }
    }
    
    /**
     * 获取最佳走法
     * @param fen FEN 格式的棋局
     * @param thinkTimeMs 思考时间（毫秒）
     * @return UCI 格式的走法
     */
    public String getBestMove(String fen, int thinkTimeMs) {
        if (!isAvailable) {
            log("引擎不可用");
            return null;
        }
        
        try {
            // 设置位置
            sendCommand("position fen " + fen);
            
            // 使用混合搜索策略：时间限制 + 深度限制
            // 根据思考时间计算搜索深度
            int searchDepth = calculateSearchDepth(thinkTimeMs);
            String searchCommand = String.format("go movetime %d depth %d", thinkTimeMs, searchDepth);
            log("开始搜索 - 深度: " + searchDepth + " 时间: " + thinkTimeMs + "ms");
            sendCommand(searchCommand);
            
            // 读取引擎响应
            String line;
            String bestMove = null;
            long startTime = System.currentTimeMillis();
            
            while ((line = engineOutput.readLine()) != null) {
                // 只记录关键信息，避免日志过于啰嗦
                if (line.startsWith("bestmove")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        bestMove = parts[1];
                        log("找到最佳走法: " + bestMove);
                        break;
                    }
                } else if (line.startsWith("info")) {
                    // 只记录最终深度的主要变化，忽略中间搜索过程
                    if (line.contains(" depth ")) {
                        // 解析深度信息
                        String[] parts = line.split(" ");
                        for (int i = 0; i < parts.length - 1; i++) {
                            if ("depth".equals(parts[i]) && i + 1 < parts.length) {
                                try {
                                    int currentDepth = Integer.parseInt(parts[i + 1]);
                                    // 只记录每5层深度的进展
                                    if (currentDepth % 5 == 0 && line.contains(" pv ")) {
                                        log("搜索深度 " + currentDepth + " 层完成");
                                    }
                                } catch (NumberFormatException e) {
                                    // 忽略解析错误
                                }
                                break;
                            }
                        }
                    }
                }
                
                // 超时保护
                if (System.currentTimeMillis() - startTime > thinkTimeMs + 5000) {
                    log("搜索超时");
                    break;
                }
            }
            
            return bestMove;
            
        } catch (IOException e) {
            log("获取最佳走法时发生错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 评估当前局面
     * @param fen FEN 格式的棋局
     * @return 评估分数（厘兵为单位）
     */
    public int evaluatePosition(String fen) {
        if (!isAvailable) {
            return 0;
        }
        
        try {
            // 设置位置
            sendCommand("position fen " + fen);
            
            // 进行浅层搜索获取评估
            sendCommand("go depth 10");
            
            String line;
            int evaluation = 0;
            long startTime = System.currentTimeMillis();
            
            while ((line = engineOutput.readLine()) != null) {
                if (line.startsWith("info") && line.contains(" score cp ")) {
                    // 解析评估分数
                    String[] parts = line.split(" ");
                    for (int i = 0; i < parts.length - 1; i++) {
                        if ("cp".equals(parts[i]) && i + 1 < parts.length) {
                            try {
                                evaluation = Integer.parseInt(parts[i + 1]);
                            } catch (NumberFormatException e) {
                                // 忽略解析错误
                            }
                            break;
                        }
                    }
                } else if (line.startsWith("bestmove")) {
                    break;
                }
                
                // 超时保护
                if (System.currentTimeMillis() - startTime > 10000) {
                    break;
                }
            }
            
            return evaluation;
            
        } catch (IOException e) {
            log("评估局面时发生错误: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 根据思考时间计算最优搜索深度
     * @param thinkTimeMs 思考时间（毫秒）
     * @return 搜索深度
     */
    private int calculateSearchDepth(int thinkTimeMs) {
        // 基于思考时间动态计算深度，确保强棋力
        if (thinkTimeMs <= 2000) {
            return 12;  // 短时间：中等深度
        } else if (thinkTimeMs <= 5000) {
            return 15;  // 中等时间：更深搜索
        } else if (thinkTimeMs <= 10000) {
            return 18;  // 较长时间：深度搜索
        } else if (thinkTimeMs <= 20000) {
            return 22;  // 长时间：非常深度的搜索
        } else {
            return 25;  // 最长时间：最深搜索，确保最强棋力
        }
    }
    
    /**
     * 检查引擎是否可用
     */
    public boolean isAvailable() {
        return isInitialized && isAvailable && engineProcess != null && engineProcess.isAlive();
    }
    
    /**
     * 获取引擎信息
     */
    public String getEngineInfo() {
        if (engineInfo.isEmpty()) {
            return "Fairy-Stockfish (多变体象棋引擎)";
        }
        return engineInfo;
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        try {
            if (engineInput != null) {
                sendCommand("quit");
                engineInput.close();
            }
        } catch (IOException e) {
            // 忽略清理时的错误
        }
        
        try {
            if (engineOutput != null) {
                engineOutput.close();
            }
        } catch (IOException e) {
            // 忽略清理时的错误
        }
        
        if (engineProcess != null) {
            engineProcess.destroyForcibly();
            try {
                engineProcess.waitFor(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        isInitialized = false;
        isAvailable = false;
        log("引擎资源已清理");
    }
    
    /**
     * 析构函数
     */
    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }
}
