package com.gdxsoft.easyweb.cache;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.script.userConfig.UserConfig;

/**
 *
 */
public class ConfigCacheWidthSqlCached {
	private static Logger LOOGER = LoggerFactory.getLogger(ConfigCacheWidthSqlCached.class);

	/**
	 * 获取配置文件的缓存key
	 * 
	 * @param xmlFileName
	 * @return
	 */
	public static String getConfigFileKey(String xmlFileName) {
		String xmlName = UserConfig.filterXmlName(xmlFileName);
		StringBuilder sbkey = new StringBuilder();
		sbkey.append("ConfigCached:XMLNAME=");
		sbkey.append(xmlName);
		String file_key = sbkey.toString();

		return file_key;
	}

	/**
	 * 获取配置项key
	 * 
	 * @param itemName
	 * @return
	 */
	public static String getConfigItemKey(String itemName) {
		StringBuilder itemkey = new StringBuilder();
		itemkey.append(",GdX,ITEMNAME=");
		// 小写配置项名称
		itemkey.append(itemName.toLowerCase());
		String key = itemkey.toString();

		return key;
	}

	/**
	 * 获取UserConfig
	 * 
	 * @param xmlFileName
	 * @param itemName
	 * @return
	 */
	public static UserConfig getUserConfig(String xmlFileName, String itemName) {
		// 配置文件信息
		String file_key = getConfigFileKey(xmlFileName);
		String item_key = getConfigItemKey(itemName);

		StringBuilder itemkey = new StringBuilder(file_key);
		itemkey.append(item_key);
		String key = itemkey.toString();

		SqlCachedValue userConfigSerialized = SqlCached.getInstance().getBinary(key);

		if (userConfigSerialized == null) {
			return null;
		}
		UserConfig uc = getUserConfigFromCache(userConfigSerialized, xmlFileName);
		if (uc == null) {
			return uc;
		}
		// not overtime
		if (!userConfigSerialized.checkOvertime(UserConfig.CHECK_CHANG_SPAN_SECONDS)) {
			return uc;
		}

		// Get cached XML meta from hsqldb
		SqlCachedValue cachedXmlMeta = SqlCached.getInstance().getText(file_key);
		if (cachedXmlMeta == null) { // 没有cached
			removeUserConfig(xmlFileName);
			return null;
		}

		// Get total XML meta from database
		ConfigStatus currentXmlMeta = uc.getConfigStatus();

		// long fileLastModify = fileStatus.lastModified();
		// long len = fileStatus.length();
		// String code = fileLastModify + "," + len + "," + fileStatus.getMd5();
		// 从数据库返回的整个 XML 文件的 code
		String code = currentXmlMeta.getStatusCode();

		JSONObject json = new JSONObject(cachedXmlMeta.toString());
		// 缓存中 整个 XML 文件的 code
		String old_code = json.optString("code");

		// 检查文件是否被修改
		if (old_code.equals(code)) {
			return uc;
		} else {
			// 文件变化了，清除本文件下的所有缓存
			removeUserConfig(xmlFileName);
			return null;
		}

	}

	private static UserConfig getUserConfigFromCache(SqlCachedValue userConfigSerialized, String xmlFileName) {

		if (userConfigSerialized == null) {
			return null;
		}
		UserConfig uc = null;
		try {
			uc = UserConfig.fromSerialize(userConfigSerialized.getBinary());
			return uc;
		} catch (ClassNotFoundException e) {
			LOOGER.error(e.getMessage());
			removeUserConfig(xmlFileName);
			return null;
		} catch (IOException e) {
			LOOGER.error(e.getMessage());
			removeUserConfig(xmlFileName);
			return null;
		}
	}

