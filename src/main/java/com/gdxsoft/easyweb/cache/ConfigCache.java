package com.gdxsoft.easyweb.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.global.EwaGlobals;
import com.gdxsoft.easyweb.script.project.Project;
import com.gdxsoft.easyweb.script.userConfig.JdbcConfig;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFileCheck;
import com.gdxsoft.easyweb.utils.UPath;

public class ConfigCache {
	private static  Logger LOGGER = LoggerFactory.getLogger(ConfigCache.class);
	private static ConcurrentHashMap<String, Object> _Objects = new ConcurrentHashMap<String, Object>();

	/**
	 * 获取项目
	 * 
	 * @param xmlProjectName 项目文件名称
	 * @return
	 */
	public static Project getProject(String xmlProjectName) {
		String path = UPath.getProjectPath() + xmlProjectName;
		String code = createUserConfigCode(path, "$$EWA_PROJECT$$");
		Object o = getObject(code, path);
		if (o == null) {
			return null;
		} else {
			return (Project) o;
		}
	}

	/**
	 * 获取UserConfig
	 * 
	 * @param xmlFileName
	 * @param itemName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static UserConfig getUserConfig(String xmlFileName, String itemName) {

		boolean isJdbcCall = JdbcConfig.isJdbcResources();
		if (isJdbcCall) {
			int id = xmlFileName.hashCode();
			// 是否存在
			boolean isHave = UFileCheck.isHave(id);
			// 是否超时
			boolean isOverTime = UFileCheck.isOverTime(id, UserConfig.CHECK_CHANG_SPAN_SECONDS);
			if (isHave && !isOverTime) {
				// 5秒内不重新扫描数据库数据
				CacheGroup<UserConfig> cg = (CacheGroup<UserConfig>) _Objects.get(xmlFileName);
				UserConfig uc = cg.getItem(itemName);
				LOGGER.debug("NOT OVERTIME: " + xmlFileName + "-" + itemName);
				return uc;
			}
		}

		ConfigStatus configStatus = UserConfig.getXmlConfigPath(xmlFileName, itemName);
		// 没有找到配置
		if (configStatus == null) {
			return null;
		}
		String path = configStatus.getAbsolutePath();
		if (_Objects.containsKey(path)) {
			if (configStatus.isChanged()) {// 检查文件或数据库的hash是否被修改
				removeObject(path);
				// 删除和配置文件相关的缓存文件
				CacheEwaScript ces = new CacheEwaScript(xmlFileName, itemName);
				ces.removeCached();
				LOGGER.info("CHANGED: " + xmlFileName + "-" + itemName);
				return null;
			} else {
				CacheGroup<UserConfig> cg = (CacheGroup<UserConfig>) _Objects.get(path);
				UserConfig uc = cg.getItem(itemName);
				LOGGER.debug("NOT CHANGED: " + xmlFileName + "-" + itemName);
				return uc;
			}
		} else {
			return null;
		}
	}

	public static void removeUserConfig(String xmlFileName, String itemName) {
		ConfigStatus configStatus = UserConfig.getXmlConfigPath(xmlFileName, itemName);
		//
		// String path = UPath.getScriptPath() + xmlFileName.replace("|", "/");
		// File f = new File(path);
		// path = f.getAbsolutePath();

		String path = configStatus.getAbsolutePath();
		if (_Objects.containsKey(path)) {
			removeObject(path);
		}
		CacheEwaScript ces = new CacheEwaScript(xmlFileName, itemName);
		ces.removeCached();
		// 2017-02-28 多站点情况下会已经死锁 guolei
		// Runtime.getRuntime().gc();
	}

	/**
	 * 设置UserConfig
	 * 
	 * @param xmlFileName
	 * @param itemName
	 * @param userConfig
	 */
	@SuppressWarnings("unchecked")
	public synchronized static void setUserConfig(String xmlFileName, String itemName, UserConfig userConfig) {
		boolean isJdbcCall = JdbcConfig.isJdbcResources();
		String path;
		if (isJdbcCall) {
			path = xmlFileName;
		} else {
			path = UPath.getScriptPath() + xmlFileName.replace("|", "/");
			java.io.File f = new java.io.File(path);
			path = f.getAbsolutePath();
		}
		CacheGroup<UserConfig> cg = null;
		if (_Objects.containsKey(path)) {
			cg = (CacheGroup<UserConfig>) _Objects.get(path);
		} else {
			cg = new CacheGroup<UserConfig>();
			_Objects.put(path, cg);
		}
		cg.addItem(itemName, userConfig);
	}

