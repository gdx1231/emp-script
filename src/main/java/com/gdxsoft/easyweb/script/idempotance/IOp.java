package com.gdxsoft.easyweb.script.idempotance;

import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;

public interface IOp {
	/**
	 * Idempotance值，保存到系统中，例如session中
	 */
	void save();

	/**
	 * 检查是否匹配，只能执行一次，因为清除了系统保存的值
	 * 
	 * @return
	 */
	boolean checkOnlyOnce();

	/**
	 * 初始化
	 * 
	 * @param htmlClass
	 * @param uxi
	 */
	void init(HtmlClass htmlClass, UserXItem uxi);

	/**
	 * 获取Idempotance的key
	 * 
	 * @return
	 */
	String generateKey();

	/**
	 * @return the htmlClass
	 */
	HtmlClass getHtmlClass();

	/**
	 * @return the uxi
	 */
	UserXItem getUxi();

	/**
	 * 创建幂等性值
	 * 
	 * @return
	 */
	String generateValue();
}