package com.gdxsoft.easyweb.statusControl;

import org.w3c.dom.Node;

import com.gdxsoft.easyweb.utils.UXml;

public class StV {

	private String _Name;
	private String _Des;
	private String _Xml;

	static StV parseStV(Node xmlNode) {
		StV s = new StV();
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node att = xmlNode.getAttributes().item(i);
			String tag = att.getNodeName().toLowerCase();
			String val = att.getNodeValue();
			if (tag.equals("name")) {
				s._Name = val.trim().toUpperCase();
			} else if (tag.equals("des")) {
				s._Des = val;
			}
		}
		s.setXml(UXml.asXml(xmlNode));
		return s;
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
	 * @return the _Des
	 */
	public String getDes() {
		return _Des;
	}

	/**
	 * @param des
	 *            the _Des to set
	 */
	public void setDes(String des) {
		_Des = des;
	}

	/**
	 * @return the _Xml
	 */
	public String getXml() {
		return _Xml;
	}

	/**
	 * @param xml
	 *            the _Xml to set
	 */
	public void setXml(String xml) {
		_Xml = xml;
	}

}
