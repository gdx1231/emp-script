@echo off
REM EWA API 示例脚本 (Windows)
REM 演示如何使用 EWA API 进行常见操作
REM

echo ========================================
echo EWA API 示例脚本
echo ========================================
echo.

REM ==================== 示例 1: 登录 ====================
echo 示例 1: 登录获取 Token
echo 命令: call-ewa-api.bat login admin password
echo.

REM ==================== 示例 2: 获取帮助 ====================
echo 示例 2: 获取 API 帮助
echo 命令: call-ewa-api.bat help
echo.

REM ==================== 示例 3: 获取数据库表列表 ====================
echo 示例 3: 获取数据库表列表
echo 命令: call-ewa-api.bat getTables work "ADM_%" json
echo 说明: 获取以 ADM_ 开头的表
echo.

REM ==================== 示例 4: 获取表结构 ====================
echo 示例 4: 获取表结构
echo 命令: call-ewa-api.bat getTable work ADM_USER json
echo 说明: 获取 ADM_USER 表的结构信息
echo.

REM ==================== 示例 5: 获取表数据 ====================
echo 示例 5: 获取表数据 (JSON 格式)
echo 命令: call-ewa-api.bat getTableData work ADM_USER "" json
echo 说明: 获取 ADM_USER 表的前 10 条数据
echo.

REM ==================== 示例 6: 获取表数据 (CSV 格式) ====================
echo 示例 6: 获取表数据 (CSV 格式)
echo 命令: call-ewa-api.bat getTableData work ADM_USER "" csv
echo 说明: 获取 ADM_USER 表的前 10 条数据，CSV 格式输出
echo.

REM ==================== 示例 7: 带 WHERE 条件查询 ====================
echo 示例 7: 带 WHERE 条件查询
echo 命令: call-ewa-api.bat getTableData work ADM_USER "ADM_ID^<10" json
echo 说明: 查询 ADM_ID 小于 10 的记录
echo.

REM ==================== 示例 8: 获取配置项 ====================
echo 示例 8: 获取配置项
echo 命令: call-ewa-api.bat getConfItem "/meta-data/services/ser_main.xml" "SER_MAIN_CAT.T.Modify" json
echo 说明: 获取指定配置项的内容
echo.

REM ==================== 示例 9: 注销 ====================
echo 示例 9: 注销 Token
echo 命令: call-ewa-api.bat logout
echo 说明: 撤销当前 Token
echo.

echo ========================================
echo 提示: 请使用 -u 参数指定 API 地址
echo 示例: call-ewa-api.bat -u https://your-server/ewa/servletApi login admin password
echo ========================================