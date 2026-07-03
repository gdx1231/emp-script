package com.gdxsoft.easyweb.conf;

/**
 * 单个 WebSocket endpoint 配置，从 ewa_conf.xml 的 &lt;wss&gt;&lt;ws /&gt;&lt;/wss&gt; 加载。
 */
public class ConfWebSocket {

	private String name;       // 唯一名称
	private String clazz;      // endpoint 实现类（全限定名）
	private String endpoint;   // WebSocket 路径，如 /ws-chat/
	private String configurator;  // Configurator 类名（可选）
	private String xml;        // 原始 XML 片段

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getConfigurator() {
		return configurator;
	}

	public void setConfigurator(String configurator) {
		this.configurator = configurator;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}
}
