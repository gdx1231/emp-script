/**
 * 
 */
package com.gdxsoft.easyweb.define.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfDefine;
import com.gdxsoft.easyweb.define.UpdateWorkflow;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.Workflow.EwaWfMain;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * @author Administrator
 * 
 */
public class ServletWorkflow extends HttpServlet {
	private static Logger LOGGER = LoggerFactory.getLogger(ServletWorkflow.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 8377846637723766982L;

	/**
	 * 
	 */

	public ServletWorkflow() {
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
			LOGGER.info("Not allow define", request == null ? "NO request?" : request.getRequestURI());
			return;
		}

		request.setCharacterEncoding("UTF-8");
		RequestValue rv = new RequestValue(request, request.getSession());
		UpdateWorkflow u = new UpdateWorkflow();
		String s = "";
		String wfType = rv.getString(FrameParameters.EWA_WF_TYPE);
		if (wfType == null) {
			wfType = "";
		}
		wfType = wfType.trim().toLowerCase();
		JSONObject obj = new JSONObject();

		if (wfType.equals("cnns")) {
			s = u.updateCnns(rv);
		} else if (wfType.equals("units")) {
			s = u.updateUnits(rv);
		} else if (wfType.equals("gunid")) {
			int num = 20;
			try {
				num = rv.getInt("num");
			} catch (Exception e1) {

			}
			if (num <= 0) {
				num = 20;
			}
			MStr s1 = new MStr();
			s1.a("[");
			for (int i = 0; i < num; i++) {
				if (i > 0) {
					s1.a(",");
				}
				s1.al("'" + Utils.getGuid() + "'");
			}
			s1.a("]");
			s = s1.toString();
		} else if (wfType.equals("all")) {
			s = u.updateUnits(rv);
			s = u.updateCnns(rv);
		} else if (wfType.equals("get")) {
			s = u.getUnitsCnns(rv);
		} else if (wfType.equals("ins_post")) { // 用户提交
			EwaWfMain main = new EwaWfMain();
			try {
				String wfId = rv.getString("WF_ID");
				main.initDlv(wfId, rv);
				main.doPost(rv);
				obj.put("RST", true);
				s = obj.toString();
			} catch (Exception e) {
				try {
					obj.put("RST", false);
					obj.put("ERR", e.getMessage());
					s = obj.toString();
				} catch (JSONException e1) {
					s = "{\"RST\":false,ERR:\"???\"}";
				}

			}
		} else if (wfType.equals("ins_get")) { // 用户提交
			EwaWfMain main = new EwaWfMain();

			try {
				String wfId = rv.getString("WF_ID");
				main.initDlv(wfId, rv);
				s = main.doGetStatusDlv(rv);
			} catch (Exception e) {
				try {
					obj.put("RST", false);
					obj.put("ERR", e.getMessage());
					s = obj.toString();
				} catch (JSONException e1) {
					s = "{\"RST\":false,ERR:\"???\"}";
				}

			}
		} else {
			s = "{\"RST\":false,\"ERR\":\"type error\"}";
		}
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(s);

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
