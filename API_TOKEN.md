# Servlet API Token 使用说明

## 概述

`ServletApi` 提供配置管理的 RESTful API 接口，支持三种认证模式：

| 认证模式 | 适用场景 | 安全级别 | 推荐度 |
|----------|----------|----------|--------|
| HMAC 签名 | 服务端调用、自动化脚本 | ⭐⭐⭐⭐⭐ | 推荐 |
| JWT Token | 客户端会话、Web应用 | ⭐⭐⭐⭐ | 推荐 |
| 简单 Token | 兼容旧版、测试环境 | ⭐⭐ | 不推荐 |

---

## API 方法列表

| 方法 | 参数 | 说明 | 需认证 |
|------|------|------|--------|
| `login` | `login_id`, `password` | 登录获取 Token | ❌ |
| `logout` | `token` | 注销 Token | ❌ |
| `getConfXml` | `xmlname` | 获取完整配置文件 XML | ✅ |
| `getConfItem` | `xmlname`, `itemname` | 获取指定配置项 | ✅ |
| `runConfItem` | `xmlname`, `itemname`, `new_itemname` | 复制配置项 | ✅ |
| `updateConfItem` | `xmlname`, `itemname`, `xml` | 更新配置项 | ✅ |
| `deleteConfItem` | `xmlname`, `itemname` | 删除配置项 | ✅ |
| `help` | - | 获取 API 帮助文档 | ✅ |

---

## 一、JWT Token 模式

### 1.1 登录获取 Token

```bash
# 登录请求
curl -X POST "http://localhost:8080/ewa/servletApi?method=login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "login_id=admin&password=your_password"
```

**响应示例：**
```json
{
  "RST": true,
  "token": "a1b2c3d4e5f67890abcdef1234567890abcdef1234567890abcdef1234567890",
  "token_type": "Bearer",
  "expires_in": 7200,
  "login_id": "admin"
}
```

### 1.2 使用 Token 调用 API

**方式一：X-Api-Token Header**
```bash
curl -H "X-Api-Token: a1b2c3d4e5f67890abcdef..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

**方式二：Authorization Bearer Header**
```bash
curl -H "Authorization: Bearer a1b2c3d4e5f67890abcdef..." \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

### 1.3 注销 Token

```bash
curl -X POST "http://localhost:8080/ewa/servletApi?method=logout" \
  -H "X-Api-Token: a1b2c3d4e5f67890abcdef..."
```

**响应示例：**
```json
{
  "RST": true,
  "MSG": "Logged out successfully"
}
```

---

## 二、HMAC 签名模式

### 2.1 签名算法

```
1. 构建签名字符串:
   stringToSign = METHOD + "\n" + TIMESTAMP + "\n" + NONCE + "\n" + PATH + "\n" + SORTED_QUERY_PARAMS

2. 计算签名:
   signature = HMAC-SHA256(password, stringToSign)

3. 转换为小写十六进制字符串
```

### 2.2 请求 Headers

| Header | 说明 | 示例 |
|--------|------|------|
| `X-Api-Key` | 管理员登录ID | `admin` |
| `X-Api-Timestamp` | 当前时间戳（毫秒） | `1700000000000` |
| `X-Api-Nonce` | 随机字符串（防重放） | `abc123random` |
| `X-Api-Signature` | HMAC-SHA256 签名 | `a1b2c3d4...` |

### 2.3 签名计算示例

假设：
- 登录ID: `admin`
- 密码: `mypassword123`
- 请求方法: `GET`
- 请求路径: `/ewa/servletApi`
- 时间戳: `1700000000000`
- Nonce: `abc123random`
- 查询参数: `method=getConfXml&xmlname=ewa/m`

```
stringToSign = "GET\n1700000000000\nabc123random\n/ewa/servletApi\nmethod=getConfXml&xmlname=ewa/m"
signature = HMAC-SHA256("mypassword123", stringToSign)
```

### 2.4 完整请求示例

```bash
curl -H "X-Api-Key: admin" \
  -H "X-Api-Timestamp: 1700000000000" \
  -H "X-Api-Nonce: abc123random" \
  -H "X-Api-Signature: calculated_signature_here" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

---

## 三、简单 Token 模式（不推荐）

仅用于测试或兼容旧版，直接使用密码作为 token：

```bash
curl -H "token: your_password" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

---

## 四、API 调用示例

### 4.1 获取配置文件

