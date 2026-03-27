# 数据库表到 EWA 配置转换设计文档

> 基于 `system.xml/EwaDefine.xml`、`system.xml/EwaDefineConfig.xml` 和 `designs/tb2ewaCfg/js/*.js` 的深度解析
> 最后更新：2025-03-27

## 0. JavaScript 核心类与函数

### 0.1 核心类结构 (EWAC_DEFINE.js)

#### 0.1.1 EWAC$Fields - 字段集合类

负责管理数据库表字段，提供 SQL 生成功能。

```javascript
function EWAC$Fields() {
    this.Fields = new EWAC$SetBase();  // 字段列表
    this.TableName;                     // 表名
    this.Pk = "";                       // 主键
    this.PkFields = new Array();        // 主键字段数组
    
    // 核心 SQL 生成方法
    this.GetSqlSelect = function()      // Frame 查询 SQL
    this.GetSqlSelectLF = function()    // ListFrame 查询 SQL
    this.GetSqlNew = function()         // 新增 SQL
    this.GetSqlUpdate = function()      // 修改 SQL
    this.GetSqlDeleteA = function()     // 软删除 SQL
    this.GetSqlRestore = function()     // 恢复 SQL
    this.GetSqlLogin = function()       // 登录验证 SQL
    this.GetSqlTreeLoad = function()    // 树加载 SQL
    this.GetSqlTreeNodeNew = function() // 树新增 SQL
    this.GetPkParas = function()        // 主键参数表达式
}
```

#### 0.1.2 EWAC$Field - 字段类

单个字段的元数据定义和类型映射。

```javascript
function EWAC$Field() {
    this.Name;          // 字段名
    this.Length;        // 长度
    this.Description;   // 描述
    this.IsPk = false;  // 是否主键
    this.IsFk = false;  // 是否外键
    this.FkTableName;   // 外键表名
    this.FkColumnName;  // 外键列名
    this.IsChecked;     // 是否选中
    
    // 类型映射方法
    this.GetTag = function()      // 获取 HTML Tag (text/select/span/textarea)
    this.GetType = function()     // 获取 EWA DataType (String/Int/Number/Date)
    this.GetFormat = function()   // 获取 Format (Date/Money)
    this.GetOrder = function()    // 是否可排序 (0/1)
    this.GetSearch = function()   // 是否可搜索 (text/empty)
}
```

**Tag 类型推断规则**:
```javascript
this.GetTag = function() {
    if (this.FrameTag == "Frame" && this.Parent.Parent.SelectedTypeName != 'V') {
        if (this.Type.toLowerCase().indexOf("date") >= 0 || 
            this.Type.toLowerCase().indexOf("time") >= 0) {
            return "date";
        }
        if (this.IsFk) {
            return "select";  // 外键使用下拉框
        }
        if (this.Length < 500) {
            return "text";
        } else {
            return "textarea";
        }
    } else {
        return "span";  // 查看模式使用 span
    }
};
```

**DataType 映射规则**:
```javascript
this.GetType = function() {
    var t = this.Type.toLowerCase();
    if (t.indexOf("date") >= 0 || t.indexOf("time") >= 0) {
        return "Date";
    }
    if (t.indexOf("int") >= 0) {
        return "Int";
    }
    if (t.indexOf("num") >= 0 || t.indexOf("money") >= 0 || 
        t.indexOf('decimal') >= 0) {
        return "Number";
    }
    if (t.indexOf("blob") >= 0 || t.indexOf("image") >= 0) {
        return "Binary";
    }
    return "String";
};
```

#### 0.1.3 EWA_DefineClass - 定义主类

管理整个配置生成流程。

```javascript
function EWA_DefineClass(frameTag) {
    this.Frames = new EWAC$SetBase();   // Frame 定义集合
    this.Steps = new Array();            // 向导步骤
    this.FrameTag = frameTag;            // Frame 类型
    this.Fields = new EWAC$Fields();     // 字段集合
    
    this.SelectedTmp = null;             // 当前选择的模板
    this.SelectedTmpName = null;         // 模板名称
    
    // 核心方法
    this.CreateFrame = function()        // 创建 Frame XML
    this.CreateNewFrame = function()     // 创建新配置并关闭
    this.HiddenCaption = function()      // 隐藏标题 (Frame=1, ListFrame=0)
    this.Width = function()              // 宽度 (Frame=700, ListFrame=100%)
}
```

### 0.2 SQL 生成器 (EWAC_SQL.js)

#### 0.2.1 EWAC_SqlCreator - SQL 创建类

用于从表结构生成 SQL 语句和 Java 类。

```javascript
function EWAC_SqlCreator() {
    this._TableName;      // 表名
    this._Fields = [];    // 字段数组
    this._ClassName;      // 类名
    
    // SQL 生成方法
    this.CreateSqlSelect = function()   // SELECT * FROM table WHERE pk=@pk
    this.CreateSqlInsert = function()   // INSERT INTO table (...) VALUES (...)
    this.CreateSqlUpdate = function()   // UPDATE table SET ... WHERE pk=@pk
    this.CreateSqlDelete = function()   // DELETE FROM table WHERE pk=@pk
    
    // Java 类生成方法
    this.CreateClass = function()       // 生成 Java 实体类
    this.CreateClassDao = function()    // 生成 Java DAO 类
}
```

#### 0.2.2 类型映射函数

```javascript
function CreateType(s1) {
    s1 = s1.toLowerCase();
    if (s1 == 'bigint' || s1 == 'lang') {
        return 'Long';
    } else if (s1 == "smallint") {
        return "Short";
    } else if (s1 == "tinyint") {
        return "Byte";
    } else if (s1 == "smallint unsigned") {
        return "UInt16";
    } else if (s1 == "int unsigned") {
        return "UInt32";
    } else if (s1 == "bigint unsigned") {
        return "UInt64";
    } else if (s1.indexOf("int") >= 0) {
        return "Integer";
    } else if (s1.indexOf("decimal") >= 0 || s1.indexOf("num") >= 0 || 
               s1.indexOf("money") >= 0 || s1.indexOf('float') >= 0) {
        return "Double";
    } else if (s1.indexOf("date") >= 0 || s1.indexOf("time") >= 0) {
        return "Date";
    } else if (s1.indexOf("bin") >= 0 || s1.indexOf("blob") >= 0 || 
               s1.indexOf("image") >= 0) {
        return "byte[]";
    } else if (s1 == "bit" || s1.indexOf("bool") >= 0) {
        return "Boolean";
    } else {
        return "String";
    }
}
```

### 0.3 默认配置映射 (EWAC_DEFAULT.js)

#### 0.3.1 字段映射 (FIELD_MAP)

```javascript
EWAC_DEF.FIELD_MAP = {
    ADM_ID : "G_ADM_ID",      // 管理员 ID → 全局管理员 ID 参数
    SUP_ID : "G_SUP_ID",      // 供应商 ID → 全局供应商 ID 参数
    OWN_SUP_ID : "G_SUP_ID",  // 所属供应商 → 全局供应商 ID 参数
    ADM_UNID : "G_ADM_UNID",
    SUP_UNID : "G_SUP_UNID"
};
```

#### 0.3.2 WHERE 条件映射

```javascript
EWAC_DEF.WHERE = {
    SUP_ID : "G_SUP_ID",      // 自动添加 SUP_ID = @G_SUP_ID 条件
    OWN_SUP_ID : "G_SUP_ID"
};
```

#### 0.3.3 列表字段映射 (LIST)

外键字段自动生成下拉列表 SQL：

```javascript
EWAC_DEF.LIST = {
    ADM_ID : {
        SQL : "'SELECT ADM_ID,ADM_NAME FROM ADM_USER WHERE SUP_ID=@G_SUP_ID ORDER BY ADM_NAME'",
        TEXT : "'ADM_NAME'",
        VALUE : "'ADM_ID'"
    },
    SUP_ID : {
        SQL : "'SELECT SUP_ID,SUP_NAME FROM SUP_MAIN WHERE 1=1 ORDER BY SUP_NAME'",
        TEXT : "'SUP_NAME'",
        VALUE : "'SUP_ID'"
    }
};
```

### 0.4 配置工具类 (EWAC_CFG.js)

#### 0.4.1 EwaCConfig - 配置管理类

```javascript
function EwaCConfig(className) {
    this.itemXml = null;       // 配置项 XML
    this.cfgXml = null;        // 配置定义 XML (EwaConfig.xml)
    this.cnnXml = null;        // 数据库连接 XML (EwaConnections.xml)
    this.globalXml = null;     // 全局配置 XML (EwaGlobal.xml)
    
    this.UIPanels = new EwaUIPanels();  // UI 面板集合
    
    // 初始化方法
    this.initItemsParas = function()    // 初始化配置项参数
    this.loadScriptItem = function()    // 加载脚本配置项
}
```

### 0.5 HTML 转配置工具 (EWAC_FROM_TEMPLATE.js)

#### 0.5.1 EWAC_F - HTML 分析器

从 HTML 页面分析生成 EWA 配置。

```javascript
var EWAC_F = {
    create : function(obj)        // 从 HTML 创建配置
    create_method1 : function()   // 方法 1: 分析占位符
    create_method2 : function()   // 方法 2: 分析 input/select
    create_sql : function()       // 生成 CREATE TABLE SQL
    create_xml : function()       // 生成 EWA XML 配置
};
```

**字段识别规则**:
```javascript
// 通过占位符识别字段类型
{SPAN#FIELD_NAME}    → span 显示
{DATETIME#FIELD}     → 日期时间输入
{SELECT#FIELD}       → 下拉选择
{TEXT#FIELD}         → 文本域
{INPUT#FIELD}        → 文本输入
{RADIO#FIELD}        → 单选框
{CHECKBOX#FIELD}     → 复选框
```

### 0.6 保存与快捷键 (EWAC.js)

#### 0.6.1 XML 保存方法

