package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfAddedResources {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfAddedResources.class);
	private static ConfAddedResources INST = null;

	private static long PROP_TIME = 0;

	private static Map<String, List<ConfAddedResource>> CACHES;

	public static ConfAddedResources getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createConfs();
		return INST;
	}

	private synchronized static ConfAddedResources createConfs() {
		// <addedResources>
		// <addedResource src="/static/add_0.js" name="addjs0"></addedResource>
		// <addedResource src="https://www.gdxsoft.com/js/add_1.js" name="addjs1"></addedResource>
		// <addedResource src="/static/add_0.css" name="addcss0"></addedResource>
		// <addedResource src="https://www.gdxsoft.com/css/add_1.css" name="addcss1"></addedResource>
		// </addedResources>

		ConfAddedResources sps = new ConfAddedResources();

		CACHES = new ConcurrentHashMap<>();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("addedResource");

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			ConfAddedResource sp = new ConfAddedResource();

			UObjectValue uo = new UObjectValue();

			uo.setObject(sp);
			uo.setAllValue(item);
			sp.setXml(UXml.asXml(item));
			if (sp.getName() == null || sp.getName().trim().length() == 0 || sp.getSrc() == null
					|| sp.getSrc().trim().length() == 0) {
				LOGGER.warn("Invalid addedResource cfg ->" + sp.getXml());
				continue;
			}

			sps.getResources().put(sp.getName(), sp);
			LOGGER.debug(sp.getXml());
		}

		return sps;
	}

	/**
	 * 根据名称获取资源清单
	 * 
	 * @param namesExp 用,分割的字符串表达式
	 * @param last     是否在页面的底部，例如js
	 * @return 列表
	 */
	public List<ConfAddedResource> getResList(String namesExp, boolean last) {
		List<ConfAddedResource> al = new ArrayList<ConfAddedResource>();
		if (namesExp == null || namesExp.trim().length() == 0) {
			return al;
		}
		String cacheKey = namesExp + "," + last;
		if (CACHES.containsKey(cacheKey)) {
			return CACHES.get(cacheKey);
		}
		String[] names = namesExp.split(",");

		for (int i = 0; i < names.length; i++) {
			String name = names[i].trim();
			if (this.resources.containsKey(name)) {
				ConfAddedResource r = this.resources.get(name);
				if (r.isLast() == last) {
					al.add(r);
				}
			}
		}
		CACHES.put(cacheKey, al);
		return al;
	}

	private Map<String, ConfAddedResource> resources = new HashMap<>();

	public Map<String, ConfAddedResource> getResources() {
		return resources;
	}
}
