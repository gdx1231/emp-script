package com.gdxsoft.easyweb.script.display.action;

/**
 * 调用执行结果
 * @author Administrator
 *
 */
public class ActionResult {

	private boolean _IsOk;
	private String _Msg;
	private Object _Object;
	private boolean _IsException;
	private String _ExceptionMsg;
	/**
	 * 是否成功
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
	 * 返回消息
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
	 * 返回对象
	 * @return the _Object
	 */
	public Object getObject() {
		return _Object;
	}
	/**
	 * 
	 * @param object the _Object to set
	 */
	public void setObject(Object object) {
		_Object = object;
	}
	/**
	 * 是否运行错误
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
	 * 执行异常信息
	 * @return the _ExceptionMsg
	 */
	public String getExceptionMsg() {
		return _ExceptionMsg;
	}
	/**
	 * @param exceptionMsg the _ExceptionMsg to set
	 */
	public void setExceptionMsg(String exceptionMsg) {
		_ExceptionMsg = exceptionMsg;
	}
	
	
}
