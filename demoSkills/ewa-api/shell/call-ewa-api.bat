@echo off
REM EWA API 调用脚本
REM 支持三种认证模式：JWT Token、HMAC签名、简单Token
REM
REM 用法: call-ewa-api.bat [options] <method> [params...]
REM
REM 示例:
REM   call-ewa-api.bat login
REM   call-ewa-api.bat getConfXml ewa/m
REM   call-ewa-api.bat getConfItem ewa/m ChangePWD
REM   call-ewa-api.bat --hmac getConfXml ewa/m

setlocal enabledelayedexpansion

REM ==================== 配置区域 ====================
if not defined EWA_API_URL set API_BASE_URL=http://localhost:8080/ewa/servletApi
if defined EWA_API_URL set API_BASE_URL=%EWA_API_URL%

if not defined EWA_API_LOGIN_ID set API_LOGIN_ID=admin
if defined EWA_API_LOGIN_ID set API_LOGIN_ID=%EWA_API_LOGIN_ID%

if defined EWA_API_PASSWORD set API_PASSWORD=%EWA_API_PASSWORD%

if not defined EWA_TOKEN_FILE set TOKEN_FILE=%TEMP%\ewa_api_token.txt
if defined EWA_TOKEN_FILE set TOKEN_FILE=%EWA_TOKEN_FILE%

if not defined EWA_AUTH_MODE set AUTH_MODE=token
if defined EWA_AUTH_MODE set AUTH_MODE=%EWA_AUTH_MODE%
REM =================================================

set VERBOSE=false
set VERSION=1.0.0

REM 解析参数
:parse_args
if "%~1"=="" goto :check_command
if /i "%~1"=="-h" goto :show_help
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-v" goto :set_verbose
if /i "%~1"=="--verbose" goto :set_verbose
if /i "%~1"=="-u" goto :set_url
if /i "%~1"=="--url" goto :set_url
if /i "%~1"=="-l" goto :set_login
if /i "%~1"=="--login" goto :set_login
if /i "%~1"=="-p" goto :set_password
if /i "%~1"=="--password" goto :set_password
if /i "%~1"=="-m" goto :set_mode
if /i "%~1"=="--mode" goto :set_mode
if /i "%~1"=="--hmac" goto :set_hmac
if /i "%~1"=="--simple" goto :set_simple
if /i "%~1"=="login" goto :do_login
if /i "%~1"=="logout" goto :do_logout
if /i "%~1"=="getConfXml" goto :do_get_conf_xml
if /i "%~1"=="getConfItem" goto :do_get_conf_item
if /i "%~1"=="runConfItem" goto :do_run_conf_item
if /i "%~1"=="updateConfItem" goto :do_update_conf_item
if /i "%~1"=="deleteConfItem" goto :do_delete_conf_item
if /i "%~1"=="getTables" goto :do_get_tables
if /i "%~1"=="getTable" goto :do_get_table
if /i "%~1"=="getTableData" goto :do_get_table_data
if /i "%~1"=="help" goto :do_help

echo [ERROR] 未知选项或命令: %~1
goto :show_help

:set_verbose
set VERBOSE=true
shift
goto :parse_args

:set_url
set API_BASE_URL=%~2
shift
shift
goto :parse_args

:set_login
set API_LOGIN_ID=%~2
shift
shift
goto :parse_args

:set_password
set API_PASSWORD=%~2
shift
shift
goto :parse_args

:set_mode
set AUTH_MODE=%~2
shift
shift
goto :parse_args

:set_hmac
set AUTH_MODE=hmac
shift
goto :parse_args

:set_simple
set AUTH_MODE=simple
shift
goto :parse_args

