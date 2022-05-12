package com.gdxsoft.easyweb.script.display.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UUrl;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * JSON调用
 * 
 * @author admin
 *
 */
public class ActionJSON {
	private static Logger LOGGER = LoggerFactory.getLogger(ActionJSON.class);
	private UNet net_;

	private String userAgent_;
	private String lastResult_;
	private boolean isDebug_ = false;

	public ActionJSON() {

	}

	public ActionJSON(String userAgent) {
		this.userAgent_ = userAgent;
	}

	public ArrayList<DTTable> action(ActionJSONParameter param) throws Exception {
		lastResult_ = this.httpRequest(param);
		if (!(this.net_.getLastStatusCode() >= 200 && this.net_.getLastStatusCode() < 400)) {
			// 错误的结果
			throw new Exception(this.net_.getLastStatusCode() + ":" + this.net_.getLastResult());
		}

		ArrayList<DTTable> al = new ArrayList<DTTable>();
		if (isJSONObject(lastResult_)) {// JSONObject
			JSONObject resultObj = new JSONObject(this.lastResult_);
			if (param.getListTags().size() == 0) {
				DTTable tb = this.fromJSONObject(resultObj);
				al.add(tb);
			} else {
				param.getListTags().forEach(tag -> {
					this.getTable(tag, resultObj, al, null);
				});

			}
		} else { // JSONArray
			JSONArray arr = new JSONArray(this.lastResult_);
			DTTable tb = this.fromJSONArray(arr);
			al.add(tb);
		}

		return al;
	}

	private boolean isJSONObject(String jsonStr) {
		int loc = jsonStr.indexOf("{");
		int loc1 = jsonStr.indexOf("[");

		if (loc1 >= 0 && loc < loc1) {// JSONObject
			return true;
		} else { // jsonArray
			return false;
		}
	}

	private void getTable(ActionJSONParameterListTag tag, JSONObject jsonData, ArrayList<DTTable> al, String pTag) {
		String tagName = tag.getTag();
		if (!jsonData.has(tagName)) {
			return;
		}
		Object o = jsonData.opt(tagName);
		DTTable tb = null;
		String tbName = pTag == null ? tagName : pTag + "." + tag;
		try {
			if (o instanceof org.json.JSONObject) {// JSONObject
				tb = this.fromJSONObject((JSONObject) o);
			} else { // JSONArray
				tb = this.fromJSONArray((JSONArray) o);
			}
			tb.getAttsTable().add("tag", tag);
			tb.setName(tbName);
			al.add(tb);
		} catch (Exception e) {
			LOGGER.error("JSON 2 TABLE, error={}, json={}", e.getMessage(), o.toString());
		}

		for (int i = 0; i < tag.getSubListTags().size(); i++) {
			ActionJSONParameterListTag subTag = tag.getSubListTags().get(i);
			JSONArray arr = new JSONArray();
			String[] keys = tag.getKey().split(",");
			try {
				// 将表指定字段拼接成JSONArray
				for (int m = 0; m < tb.getCount(); m++) {
					DTRow row = tb.getRow(m);
					String json = row.getCell(subTag.getTag()).toString();
					if (json == null || json.trim().length() == 0) {
						continue;
					}
					if (this.isJSONObject(json)) {
						JSONObject jsonObj = new JSONObject(json);
						// 添加父表主键
						this.addParentKey(jsonObj, keys, row);
						arr.put(jsonObj);
					} else {
						JSONArray jsonArr = new JSONArray(json);
						for (int k = 0; k < jsonArr.length(); k++) {
							JSONObject jsonObj = jsonArr.getJSONObject(k);
							// 添加父表主键
							this.addParentKey(jsonObj, keys, row);
							arr.put(jsonObj);
						}
					}
				}
				JSONObject subData = new JSONObject();
				subData.put(subTag.getTag(), arr);
				// 递归调用
				this.getTable(subTag, subData, al, tbName);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				continue;
			}
		}
	}

