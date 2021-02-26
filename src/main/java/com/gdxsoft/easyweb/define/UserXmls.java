package com.gdxsoft.easyweb.define;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfig;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class UserXmls {
	private ArrayList<UserXml> _Xmls;
	private IUpdateXml _UpdateXml;

	public UserXmls() {

	}

	public UserXmls(String xmlName) {
		this._UpdateXml = ConfigUtils.getUpdateXml(xmlName);
	}

	/**
	 * 更新描述
	 * 
	 * @param itemName
	 * @param des
	 */
	public void updateDescription(String itemName, String des) {
		this._UpdateXml.updateDescription(itemName, des);
	}
	
	/**
	 * 批量更新
	 * @param itemNames
	 * @param paraName
	 * @param paraValue
	 */
	public void batchUpdate(String itemNames, String paraName, String paraValue) {
		this._UpdateXml.batchUpdate(itemNames, paraName, paraValue);
	}

	/**
	 * 从数据库中加载
	 * 
	 * @param xmlPath
	 * @throws Exception
	 */
	public void initXmlJdbc(String xmlPath) throws Exception {
		_Xmls = new ArrayList<UserXml>();
		DTTable tb = JdbcConfig.getJdbcItems(xmlPath);
		for (int i = 0; i < tb.getCount(); i++) {
			String xml = tb.getCell(i, "XMLDATA").toString();
			Node node = UXml.asNode(xml);

			UserXml ux = this.getUserXml(node);
			_Xmls.add(ux);
		}
	}

	/**
	 * 创建对象
	 * 
	 * @param n
	 * @return
	 */
	private UserXml getUserXml(Node n) {
		UserXml ux = new UserXml();
		ux.setName(UXml.retNodeValue(n, "Name"));
		ux.setCreateDate(UXml.retNodeValue(n, "CreateDate"));
		ux.setUpdateDate(UXml.retNodeValue(n, "UpdateDate"));

		ux.setDataSource(this.returnTag(n, "DataSource"));
		ux.setSkin(this.returnTag(n, "SkinName"));
		ux.setTag(this.returnTag(n, "FrameTag"));

		ux.setDescription(this.returnTag(n, "DescriptionSet"));
		ux.setAcl(this.returnTag(n, "Acl"));
		ux.setLog(this.returnTag(n, "Log"));
		ux.setXml(UXml.asXml(n));

		return ux;
	}

	/**
	 * 从文件中加载
	 * 
	 * @param xmlPath
	 * @throws Exception
	 */
	public void initXmlFile(String xmlPath) throws Exception {
		Document doc;
		_Xmls = new ArrayList<UserXml>();
		if (xmlPath.indexOf("|") >= 0) {
			xmlPath = UPath.getScriptPath() + "/" + xmlPath.replace("|", "/");
		}

		doc = UXml.retDocument(xmlPath);
		NodeList nl = UXml.retNodeList(doc, "EasyWebTemplates/EasyWebTemplate");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			UserXml ux = this.getUserXml(n);
			_Xmls.add(ux);
		}
	}

	public void InitXml(String xmlPath) {
		if (JdbcConfig.isJdbcResources()) {
			try {
				this.initXmlJdbc(xmlPath);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return;
			}
		} else {
			try {
				this.initXmlFile(xmlPath);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return;
			}
		}

		UserXml[] xmls = new UserXml[_Xmls.size()];
		xmls = _Xmls.toArray(xmls);
		Arrays.sort(xmls, new Comparator<UserXml>() {
			public int compare(UserXml o1, UserXml o2) {
				if (o1.getName().compareToIgnoreCase(o2.getName()) > 0) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		_Xmls.clear();
		for (int i = 0; i < xmls.length; i++) {
			_Xmls.add(xmls[i]);
		}

	}

	private String returnTag(Node node, String tagName) {
		String p = "Page/" + tagName + "/Set/";
		Node n = UXml.retNode(node, p);
		if (tagName.equals("DescriptionSet")) {
			tagName = "Info";
		}
		String s1 = UXml.retNodeValue(n, tagName);
		return s1;
	}

	/**
	 * @return the _Xmls
	 */
	public ArrayList<UserXml> getXmls() {
		return _Xmls;
	}

	public ArrayList<UserXml> getXmls(String xmlPath) {
		this.InitXml(xmlPath);
		return this._Xmls;
	}
}
