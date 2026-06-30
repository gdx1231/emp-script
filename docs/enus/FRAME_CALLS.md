# EWA Frame Call Reference

## Overview

The EWA framework provides multiple Frame types for different page display needs:

| Frame Type | Class Name | Purpose |
|-----------|------|------|
| **Frame** | `FrameFrame` | Form input/modification interface |
| **ListFrame** | `FrameList` | Data list/table |
| **Tree** | `FrameTree` | Tree structure |
| **Grid** | `FrameGrid` | Grid display |
| **Menu** | `FrameMenu` | Menu |
| **MultiGrid** | `FrameMultiGrid` | Multi-dimensional table |
| **Logic** | `FrameLogic` | Logic control |
| **Report** | `FrameReport` | Report |
| **Combine** | `FrameCombine` | Combined page |
| **Complex** | `FrameComplex` | Complex page |

---

## 1. Frame Inheritance Structure

```
IFrame (Interface)
  ▲
  │
FrameBase (Base Class)
  │
  ├─► FrameFrame      (Form)
  ├─► FrameList       (List)
  ├─► FrameTree       (Tree)
  ├─► FrameGrid       (Grid)
  ├─► FrameMenu       (Menu)
  ├─► FrameMultiGrid  (Multi-Dimensional Table)
  ├─► FrameLogic      (Logic)
  ├─► FrameReport     (Report)
  ├─► FrameCombine    (Combine)
  └─► FrameComplex    (Complex)
```

---

## 2. Call Flow

### 2.1 Complete Call Chain

```
HTTP Request
   │
   ▼
ServletMain.doGet/doPost()
   │
   ▼
EwaWebPage.run()
   │
   ▼
HtmlCreator.createPageHtml()
   │
   ├─► 1. Load UserConfig (XML Configuration)
   │     └─► Read FrameTag (FrameType)
   │
   ├─► 2. Create Frame Instance
   │     ├─► LISTFRAME → new FrameList()
   │     ├─► FRAME → new FrameFrame()
   │     ├─► TREE → new FrameTree()
   │     └─► ...
   │
   ├─► 3. Frame.init(HtmlCreator)
   │
   ├─► 4. Execute Action (OnPageLoad/OnPagePost)
   │
   └─► 5. Frame.createHtml()
         │
         ├─► createSkinTop()      - Skin header
         ├─► createCss()          - CSS styles
         ├─► createJsTop()        - Header JS
         ├─► createContent()      - Main content
         ├─► createJsFramePage()  - Frame initialization script
         ├─► createSkinBottom()   - Skin footer
         └─► createJsBottom()     - Footer JS
```

### 2.2 Code Example

```java
// HtmlCreator.createPageHtml()
public void createPageHtml() throws Exception {
    // 1. Get Frame type
    String frameType = this._SysParas.getFrameType();  // "LISTFRAME", "FRAME", "TREE"...
    
    // 2. Create corresponding Frame instance
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
    // ... other types
    
    // 3. Initialize Frame
    this._Frame.init(this);
    
    // 4. Execute Action
    String actionName = this._SysParas.getActionName();
    if (actionName != null && actionName.length() > 0) {
        this._ActionReturnValue = this._Action.execute(actionName);
    }
    
    // 5. Generate HTML
    this._PageHtml = this._Frame.createHtml();
}
```

---

## 3. ListFrame Details

### 3.1 Configuration Example

