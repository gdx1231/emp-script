# EWA Configuration File Reference (ewa_conf.xml)

This document details the various configuration items and usage of the EWA framework main configuration file `ewa_conf.xml`.

---

## Configuration Structure Overview

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ewa_confs>
    <!-- Configuration items -->
</ewa_confs>
```

---

## 1. Admin Configuration (`<admins>`)

Defines system administrator account information.

```xml
<admins>
    <admin createDate="2005-11-01" loginId="admin" password="" userName="SysAdmin" />
</admins>
```

| Attribute | Description |
|-----------|-------------|
| `createDate` | Creation date |
| `loginId` | Login ID |
| `password` | Password (leave empty for no password or other auth methods) |
| `userName` | User display name |

---

## 2. Definition Toggle (`<define>`)

Controls the enable status of system definition features.

```xml
<define value="true" />
```

| Attribute | Description |
|-----------|-------------|
| `value` | `true` to enable, `false` to disable |

---

## 3. Config Cache Method (`<cfgCacheMethod>`)

Defines the caching strategy for configuration items.

```xml
<cfgCacheMethod value="sqlcached"></cfgCacheMethod>
```

Valid values:
- `sqlcached` - SQL result caching

---

## 4. SQL Cache Configuration (`<sqlCached>`)

Configures caching mechanisms for SQL query results.

### Using HSQLDB Cache

```xml
<sqlCached cachedMethod="hsqldb"></sqlCached>
```

### Using Redis Cache

```xml
<sqlCached cachedMethod="redis" redisName="r0" debug="true"></sqlCached>
```

| Attribute | Description |
|-----------|-------------|
| `cachedMethod` | Cache method: `hsqldb` or `redis` |
| `redisName` | Redis configuration name (references config in `<redises>`) |
| `debug` | Whether to enable debug mode |

---

## 5. Redis Configuration (`<redises>`)

Configures Redis connection parameters.

```xml
<redises>
    <redis name="r0" method="single" auth="xxx" hosts="192.168.1.252:16379,192.168.1.252:16377"></redis>
</redises>
```

| Attribute | Description |
|-----------|-------------|
| `name` | Redis configuration name |
| `method` | Connection mode: `single` (standalone), `shared` (shared instance), `cluster` (cluster mode) |
| `auth` | Redis password |
| `hosts` | Redis addresses, format `ip:port`, separated by commas |

---

## 6. Snowflake Algorithm Configuration (`<snowflake>`)

Configures the distributed ID generator (Twitter Snowflake algorithm).

```xml
<snowflake workId="1" datacenterId="1"></snowflake>
```

| Attribute | Description |
|-----------|-------------|
| `workId` | Worker node ID (0-31) |
| `datacenterId` | Data center ID (0-31) |

---

## 7. Security Encryption Configuration (`<securities>`)

Configures encryption algorithms for encrypting/decrypting sensitive data such as cookies.

```xml
<securities>
    <security name="default" default="true" algorithm="aes-192-gcm" iv="" aad="llsdlsd912"
        key="efsd91290123p9023sdkjvjdkl293048192" />
    <security name="aes_test" algorithm="aes-256-gcm" iv="" aad="" key="jdii239482903482jsdkjf9203" />
</securities>
```

| Attribute | Description |
|-----------|-------------|
| `name` | Security configuration name |
| `default` | Whether this is the default configuration |
| `algorithm` | Encryption algorithm (e.g. `aes-192-gcm`, `aes-256-gcm`) |
| `iv` | Initialization vector |
| `aad` | Additional authenticated data |
| `key` | Encryption key |

---

## 8. Global Request Values (`<requestValuesGlobal>`)

Defines global variables that are automatically injected into `RequestValue` objects.

```xml
<requestValuesGlobal>
    <rv name="rv_ewa_style_path" value="/work/EmpScriptV2" />
    <rv name="__test__" value="Test info" />
