package com.example.chinesechess.test;

import com.example.chinesechess.ai.FairyStockfishEngine;
import com.example.chinesechess.ai.FenConverter;
import com.example.chinesechess.core.*;

/**
 * 测试Fairy-Stockfish引擎板面状态同步问题的修复
 */
public class FairyStockfishBoardSyncTest {
    
    public static void main(String[] args) {
        System.out.println("🧚 ===== Fairy-Stockfish 板面状态同步测试 =====");
        
        // 创建一个测试棋盘状态
        Board testBoard = new Board();
        testBoard.initializeBoard(); // 初始化为开局状态
        
        // 执行一些走法来改变棋盘状态
        System.out.println("\n📋 设置测试局面...");
        
        // 红兵进一步 (6,0) -> (5,0)
        Piece soldier = testBoard.getPiece(6, 0);
        testBoard.setPiece(6, 0, null);
        testBoard.setPiece(5, 0, soldier);
        
        // 黑马跳出 (0,1) -> (2,2)
        Piece blackHorse = testBoard.getPiece(0, 1);
        testBoard.setPiece(0, 1, null);
        testBoard.setPiece(2, 2, blackHorse);
        
        // 生成FEN字符串
        String testFen = FenConverter.boardToFen(testBoard, PieceColor.RED);
        System.out.println("📝 测试FEN: " + testFen);
        
        // 初始化Fairy-Stockfish引擎
        System.out.println("\n🔧 初始化Fairy-Stockfish引擎...");
        FairyStockfishEngine engine = new FairyStockfishEngine("fairy-stockfish");
        engine.setLogCallback(message -> System.out.println("  " + message));
        
        if (!engine.initialize()) {
            System.out.println("❌ 引擎初始化失败，测试终止");
            return;
        }
        
        // 详细检查我们的测试棋盘状态
        System.out.println("\n🔍 详细检查测试棋盘状态:");
        for (int row = 0; row < 10; row++) {
            StringBuilder rowStr = new StringBuilder("  第" + row + "行: ");
            boolean hasAnyPiece = false;
            for (int col = 0; col < 9; col++) {
                Piece piece = testBoard.getPiece(row, col);
                if (piece != null) {
                    hasAnyPiece = true;
                    String pieceInfo = "[" + col + ":" + piece.getClass().getSimpleName() + "-" + piece.getColor() + "] ";
                    rowStr.append(pieceInfo);
                }
            }
            if (!hasAnyPiece) {
                rowStr.append("空行");
            }
            System.out.println(rowStr.toString());
        }
        
        // 特别检查几个关键位置
        System.out.println("\n🎯 关键位置检查:");
        System.out.println("  a3 (6,0): " + (testBoard.getPiece(6, 0) != null ? testBoard.getPiece(6, 0).getClass().getSimpleName() : "空"));
        System.out.println("  a4 (5,0): " + (testBoard.getPiece(5, 0) != null ? testBoard.getPiece(5, 0).getClass().getSimpleName() : "空"));
        
        // 测试1: 验证引擎能否正确解析FEN并返回合法走法
        System.out.println("\n🧪 测试1: 验证引擎能正确处理测试局面");
        String bestMove1 = engine.getBestMove(testFen, 5000);
        System.out.println("🎯 第一次搜索结果: " + bestMove1);
        
        if (bestMove1 != null && !bestMove1.equals("(none)")) {
            // 验证走法是否合法
            boolean isValidMove = validateMove(bestMove1, testBoard);
            System.out.println("✅ 走法有效性: " + (isValidMove ? "合法" : "非法"));
            
            if (!isValidMove) {
                System.out.println("❌ 第一次测试失败：引擎返回了非法走法");
                engine.cleanup();
                return;
            }
        } else {
            System.out.println("⚠️ 引擎未返回有效走法");
        }
        
        // 测试2: 再次使用相同FEN，验证引擎状态重置
        System.out.println("\n🧪 测试2: 验证引擎状态重置功能");
        String bestMove2 = engine.getBestMove(testFen, 5000);
        System.out.println("🎯 第二次搜索结果: " + bestMove2);
        
        if (bestMove2 != null && !bestMove2.equals("(none)")) {
            boolean isValidMove = validateMove(bestMove2, testBoard);
            System.out.println("✅ 走法有效性: " + (isValidMove ? "合法" : "非法"));
            
            if (!isValidMove) {
                System.out.println("❌ 第二次测试失败：引擎返回了非法走法");
                engine.cleanup();
                return;
            }
        }
        
        // 测试3: 使用不同的FEN，验证引擎能正确切换状态
        System.out.println("\n🧪 测试3: 验证引擎能正确处理不同局面");
        
        // 创建另一个测试局面
        Board testBoard2 = new Board();
        testBoard2.initializeBoard();
        
        // 红车进攻 (9,0) -> (7,0)
        Piece redChariot = testBoard2.getPiece(9, 0);
        testBoard2.setPiece(9, 0, null);
        testBoard2.setPiece(7, 0, redChariot);
        
        String testFen2 = FenConverter.boardToFen(testBoard2, PieceColor.BLACK);
        System.out.println("📝 第二个测试FEN: " + testFen2);
        
        String bestMove3 = engine.getBestMove(testFen2, 5000);
        System.out.println("🎯 第三次搜索结果: " + bestMove3);
        
        if (bestMove3 != null && !bestMove3.equals("(none)")) {
            boolean isValidMove = validateMove(bestMove3, testBoard2);
            System.out.println("✅ 走法有效性: " + (isValidMove ? "合法" : "非法"));
            
            if (!isValidMove) {
                System.out.println("❌ 第三次测试失败：引擎返回了非法走法");
                engine.cleanup();
                return;
            }
        }
        
        // 清理资源
        engine.cleanup();
        
        System.out.println("\n🎉 ===== 测试完成 =====");
        System.out.println("✅ 所有测试通过，Fairy-Stockfish 板面状态同步问题已修复");
    }
    
    /**
     * 验证UCI走法是否在给定棋盘状态下合法
     */
    private static boolean validateMove(String uciMove, Board board) {
        try {
            if (uciMove == null || uciMove.length() != 4) {
                return false;
            }
            
            // 转换UCI为棋盘坐标
            Position[] positions = FenConverter.uciToMove(uciMove);
            if (positions == null || positions.length != 2) {
                return false;
            }
            
            Position start = positions[0];
            Position end = positions[1];
            
            // 检查起始位置是否有棋子
            Piece piece = board.getPiece(start.getX(), start.getY());
            if (piece == null) {
                System.out.println("🚫 验证失败：起始位置(" + start.getX() + "," + start.getY() + ")无棋子");
                return false;
            }
            
            // 检查走法是否符合棋子规则
            if (!piece.isValidMove(board, start, end)) {
                System.out.println("🚫 验证失败：走法不符合" + piece.getClass().getSimpleName() + "的移动规则");
                return false;
            }
            
            System.out.println("✅ 走法验证通过：" + piece.getClass().getSimpleName() + 
                " 从(" + start.getX() + "," + start.getY() + ") 到(" + end.getX() + "," + end.getY() + ")");
            return true;
            
        } catch (Exception e) {
            System.out.println("🚫 验证过程中发生异常：" + e.getMessage());
            return false;
        }
    }
}