```xml
<EasyWebTemplate Name="users.LF.M">
    <Page>
        <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
        <Name><Set Name="users.LF.M"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <PageSize><Set PageSize="20" IsSplitPage="1" KeyField="USER_ID"/></PageSize>
        <ListUI>
            <Set luButtons="1" luSearch="1" luSelect="S" luDblClick="1"/>
        </ListUI>
    </Page>
    <Action>
        <ActionSet>
            <Set Type="OnPageLoad">
                <CallSet>
                    <Set CallName="loadUsers" CallType="SqlSet"/>
                </CallSet>
            </Set>
        </ActionSet>
        <SqlSet>
            <Set Name="loadUsers" SqlType="query">
                <Sql><![CDATA[
                    SELECT * FROM users 
                    WHERE status='ACTIVE'
                    ORDER BY create_date DESC
                ]]></Sql>
            </Set>
        </SqlSet>
    </Action>
    <XItems>
        <XItem Name="USER_ID">
            <Tag><Set Tag="checkboxgrid"/></Tag>
            <DataItem><Set DataField="user_id"/></DataItem>
        </XItem>
        <XItem Name="USER_NAME">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="user_name"/></DataItem>
        </XItem>
        <XItem Name="EMAIL">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="email"/></DataItem>
        </XItem>
        <XItem Name="CREATE_DATE">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="create_date" Format="yyyy-MM-dd"/></DataItem>
        </XItem>
        <XItem Name="butNew">
            <Tag><Set Tag="button"/></Tag>
            <EventSet>
                <Set EventName="onclick" EventType="Javascript"
                     EventValue="EWA.F.FOS['@sys_frame_unid'].ext_NewOrModifyOrCopy('N')"/>
            </EventSet>
        </XItem>
    </XItems>
</EasyWebTemplate>
```

### 3.2 createHtml Flow

```java
// FrameList.createHtml()
public void createHtml() throws Exception {
    if (super.getHtmlClass().getSysParas().isVue()) {
        super.createHtmlVue();  // Vue mode
    } else {
        this.createHtmlTraditional();  // Traditional mode
    }
}

public void createHtmlTraditional() {
    // 1. Skin header
    super.createSkinTop();
    
    // 2. CSS styles
    super.createCss();
    
    // 3. Header JS
    super.createJsTop();
    
    // 4. Main content (table)
    this.createContent();
    
    // 5. Frame initialization script
    this.createJsFramePage();
    
    // 6. Skin footer
    super.createSkinBottom();
    
    // 7. Footer JS
    super.createJsBottom();
}
```

### 3.3 Creating Table Content

```java
// FrameList.createContent()
public void createContent() {
    MStr sb = new MStr();
    
    // 1. Get data table
    DTTable tb = this.getSplitPageTable();
    
    // 2. Create table header
    sb.append("<table id='EWA_LF_" + gunid + "' class='EWA_LISTFRAME'>");
    sb.append("<thead><tr>");
    
    // 3. Iterate XItems to create headers
    for (int i = 0; i < xItems.count(); i++) {
        UserXItem xItem = xItems.getItem(i);
        if (this.isHiddenField(xItem.getName())) {
            continue;  // Skip hidden fields
        }
        String des = xItem.getDescription();
        sb.append("<th>" + des + "</th>");
    }
    sb.append("</tr></thead>");
    
    // 4. Create table data rows
    sb.append("<tbody>");
    for (int i = 0; i < tb.getCount(); i++) {
        DTRow row = tb.getRow(i);
        sb.append("<tr id='R_" + i + "'>");
        
        // Iterate XItems to create cells
        for (int j = 0; j < xItems.count(); j++) {
            UserXItem xItem = xItems.getItem(j);
            IItem item = ItemFactory.createItem(xItem, row);
            String html = item.render();
            sb.append("<td>" + html + "</td>");
        }
        sb.append("</tr>");
    }
    sb.append("</tbody></table>");
    
    // 5. Create pagination bar
    this.createPageBar(sb);
    
    // 6. Create button bar
    this.createButtonBar(sb);
    
    // 7. Create search box
    this.createSearchBox(sb);
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}
```

### 3.4 Generating Frame Initialization Script

