package com.gdxsoft.easyweb.define;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.define.database.DbObjectExtractor;
import com.gdxsoft.easyweb.define.database.DbSyncDiff;
import com.gdxsoft.easyweb.define.database.DbSyncObject;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UNet;

/**
 * 数据库结构同步主入口，参考 SyncRemote 的模式。
 * 支持本地对比和远程对比两种方式。
 *
 * 同步流程：
 * 1. 提取源库 schema 对象（表、视图、存储过程、函数、触发器、序列）
 * 2. 提取目标库 schema 对象
 * 3. 对比差异
 * 4. 生成同步 SQL
 * 5. 可选：执行同步 SQL 到目标库
 */
public class SyncDatabase {
	private static Logger LOGGER = LoggerFactory.getLogger(SyncDatabase.class);

	private String sourceConnName;
	private String targetConnName;
	private String remoteUrl;
	private String aesCode;

	private JSONObject sourceSchema;
	private JSONObject targetSchema;

	public SyncDatabase() {
	}

	/**
	 * 设置源和目标数据库连接名
	 */
	public SyncDatabase(String sourceConnName, String targetConnName) {
		this.sourceConnName = sourceConnName;
		this.targetConnName = targetConnName;
	}

	/**
	 * 设置远程同步参数（参考 SyncRemote 的远程通信模式）
	 */
	public SyncDatabase(String sourceConnName, String targetConnName, String remoteUrl, String aesCode) {
		this.sourceConnName = sourceConnName;
		this.targetConnName = targetConnName;
		this.remoteUrl = remoteUrl;
		this.aesCode = aesCode;
	}

	// ==================== 本地对比模式 ====================

	/**
	 * 提取源库 schema
	 */
	public JSONObject extractSource() {
		DbObjectExtractor extractor = new DbObjectExtractor(sourceConnName);
		this.sourceSchema = extractor.extractAllToJson();
		return this.sourceSchema;
	}

	/**
	 * 提取目标库 schema
	 */
	public JSONObject extractTarget() {
		DbObjectExtractor extractor = new DbObjectExtractor(targetConnName);
		this.targetSchema = extractor.extractAllToJson();
		return this.targetSchema;
	}

	/**
	 * 本地对比：提取两个库的 schema 并比较
	 */
	public List<DbSyncDiff> compareLocal() {
		if (this.sourceSchema == null) {
			extractSource();
		}
		if (this.targetSchema == null) {
			extractTarget();
		}
		return doCompare(sourceSchema, targetSchema);
	}

	// ==================== 远程对比模式 ====================

	/**
	 * 远程对比：提取源库 schema，POST 到远程，远程提取目标库并对比，返回差异
	 */
	public String compareRemote() throws Exception {
		if (this.sourceSchema == null) {
			extractSource();
		}

		JSONObject paras = new JSONObject();
		paras.put("method", "compare");
		paras.put("sourceConn", sourceConnName);
		paras.put("targetConn", targetConnName);
		paras.put("sourceSchema", sourceSchema);

		String encoded = encrypt(paras.toString());

		HashMap<String, String> vals = new HashMap<>();
		vals.put("method", "compare");
		vals.put("GDX", encoded);

		UNet net = getNet();
		String rstEncoded = net.doPost(remoteUrl, vals);
		return decrypt(rstEncoded);
	}

	/**
	 * 远程执行：在目标库执行同步 SQL
	 */
	public String executeRemote(String syncSql) throws Exception {
		JSONObject paras = new JSONObject();
		paras.put("method", "execute");
		paras.put("targetConn", targetConnName);
		paras.put("sql", syncSql);

		String encoded = encrypt(paras.toString());

		HashMap<String, String> vals = new HashMap<>();
		vals.put("method", "execute");
		vals.put("GDX", encoded);

		UNet net = getNet();
		String rstEncoded = net.doPost(remoteUrl, vals);
		return decrypt(rstEncoded);
	}

