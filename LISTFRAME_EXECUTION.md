# ListFrame 执行流程详解

## 概述

ListFrame 是 EWA 框架中用于显示数据列表的核心组件，支持分页、搜索、排序、导出等功能。

**核心类**:
- `FrameList` - ListFrame 渲染类
- `ActionListFrame` - ListFrame 动作执行类
- `FrameBase` - Frame 基类

---

## 1. 完整执行流程

```
HTTP 请求 (/ewa?XMLNAME=users&ITEMNAME=LF.M)
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
   ├─► 1. 加载 UserConfig (XML 配置)
   │     └─► 读取 FrameTag = "ListFrame"
   │
   ├─► 2. 创建 FrameList 实例
   │     this._Frame = new FrameList()
   │     this._Action = new ActionListFrame()
   │
   ├─► 3. FrameList.init(HtmlCreator)
   │
   ├─► 4. ActionListFrame.execute()
   │     ├─► 执行 SqlSet (OnPageLoad)
   │     │   └─► executeSplitSql() - 分页查询
   │     └─► 执行 ClassSet (可选)
   │
   └─► 5. FrameList.createHtml()
         ├─► createHtmlTraditional()  // 传统模式
         │   ├─► createSkinTop()      - 皮肤头部
         │   ├─► createCss()          - CSS 样式
         │   ├─► createJsTop()        - 头部 JS
         │   ├─► createContent()      - 主体内容 (表格)
         │   │   └─► createFrameContent()
         │   │       ├─► initListUIParams()  - 初始化重绘参数
         │   │       ├─► 遍历数据行
         │   │       └─► createItemHtmls()   - 生成行 HTML
         │   ├─► createJsFramePage()  - ListFrame 初始化脚本
         │   ├─► createSkinBottom()   - 皮肤底部
         │   └─► createJsBottom()     - 底部 JS
         │
         └─► createHtmlVue()  // Vue 模式 (EWA_VUE=1)
```

---

## 2. ActionListFrame 执行详解

### 2.1 执行入口

```java
// ActionListFrame.execute()
public void execute() throws Exception {
    String actionName = this.getHtmlClass().getSysParas().getActionName();
    
    // 1. 执行 SqlSet
    UserXItemValues sqlset = getUserConfig().getUserActionItem().getItem("SqlSet");
    for (int i = 0; i < sqlset.count(); i++) {
        UserXItemValue sqlItem = sqlset.getItem(i);
        String sqlName = sqlItem.getItem("Name");
        
        // 检查是否执行该 SQL
        String test = sqlItem.getItem("Test");
        if (test != null && !ULogic.runLogic(test)) {
            continue;  // 条件不满足，跳过
        }
        
        this.executeCallSql(sqlName);
    }
    
    // 2. 执行 ClassSet (可选)
    UserXItemValues classset = getUserConfig().getUserActionItem().getItem("ClassSet");
    for (int i = 0; i < classset.count(); i++) {
        UserXItemValue classItem = classset.getItem(i);
        String className = classItem.getItem("ClassName");
        String methodName = classItem.getItem("MethodName");
        
        // 调用 Java 类
        this.executeCallClass(className, methodName);
    }
}
```

### 2.2 分页查询执行