	/**
	 * 删除配置文件
	 * 
	 * @param xmlFileName
	 * @param itemName
	 */
	public static void removeUserConfig(String xmlFileName) {
		String file_key = getConfigFileKey(xmlFileName);

		// 配置文件信息
		SqlCached cached = SqlCached.getInstance();
		SqlCachedValue cvFile = cached.getText(file_key);

		if (cvFile == null) {
			return;
		}

		JSONObject json = new JSONObject(cvFile.toString());
		JSONArray arr = json.optJSONArray("itemnames");
		String[] keys = new String[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			String item_key = getConfigItemKey(arr.optString(i));
			StringBuilder itemkey = new StringBuilder(file_key);
			itemkey.append(item_key);
			keys[i] = itemkey.toString();

			LOOGER.info("CLEAR CAHCE: " + keys[i]);
		}
		// 删除和配置文件相关的缓存文件
		cached.removes(keys, "BIN");
		cached.remove(file_key, "TEXT");
		LOOGER.info("CLEAR CAHCES: " + xmlFileName);

	}

	/**
	 * 删除配置项目
	 * 
	 * @param xmlFileName
	 * @param itemName
	 */
	public static void removeUserConfig(String xmlFileName, String itemName) {

		String file_key = getConfigFileKey(xmlFileName);
		String item_key = getConfigItemKey(itemName);
		// 配置文件信息
		SqlCached cached = SqlCached.getInstance();
		SqlCachedValue cvFile = cached.getText(file_key);

		if (cvFile == null) {
			return;
		}

		// 删除 itemName cache
		StringBuilder itemkey = new StringBuilder(file_key);
		itemkey.append(item_key);
		String key = itemkey.toString();
		cached.remove(key, "BIN");

		JSONObject json = new JSONObject(cvFile.toString());
		JSONArray arr = json.optJSONArray("itemnames");
		for (int i = 0; i < arr.length(); i++) {
			String name = arr.optString(i);
			if (name.equals(itemName.toLowerCase())) {
				arr.remove(i);
				break;
			}
		}
		// 修改配置文件的缓存文件
		cached.add(file_key, json.toString());
	}

	/**
	 * 设置UserConfig
	 * 
	 * @param xmlFileName
	 * @param itemName
	 * @param userConfig
	 */
	public synchronized static void setUserConfig(String xmlFileName, String itemName, UserConfig userConfig) {
		// 获取文件的状态
		ConfigStatus fileStatus = userConfig.getConfigStatus();
		// long fileLastModify = fileStatus.lastModified();
		// long len = fileStatus.length();
		// fileLastModify + "," + len + "," + fileStatus.getMd5();
		String code = fileStatus.getStatusCode();

		// 配置文件信息
		SqlCached cached = SqlCached.getInstance();
		String file_key = getConfigFileKey(xmlFileName);
		SqlCachedValue cvFile = cached.getText(file_key);
		JSONObject configJson;
		if (cvFile == null) {
			configJson = new JSONObject();
			configJson.put("code", code);
			configJson.put("file", fileStatus.getFixedXmlName());
			configJson.put("itemnames", new JSONArray());
			configJson.put("key", file_key);
		} else {
			configJson = new JSONObject(cvFile.toString());
			// 如果文件不一致，创建新的数据
			if (!code.equals(configJson.optString("code"))) {
				// 清除根此文件和所有相关配置
				removeUserConfig(xmlFileName, itemName);

				configJson = new JSONObject();
				configJson.put("code", code);
				configJson.put("file", fileStatus.getFixedXmlName());
				configJson.put("itemnames", new JSONArray());
				configJson.put("key", file_key);
			}
		}
		// 文件配置项的信息
		JSONArray arr = configJson.optJSONArray("itemnames");
		boolean is_exists = false;
		for (int i = 0; i < arr.length(); i++) {
			if (arr.getString(i).equals(itemName.toLowerCase())) {
				is_exists = true;
				break;
			}
		}
		if (!is_exists) {
			arr.put(itemName.toLowerCase());
		}
		// 记录文件信息到缓存
		cached.add(file_key, configJson.toString(), xmlFileName);

		String item_key = getConfigItemKey(itemName);
		StringBuilder itemkey = new StringBuilder(file_key);
		itemkey.append(item_key);
		String key = itemkey.toString();
		try {
			// 记录配置项信息到缓存
			cached.add(key, userConfig.toSerialize(), xmlFileName + "::" + itemName);
		} catch (IOException e) {
			LOOGER.error("", e);
		}

	}

}
