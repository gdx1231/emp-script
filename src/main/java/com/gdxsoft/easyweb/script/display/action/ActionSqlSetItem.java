/**
 * 
 */
package com.gdxsoft.easyweb.script.display.action;

import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;

/**
 * @author Administrator
 *
 */
public class ActionSqlSetItem {

	private String[] _SqlArray;
	private String _TransType;
	private String _SqlType;
	private UserXItemValue _UserXItemValue;
	/**
	 * @return the _SqlArray
	 */
	public String[] getSqlArray() {
		return _SqlArray;
	}
	/**
	 * @param sqlArray the _SqlArray to set
	 */
	public void setSqlArray(String[] sqlArray) {
		_SqlArray = sqlArray;
	}
	/**
	 * @return the _TransType
	 */
	public String getTransType() {
		return _TransType;
	}
	/**
	 * @param transType the _TransType to set
	 */
	public void setTransType(String transType) {
		_TransType = transType;
	}
	/**
	 * @return the _SqlType
	 */
	public String getSqlType() {
		return _SqlType;
	}
	/**
	 * @param sqlType the _SqlType to set
	 */
	public void setSqlType(String sqlType) {
		_SqlType = sqlType;
	}
	/**
	 * @return the _UserXItemValue
	 */
	public UserXItemValue getUserXItemValue() {
		return _UserXItemValue;
	}
	/**
	 * @param userXItemValue the _UserXItemValue to set
	 */
	public void setUserXItemValue(UserXItemValue userXItemValue) {
		_UserXItemValue = userXItemValue;
	}
}
