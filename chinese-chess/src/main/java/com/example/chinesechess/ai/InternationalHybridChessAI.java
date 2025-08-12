package com.example.chinesechess.ai;

import com.example.chinesechess.core.InternationalChessBoard;
import com.example.chinesechess.ui.ChatPanel;

import javax.swing.SwingWorker;
import java.util.concurrent.CompletableFuture;

/**
 * 国际象棋混合AI类
 * 结合传统AI和大模型AI的优势
 */
public class InternationalHybridChessAI {

    private InternationalChessAI traditionalAI;
    private InternationalLLMChessAI llmAI;
    private int difficulty;
    private char aiColor;
    private ChatPanel chatPanel;
    
    /**
     * 构造函数
     * @param difficulty AI难度级别（1-3）
     * @param aiColor AI执子颜色
     * @param model 大模型名称
     * @param chatPanel 聊天面板
     */
    public InternationalHybridChessAI(int difficulty, char aiColor, String model, ChatPanel chatPanel) {
        this.difficulty = difficulty;
        this.aiColor = aiColor;
        this.chatPanel = chatPanel;
        
        // 初始化传统AI和大模型AI
        traditionalAI = new InternationalChessAI(difficulty, aiColor);
        llmAI = new InternationalLLMChessAI(model, aiColor, chatPanel);
    }
    
