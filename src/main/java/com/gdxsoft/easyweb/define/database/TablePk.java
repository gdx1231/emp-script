/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

import java.util.ArrayList;

/**
 * @author Administrator
 * 
 */
public class TablePk {
	private String _TableName; // 表名称
	private String _PkName;
	private ArrayList<Field> _PkFields = new ArrayList<Field>();

	/**
	 * @return the _PkName
	 */
	public String getPkName() {
		return _PkName;
	}

	/**
	 * @param pkName
	 *            the _PkName to set
	 */
	public void setPkName(String pkName) {
		_PkName = pkName;
	}

	/**
	 * @return the _PkFields
	 */
	public ArrayList<Field> getPkFields() {
		return _PkFields;
	}

	/**
	 * @param pkFields
	 *            the _PkFields to set
	 */
	public void setPkFields(ArrayList<Field> pkFields) {
		_PkFields = pkFields;
	}

	/**
	 * @return the _TableName
	 */
	public String getTableName() {
		return _TableName;
	}

	/**
	 * @param tableName
	 *            the _TableName to set
	 */
	public void setTableName(String tableName) {
		_TableName = tableName;
	}
}
