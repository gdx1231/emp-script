package com.gdxsoft.easyweb.statusControl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class St {
	private String _Name;
	private String _Des;
	private MTable _StVs;
	private String _Xml;

	static St parseToSt(Node xmlNode) {
		St s = new St();
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

		NodeList nl = ((Element) xmlNode).getElementsByTagName("stv");
		for (int i = 0; i < nl.getLength(); i++) {
			StV stv = StV.parseStV(nl.item(i));
			s._StVs.add(stv.getName(), stv);
		}
		return s;
	}

	public St() {
		this._StVs = new MTable();
	}

	public StV getStV(String name) {
		if (this._StVs.containsKey(name)) {
			return (StV) this._StVs.get(name);
		} else {
			return null;
		}
	}

	public int size() {
		return this._StVs.getCount();
	}

	public StV getStV(int index) {
		Object o = this._StVs.getByIndex(index);
		if (o == null) {
			return null;
		} else {
			return (StV) o;
		}
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
