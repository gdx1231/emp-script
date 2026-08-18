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

> **⚠ ListFrame 没有 `NM` 模板** — `EwaDefine.xml` 中 `ListFrame` 只定义了 `V`（无按钮）和 `M`（含 5 个按钮：butNew/butModify/butCopy/butDelete/butRestore + OnFrameDelete/OnFrameRestore SQL + 回收站支持）。传 `NM` 时 `getTmpConfig` 返回 null，**按钮和 JS 全部丢失**。
>
> 正确用法：**ListFrame 用 `M`**，Frame 用 `NM`。
>
> ```bash
> # ✅ 正确 — ListFrame 带按钮
> ./ewa-api.sh createBusinessXml aic my_table ListFrame M /path/to/xml MY_TABLE.LF.M
> # ✅ 正确 — Frame 新增+修改
> ./ewa-api.sh createBusinessXml aic my_table Frame NM /path/to/xml MY_TABLE.F.NM
> # ❌ 错误 — ListFrame 没有 NM 模板，不会生成按钮
> ./ewa-api.sh createBusinessXml aic my_table ListFrame NM /path/to/xml MY_TABLE.LF.NM
> ```

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
./ewa-api.sh createBusinessXml work MY_TABLE ListFrame M ewa/m MY_TABLE.LF.M  # ListFrame 用 M（带按钮）
./ewa-api.sh createBusinessXml work MY_TABLE Frame NM ewa/m MY_TABLE.F.NM     # Frame 用 NM（不指定路径→自动创建）
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

用 `jq -r` 提取 XML，正确处理 JSON unicode 转义（`\uXXXX` → 中文字符）：

```bash
./ewa-api.sh getConfItem "/business/ai/ai_chat.xml" "ITEM.NAME" xml 2>/dev/null \
  | sed 's/\x1b\[[0-9;]*m//g' \
  | sed -n '/^{/,/^}/p' \
  | jq -r '.XML' > /tmp/item.xml
```

**管道说明**：
1. `2>/dev/null` — 去掉 `[INFO]` 日志行（含 ANSI 颜色代码）
2. `sed 's/\x1b\[...//g'` — 清除残留 ANSI 转义序列
3. `sed -n '/^{/,/^}/p'` — 只保留 JSON 对象行（跳过非 JSON 输出）
4. `jq -r '.XML'` — 正确解码 JSON（含 `\uXXXX` → 中文、`\"` → `"`、`\\` → `\`）

> **禁止用 sed 手动解码 JSON**（如 `s/\\"/"/g`），会破坏 JSON 转义结构，导致 `\uXXXX` 被当字面文本存入数据库，中文前出现残留反斜杠。

### Step 2: Modify — 解析并修改 XML

此时 `/tmp/item.xml` 已是纯 XML（UTF-8），可直接用 sed/perl 修改：

```bash
# 修改示例：替换 SQL Server 语法为 PostgreSQL
sed -e 's/getdate()/CURRENT_TIMESTAMP/g' \
    -e 's/ISNULL(/COALESCE(/g' \
    -e 's/isnull(/COALESCE(/g' \
    /tmp/item.xml > /tmp/item_updated.xml

# 修改示例：用 perl 替换 SQL
perl -pe 's/GETDATE\(\)/CURRENT_TIMESTAMP/g' /tmp/item.xml > /tmp/item_updated.xml
```

### Step 3: Push — 用 ewa-api.sh 提交

```bash
# 推荐：从文件读取（避免引号、特殊字符问题）
./ewa-api.sh updateConfItem "/business/ai/ai_chat.xml" "ITEM.NAME" "@/tmp/item_updated.xml"
```

`updateConfItem` 内部使用 POST body 提交，自动处理 URL 编码。`@file` 方式通过环境变量传递文件路径，避免 shell 引号嵌套问题。

**成功响应**：`{"MSG":"Item updated successfully","RST":true,...}`

### 批量更新整个配置文件的所有模板

当需要修改整个 XML 文件中所有模板的 SQL 语法时，逐个 getConfItem → modify → updateConfItem：

```bash
XMLNAME="/business/orgniziation/admin.xml"
for ITEM in "TPL_A" "TPL_B" "TPL_C"; do
  # Fetch（jq 正确解码 unicode）
  ./ewa-api.sh getConfItem "$XMLNAME" "$ITEM" xml 2>/dev/null \
    | sed 's/\x1b\[[0-9;]*m//g' | sed -n '/^{/,/^}/p' \
    | jq -r '.XML' > /tmp/tpl.xml

  # Modify
  sed -e 's/getdate()/CURRENT_TIMESTAMP/g' \
      -e 's/ISNULL(/COALESCE(/g' -e 's/isnull(/COALESCE(/g' \
      /tmp/tpl.xml > /tmp/tpl_pg.xml

  # Push
  ./ewa-api.sh updateConfItem "$XMLNAME" "$ITEM" "@/tmp/tpl_pg.xml"
