package com.gdxsoft.easyweb.cache;

import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis集群
 * 
 * @author admin
 *
 */
public class SqlCachedRedisImpl extends SqlCachedRedisBase implements ISqlCached {

	public static SqlCachedRedisImpl getInstance(Set<HostAndPort> clusterNodes, JedisPoolConfig config) {
		SqlCachedRedisImpl o1 = new SqlCachedRedisImpl();

		if (CLUSTER == null) {
			initCluster(clusterNodes, config);
		}

		return o1;
	}

	private synchronized static void initCluster(Set<HostAndPort> clusterNodes, JedisPoolConfig config) {
		CLUSTER = new JedisCluster(clusterNodes, config);
	}
}
