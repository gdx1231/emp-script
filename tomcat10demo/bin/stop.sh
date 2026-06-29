#!/bin/bash
# 停止 Tomcat 10 Embedded Server

cd "$(dirname "$0")/.."
APP_DIR=$(pwd)
PID_FILE="$APP_DIR/bin/app.pid"

# 释放端口 8080 (Tomcat)
PIDS=$(lsof -ti:8080 2>/dev/null)
if [ -n "$PIDS" ]; then
    echo "释放端口 8080 (PID: $PIDS)"
    echo "$PIDS" | xargs kill -9 2>/dev/null
fi

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        echo "停止应用 (PID: $PID)..."
        kill "$PID" 2>/dev/null
        for i in {1..10}; do
            if ! kill -0 "$PID" 2>/dev/null; then
                break
            fi
            sleep 1
        done
        kill -9 "$PID" 2>/dev/null
    fi
    rm -f "$PID_FILE"
fi

echo "已停止"
