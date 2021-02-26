package com.gdxsoft.easyweb.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.gdxsoft.easyweb.charts.openFlashChart2.AllBase;

public class UJSon {

	/**
	 * 创建一个 错误返回结果 json
	 * 
	 * @param err
	 * @return
	 */
	public static JSONObject rstFalse(String err) {
		JSONObject rst = new JSONObject();
		rstSetFalse(rst, err);
		return rst;
	}

	/**
	 * 创建一个 正确返回结果 json
	 * 
	 * @param msg
	 * @return
	 */
	public static JSONObject rstTrue(String msg) {
		JSONObject rst = new JSONObject();
		rstSetTrue(rst, msg);
		return rst;
	}

	/**
	 * 设置返回结果 true
	 * @param rst
	 * @param msg
	 */
	public static void rstSetTrue(JSONObject rst, String msg) {
		rst.put("RST", true);
		rst.put("MSG", msg);
	}

	/**
	 *  设置返回结果 false
	 * @param rst
	 * @param err
	 */
	public static void rstSetFalse(JSONObject rst, String err) {
		rst.put("RST", false);
		rst.put("ERR", err);
	}

	public static String createParameter(Map<String, Object> map, boolean showNull) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = map.keySet().iterator();
		int m = 0;
		while (it.hasNext()) {
			String key = it.next();
			String v = createParameter(key, map.get(key), showNull);
			if (v.length() > 0) {
				if (m > 0) {
					sb.append(", \n");
				} else {
					m = 1;
				}
				sb.append(v);
			}
		}
		return sb.toString();
	}

	public static String createListValue(List<Object> list, boolean showNull) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		int m = 0;
		for (int i = 0; i < list.size(); i++) {
			Object val = list.get(i);
			String v = createValue(val, showNull);
			if (v.length() > 0) {
				if (m > 0) {
					sb.append(", ");
				} else {
					m = 1;
				}
				sb.append(v);
			}
		}
		sb.append("] ");
		return sb.toString();
	}

	public static String createParameter(String name, Object value, boolean showNull) {
		if (!showNull && value == null) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("\"" + name + "\": " + createValue(value, showNull));
			return sb.toString();
		}
	}

	@SuppressWarnings("unchecked")
	public static String createValue(Object val, boolean showNull) {
		if (val == null) {
			return createStringValue(null);
		}
		// String name = val.getClass().getName();
		if (val instanceof Number) {
			String s = val.toString();
			if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
				while (s.endsWith("0")) {
					s = s.substring(0, s.length() - 1);
				}
				if (s.endsWith(".")) {
					s = s.substring(0, s.length() - 1);
				}
			}
			return s;
		} else if (val instanceof Boolean) {
			return ((Boolean) val) ? "true" : "false";
		} else if (val instanceof Map) {
			return createParameter((Map<String, Object>) val, showNull);
		} else if (val instanceof List) {
			return createListValue((List<Object>) val, showNull);
		} else if (val instanceof AllBase) {
			return "{\n" + ((AllBase) val).toJSON() + "\n}";
		} else {
			return createStringValue(val.toString());
		}
	}

	private static String createStringValue(String string) {

		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				if (b == '<') {
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('"');
		return sb.toString();
	}
}
