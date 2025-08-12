package com.tankbattle;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class OllamaTest {
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "deepseek-r1:7b";
    
    public static void main(String[] args) {
        System.out.println("开始测试OLLAMA连接...");
        
        try {
            testOllamaConnection();
            System.out.println("✅ OLLAMA连接测试成功！");
        } catch (Exception e) {
            System.err.println("❌ OLLAMA连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testOllamaConnection() throws IOException {
        String testPrompt = "Hello, please respond with HELLO";
        
        // 转义特殊字符
        String escapedPrompt = testPrompt.replace("\\", "\\\\")
                                       .replace("\"", "\\\"")
                                       .replace("\n", "\\n")
                                       .replace("\r", "\\r")
                                       .replace("\t", "\\t");
        
        String requestBody = String.format(
            "{" +
            "\"model\": \"%s\"," +
            "\"prompt\": \"%s\"," +
            "\"stream\": false" +
            "}",
            MODEL_NAME, escapedPrompt);
        
        System.out.println("请求URL: " + OLLAMA_API_URL);
        System.out.println("请求体: " + requestBody);
        
        // 创建HTTP客户端
        org.apache.http.client.config.RequestConfig requestConfig = org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(10000)  // 10秒连接超时
                .setSocketTimeout(30000)   // 30秒响应超时
                .setConnectionRequestTimeout(10000)
                .build();
        
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            HttpPost httpPost = new HttpPost(OLLAMA_API_URL);
            StringEntity entity = new StringEntity(requestBody, "UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("User-Agent", "TankBattle-AI/1.0");
            
            System.out.println("发送请求...");
            
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("响应状态码: " + statusCode);
                
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.out.println("响应内容: " + responseBody);
                    
                    // 尝试解析响应
                    if (responseBody.contains("\"response\":")) {
                        int responseStart = responseBody.indexOf("\"response\":\"") + 12;
                        int responseEnd = responseBody.indexOf("\"", responseStart);
                        if (responseStart > 11 && responseEnd > responseStart) {
                            String aiResponse = responseBody.substring(responseStart, responseEnd);
                            System.out.println("AI回复: " + aiResponse);
                        }
                    }
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.err.println("错误响应: " + responseBody);
                    throw new IOException("HTTP请求失败，状态码: " + statusCode);
                }
            }
        }
    }
}
