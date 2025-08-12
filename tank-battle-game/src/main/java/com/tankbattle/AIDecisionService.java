package com.tankbattle;

import java.util.List;
import java.util.Random;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * AI决策服务类，与OLLAMA模型进行通信
 */
public class AIDecisionService {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "deepseek-r1:7b"; // 可以根据你的模型名称修改
    private static final int CONNECTION_TIMEOUT = 10000; // 10秒连接超时
    private static final int SOCKET_TIMEOUT = 20000; // 20秒响应超时
    private boolean ollamaAvailable = true;
    private long lastOllamaCheck = 0;
    private static final long OLLAMA_CHECK_INTERVAL = 30000; // 30秒检查一次OLLAMA可用性
    
    public AIDecisionService() {
        // 使用Apache HttpClient，Java 8兼容
        checkOllamaAvailability();
    }
    
    /**
     * 获取AI决策
     */
    public AIDecision getDecision(Tank aiTank, Tank playerTank, List<Obstacle> obstacles) {
        // 检查OLLAMA可用性，避免阻塞
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastOllamaCheck > OLLAMA_CHECK_INTERVAL) {
            checkOllamaAvailability();
            lastOllamaCheck = currentTime;
        }
        
        // 如果OLLAMA不可用，直接使用默认策略
        if (!ollamaAvailable) {
            return getDefaultDecision(aiTank, playerTank);
        }
        
