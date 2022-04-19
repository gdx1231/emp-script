package com.gdxsoft.easyweb.script.display.items;

/**
 * 用于开关元素的渲染
 * @author 郭磊
 */
public class ItemSwitch extends ItemBase {
	public String createItemHtml() throws Exception {
		String html = super.createItemHtml().trim();

		String val = super.getValue();
		//打开开关的值
		String swtOnValue = super.getUserXItem().getSingleValue("Switch", "SwtOnValue");
		if (swtOnValue != null && swtOnValue.length() > 0 && swtOnValue.equalsIgnoreCase(val)) {
			html = html.replace("[checked]", "checked");
		} else {
			html = html.replace("[checked]", "");
		}

		return html;
	}
}
