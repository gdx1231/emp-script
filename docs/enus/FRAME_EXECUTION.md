# Frame Execution Flow Detail

## Overview

Frame is the core component in the EWA framework for form input/modification, supporting three modes: New (N), Modify (M), and Copy (C).

**Core Classes**:
- `FrameFrame` - Frame rendering class
- `ActionFrame` - Frame action execution class
- `FrameBase` - Frame base class

---

## 1. Complete Execution Flow

```
HTTP Request (/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N)
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
   │     └─► Read FrameTag = "Frame"
   │
   ├─► 2. Create FrameFrame instance
   │     this._Frame = new FrameFrame()
   │     this._Action = new ActionFrame()
   │
   ├─► 3. FrameFrame.init(HtmlCreator)
   │
   ├─► 4. ActionFrame.execute()
   │     ├─► Initialize upload parameters (initUploadParas)
   │     ├─► Generate upload file parameters (createUploadsParas)
   │     └─► Execute SqlSet (OnPageLoad/OnPagePost)
   │
   └─► 5. FrameFrame.createHtml()
         ├─► createHtmlTraditional()  // Traditional mode
         │   ├─► createSkinTop()      - Skin header
         │   ├─► createCss()          - CSS styles
         │   ├─► createJsTop()        - Header JS
         │   ├─► createContent()      - Main content (form)
         │   │   ├─► createFrameContent()
         │   │   │   ├─► Check redraw mode (EWA_REDRAW)
         │   │   │   ├─► Check user custom HTML
         │   │   │   └─► createItemHtmls() - Generate form items
         │   │   ├─► createFrameHeader()   - Form header
         │   │   └─► createFrameFooter()   - Form footer
         │   ├─► createJsFramePage()  - Frame initialization script
         │   ├─► createSkinBottom()   - Skin footer
         │   └─► createJsBottom()     - Bottom JS
         │
         └─► createHtmlVue()  // Vue mode (EWA_VUE=1)
```

---

## 2. ActionFrame Execution Detail

### 2.1 Execution Entry

