# SQLite 测试指南

## 概述

SQLite 作为轻量级嵌入式数据库，无需独立服务进程，非常适合为 `DataConnection` 编写单元测试。本文档说明如何在测试中通过 SQLite 内存/文件数据库验证 DataConnection 的 CRUD、事务、批量执行及多线程并发场景。

---

## 1. Maven 依赖

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.36.0.3</version>
    <scope>test</scope>
</dependency>
```

> **版本注意**：当前锁定 3.36.0.3。3.41.2.2 修复了 CVE（攻击者控制 JDBC URL 时的 RCE 漏洞），但与 HikariCP（SafeStmtPtr NPE）和 Druid（批量执行失败）均不兼容。项目 JDBC URL 来自受信配置，CVE 风险极低。待 sqlite-jdbc 修复连接池兼容性问题后再升级。

---

## 2. ConnectionConfig 配置

测试中通过 `ConnectionConfigs.instance()` 手动注册 SQLite 连接配置，无需外部 XML 文件。

### 2.1 HikariCP（默认连接池）

```java
ConnectionConfig cfg = new ConnectionConfig();
cfg.setName("sqlite_test");
cfg.setType("SQLITE");
cfg.setConnectionString("sqlite_test");
cfg.setSchemaName("main");

MTableStr pool = new MTableStr();
pool.put("driverClassName", "org.sqlite.JDBC");
pool.put("url", "jdbc:sqlite:/tmp/test.db");   // 文件库
pool.put("username", "sa");
pool.put("password", "");                       // SQLite 无需密码
pool.put("maxActive", "1");
cfg.setPool(pool);

ConnectionConfigs.instance().put("sqlite_test", cfg);
```

### 2.2 Druid 连接池

```java
MTableStr pool = new MTableStr();
pool.put("driverClassName", "org.sqlite.JDBC");
pool.put("url", "jdbc:sqlite:/tmp/test_druid.db");
pool.put("username", "sa");
pool.put("password", "x");          // Druid 要求 password 非空，SQLite 会忽略
pool.put("poolType", "druid");      // 关键：指定使用 Druid
pool.put("maxActive", "1");
```

> **Druid 与 HikariCP 的区别**：Druid 的 `createMyDatasourcesDruids()` 方法校验 `password` 不能为空，而 HikariCP 不校验。SQLite 本身不使用密码，因此 Druid 配置中需设置一个占位值。

### 2.3 路径表达式

`ConnectionConfig` 支持在 pool URL 中使用路径快捷表达式，`setPool()` 和 XML 解析时自动替换为绝对路径：

| 表达式 | 替换为 | 示例 |
|--------|--------|------|
| `~` / `@home` | 用户主目录 (`user.home`) | `jdbc:sqlite:~/data/test.db` → `jdbc:sqlite:/home/user/data/test.db` |
| `@temp` | 系统临时目录 (`java.io.tmpdir`) | `jdbc:sqlite:@temp/test.db` → `jdbc:sqlite:/tmp/test.db` |
| `@cwd` / `@pwd` | 当前工作目录 (`user.dir`) | `jdbc:sqlite:@cwd/test.db` → `jdbc:sqlite:/project/test.db` |

```java
// 使用 ~ 指定用户目录下的数据库
pool.put("url", "jdbc:sqlite:~/myapp/data.db");

// 使用 @temp 指定临时目录
pool.put("url", "jdbc:sqlite:@temp/test.db");

// 使用 @pwd 指定当前工作目录
pool.put("url", "jdbc:sqlite:@pwd/data.db");
```

> 也可以通过 `ConnectionConfig.resolvePath(path)` 静态方法单独调用路径解析。

### 2.4 内存数据库 vs 文件数据库

| 模式 | JDBC URL | 适用场景 |
|------|----------|---------|
| 内存（单连接） | `jdbc:sqlite::memory:` | 单线程测试，最简单 |
| 内存（共享缓存） | `jdbc:sqlite:file:testdb?mode=memory&cache=shared` | 多线程，但可能有锁竞争 |
| 文件 | `jdbc:sqlite:/path/to/test.db` | **推荐**，多线程稳定 |

> 多线程测试**必须使用文件数据库**。内存模式下每个连接拥有独立的数据库实例，连接池中的不同连接无法看到彼此的数据。

---

## 3. 基本 CRUD 测试

### 3.1 建表

```java
DataConnection conn = new DataConnection("sqlite_test", null);
conn.executeUpdateNoParameter("DROP TABLE IF EXISTS test_user");
conn.executeUpdateNoParameter(
    "CREATE TABLE test_user ("
    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
    + "name TEXT NOT NULL, "
    + "age INTEGER, "
    + "score REAL)");
