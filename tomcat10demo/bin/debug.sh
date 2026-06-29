#!/bin/bash
# 调试模式启动 Tomcat 10 Embedded Server (前台运行，不进入后台)

cd "$(dirname "$0")/.."

echo "=== 调试模式启动 ==="
echo "按 Ctrl+C 停止"
echo ""

mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.Tomcat10EmbeddedServer"
