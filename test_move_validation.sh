#!/bin/bash
# 测试Pikafish走法验证修复

echo "🧪 开始测试Pikafish走法验证修复..."
echo "====================================="

# 运行游戏并捕获输出
timeout 30s java -jar ./game-launcher/target/game-launcher-1.0-SNAPSHOT.jar 2>&1 | \
while IFS= read -r line; do
    echo "$line"
    
    # 检测到"无效走法"时的处理
    if [[ "$line" == *"无效走法"* ]]; then
        echo ""
        echo "❌ 检测到无效走法问题："
        echo "$line"
        echo ""
        echo "🔧 正在分析问题原因..."
        
        # 检查是否有更多调试信息
        continue
    fi
    
    # 检测到调试信息
    if [[ "$line" == *"[调试]"* ]]; then
        echo "🔍 调试信息：$line"
        continue
    fi
    
    # 检测到验证信息
    if [[ "$line" == *"[验证]"* ]]; then
        echo "✅ 验证信息：$line"
        continue
    fi
    
    # 检测到备用方法
    if [[ "$line" == *"[备用]"* ]]; then
        echo "🔄 备用方法：$line"
        continue
    fi
    
    # 成功走法
    if [[ "$line" == *"AI移动:"* ]]; then
        echo ""
        echo "✅ 成功：$line"
        echo "🎉 测试通过！AI成功给出了有效走法"
        echo ""
        break
    fi
done

echo ""
echo "🏁 测试完成"
echo "====================================="
