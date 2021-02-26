package com.gdxsoft.easyweb.uploader;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class UploaderUserTemplate {
	private Node _ItemNode;
	private String _Exts;
	private String _UploadPath;
	private String _Name;
	private String _TemplateName;
	private boolean _IsMultiFiles;
	private String _Css;
	private String _Javascript;
	private String _Html;

	private static String CONFIG_FILE = "EwaUpload.xml";

	public UploaderUserTemplate(String itemName)
			throws ParserConfigurationException, SAXException, IOException {
		this._Name = itemName.toUpperCase().trim();
		readXml();
		this.init();
		initSkin();
	}

	private void readXml() throws ParserConfigurationException, SAXException,
			IOException {
		Document doc = UXml.retDocument(UPath.getConfigPath() + CONFIG_FILE);
		NodeList nl = UXml.retNodeList(doc, "root/uploader");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = (Node) nl.item(i);
			String name = UXml.retNodeValue(node, "Name");
			if (name == null)
				continue;
			if (name.toUpperCase().trim().equals(_Name)) {
				this._ItemNode = node;
			}
		}
	}

	private void init() {
		this._TemplateName = UXml.retNodeValue(_ItemNode,
				"UploaderTemplateName");
		this._Name = UXml.retNodeValue(_ItemNode, "Name");
		this._Exts = UXml.retNodeValue(_ItemNode, "Exts");
		this._UploadPath = UXml.retNodeValue(_ItemNode, "UploadPath");
		String mm = UXml.retNodeValue(_ItemNode, "MultiFiles");
		if (mm.equals("1")) {
			this._IsMultiFiles = true;
		} else {
			this._IsMultiFiles = false;
		}

	}

	private void initSkin() {
		Node node = UXml.retNode(_ItemNode, "css");
		this._Css = UXml.retNodeText(node);

		node = UXml.retNode(_ItemNode, "javascript");
		this._Javascript = UXml.retNodeText(node);

		node = UXml.retNode(_ItemNode, "html");
		this._Html = UXml.retNodeText(node);
	}

	public String getExts() {
		return _Exts;
	}

	public boolean isMultiFiles() {
		return _IsMultiFiles;
	}

	public String getName() {
		return _Name;
	}

	public String getTemplateName() {
		return _TemplateName;
	}

	public String getUploadPath() {
		return _UploadPath;
	}

	public String getCss() {
		return _Css;
	}

	public String getHtml() {
		return _Html;
	}

	public String getJavascript() {
		return _Javascript;
	}

}
