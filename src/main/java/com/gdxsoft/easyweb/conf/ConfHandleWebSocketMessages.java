package com.gdxsoft.easyweb.conf;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfHandleWebSocketMessages {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfHandleWebSocketMessages.class);
	private static ConfHandleWebSocketMessages INST = null;

	public static ConfHandleWebSocketMessages getInstance() {
		if (INST != null) {
			return INST;
		}
		INST = instance();
		return INST;
	}

	private synchronized static ConfHandleWebSocketMessages instance() {
		ConfHandleWebSocketMessages sps = new ConfHandleWebSocketMessages();

		try {
			loadHandles(sps);
		} catch (Exception e) {
			LOGGER.error("Load the user defined functions from ewa_conf.xml,", e);
		}
		return sps;
	}

	/**
	 * Load the handleWebSocketMessage from ewa_conf.xml
	 * 
	 * @param sps
	 */
	private static void loadHandles(ConfHandleWebSocketMessages sps) {
		if (UPath.getCfgXmlDoc() == null) {
			return;
		}

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("handleWebSocketMessage");
		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			ConfHandleWebSocketMessage sp = createConfFromXml(item);
			if (sp != null) {
				sps.handles.put(sp.getMethod(), sp);
				LOGGER.info("Added WS: {}, {}", sp.getMethod(), sp.getMapClass());
			}
		}
	}

	private static ConfHandleWebSocketMessage createConfFromXml(Element item) {
		ConfHandleWebSocketMessage sp = new ConfHandleWebSocketMessage();

		Map<String, String> vals = UXml.getElementAttributes(item, true);
		String method = vals.get("method");
		if (StringUtils.isBlank(method)) {
			LOGGER.warn("Invalid conf method: {}", UXml.asXml(item));
			return null;
		}
		String mapClass = vals.get("class");
		if (StringUtils.isBlank(mapClass)) {
			LOGGER.warn("Invalid conf class: {}", UXml.asXml(item));
			return null;
		}
		method = method.toLowerCase().trim();
		sp.setMethod(method);
		sp.setMapClass(mapClass);
		sp.setXml(UXml.asXml(item));

		return sp;
	}

	private Map<String, ConfHandleWebSocketMessage> handles;

	public ConfHandleWebSocketMessages() {
		this.handles = new HashMap<>();
	}

	public Map<String, ConfHandleWebSocketMessage> getHandles() {
		return this.handles;
	}

}
