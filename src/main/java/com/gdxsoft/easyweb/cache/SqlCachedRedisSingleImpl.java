package com.gdxsoft.easyweb.cache;

import java.io.IOException;

import org.json.JSONObject;

import com.gdxsoft.easyweb.utils.UConvert;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import org.apache.log4j.Logger;

/**
 * 单点redis的操作
 * 
 * @author admin
 *
 */
public class SqlCachedRedisSingleImpl implements ISqlCached {
	private static JedisPool POOL;
	private static Logger LOOGER = Logger.getLogger(SqlCachedRedisSingleImpl.class);
	public static Boolean DEBUG = true;

	/**
	 * 创建 单点redis的操作 对象
	 * 
	 * @param ip
	 *            IP地址
	 * @param port
	 *            端口
	 * @param password
	 *            密码，不为空时设置
	 * @param config
	 *            配置文件
	 * @return
	 */
	public static SqlCachedRedisSingleImpl getInstance(String ip, int port, String password, JedisPoolConfig config) {
		SqlCachedRedisSingleImpl o1 = new SqlCachedRedisSingleImpl();

		o1.init(ip, port, password, config);
		return o1;
	}

	private void init(String ip, int port, String password, JedisPoolConfig config) {
		if (POOL == null) {
			POOL = new JedisPool(config, ip, port);
			this.setRedisPassword(password);
		}
	}

	/**
	 * 获取jedis连接
	 * 
	 * @return
	 */
	private Jedis getConnection() {
		Jedis conn = POOL.getResource();
		// 设置密码
		if (this._RedisPassword != null && !this._RedisPassword.isEmpty()) {
			conn.auth(_RedisPassword);
		}

		return conn;
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

		Jedis cnn = this.getConnection();
		cnn.set(key, obj.toString());

		cnn.close();

		long t1 = System.currentTimeMillis();
		LOOGER.debug("SQLCACHED-PUT(text):" + key + ":" + (t1 - t0) + "ms");

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

		Jedis cnn = this.getConnection();
		cnn.set(key, obj.toString());

		cnn.close();

		long t1 = System.currentTimeMillis();
		LOOGER.debug("SQLCACHED-PUT(bin):" + key + ":" + (t1 - t0) + "ms");

		return true;
	}
	/**
	 * 删除单个缓存
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
		Jedis cnn = this.getConnection();
		for (int i = 0; i < keys.length; i++) {
			String key = "SQLCACHED_" + keys[i].hashCode();
			cnn.del(key);
		}
		cnn.close();
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

		Jedis cnn = this.getConnection();
		String value = cnn.get(key);
		cnn.close();

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
				LOOGER.error(value, e);
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

		Jedis cnn = this.getConnection();
		String value = cnn.get(key);
		cnn.close();

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
	 * @param _RedisPassword
	 *            the _RedisPassword to set
	 */
	public void setRedisPassword(String _RedisPassword) {
		this._RedisPassword = _RedisPassword;
	}
}
