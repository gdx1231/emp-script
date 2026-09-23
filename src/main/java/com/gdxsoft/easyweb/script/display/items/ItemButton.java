package com.gdxsoft.easyweb.script.display.items;

import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.utils.Utils;

public class ItemButton extends ItemBase {
	public String createItemHtml() throws Exception {
		String lang = super.getHtmlClass().getSysParas().getLang();
		UserXItem userXItem = super.getUserXItem();
		String des = HtmlUtils.getDescription(userXItem.getItem("DescriptionSet"), "Info", lang);// 描述
		String msg = HtmlUtils.getDescription(userXItem.getItem("DescriptionSet"), "Memo", lang);// 描述

		String s1 = super.getXItemFrameHtml();
		s1 = s1.replace(SkinFrame.TAG_DES, Utils.textToInputValue(des));
		s1 = s1.replace(SkinFrame.TAG_MSG, Utils.textToInputValue(msg));

		// 检查是否带图标
		String icon = null;
		if (super.getUserXItem().testName("DataItem") && super.getUserXItem().getItem("DataItem").count() > 0) {
			UserXItemValue dataItem = super.getUserXItem().getItem("DataItem").getItem(0);
			if (dataItem.testName("Icon")) {
				icon = dataItem.getItem("Icon");
			}

		}

		if (icon != null && icon.trim().length() > 0) {
			String for_id = super.getUserXItem().getItem("Name").getItem(0).getItem("Name");
			// 避免浏览器失去响应 this._lk
			String s2 = "<a rid=\"" + Utils.textToInputValue(for_id)
					+ "\" class='ewa-button-with-icon' onclick=\"if(!this._lk){this._lk=true;$(this).find('input').trigger('click');setTimeout(function(){this._lk=false},500);}\" title=\""
					+ Utils.textToInputValue(des) + "\">" + s1 + "</a>";
			return s2;
		}
		return s1.trim();
	}
}
