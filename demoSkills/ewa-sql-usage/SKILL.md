---
name: ewa-sql-usage
description: "Use when: Java 代码中调用 EWA 数据库操作 — DTTable 查询、DataConnection 更新/事务/批量、参数类型后缀（.int/.bigint/.uuid 等）、_SPLIT 参数展开、ewa_split 字符串拆分为行集、存储过程 OUT 参数、SqlCached 缓存查询、分页查询、SqlUtils 工具类、强类型数据库（PG/Oracle）兼容写法。"
trigger: ewa-sql-usage, DTTable, DataConnection, getJdbcTable, updateAndClose, getParameterByEndWithType, 参数类型后缀, .int, .bigint, .uuid, _SPLIT, ewa_split, SqlCached, 事务处理, transBegin, 强类型数据库, PostgreSQL 参数, Oracle 参数类型
---

# EWA 数据库调用方式 (Java API)

EWA 框架 Java 端数据库操作的完整参考。涵盖 DTTable / DataConnection / 参数绑定 / 缓存 / 事务 / 分页。

## 框架文档参考

| 文档 | 说明 |
|------|------|
| `docs/zhcn/DATABASE_USAGE.md` | 本文档的完整版本（含连接配置、完整示例） |
| `docs/zhcn/DATABASE_TABLES.md` | 系统表结构 DDL（ewa_cfg / ewa_restful / ewa_mod 等） |

**自动触发**：当对数据库连接配置、系统表结构等概念不确定时，先 `read_file` 对应文档再操作。

---

## 速查表

| 任务 | API | 说明 |
|------|-----|------|
| 🔥 查询 | `DTTable.getJdbcTable(sql, rv)` | 默认连接 |
| 🔥 查询（指定数据源） | `DTTable.getJdbcTable(sql, "work", rv)` | 指定连接名 |
| 🔥 更新 | `DataConnection.updateAndClose(sql, "work", rv)` | 自动关闭连接 |
| 🔥 插入返回自增ID | `DataConnection.insertAndReturnAutoIdLong(sql, "work", rv)` | 返回 long |
| 🔥 批量更新 | `DataConnection.updateBatchAndClose(sqls, "work", rv)` | 分号分隔或 List |
| 🔥 事务 | `conn.transBegin()` / `transCommit()` / `transRollback()` | 手动控制 |
| 🔥 参数类型后缀 | `@PARAM.int` / `@PARAM.uuid` | 显式指定绑定类型 |
| 📋 分页查询 | `DTTable.getJdbcTable(sql, pkField, pageSize, curPage, "work", rv)` | 含 PageSplit |
| 📋 缓存查询 | `DTTable.getCachedTable(sql, 300, "work", rv)` | 缓存 300 秒 |
| 📋 查询记录数 | `DataConnection.queryCount("table", "where", "work", rv)` | 返回 int |
| 📋 检查存在 | `DataConnection.queryExists("table", "where", "work", rv)` | 返回 boolean |
| 📋 混合 SQL | `DataConnection.runMultiSqlsAndClose(sqls, "work", rv)` | 多结果集 |
| 🔧 错误信息 | `conn.getErrorMsg()` / `tb.isOk()` | 检查执行结果 |

---

## 1. DTTable 查询

### 基本查询

```java
String sql = "SELECT * FROM users WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", 123);
DTTable tb = DTTable.getJdbcTable(sql, rv);

for (int i = 0; i < tb.getCount(); i++) {
    String name = tb.getCell(i, "name").toString();
    int age = tb.getCell(i, "age").toInt();
}
```

### 指定数据源

```java
DTTable tb = DTTable.getJdbcTable(sql, "work", rv);
```

### 分页查询

```java
DTTable tb = DTTable.getJdbcTable(sql, "order_id", 20, 1, "work", rv);

PageSplit ps = tb.getPageSplit();
ps.getPageCount();    // 总页数
ps.getRecordCount();  // 总记录数
ps.isHasNext();       // 有下一页
```

### 缓存查询

```java
// 缓存 300 秒
DTTable tb = DTTable.getCachedTable(sql, 300, "work", rv);
```

### DTTable 常用方法

