# EWA Servlet web.xml Configuration Guide

This document provides the complete `web.xml` configuration required for the EWA framework in a web application. emp-script is a JAR library; the host web project must register the relevant Servlets in `web.xml` to use EWA features.

---

## Complete Configuration Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <display-name>EWA Application</display-name>

    <!-- ==================== Core Servlets ==================== -->

    <!-- 1. Main Entry Servlet -->
    <servlet>
        <servlet-name>EwaMain</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletMain</servlet-class>
        <load-on-startup>1</load-on-startup>
        <!-- Optional: specify the directory containing ewa_conf.xml -->
        <!--
        <init-param>
            <param-name>ewa_conf</param-name>
            <param-value>/path/to/conf</param-value>
        </init-param>
        -->
        <!-- Optional: specify the WEB-INF/classes directory -->
        <!--
        <init-param>
            <param-name>ewa_path_real</param-name>
            <param-value>/path/to/WEB-INF/classes</param-value>
        </init-param>
        -->
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaMain</servlet-name>
        <url-pattern>/ewa</url-pattern>
        <url-pattern>/EWA_STYLE/cgi-bin/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 2. File Upload Servlet -->
    <servlet>
        <servlet-name>EwaUpload</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletUpload</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaUpload</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_up_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 3. Static Resources Servlet (JS/CSS combining and output) -->
    <servlet>
        <servlet-name>EwaResources</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletResources</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaResources</servlet-name>
        <url-pattern>/r.ewa</url-pattern>
        <url-pattern>/r1.ewa</url-pattern>
        <url-pattern>/EWA_STYLE/cgi-bin/_re_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 4. RESTful API Servlet -->
    <servlet>
        <servlet-name>EwaRestful</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletRestful</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaRestful</servlet-name>
        <url-pattern>/ewa-api/*</url-pattern>
    </servlet-mapping>

    <!-- 5. Verification Code Servlet -->
    <servlet>
        <servlet-name>EwaCode</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletCode</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaCode</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_co_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 6. Error Page Servlet -->
    <servlet>
        <servlet-name>EwaError</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletError</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaError</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_er_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 7. State Saving Servlet (Tree/FrameSet state) -->
    <servlet>
        <servlet-name>EwaStatus</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletStatus</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaStatus</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_st_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 8. Workflow Servlet -->
    <servlet>
        <servlet-name>EwaWorkflow</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletWorkflow</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaWorkflow</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_wf_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 9. Cross-Domain Policy Servlet -->
    <servlet>
        <servlet-name>EwaCrossDomain</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletCrossDomain</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaCrossDomain</servlet-name>
        <url-pattern>/crossdomain.xml</url-pattern>
    </servlet-mapping>

    <!-- ==================== Define Management Servlets ==================== -->
    <!-- Recommended to remove or disable in production environments -->

    <!-- 10. Define Management Index -->
    <servlet>
        <servlet-name>EwaDefineIndex</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletIndex</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineIndex</servlet-name>
        <url-pattern>/EWA_DEFINE/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 11. XML Configuration Management -->
    <servlet>
        <servlet-name>EwaDefineXml</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletXml</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineXml</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/xml/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 12. Component Import/Export -->
    <servlet>
        <servlet-name>EwaDefineGroup</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletGroup</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineGroup</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/group/*</url-pattern>
    </servlet-mapping>

    <!-- 13. Workflow Definition -->
    <servlet>
        <servlet-name>EwaDefineWorkflow</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletWorkflow</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineWorkflow</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/wf/*</url-pattern>
    </servlet-mapping>

    <!-- 14. Remote Sync -->
    <servlet>
        <servlet-name>EwaDefineRemoteSync</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletRemoteSync</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineRemoteSync</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/remoteSync/*</url-pattern>
    </servlet-mapping>

    <!-- ==================== WebSocket (Optional) ==================== -->
    <!-- WebSocket endpoint must be registered in the host application -->
    <!--
    <servlet>
        <servlet-name>EwaWebSocket</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.websocket.EwaWebSocketBus</servlet-class>
    </servlet>
    -->

</web-app>
```

---

## Servlet Reference

### Core Servlets (Required)

| # | Servlet Name | Class | URL Mapping | Description |
|---|-------------|-------|-------------|-------------|
| 1 | EwaMain | `...script.servlets.ServletMain` | `/ewa`, `/EWA_STYLE/cgi-bin/index.jsp` | Main entry, handles all page requests |
| 2 | EwaUpload | `...script.servlets.ServletUpload` | `/EWA_STYLE/cgi-bin/_up_/index.jsp` | File upload |
| 3 | EwaResources | `...script.servlets.ServletResources` | `/r.ewa`, `/r1.ewa`, `/EWA_STYLE/cgi-bin/_re_/index.jsp` | JS/CSS resource output and combining |
| 4 | EwaRestful | `...script.servlets.ServletRestful` | `/ewa-api/*` | RESTful API entry |

### Core Servlets (Optional)

| # | Servlet Name | Class | URL Mapping | Description |
|---|-------------|-------|-------------|-------------|
| 5 | EwaCode | `...script.servlets.ServletCode` | `/EWA_STYLE/cgi-bin/_co_/index.jsp` | Verification code generation |
| 6 | EwaError | `...script.servlets.ServletError` | `/EWA_STYLE/cgi-bin/_er_/index.jsp` | Error page |
| 7 | EwaStatus | `...script.servlets.ServletStatus` | `/EWA_STYLE/cgi-bin/_st_/index.jsp` | Tree/FrameSet state saving |
| 8 | EwaWorkflow | `...script.servlets.ServletWorkflow` | `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | Workflow processing |
| 9 | EwaCrossDomain | `...script.servlets.ServletCrossDomain` | `/crossdomain.xml` | Cross-domain policy file |

### Define Management Servlets (Development)

| # | Servlet Name | Class | URL Mapping | Description |
|---|-------------|-------|-------------|-------------|
| 10 | EwaDefineIndex | `...define.servlets.ServletIndex` | `/EWA_DEFINE/index.jsp` | Define management index |
| 11 | EwaDefineXml | `...define.servlets.ServletXml` | `/EWA_DEFINE/cgi-bin/xml/index.jsp` | XML configuration management |
| 12 | EwaDefineGroup | `...define.servlets.ServletGroup` | `/EWA_DEFINE/cgi-bin/group/*` | Component import/export |
| 13 | EwaDefineWorkflow | `...define.servlets.ServletWorkflow` | `/EWA_DEFINE/cgi-bin/wf/*` | Workflow definition |
| 14 | EwaDefineRemoteSync | `...define.servlets.ServletRemoteSync` | `/EWA_DEFINE/cgi-bin/remoteSync/*` | Remote synchronization |

> Package prefix: Core Servlets use `com.gdxsoft.easyweb.script.servlets`, Define Management Servlets use `com.gdxsoft.easyweb.define.servlets`.

---

## ServletMain Initialization Parameters

`ServletMain` supports the following optional `init-param` entries:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `ewa_conf` | Directory containing `ewa_conf.xml` | Auto-detected |
| `ewa_path_real` | Project `WEB-INF/classes` directory path | Auto-detected |

Example:
```xml
<init-param>
    <param-name>ewa_conf</param-name>
    <param-value>/opt/app/conf</param-value>
</init-param>
```

---

## Startup Initialization Order

`ServletMain` is configured with `<load-on-startup>1</load-on-startup>` and initializes in the following order at application startup:

```
1. static block -> initEwaInstances()
   ├── EwaConfig.instance()              Load skin configuration
   ├── Skin.instance()                   Load skin resources
   ├── EwaGlobals.instance()             Load global variables
   ├── ConfSecurities.getInstance()      Load security configuration
   ├── ConnectionConfigs.instance()      Initialize database connection pools
   ├── SqlCachedHsqldbImpl.getInstance() Initialize HSQL cache
   ├── SqlCached.getInstance()           Initialize SQL cache
   └── new ValidCode1()                  Load verification code fonts

2. init() method
   ├── Read ewa_conf parameter -> UPath.CONF_NAME
   └── Read ewa_path_real parameter -> UPath.PATH_REAL

3. First request -> service() -> doGet()/doPost()
   └── show() -> EwaWebPage.run() -> Output HTML
```

---

## URL Path Conventions

EWA uses fixed URL path conventions — do not change these:

| Path Pattern | Purpose |
|-------------|---------|
| `/ewa` | Main entry (short path) |
| `/EWA_STYLE/cgi-bin/index.jsp` | Main entry (standard path) |
| `/EWA_STYLE/cgi-bin/_up_/index.jsp` | File upload |
| `/EWA_STYLE/cgi-bin/_re_/index.jsp` | Static resources |
| `/EWA_STYLE/cgi-bin/_co_/index.jsp` | Verification code |
| `/EWA_STYLE/cgi-bin/_er_/index.jsp` | Error page |
| `/EWA_STYLE/cgi-bin/_st_/index.jsp` | State saving |
| `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | Workflow |
| `/r.ewa` / `/r1.ewa` | Resource short paths |
| `/ewa-api/*` | RESTful API |
| `/crossdomain.xml` | Cross-domain policy |
| `/EWA_DEFINE/index.jsp` | Define management index |
| `/EWA_DEFINE/cgi-bin/xml/index.jsp` | XML configuration management |
| `/EWA_DEFINE/cgi-bin/group/*` | Component import/export |
| `/EWA_DEFINE/cgi-bin/wf/*` | Workflow definition |
| `/EWA_DEFINE/cgi-bin/remoteSync/*` | Remote synchronization |

---

## WebSocket Configuration (Optional)

EWA provides WebSocket support. The endpoint class `EwaWebSocketBus` has its `@ServerEndpoint` annotation commented out by default and must be manually registered in the host application.

### Option 1: Programmatic Registration

```java
@ServerEndpoint(value = "/ewa-ws", configurator = EwaWebSocketConfigure.class)
public class EwaWebSocketBus { ... }
```

Uncomment the `@ServerEndpoint` annotation to enable.

### Option 2: web.xml Registration

Register via `ServerEndpointExporter` or container configuration.

### Message Handler Configuration

Configure message handlers in `ewa_conf.xml`:

```xml
<handleWebSocketMessages>
    <handleWebSocketMessage Name="default" MapClass="com.gdxsoft.easyweb.websocket.HandleEwaImpl"/>
</handleWebSocketMessages>
```

---

## Minimal Configuration (Business Functions Only)

If only core EWA business functionality is needed, the minimal configuration is:

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

<servlet>
    <servlet-name>EwaUpload</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletUpload</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaUpload</servlet-name>
    <url-pattern>/EWA_STYLE/cgi-bin/_up_/index.jsp</url-pattern>
</servlet-mapping>

<servlet>
    <servlet-name>EwaResources</servlet-name>
    <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletResources</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>EwaResources</servlet-name>
    <url-pattern>/r.ewa</url-pattern>
    <url-pattern>/r1.ewa</url-pattern>
    <url-pattern>/EWA_STYLE/cgi-bin/_re_/index.jsp</url-pattern>
</servlet-mapping>
```

---

## Security Recommendations

1. **Production**: Remove or comment out all `EWA_DEFINE` Servlets to prevent external configuration modifications
2. **Access Control**: Control define management feature toggle via `ConfDefine.isAllowDefine()`
3. **CORS**: Configure RESTful API cross-origin settings through `ConfRestfuls` in `ewa_conf.xml`
