package com.gdxsoft.easyweb.script.restful;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UNet;

public class SdkBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(SdkBase.class);

	/**
	 * 创建 错误提示信息
	 * 
	 * @param errorMsg
	 * @param statusCode
	 * @return
	 */
	public static RestfulResult<Object> createErrorResult(String errorMsg, int errorCode, int httpStatusCode) {
		RestfulResult<Object> rr = new RestfulResult<>();
		rr.setCode(errorCode);
		rr.setHttpStatusCode(httpStatusCode);
		rr.setSuccess(false);
		rr.setMessage(errorMsg);
		rr.setRawData(rr.toJson());
		rr.setData(rr.toJson());

		return rr;
	}

	protected String errorMessage;
	protected String result;
	protected int httpStatusCode;
	protected String databaseName;

	protected String apiRoot;
	// server
	protected String serverToken;

	// users
	protected String userToken;
	protected String parames;
	protected String fromIp; // 客户端来源地址 ipv4/ipv6
	protected String fromUserAgent; // 客户端浏览器UA
	protected long chatUserId; // JWT 认证时传递 chat user id

	protected boolean showCurl = false; // 是否显示 curl 命令

	public RestfulResult<Object> apiDelete(String endPoint, String queryString) {
		String url = getApiPath(endPoint);
		if (StringUtils.isNotBlank(queryString)) {
			url += "?" + (queryString.startsWith("?") ? queryString.substring(1) : queryString);
		}
		UNet net = getNet();
		String result = net.doDelete(url);
		this.logNon200Warning(net, "DELETE", url, null);

		RestfulResult<Object> rr = new RestfulResult<>();
		rr.parse(result);

		return rr;

	}

	/**
	 * 通过 RESTful API 发送 POST 请求
	 * 
	 * @param endPoint
	 * @param body
	 * @return
	 */
	public RestfulResult<Object> apiPost(String endPoint, String body) {
		String url = getApiPath(endPoint);
		UNet net = getNet();
		String result = net.postMsg(url, body);
		this.logNon200Warning(net, "POST", url, body);

		RestfulResult<Object> rr = new RestfulResult<>();
		rr.parse(result);

		return rr;

	}

	/**
	 * 通过 RESTful API 发送 PATCH 请求
	 * 
	 * @param endPoint
	 * @param body
	 * @return
	 */
	public RestfulResult<Object> apiPatch(String endPoint, String body) {
		String url = getApiPath(endPoint);
		UNet net = getNet();
		String result = net.doPatch(url, body);
		this.logNon200Warning(net, "PATCH", url, body);

		RestfulResult<Object> rr = new RestfulResult<>();
		rr.parse(result);

		return rr;

	}

	/**
	 * 通过 RESTful API 发送 GET 请求
	 * 
	 * @param endPoint
	 * @param queryString
	 * @return
	 */
	public RestfulResult<Object> apiGet(String endPoint, String queryString) {
		UNet net = getNet();
		String url = getApiPath(endPoint);
		if (StringUtils.isNotBlank(queryString)) {
			url += "?" + (queryString.startsWith("?") ? queryString.substring(1) : queryString);
		}
		String result = net.doGet(url);
		this.logNon200Warning(net, "GET", url, null);

		RestfulResult<Object> rr = new RestfulResult<>();
		rr.parse(result);

		return rr;

	}

	/**
	 * 通过 RESTful API 发送 PUT 请求
	 * 
	 * @param endPoint
	 * @param body
	 * @return
	 */
	public RestfulResult<Object> apiPut(String endPoint, String body) {
		String url = getApiPath(endPoint);
		UNet net = getNet();
		String result = net.doPut(url, body);
		this.logNon200Warning(net, "PUT", url, body);

		RestfulResult<Object> rr = new RestfulResult<>();
		rr.parse(result);

		return rr;

	}

	/**
	 * 当 HTTP 状态码不为 200 时，输出 WARN 日志和对应的 curl 命令
	 *
	 * @param net    UNet 实例
	 * @param method HTTP 方法 (GET/POST/PUT/DELETE)
	 * @param url    请求 URL
	 * @param body   请求体（可为 null）
	 */
	public void logNon200Warning(UNet net, String method, String url, String body) {
		int statusCode = net.getLastStatusCode();
		if (statusCode >= 200 && statusCode < 300) {
			LOGGER.info("Api request code {} for {} {}", statusCode, method, url);
		} else {
			LOGGER.warn("Api request code {} for {} {}", statusCode, method, url);
		}

		if (!showCurl) {
			return;
		}

		java.util.List<String> parts = new java.util.ArrayList<>();
		parts.add("curl -X " + method + " '" + url + "'");

		if (StringUtils.isNotBlank(this.userToken)) {
			parts.add("  -H 'Authorization: " + this.userToken + "'");
			
		} else if (StringUtils.isNotBlank(this.serverToken)) {
			parts.add("  -H 'Authorization: Bearer " + this.serverToken + "'");
		}
		if (StringUtils.isNotBlank(body)) {
			// 转义 body 中的单引号，安全拼接
			String escaped = body.replace("'", "'\\''");
			parts.add("  -d '" + escaped + "'");
		}

		String curlStr = String.join(" \\\n", parts);
		LOGGER.info("Curl:\n{}", curlStr);
	}

	/**
	 * 创建一个 Server UNet 实例，并设置必要的请求头和用户代理
	 * 
	 * @return
	 */
	public UNet getNet() {
		UNet net = new UNet();

		if (StringUtils.isNotBlank(fromIp)) { // 客户端来源地址
			net.addHeader("X-Forwarded-For", fromIp);
		}
		if (StringUtils.isNotBlank(this.fromUserAgent)) { // 客户端浏览器UA
			net.setUserAgent(fromUserAgent);
		} else {
			net.setUserAgent("UNet/1.1.10 (gdxsoft.com)");
		}
		if(this.userToken!=null && !this.userToken.isEmpty()) {
			// userToken 优先级高于 serverToken
			net.addHeader("Authorization", this.userToken);
		} else if(this.serverToken!=null && !this.serverToken.isEmpty()) {
			// 使用 serverToken 授权方式
			net.addHeader("Authorization", "Bearer " + serverToken);
		} else {
			throw new IllegalStateException("No authorization token provided. Please set either userToken or serverToken.");
		}
		net.setIsShowLog(false);

		return net;
	}

	/**
	 * 创建一个 User UNet 实例，并设置必要的请求头和用户代理
	 * 
	 * @return
	 */
	public UNet createNet() {
		UNet net = new UNet();
		net.addHeader("Authorization", this.userToken);
		net.setUserAgent("UNet/1.1.10 (gdxsoft.com)");
		net.setIsShowLog(false);
		if (StringUtils.isNotBlank(fromIp)) { // 客户端来源地址
			net.addHeader("X-Forwarded-For", fromIp);
		}
		if (StringUtils.isNotBlank(this.fromUserAgent)) { // 客户端浏览器UA
			net.setUserAgent(fromUserAgent);
		}
		return net;
	}

	public String getApiPath(String path) {
		return this.apiRoot + (this.apiRoot.endsWith("/") ? "" : "/") + path;
	}

	public String createUrl(String path) {
		return this.getApiPath(path);
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getSuperToken() {

		return serverToken;

	}

	public int getHttpStatusCode() {
		return this.httpStatusCode;
	}

	public String getResult() {
		return this.result;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * 客户端来源地址 ipv4/ipv6
	 * 
	 * @return IP地址 (ipv4/ipv6)
	 */
	public String getFromIp() {
		return fromIp;
	}

	/**
	 * 客户端来源地址 ipv4/ipv6
	 * 
	 * @param fromIp IP地址 (ipv4/ipv6)
	 */
	public void setFromIp(String fromIp) {
		this.fromIp = fromIp;
	}

	/**
	 * 客户端浏览器UA
	 * 
	 * @return UserAgent
	 */
	public String getFromUserAgent() {
		return fromUserAgent;
	}

	/**
	 * 客户端浏览器UA
	 * 
	 * @param fromUserAgent 览器UserAgent
	 */
	public void setFromUserAgent(String fromUserAgent) {
		this.fromUserAgent = fromUserAgent;
	}

	public long getChatUserId() {
		return chatUserId;
	}

	public void setChatUserId(long chatUserId) {
		this.chatUserId = chatUserId;
	}

	public String getParames() {
		return parames;
	}

	public void setParames(String parames) {
		this.parames = parames;
	}

	public boolean isShowCurl() {
		return showCurl;
	}

	public void setShowCurl(boolean showCurl) {
		this.showCurl = showCurl;
	}
}
