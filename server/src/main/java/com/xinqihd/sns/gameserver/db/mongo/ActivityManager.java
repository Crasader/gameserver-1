package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.PromotionPojo;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The game's activity manager 
 * 
 * @author wangqi
 *
 */
public class ActivityManager extends AbstractMongoManager {
	
	private static final Logger logger = LoggerFactory.getLogger(ActivityManager.class);
	
	private static final String COLL_NAME = "promotions";
	
	private static final String INDEX_NAME = "_id";
	
	private static ArrayList<PromotionPojo> dataList = new ArrayList<PromotionPojo>();

	
	private static ActivityManager instance = new ActivityManager();
	
	public static final String KEY_ACT_URL = "activity:url";
	public static final String KEY_ACT_USER = "activity:user:";
	/**
	 * 用于处理N倍经验值的KEY
	 */
	public static final String KEY_ACT_EXPRATE = "activity:exprate";
	public static final String KEY_USER_EXPRATE = "activity:userexprate:";
	/**
	 * 强化加成概率
	 */
	public static final String KEY_ACT_STRRATE = "activity:strrate";
	public static final String KEY_USER_STRRATE = "activity:userstrrate:";
	public static final String FIELD_BATTLECOUNT = "battle";
	public static final String FIELD_STRCOUNT = "strength";
	public static final String FIELD_EXPRATE = "exprate";
	public static final String FIELD_STRRATE = "strrate";
	public static final String FIELD_SERVERID = "serverId";
	
	//private float actExpRate = 0.0f;
	//private long actExpRateTimeout = -1;
	
