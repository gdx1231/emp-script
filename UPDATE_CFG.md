# UPDATE_CFG — XML 配置管理工具

## 概述

`UPDATE_CFG` 是一个用于直接操作 EWA XML 配置文件的工具。它提供了一套完整的 API 和前端页面，用于管理 XML 中的 **XItem 定义** 和 **SqlSet/ActionSet/ScriptSet 配置**。

**核心约束**：直接读写 XML 文件，不使用数据库。

---

## 架构

```
┌─────────────────────────────────────────────┐
│  HTTP 请求                                    │
│  (UpdateCfgAction.java - Servlet)             │
│  - 参数解析 (RequestValue)                    │
│  - 权限验证 (ApiTokenValidator)               │
│  - 方法分发 (dispatch)                        │
└──────────────┬──────────────────────────────┘
               │ DTO 参数
               ▼
┌─────────────────────────────────────────────┐
│  XML 操作层                                  │
│  (UpdateCfgXml.java - define 包)             │
│  - DOM 解析 (UXml)                           │
│  - 节点 CRUD (增/删/改/查/移动)              │
│  - 保存 (IUpdateXml)                         │
│  - 操作日志 (operator 审计)                   │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│  XML 文件                                     │
│  (define.xml/ewa/*.xml)                      │
└─────────────────────────────────────────────┘
```

### 文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| `UpdateCfgXml.java` | `src/main/java/.../define/UpdateCfgXml.java` | XML 操作核心类 |
| `UpdateCfgAction.java` | `src/main/java/.../define/servlets/UpdateCfgAction.java` | Servlet 入口 |
| `ApiTokenValidator.java` | `src/main/java/.../define/servlets/ApiTokenValidator.java` | API Token 验证器 |
| `UpdateCfgXItemData.java` | `src/main/java/.../define/UpdateCfgXItemData.java` | XItem 参数 DTO |
| `UpdateCfgSqlSetData.java` | `src/main/java/.../define/UpdateCfgSqlSetData.java` | SqlSet 参数 DTO |
| `UpdateCfgScriptSetData.java` | `src/main/java/.../define/UpdateCfgScriptSetData.java` | ScriptSet 参数 DTO |
| `UPDATE_CFG.xml` | `src/main/resources/define.xml/ewa/UPDATE_CFG.xml` | 前端页面配置 |
| `UpdateCfgXmlTest.java` | `src/test/java/.../define/UpdateCfgXmlTest.java` | JUnit 5 测试 |

---

## 权限与安全

### 三层验证

1. **全局开关** — `ConfDefine.isAllowDefine()` 未开启时拒绝所有请求
2. **Token 验证** — `ApiTokenValidator.validate()` 验证管理员身份
3. **操作审计** — 所有写操作记录操作人 `operator` 到日志

### Token 验证方式

支持两种模式：

**HMAC 签名模式（推荐，服务端调用）：**
```
Headers:
  X-Api-Key: {loginId}
  X-Api-Timestamp: {当前时间戳毫秒}
  X-Api-Nonce: {随机字符串}
  X-Api-Signature: {HMAC-SHA256签名}
```

签名算法：
```
signature = HMAC-SHA256(secret, method + "\n" + timestamp + "\n" + nonce + "\n" + path + "\n" + sortedQueryParams)
```

**JWT Token 模式（客户端调用）：**
```
Headers:
  Authorization: Bearer {jwt_token}
  或
  X-Api-Token: {jwt_token}
```

### 安全特性
- 时间戳容忍：±5 分钟
- Nonce 缓存：防重放攻击（10000 条缓存，10 分钟过期）
- Token 过期：2 小时
- 白名单机制：可扩展免认证方法（当前为空）

---

## API 参考

所有 API 通过 POST/GET 调用，参数 `method` 指定操作。

### 公共参数

| 参数 | 说明 |
|------|------|
| `method` | 方法名（见下方列表） |
| `XML_PATH` | XML 文件路径（如 `/ewa/admin.xml`） |
| `ITEM_NAME` | EasyWebTemplate Name（如 `ADM_USER.Frame.ChangePWD`） |

### 返回值

成功：
```json
{
  "success": true,
  "message": "...",
  "data": { ... }
}
```

失败：
```json
{
  "success": false,
  "message": "错误描述"
}
```

### 1. 文件与模板管理

#### `listXmlFiles` — 列出所有可编辑 XML
```
POST /api/updateCfg?method=listXmlFiles
```
返回：
```json
{
  "success": true,
  "data": [
    { "xmlPath": "/ewa/admin.xml", "items": 12, "frames": {...} },
    ...
  ]
}
```

#### `listItems` — 列出 XML 中所有 EasyWebTemplate
```
POST /api/updateCfg?method=listItems&XML_PATH=/ewa/admin.xml
```
返回：
```json
{
  "success": true,
  "data": [
    { "name": "ADM_USER.Frame.ChangePWD", "frameTag": "Frame", ... },
    ...
  ]
}
```

### 2. XItem 管理

