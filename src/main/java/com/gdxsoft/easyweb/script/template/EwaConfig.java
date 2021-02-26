package com.gdxsoft.easyweb.script.template;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class EwaConfig implements Cloneable , Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1661299626365114369L;
	private EwaConfigPage _ConfigPage;
	private EwaConfigPage _ConfigAction;
	private EwaConfigPage _ConfigMenu;
	private EwaConfigPage _ConfigPageInfos;
	private EwaConfigPage _ConfigChart;
	private EwaConfigPage _ConfigWorkflow;

	private EwaConfigItems _ConfigItems;
	private EwaConfigFrames _ConfigFrames;

	private static String CFG_NAME = "EwaConfig.xml";
	private Document _XmlDoc;
	private UObjectValue _OV;
	private static EwaConfig CUR_CONFIG;

	/**
	 * 获取config定义对象
	 * 
	 * @return
	 * @throws Exception
	 */
	public static EwaConfig instance() throws Exception {
		String path = UPath.getConfigPath() + CFG_NAME;
		if (CUR_CONFIG != null && !UFileCheck.fileChanged(path)) {
			return CUR_CONFIG;
		} else {
			CUR_CONFIG = new EwaConfig();
			return CUR_CONFIG;
		}
	}

	public EwaConfig() throws Exception {
		_OV = new UObjectValue();
		try {
			initConfig();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 初始化Frame类型定义
	 * 
	 * @throws Exception
	 */
	private void initConfig() throws Exception {
		this._ConfigItems = new EwaConfigItems();
		this._ConfigPage = new EwaConfigPage();
		this._ConfigAction = new EwaConfigPage();
		this._ConfigMenu = new EwaConfigPage();
		this._ConfigPageInfos = new EwaConfigPage();
		this._ConfigChart = new EwaConfigPage();
		this._ConfigWorkflow = new EwaConfigPage();

		String path = UPath.getConfigPath() + CFG_NAME;
		this._XmlDoc = UXml.retDocument(path);

		_ConfigFrames = initFrames();

		this.initItems();// 设置EwaConfigItems信息
		this.initPage();// 设置ConfigPage信息
		this.initAction();// 设置ConfigAction信息
		this.initMenu(); // 设置ConfigMenu信息
		this.initPageInfos(); // PageInfos信息
		this.initChart(); // 设置Chart信息
		this.initWorkflow();
	}

	private void initPage() throws Exception {
		// 设置XItemParameters信息
		String xmlPath = "EasyWebConfig/Page/XItemParameters/XItemParameter";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		XItemParameters paras = this.initXItemParameters(nl);
		this._ConfigPage.setParameters(paras);
	}

	private void initAction() throws Exception {
		// 设置XItemParameters信息
		String xmlPath = "EasyWebConfig/Action/XItemParameters/XItemParameter";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		XItemParameters paras = this.initXItemParameters(nl);
		this._ConfigAction.setParameters(paras);

	}

	private void initMenu() throws Exception {
		// 设置XItemParameters信息
		String xmlPath = "EasyWebConfig/Menu/XItemParameters/XItemParameter";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		XItemParameters paras = this.initXItemParameters(nl);
		this._ConfigMenu.setParameters(paras);

	}

	private void initChart() throws Exception {
		// 设置XItemParameters信息
		String xmlPath = "EasyWebConfig/Chart/XItemParameters/XItemParameter";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		XItemParameters paras = this.initXItemParameters(nl);
		this._ConfigChart.setParameters(paras);

	}

	private void initWorkflow() throws Exception {
		// 设置XItemParameters信息
		String xmlPath = "EasyWebConfig/Workflow/XItemParameters/XItemParameter";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		XItemParameters paras = this.initXItemParameters(nl);
		this._ConfigWorkflow.setParameters(paras);

	}

	private void initPageInfos() throws Exception {
		// 设置XItemParameters信息
		String xmlPath = "EasyWebConfig/PageInfos/XItemParameters/XItemParameter";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		XItemParameters paras = this.initXItemParameters(nl);
		this._ConfigPageInfos.setParameters(paras);

	}

	private void initItems() throws Exception {
		// 设置XItemParameters信息
		String xmlPath = "EasyWebConfig/Items/XItemParameters/XItemParameter";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		XItemParameters paras = this.initXItemParameters(nl);
		this._ConfigItems.setParameters(paras);

		// 设置XItems信息
		xmlPath = "EasyWebConfig/Items/XItems/XItem";
		nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		this._ConfigItems.setItems(this.initXItems(nl));
	}

	private XItems initXItems(NodeList nl) {
		XItems items = new XItems();
		for (int i = 0; i < nl.getLength(); i++) {
			XItem p = this.initXItem(nl.item(i));
			items.addObject(p, p.getName());
		}
		return items;
	}

	private EwaConfigFrames initFrames() {
		String xmlPath = "EasyWebConfig/Frames/Frame";
		NodeList nl = UXml.retNodeList(this._XmlDoc, xmlPath);
		EwaConfigFrames items = new EwaConfigFrames();
		for (int i = 0; i < nl.getLength(); i++) {
			EwaConfigFrame p = new EwaConfigFrame();
			initClass(nl.item(i), p);
			// 设置描述信息
			p.setDescriptions(Descriptions.instanceDescriptions(nl.item(i)));
			items.addObject(p, p.getName());
		}
		return items;
	}

	private XItem initXItem(Node node) {
		XItem p = new XItem();
		// 设置参数信息
		initClass(node, p);
		// 模板信息
		Node nodeTemplate = UXml.retNode(node, "Template");
		Node nodeHtml = UXml.retNode(nodeTemplate, "Html");
		Node nodeRepeat = UXml.retNode(nodeTemplate, "Repeat");

		p.setTemplateHtml(UXml.retNodeText(nodeHtml));
		p.setTemplateRepeat(UXml.retNodeText(nodeRepeat));
		// 设置描述信息
		p.setDescriptions(Descriptions.instanceDescriptions(node));
		return p;
	}

	private XItemParameters initXItemParameters(NodeList nl) throws Exception {
		XItemParameters ps = new XItemParameters();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			XItemParameter p = initXItemParameters(node);
			ps.addObject(p, p.getName());
		}
		// 设置子参数
		for (int i = 0; i < ps.count(); i++) {
			XItemParameter p = ps.getItem(i);
			if (p.getChildrenParameters() == null
					|| p.getChildrenParameters().trim().length() == 0) {
				continue;
			}
			String[] childrenNames = p.getChildrenParameters().split(",");
			for (int m = 0; m < childrenNames.length; m++) {
				String name = childrenNames[m].trim();
				XItemParameter child = ps.getItem(name);
				p.getChildren().put(child.getName(), child);
			}
		}
		return ps;
	}

	private XItemParameter initXItemParameters(Node node) {
		XItemParameter p = new XItemParameter();
		// 设置参数信息
		initClass(node, p);
		// 设置描述信息
		p.setDescriptions(Descriptions.instanceDescriptions(node));
		p.setValues(this.initXItemParameterValues(node));
		return p;
	}

	private XItemParameterValues initXItemParameterValues(Node node) {
		NodeList nl = UXml.retNodeList(node, "Values/Value");
		XItemParameterValues ps = new XItemParameterValues();
		for (int i = 0; i < nl.getLength(); i++) {
			XItemParameterValue p = this.initXItemParameterValue(nl.item(i));
			ps.addObject(p, p.getName());
		}
		return ps;
	}

	private XItemParameterValue initXItemParameterValue(Node node) {
		XItemParameterValue p = new XItemParameterValue();
		// 设置参数信息
		initClass(node, p);
		// 设置描述信息
		p.setDescriptions(Descriptions.instanceDescriptions(node));
//		if (p.getName().equalsIgnoreCase("top")) {
//			int a = 1;
//			a++;
//		}
		return p;
	}

	private void initClass(Node node, Object o) {
		this._OV.setObject(o);
		this._OV.setAllValue((Element) node);
	}

	/**
	 * @return the _ConfigPage
	 */
	public EwaConfigPage getConfigPage() {
		return _ConfigPage;
	}

	/**
	 * @return the _ConfigItems
	 */
	public EwaConfigItems getConfigItems() {
		return _ConfigItems;
	}

	/**
	 * @return the _XmlDoc
	 */
	public Document getXmlDoc() {
		return _XmlDoc;
	}

	/**
	 * @return the _ConfigAction
	 */
	public EwaConfigPage getConfigAction() {
		return _ConfigAction;
	}

	/**
	 * @return the _ConfigMenu
	 */
	public EwaConfigPage getConfigMenu() {
		return _ConfigMenu;
	}

	/**
	 * @return the _ConfigPageInfos
	 */
	public EwaConfigPage getConfigPageInfos() {
		return _ConfigPageInfos;
	}

	/**
	 * @return the _ConfigFrames
	 */
	public EwaConfigFrames getConfigFrames() {
		return _ConfigFrames;
	}

	/**
	 * @return the _ConfigChart
	 */
	public EwaConfigPage getConfigChart() {
		return _ConfigChart;
	}

	/**
	 * @return the _ConfigWorkflow
	 */
	public EwaConfigPage getConfigWorkflow() {
		return _ConfigWorkflow;
	}
}
