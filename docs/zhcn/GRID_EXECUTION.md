# Grid 执行流程详解

## 概述

Grid 是 EWA 框架中用于网格化显示数据的组件，继承自 FrameList，支持单列和多列分栏显示。

**核心类**:
- `FrameGrid` - Grid 渲染类 (继承自 FrameList)
- `ActionListFrame` - Grid 动作执行类 (与 ListFrame 共用)

---

## 1. 完整执行流程

```
HTTP 请求 (/ewa?XMLNAME=products&ITEMNAME=G.V)
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
   │     └─► 读取 FrameTag = "Grid"
   │
   ├─► 2. 创建 FrameGrid 实例
   │     this._Frame = new FrameGrid()
   │     this._Action = new ActionListFrame()
   │
   ├─► 3. FrameGrid.init(HtmlCreator)
   │
   ├─► 4. ActionListFrame.execute()
   │     ├─► 执行 SqlSet (OnPageLoad)
   │     └─► 执行 ClassSet (可选)
   │
   └─► 5. FrameGrid.createHtml()
         ├─► createSkinTop()      - 皮肤头部
         ├─► createCss()          - CSS 样式
         ├─► createJsTop()        - 头部 JS
         ├─► createContent()      - 主体内容 (网格)
         │   └─► createFrameContent()
         │       ├─► 获取数据表
         │       ├─► 检查分栏数 (ColSize)
         │       └─► 生成网格 HTML
         │           ├─► 单列模式 (ul/li)
         │           └─► 多列模式 (table/tr/td)
         ├─► createSkinBottom()   - 皮肤底部
         ├─► createJsBottom()     - 底部 JS
         └─► createJsFramePage()  - Grid 初始化脚本
```

---

## 2. FrameGrid 类结构

### 2.1 继承关系

```java
public class FrameGrid extends FrameList implements IFrame {
    // FrameGrid 继承自 FrameList
    // 因此拥有 ListFrame 的所有功能
    // 但重写了 createContent() 和 createFrameContent() 方法
}
```

### 2.2 与 FrameList 的区别

| 特性 | FrameList | FrameGrid |
|------|-----------|-----------|
| 显示方式 | 表格列表 (table/tr/td) | 网格 (ul/li 或 table/tr/td) |
| 分栏支持 | 支持 (ColSize) | 支持 (ColSize) |
| 分页支持 | 支持 | 支持 (继承) |
| 搜索支持 | 支持 | 支持 (继承) |
| 按钮栏 | 支持 | 支持 (继承) |
| 主要用途 | 数据列表 | 图片/卡片网格 |

---

## 3. createFrameContent 详解

### 3.1 主流程

