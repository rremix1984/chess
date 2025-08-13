#!/bin/bash

echo "=== KataGo 配置验证 ==="
echo "1. 检查 KataGo 可执行文件:"
which katago
katago version

echo ""
echo "2. 检查模型文件:"
ls -la ~/.katago/models/model.bin.gz

echo ""
echo "3. 检查配置文件:"
ls -la ~/.katago/configs/gtp_example.cfg

echo ""
echo "4. 测试 KataGo 启动 (3秒后自动退出):"
(echo "quit" | timeout 3 katago gtp -model ~/.katago/models/model.bin.gz -config ~/.katago/configs/gtp_example.cfg) 2>&1 | head -5

echo ""
echo "=== 测试完成 ==="