```java
// ActionFrame.executeCallSql()
public void executeCallSql(String name) throws Exception {
    // 1. Initialize upload parameters
    if (this.initUploadParas()) {
        this.createUploadsParas();  // Generate upload file parameters
    }
    
    // 2. Execute SQL
    super.executeCallSql(name);
}

// ActionFrame.initUploadParas() - Initialize upload parameters
private boolean initUploadParas() {
    if (super.getHtmlClass() == null) {
        return false;
    }
    
    UserXItems items = super.getHtmlClass().getUserConfig().getUserXItems();
    _Uploads = new MTable();
    
    // Iterate XItems, find upload-related fields
    for (int i = 0; i < items.count(); i++) {
        UserXItem item = items.getItem(i);
        String tag = item.getSingleValue("tag");
        
        // Check if it is an upload type
        if (!(tag.equalsIgnoreCase("swffile") 
              || tag.equalsIgnoreCase("image")
              || tag.equalsIgnoreCase("h5upload"))) {
            continue;
        }
        
        if (!item.testName("Upload")) {
            continue;
        }
        
        UserXItemValue u = item.getItem("Upload").getItem(0);
        if (u.testName("UpSaveMethod")) {
            String upSaveMethod = u.getItem("UpSaveMethod");
            if (upSaveMethod != null) {
                _Uploads.add(item.getName(), item);
            }
        }
    }
    
    return _Uploads.getCount() > 0;
}

// ActionFrame.createUploadsParas() - Generate upload file parameters
void createUploadsParas() throws Exception {
    RequestValue rv = super.getRequestValue();
    
    // Prevent duplicate execution
    if (rv.getString("____createUploadPara____") != null) {
        return;
    }
    
    for (int i = 0; i < this._Uploads.getCount(); i++) {
        UserXItem item = (UserXItem) this._Uploads.getByIndex(i);
        this.createUploadPara(item);
    }
}

// ActionFrame.createUploadPara() - Generate single upload file parameter
void createUploadPara(UserXItem item) throws Exception {
    RequestValue rv = super.getRequestValue();
    String uploadName = item.getName();
    
    // Get upload configuration
    UserXItemValue u = item.getItem("Upload").getItem(0);
    String p1 = u.getItem("UpPath");
    String p = super.getItemValues().replaceParameters(p1, false);
    
    // Check if JSON is encrypted
    boolean upJsonEncyrpt = true;
    if (u.testName("UpJsonEncyrpt")) {
        String val = u.getItem("UpJsonEncyrpt");
        if ("no".equalsIgnoreCase(val)) {
            upJsonEncyrpt = false;
        }
    }
    
    // Get upload file name
    String upName = rv.getString("UP_NAME");
    JSONObject item1 = null;
    JSONArray arr = null;
    
    // Data type
    String dataType = item.getSingleValue("DataItem", "DataType");
    String tag = item.getSingleValue("Tag");
    
    if (upName == null) {
        String json = rv.getString(uploadName);
        if (json == null || json.trim().length() == 0) {
            removeUpload(item);
            return;
        }
        
        if (json.trim().startsWith("[")) {
            // JSON array (potentially multiple files)
            JSONArray arrEncrypted = new JSONArray(json);
            if (arrEncrypted.length() == 0) {
                removeUpload(item);
                return;
            }
            
            if (upJsonEncyrpt) {
                // AES decryption
                arr = new JSONArray();
                for (int i = 0; i < arrEncrypted.length(); i++) {
                    JSONObject encryptUploadJson = arrEncrypted.getJSONObject(i);
                    if (encryptUploadJson.has("UP")) {
                        String decrypt = UAes.getInstance().decrypt(encryptUploadJson.getString("UP"));
                        JSONObject decryptedJson = new JSONObject(decrypt);
                        arr.put(decryptedJson);
                    }
                }
            } else {
                arr = arrEncrypted;
            }
            
            if (arr.length() == 0) {
                removeUpload(item);
                return;
            }
            
            // Get first file info
            item1 = arr.getJSONObject(0);
            if (item1.has("UP_NAME")) {
                upName = item1.getString("UP_NAME");
            }
        }
    }
    
    if (upName == null) {
        removeUpload(item);
        return;
    }
    
    // Check path validity
    if (upName.indexOf("./") >= 0 || upName.indexOf(".\\") >= 0) {
        LOGGER.warn("InValid upName:" + upName);
        removeUpload(item);
        return;
    }
    
    // Build file path
    String p_short = p.replace("\\", "/").replace("//", "/");
    String shortPath = p_short + (p_short.endsWith("/") ? "" : "/") + upName;
    String fileName = UPath.getPATH_UPLOAD() + File.separator + shortPath;
    
    File f = new File(fileName);
    
    // Add marker (prevent duplicate execution)
    rv.addValue("____createUploadPara____", "ADDED");
    
    // Check if file exists
    if (!f.exists()) {
        if (arr != null) {
            rv.addValue(uploadName + "_JSON", arr.toString());
        }
        
        // Check if delete file is configured
        if (u.testName("UpDelete")) {
            String isDel = u.getItem("UpDelete");
            if ("yes".equalsIgnoreCase(isDel)) {
                return;
            }
        }
        
        throw new Exception("The uploaded file not exists: " + f.getAbsolutePath());
    }
    
    if (!f.isFile() || !f.canRead()) {
        if (arr != null) {
            rv.addValue(uploadName + "_JSON", arr.toString());
        }
        return;
    }
    
    // Process file data
    if (dataType != null && dataType.equalsIgnoreCase("binary")) {
        long m10 = 1024 * 1024 * 10;  // 10M
        if (f.length() <= m10) {
            // Read file binary (only read files under 10M)
            byte[] buf = UFile.readFileBytes(f.getAbsolutePath());
            if (rv.getPageValues().getValue(uploadName) == null) {
                rv.addValue(uploadName, buf, "binary", buf.length);
            } else {
                rv.changeValue(uploadName, buf, "binary", buf.length);
            }
        }
    } else {
        // String type (store URL address)
        if (item1 != null) {
            String file_url = item1.getString("UP_URL");
            if (rv.getPageValues().getValue(uploadName) == null) {
                rv.addValue(uploadName, file_url);
            } else {
                rv.changeValue(uploadName, file_url, "string", file_url.length());
            }
        }
    }
    
    // Add additional parameters
    if (arr != null) {
        rv.addValue(uploadName + "_JSON", arr.toString());
    }
    
    // File MD5
    String md5 = UFile.md5(f);
    rv.addValue(uploadName + "_MD5", md5);
    
    // File name
    rv.addValue(uploadName + "_NAME", f.getName());
    
    // File full path
    rv.addValue(uploadName + "_PATH", f.getAbsolutePath());
    
    // File relative path
    rv.addValue(uploadName + "_PATH_SHORT", shortPath);
    
    // File size
    rv.addValue(uploadName + "_SIZE", f.length());
    rv.addValue(uploadName + "_LENGTH", f.length());
    
    // File extension
    rv.addValue(uploadName + "_EXT", UFile.getFileExt(f.getName()));
}
```

### 2.2 Execute SQL