```java
// ActionListFrame.executeCallSql()
public void executeCallSql(String name) throws Exception {
    UserXItemValues sqlset = getUserConfig().getUserActionItem().getItem("SqlSet");
    UserXItemValue sqlItem = sqlset.getItem(name);
    String sqlExp = sqlItem.getItem("Sql");
    String[] sqlArray = sqlExp.split(";");
    
    DataConnection conn = getItemValues().getSysParas().getDataConn();
    RequestValue rv = getRequestValue();
    
    boolean executedSplitSql = false;  // 分页只执行 1 次
    
    for (int i = 0; i < sqlArray.length; i++) {
        String sql = sqlArray[i].trim();
        if (sql.length() == 0) continue;
        
        String sqlType = sqlItem.getItem("SqlType");  // query / update
        
        // 检查是否为第一个 SELECT 查询（需要分页）
        if (sqlType.equals("query") 
            && !executedSplitSql 
            && DataConnection.checkIsSelect(sql)
            && sql.indexOf("EWA_ERR_OUT") == -1
            && sql.indexOf("EWA_SQL_SPLIT_NO") == -1) {
            
            executedSplitSql = true;
            this.executeSplitSql(sql, conn, rv, name);
        } else {
            // 其他 SQL 直接执行（不分页）
            super.executeSql(sql, sqlType, name);
        }
        
        // 检查错误
        if (StringUtils.isNotBlank(conn.getErrorMsg())) {
            throw new Exception(conn.getErrorMsg());
        }
    }
}

// ActionListFrame.executeSplitSql() - 执行分页查询
private void executeSplitSql(String sql, DataConnection conn, RequestValue rv, String name) {
    // 1. 检查 SQL 是否包含 WHERE
    if (sql.toUpperCase().indexOf("WHERE") < 0) {
        throw new Exception("查询语句中应包含 WHERE 条件");
    }
    
    // 2. 构建完整 SQL（添加搜索、排序条件）
    String sql1 = this.createSqListFrame(sql, conn);
    
    // 3. 检查下载模式
    String ajax = rv.getString(FrameParameters.EWA_AJAX);
    if (ajax != null && ajax.trim().toUpperCase().equals("DOWN_DATA")) {
        this.createDownloadData(sql1);  // 导出数据
        return;
    }
    
    // 4. 执行分页查询
    boolean useSplit = rv.getString(FrameParameters.EWA_PAGESIZE) != null;
    DTTable tb = this.executeSqlWithPageSplit(sql1, conn, rv, useSplit);
    tb.setName(name);
    
    if (tb.isOk()) {
        super.getDTTables().add(tb);
    }
}

// ActionListFrame.executeSqlWithPageSplit() - 执行分页查询
private DTTable executeSqlWithPageSplit(String sql1, DataConnection conn, 
                                         RequestValue rv, boolean useSplit) {
    PageSplit ps = null;
    DTTable tb = null;
    
    if (useSplit) {
        // 分页查询
        int iPageSize = this.getUserSettingPageSize();  // 默认 20
        ps = new PageSplit(0, rv, iPageSize);
        
        String keyField = this.getPageItemValue("PageSize", "KeyField");
        
        tb = DTTable.getJdbcTable(sql1, keyField, ps.getPageSize(), 
                                   ps.getPageCurrent(), conn);
    } else {
        // 不分页，查询全部
        tb = DTTable.getJdbcTable(sql1, conn);
    }
    
    if (tb.isOk()) {
        // 标记为分页查询结果
        tb.getAttsTable().add(EXECUTE_SPLIT_SQL, "1");
        tb.getAttsTable().add(SPLIT_SQL, sql1);
        tb.getAttsTable().add(PAGE_SIZE, ps);
        
        // 保存为 ListFrame 表
        getItemValues().setListFrameTable(tb);
    }
    
    return tb;
}
```

### 2.3 构建 SQL（搜索 + 排序）

