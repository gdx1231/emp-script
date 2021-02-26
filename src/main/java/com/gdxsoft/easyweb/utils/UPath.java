package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.cache.SqlCachedHsqldbImpl;
import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

public class UPath {

	/**
	 * 默认的ewa_conf的名字
	 */
	public static String CONF_NAME = null;
	/**
	 * ewa_conf.xml的 document对象
	 */
	private static Document CFG_XML_DOC;

	private static HashMap<String, String> RV_GLOBALS;

	private static HashMap<String, DKIMCfg> DKIM_CFGS;

	/**
	 * 获取邮件DKIM签名的配置
	 * 
	 * @return
	 */
	public static HashMap<String, DKIMCfg> getDKIM_CFGS() {
		initPath();
		return DKIM_CFGS;
	}

	/**
	 * 放到 RequestValue 的全局变量
	 * 
	 * @return
	 */
	public static HashMap<String, String> getRV_GLOBALS() {
		initPath();
		return RV_GLOBALS;
	}

	private static HashMap<String, String> RV_TYPES;

	/**
	 * 定义RequestValue的初始化类型，例如： USR_ID->int
	 * 
	 * @return
	 */
	public static HashMap<String, String> getRvTypes() {
		initPath();
		return RV_TYPES;
	}

	/**
	 * 项目Class所在目录
	 */
	public static String PATH_REAL = "";

	/**
	 * 临时目录根目录
	 */
	public static String PATH_TEMP = "_EWA_TMP_";
	/**
	 * 项目缓存目录
	 */
	public static String PATH_PRJ_CACHE = PATH_TEMP + "/PRJ/";
	/**
	 * 图片的缓存目录
	 */
	public static String PATH_IMG_CACHE = PATH_TEMP + "/IMG/";

	/**
	 * des="图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。"
	 * Name="img_tmp_path_url"，如果ewa_conf中没有配置的话，则取当前contextpath
	 */
	public static String PATH_IMG_CACHE_URL = null;
	/**
	 * 上传文件物理路径，来自ewa_conf中的 img_tmp_path <br>
	 * 如果ewa_conf中没有配置的话，则取当前contextpath
	 */
	private static String PATH_UPLOAD;
	/**
	 * 上传文件物理的URL<br>
	 * 来自ewa_conf中的 img_tmp_path_url <br>
	 * 如果ewa_conf中没有配置的话，则取当前contextpath
	 */
	private static String PATH_UPLOAD_URL;

	private static String PATH_SCRIPT = "";
	private static String PATH_CONFIG = "";
	private static String PATH_MANAGMENT = "";

	private static String SYSTEM_DB_PATH = "";
	private static String SYSTEM_DB_PASSWORD = "";
	private static long PROP_TIME = -1231;
	private static String PATH_GROUP = ""; // 用于组件的生成和导入目录

	private static String SMTP_IP = "127.0.0.1";
	private static int SMTP_PORT = 25;
	private static String SMTP_USER;
	private static String SMTP_PWD;

	private static MTableStr DEBUG_IPS; // 用于页面显示跟踪的IP地址
	private static boolean IS_DEBUG_SQL;

	private static boolean IS_WEB_CALL;

	private static MTableStr VALID_DOMAINS; // 合法的域名，用于合并css，js的合法域名检查，避免跨域攻击

	private static String CVT_OPENOFFICE_HOME;
	private static String CVT_SWFTOOL_HOME;
	private static String CVT_IMAGEMAGICK_HOME;

	private static Logger log = Logger.getLogger(UPath.class);
	static {
		// initPath();
	}

	/**
	 * 合法的域名，用于合并css，js的合法域名检查，避免跨域攻击
	 * 
	 * @return
	 */
	public static MTableStr getVALID_DOMAINS() {
		initPath();
		return VALID_DOMAINS;
	}

