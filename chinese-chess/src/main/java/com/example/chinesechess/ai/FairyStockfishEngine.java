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
    private String neuralNetworkPath; // 神经网络文件路径
    
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
     * 设置神经网络文件路径
     */
    public void setNeuralNetworkPath(String neuralNetworkPath) {
        this.neuralNetworkPath = neuralNetworkPath;
        log("设置神经网络文件: " + neuralNetworkPath);
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
            
            // 设置神经网络文件路径（如果指定）
            if (neuralNetworkPath != null && !neuralNetworkPath.isEmpty()) {
                File nnFile = new File(neuralNetworkPath);
                if (nnFile.exists()) {
                    sendCommand("setoption name EvalFile value " + neuralNetworkPath);
                    log("加载神经网络文件: " + neuralNetworkPath);
                } else {
                    log("警告：神经网络文件不存在: " + neuralNetworkPath);
                }
            }
            
            // === 根据专业建议进行极致优化 ===
            log("正在应用专业级引擎优化配置...");
            
            // 1. 最大棋力设置 - 关闭所有限制
            sendCommand("setoption name Skill Level value 20");           // 最高技能等级
            sendCommand("setoption name UCI_LimitStrength value false");   // 禁用棋力限制
            sendCommand("setoption name Contempt value 0");               // 无偏见评估
            sendCommand("setoption name Nodestime value 0");              // 禁用节点限制
            
            // 2. 大内存配置 - 最大化置换表大小
            sendCommand("setoption name Hash value 1024");               // 1GB Hash表（专业级）
            sendCommand("setoption name Clear Hash value true");          // 清除旧Hash数据
            
            // 3. 多线程优化 - 充分利用CPU核心
            int cpuCores = Runtime.getRuntime().availableProcessors();
            int threads = Math.max(4, Math.min(cpuCores, 16)); // 4-16线程范围
            sendCommand("setoption name Threads value " + threads);
            log("设置线程数: " + threads + " (CPU核心数: " + cpuCores + ")");
            
            // 4. 搜索优化 - 提升搜索质量
            sendCommand("setoption name Ponder value true");              // 启用后台思考
            sendCommand("setoption name UCI_AnalyseMode value true");     // 分析模式
            sendCommand("setoption name MultiPV value 1");                // 主要变例数
            sendCommand("setoption name Move Overhead value 50");         // 减少移动开销
            
            // 5. 评估优化 - 启用最强神经网络
            sendCommand("setoption name UCI_ShowWDL value true");         // 显示胜负平概率
            sendCommand("setoption name SyzygyPath value clear");         // 清除残局库路径
            
            // 6. 时间管理优化
            sendCommand("setoption name Minimum Thinking Time value 1000"); // 最少思考时间
            sendCommand("setoption name Slow Mover value 100");            // 减缓速度设置
            
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
     * 根据思考时间计算最优搜索深度（优化版）
     * @param thinkTimeMs 思考时间（毫秒）
     * @return 搜索深度
     */
    private int calculateSearchDepth(int thinkTimeMs) {
        // 新的增强算法，为高难度级别提供更深的搜索
        if (thinkTimeMs <= 3000) {
            return 12;   // 3秒及以下: 中等深度
        } else if (thinkTimeMs <= 8000) {
            return 15;   // 8秒及以下: 较深搜索
        } else if (thinkTimeMs <= 15000) {
            return 18;   // 15秒及以下: 深度搜索
        } else if (thinkTimeMs <= 30000) {
            return 22;   // 30秒及以下: 高深度搜索
        } else if (thinkTimeMs <= 60000) {
            return 26;   // 1分钟及以下: 非常深度搜索
        } else {
            return 30;   // 超过1分钟: 最深搜索，极致棋力
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
