package com.gdxsoft.easyweb.global;

import com.gdxsoft.easyweb.script.template.Descriptions;

public class EwaEvent {

	private String _Name;
	private String _FrontValue;
	private String _BackValue;
	private Descriptions _FrontDescriptions;
	private Descriptions _BackDescriptions;
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
	 * @return the _FrontValue
	 */
	public String getFrontValue() {
		return _FrontValue;
	}
	/**
	 * @param frontValue the _FrontValue to set
	 */
	public void setFrontValue(String frontValue) {
		_FrontValue = frontValue;
	}
	/**
	 * @return the _BackValue
	 */
	public String getBackValue() {
		return _BackValue;
	}
	/**
	 * @param backValue the _BackValue to set
	 */
	public void setBackValue(String backValue) {
		_BackValue = backValue;
	}
	/**
	 * @return the _FrontDescriptions
	 */
	public Descriptions getFrontDescriptions() {
		return _FrontDescriptions;
	}
	/**
	 * @param frontDescriptions the _FrontDescriptions to set
	 */
	public void setFrontDescriptions(Descriptions frontDescriptions) {
		_FrontDescriptions = frontDescriptions;
	}
	/**
	 * @return the _BackDescriptions
	 */
	public Descriptions getBackDescriptions() {
		return _BackDescriptions;
	}
	/**
	 * @param backDescriptions the _BackDescriptions to set
	 */
	public void setBackDescriptions(Descriptions backDescriptions) {
		_BackDescriptions = backDescriptions;
	}

	 
}
