package com.gdxsoft.easyweb.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;

/**
 * 创建EWA_SPLIT的临时数据
 * 
 * @author admin
 *
 */
public class CreateSplitData {
	public static String DDL_SQLSERVER = "CREATE TABLE _ewa_spt_data(idx int NOT NULL,col nvarchar(MAX), tag varchar(50) NOT NULL, PRIMARY KEY (tag,idx) )";
	public static String DDL_MYSQL = "CREATE TABLE _ewa_spt_data(idx int NOT NULL,col varchar(8000), tag varchar(50) NOT NULL, PRIMARY KEY(tag,idx))DEFAULT CHARSET=utf8mb4";
	public static String DDL_COMMON = "CREATE TABLE _ewa_spt_data(idx int NOT NULL,col varchar(1000), tag varchar(50) NOT NULL, PRIMARY KEY(tag,idx))";
	private static Logger LOGGER = LoggerFactory.getLogger(CreateSplitData.class);
	private RequestValue rv_;

	// _EWA_SPT_DATA.tag和数据列表
	private HashMap<String, ArrayList<String>> tempData_;
	// 分割表达式和 tempData_.key （_EWA_SPT_DATA.tag）的关系表
	private HashMap<String, String> keyMap_;

	/**
	 * EWA_SPLIT标识
	 */
	public static final String tag = "EWA_SPLIT";
	private String uid;
	private DataConnection cnn;
	private String tempTableName;
	private boolean tempTableCreated;
	private boolean dropOnClose;

	public CreateSplitData(RequestValue rv, DataConnection cnn) {
		this.rv_ = rv;
		this.cnn = cnn;
		this.uid = Utils.getGuid().replace("-", "");

		this.tempData_ = new HashMap<>();
		this.keyMap_ = new HashMap<>();

		String databaseType = this.cnn.getDatabaseType();
		boolean sqlserver = "sqlserver".equalsIgnoreCase(databaseType) || "mssql".equalsIgnoreCase(databaseType);
		boolean mysql = "MYSQL".equalsIgnoreCase(databaseType);
		if (sqlserver) {
			this.tempTableName = "[#EWA_SPT_DATA_" + this.uid + "]"; // 使用内存
			dropOnClose = true;
		} else if (mysql) {
			// this.tempTableName = "`EWA_SPT_DATA_" + this.uid + "`"; // 使用内存
			// dropOnClose = true;
			this.tempTableName = "_EWA_SPT_DATA"; // 物理表
		} else {
			this.tempTableName = "_EWA_SPT_DATA"; // 物理表
		}
	}

	/**
	 * 创建临时表
	 */
	public void createEwaSplitTempData() {
		if (this.getTempData().size() == 0) {
			return;
		}
		String databaseType = this.cnn.getDatabaseType();
		boolean mysql = "MYSQL".equalsIgnoreCase(databaseType);
		boolean sqlserver = "sqlserver".equalsIgnoreCase(databaseType) || "mssql".equalsIgnoreCase(databaseType);
		if (!tempTableCreated) {
			if (sqlserver) {
				String sqlCreate = DDL_SQLSERVER.replace("_ewa_spt_data", this.tempTableName);
				LOGGER.debug("Create sqlserver temp table. {}" + sqlCreate);
				// SQLServer创建内存临时表
				this.cnn.executeUpdateNoParameter(sqlCreate);
				tempTableCreated = true;
			} else if (mysql) {
				// MYSQL 临时表在一条查询里只能打开一次
				// ERROR 1137 (HY000): Can't reopen table: '_ewa_spt_data'
				// • You cannot refer to a TEMPORARY table more than once in the same query.
				
				/*
				 * String sqlCreate = DDL_MYSQL.replace("_ewa_spt_data", this.tempTableName);
				 * LOGGER.debug("Create mysql temp table. {}" + sqlCreate); // mysql 创建内存临时表
				 * this.cnn.executeUpdateNoParameter(sqlCreate); tempTableCreated = true;
				 */
			}
		}
		String insertHeader = "insert into " + this.tempTableName + " (idx, col, tag) values ";

		List<String> values = new ArrayList<String>();
		for (String key : this.getTempData().keySet()) {
			ArrayList<String> al = this.getTempData().get(key);
			if (al == null) {
				continue;
			}
			String keyExp = cnn.sqlParameterStringExp(key);
			for (int i = 0; i < al.size(); i++) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				sb.append(i);
				sb.append(", ");

				String col = al.get(i);
				if (sqlserver) {
					// max 2g
				} else if (mysql) {
					if (col.length() > 8000) {
						col = col.substring(0, 8000);
						LOGGER.warn("EwaSplitTempData col size > 8000, truncation");
					}
				} else {
					if (col.length() > 1000) {
						col = col.substring(0, 1000);
						LOGGER.warn("EwaSplitTempData col size > 1000, truncation");
					}
				}
				String colExp = cnn.sqlParameterStringExp(col);
				sb.append(colExp);

				sb.append(",");
				sb.append(keyExp);
				sb.append(")");

				values.add(sb.toString());
			}

		}
		for (String key : this.getTempData().keySet()) {
			// 处理过了，
			this.getTempData().put(key, null);
		}
		BatchInsert bi = new BatchInsert(this.cnn, false);

