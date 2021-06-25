package com.gdxsoft.easyweb.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理所有 IndexWebSocket的容器
 * 
 * @author admin
 *
 */
public class EwaWebSocketContainer {
	private static Logger LOGGER = LoggerFactory.getLogger(EwaWebSocketContainer.class);
	private static ConcurrentHashMap<String, EwaWebSocket> WEB_SOCKETS_MAP = new ConcurrentHashMap<String, EwaWebSocket>();

	/**
	 * 广播消息
	 * 
	 * @param msg
	 */
	public static void broadcast(String msg) {
		for (String key : WEB_SOCKETS_MAP.keySet()) {
			EwaWebSocket socket = WEB_SOCKETS_MAP.get(key);
			broadcastTo(socket, msg);
		}
	}

	/**
	 * 广播消息到指定socket
	 * 
	 * @param socket
	 * @param msg
	 * @return
	 */
	private static boolean broadcastTo(EwaWebSocket socket, String msg) {
		try {
			return socket.sendToClient(msg);
		} catch (Exception e) {
			remove(socket);
			LOGGER.error(e.getMessage());
			try {
				socket.getSession().close();
			} catch (IOException e1) {
				LOGGER.error(e1.getMessage());
			}
			return false;
		}
	}

	/**
	 * 添加链接
	 * 
	 * @param socket
	 */
	public static void add(EwaWebSocket socket) {
		WEB_SOCKETS_MAP.put(socket.getUnid(), socket);
		LOGGER.debug("JOIN [" + socket.getUnid() + "], total online: " + size());
	}

	/**
	 * 移除链接
	 * 
	 * @param socket
	 */
	public static void remove(EwaWebSocket socket) {
		WEB_SOCKETS_MAP.remove(socket.getUnid());
		LOGGER.debug("CLOSE [" + socket.getUnid() + "], total online:" + size());
	}

	/**
	 * 根据unid，获取socket
	 * 
	 * @param socketUnid
	 * @return
	 */
	public static EwaWebSocket getSocketByUnid(String socketUnid) {
		return WEB_SOCKETS_MAP.get(socketUnid);
	}

	/**
	 * 获取数量
	 * 
	 * @return
	 */
	public static int size() {
		return WEB_SOCKETS_MAP.size();
	}

	/**
	 * 获取所有的连接
	 * 
	 * @return
	 */
	public static ConcurrentHashMap<String, EwaWebSocket> getSockets() {
		return WEB_SOCKETS_MAP;
	}

}
