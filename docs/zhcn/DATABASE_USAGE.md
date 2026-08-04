# EWA 数据库调用方式详解

## 概述

EWA 框架提供多种数据库调用方式，主要包括：

1. **DTTable** - 查询返回表数据
2. **DataConnection** - 直接执行 SQL
3. **XML 配置** - 通过 XML 配置执行
4. **SqlCached** - 带缓存的查询
5. **事务处理** - 支持事务控制

---

## 1. 数据库连接配置

### 配置文件位置
- **ewa_conf.xml** - 主配置文件中定义数据库连接
- **system.xml/database.xml** - 系统数据库配置

### 配置示例 (ewa_conf.xml)
```xml
<databases>
    <!-- MySQL 配置 -->
    <database name="work" type="MYSQL" connectionString="jdbc/work" schemaName="work">
        <pool username="user" password="" maxActive="40" maxIdle="120" maxWait="5000"
              driverClassName="com.mysql.cj.jdbc.Driver"
              url="jdbc:mysql://localhost:3306/b2b?serverTimezone=GMT%2B8&amp;useUnicode=true&amp;characterEncoding=utf8"/>
    </database>
    
    <!-- HSQLDB 配置 -->
    <database name="ewaconfhelp" type="HSQLDB" connectionString="jdbc/ewaconfhelp" schemaName="PUBLIC">
        <pool username="sa" password="" maxActive="40" maxIdle="120" maxWait="5000"
              driverClassName="org.hsqldb.jdbcDriver"
              url="jdbc:hsqldb:hsql://localhost:11002/ewaconfhelp"/>
    </database>
</databases>
```

### 连接池管理
- **ConnectionConfigs** - 管理所有数据库连接配置
- **ConnectionConfig** - 单个数据库连接配置
- **HikariCP / Druid** - 支持的连接池实现

---

## 2. DTTable 查询方式

### 2.1 基本查询

#### 使用默认连接
```java
String sql = "SELECT * FROM users WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", 123);
DTTable tb = DTTable.getJdbcTable(sql, rv);

// 处理结果
for (int i = 0; i < tb.getCount(); i++) {
    String name = tb.getCell(i, "name").toString();
    int age = tb.getCell(i, "age").toInt();
}
```

#### 指定数据源
```java
// 方式 1: 指定数据源名称
DTTable tb = DTTable.getJdbcTable(sql, "work", rv);

// 方式 2: 使用 DataConnection
DataConnection conn = new DataConnection("work", rv);
DTTable tb = DTTable.getJdbcTable(sql, conn);
conn.close();
```

### 2.2 分页查询

```java
String sql = "SELECT * FROM orders WHERE user_id=@user_id";
String pkField = "order_id";  // 主键字段
int pageSize = 20;            // 每页记录数
int curPage = 1;              // 当前页

DTTable tb = DTTable.getJdbcTable(sql, pkField, pageSize, curPage, "work", rv);

// 获取分页信息
PageSplit pageSplit = tb.getPageSplit();
int pageCount = pageSplit.getPageCount();
int recordCount = pageSplit.getRecordCount();
```

### 2.3 缓存查询

```java
// 缓存 5 分钟 (300 秒)
int cacheSeconds = 300;
DTTable tb = DTTable.getCachedTable(sql, cacheSeconds, "work", rv);

// 缓存查询自动序列化，提高性能
```

### 2.4 DTTable 常用方法

```java
DTTable tb = DTTable.getJdbcTable(sql, rv);

// 基本属性
tb.isOk();              // 查询是否成功
tb.getErrorInfo();      // 错误信息
tb.getCount();          // 记录数
tb.getColumns();        // 列集合
tb.getRows();           // 行集合

// 获取单元格数据
tb.getCell(rowIndex, colIndex);      // 按行列索引
tb.getCell(rowIndex, "columnName");  // 按列名
tb.getCell(rowIndex, col).toString();  // 转字符串
tb.getCell(rowIndex, col).toInt();     // 转整数
tb.getCell(rowIndex, col).toDouble();  // 转浮点数
tb.getCell(rowIndex, col).toTime();    // long（时间戳毫秒）
tb.getCell(rowIndex, col).toDate();    // java.util.Date

// 数据导出
tb.toXml();             // 导出 XML
tb.toSerialize();       // 序列化
```

#### `DTTable.toJSONArray()` — 整表转 JSONArray

```java
JSONArray arr = tb.toJSONArray();
// [{"id":1, "name":"Alice"}, {"id":2, "name":"Bob"}]
```

