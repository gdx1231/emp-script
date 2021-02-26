package com.gdxsoft.easyweb.utils.Mail;

//附件接收实现代码：

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.Utils;

/**
 * @author Administrator
 * 
 */
public class MailDecode {
	private MimeMessage mimeMessage = null;

	private String saveAttachPath = "d:\\";// 附件下载后的存放目录

	private StringBuffer bodytext;
	private StringBuffer bodyHtml;
	private List<Attachment> _Atts = new ArrayList<Attachment>();

	/**
	 * 获取所有附件，包括attachment和inline
	 * 
	 * @return the _Atts
	 */
	public List<Attachment> getAtts() {
		return _Atts;
	}

	public MailDecode(MimeMessage mimeMessage, String attachmentSavePath) {
		this.mimeMessage = mimeMessage;
		this.saveAttachPath = attachmentSavePath;
	}

	/**
	 * 
	 * 获取发件人地址
	 * 
	 * @return
	 */
	public Addr getFrom() throws Exception {
		InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
		if (address == null) {
			return null;
		}
		String from = address[0].getAddress();
		if (from == null)
			from = "";
		String personal = address[0].getPersonal();
		if (personal == null)
			personal = "";
		Addr r = new Addr();
		r.setEmail(from);
		r.setName(personal);
		return r;
	}

	/**
	 * 获取抄送列表
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Addr> getCc() throws Exception {
		return this.getMailAddress("CC");
	}

	/**
	 * 获取收件人列表
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Addr> getTo() throws Exception {
		return this.getMailAddress("TO");
	}

	/**
	 * 获取密送人列表
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Addr> getBcc() throws Exception {
		return this.getMailAddress("BCC");
	}

	/**
	 * 获得邮件的收件人，抄送，和密送的地址和姓名，<br>
	 * 根据所传递的参数的不同 <br>
	 * "to"----收件人 <br>
	 * "cc"---抄送人地址<br>
	 * "bcc"---密送人地址
	 */

	private List<Addr> getMailAddress(String type) throws Exception {
		String addtype = type.toUpperCase();
		InternetAddress[] address = null;

		HashMap<String, String> addrs = new HashMap<String, String>();
		if (addtype.equals("TO") || addtype.equals("CC")
				|| addtype.equals("BCC")) {
			if (addtype.equals("TO")) {
				address = (InternetAddress[]) mimeMessage
						.getRecipients(Message.RecipientType.TO);
			} else if (addtype.equals("CC")) {
				address = (InternetAddress[]) mimeMessage
						.getRecipients(Message.RecipientType.CC);
			} else {
				address = (InternetAddress[]) mimeMessage
						.getRecipients(Message.RecipientType.BCC);
			}
			if (address != null) {
				for (int i = 0; i < address.length; i++) {
					String email = address[i].getAddress();
					if (email == null)
						email = "";
					else {
						email = MimeUtility.decodeText(email);
					}
					String personal = address[i].getPersonal();
					if (personal == null)
						personal = "";
					else {
						personal = MimeUtility.decodeText(personal);
					}

					addrs.put(email, personal);
				}
			}
		} else {
			throw new Exception("Error　emailaddr　type!");
		}
		List<Addr> rets = new ArrayList<Addr>();
		Iterator<String> it = addrs.keySet().iterator();
		while (it.hasNext()) {
			String email = it.next();
			String name = addrs.get(email);
			Addr r = new Addr();
			r.setEmail(email);
			r.setName(name);
			rets.add(r);
		}

		return rets;
	}

	/**
	 * 
	 * 获取邮件主题
	 */
	public String getSubject() throws MessagingException {
		String subject;
		try {
			subject = MimeUtility.decodeText(mimeMessage.getSubject());
			if (subject == null)
				subject = "";
			return subject;
		} catch (Exception exce) {
			return exce.getMessage();
		}

	}

	/**
	 * 获取邮件发送日期
	 */
	public java.util.Date getSentDate() throws Exception {
		Date sentdate = mimeMessage.getSentDate();
		return sentdate;
	}

