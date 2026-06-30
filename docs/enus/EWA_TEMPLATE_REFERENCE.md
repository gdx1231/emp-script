# EasyWebTemplate Complete Reference

> Core configuration item definition format for the EWA (Easy Web Application) framework.  
> Each `EasyWebTemplate` describes a complete page (including structure, data, interaction, and styles).

---

## Table of Contents

1. [File Structure](#1-file-structure)
2. [Root Node Attributes](#2-root-node-attributes)
3. [Page — Page Configuration](#3-page--page-configuration)
4. [XItem — Page Element Definition](#4-xitem--page-element-definition)
5. [Action — Action Chain Configuration](#5-action--action-chain-configuration)
6. [Menus — Menu Configuration](#6-menus--menu-configuration)
7. [PageInfos — Page Information](#7-pageinfos--page-information)
8. [Charts — Chart Configuration](#8-charts--chart-configuration)
9. [Workflows — Workflow Configuration](#9-workflows--workflow-configuration)
10. [FrameTag Values](#10-frametag-values)
11. [XItem Tag Types](#11-xitem-tag-types)
12. [Naming Convention](#12-naming-convention)
13. [Validation Rules](#13-validation-rules)

---

## 1. File Structure

```
<EasyWebTemplates>                        ← File root node
    ├── <EasyWebTemplate Name="...">      ← 1st configuration item
    │   ├── <Page>                        ← Page definition
    │   ├── <XItems>                      ← Field elements
    │   ├── <Action>                      ← Action chain
    │   ├── <Menus />                     ← Menu
    │   ├── <Charts />                    ← Charts
    │   ├── <PageInfos />                 ← Prompt info
    │   └── <Workflows />                 ← Workflows
    ├── <EasyWebTemplate Name="...">      ← 2nd configuration item
    │   └── ...
    └── ...
</EasyWebTemplates>
```

File location: `define.xml/ewa/*.xml`, or user-defined business configuration paths.

---

## 2. Root Node Attributes

| Attribute | Required | Description |
|:----------|:---------|:------------|
| `Name` | **Yes** | Unique identifier for the configuration item, e.g. `users.F.NM` (see [§12](#12-naming-convention)) |
| `CreateDate` | No | Creation time, format `yyyy-MM-dd HH:mm:ss` |
| `UpdateDate` | No | Last modified time |
| `Author` | No | Author identifier |

```xml
<EasyWebTemplate Name="users.F.NM" CreateDate="2024-01-15 10:00:00"
    UpdateDate="2024-06-30 14:00:00" Author="admin">
```

---

## 3. Page — Page Configuration

### 3.1 Basic Parameters

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Page/FrameTag/Set` | **Page type (required)** | `@FrameTag` (see [§10](#10-frametag-values)) |
| `Page/Name/Set` | Page name (auto-synced with root Name on submit) | `@Name` |
| `Page/SkinName/Set` | Skin/theme | `@SkinName` (e.g. `Test1`, `Blue`), `@IsXhtml` (0\|1) |
| `Page/DataSource/Set` | Database connection name | `@DataSource` (e.g. `ewa`, `ow_main`) |
| `Page/DescriptionSet/Set` | Page description (multi-language, multiple allowed) | `@Info`, `@Lang` (`zhcn`\|`enus`), `@Memo` |
| `Page/Size/Set` | Page dimensions | `@Width`, `@Height`, `@HAlign` (left\|center\|right), `@VAlign` (top\|middle\|bottom), `@HiddenCaption` (0\|1), `@FrameCols` (C1\|C2\|C3, frame column count) |
| `Page/Cached/Set` | Cache configuration | `@CachedType` (`none`\|`all`), `@CachedSeconds` |
| `Page/Acl/Set` | Access control class | `@Acl` (Java full class name) |
| `Page/Log/Set` | Logging | `@Log` |
| `Page/ConfigMemo/Set` | Configuration remarks (nested self-closing) | `<ConfigMemo />` |
| `Page/AllowJsonExport` | JSON export permission | Existence means allowed |
| `Page/PageAttributeSet/Set` | Page-level HTML attributes (multiple allowed) | `@PageAttName`, `@PageAttValue` |
| `Page/RowAttributeSet/Set` | Row-level HTML attributes (multiple allowed) | `@RowAttName`, `@RowAttValue` |
| `Page/GroupSet/Set` | Group definition | `@GroupName`, `@GroupIndex`, `@GroupCols` |

### 3.2 Extended Content

| Path | Description | Format |
|:-----|:------------|:-------|
| `Page/AddHtml/Set/Top` | Page top HTML (**only Top/Bottom child elements**) | CDATA |
| `Page/AddHtml/Set/Bottom` | Page bottom HTML | CDATA |
| `Page/AddScript/Set/Top` | Page top JavaScript (**only Top/Bottom child elements**) | CDATA |
| `Page/AddScript/Set/Bottom` | Page bottom JavaScript | CDATA |
| `Page/AddCss/Set` | Additional CSS | CDATA or `<AddCss />` |

> **Validation rule**: Only `<Top>` and `<Bottom>` child elements are allowed under `AddHtml/Set` and `AddScript/Set`; other elements (e.g. `<AddPreRow>`) will be intercepted by `ConfigValidator`.

### 3.3 ListFrame Specific

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Page/ListUI/Set` | List interaction | `@luButtons` (0\|1), `@luSelect` (S=single\|M=multi), `@luSearch` (0\|1), `@luDblClick` (0\|1), `@luDblClickIdx`, `@luStickyHeaders` |
| `Page/PageSize/Set` | Pagination | `@PageSize`, `@KeyField` (primary key field), `@IsSplitPage` (0\|1), `@StatusField`, `@Recycle`, `@AllowExport`, `@ColSize` |
| `Page/BoxJson` | Top search area JSON | Existence takes effect |
| `Page/LeftJson` | Left filter JSON | Existence takes effect |
| `Page/ChartsShow/Set` | Chart display | `@ChartsShow` |
| `Page/RedrawJson` | Form redraw JSON | Existence takes effect |

### 3.4 Tree Specific

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Page/Tree/Set` | Tree configuration | `@Key` (node key), `@ParentKey` (parent node key), `@Text` (display text), `@Title`, `@Level`, `@Order`, `@RootId`, `@LoadByLevel` (1), `@MenuGroup`, `@AddPara1-3` |
| `Page/TreeIconSet/Set` | Tree node icons | `@Test` (condition), `@Open`, `@Close`, `@Filter` |

### 3.5 Menu Specific

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Page/Menu/Set` | Menu configuration | `@Key`, `@ParentKey`, `@Text`, `@Icon`, `@IconType` (Image\|Font), `@Cmd`, `@RootId` |
| `Page/MenuShow/Set` | Menu display | `@MenuShow` (`EWA_MENU`\|`menus`) |

### 3.6 Other Frames

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Page/HtmlFrame/Set` | Frame split layout | `@FrameType` (H=horizontal\|V=vertical), `@FrameBorder` (0\|1), `@FrameSize` (e.g. `50%,*`), `@FrameSubUrl` |
| `Page/FrameHtml/Set` | Frame HTML | CDATA |
| `Page/MGAxisX/Set` | MultiGrid X-axis | Used with FrameTag=MultiGrid |
| `Page/MGAxisY/Set` | MultiGrid Y-axis | Used with FrameTag=MultiGrid |
| `Page/MGCell/Set` | MultiGrid cell | Used with FrameTag=MultiGrid |
| `Page/LogicShow/Set` | Logic condition display | `@Name`, `@HiddenFields`, `@ParaExp` (e.g. `'@linktype'='group'`) |

---

## 4. XItem — Page Element Definition

### 4.1 Main Parameters

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `XItem/Tag/Set` | **Element type (required)** | `@Tag` (see [§11](#11-xitem-tag-types)), `@IsLFEdit` (0=read-only\|1=editable\|2=inline edit), `@SpanShowAs` |
| `XItem/Name/Set` | **Field name (required)** | `@Name` |
| `XItem/GroupIndex/Set` | Group index | `@GroupIndex` (integer controlling sort order) |
| `XItem/InitValue/Set` | Initial value | `@InitValue` (special macros: `SEQID`, `GUID`, `Tody`, `Tody-Time`) |
| `XItem/DescriptionSet/Set` | Field description (multi-language, multiple allowed) | `@Info`, `@Lang` (`zhcn`\|`enus`), `@Memo` |

### 4.2 UI Parameters

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `XItem/XStyle/Set` | Standard style | `@XStyleAlign`, `@XStyleVAlign`, `@XStyleNoWrap` (yes\|no), `@XStyleFixed` (yes\|no), `@XStyleColor`, `@XStyleBold`, `@XStyleCursor`, `@XStyleWidth` |
| `XItem/Style/Set` | Custom CSS | `@Style` (written directly into the style attribute) |
| `XItem/ParentStyle/Set` | Parent container style | `@ParentStyle` (e.g. `width:400px`) |
| `XItem/IsHtml/Set` | HTML rendering | `@IsHtml` (0\|1) |

### 4.3 Data Parameters

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `XItem/DataItem/Set` | Data binding | `@DataField` (bound field), `@DataType` (String\|Int\|BigInt\|Boolean\|Date\|Time\|DateTime\|Binary\|...), `@Format` (Date\|ShortDate\|Time\|DateTime\|...), `@IsEncrypt`, `@Valid` (Email\|Number\|...), `@DisableOnModify`, `@SumBottom`, `@NumberScale`, `@Translation`, `@TransTarget`, `@TriggerValid`, `@Icon`, `@IconLoction` (left\|right), `@FrameOneCell`, `@Trim` |
| `XItem/DataRef/Set` | Data reference | `@RefKey`, `@RefShow`, `@RefMulti` (0\|1), `@RefShowStyle`, `@RefShowType`, `@RefSql` |
| `XItem/OrderSearch/Set` | Sort/search | `@IsOrder` (0\|1), `@SearchType` (text\|date\|fix\|...), `@IsGroup`, `@IsGroupDefault`, `@IsSearchQuick`, `@SearchSql`, `@SearchMulti` (1\|2), `@SearchExp`, `@OrderExp`, `@GroupTestLength` |
| `XItem/List/Set` | Static/dynamic list | `@Sql`, `@DisplayField`, `@ValueField`, `@DisplayList`, `@ValueList`, `@TitleField`, `@TitleList`, `@ListShowType`, `@ListAddBlank` (1), `@ListFilterField`, `@ListFilterType` |
| `XItem/DispEnc/Set` | Encrypted display | `@EncShowUrl`, `@EncType` |

### 4.4 Validation Parameters

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `XItem/IsMustInput/Set` | Required | `@IsMustInput` (0\|1) |
| `XItem/MaxMinLength/Set` | Length limit | `@MaxLength`, `@MinLength` |
| `XItem/MaxMinValue/Set` | Value range | `@MaxValue`, `@MinValue` |
| `XItem/Switch/Set` | Switch | `@SwtAction`, `@SwtOnText`, `@SwtOffText` |
| `XItem/VaildEx/Set` | Advanced validation | `@VXMode` (js\|action), `@VXJs`, `@VXAction`, `@VXOk`, `@VXFail` |

### 4.5 Interaction / Extended Parameters

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `XItem/AttributeSet/Set` | Custom HTML attributes (multiple allowed) | `@AttName`, `@AttValue`, `@AttLogic` |
| `XItem/EventSet/Set` | Event binding (multiple allowed) | `@EventName` (`onclick`\|`onchange`\|`onblur`\|`ewa_click`\|...), `@EventType` (Javascript), `@EventValue` (JS code), `@EventLogic` |
| `XItem/CallAction/Set` | Button action | `@Action` (action name), `@AfterTip`, `@ConfirmInfo` |
| `XItem/OpenFrame/Set` | Popup button | `@CallXmlName`, `@CallItemName`, `@CallMethod` (OpenReloadClose etc.), `@CallParas` (e.g. `id=@id`), `@AttatchParas` |
| `XItem/Frame/Set` | Embedded frame | `@CallXmlName`, `@CallItemName`, `@CallPara` |
| `XItem/UserControl/Set` | Custom control | `@UserControl` |
| `XItem/DefineFrame/Set` | Definition frame | `@CallXmlName`, `@CallItemName`, `@CallPara` |
| `XItem/PopFrame/Set` | Popup window | `@PopXmlName`, `@PopItemName`, `@PopPara`, `@PopTitleField` |
| `XItem/UserSet/Set` | User-defined | `@Lang`, and other custom attributes |
| `XItem/Upload/Set` | File upload (h5upload) | `@UpType` (0\|1\|2), `@UpMulti` (yes\|no), `@UpExts`, `@UpLimit` (M), `@UpPath`, `@UpSaveMethod` (FileSystem\|DB), `@UpSQL`, `@UpNewSizes` (e.g. `100x100`, `200x200`), `@UpUnZip` (yes\|no), `@UpDelete` (yes\|no), `@UpJsonEncyrpt` (yes\|no) |
| `XItem/MGAddField/Set` | MultiGrid aggregation | `@MgfCalc`, `@MgfComput`, `@MgfId` |
| `XItem/AnchorParas/Set` | Link parameters | `@aHref`, `@aTarget` |
| `XItem/LinkButtonParas/Set` | Link button | `@lkbButtonId` |
| `XItem/DopListShow/Set` | Dynamic list display | `@DopListShow` |
| `XItem/ReportCfg/Set` | Report configuration | `@ReportCfg` |
| `XItem/CombineFrame/Set` | Combined frame | `@CombineFrame` |
| `XItem/AddrMapRels/Set` | Address-map relation | `@AmrCountry`, `@AmrProvince`, `@AmrCity`, `@AmrZip`, `@AmrLat`, `@AmrLng` |
| `XItem/QRCode/Set` | QR code | Existence takes effect |
| `XItem/ImageDefault/Set` | Default image | `@ImageDefault` |
| `XItem/signature/Set` | Handwritten signature | `@SignColor`, `@SignBgColor`, `@SignFormat` (png\|svg), `@SignLineWidth`, `@SignPath` |

---

## 5. Action — Action Chain Configuration

### 5.1 Structure Overview

```
<Action>
    <ActionSet>              ← Action collection definition
        <Set Type="...">     ← Single action
            <CallSet>        ← Call chain
                <Set CallName="..." CallType="..." Test="..." />
            </CallSet>
        </Set>
    </ActionSet>
    <SqlSet>                 ← SQL action implementation
    <XmlSet>                 ← XML data action
    <XmlSetData>             ← XML data source binding
    <ClassSet>               ← Java class action
    <ScriptSet>              ← Script action
    <UrlSet>                 ← URL redirect action
    <CSSet>                  ← Cross-action parameters
    <JSONSet />              ← JSON action
    <CallSet>                ← Global call chain
</Action>
```

### 5.2 ActionSet

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Action/ActionSet/Set` | Action definition | `@Type` (action type), `@IsPostMsg` (yes\|no), `@LogMsg`, `@Transaction` |
| `Action/ActionSet/Set/CallSet/Set` | Action call chain (multiple allowed) | `@CallName` (target reference), `@CallType` (see below), `@Test` (conditional expression), `@CallIsChk` |

**CallType values**:

| Value | Description | Target Reference |
|:------|:------------|:-----------------|
| `SqlSet` | SQL execution | `Action/SqlSet/Set[@Name]` |
| `UrlSet` | URL redirect | `Action/UrlSet/Set[@Name]` |
| `ScriptSet` | JavaScript execution | `Action/ScriptSet/Set[@Name]` |
| `XmlSet` | XML data operation | `Action/XmlSet/Set[@Name]` |
| `ClassSet` | Java class invocation | `Action/ClassSet/Set[@Name]` |

**Common Type values**: `OnPageLoad`, `OnPagePost`, `OnFrameDelete`, `OnFrameRestore`, `OnTreeNodeNew`, `OnTreeNodeDelete`, `OnTreeNodeRename`, `OnListFrameUpdateCell`, `CheckError`, `ExtendAction0-2`

### 5.3 SqlSet

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Action/SqlSet/Set` | SQL action | `@Name` (referenced by CallName), `@SqlType` (`query`\|`update`), `@TransType` (yes\|no\|empty) |
| `Action/SqlSet/Set/Sql` | SQL statement | CDATA, supports `@param_name` parameter binding |
| `Action/SqlSet/Set/CSSet/Set` | SQL parameter definition | `@ParaName`, `@Name`, `@CSType` (all), `@Option` (C) |

### 5.4 XmlSet / XmlSetData

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Action/XmlSet/Set` | XML action | `@Name`, `@XmlAction` (load\|update\|insert\|deletes\|inertOrUpdate), `@XmlSetData` (bind XmlSetData Name), `@XmlFields`, `@XmlWhere` |
| `Action/XmlSetData/Set` | XML data source | `@Name`, `@XmlName` (XML file path), `@XmlFields`, `@XmlTagPath`, `@XmlLoadType` (attribute\|childnode) |

### 5.5 ClassSet

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Action/ClassSet/Set` | Java class action | `@Name`, `@ClassName` (full class name), `@MethodName`, `@ConData` (constructor parameters), `@MethodData` (method parameters, `@param` format), `@XmlTag` |

### 5.6 ScriptSet

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Action/ScriptSet/Set` | Script action | `@Name`, `@ScriptType` (javascript) |
| `Action/ScriptSet/Set/Script` | Script content | CDATA |

### 5.7 UrlSet

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Action/UrlSet/Set` | URL redirect | `@Name`, `@Url` |
| `Action/UrlSet/Set/CSSet/Set` | URL parameters | `@ParaName`, `@Name`, `@CSType`, `@Domain`, `@Life` |

---

## 6. Menus — Menu Configuration

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Menus/Menu` | Menu container | `@Name` (menu item unique identifier) |
| `Menus/Menu/Name/Set` | Menu name | `@Name` |
| `Menus/Menu/DescriptionSet/Set` | Description (multi-language) | `@Info`, `@Lang` |
| `Menus/Menu/Cmd/Set` | Click command | `@Cmd` (JS function name) |
| `Menus/Menu/Icon/Set` | Icon | `@Icon` (path) |
| `Menus/Menu/Group/Set` | Group | `@Group` (0\|1\|2...) |

---

## 7. PageInfos — Page Information

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `PageInfos/PageInfo` | Info item | `@Name` (e.g. `DEL_ITEM`, `CommonTitle`, `msg0-2`) |
| `PageInfos/PageInfo/Name/Set` | Info identifier | `@Name` |
| `PageInfos/PageInfo/DescriptionSet/Set` | Info text | `@Info`, `@Lang` |

---

## 8. Charts — Chart Configuration

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Charts/Chart` | Chart item | `@Name` |
| `Charts/Chart/Name/Set` | Chart name | `@Name` |
| `Charts/Chart/DescriptionSet/Set` | Description | `@Info`, `@Lang` |
| `Charts/Chart/ChartType/Set` | Chart type | `@ChartType` (LINE\|PIE\|PIE3D\|COLUMN\|COLUMN3D\|BAR\|BAR3D\|AREA\|SCATTER\|GAUGE\|FUNNEL\|RADAR) |
| `Charts/Chart/ChartSize/Set` | Chart size | `@ChartSize` (e.g. `800x400`) |
| `Charts/Chart/DataMap/Set` | Data mapping | `@Name`, `@Value`, `@Title`, `@DataType` |

---

## 9. Workflows — Workflow Configuration

| Path | Description | Attributes |
|:-----|:------------|:-----------|
| `Workflows/Workflow` | Workflow item | `@Name` |
| `Workflows/Workflow/Name/Set` | Name | `@Name` |
| `Workflows/Workflow/DescriptionSet/Set` | Description | `@Info`, `@Lang` |
| `Workflows/Workflow/WfType/Set` | Process type | `@WfType` |
| `Workflows/Workflow/WfLogic/Set` | Process logic | `@WfLogic` |
| `Workflows/Workflow/WfAction/Set` | Process action | `@WfAction` |

---

## 10. FrameTag Values

| Value | Description | Java Class |
|:------|:------------|:-----------|
| `Frame` | Standard form | `FrameFrame` |
| `ListFrame` | List page | `FrameList` |
| `Grid` | Grid editor | `FrameGrid` |
| `Tree` | Tree structure | `FrameTree` |
| `Menu` | Menu | `FrameMenu` |
| `MultiGrid` | Multi-dimensional grid | `FrameMultiGrid` |
| `Logic` | Logic page | `FrameLogic` |
| `Report` | Report | `FrameReport` |
| `Combine` | Combined page | `FrameCombine` |
| `Complex` | Composite page | `FrameComplex` |

---

## 11. XItem Tag Types

### 11.1 Standard Components

| Tag | HTML | Description |
|:----|:-----|:------------|
| `text` | `<input type="text">` | Single-line text |
| `textarea` | `<textarea>` | Multi-line text |
| `span` | `<span>` | Read-only text label |
| `hidden` | `<input type="hidden">` | Hidden field |
| `password` | `<input type="password">` | Password input |
| `passwordWithEye` | `<input type="password">` + eye icon | Viewable password |
| `combo` | `<div class="ewa-combo-box">` | Searchable combo box |
| `select` | `<select>` | Drop-down selection |
| `checkbox` | `<input type="checkbox">` | Multi-select |
| `switch` | Custom switch | Toggle switch |
| `radio` | `<input type="radio">` | Single select |
| `checkboxgrid` | Table multi-select | Grid checkboxes |
| `radiogrid` | Table single select | Grid radio buttons |
| `anchor` | `<a>` | Hyperlink |
| `anchor2` | `<a>` | Hyperlink (new tab) |
| `linkButton` | `<button>` | Link button |
| `droplist` | Drop-down list | Custom drop-down |
| `submit` | `<input type="submit">` | Submit button |
| `button` | `<button>` | Normal button |
| `butFlow` | Button group | Process action buttons |
| `QRCode` | Canvas | QR code |

### 11.2 Extended Components

| Tag | Description |
|:----|:------------|
| `date` | Date picker |
| `datetime` | Date-time picker |
| `time` | Time picker |
| `dHtml5` | HTML5 rich text editor |
| `markDown` | Markdown editor |
| `h5upload` | HTML5 file upload |
| `h5TakePhoto` | HTML5 photo/video capture |
| `valid` | Verification code |
| `smsValid` | SMS verification code |
| `signature` | Handwritten signature |
| `user` | User selector |
| `userControl` | Custom control |
| `dataType` | Data type selector |
| `addressMap` | Address map |
| `gridImage` | Grid image |
| `gridBgImage` | Grid background image |
| `popselect` | Popup selector |
| `ewaconfigitem` | EWA config item selector |
| `MGAddField` | MultiGrid add field |
| `LogicItem` | Logic frame sub-item |
| `ReportItem` | Report frame sub-item |
| `CombineItem` | Combine frame sub-item |
| `ComplexItem` | Complex frame sub-item |
| `SqlEditor` | SQL code editor |
| `JsEditor` | JavaScript code editor |
| `CssEditor` | CSS code editor |
| `XMLEditor` | XML code editor |
| `idempotence` | Idempotence token |

### 11.3 Deprecated

| Tag | Description |
|:----|:------------|
| `file` | Old file upload (replaced by h5upload) |
| `dHtml` | Old rich text editor |
| `dHtmlNoImages` | Old rich text (images disabled) |
| `image` | Old image control |
| `swffile` | SWF file |
| `SwfDoc` | SWF document |
| `SwfTakePhoto` | SWF photo capture |

---

## 12. Naming Convention

Configuration item `Name` follows the `{Object}.{FrameType}.{Mode}` format:

| Mode | Abbreviation | Description |
|:-----|:-------------|:------------|
| New | N | Create |
| Modify | M | Modify |
| View | V | View |
| New/Modify | NM | Create or modify |

| Frame | Abbreviation |
|:------|:-------------|
| Frame | F |
| ListFrame | LF |
| Tree | T |
| Menu | M |
| Grid | G |
| MultiGrid | MG |

**Examples**:
- `users.F.NM` — User form (create/modify)
- `orders.LF.M` — Order list (modify mode)
- `products.T.V` — Product tree (view mode)
- `adm_menu.listframe.view` — Menu list (historical naming style)

---

## 13. Validation Rules

`ConfigValidator.validateItemXml()` performs the following validations:

| # | Validation Item | Rule |
|:--|:----------------|:-----|
| 1 | XML format | Parseable, no syntax errors |
| 2 | Root element | Must be `EasyWebTemplate` |
| 3 | Name attribute | Must not be empty, must match the requested itemname |
| 4 | Page node | Must exist |
| 5 | FrameTag | Must exist and its value must be in the valid list at [§10](#10-frametag-values) |
| 6 | XItem Tag | Each XItem's Tag must be in the valid list at [§11](#11-xitem-tag-types) |
| 7 | AddHtml child elements | Only `<Top>` and `<Bottom>` allowed under `AddHtml/Set` |
| 8 | AddScript child elements | Only `<Top>` and `<Bottom>` allowed under `AddScript/Set` |
| 9 | Page/Name consistency | Auto-corrected if inconsistent with root Name (non-blocking) |
