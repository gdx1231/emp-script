@echo off
REM 一次性构建 javax（默认）和 jakarta 两个产物
setlocal enabledelayedexpansion

set "STASH=%TEMP%\build-all-stash-%RANDOM%"
mkdir "%STASH%"

echo ===== [1/2] building javax (default) =====
call mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true %*
if %ERRORLEVEL% neq 0 (
    echo ERROR: javax build failed
    rmdir /s /q "%STASH%" 2>nul
    exit /b 1
)
copy /y target\*.jar "%STASH%" >nul

echo.
echo ===== [2/2] building jakarta (-Pjakarta) =====
call mvn clean install -Pjakarta -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true %*
if %ERRORLEVEL% neq 0 (
    echo ERROR: jakarta build failed
    rmdir /s /q "%STASH%" 2>nul
    exit /b 1
)
copy /y "%STASH%\*.jar" target >nul

rmdir /s /q "%STASH%" 2>nul

echo.
echo ===== 完成，产物： =====
dir /b target\*.jar | findstr /v /i "sources javadoc"

endlocal
