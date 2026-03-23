# emp-script XML 配置文件说明

本文档描述 `src/main/resources` 目录下的 XML 配置文件结构和用途。

## 目录结构

```
src/main/resources/
├── define.xml/              # 系统定义配置目录
│   └── ewa/                 # EWA 核心配置
│       ├── ewa.xml          # 系统模板定义
│       ├── m.xml            # 管理功能模板
│       ├── database.xml     # 数据库相关模板
│       ├── ewa_restful.xml  # RESTful API 配置模板
│       └── ...
├── system.xml/              # 系统级配置目录
│   ├── EwaConfig.xml        # 主配置文件
│   ├── EwaDefine.xml        # 定义配置
│   ├── EwaFunctions.xml     # 函数定义
│   ├── EwaGlobal.xml        # 全局变量
│   ├── EwaSkin.xml          # 皮肤/主题配置
│   ├── EwaWorkflow.xml      # 工作流配置
│   └── database/TypesMap.xml # 数据库类型映射
├── ewa_conf_tmp.xml         # 配置文件模板
├── web-template.xml         # Web 应用部署模板
└── web-template.xml         # Web 应用配置模板
```

---

## 核心配置文件

### 1. ewa_conf_tmp.xml - 应用配置模板

**用途**: EWA 应用主配置文件模板，用于配置数据库、缓存、安全、路径等。

**主要配置节点**:

| 节点 | 说明 |
|------|------|
| `<admins>` | 管理员账户配置 |
| `<sqlCached>` | 缓存方式配置 (hsqldb/redis) |
| `<securities>` | 安全配置 (AES 加密) |
| `<requestValuesGlobal>` | 全局请求变量 |
| `<scriptPaths>` | 脚本路径配置 |
| `<addedResources>` | 附加资源 (JS/CSS) |
| `<paths>` | 系统路径配置 |
| `<debug>` | Debug IP 配置 |
| `<smtps>` | SMTP 邮件配置 |
| `<databases>` | 数据库连接池配置 |
| `<restfuls>` | RESTful API 配置 |
| `<remote_syncs>` | 远程同步配置 |

**示例**:
```xml
<ewa_confs>
    <!-- 缓存配置 -->
    <sqlCached cachedMethod="hsqldb"/>
    
    <!-- 安全配置 -->
    <securities>
        <security name="default" algorithm="aes-192-gcm" 
                  key="efsd91290123p9023sdkjvjdkl293048192"/>
    </securities>
    
    <!-- 数据库配置 -->
    <databases>
        <database name="work" type="MYSQL">
            <pool driverClassName="com.mysql.cj.jdbc.Driver" 
                  url="jdbc:mysql://localhost:3306/db"/>
        </database>
    </databases>
</ewa_confs>
```

---

### 2. web-template.xml - Web 应用部署模板

**用途**: Java Web 应用 `web.xml` 部署描述符模板。

**配置的 Servlet**:

