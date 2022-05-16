package com.gdxsoft.easyweb.data;

import java.io.Serializable;

import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class DTColumns implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309901355147038047L;
	private MList _Columns;
	private MTable _ColumnNames;
	private DTTable _Table;

	public DTColumns() {
		this._ColumnNames = new MTable();
		this._Columns = new MList();
	}

	public void clearKeys() {
		for (int i = 0; i < this.getCount(); i++) {
			this.getColumn(i).setIsKey(false);
		}
	}

	/**
	 * 设置字段主键
	 * 
	 * @param keys
	 *            主键表达式
	 */
	public void setKeys(String[] keys) {
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			int index = this.getNameIndex(key);
			if (index >= 0) {
				this.getColumn(index).setIsKey(true);
			}
		}
	}

	public DTColumn getColumn(int index) {
		return (DTColumn) this._Columns.get(index);
	}

	public DTColumn getColumn(String name) throws Exception {
		String n = name.toUpperCase().trim();
		if (this._ColumnNames.containsKey(n)) {
			int index = ((Integer) this._ColumnNames.get(n)).intValue();
			return getColumn(index);
		} else {
			MStr sb = new MStr();
			String s1 = this._ColumnNames.join(";", ",");
			sb.append(s1);
			throw new Exception("字段“" + n + "”不在表中。(" + sb.toString() + ")");
		}
	}

	/**
	 * 获取可以使用的字段表明，如果已经存在，则字段加序号
	 * 
	 * @param name
	 *            原始名称
	 * @return 可用的字段名称
	 */
	public String getCanUsedName(String name) {
		String n = name.toUpperCase().trim();
		if (!this._ColumnNames.containsKey(n)) {
			return n;
		} else {
			int idx = 1;
			String nn = n + idx;
			while (this._ColumnNames.containsKey(nn)) {
				idx++;
				nn = n + idx;
			}
			return nn;
		}
	}

	public void addColumn(DTColumn col) {
		this._Columns.add(col);
		int index = this._Columns.size() - 1;
		String n = col.getName().toUpperCase().trim();
		this._ColumnNames.put(n, index);
		col.setIndex(index);

	}
	
	/**
	 * Refresh the columns name index, manual call by changed column name
	 */
	public void refreshNamesIndex() {
		this._ColumnNames = new MTable();
		for(int i=0;i<this._Columns.size();i++) {
			DTColumn col = this.getColumn(i);
			String n = col.getName().toUpperCase().trim();
			this._ColumnNames.put(n, i);
		}
	}

	public int getCount() {
		return this._Columns.size();
	}

	/**
	 * 检查字段名称是否存在
	 * 
	 * @param name
	 * @return
	 */
	public boolean testName(String name) {
		if (name == null) {
			return false;
		}
		String n = name.trim().toUpperCase();
		return this._ColumnNames.containsKey(n);
	}

	public int getNameIndex(String name) {
		if (name == null) {
			return -1;
		}
		String n = name.trim().toUpperCase();
		if (this._ColumnNames.containsKey(n)) {
			return (Integer) this._ColumnNames.get(n);
		} else {
			return -1;
		}
	}

	/**
	 * @return the _Table
	 */
	public DTTable getTable() {
		return _Table;
	}

	/**
	 * @param table
	 *            the _Table to set
	 */
	public void setTable(DTTable table) {
		_Table = table;
	}
}
