#!/bin/bash
#
# EWA API 调用脚本
# 支持三种认证模式：JWT Token、HMAC签名、简单Token
#
# 用法: ./call-ewa-api.sh [options] <method> [params...]
#
# 示例:
#   ./call-ewa-api.sh login
#   ./call-ewa-api.sh getConfXml ewa/m
#   ./call-ewa-api.sh getConfItem ewa/m ChangePWD
#   ./call-ewa-api.sh --hmac getConfXml ewa/m
#

set -e

# ==================== 配置区域 ====================
# API 基础地址
API_BASE_URL="${EWA_API_URL:-http://localhost:8080/ewa/servletApi}"

# 登录凭据
API_LOGIN_ID="${EWA_API_LOGIN_ID:-admin}"
API_PASSWORD="${EWA_API_PASSWORD:-}"

# Token 缓存文件
TOKEN_FILE="${EWA_TOKEN_FILE:-/tmp/.ewa_api_token}"

# 认证模式: token, hmac, simple
AUTH_MODE="${EWA_AUTH_MODE:-token}"
# =================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 版本
VERSION="1.0.0"

# ==================== 工具函数 ====================

# 打印帮助
print_help() {
    cat << EOF
EWA API 调用脚本 v${VERSION}

用法:
  $0 [选项] <方法> [参数...]

方法:
  login [login_id] [password]    登录获取 Token
  logout                          注销 Token
  getConfXml <xmlname> [output]   获取配置文件 (output: xml/json)
  getConfItem <xmlname> <item> [output]  获取配置项 (output: xml/json)
  runConfItem <xmlname> <item> <newitem>  复制配置项
  updateConfItem <xmlname> <item> <xml>   更新配置项
  deleteConfItem <xmlname> <item>         删除配置项
  getTables <db> [filter] [output]        获取数据库表列表
  getTable <db> <tablename> [output]      获取表结构详情
  getTableData <db> <tablename> [where] [output]  获取表数据(最多10条)
  help                            显示 API 帮助

选项:
  -u, --url <url>         API 基础地址
  -l, --login <login_id>  登录ID
  -p, --password <pwd>    密码
  -m, --mode <mode>       认证模式: token, hmac, simple
  --hmac                  使用 HMAC 签名模式
  --simple                使用简单 Token 模式
  -v, --verbose           显示详细信息
  -h, --help              显示此帮助

环境变量:
  EWA_API_URL       API 基础地址
  EWA_API_LOGIN_ID  登录ID
  EWA_API_PASSWORD  密码
  EWA_AUTH_MODE     认证模式
  EWA_TOKEN_FILE    Token 缓存文件

示例:
  # 登录获取 Token
  $0 login

  # 使用 Token 模式获取配置 (默认 XML 格式)
  $0 getConfXml ewa/m

  # 获取 JSON 格式输出
  $0 getConfXml ewa/m json

  # 使用 HMAC 签名模式
  $0 --hmac getConfXml ewa/m

  # 使用简单 Token 模式
  $0 --simple getConfXml ewa/m

  # 指定服务器地址
  $0 -u http://server:8080/ewa/servletApi getConfXml ewa/m

EOF
}

# 日志输出
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

