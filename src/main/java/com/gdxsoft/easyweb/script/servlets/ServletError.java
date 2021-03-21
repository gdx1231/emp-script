package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ServletError extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -349402668535724826L;

	/**
	 * Constructor of the object.
	 */
	public ServletError() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");

		response.setHeader("Content-Type", "text/html;charset=UTF-8");
		response.setHeader("EWA", "V2.2;gdxsoft.com");

		PrintWriter o = response.getWriter();
		String ajax = request.getParameter("EWA_AJAX");

		RequestValue rv = new RequestValue(request, request.getSession());
		String ewa_path_root = rv.getString("RV_EWA_STYLE_PATH");
		String ewa_path = ewa_path_root;
		if (ewa_path_root == null) {
			ewa_path = "/EmpScriptV2/";
		}
		ewa_path = ewa_path + "/EWA_STYLE/";
		MStr sb = new MStr();
		sb.al("function msg(a){");
		sb.al("	var ok=_EWA_INFO_MSG['EWA.SYS.ERROR'];");
		sb.al("	if(a){document.title=ok;}");
		sb.al("	var s1=\"<div style='font-size:14px;magin:10px;padding:20px'>\"+ok+\"</div>\";");
		sb.al("	EWA.UI.Msg.ShowError(s1,ok);");
		sb.al("}");
		if (ajax == null || ajax.trim().length() == 0) {
			o.println("<!DOCTYPE html>");
			o.println("<html>");
			o.println("<head>");
			o.println("<meta http-equiv=\"Content-Type\" " + "content=\"text/html; charset=UTF-8\">");
			o.println("<script type='text/javascript' src='" + ewa_path + "/js/js_jquery/EWA_ALL.js'></script>");
			o.println("<script type='text/javascript' src='" + ewa_path_root
					+ "/thrid-party/jquery/jquery-1.12.3.min.js'></script>");
			o.println("<script type='text/javascript' src='../_re_/'></script>");
			o.println("<link rel='stylesheet' rev='stylesheet' href='" + ewa_path
					+ "/skins/default/css.css' type='text/css' />");

			o.println("<script type='text/javascript'>");
			o.println(sb.toString());
			o.println("</script>");
			o.println("</head>");
			o.println("<body onload=\"EWA.CP='" + request.getContextPath() + "';msg(true);\">");
			o.println("</body>");
			o.println("</html> ");
		} else {
			o.println(sb.toString());
			o.println("msg(false);");
		}
		o.flush();
		o.close();
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
	 * Returns information about the servlet, such as author, version, and copyright.
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
