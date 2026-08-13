---
name: ewa-2-mode
description: "Use when: 创建 EWA AI Mode XML 配置 — 定义 AI 对话模式的完整结构（mode/step/prompt/tool/sql/action/ui），包括参数提取、工具调用、枚举注入、工作流等。"
trigger: ewa-2-mode, AI Mode, ai_mode.xml, ai_ewa.xml, mode XML, step, prompt, tool, sqlRef, action, EWA AI 配置
---

# EWA AI Mode XML 编写指南

AI Mode XML 定义 AI 对话模式的完整结构，包括提示词、工具调用、SQL 查询、动作处理和 UI。框架通过 `ChatManagerBase` 解析执行。

## 参考示例

| 文件 | 说明 |
|------|------|
| `src/main/resources/ai_ewa.xml` | CRM 客户管理（tool + api 模式） |
| `src/main/resources/ai_ewa_seal.xml` | 合同章管理（tool + sqlRef 枚举注入） |
| `src/main/resources/ai_draw.xml` | 团宣传画（prompt + action + sql 模式） |
| `src/main/resources/ai_chat_room_group.xml` | 团聊天室 |
| `src/main/resources/ai_mode.xml` | 通用 AI 模式 |

## 整体结构

```xml
<?xml version="1.0" encoding="UTF-8"?>
<modes>
  <mode name="MODE_NAME" description="描述" temperature="0.3" topP="0.3"
        thinking="true" responseFormat="json_object">

    <step name="step_name" description="步骤描述" stream="true">
      <prompts>...</prompts>
    </step>

    <tools>...</tools>
    <sqls>...</sqls>
    <actions>...</actions>
    <ui>...</ui>
  </mode>

  <common>
    <apis>...</apis>
  </common>
</modes>
```

## mode 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `name` | string | 模式唯一标识 |
| `description` | string | 模式描述 |
| `temperature` | float | LLM 温度（0.1=精确, 0.7=创意） |
| `topP` | float | 核采样 |
| `thinking` | boolean | 是否启用思考模式 |
| `responseFormat` | string | 响应格式：`json_object` / 空（自由文本） |
| `enableSearch` | boolean | 是否启用联网搜索 |

## step 元素

```xml
<step name="init" description="步骤描述" stream="true"
      action="actionName" actionSqlRef="sqlName" cachedSeconds="60">
  <prompts>...</prompts>
</step>
```

| 属性 | 说明 |
|------|------|
| `name` | 步骤唯一标识 |
| `stream` | 是否流式输出 |
| `action` | 步骤完成后执行的动作（引用 `<actions>` 中的 name） |
| `actionSqlRef` | 动作的 SQL 数据源（引用 `<sqls>` 中的 name） |
| `cachedSeconds` | 缓存秒数 |

## prompt 元素

prompt 是 AI 对话的消息单元，支持多种数据来源：

### 1. 静态内容（CDATA）

```xml
<prompt name="sys_prompt" role="system">
  <![CDATA[你是一位助手...]]>
</prompt>
```

### 2. SQL 注入（sqlRef）

```xml
<prompt name="enum_values" role="user"
    sqlRef="getEnums" dataType="json"
    prefix="枚举选项：" />
```

框架执行 `<sqls>` 中对应的 SQL，将结果设为 prompt 内容。

| 属性 | 说明 |
|------|------|
| `sqlRef` | 引用 `<sqls>` 中的 SQL name |
| `dataType` | 输出格式：`json`（默认 JSONArray）/ `csv` / `xml` |
| `dataGroupField` | JSON 分组字段（调用 `toJSONObjectGroup`） |
| `prefix` | 注入到 prompt 内容前的文本 |

**执行流程**（`Mode.createStepPromptBySql`）：
```
sqlRef → findSqlQueryByRef() → DTTable.getJdbcTable(sql, db, rv)
  → dataType=json → prompt.setContent(tb.toJSONArray().toString())
  → dataType=csv  → prompt.setContent(tb.toCSV())
  → dataType=xml  → prompt.setContent(tb.toXml(rv))
```