```java
// FrameGrid.createFrameContent()
public void createFrameContent() throws Exception {
    HtmlDocument doc = this.getHtmlClass().getDocument();

    // 1. 皮肤定义的头部
    doc.addScriptHtml("<div>");
    String top = super.createSkinFCTop();
    doc.addScriptHtml(top);

    // 2. Frame 定义的页头
    doc.addScriptHtml(createFrameHeader(), "frame head");

    // 3. 获取数据表
    MList tbs = super.getHtmlClass().getAction().getDTTables();
    if (tbs == null || tbs.size() == 0) {
        doc.addScriptHtml("no data");
    }
    
    DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
    super.getHtmlClass().getItemValues().setListFrameTable(tb);

    // 4. 获取记录总数 (如果启用分页)
    DataConnection conn = super.getHtmlClass().getItemValues().getDataConn();
    DataResult ds = conn.getLastResult();
    if (ds != null && this.getHtmlClass().getUserConfig().getUserPageItem()
            .getSingleValue("PageSize", "IsSplitPage").equals("1")) {
        _ListFrameRecordCount = conn.getRecordCount(ds.getSqlOrigin());
    }

    // 5. 获取分栏数
    int colSize = 0;  // 分栏数
    String s1 = super.getPageItemValue("PageSize", "ColSize");
    if (!(s1 == null || s1.trim().length() == 0)) {
        try {
            colSize = Integer.parseInt(s1);
        } catch (Exception e) {
            // nothing
        }
    }
    if (colSize <= 0) {
        colSize = 0;  // 0 表示单列模式
    }

    int colSizeInc = 1;
    MStr sb = new MStr();
    
    // 6. 创建网格容器
    sb.al("<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_" 
        + super.getHtmlClass().getSysParas().getFrameUnid() + "' >");
    
    RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();

    // 7. 获取标签类型 (EWA_GRID_AS 参数)
    String tag0 = "li";
    String tag1 = "li";
    String gridAs = rv.s(FrameParameters.EWA_GRID_AS);
    if (rv.s(gridAs) != null) {
        if (rv.s(gridAs).equalsIgnoreCase("a")) {
            tag0 = "a";
            tag1 = "a";
        } else if (gridAs.equalsIgnoreCase("div")) {
            tag0 = "div";
            tag1 = "div";
        } else if (gridAs.equalsIgnoreCase("div2")) {
            tag0 = "div><div";
            tag1 = "div></div";
        }
    }

    // 8. 检查是否使用模板
    boolean isUseTemplate = super.isUsingTemplate();
    String frameTemplate = super.getPageItemValue("FrameHtml", "FrameHtml");

    // 9. 生成网格内容
    if (colSize == 0) {
        // 单列模式 (ul/li)
        sb.al("<ul class='ewa_grid_ul'>");
        for (int i = 0; i < tb.getCount(); i++) {
            tb.getRow(i);  // 移动当前行
            String keyExp = super.createItemKeys();
            String rowHtml = isUseTemplate 
                ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
                : super.createItemHtmls();

            sb.al("<" + tag0 + " class='ewa_grid_li' ewa_key=\"" + keyExp + "\">");
            sb.al(rowHtml);
            sb.al("</" + tag1 + ">");
        }
        sb.al("</ul>");
    } else {
        // 多列模式 (table/tr/td)
        sb.al("<table border='0' cellpadding='0' cellspacing='0' align='center'>");
        for (int i = 0; i < tb.getCount(); i++) {
            tb.getRow(i);  // 移动当前行
            String keyExp = super.createItemKeys();
            String rowHtml = isUseTemplate 
                ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
                : super.createItemHtmls();

            if (colSizeInc == 1) {
                sb.al("<tr>");
            }
            sb.al("<td " + keyExp + ">");
            sb.al(rowHtml);
            sb.al("</td>");
            
            // 每行填满 colSize 个单元格后换行
            if (colSizeInc == colSize && colSize > 0) {
                sb.al("</tr>");
                sb.al("<tr>");
            }
            colSizeInc++;
            if (colSizeInc > colSize) {
                colSizeInc = 1;
            }
        }
        // 填充空白单元格
        for (int i = colSizeInc; i <= colSize; i++) {
            sb.al("<td>&nbsp;</td>");
        }
        sb.a("</tr></table>");
    }

    sb.al("</div>");
    doc.addScriptHtml(sb.toString(), "Frame content");

    // 10. 皮肤定义的尾部
    String bottom = super.createSkinFCBottom();
    doc.addScriptHtml(bottom);

    // 11. Frame 定义的页脚
    this.createFrameFooter();
    doc.addScriptHtml("</div>");
}
```

### 3.2 单列模式 (ul/li)

```java
// 当 colSize == 0 时，使用 ul/li 布局
if (colSize == 0) {
    sb.al("<ul class='ewa_grid_ul'>");
    for (int i = 0; i < tb.getCount(); i++) {
        tb.getRow(i);
        String keyExp = super.createItemKeys();
        String rowHtml = isUseTemplate 
            ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
            : super.createItemHtmls();

        // 使用配置的标签 (li/div/a)
        sb.al("<" + tag0 + " class='ewa_grid_li' ewa_key=\"" + keyExp + "\">");
        sb.al(rowHtml);
        sb.al("</" + tag1 + ">");
    }
    sb.al("</ul>");
}
```

