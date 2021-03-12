package com.gdxsoft.easyweb.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.debug.DebugFrames;
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

	public static UserConfig getUserConfig(String xmlFileName, String itemName) {
		return getUserConfig(xmlFileName, itemName, null);
	}

	/**
	 * 获取UserConfig
	 * 
	 * @param xmlFileName
	 * @param itemName
	 * @return
	 */
	public static UserConfig getUserConfig(String xmlFileName, String itemName, DebugFrames debugFrames) {
		ConfigCacheWidthSqlCached o = new ConfigCacheWidthSqlCached();
		// 配置文件信息
		String file_key = getConfigFileKey(xmlFileName);
		String item_key = getConfigItemKey(itemName);

		StringBuilder itemkey = new StringBuilder(file_key);
		itemkey.append(item_key);
		String key = itemkey.toString();

		if (debugFrames != null) {
			debugFrames.addDebug(o, "getUserConfig", "Start get binary from cache (" + key + ")");
		}
		SqlCachedValue userConfigSerialized = SqlCached.getInstance().getBinary(key);
		if (debugFrames != null) {
			debugFrames.addDebug(o, "getUserConfig",
					"End get cache " + (userConfigSerialized == null ? "null" : "not null"));
		}
		if (userConfigSerialized == null) {
			return null;
		}

		UserConfig uc = getUserConfigFromCache(userConfigSerialized, xmlFileName);
		if (debugFrames != null) {
			debugFrames.addDebug(o, "getUserConfig", "Serialized the UserConfig " + (uc == null ? "null" : "not null"));
		}
		if (uc == null) {
			return uc;
		}
		// not overtime
		if (!userConfigSerialized.checkOvertime(UserConfig.CHECK_CHANG_SPAN_SECONDS)) {
			if (debugFrames != null) {
				debugFrames.addDebug(o, "getUserConfig", "Not overtime, return the UserConfig ");
			}
			return uc;
		}
		// Get cached XML meta
		CachedXmlFileMeta meta = ConfigCacheWidthSqlCached.getCachedXmlFileMetaFromCached(xmlFileName);
		if (debugFrames != null) {
			debugFrames.addDebug(o, "getUserConfig", "Get the cached XML meta (" + file_key + ")");
		}
		if (meta == null) {
			removeUserConfig(xmlFileName);
			if (debugFrames != null) {
				debugFrames.addDebug(o, "getUserConfig", "No meta data, remove the UserConfig ");
			}
			return null;
		}

		// Get total XML meta from database
		ConfigStatus currentXmlMeta = uc.getConfigStatus();
		if (debugFrames != null) {
			debugFrames.addDebug(o, "getUserConfig", "Get the current Meta from source (file/db)");
		}
		// long fileLastModify = fileStatus.lastModified();
		// long len = fileStatus.length();
		// String code = fileLastModify + "," + len + "," + fileStatus.getMd5();
		// 从数据库返回的整个 XML 文件的 code
		String code = currentXmlMeta.getStatusCode();
		// 缓存中 整个 XML 文件的 code
		String old_code = meta.getCode();

		// 检查文件是否被修改
		if (old_code.equals(code)) {
			if (debugFrames != null) {
				debugFrames.addDebug(o, "getUserConfig", "No changed, return the UserConfig ");
			}
			return uc;
		} else {
			// 文件变化了，清除本文件下的所有缓存
			if (debugFrames != null) {
				debugFrames.addDebug(o, "getUserConfig", "The meta data changed, remove the UserConfig ");
			}
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
	 * 删除配置项目
	 * 
	 * @param xmlFileName
	 * @param itemName
	 */
	public static void removeUserConfig(String xmlFileName, String itemName) {
		CachedXmlFileMeta meta = getCachedXmlFileMetaFromCached(xmlFileName);
		removeUserConfig(meta);
	}

	/**
	 * 删除配置文件
	 * 
	 * @param xmlFileName
	 * @param itemName
	 */
	public static void removeUserConfig(String xmlFileName) {
		CachedXmlFileMeta meta = getCachedXmlFileMetaFromCached(xmlFileName);
		removeUserConfig(meta);
	}

	public static void removeUserConfig(CachedXmlFileMeta meta) {
		if (meta == null) {
			return;
		}
		SqlCached cached = SqlCached.getInstance();

		String file_key = meta.getKey();
		List<String> keys = new ArrayList<String>();
		meta.getItemNames().forEach(itemName -> {
			String item_key = getConfigItemKey(itemName);
			// 删除 itemName cache
			StringBuilder itemkey = new StringBuilder(file_key);
			itemkey.append(item_key);
			String key = itemkey.toString();
			keys.add(key);
		});
		keys.add(file_key);
		String[] removeKeys = new String[keys.size()];
		removeKeys = keys.toArray(removeKeys);
		for (int i = 0; i < removeKeys.length; i++) {
			LOOGER.info("Del " + removeKeys[i]);
		}
		cached.removes(removeKeys, "BIN");
	}

	private static CachedXmlFileMeta getCachedXmlFileMetaFromCached(String xmlFileName) {
		String file_key = getConfigFileKey(xmlFileName);
		// 配置文件信息
		SqlCached cached = SqlCached.getInstance();

		SqlCachedValue cvFile = cached.getBinary(file_key);

		CachedXmlFileMeta meta = null;
		if (cvFile == null) {
			return null;
		}
		try {
			meta = CachedXmlFileMeta.fromSerialize(cvFile.getBinary());
			return meta;
		} catch (Exception err) {
			LOOGER.warn("Unabled from serialize", err.getMessage());
			return null;
		}

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

		SqlCachedValue cvFile = cached.getBinary(file_key);

		CachedXmlFileMeta meta = null;
		if (cvFile != null) {
			try {
				meta = CachedXmlFileMeta.fromSerialize(cvFile.getBinary());
				if (!code.equals(meta.getCode())) {
					// Clear all items
					removeUserConfig(meta);
					meta = null;
				}
			} catch (Exception err) {
				LOOGER.warn("Unabled from serialize", err.getMessage());
			}
		}
		if (meta == null) {
			meta = createCachedXmlFileMeta(file_key, fileStatus);
		}

		if (!meta.getItemNames().contains(itemName.toLowerCase())) {
			meta.addItemName(itemName.toLowerCase());
		}

		// 记录文件信息到缓存
		try {
			cached.add(file_key, meta.toSerialize(), xmlFileName);
			LOOGER.info("Add " + file_key);
		} catch (IOException e1) {
			LOOGER.error(e1.getLocalizedMessage());
			return;
		}

		String item_key = getConfigItemKey(itemName);
		StringBuilder itemkey = new StringBuilder(file_key);
		itemkey.append(item_key);
		String key = itemkey.toString();
		try {
			// 记录配置项信息到缓存
			cached.add(key, userConfig.toSerialize(), xmlFileName + "::" + itemName);
			LOOGER.info("Add " + key);
		} catch (IOException e) {
			LOOGER.error("", e);
		}
	}

	private static CachedXmlFileMeta createCachedXmlFileMeta(String fileKey, ConfigStatus fileStatus) {
		CachedXmlFileMeta meta = new CachedXmlFileMeta();

		meta = new CachedXmlFileMeta();
		meta.setCode(fileStatus.getStatusCode());
		meta.setFile(fileStatus.getFixedXmlName());
		meta.setKey(fileKey);

		return meta;
	}

}