### 3. API 调用（api）

```xml
<prompt name="group_info" role="user"
    api="getGroupInfo"
    prefix="团队信息：" />
```

框架调用 `<common><apis>` 或 `<apis>` 中定义的 API，将返回内容设为 prompt。

### 4. 工具选择（toolsCheck）

```xml
<prompt name="checkMode" role="user" toolsCheck="true">
  <![CDATA[判断是否需要调用工具...]]>
</prompt>
```

`toolsCheck="true"` 标记此 prompt 用于工具选择判断。

### 5. 用户输入前缀

```xml
<prompt name="userInput" role="user" prefix="用户输入：" />
```

无 CDATA、无 sqlRef、无 api — 框架将用户实际输入附加到 prefix 后。

### prompt role 值

| role | 说明 |
|------|------|
| `system` | 系统提示词 |
| `user` | 用户消息（SQL/API 结果、用户输入） |

## tool 元素

工具定义 AI 可调用的外部操作：

```xml
<tool name="tool_name" description="工具描述"
    url="@EWA.HOST_BASE/ewa" parameters="PARAM1,PARAM2"
    refRequest="true" key="" timeout="5000" method="post">
  <form>
    <field name="XMLNAME" value="/path/to/config.xml" />
    <field name="ITEMNAME" value="ITEM.NAME" />
    <field name="FIELD1" value="@PARAM1" />
  </form>
  <![CDATA[tool_name(PARAM1: str, PARAM2: str): 工具描述]]>
</tool>
```

| 属性 | 说明 |
|------|------|
| `name` | 工具唯一标识 |
| `description` | 工具描述（AI 看到） |
| `url` | 请求 URL（支持 `@EWA.HOST_BASE` 变量） |
| `parameters` | 逗号分隔的参数名列表 |
| `refRequest` | 是否携带当前请求上下文 |
| `method` | HTTP 方法：`post` / `get` |
| `timeout` | 超时毫秒数 |
| `useMode` | 引用另一个 mode 作为工具（子模式调用） |

### form field 的 value 规则

| value 格式 | 说明 |
|------------|------|
| `固定值` | 直接发送该值 |
| `@参数名` | 从 AI 提取的参数中取值 |
| 空 | 不发送该字段 |

### CDATA 工具签名

CDATA 内容定义工具的函数签名，AI 据此生成调用参数：
```
tool_name(param1: type, param2: type): 描述
```

## sql 元素

SQL 定义可被 `prompt.sqlRef` 或 `step.actionSqlRef` 引用：

```xml
<sqls>
  <sql name="sqlName" description="描述">
    <![CDATA[
SELECT field1 AS V, field2 AS TT
FROM table_name
WHERE condition=@param
ORDER BY field1
    ]]>
  </sql>
</sqls>
```

SQL 中可使用 `@param` 参数（从 RequestValue 注入，如 `@G_SUP_ID`、`@G_ADM_ID`）。

### 枚举 SQL 命名约定

```
get{FieldName}Enums  — 获取字段枚举值
getLastResponse      — 获取最后一次 AI 回复
get{业务}Info        — 获取业务数据
```

### 枚举 SQL 输出格式

推荐输出 `V`（值）和 `TT`（显示名）两列：
```sql
SELECT BAS_TAG AS V, BAS_TAG_NAME AS TT FROM BAS_TAG WHERE ...
```

## action 元素

动作定义步骤完成后的处理逻辑：

```xml
<actions>
  <action name="actionName" description="描述"
      class="com.example.MyAction"
      aiProvider="doubao_img" aiModel="model-name" />
</actions>
```

| 属性 | 说明 |
|------|------|
| `name` | 动作唯一标识（被 `step.action` 引用） |
| `class` | Java 实现类（实现 `com.gdxsoft.ai.export.IAction`） |
| `aiProvider` | AI 提供商（如 `doubao_img`） |
| `aiModel` | AI 模型名 |

## api 元素