| Servlet 名称 | 类 | URL 映射 | 用途 |
|-------------|-----|---------|------|
| EwaMain | `ServletMain` | `/ewa`, `/EWA_STYLE/cgi-bin/index.jsp` | 主入口 |
| EwaUpload | `ServletUpload` | `/EWA_STYLE/cgi-bin/_up_/index.jsp` | 文件上传 |
| EwaCode | `ServletCode` | `/EWA_STYLE/cgi-bin/_co_/index.jsp` | 验证码 |
| EwaResources | `ServletResources` | `/r.ewa`, `/r1.ewa` | 静态资源 |
| EwaStatus | `ServletStatus` | `/EWA_STYLE/cgi-bin/_st_/index.jsp` | 树状态 |
| EwaError | `ServletError` | `/EWA_STYLE/cgi-bin/_er_/index.jsp` | 错误处理 |
| EwaWorkflow | `ServletWorkflow` | `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | 工作流 |
| EwaDefineXml | `ServletXml` | `/EWA_DEFINE/cgi-bin/xml/index.jsp` | 定义 XML |
| EwaDefineIndex | `ServletIndex` | `/EWA_DEFINE/index.jsp` | 定义索引 |

---

### 3. system.xml/EwaConfig.xml - 系统核心配置

**用途**: 定义系统框架类型、页面元素 (XItem)、表单类型等。

**主要配置**:

#### Frames (表单类型)
- Frame - 输入修改界面
- ListFrame - 表格列表
- Grid - 网格显示
- Tree - 树形结构
- Menu - 菜单
- MultiGrid - 多维表格
- Logic - 逻辑控制
- Report - 报表
- Combine - 组合页面
- Complex - 复合页面

#### XItems (页面元素)
标准元素:
- `text` - 单行文本
- `textarea` - 多行文本
- `password` - 密码
- `date/datetime/time` - 日期时间
- `select` - 下拉选择
- `checkbox/radio` - 复选/单选
- `switch` - 开关
- `button/submit` - 按钮
- `anchor` - 链接
- `droplist` - 动态列表
- `combo` - 组合框

扩展元素:
- `dHtml5` - HTML5 编辑器
- `markDown` - Markdown 编辑器
- `h5upload` - HTML5 上传
- `file` - 文件上传 (已废弃)

---

### 4. system.xml/EwaFunctions.xml - 系统函数

**用途**: 定义系统内置函数，可在 XML 配置中调用。

| 函数名 | 类/方法 | 说明 |
|--------|--------|------|
| `password_hash` | `UArgon2.hashPwd` | Argon2 密码加密 |
| `password_verify` | `UArgon2.verifyPwd` | Argon2 密码验证 |
| `encrypt` | `UAes.defaultEncrypt` | AES 加密 |
| `decrypt` | `UAes.defaultDecrypt` | AES 解密 |
| `md5` | `Utils.md5` | MD5 哈希 |
| `sha1` | `Utils.sha1` | SHA1 哈希 |
| `digest` | `UDigest.digestHex` | 摘要算法 (支持多种) |
| `http_get` | `UNet.doGet` | HTTP GET 请求 |
| `snowflake` | `USnowflake.nextId` | Twitter 雪花 ID |
| `send_mail` | `UMail.sendHtmlMail` | 发送邮件 |

---

### 5. system.xml/EwaGlobal.xml - 全局变量

**用途**: 定义多语言全局变量、系统消息、日历格式等。

**配置内容**:
- 日历配置 (月份、周、日期格式)
- 货币符号
- 系统消息 (中英文)
- 验证错误提示
- 按钮文本 (确定/取消/是/否)
- 删除/更新提示信息

**示例**:
```xml
<Globals>
    <Global Lang='zhcn'>
        <Calendar>
            <Date>yyyy-MM-dd</Date>
            <Currency>￥</Currency>
        </Calendar>
    </Global>
    <Global Lang='enus'>
        <Calendar>
            <Date>MM/dd/yyyy</Date>
            <Currency>$</Currency>
        </Calendar>
    </Global>
</Globals>
```

---

### 6. system.xml/EwaSkin.xml - 皮肤主题

**用途**: 定义页面 HTML 模板和皮肤样式。

**支持的模板类型**:
- `Head` - HTML4 标准版
- `HeadXHtml` - XHTML 版
- `HeadH5` - HTML5 移动版
- `HeadVue` - Vue 集成版

**包含资源**:
- Font Awesome 4.7.0 图标库
- jQuery 3.6.0
- EWA 核心 JS/CSS
- Autosize 自动调整文本域

---

### 7. system.xml/database/TypesMap.xml - 数据库类型映射

**用途**: 定义不同数据库之间的数据类型映射规则。

**支持的数据库**:
- MySQL
- SQL Server (MSSQL)
- Oracle
- HSQLDB
- PostgreSQL

**映射示例**:
```xml
<Map Name="VARCHAR">
    <Database Name="MYSQL">
        <MapTo Name="VARCHAR"/>
        <MapTo Name="TINYTEXT"/>
    </Database>
    <Database Name="MSSQL">
        <MapTo Name="NVARCHAR" Scale="1"/>
        <MapTo Name="VARCHAR"/>
    </Database>