```bash
# 使用 Token
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfXml&xmlname=ewa/m"
```

**响应：**
```json
{
  "RST": true,
  "XML": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>...",
  "XMLNAME": "ewa/m"
}
```

### 4.2 获取配置项

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=getConfItem&xmlname=ewa/m&itemname=ChangePWD"
```

**响应：**
```json
{
  "RST": true,
  "XML": "<EasyWebTemplate Name=\"ChangePWD\">...</EasyWebTemplate>",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "ChangePWD"
}
```

### 4.3 复制配置项

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=runConfItem&xmlname=ewa/m&itemname=ChangePWD&new_itemname=ChangePWD_Copy"
```

**响应：**
```json
{
  "RST": true,
  "MSG": "Item copied successfully",
  "XMLNAME": "ewa/m",
  "SOURCE_ITEM": "ChangePWD",
  "NEW_ITEM": "ChangePWD_Copy"
}
```

### 4.4 更新配置项

```bash
# 使用 POST 方式，XML 内容通过 POST body 传递
curl -X POST "http://localhost:8080/ewa/servletApi?method=updateConfItem&xmlname=ewa/m&itemname=ChangePWD" \
  -H "X-Api-Token: your_token" \
  -H "Content-Type: application/xml" \
  -d '<EasyWebTemplate Name="ChangePWD">...</EasyWebTemplate>'
```

或使用 URL 参数：

```bash
curl -X POST "http://localhost:8080/ewa/servletApi?method=updateConfItem" \
  -H "X-Api-Token: your_token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "xmlname=ewa/m&itemname=ChangePWD&xml=<EasyWebTemplate Name=\"ChangePWD\">...</EasyWebTemplate>"
```

**响应：**
```json
{
  "RST": true,
  "MSG": "Item updated successfully",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "ChangePWD"
}
```

### 4.5 删除配置项

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=deleteConfItem&xmlname=ewa/m&itemname=ChangePWD_Copy"
```

**响应：**
```json
{
  "RST": true,
  "MSG": "Item deleted successfully",
  "XMLNAME": "ewa/m",
  "ITEMNAME": "ChangePWD_Copy"
}
```

### 4.6 获取帮助

```bash
curl -H "X-Api-Token: your_token" \
  "http://localhost:8080/ewa/servletApi?method=help"
```

---

## 五、错误响应

所有错误响应格式：

```json
{
  "RST": false,
  "ERR": "错误信息",
  "CODE": 401
}
```

| CODE | 说明 |
|------|------|
| 400 | 参数错误 |
| 401 | 认证失败 |
| 403 | 权限不足 |
| 500 | 服务器错误 |

---

## 六、安全特性

### HMAC 签名模式安全特性

| 特性 | 说明 |
|------|------|
| 时间戳验证 | 请求时间戳有效期 5 分钟 |
| Nonce 防重放 | 每个 Nonce 只能使用一次 |
| 常量时间比较 | 防止时序攻击 |
| 参数签名 | 请求参数参与签名计算 |

### JWT Token 模式安全特性

| 特性 | 说明 |
|------|------|
| Token 过期 | 默认 2 小时过期 |
| 服务端缓存 | Token 存储在服务端内存 |
| 主动撤销 | 支持通过 logout 撤销 Token |

---

## 七、Shell 脚本示例

### 7.1 ewa_api.sh - HMAC 签名调用脚本

```bash
#!/bin/bash
#
# EWA API 调用脚本 (HMAC 签名模式)
# 用法: ./ewa_api.sh <method> [params...]
# 示例: ./ewa_api.sh getConfXml "xmlname=ewa/m"
#

# ==================== 配置区域 ====================
API_BASE_URL="http://localhost:8080/ewa/servletApi"
API_KEY="admin"           # 登录ID
API_SECRET="your_password" # 密码
# =================================================

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印帮助
print_help() {
    echo "EWA API 调用脚本"
    echo ""
    echo "用法: $0 <method> [params...]"
    echo ""
    echo "方法:"
    echo "  login                    登录获取 Token"
    echo "  getConfXml <xmlname>     获取配置文件"
    echo "  getConfItem <xmlname> <itemname>  获取配置项"
    echo "  runConfItem <xmlname> <itemname> <newname>  复制配置项"
    echo "  deleteConfItem <xmlname> <itemname>  删除配置项"
    echo "  help                     显示 API 帮助"
    echo ""
    echo "示例:"
    echo "  $0 login"
    echo "  $0 getConfXml ewa/m"
    echo "  $0 getConfItem ewa/m ChangePWD"
    echo "  $0 runConfItem ewa/m ChangePWD ChangePWD_Copy"
    echo "  $0 deleteConfItem ewa/m ChangePWD_Copy"
}

