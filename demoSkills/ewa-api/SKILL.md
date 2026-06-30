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
| `validateSql` | `<db> <sql>` | SQL 语法校验（事务内执行并回滚，无副作用），db 来自配置文件 `EasyWebTemplate/Page/DataSource` |

**frametype**: `ListFrame` \| `Frame` \| `Tree`  
**operationtype**: `N`（新增）\| `M`（修改）\| `V`（查看）\| `NM`（新增+修改）

## DOM 节点路径参考

EWA 配置 XML 的 DOM 结构及 XPath 路径，便于 AI 定位和修改节点。

### 容器结构

| 路径 | 说明 | 关键属性 |
|------|------|----------|
| `/EasyWebTemplates` | 文件根节点 | — |
| `/EasyWebTemplates/EasyWebTemplate` | 单个配置项 | `@Name`（唯一标识）, `@CreateDate`, `@UpdateDate`, `@Author` |

### Page 页面配置

| 路径 | 说明 | 关键属性 / 子节点 |
|------|------|-------------------|
| `EasyWebTemplate/Page` | 页面配置容器 | — |
| `Page/FrameTag/Set` | 页面框架类型 | `@FrameTag`：`ListFrame`\|`Frame`\|`Tree`\|`Menu`\|`Grid`\|`MultiGrid`\|`Logic`\|`Report`\|`Combine`\|`Complex` |
| `Page/Name/Set` | 页面名称 | `@Name`（提交时 fixXml 自动与根 Name 同步） |
| `Page/SkinName/Set` | 皮肤 | `@SkinName`（如 `Test1`, `Blue`）, `@IsXhtml` |
| `Page/DataSource/Set` | 数据库连接 | `@DataSource`（如 `ewa`, `globalTravel`） |
| `Page/DescriptionSet/Set` | 页面描述 | `@Info`, `@Lang`（`zhcn`\|`enus`）, `@Memo` |
| `Page/Size/Set` | 页面尺寸 | `@Width`, `@Height`, `@HAlign`, `@VAlign`, `@HiddenCaption`（0\|1）, `@FrameCols` |
| `Page/Cached/Set` | 缓存配置 | `@CachedType`（`none`\|`all`）, `@CachedSeconds` |
| `Page/Acl/Set` | 权限控制 | `@Acl`（Java 类全名） |
| `Page/Log/Set` | 日志配置 | `@Log` |
| `Page/ConfigMemo/Set` | 配置备注 | `@ConfigMemo` |
| `Page/AllowJsonExport/Set` | 允许 JSON 导出 | `@AllowJsonExport` |

### Page 扩展配置

| 路径 | 说明 | 关键属性 |
|------|------|----------|
| `Page/AddHtml/Set/Top` | 页面顶部 HTML | CDATA |
| `Page/AddHtml/Set/Bottom` | 页面底部 HTML | CDATA |
| `Page/AddScript/Set/Top` | 顶部脚本 | CDATA（JavaScript） |
| `Page/AddScript/Set/Bottom` | 底部脚本 | CDATA |
| `Page/AddCss/Set` | 附加 CSS | CDATA |
| `Page/PageAttributeSet/Set` | 页面级 HTML 属性 | `@PageAttName`, `@PageAttValue`（键值对，可多个） |
| `Page/RowAttributeSet/Set` | 行级 HTML 属性 | `@RowAttName`, `@RowAttValue`（可多个） |
| `Page/GroupSet/Set` | 分组定义 | `@GroupName`, `@GroupIndex`, `@GroupCols` |

### Page/ListFrame 专用

| 路径 | 说明 | 属性 |
|------|------|------|
| `Page/ListUI/Set` | 列表交互 | `@luButtons`（0\|1）, `@luSelect`（S\|M）, `@luSearch`（0\|1）, `@luDblClick` |
| `Page/PageSize/Set` | 分页 | `@PageSize`, `@KeyField`, `@IsSplitPage`（0\|1）, `@StatusField`, `@Recycle` |
| `Page/BoxJson/Set` | 顶部搜索区 | `@Json`（JSON 定义） |
| `Page/LeftJson/Set` | 左侧筛选 | `@Json`（JSON 定义） |
| `Page/ChartsShow/Set` | 图表展示 | `@ChartType`, `@ChartSize`（宽x高） |
| `Page/RedrawJson/Set` | 表单重绘 | `@Json`（JSON 定义） |