API 定义外部 HTTP 调用，可被 `prompt.api` 引用：

```xml
<common>
  <apis>
    <api name="apiName" description="描述"
        url="@EWA.HOST_BASE/path/to/endpoint"
        parameters="param1=@value1" refRequest="false"
        timeout="5000" method="get" />
  </apis>
</common>
```

| 属性 | 说明 |
|------|------|
| `name` | API 唯一标识 |
| `url` | 请求 URL |
| `parameters` | URL 参数（支持 `@param` 变量） |
| `refRequest` | 是否携带请求上下文 |
| `method` | HTTP 方法 |

## ui 元素

UI 定义前端展示：

```xml
<ui>
  <welcome><![CDATA[
<div class="welcome-banner">
  <h3>标题</h3>
  <p>描述</p>
</div>
  ]]></welcome>

  <complete test="@fullText like '%KEYWORD%'"><![CDATA[
<div>完成后的 HTML</div>
  ]]></complete>
</ui>
```

| 元素 | 说明 |
|------|------|
| `welcome` | 对话开始时的欢迎 HTML |
| `complete` | 满足条件时显示的完成 HTML |
| `complete.test` | 条件表达式（`@fullText like '%关键词%'`） |

### UI 中的变量

| 变量 | 说明 |
|------|------|
| `@sys_unid` | 系统唯一 ID（用于 DOM 元素） |
| `@request_id` | 当前请求 ID |
| `@fullText` | AI 完整回复文本 |
| `@EWA.HOST_BASE` | 系统基础 URL |

## 常见模式

### 模式 A：工具调用（CRM 类）

AI 提取参数 → 调用 tool → tool 调 EWA 接口 → 返回结果

```xml
<step name="init" stream="true">
  <prompts>
    <prompt name="sys" role="system"><![CDATA[系统指令...]]></prompt>
    <prompt name="check" role="user" toolsCheck="true"><![CDATA[判断工具...]]></prompt>
    <prompt name="input" role="user" prefix="用户输入：" />
  </prompts>
</step>
<tools>
  <tool name="create" url="@EWA.HOST_BASE/ewa" parameters="FIELD1,FIELD2" method="post">
    <form>
      <field name="EWA_MTYPE" value="N" />
      <field name="FIELD1" value="@FIELD1" />
    </form>
    <![CDATA[create(FIELD1: str, FIELD2: str): 创建记录]]>
  </tool>
</tools>
```

### 模式 B：SQL 枚举注入（表单类）

SQL 查枚举 → 注入 prompt → AI 知道可选值 → 调用 tool 提交

```xml
<step name="init" stream="true">
  <prompts>
    <prompt name="sys" role="system"><![CDATA[系统指令...]]></prompt>
    <prompt name="enum1" role="user" sqlRef="getEnum1" dataType="json"
        prefix="字段1枚举：" />
    <prompt name="enum2" role="user" sqlRef="getEnum2" dataType="json"
        prefix="字段2枚举：" />
    <prompt name="check" role="user" toolsCheck="true"><![CDATA[判断工具...]]></prompt>
  </prompts>
</step>
<sqls>
  <sql name="getEnum1"><![CDATA[SELECT V, TT FROM ...]]></sql>
  <sql name="getEnum2"><![CDATA[SELECT V, TT FROM ...]]></sql>
</sqls>
```

### 模式 C：动作执行（绘图类）

AI 生成内容 → step 完成后执行 action → action 处理结果

```xml
<step name="init" stream="true">
  <prompts>
    <prompt name="sys" role="system"><![CDATA[生成绘图提示词...]]></prompt>
    <prompt name="info" role="user" api="getInfo" prefix="数据：" />
  </prompt>
</step>
<step name="draw" action="drawAction" actionSqlRef="getLastResponse" />
<sqls>
  <sql name="getLastResponse"><![CDATA[SELECT ... FROM AI_CHAT_MSG ...]]></sql>
</sqls>
<actions>
  <action name="drawAction" class="com.example.DrawAction" />
</actions>
```

