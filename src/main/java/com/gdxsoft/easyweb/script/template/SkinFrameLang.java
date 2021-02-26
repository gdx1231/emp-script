package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

public class SkinFrameLang implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 121L;
	private String _Name;
	private Descriptions _Descriptions;

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _Descriptions
	 */
	public Descriptions getDescriptions() {
		return _Descriptions;
	}

	/**
	 * @param descriptions
	 *            the _Descriptions to set
	 */
	public void setDescriptions(Descriptions descriptions) {
		_Descriptions = descriptions;
	}

}
