package com.example.chinesechess.ai;

import com.example.chinesechess.ui.AILogPanel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 中文象棋记谱语义翻译服务
 * 通过调用Python脚本实现中文记谱的语义理解和转换
 */
public class SemanticTranslatorService {
    
    private static final String PYTHON_SCRIPT_PATH = "enhanced_semantic_translator.py";
    private static final int TIMEOUT_SECONDS = 10;
    private final Gson gson = new Gson();
    private AILogPanel aiLogPanel; // AI日志面板
    
    /**
     * 设置AI日志面板
     */
    public void setAILogPanel(AILogPanel aiLogPanel) {
        this.aiLogPanel = aiLogPanel;
    }
    
    /**
     * 添加python-chinese-chess相关的AI决策日志
     */
    private void addPythonChessLog(String message) {
        if (aiLogPanel != null && aiLogPanel.isLogEnabled()) {
            // 检查消息是否已经包含python-chinese-chess标记
            if (message.contains("[python-chinese-chess]")) {
                // 如果已经包含标记，直接输出
                aiLogPanel.addAIDecision(message);
            } else {
                // 如果没有标记，添加特殊标记
                String formattedMessage = "🐍 [python-chinese-chess] " + message;
                aiLogPanel.addAIDecision(formattedMessage);
            }
        }
        // 同时输出到控制台
        if (message.contains("[python-chinese-chess]")) {
            System.out.println(message);
        } else {
            System.out.println("🐍 [python-chinese-chess] " + message);
        }
    }
    
    /**
     * 解析结果类
     */
    public static class ParseResult {
        private String color;
        private String pieceType;
        private String pieceCode;
        private Integer startFile;
        private String action;
        private Integer endFile;
        private Integer endRank;
        private String originalNotation;
        
        // Getters and setters
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getPieceType() { return pieceType; }
        public void setPieceType(String pieceType) { this.pieceType = pieceType; }
        
        public String getPieceCode() { return pieceCode; }
        public void setPieceCode(String pieceCode) { this.pieceCode = pieceCode; }
        
        public Integer getStartFile() { return startFile; }
        public void setStartFile(Integer startFile) { this.startFile = startFile; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public Integer getEndFile() { return endFile; }
        public void setEndFile(Integer endFile) { this.endFile = endFile; }
        
        public Integer getEndRank() { return endRank; }
        public void setEndRank(Integer endRank) { this.endRank = endRank; }
        
        public String getOriginalNotation() { return originalNotation; }
        public void setOriginalNotation(String originalNotation) { this.originalNotation = originalNotation; }
        
        @Override
        public String toString() {
            return String.format("ParseResult{color='%s', pieceType='%s', action='%s', startFile=%d, endFile=%d, endRank=%d}",
                    color, pieceType, action, startFile, endFile, endRank);
        }
    }
    
    /**
     * 翻译结果类
     */
    public static class TranslationResult {
        private String original;
        private ParseResult parsed;
        private String uci;
        private boolean success;
        private String error;
        
        // Getters and setters
        public String getOriginal() { return original; }
        public void setOriginal(String original) { this.original = original; }
        
        public ParseResult getParsed() { return parsed; }
        public void setParsed(ParseResult parsed) { this.parsed = parsed; }
        