# 计算 HMAC-SHA256
hmac_sha256() {
    local key="$1"
    local data="$2"
    echo -n "$data" | openssl dgst -sha256 -hmac "$key" | awk '{print $2}'
}

# 生成随机 Nonce
generate_nonce() {
    cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
}

# 获取当前时间戳（毫秒）
get_timestamp() {
    # macOS 和 Linux 兼容
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo $(($(date +%s) * 1000))
    else
        echo $(date +%s%3N)
    fi
}

# 构建签名字符串
build_string_to_sign() {
    local method="$1"
    local timestamp="$2"
    local nonce="$3"
    local path="$4"
    local params="$5"
    
    echo -e "${method}\n${timestamp}\n${nonce}\n${path}\n${params}"
}

# 发送 HMAC 签名请求
send_hmac_request() {
    local http_method="GET"
    local query_params="$1"
    
    local timestamp=$(get_timestamp)
    local nonce=$(generate_nonce)
    local path="/ewa/servletApi"
    
    # 构建签名字符串
    local string_to_sign=$(build_string_to_sign "$http_method" "$timestamp" "$nonce" "$path" "$query_params")
    
    # 计算签名
    local signature=$(hmac_sha256 "$API_SECRET" "$string_to_sign")
    
    # 发送请求
    local url="${API_BASE_URL}?${query_params}"
    
    echo -e "${YELLOW}请求 URL:${NC} $url"
    echo -e "${YELLOW}时间戳:${NC} $timestamp"
    echo -e "${YELLOW}Nonce:${NC} $nonce"
    echo -e "${YELLOW}签名:${NC} $signature"
    echo ""
    
    curl -s -H "X-Api-Key: $API_KEY" \
         -H "X-Api-Timestamp: $timestamp" \
         -H "X-Api-Nonce: $nonce" \
         -H "X-Api-Signature: $signature" \
         "$url" | python3 -m json.tool 2>/dev/null || cat
}

# 登录获取 Token
do_login() {
    echo -e "${GREEN}登录获取 Token...${NC}"
    
    local url="${API_BASE_URL}?method=login"
    
    curl -s -X POST "$url" \
         -H "Content-Type: application/x-www-form-urlencoded" \
         -d "login_id=${API_KEY}&password=${API_SECRET}" | python3 -m json.tool 2>/dev/null || cat
}

# 获取配置文件
do_get_conf_xml() {
    local xmlname="$1"
    
    if [ -z "$xmlname" ]; then
        echo -e "${RED}错误: 缺少 xmlname 参数${NC}"
        echo "用法: $0 getConfXml <xmlname>"
        exit 1
    fi
    
    echo -e "${GREEN}获取配置文件: $xmlname${NC}"
    send_hmac_request "method=getConfXml&xmlname=${xmlname}"
}

# 获取配置项
do_get_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ]; then
        echo -e "${RED}错误: 缺少参数${NC}"
        echo "用法: $0 getConfItem <xmlname> <itemname>"
        exit 1
    fi
    
    echo -e "${GREEN}获取配置项: $xmlname / $itemname${NC}"
    send_hmac_request "method=getConfItem&xmlname=${xmlname}&itemname=${itemname}"
}

# 复制配置项
do_run_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    local newname="$3"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ] || [ -z "$newname" ]; then
        echo -e "${RED}错误: 缺少参数${NC}"
        echo "用法: $0 runConfItem <xmlname> <itemname> <new_itemname>"
        exit 1
    fi
    
    echo -e "${GREEN}复制配置项: $itemname -> $newname${NC}"
    send_hmac_request "method=runConfItem&xmlname=${xmlname}&itemname=${itemname}&new_itemname=${newname}"
}

# 删除配置项
do_delete_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ]; then
        echo -e "${RED}错误: 缺少参数${NC}"
        echo "用法: $0 deleteConfItem <xmlname> <itemname>"
        exit 1
    fi
    
    echo -e "${GREEN}删除配置项: $xmlname / $itemname${NC}"
    send_hmac_request "method=deleteConfItem&xmlname=${xmlname}&itemname=${itemname}"
}

