package com.gdxsoft.easyweb.conf;

import com.gdxsoft.easyweb.utils.Utils;

/**
 * 附加资源
 * 
 * @author admin
 *
 */
public class ConfAddedResource {
	private String src;
	private String name;
	private String xml;
	private boolean last;
	private boolean defaultConf;
	private String content; // 资源内容

	private String resourceType;

	/**
	 * 资源类型，css、js、html
	 * 
	 * @return
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * 资源类型，css、js、html
	 * 
	 * @param resourceType
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String toString() {
		if ("css".equalsIgnoreCase(this.getResourceType())) {
			return toCss();
		} else if ("html".equalsIgnoreCase(this.getResourceType())) {
			return toHtml();
		} else {
			return toJs();
		}
	}

	public String toHtml() {
		return this.content == null ? "" : this.content;
	}

	/**
	 * 转成JS表达式
	 * 
	 * @return
	 */
	public String toJs() {

		StringBuilder sb = new StringBuilder();
		sb.append("<script type=\"text/javascript\" id=\"ewa_resource_");
		sb.append(Utils.textToInputValue(this.name)).append("\" ");
		if (src != null && src.trim().length() > 0) {
			sb.append("src=\"").append(Utils.textToInputValue(this.src)).append("\">");
		} else {
			sb.append(">").append(this.content == null ? "" : this.content);
		}
		sb.append("</script>");
		return sb.toString();
	}

	/**
	 * 转成CSS表达式
	 * 
	 * @return
	 */
	public String toCss() {
		StringBuilder sb = new StringBuilder();
		if (src != null && src.trim().length() > 0) {
			sb.append("<link rel=\"stylesheet\" id=\"ewa_resource_");
			sb.append(Utils.textToInputValue(this.name));
			sb.append("\" href=\"");
			sb.append(Utils.textToInputValue(this.src) + "\">");
		} else {
			sb.append("<style id=\"ewa_resource_");
			sb.append(Utils.textToInputValue(this.name));
			sb.append("\">");
			sb.append(this.content == null ? "" : this.content);
			sb.append("<style>");
		}
		return sb.toString();
	}

	/**
	 * 资源地址，相对或绝对
	 * 
	 * @return
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * 资源地址，相对或绝对
	 * 
	 * @param src
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	/**
	 * 资源名称，唯一
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 资源名称，唯一
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 配置XML信息
	 * 
	 * @return
	 */
	public String getXml() {
		// <addedResource src="/static/add_0.js" name="addjs0"></addedResource>
		return xml;
	}

	/**
	 * 配置XML信息
	 * 
	 * @param xml
	 */
	public void setXml(String xml) {
		// <addedResource src="/static/add_0.js" name="addjs0"></addedResource>
		this.xml = xml;
	}

	/**
	 * 是否放到html页面的最后
	 * 
	 * @return
	 */
	public boolean isLast() {
		return last;
	}

	/**
	 * 是否放到html页面的最后
	 * 
	 * @param last
	 */
	public void setLast(boolean last) {
		this.last = last;
	}

	/**
	 * 是否为默认附加的资源，当 ewa_added_resources未传递时
	 * 
	 * @return
	 */
	public boolean isDefaultConf() {
		return defaultConf;
	}

	public void setDefaultConf(boolean defaultConf) {
		this.defaultConf = defaultConf;
	}

	/**
	 * 资源内容，和src互斥
	 * 
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;

	}

	public String getContent() {
		return content;
	}
}
