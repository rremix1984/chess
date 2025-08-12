#!/bin/bash

# 坦克大战游戏编译脚本

echo "正在编译坦克大战游戏..."

# 创建输出目录和lib目录
mkdir -p bin
mkdir -p lib

# 下载依赖库
JSON_JAR="lib/json-20230227.jar"
HTTPCLIENT_JAR="lib/httpclient-4.5.14.jar"
HTTPCORE_JAR="lib/httpcore-4.4.16.jar"
COMMONS_LOGGING_JAR="lib/commons-logging-1.2.jar"
COMMONS_CODEC_JAR="lib/commons-codec-1.15.jar"

# 下载JSON库
if [ ! -f "$JSON_JAR" ]; then
    echo "正在下载JSON库..."
    curl -L -o "$JSON_JAR" "https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar"
fi

# 下载Apache HttpClient库
if [ ! -f "$HTTPCLIENT_JAR" ]; then
    echo "正在下载Apache HttpClient库..."
    curl -L -o "$HTTPCLIENT_JAR" "https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.14/httpclient-4.5.14.jar"
fi

if [ ! -f "$HTTPCORE_JAR" ]; then
    echo "正在下载Apache HttpCore库..."
    curl -L -o "$HTTPCORE_JAR" "https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.16/httpcore-4.4.16.jar"
fi

if [ ! -f "$COMMONS_LOGGING_JAR" ]; then
    echo "正在下载Commons Logging库..."
    curl -L -o "$COMMONS_LOGGING_JAR" "https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"
fi

if [ ! -f "$COMMONS_CODEC_JAR" ]; then
    echo "正在下载Commons Codec库..."
    curl -L -o "$COMMONS_CODEC_JAR" "https://repo1.maven.org/maven2/commons-codec/commons-codec/1.15/commons-codec-1.15.jar"
fi

# 构建classpath
CLASSPATH="src/main/java"
if [ -f "$JSON_JAR" ]; then
    CLASSPATH="$CLASSPATH:$JSON_JAR"
fi
if [ -f "$HTTPCLIENT_JAR" ]; then
    CLASSPATH="$CLASSPATH:$HTTPCLIENT_JAR"
fi
if [ -f "$HTTPCORE_JAR" ]; then
    CLASSPATH="$CLASSPATH:$HTTPCORE_JAR"
fi
if [ -f "$COMMONS_LOGGING_JAR" ]; then
    CLASSPATH="$CLASSPATH:$COMMONS_LOGGING_JAR"
fi
if [ -f "$COMMONS_CODEC_JAR" ]; then
    CLASSPATH="$CLASSPATH:$COMMONS_CODEC_JAR"
fi

echo "使用classpath: $CLASSPATH"

# 编译Java文件
javac -d bin -cp "$CLASSPATH" src/main/java/com/tankbattle/*.java

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo ""
    echo "运行游戏："
    # 构建运行时classpath
    RUN_CLASSPATH="bin"
    for jar in "$JSON_JAR" "$HTTPCLIENT_JAR" "$HTTPCORE_JAR" "$COMMONS_LOGGING_JAR" "$COMMONS_CODEC_JAR"; do
        if [ -f "$jar" ]; then
            RUN_CLASSPATH="$RUN_CLASSPATH:$jar"
        fi
    done
    echo "java -cp '$RUN_CLASSPATH' com.tankbattle.TankBattleGame"
    echo ""
    echo "或者直接运行："
    echo "./run.sh"
else
    echo "编译失败！"
    exit 1
fi
