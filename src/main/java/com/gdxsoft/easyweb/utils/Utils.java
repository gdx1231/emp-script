package com.gdxsoft.easyweb.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class Utils {
	private static char[] CHARS = "01234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
	
	private static long SECOND = 1000L;
	private static long MINUTE = 60L * SECOND;
	private static long HOR = 60L * MINUTE;
	private static long DAY = 24L * HOR;
	
	
	/**
	 * 利用随机数生成字符串 
	 * @param length 生成的长度
	 * 
	 * @return
	 */
	public static String randomStr(int length) {
		StringBuilder sb = new StringBuilder();
		int max = CHARS.length;
		for (int i = 0; i < length; i++) {
			String a = Math.random() * max + "";
			int b = Integer.parseInt(a.split("\\.")[0]);

			char c = CHARS[b];
			sb.append(c);
		}
		return sb.toString();
	}


	/**
	 * 替换原始字符串中的@参数
	 * 
	 * @param source
	 * @param rv
	 * @return
	 */
	public static String replaceParameters(String source, RequestValue rv) {
		if (source == null || rv == null)
			return source;
		MListStr a = Utils.getParameters(source, "@");
		MStr sb = new MStr(source);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val = rv.s(name);
			val = val == null ? "[NULL]" : val;

			String find = "@" + name;
			sb.replace(find, val);
		}
		return sb.toString();
	}

	/**
	 * 生成Json成对表达式
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public static String toJsonPair(String name, String value) {
		String k = name == null ? "null" : textToJscript(name);
		String v = value == null ? null : textToJscript(value);

		return "\"" + k + "\":" + (v == null ? "null" : "\"" + v + "\"");

	}

	/**
	 * t1-t2的间隔小时数
	 * 
	 * @param t1 时间1
	 * @param t2 时间2
	 * @return
	 */
	public static long timeDiffHours(Date t1, Date t2) {
		return timeDiffMSeconds(t1, t2) / HOR;
	}

	/**
	 * t1-t2的间隔天数
	 * 
	 * @param t1 时间1
	 * @param t2 时间2
	 * @return
	 */
	public static long timeDiffDays(Date t1, Date t2) {
		return timeDiffMSeconds(t1, t2) / DAY;
	}

	/**
	 * t1-t2的间隔分钟数
	 * 
	 * @param t1 时间1
	 * @param t2 时间2
	 * @return
	 */
	public static long timeDiffMinutes(Date t1, Date t2) {
		return timeDiffMSeconds(t1, t2) / MINUTE;
	}

	/**
	 * t1-t2的间隔秒数
	 * 
	 * @param t1 时间1
	 * @param t2 时间2
	 * @return
	 */
	public static long timeDiffSeconds(Date t1, Date t2) {
		return timeDiffMSeconds(t1, t2) / SECOND;
	}

	/**
	 * t1-t2的间隔毫秒
	 * 
	 * @param t1 时间1
	 * @param t2 时间2
	 * @return
	 */
	public static long timeDiffMSeconds(Date t1, Date t2) {
		return t1.getTime() - t2.getTime();
	}

	/**
	 * 分割双重数组
	 * 
	 * @param s1           字符串
	 * @param splitString0 第一层数组分割符
	 * @param splitString1 第二层数组分割符
	 * @return 双重数组
	 */
	public static String[][] split2String(String s1, String splitString0, String splitString1) {
		String[] v0 = splitString(s1, splitString0);
		String[][] v = new String[v0.length][];
		for (int i = 0; i < v0.length; i++) {
			String[] v1 = splitString(v0[i], splitString1);
			v[i] = v1;
		}

		return v;
	}

	/**
	 * 分割字符串，内容中若保留分割符，则表示两次，如“，”分割的话，内容中保留则通过",,"表示
	 * 
	 * @param s1          字符
	 * @param splitString 分割符
	 * @return
	 */
	public static String[] splitString(String s1, String splitString) {
		String tmp = "121323@!!~~@aasdas";
		s1 = s1.replace(splitString + splitString, tmp);
		String[] s2 = s1.split("\\" + splitString);
		for (int i = 0; i < s2.length; i++) {
			s2[i] = s2[i].replace(tmp, splitString).trim();
		}
		return s2;
	}

	public static boolean cvtBool(Object v) {
		if (v == null)
			return false;
		String v1 = v.toString().trim();
		try {
			double d = Double.parseDouble(v1);
			if (d > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			if (v1.equalsIgnoreCase("true") || v1.equalsIgnoreCase("y") || v1.equalsIgnoreCase("yes")
					|| v1.equals("是")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将字符串转换为数字，并忽略错误
	 * 
	 * @param v
	 * @return
	 */
	public static Integer cvtInteger(String v) {
		if (v == null) {
			return null;
		}
		if (v.trim().length() == 0) {
			return 0;
		}
		try {
			int v1 = Integer.parseInt(v);
			return v1;
		} catch (Exception e) {
			return null;
		}
	}

	public static int cvtInt(String v) {
		Integer i = cvtInteger(v);
		if (i == null) {
			return 0;
		}
		return i.intValue();
	}

	/**
	 * 将数组拼接为字符串
	 * 
	 * @param arr
	 * @param joinString
	 * @return
	 */
	public static String arrayJoin(String[] arr, String joinString) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(joinString);
			}
			sb.append(arr[i] == null ? "" : arr[i].trim());
		}
		return sb.toString();
	}

	/**
	 * 生成加密字符串
	 * 
	 * @param s1 原始字符
	 * @return 加密字符
	 */
	public static String createEncryptString(String s1) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(s1.getBytes());
			return byte2hex(md.digest());
		} catch (NoSuchAlgorithmException e) {
			return e.getMessage();
		}
	}

	/**
	 * 取bytes的md5
	 * 
	 * @param bytes
	 * @return md5字符串
	 */
	public static String md5(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			return byte2hex(md.digest());
		} catch (NoSuchAlgorithmException e) {
			return e.getMessage();
		}

	}

	/**
	 * 取str的md5
	 * 
	 * @param str 字符串
	 * @return md5字符串
	 */
	public static String md5(String str) {
		try {
			byte[] buf = str.getBytes("utf-8");
			return md5(buf);
		} catch (UnsupportedEncodingException e) {
			return e.toString();
		}

	}

	/**
	 * 将byte数组转换为16进制字符串
	 * 
	 * @param b byte数组
	 * @return 16进制字符串
	 */
	public static String byte2hex(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				sb.append("0" + stmp);
			else
				sb.append(stmp);
		}
		return sb.toString().toUpperCase();
	}

	public static byte[] hex2bytes(String hexs) {
		String stmp = "";
		byte[] buf = new byte[hexs.length() / 2];
		for (int n = 0; n < hexs.length() / 2; n++) {
			int beginIndex = n * 2;
			int endIndex = beginIndex + 2;
			stmp = hexs.substring(beginIndex, endIndex);
			byte b = Integer.decode("0x" + stmp).byteValue();
			buf[n] = b;
		}
		return buf;
	}

	public static String getUtf8(String s1) {
		if (s1 == null)
			return s1;
		try {
			byte[] bb = s1.getBytes("iso8859-1");
			String s2 = new String(bb, "utf-8");
			return s2;
		} catch (UnsupportedEncodingException e) {
			return e.getMessage();
		}
	}

	public static String getGbk(String s1) {
		if (s1 == null)
			return s1;
		try {
			byte[] bb = s1.getBytes("iso8859-1");
			String s2 = new String(bb, "gbk");
			return s2;
		} catch (UnsupportedEncodingException e) {
			return e.getMessage();
		}
	}

	/**
	 * 获取被前后标记包围的内容，例如{12121}
	 * 
	 * @param s1       原始内容
	 * @param tagStart 开始字符串
	 * @param tagEnd   结束字符串
	 * @return 参数数组
	 */
	public static ArrayList<String> getParameters(String s1, String tagStart, String tagEnd) {
		if (s1 == null) {
			return null;
		}
		String t0 = regexTag(tagStart);
		String t1 = regexTag(tagEnd);
		Pattern pat = Pattern.compile(t0 + "\\w*" + t1, Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(s1);

		ArrayList<String> rst = new ArrayList<String>();
		while (mat.find()) {
			MatchResult mr = mat.toMatchResult();
			rst.add(mr.group().replace(tagStart, "").replace(tagEnd, ""));
		}
		return rst;
	}

	private static String regexTag(String tag) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tag.length(); i++) {
			String s = tag.substring(i, i + 1);
			sb.append("\\" + s);
		}
		return sb.toString();
	}

	public static MListStr getParameters(String sql, String tag) {
		if (sql == null)
			return null;
		String tmp = "{{{GDX1郭磊GdX2郭磊gDX3郭磊GDX1}}";
		String s1 = sql.replace(tag + tag, tmp);
		Pattern pat = Pattern.compile(tag + "[a-zA-Z0-9\\-\\._:]*\\b", Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(s1);

		MListStr rst = new MListStr();
		while (mat.find()) {
			MatchResult mr = mat.toMatchResult();
			rst.add(mr.group().replace(tag, ""));
		}
		return rst;
	}

	public static boolean testString(String sourceString, String regexString) {
		Pattern pat = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(sourceString);
		return mat.find();
	}

	public static String[] getSqlSplit(String sql, String tag) {
		Pattern pat = Pattern.compile("\\b" + tag + "\\b", Pattern.CASE_INSENSITIVE);

		return pat.split(sql, 2);
	}

	/**
	 * 将纯文本转换成input/textarea所需格式
	 * 
	 * @param text
	 * @return
	 */
	public static String textToInputValue(String text) {
		if (text == null) {
			return "";
		}
		String s1 = text.replace("&", "&amp;");
		s1 = s1.replace("\"", "&quot;");
		s1 = s1.replace("<", "&lt;");
		s1 = s1.replace(">", "&gt;");
		return s1;
	}

	/**
	 * 将纯文本转换为HTML
	 * 
	 * @param text
	 * @return
	 */
	public static String textToHtml(String text) {
		if (text == null) {
			return "";
		}
		String s2 = text.replace("\r\n", "\n").replace("\n", "<br>");
		String s1 = textToInputValue(s2);
		return s1;
	}

	/**
	 * 生成脚本可以使用的文字，替换回车，双引号和 ”\“符
	 * 
	 * @param text 原始文字
	 * @return 替换后的js文字
	 */
	public static String textToJscript(String text) {
		if (text == null) {
			return "";
		}
		String s1 = text;
		s1 = s1.replace("\\", "\\\\");
		s1 = s1.replace("\"", "\\\"");
		s1 = s1.replace("\r", "\\r");
		s1 = s1.replace("\n", "\\n");
		s1 = s1.replace("\t", "\\t");
		// s1 = s1.replace("'", "\\'");
		return s1;
	}

	/**
	 * URLEncoder.encode 文字
	 * 
	 * @param text
	 * @return
	 */
	public static String textToUrl(String text) {
		if (text == null) {
			return "";
		}
		String s1;
		try {
			s1 = URLEncoder.encode(text, "utf-8");
		} catch (UnsupportedEncodingException e) {
			s1 = text;
		}
		return s1;
	}

	/**
	 * 生成编码的URL
	 * 
	 * @param s
	 * @return
	 */
	public static String encodeUrl(String s) {
		if (s == null) {
			return s;
		}

		int m = s.indexOf("?");
		String s0;
		if (m >= 0) {
			s0 = s.substring(0, m);
		} else {
			return s;
		}
		String s1 = s.substring(m + 1);
		String[] ss = s1.split("\\&");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ss.length; i++) {
			int m1 = ss[i].indexOf("=");
			String sb0 = ss[i], sb1 = "";
			if (m1 > 0) {
				sb0 = ss[i].substring(0, m1);
				sb1 = ss[i].substring(m1 + 1);
			}
			try {
				sb0 = URLEncoder.encode(sb0, "iso8859-1");
				if (sb1.indexOf("%") < 0) {
					sb1 = URLEncoder.encode(sb1, "iso8859-1");
				}
			} catch (UnsupportedEncodingException e) {
				System.out.println(e.getMessage());
			}
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(sb0);
			sb.append("=");
			sb.append(sb1);
		}

		return s0 + "?" + sb.toString();
	}

	/**
	 * 获取时间与时差计算后的结果
	 * 
	 * @param oriValue
	 * @param timeDiffMinutes
	 * @return
	 */
	public static Object getTimeDiffValue(Object oriValue, int timeDiffMinutes) {
		if (oriValue == null || timeDiffMinutes == 0) {
			return oriValue;
		}

		String cName = oriValue.getClass().getName().toUpperCase();
		Date t;
		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			t = Utils.getDate(oriValue.toString());
		} else {
			t = (Date) oriValue;
		}

		long time = t.getTime();
		time = time + timeDiffMinutes * 60 * 1000;

		if (cName.equals("JAVA.SQL.TIMESTAMP")) {
			Timestamp ta = new Timestamp(time);
			return ta;
		}

		Date tnew = new Date(time);
		return tnew;
	}

	public static String getDateString(Timestamp t1, String dateformatString) {
		Date d1 = new java.util.Date();
		d1.setTime(t1.getTime());
		return getDateString(d1, dateformatString);
	}

	/**
	 * 获取GMT时间表达式
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateGMTString(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("E, dd-MMM-yy HH:mm:ss z", Locale.UK);
		df.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));
		return df.format(date);

	}

	/**
	 * 获取GMT时间表达式
	 * 
	 * @param t1
	 * @return
	 */
	public static String getDateGMTString(Timestamp t1) {
		Date d1 = new java.util.Date();
		d1.setTime(t1.getTime());
		return getDateGMTString(t1);

	}

	/**
	 * 获取GMT时间表达式
	 * 
	 * @param calendar
	 * @return
	 */
	public static String getDateGMTString(Calendar calendar) {
		return getDateGMTString(calendar.getTime());

	}

	/**
	 * 返回时间的xml格式，yyyy-mm-ddTHH:MM:SS
	 * 
	 * @param t1 时间
	 * @return
	 */
	public static String getDateXmlString(Object t1) {
		if (t1 == null) {
			return null;
		}
		String className = t1.getClass().getName();
		long t2 = 0;
		if (className.equals("java.sql.Timestamp")) {
			java.sql.Timestamp tt = (java.sql.Timestamp) t1;
			t2 = tt.getTime();
		} else if (className.equals("java.sql.Date")) {
			java.sql.Date tt = (java.sql.Date) t1;
			t2 = tt.getTime();
		} else if (className.equals("java.util.Date")) {
			java.util.Date tt = (java.util.Date) t1;
			t2 = tt.getTime();
		}
		if (t2 == 0) {
			return null;
		}
		Date d1 = new java.util.Date();
		d1.setTime(t2);
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH) + 1;
		int d = c.get(Calendar.DAY_OF_MONTH);

		int hh = c.get(Calendar.HOUR_OF_DAY);
		int mm = c.get(Calendar.MINUTE);
		int ss = c.get(Calendar.SECOND);

		String sy = y < 10 ? "0" + y : y + "";
		String sm = m < 10 ? "0" + m : m + "";
		String sd = d < 10 ? "0" + d : d + "";

		String shh = hh < 10 ? "0" + hh : hh + "";
		String smm = mm < 10 ? "0" + mm : mm + "";
		String sss = ss < 10 ? "0" + ss : ss + "";

		return sy + "-" + sm + "-" + sd + "T" + shh + ":" + smm + ":" + sss;
	}

	/**
	 * 获取指定格式的 日期字符串<br>
	 * 格式例如 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param calendar
	 * @param dateformatString （yyyy-MM-dd HH:mm:ss）
	 * @return
	 */
	public static String getDateString(java.util.Calendar calendar, String dateformatString) {
		Date d1 = new java.util.Date();
		d1.setTime(calendar.getTimeInMillis());
		return getDateString(d1, dateformatString);
	}

	/**
	 * 获取指定格式的 日期字符串<br>
	 * 格式例如 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @param dateformatString （yyyy-MM-dd HH:mm:ss）
	 * @return
	 */
	public static String getDateString(Date date, String dateformatString) {
		SimpleDateFormat format = null;
		format = new SimpleDateFormat(dateformatString);
		return format.format(date);
	}

	/**
	 * 获取默认格式 日期 ，例如2011-04-02
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}

	/**
	 * 获取默认格式 日期和时间 ，例如 2011-04-02 11:29:31
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateTimeString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	/**
	 * 获取默认格式 时间 ，例如 11:29:31
	 * 
	 * @param date
	 * @return
	 */
	public static String getTimeString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		return format.format(date);
	}

	public static String getJavascript(String s1) {
		return "\r\n<script language=\"javascript\">\r\n" + s1 + "\r\n</script>\r\n";
	}

	public static String getAlertScript(String s1) {
		s1 = s1.replaceAll("\r", "\\\\r").replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n");
		s1 = "alert('" + s1 + "');";
		return getJavascript(s1);
	}

	public static String getGuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	/**
	 * 获取SELECT的OPTION列表
	 * 
	 * @param objectValues ArrayList中的对象
	 * @param v1           当前值
	 * @param valueMethod  value的方法名
	 * @param textMethod   text的方法名
	 * @return
	 */
	public static String getOptions(ArrayList<?> objectValues, String v1, String valueMethod, String textMethod) {
		StringBuilder tmp = new StringBuilder();
		// ObjectValue ov = new ObjectValue();
		// tmp.append("<option value=''>请选择</option>\r\n");
		// for (int i = 0; i < objectValues.size(); i++) {
		// Object obj = objectValues.get(i);
		// ov.setObject(obj);
		// String val = textToInputValue(ov.getValue(valueMethod));
		// String text = textToInputValue(ov.getValue(textMethod));
		// if (v1.toUpperCase().trim().equals(val.trim().toUpperCase())) {
		// tmp.append("<option value=\"" + val + "\" selected>" + text
		// + "</option>\r\n");
		// } else {
		// tmp.append("<option value=\"" + val + "\">" + text
		// + "</option>\r\n");
		// }
		// }
		return tmp.toString();
	}

	/**
	 * 获取SELECT的OPTION列表
	 * 
	 * @param valueList Option 的 value列表，用”，“分割
	 * @param textList  Option 的 text列表，用”，“分割
	 * @param v1        当前值
	 * @return Options
	 */
	public static String getOptions(String valueList, String textList, String v1) {
		StringBuilder tmp = new StringBuilder();
		String[] vl = valueList.split(",");
		String[] tl = textList.split(",");
		for (int i = 0; i < vl.length; i++) {
			String val = textToInputValue(vl[i]);
			String text = textToInputValue(tl[i]);
			if (v1.toUpperCase().trim().equals(val.trim().toUpperCase())) {
				tmp.append("<option value=\"" + val + "\" selected>" + text + "</option>\r\n");
			} else {
				tmp.append("<option value=\"" + val + "\">" + text + "</option>\r\n");
			}
		}
		return tmp.toString();
	}

	/**
	 * 获取和当天的日期天数
	 * 
	 * @param date1
	 * @return 天数
	 */
	public static int getDays(Date date1) {
		java.util.Date d2 = new java.util.Date();
		Calendar cal = Calendar.getInstance();
		int m = cal.get(Calendar.MONTH) + 1;
		int d = cal.get(Calendar.DAY_OF_MONTH);
		String s1 = cal.get(Calendar.YEAR) + "-" + (m > 9 ? m : "0" + m) + "-" + (d > 9 ? d : "0" + d);
		d2 = getDate(s1);
		return getDays(d2, date1);
	}

	public static int getDays(Date date1, Date date2) {
		long days = (date1.getTime() - date2.getTime()) / (24 * 60 * 60 * 1000);
		return (int) days;
	}

	public static Date getDate(String dateString) {
		if (dateString.length() == 8) {
			String y = dateString.substring(0, 4);
			String m = dateString.substring(4, 6);
			String d = dateString.substring(6, 8);
			dateString = y + "-" + m + "-" + d;
		}
		if (dateString.indexOf("/") > 0) {
			return getDate(dateString, "MM/dd/yyyy");
		} else {
			return getDate(dateString, "yyyy-MM-dd");
		}
	}

	public static Date getDate(String dateString, String dateFormat) {
		// yyyy-MM-dd HH:mm:ss;
		SimpleDateFormat sf = new SimpleDateFormat(dateFormat);
		try {
			return sf.parse(dateString);
		} catch (ParseException e) {
			return null;
		}

	}

	/**
	 * 获取sql的timestamp数据
	 * 
	 * @param s1         日期字符串
	 * @param lang       语言
	 * @param isUKFormat 当lang=enus时,是否为英式
	 * @return
	 */
	public static java.sql.Timestamp getTimestamp(String s1, String lang, boolean isUKFormat) {
		if (s1 == null || s1.trim().length() == 0) {
			return null;
		}
		// 8位表达式
		if (s1.trim().length() == 8) {
			s1 = s1.substring(0, 4) + "-" + s1.substring(4, 6) + "-" + s1.substring(6, 8);
		}

		if (s1.indexOf(":") < 1) {
			s1 = s1 + " 00:00:00.0000";
		} else {
			String[] s2 = s1.split(":");
			if (s2.length == 2) {
				s1 += ":00.0000";
			} else if (s2.length == 3 && s1.indexOf(".") == -1) {
				s1 += ".0000";
			}
		}
		if (s1.toUpperCase().indexOf("T") > 0) {// 2017-12-11T21:33
			// datetime-local (H5)
			s1 = s1.toUpperCase().replace("T", " ");
		}
		try {
			if (lang != null && lang.toUpperCase().equals("ENUS")) {
				if (s1.indexOf("-") < 0) { // 排除 yyyy-MM-dd的表达式
					// usa标准
					String[] s2 = s1.split(" ");
					String[] s3 = s2[0].split("\\/");
					if (s3.length != 3) {
						return null;
					}
					if (isUKFormat) {
						// 英式日期表达式
						s1 = s3[2] + "-" + s3[1] + "-" + s3[0] + " " + s2[1];
					} else {
						// 美式日期表达式
						s1 = s3[2] + "-" + s3[0] + "-" + s3[1] + " " + s2[1];
					}
				}
			}
			java.sql.Timestamp t1 = java.sql.Timestamp.valueOf(s1);
			return t1;
		} catch (Exception e) {
			System.out.println("CAST(Timestamp): " + s1);
			System.out.println(e.getMessage());
			return null;
		}
	}

	/**
	 * 计算显示的宽度
	 * 
	 * @param s1 字符串
	 * @return
	 */
	public static int getDisplayWidth(String s1) {
		if (s1 == null || s1.length() < 2)
			return 29;
		char[] chars = s1.toUpperCase().trim().toCharArray();
		int m = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] <= 'Z') {
				m += 11;
			} else {
				m += 14;
			}
		}
		return m;
	}

	/**
	 * 替换 StringBuilder 的字符串
	 * 
	 * @param source        StringBuilder源
	 * @param findString    需要替换的文字
	 * @param replaceString 替换的内容
	 */
	public static void replaceStringBuilder(StringBuilder source, String findString, String replaceString) {
		int m0 = source.indexOf(findString);
		if (m0 >= 0) {
			source.replace(m0, m0 + findString.length(), replaceString);
		}
	}

	/**
	 * 删除字符串中的内容
	 * 
	 * @param source 源
	 * @param find1  开始查找的字符串
	 * @param find2  后面查找的字符串
	 * @return
	 */
	public static String deleteStr(String source, String find1, String find2) {
		if (source == null)
			return null;
		int loc1 = source.indexOf(find1);
		if (loc1 >= 0) {
			int loc2 = source.indexOf(find2, loc1 + find1.length());
			if (loc2 > 0) {
				String tmp1 = source.substring(0, loc1);
				String tmp2 = source.substring(loc2 + find2.length() + 1);
				String rst = tmp1 + tmp2;
				return rst;
			}
		}
		return source;
	}

	/**
	 * 
	 * 基本功能：过滤所有以"<"开头以">"结尾的标签
	 * 
	 * @param str
	 * @return String
	 */
	public static String filterHtml(String str) {
		String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签
		Pattern pattern = Pattern.compile(regxpForHtml);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result1 = matcher.find();
		while (result1) {
			matcher.appendReplacement(sb, "");
			result1 = matcher.find();
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 
	 * 基本功能：替换指定的标签
	 * <p>
	 * 
	 * @param str
	 * @param beforeTag 要替换的标签
	 * @param tagAttrib 要替换的标签属性值
	 * @param startTag  新标签开始标记
	 * @param endTag    新标签结束标记
	 * @return String @如：替换img标签的src属性值为[img]属性值[/img]
	 */
	public static String replaceHtmlTag(String str, String beforeTag, String tagAttrib, String startTag,
			String endTag) {
		String regxpForTag = "<\\s*" + beforeTag + "\\s+([^>]*)\\s*>";
		String regxpForTagAttrib = tagAttrib + "=\"([^\"]+)\"";
		Pattern patternForTag = Pattern.compile(regxpForTag);
		Pattern patternForAttrib = Pattern.compile(regxpForTagAttrib);
		Matcher matcherForTag = patternForTag.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result = matcherForTag.find();
		while (result) {
			StringBuffer sbreplace = new StringBuffer();
			Matcher matcherForAttrib = patternForAttrib.matcher(matcherForTag.group(1));
			if (matcherForAttrib.find()) {
				matcherForAttrib.appendReplacement(sbreplace, startTag + matcherForAttrib.group(1) + endTag);
			}
			matcherForTag.appendReplacement(sb, sbreplace.toString());
			result = matcherForTag.find();
		}
		matcherForTag.appendTail(sb);
		return sb.toString();
	}
}
