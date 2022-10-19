package com.gdxsoft.easyweb.datasource;

public class SqlUtils {
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