	/**
	 * 检查是否为合法域名
	 * 
	 * @param domain
	 * @return
	 */
	public static boolean checkIsValidDomain(String domain) {
		MTableStr map = getVALID_DOMAINS();
		if (domain == null || domain.trim().length() == 0) {
			return false;
		}
		String s = domain.trim().toLowerCase();
		if (map.containsKey(s)) {
			return true;
		}
		int loc = s.indexOf(".");
		if (loc <= 0) {
			return false;
		}
		if (s.length() == loc + 1) {
			return false;
		}

		String s1 = s.substring(loc + 1);
		// *.gezz.cn
		for (Object key : map.getTable().keySet()) {
			String d = key.toString();
			if (!d.startsWith("*.")) {
				continue;
			}
			d = d.replace("*.", "");
			if (d.equals(s1)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否Web加载
	 * 
	 * @return
	 */
	public static boolean isWebCall() {
		initPath();
		return IS_WEB_CALL;
	}

	/**
	 * 获取OpenOffice目录
	 * 
	 * @return the cVT_OPENOFFICE_HOME
	 */
	public static String getCVT_OPENOFFICE_HOME() {
		return CVT_OPENOFFICE_HOME;
	}

	/**
	 * 获取swftool目录，转换成flash用
	 * 
	 * @return the cVT_SWFTOOL_HOME
	 */
	public static String getCVT_SWFTOOL_HOME() {
		return CVT_SWFTOOL_HOME;
	}

	/**
	 * 获取 ImageMagick的可执行目录(bin)
	 * 
	 * @return
	 */
	public static String getCVT_IMAGEMAGICK_HOME() {
		initPath();
		return CVT_IMAGEMAGICK_HOME;
	}

	private static String WF_XML;
	private static String DATABASE_XML;

	private static MTableStr INIT_PARAS;

	private static long LAST_CHK = 0;
	/**
	 * 用于Cache文件的目录
	 */
	private static String PATH_CACHED = "";

	private static String CFG_CACHE_METHOD;

	public UPath() {

	}

	/**
	 * 获取EMP SCRIPT的系统配置目录
	 * 
	 * @return
	 */
	public static String getConfigPath() {
		initPath();
		return PATH_CONFIG;
	}

	/**
	 * 获取EMP SCRIPT的系统描述目录
	 * 
	 * @return
	 */
	public static String getScriptPath() {
		initPath();
		return PATH_SCRIPT;
	}

	/**
	 * 获取EMP SCRIPT的系统管理目录
	 * 
	 * @return
	 */
	public static String getManagementPath() {
		initPath();
		return PATH_MANAGMENT;
	}

	/**
	 * 获取EMP SCRIPT的系统的BIN目录
	 * 
	 * @return
	 */
	public static String getRealPath() {
		initPath();
		return PATH_REAL;
	}

	/**
	 * 获取EMP SCRIPT的系统的Database目录
	 * 
	 * @return
	 */
	public static String getSystemDbPath() {
		initPath();
		return SYSTEM_DB_PATH;
	}

	/**
	 * 获取EMP SCRIPT的系统的Database密码
	 * 
	 * @return
	 */
	public static String getSystemDbPassword() {
		initPath();
		return SYSTEM_DB_PASSWORD;
	}

	/**
	 * 项目路径
	 */
	public static String getProjectPath() {
		return getConfigPath() + "/projects/";
	}

	/**
	 * 获取组件生产和导入目录
	 * 
	 * @return
	 */
	public static String getGroupPath() {
		initPath();
		return PATH_GROUP;
	}

	/**
	 * 获取用于Cache文件的目录
	 * 
	 * @return
	 */
	public static String getCachedPath() {
		initPath();
		return PATH_CACHED;
	}

	/**
	 * 获取ewa_conf.xml的 document对象
	 * 
	 * @return
	 */
	public static Document getCfgXmlDoc() {
		initPath();
		return CFG_XML_DOC;
	}

	/**
	 * 初始化配置路径
	 */
	public static void initPath() {
		if (LAST_CHK > 0 && (System.currentTimeMillis() - LAST_CHK) < 60 * 1000) {
			// 60秒内不重新检查
			return;
		}
		LAST_CHK = System.currentTimeMillis();

		if (PATH_REAL == null || PATH_REAL.length() == 0) {
			Utils xu = new Utils();
			String path;
			if (xu.getClass().getClassLoader().getResource("/") == null) {
				// console call
				path = xu.getClass().getClassLoader().getResource(".").getPath();
				IS_WEB_CALL = false;
			} else {
				// tomcat call
				path = xu.getClass().getClassLoader().getResource("/").getPath();
				IS_WEB_CALL = true;
			}
			File f1 = new File(path);
			path = f1.getPath().replaceAll("%20", " ");
			PATH_REAL = path + "/";
		}

		File f2 = null;

		if (CONF_NAME != null) { // 用户指定文件名称
			String xmlNameDefined = PATH_REAL + CONF_NAME;
			f2 = new File(xmlNameDefined);
			if (!f2.exists()) {
				f2 = null;
			}
		}
		if (f2 == null) {
			String xmlName = PATH_REAL + "ewa_conf.xml";
			f2 = new File(xmlName);
			// 用于单独执行
			String xmlName1 = PATH_REAL + "ewa_conf_console.xml";
			if (!f2.exists()) {
				f2 = new File(xmlName1);
			}

			if (!f2.exists()) {
				System.out.println("NOT FUND " + xmlName + " OR " + xmlName1);
			}
		}
		// 先找ewa_conf.xml （新配置文件）。如果不存在，再找EwaConfig.properties
		if (f2.exists()) {
			if (PROP_TIME == f2.lastModified()) {
				// 文件未被修改
				return;
			} else {
				PROP_TIME = f2.lastModified();
				log.info("加载 " + f2.getAbsolutePath());
				try {
					initPathXml(f2.getAbsolutePath());
				} catch (Exception err) {
					log.error(err);
				}

				SqlCachedHsqldbImpl.getInstance();
			}
		}
		/*
		 * 已经作废 if (PATH_SCRIPT.length() == 0) { String propName = PATH_REAL +
		 * "EwaConfig.properties"; f2 = new File(propName); if (f2.exists()) { if
		 * (PROP_TIME == f2.lastModified()) { // 文件未被修改 return; } else { PROP_TIME =
		 * f2.lastModified(); initPath(propName); } } }
		 */
		if (PATH_SCRIPT == null || PATH_SCRIPT.length() == 0)
			PATH_SCRIPT = PATH_REAL + "Scripts/";
		if (PATH_CONFIG == null || PATH_CONFIG.length() == 0)
			PATH_CONFIG = PATH_REAL + "Config/";
		if (PATH_MANAGMENT == null || PATH_MANAGMENT.length() == 0)
			PATH_MANAGMENT = PATH_REAL + "Management/";

	}

	/**
	 * 初始化定义RequestValue的初始化类型，例如： USR_ID->int <br>
	 * &lt;requestValueType Name= "G_SUP_ID, G_ADM_ID, GRP_ID, ENQ_ID, ENQ_JNY_ID"
	 * Type="int" /&gt;
	 * 
	 * @param doc
	 */
	private static void initRequestValuesType(Document doc) {
		RV_TYPES = new HashMap<String, String>();
		NodeList nl = doc.getElementsByTagName("requestValueType");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String names = ele.getAttribute("Name");
			if (names == null || names.trim().length() == 0) {
				continue;
			}
			String paramType = ele.getAttribute("Type");
			if (paramType == null || paramType.trim().length() == 0) {
				paramType = "int";// 默认整数
			} else {
				paramType = paramType.trim().toLowerCase();
			}

			String[] names1 = names.split(",");
			for (int m = 0; m < names1.length; m++) {
				String paramName = names1[m].trim().toUpperCase();
				if (paramName.length() > 0) {
					RV_TYPES.put(paramName, paramType);
				}
			}
		}
	}

	/**
	 * 在ewa_conf.xml配置文件中获取参数
	 * 
	 * @param xmlName
	 * @throws Exception
	 */
	private synchronized static void initPathXml(String xmlName) throws Exception {
		Document doc = UXml.retDocument(xmlName);
		CFG_XML_DOC = doc;
		System.out.println("UPATH " + xmlName);
		NodeList nl = doc.getElementsByTagName("path");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String name = ele.getAttribute("Name");
			String v = ele.getAttribute("Value");
			if (!(v.endsWith("/") || v.endsWith("\\"))) {
				v += "/";
			}
			if (name.equals("config_path")) {
				PATH_CONFIG = v;
			} else if (name.equals("script_path")) {
				PATH_SCRIPT = v;
			} else if (name.equals("group_path")) {
				PATH_GROUP = v;
			} else if (name.equals("cached_path")) {
				PATH_CACHED = v;
			} else if (name.equalsIgnoreCase("cvt_office_home")) {
				UPath.CVT_OPENOFFICE_HOME = v;
			} else if (name.equalsIgnoreCase("cvt_swftool_Home")) {
				UPath.CVT_SWFTOOL_HOME = v;
			} else if (name.equalsIgnoreCase("cvt_ImageMagick_Home")) {
				UPath.CVT_IMAGEMAGICK_HOME = v;
				System.out.println(UPath.CVT_IMAGEMAGICK_HOME);
			} else if (name.equals("img_tmp_path")) { // 图片缩略图保存路径
				PATH_IMG_CACHE = v + "/" + PATH_TEMP + "/IMG/";
				if (PATH_IMG_CACHE.startsWith("@")) {
					UFile.buildPaths(PATH_IMG_CACHE.replace("@", ""));
				}
				PATH_IMG_CACHE = PATH_IMG_CACHE.replace("//", "/");
				PATH_UPLOAD = v + (v.endsWith("/") ? "" : "/");
			} else if (name.equals("img_tmp_path_url")) { // 图片缩略图URL
				PATH_IMG_CACHE_URL = v + "/" + PATH_TEMP + "/IMG/";
				// PATH_IMG_CACHE_URL = PATH_IMG_CACHE_URL.replace("//", "/");
				PATH_UPLOAD_URL = v + (v.endsWith("/") ? "" : "/");
			}

		}
		if (PATH_CACHED == null || PATH_CACHED.length() == 0) {
			PATH_CACHED = PATH_REAL + "/ewa_temp/cached/";
		}

		UPath.initSmtpParas(doc);

		DEBUG_IPS = new MTableStr();
		// 记录可以进行DEBUG的ip地址
		UPath.initDebugIps(doc);

		// Workflow
		nl = doc.getElementsByTagName("workflow");
		if (nl.getLength() > 0) {
			Element ele = (Element) nl.item(0);
			WF_XML = UXml.asXml(ele);
		}

		// 数据库连接池
		nl = doc.getElementsByTagName("databases");
		if (nl.getLength() > 0) {
			Element ele = (Element) nl.item(0);
			DATABASE_XML = UXml.asXml(ele);
		}

		// 初始化的参数 用户自定义
		INIT_PARAS = new MTableStr();
		nl = doc.getElementsByTagName("para");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String n = ele.getAttribute("Name").toUpperCase();
			String v = ele.getAttribute("Value");

			if (INIT_PARAS.containsKey(n)) {
				INIT_PARAS.removeKey(n);
			} else {
				INIT_PARAS.put(n, v);
			}

		}

		// 加载到 RequestValue的全局变量
		UPath.initRequestValueGlobal(doc);

		UPath.initValidDomains(doc);

		// 设置UDes的 key 与 iv
		// <des desKeyValue="" desIvValue="" />
		nl = doc.getElementsByTagName("des");
		if (nl.getLength() > 0) {
			Element eleDes = (Element) nl.item(0);
			for (int i = 0; i < eleDes.getAttributes().getLength(); i++) {
				Node att = eleDes.getAttributes().item(i);
				String name = att.getNodeName();
				String val = att.getNodeValue();
				if (name.equals("desKeyValue")) {
					if (val.length() >= 32) {
						UDes.DES_KEY_VALUE = val;
					}
				} else if (name.equals("desIvValue")) {
					if (val.length() >= 8) {
						UDes.DES_IV_VALUE = val;
					}
				}
			}
		}

		// <!-- 配置文件缓存方式 memory / sqlcached -->
		// <!-- memory, 使用 java 内存 (默认)-->
		// <!-- sqlcached,利用 SqlCached 配置 -->
		// <cfgCacheMethod Value="sqlcached" />

		UPath.initCfgCacheMethod(doc);

		// 初始化定义RequestValue的初始化类型，例如： USR_ID->int
		initRequestValuesType(doc);
	}