</requestValuesGlobal>
```

| Attribute | Description |
|-----------|-------------|
| `name` | Variable name |
| `value` | Variable value |

---

## 9. Script Path Configuration (`<scriptPaths>`)

Configures lookup paths for EWA definition files.

```xml
<scriptPaths>
    <scriptPath name="ewa" path="resources:/define.xml" />
    <scriptPath name="aa" path="jdbc:ewa" readOnly="true" />
    <scriptPath name="my-project" path="d:/ewa/my-project-conf" />
</scriptPaths>
```

| Attribute | Description |
|-----------|-------------|
| `name` | Path name |
| `path` | Path address, supports the following formats:<br>- `resources:/define.xml` - classpath resource<br>- `jdbc:ewa` - load from database<br>- `d:/ewa/conf` - filesystem path |
| `readOnly` | Whether read-only (optional) |

---

## 10. Additional Resource Configuration (`<addedResources>`)

Configures extra JS/CSS resources, loadable via the parameter `ewa_added_resources=addjs0,addjs1`.

```xml
<addedResources>
    <addedResource src="/static/add_0.js" name="addjs0"></addedResource>
    <addedResource src="/static/add_1.js" name="addjs1" last="true"></addedResource>
    <addedResource src="/static/add_0.css" name="addcss0"></addedResource>
    <addedResource src="/static/default.css" name="addcss1" defaultConf="true"></addedResource>
    <addedResource src="/static/main.js" name="mainjs" defaultConf="true"></addedResource>
</addedResources>
```

| Attribute | Description |
|-----------|-------------|
| `src` | Resource path |
| `name` | Resource name (used for parameter reference) |
| `last` | Whether to load last |
| `defaultConf` | Whether this is a default configuration resource |

---

## 11. Path Configuration (`<paths>`)

Defines various file paths used by the system.

```xml
<paths>
    <path name="cached_path" value="d:/ewa/cached" />
    <path name="img_tmp_path" value="@/Users/admin/java/b2b/b2b_imgs/" />
    <path name="img_tmp_path_url" value="/b2b_imgs/" />
    <path name="group_path" value="d:/ewa/ewa_groups/" />
    <path name="cvt_office_home" value="C:\Program Files (x86)\OpenOffice 4\" />
    <path name="cvt_ImageMagick_Home" value="/usr/local/Cellar/imagemagick/7.0.8-12/bin/" />
</paths>
```

| Attribute | Description |
|-----------|-------------|
| `name` | Path name |
| `value` | Path value |
| `des` | Description (optional) |

**Common path descriptions:**

| Name | Purpose |
|------|---------|
| `cached_path` | Cache file save directory |
| `img_tmp_path` | Image thumbnail save root path |
| `img_tmp_path_url` | Image thumbnail URL path (virtual path must be configured in the web server) |
| `group_path` | Import/export directory |
| `cvt_office_home` | OpenOffice installation path (for document conversion) |
| `cvt_ImageMagick_Home` | ImageMagick executable path |

---

## 12. Debug Configuration (`<debug>`)

Configures IP whitelist and exclusion directories for debugging features.

```xml
<debug ips="127.0.0.1,::1" excludes="ewa" />
```

| Attribute | Description |
|-----------|-------------|
| `ips` | Allowed debug IP addresses, separated by commas |
| `excludes` | Excluded directories, not listed in DEFINE |

---

## 13. SMTP Mail Configuration (`<smtps>`)

Configures mail servers.

```xml
<smtps>
    <smtp name="default" default="true" ip="::1" port="25"></smtp>
