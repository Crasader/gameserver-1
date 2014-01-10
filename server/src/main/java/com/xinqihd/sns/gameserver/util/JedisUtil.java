package com.xinqihd.sns.gameserver.util;

import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RedisRoomManager;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;

public class JedisUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(JedisUtil.class);

	/**
	 * Pop a ready room from "room_ready_set_<N>".
	 */
	/*
	public static final Tuple popKeyFromZset(String zsetName) {
		Jedis jedis = JedisFactory.getJedis();
		Pipeline pipeline = jedis.pipelined();
		
		while ( true ) {
			try {
				pipeline.watch(zsetName);
				Response<Set<Tuple>> results = pipeline.zrangeWithScores(zsetName, 0, 0);
				pipeline.multi();
				pipeline.zremrangeByRank(zsetName, 0, 0);
				pipeline.exec();
				pipeline.sync();
				jedis.unwatch();
				
				Set<Tuple> values = results.get();
				if (values != null && values.size() > 0) {
					return values.iterator().next();
				} else {
					Long size = jedis.zcard(zsetName);
					if ( size != null && size == 0 ) {
//						logger.debug("No object in zset: size:{}", size);
						return null;
					} else {
//						logger.debug("Watch exception. Retry");
					}
				}
			} catch (Exception e) {
				try {
					pipeline.discard();
				} catch (Exception e1) {
				}
				e.printStackTrace();
			}
		}
//		return null;
	}
	*/
	
	/**
	 * Pop a ready room from "room_ready_set_<N>".
	 */
	public static final Tuple popKeyFromZset(String zsetName) {
		Pipeline pipeline = JedisFactory.getJedis().pipelined();
		
		try {
			pipeline.watch(zsetName);
			Response<Set<Tuple>> results = pipeline.zrangeWithScores(zsetName, 0, 0);
			pipeline.multi();
			pipeline.zremrangeByRank(zsetName, 0, 0);
			pipeline.exec();
			pipeline.sync();
			
			Set<Tuple> values = results.get();
			if (values.size() > 0) {
				return values.iterator().next();
			}
		} catch (Exception e) {
			try {
				pipeline.discard();
			} catch (Exception e1) {
			}
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Clean all the data in Redis that are related to this server.
	 * It should be called when the server starts up, because maybe 
	 * an abnormal shutdown leave dirty data in Redis
	 * 
	 * User:{
	 *  key: SESS_*
	 *  hfield: machineid (message_server_id)
	 *  hfield: userid 
	 * }
	 * 
	 * UserId:{
	 *  key: USERID
	 * }
	 * 
	 * Room:{
	 *  key: ROOM_*
	 *  hfield: machineid (rpcserverid)
	 *  hfield: current_set_name
	 * }
	 * 
	 * Room_Local: {
	 *  key: room_<rpcserverid>
	 * }
	 * 
	 * <current_set_name>:{
	 * }
	 * 
	 * Battle:{
	 * 	key: BATTLE_*
	 * }
	 * 
	 * Chat: {
	 * }
	 * 
	 * client_timeout_map:<client_ip> {
	 * }
	 * 
	 */
	public static final void cleanRedis() {
		String rpcServerId = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_RPC_SERVERID);
		if ( rpcServerId == null ) {
			rpcServerId = Constant.EMPTY;
		}
		
		Jedis jedis = JedisFactory.getJedis();
		//Clean local users.
		String localSessionPrefix = GameContext.getInstance().getSessionManager().getLocalSessionPrefix();
		
		logger.info("Clean the localSessionPrefix: {}", localSessionPrefix);
		Set<String> sessionSet = jedis.keys(localSessionPrefix.concat("*"));
		int queryPrefixLength = localSessionPrefix.length();
		Pipeline pipeline = jedis.pipelined();
		for ( String key : sessionSet ) {
			String userIdStr = jedis.get(key);
			UserId userId = UserId.fromString(userIdStr);
			if ( userId != null ) {
				logger.info("Clean userid: " + userIdStr);
				pipeline.del(userId.getInternal());
				String sessionKeyStr = key.substring(queryPrefixLength);
				SessionKey sessionKey = SessionKey.createSessionKeyFromHexString(sessionKeyStr);
				pipeline.del(key);
				logger.info("Clean local user session: " + key);
				pipeline.del(sessionKey.toString());
				logger.info("Clean user session: " + sessionKey.toString());
			}
		}
		//Clean rooms
		RedisRoomManager redisManager = (RedisRoomManager)(RoomManager.getInstance());
		String localRoomSetName = redisManager.getLocalRoomSetName();
		Set<String> roomSet = jedis.smembers(localRoomSetName);
		for ( String roomKey : roomSet ) {
			logger.info("Clean roomSessionKey: {}", roomKey);
			pipeline.del(roomKey);
			logger.info("Clean {} from {}", roomKey, localRoomSetName);
			pipeline.srem(localRoomSetName, roomKey);
			for ( String zsetName : RedisRoomManager.ZSET_UNFULL_NAME ) {
				pipeline.zrem(zsetName, roomKey);
			}
			for ( String zsetName : RedisRoomManager.ZSET_READY_NAME ) {
				pipeline.zrem(zsetName, roomKey);					
			}
			pipeline.zrem(RedisRoomManager.ZSET_FULL_NAME, roomKey);
		}
		pipeline.del(localRoomSetName);
		logger.info("Clean local room set {}", localRoomSetName);
		//Clean battles
		
		//BATTLE_ = 424154544C455F
		Set<String> battleSet = jedis.keys("424154544C455F*");
		for ( String battleKey : battleSet ) {
			String serverId = jedis.hget(battleKey, Constant.RPC_SERVER_KEY);
			if ( rpcServerId.equals(serverId) ) {
				pipeline.del(battleKey);
				logger.info("Clean battle {}", battleKey);
				logger.info("Clean check battle key {} battleKey and serverId {}", 
						battleKey, serverId);
			}
		}
		//Clean client_timeout_map
		Set<String> clientMap = jedis.keys(SimpleClient.TIME_OUT_MAP_KEY.concat(rpcServerId));
		for ( String clientKey : clientMap ) {
			pipeline.del(clientKey);
			logger.info("Clean client_timeout_map(rpc) {}", clientKey);
		}
		String aiServerId = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_AI_SERVERID);
		if ( aiServerId != null ) {
			clientMap = jedis.keys(SimpleClient.TIME_OUT_MAP_KEY.concat(aiServerId));
			for ( String clientKey : clientMap ) {
				pipeline.del(clientKey);
				logger.info("Clean client_timeout_map(ai) {}", clientKey);
			}
		}
		//Clean guild
		Set<String> guildMap = jedis.keys(GuildManager.REDIS_GUILDMEMBER.concat("*"));
		for ( String clientKey : guildMap ) {
			pipeline.del(clientKey);
			logger.info("Clean guild member {}", clientKey);
		}
		//Clean guild bag
		pipeline.del(GuildManager.REDIS_GUILDBAG_LOCK);
		pipeline.sync();
	}

	/**
	 * Use it with caution. 
	 * It delete all the keys from Redis
	 */
	public static final void deleteAllKeys() {
		com.xinqihd.sns.gameserver.jedis.Jedis jedis = JedisFactory.getJedis();
		deleteAllKeys(jedis);
	}
	
	/**
	 * Use it with caution. 
	 * It delete all the keys from Redis
	 */
	public static final void deleteAllKeys(Jedis jedis) {
		Pipeline pipeline = jedis.pipelined();
		Set<byte[]> bytes = jedis.keys("*".getBytes());
		for ( byte[] key : bytes ) {
			pipeline.del(key);
		}
		pipeline.sync();
	}
		
	public static void main(String ... args) {
//		String host = "babywar.xinqihd.com";
//		String host = "localhost";
//		JedisFactory.initJedis();
//		JedisUtil.deleteAllKeys();
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		Random r = new Random();
		int step = 20;
		int count = 100000;
		String zset = "zset";
		for ( int i=0; i<step; i++ ) {
			long start = System.currentTimeMillis();
			for ( int j=0; j<count; j++ ) {
				int score = r.nextInt();
				jedisDB.zadd(zset, 10, "test-"+score);
			}
			long end = System.currentTimeMillis();
			System.out.println("Step: " + i +"["+(i*count)+"-"+(i+1)*count+"): " + 
					(end-start)+" millis.");
		}
	}
}
