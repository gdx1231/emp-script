package com.gdxsoft.easyweb.define;

import org.json.JSONObject;

import com.alibaba.druid.sql.SQLUtils;


public class CodeFormat {

	/***
	 * 格式化代码
	 * 
	 * @param code
	 * @param type
	 * @return
	 */
	public static String format(String code, String type) {
		if (type.equals("sql")) {
			// String u="http://tool.lu/sql/ajax.html";
			// String ref="http://tool.lu/sql/";
			// UNet n=new UNet();
			// n.setEncode("utf-8");
			// n.setLastUrl(ref);
			// HashMap<String,String> vals=new HashMap<String,String>();
			// vals.put("code",code);
			// vals.put("operate","beauty");
			// String s=n.doPost(u,vals);
			// return s;

			// {"status":true,"message":"","text":

			JSONObject obj = new JSONObject();

			// 利用Druid的格式化工具
			String s = SQLUtils.formatSQLServer(code);
			obj.put("status", true);
			obj.put("message", "druid");
			obj.put("text", s);

			return obj.toString();
		}
		return "{error:true}";
	}

}
