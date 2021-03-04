package com.gdxsoft.easyweb.global;

import java.util.HashMap;

import com.gdxsoft.easyweb.script.template.SetBase;
import com.gdxsoft.easyweb.utils.Utils;

public class EwaInfos extends SetBase<EwaInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8060946459242771972L;
	private HashMap<String, String> _Js = new HashMap<String, String>();

	public String createJs(String lang) throws Exception {
		if (this._Js.get(lang) != null) {
			return this._Js.get(lang);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("var _EWA_INFO_MSG = {\r\n");
		int m = 0;
		for (int i = 0; i < super.count(); i++) {
			EwaInfo ee = super.getItem(i);
			if (ee.getName().trim().length() == 0) {
				continue;
			}
			if (m > 0) {
				sb.append(",\r\n");
			}
			m++;
			String memo = ee.getDescriptions().getDescription(lang).getMemo();
			memo = Utils.textToJscript(memo);
			sb.append("\"" + Utils.textToJscript(ee.getName()) + "\":\"" + memo
					+ "\"");
		}
		sb.append("};\r\n");
		this._Js.put(lang, sb.toString());
		return sb.toString();
	}
}
