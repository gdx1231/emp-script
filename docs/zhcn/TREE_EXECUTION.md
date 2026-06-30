# Tree 执行流程详解

## 概述

Tree 是 EWA 框架中用于显示树形结构数据的组件，支持分层加载、节点展开/折叠、右键菜单等功能。

**核心类**:
- `FrameTree` - Tree 渲染类
- `ActionTree` - Tree 动作执行类
- `TreeViewMain` - Tree 视图主类
- `TreeViewNode` - Tree 节点类

---

## 1. 完整执行流程

```
HTTP 请求 (/ewa?XMLNAME=categories&ITEMNAME=T.V)
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
   │     └─► 读取 FrameTag = "Tree"
   │
   ├─► 2. 创建 FrameTree 实例
   │     this._Frame = new FrameTree()
   │     this._Action = new ActionTree()
   │
   ├─► 3. FrameTree.init(HtmlCreator)
   │
   ├─► 4. ActionTree.execute()
   │     ├─► 执行 SqlSet (OnPageLoad)
   │     │   └─► createSql() - 生成分级加载 SQL
   │     └─► 执行 ClassSet (可选)
   │
   └─► 5. FrameTree.createHtml()
         ├─► createSkinTop()      - 皮肤头部
         ├─► createCss()          - CSS 样式 (包括图标 CSS)
         ├─► createJsTop()        - 头部 JS
         ├─► createContent()      - 主体内容 (树)
         │   ├─► createFrameContent()
         │   │   ├─► 创建 TreeViewMain
         │   │   ├─► initCreateAddCols() - 初始化附加字段
         │   │   └─► createTreeHtml() - 生成树 HTML
         │   │       ├─► createTreeNodes() - 构建节点关系
         │   │       └─► createHtml() - 递归生成 HTML
         │   └─► createFrameHeader/Footer()
         ├─► createJsFramePage()  - Tree 初始化脚本
         ├─► createSkinBottom()   - 皮肤底部
         └─► createJsBottom()     - 底部 JS
```

---

## 2. ActionTree 执行详解

### 2.1 执行入口

```java
// ActionTree.executeSql()
public boolean executeSql(String sql, String sqlType, String name) {
    boolean isSelect = DataConnection.checkIsSelect(sql);
    
    if (sqlType.equals("procedure")) {
        // 存储过程
        this.executeSqlProcdure(sql);
        return StringUtils.isBlank(this.getChkErrorMsg());
    } else if (isSelect) {
        // 查询
        try {
            sql = this.createSql(sql);  // 生成分级加载 SQL
        } catch (Exception e) {
            LOGGER.error("{0}, {1}, {2}, {3}", sql, sqlType, name, e.getMessage());
        }
        
        DTTable dt = this.executeSqlQuery(sql);
        dt.setName(name);
        return StringUtils.isBlank(this.getChkErrorMsg());
    } else {
        // 更新
        this.executeSqlUpdate(sql);
        return true;
    }
}
```

### 2.2 生成分级加载 SQL

