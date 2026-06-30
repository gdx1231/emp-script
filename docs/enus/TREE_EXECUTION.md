# Tree Execution Flow Detail

## Overview

Tree is a component in the EWA framework for displaying hierarchical data structures, supporting lazy loading, node expand/collapse, right-click menus, and more.

**Core Classes**:
- `FrameTree` - Tree rendering class
- `ActionTree` - Tree action execution class
- `TreeViewMain` - Tree view main class
- `TreeViewNode` - Tree node class

---

## 1. Complete Execution Flow

```
HTTP Request (/ewa?XMLNAME=categories&ITEMNAME=T.V)
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
   ├─► 1. Load UserConfig (XML configuration)
   │     └─► Read FrameTag = "Tree"
   │
   ├─► 2. Create FrameTree instance
   │     this._Frame = new FrameTree()
   │     this._Action = new ActionTree()
   │
   ├─► 3. FrameTree.init(HtmlCreator)
   │
   ├─► 4. ActionTree.execute()
   │     ├─► Execute SqlSet (OnPageLoad)
   │     │   └─► createSql() - Generate lazy-load SQL
   │     └─► Execute ClassSet (optional)
   │
   └─► 5. FrameTree.createHtml()
         ├─► createSkinTop()      - Skin header
         ├─► createCss()          - CSS styles (including icon CSS)
         ├─► createJsTop()        - Header JS
         ├─► createContent()      - Main content (tree)
         │   ├─► createFrameContent()
         │   │   ├─► Create TreeViewMain
         │   │   ├─► initCreateAddCols() - Initialize additional fields
         │   │   └─► createTreeHtml() - Generate tree HTML
         │   │       ├─► createTreeNodes() - Build node relationships
         │   │       └─► createHtml() - Recursively generate HTML
         │   └─► createFrameHeader/Footer()
         ├─► createJsFramePage()  - Tree initialization script
         ├─► createSkinBottom()   - Skin footer
         └─► createJsBottom()     - Bottom JS
```

---

## 2. ActionTree Execution Detail

### 2.1 Execution Entry

```java
// ActionTree.executeSql()
public boolean executeSql(String sql, String sqlType, String name) {
    boolean isSelect = DataConnection.checkIsSelect(sql);
    
    if (sqlType.equals("procedure")) {
        // Stored procedure
        this.executeSqlProcdure(sql);
        return StringUtils.isBlank(this.getChkErrorMsg());
    } else if (isSelect) {
        // Query
        try {
            sql = this.createSql(sql);  // Generate lazy-load SQL
        } catch (Exception e) {
            LOGGER.error("{0}, {1}, {2}, {3}", sql, sqlType, name, e.getMessage());
        }
        
        DTTable dt = this.executeSqlQuery(sql);
        dt.setName(name);
        return StringUtils.isBlank(this.getChkErrorMsg());
    } else {
        // Update
        this.executeSqlUpdate(sql);
        return true;
    }
}
```

### 2.2 Generate Lazy-Load SQL

