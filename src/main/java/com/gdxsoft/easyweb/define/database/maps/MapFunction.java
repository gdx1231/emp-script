package com.gdxsoft.easyweb.define.database.maps;

import java.util.ArrayList;
import java.util.HashMap;

public class MapFunction {
	private String _DatabaseName;
	private String _Name;
	private MapFunction _Ewa;
	private HashMap<String, ArrayList<MapFunction>> _MapTo = new HashMap<String, ArrayList<MapFunction>>();

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
	 * @return the _MapTo
	 */
	public HashMap<String, ArrayList<MapFunction>> getMapTo() {
		return _MapTo;
	}

	/**
	 * @return the _Ewa
	 */
	public MapFunction getEwa() {
		return _Ewa;
	}

	/**
	 * @param ewa the _Ewa to set
	 */
	public void setEwa(MapFunction ewa) {
		_Ewa = ewa;
	}
 
}
