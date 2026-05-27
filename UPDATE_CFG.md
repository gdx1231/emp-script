# UPDATE_CFG — XML 配置管理工具计划

## 概述

创建 `UPDATE_CFG.xml` 配置管理工具，**直接操作 XML 文件**，不涉及数据库表。

两大功能：
1. **XItem 类型管理** — 读取/修改 XML 中 `<EasyWebTemplate>/<Action>/<XItems>` 的 XItem 定义
2. **SQL + ActionSet 管理** — 读取/修改 XML 中 `<Action>/<SqlSet>` 和 `<ActionSet>`

---

## 一、测试 XML 样本

使用目录: `designs/tb2ewaCfg/examples/`

### 样本文件清单

| 文件 | 包含的 Template 示例 | 特点 |
|------|---------------------|------|
| `admin.xml` | `ADM_USER.Frame.ChangeMobile`, `ADM_USER.Frame.ChangePWD`, `ADM_USER.Frame.SysChangePwd`, `ADM_USER.PIC`, `ADM_USER.SIGN_PIC`, `ADM_USER.TakePhoto` | 多个 Frame，含复杂 ActionSet + SqlSet + ScriptSet |
| `index.xml` | `GRP_COSTUMER.LF.V_BY_IDS`, `GRP_COSTUMER.ListFrame.Modify` | ListFrame，含 PageSize, ListUI, LogicShow, UserSet |
| `product_cat.xml` | — | 树形结构 |
| `enq.xml` | — | 查询表单 |
| `flight.xml` | — | 业务表单 |
| `crm_com.xml` | — | CRM 客户 |

---

## 二、XML 结构分析（基于实际样本）

### 2.1 XItem 结构（admin.xml 实际结构）

```xml
<EasyWebTemplate Name="ADM_USER.Frame.ChangePWD">
  <XItems>
    <XItem Name="ADM_LID">
      <Tag><Set Tag="span"/></Tag>
      <Name><Set Name="ADM_LID"/></Name>
      <DescriptionSet>
        <Set Info="登录名" Lang="zhcn" Memo=""/>
        <Set Info="Login name" Lang="enus" Memo=""/>
      </DescriptionSet>
      <DataItem><Set DataField="ADM_LID" DataType="String" .../></DataItem>
      <!-- 还有其他几十个参数节点，大部分为空 -->
    </XItem>
    <XItem Name="ADM_PWD">
      <Tag><Set Tag="password"/></Tag>
      <Name><Set Name="ADM_PWD"/></Name>
      <DescriptionSet>
        <Set Info="新密码" Lang="zhcn" Memo=""/>
        <Set Info="New Password" Lang="enus" Memo=""/>
      </DescriptionSet>
      <MaxMinLength><Set MaxLength="20" MinLength="6"/></MaxMinLength>
      <IsMustInput><Set IsMustInput="1"/></IsMustInput>
      <DataItem><Set DataField="ADM_PWD" DataType="String" IsEncrypt="1" .../></DataItem>
    </XItem>
    <XItem Name="butOk">
      <Tag><Set Tag="submit"/></Tag>
      <Name><Set Name="butOk"/></Name>
      <DescriptionSet>
        <Set Info="确定" Lang="zhcn" Memo=""/>
        <Set Info="Ok" Lang="enus" Memo=""/>
      </DescriptionSet>
      <ParentStyle><Set ParentStyle="width:40px; text-align: center"/></ParentStyle>
    </XItem>
  </XItems>
</EasyWebTemplate>
```

**XItem 关键参数节点**（每个 XItem 下有 ~50+ 个参数节点）：
- `Tag` — 类型 (text/span/password/select/submit/button/smsValid/h5upload...)
- `Name` — 元素名称
- `DescriptionSet` — 中英文描述
- `DataItem` — 数据绑定 (DataField, DataType, IsEncrypt, Valid, Format...)
- `OrderSearch` — 列表排序搜索 (IsOrder, SearchType...)
- `MaxMinLength` — 最大最小长度
- `IsMustInput` — 是否必填
- `EventSet` — 事件
- `CallAction` — 调用 Action
- `List` — 下拉数据源
- `UserSet` — 自定义 HTML

### 2.2 Action + SqlSet 结构（admin.xml 实际结构）

