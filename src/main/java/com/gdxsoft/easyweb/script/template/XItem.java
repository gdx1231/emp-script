package com.gdxsoft.easyweb.script.template;

/**
 * @author Administrator
 * 
 */
public class XItem {
	private String _Name; // 名称��ʽ
	private String _HtmlTag; // 类型����
	private String[] _Parameters; // 涉及参数����
	private Descriptions _Descriptions; // 描述���
	private String _TemplateHtml; // 模板Html
	private String _TemplateRepeat;// 重复部分
	private boolean _IsRepeat;// 是否重复
	private String _ClassName;

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name
	 *            the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _HtmlTag
	 */
	public String getHtmlTag() {
		return _HtmlTag;
	}

	/**
	 * @param htmlTag
	 *            the _HtmlTag to set
	 */
	public void setHtmlTag(String htmlTag) {
		_HtmlTag = htmlTag;
	}

	/**
	 * @return the _Parameters
	 */
	public String[] getParameters() {
		return _Parameters;
	}

	/**
	 * @param parameters
	 *            the _Parameters to set
	 */
	public void setParameters(String[] parameters) {
		_Parameters = parameters;
	}

	/**
	 * @return the _Descriptions
	 */
	public Descriptions getDescriptions() {
		return _Descriptions;
	}

	/**
	 * @param descriptions
	 *            the _Descriptions to set
	 */
	public void setDescriptions(Descriptions descriptions) {
		_Descriptions = descriptions;
	}

	/**
	 * @return the _TemplateHtml
	 */
	public String getTemplateHtml() {
		return _TemplateHtml;
	}

	/**
	 * @param templateHtml
	 *            the _TemplateHtml to set
	 */
	public void setTemplateHtml(String templateHtml) {
		_TemplateHtml = templateHtml;
	}

	/**
	 * @return the _TemplateRepeat
	 */
	public String getTemplateRepeat() {
		return _TemplateRepeat;
	}

	/**
	 * @param templateRepeat
	 *            the _TemplateRepeat to set
	 */
	public void setTemplateRepeat(String templateRepeat) {
		_TemplateRepeat = templateRepeat;
	}

	/**
	 * @return the _IsRepeat
	 */
	public boolean isRepeat() {
		return _IsRepeat;
	}

	/**
	 * @param isRepeat
	 *            the _IsRepeat to set
	 */
	public void setIsRepeat(boolean isRepeat) {
		_IsRepeat = isRepeat;
	}

	/**
	 * 映射的类名称
	 * 
	 * @return the _ClassName
	 */
	public String getClassName() {
		return _ClassName;
	}

	/**
	 * 设置映射的类名称
	 * 
	 * @param className
	 *            the _ClassName to set
	 */
	public void setClassName(String className) {
		_ClassName = className;
	}

}
