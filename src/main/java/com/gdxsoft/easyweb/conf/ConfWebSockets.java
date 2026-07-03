package com.gdxsoft.easyweb.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * WebSocket endpoint 配置集合，从 ewa_conf.xml 的 &lt;wss&gt; 节点加载。
 *
 * <pre>
 * &lt;wss&gt;
 *     &lt;ws name="chat"   class="com.gdxsoft.ai.app.chatroom.WsChatEndpoint"   endpoint="/ws-chat/"   configurator="com.gdxsoft.ai.app.chatroom.RvConfigure" /&gt;
 *     &lt;ws name="aichat" class="com.gdxsoft.ai.app.chatroom.WsAiChatEndpoint" endpoint="/ws-ai-chat/" configurator="com.gdxsoft.ai.app.chatroom.RvConfigure" /&gt;
 * &lt;/wss&gt;
 * </pre>
 */
public class ConfWebSockets {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfWebSockets.class);
	private static ConfWebSockets INST = null;

	/**
	 * 获取单例，首次调用自动从 ewa_conf.xml 加载。
	 */
	public static ConfWebSockets getInstance() {
		if (INST != null) {
			return INST;
		}
		INST = instance();
		return INST;
	}

	private synchronized static ConfWebSockets instance() {
		ConfWebSockets sps = new ConfWebSockets();
		try {
			load(sps);
		} catch (Exception e) {
			LOGGER.error("加载 wss 配置失败", e);
		}
		return sps;
	}

	private static void load(ConfWebSockets sps) {
		if (UPath.getCfgXmlDoc() == null) {
			LOGGER.warn("ewa_conf.xml 未加载，跳过 wss 配置");
			return;
		}

		// 找 <wss> 根节点
		NodeList wssList = UPath.getCfgXmlDoc().getElementsByTagName("wss");
		if (wssList.getLength() == 0) {
			LOGGER.info("ewa_conf.xml 中未定义 <wss> 节点");
			return;
		}

		Element wss = (Element) wssList.item(0);
		NodeList nl = wss.getElementsByTagName("ws");
		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			ConfWebSocket ws = createFromXml(item);
			if (ws != null) {
				sps.map.put(ws.getName(), ws);
				sps.list.add(ws);
				LOGGER.info("WS 端点注册: {} -> {} ({})", ws.getName(), ws.getEndpoint(), ws.getClazz());
			}
		}
	}

	private static ConfWebSocket createFromXml(Element item) {
		Map<String, String> vals = UXml.getElementAttributes(item, true);

		String name = vals.get("name");
		if (StringUtils.isBlank(name)) {
			LOGGER.warn("ws 节点缺少 name 属性: {}", UXml.asXml(item));
			return null;
		}

		String clazz = vals.get("class");
		if (StringUtils.isBlank(clazz)) {
			LOGGER.warn("ws 节点 {} 缺少 class 属性", name);
			return null;
		}

		String endpoint = vals.get("endpoint");
		if (StringUtils.isBlank(endpoint)) {
			LOGGER.warn("ws 节点 {} 缺少 endpoint 属性", name);
			return null;
		}

		ConfWebSocket ws = new ConfWebSocket();
		ws.setName(name.trim());
		ws.setClazz(clazz.trim());
		ws.setEndpoint(endpoint.trim());
		ws.setConfigurator(vals.get("configurator")); // 可选
		ws.setXml(UXml.asXml(item));

		return ws;
	}

	private final Map<String, ConfWebSocket> map = new HashMap<>();
	private final List<ConfWebSocket> list = new ArrayList<>();

	private ConfWebSockets() {
	}

	/**
	 * 按名称获取配置
	 */
	public ConfWebSocket get(String name) {
		return map.get(name);
	}

	/**
	 * 获取所有 endpoint 配置列表
	 */
	public List<ConfWebSocket> getList() {
		return list;
	}

	/**
	 * 已注册的 endpoint 数量
	 */
	public int size() {
		return list.size();
	}
}
