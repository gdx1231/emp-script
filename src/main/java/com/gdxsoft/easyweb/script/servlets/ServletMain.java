package com.gdxsoft.easyweb.script.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.EwaWebPage;
import com.gdxsoft.easyweb.conf.ConfAdmins;
import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.conf.ConfSecurities;
import com.gdxsoft.easyweb.define.EwaConfHelpHSqlServer;
import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.template.EwaConfig;
import com.gdxsoft.easyweb.script.template.Skin;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class ServletMain extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletMain.class);

	// web.xml
	// <servlet>
	// <servlet-name>EwaMain</servlet-name>
	// <servlet-class>com.gdxsoft.easyweb.script.servlets.ServletMain</servlet-class>
	// <load-on-startup>1</load-on-startup>
	// </servlet>
	static {
		// The load-on-startup element indicates that this servlet should be loaded (instantiated and have its init()
		// called) on the startup of the web application. The optional contents of these element must be an integer
		// indicating the order in which the servlet should be loaded. If the value is a negative integer, or the
		// element is not present, the container is free to load the servlet whenever it chooses. If the value is a
		// positive integer or 0, the container must load and initialize the servlet as the application is deployed. The
		// container must guarantee that servlets marked with lower integers are loaded before servlets marked with
		// higher integers. The container may choose the order of loading of servlets with the same load-on-start-up
		// value.
		try {
			EwaConfig.instance();
			LOGGER.info("EwaConfig instance");
		} catch (Exception e) {
			LOGGER.error("EwaConfig instance,", e.getMessage());
		}

		try {
			Skin.instance();
			LOGGER.info("Skin instance");
		} catch (Exception e1) {
			LOGGER.error("Skin instance,", e1.getMessage());
		}

		try {
			EwaGlobals.instance();
			LOGGER.info("EwaGlobals instance");
		} catch (Exception e) {
			LOGGER.error("EwaGlobals instance,", e.getMessage());
		}
		try {
			ConfSecurities inst = ConfSecurities.getInstance();
			LOGGER.info("ConfSecurities inst");
			if (inst == null) {
				LOGGER.warn("The security not defined in ewa_conf.xml");
			}
		} catch (Exception e) {
			LOGGER.error("ConfSecurities instance,", e.getMessage());
		}
		try {
			if (ConfDefine.isAllowDefine()) {
				// start the EWA_DEFINE instances
				EwaConfHelpHSqlServer.getInstance();
				// admins info, random password
				ConfAdmins.getInstance().getLst();
			}
		} catch (Exception err) {
			LOGGER.error("define ServletIndex static,", err.getMessage());
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -349402668535724826L;

	/**
	 * Constructor of the object.
	 */
	public ServletMain() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getMethod();
		if ("PATCH".equalsIgnoreCase(method)) {
			this.doPatch(req, resp);
		} else {
			super.service(req, resp);
		}
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
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
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
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.show(request, response);
	}

	public void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.show(request, response);
	}

	public void outContent(HttpServletRequest request, HttpServletResponse response, String cnt)
			throws ServletException, IOException {
		GZipOut out = new GZipOut(request, response);
		out.outContent(cnt);
	}

	public void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Type", "text/html;charset=UTF-8");
		response.setHeader("X-EWA-ENGIN", "emp-script");
		HttpSession session = request.getSession();
		EwaWebPage p = new EwaWebPage(request, session, response);
		p.run();
		MStr cnt = new MStr();

		RequestValue rv = p.getHtmlCreator().getRequestValue();
		if (p.isPageError()) { // 页面执行错误
			// 记录到数据库中
			p.getDebugInfo().recordToHsql();

			if (p.isPageDebug()) { // 是否Debug状态
				cnt.a(p.getPageDeubgInfo());
			} else {
				// 跳转到错误页面
				String u = request.getContextPath() + EwaWebPage.ERR_PAGE;
				String ajax = rv.s("EWA_AJAX");
				if (ajax != null && ajax.trim().length() > 0) {
					if (u.indexOf("?") == -1) {
						u += "?EWA_AJAX=" + ajax;
					} else {
						u += "&EWA_AJAX=" + ajax;
					}
				}
				// 输出执行错误的 debug信息
				System.err.println(p.getDebugInfo().getExeptionPageText());
				response.sendRedirect(u);
			}
			this.outContent(request, response, cnt.toString());
			return;
		}

		String debugKey = rv.s("EWA_DEBUG_KEY");
		String frameUnid = p.getHtmlCreator().getHtmlClass().getSysParas().getFrameUnid();
		if (frameUnid != null && frameUnid.equals(debugKey)) {
			p.getDebugInfo().recordToHsql();
		}

		String ct = p.getPageContentType();
		if (ct != null) {
			response.setContentType(ct);
		}
		// 输出Html页面内容
		String cnt1 = p.getPageContent();

		// 输出二进制内容，例如图片、pdf文件等
		if ("download-inline".equalsIgnoreCase(rv.s("ewa_ajax"))) {
			this.outImage(rv, response, p);
			return;
		}

		if ("download".equalsIgnoreCase(rv.s("ewa_ajax"))) {
			// the download saved file name's field name
			String downloadNameField = rv.s("EWA_DOWNLOAD_FIELD");
			String downloadFile = null;
			if (StringUtils.isNotBlank(downloadNameField)) {
				String name = p.getHtmlCreator().getValueFromFrameTables(downloadNameField);
				if (StringUtils.isBlank(name)) {
					downloadFile = "invalid_parameter";
				} else {
					downloadFile = name;
				}
			}
			this.downloadFile(rv, response, p, downloadFile);
			return;
		}
		String ewaPath = rv.s("rv_ewa_style_path");
		if (StringUtils.isBlank(ewaPath)) {
			ewaPath = "/EmpScriptV2"; // default static url prefix
		}

		if (rv.s("EWA_JS_DEBUG") != null && cnt1.indexOf("<script") > 0 && cnt1.indexOf("ewa.min.js") > 0) {
			cnt1 = cnt1.replace("ewa.min.js", "fas.js");
			String debugScripts = this.createJsDebug(ewaPath);
			cnt1 = cnt1.replace("</head>", debugScripts);
		}

		cnt.a(cnt1);
		if (p.isPageDebug()) {// 是否Debug状态
			if ((ct != null && ct.equals("text/xml")) || p.getHtmlCreator().getAjaxCallType() != null) {
				// 不输出
			} else {
				// 输出Debug内容
				cnt.a(p.getPageDeubgInfo());
			}
		}
		this.outContent(request, response, cnt.toString());
	}

	/**
	 * Download the file from the frame item
	 * @param rv
	 * @param response
	 * @param p
	 * @param downloadName the saved file name
	 */
	private void downloadFile(RequestValue rv, HttpServletResponse response, EwaWebPage p, String downloadName) {
		String fileStr = p.getPageContent();
		File image = new File(fileStr);

		FileOut fo = new FileOut(rv.getRequest(), response);
		fo.initFile(image);

		fo.download(downloadName);
	}

	private void outImage(RequestValue rv, HttpServletResponse response, EwaWebPage p) {
		String fileStr = p.getPageContent();
		File image = new File(fileStr);
		String resize = rv.s("ewa_image_resize");
		if (StringUtils.isNotBlank(resize)) {
			File imgSize = FileOut.getImageResizedFile(image, resize);
			if (imgSize.exists()) {
				image = imgSize;
			} else {
				// 404
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		}
		long oneWeek = 604800L; // seconds

		FileOut fo = new FileOut(rv.getRequest(), response);
		fo.initFile(image);

		fo.outFileBytesInline(true, oneWeek);
	}

	private String createJsDebug(String ewaPath) {
		StringBuilder sbDebugJs = new StringBuilder();
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_01AjaxClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_04XmlClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_05UrlClass.js'></script>\n");

		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_00.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_02JSONClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_03DateClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_07ImageClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_06TransClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_99MqeClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/core/EWA_08WebSocket.js'></script>\n");

		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_00UI.js'></script>\n");

		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_03FrameClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_00FrameCommonClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_50UI_BoxClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_04FrameListClass.js'></script>\n");

		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_01FrameCommonItems.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_04FrameListFrameResources.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_02FrameResoures.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_06FrameMultiClass.js'></script>\n");

		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_31Html5TakePhotoClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_52UI_ComplexClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_99CombineClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_05FrameListFrame_CellResizeClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_53UI_ADListClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_07FrameTreeClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_51UI_LeftClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_03FrameMapToClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_03FrameItemClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/frames/EWA_30Html5UploadClass.js'></script>\n");

		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_12UI_PicViewClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_03UI_TipClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_071UI_CalendarYear.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_11UI_MapClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_01UI_Move.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_09UI_FlowChartClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_07CalendarClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_04UI_LinkClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_50Behavior.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_08UI_MsgClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_01UI_ExcelClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_10UI_HtmlEditor.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_06UI_DialogNewClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_UI_NewFunc.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_14UI_Dock.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_13UI_H5FrameSet.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_071UI_CalendarYearGroup.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_02UI_MenuClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_06UI_DialogClass.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/ui/EWA_05UI_TabsClass.js'></script>\n");

		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/misc/EWA_WF.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/misc/EWA_MiscPasteTool.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/misc/html_walker.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/misc/html_walker_odt.js'></script>\n");
		sbDebugJs.append("<script type='text/javascript' src='" + ewaPath
				+ "/EWA_STYLE/js/source/src/misc/EWA_UT_TRANS.js'></script>\n");

		sbDebugJs.append("</head>");

		return sbDebugJs.toString();
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

}
