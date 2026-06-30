# conf.help.hsqldb

EWA 配置帮助系统的嵌入式 HSQLDB 数据库。

## 概述

本目录包含一个嵌入式的 HSQLDB 数据库，为 `ewa_conf.xml` 配置项提供描述性帮助文档（中/英文）。
该数据库在应用启动时通过类路径资源协议 (`res:`) 被加载到内存中，提供只读的配置帮助信息查询服务。

| 数据库版本 | HSQLDB 2.4.1 |
|:----------|:-------------|
| 连接池名称 | `ewaconfhelp` |
| JDBC URL   | `jdbc:hsqldb:res:/conf.help.hsqldb/data/ewaconfhelp` |
| Schema     | `PUBLIC` |
| 加载类     | `com.gdxsoft.easyweb.define.EwaConfHelpHSqlServer` |

## 目录结构

```
conf.help.hsqldb/
├── readme.md
└── data/
    ├── .gitignore              # 忽略 HSQLDB 锁文件 ewaconfhelp.lck
    ├── ewaconfhelp.properties  # 数据库元数据（版本、修改标记）
    ├── ewaconfhelp.script      # 数据库 DDL + 数据（表结构、INSERT 语句）
    ├── ewaconfhelp.lobs        # CLOB 大对象数据（中英文描述文本）
    └── ewaconfhelp.log         # HSQLDB 事务日志（自动生成）
```

> **注意**: `ewaconfhelp.tmp/` 和 `ewaconfhelp.log` 为 HSQLDB 运行时自动生成的文件。

## 数据库表结构

### EWA_CONF — 配置项主表

存储 `ewa_conf.xml` 中的所有配置节点定义。

| 列名 | 类型 | 说明 |
|:-----|:-----|:-----|
| `CONF_TAG` | VARCHAR(100) PK | 配置项名称（如 `databases`, `scriptPaths`, `smtps`, `securities`） |
| `CONF_MEMO` | CLOB | 中文说明 |
| `CONF_MEMO_EN` | CLOB | 英文说明 |
| `CONF_PARENT_TAG` | VARCHAR(100) | 上级配置项名称（用于层级组织） |
| `CONF_CDATE` | TIMESTAMP | 创建时间 |
| `CONF_MDATE` | TIMESTAMP | 修改时间 |
| `CONF_STATUS` | VARCHAR(10) | 状态（`USED` 表示启用） |
| `CONF_SINGLE` | CHAR(1) | 是否为单一容器项（`Y`=是，`N`=可多个） |

**已注册的配置项**: `securities`, `security`, `requestValuesGlobal`, `rv`, `paths`, `path`, `scriptPaths`, `scriptPath`, `remote_syncss`, `remote_syncs`, `remote_sync`, `smtps`, `from`, `to`, `smtp`, `dkim`, `requestValuesTypes`, `requestValuesType`, `initparas`, `initpara`, `workflow`, `sql`, `sqlCached`, `debug`, `cfgCacheMethod`, `databases`, `database`, `pool`, `redises`, `redis`

### EWA_CONF_PARA — 配置参数表

存储每个配置项的子参数定义。

| 列名 | 类型 | 说明 |
|:-----|:-----|:-----|
| `PARA_NAME` | VARCHAR(100) PK | 参数名称（如 `name`, `type`, `url`, `maxActive`） |
| `CONF_TAG` | VARCHAR(100) PK | 所属配置项 |
| `PARA_MEMO` | CLOB | 中文说明 |
| `PARA_MEMO_EN` | CLOB | 英文说明 |
| `PARA_MUST` | CHAR(1) | 是否必填（`Y`=必须，`N`=可选） |
| `PARA_CDATE` | TIMESTAMP | 创建时间 |
| `PARA_MDATE` | TIMESTAMP | 修改时间 |
| `PARA_STATUS` | VARCHAR(10) | 状态 |

**参数示例**:

| 配置项 | 参数 | 必填 |
|:-------|:-----|:----:|
| `database` | `name`, `type`, `connectionString`, `schemaName` | Y |
| `pool` | `username`, `password`, `driverClassName`, `url`, `maxActive`, `maxIdle`, `maxWait` | 部分 |
| `security` | `algorithm`, `key`, `iv`, `default`, `name`, `aad` | 部分 |
| `smtp` | `name`, `host`, `port`, `pwd`, `user`, `startTls`, `ssl` | 部分 |
| `scriptPath` | `name`, `path` | Y |
| `redis` | `name`, `auth`, `hosts`, `method` | Y |
| `sqlCached` | `cachedMethod`, `redisName` | Y/N |

