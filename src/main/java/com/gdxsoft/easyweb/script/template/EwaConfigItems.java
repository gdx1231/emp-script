package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

public class EwaConfigItems implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5465209442133424558L;
	private XItems _Items;
	private XItemParameters _Parameters;

	/**
	 * @return the _Items
	 */
	public XItems getItems() {
		return _Items;
	}

	/**
	 * @param items
	 *            the _Items to set
	 */
	public void setItems(XItems items) {
		_Items = items;
	}

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
