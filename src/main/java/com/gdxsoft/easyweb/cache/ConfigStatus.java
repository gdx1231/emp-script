package com.gdxsoft.easyweb.cache;

import java.io.File;

import com.gdxsoft.easyweb.utils.UFileCheck;

/**
 * 配置信息状态（文件或数据库）
 * 
 * @author admin
 *
 */
public class ConfigStatus {

	private boolean _isFile;
	private long _lastModified;
	private long _length;
	private String _absolutePath;
	private File _file;

	private String md5;

	/**
	 * 初始化，用于数据库返回数据
	 */
	public ConfigStatus() {

	}

	/**
	 * 按文件初始化
	 * 
	 * @param file
	 */
	public ConfigStatus(File file) {
		this._file = file;
		this._length = file.length();
		this._lastModified = file.lastModified();
		this._absolutePath = file.getAbsolutePath();
		this._isFile = true;
		this.md5 = ""; // 避免计算压力，不验证文件md5
	}

	/**
	 * 用于文件或数据库返回的xml数据的状态code<br>
	 * _lastModified + "," + _length + "," + md5
	 * 
	 * @return code(_lastModified + "," + _length + "," + md5)
	 */
	public String getStatusCode() {
		String code = _lastModified + "," + _length + "," + md5;
		return code;
	}

	/**
	 * 是否文件或数据库信息发生变化
	 * 
	 * @return
	 */
	public boolean isChanged() {
		boolean rst;
		if (this.isFile()) {
			rst = UFileCheck.fileChanged(this.getAbsolutePath());
		} else {
			int id = this._absolutePath.hashCode();
			// 对应数据库的配置，长度放 xml 字符串的 hash
			int hashCode = Integer.parseInt(this._length + "");
			rst = UFileCheck.isChanged(id, hashCode, 5);
		}

		return rst;
	}

	/**
	 * 文件或xml数据长度
	 * 
	 * @return
	 */
	public long length() {
		return _length;
	}

	/**
	 * 文件或xml数据长度
	 * 
	 * @param len
	 */
	public void setLength(int len) {
		this._length = len;
	}

	/**
	 * 文件路径，或数据库xmlname
	 * 
	 * @return
	 */
	public String getAbsolutePath() {
		return _absolutePath;
	}

	/**
	 * 文件路径，或数据库xmlname
	 * 
	 * @param absolutePath
	 */
	public void setAbsolutePath(String absolutePath) {
		this._absolutePath = absolutePath;
	}

	/**
	 * 文件上次更新时间
	 * 
	 * @return the _lastModified
	 */
	public long lastModified() {
		return _lastModified;
	}

	/**
	 * 文件上次更新时间
	 * 
	 * @param _lastModified the _lastModified to set
	 */
	public void setLastModified(long _lastModified) {
		this._lastModified = _lastModified;
	}

	/**
	 * 文件本身
	 * 
	 * @return the _file
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * 文件本身 是否是文件
	 * 
	 * @return the _isFile
	 */
	public boolean isFile() {
		return _isFile;
	}

	/**
	 * 是否文件
	 * 
	 * @param _isFile the _isFile to set
	 */
	public void setIsFile(boolean isFile) {
		this._isFile = isFile;
	}

	/**
	 * 数据库返回数据的md5
	 * 
	 * @return the md5
	 */
	public String getMd5() {
		return md5;
	}

	/**
	 * 数据库返回数据的md5
	 * 
	 * @param md5 the md5 to set
	 */
	public void setMd5(String md5) {
		this.md5 = md5;
	}
}
