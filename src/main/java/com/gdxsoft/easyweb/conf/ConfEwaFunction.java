package com.gdxsoft.easyweb.conf;

public class ConfEwaFunction {
	private String name; // EWA内部调用的名称
	private String className; // 类名
	private String methodName; // 方法名

	private String des;
	private String desEn;
	private String usage;

	private String xml;
	private boolean userDefined;

	public ConfEwaFunction() {

	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getDesEn() {
		return desEn;
	}

	public void setDesEn(String desEn) {
		this.desEn = desEn;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	/**
	 * whether The function in ewa_conf.xml
	 * 
	 * @return
	 */
	public boolean isUserDefined() {
		return userDefined;
	}

	public void setUserDefined(boolean userDefined) {
		this.userDefined = userDefined;
	}
}
