package com.gdxsoft.easyweb.script.project;

import com.gdxsoft.easyweb.script.template.Descriptions;
@Deprecated
public class PageInfo {

	private String _Name;
	private Descriptions _DescriptionSet;

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
	 * @return the _DescriptionSet
	 */
	public Descriptions getDescriptionSet() {
		return _DescriptionSet;
	}

	/**
	 * @param descriptionSet
	 *            the _DescriptionSet to set
	 */
	public void setDescriptionSet(Descriptions descriptionSet) {
		_DescriptionSet = descriptionSet;
	}

}
