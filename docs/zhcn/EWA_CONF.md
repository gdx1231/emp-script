# EWA 配置文件说明 (ewa_conf.xml)

本文档详细说明 EWA 框架主配置文件 `ewa_conf.xml` 的各个配置项及其用法。

---

## 配置结构概览

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ewa_confs>
    <!-- 各配置项 -->
</ewa_confs>
```

---

## 1. 管理员配置 (`<admins>`)

定义系统管理员账户信息。

```xml
<admins>
    <admin createDate="2005-11-01" loginId="admin" password="" userName="SysAdmin" />
</admins>
```

| 属性 | 说明 |
|------|------|
| `createDate` | 创建日期 |
| `loginId` | 登录 ID |
| `password` | 密码（留空表示无密码或通过其他方式验证） |
| `userName` | 用户显示名称 |

---

## 2. 定义开关 (`<define>`)

控制系统定义功能的启用状态。

```xml
<define value="true" />
```

| 属性 | 说明 |
|------|------|
| `value` | `true` 启用，`false` 禁用 |

---

## 3. 配置缓存方法 (`<cfgCacheMethod>`)

定义配置项的缓存策略。

```xml
<cfgCacheMethod value="sqlcached"></cfgCacheMethod>
```

可选值：
- `sqlcached` - SQL 结果缓存

---

## 4. SQL 缓存配置 (`<sqlCached>`)

配置 SQL 查询结果的缓存机制。

### 使用 HSQLDB 缓存

```xml
<sqlCached cachedMethod="hsqldb"></sqlCached>
```

### 使用 Redis 缓存

```xml
<sqlCached cachedMethod="redis" redisName="r0" debug="true"></sqlCached>
```

| 属性 | 说明 |
|------|------|
| `cachedMethod` | 缓存方式：`hsqldb` 或 `redis` |
| `redisName` | Redis 配置名称（引用 `<redises>` 中的配置） |
| `debug` | 是否开启调试模式 |

---

## 5. Redis 配置 (`<redises>`)

配置 Redis 连接参数。

```xml
<redises>
    <redis name="r0" method="single" auth="xxx" hosts="192.168.1.252:16379,192.168.1.252:16377"></redis>
</redises>
```

| 属性 | 说明 |
|------|------|
| `name` | Redis 配置名称 |
| `method` | 连接模式：`single`（单机）、`shared`（共享）、`cluster`（集群） |
| `auth` | Redis 密码 |
| `hosts` | Redis 地址，格式 `ip:port`，多个用逗号分隔 |

---

## 6. 雪花算法配置 (`<snowflake>`)

配置分布式 ID 生成器（Twitter Snowflake 算法）。

```xml
<snowflake workId="1" datacenterId="1"></snowflake>
```

| 属性 | 说明 |
|------|------|
| `workId` | 工作节点 ID（0-31） |
| `datacenterId` | 数据中心 ID（0-31） |

---

## 7. 安全加密配置 (`<securities>`)

配置加密算法，用于 Cookie 等敏感数据的加解密。

```xml
<securities>
    <security name="default" default="true" algorithm="aes-192-gcm" iv="" aad="llsdlsd912"
        key="efsd91290123p9023sdkjvjdkl293048192" />
    <security name="aes_test" algorithm="aes-256-gcm" iv="" aad="" key="jdii239482903482jsdkjf9203" />
</securities>
```

| 属性 | 说明 |
|------|------|
| `name` | 安全配置名称 |
| `default` | 是否为默认配置 |
| `algorithm` | 加密算法（如 `aes-192-gcm`、`aes-256-gcm`） |
| `iv` | 初始化向量 |
| `aad` | 附加认证数据 |
| `key` | 加密密钥 |

---

## 8. 全局请求值 (`<requestValuesGlobal>`)

定义全局变量，会自动注入到 `RequestValue` 对象中。

```xml
<requestValuesGlobal>
    <rv name="rv_ewa_style_path" value="/work/EmpScriptV2" />
    <rv name="__test__" value="Test info" />
</requestValuesGlobal>
```

| 属性 | 说明 |
|------|------|
| `name` | 变量名 |
| `value` | 变量值 |

---

## 9. 脚本路径配置 (`<scriptPaths>`)

配置 EWA 定义文件的查找路径。

```xml
<scriptPaths>
    <scriptPath name="ewa" path="resources:/define.xml" />
    <scriptPath name="aa" path="jdbc:ewa" readOnly="true" />
    <scriptPath name="my-project" path="d:/ewa/my-project-conf" />