### Page/Tree 专用

| 路径 | 说明 | 属性 |
|------|------|------|
| `Page/Tree/Set` | 树形配置 | `@Key`, `@ParentKey`, `@Text`, `@Title`, `@Level`, `@Order`, `@RootId`, `@LoadByLevel`（1）, `@MenuGroup`, `@AddPara1` |
| `Page/TreeIconSet/Set` | 树节点图标 | `@Test`（条件）, `@Open`, `@Close`, `@Filter` |

### Page/Menu 专用

| 路径 | 说明 | 属性 |
|------|------|------|
| `Page/Menu/Set` | 菜单配置 | `@Key`, `@ParentKey`, `@Text`, `@Icon`, `@IconType`, `@Cmd`, `@RootId` |
| `Page/MenuShow/Set` | 菜单展示 | `@MenuShow`（`EWA_MENU`\|`menus`） |

### Page/Frame 专用

| 路径 | 说明 | 属性 |
|------|------|------|
| `Page/HtmlFrame/Set` | 框架布局 | `@FrameType`（H\|V）, `@FrameBorder`（0\|1）, `@FrameSize`（如 `50%,*`） |
| `Page/FrameHtml/Set` | 框架 HTML | CDATA |

### Page/Logic & MultiGrid

| 路径 | 说明 | 属性 |
|------|------|------|
| `Page/LogicShow/Set` | 逻辑条件展示 | `@Name`, `@ParaExp`（如 `'@linktype'='group'`） |
| `Page/XItems/XItem/MGAddField/Set` | 多维表格汇总 | `@MGAddField` |
| `Page/XItems/XItem/CombineFrame/Set` | 组合框架引用 | `@CombineFrame` |

### XItems / XItem 通用参数

| 路径 | 说明 | 关键属性 |
|------|------|----------|
| `Page/XItems` | XItem 容器 | — |
| `Page/XItems/XItem` | 单个页面元素 | — |
| `XItem/Tag/Set` | **元素类型（必填）** | `@Tag`（见上表 Tag 值）, `@IsLFEdit`（0\|1\|2：列表可编辑）, `@SpanShowAs` |
| `XItem/Name/Set` | **字段名称（必填）** | `@Name` |
| `XItem/DescriptionSet/Set` | 字段描述 | `@Info`, `@Lang`（`zhcn`\|`enus`）, `@Memo` |
| `XItem/GroupIndex/Set` | 分组索引 | `@GroupIndex`（整数，控制排列顺序） |
| `XItem/InitValue/Set` | 初始值 | `@InitValue`（如 `SEQID` 自增） |

### XItem 数据参数 (Data)

| 路径 | 说明 | 属性 |
|------|------|------|
| `XItem/DataItem/Set` | 数据绑定 | `@DataField`（字段名）, `@DataType`（String\|Int\|Date\|Binary\|...）, `@Format`（date\|DateTime\|DateShortTime\|...）, `@IsEncrypt`, `@Valid`（Email\|...） |
| `XItem/DataRef/Set` | 数据引用 | `@DataRef` |
| `XItem/OrderSearch/Set` | 排序搜索 | `@IsOrder`（0\|1）, `@SearchType`（text\|date\|...）, `@IsGroup`, `@IsGroupDefault` |
| `XItem/List/Set` | 静态列表/SQL 列表 | `@Sql`（SQL）, `@DisplayField`, `@ValueField`, `@DisplayList`, `@ValueList`, `@ListShowType`（Key-Text）, `@ListFilterType`（text）, `@ListAddBlank`（1） |
| `XItem/DispEnc/Set` | 加密显示 | `@DispEnc`（脱敏规则） |

### XItem 界面参数 (UI)

| 路径 | 说明 | 属性 |
|------|------|------|
| `XItem/XStyle/Set` | 标准样式 | `@XStyleAlign`, `@XStyleVAlign`, `@XStyleNoWrap`, `@XStyleFixed`, `@XStyleColor`, `@XStyleBold`, `@XStyleCursor`, `@XStyleWidth` |
| `XItem/Style/Set` | CSS 样式 | `@Style`（直接写入 style 属性） |
| `XItem/ParentStyle/Set` | 父容器样式 | `@ParentStyle`（如 `width:400px`） |
| `XItem/IsHtml/Set` | HTML 渲染 | `@IsHtml`（0\|1） |

