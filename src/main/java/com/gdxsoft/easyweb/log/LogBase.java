package com.gdxsoft.easyweb.log;

import com.gdxsoft.easyweb.script.display.HtmlCreator;

public class LogBase {
	
	private HtmlCreator _Creator;
	
	private Log _Log;
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.log.ILog#getLog()
	 */
	public Log getLog() {
		return _Log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.log.ILog#setLog(com.gdxsoft.easyweb.log.Log)
	 */
	public void setLog(Log log) {
		_Log = log;
	}

	/**
	 * 获取创建类
	 * @return
	 */
	public HtmlCreator getCreator() {
		return _Creator;
	}

	/**
	 * 设置创建类
	 * @param creator
	 */
	public void setCreator(HtmlCreator creator) {
		this._Creator = creator;
	}

}
