package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

public class EwaConfigPage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5345660672030985385L;
	private XItemParameters _Parameters;

	/**
	 * @return the _Parameters
	 */
	public XItemParameters getParameters() {
		return _Parameters;
	}

	/**
	 * @param parameters
	 *            the _Parameters to set
	 */
	public void setParameters(XItemParameters parameters) {
		_Parameters = parameters;
	}

}