	private static Object getObject(String code, String path) {
		if (_Objects.containsKey(code)) {
			Object o = _Objects.get(code);
			if (!UFileCheck.fileChanged(path)) {
				// 文件没有变化
				return o;
			} else {
				// 移除老的对象
				removeObject(code);
				return null;
			}
		} else {
			return null;
		}
	}

	public static EwaGlobals getGlobals() {
		String code = createUserConfigCode(EwaGlobals.FILE_NAME, "");
		String path = UPath.getConfigPath() + EwaGlobals.FILE_NAME;
		Object o = getObject(code, path);
		if (o == null) {
			return null;
		} else {// 文件没有变化
			return (EwaGlobals) o;
		}
	}

	private synchronized static void removeObject(String code) {
		@SuppressWarnings("unused")
		Object o = _Objects.remove(code);
		o = null;
	}

	/**
	 * 放置Project缓存
	 * 
	 * @param xmlProjectName 项目文件名
	 * @param project        项目
	 */
	public synchronized static void setProject(String xmlProjectName, Project project) {
		String path = UPath.getProjectPath() + xmlProjectName;
		String code = createUserConfigCode(path, "$$EWA_PROJECT$$");
		_Objects.remove(code);
		_Objects.put(code, project);
	}

	public synchronized static void setGlobals(EwaGlobals globals) {
		String code = createUserConfigCode(EwaGlobals.FILE_NAME, "");
		_Objects.remove(code);
		_Objects.put(code, globals);
	}

	private static String createUserConfigCode(String xmlFileName, String itemName) {
		String code = "USERCONFIG#" + xmlFileName + "*" + itemName;
		return code;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<UserConfig> getUserConfigs() {
		Iterator<String> it = _Objects.keySet().iterator();
		ArrayList<UserConfig> al = new ArrayList<UserConfig>();
		while (it.hasNext()) {
			String key = it.next();
			Object o = _Objects.get(key);
			if (!o.getClass().equals(CacheGroup.class)) {
				continue;
			}
			CacheGroup<UserConfig> cg = (CacheGroup<UserConfig>) o;
			HashMap<String, UserConfig> map = cg.getItems();
			Iterator<String> it1 = map.keySet().iterator();
			while (it1.hasNext()) {
				UserConfig o2 = map.get(it1.next());
				al.add(o2);
			}
		}
		return al;
	}

	public static String list() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> a = _Objects.keySet().iterator();
		int m = 0;
		while (a.hasNext()) {
			String key = a.next();
			sb.append(m + ": <a href='lookcache.jsp?key=" + key + "'>" + key + "</a>," + _Objects.get(key).toString()
					+ "<br>\r\n");
			m++;
		}
		return sb.toString();
	}

	public static void clearCache() {
		String[] aa = getArrayKey().split(",");
		clearCache(aa);
	}

	public static void removeCache(String key) {
		removeObject(key);
	}

	public static String getArrayKey() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> a = _Objects.keySet().iterator();
		while (a.hasNext()) {
			String key = a.next();
			if (key.indexOf("#") < 0) {
				if (sb.length() > 0) {
					sb.append("," + key);
				} else {
					sb.append(key);
				}
			}
		}
		return sb.toString();
	}

	public static int getCount() {
		Iterator<String> a = _Objects.keySet().iterator();
		int i = 0;
		while (a.hasNext()) {
			a.next();
			i++;
		}
		return i;
	}

	public static void clearCache(String[] keys) {
		for (int i = 0; i < keys.length; i++) {
			removeObject(keys[i]);
		}
	}
}
