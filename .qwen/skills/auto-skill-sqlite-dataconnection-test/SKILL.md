---
name: sqlite-dataconnection-test
description: How to write DataConnection tests using SQLite — driver version, pool setup (HikariCP/Druid), path expressions, concurrency, and API patterns
source: auto-skill
extracted_at: '2026-07-13T13:51:02.444Z'
---

# SQLite DataConnection Testing

## Critical: SQLite JDBC Driver Version

**Use `sqlite-jdbc:3.36.0.3`** — pinned due to connection pool incompatibilities in newer versions.

| Version | HikariCP | Druid | Security |
|---------|----------|-------|----------|
| 3.36.0.3 | ✅ | ✅ | ❌ CVE (RCE via attacker-controlled JDBC URL) |
| 3.41.2.2+ | ❌ SafeStmtPtr NPE | ❌ batch failures | ✅ CVE fixed |
| 3.45.x+ | ❌ SafeStmtPtr NPE | ❌ batch failures | ✅ CVE fixed |

**CVE context**: The vulnerability (fixed in 3.41.2.2) requires the attacker to control the JDBC URL. Since our URLs come from trusted config/code, risk is minimal. Will upgrade once sqlite-jdbc resolves pool compatibility.

```xml
<!-- SQLite JDBC for testing.
     Pinned to 3.36.0.3: versions >= 3.41.2.2 fix CVE (RCE via attacker-controlled JDBC URL)
     but break compatibility with both HikariCP (SafeStmtPtr NPE) and Druid (batch failures).
     Our JDBC URLs come from trusted config, so CVE risk is minimal. -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.36.0.3</version>
    <scope>test</scope>
</dependency>
```

## ConnectionConfig Setup

Register a SQLite config in `ConnectionConfigs` via `@BeforeAll`:

```java
ConnectionConfigs configs = ConnectionConfigs.instance();
ConnectionConfig cfg = new ConnectionConfig();
cfg.setName("sqlite_test");
cfg.setType("SQLITE");           // any non-MYSQL/MSSQL/ORACLE value works
cfg.setConnectionString("sqlite_test");
cfg.setSchemaName("main");

MTableStr pool = new MTableStr();
pool.put("driverClassName", "org.sqlite.JDBC");
// Use file-based temp DB — avoids shared-cache contention of memory mode
pool.put("url", "jdbc:sqlite:" + System.getProperty("java.io.tmpdir") + "/test.db");
pool.put("username", "sa");      // HikariCP requires non-blank username
pool.put("password", "");
pool.put("maxActive", "1");      // HikariCP single-threaded
cfg.setPool(pool);
configs.put("sqlite_test", cfg);
```

### Why these settings:
- **File-based DB** (not `:memory:`): HikariCP creates multiple connections; each `:memory:` connection gets its own empty database. File-based avoids this.
- **`maxActive=1`** (HikariCP): SQLite file-level locking causes `SQLITE_LOCKED_SHAREDCACHE` with concurrent writers.
- **`username=sa`**: HikariCP's `createMyDatasourcesHikariCP()` throws if username is blank.

### HikariCP vs Druid pool:

```java
// HikariCP (default)
pool.put("password", "");        // HikariCP does NOT validate password
pool.put("maxActive", "1");

// Druid
pool.put("password", "x");       // Druid validates password is non-blank; SQLite ignores it
pool.put("poolType", "druid");   // Switch from default HikariCP to Druid
pool.put("maxActive", "2");      // Druid needs >1 to avoid pool getting stuck after errors
```

### Druid-specific limitations:
- **No transaction tests**: After `transBegin()` sets `autoCommit=false`, Druid pool cannot properly restore connection state. Subsequent operations fail. Transaction behavior is covered by HikariCP tests only.
- **`maxActive` must be >1**: With `maxActive=1`, if a connection enters a bad state, the pool cannot replace it and times out after 60s.