	private long actUrlTimeout = -1;
	private String actUrl = null;
	
	
	private ActivityManager() {
		super(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		synchronized (dataList) {
			dataList.clear();
			for ( DBObject obj : list ) {
				PromotionPojo promotion = (PromotionPojo)MongoDBUtil.constructObject(obj);
				dataList.add(promotion);
				logger.debug("Load promotion {} from database.", promotion.getMessage());
			}
		}
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public void sendPromotionMessages(User user) {
		StringBuilder buf = new StringBuilder(200);
		for ( PromotionPojo promotionPojo : dataList ) {
			if ( promotionPojo != null ) {
				boolean sendPromotion = true;
				if ( user != null ) {
					if ( promotionPojo.getChannel() != null ) {
						if ( user.getChannel().contains(promotionPojo.getChannel()) ) {
							sendPromotion = true;
						} else {
							sendPromotion = false;
						}
						long currentMillis = System.currentTimeMillis();
						if ( sendPromotion ) {
							if ( promotionPojo.getStartMillis() > 0 ) {
								if ( promotionPojo.getStartMillis() < currentMillis ) {
									sendPromotion = true;
								} else {
									sendPromotion = false;
								}
							}
						}
						if ( sendPromotion ) {
							if ( promotionPojo.getEndMillis() > 0 ) {
								if ( promotionPojo.getEndMillis() > currentMillis ) {
									sendPromotion = true;
								} else {
									sendPromotion = false;
								}
							}
						}
					}
				}
				if ( sendPromotion ) {
					//ChatManager.getInstance().sendSysChat(user, promotionPojo.getMessage());
					buf.append(promotionPojo.getMessage()).append('\n');
				}
			}
		}//for...
		
		if ( buf.length() > 0 ) {
			SysMessageManager.getInstance().sendClientInfoRawMessage(user, buf.toString(), 
				Action.NOOP, Type.BULLETIN);
		}
	}
	
	/**
	 * Get the singleton ActivityManager
	 * @return
	 */
	public static final ActivityManager getInstance() {
		return instance;
	}
	
	/**
	 * Get the current activity url for displaying 
	 * when users login.
	 * 
	 * @return
	 */
	public String getActivityUrl() {
		if ( actUrlTimeout == -1 || System.currentTimeMillis() > actUrlTimeout ) {
			Jedis jedisDB = JedisFactory.getJedisDB();
			actUrl = jedisDB.get(KEY_ACT_URL);
			if ( actUrl != null ) {
				Long expireUrlLong = jedisDB.ttl(KEY_ACT_URL);
				if ( expireUrlLong != null && expireUrlLong.intValue() > 0 ) {
					long actExpRateExpireMillis = expireUrlLong.intValue()*1000;
					actUrlTimeout = System.currentTimeMillis() + actExpRateExpireMillis;
					logger.debug("The activity acturl {} will expire after {}", actUrl, actExpRateExpireMillis);
				} else {
					//Every minute checks the exprate.
					actUrlTimeout = System.currentTimeMillis() + 60000;					
				}
			} else {
				//Every minute checks the exprate.
				actUrlTimeout = System.currentTimeMillis() + 60000;
			}
		}
		return actUrl;
	}
	
	/**
	 * When a user logins, display the bulletin board.
	 * The bulletin board will be only displayed once for 
	 * a given user. The data will be saved in session database.
	 * 
	 * @param user
	 */
	public void displayActivityForUser(User user) {
		Jedis jedis = JedisFactory.getJedis();
		Calendar calendar = Calendar.getInstance();
		String key = StringUtil.concat(KEY_ACT_USER, user.getUsername());
		String today = DateUtil.getToday(calendar.getTimeInMillis());
		String dbToday = jedis.get(key);
		if ( dbToday == null || !today.equals(dbToday) ) {
			//Display the bulletin board
			int expire = DateUtil.getSecondsToNextDateUnit(DateUtil.DateUnit.DAILY, calendar);
			jedis.set(key, today);
			jedis.expire(key, expire);

			String activityUrl = getActivityUrl();
			if ( activityUrl != null ) {
				SysMessageManager.getInstance().sendClientInfoRawMessage(
						user.getSessionKey(), activityUrl, Action.NOOP, Type.PROMOTION);
			}
		}
	}
	
	/**
	 * Set the activity url for displaying when users login
	 * @param url
	 * @param expireSeconds
	 */
	public void setActivityUrl(String url, int expireSeconds) {
		if ( StringUtil.checkNotEmpty(url) ) {
			Jedis jedisDB = JedisFactory.getJedisDB();
			jedisDB.set(KEY_ACT_URL, url);
			if ( expireSeconds > 0 ) {
				jedisDB.expire(KEY_ACT_URL, expireSeconds);
			}
		}
	}
	
	/**
	 * 获取当前有效的经验值倍率 
	 * when users login.
	 * 
	 * @return
	 */
	public synchronized float getActivityExpRate(User user) {
		float actExpRate = 0.0f;
		Jedis jedisDB = JedisFactory.getJedisDB();
		Map<String, String> map = jedisDB.hgetAll(KEY_ACT_EXPRATE);
		if ( map.size() > 0 ) {
			String expRateStr = map.get(FIELD_EXPRATE);
			String serverId = map.get(FIELD_SERVERID);
			if ( user != null ) {
				if ( StringUtil.checkNotEmpty(serverId) ) {
					ServerPojo server = user.getServerPojo();
					if ( server != null ) {
						if ( !serverId.equals(server.getId()) ) {
							expRateStr = null;
						}
					}
				}
			}
			if ( StringUtil.checkNotEmpty(expRateStr) ) {
				try {
					actExpRate = Float.parseFloat(expRateStr);
				} catch (Exception e) {
				}
			} else {
				//Every minute checks the exprate.
				actExpRate = 0.0f;
			}
		}
		/**
		 * Check user specific exp rate.
		 */
		float userExprate = 0;
		if ( user != null ) {
			String key = StringUtil.concat(KEY_USER_EXPRATE, user.getUsername());
			map = jedisDB.hgetAll(key);
			if ( map.size() > 0 ) {
				String battleCountStr = map.get(FIELD_BATTLECOUNT);
				String exprateStr = map.get(FIELD_EXPRATE);
				int battleCount = StringUtil.toInt(battleCountStr, 0);
				if ( battleCount > 0 ) {
					try {
						userExprate = Float.parseFloat(exprateStr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		float finalExpRate = Math.max(actExpRate, userExprate);
		return finalExpRate;
	}
	
	/**
	 * 获取当前有效的强化提升概率 
	 * when users login.
	 * 
	 * @return
	 */
	public synchronized float getActivityStrengthRate(User user) {
		float actStrRate = 0.0f;
		Jedis jedisDB = JedisFactory.getJedisDB();
		Map<String, String> map = jedisDB.hgetAll(KEY_ACT_STRRATE);
		if ( map.size() > 0 ) {
			String expRateStr = map.get(FIELD_STRRATE);
			String serverId = map.get(FIELD_SERVERID);
			if ( user != null ) {
				if ( StringUtil.checkNotEmpty(serverId) ) {
					ServerPojo server = user.getServerPojo();
					if ( server != null ) {
						if ( !serverId.equals(server.getId()) ) {
							expRateStr = null;
						}
					}
				}
			}
			if ( StringUtil.checkNotEmpty(expRateStr) ) {
				try {
					actStrRate = Float.parseFloat(expRateStr);
				} catch (Exception e) {
				}
			} else {
				//Every minute checks the exprate.
				actStrRate = 0.0f;
			}
		}
		/**
		 * Check user specific exp rate.
		 */
		float userStrRate = 0;
		if ( user != null ) {
			String key = StringUtil.concat(KEY_USER_STRRATE, user.getUsername());
			map = jedisDB.hgetAll(key);
			if ( map.size() > 0 ) {
				String battleCountStr = map.get(FIELD_STRCOUNT);
				String exprateStr = map.get(FIELD_STRRATE);
				int battleCount = StringUtil.toInt(battleCountStr, 0);
				if ( battleCount > 0 ) {
					try {
						userStrRate = Float.parseFloat(exprateStr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		float finalStrRate = Math.max(actStrRate, userStrRate);
		if ( finalStrRate < 0 ) {
			finalStrRate = 0;
		}
		if ( finalStrRate > 0.2f ) {
			finalStrRate = 0.2f;
		}
		return finalStrRate;
	}
	
	/**
	 * Consume 1 battle count
	 */
	public synchronized void consumeDoubleExp(User user) {
		if ( user != null ) {
			Jedis jedisDB = JedisFactory.getJedisDB();
			String key = StringUtil.concat(KEY_USER_EXPRATE, user.getUsername());
			String battleCountStr = jedisDB.hget(key, FIELD_BATTLECOUNT);
			if ( StringUtil.checkNotEmpty(battleCountStr) ) {
				int battleCount = StringUtil.toInt(battleCountStr, 0);
				if ( battleCount > 0 ) {
					jedisDB.hset(key, FIELD_BATTLECOUNT, String.valueOf(battleCount-1));
				}
			}
		}
	}
	
	/**
	 * 设置N倍经验活动的经验倍率和持续时间
	 * 
	 * @param expRate
	 * @param seconds
	 */
	public synchronized void setActivityExpRate(String serverId, float expRate, int expireSeconds) {
		if ( expRate >= 0f ) {
			
			Jedis jedisDB = JedisFactory.getJedisDB();
			Map<String, String> map = new HashMap<String, String>();
			map.put(FIELD_EXPRATE, String.valueOf(expRate));
			if ( serverId != null ) {
				map.put(FIELD_SERVERID,	serverId);
			}
			jedisDB.hmset(KEY_ACT_EXPRATE, map);
			if ( expireSeconds > 0 ) {
				jedisDB.expire(KEY_ACT_EXPRATE, expireSeconds);
			}
		}
	}
	
	/**
	 * 设置强化加成活动的提升概率和持续时间
	 * 
	 * @param expRate
	 * @param seconds
	 */
	public synchronized void setActivityStrengthRate(String serverId, float strRate, int expireSeconds) {
		if ( strRate >= 0f ) {
			
			Jedis jedisDB = JedisFactory.getJedisDB();
			Map<String, String> map = new HashMap<String, String>();
			map.put(FIELD_STRRATE, String.valueOf(strRate));
			if ( serverId != null ) {
				map.put(FIELD_SERVERID,	serverId);
			}
			jedisDB.hmset(KEY_ACT_STRRATE, map);
			if ( expireSeconds > 0 ) {
				jedisDB.expire(KEY_ACT_STRRATE, expireSeconds);
			}
		}
	}
	
	/**
	 * 设置N倍经验活动的经验倍率和持续时间
	 * 
	 * @param expRate
	 * @param seconds
	 */
	public synchronized void setActivityExpRate(User user, int battleCount, float expRate, int expireSeconds) {
		if ( user != null ) {			
			if ( battleCount > 0 ) {
				Map<String, String> map = new HashMap<String, String>();
				
				String battleCountStr = String.valueOf(battleCount);
				String exprateStr = String.valueOf(expRate);
				map.put(FIELD_BATTLECOUNT, battleCountStr);
				map.put(FIELD_EXPRATE, exprateStr);
				
				Jedis jedisDB = JedisFactory.getJedisDB();
				String key = StringUtil.concat(KEY_USER_EXPRATE, user.getUsername());
				jedisDB.hmset(key, map);
				if ( expireSeconds > 0 ) {
					jedisDB.expire(key, expireSeconds);
				}
			}
		}
	}
}