### XItem 验证参数 (Valid)

| 路径 | 说明 | 属性 |
|------|------|------|
| `XItem/IsMustInput/Set` | 必填项 | `@IsMustInput`（0\|1） |
| `XItem/MaxMinLength/Set` | 长度限制 | `@MaxLength`, `@MinLength` |
| `XItem/MaxMinValue/Set` | 值范围 | `@MaxValue`, `@MinValue` |
| `XItem/VaildEx/Set` | 高级验证 | `@VXMode`（js\|action）, `@VXJs`, `@VXAction`, `@VXOk`, `@VXFail` |

### XItem 扩展参数 (Others)

| 路径 | 说明 | 属性 |
|------|------|------|
| `XItem/AttributeSet/Set` | 自定义属性 | `@AttName`（属性名）, `@AttValue`（属性值，可多个） |
| `XItem/EventSet/Set` | 事件绑定 | `@EventName`（`onclick`\|`onchange`\|`onblur`\|...）, `@EventType`（Javascript）, `@EventValue`（JS 代码） |
| `XItem/Upload/Set` | 上传配置 | `@UpMulti`（yes\|no）, `@UpExts`, `@UpLimit`, `@UpPath`, `@UpType`, `@UpNewSizes`, `@UpSaveMethod`, `@UpSQL`, `@UpDelete`, `@UpUnZip`, `@UpJsonEncyrpt` |
| `XItem/Switch/Set` | 开关配置 | `@SwtAction`, `@SwtOnText`, `@SwtOffText` |
| `XItem/CallAction/Set` | 按钮动作 | `@Action`（动作名）, `@ConfirmInfo`（确认提示） |
| `XItem/OpenFrame/Set` | 按钮弹窗 | `@CallXmlName`, `@CallItemName`, `@CallMethod`（OpenReloadClose）, `@CallParas` |
| `XItem/AnchorParas/Set` | 链接参数 | `@aHref`, `@aTarget` |
| `XItem/LinkButtonParas/Set` | 链接按钮 | `@lkbButtonId` |
| `XItem/DopListShow/Set` | 动态列表 | `@DopListShow` |
| `XItem/Frame/Set` | 框架引用 | `@Frame` |
| `XItem/PopFrame/Set` | 弹窗框架 | `@PopXmlName`, `@PopItemName`, `@PopPara` |
| `XItem/UserSet/Set` | 自定义渲染 | `@Lang`, 其他自定义参数 |
| `XItem/UserControl/Set` | 控件 | `@UserControl` |
| `XItem/ImageDefault/Set` | 默认图片 | `@ImageDefault` |
| `XItem/AddrMapRels/Set` | 地图关联 | `@AmrCountry`, `@AmrProvince`, `@AmrCity`, `@AmrZip`, `@AmrLat`, `@AmrLng` |
| `XItem/signature/Set` | 签名配置 | `@SignColor`, `@SignBgColor`, `@SignFormat`, `@SignLineWidth`, `@SignPath` |
| `XItem/ReportCfg/Set` | 报表配置 | `@ReportCfg` |

### Action 动作配置（Page 同级）

