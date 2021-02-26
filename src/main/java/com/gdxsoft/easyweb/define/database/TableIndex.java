package com.gdxsoft.easyweb.define.database;

import java.util.ArrayList;

public class TableIndex {
	private String _TableName; // 表名称
	private String _IndexName;
	private ArrayList<IndexField> _IndexFields = new ArrayList<IndexField>();
	private boolean _Unique;
	
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

	/**
	 * @return the _IndexName
	 */
	public String getIndexName() {
		return _IndexName;
	}

	/**
	 * @param indexName
	 *            the _IndexName to set
	 */
	public void setIndexName(String indexName) {
		_IndexName = indexName;
	}

	/**
	 * @return the _IndexFields
	 */
	public ArrayList<IndexField> getIndexFields() {
		return _IndexFields;
	}

	/**
	 * @param indexFields
	 *            the _IndexFields to set
	 */
	public void setIndexFields(ArrayList<IndexField> indexFields) {
		_IndexFields = indexFields;
	}

	/**
	 * @return the _Unique
	 */
	public boolean isUnique() {
		return _Unique;
	}

	/**
	 * @param unique the _Unique to set
	 */
	public void setUnique(boolean unique) {
		_Unique = unique;
	}

}
