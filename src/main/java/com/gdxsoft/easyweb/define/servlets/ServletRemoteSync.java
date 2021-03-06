/**
 * 
 */
package com.gdxsoft.easyweb.define.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.define.SyncRemote;
import com.gdxsoft.easyweb.define.SyncRemotes;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfig;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation;
import com.gdxsoft.easyweb.script.userConfig.ScriptPath;
import com.gdxsoft.easyweb.script.userConfig.ScriptPaths;

/**
 * 同步本地和远程数据
 * 
 * @author Administrator
 * 
 */
public class ServletRemoteSync extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletRemoteSync.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 982L;

	/**
	 * 
	 */

	private RequestValue rv;
	private HttpServletRequest _Request;
	private HttpServletResponse _Response;
	//
	// private GZipOut _Out;

	public ServletRemoteSync() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request  the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException      if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	private void out(String cnt) throws ServletException, IOException {
		// if (this._Out == null) {
		// GZipOut o = new GZipOut(_Request, _Response);
		// this._Out = o;
		// o.outContent(cnt);
		// } else {
		// this._Out.outContent(cnt);
		// }
		_Response.setCharacterEncoding("utf-8");
		// _Response.setContentLength(cnt.length());
		_Response.setContentType("text/json");

		PrintWriter out = _Response.getWriter();
		try {
			out.println(cnt);
			out.flush();
		} catch (Exception err) {
			System.err.println(err.getMessage());
			err.printStackTrace();
		} finally {
			out.close();
		}

	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

		PageValue pv = rv.getPageValues().getPageValue("EWA_ADMIN_ID");

		if (pv == null || (pv.getPVTag() != PageValueTag.SESSION)) {
			// not login
			if (method.equals("getCfgs") || method.equals("start") || method.equals("start_send")
					|| method.equals("local_recv_remote_file")) {
				// 本地操作必须登录

				JSONObject rst = new JSONObject();
				rst.put("RST", false);
				rst.put("ERR", "deny! request login");
				out(rst.toString());
				return;
			}
		}

		LOGGER.info("SyncRemote: " + method);

		JSONObject rstJson = new JSONObject();
		if (method.equals("listCfgs")) {// 获取所有配置项目
			JSONArray arr = new JSONArray();
			SyncRemotes syncRemotes = new SyncRemotes();
			for (String key : SyncRemotes.MAP_REMOTE.keySet()) {
				SyncRemote s = syncRemotes.getRemoteInstance(key);
				String des = s.getCfgs().optString("REMOTE_DES");
				if (des == null || des.trim().length() == 0) {
					des = "这个家伙很懒，没有定义说明(des)";
				}
				String url = s.getCfgs().optString("REMOTE_URL");
				JSONObject obj = new JSONObject();
				obj.put("cfg_key", key);
				obj.put("des", des);
				obj.put("url", url);
				arr.put(obj);
			}
			out(arr.toString());
			return;
		}
		try {
			String cfgkey = rv.s("CFG_KEY");
			
			if (method.equals("getCfgs")) {// 获取本地配置
				SyncRemote s = this.getRemote(cfgkey);
				JSONObject cfg = this.handleGetCfgs(s);
				out(cfg.toString());
			} else if (method.equals("start")) {// local
				SyncRemote s = this.getRemote(cfgkey);
				String id = rv.getString("id");
				s.initById(id);
				s.getDir(null);
				s.saveJson();
				String rst = s.postToRemote();
				out(rst);
			} else if (method.equals("recv_json")) { // remote 加载本地文件，比较区别
				SyncRemote s = this.getRemote(cfgkey);
				String contentEncode = rv.getString("GDX");
				JSONObject contentJson;

				try {
					String contentDecode = s.decode(contentEncode);
					contentJson = new JSONObject(contentDecode);
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage());
					out("{RST:false,ERR:'decode error'}");
					return;
				}

				String id = contentJson.optString("remote_id");
				String fjosnDecode = contentJson.optJSONObject("fjson").toString();

				// System.out.println(fjosnDecode);

				JSONObject cfgs = s.getCfgs();
				JSONObject cfg = cfgs.getJSONObject(id);
				String root = cfg.getString("target");
				String filter = cfg.getString("filter");

				s.init(filter, root);

				s.getDir(root);
				s.saveJson();

				JSONObject diffs = s.compareFiles(fjosnDecode);

				// 不进行gzip
				String encodeDiffs = s.encode(diffs.toString());
				out(encodeDiffs);
				return;

			} else if (method.equals("send_file")) {// remote 接受文件并保存
				SyncRemote s = this.getRemote(cfgkey);
				String contentEncode = rv.getString("GDX");
				JSONObject contentJson;
				try {
					String contentDecode = s.decode(contentEncode);
					contentJson = new JSONObject(contentDecode);
				} catch (Exception err) {
					LOGGER.error(err.getLocalizedMessage());

					out("{RST:false,ERR:'decode error'}");
					return;
				}
				String id = contentJson.optString("remote_id");
				String name = contentJson.getString("name");
				String file = contentJson.getString("file");

				JSONObject cfgs = s.getCfgs();
				JSONObject cfg = cfgs.getJSONObject(id);
				String root = cfg.getString("target");
				String filter = cfg.getString("filter");

				s.init(filter, root);

				String rst = s.recvFile(root, name, file);
				String encoderst = s.encode(rst);
				out(encoderst);

			} else if (method.equals("start_send")) {// local
				SyncRemote s = this.getRemote(cfgkey);
				String rst = this.handleStartSend(s);
				out(rst);
			} else if (method.equals("local_recv_remote_file")) {// 本地请求远程文件
				SyncRemote s = this.getRemote(cfgkey);
				String rst = this.handleLocalReviceRemoteFile(s);
				out(rst);
			} else if (method.equals("remote_send_file")) {// 远程发送文件到本地
				SyncRemote s = this.getRemote(cfgkey);
				String encoderst = this.handleRemoteSendFile(s);
				out(encoderst);
			} else {
				LOGGER.error("method not defined");
				LOGGER.error("建议查看Tomcat server.xml 的 maxPostSize的设置是否正确");
				rstJson.put("RST", false);
				rstJson.put("ERR", "method not defined，建议查看Tomcat server.xml 的 maxPostSize的设置是否正确");
				rstJson.put("METHOD", method);
				out(rstJson.toString());
			}
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());

			rstJson.put("RST", false);
			rstJson.put("method", method);
			rstJson.put("ERR", err.getMessage());
			rstJson.put("ERRS", err.getStackTrace());
			out(rstJson.toString());
		}
	}

	/**
	 * The local server sends a file to the remote server
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private String handleStartSend(SyncRemote s) throws Exception {
		String name = rv.getString("name");
		String id = rv.getString("id");
		String rst = s.sendFile(id, name);
		
		return rst;
	}
	
	/**
	 * The local server receives a file from the remote server
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private String handleLocalReviceRemoteFile(SyncRemote s) throws Exception {
		String name = rv.getString("name");
		String id = rv.getString("id");
		String rst = s.localRequstRemoveFile(id, name);
		
		return rst;
	}
	
	/**
	 * the remote server sends a file to the local
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private String handleRemoteSendFile(SyncRemote s) throws Exception {
		String contentEncode = rv.getString("GDX");
		JSONObject contentJson;
		try {
			String contentDecode = s.decode(contentEncode);
			contentJson = new JSONObject(contentDecode);
		} catch (Exception err) {
			LOGGER.error(err.getLocalizedMessage());
			return "{RST:false,ERR:'decode error'}";
		}
		String id = contentJson.optString("remote_id");
		String name = contentJson.getString("name");

		JSONObject cfgs = s.getCfgs();
		JSONObject cfg = cfgs.getJSONObject(id);
		String pathThis = cfg.getString("target");

		String rst = s.removeSendFile(id, pathThis, name);
		String encoderst = s.encode(rst);
		
		return encoderst;
	}
	
	private JSONObject handleGetCfgs(SyncRemote s ) {
		String ewascriptpath = rv.s("EWA_SCRIPT_PATH");
		
		ScriptPath sp = ScriptPaths.getInstance().getScriptPath(ewascriptpath);
		// 因为安全原因删除了属性，因此需要克隆配置
		JSONObject cfg = new JSONObject(s.getCfgs().toString());
		cfg.remove("REMOTE_CODE");
		cfg.remove("REMOTE_URL");
		if (sp.isJdbc()) {
			JdbcConfigOperation op = new JdbcConfigOperation(sp);
			// 数据库管理的配置文件，输出到本地cache
			try {
				op.exportAll();
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
		}
		
		return cfg;
	}
	
	private SyncRemote getRemote(String cfgkey) {
		SyncRemotes syncRemotes = new SyncRemotes();
		SyncRemote s = syncRemotes.getRemoteInstance(cfgkey);

		return s;
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request  the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException      if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	/**
	 * Returns information about the servlet, such as author, version, and
	 * copyright.
	 * 
	 * @return String information about this servlet
	 */
	public String getServletInfo() {
		return "EWA(v2.0)";
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