```java
// ActionBase.executeCallSql()
public void executeCallSql(String name) throws Exception {
    ActionSqlSetItem sqlItem = this.retActionSqlSetItem(name);
    String[] sqlArray = sqlItem.getSqlArray();
    
    DataConnection cnn = this.getItemValues().getSysParas().getDataConn();
    
    for (int i = 0; i < sqlArray.length; i++) {
        String sql = sqlArray[i].trim();
        if (sql.length() == 0) continue;
        
        String sqlType = sqlItem.getSqlType();  // query / update
        
        // Execute SQL
        this.executeSql(sql, sqlType, name);
        
        // Check errors
        if (StringUtils.isNotBlank(cnn.getErrorMsg())) {
            throw new Exception(cnn.getErrorMsg());
        }
        
        // Check user-defined errors
        if (StringUtils.isNotBlank(this.getChkErrorMsg())) {
            return;
        }
    }
    
    // Execute Session/Cookie operations
    this.executeSessionsCookies(sqlItem.getUserXItemValue());
}

// ActionBase.executeSql() - Execute single SQL
public boolean executeSql(String sql, String sqlType, String name) {
    try {
        sql = this.createSql(sql);  // Allow subclass to modify SQL
        
        if (sqlType.equalsIgnoreCase("procedure")) {
            // Stored procedure
            this.executeSqlProcdure(sql);
        } else if (sqlType.equalsIgnoreCase("query")) {
            // Query
            DTTable dt = this.executeSqlQuery(sql);
            dt.setName(name);
            if (dt.isOk()) {
                super.getDTTables().add(dt);
            }
        } else if (sqlType.equalsIgnoreCase("update")) {
            // Update
            this.executeSqlUpdate(sql);
        }
        
        return true;
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        return false;
    }
}

// ActionBase.executeSqlQuery() - Execute query
public DTTable executeSqlQuery(String sql) {
    DataConnection conn = this.getItemValues().getSysParas().getDataConn();
    conn.executeQuery(sql);
    return DTTable.returnTable(conn);
}

// ActionBase.executeSqlUpdate() - Execute update
public void executeSqlUpdate(String sql) {
    DataConnection conn = this.getItemValues().getSysParas().getDataConn();
    conn.executeUpdate(sql);
}
```

### 2.3 Execute Session/Cookie Operations

```java
// ActionBase.executeSessionsCookies()
public void executeSessionsCookies(UserXItemValue userXItemValue) throws Exception {
    if (userXItemValue.getChildren().size() == 0) {
        return;
    }
    
    Iterator<String> it = userXItemValue.getChildren().keySet().iterator();
    while (it.hasNext()) {
        String key = it.next();
        UserXItemValues subVals = userXItemValue.getChildren().get(key);
        
        for (int i = 0; i < subVals.count(); i++) {
            UserXItemValue s = subVals.getItem(i);
            this.executeSessionCookie(s);
        }
    }
}

// ActionBase.executeSessionCookie()
public void executeSessionCookie(UserXItemValue uxv) throws Exception {
    String type = uxv.getItem("CSType");  // all / session / cookie
    
    if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("session")) {
        this.executeSession(uxv);
    }
    
    if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("cookie")) {
        this.executeCookie(uxv);
    }
}

// ActionBase.executeSession() - Execute Session operation
private void executeSession(UserXItemValue uxv) throws Exception {
    RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();
    if (rv.getSession() == null) {
        LOGGER.warn("No session");
        return;
    }
    
    String name = uxv.getItem("Name").trim().toUpperCase();
    String option = uxv.getItem("Option");  // C=create, D=delete
    String paraName = uxv.getItem("ParaName");
    String val = this.getItemValues().getValue(paraName, paraName);
    
    if (val == null) {
        // Empty value deletes Session
        rv.getSession().removeAttribute(name);
    } else if (option.equalsIgnoreCase("C")) {
        // Create Session
        rv.getSession().setAttribute(name, val);
        this._OutSessions.put(name, val);  // Record output Session
    } else {
        rv.getSession().removeAttribute(name);
    }
}

// ActionBase.executeCookie() - Execute Cookie operation
private void executeCookie(UserXItemValue uxv) throws Exception {
    // Get encryption configuration
    if (ConfSecurities.getInstance().getDefaultSecurity() == null) {
        throw new Exception("No default symmetric defined");
    }
    IUSymmetricEncyrpt security = ConfSecurities.getInstance().getDefaultSecurity().createSymmetric();
    
    RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();
    
    String name = uxv.getItem("Name").trim().toUpperCase();
    String option = uxv.getItem("Option");  // C=create, D=delete
    String domain = uxv.getItem("Domain");
    String life = uxv.getItem("Life");
    String paraName = uxv.getItem("ParaName");
    String val = this.getItemValues().getValue(paraName, paraName);
    
    if (val == null) {
        // Delete Cookie
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        if (domain != null && domain.trim().length() > 0) {
            cookie.setPath(domain);
        }
        rv.getResponse().addCookie(cookie);
    } else if (option.equalsIgnoreCase("C")) {
        // Create Cookie (encrypted)
        String encrypted = security.encryptoString(val);
        Cookie cookie = new Cookie(name, encrypted);
        if (life != null && life.trim().length() > 0) {
            cookie.setMaxAge(Integer.parseInt(life));
        }
        if (domain != null && domain.trim().length() > 0) {
            cookie.setPath(domain);
        }
        rv.getResponse().addCookie(cookie);
        this._OutCookies.put(name, val);  // Record output Cookie
    } else {
        // Delete Cookie
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        rv.getResponse().addCookie(cookie);
    }
}
```