```java
// ActionListFrame.createSqListFrame()
private String createSqListFrame(String sql, DataConnection conn) throws Exception {
    // 1. 添加搜索条件
    String searchSql = this.createSqlSearchInit(conn);
    if (searchSql.trim().length() > 0) {
        sql = this.insertWhereCondition(sql, searchSql);
    }
    
    // 2. 添加用户搜索条件 (EWA_LF_SEARCH)
    String userSearch = this.createSqlSearch(conn);
    if (userSearch.trim().length() > 0) {
        sql = this.insertWhereCondition(sql, userSearch);
    }
    
    // 3. 添加排序条件
    String orderSql = this.createSqlOrder();
    if (orderSql.trim().length() > 0) {
        sql += " ORDER BY " + orderSql;
    }
    
    return sql;
}

// 添加搜索条件
private String createSqlSearchInit(DataConnection conn) throws Exception {
    RequestValue rv = getItemValues().getRequestValue();
    String ewa_search = rv.getString(FrameParameters.EWA_SEARCH);
    
    if (ewa_search == null || ewa_search.trim().length() == 0) {
        return "";
    }
    
    MStr sb = new MStr();
    sb.al("(100 = 100 ");  // 始终为真的条件
    
    // 解析搜索参数：ewa_search=name[lk]张，email[eq]test@example.com
    String[] para = ewa_search.split(",");
    for (int i = 0; i < para.length; i++) {
        SearchParameterInit lsp = new SearchParameterInit(para[i]);
        if (!lsp.isValid()) continue;
        
        // 检查字段是否在配置中存在
        if (!getUserConfig().getUserXItems().testName(lsp.getFieldName())) {
            continue;
        }
        
        UserXItem uxi = getUserConfig().getUserXItems().getItem(lsp.getFieldName());
        String dataField = uxi.getItem("DataItem").getItem(0).getItem("DataField");
        
        // 根据搜索类型生成 SQL
        String searchType = lsp.getSearchType().toLowerCase();
        switch (searchType) {
            case "lk":  // 包含
                sb.al(" AND " + dataField + " LIKE '%" + lsp.getValue() + "%'");
                break;
            case "llk": // 左包含
                sb.al(" AND " + dataField + " LIKE '" + lsp.getValue() + "%'");
                break;
            case "rlk": // 右包含
                sb.al(" AND " + dataField + " LIKE '%" + lsp.getValue() + "'");
                break;
            case "eq":  // 等于
                sb.al(" AND " + dataField + " = '" + lsp.getValue() + "'");
                break;
            case "or":  // 或
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

// 添加排序条件
private String createSqlOrder() throws Exception {
    RequestValue rv = getItemValues().getRequestValue();
    String userOrder = rv.getString(FrameParameters.EWA_LF_ORDER);
    
    if (userOrder == null || userOrder.trim().length() == 0) {
        return "";
    }
    
    // 解析排序字段：create_date DESC
    String fieldName = userOrder.split(" ")[0].trim();
    String ascDesc = userOrder.split(" ")[1].trim().toUpperCase();
    
    // 获取字段配置
    UserXItem uxi = getUserConfig().getUserXItems().getItem(fieldName);
    String dataField = uxi.getItem("DataItem").getItem(0).getItem("DataField");
    String dataType = uxi.getItem("DataItem").getItem(0).getItem("DataType");
    
    // 中文排序处理
    DataConnection conn = getItemValues().getSysParas().getDataConn();
    String dbType = conn.getDatabaseType();
    String orderField = SqlUtils.replaceChnOrder(dbType, dataField, dataType);
    
    // 默认倒序
    if (ascDesc.equals("ASC")) {
        orderField += " ASC";
    } else {
        orderField += " DESC";
    }
    
    return orderField;
}
```

---

## 3. FrameList 渲染详解

### 3.1 createHtml 流程

```java
// FrameList.createHtml()
public void createHtml() throws Exception {
    if (super.getHtmlClass().getSysParas().isVue()) {
        super.createHtmlVue();  // Vue 模式
    } else {
        this.createHtmlTraditional();  // 传统模式
    }
}

// FrameList.createHtmlTraditional()
public void createHtmlTraditional() {
    try {
        super.createSkinTop();      // 皮肤头部
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
    }
    
    try {
        super.createCss();          // CSS 样式
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
    }
    
    try {
        super.createJsTop();        // 头部 JS
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
    }
    
    // 主体内容
    try {
        String box = rv.getString(FrameParameters.EWA_BOX);
        String left = rv.getString(FrameParameters.EWA_LEFT);
        
        if (box == null && left == null) {
            doc.addScriptHtml(isNotUsingFrameBox ? "" : "<div>");
            this.createContent();
            doc.addScriptHtml(isNotUsingFrameBox ? "" : "</div>");
        } else {
            // BOX 模式重绘
            this.createJsonFrame();
            String pageAddTop = this.getPageItemValue("AddHtml", "Top");
            doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop.trim());
        }
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
    
    // 底部
    try {
        this.createSkinBottom();    // 皮肤底部
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
    
    try {
        this.createJsBottom();      // 底部 JS
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
    
    try {
        this.createJsFramePage();   // Frame 初始化脚本
    } catch (Exception err) {
        LOGGER.error(err.getMessage());
    }
}
```

