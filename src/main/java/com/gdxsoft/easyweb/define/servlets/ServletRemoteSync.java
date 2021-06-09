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

import com.gdxsoft.easyweb.conf.ConfDefine;

/**
 * Synchronize the local server files to the remote server
 * servlet是线程不安全的 一般不要轻易定成员变量 ，用局部变量吧
 */
public class ServletRemoteSync extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletRemoteSync.class);

	private static final long serialVersionUID = 982L;

	public ServletRemoteSync() {
		super();
	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ConfDefine.isAllowDefine()) {
			response.setStatus(404);
			LOGGER.info("Not allow define", request == null ? "NO request?" : request.getRequestURI());
			return;
		}

		HandleRemoteSync h = new HandleRemoteSync();
		h.handleRemoteSync(request, response);
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
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
	}

}
