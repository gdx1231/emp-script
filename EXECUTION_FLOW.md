# EWA 程序执行 XML 文件流程分析

## 执行流程图

```
HTTP 请求
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. ServletMain.service()                                    │
│    - 接收 HTTP 请求 (doGet/doPost/doPut/doDelete/doPatch)    │
│    - 创建 EwaWebPage 实例                                    │
│    - 调用 EwaWebPage.run()                                   │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. EwaWebPage.runEwaScript()                                │
│    - 创建 HtmlCreator 实例                                   │
│    - 调用 HtmlCreator.createPageHtml()                       │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. HtmlCreator.initParameters(xmlName, itemName)            │
│    - 解析 XMLNAME 和 ITEMNAME 参数                             │
│    - 加载 UserConfig (XML 配置文件)                          │
│    - 初始化系统参数、权限、数据库连接等                      │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. HtmlCreator.createPageHtml()                             │
│    - 根据 FrameType 创建对应的 Frame 实例                       │
│    - 执行 Action (OnPageLoad/OnPagePost 等)                  │
│    - 渲染 HTML 页面                                            │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. 输出 HTML/JSON/二进制内容                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 详细执行步骤

### 步骤 1: ServletMain 接收请求

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletMain.java`

**web.xml 配置**:
```xml
<servlet>
    <servlet-name>EwaMain</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletMain</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>EwaMain</servlet-name>
    <url-pattern>/ewa</url-pattern>
    <url-pattern>/EWA_STYLE/cgi-bin/index.jsp</url-pattern>
</servlet-mapping>
```

**初始化 (initEwaInstances)**:
```java
public synchronized static void initEwaInstances() {
    EwaConfig.instance();           // 加载皮肤配置
    Skin.instance();                // 加载皮肤
    EwaGlobals.instance();          // 加载全局变量
    ConfSecurities.getInstance();   // 加载安全配置
    ConnectionConfigs.instance();   // 初始化数据库连接池
    SqlCachedHsqldbImpl.getInstance(); // 初始化 HSQL 缓存
    SqlCached.getInstance();        // 初始化缓存
}
```

**请求处理**:
```java
public void show(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    EwaWebPage p = new EwaWebPage(request, session, response);
    p.run();  // 执行页面处理
    
    // 输出 HTML 内容
    String cnt = p.getPageContent();
    this.outContent(request, response, cnt);
}
```

---

### 步骤 2: EwaWebPage 处理页面

**文件**: `src/main/java/com/gdxsoft/easyweb/EwaWebPage.java`

```java
public void run() {
    String runType = this._Rv.getString("EWA_RUN_TYPE");
    if (runType != null && runType.trim().equals("SC")) {
        // 状态控制
        this.runEwaStatusControl();
    } else {
        // 执行 EWA 脚本
        this.runEwaScript();
    }
}

private void runEwaScript() {
    _HtmlCreator.init(this._Rv, this._PageResponse);
    
    // 初始化权限
    if (_HtmlCreator.getAcl() == null) {
        IAcl acl = new SampleAcl();
        _HtmlCreator.setAcl(acl);
    }
    
    // 创建页面 HTML
    _HtmlCreator.createPageHtml();
    
    // 获取页面内容
    this._PageContent = _HtmlCreator.getPageHtml();
}
```

---

### 步骤 3: HtmlCreator 初始化参数

**文件**: `src/main/java/com/gdxsoft/easyweb/script/display/HtmlCreator.java`

#### 3.1 解析 XMLNAME 和 ITEMNAME

```java
private void initParameters(String xmlName, String itemName) throws Exception {
    // 验证参数
    if (xmlName == null || xmlName.trim().length() == 0) {
        throw new Exception("XMLNAME 未定义");
    }
    if (itemName == null || itemName.trim().length() == 0) {
        throw new Exception("ITEMNAME 未定义");
    }
    
    // 清理非法字符
    xmlName = xmlName.replace("#", "");
    itemName = itemName.replace("#", "");
    
    // 初始化系统参数
    this._SysParas.initParas(xmlName, itemName, _RequestValue);
    
    // 加载用户配置 (XML 文件)
    this._UserConfig = UserConfig.instance(xmlName, itemName, _DebugFrames);
    
    // 获取 Frame 类型 (ListFrame, Frame, Tree 等)
    String frameType = this.getPageItemValue("FrameTag", "FrameTag").toUpperCase().trim();
    this._SysParas.setFrameType(frameType);
    
    // 初始化权限
    this.initAcl();
    
    // 初始化数据库连接
    this.openSqlConnection();
    
    // 初始化操作类
    this.initClass();
    
    // 初始化数据库数据参数
    this.initDataParameters();
}
```

