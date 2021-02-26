package com.gdxsoft.easyweb.define;

import java.io.File;
import java.util.Date;

/**
 * 
 */

/**
 * @author admin
 * 
 */
public class SyncFile {

	private Date _Date;
	private String _Name;

	public String getName() {
		return _Name;
	}

	public String getPath() {
		return _Path;
	}

	public long getLength() {
		return _Length;
	}

	private String _Path;
	private long _Length;

	/* 获取修改日期 */
	public java.util.Date getDate() {
		return this._Date;
	}

	public SyncFile(File f) {
		this._Length = f.length();
		this._Name = f.getName();
		this._Path = f.getAbsolutePath();
		this._Date = new Date(f.lastModified());
	}

}
