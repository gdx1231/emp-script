package com.gdxsoft.easyweb.cache;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * 单点redis的操作
 * 
 * @author admin
 *
 */
public abstract class SqlCachedRedisBase implements ISqlCached {
	private static Logger LOGGER = LoggerFactory.getLogger(SqlCachedRedisBase.class);

	static JedisCluster CLUSTER;
	static JedisPool POOL;

	/**
	 * 获取jedis连接
	 * 
	 * @return
	 */
	private Jedis getSingleConnection() {
		Jedis conn = POOL.getResource();
		// 设置密码
		if (StringUtils.isNotBlank(this.getRedisPassword())) {
			conn.auth(this.getRedisPassword());
		}

		return conn;
	}

	private void jdedisSet(String jk, String cachedValue, String type, long cahcedTime) {
		if (CLUSTER != null) {
			CLUSTER.lpush(jk, cachedValue, type, cahcedTime + "");
		} else {
			Jedis conn = getSingleConnection();
			conn.lpush(jk, cachedValue, type, cahcedTime + "");
			conn.close();
		}
	}

	private void jdedisDel(String[] ks) {
		if (CLUSTER != null) {
			CLUSTER.del(ks);
		} else {
			Jedis conn = getSingleConnection();
			conn.del(ks);
			conn.close();
		}
	}

	private SqlCachedValue jdedisGet(String jk) {
		SqlCachedValue sv = new SqlCachedValue();
		String cachedValue;
		String cachedType;
		String cahcedTime;
		if (CLUSTER != null) {
			cachedValue = CLUSTER.lindex(jk, 2);
			cachedType = CLUSTER.lindex(jk, 1);
			cahcedTime = CLUSTER.lindex(jk, 0);
		} else {
			Jedis conn = getSingleConnection();
			cachedValue = conn.lindex(jk, 2);
			cachedType = conn.lindex(jk, 1);
			cahcedTime = conn.lindex(jk, 0);
			conn.close();
		}

		if (cachedValue == null || cahcedTime == null) {
			return null;
		}

		sv.setLastTime(Long.parseLong(cahcedTime));
		sv.setType(cachedType);

		if ("BIN".equals(cachedType)) { // binary
			byte[] buf = cachedValue.getBytes(StandardCharsets.ISO_8859_1);
			sv.setValue(buf);
		} else { // text
			sv.setValue(cachedValue);
		}
		return sv;
	}

	public boolean add(String key, String value) {
		long t0 = System.currentTimeMillis();

		jdedisSet(key, value, "TEXT", t0);

		long t1 = System.currentTimeMillis();
		LOGGER.debug("SQLCACHED-PUT(text):" + key + ":" + (t1 - t0) + "ms");

		return true;
	}

	public boolean add(String key, byte[] value) {
		long t0 = System.currentTimeMillis();
		jdedisSet(key, new String(value, StandardCharsets.ISO_8859_1), "BIN", t0);
		long t1 = System.currentTimeMillis();

		LOGGER.debug("SQLCACHED-PUT(bin):" + key + ":" + (t1 - t0) + "ms");

		return true;
	}

	/**
	 * 删除单个缓存
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public boolean remove(String key, String type) {
		String[] keys = new String[1];
		keys[0] = key;
		return this.removes(keys, type);
	}

	/**
	 * 删除多个缓存
	 * 
	 * @param keys
	 * @param type
	 * @return
	 */
	public boolean removes(String[] keys, String type) {

		jdedisDel(keys);
		return true;
	}

	public SqlCachedValue getBinary(String key) {
		long t0 = System.currentTimeMillis();
		SqlCachedValue sv = this.jdedisGet(key);

		long t1 = System.currentTimeMillis();
		LOGGER.debug("SQLCACHED-GET(bin):" + key + ":" + (t1 - t0) + "ms");

		return sv;
	}

	public SqlCachedValue getText(String key) {
		long t0 = System.currentTimeMillis();

		SqlCachedValue sv = this.jdedisGet(key);

		long t1 = System.currentTimeMillis();
		LOGGER.debug("SQLCACHED-GET(text):" + key + ":" + (t1 - t0) + "ms");
		return sv;
	}

	public SqlCachedValue get(String key, String type) {
		if (type.equals("BIN")) {
			return this.getBinary(key);
		} else {
			return this.getText(key);
		}
	}

	/**
	 * redis 密码
	 */
	private String _RedisPassword;

	/**
	 * redis 密码
	 * 
	 * @return the _RedisPassword
	 */
	public String getRedisPassword() {
		return _RedisPassword;
	}

	/**
	 * redis 密码
	 * 
	 * @param _RedisPassword the _RedisPassword to set
	 */
	public void setRedisPassword(String _RedisPassword) {
		this._RedisPassword = _RedisPassword;
	}

	public boolean add(String key, String value, String memo) {
		return this.add(key, value);
	}

	public boolean add(String key, byte[] value, String memo) {
		return this.add(key, value);
	}
}