```java
tb.isOk();                          // 查询是否成功
tb.getErrorInfo();                  // 错误信息
tb.getCount();                      // 记录数
tb.getCell(rowIndex, "col").toString();  // 字符串
tb.getCell(rowIndex, "col").toInt();     // 整数
tb.getCell(rowIndex, "col").toDouble();  // 浮点
tb.getCell(rowIndex, "col").toTime();    // long（时间戳毫秒）
tb.getCell(rowIndex, "col").toDate();    // java.util.Date
tb.toXml();                         // 导出 XML
```

### JSON 导出

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

// 表数据
// code | name  | password
// A001 | Alice | pwd123
// 结果 → {"A001":"Alice"}
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

## 2. DataConnection 执行

### 更新（自动关闭）

```java
String error = DataConnection.updateAndClose(sql, "work", rv);
if (error != null) {
    System.err.println("更新失败：" + error);
}
```

### 返回自增 ID

```java
long autoId = DataConnection.insertAndReturnAutoIdLong(sql, "work", rv);
int autoId = DataConnection.insertAndReturnAutoIdInt(sql, "work", rv);
```

### 批量更新

```java
// 分号分隔
String error = DataConnection.updateBatchAndClose("INSERT ...; UPDATE ...; DELETE ...", "work", rv);

// List<String>
List<String> sqls = Arrays.asList(sql1, sql2, sql3);
String error = DataConnection.updateBatchAndClose(sqls, "work", rv);
```

### 事务处理

```java
DataConnection conn = new DataConnection("work", rv);
try {
    conn.transBegin();
    conn.executeUpdate(sql1);
    conn.executeUpdate(sql2);
    if (conn.getErrorMsg() == null) {
        conn.transCommit();
    } else {
        conn.transRollback();
    }
} catch (Exception e) {
    conn.transRollback();
    throw e;
} finally {
    conn.close();
}
```

### 批量更新（带事务）

```java
String error = DataConnection.updateBatchAndCloseTransaction(sqls, "work", rv);
```

### 混合 SQL（多结果集）

```java
List<DTTable> tables = DataConnection.runMultiSqlsAndClose(sqls, "work", rv);
```

### 查询统计

```java
int count = DataConnection.queryCount("users", "status='active'", "work", rv);
boolean exists = DataConnection.queryExists("users", "id=123", "work", rv);
```

---

## 3. 参数处理

### 3.1 RequestValue 参数

```java
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", 123);
rv.addOrUpdateValue("name", "John");
rv.addOrUpdateValue("create_date", new Date());

String sql = "SELECT * FROM users WHERE id=@id AND name=@name";
DTTable tb = DTTable.getJdbcTable(sql, rv);
```

### 3.2 参数类型后缀（`getParameterByEndWithType`）

SQL 中的参数可通过 `.类型后缀` 显式指定绑定类型，框架自动剥离后缀、取对应值并按指定类型绑定 PreparedStatement。

| 后缀 | 示例 | 绑定类型 | PreparedStatement 方法 |
|------|------|---------|----------------------|
| `.int` | `@USER_ID.int` | INTEGER | `setInt()` |
| `.bigint` | `@ORDER_ID.bigint` | BIGINT | `setLong()` |
| `.long` | `@ORDER_ID.long` | BIGINT（别名） | `setLong()` |
| `.double` | `@AMOUNT.double` | DOUBLE | `setBigDecimal()` |
| `.number` | `@PRICE.number` | NUMBER | `setBigDecimal()` |
| `.date` | `@CREATE_DATE.date` | TIMESTAMP | `setTimestamp()` |
| `.binary` | `@DATA.binary` | BINARY | `setBytes()` |
| `.bin` | `@DATA.bin` | BINARY（别名） | `setBytes()` |
| `.uuid` | `@OBJ_ID.uuid` | UUID | PG/HSQLDB: `setObject(UUID)`；MySQL/Oracle: `setBytes(16字节)`；其他: `setString()` |
| `.HASH` | `@FILTER.HASH` | INTEGER | `setInt()` |

**使用场景**：参数值来自 URL/表单（始终为 String），需要按特定类型绑定：

