package com.gdxsoft.easyweb.global;

import com.gdxsoft.easyweb.script.template.Descriptions;

public class EwaValid {

	private String _Name;
	private String _Regex;
	private Descriptions _Descriptions;
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
	 * @return the _Regex
	 */
	public String getRegex() {
		return _Regex;
	}
	/**
	 * @param regex the _Regex to set
	 */
	public void setRegex(String regex) {
		_Regex = regex;
	}
	/**
	 * @return the _Descriptions
	 */
	public Descriptions getDescriptions() {
		return _Descriptions;
	}
	/**
	 * @param descriptions the _Descriptions to set
	 */
	public void setDescriptions(Descriptions descriptions) {
		_Descriptions = descriptions;
	}
	
	
}