# 检查依赖
check_dependencies() {
    local missing=()
    
    command -v curl >/dev/null 2>&1 || missing+=("curl")
    
    if [ "$AUTH_MODE" = "hmac" ]; then
        command -v openssl >/dev/null 2>&1 || missing+=("openssl")
    fi
    
    if [ ${#missing[@]} -gt 0 ]; then
        log_error "缺少依赖: ${missing[*]}"
        exit 1
    fi
}

# 生成随机 Nonce
generate_nonce() {
    if command -v uuidgen >/dev/null 2>&1; then
        uuidgen | tr -d '-'
    elif [ -f /proc/sys/kernel/random/uuid ]; then
        cat /proc/sys/kernel/random/uuid | tr -d '-'
    else
        cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
    fi
}

# 获取时间戳（毫秒）
get_timestamp() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo $(($(date +%s) * 1000))
    else
        date +%s%3N 2>/dev/null || echo $(($(date +%s) * 1000))
    fi
}

# 计算 HMAC-SHA256
hmac_sha256() {
    local key="$1"
    local data="$2"
    printf '%s' "$data" | openssl dgst -sha256 -hmac "$key" 2>/dev/null | awk '{print $NF}'
}

# JSON 格式化输出
format_json() {
    if command -v python3 >/dev/null 2>&1; then
        python3 -m json.tool 2>/dev/null || cat
    elif command -v python >/dev/null 2>&1; then
        python -m json.tool 2>/dev/null || cat
    else
        cat
    fi
}

# ==================== 认证函数 ====================

# 登录获取 Token
do_login() {
    # 支持直接传参: login <login_id> <password>
    if [ -n "$1" ] && [ -n "$2" ]; then
        API_LOGIN_ID="$1"
        API_PASSWORD="$2"
    fi

    log_info "正在登录..."

    if [ -z "$API_PASSWORD" ]; then
        log_error "未设置密码，请使用 -p 参数或设置 EWA_API_PASSWORD 环境变量"
        exit 1
    fi

    local response
    response=$(curl -s -X POST "${API_BASE_URL}?method=login" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "login_id=${API_LOGIN_ID}&password=${API_PASSWORD}")

    local rst
    rst=$(echo "$response" | grep -o '"RST"[[:space:]]*:[[:space:]]*[^,}]*' | head -1 | sed 's/.*:[[:space:]]*//' | tr -d ' "')

    if [ "$rst" = "true" ]; then
        local token
        token=$(echo "$response" | grep -o '"token"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/.*:[[:space:]]*"//' | tr -d '"')

        if [ -n "$token" ]; then
            echo "$token" > "$TOKEN_FILE"
            chmod 600 "$TOKEN_FILE"
            log_info "登录成功!"
            echo "Token: $token"
            echo "Token 已保存到: $TOKEN_FILE"
        fi
        echo "$response" | format_json
    else
        log_error "登录失败!"
        echo "$response" | format_json
        exit 1
    fi
}

# 注销
do_logout() {
    local token
    if [ -f "$TOKEN_FILE" ]; then
        token=$(cat "$TOKEN_FILE")
    fi
    
    if [ -z "$token" ]; then
        log_warn "未找到 Token 文件"
        return
    fi
    
    log_info "正在注销..."
    curl -s -X POST "${API_BASE_URL}?method=logout" \
        -H "X-Api-Token: $token" | format_json
    
    rm -f "$TOKEN_FILE"
    log_info "已注销"
}

# 获取缓存的 Token
get_cached_token() {
    if [ -f "$TOKEN_FILE" ]; then
        cat "$TOKEN_FILE"
    fi
}

# 发送 Token 模式请求
send_token_request() {
    local params="$1"
    local token
    
    token=$(get_cached_token)
    
    if [ -z "$token" ]; then
        log_info "Token 不存在，正在登录..."
        do_login >/dev/null
        token=$(get_cached_token)
    fi
    
    curl -s -H "X-Api-Token: $token" "${API_BASE_URL}?${params}" | format_json
}

# 发送 HMAC 签名请求
send_hmac_request() {
    local params="$1"
    local http_method="GET"
    
    if [ -z "$API_PASSWORD" ]; then
        log_error "HMAC 模式需要密码，请使用 -p 参数或设置 EWA_API_PASSWORD 环境变量"
        exit 1
    fi
    
    local timestamp
    timestamp=$(get_timestamp)
    
    local nonce
    nonce=$(generate_nonce)
    
    # 从 URL 提取路径
    local path
    path=$(echo "$API_BASE_URL" | sed 's|^[^/]*//[^/]*||')
    
    # 构建签名字符串
    local string_to_sign="${http_method}
${timestamp}
${nonce}
${path}
${params}"
    
    # 计算签名
    local signature
    signature=$(hmac_sha256 "$API_PASSWORD" "$string_to_sign")
    
    log_info "时间戳: $timestamp"
    log_info "Nonce: $nonce"
    log_info "签名: $signature"
    
    curl -s \
        -H "X-Api-Key: $API_LOGIN_ID" \
        -H "X-Api-Timestamp: $timestamp" \
        -H "X-Api-Nonce: $nonce" \
        -H "X-Api-Signature: $signature" \
        "${API_BASE_URL}?${params}" | format_json
}

# 发送简单 Token 请求
send_simple_request() {
    local params="$1"
    
    if [ -z "$API_PASSWORD" ]; then
        log_error "简单 Token 模式需要密码，请使用 -p 参数或设置 EWA_API_PASSWORD 环境变量"
        exit 1
    fi
    
    curl -s -H "token: $API_PASSWORD" "${API_BASE_URL}?${params}" | format_json
}

# 发送请求（根据认证模式）
send_request() {
    local params="$1"
    
    case "$AUTH_MODE" in
        token)
            send_token_request "$params"
            ;;
        hmac)
            send_hmac_request "$params"
            ;;
        simple)
            send_simple_request "$params"
            ;;
        *)
            log_error "未知认证模式: $AUTH_MODE"
            exit 1
            ;;
    esac
}

