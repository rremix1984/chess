package com.example.chinesechess.ai;

import com.example.chinesechess.core.*;
import com.example.chinesechess.ui.AILogPanel;
import com.example.common.config.ConfigurationManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeepSeek-R1 + Pikafish 混合AI引擎
 * 采用"专业引擎 + 轻量微调"的混合架构
 * 集成Pikafish引擎提供顶级棋力，DeepSeek-R1提供局面理解
 */
public class DeepSeekPikafishAI {
    
    private final PieceColor aiColor;
    private final int difficulty;
    private final String modelName;
    
    // 核心组件
    private PikafishEngine pikafishEngine;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final EnhancedChessAI fallbackAI; // 备用AI
    private AILogPanel aiLogPanel; // AI日志面板
    private SemanticTranslatorService semanticTranslator; // 语义翻译服务
    
    // 配置管理器
    private final ConfigurationManager config;
    
    // 配置参数（从配置文件加载）
    private final String ollamaBaseUrl;
    private final String generateEndpoint;
    private final int[] thinkTimes;
    
    // 决策融合权重
    private double engineWeight = 0.8;  // Pikafish引擎权重
    private double modelWeight = 0.2;   // DeepSeek-R1权重
    
    // 反循环机制
    private List<String> moveHistory = new ArrayList<>();  // 最近的走法历史
    private static final int MAX_HISTORY_SIZE = 6;  // 保留最近6步走法
    private static final int REPETITION_THRESHOLD = 2;  // 重复阈值
    
