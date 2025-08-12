# Street Fighter Docker 容器 - 解决 macOS JavaFX 崩溃问题
FROM openjdk:11-jdk

# 安装必要的图形库和 JavaFX
RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    maven \
    xvfb \
    x11vnc \
    fluxbox \
    && rm -rf /var/lib/apt/lists/*

# 安装 JavaFX
RUN mkdir -p /opt/javafx && \
    cd /opt/javafx && \
    wget -q https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_linux-x64_bin-sdk.zip && \
    unzip -q openjfx-17.0.2_linux-x64_bin-sdk.zip && \
    rm openjfx-17.0.2_linux-x64_bin-sdk.zip

# 设置环境变量
ENV JAVAFX_HOME=/opt/javafx/javafx-sdk-17.0.2
ENV PATH_TO_FX=$JAVAFX_HOME/lib
ENV DISPLAY=:99

# 创建工作目录
WORKDIR /app

# 复制项目文件
COPY StreetFighter/ /app/

# 创建启动脚本
RUN echo '#!/bin/bash\n\
export DISPLAY=:99\n\
Xvfb :99 -screen 0 1024x768x24 &\n\
XVFB_PID=$!\n\
sleep 2\n\
fluxbox &\n\
FLUX_PID=$!\n\
sleep 2\n\
x11vnc -display :99 -nopw -listen localhost -xkb -ncache 10 -ncache_cr -forever &\n\
VNC_PID=$!\n\
echo "VNC server started on port 5900"\n\
echo "Access via VNC client: localhost:5900"\n\
echo "Starting Street Fighter..."\n\
mvn javafx:run\n\
kill $XVFB_PID $FLUX_PID $VNC_PID\n\
' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 5900

CMD ["/app/start.sh"]
