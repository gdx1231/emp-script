# BusinessXmlCreator 设计说明

## 一、概述

`BusinessXmlCreator` 是 emp-script 框架中用于**根据数据库表结构自动生成 EWA 业务 XML 配置**的核心模块。通过配置驱动和字段推断，实现从数据库表到 Frame/ListFrame/Tree 页面配置的自动化生成，大幅减少手动配置工作量。

### 核心目标
- **降低配置成本**: 通过数据库表结构自动生成 80%+ 的 XML 配置
- **配置驱动**: 字段规则、Tag 类型、默认值等全部外部化配置
- **多数据库适配**: 支持 MySQL、SQL Server、HSQLDB 等不同数据库
- **可扩展**: 通过继承基类轻松支持新的 Frame 类型

### 所在分支
`cr_ewa_cfg` (尚未合并到 main)

---

## 二、架构设计

### 2.1 类结构

```
BusinessXmlCreator (工厂类)
    │
    ├── BusinessXmlCreatorBase (抽象基类，837 行)
    │       ├── 通用 SQL 生成方法
    │       ├── 字段推断逻辑
    │       ├── 配置读取封装
    │       └── XML 节点创建工具
    │
    ├── BusinessXmlCreatorListFrame (列表模式，1175 行)
    │       ├── 列表页面 XML 生成
    │       ├── OrderSearch 自动配置
    │       └── 分页/回收站支持
    │
    ├── BusinessXmlCreatorFrame (表单模式，951 行)
    │       ├── 表单页面 XML 生成
    │       ├── Tag 类型后缀匹配
    │       └── 特殊编辑器推断
    │
    └── BusinessXmlCreatorTree (树形模式，850 行)
            ├── Tree 页面 XML 生成
            ├── Tree Action SQL 自动生成
            ├── 字段名推断 (PID/LVL/ORD/NAME)
            └── 逻辑删除支持
```

### 2.2 设计模式

| 模式 | 应用 |
|------|------|
| **工厂模式** | `BusinessXmlCreator.create()` 根据 frameType 创建实例 |
| **模板方法** | `BusinessXmlCreatorBase.create()` 定义生成流程，子类实现细节 |
| **策略模式** | 不同 Frame 类型采用不同的 XML 生成策略 |
| **单例模式** | `EwaDefineSettings.getInstance()` 配置读取 |

---

## 三、核心类说明

### 3.1 BusinessXmlCreator (工厂类)

```java
// 使用示例
BusinessXmlCreator creator = BusinessXmlCreator.create(config, table, "ListFrame");
String xml = creator.createShowXml(db, tableName, selectSql, tableJson, "ListFrame", "NM");
boolean saved = creator.createAndSave(db, tableName, selectSql, tableJson, "ListFrame", "NM", xmlName, itemName, admId);
```

| 方法 | 说明 |
|------|------|
| `create(config, table, frameType)` | 静态工厂方法，创建指定类型的创建器 |
| `createAndSave(...)` | 生成并保存 XML 到配置库 |
| `createShowXml(...)` | 生成 XML 字符串用于预览 |
| `getCreator()` | 获取底层创建器实例 |

### 3.2 BusinessXmlCreatorBase (抽象基类)

#### 抽象方法 (子类必须实现)

| 方法 | 说明 |
|------|------|
| `create(frameType, operationType)` | 创建 XML Document |
| `save(xmlContent, xmlName, itemName, admId)` | 保存 XML 到配置库 |

#### 通用 SQL 生成方法

| 方法 | 说明 |
|------|------|
| `getSqlTreeLoad()` | Tree 加载 SQL，支持状态字段 WHERE 条件 |
| `getSqlTreeNodeDelete()` | Tree 删除节点 SQL (逻辑删除: UPDATE SET status='DEL') |
| `getSqlTreeNodeRename()` | Tree 重命名 SQL |
| `getSqlTreeNodeNew()` | Tree 新增节点 SQL (CASE WHEN 适配多数据库) |
| `getSqlSelectLF(statusField, includeRecycle)` | ListFrame 查询 SQL |
| `getSqlDeleteA(statusField)` | 逻辑删除 SQL |
| `getSqlRestore(statusField)` | 恢复数据 SQL |
| `getSqlDelete()` | 物理删除 SQL |
| `getSqlSelect()` | 单条记录加载 SQL |
| `getSqlUpdate(statusField)` | 更新 SQL |
| `getSqlNew(statusField)` | 新增 SQL |

