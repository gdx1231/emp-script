# Frame 执行流程详解

## 概述

Frame 是 EWA 框架中用于表单输入/修改的核心组件，支持新增 (N)、修改 (M)、复制 (C) 三种模式。

**核心类**:
- `FrameFrame` - Frame 渲染类
- `ActionFrame` - Frame 动作执行类
- `FrameBase` - Frame 基类

---

## 1. 完整执行流程

```
HTTP 请求 (/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N)
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
   │     └─► 读取 FrameTag = "Frame"
   │
   ├─► 2. 创建 FrameFrame 实例
   │     this._Frame = new FrameFrame()
   │     this._Action = new ActionFrame()
   │
   ├─► 3. FrameFrame.init(HtmlCreator)
   │
   ├─► 4. ActionFrame.execute()
   │     ├─► 初始化上传参数 (initUploadParas)
   │     ├─► 生成上传文件参数 (createUploadsParas)
   │     └─► 执行 SqlSet (OnPageLoad/OnPagePost)
   │
   └─► 5. FrameFrame.createHtml()
         ├─► createHtmlTraditional()  // 传统模式
         │   ├─► createSkinTop()      - 皮肤头部
         │   ├─► createCss()          - CSS 样式
         │   ├─► createJsTop()        - 头部 JS
         │   ├─► createContent()      - 主体内容 (表单)
         │   │   ├─► createFrameContent()
         │   │   │   ├─► 检查重绘模式 (EWA_REDRAW)
         │   │   │   ├─► 检查用户自定义 HTML
         │   │   │   └─► createItemHtmls() - 生成表单项
         │   │   ├─► createFrameHeader()   - 表单头部
         │   │   └─► createFrameFooter()   - 表单底部
         │   ├─► createJsFramePage()  - Frame 初始化脚本
         │   ├─► createSkinBottom()   - 皮肤底部
         │   └─► createJsBottom()     - 底部 JS
         │
         └─► createHtmlVue()  // Vue 模式 (EWA_VUE=1)
```

---

## 2. ActionFrame 执行详解

### 2.1 执行入口