**生成的 HTML**:
```html
<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_abc123'>
    <ul class='ewa_grid_ul'>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="id"'>
            <div class='ewa-template-grid'>
                <!-- 项目内容 -->
                <img src='product.jpg' />
                <span>产品名称</span>
                <span>￥100.00</span>
            </div>
        </li>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="id"'>
            <!-- 更多项目... -->
        </li>
    </ul>
</div>
```

### 3.3 多列模式 (table/tr/td)

```java
// 当 colSize > 0 时，使用 table/tr/td 布局
else {
    sb.al("<table border='0' cellpadding='0' cellspacing='0' align='center'>");
    for (int i = 0; i < tb.getCount(); i++) {
        tb.getRow(i);
        String keyExp = super.createItemKeys();
        String rowHtml = isUseTemplate 
            ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
            : super.createItemHtmls();

        // 每行开始
        if (colSizeInc == 1) {
            sb.al("<tr>");
        }
        
        // 单元格
        sb.al("<td " + keyExp + ">");
        sb.al(rowHtml);
        sb.al("</td>");
        
        // 每行填满后换行
        if (colSizeInc == colSize && colSize > 0) {
            sb.al("</tr>");
            sb.al("<tr>");
        }
        colSizeInc++;
        if (colSizeInc > colSize) {
            colSizeInc = 1;
        }
    }
    // 填充空白单元格
    for (int i = colSizeInc; i <= colSize; i++) {
        sb.al("<td>&nbsp;</td>");
    }
    sb.a("</tr></table>");
}
```

**生成的 HTML (3 列)**:
```html
<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_abc123'>
    <table border='0' cellpadding='0' cellspacing='0' align='center'>
        <tr>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>产品 1</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>产品 2</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>产品 3</div>
            </td>
        </tr>
        <tr>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>产品 4</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>产品 5</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>产品 6</div>
            </td>
        </tr>
        <!-- 最后一行如果不满，填充空白 -->
        <tr>
            <td EWA_KEY="id">产品 7</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
    </table>
</div>
```

---

## 4. 标签类型配置 (EWA_GRID_AS)

### 4.1 配置方式

```java
// FrameGrid.createFrameContent()
String gridAs = rv.s(FrameParameters.EWA_GRID_AS);
if (rv.s(gridAs) != null) {
    if (rv.s(gridAs).equalsIgnoreCase("a")) {
        tag0 = "a";
        tag1 = "a";
    } else if (gridAs.equalsIgnoreCase("div")) {
        tag0 = "div";
        tag1 = "div";
    } else if (gridAs.equalsIgnoreCase("div2")) {
        tag0 = "div><div";
        tag1 = "div></div";
    }
}
```

### 4.2 标签类型

| 值 | 标签 | 用途 |
|----|------|------|
| (空) | `li` | 默认，列表项 |
| `a` | `a` | 可点击的链接 |
| `div` | `div` | 块级元素 |
| `div2` | `div><div` | 双层 div |

### 4.3 使用示例

```
# 使用链接标签
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=a

# 使用 div 标签
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=div

# 使用双层 div
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=div2
```

---

## 5. 模板支持

### 5.1 检查是否使用模板

```java
// FrameList.isUsingTemplate() (继承)
public boolean isUsingTemplate() {
    RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
    // 是否使用模板
    String frameTemplate = super.getPageItemValue("FrameHtml", "FrameHtml");
    if (frameTemplate == null || frameTemplate.trim().length() == 0) {
        return false;
    }
    // 检查是否禁用模板
    if (rv.s(FrameParameters.EWA_LF_TEMP_NO) == null 
        || rv.s(FrameParameters.EWA_TEMP_NO) == null) {
        return true;
    }
    return false;
}
```

