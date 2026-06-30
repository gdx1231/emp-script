# EWA Database Usage Guide

## Overview

The EWA framework provides multiple database invocation methods, including:

1. **DTTable** - Query returning table data
2. **DataConnection** - Direct SQL execution
3. **XML Configuration** - Execution via XML configuration
4. **SqlCached** - Queries with caching
5. **Transaction Handling** - Transaction control support

---

## 1. Database Connection Configuration

### Configuration File Locations
- **ewa_conf.xml** - Database connections defined in the main configuration file
- **system.xml/database.xml** - System database configuration

### Configuration Example (ewa_conf.xml)
```xml
<databases>
    <!-- MySQL Configuration -->
    <database name="work" type="MYSQL" connectionString="jdbc/work" schemaName="work">
        <pool username="user" password="" maxActive="40" maxIdle="120" maxWait="5000"
              driverClassName="com.mysql.cj.jdbc.Driver"
              url="jdbc:mysql://localhost:3306/b2b?serverTimezone=GMT%2B8&amp;useUnicode=true&amp;characterEncoding=utf8"/>
    </database>
    
    <!-- HSQLDB Configuration -->
    <database name="ewaconfhelp" type="HSQLDB" connectionString="jdbc/ewaconfhelp" schemaName="PUBLIC">
        <pool username="sa" password="" maxActive="40" maxIdle="120" maxWait="5000"
              driverClassName="org.hsqldb.jdbcDriver"
              url="jdbc:hsqldb:hsql://localhost:11002/ewaconfhelp"/>
    </database>
</databases>
```

### Connection Pool Management
- **ConnectionConfigs** - Manages all database connection configurations
- **ConnectionConfig** - Single database connection configuration
- **HikariCP / Druid** - Supported connection pool implementations

---

## 2. DTTable Query Methods

### 2.1 Basic Query

#### Using Default Connection
```java
String sql = "SELECT * FROM users WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("id", 123);
DTTable tb = DTTable.getJdbcTable(sql, rv);

// Process results
for (int i = 0; i < tb.getCount(); i++) {
    String name = tb.getCell(i, "name").toString();
    int age = tb.getCell(i, "age").toInt();
}
```

#### Specifying Data Source
```java
// Method 1: Specify data source name
DTTable tb = DTTable.getJdbcTable(sql, "work", rv);

// Method 2: Use DataConnection
DataConnection conn = new DataConnection("work", rv);
DTTable tb = DTTable.getJdbcTable(sql, conn);
conn.close();
```

### 2.2 Paginated Queries

```java
String sql = "SELECT * FROM orders WHERE user_id=@user_id";
String pkField = "order_id";  // Primary key field
int pageSize = 20;            // Records per page
int curPage = 1;              // Current page

DTTable tb = DTTable.getJdbcTable(sql, pkField, pageSize, curPage, "work", rv);

// Get pagination info
PageSplit pageSplit = tb.getPageSplit();
int pageCount = pageSplit.getPageCount();
int recordCount = pageSplit.getRecordCount();
```

### 2.3 Cached Queries

```java
// Cache for 5 minutes (300 seconds)
int cacheSeconds = 300;
DTTable tb = DTTable.getCachedTable(sql, cacheSeconds, "work", rv);

// Cached queries are auto-serialized for improved performance
```

### 2.4 DTTable Common Methods

```java
DTTable tb = DTTable.getJdbcTable(sql, rv);

// Basic properties
tb.isOk();              // Whether query succeeded
tb.getErrorInfo();      // Error info
tb.getCount();          // Record count
tb.getColumns();        // Column collection
tb.getRows();           // Row collection

// Get cell data
tb.getCell(rowIndex, colIndex);      // By row/col index
tb.getCell(rowIndex, "columnName");  // By column name
tb.getCell(row, col).toString();     // Convert to string
tb.getCell(row, col).toInt();        // Convert to integer
tb.getCell(row, col).toDouble();     // Convert to double
tb.getCell(row, col).toTime();       // Convert to date

// Data export
tb.toJson();            // Export as JSON
tb.toXml();             // Export as XML
tb.toSerialize();       // Serialize
```