```java
// ActionFrame.executeCallSql()
public void executeCallSql(String name) throws Exception {
    // 1. 初始化上传参数
    if (this.initUploadParas()) {
        this.createUploadsParas();  // 生成上传文件参数
    }
    
    // 2. 执行 SQL
    super.executeCallSql(name);
}

// ActionFrame.initUploadParas() - 初始化上传参数
private boolean initUploadParas() {
    if (super.getHtmlClass() == null) {
        return false;
    }
    
    UserXItems items = super.getHtmlClass().getUserConfig().getUserXItems();
    _Uploads = new MTable();
    
    // 遍历 XItems，查找上传相关字段
    for (int i = 0; i < items.count(); i++) {
        UserXItem item = items.getItem(i);
        String tag = item.getSingleValue("tag");
        
        // 检查是否为上传类型
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

// ActionFrame.createUploadsParas() - 生成上传文件参数
void createUploadsParas() throws Exception {
    RequestValue rv = super.getRequestValue();
    
    // 防止重复执行
    if (rv.getString("____createUploadPara____") != null) {
        return;
    }
    
    for (int i = 0; i < this._Uploads.getCount(); i++) {
        UserXItem item = (UserXItem) this._Uploads.getByIndex(i);
        this.createUploadPara(item);
    }
}

// ActionFrame.createUploadPara() - 生成单个上传文件参数
void createUploadPara(UserXItem item) throws Exception {
    RequestValue rv = super.getRequestValue();
    String uploadName = item.getName();
    
    // 获取上传配置
    UserXItemValue u = item.getItem("Upload").getItem(0);
    String p1 = u.getItem("UpPath");
    String p = super.getItemValues().replaceParameters(p1, false);
    
    // 检查 JSON 是否加密
    boolean upJsonEncyrpt = true;
    if (u.testName("UpJsonEncyrpt")) {
        String val = u.getItem("UpJsonEncyrpt");
        if ("no".equalsIgnoreCase(val)) {
            upJsonEncyrpt = false;
        }
    }
    
    // 获取上传文件名
    String upName = rv.getString("UP_NAME");
    JSONObject item1 = null;
    JSONArray arr = null;
    
    // 数据类型
    String dataType = item.getSingleValue("DataItem", "DataType");
    String tag = item.getSingleValue("Tag");
    
    if (upName == null) {
        String json = rv.getString(uploadName);
        if (json == null || json.trim().length() == 0) {
            removeUpload(item);
            return;
        }
        
        if (json.trim().startsWith("[")) {
            // JSON 数组（可能多个文件）
            JSONArray arrEncrypted = new JSONArray(json);
            if (arrEncrypted.length() == 0) {
                removeUpload(item);
                return;
            }
            
            if (upJsonEncyrpt) {
                // AES 解密
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
            
            // 取第一个文件信息
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
    
    // 检查路径合法性
    if (upName.indexOf("./") >= 0 || upName.indexOf(".\\") >= 0) {
        LOGGER.warn("InValid upName:" + upName);
        removeUpload(item);
        return;
    }
    
    // 构建文件路径
    String p_short = p.replace("\\", "/").replace("//", "/");
    String shortPath = p_short + (p_short.endsWith("/") ? "" : "/") + upName;
    String fileName = UPath.getPATH_UPLOAD() + File.separator + shortPath;
    
    File f = new File(fileName);
    
    // 添加标识（防止重复执行）
    rv.addValue("____createUploadPara____", "ADDED");
    
    // 检查文件是否存在
    if (!f.exists()) {
        if (arr != null) {
            rv.addValue(uploadName + "_JSON", arr.toString());
        }
        
        // 检查是否配置了删除文件
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
    
    // 处理文件数据
    if (dataType != null && dataType.equalsIgnoreCase("binary")) {
        long m10 = 1024 * 1024 * 10;  // 10M
        if (f.length() <= m10) {
            // 读取文件二进制（只读 10M 以下）
            byte[] buf = UFile.readFileBytes(f.getAbsolutePath());
            if (rv.getPageValues().getValue(uploadName) == null) {
                rv.addValue(uploadName, buf, "binary", buf.length);
            } else {
                rv.changeValue(uploadName, buf, "binary", buf.length);
            }
        }
    } else {
        // 字符串类型（存储 URL 地址）
        if (item1 != null) {
            String file_url = item1.getString("UP_URL");
            if (rv.getPageValues().getValue(uploadName) == null) {
                rv.addValue(uploadName, file_url);
            } else {
                rv.changeValue(uploadName, file_url, "string", file_url.length());
            }
        }
    }
    
    // 添加附加参数
    if (arr != null) {
        rv.addValue(uploadName + "_JSON", arr.toString());
    }
    
    // 文件 MD5
    String md5 = UFile.md5(f);
    rv.addValue(uploadName + "_MD5", md5);
    
    // 文件名称
    rv.addValue(uploadName + "_NAME", f.getName());
    
    // 文件完整路径
    rv.addValue(uploadName + "_PATH", f.getAbsolutePath());
    
    // 文件相对路径
    rv.addValue(uploadName + "_PATH_SHORT", shortPath);
    
    // 文件大小
    rv.addValue(uploadName + "_SIZE", f.length());
    rv.addValue(uploadName + "_LENGTH", f.length());
    
    // 文件扩展名
    rv.addValue(uploadName + "_EXT", UFile.getFileExt(f.getName()));
}
```

