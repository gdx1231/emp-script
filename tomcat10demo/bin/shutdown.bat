@echo off
chcp 65001 >nul
REM 停止 Tomcat 10 Embedded Server

cd /d "%~dp0.."
set APP_DIR=%CD%
set PID_FILE=%APP_DIR%\bin\app.pid

REM 释放端口 8080 (Tomcat)
for /f "tokens=5" %%a in ('netstat -ano ^| find ":8080 "') do (
    if not "%%a"=="0" (
        echo 释放端口 8080 (PID: %%a)
        taskkill /f /pid %%a >nul 2>&1
    )
)

if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    echo 停止应用 (PID: %PID%)...
    taskkill /f /pid %PID% >nul 2>&1
    del "%PID_FILE%" 2>nul
)

echo 已停止
