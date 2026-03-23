# EwaConfig.xml 与 define.xml/ewa 关系分析

## 核心关系概述

**EwaConfig.xml** 和 **define.xml/ewa/*.xml** 是 EWA 框架中两个不同层级的配置文件：

| 特性 | EwaConfig.xml | define.xml/ewa/*.xml |
|------|---------------|---------------------|
| **层级** | 系统级元配置 | 功能级模板配置 |
| **作用** | 定义框架类型和页面元素 | 具体的业务模板实例 |
| **根节点** | `<EasyWebConfig>` | `<EasyWebTemplates>` |
| **用途** | 组件定义 | 模板实例 |

---

## 1. EwaConfig.xml - 系统元配置

### 1.1 核心功能

`EwaConfig.xml` 定义 EWA 系统的**基础组件**：

```
EwaConfig.xml
├── <Frames>           # 定义 10 种表单类型
│   ├── Frame          # 输入修改界面
│   ├── ListFrame      # 表格列表
│   ├── Grid           # 网格显示
│   ├── Tree           # 树形结构
│   ├── Menu           # 菜单
│   ├── MultiGrid      # 多维表格
│   ├── Logic          # 逻辑控制
│   ├── Report         # 报表
│   ├── Combine        # 组合页面
│   └── Complex        # 复合页面
│
└── <Items>/<XItems>   # 定义页面元素 (40+ 种)
    ├── text           # 单行文本
    ├── textarea       # 多行文本
    ├── password       # 密码
    ├── date/datetime  # 日期时间
    ├── select         # 下拉选择
    ├── checkbox/radio # 复选/单选
    ├── button/submit  # 按钮
    ├── anchor         # 链接
    ├── dHtml5         # HTML5 编辑器
    └── ...
```

### 1.2 Frame 定义示例

```xml
<Frame Name="Frame" 
       FrameClassName="com.gdxsoft.easyweb.script.display.frame.FrameFrame"
       ActionClassName="com.gdxsoft.easyweb.script.display.action.ActionFrame">
    <DescriptionSet>
        <Set Lang="zhcn" Info="输入修改界面" />
        <Set Lang="enus" Info="Frame" />
    </DescriptionSet>
</Frame>
```

**说明**: 定义了 `Frame` 类型的 Java 实现类和多语言描述。

### 1.3 XItem 定义示例

```xml
<XItem Name="text" HtmlTag="input$text" BaseType="1" UIGroup="Standard"
       ClassName="com.gdxsoft.easyweb.script.display.items.ItemBase"
       Parameters="GroupIndex,Name,DescriptionSet,AttributeSet,...">
    <DescriptionSet>
        <Set Lang="zhcn" Info="单行文本" />
        <Set Lang="enus" Info="Textbox" />
    </DescriptionSet>
    <Template>
        <Html><![CDATA[<input type="text" class="EWA_INPUT" value="{__EWA_VAL__}" !!/>]]></Html>
    </Template>
</XItem>
```

**说明**: 定义了 `text` 元素的 HTML 模板、Java 类和可用参数。

---

## 2. define.xml/ewa/*.xml - 功能模板

### 2.1 文件列表及用途

| 文件 | 用途 | 模板数量 |
|------|------|---------|
| `ewa.xml` | 系统定义管理模板 | ~50 个 |
| `m.xml` | 系统管理功能模板 | ~20 个 |
| `database.xml` | 数据库相关模板 | ~30 个 |
| `ewa_restful.xml` | RESTful API 配置模板 | ~10 个 |
| `ewa_conf_help.xml` | 配置帮助模板 | ~10 个 |
| `ewa_module.xml` | 模块管理模板 | ~15 个 |
| `ewa_wf.xml` | 工作流模板 | ~10 个 |
| `app_cfg.xml` | APP 配置模板 | ~5 个 |
| `data_hor.xml` | 数据横向对比模板 | ~5 个 |
| `dev.xml` | 开发工具模板 | ~10 个 |
| `doc.xml` | 文档管理模板 | ~5 个 |
| `ewa_admin.xml` | 管理员管理模板 | ~5 个 |
| `group1.xml` | 分组配置模板 | ~5 个 |
| `m1.xml` | 扩展管理模板 | ~10 个 |
| `tmp_img_resze.xml` | 图片处理模板 | ~3 个 |

### 2.2 EasyWebTemplate 结构

每个模板文件包含多个 `<EasyWebTemplate>` 节点：

```xml
<EasyWebTemplates>
    <EasyWebTemplate Name="frames_fields_list" CreateDate="2011-05-05">
        <Page>
            <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
            <Name><Set Name="frames_fields_list"/></Name>
            <SkinName><Set SkinName="Test1"/></SkinName>
            <DescriptionSet>
                <Set Info="配置项列表" Lang="zhcn"/>
            </DescriptionSet>
            <Size><Set Width="700"/></Size>
            <AddHtml>...</AddHtml>
            <AddScript>...</AddScript>
        </Page>
        <Action>
            <ActionSet>...</ActionSet>
            <SqlSet>...</SqlSet>
        </Action>
        <XItems>
            <XItem Name="inc">
                <Tag><Set Tag="span"/></Tag>
                <DescriptionSet><Set Info="序号"/></DescriptionSet>
            </XItem>
            <!-- 更多字段 -->
        </XItems>
    </EasyWebTemplate>
    
    <EasyWebTemplate Name="ChangePWD">
        <!-- 另一个模板 -->
    </EasyWebTemplate>
</EasyWebTemplates>
```

---

## 3. 两层配置的关系

### 3.1 引用关系图

```
┌─────────────────────────────────────────────────────────────┐
│                    EwaConfig.xml (元配置)                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ <Frames>                                               │  │
│  │   - Frame (ClassName: FrameFrame)                     │  │
│  │   - ListFrame (ClassName: FrameList)                  │  │
│  │   - Tree (ClassName: FrameTree)                       │  │
│  │   - ...                                                │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ <XItems>                                               │  │
│  │   - text (HtmlTag: input$text)                        │  │
│  │   - select (HtmlTag: select)                          │  │
│  │   - button (HtmlTag: input$button)                    │  │
│  │   - ...                                                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 实例化使用
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              define.xml/ewa/*.xml (模板实例)                 │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ <EasyWebTemplate Name="ChangePWD">                    │  │
│  │   <Page>                                               │  │
│  │     <FrameTag><Set FrameTag="Frame"/></FrameTag>  ◄────┼── 使用元配置
│  │   </Page>                                              │  │
│  │   <XItems>                                             │  │
│  │     <XItem Name="ADM_LID">                            │  │
│  │       <Tag><Set Tag="span"/></Tag>              ◄─────┼── 使用元配置
│  │     </XItem>                                           │  │
│  │     <XItem Name="ADM_PWD">                            │  │
│  │       <Tag><Set Tag="password"/></Tag>          ◄─────┼── 使用元配置
│  │     </XItem>                                           │  │
│  │   </XItems>                                            │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 具体使用示例

#### 示例 1: Frame 类型引用

**EwaConfig.xml 定义**:
```xml
<Frame Name="ListFrame" 
       FrameClassName="com.gdxsoft.easyweb.script.display.frame.FrameList"/>
```

**ewa.xml 使用**:
```xml
<EasyWebTemplate Name="frames_fields_list">
    <Page>
        <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
        <!-- 使用 ListFrame 类型 -->
    </Page>
</EasyWebTemplate>
```

#### 示例 2: XItem 类型引用

**EwaConfig.xml 定义**:
```xml
<XItem Name="password" HtmlTag="input$password" BaseType="1"
       ClassName="com.gdxsoft.easyweb.script.display.items.ItemBase">
    <Template>
        <Html><![CDATA[<input type="password" class="EWA_INPUT" />]]></Html>
    </Template>
</XItem>
```

**m.xml 使用**:
```xml
<EasyWebTemplate Name="ChangePWD">
    <XItems>
        <XItem Name="ADM_PWD">
            <Tag><Set Tag="password"/></Tag>
            <!-- 渲染为密码输入框 -->
        </XItem>
    </XItems>
</EasyWebTemplate>
```

---

## 4. 配置继承与扩展

### 4.1 模板继承机制

某些模板会引用其他模板的配置：

```xml
<!-- m.xml 中的模板 -->
<EasyWebTemplate Name="admAdd">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
    </Page>
    <!-- 完整的 Page/Action/XItems 定义 -->
</EasyWebTemplate>

<!-- m.xml 中的另一个模板引用上面的模板 -->
<EasyWebTemplate Name="admlist">
    <Page>
        <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
    </Page>
    <!-- 可以引用 admAdd 的配置 -->
</EasyWebTemplate>
```

### 4.2 Tmp 模板定义

在 `EwaDefine.xml` 中定义了模板的快捷方式：

```xml
<Frame Name="Frame">
    <Tmp Name="NM">
        <DescriptionSet><Set Info="新建和修改数据"/></DescriptionSet>
        <Button Name="butOk" Tag="submit"/>
        <Button Name="butClose" Tag="button"/>
        <Action Name="OnNew" SqlType="update"/>
        <Action Name="OnModify" SqlType="update"/>
    </Tmp>
    <Tmp Name="V">
        <DescriptionSet><Set Info="查看页面"/></DescriptionSet>
        <Button Name="butClose" Tag="button"/>
    </Tmp>
</Frame>
```

**使用方式**:
- `users.F.NM` - 使用 Frame 的 NM 模板
- `users.F.V` - 使用 Frame 的 V 模板
- `orders.LF.M` - 使用 ListFrame 的 M 模板

---

## 5. 运行时处理流程

### 5.1 配置加载顺序

```
1. 系统启动
   │
   ├─► 2. 加载 EwaConfig.xml
   │    └─► 注册 Frames 和 XItems 定义
   │
   ├─► 3. 加载 define.xml/ewa/*.xml
   │    └─► 注册 EasyWebTemplate 模板
   │
   └─► 4. 加载用户自定义配置
        └─► 从数据库或文件加载业务配置
```

### 5.2 页面渲染流程

```
用户请求：/ewa?XMLNAME=users&ITEMNAME=F.NM
   │
   ▼
1. 解析配置路径
   - XMLNAME = users (配置文件)
   - ITEMNAME = F.NM (Frame 的 NM 模板)
   │
   ▼
2. 查找模板
   - 从 EwaConfig.xml 获取 Frame 定义
   - 从 EwaDefine.xml 获取 NM 模板结构
   │
   ▼
3. 渲染页面
   - 根据 FrameTag 选择 FrameFrame 类
   - 根据 XItem 的 Tag 选择对应的 HTML 模板
   - 填充数据
   │
   ▼
4. 输出 HTML
```

---

## 6. 配置命名规范

### 6.1 配置项命名规则

```
{表名}.{Frame 类型}.{模板类型}

示例:
- users.F.NM        # 用户表 - 表单 - 新增/修改
- orders.LF.M       # 订单表 - 列表 - 可修改
- products.T.V      # 产品表 - 树 - 查看
```

### 6.2 Frame 类型缩写

| 缩写 | 全称 | 对应 EwaConfig 中的 Name |
|------|------|-------------------------|
| F | Frame | `Frame` |
| LF | ListFrame | `ListFrame` |
| T | Tree | `Tree` |
| M | Menu | `Menu` |
| G | Grid | `Grid` |
| MG | MultiGrid | `MultiGrid` |

### 6.3 模板类型缩写

| 缩写 | 含义 | 用途 |
|------|------|------|
| N | New | 新增 |
| M | Modify | 修改 |
| V | View | 查看 |
| NM | New/Modify | 新增/修改共用 |
| LF.M | ListFrame Modify | 列表可修改 |

---

## 7. 配置文件对比总结

| 对比项 | EwaConfig.xml | define.xml/ewa/*.xml |
|--------|---------------|---------------------|
| **配置层级** | 元配置 (Meta Config) | 实例配置 (Instance Config) |
| **根节点** | `<EasyWebConfig>` | `<EasyWebTemplates>` |
| **定义内容** | Frames, XItems | EasyWebTemplate |
| **Java 类映射** | 是 (ClassName) | 否 (使用元配置的定义) |
| **HTML 模板** | 是 (Template) | 是 (完整页面结构) |
| **多语言支持** | 是 (DescriptionSet) | 是 (DescriptionSet) |
| **可继承性** | 否 (基础定义) | 是 (可引用其他模板) |
| **运行时修改** | 否 (需重启) | 是 (可热更新) |
| **数量** | ~10 Frames, ~40 XItems | ~200+ EasyWebTemplates |

---

## 8. 实际开发建议

### 8.1 何时修改 EwaConfig.xml

- 需要添加新的页面元素类型
- 需要修改现有元素的 HTML 渲染模板
- 需要添加新的 Frame 类型
- 需要修改现有 Frame 的 Java 实现类

### 8.2 何时修改 define.xml/ewa/*.xml

- 需要添加新的业务模板
- 需要修改现有模板的布局
- 需要添加新的管理功能
- 需要调整 RESTful API 配置

### 8.3 何时创建用户自定义配置

- 业务表对应的 CRUD 页面
- 特定业务流程的表单
- 自定义报表和统计页面
