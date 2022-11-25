/**
 *
 */
package com.gdxsoft.easyweb.define.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.SystemXmlUtils;
import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.conf.ConfScriptPath;
import com.gdxsoft.easyweb.conf.ConfScriptPaths;
import com.gdxsoft.easyweb.data.DTColumn;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.define.CodeFormat;
import com.gdxsoft.easyweb.define.ConfigUtils;
import com.gdxsoft.easyweb.define.DefineAcl;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.define.UpdateXmlBase;
import com.gdxsoft.easyweb.define.UserDirXmls;
import com.gdxsoft.easyweb.define.database.SqlSyntaxCheck;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.servlets.GZipOut;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UJSon;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UUrl;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 *
 */
public class ServletXml extends HttpServlet {

	// servlet是线程不安全的 一般不要轻易定成员变量 ，用局部变量吧
	/**
	 *
	 */
	private static final long serialVersionUID = 3799263110869941332L;
	private static Logger LOGGER = LoggerFactory.getLogger(ServletXml.class);

	public ServletXml() {
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

	private void setOutType(HttpServletResponse response, String cType) {
		String s = "text/" + cType + ";charset=UTF-8";
		response.setHeader("Content-Type", s);
		response.setHeader("EWA", "V2.2;gdxsoft.com");
		response.setCharacterEncoding("UTF-8");
	}

	private void outContent(HttpServletRequest request, HttpServletResponse response, String cnt)
			throws ServletException, IOException {
		GZipOut o = new GZipOut(request, response);
		o.outContent(cnt);

	}

	/**
	 * 获取更新接口
	 * 
	 * @param xmlName
	 * @return
	 */
	private IUpdateXml getUpdateXml(String xmlName, String admId) {
		IUpdateXml o = ConfigUtils.getUpdateXml(xmlName);
		if (o == null) {
			o = ConfigUtils.getUpdateXmlByPath(xmlName);
		}
		if (o != null) {
			o.setAdmin(admId);
		}
		return o;

	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ConfDefine.isAllowDefine()) {
			LOGGER.info("Not allow define", request == null ? "?NO request?" : request.getRequestURI());
			response.setStatus(404);
			return;
		}

		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		RequestValue rv = new RequestValue(request, session);

		String oType = rv.getString("TYPE");
		String mode = rv.getString("MODE");
		if (mode == null) {
			mode = "";
		}
		LOGGER.info("TYPE=" + oType + ", MPDE=" + mode);

		if (oType != null && oType.equalsIgnoreCase("format")) { // 格式化语法
			String code = rv.getString("code");
			String rst = CodeFormat.format(code, mode);
			this.outContent(request, response, rst);
			return;
		}

		String cnt = null;

		if (oType != null && oType.toUpperCase().equals("GUNID")) { // 获取全局编号
			this.setOutType(response, "javascript");
			cnt = this.handleGUNID(rv);
			this.outContent(request, response, cnt);
			return;
		}

		DefineAcl acl = new DefineAcl();
		acl.setRequestValue(rv);
		if (!acl.canRun()) { // not login
			JSONObject rst = new JSONObject();
			rst.put("RST", false);
			rst.put("ERR", "deny! request login");
			response.getWriter().println(rst);
			return;
		}
		PageValue pvAdmin = acl.getAdmin();

		if (oType == null) {
			cnt = this.handleNull(rv, response);
		} else if (oType.equals("ALL")) {
			this.setOutType(response, "javascript");
			cnt = this.handleAll(rv, pvAdmin);
		} else if (oType.equals("SQLS")) {
			this.setOutType(response, "html");
			cnt = this.handleSqls(rv, pvAdmin);
		} else if (oType.equals("SAVE")) {
			this.handleSave(rv, pvAdmin);
			this.setOutType(response, "html");
			cnt = "alert('ok')";
		} else if (oType.equals("VIEW")) {
			cnt = this.handleView(rv);
		} else if (oType.equals("CFG_XML")) {// 调用配置文件
			cnt = this.handleCfgXml(rv, response);
		} else if (oType.equals("CFG_DIR")) {
			UserDirXmls udx = new UserDirXmls();
			this.setOutType(response, "xml");
			cnt = udx.getXml();
		} else if (oType.equals("DELETE")) {
			this.handleRemove(rv, pvAdmin);
			this.setOutType(response, "html");
		} else if (oType.equals("PASTE")) {
			this.handlePaste(rv, pvAdmin);
			this.setOutType(response, "html");
		} else if (oType.equals("DELETE_BAKS")) {// 递归删除备份文件
			this.setOutType(response, "html");
			cnt = handleDeleteBaks(rv, pvAdmin) + "";
		} else if (oType.toUpperCase().equals("GUNID")) { // 获取全局编号
			this.setOutType(response, "javascript");
			cnt = this.handleGUNID(rv);
		} else if (oType.toUpperCase().equals("CHECK_SQL")) { // 检查SQL语法
			SqlSyntaxCheck syntaxCheck = new SqlSyntaxCheck(rv);
			cnt = syntaxCheck.checkSyntax();
		} else if (oType.toUpperCase().equals("EWA_CONF")) {
			// 加载本机的ewa_conf.xml
			cnt = this.handleEwaConf();
		} else if (oType.toUpperCase().equals("EWAC_DDL_RELOAD")) {
			cnt = this.handleEwacDdlReload(rv, pvAdmin);
		} else if (oType.toUpperCase().equals("EWAC_DDL_READ")) {
			// 从配置文件中加载DropList配置信息
			try {
				cnt = this.handleEwacDdlRead(rv);
			} catch (Exception e) {
				cnt = e.getMessage();
			}
		} else if (oType.toUpperCase().equals("GET_DOC_XML")) {
			// 获取整个配置文件
			this.handleGetDocXml(rv, response, pvAdmin);
			return;
		} else if (oType.toUpperCase().equals("IMPORT_XML")) {
			cnt = this.handleImportXml(rv, pvAdmin);
			this.setOutType(response, "json");
		} else if (oType.toUpperCase().equals("SAVE_JAVA")) {
			// 保存java代码
			JSONObject rst = this.saveJavaCode(rv);
			cnt = rst.toString();
			this.setOutType(response, "json");
		} else if ("selectSql2DTTable".equalsIgnoreCase(oType)) {
			// sql 语句转换为 DTTable的java代码
			JSONObject rst = this.handleSelectSql2DTTable(rv);
			cnt = rst.toString();
			this.setOutType(response, "json");
		}
		this.outContent(request, response, cnt);
	}

