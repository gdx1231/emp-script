package com.gdxsoft.easyweb.statusControl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class Fm {
	private String _Name;
	private String _Des;
	private String _Xml;

	private String _XmlName;
	private String _ItemName;

	private MTable _Lgs;

	static Fm parseToFm(Node xmlNode) {
		Fm s = new Fm();
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node att = xmlNode.getAttributes().item(i);
			String tag = att.getNodeName().toLowerCase();
			String val = att.getNodeValue();
			if (tag.equals("name")) {
				s._Name = val.trim().toUpperCase();
			} else if (tag.equals("des")) {
				s._Des = val;
			} else if (tag.equals("xmlname")) {
				s._XmlName = val;
			} else if (tag.equals("itemname")) {
				s._ItemName = val;
			}
		}
		s._Xml = UXml.asXml(xmlNode);

		s._Lgs = new MTable();
		Element ele = (Element) xmlNode;

		NodeList nl = ele.getElementsByTagName("lg");
		for (int i = 0; i < nl.getLength(); i++) {
			Lg lg = Lg.parseToLg(nl.item(i));

			s._Lgs.add(lg.getStName(), lg);
		}
		return s;
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @return the _Des
	 */
	public String getDes() {
		return _Des;
	}

	/**
	 * @return the _Xml
	 */
	public String getXml() {
		return _Xml;
	}

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @return the _ItemName
	 */
	public String getItemName() {
		return _ItemName;
	}

	/**
	 * @return the _Lgs
	 */
	public MTable getLgs() {
		return _Lgs;
	}

}
