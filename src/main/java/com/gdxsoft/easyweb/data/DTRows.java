package com.gdxsoft.easyweb.data;


import java.io.Serializable;

import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class DTRows implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2364092117617663782L;
	private MList _Rows;
	private DTTable _Table;
	private DTRow _CurRow;
	private int _Index = -1;
	private MTable _NameIndexes;
	private MTable _KeysIndexes;

	public DTRows() {
		this._Rows = new MList();
		_NameIndexes = new MTable();// HashMap<String, Integer>();
		_KeysIndexes = new MTable();
	}

	public DTRow getRow(int index) {
		this._CurRow = (DTRow) this._Rows.get(index);
		this._Index = index;
		this._CurRow.setIndex(index);
		return this._CurRow;
	}

	public DTRow getRow(String name) {
		if (name == null) {
			return null;
		}
		String n = name.trim().toUpperCase();
		if (!this._NameIndexes.containsKey(n)) {
			return null;
		}
		Integer index = (Integer) this._NameIndexes.get(n);

		this._CurRow = (DTRow) this._Rows.get(index);
		this._Index = index;
		this._CurRow.setIndex(index);
		return this._CurRow;
	}

	public void deleteRow(int index) {
		DTRow row = (DTRow) this._Rows.get(index);
		if (row.getNodeRow() != null) {
			row.getNodeRow().getParentNode().removeChild(row.getNodeRow());
		}

		this._Rows.removeAt(index);
		this._Index = -1;
		this._CurRow = null;

		// 删除索引的记录行
		if (this._Table.isBuildIndex() && this._Table.getIndexes() != null) {
			this._Table.getIndexes().deleteRow(index);
		}

	}

	public void addRow(DTRow row) {
		row.setTable(this._Table);
		this._Rows.add(row);
		row.setIndex(this._Rows.size() - 1);
		if (row.getName() != null) {
			String n = row.getName().trim().toUpperCase();
			if (!this._NameIndexes.containsKey(n)) {
				this._NameIndexes.add(n, row.getIndex());
			}
		}
		if (row.getKeysExp() != 0) {
			Integer exp = row.getKeysExp();
			if (!this._KeysIndexes.containsKey(exp)) {
				this._KeysIndexes.add(exp, row.getIndex());
			}
		}
	}

	/**
	 * 根据keys的hash获取行
	 * @param keysHashCode
	 * @return
	 */
	public DTRow getRowByKeys(int keysHashCode) {
		if (this._KeysIndexes.containsKey(keysHashCode)) {
			Integer index = (Integer) this._KeysIndexes.get(keysHashCode);
			return this.getRow(index);
		} else {
			return null;
		}
	}

	public int getCount() {
		return this._Rows.size();
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
	protected void setTable(DTTable table) {
		_Table = table;
	}

	/**
	 * @return the _CurRow
	 */
	public DTRow getCurRow() {
		return _CurRow;
	}

	/**
	 * @param curRow
	 *            the _CurRow to set
	 */
	public void setCurRow(DTRow curRow) {
		_CurRow = curRow;
	}

	/**
	 * @return the _Index
	 */
	public int getIndex() {
		return _Index;
	}

	/**
	 * @return the _NameIndexes
	 */
	public MTable getNameIndexes() {
		return _NameIndexes;
	}

	/**
	 * @return the _KeysIndexes
	 */
	MTable getKeysIndexes() {
		return _KeysIndexes;
	}

	 
}
