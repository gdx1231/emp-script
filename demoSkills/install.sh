#!/bin/bash
# ============================================================================
# EWA Skill 安装脚本
# ============================================================================
#
# 将 demoSkills/ 下的所有 EWA skill 以软链接方式安装到指定的 skills 目录，
# 实现「改 demoSkills 一处，所有项目生效」。
#
# 用法：
#   cd <目标 skills 目录> && /path/to/demoSkills/install.sh
#
#   示例：
#     cd /path/to/project/.qwen/skills && /path/to/demoSkills/install.sh
#     cd /path/to/project/.claude/skills && /path/to/demoSkills/install.sh
#     cd /path/to/project/.cursor/skills && /path/to/demoSkills/install.sh
#
# 安装规则：
#   1. 普通 skill（ewa-form, ewa-listframe 等）
#      → 整个目录创建软链接指向 demoSkills 源目录
#      → 已存在则跳过，不覆盖
#
#   2. ewa-api（含项目特有配置）
#      → 创建真实目录，内部文件逐一软链接
#      → ewa-api.conf 是项目特有配置（含数据库地址等），不会从源目录覆盖
#        - 首次安装：从 ewa-api.conf.example 复制一份
#        - 已存在：保留原文件不动
#
# 目录结构：
#   demoSkills/                  ← 源目录（本脚本所在位置）
#     install.sh                 ← 本脚本
#     ewa-api/                   ← 含项目特有配置，逐文件软链接
#       SKILL.md
#       ewa-api.conf.example     ← 配置模板
#       ewa-api.sh
#       shell/
#     ewa-form/                  ← 纯知识，整目录软链接
#       SKILL.md
#     ewa-listframe/
#     ewa-sql/
#     html-control/
#
# ============================================================================

set -e

DEMO_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_DIR="$(pwd)"

if [ "$DEMO_DIR" = "$TARGET_DIR" ]; then
  echo "错误：不能在 demoSkills 自身目录下运行，请先 cd 到目标 skills 目录"
  exit 1
fi

dir_name=$(basename "$TARGET_DIR")
if [ "$dir_name" != "skills" ]; then
  echo "警告：当前目录不是 skills 目录（$TARGET_DIR）"
  read -p "继续安装？(y/N) " confirm
  [ "$confirm" = "y" ] || exit 0
fi

for skill_dir in "$DEMO_DIR"/*/; do
  skill=$(basename "$skill_dir")
  target="$TARGET_DIR/$skill"

  if [ "$skill" = "ewa-api" ]; then
    # ewa-api 特殊处理：真实目录 + 文件级软链接
    if [ -L "$target" ]; then
      rm "$target"
    fi
    mkdir -p "$target"

    for f in "$skill_dir"/*; do
      base=$(basename "$f")
      # 跳过子目录，后面单独处理
      [ -d "$f" ] && continue
      if [ "$base" = "ewa-api.conf" ]; then
        # conf 有则不替换，没有则从 example 复制
        if [ ! -f "$target/ewa-api.conf" ]; then
          cp "$skill_dir/ewa-api.conf.example" "$target/ewa-api.conf"
          echo "  [ewa-api] 创建 ewa-api.conf（从 example 复制）"
        else
          echo "  [ewa-api] ewa-api.conf 已存在，跳过"
        fi
      else
        rm -f "$target/$base"
        ln -s "$f" "$target/$base"
        echo "  [ewa-api] $base -> 软链接"
      fi
    done

    # 子目录（如 shell/）也整体软链接
    for f in "$skill_dir"/*/; do
      [ -d "$f" ] || continue
      base=$(basename "$f")
      rm -rf "$target/$base"
      ln -s "$f" "$target/$base"
      echo "  [ewa-api] $base/ -> 软链接"
    done

  else
    # 普通 skill：整目录软链接
    if [ -L "$target" ]; then
      echo "[$skill] 已是软链接，跳过"
    elif [ -d "$target" ]; then
      echo "[$skill] 已存在为真实目录，跳过（如需重建请先删除）"
    else
      ln -s "$skill_dir" "$target"
      echo "[$skill] -> 整目录软链接"
    fi
  fi
done

echo ""
echo "安装完成。"