### 2.2 执行 SQL

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
        
        // 执行 SQL
        this.executeSql(sql, sqlType, name);
        
        // 检查错误
        if (StringUtils.isNotBlank(cnn.getErrorMsg())) {
            throw new Exception(cnn.getErrorMsg());
        }
        
        // 检查用户定义错误
        if (StringUtils.isNotBlank(this.getChkErrorMsg())) {
            return;
        }
    }
    
    // 执行 Session/Cookie 操作
    this.executeSessionsCookies(sqlItem.getUserXItemValue());
}

// ActionBase.executeSql() - 执行单条 SQL
public boolean executeSql(String sql, String sqlType, String name) {
    try {
        sql = this.createSql(sql);  // 允许子类修改 SQL
        
        if (sqlType.equalsIgnoreCase("procedure")) {
            // 存储过程
            this.executeSqlProcdure(sql);
        } else if (sqlType.equalsIgnoreCase("query")) {
            // 查询
            DTTable dt = this.executeSqlQuery(sql);
            dt.setName(name);
            if (dt.isOk()) {
                super.getDTTables().add(dt);
            }
        } else if (sqlType.equalsIgnoreCase("update")) {
            // 更新
            this.executeSqlUpdate(sql);
        }
        
        return true;
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        return false;
    }
}

// ActionBase.executeSqlQuery() - 执行查询
public DTTable executeSqlQuery(String sql) {
    DataConnection conn = this.getItemValues().getSysParas().getDataConn();
    conn.executeQuery(sql);
    return DTTable.returnTable(conn);
}

// ActionBase.executeSqlUpdate() - 执行更新
public void executeSqlUpdate(String sql) {
    DataConnection conn = this.getItemValues().getSysParas().getDataConn();
    conn.executeUpdate(sql);
}
```

### 2.3 执行 Session/Cookie 操作

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

// ActionBase.executeSession() - 执行 Session 操作
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
        // 空值删除 Session
        rv.getSession().removeAttribute(name);
    } else if (option.equalsIgnoreCase("C")) {
        // 创建 Session
        rv.getSession().setAttribute(name, val);
        this._OutSessions.put(name, val);  // 记录输出的 Session
    } else {
        rv.getSession().removeAttribute(name);
    }
}

// ActionBase.executeCookie() - 执行 Cookie 操作
private void executeCookie(UserXItemValue uxv) throws Exception {
    // 获取加密配置
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
        // 删除 Cookie
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        if (domain != null && domain.trim().length() > 0) {
            cookie.setPath(domain);
        }
        rv.getResponse().addCookie(cookie);
    } else if (option.equalsIgnoreCase("C")) {
        // 创建 Cookie（加密）
        String encrypted = security.encryptoString(val);
        Cookie cookie = new Cookie(name, encrypted);
        if (life != null && life.trim().length() > 0) {
            cookie.setMaxAge(Integer.parseInt(life));
        }
        if (domain != null && domain.trim().length() > 0) {
            cookie.setPath(domain);
        }
        rv.getResponse().addCookie(cookie);
        this._OutCookies.put(name, val);  // 记录输出的 Cookie
    } else {
        // 删除 Cookie
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        rv.getResponse().addCookie(cookie);
    }
}
```

---

## 3. FrameFrame 渲染详解

### 3.1 createHtml 流程

```java
// FrameFrame.createHtml()
public void createHtml() throws Exception {
    if (super.getHtmlClass().getSysParas().isVue()) {
        super.createHtmlVue();  // Vue 模式
    } else {
        this.createHtmlTraditional();  // 传统模式
    }
}

// FrameFrame.createHtmlTraditional()
public void createHtmlTraditional() throws Exception {
    // 1. 皮肤头部
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
    
    // 3. 头部 JS
    super.createJsTop();
    super.addDebug(this, "HTML", "createJsTop");
    
    // 4. 主体内容
    try {
        this.createContent();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createContent");
    
    // 5. Frame 脚本
    try {
        this.createJsFramePage();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createJsFramePage");
    
    // 6. 皮肤底部
    try {
        super.createSkinBottom();
    } catch (Exception e) {
        LOGGER.error(e.getMessage());
        throw e;
    }
    super.addDebug(this, "HTML", "createSkinBottom");
    
    // 7. 底部 JS
    super.createJsBottom();
    super.addDebug(this, "HTML", "createJsBottom");
}
```