```java
// ActionTree.createSql() - Generate lazy-load SQL
public String createSql(String sql) throws Exception {
    UserConfig uc = super.getUserConfig();
    UserXItemValues u = uc.getUserPageItem().getItem("Tree");
    RequestValue rv = super.getRequestValue();
    
    if (u.count() == 0) {
        return sql;  // No Tree configuration, return original SQL
    }
    
    UserXItemValue v = u.getItem(0);
    
    // Check if lazy loading is enabled
    if (!v.getItem("LoadByLevel").equals("1")) {
        return sql;
    } 
    // Get more child nodes
    else if ("1".equals(rv.getString(FrameParameters.EWA_TREE_MORE))) {
        return this.createLoadByLevelMore(sql, u);
    } 
    // Get tree status (expanded nodes)
    else if ("1".equals(rv.getString(FrameParameters.EWA_TREE_STATUS))) {
        String s1 = this.createStatusFindPkeys(sql, u);
        if (s1 == null) {
            return "";
        } else {
            String s2 = this.createSqlLoadByLevel(s1, u);
            return s2;
        }
    } 
    // Default lazy load
    else {
        return this.createSqlLoadByLevel(sql, u);
    }
}

// ActionTree.createSqlLoadByLevel() - Generate lazy-load SQL
private String createSqlLoadByLevel(String sql, UserXItemValues u) throws Exception {
    UserXItemValue v = u.getItem(0);
    SqlPart sp = new SqlPart();
    sp.setSql(sql);
    
    String key = v.getItem("Key");         // Primary key field
    String pkey = v.getItem("ParentKey");  // Parent key field
    
    // Original query
    String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() 
              + " WHERE " + sp.getWhere();
    
    // Count child nodes for each parent
    String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT " 
              + "FROM " + sp.getTableName() + " GROUP BY " + pkey;
    
    // LEFT JOIN with count results
    String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3 + ") A " 
              + "\r\n LEFT JOIN (" + s2 + ") B ON A." + key + "=B.EWAPID ";
    
    if (sp.getOrderBy() != null) {
        s1 += " ORDER BY " + sp.getOrderBy();
    }
    
    return s1;
}

// ActionTree.createLoadByLevelMore() - Get more child nodes
private String createLoadByLevelMore(String sql, UserXItemValues u) throws Exception {
    UserXItemValue v = u.getItem(0);
    SqlPart sp = new SqlPart();
    sp.setSql(sql);
    
    String key = v.getItem("Key");
    String pkey = v.getItem("ParentKey");
    
    // Query children of a specific parent node
    String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() 
              + " WHERE " + pkey + "=@" + key;
    
    // Count child nodes
    String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT " 
              + "FROM " + sp.getTableName() + " GROUP BY " + pkey;
    
    // LEFT JOIN
    String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3 + ") A " 
              + "\r\n LEFT JOIN (" + s2 + ") B ON A." + key + "=B.EWAPID ";
    
    if (sp.getOrderBy() != null) {
        s1 += " ORDER BY " + sp.getOrderBy();
    }
    
    return s1;
}

// ActionTree.createStatusFindPkeys() - Find all parent nodes of expanded nodes
private String createStatusFindPkeys(String sql, UserXItemValues u) throws Exception {
    ArrayList<String> keys = new ArrayList<String>();
    UserXItemValue v = u.getItem(0);
    SqlPart sp = new SqlPart();
    sp.setSql(sql);
    
    String key = v.getItem("Key");
    String pkey = v.getItem("ParentKey");
    
    String s1 = "SELECT " + pkey + " FROM " + sp.getTableName() + " WHERE " + key + "='";
    String keyValue = super.getRequestValue().getString(key);
    
    int m = 0;
    while (keyValue != null) {
        m++;
        if (m > 20) {  // Prevent infinite loop
            return null;
        }
        if (keys.contains(keyValue)) {
            break;
        }
        keys.add(keyValue);
        
        // Recursively find parent node
        String s2 = s1 + keyValue.replace("'", "''") + "'";
        keyValue = this.createStatusFindPkey(s2);
    }
    
    if (keys.size() <= 1) {
        return null;  // Specified key value is incorrect
    }
    
    // Build IN query
    StringBuilder s3 = new StringBuilder();
    s3.append("SELECT " + sp.getFields() + " FROM " + sp.getTableName() 
            + " WHERE " + pkey + " IN (");
    for (int i = 0; i < keys.size() - 1; i++) {
        if (i > 0) {
            s3.append(",");
        }
        s3.append(keys.get(i).replace("'", "''"));
    }
    s3.append(") ");
    
    if (sp.getOrderBy() != null) {
        s3.append(" ORDER BY " + sp.getOrderBy());
    }
    
    return s3.toString();
}
```

---

## 3. TreeViewMain Detail

### 3.1 Initialize Parameters

```java
// TreeViewMain.initParameters()
private void initParameters() throws Exception {
    /*
     * XML configuration example:
     * <Tree>
     *   <Set Key="category_id" LoadByLevel="1" ParentKey="parent_id" 
     *        Text="category_name" MenuGroup="menu_grp" Title="memo"
     *        AddPara1="sort_id" AddPara2="status" AddPara3=""/>
     * </Tree>
     */
    _AddParasName = new ArrayList<String>();
    UserXItem userXitem = this._UserConfig.getUserPageItem();
    
    // Tree configuration fields
    this._FieldKey = userXitem.getSingleValue("Tree", "Key");         // Primary key
    this._FieldDispVal = userXitem.getSingleValue("Tree", "Text");    // Display text
    this._FieldParentKey = userXitem.getSingleValue("Tree", "ParentKey"); // Parent key
    this._FieldMenuGroup = userXitem.getSingleValue("Tree", "MenuGroup"); // Menu group
    this._FieldTitle = userXitem.getSingleValue("Tree", "Title");     // Tooltip info
    
    // Additional parameters
    String addPara1 = userXitem.getSingleValue("Tree", "AddPara1");
    String addPara2 = userXitem.getSingleValue("Tree", "AddPara2");
    String addPara3 = userXitem.getSingleValue("Tree", "AddPara3");
    _AddParasName.add(addPara1);
    _AddParasName.add(addPara2);
    _AddParasName.add(addPara3);
    
    // Whether to lazy load
    String s1 = userXitem.getSingleValue("Tree", "LoadByLevel");
    this._IsLoadByLevel = (s1 != null && s1.equals("1"));
    
    // Get caption (multilingual)
    this._Caption = HtmlUtils.getDescription(
        userXitem.getItem("DescriptionSet"), "Info", this._Lang);
}
```