### 3.2 创建表格内容

```java
// FrameList.createFrameContent()
public void createFrameContent() throws Exception {
    UserConfig uc = getHtmlClass().getUserConfig();
    UserXItem item = uc.getUserPageItem();
    
    // 1. 初始化列表重绘参数
    this.initListUIParams(item);
    
    RequestValue rv = getHtmlClass().getSysParas().getRequestValue();
    boolean isApp = rv.s(FrameParameters.EWA_APP) != null;  // App 调用
    boolean ewaRowSign = Utils.cvtBool(rv.s(FrameParameters.EWA_ROW_SIGN));  // MD5 签名
    
    HtmlDocument doc = getHtmlClass().getDocument();
    
    // 2. 皮肤定义的头部
    if (!isNotUsingFrameBox) {
        doc.addScriptHtml("<div>");
        String top = super.createSkinFCTop();
        
        // 添加鼠标滑出事件
        if (!isApp) {
            String fos = "EWA.F.FOS[\"" + getFrameUnid() + "\"]";
            top = top.replace("<table", 
                "<table onmouseout='if(window.EWA && EWA.F && EWA.F.FOS && " 
                + fos + "){" + fos + ".MOut(event)}'");
        }
        doc.addScriptHtml(top);
    }
    
    // 3. Frame 定义的页头（搜索框、按钮栏）
    String frameHeader = this.createFrameHeader();
    doc.addScriptHtml(frameHeader);
    
    // 4. 获取数据表
    MList tbs = getHtmlClass().getAction().getDTTables();
    if (tbs == null || tbs.size() == 0) {
        createItemHtmls(false);  // 无数据
        doc.addScriptHtml("<!-- no data -->");
    } else {
        DTTable tb = this.getSplitPageTable();
        if (tb == null) {
            tb = (DTTable) tbs.get(tbs.size() - 1);
        }
        
        getItemValues().setListFrameTable(tb);
        
        // 5. 分页处理
        if (this.isSplitPage()) {
            this.queryRecords();  // 获取记录总数
        }
        
        // 6. 分栏数
        int colSize = 1;
        String s1 = getPageItemValue("PageSize", "ColSize");
        if (s1 != null && s1.trim().length() > 0) {
            try {
                colSize = Integer.parseInt(s1);
            } catch (Exception e) {}
        }
        
        // 7. 分组处理
        FrameListGroup flGroup = null;
        if (this._GroupUserXItem != null) {
            flGroup = new FrameListGroup();
            flGroup.init(this._GroupUserXItem);
            flGroup.setColSpan(tb.getColumns().getCount());
            flGroup.setFrameGUID(getFrameUnid());
        }
        
        // 8. 遍历数据行
        MStr sb = new MStr();
        boolean isUseTemplate = this.isUsingTemplate();
        String frameTemplate = getPageItemValue("FrameHtml", "FrameHtml");
        
        for (int i = 0; i < tb.getCount(); i++) {
            tb.getRow(i);  // 移动当前行
            
            // 生成行 HTML
            String rowHtml = isUseTemplate 
                ? this.createItemHtmlsByFrameHtml(frameTemplate, "FrameList")
                : this.createItemHtmls();
            
            if (isNotUsingFrameBox) {
                sb.a(rowHtml);
                continue;
            }
            
            // 添加主键表达式
            String keyExp = "EWA_KEY=\"" + this.createItemKeys() + "\" ";
            
            // 处理分组
            if (flGroup != null) {
                String grpValue = tb.getCell(i, this._GroupUserXItem.getDataField()).toString();
                if (flGroup.getGrpValue() == null || !flGroup.getGrpValue().equals(grpValue)) {
                    // 新分组
                    String grpHtml = this.createFrameGroup(flGroup);
                    sb.a(grpHtml);
                    flGroup.setGrpValue(grpValue);
                }
            }
            
            // 添加行
            sb.a("<tr id='R_" + i + "' " + keyExp);
            
            // MD5 签名
            if (ewaRowSign) {
                String md5 = Utils.md5(rowHtml);
                sb.a(" data-md5='" + md5 + "'");
            }
            
            sb.a(">" + rowHtml + "</tr>");
        }
        
        // 9. 创建表格
        if (!isNotUsingFrameBox) {
            doc.addScriptHtml("<table class='EWA_LISTFRAME'>" + sb.toString() + "</table>");
            
            // 分页栏
            this.createPageBar(doc);
            
            // 按钮栏
            this.createButtonBar(doc);
            
            // 搜索框
            this.createSearchBox(doc);
            
            doc.addScriptHtml("</div>");  // 关闭皮肤头部
        } else {
            doc.addScriptHtml(sb.toString());
        }
    }
    
    // 10. 用户自定义底部 HTML
    String pageAddBottom = getPageItemValue("AddHtml", "Bottom");
    if (pageAddBottom != null && pageAddBottom.trim().length() > 0) {
        doc.addScriptHtml(pageAddBottom);
    }
}
```

