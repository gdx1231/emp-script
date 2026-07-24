package com.gdxsoft.easyweb.datasource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfExtraGlobal;
import com.gdxsoft.easyweb.conf.ConfExtraGlobals;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.types.UInt16;
import com.gdxsoft.easyweb.utils.types.UInt32;
import com.gdxsoft.easyweb.utils.types.UInt64;

/**
 * SQL text builder and parameter binder — extracted from DataConnection to
 * reduce the God Class anti-pattern.
 *
 * <p>Owns all SQL-string manipulation (rebuildSql and its 10+ sub-methods),
 * parameter binding ({@code addSqlParameter}, {@code addStatementParameter}),
 * and the type-conversion helpers they depend on.
 *
 * <p>Holds a back-reference to the owning {@link DataConnection} for
 * operations that require connection state (CreateSplitData, ReverseIds).
 */
public class DataConnectionSqlBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataConnectionSqlBuilder.class);

	private final DataConnection owner;
	private CreateSplitData createSplitData;
	private EwaSqlFunctions ewaSqlFunctions;

	public DataConnectionSqlBuilder(DataConnection owner) {
		this.owner = owner;
	}

	// ═══════════════════════════════════════════════════════════════
	//  SQL Text Building
	// ═══════════════════════════════════════════════════════════════

	/**
	 * Master SQL rewriter — applies all transformations to a raw SQL string.
	 */
	public String rebuildSql(String sql) throws Exception {
		if (sql == null) {
			return null;
		}
		String sql1 = sql;
		RequestValue rv = owner.getRequestValue();

		// 1) ~param pre-replace (table/field name substitution)
		MListStr preReplaces = Utils.getParameters(sql, "~");
		for (int i = 0; i < preReplaces.size(); i++) {
			String para = preReplaces.get(i);
			PageValue pv = rv.getPageValues().getValue(para);
			String v1 = pv.getStringValue();
			if (v1 == null || v1.trim().length() == 0) {
				throw new Exception("The param (~" + para + ") not exists");
			}
			boolean fromTrustedSource = (pv.getPVTag() == PageValueTag.SESSION
					|| pv.getPVTag() == PageValueTag.SYSTEM
					|| pv.getPVTag() == PageValueTag.DTTABLE
					|| pv.getPVTag() == PageValueTag.OTHER
					|| pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS);
			try {
				String validatedValue = SqlIdentifierValidator.validateTildeParam(para, v1, fromTrustedSource);
				sql1 = sql1.replace("~" + para, validatedValue);
			} catch (SecurityException | IllegalArgumentException e) {
				throw new Exception("SQL injection detected in parameter ~" + para + ": " + e.getMessage(), e);
			}
		}

		// 2) Conditional blocks
		sql1 = createSqlByEwaBlockTest(sql1);
		sql1 = createSqlByEwaTest(sql1);

		// 3) Comment @-symbol protection
		sql1 = replaceSqlCommentAtSymbol(sql1);

		// 4) EWA_SPLIT temp data
		if (rv != null) {
			if (createSplitData == null) {
				createSplitData = new CreateSplitData(rv, owner);
			}
			sql = createSplitData.replaceSplitData(sql1);
			sql1 = sql;
		}

		// 5) Reverse ID lookups
		ReverseIds reverseIds = new ReverseIds(owner);
		sql1 = reverseIds.replaceReverseIds(sql1);

		// 6) EWA custom functions
		EwaSqlFunctions esf = new EwaSqlFunctions();
		sql1 = esf.extractEwaSqlFunctions(sql1);
		this.ewaSqlFunctions = esf;

		// 7) JSON data replacement
		CreateJsonData createJsonData = new CreateJsonData(rv);
		try {
			sql1 = createJsonData.replaceJsonData(sql1);
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}

		// 8) @param → random placeholder (preserve @paramName for
		//    replaceSqlSelectParameters to inline later in SELECT queries)
		HashMap<String, String> fieldsMap = new HashMap<String, String>();
		MListStr paras = Utils.getParameters(sql1, "@");
		for (int i = 0; i < paras.size(); i++) {
			String para = paras.get(i);
			if (skipReplaceParameter(para)) {
				continue;
			}
			PageValue pv = rv.getPageValues().getValue(para);
			if (pv == null) {
				String otherValue = rv.getOtherValue(para);
				if (otherValue != null) {
					pv = new PageValue();
					pv.setValue(otherValue);
					pv.setDataType("string");
				}
			}
			String v1 = null;
			if (pv == null) {
				pv = getParameterByEndWithType(para);
				if (pv != null && pv.getLength() > -1) {
					v1 = pv.getStringValue();
				}
			} else {
				v1 = pv.getStringValue();
			}
			if (v1 == null && esf.getTempData().containsKey(para)) {
				v1 = esf.getTempData().get(para).toString();
			}

			String paraName = "@" + para;
			if (v1 == null) {
				sql1 = sql1.replaceFirst(paraName + "\\b", " null ");
			} else if (para.toUpperCase().indexOf("_SPLIT") > 0) {
				StringBuilder sb = new StringBuilder();
				String[] v2 = v1.split(",");
				for (int m = 0; m < v2.length; m++) {
					String v3 = sqlParameterStringExp(v2[m]);
					if (m == 0) {
						sb.append(v3);
					} else {
						sb.append(", ");
						sb.append(v3);
					}
				}
				sql1 = sql1.replaceFirst(paraName + "\\b", sb.toString());
			} else {
				// 避免 @code_key 和 @code 的冲突，用随机占位符保护 @paramName
				// 后续 replaceSqlSelectParameters / replaceSqlParameters 会处理
				String randomName = "[gDx[" + Utils.randomStr(30) + Utils.getGuid() + "]GdX]";
				sql1 = sql1.replaceFirst(paraName + "\\b", randomName);
				fieldsMap.put(randomName, paraName);
			}
		}
		// 还原随机占位符为 @paramName，供 replaceSqlSelectParameters 使用
		for (String randomName : fieldsMap.keySet()) {
			sql1 = sql1.replace(randomName, fieldsMap.get(randomName));
		}
		return sql1;
	}

	/** Replace @param → ? in SQL, skipping rownum-style parameters. */
	public String replaceSqlParameters(String sql) {
		String sql1 = sql;
		MListStr al = Utils.getParameters(sql, "@");
		for (int i = 0; i < al.size(); i++) {
			String paramName = al.get(i);
			if (skipReplaceParameter(paramName)) {
				continue;
			}
			sql1 = sql1.replaceFirst("@" + al.get(i) + "\\b", "?");
		}
		if (sql1.indexOf("{@}") >= 0) {
			sql1 = sql1.replace("{@}", "@");
		}
		return sql1;
	}

	/**
	 * Replace @param with literal values in SELECT queries (avoids full table
	 * scans on SQL Server).
	 */
	public String replaceSqlSelectParameters(String sql) {
		return replaceSqlSelectParameters(sql, owner.getDatabaseType());
	}

	public String replaceSqlSelectParameters(String sql, String databaseType) {
		String sql1 = sql;
		MListStr al = Utils.getParameters(sql, "@");
		for (int i = 0; i < al.size(); i++) {
			String paramName = al.get(i);
			String paramValue = null;
			if (skipReplaceParameter(paramName)) {
				continue;
			}
			try {
				paramValue = getReplaceParameterValueExp(paramName, databaseType);
			} catch (Exception err) {
				LOGGER.error("replaceSqlSelectParameters[{}]: {}", paramName, err.getMessage());
			}
			if (paramValue == null || paramValue.indexOf("@") >= 0) {
				paramValue = "[[@]]" + paramName;
			}
			String safe = Matcher.quoteReplacement(paramValue);
			sql1 = sql1.replaceFirst("@" + paramName + "\\b", safe);
		}
		sql1 = sql1.replace("[[@]]", "@");
		return sql1;
	}

	public String getReplaceParameterValueExp(String paramName) {
		return getReplaceParameterValueExp(paramName, owner.getDatabaseType());
	}

	public String getReplaceParameterValueExp(String paramName, String databaseType) {
		RequestValue rv = owner.getRequestValue();
		PageValue pv = rv.getPageValues().getValue(paramName);
		if (pv == null || pv.getValue() == null) {
			String otherValue = rv.getOtherValue(paramName);
			if (otherValue != null) {
				pv = new PageValue();
				pv.setValue(otherValue);
				pv.setDataType("string");
			}
		}
		if (pv == null || pv.getValue() == null) {
			pv = getParameterByEndWithType(paramName);
			if (pv == null || pv.getValue() == null) {
				return "null";
			}
		}
		if (pv.getValue() instanceof java.util.Date) {
			Date dt1 = (Date) pv.getValue();
			Timestamp ts1 = new Timestamp(dt1.getTime());
			return getDateTimePara(ts1);
		}
		String dt = pv.getDataType();
		dt = dt == null ? "STRING" : dt.toUpperCase().trim();
		if ("BINARY".equals(dt) || "B[".equals(dt)) {
			return null;
		}
		String v1 = pv.getStringValue();
		if ("INT".equals(dt) || "INTEGER".equals(dt)) {
			return String.valueOf(getParaInteger(pv));
		} else if ("LONG".equals(dt) || "BIGINT".equals(dt)) {
			return String.valueOf(getParaLong(pv));
		} else if ("NUMBER".equals(dt) || "DOUBLE".equals(dt) || "FLOAT".equals(dt)) {
			BigDecimal v = getParaBigDecimal(pv);
			return v == null ? "null" : v.toPlainString();
		} else if ("DATE".equals(dt)) {
			return getDateTimePara(v1);
		}
		String v1Exp = sqlParameterStringExp(v1, databaseType);
		if (v1Exp.indexOf("@") < 0) {
			return v1Exp;
		}
		String v2 = SqlUtils.replaceSqlAtWithChar64(v1Exp, databaseType);
		if (v2.equals(v1Exp)) {
			return v1Exp.replace("@", "{@}");
		}
		return v2;
	}

	// ── Conditional SQL blocks ─────────────────────────────────────

	public String createSqlByEwaBlockTest(String orignalSql) {
		RequestValue rv = owner.getRequestValue();
		if (rv == null || orignalSql == null || orignalSql.toLowerCase().indexOf("ewa_block_test") == -1) {
			return orignalSql;
		}
		String[] sqls = orignalSql.split("\n");
		MStr str = new MStr();
		str.setNewLine("\n");
		boolean lastResult = true;
		boolean inTestBlock = false;
		for (int m = 0; m < sqls.length; m++) {
			String sql = sqls[m].trim();
			String len = sql.toLowerCase();
			if (!len.startsWith("--")) {
				if (!inTestBlock || lastResult) {
					str.al(sqls[m]);
				}
				continue;
			}
			int loc0 = len.indexOf("ewa_block_test");
			if (loc0 == -1) {
				if (!inTestBlock || lastResult) {
					str.al(sqls[m]);
				}
				continue;
			}
			String len2 = sql.substring(loc0 + 14).trim();
			String exp = null;
			if (len2.length() == 0) {
				inTestBlock = false;
				lastResult = true;
			} else {
				exp = replaceSqlSelectParameters(len2, "HSQLDB");
				lastResult = com.gdxsoft.easyweb.utils.ULogic.runLogic(exp);
				inTestBlock = true;
			}
			str.al("-- ewa_block_test<" + lastResult + "> " + len2.replace("@", "&#64;")
					+ (exp == null ? "" : " (" + exp + ")"));
		}
		return str.toString();
	}

	public String createSqlByEwaTest(String orignalSql) {
		RequestValue rv = owner.getRequestValue();
		if (rv == null || orignalSql == null || orignalSql.toLowerCase().indexOf("ewa_test") == -1) {
			return orignalSql;
		}
		String[] sqls = orignalSql.split("\n");
		MStr str = new MStr();
		str.setNewLine("\n");
		boolean lastResult = true;
		for (int m = 0; m < sqls.length; m++) {
			String sql = sqls[m].trim();
			String len = sql.toLowerCase();
			if (!len.startsWith("--")) {
				if (lastResult) {
					str.al(sqls[m]);
				}
				continue;
			}
			int loc0 = len.indexOf("ewa_test");
			if (loc0 == -1) {
				if (lastResult) {
					str.al(sqls[m]);
				}
				continue;
			}
			String len2 = sql.substring(loc0 + 8).trim();
			String exp = null;
			if (len2.length() == 0) {
				lastResult = true;
			} else {
				exp = replaceSqlSelectParameters(len2, "HSQLDB");
				lastResult = com.gdxsoft.easyweb.utils.ULogic.runLogic(exp);
			}
			str.al("-- ewa_test<" + lastResult + "> " + len2.replace("@", "&#64;")
					+ (exp == null ? "" : " (" + exp + ")"));
		}
		return str.toString();
	}

	public String replaceSqlCommentAtSymbol(String orignalSql) {
		if (orignalSql == null || orignalSql.indexOf("--") == -1) {
			return orignalSql;
		}
		String[] sqls = orignalSql.split("\n");
		MStr str = new MStr();
		str.setNewLine("\n");
		for (int m = 0; m < sqls.length; m++) {
			String sql = sqls[m].trim();
			if (!sql.startsWith("--") || !sql.contains("@")) {
				str.al(sqls[m]);
				continue;
			}
			str.al(sqls[m].replace("@", "&#64;"));
		}
		return str.toString();
	}

	public boolean skipReplaceParameter(String paramName) {
		return paramName != null && paramName.toLowerCase().indexOf("rownum") == 0;
	}

	public PageValue getParameterByEndWithType(String parameterName) {
		RequestValue rv = owner.getRequestValue();
		PageValue pv = null;
		String dt = null;
		String pname = parameterName.toLowerCase();
		if (pname.endsWith(".int")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 4));
			dt = "int";
		} else if (pname.endsWith(".bigint")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 7));
			dt = "bigint";
		} else if (pname.endsWith(".long")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 5));
			dt = "bigint";
		} else if (pname.endsWith(".date")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 5));
			dt = "date";
		} else if (pname.endsWith(".number")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 7));
			dt = "number";
		} else if (pname.endsWith(".double")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 7));
			dt = "double";
		} else if (pname.endsWith(".binary")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 7));
			dt = "binary";
		} else if (pname.endsWith(".bin")) {
			pv = rv.getPageValues().getValue(parameterName.substring(0, parameterName.length() - 4));
			dt = "binary";
		}
		if (pv != null) {
			pv.setDataType(dt);
		} else {
			pv = new PageValue();
			pv.setDataType(dt);
			pv.setLength(-1);
		}
		return pv;
	}

	// ═══════════════════════════════════════════════════════════════
	//  Parameter Binding
	// ═══════════════════════════════════════════════════════════════

	public void addSqlParameter(MListStr parameters, PreparedStatement pst) throws SQLException {
		if (parameters == null || owner.getRequestValue() == null) {
			return;
		}
		int index = 0;
		for (int i = 0; i < parameters.size(); i++) {
			String PKey = parameters.get(i).toUpperCase();
			if (skipReplaceParameter(PKey)) {
				continue;
			}
			index++;
			addStatementParameter(pst, PKey, index);
		}
	}

	HashMap<String, Object> addSqlParameter(MListStr parameters, CallableStatement cst) throws SQLException {
		HashMap<String, Object> outValues = new HashMap<>();
		if (parameters == null || owner.getRequestValue() == null) {
			return outValues;
		}
		PreparedStatement pst = (PreparedStatement) cst;
		int index = 0;
		for (int i = 0; i < parameters.size(); i++) {
			String key = parameters.get(i).trim();
			String key1 = key.toUpperCase();
			if (skipReplaceParameter(key1)) {
				continue;
			}
			index++;
			if (!key1.endsWith("_OUT") && !key1.endsWith("_OUTPUT")) {
				addStatementParameter(pst, key, index);
				continue;
			}
			// OUT parameter registration
			if (key1.indexOf("_BIGINT_") >= 0 || key1.indexOf("_LONG_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.BIGINT);
			} else if (key1.indexOf("_TINYINT_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.TINYINT);
			} else if (key1.indexOf("_SMALLINT_") >= 0 || key1.indexOf("_SHORT_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.SMALLINT);
			} else if (key1.indexOf("_INT_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.INTEGER);
			} else if (key1.indexOf("_BIT_") >= 0 || key1.indexOf("_BOOL_") >= 0 || key1.indexOf("_BOOLEAN_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.BIT);
			} else if (key1.indexOf("_NUMBER_") >= 0 || key1.indexOf("_MONEY_") >= 0
					|| key1.indexOf("_DECIMAL_") >= 0 || key1.indexOf("_NUMERIC_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.DECIMAL);
			} else if (key1.indexOf("_DOUBLE_") >= 0 || key1.indexOf("_FLOAT_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.DOUBLE);
			} else if (key1.indexOf("_IMAGE_") >= 0 || key1.indexOf("_BLOB_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.BLOB);
			} else if (key1.indexOf("_TEXT_") >= 0 || key1.indexOf("_CLOB_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.CLOB);
			} else if (key1.indexOf("_BINARY_") >= 0 || key1.indexOf("_BYTE_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.VARBINARY);
			} else if (key1.indexOf("_DATE_") >= 0 || key1.indexOf("_DATETIME_") >= 0
					|| key1.indexOf("_TIMESTAMP_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.TIMESTAMP);
			} else if (key1.indexOf("_TIME_") >= 0) {
				cst.registerOutParameter(index, java.sql.Types.TIME);
			} else {
				cst.registerOutParameter(index, java.sql.Types.VARCHAR);
			}
			outValues.put(key1, "" + (index));
			owner.writeDebug(owner, "添加返回参数(String)" + index, key);
		}
		return outValues;
	}

	public void addStatementParameter(PreparedStatement cst, String parameterName, int index) throws SQLException {
		RequestValue rv = owner.getRequestValue();
		if (rv == null) {
			cst.setObject(index, null);
			return;
		}
		PageValue pv = rv.getPageValues().getValue(parameterName);
		String dt;
		if (pv == null) {
			pv = getParameterByEndWithType(parameterName);
			dt = pv.getDataType();
			if (pv.getLength() == -1) {
				String othVal = rv.getOtherValue(parameterName);
				if (othVal == null) {
					cst.setObject(index, null);
					debugParam(parameterName, "Object", index, "null");
				} else if (parameterName.endsWith(".HASH")) {
					Integer intVal = Integer.parseInt(othVal);
					cst.setInt(index, intVal);
					debugParam(parameterName, "INTEGER", index, String.valueOf(intVal));
				} else {
					cst.setString(index, othVal);
					debugParam(parameterName, "String", index, othVal);
				}
				return;
			}
		}
		dt = pv.getDataType();
		if (dt == null) {
			dt = pv.getValue() == null ? "STRING" : pv.getValue().getClass().getName();
		}
		dt = dt.toUpperCase().trim();

		if ("STRING".equals(dt) || "JAVA.LANG.STRING".equals(dt)) {
			String v1 = pv.getStringValue();
			cst.setString(index, v1);
			debugParam(parameterName, "String", index, v1 == null ? "null" : v1);
			return;
		}
		if ("BINARY".equals(dt) || "[B".equals(dt) || "BYTE[]".equals(dt)) {
			byte[] b = getParaBinary(pv);
			cst.setBytes(index, b);
			debugParam(parameterName, dt + "/byte[]", index, b == null ? "null" : "(" + b.length + ")");
			return;
		}
		if ("INT".equals(dt) || "INTEGER".equals(dt) || "JAVA.LANG.INTEGER".equals(dt)) {
			Integer intVal = getParaInteger(pv);
			if (intVal == null) {
				cst.setNull(index, java.sql.Types.INTEGER);
				debugParam(parameterName, dt + "/INT", index, "null");
			} else {
				cst.setInt(index, intVal);
				debugParam(parameterName, dt + "/INT", index, String.valueOf(intVal));
			}
			return;
		}
		if ("BIGINT".equals(dt) || "LONG".equals(dt) || "JAVA.LANG.LONG".equals(dt)) {
			Long longVal = getParaLong(pv);
			if (longVal == null) {
				cst.setNull(index, java.sql.Types.BIGINT);
				debugParam(parameterName, dt + "/LONG", index, "null");
			} else {
				cst.setLong(index, longVal);
				debugParam(parameterName, dt + "/LONG", index, String.valueOf(longVal));
			}
			return;
		}
		if ("NUMBER".equals(dt) || "DOUBLE".equals(dt) || "JAVA.LANG.DOUBLE".equals(dt)) {
			BigDecimal dbVal = getParaBigDecimal(pv);
			if (dbVal == null) {
				cst.setNull(index, java.sql.Types.DOUBLE);
				debugParam(parameterName, dt + "/BigDecimal", index, "null");
			} else {
				cst.setBigDecimal(index, dbVal);
				debugParam(parameterName, dt + "/BigDecimal", index, dbVal.toString());
			}
			return;
		}
		if ("DATE".equals(dt) || "JAVA.UTIL.DATE".equals(dt) || "JAVA.SQL.DATE".equals(dt)) {
			Timestamp t1 = getParaTimestamp(pv);
			if (t1 == null) {
				cst.setNull(index, java.sql.Types.TIMESTAMP);
				debugParam(parameterName, "Timestamp", index, "null");
			} else {
				cst.setTimestamp(index, t1);
				debugParam(parameterName, dt + "/Timestamp", index, t1.toString());
			}
			return;
		}
		if ("BOOLEAN".equals(dt) || "BOOL".equals(dt) || "BOOLEN".equals(dt) || "JAVA.LANG.BOOLEAN".equals(dt)) {
			boolean v = Utils.cvtBool(pv.getStringValue());
			cst.setBoolean(index, v);
			debugParam(parameterName, "Bool", index, String.valueOf(v));
			return;
		}
		if ("COM.GDXSOFT.EASYWEB.UTILS.TYPES.UINT64".equals(dt) || "UINT64".equals(dt)) {
			UInt64 uint64 = (UInt64) pv.getValue();
			cst.setBigDecimal(index, new BigDecimal(uint64.bigInteger()));
			debugParam(parameterName, dt + "/BigDecimal", index, uint64.toString());
			return;
		}
		if ("COM.GDXSOFT.EASYWEB.UTILS.TYPES.UINT32".equals(dt) || "UINT32".equals(dt)) {
			UInt32 uint32 = (UInt32) pv.getValue();
			cst.setLong(index, uint32.longValue());
			debugParam(parameterName, dt + "/Long", index, uint32.toString());
			return;
		}
		if ("COM.GDXSOFT.EASYWEB.UTILS.TYPES.UINT16".equals(dt) || "UINT16".equals(dt)) {
			UInt16 uint16 = (UInt16) pv.getValue();
			cst.setInt(index, uint16.intValue());
			debugParam(parameterName, dt + "/Int", index, uint16.toString());
			return;
		}
		if ("JAVA.MATH.BIGDECIMAL".equals(dt) || "BIGDECIMAL".equals(dt)) {
			BigDecimal bigd = (BigDecimal) pv.getValue();
			cst.setBigDecimal(index, bigd);
			debugParam(parameterName, dt, index, bigd.toString());
			return;
		}
		if ("JAVA.MATH.BIGINTEGER".equals(dt) || "BIGINTEGER".equals(dt)) {
			BigInteger bigi = (BigInteger) pv.getValue();
			cst.setBigDecimal(index, new BigDecimal(bigi));
			debugParam(parameterName, dt + "/BigDecimal", index, bigi.toString());
			return;
		}
		String fallbackVal = pv.getStringValue();
		cst.setString(index, fallbackVal);
		debugParam(parameterName, dt, index, fallbackVal == null ? "null" : fallbackVal);
	}

	private void debugParam(String paramName, String type, int index, String value) {
		if (owner.getDebugFrames() != null) {
			owner.writeDebug(owner, "添加参数(" + type + ")" + index, paramName + "=" + value);
		}
	}

	// ═══════════════════════════════════════════════════════════════
	//  Type Conversion Helpers
	// ═══════════════════════════════════════════════════════════════

	public String sqlParameterStringExp(String parameter) {
		return sqlParameterStringExp(parameter, owner.getDatabaseType());
	}

	public String sqlParameterStringExp(String parameter, String databaseType) {
		if (parameter == null) return "NULL";
		if (parameter.length() == 0) return "''";
		parameter = parameter.replace("'", "''");
		if (databaseType != null) {
			if (SqlUtils.isMySql(databaseType)) {
				// MySQL 默认 \ 是转义符 → 需要 \\ 表示 \；
				// NO_BACKSLASH_ESCAPES 模式下 \ 是普通字符 → 不双写
				if (!owner.isMysqlNoBackslashEscapes()) {
					parameter = parameter.replace("\\", "\\\\");
				}
			} else if (SqlUtils.isHsqlDb(databaseType)) {
				parameter = parameter.replace("\\", "\\\\");
			}
			parameter = "'" + parameter + "'";
			if (SqlUtils.isSqlServer(databaseType)) {
				parameter = "N" + parameter;
			}
		} else {
			parameter = "'" + parameter + "'";
		}
		return parameter;
	}

	public String sqlFieldOrTableExp(String fieldOrTable) {
		String dbType = owner.getDatabaseType();
		if ("MYSQL".equalsIgnoreCase(dbType)) {
			return "`" + fieldOrTable + "`";
		} else if ("MSSQL".equalsIgnoreCase(dbType)) {
			return "[" + fieldOrTable + "]";
		}
		return fieldOrTable;
	}

	public String getDateTimePara(String s1) {
		if (s1 == null || s1.trim().length() == 0) return null;
		String s2 = s1.replace("'", "");
		Timestamp timestamp = getTimestamp(s2);
		return getDateTimePara(timestamp);
	}

	public String getDateTimePara(Timestamp dt) {
		if (dt == null) return null;
		String s2 = Utils.getDateTimeString(new Date(dt.getTime()));
		if ("ORACLE".equalsIgnoreCase(owner.getDatabaseType())) {
			return "to_date('" + s2 + "','YYYY-MM-DD HH24:MI:SS')";
		}
		return "'" + s2 + "'";
	}

	public Timestamp getTimestamp(String s1) {
		RequestValue rv = owner.getRequestValue();
		String lang = rv != null ? rv.getLang() : "zhcn";
		boolean isUKFormat = false;
		if (ConfExtraGlobals.getInstance() != null && "enus".equalsIgnoreCase(lang)) {
			ConfExtraGlobal extra = ConfExtraGlobals.getInstance().getConfExtraGlobalByLang(lang);
			if (extra != null) {
				if (extra.getDate() != null && extra.getDate().equalsIgnoreCase(UFormat.DATE_FROMAT_ENUS)) {
					isUKFormat = true;
				} else if (rv != null) {
					isUKFormat = UFormat.DATE_FROMAT_ENUS.equalsIgnoreCase(rv.s("SYS_EWA_ENUS_YMD"));
				}
			}
		}
		try {
			return Utils.getTimestamp(s1, lang, isUKFormat);
		} catch (Exception e) {
			LOGGER.error("转换时间错误: {} , lang={}, isUKFormat={}", s1, lang, isUKFormat);
			return null;
		}
	}

	private byte[] getParaBinary(PageValue pv) { return pv.toBinary(); }

	private Timestamp getParaTimestamp(PageValue pv) {
		Object t1 = pv.getValue();
		if (t1 == null) return null;
		Timestamp tt1;
		if (t1 instanceof java.util.Date) {
			tt1 = new Timestamp(((java.util.Date) t1).getTime());
		} else {
			String v1 = t1.toString();
			if (v1.trim().length() == 0) return null;
			tt1 = getTimestamp(v1);
		}
		if (pv.getName() != null && pv.getName().equalsIgnoreCase("SYS_DATE")) {
			return tt1;
		}
		if (owner.getTimeDiffMinutes() != 0) {
			tt1 = (Timestamp) Utils.getTimeDiffValue(tt1, owner.getTimeDiffMinutes());
		}
		return tt1;
	}

	private Integer getParaInteger(PageValue pv) { return pv.toInteger(); }
	private Long getParaLong(PageValue pv) { return pv.toLong(); }
	private BigDecimal getParaBigDecimal(PageValue pv) { return pv.toBigDecimal(); }

	// ═══════════════════════════════════════════════════════════════
	//  Accessors
	// ═══════════════════════════════════════════════════════════════

	public EwaSqlFunctions getEwaSqlFunctions() { return ewaSqlFunctions; }
	public CreateSplitData getCreateSplitData() { return createSplitData; }
	public void clearCreateSplitData() {
		if (createSplitData != null) {
			createSplitData.clearEwaSplitTempData();
			createSplitData = null;
		}
	}
}
