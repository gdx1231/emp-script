/**
 * 
 */
package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;

/**
 * 页面权限接口2，提供了检查Action是否允许执行
 * @author Administrator
 * 
 */
public interface IAcl2 {
	/**
	 * 页面是否可执行
	 * @return
	 */
	public boolean canRun();
	/**
	 * 设置参数
	 * @param requestValue
	 */
	public void setRequestValue(RequestValue requestValue);
	
	/**
	 * 跳转页面
	 * @return
	 */
	public String getGoToUrl();
	/**
	 * 是否可执行Action
	 * @param actionName
	 * @return
	 */
	public boolean canRunAction(String actionName);
	
	/**
	 * 获取不可执行的提示信息
	 * @return
	 */
	public String getNotRunTitle();
}