```javascript
EWAC.SaveXml = function(xmlName, itemName, xml, isNew) {
    var url = EWA.CP + "/EWA_DEFINE/cgi-bin/xml/?TYPE=SAVE" +
              "&XMLNAME=" + xmlName + "&ITEMNAME=" + itemName;
    AJAX.AddParameter("XML", xml);
    AJAX.PostNew(url, callback);
};
```

#### 0.6.2 快捷键绑定

```javascript
// Ctrl+S / Cmd+S 保存快捷键
key('⌘+s,ctrl+s', function(event) {
    saveXml();
    return false; // prevent default && stop propagation
});
```

#### 0.6.3 主题切换

```javascript
EWAC.Theme = {
    get_theme : function()   // 获取当前主题 (dark/light)
    change_theme : function(theme)  // 切换主题
    mark_menu : function(theme)     // 标记菜单选中状态
};
```

---

## 1. 示例 XML 文件结构模式分析

```xml
<?xml version="1.0" encoding="UTF-8"?>
<EasyWebTemplates>
    <EasyWebTemplate 
        Author="" 
        CreateDate="2019-08-25 09:51:15" 
        Name="表名.Frame.类型" 
        UpdateDate="2024-01-06 15:45:31"
    >
        <Page>...</Page>
        <Action>...</Action>
        <XItems>...</XItems>
        <Menus/>
        <Charts/>
        <PageInfos/>
        <Workflows/>
    </EasyWebTemplate>
</EasyWebTemplates>
```

### 1.2 Frame 类型分类

根据示例分析，主要有两种 Frame 类型：

| FrameTag      | 用途 | 变体                                             | 说明                     |
| ------------- | ---- | ------------------------------------------------ | ------------------------ |
| **Frame**     | 表单 | `NM` (New/Modify), `V` (View), `Login`, `N`, `M` | 用于单条记录的增删改查   |
| **ListFrame** | 列表 | `V` (View), `M` (Modify), `AddToRelation`        | 用于多条记录的浏览和管理 |

### 1.3 系统定义的向导步骤 (Steps)

根据 `EwaDefine.xml`，创建新配置需要经历 6 个步骤：

| 步骤 | Eval 方法               | AfterEval 方法               | 说明                            |
| ---- | ----------------------- | ---------------------------- | ------------------------------- |
| 1    | -                       | `this.CreateMainInfo()`      | 从数据库树选择一个表            |
| 2    | `this.SelectType()`     | -                            | 选择类型 (Frame/ListFrame/Tree) |
| 3    | `this.ModifyMainInfo()` | -                            | 修改主要信息 (名称、描述等)     |
| 4    | `this.ModifyFields()`   | `this.SelectFields('field')` | 选择字段 (Frame/ListFrame)      |
| 4a   | `this.ModifyMenus()`    | `this.SelectFields('menu')`  | 选择菜单 (仅 Tree)              |
| 5    | `this.ModifySQL()`      | -                            | 修改 SQL 语句                   |

### 1.4 系统菜单配置 (EwaDefineConfig.xml)

系统定义了以下顶级菜单：

| 菜单 ID | 图标               | 中文       | 英文             | 命令                       |
| ------- | ------------------ | ---------- | ---------------- | -------------------------- |
| 10      | fa-save            | 保存       | Save             | `saveXml()`                |
| 20      | fa-edit            | 调整信息   | Modify Infos     | 下拉菜单                   |
| 201     | -                  | 调整条目   | Change Items     | `changeItems('ITEMS')`     |
| 202     | -                  | 调整菜单   | Change Menu      | `changeItems('MENU')`      |
| 203     | -                  | 调整信息   | Change Infos     | `changeItems('PAGEINFOS')` |
| 204     | -                  | 调整图表   | Change Charts    | `changeItems('CHART')`     |
| 205     | -                  | 调整工作流 | Change Workflows | `changeItems('WORKFLOW')`  |
| 50      | fa-object-group    | 组件       | Group            | 下拉菜单                   |
| 60      | fa-plug            | 工具       | Utils            | 下拉菜单                   |
| 94      | fa-key             | 用户       | Admins           | 下拉菜单                   |
| 90      | fa-question-circle | 帮助       | Help             | 下拉菜单                   |
| 99      | fa-power-off       | 退出       | Exit             | 跳转登录页                 |

### 1.5 `<Page>` 节点核心配置

```xml
<Page>
    <!-- 1. 基础标识 -->
    <Name><Set Name="表名.Frame.类型"/></Name>
    <FrameTag><Set FrameTag="Frame|ListFrame"/></FrameTag>
    <SkinName><Set SkinName="Test1"/></SkinName>
    <DataSource><Set DataSource="数据源名称"/></DataSource>

    <!-- 2. 安全与日志 -->
    <Acl><Set Acl="com.gdxsoft.web.acl.BusinessImpl"/></Acl>
    <Log><Set Log="com.gdxsoft.web.log.EwaScriptLog"/></Log>

    <!-- 3. 多语言描述 -->
    <DescriptionSet>
        <Set Info="中文描述" Lang="zhcn" Memo=""/>
        <Set Info="English Description" Lang="enus" Memo=""/>
    </DescriptionSet>

    <!-- 4. 布局配置 -->
    <Size>
        <Set FrameCols="C2" HAlign="center" VAlign="top" Width="700"/>
    </Size>

    <!-- 5. 脚本注入 -->
    <AddScript>
        <Set>
            <Top><![CDATA[...]]></Top>
            <Bottom><![CDATA[...]]></Bottom>
        </Set>
    </AddScript>
</Page>
```

## 11. Page 节点完整解析

### 11.1 Page 节点全貌

```xml
<Page>
    <!-- ============ 基础标识配置 ============ -->
    <Name>...</Name>              <!-- 页面名称 -->
    <FrameTag>...</FrameTag>      <!-- Frame 类型 -->
    <SkinName>...</SkinName>      <!-- 皮肤样式 -->
    <DataSource>...</DataSource>  <!-- 数据源 -->
    
    <!-- ============ 功能开关 ============ -->
    <AllowJsonExport/>            <!-- 允许 JSON 导出 -->
    <Cached/>                     <!-- 缓存配置 -->
    <ConfigMemo/>                 <!-- 配置备注 -->
    
    <!-- ============ 安全与日志 ============ -->
    <Acl>...</Acl>                <!-- 权限控制类 -->
    <Log>...</Log>                <!-- 日志记录类 -->
    
    <!-- ============ 多语言描述 ============ -->
    <DescriptionSet>...</DescriptionSet>
    
    <!-- ============ 属性配置 ============ -->
    <PageAttributeSet>...</PageAttributeSet>  <!-- 页面属性 -->
    <RowAttributeSet/>                        <!-- 行属性 -->
    <GroupSet/>                               <!-- 分组配置 -->
    
    <!-- ============ 布局配置 ============ -->
    <Size>...</Size>                <!-- 尺寸布局 -->
    
    <!-- ============ HTML 注入 ============ -->
    <AddHtml>...</AddHtml>          <!-- 自定义 HTML -->
    
    <!-- ============ 样式与脚本 ============ -->
    <AddScript>...</AddScript>      <!-- JavaScript -->
    <AddCss>...</AddCss>            <!-- CSS 样式 -->
    
    <!-- ============ 图表配置 ============ -->
    <ChartsShow/>                   <!-- 图表显示 -->
    <RedrawJson/>                   <!-- 重绘 JSON -->
    <BoxJson/>                      <!-- 盒图 JSON -->
    <LeftJson/>                     <!-- 左侧 JSON -->
    
    <!-- ============ Frame HTML ============ -->
    <FrameHtml>...</FrameHtml>      <!-- Frame 自定义 HTML -->
    
    <!-- ============ 列表配置 ============ -->
    <PageSize>...</PageSize>        <!-- 分页配置 -->
    <ListUI>...</ListUI>            <!-- 列表 UI -->
    
    <!-- ============ 菜单与树 ============ -->
    <MenuShow/>                     <!-- 菜单显示 -->
    <Menu/>                         <!-- 菜单定义 -->
    <Tree/>                         <!-- 树配置 -->
    <HtmlFrame/>                    <!-- HTML Frame -->
    <TreeIconSet/>                  <!-- 树图标 -->
    
    <!-- ============ 多维表格 ============ -->
    <MGAxisX/>                      <!-- X 轴 -->
    <MGAxisY/>                      <!-- Y 轴 -->
    <MGCell/>                       <!-- 单元格 -->
    
    <!-- ============ 逻辑显示 ============ -->
    <LogicShow>...</LogicShow>      <!-- 条件显示 -->
</Page>
```

### 11.2 各节点详细说明

#### 11.2.1 基础标识配置

| 节点 | 属性 | 说明 | 示例 |
|-----|------|------|------|
| `<Name>` | `Name` | 页面唯一标识 | `CRM_COM.F.NM` |
| `<FrameTag>` | `FrameTag` | Frame 类型 | `Frame`、`ListFrame`、`Tree` |
| `<SkinName>` | `SkinName`、`IsXhtml` | 皮肤样式 | `Test1` |
| `<DataSource>` | `DataSource` | 数据库连接配置名 | `globaltravel` |

#### 11.2.2 功能开关

| 节点 | 属性 | 说明 |
|-----|------|------|
| `<AllowJsonExport>` | `AllowJsonExport` | 是否允许 JSON 导出 |
| `<Cached>` | `CachedSeconds`、`CachedType` | 缓存配置（秒数、类型） |
| `<ConfigMemo>` | - | 配置备注信息 |

#### 11.2.3 安全与日志

| 节点 | 属性 | 说明 | 示例 |
|-----|------|------|------|
| `<Acl>` | `Acl` | 权限控制类全名 | `com.gdxsoft.web.acl.BusinessImpl` |
| `<Log>` | `Log` | 日志记录类全名 | `com.gdxsoft.web.log.EwaScriptLog` |

