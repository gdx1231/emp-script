package com.gdxsoft.easyweb.define;

import org.json.JSONObject;

import com.alibaba.druid.sql.SQLUtils;
import com.gdxsoft.easyweb.utils.UJSon;

public class CodeFormat {

	/***
	 * 格式化SQL代码
	 * 
	 * @param code
	 * @param type
	 * @return
	 */
	public static String format(String code, String type) {
		JSONObject obj = UJSon.rstTrue();
		obj.put("code", code);
		obj.put("type", type);
		if (type.equals("sql")) {
			// 利用Druid的格式化工具
			try {
				String s = SQLUtils.formatSQLServer(code);
				obj.put("text", s);
			} catch (Exception err) {
				UJSon.rstSetFalse(obj, err.getMessage());
			}
			obj.put("status", true);
			obj.put("message", "druid");
		} else {
			UJSon.rstSetFalse(obj, "invalid type(" + type + ")");
		}
		return obj.toString();
	}

}
