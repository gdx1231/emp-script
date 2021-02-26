/**
 * 
 */
package com.gdxsoft.easyweb.define.group;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Fields;
import com.gdxsoft.easyweb.define.database.Table;

/**
 * @author Administrator
 * 
 */
public class GroupTableBase   {

	private Table _Table;
	private Fields _Fields;

	public GroupTableBase() {

	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#initTable(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void initTable(String schemaName, String tableName, String cnn) {
		_Table = new Table(tableName, schemaName, cnn);
		_Fields = _Table.getFields();
	}
	
	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTableFks()
	 */
	public String createTableFks(){
		return "";
	}
	
	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTableIndexs()
	 */
	public String createTableIndexs(){
		return "";
	}
	
	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTableRemarks()
	 */
	public String createTableRemarks(){
		return "";
	}
	
	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTableBody()
	 */
	public String createTableBody() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE " + this._Table.getName() + " (");
		for (int i = 0; i < this._Fields.size(); i++) {
			Field f = this._Fields.get(this._Fields.getFieldList().get(i));
			if (i > 0) {
				sb.append(",");
			}
			sb.append("\r\n\t" + f.getName() + " " + createFieldType(f));
			if (!f.isNull()) {
				sb.append(" NOT NULL");
			}
		}
		sb.append("\r\n);\r\n");
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#createTablePk()
	 */
	public String createTablePk() {
		return "";
	}

	public String createFieldType(Field f) {
		String t = f.getDatabaseType().toUpperCase();
		if (t.indexOf("CHAR") >= 0 || t.indexOf("BIN") >= 0) {
			return t + "(" + f.getColumnSize() + ")";
		} else {
			return t;
		}

	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#getTable()
	 */
	public Table getTable() {
		return _Table;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#setTable(com.gdxsoft.easyweb.define.database.Table)
	 */
	public void setTable(Table table) {
		_Table = table;
	}

	/* (non-Javadoc)
	 * @see com.gdxsoft.easyweb.define.group.IGroupTable#getFields()
	 */
	public Fields getFields() {
		return _Fields;
	}
}
