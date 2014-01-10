package com.xinqihd.sns.gameserver.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPoolConfig;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;

/**
 * Create a Jedis interface.
 * 
 * @author wangqi
 *
 */
public class JedisFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(JedisFactory.class);
	
	private static final JedisPoolConfig poolConfig = new JedisPoolConfig();
	
	//This jedis instance is for transient data that will not be stored on disk.
	private static Jedis jedis = null;
	
	//This jedis instance is for persistent data that will be stored on disk.
	private static Jedis jedisDB = null;
	/**
	 * Initialize the JedisPoolConfig
	 */
	public static void initJedis() {
		if ( jedis == null ) {
			String jedisHost = GlobalConfig.getInstance().getStringProperty("jedis.master.host");
			int    jedisPort = GlobalConfig.getInstance().getIntProperty("jedis.master.port");
			
			String jedisDBHost = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.jedis_db_host);
			int    jedisDBPort = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.jedis_db_port);
			
			int maxActive = GlobalConfig.getInstance().getIntProperty("jedis.master.pool.maxActive");
			int minIdle = GlobalConfig.getInstance().getIntProperty("jedis.master.pool.minIdle");
			int numTestsPerEvictionRun = GlobalConfig.getInstance().getIntProperty("jedis.master.pool.numTestsPerEvictionRun");
			int timeBetweenEvictionRunsMillis = GlobalConfig.getInstance().getIntProperty("jedis.master.pool.timeBetweenEvictionRunsMillis");
			int minEvictableIdleTimeMillis = GlobalConfig.getInstance().getIntProperty("jedis.master.pool.minEvictableIdleTimeMillis");
			
			logger.info("JedisPool settings: jedisHost:" +jedisHost +", jedisPort:"+ jedisPort +
					", maxActive:"+maxActive+", minIdle"+minIdle+", numTestsPerEvictionRun:"+numTestsPerEvictionRun +
					", timeBetweenEvictionRunsMillis: " + timeBetweenEvictionRunsMillis + ", minEvictableIdleTimeMillis:"+minEvictableIdleTimeMillis);

			poolConfig.setMaxActive(maxActive);
			poolConfig.setMinIdle(minIdle);
			poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
			poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
			poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
			poolConfig.setTestOnBorrow(true);
			
			jedis = new JedisAdapter(jedisHost, jedisPort, poolConfig);
			jedisDB = new JedisAdapter(jedisDBHost, jedisDBPort, poolConfig);
			
			if ( logger.isInfoEnabled() ) {
				logger.info("Successfully connect to jedis database");
			}

		}
	}
	
	/**
	 * Initialize the JedisPoolConfig
	 */
	public static Jedis createJedis(String host, int port) {
		int maxActive = 1;
		int minIdle = 1;
		
		poolConfig.setMaxActive(maxActive);
		poolConfig.setMinIdle(minIdle);
		
		JedisAdapter jedis = new JedisAdapter(host, port, poolConfig);
		
		if ( logger.isInfoEnabled() ) {
			logger.info("Successfully connect to jedis database");
		}
		
		return jedis;
	}
	
	/**
	 * Disable the Jedis utility
	 */
	public static void disableJedis() {
		jedis = new JedisDummyAdapter();
		jedisDB = new JedisDummyAdapter();
	}
	
	/**
	 * Get a Jedis for processing the transient data, that is 
	 * data not stored in hard disk.
	 * 
	 * @return
	 */
	public static final Jedis getJedis() {
		if ( jedis == null ) {
			initJedis();
		}
		return jedis;
	}
	
	/**
	 * Get a Jedis for processing the persistent data, that is 
	 * data will be stored in hard disk.
	 * 
	 * @return
	 */
	public static final Jedis getJedisDB() {
		if ( jedisDB == null ) {
			initJedis();
		}
		return jedisDB;
	}
}
