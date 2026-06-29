---
name: embedded-tomcat-demo
description: Create embedded Tomcat 10 demo sub-project with emp-script integration. Supports both HSQLDB server mode and file mode. Includes configurable properties, zip-based data initialization, and P6Spy SQL logging.
source: auto-skill
extracted_at: '2026-06-29T07:53:08.135Z'
updated_at: '2026-06-29T07:58:00.000Z'
---

# Embedded Tomcat 10 Demo with emp-script

## When to Use
- Creating a standalone demo/test environment for emp-script
- Need embedded Tomcat with JSP, Servlet, and HSQLDB support
- Migrating MySQL data to HSQLDB for offline demo systems

## Project Structure

```
tomcat10demo/
├── pom.xml
├── bin/
│   ├── start.sh          # 后台启动 (nohup, PID 管理, 日志 app.log)
│   ├── stop.sh           # 停止 (kill → 等待 → kill -9)
│   └── debug.sh          # 前台调试模式 (Ctrl+C 停止)
├── sql/                  # HSQLDB DDL 脚本 (可选, 用于无 MySQL 初始化)
│   ├── emp_ewa.sql
│   ├── emp_main_data.sql
│   └── emp_portal.sql
├── hsqldb/               # HSQLDB 数据文件 (git 保留)
├── src/main/java/com/gdxsoft/emp/demo/
│   ├── Tomcat10EmbeddedServer.java    # Main entry: auto-extract zip → start Tomcat
│   ├── HsqldbServerManager.java       # HSQLDB Server mode manager (可选, file 模式下不需要)
│   ├── ConfigLoader.java              # 配置加载器 (读取 application.properties)
│   ├── MysqlToHsqldbExporter.java     # 双模式: SQL 文件导入 / MySQL 导出
│   └── HelloServlet.java
├── src/main/resources/
│   ├── application.properties         # 配置文件 (端口, 数据库路径等)
│   ├── spy.properties                 # P6Spy 配置 (SQL 日志)
│   └── ewa_conf.xml                   # EWA 框架配置 (数据源, scriptPaths 等)
├── src/main/webapp/
│   ├── WEB-INF/web.xml                # emp-script servlets
│   ├── back_admin/
│   │   ├── login.jsp                  # 登录页 (HtmlControl)
│   │   └── index.jsp                  # 管理首页 (HtmlControl)
│   ├── index.jsp                      # 首页
│   └── hello.jsp                      # 示例 JSP
├── hsqldb/               # HSQLDB 数据文件 (git 保留)
```

## pom.xml Dependencies

```xml
<!-- emp-script main project -->
<dependency>
    <groupId>com.gdxsoft.easyweb</groupId>
    <artifactId>emp-script-jdk17</artifactId>
    <version>1.1.10</version>
</dependency>

<!-- Tomcat 10 embedded -->
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-core</artifactId>
    <version>10.1.40</version>
</dependency>
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
    <version>10.1.40</version>
</dependency>
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-el</artifactId>
    <version>10.1.40</version>
</dependency>

<!-- HSQLDB -->
<dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
    <version>2.7.4</version>
</dependency>

<!-- MySQL driver (export tool only) -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.7.0</version>
</dependency>
```

## Critical Embedded Tomcat Patterns

### 1. JSP (Jasper) ClassLoader
Must set parent classloader, otherwise JSP compilation fails with ClassNotFoundException:
```java
Context ctx = tomcat.addWebapp("", webappDir);
ctx.setParentClassLoader(Tomcat10EmbeddedServer.class.getClassLoader());
```

### 2. Session Config
Do NOT define `<session-config>` in web.xml — `addWebapp()` merges Tomcat's default web.xml which already contains session-config. Duplicate causes `IllegalArgumentException`.
Set timeout via code instead:
```java
ctx.setSessionTimeout(30); // minutes
```

### 3. JNDI DataSource (if needed)
```java
tomcat.enableNaming();
System.setProperty("java.naming.factory.initial", 
    "org.apache.naming.java.javaURLContextFactory");

// Register in BEFORE_START_EVENT, not after addWebapp()
ctx.addLifecycleListener(event -> {
    if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
        // register JNDI resources here
    }
});
```