conn.close();
```

### 3.2 插入并获取自增 ID

```java
DataConnection conn = new DataConnection("sqlite_test", null);
int autoId = conn.executeUpdateReturnAutoIncrement(
    "INSERT INTO test_user (name, age, score) VALUES ('Alice', 30, 95.5)");
assertTrue(autoId > 0);
conn.close();
```

### 3.3 参数化插入

```java
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "Bob");
rv.addOrUpdateValue("age", 25);
rv.addOrUpdateValue("score", 88.0);

DataConnection conn = new DataConnection("sqlite_test", rv);
int autoId = conn.executeUpdateReturnAutoIncrement(
    "INSERT INTO test_user (name, age, score) VALUES (@name, @age, @score)");
conn.close();
```

### 3.4 查询

```java
// 无参数查询
DTTable table = DTTable.getJdbcTable("SELECT * FROM test_user ORDER BY id", conn);
assertEquals(2, table.getCount());
assertEquals("Alice", table.getCell(0, "name").toString());

// 参数化查询
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "Alice");
DTTable table = DTTable.getJdbcTable(
    "SELECT * FROM test_user WHERE name = @name", conn);
```

### 3.5 更新与删除

```java
// 参数化更新
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("age", 31);
rv.addOrUpdateValue("name", "Alice");
conn.executeUpdate("UPDATE test_user SET age = @age WHERE name = @name");

// 参数化删除
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "Bob");
conn.executeUpdate("DELETE FROM test_user WHERE name = @name");
```

### 3.6 计数与存在性检查

```java
int count = conn.executeQueryCount("test_user", "1=1");
boolean exists = conn.executeQueryExists("test_user", "name='Alice'");
```

---

## 4. 事务测试

### 4.1 事务提交

```java
DataConnection conn = new DataConnection("sqlite_test", null);
try {
    conn.transBegin();
    conn.executeUpdateNoParameter(
        "INSERT INTO test_user (name, age, score) VALUES ('TxUser', 40, 70.0)");
    conn.transCommit();
} finally {
    resetAutoCommit(conn);  // 见下方说明
    conn.close();
}
```

### 4.2 事务回滚

```java
DataConnection conn = new DataConnection("sqlite_test", null);
int countBefore = conn.executeQueryCount("test_user", "1=1");
try {
    conn.transBegin();
    conn.executeUpdateNoParameter(
        "INSERT INTO test_user (name, age, score) VALUES ('RollbackUser', 50, 60.0)");
    conn.transRollback();
} finally {
    resetAutoCommit(conn);
    conn.close();
}
int countAfter = conn.executeQueryCount("test_user", "1=1");
assertEquals(countBefore, countAfter);
```

### 4.3 autoCommit 重置

`DataConnection.transBegin()` 将 `autoCommit` 设为 `false`，但 `transClose()` / `close()` **不会恢复** `autoCommit` 为 `true`。连接归还到连接池后，下一个使用者可能继承 `autoCommit=false` 的状态，导致后续操作异常。

测试中需要在 `finally` 块手动重置：

```java
private void resetAutoCommit(DataConnection conn) {
    try {
        conn.connect();
        Connection jdbc = conn.getConnection();
        if (jdbc != null && !jdbc.getAutoCommit()) {
            jdbc.setAutoCommit(true);
        }
    } catch (Exception ignored) {
    }
}
```

---

## 5. 批量执行

```java
DataConnection conn = new DataConnection("sqlite_test", null);
List<String> sqls = Arrays.asList(
    "INSERT INTO test_user (name, age, score) VALUES ('B1', 20, 50.0)",
    "INSERT INTO test_user (name, age, score) VALUES ('B2', 21, 51.0)",
    "INSERT INTO test_user (name, age, score) VALUES ('B3', 22, 52.0)"
);
int result = conn.executeUpdateBatch(sqls);
assertTrue(result >= 0);
conn.close();
```

---

## 6. 多线程并发测试

### 6.1 SQLite 并发特性

| 特性 | 说明 |
|------|------|
| 写锁 | 数据库级锁（非行级），同一时刻只有一个写者 |
| 读锁 | 无锁，多读者可并发 |
| WAL 模式 | 允许读写并发执行，读者不阻塞写者 |
| busy_timeout | 遇到 SQLITE_BUSY 时等待指定毫秒数，而非立即报错 |

### 6.2 连接池配置

```java
pool.put("maxActive", "4");  // 多线程需要 >1 的连接数
```

JDBC URL 中添加 PRAGMA：

```java
DataConnection conn = new DataConnection(cfgName, null);
conn.executeUpdateNoParameter("PRAGMA journal_mode=WAL");
conn.executeUpdateNoParameter("PRAGMA busy_timeout=5000");
```

- **WAL 模式**：写操作不会阻塞读操作，适合读写混合场景
- **busy_timeout=5000**：写锁冲突时等待最多 5 秒，避免立即抛出 `SQLITE_BUSY`

### 6.3 并发插入测试模式

```java
int THREAD_COUNT = 8;
int ROWS_PER_THREAD = 20;
ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
CountDownLatch startLatch = new CountDownLatch(1);

