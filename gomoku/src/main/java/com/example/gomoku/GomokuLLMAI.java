package com.example.gomoku.ui;

import com.example.gomoku.core.GomokuBoard;

/**
 * 五子棋大模型AI类
 * 使用大模型进行思考和决策
 */
public class GomokuLLMAI extends GomokuAI {

    private String modelName;
    private String thinking;
    
    /**
     * 构造函数
     */
    public GomokuLLMAI(String difficulty, String modelName) {
        super(difficulty);
        this.modelName = modelName;
    }
    
    /**
     * 获取AI的下一步走法
     */
    @Override
    public int[] getNextMove(GomokuBoard board) {
        // 生成棋盘状态描述
        String boardState = generateBoardStateDescription(board);
        
        // 生成提示词
        String prompt = generatePrompt(board, boardState);
        
        // 模拟大模型思考过程
        simulateThinking(board, prompt);
        
        // 使用基础AI逻辑获取走法
        return super.getNextMove(board);
    }
    
    /**
     * 生成棋盘状态描述
     */
    private String generateBoardStateDescription(GomokuBoard board) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前棋盘状态:\n");
        
        // 添加棋盘表示
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                char piece = board.getPiece(row, col);
                if (piece == GomokuBoard.BLACK) {
                    sb.append("X "); // 黑子
                } else if (piece == GomokuBoard.WHITE) {
                    sb.append("O "); // 白子
                } else {
                    sb.append(". "); // 空位
                }
            }
            sb.append("\n");
        }
        
        // 添加当前回合信息
        sb.append("\n当前回合: ").append(board.isBlackTurn() ? "黑方(X)" : "白方(O)").append("\n");
        
        return sb.toString();
    }
    
    /**
     * 生成提示词
     */
    private String generatePrompt(GomokuBoard board, String boardState) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("你是一个五子棋AI助手，请分析当前棋局并给出最佳的下一步走法。\n\n");
        sb.append(boardState);
        sb.append("\n请分析当前局势，考虑进攻和防守策略，然后给出你认为最佳的下一步走法。");
        sb.append("格式为：[行,列]，行列均从0开始计数。\n");
        sb.append("同时，请简要解释你选择这个走法的原因。\n");
        
        return sb.toString();
    }
    
    /**
     * 模拟大模型思考过程
     */
    private void simulateThinking(GomokuBoard board, String prompt) {
        // 在实际应用中，这里应该调用大模型API
        // 这里仅做模拟，生成一些思考过程文本
        
        StringBuilder sb = new StringBuilder();
        sb.append("分析当前棋局...\n\n");
        
        // 添加一些模拟的思考过程
        char aiPiece = board.isBlackTurn() ? GomokuBoard.BLACK : GomokuBoard.WHITE;
        char opponentPiece = aiPiece == GomokuBoard.BLACK ? GomokuBoard.WHITE : GomokuBoard.BLACK;
        
        // 寻找最佳走法
        int[] bestMove = super.getBestMove(board, super.getPossibleMoves(board));
        if (bestMove != null) {
            // 评估这个位置的价值
            int score = super.evaluateMove(board, bestMove[0], bestMove[1]);
            
            // 根据分数生成不同的思考过程
            if (score >= 10000) {
                sb.append("我发现了一个关键位置！在[" + bestMove[0] + "," + bestMove[1] + "]落子可以形成四连或者阻止对手的四连。\n");
            } else if (score >= 1000) {
                sb.append("在[" + bestMove[0] + "," + bestMove[1] + "]落子可以形成活三，为未来的胜利创造条件。\n");
            } else if (score >= 100) {
                sb.append("我选择在[" + bestMove[0] + "," + bestMove[1] + "]落子，这样可以形成潜在的威胁，同时防止对手在此处形成优势。\n");
            } else {
                sb.append("当前局势比较平衡，我选择在[" + bestMove[0] + "," + bestMove[1] + "]落子，这是一个相对均衡的选择。\n");
            }
            
            sb.append("\n我的决策: [" + bestMove[0] + "," + bestMove[1] + "]\n");
        } else {
            sb.append("当前局势不明朗，我将选择一个合理的位置落子。\n");
        }
        
        this.thinking = sb.toString();
    }
    
    /**
     * 获取AI的思考过程
     */
    @Override
    public String getThinking() {
        return thinking;
    }
}