### 4. JDBC URL
Do NOT use `autoReconnect=true` — removed in MySQL Connector/J 8.0, causes DBCP2 errors.

## HSQLDB Server Mode

```java
Server server = new Server();
server.setPort(11002);
server.setDatabaseName(0, "emp_ewa");
server.setDatabasePath(0, "/path/to/hsqldb-data/emp_ewa");
server.start();
```

Connect via: `jdbc:hsqldb:hsql://localhost:11002/emp_ewa`

### HSQLDB Oracle Syntax Compatibility

HSQLDB supports Oracle-compatible SQL syntax. Enable it on startup via `HsqldbServerManager.initDatabases()`:

```java
try (Connection conn = DriverManager.getConnection(url, "sa", "");
     Statement st = conn.createStatement()) {
    st.execute("SET DATABASE SQL SYNTAX ORA TRUE");
    conn.commit();
}
```

This enables:
- `DUAL` table (auto-created, no manual `CREATE TABLE DUAL` needed)
- `NVL()`, `NVL2()` functions
- `SYSDATE` keyword
- `CONNECT BY` hierarchical queries
- Oracle-style string concatenation with `||`

**Important:** This setting is persisted in the database `.script` file once committed. But `HsqldbServerManager` re-applies it on every startup to ensure new databases also get it.

**Why:** emp-script's EWA XML configurations use Oracle-compatible SQL (e.g., `SELECT ... FROM DUAL`). Without this mode, queries fail with "object not found: DUAL".

### HSQLDB Persistence: SHUTDOWN COMPACT Required

**Critical:** When creating tables/data via JDBC in HSQLDB Server mode, changes are written to the `.log` file first, NOT directly to `.script`. If you only call `server.shutdown()`, the `.log` may not be merged into `.script`, and tables appear lost on next startup.

**Solution:** Before stopping the server, connect via JDBC and execute `SHUTDOWN COMPACT` on each database:
```java
for (String dbName : databases) {
    String url = "jdbc:hsqldb:hsql://localhost:" + port + "/" + dbName;
    try (Connection conn = DriverManager.getConnection(url, "sa", "");
         Statement st = conn.createStatement()) {
        st.execute("SHUTDOWN COMPACT");
    }
}
// Then stop the server
server.shutdown();
```

**Why:** `server.shutdown()` sends a shutdown signal but doesn't guarantee a clean checkpoint. `SHUTDOWN COMPACT` forces HSQLDB to rewrite the `.script` file with all current schema and data, then clears the `.log` file. Without this, the `.script` file remains empty/stale and the `.log` file (which has the actual changes) may be lost or ignored on restart.

**How to apply:** Any time you programmatically create/modify HSQLDB databases (import tools, test setup, data migration), always execute `SHUTDOWN COMPACT` via JDBC before calling `server.shutdown()`.

### HSQLDB UNION Requires FROM DUAL

HSQLDB requires every SELECT statement to have a FROM clause. Unlike MySQL which allows `SELECT 1`, HSQLDB requires `SELECT 1 FROM DUAL`. This applies to UNION queries as well:

```sql
-- WRONG: fails with "unexpected token: UNION"
SELECT -99999999, 0, '-- Deleted --', 1, 999999999
UNION
SELECT -1, 0, '-- ALL --', 1, -1

-- CORRECT: add FROM DUAL to each SELECT
SELECT -99999999, 0, '-- Deleted --', 1, 999999999 FROM DUAL
UNION
SELECT -1, 0, '-- ALL --', 1, -1 FROM DUAL
```

**Why:** HSQLDB's SQL parser strictly requires a FROM clause for every SELECT. Even with Oracle syntax mode enabled (`SET DATABASE SQL SYNTAX ORA TRUE`), bare SELECT without FROM fails.

**How to apply:** When fixing EWA XML configurations that use UNION with constant SELECT statements, add `FROM DUAL` to each SELECT that lacks a table reference. Search for patterns like `UNION\n    SELECT -` or `UNION SELECT` without a FROM keyword before the next UNION/ORDER BY.

### HSQLDB NULL Comparison: IS NOT DISTINCT FROM

Standard SQL `= NULL` always returns NULL (not true). For parameterized queries where the parameter may be NULL, use `IS NOT DISTINCT FROM`:

