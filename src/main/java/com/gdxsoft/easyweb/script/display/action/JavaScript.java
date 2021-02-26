package com.gdxsoft.easyweb.script.display.action;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class JavaScript {
	static String regexQuery = "\\[sql[^\\]]*?\\](.|\\n|\\r)*?\\[\\/sql[^\\]]*?\\]";
	static String regexQueryStart = "\\[(?i)SQL[^\\]]*?\\]";
	static String regexQueryStart1 = "\\[SQL[^\\]]*?\\]";
	static String regexQueryEnd = "\\[\\/(?i)sql[^\\]]*?\\]";

	static String regexUpdate = "\\[upt[^\\]]*?\\](.|\\n|\\r)*?\\[\\/upt[^\\]]*?\\]";
	static String regexUpdateStart = "\\[(?i)upt[^\\]]*?\\]";
	static String regexUpdateStart1 = "\\[upt[^\\]]*?\\]";
	static String regexUpdateEnd = "\\[\\/(?i)upt[^\\]]*?\\]";

	static Pattern patQuery = Pattern.compile(regexQuery,
			Pattern.CASE_INSENSITIVE);
	static Pattern patQuery1 = Pattern.compile(regexQueryStart1,
			Pattern.CASE_INSENSITIVE);

	static Pattern patUpdate = Pattern.compile(regexUpdate,
			Pattern.CASE_INSENSITIVE);
	static Pattern patUpdate1 = Pattern.compile(regexUpdateStart1,
			Pattern.CASE_INSENSITIVE);

	public JavaScript() {

	}

	private String _Js;

	public String getJs() {
		return _Js;
	}

	public Object runJs(String jsss, RequestValue rv)
			throws NoSuchMethodException, ScriptException {
		String js = this.replaceQuery(jsss);
		js = this.replaceUpdate(js);

		js = this.fixSql(js);

		this._Js = js;
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		engine.eval(js);
		Invocable inv = (Invocable) engine;

		Object value = inv.invokeFunction("main", rv);

		return value;
	}

	private String fixSql(String sql) {
		String sql1 = sql;
		String[] sql2 = sql1.split("\n");
		MStr s = new MStr();

		for (int i = 0; i < sql2.length; i++) {
			String s2 = sql2[i];
			String s1=s2.trim();
			if (s1.startsWith("*")) {
				s2 = "";
			} else if (s1.startsWith("//")) {
				s2 = "";
			} else if (s1.startsWith("/*")) {
				s2 = "";
			} else if (s1.endsWith("*/")) {
				s2 = "";
			}
			s.al(s2);
		}

		return s.toString();
	}

	private String replaceQuery(String jsss) {
		Matcher mat = patQuery.matcher(jsss);
		while (mat.find()) {
			MatchResult mr = mat.toMatchResult();
			String s = mr.group();
			String sql = s.replaceAll(regexQueryStart, "").replaceAll(
					regexQueryEnd, "");
			Matcher mat1 = patQuery1.matcher(s);
			String tag = "";
			if (mat1.find()) {
				MatchResult mr1 = mat1.toMatchResult();
				tag = mr1.group();
				tag = tag.replace("[", "").replace("]", "").trim()
						.replace("\"", "");
			}
			sql = createSql(sql);
			String ss = "\n\n" + sql
					+ "\n\newa_code.query(____sqls.join('\\n'), \"" + tag
					+ "\");\n\n";
			jsss = jsss.replace(s, ss);
		}

		return jsss;
	}

	private String createSql(String sql) {
		String sql1 = sql.replace("\"", "\\\"");
		String[] sql2 = sql1.split("\n");
		MStr s = new MStr();

		s.al("var ____sqls=[];");
		for (int i = 0; i < sql2.length; i++) {
			String s1 = sql2[i].trim();
			if (s1.startsWith("*")) {
				s1 = s1.replaceFirst("\\*", "");
			}
			if (s1.startsWith("//")) {
				s1 = s1.replaceFirst("\\/\\/", "");
			}
			s.al("____sqls.push(\"" + s1 + "\");");
		}

		return s.toString();
	}

	private String replaceUpdate(String jsss) {
		Matcher mat = patUpdate.matcher(jsss);
		while (mat.find()) {
			MatchResult mr = mat.toMatchResult();
			String s = mr.group();
			String sql = s.replaceAll(regexUpdateStart, "").replaceAll(
					regexUpdateEnd, "");
			Matcher mat1 = patUpdate1.matcher(s);
			String tag = "";
			if (mat1.find()) {
				MatchResult mr1 = mat1.toMatchResult();
				tag = mr1.group();
				tag = tag.replace("[", "").replace("]", "").trim()
						.replace("\"", "");
			}
			sql = createSql(sql);
			String ss = "\n\n" + sql
					+ "\n\newa_code.update(____sqls.join('\\n'), \"" + tag
					+ "\");\n\n";
			jsss = jsss.replace(s, ss);
		}
		return jsss;
	}
}
