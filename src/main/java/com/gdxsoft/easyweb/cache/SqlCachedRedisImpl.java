package com.gdxsoft.easyweb.cache;

import java.io.IOException;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UConvert;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis集群
 * 
 * @author admin
 *
 */
public class SqlCachedRedisImpl implements ISqlCached {

	public static Boolean DEBUG = true;
	private static Logger LOOGER = LoggerFactory.getLogger(SqlCachedRedisImpl.class);

	public static SqlCachedRedisImpl getInstance(Set<HostAndPort> clusterNodes, JedisPoolConfig config) {
		SqlCachedRedisImpl o1 = new SqlCachedRedisImpl();

		o1.init(clusterNodes, config);
		return o1;
	}

	private JedisCluster _jedisCluster;

	private void init(Set<HostAndPort> clusterNodes, JedisPoolConfig config) {
		_jedisCluster = new JedisCluster(clusterNodes, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.cache.ISqlCached#add(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean add(String key, String value) {
		key = "SQLCACHED_" + key.hashCode();

		long t0 = System.currentTimeMillis();
		JSONObject obj = new JSONObject();
		obj.put("TEXT", value);
		obj.put("T", System.currentTimeMillis());
		this._jedisCluster.set(key, obj.toString());

		long t1 = System.currentTimeMillis();
		LOOGER.debug("SQLCACHED-PUT(text):" + key + ":" + (t1 - t0) + "ms");
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
		for (int i = 0; i < keys.length; i++) {
			String key = "SQLCACHED_" + keys[i].hashCode();
			this._jedisCluster.del(key);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.cache.ISqlCached#add(java.lang.String, byte[])
	 */
	@Override
	public boolean add(String key, byte[] value) {
		long t0 = System.currentTimeMillis();

		key = "SQLCACHED_" + key.hashCode();
		JSONObject obj = new JSONObject();
		if (value != null) {
			obj.put("BIN", UConvert.ToBase64String(value));
		}
		obj.put("T", System.currentTimeMillis());

		this._jedisCluster.set(key, obj.toString());

		long t1 = System.currentTimeMillis();
		LOOGER.debug("SQLCACHED-PUT(bin):" + key + ":" + (t1 - t0) + "ms");

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.cache.ISqlCached#getBinary(java.lang.String)
	 */
	@Override
	public SqlCachedValue getBinary(String key) {
		long t0 = System.currentTimeMillis();
		key = "SQLCACHED_" + key.hashCode();

		String value = this._jedisCluster.get(key);
		if (value == null) {
			return null;
		}
		SqlCachedValue v = new SqlCachedValue();
		v.setType("BIN");

		JSONObject obj = new JSONObject(value);
		v.setLastTime(obj.optLong("T"));
		String base64 = obj.optString("BIN");
		if (base64 != null && !base64.isEmpty()) {
			byte[] buf;
			try {
				buf = UConvert.FromBase64String(base64);
				v.setValue(buf);
			} catch (IOException e) {
				LOOGER.error(base64, e);
			}

		}
		long t1 = System.currentTimeMillis();
		LOOGER.debug("SQLCACHED-GET(bin):" + key + ":" + (t1 - t0) + "ms");

		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.cache.ISqlCached#getText(java.lang.String)
	 */
	@Override
	public SqlCachedValue getText(String key) {
		long t0 = System.currentTimeMillis();

		key = "SQLCACHED_" + key.hashCode();

		String value = this._jedisCluster.get(key);

		if (value == null) {
			return null;
		}
		SqlCachedValue v = new SqlCachedValue();
		v.setType("TEXT");

		JSONObject obj = new JSONObject(value);
		v.setValue(obj.optString("TEXT"));
		v.setLastTime(obj.optLong("T"));

		long t1 = System.currentTimeMillis();
		LOOGER.debug("SQLCACHED-GET(text):" + key + ":" + (t1 - t0) + "ms");

		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gdxsoft.easyweb.cache.ISqlCached#get(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public SqlCachedValue get(String key, String type) {
		if (type.equals("BIN")) {
			return this.getBinary(key);
		} else {
			return this.getText(key);
		}
	}

	@Override
	public boolean add(String key, String value, String memo) {
		return this.add(key, value);
	}

	@Override
	public boolean add(String key, byte[] value, String memo) {
		return this.add(key, value);
	}

}
