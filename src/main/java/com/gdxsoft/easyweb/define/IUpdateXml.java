package com.gdxsoft.easyweb.define;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface IUpdateXml {
	/**
	 * 导入xml配置
	 * 
	 * @param path
	 *            路径
	 * @param name
	 *            xml名称
	 * @param sourceXmlFilePath
	 *            要导入的xml文件
	 * @return
	 */
	JSONObject importXml(String path, String name, String sourceXmlFilePath);

	/**
	 * 获取文档的xml字符串
	 * 
	 * @return
	 */
	String getDocXml();

	/**
	 * 设置管理员
	 * 
	 * @param amdId
	 */
	void setAdmin(String amdId);

	/**
	 * 递归删除备份文件
	 * 
	 * @param path
	 * @return
	 */
	int deleteBaks(String xmlname);

	/**
	 * 获取管理员
	 * 
	 * @return
	 */
	String getAdmin();

	String getSqls();

	boolean rename(String itemName, String newItemName);

	boolean updateItem(String itemName, String xml);

	boolean updateItem(String itemName, String xml, boolean isUpdateTime);

	boolean saveXml(String itemName, String xml);

	boolean writeXml(Document doc);

	boolean removeItem(String itemName);

	boolean removeItems(String itemNames);

	boolean removeItem(String itemName, boolean isWrite);

	/**
	 * 获取当前的Item
	 * 
	 * @param itemName
	 *            item名字
	 * @return item节点
	 */
	Node queryItem(String itemName);

	/**
	 * 获取配置信息
	 * 
	 * @param itemName
	 * @return
	 */
	String queryItemXml(String itemName);

	boolean copyItem(String souceItemName, String newItemName);

	/**
	 * @return the _FrameType
	 */
	String getFrameType();

	/**
	 * @param frameType
	 *            the _FrameType to set
	 */
	void setFrameType(String frameType);

	void saveBackup();

	void recoverFile();

	void updateDescription(String itemName, String des);

	void batchUpdate(String itemNames, String paraName, String paraValue);

}