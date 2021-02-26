package com.gdxsoft.easyweb.script.display.frame;

import java.util.HashMap;

import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class FrameCombine extends FrameBase implements IFrame {
	private RequestValue _Rv;
	private String init_grp;
	private HashMap<String, String> _Mearge = new HashMap<String, String>();
	private String _InstallHtmls = "";
	private String _InstallJss = "";
	MStr _TopNav = new MStr();

	public void createContent() throws Exception {
		String cnt = createItemHtmls();

		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		if (pageAddTop.indexOf("<!--123-->") > 0) {
			pageAddTop = pageAddTop.replace("<!--123-->", _TopNav.toString());
		}
		this.getHtmlClass().getDocument().addScriptHtml(pageAddTop);

		this.getHtmlClass().getDocument().addScriptHtml(cnt);

		String pageAddBottom = this.getPageItemValue("AddHtml", "Bottom");
		this.getHtmlClass().getDocument().addScriptHtml(pageAddBottom);

	}

	public void createFrameContent() throws Exception {

	}

	public void createFrameFooter() throws Exception {

	}

	public String createFrameHeader() throws Exception {
		return null;
	}

	public void createHtml() throws Exception {
		_Rv = super.getHtmlClass().getSysParas().getRequestValue();
		init_grp = _Rv.getString("EWA_INIT_GRP");
		if (init_grp == null) {
			init_grp = "";
		} else {
			init_grp = init_grp.trim().toUpperCase();
		}

		super.createSkinTop();
		super.createCss();
		super.createJsTop();

		// content
		this.createContent();

		// Frame脚本
		this.createJsFramePage();
		this.createSkinBottom();
		this.createJsBottom();

	}

	public String createItemHtmls() throws Exception {
		String lang = this.getHtmlClass().getSysParas().getLang();
		MStr sb = new MStr();
		UserXItems items = super.getHtmlClass().getUserConfig().getUserXItems();
		for (int i = 0; i < items.count(); i++) {
			UserXItem uxi = items.getItem(i);
			String grp = uxi.getSingleValue("CombineFrame", "CbGrp");
			String id = uxi.getName();

			String des = HtmlUtils.getDescription(
					uxi.getItem("DescriptionSet"), "Info", lang);// 描述

			String all = uxi.getSingleValue("CombineFrame", "CbAll");
			boolean isAll = (all != null && all.trim().length() > 0);
			if (!isAll) {
				// 如果没设定组，则用id表示组
				if (grp == null || grp.trim().length() == 0) {
					grp = id.toUpperCase();
				} else {
					grp = grp.trim().toUpperCase();
				}

				// 如果参数没有设定，则用第一个作为组名
				if (this.init_grp.length() == 0) {
					this.init_grp = grp.toUpperCase();
				}
			}
			if (isAll || grp.equals(init_grp)) {
				String mearge = uxi.getSingleValue("CombineFrame", "CbMearge");
				String install = uxi
						.getSingleValue("CombineFrame", "CbInstall");

				IItem item = super.getHtmlClass().getItem(uxi);
				String itemHtml = item.createItemHtml();
				itemHtml = itemHtml.replace("@Des", des);
				if (!isAll) {
					boolean isMeager = this.addToMearge(mearge, id);
					if (isMeager) {
						itemHtml = itemHtml.replaceFirst("<div",
								"<div style='display:none'");
					}
				}
				if (install.equalsIgnoreCase("html")) {
					String h1 = (this._InstallHtmls.length() == 0 ? "" : ",")
							+ "\"" + Utils.textToJscript(id) + "\"";
					this._InstallHtmls += h1;
					sb.al(itemHtml);
				} else if (install.equalsIgnoreCase("js")) {
					int loc0 = itemHtml.indexOf("{");
					int loc1 = itemHtml.indexOf("}", loc0 + 1);
					String js = itemHtml.substring(loc0, loc1 + 1);
					String html = itemHtml.replace(js, "");
					String h1 = (this._InstallJss.length() == 0 ? "" : ",")
							+ "" + js + "";
					this._InstallJss += h1;
					sb.al(html);
				} else {
					sb.al(itemHtml);
				}
				if (_TopNav.indexOf("[" + grp + "]") < 0 && !isAll) {
					String lnk = "<a [" + grp
							+ "] class='crm_main_nav1 crm_main_nav1_cur'>"
							+ des + "</a>";
					_TopNav.al(lnk);
				}
			} else {
				if (_TopNav.indexOf("[" + grp + "]") < 0) {
					String q = createQueryParameters("EWA_INIT_GRP");
					String lnk = "<a  [" + grp
							+ "] class='crm_main_nav1' href='./?" + q
							+ "&ewa_init_grp=" + grp + "'>" + des + "</a>";
					_TopNav.al(lnk);
				}
			}
		}
		if (sb.indexOf("@") > 0) { // 替换未替换的值
			return super.getHtmlClass().getItemValues().replaceParameters(
					sb.toString(), false);
		} else {
			return sb.toString();
		}
	}

	private boolean addToMearge(String mearge, String id) {
		if (mearge.trim().length() == 0) {
			return false;
		}
		if (!_Mearge.containsKey(mearge)) {
			_Mearge.put(mearge, Utils.textToJscript("DES_" + mearge) + ","
					+ Utils.textToJscript("DES_" + id));
		} else {
			String s = _Mearge.get(mearge) + ","
					+ Utils.textToJscript("DES_" + id);
			_Mearge.remove(mearge);
			_Mearge.put(mearge, s);
		}
		return true;
	}

	public void createJsFramePage() throws Exception {
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
		String lang = super.getHtmlClass().getSysParas().getLang();

		// 页面URL的JS表达式
		String url = super.getUrlJs();

		MStr sJs = new MStr();

		// super.createJsFrameXml(); // item描述XML字符串
		// super.createJsFrameMenu(); // menu描述XML字符串

		sJs.append("\r\nEWA.LANG='" + lang + "'; //page language\r\n");
		String funName = "EWA_CB_" + gunid + "()";

		sJs.a("\r\nfunction " + funName + "{\r\n");
		sJs.a("var o1=EWA.F.FOS['" + gunid + "']=new EWA_CombineClass();\r\n");
		sJs.a("o1._Id = '" + gunid + "';\r\n");
		sJs.a("o1.Url = \"" + url + "\";\r\n");
		sJs.al("o1.EWA_COMBINES=[" + this._InstallJss + "];");
		sJs.al("o1.EWA_COMBINES_HTML=[" + this._InstallHtmls + "];");
		for (String n : this._Mearge.keySet()) {
			String v = this._Mearge.get(n);

			sJs.al("o1.MEAGER_ITEMS.push(\"" + v + "\");");

		}
		sJs.a("o1._init();o1 = null;\r\n");
		sJs.a("}\r\n");
		sJs.a(funName + ";\r\n");

		this.getHtmlClass().getDocument().addJs("FRAME_JS", sJs.toString(),
				false);

	}

	public String createaXmlData() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private String createQueryParameters(String removeIds) {
		String[] ids = removeIds.split(",");
		MTable tb = new MTable();

		for (int ia = 0; ia < _Rv.getPageValues().getQueryValues().getCount(); ia++) {
			PageValue pv = (PageValue) _Rv.getPageValues().getQueryValues()
					.getByIndex(ia);
			boolean isRemove = false;
			for (int m = 0; m < ids.length; m++) {
				if (pv.getName().equalsIgnoreCase(ids[m].trim())) {
					isRemove = true;
					break;
				}
			}
			if (!isRemove) {
				tb.add(pv.getName(), pv.getValue());
			}
		}
		String q = tb.join("=", "&");
		return q;
	}
}
