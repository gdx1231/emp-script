package com.gdxsoft.easyweb.define.database;

import java.util.HashMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.SqlUtils;

/**
 * 多数据库元数据提取器，支持 MySQL/SQL Server/PostgreSQL 等。
 * 提取表、视图、存储过程、函数、触发器、序列的定义信息。
 */
public class DbObjectExtractor {
	private static Logger LOGGER = LoggerFactory.getLogger(DbObjectExtractor.class);

	private String configName;
	private String databaseType;
	private String schemaName;

	public DbObjectExtractor(String configName) {
		this.configName = configName;
		DataConnection conn = new DataConnection();
		conn.setConfigName(configName);
		this.databaseType = conn.getCurrentConfig().getType();
		this.schemaName = conn.getCurrentConfig().getSchemaName();
	}

	/**
	 * 提取所有数据库对象
	 */
	public HashMap<String, DbSyncObject> extractAll() {
		HashMap<String, DbSyncObject> objects = new HashMap<>();
		extractTables(objects);
		extractViews(objects);
		extractProcedures(objects);
		extractFunctions(objects);
		extractTriggers(objects);
		extractSequences(objects);
		return objects;
	}

	/**
	 * 提取所有对象并返回 JSON
	 */
	public JSONObject extractAllToJson() {
		HashMap<String, DbSyncObject> objects = extractAll();
		JSONObject json = new JSONObject();
		for (DbSyncObject obj : objects.values()) {
			json.put(obj.getKey(), obj.toJson());
		}
		return json;
	}

	// ==================== 表/视图 ====================

