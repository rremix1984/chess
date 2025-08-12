#!/bin/bash

# 坦克大战游戏运行脚本

echo "正在启动坦克大战游戏..."

# 检查是否已编译
if [ ! -d "bin" ] || [ ! -f "bin/com/tankbattle/TankBattleGame.class" ]; then
    echo "游戏未编译，正在编译..."
    ./compile.sh
fi

# 运行游戏
JSON_JAR="lib/json-20230227.jar"
HTTPCLIENT_JAR="lib/httpclient-4.5.14.jar"
HTTPCORE_JAR="lib/httpcore-4.4.16.jar"
COMMONS_LOGGING_JAR="lib/commons-logging-1.2.jar"
COMMONS_CODEC_JAR="lib/commons-codec-1.15.jar"

# 构建运行时classpath
RUN_CLASSPATH="bin"
for jar in "$JSON_JAR" "$HTTPCLIENT_JAR" "$HTTPCORE_JAR" "$COMMONS_LOGGING_JAR" "$COMMONS_CODEC_JAR"; do
    if [ -f "$jar" ]; then
        RUN_CLASSPATH="$RUN_CLASSPATH:$jar"
    fi
done

echo "使用classpath运行游戏: $RUN_CLASSPATH"
java -cp "$RUN_CLASSPATH" com.tankbattle.TankBattleGame
