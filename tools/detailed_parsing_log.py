#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
详细的 python-chinese-chess 解析逻辑日志

该脚本深入展示解析过程中的每个步骤、数据结构和内部状态变化。
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

class DetailedParsingLogger:
    """详细解析日志记录器"""
    
    def __init__(self):
        self.step_count = 0
    
    def log_step(self, description, data=None):
        """记录解析步骤"""
        self.step_count += 1
        print(f"步骤 {self.step_count}: {description}")
        if data is not None:
            if isinstance(data, dict):
                for key, value in data.items():
                    print(f"    {key}: {value}")
            else:
                print(f"    结果: {data}")
        print()
    
    def log_error(self, error):
        """记录错误"""
        print(f"❌ 错误: {error}")
        print()

def detailed_parse_notation(board, notation):
    """详细解析记谱并记录每个步骤"""
    logger = DetailedParsingLogger()
    
    print(f"🔍 开始详细解析记谱: '{notation}'")
    print("="*50)
    
    try:
        # 步骤1: 长度检查
        logger.log_step("检查记谱长度", {
            "记谱": notation,
            "长度": len(notation),
            "要求长度": 4,
            "检查结果": "通过" if len(notation) == 4 else "失败"
        })
        
        if len(notation) != 4:
            raise ValueError("记号的长度不为4")
        
        # 步骤2: 字符转换
        original_notation = notation
        notation = notation.translate(cchess.PIECE_SYMBOL_TRANSLATOR[board.turn])
        logger.log_step("字符转换 (简繁体转换)", {
            "原始记谱": original_notation,
            "转换后": notation,
            "当前回合": "红方" if board.turn else "黑方",
            "转换表": str(cchess.PIECE_SYMBOL_TRANSLATOR[board.turn])
        })
        
        # 步骤3: 检查是否为士象特殊走法
        logger.log_step("检查士象特殊走法", {
            "记谱": notation,
            "在特殊走法表中": notation in cchess.ADVISOR_BISHOP_MOVES_TRADITIONAL_TO_MODERN
        })
        
        if notation in cchess.ADVISOR_BISHOP_MOVES_TRADITIONAL_TO_MODERN:
            uci = cchess.ADVISOR_BISHOP_MOVES_TRADITIONAL_TO_MODERN[notation]
            move = cchess.Move.from_uci(uci)
            piece = board.piece_type_at(move.from_square)
            
            logger.log_step("士象特殊走法处理", {
                "UCI走法": uci,
                "起始位置": cchess.square_name(move.from_square),
                "目标位置": cchess.square_name(move.to_square),
                "棋子类型": cchess.PIECE_NAMES[piece] if piece else "无",
                "是否为士象": piece in [cchess.BISHOP, cchess.ADVISOR]
            })
            
            if piece in [cchess.BISHOP, cchess.ADVISOR]:
                return move
            else:
                raise ValueError("未找到仕(士)或相(象)")
        
        # 步骤4: 分解记谱
        piece_notation = notation[:2]
        direction_move_notation = notation[2:]
        
        logger.log_step("分解记谱", {
            "棋子记号": piece_notation,
            "方向移动记号": direction_move_notation
        })
        
        # 步骤5: 解析棋子记号
        first_char = piece_notation[0]
        second_char = piece_notation[1]
        
        logger.log_step("分析棋子记号", {
            "第一个字符": first_char,
            "第二个字符": second_char,
            "第一字符类型": get_char_type(first_char),
            "第二字符类型": get_char_type(second_char)
        })
        
        # 根据第一个字符的类型进行不同的处理
        if first_char in cchess.UNICODE_PIECE_SYMBOLS.values():
            # 直接棋子符号
            from_square = handle_direct_piece_notation(board, piece_notation, logger)
        elif first_char in ['前', '后']:
            # 前后记号
            from_square = handle_front_back_notation(board, piece_notation, logger)
        elif first_char in ['中', '二', '三', '四', '五']:
            # 多兵记号
            from_square = handle_multiple_pawn_notation(board, piece_notation, logger)
        else:
            raise ValueError(f'记号首字符错误: {first_char!r}')
        
        # 步骤6: 解析方向和移动
        direction = direction_move_notation[0]
        move_target = direction_move_notation[1]
        
        logger.log_step("解析方向和移动", {
            "方向": direction,
            "移动目标": move_target,
            "方向类型": get_direction_type(direction)
        })
        
        # 获取棋子信息
        piece = board.piece_at(from_square)
        piece_type = piece.piece_type
        color = piece.color
        
        logger.log_step("棋子信息", {
            "起始位置": cchess.square_name(from_square),
            "棋子类型": cchess.PIECE_NAMES[piece_type],
            "棋子颜色": "红方" if color else "黑方",
            "棋子符号": piece.unicode_symbol()
        })
        
        # 计算目标位置
        to_square = calculate_target_square(board, from_square, piece_type, color, direction, move_target, logger)
        
        # 创建走法
        move = cchess.Move(from_square, to_square)
        
        logger.log_step("生成最终走法", {
            "起始位置": cchess.square_name(from_square),
            "目标位置": cchess.square_name(to_square),
            "UCI格式": move.uci(),
            "是否合法": board.is_legal(move)
        })
        
        return move
        
    except Exception as e:
        logger.log_error(str(e))
        raise