## EWA 接口参数速查

### ListFrame 查询（EWA_SEARCH）

```
格式：field[operator]value,field2[operator2]value2

操作符：
  [eq]   等于        [lk]   包含(like %val%)
  [gt]   大于        [lt]   小于
  [gte]  大于等于    [lte]  小于等于
  [or]   多值OR      [blk]  空白
  [nblk] 非空白      [nlk]  不包含
  [llk]  左包含      [rlk]  右包含
```

### Frame 表单提交

```
EWA_MTYPE=N     新建
EWA_MTYPE=M     修改
EWA_AJAX=1      AJAX 模式
EWA_POST=1      表单提交
EWA_NO_CONTENT=1 不返回 HTML
```

### 工作流

```
EWA_AJAX=WORKFLOW
EWA_WF_NAME=节点名
EWA_ACTION_KEY=主键值
EWA_WF_CTRL=0|1
EWA_WF_UOK=1|0     通过/拒绝
EWA_WF_UMSG=意见
```

### 删除

```
EWA_ACTION=OnFrameDelete
EWA_ACTION_KEY=主键值
```

## 配置读取与关联附加

### 读取配置

通过 ewa-api.sh 读取指定 XMLNAME + ITEMNAME 的配置：

```bash
# 读取单个配置项（纯 XML）
./ewa-api.sh getConfItem "/business/oa/seal.xml" "OA_SEAL_APPLY.ListFrame.Modify" xml 2>/dev/null \
  | sed 's/\x1b\[[0-9;]*m//g' | sed -n '/^{/,/^}/p' | jq -r '.XML'

# 读取整个配置文件
./ewa-api.sh getConfXml "/business/oa/seal.xml" xml 2>/dev/null \
  | sed 's/\x1b\[[0-9;]*m//g' | sed -n '/^{/,/^}/p' | jq -r '.XML'
```

### 关联引用类型

一个配置项通常引用其他配置，形成关联链。AI 需要理解这些引用才能完整掌握模块结构。

| 引用类型 | XML 位置 | 说明 | 提取方式 |
|----------|----------|------|----------|
| `DataRef.RefSql` | `XItem/DataRef/Set` | 下拉/搜索的选项来源 SQL | 读属性 `RefSql` |
| `List.Sql` | `XItem/List/Set` | select/radio/checkbox 选项来源 | 读属性 `Sql`、`ValueField`、`DisplayField` |
| `Frame.CallXmlName` | `XItem/Frame/Set` | 弹窗引用的外部 XML | 读属性 `CallXmlName` + `CallItemName` |
| `ActionSet → SqlSet` | `Action/ActionSet/Set` | 动作关联的 SQL 名称 | 读 `CallSet/Set` 的 `CallName` |
| JS `'@xmlname'` | `AddScript/Bottom` | JS 中动态打开的 Item | 正则提取 `'xxx.xml', 'ITEM.NAME'` |
| `DopListShow` | `XItem/DopListShow/Set` | 弹窗选择后的回调 | 读 `DlsAfterEvent`、`DlsShow` |
| `OrderSearch.SearchSql` | `XItem/OrderSearch/Set` | 搜索下拉的选项来源 | 读属性 `SearchSql` |

### 关联链示例（seal 模块）

```
OA_SEAL_APPLY.ListFrame.Modify（列表）
├── JS addNew() → OA_SEAL_APPLY.Frame.NewModify (EWA_MTYPE=N)
├── JS update() → OA_SEAL_APPLY.Frame.NewModify (EWA_MTYPE=M)
├── JS upfile() → OA_SEAL.Pic.UP（文件上传）
├── JS open_doc() → /robert/v.jsp?tb=OA_SEAL_APPLY（查看文件）
├── JS ext_print() → /back_admin/doc/doc.jsp?DOC_TMP_TAG=CONTRACT_APPROVE（打印）
├── XItem SEAL_DOC onclick → open_doc(@apply_id)
├── XItem butPrint onclick → ext_print(@APPLY_ID)
├── XItem CRM_COM_ID → Frame.CallXmlName: |business/common/droplist.xml / CRM_COM.LF.V
├── XItem REF_GRP_ID0/1/2 → Frame.CallXmlName: |business/common/droplist.xml / dl_grp_main
├── DataRef → SEAL_TYPE/AUDIT_STATUS/APPLY_TITLE 的 RefSql
├── ActionSet → OnPageLoad SQL / OnFrameDelete SQL / ExtendAction0-2 SQL
└── Workflow → SEAL_APPLY → SEAL_DEP_CHECK → SEAL_LEADER_CHECK → SEAL_SIGN → SEAL_OK
```

