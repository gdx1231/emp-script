package com.gdxsoft.easyweb.script.servlets;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdxsoft.easyweb.script.PageValue;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.UNet;
import com.gdxsoft.easyweb.utils.UPath;

/**
 * 跨域请求
 * 
 * @author admin
 *
 */
public class ServletCrossDomain extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -349402668535724826L;

	/**
	 * Constructor of the object.
	 */
	public ServletCrossDomain() {
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

	private void outContent(HttpServletRequest request,
			HttpServletResponse response, String cnt) throws ServletException,
			IOException {
		GZipOut out = new GZipOut(request, response);
		out.outContent(cnt);
	}

	/**
	 * 检查跨域是否在规定范围内（ewa_conf.xml 的 initparas 中定义 CROSS_DOMAIN）
	 * 
	 * @param url
	 * @return
	 */
	private String isValidDomain(String url) {
		String CROSS_DOMAIN = UPath.getInitPara("CROSS_DOMAIN");
		if (CROSS_DOMAIN == null || CROSS_DOMAIN.trim().length() == 0) {
			return "请在 ewa_conf.xml 的 initparas 中定义 CROSS_DOMAIN";
		}

		String[] s1 = CROSS_DOMAIN.split(",");
		for (int i = 0; i < s1.length; i++) {
			String u1 = s1[i].trim().toLowerCase();

			if (u1.indexOf("http://") == 0
					&& url.toLowerCase().indexOf(u1) == 0) {
				// 合法
				return null;
			}
		}
		return "不是合法域名(" + url.replace("<", "&lt;") + ")";
	}

	private void show(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Type", "text/html;charset=UTF-8");
		response.setHeader("X-EWA-ENGIN", "V2.2;gdxsoft.com;GZIP");
		RequestValue rv = new RequestValue(request, request.getSession());

		// 跨域请求地址
		String url = rv.s("EWA_Cd_URL");

		if (url == null || url.trim().length() == 0) {
			this.outContent(request, response,
					"{rst:false, msg:'no ewa_cd_url'}");
			return;
		}

		if (url.toLowerCase().indexOf("http://") != 0) {
			this.outContent(request, response,
					"{rst:false, msg:'ewa_cc_url not http start'}");
			return;
		}

		String chkRst = isValidDomain(url);
		if (chkRst != null) {
			this.outContent(request, response, "{rst:false, msg:'" + chkRst
					+ "'}");
			return;
		}

		// 登录地址
		String urlLogin = rv.s("EWA_Cd_LOGIN");
		if (urlLogin != null) {
			String chkRst1 = isValidDomain(urlLogin);
			if (chkRst1 != null) {
				this.outContent(request, response, "{rst:false, msg:'"
						+ chkRst1 + "'}");
				return;
			}
		}
		HashMap<String, String> map1 = new HashMap<String, String>();
		for (int i = 0; i < rv.getPageValues().getFormValues().getCount(); i++) {
			String key = rv.getPageValues().getFormValues().getKey(i)
					.toString();
			PageValue val = (PageValue) rv.getPageValues().getFormValues()
					.getByIndex(i);
			map1.put(key, val.getValue().toString());

		}
		// 去除Query
		for (int i = 0; i < rv.getPageValues().getQueryValues().getCount(); i++) {
			String key = rv.getPageValues().getFormValues().getKey(i)
					.toString();
			PageValue val = (PageValue) rv.getPageValues().getFormValues()
					.getByIndex(i);
			if (map1.containsKey(key)) {
				String thisval = val.getValue().toString();
				String prevVal = map1.get(key);
				if (thisval.equals(prevVal)) {
					map1.remove(key);
				}
			}
		}

		UNet net2 = new UNet();
		if (urlLogin != null && urlLogin.toLowerCase().startsWith("http://")) {
			String cookies = null;
			String seesionName = "app_cross_domain_cookie/"
					+ urlLogin.hashCode();
			if (request.getSession().getAttribute(seesionName) == null) {
				// 没有登录过
				UNet net = new UNet();
				String rst;
				if (map1.size() > 0) {
					rst = net.doPost_old(urlLogin, map1);
				} else {
					rst = net.doGet(urlLogin);
				}
				cookies = net.getCookies();

				request.getSession().setAttribute(seesionName, cookies);
				response.setHeader("X-EWA-CROSS-DOMAIN-LOGIN", urlLogin);
			} else {
				// 已经登录过
				cookies = request.getSession().getAttribute(seesionName)
						.toString();
			}
			if (cookies != null && cookies.trim().length() > 0) {
				net2.setCookie(cookies);
				response.setHeader("X-EWA-CROSS-DOMAIN-NAME", seesionName);
				response.setHeader("X-EWA-CROSS-DOMAIN-COOKIES", cookies);
			}

		}
		String encode = rv.s("EWA_CD_ENCODE");
		if (encode != null) {
			net2.setEncode(encode);
		}
		response.setHeader("X-EWA-CROSS-DOMAIN", url);

		String rst2;
		if (map1.size() > 0) {
			rst2 = net2.doPost_old(url, map1);
		} else {
			rst2 = net2.doGet(url);
		}
		int locbase = rst2.indexOf("<head");
		if (locbase > 0) {
			java.net.URI u = java.net.URI.create(url);
			String base_u = u.getScheme() + "://" + u.getHost() + "/";
			rst2 = "<base href='" + base_u + "' />" + rst2;

		}
		this.outContent(request, response, rst2);
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