#### 字段推断方法

| 方法 | 说明 |
|------|------|
| `findFieldBySuffix(suffix)` | 查找带指定后缀的字段 (_PID, _LVL, _ORD, _NAME 等) |
| `findStatusField()` | 查找状态字段 (_STATUS 或 _STATE) |
| `findIdentityField()` | 查找自增字段 |
| `getPrimaryKeyField()` | 获取主键字段名 |
| `shouldHideField(field)` | 判断字段是否应该隐藏 |

#### 配置读取方法

| 方法 | 说明 |
|------|------|
| `getTagType(dbType, fieldName, frameType)` | 获取 UI 控件类型 |
| `getDataType(dbType)` | 数据库类型→数据类型映射 |
| `getFormat(dbType)` | 日期/数字格式 |
| `getNumberScale(dbType)` | 数字精度 |
| `getValidType(dbType)` | 验证类型 (Email/Url/Number) |

#### 工具方法

| 方法 | 说明 |
|------|------|
| `processJsExpression(val)` | 处理 JavaScript 表达式占位符 |
| `getFrameTypeShort(frameType)` | Frame 类型简写 (F/LF/T) |
| `getFrameName(frameType)` | Frame 中文名称 |
| `getOperationName(operationType)` | 操作中文名称 (N=新增/M=修改/V=查看) |
| `createDescriptionSet(doc, descMap)` | 创建多语言描述节点 |
| `findChildElement(parent, tagName)` | 查找子元素 |

### 3.3 BusinessXmlCreatorListFrame

**职责**: 生成列表模式 (ListFrame) 的 XML 配置

#### 特色功能

1. **OrderSearch 自动配置**
   ```
   数值类型 (INT/DECIMAL/NUM) → IsOrder=1, SearchType=""
   日期类型 (DATE/TIME)       → IsOrder=1, SearchType=""
   短文本 (CHAR≤100)          → IsOrder=1, SearchType="text"
   长文本 (CHAR>100/TEXT)     → IsOrder=0, SearchType="text"
   ```

2. **PageSize 配置** (从 EwaDefineSettings 读取)
   - `IsSplitPage`: 是否分页
   - `PageSize`: 每页条数
   - `Recycle`: 回收站支持
   - `KeyField`: 主键字段

3. **ListUI 配置**
   - `luButtons`: 按钮布局
   - `luSearch`: 搜索栏
   - `luSelect`: 选择方式

4. **Buttons 作为 XItem 添加**
   - 从 EwaDefine.xml 的 ButtonConfigs 读取
   - 支持 butDelete, butRestore 等标准按钮

### 3.4 BusinessXmlCreatorFrame

**职责**: 生成表单模式 (Frame) 的 XML 配置

#### 特色功能

1. **Size 配置** (从 EwaDefineSettings 读取)
   - `Width`: 页面宽度 (默认 700)
   - `HiddenCaption`: 隐藏标题
   - `FrameCols`: 列数 (C2=两列)
   - `HAlign/VAlign`: 对齐方式

2. **Tag 类型后缀匹配**
   ```
   字段名后缀      → Tag 类型
   _SQL           → sqlEditor
   _XML           → xmlEditor
   _JSON          → jsonEditor
   _HTML          → dHtml5
   _CSS           → cssEditor
   ```

3. **字段类型映射**
   ```
   DATETIME       → datetime
   TEXT/CLOB      → textarea
   INT/DECIMAL    → number
   CHAR/VARCHAR   → text
   ```

### 3.5 BusinessXmlCreatorTree

**职责**: 生成树形模式 (Tree) 的 XML 配置

#### 特色功能

1. **Tree 配置字段推断**
   ```
   Tree 属性      → 推断规则
   Text           → 查找 _NAME 后缀字段
   Title          → 查找 _TITLE 后缀字段
   Key            → 主键字段
   ParentKey      → 查找 _PID 后缀字段
   Level          → 查找 _LVL 后缀字段
   Order          → 查找 _ORD 后缀字段
   ```

