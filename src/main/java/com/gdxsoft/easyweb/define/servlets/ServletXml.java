/**
 *
 */
package com.gdxsoft.easyweb.define.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.gdxsoft.easyweb.define.CodeFormat;
import com.gdxsoft.easyweb.define.ConfigUtils;
import com.gdxsoft.easyweb.define.IUpdateXml;
import com.gdxsoft.easyweb.define.UpdateXmlBase;
import com.gdxsoft.easyweb.define.UserDirXmls;
import com.gdxsoft.easyweb.define.database.SqlSyntaxCheck;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.servlets.GZipOut;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * @author Administrator
 *
 */
public class ServletXml extends HttpServlet {

	private int _BakFilesCount = 0;
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
		o.setAdmin(admId);

		return o;

	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		RequestValue rv = new RequestValue(request, session);

		String oType = rv.getString("TYPE");
		String mode = rv.getString("MODE");

		LOGGER.info("TYPE=" + oType + ", MPDE=" + mode);

		if (oType != null && oType.equalsIgnoreCase("format")) { // 格式化语法
			String code = rv.getString("code");
			String rst = CodeFormat.format(code, mode);
			this.outContent(request, response, rst);
			return;
		}

		String modeName = rv.getString("MODE_NAME");

		if (mode == null) {
			mode = "";
		}

		String cnt = null;