```java
// ActionTree.createSql() - 生成分级加载 SQL
public String createSql(String sql) throws Exception {
    UserConfig uc = super.getUserConfig();
    UserXItemValues u = uc.getUserPageItem().getItem("Tree");
    RequestValue rv = super.getRequestValue();
    
    if (u.count() == 0) {
        return sql;  // 无 Tree 配置，返回原 SQL
    }
    
    UserXItemValue v = u.getItem(0);
    
    // 检查是否分层加载
    if (!v.getItem("LoadByLevel").equals("1")) {
        return sql;
    } 
    // 获取更多子节点
    else if ("1".equals(rv.getString(FrameParameters.EWA_TREE_MORE))) {
        return this.createLoadByLevelMore(sql, u);
    } 
    // 获取树状态（展开的节点）
    else if ("1".equals(rv.getString(FrameParameters.EWA_TREE_STATUS))) {
        String s1 = this.createStatusFindPkeys(sql, u);
        if (s1 == null) {
            return "";
        } else {
            String s2 = this.createSqlLoadByLevel(s1, u);
            return s2;
        }
    } 
    // 默认分级加载
    else {
        return this.createSqlLoadByLevel(sql, u);
    }
}

// ActionTree.createSqlLoadByLevel() - 生成分级加载 SQL
private String createSqlLoadByLevel(String sql, UserXItemValues u) throws Exception {
    UserXItemValue v = u.getItem(0);
    SqlPart sp = new SqlPart();
    sp.setSql(sql);
    
    String key = v.getItem("Key");         // 主键字段
    String pkey = v.getItem("ParentKey");  // 父键字段
    
    // 原始查询
    String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() 
              + " WHERE " + sp.getWhere();
    
    // 统计每个父节点的子节点数量
    String s2 = "SELECT " + pkey + " EWAPID, COUNT(*) EWAMORECNT " 
              + "FROM " + sp.getTableName() + " GROUP BY " + pkey;
    
    // LEFT JOIN 统计结果
    String s1 = "SELECT A.*, B.EWAMORECNT FROM (" + s3 + ") A " 
              + "\r\n LEFT JOIN (" + s2 + ") B ON A." + key + "=B.EWAPID ";
    
    if (sp.getOrderBy() != null) {
        s1 += " ORDER BY " + sp.getOrderBy();
    }
    
    return s1;
}

// ActionTree.createLoadByLevelMore() - 获取更多子节点
private String createLoadByLevelMore(String sql, UserXItemValues u) throws Exception {
    UserXItemValue v = u.getItem(0);
    SqlPart sp = new SqlPart();
    sp.setSql(sql);
    
    String key = v.getItem("Key");
    String pkey = v.getItem("ParentKey");
    
    // 查询指定父节点的子节点
    String s3 = "SELECT " + sp.getFields() + " FROM " + sp.getTableName() 
              + " WHERE " + pkey + "=@" + key;
    
    // 统计子节点数量
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

// ActionTree.createStatusFindPkeys() - 查找展开节点的所有父节点
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
        if (m > 20) {  // 防止死循环
            return null;
        }
        if (keys.contains(keyValue)) {
            break;
        }
        keys.add(keyValue);
        
        // 递归查找父节点
        String s2 = s1 + keyValue.replace("'", "''") + "'";
        keyValue = this.createStatusFindPkey(s2);
    }
    
    if (keys.size() <= 1) {
        return null;  // 指定的 key 值不对
    }
    
    // 构建 IN 查询
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

## 3. TreeViewMain 详解

### 3.1 初始化参数

```java
// TreeViewMain.initParameters()
private void initParameters() throws Exception {
    /*
     * XML 配置示例:
     * <Tree>
     *   <Set Key="category_id" LoadByLevel="1" ParentKey="parent_id" 
     *        Text="category_name" MenuGroup="menu_grp" Title="memo"
     *        AddPara1="sort_id" AddPara2="status" AddPara3=""/>
     * </Tree>
     */
    _AddParasName = new ArrayList<String>();
    UserXItem userXitem = this._UserConfig.getUserPageItem();
    
    // 树配置字段
    this._FieldKey = userXitem.getSingleValue("Tree", "Key");         // 主键
    this._FieldDispVal = userXitem.getSingleValue("Tree", "Text");    // 显示文本
    this._FieldParentKey = userXitem.getSingleValue("Tree", "ParentKey"); // 父键
    this._FieldMenuGroup = userXitem.getSingleValue("Tree", "MenuGroup"); // 菜单组
    this._FieldTitle = userXitem.getSingleValue("Tree", "Title");     // 提示信息
    
    // 附加参数
    String addPara1 = userXitem.getSingleValue("Tree", "AddPara1");
    String addPara2 = userXitem.getSingleValue("Tree", "AddPara2");
    String addPara3 = userXitem.getSingleValue("Tree", "AddPara3");
    _AddParasName.add(addPara1);
    _AddParasName.add(addPara2);
    _AddParasName.add(addPara3);
    
    // 是否分层加载
    String s1 = userXitem.getSingleValue("Tree", "LoadByLevel");
    this._IsLoadByLevel = (s1 != null && s1.equals("1"));
    
    // 获取标题（多语言）
    this._Caption = HtmlUtils.getDescription(
        userXitem.getItem("DescriptionSet"), "Info", this._Lang);
}
```

### 3.2 构建树节点

```java
// TreeViewMain.createTreeNodes() - 构建树节点
public TreeViewNode createTreeNodes(DTTable table) throws Exception {
    TreeViewNode nodeRoot = new TreeViewNode();
    
    // 初始化根节点
    nodeRoot.setDispVal(this._Caption);
    nodeRoot.setKey("");
    
    // 键值对应的 TreeViewNode
    HashMap<String, TreeViewNode> nodesHsahMap = new HashMap<String, TreeViewNode>();
    // 按照顺序加载列表
    List<TreeViewNode> nodesList = new ArrayList<TreeViewNode>();
    
    nodesHsahMap.put(nodeRoot.getKey(), nodeRoot);
    nodeRoot.setParentKey(null);
    
    // 遍历数据表，创建节点
    for (int i = 0; i < table.getCount(); i++) {
        DTRow row = table.getRow(i);
        this.createTreeNode(row, nodesHsahMap, nodesList);
    }
    
    // 构建父子关系
    this.bulidFatherAndChildren(nodesHsahMap, nodesList);
    
    nodeRoot.setKey(TreeViewNode.NODE_ROOT_KEY);
    return nodeRoot;
}