def get_char_type(char):
    """获取字符类型"""
    if char in cchess.UNICODE_PIECE_SYMBOLS.values():
        return "棋子符号"
    elif char in ['前', '后']:
        return "前后位置"
    elif char in ['中', '二', '三', '四', '五']:
        return "多子位置"
    elif char in cchess.CHINESE_NUMBERS:
        return "中文数字"
    elif char in cchess.ARABIC_NUMBERS:
        return "阿拉伯数字"
    elif char in ['平', '进', '退']:
        return "方向符号"
    else:
        return "未知类型"

def get_direction_type(direction):
    """获取方向类型"""
    if direction == '平':
        return "水平移动"
    elif direction == '进':
        return "向前移动"
    elif direction == '退':
        return "向后移动"
    else:
        return "未知方向"

def handle_direct_piece_notation(board, piece_notation, logger):
    """处理直接棋子记号"""
    piece = cchess.Piece.from_unicode(piece_notation[0])
    piece_type = piece.piece_type
    color = piece.color
    from_column_notation = piece_notation[1]
    
    logger.log_step("处理直接棋子记号", {
        "棋子Unicode": piece_notation[0],
        "棋子类型": cchess.PIECE_NAMES[piece_type],
        "棋子颜色": "红方" if color else "黑方",
        "列记号": from_column_notation
    })
    
    # 验证列记号
    if from_column_notation not in cchess.COORDINATES_MODERN_TO_TRADITIONAL[color].values():
        raise ValueError(f"起始列记号错误: {from_column_notation!r}")
    
    column_index = cchess.COORDINATES_TRADITIONAL_TO_MODERN[color][from_column_notation]
    
    logger.log_step("列索引转换", {
        "列记号": from_column_notation,
        "列索引": column_index,
        "转换表": str(cchess.COORDINATES_TRADITIONAL_TO_MODERN[color])
    })
    
    from_square = cchess.get_unique_piece_square(board, piece_type, color, piece_notation[0], column_index)
    
    logger.log_step("定位唯一棋子", {
        "列索引": column_index,
        "找到位置": cchess.square_name(from_square)
    })
    
    return from_square

