package com.gdxsoft.easyweb.define.database;

public class MapType {

	private String _Name;
	private String _Standard;
	private String _Length;
	private String _LExp;
	private int _Max;
	private String _Other;
	
	/**
	 * 是否默认字段
	 */
	private boolean _Default;
	
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
	 * @return the _Standard
	 */
	public String getStandard() {
		return _Standard;
	}

	/**
	 * @param standard the _Standard to set
	 */
	public void setStandard(String standard) {
		_Standard = standard;
	}

	/**
	 * @return the _Length
	 */
	public String getLength() {
		return _Length;
	}

	/**
	 * @param length the _Length to set
	 */
	public void setLength(String length) {
		_Length = length;
	}

	/**
	 * @return the _LExp
	 */
	public String getLExp() {
		return _LExp;
	}

	/**
	 * @param exp the _LExp to set
	 */
	public void setLExp(String exp) {
		_LExp = exp;
	}

	/**
	 * @return the _Default
	 */
	public boolean isDefault() {
		return _Default;
	}

	/**
	 * @param default1 the _Default to set
	 */
	public void setDefault(boolean default1) {
		_Default = default1;
	}

	/**
	 * @return the _Max
	 */
	public int getMax() {
		return _Max;
	}

	/**
	 * @param max the _Max to set
	 */
	public void setMax(int max) {
		_Max = max;
	}

	/**
	 * @return the _Other
	 */
	public String getOther() {
		return _Other;
	}

	/**
	 * @param other the _Other to set
	 */
	public void setOther(String other) {
		_Other = other;
	}

	 

}
