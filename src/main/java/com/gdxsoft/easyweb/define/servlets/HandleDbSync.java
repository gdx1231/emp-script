package com.gdxsoft.easyweb.define.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.define.SyncDatabase;
import com.gdxsoft.easyweb.define.database.DbObjectExtractor;
import com.gdxsoft.easyweb.define.database.DbSyncDiff;
import com.gdxsoft.easyweb.define.database.DbSyncObject;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.servlets.FileOut;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UJSon;

/**
 * 数据库结构同步请求处理器，参考 HandleRemoteSync
 */
public class HandleDbSync {
	private static Logger LOGGER = LoggerFactory.getLogger(HandleDbSync.class);

	private RequestValue rv;
	private HttpServletRequest _Request;
	private HttpServletResponse _Response;

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this._Request = request;
		this._Response = response;

		request.setCharacterEncoding("utf-8");
		HttpSession session = null;
		try {
			session = _Request.getSession();
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
		}

		RequestValue rv = new RequestValue(_Request, session);
		this.rv = rv;
		String method = rv.getString("method");
		if (method == null) {
			method = "";
		}

		// 登录检查
		PageValue pv = rv.getPageValues().getPageValue("EWA_ADMIN_ID");
		boolean isLoggedIn = (pv != null && pv.getPVTag() == PageValueTag.SESSION);

		LOGGER.info("HandleDbSync: method={}", method);

		JSONObject rstJson = new JSONObject();
		try {
			switch (method) {
				case "listConnections":
					// 列出可用数据库连接（需登录）
					if (!isLoggedIn) {
						out(UJSon.rstFalse("login required").toString());
						return;
					}
					handleListConnections();
					break;

				case "extract":
					// 提取指定连接的 schema（远程侧调用，需登录）
					if (!isLoggedIn) {
						out(UJSon.rstFalse("login required").toString());
						return;
					}
					handleExtract();
					break;

				case "compare":
					// 远程对比：接收源库 schema，提取目标库，对比返回差异
					handleCompare();
					break;

				case "execute":
					// 在目标库执行同步 SQL
					handleExecute();
					break;

				case "compareLocal":
					// 本地对比（需登录）
					if (!isLoggedIn) {
						out(UJSon.rstFalse("login required").toString());
						return;
					}
					handleCompareLocal();
					break;

				default:
					rstJson.put("RST", false);
					rstJson.put("ERR", "method not defined: " + method);
					out(rstJson.toString());
			}
		} catch (Exception err) {
			LOGGER.error("HandleDbSync error: {}", err.getMessage());
			rstJson.put("RST", false);
			rstJson.put("ERR", err.getMessage());
			out(rstJson.toString());
		}
	}

	/**
	 * 列出可用数据库连接
	 */
	private void handleListConnections() throws Exception {
		JSONObject conns = SyncDatabase.listConnections();
		out(conns.toString());
	}

	/**
	 * 提取指定连接的 schema 对象
	 */
	private void handleExtract() throws Exception {
		String connName = rv.getString("connName");
		if (connName == null || connName.trim().isEmpty()) {
			out(UJSon.rstFalse("connName required").toString());
			return;
		}
		DbObjectExtractor extractor = new DbObjectExtractor(connName);
		JSONObject schema = extractor.extractAllToJson();
		out(schema.toString());
	}

	/**
	 * 远程对比：接收加密的源库 schema，提取目标库，对比返回差异
	 */
	private void handleCompare() throws Exception {
		String gdx = rv.getString("GDX");
		if (gdx == null || gdx.trim().isEmpty()) {
			out(UJSon.rstFalse("GDX required").toString());
			return;
		}

		// 解密
		String code = rv.getString("code");
		String content = decrypt(gdx, code);
		JSONObject paras = new JSONObject(content);

		String targetConn = paras.getString("targetConn");
		JSONObject sourceSchema = paras.getJSONObject("sourceSchema");

		// 提取目标库 schema
		DbObjectExtractor extractor = new DbObjectExtractor(targetConn);
		JSONObject targetSchema = extractor.extractAllToJson();

		// 对比
		List<DbSyncDiff> diffs = SyncDatabase.doCompare(sourceSchema, targetSchema);

		// 构建结果 JSON
		JSONArray arr = new JSONArray();
		for (DbSyncDiff diff : diffs) {
			arr.put(diff.toJson());
		}

		JSONObject result = new JSONObject();
		result.put("RST", true);
		result.put("diffs", arr);
		result.put("diffCount", diffs.size());

		// 加密返回
		String encrypted = encrypt(result.toString(), code);
		out(encrypted);
	}

	/**
	 * 本地对比
	 */
	private void handleCompareLocal() throws Exception {
		String sourceConn = rv.getString("sourceConn");
		String targetConn = rv.getString("targetConn");
		if (sourceConn == null || targetConn == null) {
			out(UJSon.rstFalse("sourceConn and targetConn required").toString());
			return;
		}

		SyncDatabase sync = new SyncDatabase(sourceConn, targetConn);
		List<DbSyncDiff> diffs = sync.compareLocal();

		JSONArray arr = new JSONArray();
		for (DbSyncDiff diff : diffs) {
			arr.put(diff.toJson());
		}

		JSONObject result = new JSONObject();
		result.put("RST", true);
		result.put("diffs", arr);
		result.put("diffCount", diffs.size());
		out(result.toString());
	}

	/**
	 * 在目标库执行同步 SQL
	 */
	private void handleExecute() throws Exception {
		String gdx = rv.getString("GDX");
		if (gdx == null || gdx.trim().isEmpty()) {
			out(UJSon.rstFalse("GDX required").toString());
			return;
		}

		String code = rv.getString("code");
		String content = decrypt(gdx, code);
		JSONObject paras = new JSONObject(content);

		String targetConn = paras.getString("targetConn");
		String syncSql = paras.getString("sql");

		SyncDatabase sync = new SyncDatabase();
		sync.setTargetConnName(targetConn);
		String rst = sync.executeLocal(syncSql);
		out(rst);
	}

	// ==================== 加密/解密 ====================

	private String encrypt(String text, String code) throws Exception {
		UAes aes = createAes(code);
		return aes.encrypt(text);
	}

	private String decrypt(String text, String code) throws Exception {
		UAes aes = createAes(code);
		return aes.decrypt(text);
	}

	private UAes createAes(String code) throws Exception {
		UAes aes = new UAes();
		aes.setCipherName(UAes.AES_128_CBC);
		aes.setPaddingMethod(UAes.NoPadding);
		aes.setUsingBc(false);
		aes.createKey(code.getBytes("utf-8"));
		return aes;
	}

	// ==================== 输出 ====================

	private void out(String cnt) throws ServletException, IOException {
		_Response.setCharacterEncoding("utf-8");
		_Response.setContentType(FileOut.MAP.get("json"));
		PrintWriter out = _Response.getWriter();
		try {
			out.println(cnt);
			out.flush();
		} catch (Exception err) {
			LOGGER.error(err.getMessage());
		} finally {
			out.close();
		}
	}
}