#### 3.2 加载 UserConfig

**文件**: `src/main/java/com/gdxsoft/easyweb/script/userConfig/UserConfig.java`

```java
public static UserConfig instance(String xmlName, String itemName, DebugFrames debugFrames) 
        throws Exception {
    // 1. 检查缓存
    String cacheKey = xmlName + "|" + itemName;
    UserConfig cached = CACHE.get(cacheKey);
    if (cached != null && !cached.isExpired()) {
        return cached;
    }
    
    // 2. 从数据库或文件加载 XML 配置
    String xmlContent = loadXmlContent(xmlName, itemName);
    
    // 3. 解析 XML
    UserConfig config = new UserConfig();
    config.parseXml(xmlContent);
    
    // 4. 缓存配置
    CACHE.put(cacheKey, config);
    
    return config;
}
```

**XML 配置结构**:
```xml
<EasyWebTemplate Name="users.F.NM">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
        <Name><Set Name="users.F.NM"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <DescriptionSet>
            <Set Info="用户管理" Lang="zhcn"/>
        </DescriptionSet>
    </Page>
    <Action>
        <ActionSet>
            <Set Type="OnPageLoad">
                <CallSet>
                    <Set CallName="loadUser" CallType="SqlSet"/>
                </CallSet>
            </Set>
        </ActionSet>
        <SqlSet>
            <Set Name="loadUser" SqlType="query">
                <Sql><![CDATA[SELECT * FROM users WHERE id=@id]]></Sql>
            </Set>
        </SqlSet>
    </Action>
    <XItems>
        <XItem Name="USER_NAME">
            <Tag><Set Tag="text"/></Tag>
            <DataItem><Set DataField="user_name"/></DataItem>
        </XItem>
    </XItems>
</EasyWebTemplate>
```

---

### 步骤 4: 创建页面 HTML

**文件**: `src/main/java/com/gdxsoft/easyweb/script/display/HtmlCreator.java`

```java
public void createPageHtml() throws Exception {
    // 1. 根据 FrameType 创建对应的 Frame 实例
    String frameType = this._SysParas.getFrameType();
    
    if (frameType.equals("LISTFRAME")) {
        this._Frame = new FrameList();
        this._Action = new ActionListFrame();
    } else if (frameType.equals("FRAME")) {
        this._Frame = new FrameFrame();
        this._Action = new ActionFrame();
    } else if (frameType.equals("TREE")) {
        this._Frame = new FrameTree();
        this._Action = new ActionTree();
    }
    // ... 其他 Frame 类型
    
    // 2. 初始化 Frame
    this._Frame.init(this);
    
    // 3. 执行 Action (如果有)
    String actionName = this._SysParas.getActionName();
    if (actionName != null && actionName.length() > 0) {
        this._ActionReturnValue = this._Action.execute(actionName);
    }
    
    // 4. 渲染 HTML
    this._PageHtml = this._Frame.createHtml();
}
```

#### 4.1 Frame 渲染流程

**文件**: `src/main/java/com/gdxsoft/easyweb/script/display/frame/FrameBase.java`

