---
name: ewa-api
description: "Use when: 调用 EWA Servlet API、查询数据库表结构与数据、读取或修改 EWA XML 配置项、生成业务 XML（自动创建容器）、查看配置存储路径（showScriptPaths）、直接读取 XML 文件（getXmlFile）、校验 SQL 语法（validateSql）、需要脚本化执行 login/getTables/getTable/getTableData/getConfItem/previewBusinessXml/createBusinessXml/showScriptPaths/getXmlFile/validateSql。"
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

## 修改 XML 时的 Skill 组合策略

修改 EWA XML 配置时，需组合多个 skill 协作：

| 步骤 | Skill | 作用 |
|------|-------|------|
| 1. 读取当前 XML | 本 skill | `getXmlFile` 或 `getConfItem` 获取配置内容 |
| 2. 理解领域知识 | 领域 skill | 按钮写法、表单字段、SQL 语法等 |
| 3. 安全更新 XML | 本 skill | Fetch-Modify-Push 流程（见下方） |

按修改类型选择领域 skill：

| 修改目标 | 领域 Skill | 查阅章节 |
|----------|-----------|----------|
| ListFrame 按钮、行操作、选中行、翻页、检索、展开行 | `ewa-listframe` | §14 新建/修改记录、§13 调用后端 Action |
| Form 表单字段、提交验证、对话框联动 | `ewa-form` | §1 表单生命周期、§2 字段操作 |
| SQL 查询、注释标记、条件控制、参数 | `ewa-sql` | §3 注释标记、§4 参数规则 |
| JSP/Java 页面渲染 | `html-control` | 典型模式 |

## 框架文档参考

遇到 API 认证、Servlet 路由等概念不确定时，读取框架文档获取权威解释：

| 文档 | 说明 |
|------|------|
| `docs/zhcn/API_USAGE.md` | ServletApi REST API 使用说明 |
| `docs/zhcn/API_TOKEN.md` | 三种认证模式（HMAC/JWT/Session） |
| `docs/zhcn/SERVLETS.md` | 14 个 Servlet 详解（URL 映射/处理逻辑） |

**自动触发**：当对 API 认证机制、Servlet 路由、请求处理流程等概念不确定时，先 `read_file` 对应框架文档再操作。

## 技能资产

- `ewa-api.sh` — Linux/macOS 包装脚本（入口）
- `shell/call-ewa-api.sh` — Linux/macOS 主调用脚本
- `shell/call-ewa-api.bat` — Windows 主调用脚本
- `ewa-api.conf.example` — 配置模板（复制为 `ewa-api.conf` 后使用）

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
| `validateSql` | `<db> <sql>` | 校验 SQL 语法安全性（在事务中执行并回滚，拒绝 DROP/DELETE without WHERE 等危险操作） |

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

### SQL 校验

```bash
./ewa-api.sh validateSql globalTravel "SELECT * FROM ADM_USER WHERE ADM_ID = 1"
./ewa-api.sh validateSql globalTravel "DROP TABLE ADM_USER"               # 会被拒绝
./ewa-api.sh validateSql globalTravel "DELETE FROM ADM_USER"              # 无 WHERE 会被拒绝
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

### Step 3: Push — 用 ewa-api.sh 提交

```bash
# 推荐：从文件读取（避免引号、特殊字符问题）
./ewa-api.sh updateConfItem "/business/ai/ai_chat.xml" "ITEM.NAME" "@/tmp/item_updated.xml"

