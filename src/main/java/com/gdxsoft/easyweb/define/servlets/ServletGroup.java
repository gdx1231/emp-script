package com.gdxsoft.easyweb.define.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.conf.ConnectionConfig;
import com.gdxsoft.easyweb.conf.ConnectionConfigs;
import com.gdxsoft.easyweb.define.DefineAcl;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.define.UpdateXmlImpl;
import com.gdxsoft.easyweb.define.UpdateXmlJdbcImpl;
import com.gdxsoft.easyweb.define.group.Exchange;
import com.gdxsoft.easyweb.define.group.ModuleCopy;
import com.gdxsoft.easyweb.define.group.ModuleExport;
import com.gdxsoft.easyweb.define.group.ModuleImport;
import com.gdxsoft.easyweb.define.group.ModulePublish;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.servlets.FileOut;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UUrl;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ServletGroup extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletGroup.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -2539866920027338064L;

	public ServletGroup() {
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

	/**
	 * 复制已下载的模块为自己的模块
	 * 
	 * @param rv
	 * @return
	 */
	private JSONObject copyPublishedModule(RequestValue rv) {

		String replaceMetaDatabaseName = rv.s("replace_meta_databaseName");// "`visa_main_data`";
		String replaceWorkDatabaseName = rv.s("replace_work_databaseName"); // "`visa`";

		String metaDataConn = rv.s("meta_data_conn"); // "`visa`";
		String workDataConn = rv.s("work_data_conn"); // "`visa`";

		ModuleCopy moduleCopy = new ModuleCopy(metaDataConn, replaceMetaDatabaseName, workDataConn,
				replaceWorkDatabaseName);

		int mod_dl_id = rv.getInt("mod_dl_id");
		JSONObject result = moduleCopy.copyDownloadModule(mod_dl_id, rv);

		return result;
	}

	/**
	 * 将已经下载的模块作为文件下载到本地
	 * 
	 * @param rv
	 * @param response
	 * @throws IOException
	 */
	private void downloadPublishedModuleAsFile(RequestValue rv, HttpServletResponse response) throws IOException {
		ModuleImport moduleImport = new ModuleImport(null, null, null);
		int mod_dl_id = rv.getInt("mod_dl_id");
		byte[] buf = moduleImport.downloadPublishedModuleAsFile(mod_dl_id, rv);

		if (buf == null) {
			response.setStatus(404);
		}

		String id = moduleImport.getModuleCode() + "_" + moduleImport.getModuleVersion();
		response.setHeader("Location", id + ".zip");
		response.setHeader("Cache-Control", "max-age=0");
		response.setHeader("Content-Disposition", "attachment; filename=" + id + ".zip");
		response.setContentType("image/oct");
		response.setContentLength(buf.length);
		response.getOutputStream().write(buf);
		response.getOutputStream().close();
	}

	/**
	 * 导入已经下载的模块
	 * 
	 * @param rv
	 * @return
	 */
	private JSONObject importDownloadedPublishedModule(RequestValue rv) {

		String importDataConn = rv.s("import_data_conn");// "visa";
		String replaceMetaDatabaseName = rv.s("replace_meta_databaseName");// "`visa_main_data`";
		String replaceWorkDatabaseName = rv.s("replace_work_databaseName"); // "`visa`";

		// 导入表/视图结构
		boolean importTables = "Y".equals(rv.s("IMPORT_TABLE"));
		// 导入数据
		boolean importData = "Y".equals(rv.s("IMPORT_DATA"));
		// 导入配置项目
		boolean importXitems = "Y".equals(rv.s("IMPORT_CFG"));

		if (!(importTables || importData || importXitems)) {
			return UJSon.rstFalse("Nothing to do.");
		}

		ModuleImport moduleImport = new ModuleImport(importDataConn, replaceMetaDatabaseName, replaceWorkDatabaseName);
		moduleImport.setImportData(importData);
		moduleImport.setImportTables(importTables);
		moduleImport.setImportXItems(importXitems);

		int mod_dl_id = rv.getInt("mod_dl_id");
		JSONObject result = moduleImport.importModuleFromDownloadModule(mod_dl_id, rv);
		return result;
	}

	/**
	 * 本地输出模块
	 * 
	 * @param rv
	 * @return
	 */
	private JSONObject exportModule(RequestValue rv) {
		JSONObject result = new JSONObject();
		String moduleCode = rv.s("mod_code"); // "com.gdxsoft.backAdmin.menu";
		result.put("moduleCode", moduleCode);

		String moduleVersion = rv.s("mod_ver"); // "1.0";
		result.put("moduleVersion", moduleVersion);

		String jdbcConfigName = rv.s("ewa_conn"); // ewa;
		result.put("jdbcConfigName", jdbcConfigName);

		ModuleExport moduleExport = new ModuleExport(moduleCode, moduleVersion, jdbcConfigName);

		try {
			result = moduleExport.exportModule();
		} catch (Exception e) {
			UJSon.rstSetFalse(result, e.getMessage());
		}

		return result;
	}

	/**
	 * 发布模块到远程模块服务器
	 * 
	 * @param rv
	 * @return
	 */
	private JSONObject publishModule(RequestValue rv) {
		JSONObject result = new JSONObject();

		ModulePublish moduleExport = new ModulePublish();
		long modVerId = 0l;

		try {
			if (rv.s("mod_ver_id") != null) {
				modVerId = rv.getLong("mod_ver_id");
			}
			result = moduleExport.publishToServer(modVerId);
		} catch (Exception e) {
			UJSon.rstSetFalse(result, e.getMessage());
		}

		return result;
	}

	/**
	 * 从远程服务器下载模块到本地数据库
	 * 
	 * @param rv
	 * @return
	 */
	private JSONObject downloadPublishedModule(RequestValue rv) {
		String data = rv.s("data");
		JSONArray parameters = new JSONArray(data);
		ModulePublish moduleExport = new ModulePublish();
		try {
			return moduleExport.downloadFromPublishServer(parameters);
		} catch (Exception e) {
			return UJSon.rstFalse(e.getMessage());
		}

	}

	/**
	 * 下载本地模块， 2021新方法
	 * 
	 * @param rv
	 * @param response
	 * @throws Exception
	 */
	private void downloadExportModule(RequestValue rv, HttpServletResponse response) throws Exception {
		JSONObject result = new JSONObject();
		String moduleCode = rv.s("mod_code"); // "com.gdxsoft.backAdmin.menu";
		result.put("moduleCode", moduleCode);

		String moduleVersion = rv.s("mod_ver"); // "1.0";
		result.put("moduleVersion", moduleVersion);

		String ewaConntectionString = rv.s("ewa_conn"); // ewa;
		result.put("ewaConntectionString", ewaConntectionString);

		ModuleExport moduleExport = new ModuleExport(moduleCode, moduleVersion, ewaConntectionString);

		byte[] buf = moduleExport.getExportModuleFile();

		if (buf == null) {
			response.setStatus(404);
		}

		String id = moduleCode + "_" + moduleVersion;
		response.setHeader("Location", id + ".zip");
		response.setHeader("Cache-Control", "max-age=0");
		response.setHeader("Content-Disposition", "attachment; filename=" + id + ".zip");
		response.setContentType("image/oct");
		response.setContentLength(buf.length);
		response.getOutputStream().write(buf);

		response.getOutputStream().close();

	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ConfDefine.isAllowDefine()) {
			response.setStatus(404);
			LOGGER.info("Not allow define", request == null ? "?not request?" : request.getRequestURI());
			return;
		}

		RequestValue rv = new RequestValue(request);
		String oType = rv.getString("TYPE");
		if (oType == null) {
			return;
		}

		DefineAcl acl = new DefineAcl();
		acl.setRequestValue(rv);

		if (!acl.canRun()) { // not login
			if (oType.equalsIgnoreCase("export_module")) {
				response.getWriter().println(UJSon.rstFalse("deny! request login"));
				return;
			}
			response.getWriter().println("deny! request login");
			return;
		}
		if (oType.equalsIgnoreCase("dl")) { // 下载生成的组件
			String id = request.getParameter("id");
			String path = UPath.getGroupPath() + "/exports/" + id + ".zip/";
			FileInputStream fs = new FileInputStream(path);
			byte[] buf = new byte[fs.available()];
			fs.read(buf);
			fs.close();

			response.setHeader("Location", id + ".zip");
			response.setHeader("Cache-Control", "max-age=0");
			response.setHeader("Content-Disposition", "attachment; filename=" + id + ".zip");
			// filename应该是编码后的(utf-8)
			response.setContentType("image/oct");
			response.setContentLength(buf.length);
			response.getOutputStream().write(buf);

			response.getOutputStream().close();
			return;
		}

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");

		// 复制下载的模块到本地的发布 2023新方法
		if (oType.equalsIgnoreCase("publish_module_copy")) {
			JSONObject result = this.copyPublishedModule(rv);
			response.getWriter().println(result.toString());
			return;
		}
		// 下载的模块导出为文件 2023新方法
		if (oType.equalsIgnoreCase("publish_module_download_as_file")) {
			try {
				this.downloadPublishedModuleAsFile(rv, response);
			} catch (Exception err) {
				response.getWriter().println(UJSon.rstFalse(err.getMessage()));
			}
			return;
		}
		// 导入已经下载的模块 2021新方法
		if (oType.equalsIgnoreCase("publish_module_import")) {
			JSONObject result = this.importDownloadedPublishedModule(rv);
			response.getWriter().println(result.toString());
			return;
		}
		// 发布模块到模块服务器 2021新方法
		if (oType.equalsIgnoreCase("publish_module")) {

			JSONObject result = this.publishModule(rv);
			response.getWriter().println(result.toString());
			return;
		}
		// 从发布模块服务器下载模块 2021新方法
		if (oType.equalsIgnoreCase("publish_module_download")) {
			try {
				JSONObject result = this.downloadPublishedModule(rv);
				response.getWriter().println(result.toString());
			} catch (Exception err) {
				response.getWriter().println(UJSon.rstFalse(err.getMessage()));
			}
			return;
		}
		// 本地导出模块 2021新方法
		if (oType.equalsIgnoreCase("export_module")) {
			JSONObject result = this.exportModule(rv);
			response.getWriter().println(result.toString());
			return;
		}
		// 本地下载模块， 2021新方法
		if (oType.equalsIgnoreCase("export_module_download")) {
			try {
				this.downloadExportModule(rv, response);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				response.setStatus(404);
			}
			return;
		}
		// 跳转到导出模块的网址
		if (oType.equalsIgnoreCase("goto_export_module_site")) {
			String url = ConfDefine.getInstance().getApiServer();
			response.sendRedirect(url);

			return;
		}

		// 跳转到查看模块的网址
		if (oType.equalsIgnoreCase("goto_export_module")) {
			String url = ConfDefine.getInstance().getApiServer();
			url += "/module";
			UUrl uu = new UUrl(url);
			uu.add("mod_ver", rv.s("mod_ver"));
			uu.add("mod_code", rv.s("mod_code"));

			String u1 = uu.getUrlWithDomain();
			response.sendRedirect(u1);

			return;
		}

		String ewaPath = rv.s("rv_ewa_style_path");
		if (StringUtils.isBlank(ewaPath)) {
			ewaPath = "/EmpScriptV2"; // default static url prefix
		}
		MStr sbHead = new MStr();
		sbHead.al("<html>");
		sbHead.al("<head>");
		sbHead.al("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		sbHead.al("<title></title>");
		sbHead.al("<link rel=\"stylesheet\" rev=\"stylesheet\"  href=\"" + ewaPath
				+ "/EWA_STYLE/skins/default/css.min.css\" type=\"text/css\" />");
		sbHead.al("<script type='text/javascript' src='" + ewaPath + "/EWA_STYLE/js/ewa.min.js'></script>");
		sbHead.al("</head>");
		sbHead.al("<body style=\"margin: 1px; overflow: auto\" onload='EWA.CP=\"" + request.getContextPath()
				+ "\";EWA.LANG=\"zhcn\"'>");
		response.getWriter().println(sbHead.toString());

		if (oType.equalsIgnoreCase("install")) {
			String root = UPath.getGroupPath() + "/imports/";
			File fileRoot = new File(root);
			String[] ff = { ".zip" };
			File[] dirs = UFile.getFiles(root, ff);
			if (dirs == null) {
				response.getWriter().println(fileRoot.getAbsolutePath() + " 没有数据!");
				return;
			}
			MStr sb = new MStr();
			sb.al("<div align=center><h1>可用的组件" + fileRoot.getAbsolutePath() + "</h1></div>");
			sb.al("<table border=0 bgcolor='#cdcdcd' align=center cellpadding=1 cellspacing=1 width=800>");
			sb.al("<tr bgcolor=white><th>文件</th><th>时间</th><th>字节</th><th>安装</th></tr>");
			for (int i = 0; i < dirs.length; i++) {
				String name = dirs[i].getName();
				File f = new File(dirs[i].getPath());
				name = name.split("\\.")[0];
				Date t = new Date(f.lastModified());
				String dateStr;
				try {
					dateStr = UFormat.formatDate("datetime", t, "zhcn");
				} catch (Exception e) {
					dateStr = e.getMessage();
				}

				sb.al("<tr bgcolor=white><td height=24>" + f.getName() + "</td><td>" + dateStr + "</td><td>"
						+ f.length() / 1024 + "k</td><td align=center>");
				sb.al(" <a href='./?TYPE=step1&id=" + name + "'>安装</a></td></tr>");
			}
			sb.al("</table>");
			response.getWriter().println(sb.toString());
		} else if (oType.equalsIgnoreCase("make")) {
			String id = request.getParameter("id");
			Exchange ex = new Exchange(id);

			String s;
			String s2;
			try {
				s = ex.exportGroup();
				File f1 = new File(s);
				s2 = f1.getAbsolutePath();
			} catch (Exception e) {
				s2 = e.getMessage();
			}
			response.getWriter().println("<h1>生产组件结果</h1>");
			response.getWriter().println("<h2><a target=_blank href='./?type=dl&id=" + id + "'>");
			response.getWriter().println(s2);
			response.getWriter().println("</a></h2>");
		} else if (oType.equalsIgnoreCase("step1")) {
			StringBuilder sb1 = new StringBuilder();
			sb1.append("<option></option>");
			ConnectionConfigs cfgs;
			MStr sb = new MStr();
			try {
				cfgs = ConnectionConfigs.instance();
				for (int i = 0; i < cfgs.size(); i++) {
					ConnectionConfig cfg = cfgs.getConfig(i);
					sb1.append("<option value='" + cfg.getName() + "'>" + cfg.getName() + "  (" + cfg.getType() + ", "
							+ cfg.getConnectionString() + ")</option>");
				}

			} catch (ParserConfigurationException e) {
				sb.al(e.getMessage());
			} catch (SAXException e) {
				sb.al(e.getMessage());
			}

			ConfScriptPaths sps = ConfScriptPaths.getInstance();
			StringBuilder sb2 = new StringBuilder();
			for (int i = 0; i < sps.getLst().size(); i++) {
				ConfScriptPath sp = sps.getLst().get(i);
				if (sp.isReadOnly()) {
					continue;
				}
				sb2.append("<option value='" + sp.getName() + "'>" + sp.getName() + "(" + sp.getPath() + ")</option>");
			}
			sb2.append("</select>");

			sb.al("<div align=center><h1>设定导入参数</h1></div>");
			sb.al("<form action=\"./?TYPE=step2\" method=post>");
			sb.al("<table bgcolor='#cdcdcd' width=600 border=0 cellpadding=1 cellspacing=1 align=center>");
			sb.al("	<tr bgcolor=white><td>组件Id：</td>");
			sb.al("<td>" + Utils.textToInputValue(request.getParameter("id")) + "<input type=hidden name=id value=\""
					+ Utils.textToInputValue(request.getParameter("id")) + "\">");
			sb.al("</td></tr>");
			sb.al("<tr bgcolor=white><td>目标数据源：</td><td>");

			sb.al("<select name=datasource>" + sb1.toString() + "</select>");
			sb.al("</td></tr>");

			sb.al("<tr bgcolor=white><td>选择ScriptPath：</td><td>");
			sb.al("<select name='script_path'>" + sb2.toString() + "</select>");
			sb.al("</td></tr>");

			sb.al("<tr bgcolor=white><td>保存文件：（采用“|test|abc.xml”格式）</td>");
			sb.al("<td><input type=text name=xmlfile></td></tr>");

			sb.al("<tr bgcolor=white><td colspan=2>");
			sb.al("<input type=submit value='确定'>");
			sb.al("</td></tr></table></form>");
			response.getWriter().println(sb.toString());
		} else if (oType.equalsIgnoreCase("step2")) {
			MStr sb = new MStr();
			String script_path = rv.s("script_path").trim();
			ConfScriptPaths sps = ConfScriptPaths.getInstance();
			ConfScriptPath sp = sps.getScriptPath(script_path);

			IUpdateXml ux = null;
			if (sp.isReadOnly()) {
				response.getWriter().println(Utils.textToInputValue(script_path) + " is read only!");
				return;
			}
			if (sp.isJdbc()) {
				ux = new UpdateXmlJdbcImpl(sp);
			} else if (sp.isResources()) {

			} else {
				ux = new UpdateXmlImpl(sp);
			}

			if (ux == null) {
				response.getWriter().println("Null parameter script_path: " + Utils.textToInputValue(script_path) + "");
				return;
			}

			String datasource = request.getParameter("datasource").trim();
			String xmlfile = request.getParameter("xmlfile").trim();
			String id = request.getParameter("id").trim();
			Exchange g1 = new Exchange(id, datasource);

			try {
				g1.importGroup();
				String s = "";
				s = g1.importTableAndData();
				sb.al(s);
				sb.al("<hr>" + g1.importReses());
				sb.al("<hr>\r\n导入到：" + g1.importCfgs(xmlfile, ux));
			} catch (Exception e) {
				sb.al(e.getMessage());
			}
			response.getWriter().println("<pre>");
			response.getWriter().println(sb.toString());
			response.getWriter().println("</pre>");
		}

		response.getWriter().println("</body></html>");
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
