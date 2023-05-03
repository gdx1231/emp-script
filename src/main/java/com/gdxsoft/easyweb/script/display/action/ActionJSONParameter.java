package com.gdxsoft.easyweb.script.display.action;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gdxsoft.easyweb.utils.Utils;

/**
 * JSON 调用参数
 * 
 * @author admin
 *
 */
public class ActionJSONParameter {

	private String method; // PUT, GET, POST, PATCH, DELETE
	private String url; // 请求地址
	private Map<String, String> headers = new HashMap<>(); // http请求的header数据
	private Map<String, String> data = new HashMap<>(); // http请求的form数据和body互斥
	private String body; // body提交数据和 form互斥
	private Map<String, String> queries = new HashMap<>(); // 查询参数请求
	private List<ActionJSONParameterListTag> listTags = new ArrayList<>();

	private String charset = StandardCharsets.UTF_8.name();
	private String userAgent = "github.com/gdx1231";
	private boolean debug = false;

	private boolean attacheQuery = false; // 附加请求参数
	private boolean attachePost = false;// 附加请求form数据
	private boolean attacheCookies = false; // 附加请求页面的cookies

	private JSONObject jsonObject; // 配置的json，替换了参数

	private String expire = ""; // 1d, 13h, 300m 1200s

	private String connConfigName; // database connection configure name

	private boolean asLfData = false; // 作为列表数据

	private JSONObject sign; // 签名算法

	public void init(String jsonStr) throws Exception {
		JSONObject obj = new JSONObject(jsonStr);
		this.init(obj);
	}