```xml
<EasyWebTemplate Name="ADM_USER.Frame.ChangeMobile">
  <Action>
    <ActionSet>
      <Set Type="OnPageLoad">
        <CallSet><Set CallName="OnPageLoad SQL" CallType="SqlSet" Test=""/></CallSet>
      </Set>
      <Set Type="OnPagePost">
        <CallSet>
          <Set CallName="checkMobile" CallType="SqlSet" Test=""/>
          <Set CallName="checkValidCode" CallType="SqlSet" Test="'@result'=''"/>
          <Set CallName="OnPagePost SQL" CallType="SqlSet" Test="'@result'=''"/>
        </CallSet>
      </Set>
      <Set Type="smsSendValidCode">
        <CallSet>
          <Set CallName="checkMobile" CallType="SqlSet" Test=""/>
          <Set CallName="smsSendValidCode sql" CallType="SqlSet" Test="'@result'=''"/>
        </CallSet>
      </Set>
    </ActionSet>
    <SqlSet>
      <Set Name="OnPageLoad SQL" SqlType="query">
        <Sql><![CDATA[SELECT adm_lid,adm_name FROM ADM_USER WHERE ADM_ID = @G_ADM_ID]]></Sql>
      </Set>
      <Set Name="OnPagePost SQL" SqlType="update">
        <Sql><![CDATA[
          -- 多语句，包含 select 返回中间结果
          select CRM_CUS_ID from adm_user where adm_id=@g_adm_id;
          select count(1) dup_mobile from ADM_USER where ...;
          UPDATE CRM_CUS SET CRM_CUS_MOBILE = @MOBILE WHERE ...;
          select case when @dup_mobile=0 then '' else '更新失败' end result,'text' rst_type;
        ]]></Sql>
      </Set>
      <Set Name="smsSendValidCode sql" SqlType="query">
        <Sql><![CDATA[select ewa_func.sms_send_validcode(...) result,'json' rst_type]]></Sql>
      </Set>
    </SqlSet>
    <ScriptSet>
      <Set Name="CHG_OK" ScriptType="javascript">
        <Script><![CDATA[ok()]]></Script>
      </Set>
    </ScriptSet>
  </Action>
</EasyWebTemplate>
```

**关键特征**：
- `ActionSet` 定义 Action 执行流程和调用链
- `SqlSet` 定义具体 SQL，通过 `Name` 被 ActionSet 引用
- `CallType` 可以是 `SqlSet` 或 `ScriptSet`
- `Test` 条件控制是否执行该步骤（如 `'@result'=''`）
- SqlType: `query` / `update`
- TransType: `no` / `yes` / 空
- SQL 可以返回中间结果给后续步骤（如 `@dup_mobile`, `@code_right`）

---

## 三、功能模块设计

### 3.1 模块一：XML 配置管理（入口）

先选择要操作的 XML 文件，然后选择具体 Item 进行操作。

#### 页面：XmlSelect.LF.M — XML 文件列表
- **数据来源**: 扫描 XML 目录
- **展示列**: XML 路径、Item 数量、Frame 类型分布
- **操作**: 选择 XML → 进入 Item 列表

### 3.2 模块二：XItem 管理

#### 页面：XItemCfg.LF.M — XItem 列表
- **数据来源**: 从选定 XML 的指定 EasyWebTemplate 解析 `<XItems>/<XItem>` 节点
- **展示列**:
  - 顺序号（节点位置）
  - Name
  - Tag (类型)
  - 中文描述 (DescriptionSet/Lang=zhcn/Info)
  - 英文描述 (DescriptionSet/Lang=enus/Info)
  - DataField (DataItem/DataField)
  - DataType
  - IsMustInput
  - IsOrder (OrderSearch/IsOrder)
- **操作**:
  - `新增` — 添加新 XItem
  - `修改` — 编辑选中 XItem
  - `删除` — 移除 XItem 节点
  - `上移/下移` — 调整顺序

#### 页面：XItemCfg.F.NM — 新增/修改 XItem
- **分组编辑**（按参数节点分组）:
  - **基础**: Name, Tag(type), Description(zhcn/enus)
  - **数据**: DataField, DataType, IsMustInput, Valid, Format, IsEncrypt
  - **验证**: MaxLength, MinLength, MaxValue, MinValue
  - **列表**: IsOrder, SearchType, OrderExp
  - **样式**: Style, ParentStyle, XStyle
  - **事件**: EventSet (EventName, EventType, EventValue)
  - **数据源**: List (Sql, ValueList, DisplayList)
  - **调用**: CallAction (Action, ConfirmInfo)

