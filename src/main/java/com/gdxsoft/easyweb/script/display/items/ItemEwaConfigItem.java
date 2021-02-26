/**
 * 
 */
package com.gdxsoft.easyweb.script.display.items;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.display.SysParameters;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;

/**
 * @author Administrator
 * 
 */
public class ItemEwaConfigItem extends ItemBase {
	public String createItemHtml() throws Exception {
		UserXItem userXItem = super.getUserXItem();
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		RequestValue rv1 = sysParas.getRequestValue();
		RequestValue rv = new RequestValue(rv1.getRequest(), rv1.getSession());

		if (sysParas.getFrameType().equalsIgnoreCase("listFrame")) {
			DTTable tb = super.getHtmlClass().getItemValues()
					.getListFrameTable();
			DTRow row = tb.getCurRow();
			for (int i = 0; i < tb.getColumns().getCount(); i++) {
				rv.addValue(tb.getColumns().getColumn(i).getName(), row
						.getCell(i).getValue());
			}
		}
		String val;
		try {

			String xmlName = userXItem.getSingleValue("DefineFrame",
					"CallXmlName");
			String itemName = userXItem.getSingleValue("DefineFrame",
					"CallItemName");
			String refParas = userXItem.getSingleValue("DefineFrame",
					"CallPara");

			String[] paras = null;

			if (refParas.trim().length() > 0) {
				paras = userXItem.getSingleValue("DefineFrame", "CallPara")
						.split(",");
				for (int i = 0; i < paras.length; i++) {
					if (paras[i].trim().length() > 0) {
						String paraVal = super.getHtmlClass().getItemValues()
								.getValue(paras[i], paras[i]);
						rv.addValue(paras[i], paraVal,
								PageValueTag.QUERY_STRING);
					}
				}
			}

			rv.addValue("EWA_AJAX", "TOP_CNT_BOTTOM");
			// 调用模式，用于判断使用
			rv.addValue("EWA_CALL_METHOD", "INNER_CALL");

			// 移除EWA_P_BEHAVIOR 脚本，由父窗体带入
			rv.getPageValues().remove("EWA_P_BEHAVIOR");

			rv.addValue("XMLNAME", xmlName);
			rv.addValue("itemName", itemName);
			HtmlCreator hc = new HtmlCreator();
			hc.setDebugFrames(super.getHtmlClass().getDebugFrames());
			hc.init(rv, super.getResponse());
			hc.createPageHtml();
			val = hc.getPageHtml();
			val = val
					.replace("@SYS_FRAME_UNID", rv.getString("SYS_FRAME_UNID"));

		} catch (Exception e) {
			val = e.getMessage();
		}
		String s1 = super.getXItemFrameHtml();
		s1 = s1.replace(SkinFrame.TAG_VAL, val == null ? "" : val.replace("@",
				"\1\2$$##GDX~##JZY$$\3\4")); // 替换值
		return s1.trim();
	}
}
