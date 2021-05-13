package com.gdxsoft.easyweb.script.display.frame;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gdxsoft.easyweb.conf.ConfAddedResource;
import com.gdxsoft.easyweb.conf.ConfAddedResources;
import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.global.EwaInfo;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.template.Description;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.SkinScriptVersion;
import com.gdxsoft.easyweb.script.template.XItemParameter;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class FrameBase {
	private static Logger LOGGER = LoggerFactory.getLogger(FrameBase.class);
	private HashMap<String, String> _ItemParentHtmls = new HashMap<String, String>();
	private HtmlClass _HtmlClass;

	protected PageSplit _PageSplit;
	private String[] _Html5FrameSet;

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
	 * 是否 显示标题栏，判断参数EWA_IS_HIDDEN_CAPTION 或Size.HiddenCaption，对于 ListFrame是第一行的字段描述，对于Frame是第一行标题
	 * 
	 * @return 是否 显示标题栏
	 */
	public boolean isHiddenCaption() {

		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();
		String paraIsHidden = rv.s("EWA_IS_HIDDEN_CAPTION");
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

		String select_id = rv.s("ewa_reload_id");
		if (select_id == null) {
			// 原来的拼写错误
			select_id = rv.s("ewa_reaload_id");
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

		ele.setAttribute("XmlName", uc.getXmlName());
		ele.setAttribute("ItemName", uc.getItemName());
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

					String attValue1 = this._HtmlClass.getItemValues().replaceParameters(attValue, false, true);
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
		sb.append(css == null ? "" : css);

		HtmlDocument doc = this._HtmlClass.getDocument();
		String css1 = this._HtmlClass.getItemValues().replaceParameters(sb.toString(), true);
		doc.addCss(css1);

		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();

		String ewa_added_resources = rv.s("ewa_added_resources");
		List<ConfAddedResource> al = ConfAddedResources.getInstance().getResList(ewa_added_resources, false);

		MStr sbCss = new MStr();
		MStr sbJs = new MStr();
		for (int i = 0; i < al.size(); i++) {
			ConfAddedResource r = al.get(i);
			if (r.getSrc().toLowerCase().endsWith(".css")) {
				sbCss.al(r.toCss());
			} else {
				sbJs.al(r.toJs());
			}
		}
		doc.addHeader(sbCss.toString());
		doc.addHeader(sbJs.toString());
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

	private String encodeUrl(String s) {
		try {
			return java.net.URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

	/**
	 * 获取页面的URL的JS表达式
	 * 
	 * @return JS
	 */
	public String getUrlJs() {
		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();
		String q;
		String callMethod = rv.getString("EWA_CALL_METHOD");

		String url;
		// //INNER_CALL 调用模式，表示为ewaconfigitem或 JSp程序调用
		if (callMethod != null && callMethod.equalsIgnoreCase("INNER_CALL")) {
			MTable map = new MTable();
			map.put("XMLNAME", encodeUrl(rv.getString("xmlname")));
			map.put("ITEMNAME", encodeUrl(rv.getString("itemname")));

			// htmlcontrol传递的参数
			MTable qvsHtmlControl = rv.getPageValues().getTagValues(PageValueTag.HTML_CONTROL_PARAS);
			if (qvsHtmlControl != null) {
				for (int i = 0; i < qvsHtmlControl.getCount(); i++) {
					String key = qvsHtmlControl.getKey(i).toString().trim().toUpperCase();
					if (!map.containsKey(key)) {
						PageValue o = (PageValue) qvsHtmlControl.get(key);
						String val = o.getStringValue();
						if (val != null) {
							map.put(key, encodeUrl(val));
						}
					}
				}
			}

			// query传递的参数
			MTable qvs = rv.getPageValues().getQueryValues();
			for (int i = 0; i < qvs.getCount(); i++) {
				String key = qvs.getKey(i).toString().trim().toUpperCase();
				if (!map.containsKey(key)) {
					PageValue o = (PageValue) qvs.get(key);
					String val = o.getStringValue();
					if (val != null) {
						map.put(key, encodeUrl(val));
					}
				}
			}

			MStr sb = new MStr();
			for (int i = 0; i < map.getCount(); i++) {
				String key = map.getKey(i).toString().trim().toUpperCase();
				if (map.get(key) == null) {
					continue;
				}
				String val = map.get(key).toString();
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(key + "=" + val);
			}
			q = sb.toString();
			url = rv.getContextPath() + "/EWA_STYLE/cgi-bin/?" + Utils.textToJscript(q);
		} else {
			q = rv.getRequest().getQueryString();
			StringBuilder sb = new StringBuilder();
			sb.append(rv.getContextPath());
			sb.append(rv.getRequest().getServletPath());

			boolean ismark = false;
			if (q != null) {
				sb.append("?");
				sb.append(Utils.textToJscript(q));
				ismark = true;
			}
			// 来自HtmlControl的参数放到 PageValueTag.HTML_CONTROL_PARAS 中
			MTable paras = rv.getPageValues().getTagValues(PageValueTag.HTML_CONTROL_PARAS);
			if (paras != null) {
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
					if (ismark) {
						sb.append("&");
					} else {
						sb.append("?");
						ismark = true;
					}
					sb.append(encodeUrl(key));
					sb.append("=");
					sb.append(encodeUrl(sval));
				}
			}

			url = sb.toString();
		}
		return url == null ? null : url.replace("@", "%40");

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
		String ewa_added_resources = rv.s("ewa_added_resources");
		List<ConfAddedResource> al = ConfAddedResources.getInstance().getResList(ewa_added_resources, true);

		MStr sbCss = new MStr();
		MStr sbJs = new MStr();
		for (int i = 0; i < al.size(); i++) {
			ConfAddedResource r = al.get(i);
			if (r.getSrc().toLowerCase().endsWith(".css")) {
				LOGGER.warn("The css put on bottom, " + r.getSrc());
				sbCss.al(r.toCss());
			} else {
				sbJs.al(r.toJs());
			}
		}
		doc.addBodyHtml(sbCss.toString(), false);
		doc.addBodyHtml(sbJs.toString(), false);
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
				} catch (Exception err) {
					stOne = tag + s0;
					stSplit = tag1 + s0;
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
		if (rv.s("EWA_MTYPE") != null) {
			String EWA_MTYPE_tag = "EWA_MTYPE_" + rv.s("EWA_MTYPE").toUpperCase();
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

		if (uc.getUserPageItem().testName("PageAttributeSet")) {
			MStr sbPageAttr = new MStr();
			UserXItemValues u = uc.getUserPageItem().getItem("PageAttributeSet");
			for (int i = 0; i < u.count(); i++) {
				UserXItemValue u0 = u.getItem(i);
				String n = u0.getItem("PageAttName").trim();
				String v = u0.getItem("PageAttValue").trim();
				if (n.length() == 0 || v.length() == 0) {
					continue;
				}
				sbPageAttr.append(" " + n + "=\"" + Utils.textToInputValue(v) + "\"");
			}
			bs = bs.replace("!!>", sbPageAttr.toString() + ">");
		}
		doc.addBodyHtml(bs, true);
		doc.addScriptHtml("<div id='EWA_FRAME_MAIN' _s='主框架开始'>", "主框架开始");

		this.createH5FrameSet();
		if (this._Html5FrameSet != null) {
			doc.addScriptHtml(_Html5FrameSet[0]);
		}
		if (this._HtmlClass.getSysParas().isPc() && !this._HtmlClass.getUserConfig().getUserPageItem()
				.getSingleValue("FrameTag").equalsIgnoreCase("Complex")) {

			// 页面的方向
			String sizeStyle = "<table id='" + skinName + "' border='0' cellspacing='0' cellpadding='0' style='";
			String sizeW = this.getPageItemValue("Size", "Width");
			String sizeH = this.getPageItemValue("Size", "Height");
			String sizeVAlign = this.getPageItemValue("Size", "VAlign");
			String sizeHAlign = this.getPageItemValue("Size", "HAlign");
			String tdH = "";
			String mainWidth = null;

			// 用户参数指定宽度
			if (rv.s("EWA_WIDTH") != null) {
				sizeW = rv.s("EWA_WIDTH").replace("'", "").replace("\"", "").replace(">", "").replace("<", "");
			}
			// 用户参数指定的高度
			if (rv.s("EWA_HEIGHT") != null) {
				sizeH = rv.s("EWA_HEIGHT").replace("'", "").replace("\"", "").replace(">", "").replace("<", "");
			}
			if (sizeW != null && sizeW.trim().length() > 0) {
				try {
					int w = Integer.parseInt(sizeW);
					mainWidth = "width:" + w + "px; ";
					sizeStyle += "width:" + w + "px; ";
				} catch (Exception e) {
					mainWidth = "width:" + sizeW + "; ";
					sizeStyle += "width:" + sizeW + "; ";
				}
				String ft = this._HtmlClass.getSysParas().getFrameType();
				if (ft.equals("COMBINE")) {
					doc.addCss(".ewa_cb_box{" + mainWidth + "}");
				}
			} else {
				sizeStyle += "width:100%; ";
			}
			if (sizeH != null && sizeH.trim().length() > 0) {
				try {
					int h = Integer.parseInt(sizeH);
					sizeStyle += "height: " + h + "px; ";
				} catch (Exception e) {
					sizeStyle += "height: " + sizeH + "; ";
				}
				tdH = " height='100%' ";
			}
			sizeStyle += "' ";
			if (sizeHAlign != null && sizeHAlign.trim().length() > 0) {
				sizeStyle += "align='" + sizeHAlign + "'";
			}
			sizeStyle += ">\r\n<tr>\r\n<td " + tdH;
			if (sizeVAlign != null && sizeVAlign.trim().length() > 0) {
				sizeStyle += " vAlign='" + sizeVAlign + "'";
			} else {
				sizeStyle += " vAlign='top'";
			}
			sizeStyle += ">";
			doc.addScriptHtml(sizeStyle);
		}
		doc.addScriptHtml(this._HtmlClass.getSkinFrameAll().getTop());
		// sb.append("<div _s='内容窗体开始'>");

	}

	/**
	 * 生成主底部 AddHtml-Bottom
	 * 
	 * @throws Exception
	 */
	public void createSkinBottom() throws Exception {
		String pageAddBottom = this.getPageItemValue("AddHtml", "Bottom");
		if (pageAddBottom != null && pageAddBottom.trim().length() > 0) {
			this._HtmlClass.getDocument().addScriptHtml(pageAddBottom);
			this._HtmlClass.getDocument().addFrameHtml(pageAddBottom);
		}
		if (this._HtmlClass.getSysParas().isPc() && !this._HtmlClass.getUserConfig().getUserPageItem()
				.getSingleValue("FrameTag").equalsIgnoreCase("Complex")) {
			this._HtmlClass.getDocument().addScriptHtml("</td></tr></table><!--浮动表结束-->");
		}
		if (this._Html5FrameSet != null) {
			this._HtmlClass.getDocument().addScriptHtml(_Html5FrameSet[1]);
		}
		this._HtmlClass.getDocument().addScriptHtml("</div><!-- 主框架结束 -->");
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

	boolean isShowHeader() {
		String a = this.getPageItemValue("Size", "HiddenCaption");
		if (a != null && a.trim().equals("1")) { // 不显示列头
			return false;
		} else {
			return true;
		}
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
	 * @see com.gdxsoft.easyweb.script.display.IFrame1#setItemParentHtmls(java.util .HashMap)
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
	};
}