### 3.2 创建表单内容

```java
// FrameFrame.createContent()
public void createContent() throws Exception {
    RequestValue rv = super.getHtmlClass().getItemValues().getRequestValue();
    HtmlDocument doc = this.getHtmlClass().getDocument();
    
    // 获取用户自定义 HTML
    String userHtml = this.getUserHtml();
    
    // 检查重绘模式
    String ewa_redraw = rv.s(FrameParameters.EWA_REDRAW);
    if (ewa_redraw != null) {
        userHtml = "";  // 重绘模式忽略用户自定义 HTML
    }
    
    // 用户自定义头部 HTML
    String pageAddTop = this.getPageItemValue("AddHtml", "Top");
    if (pageAddTop != null) {
        doc.addScriptHtml(pageAddTop);
        doc.addFrameHtml(pageAddTop);
    }
    
    if (userHtml.trim().length() == 0) {
        // 使用皮肤定义的头部
        MStr sb = new MStr();
        sb.append("<!--皮肤定义的头部-->");
        
        String skinTop = super.createSkinFCTop();
        
        // 根据 MTYPE 添加 CSS 类
        String mtypeCss;
        if ("M".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
            mtypeCss = " ewa-mtype-m";  // 修改模式
        } else if ("N".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
            mtypeCss = " ewa-mtype-n";  // 新建模式
        } else if ("C".equalsIgnoreCase(rv.s(FrameParameters.EWA_MTYPE))) {
            mtypeCss = " ewa-mtype-c";  // 复制模式
        } else {
            mtypeCss = "";
        }
        skinTop = skinTop.replace("{EWA_MTYPE}", mtypeCss);
        
        sb.append(skinTop);
        
        // Frame 定义的页头
        sb.append("<!--Frame 定义的页头-->");
        sb.append(createFrameHeader());
        
        doc.addScriptHtml(sb.toString());
        doc.addFrameHtml(sb.toString());
    }
    
    // Frame 内容
    createFrameContent();
    
    if (userHtml.trim().length() == 0) {
        // Frame 定义的页脚
        this.createFrameFooter();
        
        // 皮肤定义的尾部
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
    
    // 检查重绘模式
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
    
    // 获取用户自定义 HTML
    String userHtml = this.getUserHtml();
    
    if (userHtml.trim().length() > 0 && !_IsRedrawJson 
        && rv.s(FrameParameters.EWA_TEMP_NO) == null) {
        // 用户自定义框架
        sb.append(this.createFrameContentUserHtml());
    } else {
        // 标准框架
        sb.append(this.createItemHtmls());
    }
    
    this.getHtmlClass().getDocument().addScriptHtml(sb.toString(), "FRAME CONTENT");
    this.getHtmlClass().getDocument().addFrameHtml(sb.toString());
}

// FrameFrame.createItemHtmls() - 生成表单项
public String createItemHtmls() throws Exception {
    MStr sb = new MStr();
    UserConfig uc = this.getHtmlClass().getUserConfig();
    String lang = this.getHtmlClass().getSysParas().getLang();
    
    // 检查是否分组
    this.initGroupInfo();
    
    if (this._IsGroup) {
        // 分组显示
        for (int i = 0; i < this._GroupInfos.length; i++) {
            String grp = this._GroupInfos[i];
            sb.append("<fieldset><legend>" + grp + "</legend>");
            this.renderGroup(grp, sb);
            sb.append("</fieldset>");
        }
    } else {
        // 无分组显示
        this.renderAll(sb);
    }
    
    return sb.toString();
}

// FrameFrame.renderAll() - 渲染所有字段
private void renderAll(MStr sb) throws Exception {
    UserConfig uc = this.getHtmlClass().getUserConfig();
    String lang = this.getHtmlClass().getSysParas().getLang();
    
    // 获取数据
    DTTable tb = null;
    MList tbs = super.getHtmlClass().getAction().getDTTables();
    if (tbs != null && tbs.size() > 0) {
        tb = (DTTable) tbs.get(tbs.size() - 1);
    }
    
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
        
        // 获取字段值
        String value = "";
        if (tb != null && tb.getCount() > 0) {
            String dataField = uxi.getItem("DataItem").getItem(0).getItem("DataField");
            value = tb.getCell(0, dataField).toString();
        }
        
        // 渲染 Item
        String html = item.render();
        
        // 添加到表单
        sb.append("<div class='EWA_TR'>");
        sb.append("<td class='EWA_TD_M'>" + item.getDescription() + "</td>");
        sb.append("<td class='EWA_TD_M'>" + html + "</td>");
        sb.append("</div>");
    }
}

// FrameFrame.createFrameHeader() - 创建表单头部
public String createFrameHeader() throws Exception {
    MStr sb = new MStr();
    
    // 检查是否隐藏标题
    if (super.isHiddenCaption()) {
        return "";
    }
    
    // 获取皮肤定义的头部
    String header = super.createSkinFCHeader();
    
    sb.append("<TR EWA_TAG='HEADER'>");
    String pageDescription = Utils.textToInputValue(
        super.getHtmlClass().getSysParas().getTitle());
    sb.append(header.replace(SkinFrame.TAG_ITEM, pageDescription));
    sb.append("</tr>");
    
    // 获取 ColSpan
    int colSpan = this.getFrameColSize();
    return sb.toString().replace("3", colSpan + "");
}

// FrameFrame.getFrameColSize() - 获取表单分栏数
private int getFrameColSize() {
    RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();
    
    // 参数优先
    String frameCols = rv.s(FrameParameters.EWA_FRAME_COLS);
    if (frameCols != null && frameCols.trim().length() > 0) {
        return parseFrameCols(frameCols);
    }
    
    // 配置默认
    String defaultCols = getPageItemValue("FrameHtml", "FrameCols");
    return parseFrameCols(defaultCols);
}

private int parseFrameCols(String frameCols) {
    if (frameCols == null) return 3;
    
    frameCols = frameCols.trim().toUpperCase();
    if (frameCols.equals("C2") || frameCols.equals("2")) {
        return 2;  // 2 段（无备注框）
    } else if (frameCols.equals("C1") || frameCols.equals("1")) {
        return 1;  // 1 段（无标题框）
    } else if (frameCols.equals("C11")) {
        return 1;  // 上下排列
    }
    return 3;  // 默认 3 段
}
```