# ==================== API 方法 ====================

# 获取配置文件
do_get_conf_xml() {
    local xmlname="$1"
    local output="$2"

    if [ -z "$xmlname" ]; then
        log_error "缺少 xmlname 参数"
        echo "用法: $0 getConfXml <xmlname> [output]"
        echo "  output: xml (默认) 或 json"
        exit 1
    fi

    # 默认输出格式为 xml
    if [ -z "$output" ]; then
        output="xml"
    fi

    log_info "获取配置文件: $xmlname (格式: $output)"
    send_request "method=getConfXml&xmlname=${xmlname}&output=${output}"
}

# 获取配置项
do_get_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    local output="$3"

    if [ -z "$xmlname" ] || [ -z "$itemname" ]; then
        log_error "缺少参数"
        echo "用法: $0 getConfItem <xmlname> <itemname> [output]"
        echo "  output: xml (默认) 或 json"
        exit 1
    fi

    # 默认输出格式为 xml
    if [ -z "$output" ]; then
        output="xml"
    fi

    log_info "获取配置项: $xmlname / $itemname (格式: $output)"
    send_request "method=getConfItem&xmlname=${xmlname}&itemname=${itemname}&output=${output}"
}

# 复制配置项
do_run_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    local newname="$3"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ] || [ -z "$newname" ]; then
        log_error "缺少参数"
        echo "用法: $0 runConfItem <xmlname> <itemname> <new_itemname>"
        exit 1
    fi
    
    log_info "复制配置项: $itemname -> $newname"
    send_request "method=runConfItem&xmlname=${xmlname}&itemname=${itemname}&new_itemname=${newname}"
}

