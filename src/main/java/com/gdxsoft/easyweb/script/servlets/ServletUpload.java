package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.uploader.Upload;
import com.gdxsoft.easyweb.uploader.UploadUtils;
import com.gdxsoft.easyweb.utils.UJSon;

@MultipartConfig(fileSizeThreshold = 10485760, maxFileSize = -1L, maxRequestSize = 2147483648L)
public class ServletUpload extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletUpload.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -2804223580338447121L;

	/**
	 * Constructor of the object.
	 */
	public ServletUpload() {
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
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		PrintWriter out = response.getWriter();
		JSONObject result = new JSONObject();

		RequestValue rv = new RequestValue(request, request.getSession());

		try {
			Upload up = UploadUtils.parseAndUpload(request, rv);
			String s = up.getAlFiles() != null ? up.createJSon() : "[]";
			out.println(s);
			response.setHeader("X-EWA_UP_RET", s);
		} catch (Exception e) {
			UJSon.rstSetFalse(result, e.getMessage());
			out.print(result);
			LOGGER.error(e.getMessage());
		}
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
