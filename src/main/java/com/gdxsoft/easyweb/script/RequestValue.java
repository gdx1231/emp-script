package com.gdxsoft.easyweb.script;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfRequestValuesGlobal;
import com.gdxsoft.easyweb.conf.ConfSecurities;
import com.gdxsoft.easyweb.data.DTRow;
import com.gdxsoft.easyweb.data.DTTable;
import com.gdxsoft.easyweb.script.display.action.ActionBase;
import com.gdxsoft.easyweb.script.display.frame.FrameParameters;
import com.gdxsoft.easyweb.utils.*;
import com.gdxsoft.easyweb.utils.msnet.MListStr;
import com.gdxsoft.easyweb.utils.msnet.MStr;
import com.gdxsoft.easyweb.utils.msnet.MTable;

/**
 * 参数表类，从Form, QueryString, Sessions, Cookies 获取参数值保存到类中<br>
 * 系统参数采用新的命名方式 EWA.开头，sys_开头的为兼容老版本
 * 
 * @author Administrator
 * 
 */
public class RequestValue implements Cloneable {
	/**
	 * 用户代理，浏览器的user-agent头
	 */
	public static final String SYS_USER_AGENT = "SYS_USER_AGENT";
	/**
	 * 用户请求来源参考
	 */
	public static final String SYS_REMOTE_REFERER = "SYS_REMOTE_REFERER";
	/**
	 * 用户IP地址
	 */
	public static final String SYS_REMOTEIP = "SYS_REMOTEIP";
	/**
	 * 用户请求地址，不包含查询，例如: https://gdxsoft.com/users/userinfo.jsp
	 */
	public static final String SYS_REMOTE_URL = "SYS_REMOTE_URL";
	/**
	 * 用户请求地址，包含查询，例如:
	 * https://gdxsoft.com/users/userinfo.jsp?user_id=1&type=2&ewa_ajax=json
	 */
	public static final String SYS_REMOTE_URL_ALL = "SYS_REMOTE_URL_ALL";
	/**
	 * 实例每次创建的UNID，同 EWA.ID
	 */
	public static final String SYS_UNID = "SYS_UNID";
	/**
	 * 实例每次创建的UNID，同 SYS_UNID
	 */
	public static final String EWAdotID = "EWA.ID";
	/**
	 * 实例每次创建的当前时间，同EWAdotDATE
	 */
	public static final String SYS_DATE = "SYS_DATE";
	/**
	 * 实例每次创建的当前时间，同SYS_DATE
	 */
	public static final String EWAdotDATE = "EWA.DATE";
	/**
	 * 实例每次创建的当前时间的日
	 */
	public static final String EWAdotDATEdotDAY = "EWA.DATE.DAY";
	/**
	 * 实例每次创建的当前时间的小时（24小时）
	 */
	public static final String EWAdotDATEdotHOUR = "EWA.DATE.HOUR";
	/**
	 * 实例每次创建的当前时间的分钟
	 */
	public static final String EWAdotDATEdotMINUTE = "EWA.DATE.MINUTE";
	/**
	 * 实例每次创建的当前时间的月 1-12
	 */
	public static final String EWAdotDATEdotMONTH = "EWA.DATE.MONTH";
	/**
	 * 实例每次创建的当前时间的秒
	 */
	public static final String EWAdotDATEdotSECOND = "EWA.DATE.SECOND";
	/**
	 * 实例每次创建的当前时间的日期字符串，例如：12:39:12
	 */
	public static final String EWAdotDATEdotTIME = "EWA.DATE.TIME";
	/**
	 * 实例每次创建的当前时间的日期字符串，例如：2012-12-02
	 */
	public static final String EWAdotDATEdotSTR = "EWA.DATE.STR";
	/**
	 * 实例每次创建的当前时间的日期和时间字符串，同EWA.DATETIME.STR，例如： 2012-12-02 11:30:03
	 */
	public static final String EWAdotDATEdotSTR1 = "EWA.DATE.STR1";
	/**
	 * 实例每次创建的当前时间的日期和时间字符串，同EWA.DATE.STR1，例如： 2012-12-02 11:30:03
	 */
	public static final String EWAdotDATETIMEdotSTR = "EWA.DATETIME.STR";
	/**
	 * 实例每次创建的当前时间的年，例如：2012
	 */
	public static final String EWAdotDATEdotYEAR = "EWA.DATE.YEAR";
	/**
	 * WEB服务当前项目目录，例如 /users，同EWACP
	 */
	public static final String SYS_CONTEXTPATH = "SYS_CONTEXTPATH";

	/**
	 * WEB服务当前项目目录，例如 /users，同SYS_CONTEXTPATH
	 */
	public static final String EWAdotCP = "EWA.CP";

	/**
	 * 当前请求的servlet名称，例如: /users/userinfo.jsp
	 */
	public static final String EWAdotCPF = "EWA.CPF";
	/**
	 * 当前请求的servlet名称和查询地址，例如: /users/userinfo.jsp?user_id=1&type=2&ewa_ajax=json
	 */
	public static final String EWAdotCPF_ALL = "EWA.CPF_ALL";
	/**
	 * 请求地址的查询，不包含EWA系统参数，例如：user_id=1&type=2
	 */
	public static final String EWA_QUERY = "EWA_QUERY";
	/**
	 * 请求地址的查询，包含EWA系统参数，例如：user_id=1&type=2&ewa_ajax=json
	 */
	public static final String EWA_QUERY_ALL = "EWA_QUERY_ALL";

	/**
	 * 系统内置参数，数据库表数据中二进制临时存储文件路径，<br>
	 * 例如：/var/lib/ewa_page_cached/cached/_EWA_TMP_/IMG/
	 */
	public static final String EWAdotPATH_IMG_CACHE = "EWA.PATH_IMG_CACHE";
	/**
	 * 系统内置参数，数据库表数据中二进制临时存储文件路径映射到url路径<br>
	 * 例如：/cached/_EWA_TMP_/IMG/
	 */
	public static final String EWAdotPATH_IMG_CACHE_URL = "EWA.PATH_IMG_CACHE_URL";
	/**
	 * 系统内置参数，用户上传文件物理路径<br>
	 * 例如：/var/lib/ewa_page_cached/cached
	 */
	public static final String EWAdotPATH_UPLOAD = "EWA.PATH_UPLOAD";
	/**
	 * 系统内置参数，用户上传文件物理路径映射到url路径<br>
	 * 例如：/var/lib/ewa_page_cached/cached
	 */
	public static final String EWAdotPATH_UPLOAD_URL = "EWA.PATH_UPLOAD_URL";
	/**
	 * 系统内置参数，项目classes所在目录<br>
	 * 例如：/var/lib/project/users/WEB-INF/classes
	 */
	public static final String EWAdotREALdotPATH = "EWA.REAL.PATH";