### 5.2 使用模板生成 HTML

```java
// FrameList.createItemHtmlsByFrameHtml() (继承)
public String createItemHtmlsByFrameHtml(String template, String frameTag) throws Exception {
    ItemValues ivs = super.getHtmlClass().getItemValues();
    Map<String, String> cellData = new HashMap<String, String>();
    
    // 1. 遍历 XItems，生成每个字段的 HTML
    for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
        UserXItem uxi = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);
        
        // 跳过隐藏字段
        if (this.isHiddenField(uxi.getName())) {
            continue;
        }
        
        IItem item = super.getHtmlClass().getItem(uxi);
        String itemHtml = item.createItemHtml();
        
        // 替换@参数
        if (itemHtml.indexOf("@") >= 0) {
            itemHtml = super.getHtmlClass().getItemValues()
                .replaceParameters(itemHtml, false, false);
        }
        cellData.put(uxi.getName().toUpperCase(), itemHtml);
    }
    
    // 2. 替换模板中的@参数
    MListStr al = Utils.getParameters(template, "@");
    StringBuilder tmp = new StringBuilder();
    tmp.append(template);
    
    for (int i = 0; i < al.size(); i++) {
        String paraName = al.get(i);
        String name = paraName.toUpperCase();
        String paraValue;
        
        if (cellData.containsKey(name)) {
            paraValue = cellData.get(name);
        } else {
            paraValue = ivs.getValue(paraName, paraName);
            if (paraValue != null && paraValue.indexOf("@") >= 0) {
                paraValue = paraValue.replace("@", IItem.REP_AT_STR);
            }
            paraValue = Utils.textToInputValue(paraValue);
        }
        if (paraValue == null) {
            paraValue = "";
        }
        
        Utils.replaceStringBuilder(tmp, "@" + paraName, paraValue);
    }
    
    // 3. 添加 Grid 容器
    StringBuilder tmp1 = new StringBuilder();
    if (frameTag.equals("Grid")) {
        tmp1.append("<div class='ewa-template-grid'>");
    }
    tmp1.append(tmp);
    if (frameTag.equals("Grid")) {
        tmp1.append("</div>");
    }
    
    return tmp1.toString();
}
```

### 5.3 模板配置示例

```xml
<FrameHtml>
    <Set FrameHtml="&lt;div class='product-card'&gt;
        &lt;img src='@IMAGE_URL' /&gt;
        &lt;h3&gt;@PRODUCT_NAME&lt;/h3&gt;
        &lt;p class='price'&gt;￥@PRICE&lt;/p&gt;
        &lt;p class='stock'&gt;库存：@STOCK&lt;/p&gt;
        &lt;button onclick='buy(@ID)'&gt;购买&lt;/button&gt;
    &lt;/div&gt;"/>
</FrameHtml>
```

---

## 6. createHtml 流程

```java
// FrameGrid.createHtml()
public void createHtml() throws Exception {
    HtmlDocument doc = this.getHtmlClass().getDocument();

    // 1. 皮肤头部
    super.createSkinTop();
    
    // 2. CSS
    super.createCss();
    
    // 3. 头部 JS
    super.createJsTop();

    // 4. 主体内容
    doc.addScriptHtml("<div>");
    this.createContent();
    doc.addScriptHtml("</div>");

    // 5. 皮肤底部
    this.createSkinBottom();
    
    // 6. 底部 JS
    this.createJsBottom();
    
    // 7. Frame 脚本
    this.createJsFramePage();
}

// FrameGrid.createContent()
public void createContent() throws Exception {
    HtmlDocument doc = this.getHtmlClass().getDocument();

    // 用户自定义头部 HTML
    String pageAddTop = this.getPageItemValue("AddHtml", "Top");
    doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop.trim());
    
    // Frame 内容
    createFrameContent();
}
```