def handle_front_back_notation(board, piece_notation, logger):
    """处理前后记号"""
    position = piece_notation[0]  # '前' 或 '后'
    piece_char = piece_notation[1]
    
    logger.log_step("处理前后记号", {
        "位置记号": position,
        "棋子字符": piece_char
    })
    
    pawn_col = None
    
    if piece_char in ['俥', '傌', '炮', '兵', '車', '馬', '砲', '卒']:
        piece = cchess.Piece.from_unicode(piece_char)
        piece_type = piece.piece_type
        color = piece.color
        logger.log_step("识别为棋子符号", {
            "棋子类型": cchess.PIECE_NAMES[piece_type],
            "颜色": "红方" if color else "黑方"
        })
    elif piece_char in cchess.CHINESE_NUMBERS:
        piece_type = cchess.PAWN
        color = cchess.RED
        pawn_col = cchess.CHINESE_NUMBERS.index(piece_char)
        logger.log_step("识别为红方兵列号", {
            "列号": piece_char,
            "列索引": pawn_col
        })
    elif piece_char in cchess.ARABIC_NUMBERS:
        piece_type = cchess.PAWN
        color = cchess.BLACK
        pawn_col = cchess.ARABIC_NUMBERS.index(piece_char)
        logger.log_step("识别为黑方卒列号", {
            "列号": piece_char,
            "列索引": pawn_col
        })
    else:
        raise ValueError(f"棋子种类记号错误: {piece_char!r}")
    
    if piece_type != cchess.PAWN:
        rank = ['前', '后'].index(position)
        logger.log_step("处理双棋子定位", {
            "位置": position,
            "排序索引": rank
        })
        from_square = cchess.get_double_piece_square(board, piece_type, color, piece_char, rank)
    else:
        logger.log_step("处理多兵定位", {
            "位置": position,
            "兵列": pawn_col
        })
        from_square = cchess.get_multiply_pawn_square(board, color, position, pawn_column=pawn_col)
    
    return from_square

def handle_multiple_pawn_notation(board, piece_notation, logger):
    """处理多兵记号"""
    position = piece_notation[0]  # '中', '二', '三', '四', '五'
    piece_char = piece_notation[1]
    
    logger.log_step("处理多兵记号", {
        "位置记号": position,
        "棋子字符": piece_char
    })
    
    pawn_col = None
    
    if piece_char in ['兵', '卒']:
        color = piece_char == '兵'
        logger.log_step("识别兵卒类型", {
            "棋子": piece_char,
            "颜色": "红方" if color else "黑方"
        })
    elif piece_char in cchess.CHINESE_NUMBERS:
        color = cchess.RED
        pawn_col = cchess.CHINESE_NUMBERS.index(piece_char)
        logger.log_step("识别为红方兵列号", {
            "列号": piece_char,
            "列索引": pawn_col
        })
    elif piece_char in cchess.ARABIC_NUMBERS:
        color = cchess.BLACK
        pawn_col = cchess.ARABIC_NUMBERS.index(piece_char)
        logger.log_step("识别为黑方卒列号", {
            "列号": piece_char,
            "列索引": pawn_col
        })
    else:
        raise ValueError(f"棋子种类记号错误: {piece_char!r}")
    
    piece_type = cchess.PAWN
    from_square = cchess.get_multiply_pawn_square(board, color, position, pawn_column=pawn_col)
    
    logger.log_step("多兵定位结果", {
        "位置": cchess.square_name(from_square)
    })
    
    return from_square

