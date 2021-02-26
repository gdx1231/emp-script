package com.gdxsoft.easyweb.utils.Mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

import com.gdxsoft.easyweb.utils.UMail;
import com.gdxsoft.easyweb.utils.Utils;

import de.agitos.dkim.DKIMSigner;
import de.agitos.dkim.DKIMSignerException;
import de.agitos.dkim.SMTPDKIMMessage;

/**
 * 发送邮件 https://www.checktls.com/TestReceiver 测试smtp ssl配置
 * 
 * @author 郭磊
 *
 */
public class SendMail {
	Properties props;
	private String smtp_uid;
	private String smtp_pwd;
	private Session mailSession;
	private boolean is_mail_debug_ = false; // 是否显示debug信息

	private String messageId; // 如:供应商标识.messageID

	private Addr from_;
	private Addr sender_;

	private Addr singleTo_;

	private HashMap<String, Addr> tos_ = new HashMap<String, Addr>();
	private HashMap<String, Addr> ccs_ = new HashMap<String, Addr>();
	private HashMap<String, Addr> bccs_ = new HashMap<String, Addr>();

	private HashMap<String, Addr> replayTos_ = new HashMap<String, Addr>();

	private HashMap<String, Attachment> atts_ = new HashMap<String, Attachment>();

	private String subject_;
	private String htmlContent_;
	private String textContent_;
	private boolean isSendToSelf_; // 抄送给自己
	private boolean isDispositionNotificationTo_; //// 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
	private boolean isAutoTextPart_ = true; // 自动创建纯文字部分
	private String charset_ = "utf-8";
	private DKIMSigner dkimSigner_;

	private MimeMessage mimeMessage_;

	private Logger log = Logger.getLogger(SendMail.class);
	private Exception lastError;

	private HashMap<String, String> headers_ = new HashMap<String, String>();

	public SendMail() {

	}

	public SendMail(String host, int port, String uid, String pwd) {
		this.initProps(host, port, uid, pwd);
	}