---

## 7. 关键参数

### 7.1 URL 参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_GRID_AS` | 网格项标签类型 | `a`, `div`, `div2` |
| `EWA_GRID_TRANS` | 转置 (MultiGrid) | `1` |
| `ColSize` | 分栏数 (XML 配置) | `3`, `4`, `5` |
| `EWA_PAGECUR` | 当前页 | `EWA_PAGECUR=2` |
| `EWA_PAGESIZE` | 每页记录数 | `EWA_PAGESIZE=20` |
| `EWA_ROW_SIGN` | 行 MD5 签名 | `EWA_ROW_SIGN=1` |

### 7.2 XML 配置

```xml
<PageSize>
    <Set PageSize="20" IsSplitPage="1" KeyField="PRODUCT_ID" ColSize="4"/>
</PageSize>
<FrameHtml>
    <Set FrameHtml="&lt;div class='product-card'&gt;
        &lt;img src='@IMAGE_URL' /&gt;
        &lt;h3&gt;@PRODUCT_NAME&lt;/h3&gt;
        &lt;p&gt;￥@PRICE&lt;/p&gt;
    &lt;/div&gt;"/>
</FrameHtml>
```

---

## 8. CSS 样式

### 8.1 默认样式

```css
/* Grid 容器 */
.ewa-grid-frame {
    width: 100%;
    padding: 10px;
}

/* 单列模式 ul/li */
.ewa_grid_ul {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.ewa_grid_li {
    flex: 0 0 calc(25% - 10px);  /* 4 列 */
    box-sizing: border-box;
}

/* 模板容器 */
.ewa-template-grid {
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 10px;
    background: #fff;
    transition: box-shadow 0.3s;
}

.ewa-template-grid:hover {
    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

/* 多列模式 table */
.ewa-grid-frame table {
    width: 100%;
    border-collapse: collapse;
}

.ewa-grid-frame td {
    padding: 10px;
    vertical-align: top;
}
```

### 8.2 响应式样式

```css
/* 响应式：移动端 2 列 */
@media (max-width: 768px) {
    .ewa_grid_li {
        flex: 0 0 calc(50% - 10px);
    }
}

/* 响应式：小屏 1 列 */
@media (max-width: 480px) {
    .ewa_grid_li {
        flex: 0 0 100%;
    }
}
```

---

## 9. 分页支持

### 9.1 配置

```xml
<PageSize>
    <Set PageSize="20" IsSplitPage="1" KeyField="PRODUCT_ID" ColSize="4"/>
</PageSize>
```

### 9.2 获取记录总数

```java
// FrameGrid.createFrameContent()
DataConnection conn = super.getHtmlClass().getItemValues().getDataConn();
DataResult ds = conn.getLastResult();
if (ds != null && this.getHtmlClass().getUserConfig().getUserPageItem()
        .getSingleValue("PageSize", "IsSplitPage").equals("1")) {
    _ListFrameRecordCount = conn.getRecordCount(ds.getSqlOrigin());
}
```

### 9.3 分页栏

由于 Grid 继承自 FrameList，因此自动支持分页栏（如果配置了 ListUI 按钮）。

---

## 10. 完整示例

### 10.1 XML 配置