```sql
-- @PAGE_SIZE 来自 URL，值为 "20"，加 .int 后按 INTEGER 绑定
SELECT * FROM users LIMIT @PAGE_SIZE.int

-- @OBJ_ID 为 UUID 字符串，加 .uuid 后按 UUID 类型绑定
SELECT * FROM objects WHERE obj_id = @OBJ_ID.uuid
```

**解析规则**（`DataConnectionSqlBuilder.getParameterByEndWithType`）：
1. 参数名转小写后检查后缀
2. 剥离后缀（`USER_ID.int` → `USER_ID`），从 RequestValue 取原始值
3. 设置 `PageValue.dataType`，后续 `addStatementParameter` 按此类型绑定

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
> MySQL / SQL Server 弱类型，`setString("123")` → `INTEGER` 列会自动隐式转换。
> PostgreSQL / Oracle / HSQLDB 强类型，类型不一致直接报错：
>
> | 场景 | MySQL / SQL Server | PostgreSQL / Oracle |
> |------|-------------------|---------------------|
> | `setString("123")` → `INTEGER` 列 | ✅ 隐式转换 | ❌ `column is of type integer but expression is of type character` |
> | `setString("550e8400-...")` → `UUID` 列 | ✅ 隐式转换 | ❌ `column is of type uuid but expression is of type character` |
> | `setString("2024-01-01")` → `TIMESTAMP` 列 | ✅ 隐式转换 | ❌ `column is of type timestamp but expression is of type character` |
> | `setString("1.5")` → `NUMERIC` 列 | ✅ 隐式转换 | ❌ `column is of type numeric but expression is of type character` |
>
> **结论**：跨数据库兼容的 SQL，对非 String 参数**始终加类型后缀**：
>
> ```sql
> -- ❌ PG 报错    -- ✅ 兼容
> WHERE user_id = @ID        WHERE user_id = @ID.int
> WHERE obj_id = @OBJ_ID     WHERE obj_id = @OBJ_ID.uuid
> ```

### 3.3 运行时类型绑定（`addStatementParameter`）

不带类型后缀时，框架根据 Java 值类型自动选择绑定：

| Java 类型 | SQL 类型 | PreparedStatement 方法 |
|----------|---------|----------------------|
| `String` | VARCHAR | `setString()` |
| `Integer` / `int` | INTEGER | `setInt()` |
| `Long` / `long` | BIGINT | `setLong()` |
| `Double` / `double` / `Number` | DOUBLE | `setBigDecimal()` |
| `Boolean` | BOOLEAN | `setBoolean()` |
| `java.util.Date` / `java.sql.Date` | TIMESTAMP | `setTimestamp()` |
| `byte[]` | BINARY | `setBytes()` |
| `BigDecimal` | NUMERIC | `setBigDecimal()` |
| `BigInteger` | NUMERIC | `setBigDecimal(new BigDecimal(bigInteger))` |
| `UInt64` | NUMERIC | `setBigDecimal()` |
| `UInt32` | BIGINT | `setLong()` |
| `UInt16` | INTEGER | `setInt()` |
| `UUID` | OTHER/BINARY/VARCHAR | 按数据库类型自动选择 |
| 其他 | VARCHAR | `setString()`（兜底） |

### 3.4 `_SPLIT` 参数 — 逗号分隔列表展开

参数名包含 `_SPLIT` 时，自动展开为 SQL 内联列表（用于 `IN()`）：

```sql
-- @CITY_SPLIT = "1,2,3" → WHERE city_id IN (1, 2, 3)
WHERE city_id IN (@CITY_SPLIT)

-- @IDS_SPLIT = "'a','b','c'" → WHERE id IN ('a', 'b', 'c')
WHERE id IN (@IDS_SPLIT)
```

### 3.5 `ewa_split()` — 字符串拆分为行集

框架内置虚拟函数（非真实 SQL 函数），将逗号分隔的字符串拆分为带索引的行集 `(idx, col)`。框架在 SQL 执行前将 `ewa_split(...)` 替换为临时表查询。

