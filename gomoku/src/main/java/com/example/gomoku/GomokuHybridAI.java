package com.example.gomoku.ui;

import com.example.gomoku.core.GomokuBoard;

/**
 * 五子棋混合AI类
 * 结合传统AI和大模型AI的优势
 */
public class GomokuHybridAI extends GomokuLLMAI {

    /**
     * 构造函数
     */
    public GomokuHybridAI(String difficulty, String modelName) {
        super(difficulty, modelName);
    }
    
    /**
     * 获取AI的下一步走法
     */
    @Override
    public int[] getNextMove(GomokuBoard board) {
        // 使用传统AI评估所有可能的走法
        int[] traditionalMove = super.getBestMove(board, super.getPossibleMoves(board));
        
        // 使用大模型AI进行思考
        int[] llmMove = super.getNextMove(board);
        
        // 在关键局势下，优先使用传统AI的走法
        if (traditionalMove != null && evaluateMove(board, traditionalMove[0], traditionalMove[1]) >= 1000) {
            return traditionalMove;
        }
        
        // 否则使用大模型AI的走法
        return llmMove;
    }
}