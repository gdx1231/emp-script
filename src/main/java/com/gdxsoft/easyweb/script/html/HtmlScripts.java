package com.gdxsoft.easyweb.script.html;

import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class HtmlScripts {

	private MTable _Scripts = new MTable();

	public HtmlScript addScript(String id, String script, String language) {
		HtmlScript s = new HtmlScript();
		s.setId(id == null ? "" : id.trim());
		s.setScript(script == null ? "" : script.trim());
		s.setLanguage(language == null ? "javascript" : language.trim().toLowerCase());
		_Scripts.add(id, s);

		return s;
	}

	public HtmlScript addScript(String id, String script) {
		return addScript(id, script, "javascript");
	}

	/**
	 * 根据isShowScriptTag 显示脚本<br>
	 * isShowScriptTag=<b>true</b>，显示 script 标签
	 * 
	 * @param isShowScriptTag
	 *            是否显示script 标签
	 * @return
	 */
	public String getScripts(boolean isShowScriptTag) {
		MStr sb = new MStr();
		String last = "";
		for (int i = 0; i < _Scripts.getCount(); i++) {
			HtmlScript s = (HtmlScript) _Scripts.getByIndex(i);

			String s1;
			if (isShowScriptTag) {
				s1 = s.getHtmlPageScriptAll();
			} else {
				s1 = s.getHtmlPageScript();
			}
			// 将最后用户定义的脚本放到最后，让ewa先执行 郭磊2016-04-15
			if (s.getId() != null && s.getId().equals("JS_BOTTOM")) {
				last = s1;
			} else {
				sb.al(s1);
			}
		}
		if (last.length() > 0) {
			sb.al(last);
		}

		return sb.toString();
	}

	/**
	 * 获取所有脚本
	 * @return the _Scripts
	 */
	public MTable getScripts() {
		return _Scripts;
	}

	/**
	 * @param _Scripts the _Scripts to set
	 */
	public void setScripts(MTable scripts) {
		this._Scripts = scripts;
	}
}
