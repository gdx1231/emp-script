# EWA Frame 调用详解

## 概述

EWA 框架提供多种 Frame 类型，用于不同的页面展示需求：

| Frame 类型 | 类名 | 用途 |
|-----------|------|------|
| **Frame** | `FrameFrame` | 表单输入/修改界面 |
| **ListFrame** | `FrameList` | 数据列表/表格 |
| **Tree** | `FrameTree` | 树形结构 |
| **Grid** | `FrameGrid` | 网格显示 |
| **Menu** | `FrameMenu` | 菜单 |
| **MultiGrid** | `FrameMultiGrid` | 多维表格 |
| **Logic** | `FrameLogic` | 逻辑控制 |
| **Report** | `FrameReport` | 报表 |
| **Combine** | `FrameCombine` | 组合页面 |
| **Complex** | `FrameComplex` | 复合页面 |

---

## 1. Frame 继承结构

```
IFrame (接口)
  ▲
  │
FrameBase (基类)
  │
  ├─► FrameFrame      (表单)
  ├─► FrameList       (列表)
  ├─► FrameTree       (树)
  ├─► FrameGrid       (网格)
  ├─► FrameMenu       (菜单)
  ├─► FrameMultiGrid  (多维表格)
  ├─► FrameLogic      (逻辑)
  ├─► FrameReport     (报表)
  ├─► FrameCombine    (组合)
  └─► FrameComplex    (复合)
```

---

## 2. 调用流程

### 2.1 完整调用链路

```
HTTP 请求
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
   │     └─► 读取 FrameTag (FrameType)
   │
   ├─► 2. 创建 Frame 实例
   │     ├─► LISTFRAME → new FrameList()
   │     ├─► FRAME → new FrameFrame()
   │     ├─► TREE → new FrameTree()
   │     └─► ...
   │
   ├─► 3. Frame.init(HtmlCreator)
   │
   ├─► 4. 执行 Action (OnPageLoad/OnPagePost)
   │
   └─► 5. Frame.createHtml()
         │
         ├─► createSkinTop()      - 皮肤头部
         ├─► createCss()          - CSS 样式
         ├─► createJsTop()        - 头部 JS
         ├─► createContent()      - 主体内容
         ├─► createJsFramePage()  - Frame 初始化脚本
         ├─► createSkinBottom()   - 皮肤底部
         └─► createJsBottom()     - 底部 JS
```

### 2.2 代码示例

```java
// HtmlCreator.createPageHtml()
public void createPageHtml() throws Exception {
    // 1. 获取 Frame 类型
    String frameType = this._SysParas.getFrameType();  // "LISTFRAME", "FRAME", "TREE"...
    
    // 2. 创建对应的 Frame 实例
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
    // ... 其他类型
    
    // 3. 初始化 Frame
    this._Frame.init(this);
    
    // 4. 执行 Action
    String actionName = this._SysParas.getActionName();
    if (actionName != null && actionName.length() > 0) {
        this._ActionReturnValue = this._Action.execute(actionName);
    }
    
    // 5. 生成 HTML
    this._PageHtml = this._Frame.createHtml();
}
```

---

## 3. ListFrame 详解

### 3.1 配置示例

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

### 3.2 createHtml 流程

```java
// FrameList.createHtml()
public void createHtml() throws Exception {
    if (super.getHtmlClass().getSysParas().isVue()) {
        super.createHtmlVue();  // Vue 模式
    } else {
        this.createHtmlTraditional();  // 传统模式
    }
}

public void createHtmlTraditional() {
    // 1. 皮肤头部
    super.createSkinTop();
    
    // 2. CSS 样式
    super.createCss();
    
    // 3. 头部 JS
    super.createJsTop();
    
    // 4. 主体内容（表格）
    this.createContent();
    
    // 5. Frame 初始化脚本
    this.createJsFramePage();
    
    // 6. 皮肤底部
    super.createSkinBottom();
    
    // 7. 底部 JS
    super.createJsBottom();
}
```

### 3.3 创建表格内容