```sql
-- 基本用法：拆分为行集
SELECT idx, col FROM ewa_split(@ids, ',')
-- @ids = "a,b,c" → (0,'a'), (1,'b'), (2,'c')

-- 用于 IN()
WHERE id IN (SELECT col FROM ewa_split(@ids, ','))

-- 多表 JOIN 按索引对齐（两组逗号分隔值逐位配对）
SELECT a.col, b.col
FROM (SELECT * FROM ewa_split(@s1, ',')) a
INNER JOIN (SELECT * FROM ewa_split(@s2, ',')) b ON a.idx = b.idx

-- 批量更新：将逗号分隔的 ID 列表关联到目标表
UPDATE TARGET_TABLE
SET status = 'DONE'
FROM TARGET_TABLE
INNER JOIN ewa_split(@IDS_BATCH, ',') i ON TARGET_TABLE.id = i.col
WHERE TARGET_TABLE.sup_id = @G_SUP_ID
```

**实现原理**：
- SQL Server：内存临时表 `#EWA_SPT_DATA_{uid}`
- MySQL / PG / 其他：物理表 `_EWA_SPT_DATA`
- 最多支持 50 个 `ewa_split` 调用

**注意事项**：

| 要点 | 说明 |
|------|------|
| 必须加别名 | `ewa_split(@ids, ',') x`，否则 SQL Server 报语法错误 |
| 注释中不要写 `ewa_split` | 框架扫描整条 SQL 查找 `ewa_split` 模式，注释中出现会被误替换导致 SQL 报错 |
| 与 `_SPLIT` 参数的区别 | `_SPLIT` 是参数展开为内联列表（适合简单 `IN()`）；`ewa_split` 是行集函数（适合 JOIN、需要索引对齐的场景） |

### 3.6 存储过程 OUT 参数类型命名

OUT/OUTPUT 参数通过名称中的类型标记注册 JDBC 类型：

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
EXEC PR_GET_STATS @user_id, @count_INT_OUT OUTPUT, @total_BIGINT_OUT OUTPUT
```

---

## 4. 缓存机制

### 配置

```xml
<!-- ewa_conf.xml -->
<sqlCached cachedMethod="hsqldb"/>
<sqlCached cachedMethod="redis" redisName="r0"/>
```

### 使用

```java
DTTable tb = DTTable.getCachedTable(sql, 300, "work", rv);
// 缓存 Key = rv.replaceParameters(sql).toUpperCase()
```

---

## 5. SqlUtils 工具类

```java
// SQL 解析
String tableName = SqlUtils.getTableNameBySqlComment(sql);
String autoField = SqlUtils.getAutoField(sql);
boolean isSelect = SqlUtils.checkIsSelect(sql);

// 数据库类型判断
boolean isMySql = SqlUtils.isMySql(conn);
boolean isPostgreSql = SqlUtils.isPostgreSql(conn);

// 中文排序
String orderField = SqlUtils.replaceChnOrder(databaseType, fieldName);
// MySQL: convert([FIELD] using gbk)
// PostgreSQL: convert_to([FIELD],'gb18030')

// @ 符号转义
String replaced = SqlUtils.replaceSqlAtWithChar64(sql, databaseType);
// MySQL: CONCAT('...', CHAR(64), '...')
// SQL Server: ('...' + char(64) + '...')
// Oracle/PG: ('...' || chr(64) || '...')
```

---

## 6. 分页处理

```java
DataConnection conn = new DataConnection("work", rv);
conn.executeQueryPage(sql, pkField, curPage, pageSize);
DTTable tb = DTTable.returnTable(conn);

PageSplit ps = conn.getPageSplit();
ps.getPageCurrent();   // 当前页
ps.getPageSize();      // 每页条数
ps.getPageCount();     // 总页数
ps.getRecordCount();   // 总记录数
```

各数据库分页 SQL：
- MySQL / PostgreSQL / HSQLDB: `LIMIT offset, pageSize`
- SQL Server: `TOP` / `OFFSET FETCH`
- Oracle: `ROWNUM`

---

## 7. 错误处理

```java
DataConnection conn = new DataConnection("work", rv);
conn.executeUpdate(sql);
String errorMsg = conn.getErrorMsg();      // 完整错误（含 SQL）
String errorMsgOnly = conn.getErrorMsgOnly(); // 仅错误信息

