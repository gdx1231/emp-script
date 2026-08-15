#!/usr/bin/env bash
# 一次性构建 javax（默认）和 jakarta 两个产物
set -euo pipefail

cd "$(dirname "$0")"

STASH=$(mktemp -d)
trap 'rm -rf "$STASH"' EXIT

echo "===== [1/2] building javax (default) ====="
mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true "$@"
cp target/*.jar "$STASH/"

echo ""
echo "===== [2/2] building jakarta (-Pjakarta) ====="
mvn clean install -Pjakarta -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true "$@"
cp "$STASH"/*.jar target/

echo ""
echo "===== 完成，产物： ====="
ls -1 target/*.jar | grep -vE 'sources|javadoc'
