---
name: ewa-api
description: "Use when: 调用 EWA Servlet API、查询数据库表结构与数据、读取或修改 EWA XML 配置项、需要脚本化执行 login/getTables/getTable/getTableData/getConfItem/previewBusinessXml/createBusinessXml。"
---

# EWA API Skill

调用 EWA Servlet API 的技能，用于配置管理、数据库查询和业务 XML 生成。

## 适用场景

- 登录并缓存 API token。
- 查询数据库表清单、表结构和样例数据。
- 读取、更新、复制或删除配置 XML 与配置项。
- 从数据库表一键生成业务 XML（列表/表单/树形）。
- 在 shell 中重复执行标准 API 命令。

## 技能资产

- `ewa-api.sh` — Linux/macOS 包装脚本（入口）
- `shell/call-ewa-api.sh` — Linux/macOS 主调用脚本
- `shell/call-ewa-api.bat` — Windows 主调用脚本
- `ewa-api.conf.example` — 配置模板（复制为 `ewa-api.conf` 后使用）
- `ewa-api.md` — 完整方法参考文档
- `examples.sh` — 常用示例

## 快速开始

```bash
cp ewa-api.conf.example ewa-api.conf   # 填写 URL / 登录名 / 密码
source ewa-api.conf
./ewa-api.sh login
./ewa-api.sh getTables work "ADM_%" json
```

## 全部方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `login` | `[login_id] [password]` | 登录获取 Token |
| `logout` | — | 注销 Token |
| `help` | — | 显示 API 帮助（无需认证） |
| `getConfXml` | `<xmlname> [output]` | 获取完整配置文件 |
| `getConfItem` | `<xmlname> <itemname> [output]` | 获取单个配置项 |
| `runConfItem` | `<xmlname> <itemname> <new_itemname>` | 复制配置项 |
| `updateConfItem` | `<xmlname> <itemname> <xml>` | 更新配置项 |
| `deleteConfItem` | `<xmlname> <itemname>` | 删除配置项 |
| `getTables` | `<db> [filter] [output]` | 获取数据库表列表 |
| `getTable` | `<db> <tablename> [output]` | 获取表结构 |
| `getTableData` | `<db> <tablename> [where] [output] [page] [pagesize]` | 获取表数据（分页，默认10条） |
| `previewBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> [output]` | 预览业务 XML（不保存） |
| `createBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> <itemname> [admid]` | 生成并保存业务 XML |

**frametype**: `ListFrame` \| `Frame` \| `Tree`  
**operationtype**: `N`（新增）\| `M`（修改）\| `V`（查看）\| `NM`（新增+修改）

## 常用示例

```bash
./ewa-api.sh help
./ewa-api.sh getTable work ADM_USER json
./ewa-api.sh getTableData work ADM_USER "" json
./ewa-api.sh getConfItem "/meta-data/services/ser_main.xml" "SER_MAIN_CAT.T.Modify" json
./ewa-api.sh previewBusinessXml work MY_TABLE ListFrame V ewa/m json
./ewa-api.sh createBusinessXml work MY_TABLE Frame NM ewa/m MY_TABLE.F.NM
```

## 认证说明（HMAC 模式）

```
签名字符串 = METHOD + "\n" + TIMESTAMP + "\n" + NONCE + "\n" + PATH + "\n" + SORTED_QUERY_PARAMS
签名值     = lowercase(HMAC-SHA256(secret, 签名字符串))

请求头:
  X-Api-Key:       登录 ID
  X-Api-Timestamp: 当前毫秒时间戳
  X-Api-Nonce:     随机字符串（防重放）
  X-Api-Signature: 签名值
```

## 注意事项

- `description` 为技能发现入口，触发关键词需保留在 frontmatter 中。
- `name` 必须与目录名一致，避免技能静默失效。
- 不要在仓库中提交真实密码或生产 token。
