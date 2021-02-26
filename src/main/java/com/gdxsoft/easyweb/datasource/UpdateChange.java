package com.gdxsoft.easyweb.datasource;

import com.gdxsoft.easyweb.data.DTColumn;

/**
 * 更新的字段
 * 
 * @author admin 2018-12-03
 */
public class UpdateChange {
	private DTColumn col_;
	private Object before_;
	private Object after_;

	/**
	 * 字段
	 * 
	 * @return
	 */
	public DTColumn getCol() {
		return col_;
	}

	/**
	 * 字段
	 * 
	 * @param col
	 */
	public void setCol(DTColumn col) {
		this.col_ = col;
	}

	/**
	 * 更新前
	 * 
	 * @return
	 */
	public Object getBefore() {
		return before_;
	}

	/**
	 * 更新前
	 * 
	 * @param before
	 */
	public void setBefore(Object before) {
		this.before_ = before;
	}

	/**
	 * 更新后
	 * 
	 * @return
	 */
	public Object getAfter() {
		return after_;
	}

	/**
	 * 更新后
	 * 
	 * @param after
	 */
	public void setAfter(Object after) {
		this.after_ = after;
	}
}
