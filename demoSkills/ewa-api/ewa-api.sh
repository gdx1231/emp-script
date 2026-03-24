#!/bin/bash
#
# EWA API 包装脚本
# 用于快速调用 EWA Servlet API
#
# 用法: ./ewa-api.sh <method> [params...]
#

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 实际脚本路径
REAL_SCRIPT="${SCRIPT_DIR}/shell/call-ewa-api.sh"

# 检查脚本是否存在
if [ ! -f "$REAL_SCRIPT" ]; then
    echo "[ERROR] 找不到 call-ewa-api.sh"
    echo "请确保文件存在: $REAL_SCRIPT"
    exit 1
fi

# 传递所有参数给实际脚本
exec "$REAL_SCRIPT" "$@"