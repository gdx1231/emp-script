package com.gdxsoft.easyweb.datasource;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLWarning;
import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

class DataConnectionTest {

	private DataConnection dc;

	@BeforeEach
	void setUp() {
		dc = new DataConnection();
	}

	// ======================== getSqls ========================

	@Test
	void testGetSqlsSimple() {
		List<String> list = DataConnection.getSqls("SELECT 1; SELECT 2");
		assertEquals(2, list.size());
		assertEquals("SELECT 1", list.get(0));
		assertEquals("SELECT 2", list.get(1));
	}

	@Test
	void testGetSqlsSingleSql() {
		List<String> list = DataConnection.getSqls("SELECT 1");
		assertEquals(1, list.size());
		assertEquals("SELECT 1", list.get(0));
	}

	@Test
	void testGetSqlsEmptyPartsSkipped() {
		List<String> list = DataConnection.getSqls("SELECT 1;  ;;SELECT 2;");
		assertEquals(2, list.size(), "empty parts should be skipped");
		assertEquals("SELECT 1", list.get(0));
		assertEquals("SELECT 2", list.get(1));
	}

	@Test
	void testGetSqlsWhitespaceOnlyPartsSkipped() {
		List<String> list = DataConnection.getSqls("SELECT 1;   ; SELECT 2");
		assertEquals(2, list.size());
	}

	@Test
	void testGetSqlsEmptyString() {
		List<String> list = DataConnection.getSqls("");
		assertTrue(list.isEmpty());
	}

	// ======================== checkIsProcdure ========================

	@Test
	void testCheckIsProcdureValid() {
		assertTrue(DataConnection.checkIsProcdure("CALL pr_test(@a)"));
		assertTrue(DataConnection.checkIsProcdure("call pr_test(@a)"));
	}

	@Test
	void testCheckIsProcdureNotProcedure() {
		assertFalse(DataConnection.checkIsProcdure("SELECT 1"));
		assertFalse(DataConnection.checkIsProcdure("INSERT INTO t VALUES(1)"));
	}

	@Test
	void testCheckIsProcdureWithLeadingWhitespace() {
		assertTrue(DataConnection.checkIsProcdure("  \n CALL pr_test(@a)"));
	}

	@Test
	void testCheckIsProcdureOnlyCallNotPrefix() {
		assertFalse(DataConnection.checkIsProcdure("RECALL something"));
	}

	// ======================== checkIsSelect ========================

	@Test
	void testCheckIsSelectTrue() {
		assertTrue(DataConnection.checkIsSelect("SELECT * FROM t"));
	}

	@Test
	void testCheckIsSelectFalseForUpdate() {
		assertFalse(DataConnection.checkIsSelect("UPDATE t SET x=1"));
	}

	@Test
	void testCheckIsSelectFalseForInsert() {
		assertFalse(DataConnection.checkIsSelect("INSERT INTO t VALUES(1)"));
	}

	@Test
	void testCheckIsSelectEwaHint() {
		// EWA_IS_SELECT must appear as a non-comment keyword
		assertTrue(DataConnection.checkIsSelect("EWA_IS_SELECT\nUPDATE t SET x=1"));
	}

	// ======================== checkStartWord ========================

	@Test
	void testCheckStartWordTrue() {
		assertTrue(DataConnection.checkStartWord("SELECT 1", "SELECT"));
	}

	@Test
	void testCheckStartWordFalse() {
		assertFalse(DataConnection.checkStartWord("INSERT INTO t", "SELECT"));
	}

	@Test
	void testCheckStartWordLeadingWhitespace() {
		assertTrue(DataConnection.checkStartWord("  \n\tSELECT 1", "SELECT"));
	}

	// ======================== removeSqlMuitiComment ========================

	@Test
	void testRemoveSqlMuitiCommentSimple() {
		String result = DataConnection.removeSqlMuitiComment("SELECT 1 /* comment */ FROM t");
		assertFalse(result.contains("comment"), "comment should be removed");
		assertTrue(result.contains("SELECT 1"), "SQL should remain");
	}

	@Test
	void testRemoveSqlMuitiCommentNoComments() {
		String sql = "SELECT 1 FROM t";
		assertEquals(sql, DataConnection.removeSqlMuitiComment(sql));
	}