	/**
	 * 解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，<br>
	 * 解析邮件 主要是根据MimeType类型的不同执行不同的操作， 一步一步的解析
	 * 
	 * @param part
	 * @throws Exception
	 */
	private void getMailContent(Part part) throws Exception {
		if (bodytext == null) {
			this.bodyHtml = new StringBuffer();
			this.bodytext = new StringBuffer();
		}
		String contenttype = part.getContentType();
		int nameindex = contenttype.indexOf("name");
		boolean conname = false;
		if (nameindex != -1)
			conname = true;
		// System.out.println("CONTENTTYPE:　" + contenttype);
		if (part.isMimeType("text/plain") && !conname) {
			Object o = part.getContent();
			String txt = o.toString();
			bodytext.append(txt);
		} else if (part.isMimeType("text/html") && !conname) {
			Object o = part.getContent();
			String html = o.toString();
			html = removeHtmlBody(html);
			bodyHtml.append(html);
		} else if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent();
			int counts = multipart.getCount();
			for (int i = 0; i < counts; i++) {
				getMailContent(multipart.getBodyPart(i));
			}
		} else if (part.isMimeType("message/rfc822")) {
			getMailContent((Part) part.getContent());
		} else {
		}
	}

	private String removeHtmlBody(String html) {
		String h1 = html.toUpperCase();
		int locA = h1.indexOf("<BODY");
		if (locA > 0) {
			int locA1 = h1.indexOf(">", locA);
			if (locA1 > 0) {
				html = html.substring(locA1 + 1);
			}
		}
		h1 = html.toUpperCase();
		int locB = h1.indexOf("</BODY");
		if (locB > 0) {
			int locB1 = h1.indexOf(">", locB);
			if (locB1 > 0) {
				html = html.substring(0, locB1 - 6);
			}
		}
		return html;

	}

	/**
	 * 
	 * 判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"
	 * 
	 */

	public boolean getReplySign() throws MessagingException {
		boolean replysign = false;
		String needreply[] = mimeMessage
				.getHeader("Disposition-Notification-To");
		if (needreply != null) {
			replysign = true;
		}
		return replysign;
	}

	/**
	 * 获得此邮件的Message-ID
	 */
	public String getMessageId() throws MessagingException {
		return mimeMessage.getMessageID();
	}

	/**
	 * 【判断此邮件是否已读，如果未读返回返回false,反之返回true】
	 */
	public boolean isNew() throws MessagingException {
		boolean isnew = false;
		Flags flags = ((Message) mimeMessage).getFlags();
		Flags.Flag[] flag = flags.getSystemFlags();
		// System.out.println("flags's　length:　" + flag.length);
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == Flags.Flag.SEEN) {
				isnew = true;
				// System.out.println("seen　Message.......");
				break;
			}
		}
		return isnew;
	}

	/**
	 * 
	 * 判断此邮件是否包含附件
	 * 
	 */
	public boolean isContainAttach(Part part) throws Exception {
		boolean attachflag = false;
		// String contentType = part.getContentType();
		if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart mpart = mp.getBodyPart(i);
				String disposition = mpart.getDisposition();
				if ((disposition != null)
						&& ((disposition.equals(Part.ATTACHMENT)) || (disposition
								.equals(Part.INLINE))))
					attachflag = true;
				else if (mpart.isMimeType("multipart/*")) {
					attachflag = isContainAttach((Part) mpart);
				} else {
					String contype = mpart.getContentType();
					if (contype.toLowerCase().indexOf("application") != -1)
						attachflag = true;
					if (contype.toLowerCase().indexOf("name") != -1)
						attachflag = true;
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			attachflag = isContainAttach((Part) part.getContent());
		}
		return attachflag;
	}

	/**
	 * 保存所有附件
	 * 
	 * @throws Exception
	 */
	public void saveAttachments() throws Exception {
		Part part = (Part) this.mimeMessage;
		this.saveAttachMent(part);
	}

	/**
	 * 保存附件
	 */
	private void saveAttachMent(Part part) throws Exception {
		String fileName = "";
		if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart mpart = mp.getBodyPart(i);
				String disposition = mpart.getDisposition();
				if ((disposition != null)
						&& ((disposition.equals(Part.ATTACHMENT)) || (disposition
								.equals(Part.INLINE)))) {
					Attachment att = this.saveAttachmentToFile(mpart);
					if (att != null) {
						this._Atts.add(att);
					}
				} else if (mpart.isMimeType("multipart/*")) {
					saveAttachMent(mpart);
				} else {
					fileName = mpart.getFileName();
					if ((fileName != null)
							&& (fileName.toLowerCase().indexOf("GB2312") != -1)) {
						fileName = MimeUtility.decodeText(fileName);
						saveFile(fileName, mpart.getInputStream());
					}
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			saveAttachMent((Part) part.getContent());
		}
	}

	/**
	 * 获取保存的附件
	 * 
	 * @param mpart
	 * @return
	 * @throws Exception
	 */
	private Attachment saveAttachmentToFile(BodyPart mpart) throws Exception {
		String disposition = mpart.getDisposition();
		String fileName = mpart.getFileName();
		if (fileName == null) {
			return null;
		}
		if (fileName.indexOf("?=") > -1) {
			fileName = MimeUtility.decodeText(fileName);
		}
		String ext = UFile.getFileExt(fileName);

		String savedName = Utils.getGuid() + "." + ext;

		Attachment att = new Attachment();
		att.setAttachName(fileName);
		att.setSaveName(savedName);
		att.setSavePath(this.saveAttachPath);

		if (disposition.equals(Part.INLINE)) {
			String[] hhs = mpart.getHeader("Content-ID");
			if (hhs != null && hhs.length > 0) {
				att.setInlineId(hhs[0]);
			} else {
				att.setInlineId("not found");
			}
			att.setIsInline(true);
		}
		att.setSize(mpart.getInputStream().available());
		String rst = saveFile(savedName, mpart.getInputStream());
		att.setSavePathAndName(rst);
		return att;
	}

	/**
	 * 【获得附件存放路径】
	 */
	public String getAttachPath() {
		return saveAttachPath;
	}

	/**
	 * 获取邮件正文 HTML
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getBodyHtml() throws Exception {
		if (this.bodytext == null) {
			Part part = (Part) this.mimeMessage;
			this.getMailContent(part);
		}
		return this.bodyHtml.toString();
	}

	/**
	 * 获取邮件正文 纯文本
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	public String getBodyText() throws Exception {
		if (this.bodytext == null) {
			Part part = (Part) this.mimeMessage;
			this.getMailContent(part);
		}
		return bodytext.toString();
	}

	/**
	 * 【真正的保存附件到指定目录里】
	 */
	private String saveFile(String fileName, InputStream in) throws Exception {
		String osName = System.getProperty("os.name");
		String storedir = getAttachPath();
		UFile.buildPaths(storedir);
		String separator = "";
		if (osName == null)
			osName = "";
		if (osName.toLowerCase().indexOf("win") != -1) {
			separator = "\\";
			if (storedir == null || storedir.equals(""))
				storedir = "c:\\tmp";
		} else {
			separator = "/";
			if (storedir == null || storedir.equals(""))
				storedir = "/tmp";
		}

		File storefile = new File(storedir + separator + fileName);

		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(storefile));
			bis = new BufferedInputStream(in);
			int c;
			byte[] buf = new byte[40960];
			while ((c = bis.read(buf)) != -1) {
				bos.write(buf, 0, c);
				bos.flush();
			}
			return storefile.getAbsolutePath();
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new Exception("文件保存失败!");
		} finally {
			bos.close();
			bis.close();
		}
	}

	/**
	 * PraseMimeMessage类测试
	 */
	public static void main(String args[]) throws Exception {
		String host = "192.168.1.10";// 【POP3.163.com】
		String username = "root@192.168.1.10";// 【yuxia2217】
		String password = "brysj!@hhrhl";// 【........】
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("pop3");
		store.connect(host, username, password);
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);
		Message message[] = folder.getMessages();
		System.out.println("Messages's　length:　" + message.length);
		MailDecode pmm = null;
		for (int i = 0; i < message.length; i++) {
			pmm = new MailDecode((MimeMessage) message[i], "D:\\image");
			System.out
					.println("Message　" + i + "　subject:　" + pmm.getSubject());
			System.out.println("Message　" + i + "　sentdate:　"
					+ pmm.getSentDate());
			System.out.println("Message　" + i + "　replysign:　"
					+ pmm.getReplySign());
			System.out.println("Message　新的" + i + "　hasRead:　" + pmm.isNew());
			System.out.println("Message　附件" + i + "　　containAttachment:　"
					+ pmm.isContainAttach((Part) message[i]));
			System.out.println("Message　" + i + "　form:　" + pmm.getFrom());
			System.out.println("Message　" + i + "　to:　"
					+ pmm.getMailAddress("to"));
			System.out.println("Message　" + i + "　cc:　"
					+ pmm.getMailAddress("cc"));
			System.out.println("Message　" + i + "　bcc:　"
					+ pmm.getMailAddress("bcc"));
			System.out.println("Message" + i + "　sentdate:　"
					+ pmm.getSentDate());
			System.out.println("Message　" + i + "　Message-ID:　"
					+ pmm.getMessageId());
			System.out.println("Message　正文" + i + "　bodycontent:　\r\n"
					+ pmm.getBodyText());

			pmm.saveAttachments();

			List<Attachment> atts = pmm.getAtts();
			for (int k = 0; k < atts.size(); k++) {
				Attachment att = atts.get(k);
				System.out.println(att.toString());
			}
		}
	}

}
