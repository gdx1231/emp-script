/**
 * 
 */
package com.gdxsoft.easyweb.debug;

import java.util.Date;


/**用于跟踪执行的路径
 * @author Administrator
 *
 */
public class DebugFrame {

	private Date _DebugDate;
	private String _ClassName;
	private String _EventName;
	private String _Desscription;
	private long _CurrentTime;
	
	public DebugFrame(){
		this.init();
	}
	
	public DebugFrame(Object fromClass,String eventName,String description ){
		this.init();
		this._Desscription=description;
		this._EventName=eventName;
		this.setClassName(fromClass);
	}
	
	private void init(){
		this._CurrentTime=System.currentTimeMillis();
		_DebugDate=new Date();
	}
	
	/**
	 * @return the _DebugDate
	 */
	public Date getDebugDate() {
		return _DebugDate;
	}
	 
	/**
	 * @return the _ClassName
	 */
	public String getClassName() {
		return _ClassName;
	}
	/**
	 * @param className the _ClassName to set
	 */
	public void setClassName(Object classObject) {
		_ClassName = classObject.getClass().getName();
	}
	/**
	 * @return the _EventName
	 */
	public String getEventName() {
		return _EventName;
	}
	/**
	 * @param eventName the _EventName to set
	 */
	public void setEventName(String eventName) {
		_EventName = eventName;
	}
	/**
	 * @return the _Desscription
	 */
	public String getDesscription() {
		return _Desscription;
	}
	/**
	 * @param desscription the _Desscription to set
	 */
	public void setDesscription(String desscription) {
		_Desscription = desscription;
	}
	/**
	 * @return the _CurrentTime
	 */
	public long getCurrentTime() {
		return _CurrentTime;
	}
}