:show_help
echo.
echo EWA API 调用脚本 v%VERSION%
echo.
echo 用法:
echo   %~nx0 [选项] ^<方法^> [参数...]
echo.
echo 方法:
echo   login                           登录获取 Token
echo   logout                          注销 Token
echo   getConfXml ^<xmlname^> [output]   获取配置文件 (output: xml/json)
echo   getConfItem ^<xmlname^> ^<item^> [output]  获取配置项 (output: xml/json)
echo   runConfItem ^<xmlname^> ^<item^> ^<newitem^>  复制配置项
echo   updateConfItem ^<xmlname^> ^<item^> ^<xml^>   更新配置项
echo   deleteConfItem ^<xmlname^> ^<item^>         删除配置项
echo   getTables ^<db^> [filter] [output]        获取数据库表列表
echo   getTable ^<db^> ^<tablename^> [output]      获取表结构详情
echo   getTableData ^<db^> ^<tablename^> [where] [output]  获取表数据(最多10条)
echo   help                            显示 API 帮助
echo.
echo 选项:
echo   -u, --url ^<url^>         API 基础地址
echo   -l, --login ^<login_id^>  登录ID
echo   -p, --password ^<pwd^>    密码
echo   -m, --mode ^<mode^>       认证模式: token, hmac, simple
echo   --hmac                  使用 HMAC 签名模式
echo   --simple                使用简单 Token 模式
echo   -v, --verbose           显示详细信息
echo   -h, --help              显示此帮助
echo.
echo 环境变量:
echo   EWA_API_URL       API 基础地址
echo   EWA_API_LOGIN_ID  登录ID
echo   EWA_API_PASSWORD  密码
echo   EWA_AUTH_MODE     认证模式
echo   EWA_TOKEN_FILE    Token 缓存文件
echo.
echo 示例:
echo   %~nx0 login
echo   %~nx0 getConfXml ewa/m
echo   %~nx0 getConfXml ewa/m json
echo   %~nx0 --hmac getConfXml ewa/m
echo   %~nx0 -u http://server:8080/ewa/servletApi getConfXml ewa/m
echo.
exit /b 0

:check_command
echo [ERROR] 请指定命令
goto :show_help

REM ==================== 登录 ====================
:do_login
if "%VERBOSE%"=="true" echo [INFO] 正在登录...

if not defined API_PASSWORD (
    echo [ERROR] 未设置密码，请使用 -p 参数或设置 EWA_API_PASSWORD 环境变量
    exit /b 1
)

curl -s -X POST "%API_BASE_URL%?method=login" -H "Content-Type: application/x-www-form-urlencoded" -d "login_id=%API_LOGIN_ID%&password=%API_PASSWORD%" > %TEMP%\ewa_login_response.json

REM 使用 PowerShell 解析 JSON
for /f "delims=" %%i in ('powershell -command "try { $r = Get-Content '%TEMP%\ewa_login_response.json' | ConvertFrom-Json; if ($r.RST -eq $true) { $r.token } else { '' } } catch { '' }"') do set TOKEN=%%i

if defined TOKEN (
    echo %TOKEN% > %TOKEN_FILE%
    if "%VERBOSE%"=="true" echo [INFO] 登录成功!
    echo Token: %TOKEN%
    echo Token 已保存到: %TOKEN_FILE%
    type %TEMP%\ewa_login_response.json
) else (
    echo [ERROR] 登录失败!
    type %TEMP%\ewa_login_response.json
    exit /b 1
)
del %TEMP%\ewa_login_response.json 2>nul
exit /b 0

REM ==================== 注销 ====================
:do_logout
if exist %TOKEN_FILE% (
    set /p TOKEN=<%TOKEN_FILE%
    curl -s -X POST "%API_BASE_URL%?method=logout" -H "X-Api-Token: !TOKEN!"
    del %TOKEN_FILE% 2>nul
    if "%VERBOSE%"=="true" echo [INFO] 已注销
) else (
    echo [WARN] 未找到 Token 文件
)
exit /b 0

REM ==================== 获取 Token ====================
:get_token
if not exist %TOKEN_FILE% (
    if "%VERBOSE%"=="true" echo [INFO] Token 不存在，正在登录...
    call :do_login >nul
)
set /p TOKEN=<%TOKEN_FILE%
exit /b 0

REM ==================== 发送请求 ====================
:send_request
set PARAMS=%~1

if "%AUTH_MODE%"=="token" goto :send_token_request
if "%AUTH_MODE%"=="hmac" goto :send_hmac_request
if "%AUTH_MODE%"=="simple" goto :send_simple_request
echo [ERROR] 未知认证模式: %AUTH_MODE%
exit /b 1

:send_token_request
call :get_token
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?%PARAMS%"
echo.
exit /b 0

:send_hmac_request
if not defined API_PASSWORD (
    echo [ERROR] HMAC 模式需要密码，请使用 -p 参数或设置 EWA_API_PASSWORD 环境变量
    exit /b 1
)

REM 获取时间戳
for /f %%i in ('powershell -command "[int64]((Get-Date) - (Get-Date '1970-01-01')).TotalMilliseconds"') do set TIMESTAMP=%%i

REM 生成 Nonce
for /f %%i in ('powershell -command "[guid]::NewGuid().ToString('N')"') do set NONCE=%%i

REM 提取路径
for /f "tokens=3 delims=/" %%i in ("%API_BASE_URL%") do set PATH_PART=/%%i

REM 构建签名字符串并计算签名
for /f %%i in ('powershell -command "$key='%API_PASSWORD%'; $data='GET`n%TIMESTAMP%`n%NONCE%`n%PATH_PART%`n%PARAMS%'; $hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($key)); [BitConverter]::ToString($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($data))).Replace('-','').ToLower()"') do set SIGNATURE=%%i

