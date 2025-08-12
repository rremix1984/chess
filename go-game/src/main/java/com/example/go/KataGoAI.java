package com.example.go;

import com.example.common.config.ConfigurationManager;
import com.example.common.utils.ExceptionHandler;
import com.example.common.utils.ResourceManager;
import com.example.common.utils.KataGoInstaller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KataGo AI引擎集成类 - 专业级围棋AI
 * 提供强大的围棋AI对弈和分析功能
 */
public class KataGoAI {
    private static final String LOG_TAG = "KataGo AI";
    
    private Process katagoProcess;
    private BufferedReader katagoReader;
    private BufferedWriter katagoWriter;
    private volatile boolean engineInitialized = false;
    private volatile boolean isShutdown = false;
    
    private final ConfigurationManager config;
    private final ConfigurationManager.KataGoConfig katagoConfig;
    private final ExecutorService executorService;
    
    private int difficulty = 5;
    private int visits = 1600;
    private double timeLimit = 5.0;
    
    // AI分析和决策数据
    private GoAnalysis lastAnalysis;
    private List<String> moveHistory = new ArrayList<>();
    private volatile boolean isThinking = false;
    
    // 性能统计
    private long totalThinkTime = 0;
    private int totalMoves = 0;
    
    public KataGoAI(int difficulty) {
        this.config = ConfigurationManager.getInstance();
        this.katagoConfig = config.getKataGoConfig();
        this.difficulty = Math.max(1, Math.min(10, difficulty));
        this.visits = config.getGoAIVisits(this.difficulty);
        this.executorService = ResourceManager.getExecutorService();
        
        ExceptionHandler.logInfo(LOG_TAG, "🔧 配置信息加载完成:");
        ExceptionHandler.logInfo(LOG_TAG, "   - KataGo引擎: " + katagoConfig.enginePath);
        ExceptionHandler.logInfo(LOG_TAG, "   - 神经网络模型: " + katagoConfig.modelPath);
        ExceptionHandler.logInfo(LOG_TAG, "   - 配置文件: " + katagoConfig.configPath);
        ExceptionHandler.logInfo(LOG_TAG, "   - 访问数: " + visits + " (难度: " + difficulty + ")");
    }
    
    /**
     * 初始化KataGo引擎
     */
    public boolean initializeEngine() {
        if (engineInitialized) {
            return true;
        }
        
        try {
            ExceptionHandler.logInfo(LOG_TAG, "🚀 正在启动KataGo引擎...");
            
            // 首先检查KataGo是否已安装
            KataGoInstaller installer = KataGoInstaller.getInstance();
            if (!installer.isKataGoInstalled()) {
                ExceptionHandler.logInfo(LOG_TAG, "⚠️ 检测到KataGo未安装，开始自动安装...");
                
                // 在GUI线程中显示安装进度
                final boolean[] installResult = {false};
                final CountDownLatch latch = new CountDownLatch(1);
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        int choice = JOptionPane.showConfirmDialog(
                            null,
                            "围棋AI需要KataGo引擎支持。\n检测到KataGo未安装，是否立即下载安装？\n\n" +
                            "安装将下载约100MB的文件，请确保网络连接正常。",
                            "安装KataGo引擎",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                        );
                        
                        if (choice == JOptionPane.YES_OPTION) {
                            // 显示安装进度对话框
                            javax.swing.ProgressMonitor progressMonitor = new javax.swing.ProgressMonitor(
                                null,
                                "正在安装KataGo引擎...",
                                "初始化...",
                                0, 100
                            );
                            progressMonitor.setMillisToDecideToPopup(0);
                            progressMonitor.setMillisToPopup(0);
                            
                            // 在后台线程中执行安装
                            new Thread(() -> {
                                try {
                                    boolean success = installer.installKataGo(new KataGoInstaller.ProgressCallback() {
                                        @Override
                                        public void onProgress(int percentage, String message) {
                                            SwingUtilities.invokeLater(() -> {
                                                progressMonitor.setProgress(percentage);
                                                progressMonitor.setNote(message);
                                                
                                                if (progressMonitor.isCanceled()) {
                                                    // 用户取消安装
                                                    return;
                                                }
                                            });
                                        }
                                    });
                                    
                                    SwingUtilities.invokeLater(() -> {
                                        progressMonitor.close();
                                        if (success) {
                                            JOptionPane.showMessageDialog(
                                                null,
                                                "KataGo引擎安装成功！\n现在可以开始围棋游戏了。",
                                                "安装成功",
                                                JOptionPane.INFORMATION_MESSAGE
                                            );
                                        } else {
                                            JOptionPane.showMessageDialog(
                                                null,
                                                "KataGo引擎安装失败！\n请检查网络连接或手动安装。",
                                                "安装失败",
                                                JOptionPane.ERROR_MESSAGE
                                            );
                                        }
                                    });
                                    
                                    installResult[0] = success;
                                } finally {
                                    latch.countDown();
                                }
                            }).start();
                        } else {
                            installResult[0] = false;
                            latch.countDown();
                        }
                    } catch (Exception e) {
                        ExceptionHandler.logError(LOG_TAG, "安装过程中出现异常: " + e.getMessage());
                        installResult[0] = false;
                        latch.countDown();
                    }
                });
                
