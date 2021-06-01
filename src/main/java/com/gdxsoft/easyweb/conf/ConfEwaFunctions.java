package com.gdxsoft.easyweb.conf;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.SystemXmlUtils;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfEwaFunctions {
	private static final String CFG_NAME = "EwaFunctions.xml";
	private static Logger LOGGER = LoggerFactory.getLogger(ConfEwaFunctions.class);
	private static ConfEwaFunctions INST = null;

	public static ConfEwaFunctions getInstance() {
		if (INST != null) {
			return INST;
		}
		INST = createNewScriptPaths();
		return INST;
	}

	private synchronized static ConfEwaFunctions createNewScriptPaths() {
		ConfEwaFunctions sps = new ConfEwaFunctions();

		try {
			loadSystemDefinedFunctions(sps);
		} catch (Exception e) {
			LOGGER.error("Load the system functions error: {}, {} ", CFG_NAME, e);
		}
		try {
			loadUserDefinedFunctions(sps);
		} catch (Exception e) {
			LOGGER.error("Load the user defined functions from ewa_conf.xml,", e);
		}
		return sps;
	}

	private static void loadSystemDefinedFunctions(ConfEwaFunctions sps) {
		Document doc;
		try {
			doc = SystemXmlUtils.getSystemConfDocument(CFG_NAME);
		} catch (Exception e) {
			LOGGER.error("Load the system functions error: {}, {} ", CFG_NAME, e);
			return;
		}

		NodeList nl = doc.getElementsByTagName("EwaFunction");
		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			ConfEwaFunction sp = createConfFromXml(item);
			sp.setUserDefined(false);

			sps.functions.put(sp.getName(), sp);

			LOGGER.info("Load a system function: {}", sp.getXml());
		}

	}

	/**
	 * Load the functions from ewa_conf.xml
	 * 
	 * @param sps
	 */
	private static void loadUserDefinedFunctions(ConfEwaFunctions sps) {
		if (UPath.getCfgXmlDoc() == null) {
			return;
		}

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("EwaFunction");
		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			ConfEwaFunction sp = createConfFromXml(item);
			sp.setUserDefined(true);

			if (sps.functions.containsKey(sp.getName())) {
				sps.functions.put(sp.getName(), sp);
				LOGGER.info("Overwrite the system function with: {}", sp.getXml());
			} else {
				sps.functions.put(sp.getName(), sp);
				LOGGER.info("Load a user function: {}", sp.getXml());
			}
		}
	}

	private static ConfEwaFunction createConfFromXml(Element item) {
		ConfEwaFunction sp = new ConfEwaFunction();
		UObjectValue.fromXml(item, sp);

		String name = sp.getName().toUpperCase().trim();
		sp.setName(name);
		sp.setXml(UXml.asXml(item));

		return sp;
	}

	private Map<String, ConfEwaFunction> functions;

	public ConfEwaFunctions() {
		this.functions = new HashMap<>();
	}

	public Map<String, ConfEwaFunction> getFunctions() {
		return this.functions;
	}

}
