package com.gdxsoft.easyweb.define;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * 更新xml的基础类
 * 
 * @author admin
 *
 */
public class UpdateXmlBase {
	String _RootUri = "EasyWebTemplates";
	String _ItemUri = "EasyWebTemplate";

	String _XmlName;
	Document _Document;
	String _FrameType;
	private String _XmlFilePath;

	public String getXmlFilePath() {
		if (StringUtils.isBlank(_XmlFilePath) && configType != null) {
			String path = configType.getPath();
			this._XmlFilePath = path;
		}
		return this._XmlFilePath;
	}

	public void setXmlFilePath(String xmlFilePath) {
		this._XmlFilePath = xmlFilePath;
	}

	String _CreateDate;
	String _UpdateDate;
	String _BackUpXmlName;
	IConfig configType;
	ConfScriptPath scriptPath;
	private String _AdmId; // 管理员

	void updateTime(Node item) {
		Element ele = (Element) item;
		this._UpdateDate = Utils.getDateTimeString(new Date());
		ele.setAttribute("CreateDate", _CreateDate);
		ele.setAttribute("UpdateDate", _UpdateDate);
	}

	/**
	 * 修正item的 XML 数据
	 * 
	 * @param xml
	 * @param itemName
	 * @return
	 */
	public String fixXml(String xml, String itemName) {
		xml = UXml.filterInvalidXMLcharacter(xml);
		// xml = xml.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");

		Document doc1 = UXml.asDocument(xml);
		Element e1 = (Element) doc1.getFirstChild();
		e1.setAttribute("Name", itemName);
		Node nodeName = UXml.retNode(doc1, _ItemUri + "/Page/Name/Set");
		if (nodeName != null) {
			Element e2 = (Element) nodeName;
			e2.setAttribute("Name", itemName);
		}
		clearDoc(doc1);

		xml = UXml.asXml(doc1.getFirstChild());

		return xml;
	}

	void queryNodeCreateDate(Node node) {
		_CreateDate = UXml.retNodeValue(node, "CreateDate");
		if (_CreateDate == null || _CreateDate.trim().length() == 0) {
			_CreateDate = Utils.getDateTimeString(new Date());
		}
	}

	/**
	 * 清除文档没用的节点信息
	 * 
	 * @param doc1
	 */
	public static void clearDoc(Document doc1) {
		HashMap<String, HashMap<String, Boolean>> map = new HashMap<String, HashMap<String, Boolean>>();
		String pathConfigDoc = UPath.getConfigPath() + "/EwaConfig.xml";
		try {
			Document docConfig = UXml.retDocument(pathConfigDoc);
			NodeList nl = docConfig.getElementsByTagName("XItem");

			for (int i = 0; i < nl.getLength(); i++) {
				Element ele = (Element) nl.item(i);
				String name = ele.getAttribute("Name").trim().toUpperCase();
				String Parameters = ele.getAttribute("Parameters");
				if (Parameters == null || Parameters.trim().length() == 0) {
					continue;
				}
				HashMap<String, Boolean> haves = new HashMap<String, Boolean>();
				String[] ps = Parameters.split(",");
				for (int k = 0; k < ps.length; k++) {
					String key = ps[k].trim().toUpperCase();
					haves.put(key, true);
				}
				if (!haves.containsKey("TAG")) {
					haves.put("TAG", true);
				}
				map.put(name, haves);
			}
		} catch (Exception err) {
		}

		NodeList nlXitems = doc1.getElementsByTagName("XItem");
		for (int i = 0; i < nlXitems.getLength(); i++) {
			Element xitem = (Element) nlXitems.item(i);
			Element tag = (Element) xitem.getElementsByTagName("Tag").item(0);
			if (tag.getElementsByTagName("Set").getLength() == 0) {
				continue;
			}
			String tagName = ((Element) tag.getElementsByTagName("Set").item(0)).getAttribute("Tag").trim()
					.toUpperCase();
			ArrayList<Node> removes = new ArrayList<Node>();
			if (map.containsKey(tagName)) {
				HashMap<String, Boolean> haves = map.get(tagName);
				for (int k = 0; k < xitem.getChildNodes().getLength(); k++) {
					Node chd = xitem.getChildNodes().item(k);
					if (chd.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					String nn = chd.getNodeName().toUpperCase();
					if (!haves.containsKey(nn)) {
						removes.add(chd);
					}
				}
				for (int k = 0; k < removes.size(); k++) {
					Node r = removes.get(k);
					r.getParentNode().removeChild(r);
				}
			}
		}
	}

	public String getXmlName() {
		return _XmlName;
	}

	public void setXmlName(String xmlName) {
		this._XmlName = xmlName;
	}

	public ConfScriptPath getScriptPath() {
		return scriptPath;
	}

	public void setScriptPath(ConfScriptPath scriptPath) {
		this.scriptPath = scriptPath;
	}

	/**
	 * 设置管理员
	 * 
	 * @param admId
	 */
	public void setAdmin(String admId) {
		_AdmId = admId;

	}

	/**
	 * 获取管理员
	 * 
	 * @return
	 */
	public String getAdmin() {
		return _AdmId;
	}

	public String getFrameType() {
		return _FrameType;
	}

	public IConfig getConfigType() {
		return configType;
	}

	public void setConfigType(IConfig configType) {
		this.configType = configType;
	}
}