#### `listXItems` — 列出模板中所有 XItem
```
POST /api/updateCfg?method=listXItems&XML_PATH=/ewa/admin.xml&ITEM_NAME=ADM_USER.Frame.ChangePWD
```

#### `getXItem` — 获取 XItem 详情
```
POST /api/updateCfg?method=getXItem&XML_PATH=...&ITEM_NAME=...&XITEM_NAME=ADM_PWD
```

#### `saveXItem` — 保存 XItem
```
POST /api/updateCfg?method=saveXItem
```
参数：

| 参数 | 必填 | 说明 |
|------|------|------|
| `XITEM_NAME` | ✅ | XItem 名称 |
| `OLD_XITEM_NAME` | | 原名称（重命名时使用） |
| `TAG` | | 类型（text/password/select/submit/button...） |
| `DESC_ZH` | | 中文描述 |
| `DESC_EN` | | 英文描述 |
| `DATA_FIELD` | | 数据字段 |
| `DATA_TYPE` | | 数据类型（String/Int/Date...） |
| `IS_MUST_INPUT` | | 是否必填（1=是） |
| `IS_ENCRYPT` | | 是否加密（1=是） |
| `VALID` | | 验证规则 |
| `FORMAT` | | 格式化 |
| `MAX_LENGTH` | | 最大长度 |
| `MIN_LENGTH` | | 最小长度 |
| `MAX_VALUE` | | 最大值 |
| `MIN_VALUE` | | 最小值 |
| `IS_ORDER` | | 是否排序（1=是） |
| `SEARCH_TYPE` | | 列表搜索类型 |
| `ORDER_EXP` | | 排序表达式 |
| `STYLE` | | 样式 |
| `PARENT_STYLE` | | 父样式 |
| `XSTYLE` | | 扩展样式 |
| `EVENT_NAME` | | 事件名称 |
| `EVENT_TYPE` | | 事件类型 |
| `EVENT_VALUE` | | 事件值 |
| `CALL_ACTION` | | 调用 Action |
| `CONFIRM_INFO` | | 确认信息 |
| `LIST_SQL` | | 下拉 SQL |
| `LIST_VALUE_LIST` | | 下拉值列表 |
| `LIST_DISPLAY_LIST` | | 下拉显示列表 |
| `USER_SET` | | 自定义 HTML |

#### `deleteXItem` — 删除 XItem
```
POST /api/updateCfg?method=deleteXItem&XML_PATH=...&ITEM_NAME=...&XITEM_NAME=xxx
```

#### `moveXItem` — 调整 XItem 顺序
```
POST /api/updateCfg?method=moveXItem&XML_PATH=...&ITEM_NAME=...&XITEM_NAME=xxx&direction=up
```
`direction`: `up`（上移）或 `down`（下移）

### 3. SqlSet 管理

#### `listActions` — 列出 ActionSet + SqlSet + ScriptSet
```
POST /api/updateCfg?method=listActions&XML_PATH=...&ITEM_NAME=...
```

#### `getSqlSet` — 获取 SqlSet 详情
```
POST /api/updateCfg?method=getSqlSet&XML_PATH=...&ITEM_NAME=...&SQL_NAME=OnPageLoad SQL
```

#### `saveSqlSet` — 保存 SqlSet
```
POST /api/updateCfg?method=saveSqlSet
```
参数：

| 参数 | 必填 | 说明 |
|------|------|------|
| `SQL_NAME` | ✅ | SqlSet 名称 |
| `OLD_SQL_NAME` | | 原名称（重命名时使用） |
| `SQL_TYPE` | ✅ | SQL 类型（query/update） |
| `TRANS_TYPE` | | 事务类型（no/yes/空） |
| `SQL_CONTENT` | ✅ | SQL 内容（支持多行，CDATA 包裹） |

#### `deleteSqlSet` — 删除 SqlSet
```
POST /api/updateCfg?method=deleteSqlSet&XML_PATH=...&ITEM_NAME=...&SQL_NAME=xxx
```

### 4. ScriptSet 管理

#### `getScriptSet` — 获取 ScriptSet 详情
```
POST /api/updateCfg?method=getScriptSet&XML_PATH=...&ITEM_NAME=...&SCRIPT_NAME=xxx
```

#### `saveScriptSet` — 保存 ScriptSet
```
POST /api/updateCfg?method=saveScriptSet
```
参数：

| 参数 | 必填 | 说明 |
|------|------|------|
| `SCRIPT_NAME` | ✅ | ScriptSet 名称 |
| `OLD_SCRIPT_NAME` | | 原名称（重命名时使用） |
| `SCRIPT_TYPE` | ✅ | 脚本类型（javascript） |
| `SCRIPT_CONTENT` | ✅ | 脚本内容 |

#### `deleteScriptSet` — 删除 ScriptSet
```
POST /api/updateCfg?method=deleteScriptSet&XML_PATH=...&ITEM_NAME=...&SCRIPT_NAME=xxx
```

---

## XML 结构

### EasyWebTemplate

