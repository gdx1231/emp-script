package com.gdxsoft.easyweb.datasource;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gdxsoft.easyweb.script.display.frame.FrameParameters;

public class SqlUtils {
	/**
	 * MYSQL保留词
	 */
	public static HashMap<String, Boolean> MYSQL_RESERVED;
	static {
		String[] reseverds = ("ADD,ALL,ALTER,ANALYZE,AND,AS,ASC,ASENSITIVE,BEFORE,BETWEEN,BIGINT,BINARY"
				+ ",BLOB,BOTH,BY,CALL,CASCADE,CASE,CHANGE,CHAR,CHARACTER,CHECK,COLLATE,COLUMN,CONDITION"
				+ ",CONNECTION,CONSTRAINT,CONTINUE,CONVERT,CREATE,CROSS,CURRENT_DATE,CURRENT_TIME,"
				+ "CURRENT_TIMESTAMP,CURRENT_USER,CURSOR,DATABASE,DATABASES,DAY_HOUR,DAY_MICROSECOND"
				+ ",DAY_MINUTE,DAY_SECOND,DEC,DECIMAL,DECLARE,DEFAULT,DELAYED,DELETE,DESC,DESCRIBE"
				+ ",DETERMINISTIC,DISTINCT,DISTINCTROW,DIV,DOUBLE,DROP,DUAL,EACH,ELSE,ELSEIF,ENCLOSED"
				+ ",ESCAPED,EXISTS,EXIT,EXPLAIN,FALSE,FETCH,FLOAT,FLOAT4,FLOAT8,FOR,FORCE,FOREIGN,FROM"
				+ ",FULLTEXT,GOTO,GRANT,GROUP,HAVING,HIGH_PRIORITY,HOUR_MICROSECOND,HOUR_MINUTE"
				+ ",HOUR_SECOND,IF,IGNORE,IN,INDEX,INFILE,INNER,INOUT,INSENSITIVE,INSERT,INT,INT1"
				+ ",INT2,INT3,INT4,INT8,INTEGER,INTERVAL,INTO,IS,ITERATE,JOIN,KEY,KEYS,KILL,LABEL"
				+ ",LEADING,LEAVE,LEFT,LIKE,LIMIT,LINEAR,LINES,LOAD,LOCALTIME,LOCALTIMESTAMP,LOCK"
				+ ",LONG,LONGBLOB,LONGTEXT,LOOP,LOW_PRIORITY,MATCH,MEDIUMBLOB,MEDIUMINT,MEDIUMTEXT"
				+ ",MIDDLEINT,MINUTE_MICROSECOND,MINUTE_SECOND,MOD,MODIFIES,NATURAL,NOT"
				+ ",NO_WRITE_TO_BINLOG,NULL,NUMERIC,ON,OPTIMIZE,OPTION,OPTIONALLY,OR,ORDER,OUT"
				+ ",OUTER,OUTFILE,PRECISION,PRIMARY,PROCEDURE,PURGE,RAID0,RANGE,READ,READS,REAL"
				+ ",REFERENCES,REGEXP,RELEASE,RENAME,REPEAT,REPLACE,REQUIRE,RESTRICT,RETURN,REVOKE"
				+ ",RIGHT,RLIKE,SCHEMA,SCHEMAS,SECOND_MICROSECOND,SELECT,SENSITIVE,SEPARATOR,SET,SHOW"
				+ ",SMALLINT,SPATIAL,SPECIFIC,SQL,SQLEXCEPTION,SQLSTATE,SQLWARNING,SQL_BIG_RESULT"
				+ ",SQL_CALC_FOUND_ROWS,SQL_SMALL_RESULT,SSL,STARTING,STRAIGHT_JOIN,TABLE,TERMINATED"
				+ ",THEN,TINYBLOB,TINYINT,TINYTEXT,TO,TRAILING,TRIGGER,TRUE,UNDO,UNION,UNIQUE,UNLOCK"
				+ ",UNSIGNED,UPDATE,USAGE,USE,USING,UTC_DATE,UTC_TIME,UTC_TIMESTAMP,VALUES,VARBINARY"
				+ ",VARCHAR,VARCHARACTER,VARYING,WHEN,WHERE,WHILE,WITH,WRITE,X509,XOR,YEAR_MONTH,ZEROFILL"
				+ ",CHARACTER,TIMESTAMP,ACTION,BIT,DATE,ENUM,NO,TEXT,TIME").split(",");
		MYSQL_RESERVED = new HashMap<String, Boolean>();
		for (int i = 0; i < reseverds.length; i++) {
			String reseverd = reseverds[i].trim().toUpperCase();
			MYSQL_RESERVED.put(reseverd, true);
		}
	}
	/**
	 * 需要用中文排序的数据库类型
	 */
	public final static String[] CHN_DBS = { "MySql", "MariaDB", "PostgreSql" };
	/**
	 * 中文排序的模板列表，对应CHN_DBS<br>
	 * MySQL utf8 的中文排序 convert([FIELD] using gbk)<br>
	 * PostgreSQL utf8字段中文排序 convert_to([FIELD],'gb18030')
	 */
	public final static String[] CHN_TEMPLATES = { "convert([FIELD] using gbk)", "convert([FIELD] using gbk)",
			"convert_to([FIELD],'gb18030')" };

