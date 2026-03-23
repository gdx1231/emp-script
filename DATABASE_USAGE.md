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
tb.getCell(row, col).toString();     // 转字符串
tb.getCell(row, col).toInt();        // 转整数
tb.getCell(row, col).toDouble();     // 转浮点数
tb.getCell(row, col).toTime();       // 转日期

// 数据导出
tb.toJson();            // 导出 JSON
tb.toXml();             // 导出 XML
tb.toSerialize();       // 序列化
```

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

### 6.2 参数类型映射

| Java 类型 | SQL 参数类型 |
|----------|------------|
| String | VARCHAR |
| int/Integer | INTEGER |
| long/Long | BIGINT |
| double/Double | DOUBLE |
| BigDecimal | DECIMAL |
| Date/Timestamp | TIMESTAMP |
| byte[] | BLOB |

### 6.3 特殊字符处理

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
