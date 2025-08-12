#!/usr/bin/env python3
"""
模拟Pikafish引擎
用于演示DeepSeek-Pikafish混合AI的功能
"""

import sys
import time
import random

def main():
    print("id name Pikafish Mock Engine")
    print("id author Mock Developer")
    print("uciok")
    
    while True:
        try:
            line = input().strip()
            
            if line == "uci":
                print("id name Pikafish Mock Engine")
                print("id author Mock Developer")
                print("option name Threads type spin default 1 min 1 max 128")
                print("option name Hash type spin default 16 min 1 max 33554432")
                print("uciok")
                
            elif line == "isready":
                print("readyok")
                
            elif line.startswith("setoption"):
                # 忽略选项设置
                pass
                
            elif line.startswith("position"):
                # 保存局面信息
                pass
                
            elif line.startswith("go"):
                # 模拟思考过程
                time.sleep(0.1)  # 短暂思考
                
                # 解析思考时间
                think_time = 1000  # 默认1秒
                if "movetime" in line:
                    parts = line.split()
                    for i, part in enumerate(parts):
                        if part == "movetime" and i + 1 < len(parts):
                            think_time = int(parts[i + 1])
                            break
                
                # 模拟搜索过程
                depth = min(6, max(1, think_time // 500))
                for d in range(1, depth + 1):
                    score = random.randint(-100, 100)
                    pv = generate_random_move()
                    print(f"info depth {d} score cp {score} pv {pv}")
                    time.sleep(0.05)
                
                # 返回最佳走法
                best_move = generate_random_move()
                print(f"bestmove {best_move}")
                
            elif line == "quit":
                break
                
        except EOFError:
            break
        except Exception as e:
            print(f"info string Error: {e}", file=sys.stderr)

def generate_random_move():
    """生成一个随机的象棋走法（UCI格式）"""
    # 象棋棋盘是9x10，坐标从a0到i9
    files = "abcdefghi"
    ranks = "0123456789"
    
    from_file = random.choice(files)
    from_rank = random.choice(ranks)
    to_file = random.choice(files)
    to_rank = random.choice(ranks)
    
    return f"{from_file}{from_rank}{to_file}{to_rank}"

if __name__ == "__main__":
    main()