### 3.3 重绘模式 (Redraw)

```java
// FrameFrame.createRedrawJsonItemHtmls() - 创建重绘 JSON 表单项
private String createRedrawJsonItemHtmls(JSONObject redrawJson) throws Exception {
    MStr sb = new MStr();
    UserConfig uc = this.getHtmlClass().getUserConfig();
    String lang = this.getHtmlClass().getSysParas().getLang();
    
    // 构建 Item 映射
    HashMap<String, ArrayList<String>> itemMap = new LinkedHashMap<String, ArrayList<String>>();
    for (int i = 0; i < uc.getUserXItems().count(); i++) {
        UserXItem uxi = uc.getUserXItems().getItem(i);
        
        String des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);
        String memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);
        
        IItem item = super.getHtmlClass().getItem(uxi);
        String itemHtml = item.createItemHtml();
        
        // 替换描述
        if (itemHtml.indexOf(SkinFrame.TAG_DES) >= 0) {
            itemHtml = itemHtml.replace(SkinFrame.TAG_DES, des);
        }
        if (itemHtml.indexOf(SkinFrame.TAG_MSG) >= 0) {
            itemHtml = itemHtml.replace(SkinFrame.TAG_MSG, memo);
        }
        
        // 特殊处理
        String tag = uxi.getSingleValue("Tag");
        if (tag.equalsIgnoreCase("textarea")) {
            itemHtml = itemHtml.replaceFirst(">", 
                " placeholder=\"" + Matcher.quoteReplacement(des) + "\">");
        }
        
        // 保存到映射
        ArrayList<String> al = new ArrayList<String>();
        al.add(itemHtml);  // HTML
        al.add(des);       // 描述
        al.add(memo);      // 备注
        al.add(uxi.getSingleValue("IsMustInput"));  // 是否必须
        al.add(tag);       // 类型
        itemMap.put(uxi.getName(), al);
    }
    
    // 获取重绘配置
    int rows = redrawJson.getInt("rows");
    int cols = redrawJson.getInt("cols");
    
    // 创建消息框
    sb.append("<tr class='ewa-row-msg-box'>");
    sb.append("<td class='ewa_msg_box' colspan='" + cols * 2 + "'>");
    sb.append("<div><span class='ewa_msg_tip'>Tip</span>");
    sb.append("<span class='ewa_msg_err'></span></div></td></tr>");
    
    // 创建网格
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
    
    // 映射配置
    JSONArray map = redrawJson.getJSONArray("map");
    for (int i = 0; i < map.length(); i++) {
        JSONObject o = map.getJSONObject(i);
        String id = o.getString("id");
        String col = o.getString("col");
        String row = o.getString("row");
        
        String html = id;
        String des = "找不到";
        String tag = "";
        
        if (itemMap.containsKey(id)) {
            html = itemMap.get(id).get(0);
            des = "<div class='ewa_d0'><div class='ewa_d1'>" + itemMap.get(id).get(1) + "</div>";
            
            // 必须输入标记
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
            // 跨 2 列对象（按钮等）
            sb.replace("[!@#!@#--342''" + tmp + "0]", html);
        }
    }
    
    // 清除无用标记
    for (int i = 0; i < rows; i++) {
        for (int m = 0; m < cols; m++) {
            String tmp = i + "`" + m;
            sb.replace("[DSP" + tmp + "0]", "style='display:none'");
            sb.replace("[DSP" + tmp + "1]", "style='display:none'");
            sb.replace("[!@#!@#--342、、" + tmp + "1]", "");
            sb.replace("[!@#!@#--342''" + tmp + "0]", "");
        }
    }
    
    // 未映射的字段
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
    
    // 按钮行
    if (sbButtons.length() > 0) {
        sb.al("<tr des='buttons'><td align='right' colspan='" + cols * 2 
            + "' class='EWA_TD_B'>");
        sb.al(sbButtons);
        sb.al("</td></tr>");
    }
    
    return sb.toString();
}