	public void init(JSONObject obj) throws Exception {
		jsonObject = obj;
		Iterator<String> it = obj.keys();
		while (it.hasNext()) {
			String key = it.next();
			if (key.equalsIgnoreCase("method")) {
				String val = obj.optString(key);
				this.method = val.trim().toUpperCase();
			} else if (key.equalsIgnoreCase("url")) {
				String val = obj.optString(key);
				this.url = val.trim();
			} else if (key.equalsIgnoreCase("body")) {
				String val = obj.optString(key);
				this.body = val;
			} else if (key.equalsIgnoreCase("charset")) {
				String val = obj.optString(key);
				this.charset = val;
			} else if (key.equalsIgnoreCase("expire")) {
				String val = obj.optString(key);
				this.expire = val;
			} else if (key.equalsIgnoreCase("connConfigName")) {
				String val = obj.optString(key);
				this.connConfigName = val;
			} else if (key.equalsIgnoreCase("headers") || key.equalsIgnoreCase("header")) {
				JSONObject h = obj.optJSONObject(key);
				if (h != null) {
					this.jsonObject2Map(h, headers);
				}
			} else if (key.equalsIgnoreCase("queries")) {
				JSONObject h = obj.optJSONObject(key);
				if (h != null) {
					this.jsonObject2Map(h, queries);
				}
			} else if (key.equalsIgnoreCase("data")) {
				JSONObject h = obj.optJSONObject(key);
				if (h != null) {
					this.jsonObject2Map(h, data);
				}
			} else if (key.equalsIgnoreCase("debug")) {
				String val = obj.optString(key);
				this.debug = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("attacheQuery")) {
				String val = obj.optString(key);
				this.attacheQuery = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("attachePost")) {
				String val = obj.optString(key);
				this.attachePost = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("attacheCookies")) {
				String val = obj.optString(key);
				this.attacheCookies = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("asLfData")) {
				// 作为列表数据
				String val = obj.optString(key);
				this.asLfData = Utils.cvtBool(val);
			} else if (key.equalsIgnoreCase("listTags")) {
				JSONArray arr = obj.optJSONArray(key);
				if (arr != null) {
					this.listTag(arr);
				}
			} else if (key.equalsIgnoreCase("sign")) {
				this.sign = obj.optJSONObject(key);
			}
		}
		if (StringUtils.isNotBlank(this.connConfigName)) {
			this.listTags.forEach(t -> {
				if (StringUtils.isBlank(t.getConnConfigName())) {
					t.setConnConfigName(this.connConfigName);
				}
			});
		}
		// set method default
		if (method == null || method.trim().length() == 0) {
			if (data.size() == 0 && (body == null || body.length() == 0)) {
				method = "GET";
			} else {
				method = "POST";
			}
		} else {
			if (!checkMethod()) {
				throw new Exception("Invalid method: " + this.method);
			}
		}
		if (url == null || url.trim().length() == 0) {
			throw new Exception("url must provided ");
		}
		if (!(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
			throw new Exception("Invalid url: " + this.url);
		}
	}

	private boolean checkMethod() {
		return this.method.equalsIgnoreCase("GET") || this.method.equalsIgnoreCase("POST")
				|| this.method.equalsIgnoreCase("PUT") || this.method.equalsIgnoreCase("DELETE")
				|| this.method.equalsIgnoreCase("PATCH");
	}

	private void listTag(JSONArray arr) {
		for (int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.getJSONObject(i);
			ActionJSONParameterListTag t = new ActionJSONParameterListTag();
			t.init(o);
			this.listTags.add(t);
		}
	}

	private void jsonObject2Map(JSONObject json, Map<String, String> map) {
		Iterator<String> it = json.keys();
		while (it.hasNext()) {
			String key = it.next();
			String val = json.optString(key);
			map.put(key, val);
		}
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * @return the data
	 */
	public Map<String, String> getData() {
		return data;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the queries
	 */
	public Map<String, String> getQueries() {
		return queries;
	}

	/**
	 * @return the listTags
	 */
	public List<ActionJSONParameterListTag> getListTags() {
		return listTags;
	}

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the attacheQuery
	 */
	public boolean isAttacheQuery() {
		return attacheQuery;
	}

	/**
	 * @param attacheQuery the attacheQuery to set
	 */
	public void setAttacheQuery(boolean attacheQuery) {
		this.attacheQuery = attacheQuery;
	}

	/**
	 * @return the attachePost
	 */
	public boolean isAttachePost() {
		return attachePost;
	}

	/**
	 * @param attachePost the attachePost to set
	 */
	public void setAttachePost(boolean attachePost) {
		this.attachePost = attachePost;
	}

	/**
	 * @return the attacheCookies
	 */
	public boolean isAttacheCookies() {
		return attacheCookies;
	}

	/**
	 * @param attacheCookies the attacheCookies to set
	 */
	public void setAttacheCookies(boolean attacheCookies) {
		this.attacheCookies = attacheCookies;
	}

	/**
	 * 获取初始化参数的jsonObject
	 * 
	 * @return the jsonObject
	 */
	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public String toString() {
		if (this.jsonObject == null) {
			return "not initialized";
		} else {
			return this.jsonObject.toString(3);
		}
	}

	/**
	 * 缓存过期时间 1d, 13h, 300m 1200s
	 * 
	 * @return the expire
	 */
	public String getExpire() {
		return expire;
	}

	/**
	 * 缓存过期时间1d, 13h, 300m 1200s
	 * 
	 * @param expire the expire to set
	 */
	public void setExpire(String expire) {
		this.expire = expire;
	}

	/**
	 * @return the connConfigName
	 */
	public String getConnConfigName() {
		return connConfigName;
	}

	/**
	 * @param connConfigName the connConfigName to set
	 */
	public void setConnConfigName(String connConfigName) {
		this.connConfigName = connConfigName;

		this.listTags.forEach(t -> {
			if (StringUtils.isBlank(t.getConnConfigName())) {
				t.setConnConfigName(this.connConfigName);
			}
		});
	}

	/**
	 * 作为列表数据
	 * 
	 * @return the asLfData
	 */
	public boolean isAsLfData() {
		return asLfData;
	}

	/**
	 * 作为列表数据
	 * 
	 * @param asLfData the asLfData to set
	 */
	public void setAsLfData(boolean asLfData) {
		this.asLfData = asLfData;
	}

	/**
	 * 签名算法
	 * @return the sign
	 */
	public JSONObject getSign() {
		return sign;
	}

}
