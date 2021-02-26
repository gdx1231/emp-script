package com.gdxsoft.easyweb.utils.msnet;


public class MListStr extends MList {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1228728695405621425L;

	public String get(int index) {
		Object o = super.get(index);
		return o == null ? null : (String) o;
	}

	public String getLast() {
		Object o = super.getLast();
		return o == null ? null : (String) o;
	}

	
}