2. **Tree Action SQL 自动生成**
   - `OnPageLoad SQL`: 加载树节点 (按 LVL, ORD 排序)
   - `OnTreeNodeDelete SQL`: 逻辑删除 (UPDATE status='DEL')
   - `OnTreeNodeRename SQL`: 重命名节点
   - `OnTreeNodeNew SQL`: 新增节点 (CASE WHEN 适配多数据库)

3. **Tree 菜单**
   - `itemNew`: 新建节点
   - `itemRename`: 修改名称
   - `itemDelete`: 删除节点
   - 分隔符 (line)

4. **HtmlFrame 配置**
   - `FrameType`: H5
   - `FrameSize`: 200,* (左侧 200px 树形，右侧自适应)

---

## 四、配置体系

### 4.1 EwaDefineSettings.xml

**路径**: `src/main/resources/system.xml/EwaDefineSettings.xml`

#### Frame 字段隐藏规则

```xml
<FrameHideRules>
    <!-- 按后缀隐藏 -->
    <SuffixRule suffix="_CDATE" />
    <SuffixRule suffix="_MDATE" />
    <SuffixRule suffix="_STATUS" />
    <SuffixRule suffix="_STATE" />
    <!-- 按类型隐藏 -->
    <TypeRule type="IDENTITY" />
    <!-- 按名称隐藏 -->
    <NameRule name="ID" />
</FrameHideRules>
```

#### Frame 页面设置

```xml
<FramePageSettings>
    <Width>700</Width>
    <HiddenCaption>1</HiddenCaption>
    <FrameCols>C2</FrameCols>
    <HAlign>center</HAlign>
    <VAlign>top</VAlign>
</FramePageSettings>
```

#### ListFrame 页面设置

```xml
<ListFramePageSettings>
    <PageSize>10</PageSize>
    <PageSizeIsSplit>1</PageSizeIsSplit>
    <Recycle>1</Recycle>
    <LuButtons>1</LuButtons>
    <LuSearch>1</LuSearch>
    <LuSelect>listUI</LuSelect>
</ListFramePageSettings>
```

#### FieldTags 配置 (Frame 和 ListFrame 分别定义)

```xml
<FieldTags frameType="Frame">
    <Tag dbType="DATETIME" tag="datetime" />
    <Tag dbType="TEXT" tag="textarea" />
    <Tag suffix="_SQL" tag="sqlEditor" />
    <Tag suffix="_XML" tag="xmlEditor" />
    <Tag suffix="_JSON" tag="jsonEditor" />
    <Tag suffix="_HTML" tag="dHtml5" />
</FieldTags>
```

#### 字段格式配置

```xml
<FieldFormats>
    <Format dbType="DATE" format="DateShortTime" />
    <Format dbType="DATETIME" format="DateShortTime" />
    <Format dbType="INT" format="" />
    <Format dbType="DECIMAL" format="LeastMoney" />
</FieldFormats>
```

#### 字段默认值配置

```xml
<FieldDefaultValues>
    <DefaultValue suffix="_CDATE" value="@SYS_DATE" />
    <DefaultValue suffix="_UNID" value="@sys_unid" />
    <DefaultValue suffix="_CREATOR" value="@g_adm_id" />
    <DefaultValue suffix="_MODIFIER" value="@g_adm_id" />
    <!-- 支持 Where 属性 -->
    <DefaultValue name="SUP_ID" value="@g_sup_id" where="true" />
</FieldDefaultValues>
```

#### 状态字段配置

```xml
<StatusField>
    <Suffix>_STATUS</Suffix>
    <DefaultValue>'USED'</DefaultValue>
    <DeleteValue>'DEL'</DeleteValue>
    <Where>true</Where>
</StatusField>
```

### 4.2 EwaDefine.xml

**路径**: `src/main/resources/system.xml/EwaDefine.xml`

定义 Frame 模板、按钮配置、Action SQL、AddScript/CSS 等。

