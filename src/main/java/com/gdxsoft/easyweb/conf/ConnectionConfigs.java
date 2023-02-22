/**
 * 
 */
package com.gdxsoft.easyweb.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * @author Administrator
 * 
 */
public class ConnectionConfigs extends HashMap<String, ConnectionConfig> {
	private static Logger LOGGER = LoggerFactory.getLogger(ConnectionConfigs.class);
	/**
	 * 
	 */
	private static ConnectionConfigs CNNS;
	private static final long serialVersionUID = 1914850112639586466L;

	private static long PROP_TIME = 0;

	private ArrayList<String> _ListNames;

	/**
	 * Get the instance
	 * 
	 * @return ConnectionConfigs
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static ConnectionConfigs instance() throws ParserConfigurationException, SAXException, IOException {
		if (CNNS != null) {
			if (UPath.getPropTime() == PROP_TIME) {
				return CNNS;
			}
		} else {
			initNew();
		}
		return CNNS;
	}

	/**
	 * Create the new instance
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static synchronized void initNew() throws ParserConfigurationException, SAXException, IOException {
		ConnectionConfigs cnn1s = new ConnectionConfigs();
		cnn1s.initDataSource();
		CNNS = cnn1s;
	}

	private void initDataSource() throws ParserConfigurationException, SAXException, IOException {
		String xml = UPath.getDATABASEXML();
		if (StringUtils.isBlank(xml)) {
			PROP_TIME = 0;
			return;
		}

		_ListNames.clear();
		super.clear();

		PROP_TIME = UPath.getPropTime();

		Document doc = UXml.asDocument(xml);
		NodeList nl = doc.getElementsByTagName("database");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			// System.out.println(UXml.asXmlPretty(node));

			ConnectionConfig cc = new ConnectionConfig(node);
			addCfg(cc);
		}
	}

	private void addCfg(int index, ConnectionConfig cc) {
		String key = cc.getName();
		if (super.containsKey(key)) {
			LOGGER.info("{}. Skipt repeat ConCfg: {}, {}, {} ", index, key, cc.getType(), cc.getConnectionString());
			return;
		}
		super.put(key, cc);
		_ListNames.add(key);
		LOGGER.info("{}. Added ConCfg: {}, {}, {}", index, key, cc.getType(), cc.getConnectionString());
	}

	public void addCfg(ConnectionConfig cc) {
		this.addCfg(_ListNames.size(), cc);
	}

	private ConnectionConfigs() {
		_ListNames = new ArrayList<String>();
	}

	public ConnectionConfig getConfig(int cfgIndex) {
		return super.get(_ListNames.get(cfgIndex));
	}

	/**
	 * @return the _ListNames
	 */
	public ArrayList<String> getListNames() {
		return _ListNames;
	}

}