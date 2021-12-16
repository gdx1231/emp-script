/**
 * 
 */
package com.gdxsoft.easyweb.script.display.items;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.gdxsoft.easyweb.script.HtmlControl;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlUtils;
import com.gdxsoft.easyweb.script.display.SysParameters;
import com.gdxsoft.easyweb.script.template.SkinFrame;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * @author Administrator
 * 
 */
public class ItemComplex extends ItemBase {
	private RequestValue _Rv;

	public String createItemHtml() throws Exception {
		String lang = this.getHtmlClass().getSysParas().getLang();

		UserXItem userXItem = super.getUserXItem();
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		RequestValue rv1 = sysParas.getRequestValue();
		this._Rv = rv1;

		String id = userXItem.getName();
		String xmlName = userXItem.getSingleValue("CombineFrame", "CbXmlName");
		xmlName = super.getHtmlClass().getItemValues().replaceJsParameters(xmlName);

		String itemName = userXItem.getSingleValue("CombineFrame", "CbItemName");

		String refParas = userXItem.getSingleValue("CombineFrame", "CbPara");
		refParas = super.getHtmlClass().getItemValues().replaceJsParameters(refParas);

		String install = userXItem.getSingleValue("CombineFrame", "CbInstall");

		String js = userXItem.getSingleValue("CombineFrame", "CbJs");

		String js_rename = userXItem.getSingleValue("CombineFrame", "CbJsRename");

		// boolean isHis = his != null && his.equals("1");

		String des = HtmlUtils.getDescription(userXItem.getItem("DescriptionSet"), "Info", lang);// 描述

		if (js == null) {
			js = "";
		} else {
			js = super.getHtmlClass().getItemValues().replaceParameters(js, false);
		}
		String x = xmlName;
		String i = itemName;
		String p = refParas;

		String s1 = super.getXItemFrameHtml();
		s1 = s1.replace("@DES", des);

		if (i == null || i.trim().length() == 0) {
			// nothing
		} else if (install.equals("html")) {
			String html = this.createHtml(id, x, i, p, des, js_rename);
			s1 = s1.replace(SkinFrame.TAG_VAL, html == null ? "" : html.replace("@", "\1\2$$##GDX~##JZY$$\3\4")); // 替换值
		} else if (install.equals("iframe")) {
			String rst = this.createIframe(id, x, i, p, des);
			s1 = s1.replace(">" + SkinFrame.TAG_VAL, rst + " >");

		} else if (install.equals("iframe_lazy")) {
			String rst = this.createIframe(id, x, i, p, des);
			s1 = s1.replace(">" + SkinFrame.TAG_VAL, rst + " >");

		} else if (install.equals("box")) {
			String p1 = p + "&EWA_BOX=1";
			String html = this.createJs(id, x, i, p1, js);
			s1 = s1.replace(">" + SkinFrame.TAG_VAL, html + " >");
		} else { // js
			String html = this.createJs(id, x, i, p, js);

			s1 = s1.replace(">" + SkinFrame.TAG_VAL, html + " >");
		}

		if (install.equals("iframe") || install.equals("iframe_lazy")) {
			s1 = s1.replace("[$]", "onclick=EWA.F.FOS['@SYS_FRAME_UNID'].actIframe('" + id + "')");

		}
		return s1.trim();
	}

	private String createJs(String id, String x, String i, String p, String js) throws JSONException {

		String params = replaceParameters(p);

		MStr s = new MStr();

		s.a(" x=\"" + Utils.textToJscript(x) + "\"");
		s.a(" i=\"" + Utils.textToJscript(i) + "\"");
		s.a(" p=\"" + Utils.textToJscript(params) + "\"");
		if (js != null && js.toUpperCase().trim().length() > 0) {
			s.a("js=\"" + Utils.textToJscript(js) + "\"");
		}
		return s.toString();
	}

	private String createHtml(String id, String x, String i, String p, String des, String js_rename) {

		boolean isJsRename = js_rename != null && js_rename.trim().length() > 0;

		String params = replaceParameters(p);

		MStr s = new MStr();
		HtmlControl ht = new HtmlControl();
		if (params == null || params.trim().length() == 0) {
			params = "_xxx0099=1";
		}
		params = params + "&EWA_AJAX=INSTALL&EWA_FRAMESET_NO=1&EWA_CALL_METHOD=INNER_CALL&COMBINE_ID=" + id;
		ht.init(x, i, params, _Rv.getRequest(), _Rv.getSession(), super.getHtmlClass().getResponse());
		String html = ht.getHtml();
		if (isJsRename) {
			Pattern pat = Pattern.compile("function[ \\t\\n\\r]+\\w+[ \\t\\n\\r]{0,}\\(", Pattern.CASE_INSENSITIVE);
			Matcher mat = pat.matcher(html);

			String newJsTag = id.replace("-", "_").trim();
			while (mat.find()) {
				MatchResult mr = mat.toMatchResult();
				String jsName = mr.group();
				if (jsName.indexOf("EWA") == 0) {
					continue;
				}
				String newJsName = jsName.replace("(", newJsTag + "(");
				html = html.replace(jsName + "(", newJsName + "(");

				String findName = jsName.replace("function", "").replace("(", "").trim();
				String newJsName1 = findName + newJsTag;
				html = html.replace(findName + "(", newJsName1 + "(");
				html = html.replace(findName + " (", newJsName1 + "(");
				html = html.replace("=" + findName + ";", "=" + newJsName1 + ";");
				html = html.replace("=" + findName + " ;", "=" + newJsName1 + ";");
				html = html.replace("(" + findName, "(" + newJsName1);
				html = html.replace(findName + ")", newJsName1 + ")");
			}
		}
		s.al(html);
		return s.toString();
	}

	private String createIframe(String id, String x, String i, String p, String des) throws JSONException {

		String params = replaceParameters(p);

		String html = " x='' u='" + _Rv.getContextPath() + "/EWA_STYLE/cgi-bin/?xmlname=" + x
				+ "&ewa_debug_no=1&itemname=" + i + "&" + params + "'";

		return html;
	}

	private String replaceParameters(String s) {
		return super.getHtmlClass().getItemValues().replaceParameters(s, false);
	}
}
