/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

/**
 * @author Administrator
 *
 */
public class IndexField {
	private String _Name;	//字段名称
	private boolean _Asc;
	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}
	/**
	 * @param name the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}
	/**
	 * @return the _Asc
	 */
	public boolean isAsc() {
		return _Asc;
	}
	/**
	 * @param asc the _Asc to set
	 */
	public void setAsc(boolean asc) {
		_Asc = asc;
	}
}