                // 等待安装完成
                try {
                    latch.await(5, TimeUnit.MINUTES); // 最多等待5分钟
                } catch (InterruptedException e) {
                    ExceptionHandler.logError(LOG_TAG, "等待安装过程被中断: " + e.getMessage());
                    return false;
                }
                
                if (!installResult[0]) {
                    ExceptionHandler.logError(LOG_TAG, "❌ KataGo安装失败或被用户取消");
                    return false;
                }
            }
            
            // 更新配置以使用已安装的KataGo
            String installedKataGoPath = installer.getKataGoExecutablePath();
            String installedModelPath = installer.getModelPath();
            String installedConfigPath = installer.getConfigPath();
            
            ConfigurationManager.KataGoConfig actualConfig;
            if (installedKataGoPath != null) {
                actualConfig = new ConfigurationManager.KataGoConfig(
                    installedKataGoPath,
                    installedModelPath != null ? installedModelPath : katagoConfig.modelPath,
                    installedConfigPath != null ? installedConfigPath : katagoConfig.configPath,
                    katagoConfig.threads,
                    katagoConfig.timePerMove,
                    katagoConfig.resignThreshold
                );
            } else {
                actualConfig = katagoConfig;
            }
            
            // 检查引擎文件是否存在
            File engineFile = new File(actualConfig.enginePath);
            if (!engineFile.exists() || !engineFile.canExecute()) {
                ExceptionHandler.logError(LOG_TAG, "❌ KataGo引擎文件不存在或无法执行: " + actualConfig.enginePath);
                return false;
            }
            
            // 检查模型文件是否存在
            File modelFile = new File(actualConfig.modelPath);
            if (!modelFile.exists()) {
                ExceptionHandler.logError(LOG_TAG, "❌ 神经网络模型文件不存在: " + actualConfig.modelPath);
                return false;
            }
            
            // 构建启动命令
            List<String> command = new ArrayList<>();
            command.add(actualConfig.enginePath);
            command.add("gtp");
            command.add("-model");
            command.add(actualConfig.modelPath);
            if (actualConfig.configPath != null && !actualConfig.configPath.isEmpty()) {
                command.add("-config");
                command.add(actualConfig.configPath);
            }
            
            // 启动进程
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            katagoProcess = pb.start();
            
            // 设置输入输出流
            katagoReader = new BufferedReader(new InputStreamReader(katagoProcess.getInputStream()));
            katagoWriter = new BufferedWriter(new OutputStreamWriter(katagoProcess.getOutputStream()));
            
            // 设置初始化标志，便于配置命令可以发送
            engineInitialized = true;
            
            // 初始化引擎配置
            if (!configureEngine()) {
                engineInitialized = false;
                shutdownEngine();
                return false;
            }
            ExceptionHandler.logInfo(LOG_TAG, "✅ KataGo引擎初始化成功");
            return true;
            
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "❌ KataGo引擎初始化失败: " + e.getMessage());
            shutdownEngine();
            return false;
        }
    }
    
    /**
     * 配置KataGo引擎参数
     */
    private boolean configureEngine() throws IOException {
        try {
            // 设置棋盘大小
            sendCommand("boardsize " + GoGame.BOARD_SIZE);
            
            // 设置时间控制 - 给予足够的思考时间
            sendCommand("time_settings 0 " + katagoConfig.timePerMove + " 1");
            
            // 设置访问数限制，确保有足够的搜索深度
            int actualVisits = Math.max(visits, 300); // 最少300次访问，确保能找到好的走法
            sendCommand("kata-set-param maxVisits " + actualVisits);
            ExceptionHandler.logInfo(LOG_TAG, "设置访问数: " + actualVisits);
            
            // 设置线程数
            if (katagoConfig.threads > 0) {
                sendCommand("kata-set-param numSearchThreads " + katagoConfig.threads);
            }
            
            // 禁止弃权，强制AI下棋
            sendCommand("kata-set-param allowResignation false");
            ExceptionHandler.logInfo(LOG_TAG, "禁止AI弃权");
            
            // 设置其他参数
            sendCommand("kata-set-param ponderingEnabled false"); // 禁用后台思考
            sendCommand("kata-set-param conservativePass false"); // 不保守地弃权
            
            // 设置搜索参数，提高走法质量
            sendCommand("kata-set-param rootNoiseEnabled false"); // 禁用根节点噪声，提高稳定性
            
            return true;
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "配置引擎参数失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送GTP命令到KataGo
     */
    private String sendCommand(String command) throws IOException {
        if (!engineInitialized || isShutdown) {
            throw new IllegalStateException("KataGo引擎未初始化");
        }
        
        ExceptionHandler.logDebug(LOG_TAG, "发送命令: " + command);
        katagoWriter.write(command + "\n");
        katagoWriter.flush();
        
        StringBuilder response = new StringBuilder();
        String line;
        boolean foundResponse = false;
        
        while ((line = katagoReader.readLine()) != null) {
            ExceptionHandler.logDebug(LOG_TAG, "读取行: '" + line + "'");
            
            // 跳过空行
            if (line.trim().isEmpty()) {
                continue;
            }
            
            response.append(line).append("\n");
            
            // 检查是否是最终响应
            if (line.startsWith("=") || line.startsWith("?")) {
                foundResponse = true;
                break;
            }
        }
        
        String result = response.toString().trim();
        ExceptionHandler.logDebug(LOG_TAG, "引擎响应: '" + result + "' (找到响应: " + foundResponse + ")");
        
        if (result.startsWith("?")) {
            throw new RuntimeException("KataGo命令执行失败: " + result);
        }
        
        // 如果响应只有“="号而没有内容，返回空字符串
        if (result.equals("=")) {
            ExceptionHandler.logError(LOG_TAG, "KataGo返回空移动响应");
            return "= pass"; // 强制返回pass以避免解析错误
        }
        
        return result;
    }
    
    /**
     * 设置棋盘状态
     */
    public void setBoardState(int[][] board, int currentPlayer) {
        if (!engineInitialized) {
            return;
        }
        
        try {
            // 清空棋盘
            sendCommand("clear_board");
            
            // 设置棋盘大小
            sendCommand("boardsize " + GoGame.BOARD_SIZE);
            
            // 统计棋盘上的棋子数量，用于调试
            int stoneCount = 0;
            
            // 设置棋子
            for (int row = 0; row < GoGame.BOARD_SIZE; row++) {
                for (int col = 0; col < GoGame.BOARD_SIZE; col++) {
                    if (board[row][col] != GoGame.EMPTY) {
                        String color = (board[row][col] == GoGame.BLACK) ? "black" : "white";
                        String move = convertToGTPPosition(row, col);
                        sendCommand("play " + color + " " + move);
                        stoneCount++;
                    }
                }
            }
            
            ExceptionHandler.logDebug(LOG_TAG, "棋盘状态设置完成 - 棋子数量: " + stoneCount + ", 当前玩家: " + (currentPlayer == GoGame.BLACK ? "黑棋" : "白棋"));
            
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "设置棋盘状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算最佳移动
     */
    public GoPosition calculateBestMove(int[][] board, int currentPlayer) {
        if (!engineInitialized) {
            ExceptionHandler.logError(LOG_TAG, "❌ KataGo引擎未初始化");
            return null;
        }
        
        isThinking = true;
        long startTime = System.currentTimeMillis();
        
        try {
            ExceptionHandler.logInfo(LOG_TAG, "🧠 KataGo AI思考中...");
            
            // 设置当前棋盘状态
            setBoardState(board, currentPlayer);
            
            // 获取AI移动
            String color = (currentPlayer == GoGame.BLACK) ? "black" : "white";
            String response = sendCommand("genmove " + color);
            
            // 解析响应
            GoPosition move = parseGTPMove(response);
            
            // 如果KataGo选择弃权，强制生成一个有效移动
            if (move == null) {
                ExceptionHandler.logInfo(LOG_TAG, "KataGo选择弃权，强制生成一个有效移动");
                move = generateForcedMove(board, currentPlayer);
            }
            
            long thinkTime = System.currentTimeMillis() - startTime;
            totalThinkTime += thinkTime;
            totalMoves++;
            
            if (move != null) {
                moveHistory.add(convertToGTPPosition(move.row, move.col));
                String numericCoord = convertToNumericPosition(move.row, move.col);
                ExceptionHandler.logInfo(LOG_TAG, "✅ KataGo选择走法: " + numericCoord + " (用时: " + thinkTime + "ms)");
            } else {
                ExceptionHandler.logInfo(LOG_TAG, "🏳️ KataGo选择弃权");
            }
            
            return move;
            
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "计算最佳移动失败: " + e.getMessage());
            return null;
        } finally {
            isThinking = false;
        }
    }
    
    /**
     * 获取位置分析
     */
    public GoAnalysis analyzePosition(int[][] board, int currentPlayer) {
        if (!engineInitialized) {
            return null;
        }
        
        try {
            setBoardState(board, currentPlayer);
            
            String color = (currentPlayer == GoGame.BLACK) ? "black" : "white";
            String response = sendCommand("kata-analyze " + color + " " + visits);
            
            GoAnalysis analysis = parseAnalysisResponse(response);
            lastAnalysis = analysis;
            
            return analysis;
            
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "分析位置失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析GTP移动响应
     */
    private GoPosition parseGTPMove(String response) {
        if (response == null || response.isEmpty()) {
            ExceptionHandler.logError(LOG_TAG, "KataGo返回空响应");
            return null;
        }
        
        // 记录原始响应，用于调试
        ExceptionHandler.logInfo(LOG_TAG, "原始GTP响应: '" + response + "'");
        
        // 移除=号和空格
        String move = response.replaceFirst("^=\\s*", "").trim();
        ExceptionHandler.logInfo(LOG_TAG, "解析后的走法: '" + move + "'");
        
        // 检查是否弃权
        if ("pass".equalsIgnoreCase(move) || "PASS".equalsIgnoreCase(move)) {
            ExceptionHandler.logInfo(LOG_TAG, "KataGo选择弃权 - 走法字符串: '" + move + "'");
            return null;
        }
        
        // 解析坐标 (如 "D4")
        if (move.length() >= 2) {
            try {
                char col = move.charAt(0);
                int row = Integer.parseInt(move.substring(1));
                GoPosition pos = convertFromGTPPosition(col, row);
                ExceptionHandler.logInfo(LOG_TAG, "成功解析走法: " + move + " -> (" + pos.row + ", " + pos.col + ")");
                return pos;
            } catch (Exception e) {
                ExceptionHandler.logError(LOG_TAG, "解析移动坐标失败: " + move + " - 错误: " + e.getMessage());
            }
        } else {
            ExceptionHandler.logError(LOG_TAG, "走法字符串太短: '" + move + "'");
        }
        
        return null;
    }
    
    /**
     * 解析分析响应
     */
    private GoAnalysis parseAnalysisResponse(String response) {
        GoAnalysis analysis = new GoAnalysis();
        
        try {
            // 使用正则表达式解析分析数据
            Pattern winratePattern = Pattern.compile("winrate\\s+([\\d.]+)");
            Pattern visitsPattern = Pattern.compile("visits\\s+(\\d+)");
            Pattern pvPattern = Pattern.compile("pv\\s+([A-Z]\\d+(?:\\s+[A-Z]\\d+)*)");
            
            Matcher winrateMatcher = winratePattern.matcher(response);
            if (winrateMatcher.find()) {
                analysis.winRate = Double.parseDouble(winrateMatcher.group(1));
            }
            
            Matcher visitsMatcher = visitsPattern.matcher(response);
            if (visitsMatcher.find()) {
                analysis.visits = Integer.parseInt(visitsMatcher.group(1));
            }
            
            Matcher pvMatcher = pvPattern.matcher(response);
            if (pvMatcher.find()) {
                String[] moves = pvMatcher.group(1).split("\\s+");
                analysis.principalVariation = Arrays.asList(moves);
            }
            
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "解析分析响应失败: " + e.getMessage());
        }
        
        return analysis;
    }
    
    /**
     * 转换到GTP坐标格式
     */
    private String convertToGTPPosition(int row, int col) {
        // GTP格式: A1, B1, ..., T19 (跳过I)
        char colChar = (char) ('A' + col + (col >= 8 ? 1 : 0)); // 跳过I列
        int rowNum = GoGame.BOARD_SIZE - row;
        return colChar + String.valueOf(rowNum);
    }
    
    /**
     * 转换到数字坐标格式用于显示
     */
    private String convertToNumericPosition(int row, int col) {
        // 数字格式: (行,列) = (纵坐标,横坐标)
        int displayRow = GoGame.BOARD_SIZE - row;  // 19-1 (从上到下)
        int displayCol = col + 1;                 // 1-19 (从左到右)
        return "(" + displayRow + "," + displayCol + ")";
    }
    /**
     * 从 GTP坐标格式转换
     */
    private GoPosition convertFromGTPPosition(char col, int row) {
        // 转换列 (A=0, B=1, ..., H=7, J=8, ..., T=18)
        int colIndex = col - 'A';
        if (col >= 'J') {
            colIndex--; // 跳过I列
        }
        
        // 转换行
        int rowIndex = GoGame.BOARD_SIZE - row;
        
        if (rowIndex >= 0 && rowIndex < GoGame.BOARD_SIZE && 
            colIndex >= 0 && colIndex < GoGame.BOARD_SIZE) {
            return new GoPosition(rowIndex, colIndex);
        }
        
        return null;
    }
    
    /**
     * 强制生成一个有效的移动（当KataGo选择弃权时使用）
     */
    private GoPosition generateForcedMove(int[][] board, int currentPlayer) {
        ExceptionHandler.logInfo(LOG_TAG, "开始强制生成移动...");
        
        Random random = new Random();
        List<GoPosition> validMoves = new ArrayList<>();
        
        // 找到所有合法的移动
        for (int row = 0; row < GoGame.BOARD_SIZE; row++) {
            for (int col = 0; col < GoGame.BOARD_SIZE; col++) {
                if (board[row][col] == GoGame.EMPTY) {
                    // 简单检查是否是合法移动（非自杀）
                    if (isValidMoveSimple(board, row, col, currentPlayer)) {
                        validMoves.add(new GoPosition(row, col));
                    }
                }
            }
        }
        
        if (validMoves.isEmpty()) {
            ExceptionHandler.logError(LOG_TAG, "无法找到任何合法的移动！");
            return null;
        }
        
        // 优先选择中心区域的移动
        List<GoPosition> centerMoves = new ArrayList<>();
        int center = GoGame.BOARD_SIZE / 2;
        int radius = Math.min(center - 2, 5); // 中心区域半径
        
        for (GoPosition move : validMoves) {
            int distanceFromCenter = Math.abs(move.row - center) + Math.abs(move.col - center);
            if (distanceFromCenter <= radius) {
                centerMoves.add(move);
            }
        }
        
        // 如果中心区域有合法移动，优先选择
        List<GoPosition> candidateMoves = centerMoves.isEmpty() ? validMoves : centerMoves;
        GoPosition selectedMove = candidateMoves.get(random.nextInt(candidateMoves.size()));
        
        ExceptionHandler.logInfo(LOG_TAG, "强制生成移动: (" + selectedMove.row + ", " + selectedMove.col + ") 总候选: " + validMoves.size());
        return selectedMove;
    }
    
    /**
     * 简单检查移动是否合法（不考虑复杂的围棋规则）
     */
    private boolean isValidMoveSimple(int[][] board, int row, int col, int currentPlayer) {
        // 检查位置是否为空
        if (board[row][col] != GoGame.EMPTY) {
            return false;
        }
        
        // 简单的非自杀检查：如果周围有空位或同色棋子，则认为合法
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        
        for (int i = 0; i < 4; i++) {
            int newRow = row + dr[i];
            int newCol = col + dc[i];
            
            if (newRow >= 0 && newRow < GoGame.BOARD_SIZE && newCol >= 0 && newCol < GoGame.BOARD_SIZE) {
                int neighbor = board[newRow][newCol];
                // 如果相邻位置有空位或同色棋子，则不是自杀
                if (neighbor == GoGame.EMPTY || neighbor == currentPlayer) {
                    return true;
                }
            }
        }
        
        // 如果周围都是敌方棋子，可能是自杀，但为了简化仍然允许
        return true;
    }
    
    /**
     * 设置难度等级
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(10, difficulty));
        this.visits = config.getGoAIVisits(this.difficulty);
        
        ExceptionHandler.logInfo(LOG_TAG, "🎯 难度设置为: " + difficulty + " (访问数: " + visits + ")");
        
        if (engineInitialized) {
            try {
                sendCommand("kata-set-param maxVisits " + visits);
            } catch (Exception e) {
                ExceptionHandler.logError(LOG_TAG, "设置访问数失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取引擎信息
     */
    public String getEngineInfo() {
        if (!engineInitialized) {
            return "KataGo引擎未初始化";
        }
        
        try {
            String name = sendCommand("name");
            String version = sendCommand("version");
            
            return String.format("引擎: %s\n版本: %s\n难度: %d\n访问数: %d\n平均思考时间: %.2fs", 
                name.replaceFirst("^=\\s*", ""),
                version.replaceFirst("^=\\s*", ""),
                difficulty, 
                visits,
                totalMoves > 0 ? (totalThinkTime / 1000.0 / totalMoves) : 0);
                
        } catch (Exception e) {
            return "无法获取引擎信息: " + e.getMessage();
        }
    }
    
    /**
     * 获取最后一次分析结果
     */
    public GoAnalysis getLastAnalysis() {
        return lastAnalysis;
    }
    
    /**
     * 检查引擎是否在思考
     */
    public boolean isThinking() {
        return isThinking;
    }
    
    /**
     * 获取移动历史
     */
    public List<String> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
    
    /**
     * 清空移动历史
     */
    public void clearMoveHistory() {
        moveHistory.clear();
    }
    
    /**
     * 关闭引擎
     */
    public void shutdownEngine() {
        if (isShutdown) {
            return;
        }
        
        isShutdown = true;
        
        try {
            if (katagoWriter != null) {
                katagoWriter.write("quit\n");
                katagoWriter.flush();
                katagoWriter.close();
            }
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "关闭写入流失败: " + e.getMessage());
        }
        
        try {
            if (katagoReader != null) {
                katagoReader.close();
            }
        } catch (Exception e) {
            ExceptionHandler.logError(LOG_TAG, "关闭读取流失败: " + e.getMessage());
        }
        
        if (katagoProcess != null) {
            try {
                // 给引擎一些时间正常退出
                if (!katagoProcess.waitFor(3, TimeUnit.SECONDS)) {
                    katagoProcess.destroyForcibly();
                }
                ExceptionHandler.logInfo(LOG_TAG, "🔄 KataGo引擎已关闭");
            } catch (Exception e) {
                katagoProcess.destroyForcibly();
                ExceptionHandler.logError(LOG_TAG, "强制关闭引擎: " + e.getMessage());
            }
        }
        
        engineInitialized = false;
    }
    
    /**
     * 围棋分析结果类
     */
    public static class GoAnalysis {
        public double winRate = 0.0;
        public int visits = 0;
        public List<String> principalVariation = new ArrayList<>();
        public double score = 0.0;
        public Map<String, Double> candidateMoves = new HashMap<>();
        
        @Override
        public String toString() {
            return String.format("胜率: %.1f%%, 访问: %d, 主要变化: %s", 
                winRate * 100, visits, 
                principalVariation.isEmpty() ? "无" : String.join(" ", principalVariation));
        }
    }
}
