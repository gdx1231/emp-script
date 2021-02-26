package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;

public class SampleAcl2 implements IAcl2 {

	private RequestValue _RequestValue;
	private String _GoToUrl; //验证失败跳转页面
	public boolean canRun() {
		return true;
	}

	public SampleAcl2() {

	}
	 
	public void setRequestValue(RequestValue requestValue) {
		this._RequestValue = requestValue;

	}

	public RequestValue getRequestValue() {
		return _RequestValue;
	}

	/**
	 * @return the _GoToUrl
	 */
	public String getGoToUrl() {
		return _GoToUrl;
	}

	public boolean canRunAction(String actionName) {
		if(actionName.equalsIgnoreCase("OnPageLoad")){
			return true;
		}
		return false;
	}

	public String getNotRunTitle() {
		return "NOT ALLOW RUN!";
	}
 
 
 
 

}
