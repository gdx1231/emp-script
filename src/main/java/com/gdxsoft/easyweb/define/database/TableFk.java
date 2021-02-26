package com.gdxsoft.easyweb.define.database;

import java.util.ArrayList;

public class TableFk {
	private String _TableName; //表名称
	private String _FkName;
	private ArrayList<Field> _FkFields = new ArrayList<Field>();
	private TablePk _Pk = new TablePk();
	private boolean _IsDelete;
	private boolean _IsUpdate;

	/**
	 * @return the _FkName
	 */
	public String getFkName() {
		return _FkName;
	}

	/**
	 * @param fkName
	 *            the _FkName to set
	 */
	public void setFkName(String fkName) {
		_FkName = fkName;
	}

	/**
	 * @return the _FkFields
	 */
	public ArrayList<Field> getFkFields() {
		return _FkFields;
	}

	/**
	 * @param fkFields
	 *            the _FkFields to set
	 */
	public void setFkFields(ArrayList<Field> fkFields) {
		_FkFields = fkFields;
	}

	/**
	 * @return the _Pk
	 */
	public TablePk getPk() {
		return _Pk;
	}

	/**
	 * @param pk the _Pk to set
	 */
	public void setPk(TablePk pk) {
		_Pk = pk;
	}

	/**
	 * @return the isDelete
	 */
	public boolean isDelete() {
		return _IsDelete;
	}

	/**
	 * @param isDelete the isDelete to set
	 */
	public void setDelete(boolean isDelete) {
		_IsDelete = isDelete;
	}

	/**
	 * @return the isUpdate
	 */
	public boolean isUpdate() {
		return _IsUpdate;
	}

	/**
	 * @param isUpdate the isUpdate to set
	 */
	public void setUpdate(boolean isUpdate) {
		_IsUpdate = isUpdate;
	}

	/**
	 * @return the _TableName
	 */
	public String getTableName() {
		return _TableName;
	}

	/**
	 * @param tableName the _TableName to set
	 */
	public void setTableName(String tableName) {
		_TableName = tableName;
	}
}
