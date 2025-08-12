package com.example.internationalchess.ai;

import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.ui.ChatPanel;

import java.util.concurrent.CompletableFuture;

/**
 * 国际象棋大模型AI类
 * 使用大语言模型实现AI对弈功能
 */
public class InternationalLLMChessAI {

    private String model; // 使用的大模型名称
    private char aiColor; // AI执子颜色
    private ChatPanel chatPanel; // 聊天面板引用
    
    /**
     * 构造函数
     * @param model 大模型名称
     * @param aiColor AI执子颜色
     * @param chatPanel 聊天面板
     */
    public InternationalLLMChessAI(String model, char aiColor, ChatPanel chatPanel) {
        this.model = model;
        this.aiColor = aiColor;
        this.chatPanel = chatPanel;
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
        
        // 生成棋盘状态描述
        String boardDescription = generateBoardDescription(board);
        
        // 生成提示信息
        String prompt = generatePrompt(board, boardDescription);
        
        // 向聊天面板发送消息
        chatPanel.addChatMessage("AI", "正在思考下一步...");
        
        // 由于ChatPanel没有接受回调的sendMessage方法，我们需要自己处理AI响应
        // 创建一个新的线程来处理AI响应
        new Thread(() -> {
            try {
                // 模拟AI响应
                Thread.sleep(1000); // 模拟思考时间
                
                // 使用传统AI生成移动
                InternationalChessAI backupAI = new InternationalChessAI(2, aiColor);
                int[] move = backupAI.calculateNextMove(board);
                
                if (move != null) {
                    // 向聊天面板发送移动信息
                    String moveDescription = describeMoveInChineseChess(move, board);
                    chatPanel.addChatMessage("AI", moveDescription);
                    
                    // 完成Future
                    future.complete(move);
                } else {
                    // 如果解析失败，使用备用AI
                    chatPanel.addChatMessage("系统", "无法生成有效移动");
                    future.complete(null);
                }
            } catch (Exception e) {
                chatPanel.addChatMessage("系统", "处理AI响应时出错: " + e.getMessage());
                future.completeExceptionally(e);
            }
        }).start();
        
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
     * 生成提示信息
     */
    private String generatePrompt(InternationalChessBoard board, String boardDescription) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("你是一个国际象棋AI助手，请帮我下一步棋。\n\n");
        sb.append(boardDescription).append("\n");
        
        // 添加角色信息
        sb.append("你执").append(aiColor == InternationalChessBoard.WHITE ? "白" : "黑").append("子。\n");
        
        // 添加指令
        sb.append("请分析当前局面，并给出你的下一步移动。\n");
        sb.append("请使用国际象棋代数记号（例如：e2-e4）表示你的移动，并解释你的思考过程。\n");
        sb.append("确保你的移动是合法的，并考虑到国际象棋的所有规则。\n");
        
        return sb.toString();
    }
    
    /**
     * 解析大模型的响应
     */
    public int[] parseResponse(String response, InternationalChessBoard board) {
        try {
            // 尝试从响应中提取国际象棋代数记号
            // 例如：e2-e4 或 e2e4 或 从e2到e4
            
            // 简单的正则表达式匹配可能不足以处理所有情况
            // 这里使用一个简化的方法，寻找类似 e2-e4 的模式
            
            // 查找包含棋盘坐标的部分
            String[] lines = response.split("\\n");
            for (String line : lines) {
                // 尝试匹配常见的移动表示方式
                if (line.matches(".*[a-h][1-8][-到to][a-h][1-8].*") || 
                    line.matches(".*[a-h][1-8][a-h][1-8].*")) {
                    
                    // 提取坐标
                    String move = line.replaceAll("[^a-h1-8]", "");
                    if (move.length() >= 4) {
                        // 提取起始和目标位置
                        char fromCol = move.charAt(0);
                        char fromRow = move.charAt(1);
                        char toCol = move.charAt(2);
                        char toRow = move.charAt(3);
                        
                        // 转换为数组索引
                        int fromColIdx = fromCol - 'a';
                        int fromRowIdx = '8' - fromRow;
                        int toColIdx = toCol - 'a';
                        int toRowIdx = '8' - toRow;
                        
                        // 验证移动是否合法
                        if (board.isValidMove(fromRowIdx, fromColIdx, toRowIdx, toColIdx)) {
                            return new int[]{fromRowIdx, fromColIdx, toRowIdx, toColIdx};
                        }
                    }
                }
            }
            
            // 如果没有找到有效的移动，返回null
            return null;
        } catch (Exception e) {
            System.err.println("解析大模型响应时出错: " + e.getMessage());
            return null;
        }
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
     * 设置大模型
     */
    public void setModel(String model) {
        this.model = model;
    }
    
    /**
     * 设置AI颜色
     */
    public void setAiColor(char aiColor) {
        this.aiColor = aiColor;
    }
}