</smtps>
```

| Attribute | Description |
|-----------|-------------|
| `name` | SMTP configuration name |
| `default` | Whether this is the default configuration |
| `ip` | Mail server address |
| `port` | Mail server port |

---

## 14. Database Configuration (`<databases>`)

Configures database connection pools.

```xml
<databases>
    <database name="ewaconfhelp" type="HSQLDB" connectionString="jdbc/ewaconfhelp" schemaName="PUBLIC">
        <alias name="work1" description="Alias 1"/>
        <alias name="work2" description="Alias 2" />
        <pool username="sa" password="" maxActive="40" maxIdle="120" maxWait="5000"
            driverClassName="org.hsqldb.jdbcDriver" 
            url="jdbc:hsqldb:hsql://localhost:11002/ewaconfhelp">
        </pool>
    </database>
    <database name="work" type="MYSQL" connectionString="jdbc/work" schemaName="work">
        <pool username="user" password="" maxActive="40" maxIdle="120" maxWait="5000"
            driverClassName="com.mysql.cj.jdbc.Driver"
            url="jdbc:mysql://localhost:3306/b2b?nullCatalogMeansCurrent=true&amp;serverTimezone=GMT%2B8&amp;useUnicode=true&amp;characterEncoding=utf8&amp;autoReconnect=true&amp;useSSL=false">
        </pool>
    </database>
</databases>
```

### Database Attributes

| Attribute | Description |
|-----------|-------------|
| `name` | Database name (referenced in code) |
| `type` | Database type: `MYSQL`, `HSQLDB`, `SQLSERVER`, etc. |
| `connectionString` | JNDI connection string (optional) |
| `schemaName` | Database schema name |

### Connection Pool Attributes (`<pool>`)

| Attribute | Description |
|-----------|-------------|
| `username` | Database username |
| `password` | Database password |
| `maxActive` | Maximum active connections |
| `maxIdle` | Maximum idle connections |
| `maxWait` | Maximum wait time (milliseconds) |
| `driverClassName` | JDBC driver class name |
| `url` | JDBC connection URL |

### Database Aliases (`<alias>`)

Aliases can be configured for databases for convenient use in code.

```xml
<alias name="work1" description="Alias 1"/>
```

### Reading Password from File

**Security feature**: Supports reading database passwords from external files to avoid storing sensitive information in configuration files.

**Format**: Use the `file://` prefix in the `password` attribute to specify the password file path.

```xml
<database name="work" type="MYSQL" connectionString="jdbc/work" schemaName="work">
    <pool username="user" password="file:///etc/ewa/mysql-password.txt"
          maxActive="40" maxIdle="120" maxWait="5000"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/work">
    </pool>
</database>
```

**Source implementation**: `ConnectionConfig.java:78-106`

```java
// Detect file:// prefix when parsing pool attributes
if ("password".equalsIgnoreCase(name) && value.startsWith("file://")) {
    value = this.getPasswordFromFile(value);
}

private String getPasswordFromFile(String value) {
    if (!value.startsWith("file://")) {
        return value;
    }
    String filePath = value.substring(7); // Strip "file://" prefix
    try {
        String content = UFile.readFileText(filePath);
        return content.trim();
    } catch (IOException e) {
        LOGGER.error("Read password fail from: {} {}", value, e.getLocalizedMessage());
        return e.getLocalizedMessage();
    }
}
```

**Use cases**:
- Production environment password management (prevents config file leaks)
- Containerized deployment (password file mount)
- Regular password rotation (only need to update the password file)

**Best practices**:
1. Place password files in secure directories, e.g. `/etc/ewa/` or `/var/secrets/`
2. Set file permissions to application-read-only (e.g. `chmod 600`)
3. Do not include password files in version control
4. File content should only contain the password, no other characters

---

## 15. RESTful API Configuration (`<restfuls>`)

Configures RESTful API interfaces.

### Simple Configuration (loaded from database)

```xml
<restfuls path="jdbc:ewa" cors="*"></restfuls>
```

| Attribute | Description |
|-----------|-------------|
| `path` | Configuration path: `jdbc:ewa` means loaded from database |
| `cors` | Cross-origin resource sharing config, `*` means allow all origins |

### Complete Configuration Example

