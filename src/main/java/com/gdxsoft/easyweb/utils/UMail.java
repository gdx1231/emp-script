package com.gdxsoft.easyweb.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.jfree.util.Log;

import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;
import com.gdxsoft.easyweb.utils.Mail.MailAuth;
import com.gdxsoft.easyweb.utils.Mail.SendMail;

public class UMail {

	/**
	 * 获取MailSession
	 * 
	 * @return
	 */
	public static Session getMailSession() {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", UPath.getSmtpIp());
		if (UPath.getSmtpPort() != 25) {
			props.setProperty("mail.smtp.port", UPath.getSmtpPort() + "");
		}
		Session mailSession;
		if (UPath.getSmtpUser() != null && UPath.getSmtpUser().trim().length() > 0) {
			props.setProperty("mail.smtp.auth", "true");
			MailAuth auth = new MailAuth(UPath.getSmtpUser(), UPath.getSmtpPwd());
			mailSession = Session.getDefaultInstance(props, auth);
		} else {
			props.setProperty("mail.smtp.auth", "false");
			mailSession = Session.getDefaultInstance(props, null);
		}
		return mailSession;
	}

	/**
	 * 获取邮件 message
	 * 
	 * @param from    发件人
	 * @param to      收件人
	 * @param subject 主题
	 * @param content 内容
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public static MimeMessage getMimeMessage(String from, String to, String subject, String content)
			throws UnsupportedEncodingException, MessagingException {
		return getMimeMessage(from, from, to, to, subject, content, null, "utf-8");
	}

	/**
	 * 获取邮件 message
	 * 
	 * @param from     发件人
	 * @param fromName 发件人姓名
	 * @param to       收件人
	 * @param toName   收件人姓名
	 * @param subject  主题
	 * @param content  内容
	 * @param atts     附件
	 * @param charset  语言
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static MimeMessage getMimeMessage(String from, String fromName, String to, String toName, String subject,
			String content, String[] atts, String charset) throws MessagingException, UnsupportedEncodingException {

		SendMail sendmail = new SendMail();

		sendmail.setFrom(from, fromName).addTo(to, toName).setSubject(subject).setHtmlContent(content)
				.setCharset(charset);
		if (atts != null) {
			for (int i = 0; i < atts.length; i++) {
				String filePath = atts[i];
				if (filePath == null || filePath.trim().length() == 0) {
					continue;
				}
				sendmail.addAttach(filePath);
			}
		}
		return sendmail.getMimeMessage();
	}

	/**
	 * 创建邮件
	 * 
	 * @param from         发件人邮件
	 * @param fromName     发件人姓名
	 * @param tos          收件人数组
	 * @param toNames      收件人名称数组
	 * @param ccs          抄送人邮件数组
	 * @param ccNames      抄送人名称数组
	 * @param bccs         密送人邮件数组
	 * @param bccNames     密送人名称数组
	 * @param replyTos     回复人邮件数组
	 * @param replyToNames 回复人名称数组
	 * @param sender       发件人
	 * @param senderName   发件人名称
	 * @param subject      邮件主题
	 * @param content      邮件内容
	 * @param atts         附件文件路径数组
	 * @param charset      邮件编码
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static MimeMessage getMimeMessage(String from, String fromName, String[] tos, String[] toNames, String[] ccs,
			String[] ccNames, String[] bccs, String[] bccNames, String[] replyTos, String[] replyToNames, String sender,
			String senderName, String subject, String content, String[] atts, String charset)
			throws MessagingException, UnsupportedEncodingException {

		SendMail sendmail = UMail.createSendMail(from, fromName, tos, toNames, ccs, ccNames, bccs, bccNames, replyTos,
				replyToNames, sender, senderName, subject, content, atts, charset);
		return sendmail.getMimeMessage();
	}

	/**
	 * 创建 SendMail
	 * 
	 * @param from         发件人邮件
	 * @param fromName     发件人姓名
	 * @param tos          收件人数组
	 * @param toNames      收件人名称数组
	 * @param ccs          抄送人邮件数组
	 * @param ccNames      抄送人名称数组
	 * @param bccs         密送人邮件数组
	 * @param bccNames     密送人名称数组
	 * @param replyTos     回复人邮件数组
	 * @param replyToNames 回复人名称数组
	 * @param sender       发件人
	 * @param senderName   发件人名称
	 * @param subject      邮件主题
	 * @param content      邮件内容
	 * @param atts         附件文件路径数组
	 * @param charset      邮件编码
	 * @return
	 */
	public static SendMail createSendMail(String from, String fromName, String[] tos, String[] toNames, String[] ccs,
			String[] ccNames, String[] bccs, String[] bccNames, String[] replyTos, String[] replyToNames, String sender,
			String senderName, String subject, String content, String[] atts, String charset) {
		SendMail sendmail = new SendMail();

		sendmail.setFrom(from, fromName).addTos(tos, toNames).addCcs(ccs, ccNames).addBccs(bccs, bccNames)
				.addReplyTos(replyTos, replyToNames).addAttachs(atts, null).setSubject(subject).setHtmlContent(content);
		if (sender != null) {
			sendmail.setSender(sender, senderName);
		}
		if (charset != null) {
			sendmail.setCharset(charset);
		}

		DKIMCfg cfg = UMail.getDKIMCfgByEmail(from);
		if (cfg != null) {
			sendmail.setDkim(cfg);
		}

		return sendmail;
	}

