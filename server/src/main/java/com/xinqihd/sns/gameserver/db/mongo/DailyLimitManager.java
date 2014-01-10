package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.DateUtil.DateUnit;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * This is an utility manager that provides facilities for those daily 
 * limiting alike game system, for example the new Caishen Pray and 
 * Treasure Hurting.
 * 
 * 1. The action limit will be cleared every day.
 * 2. The user can buy extra oppotunities but the price get higher.
 * 3. The total changes to buy new opportunity is limited with VIP level. 
 * 
 * @author wangqi
 * 
 */
public class DailyLimitManager {
	
	public static enum BuyResult {
		NO_CHANGE,
		NO_MONEY,
		SUCCESS,
	};

	private static final Logger logger = LoggerFactory
			.getLogger(DailyLimitManager.class);

	
	//User's role action last changed timestamp
	private static final String FIELD_LASTTIME_KEY = "lasttime";
	
	//当日的时间做KEY
	private static final String FIELD_TODAY_STR_KEY = "todaystr";
	//当日可供购买的次数
	private static final String FIELD_TODAY_TOTAL_KEY = "todaytotal";
	//当日已经购买的次数，用来计算价格的加成
	private static final String FIELD_TODAY_BUY_KEY = "todaybuy";
	
	private boolean disableRoleAction = false;
		
	private String redisPrefixKey = Constant.EMPTY;

	public DailyLimitManager(String keyName) {
		this.redisPrefixKey = keyName;
	}


	/**
	 * @return the disableRoleAction
	 */
	public boolean isDisableDailyLimit() {
		return disableRoleAction;
	}

	/**
	 * @param disableRoleAction the disableRoleAction to set
	 */
	public void setDisableDailyLimit(boolean disableRoleAction) {
		this.disableRoleAction = disableRoleAction;
	}
	
	/**
	 * Set the daily buy count to the given buyCount if it does not set today.
	 * Usually this is called when a user first login in a day.
	 * 
	 * The 'buyCount' limits the user's buying for extra opportunities.
	 * 
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean resetDailyBuyLimit(User user, 
			int buyCount, long currentTimeMillis) {
		
		String username = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(username);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		
		List<String> list = jedis.hmget(keyName, 
				FIELD_TODAY_STR_KEY, FIELD_TODAY_TOTAL_KEY);
		String dbTodayStr = list.get(0);
		String dbTodayValue = list.get(1);
		
		//判断日期值是否有效
		boolean isTodayValid = todayStr.equals(dbTodayStr);
		if ( !isTodayValid ) {
			//为当日初始化数据库的值
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(FIELD_TODAY_STR_KEY, todayStr);
			map.put(FIELD_TODAY_TOTAL_KEY, String.valueOf(buyCount));
			jedis.hmset(keyName, map);
		} else {
			jedis.hset(keyName, FIELD_TODAY_TOTAL_KEY, String.valueOf(buyCount));
			logger.debug("The {} buyCount is reset to {} today.", user.getRoleName(), buyCount);
		}
		jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
				DateUnit.DAILY, Calendar.getInstance()));
		
		return true;
	}
	
	/**
	 * Reset the opportunities count
	 * 
	 * @param user
	 * @param buyCount
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean resetDailyOpportunityCount(User user, 
			String redisFieldKey, long currentTimeMillis) {
		
		String username = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(username);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		
		List<String> list = jedis.hmget(keyName, 
				FIELD_TODAY_STR_KEY, redisFieldKey);
		String dbTodayStr = list.get(0);
		String dbTodayValue = list.get(1);
		
		//判断日期值是否有效
		boolean isTodayValid = todayStr.equals(dbTodayStr);
		if ( !isTodayValid ) {
			//为当日初始化数据库的值
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(FIELD_TODAY_STR_KEY, todayStr);
			map.put(redisFieldKey, Constant.ZERO);
			jedis.hmset(keyName, map);
		} else {
			jedis.hset(keyName, redisFieldKey, Constant.ZERO);
			logger.debug("The {} count is reset to 0 today.", user.getRoleName());
		}
		jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
				DateUnit.DAILY, Calendar.getInstance()));
		
		return true;
	}
	
	/**
	 * Set the daily opportunity count to value for today.
	 * 
	 * @param user
	 * @param redisFieldKey
	 * @param currentTimeMillis
	 * @param value
	 * @return
	 */
	public boolean setDailyOpportunityCount(User user, 
			String redisFieldKey, long currentTimeMillis, int value) {
		
		String username = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(username);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		
		List<String> list = jedis.hmget(keyName, 
				FIELD_TODAY_STR_KEY, redisFieldKey);
		String dbTodayStr = list.get(0);
		String dbTodayValue = list.get(1);
		
		//判断日期值是否有效
		boolean isTodayValid = todayStr.equals(dbTodayStr);
		if ( isTodayValid ) {
			String valueStr = String.valueOf(value);
			jedis.hset(keyName, redisFieldKey, valueStr);
			logger.debug("The opportunity for user {} is set to {}.", user.getRoleName(), valueStr);
			
			jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
					DateUnit.DAILY, Calendar.getInstance()));

			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 *
	 * Query today user's opportunities buying info.
	 * 
	 * Redis stores the following data:
	 *   FIELD_TODAY_STR_KEY: 2012-09-13
	 *   FIELD_TODAY_TOTAL_KEY: <today's total buying limit>
	 *   FIELD_TODAY_BUY_KEY: <today's already buying count>
	 *   
	 * @param user
	 * @param currentTimeMillis
	 * @param hook
	 * @return {buyCount, buyPrice, buyValue, buyTimes}
	 */
	public int[] queryRoleActionLimit(User user, 
			long currentTimeMillis, ScriptHook hook) {
		
		String username = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(username);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		
		List<String> list = jedis.hmget(keyName, FIELD_TODAY_STR_KEY, 
				FIELD_TODAY_TOTAL_KEY, FIELD_TODAY_BUY_KEY);
		String dbTodayStr = list.get(0);
		String dbTodayValue = list.get(1);
		String dbTodayBuy = list.get(2);
		//判断日期值是否有效
		boolean isTodayValid = todayStr.equals(dbTodayStr);
		if ( !isTodayValid ) {
			dbTodayBuy = Constant.ZERO;
		}
		
		ScriptResult result = ScriptManager.getInstance().runScript(
				hook, user, dbTodayBuy);
		
	  //当日可用的购买次数
		int buyCount = 1;
		//购买的价格
		int buyPrice = 10;
		//每次购买增加的体力点数
		int buyValue = 5;
		//当日已经购买的次数，用来计算价格
		int buyTimes = 0;
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			buyCount = (Integer)result.getResult().get(0);
			buyPrice = (Integer)result.getResult().get(1);
			buyValue = (Integer)result.getResult().get(2);
			buyTimes = (Integer)result.getResult().get(3);
		}

