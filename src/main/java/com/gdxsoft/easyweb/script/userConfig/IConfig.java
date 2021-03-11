package com.gdxsoft.easyweb.script.userConfig;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.gdxsoft.easyweb.conf.ConfScriptPath;

public interface IConfig extends Serializable {
	String getPath();

	/**
	 * Check if the configuration exists
	 * 
	 * @return
	 */
	boolean checkConfigurationExists();

	/**
	 * Check if the item in the configuration file
	 * 
	 * @return true/false
	 */
	boolean checkItemExists();

	/**
	 * return the item from the configuration file
	 * 
	 * @throws Exception
	 */
	Node loadItem() throws Exception;

	/**
	 * return the configuration XML document
	 * 
	 * @throws Exception
	 */
	Document loadConfiguration() throws Exception;

	/**
	 * Set the fixed XML name
	 * 
	 * @param fixedXmlName the fixed XML name
	 */
	void setFixedXmlName(String fixedXmlName);

	/**
	 * return the ScriptPath class
	 * 
	 * @return the ScriptPath class
	 */
	ConfScriptPath getScriptPath();

	/**
	 * return the fixed XML name
	 * 
	 * @return the fixed XML name
	 */
	String getFixedXmlName();

	String getXmlName();

	String getItemName();

	public void setScriptPath(ConfScriptPath scriptPath);

	public void setXmlName(String xmlName);

	public void setItemName(String itemName);

}