```xml
<restfuls cors="*" path="/ewa-api/server/v1">
    <!-- User list -->
    <restful path="chatUsers">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <post xmlname="/ewa-apps/chat.xml" itemname="chat_user.F.NM" />
        <delete xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <patch xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
    </restful>
    
    <!-- Single user -->
    <restful path="chatUsers/{cht_usr_id}">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <put xmlname="/ewa-apps/chat.xml" itemname="chat_user.F.NM" />
        <delete xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <patch xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" 
               parameters="EWA_ACTION=OnFrameRestore" />
    </restful>
    
    <!-- Chat rooms -->
    <restful path="chatRooms">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_room.LF.M" />
        <post xmlname="/ewa-apps/chat.xml" itemname="chat_room.F.NM" />
    </restful>
    
    <!-- Nested resource: topics of a room -->
    <restful path="chatRooms/{cht_rom_id}/topics">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_topic.LF.M" />
        <post xmlname="/ewa-apps/chat.xml" itemname="chat_topic.F.NM" />
    </restful>
</restfuls>
```

### RESTful Attribute Descriptions

| Attribute | Description |
|-----------|-------------|
| `path` | API path, supports path parameters such as `{id}` |
| `xmlname` | Corresponding XML configuration file path |
| `itemname` | XML configuration item name, format `ConfigName.OperationType` |
| `parameters` | Additional parameters |

### HTTP Method Mapping

| HTTP Method | Description |
|-------------|-------------|
| `GET` | Query resources |
| `POST` | Create resources |
| `PUT` | Update resources (full replacement) |
| `PATCH` | Partial update of resources |
| `DELETE` | Delete resources |

---

## 16. Remote Sync Configuration (`<remote_syncs>`)

Syncs configuration files to remote servers.

```xml
<remote_syncs des="Sync to server" url="https://yourdomain/demo/EWA_DEFINE/cgi-bin/remoteSync/" code="">
    <remote_sync name="static" id="1" source="d:/project/static" target="/var/www/static"
        filter="js,css,htm,html,xml,txt,gif,jpg,png,jpeg,jiff" />
    <remote_sync name="documents" id="2" source="d:/project/documents" 
        target="/var/www/documents" filter="*" />
</remote_syncs>
```

| Attribute | Description |
|-----------|-------------|
| `url` | Remote sync service URL |
| `code` | Sync authorization code |

### `<remote_sync>` Attributes

| Attribute | Description |
|-----------|-------------|
| `name` | Sync configuration name |
| `id` | Sync configuration ID |
| `source` | Local source directory |
| `target` | Remote target directory |
| `filter` | File type filter, comma-separated extensions, `*` means all files |

---

## Configuration File Loading Order

1. Load `ewa_conf.xml` on system startup
2. Parse `<databases>` to establish database connection pools
3. Parse `<scriptPaths>` to load EWA definition configurations
4. Initialize cache, security encryption, and other modules
5. Register RESTful API routes

---

## Best Practices

1. **Sensitive information protection**: In production, passwords, keys, and other sensitive information should use environment variables or encrypted storage
2. **Database connection pool**: Adjust `maxActive` and `maxIdle` parameters based on actual load
3. **Caching strategy**: Redis cluster mode is recommended for production
4. **Security encryption**: Rotate encryption keys regularly and use strong encryption algorithms
5. **Path configuration**: Use relative paths or configuration management tools to manage environment-specific paths

---

## Java Invocation Methods

The following details how to use EWA configuration features in Java code.

---

### 1. Database Connection Configuration

**Configuration classes**: `ConnectionConfigs`, `ConnectionConfig`

**Purpose**: Manages database connection pools, supports multiple data sources and aliases.

#### Getting Database Connection Pool Instance

```java
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.conf.ConnectionConfig;

// Get connection pool config instance
ConnectionConfigs configs = ConnectionConfigs.instance();

// Get a specific database configuration
ConnectionConfig config = configs.get("work");

// Get a database connection
DataConnection dc = new DataConnection();
dc.setConfigName("work");  // Use configuration name or alias
```

#### Dynamically Adding Database Configuration (testing/temporary scenarios)

