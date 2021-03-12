package com.gdxsoft.easyweb.cache;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfSqlCached;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

public class SqlCached {

	private static SqlCached INSTANCE;
	public static Boolean DEBUG = false;
	private static Logger LOGGER = LoggerFactory.getLogger(SqlCached.class);

	/**
	 * 获取 SqlCached的实例
	 * 
	 * @return
	 */
	public static SqlCached getInstance() {
		if (INSTANCE == null) {
			// SqlCached.DEBUG = true;
			INSTANCE = newSqlCached();
		}
		return INSTANCE;
	}

	private synchronized static SqlCached newSqlCached() {

		long t0 = System.currentTimeMillis();

		SqlCached o = new SqlCached();
		o.init();

		long t1 = System.currentTimeMillis();
		LOGGER.info("SQLCACHED:init" + ":" + (t1 - t0) + "ms");

		INSTANCE = o;
		return o;
	}

	private ISqlCached _cachedImpl;

	private void initHsqldb() {
		LOGGER.info("SQLCACHED init: HSQLDB");
		SqlCachedHsqldbImpl.DEBUG = DEBUG;
		_cachedImpl = SqlCachedHsqldbImpl.getInstance();
	}

	private void initRedis(ConfSqlCached conf) {
		// 池基本配置
		JedisPoolConfig config = new JedisPoolConfig();
		// 最大连接个数
		config.setMaxTotal(50);
		// 最大空闲连接数
		config.setMaxIdle(5);

		// 获取连接时的最大等待毫秒数
		config.setMaxWaitMillis(10001);
		config.setMaxIdle(10000);
		// 在空闲时检查有效性,默认false

		config.setTestOnBorrow(false);
		String[] hosts1 = conf.getConfRedis().getHosts().split(",");
		Set<HostAndPort> clusterNodes = new HashSet<HostAndPort>();
		HostAndPort first = null;
		for (int i = 0; i < hosts1.length; i++) {
			String[] ipAndPort = hosts1[i].split("\\:");
			String ip = ipAndPort[0].trim();
			int port = ipAndPort.length == 1 ? 6379 : Integer.parseInt(ipAndPort[1]);
			if (i == 0) {
				first = new HostAndPort(ip, port);
			}
			clusterNodes.add(new HostAndPort(ip, port));

		}
		String method = conf.getConfRedis().getMethod();
		String auth = conf.getConfRedis().getAuth();
		if (method == null || method.isEmpty() || method.equalsIgnoreCase("single")) {
			LOGGER.info("SQLCACHED init: REDIS SINGLE");
			SqlCachedRedisSingleImpl.DEBUG = DEBUG;
			_cachedImpl = SqlCachedRedisSingleImpl.getInstance(first.getHost(), first.getPort(), auth, config);
		} else {
			LOGGER.info("SQLCACHED init: REDIS CLUSTER");
			_cachedImpl = SqlCachedRedisImpl.getInstance(clusterNodes, config);
		}
	}

	private void init() {
		ConfSqlCached conf = ConfSqlCached.getInstance();
		SqlCached.DEBUG = conf.isDebug();
		if ("redis".equalsIgnoreCase(conf.getCachedMethod())) {
			this.initRedis(conf);
		} else {
			this.initHsqldb();
		}
	}

	/**
	 * 删除多个缓存
	 * 
	 * @param keys
	 * @param type
	 * @return
	 */
	public boolean removes(String[] keys, String type) {
		return _cachedImpl.removes(keys, type);
	}

	/**
	 * 删除一个缓存
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public boolean remove(String key, String type) {
		return _cachedImpl.remove(key, type);
	}

	/**
	 * 添加 文字
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, String value) {
		return _cachedImpl.add(key, value);
	}

	public boolean add(String key, String value, String memo) {
		return _cachedImpl.add(key, value, memo);
	}

	/**
	 * 添加 二进制
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, byte[] value) {
		return _cachedImpl.add(key, value);
	}

	/**
	 * 添加 二进制
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, byte[] value, String memo) {
		return _cachedImpl.add(key, value, memo);
	}

	/**
	 * 获取二进制
	 * 
	 * @param key
	 * @return
	 */
	public SqlCachedValue getBinary(String key) {
		return _cachedImpl.getBinary(key);
	}

	/**
	 * 获取文本
	 * 
	 * @param key
	 * @return
	 */
	public SqlCachedValue getText(String key) {
		return _cachedImpl.getText(key);
	}

	/**
	 * 获取对象
	 * 
	 * @param key
	 * @param type 类型，bin, text ...
	 * @return
	 */
	public SqlCachedValue get(String key, String type) {
		return _cachedImpl.get(key, type);
	}

}
