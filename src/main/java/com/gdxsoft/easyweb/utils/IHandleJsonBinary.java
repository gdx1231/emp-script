package com.gdxsoft.easyweb.utils;

public interface IHandleJsonBinary {

	/**
	 * UObjectValue的 setDaoValue(JSONObject obj) 获取二进制的方法
	 * 
	 * @param fieldName
	 *            字段名称
	 * @param src
	 *            对象
	 * @return
	 */
	abstract byte[] getBinary(String fieldName, Object src);
}
