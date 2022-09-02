/**
 * 
 */
package com.gdxsoft.easyweb.define.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfAdmins;
import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.define.EwaConfHelpHSqlServer;
import com.gdxsoft.easyweb.utils.UUrl;

/**
 * @author Administrator
 * 
 */
public class ServletIndex extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletIndex.class);

	

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
		
		EwaConfHelpHSqlServer.getInstance();
		ConfAdmins.getInstance().getLst();
		UUrl uu = new UUrl(request);
		request.setCharacterEncoding("UTF-8");
		// version =3 menus call by EwaDefineMenu
		String urlDefine = uu.getRoot0() + request.getContextPath()
				+ "/EWA_STYLE/cgi-bin/?XMLNAME=/ewa/ewa.xml&ITEMNAME=index&version=3&EWA_DEBUG_NO=0";

		response.sendRedirect(urlDefine);
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