	private void addParentKey(JSONObject jsonObj, String[] keys, DTRow row) throws Exception {
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i].trim();
			jsonObj.put(key, row.getCell(key).toString());
		}
	}

	public ArrayList<DTTable> action(JSONObject actionJson) throws Exception {
		ActionJSONParameter param = new ActionJSONParameter();
		param.init(actionJson);
		return this.action(param);
	}

	private DTTable fromJSONArray(JSONArray jsonArray) throws Exception {
		DTTable tb = new DTTable();
		tb.initData(jsonArray);
		tb.getAttsTable().add("json", jsonArray);
		return tb;
	}

	private DTTable fromJSONObject(JSONObject jsonObject) throws Exception {
		JSONArray arr = new JSONArray();
		arr.put(jsonObject);
		return this.fromJSONArray(arr);
	}

	private String httpRequest(ActionJSONParameter param) throws Exception {
		UNet net = this.getNet();
		if (StringUtils.isNotBlank(param.getUserAgent())) {
			net.setUserAgent(param.getUserAgent());
		}
		param.getHeaders().forEach((k, v) -> {
			net.addHeader(k, v);
		});

		if (param.isDebug()) {
			net.setIsShowLog(true);
		}

		UUrl u = new UUrl(param.getUrl());
		param.getQueries().forEach((k, v) -> {
			u.add(k, v);
		});
		String url = u.getUrlWithDomain();
		String result;
		if ("GET".equalsIgnoreCase(param.getMethod())) {
			result = net.doGet(url);
		} else if ("POST".equalsIgnoreCase(param.getMethod())) {
			if (StringUtils.isBlank(param.getBody())) {
				result = net.postMsg(url, param.getBody());
			} else {
				result = net.doPost(url, param.getData());
			}
		} else if ("PUT".equalsIgnoreCase(param.getMethod())) {
			if (StringUtils.isBlank(param.getBody())) {
				result = net.doPut(url, param.getBody());
			} else {
				result = net.doPut(url, param.getData());
			}
		} else if ("PATCH".equalsIgnoreCase(param.getMethod())) {
			if (StringUtils.isBlank(param.getBody())) {
				result = net.doPatch(url, param.getBody());
			} else {
				result = net.doPatch(url, param.getData());
			}
		} else if ("DELETE".equalsIgnoreCase(param.getMethod())) {
			if (StringUtils.isBlank(param.getBody())) {
				if (param.getBody().length() == 0) {
					result = net.doDelete(url);
				} else {
					result = net.doDelete(url, param.getBody());
				}
			} else {
				result = net.doDelete(url, param.getData());
			}
		} else {
			throw new Exception("invalid method: " + param.getMethod());
		}

		return result;
	}

	/**
	 * 将返回数据保存到本地表中
	 * 
	 * @param tb
	 * @param saveJSON
	 * @throws Exception
	 */
	public void saveData(DTTable tb, ActionJSONParameterListTag param) throws Exception {
		if (tb == null || tb.getCount() == 0) {
			return;
		}
		/*
		 * "save": { "key": "HOTEL_ID", "table": "V_CACHED_HOTEL_MAIN" }
		 */
		String key = param.getKey();
		String table = param.getTable();

		String sql0 = "SELECT * FROM " + table + " WHERE 1=2";
		DTTable tbTmp = DTTable.getJdbcTable(sql0, param.getConnConfigName());

		String[] keys = key.split(",");
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		for (int i = 0; i < keys.length; i++) {
			String k = keys[i].trim();
			map.put(k.toUpperCase(), true);
			keys[i] = k;
			if (!tbTmp.getColumns().testName(k)) {// 字段在数据库表中不存在
				LOGGER.error("{}.{} not exists", table, k);
				throw new Exception(table + "." + k + " not exists");
			}
		}

		StringBuilder sb = new StringBuilder(); // 字段列表
		StringBuilder sb1 = new StringBuilder(); // 字段值列表
		StringBuilder sb2 = new StringBuilder(); // 更新 set 列表
		StringBuilder sb3 = new StringBuilder(); // 更新 where 列表

		for (int i = 0; i < tb.getColumns().getCount(); i++) {
			String field = tb.getColumns().getColumn(i).getName().trim();
			if (field.toUpperCase().equals("EWA_KEY")) {
				continue;
			}
			if (!tbTmp.getColumns().testName(field)) {
				// 字段在数据库表中不存在
				LOGGER.warn("{}.{} not exists", table, field);
				continue;
			}
			if (sb.length() > 0) {
				sb.append("	\n, ");
				sb1.append("	\n, ");
			}

			sb.append(field);
			sb1.append("@" + field);

			// update
			if (map.containsKey(field.toUpperCase())) {
				if (sb3.length() > 0) {
					sb3.append(" AND ");
				}
				sb3.append(field + " = @" + field);
			} else {
				if (sb2.length() > 0) {
					sb2.append("	\n,");
				}
				sb2.append(field + " = @" + field);
			}
		}

		// 加载已经存在数据 SQL
		StringBuilder exists = new StringBuilder("SELECT * FROM " + table + " WHERE ");
		for (int i = 0; i < tb.getCount(); i++) {
			StringBuilder w1 = new StringBuilder(); // where条件
			for (String key_field : map.keySet()) {
				if (w1.length() > 0) {
					w1.append(" AND ");
				}
				w1.append(key_field);
				String v = tb.getCell(i, key_field).toString();
				if (v != null) {
					w1.append("='");
					w1.append(v.replace("'", "''"));
					w1.append("'");
				} else {
					w1.append(" is null");
				}
			}
			if (i > 0) {
				exists.append("\n or ");
			}
			exists.append("(");
			exists.append(w1);
			exists.append(")");
		}

		RequestValue rv1 = new RequestValue();
		DataConnection cnn = new DataConnection(param.getConnConfigName(), rv1);
		// 已经存在数据
		DTTable tbExists = DTTable.getJdbcTable(exists.toString(), cnn);
		if (cnn.getErrorMsg() != null) {
			cnn.close();
			throw new Exception(cnn.getErrorMsg());
		}

		HashMap<String, DTRow> mapExists = new HashMap<String, DTRow>();

		for (int i = 0; i < tbExists.getCount(); i++) {
			DTRow r = tbExists.getRow(i);
			String exp = this.createKeyExp(keys, r);
			mapExists.put(exp, r);
		}

		StringBuilder sbUpdate = new StringBuilder();
		sbUpdate.append("UPDATE ");
		sbUpdate.append(table);
		sbUpdate.append(" SET ");
		sbUpdate.append(sb2);
		sbUpdate.append(" WHERE ");
		sbUpdate.append(sb3);

		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("INSERT INTO ");
		sbInsert.append(table);
		sbInsert.append(" (");
		sbInsert.append(sb);
		sbInsert.append(") VALUES(");
		sbInsert.append(sb1);
		sbInsert.append(")");

		String sqlInsert = sbInsert.toString();
		String sqlUpdate = sbUpdate.toString();

		try {
			for (int i = 0; i < tb.getCount(); i++) {
				DTRow r = tb.getRow(i);
				String exp = this.createKeyExp(keys, r);
				if (mapExists.containsKey(exp)) {
					if (param.isSkipExists()) {
						continue;
					}
					if (this.checkRowEquals(r, mapExists.get(exp))) {
						continue;
					}
					rv1.addValues(r);
					cnn.executeUpdate(sqlUpdate);

				} else {
					rv1.addValues(r);
					cnn.executeUpdate(sqlInsert);
				}
				if (cnn.getErrorMsg() != null) {
					LOGGER.error("{}", r.toJson().toString(4));
					throw new Exception(cnn.getErrorMsg());
				}
			}
		} catch (Exception err) {
			if (!param.isSkipError()) {
				cnn.clearErrorMsg();
				LOGGER.error(err.getMessage());
				throw err;
			} else {
				LOGGER.warn(err.getMessage());
			}
		} finally {
			cnn.close();
		}
	}

	/**
	 * 将返回数据保存到本地表中
	 * 
	 * @param tb
	 * @param saveJSON
	 * @throws Exception
	 */
	public void saveData(DTTable tb, JSONObject saveJSON) throws Exception {
		if (tb == null || tb.getCount() == 0) {
			return;
		}
		/*
		 * "save": { "key": "HOTEL_ID", "table": "V_CACHED_HOTEL_MAIN" }
		 */
		String key = saveJSON.optString("key");
		String table = saveJSON.optString("table");

		String[] keys = key.split(",");
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		for (int i = 0; i < keys.length; i++) {
			String k = keys[i].trim();
			map.put(k.toUpperCase(), true);
			keys[i] = k;
		}

		StringBuilder sb = new StringBuilder(); // 字段列表
		StringBuilder sb1 = new StringBuilder(); // 字段值列表
		StringBuilder sb2 = new StringBuilder(); // 更新 set 列表
		StringBuilder sb3 = new StringBuilder(); // 更新 where 列表

		for (int i = 0; i < tb.getColumns().getCount(); i++) {
			String field = tb.getColumns().getColumn(i).getName().trim();
			if (field.toUpperCase().equals("EWA_KEY")) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append("	\n, ");
				sb1.append("	\n, ");
			}

			sb.append(field);
			sb1.append("@" + field);

			// update
			if (map.containsKey(field.toUpperCase())) {
				if (sb3.length() > 0) {
					sb3.append(" AND ");
				}
				sb3.append(field + " = @" + field);
			} else {
				if (sb2.length() > 0) {
					sb2.append("	\n,");
				}
				sb2.append(field + " = @" + field);
			}
		}

		// 加载已经存在数据 SQL
		StringBuilder exists = new StringBuilder("SELECT ");
		exists.append(sb);
		exists.append(" FROM " + table + " WHERE ");
		for (int i = 0; i < tb.getCount(); i++) {
			StringBuilder w1 = new StringBuilder(); // where条件
			for (String key_field : map.keySet()) {
				if (w1.length() > 0) {
					w1.append(" AND ");
				}
				w1.append(key_field);

				String v = tb.getCell(i, key_field).toString();
				if (v != null) {
					w1.append("='");
					w1.append(v.replace("'", "''"));
					w1.append("'");
				} else {
					w1.append(" is null");
				}
			}

			if (i > 0) {
				exists.append("\n or ");
			}
			exists.append("(");
			exists.append(w1);
			exists.append(")");
		}

		// 已经存在数据
		DTTable tbExists = DTTable.getJdbcTable(exists.toString());
		HashMap<String, DTRow> mapExists = new HashMap<String, DTRow>();

		for (int i = 0; i < tbExists.getCount(); i++) {
			DTRow r = tbExists.getRow(i);
			String exp = this.createKeyExp(keys, r);
			mapExists.put(exp, r);
		}

		StringBuilder sbUpdate = new StringBuilder();
		sbUpdate.append("UPDATE ");
		sbUpdate.append(table);
		sbUpdate.append(" SET ");
		sbUpdate.append(sb2);
		sbUpdate.append(" WHERE ");
		sbUpdate.append(sb3);

		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("INSERT INTO ");
		sbInsert.append(table);
		sbInsert.append(" (");
		sbInsert.append(sb);
		sbInsert.append(") VALUES(");
		sbInsert.append(sb1);
		sbInsert.append(")");

		String sqlInsert = sbInsert.toString();
		String sqlUpdate = sbUpdate.toString();
		RequestValue rv1 = new RequestValue();
		DataConnection cnn = new DataConnection("", rv1);
		try {
			for (int i = 0; i < tb.getCount(); i++) {
				DTRow r = tb.getRow(i);
				String exp = this.createKeyExp(keys, r);
				if (mapExists.containsKey(exp)) {
					if (this.checkRowEquals(r, mapExists.get(exp))) {
						continue;
					}
					rv1.addValues(r);
					cnn.executeUpdate(sqlUpdate);
				} else {
					rv1.addValues(r);
					cnn.executeUpdate(sqlInsert);
				}
			}
		} catch (Exception err) {
			throw err;
		} finally {
			cnn.close();
		}
	}

	/**
	 * 比较两个数据行数据是否一致
	 * 
	 * @param r
	 * @param r1
	 * @return
	 */
	private boolean checkRowEquals(DTRow r, DTRow r1) {
		try {
			for (int i = 0; i < r.getCount(); i++) {
				Object o1 = r.getCell(i).getValue();
				String field_name = r.getTable().getColumns().getColumn(i).getName();
				if (field_name.equalsIgnoreCase("EWA_KEY")) {
					continue;
				}
				if(!r1.getTable().getColumns().testName(field_name)) {
					continue; // 试题表中字段不存在
				}
				/*
				 * if (field_name.equalsIgnoreCase("DATECREATED")) { int m = 1; m++; }
				 */
				Object o2 = r1.getCell(field_name).getValue();
				 
				System.out.println(o1.getClass());
				System.out.println(o2.getClass());
				
				if(!checkEquals(o1,o2)) {
					return false;
				}
			}
			return true;
		} catch (Exception err) {
			System.out.print(this);
			System.out.println(".checkRowEquals");
			System.out.println(err.getMessage());
			return false;
		}
	}

	private boolean checkEquals(Object o1, Object o2) {
		if (o1 == null && o2 != null) {
			return false;
		}
		if (o1 != null && o2 == null) {
			return false;
		}
		
		if(o1 instanceof java.sql.Timestamp || o2 instanceof java.sql.Timestamp) {
			Date t1;
			Date t2;
			if(o1 instanceof java.sql.Timestamp) {
				t1 = new Date( ((java.sql.Timestamp)o1).getTime());
			} else {
				t1 = Utils.getDate(o1.toString());
			}
			if(o2 instanceof java.sql.Timestamp) {
				t2 = new Date( ((java.sql.Timestamp)o2).getTime());
			} else {
				t2 = Utils.getDate(o1.toString());
			}
			return t1.getTime() == t2.getTime();
		}
		
		if (o1 instanceof org.json.JSONObject || o2 instanceof org.json.JSONObject) {
			JSONObject jo1 = new JSONObject(o1.toString());
			JSONObject jo2 = new JSONObject(o2.toString());
			Iterator<String> it = jo1.keys();
			while (it.hasNext()) {
				String key = it.next();
				Object jv1 = jo1.get(key);
				Object jv2 = jo2.get(key);
				if(!checkEquals(jv1,jv2)) {
					return false;
				}
			}
			
			return true;
		}
		if (o1 instanceof java.math.BigDecimal || o2 instanceof java.math.BigDecimal) {
			BigDecimal bd1 = new BigDecimal(o1.toString());
			BigDecimal bd2 = new BigDecimal(o2.toString());

			return bd1.compareTo(bd2) == 0;
		}
		return o1.toString().equals(o2.toString());
	}

	private String createKeyExp(String[] keys, DTRow r) throws Exception {
		StringBuilder exp = new StringBuilder();
		for (int m = 0; m < keys.length; m++) {
			if (m > 0) {
				exp.append("\1$$$$gdx$$$\2");
			}
			String kn = keys[m].toString();
			String kv = r.getCell(kn).toString();
			exp.append(kn);
			exp.append("=");
			exp.append(kv);
		}

		return exp.toString();
	}

	/**
	 * 获取最后执行的结果
	 * 
	 * @return
	 */
	public String getLastResult() {
		return this.lastResult_;
	}

	public UNet getNet() {
		if (this.net_ == null) {
			this.net_ = new UNet();
		}
		net_.setIsShowLog(this.isDebug_);
		if (this.userAgent_ != null && this.userAgent_.trim().length() > 0) {
			net_.setUserAgent(this.userAgent_);
		}
		return this.net_;
	}

	/**
	 * 获取 UserAgent
	 * 
	 * @return
	 */
	public String getUserAgent() {
		return userAgent_;
	}

	/**
	 * 设置 UserAgent
	 * 
	 * @param userAgent
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent_ = userAgent;
	}

	/**
	 * 设置是否debug输出(system.out)
	 * 
	 * @return the isDebug_
	 */
	public boolean isDebug() {
		return isDebug_;
	}

	/**
	 * 设置是否debug输出(system.out)
	 * 
	 * @param isDebug_ the isDebug_ to set
	 */
	public void setDebug(boolean isDebug_) {
		this.isDebug_ = isDebug_;
	}

}
