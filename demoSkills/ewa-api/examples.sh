#!/bin/bash
#
# EWA API 示例脚本
# 演示如何使用 EWA API 进行常见操作
#

set -e

# 获取脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/ewa-api.conf.example"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}EWA API 示例脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# ==================== 示例 1: 登录 ====================
echo -e "${GREEN}示例 1: 登录获取 Token${NC}"
echo "命令: ./ewa-api.sh login \$EWA_API_LOGIN_ID \$EWA_API_PASSWORD"
echo ""

# ==================== 示例 2: 获取帮助 ====================
echo -e "${GREEN}示例 2: 获取 API 帮助${NC}"
echo "命令: ./ewa-api.sh help"
echo ""

# ==================== 示例 3: 获取数据库表列表 ====================
echo -e "${GREEN}示例 3: 获取数据库表列表${NC}"
echo "命令: ./ewa-api.sh getTables \$EWA_DEFAULT_DB \"ADM_%\" json"
echo "说明: 获取以 ADM_ 开头的表"
echo ""

# ==================== 示例 4: 获取表结构 ====================
echo -e "${GREEN}示例 4: 获取表结构${NC}"
echo "命令: ./ewa-api.sh getTable \$EWA_DEFAULT_DB ADM_USER json"
echo "说明: 获取 ADM_USER 表的结构信息"
echo ""

# ==================== 示例 5: 获取表数据 ====================
echo -e "${GREEN}示例 5: 获取表数据 (JSON 格式)${NC}"
echo "命令: ./ewa-api.sh getTableData \$EWA_DEFAULT_DB ADM_USER \"\" json"
echo "说明: 获取 ADM_USER 表的前 10 条数据"
echo ""

# ==================== 示例 6: 获取表数据 (CSV 格式) ====================
echo -e "${GREEN}示例 6: 获取表数据 (CSV 格式)${NC}"
echo "命令: ./ewa-api.sh getTableData \$EWA_DEFAULT_DB ADM_USER \"\" csv"
echo "说明: 获取 ADM_USER 表的前 10 条数据，CSV 格式输出"
echo ""

# ==================== 示例 7: 带 WHERE 条件查询 ====================
echo -e "${GREEN}示例 7: 带 WHERE 条件查询${NC}"
echo "命令: ./ewa-api.sh getTableData \$EWA_DEFAULT_DB ADM_USER \"ADM_ID<10\" json"
echo "说明: 查询 ADM_ID < 10 的记录"
echo ""

# ==================== 示例 8: 获取配置项 ====================
echo -e "${GREEN}示例 8: 获取配置项${NC}"
echo "命令: ./ewa-api.sh getConfItem \"/meta-data/services/ser_main.xml\" \"SER_MAIN_CAT.T.Modify\" json"
echo "说明: 获取指定配置项的内容"
echo ""

# ==================== 示例 9: 注销 ====================
echo -e "${GREEN}示例 9: 注销 Token${NC}"
echo "命令: ./ewa-api.sh logout"
echo "说明: 撤销当前 Token"
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}提示: 请先修改 ewa-api.conf.example 为 ewa-api.conf 并配置正确的参数${NC}"
echo -e "${BLUE}========================================${NC}"