**行为特性**：

| 特性 | 说明 |
|------|------|
| 密码列自动脱敏 | 列名包含 `PASSWORD` 或以 `_PWD` / `_PASS` / `_KEY` 结尾 → 输出 `"******"` |
| Long → String | 避免 JavaScript 精度丢失（`Number.MAX_SAFE_INTEGER` 限制） |
| byte[] 二进制 | 通过 `JsonBinaryHandle` 转为文件 URL（含图片时最多返回 50 行） |
| CLOB | 转为 String |
| 时间偏移 | 若设置了 `timeDiffMinutes`，时间值自动加减偏移 |
| 行数上限 | 普通数据 50000 行；含图片 50 行 |

#### `DTRow.toJson()` — 单行转 JSONObject

```java
JSONObject obj = tb.getRow(0).toJson();
// {"id":1, "name":"Alice", "email":"alice@example.com"}

// 指定列名大小写
JSONObject upper = tb.getRow(0).toJson("UPPER");  // {"ID":1, "NAME":"Alice"}
JSONObject lower = tb.getRow(0).toJson("LOWER");  // {"id":1, "name":"Alice"}
```

> **注意**：`DTRow.toJson()` **不做密码脱敏**（与 `toJSONArray()` 不同）。如需脱敏，使用 `toJSONArray()` 或手动处理。

#### `DTTable.toJson(rv)` — 返回 JSON 数组字符串

```java
String json = tb.toJson(rv);
// [{"id":1, "name":"Alice"},{"id":2, "name":"Bob"}]
```

**行为特性**：

| 特性 | 说明 |
|------|------|
| 返回格式 | **纯 JSON 数组字符串** `[...]`，无分页信息 |
| 字段名大小写 | 由参数 `EWA_JSON_FIELD_CASE`（upper / lower）控制；不设置保持原样 |
| 密码列脱敏 | 列名包含 `PASSWORD` 或以 `_PWD` / `_PASS` / `_KEY` 结尾 → 输出 `"******"` |
| null 值 | 输出 `null`（无引号） |
| 值转义 | 值经 `Utils.textToJscript` 转义后以字符串形式输出 |
| 二进制 | `rv.getContextPath()` 作为二进制处理内容路径 |
| 行数上限 | 含图片最多 50 条；其他最多 50000 条 |

> **注意**：`toJson(rv)` 的参数 `rv` 用于读取 `EWA_JSON_FIELD_CASE` 字段大小写配置和二进制内容路径，**不包含分页信息**。如需分页信息，使用 `PageSplit` 单独获取。

#### `DTTable.toKVJSONObject()` — 转 KV 键值对 JSONObject

```java
// 按列名
JSONObject kv = tb.toKVJSONObject("code", "name");
// {"A001":"Alice", "A002":"Bob"}

// 按列索引
JSONObject kv2 = tb.toKVJSONObject(0, 1);
```

**行为特性**：列不存在返回 `null`；value 字段为密码列（`PASSWORD` / `_PWD` / `_PASS` / `_KEY`）时脱敏为 `"******"`；value 经 `getCellValueByJson` 转换（Long→String、CLOB→String 等）。

#### `DTTable.toJSONObjectGroup()` — 按字段分组

```java
JSONObject groups = tb.toJSONObjectGroup("dept_id");
// {"D001":[{"id":1,"name":"Alice"},{"id":2,"name":"Bob"}],
//  "D002":[{"id":3,"name":"Carol"}]}
```

**行为特性**：

| 特性 | 说明 |
|------|------|
| 分组键 | 分组字段值作为 key，分组内每行转为 JSONObject 组成 JSONArray |
| 分组字段移除 | 每行的 JSON 中自动移除分组字段本身 |
| 空值分组 | 分组字段为 null/空白时归入 `"NULL"` 组 |
| 字段不存在 | 返回空 JSONObject，并输出 warn 日志 |

#### `DTTable.toCSV()` — 转 CSV 字符串

```java
String csv = tb.toCSV();
// code,name,password\r\n
// A001,Alice,******\r\n
// A002,Bob,******\r\n
```

**行为特性**：

| 特性 | 说明 |
|------|------|
| 首行标题 | 列名作为标题行 |
| CSV 转义 | 字段含逗号/换行/双引号时用双引号包围，内部 `"` → `""` |
| 密码列脱敏 | `PASSWORD` / `_PWD` / `_PASS` / `_KEY` 结尾列输出 `"******"` |
| null 处理 | null 输出空字符串；空字段输出 `""` |
| 行分隔符 | `\r\n` |

