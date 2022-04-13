package com.gdxsoft.easyweb.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTTable;

public class ReverseIds {
	private static Logger LOGGER = LoggerFactory.getLogger(ReverseIds.class);
	private DataConnection cnn;
	private static String ewa_ids_sub = "ewa_ids_sub";
	private static String ewa_ids_up = "ewa_ids_up";

	public ReverseIds(DataConnection cnn) {
		this.cnn = cnn;
	}

	public String replaceReverseIds(String sql) {
		// select * from adm_dept where dep_id in (
		// ewa_ids_sub('adm_dept','dep_id','dep_pid',@dep_id)
		// )
		for (int i = 0; i < 50; i++) {
			String sql1 = this.replaceReverseIds1(sql, ewa_ids_sub);
			if (sql1.equals(sql)) {
				break;
			}
			sql = sql1;
		}
		for (int i = 0; i < 50; i++) {
			String sql1 = this.replaceReverseIds1(sql, ewa_ids_up);
			if (sql1.equals(sql)) {
				break;
			}
			sql = sql1;
		}
		return sql;
	}

	public String replaceReverseIds1(String sql, String tag) {
		String sql1 = sql.toLowerCase();
		int loc = sql1.indexOf(tag);
		if (loc == -1) {
			return sql;
		}
		int locEnd = -1;
		boolean isQuote = false; // 是否在小引号里
		for (int i = loc + tag.length(); i < sql1.length(); i++) {
			char c = sql1.charAt(i);
			if (c == '\'') {// 小引号
				if (isQuote) {
					isQuote = false;
				} else {
					isQuote = true;
				}
			}
			if (c == ')' && !isQuote) {
				locEnd = i;
				break;
			}
		}

		if (locEnd == -1) {
			return sql;
		}

		String exp = sql.substring(loc, locEnd + 1);
		LOGGER.debug("解析{} ->\n    {}", sql, exp);

		String ids = getIds(exp);
		sql = sql.replace(exp, ids);
		return sql;
	}

	private String getIds(String para) {
		int loc0 = para.indexOf("(");
		int loc1 = para.lastIndexOf(")");
		String cmds = para.substring(loc0 + 1, loc1);
		String name = para.substring(0, loc0);
		boolean isQuote = false;
		int lastLoc = 0;
		List<String> paraNames = new ArrayList<>();
		for (int i = 0; i < cmds.length(); i++) {
			char c = cmds.charAt(i);
			if (c == '\'') {// 小引号
				if (isQuote) {
					isQuote = false;
				} else {
					isQuote = true;
				}
			} else if (c == ',' && !isQuote) {
				String paraName = cmds.substring(lastLoc, i).trim();
				if (paraName.indexOf("'") == 0) {
					paraName = paraName.substring(1);
				}
				if (paraName.endsWith("'")) {
					paraName = paraName.substring(0, paraName.length() - 1);
				}

				lastLoc = i + 1;
				paraNames.add(paraName.trim());
			}
		}
		String lastParaName = cmds.substring(lastLoc);
		if (lastParaName.indexOf("'") == 0) {
			lastParaName = lastParaName.substring(1);
		}
		if (lastParaName.endsWith("'")) {
			lastParaName = lastParaName.substring(0, lastParaName.length() - 1);
		}
		paraNames.add(lastParaName.trim());
		if (paraNames.size() != 4) {
			LOGGER.error("解析参数不够4个，" + para);
			return "解析参数不够4个，" + para;
		}
		LOGGER.debug("tableName={}, idName={}, pIdName={}, currentIdValue={}", paraNames.get(0), paraNames.get(1),
				paraNames.get(2), paraNames.get(3));
		String result;
		if (name.equalsIgnoreCase(ewa_ids_up)) {
			result = reverseUpIds(this.cnn, paraNames.get(0), paraNames.get(1), paraNames.get(2), paraNames.get(3));
		} else {
			result = reverseSubIds(this.cnn, paraNames.get(0), paraNames.get(1), paraNames.get(2), paraNames.get(3));
		}

		return result;
	}

	/**
	 * 递归获取所有下级编号，限制100次查询
	 * 
	 * @param cnn            数据库连接池
	 * @param tableName      表名
	 * @param idName         id字段名称
	 * @param pIdName        上级字段名称
	 * @param currentIdValue 当前记录id编号
	 * @return 所有下级编号，例如 'a','0','am','d''f'
	 */
	public static String reverseSubIds(DataConnection cnn, String tableName, String idName, String pIdName,
			String currentIdValue) {
		Map<String, Integer> map = new HashMap<>();

		String sql0 = "select " + idName + " from " + tableName + " where " + pIdName + " in";

		StringBuilder sbIds = new StringBuilder();
		if (currentIdValue.startsWith("@")) {
			sbIds.append(currentIdValue);
		} else {
			sbIds.append(cnn.sqlParameterStringExp(currentIdValue));
		}
		for (int i = 0; i < 100; i++) {
			String sql = sql0 + "(" + sbIds.toString() + ")";
			LOGGER.debug(sql);

			DTTable tb = DTTable.getJdbcTable(sql, cnn);
			if (tb.getCount() == 0) {
				break;
			}
			sbIds = new StringBuilder();
			for (int m = 0; m < tb.getCount(); m++) {
				String rid = tb.getCell(m, 0).toString();
				if (rid == null) {
					continue;
				}
				if (map.containsKey(rid)) {
					continue;
				}
				map.put(rid, 1);
				if (sbIds.length() > 0) {
					sbIds.append(",");
				}
				sbIds.append(cnn.sqlParameterStringExp(rid));
			}
			if (sbIds.length() == 0) {
				break;
			}
		}
		// 输出的ids表达式
		StringBuilder sbIdsOut = new StringBuilder();
		map.forEach((k, v) -> {
			if (sbIdsOut.length() > 0) {
				sbIdsOut.append(",");
			}
			sbIdsOut.append(cnn.sqlParameterStringExp(k));
		});
		// 'a','0','am','d''f'
		return sbIdsOut.toString();
	}

	/**
	 * 递归查找所有上级编号，限制100次查询
	 * 
	 * @param cnn            数据库链接
	 * @param tableName      表名称
	 * @param idName         id字段名称
	 * @param pIdName        上级字段名称
	 * @param currentIdValue 当前记录id编号
	 * @return 所有上级编号，例如 'a','0','am','d''f'
	 */
	public static String reverseUpIds(DataConnection cnn, String tableName, String idName, String pIdName,
			String currentIdValue) {
		Map<String, Integer> map = new HashMap<>();

		String sql0 = "select " + pIdName + " from " + tableName + " where " + idName + " =";
		String rid = currentIdValue;
		for (int i = 0; i < 100; i++) {
			String sql = sql0 + (rid.startsWith("@") ? rid : cnn.sqlParameterStringExp(rid));
			LOGGER.debug(sql);
			DTTable tb = DTTable.getJdbcTable(sql, cnn);
			if (tb.getCount() == 0) {
				break;
			}
			// 上级编号
			rid = tb.getCell(0, 0).toString();
			if (rid == null) {
				break;
			}
			map.put(rid, 1);
		}
		// 输出的ids表达式
		StringBuilder sbIdsOut = new StringBuilder();
		map.forEach((k, v) -> {
			if (sbIdsOut.length() > 0) {
				sbIdsOut.append(",");
			}
			sbIdsOut.append(cnn.sqlParameterStringExp(k));
		});
		// 'a','0','am','d''f'
		return sbIdsOut.toString();
	}
}