</scriptPaths>
```

| 属性 | 说明 |
|------|------|
| `name` | 路径名称 |
| `path` | 路径地址，支持以下格式：<br>- `resources:/define.xml` - 类路径资源<br>- `jdbc:ewa` - 从数据库加载<br>- `d:/ewa/conf` - 文件系统路径 |
| `readOnly` | 是否只读（可选） |

---

## 10. 附加资源配置 (`<addedResources>`)

配置额外的 JS/CSS 资源，可通过参数 `ewa_added_resources=addjs0,addjs1` 加载。

```xml
<addedResources>
    <addedResource src="/static/add_0.js" name="addjs0"></addedResource>
    <addedResource src="/static/add_1.js" name="addjs1" last="true"></addedResource>
    <addedResource src="/static/add_0.css" name="addcss0"></addedResource>
    <addedResource src="/static/default.css" name="addcss1" defaultConf="true"></addedResource>
    <addedResource src="/static/main.js" name="mainjs" defaultConf="true"></addedResource>
</addedResources>
```

| 属性 | 说明 |
|------|------|
| `src` | 资源路径 |
| `name` | 资源名称（用于参数引用） |
| `last` | 是否放在最后加载 |
| `defaultConf` | 是否为默认配置资源 |

---

## 11. 路径配置 (`<paths>`)

定义系统使用的各种文件路径。

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

| 属性 | 说明 |
|------|------|
| `name` | 路径名称 |
| `value` | 路径值 |
| `des` | 描述（可选） |

**常用路径说明：**

| 名称 | 用途 |
|------|------|
| `cached_path` | 缓存文件保存目录 |
| `img_tmp_path` | 图片缩略图保存根路径 |
| `img_tmp_path_url` | 图片缩略图 URL 路径（需在 Web 服务器配置虚拟路径） |
| `group_path` | 导入导出目录 |
| `cvt_office_home` | OpenOffice 安装路径（用于文档转换） |
| `cvt_ImageMagick_Home` | ImageMagick 可执行文件路径 |

---

## 12. 调试配置 (`<debug>`)

配置调试功能的 IP 白名单和排除目录。

```xml
<debug ips="127.0.0.1,::1" excludes="ewa" />
```

| 属性 | 说明 |
|------|------|
| `ips` | 允许调试的 IP 地址，多个用逗号分隔 |
| `excludes` | 排除的目录，不在 DEFINE 中列出 |

---

## 13. SMTP 邮件配置 (`<smtps>`)

配置邮件服务器。

```xml
<smtps>
    <smtp name="default" default="true" ip="::1" port="25"></smtp>
</smtps>
```

| 属性 | 说明 |
|------|------|
| `name` | SMTP 配置名称 |
| `default` | 是否为默认配置 |
| `ip` | 邮件服务器地址 |
| `port` | 邮件服务器端口 |

---

## 14. 数据库配置 (`<databases>`)

配置数据库连接池。

```xml
<databases>
    <database name="ewaconfhelp" type="HSQLDB" connectionString="jdbc/ewaconfhelp" schemaName="PUBLIC">
        <alias name="work1" description="别名 1"/>
        <alias name="work2" description="别名 2" />
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

### 数据库属性

| 属性 | 说明 |
|------|------|
| `name` | 数据库名称（代码中引用） |
| `type` | 数据库类型：`MYSQL`、`HSQLDB`、`SQLSERVER` 等 |
| `connectionString` | JNDI 连接字符串（可选） |
| `schemaName` | 数据库模式名称 |

### 连接池属性 (`<pool>`)

| 属性 | 说明 |
|------|------|
| `username` | 数据库用户名 |
| `password` | 数据库密码 |
| `maxActive` | 最大活动连接数 |
| `maxIdle` | 最大空闲连接数 |
| `maxWait` | 最大等待时间（毫秒） |
| `driverClassName` | JDBC 驱动类名 |
| `url` | JDBC 连接 URL |

### 数据库别名 (`<alias>`)

可以为数据库配置别名，便于在代码中使用。

```xml
<alias name="work1" description="别名 1"/>
```

