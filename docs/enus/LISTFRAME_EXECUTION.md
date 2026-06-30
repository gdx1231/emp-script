# ListFrame Execution Flow Detail

## Overview

ListFrame is the core component in the EWA framework for displaying data lists, supporting pagination, search, sorting, export, and more.

**Core Classes**:
- `FrameList` - ListFrame rendering class
- `ActionListFrame` - ListFrame action execution class
- `FrameBase` - Frame base class

---

## 1. Complete Execution Flow

```
HTTP Request (/ewa?XMLNAME=users&ITEMNAME=LF.M)
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
   │     └─► Read FrameTag = "ListFrame"
   │
   ├─► 2. Create FrameList instance
   │     this._Frame = new FrameList()
   │     this._Action = new ActionListFrame()
   │
   ├─► 3. FrameList.init(HtmlCreator)
   │
   ├─► 4. ActionListFrame.execute()
   │     ├─► Execute SqlSet (OnPageLoad)
   │     │   └─► executeSplitSql() - Paginated query
   │     └─► Execute ClassSet (optional)
   │
   └─► 5. FrameList.createHtml()
         ├─► createHtmlTraditional()  // Traditional mode
         │   ├─► createSkinTop()      - Skin header
         │   ├─► createCss()          - CSS styles
         │   ├─► createJsTop()        - Header JS
         │   ├─► createContent()      - Main content (table)
         │   │   └─► createFrameContent()
         │   │       ├─► initListUIParams()  - Initialize redraw parameters
         │   │       ├─► Iterate data rows
         │   │       └─► createItemHtmls()   - Generate row HTML
         │   ├─► createJsFramePage()  - ListFrame initialization script
         │   ├─► createSkinBottom()   - Skin footer
         │   └─► createJsBottom()     - Bottom JS
         │
         └─► createHtmlVue()  // Vue mode (EWA_VUE=1)
```

---

## 2. ActionListFrame Execution Detail

### 2.1 Execution Entry

```java
// ActionListFrame.execute()
public void execute() throws Exception {
    String actionName = this.getHtmlClass().getSysParas().getActionName();
    
    // 1. Execute SqlSet
    UserXItemValues sqlset = getUserConfig().getUserActionItem().getItem("SqlSet");
    for (int i = 0; i < sqlset.count(); i++) {
        UserXItemValue sqlItem = sqlset.getItem(i);
        String sqlName = sqlItem.getItem("Name");
        
        // Check if this SQL should be executed
        String test = sqlItem.getItem("Test");
        if (test != null && !ULogic.runLogic(test)) {
            continue;  // Condition not met, skip
        }
        
        this.executeCallSql(sqlName);
    }
    
    // 2. Execute ClassSet (optional)
    UserXItemValues classset = getUserConfig().getUserActionItem().getItem("ClassSet");
    for (int i = 0; i < classset.count(); i++) {
        UserXItemValue classItem = classset.getItem(i);
        String className = classItem.getItem("ClassName");
        String methodName = classItem.getItem("MethodName");
        
        // Call Java class
        this.executeCallClass(className, methodName);
    }
}
```

### 2.2 Paginated Query Execution

