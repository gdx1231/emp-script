package com.gdxsoft.easyweb.websocket;

import java.util.Map;
import java.util.concurrent.Future;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfHandleWebSocketMessage;
import com.gdxsoft.easyweb.conf.ConfHandleWebSocketMessages;
import com.gdxsoft.easyweb.script.RequestValue;
import com.gdxsoft.easyweb.utils.Utils;

// @ServerEndpoint(value = "/ewa-ws", configurator = EwaWebSocketConfigure.class)

/**
 * WebSocket 消息总线
 */
public class EwaWebSocketBus {
	private static Logger LOGGER = LoggerFactory.getLogger(EwaWebSocketBus.class);

	/**
	 * 在程序中提交handler，默认是通过ewa_conf.xml中进行配置
	 * 
	 * @param methodName 处理名称
	 * @param mapClass   对应的类名称
	 * @return
	 */
	public static synchronized boolean registerHandler(String methodName, String mapClass) {
		String name1 = methodName.trim().toLowerCase();
		Map<String, ConfHandleWebSocketMessage> map = ConfHandleWebSocketMessages.getInstance().getHandles();
		if (map.containsKey(name1)) {
			LOGGER.info("{} 已经存在处理方法：{}", methodName, map.get(name1).getMapClass());
			return false;
		}

		ConfHandleWebSocketMessage handlerCfg = new ConfHandleWebSocketMessage();
		handlerCfg.setMethod(methodName);
		handlerCfg.setMapClass(mapClass);
		handlerCfg.setXml("<a>用户自己提交</a>");

		map.put(name1, handlerCfg);
		LOGGER.info("{} 添加成功：{}", methodName, handlerCfg.getMapClass());
		return true;
	}

	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;
	private RequestValue rv_;
	private String unid_;

	/**
	 * 连接建立成功调用的方法
	 * 
	 * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	@OnOpen
	public void onOpen(Session session) {
		// 每个会话有一个唯一id
		this.unid_ = System.currentTimeMillis() + "." + Utils.getGuid();
		this.session = session;

		Map<String, Object> props = session.getUserProperties();
		RequestValue rv = null;
		for (String key : props.keySet()) {
			if (key.equals("com.gdxsoft.easyweb.script.RequestValue")) {
				Object obj = props.get(key);
				if (rv == null) {
					rv = (RequestValue) obj;
				}
			}
		}
		this.rv_ = rv;

		EwaWebSocketContainer.add(this);

		// 客户端记录 服务器端的 socket的 unid
		JSONObject msg = new JSONObject();
		msg.put("socketId", this.unid_);
		msg.put("ID", "socketId");
		this.sendToClient(msg.toString());
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		EwaWebSocketContainer.remove(this); // 从set中删除
	}

	/**
	 * 收到客户端消息后调用的方法
	 * 
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		LOGGER.debug(message);
		try {
			JSONObject obj = new JSONObject(message);
			this.handleMessage(obj);
		} catch (Exception err) {
			LOGGER.error(err.getMessage());
		}
		// broadcast(message);
	}

	private void handleMessage(JSONObject obj) {
		String method = obj.optString("method");
		if (StringUtils.isBlank(method)) {
			method = obj.optString("METHOD");
		}
		if (StringUtils.isBlank(method)) {
			return;
		}
		IHandleMsg intanceHandleMsg = LoadHandleMessage.getInstance(method, this, obj);
		if (intanceHandleMsg == null) {
			return;
		}
		intanceHandleMsg.start();
	}

	/**
	 * 异步发送消息到客户端
	 * 
	 * @param msg
	 */
	public boolean sendToClient(String msg) {
		try {
			synchronized (this.session) {
				Async async = session.getAsyncRemote();
				async.setSendTimeout(2000); // 2s
				Future<Void> aa = async.sendText(msg);
				aa.get();
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return false;
		}
	}

	/**
	 * 发生错误时调用
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		LOGGER.error(error.getMessage());
	}

	public Session getSession() {
		return session;
	}

	public RequestValue getRv() {
		return rv_;
	}

	/**
	 * 唯一编号，检索用
	 * 
	 * @return
	 */
	public String getUnid() {
		return unid_;
	}

}