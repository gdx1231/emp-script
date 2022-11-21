package com.gdxsoft.easyweb.script.display.frame;

import java.util.HashMap;

import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.script.display.HtmlClass;
import com.gdxsoft.easyweb.utils.msnet.MTable;

public interface IFrame {
	 

	 
	
	FrameBase getFrameBase();
	 

	/**
	 * 需要隐藏的字段集合
	 * 
	 * @return the _HiddenFields
	 */
	public abstract MTable getHiddenFields();

	/**
	 * 需要隐藏的字段集合
	 * 
	 * @param hiddenFields the 需要隐藏的字段集合 to set
	 */
	public abstract void setHiddenFields(MTable hiddenFields);

	public abstract String createJsonJs();

	public abstract String createaXmlData() throws Exception;

	public abstract HtmlClass getHtmlClass();

	public abstract void setHtmlClass(HtmlClass htmlClass);

	/**
	 * 获取ListFrame分页<br>
	 * Get the listFrame page split
	 * 
	 * @return the page split
	 */
	public abstract PageSplit getPageSplit();

	/**
	 * 生成页面的JSON数据<br>
	 * Create the JSON String
	 * 
	 * @return the JSON String
	 * @throws Exception
	 */
	public String createJsonContent() throws Exception;

	/**
	 * 获取工作流的json表达式<br>
	 * Get the workflow json<br>
	 * 
	 * @return the WorkFlowBut
	 */
	public abstract String getWorkFlowButJson();

	/**
	 * 配置文件的对象的 JSON表达式(在FrameBase中生成)<br>
	 * JSON expression of the configuration item (generated in FrameBase)
	 * 
	 * @return the JSON expression
	 * @throws Exception
	 */
	public abstract String createJsonFrame() throws Exception;

	/**
	 * item描述XML字符串
	 * 
	 * @throws Exception
	 */
	public abstract void createJsFrameXml() throws Exception;

	/**
	 * 生成菜单的Js表达式
	 * 
	 * @throws Exception
	 */
	public abstract void createJsFrameMenu() throws Exception;

	/**
	 * 页面Js初始化
	 * 
	 * @throws Exception
	 */
	public abstract void createJsFramePage() throws Exception;

	/**
	 * 仅生成内容HTML
	 * 
	 * @throws Exception
	 */
	public abstract void createContent() throws Exception;

	/**
	 * 生成CSS内容
	 * 
	 * @throws Exception
	 */
	public abstract void createCss() throws Exception;

	/**
	 * 生成头部Js
	 */
	public abstract void createJsTop();

	/**
	 * 生成底部Js
	 * 
	 */
	public abstract void createJsBottom();

	/**
	 * 生成主底部
	 * 
	 * @throws Exception
	 */
	public abstract void createSkinBottom() throws Exception;

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
	public abstract void createHtml() throws Exception;

	/**
	 * 生成Frame页头
	 * 
	 * @return Frame页头
	 * @throws Exception
	 */
	public abstract String createFrameHeader() throws Exception;

	/**
	 * 生成Frame内容
	 * 
	 * @throws Exception
	 */
	public abstract void createFrameContent() throws Exception;

	/**
	 * 生成页面的所有元素
	 * 
	 * @return 所有元素
	 * @throws Exception
	 */
	public abstract String createItemHtmls() throws Exception;

	/**
	 * 生成Frame页脚
	 * 
	 * @throws Exception
	 */
	public abstract void createFrameFooter() throws Exception;

	/**
	 * @return the _ItemParentHtmls
	 */
	public abstract HashMap<String, String> getItemParentHtmls();

	/**
	 * @param itemParentHtmls the _ItemParentHtmls to set
	 */
	public abstract void setItemParentHtmls(HashMap<String, String> itemParentHtmls);

	public abstract String createSelectReload() throws Exception;

}