```java
// ActionListFrame.executeCallSql()
public void executeCallSql(String name) throws Exception {
    UserXItemValues sqlset = getUserConfig().getUserActionItem().getItem("SqlSet");
    UserXItemValue sqlItem = sqlset.getItem(name);
    String sqlExp = sqlItem.getItem("Sql");
    String[] sqlArray = sqlExp.split(";");
    
    DataConnection conn = getItemValues().getSysParas().getDataConn();
    RequestValue rv = getRequestValue();
    
    boolean executedSplitSql = false;  // Pagination executes only once
    
    for (int i = 0; i < sqlArray.length; i++) {
        String sql = sqlArray[i].trim();
        if (sql.length() == 0) continue;
        
        String sqlType = sqlItem.getItem("SqlType");  // query / update
        
        // Check if it is the first SELECT query (needs pagination)
        if (sqlType.equals("query") 
            && !executedSplitSql 
            && DataConnection.checkIsSelect(sql)
            && sql.indexOf("EWA_ERR_OUT") == -1
            && sql.indexOf("EWA_SQL_SPLIT_NO") == -1) {
            
            executedSplitSql = true;
            this.executeSplitSql(sql, conn, rv, name);
        } else {
            // Other SQL executed directly (no pagination)
            super.executeSql(sql, sqlType, name);
        }
        
        // Check errors
        if (StringUtils.isNotBlank(conn.getErrorMsg())) {
            throw new Exception(conn.getErrorMsg());
        }
    }
}

// ActionListFrame.executeSplitSql() - Execute paginated query
private void executeSplitSql(String sql, DataConnection conn, RequestValue rv, String name) {
    // 1. Check if SQL contains WHERE
    if (sql.toUpperCase().indexOf("WHERE") < 0) {
        throw new Exception("Query should contain WHERE condition");
    }
    
    // 2. Build full SQL (add search and sort conditions)
    String sql1 = this.createSqListFrame(sql, conn);
    
    // 3. Check download mode
    String ajax = rv.getString(FrameParameters.EWA_AJAX);
    if (ajax != null && ajax.trim().toUpperCase().equals("DOWN_DATA")) {
        this.createDownloadData(sql1);  // Export data
        return;
    }
    
    // 4. Execute paginated query
    boolean useSplit = rv.getString(FrameParameters.EWA_PAGESIZE) != null;
    DTTable tb = this.executeSqlWithPageSplit(sql1, conn, rv, useSplit);
    tb.setName(name);
    
    if (tb.isOk()) {
        super.getDTTables().add(tb);
    }
}

// ActionListFrame.executeSqlWithPageSplit() - Execute paginated query
private DTTable executeSqlWithPageSplit(String sql1, DataConnection conn, 
                                         RequestValue rv, boolean useSplit) {
    PageSplit ps = null;
    DTTable tb = null;
    
    if (useSplit) {
        // Paginated query
        int iPageSize = this.getUserSettingPageSize();  // Default 20
        ps = new PageSplit(0, rv, iPageSize);
        
        String keyField = this.getPageItemValue("PageSize", "KeyField");
        
        tb = DTTable.getJdbcTable(sql1, keyField, ps.getPageSize(), 
                                    ps.getPageCurrent(), conn);
    } else {
        // No pagination, query all
        tb = DTTable.getJdbcTable(sql1, conn);
    }
    
    if (tb.isOk()) {
        // Mark as paginated query result
        tb.getAttsTable().add(EXECUTE_SPLIT_SQL, "1");
        tb.getAttsTable().add(SPLIT_SQL, sql1);
        tb.getAttsTable().add(PAGE_SIZE, ps);
        
        // Save as ListFrame table
        getItemValues().setListFrameTable(tb);
    }
    
    return tb;
}
```

### 2.3 Build SQL (Search + Sort)

