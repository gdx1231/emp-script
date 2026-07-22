package com.gdxsoft.easyweb.datasource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UDigest;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;

/**
 * 创建EWA_SPLIT的临时数据
 * 
 * @author admin
 *
 */
public class CreateSplitData {
	public static String DDL_SQLSERVER = "CREATE TABLE _ewa_spt_data(idx int NOT NULL,col nvarchar(4000), tag varchar(50) NOT NULL, PRIMARY KEY (tag,idx) )";
	private static Logger LOGGER = LoggerFactory.getLogger(CreateSplitData.class);
	private RequestValue rv_;

	// _EWA_SPT_DATA.tag和数据列表（PG 不使用）
	private HashMap<String, ArrayList<String>> tempData_;
	// 分割表达式和 tempData_.key （_EWA_SPT_DATA.tag）的关系表
	private HashMap<String, String> keyMap_;
	// 内联表达式缓存：keyExp → 内联 SQL
	private HashMap<String, String> inlineCache_;

	/**
	 * EWA_SPLIT标识
	 */
	public static final String tag = "EWA_SPLIT";
	private String uid;
	private DataConnection cnn;
	private String tempTableName;
	private boolean tempTableCreated;
	private boolean dropOnClose;

	/** PostgreSQL — use unnest(string_to_array(...)) inline, no temp table. */
	boolean isPg;
	/** Oracle / 达梦DM — use XMLTABLE inline, no temp table. */
	boolean isOracle;
	/** MySQL 8.0+ — use JSON_TABLE inline, no temp table. */
	boolean isMysql;
	/** SQL Server 2016+ — use OPENJSON inline, no temp table. */
	boolean isSqlServer2016Plus;
	/** SQL Server 2005-2014 — use XML nodes() inline, no temp table. */
	boolean isSqlServerPre2016;

	public CreateSplitData(RequestValue rv, DataConnection cnn) {
		this.rv_ = rv;
		this.cnn = cnn;
		this.uid = Utils.getGuid().replace("-", "");

		this.tempData_ = new HashMap<>();
		this.keyMap_ = new HashMap<>();
		this.inlineCache_ = new HashMap<>();

		String databaseType = this.cnn.getDatabaseType();
		boolean isSqlServerDb = SqlUtils.isSqlServer(databaseType);
		boolean isMysqlDb = SqlUtils.isMySql(databaseType);
		boolean isOracleDb = SqlUtils.isOracle(databaseType); // 含达梦DM
		this.isPg = SqlUtils.isPostgreSql(databaseType);
		// Oracle 10gR2+ → XMLTABLE；达梦DM 全版本 → XMLTABLE
		this.isOracle = isOracleDb && (checkVersionGe(10) || "DM".equalsIgnoreCase(databaseType));
		// SQL Server: 2016+ → OPENJSON, 2005-2014 → XML nodes(), 2000 → #temp
		this.isSqlServer2016Plus = isSqlServerDb && checkVersionGe(13);
		this.isSqlServerPre2016 = isSqlServerDb && !isSqlServer2016Plus && checkVersionGe(9);

		if (isSqlServer2016Plus || isSqlServerPre2016) {
			// SQL Server 2005+ 内联，零 IO
			this.tempTableName = null;
		} else if (isSqlServerDb) {
			// SQL Server 2000 → #temp 内存临时表
			this.tempTableName = "[#EWA_SPT_DATA_" + this.uid + "]";
			dropOnClose = true;
		} else if (isMysql) {
			// MySQL 8.0+ 使用 JSON_TABLE 内联，零 IO，不需物理表
			this.tempTableName = null;
		} else if (isMysqlDb) {
			// MySQL 5.7 退回物理表
			this.tempTableName = "_EWA_SPT_DATA";
		} else if (isPg || isOracle) {
			// PG/Oracle/达梦 内联方案，零 IO，不需要物理表
			this.tempTableName = null;
		} else if (isOracleDb) {
			// Oracle < 10g → 物理表
			this.tempTableName = "_EWA_SPT_DATA";
		} else {
			this.tempTableName = "_EWA_SPT_DATA"; // 物理表
		}
	}
	/**
	 * 空值参数 → 空结果集子查询。Oracle 需 FROM DUAL。
	 * @return (SELECT 0 AS idx, '' AS col WHERE 1=0)
	 */
	private String emptyResultSet() {
		return isOracle
				? "(SELECT 0 AS idx, '' AS col FROM DUAL WHERE 1=0)"
				: "(SELECT 0 AS idx, '' AS col WHERE 1=0)";
	}
	
