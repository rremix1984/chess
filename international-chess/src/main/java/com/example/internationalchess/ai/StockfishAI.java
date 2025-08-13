package com.example.internationalchess.ai;

import com.example.internationalchess.core.Move;
import com.example.internationalchess.core.Position;
import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.ui.StockfishLogPanel;

/**
 * 基于Stockfish的高级AI实现
 * 提供不同难度级别的国际象棋AI
 */
public class StockfishAI {
    
    private StockfishEngine engine;
    private String difficulty;
    private PieceColor aiColor;
    private StockfishLogPanel logPanel;
    
    public StockfishAI(String difficulty) {
        this.difficulty = difficulty;
        initializeEngine();
    }
    
    public StockfishAI(String difficulty, PieceColor aiColor) {
        this.difficulty = difficulty;
        this.aiColor = aiColor;
        initializeEngine();
    }
    
    public StockfishAI(String difficulty, PieceColor aiColor, StockfishLogPanel logPanel) {
        this.difficulty = difficulty;
        this.aiColor = aiColor;
        this.logPanel = logPanel;
        initializeEngine();
    }
    
    /**
     * 根据难度级别初始化Stockfish引擎
     */
    private void initializeEngine() {
        int skillLevel;
        int thinkingTime;
        
        switch (difficulty.toLowerCase()) {
            case "简单":
            case "easy":
                skillLevel = 5;   // 较低技能等级
                thinkingTime = 500; // 较短思考时间
                break;
            case "中等":
            case "medium":
                skillLevel = 12;  // 中等技能等级
                thinkingTime = 1500; // 中等思考时间
                break;
            case "困难":
            case "hard":
                skillLevel = 20;  // 最高技能等级
                thinkingTime = 3000; // 较长思考时间
                break;
            default:
                skillLevel = 12;  // 默认中等
                thinkingTime = 1500;
        }
        
        if (logPanel != null) {
            engine = new StockfishEngine(skillLevel, thinkingTime, logPanel);
        } else {
            engine = new StockfishEngine(skillLevel, thinkingTime);
        }
        System.out.println("🤖 Stockfish AI初始化完成 - 难度: " + difficulty + 
                          " (技能等级: " + skillLevel + "/20)");
    }
    
    /**
     * 计算AI的下一步移动
     */
    public Move calculateMove(InternationalChessBoard board, PieceColor currentPlayer) {
        if (engine == null || !engine.isReady()) {
            System.err.println("❌ Stockfish引擎未就绪");
            return null;
        }
        
        System.out.println("🤔 Stockfish正在思考...");
        long startTime = System.currentTimeMillis();
        
        Move bestMove = engine.getBestMove(board, currentPlayer);
        
        long thinkTime = System.currentTimeMillis() - startTime;
        
        if (bestMove != null) {
            System.out.println("✅ Stockfish找到最佳移动: " + moveToString(bestMove) + 
                             " (用时: " + thinkTime + "ms)");
        } else {
            System.err.println("❌ Stockfish未能找到有效移动");
        }
        
        return bestMove;
    }
    
    /**
     * 将移动转换为易读的字符串格式
     */
    private String moveToString(Move move) {
        if (move == null) return "null";
        
        Position from = move.getFrom();
        Position to = move.getTo();
        
        char fromFile = (char) ('a' + from.getX());
        int fromRank = from.getY() + 1;
        char toFile = (char) ('a' + to.getX());
        int toRank = to.getY() + 1;
        
        return "" + fromFile + fromRank + "-" + toFile + toRank;
    }
    
    /**
     * 设置AI颜色
     */
    public void setAIColor(PieceColor color) {
        this.aiColor = color;
    }
    
    /**
     * 获取AI颜色
     */
    public PieceColor getAIColor() {
        return aiColor;
    }
    
    /**
     * 获取难度级别
     */
    public String getDifficulty() {
        return difficulty;
    }
    
    /**
     * 更改难度级别
     */
    public void setDifficulty(String newDifficulty) {
        if (!newDifficulty.equals(this.difficulty)) {
            shutdown();
            this.difficulty = newDifficulty;
            initializeEngine();
        }
    }
    
    /**
     * 检查引擎是否就绪
     */
    public boolean isReady() {
        return engine != null && engine.isReady();
    }
    
    /**
     * 获取引擎信息
     */
    public String getEngineInfo() {
        if (engine != null) {
            return "Stockfish AI - " + difficulty + " (" + engine.getEngineInfo() + ")";
        }
        return "Stockfish AI - " + difficulty + " (引擎未初始化)";
    }
    
    /**
     * 关闭AI引擎
     */
    public void shutdown() {
        if (engine != null) {
            engine.shutdown();
            engine = null;
            System.out.println("🔌 Stockfish AI已关闭");
        }
    }
    
    // 注意: finalize() 方法已被移除，因为它可能导致引擎过早关闭
    // 如果需要清理资源，请显式调用 shutdown() 方法
}