```java
// FrameList.createContent()
public void createContent() {
    MStr sb = new MStr();
    
    // 1. 获取数据表
    DTTable tb = this.getSplitPageTable();
    
    // 2. 创建表格头部
    sb.append("<table id='EWA_LF_" + gunid + "' class='EWA_LISTFRAME'>");
    sb.append("<thead><tr>");
    
    // 3. 遍历 XItems 创建表头
    for (int i = 0; i < xItems.count(); i++) {
        UserXItem xItem = xItems.getItem(i);
        if (this.isHiddenField(xItem.getName())) {
            continue;  // 跳过隐藏字段
        }
        String des = xItem.getDescription();
        sb.append("<th>" + des + "</th>");
    }
    sb.append("</tr></thead>");
    
    // 4. 创建表格数据行
    sb.append("<tbody>");
    for (int i = 0; i < tb.getCount(); i++) {
        DTRow row = tb.getRow(i);
        sb.append("<tr id='R_" + i + "'>");
        
        // 遍历 XItems 创建单元格
        for (int j = 0; j < xItems.count(); j++) {
            UserXItem xItem = xItems.getItem(j);
            IItem item = ItemFactory.createItem(xItem, row);
            String html = item.render();
            sb.append("<td>" + html + "</td>");
        }
        sb.append("</tr>");
    }
    sb.append("</tbody></table>");
    
    // 5. 创建分页栏
    this.createPageBar(sb);
    
    // 6. 创建按钮栏
    this.createButtonBar(sb);
    
    // 7. 创建搜索框
    this.createSearchBox(sb);
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}
```

### 3.4 生成 Frame 初始化脚本

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
    
    // 分页参数
    PageSplit ps = this._PageSplit;
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
    
    sJs.al("})();");
    
    this.getHtmlClass().getDocument().addJs("LISTFRAME", sJs.toString(), false);
}
```

### 3.5 常用 ListFrame 参数

```java
// URL 参数
EWA_PAGECUR=2              // 当前页
EWA_PAGESIZE=50            // 每页记录数
EWA_LF_ORDER=create_date DESC  // 排序
EWA_SEARCH=name[lk]张      // 搜索条件
EWA_BOX=1                  // BOX 模式
EWA_IS_SPLIT_PAGE=no       // 不分页
EWA_LU_STICKY_HEADERS=yes  // 固定表头
EWA_LU_BUTTONS=NO          // 不显示按钮
EWA_LU_SEARCH=NO           // 不显示搜索
EWA_LU_SELECT=S            // 单选
EWA_LU_DBLCLICK=yes        // 双击行
```

---

## 4. FrameFrame 详解

### 4.1 配置示例

```xml
<EasyWebTemplate Name="users.F.NM">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
        <Name><Set Name="users.F.NM"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <Size><Set Width="700" Height="500"/></Size>
        <FrameHtml>
            <Set FrameCols="C2"/>  <!-- C1=1 段，C2=2 段，C11=上下排列 -->
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

### 4.2 createHtml 流程

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
    // 1. 皮肤头部
    super.createSkinTop();
    
    // 2. CSS
    super.createCss();
    
    // 3. 头部 JS
    super.createJsTop();
    
    // 4. 表单内容
    this.createContent();
    
    // 5. Frame 脚本
    this.createJsFramePage();
    
    // 6. 皮肤底部
    super.createSkinBottom();
    
    // 7. 底部 JS
    super.createJsBottom();
}
```

### 4.3 创建表单内容

```java
// FrameFrame.createContent()
public void createContent() throws Exception {
    MStr sb = new MStr();
    
    // 1. 获取数据
    DTTable tb = this.getHtmlClass().getItemValues().getLastTable();
    
    // 2. 创建表单框架
    sb.append("<form id='f_" + gunid + "' method='post'>");
    sb.append("<input type='hidden' name='EWA_POST' value='1'/>");
    
    // 3. 分组渲染
    if (this._IsGroup) {
        // 分组显示
        for (int i = 0; i < _GroupInfos.length; i++) {
            String grp = _GroupInfos[i];
            sb.append("<fieldset><legend>" + grp + "</legend>");
            this.renderGroup(grp, tb, sb);
            sb.append("</fieldset>");
        }
    } else {
        // 无分组显示
        this.renderAll(tb, sb);
    }
    
    // 4. 创建按钮
    this.renderButtons(sb);
    
    sb.append("</form>");
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}