### 密码从文件读取

**安全特性**: 支持从外部文件读取数据库密码，避免将敏感信息写入配置文件。

**格式**: 在 `password` 属性中使用 `file://` 前缀指定密码文件路径。

```xml
<database name="work" type="MYSQL" connectionString="jdbc/work" schemaName="work">
    <pool username="user" password="file:///etc/ewa/mysql-password.txt"
          maxActive="40" maxIdle="120" maxWait="5000"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/work">
    </pool>
</database>
```

**源码实现**: `ConnectionConfig.java:78-106`

```java
// 解析 pool 属性时检测 file:// 前缀
if ("password".equalsIgnoreCase(name) && value.startsWith("file://")) {
    value = this.getPasswordFromFile(value);
}

private String getPasswordFromFile(String value) {
    if (!value.startsWith("file://")) {
        return value;
    }
    String filePath = value.substring(7); // 去除 "file://" 前缀
    try {
        String content = UFile.readFileText(filePath);
        return content.trim();
    } catch (IOException e) {
        LOGGER.error("Read password fail from: {} {}", value, e.getLocalizedMessage());
        return e.getLocalizedMessage();
    }
}
```

**使用场景**:
- 生产环境密码管理（避免配置文件泄露）
- 容器化部署（密码文件挂载）
- 密码定期更换（只需更新密码文件）

**最佳实践**:
1. 密码文件放置在安全目录，如 `/etc/ewa/` 或 `/var/secrets/`
2. 设置文件权限为仅应用可读（如 `chmod 600`）
3. 密码文件不要纳入版本控制
4. 文件内容只包含密码，无需其他字符

---

## 15. RESTful API 配置 (`<restfuls>`)

配置 RESTful API 接口。

### 简单配置（从数据库加载）

```xml
<restfuls path="jdbc:ewa" cors="*"></restfuls>
```

| 属性 | 说明 |
|------|------|
| `path` | 配置路径：`jdbc:ewa` 表示从数据库加载 |
| `cors` | 跨域资源共享配置，`*` 表示允许所有来源 |

### 完整配置示例

```xml
<restfuls cors="*" path="/ewa-api/server/v1">
    <!-- 用户列表 -->
    <restful path="chatUsers">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <post xmlname="/ewa-apps/chat.xml" itemname="chat_user.F.NM" />
        <delete xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <patch xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
    </restful>
    
    <!-- 单个用户 -->
    <restful path="chatUsers/{cht_usr_id}">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <put xmlname="/ewa-apps/chat.xml" itemname="chat_user.F.NM" />
        <delete xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" />
        <patch xmlname="/ewa-apps/chat.xml" itemname="chat_user.LF.M" 
               parameters="EWA_ACTION=OnFrameRestore" />
    </restful>
    
    <!-- 聊天房间 -->
    <restful path="chatRooms">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_room.LF.M" />
        <post xmlname="/ewa-apps/chat.xml" itemname="chat_room.F.NM" />
    </restful>
    
    <!-- 嵌套资源：房间的主题 -->
    <restful path="chatRooms/{cht_rom_id}/topics">
        <get xmlname="/ewa-apps/chat.xml" itemname="chat_topic.LF.M" />
        <post xmlname="/ewa-apps/chat.xml" itemname="chat_topic.F.NM" />
    </restful>
</restfuls>
```

### RESTful 属性说明

| 属性 | 说明 |
|------|------|
| `path` | API 路径，支持路径参数如 `{id}` |
| `xmlname` | 对应的 XML 配置文件路径 |
| `itemname` | XML 配置项名称，格式 `配置名.操作类型` |
| `parameters` | 附加参数 |

### HTTP 方法映射

| HTTP 方法 | 说明 |
|-----------|------|
| `GET` | 查询资源 |
| `POST` | 创建资源 |
| `PUT` | 更新资源（完整替换） |
| `PATCH` | 部分更新资源 |
| `DELETE` | 删除资源 |

---

## 16. 远程同步配置 (`<remote_syncs>`)

配置文件同步到远程服务器。

```xml
<remote_syncs des="Sync to server" url="https://yourdomain/demo/EWA_DEFINE/cgi-bin/remoteSync/" code="">
    <remote_sync name="static" id="1" source="d:/project/static" target="/var/www/static"
        filter="js,css,htm,html,xml,txt,gif,jpg,png,jpeg,jiff" />
    <remote_sync name="documents" id="2" source="d:/project/documents" 
        target="/var/www/documents" filter="*" />
</remote_syncs>
```

