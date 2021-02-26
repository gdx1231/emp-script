package com.gdxsoft.easyweb.define;

import java.io.File;

public class Dir {

	private String _path;

	private String _parentPath;

	private String _Name;
	
	private String _Ext;
	
	private boolean _IsFile;

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _path
	 */
	public String getPath() {
		return _path;
	}

	/**
	 * @param _path
	 *            the _path to set
	 */
	public void setPath(String _path) {
		this._path = _path;
	}

	/**
	 * @return the _parentPath
	 */
	public String getParentPath() {
		return _parentPath;
	}

	/**
	 * @param path
	 *            the _parentPath to set
	 */
	public void setParentPath(String path) {
		_parentPath = path;
	}

	public Dir() {

	}

	public Dir(String name, String path, String parentPath,boolean isFile) {
		_Name = name;
		_path = path;
		this._parentPath = parentPath;
		this._Ext=Dir.getFileExt(_Name);
		this._IsFile=isFile;
		
	}

	public Dir(File file) {
		_Name = file.getName();
		_path = file.getPath();
		_parentPath = file.getParentFile().getPath();
		this._Ext=Dir.getFileExt(_Name);
		this._IsFile=file.isFile();
	}

	/**
	 * @return the _Ext
	 */
	public String getExt() {
		return _Ext;
	}

	/**
	 * @param ext the _Ext to set
	 */
	public void setExt(String ext) {
		_Ext = ext;
	}

	/**
	 * @return the _IsFile
	 */
	public boolean isFile() {
		return _IsFile;
	}

	/**
	 * @param isFile the _IsFile to set
	 */
	public void setFile(boolean isFile) {
		_IsFile = isFile;
	}

	public static String getFileExt(String fileName){
		int m=fileName.lastIndexOf(".");
		if(m<0){
			return "";
		}
		String ext=fileName.substring(m);
		return ext;
	}
}