done
```

### 修复 unicode 转义损坏

如果之前用 sed 错误提取 JSON 导致中文前出现 `\`（如 `\获取` 而非 `获取`），用 perl 去除中文前的残留反斜杠：

```bash
./ewa-api.sh getConfItem "$XMLNAME" "$ITEM" xml 2>/dev/null \
  | sed 's/\x1b\[[0-9;]*m//g' | sed -n '/^{/,/^}/p' \
  | jq -r '.XML' \
  | perl -CSD -pe 's/\\([\p{Han}])/$1/g' > /tmp/tpl_fixed.xml
```

**常见陷阱**：

| 陷阱 | 解决 |
|------|------|
| 用 sed 解码 JSON 导致 unicode 损坏 | 用 `jq -r '.XML'` 提取 |
| 中文前出现 `\`（如 `\获取`） | `perl -CSD -pe 's/\\([\p{Han}])/$1/g'` |
| ANSI 颜色代码导致 JSON 解析失败 | `sed 's/\x1b\[[0-9;]*m//g'` |
| XML 在 `.XML` 不是 `.DATA` | 使用 `jq -r '.XML'` |
| Token 过期（401） | 重新 `./ewa-api.sh login` |
| updateConfItem 要求单 `<EasyWebTemplate>` | getConfItem 返回的已是单个模板 |

## 补充表字段规则

当数据库表新增列后，需同步更新对应的 EWA XML 配置（LF.M 列表 + F.NM 表单）。

### 工作流程

```
getTable → 获取表全部字段
getConfItem + jq -r '.XML' → 获取 LF.M / F.NM 当前 XML
对比差异 → 找出缺失字段
getConfItem + jq -r '.XML' → 读取 F.NM 的 OnNew SQL (INSERT)
根据 INSERT 判断自动赋值字段 → 排除无需表单填写的字段
补 XItem → 构建缺失字段的 XML 节点
F.NM 同步修改 INSERT / UPDATE SQL
updateConfItem @file → 推送
```

> **注意**：所有 `getConfItem` 输出必须经 `jq -r '.XML'` 提取，不要用 sed 手动解码 JSON。

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

从已有字段取模板（用 sed/perl 提取完整 `<XItem>...</XItem>`），替换关键属性：

```bash
# 从现有 XML 提取模板并替换属性
sed -n '/<XItem Name="模板字段">/,/<\/XItem>/p' /tmp/tpl.xml \
  | sed 's/Name="模板字段"/Name="新字段名"/' \
  | sed 's/DataField="模板字段"/DataField="新字段名"/' \
  | sed 's/Info="[^"]*" Lang="zhcn"[^>]*>/Info="中文描述" Lang="zhcn" Memo="\"\/>/' \
  | sed 's/DataType="String"/DataType="目标类型"/'