### 3.2 Build Tree Nodes

```java
// TreeViewMain.createTreeNodes() - Build tree nodes
public TreeViewNode createTreeNodes(DTTable table) throws Exception {
    TreeViewNode nodeRoot = new TreeViewNode();
    
    // Initialize root node
    nodeRoot.setDispVal(this._Caption);
    nodeRoot.setKey("");
    
    // Key-to-TreeViewNode mapping
    HashMap<String, TreeViewNode> nodesHsahMap = new HashMap<String, TreeViewNode>();
    // Ordered loading list
    List<TreeViewNode> nodesList = new ArrayList<TreeViewNode>();
    
    nodesHsahMap.put(nodeRoot.getKey(), nodeRoot);
    nodeRoot.setParentKey(null);
    
    // Iterate data table, create nodes
    for (int i = 0; i < table.getCount(); i++) {
        DTRow row = table.getRow(i);
        this.createTreeNode(row, nodesHsahMap, nodesList);
    }
    
    // Build parent-child relationships
    this.bulidFatherAndChildren(nodesHsahMap, nodesList);
    
    nodeRoot.setKey(TreeViewNode.NODE_ROOT_KEY);
    return nodeRoot;
}

// TreeViewMain.createTreeNode() - Create a single node
private void createTreeNode(DTRow row, HashMap<String, TreeViewNode> nodesHsahMap, 
                            List<TreeViewNode> nodesList) throws Exception {
    // Get parent key and primary key
    Object oId = row.getCell(this._FieldParentKey).getValue();
    Object oKey = row.getCell(this._FieldKey).getValue();
    String pid = oId == null ? null : oId.toString();
    String key = oKey == null ? "null" : oKey.toString();
    
    // If primary key equals parent key, discard
    if (pid != null && pid.equals(key)) {
        LOGGER.warn("Primary key equals parent key, discarded" + pid);
        return;
    }
    
    // Root node handling
    if (pid == null || pid.equals("0") || pid.trim().equals("") 
        || pid.equals(this._RootId)) {
        pid = "";
    }
    
    // Get display value
    String dispVal = row.getCell(this._FieldDispVal).toString();
    if (dispVal == null) {
        dispVal = "[NULL]";
    }
    
    // Get menu group
    String menuGroup = "";
    if (this._FieldMenuGroup != null && this._FieldMenuGroup.trim().length() > 0) {
        menuGroup = row.getCell(this._FieldMenuGroup).toString();
        if (menuGroup == null) {
            menuGroup = "";
        }
    }
    
    // Get tooltip info
    String title = "";
    if (this._FieldTitle != null && this._FieldTitle.trim().length() > 0) {
        title = row.getCell(this._FieldTitle).toString();
        if (title == null) {
            title = "";
        }
    }
    
    // Create node
    TreeViewNode tvNode = this.createTreeNode(key, pid, dispVal, menuGroup, 
                                                "link", title, nodesHsahMap);
    
    // If primary key equals root node value, discard
    if (key.equals(this._RootId)) {
        LOGGER.warn("Primary key equals root node value, discarded: " + tvNode);
        return;
    }
    
    nodesHsahMap.put(tvNode.getKey(), tvNode);
    nodesList.add(tvNode);
    
    // Set associated data
    tvNode.setData(row);
    
    // Set additional parameters
    for (int i = 0; i < _AddParasName.size(); i++) {
        String addParaName = _AddParasName.get(i);
        if (addParaName == null || addParaName.trim().length() == 0) {
            tvNode.getAddParas().add(null);
        } else {
            tvNode.getAddParas().add(row.getCell(addParaName).getString());
        }
    }
    
    // Lazy loading: check if there are more child nodes
    if (_IsLoadByLevel) {
        Object oMore = row.getCell("EWAMORECNT").getValue();
        try {
            int childNodes = oMore == null ? 0 : Integer.parseInt(oMore.toString());
            tvNode.setMoreChild(childNodes > 0);
        } catch (Exception e) {
            tvNode.setMoreChild(false);
        }
    }
}

// TreeViewMain.createTreeNode() - Create node object
private TreeViewNode createTreeNode(String key, String parentKey, String dispVal, 
                                     String menuGroup, String cmd, String title,
                                     HashMap<String, TreeViewNode> nodesHsahMap) {
    TreeViewNode tvNode = new TreeViewNode();
    tvNode.setDispVal(dispVal);
    tvNode.setKey(key);
    tvNode.setParentKey(parentKey);
    tvNode.setJavaScriptCmd(cmd);
    tvNode.setMenuGroup(menuGroup);
    tvNode.setTitle(title);
    
    return tvNode;
}

// TreeViewMain.bulidFatherAndChildren() - Build parent-child relationships
private void bulidFatherAndChildren(HashMap<String, TreeViewNode> nodesHsahMap, 
                                     List<TreeViewNode> nodesList) {
    for (int i = 0; i < nodesList.size(); i++) {
        TreeViewNode tvNode = nodesList.get(i);
        String parentKey = tvNode.getParentKey();
        
        if (!nodesHsahMap.containsKey(parentKey)) {
            // Parent node does not exist, orphan node
            LOGGER.warn("Orphan node" + tvNode);
            continue;
        }
        
        TreeViewNode nodeParent = nodesHsahMap.get(tvNode.getParentKey());
        
        // Set previous/next node (for keyboard navigation)
        if (nodeParent.getChildNodes().size() > 0) {
            TreeViewNode prevNode = nodeParent.getChildNodes()
                .get(nodeParent.getChildNodes().size() - 1);
            tvNode.setPrevNode(prevNode);
            prevNode.setNextNode(tvNode);
        }
        
        // Add to parent's child node list
        nodeParent.getChildNodes().add(tvNode);
        nodesHsahMap.put(tvNode.getKey(), tvNode);
    }
}
```

