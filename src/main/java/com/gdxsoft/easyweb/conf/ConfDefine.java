package com.gdxsoft.easyweb.conf;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class ConfDefine {
	private static Boolean INST = null;
	private static long PROP_TIME = 0;

	/**
	 * Whether to allow configuration files management
	 * 
	 * @return true:allow, false:deny
	 */
	public static boolean isAllowDefine() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		initDefine();
		return INST == null ? false : INST.booleanValue();
	}

	private synchronized static void initDefine() {
		// <define value="true" />
		if (UPath.getCfgXmlDoc() == null) {
			return;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("define");
		if (nl.getLength() == 0) {
			return;
		}
		Element item = (Element) nl.item(0);

		INST = Utils.cvtBool(item.getAttribute("value"));

	}
}
