/**
 * 
 */
package com.gdxsoft.easyweb.acl;

import com.gdxsoft.easyweb.script.RequestValue;

/**
 * @author Administrator
 * 
 */
public interface IAcl {

	String getDenyMessage();
	
	public boolean canRun();

	public String getXmlName();

	/**
	 * @param xmlName
	 *            the _XmlName to set
	 */
	public void setXmlName(String xmlName);

	/**
	 * @return the _ItemName
	 */
	public String getItemName();

	/**
	 * @param itemName
	 *            the _ItemName to set
	 */
	public void setItemName(String itemName);

	public void setRequestValue(RequestValue requestValue);
	
	public RequestValue getRequestValue();
	
	public String getGoToUrl();
	
	public void setGoToUrl(String url);
}