---

## 3. DataConnection 直接执行

### 3.1 查询操作

```java
DataConnection conn = new DataConnection("work", rv);

// 执行查询
boolean success = conn.executeQuery(sql);
if (success) {
    // 获取结果集
    DataResult rs = conn.getResultSet();
    DTTable tb = DTTable.returnTable(conn);
}

conn.close();
```

### 3.2 更新操作

```java
// 方式 1: 静态方法（自动关闭连接）
String sql = "UPDATE users SET name=@name WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "John");
rv.addOrUpdateValue("id", 123);

String error = DataConnection.updateAndClose(sql, "work", rv);
if (error != null) {
    System.err.println("更新失败：" + error);
}

// 方式 2: 实例方法（手动控制）
DataConnection conn = new DataConnection("work", rv);
int affectedRows = conn.executeUpdate(sql);
if (conn.getErrorMsg() != null) {
    System.err.println("错误：" + conn.getErrorMsg());
}
conn.close();
```

### 3.3 返回自增 ID

```java
// 返回 long 类型
String sql = "INSERT INTO users (name, email) VALUES (@name, @email)";
long autoId = DataConnection.insertAndReturnAutoIdLong(sql, "work", rv);

// 返回 int 类型
int autoId = DataConnection.insertAndReturnAutoIdInt(sql, "work", rv);

// 使用 DataConnection
DataConnection conn = new DataConnection("work", rv);
Object autoIdObj = conn.executeUpdateReturnAutoIncrementObject(sql);
conn.close();
```

### 3.4 批量更新

```java
// 用分号分割多条 SQL
String sqls = "INSERT INTO t1 VALUES (1); UPDATE t2 SET a=1; DELETE FROM t3 WHERE id=1";
String error = DataConnection.updateBatchAndClose(sqls, "work", rv);

// 使用 List<String>
List<String> sqlList = Arrays.asList(
    "INSERT INTO t1 VALUES (1)",
    "UPDATE t2 SET a=1",
    "DELETE FROM t3 WHERE id=1"
);
error = DataConnection.updateBatchAndClose(sqlList, "work", rv);
```

### 3.5 事务处理

```java
DataConnection conn = new DataConnection("work", rv);

try {
    conn.transBegin();  // 开始事务
    
    conn.executeUpdate(sql1);
    conn.executeUpdate(sql2);
    conn.executeUpdate(sql3);
    
    // 检查是否有错误
    if (conn.getErrorMsg() == null) {
        conn.transCommit();  // 提交事务
    } else {
        conn.transRollback();  // 回滚事务
    }
} catch (Exception e) {
    conn.transRollback();  // 异常回滚
    throw e;
} finally {
    conn.close();
}
```

### 3.6 批量更新（事务）

```java
List<String> sqls = Arrays.asList(
    "INSERT INTO t1 VALUES (1)",
    "UPDATE t2 SET a=1",
    "DELETE FROM t3 WHERE id=1"
);

// 带事务的批量更新
String error = DataConnection.updateBatchAndCloseTransaction(sqls, "work", rv);
```

### 3.7 混合 SQL 执行

```java
// 执行混合 SQL（查询、更新、存储过程）
String sqls = "SELECT * FROM users; INSERT INTO log VALUES (1); CALL proc_update()";
List<DTTable> tables = DataConnection.runMultiSqlsAndClose(sqls, "work", rv);

// 处理多个结果集
for (DTTable tb : tables) {
    // 处理每个表
}
```

### 3.8 查询统计

```java
// 获取记录数
int count = DataConnection.queryCount("users", "status='active'", "work", rv);

// 检查是否存在
boolean exists = DataConnection.queryExists("users", "id=123", "work", rv);
```

---

## 4. XML 配置方式

### 4.1 SqlSet 配置

```xml
<Action>
    <ActionSet>
        <Set Type="OnPageLoad">
            <CallSet>
                <Set CallName="loadUser" CallType="SqlSet"/>
            </CallSet>
        </Set>
    </ActionSet>
    <SqlSet>
        <Set Name="loadUser" SqlType="query">
            <Sql><![CDATA[
                SELECT * FROM users 
                WHERE id=@id 
                AND status=@status
            ]]></Sql>
        </Set>
        <Set Name="updateUser" SqlType="update">
            <Sql><![CDATA[
                UPDATE users 
                SET name=@name, email=@email 
                WHERE id=@id
            ]]></Sql>
        </Set>
    </SqlSet>
</Action>
```

