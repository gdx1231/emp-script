package com.gdxsoft.easyweb.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * !!!不建议再用!!!<br>
 * 合并静态文件对象（js/css）
 * 
 * @author admin
 *
 */
public class UCombine {

	/**
	 * 获取Js实例
	 * 
	 * @param context
	 * @return 实例化js
	 */
	public static UCombine instanceOfJs(String context) {
		UCombine c = new UCombine("JS");
		c._Context = context;
		if (c._Context.startsWith("//")) {
			c._Context = c._Context.replace("//", "/");
		}
		return c;
	}

	/**
	 * 获取Css实例
	 * 
	 * @param context
	 * @return 实例化的css
	 */
	public static UCombine instanceOfCss(String context) {
		UCombine c = new UCombine("CSS");
		c._Context = context;
		if (c._Context.startsWith("//")) {
			c._Context = c._Context.replace("//", "/");
		}
		return c;
	}

	private String _CombineCgi = "/EWA_STYLE/cgi-bin/_re_/index.jsp?method=combine&files=";

	/**
	 * 获取CGI路径
	 * 
	 * @return CGI路径
	 */
	public String getCombineCgi() {
		return _CombineCgi;
	}

	/**
	 * 设置CGI路径
	 * 
	 * @param _CombineCgi CGI路径
	 */
	public void setCombineCgi(String _CombineCgi) {
		this._CombineCgi = _CombineCgi;
	}

	private String _Context;
	private String _Method;
	private boolean _IsNew;
	private boolean _IsDebug;

	private ArrayList<String> _Al;

	private UCombine(String method) {
		this._Method = method;
		_Al = new ArrayList<String>();
	}

	/**
	 * 增加路径
	 * 
	 * @param path 路径
	 */
	public void add(String path) {
		_Al.add(path);
	}

	/**
	 * 转成表 js/css 的达式
	 * @return js/css 的达式
	 */
	public String toString() {
		if (this._Al.size() == 0) {
			return "";
		}

		StringBuilder sbCssJs = new StringBuilder();
		if (!this._IsDebug) {
			if (this._Method.equals("CSS")) {
				sbCssJs.append("<link type='text/css' rel=\"stylesheet\" href=\"" + this._Context + _CombineCgi);
				for (int i = 0; i < _Al.size(); i++) {
					try {
						if (i > 0) {
							sbCssJs.append(URLEncoder.encode(";", "utf-8"));
						}
						sbCssJs.append(URLEncoder.encode(_Al.get(i), "utf-8"));
					} catch (UnsupportedEncodingException e) {
						System.out.println(e.getMessage());
					}
				}
				if (this._IsNew) {
					sbCssJs.append("&new=1");
				}
				sbCssJs.append("\">");
			} else if (this._Method.equals("JS")) {
				sbCssJs.append("<script type='text/javascript' src=\"" + this._Context + _CombineCgi);
				for (int i = 0; i < _Al.size(); i++) {
					try {
						if (i > 0) {
							sbCssJs.append(URLEncoder.encode(";", "utf-8"));
						}
						sbCssJs.append(URLEncoder.encode(_Al.get(i), "utf-8"));
					} catch (UnsupportedEncodingException e) {
						System.out.println(e.getMessage());
					}
				}
				if (this._IsNew) {
					sbCssJs.append("&new=1");
				}
				sbCssJs.append("\"></script>");
			}
		} else {
			if (this._Method.equals("CSS")) {

				for (int i = 0; i < _Al.size(); i++) {
					sbCssJs.append("<link type='text/css' rel=\"stylesheet\" href=\"");
					sbCssJs.append(_Al.get(i));
					sbCssJs.append("\">\n");
				}
			} else if (this._Method.equals("JS")) {
				for (int i = 0; i < _Al.size(); i++) {
					sbCssJs.append("<script type='text/javascript' src=\"");
					sbCssJs.append(_Al.get(i));

					sbCssJs.append("\"></script>\n");
				}
			}
		}
		return sbCssJs.toString();
	}

	/**
	 * Context
	 * @return Context
	 */
	public String getContext() {
		return _Context;
	}

	/**
	 * Context
	 * @param _Context Context
	 */
	public void setContext(String _Context) {
		this._Context = _Context;
	}

	/**
	 *  Method
	 * @return Method
	 */
	public String getMethod() {
		return _Method;
	}

	/**
	 * Method
	 * @param _Method Method
	 */
	public void setMethod(String _Method) {
		this._Method = _Method;
	}

	/**
	 * isNew
	 * @return true/false
	 */
	public boolean isNew() {
		return _IsNew;
	}

	/**
	 * isNew
	 * @param _IsNew isNew
	 */
	public void setIsNew(boolean _IsNew) {
		this._IsNew = _IsNew;
	}

	/**
	 * isDebug 
	 * @return isDebug
	 */
	public boolean isDebug() {
		return _IsDebug;
	}

	/**
	 * isDebug
	 * @param _IsDebug isDebug
	 */
	public void setIsDebug(boolean _IsDebug) {
		this._IsDebug = _IsDebug;
	}

	/**
	 * Al
	 * @return Al
	 */
	public ArrayList<String> getAl() {
		return _Al;
	}
}
