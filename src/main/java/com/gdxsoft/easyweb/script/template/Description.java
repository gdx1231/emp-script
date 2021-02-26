/**
 * 
 */
package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

/**
 * @author Administrator
 * 
 */
public class Description implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4141328872145185637L;
	private String _Lang; // 语言
	private String _Info; // 内容
	private String _Memo; // 备注
	private String _Js;// 脚本显示内容

	/**
	 * @return the _Lang
	 */
	public String getLang() {
		return _Lang;
	}

	/**
	 * @param lang
	 *            the _Lang to set
	 */
	public void setLang(String lang) {
		_Lang = lang;
	}

	/**
	 * @return the _Memo
	 */
	public String getMemo() {
		return _Memo;
	}

	/**
	 * @param memo
	 *            the _Memo to set
	 */
	public void setMemo(String memo) {
		_Memo = memo;
	}

	/**
	 * @return the _Js
	 */
	public String getJs() {
		return _Js;
	}

	/**
	 * @param js
	 *            the _Js to set
	 */
	public void setJs(String js) {
		_Js = js;
	}

	/**
	 * @return the _Info
	 */
	public String getInfo() {
		return _Info;
	}

	/**
	 * @param info
	 *            the _Info to set
	 */
	public void setInfo(String info) {
		_Info = info;
	}

}
