package com.gdxsoft.easyweb.define;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;

public class UserXmlItems {

	private String _XmlName;
	private String _ItemName;

	public UserXmlItems(String xmlName, String itemName) {
		this._XmlName = xmlName;
		this._ItemName = itemName.toUpperCase().trim();
	}

	/**
	 * 获取配置项目下的Items
	 * 
	 * @return
	 * @throws Exception
	 */
	public ArrayList<UserXmlItem> getItems() throws Exception {

		IUpdateXml o = ConfigUtils.getUpdateXml(_XmlName);
		Node item = o.queryItem(_ItemName);

		if (item == null) {
			return null;
		}
		ArrayList<UserXmlItem> al = new ArrayList<UserXmlItem>();
		NodeList nl = UXml.retNodeList(item, "XItems/XItem");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			String name = UXml.retNodeValue(n, "Name").toUpperCase().trim();
			UserXmlItem a = new UserXmlItem();
			a.setName(name);
			a.setDes(this.returnTag(n, "DescriptionSet"));
			a.setType(this.returnTag(n, "Tag"));

			al.add(a);
		}
		return al;
	}

	private String returnTag(Node node, String tagName) {
		String p = "" + tagName + "/Set/";
		Node n = UXml.retNode(node, p);
		if (tagName.equals("DescriptionSet")) {
			tagName = "Info";
		}
		String s1 = UXml.retNodeValue(n, tagName);
		return s1;
	}
}
