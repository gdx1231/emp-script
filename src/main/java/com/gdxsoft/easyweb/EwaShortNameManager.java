package com.gdxsoft.easyweb;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MTable;

/**
 * 配置文件短名称管理
 * 
 * @author Administrator
 * 
 */
public class EwaShortNameManager {

	private static MTable NAMES = new MTable();
	private static Document DOC;
	private static String PATH = UPath.getCachedPath()
			+ "_ewa_short_names_config.xml";

	public static EwaShortName get(String shortName) {
		if (DOC == null) {
			loadDoc();
		}
		if (NAMES.containsKey(shortName)) {
			return (EwaShortName) NAMES.get(shortName);
		} else {
			return null;
		}
	}

	synchronized private static void loadDoc() {
		File file = new File(PATH);
		if (!file.exists()) {
			createNewXml();
		}
		try {
			DOC = UXml.retDocument(PATH);
			NodeList nl = DOC.getElementsByTagName("sn");

			// 从缓存中加载
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				EwaShortName esn = new EwaShortName();
				esn.setItemName(UXml.retNodeValue(node, "in"));
				esn.setXmlName(UXml.retNodeValue(node, "xn"));
				esn.setShortName(UXml.retNodeValue(node, "id"));

				NAMES.add(esn.getShortName(), esn);
			}
		} catch (Exception e) {
			createNewXml();
		}
	}

	/**
	 * 当文件不存在时，重新生成新文件
	 */
	private static void createNewXml() {
		DOC = UXml.createBlankDocument();
		Element root = DOC.createElement("ewa_short_names");
		DOC.appendChild(root);
		UXml.saveDocument(DOC, PATH);

	}

	/**
	 * 注册短名称
	 * 
	 * @param shortName
	 *            短名称
	 * @param xmlName
	 *            配置文件
	 * @param itemName
	 *            配置项
	 * @return
	 */
	synchronized public static boolean register(String shortName,
			String xmlName, String itemName) {
		if (NAMES.containsKey(shortName)) {
			return false;
		}
		EwaShortName esn = new EwaShortName();
		esn.setItemName(itemName);
		esn.setXmlName(xmlName);
		esn.setShortName(shortName);

		NAMES.put(esn.getShortName(), esn);
		Element ele = DOC.createElement("sn");
		ele.setAttribute("id", esn.getShortName());
		ele.setAttribute("xn", esn.getXmlName());
		ele.setAttribute("in", esn.getItemName());

		DOC.getFirstChild().appendChild(ele);

		UXml.saveDocument(DOC, PATH);

		return true;
	}

}
