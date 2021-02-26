package com.gdxsoft.easyweb.define.database.maps;

import java.util.HashMap;
/**
 * 数据库字段映射
 * @author Administrator
 *
 */
public class MapFieldType {

	private String _DatabaseName;
	private String _Name;
	private int _CreateNumber;
	private String _InsertPrefix;
	private String _InsertCovert;
	private int _Scale = 1;
	private HashMap<String, MapFieldType[]> _MapTo;
	private String _Fixed;
	private MapFieldType _Ewa;

	/**
	 * 转换到目标数据库对应的类型
	 * @param databaseType
	 * @return
	 */
	public MapFieldType convertTo(String databaseType) {
		String s = databaseType.trim().toUpperCase();
		if (this._MapTo.containsKey(s)) {
			MapFieldType[] maps = this._MapTo.get(s);
			if (maps.length > 0) {
				return maps[0];
			}
		}
		return null;

	}

	/**
	 * @return the _DatabaseName
	 */
	public String getDatabaseName() {
		return _DatabaseName;
	}

	/**
	 * @param databaseName
	 *            the _DatabaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		_DatabaseName = databaseName;
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
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
	 * @return the _CreateNumber
	 */
	public int getCreateNumber() {
		return _CreateNumber;
	}

	/**
	 * @param createNumber
	 *            the _CreateNumber to set
	 */
	public void setCreateNumber(int createNumber) {
		_CreateNumber = createNumber;
	}

	/**
	 * @return the _InsertPrefix
	 */
	public String getInsertPrefix() {
		return _InsertPrefix;
	}

	/**
	 * @param insertPrefix
	 *            the _InsertPrefix to set
	 */
	public void setInsertPrefix(String insertPrefix) {
		_InsertPrefix = insertPrefix;
	}

	/**
	 * @return the _InsertCovert
	 */
	public String getInsertCovert() {
		return _InsertCovert;
	}

	/**
	 * @param insertCovert
	 *            the _InsertCovert to set
	 */
	public void setInsertCovert(String insertCovert) {
		_InsertCovert = insertCovert;
	}

	/**
	 * @return the _Scale
	 */
	public int getScale() {
		return _Scale;
	}

	/**
	 * @param scale
	 *            the _Scale to set
	 */
	public void setScale(int scale) {
		_Scale = scale;
	}

	/**
	 * @return the _MapTo
	 */
	public HashMap<String, MapFieldType[]> getMapTo() {
		return _MapTo;
	}

	/**
	 * @param mapTo
	 *            the _MapTo to set
	 */
	public void setMapTo(HashMap<String, MapFieldType[]> mapTo) {
		_MapTo = mapTo;
	}

	/**
	 * @return the _Fixed
	 */
	public String getFixed() {
		return _Fixed;
	}

	/**
	 * @param fixed
	 *            the _Fixed to set
	 */
	public void setFixed(String fixed) {
		_Fixed = fixed;
	}

	/**
	 * @return the _Ewa
	 */
	public MapFieldType getEwa() {
		return _Ewa;
	}

	/**
	 * @param ewa
	 *            the _Ewa to set
	 */
	public void setEwa(MapFieldType ewa) {
		_Ewa = ewa;
	}

}