#### 11.2.4 多语言描述

```xml
<DescriptionSet>
    <Set Info="修改用户密码" Lang="zhcn" Memo="备注"/>
    <Set Info="Change password" Lang="enus" Memo="Note"/>
</DescriptionSet>
```

| 属性 | 说明 |
|-----|------|
| `Info` | 显示文本 |
| `Lang` | 语言代码（zhcn/enus） |
| `Memo` | 备注信息 |

#### 11.2.5 属性配置

```xml
<PageAttributeSet>
    <Set PageAttName="属性名" PageAttValue="属性值"/>
</PageAttributeSet>

<RowAttributeSet>
    <Set RowAttName="属性名" RowAttValue="属性值" RowAttLogic="逻辑"/>
</RowAttributeSet>

<GroupSet>
    <Set GroupInfo="分组信息" GroupShow="LST" Lang="zhcn"/>
</GroupSet>
```

#### 11.2.6 布局配置

```xml
<Size>
    <Set 
        FrameCols="C2"          <!-- 列数：C1/C2/C3 -->
        HAlign="center"         <!-- 水平对齐：left/center/right -->
        VAlign="top"            <!-- 垂直对齐：top/middle/bottom -->
        Width="500"             <!-- 宽度 -->
        Height=""               <!-- 高度 -->
        HiddenCaption="1"       <!-- 隐藏标题：0/1 -->
        TextareaAuto=""         <!-- 文本域自动高度 -->
    />
</Size>
```

#### 11.2.7 HTML 注入

```xml
<AddHtml>
    <Set>
        <Top><![CDATA[<!-- 顶部 HTML -->]]></Top>
        <Bottom><![CDATA[<!-- 底部 HTML -->]]></Bottom>
    </Set>
</AddHtml>
```

#### 11.2.8 脚本注入

```xml
<AddScript>
    <Set>
        <Top><![CDATA[
            $(function(){
                // 页面加载时执行
            });
        ]]></Top>
        <Bottom><![CDATA[
            (function(){
                const ewa = EWA.F.FOS["@SYS_FRAME_UNID"];
                ewa.DoPostBefore = function(){
                    return true;
                };
            })();
        ]]></Bottom>
    </Set>
</AddScript>
```

#### 11.2.9 CSS 注入

```xml
<AddCss>
    <Set>
        <AddCss><![CDATA[
            #f_@sys_frame_unid .EWA_TD_M {
                width: 400px;
            }
        ]]></AddCss>
    </Set>
</AddCss>
```

#### 11.2.10 图表配置

| 节点 | 说明 |
|-----|------|
| `<ChartsShow>` | 图表显示配置 |
| `<RedrawJson>` | 重绘图表 JSON 数据 |
| `<BoxJson>` | 盒图 JSON 数据 |
| `<LeftJson>` | 左侧图表 JSON 数据 |

#### 11.2.11 Frame HTML

```xml
<FrameHtml>
    <Set>
        <FrameHtml/>
    </Set>
</FrameHtml>
```

#### 11.2.12 列表配置

```xml
<PageSize>
    <Set 
        PageSize="10"           <!-- 每页记录数 -->
        KeyField="ID"           <!-- 主键字段 -->
        StatusField="STATUS"    <!-- 状态字段 -->
        IsSplitPage="1"         <!-- 是否分页 -->
        AllowExport="XLS"       <!-- 允许导出格式 -->
        Recycle=""              <!-- 回收站配置 -->
        ColSize=""              <!-- 列宽配置 -->
    />
</PageSize>

<ListUI>
    <Set 
        luSelect="S"            <!-- 选择模式：S(单选)/M(多选) -->
        luSearch="1"            <!-- 是否显示搜索 -->
        luButtons="1"           <!-- 是否显示按钮 -->
        luDblClick="0"          <!-- 是否启用双击 -->
        luStickyHeaders="1"     <!-- 固定表头 -->
    />
</ListUI>
```

#### 11.2.13 菜单与树

| 节点 | 说明 |
|-----|------|
| `<MenuShow>` | 菜单显示配置 |
| `<Menu>` | 菜单定义 |
| `<Tree>` | 树配置（Key、ParentKey、Text 等） |
| `<HtmlFrame>` | HTML Frame 配置 |
| `<TreeIconSet>` | 树图标配置 |

#### 11.2.14 多维表格配置

| 节点 | 说明 |
|-----|------|
| `<MGAxisX>` | X 轴字段配置 |
| `<MGAxisY>` | Y 轴字段配置 |
| `<MGCell>` | 单元格配置 |

#### 11.2.15 逻辑显示

```xml
<LogicShow>
    <Set 
        Name="条件名" 
        HiddenFields="字段 1，字段 2" 
        ParaExp="'@METHOD'='DJ'"
    />
</LogicShow>
```

| 属性 | 说明 |
|-----|------|
| `Name` | 条件名称 |
| `HiddenFields` | 要隐藏的字段列表 |
| `ParaExp` | 条件表达式 |

### 11.3 Page 节点配置检查清单

| 配置项 | 必需 | 说明 |
|-------|------|------|
| Name | ✓ | 页面唯一标识 |
| FrameTag | ✓ | Frame 类型 |
| DataSource | ✓ | 数据源 |
| DescriptionSet | ✓ | 多语言描述 |
| Size | ✓ | 布局尺寸 |
| Acl | ○ | 权限控制（推荐配置） |
| Log | ○ | 日志记录（推荐配置） |
| AddScript | ○ | 自定义脚本 |
| AddCss | ○ | 自定义样式 |
| PageSize | ○ | 分页配置（ListFrame 必需） |

---

### 1.6 `<Action>` 节点动作定义

```xml
<Action>
    <!-- 动作集合定义 -->
    <ActionSet>
        <Set Type="OnPageLoad">      <!-- 页面加载 -->
            <CallSet><Set CallName="OnPageLoad SQL" CallType="SqlSet"/></CallSet>
        </Set>
        <Set Type="OnPagePost">      <!-- 提交处理 -->
            <CallSet>
                <Set CallName="OnNew SQL" CallType="SqlSet" Test="'@EWA_MTYPE'='N'"/>
                <Set CallName="OnModify SQL" CallType="SqlSet" Test="'@EWA_MTYPE'='M'"/>
            </CallSet>
        </Set>
        <Set Type="OnFrameDelete">   <!-- 删除操作 -->
            <CallSet><Set CallName="OnFrameDelete SQL" CallType="SqlSet"/></CallSet>
        </Set>
        <Set Type="SAct0">           <!-- 自定义查询 0 -->
            <CallSet><Set CallName="SAct0 SQL" CallType="SqlSet"/></CallSet>
        </Set>
        <Set Type="UAct0">           <!-- 自定义更新 0 -->
            <CallSet><Set CallName="UAct0 SQL" CallType="SqlSet"/></CallSet>
        </Set>
    </ActionSet>
    
    <!-- SQL 定义集合 -->
    <SqlSet>
        <Set Name="OnPageLoad SQL" SqlType="query">
            <Sql><![CDATA[SELECT * FROM 表名 WHERE 条件]]></Sql>
        </Set>
        <Set Name="OnNew SQL" SqlType="update">
            <Sql><![CDATA[INSERT INTO 表名 (...) VALUES (...)]]></Sql>
        </Set>
        <Set Name="OnModify SQL" SqlType="update">
            <Sql><![CDATA[UPDATE 表名 SET ... WHERE 条件]]></Sql>
        </Set>
        <Set Name="OnFrameDelete SQL" SqlType="update">
            <Sql><![CDATA[UPDATE 表名 SET A_STATUS='DEL' WHERE 条件]]></Sql>
        </Set>
    </SqlSet>
</Action>
```

### 1.7 `<XItems>` 字段项定义

```xml
<XItems>
    <XItem Name="字段名">
        <!-- 显示标签 -->
        <Tag><Set Tag="text|password|select|textarea|span|checkbox|submit|button"/></Set></Tag>
        <Name><Set Name="字段名"/></Name>
        
        <!-- 多语言描述 -->
        <DescriptionSet>
            <Set Info="中文字段名" Lang="zhcn" Memo=""/>
            <Set Info="English Field Name" Lang="enus" Memo=""/>
        </DescriptionSet>
        
        <!-- 输入验证 -->
        <MaxMinLength><Set MaxLength="50" MinLength=""/></MaxMinLength>
        <IsMustInput><Set IsMustInput="0|1"/></IsMustInput>
        
        <!-- 数据绑定 -->
        <DataItem>
            <Set 
                DataField="字段名" 
                DataType="String|Int|Number|Date" 
                IsEncrypt="0|1"
                Format="Date|Money"
                Valid="Email|Mobile"
            />
        </DataItem>
        
        <!-- 下拉列表数据源 -->
        <List>
            <Set 
                ValueField="值字段" 
                DisplayField="显示字段"
                Sql="SELECT ... FROM ..."
            />
        </List>
        
        <!-- 事件绑定 -->
        <EventSet>
            <Set EventName="onchange" EventType="Javascript" EventValue="functionName()"/>
        </EventSet>
    </XItem>
</XItems>
```

---

## 2. 数据库表到 EWA 配置转换规则

### 2.1 转换流程

```
数据库表结构
    ↓
1. 读取表元数据 (表名、字段、类型、注释)
    ↓
2. 选择 Frame 类型 (Frame / ListFrame)
    ↓
3. 生成 Page 配置
    ↓
4. 生成 Action 和 SQL
    ↓
5. 生成 XItems 字段映射
    ↓
6. 注入脚本和样式
    ↓
EWA XML 配置文件
```

### 2.2 表元数据映射规则

#### 2.2.1 表名 → XML 名称