### 3.3 Generate Tree HTML

```java
// TreeViewMain.createTreeHtml() - Generate tree HTML
public String createTreeHtml(DTTable table) throws Exception {
    TreeViewNode node = this.createTreeNodes(table);
    String s1 = createHtml(node, 0);
    return s1;
}

// TreeViewMain.createHtml() - Recursively generate HTML
private String createHtml(TreeViewNode node, int lvl) {
    StringBuilder sb = new StringBuilder();
    
    if (node.getKey().equals(TreeViewNode.NODE_ROOT_KEY)) {
        // Root node
        sb.append(NODE_ROOT_TEMPLATE.replace("[GUID]", this._Guid));
        replaceStr(sb, "[TMP_CAPTION_TEXT]", node.getDispVal());
    } else {
        // Normal node
        sb.append(createNodeHtml(node));
    }
    
    // Recursively process child nodes
    StringBuilder sbChildren = new StringBuilder();
    for (int i = 0; i < node.getChildNodes().size(); i++) {
        int new_lvl = lvl + 1;
        String childHtml = createHtml(node.getChildNodes().get(i), new_lvl);
        sbChildren.append(childHtml);
    }
    
    // Replace child node placeholder
    if (sbChildren.length() > 0) {
        String childTemplate = NODE_CHILD_TEMPLATE.replace("[TMP_CONTENT]", sbChildren.toString());
        replaceStr(sb, "[TMPLATE_CHILD_NODE]", childTemplate);
        replaceStr(sb, "[TEMP_DISPLAY]", "none");  // Default collapsed
    } else {
        replaceStr(sb, "[TMPLATE_CHILD_NODE]", "");
        replaceStr(sb, "[TEMP_DISPLAY]", "none");
    }
    
    return sb.toString();
}

// TreeViewMain.createNodeHtml() - Generate node HTML
private String createNodeHtml(TreeViewNode node) {
    StringBuilder sb = new StringBuilder(NODE_TEMPLATE);
    
    // Replace node values
    replaceStr(sb, "[TMP_NODE_VALUE]", node.getDispVal());
    
    // Replace menu group
    replaceStr(sb, "[TMPLATE_MENU_GROUP]", node.getMenuGroup());
    
    // Replace Key
    replaceStr(sb, "[TMPLATE_KEY]", node.getKey());
    
    // Replace level
    replaceStr(sb, "[LVL]", String.valueOf(node.getLvl()));
    
    // Replace title
    if (node.getTitle() != null && node.getTitle().trim().length() > 0) {
        replaceStr(sb, "[TMP_TITLE]", "title=\"" + node.getTitle() + "\"");
    } else {
        replaceStr(sb, "[TMP_TITLE]", "");
    }
    
    // Replace additional parameters
    StringBuilder addParas = new StringBuilder();
    for (int i = 0; i < node.getAddParas().size(); i++) {
        String val = node.getAddParas().get(i);
        if (val != null) {
            addParas.append("EWA_P").append(i).append("=\"")
                   .append(Utils.textToJscript(val)).append("\" ");
        }
    }
    replaceStr(sb, "[TMP_ADD_PARAS]", addParas.toString());
    
    // Replace additional fields
    if (this._AddCols != null) {
        String addColsHtml = this.createTreeAddHtml(node);
        replaceStr(sb, "[TEMP_NODE_ADD_FIELDS]", addColsHtml);
    } else {
        replaceStr(sb, "[TEMP_NODE_ADD_FIELDS]", "");
    }
    
    // Handle more child nodes marker
    if (node.isMoreChild()) {
        replaceStr(sb, "[TMP_NODE_MORE]", "EWA_TREE_MORE='1'");
    } else {
        replaceStr(sb, "[TMP_NODE_MORE]", "");
    }
    
    // Handle icons
    TreeOtherIcon icon = this._Icons.getIconByNode(node);
    if (icon != null) {
        replaceStr(sb, "[EWA_TREE_TD01]", "F_" + this._Guid + "_" + icon.getIndex() + "_A");
    } else {
        replaceStr(sb, "[EWA_TREE_TD01]", "TD01A");
    }
    
    return sb.toString();
}

// TreeViewMain.createTreeAddHtml() - Generate node additional fields HTML
private String createTreeAddHtml(TreeViewNode node) throws Exception {
    if (this._FrameTree == null) {
        return "";
    }
    
    if (node.getData() != null) {
        // Set current data row
        DTRow row = (DTRow) node.getData();
        row.getTable().getRows().setCurRow(row);
    }
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this._UserConfig.getUserXItems().count(); i++) {
        UserXItem uxi = this._UserConfig.getUserXItems().getItem(i);
        String tag = uxi.getSingleValue("Tag");
        
        if ("dataType".equalsIgnoreCase(tag)) {
            continue;  // Used to define data type, has no UI
        }
        
        String col = this.createItemHtmlCell(uxi);
        sb.append("<td class='ewa-node-add ewa-node-col-").append(uxi.getName()).append("'>");
        sb.append(col);
        sb.append("</td>");
    }
    
    return sb.toString();
}

// TreeViewMain.createItemHtmlCell() - Generate cell HTML
private String createItemHtmlCell(UserXItem uxi) throws Exception {
    if (this._FrameTree == null) {
        return "";
    }
    
    // Create style
    this.createCellParentStyle(uxi);
    
    // Create Item instance
    IItem item = this._FrameTree.getHtmlClass().getItem(uxi);
    String itemHtml = item.createItemHtml();
    
    // Process attributes
    if (uxi.testName("AttributeSet")) {
        UserXItemValues atts = uxi.getItem("AttributeSet");
        for (int i = 0; i < atts.count(); i++) {
            UserXItemValue att = atts.getItem(i);
            if (!att.testName("AttLogic")) {
                continue;
            }
            
            String logic = att.getItem("AttLogic").trim();
            if (logic.length() == 0) {
                continue;
            }
            logic = iv.replaceParameters(logic, false, true);
            
            if (!ULogic.runLogic(logic)) {
                // Expression is false, remove attribute
                String attName = att.getItem("AttName");
                String attValue = att.getItem("AttValue");
                String exp = attName + "=\"" + attValue + "\"";
                itemHtml = itemHtml.replace(exp, "");
            }
        }
    }
    
    return itemHtml;
}
```