if "%VERBOSE%"=="true" (
    echo [INFO] 时间戳: %TIMESTAMP%
    echo [INFO] Nonce: %NONCE%
    echo [INFO] 签名: %SIGNATURE%
)

curl -s -H "X-Api-Key: %API_LOGIN_ID%" -H "X-Api-Timestamp: %TIMESTAMP%" -H "X-Api-Nonce: %NONCE%" -H "X-Api-Signature: %SIGNATURE%" "%API_BASE_URL%?%PARAMS%"
echo.
exit /b 0

:send_simple_request
if not defined API_PASSWORD (
    echo [ERROR] 简单 Token 模式需要密码，请使用 -p 参数或设置 EWA_API_PASSWORD 环境变量
    exit /b 1
)

curl -s -H "token: %API_PASSWORD%" "%API_BASE_URL%?%PARAMS%"
echo.
exit /b 0

REM ==================== API 方法 ====================
:do_get_conf_xml
if "%~2"=="" (
    echo [ERROR] 缺少 xmlname 参数
    echo 用法: %~nx0 getConfXml ^<xmlname^> [output]
    echo   output: xml ^(默认^) 或 json
    exit /b 1
)
REM 默认输出格式为 xml
set OUTPUT_FORMAT=xml
if not "%~3"=="" set OUTPUT_FORMAT=%~3
if "%VERBOSE%"=="true" echo [INFO] 获取配置文件: %~2 ^(格式: %OUTPUT_FORMAT%^)
set PARAMS=method=getConfXml^&xmlname=%~2^&output=%OUTPUT_FORMAT%
call :send_request "%PARAMS%"
exit /b 0

:do_get_conf_item
if "%~2"=="" (
    echo [ERROR] 缺少 xmlname 参数
    echo 用法: %~nx0 getConfItem ^<xmlname^> ^<itemname^> [output]
    echo   output: xml ^(默认^) 或 json
    exit /b 1
)
if "%~3"=="" (
    echo [ERROR] 缺少 itemname 参数
    echo 用法: %~nx0 getConfItem ^<xmlname^> ^<itemname^> [output]
    echo   output: xml ^(默认^) 或 json
    exit /b 1
)
REM 默认输出格式为 xml
set OUTPUT_FORMAT=xml
if not "%~4"=="" set OUTPUT_FORMAT=%~4
if "%VERBOSE%"=="true" echo [INFO] 获取配置项: %~2 / %~3 ^(格式: %OUTPUT_FORMAT%^)
set PARAMS=method=getConfItem^&xmlname=%~2^&itemname=%~3^&output=%OUTPUT_FORMAT%
call :send_request "%PARAMS%"
exit /b 0

:do_run_conf_item
if "%~2"=="" (
    echo [ERROR] 缺少 xmlname 参数
    echo 用法: %~nx0 runConfItem ^<xmlname^> ^<itemname^> ^<new_itemname^>
    exit /b 1
)
if "%~3"=="" (
    echo [ERROR] 缺少 itemname 参数
    echo 用法: %~nx0 runConfItem ^<xmlname^> ^<itemname^> ^<new_itemname^>
    exit /b 1
)
if "%~4"=="" (
    echo [ERROR] 缺少 new_itemname 参数
    echo 用法: %~nx0 runConfItem ^<xmlname^> ^<itemname^> ^<new_itemname^>
    exit /b 1
)
if "%VERBOSE%"=="true" echo [INFO] 复制配置项: %~3 -^> %~4
set PARAMS=method=runConfItem^&xmlname=%~2^&itemname=%~3^&new_itemname=%~4
call :send_request "%PARAMS%"
exit /b 0

:do_update_conf_item
if "%~2"=="" (
    echo [ERROR] 缺少 xmlname 参数
    echo 用法: %~nx0 updateConfItem ^<xmlname^> ^<itemname^> ^<xml^>
    exit /b 1
)
if "%~3"=="" (
    echo [ERROR] 缺少 itemname 参数
    echo 用法: %~nx0 updateConfItem ^<xmlname^> ^<itemname^> ^<xml^>
    exit /b 1
)
if "%~4"=="" (
    echo [ERROR] 缺少 xml 参数
    echo 用法: %~nx0 updateConfItem ^<xmlname^> ^<itemname^> ^<xml^>
    exit /b 1
)
if "%VERBOSE%"=="true" echo [INFO] 更新配置项: %~2 / %~3

REM URL 编码 XML
for /f "delims=" %%i in ('powershell -command "[System.Web.HttpUtility]::UrlEncode('%~4')"') do set ENCODED_XML=%%i