# 更新配置项
do_update_conf_item() {
    local xmlname="$1"
    local itemname="$2"
    local xml="$3"
    
    if [ -z "$xmlname" ] || [ -z "$itemname" ] || [ -z "$xml" ]; then
        log_error "缺少参数"
        echo "用法: $0 updateConfItem <xmlname> <itemname> <xml>"
        exit 1
    fi
    
    log_info "更新配置项: $xmlname / $itemname"
    
    # URL 编码 XML
    local encoded_xml
    if command -v python3 >/dev/null 2>&1; then
        encoded_xml=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$xml'''))")
    else
        encoded_xml=$(echo "$xml" | sed 's/&/%26/g; s/</%3C/g; s/>/%3E/g; s/"/%22/g')
    fi
    
    send_request "method=updateConfItem&xmlname=${xmlname}&itemname=${itemname}&xml=${encoded_xml}"
}

# 删除配置项
do_delete_conf_item() {
    local xmlname="$1"
    local itemname="$2"

    if [ -z "$xmlname" ] || [ -z "$itemname" ]; then
        log_error "缺少参数"
        echo "用法: $0 deleteConfItem <xmlname> <itemname>"
        exit 1
    fi

    log_info "删除配置项: $xmlname / $itemname"
    send_request "method=deleteConfItem&xmlname=${xmlname}&itemname=${itemname}"
}

# 获取数据库表列表
do_get_tables() {
    local db="$1"
    local filter="$2"
    local output="$3"

    if [ -z "$db" ]; then
        log_error "缺少 db 参数"
        echo "用法: $0 getTables <db> [filter] [output]"
        echo "  db: 数据库连接名称"
        echo "  filter: 表名过滤（可选，支持 % 通配符）"
        echo "  output: xml (默认) 或 json"
        exit 1
    fi

    # 默认输出格式为 xml
    if [ -z "$output" ]; then
        output="xml"
    fi

    local params="method=getTables&db=${db}&output=${output}"
    if [ -n "$filter" ]; then
        params="${params}&filter=${filter}"
    fi

    log_info "获取数据库表列表: $db (格式: $output)"
    send_request "$params"
}

# 获取表结构详情
do_get_table() {
    local db="$1"
    local tablename="$2"
    local output="$3"

    if [ -z "$db" ] || [ -z "$tablename" ]; then
        log_error "缺少参数"
        echo "用法: $0 getTable <db> <tablename> [output]"
        echo "  db: 数据库连接名称"
        echo "  tablename: 表名"
        echo "  output: xml (默认) 或 json"
        exit 1
    fi

    # 默认输出格式为 xml
    if [ -z "$output" ]; then
        output="xml"
    fi

    log_info "获取表结构: $db / $tablename (格式: $output)"
    send_request "method=getTable&db=${db}&tablename=${tablename}&output=${output}"
}

# 获取表数据（支持分页）
do_get_table_data() {
    local db="$1"
    local tablename="$2"
    local where="$3"
    local output="$4"
    local page="$5"
    local pagesize="$6"

    if [ -z "$db" ] || [ -z "$tablename" ]; then
        log_error "缺少参数"
        echo "用法: $0 getTableData <db> <tablename> [where] [output] [page] [pagesize]"
        echo "  db: 数据库连接名称"
        echo "  tablename: 表名"
        echo "  where: WHERE 条件（可选，不含 WHERE 关键字）"
        echo "  output: json (默认), xml 或 csv"
        echo "  page: 页码（默认 1）"
        echo "  pagesize: 每页记录数（默认 10，最大 100）"
        echo ""
        echo "示例:"
        echo "  $0 getTableData work ADM_USER"
        echo "  $0 getTableData work ADM_USER '' json"
        echo "  $0 getTableData work ADM_USER '' json 2 20"
        exit 1
    fi

    # 默认值
    [ -z "$output" ] && output="json"
    [ -z "$page" ] && page=1
    [ -z "$pagesize" ] && pagesize=10

    local params="method=getTableData&db=${db}&tablename=${tablename}&output=${output}&page=${page}&pagesize=${pagesize}"
    if [ -n "$where" ]; then
        # URL 编码 where 条件
        local encoded_where
        encoded_where=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$where'''))" 2>/dev/null || echo "$where")
        params="${params}&where=${encoded_where}"
    fi

    log_info "获取表数据: $db / $tablename (页: $page, 每页: $pagesize, 格式: $output)"
    send_request "$params"
}

# 获取帮助（不需要认证）
do_help() {
    log_info "获取 API 帮助..."
    curl -s "${API_BASE_URL}?method=help" | format_json
}

# ==================== 主程序 ====================

VERBOSE="false"

# 解析参数
while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)
            print_help
            exit 0
            ;;
        -v|--verbose)
            VERBOSE="true"
            shift
            ;;
        -u|--url)
            API_BASE_URL="$2"
            shift 2
            ;;
        -l|--login)
            API_LOGIN_ID="$2"
            shift 2
            ;;
        -p|--password)
            API_PASSWORD="$2"
            shift 2
            ;;
        -m|--mode)
            AUTH_MODE="$2"
            shift 2
            ;;
        --hmac)
            AUTH_MODE="hmac"
            shift
            ;;
        --simple)
            AUTH_MODE="simple"
            shift
            ;;
        -*)
            log_error "未知选项: $1"
            print_help
            exit 1
            ;;
        *)
            break
            ;;
    esac
done

# 检查依赖
check_dependencies

# 获取命令
COMMAND="$1"
shift || true

# 执行命令
case "$COMMAND" in
    login)
        do_login "$@"
        ;;
    logout)
        do_logout
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
    updateConfItem)
        do_update_conf_item "$@"
        ;;
    deleteConfItem)
        do_delete_conf_item "$@"
        ;;
    getTables)
        do_get_tables "$@"
        ;;
    getTable)
        do_get_table "$@"
        ;;
    getTableData)
        do_get_table_data "$@"
        ;;
    help)
        do_help
        ;;
    "")
        print_help
        ;;
    *)
        log_error "未知命令: $COMMAND"
        print_help
        exit 1
        ;;
esac