```sql
-- WRONG: when @param is NULL, this never matches
WHERE column = @param

-- CORRECT: matches both equal values and both-NULL
WHERE column IS NOT DISTINCT FROM @param
```

**Why:** EWA XML configurations often have parameters that may be NULL. `column = NULL` evaluates to UNKNOWN in SQL, so no rows match. `IS NOT DISTINCT FROM` treats NULL as a comparable value (NULL IS NOT DISTINCT FROM NULL → true).

**How to apply:** When fixing SQL errors where parameterized queries return no results with NULL parameters, replace `= @param` with `IS NOT DISTINCT FROM @param`.

## Security: Credentials via -D Parameters

Never hardcode credentials. Pass via system properties:
```bash
mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.MysqlToHsqldbExporter" \
  -Dmysql.url="jdbc:mysql://host:port" \
  -Dmysql.user=root \
  -Dmysql.password="xxx" \
  -Dmysql.databases="emp_ewa,emp_main_data,emp_portal"
```

In Java:
```java
String url = System.getProperty("mysql.url");
String user = System.getProperty("mysql.user");
String password = System.getProperty("mysql.password");
```

## MySQL → HSQLDB Type Mapping

| MySQL Type | HSQLDB Type |
|------------|-------------|
| AUTO_INCREMENT | GENERATED BY DEFAULT AS IDENTITY |
| INT/BIGINT | INTEGER/BIGINT |
| VARCHAR(n) where n ≤ 100000 | VARCHAR(n) |
| VARCHAR(n) where n > 100000 or LONGTEXT/TEXT | CLOB |
| DATETIME | TIMESTAMP |
| BLOB/LONGBLOB | BLOB |
| DECIMAL(p,s) | DECIMAL(p,s) |
| BIT/TINYINT(1) | BOOLEAN |

**Important:** Large text fields (precision > 100000 or ≤ 0) must map to CLOB, not VARCHAR. VARCHAR(1000000) causes "string data, right truncation" errors when actual data exceeds the limit. CLOB has no practical size limit.

## SQL File Import Mode (No MySQL Required)

The exporter supports two modes:
1. **SQL file import** (default): Reads `sql/*.sql` files and executes against HSQLDB
2. **MySQL sync**: Connects to MySQL and exports table structure + data

SQL file parsing: strips `--` comment lines, splits by `;`, handles multi-line CREATE TABLE statements.

```bash
# SQL file import only (no MySQL needed)
mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.MysqlToHsqldbExporter"

# MySQL sync (all databases)
mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.MysqlToHsqldbExporter" \
  -Dmysql.url="jdbc:mysql://host:port" -Dmysql.user=root -Dmysql.password="xxx" \
  -Dmysql.databases="emp_ewa,emp_main_data,emp_portal"
```

## Original MySQL Database Contents (before merge)

| Database | Tables | Key Data |
|----------|--------|----------|
| emp_ewa | 17 | ewa_cfg(176), ewa_cfg_his(503), ewa_cfg_tree(33) |
| emp_main_data | 40 | bas_date(7670), city(2445), bas_idx(1737), adm_menu(63) |
| emp_portal | 18 | p_ewa_mod_cfg_obj(5379), p_ewa_mod_ddl(440), p_ewa_mod_cfg(767) |

**Note:** emp_main_data has been merged into emp_portal (48 tables total). See "Current Database Layout" section above.

## Startup Sequence

1. Start HSQLDB Server (port 11002)
2. Start Tomcat (port 8080)
3. Register shutdown hook to stop both

## Port Conflict Handling

### stop.sh: Kill by Port
Always release ports 8080 (Tomcat) and 11002 (HSQLDB) by PID lookup:
```bash
for PORT in 8080 11002; do
    PIDS=$(lsof -ti:$PORT 2>/dev/null)
    if [ -n "$PIDS" ]; then
        echo "$PIDS" | xargs kill -9 2>/dev/null
    fi
done
```

### start.sh: Pre-flight Checks
1. Check PID file for existing instance
2. Check port availability (8080, 11002) — abort if occupied
3. Post-launch: poll `curl http://localhost:8080/` up to 15s to confirm readiness

