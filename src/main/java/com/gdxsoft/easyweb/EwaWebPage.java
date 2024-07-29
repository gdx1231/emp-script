package com.gdxsoft.easyweb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.acl.SampleAcl;
import com.gdxsoft.easyweb.debug.DebugInfo;
import com.gdxsoft.easyweb.debug.DebugRecord;
import com.gdxsoft.easyweb.debug.DebugRecords;
import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.statusControl.StatusControl;
import com.gdxsoft.easyweb.utils.UPath;

public class EwaWebPage {
	private final HttpServletRequest _PageRequest;
	private final HttpServletResponse _PageResponse;
	private final HttpSession _PageSession;
	public static String ERR_PAGE = "/EWA_STYLE/cgi-bin/_er_/";
	private final DebugInfo _DebugInfo;
	private final HtmlCreator _HtmlCreator;

	private String _PageContentType;
	private String _PageContent;
	private String _PageDeubgInfo;
	private boolean _IsPageDebug; // 是否显示跟踪信息在页面
	private boolean _IsPageError;

	private final RequestValue _Rv;

	public EwaWebPage(HttpServletRequest req, HttpSession session, HttpServletResponse response) {
		this._PageRequest = req;
		this._PageSession = session;
		this._PageResponse = response;
		this._DebugInfo = new DebugInfo();
		_HtmlCreator = new HtmlCreator();
		_Rv = new RequestValue(req, session);
	}

	public void run() {
		String runType = this._Rv.getString("EWA_RUN_TYPE");
		if (runType != null && runType.trim().equals("SC")) {
			// ewa status control
			this.runEwaStatusControl();
		} else {
			this.runEwaScript();
		}
	}

	private void runEwaStatusControl() {
		StatusControl sc = new StatusControl();
		String scXmlName = this._Rv.getString("XMLNAME_SC");
		String scItemName = this._Rv.getString("ITEMNAME_SC");
		String scFormName = this._Rv.getString("FORMNAME_SC");
		try {
			sc.load(scXmlName, scItemName);
			String logicExp = sc.createFmLogicExp(scFormName);
			this._Rv.addValue("EWA_SC_LG", logicExp, PageValueTag.SYSTEM);
			this._Rv.addValue(FrameParameters.XMLNAME, sc.getCurFm().getXmlName());
			this._Rv.addValue(FrameParameters.ITEMNAME, sc.getCurFm().getItemName());
			this.runEwaScript();
		} catch (Exception e) {
			this._PageDeubgInfo = e.getMessage();
			this._IsPageError = true;
		}
	}

