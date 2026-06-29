@echo off
chcp 65001 >nul
REM 启动 Tomcat 10 Embedded Server (后台模式)

cd /d "%~dp0.."
set APP_DIR=%CD%
set PID_FILE=%APP_DIR%\bin\app.pid
set LOG_FILE=%APP_DIR%\bin\app.log

REM 检查是否已运行
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq %PID%" 2>nul | find "%PID%" >nul
    if not errorlevel 1 (
        echo 应用已在运行 (PID: %PID%)
        exit /b 1
    )
)

REM 检查端口占用
netstat -ano | find ":8080 " >nul 2>&1
if not errorlevel 1 (
    echo 端口 8080 被占用，请先执行 shutdown.bat
    exit /b 1
)

echo 启动 Tomcat 10 Embedded Server...
start "Tomcat10Demo" /B mvn -q exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.Tomcat10EmbeddedServer" >> "%LOG_FILE%" 2>&1

REM 等待启动
for /l %%i in (1,1,15) do (
    >nul 2>&1 curl -s -o nul http://localhost:8080/
    if not errorlevel 1 (
        echo 已启动
        exit /b 0
    )
    ping -n 2 127.0.0.1 >nul
)

echo 启动超时，请检查日志: %LOG_FILE%
exit /b 1