---

## 4. FrameTree Rendering Detail

### 4.1 createHtml Flow

```java
// FrameTree.createHtml()
public void createHtml() throws Exception {
    super.addDebug(this, "TREE", "Start loading TreeViewMain");
    
    // 1. Create TreeViewMain
    _TreeViewMain = new TreeViewMain(
        super.getHtmlClass().getUserConfig(),
        super.getHtmlClass().getSysParas().getLang(),
        super.getHtmlClass().getSysParas().getFrameUnid()
    );
    super.addDebug(this, "TREE", "Loaded TreeViewMain OK");
    
    // 2. Skin header
    super.createSkinTop();
    super.addDebug(this, "TREE", "SkinTop");
    
    // 3. CSS (including icon CSS)
    super.createCss();
    this.createCssIcons();  // Special tree icons
    super.addDebug(this, "TREE", "Loaded CssIcons");
    
    // 4. Header JS
    super.createJsTop();
    super.addDebug(this, "TREE", "Loaded Jstop");
    
    // 5. Main content
    super.addDebug(this, "TREE", "Start loading main content");
    this.createContent();
    super.addDebug(this, "TREE", "Main content loaded OK");
    
    // 6. Skin footer
    this.createSkinBottom();
    
    // 7. Bottom JS
    this.createJsBottom();
    
    // 8. Frame script
    this.createJsFramePage();
}

// FrameTree.createFrameContent() - Create tree content
public void createFrameContent() throws Exception {
    MStr sb = new MStr();
    
    if (this._TreeViewMain == null) {
        this._TreeViewMain = new TreeViewMain(
            super.getHtmlClass().getUserConfig(),
            super.getHtmlClass().getSysParas().getLang(),
            super.getHtmlClass().getSysParas().getFrameUnid()
        );
    }
    
    // Set additional fields
    this._TreeViewMain.initCreateAddCols(this);
    
    // Set root node ID
    String rootId = super.getHtmlClass().getSysParas().getRequestValue()
        .getString(FrameParameters.EWA_TREE_ROOT_ID);
    if (rootId != null) {
        this._TreeViewMain.setRootId(rootId);
    }
    
    // Get data table
    MList tbs = super.getHtmlClass().getItemValues().getDTTables();
    DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
    
    // Set list data
    super.getHtmlClass().getItemValues().setListFrameTable(tb);
    
    // Generate tree HTML
    String s1 = _TreeViewMain.createTreeHtml(tb);
    sb.append(s1);
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}
```

