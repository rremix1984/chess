package com.github.chensilong78.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * KataGo引擎测试类
 */
public class KataGoEngineTest {
    
    private KataGoEngine engine;
    
    @BeforeEach
    public void setUp() {
        engine = new KataGoEngine();
    }
    
    @AfterEach
    public void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }
    
    @Test
    public void testKataGoInitialization() {
        // 测试KataGo初始化
        boolean initialized = engine.initialize();
        
        if (initialized) {
            assertTrue(engine.isAvailable(), "引擎应该可用");
            
            // 测试版本获取
            String version = engine.getVersion();
            assertNotNull(version, "版本信息不应为空");
            System.out.println("KataGo版本: " + version);
            
            // 测试棋盘设置
            assertTrue(engine.setBoardSize(19), "设置棋盘大小应该成功");
            assertTrue(engine.clearBoard(), "清空棋盘应该成功");
            
        } else {
            System.out.println("KataGo初始化失败，可能是因为:");\n            System.out.println("1. KataGo未正确安装");\n            System.out.println("2. 模型文件路径不正确");\n            System.out.println("3. 配置文件路径不正确");\n            \n            // 在CI环境中跳过测试\n            org.junit.jupiter.api.Assumptions.assumeTrue(false, "KataGo不可用，跳过测试");\n        }\n    }\n    \n    @Test\n    public void testBasicGameplay() {\n        boolean initialized = engine.initialize();\n        org.junit.jupiter.api.Assumptions.assumeTrue(initialized, "需要KataGo初始化成功");\n        \n        // 设置19x19棋盘\n        assertTrue(engine.setBoardSize(19));\n        assertTrue(engine.clearBoard());\n        \n        // 测试下棋\n        assertTrue(engine.playMove("black", "D4"), "黑棋下D4应该成功");\n        assertTrue(engine.playMove("white", "D16"), "白棋下D16应该成功");\n        \n        // 测试AI生成下一步\n        String move = engine.generateMove("black");\n        assertNotNull(move, "AI应该能生成下一步");\n        assertFalse(move.equals("resign"), "AI不应该认输");\n        \n        System.out.println("AI建议下一步: " + move);\n    }\n    \n    @Test\n    public void testAnalysis() {\n        boolean initialized = engine.initialize();\n        org.junit.jupiter.api.Assumptions.assumeTrue(initialized, "需要KataGo初始化成功");\n        \n        engine.setBoardSize(19);\n        engine.clearBoard();\n        \n        // 摆一个简单的局面\n        engine.playMove("black", "D4");\n        engine.playMove("white", "D16");\n        engine.playMove("black", "Q4");\n        \n        // 设置较少的访问次数以加快测试\n        engine.setVisits(100);\n        \n        // 获取胜率（如果支持的话）\n        String winrate = engine.getWinrate();\n        System.out.println("当前胜率: " + winrate);\n    }\n}
