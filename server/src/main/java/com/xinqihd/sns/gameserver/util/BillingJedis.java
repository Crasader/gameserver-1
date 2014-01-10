package com.xinqihd.sns.gameserver.util;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPoolConfig;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisAdapter;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;

/**
 * The billing jedis will stores a list of Jedis instance 
 * which may points to more than one jedis server for 
 * checking user online status.
 * 
 * @author wangqi
 *
 */
public class BillingJedis {

	private static final Logger logger = LoggerFactory.getLogger(BillingJedis.class);
	
	private ArrayList<Jedis> jedises = new ArrayList<Jedis>();
	
	private static final String prefix = "billing.redis.";
	
	private static BillingJedis instance = new BillingJedis();
	
	private BillingJedis() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxActive(5);
		poolConfig.setMinIdle(5);
		
		int i=1;
		while ( true ) {
			String key = StringUtil.concat(prefix, i++);
			String redisId = GlobalConfig.getInstance().getStringProperty(key);
			if ( redisId == null ) {
				break;
			} else {
				String[] redisHostPort = StringUtil.splitMachineId(redisId);
				String jedisHost = redisHostPort[0];
				int jedisPort = StringUtil.toInt(redisHostPort[1], 6379);
				Jedis jedis = new JedisAdapter(jedisHost, jedisPort, poolConfig);
				logger.info("Connect to {} {}", new Object[]{key, redisId});
				jedises.add(jedis);
			}
		}
	}
	
	/**
	 * Get the singleton instance.
	 * @return
	 */
	public static BillingJedis getInstance() {
		return instance;
	}
	
	/**
	 * Find the user's sessionKey by his/her userid
	 * @param userId
	 * @return
	 */
	public SessionKey findSessionKeyByUserId(UserId userId) {
		if ( jedises.size() == 0 ) {
			return null;
		}
		if ( userId == null ) return null;
		for ( Jedis jedis : jedises ) {
			String bytes = jedis.hget(userId.toString(), SessionManager.H_SESSION_KEY);
			if ( bytes != null ) {
				return SessionKey.createSessionKeyFromHexString(bytes);
			}
		}
		return null;
	}
	
	/**
	 * Get the given values
	 * @param key
	 * @return
	 */
	public String getValue(String key) {
		if ( jedises.size() == 0 ) {
			return null;
		}
		if ( key == null ) return null;
		for ( Jedis jedis : jedises ) {
			String values = jedis.get(key);
			if ( values != null ) {
				return values;
			}
		}
		return null;
	}
	
	/**
	 * Find the user's machine id by his/her userId
	 * @param userId
	 * @return
	 */
	public String findUserGameServerId(SessionKey userSessionKey) {
		if ( userSessionKey == null ) return null;
		for ( Jedis jedis : jedises ) {
			String rpcServerBytes = jedis.hget(userSessionKey.toString(), SessionManager.H_GAMESERVER_KEY);
			if ( rpcServerBytes != null ) {
				return rpcServerBytes;	
			}
		}
		return null;
	}
}
