package com.gdxsoft.easyweb.datasource;

import java.util.ArrayList;
import java.util.HashMap;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;

/**
 * 创建EWA_SPLIT的临时数据
 * @author admin
 *
 */
public class CreateSplitData {

	private RequestValue rv_;
	private HashMap<String, ArrayList<String>> tempData_;
	private static String tag = "EWA_SPLIT";

	public HashMap<String, ArrayList<String>> getTempData() {
		return tempData_;
	}

	public CreateSplitData(RequestValue rv) {
		rv_ = rv;
		tempData_ = new HashMap<String, ArrayList<String>>();
	}

	public String replaceSplitData(String sql) {

		for (int i = 0; i < 50; i++) {
			String sql1 = this.replaceSplitData1(sql);
			if (sql1.equals(sql)) {
				return sql;
			}
			sql = sql1;
		}
		return sql;
	}

	private String replaceSplitData1(String sql) {

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
		String dataExp = insertTmpData(exp);
		sql = sql.replace(exp, "(select idx,col from _EWA_SPT_DATA where tag='" + dataExp + "')");
		return sql;
	}

	private String insertTmpData(String para) {
		MListStr paras = Utils.getParameters(para, "@");
		if (paras.size() == 0) {
			return null;
		}
		String p1 = paras.get(0);
		String v1 = this.rv_.getString(p1);
		if (v1 == null) {
			return null;
		}
		
		int loc0 = para.indexOf(",");
		int loc1 = para.lastIndexOf(")");
		String splitStr = para.substring(loc0 + 1, loc1).trim().replace("'", "");
		// System.out.println(splitStr);
		boolean isAppendBlank = false;
		if (v1.endsWith(splitStr)) {
			v1 += " ";
			isAppendBlank = true;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < splitStr.length(); i++) {
			sb.append("\\");
			sb.append(splitStr.charAt(i));
		}
		splitStr = sb.toString();
		// System.out.println(splitStr);
		String[] vs = v1.split(splitStr);

		String tag = Utils.getGuid().hashCode() + "";

		if (this.rv_.s("JSESSIONID") != null) {
			tag = tag + "." + this.rv_.s("JSESSIONID").hashCode();
		}

		ArrayList<String> al = new ArrayList<String>();
		this.tempData_.put(tag, al);

		for (int i = 0; i < vs.length; i++) {
			String v2 = vs[i];

			if (isAppendBlank && i == vs.length - 1) {
				v2 = "";
			}
			al.add(v2);
			// System.out.println(v2);
		}

		return tag;
	}

	public static void main(String[] args) {
		RequestValue rv = new RequestValue();
		rv.addValue("s1", "1,2,3");
		rv.addValue("s2", "a,b,c");

		CreateSplitData p = new CreateSplitData(rv);
		String sql = "insert into a(v1,v2)" + " select a.col,b.col from (select * from ewa_split(@s1,',')) a "
				+ "inner join (select * from ewa_split ( @s2,',')) b on a.idx=b.idx";
		String rst = p.replaceSplitData(sql);

		System.out.println(rst);

	}

}
