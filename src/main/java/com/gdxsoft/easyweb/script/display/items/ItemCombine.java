/**
 * 
 */
package com.gdxsoft.easyweb.script.display.items;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class ItemCombine extends ItemBase {
	private RequestValue _Rv;

	public String createItemHtml() throws Exception {
		String lang = this.getHtmlClass().getSysParas().getLang();

		UserXItem userXItem = super.getUserXItem();
		SysParameters sysParas = super.getHtmlClass().getSysParas();
		RequestValue rv1 = sysParas.getRequestValue();
		this._Rv = rv1;

		String id = userXItem.getName();
		String xmlName = userXItem.getSingleValue("CombineFrame", "CbXmlName");
		String itemName = userXItem
				.getSingleValue("CombineFrame", "CbItemName");
		String refParas = userXItem.getSingleValue("CombineFrame", "CbPara");
		String install = userXItem.getSingleValue("CombineFrame", "CbInstall");

		String js = userXItem.getSingleValue("CombineFrame", "CbJs");

		String js_rename = userXItem.getSingleValue("CombineFrame",
				"CbJsRename");

		// boolean isHis = his != null && his.equals("1");

		String des = HtmlUtils.getDescription(userXItem
				.getItem("DescriptionSet"), "Info", lang);// 描述

		if (js == null) {
			js = "";
		} else {
			js = super.getHtmlClass().getItemValues().replaceParameters(js,
					false);
		}
		String x = xmlName;
		String i = itemName;
		String p = refParas;

		MStr s = new MStr();
		if (i == null || i.trim().length() == 0) {
			// nothing
		} else if (install.equals("html")) {
			String html = this.createHtml(id, x, i, p, des, js_rename);
			s.al(html);

		} else if (install.equals("iframe")) {
			String rst = this.createIframe(id, x, i, p, des);
			s.replace("[$]", "onclick=actIframe('" + id + "')");
			s.al(rst);

		} else if (install.equals("iframe_lazy")) {
			String rst = this.createIframe(id, x, i, p, des);
			rst = rst.replace("src='", "_src='");
			s.replace("[$]", "onclick=actIframe('" + id + "')");
			s.al(rst);

		} else if (install.equals("box")) {
			String lst = userXItem.getSingleValue("CombineFrame", "CbLst");
			String rst = this.createBox(id, x, i, p, des, lst);
			s.al(rst);
			s.replace("class='left'", "class='left1'");
			s.replace("[st1]", "style='margin-bottom:0px;'");
		} else { // js
			String rst = this.createJs(id, x, i, p, des);
			s.al(rst);
		}

		s.replace("[st1]", "");
		String val = s.toString();

		String s1 = super.getXItemFrameHtml();
		s1 = s1.replace("@DES", des);
		s1 = s1.replace(SkinFrame.TAG_VAL, val == null ? "" : val.replace("@",
				IItem.REP_AT_STR)); // 替换值

		if (install.equals("iframe") || install.equals("iframe_lazy")) {
			s1 = s1.replace("[$]",
					"onclick=EWA.F.FOS['@SYS_FRAME_UNID'].actIframe('" + id
							+ "')");

		}
		return s1.trim();
	}

	private String createJs(String id, String x, String i, String p, String des)
			throws JSONException {

		String params = replaceParameters(p);

		JSONObject json = new JSONObject();
		json.put("x", x);
		json.put("i", i);
		json.put("p", params + "&COMBINE_ID=" + id);
		json.put("id", id);
		json.put("des", des);
		json.put("install", "js");

		String rst = json.toString();
		return rst;
	}

	private String createHtml(String id, String x, String i, String p,
			String des, String js_rename) {

		boolean isJsRename = js_rename != null && js_rename.trim().length() > 0;

		String params = replaceParameters(p);

		MStr s = new MStr();
		HtmlControl ht = new HtmlControl();
		if (params == null || params.trim().length() == 0) {
			params = "_xxx0099=1";
		}
		params = params
				+ "&EWA_AJAX=INSTALL&EWA_FRAMESET_NO=1&EWA_CALL_METHOD=INNER_CALL&COMBINE_ID="
				+ id;
		ht.init(x, i, params, _Rv.getRequest(), _Rv.getSession(), super
				.getHtmlClass().getResponse());
		String html = ht.getHtml();
		if (isJsRename) {
			Pattern pat = Pattern.compile(
					"function[ \\t\\n\\r]+\\w+[ \\t\\n\\r]{0,}\\(",
					Pattern.CASE_INSENSITIVE);
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

				String findName = jsName.replace("function", "").replace("(",
						"").trim();
				String newJsName1 = findName + newJsTag;
				html = html.replace(findName + "(", newJsName1 + "(");
				html = html.replace(findName + " (", newJsName1 + "(");
				html = html.replace("=" + findName + ";", "=" + newJsName1
						+ ";");
				html = html.replace("=" + findName + " ;", "=" + newJsName1
						+ ";");
				html = html.replace("(" + findName, "(" + newJsName1);
				html = html.replace(findName + ")", newJsName1 + ")");
			}
		}
		s.al(html);
		return s.toString();
	}

	private String createIframe(String id, String x, String i, String p,
			String des) throws JSONException {

		String params = replaceParameters(p);

		String html = "<iframe id='__IF_"
				+ id
				+ "' frameborder=0 width=100% height=100% style='min-height:300px' scroll=no src='"
				+ _Rv.getContextPath() + "/EWA_STYLE/cgi-bin/?xmlname=" + x
				+ "&ewa_debug_no=1&itemname=" + i + "&" + params
				+ "'></iframe>";

		return html;
	}

	private String createBox(String id, String x, String i, String p,
			String des, String lst) throws JSONException {

		String params = replaceParameters(p);
		if (params.toUpperCase().indexOf("EWA_AJAX=JSON_EXT") < 0) {
			params += "&EWA_AJAX=JSON_EXT";
		}
		if (params.toUpperCase().indexOf("EWA_FRAMESET_NO=1") < 0) {
			params += "&EWA_FRAMESET_NO=1";
		}
		MStr s = new MStr();

		HtmlControl ht = new HtmlControl();
		ht.init(x, i, params + "&COMBINE_ID=" + id, _Rv, super.getHtmlClass()
				.getResponse());
		JSONObject o = new JSONObject(ht.getHtml());

		this.putJsonToRv(o);

		JSONArray cfgs = o.getJSONArray("CFG");
		JSONObject data = o.getJSONArray("DATA").getJSONObject(0);
		HashMap<String, JSONObject> map1 = new HashMap<String, JSONObject>();
		for (int ia = 0; ia < cfgs.length(); ia++) {
			JSONObject cfg = cfgs.getJSONObject(ia);
			String name = cfg.getString("NAME").toUpperCase();
			map1.put(name, cfg);
		}
		String[] lsts = lst.split("\\;");
		MStr s1 = new MStr();
		int cols = _Rv.getString("__CFG_full") == null
				|| _Rv.getString("__CFG_full").trim().length() == 0 ? 4 : 4;
		for (int ia = 0; ia < lsts.length; ia++) {
			String name2 = lsts[ia].trim();
			String[] names = name2.split("\\|");
			String name = names[0];
			String name1 = name.toUpperCase();
			if (ia == 0) {
				s1.a("<ul>");
			} else if (ia % cols == 0) {
				s1.a("</ul><ul>");
			}
			JSONObject cfg = map1.get(name1);
			String val = "";
			if (cfg.has("VAL")) {
				val = cfg.getString("VAL");
			}
			if (data.has(name + "_HTML")) {
				val = data.getString(name + "_HTML");
			}
			if (val.trim().length() == 0) {
				if (data.has(name)) {
					val = data.getString(name);
				}
			}

			String onclick = "";
			s1.a("<li class='am0' rid='" + name + "'>" + cfg.getString("DES")
					+ "</li>");
			if (names.length > 1) {
				String jsName = names[1].trim();
				if (jsName.indexOf("(") > 0) {
					onclick = " onclick=\"" + jsName + "\"";
				} else {
					if (map1.containsKey(jsName.toUpperCase())) {
						JSONObject objClick = map1.get(jsName.toUpperCase());
						if (objClick.has("ONCLICK")) {
							onclick = " onclick=\""
									+ Utils.textToInputValue(objClick
											.getString("ONCLICK")) + "\"";
						} else if (o.has("WF")) {
							JSONObject wf = o.getJSONObject("WF");
							JSONArray ids = wf.getJSONArray("RID");
							String rid = "";
							for (int ib = 0; ib < ids.length(); ib++) {
								String rid0 = ids.getString(ib);
								String vRid0 = _Rv.getString(rid0);
								if (ib > 0) {
									rid += ",";
								}
								rid += vRid0;
							}
							String wfParams = wf.getString("P").replace(
									"[RID]", rid);
							wfParams = this.replaceParameters(wfParams);
							wfParams = "combine_id=" + id + "&" + wfParams;
							String u = "EWA.UI.Dialog.OpenReloadClose('-1','"
									+ wf.getString("X") + "','"
									+ wf.getString("I") + "', false,\""
									+ wfParams + "\")";
							onclick = " onclick=\"" + Utils.textToInputValue(u)
									+ "\"";
						}
					}
				}
				s1.a("<li class='am1'><div class='am1_txt'>" + val
						+ "</div><a " + onclick
						+ "  class='am1_edit'><img src='"
						+ _Rv.getContextPath()
						+ "/images/pencil.png' /></a></li>");
			} else {
				s1.a("<li class='am1'>" + val + "</li>");
			}

		}
		s1.a("</ul>");
		if (o.has("JS")) {
			String pageJs = o.getString("JS");
			s1.al("<script>");
			s1.al(pageJs);
			s1.al("</script>");
		}
		s.al(s1.toString());

		return s.toString();
	}

	private void putJsonToRv(JSONObject o) {
		try {
			if (o.getJSONArray("DATA").length() > 0) {
				JSONObject row = o.getJSONArray("DATA").getJSONObject(0);
				Iterator<String> keys = row.keys();
				while (keys.hasNext()) {
					String name = keys.next();
					String v = row.getString(name);
					if (this._Rv.getString(name) == null) {
						this._Rv.addValue(name, v);
						// System.out.println("name="+name+", val="+v);
					}
				}
			}
		} catch (JSONException e) {
			return;
		}
	}

	private String replaceParameters(String s) {
		return super.getHtmlClass().getItemValues().replaceParameters(s, false);
	}
}