// TreeViewMain.createTreeNode() - 创建单个节点
private void createTreeNode(DTRow row, HashMap<String, TreeViewNode> nodesHsahMap, 
                            List<TreeViewNode> nodesList) throws Exception {
    // 获取父键和主键
    Object oId = row.getCell(this._FieldParentKey).getValue();
    Object oKey = row.getCell(this._FieldKey).getValue();
    String pid = oId == null ? null : oId.toString();
    String key = oKey == null ? "null" : oKey.toString();
    
    // 主键和父键一致，弃用
    if (pid != null && pid.equals(key)) {
        LOGGER.warn("主键和父键一致，弃用" + pid);
        return;
    }
    
    // 根节点处理
    if (pid == null || pid.equals("0") || pid.trim().equals("") 
        || pid.equals(this._RootId)) {
        pid = "";
    }
    
    // 获取显示值
    String dispVal = row.getCell(this._FieldDispVal).toString();
    if (dispVal == null) {
        dispVal = "[NULL]";
    }
    
    // 获取菜单组
    String menuGroup = "";
    if (this._FieldMenuGroup != null && this._FieldMenuGroup.trim().length() > 0) {
        menuGroup = row.getCell(this._FieldMenuGroup).toString();
        if (menuGroup == null) {
            menuGroup = "";
        }
    }
    
    // 获取提示信息
    String title = "";
    if (this._FieldTitle != null && this._FieldTitle.trim().length() > 0) {
        title = row.getCell(this._FieldTitle).toString();
        if (title == null) {
            title = "";
        }
    }
    
    // 创建节点
    TreeViewNode tvNode = this.createTreeNode(key, pid, dispVal, menuGroup, 
                                               "link", title, nodesHsahMap);
    
    // 主键和根节点值一致，弃用
    if (key.equals(this._RootId)) {
        LOGGER.warn("主键和主节点的值一致，弃用：" + tvNode);
        return;
    }
    
    nodesHsahMap.put(tvNode.getKey(), tvNode);
    nodesList.add(tvNode);
    
    // 设置关联数据
    tvNode.setData(row);
    
    // 设置附加参数
    for (int i = 0; i < _AddParasName.size(); i++) {
        String addParaName = _AddParasName.get(i);
        if (addParaName == null || addParaName.trim().length() == 0) {
            tvNode.getAddParas().add(null);
        } else {
            tvNode.getAddParas().add(row.getCell(addParaName).getString());
        }
    }
    
    // 分层加载：检查是否有更多子节点
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

// TreeViewMain.createTreeNode() - 创建节点对象
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

// TreeViewMain.bulidFatherAndChildren() - 构建父子关系
private void bulidFatherAndChildren(HashMap<String, TreeViewNode> nodesHsahMap, 
                                     List<TreeViewNode> nodesList) {
    for (int i = 0; i < nodesList.size(); i++) {
        TreeViewNode tvNode = nodesList.get(i);
        String parentKey = tvNode.getParentKey();
        
        if (!nodesHsahMap.containsKey(parentKey)) {
            // 父节点不存在，孤立节点
            LOGGER.warn("孤立的节点" + tvNode);
            continue;
        }
        
        TreeViewNode nodeParent = nodesHsahMap.get(tvNode.getParentKey());
        
        // 设置前节点/后节点（用于键盘导航）
        if (nodeParent.getChildNodes().size() > 0) {
            TreeViewNode prevNode = nodeParent.getChildNodes()
                .get(nodeParent.getChildNodes().size() - 1);
            tvNode.setPrevNode(prevNode);
            prevNode.setNextNode(tvNode);
        }
        
        // 添加到父节点的子节点列表
        nodeParent.getChildNodes().add(tvNode);
        nodesHsahMap.put(tvNode.getKey(), tvNode);
    }
}
```

### 3.3 生成树 HTML

```java
// TreeViewMain.createTreeHtml() - 生成树 HTML
public String createTreeHtml(DTTable table) throws Exception {
    TreeViewNode node = this.createTreeNodes(table);
    String s1 = createHtml(node, 0);
    return s1;
}

// TreeViewMain.createHtml() - 递归生成 HTML
private String createHtml(TreeViewNode node, int lvl) {
    StringBuilder sb = new StringBuilder();
    
    if (node.getKey().equals(TreeViewNode.NODE_ROOT_KEY)) {
        // 根节点
        sb.append(NODE_ROOT_TEMPLATE.replace("[GUID]", this._Guid));
        replaceStr(sb, "[TMP_CAPTION_TEXT]", node.getDispVal());
    } else {
        // 普通节点
        sb.append(createNodeHtml(node));
    }
    
    // 递归处理子节点
    StringBuilder sbChildren = new StringBuilder();
    for (int i = 0; i < node.getChildNodes().size(); i++) {
        int new_lvl = lvl + 1;
        String childHtml = createHtml(node.getChildNodes().get(i), new_lvl);
        sbChildren.append(childHtml);
    }
    
    // 替换子节点占位符
    if (sbChildren.length() > 0) {
        String childTemplate = NODE_CHILD_TEMPLATE.replace("[TMP_CONTENT]", sbChildren.toString());
        replaceStr(sb, "[TMPLATE_CHILD_NODE]", childTemplate);
        replaceStr(sb, "[TEMP_DISPLAY]", "none");  // 默认折叠
    } else {
        replaceStr(sb, "[TMPLATE_CHILD_NODE]", "");
        replaceStr(sb, "[TEMP_DISPLAY]", "none");
    }
    
    return sb.toString();
}

// TreeViewMain.createNodeHtml() - 生成节点 HTML
private String createNodeHtml(TreeViewNode node) {
    StringBuilder sb = new StringBuilder(NODE_TEMPLATE);
    
    // 替换节点值
    replaceStr(sb, "[TMP_NODE_VALUE]", node.getDispVal());
    
    // 替换菜单组
    replaceStr(sb, "[TMPLATE_MENU_GROUP]", node.getMenuGroup());
    
    // 替换 Key
    replaceStr(sb, "[TMPLATE_KEY]", node.getKey());
    
    // 替换层级
    replaceStr(sb, "[LVL]", String.valueOf(node.getLvl()));
    
    // 替换标题
    if (node.getTitle() != null && node.getTitle().trim().length() > 0) {
        replaceStr(sb, "[TMP_TITLE]", "title=\"" + node.getTitle() + "\"");
    } else {
        replaceStr(sb, "[TMP_TITLE]", "");
    }
    
    // 替换附加参数
    StringBuilder addParas = new StringBuilder();
    for (int i = 0; i < node.getAddParas().size(); i++) {
        String val = node.getAddParas().get(i);
        if (val != null) {
            addParas.append("EWA_P").append(i).append("=\"")
                   .append(Utils.textToJscript(val)).append("\" ");
        }
    }
    replaceStr(sb, "[TMP_ADD_PARAS]", addParas.toString());
    
    // 替换附加字段
    if (this._AddCols != null) {
        String addColsHtml = this.createTreeAddHtml(node);
        replaceStr(sb, "[TEMP_NODE_ADD_FIELDS]", addColsHtml);
    } else {
        replaceStr(sb, "[TEMP_NODE_ADD_FIELDS]", "");
    }
    
    // 处理更多子节点标记
    if (node.isMoreChild()) {
        replaceStr(sb, "[TMP_NODE_MORE]", "EWA_TREE_MORE='1'");
    } else {
        replaceStr(sb, "[TMP_NODE_MORE]", "");
    }
    
    // 处理图标
    TreeOtherIcon icon = this._Icons.getIconByNode(node);
    if (icon != null) {
        replaceStr(sb, "[EWA_TREE_TD01]", "F_" + this._Guid + "_" + icon.getIndex() + "_A");
    } else {
        replaceStr(sb, "[EWA_TREE_TD01]", "TD01A");
    }
    
    return sb.toString();
}

// TreeViewMain.createTreeAddHtml() - 生成节点附加字段 HTML
private String createTreeAddHtml(TreeViewNode node) throws Exception {
    if (this._FrameTree == null) {
        return "";
    }
    
    if (node.getData() != null) {
        // 设置当前数据行
        DTRow row = (DTRow) node.getData();
        row.getTable().getRows().setCurRow(row);
    }
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this._UserConfig.getUserXItems().count(); i++) {
        UserXItem uxi = this._UserConfig.getUserXItems().getItem(i);
        String tag = uxi.getSingleValue("Tag");
        
        if ("dataType".equalsIgnoreCase(tag)) {
            continue;  // 定义数据类型用的，无 UI
        }
        
        String col = this.createItemHtmlCell(uxi);
        sb.append("<td class='ewa-node-add ewa-node-col-").append(uxi.getName()).append("'>");
        sb.append(col);
        sb.append("</td>");
    }
    
    return sb.toString();
}