### 4.2 Generate Icon CSS

```java
// FrameTree.createCssIcons() - Generate tree icon CSS
private void createCssIcons() {
    MStr sb = new MStr();
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    ArrayList<TreeOtherIcon> icons = this._TreeViewMain.getIcons().getIcons();
    
    String skinName = getPageItemValue("SkinName", "SkinName");
    
    for (int i = 0; i < icons.size(); i++) {
        TreeOtherIcon icon = icons.get(i);
        String open = icon.getOpen();   // Expanded icon
        String close = icon.getClose(); // Collapsed icon
        
        // Expanded state CSS
        sb.append("#" + skinName + " .F_" + gunid + "_" + i + "_A{");
        sb.append("width:20px; height:18px; ");
        sb.append("background-image:url('" + replaceParameters(open, true) + "');");
        sb.append("background-repeat:no-repeat ;}\r\n");
        
        // Collapsed state CSS
        sb.append("#" + skinName + " .F_" + gunid + "_" + i + "_B{");
        sb.append("width:20px; height:18px; ");
        sb.append("background-image:url('" + replaceParameters(close, true) + "');");
        sb.append("background-repeat:no-repeat ;}\r\n");
    }
    
    this.getHtmlClass().getDocument().addCss(sb.toString());
}
```

### 4.3 Generate Tree Initialization Script

```java
// FrameTree.createJsFramePage()
public void createJsFramePage() throws Exception {
    RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    String lang = super.getHtmlClass().getSysParas().getLang();
    String url = super.getUrlJs();
    
    super.createJsFrameXml();   // Item description XML string
    super.createJsFrameMenu();  // Menu description XML string
    super.createJsTop();
    super.createJsBottom();
    
    MStr sJs = new MStr();
    
    // Generate icon configuration
    sJs.append(this.createJsTreeIcon());
    sJs.append("\r\nEWA.LANG='" + lang + "'; //page language\r\n");
    
    String fName = "EWA.F.FOS[\"" + gunid + "\"]";
    String parent = "document.getElementById('EWA_TREE_" + gunid + "').parentNode";
    
    sJs.append("(function() {\n");
    sJs.append("var o = EWA.F.FOS['" + gunid + "'] = new EWA_TreeClass(" 
              + parent + ",'" + fName + "',\"" + url + "\");\n");
    sJs.append("o.InitMenu(_EWA_MENU_" + gunid + ");\n");
    sJs.append("o.Init(EWA_ITEMS_XML_" + gunid + ");\n");
    sJs.append("o.Icons = _EWA_ICONS_" + gunid + ";\n");
    
    // Additional fields
    if (this._TreeViewMain != null && this._TreeViewMain.getAddColsLength() > 0) {
        sJs.append("o.AddCols =\"" + Utils.textToJscript(this._TreeViewMain.getAddCols()) + "\";\n");
    }
    
    sJs.append("o.Id = o._Id ='" + gunid + "';\n})();\n");
    
    // Initialize display value
    String k = rv.getString(FrameParameters.EWA_TREE_INIT_KEY);
    if (k != null && k.trim().length() > 0) {
        k = Utils.textToJscript(k);
        sJs.append("try{");
        sJs.append(fName + ".ShowNode(\"" + k + "\");\r\n");
        sJs.append("}catch(e){}\r\n");
    }
    
    // Frame type configuration
    String frameType = super.getPageItemValue("HtmlFrame", "FrameType");
    if (frameType != null && frameType.trim().length() > 0) {
        String subUrl = super.getPageItemValue("HtmlFrame", "FrameSubUrl");
        if (subUrl != null && subUrl.indexOf("FrameSubUrl") < 0 
            && subUrl.trim().length() > 0) {
            subUrl = subUrl.trim();
            sJs.al("function link(id){");
            sJs.al("window.parent.frames[1].location='" + subUrl + "'+id+'&'+$U();");
            sJs.al("}");
        }
    }
    
    this.getHtmlClass().getDocument().addJs("TREE", sJs.toString(), false);
}

// FrameTree.createJsTreeIcon() - Generate icon configuration JS
private String createJsTreeIcon() {
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    MStr jsIcons = new MStr();
    jsIcons.append("var _EWA_ICONS_" + gunid + "={\r\n");
    
    ArrayList<TreeOtherIcon> icons = this._TreeViewMain.getIcons().getIcons();
    
    for (int i = 0; i < icons.size(); i++) {
        TreeOtherIcon icon = icons.get(i);
        String filter = icon.getFilter();
        String test = icon.getTest();
        
        if (i > 0) {
            jsIcons.append(",");
        }
        jsIcons.append("\tITEM_" + i + ": {");
        jsIcons.append("TEST: '" + test + "', ");
        jsIcons.append("FILTER: \"" + filter + "\", ");
        jsIcons.append("NAME: 'F_" + gunid + "_" + i + "_'");
        jsIcons.append("}\r\n");
    }
    jsIcons.append("};\r\n");
    return jsIcons.toString();
}
```

