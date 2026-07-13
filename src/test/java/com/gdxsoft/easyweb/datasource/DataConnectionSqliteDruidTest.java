package com.gdxsoft.easyweb.datasource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

/**
 * DataConnection tests using Druid connection pool with SQLite.
 * Mirrors DataConnectionSqliteTest (HikariCP) for non-transaction scenarios.
 *
 * Note: Transaction tests (transBegin/transCommit/transRollback) are omitted here
 * because Druid pool + sqlite-jdbc has a fundamental connection reuse issue —
 * after transBegin() sets autoCommit=false, the connection state is not properly
 * restored when returned to the Druid pool, causing subsequent operations to fail.
 * Transaction behavior is fully covered by DataConnectionSqliteTest (HikariCP).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataConnectionSqliteDruidTest {

	private static final String CONFIG_NAME = "sqlite_druid_test";
	private static final String DB_PATH = System.getProperty("java.io.tmpdir") + "/emp_script_test_druid.db";

	@BeforeAll
	static void setUp() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs configs = ConnectionConfigs.instance();

		ConnectionConfig cfg = new ConnectionConfig();
		cfg.setName(CONFIG_NAME);
		cfg.setType("SQLITE");
		cfg.setConnectionString(CONFIG_NAME);
		cfg.setSchemaName("main");

		MTableStr pool = new MTableStr();
		pool.put("driverClassName", "org.sqlite.JDBC");
		pool.put("url", "jdbc:sqlite:" + DB_PATH);
		pool.put("username", "sa");
		pool.put("password", "x");
		pool.put("poolType", "druid");
		pool.put("maxActive", "2");
		cfg.setPool(pool);

		configs.put(CONFIG_NAME, cfg);

		DataConnection conn = new DataConnection(CONFIG_NAME, null);
		conn.executeUpdateNoParameter("DROP TABLE IF EXISTS test_user");
		conn.executeUpdateNoParameter("CREATE TABLE test_user ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "name TEXT NOT NULL, "
				+ "age INTEGER, "
				+ "score REAL, "
				+ "created_at TEXT DEFAULT (datetime('now'))"
				+ ")");
		conn.close();
	}

	@AfterAll
	static void tearDown() {
		DataHelper.closeDataSource(CONFIG_NAME);
		new java.io.File(DB_PATH).delete();
	}

	private DataConnection createConnection() {
		return new DataConnection(CONFIG_NAME, null);
	}

	private DataConnection createConnection(RequestValue rv) {
		return new DataConnection(CONFIG_NAME, rv);
	}

	@Test
	@Order(1)
	void testConnect() {
		DataConnection conn = createConnection();
		assertTrue(conn.connect(), "SQLite/Druid connection should succeed");
		assertNull(conn.getErrorMsg());
		conn.close();
	}

	@Test
	@Order(2)
	void testInsertAndReturnAutoIncrement() {
		DataConnection conn = createConnection();
		String sql = "INSERT INTO test_user (name, age, score) VALUES ('Alice', 30, 95.5)";
		int autoId = conn.executeUpdateReturnAutoIncrement(sql);
		assertTrue(autoId > 0, "Auto-increment ID should be > 0, got: " + autoId);
		assertNull(conn.getErrorMsg());
		conn.close();
	}

	@Test
	@Order(3)
	void testInsertWithParameters() {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("name", "Bob");
		rv.addOrUpdateValue("age", 25);
		rv.addOrUpdateValue("score", 88.0);

		DataConnection conn = createConnection(rv);
		String sql = "INSERT INTO test_user (name, age, score) VALUES (@name, @age, @score)";
		int autoId = conn.executeUpdateReturnAutoIncrement(sql);
		assertTrue(autoId > 0, "Auto-increment ID should be > 0, got: " + autoId);
		assertNull(conn.getErrorMsg());
		conn.close();
	}

	@Test
	@Order(4)
	void testQueryNoParameter() throws Exception {
		DataConnection conn = createConnection();
		DTTable table = DTTable.getJdbcTable("SELECT * FROM test_user ORDER BY id", conn);
		assertTrue(table.isOk(), "Query should succeed: " + table.getErrorInfo());
		assertEquals(2, table.getCount(), "Should have 2 rows");
		assertEquals("Alice", table.getCell(0, "name").toString());
		assertEquals("Bob", table.getCell(1, "name").toString());
		conn.close();
	}

	@Test
	@Order(5)
	void testQueryWithParameters() throws Exception {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("name", "Alice");

		DataConnection conn = createConnection(rv);
		DTTable table = DTTable.getJdbcTable("SELECT * FROM test_user WHERE name = @name", conn);
		assertTrue(table.isOk(), "Parameterized query should succeed: " + table.getErrorInfo());
		assertEquals(1, table.getCount());
		assertEquals(30, table.getCell(0, "age").toInt());
		conn.close();
	}

	@Test
	@Order(6)
	void testUpdateWithParameters() throws Exception {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("age", 31);
		rv.addOrUpdateValue("name", "Alice");

		DataConnection conn = createConnection(rv);
		boolean ok = conn.executeUpdate("UPDATE test_user SET age = @age WHERE name = @name");
		assertTrue(ok, "Update should succeed");
		assertNull(conn.getErrorMsg());
		conn.close();

		RequestValue rv2 = new RequestValue();
		rv2.addOrUpdateValue("name", "Alice");
		DataConnection conn2 = createConnection(rv2);
		DTTable table = DTTable.getJdbcTable("SELECT age FROM test_user WHERE name = @name", conn2);
		assertEquals(31, table.getCell(0, "age").toInt());
		conn2.close();
	}

	@Test
	@Order(7)
	void testDeleteWithParameters() throws Exception {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("name", "Bob");

		DataConnection conn = createConnection(rv);
		boolean ok = conn.executeUpdate("DELETE FROM test_user WHERE name = @name");
		assertTrue(ok, "Delete should succeed");
		assertNull(conn.getErrorMsg());
		conn.close();

		DataConnection conn2 = createConnection();
		DTTable table = DTTable.getJdbcTable("SELECT count(*) cnt FROM test_user", conn2);
		assertEquals(1, table.getCell(0, "cnt").toInt());
		conn2.close();
	}

	@Test
	@Order(8)
	void testQueryCount() {
		DataConnection conn = createConnection();
		int count = conn.executeQueryCount("test_user", "1=1");
		assertEquals(1, count);
		conn.close();
	}

	@Test
	@Order(9)
	void testQueryExists() {
		DataConnection conn = createConnection();
		boolean exists = conn.executeQueryExists("test_user", "name='Alice'");
		assertTrue(exists, "Alice should exist");

		boolean notExists = conn.executeQueryExists("test_user", "name='Nobody'");
		assertFalse(notExists, "Nobody should not exist");
		conn.close();
	}

	@Test
	@Order(10)
	void testStaticUpdateAndClose() {
		RequestValue rv = new RequestValue();
		rv.addOrUpdateValue("name", "StaticUser");
		rv.addOrUpdateValue("age", 35);
		rv.addOrUpdateValue("score", 77.7);

		String sql = "INSERT INTO test_user (name, age, score) VALUES (@name, @age, @score)";
		String err = DataConnection.updateAndClose(sql, CONFIG_NAME, rv);
		assertNull(err, "updateAndClose should succeed without error: " + err);

		DataConnection conn = createConnection();
		int count = conn.executeQueryCount("test_user", "name='StaticUser'");
		assertEquals(1, count);
		conn.close();
	}

	@Test
	@Order(11)
	void testBatchUpdate() {
		DataConnection conn = createConnection();
		List<String> sqls = Arrays.asList(
				"INSERT INTO test_user (name, age, score) VALUES ('Batch1', 20, 50.0)",
				"INSERT INTO test_user (name, age, score) VALUES ('Batch2', 21, 51.0)",
				"INSERT INTO test_user (name, age, score) VALUES ('Batch3', 22, 52.0)"
		);
		int result = conn.executeUpdateBatch(sqls);
		assertTrue(result >= 0, "Batch update should succeed, got: " + result);
		assertNull(conn.getErrorMsg());

		int count = conn.executeQueryCount("test_user", "name LIKE 'Batch%'");
		assertEquals(3, count);
		conn.close();
	}

	@Test
	@Order(12)
	void testGetJdbcTableViaConnection() throws Exception {
		DataConnection conn = createConnection();
		DTTable table = DTTable.getJdbcTable("SELECT name, age FROM test_user WHERE age >= 25 ORDER BY age", conn);
		assertTrue(table.isOk(), "DTTable query should succeed: " + table.getErrorInfo());
		assertTrue(table.getCount() >= 2, "Should have at least 2 rows with age >= 25");
		conn.close();
	}
}