### 3.3 生成行 HTML

```java
// FrameList.createItemHtmls()
public String createItemHtmls() throws Exception {
    return createItemHtmls(false);
}

public String createItemHtmls(boolean isCreateScript) throws Exception {
    MStr sb = new MStr();
    UserConfig uc = getHtmlClass().getUserConfig();
    String lang = getHtmlClass().getSysParas().getLang();
    
    // 遍历 XItems
    for (int i = 0; i < uc.getUserXItems().count(); i++) {
        UserXItem uxi = uc.getUserXItems().getItem(i);
        String name = uxi.getName().toUpperCase().trim();
        
        // 跳过隐藏字段
        if (getHtmlClass().getSysParas().isHiddenColumn(name)
            || getHtmlClass().getFrame().isHiddenField(name)) {
            continue;
        }
        
        // 获取 Tag 类型
        String tag = uxi.getSingleValue("Tag");
        if (tag == null || tag.trim().length() == 0) {
            continue;
        }
        
        // 创建 Item 实例
        IItem item = ItemFactory.createItem(tag, uxi);
        
        // 渲染 Item
        String html = item.render();
        
        // 添加到单元格
        sb.a("<td class='EWA_TD_M'>" + html + "</td>");
    }
    
    return sb.toString();
}

// Item 渲染示例 (ItemBase.render())
public String render() throws Exception {
    // 1. 获取 HTML 模板（来自 EwaConfig.xml）
    String template = this.getHtmlTemplate();
    
    // 2. 获取字段值
    String value = this.getValue();
    
    // 3. 替换模板变量
    template = template.replace("{__EWA_VAL__}", value);
    template = template.replace("@NAME", this.getXItem().getName());
    
    // 4. 替换属性
    String attributes = this.buildAttributes();
    template = template.replace("!!", attributes);
    
    return template;
}
```

