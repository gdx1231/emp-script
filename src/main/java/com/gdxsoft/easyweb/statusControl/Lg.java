package com.gdxsoft.easyweb.statusControl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class Lg {

	private String _StName;
	private MTable _LgVs;

	private String _Xml;
	private St _St;

	static Lg parseToLg(Node xmlNode) {
		Lg s = new Lg();
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node att = xmlNode.getAttributes().item(i);
			String tag = att.getNodeName().toLowerCase();
			String val = att.getNodeValue();
			if (tag.equals("st")) {
				s._StName = val.trim().toUpperCase();
			}
		}
		s._Xml = UXml.asXml(xmlNode);
		s._LgVs = new MTable();
		NodeList nl = ((Element) xmlNode).getElementsByTagName("lgv");
		for (int i = 0; i < nl.getLength(); i++) {
			LgV lgv = LgV.parseToLgV(nl.item(i));
			s._LgVs.add(lgv.getStvName(), lgv);
			lgv.setLg(s);
		}
		return s;
	}

	public String createLogicExp() {
		MStr s = new MStr();
		for (int i = 0; i < this._LgVs.getCount(); i++) {
			LgV lgv = (LgV) this._LgVs.getByIndex(i);
			String name = lgv.getStvName();

			if (name.startsWith("#")) {
				s.al(name.replace("#", " "));
			} else {
				StV stv = this.getSt().getStV(name);
				lgv.setStV(stv);
				s.a(this.getSt().getName() + " " + lgv.getExp() + " '"
						+ stv.getName().replace("'", "''") + "' ");
			}
		}
		return s.toString();
	}

	/**
	 * @return the _StName
	 */
	public String getStName() {
		return _StName;
	}

	/**
	 * @return the _LgVs
	 */
	public MTable getLgVs() {
		return _LgVs;
	}

	/**
	 * @return the _Xml
	 */
	public String getXml() {
		return _Xml;
	}

	/**
	 * @return the _St
	 */
	public St getSt() {
		return _St;
	}

	/**
	 * @param st
	 *            the _St to set
	 */
	public void setSt(St st) {
		_St = st;
	}
}
