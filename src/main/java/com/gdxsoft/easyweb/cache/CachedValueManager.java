package com.gdxsoft.easyweb.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 值缓存管理
 * 
 * @author Administrator
 * 
 */
public class CachedValueManager {

	private static Map<Integer, CachedValue> _MAP = new ConcurrentHashMap<Integer, CachedValue>();

	/**
	 * 获取缓存，如果存在并在指定的存活时间内，则返回 <br>
	 * 如果死亡，则清除
	 * 
	 * @param id
	 * @return
	 */
	public static CachedValue getValue(int id) {
		if (_MAP.containsKey(id)) {
			CachedValue v = _MAP.get(id);
			if (v.isValid()) {
				return v;
			} else {
				removeCache(id);
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 获取缓存，如果存在并在指定的存活时间内，则返回<br>
	 * 如果死亡，则清除
	 * 
	 * @param idStr
	 *            字符串，会获取hashCode作为Cahce的Id
	 * @return
	 */
	public static CachedValue getValue(String idStr) {
		int id = idStr.hashCode();
		return getValue(id);
	}

	private synchronized static void removeCache(int id) {
		_MAP.remove(id);
	}

	public synchronized static void addValue(CachedValue v) {
		int id = v.getId();
		if (_MAP.containsKey(id)) {
			removeCache(id);
		}
		_MAP.put(id, v);
	}

	/**
	 * 添加Cache
	 * 
	 * @param id
	 * @param value
	 * @param lifeSeconds
	 *            存活时间（秒）
	 */
	public synchronized static void addValue(int id, Object value,
			int lifeSeconds) {
		if (_MAP.containsKey(id)) {
			removeCache(id);
		}
		CachedValue v = new CachedValue(id, value);
		if (lifeSeconds > 0) {
			v.setLifeSeconds(lifeSeconds);
		}
		_MAP.put(id, v);
	}

	/**
	 * 添加Cache
	 * 
	 * @param idStr
	 *            字符串，会获取hashCode作为Cahce的Id
	 * @param value
	 * @param lifeSeconds
	 *            存活时间（秒）
	 */
	public synchronized static void addValue(String idStr, Object value,
			int lifeSeconds) {
		int id = idStr.hashCode();
		addValue(id, value, lifeSeconds);
	}

	/**
	 * 添加Cache 存活10分钟
	 * 
	 * @param id
	 * @param value
	 */
	public synchronized static void addValue(int id, Object value) {
		addValue(id, value, 0);
	}

	/**
	 * 添加Cache 存活10分钟
	 * 
	 * @param idStr
	 *            字符串，会获取hashCode作为Cahce的Id
	 * @param value
	 */
	public synchronized static void addValue(String idStr, Object value) {
		addValue(idStr, value, 0);
	}
}