### JS 中关联配置的提取方法

JS 代码在 `<AddScript><Set><Bottom><![CDATA[...]]></Bottom></Set></AddScript>` 中。

扫描 429 个 XML 文件，发现 4 种主要 JS 配置调用模式：

**模式 1：Dialog 打开同文件 Item（293 次 / 93 文件）**
```javascript
// '@xmlname' 表示当前 XML 文件，第三个参数是关联的 ITEMNAME
EWA.UI.Dialog.OpenReloadClose('@SYS_FRAME_UNID',
    '@xmlname', 'OA_SEAL_APPLY.Frame.NewModify', false, paras);
// → 关联：同文件下的 OA_SEAL_APPLY.Frame.NewModify
```
提取正则：
```python
re.findall(r"(?:OpenReloadClose|Dialog\.Open)\s*\(\s*'@SYS_FRAME_UNID'\s*,\s*'@xmlname'\s*,\s*'([^']+)'", js)
```

**模式 2：Dialog 打开跨文件 Item（472 次 / 97 文件）**
```javascript
// 第一个参数是显式 XML 路径，第二个是 ITEMNAME
EWA.UI.Dialog.OpenReloadClose('@SYS_FRAME_UNID',
    '|business/common/droplist.xml', 'CRM_COM.LF.V', false, paras);
// → 关联：business/common/droplist.xml 下的 CRM_COM.LF.V
// 注意：|前缀表示相对于 EWA 配置根目录
```
提取正则：
```python
re.findall(r"(?:OpenReloadClose|Dialog\.Open)\s*\([^)]*?'([^']+\.xml)'[^)]*?'([^']+)'", js)
```

**模式 3：DoAction 触发动作（221 次 / 75 文件）**
```javascript
// 触发当前 ListFrame/Frame 的 ActionSet 中定义的动作
EWA.F.FOS["@SYS_FRAME_UNID"].DoAction(null, 'ExtendAction0', ...);
// → 关联：当前配置的 ActionSet → ExtendAction0 → SqlSet
```
提取正则：
```python
re.findall(r'EWA\.F\.FOS\[.*?\]\.DoAction\s*\([^)]*\'([^\']+)\'', js)
```

**模式 4：EWA.F.Install 嵌入组件（40 次 / 5 文件）**
```javascript
// 在当前页面中嵌入另一个 EWA 配置组件
EWA.F.Install('container_div', '/business/enq/price.xml', 'ENQ_PRICE.ListFrame.Modify', paras);
// → 关联：/business/enq/price.xml 下的 ENQ_PRICE.ListFrame.Modify
```
提取正则：
```python
re.findall(r"EWA\.F\.Install\s*\([^)]*?'([^']+\.xml)'[^)]*?'([^']+)'", js)
```

**外部页面引用**（`window.open` / `top.AddTab`）：
```javascript
// EWA.CP 路径 → 相对于应用根目录
var u = EWA.CP + '/robert/v.jsp?tb=OA_SEAL_APPLY&APPLY_ID=' + id;
// → 关联：/robert/v.jsp（文件查看器）

// @ewa.cp 路径 → 在 EWA 配置中定义的基础路径
let u = "@ewa.cp/back_admin/doc/doc.jsp?DOC_TMP_TAG=CONTRACT_APPROVE&apply_id=" + applyId;
// → 关联：/back_admin/doc/doc.jsp（文档打印）
```

