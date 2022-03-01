/**
 * 
 */
package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.script.display.HtmlCreator;

/**
 * @author Administrator
 * 
 */
public interface IAcl {
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

	String getDenyMessage();

	public boolean canRun();

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

	public void setRequestValue(RequestValue requestValue);

	public RequestValue getRequestValue();

	public String getGoToUrl();

	public void setGoToUrl(String url);
}
