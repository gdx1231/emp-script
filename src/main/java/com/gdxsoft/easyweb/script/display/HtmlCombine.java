package com.gdxsoft.easyweb.script.display;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.HtmlControl;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class HtmlCombine {
	RequestValue _Rv;
	String _CombineCfgXml;
	HttpServletResponse _Response;
	MStr _Js = new MStr();
	MStr _Html = new MStr();
	MStr _TopNav = new MStr();

	private HashMap<String, String> _Items = new HashMap<String, String>();
	private HashMap<String, String> _Mearge = new HashMap<String, String>();
	private String _Title;
	private HashMap<String, DTRow> _AclMap;

	private boolean _IsEn;

	public HtmlCombine() {

	}

	public String getItem(String name) {
		String name1 = name.toUpperCase();
		return this._Items.get(name1);
	}

	public HashMap<String, String> getItems() {
		return this._Items;
	}

	public void init(RequestValue rv, String combineCfgXml, HttpServletResponse response)
			throws ParserConfigurationException, SAXException, IOException, JSONException {
		this._CombineCfgXml = combineCfgXml;
		this._Response = response;
		this._Rv = rv;

		if (rv.getString("SYS_EWA_LANG") != null && rv.getString("SYS_EWA_LANG").equalsIgnoreCase("enus")) {
			this._IsEn = true;
		} else {
			this._IsEn = false;
		}

		Document doc = UXml.retDocument(this._CombineCfgXml);

		Element root = (Element) doc.getElementsByTagName("root").item(0);
		_Title = root.getAttribute("title");
		String split = root.getAttribute("split");
		String full = root.getAttribute("full");
		rv.addValue("__CFG_SPLIT", split);
		rv.addValue("__CFG_full", full);
		String user_id = "";
		String xml = "";
		user_id = rv.getString("G_ADM_ID");
		// String unid = rv.getString("MENU_UNID");
		xml = rv.getString("XML");
		if (user_id == null || user_id == "") {
			return;
		}
		// boolean isSplit = split != null && split.equals("1");
		// isSplit = true;

		NodeList nl = doc.getElementsByTagName("item");
		_AclMap = this.getUserGrpItems("MENU_GRP_ITEM", xml);

		// 先循环，以便将数据放到Rv中
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (i == 0) {
				rv.addValue("__CFG_IDX", i);
			} else {
				rv.changeValue("__CFG_IDX", i, "int", 10);
			}

			Element et = (Element) nl.item(i);
			// if(!hasItem(user_id,unid,et.getAttribute("id")))
			// {
			// continue;
			// }

			if (!_AclMap.containsKey(et.getAttribute("id"))) {
				continue;
			}
			try {
				this.createItem(item);
			} catch (Exception err) {
				this._Html.al("<div>" + err + "</div>");

			}

		}

		// top html
		NodeList nla = doc.getElementsByTagName("htmlTop");
		if (nla.getLength() > 0 && this._Rv.getString("no_nav") == null) {
			String top = nla.item(0).getTextContent();
			top = top.replace("<!--123-->", this._TopNav.toString());
			top = this.replaceParameters(top);
			this._Html.insert(0, top);
		}

		NodeList nlb = doc.getElementsByTagName("htmlBottom");
		if (nlb.getLength() > 0) {
			String bottom = nlb.item(0).getTextContent();
			bottom = this.replaceParameters(bottom);
			this._Html.al(bottom);
		}
		if (full != null && full.trim().length() > 0) {
			String fullCss = "<style>#crm_main_box{width:1162px;margin-left:0px}#crm_main_nav{width:1162px;margin-left:0px}#crm_left_box{display:none;}.left{width:1132px;}</style>";
			this._Html.insert(0, fullCss);
		}

		String sqlHis = "SELECT TOP 10 * FROM ADM_HIS WHERE ADM_ID=@G_ADM_ID ORDER BY DT DESC";
		DTTable dt = DTTable.getJdbcTable(sqlHis, "", this._Rv);
		this._Js.al("var his_lst=" + dt.toJson(this._Rv));
		this._Js.al("initNav();installCfgs();installHis()");

	}

	/**
	 * 新版调用
	 * 
	 * @param rv
	 * @param combineCfgXml
	 * @param response
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws JSONException
	 */
	public void initRobert(RequestValue rv, String combineCfgXml, HttpServletResponse response)
			throws ParserConfigurationException, SAXException, IOException, JSONException {
		this._CombineCfgXml = combineCfgXml;
		this._Response = response;
		this._Rv = rv;

		if (rv.getString("SYS_EWA_LANG") != null && rv.getString("SYS_EWA_LANG").equalsIgnoreCase("enus")) {
			this._IsEn = true;
		} else {
			this._IsEn = false;
		}

		Document doc = UXml.retDocument(this._CombineCfgXml);

		Element root = (Element) doc.getElementsByTagName("root").item(0);
		_Title = root.getAttribute("title");
		String split = root.getAttribute("split");
		String full = root.getAttribute("full");
		rv.addValue("__CFG_SPLIT", split);
		rv.addValue("__CFG_full", full);
		String user_id = "";
		String xml = "";
		user_id = rv.getString("G_ADM_ID");
		xml = rv.getString("XML");
		if (user_id == null || user_id == "") {
			return;
		}
		// boolean isSplit = split != null && split.equals("1");
		// isSplit = true;
		HtmlCombineGrp htmlCombineGrp = new HtmlCombineGrp(doc);
		htmlCombineGrp.init();

		HashMap<String, DTRow> aclMap = this.getUserGrpItemsRobert("MENU_GRP_ITEM", xml);

		if (rv.getString("combin_all") == null) {
			ArrayList<String> al = new ArrayList<String>();
			for (String grp : htmlCombineGrp.getMap().keySet()) {
				boolean isHas = false;
				for (String key : aclMap.keySet()) {
					if (key.endsWith("=" + grp) || key.indexOf("=" + grp + "&") > 0) {
						isHas = true;
						break;
					}
				}
				if (!isHas) {
					al.add(grp);
				}
			}
			for (int i = 0; i < al.size(); i++) {
				htmlCombineGrp.getMap().remove(al.get(i));
			}
		}
		for (int i = 0; i < htmlCombineGrp.getGrpIndex().size(); i++) {
			String grp = htmlCombineGrp.getGrpIndex().get(i);
			if (!htmlCombineGrp.getMap().containsKey(grp)) {
				continue;
			}
			ArrayList<HtmlCombineItem> items = htmlCombineGrp.getMap().get(grp);
			for (int m = 0; m < items.size(); m++) {
				HtmlCombineItem cbItem = items.get(m);
				try {
					this.createItem(cbItem.getItemXml());
				} catch (Exception err) {
					this._Html.al("<div>" + err + "</div>");

				}
			}
		}

		// top html
		NodeList nla = doc.getElementsByTagName("htmlTop");
		if (nla.getLength() > 0 && this._Rv.getString("no_nav") == null) {
			String top = nla.item(0).getTextContent();
			top = top.replace("<!--123-->", this._TopNav.toString());
			top = this.replaceParameters(top);
			this._Html.insert(0, top);
		}

		NodeList nlb = doc.getElementsByTagName("htmlBottom");
		if (nlb.getLength() > 0) {
			String bottom = nlb.item(0).getTextContent();
			bottom = this.replaceParameters(bottom);
			this._Html.al(bottom);
		}
		if (full != null && full.trim().length() > 0) {
			String fullCss = "<style>#crm_main_box{width:1162px;margin-left:0px}#crm_main_nav{width:1162px;margin-left:0px}#crm_left_box{display:none;}.left{width:1132px;}</style>";
			this._Html.insert(0, fullCss);
		}

		String sqlHis = "SELECT TOP 10 * FROM ADM_HIS WHERE ADM_ID=@G_ADM_ID ORDER BY DT DESC";
		DTTable dt = DTTable.getJdbcTable(sqlHis, "", this._Rv);
		this._Js.al("var his_lst=" + dt.toJson(this._Rv));
		this._Js.al("initNav();installCfgs();installHis()");

	}

	/**
	 * 获取用户权限
	 * 
	 * @param grp
	 * @param xml
	 * @return
	 */
	private HashMap<String, DTRow> getUserGrpItemsRobert(String grp, String xml) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("select a.* from ADM_MENU a"
				+ " inner join ADM_MENU_SUPPLY b on a.MNU_ID=b.MNU_ID and b.SUP_ID=@G_SUP_ID"
				+ "	inner join ADM_ACL_MNU c on c.MNU_ID=b.MNU_ID and b.SUP_ID=c.SUP_ID and c.ADM_ID=@G_ADM_ID"
				+ " where MNU_CMD like '%" + xml.replace("'", "''").replace("%", "") + "%' and MNU_GRP='rob'");
		String sql = stringBuilder.toString();
		DTTable table = DTTable.getJdbcTable(sql, "globaltravel", this._Rv);

		HashMap<String, DTRow> map = new HashMap<String, DTRow>();
		for (int i = 0; i < table.getCount(); i++) {
			DTRow r = table.getRow(i);
			try {
				String id = r.getCell("MNU_CMD").toString();
				map.put(id, r);
			} catch (Exception e) {
			}

		}
		return map;
	}

	private HashMap<String, String> getParameters(Node item) {
		String[] params = "x,i,p,install,id,des,is_rename,his,his_name,lst,js,grp,js,js_rename".split(",");
		HashMap<String, String> map = new HashMap<String, String>();
		Element ele = (Element) item;
		String ref = ele.getAttribute("ref"); // 引用的对象

		for (int i = 0; i < params.length; i++) {
			String name = params[i].trim();
			if (name.equals("js")) {
				map.put(name, ele.getTextContent());
			} else {
				map.put(name, ele.getAttribute(name));
			}
		}

		if (ref == null || ref.length() == 0) {
			return map;
		}

		// 参考引用
		String[] refs = ref.split("\\|");
		if (refs.length != 2) {
			this._Html.al("<P>ref:" + ref + "表达错误</p>");
			return null;
		}
		String path = item.getOwnerDocument().getDocumentURI().replace("file:", "");
		File f = new File(path);
		String path1 = f.getParent() + "/" + refs[0].trim();
		if (!f.exists()) {
			this._Html.al("<P>ref:" + path1 + " 不存在</p>");
			return null;
		}
		Element e0 = null;
		try {
			Document doc = UXml.retDocument(path1);
			NodeList nl = doc.getElementsByTagName("item");

			for (int ia = 0; ia < nl.getLength(); ia++) {
				Element e1 = (Element) nl.item(ia);
				String id0 = e1.getAttribute("id");
				if (id0.equalsIgnoreCase(refs[1].trim())) {
					e0 = e1;
					break;
				}
			}

		} catch (Exception e) {
			this._Html.al("<P>ref:" + e.getMessage() + "</p>");
			return null;
		}
		if (e0 == null) {
			this._Html.al("<P>ref:" + ref + ",id=" + refs[1] + " 不存在</p>");
			return null;
		}
		for (int i = 0; i < params.length; i++) {
			String name = params[i].trim();
			String val = map.get(name);
			if (val == null || val.trim().length() == 0) {

				if (name.equals("js")) {
					map.put(name, e0.getTextContent());
				} else {
					map.put(name, e0.getAttribute(name));
				}
			}
		}
		return map;
	}

	private void createItem(Node item) throws JSONException {
		Element ele = (Element) item;
		HashMap<String, String> map = this.getParameters(item);
		String i = map.get("i");
		String install = map.get("install");
		String id = map.get("id");
		String des = map.get("des");

		if (this._IsEn && this._AclMap != null && this._AclMap.containsKey(id)) {
			DTRow r = this._AclMap.get(id);
			if (r.getTable().getColumns().testName("MENU_NAME_EN")) {
				try {
					des = r.getCell("MENU_NAME_EN").toString();
				} catch (Exception e) {
					des = e.getMessage();
				}
			} else {
				try {
					des = r.getCell("MNU_TXT_EN").toString();
				} catch (Exception e) {
					des = e.getMessage();
				}
			}

		}

		String his = map.get("his");
		String js = map.get("js");
		String grp = map.get("grp");

		String all = ele.getAttribute("all");
		String mearge = ele.getAttribute("mearge");

		if (mearge != null && mearge.length() > 0) {
			String s1 = "";
			if (this._Mearge.containsKey(mearge)) {
				s1 = this._Mearge.get(mearge) + ",DES_" + id;
			} else {
				s1 = "DES_" + mearge + ",DES_" + id;
			}
			this._Mearge.put(mearge, s1);
		}

		boolean isHis = his != null && his.equals("1");
		String initId = _Rv.getString("init_id");

		if (initId != null && initId.trim().length() > 0) {
			if (!id.equalsIgnoreCase(initId)) {
				if (all == null || all.trim().length() == 0) {
					String q = createQueryParameters("init_id");
					String lnk = "<a item_id='" + id + "' [" + grp + "] class='crm_main_nav1' href='combine_crm.jsp?"
							+ q + "&init_id=" + id + "' title=\"" + des + "\">" + des + "</a>";
					_TopNav.al(lnk);
					return;
				}
			} else {
				if (all == null || all.trim().length() == 0) {
					String lnk = "<a item_id='" + id + "' class='crm_main_nav1 crm_main_nav1_cur' title=\"" + des
							+ "\">" + des + "</a>";
					_TopNav.al(lnk);
				}
			}

			_Rv.changeValue("__CFG_TITLE", _Rv.getString("__CFG_TITLE") + " " + des, "string", 100);
		}

		// 分组
		String init_grp = _Rv.getString("init_grp");
		if (init_grp != null && init_grp.trim().length() > 0 && (all == null || all.trim().length() == 0)) {
			if (grp.equalsIgnoreCase(init_grp)) {
				if (_TopNav.indexOf("[" + grp + "]") < 0) {
					String lnk = "<a  item_id='" + id + "'[" + grp
							+ "] class='crm_main_nav1 crm_main_nav1_cur' title=\"" + des + "\">" + des + "</a>";
					_TopNav.al(lnk);
				}
			} else {
				if (_TopNav.indexOf("[" + grp + "]") < 0) {
					String q = createQueryParameters("init_grp");
					String lnk = "<a item_id='" + id + "'  [" + grp + "] class='crm_main_nav1' href='combine_crm.jsp?"
							+ q + "&init_grp=" + grp + "' title=\"" + des + "\">" + des + "</a>";
					_TopNav.al(lnk);
				}
				return;
			}
		}

		if (js == null) {
			js = "";
		} else {
			js = this.replaceParameters(js);
		}

		MStr s = new MStr();
		s.a("<div id='crm_main_box'>");
		s.al("<div class='subject' [st1]  id='DES_" + id + "'><a class='subject_expand' " + "onclick='showFull(\"DES_"
				+ id + "\")'></a><div class='ewa_lf_func_caption'" + " [$] style='float:left'>" + des
				+ "</div></div><div class='left' id='" + id + "'>");
		if (i == null || i.trim().length() == 0) {
			// nothing
		} else if (install.equals("html")) {
			String html = this.createHtml(item, map);
			s.al(html);
			_Js.al("if(window.EWA_COMBINES_HTML==null){" + "EWA_COMBINES_HTML=[];}EWA_COMBINES_HTML.push('" + id
					+ "');");
		} else if (install.equals("json")) {
			String rst = this.createJson(item, map);
			s.al(rst);

		} else if (install.equals("iframe")) {
			String rst = this.createIframe(item, map);
			s.replace("[$]", "onclick=actIframe('" + id + "')");
			s.al(rst);

		} else if (install.equals("iframe_lazy")) {
			String rst = this.createIframe(item, map);
			rst = rst.replace("src='", "_src='");
			s.replace("[$]", "onclick=actIframe('" + id + "')");
			s.al(rst);

		} else if (install.equals("box")) {
			if (this._Rv.getString("no_nav") == null) {
				String rst = this.createBox(item, map);
				s.al(rst);
				s.replace("class='left'", "class='left1'");
				s.replace("[st1]", "style='margin-bottom:0px;'");
			}
		} else { // js
			String rst = this.createJs(item, map);
			s.al(rst);
		}
		if (js.length() > 0) {
			_Js.al(js);
		}
		s.al("</div></div>");
		s.replace("[st1]", "");
		if (install.equalsIgnoreCase("json")) {
			s.al("<div id='crm_more' class='left' show='1'" + " onclick='showMore(\"" + id + "\")'>更多</div>");
		}
		_Items.put(id.toUpperCase(), s.toString());
		_Html.al(s.toString());

		if (isHis) {
			this.recordToHistory(map);
		}
	}

	private void recordToHistory(HashMap<String, String> map) {
		String his_name = map.get("his_name");
		String name = this.replaceParameters(his_name);
		String q = _Rv.getString("EWA_QUERY_ALL");
		int code = name.hashCode();
		String sql = "if not exists(select 1 from adm_his where adm_id=@g_adm_id and code=" + code + ")"
				+ "\r\nbegin\r\n" + "INSERT INTO ADM_HIS(ADM_ID,CODE,NAME,Q,DT)VALUES(@G_ADM_ID," + code + ",'"
				+ name.replace("'", "''") + "','" + q.replace("'", "''") + "',getdate())"
				+ "\r\nend\r\n else begin update adm_his set dt=getdate(),name='" + name.replace("'", "''") + "', q='"
				+ q.replace("'", "''") + "' where code=" + code + " and adm_id=@g_adm_id end";
		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");
		cnn.setRequestValue(_Rv);
		cnn.executeUpdate(sql);
		cnn.close();
	}

	private String createJs(Node item, HashMap<String, String> map) throws JSONException {
		String x = map.get("x");
		String i = map.get("i");
		String p = map.get("p");
		String id = map.get("id");
		String install = map.get("install");
		String des = map.get("des");

		String params = replaceParameters(p);

		JSONObject json = new JSONObject();
		json.put("x", x);
		json.put("i", i);
		json.put("p", params + "&COMBINE_ID=" + id);
		json.put("id", id);
		json.put("des", des);
		json.put("install", install);

		_Js.al("if(window.EWA_COMBINES==null){" + "EWA_COMBINES=[];}EWA_COMBINES.push(" + json + ");");
		return "";
	}

	private String createHtml(Node item, HashMap<String, String> map) {
		String x = map.get("x");
		String i = map.get("i");
		String p = map.get("p");
		String id = map.get("id");
		String js_rename = map.get("js_rename");

		boolean isJsRename = js_rename != null && js_rename.equals("1");

		String params = replaceParameters(p);

		MStr s = new MStr();
		HtmlControl ht = new HtmlControl();
		if (params == null || params.trim().length() == 0) {
			params = "_xxx0099=1";
		}
		params = params + "&EWA_AJAX=INSTALL&EWA_FRAMESET_NO=1&EWA_CALL_METHOD=INNER_CALL&COMBINE_ID=" + id;
		ht.init(x, i, params, _Rv.getRequest(), _Rv.getSession(), _Response);
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
		_Js.al("if(window.EWA_COMBINES_HTML==null){" + "EWA_COMBINES_HTML=[];}EWA_COMBINES_HTML.push('" + id + "');");
		return s.toString();
	}

	private String createIframe(Node item, HashMap<String, String> map) throws JSONException {
		String x = map.get("x");
		String i = map.get("i");
		String p = map.get("p");
		String id = map.get("id");

		String params = replaceParameters(p);

		String html = "<iframe id='__IF_" + id
				+ "' frameborder=0 width=100% height=100% style='min-height:300px' scroll=no src='"
				+ _Rv.getContextPath() + "/EWA_STYLE/cgi-bin/?xmlname=" + x + "&ewa_debug_no=1&itemname=" + i + "&"
				+ params + "'></iframe>";

		return html;
	}

	private String createJson(Node item, HashMap<String, String> map) throws JSONException {
		String x = map.get("x");
		String i = map.get("i");
		String p = map.get("p");
		String id = map.get("id");

		String params = replaceParameters(p);

		MStr s = new MStr();
		HtmlControl ht = new HtmlControl();
		ht.init(x, i, params + "&COMBINE_ID=" + id, _Rv, _Response);
		_Js.al("var json_" + id + "=" + ht.getHtml() + ";");

		String html = ht.getHtml();
		JSONObject arr = new JSONObject(html);
		this.putJsonToRv(arr);

		return s.toString();
	}

	private String createBox(Node item, HashMap<String, String> map) throws JSONException {

		String x = map.get("x");
		String i = map.get("i");
		String p = map.get("p");
		String id = map.get("id");
		String lst = map.get("lst");

		String params = replaceParameters(p);

		MStr s = new MStr();

		HtmlControl ht = new HtmlControl();
		ht.init(x, i, params + "&COMBINE_ID=" + id, _Rv, _Response);
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
		int cols = _Rv.getString("__CFG_full") == null || _Rv.getString("__CFG_full").trim().length() == 0 ? 4 : 4;
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
			s1.a("<li class='am0' rid='" + name + "'>" + cfg.getString("DES") + "</li>");
			if (names.length > 1) {
				String jsName = names[1].trim();
				if (jsName.indexOf("(") > 0) {
					onclick = " onclick=\"" + jsName + "\"";
				} else {
					if (map1.containsKey(jsName.toUpperCase())) {
						JSONObject objClick = map1.get(jsName.toUpperCase());
						if (objClick.has("ONCLICK")) {
							onclick = " onclick=\"" + Utils.textToInputValue(objClick.getString("ONCLICK")) + "\"";
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
							String wfParams = wf.getString("P").replace("[RID]", rid);
							wfParams = this.replaceParameters(wfParams);
							wfParams = "combine_id=" + id + "&" + wfParams;
							String u = "EWA.UI.Dialog.OpenReloadClose('-1','" + wf.getString("X") + "','"
									+ wf.getString("I") + "', false,\"" + wfParams + "\")";
							onclick = " onclick=\"" + Utils.textToInputValue(u) + "\"";
						}
					}
				}
				s1.a("<li class='am1'><div class='am1_txt'>" + val + "</div><a " + onclick
						+ "  class='am1_edit'><img src='" + _Rv.getContextPath() + "/images/pencil.png' /></a></li>");
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

	private String createQueryParameters(String removeIds) {
		String[] ids = removeIds.split(",");
		MTable tb = new MTable();

		for (int ia = 0; ia < _Rv.getPageValues().getQueryValues().getCount(); ia++) {
			PageValue pv = (PageValue) _Rv.getPageValues().getQueryValues().getByIndex(ia);
			boolean isRemove = false;
			for (int m = 0; m < ids.length; m++) {
				if (pv.getName().equalsIgnoreCase(ids[m].trim())) {
					isRemove = true;
					break;
				}
			}
			if (!isRemove) {
				tb.add(pv.getName(), pv.getValue());
			}
		}
		String q = tb.join("=", "&");
		return q;
	}

	private String replaceParameters(String s1) {
		if (s1 == null || s1.trim().length() == 0)
			return s1;
		MListStr a = Utils.getParameters(s1, "@");
		if (a.size() == 0) {
			return s1;
		}
		MStr sb = new MStr(s1);
		for (int i = 0; i < a.size(); i++) {
			String name = a.get(i);
			String val;
			try {
				val = this._Rv.getString(name);
			} catch (Exception e) {
				val = e.getLocalizedMessage();
			}

			String find = "@" + name;

			if (val == null) {
				val = "";
			}
			sb.replace(find, val);
		}
		return sb.toString();
	}

	public String getHtml() {
		String ss = this._Html.toString();
		if (this._Mearge.size() > 0) {
			MStr s = new MStr();
			for (String key : this._Mearge.keySet()) {
				String m0 = this._Mearge.get(key);
				s.al("meargeItems('" + m0 + "');");

			}
			int loc0 = ss.indexOf("lastDo");
			if (loc0 < 0) {
				ss += "<script>function lastDo(){" + s + "}</script>";
			} else {
				int loc1 = ss.indexOf("{", loc0);
				String s0 = ss.substring(loc0, loc1 + 1);

				ss = ss.replace(s0, s0 + s.toString());
			}

		}
		return ss;
	}

	public String getJs() {
		String ss = this._Js.toString();

		return ss;
	}

	public String getTitle() {
		return this.replaceParameters(this._Title);
	}

	public boolean hasItem(String user_id, String unid, String xmlid) {
		boolean bl = false;

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(
				"SELECT B.MENU_NAME_EN FROM MENU_ADM A INNER JOIN MENU_NEW B ON A.MENU_ID=B.MENU_ID AND A.ADM_ID=");
		stringBuilder.append(user_id.replace("'", "''"));
		stringBuilder.append("  WHERE B.MENU_PID IN ");
		stringBuilder.append("(SELECT MENU_ID FROM MENU_NEW  WHERE MENU_UNID='");
		stringBuilder.append(unid.replace("'", "''"));
		stringBuilder.append("') AND  B.MENU_REMARK= '");
		stringBuilder.append(xmlid.replace("'", "''"));
		stringBuilder.append("'");
		String sql = stringBuilder.toString();
		DTTable table = DTTable.getJdbcTable(sql, "globaltravel");
		if (table.getCount() > 0) {
			bl = true;
		}
		return bl;
	}

	private HashMap<String, DTRow> getUserGrpItems(String grp, String xml) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append("SELECT B.* FROM MENU_NEW A INNER JOIN MENU_NEW B ON A.MENU_ID=B.MENU_PID AND A.MENU_XML='");
		stringBuilder.append(xml == null ? "" : xml.replace("'", "''"));
		stringBuilder.append("' AND A.MENU_GRP='");
		stringBuilder.append(grp.replace("'", "''"));
		stringBuilder.append("'");
		stringBuilder.append(" INNER JOIN MENU_ADM C ON B.MENU_ID=C.MENU_ID");
		stringBuilder.append(" WHERE  (C.ADM_ID=@g_adm_id  )");
		String sql = stringBuilder.toString();
		DTTable table = DTTable.getJdbcTable(sql, "globaltravel", this._Rv);

		HashMap<String, DTRow> map = new HashMap<String, DTRow>();
		for (int i = 0; i < table.getCount(); i++) {
			DTRow r = table.getRow(i);
			try {
				String id = r.getCell("MENU_REMARK").toString();
				map.put(id, r);
			} catch (Exception e) {
			}

		}
		return map;
	}

	public boolean hasItemByGrp(String user_id, String grp, String xmlid, String xml) {
		boolean bl = false;
		if (user_id == null) {
			return false;
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append("SELECT B.* FROM MENU_NEW A INNER JOIN MENU_NEW B ON A.MENU_ID=B.MENU_PID AND A.MENU_XML='");
		stringBuilder.append(xml.replace("'", "''"));
		stringBuilder.append("' AND A.MENU_GRP='");
		stringBuilder.append(grp.replace("'", "''"));
		stringBuilder.append("'");
		stringBuilder.append(" INNER JOIN MENU_ADM C ON B.MENU_ID=C.MENU_ID");
		stringBuilder.append(" WHERE   B.MENU_REMARK= '");
		stringBuilder.append(xmlid.replace("'", "''"));
		stringBuilder.append("' and C.ADM_ID=");
		stringBuilder.append(user_id.replace("'", "''"));
		String sql = stringBuilder.toString();

		DTTable table = DTTable.getJdbcTable(sql, "globaltravel");
		if (table.getCount() > 0) {
			bl = true;
		}
		return bl;
	}
}