// 渲染单个字段
private void renderField(UserXItem xItem, DTTable tb, MStr sb) {
    // 跳过隐藏字段
    if (this.isHiddenField(xItem.getName())) {
        return;
    }
    
    // 获取字段值
    String value = "";
    if (tb != null && tb.getCount() > 0) {
        value = tb.getCell(0, xItem.getDataField()).toString();
    }
    
    // 创建 Item 实例
    IItem item = ItemFactory.createItem(xItem, value);
    String html = item.render();
    
    // 添加到表单
    sb.append("<div class='EWA_TR'>");
    sb.append("<td class='EWA_TD_M'>" + xItem.getDescription() + "</td>");
    sb.append("<td class='EWA_TD_M'>" + html + "</td>");
    sb.append("</div>");
}
```

### 4.4 FrameFrame 参数

```java
// URL 参数
EWA_FRAME_COLS=C2      // C1=1 段，C2=2 段，C11=上下排列
EWA_WIDTH=800px        // 宽度
EWA_HEIGHT=600px       // 高度
EWA_MTYPE=N            // N=新增，M=修改，C=复制
EWA_IS_HIDDEN_CAPTION=yes  // 不显示标题
EWA_TEMP_NO=1          // 不使用自定义模板
EWA_REDRAW=1           // 重绘模式
EWA_IN_DIALOG=1        // Dialog 模式
```

---

## 5. FrameTree 详解

### 5.1 配置示例

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

### 5.2 Tree 特殊字段

```sql
-- EWA_TREE_KEY: 节点 ID
-- EWA_TREE_PARENT_KEY: 父节点 ID
-- EWA_TREE_TEXT: 节点显示文本
```

### 5.3 createHtml 流程

```java
// FrameTree.createHtml()
public void createHtml() throws Exception {
    // 1. 创建 TreeViewMain
    this._TreeViewMain = new TreeViewMain(
        super.getHtmlClass().getUserConfig(),
        super.getHtmlClass().getSysParas().getLang(),
        super.getHtmlClass().getSysParas().getFrameUnid()
    );
    
    // 2. 皮肤头部
    super.createSkinTop();
    
    // 3. CSS（包括树图标 CSS）
    super.createCss();
    this.createCssIcons();  // 树特殊图标
    
    // 4. 头部 JS
    super.createJsTop();
    
    // 5. 树内容
    this.createContent();
    
    // 6. 皮肤底部
    this.createSkinBottom();
    
    // 7. Frame 脚本
    this.createJsFramePage();
    
    // 8. 底部 JS
    this.createJsBottom();
}
```

### 5.4 创建树内容

```java
// FrameTree.createFrameContent()
public void createFrameContent() throws Exception {
    MStr sb = new MStr();
    
    // 1. 获取根节点 ID
    String rootId = rv.getString(FrameParameters.EWA_TREE_ROOT_ID);
    if (rootId != null) {
        this._TreeViewMain.setRootId(rootId);
    }
    
    // 2. 获取数据
    DTTable tb = super.getHtmlClass().getItemValues().getLastTable();
    
    // 3. 生成树 HTML
    String treeHtml = _TreeViewMain.createTreeHtml(tb);
    sb.append(treeHtml);
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
}

// TreeViewMain.createTreeHtml()
public String createTreeHtml(DTTable tb) {
    // 1. 构建树结构
    this.buildTree(tb);
    
    // 2. 递归生成 HTML
    return this.createNodeHtml(this._RootNode, 0);
}

