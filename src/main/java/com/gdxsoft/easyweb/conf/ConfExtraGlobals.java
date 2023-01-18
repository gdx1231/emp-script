package com.gdxsoft.easyweb.conf;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * 额外的Global定义，主要解决英式日期格式和美式的不同
 * 
 * @author admin
 *
 */
public class ConfExtraGlobals {
	private static final Map<String, ConfExtraGlobal> CONFS = new java.util.concurrent.ConcurrentHashMap<>();
	private static Logger LOGGER = LoggerFactory.getLogger(ConfExtraGlobals.class);
	private static ConfExtraGlobals INST = null;

	private static long PROP_TIME = 0;

	public static ConfExtraGlobals getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createConfs();
		return INST;
	}

	private synchronized static ConfExtraGlobals createConfs() {
		/*
		 * <scriptPaths> <scriptPath name="/ewa" path="resources:/user.xml/ewa" />
		 * <scriptPath name="/" path="jdbc:ewa" /> </scriptPaths>
		 */
		ConfExtraGlobals sps = new ConfExtraGlobals();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("global");

		if (nl.getLength() == 0) {
			return null;
		}

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			ConfExtraGlobal sp = new ConfExtraGlobal();

			/*
			 * Map<String, String> vals = UXml.getElementAttributes(item, true);
			 * sp.setUserName( vals.get("username") );
			 * sp.setCreateDate(vals.get("createdate")); sp.setLoginId(vals.get("loginid"));
			 * sp.setPassword(vals.get("password"));
			 */
			UObjectValue uo = new UObjectValue();

			uo.setObject(sp);
			uo.setAllValue(item);
			CONFS.put(sp.getLang().toLowerCase(), sp);

			LOGGER.info("Add global {}", UXml.asXml(item));
		}

		return sps;
	}

	/**
	 * 获取额外的Global的定义
	 * 
	 * @param lang
	 * @return
	 */
	public ConfExtraGlobal getConfExtraGlobalByLang(String lang) {
		if (lang == null) {
			return null;
		}

		return CONFS.get(lang.trim().toLowerCase());
	}
}