| 路径 | 说明 | 属性 |
|------|------|------|
| `EasyWebTemplate/Action` | 动作容器 | — |
| `Action/ActionSet/Set` | 动作定义 | `@Type`（`OnPagePost`\|`loadAll`\|`CheckError`）, `@IsPostMsg`（yes\|no） |
| `Action/ActionSet/Set/CallSet/Set` | 动作调用链 | `@CallName`（如 `OnFrameRestore SQL`）, `@CallType`（`SqlSet`\|`UrlSet`\|`ScriptSet`\|`XmlSet`\|`ClassSet`）, `@Test`（条件表达式） |
| `Action/SqlSet/Set` | SQL 动作 | `@Name`, `@SqlType`（`query`\|`update`）, `@TransType`（yes\|no） |
| `Action/SqlSet/Set/CSSet/Set` | SQL 参数 | `@ParaName`, `@Name`, `@CSType`（all）, `@Option`（C） |
| `Action/UrlSet/Set` | URL 跳转 | `@Name`, `@Url` |
| `Action/UrlSet/Set/CSSet/Set` | URL 参数 | `@ParaName`, `@Name`, `@CSType`（all）, `@Domain`, `@Life` |
| `Action/ScriptSet/Set` | 脚本动作 | `@Name`, `@ScriptType`（javascript） |
| `Action/XmlSet/Set` | XML 数据动作 | `@Name`, `@XmlAction`（load\|deletes\|inertOrUpdate）, `@XmlSetData`, `@XmlWhere` |
| `Action/XmlSetData/Set` | XML 数据源 | `@Name`, `@XmlName`（路径）, `@XmlFields`, `@XmlTagPath`, `@XmlLoadType`（attribute） |
| `Action/ClassSet/Set` | Java 类动作 | `@Name`, `@ClassName`, `@MethodName`, `@ConData`, `@MethodData`, `@XmlTag` |
| `Action/CSSet/Set` | 跨动作参数 | `@ParaName`, `@Name`, `@CSType`, `@Option` |

### Menus 菜单（Page 同级）

| 路径 | 说明 | 属性 |
|------|------|------|
| `EasyWebTemplate/Menus/Menu` | 菜单容器 | — |
| `Menus/Menu/Name/Set` | 菜单名称 | `@Name` |
| `Menus/Menu/DescriptionSet/Set` | 菜单描述 | `@Info`, `@Lang` |
| `Menus/Menu/Cmd/Set` | 菜单命令 | `@Cmd`（JS 函数名） |
| `Menus/Menu/Icon/Set` | 菜单图标 | `@Icon`（路径） |
| `Menus/Menu/Group/Set` | 菜单分组 | `@Group`（0\|1\|2...） |

### PageInfos 页面信息（Page 同级）

| 路径 | 说明 | 属性 |
|------|------|------|
| `EasyWebTemplate/PageInfos/PageInfo` | 信息容器 | — |
| `PageInfos/PageInfo/Name/Set` | 信息标识 | `@Name`（如 `DEL_ITEM`, `CommonTitle`） |
| `PageInfos/PageInfo/DescriptionSet/Set` | 信息文本 | `@Info`, `@Lang` |

> **读取方式**：`UXml.retNode(doc, "Page/FrameTag/Set").getAttribute("FrameTag")`
> **写入方式**：`ele.setAttribute("FrameTag", "ListFrame")`
> **路径基准**：文档根为 `<EasyWebTemplate>`，路径从根下第一级开始（不含根元素名）。

## XItem Tag 类型参考

配置项中的 `<XItem Tag="..." />` 定义了页面元素的渲染类型。

### 标准对象 (Standard)

| Tag | 说明 | 对应 HTML |
|-----|------|-----------|
| `text` | 单行文本 | `<input type="text">` |
| `textarea` | 多行文本 | `<textarea>` |
| `span` | 文本标签 | `<span>` |
| `hidden` | 隐含字段 | `<input type="hidden">` |
| `password` | 密码输入 | `<input type="password">` |
| `passwordWithEye` | 可查看密码 | `<input type="password">` ＋ 眼睛图标 |
| `combo` | 组合框（可搜索下拉） | `<div class="ewa-combo-box">` |
| `select` | 下拉选择 | `<select>` |
| `checkbox` | 多选 | `<input type="checkbox">`（可重复） |
| `switch` | 开关 | `<div class='ewa-switch'>` |
| `radio` | 单选 | `<input type="radio">`（可重复） |
| `checkboxgrid` | 列表复选框 | `<input type="checkbox">` |
| `radiogrid` | 列表单选 | `<input type="radio">` |
| `anchor` | 链接（显示描述） | `<a class="ewa-anchor">` |
| `anchor2` | 链接（显示值内容） | `<a class="ewa-anchor">` |
| `linkButton` | 链接按钮 | `<a>` 触发表单按钮 click |
| `droplist` | 动态列表（自动补全） | `<input class="ewa-ipt-droplist">` |
| `submit` | 提交按钮 | `<input type="submit">` |
| `button` | 按钮 | `<input type="button">` |
| `butFlow` | 流程控制按钮 | `<input type="button" ewa_tag="butFlow">` |
| `QRCode` | 二维码 | `<img class="ewa-qrcode">` |

