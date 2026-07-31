package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.ConfValueResolvers;
import com.gdxsoft.easyweb.script.userConfig.ResourceConfig;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class ConfScriptPaths {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfScriptPaths.class);
	private static ConfScriptPaths INST = null;

	private static long PROP_TIME = 0;

	public static ConfScriptPaths getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		INST = createNewScriptPaths();
		return INST;
	}

	private synchronized static ConfScriptPaths createNewScriptPaths() {
		/*
		 * <scriptPaths> <scriptPath name="/ewa" path="resources:/user.xml/ewa" />
		 * <scriptPath name="/" path="jdbc:ewa" /> </scriptPaths>
		 */
		ConfScriptPaths sps = new ConfScriptPaths();

		if (UPath.getCfgXmlDoc() == null) {
			return null;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("scriptPath");
		if (nl.getLength() == 0) {
			// Get a configuration from the old version
			ConfScriptPath sp = getCompatibleConf();
			if (sp != null) {
				sps.lst.add(sp);
			}
			return sps;
		}

		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			UObjectValue uv = new UObjectValue();
			ConfScriptPath sp = new ConfScriptPath();
			uv.setObject(sp);
			uv.setAllValue(item);

			// 解析路径中的变量（如 ${env.EWA_HOME}、${user.home} 等）
			if (sp.getPath() != null && !sp.isJdbc() && !sp.isResources()) {
				String resolved = ConfValueResolvers.resolve(sp.getPath());
				sp.setPath(resolved);
			}

			if (sp.isResources()) { // force read-only mode
				sp.setReadOnly(true);
				// 初始化资源，放置到缓存中
				ResourceConfig.initializeResouces(sp);
			}
			sps.lst.add(sp);
			LOGGER.info("ScriptPath: {}, {}", sp.getName(), sp.getPath());
		}

		return sps;
	}

	private static ConfScriptPath getCompatibleConf() {
		// Compatible
		// <path des="用户配置文件目录" Name="script_path" Value="/Volumes/b2b/user.config.xml"
		// />
		// <path des="用户配置文件目录" Name="script_path" Value="jdbc:ewa" />
		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("path");
		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);

			Map<String, String> attrs = UXml.getElementAttributes(item, true);
			if ("script_path".equals(attrs.get("name"))) {
				ConfScriptPath sp = new ConfScriptPath();
				sp.setName(attrs.get("name"));
				String pathValue = attrs.get("value");
				// 解析路径中的变量
				if (pathValue != null && !pathValue.startsWith("jdbc:") && !pathValue.startsWith("resources:")) {
					pathValue = ConfValueResolvers.resolve(pathValue);
				}
				sp.setPath(pathValue);
				return sp;
			}
		}
		return null;
	}

	private List<ConfScriptPath> lst = new ArrayList<>();

	/**
	 * Get the ScriptPath by the name
	 * 
	 * @param name the ScriptPath name
	 * @return ScriptPath
	 */
	public ConfScriptPath getScriptPath(String name) {
		for (int i = 0; i < this.lst.size(); i++) {
			if (this.lst.get(i).getName().equalsIgnoreCase(name)) {
				return this.lst.get(i);
			}
		}
		return null;
	}

	public List<ConfScriptPath> getLst() {
		return lst;
	}
}