```java
public String createHtml() throws Exception {
    // 1. 加载皮肤模板
    SkinFrames skins = Skin.instance().getSkins();
    SkinFrame skin = skins.getSkinFrame(this._HtmlClass.getSysParas().getSkinName());
    
    // 2. 渲染 Page 元素
    this.renderPage(skin);
    
    // 3. 渲染 XItems
    this.renderXItems();
    
    // 4. 渲染 Actions
    this.renderActions();
    
    // 5. 组合最终 HTML
    return this.buildFinalHtml(skin);
}

private void renderXItems() throws Exception {
    UserXItems xItems = this._HtmlClass.getUserConfig().getUserXItems();
    
    for (int i = 0; i < xItems.count(); i++) {
        UserXItem xItem = xItems.getItem(i);
        
        // 获取 Tag 类型 (text, password, select 等)
        String tag = xItem.getSingleValue("Tag");
        
        // 创建对应的 Item 实例
        IItem item = ItemFactory.createItem(tag, xItem);
        
        // 渲染 Item
        String itemHtml = item.render();
        
        // 添加到页面
        this._HtmlClass.getDocument().addElement(itemHtml);
    }
}
```

#### 4.2 Item 渲染示例

**文件**: `src/main/java/com/gdxsoft/easyweb/script/display/items/ItemBase.java`

```java
public String render() throws Exception {
    // 1. 获取 EwaConfig.xml 中定义的 HTML 模板
    String template = this.getHtmlTemplate();
    
    // 2. 替换模板变量
    String value = this.getValue();  // 从数据库获取的值
    String name = this.getXItem().getName();
    
    template = template.replace("{__EWA_VAL__}", value);
    template = template.replace("@NAME", name);
    
    // 3. 替换属性
    String attributes = this.buildAttributes();
    template = template.replace("!!", attributes);
    
    return template;
}

// text 类型的 HTML 模板 (来自 EwaConfig.xml)
// <input type="text" class="EWA_INPUT" value="{__EWA_VAL__}" !!/>
```

---

### 步骤 5: Action 执行

**文件**: `src/main/java/com/gdxsoft/easyweb/script/display/action/ActionFrame.java`

```java
public String execute(String actionName) throws Exception {
    // 1. 获取 Action 定义
    UserXItem actionItem = this._HtmlClass.getUserConfig()
        .getUserActionItem(actionName);
    
    // 2. 获取执行类型 (SqlSet, XmlSet, ScriptSet, ClassSet)
    String callType = actionItem.getSingleValue("CallType");
    
    if ("SqlSet".equals(callType)) {
        // 执行 SQL
        return this.executeSql(actionName);
    } else if ("XmlSet".equals(callType)) {
        // 执行 XML 操作
        return this.executeXml(actionName);
    } else if ("ScriptSet".equals(callType)) {
        // 执行 JavaScript
        return this.executeScript(actionName);
    } else if ("ClassSet".equals(callType)) {
        // 执行 Java 方法
        return this.executeClass(actionName);
    }
    
    return "";
}

private String executeSql(String actionName) throws Exception {
    // 获取 SQL 语句
    String sql = this.getSql(actionName);
    
    // 执行 SQL
    DTTable table = DTTable.getJdbcTable(sql, this._RequestValue);
    
    // 将结果放入 PageValues
    this._HtmlClass.getItemValues().addTable(table);
    
    return "";
}
```

---

## 关键类关系图

```
┌─────────────────────────────────────────────────────────────┐
│                        ServletMain                          │
│  (web.xml 中配置的入口 Servlet)                               │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                        EwaWebPage                           │
│  - run()                                                    │
│  - runEwaScript()                                           │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                       HtmlCreator                           │
│  - initParameters(xmlName, itemName)                        │
│  - createPageHtml()                                         │
└──────────────┬──────────────────────┬───────────────────────┘
               │                      │
               ▼                      ▼
    ┌──────────────────┐    ┌──────────────────┐
    │    UserConfig    │    │    FrameBase     │
    │  (XML 配置解析)    │    │  (Frame 渲染)      │
    │                  │    │                  │
    │ - Page           │    │ - FrameFrame     │
    │ - Action         │    │ - FrameList      │
    │ - XItems         │    │ - FrameTree      │
    └──────────────────┘    └─────────┬────────┘
                                      │
                                      ▼
                            ┌──────────────────┐
                            │    ItemBase      │
                            │  (元素渲染)        │
                            │                  │
                            │ - text           │
                            │ - password       │
                            │ - select         │
                            │ - button         │
                            └──────────────────┘
```