DTTable tb = DTTable.getJdbcTable(sql, rv);
if (!tb.isOk()) {
    throw new Exception(tb.getErrorInfo());
}
```

---

## 8. 最佳实践

| 场景 | 推荐 | 避免 |
|------|------|------|
| 连接管理 | `DataConnection.updateAndClose()` 或 try-with-resources | 忘记 `conn.close()` |
| 参数化 | `@param` + `RequestValue` | SQL 拼接（注入风险） |
| 事务 | 明确 `transBegin` / `transCommit` / `transRollback` | 隐式事务 |
| 批量 | `updateBatchAndCloseTransaction()` | 多次单独 `updateAndClose()` |
| 缓存 | 频繁查询用 `getCachedTable()` | 实时数据（余额/库存）用缓存 |
| 跨数据库 | 非 String 参数加 `.int` / `.uuid` 等后缀 | 依赖隐式类型转换 |

---

## 9. 完整示例

### CRUD

```java
// 查询列表（分页）
DTTable tb = DTTable.getJdbcTable("SELECT * FROM users ORDER BY create_date DESC",
    "user_id", 20, 1, "work", new RequestValue());

// 查询单条
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("user_id", userId);
DTTable tb = DTTable.getJdbcTable("SELECT * FROM users WHERE user_id=@user_id", rv);

// 插入
rv.addOrUpdateValue("name", name);
rv.addOrUpdateValue("email", email);
rv.addOrUpdateValue("create_date", new Date());
long id = DataConnection.insertAndReturnAutoIdLong(
    "INSERT INTO users (name, email, create_date) VALUES (@name, @email, @create_date)",
    "work", rv);

// 更新
String error = DataConnection.updateAndClose(
    "UPDATE users SET name=@name, email=@email WHERE user_id=@user_id", "work", rv);

// 删除（事务）
List<String> sqls = Arrays.asList(
    "DELETE FROM user_roles WHERE user_id=@user_id",
    "DELETE FROM users WHERE user_id=@user_id");
DataConnection.updateBatchAndCloseTransaction(sqls, "work", rv);
```

### 订单处理（带事务）

```java
DataConnection conn = new DataConnection("work", null);
try {
    conn.transBegin();

    RequestValue rv = new RequestValue();
    rv.addOrUpdateValue("user_id", order.getUserId());
    rv.addOrUpdateValue("total", order.getTotal());
    long orderId = conn.executeUpdateReturnAutoIncrementObject(
        "INSERT INTO orders (user_id, total, status) VALUES (@user_id, @total, 'PENDING')");

    for (OrderItem item : order.getItems()) {
        rv.addOrUpdateValue("order_id", orderId);
        rv.addOrUpdateValue("product_id", item.getProductId());
        rv.addOrUpdateValue("quantity", item.getQuantity());
        rv.addOrUpdateValue("price", item.getPrice());
        conn.executeUpdate(
            "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@order_id, @product_id, @quantity, @price)");
        conn.executeUpdate(
            "UPDATE products SET stock = stock - @quantity WHERE product_id=@product_id");
    }

    conn.transCommit();
} catch (Exception e) {
    conn.transRollback();
    throw e;
} finally {
    conn.close();
}
```

---

## 常见陷阱

| 陷阱 | 解决 |
|------|------|
| 忘记关闭 DataConnection | 用 `updateAndClose` 或 try-with-resources |
| SQL 拼接导致注入 | 始终用 `@param` + `RequestValue` |
| PG 上报类型不匹配 | 加类型后缀 `@ID.int` / `@ID.uuid` |
| 批量操作无事务 | 用 `updateBatchAndCloseTransaction` |
| 实时数据用缓存 | 余额/库存等不用 `getCachedTable` |
| `_SPLIT` 参数为空 | 框架展开为空，`IN()` 语法错误，需先判空 |
| `ewa_split()` 缺别名 | 必须加别名 `ewa_split(@ids, ',') x`，否则 SQL Server 报语法错误 |
| SQL 注释中出现 `ewa_split` | 框架误判为函数调用并替换，注释中避免写 `ewa_split` 文字 |
| 存储过程 OUT 参数类型不对 | 名称中加类型标记 `_INT_` / `_BIGINT_` 等 |
