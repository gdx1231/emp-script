package com.gdxsoft.easyweb.datasource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.cache.SqlCached;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 创建JSON更新表达式<br>
 * 
 * json的字段名称 一律转换为小写<br>
 * 
 * 1、带字段名称的，用于更新数据<br>
 * EWA_JSON(FIELD_NAME,@NAME, @SEX, @AGE, @MOBILE) <br>
 * -> json_set(FIELD_NAME,'$.name', @NAME, '$.sex' , @SEX ... <br>
 * 
 * 2、不带自动名称的，用于新增数据<br>
 * EWA_JSON(@NAME, @SEX, @AGE, @MOBILE) <br>
 * -> json_object('name', @NAME, 'sex' , @SEX ... <br>
 * 
 * @author admin
 *
 */
public class CreateJsonData {

	private RequestValue rv_;
	private static String tag = "EWA_JSON";
	private static Logger LOOGER = LoggerFactory.getLogger(SqlCached.class);

	public CreateJsonData(RequestValue rv) {
		rv_ = rv;
	}

	public String replaceJsonData(String sql) throws Exception {
		try {
			for (int i = 0; i < 50; i++) {
				String sql1 = this.replaceJsonData1(sql);
				if (sql1.equals(sql)) {
					return sql;
				}
				sql = sql1;
			}
			return sql;
		} catch (Exception err) {
			LOOGER.error(err.getLocalizedMessage());
			throw err;
		}
	}

	private String replaceJsonData1(String sql) throws Exception {

		String sql1 = sql.toUpperCase();

		int loc = sql1.indexOf(tag);
		if (loc == -1) {
			return sql;
		}
		int locEnd = -1;
		for (int i = loc + tag.length(); i < sql1.length(); i++) {

			char c = sql1.charAt(i);
			if (c == ')') {
				locEnd = i;
				break;
			}

		}

		if (locEnd == -1) {
			return sql;
		}

		String exp = sql.substring(loc, locEnd + 1);
		LOOGER.debug("分解参数：" + exp);
		String dataExp = createJsonData(exp);
		LOOGER.debug("分解结果：" + dataExp);
		sql = sql.replace(exp, dataExp);
		return sql;
	}

	/**
	 * EWA_JSON(FIELD_NAME,@NAME, @SEX, @AGE, @MOBILE)
	 * 
	 * @param para
	 * @return
	 * @throws Exception
	 */
	private String createJsonData(String para) throws Exception {
		int firstLoc = para.indexOf("(");
		if (firstLoc <= 0) {
			throw new Exception("json表达式错误（" + para + "）");
		}
		// 获取不带括号的表达式，例如
		// FIELD_NAME,@NAME, @SEX, @AGE, @MOBILE
		String exp = para.substring(firstLoc + 1, para.length() - 1);

		String[] fields = exp.split(",");
		if (fields.length == 0) {
			throw new Exception("json表达式错误,没有字段（" + para + "）");
		}

		MStr str = new MStr();

		String field0 = fields[0].trim();
		int field_start_index = 0;
		if (field0.indexOf("@") < 0) {
			field_start_index = 1;
			str.a("\n json_set(" + field0+", ");
		} else {
			str.a("\n json_object(");
		}

		for (int i = field_start_index; i < fields.length; i++) {
			String field = fields[i].trim();
			String field_name = field.toLowerCase().replace("@", "");
			if (i > field_start_index) {
				str.a(", ");
			}
			if (field_start_index == 0) {
				str.a("'" + field_name.replace("'", "''") + "', " + field);
			} else {
				str.a("'$." + field_name.replace("'", "''") + "', " + field);
			}

		}
		str.al(")");

		return str.toString();
	}

	public static void main(String[] args) throws Exception {
		RequestValue rv = null;
		CreateJsonData p = new CreateJsonData(rv);
		String sql = "INSERT INTO AA(ID,JSON) VALUES(@ID, EWA_JSON\n(@A1, A2, @A3, @AA_DEF), EWA_JSON(FF, \n @FA,@FB,  @FC \n))";
		String rst = p.replaceJsonData(sql);

		System.out.println(rst);

	}

	/**
	 * @return the rv_
	 */
	public RequestValue getRv() {
		return rv_;
	}

}
