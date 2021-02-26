package com.gdxsoft.easyweb.utils.msnet;

public class MTableStr extends MTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1433128716224188581L;

	public String get(String key) {
		if (super.containsKey(key)) {
			return super.get(key).toString();
		} else {
			return null;
		}
	}

	public String getByIndex(int index) {
		Object rst = super.getByIndex(index);
		if (rst == null)
			return null;
		return rst.toString();
	}
}