// FrameFrame.isRedraw2Col() - 检查是否为跨 2 列对象
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

## 4. 生成 Frame 初始化脚本

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
    
    // 工作流按钮
    String wfButJson = this.getWorkFlowButJson();
    if (wfButJson != null) {
        sJs.al(" o1.WorkFlowBut = " + wfButJson + ";");
    }
    
    sJs.al("})();");
    
    super.getHtmlClass().getDocument().addJs("FRAME", sJs.toString(), false);
}
```

---

## 5. 关键参数

### 5.1 URL 参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `EWA_MTYPE` | 处理模式 | `N`=新增，`M`=修改，`C`=复制 |
| `EWA_FRAME_COLS` | 分栏数 | `C1`=1 段，`C2`=2 段，`C11`=上下 |
| `EWA_WIDTH` | 表单宽度 | `EWA_WIDTH=800px` |
| `EWA_HEIGHT` | 表单高度 | `EWA_HEIGHT=600px` |
| `EWA_IS_HIDDEN_CAPTION` | 隐藏标题 | `EWA_IS_HIDDEN_CAPTION=yes` |
| `EWA_TEMP_NO` | 不使用模板 | `EWA_TEMP_NO=1` |
| `EWA_REDRAW` | 重绘模式 | `EWA_REDRAW=1` |
| `EWA_IN_DIALOG` | Dialog 模式 | `EWA_IN_DIALOG=1` |
| `EWA_HIDDEN_FIELDS` | 隐藏字段 | `EWA_HIDDEN_FIELDS=password` |
| `EWA_VUE` | Vue 模式 | `EWA_VUE=1` |

### 5.2 XML 配置

```xml
<FrameHtml>
    <Set FrameCols="C2"/>  <!-- C1=1 段，C2=2 段，C11=上下排列 -->
