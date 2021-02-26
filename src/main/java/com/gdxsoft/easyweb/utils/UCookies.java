package com.gdxsoft.easyweb.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Cookie的操作
 * 
 * @author admin
 *
 */
public class UCookies {
	private static Logger LOGGER = Logger.getLogger(UCookies.class);
	public static String COOKIE_NAME_PREFIX = "__EWA__"; // cookie加密的名称后缀

	private String domain;
	private String path;
	private Integer maxAgeSeconds;
	private boolean httpOnly = true;
	private boolean secret = true;

	private boolean ewaDes = false;

	public UCookies() {

	}

	public UCookies(String path, Integer maxAgeSeconds) {
		this.path = path;
		this.maxAgeSeconds = maxAgeSeconds;
	}

	/**
	 * 清除浏览器的cookie
	 * 
	 * @param request   HttpServletRequest
	 * @param response  HttpServletResponse
	 * @param skipNames 需要过滤的名称
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response, List<String> skipNames) {
		if (request == null || request.getCookies() == null) {
			return;
		}
		for (javax.servlet.http.Cookie cookie : request.getCookies()) {
			if (skipNames != null) {
				boolean isSkip = skipNames.stream().anyMatch(item -> cookie.getName().equals(item));

				if (isSkip) {
					continue;
				}
			}
			cookie.setMaxAge(0);
			cookie.setPath("/");
			cookie.setValue(null);
			response.addCookie(cookie);

			cookie.setPath(request.getContextPath());
			response.addCookie(cookie);
		}
	}

	/**
	 * 删除Coolie
	 * 
	 * @param cookieName 名称
	 * @param response   jsp 的 HttpServletResponse
	 */
	public void deleteCookie(String cookieName, HttpServletResponse response) {
		Cookie cookie = this.createCookie(cookieName, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	/**
	 * 添加Cookie
	 * 
	 * @param cookieName  名称
	 * @param cookieValue 值
	 * @param response    创建的Cookie
	 * @return
	 */
	public Cookie addCookie(String cookieName, String cookieValue, HttpServletResponse response) {
		Cookie cookie = this.createCookie(cookieName, cookieValue);
		response.addCookie(cookie);

		return cookie;
	}

	/**
	 * 创建UrlEncode ascii编码的cookie值
	 * 
	 * @param cookieValue cookie明码
	 * @return UrlEncode.encode 的值
	 */
	public static String encodeCookieValue(String cookieValue) {
		if (cookieValue == null) {
			return null;
		}
		String cv;
		try {
			cv = URLEncoder.encode(cookieValue, "ascii");
		} catch (UnsupportedEncodingException e1) {
			LOGGER.warn(e1);
			cv = cookieValue;
		}

		return cv;
	}

	/**
	 * 解码 UrlDecode ascii编码的cookie值
	 * 
	 * @param encoderCookieValue UrlEncode.encode的cookie
	 * @return cookie明码
	 */
	public static String decodeCookieValue(String encoderCookieValue) {
		try {
			return URLDecoder.decode(encoderCookieValue, "ascii");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn(e);
			return encoderCookieValue;
		}
	}

	/**
	 * 创建Cookie
	 * 
	 * @param cookieName  名称
	 * @param cookieValue 值
	 * @return 创建的Cookie
	 */
	public Cookie createCookie(String cookieName, String cookieValue) {

		Cookie cookie = null;

		if (this.isEwaDes()) {
			String ckName = COOKIE_NAME_PREFIX + cookieName;
			if (cookieValue == null) {
				cookie = new Cookie(ckName, null);
			} else {
				try {
					UDes des = new UDes();
					String value = des.getEncString(cookieValue);
					cookie = new Cookie(ckName, encodeCookieValue(value));
				} catch (Exception e) {
					LOGGER.error(e);
					return null;
				}
			}
		} else {
			cookie = new Cookie(cookieName, encodeCookieValue(cookieValue));
		}
		cookie.setHttpOnly(httpOnly);
		cookie.setSecure(secret);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		if (path != null) {
			cookie.setPath(path);
		}
		if (this.maxAgeSeconds != null) {
			cookie.setMaxAge(this.maxAgeSeconds);
		}

		return cookie;
	}

	/**
	 * Cookie 的 Domain
	 * 
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Cookie 的 Domain
	 * 
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Cookie 的 Path
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Cookie 的 Path
	 * 
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Cookie 的 MaxAge
	 * 
	 * @return the maxAgeSeconds
	 */
	public Integer getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	/**
	 * Cookie 的 MaxAge
	 * 
	 * @param maxAgeSeconds the maxAgeSeconds to set
	 */
	public void setMaxAgeSeconds(Integer maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	/**
	 * Cookie 的 httpOnly
	 * 
	 * @return the httpOnly
	 */
	public boolean isHttpOnly() {
		return httpOnly;
	}

	/**
	 * Cookie 的 httpOnly
	 * 
	 * @param httpOnly the httpOnly to set
	 */
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	/**
	 * Cookie 的 secure
	 * 
	 * @return the secert
	 */
	public boolean isSecret() {
		return secret;
	}

	/**
	 * Cookie 的 secure
	 * 
	 * @param secert the secert to set
	 */
	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	/**
	 * 是否用 标准的 Des 加密cookie值
	 * 
	 * @return the ewaDes
	 */
	public boolean isEwaDes() {
		return ewaDes;
	}

	/**
	 * 是否用 标准的 Des 加密cookie值
	 * 
	 * @param ewaDes the ewaDes to set
	 */
	public void setEwaDes(boolean ewaDes) {
		this.ewaDes = ewaDes;
	}

}
