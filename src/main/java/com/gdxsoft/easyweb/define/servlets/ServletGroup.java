package com.gdxsoft.easyweb.define.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.define.DefineAcl;
import com.gdxsoft.easyweb.define.group.Exchange;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.UPath;
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

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ConfDefine.isAllowDefine()) {
			response.setStatus(404);
			LOGGER.info("Not allow define", request == null ? "?not request?" : request.getRequestURI());
			return;
		}

		RequestValue rv = new RequestValue(request);
		DefineAcl acl = new DefineAcl();
		acl.setRequestValue(rv);

		if (!acl.canRun()) { // not login
			response.getWriter().println("deny! request login");
			return;
		}

		String oType = rv.getString("TYPE");
		if (oType == null) {
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

		String ewaPath = rv.s("rv_ewa_style_path");
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
