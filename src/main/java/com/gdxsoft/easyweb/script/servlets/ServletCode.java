package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gdxsoft.easyweb.script.*;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.script.userConfig.UserXItem;

// 用 ServletMain 的ewa_ajax=ValidCode进行替换
@Deprecated 
public class ServletCode extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -349402668535724826L;

	/**
	 * Constructor of the object.
	 */
	public ServletCode() {
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
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");
		HttpSession session = request.getSession();
		response.setContentType("image/jpeg");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		ServletOutputStream output = response.getOutputStream();

		RequestValue rv = new RequestValue(request);
		String xmlName = rv.s(FrameParameters.XMLNAME);
		String itemname = rv.s(FrameParameters.ITEMNAME);
		boolean is_ok = false;
		if (xmlName != null && itemname != null) {
			try {
				UserConfig uc = UserConfig.instance(xmlName, itemname, null);
				UserXItem uxi_vc = null;
				for (int i = 0; i < uc.getUserXItems().count(); i++) {
					UserXItem uxi = uc.getUserXItems().getItem(i);
					String tag = uxi.getItem("Tag").getItem(0).getItem(0);
					if (tag.trim().equalsIgnoreCase("valid")) {
						uxi_vc = uxi;
						break;
					}
				}

				int len = 4;
				String vcType = "Int";
				boolean isNumberCode = true;// 默认是数字验证码
				if (uxi_vc != null) {
					if (uxi_vc.testName("MaxMinLength")) {
						try {
							len = Integer.parseInt(uxi_vc.getSingleValue("MaxMinLength", "MaxLength"));
						} catch (Exception err) {
						}
					}
					if (uxi_vc.testName("DataItem")) {
						vcType = uxi_vc.getSingleValue("DataItem", "DataType");
					}

					isNumberCode = !vcType.equalsIgnoreCase("string");
				}

				if (len > 10) {
					len = 10;
				} else if (len < 4) {
					len = 4;
				}

				ValidCode1 vc = new ValidCode1(len, isNumberCode);
				ImageIO.write(vc.createCode(), "jpeg", output);
				 
				// save to session
				session.setAttribute(ValidCode.SESSION_NAME, vc.getRandomNumber());
				is_ok = true;

			} catch (Exception e) {
			}
		}

		if (!is_ok) { // 传统模式
			ValidCode vc = new ValidCode();
			ImageIO.write(vc.createCode(), "png", output);
			// save to session
			session.setAttribute(ValidCode.SESSION_NAME, vc.getRandomNumber());
		}
		output.flush();
		output.close();
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
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
