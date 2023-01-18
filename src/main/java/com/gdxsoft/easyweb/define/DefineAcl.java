package com.gdxsoft.easyweb.define;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.acl.SampleAcl;
import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.utils.UUrl;
import com.gdxsoft.easyweb.utils.Utils;

public class DefineAcl extends SampleAcl implements IAcl {

	public PageValue getAdmin() {
		PageValue pv = super.getRequestValue().getPageValues().getValue(DefineParameters.EWA_ADMIN_ID);
		// EWA_ADMIN_ID 不在seesion中
		if (pv == null || pv.getPVTag() != PageValueTag.SESSION) {
			return null;
		}
		return pv;
	}

	public boolean canRun() {
		if (!ConfDefine.isAllowDefine()) {
			return false;
		}
		PageValue pv = this.getAdmin();
		if (pv == null) {
			UUrl url = new UUrl(super.getRequestValue().getRequest());
			String ref = url.getUrl();
			String goToUrl = super.getRequestValue().getContextPath()
					+ "/EWA_STYLE/cgi-bin/?XMLNAME=/ewa/m.xml&ITEMNAME=login&version=3&ref=" + Utils.textToUrl(ref);
			super.setGoToUrl(goToUrl);
			return false;
		} else {
			return true;
		}
	}

	public DefineAcl() {

	}

	public DefineAcl(String xmlName, String itemName) {
		super.setXmlName(xmlName);
		super.setItemName(itemName);
	}

}