	/**
	 * 查找自增的sql的返回字段, 例如 -- auto MEMO_ID
	 * 
	 * @param sql
	 * @return
	 */
	public static String getAutoField(String sql) {
		// 查找自增的sql, 例如 -- auto MEMO_ID
		String[] sqls = sql.split("\n");
		String auto_field = null;
		// 从后向前找
		for (int m = sqls.length - 1; m >= 0; m--) {
			String len = sqls[m].trim().toUpperCase();
			if (len.startsWith("--")) {
				len = len.replace("--", "").trim();
				// 第3个的空格是 32 &nbsp;
				if (len.indexOf("AUTO ") >= 0 || len.indexOf("AUTO\t") >= 0 || len.indexOf("AUTO ") >= 0) {
					auto_field = len.replaceFirst("AUTO", "").trim();
					return auto_field;
				}
			}
		}
		return null;
	}

	/**
	 * 生产的列表数据转换为JSONObject<br>
	 * 例如 -- ewa_kv json_name, key_field_name,
	 * value_field_name，用于转换为RequestValue的json对象
	 * 
	 * @param sql
	 * @return 数组，0：json_name，1：key_field_name，2：value_field_name
	 */
	public static String[] getKVParameters(String sql) {
		String checkName = "EWA_KV";

		String[] sqls = sql.split("\n");
		String[] jsonResult = null;
		String findLen = null;
		// 从后向前找
		for (int m = sqls.length - 1; m >= 0; m--) {
			String len = sqls[m].trim().toUpperCase();
			if (len.startsWith("--")) {
				len = len.replace("--", "").trim();
				boolean isJson = checkStartWord(len, checkName);
				if (isJson) {
					findLen = len;
					break;
				}
			}
		}
		if (findLen == null) {
			return null;
		}
		findLen = findLen.replace(checkName, "").trim();
		jsonResult = findLen.split(",");
		for (int i = 0; i < jsonResult.length; i++) {
			jsonResult[i] = jsonResult[i].trim();
		}

		return jsonResult;
	}

	/**
	 * 获取 SQL 的with部分和sql部分，用于分页查询
	 * 
	 * @param sql
	 * @return 如果存在[withSql, selectSql]，否则null
	 */
	public static String[] getSqlWithBlock(String sql) {
		String[] sqls = sql.split("\n");
		// 检查with
		StringBuilder sqlWith = new StringBuilder();
		StringBuilder sqlSelect = new StringBuilder();
		boolean findWith = false;
		boolean findSelect = false;
		int leftBracket = 0; // 左括号数量
		/*
		 * with t1 as ( SELECT * from GRP_SER_SP WHERE GSSM_ID > 0 ) -- 必须在新行上 select *
		 * from t1
		 */
		for (int i = 0; i < sqls.length; i++) {
			String line = sqls[i] + "\n";
			findWith = findWith || checkStartWord(line, "WITH");
			if (!findSelect && (leftBracket == 0 || !findWith)) {
				findSelect = checkStartWord(line, "SELECT");
			}
			int leftStartLoc = line.indexOf("(");
			while (leftStartLoc >= 0) {
				leftBracket++;
				leftStartLoc = line.indexOf("(", leftStartLoc + 1);
				if (leftBracket > 1000) {
					break;
				}
			}
			int rightStartLoc = line.indexOf(")");
			while (rightStartLoc >= 0) {
				leftBracket--;
				rightStartLoc = line.indexOf(")", rightStartLoc + 1);
				if (rightStartLoc > 1000) {
					break;
				}
			}
			if (leftBracket < 0) {
				System.out.println(leftBracket);
			}
			// 当左括号为0时候检查
			if (findSelect && !findWith) {
				// 没有with语句
				break;
			}
			if (findSelect) {
				sqlSelect.append(line);
			} else {
				sqlWith.append(line);
			}
		}
		if (!findWith) {
			return null;
		}

		return new String[] { sqlWith.toString(), sqlSelect.toString() };
	}