</FrameHtml>
<Size>
    <Set Width="700" Height="500"/>
</Size>
<GroupSet>
    <Set Lang="zhcn" GroupInfo="基本信息，扩展信息" GroupShow="LST"/>
</GroupSet>
```

---

## 6. 上传处理

### 6.1 上传配置

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

### 6.2 生成的参数

```java
// 上传后生成的参数
uploadName          // 文件 URL 或二进制
uploadName_JSON     // 上传文件 JSON 信息
uploadName_MD5      // 文件 MD5
uploadName_NAME     // 文件名称
uploadName_PATH     // 完整路径
uploadName_PATH_SHORT  // 相对路径
uploadName_SIZE     // 文件大小
uploadName_LENGTH   // 文件大小（同 SIZE）
uploadName_EXT      // 文件扩展名
```

---

## 7. MTYPE 处理模式

### 7.1 新增模式 (N)

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

### 7.2 修改模式 (M)

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

### 7.3 复制模式 (C)

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

## 8. 分组显示

### 8.1 配置

```xml
<GroupSet>
    <Set Lang="zhcn" GroupInfo="基本信息，联系信息" GroupShow="LST"/>
    <Set Lang="enus" GroupInfo="Basic Info,Contact Info" GroupShow="LST"/>
</GroupSet>
```

### 8.2 渲染

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

## 9. 调试技巧

### 9.1 启用 Debug

```
EWA_DEBUG_NO=1      # 不显示 Debug
EWA_DEBUG_KEY=xxx   # Debug 密钥
EWA_JS_DEBUG=1      # JS 调试模式
```

### 9.2 浏览器控制台

```javascript
// 获取 Frame 对象
EWA.F.FOS["frame_id"]

// 刷新 Frame
EWA.F.FOS["frame_id"].Reload()

// 获取 MTYPE
EWA.F.FOS["frame_id"].MType
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
            <Set Lang="zhcn" GroupInfo="基本信息，联系信息" GroupShow="LST"/>
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

### 10.2 URL 调用

```
# 新增用户
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=N

# 修改用户
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=M&user_id=123

# 复制用户
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_MTYPE=C&user_id=123

# 2 段显示
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_FRAME_COLS=C2

# 重绘模式
/ewa?XMLNAME=users&ITEMNAME=F.NM&EWA_REDRAW=1
```

---

## 总结

Frame 执行流程关键点：

1. **Action 执行** → `ActionFrame.execute()`
   - 初始化上传参数
   - 生成上传文件参数
   - 执行 SqlSet

2. **HTML 渲染** → `FrameFrame.createHtml()`
   - createSkinTop()
   - createCss()
   - createJsTop()
   - createContent() → createFrameContent()
     - 检查重绘模式
     - 检查用户自定义 HTML
     - createItemHtmls() - 生成表单项
   - createJsFramePage()
   - createSkinBottom()
   - createJsBottom()

3. **MTYPE 模式**
   - N = 新增
   - M = 修改
   - C = 复制

4. **特殊功能**
   - 上传文件处理
   - Session/Cookie操作
   - 分组显示
   - 重绘模式