```java
// ActionListFrame.createSqListFrame()
private String createSqListFrame(String sql, DataConnection conn) throws Exception {
    // 1. Add search conditions
    String searchSql = this.createSqlSearchInit(conn);
    if (searchSql.trim().length() > 0) {
        sql = this.insertWhereCondition(sql, searchSql);
    }
    
    // 2. Add user search conditions (EWA_LF_SEARCH)
    String userSearch = this.createSqlSearch(conn);
    if (userSearch.trim().length() > 0) {
        sql = this.insertWhereCondition(sql, userSearch);
    }
    
    // 3. Add sort conditions
    String orderSql = this.createSqlOrder();
    if (orderSql.trim().length() > 0) {
        sql += " ORDER BY " + orderSql;
    }
    
    return sql;
}

// Add search conditions
private String createSqlSearchInit(DataConnection conn) throws Exception {
    RequestValue rv = getItemValues().getRequestValue();
    String ewa_search = rv.getString(FrameParameters.EWA_SEARCH);
    
    if (ewa_search == null || ewa_search.trim().length() == 0) {
        return "";
    }
    
    MStr sb = new MStr();
    sb.al("(100 = 100 ");  // Always true condition
    
    // Parse search parameters: ewa_search=name[lk]Zhang,email[eq]test@example.com
    String[] para = ewa_search.split(",");
    for (int i = 0; i < para.length; i++) {
        SearchParameterInit lsp = new SearchParameterInit(para[i]);
        if (!lsp.isValid()) continue;
        
        // Check if field exists in configuration
        if (!getUserConfig().getUserXItems().testName(lsp.getFieldName())) {
            continue;
        }
        
        UserXItem uxi = getUserConfig().getUserXItems().getItem(lsp.getFieldName());
        String dataField = uxi.getItem("DataItem").getItem(0).getItem("DataField");
        
        // Generate SQL based on search type
        String searchType = lsp.getSearchType().toLowerCase();
        switch (searchType) {
            case "lk":  // Contains
                sb.al(" AND " + dataField + " LIKE '%" + lsp.getValue() + "%'");
                break;
            case "llk": // Left match
                sb.al(" AND " + dataField + " LIKE '" + lsp.getValue() + "%'");
                break;
            case "rlk": // Right match
                sb.al(" AND " + dataField + " LIKE '%" + lsp.getValue() + "'");
                break;
            case "eq":  // Equals
                sb.al(" AND " + dataField + " = '" + lsp.getValue() + "'");
                break;
            case "or":  // OR
                String[] orValues = lsp.getValue().split(";");
                sb.al(" AND (" + dataField + " = '" + orValues[0] + "'");
                for (int j = 1; j < orValues.length; j++) {
                    sb.al(" OR " + dataField + " = '" + orValues[j] + "'");
                }
                sb.al(")");
                break;
        }
    }
    
    sb.al(")");
    return sb.toString();
}

// Add sort conditions
private String createSqlOrder() throws Exception {
    RequestValue rv = getItemValues().getRequestValue();
    String userOrder = rv.getString(FrameParameters.EWA_LF_ORDER);
    
    if (userOrder == null || userOrder.trim().length() == 0) {
        return "";
    }
    
    // Parse sort field: create_date DESC
    String fieldName = userOrder.split(" ")[0].trim();
    String ascDesc = userOrder.split(" ")[1].trim().toUpperCase();
    
    // Get field configuration
    UserXItem uxi = getUserConfig().getUserXItems().getItem(fieldName);
    String dataField = uxi.getItem("DataItem").getItem(0).getItem("DataField");
    String dataType = uxi.getItem("DataItem").getItem(0).getItem("DataType");
    
    // Chinese sort handling
    DataConnection conn = getItemValues().getSysParas().getDataConn();
    String dbType = conn.getDatabaseType();
    String orderField = SqlUtils.replaceChnOrder(dbType, dataField, dataType);
    
    // Default descending
    if (ascDesc.equals("ASC")) {
        orderField += " ASC";
    } else {
        orderField += " DESC";
    }
    
    return orderField;
}
```

---

## 3. FrameList Rendering Detail

### 3.1 createHtml Flow

```java
// FrameList.createHtml()
public void createHtml() throws Exception {
    if (super.getHtmlClass().getSysParas().isVue()) {
        super.createHtmlVue();  // Vue mode
    } else {
        this.createHtmlTraditional();  // Traditional mode
    }
}

// FrameList.createHtmlTraditional()
public void createHtmlTraditional() {
    try {
        super.createSkinTop();      // Skin header
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
    }
    
    try {
        super.createCss();          // CSS styles
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
    }
    
    try {
        super.createJsTop();        // Header JS
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
    }
    
    // Main content
    try {
        String box = rv.getString(FrameParameters.EWA_BOX);
        String left = rv.getString(FrameParameters.EWA_LEFT);
        
        if (box == null && left == null) {
            doc.addScriptHtml(isNotUsingFrameBox ? "" : "<div>");
            this.createContent();
            doc.addScriptHtml(isNotUsingFrameBox ? "" : "</div>");
        } else {
            // BOX mode redraw
            this.createJsonFrame();
            String pageAddTop = this.getPageItemValue("AddHtml", "Top");
            doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop.trim());
        }
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
    
    // Footer
    try {
        this.createSkinBottom();    // Skin footer
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
    
    try {
        this.createJsBottom();      // Bottom JS
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
    
    try {
        this.createJsFramePage();   // Frame initialization script
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
}
```

### 3.2 Create Table Content

