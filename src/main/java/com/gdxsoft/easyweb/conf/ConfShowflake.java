package com.gdxsoft.easyweb.conf;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * Get the configuration of the snowflake parameters (workId, datacenterId)
 */
public class ConfShowflake {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfShowflake.class);
	private static ConfShowflake INST = null;
	private static long PROP_TIME = 0;

	public static ConfShowflake getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		try {
			initDefine();
		} catch (Exception err) {
			LOGGER.error("Initialize the snowflake conf error: {}", err);
		}
		return INST;
	}

	/**
	 * Initialized the configuration of the snowflake
	 */
	private synchronized static void initDefine() {
		// <define value="true" />
		if (UPath.getCfgXmlDoc() == null) {
			return;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("snowflake");
		if (nl.getLength() == 0) {
			String debugXml = UXml.asXml(UPath.getCfgXmlDoc());
			LOGGER.error("NOT found the snowflake conf int the ewa_conf.xml");
			LOGGER.debug(debugXml);
			return;
		}

		Element item = (Element) nl.item(0);
		LOGGER.info("Init the snowflake: {}", UXml.asXml(item));

		Map<String, String> attrs = UXml.getElementAttributes(item, true);
		if (!(attrs.containsKey("workid") && attrs.containsKey("datacenterid"))) {
			return;
		}
		INST = new ConfShowflake();

		INST.workId = Long.parseLong(attrs.get("workid"));
		INST.datacenterId = Long.parseLong(attrs.get("datacenterid"));
	}

	private Long workId;
	private Long datacenterId;

	public Long getWorkId() {
		return workId;
	}

	public void setWorkId(Long workId) {
		this.workId = workId;
	}

	public Long getDatacenterId() {
		return datacenterId;
	}

	public void setDatacenterId(Long datacenterId) {
		this.datacenterId = datacenterId;
	}

}