---

## 3. FrameFrame Rendering Detail

### 3.1 createHtml Flow

```java
// FrameFrame.createHtml()
public void createHtml() throws Exception {
    if (super.getHtmlClass().getSysParas().isVue()) {
        super.createHtmlVue();  // Vue mode
    } else {
        this.createHtmlTraditional();  // Traditional mode
    }
}

// FrameFrame.createHtmlTraditional()
public void createHtmlTraditional() throws Exception {
    // 1. Skin header
    try {
        super.createSkinTop();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createSkinTop");
    
    // 2. CSS
    try {
        super.createCss();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createCss");
    
    // 3. Header JS
    super.createJsTop();
    super.addDebug(this, "HTML", "createJsTop");
    
    // 4. Main content
    try {
        this.createContent();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createContent");
    
    // 5. Frame script
    try {
        this.createJsFramePage();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createJsFramePage");
    
    // 6. Skin footer
    try {
        super.createSkinBottom();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createSkinBottom");
    
    // 7. Bottom JS
    super.createJsBottom();
    super.addDebug(this, "HTML", "createJsBottom");
}
```

### 3.2 Create Form Content

```java
// FrameFrame.createContent()
public void createContent() throws Exception {
    RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
    HtmlDocument doc = this.getHtmlClass().getDocument();
    
    // Get user custom HTML
    String userHtml = this.getUserHtml();
    
    // Check redraw mode
    String ewa_redraw = rv.s(FrameParameters.EWA_REDRAW);
    if (ewa_redraw != null) {
        userHtml = "";  // Redraw mode ignores user custom HTML
    }
    
    // User custom top HTML
    String pageAddTop = this.getPageItemValue("AddHtml", "Top");
    if (pageAddTop != null) {
        doc.addScriptHtml(pageAddTop);
        doc.addFrameHtml(pageAddTop);
    }
    
    if (userHtml.trim().length() == 0) {
        // Use skin-defined header
        MStr sb = new MStr();
        sb.append("<!--Skin-defined header-->");
        
        String skinTop = super.createSkinFCTop();
        
        // Add CSS class based on MTYPE
        String mtypeCss;
        if ("M".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
            mtypeCss = " ewa-mtype-m";  // Modify mode
        } else if ("N".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
            mtypeCss = " ewa-mtype-n";  // New mode
        } else if ("C".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
            mtypeCss = " ewa-mtype-c";  // Copy mode
        } else {
            mtypeCss = "";
        }
        skinTop = skinTop.replace("{EWA_MTYPE}", mtypeCss);
        
        sb.append(skinTop);
        
        // Frame-defined header
        sb.append("<!--Frame-defined header-->");
        sb.append(createFrameHeader());
        
        doc.addScriptHtml(sb.toString());
        doc.addFrameHtml(sb.toString());
    }
    
    // Frame content
    createFrameContent();
    
    if (userHtml.trim().length() == 0) {
        // Frame-defined footer
        this.createFrameFooter();
        
        // Skin-defined footer
        String bottom = super.createSkinFCBottom();
        doc.addScriptHtml(bottom);
        doc.addFrameHtml(bottom);
    }
}

// FrameFrame.createFrameContent()
public void createFrameContent() throws Exception {
    RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
    MStr sb = new MStr();
    UserConfig uc = this.getHtmlClass().getUserConfig();
    
    // Check redraw mode
    String ewa_redraw = rv.getString(FrameParameters.EWA_REDRAW);
    if (ewa_redraw != null && ewa_redraw.equals("1") 
        && uc.getUserPageItem().testName("RedrawJson")) {
        
        String RedrawJson = uc.getUserPageItem().getSingleValue("RedrawJson");
        if (RedrawJson.trim().length() > 0) {
            _IsRedrawJson = true;
            try {
                JSONObject obj = new JSONObject(RedrawJson);
                String html = createRedrawJsonItemHtmls(obj);
                sb.al(html);
                this.getHtmlClass().getDocument().addScriptHtml(sb.toString(), "FRAME CONTENT");
                return;
            } catch (Exception err) {
                String sss = "ERROR:" + err.getMessage() + "<br>" + RedrawJson;
                this.getHtmlClass().getDocument().addScriptHtml(sss, "FRAME CONTENT");
                return;
            }
        }
    }
    
    // Get user custom HTML
    String userHtml = this.getUserHtml();
    
    if (userHtml.trim().length() > 0 && !_IsRedrawJson 
        && rv.s(FrameParameters.EWA_TEMP_NO) == null) {
        // User custom framework
        sb.append(this.createFrameContentUserHtml());
    } else {
        // Standard framework
        sb.append(this.createItemHtmls());
    }
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString(), "FRAME CONTENT");
    this.getHtmlClass().getDocument().addFrameHtml(sb.toString());
}

// FrameFrame.createItemHtmls() - Generate form items
public String createItemHtmls() throws Exception {
    MStr sb = new MStr();
    UserConfig uc = this.getHtmlClass().getUserConfig();
    String lang = this.getHtmlClass().getSysParas().getLang();
    
    // Check if grouped
    this.initGroupInfo();
    
    if (this._IsGroup) {
        // Grouped display
        for (int i = 0; i < this._GroupInfos.length; i++) {
            String grp = this._GroupInfos[i];
            sb.append("<fieldset><legend>" + grp + "</legend>");
            this.renderGroup(grp, sb);
            sb.append("</fieldset>");
        }
    } else {
        // Non-grouped display
        this.renderAll(sb);
    }
    
    return sb.toString();
}

// FrameFrame.renderAll() - Render all fields
private void renderAll(MStr sb) throws Exception {
    UserConfig uc = this.getHtmlClass().getUserConfig();
    String lang = this.getHtmlClass().getSysParas().getLang();
    
    // Get data
    DTTable tb = null;
    MList tbs = super.getHtmlClass().getAction().getDTTables();
    if (tbs != null && tbs.size() > 0) {
        tb = (DTTable) tbs.get(tbs.size() - 1);
    }
    
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
        
        // Get field value
        String value = "";
        if (tb != null && tb.getCount() > 0) {
            String dataField = uxi.getItem("DataItem").getItem(0).getItem("DataField");
            value = tb.getCell(0, dataField).toString();
        }
        
        // Render Item
        String html = item.render();
        
        // Add to form
        sb.append("<div class='EWA_TR'>");
        sb.append("<td class='EWA_TD_M'>" + item.getDescription() + "</td>");
        sb.append("<td class='EWA_TD_M'>" + html + "</td>");
        sb.append("</div>");
    }
}

// FrameFrame.createFrameHeader() - Create form header
public String createFrameHeader() throws Exception {
    MStr sb = new MStr();
    
    // Check if title is hidden
    if (super.isHiddenCaption()) {
        return "";
    }
    
    // Get skin-defined header
    String header = super.createSkinFCHeader();
    
    sb.append("<TR EWA_TAG='HEADER'>");
    String pageDescription = Utils.textToInputValue(
        super.getHtmlClass().getSysParas().getTitle());
    sb.append(header.replace(SkinFrame.TAG_ITEM, pageDescription));
    sb.append("</tr>");
    
    // Get ColSpan
    int colSpan = this.getFrameColSize();
    return sb.toString().replace("3", colSpan + "");
}

// FrameFrame.getFrameColSize() - Get form column count
private int getFrameColSize() {
    RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
    
    // Parameter takes priority
    String frameCols = rv.s(FrameParameters.EWA_FRAME_COLS);
    if (frameCols != null && frameCols.trim().length() > 0) {
        return parseFrameCols(frameCols);
    }
    
    // Configuration default
    String defaultCols = getPageItemValue("FrameHtml", "FrameCols");
    return parseFrameCols(defaultCols);
}

private int parseFrameCols(String frameCols) {
    if (frameCols == null) return 3;
    
    frameCols = frameCols.trim().toUpperCase();
    if (frameCols.equals("C2") || frameCols.equals("2")) {
        return 2;  // 2 columns (no notes area)
    } else if (frameCols.equals("C1") || frameCols.equals("1")) {
        return 1;  // 1 column (no title area)
    } else if (frameCols.equals("C11")) {
        return 1;  // Vertical layout
    }
    return 3;  // Default 3 columns
}
```