	/**
	 * 获取邮件的域名
	 * 
	 * @param email 邮件
	 * @return 域名
	 */
	public static String getEmailDomain(String email) {
		if (email == null || email.trim().length() == 0) {
			return null;
		}
		String[] aa = email.split("\\@");
		if (aa.length == 2) {
			String domain = aa[1];
			return domain;
		}
		return null;
	}

	/**
	 * 根据邮件地址获取 Dkim 配置
	 * 
	 * @param email 邮件
	 * @return
	 */
	public static DKIMCfg getDKIMCfgByEmail(String email) {
		if (UPath.getDKIM_CFGS().size() == 0) {
			return null;
		}
		String domain = getEmailDomain(email);
		return getDKIMCfgByDomain(domain);
	}

	/**
	 * 根据域名获取 Dkim 配置
	 * 
	 * @param domain 域名
	 * @return
	 */
	public static DKIMCfg getDKIMCfgByDomain(String domain) {
		if (domain == null || domain.trim().length() == 0 || UPath.getDKIM_CFGS().size() == 0) {
			return null;
		}
		String domain1 = domain.trim().toLowerCase();

		if (UPath.getDKIM_CFGS().containsKey(domain1)) {
			return UPath.getDKIM_CFGS().get(domain1);
		} else {
			return null;
		}
	}

	/**
	 * 发送邮件
	 * 
	 * @param from         发件人邮件
	 * @param fromName     发件人姓名
	 * @param tos          收件人数组
	 * @param toNames      收件人名称数组
	 * @param ccs          抄送人邮件数组
	 * @param ccNames      抄送人名称数组
	 * @param bccs         密送人邮件数组
	 * @param bccNames     密送人名称数组
	 * @param replyTos     回复人邮件数组
	 * @param replyToNames 回复人名称数组
	 * @param sender       发件人
	 * @param senderName   发件人名称
	 * @param subject      邮件主题
	 * @param content      邮件内容
	 * @param atts         附件文件路径数组
	 * @param charset      邮件编码
	 * @return
	 */
	public static String sendHtmlMail(String from, String fromName, String[] tos, String[] toNames, String[] ccs,
			String[] ccNames, String[] bccs, String[] bccNames, String[] replyTos, String[] replyToNames, String sender,
			String senderName, String subject, String content, String[] atts, String charset) {
		try {
			MimeMessage mm = UMail.getMimeMessage(from, fromName, tos, toNames, ccs, ccNames, bccs, bccNames, replyTos,
					replyToNames, sender, senderName, subject, content, atts, charset);
			return sendMail(mm);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			Log.error(e);
			return e.getMessage();
		}
	}

