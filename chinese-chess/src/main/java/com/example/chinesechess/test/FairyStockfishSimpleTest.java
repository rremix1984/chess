package com.example.chinesechess.test;

import com.example.chinesechess.ai.FairyStockfishEngine;
import com.example.chinesechess.ai.FenConverter;

/**
 * 简单测试诊断Fairy-Stockfish引擎FEN处理问题
 */
public class FairyStockfishSimpleTest {
    
    public static void main(String[] args) {
        System.out.println("🔬 ===== Fairy-Stockfish 简单诊断测试 =====");
        
        // 初始化引擎
        FairyStockfishEngine engine = new FairyStockfishEngine("fairy-stockfish");
        engine.setLogCallback(message -> System.out.println("  " + message));
        
        if (!engine.initialize()) {
            System.out.println("❌ 引擎初始化失败");
            return;
        }
        
        // 测试1: 使用初始局面
        System.out.println("\n🧪 测试1: 使用标准开局FEN");
        String initialFen = FenConverter.getInitialFen();
        System.out.println("📝 初始FEN: " + initialFen);
        
        String move1 = engine.getBestMove(initialFen, 3000);
        System.out.println("🎯 开局走法: " + move1);
        
        // 测试2: 使用修改的简单局面
        System.out.println("\n🧪 测试2: 使用简单修改的局面");
        // 只改变一个兵的位置
        String modifiedFen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1"; // 标准开局
        String modifiedFen2 = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/P8/2P1P1P1P/1C5C1/9/RNBAKABNR w - - 0 1"; // 红兵a3->a4
        
        System.out.println("📝 修改FEN: " + modifiedFen2);
        String move2 = engine.getBestMove(modifiedFen2, 3000);
        System.out.println("🎯 修改局面走法: " + move2);
        
        // 测试3: 手动验证引擎响应
        System.out.println("\n🧪 测试3: 对比两个局面的引擎响应");
        System.out.println("开局局面建议: " + move1);
        System.out.println("修改局面建议: " + move2);
        
        if (move1 != null && move2 != null) {
            if (move1.equals(move2)) {
                System.out.println("⚠️ 警告: 两个不同局面返回相同走法，可能FEN未正确处理");
            } else {
                System.out.println("✅ 引擎对不同局面返回不同走法，FEN处理似乎正常");
            }
        }
        
        // 测试4: 尝试另一种设置位置的方法
        System.out.println("\n🧪 测试4: 尝试不同的位置设置方法");
        // 这次尝试使用 startpos moves 而不是 fen
        
        engine.cleanup();
        System.out.println("\n🎯 ===== 诊断测试完成 =====");
    }
}