### 3.4 初始化 ListUI 参数

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
    
    // 固定头部 (sticky headers)
    if ("yes".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_STICKY_HEADERS))) {
        this.luStickyHeaders = true;
    } else if (x.checkItemExists("luStickyHeaders")) {
        String v1 = x.getItem("luStickyHeaders");
        if (v1.equals("yes")) {
            this.luStickyHeaders = true;
        }
    }
    
    // 重绘按钮
    if (x.checkItemExists("luButtons") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_BUTTONS)))) {
        String v1 = x.getItem("luButtons");
        if (v1.equals("1")) {
            this._IsLuButtons = true;
        }
    }
    
    // 重绘搜索
    if (x.checkItemExists("luSearch") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_SEARCH)))) {
        String v1 = x.getItem("luSearch");
        if (v1.equals("1")) {
            this._IsLuSearch = true;
        } else if (v1.equals("2")) {
            this._IsLuSearch = true;
            this._ComposeSearchTexts = true;  // 合并文字搜索
        } else if (v1.equals("3")) {
            this._IsLuSearch = true;
            this._SearchGroup = false;  // 分组搜索
        }
    }
    
    // 单选/多选
    if (x.checkItemExists("luSelect") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_SELECT)))) {
        String v1 = x.getItem("luSelect");
        this._LuSelect = v1;  // S=单选，M=多选
    }
    
    // 行双击
    if (x.checkItemExists("luDblClick") 
        && !("no".equalsIgnoreCase(rv.s(FrameParameters.EWA_LU_DBLCLICK)))) {
        String v1 = x.getItem("luDblClick");
        if (v1.equals("1")) {
            this._IsLuDblClick = true;
        }
        this._LuDblClickIdx = x.getItem("luDblClickIdx");  // 关联按钮
    }
}
```

---

## 4. 生成 Frame 初始化脚本

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
    
    // 分页参数
    PageSplit ps = this._PageSplit;
    if (ps == null) {
        ps = new PageSplit(_ListFrameRecordCount, rv, getUserSettingPageSize());
    }
    sJs.al(" o1.SetPageParameters(" + ps.getPageCurrent() + "," 
           + ps.getPageCount() + "," + ps.getPageSize() + "," 
           + ps.getRecordCount() + ");");
    
    // 排序
    sJs.al(" o1.UserSort = '" + userSort + "';");
    
    // 按钮配置
    sJs.al(" o1.LuButtons = " + this._IsLuButtons + ";");
    sJs.al(" o1.LuSearch = " + this._IsLuSearch + ";");
    sJs.al(" o1.LuSelect = '" + this._LuSelect + "';");
    sJs.al(" o1.LuDblClick = " + this._IsLuDblClick + ";");
    sJs.al(" o1.LuStickyHeaders = " + this.luStickyHeaders + ";");
    
    // 工作流按钮
    String wfButJson = this.getWorkFlowButJson();
    if (wfButJson != null) {
        sJs.al(" o1.WorkFlowBut = " + wfButJson + ";");
    }
    
    sJs.al("})();");
    
    getHtmlClass().getDocument().addJs("LISTFRAME", sJs.toString(), false);
}
```

---

## 5. 数据导出

```java
// ActionListFrame.createDownloadData()
private String createDownloadData(String sql) throws Exception {
    RequestValue rv = getRequestValue();
    String downType = rv.s(FrameParameters.EWA_AJAX_DOWN_TYPE);
    
    // 检查允许的导出格式
    String allowExport = getPageItemValue("PageSize", "AllowExport");
    if (allowExport == null || allowExport.indexOf(downType.toUpperCase()) < 0) {
        return null;
    }
    
    // 创建导出器
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
    
    // 执行查询
    DataConnection cnn = getItemValues().getSysParas().getDataConn();
    cnn.executeQuery(sql);
    ResultSet rs = cnn.getLastResult().getResultSet();
    
    // 导出文件
    String exportPathAndName = UPath.getPATH_UPLOAD() + "/download_datas/" + rv.s("EWA.ID");
    File ff = exp.export(rs, exportPathAndName);
    
    String name = ff.getAbsolutePath().replace(UPath.getPATH_UPLOAD(), UPath.getPATH_UPLOAD_URL());
    return name.replace("\\", "/");
}
```

---

## 6. 关键参数

### 6.1 URL 参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_PAGECUR` | 当前页 | `EWA_PAGECUR=2` |
| `EWA_PAGESIZE` | 每页记录数 | `EWA_PAGESIZE=50` |
| `EWA_LF_ORDER` | 排序 | `EWA_LF_ORDER=create_date DESC` |
| `EWA_SEARCH` | 搜索条件 | `EWA_SEARCH=name[lk]张` |
| `EWA_AJAX` | AJAX 类型 | `EWA_AJAX=JSON` |
| `EWA_AJAX_DOWN_TYPE` | 导出格式 | `EWA_AJAX_DOWN_TYPE=XLS` |
| `EWA_BOX` | BOX 模式 | `EWA_BOX=1` |
| `EWA_IS_SPLIT_PAGE` | 是否分页 | `EWA_IS_SPLIT_PAGE=no` |
| `EWA_LU_STICKY_HEADERS` | 固定表头 | `EWA_LU_STICKY_HEADERS=yes` |
| `EWA_LU_BUTTONS` | 不显示按钮 | `EWA_LU_BUTTONS=NO` |
| `EWA_LU_SEARCH` | 不显示搜索 | `EWA_LU_SEARCH=NO` |
| `EWA_LU_SELECT` | 选择模式 | `EWA_LU_SELECT=S` |
| `EWA_LU_DBLCLICK` | 双击行 | `EWA_LU_DBLCLICK=yes` |
| `EWA_ROW_SIGN` | MD5 签名 | `EWA_ROW_SIGN=1` |
| `EWA_FRAME_BOX_NO` | 不输出 table/tr/td | `EWA_FRAME_BOX_NO=1` |

