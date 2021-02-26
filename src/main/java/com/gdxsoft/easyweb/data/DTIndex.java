package com.gdxsoft.easyweb.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class DTIndex implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4566830082699686484L;
	private String _ColName;
	private int _ColIndex;
	private HashMap<Object, HashMap<Integer, Integer>> _Values;
	private HashMap<Integer, Object> _Values1;

	public DTIndex(DTColumn col) {
		this._ColIndex = col.getIndex();
		this._ColName = col.getName();

		this._Values = new HashMap<Object, HashMap<Integer, Integer>>();
		this._Values1 = new HashMap<Integer, Object>();

	}

	public void deleteRow(int rowIndex) {
		if (this._Values1.containsKey(rowIndex)) {
			Object o = this._Values1.get(rowIndex);
			if (this._Values.containsKey(o)) {
				HashMap<Integer, Integer> map = this._Values.get(o);
				if (map.containsKey(rowIndex)) {
					map.remove(rowIndex);
				}
			}
			this._Values1.remove(rowIndex);
		}
	}

	public DTIndex(String colName, int colIndex) {
		this._ColName = colName;
		this._ColIndex = colIndex;

		this._Values = new HashMap<Object, HashMap<Integer, Integer>>();
		this._Values1 = new HashMap<Integer, Object>();
	}

	public void update(Object val, int rowIndex) {
		if (this._Values1.containsKey(rowIndex)) {
			Object oldVal = this._Values1.get(rowIndex);
			if (oldVal.equals(val)) {
				return;
			}
			if (this._Values.containsKey(oldVal)) {
				HashMap<Integer, Integer> indexes = this._Values.get(oldVal);
				if (indexes.containsKey(rowIndex)) {
					indexes.remove(rowIndex);
				}
			}
		}else{
			this._Values1.put(rowIndex, val);
		}
		HashMap<Integer, Integer> indexes = null;
		if (this._Values.containsKey(val)) {
			indexes = this._Values.get(val);
		} else {
			indexes = new HashMap<Integer, Integer>();
			this._Values.put(val, indexes);
		}
		indexes.put(rowIndex, 0);
	}

	public Integer[] find(Object val) {
		if (this._Values.containsKey(val)) {
			HashMap<Integer, Integer> rst = this._Values.get(val);
			Iterator<Integer> it = rst.keySet().iterator();
			Integer[] rst1 = new Integer[rst.size()];
			int m=0;
			while (it.hasNext()) {
				rst1[m] = it.next();
				m++;
			}
			return rst1;
		} else {
			return null;
		}
	}

	/**
	 * @return the _ColName
	 */
	public String getColName() {
		return _ColName;
	}

	/**
	 * @param colName
	 *            the _ColName to set
	 */
	public void setColName(String colName) {
		_ColName = colName;
	}

	/**
	 * @return the _ColIndex
	 */
	public int getColIndex() {
		return _ColIndex;
	}

	/**
	 * @param colIndex
	 *            the _ColIndex to set
	 */
	public void setColIndex(int colIndex) {
		_ColIndex = colIndex;
	}

}