---

## 5. Key Parameters

### 5.1 URL Parameters

| Parameter | Description | Example |
|------|------|------|
| `EWA_TREE_ROOT_ID` | Root node ID | `EWA_TREE_ROOT_ID=0` |
| `EWA_TREE_INIT_KEY` | Initial expanded node | `EWA_TREE_INIT_KEY=root` |
| `EWA_TREE_MORE` | Lazy load | `EWA_TREE_MORE=1` |
| `EWA_TREE_STATUS` | Get tree status | `EWA_TREE_STATUS=1` |
| `EWA_TREE_SKIP_GET_STATUS` | Skip getting status | `EWA_TREE_SKIP_GET_STATUS=1` |
| `EWA_TREE_KEY` | Node ID field | `EWA_TREE_KEY=category_id` |
| `EWA_TREE_PARENT_KEY` | Parent node ID field | `EWA_TREE_PARENT_KEY=parent_id` |
| `EWA_TREE_TEXT` | Node text field | `EWA_TREE_TEXT=category_name` |

### 5.2 XML Configuration

```xml
<Tree>
    <Set Key="category_id" 
         ParentKey="parent_id" 
         Text="category_name" 
         MenuGroup="menu_grp" 
         Title="memo"
         LoadByLevel="1"
         RootId="0"
         AddPara1="sort_id" 
         AddPara2="status" 
         AddPara3=""/>
</Tree>
```

---

## 6. Lazy Loading

### 6.1 Configuration

```xml
<Tree>
    <Set Key="id" ParentKey="pid" Text="name" LoadByLevel="1"/>
</Tree>
```

### 6.2 SQL Generation

```sql
-- First load (root nodes)
SELECT A.*, B.EWAMORECNT FROM (
    SELECT id, pid, name FROM categories WHERE pid='0'
) A 
LEFT JOIN (
    SELECT pid EWAPID, COUNT(*) EWAMORECNT 
    FROM categories GROUP BY pid
) B ON A.id=B.EWAPID

-- Load child nodes when clicking to expand
SELECT A.*, B.EWAMORECNT FROM (
    SELECT id, pid, name FROM categories WHERE pid=@id
) A 
LEFT JOIN (
    SELECT pid EWAPID, COUNT(*) EWAMORECNT 
    FROM categories GROUP BY pid
) B ON A.id=B.EWAPID
```

### 6.3 Frontend Calls

```javascript
// Click to expand node
EWA.F.FOS["tree_id"].LoadMore(nodeKey);

// Get tree status
EWA.F.FOS["tree_id"].GetStatus();
```

---

## 7. Additional Fields

### 7.1 Configuration

```xml
<XItems>
    <XItem Name="SORT_ID">
        <Tag><Set Tag="span"/></Tag>
        <DataItem><Set DataField="sort_id"/></DataItem>
    </XItem>
    <XItem Name="STATUS">
        <Tag><Set Tag="span"/></Tag>
        <DataItem><Set DataField="status"/></DataItem>
    </XItem>
</XItems>
```

### 7.2 Generated HTML

```html
<table class='ewa-tree-node ewa-tree-lvl-1'>
    <tr class='ewa-node-row-0'>
        <td class='TD00A ewa-node-open-close'>...</td>
        <td class='TD01A ewa-node-icon'>...</td>
        <td class='ewa-node-caption'>Node Name</td>
        <!-- Additional fields -->
        <td class='ewa-node-add ewa-node-col-SORT_ID'>100</td>
        <td class='ewa-node-add ewa-node-col-STATUS'>Normal</td>
    </tr>
</table>
```

