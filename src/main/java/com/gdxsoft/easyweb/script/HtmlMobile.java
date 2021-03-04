package com.gdxsoft.easyweb.script;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class HtmlMobile {
	private HashMap<String, DTRow> _AppCfg;
	private ArrayList<DTRow> _Homes;
	private HashMap<String, ArrayList<DTRow>> _Subs;
	private HtmlControl _Ht;
	private DTRow _CfgRow;
	private RequestValue _Rv;
	private HashMap<String, String> _DataMap;
	private JSONObject _LastListData;
	private ArrayList<String> _History;

	private String wfCurUnitId;
	private JSONArray wfOptions;
	private HashMap<String, String> wfShowMap;

	private String _AppPara0;
	private String _AppPara1;

	private void initAppCfg() {
		_AppCfg = new HashMap<String, DTRow>();
		_Homes = new ArrayList<DTRow>();
		_Subs = new HashMap<String, ArrayList<DTRow>>();
		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");

		// 清除CACHE数据
		String sqla = "TRUNCATE TABLE MOB_APP_CACHE";
		cnn.executeUpdate(sqla);
		cnn.close();

		// 获取配置数据
		String sql = "SELECT * FROM _EWA_APP_CFG WHERE APP_ADD_PARA0='"
				+ this._AppPara0.replace("'", "''") + "'";
		if (_AppPara1 != null) {
			sql += " and app_add_para1='" + this._AppPara1.replace("'", "''")
					+ "'";
		}
		sql += " ORDER BY APP_LVL,APP_PID,APP_ORD";
		DTTable tb = DTTable.getJdbcTable(sql, "");
		if (tb.getCount() == 0) {
			return;
		}

		DTRow r = tb.getRow(0);
		DTRow wkRow = tb.addRow();
		DTRow wkRowSp = tb.addRow();
		DTRow wkRowWf = tb.addRow();

		try {
			// 审批细节
			wkRow.getCell("APP_ID").setValue("-999");
			wkRow.getCell("APP_TYPE").setValue("detail");
			wkRow.getCell("APP_PID").setValue("-1000");

			// 审批表
			wkRowSp.getCell("APP_ID").setValue("-1000");
			wkRowSp.getCell("APP_TYPE").setValue("frame");
			wkRowSp.getCell("APP_PID").setValue("-1001");

			//
			wkRowWf.getCell("APP_ID").setValue("-1001");
			wkRowWf.getCell("APP_TYPE").setValue("web");
			wkRowWf.getCell("APP_PID").setValue("-1000");

			wkRowWf
					.getCell("APP_URL")
					.setValue(
							"EWA_STYLE/cgi-bin/?xmlname=global_travel|common.xml&itemname=wf.frame.chart");

			String rootId = r.getCell("APP_PID").toString();
			for (int i = 0; i < tb.getCount(); i++) {
				DTRow r1 = tb.getRow(i);
				String pid = r1.getCell("APP_PID").toString();
				if (pid.equals(rootId)) {
					_Homes.add(r1);
				}
				String appId = r1.getCell("APP_ID").toString();
				_AppCfg.put(appId, r1);
				if (_AppCfg.containsKey(pid)) {
					if (!_Subs.containsKey(pid)) {
						ArrayList<DTRow> subs = new ArrayList<DTRow>();
						_Subs.put(pid, subs);
					}
					_Subs.get(pid).add(r1);
				}
			}
		} catch (Exception e) {
			return;
		}
	}

	private void recordCache(String appId, String cache) {
		String sql = "DELETE FROM MOB_APP_CACHE WHERE APP_ID='"
				+ appId.replace("'", "''") + "' AND ADM_ID=@G_ADM_ID;\n"
				+ "INSERT INTO MOB_APP_CACHE(APP_ID,ADM_ID,CACHE,DT)VALUES('"
				+ appId.replace("'", "''")
				+ "',@G_ADM_ID,@TMP_CACHE,@SYS_DATE)";

		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");
		this._Rv.addValue("TMP_CACHE", cache, "String", 100000);
		cnn.setRequestValue(this._Rv);
		// 更新CACHE数据
		cnn.executeUpdate(sql);
		cnn.close();
	}

	private String readCache(String appId) {
		String sql = "SELECT CACHE from MOB_APP_CACHE WHERE APP_ID='"
				+ appId.replace("'", "''") + "' AND ADM_ID=@G_ADM_ID";
		DTTable dt = DTTable.getJdbcTable(sql, "", this._Rv);
		if (dt.getCount() == 0) {
			return null;
		} else {
			return dt.getCell(0, 0).toString();
		}
	}

	public HtmlMobile(String appPara0, String appPara1) {
		this._AppPara0 = appPara0;
		this._AppPara1 = appPara1;

		initAppCfg();
		
		_DataMap = new HashMap<String, String>();
		_History = new ArrayList<String>();
	}

	public String getContent(HttpServletRequest request, HttpSession session,
			HttpServletResponse response) {
		RequestValue rv = new RequestValue(request, session);
		if (rv.getString("gdx") != null) {
			initAppCfg();
		}
		this._Rv = rv;
		if (rv.getString("is_back") == null) {
			String ref = rv.getString("ref123");
			if (ref != null && ref.trim().length() > 0) {
				int index = this._History.size() - 1;
				if (index >= 0) {
					String lastRef = this._History.get(index);
					if (!lastRef.equals(ref)) {// 不是重复提交
						this._History.add(ref);
					}
				} else {
					this._History.add(ref);
				}
			}
		} else {
			int index = this._History.size() - 1;
			// String ref = rv.getString("ref123");
			// if (ref == null) {
			// ref = "???";
			// }
			if (index >= 0) {
				// String lastRef = this._History.get(index);
				// if (ref.equals(lastRef)) {// 防止重复提交
				this._History.remove(index);
				// }
			}
		}

		String appId = rv.getString("APP_ID");
		if (appId == null) {
			String curAppId = rv.getString("CUR_APP_ID");
			if (curAppId == null) {// home
				return createHome(null);
			}
			this.getAppCfg(curAppId);

			String fromTarget = rv.getString("FROM_TARGET");
			if (fromTarget == null) {
				fromTarget = "";
			}
			if (fromTarget.equalsIgnoreCase("list-click")) {
				int listClickIndex = rv.getInt("LIST_CLICK_INDEX");
				try {
					// 将选中的数据放到缓存中
					this.pushData(curAppId, listClickIndex);
				} catch (JSONException e) {
					return e.getMessage();
				}
				String detailAppId = this.getCfgItem("APP_FRAME_DETAIL_ID");
				if (detailAppId == null) {
					detailAppId = this.getCfgItem("APP_FRAME_MODIFY_ID");
				}
				if (detailAppId == null) {
					if (this._LastListData != null) {
						JSONArray arr;
						try {
							arr = this._LastListData.getJSONArray("CFG");

							for (int i = 0; i < arr.length(); i++) {
								String tag = arr.getJSONObject(i).getString(
										"TAG");
								if (tag.equalsIgnoreCase("butFlow")) {// workflow
									JSONObject rstJson0Wf = this._LastListData
											.getJSONObject("WF");
									JSONObject wfData = this._LastListData
											.getJSONArray("DATA")
											.getJSONObject(listClickIndex);
									this.goWorkflow(rstJson0Wf, arr, wfData);
									return this.createHtml("-999", request,
											session, response);
								}
							}
							return "NO SUBS /No Workflow";
						} catch (Exception e) {
							return e.getMessage();
						}
					} else {
						return "NO SUBS";
					}
				}
				return this.createHtml(detailAppId, request, session, response);
			} else if (fromTarget.equalsIgnoreCase("detail-click")) {
				if (curAppId.equals("-999")) {// workflow
					return this.createHtml("-1000", request, session, response);
				} else {
					String detailAppId = this.getCfgItem("APP_FRAME_MODIFY_ID");
					if (detailAppId == null) {
						return createHome(curAppId);
					} else {
						return this.createHtml(detailAppId, request, session,
								response);
					}
				}
			} else if (fromTarget.equalsIgnoreCase("butadd-click")) {
				return this.createHtml(curAppId, request, session, response);
			} else if (fromTarget.equals("list-click-wf")) {// workflow
				int listClickIndex = rv.getInt("LIST_CLICK_INDEX");
				try {
					// 先获取前面的数据
					String cacheJson = this.readCache(curAppId);
					JSONObject json = new JSONObject(cacheJson);
					this._LastListData = json;

					this.pushData(curAppId, listClickIndex); // bug
					JSONObject data = this._LastListData.getJSONArray("DATA")
							.getJSONObject(listClickIndex);
					this.workflow(data);

					return this.createHtml("-999", request, session, response);

				} catch (Exception e) {
					return e.getMessage();
				}

			} else if (fromTarget.equals("butWorkflow-click")) {
				// workflow show charts
				return this.createHtml("-1001", request, session, response);
			}
			return "unkonwn target";
		} else {
			return this.createHtml(appId, request, session, response);
		}
	}

	private void pushData(String appId, int listClickIndex)
			throws JSONException {
		JSONArray arr = this._LastListData.getJSONArray("DATA");
		JSONObject data = arr.getJSONObject(listClickIndex);
		Iterator<?> it = data.keys();
		while (it.hasNext()) {
			String key = it.next().toString();
			String key1 = key.toUpperCase();
			if (data.has(key)) {
				if (this._DataMap.containsKey(key1)) {
					this._DataMap.remove(key1);
				}
				this._DataMap.put(key1, data.getString(key));
			}
		}
	}

	private String createHome(String appPid) {
		ArrayList<DTRow> al;
		if (appPid == null || appPid.trim().length() == 0) {
			al = _Homes;
			this._History.clear();
			this._CfgRow = null;
			this._DataMap.clear();

		} else {
			if (!_Subs.containsKey(appPid)) {
				return "No home";
			}
			al = _Subs.get(appPid);
		}

		try {

			MStr s = new MStr();
			for (int i = 0; i < al.size(); i++) {
				DTRow r = al.get(i);
				String appTile = r.getCell("APP_TITLE").toString();
				String appId = r.getCell("APP_ID").toString();

				s.al("<li><a href='javascript:doHome(\"" + appId + "\")'>"
						+ appTile + "</a>");
			}

			return s.toString();
		} catch (Exception err) {
			return err.getMessage();
		}
	}

	private String createHtml(String appId, HttpServletRequest request,
			HttpSession session, HttpServletResponse response) {
		this.getAppCfg(appId);
		if (this._CfgRow == null) {
			return "APP_ID error";
		}
		String appType = this.getCfgItem("APP_TYPE");
		String xmlName = this.getCfgItem("APP_XML_NAME");
		String itemName = this.getCfgItem("APP_ITEM_NAME");
		String paras = this.getCfgItem("APP_URL_PARAS");
		if (paras != null) {
			paras = this.replaceParameters(paras);
		} else {
			paras = "";
		}
		if (appType.equals("list")) {
			this._Ht = new HtmlControl();
			String p1 = paras == null ? "" : paras;
			if (p1.length() == 0) {
				p1 = "EWA_AJAX=JSON_EXT&EWA_FRAMESET_NO=1";
			} else {
				p1 += "&EWA_AJAX=JSON_EXT&EWA_FRAMESET_NO=1";
			}
			this._Ht.init(xmlName, itemName, p1, request, session, response);

			String json = this._Ht.getHtml();

			// 记录到数据库中
			this.recordCache(appId, json);

			try {
				String rst = this.handleJson(json);
				return rst;
			} catch (JSONException e) {
				return e.getMessage();
			}
		} else if (appType.equals("detail")) {
			try {
				String rst = this.handleDetail();
				return rst;
			} catch (JSONException e) {
				return e.getMessage();
			}
		} else if (appType.equals("web")) {
			String url = this.getCfgItem("APP_URL");
			url = this.replaceParameters(url);
			String u1 = this._Rv.getContextPath() + "/" + url;
			return "<iframe  src='" + u1
					+ "' width='100%' frameborder=0 height='100%'></iframe>";
		} else if (appType.equals("home")) {
			return this.createHome(appId);
		} else if (appType.equals("frame") || appId.equals("-1000")) {
			this._Ht = new HtmlControl();
			String p1 = paras == null ? "" : paras;
			if (p1.length() == 0) {
				p1 = "EWA_AJAX=JSON_EXT&EWA_FRAMESET_NO=1";
			} else {
				p1 += "&EWA_AJAX=JSON_EXT&EWA_FRAMESET_NO=1";
			}

			this._Ht.init(xmlName, itemName, p1, request, session, response);
			String json = this._Ht.getHtml();

			// 记录到数据库中
			this.recordCache(appId, json);

			String rst;
			try {
				rst = handleFrame(json);
				return rst;
			} catch (JSONException e) {
				return e.getMessage();
			}

		}
		return "unknow!";
	}

	/**
	 * 获取真正ListFrame数据
	 * 
	 * @param itemData
	 * @throws Exception
	 */
	private void workflow(JSONObject itemData) throws Exception {
		String rid1 = "";
		if (itemData.has("RID1")) {
			rid1 = itemData.getString("RID1");
		}
		if (rid1 == null) {
			rid1 = "";
		}

		// 调用Jsp文件获取ListFrame地址
		// ..../common/workflow_go.jsp?rid=12&rid1=0&rtag=OA_OVERTIME_APPLY&t=13&name=&eid=
		String workflowUrl = this.EWAWorkFlowSpA(itemData.getString("RID"),
				rid1, itemData.getString("RTAG"), "13", "", "");

		UNet net = new UNet();
		String ck = this._Rv.getRequest().getHeader("Cookie");
		net.addHeader("Cookie", ck);
		net.setUserAgent("ONEWORLD_APP 1.0");
		net.setEncode("utf-8");
		// 返回调用地址
		// {"u":"./?XMLNAME=oa|overtime.xml&ITEMNAME=OA_OVERTIME_APPLY.ListFrame.Modify&t=18&rid=12"
		// ,"p":"","eid":"","RST":true,"txt":"审批/(12)","mode":0}
		String rstJson = net.doGet(workflowUrl);

		JSONObject workflowJson = new JSONObject(rstJson);
		boolean rst = workflowJson.getBoolean("RST");
		if (!rst) {
			throw new Exception("ERROR:" + rstJson);
		}

		// 真正ListFrame地址
		String u = workflowJson.getString("u");

		String wfUrl = EWAURLRoot() + "EWA_STYLE/cgi-bin/" + u
				+ "&EWA_AJAX=Json_ext";

		wfUrl = wfUrl.replace("|", "%7c");
		// 获取Json数据表达式
		String rst1 = net.doGet(wfUrl);

		JSONObject rstJson0 = new JSONObject(rst1);
		JSONObject rstJson0Wf;
		rstJson0Wf = rstJson0.getJSONObject("WF");
		JSONObject wfData = rstJson0.getJSONArray("DATA").getJSONObject(0);
		goWorkflow(rstJson0Wf, rstJson0.getJSONArray("CFG"), wfData);

		this._LastListData = rstJson0;

		this.pushData(null, 0);
	}

	private String EWAURLRoot() {
		HttpServletRequest r = this._Rv.getRequest();
		MStr s = new MStr();
		s.a(r.getScheme());
		s.a("://");
		s.a(r.getServerName());
		s.a(":");
		s.a(r.getServerPort());
		s.a(r.getContextPath());
		s.a("/");
		return s.toString();
	}

	/**
	 * 创建审批参数
	 * 
	 * @param rstJson0Wf
	 * @param cfg
	 * @param wfData
	 * @throws Exception
	 */
	private void goWorkflow(JSONObject rstJson0Wf, JSONArray cfg,
			JSONObject wfData) throws Exception {

		String paras = rstJson0Wf.getString("P");
		JSONArray rids = rstJson0Wf.getJSONArray("RID");

		String rid = "";
		for (int i = 0; i < rids.length(); i++) {
			if (i > 0) {
				rid += ",";
			}
			rid += "@" + rids.getString(i);
		}
		DTRow wfRow = _AppCfg.get("-1000");
		wfRow.getCell("APP_TITLE").setValue("审批");
		wfRow.getCell("APP_XML_NAME").setValue(rstJson0Wf.getString("X"));
		wfRow.getCell("APP_ITEM_NAME").setValue(rstJson0Wf.getString("I"));
		wfRow.getCell("APP_URL_PARAS").setValue(paras.replace("[RID]", rid));

		DTRow wfRowWf = _AppCfg.get("-1001");
		String workflowUrl = "EWA_STYLE/cgi-bin/?xmlname=global_travel|common.xml&itemname=wf.frame.chart"
				+ "&" + paras.replace("[RID]", rid);
		wfRowWf.getCell("APP_URL").setValue(workflowUrl);
	}

	private String handleFrame(String json) throws JSONException {

		String appId = this.getCfgItem("APP_ID");
		if (appId.equals("-1000")) {
			// workflow sp
			// 获取审批的 选项
			this.getWorkflowOptionsHttp();
		} else {
			this.wfOptions = null;
		}

		JSONObject jsonObj = new JSONObject(json);
		JSONArray arr = jsonObj.getJSONArray("CFG");
		MStr s = new MStr();
		String xmlName = this.getCfgItem("APP_XML_NAME");
		String itemname = this.getCfgItem("APP_ITEM_NAME");

		String submit_url = this._Rv.getContextPath()
				+ "/EWA_STYLE/cgi-bin/?XMLNAME=" + xmlName + "&itemname="
				+ itemname + "&EWA_POST=1&EWA_ACTION=onpagepost&EWA_AJAX=json";
		if (this.wfOptions != null) {
			String curUnid;
			try {
				curUnid = URLEncoder.encode(wfCurUnitId, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				curUnid = e.getMessage();
			}
			submit_url = EWAURLRoot()
					+ "EWA_STYLE/cgi-bin/_wf_/?ewa_wf_type=ins_post&SYS_STA_TAG="
					+ curUnid;

		}
		String paras = this.getCfgItem("APP_URL_PARAS");
		paras = this.replaceParameters(paras);
		String p1 = paras == null ? "" : paras;
		if (p1.length() > 0) {
			submit_url += "&" + p1;
		}

		for (int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.getJSONObject(i);
			String des = o.getString("DES");
			String tag = o.getString("TAG");
			String name = o.getString("NAME");

			if (tag.equals("button") || tag.equals("submit")) {
				continue;
			}
			// 流程的字段显示或隐含
			if (this.wfOptions != null && name.indexOf("EWA_WF") < 0) {
				if (wfShowMap.containsKey(name)) {
					if (!wfShowMap.get(name).equalsIgnoreCase("GDX")) {
						continue;
					}
				}
			}

			boolean isMust = false;
			if (o.has("MUST") && o.getString("MUST").equals("1")) {
				isMust = true;
			}

			if (isMust) {
				des += " (*)";
			}

			String s1 = "";
			String v1 = this._DataMap.get(name);

			if (tag.equalsIgnoreCase("select")) {
				JSONArray opts;
				if (name.equalsIgnoreCase("EWA_WF_UOK")
						&& this.wfOptions != null) {
					// 审批选项
					opts = this.wfOptions;
				} else {
					opts = o.getJSONArray("LST");
				}
				MStr sOpts = new MStr();
				sOpts.al("<option   value=\"\"></option>");
				for (int m = 0; m < opts.length(); m++) {
					JSONObject opt = opts.getJSONObject(m);
					String v = opt.getString("V");
					if (v1 != null && v1.equals(v)) {
						sOpts.al("<option selected value=\""
								+ opt.getString("V") + "\">"
								+ opt.getString("T") + "</option>");
					} else {
						sOpts.al("<option value=\"" + opt.getString("V")
								+ "\">" + opt.getString("T") + "</option>");
					}
				}
				s1 = "<select  name=\"" + name + "\" id=\"" + name
						+ "\" placeholder=\"" + des + "\">" + sOpts.toString()
						+ "</select>";

			} else if (tag.equalsIgnoreCase("span")) {
				s1 = "<div>" + des + ": " + (v1 == null ? "" : v1) + "</div>";
			} else if (tag.equalsIgnoreCase("file")
					|| tag.equalsIgnoreCase("image")
					|| tag.equalsIgnoreCase("swffile")) {

				String action = submit_url.replace("/cgi-bin/",
						"/cgi-bin/_up_/")
						+ "&EWA_UP_TYPE=SWFUPLOAD&NAME=" + name;
				s1 = "<form target='up_"
						+ name
						+ "' style='margin:0px;padding:0px' action=\""
						+ action
						+ "\" method=\"post\" enctype=\"multipart/form-data\">"
						+ "<input type=\"file\" name=\""
						+ name
						+ "\" id=\""
						+ name
						+ "\" placeholder=\""
						+ des
						+ "\" value=\"\"></form><iframe style='display:none' name='up_"
						+ name + "'></iframe>";
			} else if (tag.equalsIgnoreCase("textarea")) {
				s1 = "<textarea type=\"text\" name=\"" + name + "\" id=\""
						+ name + "\" placeholder=\"" + des + "\" ></textarea>";
			} else if (tag.equalsIgnoreCase("datetime")) {
				s1 = "<input   type=\"datetime-local\" name=\"" + name
						+ "\" id=\"" + name + "\" placeholder=\"" + des
						+ "\" value=\"\" >";
			} else if (tag.equalsIgnoreCase("date")) {
				s1 = "<input type=\"date\" name=\"" + name + "\" id=\"" + name
						+ "\" placeholder=\"" + des + "\" value=\"\" >";
			} else {
				s1 = "<input type=\"text\" name=\"" + name + "\" id=\"" + name
						+ "\" placeholder=\"" + des + "\" value=\"\">";
			}
			//System.out.println(tag);
			s.al(s1);
		}
		s.al("<script>var FORM_CFGS=" + jsonObj + ";</script>");
		s.al("<script>var FORM_SUBMIT_URL=\"" + Utils.textToJscript(submit_url)
				+ "\";</script>");
		return s.toString();
	}

	private void getAppCfg(String appId) {
		this._CfgRow = _AppCfg.get(appId);
	}

	private String handleJson(String json) throws JSONException {
		String appType = this.getCfgItem("APP_TYPE");
		if (appType.equalsIgnoreCase("list")) {

			return this.handleList(json);
		}
		return "not impl" + appType;
	}

	private String handleDetail() throws JSONException {
		MStr s = new MStr();
		JSONArray cfgs = this._LastListData.getJSONArray("CFG");

		for (int i = 0; i < cfgs.length(); i++) {
			JSONObject o = cfgs.getJSONObject(i);
			String des = o.getString("DES");
			String tag = o.getString("TAG");
			String name = o.getString("NAME");
			if (tag.equals("button") || tag.equals("submit")
					|| tag.equals("file")) {
				continue;
			}
			String val = null;
			if (this._DataMap.containsKey(name + "_HTML")) {
				val = this._DataMap.get(name + "_HTML");
			}
			if (val == null) {
				if (this._DataMap.containsKey(name)) {
					val = this._DataMap.get(name);
				}
			}
			if (val == null) {
				continue;
			}
			s.al("<li><a href='javascript:doDetailClick()'>" + des + ": " + val
					+ "</a></li>");

		}
		s.al("<script>var appId='" + this.getCfgItem("APP_ID") + "';</script>");
		return s.toString();
	}

	/**
	 * 处理ListView
	 * 
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	private String handleList(String json) throws JSONException {
		JSONObject jsonObj = new JSONObject(json);
		JSONArray arr = jsonObj.getJSONArray("DATA");

		this._LastListData = jsonObj;

		MStr s = new MStr();
		String lstTitle = this.getCfgItem("APP_MAP_LABEL");
		String lstMemo = this.getCfgItem("APP_MAP_MSG");
		MListStr alTitle = Utils.getParameters(lstTitle, "@");
		MListStr alMemo = Utils.getParameters(lstMemo, "@");

		boolean isWorkflow = false;
		if (this.getCfgItem("APP_ITEM_NAME").equalsIgnoreCase(
				"v_sp_main.listframe.view")) {
			isWorkflow = true;
		}
		for (int i = 0; i < arr.length(); i++) {
			JSONObject row = arr.getJSONObject(i);
			String title = this.replaceParameters(lstTitle, row, alTitle);
			String memo = "";
			if (lstMemo != null && lstMemo.trim().length() > 0) {
				memo = this.replaceParameters(lstMemo, row, alMemo);
			}
			memo = memo.replace("\n", "<br>");
			if (isWorkflow) {
				String html = "<li><a href='javascript: doListViewClickWorkflow("
						+ i
						+ ")'>[WF] "
						+ title
						+ (memo.length() > 0 ? "<br>" + memo : "")
						+ "</a></li>";
				s.al(html);
			} else {
				String html = "<li><a href='javascript:doListViewClick(" + i
						+ ")'>" + title
						+ (memo.length() > 0 ? "<br>" + memo : "")
						+ "</a></li>";
				s.al(html);
			}
		}
		s.al("<script>var appId='" + this.getCfgItem("APP_ID") + "';</script>");
		return s.toString();
	}

	private String replaceParameters(String exp, JSONObject map, MListStr al) {

		String v1 = exp;
		for (int i = 0; i < al.size(); i++) {
			String key = al.get(i);
			String key1 = key.replace("@", "");
			String val = null;
			try {
				if (map.has(key1)) {
					val = map.getString(key1);
				}
				if (val == null) {
					val = "";
				}
			} catch (JSONException e) {
				val = e.getMessage();
			}
			v1 = v1.replace("@" + key, val);
		}
		return v1;
	}

	private String replaceParameters(String exp) {
		MListStr al = Utils.getParameters(exp, "@");
		String v1 = exp;
		for (int i = 0; i < al.size(); i++) {
			String key = al.get(i);
			String key1 = key.replace("@", "").toUpperCase();
			String val = null;
			if (this._DataMap.containsKey(key1)) {
				val = this._DataMap.get(key1);
			}
			if (val == null) {
				val = "";
			}
			v1 = v1.replace("@" + key, val);
		}
		return v1;
	}

	public String getPageButtonLeft() {
		if (getUrlBack() != null) {
			return "<a href=\"javascript:location.replace($X('butBack').getAttribute('back'))\" back=\""
					+ getUrlBack()
					+ "\" data-icon=\"carat-l\" class=\"ui-btn-left\" id='butBack'>返回</a>";
		} else {
			return "";
		}
	}

	public String getPageButtonRight() {
		MStr s = new MStr();
		ArrayList<String> al = new ArrayList<String>();
		if (this._CfgRow == null) {// home
			return "";
		}
		String addId = this.getCfgItem("APP_FRAME_ADD_ID");
		if (addId != null && addId.trim().length() > 0) {
			String butAdd = "<a href=\"javascript:addFrame('"
					+ addId
					+ "')\" data-icon=\"plus\" class=\"ui-btn-right\" style=\"right: {A}px\">新建</a>";
			al.add(butAdd);
		}
		String type = this.getCfgItem("APP_TYPE");
		if (type.equalsIgnoreCase("list")) {
			String butMore = "<a href=\"javascript:loadMore();\" data-icon=\"recycle\" class=\"ui-btn-right\" style=\"right: {A}px\">更多</a>";
			String butRefresh = "<a href=\"javascript:reloadData();\" data-icon=\"refresh\" class=\"ui-btn-right\" style=\"right: {A}px\">刷新</a>";
			al.add(butRefresh);
			al.add(butMore);
		}
		if (type.equalsIgnoreCase("frame")
				|| this.getCfgItem("APP_ID").equals("-999")) {
			String butOk = "<a href=\"javascript: frameSubmt();\" data-icon=\"action\" "
					+ " class=\"ui-btn-right\" style=\"right: {A}px\">提交</a>";
			al.add(butOk);
		}

		if (this.getCfgItem("APP_ID").equals("-1000")) {
			String butOk = "<a href=\"javascript: showWorkflow();\" data-icon=\"action\" "
					+ " class=\"ui-btn-right\" style=\"right: {A}px\">流程</a>";
			al.add(butOk);
		}
		// workflow
		if (this._Rv.getString("FROM_TARGET") != null
				&& this._Rv.getString("FROM_TARGET").equals("list-click-wf")) {
			String butOk = "<a href=\"javascript: frameWf();\" data-icon=\"action\" "
					+ " class=\"ui-btn-right\" style=\"right: {A}px\">审批</a>";
			al.add(butOk);
		}

		for (int i = al.size() - 1; i >= 0; i--) {
			int r = i * 70 + 6;
			String s1 = al.get(i).replace("{A}", r + "");
			s.al(s1);
		}
		return s.toString();
	}

	public String getPageTitle() {
		String s = this.getCfgItem("APP_TITLE");
		if (s == null) {
			return "Home";
		}
		return replaceParameters(s);
	}

	public String getPageType() {
		return this.getCfgItem("APP_TYPE");
	}

	public boolean isHaveSearch() {
		String s = this.getCfgItem("APP_SEARCH_NAME");
		if (s == null || s.trim().length() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public String getCfgItem(String name) {
		String rst;
		try {
			rst = this._CfgRow.getCell(name).toString();
			return rst;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String getUrlBack() {
		int index = this._History.size() - 1;
		if (index < 0) {
			return null;
		} else {
			String u = this._History.get(index);
			// System.out.println(u);
			if (u.indexOf("?") > 0) {
				return this._History.get(index) + "&is_back=1";
			} else {
				return this._History.get(index) + "?is_back=1";
			}
		}
	}

	public RequestValue getRequestValue() {
		return this._Rv;
	}

	public HtmlControl getHt() {
		return this._Ht;
	}

	public String EWAWorkFlowSpA(String rid, String rid1, String rtag,
			String t, String name, String eid) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(EWAURLRoot());
		stringBuilder.append("back_admin/common/workflow_go.jsp?rid=");
		stringBuilder.append(rid);
		stringBuilder.append("&rid1=");
		stringBuilder.append(rid1);
		stringBuilder.append("&rtag=");
		stringBuilder.append(rtag);
		stringBuilder.append("&t=");
		stringBuilder.append(t);
		stringBuilder.append("&name=");
		stringBuilder.append(name);
		stringBuilder.append("&eid=");
		stringBuilder.append(eid);
		String u = stringBuilder.toString();
		return u;
	}

	/**
	 * ????????????
	 * 
	 * @throws JSONException
	 */
	private void getWorkflowOptionsHttp() throws JSONException {
		String urlSp = EWAURLRoot()
				+ "EWA_STYLE/cgi-bin/_wf_/?ewa_wf_type=ins_get&"
				+ this.getCfgItem("APP_URL_PARAS");

		urlSp = this.replaceParameters(urlSp);
		urlSp = urlSp.replace("|", "%7c");

		UNet net = new UNet();
		net.setCookie(this._Rv.getRequest().getHeader("Cookie"));
		net.setEncode("utf-8");
		net.setUserAgent("ONEWORLD_APP 1.0");
		String httpResponse = net.doGet(urlSp);

		String tmp = httpResponse.replace("_EWA_WF=", "");
		// JSON
		JSONObject spJson = new JSONObject(tmp);

		JSONArray wfShow = spJson.getJSONArray("WF_SHOW");
		HashMap<String, String> wfShowMap1 = new HashMap<String, String>();
		for (int i = 0; i < wfShow.length(); i++) {
			String names = wfShow.getJSONObject(i).getString("XITEMS");
			String[] firstSplit = names.split(",");
			for (int m = 0; m < firstSplit.length; m++) {
				String v = firstSplit[m];
				v = v.trim();
				wfShowMap1.put(v, "GDX");
			}
		}
		this.wfShowMap = wfShowMap1;

		JSONArray spUnits = spJson.getJSONArray("UNIT");

		// ????????Id
		String cnnCur = spJson.getString("WF_CUR");
		this.wfCurUnitId = cnnCur;

		// ??????????MAP????
		HashMap<String, JSONObject> units = new HashMap<String, JSONObject>();
		for (int i = 0; i < spUnits.length(); i++) {
			JSONObject unit = spUnits.getJSONObject(i);
			units.put(unit.getString("WF_UNIT_ID"), unit);
		}

		JSONArray spCnns = spJson.getJSONArray("CNN");

		// ??????????????????????????
		JSONArray rstArr = new JSONArray();

		// ??????
		JSONObject rst0 = new JSONObject();
		rst0.put("V", "");
		rst0.put("T", "");
		rstArr.put(rst0);

		for (int ia = 0; ia < spCnns.length(); ia++) {
			JSONObject cnn = spCnns.getJSONObject(ia);
			String unitId = cnn.getString("WF_UNIT_FROM");
			if (unitId.equals(cnnCur)) {
				String unitTo = cnn.getString("WF_UNIT_TO");

				JSONObject unit = units.get(unitTo);

				String val = unit.getString("WF_UNIT_NAME");
				JSONObject rst = new JSONObject();
				rst.put("V", unitTo);
				rst.put("T", val);
				rstArr.put(rst);
			}
		}
		this.wfOptions = rstArr;
	}

}
