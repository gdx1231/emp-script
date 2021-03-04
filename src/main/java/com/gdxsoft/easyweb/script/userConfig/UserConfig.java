package com.gdxsoft.easyweb.script.userConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.cache.ConfigCache;
import com.gdxsoft.easyweb.cache.ConfigCacheWidthSqlCached; //有问题
import com.gdxsoft.easyweb.cache.ConfigStatus;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.XItemParameter;
import com.gdxsoft.easyweb.script.template.XItemParameterValue;
import com.gdxsoft.easyweb.utils.*;

/**
 * 用户配置文件定义
 * 
 * @author Administrator
 * 
 */
public class UserConfig implements Serializable {
	private static Logger LOGGER = LoggerFactory.getLogger(UserConfig.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1872059554611340437L;

	/**
	 * 检查配置信息检查的间隔（秒）
	 */
	public static int CHECK_CHANG_SPAN_SECONDS = 5;

	private DebugFrames _DebugFrames;
	private UserXItems _UserXItems; // EasyWebTemplates/EasyWebTemplate/Xitems
	private UserXItem _UserPageItem; // EasyWebTemplates/EasyWebTemplate/Page
	private UserXItem _UserActionItem; // EasyWebTemplates/EasyWebTemplate/Action
	private UserXItems _UserMenuItems; // EasyWebTemplates/EasyWebTemplate/Menu
	private UserXItems _UserPageInfos; // EasyWebTemplates/EasyWebTemplate/PageInfos
	private UserXItems _UserCharts; // EasyWebTemplates/EasyWebTemplate/Charts
	private UserXItems _UserWorkflows; // EasyWebTemplates/EasyWebTemplate/Workflows

	private Document _XmlDoc;
	private String _XmlName;
	private String _ItemName;
	private Node _ItemNode;

	private String _ItemNodeXml;
	private String _JS_XML; // 配置文件的对象的 JS表达式

	public static UserConfig instance(String xmlFileName, String itemName, DebugFrames debugFrames) throws Exception {
		if (xmlFileName.indexOf("../") >= 0 || xmlFileName.indexOf("..|") >= 0 || xmlFileName.indexOf("..\\") >= 0) {
			throw new Exception("非法的参数'../'");
		}

		boolean isJdbcCall = JdbcConfig.isJdbcResources();

		// 处理 xmlName，去除多余的字符
		String xmlName = isJdbcCall ? UserConfig.filterXmlNameByJdbc(xmlFileName)
				: UserConfig.filterXmlName(xmlFileName);

		UserConfig o;
		if (UPath.getCfgCacheMethod() != null && UPath.getCfgCacheMethod().equals("sqlcached")) {
			o = ConfigCacheWidthSqlCached.getUserConfig(xmlName, itemName);
			if (o == null) {
				o = new UserConfig(xmlName, itemName);
				o.setDebugFrames(debugFrames);
				o.loadUserDefined();
				o.setDebugFrames(null);
				ConfigCacheWidthSqlCached.setUserConfig(xmlName, itemName, o);
			}
		} else { // 利用内存模式
			o = ConfigCache.getUserConfig(xmlName, itemName);
			if (o == null) {
				o = new UserConfig(xmlName, itemName);
				o.setDebugFrames(debugFrames);
				o.loadUserDefined();
				o.setDebugFrames(null);
				ConfigCache.setUserConfig(xmlName, itemName, o);
			}
		}

		return o;
	}

	/**
	 * 从序列化二进制中获取
	 * 
	 * @param buf
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static UserConfig fromSerialize(byte[] buf) throws IOException, ClassNotFoundException {
		// Serialize

		ByteArrayInputStream fis = new ByteArrayInputStream(buf);
		ObjectInputStream ois = new ObjectInputStream(fis);
		UserConfig tb = (UserConfig) ois.readObject();
		ois.close();
		fis.close();
		return tb;
	}

	/**
	 * 获取配置文件状态
	 * 
	 * @param xmlFileName
	 * @return
	 */
	public static ConfigStatus getXmlConfigPath(String xmlFileName, String itemName) {
		ConfigStatus configStatus;
		if (JdbcConfig.isJdbcResources()) {
			configStatus = new ConfigStatus();

			String xmlName = UserConfig.filterXmlNameByJdbc(xmlFileName);
			configStatus.setAbsolutePath(xmlName);

			if (itemName == null) {
				configStatus.setLength(0);
				configStatus.setLastModified(new Date().getTime());
			} else {
//				StringBuilder sb = new StringBuilder();
//				sb.append("select HASH_CODE, MD5, UPDATE_DATE  from EWA_CFG where xmlname='");
//				sb.append(xmlName.replace("'", "''"));
//				sb.append("' and itemname='");
//				// 2019-01-24 删除
//				// sb.append(itemName.replace("'", "''"));
//				sb.append("'");
//				String sql = sb.toString();

				DTTable tb = JdbcConfig.getXmlMeta(xmlName);

				if (tb.getCount() == 0) {
					LOGGER.error("Not found configure " + xmlName);
					return null;
				}

				int haseCode = tb.getCell(0, 0).toInt();
				configStatus.setLength(haseCode);

				if (tb.getCell(0, 1).isNull()) {
					configStatus.setLastModified(0);
				} else {
					configStatus.setLastModified(tb.getCell(0, 1).toTime());
				}
				try {
					configStatus.setMd5(tb.getCell(0, "md5").toString());
				} catch (Exception e) {
					LOGGER.warn(e.getLocalizedMessage());
				}
				LOGGER.debug("SQL:" + xmlName + "/" + itemName + "," + configStatus.length() + ","
						+ configStatus.lastModified());
			}
		} else {
			String path = UPath.getScriptPath() + xmlFileName.replace("|", "/");
			java.io.File f = new java.io.File(path);
			if (!f.exists()) {
				path = f.getAbsolutePath();
				path = path + ".bin";
				f = new java.io.File(path);
			}
			configStatus = new ConfigStatus(f);
		}
		return configStatus;
	}

	public UserConfig() {
	}

	public UserConfig(String xmlFileName, String itemName) throws Exception {
		this._XmlName = xmlFileName.trim();
		this._ItemName = itemName.trim();
	}

	/**
	 * 处理 xmlName 中的特殊字符
	 * 
	 * @param xmlName
	 * @return
	 */
	public static String filterXmlName(String xmlName) {
		String xmlFileName = xmlName.trim();
		xmlFileName = xmlFileName.replace("%25", "%");
		xmlFileName = xmlFileName.replace("%7c", "/");
		xmlFileName = xmlFileName.replace("%7C", "/");
		// 去除危险的 ../符号
		xmlFileName = xmlFileName.replace("|", "/").replace("../", "");

		return xmlFileName;
	}

	/**
	 * 处理 xmlName 中的特殊字符, 用于数据库调用
	 * 
	 * @param xmlName
	 * @return
	 */
	public static String filterXmlNameByJdbc(String xmlName) {
		String xmlFileName = filterXmlName(xmlName);
		xmlFileName = xmlFileName.replace("/", "|").replace("\\", "|");
		while (xmlFileName.indexOf("||") >= 0) {
			xmlFileName = xmlFileName.replace("||", "|");
		}
		if (!xmlFileName.startsWith("|")) {
			xmlFileName = "|" + xmlFileName;
		}
		return xmlFileName;
	}

	/**
	 * 加载xml文件
	 * 
	 * @param xmlFileName
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void loadXmlFile(String xmlFileName) throws ParserConfigurationException, SAXException, IOException {
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "配置", "开始加载配置文件(" + this._XmlName + ")");
		}

		this._XmlName = filterXmlName(xmlFileName);

		String path = UPath.getScriptPath() + this._XmlName;
		try {
			_XmlDoc = UXml.retDocument(path);
		} catch (ParserConfigurationException e) {
			System.out.println(xmlFileName + "," + _ItemName + ":" + e.getMessage());
		} catch (SAXException e) {
			System.out.println(xmlFileName + "," + _ItemName + ":" + e.getMessage());
		}
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "配置", "结束加载配置文件");
		}
	}

	/**
	 * 在xml文件中查找 itemName
	 * 
	 * @param itemName
	 * @throws Exception
	 */
	private void loadItem(String itemName) throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配置项(" + this._ItemName + ")");
		this._ItemName = itemName.trim();
		NodeList nl = UXml.retNodeList(this._XmlDoc, "EasyWebTemplates/EasyWebTemplate");
		for (int i = 0; i < nl.getLength(); i++) {
			String name = UXml.retNodeValue(nl.item(i), "Name").trim();
			if (name.equalsIgnoreCase(this._ItemName)) {
				this._ItemNode = nl.item(i);
				return;
			}
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配置项");
		throw new Exception(this._ItemName + "未发现在" + this._XmlName + "中");
	}

	/**
	 * 通过数据库调用配置信息
	 * 
	 * @param dataSourceName
	 */
	private void loadItemByJdbc(String dataSourceName) throws Exception {
		if (this._DebugFrames != null) {
			this._DebugFrames.addDebug(this, "配置", "JDBC 开始加载配置项(" + this._XmlName + "," + this._ItemName + ")");
		}

		this._XmlName = filterXmlNameByJdbc(this._XmlName);

		String xmlStr = JdbcConfig.getJdbcItemXml(_XmlName, _ItemName);

		if (xmlStr == null) {
			throw new Exception("jdbc: " + this._ItemName + "未发现在" + this._XmlName + "中");
		}

		this._ItemNode = UXml.asNode(xmlStr);

		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配置项");
	}

	public void loadUserDefined() throws Exception {
		if (JdbcConfig.isJdbcResources()) { // 通过数据库调用
			String dataSourceName = JdbcConfig.getJdbcConfigName();
			this.loadItemByJdbc(dataSourceName);
		} else {
			this.loadXmlFile(this._XmlName);
			this.loadItem(this._ItemName);
		}
		this.initXItems();
		this.initPage();
		this.initAction();
		initMenus();
		this.initPageInfos();
		this.initCharts();
		this.initWorkflows();

		_ItemNodeXml = UXml.asXmlAll(this._ItemNode);
		this._ItemNode = null;
		this._XmlDoc = null;
	}

	public String getItemNodeXml() {
		return _ItemNodeXml;
	}

	/**
	 * 初始化用户配置中的Page下的所有信息
	 * 
	 * @throws Exception
	 */
	private void initAction() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配Action");
		_UserActionItem = new UserXItem();
		Node pageNode = UXml.retNode(this._ItemNode, "Action");
		if (pageNode == null)
			return;
		int len = pageNode.getChildNodes().getLength();
		for (int i = 0; i < len; i++) {
			Node childNode = pageNode.getChildNodes().item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			// String s1 = UXml.asXml(childNode);
			// System.out.println(s1);
			/*
			 * if (s1.indexOf("Set") >= 0) { s1 += ""; }
			 */
			UserXItemValues uxvs = this.initUserXItemValues(childNode, "action");
			this._UserActionItem.addObject(uxvs, uxvs.getName());
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配Action. (" + len + ")");

	}

	/**
	 * 初始化用户配置中的Page下的所有信息
	 * 
	 * @throws Exception
	 */
	private void initPage() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配Page");

		_UserPageItem = new UserXItem();
		Node pageNode = UXml.retNode(this._ItemNode, "Page");
		int len = pageNode.getChildNodes().getLength();
		for (int i = 0; i < len; i++) {
			Node childNode = pageNode.getChildNodes().item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			// String s1 = UXml.asXml(childNode);
			// System.out.println(s1);
			// if (s1.indexOf("IsSplit") >= 0) {
			// s1 += "";
			// }
			UserXItemValues uxvs = this.initUserXItemValues(childNode, "page");
			if (uxvs != null)
				this._UserPageItem.addObject(uxvs, uxvs.getName());
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配Page");
	}

	/**
	 * 初始化用户配置中的XItems/XItem所有信息
	 * 
	 * @throws Exception
	 */
	private void initXItems() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 XItems. ");
		this._UserXItems = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "XItems/XItem");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "xitem");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 XItems. (" + nl.getLength() + ")");
	}

	/**
	 * 初始化用户配置中的Menus/Menu所有信息
	 * 
	 * @throws Exception
	 */
	private void initMenus() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 Menus. ");
		this._UserMenuItems = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "Menus/Menu");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "menu");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 Menus. (" + nl.getLength() + ")");
	}

	private void initWorkflows() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 Workflows. ");
		this._UserWorkflows = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "Workflows/Workflow");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "workflow");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 Workflows. (" + nl.getLength() + ")");
	}

	private void initPageInfos() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 PageInfos. ");
		this._UserPageInfos = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "PageInfos/PageInfo");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "pageinfo");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 PageInfos. (" + nl.getLength() + ")");
	}

	private void initCharts() throws Exception {
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "开始加载配 Charts. ");
		this._UserCharts = new UserXItems();
		NodeList nl = UXml.retNodeList(this._ItemNode, "Charts/Chart");
		if (nl == null) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			this.initXItem(nl.item(i), "chart");
		}
		if (this._DebugFrames != null)
			this._DebugFrames.addDebug(this, "配置", "结束加载配 Charts. (" + nl.getLength() + ")");
	}

	/**
	 * 初始化用户配置中的XItems/XItem中当前XItem信息
	 * 
	 * @param node
	 * @throws Exception
	 */
	private void initXItem(Node node, String type) throws Exception {
		String nodeXml = UXml.asXml(node);
		UserXItem ui = new UserXItem();
		ui.setName(UXml.retNodeValue(node, "Name"));
		if (type.equals("chart")) {
			this._UserCharts.addObject(ui, ui.getName());
		} else if (type.equals("xitem")) {
			this._UserXItems.addObject(ui, ui.getName());
		} else if (type.equals("menu")) {
			this._UserMenuItems.addObject(ui, ui.getName());
		} else if (type.equals("workflow")) {
			this._UserWorkflows.addObject(ui, ui.getName());
		} else {
			this._UserPageInfos.addObject(ui, ui.getName());
		}
		ui.setXml(nodeXml);
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node childNode = nl.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			// System.out.println(UXml.asXml(childNode));

			UserXItemValues uvs = this.initUserXItemValues(childNode, type);
			if (uvs != null) {
				ui.addObject(uvs, uvs.getName());
			}
		}
	}

	/**
	 * 获取用户配置文件中所用的字段定义参数值<font color=blue><br>
	 * &lt;XItem&gt;<br>
	 * &lt;Tag Tag="text" /&gt;<br>
	 * &lt;Name Name="test_name" /&gt;<br>
	 * .......<br>
	 * &lt;/XItem&gt;</font>
	 * 
	 * @param node
	 * @return UserXItemValues
	 * @throws Exception
	 */
	private UserXItemValues initUserXItemValues(Node node, String type) throws Exception {
		XItemParameter itemPara = this.getItemParameter(node, type);
		if (itemPara == null) {
			return null;
		}
		UserXItemValues uxvs = new UserXItemValues();
		uxvs.setParameter(itemPara);
		uxvs.setXml(UXml.asXml(node));
		/*
		 * String aaa=UXml.asXml(node); if(aaa.indexOf("DescriptionSet")>=0){ int a=1;
		 * a++; }
		 */
		/*
		 * 集合参数，例如: <DescriptionSet> <Set Lang="zh-cn" Value="" Memo="用户名" /> <Set
		 * Lang="en-us" Value="" Memo="User Name" /> </DescriptionSet>
		 */
		NodeList childList = node.getChildNodes();
		// int m = 0;
		for (int i = 0; i < childList.getLength(); i++) {
			Node childNode = childList.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equalsIgnoreCase("set")) {
				UserXItemValue uxv = initUserXItemValue(childNode, itemPara, type);
				uxvs.addObject(uxv, uxv.getUniqueName());
				// m++;
			}
		}

		return uxvs;
	}

	/**
	 * 获取用户配置文件中字段的参数值, <br>
	 * 例如 &lt;MaxMinLength MaxLength="20" MinLength="8" /&gt;
	 * 
	 * @param node     Xml节点
	 * @param itemPara XItemParameter的定义
	 * @param uxvs
	 * @throws Exception
	 */
	private UserXItemValue initUserXItemValue(Node node, XItemParameter itemPara, String type) throws Exception {
		UserXItemValue uxv = new UserXItemValue();
		uxv.setXml(UXml.asXml(node));
		// EwaConfig中XItemParameter定义的Value
		for (int m = 0; m < itemPara.getValues().count(); m++) {
			XItemParameterValue v = itemPara.getValues().getItem(m);
			String name = v.getName();
			String val;
			if (v.isCDATA()) {
				Node cdataNode = UXml.retNode(node, name);
				if (cdataNode != null) {
					val = UXml.retNodeText(cdataNode);
				} else {
					val = UXml.retNodeText(node);
				}
			} else {
				val = UXml.retNodeValue(node, name);
			}
			uxv.addObject(val, name);
			if (v.isUnique()) {
				uxv.setUniqueName(val);
			}
		}
		if (itemPara.getChildren().size() > 0) {
			Iterator<String> it = itemPara.getChildren().keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				XItemParameter p = itemPara.getChildren().get(key);
				Node nodeChild = UXml.retNode(node, p.getName());
				if (nodeChild == null) {
					continue;
				}
				UserXItemValues o = this.initUserXItemValues(nodeChild, type);
				uxv.getChildren().put(o.getName(), o);
			}
		}
		return uxv;
	}

	/**
	 * 获取用户配置信息中对应的 XItemParameter定义(EwaConfig.xml)
	 * 
	 * @param node 当前节点
	 * @return
	 * @throws Exception
	 */
	private XItemParameter getItemParameter(Node node, String type) throws Exception {
		String nodeName = node.getNodeName();
		XItemParameter para = null;
		EwaConfig ec = EwaConfig.instance();
		try {
			if (type.equals("page")) {
				para = ec.getConfigPage().getParameters().getItem(nodeName);
			} else if (type.equals("xitem")) {
				para = ec.getConfigItems().getParameters().getItem(nodeName);
			} else if (type.equals("action")) {
				para = ec.getConfigAction().getParameters().getItem(nodeName);
			} else if (type.equals("menu")) {
				para = ec.getConfigMenu().getParameters().getItem(nodeName);
			} else if (type.equals("pageinfo")) {
				para = ec.getConfigMenu().getParameters().getItem(nodeName);
			} else if (type.equals("chart")) {
				para = ec.getConfigChart().getParameters().getItem(nodeName);
			} else if (type.equals("workflow")) {
				para = ec.getConfigWorkflow().getParameters().getItem(nodeName);
			}
		} catch (Exception e) {
			return null;
		}
		return para;
	}

	/**
	 * 序列化表
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] toSerialize() throws IOException {
		// Serialize
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(this);
		oos.close();

		byte[] buf = fos.toByteArray();
		fos.close();

		return buf;
	}

	/**
	 * 获取用户配置信息
	 * 
	 * @return the _Values
	 */
	public UserXItems getUserXItems() {
		return _UserXItems;
	}

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @param xmlName the _XmlName to set
	 */
	public void setXmlName(String xmlName) {
		_XmlName = xmlName;
	}

	/**
	 * @return the _ItemName
	 */
	public String getItemName() {
		return _ItemName;
	}

	/**
	 * @param itemName the _ItemName to set
	 */
	public void setItemName(String itemName) {
		_ItemName = itemName;
	}

	/**
	 * @return the _UserActionItem
	 */
	public UserXItem getUserActionItem() {
		return _UserActionItem;
	}

	/**
	 * @return the _UserPageItem
	 */
	public UserXItem getUserPageItem() {
		return _UserPageItem;
	}

	/**
	 * @return the _UserMenuItems
	 */
	public UserXItems getUserMenuItems() {
		return _UserMenuItems;
	}

	/**
	 * @return the _UserPageInfos
	 */
	public UserXItems getUserPageInfos() {
		return _UserPageInfos;
	}

	/**
	 * 获取工作流定义
	 * 
	 * @return
	 */
	public UserXItems getUserWorkflows() {
		return this._UserWorkflows;
	}

	public DebugFrames getDebugFrames() {
		return _DebugFrames;
	}

	public void setDebugFrames(DebugFrames debugFrames) {
		_DebugFrames = debugFrames;
	}

	/**
	 * @return the _UserCharts
	 */
	public UserXItems getUserCharts() {
		return _UserCharts;
	}

	/**
	 * @return the _JS_XML
	 */
	public String getJS_XML() {
		return _JS_XML;
	}

	/**
	 * @param _js_xml the _JS_XML to set
	 */
	public void setJS_XML(String _js_xml) {
		_JS_XML = _js_xml;
	}

}