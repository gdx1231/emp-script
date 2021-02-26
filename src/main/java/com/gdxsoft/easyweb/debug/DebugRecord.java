package com.gdxsoft.easyweb.debug;

import java.util.Date;

public class DebugRecord {
	private String _XmlName;
	private String _ItemName;
	private String _Parameters;
	private String _RunSeq;
	private Date _Date = new Date();
	private boolean _isError = false;
	private String _All;
	private long _RunTime;
	public DebugRecord() {

	}

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @param xmlName
	 *            the _XmlName to set
	 */
	public void setXmlName(String xmlName) {
		_XmlName = xmlName;
	}

	/**
	 * @return the _ItemName
	 */
	public String getItemName() {
		return _ItemName;
	}

	/**
	 * @param itemName
	 *            the _ItemName to set
	 */
	public void setItemName(String itemName) {
		_ItemName = itemName;
	}

	/**
	 * @return the _Parameters
	 */
	public String getParameters() {
		return _Parameters;
	}

	/**
	 * @param parameters
	 *            the _Parameters to set
	 */
	public void setParameters(String parameters) {
		_Parameters = parameters;
	}

	/**
	 * @return the _RunSeq
	 */
	public String getRunSeq() {
		return _RunSeq;
	}

	/**
	 * @param runSeq
	 *            the _RunSeq to set
	 */
	public void setRunSeq(String runSeq) {
		_RunSeq = runSeq;
	}

	/**
	 * @return the _Date
	 */
	public Date getDate() {
		return _Date;
	}

	/**
	 * @param date
	 *            the _Date to set
	 */
	public void setDate(Date date) {
		_Date = date;
	}

	/**
	 * @return the _isError
	 */
	public boolean isError() {
		return _isError;
	}

	/**
	 * @param error
	 *            the _isError to set
	 */
	public void setIsError(boolean error) {
		_isError = error;
	}

	/**
	 * @return the _All
	 */
	public String getAll() {
		return _All;
	}

	/**
	 * @param all the _All to set
	 */
	public void setAll(String all) {
		_All = all;
	}

	/**
	 * @return the _RunTime
	 */
	public long getRunTime() {
		return _RunTime;
	}

	/**
	 * @param runTime the _RunTime to set
	 */
	public void setRunTime(long runTime) {
		_RunTime = runTime;
	}
}
