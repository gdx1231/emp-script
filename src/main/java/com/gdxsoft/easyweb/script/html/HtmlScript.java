package com.gdxsoft.easyweb.script.html;

import com.gdxsoft.easyweb.utils.msnet.MStr;

public class HtmlScript {
	private String _Id;
	private String _Script;
	private String _Language;

	/**
	 * 获取页面的全部脚本表达式，包含<script>标记
	 * 
	 * @return
	 */
	public String getHtmlPageScriptAll() {
		String s1 = getHtmlPageScript();
		if (s1.trim().length() > 0) {
			MStr sb = new MStr();
			sb.append("<script type='text/");
			sb.append(this._Language);
			sb.append("' id=\"");
			sb.append(this._Id);
			sb.append("\" ><!--\r\n");
			sb.append(s1);
			sb.append("\r\n--></script>\r\n");
			return sb.toString();
		} else {
			return "";
		}
	}

	/**
	 * 获取页面的脚本表达式，不含script
	 * 
	 * @return
	 */
	public String getHtmlPageScript() {
		return this._Script;
	}

	/**
	 * @return the _Script
	 */
	public String getScript() {
		return _Script;
	}

	/**
	 * @param script
	 *            the _Script to set
	 */
	public void setScript(String script) {
		_Script = script;
	}

	/**
	 * @return the _Language
	 */
	public String getLanguage() {
		return _Language;
	}

	/**
	 * @param language
	 *            the _Language to set
	 */
	public void setLanguage(String language) {
		_Language = language;
	}

	/**
	 * @return the _Id
	 */
	public String getId() {
		return _Id;
	}

	/**
	 * @param id
	 *            the _Id to set
	 */
	public void setId(String id) {
		_Id = id;
	}
}
