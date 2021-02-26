package com.gdxsoft.easyweb.define.database.maps;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;

public class MapSqlTemplates {

	private HashMap<String, MapSqlTemplate> _SqlTemplates;

	/**
	 * 根据数据库类型（ORACLE,MSSQL,MYSQL...）获取目标数据库的备注类<br>
	 * 如果不存在则返回null
	 * 
	 * @param databaseType
	 *            数据库类型(ORACLE,MSSQL,MYSQL...)
	 * @return
	 */
	public MapSqlTemplate getSqlTemplate(String databaseType) {
		if (databaseType == null)
			return null;
		String n = databaseType.trim().toUpperCase();
		if (this._SqlTemplates.containsKey(n)) {
			return this._SqlTemplates.get(n);
		} else {
			return null;
		}
	}

	public void initTemplates(Document doc) {
		NodeList nl = UXml.retNodeList(doc, "Maps/SqlTemplates/SqlTemplate");
		this._SqlTemplates = new HashMap<String, MapSqlTemplate>();
		for (int i = 0; i < nl.getLength(); i++) {
			initTemplate(nl.item(i));
		}
	}

	private void initTemplate(Node node) {
		MapSqlTemplate d;
		String name = UXml.retNodeValue(node, "Name").toUpperCase().trim();
		if (this._SqlTemplates.containsKey(name)) {
			d = this._SqlTemplates.get(name);
		} else {
			d = new MapSqlTemplate();
			this._SqlTemplates.put(name, d);
			d.setDatabaseType(name);
		}
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node n = node.getChildNodes().item(i);
			short tp = n.getNodeType();
			if (tp != Node.ELEMENT_NODE) {
				continue;
			}
			String tag = n.getNodeName();
			String txt = n.getTextContent();
			d.addSqlTemplate(tag, txt);
		}
	}

}
