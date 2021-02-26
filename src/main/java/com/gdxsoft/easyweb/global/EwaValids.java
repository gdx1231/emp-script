package com.gdxsoft.easyweb.global;

import java.util.HashMap;

import com.gdxsoft.easyweb.script.template.SetBase;
import com.gdxsoft.easyweb.utils.Utils;

public class EwaValids extends SetBase<EwaValid> {
	private HashMap<String, String> _Js = new HashMap<String, String>();

	 
	
	public String createJs(String lang) throws Exception {
		if (this._Js.get(lang) != null) {
			return this._Js.get(lang);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("var _EWA_VALIDS = {\r\n");
		int m = 0;
		for (int i = 0; i < super.count(); i++) {
			EwaValid ee = super.getItem(i);

			if (ee.getName().trim().length() == 0) {
				continue;
			}
			String memo = ee.getDescriptions().getDescription(lang).getMemo();
			memo = Utils.textToJscript(memo);
			if (m > 0) {
				sb.append(", ");
			}
			m += 1;
			sb.append(ee.getName().trim().toUpperCase() + " : {MSG: \"" + memo
					+ "\", REGEX: " + ee.getRegex() + "}\r\n");
		}
		sb.append("};\r\n");
		this._Js.put(lang, sb.toString());
		return sb.toString();
	}
}