private String createNodeHtml(TreeViewNode node, int lvl) {
    MStr sb = new MStr();
    
    // 节点 HTML
    sb.append("<div class='TREE_NODE' id='N_" + node.getId() + "'>");
    sb.append("<span class='TREE_ICON' onclick='toggleNode(\"" + node.getId() + "\")'>");
    sb.append(node.hasChildren() ? "[+]" : "[-]");
    sb.append("</span>");
    sb.append("<span class='TREE_TEXT' onclick='selectNode(\"" + node.getId() + "\")'>");
    sb.append(node.getText());
    sb.append("</span>");
    sb.append("</div>");
    
    // 子节点
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

### 5.5 Tree 初始化脚本

```java
// FrameTree.createJsFramePage()
public void createJsFramePage() throws Exception {
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    String url = super.getUrlJs();
    
    MStr sJs = new MStr();
    
    // 树图标配置
    sJs.append(this.createJsTreeIcon());
    
    // 初始化树
    sJs.append("EWA.LANG='" + lang + "';");
    sJs.append("(function() {");
    sJs.append("var o = EWA.F.FOS['" + gunid + "'] = new EWA_TreeClass(");
    sJs.append("document.getElementById('EWA_TREE_" + gunid + "').parentNode,");
    sJs.append("'EWA.F.FOS[\"" + gunid + "\"]',");
    sJs.append("\"" + url + "\");");
    sJs.append("o.InitMenu(_EWA_MENU_" + gunid + ");");
    sJs.append("o.Init(EWA_ITEMS_XML_" + gunid + ");");
    sJs.append("o.Icons = _EWA_ICONS_" + gunid + ";");
    
    // 附加字段
    if (this._TreeViewMain.getAddColsLength() > 0) {
        sJs.append("o.AddCols =\"" + this._TreeViewMain.getAddCols() + "\";");
    }
    
    sJs.append("o.Id = o._Id ='" + gunid + "';");
    sJs.append("})();");
    
    // 初始化显示的值
    String k = rv.getString(FrameParameters.EWA_TREE_INIT_KEY);
    if (k != null) {
        sJs.append("try{ EWA.F.FOS['" + gunid + "'].ShowNode(\"" + k + "\"); }catch(e){}");
    }
    
    this.getHtmlClass().getDocument().addJs("TREE", sJs.toString(), false);
}
```

### 5.6 Tree 参数

```java
// URL 参数
EWA_TREE_ROOT_ID=0           // 根节点 ID
EWA_TREE_INIT_KEY=root       // 初始化展开的节点
EWA_TREE_MORE=1              // 分层加载
EWA_TREE_STATUS=1            // 获取当前状态
EWA_TREE_SKIP_GET_STATUS=1   // 不获取状态
EWA_TREE_KEY=category_id     // 节点 ID 字段
EWA_TREE_PARENT_KEY=parent_id // 父节点 ID 字段
EWA_TREE_TEXT=category_name  // 节点文本字段
```

---

## 6. 其他 Frame 类型

### 6.1 FrameGrid (网格)

```xml
<FrameTag><Set FrameTag="Grid"/></FrameTag>
```

用于网格化显示数据，类似 Excel 表格。

### 6.2 FrameMenu (菜单)

```xml
<FrameTag><Set FrameTag="Menu"/></FrameTag>
```

用于创建导航菜单。

### 6.3 FrameMultiGrid (多维表格)

```xml
<FrameTag><Set FrameTag="MultiGrid"/></FrameTag>
```

用于交叉表、透视表等复杂表格。

### 6.4 FrameLogic (逻辑)

```xml
<FrameTag><Set FrameTag="Logic"/></FrameTag>
```

用于条件判断和流程控制。

### 6.5 FrameReport (报表)

```xml
<FrameTag><Set FrameTag="Report"/></FrameTag>
```

用于报表展示。

### 6.6 FrameCombine (组合)

```xml
<FrameTag><Set FrameTag="Combine"/></FrameTag>
```

用于组合多个 Frame。

### 6.7 FrameComplex (复合)

```xml
<FrameTag><Set FrameTag="Complex"/></FrameTag>
```

用于复杂页面布局。

---

## 7. Frame 间通信

### 7.1 父子 Frame

```java
// 父 Frame 调用子 Frame
EWA.F.FOS["child_frame_id"].Reload();

// 子 Frame 调用父 Frame
EWA.F.FOS["@EWA_PARENT_FRAME"].Reload();
```

### 7.2 Frame 间参数传递

```java
// URL 参数
EWA_PARENT_FRAME=parent_id  // 指定父 Frame
EWA_FRAME_UNID_PREFIX=xxx   // Frame Unid 前缀
```

### 7.3 ListFrame + Frame 联动

```xml
<!-- ListFrame 中定义双击事件 -->
<XItem Name="butModify">
    <EventSet>
        <Set EventName="ondblclick" EventType="Javascript"
             EventValue="EWA.F.FOS['@sys_frame_unid'].ext_NewOrModifyOrCopy('M')"/>
    </EventSet>
</XItem>

<!-- Frame 中定义返回事件 -->
<XItem Name="butClose">
    <EventSet>
        <Set EventName="onclick" EventType="Javascript"
             EventValue="EWA.OW.Close(); EWA.OW.PFrame.Reload();"/>
    </EventSet>
</XItem>
```

---

## 8. Vue 模式

### 8.1 启用 Vue 模式

```java
// URL 参数
EWA_VUE=1
```

### 8.2 Vue 模式处理

```java
// FrameBase.createHtmlVue()
public void createHtmlVue() {
    HtmlDocument doc = this.getHtmlClass().getDocument();
    
    // 1. 设置标题
    String title = getHtmlClass().getItemValues()
        .replaceParameters(getHtmlClass().getSysParas().getTitle(), true);
    doc.setTitle(title);
    
    // 2. 创建 CSS
    createCss();
    
    // 3. 创建 Vue 容器
    String id = "vue_" + getHtmlClass().getSysParas().getFrameUnid();
    String content = "<div id='" + id + "'></div>";
    doc.addScriptHtml(content);
    
    // 4. 生成配置 JSON
    String json = getHtmlClass().getHtmlCreator().createPageJsonExt1();
    
    // 5. 创建 Vue 初始化脚本
    StringBuilder vueJs = new StringBuilder();
    vueJs.append("(function(){");
    vueJs.append("var ewacfg=" + json + ";");
    vueJs.append("app = EWA_VueClass(ewacfg, '#" + id + "');");
    vueJs.append("})();");
    
    doc.addJs("vue", vueJs.toString(), false);
}
```

---

## 9. 调试技巧

### 9.1 启用 Debug

```java
// URL 参数
EWA_DEBUG_NO=1      // 不显示 Debug
EWA_DEBUG_KEY=xxx   // Debug 密钥
EWA_JS_DEBUG=1      // JS 调试模式（加载未压缩的 JS）
```

### 9.2 查看 Frame 信息

```javascript
// 浏览器控制台
EWA.F.FOS["frame_id"]  // 获取 Frame 对象
EWA.F.FOS["frame_id"].Reload()  // 刷新
EWA.F.FOS["frame_id"].Url  // 获取 URL
```

### 9.3 性能优化

```java
// 缓存配置
EWA_R=1  // 强制刷新数据

// 不显示内容（仅执行）
EWA_NO_CONTENT=1

// 跳过验证
EWA_VALIDCODE_CHECK=NOT_CHECK
```

---

## 10. 最佳实践

### 10.1 ListFrame 优化

```xml
<!-- ✅ 推荐：使用分页 -->
<PageSize><Set PageSize="20" IsSplitPage="1" KeyField="USER_ID"/></PageSize>

<!-- ✅ 推荐：固定表头 -->
EWA_LU_STICKY_HEADERS=yes

<!-- ❌ 避免：不分页显示大量数据 -->
<PageSize><Set IsSplitPage="0"/></PageSize>
```

### 10.2 Frame 优化

```xml
<!-- ✅ 推荐：明确指定 MTYPE -->
EWA_MTYPE=N  <!-- 新增 -->
EWA_MTYPE=M  <!-- 修改 -->

<!-- ✅ 推荐：使用分组 -->
<FrameHtml><Set FrameCols="C2"/></FrameHtml>

<!-- ❌ 避免：过多字段在一个页面 -->
```

### 10.3 Tree 优化

```xml
<!-- ✅ 推荐：分层加载 -->
EWA_TREE_MORE=1

<!-- ✅ 推荐：指定根节点 -->
EWA_TREE_ROOT_ID=0

<!-- ❌ 避免：一次性加载大量节点 -->
```

---

## 总结

| Frame 类型 | 典型用途 | 关键配置 |
|-----------|---------|---------|
| **ListFrame** | 数据列表 | PageSize, EWA_SEARCH, EWA_LF_ORDER |
| **Frame** | 表单输入 | EWA_MTYPE, FrameCols, EWA_WIDTH |
| **Tree** | 分类导航 | EWA_TREE_KEY, EWA_TREE_PARENT_KEY, EWA_TREE_TEXT |
| **Grid** | 网格显示 | - |
| **MultiGrid** | 交叉表 | - |
| **Menu** | 导航菜单 | - |

选择建议：
- 列表展示 → ListFrame
- 数据录入 → Frame
- 层级结构 → Tree
- 复杂报表 → MultiGrid / Report
- 组合页面 → Combine / Complex
