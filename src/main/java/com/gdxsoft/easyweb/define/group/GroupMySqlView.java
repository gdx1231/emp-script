/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.data.DTTable;

/**
 * @author Administrator
 * 
 */
public class GroupMySqlView extends GroupViewBase implements IGroupView {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.define.group.IGroupView#createView()
	 */
	public String createView() {
	
	
		String sql = "SELECT VIEW_DEFINITION FROM `information_schema`.`VIEWS` where "
			+ "table_name='"
			+ super.getViewName()
			+ "' and table_schema='"
			+ super.getConn().getSchemaName() + "'";
	super.getConn().executeQueryNoParameter(sql);
	DTTable tb = new DTTable();
	tb.initData(super.getConn().getLastResult().getResultSet());
	super.getConn().close();

	return tb.getRow(0).getCell(0).toString()+";\r\n";
	
	}

}
