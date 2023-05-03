package com.gdxsoft.easyweb.script.display.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gdxsoft.easyweb.utils.UDigest;

public class ActionJSONSign {
	private JSONObject sign;
	private JSONArray names;
	private JSONObject parameters;
	private String algorithm;
	private String secert;
	private String code = "BASE64";
	private String header;
	private String concatChar = "\n";
	private boolean appendWithName = false;

	public String doSign() {
		String str;
		if (this.names == null) {
			str = this.signByParameters();
		} else {
			str = this.signByNames();
		}

		String signed;
		if (StringUtils.isBlank(secert)) {
			if ("HEX".equalsIgnoreCase(code)) {
				signed = UDigest.digestHex(str, this.algorithm);
			} else {
				signed = UDigest.digestBase64(str, this.algorithm);
			}
		} else {
			if ("HEX".equalsIgnoreCase(code)) {
				signed = UDigest.digestHex(str, this.algorithm, secert);
			} else {
				signed = UDigest.digestBase64(str, this.algorithm, secert);
			}
		}
		return signed;
	}

	private String signByNames() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.length(); i++) {
			String name = names.optString(i);
			if (appendWithName) {
				sb.append(name).append("=");
			}
			if (parameters.has(name)) {
				String value = parameters.optString(name);
				sb.append(value);
			}
			if (i < names.length() - 1) {
				sb.append(this.concatChar);
			}
		}
		return sb.toString();
	}

	/**
	 * 安装参数名称的顺序拼接
	 * 
	 * @param names
	 * @param parameters
	 * @return
	 */
	private String signByParameters() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = parameters.keys();
		ArrayList<String> names = new ArrayList<>();
		while (it.hasNext()) {
			names.add(it.next());
		}
		// 按照字段名的 ASCII 码从小到大排序（字典排序）
		Collections.sort(names, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return (o1.compareToIgnoreCase(o2) == 0 ? -o1.compareTo(o2) : o1.compareToIgnoreCase(o2));
			}
		});
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if (appendWithName) {
				sb.append(name).append("=");
			}
			if (parameters.has(name)) {
				String value = parameters.optString(name);
				sb.append(value);
			}
			sb.append(this.concatChar);
		}

		return sb.toString();
	}

	public void initCfg(JSONObject obj) {
		this.sign = obj;
		Iterator<String> it = obj.keys();
		while (it.hasNext()) {
			String key = it.next();
			if (key.equalsIgnoreCase("algorithm")) {
				String val = obj.optString(key);
				this.algorithm = val.trim();
			} else if (key.equalsIgnoreCase("secert")) {
				String val = obj.optString(key);
				this.secert = val.trim();
			} else if (key.equalsIgnoreCase("code")) {
				String val = obj.optString(key);
				this.code = val;
			} else if (key.equalsIgnoreCase("header")) {
				String val = obj.optString(key);
				this.header = val;
			} else if (key.equalsIgnoreCase("concatChar")) {
				String val = obj.optString(key);
				this.concatChar = val;
			} else if (key.equalsIgnoreCase("parameters")) {
				JSONObject h = obj.optJSONObject(key);
				this.parameters = h;
			} else if (key.equalsIgnoreCase("names")) {
				JSONArray arr = obj.optJSONArray(key);
				this.names = arr;
			} else if (key.equalsIgnoreCase("sign")) {
				this.sign = obj.optJSONObject(key);
			}
		}
	}

	/**
	 * @return the sign
	 */
	public JSONObject getSign() {
		return sign;
	}

	/**
	 * @return the names
	 */
	public JSONArray getNames() {
		return names;
	}

	/**
	 * @param names the names to set
	 */
	public void setNames(JSONArray names) {
		this.names = names;
	}

	/**
	 * @return the parameters
	 */
	public JSONObject getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(JSONObject parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * @return the secert
	 */
	public String getSecert() {
		return secert;
	}

	/**
	 * @param secert the secert to set
	 */
	public void setSecert(String secert) {
		this.secert = secert;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return the concatChar
	 */
	public String getConcatChar() {
		return concatChar;
	}

	/**
	 * @param concatChar the concatChar to set
	 */
	public void setConcatChar(String concatChar) {
		this.concatChar = concatChar;
	}

}
