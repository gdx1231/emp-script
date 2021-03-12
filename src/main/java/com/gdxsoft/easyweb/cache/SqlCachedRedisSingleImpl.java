package com.gdxsoft.easyweb.cache;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 单点redis的操作
 * 
 * @author admin
 *
 */
public class SqlCachedRedisSingleImpl extends SqlCachedRedisBase implements ISqlCached {
	public static Boolean DEBUG = true;

	/**
	 * 创建 单点redis的操作 对象
	 * 
	 * @param ip       IP地址
	 * @param port     端口
	 * @param password 密码，不为空时设置
	 * @param config   配置文件
	 * @return
	 */
	public static SqlCachedRedisSingleImpl getInstance(String ip, int port, String password, JedisPoolConfig config) {
		if (POOL == null) {
			initJedis(ip, port, config);
		}
		SqlCachedRedisSingleImpl o1 = new SqlCachedRedisSingleImpl();
		o1.setRedisPassword(password);
		return o1;
	}

	private synchronized static void initJedis(String ip, int port, JedisPoolConfig config) {
		POOL = new JedisPool(config, ip, port);
	}

}
