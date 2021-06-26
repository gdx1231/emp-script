package com.gdxsoft.easyweb.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理所有 EwaWebSocketBus的容器
 *
 */
public class EwaWebSocketContainer {
	private static Logger LOGGER = LoggerFactory.getLogger(EwaWebSocketContainer.class);
	// concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，
	// 可以使用Map来存放，其中Key可以为用户标识
	private static Map<String, EwaWebSocketBus> WEB_SOCKETS_MAP = new ConcurrentHashMap<>();

	/**
	 * 广播消息
	 * 
	 * @param msg
	 */
	public static void broadcast(String msg) {
		for (String key : WEB_SOCKETS_MAP.keySet()) {
			EwaWebSocketBus socket = WEB_SOCKETS_MAP.get(key);
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
	private static boolean broadcastTo(EwaWebSocketBus socket, String msg) {
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
	public static void add(EwaWebSocketBus socket) {
		WEB_SOCKETS_MAP.put(socket.getUnid(), socket);
		LOGGER.debug("JOIN [" + socket.getUnid() + "], total online: " + size());
	}

	/**
	 * 移除链接
	 * 
	 * @param socket
	 */
	public static void remove(EwaWebSocketBus socket) {
		WEB_SOCKETS_MAP.remove(socket.getUnid());
		LOGGER.debug("CLOSE [" + socket.getUnid() + "], total online:" + size());
	}

	/**
	 * 根据unid，获取socket
	 * 
	 * @param socketUnid
	 * @return
	 */
	public static EwaWebSocketBus getSocketByUnid(String socketUnid) {
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
	public static Map<String, EwaWebSocketBus> getSockets() {
		return WEB_SOCKETS_MAP;
	}

}
