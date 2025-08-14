package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;
import com.example.chinesechess.ui.AILogPanel;
import com.example.common.config.ConfigurationManager;

/**
 * Fairy-Stockfish AI 引擎
 * 基于 Fairy-Stockfish 多变体象棋引擎的 AI 实现
 */
public class FairyStockfishAI {
    
    private final PieceColor aiColor;
    private final int difficulty;
    private final FairyStockfishEngine fairyStockfishEngine;
    private final EnhancedChessAI fallbackAI; // 备用AI
    private AILogPanel aiLogPanel; // AI日志面板
    
    // 配置管理器
    private final ConfigurationManager config;
    
    // 配置参数
    private final int[] thinkTimes;
    
    /**
     * 构造函数
     */
    public FairyStockfishAI(PieceColor aiColor, int difficulty) {
        this.aiColor = aiColor;
        this.difficulty = Math.max(1, Math.min(10, difficulty)); // 支持1-10级难度
        
        // 初始化配置管理器
        this.config = ConfigurationManager.getInstance();
        this.thinkTimes = config.getAIThinkTimes();
        
        // 初始化 Fairy-Stockfish 引擎
        this.fairyStockfishEngine = new FairyStockfishEngine("fairy-stockfish");
        
        // 初始化备用AI
        this.fallbackAI = new EnhancedChessAI(aiColor, difficulty);
        
        System.out.println("🔧 Fairy-Stockfish AI 初始化:");
        System.out.println("   - AI颜色: " + (aiColor == PieceColor.RED ? "红方" : "黑方"));
        System.out.println("   - 难度: " + difficulty + "/10");
        System.out.println("   - 引擎路径: fairy-stockfish");
        
        // 尝试初始化引擎
        initializeFairyStockfishEngine();
    }
    
    /**
     * 设置AI日志面板
     */
    public void setAILogPanel(AILogPanel aiLogPanel) {
        this.aiLogPanel = aiLogPanel;
    }
    
    /**
     * 添加AI日志
     */
    private void addToAILog(String message) {
        if (aiLogPanel != null) {
            aiLogPanel.addSystemLog("[Fairy-Stockfish] " + message);
        }
    }
    
    /**
     * 初始化Fairy-Stockfish引擎
     */
    private void initializeFairyStockfishEngine() {
        // 设置日志回调，将引擎的决策过程输出到AI日志
        fairyStockfishEngine.setLogCallback(this::addToAILog);

        boolean initialized = fairyStockfishEngine.initialize();
        if (initialized) {
            System.out.println("🧚 Fairy-Stockfish引擎初始化成功");
            System.out.println(fairyStockfishEngine.getEngineInfo());
            addToAILog("Fairy-Stockfish引擎初始化成功");
        } else {
            System.out.println("⚠️ Fairy-Stockfish引擎初始化失败，将使用增强AI作为备用方案");
            addToAILog("Fairy-Stockfish引擎初始化失败，将使用增强AI作为备用方案");
            addToAILog("💡 提示：请确保已安装 fairy-stockfish 引擎");
        }
    }
    
