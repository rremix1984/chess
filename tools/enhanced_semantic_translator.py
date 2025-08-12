#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
增强版中文象棋语义翻译器
集成python-chinese-chess库，提供更准确的记谱解析和标准象棋术语支持
"""

import sys
import json
import re
from typing import Dict, List, Optional, Any, Tuple
import os

# 添加本地cchess模块路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'python-chinese-chess'))

try:
    import cchess
except ImportError:
    print("错误: 无法导入 cchess 库")
    print("请确保 python-chinese-chess 目录存在")
    sys.exit(1)

class EnhancedChineseChessSemanticTranslator:
    """增强版中文象棋语义翻译器"""
    
    def __init__(self):
        # 初始化棋盘
        self.board = cchess.Board()
        
        # 标准象棋术语词典
        self.chess_terms = {
            # 开局术语
            '开局': 'opening',
            '布局': 'setup',
            '起手': 'opening_move',
            
            # 中局术语
            '中局': 'middlegame',
            '攻击': 'attack',
            '防守': 'defense',
            '反击': 'counterattack',
            '牵制': 'pin',
            '闪击': 'fork',
            '双击': 'double_attack',
            '串击': 'skewer',
            
            # 残局术语
            '残局': 'endgame',
            '杀法': 'mating_pattern',
            '胜势': 'winning_position',
            '和势': 'drawn_position',
            '败势': 'losing_position',
            
            # 战术术语
            '将军': 'check',
            '将死': 'checkmate',
            '困毙': 'stalemate',
            '和棋': 'draw',
            '弃子': 'sacrifice',
            '兑子': 'exchange',
            '捉子': 'attack_piece',
            '护子': 'defend_piece',
            
            # 棋子价值
            '子力': 'material',
            '优势': 'advantage',
            '劣势': 'disadvantage',
            '均势': 'equal_position',
            
            # 位置术语
            '控制': 'control',
            '占据': 'occupy',
            '威胁': 'threaten',
            '保护': 'protect',
            '封锁': 'blockade',
            '突破': 'breakthrough',
            
            # 特殊术语
            '长将': 'perpetual_check',
            '长捉': 'perpetual_chase',
            '闲着': 'idle_move',
            '禁着': 'forbidden_move'
        }
        
        # 棋子价值评估
        self.piece_values = {
            'P': 1, 'p': 1,  # 兵/卒
            'C': 4.5, 'c': 4.5,  # 炮/砲
            'R': 9, 'r': 9,  # 车/車
            'N': 4, 'n': 4,  # 马/馬
            'B': 2, 'b': 2,  # 相/象
            'A': 2, 'a': 2,  # 仕/士
            'K': 1000, 'k': 1000  # 帅/将
        }
        
        # 位置修饰词
        self.position_modifiers = {
            '前': 'front',
            '后': 'back', 
            '中': 'middle',
            '左': 'left',
            '右': 'right',
            '二': 'second',
            '三': 'third',
            '四': 'fourth',
            '五': 'fifth'
        }
    
    def parse_notation_with_board(self, notation: str, board_fen: Optional[str] = None) -> Optional[Dict]:
        """使用棋盘状态解析记谱"""
        print(f"🎯 [Python-SemanticTranslator] parse_notation_with_board() 被调用")
        print(f"📝 [Python-SemanticTranslator] 输入记谱: '{notation}'")
        print(f"🏁 [Python-SemanticTranslator] 棋盘FEN: {board_fen if board_fen else '初始局面'}")
        
        try:
            # 设置棋盘状态
            print(f"🔧 [Python-SemanticTranslator] 正在设置棋盘状态...")
            if board_fen:
                self.board = cchess.Board(board_fen)
                print(f"✅ [Python-SemanticTranslator] 已加载自定义棋盘状态")
            else:
                self.board = cchess.Board()  # 使用初始局面
                print(f"✅ [Python-SemanticTranslator] 已加载初始棋盘状态")
            
            # 🔍 关键日志：这里直接调用了python-chinese-chess库！
            print(f"🐍 [python-chinese-chess] 正在调用 python-chinese-chess 库解析记谱！")
            print(f"🐍 [python-chinese-chess] 调用方法: self.board.parse_notation('{notation}')")
            move = self.board.parse_notation(notation)
            print(f"🐍 [python-chinese-chess] 解析结果: {move}")
            if move:
                print(f"🐍 [python-chinese-chess] 解析成功！")
                
                # 获取详细信息
                uci_move = move.uci()
                from_square = move.from_square
                to_square = move.to_square
                print(f"📍 [Python-SemanticTranslator] UCI走法: {uci_move}")
                print(f"📍 [Python-SemanticTranslator] 起始位置: {cchess.square_name(from_square)}")
                print(f"📍 [Python-SemanticTranslator] 目标位置: {cchess.square_name(to_square)}")
                
                # 获取移动的棋子
                piece = self.board.piece_at(from_square)
                captured_piece = self.board.piece_at(to_square)
                print(f"♟️ [Python-SemanticTranslator] 移动棋子: {piece.symbol() if piece else 'None'}")
                print(f"♟️ [Python-SemanticTranslator] 被吃棋子: {captured_piece.symbol() if captured_piece else 'None'}")
                
                # 生成语义描述
                print(f"🔍 [Python-SemanticTranslator] 正在生成语义描述...")
                semantic_description = self._generate_semantic_description(
                    notation, move, piece, captured_piece
                )
                print(f"📝 [Python-SemanticTranslator] 语义描述: {semantic_description}")
                
                result = {
                    'success': True,
                    'original_notation': notation,
                    'uci_move': uci_move,
                    'from_square': cchess.square_name(from_square),
                    'to_square': cchess.square_name(to_square),
                    'piece': piece.symbol() if piece else None,
                    'piece_name': self._get_piece_name(piece) if piece else None,
                    'captured_piece': captured_piece.symbol() if captured_piece else None,
                    'is_capture': captured_piece is not None,
                    'is_check': self.board.gives_check(move),
                    'semantic_description': semantic_description,
                    'move_type': self._classify_move_type(notation, move),
                    'tactical_significance': self._analyze_tactical_significance(move)
                }
                print(f"🎉 [Python-SemanticTranslator] 🔥 成功完成 python-chinese-chess 解析！")
                print(f"📊 [Python-SemanticTranslator] 返回结果: {json.dumps(result, ensure_ascii=False, indent=2)}")
                return result
            else:
                print(f"❌ [Python-SemanticTranslator] 🔥 python-chinese-chess 解析失败：无法解析记谱 '{notation}'")
                return {
                    'success': False,
                    'error': f'无法解析记谱: {notation}',
                    'original_notation': notation
                }
        except Exception as e:
            print(f"❌ [Python-SemanticTranslator] 🔥 python-chinese-chess 解析异常: {str(e)}")
            print(f"❌ [Python-SemanticTranslator] 异常类型: {type(e).__name__}")
            return {
                'success': False,
                'error': str(e),
                'original_notation': notation
            }
    
    def _generate_semantic_description(self, notation: str, move, piece, captured_piece) -> str:
        """生成语义描述"""
        descriptions = []
        
        # 基本描述
        piece_name = self._get_piece_name(piece) if piece else "棋子"
        descriptions.append(f"{piece_name}移动")
        
        # 动作类型
        if '进' in notation:
            descriptions.append("向前推进")
        elif '退' in notation:
            descriptions.append("向后撤退")
        elif '平' in notation:
            descriptions.append("横向移动")
        
        # 吃子描述
        if captured_piece:
            captured_name = self._get_piece_name(captured_piece)
            descriptions.append(f"吃掉对方{captured_name}")
        
        # 将军描述
        if self.board.gives_check(move):
            descriptions.append("形成将军")
        
        return "，".join(descriptions)
    
    def _get_piece_name(self, piece) -> str:
        """获取棋子中文名称"""
        if not piece:
            return "未知棋子"
        
        piece_names = {
            'P': '兵', 'p': '卒',
            'R': '车', 'r': '車', 
            'N': '马', 'n': '馬',
            'B': '相', 'b': '象',
            'A': '仕', 'a': '士',
            'K': '帅', 'k': '将',
            'C': '炮', 'c': '砲'
        }
        return piece_names.get(piece.symbol(), piece.symbol())
    
    def _classify_move_type(self, notation: str, move) -> str:
        """分类走法类型"""
        # 检查是否为开局常见走法
        opening_moves = ['炮二平五', '马二进三', '车一平二', '兵三进一']
        if notation in opening_moves:
            return 'opening_move'
        
        # 检查是否为攻击性走法
        if self.board.gives_check(move):
            return 'attacking_move'
        
        # 检查是否为防守走法
        if self._is_defensive_move(move):
            return 'defensive_move'
        
        return 'normal_move'
    
    def _is_defensive_move(self, move) -> bool:
        """判断是否为防守走法"""
        # 简单的防守判断逻辑
        # 可以根据需要扩展更复杂的判断
        return False
    
    def _analyze_tactical_significance(self, move) -> Dict[str, Any]:
        """分析战术意义"""
        significance = {
            'is_check': self.board.gives_check(move),
            'material_gain': 0,
            'positional_value': 'neutral'
        }
        
        # 计算子力得失
        captured_piece = self.board.piece_at(move.to_square)
        if captured_piece:
            significance['material_gain'] = self.piece_values.get(
                captured_piece.symbol(), 0
            )
        
        return significance
    
    def convert_to_uci_with_context(self, notation: str, board_fen: Optional[str] = None) -> Optional[str]:
        """将中文记谱转换为UCI格式（带上下文）"""
        result = self.parse_notation_with_board(notation, board_fen)
        if result and result['success']:
            return result['uci_move']
        return None
    
    def validate_notation_enhanced(self, notation: str) -> Dict[str, Any]:
        """增强版记谱验证"""
        try:
            # 尝试解析
            result = self.parse_notation_with_board(notation)
            
            if result and result['success']:
                return {
                    'valid': True,
                    'notation': notation,
                    'format': self._detect_notation_format(notation),
                    'parsed_result': result,
                    'suggestions': []
                }
            else:
                # 提供修正建议
                suggestions = self._generate_correction_suggestions(notation)
                return {
                    'valid': False,
                    'notation': notation,
                    'error': result.get('error', '未知错误') if result else '解析失败',
                    'suggestions': suggestions
                }
        except Exception as e:
            return {
                'valid': False,
                'notation': notation,
                'error': str(e),
                'suggestions': []
            }
    
    def _detect_notation_format(self, notation: str) -> str:
        """检测记谱格式"""
        if re.search(r'[一二三四五六七八九]', notation):
            return '中文数字格式（红方）'
        elif re.search(r'[1-9]', notation):
            return '阿拉伯数字格式（黑方）'
        else:
            return '未知格式'
    
    def _generate_correction_suggestions(self, notation: str) -> List[str]:
        """生成修正建议"""
        suggestions = []
        
        # 常见错误修正
        if '马' in notation and '進' in notation:
            suggestions.append("请使用'进'而不是'進'")
        
        if len(notation) != 4:
            suggestions.append("标准记谱应为4个字符")
        
        # 格式建议
        suggestions.extend([
            "红方使用中文数字：如'炮二平五'",
            "黑方使用阿拉伯数字：如'炮8平5'",
            "动作词：进（前进）、退（后退）、平（横移）"
        ])
        
        return suggestions
    
    def batch_translate_enhanced(self, notations: List[str], board_fen: Optional[str] = None) -> List[Dict]:
        """批量翻译（增强版）"""
        results = []
        current_board = cchess.Board(board_fen) if board_fen else cchess.Board()
        
        for notation in notations:
            try:
                # 使用当前棋盘状态解析
                move = current_board.parse_notation(notation)
                if move:
                    # 执行走法，更新棋盘状态
                    current_board.push(move)
                    
                    result = {
                        'success': True,
                        'original': notation,
                        'uci': move.uci(),
                        'board_after_move': current_board.fen()
                    }
                else:
                    result = {
                        'success': False,
                        'original': notation,
                        'error': '无法解析记谱'
                    }
            except Exception as e:
                result = {
                    'success': False,
                    'original': notation,
                    'error': str(e)
                }
            
            results.append(result)
        
        return results
    
    def get_position_analysis(self, board_fen: Optional[str] = None) -> Dict[str, Any]:
        """获取局面分析"""
        board = cchess.Board(board_fen) if board_fen else self.board
        
        analysis = {
            'fen': board.fen(),
            'turn': '红方' if board.turn == cchess.RED else '黑方',
            'is_check': board.is_check(),
            'is_checkmate': board.is_checkmate(),
            'is_stalemate': board.is_stalemate(),
            'is_game_over': board.is_game_over(),
            'material_balance': self._calculate_material_balance(board),
            'legal_moves_count': len(list(board.legal_moves))
        }
        
        # 添加游戏状态描述
        if board.is_checkmate():
            analysis['game_status'] = '将死'
        elif board.is_stalemate():
            analysis['game_status'] = '困毙'
        elif board.is_check():
            analysis['game_status'] = '将军'
        else:
            analysis['game_status'] = '正常'
        
        return analysis
    
    def _calculate_material_balance(self, board) -> Dict[str, int]:
        """计算子力平衡"""
        red_material = 0
        black_material = 0
        
        for square in cchess.SQUARES:
            piece = board.piece_at(square)
            if piece:
                value = self.piece_values.get(piece.symbol(), 0)
                if piece.color == cchess.RED:
                    red_material += value
                else:
                    black_material += value
        
        return {
            'red_material': red_material,
            'black_material': black_material,
            'balance': red_material - black_material
        }

def main():
    """主函数 - 命令行接口"""
    print(f"🚀 [Python-SemanticTranslator] 🔥 增强版中文象棋语义翻译器启动！")
    print(f"📚 [Python-SemanticTranslator] 🔥 已成功导入 python-chinese-chess 库")
    print(f"📋 [Python-SemanticTranslator] 命令行参数: {sys.argv}")
    
    if len(sys.argv) < 2:
        print("用法: python enhanced_semantic_translator.py <command> [args]")
        print("命令:")
        print("  parse <notation> [fen] - 解析记谱")
        print("  validate <notation> - 验证记谱")
        print("  batch <notations_json> [fen] - 批量处理")
        print("  analyze [fen] - 分析局面")
        return
    
    print(f"🔧 [Python-SemanticTranslator] 正在初始化语义翻译器...")
    translator = EnhancedChineseChessSemanticTranslator()
    command = sys.argv[1]
    print(f"🎯 [Python-SemanticTranslator] 执行命令: {command}")
    
    if command == 'parse':
        if len(sys.argv) < 3:
            print("错误: 需要提供记谱")
            return
        
        notation = sys.argv[2]
        board_fen = sys.argv[3] if len(sys.argv) > 3 else None
        
        result = translator.parse_notation_with_board(notation, board_fen)
        print(json.dumps(result, ensure_ascii=False, indent=2))
    
    elif command == 'validate':
        if len(sys.argv) < 3:
            print("错误: 需要提供记谱")
            return
        
        notation = sys.argv[2]
        result = translator.validate_notation_enhanced(notation)
        print(json.dumps(result, ensure_ascii=False, indent=2))
    
    elif command == 'batch':
        if len(sys.argv) < 3:
            print("错误: 需要提供记谱列表")
            return
        
        try:
            notations = json.loads(sys.argv[2])
            board_fen = sys.argv[3] if len(sys.argv) > 3 else None
            
            results = translator.batch_translate_enhanced(notations, board_fen)
            print(json.dumps(results, ensure_ascii=False, indent=2))
        except json.JSONDecodeError:
            print("错误: 无效的JSON格式")
    
    elif command == 'analyze':
        board_fen = sys.argv[2] if len(sys.argv) > 2 else None
        result = translator.get_position_analysis(board_fen)
        print(json.dumps(result, ensure_ascii=False, indent=2))
    
    else:
        print(f"未知命令: {command}")

if __name__ == '__main__':
    main()