package com.gdxsoft.easyweb.log;

import com.gdxsoft.easyweb.script.display.HtmlCreator;

/**
 * The log class base
 */
public class LogBase {

	private HtmlCreator _Creator;

	private Log _Log;

	public Log getLog() {
		return _Log;
	}

	public void setLog(Log log) {
		_Log = log;
	}

	/**
	 * Get the HtmlCreator (parent class)
	 * 
	 * @return HtmlCreator
	 */
	public HtmlCreator getCreator() {
		return _Creator;
	}

	/**
	 * Set the HtmlCreator (parent class)
	 * 
	 * @param creator the HtmlCreator
	 */
	public void setCreator(HtmlCreator creator) {
		this._Creator = creator;
	}

}
