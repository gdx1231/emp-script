package com.gdxsoft.easyweb.script.Workflow;

import java.util.HashMap;

import org.w3c.dom.*;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class OrgSqls {

	private static OrgSqls INS;
	private static int CODE;

	public static OrgSqls instance() {
		if (INS == null) {
			INS = new OrgSqls();
			INS.init();
		}
		String xml = UPath.getWFXML(); // defined by ewa_conf.xml
		if (xml.hashCode() != CODE) {
			INS = new OrgSqls();
			INS.init();
		}
		return INS;
	}

	private HashMap<String, Element> _Sqls;

	private OrgSqls() {
	}

	public String getSql(String name) {
		Element ele = this.getItem(name);
		if (ele == null) {
			return null;
		}
		return ele.getAttribute("Sql");
	}

	public Element getItem(String name) {
		if (name == null || name.trim().length() == 0) {
			return null;
		}
		String n = name.toUpperCase().trim();
		if (this._Sqls.containsKey(n)) {
			return this._Sqls.get(n);
		} else {
			return null;
		}
	}

	private synchronized void init() {
		String xml = UPath.getWFXML(); // defined by ewa_conf.xml

		CODE = xml.hashCode();

		_Sqls = new HashMap<String, Element>();

		Document doc = UXml.asDocument(xml);
		NodeList nl = doc.getElementsByTagName("sql");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String name = ele.getAttribute("Name");
			_Sqls.put(name.toUpperCase().trim(), ele);
		}
	}

}
