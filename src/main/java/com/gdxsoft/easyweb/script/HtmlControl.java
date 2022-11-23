package com.gdxsoft.easyweb.script;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.acl.IAcl;
import com.gdxsoft.easyweb.acl.SampleAcl;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.debug.DebugInfo;
import com.gdxsoft.easyweb.debug.DebugRecord;
import com.gdxsoft.easyweb.debug.DebugRecords;
import com.gdxsoft.easyweb.script.display.HtmlCreator;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.servlets.FileOut;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MList;

public class HtmlControl {
	private static Logger LOGGER = LoggerFactory.getLogger(HtmlControl.class);
	private boolean isError = false;
	private String Html;
	private String _Title;
	private RequestValue _Rv;

	private String _FrameUnidPrefix;
	private HtmlCreator _HtmlCreator;
	private DebugInfo _DebugInfo; // 执行过程

	private boolean skipAcl;

	/**
	 * 获取执行过程
	 * 
	 * @return
	 */
	public DebugInfo getDebugInfo() {
		return _DebugInfo;
	}

	/**
	 * 获取HtmlCreator;
	 * 
	 * @return
	 */
	public HtmlCreator getHtmlCreator() {
		return _HtmlCreator;
	}

	/**
	 * 获取最后一个表
	 * 
	 * @return
	 */
	public DTTable getLastTable() {
		if (this._HtmlCreator == null) {
			return null;
		}
		MList tbList = this._HtmlCreator.getHtmlClass().getItemValues().getDTTables();
		return (DTTable) tbList.getLast();
	}

	/**
	 * 获取所有执行的表
	 * 
	 * @return
	 */
	public DTTable[] getTables() {
		if (this._HtmlCreator == null) {
			return null;
		}
		MList tbList = this._HtmlCreator.getHtmlClass().getItemValues().getDTTables();
		DTTable[] tbs = new DTTable[tbList.size()];
		for (int i = 0; i < tbList.size(); i++) {
			tbs[i] = (DTTable) tbList.get(i);
		}
		return tbs;
	}

	/**
	 * 获取 配置项的 frame_unid
	 * 
	 * @return
	 */
	public String getFrameUnid() {
		return this.getHtmlCreator().getHtmlClass().getSysParas().getFrameUnid();
	}

	/**
	 * FrameUnid 前缀修正名称，避免同一个对象多次调用
	 * 
	 * @return
	 */
	public String getFrameUnidPrefix() {
		return _FrameUnidPrefix;
	}

	/**
	 * FrameUnid 前缀修正名称，避免同一个对象多次调用
	 * 
	 * @param frameUnidPrefix
	 */
	public void setFrameUnidPrefix(String frameUnidPrefix) {
		this._FrameUnidPrefix = frameUnidPrefix;
	}

	/**
	 * 获取参数对象
	 * 
	 * @return the _Rv
	 */
	public RequestValue getRequestValue() {
		return _Rv;
	}

	/**
	 * 获取文档的Title
	 * 
	 * @return the _Title
	 */
	public String getTitle() {
		return _Title;
	}

