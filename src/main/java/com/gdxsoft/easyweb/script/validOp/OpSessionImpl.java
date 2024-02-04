package com.gdxsoft.easyweb.script.validOp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;

public class OpSessionImpl extends OpBase implements IOp {
	private static Logger LOGGER = LoggerFactory.getLogger(OpSessionImpl.class);

	/**
	 * 保存到系统中
	 */
	public void save() {
		String idempotanceValue = this.generateValue();
		save(idempotanceValue);
	}

	/**
	 * 保存到系统中
	 * 
	 * @param value
	 */
	public void save(String value) {
		// 幂等性KEY
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		if (rv.getSession() != null) {
			// 幂等性值 放到session中
			rv.getSession().setAttribute(this.generateKey(), value);
		} else {
			LOGGER.warn("No session width Idempotance!");
		}
	}

	/**
	 * 获取系统保存的Idempotance值
	 * 
	 * @return
	 */
	public String getSysValue() {
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
		String idempotenceValue = this.getUserValue();
		String sessionValue = this.getSysValue();

		removeSysValue();

		return idempotenceValue != null && idempotenceValue.length() > 0
				&& idempotenceValue.equalsIgnoreCase(sessionValue);
	}

	public void removeSysValue() {
		RequestValue rv = htmlClass.getSysParas().getRequestValue();
		if (rv.getSession() != null) {
			rv.getSession().removeAttribute(this.generateKey());
		}
	}

}
