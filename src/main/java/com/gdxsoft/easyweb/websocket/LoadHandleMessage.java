package com.gdxsoft.easyweb.websocket;

import java.util.Map;

import org.json.JSONObject;

import com.gdxsoft.easyweb.conf.ConfHandleWebSocketMessage;
import com.gdxsoft.easyweb.conf.ConfHandleWebSocketMessages;
import com.gdxsoft.easyweb.utils.UObjectValue;

/**
 * 通过配置文件加载消息处理的方法
 * 
 * @author admin
 *
 */
public class LoadHandleMessage {
	/**
	 * 获取方法对应的类
	 * 
	 * @param methodName 模式名称
	 * @param webSocket  webSocket
	 * @param obj        命令对象
	 * @return
	 */
	public static IHandleMsg getInstance(String methodName, EwaWebSocket webSocket, JSONObject obj) {

		String name1 = methodName.trim().toUpperCase();
		Map<String, ConfHandleWebSocketMessage> map = ConfHandleWebSocketMessages.getInstance().getHandles();
		if (map == null || !map.containsKey(name1)) {
			return null;
		}

		ConfHandleWebSocketMessage handle = map.get(name1);

		UObjectValue ov = new UObjectValue();

		Object[] constructorParameters = new Object[2];
		constructorParameters[0] = webSocket;
		constructorParameters[1] = obj;

		Object classLoaded = ov.loadClass(handle.getMapClass(), constructorParameters);

		if (classLoaded == null) {
			return null;
		}
		IHandleMsg instance = (IHandleMsg) classLoaded;

		return instance;
	}
}