	/**
	 * 获取ListFrame分页
	 * 
	 * @return
	 */
	public PageSplit getPageSplit() {

		try {
			return this._HtmlCreator.getFrame().getPageSplit();
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * 初始化配置
	 * 
	 * @param xmlName
	 * @param itemName
	 * @param paras
	 * @param rv
	 * @param response
	 */
	public void init(String xmlName, String itemName, String paras, RequestValue rv, HttpServletResponse response) {
		HtmlCreator hc = new HtmlCreator();
		try {
			hc.init(xmlName, itemName, paras, rv, response);
			this.initHtmlCreator(hc);
		} catch (Exception e) {
			isError = true;
			LOGGER.error(e.getLocalizedMessage());
		}

	}

	/**
	 * 初始化配置
	 * 
	 * @param xmlName
	 * @param itemName
	 * @param paras
	 * @param request
	 * @param session
	 * @param response
	 */
	public void init(String xmlName, String itemName, String paras, HttpServletRequest request, HttpSession session,
			HttpServletResponse response) {
		HtmlCreator hc = new HtmlCreator();
		try {
			hc.init(xmlName, itemName, paras, request, session, response);
			this.initHtmlCreator(hc);
		} catch (Exception e) {
			isError = true;
			LOGGER.error(e.getLocalizedMessage());
		}

	}

	/**
	 * 初始化配置
	 * 
	 * @param xmlName
	 * @param itemName
	 * @param paras
	 * @param request
	 * @param session
	 * @param response
	 */
	public void initHtmlCreator(HtmlCreator hc) {
		StringBuilder sb = new StringBuilder();

		// FrameUnid 前缀修正名称，避免同一个对象多次调用
		if (this._FrameUnidPrefix != null && this._FrameUnidPrefix.trim().length() > 0) {
			hc.getSysParas().setFrameUnidPrefix(_FrameUnidPrefix);
		}
		boolean isDebug = false; // 是否显示跟踪信息在页面
		boolean paraNotDebug = false; // 是否参数指定了不跟踪

		this._HtmlCreator = hc;
		boolean isXml = false;
		DebugInfo di = new DebugInfo();
		this._DebugInfo = di;
		// 记录debug到记录池中
		DebugRecords.setIsRecord(true);
		DebugRecord record = null;
		if (DebugRecords.isRecord() && !paraNotDebug) {
			record = new DebugRecord();
		}

		String debugStr;
		// 权限效验部分，可以按照接口重新定义效验接口
		if (this.isSkipAcl() || hc.getAcl() == null) {
			IAcl acl = new SampleAcl();
			hc.setAcl(acl);
		}
		hc.getAcl().setRequestValue(hc.getRequestValue());

		String ajaxType = hc.getAjaxCallType();
		try {

			if ("XML".equalsIgnoreCase(ajaxType)) {
				if (hc.getHtmlClass().getResponse() != null) {
					hc.getHtmlClass().getResponse().setContentType("text/xml");
				}
				isXml = true;
			}

			hc.createPageHtml();

			// 输出验证码图片
			if ("ValidCode".equalsIgnoreCase(ajaxType)) {
				BufferedImage image = hc.getValidCode();
				FileOut fo = new FileOut(hc.getRequestValue().getRequest(), hc.getHtmlClass().getResponse());
				fo.outBufferedImage(image);

				return;
			}

			sb.append(hc.getPageHtml());

			this._Title = hc.getDocument().getTitle();
			this._Rv = hc.getRequestValue();

			di.setDebugFrames(hc.getDebugFrames());
			di.setRequestValue(hc.getRequestValue());
			debugStr = di.getDebugPage();

			// debug信息，用于后台跟踪
			if (record != null) {
				record.setXmlName(hc.getUserConfig().getXmlName());
				record.setItemName(hc.getUserConfig().getItemName());
				record.setParameters(hc.getRequestValue().listValues(false));
				record.setRunSeq(hc.getDebugFrames().listDebugHtml());
				record.setAll(debugStr);
				record.setRunTime(hc.getDebugFrames().getRunTime());
				// DebugRecords.add(record);
			}
			if (isDebug) {
				if (isXml || hc.getAjaxCallType() != null) {
					// System.out.println(hc.getDebugFrames().listDebugText());
				} else {// 页面显示跟踪信息
					sb.append(debugStr);
				}
			}

			String debugKey = hc.getRequestValue().s(FrameParameters.EWA_DEBUG_KEY);
			String frameUnid = this._HtmlCreator.getHtmlClass().getSysParas().getFrameUnid();
			if (frameUnid != null && frameUnid.equals(debugKey)) {
				// 记录到数据库中
				di.recordToHsql();
			}

		} catch (Exception e) {
			this.isError = true;
			di.setDebugFrames(hc.getDebugFrames());
			di.setRequestValue(hc.getRequestValue());
			di.setE(e);
			debugStr = di.getExeptionPage();

			// //debug信息，用于后台跟踪
			if (record != null) {
				record.setXmlName(hc.getUserConfig().getXmlName());
				record.setItemName(hc.getUserConfig().getItemName());
				record.setParameters(hc.getRequestValue().listValues(false));
				record.setRunSeq(hc.getDebugFrames().listDebugHtml());
				record.setIsError(true);
				record.setAll(debugStr);
				record.setRunTime(hc.getDebugFrames().getRunTime());
				DebugRecords.add(record);
			}
			if (isDebug) {
				sb.append("<!--EWA_ERROR_INFOMATION-->");
				sb.append(debugStr);
			} else {
				sb.append("系统执行错误");
				System.err.println(di.getExeptionPageText());
			}
			// 记录到数据库中
			di.recordToHsql();
		}
		this.Html = sb.toString();
	}

	/**
	 * @return the isError
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * 只显示对象内容，移除了头部和尾部信息
	 * 
	 * @return the html
	 */
	public String getHtml() {
		if (this.isError) {
			return Utils.textToInputValue(this.Html);
		}

		// 当输出 ValidCode时
		if (this.Html == null) {
			return null;
		}

		int m0 = this.Html.indexOf("<!--INC_TOP-->");
		String tmp = Html;
		if (m0 >= 0) {
			tmp = Html.substring(m0);
		}
		int m1 = tmp.lastIndexOf("</body></html>");
		if (m1 > 0) {
			tmp = tmp.substring(0, m1);
		}
		return tmp;

	}

	/**
	 * 最小化返回Frame数据，只有内容，没有外部框架，包含脚本
	 * 
	 * @return
	 */
	public String getHtmlMin() throws Exception {
		if (this.isError) {
			return this.Html;
		}
		String min = this.getHtmlCreator().getPageMin();
		if (this._FrameUnidPrefix != null && this._FrameUnidPrefix.trim().length() > 0) {
			String unid = this.getFrameUnid();
			min = min.replace(unid, this._FrameUnidPrefix.trim() + unid);
		}
		return min;

	}

	/**
	 * 最小化返回Frame数据，只有内容，没有外部框架和脚本
	 * 
	 * @return
	 */
	public String getHtmlFrameMin() {
		if (this.isError) {
			return this.Html;
		}
		int m0 = this.Html.toUpperCase().indexOf("<!-- START: FRAME CONTENT-->");
		String tmp = Html;
		if (m0 < 0) {
			m0 = this.Html.indexOf("<!-- ListFrame START -->");
		}
		if (m0 >= 0) {
			tmp = Html.substring(m0);
		}
		int m1 = tmp.toUpperCase().lastIndexOf("<!-- END: FRAME CONTENT-->");
		if (m1 < 0) {
			m1 = tmp.lastIndexOf("<!-- ListFrame END -->");
		}
		if (m1 > 0) {
			tmp = tmp.substring(0, m1);
		}

		if (this._FrameUnidPrefix != null && this._FrameUnidPrefix.trim().length() > 0) {
			String unid = this.getRequestValue().getString("SYS_FRAME_UNID");
			tmp = tmp.replace(unid, this._FrameUnidPrefix.trim() + unid);
		}
		return tmp;

	}

	public String getAllHtml() {
		return this.Html;
	}

	public boolean isSkipAcl() {
		return skipAcl;
	}

	public void setSkipAcl(boolean skipAcl) {
		this.skipAcl = skipAcl;
	}

}
