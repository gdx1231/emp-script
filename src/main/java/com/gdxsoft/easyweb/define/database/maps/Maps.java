package com.gdxsoft.easyweb.define.database.maps;

import org.w3c.dom.Document;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * 数据库字段映射主类
 * 
 * @author Administrator
 * 
 */
public class Maps {
	public static String MAP_XML = "/database/TypesMap.xml";

	private MapFieldTypes _MapFieldTypes;
	private MapFunctions _MapFunctions;
	private MapSqlTemplates _MapSqlTemplates;

	private static Maps _MAPS;

	public static Maps instance() throws Exception {
		if (_MAPS != null) {
			return _MAPS;
		} else {
			_MAPS = new Maps();
			_MAPS.init();
			return _MAPS;
		}
	}

	private void init() throws Exception {
		String xmlName = UPath.getConfigPath() + "/" + MAP_XML;
		Document doc = UXml.retDocument(xmlName);
		_MapFieldTypes = new MapFieldTypes();
		_MapFieldTypes.initMaps(doc);

		this._MapFunctions = new MapFunctions();
		this._MapFunctions.initMaps(doc);

		_MapSqlTemplates = new MapSqlTemplates();
		_MapSqlTemplates.initTemplates(doc);
	}

	/**
	 * 获取字段映射
	 * 
	 * @return the _MapFieldTypes
	 */
	public MapFieldTypes getMapFieldTypes() {
		return _MapFieldTypes;
	}

	/**
	 * 获取数据库Functions
	 * 
	 * @return the _MapFunctions
	 */
	public MapFunctions getMapFunctions() {
		return _MapFunctions;
	}

	/**
	 * 获取字段备注
	 * 
	 * @return the _MapFieldComments
	 */
	public MapSqlTemplates getMapSqlTemplates() {
		return _MapSqlTemplates;
	}

}