| 属性 | 说明 |
|------|------|
| `url` | 远程同步服务 URL |
| `code` | 同步授权码 |

### `<remote_sync>` 属性

| 属性 | 说明 |
|------|------|
| `name` | 同步配置名称 |
| `id` | 同步配置 ID |
| `source` | 本地源目录 |
| `target` | 远程目标目录 |
| `filter` | 文件类型过滤，多个扩展名用逗号分隔，`*` 表示所有文件 |

---

## 配置文件加载顺序

1. 系统启动时加载 `ewa_conf.xml`
2. 解析 `<databases>` 建立数据库连接池
3. 解析 `<scriptPaths>` 加载 EWA 定义配置
4. 初始化缓存、安全加密等模块
5. 注册 RESTful API 路由

---

## 最佳实践

1. **敏感信息保护**：生产环境中密码、密钥等敏感信息应使用环境变量或加密存储
2. **数据库连接池**：根据实际负载调整 `maxActive`、`maxIdle` 参数
3. **缓存策略**：生产环境推荐使用 Redis 集群模式
4. **安全加密**：定期更换加密密钥，使用强加密算法
5. **路径配置**：使用相对路径或配置管理工具管理环境相关路径

---

## Java 调用方法

以下详细介绍如何在 Java 代码中使用 EWA 配置的各项功能。

---

### 1. 数据库连接配置

**配置类**: `ConnectionConfigs`, `ConnectionConfig`

**实际意义**: 管理数据库连接池，支持多数据源配置和别名。

#### 获取数据库连接池实例

```java
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.conf.ConnectionConfig;

// 获取连接池配置实例
ConnectionConfigs configs = ConnectionConfigs.instance();

// 获取指定数据库配置
ConnectionConfig config = configs.get("work");

// 获取数据库连接
DataConnection dc = new DataConnection();
dc.setConfigName("work");  // 使用配置名称或别名
```

#### 动态添加数据库配置（测试/临时场景）

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

#### 从文件读取密码（安全实践）

源码位置: `TestBase.java:55`

```java
import com.gdxsoft.easyweb.utils.UFile;

// 从外部文件读取密码
String password = UFile.readFileText("/etc/ewa/mysql-password.txt").trim();

poolParams.put("password", password);
```

**安全优势**:
- 密码不存储在配置文件中
- 便于密码定期更换
- 支持容器化部署（密码文件可挂载）
- 防止密码随代码泄露

---

### 2. 脚本路径配置

**配置类**: `ConfScriptPaths`, `ConfScriptPath`

**实际意义**: 管理 EWA 定义配置文件的查找路径，支持文件系统、JAR 资源、数据库三种来源。

#### 获取所有脚本路径

```java
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.conf.ConfScriptPath;

ConfScriptPaths sps = ConfScriptPaths.getInstance();

// 获取所有配置路径列表
List<ConfScriptPath> lst = sps.getLst();

// 根据名称获取指定路径
ConfScriptPath sp = sps.getScriptPath("ewa");
```

#### 判断路径类型

```java
ConfScriptPath sp = ConfScriptPaths.getInstance().getScriptPath("my-project");

// 判断是否为数据库配置
if (sp.isJdbc()) {
    String jdbcName = sp.getJdbcConfigName(); // 返回 "ewa"
}

// 判断是否为 JAR 资源
if (sp.isResources()) {
    String resourceRoot = sp.getResourcesPath(); // 返回资源根路径
}

// 判断是否为只读
boolean readOnly = sp.isReadOnly();
```

---

### 3. 全局请求值配置

**配置类**: `ConfRequestValuesGlobal`, `ConfNameValuePair`

**实际意义**: 定义全局变量，自动注入到每个 `RequestValue` 对象中，供 SQL 和模板使用。

#### 自动注入机制

当创建 `RequestValue` 时，全局变量自动注入：

```java
import com.gdxsoft.easyweb.script.RequestValue;

RequestValue rv = new RequestValue();
// 此时会自动注入 ewa_conf.xml 中定义的全局变量

// 获取全局变量值
String stylePath = rv.getString("rv_ewa_style_path");
```

