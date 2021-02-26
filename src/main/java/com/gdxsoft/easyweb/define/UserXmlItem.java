package com.gdxsoft.easyweb.define;

public class UserXmlItem {

	/**
	 * 名称
	 * @return
	 */
	public String getName() {
		return _Name;
	}

	public void setName(String name) {
		_Name = name;
	}

	/**
	 * 描述
	 * @return
	 */
	public String getDes() {
		return _Des;
	}

	public void setDes(String des) {
		_Des = des;
	}

	/**
	 * 类型
	 * @return
	 */
	public String getType() {
		return _Type;
	}

	public void setType(String type) {
		_Type = type;
	}

	private String _Name;
	private String _Des;
	private String _Type;

}
