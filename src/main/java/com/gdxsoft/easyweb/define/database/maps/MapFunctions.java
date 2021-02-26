package com.gdxsoft.easyweb.define.database.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;
/**
 * 数据库 function的转换
 * @author Administrator
 *
 */
public class MapFunctions {

	private static HashMap<String, HashMap<String, MapFunction>> DATABASETYPE_MAPS = new HashMap<String, HashMap<String, MapFunction>>();
	private HashMap<String, MapFunction> _FunctionMaps;

	/**
	 * 获取目标数据库function类型
	 * 
	 * @param databaseName
	 * @return
	 */
	public HashMap<String, MapFunction> getTypes(String databaseName) {
		String d1 = databaseName.toUpperCase().trim();
		if (DATABASETYPE_MAPS.containsKey(d1)) {
			return DATABASETYPE_MAPS.get(d1);
		}
		HashMap<String, MapFunction> map = new HashMap<String, MapFunction>();
		Iterator<String> it = this._FunctionMaps.keySet().iterator();

		while (it.hasNext()) {
			String key = it.next();
			MapFunction a = this._FunctionMaps.get(key);
			if (!a.getMapTo().containsKey(d1)) {
				continue;
			}
			ArrayList<MapFunction> b = a.getMapTo().get(d1);
			for (int i = 0; i < b.size(); i++) {
				map.put(b.get(i).getName(), b.get(i));
			}
		}
		DATABASETYPE_MAPS.put(d1, map);
		return map;
	}

	public void initMaps(Document doc)  {
		NodeList nl = UXml.retNodeList(doc, "Maps/Functions/Function");
		this._FunctionMaps = new HashMap<String, MapFunction>();
		for (int i = 0; i < nl.getLength(); i++) {
			initMap(nl.item(i));
		}
	}

	private void initMap(Node node) {
		MapFunction d = new MapFunction();
		String name = UXml.retNodeValue(node, "Name").toUpperCase().trim();
		d.setDatabaseName("EWA");
		d.setName(name);

		this._FunctionMaps.put(d.getName(), d);

		NodeList nl = UXml.retNodeList(node, "Database");
		for (int i = 0; i < nl.getLength(); i++) {
			this.initMapTo(nl.item(i), d);
		}
	}

	private void initMapTo(Node node, MapFunction parent) {
		String databaseName = UXml.retNodeValue(node, "Name").toUpperCase()
				.trim();
		MapFunction to = new MapFunction();
		to.setDatabaseName(databaseName);
		String func = UXml.retNodeValue(node, "Func");
		to.setName(func);

		to.setEwa(parent);

		if (!parent.getMapTo().containsKey(databaseName)) {
			parent.getMapTo().put(databaseName, new ArrayList<MapFunction>());
		}
		parent.getMapTo().get(databaseName).add(to);

	}
}