        public String getUci() { return uci; }
        public void setUci(String uci) { this.uci = uci; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String error;
        private List<String> suggestions;
        private ParseResult parsed;
        private String format;
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
        
        public ParseResult getParsed() { return parsed; }
        public void setParsed(ParseResult parsed) { this.parsed = parsed; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
    }
    
    /**
     * 解析单个中文记谱
     * 
     * @param notation 中文记谱，如"红马二进三"、"炮8平5"
     * @return 解析结果
     */
    public ParseResult parseNotation(String notation) {
        System.out.println("🎯 [SemanticTranslator] parseNotation() 被调用");
        System.out.println("📝 [SemanticTranslator] 输入记谱: '" + notation + "'");
        System.out.println("🔧 [SemanticTranslator] 即将调用python-chinese-chess库进行记谱解析...");
        
        try {
            String result = executePythonScript("parse", notation);
            if (result != null && !result.trim().equals("null")) {
                ParseResult parseResult = gson.fromJson(result, ParseResult.class);
                System.out.println("✅ [SemanticTranslator] 记谱解析成功，结果: " + parseResult.toString());
                return parseResult;
            } else {
                System.err.println("❌ [SemanticTranslator] Python脚本返回null结果");
            }
        } catch (Exception e) {
            System.err.println("❌ [SemanticTranslator] 解析记谱失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 批量翻译记谱
     * 
     * @param notations 中文记谱列表
     * @return 翻译结果列表
     */
    public List<TranslationResult> translateBatch(List<String> notations) {
        System.out.println("🎯 [SemanticTranslator] translateBatch() 被调用");
        System.out.println("📝 [SemanticTranslator] 批量翻译记谱数量: " + notations.size());
        System.out.println("📋 [SemanticTranslator] 记谱列表: " + notations.toString());
        System.out.println("🔧 [SemanticTranslator] 即将调用python-chinese-chess库进行批量翻译...");
        
        try {
            String notationsJson = gson.toJson(notations);
            String result = executePythonScript("batch", notationsJson);
            if (result != null) {
                Type listType = new TypeToken<List<TranslationResult>>(){}.getType();
                List<TranslationResult> results = gson.fromJson(result, listType);
                System.out.println("✅ [SemanticTranslator] 批量翻译成功，返回 " + results.size() + " 个结果");
                return results;
            } else {
                System.err.println("❌ [SemanticTranslator] Python脚本返回null结果");
            }
        } catch (Exception e) {
            System.err.println("❌ [SemanticTranslator] 批量翻译失败: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * 验证记谱格式
     * 
     * @param notation 中文记谱
     * @return 验证结果
     */
    public ValidationResult validateNotation(String notation) {
        System.out.println("🎯 [SemanticTranslator] validateNotation() 被调用");
        System.out.println("📝 [SemanticTranslator] 验证记谱: '" + notation + "'");
        System.out.println("🔧 [SemanticTranslator] 即将调用python-chinese-chess库进行记谱验证...");
        
        try {
            String result = executePythonScript("validate", notation);
            if (result != null) {
                ValidationResult validationResult = gson.fromJson(result, ValidationResult.class);
                System.out.println("✅ [SemanticTranslator] 记谱验证完成，有效性: " + validationResult.isValid());
                if (!validationResult.isValid() && validationResult.getError() != null) {
                    System.out.println("⚠️ [SemanticTranslator] 验证错误: " + validationResult.getError());
                }
                return validationResult;
            } else {
                System.err.println("❌ [SemanticTranslator] Python脚本返回null结果");
            }
        } catch (Exception e) {
            System.err.println("❌ [SemanticTranslator] 验证记谱失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 智能解析记谱（包含验证和解析）
     * 
     * @param notation 中文记谱
     * @return 包含验证和解析信息的结果
     */
    public Map<String, Object> smartParse(String notation) {
        addPythonChessLog("smartParse() 被调用");
        addPythonChessLog("智能解析记谱: '" + notation + "'");
        addPythonChessLog("即将进行综合解析和验证...");
        
        Map<String, Object> result = new HashMap<>();
        
        // 首先验证格式
        addPythonChessLog("第1步：验证记谱格式");
        ValidationResult validation = validateNotation(notation);
        result.put("validation", validation);
        
        if (validation != null && validation.isValid()) {
            // 如果格式有效，进行解析
            addPythonChessLog("第2步：解析记谱结构");
            ParseResult parsed = parseNotation(notation);
            result.put("parsed", parsed);
            result.put("success", parsed != null);
        } else {
            addPythonChessLog("记谱格式验证失败，跳过解析步骤");
            result.put("success", false);
        }
        
        boolean isSuccess = (Boolean) result.get("success");
        addPythonChessLog("智能解析完成，综合状态: " + (isSuccess ? "成功" : "失败"));
        
        return result;
    }
    
    /**
     * 获取记谱格式建议
     * 
     * @return 格式建议列表
     */
    public List<String> getFormatSuggestions() {
        return Arrays.asList(
            "标准格式：[棋子][起始位置][动作][目标位置]",
            "示例：红马二进三、炮8平5、车九进一",
            "动作词：进、退、平",
            "红方位置：中文数字（一到九）",
            "黑方位置：阿拉伯数字（1到9）"
        );
    }
    
    /**
     * 执行Python脚本
     * 
     * @param command 命令
     * @param argument 参数
     * @return 脚本输出结果
     */
    private String executePythonScript(String command, String argument) {
        try {
            // 构建命令
            List<String> commands = new ArrayList<>();
            commands.add("python3");
            commands.add(PYTHON_SCRIPT_PATH);
            commands.add(command);
            commands.add(argument);
            
            // 🔍 关键日志：记录调用python-chinese-chess的详细信息
            System.out.println("🚀 [SemanticTranslator] 开始调用Python语义翻译器");
            System.out.println("📋 [SemanticTranslator] 执行命令: " + String.join(" ", commands));
            System.out.println("🎯 [SemanticTranslator] 命令类型: " + command);
            System.out.println("📝 [SemanticTranslator] 输入参数: " + argument);
            System.out.println("📂 [SemanticTranslator] 工作目录: " + System.getProperty("user.dir"));
            System.out.println("🐍 [SemanticTranslator] Python脚本路径: " + PYTHON_SCRIPT_PATH);
            
            // 创建进程
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.redirectErrorStream(true);
            
            long startTime = System.currentTimeMillis();
            Process process = processBuilder.start();
            System.out.println("⚡ [SemanticTranslator] Python进程已启动");
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            System.out.println("📖 [SemanticTranslator] 开始读取Python脚本输出...");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    lineCount++;
                    // 记录前几行输出用于调试
                    if (lineCount <= 3) {
                        System.out.println("📄 [SemanticTranslator] Python输出第" + lineCount + "行: " + line);
                    }
                }
                System.out.println("📊 [SemanticTranslator] 总共读取了 " + lineCount + " 行输出");
            }
            
            // 等待进程完成
            System.out.println("⏳ [SemanticTranslator] 等待Python进程完成...");
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (!finished) {
                System.err.println("❌ [SemanticTranslator] Python脚本执行超时 (>" + TIMEOUT_SECONDS + "秒)");
                process.destroyForcibly();
                throw new RuntimeException("Python脚本执行超时");
            }
            
            int exitCode = process.exitValue();
            System.out.println("🏁 [SemanticTranslator] Python进程完成，退出码: " + exitCode + ", 执行时间: " + executionTime + "ms");
            
            if (exitCode != 0) {
                System.err.println("❌ [SemanticTranslator] Python脚本执行失败，退出码: " + exitCode);
                System.err.println("📋 [SemanticTranslator] 完整输出: " + output.toString());
                throw new RuntimeException("Python脚本执行失败，退出码: " + exitCode + ", 输出: " + output.toString());
            }
            
            String result = output.toString().trim();
            System.out.println("✅ [SemanticTranslator] Python脚本执行成功");
            System.out.println("📤 [SemanticTranslator] 返回结果长度: " + result.length() + " 字符");
            if (result.length() > 0 && result.length() <= 200) {
                System.out.println("📋 [SemanticTranslator] 返回结果: " + result);
            } else if (result.length() > 200) {
                System.out.println("📋 [SemanticTranslator] 返回结果(前200字符): " + result.substring(0, 200) + "...");
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ [SemanticTranslator] 执行Python脚本发生异常: " + e.getClass().getSimpleName());
            System.err.println("❌ [SemanticTranslator] 异常消息: " + e.getMessage());
            System.err.println("❌ [SemanticTranslator] 命令: " + command + ", 参数: " + argument);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查Python环境是否可用
     * 
     * @return 是否可用
     */
    public boolean isPythonAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "--version");
            Process process = processBuilder.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查语义翻译器脚本是否存在
     * 
     * @return 是否存在
     */
    public boolean isTranslatorScriptAvailable() {
        File scriptFile = new File(PYTHON_SCRIPT_PATH);
        return scriptFile.exists() && scriptFile.canRead();
    }
    
    /**
     * 获取服务状态
     * 
     * @return 服务状态信息
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("pythonAvailable", isPythonAvailable());
        status.put("scriptAvailable", isTranslatorScriptAvailable());
        status.put("ready", isPythonAvailable() && isTranslatorScriptAvailable());
        return status;
    }
}