### 3.3 Redraw Mode

```java
// FrameFrame.createRedrawJsonItemHtmls() - Create redraw JSON form items
private String createRedrawJsonItemHtmls(JSONObject redrawJson) throws Exception {
    MStr sb = new MStr();
    UserConfig uc = this.getHtmlClass().getUserConfig();
    String lang = this.getHtmlClass().getSysParas().getLang();
    
    // Build Item mapping
    HashMap<String, ArrayList<String>> itemMap = new LinkedHashMap<String, ArrayList<String>>();
    for (int i = 0; i < uc.getUserXItems().count(); i++) {
        UserXItem uxi = uc.getUserXItems().getItem(i);
        
        String des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);
        String memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);
        
        IItem item = super.getHtmlClass().getItem(uxi);
        String itemHtml = item.createItemHtml();
        
        // Replace description
        if (itemHtml.indexOf(SkinFrame.TAG_DES) >= 0) {
            itemHtml = itemHtml.replace(SkinFrame.TAG_DES, des);
        }
        if (itemHtml.indexOf(SkinFrame.TAG_MSG) >= 0) {
            itemHtml = itemHtml.replace(SkinFrame.TAG_MSG, memo);
        }
        
        // Special handling
        String tag = uxi.getSingleValue("Tag");
        if (tag.equalsIgnoreCase("textarea")) {
            itemHtml = itemHtml.replaceFirst(">", 
                " placeholder=\"" + Matcher.quoteReplacement(des) + "\">");
        }
        
        // Save to mapping
        ArrayList<String> al = new ArrayList<String>();
        al.add(itemHtml);  // HTML
        al.add(des);       // Description
        al.add(memo);      // Notes
        al.add(uxi.getSingleValue("IsMustInput"));  // Required
        al.add(tag);       // Type
        itemMap.put(uxi.getName(), al);
    }
    
    // Get redraw configuration
    int rows = redrawJson.getInt("rows");
    int cols = redrawJson.getInt("cols");
    
    // Create message box
    sb.append("<tr class='ewa-row-msg-box'>");
    sb.append("<td class='ewa_msg_box' colspan='" + cols * 2 + "'>");
    sb.append("<div><span class='ewa_msg_tip'>Tip</span>");
    sb.append("<span class='ewa_msg_err'></span></div></td></tr>");
    
    // Create grid
    for (int i = 0; i < rows; i++) {
        sb.al("<tr class='ewa-row-" + i + "'>");
        for (int m = 0; m < cols; m++) {
            String tmp = i + "`" + m;
            sb.append("<td class='ewa_redraw_info' col='" + m + "' row='" + i + "'>"
                + "[!@#!@#--342''" + tmp + "0]</td>");
            sb.append("<td class='ewa_redraw_ctl' col='" + m + "' row='" + i + "'>"
                + "[!@#!@#--342、、" + tmp + "1]</td>");
        }
        sb.al("</tr>");
    }
    
    // Map configuration
    JSONArray map = redrawJson.getJSONArray("map");
    for (int i = 0; i < map.length(); i++) {
        JSONObject o = map.getJSONObject(i);
        String id = o.getString("id");
        String col = o.getString("col");
        String row = o.getString("row");
        
        String html = id;
        String des = "Not found";
        String tag = "";
        
        if (itemMap.containsKey(id)) {
            html = itemMap.get(id).get(0);
            des = "<div class='ewa_d0'><div class='ewa_d1'>" + itemMap.get(id).get(1) + "</div>";
            
            // Required input marker
            String IsMustInput = itemMap.get(id).get(3);
            if (IsMustInput.equals("1")) {
                des = des + " <span class='ewa_must'>*</span>";
            }
            des += "</div>";
            
            tag = itemMap.get(id).get(4);
            itemMap.remove(id);
        }
        
        String tmp = row + "`" + col;
        
        if (!isRedraw2Col(tag)) {
            sb.replace("[!@#!@#--342''" + tmp + "0]", des);
            sb.replace("[!@#!@#--342、、" + tmp + "1]", html);
        } else {
            // Cross 2-column objects (buttons, etc.)
            sb.replace("[!@#!@#--342''" + tmp + "0]", html);
        }
    }
    
    // Clear unused markers
    for (int i = 0; i < rows; i++) {
        for (int m = 0; m < cols; m++) {
            String tmp = i + "`" + m;
            sb.replace("[DSP" + tmp + "0]", "style='display:none'");
            sb.replace("[DSP" + tmp + "1]", "style='display:none'");
            sb.replace("[!@#!@#--342、、" + tmp + "1]", "");
            sb.replace("[!@#!@#--342''" + tmp + "0]", "");
        }
    }
    
    // Unmapped fields
    sb.al("<tr des='un maped' style='display:none'><td>");
    MStr sbButtons = new MStr();
    
    for (String key : itemMap.keySet()) {
        String html = itemMap.get(key).get(0);
        String tag = itemMap.get(key).get(4);
        if (tag.equalsIgnoreCase("button") || tag.equalsIgnoreCase("submit")) {
            sbButtons.al(html);
        } else {
            sb.al(html);
        }
    }
    sb.al("</td></tr>");
    
    // Button row
    if (sbButtons.length() > 0) {
        sb.al("<tr des='buttons'><td align='right' colspan='" + cols * 2 
            + "' class='EWA_TD_B'>");
        sb.al(sbButtons);
        sb.al("</td></tr>");
    }
    
    return sb.toString();
}

