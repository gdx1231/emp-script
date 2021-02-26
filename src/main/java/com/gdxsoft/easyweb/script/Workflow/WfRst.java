package com.gdxsoft.easyweb.script.Workflow;

/**
 * 流程执行结果
 * @author Administrator
 *
 */
public class WfRst {
	private boolean _IsOk;
	private String _Msg;
	private boolean _IsException;
	private String _Exception;
	private WfUnit _Unit;
	/**
	 * @return the _IsOk
	 */
	public boolean isOk() {
		return _IsOk;
	}
	/**
	 * @param isOk the _IsOk to set
	 */
	public void setIsOk(boolean isOk) {
		_IsOk = isOk;
	}
	/**
	 * @return the _Msg
	 */
	public String getMsg() {
		return _Msg;
	}
	/**
	 * @param msg the _Msg to set
	 */
	public void setMsg(String msg) {
		_Msg = msg;
	}
	/**
	 * @return the _IsException
	 */
	public boolean isException() {
		return _IsException;
	}
	/**
	 * @param isException the _IsException to set
	 */
	public void setIsException(boolean isException) {
		_IsException = isException;
	}
	/**
	 * @return the _Exception
	 */
	public String getException() {
		return _Exception;
	}
	/**
	 * @param exception the _Exception to set
	 */
	public void setException(String exception) {
		_Exception = exception;
	}
	/**
	 * @return the _Unit
	 */
	public WfUnit getUnit() {
		return _Unit;
	}
	/**
	 * @param unit the _Unit to set
	 */
	public void setUnit(WfUnit unit) {
		_Unit = unit;
	}
}
