package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于大语言模型的象棋AI引擎
 * 集成Ollama本地部署的大模型进行象棋决策
 */
public class LLMChessAI {
    
    private final PieceColor aiColor;
    private final String modelName;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ChessAI fallbackAI; // 传统AI作为备选方案
    
    // Ollama API配置
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String GENERATE_ENDPOINT = "/api/generate";
    
    public LLMChessAI(PieceColor aiColor, String modelName, int difficulty) {
        this.aiColor = aiColor;
        this.modelName = modelName;
        this.gson = new Gson();
        this.fallbackAI = new ChessAI(aiColor, difficulty); // 传统AI作为备选
        
        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)  // 增加读取超时时间
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 获取AI的颜色
     */
    public PieceColor getColor() {
        return aiColor;
    }
    
    /**
     * 获取AI的最佳移动
     */
    public Move getBestMove(Board board) {
        System.out.println("\n🤖 " + (aiColor == PieceColor.RED ? "红方" : "黑方") + "AI思考中...");
        
        try {
            // 尝试使用大模型AI
            Move llmMove = getLLMMove(board);
            if (llmMove != null && isValidMove(board, llmMove)) {
                System.out.println("💡 大模型AI决策: " + formatMove(llmMove));
                return llmMove;
            } else {
                System.out.println("⚠️  大模型AI决策失败，切换传统AI");
            }
        } catch (Exception e) {
            System.out.println("❌ 大模型AI异常，切换传统AI: " + e.getMessage());
        }
        
        // 回退到传统AI
        Move fallbackMove = fallbackAI.getBestMove(board);
        System.out.println("🎯 传统AI决策: " + formatMove(fallbackMove));
        return fallbackMove;
    }
    
    /**
     * 使用大语言模型获取移动决策
     */
    private Move getLLMMove(Board board) throws IOException {
        String boardState = getBoardStateDescription(board);
        String prompt = buildChessPrompt(boardState);
        
        System.out.print("   🧠 分析棋局...");
        
        // 构建请求
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelName);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", false);
        
