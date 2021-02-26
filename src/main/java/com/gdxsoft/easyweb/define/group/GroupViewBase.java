/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.datasource.DataConnection;

/**
 * @author Administrator
 * 
 */
public class GroupViewBase {

	private DataConnection _Conn;
	private String _ViewName;

	public void initView(String viewName, String cnn) {
		this._ViewName = viewName;
		_Conn = new DataConnection();
		_Conn.setConfigName(cnn);
	}

	/**
	 * @return the _ViewName
	 */
	public String getViewName() {
		return _ViewName;
	}

	/**
	 * @param viewName
	 *            the _ViewName to set
	 */
	public void setViewName(String viewName) {
		_ViewName = viewName;
	}

	/**
	 * @return the _Conn
	 */
	public DataConnection getConn() {
		return _Conn;
	}

	/**
	 * @param conn
	 *            the _Conn to set
	 */
	public void setConn(DataConnection conn) {
		_Conn = conn;
	}

}