```java
import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

ConnectionConfigs c1 = ConnectionConfigs.instance();
String CONN_STR = "mydb";
String CONN_URL = "jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf8";

ConnectionConfig poolCfg = new ConnectionConfig();
poolCfg.setName(CONN_STR);
poolCfg.setType("MYSQL");
poolCfg.setConnectionString(CONN_STR);
poolCfg.setSchemaName("mydb");

MTableStr poolParams = new MTableStr();
poolParams.put("driverClassName", "com.mysql.cj.jdbc.Driver");
poolParams.put("url", CONN_URL);
poolParams.put("username", "root");
poolParams.put("password", "password");
poolParams.put("maxActive", 10);
poolParams.put("maxIdle", 100);

poolCfg.setPool(poolParams);
c1.put(CONN_STR, poolCfg);
```

#### Reading Password from File (security best practice)

Source location: `TestBase.java:55`

```java
import com.gdxsoft.easyweb.utils.UFile;

// Read password from external file
String password = UFile.readFileText("/etc/ewa/mysql-password.txt").trim();

poolParams.put("password", password);
```

**Security advantages**:
- Password is not stored in configuration files
- Facilitates regular password rotation
- Supports containerized deployment (password file can be mounted)
- Prevents password leakage with source code

---

### 2. Script Path Configuration

**Configuration classes**: `ConfScriptPaths`, `ConfScriptPath`

**Purpose**: Manages lookup paths for EWA definition configuration files, supporting three sources: filesystem, JAR resources, and database.

#### Getting All Script Paths

```java
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.conf.ConfScriptPath;

ConfScriptPaths sps = ConfScriptPaths.getInstance();

// Get all configuration path list
List<ConfScriptPath> lst = sps.getLst();

// Get a specific path by name
ConfScriptPath sp = sps.getScriptPath("ewa");
```

#### Determining Path Type

```java
ConfScriptPath sp = ConfScriptPaths.getInstance().getScriptPath("my-project");

// Check if it's a database configuration
if (sp.isJdbc()) {
    String jdbcName = sp.getJdbcConfigName(); // Returns "ewa"
}

// Check if it's a JAR resource
if (sp.isResources()) {
    String resourceRoot = sp.getResourcesPath(); // Returns resource root path
}

// Check if read-only
boolean readOnly = sp.isReadOnly();
```

---

### 3. Global Request Value Configuration

**Configuration classes**: `ConfRequestValuesGlobal`, `ConfNameValuePair`

**Purpose**: Defines global variables that are automatically injected into each `RequestValue` object for use by SQL and templates.

#### Automatic Injection Mechanism

When creating a `RequestValue`, global variables are automatically injected:

```java
import com.gdxsoft.easyweb.script.RequestValue;

RequestValue rv = new RequestValue();
// Global variables defined in ewa_conf.xml are now auto-injected

// Get global variable value
String stylePath = rv.getString("rv_ewa_style_path");
```

Source location: `RequestValue.java:314-320`

```java
// Global parameters in ewa_conf, available for system invocation
ConfRequestValuesGlobal.getInstance().getLst().forEach(v -> {
    _ReqValues.addValue(v.getName(), v.getValue(), PageValueTag.SYSTEM);
});
```

#### Manually Retrieving Global Variable List

```java
import com.gdxsoft.easyweb.conf.ConfRequestValuesGlobal;
import com.gdxsoft.easyweb.conf.ConfNameValuePair;

ConfRequestValuesGlobal rvg = ConfRequestValuesGlobal.getInstance();
List<ConfNameValuePair> lst = rvg.getLst();

for (ConfNameValuePair p : lst) {
    System.out.println(p.getName() + " = " + p.getValue());
}
```

---

### 4. Security Encryption Configuration

**Configuration classes**: `ConfSecurities`, `ConfSecurity`

**Purpose**: Manages encryption/decryption for cookies and sensitive data, supporting AES-GCM, DES, and other algorithms.

#### Getting the Default Cipher

```java
import com.gdxsoft.easyweb.conf.ConfSecurities;
import com.gdxsoft.easyweb.conf.ConfSecurity;
import com.gdxsoft.easyweb.utils.IUSymmetricEncyrpt;

ConfSecurities securities = ConfSecurities.getInstance();

// Get the default security configuration
ConfSecurity defaultSecurity = securities.getDefaultSecurity();

// Create a cipher instance
IUSymmetricEncyrpt encryptor = defaultSecurity.createSymmetric();
```