### 4.2 XML 注释指令

EWA 支持在 SQL 注释中使用特殊指令：

```sql
-- ewa_table_name users          # 指定表名
-- auto MEMO_ID                  # 返回自增字段
-- EWA_IS_SELECT                 # 强制为 SELECT 查询
-- EWA_JOIN join_name, key_field # 列数据拼接为字符串
-- EWA_KV json_name, k, v        # 列数据转换为 JSONObject
-- ewa_test @condition is not null  # 条件判断
-- ewa_block_test @status = '1'     # 代码块判断
-- COMPARATIVE_CHANGES           # 比较更新前后变化
```

### 4.3 条件 SQL 块

```sql
SELECT * FROM orders
WHERE user_id=@user_id
-- ewa_test @status is not null
  AND status=@status
-- ewa_test @start_date is not null
  AND create_date >= @start_date
-- ewa_test @end_date is not null
  AND create_date <= @end_date
-- ewa_test
ORDER BY create_date DESC
```

### 4.4 EWA 内置函数

```java
// 在 SQL 中使用 EWA 函数
String sql = "SELECT ewa_func.md5(@password), ewa_func.snowflake() FROM dual";
DTTable tb = DTTable.getJdbcTable(sql, rv);
```

**可用函数**:
- `ewa_func.password_hash(@pwd)` - Argon2 加密
- `ewa_func.password_verify(@pwd, @hash)` - 密码验证
- `ewa_func.encrypt(@data)` - AES 加密
- `ewa_func.decrypt(@data)` - AES 解密
- `ewa_func.md5(@data)` - MD5 哈希
- `ewa_func.sha1(@data)` - SHA1 哈希
- `ewa_func.digestHex(@data, @algo)` - 摘要算法
- `ewa_func.snowflake()` - 雪花 ID
- `ewa_func.send_mail(...)` - 发送邮件

---

## 5. SqlUtils 工具类

### 5.1 SQL 解析

```java
// 获取表名
String tableName = SqlUtils.getTableNameBySqlComment(sql);

// 获取自增字段
String autoField = SqlUtils.getAutoField(sql);

// 检查是否为 SELECT 语句
boolean isSelect = SqlUtils.checkIsSelect(sql);

// 检查是否为存储过程
boolean isProcedure = SqlUtils.checkStartWord(sql, "CALL");

// 删除 SQL 注释
String cleanSql = SqlUtils.removeSqlMuitiComment(sql);
```

### 5.2 数据库类型判断

```java
// 判断数据库类型
boolean isMySql = SqlUtils.isMySql(databaseType);
boolean isSqlServer = SqlUtils.isSqlServer(databaseType);
boolean isOracle = SqlUtils.isOracle(databaseType);
boolean isPostgreSql = SqlUtils.isPostgreSql(databaseType);
boolean isHsqlDb = SqlUtils.isHsqlDb(databaseType);

// 从 DataConnection 判断
boolean isMySql = SqlUtils.isMySql(conn);
```

### 5.3 中文排序

```java
// 检查是否需要中文排序
boolean needChnOrder = SqlUtils.checkChnOrderByDatabase(databaseType);

// 获取中文排序模板
String template = SqlUtils.chnOrderTemplate(databaseType);
// MySQL: convert([FIELD] using gbk)
// PostgreSQL: convert_to([FIELD],'gb18030')

// 替换排序字段
String orderField = SqlUtils.replaceChnOrder(databaseType, fieldName);
```

### 5.4 @符号处理

```java
// SQL 中的@符号需要特殊处理
String sql = "SELECT email FROM users WHERE name LIKE '%@%'";
String replaced = SqlUtils.replaceSqlAtWithChar64(sql, databaseType);

// MySQL: CONCAT('email', CHAR(64), 'domain')
// SQL Server: ('email' + char(64) + 'domain')
// Oracle: ('email' || chr(64) || 'domain')
```

---

## 6. 参数处理

### 6.1 RequestValue 参数

```java
RequestValue rv = new RequestValue();

// 添加参数
rv.addOrUpdateValue("id", 123);
rv.addOrUpdateValue("name", "John");
rv.addOrUpdateValue("create_date", new Date());

// SQL 中使用 @参数名
String sql = "SELECT * FROM users WHERE id=@id AND name=@name";
DTTable tb = DTTable.getJdbcTable(sql, rv);

// 参数自动替换为 PreparedStatement 的?占位符
```

### 6.2 参数类型后缀（`getParameterByEndWithType`）

