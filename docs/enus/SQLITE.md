# SQLite Testing Guide

## Overview

SQLite is a lightweight embedded database that requires no standalone server process, making it ideal for writing unit tests for `DataConnection`. This document explains how to use SQLite in-memory/file databases to verify DataConnection's CRUD, transaction, batch execution, and multi-threaded concurrency scenarios.

---

## 1. Maven Dependency

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.36.0.3</version>
    <scope>test</scope>
</dependency>
```

> **Version Notice**: Do not use version 3.45.x or later. These versions have a compatibility issue with HikariCP connection proxies, throwing `SafeStmtPtr.isClosed() NPE` during SQL execution. Version 3.36.0.3 has been verified to work correctly.

---

## 2. ConnectionConfig Setup

Register the SQLite connection config manually via `ConnectionConfigs.instance()` during tests — no external XML file needed.

### 2.1 HikariCP (Default Pool)

```java
ConnectionConfig cfg = new ConnectionConfig();
cfg.setName("sqlite_test");
cfg.setType("SQLITE");
cfg.setConnectionString("sqlite_test");
cfg.setSchemaName("main");

MTableStr pool = new MTableStr();
pool.put("driverClassName", "org.sqlite.JDBC");
pool.put("url", "jdbc:sqlite:/tmp/test.db");   // file-based DB
pool.put("username", "sa");
pool.put("password", "");                       // SQLite requires no password
pool.put("maxActive", "1");
cfg.setPool(pool);

ConnectionConfigs.instance().put("sqlite_test", cfg);
```

### 2.2 Druid Pool

```java
MTableStr pool = new MTableStr();
pool.put("driverClassName", "org.sqlite.JDBC");
pool.put("url", "jdbc:sqlite:/tmp/test_druid.db");
pool.put("username", "sa");
pool.put("password", "x");          // Druid requires non-blank password; SQLite ignores it
pool.put("poolType", "druid");      // Key: specify Druid pool
pool.put("maxActive", "1");
```

> **Druid vs HikariCP**: Druid's `createMyDatasourcesDruids()` validates that `password` is not blank, while HikariCP does not. Since SQLite itself does not use a password, a placeholder value is required for Druid.

### 2.3 Path Expressions

`ConnectionConfig` supports path shortcut expressions in pool URLs. They are automatically resolved to absolute paths during `setPool()` and XML parsing:

| Expression | Resolved To | Example |
|------------|-------------|---------|
| `~` / `@home` | User home directory (`user.home`) | `jdbc:sqlite:~/data/test.db` → `jdbc:sqlite:/home/user/data/test.db` |
| `@temp` | System temp directory (`java.io.tmpdir`) | `jdbc:sqlite:@temp/test.db` → `jdbc:sqlite:/tmp/test.db` |
| `@cwd` / `@pwd` | Current working directory (`user.dir`) | `jdbc:sqlite:@cwd/test.db` → `jdbc:sqlite:/project/test.db` |

```java
// Use ~ for user home directory
pool.put("url", "jdbc:sqlite:~/myapp/data.db");

// Use @temp for system temp directory
pool.put("url", "jdbc:sqlite:@temp/test.db");

// Use @pwd for current working directory
pool.put("url", "jdbc:sqlite:@pwd/data.db");
```

> You can also call `ConnectionConfig.resolvePath(path)` statically for standalone path resolution.

### 2.4 In-Memory vs File Database

| Mode | JDBC URL | Use Case |
|------|----------|---------|
| In-memory (single connection) | `jdbc:sqlite::memory:` | Single-threaded tests, simplest setup |
| In-memory (shared cache) | `jdbc:sqlite:file:testdb?mode=memory&cache=shared` | Multi-threaded, but may have lock contention |
| File-based | `jdbc:sqlite:/path/to/test.db` | **Recommended**, stable for multi-threading |

> Multi-threaded tests **must use a file-based database**. In memory mode, each connection gets its own independent database instance — different connections in the pool cannot see each other's data.

---

## 3. Basic CRUD Tests

### 3.1 Create Table

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

### 3.2 Insert and Get Auto-Increment ID

```java
DataConnection conn = new DataConnection("sqlite_test", null);
int autoId = conn.executeUpdateReturnAutoIncrement(
    "INSERT INTO test_user (name, age, score) VALUES ('Alice', 30, 95.5)");
assertTrue(autoId > 0);
conn.close();
```

### 3.3 Parameterized Insert

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

### 3.4 Query

```java
// Query without parameters
DTTable table = DTTable.getJdbcTable("SELECT * FROM test_user ORDER BY id", conn);
assertEquals(2, table.getCount());
assertEquals("Alice", table.getCell(0, "name").toString());

// Parameterized query
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "Alice");
DTTable table = DTTable.getJdbcTable(
    "SELECT * FROM test_user WHERE name = @name", conn);
```

### 3.5 Update and Delete

```java
// Parameterized update
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("age", 31);
rv.addOrUpdateValue("name", "Alice");
conn.executeUpdate("UPDATE test_user SET age = @age WHERE name = @name");

