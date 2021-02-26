package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

public class SkinFrames extends SetBase<SkinFrame> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5205055931049806212L;
	private String _Name;

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
	
}
