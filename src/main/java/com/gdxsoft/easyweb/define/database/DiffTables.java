package com.gdxsoft.easyweb.define.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTTable;

public class DiffTables {
	private String _CfgFrom;
	private String _CfgTo;

	private Tables _FTables;
	private Tables _TTables;
	private DTTable _Data;

	public DiffTables(String cfgFrom, String cfgTo) {
		this._CfgFrom = cfgFrom;
		this._CfgTo = cfgTo;
		_Data = new DTTable();

		String fileds = "NAME,TYPE,F_FIELDS_LEN,T_FIELDS_LEN,IS_SAME,IS_PK,IS_FIELDS";
		String[] ff = fileds.split(",");
		for (int i = 0; i < ff.length; i++) {
			DTColumn col = new DTColumn();
			col.setName(ff[i]);
			col.setDescription(ff[i]);
			col.setTypeName("String");
			_Data.getColumns().addColumn(col);
		}
	}

	public HashMap<String, DiffTable> getMap() {
		_FTables = new Tables();
		_FTables.initTables(_CfgFrom);

		_TTables = new Tables();
		_TTables.initTables(_CfgTo);

		ArrayList<String> al = _FTables.getTableList();

		HashMap<String, DiffTable> map = new HashMap<String, DiffTable>();

		int inc = 0;
		// maped
		try {
			for (int i = 0; i < al.size(); i++) {
				String key = al.get(i);
				Table ft = this._FTables.get(key);
				// "NAME,TYPE,F_FIELDS_LEN,T_FIELDS_LEN,IS_SAME,IS_PK,IS_FIELDS";
				// r.getCell(0).setValue(ft.getName());
				//			
				// r.getCell(1).setValue(value)

				DiffTable dft = new DiffTable();
				dft.setFTable(ft);
				dft.setName(ft.getName());
				dft.setDType(ft.getTableType());
				if (_TTables.containsKey(key)) {
					Table tt = _TTables.get(key);
					dft.setTTable(tt);

					_TTables.remove(key);
				}
				if (!dft.isSame()) {
					map.put(dft.getCode(), dft);
					inc++;
					if (inc > 30) {
						break;
					}
				}

			}
			// un maped
			Iterator<String> it = _TTables.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				Table ft = _TTables.get(key);
				DiffTable dft = new DiffTable();
				dft.setTTable(ft);
				dft.setName(ft.getName());
				dft.setDType(ft.getTableType());
				map.put(dft.getCode(), dft);
				inc++;
				if (inc > 30) {
					break;
				}
			}
		} catch (Exception err) {
			System.err.println(err.getMessage());
		}
		return map;

	}
}