        // 正确构建options对象
        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0.3);
        options.addProperty("top_p", 0.9);
        requestBody.add("options", options);
        
        RequestBody body = RequestBody.create(
            gson.toJson(requestBody),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
                .url(OLLAMA_BASE_URL + GENERATE_ENDPOINT)
                .post(body)
                .build();
        
        // 发送请求
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP请求失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            String llmResponse = jsonResponse.get("response").getAsString();
            
            System.out.print(" 🎯 推理中...");
            
            // 解析大模型的回复，提取移动指令
            Move move = parseMove(llmResponse, board);
            
            if (move != null) {
                System.out.println(" ✅");
                System.out.println("\n🧠 AI详细思考过程：");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                printFormattedThinking(llmResponse.trim());
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            } else {
                System.out.println(" ❌");
                System.out.println("⚠️  无法解析AI回复，原始内容：");
                System.out.println(llmResponse.trim());
            }
            
            return move;
        }
    }
    
    /**
     * 构建象棋提示词
     */
    private String buildChessPrompt(String boardState) {
        String colorName = (aiColor == PieceColor.RED) ? "红方" : "黑方";
        String opponentColor = (aiColor == PieceColor.RED) ? "黑方" : "红方";
        
        return String.format(
            "你是专业的中国象棋AI，为%s选择最佳走法。请详细解释你的每一步思考过程。\n\n" +
            "当前棋盘状态：\n%s\n\n" +
            "请按以下格式详细分析：\n\n" +
            "【第一步：局面观察】\n" +
            "- 描述当前棋盘上的关键棋子位置\n" +
            "- 分析双方的棋子分布和阵型\n" +
            "- 识别当前局面的阶段（开局/中局/残局）\n\n" +
            "【第二步：威胁识别】\n" +
            "- 分析%s对我方的直接威胁\n" +
            "- 识别我方棋子面临的危险\n" +
            "- 评估对方可能的下一步攻击\n\n" +
            "【第三步：机会发现】\n" +
            "- 寻找攻击对方的机会\n" +
            "- 识别可以获得优势的走法\n" +
            "- 分析可以改善局面的策略\n\n" +
            "【第四步：候选走法分析】\n" +
            "请分析3个最佳候选走法：\n" +
            "1. 走法一：从(行,列)到(行,列) - 详细说明这步棋的目的和效果\n" +
            "2. 走法二：从(行,列)到(行,列) - 详细说明这步棋的目的和效果\n" +
            "3. 走法三：从(行,列)到(行,列) - 详细说明这步棋的目的和效果\n\n" +
            "【第五步：风险评估】\n" +
            "- 分析每个候选走法的风险\n" +
            "- 考虑对方可能的反击\n" +
            "- 评估走法的安全性\n\n" +
            "【第六步：最终决策】\n" +
            "- 综合考虑攻防平衡\n" +
            "- 选择最优走法的详细理由\n" +
            "- 解释为什么这步棋比其他选择更好\n\n" +
            "【最终走法】：从(行,列)到(行,列)\n\n" +
            "请确保你的分析逻辑清晰，每一步思考都有具体的理由支撑。", 
            colorName, boardState, opponentColor);
    }
    
    /**
     * 获取棋盘状态描述
     */
    private String getBoardStateDescription(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("  0 1 2 3 4 5 6 7 8\n");
        
        for (int row = 0; row < 10; row++) {
            sb.append(row).append(" ");
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) {
                    sb.append("口 ");
                } else {
                    String name = piece.getChineseName();
                    if (piece.getColor() == PieceColor.RED) {
                        sb.append("红").append(name);
                    } else {
                        sb.append("黑").append(name);
                    }
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        
        // 添加可用移动列表
        List<Move> availableMoves = getAllPossibleMoves(board, aiColor);
        sb.append("\n可用走法：\n");
        for (int i = 0; i < Math.min(availableMoves.size(), 10); i++) {
            Move move = availableMoves.get(i);
            sb.append(String.format("从(%d,%d)到(%d,%d) ", 
                move.getStart().getX(), move.getStart().getY(),
                move.getEnd().getX(), move.getEnd().getY()));
            if ((i + 1) % 3 == 0) sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 解析大模型回复中的移动指令
     */
    private Move parseMove(String response, Board board) {
        // 使用正则表达式提取移动坐标
        Pattern pattern = Pattern.compile("从\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s*到\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
        Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            try {
                int startRow = Integer.parseInt(matcher.group(1));
                int startCol = Integer.parseInt(matcher.group(2));
                int endRow = Integer.parseInt(matcher.group(3));
                int endCol = Integer.parseInt(matcher.group(4));
                
                Position start = new Position(startRow, startCol);
                Position end = new Position(endRow, endCol);
                
                return new Move(start, end);
            } catch (NumberFormatException e) {
                // 继续尝试其他格式
            }
        }
        
        // 如果解析失败，尝试其他格式
        Pattern simplePattern = Pattern.compile("(\\d+)\\s*,\\s*(\\d+).*?(\\d+)\\s*,\\s*(\\d+)");
        Matcher simpleMatcher = simplePattern.matcher(response);
        
        if (simpleMatcher.find()) {
            try {
                int startRow = Integer.parseInt(simpleMatcher.group(1));
                int startCol = Integer.parseInt(simpleMatcher.group(2));
                int endRow = Integer.parseInt(simpleMatcher.group(3));
                int endCol = Integer.parseInt(simpleMatcher.group(4));
                
                Position start = new Position(startRow, startCol);
                Position end = new Position(endRow, endCol);
                
                return new Move(start, end);
            } catch (NumberFormatException e) {
                // 解析失败
            }
        }
        
        return null;
    }
    
    /**
     * 验证移动是否有效
     */
    private boolean isValidMove(Board board, Move move) {
        if (move == null) {
            return false;
        }
        
        Position start = move.getStart();
        Position end = move.getEnd();
        
        // 检查坐标范围
        if (!isValidPosition(start) || !isValidPosition(end)) {
            return false;
        }
        
        // 检查起始位置是否有棋子
        Piece piece = board.getPiece(start.getX(), start.getY());
        if (piece == null || piece.getColor() != aiColor) {
            return false;
        }
        
        // 检查移动规则
        if (!piece.isValidMove(board, start, end)) {
            return false;
        }
        
        // 检查移动安全性
        return board.isMoveSafe(start, end, aiColor);
    }
    
    /**
     * 格式化打印AI思考过程
     */
    private void printFormattedThinking(String thinking) {
        String separator = repeatString("=", 80);
        System.out.println("\n" + separator);
        System.out.println("🧠 大模型AI详细思考过程");
        System.out.println(separator);
        
        String[] lines = thinking.split("\n");
        String currentSection = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 检查是否是主要步骤标题
            if (line.startsWith("【第") && line.contains("步：") && line.endsWith("】")) {
                currentSection = line;
                System.out.println("\n🔍 " + line);
                System.out.println(repeatString("─", 50));
            }
            // 检查是否是其他标题行
            else if (line.startsWith("【") && line.contains("】")) {
                System.out.println("\n📋 " + line);
                System.out.println(repeatString("─", 40));
            }
            // 列表项（带-或•开头）
            else if (line.startsWith("-") || line.startsWith("•")) {
                System.out.println("  💡 " + line.substring(1).trim());
            }
            // 数字列表（候选走法分析）
            else if (line.matches("\\d+\\.*")) {
                System.out.println("  🎯 " + line);
            }
            // 走法格式（包含"从"和"到"）
            else if (line.contains("从(") && line.contains(")到(") && line.contains(")")) {
                System.out.println("  ♟️  " + line);
            }
            // 普通文本
            else if (!line.isEmpty()) {
                System.out.println("     " + line);
            }
        }
        
        System.out.println("\n" + repeatString("=", 80));
        System.out.println("✅ 思考过程分析完成");
        System.out.println(repeatString("=", 80));
    }

    /**
     * 检查位置是否有效
     */
    private boolean isValidPosition(Position pos) {
        return pos.getX() >= 0 && pos.getX() < 10 && 
               pos.getY() >= 0 && pos.getY() < 9;
    }
    
    /**
     * 获取所有可能的移动
     */
    private List<Move> getAllPossibleMoves(Board board, PieceColor color) {
        List<Move> moves = new ArrayList<>();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color) {
                    Position start = new Position(row, col);
                    
                    for (int targetRow = 0; targetRow < 10; targetRow++) {
                        for (int targetCol = 0; targetCol < 9; targetCol++) {
                            Position end = new Position(targetRow, targetCol);
                            if (piece.isValidMove(board, start, end) && 
                                board.isMoveSafe(start, end, color)) {
                                moves.add(new Move(start, end));
                            }
                        }
                    }
                }
            }
        }
        
        return moves;
    }
    
    /**
     * 格式化移动信息
     */
    private String formatMove(Move move) {
        if (move == null) return "无效移动";
        return String.format("从(%d,%d)到(%d,%d)", 
            move.getStart().getX(), move.getStart().getY(),
            move.getEnd().getX(), move.getEnd().getY());
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
    
    /**
     * 重复字符串指定次数
     */
    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}