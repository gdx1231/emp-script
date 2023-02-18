package com.gdxsoft.easyweb.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.function.EwaFunctions;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.Utils;

public class EwaSqlFunctions {
	private static Logger LOGGER = LoggerFactory.getLogger(EwaSqlFunctions.class);
	private HashMap<String, EwaSqlFunction> tempData_;

	public HashMap<String, EwaSqlFunction> getTempData() {
		return tempData_;
	}

	public EwaSqlFunctions() {
		tempData_ = new HashMap<>();
	}

	/**
	 * Execute the functions and return result
	 * 
	 * @param rv the RequstValue
	 * @return the result
	 */
	public Map<String, Object> executeEwaFunctions(RequestValue rv) {
		return executeEwaFunctions(rv, null);
	}

	/**
	 * Execute the functions and return result
	 * 
	 * @param rv          the RequstValue
	 * @param debugFrames the DebugFrames
	 * @return the result
	 */
	public Map<String, Object> executeEwaFunctions(RequestValue rv, DebugFrames debugFrames) {

		if (this.getTempData() == null) {
			LOGGER.debug("No defined functions");
			if (debugFrames != null) {
				debugFrames.addDebug(this, "executeEwaFunctions", "No defined functions");
			}
			return null;
		}

		Map<String, Object> vals = new HashMap<>();
		this.getTempData().forEach((k, v) -> {
			Object[] methodParameters = new Object[v.getMethodParameters().size()];

			for (int i = 0; i < v.getMethodParameters().size(); i++) {
				String parameter = v.getMethodParameters().get(i);
				if ("rv".equalsIgnoreCase(parameter)) {
					methodParameters[i] = rv;
				} else if ("hc".equalsIgnoreCase(parameter)) { // HtmlCreator
					methodParameters[i] = rv.getHtmlCreator();
				} else {
					String result = rv.replaceParameters(parameter);
					methodParameters[i] = result;
				}
			}
			Object executeResult = null;
			if (v.isStaticCall()) {
				executeResult = EwaFunctions.executeStaticFunction(v.getFunctionName(), methodParameters);
			} else {
				Object[] valsConstruct = new Object[v.getConstructorParameters().size()];
				for (int i = 0; i < v.getConstructorParameters().size(); i++) {
					String parameter = v.getConstructorParameters().get(i);
					if ("rv".equalsIgnoreCase(parameter)) {
						methodParameters[i] = rv;
					} else {
						String result = rv.replaceParameters(parameter);
						valsConstruct[i] = result;
					}
				}
				executeResult = EwaFunctions.executeFunction(v.getFunctionName(), valsConstruct, methodParameters);
			}
			// 将执行结果放到 RV 中
			rv.addOrUpdateValue(v.getRvParamName(), executeResult);
			vals.put(v.getRvParamName(), executeResult);
			String msg = "Exec " + v.getFunctionName() + ", Set rv." + v.getRvParamName() + "=" + executeResult;
			LOGGER.debug(msg);
			if (debugFrames != null) {
				debugFrames.addDebug(this, "executeEwaFunctions", msg);
			}
		});

		return vals;
	}

	public String extractEwaSqlFunctions(String sql) {
		// select * from ss where id = ewa_func.password_hash(@s1)
		// and ewa_func.password_verify(@s0, @s2) ='true' and c=123
		for (int i = 0; i < 50; i++) {
			String sql1 = this.extractSqlFunction(sql);
			if (sql1.equals(sql)) {
				return sql;
			}
			sql = sql1;
		}
		return sql;
	}

	private String extractSqlFunction(String sql) {
		// ewa_func.funcname(arg0, arg1, arg2) 静态方法调用
		// ewa_func.funcname(construct0,construct2).(arg0, arg1, arg2) 动态类执行
		String sql1 = sql.toLowerCase();
		int loc = sql1.indexOf("ewa_func.");
		if (loc == -1) {
			return sql;
		}
		int locStart = loc + "ewa_func.".length();
		int locFirst = -1;
		for (int i = locStart; i < sql1.length(); i++) {
			char c = sql1.charAt(i);
			if (c == '(') { // 找到第一个(
				locFirst = i;
				break;
			}
		}

		if (locFirst == -1) {
			return sql;
		}

		EwaSqlFunction f = new EwaSqlFunction();
		// 方法名称
		String funcName = sql1.substring(locStart, locFirst).trim();
		f.setFunctionName(funcName);
		f.setStaticCall(true);

		int locFirstEnd = -1;
		for (int i = locFirst + 1; i < sql1.length(); i++) {
			char c = sql1.charAt(i);
			if (c == ')') { // 找到第一个)
				locFirstEnd = i;
				break;
			}
		}
		if (locFirstEnd == -1) {
			return sql;
		}

		String group0 = sql1.substring(locFirst + 1, locFirstEnd);
		f.setGroupMethodParameters(group0);

		int loFristDot = -1;
		for (int i = locFirstEnd + 1; i < sql1.length(); i++) {
			char c = sql1.charAt(i);
			if (!Character.isWhitespace(c) && c != '.') {
				break;
			}
			if (c == '.') { // 找到第一个)
				loFristDot = i;
				break;
			}
		}
		String sql2 = sql;
		// 静态方法，没有构造函数
		if (loFristDot == -1) {
			this.splitParameters(f.getGroupMethodParameters(), f.getMethodParameters());

			String functionExp = sql.substring(loc, locFirstEnd + 1);
			f.setFunctionExp(functionExp);
			String paramName = "_ewa_func_" + Utils.sha1(functionExp);
			f.setRvParamName(paramName);
			this.tempData_.put(f.getRvParamName(), f);

			String newExp = "@" + paramName + " /* " + functionExp.replace(".", "<dot>").replace("@", "<at>") + " */ ";
			sql2 = sql.replace(functionExp, newExp);
			return sql2;
		}

		// 查找动态构造方法
		int locSecondFrist = -1;
		for (int i = loFristDot + 1; i < sql1.length(); i++) {
			char c = sql1.charAt(i);
			if (!Character.isWhitespace(c) && c != '(') {
				break;
			}
			if (c == '(') { // 找到第一个)
				locSecondFrist = i;
				break;
			}
		}
		if (locSecondFrist == -1) {
			return sql2;
		}
		int locSecondEnd = -1;
		for (int i = locSecondFrist + 1; i < sql1.length(); i++) {
			char c = sql1.charAt(i);
			if (c == ')') { // 找到第一个)
				locSecondEnd = i;
				break;
			}
		}

		if (locSecondEnd == -1) {
			return sql2;
		}

		f.setStaticCall(false);
		// 有两个组，第一个是构造参数，第二个是方法参数
		String group1 = sql.substring(locSecondFrist + 1, locSecondEnd);
		f.setGroupConstructorParameters(group0);
		this.splitParameters(f.getGroupConstructorParameters(), f.getConstructorParameters());
		f.setGroupMethodParameters(group1);
		this.splitParameters(f.getGroupMethodParameters(), f.getMethodParameters());

		String functionExp = sql.substring(loc, locSecondEnd + 1);
		f.setFunctionExp(functionExp);
		String paramName = "_ewa_func_" + Utils.sha1(functionExp);
		f.setRvParamName(paramName);
		this.tempData_.put(f.getRvParamName(), f);

		sql2 = sql.replace(functionExp, "@" + paramName);
		return sql2;
	}

	private void splitParameters(String exp, List<String> lst) {
		if (exp.trim().length() == 0) {
			return;
		}
		String[] exps = exp.split(",");
		for (int i = 0; i < exps.length; i++) {
			lst.add(exps[i].trim());
		}
	}

}
