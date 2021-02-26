package com.gdxsoft.easyweb.log;

import java.util.Date;

import com.gdxsoft.easyweb.datasource.DataConnection;
import com.gdxsoft.easyweb.script.RequestValue;

public class Log {
	private DataConnection _DataConn; // 数据库连接
	private RequestValue _RequestValue;

	private String _XmlName; // 配置文件名称
	private String _ItemName; // 配置项
	private String _Description; //描述
	
	private String _Url; // URL
	private String _Ip; // 用户IP
	private Date _Date = new Date(); // 日期
	private String _RefererUrl; // 来源参考页面
	private String _Msg;// 主要信息
	private String _ActionName; // 调用Action
	private Long _StartTime = System.currentTimeMillis();

	/**
	 * @return the _XmlName
	 */
	public String getXmlName() {
		return _XmlName;
	}

	/**
	 * @param xmlName
	 *            the _XmlName to set
	 */
	public void setXmlName(String xmlName) {
		_XmlName = xmlName;
	}

	/**
	 * @return the _ItemName
	 */
	public String getItemName() {
		return _ItemName;
	}

	/**
	 * @param itemName
	 *            the _ItemName to set
	 */
	public void setItemName(String itemName) {
		_ItemName = itemName;
	}

	/**
	 * @return the _Url
	 */
	public String getUrl() {
		return _Url;
	}

	/**
	 * @param url
	 *            the _Url to set
	 */
	public void setUrl(String url) {
		_Url = url;
	}

	/**
	 * @return the _Ip
	 */
	public String getIp() {
		return _Ip;
	}

	/**
	 * @param ip
	 *            the _Ip to set
	 */
	public void setIp(String ip) {
		_Ip = ip;
	}

	/**
	 * @return the _Date
	 */
	public Date getDate() {
		return _Date;
	}

	/**
	 * @param date
	 *            the _Date to set
	 */
	public void setDate(Date date) {
		_Date = date;
	}

	/**
	 * @return the _RefererUrl
	 */
	public String getRefererUrl() {
		return _RefererUrl;
	}

	/**
	 * @param refererUrl
	 *            the _RefererUrl to set
	 */
	public void setRefererUrl(String refererUrl) {
		_RefererUrl = refererUrl;
	}

	/**
	 * @return the _RunTime
	 */
	public Long getRunTime() {
		return System.currentTimeMillis() - this._StartTime;
	}

	/**
	 * @return the _Msg
	 */
	public String getMsg() {
		return _Msg;
	}

	/**
	 * @param msg
	 *            the _Msg to set
	 */
	public void setMsg(String msg) {
		_Msg = msg;
	}

	/**
	 * @return the _ActionName
	 */
	public String getActionName() {
		return _ActionName;
	}

	/**
	 * @param actionName
	 *            the _ActionName to set
	 */
	public void setActionName(String actionName) {
		_ActionName = actionName;
	}

	/**
	 * @return the _DataConn
	 */
	public DataConnection getDataConn() {
		return _DataConn;
	}

	/**
	 * @param dataConn
	 *            the _DataConn to set
	 */
	public void setDataConn(DataConnection dataConn) {
		_DataConn = dataConn;
	}

	/**
	 * @return the _RequestValue
	 */
	public RequestValue getRequestValue() {
		return _RequestValue;
	}

	/**
	 * @param requestValue
	 *            the _RequestValue to set
	 */
	public void setRequestValue(RequestValue requestValue) {
		_RequestValue = requestValue;
	}

	/**
	 * @return the _Description
	 */
	public String getDescription() {
		return _Description;
	}

	/**
	 * @param description the _Description to set
	 */
	public void setDescription(String description) {
		_Description = description;
	}

}
