package com.gdxsoft.easyweb.log;

import com.gdxsoft.easyweb.script.display.HtmlCreator;

public interface ILog {

	/**
	 * 写入日志信息，参考SQL脚本<br>
	 * 
	 * CREATE TABLE LOG_MAIN( LOG_ID INT IDENTITY, USER_ID INT, LOG_MSG
	 * NVARCHAR(1000), LOG_TIME DATETIME, LOG_IP VARCHAR(19), LOG_XMLNAME
	 * VARCHAR(200), LOG_ITEMNAME VARCHAR(244), LOG_RUNTIME INT, LOG_ACTION
	 * VARCHAR(233), LOG_URL varchar(1500), LOG_REFERER varchar(1500), LOG_DES
	 * nvarchar(200) )
	 */
	public abstract void Write();

	 
	public abstract Log getLog();

	 
	public abstract void setLog(Log log);

	 
	public abstract HtmlCreator getCreator();

	 
	public abstract void setCreator(HtmlCreator creator);

}