SQL 中的参数可通过 `.类型后缀` 显式指定绑定类型，框架在解析 `@param.xxx` 时自动剥离后缀、取对应值并按指定类型绑定 PreparedStatement。

| 后缀 | 示例 | 绑定类型 | PreparedStatement 方法 |
|------|------|---------|----------------------|
| `.int` | `@USER_ID.int` | INTEGER | `setInt()` |
| `.bigint` | `@ORDER_ID.bigint` | BIGINT | `setLong()` |
| `.long` | `@ORDER_ID.long` | BIGINT（`.bigint` 别名） | `setLong()` |
| `.double` | `@AMOUNT.double` | DOUBLE | `setBigDecimal()` |
| `.number` | `@PRICE.number` | NUMBER | `setBigDecimal()` |
| `.date` | `@CREATE_DATE.date` | DATE/TIMESTAMP | `setTimestamp()` |
| `.binary` | `@DATA.binary` | BINARY | `setBytes()` |
| `.bin` | `@DATA.bin` | BINARY（`.binary` 别名） | `setBytes()` |
| `.uuid` | `@OBJ_ID.uuid` | UUID | PostgreSQL/HSQLDB: `setObject(UUID)`；MySQL/Oracle: `setBytes(16字节)`；其他: `setString()` |
| `.HASH` | `@FILTER.HASH` | INTEGER（哈希值） | `setInt()` |

**使用场景**：当参数值来自 URL/表单（始终为 String），但需要按特定类型绑定时：

```sql
-- @PAGE_SIZE 来自 URL，值为字符串 "20"，加 .int 后按 INTEGER 绑定
SELECT * FROM users LIMIT @PAGE_SIZE.int

-- @AMOUNT 来自表单，加 .double 后按 BigDecimal 绑定
UPDATE products SET price = @AMOUNT.double WHERE id = @ID

-- @OBJ_ID 为 UUID 字符串，加 .uuid 后按 UUID 类型绑定（PostgreSQL 原生 UUID）
SELECT * FROM objects WHERE obj_id = @OBJ_ID.uuid
```

**解析规则**（`DataConnectionSqlBuilder.getParameterByEndWithType`）：
1. 参数名转小写后检查是否以 `.int` / `.bigint` / `.long` / `.date` / `.number` / `.double` / `.binary` / `.bin` / `.uuid` 结尾
2. 剥离后缀（如 `USER_ID.int` → `USER_ID`），从 RequestValue 中取原始值
3. 设置 `PageValue.dataType` 为对应类型，后续 `addStatementParameter` 按此类型绑定

> **Java 端显式指定类型 → SQL 中不需要后缀**
>
> `RequestValue.addOrUpdateValue(key, val, dataType, maxLength)` 会在 `PageValue` 上设置 `dataType`，`addStatementParameter` 直接使用该类型绑定，SQL 中无需加 `.xxx` 后缀：
>
> ```java
> // Java 端已指定 dataType="long"，SQL 中写 @ID 即可，不需要 @ID.long
> rv.addOrUpdateValue("id", 123, "long", 100);
>
> // Java 端已指定 dataType="date"，SQL 中写 @CREATE_DATE 即可，不需要 @CREATE_DATE.date
> rv.addOrUpdateValue("create_date", new Date(), "date", 100);
> ```
>
> **何时需要 `.xxx` 后缀**：参数值来自 URL/表单（始终为 String），且 Java 端未通过 `addOrUpdateValue(key, val, dataType, length)` 显式指定类型时，框架无法自动推断目标绑定类型，此时必须在 SQL 中加后缀。

> **强类型数据库（PostgreSQL / Oracle / HSQLDB）必须使用类型后缀**
>
> MySQL / SQL Server 属于弱类型数据库，`setString()` 传入 `"123"` 到 `INTEGER` 列时会自动做隐式类型转换，不会报错。
> 但 **PostgreSQL / Oracle / HSQLDB** 是强类型数据库，JDBC 驱动严格按 `PreparedStatement` 的绑定类型匹配列类型，类型不一致直接报错：
>
> | 场景 | MySQL / SQL Server | PostgreSQL / Oracle |
> |------|-------------------|---------------------|
> | `setString("123")` → `INTEGER` 列 | ✅ 隐式转换 | ❌ `ERROR: column is of type integer but expression is of type character` |
> | `setString("550e8400-...")` → `UUID` 列 | ✅ 隐式转换 | ❌ `ERROR: column is of type uuid but expression is of type character` |
> | `setString("2024-01-01")` → `TIMESTAMP` 列 | ✅ 隐式转换 | ❌ `ERROR: column is of type timestamp but expression is of type character` |
> | `setString("1.5")` → `NUMERIC` 列 | ✅ 隐式转换 | ❌ `ERROR: column is of type numeric but expression is of type character` |
>
> **结论**：编写需要跨数据库兼容的 SQL 时，对非 String 类型的参数**始终加上类型后缀**：
>
> ```sql
> -- ❌ 在 PG 上报错（@ID 以 String 绑定，但 user_id 是 integer 列）
> SELECT * FROM users WHERE user_id = @ID
>
> -- ✅ 所有数据库兼容
> SELECT * FROM users WHERE user_id = @ID.int
>
> -- ❌ PG 上 UUID 列报错
> SELECT * FROM objects WHERE obj_id = @OBJ_ID
>
> -- ✅ 所有数据库兼容
> SELECT * FROM objects WHERE obj_id = @OBJ_ID.uuid
> ```

