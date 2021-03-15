package com.gdxsoft.easyweb.utils;

public interface IHandleJsonBinary {

	/**
	 * the UObjectValue.setDaoValue(JSONObject obj) return the object's binary
	 * 
	 * @param fieldName the field name
	 * @param src       the object
	 * @return the binary
	 */
	abstract byte[] getBinary(String fieldName, Object src);
}