---

## 3. DataConnection Direct Execution

### 3.1 Query Operations

```java
DataConnection conn = new DataConnection("work", rv);

// Execute query
boolean success = conn.executeQuery(sql);
if (success) {
    // Get result set
    DataResult rs = conn.getResultSet();
    DTTable tb = DTTable.returnTable(conn);
}

conn.close();
```

### 3.2 Update Operations

```java
// Method 1: Static method (auto-closes connection)
String sql = "UPDATE users SET name=@name WHERE id=@id";
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "John");
rv.addOrUpdateValue("id", 123);

String error = DataConnection.updateAndClose(sql, "work", rv);
if (error != null) {
    System.err.println("Update failed: " + error);
}

// Method 2: Instance method (manual control)
DataConnection conn = new DataConnection("work", rv);
int affectedRows = conn.executeUpdate(sql);
if (conn.getErrorMsg() != null) {
    System.err.println("Error: " + conn.getErrorMsg());
}
conn.close();
```

### 3.3 Return Auto-Increment ID

```java
// Return long type
String sql = "INSERT INTO users (name, email) VALUES (@name, @email)";
long autoId = DataConnection.insertAndReturnAutoIdLong(sql, "work", rv);

// Return int type
int autoId = DataConnection.insertAndReturnAutoIdInt(sql, "work", rv);

// Using DataConnection
DataConnection conn = new DataConnection("work", rv);
Object autoIdObj = conn.executeUpdateReturnAutoIncrementObject(sql);
conn.close();
```

### 3.4 Batch Updates

```java
// Semicolon-separated SQL statements
String sqls = "INSERT INTO t1 VALUES (1); UPDATE t2 SET a=1; DELETE FROM t3 WHERE id=1";
String error = DataConnection.updateBatchAndClose(sqls, "work", rv);

// Using List<String>
List<String> sqlList = Arrays.asList(
    "INSERT INTO t1 VALUES (1)",
    "UPDATE t2 SET a=1",
    "DELETE FROM t3 WHERE id=1"
);
error = DataConnection.updateBatchAndClose(sqlList, "work", rv);
```

### 3.5 Transaction Handling

```java
DataConnection conn = new DataConnection("work", rv);

try {
    conn.transBegin();  // Begin transaction
    
    conn.executeUpdate(sql1);
    conn.executeUpdate(sql2);
    conn.executeUpdate(sql3);
    
    // Check for errors
    if (conn.getErrorMsg() == null) {
        conn.transCommit();  // Commit transaction
    } else {
        conn.transRollback();  // Rollback transaction
    }
} catch (Exception e) {
    conn.transRollback();  // Rollback on exception
    throw e;
} finally {
    conn.close();
}
```

### 3.6 Batch Update (Transactional)

```java
List<String> sqls = Arrays.asList(
    "INSERT INTO t1 VALUES (1)",
    "UPDATE t2 SET a=1",
    "DELETE FROM t3 WHERE id=1"
);

// Batch update with transaction
String error = DataConnection.updateBatchAndCloseTransaction(sqls, "work", rv);
```

### 3.7 Mixed SQL Execution

```java
// Execute mixed SQL (queries, updates, stored procedures)
String sqls = "SELECT * FROM users; INSERT INTO log VALUES (1); CALL proc_update()";
List<DTTable> tables = DataConnection.runMultiSqlsAndClose(sqls, "work", rv);

// Process multiple result sets
for (DTTable tb : tables) {
    // Process each table
}
```

### 3.8 Query Statistics

```java
// Get record count
int count = DataConnection.queryCount("users", "status='active'", "work", rv);

// Check existence
boolean exists = DataConnection.queryExists("users", "id=123", "work", rv);
```

---

## 4. XML Configuration Approach

### 4.1 SqlSet Configuration

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

### 4.2 XML Comment Directives

EWA supports special directives in SQL comments:

```sql
-- ewa_table_name users          # Specify table name
-- auto MEMO_ID                  # Return auto-increment field
-- EWA_IS_SELECT                 # Force as SELECT query
-- EWA_JOIN join_name, key_field # Concatenate column data into string
-- EWA_KV json_name, k, v        # Convert column data to JSONObject
-- ewa_test @condition is not null  # Conditional check
-- ewa_block_test @status = '1'     # Code block conditional
-- COMPARATIVE_CHANGES           # Compare before/after update changes
```

### 4.3 Conditional SQL Blocks

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

### 4.4 EWA Built-in Functions

```java
// Use EWA functions in SQL
String sql = "SELECT ewa_func.md5(@password), ewa_func.snowflake() FROM dual";
DTTable tb = DTTable.getJdbcTable(sql, rv);
```

**Available functions**:
- `ewa_func.password_hash(@pwd)` - Argon2 encryption
- `ewa_func.password_verify(@pwd, @hash)` - Password verification
- `ewa_func.encrypt(@data)` - AES encryption
- `ewa_func.decrypt(@data)` - AES decryption
- `ewa_func.md5(@data)` - MD5 hash
- `ewa_func.sha1(@data)` - SHA1 hash
- `ewa_func.digestHex(@data, @algo)` - Digest algorithm
- `ewa_func.snowflake()` - Snowflake ID
- `ewa_func.send_mail(...)` - Send email

---

## 5. SqlUtils Utility Class

### 5.1 SQL Parsing

```java
// Get table name
String tableName = SqlUtils.getTableNameBySqlComment(sql);

// Get auto-increment field
String autoField = SqlUtils.getAutoField(sql);

// Check if SELECT statement
boolean isSelect = SqlUtils.checkIsSelect(sql);

// Check if stored procedure
boolean isProcedure = SqlUtils.checkStartWord(sql, "CALL");

// Remove SQL comments
String cleanSql = SqlUtils.removeSqlMuitiComment(sql);
```

### 5.2 Database Type Detection

```java
// Determine database type
boolean isMySql = SqlUtils.isMySql(databaseType);
boolean isSqlServer = SqlUtils.isSqlServer(databaseType);
boolean isOracle = SqlUtils.isOracle(databaseType);
boolean isPostgreSql = SqlUtils.isPostgreSql(databaseType);
boolean isHsqlDb = SqlUtils.isHsqlDb(databaseType);

// Determine from DataConnection
boolean isMySql = SqlUtils.isMySql(conn);
```

### 5.3 Chinese Collation

```java
// Check if Chinese collation is needed
boolean needChnOrder = SqlUtils.checkChnOrderByDatabase(databaseType);

// Get Chinese collation template
String template = SqlUtils.chnOrderTemplate(databaseType);
// MySQL: convert([FIELD] using gbk)
// PostgreSQL: convert_to([FIELD],'gb18030')

// Replace sort field
String orderField = SqlUtils.replaceChnOrder(databaseType, fieldName);
```

### 5.4 @ Symbol Handling

```java
// The @ symbol in SQL needs special handling
String sql = "SELECT email FROM users WHERE name LIKE '%@%'";
String replaced = SqlUtils.replaceSqlAtWithChar64(sql, databaseType);

// MySQL: CONCAT('email', CHAR(64), 'domain')
// SQL Server: ('email' + char(64) + 'domain')
// Oracle: ('email' || chr(64) || 'domain')
```

---

## 6. Parameter Handling

### 6.1 RequestValue Parameters

```java
RequestValue rv = new RequestValue();

// Add parameters
rv.addOrUpdateValue("id", 123);
rv.addOrUpdateValue("name", "John");
rv.addOrUpdateValue("create_date", new Date());

// Use @parameter_name in SQL
String sql = "SELECT * FROM users WHERE id=@id AND name=@name";
DTTable tb = DTTable.getJdbcTable(sql, rv);

// Parameters are automatically replaced with PreparedStatement ? placeholders
```

### 6.2 Parameter Type Mapping

| Java Type | SQL Parameter Type |
|-----------|--------------------|
| String | VARCHAR |
| int/Integer | INTEGER |
| long/Long | BIGINT |
| double/Double | DOUBLE |
| BigDecimal | DECIMAL |
| Date/Timestamp | TIMESTAMP |
| byte[] | BLOB |