```java
// FrameList.createJsFramePage()
public void createJsFramePage() throws Exception {
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    String url = super.getUrlJs();
    
    MStr sJs = new MStr();
    sJs.al("EWA.LANG='" + lang.toLowerCase() + "';");
    sJs.al("(function() {");
    sJs.al(" var o1 = EWA.F.FOS['" + gunid + "'] = new EWA_ListFrameClass();");
    sJs.al(" o1._Id = o1.Id = '" + gunid + "';");
    sJs.al(" o1.Title = \"" + pageDescription + "\";");
    sJs.al(" o1.Init(EWA_ITEMS_XML_" + gunid + ");");
    
    // Pagination parameters
    PageSplit ps = this._PageSplit;
    sJs.al(" o1.SetPageParameters(" + ps.getPageCurrent() + "," 
           + ps.getPageCount() + "," + ps.getPageSize() + "," 
           + ps.getRecordCount() + ");");
    
    // Sorting
    sJs.al(" o1.UserSort = '" + userSort + "';");
    
    // Button configuration
    sJs.al(" o1.LuButtons = " + this._IsLuButtons + ";");
    sJs.al(" o1.LuSearch = " + this._IsLuSearch + ";");
    sJs.al(" o1.LuSelect = '" + this._LuSelect + "';");
    sJs.al(" o1.LuDblClick = " + this._IsLuDblClick + ";");
    
    sJs.al("})();");
    
    this.getHtmlClass().getDocument().addJs("LISTFRAME", sJs.toString(), false);
}
```

### 3.5 Common ListFrame Parameters

```java
// URL Parameters
EWA_PAGECUR=2              // Current page
EWA_PAGESIZE=50            // Records per page
EWA_LF_ORDER=create_date DESC  // Sort order
EWA_SEARCH=name[lk]Smith   // Search condition
EWA_BOX=1                  // BOX mode
EWA_IS_SPLIT_PAGE=no       // No pagination
EWA_LU_STICKY_HEADERS=yes  // Sticky headers
EWA_LU_BUTTONS=NO          // Hide buttons
EWA_LU_SEARCH=NO           // Hide search
EWA_LU_SELECT=S            // Single selection
EWA_LU_DBLCLICK=yes        // Double-click row
```

---

## 4. FrameFrame Details

### 4.1 Configuration Example

```xml
<EasyWebTemplate Name="users.F.NM">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
        <Name><Set Name="users.F.NM"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <Size><Set Width="700" Height="500"/></Size>
        <FrameHtml>
            <Set FrameCols="C2"/>  <!-- C1=1 segment, C2=2 segments, C11=stacked -->
        </FrameHtml>
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
                    <Set CallName="saveUser" CallType="SqlSet" 
                         Test="'@EWA_MTYPE'='N' or '@EWA_MTYPE'='M'"/>
                </CallSet>
            </Set>
        </ActionSet>
        <SqlSet>
            <Set Name="loadUser" SqlType="query">
                <Sql><![CDATA[
                    SELECT * FROM users WHERE user_id=@user_id
                ]]></Sql>
            </Set>
            <Set Name="saveUser" SqlType="update">
                <Sql><![CDATA[
                    INSERT INTO users (name, email) VALUES (@name, @email)
                ]]></Sql>
            </Set>
        </SqlSet>
    </Action>
    <XItems>
        <XItem Name="USER_NAME">
            <Tag><Set Tag="text"/></Tag>
            <DataItem><Set DataField="user_name"/></DataItem>
            <IsMustInput><Set IsMustInput="1"/></IsMustInput>
        </XItem>
        <XItem Name="EMAIL">
            <Tag><Set Tag="text"/></Tag>
            <DataItem><Set DataField="email"/></DataItem>
        </XItem>
        <XItem Name="butOk">
            <Tag><Set Tag="submit"/></Tag>
        </XItem>
        <XItem Name="butClose">
            <Tag><Set Tag="button"/></Tag>
        </XItem>
    </XItems>
</EasyWebTemplate>
```

### 4.2 createHtml Flow

