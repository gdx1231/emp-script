# EWA Program Execution XML File Flow Analysis

## Execution Flow Diagram

```
HTTP Request
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. ServletMain.service()                                    │
│    - Receives HTTP request (doGet/doPost/doPut/doDelete/doPatch) │
│    - Creates EwaWebPage instance                            │
│    - Calls EwaWebPage.run()                                 │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. EwaWebPage.runEwaScript()                                │
│    - Creates HtmlCreator instance                           │
│    - Calls HtmlCreator.createPageHtml()                     │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. HtmlCreator.initParameters(xmlName, itemName)            │
│    - Parses XMLNAME and ITEMNAME parameters                 │
│    - Loads UserConfig (XML config file)                     │
│    - Initializes system params, permissions, DB connections │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. HtmlCreator.createPageHtml()                             │
│    - Creates corresponding Frame instance based on FrameType │
│    - Executes Action (OnPageLoad/OnPagePost, etc.)          │
│    - Renders HTML page                                      │
└─────────────────────────────────────────────────────────────┘
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. Output HTML/JSON/Binary Content                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Detailed Execution Steps

### Step 1: ServletMain Receives Request

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletMain.java`

**web.xml Configuration**:
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

**Initialization (initEwaInstances)**:
```java
public synchronized static void initEwaInstances() {
    EwaConfig.instance();           // Load skin configuration
    Skin.instance();                // Load skin
    EwaGlobals.instance();          // Load global variables
    ConfSecurities.getInstance();   // Load security configuration
    ConnectionConfigs.instance();   // Initialize DB connection pool
    SqlCachedHsqldbImpl.getInstance(); // Initialize HSQL cache
    SqlCached.getInstance();        // Initialize cache
}
```

**Request Handling**:
```java
public void show(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    EwaWebPage p = new EwaWebPage(request, session, response);
    p.run();  // Execute page processing
    
    // Output HTML content
    String cnt = p.getPageContent();
    this.outContent(request, response, cnt);
}
```

---

### Step 2: EwaWebPage Processes the Page

**File**: `src/main/java/com/gdxsoft/easyweb/EwaWebPage.java`

```java
public void run() {
    String runType = this._Rv.getString("EWA_RUN_TYPE");
    if (runType != null && runType.trim().equals("SC")) {
        // Status control
        this.runEwaStatusControl();
    } else {
        // Execute EWA script
        this.runEwaScript();
    }
}

private void runEwaScript() {
    _HtmlCreator.init(this._Rv, this._PageResponse);
    
    // Initialize permissions
    if (_HtmlCreator.getAcl() == null) {
        IAcl acl = new SampleAcl();
        _HtmlCreator.setAcl(acl);
    }
    
    // Create page HTML
    _HtmlCreator.createPageHtml();
    
    // Get page content
    this._PageContent = _HtmlCreator.getPageHtml();
}
```

---

### Step 3: HtmlCreator Initializes Parameters

**File**: `src/main/java/com/gdxsoft/easyweb/script/display/HtmlCreator.java`

#### 3.1 Parse XMLNAME and ITEMNAME

```java
private void initParameters(String xmlName, String itemName) throws Exception {
    // Validate parameters
    if (xmlName == null || xmlName.trim().length() == 0) {
        throw new Exception("XMLNAME is not defined");
    }
    if (itemName == null || itemName.trim().length() == 0) {
        throw new Exception("ITEMNAME is not defined");
    }
    
    // Clean illegal characters
    xmlName = xmlName.replace("#", "");
    itemName = itemName.replace("#", "");
    
    // Initialize system parameters
    this._SysParas.initParas(xmlName, itemName, _RequestValue);
    
    // Load user configuration (XML file)
    this._UserConfig = UserConfig.instance(xmlName, itemName, _DebugFrames);
    
    // Get Frame type (ListFrame, Frame, Tree, etc.)
    String frameType = this.getPageItemValue("FrameTag", "FrameTag").toUpperCase().trim();
    this._SysParas.setFrameType(frameType);
    
    // Initialize permissions
    this.initAcl();
    
    // Initialize database connection
    this.openSqlConnection();
    
    // Initialize operation class
    this.initClass();
    
    // Initialize database data parameters
    this.initDataParameters();
}
```

