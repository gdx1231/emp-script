package com.gdxsoft.easyweb.data;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;

public class JsonFieldNameCase {

	private int fieldNameCase;

	public JsonFieldNameCase(RequestValue rv) {
		fieldNameCase = 0;
		if (rv != null) {
			if (rv.s(FrameParameters.EWA_JSON_FIELD_CASE) != null) {
				if (rv.s(FrameParameters.EWA_JSON_FIELD_CASE).equalsIgnoreCase("upper")) {
					fieldNameCase = 1; // 大写字段
				} else if (rv.s(FrameParameters.EWA_JSON_FIELD_CASE).equalsIgnoreCase("lower")) {
					fieldNameCase = 2; // 小写字段
				}
			}
		}
	}

	public String createJsonObjectName(String oriName) {
		if (oriName == null || 0 == fieldNameCase) {
			return oriName;
		} else if (1 == fieldNameCase) { // upperCase
			return oriName.toUpperCase();
		} else { // lowerCase
			return oriName.toLowerCase();
		}
	}

}