```java
// FrameFrame.createHtml()
public void createHtml() throws Exception {
    if (super.getHtmlClass().getSysParas().isVue()) {
        super.createHtmlVue();
    } else {
        this.createHtmlTraditional();
    }
}

public void createHtmlTraditional() throws Exception {
    // 1. Skin header
    super.createSkinTop();
    
    // 2. CSS
    super.createCss();
    
    // 3. Header JS
    super.createJsTop();
    
    // 4. Form content
    this.createContent();
    
    // 5. Frame script
    this.createJsFramePage();
    
    // 6. Skin footer
    super.createSkinBottom();
    
    // 7. Footer JS
    super.createJsBottom();
}
```

### 4.3 Creating Form Content

```java
// FrameFrame.createContent()
public void createContent() throws Exception {
    MStr sb = new MStr();
    
    // 1. Get data
    DTTable tb = this.getHtmlClass().getItemValues().getLastTable();
    
    // 2. Create form framework
    sb.append("<form id='f_" + gunid + "' method='post'>");
    sb.append("<input type='hidden' name='EWA_POST' value='1'/>");
    
    // 3. Grouped rendering
    if (this._IsGroup) {
        // Grouped display
        for (int i = 0; i < _GroupInfos.length; i++) {
            String grp = _GroupInfos[i];
            sb.append("<fieldset><legend>" + grp + "</legend>");
            this.renderGroup(grp, tb, sb);
            sb.append("</fieldset>");
        }
    } else {
        // No grouping display
        this.renderAll(tb, sb);
    }
    
    // 4. Create buttons
    this.renderButtons(sb);
    
    sb.append("</form>");
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}

// Render a single field
private void renderField(UserXItem xItem, DTTable tb, MStr sb) {
    // Skip hidden fields
    if (this.isHiddenField(xItem.getName())) {
        return;
    }
    
    // Get field value
    String value = "";
    if (tb != null && tb.getCount() > 0) {
        value = tb.getCell(0, xItem.getDataField()).toString();
    }
    
    // Create Item instance
    IItem item = ItemFactory.createItem(xItem, value);
    String html = item.render();
    
    // Add to form
    sb.append("<div class='EWA_TR'>");
    sb.append("<td class='EWA_TD_M'>" + xItem.getDescription() + "</td>");
    sb.append("<td class='EWA_TD_M'>" + html + "</td>");
    sb.append("</div>");
}
```

### 4.4 FrameFrame Parameters

```java
// URL Parameters
EWA_FRAME_COLS=C2      // C1=1 segment, C2=2 segments, C11=stacked
EWA_WIDTH=800px        // Width
EWA_HEIGHT=600px       // Height
EWA_MTYPE=N            // N=New, M=Modify, C=Copy
EWA_IS_HIDDEN_CAPTION=yes  // Hide title
EWA_TEMP_NO=1          // Don't use custom template
EWA_REDRAW=1           // Redraw mode
EWA_IN_DIALOG=1        // Dialog mode
```

---

## 5. FrameTree Details

### 5.1 Configuration Example

```xml
<EasyWebTemplate Name="categories.T.V">
    <Page>
        <FrameTag><Set FrameTag="Tree"/></FrameTag>
        <Name><Set Name="categories.T.V"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
    </Page>
    <Action>
        <ActionSet>
            <Set Type="OnPageLoad">
                <CallSet>
                    <Set CallName="loadCategories" CallType="SqlSet"/>
                </CallSet>
            </Set>
        </ActionSet>
        <SqlSet>
            <Set Name="loadCategories" SqlType="query">
                <Sql><![CDATA[
                    SELECT 
                        category_id as EWA_TREE_KEY,
                        parent_id as EWA_TREE_PARENT_KEY,
                        category_name as EWA_TREE_TEXT,
                        sort_id
                    FROM categories
                    ORDER BY sort_id
                ]]></Sql>
            </Set>
        </SqlSet>
    </Action>
</EasyWebTemplate>
```

### 5.2 Tree Special Fields

