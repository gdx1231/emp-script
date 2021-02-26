package com.gdxsoft.easyweb.global;

import java.util.HashMap;

import com.gdxsoft.easyweb.script.template.SetBase;
import com.gdxsoft.easyweb.utils.Utils;

public class EwaSettings extends SetBase<EwaSetting> {
	private HashMap<String, String> _Js = new HashMap<String, String>();

	public String createJs(String lang) throws Exception {
		if (this._Js.get(lang) != null) {
			return this._Js.get(lang);
		}
		StringBuilder sb = new StringBuilder();
		EwaSetting ee = super.getItem(lang);

		sb.append("var _EWA_G_SETTINGS={\r\n");
		sb.append("WEEKS:\"" + Utils.arrayJoin(ee.getWeeks(), ",") + "\",\r\n");
		sb.append("MONTHS:\"" + Utils.arrayJoin(ee.getMonths(), ",")
				+ "\",\r\n");
		sb.append("DATE:\"" + ee.getDate() + "\",\r\n");
		sb.append("TIME:\"" + ee.getTime() + "\",\r\n");
		sb.append("CURRENCY:\"" + ee.getCurrency() + "\",\r\n");
		sb.append("Today:\"" + ee.getToday() + "\",\r\n");
		sb.append("Hour:\"" + ee.getHour() + "\",\r\n");
		sb.append("Minute:\"" + ee.getMinute() + "\",\r\n");
		sb.append("Second:\"" + ee.getSecond() + "\"\r\n");
		
		sb.append("};\r\n");
		return sb.toString();
	}
}
