package com.gdxsoft.easyweb.define;

public class UpdateXml {

	private IUpdateXml ux;

	public UpdateXml(String xmlName) {
		ux = ConfigUtils.getUpdateXml(xmlName);

		if (ux == null) {
			ux = ConfigUtils.getUpdateXmlByPath(xmlName);
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