	/**
	 * sql 语句转换为 DTTable的java代码
	 * 
	 * @param rv
	 * @return
	 */
	private JSONObject handleSelectSql2DTTable(RequestValue rv) {
		JSONObject obj = UJSon.rstTrue();

		String sql = rv.s("SQL");
		String configName = rv.s("CONFIG_NAME");
		if (configName == null) {
			configName = "";
		}
		String prefix = rv.s("PREF_FIX");
		if (prefix == null) {
			prefix = "";
		}
		obj.put("SQL", sql);
		obj.put("CONFIG_NAME", configName);
		String sql1 = "select a.* from (" + sql + ")a where 1=2";
		DTTable tb = DTTable.getJdbcTable(sql1, configName);

		if (!tb.isOk()) {
			UJSon.rstSetFalse(obj, tb.getErrorInfo());
			return obj;
		}
		MStr sb = new MStr();
		sb.setNewLine("\n");
		sb.al("StringBuilder " + prefix + "sb = new StringBuilder()");
		String[] sqls = sql.split("\n");
		for (int i = 0; i < sqls.length; i++) {
			String s = sqls[i];
			if (s.trim().length() == 0) {
				continue;
			}
			s = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    ");
			sb.al("sb.append(\"" + s + "\\n\");");
		}
		sb.al("DTTable " + prefix + "tb = new DTTable(" + prefix + "sb.toString(), \"" + configName + "\", rv);");
		sb.al("for(int i = 0;i < " + prefix + "tb.getCount(); i++){");
		JSONArray columns = new JSONArray();
		for (int i = 0; i < tb.getColumns().getCount(); i++) {
			DTColumn col = tb.getColumns().getColumn(i);
			JSONObject colJson = new JSONObject(col);
			columns.put(colJson);
			String name = this.field2ClassName(col.getName());
			String code;
			if ("java.lang.String".equalsIgnoreCase(col.getClassName())) {
				code = "String " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").toString();";
			} else if ("java.lang.Integer".equalsIgnoreCase(col.getClassName())) {
				code = "Int " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").toInt();";
			} else if ("java.lang.Long".equalsIgnoreCase(col.getClassName())) {
				code = "Long " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").toLong();";
			} else if ("java.lang.Double".equalsIgnoreCase(col.getClassName())) {
				code = "Double " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").toDouble();";
			} else if ("java.sql.Timestamp".equalsIgnoreCase(col.getClassName())) {
				code = "Date " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").toDate();";
			} else if ("java.math.BigDecimal".equalsIgnoreCase(col.getClassName())) {
				code = "BigDecimal " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").toBigDecimal();";
			}  else if ("java.math.BigInteger".equalsIgnoreCase(col.getClassName())) {
				code = "BigInteger " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").toBigInteger();";
			}  else {
				code = "Object " + name + " = " + prefix + "tb.getCell(i, \"" + col.getName() + "\").getValue();";
			}
			
			sb.al("    " + code);
		}
		sb.al("}");
		obj.put("JAVA", sb.toString());
		obj.put("COLUMNS", columns);
		return obj;
	}

