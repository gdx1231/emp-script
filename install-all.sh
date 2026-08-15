#!/usr/bin/env bash
# 全量安装：utils → emp-script → web → static（各 javax + jakarta，按依赖顺序）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

for REPO in emp-script-utils emp-script emp-script-web emp-script-static; do
	if [ "$REPO" = "emp-script" ]; then
		DIR="$ROOT"
	else
		DIR="$ROOT/../$REPO"
	fi

	if [ ! -x "$DIR/build-all.sh" ]; then
		echo "错误：找不到 $DIR/build-all.sh" >&2
		exit 1
	fi

	echo "===== $REPO ====="
	(cd "$DIR" && ./build-all.sh "$@")
	echo ""
done

echo "===== 全部完成 ====="