	/**
	 * 发送邮件
	 * 
	 * @param from         发件人邮件
	 * @param fromName     发件人姓名
	 * @param tos          收件人邮件，多个收件人用“,”分割
	 * @param toNames      收件人姓名，多个收件人姓名用“,”分割
	 * @param replyTos     回复人邮件，多个回复人用“,”分割
	 * @param replyToNames 回复人姓名，多个回复人用“,”分割
	 * @param subject      邮件标题
	 * @param content      正文
	 * @param atts         附件文件路径数组
	 * @return
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String replyTos,
			String replyToNames, String subject, String content, String[] atts, String charset) {
		String[] tos1 = tos.split(",");
		String[] toNames1 = toNames == null ? null : toNames.split(",");

		String[] replyTos1 = replyTos == null ? null : replyTos.split(",");
		String[] replyToNames1 = replyToNames == null ? null : replyToNames.split(",");
		return UMail.sendHtmlMail(from, fromName, tos1, toNames1, null, null, null, null, replyTos1, replyToNames1,
				null, null, subject, content, atts, charset);

	}

	/**
	 * 发生Html邮件
	 * 
	 * @param from     发件人
	 * @param fromName 发件人姓名
	 * @param tos      收件人姓名，多个收件人姓名用“,”分割
	 * @param toNames  收件人姓名
	 * @param subject  主题
	 * @param content  内容
	 * @param atts     附件
	 * @param charset  语言
	 * @return 是否成功
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String subject,
			String content, String[] atts, String charset) {
		return UMail.sendHtmlMail(from, fromName, tos, toNames, null, null, subject, content, atts, charset);
	}

	/**
	 * 发送邮件
	 * 
	 * @param from     发件人
	 * @param fromName 发件人姓名
	 * @param tos      收件人邮件，多个收件人用“,”分割
	 * @param toNames  收件人姓名，多个收件人姓名用“,”分割
	 * @param subject  主题
	 * @param content  内容
	 * @return
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String subject,
			String content) {
		return sendHtmlMail(from, fromName, tos, toNames, subject, content, null, "utf-8");
	}

	/**
	 * 发送邮件
	 * 
	 * @param from     发件人
	 * @param fromName 发件人姓名
	 * @param tos      收件人邮件，多个收件人用“,”分割
	 * @param toNames  收件人姓名，多个收件人姓名用“,”分割
	 * @param subject  主题
	 * @param content  内容
	 * @param atts     附件文件路径数组
	 * @return
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String subject,
			String content, String[] atts) {
		return sendHtmlMail(from, fromName, tos, toNames, subject, content, atts, "utf-8");
	}

	/**
	 * 发送邮件
	 * 
	 * @param from    发件人邮件
	 * @param tos     收件人邮件，多个收件人用“,”分割
	 * @param subject 主题
	 * @param content 内容
	 * @param atts    附件文件路径数组
	 * @return
	 */
	public static String sendHtmlMail(String from, String tos, String subject, String content, String[] atts) {
		return sendHtmlMail(from, "", tos, "", subject, content, atts, "utf-8");
	}

	/**
	 * 发送邮件
	 * 
	 * @param from    发件人邮件
	 * @param tos     收件人邮件，多个收件人用“,”分割
	 * @param subject 主题
	 * @param content 内容
	 * @return
	 */
	public static String sendHtmlMail(String from, String tos, String subject, String content) {
		return sendHtmlMail(from, "", tos, "", subject, content, null, "utf-8");
	}

	/**
	 * 发送邮件
	 * 
	 * @param mm
	 * @return
	 */
	public static String sendMail(Message mm) {
		Session mailSession = getMailSession();
		try {
			Transport transport = mailSession.getTransport();
			transport.connect();
			Transport.send(mm, mm.getAllRecipients());
			transport.close();
			return null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return e.getMessage();
		}
	}
}
