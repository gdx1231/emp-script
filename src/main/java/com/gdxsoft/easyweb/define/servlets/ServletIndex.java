/**
 * 
 */
package com.gdxsoft.easyweb.define.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;

/**
 * @author Administrator
 * 
 */
public class ServletIndex extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8377846637723766982L;

	/**
	 * 
	 */

	public ServletIndex() {
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
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		RequestValue rv = new RequestValue(request, session);

		String urlLogin = request.getContextPath()
				+ "/EWA_STYLE/cgi-bin/?XMLNAME=/ewa/m.xml&ITEMNAME=login";
		String urlDefine = request.getContextPath()
				+ "/EWA_STYLE/cgi-bin/?XMLNAME=/ewa/ewa.xml&ITEMNAME=index&EWA_DEBUG_NO=0";
		PageValue pv = rv.getPageValues().getPageValue("EWA_ADMIN_ID");
		// not login
		if (pv == null
				|| (pv.getPVTag() != PageValueTag.SESSION && pv.getPVTag() != PageValueTag.COOKIE_ENCYRPT)) {
			response.sendRedirect(urlLogin);
			return;
		} else {
			response.sendRedirect(urlDefine);
		}
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