### 6.3 运行时类型绑定（`addStatementParameter`）

当参数不带类型后缀时，框架根据 `PageValue` 中值的 Java 类型自动选择绑定方式：

| Java 类型 | SQL 类型 | PreparedStatement 方法 |
|----------|---------|----------------------|
| `String` | VARCHAR | `setString()` |
| `Integer` / `int` | INTEGER | `setInt()` |
| `Long` / `long` | BIGINT | `setLong()` |
| `Double` / `double` | DOUBLE | `setBigDecimal()` |
| `Number` | DOUBLE | `setBigDecimal()` |
| `Boolean` / `boolean` | BOOLEAN | `setBoolean()` |
| `java.util.Date` / `java.sql.Date` | TIMESTAMP | `setTimestamp()` |
| `byte[]` | BINARY | `setBytes()` |
| `BigDecimal` | NUMERIC | `setBigDecimal()` |
| `BigInteger` | NUMERIC | `setBigDecimal(new BigDecimal(bigInteger))` |
| `UInt64` | NUMERIC | `setBigDecimal()` |
| `UInt32` | BIGINT | `setLong()` |
| `UInt16` | INTEGER | `setInt()` |
| `UUID` | OTHER / BINARY / VARCHAR | 按数据库类型自动选择（见上表） |
| 其他 | VARCHAR | `setString()`（兜底） |

### 6.4 `_SPLIT` 参数 — 逗号分隔列表展开

参数名包含 `_SPLIT` 时，框架自动将逗号分隔的值展开为 SQL 内联列表（用于 `IN()` 子句）：

```sql
-- 如果 @CITY_SPLIT = "1,2,3"
-- 框架展开为: WHERE city_id IN (1, 2, 3)
WHERE city_id IN (@CITY_SPLIT)

-- 如果 @IDS_SPLIT = "'a','b','c'"
-- 框架展开为: WHERE id IN ('a', 'b', 'c')
WHERE id IN (@IDS_SPLIT)
```

展开逻辑在 `DataConnectionSqlBuilder` 中：按 `,` 分割值，每个元素经 `sqlParameterStringExp` 处理后用 `, ` 拼接替换原参数位置。

### 6.5 存储过程 OUT 参数类型命名

存储过程的 OUT/OUTPUT 参数通过名称中的类型标记注册 JDBC 类型：

| 名称包含 | 注册类型 |
|----------|---------|
| `_BIGINT_` / `_LONG_` | `Types.BIGINT` |
| `_TINYINT_` | `Types.TINYINT` |
| `_SMALLINT_` / `_SHORT_` | `Types.SMALLINT` |
| `_INT_` | `Types.INTEGER` |
| `_BIT_` / `_BOOL_` / `_BOOLEAN_` | `Types.BIT` |
| `_NUMBER_` / `_MONEY_` / `_DECIMAL_` / `_NUMERIC_` | `Types.DECIMAL` |
| `_DOUBLE_` / `_FLOAT_` | `Types.DOUBLE` |
| `_IMAGE_` / `_BLOB_` | `Types.BLOB` |
| `_TEXT_` / `_CLOB_` | `Types.CLOB` |
| `_BINARY_` / `_BYTE_` | `Types.VARBINARY` |
| `_DATE_` / `_DATETIME_` / `_TIMESTAMP_` | `Types.TIMESTAMP` |
| `_TIME_` | `Types.TIME` |
| 其他（默认） | `Types.VARCHAR` |

```sql
-- OUT 参数示例：名称中包含 _INT_ 和 _BIGINT_
EXEC PR_GET_STATS @user_id, @count_INT_OUT OUTPUT, @total_BIGINT_OUT OUTPUT
```

