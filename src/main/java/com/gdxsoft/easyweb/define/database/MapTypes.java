/**
 * 
 */
package com.gdxsoft.easyweb.define.database;

import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UXml;


/**
 * @author Administrator
 * 
 */
public class MapTypes extends HashMap<String, MapType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8287123891239066957L;
	private String _Name;
	private boolean _Identity;

	public MapTypes(Node node) {
		setToMap(node);
	}

	private void setToMap(Node node) {
		// <type name="char" standard="varchar" length="columnsize">
		this._Name = UXml.retNodeValue(node, "name").toLowerCase().trim();
		String identity = UXml.retNodeValue(node, "identity").toLowerCase()
				.trim();
		if (identity.equals("1")) {
			this._Identity = true;
		} else {
			this._Identity = false;
		}

		NodeList nl = UXml.retNodeList(node, "type");
		for (int i = 0; i < nl.getLength(); i++) {
			MapType mt = new MapType();
			String length = UXml.retNodeValue(nl.item(i), "length")
					.toLowerCase().trim();
			String standard = UXml.retNodeValue(nl.item(i), "standard")
					.toLowerCase().trim();
			String name = UXml.retNodeValue(nl.item(i), "name")
					.toLowerCase().trim();
			String lexp = UXml.retNodeValue(nl.item(i), "lexp")
					.toLowerCase().trim();
			String def = UXml.retNodeValue(nl.item(i), "default")
					.toLowerCase().trim();
			String sMax = UXml.retNodeValue(nl.item(i), "max")
					.toLowerCase().trim();
			String other = UXml.retNodeValue(nl.item(i), "other")
					.toLowerCase().trim();
			if (standard.equals("")) {
				standard = name;
			}
			if (length.equals("")) {
				length = "columnsize";
			}
			boolean isDefault = false;
			if (def != null && def.equals("1")) {
				isDefault = true;
			}
			int max = -1;
			if (sMax.length() > 0) {
				try {
					max = Integer.parseInt(sMax);
				} catch (Exception e) {
				}
			}
			mt.setLength(length);
			mt.setName(name);
			mt.setStandard(standard);
			mt.setLExp(lexp);
			mt.setDefault(isDefault);
			mt.setMax(max);
			mt.setOther(other);
			super.put(name, mt);
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

	public MapType getDefaultMapType() {
		MapType def = super.get("__default");
		String name = def.getOther();
		return super.get(name);
	}

	/**
	 * @return the _Identity
	 */
	public boolean isIdentity() {
		return _Identity;
	}

	/**
	 * @param identity
	 *            the _Identity to set
	 */
	public void setIdentity(boolean identity) {
		_Identity = identity;
	}
}