	// ==================== 对比逻辑 ====================

	/**
	 * 对比两个 schema JSON，返回差异列表
	 */
	public static List<DbSyncDiff> doCompare(JSONObject sourceJson, JSONObject targetJson) {
		List<DbSyncDiff> diffs = new ArrayList<>();
		HashMap<String, Boolean> visited = new HashMap<>();

		// 遍历源库对象
		Iterator<?> sourceKeys = sourceJson.keys();
		while (sourceKeys.hasNext()) {
			String key = sourceKeys.next().toString();
			visited.put(key, true);

			JSONObject sourceObjJson = sourceJson.getJSONObject(key);
			DbSyncObject sourceObj = DbSyncObject.fromJson(sourceObjJson);

			DbSyncObject targetObj = null;
			if (targetJson.has(key)) {
				JSONObject targetObjJson = targetJson.getJSONObject(key);
				targetObj = DbSyncObject.fromJson(targetObjJson);
			}

			DbSyncDiff diff = new DbSyncDiff(sourceObj, targetObj);
			if (!DbSyncDiff.SAME.equals(diff.getStatus())) {
				diff.setSyncSql(generateSyncSql(diff, sourceObj));
				diffs.add(diff);
			}
		}

		// 遍历目标库中独有的对象（源库没有的）
		Iterator<?> targetKeys = targetJson.keys();
		while (targetKeys.hasNext()) {
			String key = targetKeys.next().toString();
			if (visited.containsKey(key)) {
				continue;
			}
			JSONObject targetObjJson = targetJson.getJSONObject(key);
			DbSyncObject targetObj = DbSyncObject.fromJson(targetObjJson);

			DbSyncDiff diff = new DbSyncDiff(null, targetObj);
			diff.setSyncSql(generateSyncSql(diff, null));
			diffs.add(diff);
		}

		return diffs;
	}

	/**
	 * 生成同步 SQL
	 */
	private static String generateSyncSql(DbSyncDiff diff, DbSyncObject sourceObj) {
		String status = diff.getStatus();
		String objectType = diff.getObjectType();

		if (DbSyncDiff.SOURCE_ONLY.equals(status)) {
			// 源库有，目标库没有 → CREATE
			if (DbSyncObject.TYPE_TABLE.equals(objectType)) {
				return sourceObj.getDdl() + ";";
			}
			return "DROP " + getDropType(objectType) + " IF EXISTS " + quoteName(objectType, diff) + ";\n"
					+ sourceObj.getDdl() + ";";
		}

		if (DbSyncDiff.DIFFERENT.equals(status)) {
			// 两边都有但不同 → DROP + CREATE
			if (DbSyncObject.TYPE_TABLE.equals(objectType)) {
				// 表结构变更：使用 ALTER（简化处理，实际可能需要更复杂的逻辑）
				return "-- Table altered, manual review recommended\n"
						+ "-- DROP TABLE " + quoteName(objectType, diff) + ";\n"
						+ sourceObj.getDdl() + ";";
			}
			return "DROP " + getDropType(objectType) + " IF EXISTS " + quoteName(objectType, diff) + ";\n"
					+ sourceObj.getDdl() + ";";
		}

		if (DbSyncDiff.TARGET_ONLY.equals(status)) {
			// 目标库有，源库没有 → 可选 DROP
			return "-- Object only in target: " + diff.getObjectName() + "\n"
					+ "-- DROP " + getDropType(objectType) + " IF EXISTS " + quoteName(objectType, diff) + ";";
		}

		return null;
	}

	private static String getDropType(String objectType) {
		switch (objectType) {
			case DbSyncObject.TYPE_TABLE:
				return "TABLE";
			case DbSyncObject.TYPE_VIEW:
				return "VIEW";
			case DbSyncObject.TYPE_PROCEDURE:
				return "PROCEDURE";
			case DbSyncObject.TYPE_FUNCTION:
				return "FUNCTION";
			case DbSyncObject.TYPE_TRIGGER:
				return "TRIGGER";
			case DbSyncObject.TYPE_SEQUENCE:
				return "SEQUENCE";
			default:
				return "OBJECT";
		}
	}