### 6.6 特殊字符处理

```java
// 日期格式自动转换
rv.addOrUpdateValue("date", new Date());
// SQL: WHERE create_date=@date

// 空值处理
rv.addOrUpdateValue("memo", null);
// SQL: WHERE memo IS NULL
```

---

## 7. 缓存机制

### 7.1 SqlCached 配置

```xml
<!-- ewa_conf.xml -->
<sqlCached cachedMethod="hsqldb"/>
<!-- 或 -->
<sqlCached cachedMethod="redis" redisName="r0"/>
```

### 7.2 缓存查询

```java
// 使用缓存（自动序列化）
int cacheSeconds = 300;
DTTable tb = DTTable.getCachedTable(sql, cacheSeconds, "work", rv);

// 缓存 Key 生成规则
String cacheKey = rv.replaceParameters(sql).toUpperCase();
```

### 7.3 HSQLDB 缓存表

```sql
-- 系统自动创建缓存表
CREATE TABLE CACHED (
    C_KEY varchar(500) NOT NULL,
    C_VALUE longblob,
    C_DATE timestamp,
    PRIMARY KEY (C_KEY)
);
```

---

## 8. 分页处理

### 8.1 PageSplit 类

```java
DataConnection conn = new DataConnection("work", rv);
conn.executeQueryPage(sql, pkField, curPage, pageSize);
DTTable tb = DTTable.returnTable(conn);

// 获取分页信息
PageSplit ps = conn.getPageSplit();
ps.getPageCurrent();     // 当前页
ps.getPageSize();        // 每页记录数
ps.getPageCount();       // 总页数
ps.getRecordCount();     // 总记录数
ps.isHasNext();          // 是否有下一页
ps.isHasPrev();          // 是否有上一页
```

### 8.2 不同数据库的分页 SQL

```java
// MySQL: LIMIT offset, pageSize
// SQL Server: TOP / OFFSET FETCH
// Oracle: ROWNUM
// PostgreSQL: LIMIT offset, pageSize
// HSQLDB: LIMIT offset, pageSize
```

---

## 9. 错误处理

### 9.1 获取错误信息

```java
DataConnection conn = new DataConnection("work", rv);
conn.executeUpdate(sql);

// 完整错误信息（包含 SQL）
String errorMsg = conn.getErrorMsg();

// 仅错误信息
String errorMsgOnly = conn.getErrorMsgOnly();

if (errorMsg != null) {
    LOGGER.error("SQL 执行失败：{}", errorMsg);
}
```

### 9.2 异常处理

```java
try {
    DTTable tb = DTTable.getJdbcTable(sql, rv);
    if (!tb.isOk()) {
        throw new Exception(tb.getErrorInfo());
    }
} catch (SQLException e) {
    LOGGER.error("数据库错误", e);
    throw e;
}
```

---

## 10. 最佳实践

### 10.1 连接管理

```java
// ✅ 推荐：使用静态方法（自动关闭）
DataConnection.updateAndClose(sql, "work", rv);

// ✅ 推荐：使用 try-with-resources
try (DataConnection conn = new DataConnection("work", rv)) {
    conn.executeUpdate(sql);
}

// ❌ 避免：忘记关闭连接
DataConnection conn = new DataConnection("work", rv);
conn.executeUpdate(sql);
// 忘记 conn.close()
```

### 10.2 参数化查询

```java
// ✅ 推荐：使用参数化查询
String sql = "SELECT * FROM users WHERE id=@id";
rv.addOrUpdateValue("id", userId);
DTTable tb = DTTable.getJdbcTable(sql, rv);

// ❌ 避免：SQL 拼接
String sql = "SELECT * FROM users WHERE id=" + userId;  // SQL 注入风险
```

### 10.3 事务使用

```java
// ✅ 推荐：明确事务边界
conn.transBegin();
try {
    // 多个更新操作
    conn.transCommit();
} catch (Exception e) {
    conn.transRollback();
    throw e;
} finally {
    conn.close();
}
```

### 10.4 批量操作

```java
// ✅ 推荐：使用批量更新
List<String> sqls = Arrays.asList(sql1, sql2, sql3);
DataConnection.updateBatchAndCloseTransaction(sqls, "work", rv);

// ❌ 避免：多次单独更新
DataConnection.updateAndClose(sql1, "work", rv);
DataConnection.updateAndClose(sql2, "work", rv);
DataConnection.updateAndClose(sql3, "work", rv);
```