| 数据库表名 | 命名规则           | XML Name 示例              |
| ---------- | ------------------ | -------------------------- |
| `ADM_USER` | 全大写，下划线分隔 | `ADM_USER.Frame.NewModify` |
| `CRM_COM`  | 全大写，下划线分隔 | `CRM_COM.F.Register`       |
| `ENQ_JNY`  | 全大写，下划线分隔 | `ENQ_JNY.LF.View`          |

**规则**:
- 主表操作：`表名.Frame.类型`
- 列表视图：`表名.ListFrame.类型`
- 简写形式：`表名.F.类型` (Frame)、`表名.LF.类型` (ListFrame)

#### 2.2.2 字段类型 → DataType 映射

| SQL Server / MySQL                               | Java/DTTable | EWA DataType |
| ------------------------------------------------ | ------------ | ------------ |
| `VARCHAR`, `NVARCHAR`, `CHAR`, `TEXT`            | `String`     | `String`     |
| `INT`, `INTEGER`, `SMALLINT`, `TINYINT`          | `int`        | `Int`        |
| `DECIMAL`, `NUMERIC`, `MONEY`, `FLOAT`, `DOUBLE` | `double`     | `Number`     |
| `DATETIME`, `DATE`, `TIMESTAMP`                  | `Date`       | `Date`       |
| `BIT`, `BOOLEAN`                                 | `boolean`    | `Boolean`    |

#### 2.2.3 字段注释 → 多语言描述

```xml
<!-- 数据库注释：登录名|Login name -->
<XItem Name="ADM_LID">
    <DescriptionSet>
        <Set Info="登录名" Lang="zhcn" Memo=""/>
        <Set Info="Login name" Lang="enus" Memo=""/>
    </DescriptionSet>
</XItem>
```

**解析规则**:
- 注释格式：`中文描述 | 英文描述`
- 仅中文：`中文描述` → 英文使用相同内容或空值

### 2.3 标准 CRUD SQL 生成规则

#### 2.3.1 查询 SQL (OnPageLoad)

```sql
-- 单表查询模板
SELECT [字段列表] 
FROM [表名] 
WHERE [主键] = @[主键]
    AND SUP_ID = @G_SUP_ID  -- 如果存在 SUP_ID 字段
```

#### 2.3.2 新增 SQL (OnNew)

```sql
-- INSERT 模板
INSERT INTO [表名] (
    [字段 1], [字段 2], ..., 
    SUP_ID, ADM_ID, CDATE
) VALUES (
    @[字段 1], @[字段 2], ...,
    @G_SUP_ID, @G_ADM_ID, @sys_date
)
```

#### 2.3.3 修改 SQL (OnModify)

```sql
-- UPDATE 模板
UPDATE [表名] SET
    [字段 1] = @[字段 1],
    [字段 2] = @[字段 2],
    ...
    MDATE = @sys_date
WHERE [主键] = @[主键]
    AND SUP_ID = @G_SUP_ID
```

#### 2.3.4 删除 SQL (OnFrameDelete)

```sql
-- 软删除模板 (推荐)
UPDATE [表名] SET 
    A_STATUS = 'DEL', 
    A_MDATE = @sys_date 
WHERE [主键] = @[主键]
    AND SUP_ID = @G_SUP_ID

-- 或物理删除
DELETE FROM [表名] 
WHERE [主键] = @[主键]
    AND SUP_ID = @G_SUP_ID
```

### 2.4 字段 Tag 类型选择规则

| 字段特征                       | Tag 类型                | 说明       |
| ------------------------------ | ----------------------- | ---------- |
| 主键、自增 ID                  | `span`                  | 只读显示   |
| 普通字符串 (长度<100)          | `text`                  | 文本输入框 |
| 密码字段 (含 PWD 关键字)       | `password`              | 密码框     |
| 长文本 (长度>200 或 TEXT 类型) | `textarea`              | 多行文本   |
| 日期类型                       | `date`                  | 日期选择器 |
| 数字类型                       | `text` (Format="Money") | 数字输入   |
| 外键关联                       | `select` / `droplist`   | 下拉列表   |
| 布尔值                         | `checkbox`              | 复选框     |
| 邮箱格式                       | `text` (Valid="Email")  | 邮箱验证   |
| 手机号                         | `text` (Valid="Mobile") | 手机验证   |

### 2.5 外键关联处理规则

#### 2.5.1 识别外键

```sql
-- 通过命名约定识别
XXX_ID → 关联 XXX 表的主键

-- 示例
CRM_COM_ID → CRM_COM.CRM_COM_ID
CITY_ID → CITY.CITY_ID
ADM_ID → ADM_USER.ADM_ID
```

#### 2.5.2 生成下拉列表 SQL

```xml
<XItem Name="CITY_ID">
    <Tag><Set Tag="select"/></Tag>
    <DescriptionSet>
        <Set Info="城市" Lang="zhcn" Memo=""/>
    </DescriptionSet>
    <List>
        <Set 
            ValueField="CITY_ID" 
            DisplayField="CITY_NAME"
            Sql="SELECT CITY_ID, CITY_NAME FROM CITY ORDER BY CITY_NAME"
        />
    </List>
</XItem>
```

### 2.6 特殊字段处理规则

#### 2.6.1 系统字段 (自动生成/维护)

| 字段名                 | 处理方式                   |
| ---------------------- | -------------------------- |
| `CDATE`, `CREATE_TIME` | 新增时自动填入 `@sys_date` |
| `MDATE`, `UPDATE_TIME` | 修改时自动填入 `@sys_date` |
| `SUP_ID`               | 自动填入 `@G_SUP_ID`       |
| `ADM_ID`               | 自动填入 `@G_ADM_ID`       |
| `*_STATUS`             | 软删除标记，默认 `USED`    |
| `UNID`, `GUID`         | 自动填入 `@SYS_UNID`       |

#### 2.6.2 逻辑删除字段

如果表包含 `*_STATUS` 或类似字段：
- 删除操作使用软删除 (UPDATE *_STATUS='DEL')
- 查询条件添加 `AND *_STATUS='USED'`

### 2.7 脚本注入规则

#### 2.7.1 标准表单提交前校验

```javascript
(function() {
    var ewa = EWA.F.FOS["@SYS_FRAME_UNID"];
    ewa.DoPostBefore = function() {
        // 1. 检查必填项
        if(!ewa.CheckValidAll()){
            return false;
        }
        // 2. 自定义校验逻辑
        // ...
        return true;
    };
})();
```

#### 2.7.2 列表Frame 标准脚本

```javascript
function addNewCustomer() {
    var p = UrlParas();
    p += "&EWA_MTYPE=N";
    EWA.UI.Dialog.OpenReloadClose('@SYS_FRAME_UNID', '@xmlname', '表名.Frame.NewModify', false, p);
}

function modifyCustomer(id) {
    var p = UrlParas();
    p += "&EWA_MTYPE=M&主键=" + id;
    EWA.UI.Dialog.OpenReloadClose('@SYS_FRAME_UNID', '@xmlname', '表名.Frame.NewModify', false, p);
}
```

---

## 3. 转换工具设计

### 3.1 输入参数

| 参数         | 说明           | 示例                                   |
| ------------ | -------------- | -------------------------------------- |
| `tableName`  | 数据库表名     | `CRM_COM`                              |
| `frameType`  | Frame 类型     | `Frame` / `ListFrame`                  |
| `mode`       | 模式           | `NM`(新增修改) / `V`(查看) / `M`(管理) |
| `dataSource` | 数据源         | `globaltravel`                         |
| `fields`     | 选中的字段列表 | `["CRM_COM_NAME", "CRM_COM_ADDR"]`     |

### 3.2 输出产物

```
输出目录/
├── 表名.Frame.类型.xml      # 生成的 EWA 配置文件
└── 表名.Frame.类型.sql      # 可选：SQL 脚本备份
```

### 3.3 核心转换类设计 (Java)

```java
public class Tb2EwaCfgConverter {
    
    // 1. 读取表元数据
    public TableMetadata loadTableMetadata(String tableName);
    
    // 2. 生成 Page 配置
    public PageConfig generatePageConfig(TableMetadata table, FrameType type);
    
    // 3. 生成 Action 和 SQL
    public ActionConfig generateActionConfig(TableMetadata table, String[] fields);
    
    // 4. 生成 XItems
    public List<XItemConfig> generateXItems(TableMetadata table, String[] fields);
    
    // 5. 生成脚本
    public ScriptConfig generateScriptConfig(FrameType type, Mode mode);
    
    // 6. 输出 XML
    public void writeXml(EwaConfig config, String outputPath);
}
```

### 3.4 字段类型推断流程

```java
public String mapToEwaDataType(String sqlType, int columnSize) {
    switch (sqlType.toUpperCase()) {
        case "VARCHAR":
        case "NVARCHAR":
        case "CHAR":
        case "TEXT":
            return "String";
        case "INT":
        case "INTEGER":
        case "SMALLINT":
            return "Int";
        case "DECIMAL":
        case "NUMERIC":
        case "MONEY":
            return "Number";
        case "DATETIME":
        case "DATE":
            return "Date";
        case "BIT":
            return "Boolean";
        default:
            return "String";
    }
}

public String mapToTag(String sqlType, int columnSize, String columnName) {
    if (columnName.toUpperCase().contains("PWD")) {
        return "password";
    }
    if (sqlType.equals("TEXT") || columnSize > 200) {
        return "textarea";
    }
    if (columnName.toUpperCase().endsWith("_ID")) {
        return "select"; // 外键
    }
    if (Arrays.asList("DATE", "DATETIME").contains(sqlType)) {
        return "date";
    }
    return "text";
}
```

---

## 4. 示例转换

### 4.1 输入：数据库表结构

