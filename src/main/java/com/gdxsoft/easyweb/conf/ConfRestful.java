package com.gdxsoft.easyweb.conf;

/**
 * 附加资源
 * 
 * @author admin
 *
 */
public class ConfRestful {
	private String xmlName;
	private String itemName;
	private String parameters;
	private String path;
	private String restfulPath;
	private String xml;
	private String method;

	private int pathDirsDepth;

	public String getXmlName() {
		return xmlName;
	}

	public void setXmlName(String xmlName) {
		this.xmlName = xmlName;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	/**
	 * http method, PUT/GET/POST/PATCH/DELETE
	 * 
	 * @return
	 */
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRestfulPath() {
		return restfulPath;
	}

	public void setRestfulPath(String restfulPath) {
		this.restfulPath = restfulPath;
	}

	/**
	 * The path directories depth
	 * 
	 * @return
	 */
	public int getPathDirsDepth() {
		return pathDirsDepth;
	}

	public void setPathDirsDepth(int depth) {
		this.pathDirsDepth = depth;
	}

}