	@Test
	void testRemoveSqlMuitiCommentMultipleBlocks() {
		String result = DataConnection.removeSqlMuitiComment("SELECT /*a*/ 1 /*b*/ FROM /*c*/ t");
		assertFalse(result.contains("/*"), "all comments should be removed");
	}

	// ======================== isComparativeChanges ========================

	@Test
	void testIsComparativeChangesTrue() {
		// Marker must be on its own line: -- COMPARATIVE_CHANGES
		assertTrue(DataConnection.isComparativeChanges("UPDATE t SET x=1\n-- COMPARATIVE_CHANGES"));
	}

	@Test
	void testIsComparativeChangesFalse() {
		assertFalse(DataConnection.isComparativeChanges("UPDATE t SET x=1"));
	}

	// ======================== getAutoField ========================

	@Test
	void testGetAutoFieldNullForRegularSql() {
		assertNull(DataConnection.getAutoField("SELECT * FROM t"));
	}

	@Test
	void testGetAutoFieldNullForEmpty() {
		assertNull(DataConnection.getAutoField(""));
	}

	// ======================== sqlParameterStringExp ========================

	@Test
	void testSqlParameterStringExpNull() {
		assertEquals("NULL", dc.sqlParameterStringExp(null));
		assertEquals("NULL", dc.sqlParameterStringExp(null, "MYSQL"));
		assertEquals("NULL", dc.sqlParameterStringExp(null, "MSSQL"));
	}

	@Test
	void testSqlParameterStringExpEmpty() {
		assertEquals("''", dc.sqlParameterStringExp(""));
	}

	@Test
	void testSqlParameterStringExpNormal() {
		String result = dc.sqlParameterStringExp("hello");
		assertTrue(result.startsWith("'"));
		assertTrue(result.endsWith("'"));
	}

	@Test
	void testSqlParameterStringExpSingleQuoteEscape() {
		String result = dc.sqlParameterStringExp("it's");
		assertTrue(result.contains("''"), "single quote should be doubled");
		assertTrue(result.contains("'it''s'"));
	}

	@Test
	void testSqlParameterStringExpMysqlBackslashEscape() {
		String result = dc.sqlParameterStringExp("a\\b", "MYSQL");
		assertTrue(result.contains("\\\\"), "backslash should be doubled for MySQL");
	}

	@Test
	void testSqlParameterStringExpSqlServerUnicodePrefix() {
		String result = dc.sqlParameterStringExp("hello", "MSSQL");
		assertTrue(result.startsWith("N'"), "SQL Server should prefix with N");
	}

	@Test
	void testSqlParameterStringExpNullDatabaseType() {
		String result = dc.sqlParameterStringExp("hello", null);
		assertTrue(result.startsWith("'"));
		assertTrue(result.endsWith("'"));
		assertFalse(result.startsWith("N'"));
	}

	// ======================== sqlFieldOrTableExp ========================

	@Test
	void testSqlFieldOrTableExpMysqlUsesBacktick() {
		setDbType("MYSQL");
		assertEquals("`my_table`", dc.sqlFieldOrTableExp("my_table"));
	}

	@Test
	void testSqlFieldOrTableExpSqlServerUsesBrackets() {
		setDbType("MSSQL");
		assertEquals("[my_table]", dc.sqlFieldOrTableExp("my_table"));
	}

	@Test
	void testSqlFieldOrTableExpOtherDbNoDecoration() {
		setDbType("HSQLDB");
		assertEquals("my_table", dc.sqlFieldOrTableExp("my_table"));
	}

	// ======================== getDateTimePara ========================

	@Test
	void testGetDateTimeParaStringNull() {
		setDbType("MYSQL");
		assertNull(dc.getDateTimePara((String) null));
		assertNull(dc.getDateTimePara(""));
		assertNull(dc.getDateTimePara("  "));
	}

	@Test
	void testGetDateTimeParaTimestampNull() {
		setDbType("MYSQL");
		assertNull(dc.getDateTimePara((Timestamp) null));
	}

