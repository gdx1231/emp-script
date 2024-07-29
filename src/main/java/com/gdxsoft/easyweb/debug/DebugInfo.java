package com.gdxsoft.easyweb.debug;

import com.gdxsoft.easyweb.cache.SqlCachedHsqldbImpl;
import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MStr;

public class DebugInfo {

	private Exception _E;
	private DebugFrames _DebugFrames;
	private RequestValue _RequestValue;

	public DebugInfo() {

	}

	/**
	 * 记录到 HSQL数据库中
	 */
	public void recordToHsql() {
		String xmlname = this._RequestValue.s(FrameParameters.XMLNAME);
		if (xmlname == null || xmlname.indexOf("ewa.xml") > 0) {
			return;
		}
		String ITEMNAME = this._RequestValue.s(FrameParameters.ITEMNAME);
		if ("FRAME_DEBUG.LF.M".equalsIgnoreCase(ITEMNAME) || "FRAME_DEBUG.F.NM".equalsIgnoreCase(ITEMNAME)) {
			return;
		}

		String name = "__D_HTML_XXXXXXXXXXXXX123123123XXXXXXXXXXXXXXXXX";
		StringBuilder sbCnt = new StringBuilder();
		sbCnt.append(this._RequestValue.listValuesHtml());
		sbCnt.append(this.getDebugInfo(true));
		this._RequestValue.addValue(name, sbCnt.toString());

		String name1 = "__D_HTML_XXXXXXXXXXXXX123123123XXXXXXXXXXXXXXXXX1";
		xmlname = UserConfig.filterXmlNameByJdbc(xmlname);
		this._RequestValue.addValue(name1, xmlname);

		StringBuilder sb = new StringBuilder();
		sb.append(
				"INSERT INTO FRAME_DEBUG(D_XMLNAME, D_ITEMNAME, D_DATE, D_HTML, D_IP, D_CGI, D_USER_AGENT, D_REFERER)");
		sb.append("VALUES(@");
		sb.append(name1);
		sb.append(", @ITEMNAME, @SYS_DATE, @");
		sb.append(name);
		sb.append(", @SYS_REMOTEIP, @SYS_REMOTE_URL, @SYS_USER_AGENT, @SYS_REMOTE_REFERER)");
		String sql = sb.toString();

		DataConnection cnn = new DataConnection();
		cnn.setConfigName(SqlCachedHsqldbImpl.CONN_STR);
		cnn.setRequestValue(_RequestValue);

		cnn.executeUpdate(sql);
		cnn.close();

		_RequestValue.getPageValues().remove(name);
		_RequestValue.getPageValues().remove(name1);
	}

	public String getExeptionPageText() {
		MStr builder = new MStr();
		builder.al(this.getExceptionInfo(false));
		builder.al(this.getRequestInfo(false));
		builder.al(this.getDebugInfo(false));
		return builder.toString();
	}

	public String getExeptionPage() {
		MStr ss = new MStr();
		ss.al(this.getExceptionInfo(true));
		ss.al(this.getRequestInfo(true));
		ss.al(this.getDebugInfo(true));
		return ss.toString();
	}

	public String getDebugPage() {
		String id = "DEBUG_" + Utils.randomStr(10);
		MStr sb = new MStr();
		sb.al("<div id='__EWA_DEBUG' class='EWA_DEBUG'><div style='font-size:9px;color:#dcdcdc' ");
		sb.al("onclick=\"var o1=document.getElementById('" + id + "');");
		sb.al("if(o1.style.display==''){o1.style.display='none'}else{o1.style.display=''};");
		sb.al("\">DEBUG INFO</div><div id='" + id + "' class='EWA_DEBUG_INFO' style='display:none'>");
		sb.al(this.getRequestInfo(true));
		sb.al(this.getDebugInfo(true));
		sb.al("</div></div>");
		return sb.toString();

	}

	public String getExceptionInfo(boolean isHtml) {
		if (_E == null)
			return "";
		String e = _E.toString();
		String info;
		if (!isHtml) {
			info = "Exception: " + e;
		} else {
			info = "<fieldset  style='font-size:12px;font-family:arial'><legend  >Exception info</legend>" + e
					+ "</fieldset>";
		}
		return info;
	}

	public String getDebugInfo(boolean isHtml) {
		if (_DebugFrames == null)
			return "";
		String info;
		if (!isHtml) {
			info = _DebugFrames.listDebugText();
		} else {
			info = "<fieldset  style='font-size:9px;font-family:arial'><legend  >Debug info</legend>"
					+ _DebugFrames.listDebugHtml() + "</fieldset>";
			;
		}
		return info;
	}

	public String getRequestInfo(boolean isHtml) {
		if (_RequestValue == null)
			return "";
		String info;
		if (!isHtml) {
			info = _RequestValue.listValues(false);
		} else {
			info = "<fieldset  style='font-size:12px;font-family:arial'><legend  >Request info</legend>"
					+ _RequestValue.listValuesHtml() + "</fieldset>";
			;
		}
		;
		return info;
	}

	/**
	 * @return the _E
	 */
	public Exception getE() {
		return _E;
	}

	/**
	 * @param _e the _E to set
	 */
	public void setE(Exception _e) {
		_E = _e;
	}

	/**
	 * @return the _DebugFrames
	 */
	public DebugFrames getDebugFrames() {
		return _DebugFrames;
	}

	/**
	 * @param debugFrames the _DebugFrames to set
	 */
	public void setDebugFrames(DebugFrames debugFrames) {
		_DebugFrames = debugFrames;
	}

	/**
	 * @return the _RequestValue
	 */
	public RequestValue getRequestValue() {
		return _RequestValue;
	}

	/**
	 * @param requestValue the _RequestValue to set
	 */
	public void setRequestValue(RequestValue requestValue) {
		_RequestValue = requestValue;
	}
}
