package com.gdxsoft.easyweb.utils;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import com.gdxsoft.easyweb.global.EwaGlobals;

public class UFormat {

	public static String formatValue(String format, Object oriValue, String lang) throws Exception {
		if (oriValue == null)
			return null;
		if (format == null || format.trim().length() == 0)
			return oriValue.toString();

		String f = format.trim().toLowerCase();
		// 日期型
		if (f.indexOf("date") >= 0 || f.indexOf("time") >= 0) {
			return formatDate(format, oriValue, lang);
		} else if (f.equals("age")) { // 年龄 当前年-出生年
			return formatAge(oriValue);
		} else if (f.equals("int")) {
			return formatInt(oriValue);
		} else if (f.equals("money")) {
			return formatMoney(oriValue);
		} else if (f.equals("fixed2")) { // 保留2位小数
			String m = formatMoney(oriValue);
			return m == null ? null : m.replaceAll(",", "");
		} else if (f.equals("leastmoney")) { // 清除小数后的0
			return formatNumberClearZero(oriValue);
		} else if (f.equals("leastdecimal")) {// 清除小数后的0,没有逗号
			return formatDecimalClearZero(oriValue);
		} else if (f.equals("percent")) {// jinzhaopeng 20121114 增加百分比格式
			return formatPercent(oriValue);
		} else if (f.equals("week")) {
			return formatWeek(oriValue, lang);
		}
		return oriValue.toString();
	}

	/**
	 * 返回年龄 当前年-出生年
	 * 
	 * @param dbo
	 *            出生日期
	 * @return
	 */
	public static String formatAge(Object dbo) {
		if (dbo == null)
			return null;
		Date t;
		String cName = dbo.getClass().getName().toUpperCase();
		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			if (dbo.toString().length() < 10) {
				return dbo.toString();
			}
			String[] ss = dbo.toString().split(" ");
			t = Utils.getDate(ss[0]);
		} else {
			t = (Date) dbo;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(t);
		Calendar calToday = Calendar.getInstance();
		cal.setTime(t);

		int age = calToday.get(Calendar.YEAR) - cal.get(Calendar.YEAR);

		return age + "";
	}

	public static String formatWeek(Object oriValue, String lang) throws Exception {
		if (oriValue == null)
			return null;

		String cName = oriValue.getClass().getName().toUpperCase();
		Date t;

		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			t = Utils.getDate(oriValue.toString());
		} else {
			t = (Date) oriValue;
		}

		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(t);

		int wk = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
		EwaGlobals g = EwaGlobals.instance();
		String[] wks = g.getEwaSettings().getItem(lang).getWeeks();

		return wks[wk];
	}

	public static String formatDate(String format, Object oriValue, String lang) throws Exception {
		if (oriValue == null)
			return null;
		if (format == null || format.trim().length() == 0)
			return oriValue.toString();
		String f = format.trim().toLowerCase();
		String cName = oriValue.getClass().getName().toUpperCase();

		String sDate = null;
		String sTime = null;
		String sDt = null;

		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			if (oriValue.toString().length() < 10) {
				return oriValue.toString();
			}
			String[] ss = oriValue.toString().split(" ");
			sDate = ss[0];
			sTime = "";
			if (ss.length > 1) {
				sTime = ss[1];
			} else if (ss.length == 0 && ss[0].indexOf(":") > 0) {// 时间
				sDate = "";
				sTime = ss[0];
			}
			sDt = sDate + " " + sTime;
		} else {
			EwaGlobals g = EwaGlobals.instance();
			String dateFormat = g.getEwaSettings().getItem(lang).getDate();

			Date t = (Date) oriValue;
			sDate = Utils.getDateString(t, dateFormat);
			sTime = Utils.getTimeString(t);
			sDt = sDate + " " + sTime;
		}
		// 日期格式
		if (f.equals("date")) {
			return sDate;
		}
		// 日期和时间格式
		if (f.equals("datetime")) {
			return sDt;
		}
		if (f.equals("time")) {
			return sTime;
		}

		String sTimeShort = sTime.lastIndexOf(":") > 0 ? sTime.substring(0, sTime.lastIndexOf(":")) : sTime;
		String sDateShort = sDate.substring(5);
		if (lang != null && lang.trim().equalsIgnoreCase("enus")) {
			sDateShort = sDate.substring(0, 5);
		}

		// 日期和短时间格式
		if (f.equals("dateshorttime")) {
			return sDate + " " + sTimeShort;
		}
		if (f.equals("shortdatetime")) {
			return sDateShort + " " + sTimeShort;
		}
		if (f.equals("shorttime")) {
			return sTimeShort;
		}
		if (f.equals("shortdate")) {
			return sDateShort;
		}
		return oriValue.toString();
	}

	public static String formatInt(Object oriValue) {
		if (oriValue == null)
			return null;
		String v1 = oriValue.toString();
		String[] v2 = v1.split("\\.");
		return v2[0];
	}

	public static String formatMoney(Object oriValue) {
		if (oriValue == null)
			return null;
		try {
			double number = Double.parseDouble(oriValue.toString());

			NumberFormat numberFormat = NumberFormat.getNumberInstance();
			numberFormat.setMaximumFractionDigits(2);
			numberFormat.setMinimumFractionDigits(2);
			return numberFormat.format(number);
		} catch (Exception err) {
			return oriValue.toString();
		}
		/*
		 * String v1 = oriValue.toString(); String[] v2 = v1.split("\\."); //java.text.
		 * if (v2.length == 1) { return v2[0] + ".00"; } else { v1 =
		 * v2[0].trim().length() == 0 ? "0" : v2[0] + "."; String v3 = v2[1] + "0000";
		 * return v1 + v3.substring(0, 2); }
		 */
	}

	/**
	 * 格式为百分数
	 * 
	 * @param oriValue
	 * @return
	 * @throws Exception
	 */
	public static String formatPercent(Object oriValue) throws Exception {
		if (oriValue == null)
			return null;
		double d1 = UConvert.ToDouble(oriValue.toString()) * 100;
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String v1 = df.format(d1) + "%";
		return v1;
	}

	/**
	 * 格式化为有逗号分隔的数字，并清除小数末尾的0，最多保留4位小数
	 * 
	 * @param oriValue
	 * @return
	 * @throws Exception
	 */
	public static String formatNumberClearZero(Object oriValue) throws Exception {
		if (oriValue == null) {
			return null;
		}
		try {
			double d1 = Double.parseDouble(oriValue.toString());
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumFractionDigits(0);
			nf.setMaximumFractionDigits(4);
			return nf.format(d1);
		} catch (Exception err) {
			return oriValue.toString();
		}
	}

	/**
	 * 清除小数末尾的0，最多保留4位小数
	 * 
	 * @param oriValue
	 * @return
	 * @throws Exception
	 */
	public static String formatDecimalClearZero(Object oriValue) throws Exception {
		if (oriValue == null) {
			return null;
		}
		try {
			double d1 = Double.parseDouble(oriValue.toString());
			java.text.DecimalFormat df = new java.text.DecimalFormat("#.####");
			return df.format(d1);
		} catch (Exception err) {
			return oriValue.toString();
		}
	}
}