    /**
     * 获取最佳移动
     */
    public Move getBestMove(Board board) {
        System.out.println("🧚 Fairy-Stockfish AI 思考中...");
        addToAILog("=== Fairy-Stockfish AI 开始思考 ===");
        
        try {
            // 转换为FEN格式
            String fen = FenConverter.boardToFen(board, aiColor);
            System.out.println("🔍 [调试] FEN: " + fen);
            addToAILog("分析局面: " + fen);
            
            int thinkTime = thinkTimes[difficulty - 1];
            
            // 优先尝试Fairy-Stockfish引擎
            if (fairyStockfishEngine != null && fairyStockfishEngine.isAvailable()) {
                System.out.println("🧚 使用Fairy-Stockfish引擎计算");
                addToAILog("使用Fairy-Stockfish引擎计算");
                
                long startTime = System.currentTimeMillis();
                String engineMove = fairyStockfishEngine.getBestMove(fen, thinkTime);
                long endTime = System.currentTimeMillis();
                
                System.out.println("🔍 [调试] Fairy-Stockfish引擎返回: " + engineMove);
                addToAILog("引擎思考时间: " + (endTime - startTime) + "ms");
                
                if (engineMove != null && !engineMove.equals("(none)")) {
                    // 转换UCI格式走法为Move对象
                    Move move = convertUciToMove(engineMove, board);
                    if (move != null) {
                        System.out.println("✅ Fairy-Stockfish引擎选择走法: " + engineMove);
                        addToAILog("选择走法: " + engineMove);
                        addToAILog("=== Fairy-Stockfish AI 思考完成 ===");
                        return move;
                    } else {
                        System.out.println("⚠️ UCI走法转换失败: " + engineMove);
                        addToAILog("走法转换失败: " + engineMove);
                    }
                } else {
                    System.out.println("⚠️ Fairy-Stockfish引擎未返回有效走法");
                    addToAILog("引擎未返回有效走法");
                }
            } else {
                System.out.println("⚠️ Fairy-Stockfish引擎不可用");
                addToAILog("Fairy-Stockfish引擎不可用");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Fairy-Stockfish AI计算失败: " + e.getMessage());
            addToAILog("计算失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 降级到备用AI
        System.out.println("🔄 降级使用增强AI");
        addToAILog("降级使用增强AI");
        addToAILog("=== 使用增强AI备用方案 ===");
        return fallbackAI.getBestMove(board);
    }
    
    /**
     * 将UCI格式的走法转换为Move对象
     */
    private Move convertUciToMove(String uciMove, Board board) {
        try {
            if (uciMove == null || uciMove.length() < 4) {
                return null;
            }
            
            // UCI格式: e2e4 (从e2到e4)
            // 转换为数组坐标
            char fromFile = uciMove.charAt(0);
            char fromRank = uciMove.charAt(1);
            char toFile = uciMove.charAt(2);
            char toRank = uciMove.charAt(3);
            
            // 将国际象棋坐标转换为中国象棋坐标
            // 文件: a-i (0-8)
            // 排: 1-10 (但在中国象棋FEN中是0-9)
            int fromCol = fromFile - 'a';
            int fromRow = Character.getNumericValue(fromRank);
            int toCol = toFile - 'a';
            int toRow = Character.getNumericValue(toRank);
            
            // 验证坐标范围
            if (fromCol < 0 || fromCol >= 9 || fromRow < 0 || fromRow >= 10 ||
                toCol < 0 || toCol >= 9 || toRow < 0 || toRow >= 10) {
                System.out.println("⚠️ 坐标超出范围: " + uciMove);
                return null;
            }
            
            Position start = new Position(fromRow, fromCol);
            Position end = new Position(toRow, toCol);
            
            // 验证起始位置有棋子且属于当前AI
            Piece piece = board.getPiece(fromRow, fromCol);
            if (piece == null) {
                System.out.println("⚠️ 起始位置无棋子: " + uciMove);
                return null;
            }
            
            if (piece.getColor() != aiColor) {
                System.out.println("⚠️ 棋子颜色不匹配: " + uciMove);
                return null;
            }
            
            // 验证走法是否合法
            if (!piece.isValidMove(board, start, end)) {
                System.out.println("⚠️ 走法不合法: " + uciMove);
                return null;
            }
            
            // 进一步验证移动安全性（不会导致己方将军被将军）
            if (!board.isMoveSafe(start, end, aiColor)) {
                System.out.println("⚠️ 走法不安全（会导致己方将军被将军）: " + uciMove);
                return null;
            }
            
            return new Move(start, end);
            
        } catch (Exception e) {
            System.err.println("❌ UCI走法转换异常: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查Fairy-Stockfish引擎是否可用
     */
    public boolean isFairyStockfishAvailable() {
        return fairyStockfishEngine != null && fairyStockfishEngine.isAvailable();
    }
    
    /**
     * 获取引擎信息
     */
    public String getEngineInfo() {
        if (isFairyStockfishAvailable()) {
            return "Fairy-Stockfish AI (" + fairyStockfishEngine.getEngineInfo() + ")";
        } else {
            return "Fairy-Stockfish AI (使用增强AI备用方案)";
        }
    }
    
    /**
     * 获取AI状态信息
     */
    public String getStatusInfo() {
        StringBuilder status = new StringBuilder();
        status.append("🧚 Fairy-Stockfish AI 状态:\n");
        status.append("- 颜色: ").append(aiColor == PieceColor.RED ? "红方" : "黑方").append("\n");
        status.append("- 难度: ").append(difficulty).append("/10\n");
        status.append("- 引擎: ").append(isFairyStockfishAvailable() ? "可用" : "不可用").append("\n");
        
        if (isFairyStockfishAvailable()) {
            status.append("- 引擎信息: ").append(fairyStockfishEngine.getEngineInfo()).append("\n");
        } else {
            status.append("- 备用方案: 增强AI\n");
        }
        
        return status.toString();
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (fairyStockfishEngine != null) {
            fairyStockfishEngine.cleanup();
        }
        System.out.println("🧚 Fairy-Stockfish AI 资源已清理");
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