### 扩展对象 (Extend)

| Tag | 说明 | 备注 |
|-----|------|------|
| `date` | 日期选择 | 弹出日期面板 |
| `datetime` | 日期时间选择 | 弹出日期时间面板 |
| `time` | 时间选择 | 弹出时间面板 |
| `dHtml5` | HTML5 编辑器 | 含图片上传 |
| `markDown` | Markdown 编辑器 | 嵌入 iframe 编辑器 |
| `h5upload` | HTML5 文件上传 | 多文件、缩略图 → 见下方参数详解 |
| `h5TakePhoto` | HTML5 拍照 | 调用摄像头拍照 |
| `valid` | 验证码 | 图形验证码 |
| `smsValid` | 短信验证码 | 发送短信验证 |
| `signature` | 手写签名 | Canvas 签名板 |
| `user` | 自定义渲染 | 通过 Java 类自定义输出 |
| `userControl` | 控件 | 嵌入自定义控件 |
| `dataType` | 数据类型（无 UI） | 仅定义数据类型，不渲染 |
| `addressMap` | 带地图的地址栏 | 集成地图选择 |
| `gridImage` | 列表图片 | 点击放大 |
| `gridBgImage` | 列表背景图片 | 懒加载背景图 |
| `popselect` | 弹出选择 | 弹出对话框选择数据 |
| `ewaconfigitem` | EWA 配置项引用 | 嵌入其他配置项 |
| `MGAddField` | 多维表格汇总字段 | MultiGrid 专用 |
| `LogicItem` | 复合逻辑条目 | Logic 框架专用 |
| `ReportItem` | 报表项 | Report 框架专用 |
| `CombineItem` | 组合配置项 | Combine 框架专用 |
| `ComplexItem` | 复合配置项 | Complex 框架专用 |
| `SqlEditor` | SQL 编辑器 | 基于 CodeMirror |
| `JsEditor` | JavaScript 编辑器 | 基于 CodeMirror |
| `CssEditor` | CSS 编辑器 | 基于 CodeMirror |
| `XMLEditor` | XML 编辑器 | 基于 CodeMirror |
| `idempotence` | 幂等性字段 | 防重复提交，Frame 专用 |

### 基本参数说明

所有 XItem 共用的参数（配置在 `<XItem>` 标签属性或 `AttributeSet` 中）。

**主信息 (Main)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `Tag` | 多值 | 对象类型，即上表中的 Tag 值 |
| `Name` | string | 唯一名称，生成 HTML `id` 和 `name` 属性 |
| `GroupIndex` | int | 分组索引，控制 Frame 中字段排列顺序 |
| `DescriptionSet` | 多值 | 描述集：`Lang`（国别）、`Info`（显示文本）、`Memo`（备注） |
| `InitValue` | group | 初始值，页面加载时预填 |

**数据 (Data)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `DataItem` | 多值 | 数据项绑定，含字段名、类型、格式化等 |
| `DataRef` | 引用 | 数据引用，关联其他数据源 |
| `OrderSearch` | 多值 | ListFrame 中是否可排序/搜索 |
| `List` | 多值 | 静态列表数据（用于 select / checkbox / radio） |
| `DispEnc` | 多值 | 加密显示，如手机号/身份证脱敏 |

**界面 (UI)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `XStyle` | 多值 | 标准样式：`XStyleAlign`（居中）、`XStyleVAlign`（垂直居中）、`XStyleNoWrap`（不换行）、`XStyleFixed`（固定宽度）、`XStyleColor`（颜色）、`XStyleBold`（加粗）、`XStyleCursor`（鼠标图标）、`XStyleWidth`（宽度） |
| `Style` | string | 自定义 CSS 样式，直接写入 `style` 属性 |
| `ParentStyle` | string | 父容器样式（`<td>` 或 `<div>`） |
| `IsHtml` | group | 值是否以 HTML 渲染 |

**验证 (Valid)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `IsMustInput` | group | 必填项，显示 `*` 标记 |
| `MaxMinLength` | 多值 | 最大/最小长度，含 JS 校验提示 |
| `MaxMinValue` | 多值 | 最大/最小值，含 JS 校验提示 |
| `VaildEx` | 多值 | 高级验证：`VXMode`（模式）、`VXJs`（JS 校验）、`VXAction`（后端校验）、`VXOk`/`VXFail`（回调） |

