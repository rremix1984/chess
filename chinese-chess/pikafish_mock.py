#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import time
import random

def log_debug(message):
    """调试日志，写入stderr避免干扰UCI通信"""
    print(f"[DEBUG] {message}", file=sys.stderr)

def main():
    log_debug("Pikafish模拟引擎启动")
    
    # 预定义的测试走法
    test_moves = ["b2e2", "h2e2", "c3c4", "g3g4", "a0a1", "i0i1"]
    
    try:
        while True:
            line = input().strip()
            log_debug(f"收到命令: {line}")
            
            if line == "uci":
                print("id name Pikafish Mock Engine")
                print("id author Mock Developer")
                print("option name Hash type spin default 16 min 1 max 1024")
                print("option name Threads type spin default 1 min 1 max 128")
                print("option name MultiPV type spin default 1 min 1 max 256")
                print("option name EvalFile type string default <empty>")
                print("uciok")
                
            elif line == "isready":
                print("readyok")
                
            elif line.startswith("setoption"):
                log_debug(f"设置选项: {line}")
                # 直接忽略选项设置，不回应
                
            elif line.startswith("position"):
                log_debug(f"设置局面: {line}")
                # 记录局面但不回应
                
            elif line.startswith("go"):
                log_debug(f"开始搜索: {line}")
                
                # 解析搜索参数
                parts = line.split()
                search_depth = 10  # 默认深度
                search_time = 1000  # 默认时间
                
                for i, part in enumerate(parts):
                    if part == "depth" and i + 1 < len(parts):
                        try:
                            search_depth = int(parts[i + 1])
                        except ValueError:
                            pass
                    elif part == "movetime" and i + 1 < len(parts):
                        try:
                            search_time = int(parts[i + 1])
                        except ValueError:
                            pass
                
                log_debug(f"搜索深度: {search_depth}, 搜索时间: {search_time}ms")
                
                # 模拟搜索过程，逐步增加深度
                max_depth = min(search_depth, 25)  # 限制最大深度避免过长输出
                
                for depth in range(1, max_depth + 1):
                    # 模拟每个深度的搜索
                    score = random.randint(-200, 200) + depth * 5  # 随深度变化的分数
                    nodes = depth * 1000 + random.randint(100, 900)
                    search_time_used = depth * 50 + random.randint(10, 40)
                    
                    # 选择一个测试走法
                    move = random.choice(test_moves)
                    
                    # 输出搜索信息 - 符合UCI协议格式
                    print(f"info depth {depth} seldepth {depth + 2} score cp {score} nodes {nodes} "
                          f"nps {nodes * 10} time {search_time_used} pv {move}")
                    
                    # 模拟搜索时间
                    time.sleep(0.05)  # 每层50毫秒模拟真实搜索
                    
                    # 如果到达目标深度或时间限制，提前结束
                    if depth >= search_depth or search_time_used >= search_time / 10:
                        break
                
                # 最终输出最佳走法
                best_move = random.choice(test_moves)
                print(f"bestmove {best_move}")
                log_debug(f"返回最佳走法: {best_move}")
                
            elif line == "quit":
                log_debug("引擎退出")
                break
                
            else:
                log_debug(f"忽略未知命令: {line}")
                
    except EOFError:
        log_debug("输入流结束，引擎退出")
    except Exception as e:
        log_debug(f"引擎异常: {e}")

if __name__ == "__main__":
    main()
