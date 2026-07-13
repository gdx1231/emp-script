package com.gdxsoft.easyweb.datasource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

/**
 * Verify ConnectionConfig.resolvePath() substitutes ~, @temp, @cwd
 * in pool URLs, and that DataConnection works with each resolved path.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataConnectionSqlitePathTest {

	private static final String HOME_DB = System.getProperty("user.home") + "/emp_path_test_home.db";
	private static final String TEMP_DB = System.getProperty("java.io.tmpdir") + "/emp_path_test_temp.db";
	private static final String CWD_DB = System.getProperty("user.dir") + "/emp_path_test_cwd.db";

	@AfterAll
	static void tearDown() {
		DataHelper.closeDataSource("path_home");
		DataHelper.closeDataSource("path_temp");
		DataHelper.closeDataSource("path_cwd");
		new File(HOME_DB).delete();
		new File(TEMP_DB).delete();
		new File(CWD_DB).delete();
	}

	@Test
	@Order(1)
	void testResolvePathStaticMethod() {
		String home = System.getProperty("user.home");
		String temp = System.getProperty("java.io.tmpdir");
		String cwd = System.getProperty("user.dir");

		assertEquals(home + "/data/test.db", ConnectionConfig.resolvePath("~/data/test.db"));
		assertEquals(home + "/data/test.db", ConnectionConfig.resolvePath("@home/data/test.db"));
		assertEquals(temp + "/test.db", ConnectionConfig.resolvePath("@temp/test.db"));
		assertEquals(cwd + "/test.db", ConnectionConfig.resolvePath("@cwd/test.db"));
		assertEquals(cwd + "/test.db", ConnectionConfig.resolvePath("@pwd/test.db"));

		// combined
		assertEquals("jdbc:sqlite:" + home + "/test.db",
				ConnectionConfig.resolvePath("jdbc:sqlite:~/test.db"));
		assertEquals("jdbc:sqlite:" + temp + "/test.db",
				ConnectionConfig.resolvePath("jdbc:sqlite:@temp/test.db"));

		// no markers — unchanged
		assertEquals("jdbc:mysql://localhost:3306/db",
				ConnectionConfig.resolvePath("jdbc:mysql://localhost:3306/db"));

		// null safe
		assertNull(ConnectionConfig.resolvePath(null));
	}

	@Test
	@Order(2)
	void testTildePathViaSetPool() throws Exception {
		String cfgName = "path_home";
		registerAndTest(cfgName, "~/emp_path_test_home.db", HOME_DB);
	}

	@Test
	@Order(3)
	void testTempPathViaSetPool() throws Exception {
		String cfgName = "path_temp";
		registerAndTest(cfgName, "@temp/emp_path_test_temp.db", TEMP_DB);
	}

	@Test
	@Order(4)
	void testCwdPathViaSetPool() throws Exception {
		String cfgName = "path_cwd";
		registerAndTest(cfgName, "@cwd/emp_path_test_cwd.db", CWD_DB);
	}

	/**
	 * Register a SQLite config using a path expression in the URL,
	 * verify the URL was resolved, then do a round-trip insert + query.
	 */
	private void registerAndTest(String cfgName, String pathExpr, String expectedResolvedPath) throws Exception {
		ConnectionConfigs configs = ConnectionConfigs.instance();

		ConnectionConfig cfg = new ConnectionConfig();
		cfg.setName(cfgName);
		cfg.setType("SQLITE");
		cfg.setConnectionString(cfgName);
		cfg.setSchemaName("main");

		MTableStr pool = new MTableStr();
		pool.put("driverClassName", "org.sqlite.JDBC");
		pool.put("url", "jdbc:sqlite:" + pathExpr);
		pool.put("username", "sa");
		pool.put("password", "");
		pool.put("maxActive", "1");
		cfg.setPool(pool);

		// verify the URL was resolved by setPool
		String actualUrl = cfg.getPool().get("url");
		assertEquals("jdbc:sqlite:" + expectedResolvedPath, actualUrl,
				"URL should be resolved: " + actualUrl);

		configs.put(cfgName, cfg);

		// round-trip: create table, insert, query
		DataConnection conn = new DataConnection(cfgName, null);
		conn.executeUpdateNoParameter("DROP TABLE IF EXISTS path_test");
		conn.executeUpdateNoParameter("CREATE TABLE path_test (id INTEGER PRIMARY KEY, val TEXT)");
		conn.executeUpdateNoParameter("INSERT INTO path_test (val) VALUES ('hello')");

		DTTable table = DTTable.getJdbcTable("SELECT val FROM path_test", conn);
		assertTrue(table.isOk(), "Query should succeed: " + table.getErrorInfo());
		assertEquals(1, table.getCount());
		assertEquals("hello", table.getCell(0, "val").toString());
		conn.close();

		// verify the DB file was actually created at the resolved path
		assertTrue(new File(expectedResolvedPath).exists(),
				"DB file should exist at resolved path: " + expectedResolvedPath);
	}
}
