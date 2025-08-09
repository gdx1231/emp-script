package com.gdxsoft.easyweb.script.display.frame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.conf.ConfAddedResource;
import com.gdxsoft.easyweb.conf.ConfAddedResources;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.global.EwaInfo;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.ItemValues;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.display.items.ItemBase;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.template.Description;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.SkinScriptVersion;
import com.gdxsoft.easyweb.script.template.XItemParameter;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.UUrl;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class FrameBase {
	private static Logger LOGGER = LoggerFactory.getLogger(FrameBase.class);

	/**
	 * 固定查询的表，用于ListFrame
	 */
	private Map<String, DTTable> searchFixTables = new HashMap<String, DTTable>();
	private HashMap<String, String> _ItemParentHtmls = new HashMap<String, String>();
	private HtmlClass _HtmlClass;

	protected PageSplit _PageSplit;
	private String[] _Html5FrameSet;
	// 需要隐藏的字段集合
	private MTable _HiddenFields = null;

	public FrameBase getFrameBase() {
		return this;
	}

	/**
	 * 检查是否为隐含字段，在Page的LogicShow中定义
	 * 
	 * @param name
	 * @return
	 */
	public boolean isHiddenField(String name) {
		if (_HiddenFields == null) {
			this.initHiddenFields();
		}
		if (this._HiddenFields.getCount() == 0) {
			return false;
		}
		String name1 = name.trim().toUpperCase();
		return this._HiddenFields.containsKey(name1);
	}

	/**
	 * 初始化隐含的字段
	 */
	public void initHiddenFields() {
		UserXItem page = this.getHtmlClass().getUserConfig().getUserPageItem();
		this._HiddenFields = new MTable();
		ItemValues iv = this.getHtmlClass().getItemValues();
		if (page.testName("LogicShow")) {
			try {
				UserXItemValues logicShows = page.getItem("LogicShow");
				for (int i = 0; i < logicShows.count(); i++) {
					UserXItemValue logicShow = logicShows.getItem(i);
					// String name = logicShow.getItem("Name");
					String paraExp = logicShow.getItem("ParaExp");
					paraExp = iv.replaceLogicParameters(paraExp);
					if (!ULogic.runLogic(paraExp)) {
						continue;
					}
					String hiddenFields = logicShow.getItem("HiddenFields");
					String[] fields = hiddenFields.split(",");
					for (int k = 0; k < fields.length; k++) {
						String n = fields[k].trim().toUpperCase();
						if (!this._HiddenFields.containsKey(n)) {
							this._HiddenFields.add(n, true);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("initial the hidden fields(LogicShow) error: {}", e.getMessage());
			}
		}

		// 数据类型定义元素无UI tag = dataType
		try {
			UserXItems items = this.getHtmlClass().getUserConfig().getUserXItems();
			for (int i = 0; i < items.count(); i++) {
				UserXItem uxi = items.getItem(i);
				String tag = uxi.getSingleValue("Tag");

				// dataType
				if (ItemBase.TAG_DATA_TYPE.equalsIgnoreCase(tag)) {
					String name1 = uxi.getName().toUpperCase();
					if (!this._HiddenFields.containsKey(name1)) {
						this._HiddenFields.add(name1, true);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("initial the hidden fields (dataType) error: {}", e.getMessage());
		}

		// 通过参数传递需要隐藏的字段表达式，用,分割
		RequestValue rv = this._HtmlClass.getItemValues().getRequestValue();
		if (rv.s(FrameParameters.EWA_HIDDEN_FIELDS) != null) {
			String[] fields = rv.s(FrameParameters.EWA_HIDDEN_FIELDS).split(",");
			if (fields.length > 300) {
				LOGGER.warn("EWA_HIDDEN_FIELDS fields over 300");
				return;
			}
			for (int i = 0; i < fields.length; i++) {
				String field = fields[i].trim().toUpperCase();
				if (field.length() == 0) {
					continue;
				}
				if (!this._HiddenFields.containsKey(field)) {
					this._HiddenFields.add(field, true);
				}
			}
		}
	}

	public void createHtmlVue() {
		HtmlDocument doc = this.getHtmlClass().getDocument();

		String title = getHtmlClass().getItemValues().replaceParameters(getHtmlClass().getSysParas().getTitle(), true);
		doc.setTitle(title);

		try {
			createCss();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}

		String id = "vue_" + getHtmlClass().getSysParas().getFrameUnid();
		String content = "<div id='" + id + "'></div>";

		doc.addScriptHtml(content);

		try {
			String json = getHtmlClass().getHtmlCreator().createPageJsonExt1();
			json = json.replace("@", IItem.REP_AT_STR);

			StringBuilder vueJs = new StringBuilder("(function(){var ewacfg=");
			vueJs.append(json);
			vueJs.append(";\n");
			vueJs.append("app = EWA_VueClass(ewacfg, '#" + id + "');\n");
			vueJs.append("})();");

			doc.getJsBottom().getScripts().clear();

			doc.addJs("vue", vueJs.toString(), false);
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}

		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		if (pageAddTop != null && pageAddTop.trim().length() > 0) {
			doc.addScriptHtml(pageAddTop.trim());
		}

		String pageAddBottom = this.getPageItemValue("AddHtml", "Bottom");
		if (pageAddBottom != null && pageAddBottom.trim().length() > 0) {
			doc.addScriptHtml(pageAddBottom);
		}
	}

	/**
	 * 获取 createJsFramePage 调用已经处理好的title
	 * 
	 * @return title
	 */
	public String getPageJsTitle() {
		// 提前处理title避免出现回车问题
		String title = this.getHtmlClass().getSysParas().getTitle();
		if (title != null && title.indexOf("@") >= 0) {
			title = this.getHtmlClass().getItemValues().replaceParameters(title, true, true);
			title = title.replace("@", IItem.REP_AT_STR);
		}
		String pageDescription = Utils.textToJscript(title);

		return pageDescription;
	}

	/**
	 * 是否 显示标题栏，判断参数EWA_IS_HIDDEN_CAPTION 或Size.HiddenCaption，对于
	 * ListFrame是第一行的字段描述，对于Frame是第一行标题
	 * 
	 * @return 是否 显示标题栏
	 */
	public boolean isHiddenCaption() {
		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();
		String paraIsHidden = rv.s(FrameParameters.EWA_IS_HIDDEN_CAPTION);
		if (paraIsHidden != null) {
			if ("yes".equals(paraIsHidden) || "1".equals(paraIsHidden) || "true".equals(paraIsHidden)) {
				return true;
			}
			if ("no".equals(paraIsHidden) || "0".equals(paraIsHidden) || "false".equals(paraIsHidden)) {
				return false;
			}
		}
		String hiddenCaption = this.getPageItemValue("Size", "HiddenCaption");
		if (hiddenCaption != null && hiddenCaption.trim().equals("1")) {
			// 不显示列头
			return true;
		}

		return false;
	}

	/**
	 * 获取ListFrame分页
	 * 
	 * @return ListFrame分页
	 */
	public PageSplit getPageSplit() {
		return _PageSplit;
	}

	/**
	 * 创建 select 对象的reload事件
	 * 
	 * @return reload事件
	 * @throws Exception
	 */
	public String createSelectReload() throws Exception {
		JSONObject rst = new JSONObject();
		UserConfig uc = this.getHtmlClass().getUserConfig();
		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();

		String select_id = rv.s(FrameParameters.EWA_RELOAD_ID);
		if (select_id == null) {
			// 原来的拼写错误
			select_id = rv.s(FrameParameters.EWA_RELOAD_ID);
		}
		if (select_id == null) {
			rst.put("RST", false);
			rst.put("ERR", "EWA_RELOAD_ID 为空");

			return rst.toString();
		}
		UserXItem uxi_selected = null;
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			String name = uxi.getName();

			if (select_id.equalsIgnoreCase(name)) {
				uxi_selected = uxi;
				break;
			}
		}

		if (uxi_selected == null) {
			rst.put("RST", false);
			rst.put("ERR", "EWA_RELOAD_ID 无对应对象");

			return rst.toString();
		}

		IItem item = this.getHtmlClass().getItem(uxi_selected);
		// String itemHtml = item.createItemHtml();
		rst.put("RST", true);
		JSONObject itemJSON = item.createItemJson();
		rst.put("ITEM", itemJSON);

		return rst.toString();

	}

	public Document createXmlDataDocument() throws DOMException, Exception {
		UserConfig uc = this._HtmlClass.getUserConfig();
		String lang = this._HtmlClass.getSysParas().getLang();
		String frameUuid = _HtmlClass.getSysParas().getFrameUnid();

		Document doc = UXml.createBlankDocument();
		Element ele = doc.createElement("FrameData");
		doc.appendChild(ele);
		ele.setAttribute("Type", _HtmlClass.getSysParas().getFrameType());
		ele.setAttribute("ClassName", "__EWA_F_" + frameUuid);

		ele.setAttribute(FrameParameters.XMLNAME, uc.getXmlName());
		ele.setAttribute(FrameParameters.ITEMNAME, uc.getItemName());
		ele.setAttribute("Description",
				HtmlUtils.getDescription(uc.getUserPageItem().getItem("DescriptionSet"), "Info", lang));

		Element eleCss = doc.createElement("Css");
		String skinName = getPageItemValue("SkinName", "SkinName");
		eleCss.setAttribute("SkinName", skinName);
		eleCss.setTextContent(this._HtmlClass.getDocument().getCss().toString());
		ele.appendChild(eleCss);
		return doc;
	}

	/**
	 * 配置文件的对象的 JSON表达式(在FrameBase中生成)
	 * 
	 * @return JSON
	 * @throws Exception
	 */
	public String createJsonFrame() throws Exception {
		UserConfig uc = this._HtmlClass.getUserConfig();
		String lang = this._HtmlClass.getSysParas().getLang();
		JSONArray arr = new JSONArray();
		ItemValues iv = this._HtmlClass.getItemValues();

		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			IItem item = getHtmlClass().getItem(uxi);

			// 生成最初对象
			JSONObject obj = item.createItemJson();

			// 添加其它属性
			if (uxi.testName("DataItem")) {
				UserXItemValues us = uxi.getItem("DataItem");
				if (us.count() > 0) {
					UserXItemValue u = us.getItem(0);
					String dataField = u.getItem("DataField");
					String dataType = u.getItem("DataType");
					obj.put("DF", dataField);
					obj.put("DT", dataType);
				}
			}
			// OrderSearch 排序
			if (uxi.testName("OrderSearch")) {
				UserXItemValues us = uxi.getItem("OrderSearch");
				if (us.count() > 0) {
					UserXItemValue u = us.getItem(0);
					String isOrder = u.getItem("IsOrder");
					obj.put("IS_ORDER", "1".equals(isOrder));
				}
			}
			if (uxi.testName("DescriptionSet")) {
				String des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
				obj.put("DES", des);

				String memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);// 描述
				obj.put("MEMO", memo);
			}
			if (uxi.testName("IsMustInput") && uxi.getItem("IsMustInput").count() > 0) {
				String must = uxi.getSingleValue("IsMustInput");
				obj.put("MUST", must);
			}
			if (uxi.testName("MaxMinLength") && uxi.getItem("MaxMinLength").count() > 0) {
				String maxLength = uxi.getSingleValue("MaxMinLength", "MaxLength");
				obj.put("MAX", maxLength);
				String minLength = uxi.getSingleValue("MaxMinLength", "MinLength");
				obj.put("MIN", minLength);
			}
			// 根据逻辑表达式去除属性
			if (uxi.testName("EventSet")) {
				UserXItemValues atts = uxi.getItem("EventSet");
				for (int ia = 0; ia < atts.count(); ia++) {
					UserXItemValue att = atts.getItem(ia);

					String attName = att.getItem("EventName");
					String attValue = att.getItem("EventValue");
					if (attName.trim().length() == 0 || attValue.trim().length() == 0) {
						continue;
					}

					String attValue1 = iv.replaceLogicParameters(attValue);
					obj.put(attName.toUpperCase(), attValue1);
					obj.put("_" + attName.toUpperCase(), attValue);
				}
			}
			boolean is_open_frame_checked = false;
			if (uxi.testName("OpenFrame")) {
				UserXItemValues us = uxi.getItem("OpenFrame");
				if (us.count() > 0) {
					UserXItemValue u = us.getItem(0);
					String CallMethod = u.getItem("CallMethod");
					if (CallMethod.trim().length() > 0) {
						String CallXmlName = u.getItem("CallXmlName");
						String CallItemName = u.getItem("CallItemName");
						String CallParas = u.getItem("CallParas");
						CallXmlName = this._HtmlClass.getItemValues().replaceParameters(CallXmlName, false, true);
						CallItemName = this._HtmlClass.getItemValues().replaceParameters(CallItemName, false, true);
						String CallParas1 = this._HtmlClass.getItemValues().replaceParameters(CallParas, false, true);
						String js = "EWA.UI.Dialog." + CallMethod + "('" + this._HtmlClass.getSysParas().getFrameUnid()
								+ "','" + CallXmlName + "','" + CallItemName + "', false,\"" + CallParas1 + "\")";
						String js1 = "EWA.UI.Dialog." + CallMethod + "('" + this._HtmlClass.getSysParas().getFrameUnid()
								+ "','" + CallXmlName + "','" + CallItemName + "', false,\"" + CallParas + "\")";
						obj.put("ONCLICK", js);
						obj.put("_ONCLICK", js1);

						is_open_frame_checked = true;
					}
				}
			}
			if (!is_open_frame_checked && uxi.testName("CallAction")) {
				UserXItemValues us = uxi.getItem("CallAction");
				if (us.count() > 0) {
					UserXItemValue u = us.getItem(0);
					String Action = u.getItem("Action");
					if (Action != null && Action.trim().length() > 0) {
						String ConfirmInfo = u.getItem("ConfirmInfo");
						String AfterTip = u.getItem("AfterTip");
						ConfirmInfo = this._HtmlClass.getItemValues().replaceParameters(ConfirmInfo, false, true);
						AfterTip = this._HtmlClass.getItemValues().replaceParameters(AfterTip, false, true);

						Action = Action.replace("\"", "&quot;");
						ConfirmInfo = ConfirmInfo.replace("\"", "&quot;");
						AfterTip = AfterTip.replace("\"", "&quot;");
						String js = "EWA.F.FOS['" + this._HtmlClass.getSysParas().getFrameUnid() + "'].DoAction(this,\""
								+ Action + "\",\"" + ConfirmInfo + "\",\"" + AfterTip + "\")";
						obj.put("ONCLICK", js);
						obj.put("_ONCLICK", js);
					}
				}
			}
			if (uxi.getSingleValue("tag").equals("droplist")) {
				UserXItemValues us = uxi.getItem("Frame");
				if (us.count() > 0) {
					UserXItemValue u = us.getItem(0);

					String CallXmlName = u.getItem("CallXmlName");
					String CallItemName = u.getItem("CallItemName");
					String CallPara = u.getItem("CallPara");
					CallXmlName = this._HtmlClass.getItemValues().replaceParameters(CallXmlName, false, true);
					CallItemName = this._HtmlClass.getItemValues().replaceParameters(CallItemName, false, true);
					CallPara = this._HtmlClass.getItemValues().replaceParameters(CallPara, false, true);
					obj.put("CallXmlName", CallXmlName);
					obj.put("CallItemName", CallItemName);
					obj.put("CallPara", CallPara);
				}
				us = uxi.getItem("DopListShow");
				if (us.count() > 0) {
					UserXItemValue u = us.getItem(0);

					String DlsShow = u.getItem("DlsShow");
					String DlsAction = u.getItem("DlsAction");

					obj.put("DlsShow", DlsShow);
					obj.put("DlsAction", DlsAction);
				}
			} else if (uxi.getSingleValue("tag").equals("swffile") || uxi.getSingleValue("tag").equals("image")
					|| uxi.getSingleValue("tag").equals("h5upload")) {
				UserXItemValues us = uxi.getItem("Upload");
				if (us.count() > 0) {
					UserXItemValue u = us.getItem(0);
					if (u.testName("UpMulti")) {// 多文件上传
						String UpMulti = u.getItem("UpMulti");
						if (UpMulti.equalsIgnoreCase("yes")) {
							obj.put("UpMulti", "yes");
						} else {
							obj.put("UpMulti", "no");
						}
					} else {
						obj.put("UpMulti", "no");
					}

				}
			} else if (uxi.getSingleValue("tag").equals("butFlow")) {
				obj.put("flow", "{{FLOW}}");
			}
			arr.put(obj);
		}
		return arr.toString();
	}

	/**
	 * item描述XML字符串
	 * 
	 * @throws Exception
	 */
	public void createJsFrameXml() throws Exception {
		String frameUuid = _HtmlClass.getSysParas().getFrameUnid();
		String s2 = this.createJsFrameXmlString();

		StringBuilder sb = new StringBuilder();
		sb.append("var EWA_ITEMS_XML_");
		sb.append(frameUuid);
		sb.append("=\"");
		sb.append(s2);
		sb.append("\";");
		this._HtmlClass.getDocument().addJs("FRAME_XML", sb.toString(), false);
	}

	/**
	 * 生成 JsFrameXml 字符串表达式，2020-05-26新增
	 * 
	 * @return JsFrameXml 字符串表达式
	 * @throws Exception
	 */
	public String createJsFrameXmlString() throws Exception {
		UserConfig uc = this._HtmlClass.getUserConfig();
		MStr hiddenInfo = new MStr(); // 在页面上隐含的
		hiddenInfo.append("<root>");
		for (int i = 0; i < uc.getUserXItems().count(); i++) {
			UserXItem uxi = uc.getUserXItems().getItem(i);
			hiddenInfo.append(this.createJsFrameXml(uxi));
		}

		for (int i = 0; i < uc.getUserPageInfos().count(); i++) {
			UserXItem uxi = uc.getUserPageInfos().getItem(i);
			hiddenInfo.append(uxi.getXml());
		}

		hiddenInfo.append("</root>");
		String s2 = Utils.textToJscript(hiddenInfo.toString());
		s2 = s2.replace("@", "@@");
		return s2;
	}

	/**
	 * 生成菜单的Js表达式
	 * 
	 * @throws Exception
	 */
	public void createJsFrameMenu() throws Exception {
		String gunid = _HtmlClass.getSysParas().getFrameUnid();
		UserConfig uc = this._HtmlClass.getUserConfig();
		String lang = this._HtmlClass.getSysParas().getLang();

		MStr js = new MStr();
		String name = "\r\n_EWA_MENU_" + gunid + "[";
		js.append("\r\nvar _EWA_MENU_" + gunid + "=[];");
		for (int i = 0; i < uc.getUserMenuItems().count(); i++) {
			UserXItem u = uc.getUserMenuItems().getItem(i);
			String icon = Utils.textToJscript(u.getSingleValue("Icon"));
			String cmd = Utils.textToJscript(u.getSingleValue("Cmd"));
			String txt = Utils.textToJscript(HtmlUtils.getDescription(u.getItem("DescriptionSet"), "Info", lang));
			String id = Utils.textToJscript(u.getSingleValue("Name"));
			String mg = Utils.textToJscript(u.getSingleValue("Group"));
			js.append(name + i + "]=new EWA_UI_MenuItemClass();");
			js.append(name + i + "].Id=\"" + id + "\";");
			js.append(name + i + "].Txt=\"" + txt + "\";");
			js.append(name + i + "].Img=\"" + icon + "\";");
			js.append(name + i + "].Cmd=\"" + cmd + "\";");
			js.append(name + i + "].Group=\"" + mg + "\";");
		}

		this._HtmlClass.getDocument().addJs("FRAME_MENU", js.toString(), false);
	}

	/**
	 * item描述XML字符串
	 * 
	 * @param userXItem
	 * @return XML
	 * @throws Exception
	 */
	private String createJsFrameXml(UserXItem userXItem) throws Exception {
		MStr sb = new MStr();
		sb.append("<XItem>");
		for (int i = 0; i < userXItem.count(); i++) {
			UserXItemValues userXItemValues = userXItem.getItem(i);
			XItemParameter x = userXItemValues.getParameter();
			if (!x.isJsShow() || x.isNotJsShow()) {
				continue;
			}
			String xml = userXItemValues.getXml();
			if (xml.indexOf("<DispEnc") == 0) {
				// 替换 @EWA.CP
				xml = this._HtmlClass.getItemValues().replaceJsParameters(xml);
			}

			if (xml.toUpperCase().indexOf("@SYS_FRAME_UNID") > 0) {
				// 替换 @SYS_FRAME_UNID 2018-06-25 guolei
				xml = this._HtmlClass.getItemValues().replaceJsParameters(xml);
			}
			xml = Utils.deleteStr(xml, "SearchSql=\"", "\"");
			sb.append(xml);
		}
		sb.append("</XItem>");
		return sb.toString();
	}

	/**
	 * 生成CSS内容
	 * 
	 * @throws Exception
	 */
	public void createCss() throws Exception {
		MStr sb = new MStr();

		sb.append(this._HtmlClass.getSkin().getStyle() + "\r\n");
		sb.append(this._HtmlClass.getSkinFrameAll().getStyle() + "\r\n");
		sb.append(this._HtmlClass.getSkinFrameCurrent().getStyle() + "\r\n");

		// 用户定义CSS
		String css = this.getPageItemValue("AddCss", "AddCss");
		if (css != null) {
			// 保留 @media @keyframes @import标签
			css = css.replace("@media", IItem.REP_AT_STR + "media")
					.replace("@keyframes", IItem.REP_AT_STR + "keyframes")
					.replace("@import", IItem.REP_AT_STR + "import");
			sb.append(css);
		}

		HtmlDocument doc = this._HtmlClass.getDocument();
		String css1 = this._HtmlClass.getItemValues().replaceParameters(sb.toString(), true);
		doc.addCss(css1);

	}

	/**
	 * 生成头部Js
	 */
	public void createJsTop() {
		// for tree default action
		HtmlDocument doc = this._HtmlClass.getDocument();
		String s = this._HtmlClass.getSkin().getScript();

		if (s.trim().length() > 0) {
			doc.addJs("SKIN_0", s, true);
		}

		s = this._HtmlClass.getSkinFrameAll().getScript();
		if (s.trim().length() > 0) {
			doc.addJs("SKIN_1", s, true);
		}
		s = this._HtmlClass.getSkinFrameCurrent().getScript();
		if (s.trim().length() > 0) {
			doc.addJs("SKIN_2", s, true);
		}

		// 用户自定义头部Js
		String pageAddJsTop = this.getPageItemValue("AddScript", "Top");
		if (pageAddJsTop != null && pageAddJsTop.trim().length() > 0) {
			pageAddJsTop = this._HtmlClass.getItemValues().replaceJsParameters(pageAddJsTop);
			pageAddJsTop = pageAddJsTop.replace("@", IItem.REP_AT_STR);
			doc.addJs("JS_TOP", pageAddJsTop, true);
		}
	}

	/**
	 * 获取页面的URL的JS表达式
	 * 
	 * @return JS
	 */
	public String getUrlJs() {
		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();

		String url;
		UUrl uu = new UUrl(rv.getRequest());

		String callMethod = rv.s(FrameParameters.EWA_CALL_METHOD);
		// INNER_CALL 调用模式，表示为ewaconfigitem或 JSp程序调用
		if (FrameParameters.INNER_CALL.equalsIgnoreCase(callMethod)) {
			uu.setPath(rv.getContextPath());
			uu.setName("/EWA_STYLE/cgi-bin/");

			uu.add(FrameParameters.XMLNAME, rv.s(FrameParameters.XMLNAME));
			uu.add(FrameParameters.ITEMNAME, rv.s(FrameParameters.ITEMNAME));
		}
		// 来自HtmlControl的参数放到 PageValueTag.HTML_CONTROL_PARAS 中
		// 覆盖queryString
		this.attachHtmlControlParas(uu, rv);
		url = uu.getUrl();
		return url == null ? null : url.replace("@", "%40").replace("+", "%20");
	}

	/**
	 * 来自HtmlControl的参数放到 PageValueTag.HTML_CONTROL_PARAS 中
	 * 
	 * @param uu
	 * @param rv
	 */
	private void attachHtmlControlParas(UUrl uu, RequestValue rv) {
		MTable paras = rv.getPageValues().getTagValues(PageValueTag.HTML_CONTROL_PARAS);
		if (paras == null) {
			return;
		}
		for (int i = 0; i < paras.getCount(); i++) {
			String key = paras.getKey(i).toString();
			Object val = paras.getByIndex(i);
			PageValue pv = (PageValue) val;
			String sval = pv.getStringValue();
			if (sval == null) {
				continue;
			}
			if (sval.length() > 1000) { // 太长了，抛弃
				continue;
			}
			uu.add(key, sval);
		}
	}

	/**
	 * 配置页面定义的底部附加 Js
	 */
	public void createJsBottom() {
		HtmlDocument doc = this._HtmlClass.getDocument();

		String pageAddJsBottom = this.getPageItemValue("AddScript", "Bottom");

		if (pageAddJsBottom != null) {
			// pageAddJsBottom =
			// this._HtmlClass.getItemValues().replaceParameters(pageAddJsBottom, false);
			pageAddJsBottom = this._HtmlClass.getItemValues().replaceJsParameters(pageAddJsBottom);

			pageAddJsBottom = pageAddJsBottom.replace("@", IItem.REP_AT_STR);
			doc.addJs("JS_BOTTOM", pageAddJsBottom, false);
		}

		// 增加附加的资源
		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();
		String ewa_added_resources = rv.s(FrameParameters.EWA_ADDED_RESOURCES);
		List<ConfAddedResource> al = StringUtils.isBlank(ewa_added_resources)
				? ConfAddedResources.getInstance().getDefaultResList(true)
				: ConfAddedResources.getInstance().getResList(ewa_added_resources, true);
		al.forEach(r -> {
			if ("css".equals(r.getResourceType())) {
				LOGGER.warn("The css put on bottom, " + r.getName());
			}
			doc.addScriptHtml(r.toString());
		});
	}

	private String[] createH5FrameSet() {
		try {
			UserXItemValues us = this._HtmlClass.getUserConfig().getUserPageItem().getItem("HtmlFrame");
			if (us.count() == 0)
				return null;
			UserXItemValue u = us.getItem(0);
			String frameType = u.getItem("FrameType");
			if (frameType.trim().length() == 0)
				return null;
			String frameSize = u.getItem("FrameSize");

			if (!(frameType.equals("H5") || frameType.equals("V5"))) { // html5
				return null;
			}
			String[] content = new String[2];

			String stOne = null;
			String stTwo = null;
			String stSplit = null;
			String tag = frameType.equals("H5") ? "width: " : "height: ";
			String tag1 = frameType.equals("H5") ? "left: " : "top: ";
			if (frameSize != null && frameSize.trim().length() > 0) {
				String[] sizes = frameSize.split(",");
				String s0 = sizes[0].trim();
				try {
					int is0 = Integer.parseInt(s0);
					stOne = tag + is0 + "px";
					stSplit = tag1 + is0 + "px";
					stTwo = tag1 + s0+ "px";
				} catch (Exception err) {
					stOne = tag + s0;
					stSplit = tag1 + s0;
					stTwo = tag1 + s0;
				}

				if (sizes.length > 1) {
					String s1 = sizes[1].trim();
					if (s1.length() > 0 && !s1.equals("*")) {
						try {
							int is1 = Integer.parseInt(s1);
							stTwo = tag1 + is1 + "px";
						} catch (Exception err) {
							stTwo = tag1 + s1;
						}
					}
				}
			}

			MStr top = new MStr();
			MStr bottom = new MStr();
			String id0 = this._HtmlClass.getItemValues().getSysParas().getFrameUnid();
			top.a("<div  class='ewa-frameset-" + frameType + "'><div class='ewa-frameset-one' id='F0_" + id0 + "'");
			if (stOne != null) {
				top.a(" style='" + stOne.replace("'", "").replace("\"", "") + "'");
			}
			top.al(">");
			bottom.a("</div><div class='ewa-frameset-split'");

			if (stSplit != null) {
				bottom.a(" style='" + stSplit.replace("'", "").replace("\"", "") + "'");
			}

			bottom.a("></div><div class='ewa-frameset-two' id='F1_" + id0 + "'");
			if (stTwo != null) {
				bottom.a(" style='" + stTwo.replace("'", "").replace("\"", "") + "'");
			}
			bottom.a("></div><div class='ewa-frameset-cover'></div></div>");
			bottom.al("<script>(function(){var a=new EWA_UI_H5FrameSet(); a.Create('" + id0 + "','" + frameType
					+ "')})();</script>");
			content[0] = top.toString();
			content[1] = bottom.toString();

			this._Html5FrameSet = content;

			return content;

		} catch (Exception err) {
			return null;
		}
	}

	/**
	 * 生成主头部
	 * 
	 * @throws Exception
	 */
	public void createSkinTop() throws Exception {
		HtmlDocument doc = this._HtmlClass.getDocument();
		String skinName = getPageItemValue("SkinName", "SkinName");
		// if (this._HtmlClass.getSysParas().isXhtml()
		// || (isXhtml != null && isXhtml.trim().equals("1"))) {
		// sb.append(this._HtmlClass.getSkin().getHeadXHtml());
		// } else {
		// sb.append(this._HtmlClass.getSkin().getHead());
		// }
		// 页面的 title
		String title = this._HtmlClass.getItemValues().replaceParameters(this._HtmlClass.getSysParas().getTitle(),
				true);

		RequestValue rv = this._HtmlClass.getItemValues().getRequestValue();

		String lang = this._HtmlClass.getItemValues().getSysParas().getLang();
		if (rv.s(FrameParameters.EWA_MTYPE) != null && this._HtmlClass.getFrame() instanceof FrameFrame) {
			// 在Title前增加“修改、新增、复制”前缀
			// EWA_MTYPE_tag = M or N or C
			String EWA_MTYPE_tag = "EWA_MTYPE_" + rv.s(FrameParameters.EWA_MTYPE).toUpperCase();
			if (EwaGlobals.instance().getEwaInfos().testName(EWA_MTYPE_tag)) {
				try {
					EwaInfo b = EwaGlobals.instance().getEwaInfos().getItem(EWA_MTYPE_tag);
					Description a = b.getDescriptions().getItem(lang);
					String txt = a.getInfo();
					title = txt + " " + title;
				} catch (Exception err) {

				}
			}
		}
		doc.setTitle(title);
		String bodyStart = this._HtmlClass.getSkinFrameAll().getBodyStart() == null ? ""
				: this._HtmlClass.getSkinFrameAll().getBodyStart();
		doc.addBodyHtml(bodyStart, true);

		String bs = this._HtmlClass.getSkin().getBodyStart() == null ? "" : this._HtmlClass.getSkin().getBodyStart();

		// 将脚本替换成按照脚本的fileCode组成的表达式，用于判别脚本是否变化
		if (bs.indexOf("@EWA_SCRIPTS") >= 0) {
			String cp = this._HtmlClass.getHtmlCreator().getRequestValue().getContextPath();
			String scripts = SkinScriptVersion.ewaScripts(cp).toHtml();
			bs = bs.replace("@EWA_SCRIPTS", scripts);
		}

		UserConfig uc = this._HtmlClass.getUserConfig();

		// 页面定义的参数 PageAttributeSet
		String attrs = this.createPageAttrs(uc);
		bs = bs.replace("!!>", attrs + ">");

		doc.addBodyHtml(bs, true);

		ConfAddedResources res = ConfAddedResources.getInstance();
		if (res != null) {
			String ewa_added_resources = rv.s(FrameParameters.EWA_ADDED_RESOURCES);
			List<ConfAddedResource> al = StringUtils.isBlank(ewa_added_resources)
					? ConfAddedResources.getInstance().getDefaultResList(false)
					: ConfAddedResources.getInstance().getResList(ewa_added_resources, false);
			al.forEach(r -> {
				doc.addScriptHtml(r.toString());
			});
		}
		boolean useTest1 = this.isUseTest1Table();
		if (useTest1) {
			doc.addScriptHtml("<div id='EWA_FRAME_MAIN' _s='主框架开始'>", "主框架开始");
		}
		this.createH5FrameSet();
		if (this._Html5FrameSet != null) {
			doc.addScriptHtml(_Html5FrameSet[0]);
		}
		if (useTest1) {
			this.createTest1Table(doc, skinName); // <table id='Test1'
		}
		if (this._HtmlClass.getItemValues().getRequestValue().s(FrameParameters.EWA_IN_DIALOG) != null) {
			doc.addScriptHtml("<div class='ewa-in-dialog'>");
		}
		doc.addScriptHtml(this._HtmlClass.getSkinFrameAll().getTop());
		// sb.append("<div _s='内容窗体开始'>");

	}

	/**
	 * 根据逻辑表达式去除对象属性
	 * 
	 * @param uxi
	 * @param itemHtml
	 * @return 去除对象属性
	 */
	String removeAttrsByLogic(UserXItem uxi, String itemHtml) {
		if (!uxi.testName("AttributeSet")) {
			return itemHtml;
		}
		ItemValues iv = getHtmlClass().getItemValues();
		MStr html = new MStr(itemHtml);
		try {
			UserXItemValues atts = uxi.getItem("AttributeSet");

			for (int ia = 0; ia < atts.count(); ia++) {
				UserXItemValue att = atts.getItem(ia);
				if (!att.testName("AttLogic")) {
					continue;
				}
				String logic = att.getItem("AttLogic");
				if (logic.length() == 0) {
					continue;
				}
				String logic1 = iv.replaceLogicParameters(logic);
				if (ULogic.runLogic(logic1)) {
					continue;
				}

				// 在 ItemBase.replaceProperties中创建
				String attrExp = att.getItem(ItemBase.ATTR_EXP_NAME);
				// 表达式为假
				/*
				 * String attName = att.getItem("AttName"); String attValue =
				 * att.getItem("AttValue"); String attName1 =
				 * HtmlUtils.createAttNameByValue(attName, attValue);
				 * 
				 * if (StringUtils.isBlank(attName1)) { continue; }
				 * 
				 * String exp = attName1 + "=\"" + Utils.textToInputValue(attValue) + "\"";
				 */
				html.replace(attrExp, "");
			}
			return html.toString();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
			return itemHtml;
		}

	}

	/**
	 * 页面定义的参数 PageAttributeSet
	 * 
	 * @param uc
	 * @return
	 * @throws Exception
	 */
	public String createPageAttrs(UserConfig uc) throws Exception {
		if (!uc.getUserPageItem().testName("PageAttributeSet")) {
			return "";
		}
		MStr sbPageAttr = new MStr();
		UserXItemValues u = uc.getUserPageItem().getItem("PageAttributeSet");
		for (int i = 0; i < u.count(); i++) {
			UserXItemValue u0 = u.getItem(i);
			String n = u0.getItem("PageAttName").trim();
			String v = u0.getItem("PageAttValue").trim();
			String attName = HtmlUtils.createAttNameByValue(n, v);
			if (StringUtils.isBlank(attName)) {
				continue;
			}
			// v 在最后处理，以避免数据未获取到
			sbPageAttr.append(" " + attName + "=\"" + Utils.textToInputValue(v) + "\"");
		}

		return sbPageAttr.toString();
	}

	/**
	 * 是否使用 Test1的table，ewa_skip_test1 =1可以跳过
	 * 
	 * @return
	 */
	public boolean isUseTest1Table() {
		RequestValue rv = this._HtmlClass.getItemValues().getRequestValue();
		if (rv.s(FrameParameters.EWA_SKIP_TEST1) != null) {
			return !Utils.cvtBool(rv.s(FrameParameters.EWA_SKIP_TEST1));
		}
		if (this._HtmlClass.getSysParas().isPc()) {
			return true;
		}
		String frameTag = this._HtmlClass.getUserConfig().getUserPageItem().getSingleValue("FrameTag");
		if (!frameTag.equalsIgnoreCase("Complex")) {
			return true;
		}
		return false;
	}

	/**
	 * 创建页面的style，宽、高、水平和垂直对齐方式
	 * 
	 * @param doc
	 * @return
	 */
	public String createFrameStyle(HtmlDocument doc) {
		RequestValue rv = this._HtmlClass.getItemValues().getRequestValue();

		StringBuilder sb = new StringBuilder();

		String sizeW = this.getPageItemValue("Size", "Width");
		String sizeH = this.getPageItemValue("Size", "Height");
		String sizeVAlign = this.getPageItemValue("Size", "VAlign");
		String sizeHAlign = this.getPageItemValue("Size", "HAlign");
		String mainWidth = null;

		String userWidth = null;
		// 用户参数指定宽度
		if (rv.isNotBlank(FrameParameters.EWA_WIDTH)) {
			userWidth = rv.s(FrameParameters.EWA_WIDTH).replace("'", "").replace("\"", "").replace(">", "")
					.replace("<", "").replace(";", "").replace("\n", " ").trim();
		}
		String userHeight = null;
		// 用户参数指定的高度
		if (rv.isNotBlank(FrameParameters.EWA_HEIGHT)) {
			userHeight = rv.s(FrameParameters.EWA_HEIGHT).replace("'", "").replace("\"", "").replace(">", "")
					.replace("<", "").replace(";", "").replace("\n", " ").trim();
		}
		if (userWidth != null && userWidth.length() > 0) {
			try {
				int w = Integer.parseInt(userWidth);
				mainWidth = "width: " + w + "px; ";
				sb.append("width: " + w + "px; ");
			} catch (Exception e) {
				mainWidth = userWidth;
				sb.append("width: " + mainWidth + "; ");
			}
		} else if (sizeW != null && sizeW.length() > 0) {
			try {
				int w = Integer.parseInt(sizeW);
				mainWidth = "width: " + w + "px; ";
				sb.append("width: " + w + "px; ");
			} catch (Exception e) {
				mainWidth = "width: " + sizeW + "; ";
				sb.append("width: " + sizeW + "; ");
			}
		} else {
			sb.append("width:100%; ");
		}

		if (mainWidth != null) {
			String ft = this._HtmlClass.getSysParas().getFrameType();
			if (ft.equals("COMBINE")) {
				doc.addCss(".ewa_cb_box{" + mainWidth + "}");
			}
		}
		if (userHeight != null && userHeight.trim().length() > 0) {
			try {
				int h = Integer.parseInt(userHeight);
				sb.append("height: " + h + "px; ");
			} catch (Exception e) {
				sb.append("height: " + userHeight + "; ");
			}
		} else if (sizeH != null && sizeH.trim().length() > 0) {
			try {
				int h = Integer.parseInt(sizeH);
				sb.append("height: " + h + "px; ");
			} catch (Exception e) {
				sb.append("height: " + sizeH + "; ");
			}
		}
		if (sizeHAlign != null && sizeHAlign.trim().length() > 0) {
			if ("center".equals(sizeHAlign)) {
				sb.append("margin: auto; ");
			} else if ("left".equals(sizeHAlign)) {
				sb.append("margin-right: auto; ");
			} else if ("right".equals(sizeHAlign)) {
				sb.append("margin-left: auto; ");
			}
		}
		if (sizeVAlign != null && sizeVAlign.trim().length() > 0) {
			sb.append("vertical-align: " + sizeVAlign + "; ");
		}

		return sb.toString();
	}

	private void createTest1Table(HtmlDocument doc, String skinName) {
		String style = this.createFrameStyle(doc);
		String fuid = this._HtmlClass.getItemValues().getSysParas().getFrameUnid();
		StringBuilder sb = new StringBuilder();
		sb.append("<table id='");
		sb.append(skinName);
		sb.append("' border='0' cellspacing='0' cellpadding='0' style=\"");
		sb.append(style);
		sb.append("\"");
		sb.append(" class='ewa-frame-");
		sb.append(fuid);
		sb.append("'");
		sb.append(">");

		String sizeVAlign = this.getPageItemValue("Size", "VAlign");
		if (sizeVAlign == null || sizeVAlign.trim().length() == 0) {
			sizeVAlign = "top";
		}
		sb.append("\n<tr>\n<td height='100%' class='ewa-frame-box-" + fuid + "' vAlign='" + sizeVAlign + "'>");

		doc.addScriptHtml(sb.toString());
	}

	/**
	 * 生成主底部 AddHtml-Bottom
	 * 
	 * @throws Exception
	 */
	public void createSkinBottom() throws Exception {
		String pageAddBottom = this.getPageItemValue("AddHtml", "Bottom");
		HtmlDocument doc = this._HtmlClass.getDocument();
		if (pageAddBottom != null && pageAddBottom.trim().length() > 0) {
			doc.addScriptHtml(pageAddBottom);
			doc.addFrameHtml(pageAddBottom);
		}
		// 在对话框的类结束
		if (this._HtmlClass.getItemValues().getRequestValue().s(FrameParameters.EWA_IN_DIALOG) != null) {
			doc.addScriptHtml("</div><!-- end of ewa-in-dialog -->");
		}
		boolean useTest1 = this.isUseTest1Table();
		if (useTest1) {
			doc.addScriptHtml("</td></tr></table><!--浮动表结束-->");
		}
		if (this._Html5FrameSet != null) {
			doc.addScriptHtml(_Html5FrameSet[1]);
		}
		if (useTest1) {
			doc.addScriptHtml("</div><!-- 主框架结束 -->");
		}
		// this._HtmlClass.getDocument().addScriptHtml(
		// this._HtmlClass.getSkin().getBodyEnd());
	}

	/**
	 * 生成Item的容器html
	 * 
	 * @param uxi
	 * @return html
	 * @throws Exception
	 */
	public String createItemParentHtml(UserXItem uxi) throws Exception {
		String lang = this._HtmlClass.getSysParas().getLang();
		String name = uxi.getName();
		if (_ItemParentHtmls.get(name) != null) {
			return _ItemParentHtmls.get(name);
		}
		String frameTemplate; // 皮肤定义的页面样式
		String des = "", memo = "";
		try {
			des = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Info", lang);// 描述
			memo = HtmlUtils.getDescription(uxi.getItem("DescriptionSet"), "Memo", lang);// memo
		} catch (Exception err) {

		}

		frameTemplate = this._HtmlClass.getSkinFrameCurrent().getItem();

		String s2 = frameTemplate.replace(SkinFrame.TAG_DES, des); // 替换描述
		s2 = s2.replace(SkinFrame.TAG_NAME, uxi.getName()); // 替换 id
		s2 = s2.replace(SkinFrame.TAG_MSG, memo); // 替换备注

		RequestValue rv = this._HtmlClass.getItemValues().getRequestValue();
		if (rv.s(FrameParameters.EWA_CELL_ADD_DES) != null) {
			// 在每个td上添加属性 ewa_cell_des, ewa_cell_memo
			// 前端css：.ewa-col-name::before {content: attr(ewa_cell_des);}
			String atts = " " + FrameParameters.EWA_CELL_ADD_DES_NAME + "=\"" + Utils.textToInputValue(des) + "\"";
			if (memo.length() > 0) {
				atts += " " + FrameParameters.EWA_CELL_ADD_DES_NAME_MEMO + "=\"" + Utils.textToInputValue(memo) + "\"";
			}
			s2 = s2.replace("EWA_TD_M\"", "EWA_TD_M\" " + atts);
		}
		// 将ParentHtml内容放入，下次使用
		_ItemParentHtmls.put(name, s2);
		return s2;
	}

	/**
	 * 生成页面的JSON数据
	 * 
	 * @return JSON
	 * @throws Exception
	 */
	public String createJsonContent() throws Exception {
		return null;
	}

	/**
	 * 生成页面的JS
	 * 
	 * @return JS
	 */
	public String createJsonJs() {
		String pageAddBottom = this.getPageItemValue("AddScript", "Bottom");
		String pageAddTop = this.getPageItemValue("AddScript", "Top");
		String js = this._HtmlClass.getItemValues().replaceParameters(pageAddTop + pageAddBottom, false, true);
		return js;
	}

	public String createSkinFCFooter() {
		String lang = this._HtmlClass.getSysParas().getLang();
		try {
			return this._HtmlClass.getSkinFrameCurrent().getItemFooter().getDescription(lang).getMemo();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#createSkinFCTop()
	 */
	String createSkinFCTop() {
		return this._HtmlClass.getSkinFrameCurrent().getTop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#createSkinFCHeader()
	 */
	String createSkinFCHeader() {
		return this._HtmlClass.getSkinFrameCurrent().getItemHeader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#createSkinFCItem()
	 */
	String createSkinFCItem() {
		return this._HtmlClass.getSkinFrameCurrent().getItem();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#createSkinFCItemButton()
	 */
	String createSkinFCItemButton() {
		return this._HtmlClass.getSkinFrameCurrent().getItemButton();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#createSkinFCBottom()
	 */
	String createSkinFCBottom() {
		return this._HtmlClass.getSkinFrameCurrent().getBottom();
	}

	String getPageItemValue(String itemName, String tagName) {
		UserConfig uc = this._HtmlClass.getUserConfig();
		if (uc.getUserPageItem().testName(itemName)) {
			try {
				UserXItemValues v = uc.getUserPageItem().getItem(itemName);
				if (v.count() == 0) {
					return null;
				}
				if (!v.getParameter().isMulti()) {
					return v.getItem(0).getItem(0);
				} else {
					return v.getItem(0).getItem(tagName);
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		} else {
			return null;
		}
	}

	public void addDebug(Object fromClass, String eventName, String description) {
		if (this._HtmlClass.getDebugFrames() != null) {
			this._HtmlClass.getDebugFrames().addDebug(fromClass, eventName, description);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#getItemParentHtmls()
	 */
	public HashMap<String, String> getItemParentHtmls() {
		return _ItemParentHtmls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#setItemParentHtmls(java.util
	 * .HashMap)
	 */
	public void setItemParentHtmls(HashMap<String, String> itemParentHtmls) {
		_ItemParentHtmls = itemParentHtmls;
	}

	/*
	 * 
	 * /** @return the _HtmlClass
	 */
	public HtmlClass getHtmlClass() {
		return _HtmlClass;
	}

	/**
	 * @param htmlClass the _HtmlClass to set
	 */
	public void setHtmlClass(HtmlClass htmlClass) {
		_HtmlClass = htmlClass;
	}

	/**
	 * 获取工作流的js表达式
	 * 
	 * @return the _WorkFlowBut
	 */
	public String getWorkFlowButJson() {
		return null;
	}

	/**
	 * 需要隐藏的字段集合
	 * 
	 * @return the _HiddenFields
	 */
	public MTable getHiddenFields() {
		return _HiddenFields;
	}

	/**
	 * 需要隐藏的字段集合
	 * 
	 * @param hiddenFields the 需要隐藏的字段集合 to set
	 */
	public void setHiddenFields(MTable hiddenFields) {
		this._HiddenFields = hiddenFields;
	}

	/**
	 * 固定查询的表，用于ListFrame
	 * 
	 * @return 固定查询的表
	 */
	public Map<String, DTTable> getSearchFixTables() {
		return searchFixTables;
	}
}