	private void extractTables(HashMap<String, DbSyncObject> objects) {
		try {
			Tables tables = new Tables();
			tables.initTables(configName);
			for (String tableName : tables.getTableList()) {
				Table table = tables.get(tableName);
				if ("VIEW".equalsIgnoreCase(table.getTableType())) {
					continue;
				}
				table.init();
				String ddl = table.getSqlTable();
				if (ddl == null || ddl.trim().isEmpty()) {
					continue;
				}
				DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_TABLE, tableName, schemaName, ddl);
				objects.put(obj.getKey(), obj);
			}
		} catch (Exception e) {
			LOGGER.error("extractTables error: {}", e.getMessage());
		}
	}

	private void extractViews(HashMap<String, DbSyncObject> objects) {
		try {
			Tables tables = new Tables();
			tables.initTables(configName);
			for (String tableName : tables.getTableList()) {
				Table table = tables.get(tableName);
				if (!"VIEW".equalsIgnoreCase(table.getTableType())) {
					continue;
				}
				table.init();
				String ddl = table.getSqlTable();
				if (ddl == null || ddl.trim().isEmpty()) {
					continue;
				}
				DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_VIEW, tableName, schemaName, ddl);
				objects.put(obj.getKey(), obj);
			}
		} catch (Exception e) {
			LOGGER.error("extractViews error: {}", e.getMessage());
		}
	}

	// ==================== 存储过程 ====================

	private void extractProcedures(HashMap<String, DbSyncObject> objects) {
		try {
			if (SqlUtils.isMySql(databaseType)) {
				extractProceduresMySQL(objects);
			} else if (SqlUtils.isSqlServer(databaseType)) {
				extractProceduresSqlServer(objects);
			} else if (SqlUtils.isPostgreSql(databaseType)) {
				extractProceduresPostgreSQL(objects);
			}
		} catch (Exception e) {
			LOGGER.error("extractProcedures error: {}", e.getMessage());
		}
	}

	private void extractProceduresMySQL(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT ROUTINE_NAME FROM information_schema.ROUTINES "
				+ "WHERE ROUTINE_SCHEMA='" + escapeSql(schemaName) + "' AND ROUTINE_TYPE='PROCEDURE'";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "ROUTINE_NAME").toString();
			String ddl = getSingleDdlMySQL("PROCEDURE", name);
			if (ddl != null) {
				DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_PROCEDURE, name, schemaName, ddl);
				objects.put(obj.getKey(), obj);
			}
		}
	}

	private void extractProceduresSqlServer(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT p.name AS PROC_NAME, m.definition AS DDL "
				+ "FROM sys.procedures p INNER JOIN sys.sql_modules m ON p.object_id=m.object_id "
				+ "WHERE p.is_ms_shipped=0 ORDER BY p.name";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "PROC_NAME").toString();
			String ddl = tb.getCell(i, "DDL").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_PROCEDURE, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	private void extractProceduresPostgreSQL(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT p.proname AS name, pg_get_functiondef(p.oid) AS ddl "
				+ "FROM pg_proc p "
				+ "JOIN pg_namespace n ON p.pronamespace=n.oid "
				+ "WHERE n.nspname='" + escapeSql(schemaName) + "' "
				+ "AND p.prokind='p'";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "name").toString();
			String ddl = tb.getCell(i, "ddl").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_PROCEDURE, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	// ==================== 函数 ====================

	private void extractFunctions(HashMap<String, DbSyncObject> objects) {
		try {
			if (SqlUtils.isMySql(databaseType)) {
				extractFunctionsMySQL(objects);
			} else if (SqlUtils.isSqlServer(databaseType)) {
				extractFunctionsSqlServer(objects);
			} else if (SqlUtils.isPostgreSql(databaseType)) {
				extractFunctionsPostgreSQL(objects);
			}
		} catch (Exception e) {
			LOGGER.error("extractFunctions error: {}", e.getMessage());
		}
	}

	private void extractFunctionsMySQL(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT ROUTINE_NAME FROM information_schema.ROUTINES "
				+ "WHERE ROUTINE_SCHEMA='" + escapeSql(schemaName) + "' AND ROUTINE_TYPE='FUNCTION'";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "ROUTINE_NAME").toString();
			String ddl = getSingleDdlMySQL("FUNCTION", name);
			if (ddl != null) {
				DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_FUNCTION, name, schemaName, ddl);
				objects.put(obj.getKey(), obj);
			}
		}
	}

	private void extractFunctionsSqlServer(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT o.name AS FUNC_NAME, m.definition AS DDL "
				+ "FROM sys.sql_modules m INNER JOIN sys.objects o ON m.object_id=o.object_id "
				+ "WHERE o.type IN ('FN','IF','TF') AND o.is_ms_shipped=0 ORDER BY o.name";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "FUNC_NAME").toString();
			String ddl = tb.getCell(i, "DDL").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_FUNCTION, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	private void extractFunctionsPostgreSQL(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT p.proname AS name, pg_get_functiondef(p.oid) AS ddl "
				+ "FROM pg_proc p "
				+ "JOIN pg_namespace n ON p.pronamespace=n.oid "
				+ "WHERE n.nspname='" + escapeSql(schemaName) + "' "
				+ "AND p.prokind='f'";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "name").toString();
			String ddl = tb.getCell(i, "ddl").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_FUNCTION, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	// ==================== 触发器 ====================

	private void extractTriggers(HashMap<String, DbSyncObject> objects) {
		try {
			if (SqlUtils.isMySql(databaseType)) {
				extractTriggersMySQL(objects);
			} else if (SqlUtils.isSqlServer(databaseType)) {
				extractTriggersSqlServer(objects);
			} else if (SqlUtils.isPostgreSql(databaseType)) {
				extractTriggersPostgreSQL(objects);
			}
		} catch (Exception e) {
			LOGGER.error("extractTriggers error: {}", e.getMessage());
		}
	}

	private void extractTriggersMySQL(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT TRIGGER_NAME FROM information_schema.TRIGGERS "
				+ "WHERE TRIGGER_SCHEMA='" + escapeSql(schemaName) + "'";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "TRIGGER_NAME").toString();
			String ddl = getSingleDdlMySQL("TRIGGER", name);
			if (ddl != null) {
				DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_TRIGGER, name, schemaName, ddl);
				objects.put(obj.getKey(), obj);
			}
		}
	}

	private void extractTriggersSqlServer(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT t.name AS TRIG_NAME, OBJECT_NAME(t.parent_id) AS PARENT_TABLE, "
				+ "m.definition AS DDL, t.is_disabled "
				+ "FROM sys.triggers t INNER JOIN sys.sql_modules m ON t.object_id=m.object_id "
				+ "WHERE t.is_ms_shipped=0 ORDER BY t.name";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "TRIG_NAME").toString();
			String ddl = tb.getCell(i, "DDL").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_TRIGGER, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	private void extractTriggersPostgreSQL(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT t.tgname AS name, pg_get_triggerdef(t.oid) AS ddl "
				+ "FROM pg_trigger t "
				+ "JOIN pg_class c ON t.tgrelid=c.oid "
				+ "JOIN pg_namespace n ON c.relnamespace=n.oid "
				+ "WHERE n.nspname='" + escapeSql(schemaName) + "' "
				+ "AND NOT t.tgisinternal";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "name").toString();
			String ddl = tb.getCell(i, "ddl").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_TRIGGER, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	// ==================== 序列 ====================

	private void extractSequences(HashMap<String, DbSyncObject> objects) {
		try {
			if (SqlUtils.isMySql(databaseType)) {
				// MySQL 没有独立的序列对象
				return;
			} else if (SqlUtils.isSqlServer(databaseType)) {
				extractSequencesSqlServer(objects);
			} else if (SqlUtils.isPostgreSql(databaseType)) {
				extractSequencesPostgreSQL(objects);
			}
		} catch (Exception e) {
			LOGGER.error("extractSequences error: {}", e.getMessage());
		}
	}

	private void extractSequencesSqlServer(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT s.name AS SEQ_NAME, "
				+ "'CREATE SEQUENCE [' + s.name + '] START WITH ' + CAST(s.start_value AS VARCHAR) "
				+ "+ ' INCREMENT BY ' + CAST(s.increment AS VARCHAR) AS DDL "
				+ "FROM sys.sequences s ORDER BY s.name";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "SEQ_NAME").toString();
			String ddl = tb.getCell(i, "DDL").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_SEQUENCE, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	private void extractSequencesPostgreSQL(HashMap<String, DbSyncObject> objects) throws Exception {
		String sql = "SELECT sequence_name, "
				+ "'CREATE SEQUENCE \"' || sequence_name || '\" START WITH ' || start_value "
				+ "|| ' INCREMENT BY ' || increment "
				+ "AS ddl "
				+ "FROM information_schema.sequences "
				+ "WHERE sequence_schema='" + escapeSql(schemaName) + "' "
				+ "ORDER BY sequence_name";
		DTTable tb = DTTable.getJdbcTable(sql, configName);
		for (int i = 0; i < tb.getCount(); i++) {
			String name = tb.getCell(i, "sequence_name").toString();
			String ddl = tb.getCell(i, "ddl").toString();
			DbSyncObject obj = new DbSyncObject(DbSyncObject.TYPE_SEQUENCE, name, schemaName, ddl);
			objects.put(obj.getKey(), obj);
		}
	}

	// ==================== 辅助方法 ====================

	/**
	 * MySQL: 通过 SHOW CREATE 获取单个对象的 DDL
	 */
	private String getSingleDdlMySQL(String objectType, String name) {
		String sql = "SHOW CREATE " + objectType + " `" + schemaName + "`.`" + name.replace("`", "``") + "`";
		DataConnection conn = new DataConnection();
		conn.setConfigName(configName);
		try {
			conn.connect();
			if (!conn.executeQuery(sql)) {
				return null;
			}
			DTTable tb = new DTTable();
			tb.initData(conn.getLastResult().getResultSet());
			if (tb.getCount() == 0) {
				return null;
			}
			// SHOW CREATE 返回两列：Name, Create XXX
			// 取最后一列
			String colName = tb.getColumns().getColumn(tb.getColumns().getCount() - 1).getName();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tb.getCount(); i++) {
				sb.append(tb.getCell(i, colName).toString());
			}
			return sb.toString();
		} catch (Exception e) {
			LOGGER.error("getSingleDdlMySQL error for {} {}: {}", objectType, name, e.getMessage());
			return null;
		} finally {
			conn.close();
		}
	}

	private static String escapeSql(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("'", "''");
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getConfigName() {
		return configName;
	}
}
