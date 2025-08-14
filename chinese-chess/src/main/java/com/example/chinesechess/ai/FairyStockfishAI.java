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
            
            // 添加棋盘状态调试信息
            System.out.println("🔍 [调试] 棋盘状态检查:");
            for (int row = 5; row <= 7; row++) {
                for (int col = 0; col < 9; col++) {
                    Piece piece = board.getPiece(row, col);
                    if (piece != null) {
                        System.out.println("  位置(" + row + "," + col + "): " + piece.getClass().getSimpleName() + " " + piece.getColor());
                    }
                }
            }
            addToAILog("AI难度: " + difficulty + "/10 (" + getDifficultyName() + ")");
            
            // 按照新的难度算法计算思考时间，大幅提高高难度级别的分析质量
            int thinkTime = calculateEnhancedThinkTime(difficulty);
            addToAILog("优化后的思考时间: " + thinkTime + "ms （难度级别: " + difficulty + "）");
            
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
                addToAILog("UCI走法无效: " + uciMove);
                return null;
            }
            
            System.out.println("🔍 [调试] 转换UCI走法: " + uciMove);
            addToAILog("转换UCI走法: " + uciMove);
            
            // 使用FenConverter的UCI转换方法
            Position[] positions = FenConverter.uciToMove(uciMove);
            if (positions == null || positions.length != 2) {
                System.out.println("⚠️ UCI走法格式错误: " + uciMove);
                addToAILog("UCI走法格式错误: " + uciMove);
                return null;
            }
            
            Position start = positions[0];
            Position end = positions[1];
            
            System.out.println("🔍 [调试] UCI转换结果: " + uciMove + " -> " + 
                "(起点: " + start.getX() + "," + start.getY() + ") " + 
                "(终点: " + end.getX() + "," + end.getY() + ")");
            addToAILog("UCI转换: " + uciMove + " -> (" + start.getX() + "," + start.getY() + ") to (" + end.getX() + "," + end.getY() + ")");
            
            // 验证起始位置有棋子且属于当前AI
            Piece piece = board.getPiece(start.getX(), start.getY());
            if (piece == null) {
                System.out.println("⚠️ 起始位置无棋子: " + uciMove + " (位置: " + start.getX() + "," + start.getY() + ")");
                addToAILog("起始位置无棋子: " + uciMove);
                return null;
            }
            
            if (piece.getColor() != aiColor) {
                System.out.println("⚠️ 棋子颜色不匹配: " + uciMove + " (期望: " + aiColor + ", 实际: " + piece.getColor() + ")");
                addToAILog("棋子颜色不匹配: " + uciMove);
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
     * 计算专业竞赛级思考时间，充分利用新的引擎优化配置
     * 基于引擎的极致优化，提供前所未有的AI棋力体验
     * @param difficulty 难度级别 (1-10)
     * @return 思考时间（毫秒）
     */
    private int calculateEnhancedThinkTime(int difficulty) {
        // 新的专业级思考时间映射，配合1GB Hash、多线程、30层深度搜索
        switch (difficulty) {
            case 1:  return 5000;     // 简单: 5秒 (基础提升)
            case 2:  return 10000;    // 普通: 10秒 (明显增强)
            case 3:  return 18000;    // 困难: 18秒 (深度搜索)
            case 4:  return 30000;    // 专家: 30秒 (竞赛水平)
            case 5:  return 45000;    // 大师: 45秒 (职业级别)
            case 6:  return 75000;    // 特级: 75秒 (顶尖水平)
            case 7:  return 120000;   // 超级: 2分钟 (大师级深度)
            case 8:  return 180000;   // 顶级: 3分钟 (超级深度分析)
            case 9:  return 300000;   // 传奇: 5分钟 (极致分析)
            case 10: return 600000;   // 神级: 10分钟 (终极棋力，无人能敌)
            default: return 18000;    // 默认提升到困难级别
        }
    }
    
    /**
     * 获取难度等级名称
     */
    private String getDifficultyName() {
        String[] difficultyNames = {
            "简单", "普通", "困难", "专家", "大师",
            "特级", "超级", "顶级", "传奇", "神级"
        };
        if (difficulty >= 1 && difficulty <= difficultyNames.length) {
            return difficultyNames[difficulty - 1];
        }
        return "未知";
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
