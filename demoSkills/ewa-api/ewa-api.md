# EWA API 参考文档

调用 EWA Servlet API 的完整方法参考。

- **API 端点**: `https://gdx/cm/EWA_DEFINE/cgi-bin/api`

## 配置

复制并填写配置文件后 `source` 加载：

```bash
cp ewa-api.conf.example ewa-api.conf
# 编辑 ewa-api.conf，填写 EWA_API_URL / EWA_API_LOGIN_ID / EWA_API_PASSWORD
source ewa-api.conf
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
| `getTable` | `<db> <tablename> [output]` | 获取表结构详情 |
| `getTableData` | `<db> <tablename> [where] [output] [page] [pagesize]` | 获取表数据（分页） |
| `previewBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> [output] [scriptpath]` | 预览业务 XML（不保存，容器不存在自动创建） |
| `createBusinessXml` | `<db> <tablename> <frametype> <operationtype> <xmlname> <itemname> [admid] [scriptpath]` | 生成并保存业务 XML，容器不存在自动创建 |
| `showScriptPaths` | — | 列出所有可用配置存储路径 |

### 参数说明

| 参数 | 说明 |
|------|------|
| `db` | 数据库连接名（定义于 `ewa_conf.xml`，如 `globalTravel`、`gyap`） |
| `xmlname` | 配置文件名，如 `ewa/m`、`ewa/studyabroad` |
| `itemname` | 配置项名，如 `ESL_SA_MAIN.F.NM` |
| `output` | 输出格式：`xml`（默认）\| `json` \| `csv`（仅 getTableData） |
| `filter` | 表名过滤，支持 `%` 通配符，如 `ESL_SA_%` |
| `frametype` | 业务 XML 框架类型：`ListFrame` \| `Frame` \| `Tree` |
| `operationtype` | 操作类型：`N`（新增）\| `M`（修改）\| `V`（查看）\| `NM`（新增+修改） |
| `page` | 页码（默认 1） |
| `pagesize` | 每页记录数（默认 10，最大 100） |
| `pk` | 主键字段（用于 getTableData 分页） |
| `admid` | 管理员 ID（createBusinessXml 可选，默认使用当前登录用户） |
| `scriptpath` | 指定配置存储路径名称，如 `pf` / `b2b`。不提供则自动选择第一个可写路径。通过 `showScriptPaths` 查询可用路径 |

## 示例

### 登录
```bash
./ewa-api.sh login
./ewa-api.sh login admin mypassword
```

### 数据库探索
```bash
# 列出 study-abroad 相关表
./ewa-api.sh getTables globalTravel "ESL_SA_%" json

# 查看表结构
./ewa-api.sh getTable globalTravel ESL_SA_MAIN json
./ewa-api.sh getTable globalTravel ESL_SA_MAJOR json

# 查看表数据（前10条）
./ewa-api.sh getTableData globalTravel ESL_SA_MAIN "" json

# 带 WHERE 条件 + 分页
./ewa-api.sh getTableData globalTravel ESL_SA_MAIN "ESL_SA_COUNTRY='CN'" json 1 20
```

### 业务 XML 生成
```bash
# 预览学校主表列表 XML（不保存，用于确认生成结果）
./ewa-api.sh previewBusinessXml globalTravel ESL_SA_MAIN ListFrame V ewa/studyabroad json

# 指定保存到 pf（jdbc:ewa2023）
./ewa-api.sh previewBusinessXml globalTravel ESL_SA_MAIN ListFrame V ewa/studyabroad json pf

# 生成并保存学校主表表单 XML（新增+修改）
./ewa-api.sh createBusinessXml globalTravel ESL_SA_MAIN Frame NM ewa/studyabroad ESL_SA_MAIN.F.NM

# 指定保存到 pf
./ewa-api.sh createBusinessXml globalTravel ESL_SA_MAIN Frame NM ewa/studyabroad ESL_SA_MAIN.F.NM pf