		if ( isTodayValid ) {
			//用数据库中的购买次数值覆盖默认值
			buyCount = StringUtil.toInt(dbTodayValue, buyCount);
		} else {
			//为当日初始化数据库的值
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(FIELD_TODAY_STR_KEY, todayStr);
			map.put(FIELD_TODAY_TOTAL_KEY, String.valueOf(buyCount));
			map.put(FIELD_TODAY_BUY_KEY, String.valueOf(buyTimes));
			jedis.hmset(keyName, map);
			jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
					DateUnit.DAILY, Calendar.getInstance()));
		}
				
		return new int[]{buyCount, buyPrice, buyValue, buyTimes};
	}
	
	/**
	 * Users buy extra roleActionPoints from game system.
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public BuyResult buyNewOpportunity(User user, ScriptHook hook, 
			String redisFieldKey, long currentTimeMillis) {
		
		BuyResult buyResult = BuyResult.SUCCESS;
		String username = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(username);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		boolean success = true;
		String  info = null;
		
		//Query today's limit.
		int[] todayLimit = queryRoleActionLimit(user, 
				currentTimeMillis, hook);
		int buyCount = todayLimit[0];
		if ( buyCount < 1 ) {
			//There is no more chance to buy extra points
			buyResult = BuyResult.NO_CHANGE;
			return buyResult;
		}
		if ( success ) {
			int buyPrice = todayLimit[1];
			int buyValue = todayLimit[2];
			//当日已经购买的次数
			int buyTimes = todayLimit[3];
			success = ShopManager.getInstance().payForSomething(user, MoneyType.YUANBAO, buyPrice, 1, null, false);
			if ( success ) {
				//update user status
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(FIELD_TODAY_STR_KEY, todayStr);
				map.put(FIELD_TODAY_TOTAL_KEY, String.valueOf(buyCount-1));
				map.put(FIELD_TODAY_BUY_KEY, String.valueOf(buyTimes+1));
				jedis.hmset(keyName, map);
				//Increment the actual roleaction points.
				jedis.hincrBy(keyName, redisFieldKey, -buyValue);
				jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
						DateUnit.DAILY, Calendar.getInstance()));
				
				//Send Stat
				StatClient.getIntance().sendDataToStatServer(user, 
						StatAction.ConsumeBuyPoint, MoneyType.YUANBAO, buyPrice, buyValue);
			} else {
				buyResult = BuyResult.NO_MONEY;
			}
		}

		return buyResult;
	}

	
	/**
	 * 
	 * @param user
	 * @param count
	 * @param currentTimeMillis
	 * @param sendBse
	 * @return
	 */
	public boolean consumeOpportunity(User user, String redisFieldKey, 
			int count, int limit, int increasePerHour, long currentTimeMillis) {
		
		if ( disableRoleAction ) return true;
		String username = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(username);
		int opportunities = getCurrentOpportunity(user, redisFieldKey, limit, 
				increasePerHour, currentTimeMillis);
		if ( opportunities > limit ) {
			return false;
		}
		boolean passCheck = true;
		if ( count > 0 ) {
			opportunities += count;
			if ( limit > 0 ) {
				if ( opportunities > limit ) {
					passCheck = false;
					logger.debug("User '{}' reach the roleaction limit {}", 
							new Object[]{username, limit});
				}
				storeOpportunityStatus(jedis, redisFieldKey, keyName, opportunities, currentTimeMillis);
			}
		}

		return passCheck;
	}

	/**
	 * Get the current opportunity count.
	 * If the increasePerHour>0, then every hour the available count
	 * will increase that count.
	 * 
	 * @param user
	 * @param redisFieldKey
	 * @param limit
	 * @param increasePerHour
	 * @param currentTimeMillis
	 * @return
	 */
	public int getCurrentOpportunity(User user, String redisFieldKey, int limit,
			int increasePerHour, long currentTimeMillis) {
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(user.getUsername());
		Map<String, String> map = jedis.hgetAll(keyName);
		String dbTodayStr = map.get(FIELD_TODAY_STR_KEY);
		String roleActionStr = map.get(redisFieldKey);
		String lastTimeStr = map.get(FIELD_LASTTIME_KEY);

		String currentRoleAction = jedis.hget(keyName, redisFieldKey);
		int roleAction = StringUtil.toInt(currentRoleAction, 0); 
		
		long lastTime = 0;
		
		//判断日期值是否有效
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		boolean isTodayValid = todayStr.equals(dbTodayStr);
		if ( !isTodayValid ) {
			storeOpportunityStatus(jedis, redisFieldKey, keyName, 0, currentTimeMillis);
			//reset user's roleaction if it > 0
			if ( roleAction > 0 ) roleAction = 0;
		} else {
			//每小时自动增长经验值
			if ( roleAction > 0 && increasePerHour > 0 ) {
				int gain = increasePerHour;
				if ( roleActionStr != null && lastTimeStr != null ) {
					roleAction = StringUtil.toInt(roleActionStr, 0);
					lastTime = Long.parseLong(lastTimeStr);
					int times = Math.round((currentTimeMillis - lastTime) / 3600000.0f);
					if ( times > 0 ) {
						roleAction = roleAction + (times * -gain);
						if ( roleAction < 0 ) roleAction = 0;
						storeOpportunityStatus(jedis, redisFieldKey, keyName, roleAction, currentTimeMillis);
					}
				}
			}	
		}

		return roleAction;
	}
	
	/**
	 * Check If the user still have extra role action points
	 * @param user
	 * @return
	 */
	public final boolean checkUserHasOpportunities(User user, int limit, 
			int increasePerHour, String redisFieldKey) {
  	int opportunity = this.getCurrentOpportunity(
  			user, redisFieldKey, limit, increasePerHour, System.currentTimeMillis());
  	if ( opportunity <= limit ) {
  		return true;
  	}
  	return false;
	}
	
	/**
	 * @param jedis
	 * @param keyName
	 * @param value
	 */
	public final void storeOpportunityStatus(Jedis jedis, String redisFieldKey, 
			String keyName, int value, long currentTimeMillis) {
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(redisFieldKey, String.valueOf(value));
		map.put(FIELD_LASTTIME_KEY, String.valueOf(currentTimeMillis));
		map.put(FIELD_TODAY_STR_KEY, todayStr);
		jedis.hmset(keyName, map);
		jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
				DateUnit.DAILY, Calendar.getInstance()));
		logger.debug("store user roleaction status");
	}
	
	/**
	 * Get the Redis "mail" list name for inbox type
	 * @param user
	 * @return
	 */
	public final String getRedisLimitKeyName(String username) {
		return StringUtil.concat(this.redisPrefixKey, username);
	}
	
}
