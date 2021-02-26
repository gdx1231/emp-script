package com.gdxsoft.easyweb.utils.msnet;

import java.io.Serializable;

import com.gdxsoft.easyweb.utils.Utils;

/**
 * 兼容的StringBuilder 对象
 * 
 * @author Administrator
 * 
 */
public class MStr implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877502096998706509L;
	private StringBuilder _Sb;

	public MStr() {
		_Sb = new StringBuilder();
	}

	public int length() {
		return _Sb.length();
	}

	public MStr(Object val) {
		_Sb = new StringBuilder();
		_Sb.append(val);
	}

	public void setLength(int length) {
		_Sb.setLength(length);
	}

	public void reset() {
		_Sb.setLength(0);
	}

	/**
	 * 添加内容
	 * @param val
	 */
	public void a(Object val) {
		_Sb.append(val);
	}
	
	public void append(Object val) {
		_Sb.append(val);
	}

	public void appendLine(Object val) {
		_Sb.append(val);
		_Sb.append("\r\n");
	}

	/**
	 * 增加新行
	 * @param val
	 */
	public void al(Object val) {
		this.appendLine(val);
	}

	public int indexOf(String val) {
		return _Sb.indexOf(val);
	}

	public int indexOf(String val, int start) {
		return _Sb.indexOf(val, start);
	}

	public String substring(int startIndex) {
		return _Sb.substring(startIndex);
	}

	public String substring(int startIndex, int endIndex) {
		return _Sb.substring(startIndex, endIndex);
	}

	public void delete(int startIndex, int endIndex) {
		_Sb.delete(startIndex, endIndex);
	}

	public void insert(int index, Object val) {
		_Sb.insert(index, val);
	}

	public void replace(String find, String val) {
		Utils.replaceStringBuilder(_Sb, find, val);
	}

	public String toString() {
		return _Sb.toString();
	}
}
