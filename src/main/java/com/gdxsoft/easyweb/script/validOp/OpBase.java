package com.gdxsoft.easyweb.script.validOp;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.utils.Utils;

public class OpBase {
	HtmlClass htmlClass;
	UserXItem uxi;
	String generatedValue;
	String key;

	public void init(HtmlClass htmlClass, UserXItem uxi) {
		this.htmlClass = htmlClass;
		this.uxi = uxi;
		this.generatedValue = null;
		this.key = null;
	}

	/**
	 * 创建Key
	 * 
	 * @return
	 */
	public String generateKey() {
		if (key == null) {
			key = "EWA_IDEMPOTENCE_" + (uxi == null ? "NOUXI" : uxi.getName().toUpperCase()) + "_"
					+ htmlClass.getSysParas().getFrameUnid();
		}
		return key;
	}

	/**
	 * 创建幂等性值
	 * 
	 * @return
	 */
	public String generateValue() {
		if (generatedValue == null) {
			generatedValue = Utils.getGuid().toLowerCase().replace("-", "");
		}
		return generatedValue;
	}

	/**
	 * 获取从前端传递的值
	 * 
	 * @return
	 */
	public String getUserValue() {
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		String idempotenceValue = rv.s(uxi.getName());
		return idempotenceValue;
	}

	/**
	 * @return the htmlClass
	 */
	public HtmlClass getHtmlClass() {
		return htmlClass;
	}

	/**
	 * @return the uxi
	 */
	public UserXItem getUxi() {
		return uxi;
	}

	public String getGeneratedValue() {
		return generatedValue;
	}

	public void setGeneratedValue(String generatedValue) {
		this.generatedValue = generatedValue;
	}

}
