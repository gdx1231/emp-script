package com.gdxsoft.easyweb.global;

import java.util.HashMap;

import com.gdxsoft.easyweb.script.template.SetBase;
import com.gdxsoft.easyweb.utils.Utils;

public class EwaEvents extends SetBase<EwaEvent> {

	private HashMap<String, String> _Js = new HashMap<String, String>();

	public String createJs(String lang) throws Exception {
		if (this._Js.get(lang) != null) {
			return this._Js.get(lang);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("var _EWA_EVENT_MSG=new Object();\r\n");
		for (int i = 0; i < super.count(); i++) {
			EwaEvent ee = super.getItem(i);
			String memo = ee.getFrontDescriptions().getDescription(lang).getMemo();
			memo = Utils.textToJscript(memo);
			sb.append("_EWA_EVENT_MSG['" + ee.getName() + "']=\"" + memo
					+ "\";\r\n");
		}
		return sb.toString();
	}
}