set PARAMS=method=updateConfItem^&xmlname=%~2^&itemname=%~3^&xml=%ENCODED_XML%
call :send_request "%PARAMS%"
exit /b 0

:do_delete_conf_item
if "%~2"=="" (
    echo [ERROR] 缺少 xmlname 参数
    echo 用法: %~nx0 deleteConfItem ^<xmlname^> ^<itemname^>
    exit /b 1
)
if "%~3"=="" (
    echo [ERROR] 缺少 itemname 参数
    echo 用法: %~nx0 deleteConfItem ^<xmlname^> ^<itemname^>
    exit /b 1
)
if "%VERBOSE%"=="true" echo [INFO] 删除配置项: %~2 / %~3
set PARAMS=method=deleteConfItem^&xmlname=%~2^&itemname=%~3
call :send_request "%PARAMS%"
exit /b 0

:do_get_tables
if "%~2"=="" (
    echo [ERROR] 缺少 db 参数
    echo 用法: %~nx0 getTables ^<db^> [filter] [output]
    echo   db: 数据库连接名称
    echo   filter: 表名过滤（可选）
    echo   output: xml ^(默认^) 或 json
    exit /b 1
)
REM 默认输出格式为 xml
set OUTPUT_FORMAT=xml
if not "%~4"=="" set OUTPUT_FORMAT=%~4
set FILTER_PARAM=
if not "%~3"=="" set FILTER_PARAM=^&filter=%~3
if "%VERBOSE%"=="true" echo [INFO] 获取数据库表列表: %~2 ^(格式: %OUTPUT_FORMAT%^)
set PARAMS=method=getTables^&db=%~2^&output=%OUTPUT_FORMAT%%FILTER_PARAM%
call :send_request "%PARAMS%"
exit /b 0

:do_get_table
if "%~2"=="" (
    echo [ERROR] 缺少 db 参数
    echo 用法: %~nx0 getTable ^<db^> ^<tablename^> [output]
    echo   db: 数据库连接名称
    echo   tablename: 表名
    echo   output: xml ^(默认^) 或 json
    exit /b 1
)
if "%~3"=="" (
    echo [ERROR] 缺少 tablename 参数
    echo 用法: %~nx0 getTable ^<db^> ^<tablename^> [output]
    exit /b 1
)
REM 默认输出格式为 xml
set OUTPUT_FORMAT=xml
if not "%~4"=="" set OUTPUT_FORMAT=%~4
if "%VERBOSE%"=="true" echo [INFO] 获取表结构: %~2 / %~3 ^(格式: %OUTPUT_FORMAT%^)
set PARAMS=method=getTable^&db=%~2^&tablename=%~3^&output=%OUTPUT_FORMAT%
call :send_request "%PARAMS%"
exit /b 0

:do_get_table_data
if "%~2"=="" (
    echo [ERROR] 缺少 db 参数
    echo 用法: %~nx0 getTableData ^<db^> ^<tablename^> [where] [output]
    echo   db: 数据库连接名称
    echo   tablename: 表名
    echo   where: WHERE 条件（可选）
    echo   output: json ^(默认^), xml 或 csv
    exit /b 1
)
if "%~3"=="" (
    echo [ERROR] 缺少 tablename 参数
    echo 用法: %~nx0 getTableData ^<db^> ^<tablename^> [where] [output]
    exit /b 1
)
REM 默认输出格式为 json
set OUTPUT_FORMAT=json
set WHERE_CLAUSE=
REM 检查参数4是否为输出格式
if not "%~4"=="" (
    if /i "%~4"=="json" set OUTPUT_FORMAT=json
    if /i "%~4"=="xml" set OUTPUT_FORMAT=xml
    if /i "%~4"=="csv" set OUTPUT_FORMAT=csv
    if /i not "%~4"=="json" if /i not "%~4"=="xml" if /i not "%~4"=="csv" (
        set WHERE_CLAUSE=%~4
        if not "%~5"=="" set OUTPUT_FORMAT=%~5
    )
)
if "%VERBOSE%"=="true" echo [INFO] 获取表数据: %~2 / %~3 ^(格式: %OUTPUT_FORMAT%, 最多10条^)
set PARAMS=method=getTableData^&db=%~2^&tablename=%~3^&output=%OUTPUT_FORMAT%
if not "%WHERE_CLAUSE%"=="" set PARAMS=%PARAMS%^&where=%WHERE_CLAUSE%
call :send_request "%PARAMS%"
exit /b 0

:do_help
if "%VERBOSE%"=="true" echo [INFO] 获取 API 帮助...
REM help 方法不需要认证
curl -s "%API_BASE_URL%?method=help"
echo.
exit /b 0