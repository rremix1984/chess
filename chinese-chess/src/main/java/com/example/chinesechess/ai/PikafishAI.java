package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;
import com.example.chinesechess.ui.AILogPanel;
import java.util.List;

/**
 * 纯 Pikafish AI 实现
 * 直接使用 Pikafish 引擎进行象棋计算
 */
public class PikafishAI {
    private final PieceColor aiColor;
    private final int difficulty;
    private PikafishEngine engine;
    private AILogPanel aiLogPanel;
    private FenConverter fenConverter;
    
    // 根据难度调整思考时间
    private static final int[] THINK_TIME_BY_DIFFICULTY = {
        300,   // 1 - 简单: 300ms
        500,   // 2 - 普通: 500ms
        1000,  // 3 - 困难: 1000ms
        1500,  // 4 - 专家: 1500ms
        2000,  // 5 - 大师: 2000ms
        3000,  // 6 - 特级: 3000ms
        4000,  // 7 - 超级: 4000ms
        5000,  // 8 - 顶级: 5000ms
        7000,  // 9 - 传奇: 7000ms
        10000  // 10 - 神级: 10000ms
    };

    public PikafishAI(PieceColor aiColor, int difficulty) {
        this.aiColor = aiColor;
        this.difficulty = Math.max(1, Math.min(10, difficulty)); // 限制在1-10范围内
        this.fenConverter = new FenConverter();
        
        initializeEngine();
    }

    private void initializeEngine() {
        try {
            // 检查系统中是否有 pikafish 命令，或者使用预编译的引擎
            String enginePath = findPikafishEngine();
            if (enginePath == null) {
                logInfo("⚠️ 未找到 Pikafish 引擎，请确保已安装 Pikafish");
                return;
            }

            engine = new PikafishEngine(enginePath);
            
            // 设置日志回调
            engine.setLogCallback(message -> {
                if (aiLogPanel != null) {
                    aiLogPanel.addThinkingLog("Pikafish", message);
                } else {
                    System.out.println("[Pikafish] " + message);
                }
            });

            boolean initialized = engine.initialize();
            if (initialized) {
                logInfo("✅ Pikafish 引擎初始化成功");
            } else {
                logError("❌ Pikafish 引擎初始化失败");
            }
        } catch (Exception e) {
            logError("初始化 Pikafish 引擎时发生异常: " + e.getMessage());
        }
    }

    private String findPikafishEngine() {
        // 首先尝试系统命令
        String[] possiblePaths = {
            "pikafish",  // 系统 PATH 中的命令
            "/usr/local/bin/pikafish",
            "/opt/homebrew/bin/pikafish",
            "./pikafish",
            "../pikafish",
            System.getProperty("user.home") + "/pikafish/pikafish"
        };

        for (String path : possiblePaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(path);
                Process process = pb.start();
                process.destroy();
                logInfo("找到 Pikafish 引擎: " + path);
                return path;
            } catch (Exception e) {
                // 继续尝试下一个路径
            }
        }
        