	private String field2ClassName(String field) {
		String[] names = field.split("\\_");
		MStr s = new MStr();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name.length() == 0) {
				s.a("_");
				continue;
			}
			String firstAlpha = name.substring(0, 1);
			if (i == 0) {
				s.a(firstAlpha.toLowerCase());
			} else {
				s.a(firstAlpha.toUpperCase());
			}
			if (name.length() > 1) {
				String others = name.substring(1);
				s.a(others.toLowerCase());
			}
		}
		return s.toString();
	}

	/**
	 * View the xml's string
	 * 
	 * @return
	 */
	private String handleView(RequestValue rv) {
		String xml = rv.getString("XML");
		Document doc = UXml.asDocument(xml);
		UpdateXmlBase.clearDoc(doc);

		String xmlPretty = UXml.asXmlPretty(doc);

		return xmlPretty;
	}

	/**
	 * return the xmlname's all sqls
	 * 
	 * @return
	 */
	private String handleSqls(RequestValue rv, PageValue pvAdmin) {
		String xmlName = rv.getString(FrameParameters.XMLNAME);
		IUpdateXml up = this.getUpdateXml(xmlName, pvAdmin.getStringValue());
		String sqls = up.getSqls();
		return sqls;
	}

	private boolean handleSave(RequestValue rv, PageValue pvAdmin) {
		String xmlName = rv.getString(FrameParameters.XMLNAME);
		String itemName = rv.getString(FrameParameters.ITEMNAME);
		String xml = rv.getString("XML");
		IUpdateXml up = this.getUpdateXml(xmlName, pvAdmin.getStringValue());
		return up.updateItem(itemName, xml);
	}

	private String handleNull(RequestValue rv, HttpServletResponse response) {
		String modeName = rv.getString("MODE_NAME");
		String mode = rv.getString("MODE");
		if (mode == null) {
			mode = "";
		}
		if (StringUtils.isBlank(modeName)) {
			modeName = "CFG";
		}

		HtmlCreator hc = new HtmlCreator();
		try {
			hc.init(rv.getRequest(), rv.getSession(), response);
			String xml = hc.getConfigItemXml();
			if (mode.equals("JS")) {
				this.setOutType(response, "javascript");
				xml = "window[" + Utils.textToJscript(modeName) + "]=\"" + Utils.textToJscript(xml) + "\"";
			} else {
				this.setOutType(response, "xml");
			}
			return xml;
		} catch (Exception err) {
			return err.getMessage();
		}
	}

	private String handleAll(RequestValue rv, PageValue pvAdmin) {
		MStr s = new MStr();
		String xmlName = rv.getString(FrameParameters.XMLNAME);
		String itemName = rv.getString(FrameParameters.ITEMNAME);
		try {
			IUpdateXml up = this.getUpdateXml(xmlName, pvAdmin.getStringValue());
			String xml;
			if (up == null) {
				xml = "<root/>";
			} else {
				xml = up.queryItemXml(itemName);
			}
			s.a("window._CFG_ITEM=\"");
			s.a(Utils.textToJscript(xml));
			s.al("\";");

			xml = this.getCfgXmlJs("EwaGlobal.xml", "_CFG_GLOBAL");
			s.al(xml);

			xml = this.getCfgXmlJs("EwaConnections.xml", "_CFG_CNN");
			s.al(xml);

			xml = this.getCfgXmlJs("EwaSkin.xml", "_CFG_SKIN");
			s.al(xml);

			xml = this.getCfgXmlJs("EwaConfig.xml", "_CFG_MAIN");
			s.al(xml);
		} catch (Exception e) {
			s.a("ERROR:" + rv.getRequest().getQueryString());
		}

		return s.toString();
	}

	/**
	 * CFG_XML
	 * 
	 * @return
	 */
	private String handleCfgXml(RequestValue rv, HttpServletResponse response) {
		String xmlName = rv.getString(FrameParameters.XMLNAME);
		String name = xmlName;
		String xml = null;
		String modeName = rv.getString("MODE_NAME");
		String mode = rv.getString("MODE");
		if (mode == null) {
			mode = "";
		}

		LOGGER.info("name={}, mode={}, modeName={}", name, mode, modeName);

		if (mode.equals("JS")) {
			this.setOutType(response, "html");
			xml = this.getCfgXmlJs(name, modeName);
		} else {
			this.setOutType(response, "xml");
			xml = this.getCfgXml(name);
		}

		return xml;
	}

	private void handleRemove(RequestValue rv, PageValue pvAdmin) {
		String xmlName = rv.getString(FrameParameters.XMLNAME);
		String itemName = rv.getString(FrameParameters.ITEMNAME);
		IUpdateXml up = this.getUpdateXml(xmlName, pvAdmin.getStringValue());
		up.removeItem(itemName);
	}

	private void handlePaste(RequestValue rv, PageValue pvAdmin) {
		// PASTE
		String from = rv.getString("FROM").replace("|", "/");
		String to = rv.getString("TO").replace("|", "/").split("\\*")[0];
		String toName = rv.getString("TONAME");

		String[] froms = from.split("\\*");
		IUpdateXml up = this.getUpdateXml(froms[0], pvAdmin.getStringValue());
		String sourceXml = up.queryItemXml(froms[1]);
		IUpdateXml up1 = this.getUpdateXml(to, pvAdmin.getStringValue());
		up1.saveXml(toName, sourceXml);

	}

	/**
	 * remove the configuration's backups
	 * 
	 * @return the removes count
	 */
	private int handleDeleteBaks(RequestValue rv, PageValue pvAdmin) {
		String xmlname = rv.getString(FrameParameters.XMLNAME);
		IUpdateXml up = this.getUpdateXml(xmlname, pvAdmin.getStringValue());
		int bakFilesCount = up.deleteBaks(xmlname);

		return bakFilesCount;
	}

	private String handleGUNID(RequestValue rv) {
		String num = rv.getString("NUM");
		int iNum = 1;
		if (num != null && num.trim().length() > 0) {
			iNum = Integer.parseInt(num.trim());
		}
		if (iNum <= 0 || iNum > 100) {
			iNum = 1;
		}
		StringBuilder s = new StringBuilder();
		s.append("[");
		for (int i = 0; i < iNum; i++) {
			if (i > 0) {
				s.append(",\r\n");
			}
			s.append("\"" + Utils.getGuid() + "\"");
		}
		s.append("]");

		return s.toString();
	}

	/**
	 * return the ewa_conf.xml content
	 * 
	 * @return
	 */
	private String handleEwaConf() {
		JSONObject rst = new JSONObject();
		rst.put("RST", true);
		try {
			String xml = UXml.asXml(UPath.getCfgXmlDoc());
			rst.put("XML", xml);
		} catch (Exception e) {
			rst.put("RST", false);
			rst.put("ERR", e.toString());
		}

		return rst.toString();
	}

	private void handleGetDocXml(RequestValue rv, HttpServletResponse response, PageValue pvAdmin) throws IOException {
		// 获取整个配置文件
		String xmlname = rv.getString(FrameParameters.XMLNAME);
		IUpdateXml up = this.getUpdateXml(xmlname, pvAdmin.getStringValue());
		String cnt = up.getDocXml();

		String[] names = UserConfig.filterXmlNameByJdbc(xmlname).split("\\|");
		String name = names[names.length - 1];
		response.setHeader("Location", name);
		response.setHeader("Cache-Control", "max-age=30");
		response.setHeader("Content-Disposition", "attachment; filename=" + name);
		// filename应该是编码后的(utf-8)
		response.setContentType("text/xml");

		byte[] buf = cnt.getBytes(StandardCharsets.UTF_8);
		response.setContentLength(buf.length);
		response.getOutputStream().write(buf);
	}

	/**
	 * IMPORT_XML
	 * 
	 * @return
	 */
	private String handleImportXml(RequestValue rv, PageValue pvAdmin) {
		String sourceXmlFilePath = UPath.getPATH_UPLOAD() + rv.s("UP_NAME");

		String path = rv.s("path");
		String xmlname = rv.s(FrameParameters.XMLNAME);

		IUpdateXml up = this.getUpdateXml(path, pvAdmin.getStringValue());
		JSONObject rst = up.importXml(path, xmlname, sourceXmlFilePath);

		return rst.toString();
	}

	/**
	 * EWAC_DDL_READ
	 * 
	 * @return
	 * @throws Exception
	 */
	private String handleEwacDdlRead(RequestValue rv) throws Exception {
		String ref = rv.s("SYS_REMOTE_REFERER");
		if (StringUtils.isBlank(ref)) {
			throw new Exception("Need the http referer");
		}
		UUrl uref = new UUrl(ref);
		String D_XMLNAME = uref.getParamter("D_XMLNAME");
		if (StringUtils.isBlank(D_XMLNAME)) {
			throw new Exception("Need the parameter D_XMLNAME in the http referer");
		}
		IConfig configType = UserConfig.getConfig(D_XMLNAME, null);
		ConfScriptPath sp = configType.getScriptPath();

		ConfigUtils configUtils = new ConfigUtils(sp);
		// 从配置文件中加载DropList配置信息
		return configUtils.loadDdls();
	}

	/**
	 * EWAC_DDL_RELOAD
	 * 
	 * @return
	 */
	private String handleEwacDdlReload(RequestValue rv, PageValue pvAdmin) {
		String ewascriptpath = rv.s(FrameParameters.EWA_SCRIPT_PATH);
		ConfScriptPath sp = ConfScriptPaths.getInstance().getScriptPath(ewascriptpath);

		if (sp == null) {
			// 查找第一个不是资源(jar文件）的配置
			List<ConfScriptPath> al = ConfScriptPaths.getInstance().getLst();
			for (int i = 0; i < al.size(); i++) {
				ConfScriptPath sp1 = al.get(i);
				if (!sp1.isResources()) {
					sp = sp1;
					break;
				}
			}
		}
		// 刷新配置文件中 DropList配置信息
		ConfigUtils configUtils = new ConfigUtils(sp);
		JSONObject rst = new JSONObject();
		int inc;
		try {
			inc = configUtils.renewDdls(pvAdmin.getStringValue());

			rst.put("RST", true);
			rst.put("MSG", inc);
		} catch (Exception e) {
			rst.put("RST", false);
			rst.put("MSG", e.getMessage());
			rst.put("Exception", e);
		}

		return rst.toString();
	}

	/**
	 * 保存java代码
	 * 
	 * @param rv
	 * @return
	 */
	private JSONObject saveJavaCode(RequestValue rv) {
		JSONObject rst = new JSONObject();
		rst.put("RST", false);
		String path = rv.s("path"); // 源文件所在目录
		String java = rv.s("java"); // java代码
		String name = rv.s("name"); // 文件名称
		String packagename = rv.s("packagename"); // 包名

		if (path == null || path.trim().length() == 0) {

			rst.put("ERR", "Path empty");

			return rst;
		}
		if (name == null || name.trim().length() == 0) {
			rst.put("ERR", "Name empty");

			return rst;
		}

		if (name.indexOf(".") >= 0 || name.indexOf("/") >= 0 || name.indexOf("\\") >= 0) {
			rst.put("ERR", "Name invalid");

			return rst;
		}

		if (packagename == null || packagename.trim().length() == 0) {
			rst.put("ERR", "packagename empty");

			return rst;
		}
		if (packagename.indexOf("/") >= 0 || packagename.indexOf("\\") >= 0) {
			rst.put("ERR", "packagename invalid");

			return rst;
		}
		if (java == null || java.trim().length() == 0) {
			rst.put("ERR", "Code empty");

			return rst;
		}

		File fPath = new File(path.trim());

		if (!fPath.exists()) {
			rst.put("ERR", "Saved path not exists(" + path + ")");
			return rst;
		}
		// 保存路径 path + 包名转换为路径 + 文件名称 + .java
		String path1 = fPath.getAbsolutePath() + "/" + packagename.trim().replace(".", "/");
		String pathAndName = path1 + "/" + name.trim() + ".java";

		StringBuilder sbCode = new StringBuilder("package ");
		sbCode.append(packagename.trim());
		sbCode.append(";\n\n");
		sbCode.append(java);
		try {
			UFile.createNewTextFile(pathAndName, sbCode.toString());
			rst.put("RST", true);
			rst.put("MSG", pathAndName);
		} catch (IOException e) {
			rst.put("ERR", e);
			LOGGER.info(e.getLocalizedMessage());
		}

		return rst;
	}

	private String getCfgXml(String name) {
		String xml;
		if (name.equals("EwaConnections.xml")) { // 数据库连接
			// 新方法 从ewa_conf.xml中获取
			if (UPath.getDATABASEXML() != null) {
				xml = UPath.getDATABASEXML();
				xml = xml.replace("<databases", "<root");
				xml = xml.replace("</databases>", "</root>");
				return xml;
			}
		}
		try {
			xml = SystemXmlUtils.getSystemConfContent(name);
			// xml = UXml.asXmlAll(UXml.retDocument(name1));
		} catch (Exception e) {
			xml = "GET " + name + " ERROR:" + e.getMessage();
		}
		return xml;
	}

	private String getCfgXmlJs(String name, String jsName) {
		String xml = this.getCfgXml(name);
		String s1 = "window." + jsName + "=\"" + Utils.textToJscript(xml) + "\";";
		return s1;
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
