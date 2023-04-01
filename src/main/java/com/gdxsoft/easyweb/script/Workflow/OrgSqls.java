package com.gdxsoft.easyweb.script.Workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import com.gdxsoft.easyweb.SystemXmlUtils;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class OrgSqls {
	private static Logger LOGGER = LoggerFactory.getLogger(OrgSqls.class);
	public final static String FILE_NAME = "EwaWorkflow.xml";
	private static OrgSqls INS;
	private static long CODE;

	public static OrgSqls instance() throws Exception {
		if (INS == null) {
			newInstance();
		}
		long time = UPath.getPropTime();
		if (time != CODE) {
			newInstance();
		}
		return INS;
	}

	private static synchronized void newInstance() throws Exception {
		INS = new OrgSqls();
		INS.init();

		CODE = UPath.getPropTime();
	}

	private Map<String, Element> sqls;

	public String getSql(String name) {
		Element ele = this.getItem(name);
		if (ele == null) {
			return null;
		}
		if (ele.hasAttribute("Sql")) {
			return ele.getAttribute("Sql");
		} else if (ele.hasAttribute("sql")) {
			return ele.getAttribute("sql");
		} else {
			return ele.getTextContent().trim();
		}
	}

	public Element getItem(String name) {
		if (name == null || name.trim().length() == 0) {
			return null;
		}
		String n = name.toUpperCase().trim();
		if (this.sqls.containsKey(n)) {
			return this.sqls.get(n);
		} else {
			return null;
		}
	}

	private synchronized void init() throws Exception {
		sqls = new HashMap<String, Element>();

		Document doc = null;
		try {
			// 标准定义的工作流
			doc = SystemXmlUtils.getSystemConfDocument(FILE_NAME);
			this.addDoc(doc);
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw e;
		}

		// ewa_conf.xml中的工作流定义，覆盖标准定义
		String xml = UPath.getWFXML();
		if (StringUtils.isBlank(xml)) {
			return;
		}

		Document doc1 = UXml.asDocument(xml);
		this.addDoc(doc1);
	}

	private void addDoc(Document doc) {
		NodeList nl = doc.getElementsByTagName("sql");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String name = ele.hasAttribute("name") ? ele.getAttribute("name") : ele.getAttribute("Name");
			sqls.put(name.toUpperCase().trim(), ele);
		}
	}

	/**
	 * @return the sqls
	 */
	public Map<String, Element> getSqls() {
		return sqls;
	}

}
