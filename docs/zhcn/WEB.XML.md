# EWA Servlet web.xml 配置指南

本文档提供 EWA 框架在 Web 应用中所需的完整 `web.xml` 配置。emp-script 是一个 JAR 库，宿主 Web 项目需要在 `web.xml` 中注册相关 Servlet 才能使用 EWA 功能。

---

## 完整配置模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <display-name>EWA Application</display-name>

    <!-- ==================== 核心 Servlet ==================== -->

    <!-- 1. 主入口 Servlet -->
    <servlet>
        <servlet-name>EwaMain</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletMain</servlet-class>
        <load-on-startup>1</load-on-startup>
        <!-- 可选：指定 ewa_conf.xml 所在目录 -->
        <!--
        <init-param>
            <param-name>ewa_conf</param-name>
            <param-value>/path/to/conf</param-value>
        </init-param>
        -->
        <!-- 可选：指定 WEB-INF/classes 目录 -->
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

    <!-- 2. 文件上传 Servlet -->
    <servlet>
        <servlet-name>EwaUpload</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletUpload</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaUpload</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_up_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 3. 静态资源 Servlet (JS/CSS 合并与输出) -->
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

    <!-- 5. 验证码 Servlet -->
    <servlet>
        <servlet-name>EwaCode</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletCode</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaCode</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_co_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 6. 错误页面 Servlet -->
    <servlet>
        <servlet-name>EwaError</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletError</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaError</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_er_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 7. 状态保存 Servlet (Tree/FrameSet 状态) -->
    <servlet>
        <servlet-name>EwaStatus</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletStatus</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaStatus</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_st_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 8. 工作流 Servlet -->
    <servlet>
        <servlet-name>EwaWorkflow</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletWorkflow</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaWorkflow</servlet-name>
        <url-pattern>/EWA_STYLE/cgi-bin/_wf_/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 9. 跨域策略 Servlet -->
    <servlet>
        <servlet-name>EwaCrossDomain</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletCrossDomain</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaCrossDomain</servlet-name>
        <url-pattern>/crossdomain.xml</url-pattern>
    </servlet-mapping>

    <!-- ==================== 定义管理 Servlet ==================== -->
    <!-- 生产环境建议移除或禁用以下 Servlet -->

    <!-- 10. 定义管理首页 -->
    <servlet>
        <servlet-name>EwaDefineIndex</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletIndex</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineIndex</servlet-name>
        <url-pattern>/EWA_DEFINE/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 11. XML 配置管理 -->
    <servlet>
        <servlet-name>EwaDefineXml</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletXml</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineXml</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/xml/index.jsp</url-pattern>
    </servlet-mapping>

    <!-- 12. 组件导入导出 -->
    <servlet>
        <servlet-name>EwaDefineGroup</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletGroup</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineGroup</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/group/*</url-pattern>
    </servlet-mapping>

    <!-- 13. 工作流定义 -->
    <servlet>
        <servlet-name>EwaDefineWorkflow</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletWorkflow</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineWorkflow</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/wf/*</url-pattern>
    </servlet-mapping>

    <!-- 14. 远程同步 -->
    <servlet>
        <servlet-name>EwaDefineRemoteSync</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.define.servlets.ServletRemoteSync</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>EwaDefineRemoteSync</servlet-name>
        <url-pattern>/EWA_DEFINE/cgi-bin/remoteSync/*</url-pattern>
    </servlet-mapping>

    <!-- ==================== WebSocket (可选) ==================== -->
    <!-- 需要在宿主应用中注册 WebSocket 端点 -->
    <!--
    <servlet>
        <servlet-name>EwaWebSocket</servlet-name>
        <servlet-class>com.gdxsoft.easyweb.websocket.EwaWebSocketBus</servlet-class>
    </servlet>
    -->

</web-app>
```

---

## Servlet 说明

### 核心 Servlet (必须配置)

| # | Servlet 名称 | 类名 | URL 映射 | 说明 |
|---|-------------|------|---------|------|
| 1 | EwaMain | `...script.servlets.ServletMain` | `/ewa`, `/EWA_STYLE/cgi-bin/index.jsp` | 主入口，处理所有页面请求 |
| 2 | EwaUpload | `...script.servlets.ServletUpload` | `/EWA_STYLE/cgi-bin/_up_/index.jsp` | 文件上传 |
| 3 | EwaResources | `...script.servlets.ServletResources` | `/r.ewa`, `/r1.ewa`, `/EWA_STYLE/cgi-bin/_re_/index.jsp` | JS/CSS 资源输出与合并 |
| 4 | EwaRestful | `...script.servlets.ServletRestful` | `/ewa-api/*` | RESTful API 入口 |

### 核心 Servlet (可选)

| # | Servlet 名称 | 类名 | URL 映射 | 说明 |
|---|-------------|------|---------|------|
| 5 | EwaCode | `...script.servlets.ServletCode` | `/EWA_STYLE/cgi-bin/_co_/index.jsp` | 验证码生成 |
| 6 | EwaError | `...script.servlets.ServletError` | `/EWA_STYLE/cgi-bin/_er_/index.jsp` | 错误页面 |
| 7 | EwaStatus | `...script.servlets.ServletStatus` | `/EWA_STYLE/cgi-bin/_st_/index.jsp` | Tree/FrameSet 状态保存 |
| 8 | EwaWorkflow | `...script.servlets.ServletWorkflow` | `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | 工作流处理 |
| 9 | EwaCrossDomain | `...script.servlets.ServletCrossDomain` | `/crossdomain.xml` | 跨域策略文件 |

### 定义管理 Servlet (开发环境)

| # | Servlet 名称 | 类名 | URL 映射 | 说明 |
|---|-------------|------|---------|------|
| 10 | EwaDefineIndex | `...define.servlets.ServletIndex` | `/EWA_DEFINE/index.jsp` | 定义管理首页 |
| 11 | EwaDefineXml | `...define.servlets.ServletXml` | `/EWA_DEFINE/cgi-bin/xml/index.jsp` | XML 配置管理 |
| 12 | EwaDefineGroup | `...define.servlets.ServletGroup` | `/EWA_DEFINE/cgi-bin/group/*` | 组件导入导出 |
| 13 | EwaDefineWorkflow | `...define.servlets.ServletWorkflow` | `/EWA_DEFINE/cgi-bin/wf/*` | 工作流定义 |
| 14 | EwaDefineRemoteSync | `...define.servlets.ServletRemoteSync` | `/EWA_DEFINE/cgi-bin/remoteSync/*` | 远程同步 |

> 包名前缀：核心 Servlet 为 `com.gdxsoft.easyweb.script.servlets`，定义管理 Servlet 为 `com.gdxsoft.easyweb.define.servlets`。

---

## ServletMain 初始化参数

`ServletMain` 支持以下 `init-param`，均为可选：

| 参数名 | 说明 | 默认值 |
|--------|------|--------|
| `ewa_conf` | `ewa_conf.xml` 配置文件所在目录 | 自动检测 |
| `ewa_path_real` | 项目 `WEB-INF/classes` 目录路径 | 自动检测 |

示例：
```xml
<init-param>
    <param-name>ewa_conf</param-name>
    <param-value>/opt/app/conf</param-value>
</init-param>
```

---

## 启动初始化顺序

`ServletMain` 配置 `<load-on-startup>1</load-on-startup>`，应用启动时按以下顺序初始化：

```
1. static 块 → initEwaInstances()
   ├── EwaConfig.instance()          加载皮肤配置
   ├── Skin.instance()               加载皮肤资源
   ├── EwaGlobals.instance()         加载全局变量
   ├── ConfSecurities.getInstance()  加载安全配置
   ├── ConnectionConfigs.instance()  初始化数据库连接池
   ├── SqlCachedHsqldbImpl.getInstance()  初始化 HSQL 缓存
   ├── SqlCached.getInstance()       初始化 SQL 缓存
   └── new ValidCode1()              加载验证码字体

2. init() 方法
   ├── 读取 ewa_conf 参数 → UPath.CONF_NAME
   └── 读取 ewa_path_real 参数 → UPath.PATH_REAL

3. 首次请求 → service() → doGet()/doPost()
   └── show() → EwaWebPage.run() → 输出 HTML
```

---

## URL 路径约定

EWA 使用固定的 URL 路径约定，请勿更改：

| 路径模式 | 用途 |
|---------|------|
| `/ewa` | 主入口（短路径） |
| `/EWA_STYLE/cgi-bin/index.jsp` | 主入口（标准路径） |
| `/EWA_STYLE/cgi-bin/_up_/index.jsp` | 文件上传 |
| `/EWA_STYLE/cgi-bin/_re_/index.jsp` | 静态资源 |
| `/EWA_STYLE/cgi-bin/_co_/index.jsp` | 验证码 |
| `/EWA_STYLE/cgi-bin/_er_/index.jsp` | 错误页面 |
| `/EWA_STYLE/cgi-bin/_st_/index.jsp` | 状态保存 |
| `/EWA_STYLE/cgi-bin/_wf_/index.jsp` | 工作流 |
| `/r.ewa` / `/r1.ewa` | 资源短路径 |
| `/ewa-api/*` | RESTful API |
| `/crossdomain.xml` | 跨域策略 |
| `/EWA_DEFINE/index.jsp` | 定义管理首页 |
| `/EWA_DEFINE/cgi-bin/xml/index.jsp` | XML 配置管理 |
| `/EWA_DEFINE/cgi-bin/group/*` | 组件导入导出 |
| `/EWA_DEFINE/cgi-bin/wf/*` | 工作流定义 |
| `/EWA_DEFINE/cgi-bin/remoteSync/*` | 远程同步 |

---

## WebSocket 配置 (可选)

EWA 提供 WebSocket 支持，端点类 `EwaWebSocketBus` 的 `@ServerEndpoint` 注解默认被注释，需要在宿主应用中手动注册。

### 方式一：编程注册

```java
@ServerEndpoint(value = "/ewa-ws", configurator = EwaWebSocketConfigure.class)
public class EwaWebSocketBus { ... }
```

取消 `@ServerEndpoint` 注解注释即可。

### 方式二：web.xml 注册

通过 `ServerEndpointExporter` 或容器配置注册。

### 消息处理配置

在 `ewa_conf.xml` 中配置消息处理器：

```xml
<handleWebSocketMessages>
    <handleWebSocketMessage Name="default" MapClass="com.gdxsoft.easyweb.websocket.HandleEwaImpl"/>
</handleWebSocketMessages>
```

---

## 最小配置 (仅业务功能)

如果只需要 EWA 核心业务功能，最小配置如下：

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

## 安全建议

1. **生产环境**：移除或注释所有 `EWA_DEFINE` 相关 Servlet，防止配置被外部修改
2. **访问控制**：通过 `ConfDefine.isAllowDefine()` 控制定义管理功能开关
3. **CORS**：RESTful API 的跨域设置通过 `ewa_conf.xml` 中的 `ConfRestfuls` 配置