	/**
	 * 初始化 以进行DEBUG的ip地址
	 * 
	 * @param doc
	 */
	private static void initDebugIps(Document doc) {
		DEBUG_IPS = new MTableStr();
		// 记录可以进行DEBUG的ip地址
		NodeList nl = doc.getElementsByTagName("debug");
		if (nl.getLength() > 0) {
			Element ele = (Element) nl.item(0);
			String ips = ele.getAttribute("ips");
			if (ips.trim().length() > 0) {
				String[] ips1 = ips.split(",");
				for (int i = 0; i < ips1.length; i++) {
					DEBUG_IPS.add(ips1[i], ips1[i]);
				}
			}
			// 是否显示SQL执行情况 debug状态下使用
			String sqlout = ele.getAttribute("sqlout");
			if (sqlout != null && sqlout.equals("true")) {
				IS_DEBUG_SQL = true;
			} else {
				IS_DEBUG_SQL = false;
			}

		}
	}

	/**
	 * 配置文件缓存方式
	 * 
	 * @param doc
	 */
	private static void initCfgCacheMethod(Document doc) {
		// <!-- 配置文件缓存方式 memory / sqlcached -->
		// <!-- memory, 使用 java 内存 (默认)-->
		// <!-- sqlcached,利用 SqlCached 配置 -->
		// <cfgCacheMethod Value="sqlcached" />

		NodeList nl = doc.getElementsByTagName("cfgCacheMethod");
		CFG_CACHE_METHOD = "memory"; // 默认模式
		if (nl.getLength() > 0) {
			Element eleDes = (Element) nl.item(0);
			String value = eleDes.getAttribute("Value");
			if (value != null && value.trim().equalsIgnoreCase("sqlcached")) {
				CFG_CACHE_METHOD = "sqlcached";
			}
		}
	}

