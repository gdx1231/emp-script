package com.gdxsoft.easyweb.script.display.action;


public interface IActionProvider {
 
	 

	/**
	 * 邮件发送
	 * @param fromName
	 * @param fromMail
	 * @param toName
	 * @param toEmail
	 * @param subject
	 * @param cnt
	 * @param atts
	 * @return
	 */
	public abstract ActionResult executeMailSend(String fromName,String fromMail,String toName,String toEmail,String subject,String cnt,String[] atts);

	/**
	 * 邮件发送
	 * @param fromMail
	 * @param toEmail
	 * @param subject
	 * @param cnt
	 * @param atts
	 * @return
	 */
	public abstract ActionResult executeMailSend( String fromMail, String toEmail,String subject,String cnt,String[] atts);

	/**
	 * 短信发送
	 * @param cnt
	 * @param mobiles 用”,“分割的手机地址
	 * @return
	 */
	public abstract ActionResult executeSmsSend(String cnt,String mobiles);
	 
}