```sql
CREATE TABLE CRM_COM (
    CRM_COM_ID INT IDENTITY(1,1) PRIMARY KEY,  -- 公司 ID
    CRM_COM_NAME NVARCHAR(200) NOT NULL,        -- 公司名称 | Company Name
    CRM_COM_ADDR NVARCHAR(500),                 -- 公司地址 | Address
    CRM_COM_TELE NVARCHAR(50),                  -- 电话 | Telephone
    CRM_COM_EMAIL NVARCHAR(100),                -- 邮箱 | Email
    CRM_COM_NOTE NTEXT,                         -- 备注 | Note
    SUP_ID INT,                                 -- 供应商 ID
    ADM_ID INT,                                 -- 管理员 ID
    CDATE DATETIME,                             -- 创建时间
    MDATE DATETIME                              -- 修改时间
);
```

### 4.2 输出：EWA XML 配置 (Frame.NM)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<EasyWebTemplates>
    <EasyWebTemplate 
        Name="CRM_COM.Frame.NewModify"
        CreateDate="2025-03-27 10:00:00"
        UpdateDate="2025-03-27 10:00:00"
    >
        <Page>
            <Name><Set Name="CRM_COM.Frame.NewModify"/></Name>
            <FrameTag><Set FrameTag="Frame"/></FrameTag>
            <DataSource><Set DataSource="globaltravel"/></DataSource>
            <Acl><Set Acl="com.gdxsoft.web.acl.BusinessImpl"/></Acl>
            <Log><Set Log="com.gdxsoft.web.log.EwaScriptLog"/></Log>
            <DescriptionSet>
                <Set Info="公司信息维护" Lang="zhcn" Memo=""/>
                <Set Info="Company Information" Lang="enus" Memo=""/>
            </DescriptionSet>
            <Size><Set FrameCols="C2" Width="700"/></Size>
        </Page>
        
        <Action>
            <ActionSet>
                <Set Type="OnPageLoad">
                    <CallSet><Set CallName="OnPageLoad SQL" CallType="SqlSet"/></CallSet>
                </Set>
                <Set Type="OnPagePost">
                    <CallSet>
                        <Set CallName="OnNew SQL" CallType="SqlSet" Test="'@EWA_MTYPE'='N'"/>
                        <Set CallName="OnModify SQL" CallType="SqlSet" Test="'@EWA_MTYPE'='M'"/>
                    </CallSet>
                </Set>
            </ActionSet>
            
            <SqlSet>
                <Set Name="OnPageLoad SQL" SqlType="query">
                    <Sql><![CDATA[
                        SELECT * FROM CRM_COM 
                        WHERE CRM_COM_ID = @CRM_COM_ID
                        AND SUP_ID = @G_SUP_ID
                    ]]></Sql>
                </Set>
                <Set Name="OnNew SQL" SqlType="update">
                    <Sql><![CDATA[
                        INSERT INTO CRM_COM (
                            CRM_COM_NAME, CRM_COM_ADDR, CRM_COM_TELE, 
                            CRM_COM_EMAIL, CRM_COM_NOTE,
                            SUP_ID, ADM_ID, CDATE
                        ) VALUES (
                            @CRM_COM_NAME, @CRM_COM_ADDR, @CRM_COM_TELE,
                            @CRM_COM_EMAIL, @CRM_COM_NOTE,
                            @G_SUP_ID, @G_ADM_ID, @sys_date
                        )
                    ]]></Sql>
                </Set>
                <Set Name="OnModify SQL" SqlType="update">
                    <Sql><![CDATA[
                        UPDATE CRM_COM SET
                            CRM_COM_NAME = @CRM_COM_NAME,
                            CRM_COM_ADDR = @CRM_COM_ADDR,
                            CRM_COM_TELE = @CRM_COM_TELE,
                            CRM_COM_EMAIL = @CRM_COM_EMAIL,
                            CRM_COM_NOTE = @CRM_COM_NOTE,
                            MDATE = @sys_date
                        WHERE CRM_COM_ID = @CRM_COM_ID
                            AND SUP_ID = @G_SUP_ID
                    ]]></Sql>
                </Set>
            </SqlSet>
        </Action>
        
        <XItems>
            <XItem Name="CRM_COM_ID">
                <Tag><Set Tag="span"/></Tag>
                <DescriptionSet><Set Info="公司 ID" Lang="zhcn"/></DescriptionSet>
                <DataItem><Set DataField="CRM_COM_ID" DataType="Int"/></DataItem>
            </XItem>
            
            <XItem Name="CRM_COM_NAME">
                <Tag><Set Tag="text"/></Tag>
                <DescriptionSet>
                    <Set Info="公司名称" Lang="zhcn"/>
                    <Set Info="Company Name" Lang="enus"/>
                </DescriptionSet>
                <IsMustInput><Set IsMustInput="1"/></IsMustInput>
                <MaxMinLength><Set MaxLength="200"/></MaxMinLength>
                <DataItem><Set DataField="CRM_COM_NAME" DataType="String"/></DataItem>
            </XItem>
            
            <XItem Name="CRM_COM_ADDR">
                <Tag><Set Tag="text"/></Tag>
                <DescriptionSet>
                    <Set Info="公司地址" Lang="zhcn"/>
                    <Set Info="Address" Lang="enus"/>
                </DescriptionSet>
                <DataItem><Set DataField="CRM_COM_ADDR" DataType="String"/></DataItem>
            </XItem>
            
            <XItem Name="CRM_COM_TELE">
                <Tag><Set Tag="text"/></Tag>
                <DescriptionSet><Set Info="电话" Lang="zhcn"/></DescriptionSet>
                <DataItem><Set DataField="CRM_COM_TELE" DataType="String"/></DataItem>
            </XItem>
            
            <XItem Name="CRM_COM_EMAIL">
                <Tag><Set Tag="text"/></Tag>
                <DescriptionSet><Set Info="邮箱" Lang="zhcn"/></DescriptionSet>
                <DataItem><Set DataField="CRM_COM_EMAIL" DataType="String" Valid="Email"/></DataItem>
            </XItem>
            
            <XItem Name="CRM_COM_NOTE">
                <Tag><Set Tag="textarea"/></Tag>
                <DescriptionSet><Set Info="备注" Lang="zhcn"/></DescriptionSet>
                <DataItem><Set DataField="CRM_COM_NOTE" DataType="String"/></DataItem>
            </XItem>
            
            <XItem Name="butOk">
                <Tag><Set Tag="submit"/></Tag>
                <DescriptionSet><Set Info="确定" Lang="zhcn"/></DescriptionSet>
            </XItem>
            
            <XItem Name="butClose">
                <Tag><Set Tag="button"/></Tag>
                <DescriptionSet><Set Info="关闭" Lang="zhcn"/></DescriptionSet>
            </XItem>
        </XItems>
    </EasyWebTemplate>
</EasyWebTemplates>
```

---

## 5. 最佳实践建议

### 5.1 命名规范

1. **XML 名称**: `表名.Frame.用途` 或 `表名.ListFrame.用途`
2. **Action 名称**: 
   - 标准：`OnPageLoad`, `OnPagePost`, `OnFrameDelete`
   - 自定义：`SAct0-9` (查询), `UAct0-9` (更新)
3. **SQL 名称**: 与 Action 对应，后缀 `SQL`

### 5.2 安全建议

1. 所有 SQL 使用参数化查询 (`@参数名`)
2. 多租户数据隔离 (`SUP_ID = @G_SUP_ID`)
3. 敏感字段加密 (`IsEncrypt="1"`)
4. 操作日志记录 (`<Log>` 配置)

### 5.3 性能优化

1. 列表分页 (`<PageSize PageSize="50"/>`)
2. 缓存配置 (`<Cached CachedSeconds="300"/>`)
3. 避免 SELECT * (明确字段列表)
4. 外键下拉列表数据缓存

---

## 6. 系统模板定义详解 (EwaDefine.xml)

### 6.1 Frame 模板类型

#### 6.1.1 NM 模板 (New/Modify)

最常用的表单模板，用于数据的录入和编辑。

**预置按钮**:
- `butOk` (submit) - 确定
- `butClose` (button) - 关闭

**预置动作**:
```xml
<Action Name="OnPageLoad" SqlType="query"
    Test="'@EWA_MTYPE'='M' or '@EWA_MTYPE'='C'"
    Sql="this.Fields.GetSqlSelect()"/>

<Action Name="OnNew" ActionName="OnPagePost" SqlType="update"
    Test="'@EWA_MTYPE'='N' or '@EWA_MTYPE'='C'"
    Sql="this.Fields.GetSqlNew()"/>

<Action Name="OnModify" ActionName="OnPagePost" SqlType="update"
    Test="'@EWA_MTYPE'='M'"
    Sql="this.Fields.GetSqlUpdate()"/>
```

**预置脚本** (DoPostBefore):
```javascript
(function(){
    const ewa = EWA.F.FOS["@SYS_FRAME_UNID"];
    ewa.DoPostBefore = function(){
        // 附加数据处理
        // ewa.PostAddData = {add1:'a',add2:2};
        
        // 附加检查合法性
        // if(getObj('#aaa').val() == ''){
        //     return false;
        // }
        return true;
    };
})();
```

**CSS 注入**:
```css
#f_@SYS_FRAME_UNID .EWA_TD_M{width:400px;}
```

#### 7.1.2 N 模板 (New Only)

仅用于新增记录的模板。

**预置动作**:
```xml
<Action Name="OnPagePost" SqlType="update"
    Sql="this.Fields.GetSqlNew()"/>
```

#### 7.1.3 M 模板 (Modify Only)

仅用于修改记录的模板。

**预置动作**:
```xml
<Action Name="OnPageLoad" SqlType="query"
    Sql="this.Fields.GetSqlSelect()"/>

<Action Name="OnPagePost" SqlType="update"
    Sql="this.Fields.GetSqlUpdate()"/>
```

#### 7.1.4 V 模板 (View Only)

查看页面，所有对象显示为 SPAN。

**预置按钮**:
- `butClose` (button) - 关闭

**预置动作**:
```xml
<Action Name="OnPageLoad" SqlType="query"
    Sql="this.Fields.GetSqlSelect()"/>
