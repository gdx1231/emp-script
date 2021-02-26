package com.gdxsoft.easyweb.utils.Mail;

/**
 * 邮件附件
 * 
 * @author Administrator
 * 
 */
public class Attachment {

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("附件名：" + this._AttachName + ", 保存地址：" + this._SavePathAndName
				+ ", 内联:" + this._IsInline);

		return sb.toString();

	}

	/**
	 * @return the _AttachName
	 */
	public String getAttachName() {
		return _AttachName;
	}

	/**
	 * @param attachName
	 *            the _AttachName to set
	 */
	public void setAttachName(String attachName) {
		_AttachName = attachName;
	}

	/**
	 * @return the _SavePathAndName
	 */
	public String getSavePathAndName() {
		return _SavePathAndName;
	}

	/**
	 * @param savePathAndName
	 *            the _SavePathAndName to set
	 */
	public void setSavePathAndName(String savePathAndName) {
		_SavePathAndName = savePathAndName;
	}

	/**
	 * @return the _SavePath
	 */
	public String getSavePath() {
		return _SavePath;
	}

	/**
	 * @param savePath
	 *            the _SavePath to set
	 */
	public void setSavePath(String savePath) {
		_SavePath = savePath;
	}

	/**
	 * @return the _SaveName
	 */
	public String getSaveName() {
		return _SaveName;
	}

	/**
	 * @param saveName
	 *            the _SaveName to set
	 */
	public void setSaveName(String saveName) {
		_SaveName = saveName;
	}

	/**
	 * @return the _IsInline
	 */
	public boolean isInline() {
		return _IsInline;
	}

	/**
	 * @param isInline
	 *            the _IsInline to set
	 */
	public void setIsInline(boolean isInline) {
		_IsInline = isInline;
	}

	/**
	 * @return the _InlineId
	 */
	public String getInlineId() {
		return _InlineId;
	}

	/**
	 * @param inlineId
	 *            the _InlineId to set
	 */
	public void setInlineId(String inlineId) {
		_InlineId = inlineId.replace("<", "").replace(">", "");
	}

	/**
	 * @return the _Size
	 */
	public int getSize() {
		return _Size;
	}

	/**
	 * @param size
	 *            the _Size to set
	 */
	public void setSize(int size) {
		_Size = size;
	}

	private String _AttachName;
	private String _SavePathAndName;
	private String _SavePath;
	private String _SaveName;

	private boolean _IsInline;
	private String _InlineId;
	private int _Size;

}
