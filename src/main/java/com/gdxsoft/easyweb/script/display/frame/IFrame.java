package com.gdxsoft.easyweb.script.display.frame;

import java.util.HashMap;

import com.gdxsoft.easyweb.datasource.PageSplit;
import com.gdxsoft.easyweb.script.display.HtmlClass;

public interface IFrame {
	public abstract String createJsonJs();

	public abstract String createaXmlData() throws Exception;

	public abstract HtmlClass getHtmlClass();

	public abstract void setHtmlClass(HtmlClass htmlClass);

	/**
	 * 获取ListFrame分页
	 * 
	 * @return
	 */
	public abstract PageSplit getPageSplit();

	/**
	 * 生成页面的JSON数据
	 * 
	 * @return
	 * @throws Exception
	 */
	public String createJsonContent() throws Exception;

	/**
	 * 获取工作流的js表达式
	 * 
	 * @return the _WorkFlowBut
	 */
	public abstract String getWorkFlowButJson();

	/**
	 * 配置文件的对象的 JSON表达式(在FrameBase中生成)
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract String createJsonFrame() throws Exception;

	/**
	 * item描述XML字符串
	 * 
	 * @return
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
	 * @return
	 * @throws Exception
	 */
	public abstract void createJsFramePage() throws Exception;

	/**
	 * 仅生成内容HTML
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void createContent() throws Exception;

	/**
	 * 生成CSS内容
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void createCss() throws Exception;

	/**
	 * 生成头部Js
	 * 
	 * @return
	 */
	public abstract void createJsTop();

	/**
	 * 生成底部Js
	 * 
	 * @return
	 */
	public abstract void createJsBottom();

	/**
	 * 生成主底部
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void createSkinBottom() throws Exception;

	/**
	 * 生成主头部
	 * 
	 * @return
	 * @throws Exception
	 */
	public void createSkinTop() throws Exception;

	/**
	 * 生成Frame全部HTML
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void createHtml() throws Exception;

	/**
	 * 生成Frame页头
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract String createFrameHeader() throws Exception;

	/**
	 * 生成Frame内容
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void createFrameContent() throws Exception;

	/**
	 * 生成页面的所有元素
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract String createItemHtmls() throws Exception;

	/**
	 * 生成Frame页脚
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void createFrameFooter() throws Exception;

	/**
	 * @return the _ItemParentHtmls
	 */
	public abstract HashMap<String, String> getItemParentHtmls();

	/**
	 * @param itemParentHtmls
	 *            the _ItemParentHtmls to set
	 */
	public abstract void setItemParentHtmls(HashMap<String, String> itemParentHtmls);

	public abstract String createSelectReload() throws Exception;

}