// Parameterized delete
RequestValue rv = new RequestValue();
rv.addOrUpdateValue("name", "Bob");
conn.executeUpdate("DELETE FROM test_user WHERE name = @name");
```

### 3.6 Count and Existence Check

```java
int count = conn.executeQueryCount("test_user", "1=1");
boolean exists = conn.executeQueryExists("test_user", "name='Alice'");
```

---

## 4. Transaction Tests

### 4.1 Transaction Commit

```java
DataConnection conn = new DataConnection("sqlite_test", null);
try {
    conn.transBegin();
    conn.executeUpdateNoParameter(
        "INSERT INTO test_user (name, age, score) VALUES ('TxUser', 40, 70.0)");
    conn.transCommit();
} finally {
    resetAutoCommit(conn);  // See explanation below
    conn.close();
}
```

### 4.2 Transaction Rollback

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

### 4.3 autoCommit Reset

`DataConnection.transBegin()` sets `autoCommit` to `false`, but `transClose()` / `close()` **does not restore** `autoCommit` to `true`. After the connection is returned to the pool, the next user may inherit the `autoCommit=false` state, causing unexpected behavior.

Tests must manually reset in a `finally` block:

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

## 5. Batch Execution

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

## 6. Multi-Threaded Concurrency Tests

### 6.1 SQLite Concurrency Characteristics

| Feature | Description |
|---------|-------------|
| Write lock | Database-level lock (not row-level), only one writer at a time |
| Read lock | No lock, multiple readers can proceed concurrently |
| WAL mode | Allows concurrent reads during writes, readers do not block writers |
| busy_timeout | Waits the specified milliseconds on SQLITE_BUSY instead of failing immediately |

### 6.2 Pool Configuration

```java
pool.put("maxActive", "4");  // Multi-threaded tests need >1 connections
```

Set PRAGMAs via JDBC:

```java
DataConnection conn = new DataConnection(cfgName, null);
conn.executeUpdateNoParameter("PRAGMA journal_mode=WAL");
conn.executeUpdateNoParameter("PRAGMA busy_timeout=5000");
```

- **WAL mode**: Write operations do not block read operations, suitable for mixed read/write scenarios
- **busy_timeout=5000**: On write lock contention, wait up to 5 seconds instead of throwing `SQLITE_BUSY` immediately

### 6.3 Concurrent Insert Test Pattern

```java
int THREAD_COUNT = 8;
int ROWS_PER_THREAD = 20;
ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
CountDownLatch startLatch = new CountDownLatch(1);

for (int t = 0; t < THREAD_COUNT; t++) {
    final String tName = "thread_" + t;
    executor.submit(() -> {
        startLatch.await();  // All threads start simultaneously
        for (int i = 0; i < ROWS_PER_THREAD; i++) {
            DataConnection conn = new DataConnection(cfgName, null);
            conn.executeUpdateNoParameter(
                "INSERT INTO mt_test (thread_name, seq) VALUES ('" + tName + "', " + i + ")");
            conn.close();  // Return connection to pool
        }
    });
}
startLatch.countDown();  // Release all threads
```

> Each iteration creates a new `DataConnection` and acquires a connection from the pool, simulating real-world usage. `conn.close()` returns the connection to the pool rather than truly closing it.

### 6.4 Mixed Read/Write Test

```java
// Half the threads write
for (int t = 0; t < THREAD_COUNT / 2; t++) {
    executor.submit(() -> {
        for (int i = 0; i < ROWS_PER_THREAD; i++) {
            DataConnection conn = new DataConnection(cfgName, null);
            conn.executeUpdateNoParameter("INSERT INTO ...");
            conn.close();
        }
    });
}
// Half the threads read
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

## 7. Test Cleanup

```java
@AfterAll
static void tearDown() {
    // Close the connection pool
    DataHelper.closeDataSource("sqlite_test");
    // Delete the temp database file
    new java.io.File("/tmp/test.db").delete();
    // WAL mode may produce auxiliary files
    new java.io.File("/tmp/test.db-wal").delete();
    new java.io.File("/tmp/test.db-shm").delete();
}
```

---

## 8. Known Issues and Caveats

### 8.1 sqlite-jdbc Version Compatibility

| Version | HikariCP | Druid |
|---------|----------|-------|
| 3.36.0.3 | ✅ | ✅ |
| 3.45.1.0 | ❌ SafeStmtPtr NPE | ❌ |

**Root Cause**: Version 3.45.x uses `SafeStmtPtr` internally in `SQLiteStatement` to manage statement pointers. Under HikariCP/Druid proxy connections, the statement close timing is incompatible with the pool's proxy layer, causing an NPE.

### 8.2 Pool Size and SQLite Locks

- `maxActive=1`: All operations are serialized, no lock contention, but low throughput
- `maxActive>1`: Multiple threads can acquire connections in parallel, but write operations are still limited by SQLite's database-level lock
- For write-intensive scenarios, use `maxActive=1` or combine with `busy_timeout`

### 8.3 transBegin() autoCommit Side Effect

`DataConnection.transBegin()` calls `connection.setAutoCommit(false)`, but `close()` does not restore it. After the connection is returned to the pool, the next user may inherit this state. Test code must manually restore `autoCommit=true` in a `finally` block.

### 8.4 useDatabase() Impact on SQLite

`DataConnection.useDatabase()` only executes `USE database` for MySQL and SQL Server. SQLite's `_DatabaseType` is `"SQLITE"`, which matches no condition — `useDatabase()` simply returns `false` with no effect on normal operations.

---

## 9. Test File Index

| File | Description |
|------|-------------|
| `DataConnectionSqliteTest.java` | HikariCP pool basic tests (16 cases) |
| `DataConnectionSqliteDruidTest.java` | Druid pool basic tests (14 cases) |
| `DataConnectionSqliteConcurrencyTest.java` | Multi-threaded read/write concurrency tests (8 cases) |