```java
// FrameList.createFrameContent()
public void createFrameContent() throws Exception {
    UserConfig uc = getHtmlClass().getUserConfig();
    UserXItem item = uc.getUserPageItem();
    
    // 1. Initialize list redraw parameters
    this.initListUIParams(item);
    
    RequestValue rv = getHtmlClass().getSysParas().getRequestValue();
    boolean isApp = rv.s(FrameParameters.EWA_APP) != null;  // App call
    boolean ewaRowSign = Utils.cvtBool(rv.s(FrameParameters.EWA_ROW_SIGN));  // MD5 signature
    
    HtmlDocument doc = getHtmlClass().getDocument();
    
    // 2. Skin-defined header
    if (!isNotUsingFrameBox) {
        doc.addScriptHtml("<div>");
        String top = super.createSkinFCTop();
        
        // Add mouseout event
        if (!isApp) {
            String fos = "EWA.F.FOS[\"" + getFrameUnid() + "\"]";
            top = top.replace("<table", 
                "<table onmouseout='if(window.EWA && EWA.F && EWA.F.FOS && " 
                + fos + "){" + fos + ".MOut(event)}'");
        }
        doc.addScriptHtml(top);
    }
    
    // 3. Frame-defined header (search box, button bar)
    String frameHeader = this.createFrameHeader();
    doc.addScriptHtml(frameHeader);
    
    // 4. Get data table
    MList tbs = getHtmlClass().getAction().getDTTables();
    if (tbs == null || tbs.size() == 0) {
        createItemHtmls(false);  // No data
        doc.addScriptHtml("<!-- no data -->");
    } else {
        DTTable tb = this.getSplitPageTable();
        if (tb == null) {
            tb = (DTTable) tbs.get(tbs.size() - 1);
        }
        
        getItemValues().setListFrameTable(tb);
        
        // 5. Pagination handling
        if (this.isSplitPage()) {
            this.queryRecords();  // Get total record count
        }
        
        // 6. Column count
        int colSize = 1;
        String s1 = getPageItemValue("PageSize", "ColSize");
        if (s1 != null && s1.trim().length() > 0) {
            try {
                colSize = Integer.parseInt(s1);
            } catch (Exception e) {}
        }
        
        // 7. Group handling
        FrameListGroup flGroup = null;
        if (this._GroupUserXItem != null) {
            flGroup = new FrameListGroup();
            flGroup.init(this._GroupUserXItem);
            flGroup.setColSpan(tb.getColumns().getCount());
            flGroup.setFrameGUID(getFrameUnid());
        }
        
        // 8. Iterate data rows
        MStr sb = new MStr();
        boolean isUseTemplate = this.isUsingTemplate();
        String frameTemplate = getPageItemValue("FrameHtml", "FrameHtml");
        
        for (int i = 0; i < tb.getCount(); i++) {
            tb.getRow(i);  // Move current row
            
            // Generate row HTML
            String rowHtml = isUseTemplate 
                ? this.createItemHtmlsByFrameHtml(frameTemplate, "FrameList")
                : this.createItemHtmls();
            
            if (isNotUsingFrameBox) {
                sb.a(rowHtml);
                continue;
            }
            
            // Add primary key expression
            String keyExp = "EWA_KEY=\"" + this.createItemKeys() + "\" ";
            
            // Handle groups
            if (flGroup != null) {
                String grpValue = tb.getCell(i, this._GroupUserXItem.getDataField()).toString();
                if (flGroup.getGrpValue() == null || !flGroup.getGrpValue().equals(grpValue)) {
                    // New group
                    String grpHtml = this.createFrameGroup(flGroup);
                    sb.a(grpHtml);
                    flGroup.setGrpValue(grpValue);
                }
            }
            
            // Add row
            sb.a("<tr id='R_" + i + "' " + keyExp);
            
            // MD5 signature
            if (ewaRowSign) {
                String md5 = Utils.md5(rowHtml);
                sb.a(" data-md5='" + md5 + "'");
            }
            
            sb.a(">" + rowHtml + "</tr>");
        }
        
        // 9. Create table
        if (!isNotUsingFrameBox) {
            doc.addScriptHtml("<table class='EWA_LISTFRAME'>" + sb.toString() + "</table>");
            
            // Pagination bar
            this.createPageBar(doc);
            
            // Button bar
            this.createButtonBar(doc);
            
            // Search box
            this.createSearchBox(doc);
            
            doc.addScriptHtml("</div>");  // Close skin header
        } else {
            doc.addScriptHtml(sb.toString());
        }
    }
    
    // 10. User custom bottom HTML
    String pageAddBottom = getPageItemValue("AddHtml", "Bottom");
    if (pageAddBottom != null && pageAddBottom.trim().length() > 0) {
        doc.addScriptHtml(pageAddBottom);
    }
}
```

### 3.3 Generate Row HTML