### 10.5 缓存使用

```java
// ✅ 推荐：频繁查询使用缓存
DTTable tb = DTTable.getCachedTable(sql, 300, "work", rv);

// ❌ 避免：实时数据使用缓存
// 用户余额、库存等实时性要求高的数据不应缓存
```

---

## 11. 完整示例

### 11.1 用户管理 CRUD

```java
public class UserService {
    
    // 查询用户列表
    public DTTable getUserList(int page, int pageSize) {
        String sql = "SELECT * FROM users ORDER BY create_date DESC";
        RequestValue rv = new RequestValue();
        return DTTable.getJdbcTable(sql, "user_id", pageSize, page, "work", rv);
    }
    
    // 查询单个用户
    public DTTable getUserById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id=@user_id";
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("user_id", userId);
        return DTTable.getJdbcTable(sql, rv);
    }
    
    // 创建用户
    public long createUser(String name, String email) {
        String sql = "INSERT INTO users (name, email, create_date) VALUES (@name, @email, @create_date)";
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("name", name);
        rv.addOrUpdateValue("email", email);
        rv.addOrUpdateValue("create_date", new Date());
        return DataConnection.insertAndReturnAutoIdLong(sql, "work", rv);
    }
    
    // 更新用户
    public boolean updateUser(long userId, String name, String email) {
        String sql = "UPDATE users SET name=@name, email=@email WHERE user_id=@user_id";
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("user_id", userId);
        rv.addOrUpdateValue("name", name);
        rv.addOrUpdateValue("email", email);
        String error = DataConnection.updateAndClose(sql, "work", rv);
        return error == null;
    }
    
    // 删除用户（事务）
    public boolean deleteUser(long userId) {
        List<String> sqls = Arrays.asList(
            "DELETE FROM user_roles WHERE user_id=@user_id",
            "DELETE FROM users WHERE user_id=@user_id"
        );
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("user_id", userId);
        String error = DataConnection.updateBatchAndCloseTransaction(sqls, "work", rv);
        return error == null;
    }
}
```

### 11.2 订单处理（带事务）

```java
public class OrderService {
    
    public boolean createOrder(Order order) {
        DataConnection conn = new DataConnection("work", null);
        
        try {
            conn.transBegin();
            
            // 1. 创建订单
            String sql1 = "INSERT INTO orders (user_id, total, status) VALUES (@user_id, @total, 'PENDING')";
            RequestValue rv = new RequestValue();
            rv.addOrUpdateValue("user_id", order.getUserId());
            rv.addOrUpdateValue("total", order.getTotal());
            long orderId = conn.executeUpdateReturnAutoIncrementObject(sql1);
            
            // 2. 创建订单明细
            for (OrderItem item : order.getItems()) {
                String sql2 = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@order_id, @product_id, @quantity, @price)";
                rv.addOrUpdateValue("order_id", orderId);
                rv.addOrUpdateValue("product_id", item.getProductId());
                rv.addOrUpdateValue("quantity", item.getQuantity());
                rv.addOrUpdateValue("price", item.getPrice());
                conn.executeUpdate(sql2);
            }
            
            // 3. 更新库存
            for (OrderItem item : order.getItems()) {
                String sql3 = "UPDATE products SET stock = stock - @quantity WHERE product_id=@product_id";
                rv.addOrUpdateValue("product_id", item.getProductId());
                rv.addOrUpdateValue("quantity", item.getQuantity());
                conn.executeUpdate(sql3);
            }
            
            conn.transCommit();
            return true;
            
        } catch (Exception e) {
            conn.transRollback();
            LOGGER.error("创建订单失败", e);
            return false;
        } finally {
            conn.close();
        }
    }
}
```

---

## 总结

EWA 框架提供丰富的数据库调用方式：

| 方式 | 适用场景 | 优点 |
|------|---------|------|
| **DTTable** | 查询返回表数据 | 简单易用，自动关闭连接 |
| **DataConnection** | 复杂操作、事务 | 灵活控制，支持批量 |
| **XML 配置** | 页面驱动的应用 | 配置化，无需编码 |
| **SqlCached** | 频繁查询 | 性能优化 |
| **事务处理** | 多表更新 | 数据一致性 |

选择建议：
- 简单查询 → `DTTable.getJdbcTable()`
- 单表更新 → `DataConnection.updateAndClose()`
- 多表操作 → 事务 + 批量更新
- 频繁查询 → 缓存查询
- 实时数据 → 直接查询（不缓存）
