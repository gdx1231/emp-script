package com.gdxsoft.easyweb.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DTIndexes implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3214106735502324461L;
	private HashMap<String, DTIndex> _Indexes;

	public HashMap<String, DTIndex> getIndexes() {
		return _Indexes;
	}

	public DTIndexes() {
		this._Indexes = new HashMap<String, DTIndex>();
	}

	/**
	 * delete index row
	 * @param rowIndex
	 */
	public void deleteRow(int rowIndex) {
		Iterator<String> it = this._Indexes.keySet().iterator();
		while (it.hasNext()) {
			this._Indexes.get(it.next()).deleteRow(rowIndex);
		}
	}

	/**
	 * init index by columns
	 * @param cols
	 */
	public void setColumns(DTColumns cols) {
		for (int i = 0; i < cols.getCount(); i++) {
			DTColumn col = cols.getColumn(i);
			_Indexes.put(col.getName().toUpperCase(), new DTIndex(col));
		}
	}

	/**
	 * update index by cell
	 * @param cell
	 */
	public void update(DTCell cell) {
		String colName = cell.getColumn().getName().toUpperCase();
		DTIndex idx = this._Indexes.get(colName);
		if(idx !=null){
			idx.update(cell.getValue(), cell.getRow().getIndex());
		}
	}

	/**
	 * 查找数据行 name=321,sex=f,age=12
	 * 
	 * @param exp
	 */
	public Integer[] find(String exp) {
		String[] exps = exp.split(",");
		HashMap<Integer, Integer> rstall = new HashMap<Integer, Integer>();
		int m = 0;
		for (int i = 0; i < exps.length; i++) {
			String[] s1 = exps[i].split("\\=");
			if (s1.length != 2) {
				continue;
			}
			String colName = s1[0].trim().toUpperCase();
			String val = s1[1];

			DTIndex idx = this._Indexes.get(colName);
			if(idx==null){
				return null;
			}
			Integer[] rst = idx.find(val);
			if (rst == null) {
				return null;
			}
			m++;
			boolean isFind = false;
			for (int k = 0; k < rst.length; k++) {
				if (m == 1) {
					rstall.put(rst[k], 1);
					isFind = true;
				} else {
					if (rstall.containsKey(rst[k])) {
						rstall.put(rst[k], m);
						isFind = true;
					}
				}
			}
			if (!isFind) {
				return null;
			}
		}

		Iterator<Integer> it = rstall.keySet().iterator();
		ArrayList<Integer> al = new ArrayList<Integer>();
		while (it.hasNext()) {
			Integer key = it.next();
			Integer val = rstall.get(key);
			if (val.intValue() == m) {
				al.add(key);
			}
		}
		Integer[] a = new Integer[al.size()];
		al.toArray(a);
		return a;
	}
}