        return null;
    }

    /**
     * 获取最佳走法
     */
    public Move getBestMove(Board board) {
        if (engine == null || !engine.isAvailable()) {
            logError("Pikafish 引擎不可用");
            return null;
        }

        try {
            // 转换棋盘为 FEN 格式
            String fen = fenConverter.boardToFen(board);
            logInfo("分析局面 FEN: " + fen);

            // 根据难度获取思考时间
            int thinkTime = getThinkTimeForDifficulty();
            logInfo("思考时间: " + thinkTime + "ms (难度: " + difficulty + ")");

            // 获取最佳走法
            String bestMoveUci = engine.getBestMove(fen, thinkTime);
            
            if (bestMoveUci != null && !bestMoveUci.isEmpty()) {
                logInfo("Pikafish 推荐走法 (UCI): " + bestMoveUci);
                
                // 将 UCI 格式转换为 Move 对象
                Move move = parseUciMove(bestMoveUci, board);
                if (move != null) {
                    String moveDescription = formatMoveDescription(move, board);
                    logInfo("转换后的走法: " + moveDescription);
                    return move;
                } else {
                    logError("无法解析走法: " + bestMoveUci);
                }
            } else {
                logWarning("Pikafish 未返回有效走法");
            }
        } catch (Exception e) {
            logError("获取最佳走法时发生异常: " + e.getMessage());
        }

        return null;
    }

    private int getThinkTimeForDifficulty() {
        int index = difficulty - 1; // 转换为数组索引
        if (index >= 0 && index < THINK_TIME_BY_DIFFICULTY.length) {
            return THINK_TIME_BY_DIFFICULTY[index];
        }
        return 1000; // 默认 1 秒
    }

    /**
     * 解析 UCI 格式的走法为 Move 对象
     */
    private Move parseUciMove(String uciMove, Board board) {
        if (uciMove == null || uciMove.length() != 4) {
            return null;
        }

        try {
            // UCI 格式: e2e4 表示从 e2 到 e4
            char fromFile = uciMove.charAt(0);
            char fromRank = uciMove.charAt(1);
            char toFile = uciMove.charAt(2);
            char toRank = uciMove.charAt(3);

            // 转换为棋盘坐标
            Position from = uciToPosition(fromFile, fromRank);
            Position to = uciToPosition(toFile, toRank);

            if (from != null && to != null) {
                return new Move(from, to);
            }
        } catch (Exception e) {
            logError("解析 UCI 走法失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 将 UCI 坐标转换为棋盘位置
     */
    private Position uciToPosition(char file, char rank) {
        // 文件 (列): a-i 对应 0-8
        int col = file - 'a';
        // 等级 (行): 0-9 对应 9-0 (UCI 中 0 是棋盘底部)
        int row = 9 - (rank - '0');

        if (col >= 0 && col <= 8 && row >= 0 && row <= 9) {
            return new Position(row, col);
        }

        return null;
    }

    /**
     * 格式化走法描述
     */
    private String formatMoveDescription(Move move, Board board) {
        Position from = move.getStart();
        Position to = move.getEnd();
        
        Piece piece = board.getPiece(from.getX(), from.getY());
        String pieceName = (piece != null) ? piece.getChineseName() : "未知棋子";
        
        return String.format("%s 从 (%d,%d) 到 (%d,%d)", 
            pieceName, from.getX(), from.getY(), to.getX(), to.getY());
    }

    /**
     * 设置 AI 日志面板
     */
    public void setAILogPanel(AILogPanel panel) {
        this.aiLogPanel = panel;
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        if (engine != null) {
            engine.quit();
            engine = null;
        }
    }

    /**
     * 获取引擎状态
     */
    public boolean isAvailable() {
        return engine != null && engine.isAvailable();
    }

    /**
     * 获取引擎信息
     */
    public String getEngineInfo() {
        if (engine != null) {
            return engine.getEngineInfo();
        }
        return "Pikafish 引擎未初始化";
    }

    // 日志方法
    private void logInfo(String message) {
        String logMessage = "🐟 " + message;
        if (aiLogPanel != null) {
            aiLogPanel.addAnalysis(logMessage);
        }
        System.out.println("[PikafishAI] " + logMessage);
    }

    private void logWarning(String message) {
        String logMessage = "⚠️ " + message;
        if (aiLogPanel != null) {
            aiLogPanel.addAnalysis(logMessage);
        }
        System.out.println("[PikafishAI] " + logMessage);
    }

    private void logError(String message) {
        String logMessage = "❌ " + message;
        if (aiLogPanel != null) {
            aiLogPanel.addAnalysis(logMessage);
        }
        System.err.println("[PikafishAI] " + logMessage);
    }

    public PieceColor getAiColor() {
        return aiColor;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