```sql
-- EWA_TREE_KEY: Node ID
-- EWA_TREE_PARENT_KEY: Parent node ID
-- EWA_TREE_TEXT: Node display text
```

### 5.3 createHtml Flow

```java
// FrameTree.createHtml()
public void createHtml() throws Exception {
    // 1. Create TreeViewMain
    this._TreeViewMain = new TreeViewMain(
        super.getHtmlClass().getUserConfig(),
        super.getHtmlClass().getSysParas().getLang(),
        super.getHtmlClass().getSysParas().getFrameUnid()
    );
    
    // 2. Skin header
    super.createSkinTop();
    
    // 3. CSS (including tree icon CSS)
    super.createCss();
    this.createCssIcons();  // Special tree icons
    
    // 4. Header JS
    super.createJsTop();
    
    // 5. Tree content
    this.createContent();
    
    // 6. Skin footer
    this.createSkinBottom();
    
    // 7. Frame script
    this.createJsFramePage();
    
    // 8. Footer JS
    this.createJsBottom();
}
```

### 5.4 Creating Tree Content

```java
// FrameTree.createFrameContent()
public void createFrameContent() throws Exception {
    MStr sb = new MStr();
    
    // 1. Get root node ID
    String rootId = rv.getString(FrameParameters.EWA_TREE_ROOT_ID);
    if (rootId != null) {
        this._TreeViewMain.setRootId(rootId);
    }
    
    // 2. Get data
    DTTable tb = super.getHtmlClass().getItemValues().getLastTable();
    
    // 3. Generate tree HTML
    String treeHtml = _TreeViewMain.createTreeHtml(tb);
    sb.append(treeHtml);
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}

// TreeViewMain.createTreeHtml()
public String createTreeHtml(DTTable tb) {
    // 1. Build tree structure
    this.buildTree(tb);
    
    // 2. Recursively generate HTML
    return this.createNodeHtml(this._RootNode, 0);
}

private String createNodeHtml(TreeViewNode node, int lvl) {
    MStr sb = new MStr();
    
    // Node HTML
    sb.append("<div class='TREE_NODE' id='N_" + node.getId() + "'>");
    sb.append("<span class='TREE_ICON' onclick='toggleNode(\"" + node.getId() + "\")'>");
    sb.append(node.hasChildren() ? "[+]" : "[-]");
    sb.append("</span>");
    sb.append("<span class='TREE_TEXT' onclick='selectNode(\"" + node.getId() + "\")'>");
    sb.append(node.getText());
    sb.append("</span>");
    sb.append("</div>");
    
    // Child nodes
    if (node.hasChildren()) {
        sb.append("<div id='C_" + node.getId() + "' class='TREE_CHILDREN' style='display:none'>");
        for (int i = 0; i < node.getChildNodes().size(); i++) {
            TreeViewNode child = node.getChildNodes().get(i);
            sb.append(this.createNodeHtml(child, lvl + 1));
        }
        sb.append("</div>");
    }
    
    return sb.toString();
}
```

### 5.5 Tree Initialization Script

```java
// FrameTree.createJsFramePage()
public void createJsFramePage() throws Exception {
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    String url = super.getUrlJs();
    
    MStr sJs = new MStr();
    
    // Tree icon configuration
    sJs.append(this.createJsTreeIcon());
    
    // Initialize tree
    sJs.append("EWA.LANG='" + lang + "';");
    sJs.append("(function() {");
    sJs.append("var o = EWA.F.FOS['" + gunid + "'] = new EWA_TreeClass(");
    sJs.append("document.getElementById('EWA_TREE_" + gunid + "').parentNode,");
    sJs.append("'EWA.F.FOS[\"" + gunid + "\"]',");
    sJs.append("\"" + url + "\");");
    sJs.append("o.InitMenu(_EWA_MENU_" + gunid + ");");
    sJs.append("o.Init(EWA_ITEMS_XML_" + gunid + ");");
    sJs.append("o.Icons = _EWA_ICONS_" + gunid + ";");
    
    // Additional fields
    if (this._TreeViewMain.getAddColsLength() > 0) {
        sJs.append("o.AddCols =\"" + this._TreeViewMain.getAddCols() + "\";");
    }
    
    sJs.append("o.Id = o._Id ='" + gunid + "';");
    sJs.append("})();");
    
    // Initialize displayed value
    String k = rv.getString(FrameParameters.EWA_TREE_INIT_KEY);
    if (k != null) {
        sJs.append("try{ EWA.F.FOS['" + gunid + "'].ShowNode(\"" + k + "\"); }catch(e){}");
    }
    
    this.getHtmlClass().getDocument().addJs("TREE", sJs.toString(), false);
}
```

