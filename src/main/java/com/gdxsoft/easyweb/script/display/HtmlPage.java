package com.gdxsoft.easyweb.script.display;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.acl.SampleAcl;
import com.gdxsoft.easyweb.debug.DebugInfo;
import com.gdxsoft.easyweb.debug.DebugRecord;
import com.gdxsoft.easyweb.debug.DebugRecords;
import com.gdxsoft.easyweb.script.html.HtmlDocument;

/**
 * 用于页面输出的类
 * 
 * @author Administrator
 * 
 */
public class HtmlPage {
	private HttpServletRequest _Request;
	private HttpSession _Session;
	private HttpServletResponse _Response;
	/**
	 * 是否显示Debug信息在页面
	 */
	private boolean _IsDebug; // 是否显示Debug信息在页面
	/**
	 * 不进行跟踪，用于后台调试
	 */
	private boolean _IsParaNotDebug; // 不进行跟踪
	/**
	 * 错误跳转页面
	 */
	private String _ErrPage; // 错误跳转页面

	private boolean _IsXhtml;
	private boolean _IsXml;

	private DebugRecord _Record;
	private DebugInfo _DebugInfo;
	private HtmlCreator _HtmlCreator;

	public HtmlPage(HttpServletRequest req, HttpSession session,
			HttpServletResponse response, boolean isXhtml) {
		this._Request = req;
		this._Session = session;
		this._Response = response;
		_IsXhtml = isXhtml;
	}

	void init() {
		String clientIp = _Request.getRemoteAddr();
		String cp = _Request.getContextPath();
		_ErrPage = cp + "/EWA_STYLE/cgi-bin/_er_/";// 错误跳转页面
		String debugIp = "-";

		boolean isDebug = false; // 是否显示跟踪信息在页面
		boolean paraNotDebug = false; // 是否参数指定了不跟踪
		String ewaDebugNo = _Request.getParameter("EWA_DEBUG_NO"); // 参数设置是否不跟踪

		if ((ewaDebugNo == null || !ewaDebugNo.equals("1") || _Request.getQueryString().indexOf("ewa.xml")>0)
				&& clientIp.equals(debugIp)) {
			// 本地ip可以页面显示跟踪信息
			isDebug = true;
		}
		this._IsDebug = isDebug;

		if (ewaDebugNo != null && ewaDebugNo.equals("1")) {
			paraNotDebug = true; // 参数指定了不进行跟踪
		}
		_IsParaNotDebug = paraNotDebug;
	}

	void creat() throws Exception {
		_HtmlCreator = new HtmlCreator();
		// 是否Xhtml
		_HtmlCreator.setIsXhtml(this._IsXhtml);

		_DebugInfo = new DebugInfo();

		// 记录debug到记录池中
		DebugRecords.setIsRecord(true);
		if (DebugRecords.isRecord() && !this._IsParaNotDebug) {
			_Record = new DebugRecord();
		}
		_HtmlCreator.init(this._Request, this._Session, this._Response);
		// 权限效验部分，可以按照接口重新定义效验接口
		if (_HtmlCreator.getAcl() == null) {
			IAcl acl = new SampleAcl();
			_HtmlCreator.setAcl(acl);
		}

		if (_HtmlCreator.getAjaxCallType() != null
				&& _HtmlCreator.getAjaxCallType().equalsIgnoreCase("XML")) {
			_IsXml = true;
		}

	}
	
	public HtmlDocument getDocument(){
		 return this._HtmlCreator.getDocument();
	}

	public void show() throws IOException {
		String debugStr;
		if (this._IsXml) {
			_Response.setContentType("text/xml");
		}
		DebugInfo di = _DebugInfo;
		try {
			this.creat();
		} catch (Exception e) {
			if (this._IsDebug) {
				if (this._IsXml || _HtmlCreator.getAjaxCallType() != null) {
					_Response.getWriter().append("初始化错误:" + e.getMessage());
				} else {// 页面显示跟踪信息
					_Response.getWriter().append("初始化错误:" + e.getMessage());
				}
			} else {
				gotoErrorPage(e.getMessage());
			}
			return;
		}

		di.setDebugFrames(_HtmlCreator.getDebugFrames());
		di.setRequestValue(_HtmlCreator.getRequestValue());
		try {
			_HtmlCreator.createPageHtml();

			String shtml = _HtmlCreator.getPageHtml();
			debugStr = di.getDebugPage();

			out(shtml);
			// debug信息，用于后台跟踪
			initRecord(debugStr, false);

			if (this._IsDebug) {
				if (this._IsXml || _HtmlCreator.getAjaxCallType() != null) {
					// System.out.println(hc.getDebugFrames().listDebugText());
				} else {// 页面显示跟踪信息
					out(debugStr);
				}
			}
		} catch (Exception e) {
			di.setE(e);
			debugStr = di.getExeptionPage();

			// //debug信息，用于后台跟踪
			initRecord(debugStr, true);
			if (_IsDebug) {
				out("<!--EWA_ERROR_INFOMATION-->");
				out(debugStr);
			} else {
				System.err.println(e.getMessage());
				gotoErrorPage(e.getMessage());
			}
		}
	}

	void initRecord(String debugStr, boolean isError) {
		if (_Record == null)
			return;
		String xmlName = _HtmlCreator.getRequestValue().getString("xmlname");
		_Record.setXmlName(xmlName);
		_Record.setItemName(_HtmlCreator.getRequestValue()
				.getString("itemname"));
		_Record.setParameters(_HtmlCreator.getRequestValue().listValues(false));
		_Record.setRunSeq(_HtmlCreator.getDebugFrames().listDebugHtml());
		_Record.setAll(debugStr);
		_Record.setIsError(isError);
		_Record.setRunTime(_HtmlCreator.getDebugFrames().getRunTime());
		DebugRecords.add(_Record);
	}

	private void gotoErrorPage(String errMsg) throws IOException {
		_Response.sendRedirect(this._ErrPage);
	}

	private void out(String s) throws IOException {
		_Response.getWriter().append(s);
	}

	/**
	 * @return the _HtmlCreator
	 */
	public HtmlCreator getHtmlCreator() {
		return _HtmlCreator;
	}
}
