package com.gdxsoft.easyweb.script.userConfig;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class ResourceConfig extends ConfigBase implements IConfig {

	private static Logger LOGER = LoggerFactory.getLogger(ResourceConfig.class);

	public ResourceConfig(ScriptPath scriptPath, String xmlName, String itemName) {
		super(scriptPath, xmlName, itemName);
	} 

	public String getPath() {
		String root = super.getScriptPath().getResourcesPath();
		return root + super.getFixedXmlName();
	}

	/**
	 * Check if the configuration file exists
	 */
	public boolean checkConfigurationExists() {
		String resourcePath = this.getPath();
		URL url = ResourceConfig.class.getResource(resourcePath);
		return url != null;
	}

	/**
	 * return the configuration XML document
	 * 
	 * @param xmlName the configuration name
	 * @throws Exception
	 */
	public Document loadConfiguration() throws Exception {
		String resourcePath = this.getPath();
		URL url = ResourceConfig.class.getResource(resourcePath);

		if (url == null) {
			String err = "The resource " + super.getXmlName() + " not exists";
			LOGER.error(err);
			throw new Exception(err);
		}

		String xmlContent = IOUtils.toString(url, StandardCharsets.UTF_8);

		return super.getDocumentByXmlString(xmlContent);
	}

	 

}
