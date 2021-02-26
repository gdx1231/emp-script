package com.gdxsoft.easyweb.script;

/**
 * 值的属性
 * 
 * @author Administrator
 * 
 */
public enum PageValueTag {
	/**
	 * Web queryString
	 */
	QUERY_STRING,

	/**
	 * Cookie
	 */
	COOKIE,

	/**
	 * WEB Form para
	 */
	FORM,

	/**
	 * 加密的Cookie
	 */
	COOKIE_ENCYRPT,

	/**
	 * Session
	 */
	SESSION,

	/**
	 * 系统内部参数
	 */
	SYSTEM,

	/**
	 * 数据表
	 */
	DTTABLE,
	/**
	 * htmlcontrol 传递paras的参数放置的位置
	 */
	HTML_CONTROL_PARAS,
	/**
	 * 其他
	 */
	OTHER;

	public static PageValueTag[] getOrder() {
		PageValueTag[] tagOrders = new PageValueTag[PageValueTag.values().length];
		tagOrders[0] = PageValueTag.SESSION;
		tagOrders[1] = PageValueTag.SYSTEM;
		tagOrders[2] = PageValueTag.COOKIE_ENCYRPT;
		tagOrders[3] = PageValueTag.OTHER;
		tagOrders[4] = PageValueTag.HTML_CONTROL_PARAS;
		tagOrders[5] = PageValueTag.DTTABLE;
		tagOrders[6] = PageValueTag.COOKIE;
		tagOrders[7] = PageValueTag.FORM;
		tagOrders[8] = PageValueTag.QUERY_STRING;
		return tagOrders;
	}
}
