package com.gdxsoft.easyweb.define.database;

import java.util.ArrayList;

import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class DiffTable {

	private Table _FTable;
	private Table _TTable;

	private String _Name;
	private String _Type;

	private MTable _Map = new MTable();
	private MStr _MapDes = new MStr();
	private MStr _Sql=new MStr();
	public int getFLen() {
		return this._FTable == null ? -1 : this._FTable.getFields().size();
	}

	public int getTLen() {
		return this._TTable == null ? -1 : this._TTable.getFields().size();
	}

	public String getDes() {
		return this._MapDes.toString();
	}
	
	public String getSql(){
		return this._Sql.toString();
	}

	public boolean isExists() {
		String name = "isExists";
		if (this._Map.containsKey(name)) {
			return (Boolean) this._Map.get(name);
		}
		if (this._FTable == null || this._TTable == null) {
			if (this._FTable == null) {
				String sql=this._TTable.getSqlTable();
				_MapDes.al("源表：" + this._Name + "不存在");
				this._Sql.al(sql+";");
			} else {
				String sql=this._FTable.getSqlTable();
				_MapDes.al("目标表：" + this._Name + "不存在");
				this._Sql.al(sql+";");
			}
			
			this._Map.put(name, false);
			return false;
		} else {
			this._Map.put(name, true);
			return true;
		}
	}

	/**
	 * 字段一致
	 * 
	 * @return
	 */
	public boolean isSameFields() {
		String name = "isSameFields";
		if (this._Map.containsKey(name)) {
			return (Boolean) this._Map.get(name);
		}
		if (!isExists()) {
			return false;
		}

		boolean ok = true;
		String unExistsFields = "";
		MStr fields = new MStr();
		MStr sqlField=new MStr();
		
		//目标表中不存在的字段
		for (int i = 0; i < this._FTable.getFields().getFieldList().size(); i++) {
			String key = this._FTable.getFields().getFieldList().get(i);
			Field fromField = this._FTable.getFields().get(key);
			if (this._TTable.getFields().containsKey(key)) {
				Field toField = this._TTable.getFields().get(key);
				//字段是否一致
				String rst = isSameField(fromField, toField);
				if (rst.length() > 0) {
					ok = false;
					fields.a(rst + "; ");
					sqlField.al(fromField.getSqlChange()+";");
				}
			} else {
				unExistsFields += key + ", ";
				sqlField.al(fromField.getSqlAlter()+";");
				ok = false;
			}
		}
		//目标表有，源表中不存在的字段
		String unExistsFields1 = "";
		for (int i = 0; i < this._TTable.getFields().getFieldList().size(); i++) {
			
			String key = this._TTable.getFields().getFieldList().get(i);
			Field fromField = this._TTable.getFields().get(key);
			if (!this._FTable.getFields().containsKey(key)) {
				unExistsFields1 += key + ", ";
				sqlField.al(fromField.getSqlAlter()+";");
				ok = false;
			}
		}
		if (!ok) {
			if (unExistsFields.length() > 0) {
				this._MapDes.a(" 目标字段缺失：" + unExistsFields);
			}
			if (unExistsFields1.length() > 0) {
				this._MapDes.a(" 源字段缺失：" + unExistsFields1);
			}
			if (fields.length() > 0) {
				this._MapDes.a(" 字段不一致：" + fields.toString());
			}
			this._Sql.a(sqlField.toString());
		}
		this._Map.put(name, ok);
		return ok;
	}

	private String isSameField(Field from, Field to) {
		String ok = "";
		if (from.getColumnSize() != to.getColumnSize()) {
			ok =  "长度：" + from.getColumnSize() + ","
					+ to.getColumnSize();
		}
		if (!from.getDatabaseType().equals(to.getDatabaseType())) {
			ok += " 类型：" + from.getDatabaseType() + ", " + to.getDatabaseType();
		}
		if (ok.length() > 0) {
			ok = from.getName() + ok;
		}
		return ok;

	}

	/**
	 * 主键一致
	 * 
	 * @return
	 */
	public boolean isSamePk() {
		String name = "isSamePk";
		if (this._Map.containsKey(name)) {
			return (Boolean) this._Map.get(name);
		}
		if (this._FTable == null || this._TTable == null) {
			this._Map.put(name, false);
			return false;
		}

		TablePk pk1 = this._FTable.getPk();
		TablePk pk2 = this._TTable.getPk();

		if (pk1 == null && pk2 == null) {
			this._Map.put(name, true);
			return true;
		}
		if (pk2 == null || pk2 == null) {
			this._Map.put(name, false);
			return false;
		}

		ArrayList<Field> al1 = pk1.getPkFields();
		String s1 = "";
		for (int i = 0; i < al1.size(); i++) {
			s1 += al1.get(i).getName() + ",";
		}
		ArrayList<Field> al2 = pk2.getPkFields();
		String s2 = "";
		for (int i = 0; i < al2.size(); i++) {
			s2 += al1.get(i).getName() + ",";
		}
		boolean ok = s1.equals(s2);
		this._Map.put(name, ok);
		return ok;
	}

	/**
	 * 一致
	 * 
	 * @return
	 */
	public boolean isSame() {
		boolean ok = true;
		ok = ok && this.isSameFields();
		if (!ok) {
			return false;
		}

		ok = ok && this.isSamePk();
		if (!ok) {
			return false;
		}

		return true;
	}

	public String getCode() {
		return this._Type + "!!" + this._Name;
	}

	/**
	 * @return the _FTable
	 */
	public Table getFTable() {
		return _FTable;
	}

	/**
	 * @param table
	 *            the _FTable to set
	 */
	public void setFTable(Table table) {
		_FTable = table;
	}

	/**
	 * @return the _TTable
	 */
	public Table getTTable() {
		return _TTable;
	}

	/**
	 * @param table
	 *            the _TTable to set
	 */
	public void setTTable(Table table) {
		_TTable = table;
	}

	/**
	 * @return the _Name
	 */
	public String getDName() {
		return _Name;
	}

	/**
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _Type
	 */
	public String getDType() {
		return _Type;
	}

	/**
	 * @param type
	 *            the _Type to set
	 */
	public void setDType(String type) {
		_Type = type;
	}
}