```

#### 7.1.5 Login 模板 (登录专用)

用户登录专用模板。

**预置按钮**:
- `_EWA_ValidCode` (valid) - 验证码
- `butOk` (submit) - 登录

**预置动作**:
```xml
<Action Name="OnPagePost" SqlType="update"
    Sql="this.Fields.GetSqlLogin()"/>
```

### 6.2 ListFrame 模板类型

#### 6.2.1 V 模板 (View Only)

浏览数据，无附加功能键。

**预置动作**:
```xml
<Action Name="OnPageLoad" SqlType="query"
    Sql="this.Fields.GetSqlSelectLF()"/>

<Action Name="SAct0" SqlType="query" Sql="'-- enter your sql'"/>
<Action Name="SAct1" SqlType="query" Sql="'-- enter your sql'"/>
<Action Name="SAct2" SqlType="query" Sql="'-- enter your sql'"/>
<Action Name="UAct0" SqlType="update" Sql="'-- enter your sql'"/>
<Action Name="UAct1" SqlType="update" Sql="'-- enter your sql'"/>
<Action Name="UAct2" SqlType="update" Sql="'-- enter your sql'"/>
```

**预置脚本** (ReloadAfter):
```javascript
(function(){
    const ewa = EWA.F.FOS['@SYS_FRAME_UNID'];
    ewa.ReloadAfter = function(httpReferer){
        if(httpReferer) {
            let uref = new EWA_UrlClass(httpReferer);
            let mtype   = uref.GetParameter("EWA_MTYPE");
            let refXml  = uref.GetParameter("XMLNAME");
            let refItem = uref.GetParameter("ITEMNAME");
        }
        // ewa.Merge('fromId', 'toId'); //合并单元格
    };
})();
```

##### 6.2.2 M 模板 (Modify)

浏览并修改，同时生成关联的 Frame。

**预置按钮**:
- `butNew` (button) - 新增
- `butModify` (button) - 修改
- `butCopy` (button) - 复制
- `butDelete` (button) - 删除
- `butRestore` (button) - 恢复

**预置动作**:
```xml
<Action Name="OnPageLoad" SqlType="query"
    Sql="this.Fields.GetSqlSelectLF()"/>

<Action Name="OnFrameDelete" SqlType="update"
    Sql="this.Fields.GetSqlDeleteA()"/>

<Action Name="OnFrameRestore" SqlType="update"
    Sql="this.Fields.GetSqlRestore()"/>
```

**预置脚本** (ext_NewOrModifyOrCopy):
```javascript
ewa.ext_NewOrModifyOrCopy = function(mtype, pkParas){
    const u1 = new EWA_UrlClass(ewa.Url);
    u1.RemoveEwa();
    u1.AddParameter("EWA_MTYPE", mtype);
    let paras = u1.GetParas();
    if(pkParas){
        paras += '&' + pkParas;
    }
    EWA.UI.Dialog.OpenReloadClose(
        '@SYS_FRAME_UNID',
        '@xmlName',
        '{@define.Fields.TableName}.F.NM',
        false,
        paras
    );
};
```

### 6.3 标准 SQL 生成方法

`EwaDefine.xml` 中定义了以下字段生成方法：

| 方法名                   | 用途           | 生成的 SQL 类型                |
| ------------------------ | -------------- | ------------------------------ |
| `GetSqlSelect()`         | Frame 查询     | SELECT 单条记录                |
| `GetSqlNew()`            | Frame 新增     | INSERT INTO                    |
| `GetSqlUpdate()`         | Frame 修改     | UPDATE SET                     |
| `GetSqlLogin()`          | Frame 登录     | 验证用户                       |
| `GetSqlSelectLF()`       | ListFrame 查询 | SELECT 多条记录                |
| `GetSqlDeleteA()`        | ListFrame 删除 | UPDATE A_STATUS='DEL' (软删除) |
| `GetSqlRestore()`        | ListFrame 恢复 | UPDATE A_STATUS='USED'         |
| `GetSqlRelationSelect()` | 关系表查询     | SELECT 关系数据                |
| `GetSqlRelationUpdate()` | 关系表更新     | INSERT/UPDATE 关系             |

### 6.4 PageInfo 预定义

每个模板预定义了 3 个 PageInfo 用于消息传递：

```xml
<PageInfos>
    <PageInfo Name="msg0">
        <DescriptionSet>
            <Set Lang="zhcn" Info="消息 0"/>
            <Set Lang="enus" Info="Msg0"/>
        </DescriptionSet>
    </PageInfo>
    <PageInfo Name="msg1">
        <DescriptionSet>
            <Set Lang="zhcn" Info="消息 1"/>
            <Set Lang="enus" Info="Msg1"/>
        </DescriptionSet>
    </PageInfo>
    <PageInfo Name="msg2">
        <DescriptionSet>
            <Set Lang="zhcn" Info="消息 2"/>
            <Set Lang="enus" Info="Msg2"/>
        </DescriptionSet>
    </PageInfo>
</PageInfos>

 
## 7. 工具菜单功能 (EwaDefineConfig.xml)

### 7.1 组件管理 (Group)

| 功能 | 说明 | 路径 |
|-----|------|------|
| 创建和发布组件 | 将当前配置打包为组件 | `/ewa/ewa_module.xml` |
| 下载和导入组件 | 从文件导入组件 | `/ewa/ewa_module.xml` |
| 组件交换中心 | 访问组件网站 | 外部链接 |

### 7.2 开发工具 (Utils)

| 功能 | 说明 | 路径 |
|-----|------|------|
| Restful API | RESTful 接口定义 | `/ewa/ewa_restful.xml` |
| 数据库类映射 | 数据库表映射到 Java 类 | `/ewa/ewa.xml?linkType=CLASS` |
| ewa_conf.xml 配置 | 配置帮助 | `/ewa/ewa_conf_help.xml` |
| 查看缓存 | 查看系统缓存 | `/ewa/ewa.xml#cache_list` |
| 跟踪 | 系统跟踪日志 | `/ewa/ewa.xml#trace` |
| HSQL 缓存 | HSQL 缓存管理 | `/ewa/ewa.xml#CACHED.LF.M` |
| Frame Debug | Frame 调试 | `/ewa/ewa.xml#FRAME_DEBUG.LF.M` |
| 垂直表 | 垂直表管理 | `/ewa/data_hor.xml` |
| 流程设计 | 工作流设计器 | `/ewa/ewa_wf.xml` |
| 流程应用 | 工作流应用 | `/ewa/ewa_wf.xml` |
| 文件同步 | 文件同步工具 | `/ewa/ewa.xml#remote_sync` |
| 移动应用设计 | APP 设计器 | `/ewa/app_cfg.xml` |

### 7.3 用户管理 (Admin)

| 功能 | 说明 | 路径 |
|-----|------|------|
| 用户列表 | 管理员列表 | `/ewa/m.xml#admlist` |
| 更改我的密码 | 修改当前用户密码 | `/ewa/m.xml#ChangePWD` |
| BUG 跟踪 | 系统 BUG 跟踪 | `/ewa/tmp_img_resze.xml` |

---

## 8. 最佳实践建议

### 8.1 命名规范

#### 8.1.1 XML 名称命名规则

| 类型 | 命名格式 | 示例 | 说明 |
|-----|---------|------|------|
| **列表 (ListFrame)** | `{table_name}.Lf.M` | `CRM_COM.Lf.M` | 管理模式 |
| **列表 (ListFrame)** | `{table_name}.Lf.V` | `CRM_COM.Lf.V` | 浏览模式 |
| **表单 (Frame)** | `{table_name}.F.NM` | `CRM_COM.F.NM` | 新增/修改模式 |
| **表单 (Frame)** | `{table_name}.F.V` | `CRM_COM.F.V` | 仅查看模式 |
| **树 (Tree)** | `{table_name}.Tree.M` | `ADM_DEPT.Tree.M` | 树管理模式 |
| **树 (Tree)** | `{table_name}.Tree.V` | `ADM_DEPT.Tree.V` | 树查看模式 |

#### 8.1.2 表单模式区分 (EWA_MTYPE 参数)

| EWA_MTYPE | 操作 | SQL 生成方法 |
|----------|------|------------|
| `N` | 新增 | `GetSqlNew()` |
| `M` | 修改 | `GetSqlUpdate()` |
| `C` | 复制 | `GetSqlNew()` |

#### 8.1.3 Action 命名规范

| Action 名称 | 用途 |
|------------|------|
| `OnPageLoad` | 页面加载 |
| `OnPagePost` | 提交处理 |
| `OnFrameDelete` | 删除操作 |
| `OnFrameRestore` | 恢复操作 |
| `ExtendAction0-9` | 自定义扩展 |

#### 8.1.4 SQL 命名规范

SQL 名称与 Action 对应，后缀 `SQL`：

```xml
<SqlSet>
    <Set Name="OnPageLoad SQL" SqlType="query"/>
    <Set Name="OnNew SQL" SqlType="update"/>
    <Set Name="OnModify SQL" SqlType="update"/>
    <Set Name="OnFrameDelete SQL" SqlType="update"/>
</SqlSet>
```

#### 8.1.5 按钮命名规范

| 按钮名称 | Tag 类型 | 用途 |
|---------|---------|------|
| `butOk` | `submit` | 确定/提交 |
| `butClose` | `button` | 关闭/取消 |
| `butNew` | `button` | 新增 |
| `butModify` | `button` | 修改 |
| `butDelete` | `button` | 删除 |
| `butRestore` | `button` | 恢复 |

### 8.2 安全建议

1. 所有 SQL 使用参数化查询 (`@参数名`)
2. 多租户数据隔离 (`SUP_ID = @G_SUP_ID`)
3. 敏感字段加密 (`IsEncrypt="1"`)
4. 操作日志记录 (`<Log>` 配置)
5. ACL 权限控制 (`<Acl>` 配置)