#### 3.2 Load UserConfig

**File**: `src/main/java/com/gdxsoft/easyweb/script/userConfig/UserConfig.java`

```java
public static UserConfig instance(String xmlName, String itemName, DebugFrames debugFrames) 
        throws Exception {
    // 1. Check cache
    String cacheKey = xmlName + "|" + itemName;
    UserConfig cached = CACHE.get(cacheKey);
    if (cached != null && !cached.isExpired()) {
        return cached;
    }
    
    // 2. Load XML configuration from database or file
    String xmlContent = loadXmlContent(xmlName, itemName);
    
    // 3. Parse XML
    UserConfig config = new UserConfig();
    config.parseXml(xmlContent);
    
    // 4. Cache configuration
    CACHE.put(cacheKey, config);
    
    return config;
}
```

**XML Configuration Structure**:
```xml
<EasyWebTemplate Name="users.F.NM">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
        <Name><Set Name="users.F.NM"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <DescriptionSet>
            <Set Info="User management" Lang="zhcn"/>
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

### Step 4: Create Page HTML

**File**: `src/main/java/com/gdxsoft/easyweb/script/display/HtmlCreator.java`

```java
public void createPageHtml() throws Exception {
    // 1. Create corresponding Frame instance based on FrameType
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
    // ... Other Frame types
    
    // 2. Initialize Frame
    this._Frame.init(this);
    
    // 3. Execute Action (if any)
    String actionName = this._SysParas.getActionName();
    if (actionName != null && actionName.length() > 0) {
        this._ActionReturnValue = this._Action.execute(actionName);
    }
    
    // 4. Render HTML
    this._PageHtml = this._Frame.createHtml();
}
```

#### 4.1 Frame Rendering Flow

**File**: `src/main/java/com/gdxsoft/easyweb/script/display/frame/FrameBase.java`

```java
public String createHtml() throws Exception {
    // 1. Load skin template
    SkinFrames skins = Skin.instance().getSkins();
    SkinFrame skin = skins.getSkinFrame(this._HtmlClass.getSysParas().getSkinName());
    
    // 2. Render Page elements
    this.renderPage(skin);
    
    // 3. Render XItems
    this.renderXItems();
    
    // 4. Render Actions
    this.renderActions();
    
    // 5. Assemble final HTML
    return this.buildFinalHtml(skin);
}

private void renderXItems() throws Exception {
    UserXItems xItems = this._HtmlClass.getUserConfig().getUserXItems();
    
    for (int i = 0; i < xItems.count(); i++) {
        UserXItem xItem = xItems.getItem(i);
        
        // Get Tag type (text, password, select, etc.)
        String tag = xItem.getSingleValue("Tag");
        
        // Create corresponding Item instance
        IItem item = ItemFactory.createItem(tag, xItem);
        
        // Render Item
        String itemHtml = item.render();
        
        // Add to page
        this._HtmlClass.getDocument().addElement(itemHtml);
    }
}
```

#### 4.2 Item Rendering Example

**File**: `src/main/java/com/gdxsoft/easyweb/script/display/items/ItemBase.java`

```java
public String render() throws Exception {
    // 1. Get HTML template defined in EwaConfig.xml
    String template = this.getHtmlTemplate();
    
    // 2. Replace template variables
    String value = this.getValue();  // Value from database
    String name = this.getXItem().getName();
    
    template = template.replace("{__EWA_VAL__}", value);
    template = template.replace("@NAME", name);
    
    // 3. Replace attributes
    String attributes = this.buildAttributes();
    template = template.replace("!!", attributes);
    
    return template;
}

