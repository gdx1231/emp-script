package com.gdxsoft.easyweb.script.display.items;

import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;

public class ItemButton extends ItemBase {
	public String createItemHtml() throws Exception {
		String lang = super.getHtmlClass().getSysParas().getLang();
		UserXItem userXItem = super.getUserXItem();
		String des = HtmlUtils.getDescription(userXItem
				.getItem("DescriptionSet"), "Info", lang);// 描述
		String msg = HtmlUtils.getDescription(userXItem
				.getItem("DescriptionSet"), "Memo", lang);// 描述
		String s1 = super.getXItemFrameHtml();
		s1 = s1.replace(SkinFrame.TAG_DES, des);
		s1 = s1.replace(SkinFrame.TAG_MSG, msg);

		
		return s1.trim();
	}
}
