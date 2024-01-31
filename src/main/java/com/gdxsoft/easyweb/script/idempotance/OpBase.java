package com.gdxsoft.easyweb.script.idempotance;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.utils.Utils;

public class OpBase {
	HtmlClass htmlClass;
	UserXItem uxi;
	String idempotenceValue;
	String key;
	
	public void init(HtmlClass htmlClass, UserXItem uxi) {
		this.htmlClass = htmlClass;
		this.uxi = uxi;
		this.idempotenceValue = null;
		this.key = null;
	}
	
	

	/**
	 * 创建Idempotance的键值
	 * 
	 * @return
	 */
	public String generateKey() {
		if (key == null) {
			key = "EWA_IDEMPOTENCE_" + uxi.getName().toUpperCase() + "_" + htmlClass.getSysParas().getFrameUnid();
		}
		return key;
	}

	/**
	 * 创建幂等性值
	 * 
	 * @return
	 */
	public String generateValue() {
		if (idempotenceValue == null) {
			idempotenceValue = Utils.getGuid().toLowerCase().replace("-", "");
		}
		return idempotenceValue;
	}

	/**
	 * 获取从前端传递的值
	 * @return
	 */
	public String getIdempontance() {
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

}
