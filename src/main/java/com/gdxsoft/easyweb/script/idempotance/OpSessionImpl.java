package com.gdxsoft.easyweb.script.idempotance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;

public class OpSessionImpl extends OpBase implements IOp {
	private static Logger LOGGER = LoggerFactory.getLogger(OpSessionImpl.class);

	/**
	 * Idempotance值，保存到系统中
	 */
	public void save() {
		String idempotanceValue = this.generateValue();
		// 幂等性KEY
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		if (rv.getSession() != null) {
			// 幂等性值 放到session中
			rv.getSession().setAttribute(this.generateKey(), idempotanceValue);
		} else {
			LOGGER.warn("No session width Idempotance!");
		}
	}

	/**
	 * 获取系统保存的Idempotance值
	 * 
	 * @return
	 */
	public String getValue() {
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		String key = this.generateKey();
		String sessionValue = rv.getPageValues().getSessionValue(key);
		return sessionValue;
	}

	/**
	 * 检查是否匹配，只能执行一次，因为清除了session值
	 * 
	 * @return
	 */
	public boolean checkOnlyOnce() {
		String idempotenceValue = getIdempontance();
		String sessionValue = this.getValue();

		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		if (rv.getSession() != null) {
			rv.getSession().removeAttribute(this.generateKey());
		}

		return idempotenceValue != null && idempotenceValue.length() == 32 && idempotenceValue.equals(sessionValue);
	}

}