// FrameFrame.isRedraw2Col() - Check if it is a cross 2-column object
private boolean isRedraw2Col(String tag) {
    return tag.equalsIgnoreCase("button") 
        || tag.equalsIgnoreCase("submit") 
        || tag.toUpperCase().startsWith("DHTML")
        || tag.equalsIgnoreCase("h5upload") 
        || tag.equalsIgnoreCase("textarea") 
        || tag.equals("markDown")
        || tag.equalsIgnoreCase("xmleditor") 
        || tag.equalsIgnoreCase("jseditor")
        || tag.equalsIgnoreCase("csseditor")
        || tag.equalsIgnoreCase("sqleditor") 
        || tag.equalsIgnoreCase("user")
        || tag.equalsIgnoreCase("ewaconfigitem") 
        || tag.equalsIgnoreCase("signature");
}
```

---

## 4. Generate Frame Initialization Script

```java
// FrameFrame.createJsFramePage()
public void createJsFramePage() throws Exception {
    RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
    String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
    String url = super.getUrlJs();
    
    MStr sJs = new MStr();
    String lang = super.getHtmlClass().getSysParas().getLang();
    
    sJs.al("EWA.LANG='" + lang.toLowerCase() + "';");
    sJs.al("(function() {");
    sJs.al(" var o1 = EWA.F.FOS['" + gunid + "'] = new EWA_FrameClass();");
    sJs.al(" o1._Id = o1.Id = '" + gunid + "';");
    sJs.al(" o1.Title = \"" + super.getHtmlClass().getSysParas().getTitle() + "\";");
    sJs.al(" o1.Init(EWA_ITEMS_XML_" + gunid + ");");
    
    // MTYPE
    String mtype = rv.getString(FrameParameters.EWA_MTYPE);
    if (mtype != null) {
        sJs.al(" o1.MType = '" + mtype + "';");
    }
    
    // FrameCols
    String frameCols = rv.getString(FrameParameters.EWA_FRAME_COLS);
    if (frameCols != null) {
        sJs.al(" o1.FrameCols = '" + frameCols + "';");
    }
    
    // Workflow buttons
    String wfButJson = this.getWorkFlowButJson();
    if (wfButJson != null) {
        sJs.al(" o1.WorkFlowBut = " + wfButJson + ";");
    }
    
    sJs.al("})();");
    
    super.getHtmlClass().getDocument().addJs("FRAME", sJs.toString(), false);
}
```

---

## 5. Key Parameters

### 5.1 URL Parameters

| Parameter | Description | Example |
|------|------|------|
| `EWA_MTYPE` | Processing mode | `N`=New, `M`=Modify, `C`=Copy |
| `EWA_FRAME_COLS` | Column count | `C1`=1 column, `C2`=2 columns, `C11`=vertical |
| `EWA_WIDTH` | Form width | `EWA_WIDTH=800px` |
| `EWA_HEIGHT` | Form height | `EWA_HEIGHT=600px` |
| `EWA_IS_HIDDEN_CAPTION` | Hide title | `EWA_IS_HIDDEN_CAPTION=yes` |
| `EWA_TEMP_NO` | Do not use template | `EWA_TEMP_NO=1` |
| `EWA_REDRAW` | Redraw mode | `EWA_REDRAW=1` |
| `EWA_IN_DIALOG` | Dialog mode | `EWA_IN_DIALOG=1` |
| `EWA_HIDDEN_FIELDS` | Hidden fields | `EWA_HIDDEN_FIELDS=password` |
| `EWA_VUE` | Vue mode | `EWA_VUE=1` |

### 5.2 XML Configuration

```xml
<FrameHtml>
    <Set FrameCols="C2"/>  <!-- C1=1 column, C2=2 columns, C11=vertical layout -->
