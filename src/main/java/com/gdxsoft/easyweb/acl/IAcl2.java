/**
 * 
 */
package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;

/**
 * 页面权限接口2，提供了检查Action是否允许执行<br>
 * Page permission interface 2, which provides to check whether the Action is
 * allowed to execute
 */
public interface IAcl2 {
	/**
	 * 页面是否可执行<br>
	 * Whether the page can be execute
	 * 
	 * @return true: allow/false: deny
	 */
	public boolean canRun();

	/**
	 * 设置 RequestValue 参数<br>
	 * Set the requestValue
	 * 
	 * @param requestValue
	 */
	public void setRequestValue(RequestValue requestValue);

	/**
	 * 跳转页面
	 * 
	 * @return
	 */
	public String getGoToUrl();

	/**
	 * 是否可执行Action<br>
	 * Whether to execute the action
	 * 
	 * @param actionName the action name
	 * @return true: allow/ false: deny
	 */
	public boolean canRunAction(String actionName);

	/**
	 * 获取不可执行的提示信息
	 * 
	 * @return the not run title
	 */
	public String getNotRunTitle();
}