源码位置: `RequestValue.java:314-320`

```java
// 在 ewa_conf中的全局参数,可以被系统调用
ConfRequestValuesGlobal.getInstance().getLst().forEach(v -> {
    _ReqValues.addValue(v.getName(), v.getValue(), PageValueTag.SYSTEM);
});
```

#### 手动获取全局变量列表

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

### 4. 安全加密配置

**配置类**: `ConfSecurities`, `ConfSecurity`

**实际意义**: 管理 Cookie、敏感数据的加密解密，支持 AES-GCM、DES 等算法。

#### 获取默认加密器

```java
import com.gdxsoft.easyweb.conf.ConfSecurities;
import com.gdxsoft.easyweb.conf.ConfSecurity;
import com.gdxsoft.easyweb.utils.IUSymmetricEncyrpt;

ConfSecurities securities = ConfSecurities.getInstance();

// 获取默认安全配置
ConfSecurity defaultSecurity = securities.getDefaultSecurity();

// 创建加密器实例
IUSymmetricEncyrpt encryptor = defaultSecurity.createSymmetric();
```

#### 加密和解密 Cookie

源码位置: `ActionBase.java:765-809`

```java
// 检查是否配置了加密
if (ConfSecurities.getInstance().getDefaultSecurity() == null) {
    throw new Exception("No default symmetric defined, in ewa_conf.xml securities->security");
}

// 创建加密器
IUSymmetricEncyrpt security = ConfSecurities.getInstance().getDefaultSecurity().createSymmetric();

// 使用 UCookies 工具类处理 Cookie
UCookies us = new UCookies(security);
us.setMaxAgeSeconds(cookieAge);
```

#### 根据名称获取指定安全配置

```java
ConfSecurity aesConfig = securities.getConf("aes_test");
IUSymmetricEncyrpt aesEncryptor = aesConfig.createSymmetric();
```

---

### 5. SQL 缓存配置

**配置类**: `ConfSqlCached`, `SqlCached`

**实际意义**: 缓存 SQL 查询结果，减少数据库压力，支持 HSQLDB 和 Redis 后端。

#### 获取缓存实例

```java
import com.gdxsoft.easyweb.cache.SqlCached;
import com.gdxsoft.easyweb.cache.SqlCachedValue;

SqlCached cached = SqlCached.getInstance();
```

#### 缓存表数据

源码位置: `DTTable.java:130-150`

```java
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;

String sql = "SELECT * FROM users WHERE status=@status";
int lifeSeconds = 3600; // 缓存1小时
String dataSource = "work";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("status", 1);

// 获取缓存表数据（自动处理缓存）
DTTable tb = DTTable.getCachedTable(sql, lifeSeconds, dataSource, rv);
```

#### 手动添加/获取缓存

```java
// 添加缓存
String key = "my_cache_key";
String value = "cache content";
SqlCached.getInstance().add(key, value);

// 获取缓存
SqlCachedValue sc = SqlCached.getInstance().getText(key);
if (sc != null) {
    String cachedContent = sc.getValue();
}
```

---

### 6. 雪花算法配置

**配置类**: `ConfShowflake`, `USnowflake`

**实际意义**: 生成分布式唯一 ID，用于主键生成、订单号等场景。

#### 生成唯一 ID

```java
import com.gdxsoft.easyweb.utils.USnowflake;

// 获取下一个唯一 ID
long id = USnowflake.nextId();
System.out.println("Generated ID: " + id);

// 获取雪花算法实例
Snowflake sf = USnowflake.getInstance();
long anotherId = sf.nextId();
```

源码位置: `USnowflake.java:22-36`

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

### 7. RESTful API 配置

**配置类**: `ConfRestfuls`, `ConfRestful`

**实际意义**: 管理 RESTful API 路由映射，支持路径参数（如 `{userId}`）和数据库配置。

#### 获取 RESTful 配置

源码位置: `ConfRestfuls.java:174-189`

```java
import com.gdxsoft.easyweb.conf.ConfRestfuls;
import com.gdxsoft.easyweb.conf.ConfRestful;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.servlets.RestfulResult;

ConfRestfuls restfuls = ConfRestfuls.getInstance();
RequestValue rv = new RequestValue();
RestfulResult<Object> result = new RestfulResult<>();

// 获取 RESTful 配置
String path = "/ewa-api/server/v1/chatUsers/123";
String httpMethod = "GET";
ConfRestful conf = restfuls.getConfRestful(path, httpMethod, rv, result);

if (conf != null) {
    String xmlName = conf.getXmlName();   // 配置文件路径
    String itemName = conf.getItemName(); // 配置项名称
    String method = conf.getMethod();     // HTTP 方法
}
```