	private static String quoteName(String objectType, DbSyncDiff diff) {
		String name = diff.getObjectName();
		// MySQL 使用反引号，SQL Server 使用方括号，PG 使用双引号
		// 这里使用通用格式，实际执行时根据数据库类型调整
		return name;
	}

	// ==================== 在目标库执行 SQL ====================

	/**
	 * 在目标库执行同步 SQL（本地模式）
	 */
	public String executeLocal(String syncSql) {
		DataConnection conn = new DataConnection();
		conn.setConfigName(targetConnName);
		try {
			conn.connect();
			// 分段执行（以 ; 分隔，跳过注释行）
			String[] sqls = syncSql.split(";");
			StringBuilder executed = new StringBuilder();
			for (String sql : sqls) {
				String trimmed = sql.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("--")) {
					continue;
				}
				conn.executeUpdateNoParameter(trimmed);
				if (conn.getErrorMsg() != null) {
					return "{\"RST\":false,\"ERR\":\"" + conn.getErrorMsg() + "\"}";
				}
				executed.append(trimmed).append(";\n");
			}
			return "{\"RST\":true,\"MSG\":\"OK\",\"EXECUTED\":\"" + executed.toString().replace("\"", "\\\"") + "\"}";
		} catch (Exception e) {
			return "{\"RST\":false,\"ERR\":\"" + e.getMessage() + "\"}";
		} finally {
			conn.close();
		}
	}

	// ==================== 列出可用数据库连接 ====================

	/**
	 * 列出所有可用的数据库连接配置
	 */
	public static JSONObject listConnections() {
		JSONObject result = new JSONObject();
		try {
			ConnectionConfigs configs = ConnectionConfigs.instance();
			for (String name : configs.getListNames()) {
				ConnectionConfig cfg = configs.get(name);
				JSONObject obj = new JSONObject();
				obj.put("name", cfg.getName());
				obj.put("type", cfg.getType());
				obj.put("schemaName", cfg.getSchemaName());
				result.put(name, obj);
			}
		} catch (Exception e) {
			LOGGER.error("listConnections error: {}", e.getMessage());
		}
		return result;
	}

	// ==================== 加密/解密 ====================

	private String encrypt(String text) throws Exception {
		UAes aes = new UAes();
		aes.setCipherName(UAes.AES_128_CBC);
		aes.setPaddingMethod(UAes.NoPadding);
		aes.setUsingBc(false);
		aes.createKey(aesCode.getBytes("utf-8"));
		return aes.encrypt(text);
	}

	private String decrypt(String text) throws Exception {
		UAes aes = new UAes();
		aes.setCipherName(UAes.AES_128_CBC);
		aes.setPaddingMethod(UAes.NoPadding);
		aes.setUsingBc(false);
		aes.createKey(aesCode.getBytes("utf-8"));
		return aes.decrypt(text);
	}

	private UNet getNet() {
		UNet net = new UNet();
		net.setIsShowLog(false);
		net.setEncode("utf-8");
		return net;
	}

	// ==================== getters / setters ====================

	public String getSourceConnName() {
		return sourceConnName;
	}

	public void setSourceConnName(String sourceConnName) {
		this.sourceConnName = sourceConnName;
	}

	public String getTargetConnName() {
		return targetConnName;
	}

	public void setTargetConnName(String targetConnName) {
		this.targetConnName = targetConnName;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public String getAesCode() {
		return aesCode;
	}

	public void setAesCode(String aesCode) {
		this.aesCode = aesCode;
	}

	public JSONObject getSourceSchema() {
		return sourceSchema;
	}

	public JSONObject getTargetSchema() {
		return targetSchema;
	}
}