---

## 8. Icon Configuration

### 8.1 Configuration

```xml
<TreeOtherIcons>
    <TreeOtherIcon>
        <Test>@status='1'</Test>
        <Filter>status=1</Filter>
        <Open>/images/folder_open.png</Open>
        <Close>/images/folder_close.png</Close>
    </TreeOtherIcon>
    <TreeOtherIcon>
        <Test>@status='0'</Test>
        <Filter>status=0</Filter>
        <Open>/images/file_open.png</Open>
        <Close>/images/file_close.png</Close>
    </TreeOtherIcon>
</TreeOtherIcons>
```

### 8.2 Icon CSS

```css
#Test1 .F_abc123_0_A {
    width:20px; height:18px;
    background-image:url('/images/folder_open.png');
    background-repeat:no-repeat;
}
#Test1 .F_abc123_0_B {
    width:20px; height:18px;
    background-image:url('/images/folder_close.png');
    background-repeat:no-repeat;
}
```

---

## 9. Debugging Tips

### 9.1 Enable Debug

```
EWA_DEBUG_NO=1      # Do not show Debug
EWA_DEBUG_KEY=xxx   # Debug key
EWA_JS_DEBUG=1      # JS debug mode
```

### 9.2 Browser Console

```javascript
// Get Tree object
EWA.F.FOS["tree_id"]

// Expand node
EWA.F.FOS["tree_id"].ShowNode("node_key")

// Get tree status
EWA.F.FOS["tree_id"].GetStatus()

// Reload
EWA.F.FOS["tree_id"].Reload()
```

### 9.3 View Executed SQL

```java
// Recorded in DataConnection
String sql = conn.getLastResult().getSqlOrigin();
System.out.println("Original SQL: " + sql);

String executedSql = conn.getLastResult().getSqlExecute();
System.out.println("Executed SQL: " + executedSql);
```

---

## 10. Complete Example

### 10.1 XML Configuration

```xml
<EasyWebTemplate Name="categories.T.V">
    <Page>
        <FrameTag><Set FrameTag="Tree"/></FrameTag>
        <Name><Set Name="categories.T.V"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <Tree>
            <Set Key="category_id" 
                 ParentKey="parent_id" 
                 Text="category_name" 
                 MenuGroup="menu_grp" 
                 Title="memo"
                 LoadByLevel="1"
                 RootId="0"
                 AddPara1="sort_id" 
                 AddPara2="status"/>
        </Tree>
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
                    SELECT category_id, parent_id, category_name, 
                           sort_id, status, memo
                    FROM categories
                    WHERE parent_id='0'
                    ORDER BY sort_id
                ]]></Sql>
            </Set>
        </SqlSet>
    </Action>
    <XItems>
        <XItem Name="SORT_ID">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="sort_id"/></DataItem>
        </XItem>
        <XItem Name="STATUS">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="status"/></DataItem>
        </XItem>
    </XItems>
    <TreeOtherIcons>
        <TreeOtherIcon>
            <Test>@status='1'</Test>
            <Filter>status=1</Filter>
            <Open>/images/folder_open.png</Open>
            <Close>/images/folder_close.png</Close>
        </TreeOtherIcon>
    </TreeOtherIcons>
</EasyWebTemplate>
```

### 10.2 URL Calls

```
# Basic query
/ewa?XMLNAME=categories&ITEMNAME=T.V

# Specify root node
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_ROOT_ID=0

# Initialize expanded node
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_INIT_KEY=100

# Lazy load
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_MORE=1&category_id=100

# Get tree status
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_STATUS=1&category_id=100

# Skip status
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_SKIP_GET_STATUS=1
```

---

## Summary

Key points of Tree execution flow:

1. **Action Execution** → `ActionTree.execute()`
   - Generate lazy-load SQL
   - Execute SqlSet

2. **Tree Construction** → `TreeViewMain.createTreeNodes()`
   - Iterate data table
   - Create nodes
   - Build parent-child relationships

3. **HTML Rendering** → `FrameTree.createHtml()`
   - createSkinTop()
   - createCss() + createCssIcons()
   - createJsTop()
   - createContent() → createFrameContent()
     - createTreeHtml()
       - createTreeNodes() - Build node relationships
       - createHtml() - Recursively generate HTML
   - createJsFramePage()
   - createSkinBottom()
   - createJsBottom()

4. **Lazy Loading**
   - LoadByLevel=1 enabled
   - EWAMORECNT counts child nodes
   - EWA_TREE_MORE gets more child nodes

5. **Special Features**
   - Additional field display
   - Dynamic icons
   - Right-click menus
   - Node expand/collapse