### Path expressions in pool URL:
`ConnectionConfig` auto-resolves these in pool URLs (both XML and programmatic):

| Expression | Resolved To | Example |
|------------|-------------|---------|
| `~` / `@home` | `user.home` | `jdbc:sqlite:~/data/test.db` |
| `@temp` | `java.io.tmpdir` | `jdbc:sqlite:@temp/test.db` |
| `@cwd` / `@pwd` | `user.dir` | `jdbc:sqlite:@cwd/test.db` |

```java
// All work — resolved automatically by setPool() and XML parsing:
pool.put("url", "jdbc:sqlite:~/test.db");
pool.put("url", "jdbc:sqlite:@temp/test.db");
pool.put("url", "jdbc:sqlite:@pwd/test.db");

// Standalone resolution:
String resolved = ConnectionConfig.resolvePath("~/data.db");
```

## Transaction Cleanup (HikariCP only)

`DataConnection.transClose()` calls `close()` but does NOT restore `autoCommit=true`. After a transaction test, the pooled connection retains `autoCommit=false`, breaking subsequent tests. Fix:

```java
private void resetAutoCommit(DataConnection conn) {
    try {
        conn.connect();
        Connection jdbc = conn.getConnection();
        if (jdbc != null && !jdbc.getAutoCommit()) {
            jdbc.setAutoCommit(true);
        }
    } catch (Exception ignored) {}
}

// In transaction tests:
try {
    conn.transBegin();
    // ... operations ...
    conn.transCommit();
} finally {
    resetAutoCommit(conn);
    conn.close();
}
```

## Multi-Threaded Concurrency

For concurrent tests, increase pool size and enable WAL + busy_timeout:

```java
pool.put("maxActive", "4");  // >1 for concurrent access

// In @BeforeAll, after creating the table:
DataConnection conn = new DataConnection(cfgName, null);
conn.executeUpdateNoParameter("PRAGMA journal_mode=WAL");      // readers don't block on writes
conn.executeUpdateNoParameter("PRAGMA busy_timeout=5000");     // wait on SQLITE_BUSY
conn.close();
```

- **WAL mode**: allows concurrent reads during writes
- **busy_timeout**: writers wait instead of failing immediately on lock contention
- Each thread creates its own `DataConnection` per iteration, returns to pool via `close()`
- Use `CountDownLatch` to release all threads simultaneously for true concurrency

## DTTable API Patterns

```java
// Query via DataConnection
DTTable table = DTTable.getJdbcTable("SELECT ...", conn);
assertTrue(table.isOk(), table.getErrorInfo());

// Row count: use getCount() NOT getRows().size()
assertEquals(2, table.getCount());

// Cell access: getCell(rowIndex, colName) throws Exception
// Test methods must declare: throws Exception
assertEquals("value", table.getCell(0, "columnName").toString());
assertEquals(42, table.getCell(0, "intColumn").toInt());
```

## Known Bug: useDatabase() Statement Leak

`DataConnection.useDatabase()` previously created a Statement via `_ds.getStatement()` without closing it. Fixed with try-finally:

```java
Statement useDbSt = this._ds.getStatement();
try {
    useDbSt.executeUpdate(sql);
} finally {
    closeStatment(useDbSt);
}
```

This only affects MySQL/SQL Server (SQLite returns early from `useDatabase()`). The leaked Statement accumulated in `_ListStatement` and caused issues when `DataHelper.close()` batch-closed them all.

## Cleanup

```java
@AfterAll
static void tearDown() {
    DataHelper.closeDataSource("sqlite_test");
    new java.io.File(System.getProperty("java.io.tmpdir") + "/test.db").delete();
    // WAL mode may produce auxiliary files
    new java.io.File(System.getProperty("java.io.tmpdir") + "/test.db-wal").delete();
    new java.io.File(System.getProperty("java.io.tmpdir") + "/test.db-shm").delete();
}
```