#### Encrypting and Decrypting Cookies

Source location: `ActionBase.java:765-809`

```java
// Check if encryption is configured
if (ConfSecurities.getInstance().getDefaultSecurity() == null) {
    throw new Exception("No default symmetric defined, in ewa_conf.xml securities->security");
}

// Create cipher
IUSymmetricEncyrpt security = ConfSecurities.getInstance().getDefaultSecurity().createSymmetric();

// Use UCookies utility class to handle cookies
UCookies us = new UCookies(security);
us.setMaxAgeSeconds(cookieAge);
```

#### Getting a Named Security Configuration

```java
ConfSecurity aesConfig = securities.getConf("aes_test");
IUSymmetricEncyrpt aesEncryptor = aesConfig.createSymmetric();
```

---

### 5. SQL Cache Configuration

**Configuration classes**: `ConfSqlCached`, `SqlCached`

**Purpose**: Caches SQL query results to reduce database load, supporting HSQLDB and Redis backends.

#### Getting Cache Instance

```java
import com.gdxsoft.easyweb.cache.SqlCached;
import com.gdxsoft.easyweb.cache.SqlCachedValue;

SqlCached cached = SqlCached.getInstance();
```

#### Caching Table Data

Source location: `DTTable.java:130-150`

```java
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;

String sql = "SELECT * FROM users WHERE status=@status";
int lifeSeconds = 3600; // Cache for 1 hour
String dataSource = "work";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("status", 1);

// Get cached table data (automatic cache handling)
DTTable tb = DTTable.getCachedTable(sql, lifeSeconds, dataSource, rv);
```

#### Manually Adding/Getting Cache

```java
// Add cache entry
String key = "my_cache_key";
String value = "cache content";
SqlCached.getInstance().add(key, value);

// Get cache entry
SqlCachedValue sc = SqlCached.getInstance().getText(key);
if (sc != null) {
    String cachedContent = sc.getValue();
}
```

---

### 6. Snowflake Algorithm Configuration

**Configuration classes**: `ConfShowflake`, `USnowflake`

**Purpose**: Generates distributed unique IDs for primary key generation, order numbers, and other scenarios.

#### Generating Unique IDs

```java
import com.gdxsoft.easyweb.utils.USnowflake;

// Get next unique ID
long id = USnowflake.nextId();
System.out.println("Generated ID: " + id);

// Get Snowflake instance
Snowflake sf = USnowflake.getInstance();
long anotherId = sf.nextId();
```

Source location: `USnowflake.java:22-36`

```java
public static synchronized Snowflake getInstance() {
    if (SF == null) {
        ConfShowflake conf = ConfShowflake.getInstance();
        long workId = DEF_WORK_ID;
        long datacenterId = DEF_DATACENTER_ID;
        if (conf != null) {
            workId = conf.getWorkId();
            datacenterId = conf.getDatacenterId();
        }
        SF = new Snowflake(workId, datacenterId);
    }
    return SF;
}
```

---

### 7. RESTful API Configuration

**Configuration classes**: `ConfRestfuls`, `ConfRestful`

**Purpose**: Manages RESTful API route mappings, supporting path parameters (e.g. `{userId}`) and database configuration.

#### Getting RESTful Configuration

Source location: `ConfRestfuls.java:174-189`

```java
import com.gdxsoft.easyweb.conf.ConfRestfuls;
import com.gdxsoft.easyweb.conf.ConfRestful;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.servlets.RestfulResult;

ConfRestfuls restfuls = ConfRestfuls.getInstance();
RequestValue rv = new RequestValue();
RestfulResult<Object> result = new RestfulResult<>();

// Get RESTful configuration
String path = "/ewa-api/server/v1/chatUsers/123";
String httpMethod = "GET";
ConfRestful conf = restfuls.getConfRestful(path, httpMethod, rv, result);

if (conf != null) {
    String xmlName = conf.getXmlName();   // Configuration file path
    String itemName = conf.getItemName(); // Configuration item name
    String method = conf.getMethod();     // HTTP method
}
```