	@Test
	void testGetDateTimeParaTimestampMysql() {
		setDbType("MYSQL");
		Timestamp ts = Timestamp.valueOf("2024-01-15 10:30:00");
		String result = dc.getDateTimePara(ts);
		assertTrue(result.contains("2024-01-15"));
		assertFalse(result.toUpperCase().contains("TO_DATE"), "MySQL should not use to_date");
	}

	@Test
	void testGetDateTimeParaTimestampOracle() {
		setDbType("ORACLE");
		Timestamp ts = Timestamp.valueOf("2024-01-15 10:30:00");
		String result = dc.getDateTimePara(ts);
		assertTrue(result.toUpperCase().contains("TO_DATE"), "Oracle should use to_date");
	}

	// ======================== error management ========================

	@Test
	void testErrorMsgInitiallyNull() {
		assertNull(dc.getErrorMsg());
		assertNull(dc.getErrorMsgOnly());
	}

	@Test
	void testSetAndClearErrorMsg() {
		dc.setErrorMsg("test error");
		assertEquals("test error", dc.getErrorMsg());
		assertEquals("test error", dc.getErrorMsgOnly());

		dc.clearErrorMsg();
		assertNull(dc.getErrorMsg());
		assertNull(dc.getErrorMsgOnly());
	}

	// ======================== getConnection null safety ========================

	@Test
	void testGetConnectionNullDsReturnsNull() {
		assertNull(dc.getDataHelper(), "_ds should be null for empty constructor");
		assertNull(dc.getConnection(), "getConnection should return null when _ds is null");
	}

	// ======================== getUpdateBatchTables ========================

	@Test
	void testGetUpdateBatchTablesInitiallyNull() {
		assertNull(dc.getUpdateBatchTables());
	}

	// ======================== isTrans ========================

	@Test
	void testIsTransInitiallyFalse() {
		assertFalse(dc.isTrans());
	}

	// ======================== timeDiffMinutes getter/setter ========================

	@Test
	void testTimeDiffMinutesGetterSetter() {
		assertEquals(0, dc.getTimeDiffMinutes());
		dc.setTimeDiffMinutes(30);
		assertEquals(30, dc.getTimeDiffMinutes());
		dc.setTimeDiffMinutes(-60);
		assertEquals(-60, dc.getTimeDiffMinutes());
	}

	// ======================== resultSetList ========================

	@Test
	void testResultSetListInitiallyEmpty() {
		assertNotNull(dc.getResultSetList());
		assertEquals(0, dc.getResultSetList().size());
	}

	// ======================== close without connection ========================

	@Test
	void testCloseWithoutConnectionDoesNotThrow() {
		assertDoesNotThrow(() -> dc.close(), "close() should not throw when _ds is null");
		assertNull(dc.getUpdateBatchTables(), "updateBatchTables should be cleared");
		assertFalse(dc.isTrans(), "_IsTrans should be false");
	}

	// ======================== constructor never throws ========================

	@Test
	void testNoArgsConstructorNeverThrows() {
		assertDoesNotThrow(() -> new DataConnection());
		assertDoesNotThrow(() -> new DataConnection((com.gdxsoft.easyweb.script.RequestValue) null));
		assertDoesNotThrow(() -> new DataConnection("nonexistent_db", null));
	}

	// ======================== getParaInteger (parameter expansion) ========================

	@Test
	void testGetReplaceParameterValueExpNullDatabaseType() {
		com.gdxsoft.easyweb.script.RequestValue rv = new com.gdxsoft.easyweb.script.RequestValue();
		dc.setRequestValue(rv);
		String result = dc.getReplaceParameterValueExp("nonexistent_param", (String) null);
		assertEquals("null", result, "missing param should return 'null' string");
	}

	// ================= Helper ========================

	private void setDbType(String type) {
		ConnectionConfig cfg = new ConnectionConfig();
		cfg.setName("test");
		cfg.setType(type);
		cfg.setConnectionString("test");
		MTableStr pool = new MTableStr();
		pool.put("driverClassName", "org.hsqldb.jdbc.JDBCDriver");
		pool.put("url", "jdbc:hsqldb:mem:test");
		pool.put("username", "sa");
		pool.put("password", "");
		cfg.setPool(pool);
		dc.setCurrentConfig(cfg);
	}
}
