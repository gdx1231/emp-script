package com.gdxsoft.easyweb.cache;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.userConfig.IConfig;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfigOperation;
import com.gdxsoft.easyweb.script.userConfig.ScriptPath;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFileCheck;

/**
 * 配置信息状态（文件或数据库）
 * 
 * @author admin
 *
 */
public class ConfigStatus implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2194022825597581140L;

	private static Logger LOGGER = LoggerFactory.getLogger(ConfigStatus.class);

	private boolean jdbc;
	private boolean file;
	private boolean resource;
	private long _lastModified;
	private long _length;
	private String fixedXmlName;

	private String md5;

	private IConfig configType;

	/**
	 * 初始化，用于数据库返回数据
	 */
	public ConfigStatus() {

	}

	public ConfigStatus(IConfig configType) {
		this.configType = configType;
		if (configType.getScriptPath().isJdbc()) {
			this.initByJdbc();
		} else if (configType.getScriptPath().isResources()) {
			this.initByResource();
		} else {
			this.initByFile();
		}
		this.fixedXmlName = this.configType.getFixedXmlName();
	}

	private void initByFile() {
		File file = new File(this.configType.getPath());
		this._length = file.length();
		this._lastModified = file.lastModified();

		this.file = true;
		this.md5 = ""; // 避免计算压力，不验证文件md5
	}

	private void initByJdbc() {
		this.jdbc = true;
		ScriptPath sp = configType.getScriptPath();
		JdbcConfigOperation op = new JdbcConfigOperation(sp);

		// only the total XML meta
		DTTable tb = op.getXmlMeta(configType.getXmlName(), null);

		if (tb.getCount() == 0) {
			LOGGER.error("Not found configure " + configType.getXmlName());
			return;
		}

		// HASH_CODE, UPDATE_DATE, MD5, DATASOURCE, CLASS_ACL, CLASS_LOG, ADM_LID
		int haseCode = tb.getCell(0, 0).toInt();
		this.setLength(haseCode);

		if (tb.getCell(0, 1).isNull()) {
			this._lastModified = 0;
		} else {
			this._lastModified = tb.getCell(0, 1).toTime();
		}
		try {
			this.md5 = tb.getCell(0, "md5").toString();
		} catch (Exception e) {
			this.md5 = "";
			LOGGER.warn(e.getLocalizedMessage());
		}
	}

	private void initByResource() {
		this.resource = true;
		this.md5 = "1231";
		this._lastModified = 1231;
		this._length = 1231;

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
		if (this.resource) {
			// the resource configuration can't be modified
			return false;
		}
		int fileCode = getFileCode();
		int statusCode = this.getStatusCode().hashCode();

		rst = UFileCheck.isChanged(fileCode, statusCode, UserConfig.CHECK_CHANG_SPAN_SECONDS);

		return rst;
	}

	public int getFileCode() {
		return this.fixedXmlName.hashCode();
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
	 * The fixed XML name
	 * 
	 * @return the fixedXmlName
	 */
	public String getFixedXmlName() {
		return this.fixedXmlName;
	}

	/**
	 * The configuration last modified time
	 * 
	 * @return the _lastModified
	 */
	public long lastModified() {
		return _lastModified;
	}

	/**
	 * return whether it is a file type
	 * 
	 * @return the _isFile
	 */
	public boolean isFile() {
		return file;
	}

	/**
	 * return whether it is a jdbc type
	 * 
	 * @return
	 */
	public boolean isJdbc() {
		return jdbc;
	}

	/**
	 * return whether it is a resource type
	 * 
	 * @return
	 */
	public boolean isResource() {
		return resource;
	}

	/**
	 * 数据库返回数据的md5
	 * 
	 * @return the md5
	 */
	public String getMd5() {
		return md5;
	}

}
