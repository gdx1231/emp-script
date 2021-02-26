package com.gdxsoft.easyweb.global;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.cache.ConfigCache;
import com.gdxsoft.easyweb.script.template.Descriptions;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class EwaGlobals {

	public static String FILE_NAME = "EwaGlobal.xml";
	private EwaEvents _EwaEvents;
	private EwaInfos _EwaInfos;
	private EwaValids _EwaValids;
	private EwaSettings _EwaSettings;
	private UObjectValue _OV;

	public EwaGlobals() throws Exception {
		_OV = new UObjectValue();
		this.init();
	}

	public static EwaGlobals instance() throws Exception {
		EwaGlobals o = ConfigCache.getGlobals();
		if (o == null) {
			o = newIntance();
			ConfigCache.setGlobals(o);
		}
		return o;
	}

	private synchronized static EwaGlobals newIntance() throws Exception {
		EwaGlobals o = new EwaGlobals();
		return o;
	}

	private void init() throws Exception {
		String path = UPath.getConfigPath() + FILE_NAME;
		Document doc = UXml.retDocument(path);
		NodeList nl = UXml.retNodeList(doc, "Globals/Global");
		this._EwaSettings = new EwaSettings();
		for (int i = 0; i < nl.getLength(); i++) {
			this.initSetting(nl.item(i));
		}
		this.initEvents(doc);// 设置EWA内置的事件
		this.initInfos(doc);// info信息
		this.initValids(doc); // valids效验信息，用于输入
	}

	private void initSetting(Node node) {
		EwaSetting c = new EwaSetting();
		c.setLang(UXml.retNodeValue(node, "Lang"));
		c.setCurrency(this.getText(node, "Currency"));
		Node nodeCal = UXml.retNode(node, "Calendar");
		c.setDate(this.getText(nodeCal, "Date"));
		c.setTime(this.getText(nodeCal, "Time"));
		c.setToday(this.getText(nodeCal, "Today"));
		String weeks = this.getText(nodeCal, "Weeks");
		String months = this.getText(nodeCal, "Months");

		// 周的显示字符，用于页面日期对话框显示
		c.setWeeks(weeks.split(","));
		// 月的显示字符，用于页面日期对话框显示
		c.setMonths(months.split(","));

		c.setHour(this.getText(node, "Hour"));
		c.setMinute(this.getText(node, "Minute"));
		c.setSecond(this.getText(node, "Second"));
		this._EwaSettings.addObject(c, c.getLang());
	}

	private void initInfos(Document doc) {
		String xmlPath = "Globals/Infos/Info";
		NodeList nl = UXml.retNodeList(doc, xmlPath);
		this._EwaInfos = new EwaInfos();
		for (int i = 0; i < nl.getLength(); i++) {
			EwaInfo p = new EwaInfo();
			this.initClass(nl.item(i), p);
			p.setDescriptions(Descriptions.instanceDescriptions(nl.item(i)));
			this._EwaInfos.addObject(p, p.getName());
		}
	}

	private void initEvents(Document doc) {
		String xmlPath = "Globals/Events/Event";
		NodeList nl = UXml.retNodeList(doc, xmlPath);
		this._EwaEvents = new EwaEvents();
		for (int i = 0; i < nl.getLength(); i++) {
			EwaEvent p = this.initEvent(nl.item(i));
			this._EwaEvents.addObject(p, p.getName());
		}
	}

	private void initValids(Document doc) {
		String xmlPath = "Globals/Valids/Valid";
		NodeList nl = UXml.retNodeList(doc, xmlPath);
		this._EwaValids = new EwaValids();
		for (int i = 0; i < nl.getLength(); i++) {
			EwaValid p = new EwaValid();
			this.initClass(nl.item(i), p);
			p.setDescriptions(Descriptions.instanceDescriptions(nl.item(i)));
			this._EwaValids.addObject(p, p.getName());
		}
	}

	private EwaEvent initEvent(Node node) {
		EwaEvent p = new EwaEvent();
		// 设置参数信息
		initClass(node, p);
		// 设置描述信息
		Node fnode = UXml.retNode(node, "Front");
		Node bnode = UXml.retNode(node, "Back");
		p.setBackDescriptions(Descriptions.instanceDescriptions(bnode));
		p.setFrontDescriptions(Descriptions.instanceDescriptions(fnode));
		return p;
	}

	private void initClass(Node node, Object o) {
		this._OV.setObject(o);
		this._OV.setAllValue((Element) node);
	}

	private String getText(Node node, String tagName) {
		Node n = UXml.retNode(node, tagName);
		return UXml.retNodeText(n);
	}

	/**
	 * @return the _EwaInfos
	 */
	public EwaInfos getEwaInfos() {
		return _EwaInfos;
	}

	/**
	 * @return the _EwaEvents
	 */
	public EwaEvents getEwaEvents() {
		return _EwaEvents;
	}

	/**
	 * @return the _EwaValids
	 */
	public EwaValids getEwaValids() {
		return _EwaValids;
	}

	public String createJs(String lang) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(this._EwaEvents.createJs(lang));
		sb.append(this._EwaInfos.createJs(lang));
		sb.append(this._EwaValids.createJs(lang));
		sb.append(this._EwaSettings.createJs(lang));

		String js = EwaConfig.instance().getConfigItems().getParameters().createJsAlert(lang);
		sb.append(js);
		return sb.toString();
	}

	/**
	 * @return the _EwaSettings
	 */
	public EwaSettings getEwaSettings() {
		return _EwaSettings;
	}

}
