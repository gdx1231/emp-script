/**
 * 
 */
package com.gdxsoft.easyweb.script.display.items;

import java.util.HashMap;

import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.SysParameters;
import com.gdxsoft.easyweb.script.html.HtmlDocument;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.template.XItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.utils.msnet.MTable;

/**
 * @author Administrator
 * 
 */
public class ItemUserControl extends ItemBase {
	public String createItemHtml() throws Exception {
		UserXItem userXItem = super.getUserXItem();
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		RequestValue rv1 = sysParas.getRequestValue();
		RequestValue rv = new RequestValue(rv1.getRequest(), rv1.getSession());

		/*
		 * <Value Type="string" Name="UCCallXmlName"> <DescriptionSet> <Set
		 * Lang="zhcn" Info="配置文件" /> <Set Lang="enus" Info="Xml File Name" />
		 * </DescriptionSet> </Value> <Value Type="string"
		 * Name="UCCallItemName"> <DescriptionSet> <Set Lang="zhcn" Info="配置名称"
		 * /> <Set Lang="enus" Info="Item Name" /> </DescriptionSet> </Value>
		 * <Value Type="string" Name="UCCallItem"> <DescriptionSet> <Set
		 * Lang="zhcn" Info="引用条目" /> <Set Lang="enus" Info="Item" />
		 * </DescriptionSet> </Value> <Value Type="string" Name="UCCallPara">
		 * <DescriptionSet> <Set Lang="zhcn" Info="传递本值参数" /> <Set Lang="enus"
		 * Info="Parameters" /> </DescriptionSet> </Value>
		 */
		HashMap<String, Object> cache = super.getHtmlClass().getSysParas()
				.getCacheAny();

		String val;
		try {

			String xmlName = userXItem.getSingleValue("UserControl",
					"UCCallXmlName");
			String itemName = userXItem.getSingleValue("UserControl",
					"UCCallItemName");
			String refParas = userXItem.getSingleValue("UserControl",
					"UCCallPara");
			String UCCallItem = userXItem.getSingleValue("UserControl",
					"UCCallItem");

			if (refParas.trim().length() > 0) {
				String[] paras = refParas.split("&");
				for (int i = 0; i < paras.length; i++) {
					String p = paras[i].trim();
					String[] p1 = p.split("=");
					if (p1.length == 2) {
						String paraVal = p1[1].trim();
						if (paraVal.indexOf("@") == 0) {
							paraVal = super
									.getHtmlClass()
									.getItemValues()
									.getValue(paraVal.replace("@", ""),
											paraVal.replace("@", ""));
						}

						if (paraVal != null) {
							rv.addValue(p1[0], paraVal,
									PageValueTag.QUERY_STRING);
						}

					}
				}
			}

			HtmlDocument doc = getDoc(xmlName, itemName.toString(), rv, cache);
			val = doc.getItems().get(UCCallItem);
			RequestValue rvCache = (RequestValue) cache.get(xmlName + "?"
					+ itemName + "_rv");
			val = val.replace("@SYS_FRAME_UNID",
					rvCache.getString("SYS_FRAME_UNID"));
			val = val.replace("@sys_frame_unid",
					rvCache.getString("SYS_FRAME_UNID"));
			val = val.replace("id=\"",
					" ucuid='" + rvCache.getString("SYS_FRAME_UNID")
							+ "' ucfid=\"" + rv1.getString("SYS_FRAME_UNID")
							+ "\" id=\"" + userXItem.getName() + "\" ucid=\"");

		} catch (Exception e) {
			val = e.getMessage();
		}
		String s1 = super.getXItemFrameHtml();
		s1 = s1.replace(SkinFrame.TAG_VAL,
				val == null ? "" : val.replace("@", "\1\2$$##GDX~##JZY$$\3\4")); // 替换值
		return s1.trim();
	}

