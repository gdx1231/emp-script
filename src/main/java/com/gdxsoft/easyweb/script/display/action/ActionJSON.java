package com.gdxsoft.easyweb.script.display.action;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UNet;

/**
 * JSON调用
 * 
 * @author admin
 *
 */
public class ActionJSON {

	private UNet net_;

	private String userAgent_;
	private String lastResult_;
	private boolean isDebug_ = false;

	public ActionJSON() {

	}

	public ActionJSON(String userAgent) {
		this.userAgent_ = userAgent;
	}

	/**
	 * 提交并返回结果
	 * 
	 * @param url
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public boolean update(String url, HashMap<String, String> data, String charset) throws Exception {
		UNet net = this.getNet();
		// default charset is utf-8
		if (charset != null && charset.trim().length() > 0) {
			net.setEncode(charset);
		}

		String rst;
		if (data == null || data.size() == 0) {
			rst = net.doGet(url);
		} else {
			rst = net.doPost(url, data);
		}
		lastResult_ = rst;
		JSONObject obj = new JSONObject(rst);

		if (obj.has("RST")) {
			boolean rst1 = obj.optBoolean("RST");
			return rst1;
		}

		return true;
	}

	/**
	 * 查询并返回结果
	 * 
	 * @param url
	 *            地址
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public ArrayList<DTTable> query(String url, HashMap<String, String> data, String charset) throws Exception {
		UNet net = this.getNet();

		// default charset is utf-8
		if (charset != null && charset.trim().length() > 0) {
			net.setEncode(charset);
		}
		String rst;
		if (data == null || data.size() == 0) {
			rst = net.doGet(url);
		} else {
			rst = net.doPost(url, data);
		}
		lastResult_ = rst;

		int loc = rst.indexOf("{");
		int loc1 = rst.indexOf("[");

		JSONArray obj = null;
		ArrayList<DTTable> al = new ArrayList<DTTable>();
		if (loc1 >= 0 && loc < loc1) {
			DTTable tb = new DTTable();
			JSONObject json = new JSONObject(rst);
			if (json.has("DATA")) {
				obj = json.getJSONArray("DATA");
			} else {
				throw new Exception("返回数据没有DATA字段");
			}
			if (json.has("RECORDS")) { // 总记录数
				tb.getAttsTable().add("RECORDS", json.optInt("RECORDS"));
			}
			tb.initData(obj);
			al.add(tb);

		} else {
			// 直接返回数据
			obj = new JSONArray(rst);
			String cname=obj.get(0).getClass().getName().toString();
			if (cname.indexOf("org.json.JSONArray")>=0) {
				// 返回多个表
				for (int i = 0; i < obj.length(); i++) {
					DTTable tb = new DTTable();
					JSONArray arr=obj.getJSONArray(i);
					tb.initData(arr);
					al.add(tb);
				}
			} else {
				DTTable tb = new DTTable();
				tb.initData(obj);
			}

		}

		return al;
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
				Object o2 = r1.getCell(field_name).getValue();
				if (o1 == null && o2 != null) {
					return false;
				}
				if (o1 != null && o2 == null) {
					return false;
				}
				if (!o1.toString().equals(o2.toString())) {
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
	 * @param isDebug_
	 *            the isDebug_ to set
	 */
	public void setDebug(boolean isDebug_) {
		this.isDebug_ = isDebug_;
	}

}
