# Grid Execution Flow Detail

## Overview

Grid is a component in the EWA framework for displaying data in a grid/card layout, inheriting from FrameList, supporting single-column and multi-column display.

**Core Classes**:
- `FrameGrid` - Grid rendering class (inherits from FrameList)
- `ActionListFrame` - Grid action execution class (shared with ListFrame)

---

## 1. Complete Execution Flow

```
HTTP Request (/ewa?XMLNAME=products&ITEMNAME=G.V)
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
   │     └─► Read FrameTag = "Grid"
   │
   ├─► 2. Create FrameGrid instance
   │     this._Frame = new FrameGrid()
   │     this._Action = new ActionListFrame()
   │
   ├─► 3. FrameGrid.init(HtmlCreator)
   │
   ├─► 4. ActionListFrame.execute()
   │     ├─► Execute SqlSet (OnPageLoad)
   │     └─► Execute ClassSet (optional)
   │
   └─► 5. FrameGrid.createHtml()
         ├─► createSkinTop()      - Skin header
         ├─► createCss()          - CSS styles
         ├─► createJsTop()        - Header JS
         ├─► createContent()      - Main content (grid)
         │   └─► createFrameContent()
         │       ├─► Get data table
         │       ├─► Check column count (ColSize)
         │       └─► Generate grid HTML
         │           ├─► Single-column mode (ul/li)
         │           └─► Multi-column mode (table/tr/td)
         ├─► createSkinBottom()   - Skin footer
         ├─► createJsBottom()     - Bottom JS
         └─► createJsFramePage()  - Grid initialization script
```

---

## 2. FrameGrid Class Structure

### 2.1 Inheritance

```java
public class FrameGrid extends FrameList implements IFrame {
    // FrameGrid inherits from FrameList
    // Therefore it has all ListFrame functionality
    // But overrides createContent() and createFrameContent() methods
}
```

### 2.2 Differences from FrameList

| Feature | FrameList | FrameGrid |
|------|-----------|-----------|
| Display mode | Table list (table/tr/td) | Grid (ul/li or table/tr/td) |
| Column support | Supported (ColSize) | Supported (ColSize) |
| Pagination support | Supported | Supported (inherited) |
| Search support | Supported | Supported (inherited) |
| Button bar | Supported | Supported (inherited) |
| Primary use case | Data lists | Image/card grids |

---

## 3. createFrameContent Detail

### 3.1 Main Flow

```java
// FrameGrid.createFrameContent()
public void createFrameContent() throws Exception {
    HtmlDocument doc = this.getHtmlClass().getDocument();

    // 1. Skin-defined header
    doc.addScriptHtml("<div>");
    String top = super.createSkinFCTop();
    doc.addScriptHtml(top);

    // 2. Frame-defined header
    doc.addScriptHtml(createFrameHeader(), "frame head");

    // 3. Get data table
    MList tbs = super.getHtmlClass().getAction().getDTTables();
    if (tbs == null || tbs.size() == 0) {
        doc.addScriptHtml("no data");
    }
    
    DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
    super.getHtmlClass().getItemValues().setListFrameTable(tb);

    // 4. Get total record count (if pagination is enabled)
    DataConnection conn = super.getHtmlClass().getItemValues().getDataConn();
    DataResult ds = conn.getLastResult();
    if (ds != null && this.getHtmlClass().getUserConfig().getUserPageItem()
            .getSingleValue("PageSize", "IsSplitPage").equals("1")) {
        _ListFrameRecordCount = conn.getRecordCount(ds.getSqlOrigin());
    }

    // 5. Get column count
    int colSize = 0;  // Column count
    String s1 = super.getPageItemValue("PageSize", "ColSize");
    if (!(s1 == null || s1.trim().length() == 0)) {
        try {
            colSize = Integer.parseInt(s1);
        } catch (Exception e) {
            // nothing
        }
    }
    if (colSize <= 0) {
        colSize = 0;  // 0 means single-column mode
    }

    int colSizeInc = 1;
    MStr sb = new MStr();
    
    // 6. Create grid container
    sb.al("<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_" 
        + super.getHtmlClass().getSysParas().getFrameUnid() + "' >");
    
    RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();

    // 7. Get tag type (EWA_GRID_AS parameter)
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

    // 8. Check if template is used
    boolean isUseTemplate = super.isUsingTemplate();
    String frameTemplate = super.getPageItemValue("FrameHtml", "FrameHtml");

    // 9. Generate grid content
    if (colSize == 0) {
        // Single-column mode (ul/li)
        sb.al("<ul class='ewa_grid_ul'>");
        for (int i = 0; i < tb.getCount(); i++) {
            tb.getRow(i);  // Move current row
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
        // Multi-column mode (table/tr/td)
        sb.al("<table border='0' cellpadding='0' cellspacing='0' align='center'>");
        for (int i = 0; i < tb.getCount(); i++) {
            tb.getRow(i);  // Move current row
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
            
            // New row after filling colSize cells
            if (colSizeInc == colSize && colSize > 0) {
                sb.al("</tr>");
                sb.al("<tr>");
            }
            colSizeInc++;
            if (colSizeInc > colSize) {
                colSizeInc = 1;
            }
        }
        // Fill empty cells
        for (int i = colSizeInc; i <= colSize; i++) {
            sb.al("<td>&nbsp;</td>");
        }
        sb.a("</tr></table>");
    }

    sb.al("</div>");
    doc.addScriptHtml(sb.toString(), "Frame content");

    // 10. Skin-defined footer
    String bottom = super.createSkinFCBottom();
    doc.addScriptHtml(bottom);

    // 11. Frame-defined footer
    this.createFrameFooter();
    doc.addScriptHtml("</div>");
}
```

