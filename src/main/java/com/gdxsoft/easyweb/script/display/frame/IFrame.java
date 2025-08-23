package com.gdxsoft.easyweb.script.display.frame;

import java.util.HashMap;
import java.util.Map;

import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public interface IFrame {

	FrameBase getFrameBase();

	/**
	 * 创建用于AI提示词的JSON格式的页面框架(2025-08-23)
	 * 
	 * @return JSON格式的页面框架
	 * @throws Exception
	 */
	String createJsonFrameAIPrompt() throws Exception;

	/**
	 * 固定查询的表，用于ListFrame
	 * 
	 * @return 固定查询的表
	 */
	Map<String, DTTable> getSearchFixTables();

	/**
	 * 检查是否为隐含字段，在Page的LogicShow中定义
	 * 
	 * @param name
	 * @return
	 */
	boolean isHiddenField(String name);

	/**
	 * 需要隐藏的字段集合
	 * 
	 * @return the _HiddenFields
	 */
	MTable getHiddenFields();

	/**
	 * 需要隐藏的字段集合
	 * 
	 * @param hiddenFields the 需要隐藏的字段集合 to set
	 */
	void setHiddenFields(MTable hiddenFields);

	String createJsonJs();

	String createaXmlData() throws Exception;

	HtmlClass getHtmlClass();

	void setHtmlClass(HtmlClass htmlClass);

	/**
	 * 获取ListFrame分页<br>
	 * Get the listFrame page split
	 * 
	 * @return the page split
	 */
	PageSplit getPageSplit();

	/**
	 * 创建JSON数据
	 * 
	 * @param skipUnDefined 是否跳过未定义的字段
	 * @return the JSON String
	 * @throws Exception
	 */
	String createJsonContent(boolean skipUnDefined) throws Exception;

	/**
	 * 生成页面的JSON数据<br>
	 * Create the JSON String
	 * 
	 * @return the JSON String
	 * @throws Exception
	 */
	String createJsonContent() throws Exception;

	/**
	 * 获取工作流的json表达式<br>
	 * Get the workflow json<br>
	 * 
	 * @return the WorkFlowBut
	 */
	String getWorkFlowButJson();

	/**
	 * 配置文件的对象的 JSON表达式(在FrameBase中生成)<br>
	 * JSON expression of the configuration item (generated in FrameBase)
	 * 
	 * @return the JSON expression
	 * @throws Exception
	 */
	String createJsonFrame() throws Exception;

	/**
	 * item描述XML字符串
	 * 
	 * @throws Exception
	 */
	void createJsFrameXml() throws Exception;

	/**
	 * 生成菜单的Js表达式
	 * 
	 * @throws Exception
	 */
	void createJsFrameMenu() throws Exception;

	/**
	 * 页面Js初始化
	 * 
	 * @throws Exception
	 */
	void createJsFramePage() throws Exception;

	/**
	 * 仅生成内容HTML
	 * 
	 * @throws Exception
	 */
	void createContent() throws Exception;

	/**
	 * 生成CSS内容
	 * 
	 * @throws Exception
	 */
	void createCss() throws Exception;

	/**
	 * 生成头部Js
	 */
	void createJsTop();

	/**
	 * 生成底部Js
	 * 
	 */
	void createJsBottom();

	/**
	 * 生成主底部
	 * 
	 * @throws Exception
	 */
	void createSkinBottom() throws Exception;

	/**
	 * 生成主头部
	 * 
	 * @throws Exception
	 */
	public void createSkinTop() throws Exception;

	/**
	 * 生成Frame全部HTML
	 * 
	 * @throws Exception
	 */
	void createHtml() throws Exception;

	/**
	 * 生成Frame页头
	 * 
	 * @return Frame页头
	 * @throws Exception
	 */
	String createFrameHeader() throws Exception;

	/**
	 * 生成Frame内容
	 * 
	 * @throws Exception
	 */
	void createFrameContent() throws Exception;

	/**
	 * 生成页面的所有元素
	 * 
	 * @return 所有元素
	 * @throws Exception
	 */
	String createItemHtmls() throws Exception;

	/**
	 * 生成Frame页脚
	 * 
	 * @throws Exception
	 */
	void createFrameFooter() throws Exception;

	/**
	 * @return the _ItemParentHtmls
	 */
	HashMap<String, String> getItemParentHtmls();

	/**
	 * @param itemParentHtmls the _ItemParentHtmls to set
	 */
	void setItemParentHtmls(HashMap<String, String> itemParentHtmls);

	String createSelectReload() throws Exception;

}