```xml
<EasyWebTemplate Name="products.G.V">
    <Page>
        <FrameTag><Set FrameTag="Grid"/></FrameTag>
        <Name><Set Name="products.G.V"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <PageSize>
            <Set PageSize="20" IsSplitPage="1" KeyField="PRODUCT_ID" ColSize="4"/>
        </PageSize>
        <FrameHtml>
            <Set FrameHtml="&lt;div class='product-card'&gt;
                &lt;img src='@IMAGE_URL' alt='@PRODUCT_NAME' /&gt;
                &lt;h3 class='product-name'&gt;@PRODUCT_NAME&lt;/h3&gt;
                &lt;p class='product-price'&gt;￥@PRICE&lt;/p&gt;
                &lt;p class='product-stock'&gt;库存：@STOCK&lt;/p&gt;
                &lt;button class='buy-btn' onclick='buy(@PRODUCT_ID)'&gt;购买&lt;/button&gt;
            &lt;/div&gt;"/>
        </FrameHtml>
    </Page>
    <Action>
        <ActionSet>
            <Set Type="OnPageLoad">
                <CallSet>
                    <Set CallName="loadProducts" CallType="SqlSet"/>
                </CallSet>
            </Set>
        </ActionSet>
        <SqlSet>
            <Set Name="loadProducts" SqlType="query">
                <Sql><![CDATA[
                    SELECT product_id, product_name, price, stock, image_url
                    FROM products
                    WHERE status='ACTIVE'
                    ORDER BY create_date DESC
                ]]></Sql>
            </Set>
        </SqlSet>
    </Action>
    <XItems>
        <XItem Name="IMAGE_URL">
            <Tag><Set Tag="image"/></Tag>
            <DataItem><Set DataField="image_url"/></DataItem>
        </XItem>
        <XItem Name="PRODUCT_NAME">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="product_name"/></DataItem>
        </XItem>
        <XItem Name="PRICE">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="price" Format="0.00"/></DataItem>
        </XItem>
        <XItem Name="STOCK">
            <Tag><Set Tag="span"/></Tag>
            <DataItem><Set DataField="stock"/></DataItem>
        </XItem>
        <XItem Name="PRODUCT_ID">
            <Tag><Set Tag="hidden"/></Tag>
            <DataItem><Set DataField="product_id"/></DataItem>
        </XItem>
    </XItems>
</EasyWebTemplate>
```

### 10.2 URL 调用

```
# 基本查询
/ewa?XMLNAME=products&ITEMNAME=G.V

# 指定分栏数
/ewa?XMLNAME=products&ITEMNAME=G.V

# 使用链接标签
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=a

# 使用 div 标签
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=div

# 分页
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_PAGECUR=2&EWA_PAGESIZE=20
```

### 10.3 生成的 HTML

```html
<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_abc123'>
    <ul class='ewa_grid_ul'>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="product_id"'>
            <div class='ewa-template-grid'>
                <img src='/images/product1.jpg' alt='产品 1' />
                <h3 class='product-name'>产品 1</h3>
                <p class='product-price'>￥100.00</p>
                <p class='product-stock'>库存：50</p>
                <button class='buy-btn' onclick='buy(1)'>购买</button>
            </div>
        </li>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="product_id"'>
            <!-- 更多产品... -->
        </li>
    </ul>
</div>
```

---

## 11. 调试技巧

### 11.1 启用 Debug

```
EWA_DEBUG_NO=1      # 不显示 Debug
EWA_DEBUG_KEY=xxx   # Debug 密钥
EWA_JS_DEBUG=1      # JS 调试模式
```

### 11.2 浏览器控制台

```javascript
// 获取 Grid 对象
EWA.F.FOS["grid_id"]

// 刷新 Grid
EWA.F.FOS["grid_id"].Reload()

// 获取当前页
EWA.F.FOS["grid_id"].PageCur
```

---

## 总结

Grid 执行流程关键点：

1. **继承 FrameList**
   - 拥有 ListFrame 的所有功能
   - 重写了 createContent() 和 createFrameContent()

2. **两种显示模式**
   - 单列模式 (colSize=0): ul/li 布局
   - 多列模式 (colSize>0): table/tr/td 布局

3. **标签类型配置**
   - 默认：li
   - 可选：a, div, div2

4. **模板支持**
   - FrameHtml 定义自定义模板
   - @参数替换

5. **分页支持**
   - 继承自 FrameList
   - 自动生成分页栏

6. **主要用途**
   - 产品列表
   - 图片展示
   - 卡片布局