### 8.3 性能优化

1. 列表分页 (`<PageSize PageSize="50"/>`)
2. 缓存配置 (`<Cached CachedSeconds="300"/>`)
3. 避免 SELECT * (明确字段列表)
4. 外键下拉列表数据缓存

### 8.4 脚本注入规范

1. 使用 IIFE 闭包包裹：`(function(){ ... })();`
2. 使用 `@SYS_FRAME_UNID` 获取当前 Frame 实例
3. 统一使用 `getObj()` 辅助函数选择 DOM
4. `DoPostBefore` 返回 `false` 阻止提交
5. `ReloadAfter` 处理刷新后的附加逻辑

### 8.5 CSS 注入规范

1. 使用 `#f_@SYS_FRAME_UNID` 选择器限定 Frame 范围
2. 使用 `#EWA_LF_@SYS_FRAME_UNID` 选择器限定 ListFrame 范围
3. 避免全局样式污染

---

## 9. Action 与执行单元关系详解

### 9.1 核心结构关系图

```
<Action>
    ├── <ActionSet>           # 动作集合，定义触发条件
    │       └── <Set Type="OnPageLoad">
    │           └── <CallSet>
    │               └── <Set CallName="xxx SQL" CallType="SqlSet"/>
    │
    ├── <SqlSet>              # SQL 语句定义
    │       └── <Set Name="xxx SQL" SqlType="query">
    │           └── <Sql><![CDATA[SELECT ...]]></Sql>
    │
    ├── <ClassSet>            # Java 类调用定义
    │       └── <Set ClassName="com.xxx.MapTo" MethodName="map2s"/>
    │
    ├── <ScriptSet>           # JavaScript 脚本定义
    │       └── <Set Name="xxx" ScriptType="javascript">
    │           └── <Script><![CDATA[alert('...');]]></Script>
    │
    ├── <JSONSet>             # JSON 数据处理
    ├── <XmlSet>              # XML 数据处理
    ├── <XmlSetData>          # XML 数据源
    └── <UrlSet>              # URL 调用定义
```

### 9.2 ActionSet - 动作触发器

**作用**: 定义何时执行哪些操作，是系统的**事件驱动核心**。

**标准动作类型**:

| Type | 触发时机 | 说明 |
|------|---------|------|
| `OnPageLoad` | 页面加载时 | 加载初始数据 |
| `OnPagePost` | 表单提交时 | 处理数据提交 |
| `OnFrameDelete` | 删除操作时 | 删除选中记录 |
| `OnFrameRestore` | 恢复操作时 | 恢复已删除记录 |
| `ExtendAction0-9` | 自定义按钮 | 扩展功能 |
| `SAct0-9` | 自定义查询 | 扩展查询 |
| `UAct0-9` | 自定义更新 | 扩展更新 |

### 9.3 CallType - 执行单元类型

`CallType` 指定了要调用的执行单元类型：

| CallType | 对应 Set | 用途 |
|---------|---------|------|
| `SqlSet` | `<SqlSet>` | 执行 SQL 语句 |
| `ClassSet` | `<ClassSet>` | 调用 Java 类方法 |
| `ScriptSet` | `<ScriptSet>` | 执行 JavaScript |
| `JSONSet` | `<JSONSet>` | JSON 数据处理 |
| `XmlSet` | `<XmlSet>` | XML 数据处理 |
| `UrlSet` | `<UrlSet>` | URL 调用 |

### 9.4 Test 条件表达式

| Test 表达式 | 说明 | 示例 |
|------------|------|------|
| `""` | 无条件执行 | 默认 |
| `"'@EWA_MTYPE'='N'"` | 新增模式执行 | 新增操作 |
| `"'@result'=''"` | 前一步成功执行 | 检查结果 |

---

## 12. 基于 EwaConfig.xml 规则模板创建业务 XML

### 12.1 EwaConfig.xml 结构概述

`EwaConfig.xml` 是 EWA 系统的**配置规则模板**，定义了创建业务 XML 时的所有合法节点、属性及其取值规则。

```xml
<?xml version="1.0" encoding="utf-8"?>
<EasyWebConfig>
    <!-- 1. Frame 类型定义 -->
    <Frames>
        <Frame Name="Frame" FrameClassName="..." ActionClassName="..."/>
        <Frame Name="ListFrame" FrameClassName="..." ActionClassName="..."/>
        <!-- 共 10 种 Frame 类型 -->
    </Frames>
    
    <!-- 2. 页面元素 (XItem) 定义 -->
    <Items>
        <XItems>
            <XItem Name="text" HtmlTag="input$text" Parameters="..."/>
            <XItem Name="textarea" HtmlTag="textarea" Parameters="..."/>
            <!-- 共 30+ 种元素类型 -->
        </XItems>
        
        <!-- 3. 参数配置规则 -->
        <XItemParameters>
            <XItemParameter Name="Tag" IsSet="0" IsMulti="1" UIGroup="Main">
                <Values>
                    <Value Name="Tag" Type="ref" Ref="EasyWebConfig/XItems/XItem"/>
                    <Value Name="IsLFEdit" Type="group"/>
                </Values>
            </XItemParameter>
            <!-- 共 50+ 个参数配置项 -->
        </XItemParameters>
        
        <!-- 4. 全局值列表 -->
        <XGroupValues>
            <XGroupValue Name="DataType" Values=",String,Int,BigInt,Boolean,Number,Date,Binary"/>
            <XGroupValue Name="Format" Values=",Date,Money,LeastMoney..."/>
            <!-- 共 40+ 个全局值定义 -->
        </XGroupValues>
    </Items>
</EasyWebConfig>
```

### 12.2 XItemParameters - 参数配置规则

每个 `<XItemParameter>` 定义了一个可配置的参数项：

```xml
<XItemParameter 
    Name="参数名" 
    IsShow="0|1"           <!-- 是否在 UI 显示 -->
    IsSet="0|1"            <!-- 是否为 Set 结构 (多语言) -->
    IsMulti="0|1"          <!-- 是否多值 -->
    UIGroup="Main|Data|UI|Valid|Others"
    Frames="Frame|ListFrame"  <!-- 适用的 Frame 类型 -->
    Html=' id="@Name" '    <!-- HTML 属性模板 -->
    IsJsShow="0|1"         <!-- 是否在 JS 中显示 -->
>
    <DescriptionSet>
        <Set Lang="zhcn" Info="中文描述"/>
        <Set Lang="enus" Info="English Description"/>
    </DescriptionSet>
    <Values>
        <Value 
            Name="值名称" 
            Type="string|int|group|ref" 
            CreateValue="this.Field.Name"
            Ref="EasyWebConfig/XItems/XItem"
        >
            <DescriptionSet>...</DescriptionSet>
        </Value>
    </Values>
</XItemParameter>
```

### 12.3 XGroupValue - 全局值列表

定义所有参数可选的预定义值：

```xml
<XGroupValues>
    <!-- 数据类型 -->
    <XGroupValue Name="DataType" Default="" 
        Values=",String,Int,BigInt,Boolean,Number,Date,Binary"/>
    
    <!-- 格式化 -->
    <XGroupValue Name="Format" Default="0" 
        Values=",Date,DateShort,DateLong,DateTime,Money,LeastMoney..."/>
    
    <!-- 验证规则 -->
    <XGroupValue Name="Valid" Default="" 
        Values=",Email,Mobile,Phone,Url,Number,Int,Date..."/>
    
    <!-- 是/否值 -->
    <XGroupValue Name="FrameOneCell" Default="" Values="no,yes"/>
    <XGroupValue Name="UpDelete" Default="no" Values="no,yes"/>
    
    <!-- 列表显示类型 -->
    <XGroupValue Name="ListShowType" Default="0" 
        Values=", Text-Key, Key-Text"/>
    
    <!-- 事件类型 -->
    <XGroupValue Name="EventType" Default="0" Values="Javascript,Ajax"/>
</XGroupValues>
```

### 12.4 基于规则模板创建 XML 的流程

#### 12.4.1 步骤 1: 读取规则模板

```java
// 1. 加载 EwaConfig.xml
EwaConfig config = new EwaConfig();
config.load("/system.xml/EwaConfig.xml");

// 2. 获取所有 XItem 定义
List<XItem> xItems = config.getXItems();

// 3. 获取所有参数规则
List<XItemParameter> parameters = config.getXItemParameters();

// 4. 获取所有全局值
Map<String, XGroupValue> groupValues = config.getXGroupValues();
```

#### 12.4.2 步骤 2: 根据规则创建节点

**规则**: 所有节点和属性必须来自 `XItemParameters` 定义，**禁止硬编码**

```java
public class XmlCreator {
    
    // 创建 XItem 节点
    public Element createXItem(Field field, XItem xItemTemplate) {
        Element xItem = doc.createElement("XItem");
        xItem.setAttribute("Name", field.getName());
        
        // 遍历该 XItem 类型的所有参数规则
        List<XItemParameter> params = getParametersForXItem(xItemTemplate);
        
        for (XItemParameter param : params) {
            // 根据参数类型创建节点
            if (param.isSet()) {
                // Set 结构（多语言）
                createSetNode(xItem, param);
            } else if (param.isMulti()) {
                // 多值结构
                createMultiNode(xItem, param);
            } else {
                // 单值结构
                createSingleNode(xItem, param);
            }
        }
        
        return xItem;
    }
    
    // 创建 Set 结构节点（多语言）
    private void createSetNode(Element parent, XItemParameter param) {
        Element setParent = doc.createElement(param.getName() + "Set");
        
        // 从 XGroupValue 获取语言选项
        XGroupValue langValues = config.getGroupValue("Lang");
        
        for (String lang : langValues.getValues()) {
            Element set = doc.createElement("Set");
            set.setAttribute("Lang", lang);
            set.setAttribute("Info", getDescription(field, lang));
            setParent.appendChild(set);
        }
        
        parent.appendChild(setParent);
    }
    
    // 创建单值节点
    private void createSingleNode(Element parent, XItemParameter param) {
        Element set = doc.createElement("Set");
        
        // 从 XGroupValue 获取可选值
        XGroupValue groupValue = config.getGroupValue(param.getName());
        if (groupValue != null) {
            // 使用预定义值
            String value = getFieldValue(field, param.getName());
            if (groupValues.getValues().contains(value)) {
                set.setAttribute(param.getName(), value);
            }
        } else {
            // 自由值
            set.setAttribute(param.getName(), getFieldValue(field, param.getName()));
        }
        
        parent.appendChild(set);
    }
}
```