### 6.2 XML 配置

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

## 7. 性能优化

### 7.1 分页查询

```java
// ✅ 推荐：使用分页
<PageSize><Set PageSize="20" IsSplitPage="1" KeyField="USER_ID"/></PageSize>

// ❌ 避免：不分页
<PageSize><Set IsSplitPage="0"/></PageSize>
```

### 7.2 固定表头

```java
// ✅ 推荐：固定表头（大数据量）
EWA_LU_STICKY_HEADERS=yes

// ❌ 避免：不固定（大数据量滚动体验差）
```

### 7.3 导出限制

```xml
<!-- ✅ 推荐：限制导出格式 -->
<PageSize><Set AllowExport="XLS"/></PageSize>

<!-- ❌ 避免：不限制 -->
<PageSize><Set AllowExport=""/></PageSize>
```

---

## 8. 调试技巧

### 8.1 启用 Debug

```
EWA_DEBUG_NO=1      # 不显示 Debug
EWA_DEBUG_KEY=xxx   # Debug 密钥
EWA_JS_DEBUG=1      # JS 调试模式
```

### 8.2 查看执行 SQL

```java
// DataConnection 中记录
String sql = conn.getLastResult().getSqlOrigin();
System.out.println("原始 SQL: " + sql);

String executedSql = conn.getLastResult().getSqlExecute();
System.out.println("执行 SQL: " + executedSql);
```

### 8.3 浏览器控制台

```javascript
// 获取 ListFrame 对象
EWA.F.FOS["frame_id"]

// 刷新列表
EWA.F.FOS["frame_id"].Reload()

// 获取当前页
EWA.F.FOS["frame_id"].PageCur

// 获取总页数
EWA.F.FOS["frame_id"].PageCount
```

---

## 9. 完整示例

### 9.1 XML 配置

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

### 9.2 URL 调用

```
# 基本查询
/ewa?XMLNAME=users&ITEMNAME=LF.M

# 分页
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_PAGECUR=2&EWA_PAGESIZE=50

# 排序
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_LF_ORDER=create_date DESC

# 搜索
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_SEARCH=user_name[lk]张，email[eq]test@example.com

# 导出
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_AJAX=DOWN_DATA&EWA_AJAX_DOWN_TYPE=XLS

# JSON
/ewa?XMLNAME=users&ITEMNAME=LF.M&EWA_AJAX=JSON
```

---

## 总结

ListFrame 执行流程关键点：

1. **Action 执行** → `ActionListFrame.execute()`
   - 执行 SqlSet（分页查询）
   - 执行 ClassSet（可选）

2. **SQL 构建** → `createSqListFrame()`
   - 添加搜索条件（EWA_SEARCH）
   - 添加用户搜索（EWA_LF_SEARCH）
   - 添加排序（EWA_LF_ORDER）

3. **分页查询** → `executeSqlWithPageSplit()`
   - 使用 PageSplit 分页
   - 保存为 ListFrameTable

4. **HTML 渲染** → `FrameList.createHtml()`
   - 创建表格内容
   - 生成行 HTML
   - 创建分页栏、按钮栏、搜索框

5. **JS 初始化** → `createJsFramePage()`
   - 创建 EWA_ListFrameClass 实例
   - 设置分页参数、按钮配置等