### 3.3 模块三：SQL + ActionSet 管理

#### 页面：SqlCfg.LF.M — Action/SqlSet 列表
- **数据来源**: 从选定 XML 的指定 EasyWebTemplate 解析 `<Action>` 节点
- **展示**: 树形/表格混合
  - Action 名称 (Type) → 展开显示其 CallSet 中的 SqlSet
  - 每个 SqlSet: Name, SqlType, TransType, Test, SQL预览
- **操作**:
  - `新增 Action` — 添加 Action + 关联 SqlSet
  - `修改 SqlSet` — 编辑 SQL 内容
  - `删除` — 移除 Action 或 SqlSet
  - `测试SQL` — 执行 query 测试

#### 页面：SqlCfg.F.NM — 新增/修改 SqlSet
- **字段**:
  - SqlSet Name
  - SqlType (query/update)
  - TransType (no/yes)
  - Test (条件表达式)
  - SqlContent (SqlEditor)
- **关联 ActionSet**: 选择该 SqlSet 被哪些 Action 调用

---

## 四、页面结构

```
UPDATE_CFG.xml
│
├── 1. 入口
│   ├── XmlSelect.LF.M          # 选择要编辑的 XML 文件
│   └── ItemSelect.LF.M         # 选择 EasyWebTemplate（Item）
│
├── 2. XItem 管理
│   ├── XItemCfg.LF.M           # XItem 列表（按选定的 Item 展示）
│   │   ├── 顶部: 当前 XML + Item 名称显示
│   │   ├── 列: #, Name, Tag, 中文描述, 英文描述, DataField, DataType, 必填, 排序
│   │   └── 操作: 新增 / 修改 / 删除 / 上移 / 下移
│   │
│   └── XItemCfg.F.NM           # 新增/修改 XItem（分组表单）
│       ├── 基础组: Name, Tag, 中文描述, 英文描述
│       ├── 数据组: DataField, DataType, IsMustInput, Valid
│       ├── 验证组: MaxLength, MinLength
│       ├── 排序组: IsOrder, SearchType
│       └── 按钮: 确定 / 关闭
│
├── 3. SQL + ActionSet 管理
│   ├── SqlCfg.LF.M             # Action/SqlSet 列表
│   │   ├── 顶部: 当前 XML + Item 名称
│   │   ├── 树形展示: Action → SqlSet
│   │   └── 操作: 新增 / 修改 / 删除
│   │
│   ├── SqlCfg.F.NM             # 新增/修改 SqlSet
│   │   ├── Name, SqlType, TransType
│   │   ├── SqlContent (textarea)
│   │   └── 按钮: 保存 / 返回
│   │
│   └── ScriptCfg.F.NM          # 新增/修改 ScriptSet
│       ├── Name, ScriptType
│       ├── ScriptContent (textarea)
│       └── 按钮: 保存 / 返回
│       ├── Name, SqlType, TransType, Test
│       ├── SqlContent (SqlEditor)
│       └── 调用此 SQL 的 Action 列表
│
└── 4. 模板选择
    └── ItemSelect.LF.M         # 选择 EasyWebTemplate（Item）
        └── 列表: Item Name, FrameTag, 描述
```

---

## 五、访问路径

| 功能 | URL |
|------|-----|
| XML 选择 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XmlSelect.LF.M` |
| Item 选择 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=ItemSelect.LF.M&XML_PATH=xxx` |
| XItem 列表 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XItemCfg.LF.M&XML_PATH=xxx&ITEM_NAME=yyy` |
| XItem 编辑 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=XItemCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&XITEM_NAME=zzz` |
| SQL 列表 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=SqlCfg.LF.M&XML_PATH=xxx&ITEM_NAME=yyy` |
| SQL 编辑 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=SqlCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&SQL_NAME=zzz` |
| Script 编辑 | `./?XMLNAME=/ewa/UPDATE_CFG.xml&ITEMNAME=ScriptCfg.F.NM&XML_PATH=xxx&ITEM_NAME=yyy&SCRIPT_NAME=zzz` |

### API 端点（UpdateCfgAction）

所有 API 通过 POST/GET 调用，参数 `method` 指定操作：

