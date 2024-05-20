/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.SystemXmlUtils;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * @author Administrator
 * 
 */
public class MapDatabaseTypes extends HashMap<String, MapTypes> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5038140753656610641L;
	private static String XML_NAME = "empscript_field_type.xml";
	public static MapDatabaseTypes MAP_DATABASE_TYPES;

	public MapDatabaseTypes() throws Exception {
		setToMap();
	}

	private void setToMap() throws Exception {
		Document doc = this.loadDoc();
		NodeList nl = UXml.retNodeList(doc, "root/database");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			MapTypes mts = new MapTypes(node);
			super.put(mts.getName(), mts);
		}

	}

	private Document loadDoc() throws Exception {

		Document doc = SystemXmlUtils.getSystemConfDocument(XML_NAME);
		return doc;
	}

	public MapTypes getDefaultMapTypes() {
		return super.get("__default");
	}

	public static MapDatabaseTypes instance() throws Exception {
		if (MapDatabaseTypes.MAP_DATABASE_TYPES == null) {
			MapDatabaseTypes mdt = new MapDatabaseTypes();
			MapDatabaseTypes.MAP_DATABASE_TYPES = mdt;
		}
		return MapDatabaseTypes.MAP_DATABASE_TYPES;
	}

	public static void main(String[] args) throws Exception {
		MapDatabaseTypes aa = MapDatabaseTypes.instance();
		java.util.Iterator<String> it = aa.keySet().iterator();
		while (it.hasNext()) {
			MapTypes mt = aa.get(it.next());

			System.out.println(mt.getName());
			java.util.Iterator<String> its = mt.keySet().iterator();
			while (its.hasNext()) {
				MapType mt1 = mt.get(its.next());
				System.out.println("\t" + mt1.getName());
			}
		}
	}
}
