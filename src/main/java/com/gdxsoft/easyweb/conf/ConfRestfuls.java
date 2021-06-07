package com.gdxsoft.easyweb.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfRestfuls {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfRestfuls.class);
	private static ConfRestfuls INST = null;

	private static long PROP_TIME = 0;

	private static Map<String, Map<String, ConfRestful>> CONFS;

	public static ConfRestfuls getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createConfs();
		return INST;
	}

	/**
	 * Return the confs
	 * 
	 * @return
	 */
	public static Map<String, Map<String, ConfRestful>> getConfs() {
		getInstance();
		return CONFS;
	}

	private synchronized static ConfRestfuls createConfs() {

		ConfRestfuls sps = new ConfRestfuls();

		CONFS = new ConcurrentHashMap<>();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("restfuls");

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			createConfs(item);
		}

		return sps;
	}

	private static void createConfs(Element parentItem) {
		String pathParent = parentItem.getAttribute("path").trim();
		int inc = 0;
		while (pathParent.endsWith("/")) {
			pathParent = pathParent.substring(0, pathParent.length() - 1);
			inc++;
			if (inc == 1000) { // 疯了？
				break;
			}
		}

		NodeList nl = parentItem.getElementsByTagName("restful");

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			createRestfulConf(pathParent, item);
		}

	}

	private static void createRestfulConf(String pathParent, Element item) {

		NodeList methods = item.getChildNodes();
		for (int i = 0; i < methods.getLength(); i++) {
			Node methodItem = methods.item(i);

			if (methodItem.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			ConfRestful restful = new ConfRestful();

			UObjectValue uo = new UObjectValue();
			uo.setObject(restful);
			uo.setAllValue(item);

			// full path
			String restfulPath = pathParent + "/" + restful.getPath().trim();
			int inc = 0;
			while (restfulPath.indexOf("//") >= 0) {
				restfulPath = restfulPath.replace("//", "/");
				inc++;
				if (inc == 1000) { // 疯了？
					break;
				}
			}
			inc = 0;
			while (restfulPath.endsWith("/")) {
				restfulPath = restfulPath.substring(0, restfulPath.length() - 1);
				inc++;
				if (inc == 1000) { // 疯了？
					break;
				}
			}

			restful.setRestfulPath(restfulPath);

			restful.setPathDirsDepth(restfulPath.split("/").length);

			uo.setAllValue((Element) methodItem);

			restful.setXml(UXml.asXml(methodItem));

			restful.setMethod(methodItem.getNodeName().toUpperCase());

			Map<String, ConfRestful> map;
			if (!CONFS.containsKey(restfulPath)) {
				map = new HashMap<String, ConfRestful>();
				CONFS.put(restfulPath, map);
			} else {
				map = CONFS.get(restfulPath);
			}

			// http method, PUT/GET/POST/PATCH/DELETE
			String httpMethod = restful.getMethod();
			if (map.containsKey(httpMethod)) {
				LOGGER.warn("The repeat being overwrited: {} with {}", map.get(httpMethod).getXml(), restful.getXml());
			} else {
				LOGGER.info("Add restful ->{}: {}", restful.getRestfulPath(), restful.getXml());
			}
			map.put(httpMethod, restful);
		}

	}
}
