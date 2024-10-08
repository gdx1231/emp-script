/**
 * 
 */
package com.gdxsoft.easyweb.script.display.items;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.display.SysParameters;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;

/**
 * @author Administrator
 * 
 */
public class ItemEwaConfigItem extends ItemBase {
	private static Logger LOGGER = LoggerFactory.getLogger(ItemEwaConfigItem.class);

	private String _FrameUnidPrefix;

	/**
	 * FrameUnid 前缀修正名称，避免同一个对象多次调用
	 * 
	 * @return
	 */
	public String getFrameUnidPrefix() {
		return _FrameUnidPrefix;
	}

	/**
	 * FrameUnid 前缀修正名称，避免同一个对象多次调用
	 * 
	 * @param frameUnidPrefix
	 */
	public void setFrameUnidPrefix(String frameUnidPrefix) {
		this._FrameUnidPrefix = frameUnidPrefix;
	}

	public String createItemHtml() throws Exception {
		UserXItem userXItem = super.getUserXItem();
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		RequestValue rv1 = sysParas.getRequestValue();
		// RequestValue rv = new RequestValue(rv1.getRequest(), rv1.getSession());
		RequestValue rv = rv1.clone();

		if (sysParas.getFrameType().equalsIgnoreCase("listFrame")) {
			DTTable tb = super.getHtmlClass().getItemValues().getListFrameTable();
			DTRow row = tb.getCurRow();
			for (int i = 0; i < tb.getColumns().getCount(); i++) {
				rv.addValue(tb.getColumns().getColumn(i).getName(), row.getCell(i).getValue());
			}
		}
		String val;
		String xmlName = userXItem.getSingleValue("DefineFrame", "CallXmlName");
		xmlName = super.getHtmlClass().getItemValues().replaceParameters(xmlName, false, true);
		String itemName = userXItem.getSingleValue("DefineFrame", "CallItemName");

		String refParas = userXItem.getSingleValue("DefineFrame", "CallPara");
		refParas = super.getHtmlClass().getItemValues().replaceParameters(refParas, false, true);
		String[] paras = null;
		if (refParas.trim().length() > 0) {
			if (refParas.indexOf("=") > 0) {
				// 新方法 name=@user_name&phone=@user_phone
				rv.addValues(refParas, PageValueTag.HTML_CONTROL_PARAS);
			} else {
				// 老方法 name,phone
				paras = refParas.split(",");
				for (int i = 0; i < paras.length; i++) {
					String para = paras[i].trim();
					if (para.length() == 0) {
						continue;
					}
					// 只有参数名称，没有赋值
					String paraVal = super.getHtmlClass().getItemValues().getValue(para, para);
					rv.addValue(paras[i], paraVal, PageValueTag.QUERY_STRING);
				}
			}
		}
		rv.addOrUpdateValue(FrameParameters.EWA_AJAX, "TOP_CNT_BOTTOM");

		if (rv.s(FrameParameters.EWA_CALL_METHOD) == null) {// 调用模式，用于判断使用
			String callUrlMethod = userXItem.getSingleValue("DefineFrame", "CallUrlMethod");
			// INNER_CALL url = /xxx/EWA_STYLE/cgi-bin/?xmlname=...
			rv.addOrUpdateValue(FrameParameters.EWA_CALL_METHOD,
					StringUtils.isBlank(callUrlMethod) ? "INNER_CALL" : callUrlMethod);
		}
		// 移除EWA_P_BEHAVIOR 脚本，由父窗体带入
		rv.getPageValues().remove(FrameParameters.EWA_P_BEHAVIOR);

		rv.addOrUpdateValue(FrameParameters.XMLNAME, xmlName);
		rv.addOrUpdateValue(FrameParameters.ITEMNAME, itemName);

		try {

			HtmlCreator hc = new HtmlCreator();
			hc.getSysParas().setFrameUnidPrefix(this._FrameUnidPrefix);

			hc.setDebugFrames(super.getHtmlClass().getDebugFrames());

			hc.init(rv, super.getResponse());
			hc.createPageHtml();
			val = hc.getPageHtml();
			val = val.replace("@SYS_FRAME_UNID", rv.getString(FrameParameters.SYS_FRAME_UNID));

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			val = e.getMessage();
		}
		String s1 = super.getXItemFrameHtml();
		s1 = s1.replace(SkinFrame.TAG_VAL, val == null ? "" : val.replace("@", IItem.REP_AT_STR)); // 替换值
		return s1.trim();
	}

}