def calculate_target_square(board, from_square, piece_type, color, direction, move_target, logger):
    """计算目标位置"""
    from_row = cchess.square_row(from_square)
    from_column = cchess.square_column(from_square)
    
    logger.log_step("当前位置信息", {
        "行": from_row,
        "列": from_column,
        "位置名称": cchess.square_name(from_square)
    })
    
    if direction == '平':
        # 水平移动
        logger.log_step("处理水平移动", {
            "允许棋子": "俥(車)、炮(砲)、兵(卒)、帥(將)",
            "当前棋子": cchess.PIECE_NAMES[piece_type]
        })
        
        if piece_type not in [cchess.ROOK, cchess.CANNON, cchess.PAWN, cchess.KING]:
            raise ValueError("只有俥(車)、炮(砲)、兵(卒)、帥(將)可以使用移动方向'平'")
        
        to_column_notation = move_target
        
        if to_column_notation not in cchess.COORDINATES_MODERN_TO_TRADITIONAL[color].values():
            raise ValueError(f"到达列记号错误: {to_column_notation!r}")
        
        to_column = cchess.COORDINATES_TRADITIONAL_TO_MODERN[color][to_column_notation]
        
        logger.log_step("水平移动计算", {
            "目标列记号": to_column_notation,
            "目标列索引": to_column,
            "起始列": from_column,
            "是否同列": from_column == to_column
        })
        
        if from_column == to_column:
            raise ValueError("使用'平'时,不能移动到同一列上。")
        
        to_square = cchess.square(to_column, from_row)
        
    elif direction in ['进', '退']:
        # 垂直移动
        move = move_target
        
        logger.log_step("处理垂直移动", {
            "方向": direction,
            "移动目标": move,
            "棋子类型": cchess.PIECE_NAMES[piece_type]
        })
        
        if piece_type in [cchess.ROOK, cchess.CANNON, cchess.PAWN, cchess.KING]:
            # 直线移动的棋子
            if color:
                if move not in cchess.CHINESE_NUMBERS:
                    raise ValueError(f"前进、后退步数错误: {move!r}")
                move = cchess.VERTICAL_MOVE_CHINESE_TO_ARABIC[move]
            else:
                if move not in cchess.ARABIC_NUMBERS:
                    raise ValueError(f"前进、后退步数错误: {move!r}")
            
            logger.log_step("直线移动计算", {
                "步数": move,
                "方向判断": f"color({color}) ^ (direction == '退')({direction == '退'}) = {color ^ (direction == '退')}"
            })
            
            if color ^ (direction == '退'):
                to_square = from_square + 9 * int(move)
            else:
                to_square = from_square - 9 * int(move)
                
        elif piece_type == cchess.KNIGHT:
            # 马的移动
            if move not in cchess.COORDINATES_MODERN_TO_TRADITIONAL[color].values():
                raise ValueError(f"到达列记号错误: {move!r}")
            
            to_column = cchess.COORDINATES_TRADITIONAL_TO_MODERN[color][move]
            
            logger.log_step("马的移动计算", {
                "目标列记号": move,
                "目标列索引": to_column
            })
            
            # 获取马的可能移动位置
            to_squares = cchess._knight_attacks(from_square, cchess.BB_EMPTY)
            
            for to_square in cchess.scan_forward(to_squares & cchess.BB_COLUMNS[to_column]):
                if color ^ (direction == '退'):
                    if to_square > from_square:
                        break
                else:
                    if to_square < from_square:
                        break
            else:
                raise ValueError(f"马的到达位置错误!")
        else:
            raise ValueError(f"未知棋子类型: {piece_type}")
    else:
        raise ValueError(f'方向记号错误: {direction!r}')
    
    logger.log_step("目标位置计算完成", {
        "目标位置": cchess.square_name(to_square),
        "目标行": cchess.square_row(to_square),
        "目标列": cchess.square_column(to_square)
    })
    
    return to_square

def main():
    """主函数"""
    print("🔍 python-chinese-chess 详细解析逻辑日志")
    print("="*60)
    
    # 创建棋盘
    board = cchess.Board()
    
    # 测试用例
    test_cases = [
        "炮二平五",
        "马二进三", 
        "车一进一",
        "兵七进一"
    ]
    
    for i, notation in enumerate(test_cases, 1):
        print(f"\n🎯 测试用例 {i}/{len(test_cases)}")
        print("="*60)
        
        try:
            move = detailed_parse_notation(board, notation)
            print(f"✅ 解析成功: {notation} -> {move.uci()}")
            
            if board.is_legal(move):
                board.push(move)
                print(f"✅ 走法已执行")
            else:
                print(f"❌ 走法不合法")
                
        except Exception as e:
            print(f"❌ 解析失败: {e}")
        
        print("\n" + "-"*60)
    
    print("\n🎉 详细解析日志测试完成！")

if __name__ == "__main__":
    main()