### 5.6 Tree Parameters

```java
// URL Parameters
EWA_TREE_ROOT_ID=0           // Root node ID
EWA_TREE_INIT_KEY=root       // Initialize expanded node
EWA_TREE_MORE=1              // Lazy loading
EWA_TREE_STATUS=1            // Get current state
EWA_TREE_SKIP_GET_STATUS=1   // Don't get state
EWA_TREE_KEY=category_id     // Node ID field
EWA_TREE_PARENT_KEY=parent_id // Parent node ID field
EWA_TREE_TEXT=category_name  // Node text field
```

---

## 6. Other Frame Types

### 6.1 FrameGrid (Grid)

```xml
<FrameTag><Set FrameTag="Grid"/></FrameTag>
```

Used for grid-based data display, similar to Excel tables.

### 6.2 FrameMenu (Menu)

```xml
<FrameTag><Set FrameTag="Menu"/></FrameTag>
```

Used for creating navigation menus.

### 6.3 FrameMultiGrid (Multi-Dimensional Table)

```xml
<FrameTag><Set FrameTag="MultiGrid"/></FrameTag>
```

Used for cross tables, pivot tables, and other complex tables.

### 6.4 FrameLogic (Logic)

```xml
<FrameTag><Set FrameTag="Logic"/></FrameTag>
```

Used for condition evaluation and flow control.

### 6.5 FrameReport (Report)

```xml
<FrameTag><Set FrameTag="Report"/></FrameTag>
```

Used for report display.

### 6.6 FrameCombine (Combine)

```xml
<FrameTag><Set FrameTag="Combine"/></FrameTag>
```

Used for combining multiple Frames.

### 6.7 FrameComplex (Complex)

```xml
<FrameTag><Set FrameTag="Complex"/></FrameTag>
```

Used for complex page layouts.

---

## 7. Inter-Frame Communication

### 7.1 Parent-Child Frame

```java
// Parent Frame calls child Frame
EWA.F.FOS["child_frame_id"].Reload();

// Child Frame calls parent Frame
EWA.F.FOS["@EWA_PARENT_FRAME"].Reload();
```

### 7.2 Inter-Frame Parameter Passing

```java
// URL Parameters
EWA_PARENT_FRAME=parent_id  // Specify parent Frame
EWA_FRAME_UNID_PREFIX=xxx   // Frame Unid prefix
```

### 7.3 ListFrame + Frame Coordination

```xml
<!-- Define double-click event in ListFrame -->
<XItem Name="butModify">
    <EventSet>
        <Set EventName="ondblclick" EventType="Javascript"
             EventValue="EWA.F.FOS['@sys_frame_unid'].ext_NewOrModifyOrCopy('M')"/>
    </EventSet>
</XItem>

<!-- Define return event in Frame -->
<XItem Name="butClose">
    <EventSet>
        <Set EventName="onclick" EventType="Javascript"
             EventValue="EWA.OW.Close(); EWA.OW.PFrame.Reload();"/>
    </EventSet>
</XItem>
```

---

## 8. Vue Mode

### 8.1 Enabling Vue Mode

```java
// URL Parameter
EWA_VUE=1
```

### 8.2 Vue Mode Processing