# 获取帮助
do_help() {
    echo -e "${GREEN}获取 API 帮助...${NC}"
    send_hmac_request "method=help"
}

# 主程序
main() {
    if [ $# -eq 0 ]; then
        print_help
        exit 0
    fi
    
    local command="$1"
    shift
    
    case "$command" in
        login)
            do_login "$@"
            ;;
        getConfXml)
            do_get_conf_xml "$@"
            ;;
        getConfItem)
            do_get_conf_item "$@"
            ;;
        runConfItem)
            do_run_conf_item "$@"
            ;;
        deleteConfItem)
            do_delete_conf_item "$@"
            ;;
        help)
            do_help
            ;;
        *)
            echo -e "${RED}未知命令: $command${NC}"
            print_help
            exit 1
            ;;
    esac
}

main "$@"
```

### 7.2 ewa_api_token.sh - Token 模式调用脚本

```bash
#!/bin/bash
#
# EWA API 调用脚本 (Token 模式)
# 用法: ./ewa_api_token.sh <method> [params...]
#

# ==================== 配置区域 ====================
API_BASE_URL="http://localhost:8080/ewa/servletApi"
API_LOGIN_ID="admin"
API_PASSWORD="your_password"
TOKEN_FILE="/tmp/.ewa_api_token"
# =================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 登录并保存 Token
login() {
    echo -e "${GREEN}登录中...${NC}"
    
    local response=$(curl -s -X POST "${API_BASE_URL}?method=login" \
         -H "Content-Type: application/x-www-form-urlencoded" \
         -d "login_id=${API_LOGIN_ID}&password=${API_PASSWORD}")
    
    local rst=$(echo "$response" | grep -o '"RST":[^,}]*' | cut -d':' -f2)
    
    if [ "$rst" = "true" ]; then
        local token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        echo "$token" > "$TOKEN_FILE"
        echo -e "${GREEN}登录成功!${NC}"
        echo "Token: $token"
        echo "Token 已保存到: $TOKEN_FILE"
    else
        echo -e "${RED}登录失败!${NC}"
        echo "$response"
    fi
}

# 注销
logout() {
    if [ -f "$TOKEN_FILE" ]; then
        local token=$(cat "$TOKEN_FILE")
        curl -s -X POST "${API_BASE_URL}?method=logout" \
             -H "X-Api-Token: $token"
        rm -f "$TOKEN_FILE"
        echo -e "${GREEN}已注销${NC}"
    else
        echo -e "${YELLOW}未找到 Token 文件${NC}"
    fi
}

# 获取 Token
get_token() {
    if [ ! -f "$TOKEN_FILE" ]; then
        echo -e "${YELLOW}Token 不存在，正在登录...${NC}"
        login
    fi
    
    cat "$TOKEN_FILE"
}

# 发送请求
send_request() {
    local params="$1"
    local token=$(get_token)
    
    curl -s -H "X-Api-Token: $token" \
         "${API_BASE_URL}?${params}" | python3 -m json.tool 2>/dev/null || cat
}

# 主程序
case "$1" in
    login)
        login
        ;;
    logout)
        logout
        ;;
    getConfXml)
        send_request "method=getConfXml&xmlname=$2"
        ;;
    getConfItem)
        send_request "method=getConfItem&xmlname=$2&itemname=$3"
        ;;
    runConfItem)
        send_request "method=runConfItem&xmlname=$2&itemname=$3&new_itemname=$4"
        ;;
    deleteConfItem)
        send_request "method=deleteConfItem&xmlname=$2&itemname=$3"
        ;;
    help)
        send_request "method=help"
        ;;
    *)
        echo "用法: $0 {login|logout|getConfXml|getConfItem|runConfItem|deleteConfItem|help} [params...]"
        echo ""
        echo "示例:"
        echo "  $0 login"
        echo "  $0 getConfXml ewa/m"
        echo "  $0 getConfItem ewa/m ChangePWD"
        echo "  $0 runConfItem ewa/m ChangePWD ChangePWD_Copy"
        echo "  $0 deleteConfItem ewa/m ChangePWD_Copy"
        echo "  $0 logout"
        ;;
esac
```

---

## 八、Windows BAT 脚本示例

### 8.1 ewa_api.bat - HMAC 签名调用脚本

```batch
@echo off
REM EWA API 调用脚本 (HMAC 签名模式)
REM 需要安装 OpenSSL 和 curl

setlocal enabledelayedexpansion

