/**
 * 
 */
package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;

/**
 * 页面权限接口2，提供了检查Action是否允许执行<br>
 * Page permission interface 2, which provides to check whether the Action is
 * allowed to execute
 */
public interface IAcl2 {
	/**
	 * return the parent object HtmlCreator
	 * 
	 * @return the parent object HtmlCreator
	 */
	public HtmlCreator getHtmlCreator();

	/**
	 * set the parent object HtmlCreator
	 * 
	 * @param htmlCreator the htmlCreator to set
	 */
	public void setHtmlCreator(HtmlCreator htmlCreator);
	
	public String getXmlName();

	/**
	 * @param xmlName the _XmlName to set
	 */
	public void setXmlName(String xmlName);

	/**
	 * @return the _ItemName
	 */
	public String getItemName();

	/**
	 * @param itemName the _ItemName to set
	 */
	public void setItemName(String itemName);

	
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


	public RequestValue getRequestValue();

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