| method | 参数 | 说明 |
|--------|------|------|
| `listXmlFiles` | 无 | 列出所有可编辑 XML 文件 |
| `listItems` | `XML_PATH` | 列出 XML 中所有 EasyWebTemplate |
| `listXItems` | `XML_PATH`, `ITEM_NAME` | 列出模板中所有 XItem |
| `getXItem` | `XML_PATH`, `ITEM_NAME`, `XITEM_NAME` | 获取 XItem 详情 |
| `saveXItem` | `XML_PATH`, `ITEM_NAME`, `XITEM_NAME`, `OLD_XITEM_NAME`, `TAG`, `DESC_ZH`, `DESC_EN`, ... | 保存 XItem |
| `deleteXItem` | `XML_PATH`, `ITEM_NAME`, `XITEM_NAME` | 删除 XItem |
| `moveXItem` | `XML_PATH`, `ITEM_NAME`, `XITEM_NAME`, `direction` | 移动 XItem (up/down) |
| `listActions` | `XML_PATH`, `ITEM_NAME` | 列出 ActionSet + SqlSet + ScriptSet |
| `getSqlSet` | `XML_PATH`, `ITEM_NAME`, `SQL_NAME` | 获取 SqlSet 详情 |
| `saveSqlSet` | `XML_PATH`, `ITEM_NAME`, `SQL_NAME`, `OLD_SQL_NAME`, `SQL_TYPE`, `TRANS_TYPE`, `SQL_CONTENT` | 保存 SqlSet |
| `deleteSqlSet` | `XML_PATH`, `ITEM_NAME`, `SQL_NAME` | 删除 SqlSet |
| `getScriptSet` | `XML_PATH`, `ITEM_NAME`, `SCRIPT_NAME` | 获取 ScriptSet 详情 |
| `saveScriptSet` | `XML_PATH`, `ITEM_NAME`, `SCRIPT_NAME`, `OLD_SCRIPT_NAME`, `SCRIPT_TYPE`, `SCRIPT_CONTENT` | 保存 ScriptSet |
| `deleteScriptSet` | `XML_PATH`, `ITEM_NAME`, `SCRIPT_NAME` | 删除 ScriptSet |

---

## 六、关键技术实现

### 6.1 XML 读取
```java
// 从 classpath 读取 XML
String xmlPath = "define.xml/ewa/admin.xml";
InputStream is = getClass().getClassLoader().getResourceAsStream(xmlPath);
Document doc = UXml.asDocument(xmlContent);
```

### 6.2 XML 修改保存
```java
// 通过 IUpdateXml 保存
IUpdateXml ux = ConfigUtils.getUpdateXml(xmlName);
ux.saveXml(itemName, modifiedXmlContent);
```

### 6.3 XItem 顺序调整
通过 DOM `parentNode.insertBefore(node, referenceNode)` 调整节点顺序后保存。

### 6.4 参数传递
- `XML_PATH` — XML 文件路径（如 `/ewa/admin.xml`）
- `ITEM_NAME` — EasyWebTemplate Name（如 `ADM_USER.Frame.ChangePWD`）
- `XITEM_NAME` — XItem Name（如 `ADM_PWD`）
- `SQL_NAME` — SqlSet Name（如 `OnPagePost SQL`）

---

## 七、文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| `UpdateCfgAction.java` | `src/main/java/.../define/servlets/UpdateCfgAction.java` | 后端 Action — XML 读取/修改/保存 |
| `UPDATE_CFG.xml` | `src/main/resources/define.xml/ewa/UPDATE_CFG.xml` | 配置管理 XML 模板（7 个 EasyWebTemplate） |
| `UPDATE_CFG.md` | 根目录 | 本计划文档 |

---

## 八、实施步骤

1. ✅ 分析样本 XML 结构（admin.xml, index.xml）
2. ✅ 编写本计划文档
3. ✅ 编写 UpdateCfgAction.java — 16 个 API 方法，支持 XItem/SqlSet/ScriptSet CRUD
4. ✅ 编写 UPDATE_CFG.xml — 包含 7 个 EasyWebTemplate（XmlSelect, ItemSelect, XItemCfg.LF, XItemCfg.F, SqlCfg.LF, SqlCfg.F, ScriptCfg.F）
5. ⬜ 部署测试 — 验证 XItem 和 SQL 的 CRUD 操作
6. ⬜ 添加 SQL 测试执行功能