	/**
	 * 系统内置参数，配置项目导入、导出目录
	 */
	public static final String EWAdotGROUPdotPATH = "EWA.GROUP.PATH";

	public static final String EWAdotHOST = "EWA.HOST";
	/**
	 * 网站地址，例如：https://www.gdxsoft.com
	 */
	public static final String EWAdotHTTP = "EWA.HTTP";

	/**
	 * 网站的BASE地址
	 */
	public static final String EWAdotHOST_BASE = "EWA.HOST_BASE";
	 
	/**
	 * 网站的端口
	 */
	public static final String EWAdotHOST_PORT = "EWA.HOST_PORT";
	/**
	 * 网站的协议
	 */
	public static final String EWAdotHOST_PROTOCOL = "EWA.HOST_PROTOCOL"; // 协议
	/**
	 * WEB服务当前项目目录，例如 /users，同EWACP
	 */
	public static final String EWAdotHOST_CONTEXT = "EWA.HOST_CONTEXT";

	/**
	 * 日志
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(RequestValue.class);

	private PageValues _ReqValues = new PageValues();
	private HttpServletRequest _Request;
	private HttpSession _Session;
	private String _ContextPath;
	/**
	 * 用于Cache文件用的标记
	 */
	private int _ParameterHashCode = 0;

	private Map<String, JSONObject> mapJson_;

	private boolean jsonBodyParameters;

	public RequestValue() {
		initSysParameters();
	}

	/**
	 * 获取系统语言
	 * 
	 * @return
	 */
	public String getLang() {
		// 字符集
		String lang = this.s(FrameParameters.EWA_LANG);
		if (lang == null || lang.trim().length() == 0) {
			lang = this.s(FrameParameters.SYS_EWA_LANG); // 从session取
		}
		// //避免跨站脚本攻击漏洞 2015-2-4
		// 因为 FrameFrame.createJsFramePage sJs.append("\r\nEWA.LANG='" +
		// lang.toLowerCase()+ "'; //page language\r\n");
		if (lang == null || lang.trim().length() == 0 || !lang.equalsIgnoreCase("enus")) {
			lang = "zhcn";// 默认字符集为简体中文
		} else {
			lang = "enus";
		}

		return lang;
	}

	public String queryToJson() {
		MStr s = new MStr();
		MTable qv = this._ReqValues.getQueryValues();
		s.a("{");
		for (int i = 0; i < qv.getCount(); i++) {
			Object key = qv.getKey(i);
			Object val = qv.getByIndex(i);
			if (key == null || val == null) {
				continue;
			}
			PageValue pv = (PageValue) val;

			s.a(Utils.toJsonPair(key.toString(), pv.getStringValue()));
			s.a(",");
		}
		s.a(Utils.toJsonPair(SYS_UNID, this.getString(SYS_UNID)));
		s.a("}");
		return s.toString();
	}

	/**
	 * 系统参数，日期，时间，guid，路径等
	 * 
	 */
	public void initSysParameters() {

		this._ReqValues.addValue(EWAdotGROUPdotPATH, UPath.getGroupPath(), PageValueTag.SYSTEM);
		// this._ReqValues.addValue(EWAdotSCRIPTdotPATH, UPath.getScriptPath(),
		// PageValueTag.SYSTEM);
		// this._ReqValues.addValue("EWA.CONFIG.PATH", UPath.getConfigPath(),
		// PageValueTag.SYSTEM);
		this._ReqValues.addValue(EWAdotPATH_UPLOAD_URL, UPath.getPATH_UPLOAD_URL(), PageValueTag.SYSTEM);

		this._ReqValues.addValue(EWAdotPATH_UPLOAD, UPath.getPATH_UPLOAD(), PageValueTag.SYSTEM);

		this._ReqValues.addValue(EWAdotPATH_IMG_CACHE_URL, UPath.getPATH_IMG_CACHE_URL(), PageValueTag.SYSTEM);
		this._ReqValues.addValue(EWAdotPATH_IMG_CACHE, UPath.getPATH_IMG_CACHE(), PageValueTag.SYSTEM);

		// src 所在目录
		this._ReqValues.addValue(EWAdotREALdotPATH, UPath.getRealPath(), PageValueTag.SYSTEM);

		// 在 ewa_conf中的全局参数,可以被系统调用
		// <requestValuesGlobal>
		// <rv name="rv_ewa_style_path" value="/demo/EmpScriptV2" />
		// </requestValuesGlobal>
		ConfRequestValuesGlobal.getInstance().getLst().forEach(v -> {
			_ReqValues.addValue(v.getName(), v.getValue(), PageValueTag.SYSTEM);
		});

		/*
		 * if (UPath.getRV_GLOBALS() != null) { HashMap<String, String> rvGlobal =
		 * UPath.getRV_GLOBALS(); for (String key : rvGlobal.keySet()) { String
		 * valGlobal = rvGlobal.get(key); this._ReqValues.addValue(key, valGlobal,
		 * PageValueTag.SYSTEM); } }
		 */
		this.resetSysUnid();

		this.resetDateTime();
	}

	/**
	 * 重新设置日期时间参数
	 */
	public void resetDateTime() {
		Calendar cal = Calendar.getInstance();

		// year
		String y = cal.get(Calendar.YEAR) + "";
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotYEAR, y, PageValueTag.SYSTEM);

