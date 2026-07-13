package com.gdxsoft.easyweb.datasource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

/**
 * Multi-threaded read/write tests for DataConnection with SQLite.
 * Tests both HikariCP and Druid pools under concurrent access.
 *
 * SQLite uses database-level locking for writes. WAL mode is enabled
 * to allow concurrent reads during writes. busy_timeout is set so
 * writers wait instead of failing immediately on SQLITE_BUSY.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataConnectionSqliteConcurrencyTest {

	private static final String HIKARI_CFG = "sqlite_mt_hikari";
	private static final String DRUID_CFG = "sqlite_mt_druid";
	private static final String DB_HIKARI = System.getProperty("java.io.tmpdir") + "/emp_script_mt_hikari.db";
	private static final String DB_DRUID = System.getProperty("java.io.tmpdir") + "/emp_script_mt_druid.db";
	private static final int POOL_SIZE = 4;
	private static final int THREAD_COUNT = 8;
	private static final int ROWS_PER_THREAD = 20;

	@BeforeAll
	static void setUp() throws Exception {
		ConnectionConfigs configs = ConnectionConfigs.instance();

		registerConfig(configs, HIKARI_CFG, DB_HIKARI, "", null);
		registerConfig(configs, DRUID_CFG, DB_DRUID, "x", "druid");

		for (String cfgName : new String[] { HIKARI_CFG, DRUID_CFG }) {
			DataConnection conn = new DataConnection(cfgName, null);
			conn.executeUpdateNoParameter("PRAGMA journal_mode=WAL");
			conn.executeUpdateNoParameter("PRAGMA busy_timeout=5000");
			conn.executeUpdateNoParameter("DROP TABLE IF EXISTS mt_test");
			conn.executeUpdateNoParameter("CREATE TABLE mt_test ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "thread_name TEXT NOT NULL, "
					+ "seq INTEGER NOT NULL, "
					+ "payload TEXT"
					+ ")");
			conn.close();
		}
	}

	private static void registerConfig(ConnectionConfigs configs, String name, String dbPath,
			String password, String poolType) throws Exception {
		ConnectionConfig cfg = new ConnectionConfig();
		cfg.setName(name);
		cfg.setType("SQLITE");
		cfg.setConnectionString(name);
		cfg.setSchemaName("main");

		MTableStr pool = new MTableStr();
		pool.put("driverClassName", "org.sqlite.JDBC");
		pool.put("url", "jdbc:sqlite:" + dbPath);
		pool.put("username", "sa");
		pool.put("password", password);
		pool.put("maxActive", String.valueOf(POOL_SIZE));
		if (poolType != null) {
			pool.put("poolType", poolType);
		}
		cfg.setPool(pool);
		configs.put(name, cfg);
	}

	@AfterAll
	static void tearDown() {
		DataHelper.closeDataSource(HIKARI_CFG);
		DataHelper.closeDataSource(DRUID_CFG);
		new java.io.File(DB_HIKARI).delete();
		new java.io.File(DB_DRUID).delete();
		new java.io.File(DB_HIKARI + "-wal").delete();
		new java.io.File(DB_HIKARI + "-shm").delete();
		new java.io.File(DB_DRUID + "-wal").delete();
		new java.io.File(DB_DRUID + "-shm").delete();
	}

	// ======================== HikariCP ========================

	@Test
	@Order(1)
	void testConcurrentInsertsHikari() throws Exception {
		runConcurrentInserts(HIKARI_CFG, "h_insert");
	}

	@Test
	@Order(2)
	void testConcurrentReadsHikari() throws Exception {
		runConcurrentReads(HIKARI_CFG);
	}

	@Test
	@Order(3)
	void testConcurrentMixedRWHikari() throws Exception {
		runConcurrentMixedRW(HIKARI_CFG, "h_mixed");
	}

	@Test
	@Order(4)
	void testConcurrentParamOpsHikari() throws Exception {
		runConcurrentParamOps(HIKARI_CFG, "h_param");
	}

	// ======================== Druid ========================

	@Test
	@Order(5)
	void testConcurrentInsertsDruid() throws Exception {
		runConcurrentInserts(DRUID_CFG, "d_insert");
	}

	@Test
	@Order(6)
	void testConcurrentReadsDruid() throws Exception {
		runConcurrentReads(DRUID_CFG);
	}

	@Test
	@Order(7)
	void testConcurrentMixedRWDruid() throws Exception {
		runConcurrentMixedRW(DRUID_CFG, "d_mixed");
	}

	@Test
	@Order(8)
	void testConcurrentParamOpsDruid() throws Exception {
		runConcurrentParamOps(DRUID_CFG, "d_param");
	}

	// ======================== implementations ========================

	/**
	 * THREAD_COUNT threads each insert ROWS_PER_THREAD rows.
	 * Each iteration creates a new DataConnection (gets a pooled connection).
	 */
	private void runConcurrentInserts(String cfgName, String prefix) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch startLatch = new CountDownLatch(1);
		CopyOnWriteArrayList<String> errors = new CopyOnWriteArrayList<>();
		AtomicInteger totalInserted = new AtomicInteger(0);

		List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < THREAD_COUNT; t++) {
			final String tName = prefix + "_t" + t;
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < ROWS_PER_THREAD; i++) {
						DataConnection conn = new DataConnection(cfgName, null);
						String sql = "INSERT INTO mt_test (thread_name, seq, payload) VALUES ('"
								+ tName + "', " + i + ", 'd_" + i + "')";
						if (conn.executeUpdateNoParameter(sql)) {
							totalInserted.incrementAndGet();
						} else {
							errors.add(tName + "[" + i + "]: " + conn.getErrorMsg());
						}
						conn.close();
					}
				} catch (Exception e) {
					errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}));
		}

		startLatch.countDown();
		for (Future<?> f : futures) {
			f.get(60, TimeUnit.SECONDS);
		}
		shutdown(executor);

		assertTrue(errors.isEmpty(),
				"Errors (" + errors.size() + "): first=" + (errors.isEmpty() ? "" : errors.get(0)));
		assertEquals(THREAD_COUNT * ROWS_PER_THREAD, totalInserted.get());

		// verify persisted count
		DataConnection conn = new DataConnection(cfgName, null);
		DTTable table = DTTable.getJdbcTable(
				"SELECT count(*) cnt FROM mt_test WHERE thread_name LIKE '" + prefix + "%'", conn);
		int dbCount = table.getCell(0, "cnt").toInt();
		conn.close();
		assertEquals(THREAD_COUNT * ROWS_PER_THREAD, dbCount);
	}

	/**
	 * THREAD_COUNT threads read concurrently.
	 * All should see a consistent snapshot (SQLite serializes reads against writes).
	 */
	private void runConcurrentReads(String cfgName) throws Exception {
		// seed data if needed
		DataConnection seed = new DataConnection(cfgName, null);
		DTTable chk = DTTable.getJdbcTable("SELECT count(*) cnt FROM mt_test", seed);
		if (chk.getCell(0, "cnt").toInt() == 0) {
			for (int i = 0; i < 50; i++) {
				seed.executeUpdateNoParameter(
						"INSERT INTO mt_test (thread_name, seq, payload) VALUES ('seed', " + i + ", 's_" + i + "')");
			}
		}
		seed.close();

		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch startLatch = new CountDownLatch(1);
		CopyOnWriteArrayList<String> errors = new CopyOnWriteArrayList<>();
		CopyOnWriteArrayList<Integer> counts = new CopyOnWriteArrayList<>();

		List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < THREAD_COUNT; t++) {
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					DataConnection conn = new DataConnection(cfgName, null);
					DTTable table = DTTable.getJdbcTable("SELECT count(*) cnt FROM mt_test", conn);
					if (table.isOk()) {
						counts.add(table.getCell(0, "cnt").toInt());
					} else {
						errors.add("query failed: " + table.getErrorInfo());
					}
					conn.close();
				} catch (Exception e) {
					errors.add(e.getMessage());
				}
			}));
		}

		startLatch.countDown();
		for (Future<?> f : futures) {
			f.get(15, TimeUnit.SECONDS);
		}
		shutdown(executor);

		assertTrue(errors.isEmpty(), "Read errors: " + errors);
		assertFalse(counts.isEmpty());
		int expected = counts.get(0);
		assertTrue(expected > 0);
		for (int c : counts) {
			assertEquals(expected, c, "All readers should see the same count");
		}
	}

	/**
	 * Half threads write, half read — simultaneously.
	 * WAL mode allows readers to proceed while a writer holds the write lock.
	 */
	private void runConcurrentMixedRW(String cfgName, String prefix) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch startLatch = new CountDownLatch(1);
		CopyOnWriteArrayList<String> errors = new CopyOnWriteArrayList<>();
		AtomicInteger writeOk = new AtomicInteger(0);
		AtomicInteger readOk = new AtomicInteger(0);

		List<Future<?>> futures = new ArrayList<>();

		// writers
		for (int t = 0; t < THREAD_COUNT / 2; t++) {
			final String tName = prefix + "_w" + t;
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < ROWS_PER_THREAD; i++) {
						DataConnection conn = new DataConnection(cfgName, null);
						String sql = "INSERT INTO mt_test (thread_name, seq, payload) VALUES ('"
								+ tName + "', " + i + ", 'm_" + i + "')";
						if (conn.executeUpdateNoParameter(sql)) {
							writeOk.incrementAndGet();
						} else {
							errors.add("W " + tName + "[" + i + "]: " + conn.getErrorMsg());
						}
						conn.close();
					}
				} catch (Exception e) {
					errors.add("W: " + e.getMessage());
				}
			}));
		}

		// readers
		for (int t = 0; t < THREAD_COUNT / 2; t++) {
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < ROWS_PER_THREAD; i++) {
						DataConnection conn = new DataConnection(cfgName, null);
						DTTable table = DTTable.getJdbcTable("SELECT count(*) cnt FROM mt_test", conn);
						if (table.isOk()) {
							readOk.incrementAndGet();
						} else {
							errors.add("R: " + table.getErrorInfo());
						}
						conn.close();
					}
				} catch (Exception e) {
					errors.add("R: " + e.getMessage());
				}
			}));
		}

		startLatch.countDown();
		for (Future<?> f : futures) {
			f.get(60, TimeUnit.SECONDS);
		}
		shutdown(executor);

		assertTrue(errors.isEmpty(),
				"Mixed R/W errors (" + errors.size() + "): first=" + (errors.isEmpty() ? "" : errors.get(0)));
		assertEquals(THREAD_COUNT / 2 * ROWS_PER_THREAD, writeOk.get());
		assertEquals(THREAD_COUNT / 2 * ROWS_PER_THREAD, readOk.get());
	}

	/**
	 * THREAD_COUNT threads each insert ROWS_PER_THREAD rows using @param parameterized SQL.
	 * Verifies data integrity after concurrent parameterized writes.
	 */
	private void runConcurrentParamOps(String cfgName, String prefix) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch startLatch = new CountDownLatch(1);
		CopyOnWriteArrayList<String> errors = new CopyOnWriteArrayList<>();

		List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < THREAD_COUNT; t++) {
			final String tName = prefix + "_t" + t;
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < ROWS_PER_THREAD; i++) {
						RequestValue rv = new RequestValue();
						rv.addOrUpdateValue("tname", tName);
						rv.addOrUpdateValue("seq", i);
						rv.addOrUpdateValue("payload", "p_" + tName + "_" + i);

						DataConnection conn = new DataConnection(cfgName, rv);
						String sql = "INSERT INTO mt_test (thread_name, seq, payload) VALUES (@tname, @seq, @payload)";
						if (!conn.executeUpdate(sql)) {
							errors.add(tName + "[" + i + "]: " + conn.getErrorMsg());
						}
						conn.close();
					}
				} catch (Exception e) {
					errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}));
		}

		startLatch.countDown();
		for (Future<?> f : futures) {
			f.get(60, TimeUnit.SECONDS);
		}
		shutdown(executor);

		assertTrue(errors.isEmpty(),
				"Param errors (" + errors.size() + "): first=" + (errors.isEmpty() ? "" : errors.get(0)));

		// verify: each thread should have exactly ROWS_PER_THREAD rows
		DataConnection verifyConn = new DataConnection(cfgName, null);
		for (int t = 0; t < THREAD_COUNT; t++) {
			String tName = prefix + "_t" + t;
			DTTable table = DTTable.getJdbcTable(
					"SELECT count(*) cnt FROM mt_test WHERE thread_name='" + tName + "'", verifyConn);
			assertEquals(ROWS_PER_THREAD, table.getCell(0, "cnt").toInt(),
					"Thread " + tName + " should have " + ROWS_PER_THREAD + " rows");
		}
		verifyConn.close();
	}

	private static void shutdown(ExecutorService executor) {
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
