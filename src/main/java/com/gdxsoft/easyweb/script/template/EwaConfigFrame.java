package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

public class EwaConfigFrame implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1473240220428951031L;
	private String _Name;
	private String _FrameClassName;
	private String _ActionClassName;

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
	 * @return the _FrameClassName
	 */
	public String getFrameClassName() {
		return _FrameClassName;
	}

	/**
	 * @param frameClassName
	 *            the _FrameClassName to set
	 */
	public void setFrameClassName(String frameClassName) {
		_FrameClassName = frameClassName;
	}

	/**
	 * @return the _ActionClassName
	 */
	public String getActionClassName() {
		return _ActionClassName;
	}

	/**
	 * @param actionClassName
	 *            the _ActionClassName to set
	 */
	public void setActionClassName(String actionClassName) {
		_ActionClassName = actionClassName;
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