	private HtmlDocument getDoc(String xmlName, String itemName,
			RequestValue rv, HashMap<String, Object> cache) throws Exception {
		String key = xmlName + "?" + itemName;
		if (cache.containsKey(key)) {
			return (HtmlDocument) cache.get(key);
		}

		rv.addValue("EWA_AJAX", "TOP_CNT_BOTTOM");
		// 调用模式，用于判断使用
		rv.addValue("EWA_CALL_METHOD", "INNER_CALL");

		// 移除EWA_P_BEHAVIOR 脚本，由父窗体带入
		rv.getPageValues().remove("EWA_P_BEHAVIOR");
		rv.getPageValues().remove("EWA_REDRAW");
		rv.addValue("XMLNAME", xmlName);
		rv.addValue("itemName", itemName);

		SysParameters sysParas = super.getHtmlClass().getSysParas();
		RequestValue rv1 = sysParas.getRequestValue();
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < super.getHtmlClass().getUserConfig()
				.getUserXItems().count(); i++) {
			UserXItem uxi = super.getHtmlClass().getUserConfig()
					.getUserXItems().getItem(i);
			XItem xItem = HtmlUtils.getXItem(uxi);
			String tag = xItem.getName();
			if (tag.equalsIgnoreCase("UserControl")) {
				String xmlName1 = uxi.getSingleValue("UserControl",
						"UCCallXmlName");
				String itemName1 = uxi.getSingleValue("UserControl",
						"UCCallItemName");

				if (xmlName1.equals(xmlName) && itemName1.equals(itemName)) {
					String UCCallItem1 = uxi.getSingleValue("UserControl",
							"UCCallItem");
					map.put(uxi.getName().toUpperCase(), UCCallItem1);
				}
			}
		}

		MTable tv = rv1.getPageValues().getTagValues(PageValueTag.DTTABLE);
		for (int i = 0; i < tv.getCount(); i++) {
			String fName = tv.getKey(i).toString().toUpperCase();
			PageValue v = (PageValue) tv.get(fName);

			rv.addValue(fName, v.getValue(), PageValueTag.DTTABLE);
			if (map.containsKey(fName)) {
				rv.addValue(map.get(fName), v.getValue(), PageValueTag.DTTABLE);
			}
		}

		HtmlCreator hc = new HtmlCreator();
		hc.setDebugFrames(super.getHtmlClass().getDebugFrames());
		hc.init(rv, super.getResponse());
		hc.createPageHtml();
		HtmlDocument doc = hc.getDocument();
		cache.put(key, doc);
		cache.put(key + "_rv", rv);
		String jsTop = doc.getJsTop().getScripts(false);
		String jsBottom = doc.getJsBottom().getScripts(false);

		if (jsTop.trim().length() > 0) {
			jsTop = hc.getHtmlClass().getItemValues()
					.replaceParameters(jsTop, false, true);
			super.getHtmlClass().getDocument().getJsTop()
					.addScript(key + "_top", jsTop);
		}
		if (jsBottom.trim().length() > 0) {
			jsBottom = hc.getHtmlClass().getItemValues()
					.replaceParameters(jsBottom, false, true);
			super.getHtmlClass().getDocument().getJsBottom()
					.addScript(key + "_bottom", jsBottom);
		}

		String htmlTop = doc.getBodyTop().toString();
		String htmlBottom = doc.getBodyBottom().toString();
		if (htmlTop.trim().length() > 0) {
			htmlTop = hc.getHtmlClass().getItemValues()
					.replaceParameters(htmlTop, false, true);
			super.getHtmlClass().getDocument().addBodyHtml(htmlTop, true);
		}
		if (htmlBottom.trim().length() > 0) {
			htmlBottom = hc.getHtmlClass().getItemValues()
					.replaceParameters(htmlBottom, false, true);
			super.getHtmlClass().getDocument().addBodyHtml(htmlBottom, false);
		}
		return doc;
	}
}