</FrameHtml>
<Size>
    <Set Width="700" Height="500"/>
</Size>
<GroupSet>
    <Set Lang="zhcn" GroupInfo="Basic Info,Extended Info" GroupShow="LST"/>
</GroupSet>
```

---

## 6. Upload Processing

### 6.1 Upload Configuration

```xml
<XItem Name="AVATAR">
    <Tag><Set Tag="h5upload"/></Tag>
    <DataItem><Set DataField="avatar"/></DataItem>
    <Upload>
        <Set UpPath="/avatars/" UpSaveMethod="url" 
             UpJsonEncyrpt="yes" UpDelete="yes"/>
    </Upload>
</XItem>
```

### 6.2 Generated Parameters

```java
// Parameters generated after upload
uploadName          // File URL or binary
uploadName_JSON     // Upload file JSON info
uploadName_MD5      // File MD5
uploadName_NAME     // File name
uploadName_PATH     // Full path
uploadName_PATH_SHORT  // Relative path
uploadName_SIZE     // File size
uploadName_LENGTH   // File size (same as SIZE)
uploadName_EXT      // File extension
```

---

## 7. MTYPE Processing Modes

### 7.1 New Mode (N)

```xml
<Action>
    <ActionSet>
        <Set Type="OnPagePost">
            <CallSet>
                <Set CallName="insertUser" CallType="SqlSet" 
                     Test="'@EWA_MTYPE'='N'"/>
            </CallSet>
        </Set>
    </ActionSet>
    <SqlSet>
        <Set Name="insertUser" SqlType="update">
            <Sql><![CDATA[
                INSERT INTO users (name, email) 
                VALUES (@name, @email)
            ]]></Sql>
        </Set>
    </SqlSet>
