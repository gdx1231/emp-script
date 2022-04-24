package com.gdxsoft.easyweb.script.display.items;

import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * 用于开关元素的渲染
 * 
 * @author 郭磊
 */
public class ItemSwitch extends ItemBase {
	/**
	 * 生成对象HTML
	 */

	public String createItemHtml() throws Exception {
		String html = super.getXItemFrameHtml();
		String val = super.getValue();
		if (val == null) {
			val = "";
		} else {
			val = val.trim();
		}
		// 打开开关的值
		String swtOnValue = super.getUserXItem().getSingleValue("Switch", "SwtOnValue");
		if (val.length() > 0 && val.equals(swtOnValue == null ? "" : swtOnValue.trim())) {
			html = html.replace("[checked]", "checked");
		} else {
			html = html.replace("[checked]", "");
		}
		// 替@
		html = html.replace(SkinFrame.TAG_VAL, Utils.textToInputValue(val.replace("@", REP_AT_STR)));

		return html;
	}
}
