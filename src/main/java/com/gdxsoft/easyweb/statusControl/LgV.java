package com.gdxsoft.easyweb.statusControl;

import org.w3c.dom.Node;

import com.gdxsoft.easyweb.utils.UXml;

public class LgV {
	// <lgv stv='OUT_NONE' exp='=' />
	private String _StvName;
	private String _Exp;
	private StV _StV;
	private String _Xml;
	private Lg _Lg;
	static LgV parseToLgV(Node xmlNode) {
		LgV s = new LgV();
		for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
			Node att = xmlNode.getAttributes().item(i);
			String tag = att.getNodeName().toLowerCase();
			String val = att.getNodeValue();
			if (tag.equals("stv")) {
				s._StvName = val.trim().toUpperCase();
			} else if (tag.equals("exp")) {
				s._Exp = val;
			}
		}
		s._Xml = UXml.asXml(xmlNode);
		return s;
	}

	/**
	 * @return the _StV
	 */
	public StV getStV() {
		return _StV;
	}

	/**
	 * @param stV
	 *            the _StV to set
	 */
	public void setStV(StV stV) {
		_StV = stV;
	}

	/**
	 * @return the _StvName
	 */
	public String getStvName() {
		return _StvName;
	}

	/**
	 * @return the _Exp
	 */
	public String getExp() {
		return _Exp;
	}

	/**
	 * @return the _Xml
	 */
	public String getXml() {
		return _Xml;
	}

	/**
	 * @return the _Lg
	 */
	public Lg getLg() {
		return _Lg;
	}

	/**
	 * @param lg the _Lg to set
	 */
	public void setLg(Lg lg) {
		_Lg = lg;
	}

}
