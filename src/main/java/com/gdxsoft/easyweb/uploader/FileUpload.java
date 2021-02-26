package com.gdxsoft.easyweb.uploader;

import java.util.ArrayList;

public class FileUpload {

	private String _UserLocalPath; // 用户本地文件路径
	private String _SavePath; // 保存的路径
	private String _Ext; // 扩展名
	private String _SaveFileName; // 保存文件路径
	private String _FileUrl; // http的URL
	private String _Unid; // 全局编号
	private String _ContextType; // 上传文件类型
	private FileUpload _From;
	private ArrayList<FileUpload> _Subs = new ArrayList<FileUpload>();

	/**
	 * 子文件，解压的文件或新尺寸的图片文件
	 * @return
	 */
	public ArrayList<FileUpload> getSubs() {
		return _Subs;
	}

	private int _Length;

	/**
	 * @return the _UserLocalPath
	 */
	public String getUserLocalPath() {
		return _UserLocalPath;
	}

	/**
	 * @param userLocalPath
	 *            the _UserLocalPath to set
	 */
	public void setUserLocalPath(String userLocalPath) {
		_UserLocalPath = userLocalPath;
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
	 * @return the _Ext
	 */
	public String getExt() {
		return _Ext;
	}

	/**
	 * @param ext
	 *            the _Ext to set
	 */
	public void setExt(String ext) {
		_Ext = ext;
	}

	/**
	 * @return the _SaveFileName
	 */
	public String getSaveFileName() {
		return _SaveFileName;
	}

	/**
	 * @param saveFileName
	 *            the _SaveFileName to set
	 */
	public void setSaveFileName(String saveFileName) {
		_SaveFileName = saveFileName;
	}

	/**
	 * @return the _FileUrl
	 */
	public String getFileUrl() {
		return _FileUrl;
	}

	/**
	 * @param fileUrl
	 *            the _FileUrl to set
	 */
	public void setFileUrl(String fileUrl) {
		_FileUrl = fileUrl;
	}

	/**
	 * 全局编号
	 * 
	 * @return the _Unid
	 */
	public String getUnid() {
		return _Unid;
	}

	/**
	 * 全局编号
	 * 
	 * @param unid
	 *            the _Unid to set
	 */
	public void setUnid(String unid) {
		_Unid = unid;
	}

	/**
	 * 上传类型
	 * 
	 * @return the _ContextType
	 */
	public String getContextType() {
		return _ContextType;
	}

	/**
	 * 上传类型
	 * 
	 * @param contextType
	 *            the _ContextType to set
	 */
	public void setContextType(String contextType) {
		_ContextType = contextType;
	}

	/**
	 * @return the _From
	 */
	public FileUpload getFrom() {
		return _From;
	}

	/**
	 * @param from
	 *            the _From to set
	 */
	public void setFrom(FileUpload from) {
		_From = from;
	}

	/**
	 * @return the _Length
	 */
	public int getLength() {
		return _Length;
	}

	/**
	 * @param length
	 *            the _Length to set
	 */
	public void setLength(int length) {
		_Length = length;
	}

}
