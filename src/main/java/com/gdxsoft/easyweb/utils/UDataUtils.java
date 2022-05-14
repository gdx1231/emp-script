package com.gdxsoft.easyweb.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTColumns;
import com.gdxsoft.easyweb.data.DTRow;

public class UDataUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	/**
	 * 获取两个数据行数据是不一致的字段
	 * 
	 * @param rowFrom            来源
	 * @param rowTo              目的
	 * @param skipNotExistsField 忽略不存在的字段
	 * @return 不一致的字段
	 */
	public static List<String> getNotEqualsFields(DTRow rowFrom, DTRow rowTo, boolean skipNotExistsField) {
		DTColumns fromCols = rowFrom.getTable().getColumns();
		DTColumns toCols = rowTo.getTable().getColumns();
		List<String> alNotEuals = new ArrayList<String>();
		for (int i = 0; i < rowFrom.getCount(); i++) {
			Object o1 = rowFrom.getCell(i).getValue();
			String field_name = fromCols.getColumn(i).getName();
			if (field_name.equalsIgnoreCase("EWA_KEY")) {
				continue;
			}
			if (!toCols.testName(field_name)) {// 目的表中字段不存在
				if (!skipNotExistsField) { // 不忽略不存在的字段
					alNotEuals.add(field_name);
				}
				continue;
			}

			int colIndex = toCols.getNameIndex(field_name);
			Object o2 = rowTo.getCell(colIndex).getValue();

			try {
				if (!checkEquals(o1, o2)) {
 					alNotEuals.add(field_name);
				}
			} catch (Exception err) {
				LOGGER.error("Check row equals. {},{},{}", o1, o2, err.getMessage());
				alNotEuals.add(field_name);
			}
		}
		return alNotEuals;
	}

	/**
	 * 比较两个数据行数据是否一致
	 * 
	 * @param rowFrom            来源
	 * @param rowTo              目的
	 * @param skipNotExistsField 忽略不存在的字段
	 * @return
	 */
	public static boolean checkRowEquals(DTRow rowFrom, DTRow rowTo, boolean skipNotExistsField) {
		DTColumns fromCols = rowFrom.getTable().getColumns();
		DTColumns toCols = rowTo.getTable().getColumns();

		for (int i = 0; i < rowFrom.getCount(); i++) {
			Object from = rowFrom.getCell(i).getValue();
			String field_name = fromCols.getColumn(i).getName();
			if (field_name.equalsIgnoreCase("EWA_KEY")) {
				continue;
			}
			if (!toCols.testName(field_name)) {// 目的表中字段不存在
				if (!skipNotExistsField) {// 不忽略不存在的字段
					return false;
				}
				continue;
			}

			int colIndex = toCols.getNameIndex(field_name);
			Object to = rowTo.getCell(colIndex).getValue();

			try {
				if (!checkEquals(from, to)) {
					return false;
				}
			} catch (Exception err) {
				LOGGER.error("Check row equals. {},{},{}", from, to, err.getMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * 比较两个对象值是否一致，不判断对象类型一致<br>
	 * BigDecimal compareTo=0<br>
	 * Timestamp/Date getTime<br>
	 * JSONObject every property<br>
	 * Others toString.equals
	 * 
	 * @param from 来源
	 * @param to   目的
	 * @return true/false
	 */
	public static boolean checkEquals(Object from, Object to) {
		if (from == null && to == null) {
			return true;
		}
		if (from == null && to != null) {
			return false;
		}
		if (from != null && to == null) {
			return false;
		}
		LOGGER.debug("{}:{}", from.getClass(), to.getClass());
		if (from instanceof java.sql.Timestamp || to instanceof java.sql.Timestamp) {
			Date t1;
			Date t2;
			if (from instanceof java.sql.Timestamp) {
				t1 = new Date(((java.sql.Timestamp) from).getTime());
			} else {
				t1 = Utils.getDate(from.toString(), "yyyy-MM-dd HH:mm:ss");
			}
			if (to instanceof java.sql.Timestamp) {
				t2 = new Date(((java.sql.Timestamp) to).getTime());
			} else {
				t2 = Utils.getDate(to.toString(), "yyyy-MM-dd HH:mm:ss");
			}
			return t1.getTime() == t2.getTime();
		}

		if (from instanceof java.util.Date || to instanceof java.util.Date) {
			Date t1;
			Date t2;
			if (from instanceof java.util.Date) {
				t1 = new Date(((java.util.Date) from).getTime());
			} else {
				t1 = Utils.getDate(from.toString(), "yyyy-MM-dd HH:mm:ss");
			}
			if (to instanceof java.util.Date) {
				t2 = new Date(((java.util.Date) to).getTime());
			} else {
				t2 = Utils.getDate(to.toString(), "yyyy-MM-dd HH:mm:ss");
			}
			return t1.getTime() == t2.getTime();
		}

		if (from instanceof org.json.JSONObject || to instanceof org.json.JSONObject) {
			JSONObject jfrom = new JSONObject(from.toString());
			JSONObject jto = new JSONObject(to.toString());
			Iterator<String> it = jfrom.keys();
			while (it.hasNext()) {
				String key = it.next();
				Object jv1 = jfrom.get(key);
				Object jv2 = jto.get(key);
				if (!checkEquals(jv1, jv2)) {
					return false;
				}
			}
			return true;
		}

		if (from instanceof java.math.BigDecimal || to instanceof java.math.BigDecimal) {
			BigDecimal bd1 = new BigDecimal(from.toString());
			BigDecimal bd2 = new BigDecimal(to.toString());

			return bd1.compareTo(bd2) == 0;
		}

		// 二进制数组
		if (from.getClass().getName().equals("[B") && to.getClass().getName().equals("[B")) {
			byte[] from1 = (byte[]) from;
			byte[] to1 = (byte[]) to;
			if (from1.length != to1.length) {
				return false;
			}
			for (int i = 0; i < from1.length; i++) {
				if (from1[i] != to1[i]) { // 按字节比较
					return false;
				}
			}
		}
		// 字符串比较
		return from.toString().equals(to.toString());
	}

}
