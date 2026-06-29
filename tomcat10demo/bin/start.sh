#!/bin/bash
# 启动 Tomcat 10 Embedded Server (后台模式)

cd "$(dirname "$0")/.."
APP_DIR=$(pwd)
PID_FILE="$APP_DIR/bin/app.pid"
LOG_FILE="$APP_DIR/bin/app.log"

# 检查是否已运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        echo "应用已在运行 (PID: $PID)"
        exit 1
    fi
fi

# 检查端口占用
for PORT in 8080 11002; do
    if lsof -ti:$PORT >/dev/null 2>&1; then
        echo "端口 $PORT 被占用，请先执行 bin/stop.sh"
        exit 1
    fi
done

echo "启动 Tomcat 10 Embedded Server..."
nohup mvn -q exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.Tomcat10EmbeddedServer" >> "$LOG_FILE" 2>&1 &
PID=$!
echo $PID > "$PID_FILE"

# 等待启动
for i in {1..15}; do
    if curl -s -o /dev/null http://localhost:8080/ 2>/dev/null; then
        echo "已启动 (PID: $PID)"
        echo "日志: $LOG_FILE"
        exit 0
    fi
    sleep 1
done

echo "启动超时，请检查日志: $LOG_FILE"
tail -20 "$LOG_FILE"
exit 1