// HTML template for text type (from EwaConfig.xml)
// <input type="text" class="EWA_INPUT" value="{__EWA_VAL__}" !!/>
```

---

### Step 5: Action Execution

**File**: `src/main/java/com/gdxsoft/easyweb/script/display/action/ActionFrame.java`

```java
public String execute(String actionName) throws Exception {
    // 1. Get Action definition
    UserXItem actionItem = this._HtmlClass.getUserConfig()
        .getUserActionItem(actionName);
    
    // 2. Get execution type (SqlSet, XmlSet, ScriptSet, ClassSet)
    String callType = actionItem.getSingleValue("CallType");
    
    if ("SqlSet".equals(callType)) {
        // Execute SQL
        return this.executeSql(actionName);
    } else if ("XmlSet".equals(callType)) {
        // Execute XML operation
        return this.executeXml(actionName);
    } else if ("ScriptSet".equals(callType)) {
        // Execute JavaScript
        return this.executeScript(actionName);
    } else if ("ClassSet".equals(callType)) {
        // Execute Java method
        return this.executeClass(actionName);
    }
    
    return "";
}

private String executeSql(String actionName) throws Exception {
    // Get SQL statement
    String sql = this.getSql(actionName);
    
    // Execute SQL
    DTTable table = DTTable.getJdbcTable(sql, this._RequestValue);
    
    // Put results into PageValues
    this._HtmlClass.getItemValues().addTable(table);
    
    return "";
}
```

---

## Key Class Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        ServletMain                          │
│  (Entry Servlet configured in web.xml)                      │
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
    │  (XML Config     │    │  (Frame Render)  │
    │   Parser)        │    │                  │
    │                  │    │ - FrameFrame     │
    │ - Page           │    │ - FrameList      │
    │ - Action         │    │ - FrameTree      │
    │ - XItems         │    └─────────┬────────┘
    └──────────────────┘              │
                                      ▼
                            ┌──────────────────┐
                            │    ItemBase      │
                            │  (Item Render)   │
                            │                  │
                            │ - text           │
                            │ - password       │
                            │ - select         │
                            │ - button         │
                            └──────────────────┘
```

---

## XML Configuration Execution Example

### Request URL
```
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N
```

### Executed XML Configuration
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

### Execution Flow

1. **GET Request** (`EWA_MTYPE=N`):
   - Calls `OnPageLoad` action
   - Condition `'@EWA_MTYPE'='M'` is not met, skips SQL execution
   - Renders empty form

2. **POST Request** (form submission):
   - Calls `OnPagePost` action
   - Condition `'@EWA_MTYPE'='N'` is met
   - Executes `insertUser` SQL
   - Returns success message

---

## Caching Mechanism

### UserConfig Cache
```java
private static Map<String, UserConfig> CACHE = new ConcurrentHashMap<>();

public static UserConfig instance(String xmlName, String itemName) {
    String key = xmlName + "|" + itemName;
    UserConfig cached = CACHE.get(key);
    
    // Check if expired (default 5 seconds)
    if (cached != null && !cached.isExpired()) {
        return cached;
    }
    
    // Reload
    UserConfig config = loadFromDatabaseOrFile(xmlName, itemName);
    CACHE.put(key, config);
    return config;
}
```

### SQL Cache
```java
// ewa_conf.xml configuration
<sqlCached cachedMethod="hsqldb"/>
<!-- or -->
<sqlCached cachedMethod="redis" redisName="r0"/>
```

---

## Debug Mode

### Enable Debug
```
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_DEBUG_NO=1
```

### Debug Information Logging
```java
// DebugFrames records execution steps
_DebugFrames.addDebug(this, "INIT", "Start reading user template");
this._UserConfig = UserConfig.instance(xmlName, itemName, _DebugFrames);
_DebugFrames.addDebug(this, "INIT", "Finish reading user template");

// Record to database
if (frameUnid != null && frameUnid.equals(debugKey)) {
    p.getDebugInfo().recordToHsql();
}
```

---

## Summary

Core flow of EWA framework XML file execution:

1. **ServletMain** receives the HTTP request
2. **EwaWebPage** creates the execution environment
3. **HtmlCreator** loads XML configuration and initializes parameters
4. **UserConfig** parses XML configuration (Page/Action/XItems)
5. **Frame** renders the page based on the configuration type
6. **Item** generates HTML based on the Tag type
7. **Action** executes SQL/Script/Java code
8. Output the final HTML/JSON/binary content

The entire process is fully driven by XML configuration, enabling CRUD functionality without writing any Java code.