	/**
	 * 设置发件人
	 * 
	 * @param fromEmail 发件人邮件
	 * @param fromName  发件人姓名
	 */
	public SendMail setFrom(String fromEmail, String fromName) {
		this.from_ = new Addr(fromEmail, fromName);
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param fromEmail 发件人邮件
	 * @return
	 */
	public SendMail setFrom(String fromEmail) {
		this.from_ = new Addr(fromEmail, null);
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param from 发件人
	 * @return
	 */
	public SendMail setFrom(Addr from) {
		this.from_ = from;
		return this;
	}

	/**
	 * @return the sender_
	 */
	public Addr getSender() {
		return sender_;
	}

	/**
	 * @param sender_ the sender_ to set
	 * @return
	 */
	public SendMail setSender(Addr sender) {
		sender_ = sender;
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param fromEmail 发件人邮件
	 * @param fromName  发件人姓名
	 */
	public SendMail setSender(String senderEmail, String senderName) {
		this.sender_ = new Addr(senderEmail, senderName);
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param fromEmail 发件人邮件
	 * @return
	 */
	public SendMail setSender(String senderEmail) {
		this.sender_ = new Addr(senderEmail, null);
		return this;
	}

	/**
	 * 批量添加收件人
	 * 
	 * @param tos
	 * @param toNames
	 * @return
	 */
	public SendMail addTos(String[] tos, String[] toNames) {
		if (tos != null) {
			for (int i = 0; i < tos.length; i++) {
				String to = tos[i];
				String toName = null;
				if (toNames != null && toNames.length > i) {
					toName = toNames[i];
				}

				this.addTo(to, toName);
			}
		}
		return this;
	}

	/**
	 * 添加收件人
	 * 
	 * @param toEmail
	 */
	public SendMail addTo(String toEmail) {
		return this.addTo(toEmail, null);
	}

	/**
	 * 添加收件人
	 * 
	 * @param toEmail
	 * @param toName
	 */
	public SendMail addTo(String toEmail, String toName) {
		String keytoEmail = toEmail.trim().toLowerCase();
		Addr to = new Addr(toEmail, toName);
		tos_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加收件人
	 * 
	 * @param to
	 */
	public SendMail addTo(Addr to) {
		String keytoEmail = to.getEmail().toUpperCase().trim();
		tos_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加回复人
	 * 
	 * @param toEmail
	 */
	public SendMail addReplyTo(String toEmail) {
		return this.addReplyTo(toEmail, null);
	}

	/**
	 * 添加回复人
	 * 
	 * @param toEmail
	 * @param toName
	 */
	public SendMail addReplyTo(String toEmail, String toName) {
		String keytoEmail = toEmail.trim().toLowerCase();
		Addr to = new Addr(toEmail, toName);
		this.replayTos_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加回复人
	 * 
	 * @param to
	 */
	public SendMail addReplyTo(Addr to) {
		String keytoEmail = to.getEmail().toUpperCase().trim();
		replayTos_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 批量添加回复人
	 * 
	 * @param tos
	 * @param toNames
	 * @return
	 */
	public SendMail addReplyTos(String[] tos, String[] toNames) {
		if (tos != null) {
			for (int i = 0; i < tos.length; i++) {
				String to = tos[i];
				String toName = null;
				if (toNames != null && toNames.length > i) {
					toName = toNames[i];
				}

				this.addReplyTo(to, toName);
			}
		}

		return this;
	}

	/**
	 * 添加抄送人
	 * 
	 * @param ccEmail 抄送人邮件
	 */
	public SendMail addCc(String ccEmail) {
		this.addCc(ccEmail, null);
		return this;
	}

	/**
	 * 添加抄送人
	 * 
	 * @param ccEmail 抄送人邮件
	 * @param ccName  抄送人姓名
	 */
	public SendMail addCc(String ccEmail, String ccName) {
		String keytoEmail = ccEmail.trim().toLowerCase();
		Addr to = new Addr(ccEmail, ccName);
		ccs_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加抄送人
	 * 
	 * @param bcc 抄送人
	 */
	public SendMail addCc(Addr bcc) {
		String keytoEmail = bcc.getEmail().toUpperCase().trim();
		bccs_.put(keytoEmail, bcc);
		return this;
	}

	/**
	 * 批量添加 抄送人
	 * 
	 * @param tos
	 * @param toNames
	 * @return
	 */
	public SendMail addCcs(String[] ccs, String[] ccNames) {
		if (ccs != null) {
			for (int i = 0; i < ccs.length; i++) {
				String to = ccs[i];
				String toName = null;
				if (ccNames != null && ccNames.length > i) {
					toName = ccNames[i];
				}

				this.addCc(to, toName);
			}
		}
		return this;
	}

	/**
	 * 添加密送
	 * 
	 * @param bccEmail 密送邮件
	 */
	public SendMail addBcc(String bccEmail) {
		this.addBcc(bccEmail, null);
		return this;
	}

	/**
	 * 添加密送
	 * 
	 * @param bccEmail 密送邮件
	 * @param bccName  密送人姓名
	 */
	public SendMail addBcc(String bccEmail, String bccName) {
		String keytoEmail = bccEmail.trim().toLowerCase();
		Addr to = new Addr(bccEmail, bccName);
		bccs_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加密送
	 * 
	 * @param bcc 密送人
	 */
	public SendMail addBcc(Addr bcc) {
		String keytoEmail = bcc.getEmail().toUpperCase().trim();
		bccs_.put(keytoEmail, bcc);
		return this;
	}

	/**
	 * 批量添加 密送人
	 * 
	 * @param tos
	 * @param toNames
	 * @return
	 */
	public SendMail addBccs(String[] bccs, String[] bccNames) {
		if (bccs != null) {
			for (int i = 0; i < bccs.length; i++) {
				String to = bccs[i];
				String toName = null;
				if (bccNames != null && bccNames.length > i) {
					toName = bccNames[i];
				}

				this.addBcc(to, toName);
			}
		}
		return this;
	}

	/**
	 * 添加附件
	 * 
	 * @param file
	 * @return
	 */
	public SendMail addAttach(File file) {
		String name = file.getName();
		String path = file.getAbsolutePath();

		Attachment att = this.createAtt(name, path);
		this.atts_.put(att.getAttachName(), att);

		return this;
	}

	/**
	 * 添加附件
	 * 
	 * @param attName
	 * @param file
	 * @return
	 */
	public SendMail addAttach(String attName, File file) {
		String path = file.getAbsolutePath();

		if (attName == null || attName.trim().length() == 0) {
			attName = file.getName();
		}

		Attachment att = this.createAtt(attName, path);
		this.atts_.put(att.getAttachName(), att);

		return this;
	}

	/**
	 * 创建附件
	 * 
	 * @param attName
	 * @param path
	 * @return
	 */
	private Attachment createAtt(String attName, String path) {
		if (attName == null || attName.trim().length() == 0) {
			File file = new File(path);
			attName = file.getName();
		}

		Attachment att = new Attachment();
		att.setAttachName(attName);
		att.setSavePathAndName(path);

		return att;
	}

	/**
	 * 添加附件
	 * 
	 * @param path 附件路径
	 * @return
	 */
	public SendMail addAttach(String path) {
		File f = new File(path);
		return this.addAttach(f);
	}

	/**
	 * 添加附件
	 * 
	 * @param attName 附加名称
	 * @param path    附件路径
	 * @return
	 */
	public SendMail addAttach(String attName, String path) {
		Attachment att = this.createAtt(attName, path);
		this.atts_.put(att.getAttachName(), att);
		return this;
	}

	/**
	 * 批量添加 附件
	 * 
	 * @param tos
	 * @param toNames
	 * @return
	 */
	public SendMail addAttachs(String[] attachPaths, String[] attNames) {
		if (attachPaths != null) {
			for (int i = 0; i < attachPaths.length; i++) {
				String attachPath = attachPaths[i];
				if (attNames != null && attNames.length > i) {
					String attachName = attNames[i];
					this.addAttach(attachName, attachPath);
				} else {
					this.addAttach(attachPath);
				}
			}
		}
		return this;
	}

	/**
	 * 初始化SMTP属性
	 * 
	 * @param host
	 * @param port
	 * @param uid
	 * @param pwd
	 * @return
	 */
	public SendMail initProps(String host, int port, String uid, String pwd) {
		return this.initProps(host, port, uid, pwd, false);
	}

	/**
	 * 初始化SMTP属性
	 * 
	 * @param host
	 * @param port
	 * @param uid
	 * @param pwd
	 * @param isTryStartTls 尝试用starttls命令发邮件
	 * @return
	 */
	public SendMail initProps(String host, int port, String uid, String pwd, boolean isTryStartTls) {
		props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		// props.setProperty("mail.host", host);
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.port", port + "");
		if (uid != null && uid.trim().length() > 0) {
			props.setProperty("mail.smtp.auth", "true");
		} else {
			props.setProperty("mail.smtp.auth", "false");
		}
		smtp_uid = uid;
		smtp_pwd = pwd;

		if (port == 465) { // smtps 端口
			// 信任服务器的证书
			props.put("mail.smtp.ssl.trust", host);
			props.put("mail.smtp.ssl.trust", host);
			this.setUseSsl(true);
		} else if (isTryStartTls) {
			// If true, requires the use of the STARTTLS command. If the server doesn't
			// support the STARTTLS command, or the command fails, the connect method will
			// fail. Defaults to false.
			// props.put("mail.smtp.starttls.required", "true");

			// If true, enables the use of the STARTTLS command (if supported by the server)
			// to switch the connection to a TLS-protected connection before issuing any
			// login commands. Defaults to false.
			props.put("mail.smtp.starttls.enable", "true");
			// 信任服务器的证书
			props.put("mail.smtp.ssl.trust", host);
		}
		// System.out.println("SMTP: " + host + ":" + port + ", uid=" + uid + ", pwd=" +
		// pwd);

		return this;
	}

	/**
	 * 获取MailSession
	 * 
	 * @return
	 */
	public Session getMailSession() {
		if (mailSession == null) {
			if (props == null) {
				// 默认的发件, ewa_conf中定义
				mailSession = UMail.getMailSession();
			} else {
				if (smtp_uid != null && smtp_uid.trim().length() > 0) {
					MailAuth auth = new MailAuth(smtp_uid, smtp_pwd);
					props.setProperty("mail.smtp.auth", "true");
					mailSession = Session.getInstance(props, auth);
				} else {
					props.setProperty("mail.smtp.auth", "false");
					mailSession = Session.getInstance(props, null);
				}
			}
			mailSession.setDebug(this.is_mail_debug_);

		}

		return mailSession;
	}

	public InternetAddress getAddress(Addr addr) {
		InternetAddress iaFrom = new InternetAddress();
		iaFrom.setAddress(addr.getEmail().trim());
		if (addr.getName() != null) {
			try {
				iaFrom.setPersonal(addr.getName(), this.charset_);
			} catch (UnsupportedEncodingException e) {
				log.error(e);
			}
		}

		return iaFrom;
	}

	private InternetAddress[] getAddresses(HashMap<String, Addr> addrs) {
		InternetAddress[] addresses = new InternetAddress[addrs.size()];
		int inc = 0;
		for (String key : addrs.keySet()) {
			Addr addr = addrs.get(key);
			InternetAddress iaFrom = this.getAddress(addr);
			addresses[inc] = iaFrom;
			inc++;
		}
		return addresses;
	}

	/**
	 * 获取邮件
	 * 
	 * @return
	 * @throws MessagingException
	 */
	public MimeMessage getMimeMessage() throws MessagingException {
		if (this.mimeMessage_ != null) {
			return this.mimeMessage_;
		}
		MimeMessage mm = this.createMinMessage();
		return mm;
	}

	private MimeBodyPart getMailContent() throws MessagingException {
		if (this.textContent_ == null && this.isAutoTextPart_ && this.htmlContent_ != null) {
			this.textContent_ = Utils.filterHtml(this.htmlContent_);
		}
		MimeBodyPart contentPart = new MimeBodyPart();

		// 混合了正文和 html 降低邮件垃圾评分
		// http://www.mail-tester.com/
		if (this.textContent_ != null && this.htmlContent_ != null) {
			// 创建一个MIME子类型为"alternative"的MimeMultipart对象
			// 并作为前面创建的 contentPart 对象的邮件内容
			MimeMultipart htmlMultipart = new MimeMultipart("alternative");

			contentPart.setContent(htmlMultipart);
			// 纯文本正文
			MimeBodyPart textBodypart = new MimeBodyPart();
			textBodypart.setText(textContent_, charset_);
			htmlMultipart.addBodyPart(textBodypart);

			// html正文
			MimeBodyPart htmlBodypart = new MimeBodyPart();
			htmlBodypart.setContent(this.htmlContent_, "text/html;charset=" + this.charset_);
			htmlMultipart.addBodyPart(htmlBodypart);

		} else if (this.textContent_ != null) {
			// 纯文本正文
			contentPart.setText(textContent_, charset_);
		} else if (this.htmlContent_ != null) {
			// html正文
			contentPart.setContent(this.htmlContent_, "text/html;charset=" + this.charset_);

		} else {// 没有正文
			contentPart.setText("", charset_);
		}

		return contentPart;
	}

	/**
	 * 创建邮件
	 * 
	 * @return
	 * @throws MessagingException
	 */
	public MimeMessage createMinMessage() throws MessagingException {
		MimeMessage mm;
		if (this.messageId != null && this.messageId.trim().length() > 0) {
			// 更改邮件的头的message_id
			CustomMimeMessage cmm = new CustomMimeMessage(mailSession);
			cmm.setOldMessageId(this.messageId);
			cmm.updateMessageID();
			this.addHeader("X-EWA-MESSAGE-ID", this.messageId.trim());
			mm = cmm;
		} else {
			mm = new MimeMessage(mailSession);
		}
		this.mimeMessage_ = mm;

		mm.setSubject(this.encodeAttName(this.getSubject()));

		InternetAddress iaFrom = this.getAddress(this.from_);
		mm.setFrom(iaFrom);

		InternetAddress[] tos = this.getAddresses(this.tos_);
		mm.setRecipients(Message.RecipientType.TO, tos); // 收件人

		// 抄送
		if (this.ccs_.size() > 0) {
			InternetAddress[] ccs = this.getAddresses(this.ccs_);
			mm.setRecipients(Message.RecipientType.CC, ccs);
		}
		if (this.isSendToSelf_) {// 抄送给自己
			this.addBcc(this.from_);
		}

		// 秘送
		if (this.bccs_.size() > 0) {
			InternetAddress[] bccs = this.getAddresses(this.bccs_);
			mm.setRecipients(Message.RecipientType.BCC, bccs);
		}

		// 回复人
		if (this.replayTos_.size() > 0) {
			InternetAddress[] replayTos = this.getAddresses(this.replayTos_);
			mm.setReplyTo(replayTos);
		}
		//// 创建一个MIME子类型为"mixed"的MimeMultipart对象，表示这是一封混合组合类型的邮件
		Multipart multipart = new MimeMultipart("mixed");
		mm.setContent(multipart);

		if (this.messageId != null && this.messageId.trim().length() > 0) {
			this.addHeader("X-EWA-MESSAGE-ID", this.messageId.trim());
		}
		// 要求回执，妈的被他害了，收了无数的垃圾邮件，Mac的邮件会自动发送，因此变成了反弹垃圾邮件
		if (this.isDispositionNotificationTo_) {
			this.addHeader("Disposition-Notification-To", from_.getEmail());
		}
		if (this.sender_ != null) {
			// 设置邮件的sender参数，看看能不能躲过垃圾邮件检测
			InternetAddress sender = this.getAddress(this.sender_);
			mm.setSender(sender);
		}
		// 设置正文
		MimeBodyPart textBody = this.getMailContent();
		multipart.addBodyPart(textBody);

		for (String attName : this.atts_.keySet()) {
			Attachment att = this.atts_.get(attName);
			BodyPart affixBody = new MimeBodyPart();
			DataSource source = new FileDataSource(att.getSavePathAndName());
			// 添加附件的内容
			affixBody.setDataHandler(new DataHandler(source));

			String fileName = this.encodeAttName(attName);
			affixBody.setFileName(fileName);

			multipart.addBodyPart(affixBody);
		}

		// 设置发送时间
		mm.setSentDate(new Date());

		if (!this.headers_.containsKey("X-Mailer")) {
			this.headers_.put("X-Mailer", "EWA2(java) gdxsoft.com");
		}

		// 添加头部
		for (String name : this.headers_.keySet()) {
			String value = this.headers_.get(name);
			if (value != null) {
				mm.addHeader(name, value);
			}
		}

		try {
			mm = this.dkimSign(mm);
		} catch (Exception e) {
			log.error(e);
		}

		return mm;
	}

	/**
	 * 将头部放到缓存中
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public boolean addHeader(String name, String value) {
		headers_.put(name, value);
		return true;
	}

	/**
	 * 发送邮件
	 * 
	 * @return
	 */
	public boolean send() {
		Session mailSession = getMailSession();
		MimeMessage mm;
		try {
			mm = this.getMimeMessage();
		} catch (MessagingException e1) {
			this.lastError = e1;
			log.error(e1);
			return false;
		}

		Transport transport = null;
		try {
			transport = mailSession.getTransport();
			transport.connect();

			if (this.singleTo_ == null) {
				Address[] recs = mm.getAllRecipients();
				Transport.send(mm, recs);
			} else {
				// 跟踪需要，单一发送独立邮件到独立的邮箱，单是显示收件人为多个
				Address[] singleTo = new Address[1];
				singleTo[0] = this.getAddress(singleTo_);
				Transport.send(mm, singleTo);
			}
			return true;
		} catch (Exception e) {
			this.lastError = e;
			log.error(e);
			return false;
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					log.error(e);
				}
			}
		}
	}

	/**
	 * 获取附件中文名称
	 * 
	 * @param attName
	 * @return
	 */
	private String encodeAttName(String attName) {
		// https://stackoverflow.com/questions/31799960/java-mail-mimeutility-encodetext-unsupportedencodingexception-base64
		try {
			// 中文附件标题名在发送时不会变成乱码
			// String fileName = "=?UTF-8?B?" +
			// UConvert.ToBase64String(attName.getBytes("UTF-8")) + "?=";
			// B" or "Q";
			String fileName = MimeUtility.encodeText(attName, this.charset_, "Q");
			return fileName;
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			return attName;
		}

	}

	/**
	 * 设置邮件DKIM
	 * 
	 * @param domain             域名，需要和发件人域名一致
	 * @param privateKeyFilePath 私有文件路径
	 * @param select             选择，默认default
	 * @return
	 */
	public SendMail setDkim(String domain, String privateKeyFilePath, String select) {
		if (select == null) {
			select = "default";
		}
		try {
			this.dkimSigner_ = new DKIMSigner(domain, select, privateKeyFilePath);
		} catch (Exception e) {
		}

		return this;
	}

	/**
	 * 设置邮件DKIM
	 * 
	 * @param cfg DKIMCfg
	 * @return
	 */
	public SendMail setDkim(DKIMCfg cfg) {
		if (cfg != null) {
			try {
				this.dkimSigner_ = new DKIMSigner(cfg.getDomain(), cfg.getSelect(), cfg.getPrivateKeyPath());
			} catch (Exception e) {
				log.warn(e);
			}
		}

		return this;
	}

	public MimeMessage dkimSign(MimeMessage mm) throws MessagingException, DKIMSignerException {
		if (this.dkimSigner_ != null) {
			dkimSigner_.setIdentity(this.from_.getEmail());
			mm = new SMTPDKIMMessage(mm, dkimSigner_);
		}
		return mm;
	}

	public String getMessageId() {
		return messageId;

	}

	public SendMail setMessageId(String msgId) {
		this.messageId = msgId;
		return this;
	}

	/**
	 * @return the subject_
	 */
	public String getSubject() {
		return subject_;
	}

	/**
	 * @param subject_ the subject_ to set
	 */
	public SendMail setSubject(String subject) {
		this.subject_ = subject;
		return this;
	}

	/**
	 * @return the htmlContent_
	 */
	public String getHtmlContent() {
		return htmlContent_;
	}

	/**
	 * @param htmlContent_ the htmlContent_ to set
	 */
	public SendMail setHtmlContent(String htmlContent) {
		this.htmlContent_ = htmlContent;
		return this;
	}

	/**
	 * @return the textContent_
	 */
	public String getTextContent() {
		return textContent_;
	}

	/**
	 * @param textContent_ the textContent_ to set
	 */
	public SendMail setTextContent(String textContent) {
		this.textContent_ = textContent;
		return this;
	}

	/**
	 * 发件人
	 * 
	 * @return the from_
	 */
	public Addr getFrom() {
		return from_;
	}

	/**
	 * 收件人
	 * 
	 * @return the to_
	 */
	public HashMap<String, Addr> getTos() {
		return tos_;
	}

	/**
	 * 抄送
	 * 
	 * @return the ccs_
	 */
	public HashMap<String, Addr> getCcs() {
		return ccs_;
	}

	/**
	 * 密送
	 * 
	 * @return the bccs_
	 */
	public HashMap<String, Addr> getBccs() {
		return bccs_;
	}

	/**
	 * 获取邮件编码
	 * 
	 * @return the charset_
	 */
	public String getCharset() {
		return charset_;
	}

	/**
	 * 设置邮件编码
	 * 
	 * @param charset_ the charset_ to set
	 * @return
	 */
	public SendMail setCharset(String charset) {
		this.charset_ = charset;
		return this;
	}

	/**
	 * 抄送给自己
	 * 
	 * @return the isSendToSelf_
	 */
	public boolean isSendToSelf() {
		return isSendToSelf_;
	}

	/**
	 * 抄送给自己
	 * 
	 * @param isSendToSelf_ the isSendToSelf_ to set
	 * @return
	 */
	public SendMail setSendToSelf(boolean isSendToSelf) {
		this.isSendToSelf_ = isSendToSelf;
		return this;
	}

	/**
	 * 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
	 * 
	 * @return the isDispositionNotificationTo_
	 */
	public boolean isDispositionNotificationTo() {
		return isDispositionNotificationTo_;
	}

	/**
	 * 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
	 * 
	 * @param isDispositionNotificationTo 要求阅读回执
	 * @return
	 */
	public SendMail setDispositionNotificationTo(boolean isDispositionNotificationTo) {
		this.isDispositionNotificationTo_ = isDispositionNotificationTo;
		return this;
	}

	/**
	 * 是否自动创建html邮件的纯文本部分，便于降低垃圾邮件判别的评分，默认true
	 * 
	 * @return the isAutoTextPart_
	 */
	public boolean isAutoTextPart() {
		return isAutoTextPart_;
	}

	/**
	 * 是否自动创建html邮件的纯文本部分，便于降低垃圾邮件判别的评分，默认true
	 * 
	 * @param isAutoTextPart_ the isAutoTextPart_ to set
	 * @return
	 */
	public SendMail setAutoTextPart(boolean isAutoTextPart_) {
		this.isAutoTextPart_ = isAutoTextPart_;
		return this;
	}

	/**
	 * @return the replayTos_
	 */
	public HashMap<String, Addr> getReplayTos() {
		return replayTos_;
	}

	/**
	 * 是否跟踪邮件发送细节
	 * 
	 * @return the is_mail_debug_
	 */
	public boolean isMailDebug() {
		return is_mail_debug_;
	}

	/**
	 * 是否跟踪邮件发送细节
	 * 
	 * @param is_mail_debug_ the is_mail_debug_ to set
	 * @return
	 */
	public SendMail setIsMailDebug(boolean is_mail_debug) {
		this.is_mail_debug_ = is_mail_debug;
		return this;
	}

	/**
	 * @return the lastError
	 */
	public Exception getLastError() {
		return lastError;
	}

	/**
	 * 单一收件人，TO为多人，实际发送此人，用于跟踪
	 * 
	 * @return the singleTo_
	 */
	public Addr getSingleTo() {
		return singleTo_;
	}

	/**
	 * 设置单一收件人，TO为多人，实际发送此人，用于跟踪
	 * 
	 * @param singleToEmail 邮件地址
	 * @param singleToName  名称
	 * 
	 */
	public void setSingleTo(String singleToEmail, String singleToName) {
		Addr addr = new Addr();
		addr.setEmail(singleToEmail);
		addr.setName(singleToName);
		this.singleTo_ = addr;
	}

	/**
	 * 设定邮件内容
	 * 
	 * @param mineMessage_ the mineMessage_ to set
	 */
	public void setMimeMessage(MimeMessage mimeMessage) {
		this.mimeMessage_ = mimeMessage;
	}

	/**
	 * 获取 发送邮件配置信息，用于修改
	 * 
	 * @return the props
	 */
	public Properties getProps() {
		return props;
	}

	/**
	 * 设置是否用 ssl协议进行发送邮件，端口465默认打开此协议
	 * 
	 * @param isSsl
	 */
	public void setUseSsl(boolean isSsl) {
		props.put("mail.smtp.ssl.enable", isSsl);
	}

}
