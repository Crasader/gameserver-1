package com.xinqihd.sns.gameserver.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.entity.user.UserStatus;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.util.BillingJedis;
import com.xinqihd.sns.gameserver.util.OtherUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Manage the user's session. Maybe query or store from/to Redis.
 * 
 * @author wangqi
 *
 */
public class SessionManager {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
	
	private static final String SESSION_KEY_PREFIX = "SESS_";
	
	public static final String PROXY_SESSION_KEYS = "PROXY";
	
	public static final String LOCALE_SESSION_KEY = "LOCALE";
	
	//The machine id is the MessageServer's socket address: localhost:3444
	private static String machineId = null;
	private static String rpcServerId = null;
	private static String gameServerId = null;
	private static String aiServerId = null;
	
	//The key to store machineid in Redis hash data type
	public static final String H_MACHINE_KEY = "mach";
  //The key to store machineid in Redis hash data type
	public static final String H_RPCSERVER_KEY = "rpc";
  //The key to store machineid in Redis hash data type
	public static final String H_GAMESERVER_KEY = "game";
	//The key to store userid in Redis hash data type
	public static final String H_USERID_KEY = "user";
	//The key to store session key in Redis hash data type
	public static final String H_SESSION_KEY = "sess";
  //The key to store locale (zh_CN) in Redis hash data type
	public static final String H_LOCALE_KEY = "locale";
	
	public static final String H_ISAI = "isai";
	public static final String H_ISPROXY = "isproxy";
	public static final String V_TRUE = "1";
	public static final String V_FALSE = "0";
	
	public static final String Q_SESSION_PATTERN = "534553535F*";
	
	public static final String L_SESSION_PATTERN = "LSESS_*";

	private String localSessionPrefix = null;
	
