package test.java;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 测试Stockfish引擎基本功能
 */
public class TestStockfishEngine {
    public static void main(String[] args) {
        System.out.println("=== 测试Stockfish引擎 ===");
        
        try {
            // 测试1: 检查Stockfish是否可执行
            System.out.println("1. 检查Stockfish可执行性...");
            ProcessBuilder pb = new ProcessBuilder("stockfish");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
            
            // 测试2: UCI初始化
            System.out.println("2. 测试UCI协议...");
            writer.println("uci");
            writer.flush();
            
            String line;
            boolean uciOk = false;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 50) {
                System.out.println("引擎响应: " + line);
                if (line.equals("uciok")) {
                    uciOk = true;
                    break;
                }
                lineCount++;
            }
            
            if (uciOk) {
                System.out.println("✅ UCI协议初始化成功");
                
                // 测试3: 设置选项
                System.out.println("3. 测试设置选项...");
                writer.println("setoption name Skill Level value 12");
                writer.println("setoption name Threads value 1");
                writer.println("isready");
                writer.flush();
                
                boolean ready = false;
                while ((line = reader.readLine()) != null) {
                    System.out.println("引擎响应: " + line);
                    if (line.equals("readyok")) {
                        ready = true;
                        break;
                    }
                }
                
                if (ready) {
                    System.out.println("✅ 引擎设置成功");
                    
                    // 测试4: 简单的走法分析
                    System.out.println("4. 测试走法分析...");
                    writer.println("position startpos");
                    writer.println("go movetime 1000");
                    writer.flush();
                    
                    while ((line = reader.readLine()) != null) {
                        System.out.println("分析: " + line);
                        if (line.startsWith("bestmove")) {
                            System.out.println("✅ 成功获取最佳走法: " + line);
                            break;
                        }
                    }
                } else {
                    System.out.println("❌ 引擎准备失败");
                }
            } else {
                System.out.println("❌ UCI协议初始化失败");
            }
            
            // 关闭引擎
            writer.println("quit");
            writer.flush();
            
            boolean terminated = process.waitFor(5, TimeUnit.SECONDS);
            if (!terminated) {
                System.out.println("强制终止引擎进程");
                process.destroyForcibly();
            }
            
            System.out.println("测试完成");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
