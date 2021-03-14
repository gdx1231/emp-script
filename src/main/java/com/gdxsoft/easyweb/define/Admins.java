package com.gdxsoft.easyweb.define;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;

public class Admins {

	public void init() {
		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("Admins");
		if (nl.getLength() == 0) {
			nl = UPath.getCfgXmlDoc().getElementsByTagName("admins");
			return;
		}

		Element admins = (Element) nl.item(0);
		NodeList nlAdm = admins.getElementsByTagName("Admin");
		if (nlAdm.getLength() == 0) {
			nlAdm = admins.getElementsByTagName("admin");
		}

	}
}
