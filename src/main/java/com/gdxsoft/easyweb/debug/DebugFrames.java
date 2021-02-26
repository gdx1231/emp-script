/**
 * 
 */
package com.gdxsoft.easyweb.debug;

import java.util.ArrayList;

/**
 * @author Administrator
 * 
 */
public class DebugFrames extends ArrayList<DebugFrame> {

	private long _CurrentTime;

	private Exception _RunTimeException;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8950345360838901678L;

	/**
	 * 
	 */
	public DebugFrames() {
		_CurrentTime = System.currentTimeMillis();
	}

	public void addDebug(Object fromClass, String eventName, String description) {
		DebugFrame df = new DebugFrame(fromClass, eventName, description);
		super.add(df);
	}

	public String listDebugText() {
		StringBuilder sb = new StringBuilder();
		long t1 = this._CurrentTime;
		for (int i = 0; i < super.size(); i++) {
			DebugFrame df = super.get(i);
			long t2 = df.getCurrentTime();
			sb.append((i + 1) + ", " + (t2 - this._CurrentTime) + ", +" + (t2 - t1) + ", " + df.getClassName() + ", "
					+ df.getEventName() + ", " + df.getDesscription() + "\r\n");
			t1 = t2;
		}
		return sb.toString();
	}

	public String listDebugHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n<style>\n");
		sb.append("#EMP_DEBUG_INFO_XML{color:blue}\n");
		sb.append("#EMP_DEBUG_INFO_ERR{color:red}\n");
		sb.append("#EMP_DEBUG_INFO_SQL{color:green}\n");
		sb.append("#EMP_DEBUG_INFO_DS{color:darkgreen}\n");
		sb.append("#EMP_DEBUG_INFO_HTML{color:darkblue}\n");
		sb.append("</style>\n");

		sb.append(
				"<table border=0 style='font-size:12px;font-family:arial;' cellpadding=3 cellspacing=1 bgcolor='buttonface'>\n");
		sb.append(
				"<tr align=center bgcolor=darkgray style='color:white'><td>Index</td><td>All(ms)</td><td>ms</td><td>Class</td><td>Event</td><td>Description</td></tr>\n");
		long t1 = this._CurrentTime;
		for (int i = 0; i < super.size(); i++) {
			DebugFrame df = super.get(i);
			long t2 = df.getCurrentTime();
			sb.append("<tr id='EMP_DEBUG_INFO_");
			sb.append(df.getEventName());
			sb.append("' align=center bgcolor=white><td><nobr>");
			sb.append((i + 1));
			sb.append("</nobr></td><td><nobr>");
			sb.append((t2 - this._CurrentTime));
			sb.append("</nobr></td><td><nobr>+");
			sb.append((t2 - t1));
			sb.append("</nobr></td><td align=left><nobr>");
			sb.append(df.getClassName());
			sb.append("</nobr></td><td ><nobr>");
			sb.append(df.getEventName());
			sb.append("</nobr></td><td align=left><pre>");
			sb.append(df.getDesscription().replace("<", "&lt;").replace(">", "&gt"));
			sb.append("</pre></td></tr>\n");
			t1 = t2;
		}
		sb.append("</table>\n");
		return sb.toString();
	}

	/**
	 * 获取执行时间（ms）
	 * 
	 * @return
	 */
	public long getRunTime() {
		if (this.size() > 0) {
			DebugFrame df = this.get(this.size() - 1);
			return df.getCurrentTime() - this._CurrentTime;
		} else {
			return 0;
		}

	}

	/**
	 * @return the _RunTimeException
	 */
	public Exception getRunTimeException() {
		return _RunTimeException;
	}

	/**
	 * @param runTimeException
	 *            the _RunTimeException to set
	 */
	public void setRunTimeException(Exception runTimeException) {
		_RunTimeException = runTimeException;
	}
}
