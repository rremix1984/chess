package com.example.internationalchess.ai;

import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.core.Move;
import com.example.internationalchess.core.Position;
import com.example.internationalchess.ui.StockfishLogPanel;

/**
 * Stockfish AI适配器
 * 将StockfishAI包装成与现有系统兼容的接口
 */
public class StockfishAIAdapter {
    
    private StockfishAI stockfishAI;
    private int difficulty;
    private char aiColor;
    private StockfishLogPanel logPanel;
    
    public StockfishAIAdapter(int difficulty, char aiColor) {
        this.difficulty = difficulty;
        this.aiColor = aiColor;
        
        // 转换难度级别到文字描述
        String difficultyStr = getDifficultyString(difficulty);
        PieceColor pieceColor = aiColor == 'W' ? PieceColor.WHITE : PieceColor.BLACK;
        
        this.stockfishAI = new StockfishAI(difficultyStr, pieceColor);
    }
    
    public StockfishAIAdapter(int difficulty, char aiColor, StockfishLogPanel logPanel) {
        this.difficulty = difficulty;
        this.aiColor = aiColor;
        this.logPanel = logPanel;
        
        // 转换难度级别到文字描述
        String difficultyStr = getDifficultyString(difficulty);
        PieceColor pieceColor = aiColor == 'W' ? PieceColor.WHITE : PieceColor.BLACK;
        
        this.stockfishAI = new StockfishAI(difficultyStr, pieceColor, logPanel);
    }
    
    /**
     * 将数字难度转换为文字描述
     */
    private String getDifficultyString(int difficulty) {
        switch (difficulty) {
            case 1:
                return "简单";
            case 2:
                return "中等";
            case 3:
                return "困难";
            default:
                return "中等";
        }
    }
    
    /**
     * 计算AI的下一步移动
     * 返回格式: [fromRow, fromCol, toRow, toCol]
     */
    public int[] calculateNextMove(InternationalChessBoard board) {
        if (stockfishAI == null || !stockfishAI.isReady()) {
            System.err.println("❌ Stockfish AI未就绪");
            return null;
        }
        
        // 确定当前玩家
        PieceColor currentPlayer = board.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK;
        
        try {
            Move move = stockfishAI.calculateMove(board, currentPlayer);
            if (move != null) {
                Position from = move.getFrom();
                Position to = move.getTo();
                
                return new int[]{
                    from.getY(),  // fromRow
                    from.getX(),  // fromCol  
                    to.getY(),    // toRow
                    to.getX()     // toCol
                };
            }
        } catch (Exception e) {
            System.err.println("❌ Stockfish计算移动时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 获取AI信息
     */
    public String getAIInfo() {
        if (stockfishAI != null) {
            return stockfishAI.getEngineInfo();
        }
        return "Stockfish AI (未初始化)";
    }
    
    /**
     * 检查AI是否就绪
     */
    public boolean isReady() {
        return stockfishAI != null && stockfishAI.isReady();
    }
    
    /**
     * 设置难度
     */
    public void setDifficulty(int newDifficulty) {
        if (this.difficulty != newDifficulty) {
            this.difficulty = newDifficulty;
            String difficultyStr = getDifficultyString(newDifficulty);
            if (stockfishAI != null) {
                stockfishAI.setDifficulty(difficultyStr);
            }
        }
    }
    
    /**
     * 关闭AI引擎
     */
    public void shutdown() {
        if (stockfishAI != null) {
            stockfishAI.shutdown();
            stockfishAI = null;
        }
    }
    
    // 注意: finalize() 方法已被移除，因为它可能导致引擎过早关闭
    // 如果需要清理资源，请显式调用 shutdown() 方法
}
