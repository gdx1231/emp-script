package com.gdxsoft.easyweb.script.display.frame;

import org.w3c.dom.Document;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.display.MenuMain;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.msnet.MList;

/**
 * 菜单生成类
 * 
 * @author Administrator
 * 
 */
public class FrameMenu extends FrameBase implements IFrame {
	public void createJsFramePage() throws Exception {
		super.createJsFrameXml(); // item描述XML字符串
		super.createJsFrameMenu(); // menu描述XML字符串
	}

	public void createFrameContent() throws Exception {
		MenuMain mm = new MenuMain(super.getHtmlClass().getItemValues(), super
				.getHtmlClass().getUserConfig(), super.getHtmlClass()
				.getSysParas().getLang(), super.getHtmlClass().getSysParas()
				.getFrameUnid());
		 
		MList tbs = super.getHtmlClass().getItemValues().getDTTables();
		DTTable tb = (DTTable) tbs.get(tbs.size() - 1);
		String s1 = mm.createMenusHtml(tb);

		super.getHtmlClass().getDocument().addScriptHtml(s1);
	}

	public void createFrameFooter() throws Exception {
	}

	public String createFrameHeader() throws Exception {
		return "";
	}

	public void createHtml() throws Exception {
		super.createSkinTop();
		super.createCss();
		super.createJsTop();

		this.createContent();
		// Frame脚本
		this.createJsFramePage();
		this.createJsBottom();

		this.createSkinBottom();
	}

	public String createItemHtmls() throws Exception {
		return "";
	}

	public void createContent() throws Exception {
		HtmlDocument doc = this.getHtmlClass().getDocument();
		// 皮肤定义的头部
		doc.addScriptHtml(super.createSkinFCTop(), "皮肤定义的头部");

		// Frame定义的页头
		doc.addScriptHtml(createFrameHeader(), "Frame定义的页头");

		// 用户自定义头部html
		String pageAddTop = this.getPageItemValue("AddHtml", "Top");
		doc.addScriptHtml(pageAddTop == null ? "" : pageAddTop);

		// Frame内容
		createFrameContent();

		// Frame定义的页脚
		this.createFrameFooter();

		// 皮肤定义定义的尾部
		doc.addScriptHtml(super.createSkinFCBottom(), "皮肤定义定义的尾部");

	}

	public String createaXmlData() throws Exception {
		Document doc = super.createXmlDataDocument();
		return UXml.asXmlAll(doc);
	}
}