**Why:** `pkill -f` may miss child processes (Maven spawns Java). Port-based kill is more reliable. HSQLDB `BindException: Address already in use` is the most common startup failure — always check ports first.

## Suppressing unoloader.jar Warning

emp-script's JODConverter dependency pulls in OpenOffice jars (`jurt`, `juh`). These jars have `Class-Path: unoloader.jar` in their MANIFEST.MF. Tomcat's jar scanner follows this reference and warns when `unoloader.jar` is missing:
```
警告: Failed to scan [file:/...unoloader.jar] from classloader hierarchy
java.nio.file.NoSuchFileException
```

**Root cause:** The warning is NOT from Maven classpath — it's from the `Class-Path` entry in `jurt.jar` and `juh.jar` manifests. Tomcat's `StandardJarScanner` follows manifest Class-Path references and tries to open the referenced files.

**Fix:** Remove the `Class-Path` entry from the manifests:
```bash
for JAR in ~/.m2/repository/org/openoffice/jurt/4.1.2/jurt-4.1.2.jar \
           ~/.m2/repository/org/openoffice/juh/4.1.2/juh-4.1.2.jar; do
    TMPDIR=$(mktemp -d)
    cd "$TMPDIR" && unzip -q "$JAR" META-INF/MANIFEST.MF
    sed -i '' '/^Class-Path:/d' "$TMPDIR/META-INF/MANIFEST.MF"
    cd "$TMPDIR" && zip -q -u "$JAR" META-INF/MANIFEST.MF
    rm -rf "$TMPDIR"
done
```

**Note:** pom.xml `<exclusion>` does NOT fix this because the reference is inside the jar manifest, not in the Maven dependency tree.

## ewa_conf.xml Database Config Format

`ConnectionConfig` requires these attributes on the `<database>` element:
- `name` — connection name (lowercase)
- `type` — database type (HSQLDB, MYSQL, MSSQL)
- `connectionstring` — connection identifier (for HSQLDB, same as database name)
- `schemaname` — schema name (for HSQLDB, always `PUBLIC`)

Missing `connectionstring` or `schemaname` triggers: `WARN Invalid database cfg -> <database ...>`

Correct format:
```xml
<database name="emp_ewa" type="HSQLDB" connectionstring="emp_ewa" schemaname="PUBLIC">
    <pool driverClassName="org.hsqldb.jdbc.JDBCDriver"
          url="jdbc:hsqldb:hsql://localhost:11002/emp_ewa"
          username="sa" password="" maxActive="20" maxIdle="10"/>
</database>
```

## Database Aliases

`ConnectionConfig` supports `<alias>` sub-elements. Aliases allow XML configurations to reference a database by alternate names. The `ConnectionConfigs` class registers aliases as additional keys pointing to the same `ConnectionConfig`:

```xml
<database name="emp_portal" type="HSQLDB" connectionstring="emp_portal" schemaname="PUBLIC">
    <alias name="emp"/>
    <pool .../>
</database>
```

Now both `emp_portal` and `emp` resolve to the same connection pool.

**Current alias mapping:**
| Database | Aliases |
|----------|---------|
| emp_portal | emp |
| emp_main_data | main_data (points to emp_portal after merge) |
| emp_ewa | ewa2023 |

**Why:** XML configurations in emp-script reference databases by name (e.g., `DataSource="main_data"`). Aliases allow these references to work without renaming all XML configs when database names change.

## HSQLDB File Mode (替代 Server Mode)

HSQLDB 支持两种连接模式：

| 模式 | JDBC URL | 特点 |
|------|----------|------|
| **Server** | `jdbc:hsqldb:hsql://host:port/dbname` | 独立进程，支持多客户端 |
| **File** | `jdbc:hsqldb:file:/path/to/dbname` | 同一进程内，无需服务器 |

**File 模式优势：**
- 无需启动 HSQLDB 服务器进程
- 无端口占用（不占用 11002）
- 启动更快（省去 server 初始化）
- 部署更简单（无需管理独立进程）
- 适用于单机 demo/开发环境

### File 模式配置