    /**
     * 异步计算AI的下一步移动
     * @param board 当前棋盘状态
     * @return 包含移动数组的CompletableFuture [fromRow, fromCol, toRow, toCol]
     */
    public CompletableFuture<int[]> calculateNextMoveAsync(InternationalChessBoard board) {
        // 如果不是AI的回合，返回null
        boolean isWhiteTurn = board.isWhiteTurn();
        if ((isWhiteTurn && aiColor != InternationalChessBoard.WHITE) || 
            (!isWhiteTurn && aiColor != InternationalChessBoard.BLACK)) {
            return CompletableFuture.completedFuture(null);
        }
        
        // 创建一个新的CompletableFuture
        CompletableFuture<int[]> future = new CompletableFuture<>();
        
        // 先使用传统AI计算移动
        int[] traditionalMove = traditionalAI.calculateNextMove(board);
        
        // 使用大模型AI进行决策
        chatPanel.addChatMessage("系统", "混合AI正在思考...");
        
        // 生成棋盘状态描述
        String boardDescription = generateBoardDescription(board);
        
        // 生成提示信息，包含传统AI的建议
        String prompt = generatePromptWithTraditionalAISuggestion(board, boardDescription, traditionalMove);
        
        // 发送提示到大模型并处理响应
        // 使用SwingWorker替代sendToLLM方法
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // 这里应该调用获取AI响应的方法，暂时使用模拟响应
                return "模拟的AI响应";
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    try {
                        // 解析大模型的响应
                        int[] move = llmAI.parseResponse(response, board);
                        
                        if (move != null) {
                            // 向聊天面板发送移动信息
                            String moveDescription = describeMoveInChineseChess(move, board);
                            chatPanel.addChatMessage("混合AI", moveDescription);
                            
                            // 完成Future
                            future.complete(move);
                        } else {
                            // 如果解析失败，使用传统AI的移动
                            chatPanel.addChatMessage("系统", "大模型响应解析失败，使用传统AI的建议");
                            String traditionalMoveDescription = describeMoveInChineseChess(traditionalMove, board);
                            chatPanel.addChatMessage("混合AI", "使用传统AI的建议: " + traditionalMoveDescription);
                            future.complete(traditionalMove);
                        }
                    } catch (Exception e) {
                        chatPanel.addChatMessage("系统", "处理大模型响应时出错: " + e.getMessage());
                        future.complete(traditionalMove); // 出错时使用传统AI的移动
                    }
                } catch (Exception e) {
                    chatPanel.addChatMessage("系统", "获取AI响应时出错: " + e.getMessage());
                    future.complete(traditionalMove); // 出错时使用传统AI的移动
                }
            }
        };
        
        worker.execute();
        
        return future;
    }
    
    /**
     * 生成棋盘状态描述
     */
    private String generateBoardDescription(InternationalChessBoard board) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前棋盘状态:\n");
        
        // 添加列标签 (a-h)
        sb.append("  ");
        for (char c = 'a'; c < 'a' + InternationalChessBoard.BOARD_SIZE; c++) {
            sb.append(c).append(" ");
        }
        sb.append("\n");
        
        // 添加棋盘内容和行标签 (8-1)
        for (int row = 0; row < InternationalChessBoard.BOARD_SIZE; row++) {
            sb.append(8 - row).append(" ");
            for (int col = 0; col < InternationalChessBoard.BOARD_SIZE; col++) {
                String piece = board.getPiece(row, col);
                if (piece == null) {
                    // 使用不同的符号表示黑白格子
                    sb.append((row + col) % 2 == 0 ? "□ " : "■ ");
                } else {
                    sb.append(getPieceSymbol(piece)).append(" ");
                }
            }
            sb.append("\n");
        }
        
        // 添加当前回合信息
        sb.append("当前回合: ").append(board.isWhiteTurn() ? "白方" : "黑方").append("\n");
        
        return sb.toString();
    }
    
    /**
     * 获取棋子的Unicode符号
     */
    private String getPieceSymbol(String piece) {
        char color = piece.charAt(0);
        char type = piece.charAt(1);
        
        // 使用Unicode国际象棋符号
        switch (type) {
            case InternationalChessBoard.KING:
                return color == InternationalChessBoard.WHITE ? "♔" : "♚";
            case InternationalChessBoard.QUEEN:
                return color == InternationalChessBoard.WHITE ? "♕" : "♛";
            case InternationalChessBoard.ROOK:
                return color == InternationalChessBoard.WHITE ? "♖" : "♜";
            case InternationalChessBoard.BISHOP:
                return color == InternationalChessBoard.WHITE ? "♗" : "♝";
            case InternationalChessBoard.KNIGHT:
                return color == InternationalChessBoard.WHITE ? "♘" : "♞";
            case InternationalChessBoard.PAWN:
                return color == InternationalChessBoard.WHITE ? "♙" : "♟";
            default:
                return "?";
        }
    }
    
    /**
     * 生成包含传统AI建议的提示信息
     */
    private String generatePromptWithTraditionalAISuggestion(InternationalChessBoard board, 
                                                           String boardDescription, 
                                                           int[] traditionalMove) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("你是一个国际象棋混合AI助手，结合了传统AI和大语言模型的优势。请帮我下一步棋。\n\n");
        sb.append(boardDescription).append("\n");
        
        // 添加角色信息
        sb.append("你执").append(aiColor == InternationalChessBoard.WHITE ? "白" : "黑").append("子。\n");
        
        // 添加传统AI的建议
        if (traditionalMove != null) {
            char fromCol = (char) ('a' + traditionalMove[1]);
            char fromRow = (char) ('8' - traditionalMove[0]);
            char toCol = (char) ('a' + traditionalMove[3]);
            char toRow = (char) ('8' - traditionalMove[2]);
            
            sb.append("传统AI建议的移动是: ").append(fromCol).append(fromRow)
              .append("-").append(toCol).append(toRow).append("\n");
        }
        
        // 添加指令
        sb.append("请分析当前局面，考虑传统AI的建议，并给出你认为最佳的下一步移动。\n");
        sb.append("你可以接受传统AI的建议，也可以提出自己的移动方案。\n");
        sb.append("请使用国际象棋代数记号（例如：e2-e4）表示你的移动，并解释你的思考过程。\n");
        sb.append("确保你的移动是合法的，并考虑到国际象棋的所有规则。\n");
        
        return sb.toString();
    }
    
    /**
     * 描述国际象棋的移动
     */
    private String describeMoveInChineseChess(int[] move, InternationalChessBoard board) {
        int fromRow = move[0];
        int fromCol = move[1];
        int toRow = move[2];
        int toCol = move[3];
        
        String piece = board.getPiece(fromRow, fromCol);
        if (piece == null) {
            return "移动无效";
        }
        
        char pieceType = piece.charAt(1);
        String pieceName = getPieceName(pieceType);
        
        // 使用国际象棋代数记号
        char fromColChar = (char) ('a' + fromCol);
        char fromRowChar = (char) ('8' - fromRow);
        char toColChar = (char) ('a' + toCol);
        char toRowChar = (char) ('8' - toRow);
        
        String captureText = "";
        String targetPiece = board.getPiece(toRow, toCol);
        if (targetPiece != null) {
            captureText = "吃掉" + getPieceName(targetPiece.charAt(1));
        }
        
        return String.format("我移动%s，从%c%c到%c%c%s", 
                pieceName, fromColChar, fromRowChar, toColChar, toRowChar, captureText);
    }
    
    /**
     * 获取棋子的中文名称
     */
    private String getPieceName(char pieceType) {
        switch (pieceType) {
            case InternationalChessBoard.KING:
                return "王";
            case InternationalChessBoard.QUEEN:
                return "后";
            case InternationalChessBoard.ROOK:
                return "车";
            case InternationalChessBoard.BISHOP:
                return "象";
            case InternationalChessBoard.KNIGHT:
                return "马";
            case InternationalChessBoard.PAWN:
                return "兵";
            default:
                return "未知棋子";
        }
    }
    
    /**
     * 设置难度
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
        if (traditionalAI != null) {
            traditionalAI = new InternationalChessAI(difficulty, aiColor);
        }
    }
    
    /**
     * 设置AI颜色
     */
    public void setAiColor(char aiColor) {
        this.aiColor = aiColor;
        if (traditionalAI != null) {
            traditionalAI = new InternationalChessAI(difficulty, aiColor);
        }
        if (llmAI != null) {
            llmAI.setAiColor(aiColor);
        }
    }
    
    /**
     * 设置大模型
     */
    public void setModel(String model) {
        if (llmAI != null) {
            llmAI.setModel(model);
        }
    }
}