package com.gdxsoft.easyweb.script.display.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.gdxsoft.easyweb.datasource.UpdateChanges;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.script.userConfig.UserXItemValue;
import com.gdxsoft.easyweb.utils.msnet.MList;

public interface IAction {
	/**
	 * 获取执行过程中输出的所有Cookie
	 * 
	 * @return the _OutCookes
	 */
	Map<String, Cookie> getOutCookes();

	/**
	 * 获取所有输出的Session
	 * 
	 * @return the _OutSessions
	 */
	Map<String, Object> getOutSessions();

	/**
	 * 获取update完的所有变化列表
	 * 
	 * @return
	 */
	List<UpdateChanges> getLstChanges();

	/**
	 * 执行前检测错误
	 * 
	 * @return
	 */
	String getChkErrorMsg();

	/**
	 * 执行前检测错误
	 * 
	 * @param chkErrorMsg
	 */
	void setChkErrorMsg(String chkErrorMsg);

	/**
	 * 执行数据库调用条目
	 * 
	 * @param name
	 * @throws Exception
	 */
	void executeCallSql(String name) throws Exception;

	void executeXml(String name) throws Exception;

	/**
	 * 调用URl执行JSON
	 * 
	 * @param name 调用名称
	 * @throws Exception
	 */
	void executeCallJSon(String name) throws Exception;

	/**
	 * 执行调用类
	 * 
	 * @param name
	 * @throws Exception
	 */
	void executeCallClass(String name) throws Exception;

	/**
	 * 执行调用生成对象
	 * 
	 * @param strData
	 * @return
	 */
	Object[] executeCallClassCreateObjects(String strData);

	/**
	 * 执行Script条目
	 * 
	 * @param name
	 * @throws Exception
	 */
	String executeCallScript(String name) throws Exception;

	/**
	 * 执行Url条目,即跳转页面
	 * 
	 * @param name
	 * @throws Exception
	 */
	String exceuteCallUrl(String name) throws Exception;

	/**
	 * 执行生成Sessions和cookies
	 * 
	 * @param userXItemValue
	 * @throws Exception
	 */
	void executeSessionsCookies(UserXItemValue userXItemValue) throws Exception;

	/**
	 * 执行生成Sessions和cookies
	 * 
	 * @param uxv
	 * @throws Exception
	 */
	void executeSessionCookie(UserXItemValue uxv) throws Exception;

	/**
	 * @return the _Response
	 */
	HttpServletResponse getResponse();

	/**
	 * @param response the _Response to set
	 */
	void setResponse(HttpServletResponse response);

	ActionResult executeMailSend(String fromName, String fromMail, String toName, String toEmail, String subject,
			String cnt, String[] atts);

	ActionResult executeMailSend(String fromMail, String toEmail, String subject, String cnt, String[] atts);

	ActionResult executeSmsSend(String cnt, String mobiles);

	/**
	 * @return the _HtmlClass
	 */
	HtmlClass getHtmlClass();

	/**
	 * @param htmlClass the _HtmlClass to set
	 */
	void setHtmlClass(HtmlClass htmlClass);

	/**
	 * 获取所有执行返回的表
	 * 
	 * @return
	 */
	MList getDTTables();

}