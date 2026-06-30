# EasyWebTemplate 完整参考文档

> EWA (Easy Web Application) 框架的核心配置项定义格式。  
> 每个 `EasyWebTemplate` 描述一个完整的页面（含结构、数据、交互、样式）。

---

## 目录

1. [文件结构](#1-文件结构)
2. [根节点属性](#2-根节点属性)
3. [Page — 页面配置](#3-page--页面配置)
4. [XItem — 页面元素定义](#4-xitem--页面元素定义)
5. [Action — 动作链配置](#5-action--动作链配置)
6. [Menus — 菜单配置](#6-menus--菜单配置)
7. [PageInfos — 页面信息](#7-pageinfos--页面信息)
8. [Charts — 图表配置](#8-charts--图表配置)
9. [Workflows — 工作流配置](#9-workflows--工作流配置)
10. [FrameTag 取值](#10-frametag-取值)
11. [XItem Tag 类型](#11-xitem-tag-类型)
12. [命名约定](#12-命名约定)
13. [校验规则](#13-校验规则)

---

## 1. 文件结构

```
<EasyWebTemplates>                        ← 文件根节点
    ├── <EasyWebTemplate Name="...">      ← 第1个配置项
    │   ├── <Page>                        ← 页面定义
    │   ├── <XItems>                      ← 字段元素
    │   ├── <Action>                      ← 动作链
    │   ├── <Menus />                     ← 菜单
    │   ├── <Charts />                    ← 图表
    │   ├── <PageInfos />                 ← 提示信息
    │   └── <Workflows />                 ← 工作流
    ├── <EasyWebTemplate Name="...">      ← 第2个配置项
    │   └── ...
    └── ...
</EasyWebTemplates>
```

文件位置：`define.xml/ewa/*.xml`，或用户自定义的业务配置路径。

---

## 2. 根节点属性

| 属性 | 必需 | 说明 |
|:-----|:----|:-----|
| `Name` | **是** | 配置项唯一标识，如 `users.F.NM`（命名规则见 [§12](#12-命名约定)） |
| `CreateDate` | 否 | 创建时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `UpdateDate` | 否 | 最后修改时间 |
| `Author` | 否 | 作者标识 |

```xml
<EasyWebTemplate Name="users.F.NM" CreateDate="2024-01-15 10:00:00"
    UpdateDate="2024-06-30 14:00:00" Author="admin">
```

---

## 3. Page — 页面配置

### 3.1 基础参数

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Page/FrameTag/Set` | **页面类型（必填）** | `@FrameTag`（见 [§10](#10-frametag-取值)） |
| `Page/Name/Set` | 页面名称（提交时自动与根 Name 同步） | `@Name` |
| `Page/SkinName/Set` | 皮肤/主题 | `@SkinName`（如 `Test1`, `Blue`）, `@IsXhtml`（0\|1） |
| `Page/DataSource/Set` | 数据库连接名 | `@DataSource`（如 `ewa`, `ow_main`） |
| `Page/DescriptionSet/Set` | 页面描述（多语言，可多个） | `@Info`, `@Lang`（`zhcn`\|`enus`）, `@Memo` |
| `Page/Size/Set` | 页面尺寸 | `@Width`, `@Height`, `@HAlign`（left\|center\|right）, `@VAlign`（top\|middle\|bottom）, `@HiddenCaption`（0\|1）, `@FrameCols`（C1\|C2\|C3, 框架列数） |
| `Page/Cached/Set` | 缓存配置 | `@CachedType`（`none`\|`all`）, `@CachedSeconds` |
| `Page/Acl/Set` | 权限控制类 | `@Acl`（Java 类全名） |
| `Page/Log/Set` | 日志 | `@Log` |
| `Page/ConfigMemo/Set` | 配置备注（嵌套自闭合） | `<ConfigMemo />` |
| `Page/AllowJsonExport` | JSON 导出许可 | 存在即允许 |
| `Page/PageAttributeSet/Set` | 页面级 HTML 属性（可多个） | `@PageAttName`, `@PageAttValue` |
| `Page/RowAttributeSet/Set` | 行级 HTML 属性（可多个） | `@RowAttName`, `@RowAttValue` |
| `Page/GroupSet/Set` | 分组定义 | `@GroupName`, `@GroupIndex`, `@GroupCols` |

### 3.2 扩展内容

| 路径 | 说明 | 格式 |
|:-----|:-----|:-----|
| `Page/AddHtml/Set/Top` | 页面顶部 HTML（**仅 Top/Bottom 子元素**） | CDATA |
| `Page/AddHtml/Set/Bottom` | 页面底部 HTML | CDATA |
| `Page/AddScript/Set/Top` | 页面顶部 JavaScript（**仅 Top/Bottom 子元素**） | CDATA |
| `Page/AddScript/Set/Bottom` | 页面底部 JavaScript | CDATA |
| `Page/AddCss/Set` | 附加 CSS | CDATA 或 `<AddCss />` |

> **校验规则**：`AddHtml/Set` 和 `AddScript/Set` 下只允许 `<Top>` 和 `<Bottom>` 子元素，其他元素（如 `<AddPreRow>`）会被 `ConfigValidator` 拦截。

### 3.3 ListFrame 专用

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Page/ListUI/Set` | 列表交互 | `@luButtons`（0\|1）, `@luSelect`（S=single\|M=multi）, `@luSearch`（0\|1）, `@luDblClick`（0\|1）, `@luDblClickIdx`, `@luStickyHeaders` |
| `Page/PageSize/Set` | 分页 | `@PageSize`, `@KeyField`（主键字段）, `@IsSplitPage`（0\|1）, `@StatusField`, `@Recycle`, `@AllowExport`, `@ColSize` |
| `Page/BoxJson` | 顶部搜索区 JSON | 存在即生效 |
| `Page/LeftJson` | 左侧筛选 JSON | 存在即生效 |
| `Page/ChartsShow/Set` | 图表展示 | `@ChartsShow` |
| `Page/RedrawJson` | 表单重绘 JSON | 存在即生效 |

### 3.4 Tree 专用

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Page/Tree/Set` | 树形配置 | `@Key`（节点键）, `@ParentKey`（父节点键）, `@Text`（显示文本）, `@Title`, `@Level`, `@Order`, `@RootId`, `@LoadByLevel`（1）, `@MenuGroup`, `@AddPara1-3` |
| `Page/TreeIconSet/Set` | 树节点图标 | `@Test`（条件）, `@Open`, `@Close`, `@Filter` |

### 3.5 Menu 专用

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Page/Menu/Set` | 菜单配置 | `@Key`, `@ParentKey`, `@Text`, `@Icon`, `@IconType`（Image\|Font），`@Cmd`, `@RootId` |
| `Page/MenuShow/Set` | 菜单展示 | `@MenuShow`（`EWA_MENU`\|`menus`） |

### 3.6 其他框架

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Page/HtmlFrame/Set` | 框架分割布局 | `@FrameType`（H=水平\|V=垂直），`@FrameBorder`（0\|1），`@FrameSize`（如 `50%,*`），`@FrameSubUrl` |
| `Page/FrameHtml/Set` | 框架 HTML | CDATA |
| `Page/MGAxisX/Set` | MultiGrid X轴 | 配合 FrameTag=MultiGrid |
| `Page/MGAxisY/Set` | MultiGrid Y轴 | 配合 FrameTag=MultiGrid |
| `Page/MGCell/Set` | MultiGrid 单元格 | 配合 FrameTag=MultiGrid |
| `Page/LogicShow/Set` | 逻辑条件展示 | `@Name`, `@HiddenFields`, `@ParaExp`（如 `'@linktype'='group'`） |

---

## 4. XItem — 页面元素定义

### 4.1 主参数

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `XItem/Tag/Set` | **元素类型（必填）** | `@Tag`（见 [§11](#11-xitem-tag-类型)）, `@IsLFEdit`（0=只读\|1=可编辑\|2=行内编辑）, `@SpanShowAs` |
| `XItem/Name/Set` | **字段名（必填）** | `@Name` |
| `XItem/GroupIndex/Set` | 分组索引 | `@GroupIndex`（整数控制排列顺序） |
| `XItem/InitValue/Set` | 初始值 | `@InitValue`（含特殊宏：`SEQID`, `GUID`, `Tody`, `Tody-Time`） |
| `XItem/DescriptionSet/Set` | 字段描述（多语言，可多个） | `@Info`, `@Lang`（`zhcn`\|`enus`）, `@Memo` |

### 4.2 界面参数

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `XItem/XStyle/Set` | 标准样式 | `@XStyleAlign`, `@XStyleVAlign`, `@XStyleNoWrap`（yes\|no），`@XStyleFixed`（yes\|no），`@XStyleColor`, `@XStyleBold`, `@XStyleCursor`, `@XStyleWidth` |
| `XItem/Style/Set` | 自定义 CSS | `@Style`（直接写入 style 属性） |
| `XItem/ParentStyle/Set` | 父容器样式 | `@ParentStyle`（如 `width:400px`） |
| `XItem/IsHtml/Set` | HTML 渲染 | `@IsHtml`（0\|1） |

### 4.3 数据参数

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `XItem/DataItem/Set` | 数据绑定 | `@DataField`（绑定字段）, `@DataType`（String\|Int\|BigInt\|Boolean\|Date\|Time\|DateTime\|Binary\|...）, `@Format`（Date\|ShortDate\|Time\|DateTime\|...）, `@IsEncrypt`, `@Valid`（Email\|Number\|...）, `@DisableOnModify`, `@SumBottom`, `@NumberScale`, `@Translation`, `@TransTarget`, `@TriggerValid`, `@Icon`, `@IconLoction`（left\|right）, `@FrameOneCell`, `@Trim` |
| `XItem/DataRef/Set` | 数据引用 | `@RefKey`, `@RefShow`, `@RefMulti`（0\|1）, `@RefShowStyle`, `@RefShowType`, `@RefSql` |
| `XItem/OrderSearch/Set` | 排序/搜索 | `@IsOrder`（0\|1）, `@SearchType`（text\|date\|fix\|...）, `@IsGroup`, `@IsGroupDefault`, `@IsSearchQuick`, `@SearchSql`, `@SearchMulti`（1\|2）, `@SearchExp`, `@OrderExp`, `@GroupTestLength` |
| `XItem/List/Set` | 静态/动态列表 | `@Sql`, `@DisplayField`, `@ValueField`, `@DisplayList`, `@ValueList`, `@TitleField`, `@TitleList`, `@ListShowType`, `@ListAddBlank`（1）, `@ListFilterField`, `@ListFilterType` |
| `XItem/DispEnc/Set` | 加密显示 | `@EncShowUrl`, `@EncType` |

### 4.4 验证参数

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `XItem/IsMustInput/Set` | 必填 | `@IsMustInput`（0\|1） |
| `XItem/MaxMinLength/Set` | 长度限制 | `@MaxLength`, `@MinLength` |
| `XItem/MaxMinValue/Set` | 值范围 | `@MaxValue`, `@MinValue` |
| `XItem/Switch/Set` | 开关 | `@SwtAction`, `@SwtOnText`, `@SwtOffText` |
| `XItem/VaildEx/Set` | 高级验证 | `@VXMode`（js\|action）, `@VXJs`, `@VXAction`, `@VXOk`, `@VXFail` |

### 4.5 交互/扩展参数

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `XItem/AttributeSet/Set` | 自定义 HTML 属性（可多个） | `@AttName`, `@AttValue`, `@AttLogic` |
| `XItem/EventSet/Set` | 事件绑定（可多个） | `@EventName`（`onclick`\|`onchange`\|`onblur`\|`ewa_click`\|...）, `@EventType`（Javascript）, `@EventValue`（JS 代码）, `@EventLogic` |
| `XItem/CallAction/Set` | 按钮动作 | `@Action`（动作名）, `@AfterTip`, `@ConfirmInfo` |
| `XItem/OpenFrame/Set` | 弹窗按钮 | `@CallXmlName`, `@CallItemName`, `@CallMethod`（OpenReloadClose 等）, `@CallParas`（如 `id=@id`）, `@AttatchParas` |
| `XItem/Frame/Set` | 内嵌框架 | `@CallXmlName`, `@CallItemName`, `@CallPara` |
| `XItem/UserControl/Set` | 自定义控件 | `@UserControl` |
| `XItem/DefineFrame/Set` | 定义框架 | `@CallXmlName`, `@CallItemName`, `@CallPara` |
| `XItem/PopFrame/Set` | 弹出窗口 | `@PopXmlName`, `@PopItemName`, `@PopPara`, `@PopTitleField` |
| `XItem/UserSet/Set` | 用户自定义 | `@Lang`, 以及其他自定义属性 |
| `XItem/Upload/Set` | 文件上传（h5upload） | `@UpType`（0\|1\|2）, `@UpMulti`（yes\|no）, `@UpExts`, `@UpLimit`（M）, `@UpPath`, `@UpSaveMethod`（FileSystem\|DB）, `@UpSQL`, `@UpNewSizes`（如 `100x100`, `200x200`）, `@UpUnZip`（yes\|no）, `@UpDelete`（yes\|no）, `@UpJsonEncyrpt`（yes\|no） |
| `XItem/MGAddField/Set` | MultiGrid 汇总 | `@MgfCalc`, `@MgfComput`, `@MgfId` |
| `XItem/AnchorParas/Set` | 链接参数 | `@aHref`, `@aTarget` |
| `XItem/LinkButtonParas/Set` | 链接按钮 | `@lkbButtonId` |
| `XItem/DopListShow/Set` | 动态列表显示 | `@DopListShow` |
| `XItem/ReportCfg/Set` | 报表配置 | `@ReportCfg` |
| `XItem/CombineFrame/Set` | 组合框架 | `@CombineFrame` |
| `XItem/AddrMapRels/Set` | 地址-地图关联 | `@AmrCountry`, `@AmrProvince`, `@AmrCity`, `@AmrZip`, `@AmrLat`, `@AmrLng` |
| `XItem/QRCode/Set` | 二维码 | 存在即生效 |
| `XItem/ImageDefault/Set` | 默认图片 | `@ImageDefault` |
| `XItem/signature/Set` | 手写签名 | `@SignColor`, `@SignBgColor`, `@SignFormat`（png\|svg）, `@SignLineWidth`, `@SignPath` |

---

## 5. Action — 动作链配置

### 5.1 结构概览

```
<Action>
    <ActionSet>              ← 动作集合定义
        <Set Type="...">     ← 单个动作
            <CallSet>        ← 调用链
                <Set CallName="..." CallType="..." Test="..." />
            </CallSet>
        </Set>
    </ActionSet>
    <SqlSet>                 ← SQL 动作实现
    <XmlSet>                 ← XML 数据动作
    <XmlSetData>             ← XML 数据源绑定
    <ClassSet>               ← Java 类动作
    <ScriptSet>              ← 脚本动作
    <UrlSet>                 ← URL 跳转动作
    <CSSet>                  ← 跨动作参数
    <JSONSet />              ← JSON 动作
    <CallSet>                ← 全局调用链
</Action>
```

### 5.2 ActionSet

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Action/ActionSet/Set` | 动作定义 | `@Type`（动作类型）, `@IsPostMsg`（yes\|no）, `@LogMsg`, `@Transaction` |
| `Action/ActionSet/Set/CallSet/Set` | 动作调用链（可多个） | `@CallName`（引用目标）, `@CallType`（见下）, `@Test`（条件表达式）, `@CallIsChk` |

**CallType 取值**：

| 值 | 说明 | 引用目标 |
|:---|:-----|:---------|
| `SqlSet` | SQL 执行 | `Action/SqlSet/Set[@Name]` |
| `UrlSet` | URL 跳转 | `Action/UrlSet/Set[@Name]` |
| `ScriptSet` | JavaScript 执行 | `Action/ScriptSet/Set[@Name]` |
| `XmlSet` | XML 数据操作 | `Action/XmlSet/Set[@Name]` |
| `ClassSet` | Java 类调用 | `Action/ClassSet/Set[@Name]` |

**常见 Type 值**：`OnPageLoad`, `OnPagePost`, `OnFrameDelete`, `OnFrameRestore`, `OnTreeNodeNew`, `OnTreeNodeDelete`, `OnTreeNodeRename`, `OnListFrameUpdateCell`, `CheckError`, `ExtendAction0-2`

### 5.3 SqlSet

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Action/SqlSet/Set` | SQL 动作 | `@Name`（被 CallName 引用）, `@SqlType`（`query`\|`update`）, `@TransType`（yes\|no\|空） |
| `Action/SqlSet/Set/Sql` | SQL 语句 | CDATA，支持 `@参数名` 参数绑定 |
| `Action/SqlSet/Set/CSSet/Set` | SQL 参数定义 | `@ParaName`, `@Name`, `@CSType`（all）, `@Option`（C） |

### 5.4 XmlSet / XmlSetData

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Action/XmlSet/Set` | XML 动作 | `@Name`, `@XmlAction`（load\|update\|insert\|deletes\|inertOrUpdate）, `@XmlSetData`（绑定 XmlSetData Name）, `@XmlFields`, `@XmlWhere` |
| `Action/XmlSetData/Set` | XML 数据源 | `@Name`, `@XmlName`（XML 文件路径）, `@XmlFields`, `@XmlTagPath`, `@XmlLoadType`（attribute\|childnode） |

### 5.5 ClassSet

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Action/ClassSet/Set` | Java 类动作 | `@Name`, `@ClassName`（完整类名）, `@MethodName`, `@ConData`（构造参数）, `@MethodData`（方法参数，`@param` 格式）, `@XmlTag` |

### 5.6 ScriptSet

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Action/ScriptSet/Set` | 脚本动作 | `@Name`, `@ScriptType`（javascript） |
| `Action/ScriptSet/Set/Script` | 脚本内容 | CDATA |

### 5.7 UrlSet

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Action/UrlSet/Set` | URL 跳转 | `@Name`, `@Url` |
| `Action/UrlSet/Set/CSSet/Set` | URL 参数 | `@ParaName`, `@Name`, `@CSType`, `@Domain`, `@Life` |

---

## 6. Menus — 菜单配置

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Menus/Menu` | 菜单容器 | `@Name`（菜单项唯一标识） |
| `Menus/Menu/Name/Set` | 菜单名称 | `@Name` |
| `Menus/Menu/DescriptionSet/Set` | 描述（多语言） | `@Info`, `@Lang` |
| `Menus/Menu/Cmd/Set` | 点击命令 | `@Cmd`（JS 函数名） |
| `Menus/Menu/Icon/Set` | 图标 | `@Icon`（路径） |
| `Menus/Menu/Group/Set` | 分组 | `@Group`（0\|1\|2...） |

---

## 7. PageInfos — 页面信息

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `PageInfos/PageInfo` | 信息项 | `@Name`（如 `DEL_ITEM`, `CommonTitle`, `msg0-2`） |
| `PageInfos/PageInfo/Name/Set` | 信息标识 | `@Name` |
| `PageInfos/PageInfo/DescriptionSet/Set` | 信息文本 | `@Info`, `@Lang` |

---

## 8. Charts — 图表配置

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Charts/Chart` | 图表项 | `@Name` |
| `Charts/Chart/Name/Set` | 图表名称 | `@Name` |
| `Charts/Chart/DescriptionSet/Set` | 描述 | `@Info`, `@Lang` |
| `Charts/Chart/ChartType/Set` | 图表类型 | `@ChartType`（LINE\|PIE\|PIE3D\|COLUMN\|COLUMN3D\|BAR\|BAR3D\|AREA\|SCATTER\|GAUGE\|FUNNEL\|RADAR） |
| `Charts/Chart/ChartSize/Set` | 图表尺寸 | `@ChartSize`（如 `800x400`） |
| `Charts/Chart/DataMap/Set` | 数据映射 | `@Name`, `@Value`, `@Title`, `@DataType` |

---

## 9. Workflows — 工作流配置

| 路径 | 说明 | 属性 |
|:-----|:-----|:-----|
| `Workflows/Workflow` | 工作流项 | `@Name` |
| `Workflows/Workflow/Name/Set` | 名称 | `@Name` |
| `Workflows/Workflow/DescriptionSet/Set` | 描述 | `@Info`, `@Lang` |
| `Workflows/Workflow/WfType/Set` | 流程类型 | `@WfType` |
| `Workflows/Workflow/WfLogic/Set` | 流程逻辑 | `@WfLogic` |
| `Workflows/Workflow/WfAction/Set` | 流程动作 | `@WfAction` |

---

## 10. FrameTag 取值

| 值 | 说明 | Java 类 |
|:---|:-----|:--------|
| `Frame` | 标准表单 | `FrameFrame` |
| `ListFrame` | 列表页 | `FrameList` |
| `Grid` | 网格编辑 | `FrameGrid` |
| `Tree` | 树形结构 | `FrameTree` |
| `Menu` | 菜单 | `FrameMenu` |
| `MultiGrid` | 多维网格 | `FrameMultiGrid` |
| `Logic` | 逻辑页面 | `FrameLogic` |
| `Report` | 报表 | `FrameReport` |
| `Combine` | 组合页面 | `FrameCombine` |
| `Complex` | 复合页面 | `FrameComplex` |

---

## 11. XItem Tag 类型

### 11.1 标准组件

| Tag | HTML | 说明 |
|:----|:-----|:-----|
| `text` | `<input type="text">` | 单行文本 |
| `textarea` | `<textarea>` | 多行文本 |
| `span` | `<span>` | 只读文本标签 |
| `hidden` | `<input type="hidden">` | 隐藏字段 |
| `password` | `<input type="password">` | 密码输入 |
| `passwordWithEye` | `<input type="password">` + 眼睛图标 | 可查看密码 |
| `combo` | `<div class="ewa-combo-box">` | 可搜索下拉组合框 |
| `select` | `<select>` | 下拉选择 |
| `checkbox` | `<input type="checkbox">` | 多选 |
| `switch` | 自定义开关 | 开关切换 |
| `radio` | `<input type="radio">` | 单选 |
| `checkboxgrid` | 表格多选 | 网格复选框 |
| `radiogrid` | 表格单选 | 网格单选框 |
| `anchor` | `<a>` | 超链接 |
| `anchor2` | `<a>` | 超链接（新标签页） |
| `linkButton` | `<button>` | 链接按钮 |
| `droplist` | 下拉列表 | 自定义下拉 |
| `submit` | `<input type="submit">` | 提交按钮 |
| `button` | `<button>` | 普通按钮 |
| `butFlow` | 按钮组 | 流程操作按钮 |
| `QRCode` | Canvas | 二维码 |

### 11.2 扩展组件

| Tag | 说明 |
|:----|:-----|
| `date` | 日期选择器 |
| `datetime` | 日期时间选择器 |
| `time` | 时间选择器 |
| `dHtml5` | HTML5 富文本编辑器 |
| `markDown` | Markdown 编辑器 |
| `h5upload` | HTML5 文件上传 |
| `h5TakePhoto` | HTML5 拍照/录像 |
| `valid` | 验证码 |
| `smsValid` | 短信验证码 |
| `signature` | 手写签名 |
| `user` | 用户选择器 |
| `userControl` | 自定义控件 |
| `dataType` | 数据类型选择 |
| `addressMap` | 地址地图 |
| `gridImage` | 网格图片 |
| `gridBgImage` | 网格背景图片 |
| `popselect` | 弹出选择 |
| `ewaconfigitem` | EWA 配置项选择器 |
| `MGAddField` | MultiGrid 添加字段 |
| `LogicItem` | Logic 框架子项 |
| `ReportItem` | Report 框架子项 |
| `CombineItem` | Combine 框架子项 |
| `ComplexItem` | Complex 框架子项 |
| `SqlEditor` | SQL 代码编辑器 |
| `JsEditor` | JavaScript 代码编辑器 |
| `CssEditor` | CSS 代码编辑器 |
| `XMLEditor` | XML 代码编辑器 |
| `idempotence` | 幂等令牌 |

### 11.3 已废弃

| Tag | 说明 |
|:----|:-----|
| `file` | 旧文件上传（用 h5upload 替代） |
| `dHtml` | 旧富文本编辑器 |
| `dHtmlNoImages` | 旧富文本（禁用图片） |
| `image` | 旧图片控件 |
| `swffile` | SWF 文件 |
| `SwfDoc` | SWF 文档 |
| `SwfTakePhoto` | SWF 拍照 |

---

## 12. 命名约定

配置项 `Name` 遵循 `{对象}.{框架类型}.{模式}` 格式：

| 模式 | 缩写 | 说明 |
|:-----|:-----|:-----|
| New | N | 新建 |
| Modify | M | 修改 |
| View | V | 查看 |
| New/Modify | NM | 新建或修改 |

| 框架 | 缩写 |
|:-----|:-----|
| Frame | F |
| ListFrame | LF |
| Tree | T |
| Menu | M |
| Grid | G |
| MultiGrid | MG |

**示例**：
- `users.F.NM` — 用户表单（新建/修改）
- `orders.LF.M` — 订单列表（修改模式）
- `products.T.V` — 产品树（查看模式）
- `adm_menu.listframe.view` — 菜单列表（历史命名风格）

---

## 13. 校验规则

`ConfigValidator.validateItemXml()` 执行以下校验：

| # | 校验项 | 规则 |
|:--|:-------|:-----|
| 1 | XML 格式 | 可解析，无语法错误 |
| 2 | 根元素 | 必须为 `EasyWebTemplate` |
| 3 | Name 属性 | 不能为空，须与请求的 itemname 匹配 |
| 4 | Page 节点 | 必须存在 |
| 5 | FrameTag | 必须存在且值在 [§10](#10-frametag-取值) 合法列表中 |
| 6 | XItem Tag | 每个 XItem 的 Tag 必须在 [§11](#11-xitem-tag-类型) 合法列表中 |
| 7 | AddHtml 子元素 | `AddHtml/Set` 下仅允许 `<Top>` 和 `<Bottom>` |
| 8 | AddScript 子元素 | `AddScript/Set` 下仅允许 `<Top>` 和 `<Bottom>` |
| 9 | Page/Name 一致性 | 与根 Name 不一致时自动修正（非阻断） |
