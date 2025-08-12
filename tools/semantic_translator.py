#!/usr/bin/env python3
"""
中文象棋记谱语义翻译器
用于将中文象棋记谱转换为机器可理解的坐标格式
支持多种记谱格式的语义理解和转换
"""

import sys
import json
import re
from typing import Dict, List, Tuple, Optional

class ChineseChessSemanticTranslator:
    """中文象棋语义翻译器"""
    
    def __init__(self):
        # 棋子名称映射（标准象棋术语）
        self.piece_names = {
            '帅': 'K', '将': 'k',  # 帅/将
            '仕': 'A', '士': 'a',  # 仕/士
            '相': 'B', '象': 'b',  # 相/象
            '马': 'N', '馬': 'n',  # 马
            '车': 'R', '車': 'r',  # 车
            '炮': 'C', '砲': 'c',  # 炮/砲
            '兵': 'P', '卒': 'p'   # 兵/卒
        }
        
        # 标准象棋术语扩展
        self.piece_aliases = {
            '前': 'front', '后': 'back', '中': 'middle',  # 位置修饰词
            '左': 'left', '右': 'right'  # 方向修饰词
        }
        
        # 中文数字映射（红方使用）
        self.chinese_numbers = {
            '一': 1, '二': 2, '三': 3, '四': 4, '五': 5,
            '六': 6, '七': 7, '八': 8, '九': 9,
            '１': 1, '２': 2, '３': 3, '４': 4, '５': 5,
            '６': 6, '７': 7, '８': 8, '９': 9
        }
        
        # 阿拉伯数字映射（黑方使用）
        self.arabic_numbers = {
            '1': 1, '2': 2, '3': 3, '4': 4, '5': 5,
            '6': 6, '7': 7, '8': 8, '9': 9
        }
        
        # 动作词映射（标准术语）
        self.actions = {
            '进': 'forward',   # 向前移动
            '退': 'backward',  # 向后移动
            '平': 'horizontal', # 横向移动
            '上': 'forward',
            '下': 'backward'
        }
        
        # 标准象棋术语词典
        self.chess_terms = {
            '开局': 'opening',
            '中局': 'middlegame', 
            '残局': 'endgame',
            '将军': 'check',
            '将死': 'checkmate',
            '和棋': 'draw',
            '弃子': 'sacrifice',
            '兑子': 'exchange',
            '攻击': 'attack',
            '防守': 'defense',
            '控制': 'control',
            '占据': 'occupy'
        }
        
    def parse_chinese_notation(self, notation: str) -> Optional[Dict]:
        """解析中文记谱
        
        Args:
            notation: 中文记谱，如"红马二进三"、"炮8平5"
            
        Returns:
            解析结果字典，包含棋子、起始位置、动作、目标位置等信息
        """
        notation = notation.strip()
        
        # 移除颜色前缀
        color = None
        if notation.startswith('红'):
            color = 'red'
            notation = notation[1:]
        elif notation.startswith('黑'):
            color = 'black'
            notation = notation[1:]
            
        # 解析棋子类型
        piece_type = None
        for piece_name in self.piece_names.keys():
            if notation.startswith(piece_name):
                piece_type = piece_name
                notation = notation[len(piece_name):]
                break
                
        if not piece_type:
            return None
            
        # 解析位置和动作
        # 格式：[起始位置][动作][目标位置]
        # 例如：二进三、8平5
        
        if len(notation) < 3:
            return None
            
        start_pos_char = notation[0]
        action_char = notation[1]
        end_pos_char = notation[2]
        
        # 解析起始位置
        start_file = self._parse_position_number(start_pos_char)
        if start_file is None:
            return None
            
        # 解析动作
        action = self.actions.get(action_char)
        if not action:
            return None
            
        # 解析目标位置
        if action == 'horizontal':
            # 平移：目标位置是纵线
            end_file = self._parse_position_number(end_pos_char)
            if end_file is None:
                return None
            end_rank = None
        else:
            # 进退：目标位置是步数
            steps = self._parse_position_number(end_pos_char)
            if steps is None:
                return None
            end_file = None
            end_rank = steps
            
        return {
            'color': color,
            'piece_type': piece_type,
            'piece_code': self.piece_names.get(piece_type),
            'start_file': start_file,
            'action': action,
            'end_file': end_file,
            'end_rank': end_rank,
            'original_notation': notation
        }
        
    def _parse_position_number(self, char: str) -> Optional[int]:
        """解析位置数字（中文或阿拉伯数字）"""
        if char in self.chinese_numbers:
            return self.chinese_numbers[char]
        elif char in self.arabic_numbers:
            return self.arabic_numbers[char]
        else:
            return None
            
    def convert_to_uci(self, parsed_move: Dict, board_state: Optional[Dict] = None) -> Optional[str]:
        """将解析的走法转换为UCI格式
        
        Args:
            parsed_move: 解析的走法字典
            board_state: 当前棋盘状态（可选，用于消歧义）
            
        Returns:
            UCI格式的走法，如"h2e2"
        """
        if not parsed_move:
            return None
            
        # 这里需要根据具体的棋盘状态和走法规则来实现
        # 由于缺少完整的棋盘状态，这里提供一个简化的实现
        
        start_file = parsed_move.get('start_file')
        action = parsed_move.get('action')
        end_file = parsed_move.get('end_file')
        end_rank = parsed_move.get('end_rank')
        
        if not start_file or not action:
            return None
            
        # 转换纵线坐标（1-9 -> a-i）
        start_file_uci = chr(ord('a') + start_file - 1)
        
        if action == 'horizontal':
            if not end_file:
                return None
            end_file_uci = chr(ord('a') + end_file - 1)
            # 平移时需要知道当前行，这里使用占位符
            return f"{start_file_uci}?{end_file_uci}?"
        else:
            # 进退移动，需要计算目标行
            # 这里需要更复杂的逻辑来确定确切的UCI坐标
            return f"{start_file_uci}?{start_file_uci}?"
            
    def translate_batch(self, notations: List[str]) -> List[Dict]:
        """批量翻译记谱
        
        Args:
            notations: 中文记谱列表
            
        Returns:
            翻译结果列表
        """
        results = []
        for notation in notations:
            parsed = self.parse_chinese_notation(notation)
            if parsed:
                uci = self.convert_to_uci(parsed)
                results.append({
                    'original': notation,
                    'parsed': parsed,
                    'uci': uci,
                    'success': True
                })
            else:
                results.append({
                    'original': notation,
                    'parsed': None,
                    'uci': None,
                    'success': False,
                    'error': 'Failed to parse notation'
                })
        return results
        
    def validate_notation(self, notation: str) -> Dict:
        """验证记谱格式
        
        Args:
            notation: 中文记谱
            
        Returns:
            验证结果字典
        """
        parsed = self.parse_chinese_notation(notation)
        
        if not parsed:
            return {
                'valid': False,
                'error': 'Invalid notation format',
                'suggestions': self._get_format_suggestions(notation)
            }
            
        return {
            'valid': True,
            'parsed': parsed,
            'format': self._detect_format(notation)
        }
        
    def _get_format_suggestions(self, notation: str) -> List[str]:
        """获取格式建议"""
        suggestions = [
            "标准格式：[棋子][起始位置][动作][目标位置]",
            "示例：红马二进三、炮8平5、车九进一",
            "动作词：进、退、平",
            "位置：中文数字（红方）或阿拉伯数字（黑方）"
        ]
        return suggestions
        
    def _detect_format(self, notation: str) -> str:
        """检测记谱格式"""
        if any(char in notation for char in self.chinese_numbers.keys()):
            return "中文数字格式（红方）"
        elif any(char in notation for char in self.arabic_numbers.keys()):
            return "阿拉伯数字格式（黑方）"
        else:
            return "未知格式"