```xml
<EwaDefine>
    <Frame name="ListFrame" tmp="M">
        <Buttons>
            <Button name="butNew" tag="button" />
            <Button name="butDelete" tag="button" />
        </Buttons>
        <Actions>
            <Action name="OnPageLoad" sqlType="query">
                <Sql>SELECT * FROM ...</Sql>
            </Action>
        </Actions>
        <Adds>
            <AddScript><Top>...</Top><Bottom>...</Bottom></AddScript>
            <AddCss>...</AddCss>
        </Adds>
    </Frame>
</EwaDefine>
```

---

## 五、字段推断规则

### 5.1 树形字段推断

| 后缀 | 用途 | 示例 |
|------|------|------|
| `_PID` | 父 ID | `SUP_PID`, `CAT_PID` |
| `_LVL` | 层级 | `SUP_LVL`, `CAT_LVL` |
| `_ORD` | 排序 | `SUP_ORD`, `CAT_ORD` |
| `_NAME` | 名称 | `SUP_NAME`, `CAT_NAME` |

### 5.2 审计字段推断

| 后缀 | 用途 | 默认值 |
|------|------|--------|
| `_CDATE` | 创建时间 | `@SYS_DATE` |
| `_MDATE` | 修改时间 | `@SYS_DATE` |
| `_CREATOR` | 创建人 | `@g_adm_id` |
| `_MODIFIER` | 修改人 | `@g_adm_id` |
| `_STATUS` | 状态 | `'USED'` |
| `_STATE` | 状态 (备选) | `'USED'` |
| `_UNID` | 唯一 ID | `@sys_unid` |

### 5.3 隐藏规则

以下字段在生成的 XML 中**自动隐藏**:
- 自增主键 (Identity)
- `**_CDATE` (创建时间)
- `**_MDATE` (修改时间)
- `**_STATUS` / `**_STATE` (状态字段)

---

## 六、SQL 生成逻辑

### 6.1 Tree 加载 SQL

```sql
SELECT * FROM table_name
WHERE status_field='USED'
  AND where_field=@default_value
ORDER BY level_field, order_field
```

### 6.2 Tree 新增节点 SQL (MySQL)

```sql
INSERT INTO table_name (name, pid, lvl, ord, cdate, status)
SELECT 
    @EWA_TREE_TEXT name,
    CASE WHEN @EWA_TREE_PARENT_KEY IS NULL THEN 0 ELSE @EWA_TREE_PARENT_KEY END pid,
    CASE WHEN MAX(pp.lvl) IS NULL THEN -1 ELSE MAX(pp.lvl) END+1 lvl,
    CASE WHEN MAX(pc.ord) IS NULL THEN 0 ELSE MAX(pc.ord) END+1 ord,
    @SYS_DATE cdate,
    'USED' status
FROM table_name pp
LEFT JOIN table_name pc ON pc.pid=pp.pk
WHERE pp.pid = @EWA_TREE_PARENT_KEY
```

### 6.3 Tree 删除节点 SQL (逻辑删除)

```sql
-- 有状态字段时
UPDATE table_name SET status='DEL' WHERE pk = @pk AND where_field=@default_value

-- 无状态字段时
DELETE FROM table_name WHERE pk = @pk AND where_field=@default_value
```

### 6.4 ListFrame 查询 SQL

```sql
SELECT A.* FROM table_name A WHERE 1=1
    -- @EWA_RECYCLE is null
    AND A.status = 'USED'
    -- @EWA_RECYCLE = '1'
    AND A.status = 'DEL'
    --
ORDER BY A.mdate DESC
```

---

## 七、与 Define 模块的交互

### 7.1 依赖关系

```
BusinessXmlCreator
    ├── EwaConfig (script.template)     # EWA 配置模板
    ├── Table/Field (define.database)   # 数据库表结构
    ├── EwaDefineConfig                 # 读取 EwaDefine.xml 模板配置
    ├── EwaDefineSettings               # 读取 EwaDefineSettings.xml 规则配置
    └── UXml (utils)                    # XML 工具
```

### 7.2 在 Define 模块中的位置

