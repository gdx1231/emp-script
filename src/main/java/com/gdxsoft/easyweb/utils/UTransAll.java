/**
 * 
 */
package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.datasource.SqlPart;

/**
 * @author admin
 * 
 */
public class UTransAll {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MAPS = new HashMap<String, String>();
		MAPS.put("团", "Group");
		MAPS.put("团号", "Group Code");
		MAPS.put("编号", "No");
		MAPS.put("人数", "Number of People");

		/*
		 * try{ transDess("/Volumes/user.config.xml/test/wf.xml"); } catch(Exception
		 * err){ System.out.println(err.getMessage()); }
		 */
		// searchTagDir1("/Volumes/user.config.xml");
		search$Dir1("/Volumes/user.config.xml");

	}

	private static void search$Dir1(String name) {
		File f = new File(name);
		File[] fs = f.listFiles();
		for (int i = 0; i < fs.length; i++) {
			File f1 = fs[i];
			if (f1.isDirectory()) {
				search$Dir1(f1.getAbsolutePath());
			} else {
				if (f1.getAbsolutePath().endsWith(".xml")) {
					try {
						System.out.println("\nSTART:" + f1.getAbsolutePath());
						tag$(f1.getAbsolutePath());
						System.out.println("\nEND:" + f1.getAbsolutePath());
					} catch (Exception err) {
						System.out.println(err.getMessage());
					}
				}
			}
		}
	}

	private static void tag$(String xmlname) throws ParserConfigurationException, SAXException, IOException {
		Document doc = UXml.retDocument(xmlname);
		NodeList nl = doc.getElementsByTagName("AddScript");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			Element ele = (Element) node;
			NodeList nlTops = ele.getElementsByTagName("Top");
			if (nlTops.getLength() == 1) {
				Node top = nlTops.item(0);
				tag$(top);
			}
			NodeList nlBottoms = ele.getElementsByTagName("Bottom");
			if (nlBottoms.getLength() == 1) {
				Node bottom = nlBottoms.item(0);
				tag$(bottom);
			}
		}

		String xml = UXml.asXmlAll(doc);
		UFile.createNewTextFile(xmlname, xml);
	}

	private static void tag$(Node node) {
		for (int m = 0; m < node.getChildNodes().getLength(); m++) {
			Node cdata = node.getChildNodes().item(0);
			if (cdata.getNodeName().equals("#cdata-section")) {
				String txt = cdata.getTextContent();
				// System.out.println(txt);
				if (txt.indexOf("$(") > 0) {
					String txt1 = txt.replace("$(", "$X(");
					cdata.setTextContent(txt1);
				}
			}

		}
	}

	private static void searchTagDir1(String name) {
		File f = new File(name);
		File[] fs = f.listFiles();
		for (int i = 0; i < fs.length; i++) {
			File f1 = fs[i];
			if (f1.isDirectory()) {
				searchTagDir1(f1.getAbsolutePath());
			} else {
				if (f1.getAbsolutePath().endsWith(".xml")) {
					try {
						System.out.println("\nSTART:" + f1.getAbsolutePath());
						tagSql(f1.getAbsolutePath());
						System.out.println("\nEND:" + f1.getAbsolutePath());
					} catch (Exception err) {
						System.out.println(err.getMessage());
					}
				}
			}
		}
	}

	private static void tagSql(String xmlname) throws ParserConfigurationException, SAXException, IOException {
		Document doc = UXml.retDocument(xmlname);
		NodeList nl = doc.getElementsByTagName("OrderSearch");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			Element ele = (Element) node;

			NodeList sets = ele.getElementsByTagName("Set");
			if (sets.getLength() == 0) {
				continue;
			}
			handleTagSets(sets.item(0));
		}

		NodeList nl2 = doc.getElementsByTagName("List");
		for (int i = 0; i < nl2.getLength(); i++) {
			Node node = nl2.item(i);
			Element ele = (Element) node;

			NodeList sets = ele.getElementsByTagName("Set");
			if (sets.getLength() == 0) {
				continue;
			}
			handleTagSets(sets.item(0));
		}
		String xml = UXml.asXmlAll(doc);
		UFile.createNewTextFile(xmlname, xml);
	}

	private static void handleTagSets(Node nodeTag) {

		Element ele = (Element) nodeTag;
		String SearchSql;
		String tagName = nodeTag.getParentNode().getNodeName();
		if (tagName.equals("OrderSearch")) {
			SearchSql = ele.getAttribute("SearchSql");
		} else {
			SearchSql = ele.getAttribute("Sql");
		}
		if (SearchSql == null || SearchSql.trim().length() == 0) {
			return;
		}
		System.out.println(SearchSql);
		String s1 = SearchSql.trim().toUpperCase();

		SqlPart sp = new SqlPart();
		try {
			sp.setSql(s1);
		} catch (Exception err) {
			System.out.println("--- " + err + " ----\n");
			return;
		}
		String tbName = sp.getTableName().trim();

		if (!tbName.equalsIgnoreCase("BAS_TAG")) {
			System.out.println("--- " + tbName + " ----\n");
			return;
		}
		String fields = sp.getFields();
		String[] fs = fields.replace(" ", "").split(",");

		HashMap<String, Boolean> maps = new HashMap<String, Boolean>();
		for (int i = 0; i < fs.length; i++) {
			String f = fs[i].trim().toUpperCase();
			maps.put(f, true);
		}
		if (maps.containsKey("BAS_TAG_NAME") && !maps.containsKey("BAS_TAG_NAME_EN")) {
			String sql1 = "SELECT " + sp.getFields() + ", BAS_TAG_NAME_EN FROM BAS_TAG WHERE " + sp.getWhere();
			if (sp.getOrderBy() != null && sp.getOrderBy().trim().length() > 0) {
				sql1 += " ORDER BY " + sp.getOrderBy();
			}
			System.out.println(tagName + ": [" + sql1 + "]\n");
			if (tagName.equals("OrderSearch")) {
				ele.setAttribute("SearchSql", sql1);
			} else {
				ele.setAttribute("Sql", sql1);
			}
		}
	}

	private static void searchDir(String name) {

		File f = new File(name);
		File[] fs = f.listFiles();
		for (int i = 0; i < fs.length; i++) {
			File f1 = fs[i];
			if (f1.isDirectory()) {
				searchDir(f1.getAbsolutePath());
			} else {
				if (f1.getAbsolutePath().endsWith(".xml")) {
					try {
						System.out.println("START:" + f1.getAbsolutePath());
						transDess(f1.getAbsolutePath());
						System.out.println("END:" + f1.getAbsolutePath());
					} catch (Exception err) {
						System.out.println(err.getMessage());
					}
				}
			}
		}
	}

	private static void transDess(String xmlname) throws ParserConfigurationException, SAXException, IOException {
		Document doc = UXml.retDocument(xmlname);
		NodeList nl = doc.getElementsByTagName("DescriptionSet");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			Element ele = (Element) node;

			NodeList sets = ele.getElementsByTagName("Set");
			if (sets.getLength() == 0) {
				continue;
			}
			handleSets(sets);
		}
		String xml = UXml.asXmlAll(doc);
		UFile.createNewTextFile(xmlname, xml);
	}

	private static void handleSets(NodeList sets) {
		Node cn = null;
		Node en = null;
		for (int i = 0; i < sets.getLength(); i++) {
			Node n = sets.item(i);
			Element ele = (Element) n;
			String lang = ele.getAttribute("Lang");
			if (lang.equals("zhcn")) {
				cn = ele;
			} else if (lang.equals("enus")) {
				en = ele;
			}
		}
		if (cn == null) {
			return;
		}
		if (en == null) {
			en = cn.getOwnerDocument().createElement("Set");
			cn.getParentNode().appendChild(en);
			Element eleEn = (Element) en;
			eleEn.setAttribute("Lang", "enus");
		}

		Element eleCN = (Element) cn;
		Element eleEN = (Element) en;

		String infoCn = eleCN.getAttribute("Info");
		String memoCn = eleCN.getAttribute("Memo");

		String infoEn = eleEN.getAttribute("Info");
		String memoEn = eleEN.getAttribute("Memo");

		if (infoEn == null || infoEn.trim().length() == 0) {
			String v = trans(infoCn);
			// System.out.println("T:" + infoCn + " -> " + v);

			eleEN.setAttribute("Info", v);

			// System.out.println(UXml.asXml(eleEN));
		}
		if (memoEn == null || memoEn.trim().length() == 0) {
			String v = trans(memoCn);
			// System.out.println("T:" + memoCn + " -> " + v);
			eleEN.setAttribute("Memo", v);
		}
	}

	private static String trans(String str) {
		str = str.trim();
		if (str.length() == 0) {
			return "";
		}
		if (MAPS.containsKey(str)) {
			return MAPS.get(str);
		}
		UNet client = new UNet();
		client.setIsShowLog(false);
		try {
			String url = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate?appid=50F7C8D4BC00A6E047C046D012F334DEC61FA003&oncomplete=A&from=zh-CHS&to=en&text=";
			url += URLEncoder.encode(str, "UTF-8");

			String s = client.doGet(url).trim();
			s = s.substring(4, s.length() - 3);
			MAPS.put(str, s);
			return s;
		} catch (Exception err) {
			return err.getMessage();
		}
	}

	private static HashMap<String, String> MAPS;
}