#### Getting RESTful Configuration from Database

When configured with `path="jdbc:ewa"`, the system reads configurations from the `ewa_restful` and `ewa_restful_catalog` tables.

```java
// Check if using database configuration
if (restfuls.isJdbc()) {
    String dataSource = restfuls.getDataSource(); // Returns "ewa"
}
```

---

### 8. Configuration Path Management

**Utility class**: `UPath`

**Purpose**: Retrieves configuration paths such as system path, upload directory, and cache directory.

#### Getting Configuration Paths

```java
import com.gdxsoft.easyweb.utils.UPath;

// Get web application root directory
String realPath = UPath.getRealPath();

// Get upload directory
String uploadPath = UPath.getPATH_UPLOAD();
String uploadUrl = UPath.getPATH_UPLOAD_URL();

// Get image cache directory
String imgCachePath = UPath.getPATH_IMG_CACHE();
String imgCacheUrl = UPath.getPATH_IMG_CACHE_URL();

// Get configuration file modification time (for detecting config changes)
long propTime = UPath.getPropTime();

// Get configuration XML Document
Document cfgDoc = UPath.getCfgXmlDoc();
```

---

### 9. Data Operation Examples

**Core classes**: `DTTable`, `DataConnection`, `RequestValue`

**Purpose**: Core APIs for database query and update operations.

#### Querying Data

```java
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;

String sql = "SELECT * FROM users WHERE id=@id AND status=@status";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", 1001);
rv.addOrUpdateValue("status", "active");

// Execute query
DTTable tb = DTTable.getJdbcTable(sql, "work", rv);

if (tb.isOk()) {
    // Iterate data
    for (int i = 0; i < tb.getCount(); i++) {
        DTRow row = tb.getRow(i);
        String name = row.getCell("name").toString();
        System.out.println("Name: " + name);
    }
} else {
    System.out.println("Error: " + tb.getErrorInfo());
}
```

#### Updating Data

```java
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;

String sql = "UPDATE users SET name=@name, modified=@modified WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "New Name");
rv.addOrUpdateValue("modified", new java.util.Date());
rv.addOrUpdateValue("id", 1001);

// Execute update and close connection
int affectedRows = DataConnection.updateAndClose(sql, "work", rv);
```

---

### 10. Configuration Hot-Reload Mechanism

**Implementation principle**: All configuration classes detect `ewa_conf.xml` modification time via `PROP_TIME` and automatically reload when the file changes.

Source pattern:

```java
public static ConfXXX getInstance() {
    if (INST != null) {
        if (UPath.getPropTime() == PROP_TIME) {
            return INST; // Config unchanged, return cached instance
        }
    }
    INST = createNewXXX(); // Config changed, reload
    return INST;
}
```

---

## Configuration Class Summary Table

| Configuration Class | Corresponding XML Element | Main Purpose |
|---------------------|---------------------------|--------------|
| `ConnectionConfigs` | `<databases>` | Database connection pool management |
| `ConfScriptPaths` | `<scriptPaths>` | Configuration file path management |
| `ConfRequestValuesGlobal` | `<requestValuesGlobal>` | Global variable injection |
| `ConfSecurities` | `<securities>` | Encryption/decryption management |
| `ConfSqlCached` | `<sqlCached>` | SQL cache configuration |
| `ConfShowflake` | `<snowflake>` | Distributed ID configuration |
| `ConfRestfuls` | `<restfuls>` | RESTful API routing |
| `ConfAddedResources` | `<addedResources>` | Additional JS/CSS resources |
| `ConfAdmins` | `<admins>` | System administrator configuration |
| `ConfDefine` | `<define>` | Definition feature toggle |

---

## Related Files

- `readme.md` - Overall project description
- `AI.prompt.md` - AI-assisted development prompts
- `MYSQL.functions.md` - MySQL custom functions
