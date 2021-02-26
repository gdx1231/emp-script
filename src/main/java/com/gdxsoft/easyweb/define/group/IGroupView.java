/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.datasource.DataConnection;

/**
 * @author Administrator
 * 
 */
public interface IGroupView {

	public abstract void initView(String viewName, String cnn);

	public abstract String createView();

	public abstract String getViewName();

	public abstract DataConnection getConn();

	public abstract void setConn(DataConnection conn);
}