# 或直接传 XML（仅适用于简单内容）
./ewa-api.sh updateConfItem "/business/ai/ai_chat.xml" "ITEM.NAME" "<xml>...</xml>"
```

`updateConfItem` 内部使用 POST body 提交，自动处理 URL 编码。`@file` 方式通过环境变量传递文件路径，避免 shell 引号嵌套问题。

**成功响应**：`{"MSG":"Item updated successfully","RST":true,...}`

**常见陷阱**：

| 陷阱 | 解决 |
|------|------|
| ANSI 颜色代码导致 JSON 解析失败 | `2>/dev/null` 或 `raw.index(b'{')` 跳过 |
| XML 在 `d['XML']` 不是 `d['DATA']` | 使用 `d['XML']` |
| JSON 中 `/` 转义为 `\/` | `.replace('\\/', '/')` |
| Token 过期（401） | 重新 `./ewa-api.sh login` |
| 缺失 Content-Type | 必须设 `application/x-www-form-urlencoded` |

## 补充表字段规则

当数据库表新增列后，需同步更新对应的 EWA XML 配置（LF.M 列表 + F.NM 表单）。

### 工作流程

```
getTable → 获取表全部字段
getConfItem → 获取 LF.M / F.NM 当前字段
对比差异 → 找出缺失字段
getConfItem → 读取 F.NM 的 OnNew SQL (INSERT)
根据 INSERT 判断自动赋值字段 → 排除无需表单填写的字段
补 XItem → 构建缺失字段的 XML 节点
F.NM 同步修改 INSERT / UPDATE SQL
updateConfItem → 推送
```

### F.NM 表单字段判断规则

读取 `OnNew SQL`（INSERT 语句）的 VALUES 子句，判断每个字段的赋值方式：

| 赋值方式 | 示例 | 是否需加入表单 |
|----------|------|:---:|
| `@sys_unid` | 自动生成 UUID | ❌ 不需要 |
| `'USED'` / 常量 | 自动设定状态 | ❌ 不需要 |
| `@SYS_DATE` | 自动取当前时间 | ❌ 不需要 |
| `@G_ADM_ID` | 自动取当前管理员 | ❌ 不需要 |
| `@字段名`（非系统变量） | 用户输入参数 | ✅ 需要 |

常见自动赋值字段（无需加入表单）：`*_UID`、`*_STATUS`、`*_CDATE`、`*_MDATE`、`ADM_ID`

### 构建 XItem

从已有字段取模板（`re.search` 提取完整 `<XItem>...</XItem>`），替换关键属性：

```python
template = match.group(1)  # XItem 内部内容
new_item = f'<XItem Name="新字段名">{template}</XItem>'  # 必须闭合！
new_item = re.sub(r'<Set Name="旧名"', '<Set Name="新名"', new_item)
new_item = re.sub(r'DataField="旧名"', 'DataField="新名"', new_item)
new_item = re.sub(r'<Set Info="[^"]*" Lang="zhcn"[^>]*>', 
                  '<Set Info="中文描述" Lang="zhcn" Memo=""/>', new_item)
new_item = re.sub(r'DataType="String"', 'DataType="目标类型"', new_item)
```

### 同步修改 F.NM 的 SQL

- **INSERT (OnNew SQL)**：columns 列表 + VALUES 列表都要加新字段
- **UPDATE (OnModify SQL)**：SET 子句加 `新字段 = @新字段`
- **SELECT**：通常用 `A.*` 无需改动

### 常见陷阱

| 陷阱 | 解决 |
|------|------|
| XItem 缺少 `</XItem>` 闭合 | 模板是 `(.*?)</XItem>` 不含闭合标签，需手动拼接 |
| XML 校验失败 | push 前用 `ET.fromstring(xml)` 验证 |
| 表单 Tag 选错 | 短文本用 `text`，长文本用 `textarea`，下拉用 `select` |
| money 字段未设精度 | Double 类型补 `NumberScale="4"` |
| `\/` 转义 | API 返回的 XML 中 `/` 被转义为 `\/`，编辑前先 `.replace('\\/', '/')` |

## 注意事项

- `description` 为技能发现入口，触发关键词需保留在 frontmatter 中。
- `name` 必须与目录名一致，避免技能静默失效。
- 不要在仓库中提交真实密码或生产 token。
