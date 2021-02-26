package com.gdxsoft.easyweb.utils.Mail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class CustomMimeMessage extends MimeMessage {
	private String oldMessageId;

	public CustomMimeMessage(Session session) {
		super(session);
	}

	public CustomMimeMessage(Session session, java.io.InputStream is) throws MessagingException {
		super(session, is);
	}
	 
	@Override
	protected void updateMessageID() throws MessagingException {
		if (this.oldMessageId != null) {
			setHeader("Message-ID", oldMessageId);
		} else {
			super.updateMessageID();
		}
	}

	/**
	 * @return the oldMessageId
	 */
	public String getOldMessageId() {
		return oldMessageId;
	}

	/**
	 * @param oldMessageId
	 *            the oldMessageId to set
	 */
	public void setOldMessageId(String oldMessageId) {
		this.oldMessageId = oldMessageId;
	}

}