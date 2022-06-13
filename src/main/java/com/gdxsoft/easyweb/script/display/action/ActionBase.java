package com.gdxsoft.easyweb.script.display.action;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfSecurities;
import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.data.HorTable;
import com.gdxsoft.easyweb.data.XmlData;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.datasource.UpdateChanges;
import com.gdxsoft.easyweb.debug.DebugFrames;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.display.ItemValues;
import com.gdxsoft.easyweb.script.display.action.extend.ExtOpt;
import com.gdxsoft.easyweb.script.display.action.extend.ExtOpts;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValues;
import com.gdxsoft.easyweb.script.userConfig.UserXItems;
import com.gdxsoft.easyweb.utils.IUSymmetricEncyrpt;
import com.gdxsoft.easyweb.utils.UCookies;
import com.gdxsoft.easyweb.utils.UMail;
import com.gdxsoft.easyweb.utils.UObjectValue;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public class ActionBase {
	private static Logger LOGGER = LoggerFactory.getLogger(ActionBase.class);

	public static String COOKIE_NAME_PREFIX = "__EWA__"; // cookie加密的名称后缀
	private HttpServletResponse _Response;
	private HtmlClass _HtmlClass;
	// 所有输出的 Cookie
	private Map<String, Cookie> _OutCookes = new HashMap<String, Cookie>();
	// 所有输出的 Session
	private Map<String, Object> _OutSessions = new HashMap<String, Object>();
	private List<UpdateChanges> _LstChanges = new ArrayList<UpdateChanges>();

	/**
	 * 获取执行Sqls
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	protected ActionSqlSetItem retActionSqlSetItem(String name) throws Exception {
		UserXItemValues sqlset = this.getUserConfig().getUserActionItem().getItem("SqlSet");
		UserXItemValue sqlItem = sqlset.getItem(name);
		ActionSqlSetItem a = new ActionSqlSetItem();
		a.setUserXItemValue(sqlItem);
		a.setTransType(sqlItem.getItem("TransType"));

		String sqlExp = sqlItem.getItem("Sql");
		String[] sqlArray = sqlExp.split(";");
		a.setSqlArray(sqlArray);
		a.setSqlType(sqlItem.getItem("SqlType"));
		return a;
	}

	private XmlData getXmlData(String name) throws Exception {
		UserXItemValues sqlset = this.getUserConfig().getUserActionItem().getItem("XmlSetData");
		UserXItemValue sqlItem = sqlset.getItem(name);

		String xmlName = sqlItem.getItem("XmlName");
		String xmlTagPath = sqlItem.getItem("XmlTagPath").trim();
		String xmlFields = sqlItem.getItem("XmlFields").trim();
		String xmlLoadType = sqlItem.getItem("XmlLoadType").trim();

		xmlName = this.getItemValues().replaceParameters(xmlName, true, false);
		xmlTagPath = this.getItemValues().replaceParameters(xmlTagPath, true, false);
		xmlFields = this.getItemValues().replaceParameters(xmlFields, true, false);
		String fields[];
		if (xmlFields.trim().length() == 0) {
			UserXItems xItems = this.getUserConfig().getUserXItems();
			StringBuilder sbField = new StringBuilder();
			for (int i = 0; i < xItems.count(); i++) {
				UserXItem xItem = xItems.getItem(i);
				if (!xItem.testName("DataItem")) {
					continue;
				}
				UserXItemValues items = xItem.getItem("DataItem");
				if (items.count() == 0) {
					continue;
				}
				UserXItemValue x = items.getItem(0);
				if (!x.testName("DataField")) {
					continue;
				}
				String f = x.getItem("DataField");
				if (f.trim().length() > 0) {
					if (sbField.length() > 0) {
						sbField.append(" , ");
					}
					f = f.replace(",", ",,");
					sbField.append(f);
				}
			}
			xmlFields = sbField.toString();
		}
		fields = Utils.splitString(xmlFields, ",");

		StringBuilder sbError = new StringBuilder();
		sbError.append("\r\n<br>原始数据");
		sbError.append("\r\n<br>xmlFields=" + xmlFields);
		sbError.append("\r\n<br>xmlName=" + xmlName);

		XmlData xd = new XmlData();
		try {
			if (xmlLoadType.equalsIgnoreCase("childnode")) {
				// 子节点方式加载
				xd.readData(xmlName, fields, xmlTagPath, false);
			} else { // 属性方式加载
				xd.readData(xmlName, fields, xmlTagPath, true);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage() + "<br>\r\nXML 读取数据 " + sbError.toString());
		}
		return xd;

	}

	public void executeXml(String name) throws Exception {
		UserXItemValues sqlset = this.getUserConfig().getUserActionItem().getItem("XmlSet");

		UserXItemValue sqlItem = sqlset.getItem(name);
		String xmlDataSet = sqlItem.getItem("XmlSetData");
		XmlData xd = this.getXmlData(xmlDataSet);

		String xmlAction = sqlItem.getItem("XmlAction").trim();
		String xmlData = sqlItem.getItem("XmlData");
		String xmlWhere = sqlItem.getItem("XmlWhere").trim();
		String XmlFields = sqlItem.getItem("XmlFields").trim();
		xmlAction = this.getItemValues().replaceParameters(xmlAction, true);
		xmlData = this.getItemValues().replaceParameters(xmlData, true, true);

		ArrayList<String> alFields = new ArrayList<String>();
		ArrayList<String> alVals = new ArrayList<String>();

		if (XmlFields.trim().length() == 0) {
			for (int i = 0; i < xd.getTable().getColumns().getCount(); i++) {
				DTColumn col = xd.getTable().getColumns().getColumn(i);
				alFields.add(col.getName());
				if (xmlData.trim().length() == 0) {
					String v = this.getItemValues().getValue(col.getName(), col.getName());
					alVals.add(v);
				}
			}
		} else {
			String[] fs = Utils.splitString(XmlFields, ",");
			String[] vs = Utils.splitString(xmlData, ",");
			for (int i = 0; i < fs.length; i++) {
				alFields.add(fs[i]);
				if (xmlData.trim().length() == 0) {
					String v = this.getItemValues().getValue(fs[i], fs[i]);
					v = v.replace("\r", "#x0D;").replace("\n", "#x0A;");
					alVals.add(v);
				} else {
					alVals.add(vs[i].replace("\r", "#x0D;").replace("\n", "#x0A;"));
				}
			}
		}
		xmlWhere = this.getItemValues().replaceParameters(xmlWhere, true, false);

		StringBuilder sbError = new StringBuilder();
		sbError.append("\r\n<br>原始数据");
		sbError.append("\r\n<br>xmlWhere=" + xmlWhere);
		sbError.append("\r\n<br>xmlData=" + xmlData);
		sbError.append("\r\n<br>xmlAction=" + xmlAction);

		// load,insert,update,inertOrUpdate,deletes
		// 加载数据,新数据,更新数据,新或更新多条数据,删除数据
		if (xmlAction.equalsIgnoreCase("load")) {
			if (xmlWhere == null || xmlWhere.trim().length() == 0) {
				this.getDTTables().add(xd.getTable());
			} else {
				DTTable tb = new DTTable();
				try {
					tb.setColumns(xd.getTable().getColumns());

					Integer[] rst = xd.getTable().getIndexes().find(xmlWhere);
					if (rst != null) {
						for (int i = 0; i < rst.length; i++) {
							DTRow row = xd.getTable().getRow(rst[i].intValue());
							tb.getRows().addRow(row);
						}
					}
				} catch (Exception e) {
					throw new Exception(e.getMessage() + "<br>\r\nXML find " + sbError.toString());
				}
				this.getDTTables().add(tb);
			}
		} else if (xmlAction.equalsIgnoreCase("insert")) {
			try {
				xd.insertRow(alFields, alVals);
				xd.save();
			} catch (Exception e) {
				throw new Exception(e.getMessage() + "<br>\r\nXML insertRow " + sbError.toString());
			}
		} else if (xmlAction.equalsIgnoreCase("update")) {
			try {
				Integer[] rst;
				if (xmlWhere.trim().length() == 0) {
					rst = new Integer[1];
					rst[0] = xd.getTable().getCount() - 1;
					if (rst[0] < 0) {
						rst = null;
					}
				} else {
					rst = xd.getTable().getIndexes().find(xmlWhere);
				}
				if (rst != null && rst.length > 0) {
					for (int i = 0; i < rst.length; i++) {
						xd.updateRow(rst[i], alFields, alVals);
					}
					xd.save();
				}
			} catch (Exception e) {
				throw new Exception(e.getMessage() + "<br>\r\nXML updateRow " + sbError.toString());
			}
		} else if (xmlAction.equalsIgnoreCase("inertOrUpdate")) {
			ArrayList<ArrayList<String>> vals = new ArrayList<ArrayList<String>>();
			if (xmlData.trim().length() == 0) {
				vals.add(alVals);
			} else {
				String[][] vs = Utils.split2String(xmlData, ";", ",");
				for (int i = 0; i < vs.length; i++) {
					ArrayList<String> v = new ArrayList<String>();
					for (int m = 0; m < vs[i].length; m++) {
						v.add(vs[i][m]);
					}
					vals.add(v);
				}
			}
			ArrayList<String> pks = new ArrayList<String>();
			String[] pks1 = Utils.splitString(xmlWhere, ",");
			for (int i = 0; i < pks1.length; i++) {
				pks.add(pks1[i]);
			}
			try {
				if (pks.size() == 0) {
					throw new Exception("XMLDATA inertOrUpdate 方法必须制定 XmlWhere参数，请修改设置！");
				}
				xd.updateOrInsertRows(alFields, vals, pks);
				xd.save();
			} catch (Exception e) {
				throw new Exception(e.getMessage() + "<br>\r\nXML updateOrInsertRows 错误" + sbError.toString());
			}
		} else if (xmlAction.equalsIgnoreCase("deletes")) {
			Integer[] rst;
			while ((rst = xd.getTable().getIndexes().find(xmlWhere)) != null) {
				xd.getTable().getRows().deleteRow(rst[0]);
			}
			xd.save();
		}
	}

	/**
	 * 执行JSON调用
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void executeCallJSon(String name) throws Exception {
		this.getDebugFrames().addDebug(this, "executeCallJSon-开始", name);

		UserXItemValues sqlset = this.getUserConfig().getUserActionItem().getItem("JSONSet");
		UserXItemValue sqlItem = sqlset.getItem(name);
		String sqlExp = sqlItem.getItem("JSON");

		JSONArray jsonArray = new JSONArray(sqlExp);
		RequestValue rv = this.getRequestValue();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject act = jsonArray.getJSONObject(i);
			String actStr = this._HtmlClass.getItemValues().replaceJsParameters(act.toString());

			act = new JSONObject(actStr);
			ActionJSONParameter actionJSONParameter = new ActionJSONParameter();
			actionJSONParameter.init(act);

			if (actionJSONParameter.isAttacheQuery()) {
				// 将Query本地数据带过去
				MTable querys = rv.getPageValues().getQueryValues();
				this.attachJsonPostData(querys, actionJSONParameter.getQueries());
			}

			if (actionJSONParameter.isAttachePost()) {
				// 将Form本地数据带过去
				MTable forms = rv.getPageValues().getFormValues();
				this.attachJsonPostData(forms, actionJSONParameter.getData());
			}

			if (actionJSONParameter.isAttacheCookies()) {
				// 将本地的cookie带过去
				actionJSONParameter.getHeaders().put("cookie", rv.getRequest().getHeader("cookie"));
			}

			if (StringUtils.isBlank(actionJSONParameter.getConnConfigName())) {
				String dataSource = this.getPageItemValue("DataSource", "DataSource");
				actionJSONParameter.setConnConfigName(dataSource);
			}

			ActionJSON actionJson = new ActionJSON();
			actionJson.setActionBase(this);

			this.getDebugFrames().addDebug(this, "JSON-Q-开始", act.toString(4));
			ArrayList<DTTable> tbs = actionJson.action(actionJSONParameter);
			this.getDebugFrames().addDebug(this, "JSON-Q-结束", actionJson.getNet().getLastStatusCode() + "");

			// 放到全局中，用于sql等访问 EWA_API.REQ_ID0， EWA_API.REQ_ID1 ...
			this.getRequestValue().addOrUpdateValue("EWA_API.REQ_ID" + i, actionJson.getReqId());
			this.getRequestValue().addOrUpdateValue("EWA_API.REQ_OPT_MD5" + i, actionJson.getReqOptMd5());

			for (int ia = 0; ia < tbs.size(); ia++) {
				DTTable tb = tbs.get(ia);
				// JSON请求默认不用作为列表数据
				if (tb.getAttsTable().containsKey("AsLfData") && Utils.cvtBool(tb.getAttsTable().get("AsLfData"))) {
					if (tb.getCount() == 1) {
						this.addDTTableToRequestValue(tb);
					}
					this.getDTTables().add(tb);
				}

				if (actionJson.isFromCache()) {
					// 来自缓存的数据不进行本地保存
					continue;
				}

				// ActionJSONParameterListTag
				if (!tb.getAttsTable().containsKey("tag")) {
					continue;
				}

				Object param = tb.getAttsTable().get("tag");
				ActionJSONParameterListTag p = null;
				try {
					p = (ActionJSONParameterListTag) param;
					// 本地保存
					if (p.isSaveValid()) {
						actionJson.saveData(tb, p);
					}
				} catch (Exception err) {
					LOGGER.error("actionJson.save {}, {}", err.getMessage(),
							p == null ? "" : p.getJsonObject().toString(4));
				}
			}

		}
		this.executeSessionsCookies(sqlItem);
		this.getDebugFrames().addDebug(this, "executeCallJSon-结束", name);
	}

	/**
	 * 附件JSON的本地参数
	 * 
	 * @param querys
	 * @param data
	 * @param paras
	 */
	private void attachJsonPostData(MTable querys, Map<String, String> paras) {
		for (int ia = 0; ia < querys.getCount(); ia++) {
			Object key = querys.getKey(ia);
			PageValue val = (PageValue) querys.get(key);
			String v = val.getStringValue();
			if (v != null) {
				String val_name = val.getName();
				if (val_name.toUpperCase().equals("XMLNAME") || val_name.toUpperCase().equals("ITEMNAME")
						|| val_name.toUpperCase().equals("EWA_AJAX")) {
					continue;
				}
				// 去除重复值
				if (paras.containsKey(val_name)) {
					continue;
				}
				paras.put(val_name, v);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#executeCallClass(java.lang
	 * .String)
	 */
	public void executeCallClass(String name) throws Exception {
		UserXItemValues sqlset = this.getUserConfig().getUserActionItem().getItem("ClassSet");
		UserXItemValue sqlItem = sqlset.getItem(name);
		String className = sqlItem.getItem("ClassName");
		String conData = sqlItem.getItem("ConData").trim();
		String methodName = sqlItem.getItem("MethodName");
		String methodData = sqlItem.getItem("MethodData").trim();
		String xmlTag = sqlItem.getItem("XmlTag").trim();
		Object[] objConData = executeCallClassCreateObjects(conData);
		Object[] objMethodData = executeCallClassCreateObjects(methodData);

		this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "开始调用Class (" + className + ", " + methodName + ")");

		Object oo = null;
		RequestValue rv = this._HtmlClass.getItemValues().getRequestValue();

		// 反射生成
		UObjectValue ov = new UObjectValue();
		if (className.toUpperCase().endsWith("#SESSION")) {
			// 从Session中取
			String className1 = className.split("#")[0];
			Object sessionObject = rv.getSession().getAttribute(className1);
			String[] vals = methodData.split(",");
			Object[] objs = new Object[vals.length];
			for (int i = 0; i < vals.length; i++) {
				if (vals[i].startsWith("@")) {
					objs[i] = rv.getString(vals[i].replace("@", ""));
				} else {
					objs[i] = vals[i];
				}
			}

			oo = ov.invoke(sessionObject, methodName, objs);
		} else {

			boolean isok = ov.loadClass(className, objConData, methodName, objMethodData, rv);
			if (!isok) {
				this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回错误");
				return;
			}

			oo = ov.getObject();
		}
		if (ov.getLastErrMsg() != null) {
			this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用CLASS错误，" + ov.getLastErrMsg());
			this._HtmlClass.getSysParas().getRequestValue().addValue("EWA_CLASS_CALL_EXCEPTION",
					Utils.textToJscript(ov.getLastErrMsg()));
		} else {
			if (oo == null) {
				this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回空值");
				return;
			}
			this.createClassData(oo, xmlTag);
		}
	}

	@SuppressWarnings("unchecked")
	void createClassData(Object oo, String xmlTag) {
		if (oo.getClass().equals(String.class)) {
			String v1 = oo.toString();
			if (xmlTag != null && xmlTag.trim().length() > 0) {
				// 有XMLTag
				DTTable tb = new DTTable();
				tb.initData(v1, xmlTag);
				if (tb.isOk()) {
					this.getDTTables().add(tb);
				}
				this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回XML字符串");
				this._HtmlClass.getDebugFrames().addDebug(this, "ACT", Utils.textToInputValue(v1));
			} else { // 无XMLTag ，有返回值如a=1&b=2
				String[] v2 = v1.split("\\&");
				for (int i = 0; i < v2.length; i++) {
					String[] v3 = v2[i].split("\\=");
					if (v3.length == 2) {// 放入到参数值中
						this._HtmlClass.getSysParas().getRequestValue().addValue(v3[0], v3[1], PageValueTag.SYSTEM);
					}
				}
				this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回URL字符串");
			}
		} else if (oo.getClass().isArray()) {
			// 返回的是数组对象
			DTTable tb = new DTTable();
			Object[] oo1 = (Object[]) oo;
			List<Object> o = new ArrayList<Object>();
			for (int i = 0; i < oo1.length; i++) {
				o.add(oo1[i]);
			}
			tb.initData(o);
			if (tb.isOk()) {
				this.getDTTables().add(tb);
			}
			this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回数字对象, " + oo.getClass().toString());
		} else if (oo.getClass().equals(java.util.List.class) || oo.getClass().equals(java.util.ArrayList.class)) {
			// 返回的是List
			DTTable tb = new DTTable();
			List<Object> o = (List<Object>) oo;
			tb.initData(o);
			if (tb.isOk()) {
				this.getDTTables().add(tb);
			}
			this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返List对象, " + oo.getClass().toString());
		} else if (oo.getClass().equals(java.util.HashMap.class) || oo.getClass().equals(java.util.Map.class)) {
			// 返回map
			DTTable tb = new DTTable();
			HashMap<Object, Object> o = (HashMap<Object, Object>) oo;
			tb.initData(o);
			if (tb.isOk()) {
				this.getDTTables().add(tb);
			}
			this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回Map对象, " + oo.getClass().toString());
		} else if (oo.getClass().equals(DTTable.class)) {
			// 返回表
			DTTable tb = (DTTable) oo;
			this.getDTTables().add(tb);

			this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回Map对象, " + oo.getClass().toString());
		} else if (oo.getClass().equals(org.json.JSONObject.class)) {// 返回json
																		// object
			this.getJSONObjects().add(oo);
			this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回 JSON 对象, " + oo.getClass().toString());
		} else {
			UObjectValue o = new UObjectValue();
			o.setObject(oo);
			for (int i = 0; i < o.getGetterMethods().size(); i++) {
				Method m = o.getGetterMethods().get(i);
				int mo = m.getModifiers();
				if (mo == 0) {
					// private;
					continue;
				}
				String n1 = m.getName();
				if (n1.toUpperCase().indexOf("GET") == 0) {
					n1 = n1.substring(3);
				} else if (n1.toUpperCase().indexOf("IS") == 0) {
					n1 = n1.substring(2);
				}
				String v = o.getValue(m);
				if (v != null) {
					this._HtmlClass.getSysParas().getRequestValue().addValue(n1, v, PageValueTag.SYSTEM);
				}
			}
			this._HtmlClass.getDebugFrames().addDebug(this, "ACT", "调用完成，并返回对象, " + oo.getClass().toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#executeCallClassCreateObjects
	 * (java.lang.String)
	 */
	public Object[] executeCallClassCreateObjects(String strData) {
		if (strData == null)
			return null;
		strData = strData.trim();
		if (strData.length() == 0)
			return null;
		String[] aConData = strData.split(",");
		Object[] objData = new Object[aConData.length];
		for (int i = 0; i < aConData.length; i++) {
			objData[i] = this.getItemValues().replaceParameters(aConData[i].trim(), true);
		}
		return objData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#executeCallScript(java.lang
	 * .String)
	 */
	public String executeCallScript(String name) throws Exception {
		// <ScriptSet>
		// <Set Name="提醒" ScriptType="javascript"><![CDATA[alert(1)]]></Set>
		// </ScriptSet>
		UserXItemValues urlSet = this.getUserConfig().getUserActionItem().getItem("ScriptSet");
		UserXItemValue urlItem = urlSet.getItem(name);
		String s1 = "\r\n" + urlItem.getItem("Script");
		this.executeSessionsCookies(urlItem);
		return s1;
	}

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 * @param sqlType
	 * @param name
	 * @return false = 检查执行提交时返回的错误判断
	 */
	public boolean executeSql(String sql, String sqlType, String name) {
		boolean isSelect = DataConnection.checkIsSelect(sql);
		if (sqlType.equals("procedure")) {
			this.executeSqlProcdure(sql);
			// 检查执行提交时返回的错误判断
			return StringUtils.isBlank(this.getChkErrorMsg());
		} else if (isSelect) { // 不再判断 sqlType = query or update
			// 查询
			DTTable dt = this.executeSqlQuery(sql);
			dt.setName(name);
			// 检查执行提交时返回的错误判断
			return StringUtils.isBlank(this.getChkErrorMsg());
		} else {// 更新
			this.executeSqlUpdate(sql);
			return true;
		}
	}

	/**
	 * 执行SQL语句
	 * 
	 * @param name SqlSet名称
	 * @throws Exception
	 */
	public void executeCallSql(String name) throws Exception {
		ActionSqlSetItem sqlItem = this.retActionSqlSetItem(name);
		String[] sqlArray = sqlItem.getSqlArray();
		String transType = sqlItem.getTransType();
		boolean isTrans = transType.equalsIgnoreCase("yes") ? true : false;
		DataConnection cnn = this.getItemValues().getSysParas().getDataConn();

		if (isTrans) {
			cnn.transBegin();
		}
		for (int i = 0; i < sqlArray.length; i++) {
			String sql = sqlArray[i].trim();
			if (sql.length() == 0) {// 空语句
				continue;
			}
			String sqlType = sqlItem.getSqlType();

			this.executeSql(sql, sqlType, name);
			if (StringUtils.isNotBlank(cnn.getErrorMsg())) {
				if (isTrans) {
					cnn.transRollback();
				}
				cnn.close();
				throw new Exception(cnn.getErrorMsg());
			}
			if (StringUtils.isNotBlank(this.getChkErrorMsg())) { // 检查执行提交时返回的错误判断
				if (isTrans) { // 事务回滚
					cnn.transRollback();
				}
				cnn.close();
				return;
			}
		}
		if (isTrans) {
			cnn.transCommit();
		}
		this.executeSessionsCookies(sqlItem.getUserXItemValue());
	}

	/**
	 * 需要的话，替换此方法
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public String createSql(String sql) throws Exception {
		return sql;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#exceuteCallUrl(java.lang.
	 * String )
	 */
	public String exceuteCallUrl(String name) throws Exception {
		// <UrlSet>
		// <Set Name="到主页" Url="./index.jsp">
		// <SessionSet>
		// <Set Name="USER_ID" Type="save" />
		// <Set Name="USER_NAME" Type="save" />
		// </SessionSet>
		// <CookieSet>
		// <Set Name="USER_ID" Type="save" Life="100"
		// Domain="." />
		// <Set Name="USER_NAME" Type="save" Life="100"
		// Domain="." />
		// </CookieSet>
		// </Set>
		// <Set Name="退出" Url="./logout.jsp" />
		// </UrlSet>
		UserXItemValues urlSet = this.getUserConfig().getUserActionItem().getItem("UrlSet");
		UserXItemValue urlItem = urlSet.getItem(name);
		String url = getItemValues().replaceParameters(urlItem.getItem("Url"), true);
		// 调用Sessions和Cookies

		this.executeSessionsCookies(urlItem);
		if (url.trim().length() > 0) {
			url = url.replace("|", "%7c");
			String s1 = "document.location.href=\"" + Utils.textToJscript(url) + "\"";
			return s1;
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#executeSessionsCookies(com
	 * .gdxsoft.easyweb.script.userConfig.UserXItemValue)
	 */
	public void executeSessionsCookies(UserXItemValue userXItemValue) throws Exception {
		if (userXItemValue.getChildren().size() == 0) {
			return;
		}

		java.util.Iterator<String> it = userXItemValue.getChildren().keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			UserXItemValues subVals = userXItemValue.getChildren().get(key);
			for (int i = 0; i < subVals.count(); i++) {
				if (this._Response == null) {
					throw new Exception("执行executeSessionsCookies时response 为空");
				}

				UserXItemValue s = subVals.getItem(i);
				this.executeSessionCookie(s);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#executeSessionCookie(com.
	 * gdxsoft .easyweb.script.userConfig.UserXItemValue)
	 */
	public void executeSessionCookie(UserXItemValue uxv) throws Exception {

		// <Set Name="SYS_ADMIN_ID" ParaName="USER_LOGIN_ID"
		// CSType="all" Life="" Domain="" Option="C" />
		String type = uxv.getItem("CSType");

		if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("session")) {
			this.executeSession(uxv);
		}

		if (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("cookie")) {
			this.executeCookie(uxv);
		}
	}

	/**
	 * Output the session
	 * 
	 * @param uxv
	 * @throws Exception
	 */
	private void executeSession(UserXItemValue uxv) throws Exception {
		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();

		String name = uxv.getItem("Name").trim().toUpperCase();
		String option = uxv.getItem("Option");
		String paraName = uxv.getItem("ParaName");
		String val = this.getItemValues().getValue(paraName, paraName);
		if (val == null) {
			// 空值就删除，避免当使用 redis作为session管理时，出现错误
			rv.getSession().removeAttribute(name);
		} else if (option.equalsIgnoreCase("C")) { // create
			rv.getSession().setAttribute(name, val);
			// 记录输出的 session
			this._OutSessions.put(name, val);
		} else {
			rv.getSession().removeAttribute(name);
		}
	}

	/**
	 * Output the cookie
	 * 
	 * @param uxv
	 * @throws Exception
	 */
	private void executeCookie(UserXItemValue uxv) throws Exception {
		if (ConfSecurities.getInstance().getDefaultSecurity() == null) {
			String err = "No default symmetric defined, in the ewa_conf.xml securities->security";
			LOGGER.error(err);
			throw new Exception(err);
		}
		IUSymmetricEncyrpt security = ConfSecurities.getInstance().getDefaultSecurity().createSymmetric();

		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();

		String name = uxv.getItem("Name").trim().toUpperCase();
		String option = uxv.getItem("Option");
		String domain = uxv.getItem("Domain");
		String life = uxv.getItem("Life");

		// 郭磊 2017-08-24 增加，用于更改cookie的域
		PageValue pv = rv.getPageValues().getPageValue("EWA_COOKIE_DOMAIN");
		if (pv != null && (pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS)) {
			// 只能用于HtmlControl调用
			domain = pv.getValue().toString();
		}

		String paraName = uxv.getItem("ParaName");
		String val = this.getItemValues().getValue(paraName, paraName);

		int cookieAge = -1;

		if (option.equalsIgnoreCase("C") && val != null) { // create
			if (StringUtils.isNotBlank(life)) {
				try {
					cookieAge = Integer.parseInt(life);
				} catch (Exception e) {
					LOGGER.warn("Invalid cookie life: " + life, e.getLocalizedMessage());
					cookieAge = -1;
				}
			}
		} else { // remove cookie
			cookieAge = 0;
		}
		UCookies us = new UCookies(security);

		// 默认 a=-1 跟随浏览器session
		if (cookieAge >= 0) {
			us.setMaxAgeSeconds(cookieAge); // 单位秒
		}
		if (domain.trim().length() > 0) {
			us.setPath(domain);
		} else {
			us.setPath(rv.getContextPath());
		}

		if ("https".equals(this.getRequestValue().getRequest().getHeader("X-Forwarded-Scheme"))) {
			us.setSecret(true);
		} else {
			us.setSecret(false);
		}
		us.setHttpOnly(true);

		Cookie newCookie = us.addCookie(name, val, this._Response);

		// 记录输出的cookie
		this._OutCookes.put(newCookie.getName(), newCookie);
	}

	/**
	 * @deprecated Output the cookie
	 * 
	 * @param uxv
	 * @throws Exception
	 */
	void executeCookieold(UserXItemValue uxv) throws Exception {
		if (ConfSecurities.getInstance().getDefaultSecurity() == null) {
			String err = "No default symmetric defined, in the ewa_conf.xml securities->security";
			LOGGER.error(err);
			throw new Exception(err);
		}
		IUSymmetricEncyrpt security = ConfSecurities.getInstance().getDefaultSecurity().createSymmetric();

		RequestValue rv = this._HtmlClass.getSysParas().getRequestValue();

		String name = uxv.getItem("Name").trim().toUpperCase();
		String option = uxv.getItem("Option");
		String domain = uxv.getItem("Domain");
		String life = uxv.getItem("Life");

		// 郭磊 2017-08-24 增加，用于更改cookie的域
		PageValue pv = rv.getPageValues().getPageValue("EWA_COOKIE_DOMAIN");
		if (pv != null && (pv.getPVTag() == PageValueTag.HTML_CONTROL_PARAS)) {
			// 只能用于HtmlControl调用
			domain = pv.getValue().toString();
		}

		String paraName = uxv.getItem("ParaName");
		String val = this.getItemValues().getValue(paraName, paraName);
		String ckName = name + COOKIE_NAME_PREFIX;

		String cookieValue = null;
		int cookieAge = -1;

		if (option.equalsIgnoreCase("C") && val != null) { // create
			if (StringUtils.isNotBlank(life)) {
				try {
					cookieAge = Integer.parseInt(life);
				} catch (Exception e) {
					LOGGER.warn("Invalid cookie life: " + life, e.getLocalizedMessage());
					cookieAge = -1;
				}
			}
			String cVal = security.encrypt(val);
			cookieValue = UCookies.encodeCookieValue(cVal);
		} else { // remove cookie
			cookieAge = 0;
		}

		Cookie cookie = new Cookie(ckName, cookieValue);
		// 默认 a=-1 跟随浏览器session
		if (cookieAge >= 0) {
			cookie.setMaxAge(cookieAge); // 单位秒
		}
		if (domain.trim().length() > 0) {
			cookie.setPath(domain);
		} else {
			cookie.setPath(rv.getContextPath());
		}
		this.addCookie(cookie);
	}

	/**
	 * @deprecated
	 * @param cookie
	 */
	public void addCookie(Cookie cookie) {
		if (cookie == null) {
			return;
		}
		if ("https".equals(this.getRequestValue().getRequest().getHeader("X-Forwarded-Scheme"))) {
			cookie.setSecure(true);
		}
		cookie.setHttpOnly(true);
		// 记录输出的cookie
		this._OutCookes.put(cookie.getName(), cookie);

		// 使用标准的输出方法
		this._Response.addCookie(cookie);

		/*
		 * MStr s = new MStr(); // Set-Cookie:G_ADM_UNID__EWA__=
		 * TxcW7fr2HY6WYLEn8hj3Q78IG3lvkSpmymAwL0nJERk8EuvwdyUjxA%3D%3D; // Expires=Tue,
		 * 19-Apr-2016 02:05:54 GMT; Path=/cm-b2b-ex s.a(cookie.getName()); s.a("="); if
		 * (cookie.getValue() != null) { s.a(cookie.getValue()); } //
		 * Set-Cookie:JSESSIONID=2221EC98D8C089036065E4F31FFB42DF; // Path=/cm-b2b-ex if
		 * (cookie.getMaxAge() > 0) { Calendar c = Calendar.getInstance();
		 * c.add(Calendar.SECOND, cookie.getMaxAge()); String dt =
		 * Utils.getDateGMTString(c); s.a("; Expires=" + dt); } else if
		 * (cookie.getMaxAge() == 0 ) { // 删除cookie Calendar c = Calendar.getInstance();
		 * c.add(Calendar.YEAR, -2); String dt = Utils.getDateGMTString(c);
		 * s.a("; Expires=" + dt); } if (cookie.getPath() != null &&
		 * cookie.getPath().trim().length() > 0) { s.a("; Path=" + cookie.getPath()); }
		 * if (cookie.getSecure()) { s.a("; Secure"); } cookie.setHttpOnly(true);
		 * s.a("; HttpOnly");
		 * 
		 * // 当是https请求时，需要配置 nginx // proxy_set_header X-Forwarded-Scheme $scheme; if
		 * ("https".equals(this.getRequestValue().getRequest().getHeader(
		 * "X-Forwarded-Scheme"))) { s.a("; secure"); cookie.setSecure(true); } // ;
		 * SameSite=Lax 老版本浏览器不支持
		 * 
		 * this._Response.addHeader("Set-Cookie", s.toString());
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#getPageItemValue(java.lang
	 * .String, java.lang.String)
	 */
	public String getPageItemValue(String itemName, String tagName) {
		return this.getHtmlClass().getHtmlCreator().getPageItemValue(itemName, tagName);
	}

	/**
	 * 去除SQL表达式的备注 /××/
	 * 
	 * @param sql
	 * @return
	 */
	public String getSql(String sql) {
		return DataConnection.removeSqlMuitiComment(sql);
	}

	/**
	 * 执行存储过程，并将返回参数放到RequestValue中
	 * 
	 * @param sql
	 */
	public void executeSqlProcdure(String sql) {
		String sql1 = getSql(sql);
		DataConnection conn = this.getItemValues().getDataConn();

		HashMap<String, Object> a = conn.executeProcdure(sql1);
		java.util.Iterator<String> it = a.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Object val = a.get(key);
			PageValue pv = new PageValue();
			pv.setPVTag(PageValueTag.DTTABLE);
			pv.setValue(val);
			pv.setName(key);
			pv.autoDetectDataType();

			this.getItemValues().getRequestValue().addValue(pv);
		}
		this.checkActionErrorOutInProcdure();
		this.executeExtOpt(sql);
	}

	/**
	 * 执行更新
	 * 
	 * @param sql
	 */
	public void executeSqlUpdate(String sql) {
		if (DataConnection.checkIsProcdure(sql)) {
			// 存储过程
			this.executeSqlProcdure(sql);
			return;
		}

		String sql1 = getSql(sql); // 删除sql备注 /**/
		DataConnection conn = this.getItemValues().getDataConn();

		// 查找自增的sql, 例如 -- auto MEMO_ID
		String auto_field = DataConnection.getAutoField(sql1);
		if (auto_field != null && auto_field.length() > 0) {
			// 执行自增的插入
			int autov = conn.executeUpdateReturnAutoIncrement(sql1);
			if (autov > -1) {
				this.getRequestValue().addValue(auto_field, autov);
			}
		} else if (DataConnection.isComparativeChanges(sql1)) {
			// 比较更新前和更新后字段的变化
			// 记录更新前后变化的 ，方式 -- COMPARATIVE_CHANGES
			UpdateChanges ucs = conn.executeUpdateAndReturnChanges(sql1);
			// 记录到变化列表
			_LstChanges.add(ucs);
		} else {
			conn.executeUpdate(sql1);
		}
		this.executeExtOpt(sql);
	}

	/**
	 * 执行查询
	 * 
	 * @param sql
	 */
	public DTTable executeSqlQuery(String sql) {
		String sql1 = getSql(sql);
		DataConnection conn = this.getItemValues().getDataConn();
		conn.executeQuery(sql1);

		DTTable tb = new DTTable(); // 映射到自定义数据表
		tb.initData(this.getItemValues().getDataConn().getLastResult().getResultSet());
		try {
			this.getItemValues().getDataConn().getLastResult().getResultSet().close();
		} catch (SQLException e) {
			LOGGER.warn("close the result. {} {}", e.getMessage(), sql);
		}
		if (!tb.isOk()) {
			conn.setErrorMsg("tb error, " + sql);
			return null;
		}

		// 执行SQL的描述
		this.executeExtOpt(sql, tb);
		if (tb.getName() != null && tb.getName().equals("!!JOIN_DROP!!")) {
			conn.getResultSetList().removeValue(conn.getLastResult());
		} else {
			this.getDTTables().add(tb);
		}

		if (!this.checkActionErrorOutInTable(tb)) {
			if (tb.getCount() == 1) {
				this.addDTTableToRequestValue(tb);
			}
		}
		return tb;

	}

	/**
	 * 执行SQL扩展
	 * 
	 * @param sql
	 */
	void executeExtOpt(String sql) {
		executeExtOpt(sql, null);
	}

	/**
	 * 执行SQL扩展
	 * 
	 * @param sql
	 * @param fromTable
	 */
	void executeExtOpt(String sql, DTTable fromTable) {
		ExtOpts opts = new ExtOpts();
		opts.init(sql);

		if (opts.size() == 0) {
			return;
		}

		for (int i = 0; i < opts.size(); i++) {
			ExtOpt opt = opts.get(i);
			if (opt.getWorkType().equals("HOR")) {
				this.executeExtHorTable(opt, fromTable);
			} else if (opt.getWorkType().equals("JOIN")) {
				executeExtJoin(opt, fromTable);
			} else if (opt.getWorkType().equals("JOIN_HOR")) {
				executeExtJoinHor(opt, fromTable);
			}
		}
	}

	/**
	 * 执行纵向表转为横向表<br>
	 * {"TYPE":"HOR", "RUN_TYPE": "U", "FROM_TABLE": "WEB_USER", "UNID":"XX",
	 * "WHERE":"A=@AA"}
	 * 
	 * @param opt
	 * @param fromTable
	 */
	public void executeExtHorTable(ExtOpt opt, DTTable fromTable) {
		// {"RUN_TYPE": "U", "FROM_TABLE": "WEB_USER", "UNID":"XX",
		// "WHERE":"A=@AA"}
		DataConnection conn = this.getItemValues().getDataConn();
		String runType = opt.getPara("RUN_TYPE");
		String fromTableName = opt.getPara("FROM_TABLE");
		if (runType == null || fromTableName == null || fromTableName.trim().length() == 0) {
			return;
		}

		String unid = opt.getPara("UNID");
		String where = opt.getPara("WHERE");

		try {

			HorTable ht = new HorTable(fromTableName, conn);
			ht.setRunType(runType);
			ht.setUpdateUNID(unid);
			ht.setWhere(where);

			if (runType.toUpperCase().equals("Q") && fromTable != null) {
				ht.loadHorTable(fromTable);
				this._HtmlClass.getDebugFrames().addDebug(this, "SQL", "HorTable Q");
			} else if (ht.getRunType().toUpperCase().equals("U")) {
				ht.setUpdateUNID(unid);

				ht.update();
				this._HtmlClass.getDebugFrames().addDebug(this, "SQL", "HorTable U");
			} else if (ht.getRunType().toUpperCase().equals("D")) {

				ht.delete();
				this._HtmlClass.getDebugFrames().addDebug(this, "SQL", "HorTable D");
			} else {
				this._HtmlClass.getDebugFrames().addDebug(this, "SQL", "!! NOT RUN !! HorTable " + runType);
			}
		} catch (Throwable e) {
			this.getDebugFrames().addDebug(this, "ERR", opt.getJsonStr() + "[" + e.getMessage());
		}
	}

	/**
	 * 执行连接表<br>
	 * {"TYPE":"JOIN", "FROM_KEYS": "TAG1,TAG2", "TO_KEYS": "BAS_TAG1,BAS_TAG2"}
	 * 
	 * @param opt
	 * @param fromTable
	 */
	void executeExtJoin(ExtOpt opt, DTTable fromTable) {
		if (this.getItemValues().getDTTables().size() == 0) {
			// 没有可以合并的表
			this.getDebugFrames().addDebug(this, "WAR", "没有可以合并的表." + opt.getJsonStr());
			return;
		}
		DTTable lastTb = (DTTable) this.getItemValues().getDTTables().getLast();
		String fromKeys = opt.getPara("FROM_KEYS");
		String toKeys = opt.getPara("TO_KEYS");
		String[] fKeys = fromKeys.split(",");
		String[] tKeys = toKeys.split(",");
		try {
			lastTb.join(fromTable, fKeys, tKeys);
			this.getDebugFrames().addDebug(this, "SQL", opt.getJsonStr());
			fromTable.setName("!!JOIN_DROP!!");
		} catch (Throwable e) {
			this.getDebugFrames().addDebug(this, "ERR", opt.getJsonStr() + "[" + e.getMessage() + "]");
		}
	}

	/**
	 * 连接纵向表<br>
	 * {"TYPE":"JOIN_HOR", "FROM_KEYS": "TAG1,TAG2", "TO_KEYS": "BAS_TAG1,BAS_TAG2",
	 * "FIELDS":"A1,A2, A3", "NAME_KEY":"DT_NAME", "NAME_KEY":"DT_VAL"}
	 * 
	 * @param opt
	 * @param fromTable
	 */
	void executeExtJoinHor(ExtOpt opt, DTTable fromTable) {
		if (this.getItemValues().getDTTables().size() == 0) {
			// 没有可以合并的表
			this.getDebugFrames().addDebug(this, "WAR", "没有可以合并的表." + opt.getJsonStr());
			return;
		}
		DTTable lastTb = (DTTable) this.getItemValues().getDTTables().getLast();
		String fromKeys = opt.getPara("FROM_KEYS");
		String toKeys = opt.getPara("TO_KEYS");
		String fields = opt.getPara("FIELDS");
		String namedField = opt.getPara("NAME_KEY");
		String valField = opt.getPara("VALUE_KEY");
		if (fromKeys == null || toKeys == null || fields == null || namedField == null || valField == null) {
			this.getDebugFrames().addDebug(this, "WAR", "参数不足." + opt.getJsonStr());
			return;
		}
		String[] fKeys = fromKeys.split(",");
		String[] tKeys = toKeys.split(",");
		String[] addFields = fields.split(",");
		try {
			lastTb.joinHor(fromTable, fKeys, tKeys, addFields, namedField, valField);
			this.getDebugFrames().addDebug(this, "SQL", opt.getJsonStr());
			fromTable.setName("!!JOIN_DROP!!");
		} catch (Throwable e) {
			this.getDebugFrames().addDebug(this, "ERR", opt.getJsonStr() + "[" + e.getMessage() + "]");
		}
	}

	/**
	 * 将只有一行的数据放置到RequestValue中
	 * 
	 * @param dt
	 */
	void addDTTableToRequestValue(DTTable dt) {
		if (dt.getCount() != 1) {
			return;
		}
		DTRow r = dt.getRow(0);
		for (int k = 0; k < dt.getColumns().getCount(); k++) {
			DTColumn col = dt.getColumns().getColumn(k);
			Object v = r.getCell(k).getValue();
			if (v != null) { // 2015-9-16 将空值排除
				this.getRequestValue().getPageValues().addValue(col.getName(), v, PageValueTag.DTTABLE);
			}
		}
	}

	public UserConfig getUserConfig() {
		return this._HtmlClass.getUserConfig();
	}

	public ItemValues getItemValues() {
		return this._HtmlClass.getItemValues();
	}

	public MList getDTTables() {
		return this._HtmlClass.getItemValues().getDTTables();
	}

	public MList getJSONObjects() {
		return this._HtmlClass.getItemValues().getJSONObjects();
	}

	public RequestValue getRequestValue() {
		return this._HtmlClass.getSysParas().getRequestValue();
	}

	public DebugFrames getDebugFrames() {
		return this._HtmlClass.getDebugFrames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#getResponse()
	 */
	public HttpServletResponse getResponse() {
		return _Response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.script.display.IAction#setResponse(javax.servlet.
	 * http.HttpServletResponse)
	 */
	public void setResponse(HttpServletResponse response) {
		_Response = response;
	}

	/**
	 * @return the _HtmlClass
	 */
	public HtmlClass getHtmlClass() {
		return _HtmlClass;
	}

	/**
	 * @param htmlClass the _HtmlClass to set
	 */
	public void setHtmlClass(HtmlClass htmlClass) {
		_HtmlClass = htmlClass;
	}

	/**
	 * 邮件发送
	 * 
	 * @param fromName 发件人名称
	 * @param fromMail 发件人地址
	 * @param toName   收件人名称
	 * @param toEmail  收件人地址
	 * @param subject  主题
	 * @param cnt      内容
	 * @param atts     附件列表
	 * @return
	 */
	public ActionResult executeMailSend(String fromName, String fromMail, String toName, String toEmail, String subject,
			String cnt, String[] atts) {
		String rst = UMail.sendHtmlMail(fromMail, fromName, toEmail, toName, subject, cnt, atts);
		ActionResult r = new ActionResult();
		if (rst == null) {
			r.setIsOk(true);
		} else {
			r.setIsOk(false);
			r.setMsg(rst);
		}
		return r;
	}

	/**
	 * 邮件发送
	 * 
	 * @param fromMail 发件人地址
	 * @param toEmail  发件人地址
	 * @param subject  主题
	 * @param cnt      内容
	 * @param atts     附件列表
	 * @return
	 */
	public ActionResult executeMailSend(String fromMail, String toEmail, String subject, String cnt, String[] atts) {
		return this.executeMailSend("", fromMail, "", toEmail, subject, cnt, atts);
	}

	/**
	 * 发送短消息
	 * 
	 * @param cnt     内容
	 * @param mobiles 地址
	 * @return 结果
	 */
	public ActionResult executeSmsSend(String cnt, String mobiles) {
		return null;
	}

	private String chkErrorMsg;

	/**
	 * 执行前检测错误
	 * 
	 * @return the chkErrorMsg
	 */
	public String getChkErrorMsg() {
		return chkErrorMsg;
	}

	/**
	 * 执行前检测错误
	 * 
	 * @param chkErrorMsg the chkErrorMsg to set
	 */
	public void setChkErrorMsg(String chkErrorMsg) {
		this.chkErrorMsg = chkErrorMsg;
	}

	/**
	 * 在返回的表中 判断Action执行的表中是否包含错误
	 * 
	 * @param dt
	 * @return
	 */
	public boolean checkActionErrorOutInTable(DTTable dt) {
		if (dt.getCount() == 0 || !dt.getColumns().testName("EWA_ERR_OUT")) {
			return false;
		}
		try {
			for (int i = 0; i < dt.getCount(); i++) {
				String v = dt.getCell(i, "EWA_ERR_OUT").toString();
				if (v != null && v.trim().length() > 0) {
					this.setChkErrorMsg(v);
					return true;
				}
			}
		} catch (Exception e) {
			this.setChkErrorMsg("Exception, " + e.getMessage());
			return true;
		}
		return false;
	}

	/**
	 * 在存储过程中，检查执行提交时返回的错误判断
	 */
	public boolean checkActionErrorOutInProcdure() {
		// 现在参数中检查是否存在错误输出
		MTable pvs = this.getRequestValue().getPageValues().getTagValues(PageValueTag.DTTABLE);
		Object o = pvs.get("EWA_ERR_OUT");
		if (o != null) {
			PageValue pv = (PageValue) o;
			if (pv.getValue() != null) {
				String v = pv.getValue().toString();
				if (v != null && v.trim().length() > 0) {
					this.setChkErrorMsg(v);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 获取update完的所有变化列表
	 * 
	 * @return
	 */
	public List<UpdateChanges> getLstChanges() {
		return _LstChanges;
	}

	/**
	 * 获取执行过程中输出的所有Cookie
	 * 
	 * @return the _OutCookes
	 */
	public Map<String, Cookie> getOutCookes() {
		return _OutCookes;
	}

	/**
	 * 获取所有输出的Session
	 * 
	 * @return the _OutSessions
	 */
	public Map<String, Object> getOutSessions() {
		return _OutSessions;
	}
}