# 生成专业表列表 XML
./ewa-api.sh createBusinessXml globalTravel ESL_SA_MAJOR ListFrame V ewa/studyabroad ESL_SA_MAJOR.LF.V
```

### 配置存储路径
```bash
# 列出所有可用配置存储路径
./ewa-api.sh showScriptPaths
```

### 配置项操作
```bash
# 读取配置项
./ewa-api.sh getConfItem "ewa/studyabroad" "ESL_SA_MAIN.F.NM" json

# 复制配置项
./ewa-api.sh runConfItem "ewa/studyabroad" "ESL_SA_MAIN.F.NM" "ESL_SA_MAIN.F.V"

# 删除配置项
./ewa-api.sh deleteConfItem "ewa/studyabroad" "ESL_SA_MAIN.F.NM"
```

## DOM 节点路径参考

完整 DOM 结构及 XPath 路径详见 [SKILL.md → DOM 节点路径参考](SKILL.md#dom-节点路径参考)。摘要如下：

```
EasyWebTemplates                          # 文件根
└── EasyWebTemplate (@Name)              # 单个配置项
    ├── Page
    │   ├── FrameTag/Set (@FrameTag)      # ListFrame|Frame|Tree|...
    │   ├── Name/Set (@Name)
    │   ├── DataSource/Set (@DataSource)
    │   ├── DescriptionSet/Set (@Info, @Lang, @Memo)
    │   ├── Size/Set (@Width, @Height, ...)
    │   ├── SkinName/Set (@SkinName)
    │   ├── Cached/Set / Acl/Set / Log/Set
    │   ├── XItems
    │   │   └── XItem
    │   │       ├── Tag/Set (@Tag)         # 必填：元素类型
    │   │       ├── Name/Set (@Name)       # 必填：字段名
    │   │       ├── DataItem/Set           # 数据绑定
    │   │       ├── DescriptionSet/Set     # 标签
    │   │       └── ...                    # 见 SKILL.md
    │   ├── AddHtml / AddScript / AddCss   # 自定义代码
    │   ├── PageAttributeSet / GroupSet    # 属性/分组
    │   ├── ListUI / PageSize              # ListFrame 专用
    │   ├── Tree / TreeIconSet             # Tree 专用
    │   ├── Menu / MenuShow                # Menu 专用
    │   ├── HtmlFrame / FrameHtml          # Frame 专用
    │   └── LogicShow                      # Logic 专用
    ├── Action                             # 后端动作
    │   ├── ActionSet / CallSet
    │   ├── SqlSet / UrlSet / ScriptSet
    │   ├── XmlSet / XmlSetData / ClassSet
    │   └── CSSet
    ├── Menus / Menu                       # 菜单定义
    └── PageInfos / PageInfo               # 页面信息文本
