package com.gdxsoft.easyweb.define;

import com.gdxsoft.easyweb.script.userConfig.JdbcConfig;

public class UpdateXml {

	private IUpdateXml ux;

	public UpdateXml(String xmlName) {
		if (JdbcConfig.isJdbcResources()) {
			ux = new UpdateXmlJdbcImpl(xmlName);
		} else {
			ux = new UpdateXmlImpl(xmlName);
		}
	}

	/**
	 * 批处理更新 配置文件参数
	 * 
	 * @param itemNames
	 * @param paraName
	 * @param paraValue
	 */
	public void batchUpdate(String itemNames, String paraName, String paraValue) {
		ux.batchUpdate(itemNames, paraName, paraValue);
	}
}
