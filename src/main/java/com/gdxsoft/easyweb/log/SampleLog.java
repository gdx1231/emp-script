/**
 * 
 */
package com.gdxsoft.easyweb.log;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;

/**
 * 这是个简单的写入日志的方法，你可以参照该方法定义自己需要的日志方法， <br>
 * 日志信息在对象“com.gdxsoft.easyweb.log”中
 * 
 * @author Administrator
 * 
 */
public class SampleLog extends LogBase implements ILog {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.log.ILog#Write()
	 */
	public void write() {
		if (super.getLog().getMsg() == null || super.getLog().getMsg().trim().length() == 0) {
			return;
		}
		String msg = super.getLog().getXmlName() + ", " + super.getLog().getItemName() + ", " + super.getLog().getMsg()
				+ ", " + super.getLog().getRunTime() + ", " + super.getLog().getDate();
		System.out.println(msg);
		this.writeToLog();
	}

	/**
	 * 参考定义自己的写入日志的方法, 下面的例子是个参考，是用于写入数据库的日志
	 * 
	 * CREATE TABLE LOG_MAIN( LOG_ID INT IDENTITY, USER_ID INT, LOG_MSG
	 * NVARCHAR(1000), LOG_TIME DATETIME, LOG_IP VARCHAR(19), LOG_XMLNAME
	 * VARCHAR(200), LOG_ITEMNAME VARCHAR(244), LOG_RUNTIME INT, LOG_ACTION
	 * VARCHAR(233), LOG_URL varchar(1500), LOG_REFERER varchar(1500), LOG_DES
	 * nvarchar(200) )
	 */
	private void writeToLog() {
		int abc = 0;
		if (1 == abc) {
			return;
		}

		// 根据自己的日志表结构生成写入日志的方法
		Log log = super.getLog();
		String sql = "INSERT INTO LOG_MAIN(LOG_DES, LOG_MSG, LOG_TIME,"
				+ " LOG_IP, LOG_XMLNAME, LOG_ITEMNAME, LOG_RUNTIME" + ", LOG_ACTION, LOG_URL, LOG_REFERER)"
				+ " VALUES (@LOG_DES, @LOG_MSG, @LOG_TIME," + " @LOG_IP, @LOG_XMLNAME, @LOG_ITEMNAME, @LOG_RUNTIME, "
				+ "@LOG_ACTION,  @LOG_URL, @LOG_REFERER)";
		RequestValue rv = new RequestValue();
		rv.addValue("LOG_DES", log.getDescription());
		rv.addValue("LOG_MSG", log.getMsg());
		rv.addValue("LOG_TIME", log.getDate(), "Date", 100);
		rv.addValue("LOG_IP", log.getIp());
		rv.addValue("LOG_XMLNAME", log.getXmlName());
		rv.addValue("LOG_ITEMNAME", log.getItemName());
		rv.addValue("LOG_RUNTIME", log.getRunTime());
		rv.addValue("LOG_ACTION", log.getActionName());
		rv.addValue("LOG_URL", log.getUrl());
		rv.addValue("LOG_REFERER", log.getRefererUrl());

		DataConnection a = log.getDataConn();
		// 取得数据连接当前的 RequestValue
		RequestValue rvOld = a.getRequestValue();
		try {
			a.connect();
			// 设在日志的 RequestValue
			a.setRequestValue(rv);
			a.executeUpdate(sql);
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			// 恢复RequestValue
			a.setRequestValue(rvOld);
			a.close();
		}
	}

}