### 6.3 Special Character Handling

```java
// Automatic date format conversion
rv.addOrUpdateValue("date", new Date());
// SQL: WHERE create_date=@date

// Null value handling
rv.addOrUpdateValue("memo", null);
// SQL: WHERE memo IS NULL
```

---

## 7. Caching Mechanisms

### 7.1 SqlCached Configuration

```xml
<!-- ewa_conf.xml -->
<sqlCached cachedMethod="hsqldb"/>
<!-- or -->
<sqlCached cachedMethod="redis" redisName="r0"/>
```

### 7.2 Cached Queries

```java
// Use cache (auto-serialized)
int cacheSeconds = 300;
DTTable tb = DTTable.getCachedTable(sql, cacheSeconds, "work", rv);

// Cache key generation rule
String cacheKey = rv.replaceParameters(sql).toUpperCase();
```

### 7.3 HSQLDB Cache Table

```sql
-- System auto-creates the cache table
CREATE TABLE CACHED (
    C_KEY varchar(500) NOT NULL,
    C_VALUE longblob,
    C_DATE timestamp,
    PRIMARY KEY (C_KEY)
);
```

---

## 8. Pagination Handling

### 8.1 PageSplit Class

```java
DataConnection conn = new DataConnection("work", rv);
conn.executeQueryPage(sql, pkField, curPage, pageSize);
DTTable tb = DTTable.returnTable(conn);

// Get pagination info
PageSplit ps = conn.getPageSplit();
ps.getPageCurrent();     // Current page
ps.getPageSize();        // Records per page
ps.getPageCount();       // Total pages
ps.getRecordCount();     // Total records
ps.isHasNext();          // Whether there is a next page
ps.isHasPrev();          // Whether there is a previous page
```

### 8.2 Database-Specific Pagination SQL

```java
// MySQL: LIMIT offset, pageSize
// SQL Server: TOP / OFFSET FETCH
// Oracle: ROWNUM
// PostgreSQL: LIMIT offset, pageSize
// HSQLDB: LIMIT offset, pageSize
```

---

## 9. Error Handling

### 9.1 Getting Error Information

```java
DataConnection conn = new DataConnection("work", rv);
conn.executeUpdate(sql);

// Full error info (includes SQL)
String errorMsg = conn.getErrorMsg();

// Error message only
String errorMsgOnly = conn.getErrorMsgOnly();

if (errorMsg != null) {
    LOGGER.error("SQL execution failed: {}", errorMsg);
}
```

### 9.2 Exception Handling

```java
try {
    DTTable tb = DTTable.getJdbcTable(sql, rv);
    if (!tb.isOk()) {
        throw new Exception(tb.getErrorInfo());
    }
} catch (SQLException e) {
    LOGGER.error("Database error", e);
    throw e;
}
```

---

## 10. Best Practices

### 10.1 Connection Management

```java
// ✅ Recommended: Use static methods (auto-close)
DataConnection.updateAndClose(sql, "work", rv);

// ✅ Recommended: Use try-with-resources
try (DataConnection conn = new DataConnection("work", rv)) {
    conn.executeUpdate(sql);
}

// ❌ Avoid: Forgetting to close connections
DataConnection conn = new DataConnection("work", rv);
conn.executeUpdate(sql);
// Missing conn.close()
```

### 10.2 Parameterized Queries

```java
// ✅ Recommended: Use parameterized queries
String sql = "SELECT * FROM users WHERE id=@id";
rv.addOrUpdateValue("id", userId);
DTTable tb = DTTable.getJdbcTable(sql, rv);

// ❌ Avoid: SQL concatenation
String sql = "SELECT * FROM users WHERE id=" + userId;  // SQL injection risk
```

### 10.3 Transaction Usage

```java
// ✅ Recommended: Clear transaction boundaries
conn.transBegin();
try {
    // Multiple update operations
    conn.transCommit();
} catch (Exception e) {
    conn.transRollback();
    throw e;
} finally {
    conn.close();
}
```

### 10.4 Batch Operations