### 3.2 Single-Column Mode (ul/li)

```java
// When colSize == 0, use ul/li layout
if (colSize == 0) {
    sb.al("<ul class='ewa_grid_ul'>");
    for (int i = 0; i < tb.getCount(); i++) {
        tb.getRow(i);
        String keyExp = super.createItemKeys();
        String rowHtml = isUseTemplate 
            ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
            : super.createItemHtmls();

        // Use configured tag (li/div/a)
        sb.al("<" + tag0 + " class='ewa_grid_li' ewa_key=\"" + keyExp + "\">");
        sb.al(rowHtml);
        sb.al("</" + tag1 + ">");
    }
    sb.al("</ul>");
}
```

**Generated HTML**:
```html
<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_abc123'>
    <ul class='ewa_grid_ul'>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="id"'>
            <div class='ewa-template-grid'>
                <!-- Item content -->
                <img src='product.jpg' />
                <span>Product Name</span>
                <span>$100.00</span>
            </div>
        </li>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="id"'>
            <!-- More items... -->
        </li>
    </ul>
</div>
```

### 3.3 Multi-Column Mode (table/tr/td)

```java
// When colSize > 0, use table/tr/td layout
else {
    sb.al("<table border='0' cellpadding='0' cellspacing='0' align='center'>");
    for (int i = 0; i < tb.getCount(); i++) {
        tb.getRow(i);
        String keyExp = super.createItemKeys();
        String rowHtml = isUseTemplate 
            ? super.createItemHtmlsByFrameHtml(frameTemplate, "Grid")
            : super.createItemHtmls();

        // Start new row
        if (colSizeInc == 1) {
            sb.al("<tr>");
        }
        
        // Cell
        sb.al("<td " + keyExp + ">");
        sb.al(rowHtml);
        sb.al("</td>");
        
        // Close row when filled
        if (colSizeInc == colSize && colSize > 0) {
            sb.al("</tr>");
            sb.al("<tr>");
        }
        colSizeInc++;
        if (colSizeInc > colSize) {
            colSizeInc = 1;
        }
    }
    // Fill empty cells
    for (int i = colSizeInc; i <= colSize; i++) {
        sb.al("<td>&nbsp;</td>");
    }
    sb.a("</tr></table>");
}
```

**Generated HTML (3 columns)**:
```html
<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_abc123'>
    <table border='0' cellpadding='0' cellspacing='0' align='center'>
        <tr>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>Product 1</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>Product 2</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>Product 3</div>
            </td>
        </tr>
        <tr>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>Product 4</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>Product 5</div>
            </td>
            <td EWA_KEY="id">
                <div class='ewa-template-grid'>Product 6</div>
            </td>
        </tr>
        <!-- Fill empty cells in last row if incomplete -->
        <tr>
            <td EWA_KEY="id">Product 7</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
    </table>
</div>
```

---

## 4. Tag Type Configuration (EWA_GRID_AS)

### 4.1 Configuration Method

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

### 4.2 Tag Types

| Value | Tag | Usage |
|----|------|------|
| (empty) | `li` | Default, list item |
| `a` | `a` | Clickable link |
| `div` | `div` | Block element |
| `div2` | `div><div` | Double div wrapper |

### 4.3 Usage Example

```
# Use link tag
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=a

# Use div tag
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=div

# Use double div
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=div2
```

---

## 5. Template Support