**ewa_conf.xml 中的 JDBC URL：**
```xml
<!-- Server 模式 -->
<database name="emp_ewa" type="HSQLDB" connectionstring="emp_ewa" schemaname="PUBLIC">
    <pool driverClassName="com.p6spy.engine.spy.P6SpyDriver"
          url="jdbc:p6spy:hsqldb:hsql://localhost:11002/emp_ewa" .../>
</database>

<!-- File 模式（不需要 HSQLDB 服务器） -->
<database name="emp_ewa" type="HSQLDB" connectionstring="emp_ewa" schemaname="PUBLIC">
    <pool driverClassName="com.p6spy.engine.spy.P6SpyDriver"
          url="jdbc:p6spy:hsqldb:file:hsqldb/emp_ewa" .../>
</database>
```

**URL 转换规则：** `jdbc:p6spy:hsqldb:hsql://host:port/db` → `jdbc:p6spy:hsqldb:file:hsqldb/db`

### File 模式的启动代码

File 模式下不需要启动 HSQLDB 服务器，Tomcat10EmbeddedServer 简化为只启动 Tomcat：

```java
public class Tomcat10EmbeddedServer {
    public static void main(String[] args) throws Exception {
        // 1. 解压数据文件（如果需要）
        ensureHsqldbData();

        // 2. 启动 Tomcat（HSQLDB file 模式随 Tomcat 启动自动连接）
        startTomcat();
    }
}
```

不需要 `HsqldbServerManager`，不需要 `server.start()`，不需要端口检测。

### 切换模式

在 `application.properties` 中配置：
```properties
hsqldb.mode=file         # 或 server
hsqldb.port=11002        # 仅 server 模式使用
```

## Configuration File Pattern

使用 `application.properties` + `ConfigLoader` 集中管理所有配置，避免硬编码。

### application.properties

```properties
# Tomcat 配置
tomcat.port=8080
tomcat.host=0.0.0.0

# HSQLDB 配置
hsqldb.mode=file
hsqldb.data.dir=hsqldb
hsqldb.port=11002              # 仅 server 模式使用
hsqldb.databases=emp_ewa,emp_portal

# P6Spy 日志
p6spy.enabled=true
```

### ConfigLoader.java

```java
public class ConfigLoader {
    private static Properties props = new Properties();

    static {
        try (InputStream is = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            props.load(is);
        }
    }

    public static int getTomcatPort() {
        return getInt("tomcat.port", 8080);
    }

    public static String getHsqldbMode() {
        return get("hsqldb.mode", "file");
    }

    public static String[] getHsqldbDatabases() {
        String dbs = get("hsqldb.databases", "emp_ewa,emp_portal");
        return dbs.split(",");
    }

    private static int getInt(String key, int def) { ... }
    private static String get(String key, String def) { ... }
    private static boolean getBoolean(String key, boolean def) { ... }
}
```

### 使用 ConfigLoader

在 Tomcat10EmbeddedServer 中：
```java
tomcat.setPort(ConfigLoader.getTomcatPort());
tomcat.setHostname(ConfigLoader.getTomcatHost());

String dataDir = ConfigLoader.getHsqldbDataDir();
String[] databases = ConfigLoader.getHsqldbDatabases();
```

**Why:** 硬编码的端口、路径等在多环境部署时容易出错。属性文件 + ConfigLoader 提供统一的配置入口，支持默认值，便于维护和部署。

**How to apply:** 创建新 demo 项目时，始终使用 `application.properties` + `ConfigLoader` 模式。配置项包括所有可变参数（端口、路径、数据库列表等）。Tomcat 和 HSQLDB 的配置参数从 ConfigLoader 读取而非硬编码。

## Merging HSQLDB Databases

When consolidating databases (e.g., merging emp_main_data into emp_portal), use `MergeDatabases.java`:

1. Start HSQLDB Server with both source and target databases
2. For each table in source:
   - Check if table exists in target → skip if yes
   - Create table in target using source metadata
   - Batch INSERT from source to target (1000 rows per batch)
3. Execute `SHUTDOWN COMPACT` on target database via JDBC
4. Stop server
5. Update `ewa_conf.xml`: change source database URL to point to target
6. Update `Tomcat10EmbeddedServer.DATABASES` array to remove source

```bash
# Run merge
mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.MergeDatabases"
```

**Post-merge config:** The source database name is kept in `ewa_conf.xml` but its URL points to the target database, preserving backward compatibility with XML configs that reference the old name.