	/**
	 * 判断 特定字符出现在非注释的SQL的0位置，多行的SQL只进行第一次判断
	 * 
	 * @param sql
	 * @param word 关键单词，例如SELECT
	 * @return
	 */
	public static boolean checkStartWord(String sql, String word) {
		if (word == null || word.trim().length() == 0) {
			return false;
		}
		if (sql == null || sql.trim().length() == 0) {
			return false;
		}
		// 删除sql 的多行备注 /* */
		sql = removeSqlMuitiComment(sql);

		String[] sqls = sql.split("\n");
		word = word.toUpperCase().trim();
		String chk0 = word + " ";
		String chk1 = word + "\t";
		String chk2 = word + " "; // 空格是 32 &nbsp;
		for (int m = 0; m < sqls.length; m++) {
			String line = sqls[m].trim().toUpperCase();
			if (line.length() == 0 || line.startsWith("--")) {
				// 空行和 -- 注释
				continue;
			}

			if (line.indexOf(chk0) == 0 || line.indexOf(chk1) == 0 || line.indexOf(chk2) == 0 || line.equals(word)) {
				// 非注释和空行的第一次出现进行判断
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 检查是否为 select语句或标记为<b>-- EWA_IS_SELECT</b>
	 * 
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean checkIsSelect(String sql) {
		if (checkStartWord(sql, "SELECT")) {
			int intoIndex = sql.toLowerCase().indexOf("into");
			if (intoIndex == -1) {
				return true;
			}
			int fromIndex = sql.toLowerCase().indexOf("from");
			if (intoIndex < fromIndex && fromIndex > 0) {
				// select * into aa from bb SQLSEREVER
				return false;
			}
			if (fromIndex == -1) {
				// select 1 a,2 b into #temp
				return false;
			}
			return true;
		}
		// 强制执行为select 模式，解决with xxx as 语句后面的selec
		if (SqlUtils.ewaIsSelect(sql)) {
			return true;
		}

		String[] result = SqlUtils.getSqlWithBlock(sql);
		if (result == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 是否比较更新前和更新后字段的变化, 方式：<br>
	 * SQL 添加 -- COMPARATIVE_CHANGES
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean isComparativeChanges(String sql) {
		// 记录更新前后变化的 ，方式 -- COMPARATIVE_CHANGES
		String[] sqls = sql.split("\n");
		for (int m = sqls.length - 1; m >= 0; m--) {
			String len = sqls[m].trim().toUpperCase();
			if (len.startsWith("--")) {
				len = len.replace("--", "").trim();
				if (len.equalsIgnoreCase("COMPARATIVE_CHANGES")) {
					// Comparative changes
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 删除sql 的多行备注
	 * 
	 * @param sql
	 * @return
	 */
	public static String removeSqlMuitiComment(String sql) {
		Pattern pat = Pattern.compile("(\\/\\*[^\\/]*\\*\\/)", Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(sql);
		String s1 = mat.replaceAll("");
		return s1;
	}

	/**
	 * 去除SQL注释后/* 和 --，是否有可执行的sql
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean checkHaveSql(String sql) {
		String sql1 = SqlUtils.removeSqlMuitiComment(sql);
		String[] sqls = sql1.split("\n");

		for (int m = 0; m < sqls.length; m++) {
			String len = sqls[m].trim();
			if (len.length() > 1 && !len.startsWith("--")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 强制执行为select 模式，解决with xxx as 语句后面的select<br>
	 * 标记为：<b>-- EWA_IS_SELECT</b>
	 * 
	 * @param sql
	 * @return true/false
	 */
	public static boolean ewaIsSelect(String sql) {
		return SqlUtils.checkStartWord(sql, FrameParameters.EWA_IS_SELECT);
	}

	public static boolean isMySql(String databaseType) {
		return "mysql".equalsIgnoreCase(databaseType) || "MariaDB".equalsIgnoreCase(databaseType);
	}

	public static boolean isMySql(DataConnection cnn) {
		return isMySql(cnn.getDatabaseType());
	}

	public static boolean isSqlServer(String databaseType) {
		return "mssql".equalsIgnoreCase(databaseType) || "sqlServer".equalsIgnoreCase(databaseType);
	}

	public static boolean isSqlServer(DataConnection cnn) {
		return isSqlServer(cnn.getDatabaseType());
	}

	public static boolean isPostgreSql(String databaseType) {
		return "PostgreSql".equalsIgnoreCase(databaseType);
	}

	public static boolean isPostgreSql(DataConnection cnn) {
		return isPostgreSql(cnn.getDatabaseType());
	}

	public static boolean isOracle(String databaseType) {
		return "Oracle".equalsIgnoreCase(databaseType);
	}

	public static boolean isOracle(DataConnection cnn) {
		return isOracle(cnn.getDatabaseType());
	}

	public static boolean isHsqlDb(String databaseType) {
		return "HSQLDB".equalsIgnoreCase(databaseType) || "H2".equalsIgnoreCase(databaseType);
	}

	public static boolean isHsqlDb(DataConnection cnn) {
		return isHsqlDb(cnn.getDatabaseType());
	}

	/**
	 * 根据数据库类型判断是否采用中文排序表达式
	 * 
	 * @param databaseType 数据库类
	 * @return 是否使用
	 */
	public static boolean checkChnOrderByDatabase(String databaseType) {
		if (databaseType == null || databaseType.length() == 0) {
			return false;
		}
		for (int i = 0; i < CHN_DBS.length; i++) {
			if (CHN_DBS[i].equalsIgnoreCase(databaseType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 通过字段类型判断是否使用中文排序
	 * 
	 * @param dataType 字段类型
	 * @return
	 */
	public static boolean checkChnOrderByType(String dataType) {
		if (dataType == null || dataType.length() == 0) {
			return true;
		}
		if (dataType.equalsIgnoreCase("bigint") || dataType.equalsIgnoreCase("int")
				|| dataType.equalsIgnoreCase("number") || dataType.equalsIgnoreCase("date")
				|| dataType.equalsIgnoreCase("time") || dataType.equalsIgnoreCase("binary")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 根据字段名称判断是字段是否使用中文排序
	 * 
	 * @param fieldName 字段名称
	 * @return
	 */
	public static boolean checkChnOrderByName(String fieldName) {
		fieldName = fieldName.trim().toUpperCase();
		if (fieldName.indexOf("(") > 0 || fieldName.endsWith("ID") || fieldName.endsWith("IDX")
				|| fieldName.endsWith("ORD") || fieldName.endsWith("DATE") || fieldName.endsWith("NUM")
				|| fieldName.endsWith("DAY") || fieldName.endsWith("TIME") || fieldName.endsWith("INC")
				|| fieldName.indexOf("MOENY") >= 0 || fieldName.indexOf("PRICE") >= 0 || fieldName.indexOf("STAR") >= 0
				|| fieldName.indexOf("SCORE") >= 0 || fieldName.indexOf("COUNT") >= 0 || fieldName.indexOf("SIZE") >= 0
				|| fieldName.indexOf("UNID") >= 0 || fieldName.indexOf("AGE") >= 0 || fieldName.endsWith("LVL")
				|| fieldName.endsWith("_EN") || fieldName.startsWith("EN_")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 根据不同的数据库类型获取中文排序模板
	 * 
	 * @param databaseType 数据库类型
	 * @return 排序模板
	 */
	public static String chnOrderTemplate(String databaseType) {
		for (int i = 0; i < CHN_DBS.length; i++) {
			if (CHN_DBS[i].equalsIgnoreCase(databaseType)) {
				return CHN_TEMPLATES[i];
			}
		}
		return "";
	}

	/**
	 * 根据数据库类型、字段名称、字段类型来判断并替换select 中文排序字段表达式
	 * 
	 * @param databaseType 数据库类型
	 * @param fieldName    字段名称
	 * @param dataType     字段类型
	 * @return 排序表达式
	 */
	public static String replaceChnOrder(String databaseType, String fieldName, String dataType) {
		if (!checkChnOrderByDatabase(databaseType)) {
			return fieldName;
		}
		if (checkChnOrderByName(fieldName) && checkChnOrderByType(dataType)) {
			return replaceChnOrder(databaseType, fieldName);
		}
		return fieldName;
	}

	/**
	 * 根据数据库类型来判断，替换select 中文排序字段表达式
	 * 
	 * @param databaseType 数据库类型
	 * @param fieldName    字段名称
	 * @return 排序表达式
	 */
	public static String replaceChnOrder(String databaseType, String fieldName) {
		String temp = chnOrderTemplate(databaseType);
		if (temp.length() == 0) {
			return fieldName;
		}

		return temp.replace("[FIELD]", fieldName);
	}
}
