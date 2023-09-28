package com.gdxsoft.easyweb.datasource;

import java.util.HashMap;

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