        try {
            String gameContext = buildGameContext(aiTank, playerTank, obstacles);
            String prompt = buildPrompt(gameContext);
            
            // 添加调试日志
            System.out.println("[AI调试] 发送给模型的完整提示词:");
            System.out.println("=== 提示词开始 ===");
            System.out.println(prompt);
            System.out.println("=== 提示词结束 ===");
            
            String response = queryOllamaWithTimeout(prompt);
            
            System.out.println("[AI调试] 模型原始响应:");
            System.out.println("=== 响应开始 ===");
            System.out.println(response);
            System.out.println("=== 响应结束 ===");
            
            AIDecision decision = parseResponse(response);
            
            System.out.println("[AI调试] 解析结果: " + decision.getActionType() + 
                             (decision.getDirection() != null ? " " + decision.getDirection() : ""));
            
            return decision;
        } catch (Exception e) {
            // 标记OLLAMA不可用
            if (ollamaAvailable) {
                System.out.println("[AI] OLLAMA服务不可用，切换到传统AI: " + e.getMessage());
                ollamaAvailable = false;
            }
            // 返回默认策略
            return getDefaultDecision(aiTank, playerTank);
        }
    }
    
    /**
     * 构建游戏上下文信息
     */
    private String buildGameContext(Tank aiTank, Tank playerTank, List<Obstacle> obstacles) {
        StringBuilder context = new StringBuilder();
        
        // AI坦克信息
        context.append("AI坦克位置: (").append(aiTank.getX()).append(", ").append(aiTank.getY()).append(")\n");
        context.append("AI坦克生命值: ").append(aiTank.getHealth()).append("\n");
        context.append("AI坦克方向: ").append(aiTank.getDirection()).append("\n");
        
        // 玩家坦克信息
        context.append("玩家坦克位置: (").append(playerTank.getX()).append(", ").append(playerTank.getY()).append(")\n");
        context.append("玩家坦克生命值: ").append(playerTank.getHealth()).append("\n");
        context.append("玩家坦克方向: ").append(playerTank.getDirection()).append("\n");
        
        // 距离信息
        double distance = Math.sqrt(
            Math.pow(aiTank.getX() - playerTank.getX(), 2) + 
            Math.pow(aiTank.getY() - playerTank.getY(), 2)
        );
        context.append("与玩家距离: ").append((int)distance).append("\n");
        
        // 附近障碍物信息（分类统计）
        int nearbySteel = 0, nearbyBrick = 0, nearbyWater = 0;
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isDestroyed()) continue;
            double obstacleDistance = Math.sqrt(
                Math.pow(aiTank.getX() - obstacle.getX(), 2) + 
                Math.pow(aiTank.getY() - obstacle.getY(), 2)
            );
            if (obstacleDistance < 150) {
                switch (obstacle.getType()) {
                    case STEEL:
                        nearbySteel++;
                        break;
                    case BRICK:
                        nearbyBrick++;
                        break;
                    case WATER:
                        nearbyWater++;
                        break;
                }
            }
        }
        context.append("附近障碍物: 钢铁").append(nearbySteel)
               .append("个, 砖块").append(nearbyBrick)
               .append("个, 水域").append(nearbyWater).append("个\n");
        
        return context.toString();
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(String gameContext) {
        return "# 坦克大战AI指令\n" +
               "\n" +
               "你是坦克大战游戏中的AI坦克控制器。\n" +
               "\n" +
               "## 游戏状态\n" +
               gameContext +
               "\n" +
               "## 任务\n" +
               "基于当前游戏状态，选择最佳行动。你的目标是攻击玩家坦克。\n" +
               "\n" +
               "## 规则\n" +
               "- 坦克不能穿越任何障碍物（钢铁、砖块、水域）\n" +
               "- 子弹可以摧毁砖块，但不能摧毁钢铁和水域\n" +
               "- 距离玩家<200像素时射击效果最佳\n" +
               "- 距离较远时需要移动接近玩家\n" +
               "\n" +
               "## 输出格式\n" +
               "你必须且只能回复以下6个指令中的一个，不要添加任何解释或其他文字：\n" +
               "\n" +
               "MOVE_UP\n" +
               "MOVE_DOWN\n" +
               "MOVE_LEFT\n" +
               "MOVE_RIGHT\n" +
               "SHOOT\n" +
               "WAIT\n" +
               "\n" +
               "## 示例\n" +
               "如果玩家在你的右边且距离较远，回复：MOVE_RIGHT\n" +
               "如果玩家在射击范围内，回复：SHOOT\n" +
               "\n" +
               "现在请基于上述游戏状态选择行动：";
    }
    
    /**
     * 检查OLLAMA可用性
     */
    private void checkOllamaAvailability() {
        try {
            // 发送一个简单的测试请求
            String testPrompt = "请回复：OK";
            String response = queryOllamaWithTimeout(testPrompt);
            if (!ollamaAvailable) {
                System.out.println("[AI] OLLAMA服务已恢复，测试响应: " + response.trim());
            }
            ollamaAvailable = true;
        } catch (Exception e) {
            if (ollamaAvailable) {
                System.out.println("[AI] OLLAMA服务检查失败: " + e.getMessage());
            }
            ollamaAvailable = false;
        }
    }
    
    /**
     * 查询OLLAMA模型（使用Apache HTTP Client）
     */
    private String queryOllamaWithTimeout(String prompt) throws Exception {
        // 创建HTTP客户端
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 配置超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
            
            // 创建POST请求
            HttpPost post = new HttpPost(OLLAMA_API_URL);
            post.setConfig(requestConfig);
            post.setHeader("Content-Type", "application/json");

            // 准备JSON请求体
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("model", MODEL_NAME);
            jsonPayload.put("prompt", prompt);
            jsonPayload.put("stream", false); // 关闭流式响应
            
            post.setEntity(new StringEntity(jsonPayload.toString(), "UTF-8"));

            // 发送请求并接收响应
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new Exception("OLLAMA HTTP请求失败，状态码: " + statusCode);
                }
                
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                
                // 解析OLLAMA响应JSON，提取response字段
                JSONObject responseJson = new JSONObject(responseBody);
                if (responseJson.has("response")) {
                    String s = responseJson.getString("response");
                    return s;
                } else {
                    throw new Exception("OLLAMA响应格式错误，缺少response字段");
                }
            }
        } catch (Exception e) {
            throw new Exception("OLLAMA查询失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析模型响应
     */
    private AIDecision parseResponse(String response) {
        String cleanResponse = response.trim().toUpperCase();
        
        // 更精确的匹配逻辑，优先匹配完整指令
        if (cleanResponse.equals("MOVE_UP") || cleanResponse.contains("MOVE_UP")) {
            return new AIDecision(AIDecision.ActionType.MOVE, Tank.Direction.UP);
        } else if (cleanResponse.equals("MOVE_DOWN") || cleanResponse.contains("MOVE_DOWN")) {
            return new AIDecision(AIDecision.ActionType.MOVE, Tank.Direction.DOWN);
        } else if (cleanResponse.equals("MOVE_LEFT") || cleanResponse.contains("MOVE_LEFT")) {
            return new AIDecision(AIDecision.ActionType.MOVE, Tank.Direction.LEFT);
        } else if (cleanResponse.equals("MOVE_RIGHT") || cleanResponse.contains("MOVE_RIGHT")) {
            return new AIDecision(AIDecision.ActionType.MOVE, Tank.Direction.RIGHT);
        } else if (cleanResponse.equals("SHOOT") || cleanResponse.contains("SHOOT")) {
            return new AIDecision(AIDecision.ActionType.SHOOT, null);
        } else if (cleanResponse.equals("WAIT") || cleanResponse.contains("WAIT")) {
            return new AIDecision(AIDecision.ActionType.WAIT, null);
        } else {
            // 如果模型响应不符合预期，输出警告并使用默认策略
            System.out.println("[AI警告] 模型响应格式不正确，使用WAIT作为默认行动");
            System.out.println("[AI警告] 原始响应: " + response);
            return new AIDecision(AIDecision.ActionType.WAIT, null);
        }
    }
    
    /**
     * 获取默认决策（当AI服务失败时使用）
     */
    private AIDecision getDefaultDecision(Tank aiTank, Tank playerTank) {
        // 简单的默认逻辑：如果距离近就射击，否则向玩家移动
        double distance = Math.sqrt(
            Math.pow(aiTank.getX() - playerTank.getX(), 2) + 
            Math.pow(aiTank.getY() - playerTank.getY(), 2)
        );
        
        if (distance < 200) {
            return new AIDecision(AIDecision.ActionType.SHOOT, null);
        } else {
            // 向玩家方向移动
            int dx = playerTank.getX() - aiTank.getX();
            int dy = playerTank.getY() - aiTank.getY();
            
            Tank.Direction direction;
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = dx > 0 ? Tank.Direction.RIGHT : Tank.Direction.LEFT;
            } else {
                direction = dy > 0 ? Tank.Direction.DOWN : Tank.Direction.UP;
            }
            
            return new AIDecision(AIDecision.ActionType.MOVE, direction);
        }
    }
    
    /**
     * AI决策结果类
     */
    public static class AIDecision {
        public enum ActionType {
            MOVE, SHOOT, WAIT
        }
        
        private final ActionType actionType;
        private final Tank.Direction direction;
        
        public AIDecision(ActionType actionType, Tank.Direction direction) {
            this.actionType = actionType;
            this.direction = direction;
        }
        
        public ActionType getActionType() {
            return actionType;
        }
        
        public Tank.Direction getDirection() {
            return direction;
        }
    }
}