REM ==================== 配置区域 ====================
set API_BASE_URL=http://localhost:8080/ewa/servletApi
set API_KEY=admin
set API_SECRET=your_password
REM =================================================

if "%1"=="" goto :help
if "%1"=="help" goto :help
if "%1"=="login" goto :login
if "%1"=="getConfXml" goto :getConfXml
if "%1"=="getConfItem" goto :getConfItem
if "%1"=="runConfItem" goto :runConfItem
if "%1"=="deleteConfItem" goto :deleteConfItem
goto :help

:help
echo EWA API 调用脚本
echo.
echo 用法: %0 ^<method^> [params...]
echo.
echo 方法:
echo   login                    登录获取 Token
echo   getConfXml ^<xmlname^>     获取配置文件
echo   getConfItem ^<xmlname^> ^<itemname^>  获取配置项
echo   runConfItem ^<xmlname^> ^<itemname^> ^<newname^>  复制配置项
echo   deleteConfItem ^<xmlname^> ^<itemname^>  删除配置项
echo   help                     显示帮助
echo.
echo 示例:
echo   %0 login
echo   %0 getConfXml ewa/m
echo   %0 getConfItem ewa/m ChangePWD
goto :eof

:login
echo 登录获取 Token...
curl -s -X POST "%API_BASE_URL%?method=login" -H "Content-Type: application/x-www-form-urlencoded" -d "login_id=%API_KEY%&password=%API_SECRET%"
echo.
goto :eof

:getConfXml
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
echo 获取配置文件: %2
call :send_hmac_request "method=getConfXml&xmlname=%2"
goto :eof

:getConfItem
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
if "%3"=="" (
    echo 错误: 缺少 itemname 参数
    goto :eof
)
echo 获取配置项: %2 / %3
call :send_hmac_request "method=getConfItem&xmlname=%2&itemname=%3"
goto :eof

:runConfItem
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
if "%3"=="" (
    echo 错误: 缺少 itemname 参数
    goto :eof
)
if "%4"=="" (
    echo 错误: 缺少 new_itemname 参数
    goto :eof
)
echo 复制配置项: %3 -^> %4
call :send_hmac_request "method=runConfItem&xmlname=%2&itemname=%3&new_itemname=%4"
goto :eof

:deleteConfItem
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
if "%3"=="" (
    echo 错误: 缺少 itemname 参数
    goto :eof
)
echo 删除配置项: %2 / %3
call :send_hmac_request "method=deleteConfItem&xmlname=%2&itemname=%3"
goto :eof

REM 发送 HMAC 签名请求
:send_hmac_request
set params=%~1

REM 获取时间戳 (Windows 没有毫秒，使用秒*1000)
for /f %%i in ('powershell -command "[int64](Get-Date -UFormat %%s) * 1000"') do set timestamp=%%i

REM 生成随机 Nonce
for /f %%i in ('powershell -command "[guid]::NewGuid().ToString('N')"') do set nonce=%%i

REM 构建签名字符串
set string_to_sign=GET\n%timestamp%\n%nonce%\n/ewa/servletApi\n%params%

REM 计算 HMAC-SHA256 签名 (使用 PowerShell)
for /f %%i in ('powershell -command "$key='%API_SECRET%'; $data='GET`n%timestamp%`n%nonce%`n/ewa/servletApi`n%params%'; $hmac = [System.Security.Cryptography.HMACSHA256]::new([System.Text.Encoding]::UTF8.GetBytes($key)); [BitConverter]::ToString($hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($data))).Replace('-','').ToLower()"') do set signature=%%i

echo 时间戳: %timestamp%
echo Nonce: %nonce%
echo 签名: %signature%

curl -s -H "X-Api-Key: %API_KEY%" -H "X-Api-Timestamp: %timestamp%" -H "X-Api-Nonce: %nonce%" -H "X-Api-Signature: %signature%" "%API_BASE_URL%?%params%"
echo.
goto :eof
```

### 8.2 ewa_api_token.bat - Token 模式调用脚本

```batch
@echo off
REM EWA API 调用脚本 (Token 模式)

setlocal enabledelayedexpansion

REM ==================== 配置区域 ====================
set API_BASE_URL=http://localhost:8080/ewa/servletApi
set API_LOGIN_ID=admin
set API_PASSWORD=your_password
set TOKEN_FILE=%TEMP%\ewa_api_token.txt
REM =================================================