```
define/servlets/
    ├── ServletXml.java          # 主 XML 管理 Servlet
    ├── ServletApi.java          # RESTful API Servlet
    └── [待扩展] ServletBusinessXmlCreator  # 业务 XML 生成 API

define/bussinessXmlCreator/
    ├── BusinessXmlCreator.java           # 工厂类
    ├── BusinessXmlCreatorBase.java       # 抽象基类
    ├── BusinessXmlCreatorListFrame.java  # 列表模式
    ├── BusinessXmlCreatorFrame.java      # 表单模式
    ├── BusinessXmlCreatorTree.java       # 树形模式
    ├── EwaDefineConfig.java              # 模板配置读取
    ├── EwaDefineSettings.java            # 规则配置读取
    ├── BusinessXmlCreateParams.java      # 创建参数
    └── SqlValidator.java                 # SQL 验证器
```

---

## 八、API 接口

### 8.1 Servlet 路由

API 已集成到现有的 `ServletApi.java` 中，路由: `/EWA_DEFINE/cgi-bin/api/`

### 8.2 接口列表

#### 预览 XML (无需保存)

```
POST /EWA_DEFINE/cgi-bin/api/
参数:
  - method: previewBusinessXml
  - db: 数据库名
  - tablename: 表名
  - frametype: ListFrame/Frame/Tree
  - operationtype: N/M/V/NM
  - xmlname: 配置文件名 (如 ewa/m)
  - output: xml/json (可选，默认 xml)
返回:
  {
    "RST": true,
    "XMLNAME": "ewa/m",
    "FRAMETYPE": "LISTFRAME",
    "OPERATIONTYPE": "M",
    "OUTPUT": "xml",
    "XML": "<EasyWebTemplates>...</EasyWebTemplates>"
  }
```

#### 保存 XML

```
POST /EWA_DEFINE/cgi-bin/api/
参数:
  - method: createBusinessXml
  - db: 数据库名
  - tablename: 表名
  - frametype: ListFrame/Frame/Tree
  - operationtype: N/M/V/NM
  - xmlname: 配置文件名 (如 ewa/m)
  - itemname: 配置项名 (如 CRM_COM.LF.M)
  - admid: 管理员ID (可选，使用认证用户)
返回:
  {
    "RST": true,
    "MSG": "Business XML created and saved successfully",
    "XMLNAME": "ewa/m",
    "ITEMNAME": "CRM_COM.LF.M",
    "FRAMETYPE": "LISTFRAME",
    "OPERATIONTYPE": "M"
  }
```

### 8.3 调用流程

```
用户请求
    └── ServletApi.processRequest()
            ├── 1. 认证检查 (ApiTokenValidator)
            ├── 2. 参数验证 (db/tablename/frametype/operationtype/xmlname/itemname)
            ├── 3. 读取数据库表结构 (Table.readStructure(db, tableName))
            ├── 4. 创建 EwaConfig 实例
            ├── 5. BusinessXmlCreator.create(config, table, frameType)
            ├── 6. creator.createShowXml(...) 生成 XML (预览)
            └── 7. creator.createAndSave(...) 保存到配置库 (保存)
```

### 8.4 在 Define 模块中的位置

```
define/servlets/
    ├── ServletXml.java          # 主 XML 管理 Servlet
    ├── ServletApi.java          # RESTful API Servlet (已集成 BusinessXmlCreator)
    ├── ServletGroup.java        # 组件管理 Servlet
    └── ServletWorkflow.java     # 工作流 Servlet

define/bussinessXmlCreator/
    ├── BusinessXmlCreator.java           # 工厂类
    ├── BusinessXmlCreatorBase.java       # 抽象基类
    ├── BusinessXmlCreatorListFrame.java  # 列表模式
    ├── BusinessXmlCreatorFrame.java      # 表单模式
    ├── BusinessXmlCreatorTree.java       # 树形模式
    ├── EwaDefineConfig.java              # 模板配置读取
    ├── EwaDefineSettings.java            # 规则配置读取
    ├── BusinessXmlCreateParams.java      # 创建参数
    └── SqlValidator.java                 # SQL 验证器
```

---

## 九、测试覆盖

### 9.1 现有测试