#### 12.4.3 步骤 3: 验证生成的 XML

```java
public class XmlValidator {
    
    // 验证生成的 XML 是否符合规则
    public boolean validate(Element generatedXml, EwaConfig config) {
        // 1. 检查所有节点是否在 XItemParameters 中定义
        for (Element node : getAllNodes(generatedXml)) {
            if (!config.hasParameter(node.getNodeName())) {
                log.error("未定义的节点：" + node.getNodeName());
                return false;
            }
        }
        
        // 2. 检查所有属性值是否在 XGroupValue 中定义
        for (Element set : getAllSetNodes(generatedXml)) {
            for (Attr attr : set.getAttributes()) {
                String paramName = attr.getName();
                String value = attr.getValue();
                
                XGroupValue groupValue = config.getGroupValue(paramName);
                if (groupValue != null && !groupValue.getValues().contains(value)) {
                    log.error("无效的属性值：" + paramName + "=" + value);
                    return false;
                }
            }
        }
        
        return true;
    }
}
```

### 12.5 参数映射规则表

#### 12.5.1 Main 组参数

| 参数名 | Type | IsSet | IsMulti | 说明 | 示例值 |
|-------|------|-------|---------|------|--------|
| Tag | ref | 0 | 1 | 对象类型 | text, span, select |
| Name | string | 0 | 0 | 字段名称 | CRM_COM_NAME |
| GroupIndex | int | 0 | 0 | 分组索引 | 0, 1, 2 |
| InitValue | group | 0 | 0 | 初始化值 | @sys_date |
| DescriptionSet | - | 1 | 1 | 多语言描述 | zhcn/enus |

#### 12.5.2 Data 组参数

| 参数名 | Type | IsSet | IsMulti | 说明 | 可选值 |
|-------|------|-------|---------|------|--------|
| DataItem | - | 0 | 1 | 数据项配置 | - |
| DataType | group | 0 | 0 | 数据类型 | String,Int,Number,Date |
| DataField | string | 0 | 0 | 数据字段 | 数据库字段名 |
| Format | group | 0 | 0 | 格式化 | Date,Money,LeastMoney |
| Valid | group | 0 | 0 | 验证规则 | Email,Mobile,Number |
| List | - | 0 | 1 | 列表配置 | Sql, ValueField, DisplayField |

#### 12.5.3 UI 组参数

| 参数名 | Type | IsSet | IsMulti | 说明 | 可选值 |
|-------|------|-------|---------|------|--------|
| Style | - | 0 | 1 | 样式配置 | - |
| ParentStyle | - | 0 | 1 | 父级样式 | - |
| XStyle | - | 0 | 1 | 标准样式 | - |
| XStyleAlign | group | 0 | 0 | 对齐 | no, yes |
| XStyleBold | group | 0 | 0 | 粗体 | no, yes |
| XStyleColor | group | 0 | 0 | 颜色 | red, blue, green |
| XStyleNoWrap | group | 0 | 0 | 不换行 | no, yes |
| XStyleFixed | group | 0 | 0 | 固定 | no, yes |

#### 12.5.4 Valid 组参数

| 参数名 | Type | IsSet | IsMulti | 说明 | 可选值 |
|-------|------|-------|---------|------|--------|
| IsMustInput | - | 0 | 1 | 是否必填 | 0, 1 |
| MaxMinLength | - | 0 | 1 | 最大最小长度 | - |
| MaxMinValue | - | 0 | 1 | 最大最小值 | - |
| VaildEx | - | 0 | 1 | 扩展验证 | - |
| TriggerValid | group | 0 | 0 | 触发验证 | ValidSlidePuzzle |

### 12.6 完整示例：从规则到 XML

#### 12.6.1 数据库表字段

```sql
CREATE TABLE CRM_COM (
    CRM_COM_ID INT IDENTITY(1,1) PRIMARY KEY,
    CRM_COM_NAME NVARCHAR(200) NOT NULL,  -- 公司名称
    CRM_COM_ADDR NVARCHAR(500),           -- 公司地址
    CRM_COM_EMAIL NVARCHAR(100)           -- 邮箱
);
```

#### 12.6.2 根据规则生成的 XML

```xml
<XItems>
    <!-- CRM_COM_ID: 根据 XItemParameter 规则生成 -->
    <XItem Name="CRM_COM_ID">
        <!-- Tag 参数 (IsMulti=1) -->
        <Tag>
            <Set Tag="span" IsLFEdit="0"/>
        </Tag>
        
        <!-- Name 参数 (IsShow=1) -->
        <Name>
            <Set Name="CRM_COM_ID"/>
        </Name>
        
        <!-- DescriptionSet 参数 (IsSet=1, IsMulti=1) -->
        <DescriptionSet>
            <Set Info="公司 ID" Lang="zhcn" Memo=""/>
            <Set Info="Company ID" Lang="enus" Memo=""/>
        </DescriptionSet>
        
        <!-- DataItem 参数 -->
        <DataItem>
            <Set 
                DataField="CRM_COM_ID" 
                DataType="Int"
                Format=""
                Valid=""
            />
        </DataItem>
    </XItem>
    
    <!-- CRM_COM_NAME: 根据 XItemParameter 规则生成 -->
    <XItem Name="CRM_COM_NAME">
        <Tag>
            <Set Tag="text" IsLFEdit="0"/>
        </Tag>
        
        <Name>
            <Set Name="CRM_COM_NAME"/>
        </Name>
        
        <DescriptionSet>
            <Set Info="公司名称" Lang="zhcn" Memo=""/>
            <Set Info="Company Name" Lang="enus" Memo=""/>
        </DescriptionSet>
        
        <!-- IsMustInput (来自 Valid 组) -->
        <IsMustInput>
            <Set IsMustInput="1"/>
        </IsMustInput>
        
        <!-- MaxMinLength (来自 Valid 组) -->
        <MaxMinLength>
            <Set MaxLength="200" MinLength=""/>
        </MaxMinLength>
        
        <DataItem>
            <Set 
                DataField="CRM_COM_NAME" 
                DataType="String"
                Format=""
                Valid=""
            />
        </DataItem>
    </XItem>
    
    <!-- CRM_COM_EMAIL: 带验证规则 -->
    <XItem Name="CRM_COM_EMAIL">
        <Tag>
            <Set Tag="text" IsLFEdit="0"/>
        </Tag>
        
        <Name>
            <Set Name="CRM_COM_EMAIL"/>
        </Name>
        
        <DescriptionSet>
            <Set Info="邮箱" Lang="zhcn" Memo=""/>
            <Set Info="Email" Lang="enus" Memo=""/>
        </DescriptionSet>
        
        <IsMustInput>
            <Set IsMustInput="1"/>
        </IsMustInput>
        
        <DataItem>
            <Set 
                DataField="CRM_COM_EMAIL" 
                DataType="String"
                Format=""
                Valid="Email"  <!-- 来自 XGroupValue/Valid -->
            />
        </DataItem>
    </XItem>
</XItems>
```

### 12.7 规则模板的优势

| 优势 | 说明 |
|-----|------|
| **类型安全** | 所有属性值来自预定义的 XGroupValue，避免拼写错误 |
| **可扩展** | 新增 XItem 类型只需在 EwaConfig.xml 中添加定义 |
| **UI 驱动** | 配置界面可根据 XItemParameters 动态生成 |
| **验证机制** | 生成的 XML 可通过规则模板自动验证 |
| **多语言支持** | IsSet=1 的参数自动支持多语言 |

### 12.8 注意事项

1. **禁止硬编码**: 所有节点名、属性名必须来自 `XItemParameters`
2. **值验证**: 所有属性值必须在对应的 `XGroupValue` 中定义
3. **Frame 兼容性**: 某些参数仅适用于特定 Frame 类型（通过 `Frames` 属性限制）
4. **默认值**: 使用 `XGroupValue` 的 `Default` 属性作为默认值
5. **引用完整性**: `Type="ref"` 的参数引用其他定义（如 XItem 列表）

---

## 10. 附录：完整转换检查清单

| 检查项         | 说明                                  | 状态 |
| -------------- | ------------------------------------- | ---- |
| 表元数据读取   | 表名、字段、类型、注释、主键、外键    | ☐    |
| Frame 类型选择 | Frame / ListFrame / Tree              | ☐    |
| 模板变体选择   | NM / N / M / V / Login (Frame)        | ☐    |
| 模板变体选择   | V / M (ListFrame)                     | ☐    |
| 字段选择       | 勾选需要显示的字段                    | ☐    |
| 外键关联识别   | 自动识别 `_ID` 后缀字段               | ☐    |
| SQL 生成       | CRUD 四条标准 SQL                     | ☐    |
| 验证规则       | 必填、长度、格式 (Email/Mobile)       | ☐    |
| 多语言         | 中英文描述                            | ☐    |
| 脚本注入       | DoPostBefore、ReloadAfter             | ☐    |
| 系统字段       | SUP_ID, ADM_ID, CDATE, MDATE          | ☐    |
| PageInfo       | msg0, msg1, msg2                      | ☐    |
| 按钮配置       | butOk, butClose, butNew, butModify... | ☐    |