def main():
    """命令行接口"""
    if len(sys.argv) < 2:
        print("用法: python semantic_translator.py <command> [args...]")
        print("命令:")
        print("  parse <notation>     - 解析单个记谱")
        print("  batch <notations>    - 批量解析记谱（JSON格式）")
        print("  validate <notation>  - 验证记谱格式")
        return
        
    translator = ChineseChessSemanticTranslator()
    command = sys.argv[1]
    
    if command == "parse":
        if len(sys.argv) < 3:
            print("错误: 请提供记谱")
            return
        notation = sys.argv[2]
        result = translator.parse_chinese_notation(notation)
        print(json.dumps(result, ensure_ascii=False, indent=2))
        
    elif command == "batch":
        if len(sys.argv) < 3:
            print("错误: 请提供记谱列表（JSON格式）")
            return
        try:
            notations = json.loads(sys.argv[2])
            results = translator.translate_batch(notations)
            print(json.dumps(results, ensure_ascii=False, indent=2))
        except json.JSONDecodeError:
            print("错误: 无效的JSON格式")
            
    elif command == "validate":
        if len(sys.argv) < 3:
            print("错误: 请提供记谱")
            return
        notation = sys.argv[2]
        result = translator.validate_notation(notation)
        print(json.dumps(result, ensure_ascii=False, indent=2))
        
    else:
        print(f"错误: 未知命令 '{command}'")

if __name__ == "__main__":
    main()