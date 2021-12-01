package com.gdxsoft.easyweb.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Utils;

public class ConfDefine {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfSecurities.class);
	private static ConfDefine INST = null;
	private static long PROP_TIME = 0;

	/**
	 * Whether to allow configuration files management
	 * 
	 * @return true:allow, false:deny
	 */
	public static boolean isAllowDefine() {
		return getInstance().isDefine();
	}
	/**
	 * Return the instance of define
	 * @return
	 */
	public static ConfDefine getInstance() {
		if (INST != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return INST;
			}
		}
		initDefine();
		return INST ;
	}
	
	private synchronized static void initDefine() {
		// <define value="true" />
		if (UPath.getCfgXmlDoc() == null) {
			return;
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();
		INST = new ConfDefine();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("define");
		if (nl.getLength() == 0) {
			return;
		}
		Element item = (Element) nl.item(0);

		// value was old version, new version is allow
		String allow = item.hasAttribute("allow") ? item.getAttribute("allow") : item.getAttribute("value");
		// allow or deny using ewa_define
		INST.define = Utils.cvtBool(allow);
		LOGGER.info("ConfDefine.define=" + INST.define);
		if (!INST.define) {
			return;
		}
		// the ewa-conf-help hsqldb data path
		/*
		 * INST.ewaConfHelpDbPath = item.getAttribute("ewaConfHelpDbPath"); if
		 * (StringUtils.isBlank(INST.ewaConfHelpDbPath)) { INST.ewaConfHelpDbPath =
		 * FileUtils.getTempDirectoryPath() + File.separatorChar +
		 * "ewa_conf_help_db_path"; } LOGGER.info("ConfDefine.ewaConfHelpDbPath=" +
		 * INST.ewaConfHelpDbPath);
		 */

		INST.apiKey = item.getAttribute("apiKey");
		INST.apiSecert = item.getAttribute("apiSecert");
		INST.apiServer = item.getAttribute("apiServer");
	}

	private boolean define;
	private String ewaConfHelpDbPath;

	private String apiKey;
	private String apiSecert;
	private String apiServer;

	public String getEwaConfHelpDbPath() {
		return ewaConfHelpDbPath;
	}

	public void setEwaConfHelpDbPath(String ewaConfHelpDbPath) {
		this.ewaConfHelpDbPath = ewaConfHelpDbPath;
	}

	public boolean isDefine() {
		return define;
	}

	public void setDefine(boolean define) {
		this.define = define;
	}

	/**
	 * 模块服务器的ApiKey
	 * 
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * 模块服务器的ApiKey
	 * 
	 * @param apiKey the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * 模块服务器的ApiSecert
	 * 
	 * @return the apiSecert
	 */
	public String getApiSecert() {
		return apiSecert;
	}

	/**
	 * 模块服务器的ApiSecert
	 * 
	 * @param apiSecert the apiSecert to set
	 */
	public void setApiSecert(String apiSecert) {
		this.apiSecert = apiSecert;
	}

	/**
	 * 模块服务器的网址
	 * 
	 * @return the apiServer
	 */
	public String getApiServer() {
		return apiServer;
	}

	/**
	 * 模块服务器的网址
	 * 
	 * @param apiServer the apiServer to set
	 */
	public void setApiServer(String apiServer) {
		this.apiServer = apiServer;
	}

}
