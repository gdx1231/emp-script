package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;

public class SampleAcl2 implements IAcl2 {
	private String _XmlName;
	private String _ItemName;
	private RequestValue _RequestValue;
	private String _GoToUrl; // 验证失败跳转页面
	private HtmlCreator htmlCreator;

	public boolean canRun() {
		return true;
	}

	public SampleAcl2() {

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

	public boolean canRunAction(String actionName) {
		if (actionName.equalsIgnoreCase("OnPageLoad")) {
			return true;
		}
		return false;
	}

	public String getNotRunTitle() {
		return "NOT ALLOW RUN!";
	}

	/**
	 * @return the htmlCreator
	 */
	public HtmlCreator getHtmlCreator() {
		return htmlCreator;
	}

	/**
	 * @param htmlCreator the htmlCreator to set
	 */
	public void setHtmlCreator(HtmlCreator htmlCreator) {
		this.htmlCreator = htmlCreator;
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

}
