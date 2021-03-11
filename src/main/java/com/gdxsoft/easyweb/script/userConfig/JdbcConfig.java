package com.gdxsoft.easyweb.script.userConfig;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.utils.UXml;

public class JdbcConfig extends ConfigBase implements IConfig, Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1935997388322977522L;
	private static Logger LOGER = LoggerFactory.getLogger(JdbcConfig.class);
	private JdbcConfigOperation op;

	public JdbcConfig() {
		super();
	}

	public JdbcConfig(ConfScriptPath scriptPath, String xmlName, String itemName) {
		super(scriptPath, xmlName, itemName);
		op = new JdbcConfigOperation(scriptPath);
	}

	@Override
	public String getPath() {
		return super.getScriptPath().getJdbcConfigName() + this.getFixedXmlName();
	}

	@Override
	public boolean checkConfigurationExists() {
		return op.checkExists(super.getXmlName(), "");
	}

	/**
	 * return the item from the configuration file
	 * 
	 * @param itemName the item name
	 * @throws Exception
	 */
	@Override
	public Node loadItem() throws Exception {
		String itemXml = op.getJdbcItemXml(getXmlName(), super.getItemName());
		if (itemXml == null) {
			String err = "The jdbc " + super.getXmlName() + ", " + super.getItemName() + " not exists";
			LOGER.error(err);
			throw new Exception(err);
		}
		try {
			return UXml.asNode(itemXml);
		} catch (Exception e) {
			LOGER.error(e.getLocalizedMessage());
			throw e;
		}
	}

	@Override
	public Document loadConfiguration() throws Exception {
		String xmlDocument = op.getXml(getXmlName());
		if (xmlDocument == null) {
			String err = "The jdbc " + super.getXmlName() + " not exists";
			LOGER.error(err);
			throw new Exception(err);
		}
		return super.getDocumentByXmlString(xmlDocument);
	}

}
