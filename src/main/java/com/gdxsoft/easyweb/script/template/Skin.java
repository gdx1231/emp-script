package com.gdxsoft.easyweb.script.template;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class Skin implements Serializable {
	private static Logger LOGGER = LoggerFactory.getLogger(Skin.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 5005393509202838654L;
	private String _Head;
	private String _HeadXHtml;
	private String _HeadH5; // h5d的头
	private String _HeadMobile; // 手机的头
	private String _HeadVue; // vue的头
	private String _Style;
	private String _Script;
	private String _Title;
	private String _BodyStart;
	private String _BodyEnd;
	private String _BodyStartMobile;
	private String _BodyEndMobile;
	private String _Name;

	private HashMap<String, SkinFrames> _SkinFrames = new HashMap<String, SkinFrames>();

	private static Skin SKIN;

	public static Skin instance() throws Exception {
		String path = UPath.getConfigPath() + "EwaSkin.xml";

		// 30s 检查一次是否变化
		if (SKIN != null && !UFileCheck.fileChanged(path, 30)) {
			return SKIN;
		} else {
			SKIN = syncInitSkin();
			return SKIN;
		}
	}

	/**
	 * 同步锁定并创建Skin
	 * 
	 * @return
	 * @throws Exception
	 */
	private static synchronized Skin syncInitSkin() throws Exception {
		try {
			Skin skin = new Skin();
			return skin;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw e;
		}

	}

	private Skin() throws Exception {
		initSkin();
	}

	private void initSkin() throws ParserConfigurationException, SAXException, IOException {
		String path = UPath.getConfigPath() + "EwaSkin.xml";
		LOGGER.info(path);

		Document doc = UXml.retDocument(path);

		Node mainSkinNode = UXml.retNodeList(doc, "SkinRoot/MainSkin").item(0);
		this.initMainSkin(mainSkinNode);

		NodeList nl = UXml.retNodeList(doc, "SkinRoot/Skin");
		for (int i = 0; i < nl.getLength(); i++) {
			initSkinFrames(nl.item(i));
		}
	}

	private void initSkinFrames(Node node) {
		SkinFrames skinFrames = new SkinFrames();
		skinFrames.setName(UXml.retNodeValue(node, "Name").toUpperCase().trim());
		NodeList nl = UXml.retNodeList(node, "FrameItem");
		for (int i = 0; i < nl.getLength(); i++) {
			SkinFrame sf = this.initSkinFrame(nl.item(i));
			skinFrames.addObject(sf, sf.getFrameType());
		}
		this._SkinFrames.put(skinFrames.getName(), skinFrames);
	}

	private SkinFrame initSkinFrame(Node node) {
		SkinFrame sf = new SkinFrame();
		sf.setFrameType(UXml.retNodeValue(node, "FrameType").toUpperCase().trim());
		sf.setBodyStart(this.getChildNodeText(node, "BodyStart"));
		sf.setBodyEnd(this.getChildNodeText(node, "BodyEnd"));
		sf.setTop(this.getChildNodeText(node, "Top"));
		sf.setBottom(this.getChildNodeText(node, "Bottom"));
		sf.setItem(this.getChildNodeText(node, "Item"));
		sf.setItemButton(this.getChildNodeText(node, "ItemButton"));
		sf.setItemHeader(this.getChildNodeText(node, "ItemHeader"));
		sf.setScript(this.getChildNodeText(node, "Script"));
		sf.setStyle(this.getChildNodeText(node, "Style"));
		Node footNode = UXml.retNode(node, "ItemFooter/FooterHtml");
		if (footNode != null) {
			Descriptions des = Descriptions.instanceDescriptions(footNode);
			sf.setItemFooter(des);
		}
		// 初始化ListFrame的分页描述
		initSkinSplitPageButtons(node, sf);
		return sf;
	}

	private void initSkinSplitPageButtons(Node node, SkinFrame skinFrame) {
		skinFrame.setPageFirst(initSkinSplitPageButton(node, "First"));
		skinFrame.setPageNext(initSkinSplitPageButton(node, "Next"));
		skinFrame.setPagePrev(initSkinSplitPageButton(node, "Prev"));
		skinFrame.setPageLast(initSkinSplitPageButton(node, "Last"));
	}

	private SkinFrameLang initSkinSplitPageButton(Node node, String tagName) {
		Node nodeFirst = UXml.retNode(node, tagName);
		if (nodeFirst == null) {
			return null;
		}
		SkinFrameLang skinFrameLang = new SkinFrameLang();
		skinFrameLang.setName(tagName);
		Descriptions ds = Descriptions.instanceDescriptions(nodeFirst);
		skinFrameLang.setDescriptions(ds);
		return skinFrameLang;
	}

	/**
	 * 初始化皮肤主参数
	 * 
	 * @param node
	 */
	private void initMainSkin(Node node) {
		this._Head = getChildNodeText(node, "Head");
		this._HeadXHtml = getChildNodeText(node, "HeadXHtml");
		this._HeadH5 = getChildNodeText(node, "HeadH5");
		this._HeadMobile = getChildNodeText(node, "HeadMobile");
		this._HeadVue = getChildNodeText(node, "HeadVue");
		
		this._Style = getChildNodeText(node, "Style");
		this._Script = getChildNodeText(node, "Script");
		this._BodyStart = getChildNodeText(node, "BodyStart");

		this._BodyEnd = getChildNodeText(node, "BodyEnd");

		// 输出为手机显示模式
		this._BodyStartMobile = getChildNodeText(node, "BodyStartMobile");
		this._BodyEndMobile = getChildNodeText(node, "BodyEndMobile");

	}

	private String getChildNodeText(Node node, String name) {
		Node nodeChild = UXml.retNode(node, name);
		String s1 = UXml.retNodeText(nodeChild);
		if (s1 == null) {
			return null;
		} else {
			return s1.trim();
		}
	}

	public SkinFrames getSkinFrames(String skinName) throws Exception {
		SkinFrames sfs = this._SkinFrames.get(skinName.trim().toUpperCase());
		return sfs;
	}

	/**
	 * html4定义的头
	 * 
	 * @return the _Head
	 */
	public String getHead() {
		return _Head;
	}

	/**
	 * 获取 XHtml 定义的头
	 * 
	 * @return the _HeadXHtml
	 */
	public String getHeadXHtml() {
		return _HeadXHtml;
	}

	/**
	 * 获取 h5定义的头
	 * 
	 * @return the _HeadH5
	 */
	public String getHeadH5() {
		return _HeadH5;
	}

	/**
	 * 手机的头
	 * 
	 * @return the headMobile
	 */
	public String getHeadMobile() {
		return _HeadMobile;
	}

	/**
	 * @return the _Style
	 */
	public String getStyle() {
		return _Style;
	}

	/**
	 * @return the _Script
	 */
	public String getScript() {
		return _Script;
	}

	/**
	 * @return the _Title
	 */
	public String getTitle() {
		return _Title;
	}

	/**
	 * @return the _BodyStart
	 */
	public String getBodyStart() {
		return _BodyStart;
	}

	/**
	 * @return the _BodyEnd
	 */
	public String getBodyEnd() {
		return _BodyEnd;
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @return the _SkinFrames
	 */
	public HashMap<String, SkinFrames> getSkinFrames() {
		return _SkinFrames;
	}

	/**
	 * 获取通用body的 Mobile 开始部分
	 * 
	 * @return the _BodyStartH5
	 */
	public String getBodyStartMobile() {
		return _BodyStartMobile;
	}

	/**
	 * 获取通用body的 Mobile结束部分
	 * 
	 * @return the _BodyEndH5
	 */
	public String getBodyEndMobile() {
		return _BodyEndMobile;
	}

	/**
	 * vue的头
	 * @return the _HeadVue
	 */
	public String getHeadVue() {
		return _HeadVue;
	}

	 

}