**XItem 事件引用**（`EventSet`）：
```xml
<!-- XItem 的 onclick 事件调用 JS 函数 -->
<EventSet><Set EventName="onclick" EventType="Javascript"
    EventValue="open_doc(@apply_id)"/></EventSet>
<!-- → 追踪 open_doc 函数找到关联配置 -->
```

### 在 AI Mode 中附加关联配置

创建 AI Mode 时，需要从主配置项中提取关联信息并注入到 prompt：

**步骤 1：读取主配置**
```bash
./ewa-api.sh getConfItem "$XMLNAME" "$ITEMNAME" xml | jq -r '.XML'
```

**步骤 2：提取关联引用**
```python
# 从 ListFrame 提取 JS 中引用的 Frame
re.findall(r"'([^']+\.xml)'\s*,\s*'([^']+)'", js_code)

# 从 Frame 提取 List.Sql（select 字段枚举来源）
for xitem in root.iter('XItem'):
    list_set = xitem.find('.//List/Set')
    if list_set is not None:
        sql = list_set.get('Sql', '')
        value_field = list_set.get('ValueField', '')
        display_field = list_set.get('DisplayField', '')

# 从 Frame 提取 Frame.CallXmlName（弹窗引用）
    frame_set = xitem.find('.//Frame/Set')
    if frame_set is not None:
        call_xml = frame_set.get('CallXmlName', '')
        call_item = frame_set.get('CallItemName', '')
```

**步骤 3：为每个枚举来源创建 `<sql>` + `<prompt sqlRef>`**
```xml
<!-- 从 List.Sql 提取的枚举 → 转为 sqls -->
<sql name="getSealTypeEnums">
  <![CDATA[SELECT BAS_TAG AS V, BAS_TAG_NAME AS TT
           FROM BAS_TAG WHERE BAS_TAG_GRP='OA_SEAL' ORDER BY BAS_TAG_ORD]]>
</sql>

<!-- 注入到 prompt -->
<prompt name="enum_seal_type" role="user"
    sqlRef="getSealTypeEnums" dataType="json"
    prefix="【SEAL_TYPE 图章类型】：" />
```

**步骤 4：弹窗字段标记为"AI 不填"**
```
CRM_COM_ID 和 REF_GRP_ID 通过弹窗选择（Frame.CallXmlName），AI 不填。
```

### 配置关联速查表

编写 AI Mode 时，按此表检查是否遗漏关联：

| 检查项 | 来源 | 处理方式 |
|--------|------|----------|
| select/radio 选项 | `List.Sql` | → `<sql>` + `<prompt sqlRef>` |
| fix 搜索选项 | `OrderSearch.SearchSql` | → `<sql>` + `<prompt sqlRef>` |
| 下拉显示映射 | `DataRef.RefSql` + `RefKey`/`RefShow` | → `<sql>` + `<prompt sqlRef>` |
| 弹窗选择字段 | `Frame.CallXmlName` + `CallItemName` | 标记"AI 不填，用户 UI 操作" |
| 动作 SQL | `ActionSet → SqlSet` | 理解业务逻辑，写入 system prompt |
| JS 打开的 Item | `AddScript` 中的 `'xml', 'item'` | 关联的 tool 定义 |
| 工作流节点 | `Workflow` 元素 | 写入 system prompt 状态机说明 |

## 注意事项

1. **枚举值动态获取**：fix/select 字段的枚举值必须通过 `<sqls>` + `<prompt sqlRef="">` 动态注入，不能硬编码在 prompt 中
2. **SQL 参数**：`@G_SUP_ID`（供应商ID）、`@G_ADM_ID`（管理员ID）等系统变量由框架自动注入
3. **XML 转义**：属性值中的 `&` 必须写为 `&amp;`
4. **CDATA**：SQL 和 prompt 内容必须用 `<![CDATA[...]]>` 包裹
5. **tool parameters**：逗号分隔的参数名，框架将 AI 提取的值注入 RequestValue，form 中用 `@参数名` 引用
