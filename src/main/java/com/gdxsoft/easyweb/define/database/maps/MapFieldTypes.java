package com.gdxsoft.easyweb.define.database.maps;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;

public class MapFieldTypes {

	private HashMap<String, MapFieldType> _FieldMaps;

	public MapFieldTypes() {
		this._FieldMaps = new HashMap<String, MapFieldType>();
	}

	private static HashMap<String, HashMap<String, MapFieldType>> DATABASETYPE_MAPS = new HashMap<String, HashMap<String, MapFieldType>>();

	public HashMap<String, MapFieldType> getTypes(String databaseName) {
		String d1 = databaseName.toUpperCase().trim();
		if (DATABASETYPE_MAPS.containsKey(d1)) {
			return DATABASETYPE_MAPS.get(d1);
		}
		HashMap<String, MapFieldType> map = new HashMap<String, MapFieldType>();
		Iterator<String> it = this._FieldMaps.keySet().iterator();

		while (it.hasNext()) {
			String key = it.next();
			MapFieldType a = this._FieldMaps.get(key);
			if (!a.getMapTo().containsKey(d1)) {
				continue;
			}
			MapFieldType[] b = a.getMapTo().get(d1);
			for (int i = 0; i < b.length; i++) {
				map.put(b[i].getName(), b[i]);
			}
		}
		DATABASETYPE_MAPS.put(d1, map);
		return map;
	}

	public void initMaps(Document doc) {
		NodeList nl = UXml.retNodeList(doc, "Maps/EwaMap/Map");
		for (int i = 0; i < nl.getLength(); i++) {
			initMap(nl.item(i));
		}
	}

	private void initMap(Node node) {
		MapFieldType d = new MapFieldType();
		String name = UXml.retNodeValue(node, "Name").toUpperCase().trim();
		String createNumber = UXml.retNodeValue(node, "CreateNumber");
		String insertPrefix = UXml.retNodeValue(node, "InsertPrefix");
		String insertCovert = UXml.retNodeValue(node, "InsertCovert");

		int cn = createNumber == null ? 1 : Integer.parseInt(createNumber);
		d.setCreateNumber(cn);
		d.setDatabaseName("EWA");
		d.setInsertPrefix(insertPrefix == null ? "" : insertPrefix);
		d.setInsertCovert(insertCovert == null ? "" : insertCovert);
		d.setName(name);

		this._FieldMaps.put(d.getName(), d);

		NodeList nl = UXml.retNodeList(node, "Database");
		for (int i = 0; i < nl.getLength(); i++) {
			this.initMapTo(nl.item(i), d);
		}
	}

	private void initMapTo(Node node, MapFieldType parent) {

		String databaseName = UXml.retNodeValue(node, "Name").toUpperCase()
				.trim();
		NodeList nl = UXml.retNodeList(node, "MapTo");
		MapFieldType[] maps = new MapFieldType[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++) {
			maps[i] = new MapFieldType();
			maps[i].setDatabaseName(databaseName);
			Node n = nl.item(i);
			String name = UXml.retNodeValue(n, "Name").toUpperCase().trim();
			String scale = UXml.retNodeValue(n, "Scale");
			String fixed = UXml.retNodeValue(n, "Fixed");
			String insertCovert = UXml.retNodeValue(n, "InsertCovert");

			int s = scale == null || scale.trim().length() == 0 ? 1 : Integer
					.parseInt(scale);
			s = s == 0 ? 1 : s;
			maps[i].setName(name);
			maps[i].setScale(s);
			maps[i].setFixed(fixed == null ? "" : fixed);
			maps[i].setInsertCovert(insertCovert);

			maps[i].setEwa(parent);
		}

		HashMap<String, MapFieldType[]> mapTo = parent.getMapTo();
		if (mapTo == null) {
			mapTo = new HashMap<String, MapFieldType[]>();
			parent.setMapTo(mapTo);
		}
		mapTo.put(databaseName, maps);
	}

	/**
	 * @return the _FieldMaps
	 */
	public HashMap<String, MapFieldType> getFieldMaps() {
		return _FieldMaps;
	}
}