		if (oType != null && oType.toUpperCase().equals("GUNID")) { // 获取全局编号
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
				s.append("\"").append(Utils.getGuid()).append("\"");
			}
			s.append("]");
			this.setOutType(response, "javascript");
			cnt = s.toString();
			this.outContent(request, response, cnt);
			return;
		}

		PageValue pv = rv.getPageValues().getPageValue("EWA_ADMIN_ID");
		// not login
		if (pv == null || (pv.getPVTag() != PageValueTag.SESSION)) {
			JSONObject rst = new JSONObject();
			rst.put("RST", false);
			rst.put("ERR", "deny! request login");
			response.getWriter().println(rst);
			return;
		}
		if (oType == null) {
			HtmlCreator hc = new HtmlCreator();
			try {
				hc.init(request, session, response);
				String xml = hc.getConfigItemXml();
				if (mode.equals("JS")) {
					this.setOutType(response, "javascript");
					xml = "window." + modeName + "=\"" + Utils.textToJscript(xml) + "\"";
				} else {
					this.setOutType(response, "xml");
				}
				cnt = xml;
			} catch (Exception err) {
				cnt = err.getMessage();
			}
		} else if (oType.equals("ALL")) {
			this.setOutType(response, "javascript");
			MStr s = new MStr();
			// HtmlCreator hc = new HtmlCreator();
			String xmlName = rv.getString("XMLNAME").replace("|", "/");
			String itemName = rv.getString("ITEMNAME");
			try {
				// hc.init(request, session, response);
				// String xml = hc.getConfigItemXml();
				IUpdateXml up = this.getUpdateXml(xmlName, pv.getStringValue());
				String xml = up.queryItemXml(itemName);
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
			cnt = s.toString();
		} else if (oType.equals("SQLS")) {
			String xmlName = rv.getString("XMLNAME").replace("|", "/");
			IUpdateXml up = this.getUpdateXml(xmlName, pv.getStringValue());
			String sqls = up.getSqls();
			this.setOutType(response, "html");
			cnt = sqls;
		} else if (oType.equals("SAVE")) {
			String xmlName = rv.getString("XMLNAME").replace("|", "/");
			String itemName = rv.getString("ITEMNAME");
			String xml = rv.getString("XML");
			IUpdateXml up = this.getUpdateXml(xmlName, pv.getStringValue());
			up.updateItem(itemName, xml);
			this.setOutType(response, "html");
			cnt = "alert('ok')";
		} else if (oType.equals("VIEW")) {
			String xml = rv.getString("XML");
			Document doc = UXml.asDocument(xml);
			UpdateXmlBase.clearDoc(doc);

			String xmlPretty = UXml.asXmlPretty(doc);
			// this.setOutType(response, "xml");
			cnt = xmlPretty;
		} else if (oType.equals("CFG_XML")) {// 调用配置文件
			String xmlName = rv.getString("XMLNAME");
			String name = xmlName.replace("|", "/");
			String xml = null;
			if (mode.equals("JS")) {
				this.setOutType(response, "html");
				xml = this.getCfgXmlJs(name, modeName);
			} else {
				this.setOutType(response, "xml");
				xml = this.getCfgXml(name);
			}
			cnt = xml;
		} else if (oType.equals("CFG_DIR")) {
			UserDirXmls udx = new UserDirXmls();
			this.setOutType(response, "xml");
			cnt = udx.getXml();
		} else if (oType.equals("DELETE")) {
			String xmlName = rv.getString("XMLNAME").replace("|", "/");
			String itemName = rv.getString("ITEMNAME");
			IUpdateXml up = this.getUpdateXml(xmlName, pv.getStringValue());
			this.setOutType(response, "html");
			up.removeItem(itemName);
		} else if (oType.equals("PASTE")) {
			String from = rv.getString("FROM").replace("|", "/");
			String to = rv.getString("TO").replace("|", "/").split("\\*")[0];
			String toName = rv.getString("TONAME");

			String[] froms = from.split("\\*");
			IUpdateXml up = this.getUpdateXml(froms[0], pv.getStringValue());
			String sourceXml = up.queryItemXml(froms[1]);
			IUpdateXml up1 = this.getUpdateXml(to, pv.getStringValue());
			up1.saveXml(toName, sourceXml);
			this.setOutType(response, "html");
		} else if (oType.equals("DELETE_BAKS")) {// 递归删除备份文件
			String xmlname = rv.getString("xmlname").replace("|", "/");
			IUpdateXml up = this.getUpdateXml(xmlname, pv.getStringValue());
			_BakFilesCount = up.deleteBaks(xmlname);
			this.setOutType(response, "html");
			cnt = _BakFilesCount + "";
		} else if (oType.toUpperCase().equals("GUNID")) { // 获取全局编号
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
			this.setOutType(response, "javascript");
			cnt = s.toString();
		} else if (oType.toUpperCase().equals("CHECK_SQL")) { // 检查SQL语法
			SqlSyntaxCheck syntaxCheck = new SqlSyntaxCheck(rv);
			cnt = syntaxCheck.checkSyntax();
		} else if (oType.toUpperCase().equals("EWA_CONF")) { // 加载本机的ewa_conf.xml
			String ewa_conf_file = UPath.getRealPath() + "/ewa_conf.xml";
			JSONObject rst = new JSONObject();
			rst.put("RST", true);
			String xml;
			try {
				xml = UFile.readFileText(ewa_conf_file);
				rst.put("XML", xml);
			} catch (Exception e) {
				rst.put("RST", false);
				rst.put("ERR", e.toString());
			}

			cnt = rst.toString();
		} else if (oType.toUpperCase().equals("EWAC_DDL_RELOAD")) {
			// 刷新配置文件中 DropList配置信息
			ConfigUtils configUtils = new ConfigUtils();
			JSONObject rst = new JSONObject();
			int inc;
			try {
				inc = configUtils.renewDdls(pv.getStringValue());

				rst.put("RST", true);
				rst.put("MSG", inc);
			} catch (Exception e) {
				rst.put("RST", false);
				rst.put("MSG", e.getMessage());
				rst.put("Exception", e);
			}

			cnt = rst.toString();
		} else if (oType.toUpperCase().equals("EWAC_DDL_READ")) {
			// 从配置文件中加载DropList配置信息
			ConfigUtils configUtils = new ConfigUtils();
			cnt = configUtils.loadDdls();
		} else if (oType.toUpperCase().equals("GET_DOC_XML")) {
			// 获取整个配置文件
			String xmlname = rv.getString("xmlname").replace("|", "/");
			IUpdateXml up = this.getUpdateXml(xmlname, pv.getStringValue());
			cnt = up.getDocXml();

			String[] names = UserConfig.filterXmlNameByJdbc(xmlname).split("\\|");
			String name = names[names.length - 1];
			byte[] buf = cnt.getBytes();

			response.setHeader("Location", name);
			response.setHeader("Cache-Control", "max-age=30");
			response.setHeader("Content-Disposition", "attachment; filename=" + name);
			// filename应该是编码后的(utf-8)
			response.setContentType("text/xml");
			response.setContentLength(buf.length);
			response.getOutputStream().write(buf);

			return;
		} else if (oType.toUpperCase().equals("IMPORT_XML")) {
			String sourceXmlFilePath = UPath.getPATH_UPLOAD() + rv.s("UP_NAME");

			String path = rv.s("path");
			String xmlname = rv.s("xmlname");

			IUpdateXml up = this.getUpdateXml(null, pv.getStringValue());
			JSONObject rst = up.importXml(path, xmlname, sourceXmlFilePath);

			cnt = rst.toString();

			this.setOutType(response, "json");
		} else if (oType.toUpperCase().equals("SAVE_JAVA")) {// 保存java代码

			JSONObject rst = this.saveJavaCode(rv);
			cnt = rst.toString();
			this.setOutType(response, "json");
		}
		this.outContent(request, response, cnt);
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
		String name1 = UPath.getConfigPath() + name;
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
			xml = UXml.asXmlAll(UXml.retDocument(name1));
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
