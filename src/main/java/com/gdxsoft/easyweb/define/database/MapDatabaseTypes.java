/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

	public MapDatabaseTypes() throws ParserConfigurationException,
			SAXException, IOException {
		setToMap();
	}

	private void setToMap() throws ParserConfigurationException, SAXException,
			IOException {
		Document doc = this.loadDoc();
		NodeList nl = UXml.retNodeList(doc, "root/database");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			MapTypes mts = new MapTypes(node);
			super.put(mts.getName(), mts);
		}

	}

	private Document loadDoc() throws ParserConfigurationException,
			SAXException, IOException {
		UXml xu = new UXml();

		String path = xu.getClass().getClassLoader().getResource("").getPath();
		if (path == null) {
			path = xu.getClass().getClassLoader().getResource(".").getPath();
		}
		if (path == null) {
			path = xu.getClass().getClassLoader().getResource("/").getPath();
		}
		String file = path + "/" + XML_NAME;

		Document doc = UXml.retDocument(file);
		return doc;
	}

	public MapTypes getDefaultMapTypes() {
		return super.get("__default");
	}

	public static MapDatabaseTypes instance()
			throws ParserConfigurationException, SAXException, IOException {
		if (MapDatabaseTypes.MAP_DATABASE_TYPES == null) {
			MapDatabaseTypes mdt = new MapDatabaseTypes();
			MapDatabaseTypes.MAP_DATABASE_TYPES = mdt;
		}
		return MapDatabaseTypes.MAP_DATABASE_TYPES;
	}
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		MapDatabaseTypes aa=MapDatabaseTypes.instance();
		java.util.Iterator<String> it=aa.keySet().iterator();
		while(it.hasNext()){
			MapTypes mt=aa.get(it.next());
			
			System.out.println(mt.getName());
			java.util.Iterator<String> its=mt.keySet().iterator();
			while(its.hasNext()){
				MapType mt1=mt.get(its.next());
				System.out.println("\t"+mt1.getName());
			}
		}
	}
}