**扩展 (Others)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `AttributeSet` | 多值 | 自定义属性键值对，写入 HTML 标签 |
| `EventSet` | 多值 | 事件绑定键值对，如 `onclick`、`onchange` |
| `Upload` | 多值 | 文件上传配置 → 见下方 h5upload |
| `Switch` | 多值 | 开关控件配置 |
| `CallAction` | 多值 | 按钮绑定的后端动作 |
| `OpenFrame` | 多值 | 按钮打开的 Frame 配置 |

> **Tag 取值来源参考**：`src/main/resources/system.xml/EwaConfig.xml` → `/EasyWebConfig/Items/XItems/XItem`

### h5upload 参数详解

`h5upload` 是 HTML5 文件上传控件，在 AttributeSet 中配置 `Upload` 参数组。

**基础参数（AttributeSet）**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `UpMulti` | group（yes/no） | 多文件上传，设为 `yes` 开启 |
| `UpExts` | string | 允许的扩展名，逗号分隔，如 `jpg,png,gif,pdf` |
| `UpLimit` | string | 文件大小限制，如 `100k`、`12m`、`2g` |
| `UpPath` | string | 自定义上传路径（服务器端目录） |
| `UpSaveMethod` | group | 保存模式 → [WithFrame 可用变量](#upsavemethod-withframe-变量) |
| `UpSQL` | sql | 自定义保存 SQL → [可用变量](#upsql-变量) |
| `UpNewSizes` | string | 图片缩略图尺寸，如 `100x50,200x100`（宽x高，逗号分隔） |
| `NewSizesIn` | group | 缩略图生成方式 |
| `UpUnZip` | group（yes/no） | 上传后自动解压 zip 文件 |
| `UpDelete` | group | 删除已上传文件（数据库保存模式下） |
| `UpJsonEncyrpt` | group | 返回 JSON 是否加密 |

**UpSaveMethod WithFrame 变量**：

| 变量 | 说明 |
|------|------|
| `{NAME}` | 文件二进制 |
| `{NAME}_NAME` | 文件服务器名称 |
| `{NAME}_PATH` | 文件服务器保存位置 |
| `{NAME}_PATH_SHORT` | 文件服务器保存位置（短路径） |
| `{NAME}_LOCAL_NAME` | 用户本地文件名称 |
| `{NAME}_URL` | 文件 HTTP URL |
| `{NAME}_EXT` | 文件扩展名 |
| `{NAME}_MD5` | 文件 MD5 |
| `{NAME}_SIZE` | 文件长度 |
| `{NAME}_LENGTH` | 文件长度（同 SIZE） |
| `{NAME}_UP_UNID` | 上传文件的 UNID |
| `{NAME}_CT` | 上传文件 URL 前缀 |

**UpSQL 变量**：

| 变量 | 说明 |
|------|------|
| `EWA_UP_FILE` | 文件二进制 |
| `EWA_UP_NAME` | 文件服务器名称 |
| `EWA_UP_URL` | 文件 HTTP URL |
| `EWA_UP_EXT` | 文件扩展名 |
| `EWA_UP_PATH` | 文件服务器保存位置 |
| `EWA_UP_PATH_SHORT` | 文件服务器保存位置（去除上传根路径） |
| `EWA_UP_TYPE` | 文件 HTTP Content-Type |
| `EWA_UP_UNID` | 文件 GUNID |
| `EWA_UP_FROM` | 来源（用于图片重新生成） |
| `EWA_UP_LOCAL` | 服务器本地路径 |
| `EWA_UP_LENGTH` | 文件长度 |
| `EWA_UP_MD5` | 文件 MD5 |
| `EWA_UP_LOCAL` | 用户本地文件名称 |

> **注意**：`{NAME}` 占位符中的 `NAME` 是该 XItem 的 `Name` 属性值，多个上传字段会自动区分。

## SQL 语法验证 (validateSql)

验证 SQL 语法是否正确，在事务内执行并回滚，无副作用。

**参数**：
- `db` — 数据库连接名，对应配置 XML 中 `EasyWebTemplate/Page/DataSource/Set@DataSource`
- `sql` — 待验证的 SQL 语句

**注意**：
- SQL 中的参数（如 `@MNU_ID`）需替换为测试值（如 `'TEST'` 或 `1`）
- 复杂 SQL（含 CTE、多语句）建议用 POST 方式提交，GET 方式可能因 URL 编码失败

### 简单 SQL（GET 方式）

```bash
source ewa-api.conf
curl -s "${EWA_API_URL}?method=validatesql&db=globalTravel&sql=SELECT%20*%20FROM%20ADM_USER%20WHERE%201=2" \
  -H "X-Api-Token: $(cat /tmp/.ewa_api_token)" | python3 -m json.tool
```

### 复杂 SQL（POST 方式，推荐）

```bash
source ewa-api.conf
curl -s -X POST "${EWA_API_URL}?method=validatesql&db=globalTravel" \
  -H "X-Api-Token: $(cat /tmp/.ewa_api_token)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "sql=WITH MenuTree AS (
    SELECT mnu_id, mnu_pid, 0 AS level FROM adm_menu WHERE mnu_id = 'TEST'
    UNION ALL
    SELECT m.mnu_id, m.mnu_pid, mt.level + 1 FROM adm_menu m
    INNER JOIN MenuTree mt ON m.mnu_pid = mt.mnu_id
  )
  SELECT A.*, B.* FROM ADM_MENU A
  INNER JOIN MenuTree mt ON a.mnu_id = mt.mnu_id
  WHERE a.mnu_id != 'TEST'" | python3 -m json.tool
```

### 多语句验证

```bash
# 分号分隔的多语句会依次验证
curl -s -X POST "${EWA_API_URL}?method=validatesql&db=globalTravel" \
  -H "X-Api-Token: $(cat /tmp/.ewa_api_token)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "sql=DELETE FROM ADM_MENU_GRP_IDS WHERE MG_TAG = 'TEST';
INSERT INTO ADM_MENU_GRP_IDS(MG_TAG, MNU_ID) SELECT 'TEST', MNU_ID FROM ADM_MENU WHERE MNU_ID IN (1,2,3);
UPDATE ADM_MENU_GRP SET MNU_IDS = '1,2,3' WHERE MG_TAG = 'TEST';"
```

**成功响应**：`{"MSG":"SQL 语法校验通过","RST":true,"DB":"globalTravel"}`

**失败响应**：`{"ERR":"SQL 语法错误: ...","RST":false}`

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
# validatesql 详见 "SQL 语法验证" 章节
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

### Step 2.5: Validate JS — 本地 Node.js 验证（可选但推荐）

修改包含 JavaScript 的配置前，建议先用 Node.js 验证语法：

```bash
# 提取 CDATA 中的 JS 并验证
python3 << 'PYEOF'
import re
with open('/tmp/item_updated.xml') as f:
    xml = f.read()
# AddScript/Set/Bottom 包含主要 JS 逻辑（含 AddPreRow）
m = re.search(r'<AddScript><Set>.*?<Bottom><!\[CDATA\[(.*?)\]\]></Bottom>', xml, re.DOTALL)
if m:
    js = m.group(1).strip()
    with open('/tmp/_check.js', 'w') as f:
        f.write(js)
    print(f"提取 AddScript/Bottom: {len(js)} chars")
PYEOF

# 用 Node.js 检查语法
node --check /tmp/_check.js && echo "JS 语法正确" || echo "JS 语法错误"
```

**验证 AddScript 括号匹配**：
```bash
python3 -c "
import re
with open('/tmp/item_updated.xml') as f:
    xml = f.read()
# AddScript 有 Top/Bottom（AddHtml 是 HTML 不需要验证）
for pos in ['Top', 'Bottom']:
    pattern = f'<AddScript><Set>.*?<{pos}><!\[CDATA\[(.*?)\]\]></{pos}>'
    m = re.search(pattern, xml, re.DOTALL)
    if m:
        js = m.group(1)
        if js.strip():
            p, b = js.count('('), js.count('{')
            status = '✓' if p==js.count(')') and b==js.count('}') else '✗'
            print(f'AddScript/{pos}: () {p}/{js.count(\")\")}, {{}} {b}/{js.count(\"}\")}', status)
"
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