```java
// FrameList.createItemHtmls()
public String createItemHtmls() throws Exception {
    return createItemHtmls(false);
}

public String createItemHtmls(boolean isCreateScript) throws Exception {
    MStr sb = new MStr();
    UserConfig uc = getHtmlClass().getUserConfig();
    String lang = getHtmlClass().getSysParas().getLang();
    
    // Iterate XItems
    for (int i = 0; i < uc.getUserXItems().count(); i++) {
        UserXItem uxi = uc.getUserXItems().getItem(i);
        String name = uxi.getName().toUpperCase().trim();
        
        // Skip hidden fields
        if (getHtmlClass().getSysParas().isHiddenColumn(name)
            || getHtmlClass().getFrame().isHiddenField(name)) {
            continue;
        }
        
        // Get Tag type
        String tag = uxi.getSingleValue("Tag");
        if (tag == null || tag.trim().length() == 0) {
            continue;
        }
        
        // Create Item instance
        IItem item = ItemFactory.createItem(tag, uxi);
        
        // Render Item
        String html = item.render();
        
        // Add to cell
        sb.a("<td class='EWA_TD_M'>" + html + "</td>");
    }
    
    return sb.toString();
}

// Item rendering example (ItemBase.render())
public String render() throws Exception {
    // 1. Get HTML template (from EwaConfig.xml)
    String template = this.getHtmlTemplate();
    
    // 2. Get field value
    String value = this.getValue();
    
    // 3. Replace template variables
    template = template.replace("{__EWA_VAL__}", value);
    template = template.replace("@NAME", this.getXItem().getName());
    
    // 4. Replace attributes
    String attributes = this.buildAttributes();
    template = template.replace("!!", attributes);
    
    return template;
}
```

### 3.4 Initialize ListUI Parameters

```java
// FrameList.initListUIParams()
private void initListUIParams(UserXItem item) throws Exception {
    if (!item.checkItemExists("ListUI")) {
        return;
    }
    
    UserXItemValues uv = item.getItem("ListUI");
    if (uv.count() == 0) {
        return;
    }
    
    RequestValue rv = getHtmlClass().getItemValues().getRequestValue();
    UserXItemValue x = uv.getItem(0);
    
    // Sticky headers
    if ("yes".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_STICKY_HEADERS))) {
        this.luStickyHeaders = true;
    } else if (x.checkItemExists("luStickyHeaders")) {
        String v1 = x.getItem("luStickyHeaders");
        if (v1.equals("yes")) {
            this.luStickyHeaders = true;
        }
    }
    
    // Redraw buttons
    if (x.checkItemExists("luButtons") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_BUTTONS)))) {
        String v1 = x.getItem("luButtons");
        if (v1.equals("1")) {
            this._IsLuButtons = true;
        }
    }
    
    // Redraw search
    if (x.checkItemExists("luSearch") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_SEARCH)))) {
        String v1 = x.getItem("luSearch");
        if (v1.equals("1")) {
            this._IsLuSearch = true;
        } else if (v1.equals("2")) {
            this._IsLuSearch = true;
            this._ComposeSearchTexts = true;  // Combined text search
        } else if (v1.equals("3")) {
            this._IsLuSearch = true;
            this._SearchGroup = false;  // Grouped search
        }
    }
    
    // Single/multi select
    if (x.checkItemExists("luSelect") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_SELECT)))) {
        String v1 = x.getItem("luSelect");
        this._LuSelect = v1;  // S=single, M=multi
    }
    
    // Row double-click
    if (x.checkItemExists("luDblClick") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_DBLCLICK)))) {
        String v1 = x.getItem("luDblClick");
        if (v1.equals("1")) {
            this._IsLuDblClick = true;
        }
        this._LuDblClickIdx = x.getItem("luDblClickIdx");  // Associated button
    }
}
```

---

## 4. Generate Frame Initialization Script

