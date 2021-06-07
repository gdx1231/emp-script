package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;

public class SampleAcl implements IAcl {

	private String _XmlName;
	private String _ItemName;
	private RequestValue _RequestValue;
	private String _GoToUrl; // 验证失败跳转页面

	public boolean canRun() {
		return true;
	}

	public SampleAcl() {

	}

	public SampleAcl(String xmlName, String itemName) {
		this._ItemName = itemName;
		this._XmlName = xmlName;
	}

	public String getDenyMessage() {
		return null;
	}

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @param xmlName the _XmlName to set
	 */
	public void setXmlName(String xmlName) {
		_XmlName = xmlName;
	}

	/**
	 * @return the _ItemName
	 */
	public String getItemName() {
		return _ItemName;
	}

	/**
	 * @param itemName the _ItemName to set
	 */
	public void setItemName(String itemName) {
		_ItemName = itemName;
	}

	public void setRequestValue(RequestValue requestValue) {
		this._RequestValue = requestValue;

	}

	public RequestValue getRequestValue() {
		return _RequestValue;
	}

	/**
	 * @return the _GoToUrl
	 */
	public String getGoToUrl() {
		return _GoToUrl;
	}

	/**
	 * @param goToUrl the _GoToUrl to set
	 */
	public void setGoToUrl(String goToUrl) {
		_GoToUrl = goToUrl;
	}

}