</Action>
```

### 7.2 Modify Mode (M)

```xml
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
                <Set CallName="updateUser" CallType="SqlSet" 
                     Test="'@EWA_MTYPE'='M'"/>
            </CallSet>
        </Set>
    </ActionSet>
    <SqlSet>
        <Set Name="loadUser" SqlType="query">
            <Sql><![CDATA[
                SELECT * FROM users WHERE user_id=@user_id
            ]]></Sql>
        </Set>
        <Set Name="updateUser" SqlType="update">
            <Sql><![CDATA[
                UPDATE users 
                SET name=@name, email=@email 
                WHERE user_id=@user_id
            ]]></Sql>
        </Set>
    </SqlSet>
</Action>
```

### 7.3 Copy Mode (C)

```xml
<Action>
    <ActionSet>
        <Set Type="OnPageLoad">
            <CallSet>
                <Set CallName="loadUser" CallType="SqlSet" 
                     Test="'@EWA_MTYPE'='C'"/>
            </CallSet>
        </Set>
        <Set Type="OnPagePost">
            <CallSet>
                <Set CallName="insertUser" CallType="SqlSet" 
                     Test="'@EWA_MTYPE'='C'"/>
            </CallSet>
        </Set>
    </ActionSet>
</Action>
```

---

## 8. Group Display

### 8.1 Configuration

```xml
<GroupSet>
    <Set Lang="zhcn" GroupInfo="Basic Info,Contact Info" GroupShow="LST"/>
    <Set Lang="enus" GroupInfo="Basic Info,Contact Info" GroupShow="LST"/>
</GroupSet>
```

### 8.2 Rendering

```java
// FrameFrame.initGroupInfo()
private void initGroupInfo() {
    UserConfig uc = this.getHtmlClass().getUserConfig();
    String lang = this.getHtmlClass().getSysParas().getLang();
    
    if (!uc.getUserPageItem().testName("GroupSet")) {
        this._IsGroup = false;
        return;
    }
    
    try {
        UserXItemValues u = uc.getUserPageItem().getItem("GroupSet");
        for (int i = 0; i < u.count(); i++) {
            UserXItemValue u0 = u.getItem(i);
            String lang1 = u0.getItem("Lang");
            String groupInfo = u0.getItem("GroupInfo");
            
            if (lang1.equalsIgnoreCase(lang) && groupInfo.trim().length() > 0) {
                String[] arrayGroup = groupInfo.split(",");
                this._IsGroup = true;
                this._GroupInfos = arrayGroup;
                this._GroupShow = u0.getItem("GroupShow");
                if (this._GroupShow == null)
                    this._GroupShow = "LST";
                this._GroupShow = this._GroupShow.trim().toUpperCase();
                return;
            }
        }
        this._IsGroup = false;
    } catch (Exception e) {
        this._IsGroup = false;
    }
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
// Get Frame object
EWA.F.FOS["frame_id"]

// Refresh Frame
EWA.F.FOS["frame_id"].Reload()

// Get MTYPE
EWA.F.FOS["frame_id"].MType
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
<EasyWebTemplate Name="users.F.NM">
    <Page>
        <FrameTag><Set FrameTag="Frame"/></FrameTag>
        <Name><Set Name="users.F.NM"/></Name>
        <SkinName><Set SkinName="Test1"/></SkinName>
        <Size><Set Width="700" Height="500"/></Size>
        <FrameHtml>
            <Set FrameCols="C2"/>
        </FrameHtml>
        <GroupSet>
            <Set Lang="zhcn" GroupInfo="Basic Info,Contact Info" GroupShow="LST"/>
        </GroupSet>
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
                    INSERT INTO users (name, email) 
                    VALUES (@name, @email)
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
        <XItem Name="AVATAR">
            <Tag><Set Tag="h5upload"/></Tag>
            <DataItem><Set DataField="avatar"/></DataItem>
            <Upload>
                <Set UpPath="/avatars/" UpSaveMethod="url"/>
            </Upload>
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

### 10.2 URL Calls

```
# New user
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N

# Modify user
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=M&user_id=123

# Copy user
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=C&user_id=123

# 2-column display
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_FRAME_COLS=C2

# Redraw mode
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_REDRAW=1
```

---

## Summary

Key points of Frame execution flow:

1. **Action Execution** → `ActionFrame.execute()`
   - Initialize upload parameters
   - Generate upload file parameters
   - Execute SqlSet

2. **HTML Rendering** → `FrameFrame.createHtml()`
   - createSkinTop()
   - createCss()
   - createJsTop()
   - createContent() → createFrameContent()
     - Check redraw mode
     - Check user custom HTML
     - createItemHtmls() - Generate form items
   - createJsFramePage()
   - createSkinBottom()
   - createJsBottom()

3. **MTYPE Modes**
   - N = New
   - M = Modify
   - C = Copy

4. **Special Features**
   - Upload file processing
   - Session/Cookie operations
   - Group display
   - Redraw mode
