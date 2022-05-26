package com.gdxsoft.easyweb.script.display.items;

import com.gdxsoft.easyweb.script.template.SkinFrame;

public class ItemUser extends ItemBase {
	public String createItemHtml() throws Exception {
		String lang = super.getHtmlClass().getSysParas().getLang();
		String userHtml = super.getUserXItem().getItemValue("UserSet", "Lang=" + lang, "User");
		if (userHtml.indexOf("UserSet=Lang=") >= 0) {
			userHtml = super.getUserXItem().getItemValue("UserSet", "Lang=zhcn", "User");
		}
		// 用户没有设定
		if (userHtml.indexOf("UserSet=Lang=") >= 0) {
			userHtml = "";
		}
		String val = super.getHtmlClass().getItemValues().replaceParameters(userHtml, true, true);
		if (val.toLowerCase().indexOf("<script") >= 0) {
			val = val.toLowerCase().replace("<script", "[脚本1").replace("</script", "[/脚本1");
		}
		String s1 = super.getXItemFrameHtml();
		// 替换 @ 为 \1\2$$##GDX~##JZY$$\3\4
		s1 = s1.replace(SkinFrame.TAG_VAL, val.replace("@", IItem.REP_AT_STR));
		return s1.trim();
	}
}