## Current Database Layout (after merge)

| Database | Tables | Contents |
|----------|--------|----------|
| **emp_portal** | 48 | Portal admin (adm_*), business data (bas_*, city, sys_coin*), module config (p_ewa_mod_*), RESTful (ewa_restful*) |
| **emp_ewa** | 17 | EWA configuration (ewa_cfg*, ewa_mod*, ewa_restful*) |

**Note:** emp_main_data was merged into emp_portal. The `emp_main_data` / `main_data` connection names still work — they point to emp_portal.

## Verification

```bash
cd tomcat10demo

# Option 1: Shell scripts
bin/start.sh     # 后台启动
bin/stop.sh      # 停止
bin/debug.sh     # 前台调试

# Option 2: Maven directly
mvn exec:java -Dexec.mainClass="com.gdxsoft.emp.demo.Tomcat10EmbeddedServer"

# → http://localhost:8080/
# → http://localhost:8080/back_admin/login.jsp (后台管理)

# HSQLDB 连接（File 模式）:
jdbc:hsqldb:file:hsqldb/emp_ewa (user: sa, password: empty)
# HSQLDB 连接（Server 模式）:
jdbc:hsqldb:hsql://localhost:11002/emp_ewa (user: sa, password: empty)
```

## Systemd Service (可选)

如需将 tomcat10demo 注册为系统服务，创建 `/etc/systemd/system/tomcat10demo.service`：

```ini
[Unit]
Description=Tomcat 10 Embedded Demo
After=network.target

[Service]
Type=simple
User=admin
WorkingDirectory=/opt/tomcat10demo
ExecStart=/opt/tomcat10demo/bin/debug.sh
ExecStop=/opt/tomcat10demo/bin/stop.sh
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

```bash
# 启用
sudo systemctl start tomcat10demo
sudo systemctl enable tomcat10demo
# 查看日志
sudo journalctl -u tomcat10demo -f
```

## Back Admin JSP Pages

`back_admin/login.jsp` and `back_admin/index.jsp` provide an admin interface using EWA's HtmlControl.

### Using HtmlControl in JSP

The EWA-idiomatic way to render pages is via `HtmlControl`, which loads XML configurations from the database (`ewa_cfg` table) or classpath resources:

```jsp
<%@ page import="com.gdxsoft.easyweb.script.HtmlControl,
                com.gdxsoft.easyweb.script.RequestValue" %>
<%
    RequestValue rv = new RequestValue(request, session);
    HtmlControl ht = new HtmlControl();
    ht.init(xmlName, itemName, null, request, session, response);
    ht.setAjaxCallUrl(request.getContextPath() + "/ewa");

    // AJAX/special requests (ValidCode, etc.) — let HtmlControl write directly
    if (rv.s("ewa_ajax") != null) {
        ht.getHtml();
        return;
    }

    // Normal page request
    String pageContent = ht.getAllHtml();
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().print(pageContent);
%>
```

**Key points:**
- `ht.getAllHtml()` returns the full HTML page (with `<html>`, `<head>`, CSS/JS includes)
- `ht.getHtml()` returns just the content fragment (for AJAX calls)
- `setAjaxCallUrl()` tells EWA where to send AJAX requests (usually `/ewa`)
- XML configs are referenced by path: `/business/menu/menu.xml` with item `ADM_MENU.F.Index`

### ValidCode / Image Output Fix

When `EWA_AJAX=ValidCode` is requested, HtmlControl writes a JPEG image directly to `response.getOutputStream()`. If you use `response.getWriter()` (PrintWriter) afterward, it causes `ERR_INCOMPLETE_CHUNKED_ENCODING`.

**Fix:** For `ewa_ajax` requests, call `ht.getHtml()` and `return` immediately — do NOT touch `response.getWriter()`:
```java
if (rv.s("ewa_ajax") != null) {
    ht.getHtml();  // HtmlControl handles output (image, JSON, etc.)
    return;        // Do NOT use PrintWriter
}
```

### login.jsp Authentication

```java
ConfAdmins admins = ConfAdmins.getInstance();
ConfAdmin adm = admins.getAdm(loginId, password);  // returns ConfAdmin or null
if (adm != null) {
    session.setAttribute("EWA_ADMIN_ID", adm.getLoginId());
    session.setAttribute("EWA_ADMIN_NAME", adm.getUserName());
}
```

### JSP Pitfalls (Tomcat 10 / Jakarta EE)

1. **`out` is reserved** — JSP predefines `JspWriter out`. Never declare `PrintWriter out = response.getWriter()`. Use a different name like `pw`:
```java
java.io.PrintWriter pw = response.getWriter();  // NOT "out"
pw.print(json);
```

2. **DTCell API** — Use `toInt()` not `asInt()`:
```java
DTCell cell = tb.getCell(0, 0);
Integer v = cell.toInt();  // correct
// cell.asInt()  // WRONG — method does not exist
```

3. **ConfAdmins API** — No `checkAdmin()` or `getAdmin()` methods:
```java
ConfAdmins admins = ConfAdmins.getInstance();
ConfAdmin adm = admins.getAdm(loginId, password);  // returns ConfAdmin or null
// adm.getLoginId(), adm.getUserName()
```

4. **Import ConfAdmin explicitly** — `ConfAdmins` and `ConfAdmin` are separate classes:
```jsp
<%@ page import="com.gdxsoft.easyweb.conf.ConfAdmins,
                com.gdxsoft.easyweb.conf.ConfAdmin" %>
