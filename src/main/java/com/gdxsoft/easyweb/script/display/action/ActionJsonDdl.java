package com.gdxsoft.easyweb.script.display.action;

public class ActionJsonDdl {

	/**
	 * api请求日志
	 * @return
	 */
	 public static String getLogDdl() {
		 StringBuilder sb=new StringBuilder();
		 sb.append("CREATE TABLE [_EWA_API_REQ_LOG] (\n");
		 sb.append("	REQ_ID bigint NOT NULL,\n");
		 sb.append("	REQ_AGENT varchar(50)  NOT NULL,\n");
		 sb.append("	REQ_URL nvarchar  NOT NULL,\n");
		 sb.append("	REQ_OPTS nvarchar  NOT NULL,\n");
		 sb.append("	REQ_OPTS_MD5 varchar(32)  NOT NULL,\n");
		 sb.append("	REQ_CDATE datetime NOT NULL,\n");
		 sb.append("	REQ_MDATE datetime NOT NULL,\n");
		 sb.append("	REQ_EXPIRE datetime NOT NULL,\n");
		 sb.append("	REQ_RST nvarchar  NULL,\n");
		 sb.append("	REQ_RST_MD5 varchar(32)  NULL,\n");
		 sb.append("	REQ_RST_HTTP_CODE int NULL,\n");
		 sb.append("	REQ_START datetime NULL,\n");
		 sb.append("	REQ_END datetime NULL,\n");
		 sb.append("	REQ_UA nvarchar  NULL,\n");
		 sb.append("	REQ_IP varchar(50)  NULL,\n");
		 sb.append("	REQ_JSP nvarchar  NULL,\n");
		 sb.append("	CONSTRAINT PK_EWA_API_REQ PRIMARY KEY (REQ_ID)\n");
		 sb.append(");");
		 return sb.toString();
	 }

}