		// month
		int m = cal.get(Calendar.MONTH) + 1;
		String mstr = m < 10 ? "0" + m : m + "";
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotMONTH, mstr, PageValueTag.SYSTEM);

		// day of month
		int d = cal.get(Calendar.DAY_OF_MONTH);
		String dstr = d < 10 ? "0" + d : d + "";
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotDAY, dstr, PageValueTag.SYSTEM);

		// hour
		int h = cal.get(Calendar.HOUR_OF_DAY);
		String hstr = h < 10 ? "0" + h : h + "";
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotHOUR, hstr, PageValueTag.SYSTEM);

		// minute
		int mm = cal.get(Calendar.MINUTE);
		String mmstr = mm < 10 ? "0" + mm : mm + "";
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotMINUTE, mmstr, PageValueTag.SYSTEM);

		// second
		int ss = cal.get(Calendar.SECOND);
		String ssstr = ss < 10 ? "0" + ss : ss + "";
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotSECOND, ssstr, PageValueTag.SYSTEM);

		// 时间字符串
		String time = hstr + ":" + mmstr + ":" + ssstr;
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotTIME, time, PageValueTag.SYSTEM);
		// date
		PageValue pv = new PageValue();
		pv.setName(SYS_DATE);
		pv.setValue(cal.getTime());
		pv.setDataType("Date");
		pv.setPVTag(PageValueTag.SYSTEM);
		this._ReqValues.addOrUpdateValue(pv);

		PageValue pv1 = new PageValue();
		pv1.setName(EWAdotDATE);
		pv1.setValue(cal.getTime());
		pv1.setDataType("Date");
		pv1.setPVTag(PageValueTag.SYSTEM);
		this._ReqValues.addOrUpdateValue(pv1);

		// date str
		String dateStr = y + "-" + mstr + "-" + dstr;
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotSTR, dateStr, PageValueTag.SYSTEM);

		// date str
		String dateStr1 = dateStr + " " + time;
		this._ReqValues.addOrUpdateValue(EWAdotDATEdotSTR1, dateStr1, PageValueTag.SYSTEM);
		this._ReqValues.addOrUpdateValue(EWAdotDATETIMEdotSTR, dateStr1, PageValueTag.SYSTEM);

	}

	/**
	 * 修改Sys_unid/EWA.ID的参数
	 */
	public void resetSysUnid() {
		String guid = Utils.getGuid();
		this._ReqValues.addOrUpdateValue(SYS_UNID, guid, PageValueTag.SYSTEM);
		this._ReqValues.addOrUpdateValue(EWAdotID, guid, PageValueTag.SYSTEM);
	}

	/**
	 * 获取字符串
	 * 
	 * @param name
	 * @return
	 */
	public String getString(String name) {
		if (name == null) {
			return null;
		}
		String v = this._ReqValues.getString(name);
		if (v == null) {
			v = this.getOtherValue(name);
			// 获取例如 json={"ADM_NAME":"GDX", "ADM_ID", 19}放到session中key =ADM_USER
			// 用户获取参数 @ADM_USER.ADM_NAME
			if (v == null && name.indexOf(".") > 0) {
				Object v1 = this.getJsonValue(name);
				if (v1 != null) {
					v = v1.toString();
				}
			}
		}

		return v;
	}

	/**
	 * 获取其它值 EWA.HOST，EWA.HOST_PORT，EWA.HOST_PROTOCOL，EWA.HOST_BASE，EWA.HOST.CONTEXT
	 * <br>
	 * xxxx.HASH 参数xxxx的 hashCode <br>
	 * xxxx.MD5 参数xxxx的md5值 <br>
	 * xxxx.SHA1 参数xxxx的sha1值 <br>
	 * xxxx.SHA256 参数xxxx的sha256值 <br>
	 * xxxx.SM3 参数xxxx的sm3值 <br>
	 * 
	 * @param name
	 * @return
	 */
	public String getOtherValue(String name) {
		String name1 = name.toUpperCase().trim();
		String v = null;
		if (name1.endsWith(".HASH")) {
			String name2 = name1.substring(0, name1.length() - 5);
			PageValue pv = this._ReqValues.getPageValue(name2);
			if (pv == null || pv.getValue() == null) {
				return null;
			}
			String dt = pv.getDataType();
			dt = dt == null ? "STRING" : dt.toUpperCase().trim();
			String v1;
			if (dt.equals("BINARY") || dt.equals("B[")) {
				try {
					byte[] b = (byte[]) pv.getValue();
					v1 = new String(b, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					return null;
				}
			} else {
				v1 = pv.getStringValue();
			}
			if (v1 != null) {
				v = v1.hashCode() + "";
			}
		} else if (name1.endsWith(".MD5") || name1.endsWith(".SHA1") || name1.endsWith(".SHA256")
				|| name1.endsWith(".SM3")) {
			String name2 = name1.substring(0, name1.length() - 4);
			PageValue pv = this._ReqValues.getPageValue(name2);
			if (pv == null || pv.getValue() == null) {
				return null;
			}
			byte[] buf = this.getPvBytes(pv);
			try {
				if (name1.endsWith(".SHA1")) {
					v = UDigest.digestHex(buf, "sha1");
				} else if (name1.endsWith(".SHA256")) {
					v = UDigest.digestHex(buf, "sha256");
				} else if (name1.endsWith(".SM3")) {
					v = UDigest.digestHex(buf, "sm3"); // 国密3
				} else {
					v = UDigest.digestHex(buf, "md5");
				}
			} catch (Exception err) {
				v = err.getMessage();
			}
		}
		return v;
	}

	private byte[] getPvBytes(PageValue pv) {
		if (pv == null || pv.getValue() == null) {
			return null;
		}
		String dt = pv.getDataType();
		dt = dt == null ? "STRING" : dt.toUpperCase().trim();
		byte[] buf;
		if (dt.equals("BINARY") || dt.equals("B[")) {
			buf = (byte[]) pv.getValue();
		} else {
			try {
				buf = pv.getStringValue().getBytes("UTF8");
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}

		return buf;
	}

	/**
	 * 从JSON对象中获取参数，例如：<br>
	 * json = {"ADM_NAME":"GDX", "ADM_ID", 19}放到session中key =ADM_USER<br>
	 * 用户获取参数 @ADM_USER.ADM_NAME
	 * 
	 * @param name
	 * @return
	 */
	public Object getJsonValue(String name) {
		if (name.indexOf(".") <= 0) {
			return null;
		}
		int lastloc = name.lastIndexOf(".");
		String name0 = name.substring(0, lastloc).toUpperCase();
		String name1 = name.substring(lastloc + 1);

		JSONObject json = null;
		if (this.mapJson_ == null) {
			mapJson_ = new HashMap<String, JSONObject>();
		}
		if (mapJson_.containsKey(name0)) {
			json = mapJson_.get(name0);
		} else {
			// 先从 Session中获取对象
			Object obj = this.getPageValues().getSessionObject(name0);
			if (obj == null) {
				// 再从 request的attribute中获取对象
				obj = this.getRequestAttribute(name0);
			}
			if (obj == null) {
				mapJson_.put(name0, null);
				return null;
			}

			String className = obj.getClass().getName();
			// System.out.println(className);

			if (!className.equals("org.json.JSONObject")) {
				mapJson_.put(name0, null);
				return null;
			}
			try {
				json = (JSONObject) obj;
				mapJson_.put(name0, json);
			} catch (Exception err) {
				mapJson_.put(name0, null);
				return null;
			}

		}
		if (json == null) {
			return null;
		}
		if (json.has(name1)) {
			return json.optString(name1);
		} else if (json.has(name1.toLowerCase())) {
			return json.optString(name1.toLowerCase());
		} else if (json.has(name1.toUpperCase())) {
			return json.optString(name1.toUpperCase());
		} else {
			return null;
		}
	}

	/**
	 * 从 request 中获取对象(request.getAttribute )
	 * 
	 * @param key attribute name
	 * @return
	 */
	public Object getRequestAttribute(String key) {
		HttpServletRequest req = this.getRequest();
		if (req == null) {
			return null;
		}
		Enumeration<String> atts = req.getAttributeNames();
		if (atts == null) {
			return null;
		}
		while (atts.hasMoreElements()) {
			String key1 = (String) atts.nextElement();
			if (key1.equalsIgnoreCase(key)) {
				return req.getAttribute(key);
			}
		}

		return null;
	}

	/**
	 * 获取字符串 ,同 getString
	 * 
	 * @param name 参数名称
	 * @return
	 */
	public String s(String name) {
		return this.getString(name);
	}

	/**
	 * 获取限定长度的字符串
	 * 
	 * @param name      参数名称
	 * @param maxLength 最大长度
	 * @return 限定长度的字符串
	 */
	public String s(String name, int maxLength) {
		String val = this.getString(name);
		if (val == null || val.length() <= maxLength) {
			return val;
		}

		return val.substring(0, maxLength);
	}

	/**
	 * 判断对象是否为null
	 * 
	 * @param name 参数名称
	 * @return 是/否
	 */
	public boolean isNull(String name) {
		return this.s(name) == null;
	}

	/**
	 * 判断对象是否非null
	 * 
	 * @param name
	 * @return
	 */
	public boolean isNotNull(String name) {
		return !this.isNull(name);
	}

	/**
	 * 判断对象是否为null 或 空白
	 * 
	 * <pre>
	 * isBlank(null)      = true
	 * isBlank("")        = true
	 * isBlank(" ")       = true
	 * isBlank("bob")     = false
	 * isBlank("  bob  ") = false
	 * </pre>
	 * 
	 * @param name 参数名称
	 * @return 是/否
	 */
	public boolean isBlank(String name) {
		String s = this.getString(name);
		return s == null || s.trim().length() == 0;
	}

	/**
	 * 判断对象非null 或 空白
	 * 
	 * @param name 参数名称
	 * @return 是/否
	 */
	public boolean isNotBlank(String name) {
		return !this.isBlank(name);
	}

	/**
	 * 获取整型
	 * 
	 * @param name 参数名称
	 * @return 整型
	 */
	public int getInt(String name) {
		return Integer.parseInt(this.getString(name));

	}

	/**
	 * 获取长整型
	 * 
	 * @param name 参数名称
	 * @return 长整型
	 */
	public long getLong(String name) {
		return Long.parseLong(this.getString(name));
	}

	/**
	 * 获取时间
	 * 
	 * @param name 参数名称
	 * @param lang 语言类型
	 * @return 时间
	 */
	public Date getDate(String name, String lang) {
		String s = this.getString(name);
		if (s == null) {
			return null;
		}
		Timestamp t = Utils.getTimestamp(s, lang, false);

		Date t1 = new Date(t.getTime());
		return t1;
	}

	/**
	 * 获取时间(语言类型按照系统值)
	 * 
	 * @param name 参数名称
	 * @return 时间
	 */
	public Date getDate(String name) {
		return this.getDate(name, this.getLang());
	}

	/**
	 * 获取双精度
	 * 
	 * @param name 参数名称
	 * @return 双精度
	 */
	public Double getDouble(String name) {
		String s = this.getString(name);
		if (s == null) {
			return null;
		}
		return Double.parseDouble(s);
	}

	/**
	 * 获取对象
	 * 
	 * @param name 参数名称
	 * @return
	 */
	public Object getObject(String name) {
		return this._ReqValues.getObject(name);
	}

	private void addQueryValues(String queryString) {
		if (queryString == null)
			return;
		String[] s1 = queryString.split("&");
		StringBuilder parameters = new StringBuilder();
		StringBuilder parameters1 = new StringBuilder();
		for (int i = 0; i < s1.length; i++) {
			if (s1[i].trim().length() == 0) {
				continue;
			}
			int equalLoc = s1[i].indexOf("=");
			if (equalLoc <= 0) {
				continue;
			}

			String key = s1[i].substring(0, equalLoc);
			String val = s1[i].substring(equalLoc + 1);
			if (val.indexOf("%") >= 0) {
				try {
					val = java.net.URLDecoder.decode(val, "utf-8");
				} catch (Exception e) {

				}
			}
			this._ReqValues.addValue(key, val, PageValueTag.QUERY_STRING);
			if (parameters.length() > 0) {
				parameters.append("&");
			}

			if (key.equalsIgnoreCase("_R")) {
				continue;
			}

			String v1;
			try {
				v1 = java.net.URLEncoder.encode(val, "utf-8");
			} catch (Exception err) {
				v1 = val;
			}
			parameters.append(key);
			parameters.append("=");
			parameters.append(v1);

			if (key.equalsIgnoreCase(FrameParameters.XMLNAME) || key.equalsIgnoreCase(FrameParameters.ITEMNAME)
					|| key.toUpperCase().startsWith("EWA_")) {
				continue;
			}
			if (parameters1.length() > 0) {
				parameters1.append("&");
			}
			parameters1.append(key);
			parameters1.append("=");

			parameters1.append(v1);
		}
		this.addValue(EWA_QUERY_ALL, parameters.toString());
		this.addValue(EWA_QUERY, parameters1.toString());
	}

	public void reloadQueryValues(String queryString) {
		this._ReqValues.getTagValues(PageValueTag.QUERY_STRING).clear();
		this.addQueryValues(queryString);
	}

	/**
	 * 初始化参数表 session=req.getSession()
	 * 
	 * @param req     Request
	 * @param session Sessions
	 */
	public RequestValue(HttpServletRequest req) {
		this.initRequest(req, req.getSession(false));
	}

	/**
	 * 初始化参数表 session=req.getSession()
	 * 
	 * @param req                Request
	 * @param jsonBodyParameters 通过body提交的json参数
	 */
	public RequestValue(HttpServletRequest req, boolean jsonBodyParameters) {
		this.jsonBodyParameters = jsonBodyParameters;
		this.initRequest(req, req.getSession(false));
	}

	/**
	 * 初始化参数表
	 * 
	 * @param req     Request
	 * @param session Sessions
	 */
	public RequestValue(HttpServletRequest req, HttpSession session) {
		this.initRequest(req, session);
	}

	/**
	 * 初始化参数表
	 * 
	 * @param req     Request
	 * @param session Sessions
	 */
	private void initRequest(HttpServletRequest req, HttpSession session) {
		_Request = req;
		this._Session = session;

		initSysParameters();

		if (this._Session != null) {
			this.addSessions(session);
		}

		if (req == null) {
			return;
		}

		String ctx = req.getContextPath();
		int inc = 0;
		// 避免 http://www.gdxsoft.com/////////users/////////userinfo.jsp?js_debug=1 情况出现
		while (ctx.startsWith("//")) {
			ctx = ctx.replace("//", "/");
			inc++;
			if (inc > 500) {
				break;
			}
		}
		_ContextPath = ctx;

		this._ReqValues.addValue(SYS_CONTEXTPATH, ctx, PageValueTag.SYSTEM);
		this._ReqValues.addValue(EWAdotCP, ctx, PageValueTag.SYSTEM);
		this._ReqValues.addValue(EWAdotHOST_CONTEXT, ctx, PageValueTag.SYSTEM);

		Enumeration<?> enums = _Request.getHeaderNames();
		int inc1 = 0;
		Map<String, String> headers = new HashMap<String, String>();
		while (enums.hasMoreElements()) {
			String name = enums.nextElement().toString();
			String val = _Request.getHeader(name).toString();

			headers.put(name, val);
			if (inc1 > 100) {
				break;
			}
			inc1++;
		}

		this.initParametersByHeaders(headers);

		if (this.s(SYS_REMOTEIP) == null) {
			// 从header未取得 X-Real-IP和X-Forwarded-For
			String ip = req.getRemoteAddr();
			this._ReqValues.addValue(SYS_REMOTEIP, ip, PageValueTag.SYSTEM);
		}

		UUrl uu = new UUrl(req);

		//域名
		this._ReqValues.addValue(EWAdotHOST, req.getServerName(), PageValueTag.SYSTEM);
		
		//网站的BASE地址
		this._ReqValues.addValue(EWAdotHOST_BASE, uu.getRoot()+req.getContextPath()+"/", PageValueTag.SYSTEM);
		
		this._ReqValues.addValue(EWAdotHOST_PORT, req.getServerPort(), PageValueTag.SYSTEM);
		
		this._ReqValues.addValue(EWAdotHOST_PROTOCOL, uu.getRoot().startsWith("https")?"https":"http", PageValueTag.SYSTEM);
		// 网址,例如 https://www.gdxsoft.com
		this._ReqValues.addValue(EWAdotHTTP, uu.getRoot(), PageValueTag.SYSTEM);

		this._ReqValues.addValue(EWAdotCPF, uu.getUrl(false), PageValueTag.SYSTEM);
		this._ReqValues.addValue(EWAdotCPF_ALL, uu.getUrl(true), PageValueTag.SYSTEM);

		// 全网址，不含 querystring
		// String s1 = req.getRequestURL().toString();
		this._ReqValues.addValue(SYS_REMOTE_URL, uu.getUrlWithDomain(false), PageValueTag.SYSTEM);
		this._ReqValues.addValue(SYS_REMOTE_URL_ALL, uu.getUrlWithDomain(true), PageValueTag.SYSTEM);

		
		this.addQueryValues(_Request.getQueryString());

		if (this.jsonBodyParameters) {
			// 获取通过 body提交的 json 参数
			this.loadJsonBodyParameters(req);
		} else {
			// 页面提交的参数
			this.addParameter(req);
		}
		// ��ȡCookie ֵ
		this.addCookies(req.getCookies());

	}

	public void initParametersByHeaders(Map<String, String> headers) {
		String x_real_ip = "";
		String x_forwarded_for = "";
		for (String key : headers.keySet()) {
			String val = headers.get(key);
			if (key.equalsIgnoreCase("referer")) {
				this._ReqValues.addValue(SYS_REMOTE_REFERER, val, PageValueTag.SYSTEM);
			} else if (key.equalsIgnoreCase("user-agent")) {
				this._ReqValues.addValue(SYS_USER_AGENT, val, PageValueTag.SYSTEM);
			} else if (key.equalsIgnoreCase("X-Real-IP")) {
				this._ReqValues.addValue("SYS_x-real-ip", val, PageValueTag.SYSTEM);
				x_real_ip = val;
			} else if (key.equalsIgnoreCase("X-Forwarded-For")) {
				// X-Forwarded-For:简称XFF头，它代表客户端，也就是HTTP的请求端真实的IP，只有在通过了HTTP
				// 代理或者负载均衡服务器时才会添加该项。它不是RFC中定义的标准请求头信息，在squid缓存代理服务器开发文档中可以找到该项的详细介绍。
				// 标准格式如下：
				// X-Forwarded-For: client1, proxy1, proxy2
				// 从标准格式可以看出，X-Forwarded-For头信息可以有多个，中间用逗号分隔，第一项为真实的客户端ip，剩下的就是曾经经过的代理或负载均衡的ip地址，经过几个就会出现几个。
				// x-forwarded-for=111.197.114.105, 47.104.164.211

				this._ReqValues.addValue("SYS_X-Forwarded-For", val, PageValueTag.SYSTEM);
				String[] ips = val.split(",");
				for (int i = 0; i < ips.length; i++) {
					this._ReqValues.addValue("SYS_X-Forwarded-For" + i, ips[i], PageValueTag.SYSTEM);
				}
				if (ips.length > 0) {// 用户ip取第0个
					x_forwarded_for = ips[0];
				}
			}
		}

		if (x_forwarded_for.length() > 0) {
			this._ReqValues.addValue(SYS_REMOTEIP, x_forwarded_for, PageValueTag.SYSTEM);
		} else if (x_real_ip.length() > 0) {
			this._ReqValues.addValue(SYS_REMOTEIP, x_real_ip, PageValueTag.SYSTEM);
		}

	}

	

	private void loadJsonBodyParameters(HttpServletRequest request) {
		String bodyContent = null;
		String charset = request.getCharacterEncoding();
		if (StringUtils.isBlank(charset)) {
			charset = "utf8";
		}
		try {
			bodyContent = UHtml.getHttpBody(request, charset);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return;
		}

		if (StringUtils.isBlank(bodyContent)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(bodyContent);
			this.addValues(obj);
		} catch (Exception err) {
			LOGGER.warn(err.getMessage());
		}
	}

	private void addParameter(HttpServletRequest req) {
		 
		Enumeration<?> ee = req.getParameterNames();
		MTable mt = new MTable();
		while (ee.hasMoreElements()) {
			String RKey = ee.nextElement().toString().trim();
			if (RKey.indexOf(":") > 0 || RKey.indexOf("__EVENT") == 0 || RKey.indexOf("__VIEWSTATE") >= 0) {
				continue;
			}
			String[] vals = req.getParameterValues(RKey);

			if (vals == null || vals.length == 0)
				continue;

			String RVal = req.getParameter(RKey);
			if (this._ReqValues.getQueryValue(RKey) != null) {
				String qv = this._ReqValues.getQueryValue(RKey);
				if (qv.indexOf("%") >= 0) {
					try {
						qv = java.net.URLDecoder.decode(RVal, "utf-8");
					} catch (Exception e) {
						qv = RVal;
					}
					// qv = Utils.getUtf8(RVal);
				} else {
					qv = RVal;
				}

				if (vals.length == 2) {// form and query
					// form 优先级高
					RVal = qv.equals(vals[0]) ? vals[1] : vals[0];
					if (RVal.equals("undefined")) {// 属于在页面未找到
						RVal = qv;
					}
				} else {
					RVal = qv;
				}
			}
			if (RKey.toLowerCase().endsWith("_base64")) {
				try {
					byte[] bytes = UConvert.FromBase64String(RVal);
					this._ReqValues.addValue(RKey, bytes, PageValueTag.FORM);
					PageValue pv = this._ReqValues.getPageValue(RKey);
					pv.setDataType("binary");

				} catch (Exception err) {
					LOGGER.error(err.getMessage());
				}

			}
			this._ReqValues.addValue(RKey, RVal, PageValueTag.FORM);

			if (!RKey.equals("_R")) {
				mt.add(RKey, RVal);
			}
		}
		mt.sort();

		this._ParameterHashCode = mt.join("=!@#ggg!@dd!@~~x=", "&j%^&zy&").hashCode();

		// 将 request.setAttribute 的值取出， 2019-09-29 郭磊
		// 在使用htmlcontol 传递参数不暴露在html上
		Enumeration<String> atts = req.getAttributeNames();
		if (atts != null) {
			while (atts.hasMoreElements()) {
				String key = (String) atts.nextElement();
				Object val = req.getAttribute(key);
				if (val == null) {
					continue;
				}
				String name = val.getClass().getName();
				// System.out.println(name);
				if (name.equals("java.lang.String") || name.equals("java.lang.Boolean")
						|| name.equals("java.lang.Integer") || name.equals("java.lang.Long")
						|| name.equals("java.lang.Float") || name.equals("java.lang.Double")
						|| name.equals("java.util.Date") || name.equals("[B") || name.equals("java.lang.Character")
						|| name.equals("java.lang.Short")) {
					// 数字，时间，字符串，字符，二进制
					this._ReqValues.addValue(key, val, PageValueTag.SYSTEM);
				}
			}
		}
	}

	/**
	 * 将Sessions加入到参数表中
	 * 
	 * @param session
	 */
	private void addSessions(HttpSession session) {
		if (session == null) {
			return;
		}
		Enumeration<?> sessions = session.getAttributeNames();
		while (sessions.hasMoreElements()) {
			String RKey = sessions.nextElement().toString().trim();
			Object RVal = session.getAttribute(RKey);
			this._ReqValues.addValue(RKey, RVal, PageValueTag.SESSION);
		}
	}

	/**
	 * 重新加载session，会清除以前的session值
	 * 
	 * @param session
	 */
	public void reloadSessions(HttpSession session) {
		this._ReqValues.getTagValues(PageValueTag.SESSION).clear();
		this.addSessions(session);
	}

	/**
	 * 重新加载cookies, 会清除以前的cookies
	 * 
	 * @param cc
	 */
	public void reloadCookies(Cookie[] cc) {
		this._ReqValues.getTagValues(PageValueTag.COOKIE).clear();
		this._ReqValues.getTagValues(PageValueTag.COOKIE_ENCYRPT).clear();

		this.addCookies(cc);
	}

	/**
	 * 将Cookie加入到参数表中
	 * 
	 * @param cc
	 */
	private void addCookies(Cookie[] cc) {
		if (cc == null) {
			return;
		}

		IUSymmetricEncyrpt symmetric = null;
		// defined in ewa_conf securities -> security
		if (ConfSecurities.getInstance() != null && ConfSecurities.getInstance().getDefaultSecurity() != null) {
			symmetric = ConfSecurities.getInstance().getDefaultSecurity().createSymmetric();
		}

		String ckPrefix = ActionBase.COOKIE_NAME_PREFIX;
		for (int i = 0; i < cc.length; i++) {
			String key = cc[i].getName().toUpperCase().trim();

			// 过滤掉所有以EWA_开头的cookie,不应存在的数据
			// 时差可以放到 cookie 里
			if (key.startsWith("EWA_") && !key.endsWith(ckPrefix) && !key.equals(FrameParameters.EWA_TIMEDIFF)) {
				continue;
			}

			if (key.endsWith(ckPrefix)) {
				this.addEncryptedCookie(cc[i], symmetric);
			} else {
				String val = cc[i].getValue();
				// 对cookie值进行UrlDecode操作
				val = UCookies.decodeCookieValue(val);
				this._ReqValues.addValue(key, val, PageValueTag.COOKIE);
			}
		}
	}

	/**
	 * Decrypt the encryption cookie
	 * 
	 * @param ck        the cookie
	 * @param symmetric
	 */
	private void addEncryptedCookie(Cookie ck, IUSymmetricEncyrpt symmetric) {
		if (symmetric == null) {
			String err = "No default symmetric defined, in the ewa_conf.xml securities->security";
			LOGGER.warn(err);
			return;
		}
		String key = ck.getName().toUpperCase().trim();
		boolean ewaEncrypted = key.endsWith(ActionBase.COOKIE_NAME_PREFIX);
		// 过滤掉所有以EWA_开头的cookie,不应存在的数据
		// 时差可以放到 cookie 里
		if (key.startsWith("EWA_") && !ewaEncrypted && !key.equals(FrameParameters.EWA_TIMEDIFF)) {
			return;
		}

		String val = ck.getValue();
		// 解密加密的Cookie
		if (val == null || val.trim().length() == 0) {
			// 值为空白的加密cookie不处理
			return;
		}

		// 对cookie值进行UrlDecode操作
		String decodeVal = UCookies.decodeCookieValue(val);
		if (!ewaEncrypted) {
			this._ReqValues.addValue(key, decodeVal, PageValueTag.COOKIE);
		}

		String fixedKey = key.substring(0, key.length() - ActionBase.COOKIE_NAME_PREFIX.length());
		try {
			String plainText = symmetric.decrypt(decodeVal);
			this._ReqValues.addValue(fixedKey, plainText, PageValueTag.COOKIE_ENCYRPT);
		} catch (Exception e) {
			LOGGER.warn("Decrypte the cookie " + key + "=" + val + ", " + e.getLocalizedMessage());
		}
	}

	/**
	 * 添加表到Rv中, 只有第一行数据
	 * 
	 * @param table 表
	 * @return 添加的字段列表，null表示表无数据或表有错误
	 */
	public List<String> addValues(DTTable table) {
		if (table == null || !table.isOk() || table.getCount() == 0) {
			return null;
		}
		DTRow r = table.getRow(0);

		return addValues(r);
	}

	/**
	 * 添加数据行到Rv中
	 * 
	 * @param r 数据行
	 * @return 添加的字段列表
	 */
	public List<String> addValues(DTRow r) {
		if (r == null) {
			return null;
		}
		List<String> addList = new ArrayList<String>();
		DTTable table = r.getTable();
		for (int i = 0; i < table.getColumns().getCount(); i++) {
			String key = table.getColumns().getColumn(i).getName();
			String type = table.getColumns().getColumn(i).getTypeName();
			int length = table.getColumns().getColumn(i).getLength();
			PageValue pv = new PageValue();
			pv.setName(key.toUpperCase());
			pv.setDataType(type);
			pv.setLength(length);
			pv.setValue(r.getCell(i).getValue());
			pv.setPVTag(PageValueTag.DTTABLE);
			this._ReqValues.addOrUpdateValue(pv);

			addList.add(key);
		}
		return addList;
	}

	/**
	 * 添加JSONObject到Rv中
	 * 
	 * @param json JSONObject
	 * @return 添加的字段
	 */
	public List<String> addValues(JSONObject json) {
		if (json == null) {
			return null;
		}
		List<String> addList = new ArrayList<String>();
		Iterator<?> it = json.keys();
		while (it.hasNext()) {
			String key = it.next().toString();
			try {
				Object val = json.get(key);
				if (val == null) {
					continue;
				}
				this._ReqValues.remove(key);

				if (val instanceof JSONArray) {
					JSONArray arr = (JSONArray) val;
					byte[] bufs = new byte[arr.length()];
					for (int i = 0; i < arr.length(); i++) {
						bufs[i] = Byte.parseByte(arr.getInt(i) + "");
					}
					this.addValue(key, bufs, "binary", bufs.length);
				} else {
					String valStr = val.toString();
					if (valStr.indexOf("##BINARY_FILE[") >= 0 && valStr.indexOf("]BINARY_FILE##") > 0) {
						// 二进制文件REF，由DTTable.toJSONArrayBinaryToFile 创建
						valStr = valStr.replace("##BINARY_FILE[", "").replace("]BINARY_FILE##", "");
						byte[] bufs = getJsonFileRef(valStr);
						if (bufs != null) {
							this.addValue(key, bufs, "binary", bufs.length);
						}
					} else {
						this.addValue(key, val);
					}
				}
				addList.add(key);
			} catch (JSONException e) {
			}
		}
		return addList;
	}

	private byte[] getJsonFileRef(String valStr) {
		JSONObject jsonFileRef;
		try {
			jsonFileRef = new JSONObject(valStr);
			String name = jsonFileRef.getString("NAME");
			String path = jsonFileRef.getString("PATH");
			String url = jsonFileRef.getString("URL");

			String name1 = path + "/" + name;
			File f1 = new File(name1);

			// 读取本地文件
			if (f1.exists()) {
				return UFile.readFileBytes(name1);
			}

			// 通过http调用
			UNet net = new UNet();
			String url1 = url + "/" + name;
			return net.downloadData(url1);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}

	}

	public void addValue(PageValue pv) {
		String key = pv.getName().toUpperCase().trim();
		MTable pvs = this._ReqValues.getFormValues();
		if (pvs.containsKey(key)) {
			pvs.removeKey(key);
		}
		this._ReqValues.getFormValues().put(key, pv);
	}

	/**
	 * 增加参数到其他数据组（PageValueTag.OTHER）
	 * 
	 * @param key
	 * @param val
	 */
	public void addValue(String key, Object val) {
		this._ReqValues.addValue(key, val, PageValueTag.OTHER);
	}

	public void addValue(String key, Object val, PageValueTag pvTag) {
		this._ReqValues.addValue(key, val, pvTag);
	}

	/**
	 * 增加参数，用于ClassDao，参数添加到PageValueTag.OTHER<br>
	 * 如果参数已经存在，则替换参数
	 * 
	 * @param key
	 * @param val
	 * @param dataType 数据类型
	 * @param len
	 */
	public void addValue(String key, Object val, String dataType, int len) {
		PageValue pv = new PageValue();
		pv.setName(key.trim().toUpperCase());
		pv.setValue(val);
		pv.setDataType(dataType);
		pv.setLength(len);
		pv.setPVTag(PageValueTag.OTHER);
		this._ReqValues.remove(pv.getName(), pv.getPVTag());
		this._ReqValues.addValue(pv);
	}

	/**
	 * 修改参数，如果参数不存在，则不修改
	 * 
	 * @param Key
	 * @param Val
	 * @param dataType
	 * @param maxLength
	 */
	public void changeValue(String Key, Object Val, String dataType, int maxLength) {
		PageValue pv = this._ReqValues.getValue(Key);
		if (pv == null)
			return;
		pv.setDataType(dataType);
		pv.setLength(maxLength);
		pv.setValue(Val);
	}

	/**
	 * 新增或修改任意等级的 参数
	 * 
	 * @param Key
	 * @param Val
	 */
	public void addOrUpdateValue(String Key, Object Val) {
		this.addOrUpdateValue(Key, Val, "String", -1);
	}

	/**
	 * 新增或修改任意等级的 参数
	 * 
	 * @param Key
	 * @param Val
	 * @param dataType
	 * @param maxLength
	 */
	public void addOrUpdateValue(String Key, Object Val, String dataType, int maxLength) {
		PageValue pv = new PageValue();

		Key = Key.toUpperCase(); // 2019-04-12

		pv.setName(Key); // 为啥原来没有设定呢？？？？？ 2019-01-10

		pv.setDataType(dataType);
		if (maxLength > 0) {
			pv.setLength(maxLength);
		}
		pv.setValue(Val);
		pv.setPVTag(PageValueTag.OTHER);

		// 删除所有等级下的参数
		this._ReqValues.remove(Key);

		// 添加到PageValueTag.OTHER下
		this._ReqValues.addValue(pv);
	}

	public String getContextPath() {
		return _ContextPath;
	}

	/**
	 * 获取所有参数的 JSON表达式
	 * 
	 * @return
	 */
	public JSONObject listValuesAsJson() {
		JSONObject obj = new JSONObject();
		PageValueTag[] tags = PageValueTag.getOrder();
		for (int i = 0; i < tags.length; i++) {
			PageValueTag tag = tags[i];
			MTable pvs = this._ReqValues.getTagValues(tag);
			JSONArray arr = new JSONArray();
			obj.put(tag.toString(), arr);
			for (int ia = 0; ia < pvs.getCount(); ia++) {
				Object key = pvs.getKey(ia);
				PageValue val = (PageValue) pvs.get(key);
				JSONObject item = new JSONObject();
				item.put(key.toString(), val.getValue());
				arr.put(item);
			}
		}

		return obj;
	}

	public String listValues(boolean isHtml) {
		StringBuilder sb = new StringBuilder();
		if (isHtml) {
			sb.append("<table border=0 " + "style='font-size:11px;font-family:arial;'"
					+ " cellpadding=3 cellspacing=1 bgcolor='buttonface'>");
		}
		PageValueTag[] tags = PageValueTag.getOrder();
		for (int i = 0; i < tags.length; i++) {
			PageValueTag tag = tags[i];
			MTable pvs = this._ReqValues.getTagValues(tag);
			sb.append(this.listValues(pvs, isHtml));
		}
		if (isHtml) {
			sb.append("</table>");
		}
		return sb.toString();
	}

	public String toString() {
		return this.listValues(false);
	}

	private String listValues(MTable pvs, boolean isHtml) {
		if (pvs == null) {
			return "";
		}
		pvs.sort();

		MStr sb = new MStr();
		for (int i = 0; i < pvs.getCount(); i++) {
			Object key = pvs.getKey(i);
			PageValue val = (PageValue) pvs.get(key);
			if (isHtml) {
				sb.append("<tr bgcolor=white><td>");
				sb.append(key);
				sb.append("</td><td>");
				sb.append(val.getPVTag());
				sb.append("</td><td>");
				sb.append(val.getDataType());
				sb.append("</td><td>");
				sb.append(val.getStringValue());
				sb.append("</td></tr>");
			} else {
				sb.append(key + "(" + val.getTag() + ")=" + val.getStringValue() + "\r\n");
			}
		}
		return sb.toString();
	}

	public String listValuesHtml() {
		String s1 = listValues(true);
		s1 = s1.replace(" = ", " = <span style='color:red'>");
		s1 = s1.replace("\r\n", "</span><br>");
		return s1;
	}

	/**
	 * 替换原始字符串中的@参数
	 * 
	 * @param exp 字符串表达式
	 * @return
	 */
	public String replaceParameters(String exp) {
		if (exp == null) {
			return null;
		}
		MListStr al = Utils.getParameters(exp, "@");
		String lang = this.getLang();
		for (int i = 0; i < al.size(); i++) {
			String paramName = al.get(i);
			String paramValue = null;

			paramValue = this.s(paramName);

			if (paramValue == null) {
				paramValue = "";
			} else if (paramName.toUpperCase().endsWith("_MONEY")) { // 货币类型
				paramValue = UFormat.formatMoney(paramValue);
			} else if (paramName.toUpperCase().endsWith("_TIME")) { //
				try {
					paramValue = UFormat.formatDate("time", paramValue, lang);
				} catch (Exception e) {
				}
			} else if (paramName.toUpperCase().endsWith("_DATE")) { //
				try {
					paramValue = UFormat.formatDate("date", paramValue, lang);
				} catch (Exception e) {
				}
			} else if (paramName.toUpperCase().endsWith("_DATETIME")) { //
				try {
					paramValue = UFormat.formatDate("datetime", paramValue, lang);
				} catch (Exception e) {
				}
			}

			String fullName = "@" + paramName;
			/*
			 * if (al.size() == 0 && fullName.equals(exp)) { return paramValue; }
			 */

			exp = exp.replaceFirst(fullName, Matcher.quoteReplacement(paramValue));
		}

		return exp;
	}

	/**
	 * form和query参数哈希值，用于cache文件用
	 * 
	 * @return the _ParameterHashCode
	 */
	public int getParameterHashCode() {
		return _ParameterHashCode;
	}

	/**
	 * @return the _Request
	 */
	public HttpServletRequest getRequest() {
		return _Request;
	}

	/**
	 * @return the _Session
	 */
	public HttpSession getSession() {
		return _Session;
	}

	/**
	 * @return the _ReqValues
	 */
	public PageValues getPageValues() {
		return _ReqValues;
	}

	/**
	 * 克隆这个对象
	 */
	public RequestValue clone() {
		RequestValue rv = new RequestValue();
		PageValues target = rv.getPageValues();
		this._ReqValues.copyTo(target);

		rv._Request = this._Request;
		rv._ContextPath = this._ContextPath;
		rv._Session = this._Session;
		return rv;

	}

	/**
	 * 是否通过body提交的json参数
	 * 
	 * @return
	 */
	public boolean isJsonBodyParameters() {
		return jsonBodyParameters;
	}

	public void setJsonBodyParameters(boolean jsonBodyParameters) {
		this.jsonBodyParameters = jsonBodyParameters;
	}
}