	private static void initValidDomains(Document doc) {
		VALID_DOMAINS = new MTableStr();
		NodeList nl = doc.getElementsByTagName("validDomains");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String v = ele.getAttribute("Value");
			String[] vs = v.split(",");
			for (int m = 0; m < vs.length; m++) {
				String v2 = vs[m].trim().toLowerCase();
				if (v2.length() == 0) {
					continue;
				}
				VALID_DOMAINS.put(v2, v2);
			}
			String host = ele.getAttribute("Host");
			if (host != null && host.trim().length() > 0) {
				VALID_DOMAINS.put("___HOST___", host);
				System.out.println("combin-host:" + VALID_DOMAINS.get("___HOST___"));
			}
		}
	}

	/**
	 * RequestValue的全局变量
	 */
	private static void initRequestValueGlobal(Document doc) {
		// 加载到 RequestValue的全局变量
		RV_GLOBALS = new HashMap<String, String>();
		NodeList nl = doc.getElementsByTagName("rv");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String n = ele.getAttribute("Name").toUpperCase();
			String v = ele.getAttribute("Value");
			if (RV_GLOBALS.containsKey(n)) {
				RV_GLOBALS.remove(n);
			} else {
				RV_GLOBALS.put(n, v);
			}

		}
	}

	/**
	 * 初始化SMTP参数
	 * 
	 * @param doc
	 */
	private static void initSmtpParas(Document doc) {
		DKIM_CFGS = new HashMap<String, DKIMCfg>();
		// smtp
		NodeList nl = doc.getElementsByTagName("smtp");
		if (nl.getLength() > 0) {
			Element eleSmtp = (Element) nl.item(0);
			for (int i = 0; i < eleSmtp.getAttributes().getLength(); i++) {
				Node att = eleSmtp.getAttributes().item(i);
				String name = att.getNodeName();
				String val = att.getNodeValue();
				if (name.equals("ip")) {
					SMTP_IP = val;
				} else if (name.equals("port")) {
					SMTP_PORT = Integer.parseInt(val);
				} else if (name.equals("user")) {
					SMTP_USER = val;
				} else if (name.equals("pwd")) {
					SMTP_PWD = val;
				}
			}

			// <dkim dkimDomain="oneworld.cc"
			// dkimKey="/var/keys/dkims/oneworld.cc.der"
			// dkimSelect="gdx" />
			NodeList nlDkims = eleSmtp.getElementsByTagName("dkim");
			for (int p = 0; p < nlDkims.getLength(); p++) {
				Element itemDkim = (Element) nlDkims.item(p);
				DKIMCfg cfg = new DKIMCfg();
				for (int i = 0; i < itemDkim.getAttributes().getLength(); i++) {
					Node att = itemDkim.getAttributes().item(i);
					String name = att.getNodeName();
					String val = att.getNodeValue();
					if (name.equals("dkimDomain")) {
						cfg.setDomain(val.toLowerCase().trim());
					} else if (name.equals("dkimKey")) {
						cfg.setPrivateKeyPath(val);
					} else if (name.equals("dkimSelect")) {
						cfg.setSelect(val);
					}
				}
				if (cfg.getDomain() != null && cfg.getPrivateKeyPath() != null) {
					String domain = cfg.getDomain();
					DKIM_CFGS.put(domain, cfg);
				}
			}

		}
		if (SMTP_IP == null || SMTP_IP.length() == 0) {
			SMTP_IP = "127.0.0.1";
		}
		if (SMTP_PORT <= 0) {
			SMTP_PORT = 25;
		}
	}

	/**
	 * 获取初始化参数
	 * 
	 * @param name
	 * @return
	 */
	public static String getInitPara(String name) {
		if (name == null) {
			return null;
		}
		String name1 = name.trim().toUpperCase();
		if (INIT_PARAS.containsKey(name1)) {
			return INIT_PARAS.get(name1);
		} else {
			return null;
		}
	}

	/*
	 * 已经作废 private synchronized static void initPath(String propName) {
	 * java.io.FileInputStream fi = null; try { fi = new
	 * java.io.FileInputStream(propName); java.util.Properties props = new
	 * java.util.Properties(); props.load(fi); if (props.getProperty("config_path")
	 * != null) { PATH_CONFIG = props.getProperty("config_path").trim() + "/"; } if
	 * (props.getProperty("script_path") != null) { PATH_SCRIPT =
	 * props.getProperty("script_path").trim() + "/"; } if
	 * (props.getProperty("management_path") != null) { PATH_MANAGMENT =
	 * props.getProperty("management_path").trim() + "/"; } if
	 * (props.getProperty("system_db_path") != null) { SYSTEM_DB_PATH =
	 * props.getProperty("system_db_path").trim(); } if
	 * (props.getProperty("system_db_password") != null) { SYSTEM_DB_PASSWORD =
	 * props.getProperty("system_db_password").trim(); } if
	 * (props.getProperty("group_path") != null) { PATH_GROUP =
	 * props.getProperty("group_path").trim(); } if
	 * (props.getProperty("cached_path") != null) { PATH_CACHED =
	 * props.getProperty("cached_path").trim(); } else { PATH_CACHED = PATH_REAL +
	 * "/ewa_temp/cached/"; } if (props.getProperty("smtp_ip") != null) { SMTP_IP =
	 * props.getProperty("smtp_ip").trim(); } if (props.getProperty("smtp_port") !=
	 * null) { SMTP_PORT = Integer.parseInt(props.getProperty("smtp_port").trim());
	 * } if (props.getProperty("smtp_user") != null) { SMTP_USER =
	 * props.getProperty("smtp_user").trim(); } if (props.getProperty("smtp_pwd") !=
	 * null) { SMTP_PWD = props.getProperty("smtp_pwd").trim(); }
	 * 
	 * } catch (Exception e) { } finally { if (fi != null) { try { fi.close(); }
	 * catch (Exception e) {
	 * 
	 * } } } }
	 */
	/**
	 * 获取项目WebRoot所在的物理目录
	 * 
	 * @return
	 */
	public static String getRealContextPath() {
		String s1 = getRealPath();
		return s1.split("WEB-INF")[0];
	}

	/**
	 * 邮件发送地址
	 * 
	 * @return the sMTP_IP
	 */
	public static String getSmtpIp() {
		initPath();
		return SMTP_IP;
	}

	/**
	 * 端口
	 * 
	 * @return the sMTP_PORT
	 */
	public static int getSmtpPort() {
		initPath();
		return SMTP_PORT;
	}

	/**
	 * 用户
	 * 
	 * @return the sMTP_USER
	 */
	public static String getSmtpUser() {
		initPath();
		return SMTP_USER;
	}

	/**
	 * 密码
	 * 
	 * @return the sMTP_PWD
	 */
	public static String getSmtpPwd() {
		initPath();
		return SMTP_PWD;
	}

	/**
	 * 流程部门列表
	 * 
	 * @return the wF_DEPT
	 */
	public static String getWFXML() {
		initPath();
		return WF_XML;
	}

	/**
	 * 获取数据库连接池配置
	 * 
	 * @return the dATABASE_XML
	 */
	public static String getDATABASEXML() {
		initPath();
		return DATABASE_XML;
	}

	/**
	 * 获取可用进行DEBUG的IP地址
	 * 
	 * @return
	 */
	public static MTableStr getDebugIps() {
		initPath();
		return DEBUG_IPS;
	}

	public static boolean isDebugSql() {
		initPath();
		return IS_DEBUG_SQL;
	}

	/**
	 * 获取上传文件物理路径<br>
	 * Name="img_tmp_path"，如果ewa_conf中没有配置的话，则为当前WEB所住目录
	 * 
	 * @return
	 */
	public static String getPATH_UPLOAD() {
		initPath();
		if (PATH_UPLOAD.startsWith("@")) {
			return PATH_UPLOAD.replace("@", "");
		} else {
			return UPath.getRealContextPath() + "/" + PATH_UPLOAD;
		}
	}

	/**
	 * des="图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。"
	 * Name="img_tmp_path_url"，如果ewa_conf中没有配置的话，则为null
	 * 
	 * @return
	 */
	public static String getPATH_UPLOAD_URL() {
		initPath();
		if (PATH_UPLOAD_URL == null) {
			return null;
		} else {
			return UPath.PATH_UPLOAD_URL;
		}
	}

	/**
	 * 获取图片临时文件路径
	 * 
	 * @return
	 */
	public static String getPATH_IMG_CACHE() {
		initPath();
		if (PATH_IMG_CACHE.startsWith("@")) {
			return PATH_IMG_CACHE.replace("@", "");
		} else {
			return UPath.getRealContextPath() + UPath.PATH_IMG_CACHE;
		}
	}

	/**
	 * des="图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。"
	 * Name="img_tmp_path_url"，如果ewa_conf中没有配置的话，则取当前contextpath
	 * 
	 * @return
	 */
	public static String getPATH_IMG_CACHE_URL() {
		initPath();
		if (PATH_IMG_CACHE_URL == null) {
			return PATH_IMG_CACHE;
		}
		return UPath.PATH_IMG_CACHE_URL;
	}

	/**
	 * 获取 配置文件缓存的模式，内存或sqlcached
	 * 
	 * @return
	 */
	public static String getCfgCacheMethod() {
		initPath();

		return UPath.CFG_CACHE_METHOD;
	}
}
