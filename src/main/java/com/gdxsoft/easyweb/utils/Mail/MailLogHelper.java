package com.gdxsoft.easyweb.utils.Mail;

import java.io.PrintStream;

public class MailLogHelper extends PrintStream {
	private StringBuilder sb;
	private StringBuilder sbData;

	private boolean isData;

	private boolean isShowConsole;

	public boolean isShowConsole() {
		return isShowConsole;
	}

	public void setShowConsole(boolean isShowConsole) {
		this.isShowConsole = isShowConsole;
	}

	public MailLogHelper() {
		super(System.out);
		sb = new StringBuilder();
		sbData = new StringBuilder();
	}

	@Override
	public void println(String x) {
		if (isData) {
			sbData.append(x);
			sbData.append("\r\n");
			if (isShowConsole) {
				System.out.println(x);
			}
		} else {

			sb.append(x);
			sb.append("\r\n");
			if (isShowConsole) {
				System.out.println(x);
			}
		}
		if ("DATA".equals(x)) {
			isData = true;
		}
	}

	public String getDebugHeaderInfo() {
		return this.sb.toString();
	}

	public String getDebugDataInfo() {
		return this.sbData.toString();
	}

}
