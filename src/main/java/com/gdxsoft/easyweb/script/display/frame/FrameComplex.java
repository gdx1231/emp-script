package com.gdxsoft.easyweb.script.display.frame;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.items.IItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.utils.ULogic;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class FrameComplex extends FrameBase implements IFrame {
	private RequestValue _Rv;
	private String init_grp;
	private String _InstallHtmls = "";
	private MTable _HiddenFields = null;
	MStr _TopNav = new MStr();

	public void createContent() throws Exception {
		String skinTop=super.getHtmlClass().getSkinFrameCurrent().getTop();
		this.getHtmlClass().getDocument().addScriptHtml(skinTop);
		
		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		this.getHtmlClass().getDocument().addScriptHtml(pageAddTop);

		String cnt = createItemHtmls();
		this.getHtmlClass().getDocument().addScriptHtml(cnt);

		String pageAddBottom = this.getPageItemValue("AddHtml", "Bottom");
		this.getHtmlClass().getDocument().addScriptHtml(pageAddBottom);
		
		String skinBottom=super.getHtmlClass().getSkinFrameCurrent().getBottom();
		this.getHtmlClass().getDocument().addScriptHtml(skinBottom);
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
		//this.createSkinBottom();
		this.createJsBottom();

	}

	public String createItemHtmls() throws Exception {
		String lang = this.getHtmlClass().getSysParas().getLang();
		MStr sb = new MStr();
		UserXItems items = super.getHtmlClass().getUserConfig().getUserXItems();
		for (int i = 0; i < items.count(); i++) {
			UserXItem uxi = items.getItem(i);
			String id = uxi.getName();
			// 检查是否为隐含字段，在逻辑控制页面生成
			if (super.getHtmlClass().getSysParas().isHiddenColumn(id)) {
				continue;
			}

			// 检查是否为隐含字段，在Page的LogicShow中定义
			if (this.isHiddenField(id)) {
				continue;
			}
			
			String des = HtmlUtils.getDescription(
					uxi.getItem("DescriptionSet"), "Info", lang);// 描述


			String install = uxi.getSingleValue("CombineFrame", "CbInstall");

			IItem item = super.getHtmlClass().getItem(uxi);
			String itemHtml = item.createItemHtml();
			itemHtml = itemHtml.replace("@Des", des);
		 
			if (install.equalsIgnoreCase("html")) {
				String h1 = (this._InstallHtmls.length() == 0 ? "" : ",")
						+ "\"" + Utils.textToJscript(id) + "\"";
				this._InstallHtmls += h1;
				sb.al(itemHtml);
			} else if (install.equalsIgnoreCase("js")) {
				sb.al(itemHtml);
			} else {
				sb.al(itemHtml);
			}

		}
		// if (sb.indexOf("@") < 0) { // 替换未替换的值
		// return super.getHtmlClass().getItemValues().replaceParameters(
		// sb.toString(), false);
		// } else {
		return sb.toString();
		// }
	}
	/**
	 * 检查是否为隐含字段，在Page的LogicShow中定义
	 * 
	 * @param name
	 * @return
	 */
	boolean isHiddenField(String name) {
		if (_HiddenFields == null) {
			UserXItem page = super.getHtmlClass().getUserConfig().getUserPageItem();
			this._HiddenFields = new MTable();
			try {
				if (page.testName("LogicShow")) {
					UserXItemValues logicShows = page.getItem("LogicShow");
					for (int i = 0; i < logicShows.count(); i++) {
						UserXItemValue logicShow = logicShows.getItem(i);
						// String name = logicShow.getItem("Name");
						String paraExp = logicShow.getItem("ParaExp");
						paraExp = super.getHtmlClass().getItemValues().replaceParameters(paraExp, false);
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
						// break;
					}
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		if (this._HiddenFields.getCount() == 0) {
			return false;
		}
		String name1 = name.trim().toUpperCase();
		return this._HiddenFields.containsKey(name1);
	}
	 
	public void createJsFramePage() throws Exception {
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
		String lang = super.getHtmlClass().getSysParas().getLang();

		// 页面URL的JS表达式
		String url = super.getUrlJs();
		super.createJsFrameXml(); // item描述XML字符串
		super.createJsFrameMenu(); // menu描述XML字符串
		
		MStr sJs = new MStr();

		// super.createJsFrameXml(); // item描述XML字符串
		// super.createJsFrameMenu(); // menu描述XML字符串

		sJs.append("\r\nEWA.LANG='" + lang + "'; //page language\r\n");
		String funName = "EWA_CB_" + gunid + "()";

		sJs.a("\r\nfunction " + funName + "{\r\n");
		sJs.a("var o1=EWA.F.FOS['" + gunid + "']=new EWA_ComplexClass();\r\n");
		sJs.a("o1._Id = '" + gunid + "';\r\n");
		sJs.a("o1.Url = \"" + url + "\";\r\n");
		 
		sJs.a("o1._init();o1 = null;\r\n");
		sJs.a("}\r\n");
		sJs.a(funName + ";\r\n");

		this.getHtmlClass().getDocument()
				.addJs("FRAME_JS", sJs.toString(), false);

	}

	public String createaXmlData() throws Exception {
		return null;
	}

	 
}