// TreeViewMain.createItemHtmlCell() - 生成单元格 HTML
private String createItemHtmlCell(UserXItem uxi) throws Exception {
    if (this._FrameTree == null) {
        return "";
    }
    
    // 创建样式
    this.createCellParentStyle(uxi);
    
    // 创建 Item 实例
    IItem item = this._FrameTree.getHtmlClass().getItem(uxi);
    String itemHtml = item.createItemHtml();
    
    // 处理属性
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
                // 表达式为 false，移除属性
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

## 4. FrameTree 渲染详解

### 4.1 createHtml 流程

```java
// FrameTree.createHtml()
public void createHtml() throws Exception {
    super.addDebug(this, "TREE", "开始加载 TreeViewMain");
    
    // 1. 创建 TreeViewMain
    _TreeViewMain = new TreeViewMain(
        super.getHtmlClass().getUserConfig(),
        super.getHtmlClass().getSysParas().getLang(),
        super.getHtmlClass().getSysParas().getFrameUnid()
    );
    super.addDebug(this, "TREE", "加载 TreeViewMain 完成");
    
    // 2. 皮肤头部
    super.createSkinTop();
    super.addDebug(this, "TREE", "SkinTop");
    
    // 3. CSS（包括图标 CSS）
    super.createCss();
    this.createCssIcons();  // 树特殊图标
    super.addDebug(this, "TREE", "加载 CssIcons");
    
    // 4. 头部 JS
    super.createJsTop();
    super.addDebug(this, "TREE", "加载 Jstop");
    
    // 5. 主内容
    super.addDebug(this, "TREE", "开始加载 主内容");
    this.createContent();
    super.addDebug(this, "TREE", "开始加载 主内容 OK");
    
    // 6. 皮肤底部
    this.createSkinBottom();
    
    // 7. 底部 JS
    this.createJsBottom();
    
    // 8. Frame 脚本
    this.createJsFramePage();
}

// FrameTree.createFrameContent() - 创建树内容
public void createFrameContent() throws Exception {
    MStr sb = new MStr();
    
    if (this._TreeViewMain == null) {
        this._TreeViewMain = new TreeViewMain(
            super.getHtmlClass().getUserConfig(),
            super.getHtmlClass().getSysParas().getLang(),
            super.getHtmlClass().getSysParas().getFrameUnid()
        );
    }
    
    // 设置附加字段
    this._TreeViewMain.initCreateAddCols(this);
    
    // 设置根节点 ID
    String rootId = super.getHtmlClass().getSysParas().getRequestValue()
        .getString(FrameParameters.EWA_TREE_ROOT_ID);
    if (rootId != null) {
        this._TreeViewMain.setRootId(rootId);
    }
    
    // 获取数据表
    MList tbs = super.getHtmlClass().getItemValues().getDTTables();
    DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
    
    // 设置列表数据
    super.getHtmlClass().getItemValues().setListFrameTable(tb);
    
    // 生成树 HTML
    String s1 = _TreeViewMain.createTreeHtml(tb);
    sb.append(s1);
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}
```

### 4.2 生成图标 CSS

```java
// FrameTree.createCssIcons() - 生成树图标 CSS
private void createCssIcons() {
    MStr sb = new MStr();
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    ArrayList<TreeOtherIcon> icons = this._TreeViewMain.getIcons().getIcons();
    
    String skinName = getPageItemValue("SkinName", "SkinName");
    
    for (int i = 0; i < icons.size(); i++) {
        TreeOtherIcon icon = icons.get(i);
        String open = icon.getOpen();   // 展开图标
        String close = icon.getClose(); // 关闭图标
        
        // 展开状态 CSS
        sb.append("#" + skinName + " .F_" + gunid + "_" + i + "_A{");
        sb.append("width:20px; height:18px; ");
        sb.append("background-image:url('" + replaceParameters(open, true) + "');");
        sb.append("background-repeat:no-repeat ;}\r\n");
        
        // 关闭状态 CSS
        sb.append("#" + skinName + " .F_" + gunid + "_" + i + "_B{");
        sb.append("width:20px; height:18px; ");
        sb.append("background-image:url('" + replaceParameters(close, true) + "');");
        sb.append("background-repeat:no-repeat ;}\r\n");
    }
    
    this.getHtmlClass().getDocument().addCss(sb.toString());
}
```

### 4.3 生成 Tree 初始化脚本

```java
// FrameTree.createJsFramePage()
public void createJsFramePage() throws Exception {
    RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    String lang = super.getHtmlClass().getSysParas().getLang();
    String url = super.getUrlJs();
    
    super.createJsFrameXml();   // item 描述 XML 字符串
    super.createJsFrameMenu();  // menu 描述 XML 字符串
    super.createJsTop();
    super.createJsBottom();
    
    MStr sJs = new MStr();
    
    // 生成图标配置
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
    
    // 附加字段
    if (this._TreeViewMain != null && this._TreeViewMain.getAddColsLength() > 0) {
        sJs.append("o.AddCols =\"" + Utils.textToJscript(this._TreeViewMain.getAddCols()) + "\";\n");
    }
    
    sJs.append("o.Id = o._Id ='" + gunid + "';\n})();\n");
    
    // 初始化显示的值
    String k = rv.getString(FrameParameters.EWA_TREE_INIT_KEY);
    if (k != null && k.trim().length() > 0) {
        k = Utils.textToJscript(k);
        sJs.append("try{");
        sJs.append(fName + ".ShowNode(\"" + k + "\");\r\n");
        sJs.append("}catch(e){}\r\n");
    }
    
    // Frame 类型配置
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

// FrameTree.createJsTreeIcon() - 生成图标配置 JS
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

## 5. 关键参数

### 5.1 URL 参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_TREE_ROOT_ID` | 根节点 ID | `EWA_TREE_ROOT_ID=0` |
| `EWA_TREE_INIT_KEY` | 初始化展开的节点 | `EWA_TREE_INIT_KEY=root` |
| `EWA_TREE_MORE` | 分层加载 | `EWA_TREE_MORE=1` |
| `EWA_TREE_STATUS` | 获取树状态 | `EWA_TREE_STATUS=1` |
| `EWA_TREE_SKIP_GET_STATUS` | 不获取状态 | `EWA_TREE_SKIP_GET_STATUS=1` |
| `EWA_TREE_KEY` | 节点 ID 字段 | `EWA_TREE_KEY=category_id` |
| `EWA_TREE_PARENT_KEY` | 父节点 ID 字段 | `EWA_TREE_PARENT_KEY=parent_id` |
| `EWA_TREE_TEXT` | 节点文本字段 | `EWA_TREE_TEXT=category_name` |

### 5.2 XML 配置

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

## 6. 分层加载

### 6.1 配置

```xml
<Tree>
    <Set Key="id" ParentKey="pid" Text="name" LoadByLevel="1"/>
</Tree>
```

### 6.2 SQL 生成

```sql
-- 第一次加载（根节点）
SELECT A.*, B.EWAMORECNT FROM (
    SELECT id, pid, name FROM categories WHERE pid='0'
) A 
LEFT JOIN (
    SELECT pid EWAPID, COUNT(*) EWAMORECNT 
    FROM categories GROUP BY pid
) B ON A.id=B.EWAPID

-- 点击展开时加载子节点
SELECT A.*, B.EWAMORECNT FROM (
    SELECT id, pid, name FROM categories WHERE pid=@id
) A 
LEFT JOIN (
    SELECT pid EWAPID, COUNT(*) EWAMORECNT 
    FROM categories GROUP BY pid
) B ON A.id=B.EWAPID
```

### 6.3 前端调用

```javascript
// 点击展开节点
EWA.F.FOS["tree_id"].LoadMore(nodeKey);

// 获取树状态
EWA.F.FOS["tree_id"].GetStatus();
```

---

## 7. 附加字段

### 7.1 配置

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

### 7.2 生成 HTML

```html
<table class='ewa-tree-node ewa-tree-lvl-1'>
    <tr class='ewa-node-row-0'>
        <td class='TD00A ewa-node-open-close'>...</td>
        <td class='TD01A ewa-node-icon'>...</td>
        <td class='ewa-node-caption'>节点名称</td>
        <!-- 附加字段 -->
        <td class='ewa-node-add ewa-node-col-SORT_ID'>100</td>
        <td class='ewa-node-add ewa-node-col-STATUS'>正常</td>
    </tr>
</table>
```

---

## 8. 图标配置

### 8.1 配置

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

### 8.2 图标 CSS

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

## 9. 调试技巧

### 9.1 启用 Debug

```
EWA_DEBUG_NO=1      # 不显示 Debug
EWA_DEBUG_KEY=xxx   # Debug 密钥
EWA_JS_DEBUG=1      # JS 调试模式
```

### 9.2 浏览器控制台

```javascript
// 获取 Tree 对象
EWA.F.FOS["tree_id"]

// 展开节点
EWA.F.FOS["tree_id"].ShowNode("node_key")

// 获取树状态
EWA.F.FOS["tree_id"].GetStatus()

// 重新加载
EWA.F.FOS["tree_id"].Reload()
```

### 9.3 查看执行 SQL

```java
// DataConnection 中记录
String sql = conn.getLastResult().getSqlOrigin();
System.out.println("原始 SQL: " + sql);

String executedSql = conn.getLastResult().getSqlExecute();
System.out.println("执行 SQL: " + executedSql);
```

---

## 10. 完整示例

### 10.1 XML 配置

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

### 10.2 URL 调用

```
# 基本查询
/ewa?XMLNAME=categories&ITEMNAME=T.V

# 指定根节点
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_ROOT_ID=0

# 初始化展开节点
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_INIT_KEY=100

# 分层加载
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_MORE=1&category_id=100

# 获取树状态
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_STATUS=1&category_id=100

# 不获取状态
/ewa?XMLNAME=categories&ITEMNAME=T.V&EWA_TREE_SKIP_GET_STATUS=1
```

---

## 总结

Tree 执行流程关键点：

1. **Action 执行** → `ActionTree.execute()`
   - 生成分级加载 SQL
   - 执行 SqlSet

2. **树构建** → `TreeViewMain.createTreeNodes()`
   - 遍历数据表
   - 创建节点
   - 构建父子关系

3. **HTML 渲染** → `FrameTree.createHtml()`
   - createSkinTop()
   - createCss() + createCssIcons()
   - createJsTop()
   - createContent() → createFrameContent()
     - createTreeHtml()
       - createTreeNodes() - 构建节点关系
       - createHtml() - 递归生成 HTML
   - createJsFramePage()
   - createSkinBottom()
   - createJsBottom()

4. **分层加载**
   - LoadByLevel=1 启用
   - EWAMORECNT 统计子节点数
   - EWA_TREE_MORE 获取更多子节点

5. **特殊功能**
   - 附加字段显示
   - 动态图标
   - 右键菜单
   - 节点展开/折叠
