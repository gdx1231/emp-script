package com.gdxsoft.easyweb.script.html;

import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 页面的DocType声明，根据是否为XHTML显示不同的Doctype声明
 * @author Administrator
 *
 */
public class HtmlDoctype {

	boolean _IsXhtml;

	/**
	 * 获取DocType
	 * @return
	 */
	public String getDoctype() {
		MStr sb = new MStr();
		if (this._IsXhtml) {
			sb
					.appendLine("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			sb.appendLine("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		} else {
			sb
					.appendLine("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			sb.appendLine("<html>");
		}

		return sb.toString();
	}

	/**
	 * @return the _IsXhtml
	 */
	public boolean isXhtml() {
		return _IsXhtml;
	}

	/**
	 * @param isXhtml the _IsXhtml to set
	 */
	public void setIsXhtml(boolean isXhtml) {
		_IsXhtml = isXhtml;
	}
}