#### 从数据库获取 RESTful 配置

当配置 `path="jdbc:ewa"` 时，系统会从 `ewa_restful` 和 `ewa_restful_catalog` 表读取配置。

```java
// 判断是否使用数据库配置
if (restfuls.isJdbc()) {
    String dataSource = restfuls.getDataSource(); // 返回 "ewa"
}
```

---

### 8. 配置路径管理

**工具类**: `UPath`

**实际意义**: 获取系统路径、上传目录、缓存目录等配置路径。

#### 获取配置路径

```java
import com.gdxsoft.easyweb.utils.UPath;

// 获取 Web 应用根目录
String realPath = UPath.getRealPath();

// 获取上传目录
String uploadPath = UPath.getPATH_UPLOAD();
String uploadUrl = UPath.getPATH_UPLOAD_URL();

// 获取图片缓存目录
String imgCachePath = UPath.getPATH_IMG_CACHE();
String imgCacheUrl = UPath.getPATH_IMG_CACHE_URL();

// 获取配置文件修改时间（用于检测配置变更）
long propTime = UPath.getPropTime();

// 获取配置 XML Document
Document cfgDoc = UPath.getCfgXmlDoc();
```

---

### 9. 数据操作示例

**核心类**: `DTTable`, `DataConnection`, `RequestValue`

**实际意义**: 数据库查询、更新操作的核心 API。

#### 查询数据

```java
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;

String sql = "SELECT * FROM users WHERE id=@id AND status=@status";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", 1001);
rv.addOrUpdateValue("status", "active");

// 执行查询
DTTable tb = DTTable.getJdbcTable(sql, "work", rv);

if (tb.isOk()) {
    // 遍历数据
    for (int i = 0; i < tb.getCount(); i++) {
        DTRow row = tb.getRow(i);
        String name = row.getCell("name").toString();
        System.out.println("Name: " + name);
    }
} else {
    System.out.println("Error: " + tb.getErrorInfo());
}
```

#### 更新数据

```java
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;

String sql = "UPDATE users SET name=@name, modified=@modified WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "新名称");
rv.addOrUpdateValue("modified", new java.util.Date());
rv.addOrUpdateValue("id", 1001);

// 执行更新并关闭连接
int affectedRows = DataConnection.updateAndClose(sql, "work", rv);
```

---

### 10. 配置热更新机制

**实现原理**: 所有配置类通过 `PROP_TIME` 检测 `ewa_conf.xml` 修改时间，文件变更后自动重新加载。

源码模式:

```java
public static ConfXXX getInstance() {
    if (INST != null) {
        if (UPath.getPropTime() == PROP_TIME) {
            return INST; // 配置未变更，返回缓存实例
        }
    }
    INST = createNewXXX(); // 配置已变更，重新加载
    return INST;
}
```

---

## 配置类汇总表

| 配置类 | 对应 XML 元素 | 主要用途 |
|--------|---------------|----------|
| `ConnectionConfigs` | `<databases>` | 数据库连接池管理 |
| `ConfScriptPaths` | `<scriptPaths>` | 配置文件路径管理 |
| `ConfRequestValuesGlobal` | `<requestValuesGlobal>` | 全局变量注入 |
| `ConfSecurities` | `<securities>` | 加密解密管理 |
| `ConfSqlCached` | `<sqlCached>` | SQL 缓存配置 |
| `ConfShowflake` | `<snowflake>` | 分布式 ID 配置 |
| `ConfRestfuls` | `<restfuls>` | RESTful API 路由 |
| `ConfAddedResources` | `<addedResources>` | 附加 JS/CSS 资源 |
| `ConfAdmins` | `<admins>` | 系统管理员配置 |
| `ConfDefine` | `<define>` | 定义功能开关 |

---

## 相关文件

- `readme.md` - 项目总体说明
- `AI.prompt.md` - AI 辅助开发提示
- `MYSQL.functions.md` - MySQL 自定义函数