```java
// ✅ Recommended: Use batch updates
List<String> sqls = Arrays.asList(sql1, sql2, sql3);
DataConnection.updateBatchAndCloseTransaction(sqls, "work", rv);

// ❌ Avoid: Multiple separate updates
DataConnection.updateAndClose(sql1, "work", rv);
DataConnection.updateAndClose(sql2, "work", rv);
DataConnection.updateAndClose(sql3, "work", rv);
```

### 10.5 Cache Usage

```java
// ✅ Recommended: Use cache for frequent queries
DTTable tb = DTTable.getCachedTable(sql, 300, "work", rv);

// ❌ Avoid: Caching real-time data
// Data with high real-time requirements (balances, inventory) should not be cached
```

---

## 11. Complete Examples

### 11.1 User Management CRUD

```java
public class UserService {
    
    // Query user list
    public DTTable getUserList(int page, int pageSize) {
        String sql = "SELECT * FROM users ORDER BY create_date DESC";
        RequestValue rv = new RequestValue();
        return DTTable.getJdbcTable(sql, "user_id", pageSize, page, "work", rv);
    }
    
    // Query single user
    public DTTable getUserById(long userId) {
        String sql = "SELECT * FROM users WHERE user_id=@user_id";
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("user_id", userId);
        return DTTable.getJdbcTable(sql, rv);
    }
    
    // Create user
    public long createUser(String name, String email) {
        String sql = "INSERT INTO users (name, email, create_date) VALUES (@name, @email, @create_date)";
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("name", name);
        rv.addOrUpdateValue("email", email);
        rv.addOrUpdateValue("create_date", new Date());
        return DataConnection.insertAndReturnAutoIdLong(sql, "work", rv);
    }
    
    // Update user
    public boolean updateUser(long userId, String name, String email) {
        String sql = "UPDATE users SET name=@name, email=@email WHERE user_id=@user_id";
        RequestValue rv = new RequestValue();
        rv.addOrUpdateValue("user_id", userId);
        rv.addOrUpdateValue("name", name);
        rv.addOrUpdateValue("email", email);
        String error = DataConnection.updateAndClose(sql, "work", rv);
        return error == null;
    }
    
    // Delete user (transactional)
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

### 11.2 Order Processing (Transactional)

```java
public class OrderService {
    
    public boolean createOrder(Order order) {
        DataConnection conn = new DataConnection("work", null);
        
        try {
            conn.transBegin();
            
            // 1. Create order
            String sql1 = "INSERT INTO orders (user_id, total, status) VALUES (@user_id, @total, 'PENDING')";
            RequestValue rv = new RequestValue();
            rv.addOrUpdateValue("user_id", order.getUserId());
            rv.addOrUpdateValue("total", order.getTotal());
            long orderId = conn.executeUpdateReturnAutoIncrementObject(sql1);
            
            // 2. Create order items
            for (OrderItem item : order.getItems()) {
                String sql2 = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@order_id, @product_id, @quantity, @price)";
                rv.addOrUpdateValue("order_id", orderId);
                rv.addOrUpdateValue("product_id", item.getProductId());
                rv.addOrUpdateValue("quantity", item.getQuantity());
                rv.addOrUpdateValue("price", item.getPrice());
                conn.executeUpdate(sql2);
            }
            
            // 3. Update inventory
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
            LOGGER.error("Order creation failed", e);
            return false;
        } finally {
            conn.close();
        }
    }
}
```

---

## Summary

The EWA framework provides a rich set of database access methods:

| Method | Use Case | Advantages |
|--------|----------|------------|
| **DTTable** | Query returning table data | Simple to use, auto-closes connections |
| **DataConnection** | Complex operations, transactions | Flexible control, batch support |
| **XML Configuration** | Page-driven applications | Configuration-based, no coding needed |
| **SqlCached** | Frequent queries | Performance optimization |
| **Transaction Handling** | Multi-table updates | Data consistency |

Selection guidance:
- Simple queries → `DTTable.getJdbcTable()`
- Single-table updates → `DataConnection.updateAndClose()`
- Multi-table operations → Transaction + batch updates
- Frequent queries → Cached queries
- Real-time data → Direct queries (no cache)
