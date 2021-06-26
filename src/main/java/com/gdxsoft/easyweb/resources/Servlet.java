package com.gdxsoft.easyweb.resources;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7073908316136252285L;

	public Servlet() {
		super();
	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		String path = pathInfo;

		Resource r = Resources.getResource(path);
		response.setStatus(r.getStatus());
		if (r.getStatus() != 200) {
			return;
		}

		response.setContentType(r.getType());
		response.addHeader("cache-control", "max-age=86400");
		response.addHeader("x-emp-script-static", r.getPath());
		if (r.isBinary()) {
			response.getOutputStream().write(r.getBuffer());
		} else {
			response.setCharacterEncoding("utf-8");
			response.getWriter().print(r.getContent());
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	/**
	 * Returns information about the servlet, such as author, version, and copyright.
	 * 
	 * @return String information about this servlet
	 */
	public String getServletInfo() {
		return "emp-scrip-static";
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		super.init();
	}

	public void destroy() {
		super.destroy();
	}
}