```

**XItem 排序规则**：`textarea` / `ntext` 类型字段放在所有字段最后、按钮之前，避免大文本框撑开表单布局。

```bash
# 插入到 butOk 之前（textarea 自动在最后）
sed '/<XItem Name="butOk">/i\
'"$new_item"'
' /tmp/tpl.xml > /tmp/tpl_updated.xml
```

### 同步修改 F.NM 的 SQL

- **INSERT (OnNew SQL)**：columns 列表 + VALUES 列表都要加新字段
- **UPDATE (OnModify SQL)**：SET 子句加 `新字段 = @新字段`
- **SELECT**：通常用 `A.*` 无需改动

### 常见陷阱

| 陷阱 | 解决 |
|------|------|
| XItem 缺少 `</XItem>` 闭合 | sed 范围匹配 `/<XItem>/,/<\/XItem>/p` 包含闭合标签 |
| XML 校验失败 | push 前用 `xmllint --noout file.xml` 验证 |
| 表单 Tag 选错 | 短文本用 `text`，长文本用 `textarea`，下拉用 `select` |
| money 字段未设精度 | Double 类型补 `NumberScale="4"` |
| JSON unicode 损坏中文 | 用 `jq -r '.XML'` 提取，不要用 sed 手动解码 |
| updateConfItem 报"根元素应为 EasyWebTemplate" | **必须提交完整 `<EasyWebTemplate>` 文档**，不是 XItem 片段 |
| Python 正则替换后文档变小 | 用 `xml.replace(m.group(0), new_item, 1)` 只替换匹配块；**不要** `xml = m.group(1)+...+m.group(3)`（会把整个文档替换成捕获组片段） |
| call-ewa-api.sh 不处理 `@file` 参数 | 直接 curl：`curl -X POST "$EWA_API_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/x-www-form-urlencoded" --data-urlencode "method=updateConfItem" --data-urlencode "xmlname=..." --data-urlencode "itemname=..." --data-urlencode "xml@/tmp/file.xml"` |
| JSP tag `<%! %>` 方法调 `getCell` 报 Unhandled exception | 方法签名必须 `throws Exception` |

## DataRef 字典显示（LF.M 列表）

列表列把 code 显示为字典名（span 只读展示）：

```xml
<DataRef><Set RefKey="BAS_TAG" RefShow="BAS_TAG_NAME"
    RefSql="SELECT BAS_TAG, BAS_TAG_NAME FROM AIC_META.BAS_TAG WHERE BAS_TAG_GRP = 'MODEL_TYPE'"/></DataRef>
```

- `RefSql`：字典查询，结果必须含 key 列 + show 列
- `RefKey`：字典表 key 列（匹配源字段值）
- `RefShow`：显示列
- 框架用 `dt.getRowByKey(refKey, v)` 查行 → 显示 `refShow` 值（`ItemFormat.java` / `ItemValues.getRefTable`）

## select 下拉（F.NM 表单）

表单字段从字典生成下拉（`Tag="select"` + List）：

```xml
<XItem Name="APM_TYPE">
  <Tag><Set IsLFEdit="0" SpanShowAs="" Tag="select"/></Tag>
  <List><Set DisplayField="BAS_TAG_NAME" ValueField="BAS_TAG" ListAddBlank="1"
      Sql="SELECT BAS_TAG, BAS_TAG_NAME FROM AIC_META.BAS_TAG WHERE BAS_TAG_GRP = 'MODEL_TYPE'"/></List>
</XItem>
```

- `Sql`：字典查询
- `DisplayField`：下拉显示列；`ValueField`：提交值列（存 code 不存名）
- `ListAddBlank="1"`：首行空选项
- 有 `List.Sql` 时 DataRef 可留空（select 走 List，显示翻译走 DataRef，二选一即可）

## 批量更新 XML 配置项（新增表字段时）

1. `getConfItem` 拉取 LF.M 与 F.NM 的完整 XML（`jq -r '.XML'` 解码）
2. 用 `make_xitem()` Python 模板生成新字段 XItem（`DataField`/`DataType`/中文 Info）
3. LF.M：插到 `<XItem Name="butNew">` 前（`SELECT A.*` 自动带出新列）；只读列 Tag 用 `span`
4. F.NM：插到 `<XItem Name="butOk">` 前（textarea 类最后）；同步改 INSERT/UPDATE SQL
5. `xmllint` 校验后 curl push，回读 `getConfItem` 验证

## 注意事项

- `description` 为技能发现入口，触发关键词需保留在 frontmatter 中。
- `name` 必须与目录名一致，避免技能静默失效。
- 不要在仓库中提交真实密码或生产 token。
