/**
 * 
 */
package com.gdxsoft.easyweb.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
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

	/**
	 * 
	 */
	private static ConnectionConfigs CNNS;
	private static int LAST_CODE;
	private static final long serialVersionUID = 1914850112639586466L;

	private ArrayList<String> _ListNames;

	public static ConnectionConfigs instance() throws ParserConfigurationException, SAXException, IOException {
		if (CNNS == null) {
			ConnectionConfigs cnn1s = new ConnectionConfigs();
			cnn1s.initDataSource();
			CNNS = cnn1s;
		} else {
			if (LAST_CODE > 0 && UPath.getDATABASEXML() != null) {
				int code = UPath.getDATABASEXML().hashCode();
				if (code != LAST_CODE) {
					ConnectionConfigs cnn1s = new ConnectionConfigs();
					cnn1s.initDataSource();
					CNNS = cnn1s;
				}
			}

		}
		return CNNS;
	}

	private synchronized void initDataSource() throws ParserConfigurationException, SAXException, IOException {

		if (StringUtils.isBlank(UPath.getDATABASEXML())) {
			return;
		}

		String xml = UPath.getDATABASEXML();
		LAST_CODE = xml.hashCode();
		Document doc = UXml.asDocument(xml);
		NodeList nl = doc.getElementsByTagName("database");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			ConnectionConfig cc = new ConnectionConfig(node);
			super.put(cc.getName(), cc);
			_ListNames.add(cc.getName());
		}
	}

	private ConnectionConfigs() throws ParserConfigurationException, SAXException, IOException {
		_ListNames = new ArrayList<String>();
		initDataSource();

	}

	public ConnectionConfig getConfig(int cfgIndex) {
		return super.get(_ListNames.get(cfgIndex));
	}

}