```xml
<EasyWebTemplate Name="ADM_USER.Frame.ChangePWD">
  <Action>
    <ActionSet>
      <Set Type="OnPageLoad">
        <CallSet>
          <Set CallName="OnPageLoad SQL" CallType="SqlSet" Test=""/>
        </CallSet>
      </Set>
    </ActionSet>
    <SqlSet>
      <Set Name="OnPageLoad SQL" SqlType="query" TransType="">
        <Sql><![CDATA[SELECT ...]]></Sql>
      </Set>
    </SqlSet>
    <ScriptSet>
      <Set Name="CHG_OK" ScriptType="javascript">
        <Script><![CDATA[ok()]]></Script>
      </Set>
    </ScriptSet>
  </Action>
  <XItems>
    <XItem Name="ADM_PWD">
      <Tag><Set Tag="password"/></Tag>
      <Name><Set Name="ADM_PWD"/></Name>
      <DescriptionSet>
        <Set Info="新密码" Lang="zhcn" Memo=""/>
        <Set Info="New Password" Lang="enus" Memo=""/>
      </DescriptionSet>
      <DataItem><Set DataField="ADM_PWD" DataType="String" IsEncrypt="1"/></DataItem>
      <MaxMinLength><Set MaxLength="20" MinLength="6"/></MaxMinLength>
      <IsMustInput><Set IsMustInput="1"/></IsMustInput>
      <!-- 50+ 参数节点 -->
    </XItem>
  </XItems>
</EasyWebTemplate>
```

### 关键关系

- **ActionSet** 定义执行流程和调用链（Type → CallSet → SqlSet/ScriptSet）
- **SqlSet** 通过 `Name` 被 ActionSet 的 `CallName` 引用
- **Test** 条件控制是否执行（如 `'@result'=''` 表示仅当 result 为空时执行）
- **SqlType**: `query`（查询）/ `update`（更新）
- **TransType**: `no`（无事务）/ `yes`（事务）/ 空（默认）
- SQL 可返回中间结果（如 `@dup_mobile`, `@code_right`）给后续步骤使用

---

## 测试

### 运行测试

```bash
mvn test -Dtest=UpdateCfgXmlTest
```

### 测试覆盖

| # | 测试 | 说明 |
|---|------|------|
| 1 | XML 解析 | 验证样本 XML 可解析 |
| 2 | 查找模板 | 验证能找到目标 EasyWebTemplate |
| 10 | 列表 XItem | 验证 XItem 列表结构和字段 |
| 20 | 新增 XItem | 创建 XItem 并验证 Tag/Desc/DataItem/IsMustInput |
| 30 | 修改 XItem | 修改描述和 DataField |
| 40 | 删除 XItem | 删除并验证不再存在 |
| 50 | 列表 SqlSet | 验证 SqlSet 列表和 SQL 内容 |
| 60 | 新增 SqlSet | 创建 SqlSet（CDATA 多行 SQL） |
| 70 | 修改 SqlSet | 修改 SqlType 和 SQL 内容 |
| 80 | 删除 SqlSet | 删除并验证不再存在 |
| 90 | 新增 ScriptSet | 创建 ScriptSet |
| 95 | 删除 ScriptSet | 删除并验证不再存在 |
| 100 | 多行 CDATA | 验证多行 SQL 写入和读取一致性 |
| 110 | 引用关系 | 验证 ActionSet→SqlSet 引用完整性 |

测试使用 `designs/tb2ewaCfg/examples/admin.xml` 作为样本，复制到临时目录操作，不影响源文件。

---

## 访问方式

### 前端页面

通过 `UPDATE_CFG.xml` 配置提供页面：

| 页面 | 访问方式 |
|------|---------|
| XML 选择 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XmlSelect.LF.M` |
| Item 选择 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=ItemSelect.LF.M&XML_PATH=xxx` |
| XItem 列表 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XItemCfg.LF.M&XML_PATH=xxx&ITEM_NAME=yyy` |
| XItem 编辑 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XItemCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&XITEM_NAME=zzz` |
| SQL 列表 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=SqlCfg.LF.M&XML_PATH=xxx&ITEM_NAME=yyy` |
| SQL 编辑 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=SqlCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&SQL_NAME=zzz` |
| Script 编辑 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=ScriptCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&SCRIPT_NAME=zzz` |

---

## 实施状态

- [x] 分析样本 XML 结构（admin.xml, index.xml）
- [x] 撰写计划文档
- [x] 编写 UpdateCfgXml.java — 14 个 API 方法
- [x] 编写 UpdateCfgAction.java — Servlet 入口 + 权限验证
- [x] 编写 UPDATE_CFG.xml — 7 个 EasyWebTemplate 前端页面
- [x] 编写 DTO 类 — UpdateCfgXItemData / UpdateCfgSqlSetData / UpdateCfgScriptSetData
- [x] 编写 ApiTokenValidator — Token 验证器
- [x] 编写单元测试 — 14 个测试方法全部通过
- [ ] 端到端部署测试
- [ ] SQL 测试执行功能
