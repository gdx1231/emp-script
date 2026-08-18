package com.gdxsoft.easyweb.define.database;

import java.security.MessageDigest;

import org.json.JSONObject;

/**
 * 数据库对象模型，表示一个表、视图、存储过程、函数、触发器或序列
 */
public class DbSyncObject {

	public static final String TYPE_TABLE = "TABLE";
	public static final String TYPE_VIEW = "VIEW";
	public static final String TYPE_PROCEDURE = "PROCEDURE";
	public static final String TYPE_FUNCTION = "FUNCTION";
	public static final String TYPE_TRIGGER = "TRIGGER";
	public static final String TYPE_SEQUENCE = "SEQUENCE";

	private String objectType;
	private String objectName;
	private String schemaName;
	private String ddl;
	private String md5;

	public DbSyncObject() {
	}

	public DbSyncObject(String objectType, String objectName, String schemaName, String ddl) {
		this.objectType = objectType;
		this.objectName = objectName;
		this.schemaName = schemaName;
		this.ddl = ddl;
		this.md5 = calcMd5(normalizeDdl(ddl));
	}

	/**
	 * 标准化 DDL 文本（去除多余空白），用于对比
	 */
	private static String normalizeDdl(String ddl) {
		if (ddl == null) {
			return "";
		}
		return ddl.replaceAll("\\s+", " ").trim();
	}

	private static String calcMd5(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(text.getBytes("utf-8"));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (Exception e) {
			return text.hashCode() + "";
		}
	}

	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		obj.put("objectType", objectType);
		obj.put("objectName", objectName);
		if (schemaName != null) {
			obj.put("schemaName", schemaName);
		}
		if (ddl != null) {
			obj.put("ddl", ddl);
		}
		if (md5 != null) {
			obj.put("md5", md5);
		}
		return obj;
	}

	public static DbSyncObject fromJson(JSONObject json) {
		DbSyncObject obj = new DbSyncObject();
		obj.objectType = json.optString("objectType");
		obj.objectName = json.optString("objectName");
		obj.schemaName = json.optString("schemaName");
		obj.ddl = json.optString("ddl");
		obj.md5 = json.optString("md5");
		return obj;
	}

	public String getKey() {
		return objectType + "::" + (schemaName == null ? "" : schemaName + ".") + objectName;
	}

	// ---- getters / setters ----

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getDdl() {
		return ddl;
	}

	public void setDdl(String ddl) {
		this.ddl = ddl;
		this.md5 = calcMd5(normalizeDdl(ddl));
	}

	public String getMd5() {
		return md5;
	}
}
