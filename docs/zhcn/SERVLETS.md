# EWA Servlet 详解

## Servlet 总览

EWA 框架共有 **14 个 Servlet**，分为两大类：

### 核心 Servlet (script.servlets 包)
| Servlet | URL 映射 | 用途 |
|---------|---------|------|
| **ServletMain** | `/ewa`, `/EWA_STYLE/cgi-bin/index.jsp` | 主入口，处理页面请求 |
| **ServletUpload** | `/EWA_STYLE/cgi-bin/_up_/index.jsp` | 文件上传 |
| **ServletCode** | `/EWA_STYLE/cgi-bin/_co_/index.jsp` | 验证码生成 (已废弃) |
| **ServletResources** | `/r.ewa`, `/r1.ewa`, `/EWA_STYLE/cgi-bin/_re_/index.jsp` | 静态资源、JS/CSS 合并 |
| **ServletStatus** | `/EWA_STYLE/cgi-bin/_st_/index.jsp` | 树状态保存/恢复 |
| **ServletError** | `/EWA_STYLE/cgi-bin/_er_/index.jsp` | 错误页面 |
| **ServletWorkflow** | `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | 工作流处理 |
| **ServletRestful** | `/ewa-api/*` | RESTful API 入口 |
| **ServletCrossDomain** | `/crossdomain.xml` | 跨域策略文件 |

### 定义管理 Servlet (define.servlets 包)
| Servlet | URL 映射 | 用途 |
|---------|---------|------|
| **ServletIndex** | `/EWA_DEFINE/index.jsp` | 定义管理首页 |
| **ServletXml** | `/EWA_DEFINE/cgi-bin/xml/index.jsp` | XML 配置管理 |
| **ServletGroup** | `/EWA_DEFINE/cgi-bin/group/*` | 组件导入导出 |
| **ServletWorkflow** | `/EWA_DEFINE/cgi-bin/wf/*` | 工作流定义 |
| **ServletRemoteSync** | `/EWA_DEFINE/cgi-bin/remoteSync/*` | 远程同步 |

---

## 1. ServletMain - 主入口 Servlet

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletMain.java`

### web.xml 配置
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

### 生命周期方法

#### 1.1 静态初始化 (类加载时)
```java
static {
    initEwaInstances();  // 初始化所有 EWA 实例
}

public synchronized static void initEwaInstances() {
    EwaConfig.instance();              // 加载皮肤配置
    Skin.instance();                   // 加载皮肤
    EwaGlobals.instance();             // 加载全局变量
    ConfSecurities.getInstance();      // 加载安全配置
    ConnectionConfigs.instance();      // 初始化数据库连接池
    SqlCachedHsqldbImpl.getInstance(); // 初始化 HSQL 缓存
    SqlCached.getInstance();           // 初始化缓存
    new ValidCode1();                  // 加载验证码字体
}
```

#### 1.2 init() 方法 (Servlet 初始化)
```java
public void init() throws ServletException {
    super.init();
    
    // 从 web.xml 获取 init-param 参数
    Enumeration<String> names = this.getInitParameterNames();
    while (names.hasMoreElements()) {
        String name = names.nextElement();
        String value = this.getInitParameter(name);
        
        if ("ewa_conf".equals(name)) {
            // 设置 ewa_conf.xml 配置文件所在目录
            UPath.CONF_NAME = value;
        } else if ("ewa_path_real".equals(name)) {
            // 设置项目 WEB-INF/classes 目录
            UPath.PATH_REAL = value;
        }
    }
    
    initEwaInstances();
}
```

#### 1.3 service() 方法 (请求处理)
```java
@Override
public void service(HttpServletRequest req, HttpServletResponse resp) {
    String method = req.getMethod();
    if ("PATCH".equalsIgnoreCase(method)) {
        this.doPatch(req, resp);
    } else {
        super.service(req, resp);  // 分发到 doGet/doPost 等
    }
}
```

#### 1.4 show() 方法 (核心处理逻辑)
```java
public void show(HttpServletRequest request, HttpServletResponse response) {
    // 1. 设置编码
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-Type", "text/html;charset=UTF-8");
    response.setHeader("X-EWA-ENGIN", "emp-script");
    
    // 2. 创建 EwaWebPage 并执行
    HttpSession session = request.getSession();
    EwaWebPage p = new EwaWebPage(request, session, response);
    p.run();
    
    // 3. 处理错误
    if (p.isPageError()) {
        p.getDebugInfo().recordToHsql();  // 记录到数据库
        if (!p.isPageDebug()) {
            response.sendRedirect(request.getContextPath() + EwaWebPage.ERR_PAGE);
            return;
        }
    }
    
    // 4. 处理特殊请求
    RequestValue rv = p.getHtmlCreator().getRequestValue();
    
    if ("download-inline".equalsIgnoreCase(rv.s("EWA_AJAX"))) {
        this.outImage(rv, response, p);  // 输出图片
        return;
    }
    if ("validcode".equalsIgnoreCase(rv.s("EWA_AJAX"))) {
        this.outValidCode(p);  // 输出验证码
        return;
    }
    if ("download".equalsIgnoreCase(rv.s("EWA_AJAX"))) {
        this.downloadFile(rv, response, p, null);  // 下载文件
        return;
    }
    
    // 5. 输出 HTML 内容 (支持 GZip)
    String cnt = p.getPageContent();
    if (p.isPageDebug()) {
        cnt += p.getPageDeubgInfo();  // 附加 Debug 信息
    }
    this.outContent(request, response, cnt);
}
```

### 支持的 HTTP 方法
- `doGet()` - GET 请求
- `doPost()` - POST 请求
- `doPut()` - PUT 请求
- `doDelete()` - DELETE 请求
- `doPatch()` - PATCH 请求

---

## 2. ServletUpload - 文件上传

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletUpload.java`

### URL 映射
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

### 核心处理逻辑
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    JSONObject result = new JSONObject();
    PrintWriter out = response.getWriter();
    
    // 1. 初始化 RequestValue
    RequestValue rv = new RequestValue(request, request.getSession());
    
    // 2. 初始化 Upload 组件
    Upload up = new Upload();
    up.setRv(rv);
    up.init(request);
    
    // 3. 配置 Apache Commons FileUpload
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(10 * 1024 * 1024);  // 10M 内存缓冲区
    factory.setRepository(new File(UPath.getPATH_UPLOAD() + "/upload"));
    
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(2 * 1024 * 1024 * 1024);  // 2G 最大上传
    
    // 4. 解析请求
    List<FileItem> items = upload.parseRequest(request);
    for (FileItem item : items) {
        if (item.isFormField()) {
            // 提取表单参数到 rv
            rv.addValue(item.getFieldName(), item.getString());
        }
    }
    
    // 5. 执行上传
    up.setUploadItems(items);
    String rst = up.upload();
    out.println(rst);
}
```

### 上传配置
| 参数 | 默认值 | 说明 |
|------|--------|------|
| 内存缓冲区 | 10MB | 超过后写入临时文件 |
| 最大上传 | 2GB | 单个文件最大大小 |
| 临时目录 | `UPath.getPATH_UPLOAD()/upload` | 临时文件存储位置 |

---

## 3. ServletResources - 静态资源

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletResources.java`

### URL 映射
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

### 功能列表

#### 3.1 获取 JS 资源 (默认)
```java
// URL: /r.ewa 或 /r.ewa?method=xxx
String lang = "zhcn";  // 或 enus
String js = EwaGlobals.instance().createJs(lang);
response.setContentType("text/javascript");
out1.outContent(js);  // GZip 压缩输出
```

#### 3.2 HTML 编辑器图片处理
```java
// URL: /r.ewa?method=HtmlImages
// 从 HTML 编辑器获取图片信息
String rst = this.handleGetImages(request, response);
out1.outContent(rst);
```

#### 3.3 文件转 HTML
```java
// URL: /r.ewa?method=2Html
// 将上传的 Office 文档转换为 HTML
String rst = this.handleCreateHtml(request, response);
out1.outContent(rst);
```

#### 3.4 JS/CSS 合并
```java
// URL: /r.ewa?method=combine&tag=xxx
// 或 URL: /r.ewa?method=combine&files=file1.js;file2.css
String rst = this.handleCombineMaster(request, response);
out1.outContent(rst);
```

**合并配置示例** (ewa_conf.xml):
```xml
<combineResources Name="main-js" Type="js" Host="http://cdn.example.com">
    <resource Debug="/js/jquery.js" Normal="/js/jquery.min.js"/>
    <resource Debug="/js/ewa.js" Normal="/js/ewa.min.js"/>
</combineResources>
```

### 缓存机制
```java
// ETag 缓存
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

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletRestful.java`

### URL 映射
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

### 核心处理流程
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
    
    // 1. 解析请求路径
    String uri = request.getRequestURI();
    String path = uri.substring(request.getContextPath().length());
    
    // 2. 获取 HTTP 方法
    String httpMethod = request.getMethod().toUpperCase();
    
    // 3. 解析参数
    RequestValue rv;
    if ("GET".equalsIgnoreCase(httpMethod)) {
        rv = new RequestValue(request);
    } else if (isMultipart(request)) {
        rv = new RequestValue(request);
        isUpload = true;
    } else {
        rv = new RequestValue(request, true);  // JSON 体解析
    }
    
    // 4. 查找 ConfRestful 配置
    ConfRestful conf = ConfRestfuls.getInstance()
        .getConfRestful(path, httpMethod, rv, result);
    
    if (conf == null) {
        response.setStatus(result.getHttpStatusCode());
        return result.toString();  // 404 JSON
    }
    
    // 5. 根据类型处理
    if (isOutImage) {
        this.handleImage(conf, rv, response, result);
        return null;  // 已直接输出
    } else if (isDownload) {
        this.handleDownload(conf, rv, response, result);
    } else if (isUpload) {
        this.handleUpload(conf, rv, request, result);
    } else {
        this.handleConf(conf, rv, response, result);
    }
    
    // 6. 设置 CORS
    String cors = ConfRestfuls.getInstance().getCors();
    response.setHeader("Access-Control-Allow-Origin", cors);
    response.setStatus(result.getHttpStatusCode());
    
    return result.toString();
}
```

### HTTP 方法映射
| HTTP 方法 | EWA 参数 | 说明 |
|----------|---------|------|
| GET | `EWA_AJAX=JSON_EXT` | 查询列表 |
| POST | `EWA_ACTION=OnPagePost`, `EWA_MTYPE=N` | 新增 |
| PUT | `EWA_ACTION=OnPagePost`, `EWA_MTYPE=M` | 修改 |
| PATCH | `EWA_ACTION=OnFrameRestore` | 恢复 (逻辑删除恢复) |
| DELETE | `EWA_ACTION=OnFrameDelete` | 删除 |

### 响应格式
```json
{
    "success": true,
    "code": 200,
    "message": "操作成功",
    "data": {...},
    "ewaPageCur": 1,
    "ewaPageSize": 20,
    "pageCount": 10,
    "recordCount": 200,
    "start": 123,
    "end": 456
}
```

### HTTP 状态码
| 状态码 | 场景 |
|--------|------|
| 200 OK | 查询成功、修改成功 |
| 201 Created | POST 创建成功 |
| 204 No Content | 无数据 |
| 400 Bad Request | SQL 执行错误 |
| 401 Unauthorized | 权限验证失败 |
| 403 Forbidden | 系统执行错误 |
| 404 Not Found | 配置未找到、数据不存在 |
| 500 Internal Server Error | 系统内部错误 |

---

## 5. ServletCode - 验证码

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletCode.java`

### URL 映射
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

### 处理逻辑
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("image/jpeg");
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    
    ServletOutputStream output = response.getOutputStream();
    
    // 从 XML 配置读取验证码设置
    UserConfig uc = UserConfig.instance(xmlName, itemname, null);
    int len = 4;  // 默认 4 位
    boolean isNumberCode = true;  // 数字验证码
    
    // 生成验证码
    ValidCode1 vc = new ValidCode1(len, isNumberCode);
    ImageIO.write(vc.createCode(), "jpeg", output);
    
    // 保存到 Session
    session.setAttribute(ValidCode.SESSION_NAME, vc.getRandomNumber());
}
```

**注意**: 该 Servlet 已废弃，建议使用 `ServletMain` 的 `ewa_ajax=validcode` 参数。

---

## 6. ServletError - 错误页面

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletError.java`

### URL 映射
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

### 处理逻辑
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    String ajax = request.getParameter("EWA_AJAX");
    
    if (ajax == null || ajax.trim().length() == 0) {
        // 输出完整错误页面 HTML
        o.println("<!DOCTYPE html>");
        o.println("<html><head>...");
        o.println("<body onload=\"msg(true);\">");
    } else {
        // 输出错误消息 JS
        o.println("msg(false);");
    }
}
```

---

## 7. ServletStatus - 状态保存

**文件**: `src/main/java/com/gdxsoft/easyweb/script/servlets/ServletStatus.java`

### URL 映射
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

### 功能
保存和恢复 Tree/FrameSet 的状态到 Session：

```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    RequestValue rv = new RequestValue(request, session);
    
    String url = rv.getString("U");  // URL
    String t = rv.getString("T");    // 类型：TREE 或 FRAMESET
    int code = url.hashCode();
    String sessionName = t + code;
    
    if (rv.getString("M").equals("1")) {
        // 保存状态
        String val = rv.getString("V");
        session.setAttribute(sessionName, val);
    } else {
        // 获取状态
        statusValue = session.getAttribute(sessionName);
    }
    
    out.println("_EWA_STATUS_VALUE=\"" + statusValue + "\";");
}
```

---

## 8. ServletIndex - 定义管理首页

**文件**: `src/main/java/com/gdxsoft/easyweb/define/servlets/ServletIndex.java`

### URL 映射
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

### 处理逻辑
```java
private void show(HttpServletRequest request, HttpServletResponse response) {
    // 检查是否允许定义管理
    if (!ConfDefine.isAllowDefine()) {
        response.setStatus(404);
        return;
    }
    
    // 初始化 HSQL 和管理员信息
    EwaConfHelpHSqlServer.getInstance();
    ConfAdmins.getInstance().getLst();
    
    // 重定向到配置管理页面
    String urlDefine = request.getContextPath()
        + "/EWA_STYLE/cgi-bin/?XMLNAME=/ewa/ewa.xml&ITEMNAME=index";
    response.sendRedirect(urlDefine);
}
```

---

## 9. ServletXml - XML 配置管理

**文件**: `src/main/java/com/gdxsoft/easyweb/define/servlets/ServletXml.java`

### URL 映射
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

### 功能列表

| TYPE | 说明 |
|------|------|
| `format` | 格式化代码 |
| `GUNID` | 获取全局唯一 ID |
| `saveXml` | 保存 XML 配置 |
| `deleteXml` | 删除 XML 配置 |
| `getXml` | 获取 XML 内容 |
| `formatSql` | 格式化 SQL |
| `checkSql` | 检查 SQL 语法 |
| `exportModule` | 导出模块 |
| `importModule` | 导入模块 |

### 保存 XML 示例
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

## Servlet 关系图

```
┌─────────────────────────────────────────────────────────────┐
│                      web.xml 配置                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌──────────────┐ ┌──────────┐ ┌──────────────┐
│  ServletMain │ │ServletUp │ │ServletResou..│
│  (主入口)     │ │(上传)     │ │(资源)         │
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

## Servlet 初始化顺序

```
1. 类加载 (static 块)
   └─► initEwaInstances()
       ├─► EwaConfig.instance()
       ├─► Skin.instance()
       ├─► EwaGlobals.instance()
       ├─► ConnectionConfigs.instance()
       └─► SqlCached.getInstance()

2. Servlet init()
   └─► 读取 web.xml init-param
       ├─► ewa_conf (配置文件路径)
       └─► ewa_path_real (WEB-INF 目录)

3. 第一次请求
   └─► service() → doGet()/doPost()
       └─► show()
           ├─► EwaWebPage.run()
           ├─► HtmlCreator.createPageHtml()
           └─► 输出内容
```

---

## 总结

EWA 框架的 Servlet 设计特点：

1. **单一入口**: `ServletMain` 处理所有页面请求
2. **功能分离**: 上传、资源、验证码等独立 Servlet
3. **RESTful 支持**: `ServletRestful` 提供标准 API 接口
4. **配置驱动**: 所有行为由 XML 配置定义
5. **缓存优化**: 支持 ETag、GZip、SQL 缓存
6. **错误处理**: 统一错误页面和 Debug 模式
7. **安全性**: ACL 权限控制、SQL 注入防护
