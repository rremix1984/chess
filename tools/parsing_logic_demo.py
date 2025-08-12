#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
python-chinese-chess 解析逻辑演示
展示核心解析步骤和内部数据结构
"""

import sys
import os

# 添加 python-chinese-chess 库路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'python-chinese-chess'))

try:
    import cchess
except ImportError:
    print("错误: 无法导入 cchess 库")
    sys.exit(1)

def print_core_data_structures():
    """打印核心数据结构"""
    print("📊 核心数据结构")
    print("="*50)
    
    print("\n1. 棋子Unicode符号映射:")
    print(f"   Unicode棋子符号: {cchess.UNICODE_PIECE_SYMBOLS}")
    print(f"   Unicode到棋子符号: {cchess.UNICODE_TO_PIECE_SYMBOLS}")
    print(f"   棋子符号列表: {cchess.PIECE_SYMBOLS}")
    print(f"   棋子名称列表: {cchess.PIECE_NAMES}")
    
    print("\n2. 坐标转换映射 (红方):")
    print(f"   传统->现代: {cchess.COORDINATES_TRADITIONAL_TO_MODERN[cchess.RED]}")
    print(f"   现代->传统: {cchess.COORDINATES_MODERN_TO_TRADITIONAL[cchess.RED]}")
    
    print("\n3. 坐标转换映射 (黑方):")
    print(f"   传统->现代: {cchess.COORDINATES_TRADITIONAL_TO_MODERN[cchess.BLACK]}")
    print(f"   现代->传统: {cchess.COORDINATES_MODERN_TO_TRADITIONAL[cchess.BLACK]}")
    
    print("\n4. 方向记号:")
    print(f"   向前: {['进']}")
    print(f"   向后: {['退']}")
    print(f"   水平: {['平']}")
    
    print("\n5. 士象特殊走法映射:")
    for notation, uci in cchess.ADVISOR_BISHOP_MOVES_TRADITIONAL_TO_MODERN.items():
        print(f"   {notation} -> {uci}")

def demonstrate_parsing_steps(notation):
    """演示解析步骤"""
    print(f"\n🔍 解析记谱: '{notation}'")
    print("="*50)
    
    board = cchess.Board()
    
    try:
        # 步骤1: 记谱长度检查
        print(f"步骤1: 记谱长度检查 - 长度: {len(notation)} (要求: 4)")
        if len(notation) != 4:
            raise ValueError(f"记谱长度错误: {len(notation)}")
        
        # 步骤2: 字符转换
        original = notation
        notation = notation.translate(cchess.PIECE_SYMBOL_TRANSLATOR[board.turn])
        print(f"步骤2: 字符转换 - '{original}' -> '{notation}'")
        
        # 步骤3: 检查特殊走法
        is_special = notation in cchess.ADVISOR_BISHOP_MOVES_TRADITIONAL_TO_MODERN
        print(f"步骤3: 特殊走法检查 - {is_special}")
        
        if is_special:
            uci = cchess.ADVISOR_BISHOP_MOVES_TRADITIONAL_TO_MODERN[notation]
            print(f"特殊走法映射: {notation} -> {uci}")
            return
        
        # 步骤4: 分解记谱
        piece_notation = notation[:2]
        direction_move = notation[2:]
        print(f"步骤4: 分解记谱 - 棋子: '{piece_notation}', 方向移动: '{direction_move}'")
        
        # 步骤5: 解析棋子
        piece_char = piece_notation[0]
        column_char = piece_notation[1]
        print(f"步骤5: 解析棋子 - 棋子符号: '{piece_char}', 列符号: '{column_char}'")
        
        # 获取棋子类型和颜色
        piece_type = None
        color = None
        if piece_char in cchess.UNICODE_TO_PIECE_SYMBOLS:
            piece_symbol = cchess.UNICODE_TO_PIECE_SYMBOLS[piece_char]
            # 根据大小写判断颜色，大写为红方，小写为黑方
            color = cchess.RED if piece_symbol.isupper() else cchess.BLACK
            # 获取棋子类型
            piece_symbol_lower = piece_symbol.lower()
            if piece_symbol_lower in cchess.PIECE_SYMBOLS:
                piece_type = cchess.PIECE_SYMBOLS.index(piece_symbol_lower)
        
        if piece_type is None:
            raise ValueError(f"未知棋子符号: {piece_char}")
        
        color_name = "红方" if color == cchess.RED else "黑方"
        print(f"步骤6: 棋子识别 - 类型: {cchess.PIECE_NAMES[piece_type]}, 颜色: {color_name}")
        
        # 步骤7: 列索引转换
        if column_char in cchess.COORDINATES_TRADITIONAL_TO_MODERN[color]:
            column_index = cchess.COORDINATES_TRADITIONAL_TO_MODERN[color][column_char]
            print(f"步骤7: 列转换 - '{column_char}' -> 列索引 {column_index}")
        else:
            raise ValueError(f"无效列符号: {column_char}")
        
        # 步骤8: 解析方向和移动
        direction = direction_move[0]
        move_target = direction_move[1]
        print(f"步骤8: 方向移动 - 方向: '{direction}', 目标: '{move_target}'")
        
        # 步骤9: 调用原始解析函数
        move = board.parse_notation(original)
        print(f"步骤9: 最终结果 - UCI: {move.uci()}, 合法性: {board.is_legal(move)}")
        
        # 显示移动详情
        from_square = move.from_square
        to_square = move.to_square
        print(f"移动详情: {cchess.square_name(from_square)} -> {cchess.square_name(to_square)}")
        
    except Exception as e:
        print(f"❌ 解析失败: {e}")

def show_board_state():
    """显示棋盘状态"""
    print("\n🏁 当前棋盘状态")
    print("="*50)
    
    board = cchess.Board()
    print(f"FEN: {board.fen()}")
    print(f"当前回合: {'红方' if board.turn == cchess.RED else '黑方'}")
    print(f"合法走法数量: {len(list(board.legal_moves))}")
    
    # 显示部分合法走法
    legal_moves = list(board.legal_moves)[:10]
    print(f"\n前10个合法走法:")
    for i, move in enumerate(legal_moves, 1):
        try:
            notation = board.move_to_notation(move)
            print(f"   {i:2d}. {move.uci()} -> {notation}")
        except:
            print(f"   {i:2d}. {move.uci()} -> (转换失败)")

def main():
    """主函数"""
    print("🎯 python-chinese-chess 解析逻辑演示")
    print("="*60)
    
    # 显示核心数据结构
    print_core_data_structures()
    
    # 显示棋盘状态
    show_board_state()
    
    # 测试记谱解析
    test_cases = [
        "炮二平五",
        "马二进三", 
        "车一进一",
        "兵七进一",
        "士四进五",  # 特殊走法
        "象三进五"   # 特殊走法
    ]
    
    for notation in test_cases:
        demonstrate_parsing_steps(notation)
    
    print("\n🎉 解析逻辑演示完成！")
    print("\n💡 关键解析函数位置:")
    print("   - parse_notation: cchess/__init__.py 第2089行")
    print("   - move_to_notation: cchess/__init__.py 第2200行")
    print("   - get_unique_piece_square: cchess/__init__.py 第1980行")
    print("   - get_double_piece_square: cchess/__init__.py 第2010行")
    print("   - get_multiply_pawn_square: cchess/__init__.py 第2050行")

if __name__ == "__main__":
    main()