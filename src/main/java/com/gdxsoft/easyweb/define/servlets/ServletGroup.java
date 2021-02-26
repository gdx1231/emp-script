package com.gdxsoft.easyweb.define.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.datasource.ConnectionConfig;
import com.gdxsoft.easyweb.datasource.ConnectionConfigs;
import com.gdxsoft.easyweb.define.Dir;
import com.gdxsoft.easyweb.define.group.Exchange;
import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ServletGroup extends HttpServlet {

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
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.show(request, response);
	}

	private void show(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		RequestValue rv = new RequestValue(request, session);
		PageValue pv = rv.getPageValues().getPageValue("EWA_ADMIN_ID");
		// not login
		if (pv == null
				|| (pv.getPVTag() != PageValueTag.SESSION && pv.getPVTag() != PageValueTag.COOKIE_ENCYRPT)) {
			response.getWriter().println("deny! request login");
			return;
		}

		String oType = rv.getString("TYPE");
		if (oType == null) {
			return;
		}

		if (oType.equalsIgnoreCase("dl")) {
			String id = request.getParameter("id");
			String path = UPath.getGroupPath() + "/exports/" + id + ".zip/";
			FileInputStream fs = new FileInputStream(path);
			byte[] buf = new byte[fs.available()];
			fs.read(buf);
			fs.close();

			response.setHeader("Location", id + ".zip");
			response.setHeader("Cache-Control", "max-age=0");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ id + ".zip");
			// filename应该是编码后的(utf-8)
			response.setContentType("image/oct");
			response.setContentLength(buf.length);
			response.getOutputStream().write(buf);

			response.getOutputStream().close();
			return;
		}
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");

		MStr sbHead = new MStr();
		sbHead.al("<html>");
		sbHead.al("<head>");
		sbHead.al("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		sbHead.al("<title></title>");
		sbHead.al("<link rel=\"stylesheet\" rev=\"stylesheet\"  href=\""
				+ rv.s("rv_ewa_style_path")
				+ "/EWA_STYLE/skins/default/css.css\" type=\"text/css\" />");
		sbHead.al("<script type='text/javascript' src='"
				+ rv.s("rv_ewa_style_path") + "/EWA_STYLE/js/EWA.js'></script>");
		sbHead.al("<script type='text/javascript' src='"
				+ rv.s("rv_ewa_style_path")
				+ "/EWA_STYLE/js/EWA_UI.js'></script>");
		sbHead.al("<script type='text/javascript' src='"
				+ rv.s("rv_ewa_style_path")
				+ "/EWA_STYLE/js/EWA_FRAME.js'></script>");
		sbHead.al("</head>");
		sbHead.al("<body style=\"margin: 1px; overflow: auto\" onload='EWA.CP=\""
				+ request.getContextPath() + "\";EWA.LANG=\"zhcn\"'>");
		response.getWriter().println(sbHead.toString());

		if (oType.equalsIgnoreCase("install")) {
			String root = UPath.getGroupPath() + "/imports/";
			File fileRoot = new File(root);
			String[] ff = { ".zip" };
			Dir[] dirs = UFile.getFiles(root, ff);
			if (dirs == null) {
				response.getWriter().println(
						fileRoot.getAbsolutePath() + " 没有数据!");
				return;
			}
			MStr sb = new MStr();
			sb.al("<div align=center><h1>可用的组件" + fileRoot.getAbsolutePath()
					+ "</h1></div>");
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

				sb.al("<tr bgcolor=white><td height=24>" + f.getName()
						+ "</td><td>" + dateStr + "</td><td>" + f.length()
						/ 1024 + "k</td><td align=center>");
				sb.al(" <a href='./?TYPE=step1&id=" + name
						+ "'>安装</a></td></tr>");
			}
			sb.al("</table>");
			response.getWriter().println(sb.toString());
		} else if (oType.equalsIgnoreCase("step1")) {
			StringBuilder sb1 = new StringBuilder();
			sb1.append("<option></option>");
			ConnectionConfigs cfgs;
			MStr sb = new MStr();
			try {
				cfgs = ConnectionConfigs.instance();
				for (int i = 0; i < cfgs.size(); i++) {
					ConnectionConfig cfg = cfgs.getConfig(i);
					sb1.append("<option value='" + cfg.getName() + "'>"
							+ cfg.getName() + "  (" + cfg.getType() + ", "
							+ cfg.getConnectionString() + ")</option>");
				}
				sb.al("<div align=center><h1>设定导入参数</h1></div>");
				sb.al("<form action=\"./?TYPE=step2\" method=post>");
				sb.al("<table bgcolor='#cdcdcd' width=600 border=0 cellpadding=1 cellspacing=1 align=center>");
				sb.al("	<tr bgcolor=white><td>组件Id：</td>");
				sb.al("<td>" + request.getParameter("id")
						+ "<input type=hidden name=id value=\""
						+ request.getParameter("id") + "\">");
				sb.al("</td></tr>");
				sb.al("<tr bgcolor=white><td>目标数据源：</td><td>");

				sb.al("<select name=datasource>" + sb1.toString() + "</select>");
				sb.al("</td></tr><tr bgcolor=white><td>保存文件：（采用“|test|abc.xml”格式）</td>");
				sb.al("<td><input type=text name=xmlfile></td>");
				sb.al("	</tr>");
				sb.al("<tr bgcolor=white><td colspan=2>");
				sb.al("<input type=submit value='确定'>");
				sb.al("</td></tr></table></form>");
			} catch (ParserConfigurationException e) {
				sb.al(e.getMessage());
			} catch (SAXException e) {
				sb.al(e.getMessage());
			}
			response.getWriter().println(sb.toString());
		} else if (oType.equalsIgnoreCase("step2")) {
			MStr sb = new MStr();
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
				sb.al("<hr>\r\n导入到：" + g1.importCfgs(xmlfile));
			} catch (Exception e) {
				sb.al(e.getMessage());
			}
			response.getWriter().println("<pre>");
			response.getWriter().println(sb.toString());
			response.getWriter().println("</pre>");
		} else if (oType.equalsIgnoreCase("main")) {
			MStr sb = new MStr();
			sb.al("<script>");
			sb.al("function go(x,i){");
			sb.al("var u='../../../EWA_STYLE/cgi-bin/?XMLNAME='+x+'&ITEMNAME='+i;");
			sb.al("	window.frames[0].location.href=u;");
			sb.al("	}");
			sb.al("	function makeGroup(){");
			sb.al("		var u = './?step=make';");
			sb.al("		window.frames[0].location.href = u;");
			sb.al("	}");
			sb.al("	function installGroup(){");
			sb.al("	var u = './?type=install';");
			sb.al("		window.frames[0].location.href = u;");
			sb.al("	}");
			sb.al("	function res(){");
			sb.al("var u = './?type=res';");
			sb.al("	window.frames[0].location.href = u;");
			sb.al("	}");
			sb.al("	</script>");

			sb.al("<table border=0 width=100% height=100%>");
			sb.al("<tr>");
			sb.al("<td height=25 style='border-bottom:1px solid gray'>&nbsp;");
			sb.al("<button type=button onclick=\"go('|ewa|group.xml','ui')\">基本信息</button>&nbsp;");
			sb.al("<button type=button onclick=\"go('|ewa|group.xml','items')\">配置项</button>&nbsp;");
			sb.al("<button type=button onclick=\"go('|ewa|group.xml','tables')\">数据</button>&nbsp;");
			sb.al("<button type=button onclick=\"res();\">资源文件</button>&nbsp;");
			sb.al("<button type=button onclick=\"makeGroup();\">生成组件</button>");
			sb.al("	</td>");
			sb.al("	</tr>");
			sb.al("	<tr>");
			sb.al("	<td><iframe frameborder=0 width=100% height=100% src=\"\"></iframe></td>");
			sb.al("	</tr>");

			sb.al("	</table>");
			response.getWriter().println(sb.toString());
		} else if (oType.equalsIgnoreCase("make")) {
			String id = request.getParameter("id");
			Exchange ex = new Exchange(id);

			String s;
			String s2;
			try {
				s = ex.exportGroup();
				File f1=new File(s);
				  s2=f1.getAbsolutePath();
			} catch (Exception e) {
				s2= e.getMessage();
			}
			response.getWriter().println("<h1>生产组件结果</h1>");
			response.getWriter().println(
					"<h2><a target=_blank href='./?type=dl&id=" + id + "'>");
			response.getWriter().println(s2);
			response.getWriter().println("</a></h2>");
		}

		response.getWriter().println("</body></html>");
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}
}
