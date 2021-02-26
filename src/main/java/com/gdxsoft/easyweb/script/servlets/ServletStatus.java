package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gdxsoft.easyweb.script.RequestValue;

public class ServletStatus extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -349402668535724826L;

	/**
	 * Constructor of the object.
	 */
	public ServletStatus() {
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");
		
		response.setHeader("Content-Type", "text/html;charset=UTF-8");
		response.setHeader("EWA", "V2.2;gdxsoft.com");
		
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		String statusValue = "";
		RequestValue rv = new RequestValue(request, session);
		String url = rv.getString("U").toString(); // url
		url = URLDecoder.decode(url, "utf-8");
		int loc = url.indexOf("&_r=");
		if (loc > 0) {
			url = url.substring(0, loc);
		}
		int code = url.hashCode();
		String t = rv.getString("T").toString(); // type
		String sessionName = t + code;
		if (t.equals("TREE") || t.equals("FRAMESET")) { // treeview
			if (rv.getString("M").equals("1")) { // save status
				String val = rv.getString("V").toString(); // value
				session.setAttribute(sessionName, val);
			} else {
				if (session.getAttribute(sessionName) != null) {
					statusValue = rv.getString(sessionName).toString();
				}
			}
		}
		out.println("_EWA_STATUS_VALUE=\"" + statusValue + "\";");
		out.flush();
		out.close();
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