	public SessionManager() {
		JedisFactory.initJedis();
		
		String messageServerIdStr = GlobalConfig.getInstance().getStringProperty(
				GlobalConfig.RUNTIME_MESSAGE_SERVER_ID);
		if ( messageServerIdStr == null ) {
			messageServerIdStr = OtherUtil.getHostName()+":3444";
		}
		String aiServerIdStr = GlobalConfig.getInstance().getStringProperty(
				GlobalConfig.RUNTIME_AI_SERVERID);
		if ( aiServerIdStr == null ) {
			aiServerIdStr = OtherUtil.getHostName()+":3446";
		}
		String rpcServerStr = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_RPC_SERVERID);
		if ( rpcServerStr == null ) {
			rpcServerStr = OtherUtil.getHostName()+":3445";
		}
		String gameServerStr = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_GAME_SERVERID);
		if ( gameServerStr == null ) {
			gameServerStr = OtherUtil.getHostName()+":3443";
		}
		
		localSessionPrefix = StringUtil.concat("LSESS_", messageServerIdStr, "_");
		
		machineId = messageServerIdStr;
		aiServerId = aiServerIdStr;
		rpcServerId = rpcServerStr;
		gameServerId = gameServerStr;
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("messageServerId : {}", messageServerIdStr);
			logger.debug("aiServerId : {}", aiServerIdStr);
			logger.debug("rpcServerId : {}", rpcServerStr);
			logger.debug("gameServerId : {}", gameServerStr);
		}
		
	}
	
	/**
	 * Get the local session prefix.
	 * @return
	 */
	public String getLocalSessionPrefix() {
		return localSessionPrefix;
	}

	/**
	 * Register an login user with a new session key.
	 * @param session
	 * @param user
	 * @return
	 */
	public SessionKey registerSession(IoSession session, User user) {
		return registerSession(session, user, null);
	}
	
	/**
	 * Register an login user with a new session key. If the given sessionHexString is not null,
	 * create a session from it.
	 * 
	 * Redis:
	 * <sessionKey>: hashtable
	 *    <machine_key>: <user's machine id>
	 *    <user_id>    : <user's id>
	 *    
	 * <user_id>: hashtable
	 *    <machine_key>: <user's machine id>
	 *    <sessionKey> : <user's session key>
	 * 
	 * LSESS_<machineid>_sessionKey: <user_id>
	 * 
	 * @param session
	 * @param user
	 * @return
	 */
	public SessionKey registerSession(IoSession session, User user, SessionKey existSessionKey) {
		SessionKey sessionKey = null;
		if ( existSessionKey != null ) {
			sessionKey = existSessionKey;
			logger.debug("Create session from existing session hex string {}", sessionKey);
		} else {
			sessionKey = SessionKey.createSessionKeyFromRandomString(SESSION_KEY_PREFIX);
		}
		
		//Try to clean existing session
		if ( !user.isProxy() && user.get_id() != null ) {
			SessionKey oldSessionKey = findSessionKeyByUserId(user.get_id());
			if ( oldSessionKey != null ) {
				Jedis jedis = JedisFactory.getJedis();
				jedis.del(oldSessionKey.toString());
				logger.debug("Clean old sessionKey {} for user {}", oldSessionKey, user.getRoleName());
			}
		}
		
		user.setSessionKey(sessionKey);
		user.setSession(session);
		user.setStatus(UserStatus.NORMAL);
		
		//Try to get user's locale
		Locale userLocale = user.getUserLocale();
		if ( Constant.I18N_ENABLE ) {
			ThreadLocal<Locale> localeLocal = new ThreadLocal<Locale>();
			localeLocal.set(userLocale);
		}
		
		logger.debug("Register user '{}' with sessionKey: {}", 
				user.getRoleName(), sessionKey.toString());
		
		//1. Store it with machineid to redis
		String userMachineId = this.machineId;
		if ( user.isAI() ) {
			userMachineId = this.aiServerId;
		} else {
			user.setGameserverId(gameServerId);
			if ( session != null ) {
				if ( user.isProxy() ) {
					ArrayList<SessionKey> proxySessionKeys = (ArrayList<SessionKey>)session.getAttribute(PROXY_SESSION_KEYS);
					if ( proxySessionKeys == null ) {
						proxySessionKeys = new ArrayList<SessionKey>();
						session.setAttribute(PROXY_SESSION_KEYS, proxySessionKeys);
					}
					proxySessionKeys.add(user.getSessionKey());
				} else {
					session.setAttribute(Constant.SESSION_KEY, sessionKey);
					session.setAttribute(LOCALE_SESSION_KEY, userLocale);
				}
			}
		}
		Jedis jedis = JedisFactory.getJedis();
		Pipeline pipeline = jedis.pipelined();
		//Disable the timeout if exist
		pipeline.hset(sessionKey.toString(), H_MACHINE_KEY, userMachineId);
		pipeline.hset(sessionKey.toString(), H_RPCSERVER_KEY, rpcServerId);
		pipeline.hset(sessionKey.toString(), H_GAMESERVER_KEY, gameServerId);
		pipeline.hset(sessionKey.toString(), H_LOCALE_KEY, userLocale.toString());
		if ( user.get_id() != null ) {
			pipeline.hset(sessionKey.toString(), H_USERID_KEY, user.get_id().toString());
		}
		if ( user.isAI() ) {
			pipeline.hset(sessionKey.toString(), H_ISAI, V_TRUE);
		}
		if ( user.isProxy() ) {
			pipeline.hset(sessionKey.toString(), H_ISPROXY, V_TRUE);
		}
		pipeline.expire(sessionKey.toString(), Constant.QUARTER_DAY_SECONDS);
		//2. Store the userId with sessionKey
		String userKey = null; 
		if ( user.get_id() != null ) {
			//Disable the timeout if exist
			if ( user.isProxy() ) {
				userKey = getProxyUserIdKey(user.get_id());
				pipeline.hset(userKey, H_MACHINE_KEY, userMachineId);
				pipeline.hset(userKey, H_RPCSERVER_KEY, rpcServerId);
				pipeline.hset(userKey, H_GAMESERVER_KEY, gameServerId);
				pipeline.hset(userKey, H_SESSION_KEY, sessionKey.toString());
				pipeline.hset(userKey, H_LOCALE_KEY, userLocale.toString());
				pipeline.expire(userKey, Constant.QUARTER_DAY_SECONDS);
			} else {
				userKey = user.get_id().toString();
				pipeline.hset(userKey, H_MACHINE_KEY, userMachineId);
				pipeline.hset(userKey, H_RPCSERVER_KEY, rpcServerId);
				pipeline.hset(userKey, H_GAMESERVER_KEY, gameServerId);
				pipeline.hset(userKey, H_SESSION_KEY, sessionKey.toString());
				pipeline.hset(userKey, H_LOCALE_KEY, userLocale.toString());
				pipeline.expire(userKey, Constant.QUARTER_DAY_SECONDS);				
			}
		}
		//3. Keep a local traceable key of user session
		pipeline.set(localSessionPrefix.concat(user.getSessionKey().toString()), user.get_id().toString());
		pipeline.expire(localSessionPrefix.concat(user.getSessionKey().toString()), Constant.QUARTER_DAY_SECONDS);
		pipeline.sync();
		return sessionKey;
	}
		
	/**
	 * Deregister an login user from sessionKey.
	 * @param session
	 * @param user
	 * @return
	 */
	public boolean deregisterSession(IoSession session, User user) {
		boolean result = true;
		SessionKey sessionKey = null;
		if ( user != null ) {
			sessionKey = user.getSessionKey();
		}
		if ( session != null ) {
			if ( user.isProxy() ) {
				ArrayList<SessionKey> proxySessionKeys = (ArrayList<SessionKey>)session.getAttribute(PROXY_SESSION_KEYS);
				if ( proxySessionKeys != null ) {
					proxySessionKeys.remove(sessionKey);
				}
				logger.debug("Remove User {}'s proxy sessionKey {}", user.getRoleName(), sessionKey.toString());
			} else {
				if ( sessionKey == null ) {
					sessionKey = (SessionKey)session.getAttribute(Constant.SESSION_KEY);
				}
				session.removeAttribute(Constant.SESSION_KEY);
				session.removeAttribute(LOCALE_SESSION_KEY);
			}
		}
		int sessionTimeoutSeconds = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.SESSION_TIMEOUT, 60);
		if ( user != null ) {
			if ( user.isAI() || user.isProxy() || sessionTimeoutSeconds <= 0 ) {
				try {
					Jedis jedis = JedisFactory.getJedis();
					if ( user != null ) {
						user.setSessionKey(null);
						user.setSession(null);
						String userIdStr = null;
						if ( user.isProxy() ) {
							userIdStr = getProxyUserIdKey(user.get_id());
						} else {
							userIdStr = user.get_id().toString(); 
						}
						jedis.del(userIdStr);
					}
					if ( sessionKey != null ) {
						jedis.del(sessionKey.toString());
						jedis.del(localSessionPrefix.concat(sessionKey.toString()));
						
						logger.debug("User [{}] session [{}] is removed", user.getRoleName(), sessionKey.toString());
					}
				} catch (Exception e) {
					logger.warn("Deregister session exception: {}", e.getMessage());
					if ( logger.isDebugEnabled() ) {
						logger.debug(e.getMessage(), e);
					}
					result = false;
				}
			} else {
				//Delete the session in the future.
				Jedis jedis = JedisFactory.getJedis();
				if ( sessionKey != null ) {
					jedis.expire(sessionKey.toString(), sessionTimeoutSeconds);
					jedis.expire(localSessionPrefix.concat(sessionKey.toString()), sessionTimeoutSeconds);
				}
				if ( user != null && user.get_id() != null ) {
					jedis.expire(user.get_id().toString(), sessionTimeoutSeconds);
				}
				logger.debug("User [{}] session {} is to be removed after 10 seconds.", 
						user!=null?user.getRoleName():Constant.EMPTY, user.getSessionKey().toString());
				user.setStatus(UserStatus.SESSION_CLOSED);
			}
		}
		return result;
	}
	
	/**
	 * Find the user's machine id by his/her userId
	 * @param userId
	 * @return
	 */
	public Locale findUserLocale(SessionKey sessionKey) {
		if ( sessionKey == null ) return Locale.SIMPLIFIED_CHINESE;
		Jedis jedis = JedisFactory.getJedis();
		String locale = jedis.hget(sessionKey.toString(), H_LOCALE_KEY);
		return StringUtil.parseLocale(locale, Locale.SIMPLIFIED_CHINESE);
	}
		
	/**
	 * Find the user's machine id by his/her userId
	 * @param userId
	 * @return
	 */
	public String findUserMachineId(UserId userId) {
		if ( userId == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String machineBytes = jedis.hget(userId.toString(), H_MACHINE_KEY);
		return machineBytes;
	}
	
	/**
	 * Find the user's machine id by his/her userId
	 * @param userId
	 * @return
	 */
	public String findUserRpcId(UserId userId) {
		if ( userId == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String rpcServerBytes = jedis.hget(userId.toString(), H_RPCSERVER_KEY);
		return rpcServerBytes;
	}
	
	/**
	 * Find the user's machine id by his/her userId
	 * @param userId
	 * @return
	 */
	public String findUserRpcId(SessionKey userSessionKey) {
		if ( userSessionKey == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String rpcServerBytes = jedis.hget(userSessionKey.toString(), H_RPCSERVER_KEY);
		return rpcServerBytes;
	}
	
	/**
	 * Find the user's machine id by his/her userId
	 * @param userId
	 * @return
	 */
	public String findUserGameServerId(UserId userId) {
		if ( userId == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String rpcServerBytes = jedis.hget(userId.toString(), H_GAMESERVER_KEY);
		return rpcServerBytes;
	}

	/**
	 * Find the user's machine id by his/her userId
	 * @param userId
	 * @return
	 */
	public String findUserGameServerId(SessionKey userSessionKey) {
		if ( userSessionKey == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String rpcServerBytes = jedis.hget(userSessionKey.toString(), H_GAMESERVER_KEY);
		if ( rpcServerBytes == null ) {
			rpcServerBytes = BillingJedis.getInstance().findUserGameServerId(userSessionKey);
		}
		return rpcServerBytes;
	}

	/**
	 * This method will find all users that are online. It 
	 * includes both users in this JVM and in other JVMs. 
	 * @return
	 */
	public List<SessionKey> findAllOnlineUsers() {
		return findAllOnlineUsers(-1);
	}

	/**
	 * Put a limit on the total number of online users
	 * @param limit
	 * @return
	 */
	public List<SessionKey> findAllOnlineUsers(int limit) {
		Jedis jedis = JedisFactory.getJedis();
		Set<String> sessionByteList = jedis.keys(L_SESSION_PATTERN);
		if ( limit < 0 ) limit = Integer.MAX_VALUE;
		int size = Math.min(limit, sessionByteList.size());
		ArrayList<SessionKey> sessionKeys = new ArrayList<SessionKey>(size);
		Iterator<String> iter = sessionByteList.iterator();
		for (int i=0; i<size && iter.hasNext(); i++) {
			String bytes = iter.next();
			String[] fields = bytes.split(Constant.UNDERLINE);
			String hexString = null;
			if ( fields != null && fields.length==3 ) {
				hexString = fields[2];
				sessionKeys.add(SessionKey.createSessionKeyFromHexString(hexString));
			}
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("Find total {} users online. ", size);
		}
		return sessionKeys;
	}
	
	/**
	 * Find the user's machine id by his/her sessionkey
	 * @param userId
	 * @return
	 */
	public String findSessionMachineId(SessionKey sessionKey) {
		if ( sessionKey == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String machineBytes = jedis.hget(sessionKey.toString(), H_MACHINE_KEY);
		return machineBytes;
	}
	
	/**
	 * Find the user's id by his/her sessionkey
	 * @param userId
	 * @return
	 */
	public UserId findUserIdBySessionKey(SessionKey sessionKey) {
		if ( sessionKey == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String bytes = jedis.hget(sessionKey.toString(), H_USERID_KEY);
		if ( bytes != null ) {
			return UserId.fromString(bytes);
		} else {
			return null;
		}
	}
	
	/**
	 * Find the user's sessionKey by his/her userid
	 * @param userId
	 * @return
	 */
	public SessionKey findSessionKeyByUserId(UserId userId) {
		if ( userId == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String bytes = jedis.hget(userId.toString(), H_SESSION_KEY);
		if ( bytes != null ) {
			return SessionKey.createSessionKeyFromHexString(bytes);
		} else {
			return null;
		}
	}
	
	/**
	 * Find the user's sessionKey by his/her userid
	 * @param userId
	 * @return
	 */
	public SessionKey findSessionKeyByProxyUserId(UserId userId) {
		if ( userId == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String key = getProxyUserIdKey(userId);
		String sessionKeyStr = jedis.hget(key, H_SESSION_KEY);
		if ( sessionKeyStr != null ) {
			return SessionKey.createSessionKeyFromHexString(sessionKeyStr);
		} else {
			return null;
		}
	}
	
	/**
	 * Check if a sessionKey belongs to an AI user.
	 * @param sesseionKey
	 * @return
	 */
	public boolean isSessionKeyFromAI(SessionKey sessionKey) {
		Jedis jedis = JedisFactory.getJedis();
		String bytes = jedis.hget(sessionKey.toString(), H_ISAI);
		if ( V_TRUE.equals(bytes) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if a sessionKey belongs to a proxy user.
	 * @param sesseionKey
	 * @return
	 */
	public boolean isSessionKeyFromProxy(SessionKey sesseionKey) {
		Jedis jedis = JedisFactory.getJedis();
		String bytes = jedis.hget(sesseionKey.toString(), H_ISPROXY);
		if ( V_TRUE.equals(bytes) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * Get the userId's proxy key in Redis
	 * @param idString
	 * @return
	 */
	private String getProxyUserIdKey(UserId userId) {
		return StringUtil.concat("proxy:", gameServerId, Constant.COLON, userId.toString());
	}
}
