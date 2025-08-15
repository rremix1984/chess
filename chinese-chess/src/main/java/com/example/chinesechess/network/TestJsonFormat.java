package com.example.chinesechess.network;

/**
 * 测试JSON格式输出
 */
public class TestJsonFormat {
    public static void main(String[] args) {
        // 测试连接请求消息
        ConnectRequestMessage request = new ConnectRequestMessage("test_client", "TestPlayer", "1.0.0");
        String json = request.toJson();
        
        System.out.println("JSON输出:");
        System.out.println(json);
        System.out.println("\n包含换行符数量: " + json.split("\n").length);
        
        // 测试解析
        try {
            NetworkMessage parsed = NetworkMessage.fromJson(json);
            System.out.println("解析成功: " + parsed.getType());
        } catch (Exception e) {
            System.out.println("解析失败: " + e.getMessage());
        }
    }
}
