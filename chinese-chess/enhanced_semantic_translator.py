#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简化版语义翻译器 - 用于中文象棋记谱解析
这是一个基础版本，提供基本的记谱解析功能
"""
import sys
import json
import re


def parse_notation(notation):
    """
    解析中文象棋记谱
    
    Args:
        notation (str): 中文记谱，如"红马二进三"、"炮8平5"
        
    Returns:
        dict: 解析结果
    """
    try:
        # 基本的记谱解析逻辑
        # 这里提供一个简化的实现
        result = {
            "color": "红方" if "红" in notation else ("黑方" if "黑" in notation else "未知"),
            "pieceType": "未知",
            "pieceCode": "P",
            "startFile": None,
            "action": "未知", 
            "endFile": None,
            "endRank": None,
            "originalNotation": notation
        }
        
        # 简单的棋子类型识别
        if "马" in notation:
            result["pieceType"] = "马"
            result["pieceCode"] = "N"
        elif "车" in notation:
            result["pieceType"] = "车"
            result["pieceCode"] = "R"
        elif "炮" in notation:
            result["pieceType"] = "炮"
            result["pieceCode"] = "C"
        elif "兵" in notation or "卒" in notation:
            result["pieceType"] = "兵"
            result["pieceCode"] = "P"
        elif "将" in notation or "帅" in notation:
            result["pieceType"] = "将"
            result["pieceCode"] = "K"
        elif "士" in notation or "仕" in notation:
            result["pieceType"] = "士"  
            result["pieceCode"] = "A"
        elif "象" in notation or "相" in notation:
            result["pieceType"] = "象"
            result["pieceCode"] = "B"
            
        # 动作识别
        if "进" in notation:
            result["action"] = "进"
        elif "退" in notation:
            result["action"] = "退"
        elif "平" in notation:
            result["action"] = "平"
            
        return result
        
    except Exception as e:
        return {"error": f"解析失败: {str(e)}", "originalNotation": notation}


def validate_notation(notation):
    """
    验证记谱格式
    
    Args:
        notation (str): 记谱字符串
        
    Returns:
        dict: 验证结果
    """
    try:
        result = {
            "valid": False,
            "error": None,
            "suggestions": [],
            "format": "中文象棋标准记谱"
        }
        
        if not notation or len(notation.strip()) == 0:
            result["error"] = "记谱不能为空"
            result["suggestions"] = ["请输入有效的中文象棋记谱"]
            return result
            
        # 基本格式检查
        if len(notation) < 3:
            result["error"] = "记谱格式太短"
            result["suggestions"] = ["标准格式：棋子+位置+动作+目标", "如：红马二进三"]
            return result
            
        # 检查是否包含有效的棋子
        pieces = ["马", "车", "炮", "兵", "卒", "将", "帅", "士", "仕", "象", "相"]
        has_piece = any(piece in notation for piece in pieces)
        
        if not has_piece:
            result["error"] = "未识别到有效棋子"
            result["suggestions"] = ["请确保记谱包含棋子名称：马、车、炮、兵、将、士、象等"]
            return result
            
        # 检查动作
        actions = ["进", "退", "平"]
        has_action = any(action in notation for action in actions)
        
        if not has_action:
            result["error"] = "未识别到有效动作"  
            result["suggestions"] = ["请确保记谱包含动作：进、退、平"]
            return result
            
        result["valid"] = True
        return result
        
    except Exception as e:
        return {"valid": False, "error": f"验证出错: {str(e)}"}


def translate_batch(notations_json):
    """
    批量翻译记谱
    
    Args:
        notations_json (str): JSON格式的记谱列表
        
    Returns:
        list: 翻译结果列表
    """
    try:
        notations = json.loads(notations_json)
        results = []
        
        for notation in notations:
            parsed = parse_notation(notation)
            result = {
                "original": notation,
                "parsed": parsed,
                "uci": f"a1a2",  # 简化的UCI格式
                "success": "error" not in parsed,
                "error": parsed.get("error")
            }
            results.append(result)
            
        return results
        
    except Exception as e:
        return [{"error": f"批量翻译失败: {str(e)}"}]


def main():
    """主函数"""
    if len(sys.argv) < 3:
        print(json.dumps({"error": "参数不足"}))
        return
        
    command = sys.argv[1]
    argument = sys.argv[2]
    
    try:
        if command == "parse":
            result = parse_notation(argument)
            print(json.dumps(result, ensure_ascii=False))
            
        elif command == "validate":
            result = validate_notation(argument)  
            print(json.dumps(result, ensure_ascii=False))
            
        elif command == "batch":
            result = translate_batch(argument)
            print(json.dumps(result, ensure_ascii=False))
            
        else:
            print(json.dumps({"error": f"未知命令: {command}"}, ensure_ascii=False))
            
    except Exception as e:
        print(json.dumps({"error": f"执行出错: {str(e)}"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