```

> **读取节点**：`UXml.retNode(doc, "Page/FrameTag/Set").getAttribute("FrameTag")`
> **路径基准**：文档根为 `<EasyWebTemplate>`，路径从根下第一级开始。

## XItem Tag 类型参考

配置项中的 `<XItem Tag="..." />` 定义了页面元素的渲染类型，Tag 值来源于 `EwaConfig.xml` 的 `/EasyWebConfig/Items/XItems/XItem`。

### 标准对象 (Standard)

| Tag | 说明 | HtmlTag |
|-----|------|---------|
| `text` | 单行文本 | `input$text` |
| `textarea` | 多行文本 | `textarea` |
| `span` | 文本标签 | `span` |
| `hidden` | 隐含字段 | `input$hidden` |
| `password` | 密码输入 | `input$password` |
| `passwordWithEye` | 可查看密码 | `input$password` |
| `combo` | 组合框（可搜索下拉） | `combo` |
| `select` | 下拉选择 | `select` |
| `checkbox` | 多选 | `input$checkbox` |
| `switch` | 开关 | — |
| `radio` | 单选 | `input$radio` |
| `checkboxgrid` | 列表复选框 | — |
| `radiogrid` | 列表单选 | `input$radio` |
| `anchor` | 链接（显示描述） | — |
| `anchor2` | 链接（显示值内容） | — |
| `linkButton` | 链接按钮 | — |
| `droplist` | 动态列表（自动补全） | — |
| `submit` | 提交按钮 | `input$submit` |
| `button` | 按钮 | `input$button` |
| `butFlow` | 流程控制按钮 | — |
| `QRCode` | 二维码 | — |

### 扩展对象 (Extend)

| Tag | 说明 | 备注 |
|-----|------|------|
| `date` | 日期选择 | 弹出日期面板 |
| `datetime` | 日期时间选择 | 弹出日期时间面板 |
| `time` | 时间选择 | 弹出时间面板 |
| `h5upload` | HTML5 文件上传 | 多文件、缩略图 → 见下方参数详解 |
| `h5TakePhoto` | HTML5 拍照 | 调用摄像头 |
| `valid` | 图形验证码 | — |
| `smsValid` | 短信验证码 | 发送验证短信 |
| `signature` | 手写签名 | Canvas 签名板 |
| `user` | 自定义渲染 | Java 类自定义 |
| `userControl` | 自定义控件 | 嵌入控件 |
| `dataType` | 数据类型（无 UI） | 不渲染，仅定义类型 |
| `addressMap` | 带地图的地址栏 | 集成地图 |
| `gridImage` | 列表图片 | 点击放大 |
| `gridBgImage` | 列表背景图片 | 懒加载 |
| `popselect` | 弹出选择 | 弹出对话框 |
| `ewaconfigitem` | EWA 配置项引用 | 嵌入其他配置项 |
| `MGAddField` | 多维表格汇总 | MultiGrid 专用 |
| `LogicItem` | 复合逻辑条目 | Logic 框架专用 |
| `ReportItem` | 报表项 | Report 框架专用 |
| `CombineItem` | 组合配置项 | Combine 框架专用 |
| `ComplexItem` | 复合配置项 | Complex 框架专用 |
| `SqlEditor` | SQL 编辑器 | CodeMirror |
| `JsEditor` | JavaScript 编辑器 | CodeMirror |
| `CssEditor` | CSS 编辑器 | CodeMirror |
| `XMLEditor` | XML 编辑器 | CodeMirror |
| `idempotence` | 幂等性字段 | Frame 防重复提交 |
| `dHtml5` | HTML5 编辑器 | 含图片上传 |
| `markDown` | Markdown 编辑器 | iframe 编辑器 |

### 基本参数说明

所有 XItem 共用参数（配置在标签属性或 `AttributeSet` 中）。

**主信息 (Main)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `Tag` | 多值 | 对象类型，即上表中的 Tag 值 |
| `Name` | string | 唯一名称，生成 HTML `id`/`name` |
| `GroupIndex` | int | 分组索引，控制 Frame 中排列顺序 |
| `DescriptionSet` | 多值 | 描述集：`Lang`（国别）、`Info`（标签）、`Memo`（备注） |
| `InitValue` | group | 初始值 |

**数据 (Data)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `DataItem` | 多值 | 数据项，含字段名、类型、格式化 |
| `DataRef` | 引用 | 数据引用，关联其他数据源 |
| `OrderSearch` | 多值 | ListFrame 中是否排序/搜索 |
| `List` | 多值 | 静态列表数据（select / checkbox / radio） |
| `DispEnc` | 多值 | 加密显示（脱敏） |

**界面 (UI)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `XStyle` | 多值 | 标准样式：`XStyleAlign`（居中）、`XStyleVAlign`（垂直居中）、`XStyleNoWrap`（不换行）、`XStyleFixed`（固定宽度）、`XStyleColor`（颜色）、`XStyleBold`（加粗）、`XStyleCursor`（鼠标）、`XStyleWidth`（宽度） |
| `Style` | string | 自定义 CSS 样式 |
| `ParentStyle` | string | 父容器样式 |
| `IsHtml` | group | 值是否 HTML 渲染 |

**验证 (Valid)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `IsMustInput` | group | 必填项 |
| `MaxMinLength` | 多值 | 最大最小长度校验 |
| `MaxMinValue` | 多值 | 最大最小值校验 |
| `VaildEx` | 多值 | 高级验证：`VXMode`、`VXJs`、`VXAction`、`VXOk`/`VXFail` |

**扩展 (Others)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `AttributeSet` | 多值 | 自定义属性，写入 HTML 标签 |
| `EventSet` | 多值 | 事件绑定（onclick / onchange 等） |
| `Upload` | 多值 | 文件上传 → 见下方 h5upload |
| `Switch` | 多值 | 开关控件 |
| `CallAction` | 多值 | 按钮后端动作 |
| `OpenFrame` | 多值 | 按钮打开的 Frame |

> **数据来源**：`src/main/resources/system.xml/EwaConfig.xml` → `/EasyWebConfig/Items/XItems/XItem`

### h5upload 参数详解

`h5upload` 是 HTML5 文件上传控件，通过 `Upload` 参数组（在 AttributeSet 内配置）控制上传行为。

**基础参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `UpMulti` | group | `yes` 开启多文件上传 |
| `UpExts` | string | 允许扩展名，逗号分隔，如 `jpg,png,gif,pdf` |
| `UpLimit` | string | 文件大小限制，如 `100k`、`12m`、`2g` |
| `UpPath` | string | 服务器上传路径 |
| `UpSaveMethod` | group | 保存模式，取值 `WithFrame` → [可用变量](#upsavemethod-变量) |
| `UpSQL` | sql | 自定义保存 SQL → [可用变量](#upsql-可用变量) |
| `UpNewSizes` | string | 图片缩略图尺寸，如 `100x50,200x100`（宽x高，逗号分隔） |
| `NewSizesIn` | group | 缩略图生成方式 |
| `UpUnZip` | group | `yes` 上传后自动解压 zip |
| `UpDelete` | group | 删除已上传文件 |
| `UpJsonEncyrpt` | group | 返回 JSON 是否加密 |

**UpSaveMethod 变量**（WithFrame 模式，`{NAME}` 替换为 XItem 的 Name）：

| 变量 | 说明 |
|------|------|
| `{NAME}` | 文件二进制 |
| `{NAME}_NAME` | 文件服务器名称 |
| `{NAME}_PATH` | 文件服务器保存位置 |
| `{NAME}_PATH_SHORT` | 短路径 |
| `{NAME}_LOCAL_NAME` | 用户本地文件名称 |
| `{NAME}_URL` | 文件 HTTP URL |
| `{NAME}_EXT` | 文件扩展名 |
| `{NAME}_MD5` | 文件 MD5 |
| `{NAME}_SIZE` / `{NAME}_LENGTH` | 文件大小 |
| `{NAME}_UP_UNID` | 上传文件 UNID |
| `{NAME}_CT` | URL 前缀 |

**UpSQL 可用变量**：

| 变量 | 说明 |
|------|------|
| `EWA_UP_FILE` | 文件二进制 |
| `EWA_UP_NAME` | 服务器文件名 |
| `EWA_UP_URL` | HTTP URL |
| `EWA_UP_EXT` | 扩展名 |
| `EWA_UP_PATH` | 服务器路径 |
| `EWA_UP_PATH_SHORT` | 短路径（去除根路径） |
| `EWA_UP_TYPE` | Content-Type |
| `EWA_UP_UNID` | GUNID |
| `EWA_UP_FROM` | 来源（图片重新生成用） |
| `EWA_UP_LOCAL` | 本地路径 / 本地文件名 |
| `EWA_UP_LENGTH` | 文件长度 |
| `EWA_UP_MD5` | 文件 MD5 |

## 脚本位置

- Linux/macOS: `ewa-api.sh` → `shell/call-ewa-api.sh`
- Windows: `shell/call-ewa-api.bat`