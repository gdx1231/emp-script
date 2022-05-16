package com.gdxsoft.easyweb.script.display.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UDataUtils;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.USnowflake;
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

	private ActionBase actionBase;

	private DataConnection conn;
	private RequestValue rv;

	private long reqId;
	private boolean fromCache;
	private String reqOptMd5;

	public ActionJSON() {

	}

	public ActionJSON(String userAgent) {
		this.userAgent_ = userAgent;
	}

	public ArrayList<DTTable> action(ActionJSONParameter param) throws Exception {
		rv = new RequestValue();
		conn = new DataConnection(param.getConnConfigName(), rv);

		String opt = param.getJsonObject().toString(2);
		String optMd5 = Utils.md5(opt);
		this.reqOptMd5 = optMd5;

		// 后面要用到
		this.rv.addOrUpdateValue("REQ_OPTS", opt);
		this.rv.addOrUpdateValue("REQ_OPTS_MD5", optMd5);

		long reqId = this.checkExistsRequest(param);
		if (reqId > 0) { // from cached
			String sqlExists = "SELECT * FROM _EWA_API_REQ_LOG WHERE REQ_ID=" + reqId;
			DTTable tbExists = DTTable.getJdbcTable(sqlExists, conn);
			conn.close();
			lastResult_ = tbExists.getCell(0, "REQ_RST").toString();

			// 保存请求编号
			this.reqId = reqId;
			this.fromCache = true; // 来自缓存

		} else { // new request
			lastResult_ = this.httpRequest(param);
			if (!(this.net_.getLastStatusCode() >= 200 && this.net_.getLastStatusCode() < 400)) {
				// 错误的结果
				throw new Exception(this.net_.getLastStatusCode() + ":" + this.net_.getLastResult());
			}
		}
		ArrayList<DTTable> al = new ArrayList<DTTable>();
		if (isJSONObject(lastResult_)) {// JSONObject
			JSONObject resultObj = new JSONObject(this.lastResult_);
			if (param.getListTags().size() == 0) {
				if (!this.isFromCache() || param.isAsLfData()) {
					DTTable tb = this.fromJSONObject(resultObj);
					al.add(tb);
					// 作为列表数据，默认不用
					tb.getAttsTable().add("AsLfData", param.isAsLfData());
				}
			} else {
				param.getListTags().forEach(tag -> {
					if (!this.isFromCache() || tag.isAsLfData()) {
						this.getTable(tag, resultObj, al, null);
					}
				});
			}
		} else { // JSONArray
			if (!this.isFromCache() || param.isAsLfData()) {
				JSONArray arr = new JSONArray(this.lastResult_);
				DTTable tb = this.fromJSONArray(arr);
				al.add(tb);
				// 作为列表数据，默认不用
				tb.getAttsTable().add("AsLfData", param.isAsLfData());
			}
		}

		return al;
	}

	private boolean isJSONObject(String jsonStr) {
		int loc = jsonStr.indexOf("{");
		int loc1 = jsonStr.indexOf("[");

		if (loc == 0 || (loc > 0 && loc < loc1)) {// JSONObject
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
		String tbName = pTag == null ? tagName : pTag + "." + tagName;
		try {
			if (o instanceof org.json.JSONObject) {// JSONObject
				tb = this.fromJSONObject((JSONObject) o);
			} else { // JSONArray
				tb = this.fromJSONArray((JSONArray) o);
			}
			tb.getAttsTable().add("tag", tag);
			tb.setName(tbName);

			al.add(tb);
			// 作为列表数据，默认不用
			tb.getAttsTable().add("AsLfData", tag.isAsLfData());

		} catch (Exception e) {
			LOGGER.error("JSON 2 TABLE, error={}, json={}", e.getMessage(), o.toString());
		}

		for (int i = 0; i < tag.getSubListTags().size(); i++) {
			ActionJSONParameterListTag subTag = tag.getSubListTags().get(i);
			if (!tb.getColumns().testName(subTag.getTag())) {
				LOGGER.warn("The field {} not in the table. {}", subTag.getTag(), subTag.getJsonObject().toString(2));
				continue;
			}
			JSONArray arr = new JSONArray();
			String[] keys = tag.getKey().split(",");

			// 将表指定字段拼接成JSONArray
			for (int m = 0; m < tb.getCount(); m++) {
				DTRow row = tb.getRow(m);
				String json = null;
				try {
					json = row.getCell(subTag.getTag()).toString();
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
					continue;
				}
				if (json == null || json.trim().length() == 0) {
					continue;
				}
				try {
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
				} catch (Exception e) {
					LOGGER.error("{},{}", json, e.getMessage());
					continue;
				}
			}
			JSONObject subData = new JSONObject();
			subData.put(subTag.getTag(), arr);
			// 递归调用
			this.getTable(subTag, subData, al, tbName);

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

	public long checkExistsRequest(ActionJSONParameter param) {
		if (StringUtils.isBlank(param.getExpire())) {
			return -1;
		}
		String expire = param.getExpire();
		long expireLong = 0;
		try {
			expireLong = this.getExpireLong(expire);
		} catch (Exception err) {
			LOGGER.warn("The api request expire time {}, {}", expire, err.getMessage());
		}
		if (expireLong <= 0) {
			return -1;
		}

		Date expireDate = new Date(System.currentTimeMillis() + expireLong);
		this.rv.addOrUpdateValue("REQ_EXPIRE", expireDate, "DATE", 100);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT REQ_ID FROM _EWA_API_REQ_LOG \n");
		sb.append(" WHERE REQ_OPTS_MD5 = @REQ_OPTS_MD5 \n");
		sb.append("   AND REQ_EXPIRE < @REQ_EXPIRE \n");
		sb.append("   AND REQ_RST_HTTP_CODE BETWEEN 200 AND 300 \n");
		sb.append(" ORDER BY REQ_ID DESC");
		String sqlchk = sb.toString();

		this.getActionBase().getDebugFrames().addDebug(this, "Check exists",
				"Start check optmd5=" + rv.s("REQ_OPTS_MD5"));

		DTTable tbChk = DTTable.getJdbcTable(sqlchk, "REQ_ID", 1, 1, conn);
		if (conn.getErrorMsg() != null) {
			LOGGER.warn("{}", conn.getErrorMsg());
			this.getActionBase().getDebugFrames().addDebug(this, "error", conn.getErrorMsg());
			// 创建表 _EWA_API_REQ_LOG 的ddl
			LOGGER.info("如果表不存在，请用下面DDL:\n{}", ActionJsonDdl.getLogDdl());
			this.getActionBase().getDebugFrames().addDebug(this, "error",
					"如果表不存在，请用下面DDL:\n" + ActionJsonDdl.getLogDdl());
			conn.clearErrorMsg();
			conn.close();
			return -1;
		}
		conn.close();
		if (tbChk.getCount() == 0) {
			this.getActionBase().getDebugFrames().addDebug(this, "Check exists", "no data");
			return -1;
		}
		this.getActionBase().getDebugFrames().addDebug(this, "Check exists",
				"Found. REQ_ID=" + tbChk.getCell(0, 0).toLong());
		return tbChk.getCell(0, 0).toLong();
	}

	private long getExpireLong(String expire) {
		expire = expire.trim().toLowerCase();
		long scale = 1000; // second
		boolean haveUnit = false;
		if (expire.endsWith("h")) { // hour
			scale = 60 * 60 * 1000;
			haveUnit = true;
		} else if (expire.endsWith("m")) { // minute
			scale = 60 * 1000;
			haveUnit = true;
		} else if (expire.endsWith("d")) { // day
			scale = 24 * 60 * 60 * 1000;
			haveUnit = true;
		} else if (expire.endsWith("s")) { // second
			scale = 1000;
			haveUnit = true;
		}
		if (haveUnit) {
			expire = expire.substring(0, expire.length() - 1);
		}
		long expireLong = 0;
		expireLong = Long.parseLong(expire);
		if (expireLong <= 0) {
			return expireLong;
		}
		expireLong = expireLong * scale;
		return expireLong;
	}

	/**
	 * 发送RSETful请求
	 * 
	 * @param param 请求参数
	 * @return 结果
	 * @throws Exception
	 */
	private String httpRequest(ActionJSONParameter param) throws Exception {

		UNet net = this.getNet();
		if (StringUtils.isNotBlank(param.getUserAgent())) {
			net.setUserAgent(param.getUserAgent());
		}

		// 头部是否有accept参数设定
		Map<String, Boolean> haveAccept = new HashMap<>();

		// 添加请求头部信息，一般会有apikey等信息
		param.getHeaders().forEach((k, v) -> {
			net.addHeader(k, v);
			if (k.equalsIgnoreCase("accept")) {
				haveAccept.put("accept", true);
			}
		});

		// 添加默认请求方式
		if (!haveAccept.containsKey("accept")) {
			net.addHeader("accept", "application/json");
		}
		if (param.isDebug()) {
			net.setIsShowLog(true);
		}

		UUrl u = new UUrl(param.getUrl());
		param.getQueries().forEach((k, v) -> {
			u.add(k, v);
		});
		String url = u.getUrlWithDomain();
		String result;

		Date reqStart = new Date();
		this.getActionBase().getDebugFrames().addDebug(this, "http start", param.getMethod() + ": " + url);

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
		Date reqEnd = new Date();
		this.getActionBase().getDebugFrames().addDebug(this, "http end", "statusCode=" + net.getLastStatusCode());

		String expire = param.getExpire();
		long expireLong = 0;
		try {
			expireLong = this.getExpireLong(expire);
		} catch (Exception err) {
			LOGGER.warn("The api request expire time {}, {}", expire, err.getMessage());
		}
		Date expireDate = new Date(System.currentTimeMillis() + expireLong);
		this.rv.addOrUpdateValue("REQ_EXPIRE", expireDate, "DATE", 100);

		long reqId = USnowflake.nextId();
		this.rv.addOrUpdateValue("REQ_ID", reqId);
		this.rv.addOrUpdateValue("REQ_URL", param.getUrl());

		this.rv.addOrUpdateValue("REQ_RST", result);
		this.rv.addOrUpdateValue("REQ_RST_MD5", Utils.md5(result));
		this.rv.addOrUpdateValue("REQ_RST_HTTP_CODE", this.net_.getLastStatusCode());

		this.rv.addOrUpdateValue("REQ_START", reqStart, "DATE", 100);
		this.rv.addOrUpdateValue("REQ_START", reqEnd, "DATE", 100);

		this.rv.addOrUpdateValue("REQ_UA", this.actionBase.getRequestValue().s("SYS_USER_AGENT"));
		this.rv.addOrUpdateValue("REQ_IP", this.actionBase.getRequestValue().s("SYS_REMOTEIP"));
		this.rv.addOrUpdateValue("REQ_JSP", this.actionBase.getRequestValue().s("SYS_REMOTE_URL_ALL"));

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO _EWA_API_REQ_LOG(\n");
		sb.append("  REQ_ID, REQ_AGENT, REQ_URL, REQ_OPTS, REQ_OPTS_MD5\n");
		sb.append(", REQ_CDATE, REQ_MDATE, REQ_EXPIRE, REQ_RST, REQ_RST_MD5\n");
		sb.append(", REQ_RST_HTTP_CODE, REQ_START, REQ_END, REQ_UA, REQ_IP, REQ_JSP\n");
		sb.append(")VALUES(\n");
		// @REQ_OPTS, @REQ_OPTS_MD5 在action已经赋值
		sb.append(
				"  @REQ_ID, CASE WHEN @REQ_AGENT IS NULL THEN '' ELSE @REQ_AGENT END, @REQ_URL, @REQ_OPTS, @REQ_OPTS_MD5\n");
		sb.append(", @sys_DATE, @sys_DATE, @REQ_EXPIRE, @REQ_RST, @REQ_RST_MD5\n");
		sb.append(", @REQ_RST_HTTP_CODE, @REQ_START, @REQ_END, @REQ_UA, @REQ_IP, @REQ_JSP\n");
		sb.append(")");

		// 记录日志
		conn.executeUpdate(sb.toString());
		if (conn.getErrorMsg() != null) {
			LOGGER.warn("{}", conn.getErrorMsg());
			conn.clearErrorMsg();
		}
		conn.close();
		this.getActionBase().getDebugFrames().addDebug(this, "http",
				"Save the result to _EWA_API_REQ_LOG, REQ_ID = " + reqId);
		// 保存请求编号
		this.reqId = reqId;
		this.fromCache = false;
		return result;
	}

	/**
	 * 将返回数据保存到本地表中
	 * 
	 * @param jsonTb
	 * @param saveJSON
	 * @throws Exception
	 */
	public void saveData(DTTable jsonTb, ActionJSONParameterListTag param) throws Exception {
		if (jsonTb == null || jsonTb.getCount() == 0) {
			return;
		}
		// "listTags": [{
		// ...."tag": "products",
		// ...."key": "PRODUCTCODE",
		// ...."table": "PRODUCT"
		// ...."listTags": [{
		// ........"tag": "images",
		// ........"key": "id",
		// ........"table": "image"
		// ....},{
		// ........"tag": "extras",
		// ........"key": "name",
		// ........"table": "extra"
		// ....}]
		// }]

		if (param.getFieldsMap().size() > 0) {// 段对应关系，jsonKey:fieldName
			// 更改 jsonTb 的字段名称为物理表的字段名称
			Iterator<String> it = param.getFieldsMap().keySet().iterator();
			while (it.hasNext()) {
				String jsonKey = it.next();
				String field = param.getFieldsMap().get(jsonKey);
				if (jsonTb.getColumns().testName(jsonKey)) {
					jsonTb.getColumns().getColumn(jsonKey).setName(field);
					LOGGER.info("Change jsonTb.{} to {}", jsonKey, field);
				} else {
					LOGGER.error("jsonTb.{} not exists", jsonKey);
					throw new Exception("jsonTb." + jsonKey + " not exists");
				}
			}
			// 重建表字段名称索引
			jsonTb.getColumns().refreshNamesIndex();
		}

		String key = param.getKey();
		String table = param.getTable();

		this.getActionBase().getDebugFrames().addDebug(this, "save",
				"start save to the talbe " + table + ", records= " + jsonTb.getCount());

		// 从数据库获取空表，用于字段判断
		String sql0 = "SELECT * FROM " + table + " WHERE 1=2";
		DTTable tbTmp = DTTable.getJdbcTable(sql0, param.getConnConfigName());

		// 检查表是否有 _EWA_LOG_ID 字段，没有的化自动创建字段和创建检索
		this.checkAndCreateField_EWA_LOG_ID(tbTmp, table);

		// 物理主键表达式
		String[] keys = key.split(",");
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (int i = 0; i < keys.length; i++) {
			String k = keys[i].trim();
			map.put(k.toUpperCase(), true);
			keys[i] = k;
			if (!tbTmp.getColumns().testName(k)) {// 字段在数据库表中不存在
				LOGGER.error("{}.{} not exists", table, k);
				throw new Exception(table + "." + k + " not exists");
			}
		}

		StringBuilder insertFields = new StringBuilder(); // 插入字段列表
		StringBuilder insertValues = new StringBuilder(); // 插入值列表

		insertFields.append("_EWA_LOG_ID"); // 日志记录ID，和表_EWA_API_REQ_LOG_LST.REF_EWA_LOG_ID一致
		insertValues.append("@_EWA_LOG_ID");

		StringBuilder updateWhere = new StringBuilder(); // 更新 where 列表
		for (int i = 0; i < jsonTb.getColumns().getCount(); i++) {
			String field = jsonTb.getColumns().getColumn(i).getName().trim();

			if (field.toUpperCase().equals("EWA_KEY")) {
				continue;
			}
			if (!tbTmp.getColumns().testName(field)) {
				// 字段在数据库表中不存在
				LOGGER.warn("{}.{} not exists", table, field);
				continue;
			}
			insertFields.append("   \n, ").append(field);
			insertValues.append("	\n, @").append(field);

			// update where 表达式
			if (map.containsKey(field.toUpperCase())) {
				if (updateWhere.length() > 0) {
					updateWhere.append(" AND ");
				}
				updateWhere.append(field).append("=@").append(field);
			}
		}

		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("INSERT INTO ").append(table).append("(").append(insertFields).append(")VALUES(")
				.append(insertValues).append(")");
		String sqlInsert = sbInsert.toString();

		this.getActionBase().getDebugFrames().addDebug(this, "save", "start get exists data ");
		// 已经存在数据的map，便于快速查找
		Map<String, DTRow> mapExists = this.createMapExistsData(table, jsonTb, map, keys);
		this.getActionBase().getDebugFrames().addDebug(this, "save", "end get exists data (" + mapExists.size() + ")");

		this.getActionBase().getDebugFrames().addDebug(this, "save", "start new/update data and log detail");
		rv.addOrUpdateValue("_EWA_API_REQ_LOG_LST_REQ_ID", this.reqId);
		rv.addOrUpdateValue("_EWA_API_REQ_LOG_LST_REF_TABLE", table);

		String logExists = this.getSqlLogExists();
		String logInsert = this.getSqlLogListNew();
		List<String> addedField = new ArrayList<String>();
		try {
			// 循环导入/更高数据
			for (int i = 0; i < jsonTb.getCount(); i++) {
				long ewaLogId = -1;
				DTRow r = jsonTb.getRow(i);
				String exp = this.createKeyExp(keys, r);
				// 清除增加的字段
				addedField.forEach(name -> {
					rv.getPageValues().remove(name);
				});
				// 增加数据行到 rv
				addedField = rv.addValues(r);
				if (mapExists.containsKey(exp)) {
					DTRow exisRow = mapExists.get(exp);
					ewaLogId = exisRow.getCell("_EWA_LOG_ID").toLong();
					rv.addOrUpdateValue("_EWA_LOG_ID", ewaLogId);
					// 保存日志明细
					saveLogList(logExists, logInsert);

					if (param.isSkipExists()) {
						continue;
					}
					// 获取不一致的字段
					List<String> fields = UDataUtils.getNotEqualsFields(r, exisRow, true);
					if (fields.size() == 0) { // 数据一致
						continue;
					}

					String sqlUpdate = this.createUpdateSql(table, fields, updateWhere.toString());
					conn.executeUpdate(sqlUpdate);
				} else {
					ewaLogId = USnowflake.nextId(); // 获取雪花id，用于记录日志编号
					rv.addOrUpdateValue("_EWA_LOG_ID", ewaLogId);
					conn.executeUpdate(sqlInsert);
					// 保存日志明细
					saveLogList(logExists, logInsert);
					// 清除增加的字段
					addedField.forEach(name -> {
						rv.getPageValues().remove(name);
					});
				}
				if (conn.getErrorMsg() != null) {
					LOGGER.error("{}", r.toJson().toString(4));
					throw new Exception(conn.getErrorMsg());
				}
			}
			this.getActionBase().getDebugFrames().addDebug(this, "save", "end save log to _EWA_API_REQ_LOG_LST ");
			// 清除增加的字段
			addedField.forEach(name -> {
				rv.getPageValues().remove(name);
			});

		} catch (Exception err) {
			this.getActionBase().getDebugFrames().addDebug(this, "error", err.getMessage());
			if (!param.isSkipError()) {
				LOGGER.error(err.getMessage());
				conn.clearErrorMsg();
				throw err;
			} else {
				LOGGER.warn(err.getMessage());
			}
		} finally {
			conn.close();
		}
	}

	private Map<String, DTRow> createMapExistsData(String table, DTTable jsonTable, Map<String, Boolean> keysMap,
			String[] keys) throws Exception {
		// 加载已经存在数据 SQL
		String exists = this.getExistsDataSql(table, jsonTable, keysMap);
		LOGGER.debug(exists);
		// 已经存在数据
		DTTable tbExists = DTTable.getJdbcTable(exists, conn);
		if (conn.getErrorMsg() != null) {
			conn.close();
			throw new Exception(conn.getErrorMsg());
		}

		// 已经存在数据的map，便于快速查找
		Map<String, DTRow> mapExists = new HashMap<>();
		for (int i = 0; i < tbExists.getCount(); i++) {
			DTRow r = tbExists.getRow(i);
			String exp = this.createKeyExp(keys, r);
			mapExists.put(exp, r);
		}

		return mapExists;
	}

	private String getExistsDataSql(String table, DTTable jsonTable, Map<String, Boolean> keysMap) throws Exception {
		// 加载已经存在数据 SQL
		StringBuilder exists = new StringBuilder("SELECT * FROM " + table + " WHERE ");
		for (int i = 0; i < jsonTable.getCount(); i++) {
			StringBuilder w1 = new StringBuilder(); // where条件
			for (String key_field : keysMap.keySet()) {
				if (w1.length() > 0) {
					w1.append(" AND ");
				}
				w1.append(key_field);
				String v = jsonTable.getCell(i, key_field).toString();
				if (v != null) {
					w1.append("=").append(conn.sqlParameterStringExp(v));
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

		return exists.toString();
	}

	/**
	 * 检查表是否有 _EWA_LOG_ID 字段，没有的化自动创建字段和创建检索
	 * 
	 * @param tbTmp 模板表
	 * @param table 标名称
	 * @throws Exception
	 */
	private void checkAndCreateField_EWA_LOG_ID(DTTable tbTmp, String table) throws Exception {
		if (tbTmp.getColumns().testName("_EWA_LOG_ID")) {
			return;
		}
		String sql = "alter table " + table + " add _EWA_LOG_ID bigint ";
		String sql1 = "create index IDX_" + table + "__EWA_LOG_ID on " + table + "(_EWA_LOG_ID)";
		conn.executeUpdate(sql);
		if (conn.getErrorMsg() == null) {
			conn.executeUpdate(sql1);
		}
		if (conn.getErrorMsg() != null) {
			LOGGER.error(conn.getErrorMsg());
			conn.close();
			throw new Exception(conn.getErrorMsg());
		}
		conn.close();
	}

	/**
	 * 获取插入 _EWA_API_REQ_LOG_LST 表的sql
	 * 
	 * @return
	 */
	private String getSqlLogListNew() {
		StringBuilder logInsert = new StringBuilder();
		logInsert.append("INSERT INTO _EWA_API_REQ_LOG_LST(REQ_ID, REF_TABLE, REF_EWA_LOG_ID)\n");
		logInsert.append("VALUES(@_EWA_API_REQ_LOG_LST_REQ_ID, @_EWA_API_REQ_LOG_LST_REF_TABLE, @_EWA_LOG_ID)");
		return logInsert.toString();
	}

	/**
	 * 获取日志明细 _EWA_API_REQ_LOG_LST 查询sql
	 * 
	 * @return
	 */
	private String getSqlLogExists() {
		StringBuilder logExists = new StringBuilder(); // 更新 where 列表
		logExists.append("SELECT 1 FROM _EWA_API_REQ_LOG_LST WHERE \n");
		logExists.append("     REQ_ID    		= @_EWA_API_REQ_LOG_LST_REQ_ID\n");
		logExists.append(" AND REF_TABLE 		= @_EWA_API_REQ_LOG_LST_REF_TABLE\n");
		logExists.append(" AND REF_EWA_LOG_ID	= @_EWA_LOG_ID");
		return logExists.toString();
	}

	private void saveLogList(String sqlLogExists, String sqlLogInsert) {
		DTTable tb = DTTable.getJdbcTable(sqlLogExists, conn);
		if (tb.getCount() > 0) {
			return;
		}
		conn.executeUpdate(sqlLogInsert);

	}

	private String createUpdateSql(String table, List<String> fields, String where) {
		StringBuilder sbUpdate = new StringBuilder();
		sbUpdate.append("UPDATE ");
		sbUpdate.append(table);
		sbUpdate.append(" SET \n\t");
		for (int i = 0; i < fields.size(); i++) {
			String field = fields.get(i);
			if (i > 0) {
				sbUpdate.append("\n\t, ");
			}
			sbUpdate.append(field + " = @" + field);
		}
		sbUpdate.append("\nWHERE ");
		sbUpdate.append(where);

		return sbUpdate.toString();
	}

	/**
	 * 将返回数据保存到本地表中
	 * 
	 * @param tb
	 * @param saveJSON
	 * @throws Exception
	 */
	@Deprecated
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
					if (UDataUtils.checkRowEquals(r, mapExists.get(exp), true)) {
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

	private String createKeyExp(String[] keys, DTRow r) throws Exception {
		StringBuilder exp = new StringBuilder();
		for (int m = 0; m < keys.length; m++) {
			if (m > 0) {
				exp.append("\1$$$$gdx$$$\2");
			}
			String kn = keys[m].toString();
			Object v = r.getCell(kn).getValue();
			String kv = null;
			if (v instanceof java.math.BigDecimal) {
				kv = UFormat.formatDecimalClearZero(v);
			} else {
				kv = v.toString();
			}
			exp.append(m);
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

	/**
	 * @return the actionBase
	 */
	public ActionBase getActionBase() {
		return actionBase;
	}

	/**
	 * @param actionBase the actionBase to set
	 */
	public void setActionBase(ActionBase actionBase) {
		this.actionBase = actionBase;
	}

	/**
	 * 请求数据保存的日志id
	 * 
	 * @return the reqId
	 */
	public long getReqId() {
		return reqId;
	}

	/**
	 * 请求数据保存的日志id
	 * 
	 * @param reqId the reqId to set
	 */
	public void setReqId(long reqId) {
		this.reqId = reqId;
	}

	/**
	 * 请求数据来自缓存
	 * 
	 * @return the fromCache
	 */
	public boolean isFromCache() {
		return fromCache;
	}

	/**
	 * 请求数据来自缓存
	 * 
	 * @param fromCache the fromCache to set
	 */
	public void setFromCache(boolean fromCache) {
		this.fromCache = fromCache;
	}

	/**
	 * 请求数据参数的md5
	 * 
	 * @return the reqOptMd5
	 */
	public String getReqOptMd5() {
		return reqOptMd5;
	}

	/**
	 * 请求数据参数的md5
	 * 
	 * @param reqOptMd5 the reqOptMd5 to set
	 */
	public void setReqOptMd5(String reqOptMd5) {
		this.reqOptMd5 = reqOptMd5;
	}

}