```java
// FrameBase.createHtmlVue()
public void createHtmlVue() {
    HtmlDocument doc = this.getHtmlClass().getDocument();
    
    // 1. Set title
    String title = getHtmlClass().getItemValues()
        .replaceParameters(getHtmlClass().getSysParas().getTitle(), true);
    doc.setTitle(title);
    
    // 2. Create CSS
    createCss();
    
    // 3. Create Vue container
    String id = "vue_" + getHtmlClass().getSysParas().getFrameUnid();
    String content = "<div id='" + id + "'></div>";
    doc.addScriptHtml(content);
    
    // 4. Generate configuration JSON
    String json = getHtmlClass().getHtmlCreator().createPageJsonExt1();
    
    // 5. Create Vue initialization script
    StringBuilder vueJs = new StringBuilder();
    vueJs.append("(function(){");
    vueJs.append("var ewacfg=" + json + ";");
    vueJs.append("app = EWA_VueClass(ewacfg, '#" + id + "');");
    vueJs.append("})();");
    
    doc.addJs("vue", vueJs.toString(), false);
}
```

---

## 9. Debugging Tips

### 9.1 Enabling Debug

```java
// URL Parameters
EWA_DEBUG_NO=1      // Don't show debug
EWA_DEBUG_KEY=xxx   // Debug key
EWA_JS_DEBUG=1      // JS debug mode (load uncompressed JS)
```

### 9.2 Viewing Frame Information

```javascript
// Browser console
EWA.F.FOS["frame_id"]  // Get Frame object
EWA.F.FOS["frame_id"].Reload()  // Refresh
EWA.F.FOS["frame_id"].Url  // Get URL
```

### 9.3 Performance Optimization

```java
// Cache configuration
EWA_R=1  // Force refresh data

// Don't display content (execute only)
EWA_NO_CONTENT=1

// Skip verification
EWA_VALIDCODE_CHECK=NOT_CHECK
```

---

## 10. Best Practices

### 10.1 ListFrame Optimization

```xml
<!-- ✅ Recommended: Use pagination -->
<PageSize><Set PageSize="20" IsSplitPage="1" KeyField="USER_ID"/></PageSize>

<!-- ✅ Recommended: Sticky headers -->
EWA_LU_STICKY_HEADERS=yes

<!-- ❌ Avoid: Display large amounts of data without pagination -->
<PageSize><Set IsSplitPage="0"/></PageSize>
```

### 10.2 Frame Optimization

```xml
<!-- ✅ Recommended: Explicitly specify MTYPE -->
EWA_MTYPE=N  <!-- New -->
EWA_MTYPE=M  <!-- Modify -->

<!-- ✅ Recommended: Use grouping -->
<FrameHtml><Set FrameCols="C2"/></FrameHtml>

<!-- ❌ Avoid: Too many fields on one page -->
```

### 10.3 Tree Optimization

```xml
<!-- ✅ Recommended: Lazy loading -->
EWA_TREE_MORE=1

<!-- ✅ Recommended: Specify root node -->
EWA_TREE_ROOT_ID=0

<!-- ❌ Avoid: Loading a large number of nodes at once -->
```

---

## Summary

| Frame Type | Typical Use | Key Configuration |
|-----------|---------|---------|
| **ListFrame** | Data list | PageSize, EWA_SEARCH, EWA_LF_ORDER |
| **Frame** | Form input | EWA_MTYPE, FrameCols, EWA_WIDTH |
| **Tree** | Category navigation | EWA_TREE_KEY, EWA_TREE_PARENT_KEY, EWA_TREE_TEXT |
| **Grid** | Grid display | - |
| **MultiGrid** | Cross table | - |
| **Menu** | Navigation menu | - |

Selection advice:
- List display → ListFrame
- Data entry → Frame
- Hierarchical structure → Tree
- Complex reports → MultiGrid / Report
- Combined pages → Combine / Complex