</Map>
```

---

## define.xml 配置

### 8. define.xml/ewa/ewa.xml - 系统模板

**用途**: 定义系统内置的配置模板，用于快速生成页面。

**包含的模板**:
- `frames_fields_list` - 配置项列表
- `frames_fields` - 配置项编辑
- `define_left` - 定义管理左框架
- `define_right` - 定义管理右框架
- `define_tree` - 定义管理树

---

### 9. define.xml/ewa/m.xml - 管理功能模板

**用途**: 系统管理功能的模板集合。

**包含的模板**:
- `ChangePWD` - 修改密码
- `admAdd` - 管理员新增
- `admlist` - 管理员列表
- `ewa_conf` - 配置文件管理
- `ewa_module` - 模块管理

---

### 10. define.xml/ewa/database.xml - 数据库模板

**用途**: 数据库相关操作的模板。

**包含的模板**:
- `cr_view` - 创建视图
- `tables_same` - 表一致性检查
- `table_field` - 表字段管理
- `db_connect` - 数据库连接

---

### 11. define.xml/ewa/ewa_restful.xml - RESTful API 模板

**用途**: RESTful API 配置和管理模板。

**包含的模板**:
- `ewa_restful.F.NM` - RESTful 配置新增/修改
- `ewa_restful.F.bat` - 批量生成 RESTful 配置
- `ewa_restful.LF.M` - RESTful 列表管理
- `ewa_restful_catalog.*` - 目录管理

**RESTful 配置示例**:
```xml
<restful path="chatUsers">
    <get xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M"/>
    <post xmlname="/ewa-apps/chat.xml" itemname="chat_user.F.NM"/>
    <delete xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M"/>
    <patch xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M"/>
</restful>
```

---

## 其他配置文件

### 12. define.xml/ewa/ewa_conf_help.xml
配置帮助系统的模板定义。

### 13. define.xml/ewa/ewa_module.xml
模块导入/导出功能模板。

### 14. define.xml/ewa/ewa_wf.xml
工作流相关模板。

### 15. define.xml/ewa/group1.xml
分组配置模板。

### 16. define.xml/ewa/tmp_img_resze.xml
图片缩放处理模板。

---

## system.xml 下的 EwaDefine 相关配置

### EwaDefineConfig.xml - 定义管理界面菜单

**用途**: 定义 EWA 配置管理界面 (EWA_DEFINE) 的顶部菜单结构。

**菜单位置**: 管理界面顶部导航栏

**菜单结构**:

| ID | 菜单名称 | 功能说明 |
|----|---------|---------|
| 10 | 保存 | 保存当前配置 |
| 20 | 调整信息 | 下拉菜单，包含：调整条目、调整菜单、调整信息、调整图表、调整工作流 |
| 50 | 组件 | 组件管理：创建发布、下载导入、组件交换中心 |
| 60 | 工具 | 开发工具：Restful API、数据库映射、缓存查看、流程设计等 |
| 94 | 用户 | 用户管理：用户列表、更改密码、BUG 跟踪 |
| 90 | 帮助 | 帮助文档、主题切换 (黑暗/光明)、关于 |
| 99 | 退出 | 退出系统 |

**菜单属性**:
- `mcmd`: 点击执行的 JavaScript 命令
- `micon`: 菜单图标 (Font Awesome 类名或图片路径)
- `mtext`: 中文显示文本
- `mtext_enus`: 英文显示文本
- `pid`: 父菜单 ID (0 表示顶级菜单)
- `mid`: 菜单唯一 ID

**示例**:
```xml
<Menu mcmd="saveXml();" micon="fa fa-save" mid="10" 
      mtext="保存" mtext_enus="Save" pid="0" />

<Menu micon="fa fa-plug" mid="60" 
      mtext="工具&lt;i class='fa fa-caret-down'>&lt;i>" 
      mtext_enus="Utils" pid="0" />

<Menu mcmd="window.open('./?XMLNAME=/ewa/ewa_restful.xml&amp;itemname=ewa_restful_catalog.T.Modify')"
      micon="fa fa-microchip" mid="600" 
      mtext="Restful API" mtext_enus="Restful API" pid="60" />
