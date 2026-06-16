---
name: ewa-api
description: "Use when: 调用 EWA Servlet API、查询数据库表结构与数据、读取或修改 EWA XML 配置项、生成业务 XML（自动创建容器）、查看配置存储路径（showScriptPaths）、直接读取 XML 文件（getXmlFile）、需要脚本化执行 login/getTables/getTable/getTableData/getConfItem/previewBusinessXml/createBusinessXml/showScriptPaths/getXmlFile。"
---

# EWA API Skill

调用 EWA Servlet API 的技能，用于配置管理、数据库查询和业务 XML 生成。

## 适用场景

- 登录并缓存 API token。
- 查询数据库表清单、表结构和样例数据。
- 读取、更新、复制或删除配置 XML 与配置项。
- 从数据库表一键生成业务 XML（列表/表单/树形），容器不存在时自动创建。
- 查看可用配置存储路径（JDBC / File）。
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
| `previewBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> [output] [scriptpath]` | 预览业务 XML（不保存，容器不存在自动创建） |
| `createBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> <itemname> [admid] [scriptpath]` | 生成并保存业务 XML，容器不存在自动创建 |
| `showScriptPaths` | — | 列出所有可用配置存储路径 |
| `getXmlFile` | `<xmlname> [output] [scriptpath]` | 直接读取 XML 文件内容（File/JDBC） |

**frametype**: `ListFrame` \| `Frame` \| `Tree`  
**operationtype**: `N`（新增）\| `M`（修改）\| `V`（查看）\| `NM`（新增+修改）

## 新增参数

**`scriptpath`**（可选）— 指定配置存储路径名称（如 `pf`）。用于 `createBusinessXml` / `previewBusinessXml`：

```bash
# 指定保存到 pf（jdbc:ewa2023）
./ewa-api.sh createBusinessXml globalTravel MY_TABLE Frame NM ewa/m MY_TABLE.F.NM pf
```

- 不提供：自动在第一个可写路径创建（跳过 `resources:` 和只读路径）
- 提供但路径不存在：报错并提示路径名
- 路径名称通过 `showScriptPaths` 查询

## Auto-create 机制

`createBusinessXml` / `previewBusinessXml` 在目标 xmlName 容器不存在时：

| 存储模式 | 行为 |
|----------|------|
| JDBC（`jdbc:`） | `INSERT INTO EWA_CFG_TREE` + `INSERT INTO EWA_CFG` 空白容器 |
| File（磁盘路径） | 创建目录 + 写入 `<?xml version="1.0"?><EasyWebTemplates />` |

## 常用示例

```bash
./ewa-api.sh help
./ewa-api.sh showScriptPaths                         # 列出可用配置存储路径
./ewa-api.sh getXmlFile /test/ser_tag.xml xml        # 直接读取 XML 文件
./ewa-api.sh getXmlFile /test/ser_tag.xml xml pf     # 指定路径读取
./ewa-api.sh getTable work ADM_USER json
./ewa-api.sh getTableData work ADM_USER "" json
./ewa-api.sh getConfItem "/meta-data/services/ser_main.xml" "SER_MAIN_CAT.T.Modify" json
./ewa-api.sh previewBusinessXml work MY_TABLE ListFrame V ewa/m json           # 不指定路径→自动创建
./ewa-api.sh previewBusinessXml work MY_TABLE ListFrame V ewa/m json pf       # 指定路径 pf
./ewa-api.sh createBusinessXml work MY_TABLE Frame NM ewa/m MY_TABLE.F.NM     # 不指定路径→自动创建
./ewa-api.sh createBusinessXml work MY_TABLE Frame NM ewa/m MY_TABLE.F.NM pf  # 指定路径 pf
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

## updateConfItem 大 XML 更新（Fetch → Modify → Push）

URL 参数过长会触发 Nginx 414 错误，大 XML（> 2KB）需通过 POST body 提交。

### Step 1: Fetch — 获取配置项

```bash
./ewa-api.sh --simple getConfItem "/business/ai/ai_chat.xml" "ITEM.NAME" xml 2>/dev/null > /tmp/item.xml
```

**关键陷阱**：
- `[INFO]` 行带有 ANSI 颜色代码，必须用 `2>/dev/null` 重定向
- 响应是 JSON 包裹，XML 在 `"XML"` 字段（不是 `"DATA"`）
- JSON 中 `/` 被转义为 `\/`，解析后需 `.replace('\\/', '/')`

### Step 2: Modify — 解析并修改 XML

```bash
python3 << 'PYEOF'
import json, re

with open('/tmp/item.xml', 'rb') as f:
    raw = f.read()

# 跳过 ANSI 行
idx = raw.index(b'{')
d = json.loads(raw[idx:])

# 提取 XML（在 "XML" 字段），反转义斜杠
xml = d['XML'].replace('\\/', '/')

# 修改示例：更新 SQL
xml = xml.replace('WHERE 1=1', 'WHERE 1=1 AND ai_id = @ai_id', 1)

# 修改示例：更新 JS（CDATA 内）
cdata_start, cdata_end = '<![CDATA[', ']]>'
idx_start = xml.index(cdata_start) + len(cdata_start)
idx_end = xml.index(cdata_end)
xml = xml[:idx_start] + '新 JS 代码' + xml[idx_end:]

with open('/tmp/item_updated.xml', 'w') as f:
    f.write(xml)
PYEOF
```

### Step 3: Push — curl POST body 提交

方式一：`--data-urlencode` 文件模式
```bash
source ewa-api.conf
curl -s -X POST \
  "${EWA_API_URL}?method=updateConfItem&xmlname=/business/ai/ai_chat.xml&itemname=ITEM.NAME" \
  -H "X-Api-Token: $(cat /tmp/.ewa_api_token)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "xml@/tmp/item_updated.xml"
```

方式二：手动 URL 编码
```bash
source ewa-api.conf
ENCODED_XML=$(python3 -c "import urllib.parse; print(urllib.parse.quote(open('/tmp/item_updated.xml').read(), safe=''))")
curl -s -X POST \
  "${EWA_API_URL}?method=updateConfItem&xmlname=/business/ai/ai_chat.xml&itemname=ITEM.NAME" \
  -H "X-Api-Token: $(cat /tmp/.ewa_api_token)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "xml=${ENCODED_XML}"
```

**成功响应**：`{"MSG":"Item updated successfully","RST":true,...}`

**常见陷阱**：

| 陷阱 | 解决 |
|------|------|
| ANSI 颜色代码导致 JSON 解析失败 | `2>/dev/null` 或 `raw.index(b'{')` 跳过 |
| XML 在 `d['XML']` 不是 `d['DATA']` | 使用 `d['XML']` |
| JSON 中 `/` 转义为 `\/` | `.replace('\\/', '/')` |
| Token 过期（401） | 重新 `./ewa-api.sh login` |
| 缺失 Content-Type | 必须设 `application/x-www-form-urlencoded` |

## 注意事项

- `description` 为技能发现入口，触发关键词需保留在 frontmatter 中。
- `name` 必须与目录名一致，避免技能静默失效。
- 不要在仓库中提交真实密码或生产 token。
