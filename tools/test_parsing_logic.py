#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
python-chinese-chess 解析逻辑测试脚本

该脚本演示了 python-chinese-chess 库的记谱解析逻辑，
包括详细的解析步骤和日志输出。
"""

import sys
import os

# 添加 python-chinese-chess 库路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'python-chinese-chess'))

try:
    import cchess
except ImportError:
    print("错误: 无法导入 cchess 库")
    print("请确保 python-chinese-chess 库已正确安装")
    sys.exit(1)

def print_separator(title):
    """打印分隔线"""
    print("\n" + "="*60)
    print(f" {title} ")
    print("="*60)

def print_board_info(board):
    """打印棋盘信息"""
    print(f"当前回合: {'红方' if board.turn else '黑方'}")
    print(f"FEN: {board.fen()}")
    print(f"是否将军: {board.is_check()}")
    print(f"合法走法数量: {len(list(board.legal_moves))}")
    print("\n棋盘状态:")
    print(board.unicode())

def test_notation_parsing():
    """测试记谱解析功能"""
    print_separator("记谱解析逻辑测试")
    
    # 创建棋盘
    board = cchess.Board()
    print("初始化棋盘:")
    print_board_info(board)
    
    # 测试用例：不同类型的记谱
    test_cases = [
        "炮二平五",  # 炮的平移
        "马二进三",  # 马的移动
        "车一平二",  # 车的平移
        "兵三进一",  # 兵的前进
        "帅五进一",  # 帅的移动
    ]
    
    for i, notation in enumerate(test_cases, 1):
        print_separator(f"测试用例 {i}: {notation}")
        
        try:
            # 解析记谱
            print(f"正在解析记谱: {notation}")
            
            # 调用解析方法
            move = board.parse_notation(notation)
            
            print(f"解析结果:")
            print(f"  - 起始位置: {cchess.square_name(move.from_square)} ({move.from_square})")
            print(f"  - 目标位置: {cchess.square_name(move.to_square)} ({move.to_square})")
            print(f"  - UCI格式: {move.uci()}")
            
            # 获取棋子信息
            piece = board.piece_at(move.from_square)
            if piece:
                print(f"  - 棋子类型: {piece.unicode_symbol()}")
                print(f"  - 棋子颜色: {'红方' if piece.color else '黑方'}")
            
            # 检查走法是否合法
            is_legal = board.is_legal(move)
            print(f"  - 是否合法: {is_legal}")
            
            if is_legal:
                # 执行走法
                board.push(move)
                print(f"  - 走法已执行")
                
                # 转换回记谱
                board.pop()  # 撤销走法
                converted_notation = board.move_to_notation(move)
                print(f"  - 转换回记谱: {converted_notation}")
                
                # 重新执行走法
                board.push(move)
                
                print("\n执行走法后的棋盘状态:")
                print_board_info(board)
            else:
                print("  - 走法不合法，跳过执行")
                
        except Exception as e:
            print(f"解析失败: {e}")
            print(f"错误类型: {type(e).__name__}")

def test_complex_notations():
    """测试复杂记谱解析"""
    print_separator("复杂记谱解析测试")
    
    # 创建特定局面来测试复杂记谱
    board = cchess.Board()
    
    # 执行一些走法来创建复杂局面
    initial_moves = ["炮二平五", "马8进7", "马二进三", "车9平8"]
    
    print("设置测试局面:")
    for move_notation in initial_moves:
        try:
            move = board.parse_notation(move_notation)
            board.push(move)
            print(f"  执行: {move_notation}")
        except Exception as e:
            print(f"  执行 {move_notation} 失败: {e}")
    
    print("\n当前局面:")
    print_board_info(board)
    
    # 测试复杂记谱
    complex_cases = [
        "前马进七",  # 前后马的区分
        "后炮平四",  # 前后炮的区分
    ]
    
    for notation in complex_cases:
        print_separator(f"复杂记谱测试: {notation}")
        
        try:
            move = board.parse_notation(notation)
            print(f"解析成功:")
            print(f"  - UCI: {move.uci()}")
            print(f"  - 起始位置: {cchess.square_name(move.from_square)}")
            print(f"  - 目标位置: {cchess.square_name(move.to_square)}")
            
            if board.is_legal(move):
                board.push(move)
                print(f"  - 走法已执行")
            else:
                print(f"  - 走法不合法")
                
        except Exception as e:
            print(f"解析失败: {e}")

def test_parsing_details():
    """测试解析细节"""
    print_separator("解析细节分析")
    
    board = cchess.Board()
    
    # 展示解析过程的关键数据结构
    print("关键数据结构:")
    print(f"\n1. 棋子Unicode符号映射:")
    for symbol, unicode_char in cchess.UNICODE_PIECE_SYMBOLS.items():
        print(f"   {symbol} -> {unicode_char}")
    
    print(f"\n2. 坐标转换映射:")
    print(f"   红方数字: {dict(zip(range(9), cchess.CHINESE_NUMBERS))}")
    print(f"   黑方数字: {dict(zip(range(9), cchess.ARABIC_NUMBERS))}")
    
    print(f"\n3. 方向记号:")
    print(f"   红方: 进={cchess.TRADITIONAL_VERTICAL_DIRECTION[1][False]}, 退={cchess.TRADITIONAL_VERTICAL_DIRECTION[1][True]}")
    print(f"   黑方: 进={cchess.TRADITIONAL_VERTICAL_DIRECTION[0][False]}, 退={cchess.TRADITIONAL_VERTICAL_DIRECTION[0][True]}")
    
    print(f"\n4. 士象特殊走法映射 (部分):")
    for traditional, modern in list(cchess.ADVISOR_BISHOP_MOVES_TRADITIONAL_TO_MODERN.items())[:5]:
        print(f"   {traditional} -> {modern}")
    print("   ...")
    
    # 演示具体解析步骤
    notation = "炮二平五"
    print(f"\n解析步骤演示: {notation}")
    print(f"1. 记谱长度检查: {len(notation)} == 4 ✓")
    print(f"2. 字符转换: {notation} (无需转换)")
    print(f"3. 棋子记号: {notation[:2]}")
    print(f"4. 方向移动记号: {notation[2:]}")
    print(f"5. 棋子识别: {notation[0]} -> 炮")
    print(f"6. 列位置: {notation[1]} -> 二")
    print(f"7. 移动方向: {notation[2]} -> 平")
    print(f"8. 目标位置: {notation[3]} -> 五")

def main():
    """主函数"""
    print("python-chinese-chess 解析逻辑详细测试")
    print("作者: AI Assistant")
    print("版本: 1.0")
    
    try:
        # 基础解析测试
        test_notation_parsing()
        
        # 复杂记谱测试
        test_complex_notations()
        
        # 解析细节分析
        test_parsing_details()
        
        print_separator("测试完成")
        print("所有测试已完成！")
        print("\n关键解析函数位置:")
        print("- parse_notation(): 主解析函数 (第1992行)")
        print("- move_to_notation(): 走法转记谱 (第2090行)")
        print("- get_unique_piece_square(): 唯一棋子定位 (第2295行)")
        print("- get_double_piece_square(): 双棋子定位 (第2303行)")
        print("- get_multiply_pawn_square(): 多兵定位 (第2319行)")
        
    except Exception as e:
        print(f"\n测试过程中发生错误: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()