```java
// FrameList.createJsFramePage()
public void createJsFramePage() throws Exception {
    RequestValue rv = getHtmlClass().getSysParas().getRequestValue();
    String gunid = getHtmlClass().getSysParas().getFrameUnid();
    String url = super.getUrlJs();
    
    String userSort = rv.getString(FrameParameters.EWA_LF_ORDER);
    if (userSort == null) {
        userSort = "";
    }
    
    MStr sJs = new MStr();
    String lang = getHtmlClass().getSysParas().getLang();
    
    sJs.al("EWA.LANG='" + lang.toLowerCase() + "';");
    sJs.al("(function() {");
    sJs.al(" var o1 = EWA.F.FOS['" + gunid + "'] = new EWA_ListFrameClass();");
    sJs.al(" o1._Id = o1.Id = '" + gunid + "';");
    sJs.al(" o1.Title = \"" + pageDescription + "\";");
    sJs.al(" o1.Init(EWA_ITEMS_XML_" + gunid + ");");
    
    // Pagination parameters
    PageSplit ps = this._PageSplit;
    if (ps == null) {
        ps = new PageSplit(_ListFrameRecordCount, rv, getUserSettingPageSize());
    }
    sJs.al(" o1.SetPageParameters(" + ps.getPageCurrent() + "," 
           + ps.getPageCount() + "," + ps.getPageSize() + "," 
           + ps.getRecordCount() + ");");
    
    // Sort
    sJs.al(" o1.UserSort = '" + userSort + "';");
    
    // Button configuration
    sJs.al(" o1.LuButtons = " + this._IsLuButtons + ";");
    sJs.al(" o1.LuSearch = " + this._IsLuSearch + ";");
    sJs.al(" o1.LuSelect = '" + this._LuSelect + "';");
    sJs.al(" o1.LuDblClick = " + this._IsLuDblClick + ";");
    sJs.al(" o1.LuStickyHeaders = " + this.luStickyHeaders + ";");
    
    // Workflow buttons
    String wfButJson = this.getWorkFlowButJson();
    if (wfButJson != null) {
        sJs.al(" o1.WorkFlowBut = " + wfButJson + ";");
    }
    
    sJs.al("})();");
    
    getHtmlClass().getDocument().addJs("LISTFRAME", sJs.toString(), false);
}
```

---

## 5. Data Export

```java
// ActionListFrame.createDownloadData()
private String createDownloadData(String sql) throws Exception {
    RequestValue rv = getRequestValue();
    String downType = rv.s(FrameParameters.EWA_AJAX_DOWN_TYPE);
    
    // Check allowed export formats
    String allowExport = getPageItemValue("PageSize", "AllowExport");
    if (allowExport == null || allowExport.indexOf(downType.toUpperCase()) < 0) {
        return null;
    }
    
    // Create exporter
    IExport exp = null;
    if (downType.equals("DBF")) {
        exp = new DbfExport();
    } else if (downType.equals("XML")) {
        exp = new XmlExport();
    } else if (downType.equals("TXT")) {
        exp = new TxtExport(getLang());
    } else {
        exp = new ExcelExport(getLang());
    }
    
    // Execute query
    DataConnection cnn = getItemValues().getSysParas().getDataConn();
    cnn.executeQuery(sql);
    ResultSet rs = cnn.getLastResult().getResultSet();
    
    // Export file
    String exportPathAndName = UPath.getPATH_UPLOAD() + "/download_datas/" + rv.s("EWA.ID");
    File ff = exp.export(rs, exportPathAndName);
    
    String name = ff.getAbsolutePath().replace(UPath.getPATH_UPLOAD(), UPath.getPATH_UPLOAD_URL());
    return name.replace("\\", "/");
}
```

---

## 6. Key Parameters

### 6.1 URL Parameters

| Parameter | Description | Example |
|------|------|------|
| `EWA_PAGECUR` | Current page | `EWA_PAGECUR=2` |
| `EWA_PAGESIZE` | Records per page | `EWA_PAGESIZE=50` |
| `EWA_LF_ORDER` | Sort | `EWA_LF_ORDER=create_date DESC` |
| `EWA_SEARCH` | Search condition | `EWA_SEARCH=name[lk]Zhang` |
| `EWA_AJAX` | AJAX type | `EWA_AJAX=JSON` |
| `EWA_AJAX_DOWN_TYPE` | Export format | `EWA_AJAX_DOWN_TYPE=XLS` |
| `EWA_BOX` | BOX mode | `EWA_BOX=1` |
| `EWA_IS_SPLIT_PAGE` | Enable pagination | `EWA_IS_SPLIT_PAGE=no` |
| `EWA_LU_STICKY_HEADERS` | Sticky headers | `EWA_LU_STICKY_HEADERS=yes` |
| `EWA_LU_BUTTONS` | Hide buttons | `EWA_LU_BUTTONS=NO` |
| `EWA_LU_SEARCH` | Hide search | `EWA_LU_SEARCH=NO` |
| `EWA_LU_SELECT` | Select mode | `EWA_LU_SELECT=S` |
| `EWA_LU_DBLCLICK` | Double-click row | `EWA_LU_DBLCLICK=yes` |
| `EWA_ROW_SIGN` | MD5 signature | `EWA_ROW_SIGN=1` |
| `EWA_FRAME_BOX_NO` | No table/tr/td output | `EWA_FRAME_BOX_NO=1` |

