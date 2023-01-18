package com.gdxsoft.easyweb.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import com.gdxsoft.easyweb.script.PageValueTag;
import com.gdxsoft.easyweb.script.RequestValue;

/**
 * 附加 RequestValue
 * 
 * @author admin
 */
public class EwaWebSocketConfigure extends Configurator {

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		// httpsession
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		RequestValue rv = new RequestValue();

		Cookie[] cookies = null;
		Map<String, String> headers = new HashMap<String, String>();
		for (String key : request.getHeaders().keySet()) {
			List<String> lst = request.getHeaders().get(key);
			if (key.equalsIgnoreCase("cookie")) {
				cookies = this.getCookies(lst);
			} else {
				// Websocket 通过header传递的参数
				rv.addValue(key, lst.get(0), PageValueTag.OTHER);
				headers.put(key, lst.get(0));
			}
		}

		// 增加header的别名
		rv.initParametersByHeaders(headers);
		rv.reloadSessions(httpSession);
		rv.reloadCookies(cookies);
		rv.reloadQueryValues(request.getQueryString());

		sec.getUserProperties().put(RequestValue.class.getName(), rv);
	}

	/**
	 * 将头部的cookies参数转换为cookie
	 * 
	 * @param lst
	 * @return
	 */
	private Cookie[] getCookies(List<String> lst) {
		List<Cookie> cks = new ArrayList<Cookie>();
		for (int i = 0; i < lst.size(); i++) {
			String[] items = lst.get(i).split(";");
			for (int m = 0; m < items.length; m++) {
				String[] item = items[m].split("\\=");
				if (item.length == 2) {
					// System.out.println(item[0] + "=" + item[1]);
					Cookie ck = new Cookie(item[0].trim(), item[1]);
					cks.add(ck);
				}
			}
		}
		Cookie[] cks1 = new Cookie[cks.size()];
		return cks.toArray(cks1);
	}
}