```

5. **JSP encoding** — Always include both `contentType` and `pageEncoding` to avoid Chinese character garbling:
```jsp
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
```
`contentType` sets the response header; `pageEncoding` tells the JSP compiler how to read the source file. Missing `pageEncoding` causes the compiler to use the platform default encoding, resulting in garbled characters for non-ASCII text.

## P6Spy SQL Logging

P6Spy intercepts all JDBC calls and logs SQL statements with execution time — no code changes needed.

### Setup

**1. Add dependency to pom.xml:**
```xml
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

**2. Create `src/main/resources/spy.properties`:**
```properties
# P6Spy configuration
modulelist=com.p6spy.engine.spy.P6SpyFactory,com.p6spy.engine.logging.P6LogFactory

# File logger
appender=com.p6spy.engine.spy.appender.FileLogger
logfile=spy.log

# Log format: time|executionTime|category|connectionId|sql
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(currentTime)|%(executionTime)ms|%(category)|connection%(connectionId)|%(sqlSingleLine)
dateformat=yyyy-MM-dd HH:mm:ss

# Slow query detection
outagedetection=true
outagedetectioninterval=2

# Exclude noisy categories
excludecategories=info,debug,result,resultset,batch
excludebinary=true
```

**3. Update `ewa_conf.xml` — change driver and URL:**
```xml
<!-- Before -->
<pool driverClassName="org.hsqldb.jdbc.JDBCDriver"
      url="jdbc:hsqldb:hsql://localhost:11002/emp_ewa" .../>

<!-- After -->
<pool driverClassName="com.p6spy.engine.spy.P6SpyDriver"
      url="jdbc:p6spy:hsqldb:hsql://localhost:11002/emp_ewa" .../>
```

The URL pattern is `jdbc:p6spy:<original-url>` — P6Spy strips its prefix and delegates to the real driver.

### Output Example

```
2026-06-29 15:22:24|1ms|statement|connection0|select 1 a from EWA_CFG where lower(xmlname)=lower('|meta-data|organization|admin.xml')
2026-06-29 15:22:24|0ms|statement|connection0|select * from EWA_CFG where lower(xmlname)=lower('|meta-data|organization|admin.xml') and lower(itemname)=lower('ADM_USER.F.Login')
```

### Monitoring

```bash
# Real-time SQL log
tail -f tomcat10demo/spy.log

# Find slow queries (>100ms)
awk -F'|' '$2+0 > 100' spy.log
```

**Why P6Spy over code-level logging:** No need to modify `DataConnection` or any emp-script source code. P6Spy works at the JDBC driver level, capturing all SQL regardless of which code path executes it. The `spy.properties` file allows tuning log format, filtering, and slow query thresholds without recompilation.

## Zip Compression for HSQLDB Data Files (替代 Git LFS)

HSQLDB 数据文件（.script + .lobs）可达 323MB+，超过 GitHub 100MB 限制。使用 zip 压缩 + 启动时自动解压替代 Git LFS。