### 6.2 XML Configuration

```xml
<PageSize>
    <Set PageSize="20" IsSplitPage="1" KeyField="USER_ID" AllowExport="XLS,DBF"/>
</PageSize>
<ListUI>
    <Set luButtons="1" luSearch="1" luSelect="S" luDblClick="1" 
         luStickyHeaders="yes"/>
</ListUI>
```

---

## 7. Performance Optimization

### 7.1 Paginated Queries

```java
// ✅ Recommended: Use pagination
<PageSize><Set PageSize="20" IsSplitPage="1" KeyField="USER_ID"/></PageSize>

// ❌ Avoid: No pagination
<PageSize><Set IsSplitPage="0"/></PageSize>
```

### 7.2 Sticky Headers

```java
// ✅ Recommended: Sticky headers (large datasets)
EWA_LU_STICKY_HEADERS=yes

// ❌ Avoid: Non-sticky (poor scrolling experience with large data)
```

### 7.3 Export Restrictions

```xml
<!-- ✅ Recommended: Restrict export formats -->
<PageSize><Set AllowExport="XLS"/></PageSize>

<!-- ❌ Avoid: No restrictions -->
<PageSize><Set AllowExport=""/></PageSize>
```

---

## 8. Debugging Tips

### 8.1 Enable Debug

```
EWA_DEBUG_NO=1      # Do not show Debug
EWA_DEBUG_KEY=xxx   # Debug key
EWA_JS_DEBUG=1      # JS debug mode
```

### 8.2 View Executed SQL

```java
// Recorded in DataConnection
String sql = conn.getLastResult().getSqlOrigin();
System.out.println("Original SQL: " + sql);

String executedSql = conn.getLastResult().getSqlExecute();
System.out.println("Executed SQL: " + executedSql);
```

### 8.3 Browser Console

```javascript
// Get ListFrame object
EWA.F.FOS["frame_id"]

// Refresh list
EWA.F.FOS["frame_id"].Reload()

// Get current page
EWA.F.FOS["frame_id"].PageCur

// Get total pages
EWA.F.FOS["frame_id"].PageCount
```

---

## 9. Complete Example

### 9.1 XML Configuration

```xml
<EasyWebTemplate Name="users.LF.M">
    <Page>
        <FrameTag><Set FrameTag="ListFrame"/></FrameTag>
        <Name><Set Name="users.LF.M"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <PageSize>
            <Set PageSize="20" IsSplitPage="1" KeyField="USER_ID" AllowExport="XLS,DBF"/>
        </PageSize>
        <ListUI>
            <Set luButtons="1" luSearch="1" luSelect="S" luDblClick="1" 
                 luStickyHeaders="yes"/>
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
            <OrderSearch>
                <Set SearchType="text"/>
            </OrderSearch>
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

### 9.2 URL Calls

```
# Basic query
/ewa?XMLNAME=users&ITEMNAME=LF.M

# Paginated
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_PAGECUR=2&EWA_PAGESIZE=50

# Sorted
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_LF_ORDER=create_date DESC

# Search
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_SEARCH=user_name[lk]Zhang,email[eq]test@example.com

# Export
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_AJAX=DOWN_DATA&EWA_AJAX_DOWN_TYPE=XLS

# JSON
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_AJAX=JSON
```

---

## Summary

Key points of ListFrame execution flow:

1. **Action Execution** → `ActionListFrame.execute()`
   - Execute SqlSet (paginated query)
   - Execute ClassSet (optional)

2. **SQL Construction** → `createSqListFrame()`
   - Add search conditions (EWA_SEARCH)
   - Add user search (EWA_LF_SEARCH)
   - Add sort (EWA_LF_ORDER)

3. **Paginated Query** → `executeSqlWithPageSplit()`
   - Use PageSplit for pagination
   - Save as ListFrameTable

4. **HTML Rendering** → `FrameList.createHtml()`
   - Create table content
   - Generate row HTML
   - Create pagination bar, button bar, search box

5. **JS Initialization** → `createJsFramePage()`
   - Create EWA_ListFrameClass instance
   - Set pagination parameters, button configuration, etc.
