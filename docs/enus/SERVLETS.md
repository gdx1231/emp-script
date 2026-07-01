# EWA Servlet Reference

## Servlet Overview

The EWA framework has **14 Servlets** in total, divided into two categories:

### Core Servlets (script.servlets package)
| Servlet | URL Mapping | Purpose |
|---------|-------------|---------|
| **ServletMain** | `/ewa`, `/EWA_STYLE/cgi-bin/index.jsp` | Main entry point, handles page requests |
| **ServletUpload** | `/EWA_STYLE/cgi-bin/_up_/index.jsp` | File upload |
| **ServletCode** | `/EWA_STYLE/cgi-bin/_co_/index.jsp` | Verification code generation |
| **ServletResources** | `/r.ewa`, `/r1.ewa`, `/EWA_STYLE/cgi-bin/_re_/index.jsp` | Static resources, JS/CSS combining |
| **ServletStatus** | `/EWA_STYLE/cgi-bin/_st_/index.jsp` | Tree state save/restore |
| **ServletError** | `/EWA_STYLE/cgi-bin/_er_/index.jsp` | Error page |
| **ServletWorkflow** | `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | Workflow processing |
| **ServletRestful** | `/ewa-api/*` | RESTful API entry point |
| **ServletCrossDomain** | `/crossdomain.xml` | Cross-domain policy file |

### Definition Management Servlets (define.servlets package)
| Servlet | URL Mapping | Purpose |
|---------|-------------|---------|
| **ServletIndex** | `/EWA_DEFINE/index.jsp` | Definition management home page |
| **ServletXml** | `/EWA_DEFINE/cgi-bin/xml/index.jsp` | XML configuration management |
| **ServletGroup** | `/EWA_DEFINE/cgi-bin/group/*` | Component import/export |
| **ServletWorkflow** | `/EWA_DEFINE/cgi-bin/wf/*` | Workflow definition |
| **ServletRemoteSync** | `/EWA_DEFINE/cgi-bin/remoteSync/*` | Remote synchronization |

---

## 1. ServletMain - Main Entry Servlet

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletMain.java`

### web.xml Configuration
```xml
<servlet>
    <servlet-name>EwaMain</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletMain</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>EwaMain</servlet-name>
    <url-pattern>/ewa</url-pattern>
    <url-pattern>/EWA_STYLE/cgi-bin/index.jsp</url-pattern>
</servlet-mapping>
```

### Lifecycle Methods

#### 1.1 Static Initialization (at class load time)
```java
static {
    initEwaInstances();  // Initialize all EWA instances
}

public synchronized static void initEwaInstances() {
    EwaConfig.instance();              // Load skin configuration
    Skin.instance();                   // Load skins
    EwaGlobals.instance();             // Load global variables
    ConfSecurities.getInstance();      // Load security configuration
    ConnectionConfigs.instance();      // Initialize database connection pools
    SqlCachedHsqldbImpl.getInstance(); // Initialize HSQL cache
    SqlCached.getInstance();           // Initialize cache
    new ValidCode1();                  // Load verification code font
}
```

#### 1.2 init() Method (Servlet initialization)
```java
public void init() throws ServletException {
    super.init();
    
    // Get init-param parameters from web.xml
    Enumeration<String> names = this.getInitParameterNames();
    while (names.hasMoreElements()) {
        String name = names.nextElement();
        String value = this.getInitParameter(name);
        
        if ("ewa_conf".equals(name)) {
            // Set the directory where ewa_conf.xml resides
            UPath.CONF_NAME = value;
        } else if ("ewa_path_real".equals(name)) {
            // Set the project WEB-INF/classes directory
            UPath.PATH_REAL = value;
        }
    }
    
    initEwaInstances();
}
```

#### 1.3 service() Method (request handling)
```java
@Override
public void service(HttpServletRequest req, HttpServletResponse resp) {
    String method = req.getMethod();
    if ("PATCH".equalsIgnoreCase(method)) {
        this.doPatch(req, resp);
    } else {
        super.service(req, resp);  // Dispatch to doGet/doPost etc.
    }
}
```

#### 1.4 show() Method (core processing logic)
```java
public void show(HttpServletRequest request, HttpServletResponse response) {
    // 1. Set encoding
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-Type", "text/html;charset=UTF-8");
    response.setHeader("X-EWA-ENGIN", "emp-script");
    
    // 2. Create EwaWebPage and execute
    HttpSession session = request.getSession();
    EwaWebPage p = new EwaWebPage(request, session, response);
    p.run();
    
    // 3. Handle errors
    if (p.isPageError()) {
        p.getDebugInfo().recordToHsql();  // Record to database
        if (!p.isPageDebug()) {
            response.sendRedirect(request.getContextPath() + EwaWebPage.ERR_PAGE);
            return;
        }
    }
    
    // 4. Handle special requests
    RequestValue rv = p.getHtmlCreator().getRequestValue();
    
    if ("download-inline".equalsIgnoreCase(rv.s("EWA_AJAX"))) {
        this.outImage(rv, response, p);  // Output image
        return;
    }
    if ("validcode".equalsIgnoreCase(rv.s("EWA_AJAX"))) {
        this.outValidCode(p);  // Output verification code
        return;
    }
    if ("download".equalsIgnoreCase(rv.s("EWA_AJAX"))) {
        this.downloadFile(rv, response, p, null);  // Download file
        return;
    }
    
    // 5. Output HTML content (with GZip support)
    String cnt = p.getPageContent();
    if (p.isPageDebug()) {
        cnt += p.getPageDeubgInfo();  // Append debug info
    }
    this.outContent(request, response, cnt);
}
```

### Supported HTTP Methods
- `doGet()` - GET requests
- `doPost()` - POST requests
- `doPut()` - PUT requests
- `doDelete()` - DELETE requests
- `doPatch()` - PATCH requests

---

## 2. ServletUpload - File Upload

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletUpload.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaUpload</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletUpload</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaUpload</servlet-name>
    <url-pattern>/EWA_STYLE/cgi-bin/_up_/index.jsp</url-pattern>
</servlet-mapping>
```

### Core Processing Logic
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    JSONObject result = new JSONObject();
    PrintWriter out = response.getWriter();
    
    // 1. Initialize RequestValue
    RequestValue rv = new RequestValue(request, request.getSession());
    
    // 2. Initialize Upload component
    Upload up = new Upload();
    up.setRv(rv);
    up.init(request);
    
    // 3. Configure Apache Commons FileUpload
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(10 * 1024 * 1024);  // 10M memory buffer
    factory.setRepository(new File(UPath.getPATH_UPLOAD() + "/upload"));
    
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(2 * 1024 * 1024 * 1024);  // 2G max upload
    
    // 4. Parse request
    List<FileItem> items = upload.parseRequest(request);
    for (FileItem item : items) {
        if (item.isFormField()) {
            // Extract form parameters into rv
            rv.addValue(item.getFieldName(), item.getString());
        }
    }
    
    // 5. Execute upload
    up.setUploadItems(items);
    String rst = up.upload();
    out.println(rst);
}
```

### Upload Configuration
| Parameter | Default | Description |
|-----------|---------|-------------|
| Memory buffer | 10MB | Exceeded content written to temp file |
| Max upload | 2GB | Maximum single file size |
| Temp directory | `UPath.getPATH_UPLOAD()/upload` | Temporary file storage location |

---

## 3. ServletResources - Static Resources

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletResources.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaResources</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletResources</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaResources</servlet-name>
    <url-pattern>/EWA_STYLE/cgi-bin/_re_/index.jsp</url-pattern>
    <url-pattern>/r.ewa</url-pattern>
    <url-pattern>/r1.ewa</url-pattern>
</servlet-mapping>
```

### Feature List

#### 3.1 Get JS Resources (default)
```java
// URL: /r.ewa or /r.ewa?method=xxx
String lang = "zhcn";  // or enus
String js = EwaGlobals.instance().createJs(lang);
response.setContentType("text/javascript");
out1.outContent(js);  // GZip compressed output
```

#### 3.2 HTML Editor Image Processing
```java
// URL: /r.ewa?method=HtmlImages
// Get image info from HTML editor
String rst = this.handleGetImages(request, response);
out1.outContent(rst);
```

#### 3.3 File to HTML Conversion
```java
// URL: /r.ewa?method=2Html
// Convert uploaded Office documents to HTML
String rst = this.handleCreateHtml(request, response);
out1.outContent(rst);
```

#### 3.4 JS/CSS Combining
```java
// URL: /r.ewa?method=combine&tag=xxx
// or URL: /r.ewa?method=combine&files=file1.js;file2.css
String rst = this.handleCombineMaster(request, response);
out1.outContent(rst);
```

**Combine configuration example** (ewa_conf.xml):
```xml
<combineResources Name="main-js" Type="js" Host="http://cdn.example.com">
    <resource Debug="/js/jquery.js" Normal="/js/jquery.min.js"/>
    <resource Debug="/js/ewa.js" Normal="/js/ewa.min.js"/>
</combineResources>
```

### Caching Mechanism
```java
// ETag caching
String IfNoneMatch = request.getHeader("If-None-Match");
String etag = "EWA_RES/" + lang + "/" + js.hashCode();

if (IfNoneMatch != null && IfNoneMatch.equals(etag)) {
    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);  // 304
    return;
}
response.addHeader("ETag", etag);
response.addHeader("Cache-Control", "public, max-age=1231");
```

---

## 4. ServletRestful - RESTful API

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletRestful.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaRestful</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletRestful</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaRestful</servlet-name>
    <url-pattern>/ewa-api/*</url-pattern>
</servlet-mapping>
```

### Core Processing Flow
```java
@Override
public void service(HttpServletRequest req, HttpServletResponse resp) {
    try {
        String resultContent = this.ewaRestfulHandler(req, resp);
        this.outContent(req, resp, resultContent);
    } catch (Exception err) {
        resp.setStatus(500);
        this.outContent(req, resp, "Inner error");
    }
}

public String ewaRestfulHandler(HttpServletRequest request, HttpServletResponse response) {
    RestfulResult<Object> result = new RestfulResult<>();
    
    // 1. Parse request path
    String uri = request.getRequestURI();
    String path = uri.substring(request.getContextPath().length());
    
    // 2. Get HTTP method
    String httpMethod = request.getMethod().toUpperCase();
    
    // 3. Parse parameters
    RequestValue rv;
    if ("GET".equalsIgnoreCase(httpMethod)) {
        rv = new RequestValue(request);
    } else if (isMultipart(request)) {
        rv = new RequestValue(request);
        isUpload = true;
    } else {
        rv = new RequestValue(request, true);  // JSON body parsing
    }
    
    // 4. Look up ConfRestful configuration
    ConfRestful conf = ConfRestfuls.getInstance()
        .getConfRestful(path, httpMethod, rv, result);
    
    if (conf == null) {
        response.setStatus(result.getHttpStatusCode());
        return result.toString();  // 404 JSON
    }
    
    // 5. Process by type
    if (isOutImage) {
        this.handleImage(conf, rv, response, result);
        return null;  // Already output directly
    } else if (isDownload) {
        this.handleDownload(conf, rv, response, result);
    } else if (isUpload) {
        this.handleUpload(conf, rv, request, result);
    } else {
        this.handleConf(conf, rv, response, result);
    }
    
    // 6. Set CORS
    String cors = ConfRestfuls.getInstance().getCors();
    response.setHeader("Access-Control-Allow-Origin", cors);
    response.setStatus(result.getHttpStatusCode());
    
    return result.toString();
}
```

### HTTP Method Mapping
| HTTP Method | EWA Parameter | Description |
|-------------|---------------|-------------|
| GET | `EWA_AJAX=JSON_EXT` | Query list |
| POST | `EWA_ACTION=OnPagePost`, `EWA_MTYPE=N` | Create |
| PUT | `EWA_ACTION=OnPagePost`, `EWA_MTYPE=M` | Modify |
| PATCH | `EWA_ACTION=OnFrameRestore` | Restore (logical delete restore) |
| DELETE | `EWA_ACTION=OnFrameDelete` | Delete |

### Response Format
```json
{
    "success": true,
    "code": 200,
    "message": "Operation successful",
    "data": {...},
    "ewaPageCur": 1,
    "ewaPageSize": 20,
    "pageCount": 10,
    "recordCount": 200,
    "start": 123,
    "end": 456
}
```

### HTTP Status Codes
| Status Code | Scenario |
|-------------|----------|
| 200 OK | Query successful, modification successful |
| 201 Created | POST creation successful |
| 204 No Content | No data |
| 400 Bad Request | SQL execution error |
| 401 Unauthorized | Permission verification failed |
| 403 Forbidden | System execution error |
| 404 Not Found | Configuration not found, data does not exist |
| 500 Internal Server Error | Internal system error |

---

## 5. ServletCode - Verification Code

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletCode.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaCode</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletCode</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaCode</servlet-name>
    <url-pattern>/EWA_STYLE/cgi-bin/_co_/index.jsp</url-pattern>
</servlet-mapping>
```

### Processing Logic
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("image/jpeg");
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    
    ServletOutputStream output = response.getOutputStream();
    
    // Read verification code settings from XML configuration
    UserConfig uc = UserConfig.instance(xmlName, itemname, null);
    int len = 4;  // Default 4 digits
    boolean isNumberCode = true;  // Numeric verification code
    
    // Generate verification code
    ValidCode1 vc = new ValidCode1(len, isNumberCode);
    ImageIO.write(vc.createCode(), "jpeg", output);
    
    // Save to Session
    session.setAttribute(ValidCode.SESSION_NAME, vc.getRandomNumber());
}
```

### Verification Code Configuration
The number of digits and type of verification code can be configured via `UserConfig` in the XML configuration.

---

## 6. ServletError - Error Page

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletError.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaError</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletError</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaError</servlet-name>
    <url-pattern>/EWA_STYLE/cgi-bin/_er_/index.jsp</url-pattern>
</servlet-mapping>
```

### Processing Logic
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    String ajax = request.getParameter("EWA_AJAX");
    
    if (ajax == null || ajax.trim().length() == 0) {
        // Output full error page HTML
        o.println("<!DOCTYPE html>");
        o.println("<html><head>...");
        o.println("<body onload=\"msg(true);\">");
    } else {
        // Output error message JS
        o.println("msg(false);");
    }
}
```

---

## 7. ServletStatus - State Save/Restore

**File**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletStatus.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaStatus</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletStatus</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaStatus</servlet-name>
    <url-pattern>/EWA_STYLE/cgi-bin/_st_/index.jsp</url-pattern>
</servlet-mapping>
```

### Functionality
Saves and restores Tree/FrameSet states to Session:

```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    RequestValue rv = new RequestValue(request, session);
    
    String url = rv.getString("U");  // URL
    String t = rv.getString("T");    // Type: TREE or FRAMESET
    int code = url.hashCode();
    String sessionName = t + code;
    
    if (rv.getString("M").equals("1")) {
        // Save state
        String val = rv.getString("V");
        session.setAttribute(sessionName, val);
    } else {
        // Get state
        statusValue = session.getAttribute(sessionName);
    }
    
    out.println("_EWA_STATUS_VALUE=\"" + statusValue + "\";");
}
```

---

## 8. ServletIndex - Definition Management Home

**File**: `src/main/java/com/gdxsoft/easyweb/define/servlets/ServletIndex.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaDefineIndex</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletIndex</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaDefineIndex</servlet-name>
    <url-pattern>/EWA_DEFINE/index.jsp</url-pattern>
</servlet-mapping>
```

### Processing Logic
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    // Check if definition management is allowed
    if (!ConfDefine.isAllowDefine()) {
        response.setStatus(404);
        return;
    }
    
    // Initialize HSQL and admin info
    EwaConfHelpHSqlServer.getInstance();
    ConfAdmins.getInstance().getLst();
    
    // Redirect to configuration management page
    String urlDefine = request.getContextPath()
        + "/EWA_STYLE/cgi-bin/?XMLNAME=/ewa/ewa.xml&ITEMNAME=index";
    response.sendRedirect(urlDefine);
}
```

---

## 9. ServletXml - XML Configuration Management

**File**: `src/main/java/com/gdxsoft/easyweb/define/servlets/ServletXml.java`

### URL Mapping
```xml
<servlet>
    <servlet-name>EwaDefineXml</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletXml</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaDefineXml</servlet-name>
    <url-pattern>/EWA_DEFINE/cgi-bin/xml/index.jsp</url-pattern>
</servlet-mapping>
```

### Feature List

| TYPE | Description |
|------|-------------|
| `format` | Format code |
| `GUNID` | Get globally unique ID |
| `saveXml` | Save XML configuration |
| `deleteXml` | Delete XML configuration |
| `getXml` | Get XML content |
| `formatSql` | Format SQL |
| `checkSql` | Check SQL syntax |
| `exportModule` | Export module |
| `importModule` | Import module |

### Save XML Example
```java
if (oType.equalsIgnoreCase("saveXml")) {
    String xmlName = rv.getString("XMLNAME");
    String itemName = rv.getString("ITEMNAME");
    String xmlData = rv.getString("XMLDATA");
    
    IUpdateXml updater = getUpdateXml(xmlName, admId);
    updater.saveXml(xmlName, itemName, xmlData);
    
    outContent(request, response, "OK");
}
```

---

## Servlet Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     web.xml Configuration                     │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌──────────────┐ ┌──────────┐ ┌──────────────┐
│  ServletMain │ │ServletUp │ │ServletResou..│
│  (Main Entry)│ │(Upload)   │ │(Resources)   │
└──────┬───────┘ └──────────┘ └──────────────┘
       │
       ▼
┌──────────────┐
│  EwaWebPage  │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ HtmlCreator  │
└──────┬───────┘
       │
       ├──────────┬──────────┬──────────┐
       ▼          ▼          ▼          ▼
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│FrameFrame│ │FrameList│ │FrameTree│ │ ...    │
└─────────┘ └─────────┘ └─────────┘ └─────────┘
```

---

## Servlet Initialization Order

```
1. Class loading (static block)
   └─► initEwaInstances()
       ├─► EwaConfig.instance()
       ├─► Skin.instance()
       ├─► EwaGlobals.instance()
       ├─► ConnectionConfigs.instance()
       └─► SqlCached.getInstance()

2. Servlet init()
   └─► Read web.xml init-param
       ├─► ewa_conf (configuration file path)
       └─► ewa_path_real (WEB-INF directory)

3. First request
   └─► service() → doGet()/doPost()
       └─► show()
           ├─► EwaWebPage.run()
           ├─► HtmlCreator.createPageHtml()
           └─► Output content
```

---

## Summary

Design characteristics of EWA framework Servlets:

1. **Single entry point**: `ServletMain` handles all page requests
2. **Functional separation**: Upload, resources, verification code are separate Servlets
3. **RESTful support**: `ServletRestful` provides standard API interfaces
4. **Configuration-driven**: All behavior is defined by XML configuration
5. **Cache optimization**: Supports ETag, GZip, SQL caching
6. **Error handling**: Unified error page and debug mode
7. **Security**: ACL permission control, SQL injection prevention