---

## XML 配置执行示例

### 请求 URL
```
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N
```

### 执行的 XML 配置
```xml
<!-- define.xml/ewa/m.xml -->
<EasyWebTemplate Name="users.F.NM">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
    </Page>
    <Action>
        <ActionSet>
            <Set Type="OnPageLoad">
                <CallSet>
                    <Set CallName="loadUser" CallType="SqlSet" 
                         Test="'@EWA_MTYPE'='M'"/>
                </CallSet>
            </Set>
            <Set Type="OnPagePost">
                <CallSet>
                    <Set CallName="insertUser" CallType="SqlSet" 
                         Test="'@EWA_MTYPE'='N'"/>
                </CallSet>
            </Set>
        </ActionSet>
        <SqlSet>
            <Set Name="loadUser" SqlType="query">
                <Sql><![CDATA[SELECT * FROM users WHERE id=@id]]></Sql>
            </Set>
            <Set Name="insertUser" SqlType="update">
                <Sql><![CDATA[INSERT INTO users (name, email) 
                            VALUES (@name, @email)]]></Sql>
            </Set>
        </SqlSet>
    </Action>
    <XItems>
        <XItem Name="name">
            <Tag><Set Tag="text"/></Tag>
            <DataItem><Set DataField="name"/></DataItem>
        </XItem>
        <XItem Name="email">
            <Tag><Set Tag="text"/></Tag>
            <DataItem><Set DataField="email"/></DataItem>
        </XItem>
        <XItem Name="butOk">
            <Tag><Set Tag="submit"/></Tag>
        </XItem>
    </XItems>
</EasyWebTemplate>
```

### 执行流程

1. **GET 请求** (`EWA_MTYPE=N`):
   - 调用 `OnPageLoad` 动作
   - 条件 `'@EWA_MTYPE'='M'` 不满足，跳过 SQL 执行
   - 渲染空表单

2. **POST 请求** (提交表单):
   - 调用 `OnPagePost` 动作
   - 条件 `'@EWA_MTYPE'='N'` 满足
   - 执行 `insertUser` SQL
   - 返回成功消息

---

## 缓存机制

### UserConfig 缓存
```java
private static Map<String, UserConfig> CACHE = new ConcurrentHashMap<>();

public static UserConfig instance(String xmlName, String itemName) {
    String key = xmlName + "|" + itemName;
    UserConfig cached = CACHE.get(key);
    
    // 检查是否过期 (默认 5 秒)
    if (cached != null && !cached.isExpired()) {
        return cached;
    }
    
    // 重新加载
    UserConfig config = loadFromDatabaseOrFile(xmlName, itemName);
    CACHE.put(key, config);
    return config;
}
```

### SQL 缓存
```java
// ewa_conf.xml 配置
<sqlCached cachedMethod="hsqldb"/>
<!-- 或 -->
<sqlCached cachedMethod="redis" redisName="r0"/>
```

---

## 调试模式

### 启用 Debug
```
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_DEBUG_NO=1
```

### Debug 信息记录
```java
// DebugFrames 记录执行步骤
_DebugFrames.addDebug(this, "INIT", "开始读取用户模板");
this._UserConfig = UserConfig.instance(xmlName, itemName, _DebugFrames);
_DebugFrames.addDebug(this, "INIT", "结束读取用户模板");

// 记录到数据库
if (frameUnid != null && frameUnid.equals(debugKey)) {
    p.getDebugInfo().recordToHsql();
}
```

---

## 总结

EWA 框架执行 XML 文件的核心流程：

1. **ServletMain** 接收 HTTP 请求
2. **EwaWebPage** 创建执行环境
3. **HtmlCreator** 加载 XML 配置并初始化参数
4. **UserConfig** 解析 XML 配置 (Page/Action/XItems)
5. **Frame** 根据配置类型渲染页面
6. **Item** 根据 Tag 类型生成 HTML
7. **Action** 执行 SQL/Script/Java 代码
8. 输出最终 HTML/JSON/二进制内容

整个过程完全由 XML 配置驱动，无需编写 Java 代码即可实现 CRUD 功能。
