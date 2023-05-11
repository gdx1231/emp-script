package com.gdxsoft.easyweb.script.display.frame;

import java.util.ArrayList;

import org.w3c.dom.Document;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.TreeOtherIcon;
import com.gdxsoft.easyweb.script.display.TreeViewMain;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class FrameTree extends FrameBase implements IFrame {
	private TreeViewMain _TreeViewMain;

	public TreeViewMain getTreeViewMain() {
		return _TreeViewMain;
	}

	public void createJsFramePage() throws Exception {
		// 页面脚本初始化ListFrame
		RequestValue rv = super.getHtmlClass().getSysParas().getRequestValue();

		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
		String lang = super.getHtmlClass().getSysParas().getLang();
		String url = super.getUrlJs();

		super.createJsFrameXml(); // item描述XML字符串
		super.createJsFrameMenu(); // menu描述XML字符串

		super.createJsTop();
		super.createJsBottom();

		MStr sJs = new MStr();
		sJs.append(this.createJsTreeIcon());
		sJs.append("\r\nEWA.LANG='" + lang + "'; //page language\r\n");
		String fName = "EWA.F.FOS[\"" + gunid+"\"]";
		String parent = "document.getElementById('EWA_TREE_" + gunid + "').parentNode";
		sJs.append("(function() {\n");
		sJs.append("var o = EWA.F.FOS['" + gunid + "'] = new EWA_TreeClass(" + parent + ",'" + fName + "',\"" + url
				+ "\");\n");
		sJs.append("o.InitMenu(_EWA_MENU_" + gunid + ");\n");
		sJs.append("o.Init(EWA_ITEMS_XML_" + gunid + ");\n");
		sJs.append("o.Icons = _EWA_ICONS_" + gunid + ";\n");

		// 附加字段
		if (this._TreeViewMain != null && this._TreeViewMain.getAddColsLength() > 0) {
			sJs.append("o.AddCols =\"" + Utils.textToJscript(this._TreeViewMain.getAddCols()) + "\";\n");
		}

		sJs.append("o._Id ='" + gunid + "';\n})();\n");

		// 初始化显示的值
		String k = rv.getString( FrameParameters.EWA_TREE_INIT_KEY);
		if (k != null && k.trim().length() > 0) {
			k = Utils.textToJscript(k);
			sJs.append("try{");
			sJs.append(fName + ".ShowNode(\"" + k + "\");\r\n");
			sJs.append("}catch(e){}\r\n");
		}

		String frameType = super.getPageItemValue("HtmlFrame", "FrameType");
		if (frameType != null && frameType.trim().length() > 0) {
			String subUrl = super.getPageItemValue("HtmlFrame", "FrameSubUrl");
			if (subUrl != null && subUrl.indexOf("FrameSubUrl") < 0 && subUrl.trim().length() > 0) {
				subUrl = subUrl.trim();
				sJs.al("function link(id){");
				sJs.al("window.parent.frames[1].location='" + subUrl + "'+id+'&'+$U();");
				sJs.al("}");
			}
		}
		this.getHtmlClass().getDocument().addJs("TREE", sJs.toString(), false);
	}

	/**
	 * 生成树的Icon的表达式
	 * 
	 * @return
	 */
	private String createJsTreeIcon() {
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
		MStr jsIcons = new MStr();
		jsIcons.append("var _EWA_ICONS_" + gunid + "={\r\n");
		ArrayList<TreeOtherIcon> icons = this._TreeViewMain.getIcons().getIcons();

		for (int i = 0; i < icons.size(); i++) {
			TreeOtherIcon icon = icons.get(i);
			String filter = icon.getFilter();
			String test = icon.getTest();
			if (i > 0) {
				jsIcons.append(",");
			}
			jsIcons.append("\tITEM_" + i + ": {TEST: '" + test + "', FILTER: \"" + filter + "\", NAME: 'F_" + gunid
					+ "_" + i + "_'}\r\n");
		}
		jsIcons.append("};\r\n");
		return jsIcons.toString();
	}

	/**
	 * 生成树附加ICON的CSS
	 * 
	 * @return
	 */
	private void createCssIcons() {
		MStr sb = new MStr();
		String gunid = super.getHtmlClass().getSysParas().getFrameUnid();
		ArrayList<TreeOtherIcon> icons = this._TreeViewMain.getIcons().getIcons();

		String skinName = getPageItemValue("SkinName", "SkinName");
		for (int i = 0; i < icons.size(); i++) {
			TreeOtherIcon icon = icons.get(i);
			String open = icon.getOpen();
			String close = icon.getClose();
			sb.append("#" + skinName + " .F_" + gunid + "_" + i + "_A{width:20px; height:18px; "
					+ "background-image:url('" + super.getHtmlClass().getItemValues().replaceParameters(open, true)
					+ "');" + "background-repeat:no-repeat ;}\r\n");
			sb.append("#" + skinName + " .F_" + gunid + "_" + i + "_B{width:20px; height:18px; "
					+ "background-image:url('" + super.getHtmlClass().getItemValues().replaceParameters(close, true)
					+ "');" + "background-repeat:no-repeat ;}\r\n");
		}
		this.getHtmlClass().getDocument().addCss(sb.toString());
	}

	public void createFrameContent() throws Exception {
		MStr sb = new MStr();

		if (this._TreeViewMain == null) {
			this._TreeViewMain = new TreeViewMain(super.getHtmlClass().getUserConfig(),
					super.getHtmlClass().getSysParas().getLang(), super.getHtmlClass().getSysParas().getFrameUnid());

		}

		// 设置附加字段内容 2019-03-26
		this._TreeViewMain.initCreateAddCols(this);

		String rootId = super.getHtmlClass().getSysParas().getRequestValue().getString(FrameParameters.EWA_TREE_ROOT_ID);
		if (rootId != null) {
			this._TreeViewMain.setRootId(rootId);
		}

		String s1 = "";
		MList tbs = super.getHtmlClass().getItemValues().getDTTables();
		DTTable tb = (DTTable) tbs.get(tbs.size() - 1);

		// 设置列表数据 附加字段获取数据用
		super.getHtmlClass().getItemValues().setListFrameTable(tb);
		
		s1 = _TreeViewMain.createTreeHtml(tb);
		sb.append(s1);
		this.getHtmlClass().getDocument().addScriptHtml(sb.toString());
	}

	public void createFrameFooter() throws Exception {
	}

	public String createFrameHeader() throws Exception {
		return "";
	}

	public void createHtml() throws Exception {
		super.addDebug(this, "TREE", "开始加载 TreeViewMain");

		_TreeViewMain = new TreeViewMain(super.getHtmlClass().getUserConfig(),
				super.getHtmlClass().getSysParas().getLang(), super.getHtmlClass().getSysParas().getFrameUnid());

		super.addDebug(this, "TREE", "加载 TreeViewMain 完成");

		super.createSkinTop();
		super.addDebug(this, "TREE", "SkinTop");

		super.createCss();
		super.addDebug(this, "TREE", "加载 Css");

		// 附加树的表达式
		this.createCssIcons();

		super.addDebug(this, "TREE", "加载 CssIcons");

		// 头部脚本
		super.createJsTop();

		super.addDebug(this, "TREE", "加载 Jstop");

		// 主内容
		super.addDebug(this, "TREE", "开始加载 主内容");

		this.createContent();

		super.addDebug(this, "TREE", "开始加载 主内容OK");

		// 底部
		this.createSkinBottom();
		// 底部脚本
		this.createJsBottom();
		// Frame脚本
		this.createJsFramePage();
	}

	public String createItemHtmls() throws Exception {
		return "";
	}

	public void createContent() throws Exception {
		HtmlDocument doc = this.getHtmlClass().getDocument();

		// 用户自定义头部html
		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop);

		// 皮肤定义的头部
		doc.addScriptHtml(super.createSkinFCTop());
		super.addDebug(this, "TREE", "皮肤定义的头部");

		// Frame定义的页头
		doc.addScriptHtml(createFrameHeader());
		super.addDebug(this, "TREE", "Frame定义的页头");

		// Frame内容
		createFrameContent();
		super.addDebug(this, "TREE", "Frame内容");
		// Frame定义的页脚
		this.createFrameFooter();
		super.addDebug(this, "TREE", "Frame定义的页脚");
		// 皮肤定义定义的尾部
		super.createSkinFCBottom();
		super.addDebug(this, "TREE", "皮肤定义定义的尾部");
	}

	public String createaXmlData() throws Exception {
		Document doc = super.createXmlDataDocument();
		MList tbs = super.getHtmlClass().getItemValues().getDTTables();
		if (tbs.size() == 0) {
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- no xml data -->";
		}
		DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
		if (this._TreeViewMain == null) {
			_TreeViewMain = new TreeViewMain(super.getHtmlClass().getUserConfig(),
					super.getHtmlClass().getSysParas().getLang(), super.getHtmlClass().getSysParas().getFrameUnid());
		}
		this._TreeViewMain.createTreeXml(tb, doc);
		String xml = UXml.asXmlAll(doc);
		return xml;
	}

}
