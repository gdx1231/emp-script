package com.gdxsoft.easyweb.define;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UUrl;
import com.gdxsoft.easyweb.utils.Utils;

public class DefineAcl implements IAcl {

	private String _XmlName;
	private String _ItemName;
	private RequestValue _RequestValue;
	private String _GoToUrl; // 验证失败跳转页面

	public boolean canRun() {
		PageValue pv = this._RequestValue.getPageValues().getValue("EWA_ADMIN_ID");

		// EWA_ADMIN_ID 不在seesion中
		if (pv == null || pv.getPVTag() != PageValueTag.SESSION) {
			UUrl url = new UUrl(this._RequestValue.getRequest());
			String ref = url.getUrl();
			this._GoToUrl = this._RequestValue.getContextPath()
					+ "/EWA_STYLE/cgi-bin/?XMLNAME=/ewa/m.xml&ITEMNAME=login&version=3&ref=" + Utils.textToUrl(ref);
			return false;
		} else {
			return true;
		}
	}

	public DefineAcl() {

	}

	public DefineAcl(String xmlName, String itemName) {
		this._ItemName = itemName;
		this._XmlName = xmlName;
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
