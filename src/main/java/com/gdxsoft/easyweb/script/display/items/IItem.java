package com.gdxsoft.easyweb.script.display.items;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.gdxsoft.easyweb.script.InitValues;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;

public interface IItem {

	public final static String REP_AT_STR = "\1\2$$##GDX~##JZY$$\3\4";

	/** tag 的 IsLFEdit 0 不可编辑，1双击，2单击
	 * @return the _TagIsLfEdit
	 */
	public abstract int getTagIsLfEdit();
	
	/**
	 * 获取Item的JSON对象，用于APP
	 * @return
	 * @throws Exception
	 */
	public abstract JSONObject createItemJson() throws Exception;
	
	public abstract String createItemHtml() throws Exception;

	/**
	 * 获取格式化好的单元格值
	 * @return
	 * @throws Exception
	 */
	public String createFormatValue() throws Exception;
	
	/**
	 * 获取字段值，来自于数据库，类或初始值
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract String getValue() throws Exception;

	/**
	 * @return the __UserXItem
	 */
	public abstract UserXItem getUserXItem();

	/**
	 * @param _UserXItem
	 *            the __UserXItem to set
	 */
	public abstract void setUserXItem(UserXItem userXItem);

	/**
	 * @return the _InitValues
	 */
	public abstract InitValues getInitValues();

	/**
	 * @param initValues
	 *            the _InitValues to set
	 */
	public abstract void setInitValues(InitValues initValues);

	/**
	 * @return the _Response
	 */
	public abstract HttpServletResponse getResponse();

	/**
	 * @param response
	 *            the _Response to set
	 */
	public abstract void setResponse(HttpServletResponse response);

	/**
	 * @return the _HtmlClass
	 */
	public abstract HtmlClass getHtmlClass();

	/**
	 * @param htmlClass
	 *            the _HtmlClass to set
	 */
	public abstract void setHtmlClass(HtmlClass htmlClass);
}