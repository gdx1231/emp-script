package com.gdxsoft.easyweb.cache;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gdxsoft.easyweb.script.userConfig.JdbcConfig;
import com.gdxsoft.easyweb.script.userConfig.UserConfig;
import com.gdxsoft.easyweb.utils.UFileCheck;

/**
 * 有问题，没改完
 * 
 * @author admin
 *
 */
public class ConfigCacheWidthSqlCached {
	private static Logger LOOGER = Logger.getLogger(ConfigCacheWidthSqlCached.class);

	/**
	 * 获取配置文件的缓存key
	 * 
	 * @param xmlFileName
	 * @return
	 */
	public static String getConfigFileKey(String xmlFileName) {
		StringBuilder sbkey = new StringBuilder();
		sbkey.append("ConfigCached:XMLNAME=");
		if (JdbcConfig.isJdbcResources()) {
			sbkey.append(JdbcConfig.getJdbcConfigName());
			String xmlName = UserConfig.filterXmlNameByJdbc(xmlFileName);
			sbkey.append(xmlName);
		} else {
			String xmlName = UserConfig.filterXmlName(xmlFileName);
			sbkey.append(xmlName);
		}
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

		SqlCached cached = SqlCached.getInstance();

		int id = key.hashCode();
		// 是否存在
		boolean isHave = UFileCheck.isHave(id);
		// 是否超时
		boolean isOverTime = UFileCheck.isOverTime(id, UserConfig.CHECK_CHANG_SPAN_SECONDS);
		if (isHave && !isOverTime) {
			// 5秒内不重新扫描数据库数据
			SqlCachedValue cv = cached.getBinary(key);
			if (cv == null) {
				return null;
			}
			try {
				UserConfig uc = UserConfig.fromSerialize(cv.getBinary());
				return uc;

			} catch (ClassNotFoundException e) {
				LOOGER.error(e);
				return null;
			} catch (IOException e) {
				LOOGER.error(e);
				return null;
			}
		}

		int fileId = file_key.hashCode();
		boolean is_file_not_changed = true;
		if (UFileCheck.isHave(fileId) && !UFileCheck.isOverTime(fileId, UserConfig.CHECK_CHANG_SPAN_SECONDS)) {
			// 指定的时间内不重复检查
		} else {
			// 对于文件保存获取文件的状态
			ConfigStatus fileStatus = UserConfig.getXmlConfigPath(xmlFileName, "");
			SqlCachedValue cvFile = cached.getText(file_key);
			if (cvFile == null) { // 没有cached
				return null;
			}

			// long fileLastModify = fileStatus.lastModified();
			// long len = fileStatus.length();
			// String code = fileLastModify + "," + len + "," + fileStatus.getMd5();

			// 从数据库返回的整个 XML 文件的 code
			String code = fileStatus.getStatusCode();

			JSONObject json = new JSONObject(cvFile.toString());

			// 缓存中 整个 XML 文件的 code
			String old_code = json.optString("code");

			// 检查文件是否被修改
			is_file_not_changed = old_code.equals(code);
		}
		if (is_file_not_changed) {
			SqlCachedValue cv = cached.getBinary(key);
			if (cv == null) {
				return null;
			}
			try {
				UserConfig uc = UserConfig.fromSerialize(cv.getBinary());
				// 记录配置项的时间
				UFileCheck.putTime(id, System.currentTimeMillis());
				// 记录配置文件的时间
				UFileCheck.putTime(fileId, System.currentTimeMillis());
				return uc;

			} catch (ClassNotFoundException e) {
				UFileCheck.remove(id);
				UFileCheck.remove(fileId);
				LOOGER.error(e);
				return null;
			} catch (IOException e) {
				UFileCheck.remove(id);
				UFileCheck.remove(fileId);
				LOOGER.error(e);
				return null;
			}
		} else {
			// 文件变化了，清除本文件下的所有缓存
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

			UFileCheck.remove(keys[i].hashCode());
		}
		// 删除和配置文件相关的缓存文件
		cached.removes(keys, "BIN");
		cached.remove(file_key, "TEXT");
		LOOGER.info("CLEAR CAHCES: " + keys);
		LOOGER.info("CLEAR CAHCES: " + xmlFileName);

		UFileCheck.remove(file_key.hashCode());
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
		UFileCheck.remove(key.hashCode());

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
		ConfigStatus fileStatus = UserConfig.getXmlConfigPath(xmlFileName, "");
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
			configJson.put("file", fileStatus.getAbsolutePath());
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
				configJson.put("file", fileStatus.getAbsolutePath());
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
		cached.add(file_key, configJson.toString());

		String item_key = getConfigItemKey(itemName);
		StringBuilder itemkey = new StringBuilder(file_key);
		itemkey.append(item_key);
		String key = itemkey.toString();
		try {
			// 记录配置项信息到缓存
			cached.add(key, userConfig.toSerialize());
			UFileCheck.putTimeAndFileCode(key.hashCode(), System.currentTimeMillis(), 0);
		} catch (IOException e) {
			LOOGER.error(e);
		}

	}

}
