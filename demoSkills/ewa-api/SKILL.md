---
name: ewa-api
description: "Use when: 调用 EWA Servlet API、查询数据库表结构与数据、读取或修改 EWA XML 配置项、需要脚本化执行 login/getTables/getTable/getTableData/getConfItem。"
---

# EWA API Skill

调用 EWA Servlet API 的技能，用于配置管理和数据库查询。

## 适用场景

- 登录并缓存 API token。
- 查询数据库表清单、表结构和样例数据。
- 读取配置 XML 与配置项。
- 在 shell 中重复执行标准 API 命令。

## 技能资产

- `ewa-api.sh`: Linux/macOS 包装脚本。
- `shell/call-ewa-api.sh`: Linux/macOS 主调用脚本。
- `shell/call-ewa-api.bat`: Windows 主调用脚本。
- `ewa-api.conf.example`: 配置模板。
- `examples.sh`、`examples.bat`: 示例命令。
- `README.md`、`ewa-api.md`: 说明文档。

## 快速开始

1. 复制并填写配置。
2. 加载配置并登录。
3. 执行 API 查询。

```bash
cp ewa-api.conf.example ewa-api.conf
source ewa-api.conf
./ewa-api.sh login
./ewa-api.sh getTables work "ADM_%" json
```

## 常见命令

```bash
./ewa-api.sh help
./ewa-api.sh getTable work ADM_USER json
./ewa-api.sh getTableData work ADM_USER "" json
./ewa-api.sh getConfItem "/meta-data/services/ser_main.xml" "SER_MAIN_CAT.T.Modify" json
```

## 注意事项

- `description` 为技能发现入口，触发关键词需保留在 frontmatter 中。
- `name` 必须与目录名一致，避免技能静默失效。
- 不要在仓库中提交真实密码或生产 token。
