package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfRedises {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfRedises.class);
	private static ConfRedises INST = null;

	private static long PROP_TIME = 0;

	public static ConfRedises getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createNewRedises();
		return INST;
	}

	private synchronized static ConfRedises createNewRedises() {
		/*
		 * <redis name="r0" method="single" auth="passwrod" hosts="192.168.1.252:16379"
		 * ></redis>
		 */
		ConfRedises sps = new ConfRedises();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("redis");
		if (nl.getLength() == 0) {
			return null;
		}

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);

			Map<String, String> map = UXml.getElementAttributes(item, true);

			String name = map.get("name");
			String method = map.get("method");
			String auth = map.get("auth");
			String hosts = map.get("hosts");

			if (StringUtils.isBlank(name) || StringUtils.isBlank(hosts)) {
				LOGGER.warn("Invalid redis conf -> " + UXml.asXml(item));
				continue;
			}

			ConfRedis sp = new ConfRedis();
			sp.setName(name.trim());
			sp.setMethod(method);
			sp.setAuth(auth);
			sp.setHosts(hosts.trim());

			sps.lst.add(sp);
			
			LOGGER.info("Add Redis: {}, {}, {}", sp.getName(), sp.getMethod(), sp.getHosts() );
		}

		return sps;
	}

	private List<ConfRedis> lst = new ArrayList<>();

	/**
	 * Get the ScriptPath by the name
	 * 
	 * @param name the ScriptPath name
	 * @return ScriptPath
	 */
	public ConfRedis getScriptPath(String name) {
		for (int i = 0; i < this.lst.size(); i++) {
			if (this.lst.get(i).getName().equalsIgnoreCase(name)) {
				return this.lst.get(i);
			}
		}
		return null;
	}

	public List<ConfRedis> getLst() {
		return lst;
	}
}