    /**
     * 构造函数
     */
    public DeepSeekPikafishAI(PieceColor aiColor, int difficulty, String modelName) {
        this.aiColor = aiColor;
        this.difficulty = Math.max(1, Math.min(10, difficulty)); // 支持1-10级难度
        
        // 初始化配置管理器
        this.config = ConfigurationManager.getInstance();
        
        // 从配置文件加载参数
        ConfigurationManager.OllamaConfig ollamaConfig = config.getOllamaConfig();
        this.ollamaBaseUrl = ollamaConfig.baseUrl;
        this.generateEndpoint = ollamaConfig.generateEndpoint;
        this.thinkTimes = config.getAIThinkTimes();
        
        // 获取DeepSeek配置
        ConfigurationManager.DeepSeekConfig deepSeekConfig = config.getDeepSeekConfig();
        this.modelName = modelName != null ? modelName : deepSeekConfig.modelName;
        
        // 获取Pikafish配置
        ConfigurationManager.PikafishConfig pikafishConfig = config.getPikafishConfig();
        this.pikafishEngine = new PikafishEngine(pikafishConfig.enginePath);
        
        // 获取HTTP客户端配置
        ConfigurationManager.HttpClientConfig httpConfig = config.getHttpClientConfig();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(httpConfig.connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(httpConfig.readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(httpConfig.writeTimeout, TimeUnit.MILLISECONDS)
                .build();
        
        System.out.println("🔧 配置信息加载完成:");
        System.out.println("   - Ollama服务: " + this.ollamaBaseUrl);
        System.out.println("   - DeepSeek模型: " + this.modelName);
        System.out.println("   - Pikafish引擎: " + pikafishConfig.enginePath);
        System.out.println("   - 神经网络文件: " + pikafishConfig.neuralNetworkPath);
        
        this.gson = new Gson();
        this.fallbackAI = new EnhancedChessAI(aiColor, difficulty);
        this.semanticTranslator = new SemanticTranslatorService();
        
        // 尝试初始化Pikafish引擎
        initializePikafishEngine();
        
        // 检查语义翻译服务状态
        Map<String, Object> translatorStatus = semanticTranslator.getServiceStatus();
        if ((Boolean) translatorStatus.get("ready")) {
            System.out.println("✅ 语义翻译服务已就绪");
        } else {
            System.out.println("⚠️ 语义翻译服务不可用，将使用基础记谱功能");
        }
    }
    
    /**
     * 初始化Pikafish引擎
     */
    private void initializePikafishEngine() {
        // 设置日志回调，将Pikafish的决策过程输出到AI日志
        pikafishEngine.setLogCallback(this::addToAILog);

        boolean initialized = pikafishEngine.initialize();
        if (initialized) {
            System.out.println("🐟 Pikafish引擎初始化成功");
            System.out.println(pikafishEngine.getEngineInfo());
            addToAILog("Pikafish引擎初始化成功");
        } else {
            System.out.println("⚠️ 真实Pikafish引擎初始化失败，尝试使用模拟引擎");
            addToAILog("真实Pikafish引擎初始化失败，尝试使用模拟引擎");
            
            // Pikafish不可用时会自动使用备用AI
            System.out.println("⚠️ Pikafish引擎不可用，将使用增强AI作为备用方案");
            addToAILog("Pikafish引擎不可用，将使用增强AI作为备用方案");
            addToAILog("💡 提示：请参考 PIKAFISH_INSTALL.md 安装 Pikafish 引擎");
            
            // 不要将引擎设置为null，保持引用但标记为不可用
            // this.pikafishEngine = null; // 移除这行，避免空指针异常
        }
    }
    
    /**
     * 获取最佳移动（国际象棋版本）
     */
    public Move getBestMove(com.example.chinesechess.core.InternationalChessBoard board) {
        System.out.println("🧠 DeepSeek-Pikafish混合AI思考中...");
        
        try {
            // 转换为FEN格式（国际象棋）
            String fen = convertInternationalBoardToFen(board);
            
            // 1. 使用Pikafish引擎获取最佳走法
            int thinkTime = thinkTimes[difficulty - 1];
            String engineMove = getPikafishMove(fen, thinkTime);
            
            // 2. 使用DeepSeek-R1评估局面
            double positionEval = evaluatePositionWithDeepSeek(fen);
            
            // 3. 决策融合
            String finalMove = fusionDecision(fen, engineMove, positionEval);
            
            // 4. 转换为Move对象
            Move move = convertUciToMoveInternational(finalMove, board);
            
            if (move != null) {
                // 将走法添加到历史记录
                addMoveToHistory(finalMove);
                
                System.out.println("✅ 国际象棋AI选择走法: " + finalMove);
                return move;
            }
            
        } catch (Exception e) {
            System.err.println("❌ 混合AI计算失败: " + e.getMessage());
        }
        
        // 降级到传统国际象棋AI
        System.out.println("🔄 降级使用传统国际象棋AI");
        char aiColorChar = (aiColor == com.example.chinesechess.core.PieceColor.RED) ? 
            com.example.chinesechess.core.InternationalChessBoard.WHITE : 
            com.example.chinesechess.core.InternationalChessBoard.BLACK;
        com.example.chinesechess.ai.InternationalChessAI fallbackInternationalAI = 
            new com.example.chinesechess.ai.InternationalChessAI(difficulty, aiColorChar);
        int[] moveArray = fallbackInternationalAI.calculateNextMove(board);
        if (moveArray != null && moveArray.length == 4) {
            return new Move(
                new com.example.chinesechess.core.Position(moveArray[0], moveArray[1]),
                new com.example.chinesechess.core.Position(moveArray[2], moveArray[3])
            );
        }
        
        return null;
    }

    /**
     * 获取最佳移动（中国象棋版本）- 优化版本，优先使用Pikafish引擎
     */
    public Move getBestMove(Board board) {
        System.out.println("🧠 DeepSeek-Pikafish混合AI思考中...");
        
        try {
            // 转换为FEN格式
            String fen = FenConverter.boardToFen(board, aiColor);
            System.out.println("🔍 [调试] FEN: " + fen);
            
            int thinkTime = thinkTimes[difficulty - 1];
            
            // 优先尝试Pikafish引擎
            if (pikafishEngine != null && pikafishEngine.isAvailable()) {
                System.out.println("🐟 优先使用Pikafish引擎计算");
                
                String engineMove = getPikafishMove(fen, thinkTime);
                System.out.println("🔍 [调试] Pikafish引擎返回: " + engineMove);
                
                if (engineMove != null) {
                    // 检查是否会导致循环走法
                    if (!isRepetitiveMove(engineMove)) {
                        Move move = convertUciToMove(engineMove, board);
                        if (move != null) {
                            // 将走法添加到历史记录
                            addMoveToHistory(engineMove);
                            
                            System.out.println("✅ Pikafish引擎选择走法: " + engineMove);
                            return move;
                        }
                    } else {
                        System.out.println("🔄 检测到循环走法，尝试获取替代走法");
                        List<String> candidateMoves = getCandidateMovesFromPikafish(fen, 5);
                        for (String candidate : candidateMoves) {
                            if (!isRepetitiveMove(candidate)) {
                                Move move = convertUciToMove(candidate, board);
                                if (move != null) {
                                    addMoveToHistory(candidate);
                                    System.out.println("✅ 找到替代走法: " + candidate);
                                    return move;
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("⚠️ Pikafish引擎不可用");
            }
            
            // 如果Pikafish不可用或没有返回有效走法，可选地尝试DeepSeek评估
            // 但由于DeepSeek经常超时，我们直接跳到备用AI
            System.out.println("🔍 [调试] Pikafish引擎无结果，跳过DeepSeek评估，直接使用备用AI");
            
        } catch (Exception e) {
            System.err.println("❌ 混合AI计算失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 降级到备用AI
        System.out.println("🔄 降级使用增强AI");
        return fallbackAI.getBestMove(board);
    }


    
    /**
     * 使用Pikafish引擎获取走法
     */
    private String getPikafishMove(String fen, int thinkTime) {
        if (pikafishEngine == null || !pikafishEngine.isAvailable()) {
            return null;
        }
        
        addToAILog("=== Pikafish引擎决策开始 ===");
        
        long startTime = System.currentTimeMillis();
        String move = pikafishEngine.getBestMove(fen, thinkTime);
        long endTime = System.currentTimeMillis();
        
        addToAILog("=== Pikafish引擎决策结束 ===");
        
        if (move != null) {
            System.out.println("Pikafish引擎计算完成，用时: " + (endTime - startTime) + "ms，走法: " + move);
        }
        
        return move;
    }
    
    /**
     * 使用DeepSeek-R1评估局面
     */
    private double evaluatePositionWithDeepSeek(String fen) {
        try {
            System.out.println("🔍 [调试] 开始DeepSeek评估...");
            long startTime = System.currentTimeMillis();
            
            String prompt = buildEvaluationPrompt(fen);
            String response = callDeepSeekModel(prompt);
            
            long endTime = System.currentTimeMillis();
            System.out.println("🔍 [调试] DeepSeek评估完成，用时: " + (endTime - startTime) + "ms");
            
            if (response != null) {
                double score = parseEvaluationScore(response);
                System.out.println("🔍 [调试] DeepSeek评估分数: " + score);
                return score;
            } else {
                System.out.println("🔍 [调试] DeepSeek返回null，使用默认评估");
            }
        } catch (Exception e) {
            System.err.println("❌ DeepSeek-R1评估失败: " + e.getMessage());
            System.out.println("🔍 [调试] DeepSeek评估异常，使用默认评估");
        }
        
        return 0.0; // 默认评估为平衡
    }
    
    /**
     * 构建评估提示词
     */
    private String buildEvaluationPrompt(String fen) {
        return String.format(
            "你是一位中国象棋特级大师，请评估以下局面：\n\n" +
            "FEN: %s\n\n" +
            "请从以下角度进行分析：\n" +
            "1. 子力对比（棋子价值和数量）\n" +
            "2. 位置优势（棋子的活跃度和控制力）\n" +
            "3. 王的安全性\n" +
            "4. 战术机会（将军、捉子、牵制等）\n" +
            "5. 战略布局（子力协调、空间控制）\n\n" +
            "请给出一个评估分数，范围从-10到+10：\n" +
            "- 正数表示红方优势\n" +
            "- 负数表示黑方优势\n" +
            "- 0表示局面平衡\n\n" +
            "最后请用以下格式输出：\n" +
            "评估分数: [分数]\n" +
            "主要原因: [简要说明]", 
            fen);
    }
    
    /**
     * 公共API：调用DeepSeek模型进行分析（供外部调用）
     */
    public String callDeepSeekAPI(String prompt) {
        return callDeepSeekModel(prompt);
    }
    
    /**
     * 调用DeepSeek模型
     */
    private String callDeepSeekModel(String prompt) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", modelName);
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("stream", false);
            requestBody.addProperty("temperature", 0.1);
            
            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(ollamaBaseUrl + generateEndpoint)
                    .post(body)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    
                    if (jsonResponse.has("response")) {
                        return jsonResponse.get("response").getAsString();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ DeepSeek模型调用失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 解析评估分数
     */
    private double parseEvaluationScore(String response) {
        if (response == null) {
            return 0.0;
        }
        
        // 使用正则表达式提取评估分数
        Pattern pattern = Pattern.compile("评估分数[：:]\\s*([+-]?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("❌ 解析评估分数失败: " + matcher.group(1));
            }
        }
        
        // 备用解析方法：查找数字
        pattern = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
        matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            try {
                double score = Double.parseDouble(matcher.group(1));
                if (score >= -10 && score <= 10) {
                    return score;
                }
            } catch (NumberFormatException e) {
                // 忽略
            }
        }
        
        return 0.0;
    }
    
    /**
     * 决策融合
     */
    private String fusionDecision(String fen, String engineMove, double positionEval) {
        // 如果引擎没有给出走法，直接返回null
        if (engineMove == null) {
            return null;
        }
        
        // 检查是否会导致循环走法
        if (isRepetitiveMove(engineMove)) {
            System.out.println("🔄 检测到潜在循环走法: " + engineMove + "，尝试获取替代走法");
            
            // 尝试获取多个候选走法
            List<String> candidateMoves = getCandidateMovesFromPikafish(fen, 5);
            
            // 寻找非重复的走法
            for (String candidate : candidateMoves) {
                if (!isRepetitiveMove(candidate)) {
                    System.out.println("✅ 找到替代走法: " + candidate);
                    return candidate;
                }
            }
            
            System.out.println("⚠️ 未找到非重复走法，使用原始走法但清空历史记录");
            moveHistory.clear();  // 清空历史记录以打破僵局
        }
        
        // 根据局面评估调整权重
        if (Math.abs(positionEval) > 3.0) {
            // 局面明显倾斜时，更信任引擎
            engineWeight = 0.9;
            modelWeight = 0.1;
        } else if (Math.abs(positionEval) < 0.5) {
            // 接近平衡局面时，增加模型权重
            engineWeight = 0.7;
            modelWeight = 0.3;
            
            // 移除额外思考时间以提高下棋速度
            // 直接使用原始走法，不再进行深度计算
        }
        
        return engineMove;
    }

    /**
     * 获取带分析的推荐走法
     * @param board 当前棋盘
     * @param numMoves 需要的推荐走法数量
     * @return 带分析的推荐走法列表
     */
    public List<String> getRecommendedMovesWithAnalysis(Board board, int numMoves) {
        List<String> analysisResults = new ArrayList<>();
        String fen = FenConverter.boardToFen(board, aiColor);
        List<String> candidateMoves = getCandidateMovesFromPikafish(fen, numMoves);

        for (String moveUci : candidateMoves) {
            try {
                Board tempBoard = board.clone();
                Move move = convertUciToMove(moveUci, tempBoard);
                if (move != null) {
                    tempBoard.makeMove(move);
                    String nextFen = FenConverter.boardToFen(tempBoard, aiColor.getOpposite());
                    String analysis = analyzeMoveWithDeepSeek(fen, nextFen, moveUci);
                    analysisResults.add(analysis);
                }
            } catch (Exception e) {
                System.err.println("分析走法时出错: " + moveUci + ", " + e.getMessage());
            }
        }
        return analysisResults;
    }

    /**
     * 使用DeepSeek分析单个走法
     */
    private String analyzeMoveWithDeepSeek(String originalFen, String nextFen, String moveUci) {
        String prompt = String.format(
            "你是一位中国象棋特级大师，请分析从局面 A 到局面 B 的走法。\n\n" +
            "局面 A (走法前): %s\n" +
            "走法: %s\n" +
            "局面 B (走法后): %s\n\n" +
            "请分析此走法的优缺点，并从战略和战术角度进行评估。\n" +
            "请用以下格式输出：\n" +
            "走法: %s\n" +
            "优点: [优点分析]\n" +
            "缺点: [缺点分析]\n" +
            "综合评估: [综合评估]",
            originalFen, moveUci, nextFen, moveUci);

        return callDeepSeekModel(prompt);
    }

    
    /**
     * 检查走法是否会导致重复循环
     */
    private boolean isRepetitiveMove(String move) {
        if (move == null || moveHistory.size() < 2) {
            return false;
        }
        
        // 检查是否与最近的走法形成简单循环（A-B-A模式）
        if (moveHistory.size() >= 2) {
            String lastMove = moveHistory.get(moveHistory.size() - 1);
            String secondLastMove = moveHistory.get(moveHistory.size() - 2);
            
            // 检查是否形成A-B-A循环
            if (move.equals(secondLastMove) && !move.equals(lastMove)) {
                return true;
            }
        }
        
        // 检查在历史记录中的重复次数
        int count = 0;
        for (String historyMove : moveHistory) {
            if (move.equals(historyMove)) {
                count++;
            }
        }
        
        return count >= REPETITION_THRESHOLD;
    }
    
    /**
     * 将走法添加到历史记录
     */
    private void addMoveToHistory(String move) {
        if (move != null) {
            moveHistory.add(move);
            
            // 保持历史记录大小在限制范围内
            while (moveHistory.size() > MAX_HISTORY_SIZE) {
                moveHistory.remove(0);
            }
            
            System.out.println("📝 走法历史: " + moveHistory);
        }
    }
    
    /**
     * 将UCI格式转换为Move对象（增强版本）
     */
    private Move convertUciToMove(String uci, Board board) {
        if (uci == null) {
            System.out.println("🔍 [调试] UCI为null，无法转换");
            return null;
        }
        
        try {
            System.out.println("🔍 [调试] 开始转换UCI: " + uci);
            
            // 验证UCI格式
            if (uci.length() != 4) {
                System.out.println("❌ [调试] UCI格式错误，长度不为4: " + uci);
                return null;
            }
            
            Position[] positions = FenConverter.uciToMove(uci);
            System.out.println("🔍 [调试] FenConverter.uciToMove结果: " + (positions != null ? "成功" : "失败"));
            
            if (positions != null && positions.length == 2) {
                Position from = positions[0];
                Position to = positions[1];
                
                System.out.println("🔍 [调试] 转换后坐标: " + from + " -> " + to);
                
                // 检查坐标范围
                if (!isValidPosition(from) || !isValidPosition(to)) {
                    System.out.println("❌ [调试] 坐标超出棋盘范围: " + from + " -> " + to);
                    return null;
                }
                
                Move move = new Move(from, to);
                
                // 增强的走法验证
                boolean isValid = isValidMoveEnhanced(move, board, uci);
                System.out.println("🔍 [调试] 走法验证结果: " + (isValid ? "合法" : "不合法"));
                
                if (isValid) {
                    return move;
                } else {
                    // 如果验证失败，尝试使用所有可能走法进行匹配
                    Move fallbackMove = findMoveByUCI(uci, board);
                    if (fallbackMove != null) {
                        System.out.println("✅ [调试] 通过备用方法找到合法走法: " + uci);
                        return fallbackMove;
                    }
                    System.out.println("❌ [调试] 走法验证失败且无备用方案: " + uci);
                }
            } else {
                System.out.println("❌ [调试] FenConverter.uciToMove返回无效结果");
            }
        } catch (Exception e) {
            System.err.println("❌ UCI转换失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 检查位置是否在棋盘范围内
     */
    private boolean isValidPosition(Position pos) {
        return pos != null && pos.getX() >= 0 && pos.getX() <= 9 && pos.getY() >= 0 && pos.getY() <= 8;
    }
    
    /**
     * 增强的走法验证
     */
    private boolean isValidMoveEnhanced(Move move, Board board, String originalUci) {
        try {
            Position start = move.getStart();
            Position end = move.getEnd();
            
            // 检查起始位置是否有棋子
            Piece piece = board.getPiece(start.getX(), start.getY());
            if (piece == null) {
                System.out.println("❌ [验证] 起始位置无棋子: " + start + " (UCI: " + originalUci + ")");
                return false;
            }
            
            // 检查棋子颜色是否正确
            if (piece.getColor() != aiColor) {
                System.out.println("❌ [验证] 棋子颜色不匹配: " + piece.getColor() + " vs " + aiColor + " (UCI: " + originalUci + ")");
                return false;
            }
            
            // 检查目标位置是否可以移动到
            Piece targetPiece = board.getPiece(end.getX(), end.getY());
            if (targetPiece != null && targetPiece.getColor() == aiColor) {
                System.out.println("❌ [验证] 目标位置有同方棋子: " + end + " (UCI: " + originalUci + ")");
                return false;
            }
            
            // 使用棋子的isValidMove方法验证（但要捕获异常）
            try {
                boolean pieceValidation = piece.isValidMove(board, start, end);
                if (!pieceValidation) {
                    System.out.println("❌ [验证] 棋子移动规则验证失败: " + piece.getChineseName() + " " + start + "->" + end + " (UCI: " + originalUci + ")");
                }
                return pieceValidation;
            } catch (Exception e) {
                System.out.println("⚠️ [验证] 棋子验证异常，但允许通过: " + e.getMessage() + " (UCI: " + originalUci + ")");
                return true; // 如果验证方法本身有问题，允许走法通过
            }
        } catch (Exception e) {
            System.out.println("❌ [验证] 增强验证异常: " + e.getMessage() + " (UCI: " + originalUci + ")");
            return false;
        }
    }
    
    /**
     * 通过UCI在所有可能走法中寻找匹配
     */
    private Move findMoveByUCI(String uci, Board board) {
        try {
            List<Move> allPossibleMoves = getAllPossibleMoves(board);
            for (Move move : allPossibleMoves) {
                String moveUci = FenConverter.moveToUci(move.getStart(), move.getEnd());
                if (uci.equals(moveUci)) {
                    System.out.println("✅ [备用] 在可能走法中找到匹配: " + uci);
                    return move;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ [备用] 备用查找失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 验证走法是否合法
     */
    private boolean isValidMove(Move move, Board board) {
        try {
            Position start = move.getStart();
            Position end = move.getEnd();
            
            Piece piece = board.getPiece(start.getX(), start.getY());
            if (piece == null || piece.getColor() != aiColor) {
                return false;
            }
            
            // 使用棋子的isValidMove方法验证
            return piece.isValidMove(board, start, end);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取所有可能的移动
     */
    public List<Move> getAllPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == aiColor) {
                    Position start = new Position(row, col);
                    
                    // 获取该棋子的所有可能移动
                    for (int toRow = 0; toRow < 10; toRow++) {
                        for (int toCol = 0; toCol < 9; toCol++) {
                            Position end = new Position(toRow, toCol);
                            if (piece.isValidMove(board, start, end)) {
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
     * 智能计算增强思考时间
     * 根据难度级别动态调整引擎性能，提升棋力
     */
    private int calculateEnhancedThinkTime(int difficulty) {
        int baseTime = thinkTimes[Math.min(difficulty - 1, thinkTimes.length - 1)];
        
        // 根据难度级别智能调整思考时间倍数
        double multiplier;
        switch (difficulty) {
            case 1: // 入门级
                multiplier = 1.5; // 轻微提升
                break;
            case 2: // 初级
                multiplier = 2.0; // 双倍时间
                break;
            case 3: // 中级
                multiplier = 2.5; // 2.5倍时间
                break;
            case 4: // 高级
                multiplier = 3.0; // 3倍时间
                break;
            case 5: // 专家级
                multiplier = 4.0; // 4倍时间，确保最强棋力
                break;
            case 6:
            case 7:
            case 8:
                multiplier = 5.0; // 5倍时间，超级专家级
                break;
            case 9:
            case 10:
                multiplier = 6.0; // 6倍时间，大师级
                break;
            default:
                multiplier = 2.0; // 默认双倍时间
                break;
        }
        
        int enhancedTime = (int)(baseTime * multiplier);
        
        // 设置最小和最大思考时间限制
        int minTime = 1000; // 最少1秒
        int maxTime = 30000; // 最多30秒
        
        enhancedTime = Math.max(minTime, Math.min(enhancedTime, maxTime));
        
        System.out.println(String.format(
            "🎯 智能思考时间计算: 难度%d, 基础时间%dms, 倍数%.1f, 最终时间%dms", 
            difficulty, baseTime, multiplier, enhancedTime));
        
        return enhancedTime;
    }
    
    /**
     * 获取AI颜色
     */
    public PieceColor getColor() {
        return aiColor;
    }
    
    /**
     * 获取难度级别
     */
    public int getDifficulty() {
        return difficulty;
    }
    
    /**
     * 获取模型名称
     */
    public String getModelName() {
        return modelName;
    }
    
    /**
     * 检查Pikafish引擎是否可用
     */
    public boolean isPikafishAvailable() {
        return pikafishEngine.isAvailable();
    }
    
    /**
     * 获取引擎状态信息
     */
    public String getEngineStatus() {
        StringBuilder status = new StringBuilder();
        status.append("🤖 DeepSeek-Pikafish混合AI状态:\n");
        status.append("- 颜色: ").append(aiColor == PieceColor.RED ? "红方" : "黑方").append("\n");
        status.append("- 难度: ").append(difficulty).append("/5\n");
        status.append("- 模型: ").append(modelName).append("\n");
        status.append("- Pikafish: ").append(isPikafishAvailable() ? "可用" : "不可用").append("\n");
        status.append("- 引擎权重: ").append(String.format("%.1f", engineWeight)).append("\n");
        status.append("- 模型权重: ").append(String.format("%.1f", modelWeight));
        
        return status.toString();
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        try {
            if (pikafishEngine != null) {
                pikafishEngine.quit();
            }
            
            if (httpClient != null) {
                httpClient.dispatcher().executorService().shutdown();
                httpClient.connectionPool().evictAll();
            }
            
            System.out.println("🔄 DeepSeek-Pikafish AI资源已释放");
        } catch (Exception e) {
            System.err.println("❌ 关闭资源时出错: " + e.getMessage());
        }
    }
    
    /**
     * 设置AI日志面板
     */
    public void setAILogPanel(AILogPanel aiLogPanel) {
        this.aiLogPanel = aiLogPanel;
        // 同时为语义翻译服务设置AI日志面板，以便python-chinese-chess日志能输出到AI决策日志
        if (semanticTranslator != null) {
            semanticTranslator.setAILogPanel(aiLogPanel);
        }
    }
    
    /**
     * 添加AI决策日志
     */
    private void addToAILog(String message) {
        // 同时输出到控制台和AI日志面板
        String logMessage = "🐟 [Pikafish] " + message;
        System.out.println(logMessage);
        
        if (aiLogPanel != null && aiLogPanel.isLogEnabled()) {
            aiLogPanel.addAIDecision(logMessage);
        }
    }
    
    /**
     * 评估当前棋局并给出建议（用于AI棋局讨论）
     */
    public String evaluateGameAndGiveAdvice(Board board, PieceColor playerColor) {
        try {
            System.out.println("🔍 Pikafish开始评估棋局...");
            
            // 转换为FEN格式
            String fen = FenConverter.boardToFen(board, playerColor);
            
            // 使用Pikafish引擎分析当前局面
            String engineAnalysis = analyzePositionWithPikafish(fen, 2000); // 2秒深度分析
            
            // 获取多个候选走法
            List<String> candidateMoves = getCandidateMovesFromPikafish(fen, 3); // 获取前3个走法
            
            // 构建评估报告
            StringBuilder advice = new StringBuilder();
            advice.append("🐟 **Pikafish引擎分析报告**\n\n");
            
            // 1. 局面评估
            if (engineAnalysis != null && !engineAnalysis.isEmpty()) {
                advice.append("📊 **局面评估**：\n");
                advice.append(parseEngineEvaluation(engineAnalysis)).append("\n\n");
            }
            
            // 2. 推荐走法
            if (candidateMoves != null && !candidateMoves.isEmpty()) {
                advice.append("🎯 **推荐走法**：\n");
                for (int i = 0; i < candidateMoves.size(); i++) {
                    String move = candidateMoves.get(i);
                    String moveDescription = describeMoveInChinese(move, board);
                    advice.append(String.format("%d. %s (%s)\n", i + 1, moveDescription, move));
                }
                advice.append("\n");
            }
            
            // 3. 战术建议
            advice.append("💡 **战术建议**：\n");
            advice.append(generateTacticalAdvice(engineAnalysis, candidateMoves, board, playerColor));
            
            return advice.toString();
            
        } catch (Exception e) {
            System.err.println("❌ Pikafish评估失败: " + e.getMessage());
            return "❌ 抱歉，Pikafish引擎暂时无法分析当前棋局。请检查引擎状态。";
        }
    }
    
    /**
     * 评估棋局并获取推荐走法的详细信息（包含位置坐标）
     * @param board 棋盘
     * @param playerColor 玩家颜色
     * @return 包含推荐走法详细信息的结果
     */
    public EvaluationResult evaluateGameWithDetails(Board board, PieceColor playerColor) {
        try {
            System.out.println("🔍 Pikafish开始详细评估棋局...");
            
            // 转换为FEN格式
            String fen = FenConverter.boardToFen(board, playerColor);
            
            // 使用Pikafish引擎分析当前局面
            String engineAnalysis = analyzePositionWithPikafish(fen, 2000);
            
            // 获取多个候选走法
            List<String> candidateMoves = getCandidateMovesFromPikafish(fen, 3);
            
            // 创建评估结果
            EvaluationResult result = new EvaluationResult();
            result.setEngineAnalysis(engineAnalysis);
            result.setEvaluation(parseEngineEvaluation(engineAnalysis));
            
            // 解析推荐走法
            List<RecommendedMove> recommendedMoves = new ArrayList<>();
            if (candidateMoves != null && !candidateMoves.isEmpty()) {
                for (int i = 0; i < candidateMoves.size(); i++) {
                    String uciMove = candidateMoves.get(i);
                    RecommendedMove recMove = parseRecommendedMove(uciMove, board, i + 1);
                    if (recMove != null) {
                        recommendedMoves.add(recMove);
                    }
                }
            }
            result.setRecommendedMoves(recommendedMoves);
            
            // 生成文本报告
            StringBuilder advice = new StringBuilder();
            advice.append("🐟 **Pikafish引擎分析报告**\n\n");
            
            if (result.getEvaluation() != null && !result.getEvaluation().isEmpty()) {
                advice.append("📊 **局面评估**：\n");
                advice.append(result.getEvaluation()).append("\n\n");
            }
            
            if (!recommendedMoves.isEmpty()) {
                advice.append("🎯 **推荐走法**：\n");
                for (RecommendedMove recMove : recommendedMoves) {
                    advice.append(String.format("%d. %s (%s)\n", 
                        recMove.getRank(), recMove.getDescription(), recMove.getUciMove()));
                }
                advice.append("\n");
            }
            
            advice.append("💡 **战术建议**：\n");
            advice.append(generateTacticalAdvice(engineAnalysis, candidateMoves, board, playerColor));
            
            result.setAdviceText(advice.toString());
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Pikafish详细评估失败: " + e.getMessage());
            EvaluationResult errorResult = new EvaluationResult();
            errorResult.setAdviceText("❌ 抱歉，Pikafish引擎暂时无法分析当前棋局。请检查引擎状态。");
            return errorResult;
        }
    }
    
    /**
     * 解析推荐走法，转换为包含位置坐标的对象
     */
    private RecommendedMove parseRecommendedMove(String uciMove, Board board, int rank) {
        try {
            Position[] positions = FenConverter.uciToMove(uciMove);
            if (positions != null && positions.length == 2) {
                Position startPos = positions[0];
                Position endPos = positions[1];
                
                // 验证位置有效性
                if (isValidPosition(startPos) && isValidPosition(endPos)) {
                    String description = describeMoveInChinese(uciMove, board);
                    
                    RecommendedMove recMove = new RecommendedMove();
                    recMove.setUciMove(uciMove);
                    recMove.setStartPosition(startPos);
                    recMove.setEndPosition(endPos);
                    recMove.setDescription(description);
                    recMove.setRank(rank);
                    
                    return recMove;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 解析推荐走法失败: " + uciMove + ", " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 推荐走法数据类
     */
    public static class RecommendedMove {
        private String uciMove;
        private Position startPosition;
        private Position endPosition;
        private String description;
        private int rank;
        
        // Getters and setters
        public String getUciMove() { return uciMove; }
        public void setUciMove(String uciMove) { this.uciMove = uciMove; }
        
        public Position getStartPosition() { return startPosition; }
        public void setStartPosition(Position startPosition) { this.startPosition = startPosition; }
        
        public Position getEndPosition() { return endPosition; }
        public void setEndPosition(Position endPosition) { this.endPosition = endPosition; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }
    
    /**
     * 评估结果数据类
     */
    public static class EvaluationResult {
        private String engineAnalysis;
        private String evaluation;
        private List<RecommendedMove> recommendedMoves;
        private String adviceText;
        
        public EvaluationResult() {
            this.recommendedMoves = new ArrayList<>();
        }
        
        // Getters and setters
        public String getEngineAnalysis() { return engineAnalysis; }
        public void setEngineAnalysis(String engineAnalysis) { this.engineAnalysis = engineAnalysis; }
        
        public String getEvaluation() { return evaluation; }
        public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
        
        public List<RecommendedMove> getRecommendedMoves() { return recommendedMoves; }
        public void setRecommendedMoves(List<RecommendedMove> recommendedMoves) { this.recommendedMoves = recommendedMoves; }
        
        public String getAdviceText() { return adviceText; }
        public void setAdviceText(String adviceText) { this.adviceText = adviceText; }
    }
    
    /**
     * 使用Pikafish引擎深度分析局面（增强版本）
     */
    private String analyzePositionWithPikafish(String fen, int timeMs) {
        if (pikafishEngine == null || !pikafishEngine.isAvailable()) {
            addToAILog("⚠️ Pikafish引擎不可用，无法分析局面");
            return null;
        }
        
        try {
            addToAILog("开始深度分析局面，时间: " + timeMs + "ms");
            
            // 设置局面
            pikafishEngine.setPosition(fen);
            
            // 开始分析，使用更长的超时时间
            int extendedTime = Math.max(timeMs, 3000); // 至少 3 秒
            String result = pikafishEngine.getBestMove(fen, extendedTime);
            
            // 获取详细分析信息
            String analysisInfo = pikafishEngine.getLastAnalysisInfo();
            
            if (analysisInfo == null || analysisInfo.trim().isEmpty()) {
                addToAILog("⚠️ 未获取到分析信息，可能是搜索深度不足");
                
                // 尝试使用更长时间重新分析
                result = pikafishEngine.getBestMove(fen, extendedTime * 2);
                analysisInfo = pikafishEngine.getLastAnalysisInfo();
            }
            
            if (analysisInfo != null && !analysisInfo.trim().isEmpty()) {
                addToAILog("✅ 获取到分析信息，长度: " + analysisInfo.length() + " 字符");
            } else {
                addToAILog("❌ 仍然未获取到分析信息");
            }
            
            return analysisInfo;
            
        } catch (Exception e) {
            addToAILog("❌ Pikafish分析异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取Pikafish引擎的候选走法（增强版本）
     */
    private List<String> getCandidateMovesFromPikafish(String fen, int count) {
        if (pikafishEngine == null || !pikafishEngine.isAvailable()) {
            addToAILog("⚠️ Pikafish引擎不可用，无法获取候选走法");
            return new ArrayList<>();
        }
        
        addToAILog("——— 获取Pikafish候选走法 ———");
        
        // 智能增强的超时参数，给引擎更多时间提升棋力
        int extendedThinkTime = calculateEnhancedThinkTime(difficulty);
        addToAILog("使用增强的思考时间: " + extendedThinkTime + "ms（难度级别: " + difficulty + "）");
        
        List<String> moves = pikafishEngine.getBestMoves(fen, extendedThinkTime, count);
        
        if (moves != null && !moves.isEmpty()) {
            addToAILog("✅ Pikafish返回 " + moves.size() + " 个候选走法");
            for (int i = 0; i < moves.size(); i++) {
                addToAILog(String.format("候选走法 %d: %s", i + 1, moves.get(i)));
            }
            return moves;
        } else {
            addToAILog("⚠️ Pikafish未返回候选走法，尝试单独获取最佳走法");
            
            // 备用方案：尝试获取单个最佳走法
            String bestMove = pikafishEngine.getBestMove(fen, extendedThinkTime);
            if (bestMove != null && !bestMove.trim().isEmpty()) {
                addToAILog("✅ 通过备用方法获取走法: " + bestMove);
                return Arrays.asList(bestMove);
            }
            
            // 最后的备用方案：使用最小时间、最小深度尝试
            addToAILog("⚠️ 尝试使用最小参数重新获取走法");
            String emergencyMove = pikafishEngine.getBestMove(fen, 500); // 500ms 最小时间
            if (emergencyMove != null && !emergencyMove.trim().isEmpty()) {
                addToAILog("✅ 紧急模式获取走法: " + emergencyMove);
                return Arrays.asList(emergencyMove);
            }
            
            addToAILog("❌ 所有尝试都失败，可能是无法移动的局面或引擎问题");
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析引擎评估信息
     */
    private String parseEngineEvaluation(String analysisInfo) {
        if (analysisInfo == null || analysisInfo.isEmpty()) {
            return "局面评估信息不可用";
        }
        
        StringBuilder eval = new StringBuilder();
        
        // 解析评估分数
        if (analysisInfo.contains("cp ")) {
            try {
                String[] parts = analysisInfo.split("cp ");
                if (parts.length > 1) {
                    String scoreStr = parts[1].split(" ")[0];
                    int centipawns = Integer.parseInt(scoreStr);
                    double pawns = centipawns / 100.0;
                    
                    if (pawns > 0) {
                        eval.append(String.format("红方优势约 %.1f 兵", pawns));
                    } else if (pawns < 0) {
                        eval.append(String.format("黑方优势约 %.1f 兵", Math.abs(pawns)));
                    } else {
                        eval.append("局面基本平衡");
                    }
                }
            } catch (Exception e) {
                eval.append("评估分数解析失败");
            }
        }
        
        // 解析搜索深度
        if (analysisInfo.contains("depth ")) {
            try {
                String[] parts = analysisInfo.split("depth ");
                if (parts.length > 1) {
                    String depthStr = parts[1].split(" ")[0];
                    eval.append(String.format("（搜索深度：%s层）", depthStr));
                }
            } catch (Exception e) {
                // 忽略深度解析错误
            }
        }
        
        return eval.length() > 0 ? eval.toString() : "局面评估信息不完整";
    }
    
    /**
     * 将UCI走法转换为中文描述
     */
    private String describeMoveInChinese(String uciMove, Board board) {
        if (uciMove == null || uciMove.length() != 4) {
            return "走法格式错误";
        }
        
        try {
            // 使用FenConverter正确解析UCI坐标
            Position fromPos = FenConverter.uciToPosition(uciMove.substring(0, 2));
            Position toPos = FenConverter.uciToPosition(uciMove.substring(2, 4));
            
            if (fromPos == null || toPos == null) {
                return "坐标转换失败";
            }
            
            Piece piece = board.getPiece(fromPos.getX(), fromPos.getY());
            if (piece == null) {
                return "无效走法";
            }
            
            // 使用标准象棋术语转换
            String standardNotation = convertToStandardChessNotation(piece, fromPos, toPos);
            if (standardNotation != null) {
                return standardNotation;
            }
            
            // 备用格式：简单描述
            Piece targetPiece = board.getPiece(toPos.getX(), toPos.getY());
            boolean isCapture = targetPiece != null;
            String color = piece.getColor() == PieceColor.RED ? "红" : "黑";
            String pieceName = piece.getChineseName();
            String action = isCapture ? "吃" : "走";
            
            return String.format("%s%s%s至%s", color, pieceName, action, formatPosition(toPos, piece.getColor()));
            
        } catch (Exception e) {
            return "走法描述失败";
        }
    }
    
    /**
     * 使用语义翻译服务增强的中文记谱描述
     */
    public String describeMoveWithSemantics(String uciMove, Board board) {
        // 首先使用基础方法获取描述
        String basicDescription = describeMoveInChinese(uciMove, board);
        
        // 如果语义翻译服务可用，尝试进行语义增强
        if (semanticTranslator != null) {
            Map<String, Object> status = semanticTranslator.getServiceStatus();
            if ((Boolean) status.get("ready")) {
                try {
                    // 验证基础描述的格式
                    SemanticTranslatorService.ValidationResult validation = 
                        semanticTranslator.validateNotation(basicDescription);
                    
                    if (validation != null && validation.isValid()) {
                        // 如果格式有效，解析语义信息
                        SemanticTranslatorService.ParseResult parsed = 
                            semanticTranslator.parseNotation(basicDescription);
                        
                        if (parsed != null) {
                            // 根据语义信息生成更详细的描述
                            return enhanceDescriptionWithSemantics(basicDescription, parsed);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("语义增强失败: " + e.getMessage());
                }
            }
        }
        
        return basicDescription;
    }
    
    /**
     * 根据语义信息增强描述
     */
    private String enhanceDescriptionWithSemantics(String basicDescription, 
                                                   SemanticTranslatorService.ParseResult parsed) {
        StringBuilder enhanced = new StringBuilder(basicDescription);
        
        // 添加语义信息
        if (parsed.getAction() != null) {
            String actionDesc = "";
            switch (parsed.getAction()) {
                case "forward":
                    actionDesc = "(向前移动)";
                    break;
                case "backward":
                    actionDesc = "(向后移动)";
                    break;
                case "horizontal":
                    actionDesc = "(横向移动)";
                    break;
            }
            if (!actionDesc.isEmpty()) {
                enhanced.append(" ").append(actionDesc);
            }
        }
        
        return enhanced.toString();
    }
    
    /**
     * 解析用户输入的中文记谱
     */
    public Map<String, Object> parseUserNotation(String notation) {
        if (semanticTranslator == null) {
            return null;
        }
        
        return semanticTranslator.smartParse(notation);
    }
    
    /**
     * 获取记谱格式建议
     */
    public List<String> getNotationFormatSuggestions() {
        if (semanticTranslator != null) {
            return semanticTranslator.getFormatSuggestions();
        }
        
        // 返回基础建议
        return Arrays.asList(
            "标准格式：[棋子][起始位置][动作][目标位置]",
            "示例：红马二进三、炮8平5、车九进一",
            "动作词：进、退、平"
        );
    }
    
    /**
     * 将走法转换为标准象棋语言
     */
    private String convertToStandardChessNotation(Piece piece, Position start, Position end) {
        if (piece == null) {
            return null;
        }
        
        try {
            PieceColor color = piece.getColor();
            
            // 获取棋子名称
            String pieceName = piece.getChineseName();
            
            // 计算起始和结束位置的坐标
            int startFile = start.getY(); // 纵线（列，0-8）
            int startRank = start.getX(); // 横线（行，0-9）
            int endFile = end.getY();
            int endRank = end.getX();
            
            // 转换为象棋坐标系统
            String startPos, endPos;
            if (color == PieceColor.RED) {
                // 红方：纵线用中文数字，从右到左为一到九
                startPos = getRedFileNotation(startFile);
                endPos = getRedFileNotation(endFile);
            } else {
                // 黑方：纵线用阿拉伯数字，从左到右为1到9
                startPos = getBlackFileNotation(startFile);
                endPos = getBlackFileNotation(endFile);
            }
            
            // 判断移动方向
            String direction;
            if (startFile == endFile) {
                // 纵向移动
                if (color == PieceColor.RED) {
                    // 红方在下方，进是向上（行号减小），退是向下（行号增大）
                    direction = (endRank < startRank) ? "进" : "退";
                } else {
                    // 黑方在上方，进是向下（行号增大），退是向上（行号减小）
                    direction = (endRank > startRank) ? "进" : "退";
                }
                
                // 对于斜行棋子（马、士、象），数字表示落点所在纵线
                if (piece.getChineseName().contains("马") || piece.getChineseName().contains("士") || piece.getChineseName().contains("象")) {
                    return pieceName + startPos + direction + endPos;
                } else {
                    // 对于直行棋子（车、炮、兵、帅/将），直行时数字代表步数
                    int steps = Math.abs(endRank - startRank);
                    String stepNotation = getStepNotation(steps, color);
                    return pieceName + startPos + direction + stepNotation;
                }
            } else if (startRank == endRank) {
                // 横向移动（平移）
                direction = "平";
                // 对于直行棋子，横行时数字代表目标纵线
                return pieceName + startPos + direction + endPos;
            } else {
                // 斜向移动（马、士、象的斜向移动）
                if (color == PieceColor.RED) {
                    direction = (endRank < startRank) ? "进" : "退";
                } else {
                    direction = (endRank > startRank) ? "进" : "退";
                }
                return pieceName + startPos + direction + endPos;
            }
            
        } catch (Exception e) {
            // 转换失败，返回null使用备用格式
            return null;
        }
    }
    
    /**
     * 获取红方纵线表示法
     * 红方纵线：从右到左为一到九（file=0对应九，file=8对应一）
     */
    private String getRedFileNotation(int file) {
        String[] redFiles = {"九", "八", "七", "六", "五", "四", "三", "二", "一"};
        return (file >= 0 && file < redFiles.length) ? redFiles[file] : "五";
    }
    
    /**
     * 获取黑方纵线表示法
     */
    private String getBlackFileNotation(int file) {
        return String.valueOf(file + 1);
    }
    
    /**
     * 获取步数表示法
     */
    private String getStepNotation(int steps, PieceColor color) {
        if (color == PieceColor.RED) {
            String[] redNumbers = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
            return (steps >= 0 && steps < redNumbers.length) ? redNumbers[steps] : "一";
        } else {
            return String.valueOf(steps);
        }
    }
    
    /**
     * 格式化位置显示
     */
    private String formatPosition(Position pos, PieceColor color) {
        if (color == PieceColor.RED) {
            return getRedFileNotation(pos.getY()) + (10 - pos.getX());
        } else {
            return getBlackFileNotation(pos.getY()) + (pos.getX() + 1);
        }
    }
    
    /**
     * 生成战术建议
     */
    private String generateTacticalAdvice(String analysisInfo, List<String> candidateMoves, Board board, PieceColor playerColor) {
        StringBuilder advice = new StringBuilder();
        
        // 基于引擎分析给出建议
        if (analysisInfo != null && analysisInfo.contains("cp ")) {
            try {
                String[] parts = analysisInfo.split("cp ");
                if (parts.length > 1) {
                    int centipawns = Integer.parseInt(parts[1].split(" ")[0]);
                    
                    if (playerColor == PieceColor.RED) {
                        if (centipawns > 100) {
                            advice.append("• 你目前处于优势，建议保持攻势，寻找决定性打击\n");
                        } else if (centipawns < -100) {
                            advice.append("• 目前处于劣势，建议加强防守，寻找反击机会\n");
                        } else {
                            advice.append("• 局面相对平衡，建议稳扎稳打，避免冒险\n");
                        }
                    } else {
                        if (centipawns < -100) {
                            advice.append("• 你目前处于优势，建议保持攻势，寻找决定性打击\n");
                        } else if (centipawns > 100) {
                            advice.append("• 目前处于劣势，建议加强防守，寻找反击机会\n");
                        } else {
                            advice.append("• 局面相对平衡，建议稳扎稳打，避免冒险\n");
                        }
                    }
                }
            } catch (Exception e) {
                advice.append("• 建议仔细分析局面，选择最稳妥的走法\n");
            }
        }
        
        // 通用战术建议
        advice.append("• 注意保护将帅安全，避免被将军\n");
        advice.append("• 寻找攻击对方薄弱环节的机会\n");
        advice.append("• 考虑子力配合，发挥最大战斗力\n");
        
        return advice.toString();
    }
    
    /**
     * 将国际象棋棋盘转换为FEN格式
     */
    private String convertInternationalBoardToFen(com.example.chinesechess.core.InternationalChessBoard board) {
        StringBuilder fen = new StringBuilder();
        
        // 棋盘状态（从第8行到第1行）
        for (int row = 7; row >= 0; row--) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                String piece = board.getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToFenChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row > 0) {
                fen.append('/');
            }
        }
        
        // 当前行棋方
        fen.append(' ');
        fen.append(board.isWhiteTurn() ? 'w' : 'b');
        
        // 王车易位权利（简化处理）
        fen.append(" KQkq");
        
        // 吃过路兵目标格（简化处理）
        fen.append(" -");
        
        // 半回合计数（简化处理）
        fen.append(" 0");
        
        // 全回合计数（简化处理）
        fen.append(" 1");
        
        return fen.toString();
    }
    
    /**
     * 将棋子转换为FEN字符
     */
    private char pieceToFenChar(String piece) {
        if (piece == null || piece.length() < 2) {
            return 'p';
        }
        
        char color = piece.charAt(0);
        char type = piece.charAt(1);
        
        char c;
        switch (type) {
            case com.example.chinesechess.core.InternationalChessBoard.KING: c = 'k'; break;
            case com.example.chinesechess.core.InternationalChessBoard.QUEEN: c = 'q'; break;
            case com.example.chinesechess.core.InternationalChessBoard.ROOK: c = 'r'; break;
            case com.example.chinesechess.core.InternationalChessBoard.BISHOP: c = 'b'; break;
            case com.example.chinesechess.core.InternationalChessBoard.KNIGHT: c = 'n'; break;
            case com.example.chinesechess.core.InternationalChessBoard.PAWN: c = 'p'; break;
            default: c = 'p'; break;
        }
        
        return color == com.example.chinesechess.core.InternationalChessBoard.WHITE ? 
            Character.toUpperCase(c) : c;
    }
    
    /**
     * 将UCI格式转换为国际象棋Move对象
     */
    private Move convertUciToMoveInternational(String uci, com.example.chinesechess.core.InternationalChessBoard board) {
        if (uci == null || uci.length() < 4) {
            return null;
        }
        
        try {
            // 解析UCI格式，如 "e2e4"
            int fromCol = uci.charAt(0) - 'a';
            int fromRow = uci.charAt(1) - '1';
            int toCol = uci.charAt(2) - 'a';
            int toRow = uci.charAt(3) - '1';
            
            // 验证坐标范围
            if (fromCol < 0 || fromCol > 7 || fromRow < 0 || fromRow > 7 ||
                toCol < 0 || toCol > 7 || toRow < 0 || toRow > 7) {
                return null;
            }
            
            com.example.chinesechess.core.Position start = new com.example.chinesechess.core.Position(fromRow, fromCol);
            com.example.chinesechess.core.Position end = new com.example.chinesechess.core.Position(toRow, toCol);
            
            Move move = new Move(start, end);
            
            // 验证走法是否合法
            if (isValidMoveInternational(move, board)) {
                return move;
            }
        } catch (Exception e) {
            System.err.println("❌ 国际象棋UCI转换失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 验证国际象棋走法是否合法
     */
    private boolean isValidMoveInternational(Move move, com.example.chinesechess.core.InternationalChessBoard board) {
        try {
            com.example.chinesechess.core.Position start = move.getStart();
            com.example.chinesechess.core.Position end = move.getEnd();
            
            String piece = board.getPiece(start.getX(), start.getY());
            if (piece == null) {
                return false;
            }
            
            // 检查是否是当前玩家的棋子
            char pieceColor = piece.charAt(0);
            boolean isWhitePiece = (pieceColor == com.example.chinesechess.core.InternationalChessBoard.WHITE);
            if (isWhitePiece != board.isWhiteTurn()) {
                return false;
            }
            
            // 使用棋盘的isValidMove方法验证
            return board.isValidMove(start.getX(), start.getY(), end.getX(), end.getY());
        } catch (Exception e) {
            return false;
        }
    }
}