if "%1"=="" goto :help
if "%1"=="help" goto :help
if "%1"=="login" goto :login
if "%1"=="logout" goto :logout
if "%1"=="getConfXml" goto :getConfXml
if "%1"=="getConfItem" goto :getConfItem
if "%1"=="runConfItem" goto :runConfItem
if "%1"=="deleteConfItem" goto :deleteConfItem
goto :help

:help
echo EWA API 调用脚本 (Token 模式)
echo.
echo 用法: %0 ^<method^> [params...]
echo.
echo 方法:
echo   login                    登录获取 Token
echo   logout                   注销 Token
echo   getConfXml ^<xmlname^>     获取配置文件
echo   getConfItem ^<xmlname^> ^<itemname^>  获取配置项
echo   runConfItem ^<xmlname^> ^<itemname^> ^<newname^>  复制配置项
echo   deleteConfItem ^<xmlname^> ^<itemname^>  删除配置项
goto :eof

:login
echo 登录中...
curl -s -X POST "%API_BASE_URL%?method=login" -H "Content-Type: application/x-www-form-urlencoded" -d "login_id=%API_LOGIN_ID%&password=%API_PASSWORD%" > %TEMP%\login_response.json

REM 解析 Token (使用 PowerShell)
for /f %%i in ('powershell -command "(Get-Content %TEMP%\login_response.json | ConvertFrom-Json).token"') do set token=%%i

if defined token (
    echo %token% > %TOKEN_FILE%
    echo 登录成功!
    echo Token: %token%
    echo Token 已保存到: %TOKEN_FILE%
) else (
    echo 登录失败!
    type %TEMP%\login_response.json
)
goto :eof

:logout
if exist %TOKEN_FILE% (
    set /p token=<%TOKEN_FILE%
    curl -s -X POST "%API_BASE_URL%?method=logout" -H "X-Api-Token: !token!"
    del %TOKEN_FILE%
    echo 已注销
) else (
    echo 未找到 Token 文件
)
goto :eof

:get_token
if not exist %TOKEN_FILE% (
    echo Token 不存在，正在登录...
    call :login
)
set /p TOKEN=<%TOKEN_FILE%
goto :eof

:getConfXml
call :get_token
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=getConfXml&xmlname=%2"
echo.
goto :eof

:getConfItem
call :get_token
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
if "%3"=="" (
    echo 错误: 缺少 itemname 参数
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=getConfItem&xmlname=%2&itemname=%3"
echo.
goto :eof

:runConfItem
call :get_token
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
if "%3"=="" (
    echo 错误: 缺少 itemname 参数
    goto :eof
)
if "%4"=="" (
    echo 错误: 缺少 new_itemname 参数
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=runConfItem&xmlname=%2&itemname=%3&new_itemname=%4"
echo.
goto :eof

:deleteConfItem
call :get_token
if "%2"=="" (
    echo 错误: 缺少 xmlname 参数
    goto :eof
)
if "%3"=="" (
    echo 错误: 缺少 itemname 参数
    goto :eof
)
curl -s -H "X-Api-Token: %TOKEN%" "%API_BASE_URL%?method=deleteConfItem&xmlname=%2&itemname=%3"
echo.
goto :eof
```

---

## 九、Web.xml 配置

在 `web.xml` 中添加 Servlet 映射：

```xml
<servlet>
    <servlet-name>ServletApi</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletApi</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>ServletApi</servlet-name>
    <url-pattern>/servletApi</url-pattern>
</servlet-mapping>
```

---

## 十、注意事项

1. **生产环境建议使用 HTTPS**，防止 Token 和签名被窃听
2. **定期更换密码**，ewa_conf.xml 中的 Admin 密码应定期更换
3. **限制 IP 访问**，可在反向代理层限制 API 访问 IP
4. **日志审计**，记录所有 API 调用日志用于审计
5. **Token 有效期**，默认 2 小时，可根据需求调整

---

## 十一、常见问题

### Q1: Token 过期怎么办？
A: Token 默认 2 小时过期，过期后需要重新调用 `login` 方法获取新 Token。

### Q2: HMAC 签名验证失败？
A: 检查以下几点：
- 时间戳是否在 5 分钟内
- Nonce 是否重复使用
- 签名算法是否正确（HMAC-SHA256）
- 参数排序是否正确

### Q3: 如何调试签名？
A: 调用 `method=help` 可以查看签名算法说明，对比服务端和客户端的签名字符串。

---

**文档版本**: v1.0  
**最后更新**: 2024年