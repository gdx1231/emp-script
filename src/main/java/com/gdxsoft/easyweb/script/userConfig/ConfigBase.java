package com.gdxsoft.easyweb.script.userConfig;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UXml;

public abstract class ConfigBase {
	private static Logger LOGER = LoggerFactory.getLogger(ConfigBase.class);

	private ScriptPath scriptPath;
	private String fixedXmlName;

	private String xmlName;
	private String itemName;

	public ConfigBase() {

	}

	public ConfigBase(ScriptPath scriptPath, String xmlName, String itemName) {
		this.scriptPath = scriptPath;
		this.xmlName = xmlName;
		this.fixedXmlName = xmlName == null ? null : UserConfig.filterXmlName(xmlName);
		this.itemName = itemName;
	}

	/**
	 * return the item from the configuration document
	 * 
	 * @param doc      the XML document
	 * @param itemName the item name
	 * @throws Exception
	 */
	public Node loadItemFromDoc(Document doc) throws Exception {
		String itemName1 = itemName.trim();

		NodeList nl = UXml.retNodeList(doc, "EasyWebTemplates/EasyWebTemplate");
		for (int i = 0; i < nl.getLength(); i++) {
			String name = UXml.retNodeValue(nl.item(i), "Name").trim();
			if (name.equalsIgnoreCase(itemName1)) {
				return nl.item(i);
			}
		}
		return null;
	}

	/**
	 * Return the configuration document
	 * 
	 * @param xmlContent the configuration XML content
	 * @return the configuration document
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getDocumentByXmlString(String xmlContent)
			throws ParserConfigurationException, SAXException, IOException {
		try {
			Document doc1 = UXml.asDocument(xmlContent);
			return doc1;
		} catch (Exception e) {
			LOGER.error("Invaid cast to xml document", e);
			throw e;
		}

	}

	/**
	 * Check if the item in the configuration file
	 * 
	 * @param xmlName  the configuration file
	 * @param itemName the item name
	 * @return true/false
	 */
	public boolean checkItemExists() {
		boolean isXmlExists = this.checkConfigurationExists();

		if (StringUtils.isBlank(this.getItemName())) {
			return isXmlExists;
		}

		if (!isXmlExists) {
			return false;
		}

		try {
			Node node = this.loadItem();
			return node != null;
		} catch (Exception e) {
			LOGER.error(e.getLocalizedMessage());
			return false;
		}
	}

	public abstract boolean checkConfigurationExists();

	public abstract Document loadConfiguration() throws Exception;

	/**
	 * return the item from the configuration file
	 * 
	 * @param itemName the item name
	 * @throws Exception
	 */
	public Node loadItem() throws Exception {
		Document doc = this.loadConfiguration();

		return this.loadItemFromDoc(doc);
	}

	public void setFixedXmlName(String fixedXmlName) {
		this.fixedXmlName = fixedXmlName;
	}

	public ScriptPath getScriptPath() {
		return scriptPath;
	}

	public String getFixedXmlName() {
		return fixedXmlName;
	}

	public String getXmlName() {
		return xmlName;
	}

	public String getItemName() {
		return itemName;
	}

	public void setScriptPath(ScriptPath scriptPath) {
		this.scriptPath = scriptPath;
	}

	public void setXmlName(String xmlName) {
		this.xmlName = xmlName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
}
