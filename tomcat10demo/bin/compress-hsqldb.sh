#!/bin/bash
# 压缩 HSQLDB 数据文件为 hsqldb-data.zip
# 运行时文件（.lck, .log, .tmp）不会被包含在压缩包中

cd "$(dirname "$0")/.."
APP_DIR=$(pwd)
DATA_DIR="hsqldb"
ZIP_FILE="$APP_DIR/hsqldb-data.zip"

if [ ! -d "$DATA_DIR" ]; then
    echo "错误: HSQLDB 数据目录不存在: $DATA_DIR"
    echo "请先启动服务生成数据文件"
    exit 1
fi

echo "压缩 HSQLDB 数据文件..."
echo "  源目录: $DATA_DIR"
echo "  输出文件: $ZIP_FILE"

# 计算压缩前大小（macOS/Linux 兼容）
ORIG_SIZE=$(du -sh "$DATA_DIR" | awk '{print $1}')

# 删除旧的压缩包
rm -f "$ZIP_FILE"

# 压缩，排除运行时文件，使用最大压缩率
zip -9 -r "$ZIP_FILE" "$DATA_DIR" \
    -x "hsqldb/*.lck" \
    -x "hsqldb/*.log" \
    -x "hsqldb/*/"

# 检查压缩是否成功
if [ ! -f "$ZIP_FILE" ]; then
    echo "压缩失败"
    exit 1
fi

# 计算压缩后大小
ZIP_SIZE=$(du -sh "$ZIP_FILE" | awk '{print $1}')

# 计算压缩率
ORIG_BYTES=$(du -sk "$DATA_DIR" | awk '{print $1}')
ZIP_BYTES=$(du -sk "$ZIP_FILE" | awk '{print $1}')
if [ "$ORIG_BYTES" -gt 0 ]; then
    RATIO=$(( (100 * (ORIG_BYTES - ZIP_BYTES)) / ORIG_BYTES ))
else
    RATIO=0
fi

echo ""
echo "完成!"
echo "  压缩前: $ORIG_SIZE"
echo "  压缩后: $ZIP_SIZE"
echo "  压缩率: ${RATIO}%"
