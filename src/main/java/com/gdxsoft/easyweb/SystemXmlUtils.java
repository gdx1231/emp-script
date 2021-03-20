package com.gdxsoft.easyweb;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class SystemXmlUtils {
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

	public static String getSystemConfContent(String confName) throws Exception {
		String configPath = UPath.getConfigPath();

		String path;
		if (StringUtils.isBlank(configPath)) {
			// read from jar
			path = "/system.xml/" + confName;
			path = path.replace("\\", "/");

			while (path.indexOf("//") > 0) {
				path = path.replace("//", "/");
			}

			URL url = SystemXmlUtils.class.getClassLoader().getResource(path);
			if (url == null) {
				throw new Exception("The file " + url + " not exists");
			}
			return IOUtils.toString(url, StandardCharsets.UTF_8);

		} else {
			path = UPath.getConfigPath() + confName;
		}
		return UFile.readFileText(path);
	}
}