```

**Java 调用**:
```java
EwaDefineMenu menu = new EwaDefineMenu();
DTTable menus = menu.getMenus();
// 返回所有菜单项的 DataTable
```

---

### EwaDefine.xml - 定义生成器模板

**用途**: 定义配置生成器的步骤和模板，用于快速生成数据库表对应的 CRUD 配置。

**主要配置**:

#### 1. 生成步骤 (Steps)
```xml
<Steps>
    <Step AfterEval="this.CreateMainInfo();">
        <DescriptionSet><Set Info="第一步：从数据库树选择一个表"/></DescriptionSet>
    </Step>
    <Step Eval="this.SelectType()">
        <DescriptionSet><Set Info="第二步：选择类型"/></DescriptionSet>
    </Step>
    <Step Eval="this.ModifyMainInfo();">
        <DescriptionSet><Set Info="第三步：修改主要信息"/></DescriptionSet>
    </Step>
    <Step Eval="this.ModifyFields();" Using="Frame,ListFrame">
        <DescriptionSet><Set Info="第四步：选择字段"/></DescriptionSet>
    </Step>
    <Step Eval="this.ModifySQL();">
        <DescriptionSet><Set Info="第五步：修改 SQL 语句"/></DescriptionSet>
    </Step>
</Steps>
```

#### 2. Frame 模板 (Tmp)
为每种 Frame 类型定义预置模板：

```xml
<Frame Name="Frame">
    <!-- NM 模板：新增/修改 -->
    <Tmp Name="NM">
        <Button Name="butOk" Tag="submit"/>
        <Button Name="butClose" Tag="button"/>
        <Action Name="OnNew" SqlType="update"/>
        <Action Name="OnModify" SqlType="update"/>
    </Tmp>
    
    <!-- V 模板：查看 -->
    <Tmp Name="V">
        <Button Name="butClose" Tag="button"/>
        <Action Name="OnPageLoad" SqlType="query"/>
    </Tmp>
    
    <!-- Login 模板：登录 -->
    <Tmp Name="Login">
        <Button Name="_EWA_ValidCode" Tag="valid"/>
        <Button Name="butOk" Tag="submit"/>
    </Tmp>
</Frame>

<Frame Name="ListFrame">
    <!-- V 模板：浏览 -->
    <Tmp Name="V">
        <Action Name="OnPageLoad" SqlType="query"/>
    </Tmp>
    
    <!-- M 模板：可修改列表 -->
    <Tmp Name="M" ReleateFrame="Frame" ReleateTmp="NM">
        <Button Name="butNew" Tag="button"/>
        <Button Name="butModify" Tag="button"/>
        <Button Name="butDelete" Tag="button"/>
        <Action Name="OnFrameDelete" SqlType="update"/>
    </Tmp>
</Frame>
```

**使用场景**:
- 在 EWA_DEFINE 界面中，选择数据库表后自动生成配置
- 根据选择的 Frame 类型和模板类型，快速生成 CRUD 页面配置
- 减少手动配置工作量

---

## XML 配置命名约定

### 配置项命名规则
```
{表名}.{Frame 类型}.{模板类型}
```

**Frame 类型**:
- `F` - Frame (表单)
- `LF` - ListFrame (列表)
- `T` - Tree (树)
- `M` - Menu (菜单)
- `G` - Grid (网格)
- `MG` - MultiGrid (多维表格)

**模板类型**:
- `N` - 新增 (New)
- `M` - 修改 (Modify)
- `V` - 查看 (View)
- `NM` - 新增/修改 (New/Modify)
- `LF.M` - 列表可修改

**示例**:
- `users.F.NM` - 用户表单新增/修改
- `orders.LF.M` - 订单列表可修改
- `products.T.V` - 产品树查看

---

## 配置文件最佳实践

1. **ewa_conf.xml** - 项目主配置文件，从模板复制后修改
2. **数据库配置** - 使用连接池管理，配置最大连接数
3. **安全配置** - 使用 AES-192-GCM 或 AES-256-GCM 加密
4. **缓存配置** - 生产环境建议使用 Redis
5. **路径配置** - 使用绝对路径或 `@` 前缀的相对路径
6. **多语言** - 使用 `DescriptionSet` 节点定义中英文
