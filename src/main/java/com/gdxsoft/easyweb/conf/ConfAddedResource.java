package com.gdxsoft.easyweb.conf;

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
	
	/**
	 * 转成JS表达式
	 * @return
	 */
	public String toJs() {
		StringBuilder sb = new StringBuilder();
		sb.append("<script type='text/javascript' id='ewa_resource_");
		sb.append(this.name);
		sb.append("' src='");
		sb.append(this.src+"'></script>");
		
		return sb.toString();
	}
	/**
	 * 转成CSS表达式
	 * @return
	 */
	public String toCss() {
		StringBuilder sb = new StringBuilder();
		sb.append("<link rel='stylesheet' id='ewa_resource_");
		sb.append(this.name);
		sb.append("' href='");
		sb.append(this.src+"'>");
		
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
	 * @return
	 */
	public String getXml() {
		// <addedResource src="/static/add_0.js" name="addjs0"></addedResource>
		return xml;
	}

	/**
	 * 配置XML信息
	 * @param xml
	 */
	public void setXml(String xml) {
		// <addedResource src="/static/add_0.js" name="addjs0"></addedResource>
		this.xml = xml;
	}

	/**
	 * 是否放到html页面的最后
	 * @return
	 */
	public boolean isLast() {
		return last;
	}

	/**
	 * 是否放到html页面的最后
	 * @param last
	 */
	public void setLast(boolean last) {
		this.last = last;
	}
}
