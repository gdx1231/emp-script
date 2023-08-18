package com.gdxsoft.easyweb;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class SystemXmlUtils {
	private static Map<String, String> SYSTEM_XML = new ConcurrentHashMap<String, String>();
	private static Logger LOGGER = LoggerFactory.getLogger(SystemXmlUtils.class);

	public static boolean checkConfChanged(String confName, int intervalSeconds) {
		String configPath = UPath.getConfigPath();

		if (StringUtils.isBlank(configPath)) {
			// 资源文件在jar里
			return false;
		} else {
			String path = configPath + confName;
			return UFileCheck.fileChanged(path, intervalSeconds);
		}
	}

	public static boolean checkConfChanged(String confName) {
		return checkConfChanged(confName, 5);
	}

	public static Document getSystemConfDocument(String confName) throws Exception {
		String content = getSystemConfContent(confName);
		return UXml.asDocument(content);
	}

	/**
	 * Get the system inner config
	 * 
	 * @param confName
	 * @return
	 * @throws Exception
	 */
	public static String getSystemConfContent(String confName) throws Exception {
		if (confName.indexOf("..") >= 0) {
			LOGGER.error("Invalid confname: {}", confName);
			throw new Exception("Invalid confname");
		}
		String configPath = UPath.getConfigPath();

		String path;
		if (StringUtils.isBlank(configPath)) {
			// read from jar
			path = "/system.xml/" + confName;
			path = path.replace("\\", "/");

			while (path.indexOf("//") > 0) {
				path = path.replace("//", "/");
			}

			if (SYSTEM_XML.containsKey(path)) {
				return SYSTEM_XML.get(path);
			} else {
				return loadSystemXmlFromResources(path);
			}

		} else {
			path = UPath.getConfigPath() + confName;
		}
		return UFile.readFileText(path);
	}

	/**
	 * 同步读取
	 * 
	 * @param path
	 * @return
	 */
	private static synchronized String loadSystemXmlFromResources(String path) {
		URL url = SystemXmlUtils.class.getResource(path);
		if (url == null) {
			LOGGER.error("Can't found the CFG: {}", path);
			return null;
		}
		String xml;
		try {
			LOGGER.info("Load resource: {}", url.toString());
			xml = IOUtils.toString(url, StandardCharsets.UTF_8);
			SYSTEM_XML.put(path, xml);
			return xml;
		} catch (IOException e) {
			LOGGER.error("Load resource: {}, {}", e.getMessage(), url.toString());
			return null;
		}

	}
}