### 5.1 Check Template Usage

```java
// FrameList.isUsingTemplate() (inherited)
public boolean isUsingTemplate() {
    RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
    // Check if template should be used
    String frameTemplate = super.getPageItemValue("FrameHtml", "FrameHtml");
    if (frameTemplate == null || frameTemplate.trim().length() == 0) {
        return false;
    }
    // Check if template is disabled
    if (rv.s(FrameParameters.EWA_LF_TEMP_NO) == null 
        || rv.s(FrameParameters.EWA_TEMP_NO) == null) {
        return true;
    }
    return false;
}
```

### 5.2 Generate HTML Using Template

```java
// FrameList.createItemHtmlsByFrameHtml() (inherited)
public String createItemHtmlsByFrameHtml(String template, String frameTag) throws Exception {
    ItemValues ivs = super.getHtmlClass().getItemValues();
    Map<String, String> cellData = new HashMap<String, String>();
    
    // 1. Iterate XItems, generate HTML for each field
    for (int i = 0; i < super.getHtmlClass().getUserConfig().getUserXItems().count(); i++) {
        UserXItem uxi = super.getHtmlClass().getUserConfig().getUserXItems().getItem(i);
        
        // Skip hidden fields
        if (this.isHiddenField(uxi.getName())) {
            continue;
        }
        
        IItem item = super.getHtmlClass().getItem(uxi);
        String itemHtml = item.createItemHtml();
        
        // Replace @ parameters
        if (itemHtml.indexOf("@") >= 0) {
            itemHtml = super.getHtmlClass().getItemValues()
                .replaceParameters(itemHtml, false, false);
        }
        cellData.put(uxi.getName().toUpperCase(), itemHtml);
    }
    
    // 2. Replace @ parameters in template
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
    
    // 3. Add Grid container
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

### 5.3 Template Configuration Example

```xml
<FrameHtml>
    <Set FrameHtml="&lt;div class='product-card'&gt;
        &lt;img src='@IMAGE_URL' /&gt;
        &lt;h3&gt;@PRODUCT_NAME&lt;/h3&gt;
        &lt;p class='price'&gt;$@PRICE&lt;/p&gt;
        &lt;p class='stock'&gt;Stock: @STOCK&lt;/p&gt;
        &lt;button onclick='buy(@ID)'&gt;Buy&lt;/button&gt;
    &lt;/div&gt;"/>
</FrameHtml>
```

---

## 6. createHtml Flow

```java
// FrameGrid.createHtml()
public void createHtml() throws Exception {
    HtmlDocument doc = this.getHtmlClass().getDocument();

    // 1. Skin header
    super.createSkinTop();
    
    // 2. CSS
    super.createCss();
    
    // 3. Header JS
    super.createJsTop();

    // 4. Main content
    doc.addScriptHtml("<div>");
    this.createContent();
    doc.addScriptHtml("</div>");

    // 5. Skin footer
    this.createSkinBottom();
    
    // 6. Bottom JS
    this.createJsBottom();
    
    // 7. Frame script
    this.createJsFramePage();
}

// FrameGrid.createContent()
public void createContent() throws Exception {
    HtmlDocument doc = this.getHtmlClass().getDocument();

    // User custom top HTML
    String pageAddTop = this.getPageItemValue("AddHtml", "Top");
    doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop.trim());
    
    // Frame content
    createFrameContent();
}
```

---

## 7. Key Parameters

### 7.1 URL Parameters

| Parameter | Description | Example |
|------|------|------|
| `EWA_GRID_AS` | Grid item tag type | `a`, `div`, `div2` |
| `EWA_GRID_TRANS` | Transpose (MultiGrid) | `1` |
| `ColSize` | Column count (XML config) | `3`, `4`, `5` |
| `EWA_PAGECUR` | Current page | `EWA_PAGECUR=2` |
| `EWA_PAGESIZE` | Records per page | `EWA_PAGESIZE=20` |
| `EWA_ROW_SIGN` | Row MD5 signature | `EWA_ROW_SIGN=1` |

### 7.2 XML Configuration

```xml
<PageSize>
    <Set PageSize="20" IsSplitPage="1" KeyField="PRODUCT_ID" ColSize="4"/>
</PageSize>
<FrameHtml>
    <Set FrameHtml="&lt;div class='product-card'&gt;
        &lt;img src='@IMAGE_URL' /&gt;
        &lt;h3&gt;@PRODUCT_NAME&lt;/h3&gt;
        &lt;p&gt;$@PRICE&lt;/p&gt;
    &lt;/div&gt;"/>