| 测试类 | 测试内容 |
|--------|----------|
| `BusinessXmlCreateTest` | ListFrame.M / ListFrame.V / Frame.NM 创建 |
| `DatabaseTableToXmlTest` | 数据库表转 XML 完整流程 |
| `CreateBusinessXmlFromDatabase` | 从数据库创建业务 XML (合并配置) |
| `TreeSqlWithStatusTest` | Tree SQL 状态字段逻辑删除 |
| `CreateBasTagTreeFromSqlServerTest` | SQL Server bas_TAG Tree 创建 |
| `CreateProductCatTreeTest` | 产品分类 Tree 创建 |
| `CreateBusinessXmlFromDbDemo` | Demo 数据库完整流程测试 |

### 9.2 测试数据

- HSQLDB 内存数据库 (`conf.help.hsqldb`)
- SQL Server 连接测试 (可选)
- `CRM_COM` 表结构测试

---

## 十、扩展指南

### 10.1 新增 Frame 类型

```java
public class BusinessXmlCreatorNewType extends BusinessXmlCreatorBase {
    
    public BusinessXmlCreatorNewType(EwaConfig config, Table table) {
        super(config, table);
    }
    
    @Override
    protected Document create(String frameType, String operationType) throws Exception {
        // 实现 XML 生成逻辑
    }
    
    @Override
    protected boolean save(String xmlContent, String xmlName, String itemName, String admId) {
        // 实现保存逻辑
        return true;
    }
}
```

然后在 `BusinessXmlCreator.create()` 中添加分支:

```java
} else if ("NewType".equalsIgnoreCase(frameType)) {
    baseCreator = new BusinessXmlCreatorNewType(config, table);
}
```

### 10.2 新增字段推断规则

在 `EwaDefineSettings.xml` 中添加配置即可，无需修改代码。

### 10.3 新增数据库适配

在 `BusinessXmlCreatorBase.getSqlTreeNodeNew()` 中，当前使用 CASE WHEN 语法已兼容 MySQL 和 SQL Server。如需支持其他数据库，可在此方法中增加特定语法。

---

## 十一、版本历史

| 提交 | 说明 |
|------|------|
| `c27d2a4` | refactor: 将 Fields.GetSql* 方法移到 BusinessXmlCreatorBase |
| `7145654` | fix: WHERE 条件使用配置的默认值 |
| `f4e7036` | feat: 状态字段增加 DeleteValue 属性 |
| `153c1e1` | feat: 字段默认值配置增加 Where 属性 |
| `509c892` | feat: EwaDefineSettings 添加字段默认值配置 |
| `4d24458` | refactor: 将 Tree SQL 生成方法移到 BusinessXmlCreatorBase |
| `a356fe9` | feat: Tree SQL 支持状态字段 (_STATUS/_STATE) |
| `aeb0ef4` | refactor: GetSqlTreeNodeNew 使用 CASE WHEN 适配不同数据库 |
| `de75fe5` | feat: GetSqlTreeNodeNew 参考 SQL Server 树形新增模板 |
| `8107cda` | feat: Tree Action SQL 从 EwaDefine.xml 读取并自动生成 |
| `3920a76` | feat: BusinessXmlCreatorTree 根据实际字段名称推断 Tree 配置 |
| `1974e1f` | feat: 新增 BusinessXmlCreatorTree 支持 FrameTree 配置 |
| `b3f4e74` | fix: 使用 INFORMATION_SCHEMA 读取 HSQL 表结构 |
| `3a3b7a9` | feat: 支持字段后缀匹配特殊编辑器 |
| `493dfb8` | refactor: 重构 BusinessXmlCreator 为基类和子类 |

---

## 十二、注意事项

1. **分支状态**: 当前代码在 `cr_ewa_cfg` 分支，尚未合并到 `main`
2. **save 方法**: `save()` 方法目前为占位实现 (`TODO`)，需要集成 `IUpdateXml` 接口
3. **API 接口**: ~~尚未创建~~ 已集成到 `ServletApi.java`，支持 `previewBusinessXml` 和 `createBusinessXml` 两个方法
4. **SQL 验证**: `SqlValidator` 仅支持基础语法检查，复杂 SQL 需手动验证
5. **数据库连接**: 需要确保数据库连接可用，否则 `Table.readStructure()` 会失败