	private void runEwaScript() {
		String clientIp = this._Rv.s(RequestValue.SYS_REMOTEIP);
		String ewaDebugNo = this._Rv.getString(FrameParameters.EWA_DEBUG_NO);
		String q = _PageRequest.getQueryString();
		if (q == null)
			q = "";
		if ((ewaDebugNo != null && ewaDebugNo.equals("1"))
				// || this._Rv.getString(FrameParameters.EWA_P_BEHAVIOR) != null
				|| q.indexOf("ewa.xml") > 0) {
			// 参数设置不跟踪
			// 弹出窗体不显示Debug
			_IsPageDebug = false;
		} else {
			_IsPageDebug = UPath.getDebugIps().containsKey(clientIp);
		}
		boolean isXml = false;

		// 记录debug到记录池中
		DebugRecords.setIsRecord(true);
		DebugRecord record = null;
		if (DebugRecords.isRecord() && _IsPageDebug) {
			record = new DebugRecord();
		}
		String debugStr;

		try {
			_HtmlCreator.init(this._Rv, this._PageResponse);
			// 权限效验部分，可以按照接口重新定义效验接口
			if (_HtmlCreator.getAcl() == null) {
				IAcl acl = new SampleAcl();
				_HtmlCreator.setAcl(acl);
			}
			_HtmlCreator.createPageHtml();
			String ajaxType = _HtmlCreator.getAjaxCallType();
			if (ajaxType == null) {
				ajaxType = "";
			}

			if (ajaxType.equalsIgnoreCase("XML") || ajaxType.equalsIgnoreCase("XMLDATA")) {
				this._PageContentType = "text/xml";
				isXml = true;
			} else if (ajaxType.equalsIgnoreCase("JSON")) {
				// application/json
				if (this._Rv.getString(FrameParameters.EWA_JSON_NAME) == null) {
					this._PageContentType = "text/json";
				} else {
					this._PageContentType = "text/javascript";
				}
			} else if (ajaxType.equalsIgnoreCase("JSON_EXT") || ajaxType.equalsIgnoreCase("JSON_EXT1")) {
				this._PageContentType = "text/json";
			}

			this._PageContent = _HtmlCreator.getPageHtml();
//			int loc0=_PageContent.toString().indexOf("EWA_ITEMS_XML_");
//			int loc1=_PageContent.toString().indexOf("\";",loc0);
//			String tmp=_PageContent.toString().substring(loc0,loc1);
//			System.out.println(tmp);

			this._DebugInfo.setDebugFrames(_HtmlCreator.getDebugFrames());
			this._DebugInfo.setRequestValue(_HtmlCreator.getRequestValue());
			debugStr = this._DebugInfo.getDebugPage();

			// debug信息，用于后台跟踪
			if (record != null) {
				String xmlName = _HtmlCreator.getRequestValue().getString(FrameParameters.XMLNAME);
				record.setXmlName(xmlName);
				record.setItemName(_HtmlCreator.getRequestValue().getString(FrameParameters.ITEMNAME));
				record.setParameters(_HtmlCreator.getRequestValue().listValues(false));
				record.setRunSeq(_HtmlCreator.getDebugFrames().listDebugHtml());
				record.setAll(debugStr);
				record.setRunTime(_HtmlCreator.getDebugFrames().getRunTime());
				// DebugRecords.add(record);
			}
			if (_IsPageDebug) {
				if (!isXml || ajaxType.length() == 0 || "LF_RELOAD".equalsIgnoreCase(ajaxType)) {
					this._PageDeubgInfo = debugStr;
				}
			}
		} catch (Exception e) {
			handleError(e, record);
		}
	}

	private void handleError(Exception e, DebugRecord record) {
		this._IsPageError = true;
		DebugInfo di = this._DebugInfo;
		di.setDebugFrames(_HtmlCreator.getDebugFrames());
		di.setRequestValue(_HtmlCreator.getRequestValue());
		di.setE(e);
		String debugStr = di.getExeptionPage();

		// //debug信息，用于后台跟踪
		if (record != null) {
			String xmlName = _HtmlCreator.getRequestValue().getString(FrameParameters.XMLNAME);
			record.setXmlName(xmlName);
			record.setItemName(_HtmlCreator.getRequestValue().getString(FrameParameters.ITEMNAME));
			record.setParameters(_HtmlCreator.getRequestValue().listValues(false));
			record.setRunSeq(_HtmlCreator.getDebugFrames().listDebugHtml());
			record.setIsError(true);
			record.setAll(debugStr);
			record.setRunTime(_HtmlCreator.getDebugFrames().getRunTime());
			DebugRecords.add(record);
		}
		this._PageDeubgInfo = "<!--EWA_ERROR_INFOMATION-->" + debugStr;
	}

	/**
	 * @return the _PageRequest
	 */
	public HttpServletRequest getPageRequest() {
		return _PageRequest;
	}

	/**
	 * @return the _PageResponse
	 */
	public HttpServletResponse getPageResponse() {
		return _PageResponse;
	}

	/**
	 * @return the _PageSession
	 */
	public HttpSession getPageSession() {
		return _PageSession;
	}

	/**
	 * @return the _DebugInfo
	 */
	public DebugInfo getDebugInfo() {
		return _DebugInfo;
	}

	/**
	 * @return the _HtmlCreator
	 */
	public HtmlCreator getHtmlCreator() {
		return _HtmlCreator;
	}

	/**
	 * @return the _PageContentType
	 */
	public String getPageContentType() {
		return _PageContentType;
	}

	/**
	 * @return the _PageContent
	 */
	public String getPageContent() {
		return _PageContent;
	}

	/**
	 * @return the _PageDeubgInfo
	 */
	public String getPageDeubgInfo() {
		return _PageDeubgInfo;
	}

	/**
	 * @return the _IsPageDebug
	 */
	public boolean isPageDebug() {
		return _IsPageDebug;
	}

	/**
	 * @return the _IsPageError
	 */
	public boolean isPageError() {
		return _IsPageError;
	}
}