</FrameHtml>
```

---

## 8. CSS Styles

### 8.1 Default Styles

```css
/* Grid container */
.ewa-grid-frame {
    width: 100%;
    padding: 10px;
}

/* Single-column mode ul/li */
.ewa_grid_ul {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.ewa_grid_li {
    flex: 0 0 calc(25% - 10px);  /* 4 columns */
    box-sizing: border-box;
}

/* Template container */
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

/* Multi-column mode table */
.ewa-grid-frame table {
    width: 100%;
    border-collapse: collapse;
}

.ewa-grid-frame td {
    padding: 10px;
    vertical-align: top;
}
```

### 8.2 Responsive Styles

```css
/* Responsive: 2 columns on mobile */
@media (max-width: 768px) {
    .ewa_grid_li {
        flex: 0 0 calc(50% - 10px);
    }
}

/* Responsive: 1 column on small screens */
@media (max-width: 480px) {
    .ewa_grid_li {
        flex: 0 0 100%;
    }
}
```

---

## 9. Pagination Support

### 9.1 Configuration

```xml
<PageSize>
    <Set PageSize="20" IsSplitPage="1" KeyField="PRODUCT_ID" ColSize="4"/>
</PageSize>
```

### 9.2 Get Total Record Count

```java
// FrameGrid.createFrameContent()
DataConnection conn = super.getHtmlClass().getItemValues().getDataConn();
DataResult ds = conn.getLastResult();
if (ds != null && this.getHtmlClass().getUserConfig().getUserPageItem()
        .getSingleValue("PageSize", "IsSplitPage").equals("1")) {
    _ListFrameRecordCount = conn.getRecordCount(ds.getSqlOrigin());
}
```

### 9.3 Pagination Bar

Since Grid inherits from FrameList, it automatically supports pagination bar (if ListUI buttons are configured).

---

## 10. Complete Example

### 10.1 XML Configuration

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
                &lt;p class='product-price'&gt;$@PRICE&lt;/p&gt;
                &lt;p class='product-stock'&gt;Stock: @STOCK&lt;/p&gt;
                &lt;button class='buy-btn' onclick='buy(@PRODUCT_ID)'&gt;Buy&lt;/button&gt;
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

### 10.2 URL Calls

```
# Basic query
/ewa?XMLNAME=products&ITEMNAME=G.V

# Specify column count
/ewa?XMLNAME=products&ITEMNAME=G.V

# Use link tag
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=a

# Use div tag
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_GRID_AS=div

# Paginated
/ewa?XMLNAME=products&ITEMNAME=G.V&EWA_PAGECUR=2&EWA_PAGESIZE=20
```

### 10.3 Generated HTML

```html
<div class='EWA_TABLE ewa-grid-frame' id='EWA_LF_abc123'>
    <ul class='ewa_grid_ul'>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="product_id"'>
            <div class='ewa-template-grid'>
                <img src='/images/product1.jpg' alt='Product 1' />
                <h3 class='product-name'>Product 1</h3>
                <p class='product-price'>$100.00</p>
                <p class='product-stock'>Stock: 50</p>
                <button class='buy-btn' onclick='buy(1)'>Buy</button>
            </div>
        </li>
        <li class='ewa_grid_li' ewa_key='EWA_KEY="product_id"'>
            <!-- More products... -->
        </li>
    </ul>
</div>
```

---

## 11. Debugging Tips

### 11.1 Enable Debug

```
EWA_DEBUG_NO=1      # Do not show Debug
EWA_DEBUG_KEY=xxx   # Debug key
EWA_JS_DEBUG=1      # JS debug mode
```

### 11.2 Browser Console

```javascript
// Get Grid object
EWA.F.FOS["grid_id"]

// Refresh Grid
EWA.F.FOS["grid_id"].Reload()

// Get current page
EWA.F.FOS["grid_id"].PageCur
```

---

## Summary

Key points of Grid execution flow:

1. **Inherits FrameList**
   - Has all ListFrame functionality
   - Overrides createContent() and createFrameContent()

2. **Two Display Modes**
   - Single-column mode (colSize=0): ul/li layout
   - Multi-column mode (colSize>0): table/tr/td layout

3. **Tag Type Configuration**
   - Default: li
   - Options: a, div, div2

4. **Template Support**
   - FrameHtml defines custom template
   - @ parameter replacement

5. **Pagination Support**
   - Inherited from FrameList
   - Auto-generated pagination bar

6. **Primary Use Cases**
   - Product lists
   - Image galleries
   - Card layouts
