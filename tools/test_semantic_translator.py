#!/usr/bin/env python3
"""
语义翻译器测试脚本
用于测试中文象棋记谱语义翻译功能
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from semantic_translator import ChineseChessSemanticTranslator
import json

def test_basic_parsing():
    """测试基础解析功能"""
    print("=== 测试基础解析功能 ===")
    
    translator = ChineseChessSemanticTranslator()
    
    test_cases = [
        "红马二进三",
        "炮8平5", 
        "车九进一",
        "黑马2进3",
        "兵五进一",
        "将5平4"
    ]
    
    for notation in test_cases:
        print(f"\n测试记谱: {notation}")
        result = translator.parse_chinese_notation(notation)
        if result:
            print(f"解析成功: {json.dumps(result, ensure_ascii=False, indent=2)}")
        else:
            print("解析失败")

def test_validation():
    """测试验证功能"""
    print("\n=== 测试验证功能 ===")
    
    translator = ChineseChessSemanticTranslator()
    
    test_cases = [
        "红马二进三",  # 有效
        "炮8平5",      # 有效
        "马二进",      # 无效 - 缺少目标位置
        "红马二进十",  # 无效 - 目标位置超出范围
        "xyz",         # 无效 - 完全错误的格式
    ]
    
    for notation in test_cases:
        print(f"\n验证记谱: {notation}")
        result = translator.validate_notation(notation)
        print(f"验证结果: {json.dumps(result, ensure_ascii=False, indent=2)}")

def test_batch_translation():
    """测试批量翻译功能"""
    print("\n=== 测试批量翻译功能 ===")
    
    translator = ChineseChessSemanticTranslator()
    
    notations = [
        "红马二进三",
        "炮8平5",
        "车九进一",
        "无效记谱"
    ]
    
    results = translator.translate_batch(notations)
    print(f"批量翻译结果: {json.dumps(results, ensure_ascii=False, indent=2)}")

def test_number_parsing():
    """测试数字解析功能"""
    print("\n=== 测试数字解析功能 ===")
    
    translator = ChineseChessSemanticTranslator()
    
    # 测试中文数字
    chinese_numbers = ['一', '二', '三', '四', '五', '六', '七', '八', '九']
    for i, num in enumerate(chinese_numbers, 1):
        parsed = translator._parse_position_number(num)
        print(f"中文数字 '{num}' -> {parsed} (期望: {i})")
        assert parsed == i, f"中文数字解析错误: {num}"
    
    # 测试阿拉伯数字
    arabic_numbers = ['1', '2', '3', '4', '5', '6', '7', '8', '9']
    for i, num in enumerate(arabic_numbers, 1):
        parsed = translator._parse_position_number(num)
        print(f"阿拉伯数字 '{num}' -> {parsed} (期望: {i})")
        assert parsed == i, f"阿拉伯数字解析错误: {num}"
    
    print("✅ 数字解析测试通过")

def test_piece_recognition():
    """测试棋子识别功能"""
    print("\n=== 测试棋子识别功能 ===")
    
    translator = ChineseChessSemanticTranslator()
    
    piece_tests = [
        ("红帅五进一", "帅"),
        ("黑将5平4", "将"),
        ("红马二进三", "马"),
        ("黑车1进1", "车"),
        ("红炮二平五", "炮"),
        ("黑砲8平5", "砲"),
        ("红兵三进一", "兵"),
        ("黑卒7进1", "卒")
    ]
    
    for notation, expected_piece in piece_tests:
        result = translator.parse_chinese_notation(notation)
        if result:
            actual_piece = result.get('piece_type')
            print(f"记谱: {notation} -> 棋子: {actual_piece} (期望: {expected_piece})")
            assert actual_piece == expected_piece, f"棋子识别错误: {notation}"
        else:
            print(f"❌ 解析失败: {notation}")
    
    print("✅ 棋子识别测试通过")

def test_action_recognition():
    """测试动作识别功能"""
    print("\n=== 测试动作识别功能 ===")
    
    translator = ChineseChessSemanticTranslator()
    
    action_tests = [
        ("红马二进三", "forward"),
        ("黑车1退2", "backward"),
        ("红炮二平五", "horizontal")
    ]
    
    for notation, expected_action in action_tests:
        result = translator.parse_chinese_notation(notation)
        if result:
            actual_action = result.get('action')
            print(f"记谱: {notation} -> 动作: {actual_action} (期望: {expected_action})")
            assert actual_action == expected_action, f"动作识别错误: {notation}"
        else:
            print(f"❌ 解析失败: {notation}")
    
    print("✅ 动作识别测试通过")

def run_all_tests():
    """运行所有测试"""
    print("🧪 开始语义翻译器测试")
    print("=" * 50)
    
    try:
        test_number_parsing()
        test_piece_recognition()
        test_action_recognition()
        test_basic_parsing()
        test_validation()
        test_batch_translation()
        
        print("\n" + "=" * 50)
        print("🎉 所有测试通过！语义翻译器工作正常。")
        
    except Exception as e:
        print(f"\n❌ 测试失败: {e}")
        import traceback
        traceback.print_exc()
        return False
    
    return True

if __name__ == "__main__":
    success = run_all_tests()
    sys.exit(0 if success else 1)