	/**
	 * 检测数据库大版本号是否 ≥ 指定值。
	 * 连接不可用或检测失败时，保守返回 false（退回旧路径）。
	 *
	 * @param major 目标大版本号（MySQL 8, SQL Server 13/2016）
	 */
	private boolean checkVersionGe(int major) {
		try {
			Connection conn = this.cnn.getConnection();
			if (conn != null && !conn.isClosed()) {
				DatabaseMetaData meta = conn.getMetaData();
				return meta.getDatabaseMajorVersion() >= major;
			}
		} catch (SQLException e) {
			LOGGER.debug("checkVersionGe({}) failed, assume < {}: {}", major, major, e.getMessage());
		}
		return false;
	}

	/**
	 * 创建临时表
	 */
	public void createEwaSplitTempData() {
		if (this.getTempData().size() == 0) {
			return;
		}
		// 所有主流数据库内联路径，无临时表
		if (isPg || isOracle || isMysql || isSqlServer2016Plus || isSqlServerPre2016) {
			return;
		}
		String databaseType = this.cnn.getDatabaseType();
		boolean mysql = SqlUtils.isMySql(databaseType);
		boolean sqlserver = SqlUtils.isSqlServer(databaseType); // SQL Server 2000

		if (!tempTableCreated) {
			if (sqlserver) {
				String sqlCreate = DDL_SQLSERVER.replace("_ewa_spt_data", this.tempTableName);
				LOGGER.debug("Create sqlserver temp table. {}", sqlCreate);
				this.cnn.executeUpdateNoParameter(sqlCreate);
				tempTableCreated = true;
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
					// SQL Server 2000: nvarchar(4000) limit
					if (col.length() > 4000) {
						col = col.substring(0, 4000);
						LOGGER.warn("EwaSplitTempData col size > 4000, truncation");
					}
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
		// 内联路径，无临时表需要清理
		if (isPg || isOracle || isMysql || isSqlServer2016Plus || isSqlServerPre2016) {
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

		// 计算缓存 key（与 insertTmpData 一致：分隔符/gdx/值）
		boolean isInline = isPg || isOracle || isSqlServer2016Plus
				|| isSqlServerPre2016 || isMysql;
		String cacheKey = null;
		if (isInline) {
			MListStr ppp = Utils.getParameters(exp, "@");
			if (ppp.size() > 0) {
				String pn = ppp.get(0);
				String pv = this.rv_.getString(pn);
				int l0 = exp.indexOf(",");
				int l1 = exp.lastIndexOf(")");
				String dl = l0 >= 0 && l1 > l0
						? exp.substring(l0 + 1, l1).trim().replace("'", "")
						: ",";
				cacheKey = dl + "/gdx/" + (pv == null ? "__null__" : UDigest.digestHex(pv, "md5"));
				// 命中缓存 → 直接复用
				String cached = this.inlineCache_.get(cacheKey);
				if (cached != null) {
					sql = sql.replace(exp, cached);
					return sql;
				}
			}
		}

		String inline = null;
		if (isPg) {
			inline = buildPgUnnest(exp);
		} else if (isOracle) {
			inline = buildOracleXmltable(exp);
		} else if (isSqlServer2016Plus) {
			inline = buildSqlServerOpenJson(exp);
		} else if (isSqlServerPre2016) {
			inline = buildSqlServerXmlNodes(exp);
		} else if (isMysql) {
			inline = buildMySqlJsonTable(exp);
		}

		if (inline != null) {
			// 写入缓存
			if (cacheKey != null) {
				this.inlineCache_.put(cacheKey, inline);
			}
			sql = sql.replace(exp, inline);
		} else if (!isInline) {
			// 非内联数据库 → 临时表路径
			String dataExp = insertTmpData(exp);
			if (dataExp != null) {
				sql = sql.replace(exp,
						"(select idx, col from " + this.tempTableName + " where tag='" + dataExp + "')");
			}
		}
		// 内联 build 返回 null（无 @param），不做替换，留给下次循环或原样
		return sql;
	}

	/**
	 * Parse an ewa_split expression and return a PG inline subquery using
	 * {@code unnest(string_to_array(...)) WITH ORDINALITY}, avoiding any
	 * temporary-table I/O.
	 *
	 * <p>Example transformation:
	 * <pre>{@code
	 *   ewa_split(@ids, ',')
	 *   →
	 *   (SELECT ordinality - 1 AS idx, t.val AS col
	 *    FROM unnest(string_to_array('1,2,3', ',')) WITH ORDINALITY AS t(val, ordinality))
	 * }</pre>
	 *
	 * @param exp the full ewa_split expression, e.g. {@code EWA_SPLIT(@ids, ',')}
	 * @return a PG subquery that returns (idx, col), or null if params are missing
	 */
	String buildPgUnnest(String exp) {
		MListStr paras = Utils.getParameters(exp, "@");
		if (paras.size() == 0) {
			return null;
		}
		String paramName = paras.get(0);
		String v1 = this.rv_.getString(paramName);
		if (v1 == null) {
			// 空值 → 返回空结果集
			return emptyResultSet();
		}

		int loc0 = exp.indexOf(",");
		int loc1 = exp.lastIndexOf(")");
		String delimiter = loc0 >= 0 && loc1 > loc0
				? exp.substring(loc0 + 1, loc1).trim().replace("'", "")
				: ",";

		// 转义 PG 字符串字面量中的特殊字符
		String escapedValue = v1.replace("'", "''");

		StringBuilder sb = new StringBuilder();
		sb.append("(SELECT ordinality - 1 AS idx, t.val AS col ");
		sb.append("FROM unnest(string_to_array('");
		sb.append(escapedValue);
		sb.append("', '");
		sb.append(delimiter.replace("'", "''"));
		sb.append("')) WITH ORDINALITY AS t(val, ordinality))");
		return sb.toString();
	}

	/**
	 * Parse an ewa_split expression and return an Oracle inline subquery using
	 * {@code XMLTABLE}, avoiding temporary-table I/O.
	 *
	 * <p>Example transformation:
	 * <pre>{@code
	 *   ewa_split(@ids, ',')
	 *   →
	 *   (SELECT ROWNUM - 1 AS idx, TRIM(COLUMN_VALUE) AS col
	 *    FROM XMLTABLE(('"' || REPLACE('1,2,3', ',', '","') || '"')))
	 * }</pre>
	 *
	 * <p>Values containing XML-special characters ({@code " < > &}) are escaped.
	 *
	 * @param exp the full ewa_split expression, e.g. {@code EWA_SPLIT(@ids, ',')}
	 * @return an Oracle subquery returning (idx, col), or null if params missing
	 */
	String buildOracleXmltable(String exp) {
		MListStr paras = Utils.getParameters(exp, "@");
		if (paras.size() == 0) {
			return null;
		}
		String paramName = paras.get(0);
		String v1 = this.rv_.getString(paramName);
		if (v1 == null) {
			return emptyResultSet();
		}

		int loc0 = exp.indexOf(",");
		int loc1 = exp.lastIndexOf(")");
		String delimiter = loc0 >= 0 && loc1 > loc0
				? exp.substring(loc0 + 1, loc1).trim().replace("'", "")
				: ",";

		// Escape XML special characters in values
		String escapedValue = v1
				.replace("&", "&amp;")
				.replace("\"", "&quot;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");

		String escapedDelimiter = delimiter
				.replace("&", "&amp;")
				.replace("\"", "&quot;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");

		StringBuilder sb = new StringBuilder();
		sb.append("(SELECT ROWNUM - 1 AS idx, TRIM(COLUMN_VALUE) AS col ");
		sb.append("FROM XMLTABLE(('\"' || REPLACE('");
		sb.append(escapedValue);
		sb.append("', '");
		sb.append(escapedDelimiter);
		sb.append("', '\",\"') || '\"')))");
		return sb.toString();
	}

	/**
	 * Parse an ewa_split expression and return a MySQL inline subquery using
	 * {@code JSON_TABLE}, avoiding temporary-table I/O.
	 *
	 * <p>Requires MySQL 8.0.4+. JSON-special characters ({@code \ "}) in values
	 * are escaped via a SQL REPLACE chain inside a CONCAT-built JSON array.
	 *
	 * <p>Example transformation:
	 * <pre>{@code
	 *   ewa_split(@ids, ',')
	 *   →
	 *   (SELECT t.idx - 1 AS idx, t.val AS col
	 *    FROM JSON_TABLE(
	 *        CONCAT('["', REPLACE(REPLACE(REPLACE('1,2,3', '\\', '\\\\'), '"', '\\"'), ',', '","'), '"]'),
	 *        '$[*]' COLUMNS(idx FOR ORDINALITY, val VARCHAR(8000) PATH '$')
	 *    ) AS t)
	 * }</pre>
	 *
	 * @param exp the full ewa_split expression, e.g. {@code EWA_SPLIT(@ids, ',')}
	 * @return a MySQL subquery returning (idx, col), or null if params missing
	 */
	String buildMySqlJsonTable(String exp) {
		MListStr paras = Utils.getParameters(exp, "@");
		if (paras.size() == 0) {
			return null;
		}
		String paramName = paras.get(0);
		String v1 = this.rv_.getString(paramName);
		if (v1 == null) {
			// 空值 → 返回空结果集
			return emptyResultSet();
		}

		int loc0 = exp.indexOf(",");
		int loc1 = exp.lastIndexOf(")");
		String delimiter = loc0 >= 0 && loc1 > loc0
				? exp.substring(loc0 + 1, loc1).trim().replace("'", "")
				: ",";

		// SQL string literal escaping: ' → ''; \ → \\ only if NO_BACKSLASH_ESCAPES is off
		String sqlEscaped = v1.replace("'", "''");
		if (!this.cnn.isMysqlNoBackslashEscapes()) {
			sqlEscaped = sqlEscaped.replace("\\", "\\\\");
		}

		// Delimiter also needs SQL escaping
		String sqlEscapedDelim = delimiter.replace("'", "''");
		if (!this.cnn.isMysqlNoBackslashEscapes()) {
			sqlEscapedDelim = sqlEscapedDelim.replace("\\", "\\\\");
		}

		// Build: CONCAT('["', REPLACE(REPLACE(REPLACE(val,'\\','\\\\'),'"','\\"'),delim,'","'),'"]')
		StringBuilder sb = new StringBuilder();
		sb.append("(SELECT t.idx - 1 AS idx, t.val AS col ");
		sb.append("FROM JSON_TABLE(");
		sb.append("CONCAT('[\"', REPLACE(REPLACE(REPLACE('");
		sb.append(sqlEscaped);
		sb.append("', '\\\\', '\\\\\\\\'), '\"', '\\\\\"'), '");
		sb.append(sqlEscapedDelim);
		sb.append("', '\",\"'), '\"]')");
		sb.append(", '$[*]' COLUMNS(idx FOR ORDINALITY, val VARCHAR(8000) PATH '$')");
		sb.append(") AS t)");
		return sb.toString();
	}

	/**
	 * Parse an ewa_split expression and return a SQL Server inline subquery using
	 * {@code OPENJSON}, avoiding temporary-table I/O.
	 *
	 * <p>Requires SQL Server 2016+. The key advantage over {@code STRING_SPLIT}:
	 * {@code OPENJSON} guarantees ordinal position via the {@code [key]} column.
	 *
	 * <p>Example transformation:
	 * <pre>{@code
	 *   ewa_split(@ids, ',')
	 *   →
	 *   (SELECT CAST(t.[key] AS INT) AS idx, t.value AS col
	 *    FROM OPENJSON('["' + REPLACE(REPLACE(REPLACE('1,2,3', '\', '\\'), '"', '\"'), ',', '","') + '"]') AS t)
	 * }</pre>
	 *
	 * @param exp the full ewa_split expression, e.g. {@code EWA_SPLIT(@ids, ',')}
	 * @return a SQL Server subquery returning (idx, col), or null if params missing
	 */
	String buildSqlServerOpenJson(String exp) {
		MListStr paras = Utils.getParameters(exp, "@");
		if (paras.size() == 0) {
			return null;
		}
		String paramName = paras.get(0);
		String v1 = this.rv_.getString(paramName);
		if (v1 == null) {
			return emptyResultSet();
		}

		int loc0 = exp.indexOf(",");
		int loc1 = exp.lastIndexOf(")");
		String delimiter = loc0 >= 0 && loc1 > loc0
				? exp.substring(loc0 + 1, loc1).trim().replace("'", "")
				: ",";

		// T-SQL string literal escaping: only ' → '' (backslash is literal)
		String tsvEscaped = v1.replace("'", "''");
		String tsvEscapedDelim = delimiter.replace("'", "''");

		// JSON escaping inside T-SQL: backslash and quote are literal chars
		// REPLACE chain: \ → \\, " → \", delimiter → ","
		StringBuilder sb = new StringBuilder();
		sb.append("(SELECT CAST(t.[key] AS INT) AS idx, t.value AS col ");
		sb.append("FROM OPENJSON('[\"' + REPLACE(REPLACE(REPLACE('");
		sb.append(tsvEscaped);
		sb.append("', '\\', '\\\\'), '\"', '\\\"'), '");
		sb.append(tsvEscapedDelim);
		sb.append("', '\",\"') + '\"]') AS t)");
		return sb.toString();
	}

	/**
	 * Parse an ewa_split expression and return a SQL Server inline subquery using
	 * XML {@code nodes()}, avoiding temporary-table I/O.
	 *
	 * <p>Works on SQL Server 2005+. Values are XML-escaped, then wrapped in
	 * {@code <x>…</x>} tags and shredded via {@code CROSS APPLY xml.nodes('/x')}.
	 *
	 * <p>Example transformation:
	 * <pre>{@code
	 *   ewa_split(@ids, ',')
	 *   →
	 *   (SELECT ROW_NUMBER() OVER (ORDER BY (SELECT 1)) - 1 AS idx,
	 *           LTRIM(RTRIM(x.value('.', 'NVARCHAR(MAX)'))) AS col
	 *    FROM (SELECT CAST('<x>' + REPLACE('1,2,3', ',', '</x><x>') + '</x>' AS XML) AS xml_doc) src
	 *    CROSS APPLY xml_doc.nodes('/x') AS t(x))
	 * }</pre>
	 *
	 * @param exp the full ewa_split expression, e.g. {@code EWA_SPLIT(@ids, ',')}
	 * @return a SQL Server subquery returning (idx, col), or null if params missing
	 */
	String buildSqlServerXmlNodes(String exp) {
		MListStr paras = Utils.getParameters(exp, "@");
		if (paras.size() == 0) {
			return null;
		}
		String paramName = paras.get(0);
		String v1 = this.rv_.getString(paramName);
		if (v1 == null) {
			return emptyResultSet();
		}

		int loc0 = exp.indexOf(",");
		int loc1 = exp.lastIndexOf(")");
		String delimiter = loc0 >= 0 && loc1 > loc0
				? exp.substring(loc0 + 1, loc1).trim().replace("'", "")
				: ",";

		// XML entity escaping: & < > " '  (must be done before SQL embedding)
		String xmlEscaped = v1
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");

		String xmlEscapedDelim = delimiter
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");

		// Build XML doc: <x>v1</x><x>v2</x>..., then shred with nodes('/x')
		StringBuilder sb = new StringBuilder();
		sb.append("(\n\tSELECT ROW_NUMBER() OVER (ORDER BY (SELECT 1)) - 1 AS idx, ");
		sb.append("LTRIM(RTRIM(x.value('.', 'NVARCHAR(MAX)'))) AS col ");
		sb.append("\n\tFROM (\n\t\tSELECT CAST('<x>' + REPLACE('");
		sb.append(xmlEscaped);
		sb.append("', '");
		sb.append(xmlEscapedDelim);
		sb.append("', '</x><x>') + '</x>' AS XML) AS xml_doc\n\t) src ");
		sb.append("CROSS APPLY xml_doc.nodes('/x') AS t(x)\n)\n");
		return sb.toString();
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
		String keyExp = splitStr0 + "/gdx/" + (v1 == null ? "__null__" : UDigest.digestHex(v1, "md5"));
		if (this.keyMap_.containsKey(keyExp)) {
			return this.keyMap_.get(keyExp);
		}
		// 创建在_ewa_split_data.tag (唯一ID)
		String tmpDataTag = (this.dropOnClose ? "" : this.uid) + ".gdx." + this.keyMap_.size();
		LOGGER.debug("Create temp data {}", tmpDataTag);

		this.keyMap_.put(keyExp, tmpDataTag);

		ArrayList<String> al = new ArrayList<String>();
		this.tempData_.put(tmpDataTag, al);
		if (v1 == null) {
			// 创建空记录
			return tmpDataTag;
		}
		// Pattern.quote 转义正则特殊字符（. | * + 等）；-1 保留尾部空串
		String[] vs = v1.split(Pattern.quote(splitStr0), -1);

		for (int i = 0; i < vs.length; i++) {
			al.add(vs[i]);
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