### 压缩效果

| 项目 | 大小 |
|------|------|
| hsqldb/ 原始目录 | 323 MB |
| hsqldb-data.zip | 11 MB |
| 压缩率 | 97% |

### 创建压缩包

```bash
cd tomcat10demo
# 停止服务后压缩（排除运行时文件）
zip -r hsqldb-data.zip hsqldb/ \
  -x "hsqldb/*.lck" "hsqldb/*.log" "hsqldb/*.tmp" "hsqldb/*.tmp/*"
```

### 自动解压实现

在 `Tomcat10EmbeddedServer.java` 中添加 `ensureHsqldbData()` 方法：

```java
private static final String HSQLDB_ZIP_FILE = "hsqldb-data.zip";

private static void ensureHsqldbData() throws Exception {
    File dataDir = new File(HSQLDB_DATA_DIR);
    File zipFile = new File(HSQLDB_ZIP_FILE);
    
    // 检查关键数据文件是否存在
    boolean dataExists = true;
    for (String dbName : DATABASES) {
        if (!new File(dataDir, dbName + ".script").exists()) {
            dataExists = false;
            break;
        }
    }
    
    if (dataExists) {
        LOGGER.info("HSQLDB data files found in {}", HSQLDB_DATA_DIR);
        return;
    }
    
    if (!zipFile.exists()) {
        throw new RuntimeException("HSQLDB data zip not found: " + HSQLDB_ZIP_FILE);
    }
    
    LOGGER.info("Extracting HSQLDB data from {}...", HSQLDB_ZIP_FILE);
    dataDir.mkdirs();
    
    // 解压
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
        ZipEntry entry;
        byte[] buffer = new byte[8192];
        while ((entry = zis.getNextEntry()) != null) {
            File outFile = new File(entry.getName());
            if (entry.isDirectory()) {
                outFile.mkdirs();
            } else {
                File parent = outFile.getParentFile();
                if (parent != null) parent.mkdirs();
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zis.closeEntry();
        }
    }
    LOGGER.info("HSQLDB data extracted successfully");
}
```

在 `main()` 中，启动 HSQLDB Server 之前调用：
```java
ensureHsqldbData();
hsqldbServer = new HsqldbServerManager(...);
hsqldbServer.start();
```

### .gitignore 配置

```gitignore
# tomcat10demo 运行时文件
/tomcat10demo/spy.log
/tomcat10demo/hsqldb/*.script
/tomcat10demo/hsqldb/*.lobs
/tomcat10demo/hsqldb/*.properties
/tomcat10demo/hsqldb/*.log
/tomcat10demo/hsqldb/*.lck
/tomcat10demo/hsqldb/*.tmp
/tomcat10demo/hsqldb/*.tmp/*
!/tomcat10demo/hsqldb/.gitkeep
/tomcat10demo/bin/*.log
/tomcat10demo/bin/*.pid
/tomcat10demo/target/
/tomcat10demo/tomcat.*/
```

**关键：** 忽略解压后的数据文件，但保留 `hsqldb-data.zip` 和 `hsqldb/.gitkeep`。

### 数据更新流程

当数据库内容变更（新增表、导入数据等）后，重新打包：

```bash
cd tomcat10demo
bin/stop.sh
zip -r hsqldb-data.zip hsqldb/ -x "hsqldb/*.lck" "hsqldb/*.log" "hsqldb/*.tmp" "hsqldb/*.tmp/*"
git add hsqldb-data.zip
git commit -m "chore: update hsqldb data"
git push
```

### 优势（对比 Git LFS）

| 特性 | Git LFS | Zip 压缩 |
|------|---------|----------|
| 额外依赖 | 需要 git-lfs | 无 |
| 带宽限制 | 1GB/月（免费） | 无 |
| clone 速度 | 需要额外 pull | 一次 clone |
| 存储大小 | 370MB | 11MB |
| 自动初始化 | 无 | 启动时自动解压 |

**Why:** Git LFS 有存储和带宽限制，且其他开发者需要额外安装 git-lfs 并执行 `git lfs pull`。Zip 压缩方案更简单：11MB 的 zip 文件直接在 git 中，首次启动自动解压，无需任何额外工具。
