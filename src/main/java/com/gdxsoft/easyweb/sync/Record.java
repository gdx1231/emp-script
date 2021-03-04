package com.gdxsoft.easyweb.sync;

import org.json.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.data.*;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;

public class Record {

	public static JSONArray getRecords(String dbName, String tableName, String wh) {
		String sql = "select * from " + dbName + ".." + tableName + " where "
				+ (wh == null || wh.trim() == "" ? "1=1" : wh);
		DTTable dt = DTTable.getJdbcTable(sql);
		return dt.toJSONArray();
	}

	public static String execute(RequestValue rv) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");
		JSONObject jsonRst = new JSONObject();
		try {

			String dbName = rv.s("dbname");
			String tableName = rv.s("tablename");
			String pkName = rv.s("pkname");
			String tp = rv.s("tp");
			int ix = 0;
			JSONObject json = new JSONObject(rv.s("json"));
			Iterator<?> it = json.keys();
			if (tp.equals("U")) {
				String keyVal = json.getString(pkName);
				rv.addValue("PARA_jzp_" + pkName, keyVal);
				StringBuilder sb = new StringBuilder();
				sb.append("update " + dbName + ".." + tableName + " set ");
				while (it.hasNext()) {
					String kn = it.next().toString();
					if (!kn.equalsIgnoreCase(pkName)) {
						if (ix++ > 0) {
							sb.append(",");
						}
						sb.append(kn + "=@PARA_jzp_" + kn);
						rv.addValue("PARA_jzp_" + kn, json.get(kn).toString());
					}
				}
				sb.append(" where " + pkName + "=@PARA_jzp_" + pkName);

				cnn.setRequestValue(rv);
				cnn.executeUpdate(sb.toString());
			} else if (tp.equals("N")) {
				StringBuilder sb1 = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				sb1.append("insert into " + dbName + ".." + tableName + "(");
				sb2.append(") values(");
				while (it.hasNext()) {
					String kn = it.next().toString();
					String val = json.get(kn).toString();
					rv.addValue("PARA_jzp_" + kn, val);
					if (ix++ > 0) {
						sb1.append(",");
						sb2.append(",");
					}
					sb1.append(kn);
					sb2.append("@PARA_jzp_" + kn);
				}
				sb1.append(sb2.toString() + ")");

				cnn.setRequestValue(rv);
				cnn.executeUpdate(sb1.toString());
			} else {
				jsonRst.put("RST", false);
				jsonRst.put("ERR", "type error");
			}
			if (jsonRst.isNull("RST")) {
				if (cnn.getErrorMsg() == null) {
					jsonRst.put("RST", true);
					jsonRst.put("MSG", "OK");
				} else {
					jsonRst.put("RST", false);
					jsonRst.put("ERR", cnn.getErrorMsg());
				}
			}
			return jsonRst.toString();
		} catch (JSONException e) {
			return "{\"RST\":false,ERR:\"" + Utils.textToJscript(e.getMessage()) + "\"}";
		} catch (Exception e) {
			return "{\"RST\":false,ERR:\"" + Utils.textToJscript(e.getMessage()) + "\"}";
		} finally {
			cnn.close();
		}
	}

	public static String send(RequestValue rv, JSONObject tbJson) {
		String dbName = rv.s("remotedb");
		String tableName = rv.s("tablename");
		String tp = rv.s("tp");
		String pkName = tbJson.getString(tableName).split(",")[0];
		String remote_url = getRemoteUrl() + "&method=execute&dbname=" + dbName + "&tablename=" + tableName + "&tp="
				+ tp + "&pkname=" + pkName;

		UNet net = new UNet();
		net.setIsShowLog(false);

		net.setEncode("utf-8");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("json", rv.s("json"));
		String rst = net.doPost(remote_url, map);
		System.out.println(rst);
		return rst;
	}

	public static void runSql(String sql, RequestValue rv) {
		DataConnection cnn = new DataConnection();
		cnn.setConfigName("");

		System.out.println(sql);
	}

	public static String getRemoteUrl() {
		String xml = UPath.getRealPath() + "/ewa_conf.xml";
		try {
			Document doc = UXml.retDocument(xml);
			NodeList nl = doc.getElementsByTagName("remote_record_syncs");

			if (nl.getLength() > 0) {
				Element ele = (Element) nl.item(0);
				String u = ele.getAttribute("url");
				String code = ele.getAttribute("code");
				return u + "?code=" + URLEncoder.encode(code, "utf-8");
			}
			return "remote_record_syncs not defined";
		} catch (ParserConfigurationException e) {
			return e.getMessage();
		} catch (SAXException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}