for (int t = 0; t < THREAD_COUNT; t++) {
    final String tName = "thread_" + t;
    executor.submit(() -> {
        startLatch.await();  // 所有线程同时开始
        for (int i = 0; i < ROWS_PER_THREAD; i++) {
            DataConnection conn = new DataConnection(cfgName, null);
            conn.executeUpdateNoParameter(
                "INSERT INTO mt_test (thread_name, seq) VALUES ('" + tName + "', " + i + ")");
            conn.close();  // 归还连接到池
        }
    });
}
startLatch.countDown();  // 释放所有线程
```

> 每次迭代创建新的 `DataConnection` 并从池中获取连接，模拟真实使用场景。`conn.close()` 将连接归还到池中而非真正关闭。

### 6.4 读写混合测试

```java
// 一半线程写
for (int t = 0; t < THREAD_COUNT / 2; t++) {
    executor.submit(() -> {
        for (int i = 0; i < ROWS_PER_THREAD; i++) {
            DataConnection conn = new DataConnection(cfgName, null);
            conn.executeUpdateNoParameter("INSERT INTO ...");
            conn.close();
        }
    });
}
// 一半线程读
for (int t = 0; t < THREAD_COUNT / 2; t++) {
    executor.submit(() -> {
        for (int i = 0; i < ROWS_PER_THREAD; i++) {
            DataConnection conn = new DataConnection(cfgName, null);
            DTTable table = DTTable.getJdbcTable("SELECT count(*) cnt FROM ...", conn);
            conn.close();
        }
    });
}
```

---

## 7. 测试清理

```java
@AfterAll
static void tearDown() {
    // 关闭连接池
    DataHelper.closeDataSource("sqlite_test");
    // 删除临时数据库文件
    new java.io.File("/tmp/test.db").delete();
    // WAL 模式可能产生附属文件
    new java.io.File("/tmp/test.db-wal").delete();
    new java.io.File("/tmp/test.db-shm").delete();
}
```

---

## 8. 已知问题与注意事项

### 8.1 sqlite-jdbc 版本兼容性

| 版本 | HikariCP | Druid |
|------|----------|-------|
| 3.36.0.3 | ✅ | ✅ |
| 3.45.1.0 | ❌ SafeStmtPtr NPE | ❌ |

**根因**：3.45.x 的 `SQLiteStatement` 内部使用 `SafeStmtPtr` 管理语句指针，在 HikariCP/Druid 代理连接环境下，语句关闭时序与连接池的代理层不兼容，导致 NPE。

### 8.2 连接池大小与 SQLite 锁

- `maxActive=1`：所有操作串行，无锁竞争，但吞吐量低
- `maxActive>1`：多线程可并行获取连接，但写操作仍受 SQLite 数据库级锁限制
- 写密集场景建议 `maxActive=1` 或配合 `busy_timeout` 使用

### 8.3 transBegin() 的 autoCommit 副作用

`DataConnection.transBegin()` 调用 `connection.setAutoCommit(false)`，但 `close()` 不会恢复。连接归还到池后，下一个使用者可能继承此状态。测试代码中必须在 `finally` 块手动恢复 `autoCommit=true`。

### 8.4 useDatabase() 对 SQLite 的影响

`DataConnection.useDatabase()` 仅对 MySQL 和 SQL Server 执行 `USE database` 语句。SQLite 的 `_DatabaseType` 为 `"SQLITE"`，不匹配任何条件，`useDatabase()` 直接返回 `false`，不影响正常操作。

---

## 9. 测试文件索引

| 文件 | 说明 |
|------|------|
| `DataConnectionSqliteTest.java` | HikariCP 连接池基础测试（16 个用例） |
| `DataConnectionSqliteDruidTest.java` | Druid 连接池基础测试（14 个用例） |
| `DataConnectionSqliteConcurrencyTest.java` | 多线程并发读写测试（8 个用例） |