		String result = bi.insertBatch(insertHeader, values);
		if (!StringUtils.isBlank(result)) {
			LOGGER.error(result);
		}
	}

	/**
	 * 清除临时数据
	 */
	public void clearEwaSplitTempData() {
		if (this.getTempData().size() == 0) {
			return;
		}
		LOGGER.debug("clearEwaSplitTempData, table={}, dropOnClose={}", this.tempTableName, this.dropOnClose);
		if (dropOnClose) {
			String sqlDrop = "drop table " + this.tempTableName;
			LOGGER.debug(sqlDrop);
			cnn.executeUpdateNoParameter(sqlDrop);
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("delete from _EWA_SPT_DATA where tag in (");
		int i = 0;
		for (String key : getTempData().keySet()) {
			if (i > 0) {
				sb.append(",");
			}
			i++;
			String keyExp = cnn.sqlParameterStringExp(key);
			sb.append(keyExp);
		}
		sb.append(")");
		String sqlDelete = sb.toString();
		LOGGER.debug(sqlDelete);
		cnn.executeUpdateNoParameter(sqlDelete);
	}

	public String replaceSplitData(String sql) {
		// insert into a(v1,v2)
		// select a.col,b.col from (select * from ewa_split(@s1,',')) a
		// inner join (select * from ewa_split ( @s2,',')) b on a.idx = b.idx
		for (int i = 0; i < 50; i++) {
			String sql1 = this.replaceSplitData1(sql);
			if (sql1.equals(sql)) {
				return sql;
			}
			sql = sql1;
		}
		return sql;
	}

	private String replaceSplitData1(String sql) {
		String sql1 = sql.toUpperCase();
		int loc = sql1.indexOf(tag);
		if (loc == -1) {
			return sql;
		}
		int locEnd = -1;
		for (int i = loc + tag.length(); i < sql1.length(); i++) {

			char c = sql1.charAt(i);
			if (c == ')') {
				locEnd = i;
				break;
			}
		}

		if (locEnd == -1) {
			return sql;
		}

		String exp = sql.substring(loc, locEnd + 1);
		String dataExp = insertTmpData(exp);
		sql = sql.replace(exp, "(select idx, col from " + this.tempTableName + " where tag='" + dataExp + "')");
		return sql;
	}

	private String insertTmpData(String para) {
		MListStr paras = Utils.getParameters(para, "@");
		if (paras.size() == 0) {
			return null;
		}
		String p1 = paras.get(0);
		String v1 = this.rv_.getString(p1);

		int loc0 = para.indexOf(",");
		int loc1 = para.lastIndexOf(")");

		// 分割字符串的 分割符
		String splitStr0 = para.substring(loc0 + 1, loc1).trim().replace("'", "");
		String keyExp = splitStr0 + "/gdx/" + v1;
		if (this.keyMap_.containsKey(keyExp)) {
			return this.keyMap_.get(keyExp);
		}
		// 创建在_ewa_split_data.tag (唯一ID)
		String tmpDataTag = (this.dropOnClose ? "" : this.uid) + ".gdx." + this.keyMap_.size();
		LOGGER.debug("Create temp data {}", tmpDataTag);

		this.keyMap_.put(keyExp, tmpDataTag);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < splitStr0.length(); i++) {
			sb.append("\\");
			sb.append(splitStr0.charAt(i));
		}
		// 分割字符串的正则表达式
		String splitStr = sb.toString();

		boolean isAppendBlank = false;

		ArrayList<String> al = new ArrayList<String>();
		this.tempData_.put(tmpDataTag, al);
		if (v1 == null) {
			// 创建空记录
			return tmpDataTag;
		}
		if (v1.endsWith(splitStr0)) {
			v1 += " ";
			isAppendBlank = true;
		}
		// System.out.println(splitStr);
		String[] vs = v1.split(splitStr);

		// 创建数组列表
		for (int i = 0; i < vs.length; i++) {
			String v2 = vs[i];
			if (isAppendBlank && i == vs.length - 1) {
				v2 = "";
			}
			al.add(v2);
		}
		return tmpDataTag;
	}

	public HashMap<String, ArrayList<String>> getTempData() {
		return tempData_;
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @return the tempTableName
	 */
	public String getTempTableName() {
		return tempTableName;
	}
}