### EWA_CONF_DICT — 配置字典表

存储参数的合法取值选项。

| 列名 | 类型 | 说明 |
|:-----|:-----|:-----|
| `PARA_NAME` | VARCHAR(100) PK | 参数名称 |
| `CONF_TAG` | VARCHAR(100) PK | 所属配置项 |
| `DICT_NAME` | VARCHAR(100) PK | 字典值 |
| `DICT_MEMO` | CLOB | 中文描述 |
| `DICT_MEMO_EN` | CLOB | 英文描述 |
| `DICT_CDATE` | TIMESTAMP | 创建时间 |
| `DICT_MDATE` | TIMESTAMP | 修改时间 |
| `DICT_STAUTS` | VARCHAR(10) | 状态 |

**字典值示例**:

| 配置项 | 参数 | 可选值 |
|:-------|:-----|:-------|
| `database` | `type` | `HSQLDB`, `MSSQL`, `MYSQL`, `ORACLE` |
| `pool` | `driverClassName` | `org.hsqldb.jdbcDriver`, `com.mysql.cj.jdbc.Driver`, `com.microsoft.sqlserver.jdbc.SQLServerDriver`, `oracle.jdbc.OracleDriver` |
| `security` | `algorithm` | `AES-128-CBC`, `AES-128-GCM`, `AES-192-CBC`, `AES-192-GCM`, `AES-256-CBC`, `AES-256-GCM` |
| `smtp` | `port` | `25`, `465` |
| `smtp` | `ssl` | `true`, `false` |
| `smtp` | `startTls` | `true`, `false` |
| `redis` | `method` | `single`, `cluster`, `sharding` |
| `scriptPath` | `path` | `resource:/user.xml/`, `jdbc: ewa-database-name` |
| `cfgCacheMethod` | `value` | `memory`, `sqlcached` |

## 加载机制

### 自动初始化

系统通过 `EwaConfHelpHSqlServer.java` 在启动时自动加载该数据库：

1. **ServletMain** / **ServletIndex** 调用 `EwaConfHelpHSqlServer.getInstance()`
2. 检查 `ewa_conf.xml` 中是否已配置名为 `ewaconfhelp` 的数据库连接
3. 若未配置，则通过 `jdbc:hsqldb:res:/conf.help.hsqldb/data/ewaconfhelp` 打开 JAR 内嵌资源
4. 注册为 `ConnectionConfig` 供整个应用使用

```java
// EwaConfHelpHSqlServer.java - 关键代码
public static final String CONN_STR = "ewaconfhelp"; // 必须小写
String url = "jdbc:hsqldb:res:/conf.help.hsqldb/data/ewaconfhelp";
```

### 外部数据库替代

若 `ewa_conf.xml` 中预先配置了 `ewaconfhelp` 数据库连接（TCP Server 模式），则跳过嵌入式加载：

```xml
<!-- 外部 HSQLDB Server 模式（开发/编辑数据时使用） -->
<database name="ewaconfhelp" type="HSQLDB" connectionString="jdbc/ewaconfhelp" schemaName="PUBLIC">
    <pool username="sa" password="" maxActive="40" maxIdle="120" maxWait="5000"
        driverClassName="org.hsqldb.jdbcDriver" url="jdbc:hsqldb:hsql://localhost:11002/ewaconfhelp">
    </pool>
</database>
```

## 配置帮助界面

该数据库由 `define.xml/ewa/ewa_conf_help.xml` 中的 EasyWebTemplate 模板驱动，通过 `DataSource="ewaconfhelp"` 查询三张表，提供可视化的配置帮助界面。

## 修改数据

如需更新配置帮助数据：

1. 启动外部 HSQLDB TCP Server 并使用 IDE 修改
2. 执行 `SHUTDOWN COMPACT` 压缩数据库
3. 将 `data/` 目录下的 `.script`、`.lobs`、`.properties` 文件更新到项目中

注意：
- `.log` / `.lck` / `.tmp` 为运行时文件，会被 `.gitignore` 忽略
- CLOB 数据存储在 `ewaconfhelp.lobs` 中，需一并更新
