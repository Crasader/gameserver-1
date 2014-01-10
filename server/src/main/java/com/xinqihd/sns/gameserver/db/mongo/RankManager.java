package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Tuple;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossType;
import com.xinqihd.sns.gameserver.chat.ChatSender;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.DateUtil.DateUnit;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The RankManager is used to provide gobal real-time ranking for all users in game server.
 * It is base on Redis database to do that.
 * 
 * It provide the following ranking service:
 * 
 * Category:
 * 1) Global: all users
 * 2) Friend: all friends
 * 3) Guild: all guild members
 * 
 * Type:
 * 1) All
 * 2) Daily
 * 3) Monthly
 * 
 * Content:
 * 1) Power
 * 2) Level
 * 3) Wealth
 * 4) Achievement
 * 5) Medal
 * 
 * The set's names in Redis will include:
 *  1) Global_all_power: all users in all period's power ranking
 *  2) Global_all_level:
 *  3) Global_all_wealth:
 *  4) Global_all_achievement:
 *  5) Global_all_medal:
 *  6) Global_daily_power:
 *  7) Global_daily_level:
 *  8) Global_daily_wealth:
 *  9) Global_daily_achievement:
 * 10) Global_daily_medal:
 * ......
 * 
 * Redis will store user's specific data too:
 * "rank:<user_name>" 
 * 
 * @author wangqi
 *
 */
public final class RankManager {

	private static final int EXPIRE_SECOND = 86400*7;

	private static final Logger logger = LoggerFactory.getLogger(RankManager.class);
	
	private static final String KEY_PREFIX = "RANK:";
	
	private static RankManager instance = new RankManager();
	
	RankManager() {
		
	}
	
	/**
	 * Get a singleton instance for this manager.
	 * @return
	 */
	public static final RankManager getInstance() {
		return instance;
	}
	
	/**
	 * Store global rank datas
	 * @param user
	 * @param scoreType
	 * @param currentMillis
	 */
	public void storeGlobalRankData(User user, RankScoreType scoreType, 
			long currentMillis) {
		storeGlobalRankData(user, scoreType, currentMillis, RankFilterType.values());
	}
	
	/**
	 * Store the user's data into Redis ranking set
	 * @param user
	 * @param type
	 */
	public void storeGlobalRankData(User user, RankScoreType scoreType, 
			long currentMillis, RankFilterType[] filterTypes) {
		if ( user.isAI() || user.isDefaultUser() || user.isProxy() ) {
			logger.debug("Disable the ai or proxy user ranking");
			return;
		}
		if ( scoreType == null ) {
			logger.warn("#storeRankData: cannot store null RankScoreType");
			return;
		}
		
		int score = getUserScore(user, scoreType);
		
		/**
		 * Disable all other filter types except GLOBAL
		 */
		/*
		if ( filterTypes != null ) {
			for ( RankFilterType filter : filterTypes ) {
				String zsetName = getRankSetName(filter, scoreType);
				storeDataInZSet(zsetName, user, score, rankType, scoreType, filter, currentMillis);				
			}
		}
		*/
		RankFilterType filter = RankFilterType.TOTAL;
		/**
		 * Current server 
		 */
		RankType rankType = RankType.GLOBAL;
		String zsetName = getRankSetName(user, rankType, filter, scoreType, null);
		storeDataInZSet(zsetName, user, score, rankType, scoreType, filter, currentMillis);
		/**
		 * World
		 */
		rankType = RankType.WORLD;
		zsetName = getRankSetName(user, rankType, filter, scoreType, null);
		storeDataInZSet(zsetName, user, score, rankType, scoreType, filter, currentMillis);
	}

	/**
	 * Store the boss hurt data
	 * 
	 * @param user
	 * @param bossHurt
	 * @param currentMillis
	 */
	public void storeBossHurtRankData(User user, String bossId, int bossHurt, long currentMillis ) {
		/**
		 * The boss should be crossing server ranking
		 */
		String zsetName = getRankSetName(user, RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, bossId);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userName = user.getUsername();
		Double scoreDouble = jedisDB.zscore(zsetName, userName);
		int oldHurt = 0;
		if ( scoreDouble != null ) {
			oldHurt = scoreDouble.intValue();
		}
		int totalHurt = oldHurt + bossHurt;
		storeDataInZSet(zsetName, user, totalHurt, RankType.PVE, 
				RankScoreType.PVE, RankFilterType.TOTAL, currentMillis);
	}
	
	/**
	 * Store the boss hurt data for single type BOSS
	 * 
	 * @param user
	 * @param bossHurt
	 * @param currentMillis
	 */
	public void storeSingleBossHurtRankData(User user, String bossId, int bossHurt, 
			long currentMillis, Collection<BattleUser> allBattleUsers ) {
		/**
		 * 因为单人副本需要支持组队，所以在战斗结束时，需要在每个人的排行榜里面存放战斗中
		 * 所有人的战斗信息，targetUser表示目标的排行榜用户，user表示当前要存储的分数的用户
		 * 
		 * 2013-2-25
		 */
		/**
		 * The boss should be crossing server ranking
		 */
		for ( BattleUser bUser : allBattleUsers ) {
			User targetUser = bUser.getUser();
			if ( targetUser.isBoss() || targetUser.isAI() ) continue;
			String zsetName = getSingleBossRankSetName(targetUser, bossId);
			Jedis jedisDB = JedisFactory.getJedisDB();
			String userName = user.getUsername();
			Double scoreDouble = jedisDB.zscore(zsetName, userName);
			int oldHurt = 0;
			if ( scoreDouble != null ) {
				oldHurt = scoreDouble.intValue();
			}
			int totalHurt = oldHurt + bossHurt;
			logger.debug("Put zset {} with username:{} and score:{}", new Object[]{zsetName, user.getRoleName(), totalHurt});
			storeDataInZSet(zsetName, user, totalHurt, RankType.PVE, 
					RankScoreType.PVE, RankFilterType.TOTAL, currentMillis);
		}
	}
	
	/**
	 * Store a data member in the Redis zset.
	 * The size will keep growing when more users submit their score.
	 * Redis provides a O(log) algorithm for inserting. 
	 * I do a simple test in JedisUtil.main(). The result is as follows:
	 * 
			Step: 0[0-100000): 9124 millis.
			Step: 1[100000-200000): 9422 millis.
			Step: 2[200000-300000): 9445 millis.
			Step: 3[300000-400000): 9556 millis.
			Step: 4[400000-500000): 9462 millis.
			Step: 5[500000-600000): 10091 millis.
			Step: 6[600000-700000): 10266 millis.
			Step: 7[700000-800000): 9938 millis.
			Step: 8[800000-900000): 9987 millis.
			Step: 9[900000-1000000): 9835 millis.
			Step: 10[1000000-1100000): 10310 millis.
			Step: 11[1100000-1200000): 10488 millis.
			Step: 12[1200000-1300000): 10466 millis.
			Step: 13[1300000-1400000): 10832 millis.
			Step: 14[1400000-1500000): 10313 millis.
			Step: 15[1500000-1600000): 10241 millis.
			Step: 16[1600000-1700000): 10142 millis.
			Step: 17[1700000-1800000): 10193 millis.
			Step: 18[1800000-1900000): 10258 millis.
			Step: 19[1900000-2000000): 10568 millis.
			
	 * If there are many members with same score, that is the case in game,
	 * the insertion performance will be worse 1/2 than normal case. See
	 * the following data
	 *
			Step: 0[0-100000): 13931 millis.
			Step: 1[100000-200000): 13775 millis.
			Step: 2[200000-300000): 13897 millis.
			Step: 3[300000-400000): 14007 millis.
			Step: 4[400000-500000): 13824 millis.
			Step: 5[500000-600000): 14101 millis.
			Step: 6[600000-700000): 14035 millis.
			Step: 7[700000-800000): 13923 millis.
			Step: 8[800000-900000): 14200 millis.
			Step: 9[900000-1000000): 13983 millis.
			Step: 10[1000000-1100000): 14216 millis.
			Step: 11[1100000-1200000): 14343 millis.
			Step: 12[1200000-1300000): 14330 millis.
			Step: 13[1300000-1400000): 14049 millis.
			Step: 14[1400000-1500000): 14093 millis.
			Step: 15[1500000-1600000): 11804 millis.
			Step: 16[1600000-1700000): 11404 millis.
			Step: 17[1700000-1800000): 11432 millis.
			Step: 18[1800000-1900000): 11530 millis.
			Step: 19[1900000-2000000): 11539 millis.
 
	 *
	 * The inserting cost is near a constant number at 2 million level.
	 * So it is feasible for us to implement a real-time like ranking system.
	 * 
	 * @param zsetName
	 * @param memeber
	 * @param score
	 * @param ttl
	 * @return
	 */
	public final boolean storeDataInZSet(String zsetName, User user, 
			int score, RankType rankType, RankScoreType scoreType, RankFilterType filterType, 
			long currentMillis) {
		
		String userName = user.getUsername();
		boolean result = true;
		Jedis jedisDB = JedisFactory.getJedisDB();
		boolean exist = jedisDB.exists(zsetName);
		
		//Check if it need to store last rank
		boolean storeYesterday = false;
		String yesterday = queryUserSpecificData(userName, scoreType, Field.STR_YESTERDAY );
		if ( yesterday == null ) {
			storeYesterday = true;
		} else {
			String currentYesterday = DateUtil.getYesterday(currentMillis);
			if ( !currentYesterday.equals(yesterday) ) {
				storeYesterday = true;
			}
		}
		int currentRank = queryUserCurrentRank(zsetName, user);
		if ( storeYesterday ) {
			String currentYesterday = DateUtil.getYesterday(currentMillis);
			storeUserSpecificData(userName, scoreType, Field.STR_YESTERDAY, currentYesterday);
			storeUserSpecificData(userName, scoreType, Field.LAST_DAY_RANK, String.valueOf(currentRank));
			logger.debug("#storeDataInZSet: Store rank {} as user's last day '{}' rank",
					currentRank, currentYesterday);
		}
		boolean storeLastMonth = false;
		String lastMonth = queryUserSpecificData(userName, scoreType, Field.STR_LAST_MONTH );
		if ( lastMonth == null ) {
			storeLastMonth = true;
		} else {
			String currentLastMonth = DateUtil.getLastMonth(currentMillis);
			if ( !currentLastMonth.equals(lastMonth) ) {
				storeLastMonth = true;
			}
		}
		if ( storeLastMonth ) {
			String currentLastMonth = DateUtil.getLastMonth(currentMillis);
			storeUserSpecificData(userName, scoreType, Field.STR_LAST_MONTH, currentLastMonth);
			storeUserSpecificData(userName, scoreType, Field.LAST_MONTH_RANK, String.valueOf(currentRank));
			logger.debug("#storeDataInZSet: Store rank {} as user's last month '{}' rank",
					currentRank, currentLastMonth);
		}
		//Store total rank
		storeUserSpecificData(userName, scoreType, Field.TOTAL_RANK, String.valueOf(currentRank));
		
		//Add the value
		jedisDB.zadd(zsetName, score, userName);
		if ( logger.isDebugEnabled() ) {
			Long ttl = jedisDB.ttl(zsetName);
			logger.debug("Put member {} with score {} into zset {}. The ttl is {}", 
					new Object[]{userName, score, zsetName, ttl});
		}
		//Set the expire seconds if necessary.
		if ( !exist ) {
			if ( filterType != RankFilterType.TOTAL ) {
				Calendar cal = Calendar.getInstance();
				int seconds = calculateExpireSecond(filterType, cal);
				Long expireResult = jedisDB.expire(zsetName, seconds);
				if ( expireResult == null || expireResult.intValue() != 1 ) {
					result = false;
				} else {
					logger.debug("The Redis zset {} will expire after {} seconds", zsetName, seconds);
				}
			}
		}
		
		/**
		 * Only the global total ranking are sending notify.
		 */
		if ( rankType == RankType.GLOBAL && filterType == RankFilterType.TOTAL ) {
			sendUserRankNotify(user, currentRank, scoreType, filterType);
		}
		
		//Thie is the task that use power's rank
		TaskManager.getInstance().processUserTasks(user, 
				TaskHook.RANK, scoreType, filterType, currentRank);
		
		return result;
	}
	
	/**
	 * Query the user's current rank in the given zset.
	 * @param zsetName
	 * @param user
	 * @return
	 */
	public final int queryUserCurrentRank(String zsetName, BasicUser user) {
		return queryUserCurrentRank(zsetName, user.getUsername());
	}
	
	/**
	 * Query the user's current rank in the given zset.
	 * @param zsetName
	 * @param user
	 * @return
	 */
	public final int queryUserCurrentRank(String zsetName, String userName) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long rankLong = jedisDB.zrevrank(zsetName, userName);
		if ( rankLong == null ) {
			//Not store the user before. Treat him as the last one
			Long zcard = jedisDB.zcard(zsetName);
			if ( zcard != null ) {
				return zcard.intValue()+1;
			} else {
				return 1;
			}
		} else {
			return rankLong.intValue()+1;
		}
	}
	
	/**
	 * Query the user's current rank in the given zset.
	 * @param user
	 * @param rankId TODO
	 * @param zsetName
	 * @return
	 */
	public final RankUser queryUserCurrentRank(User user, RankType rankType, 
			RankFilterType filterType, RankScoreType scoreType, String rankId) {
		
		RankUser rankUser = new RankUser();
		rankUser.setBasicUser(user);
		String userName = user.getUsername();
		String zsetName = null;
		if ( StringUtil.checkNotEmpty(rankId) ) {
			zsetName = getRankSetName(user, rankType, filterType, scoreType, rankId);
		} else {
			zsetName = getRankSetName(user, filterType, scoreType);
		}
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long rankLong = jedisDB.zrevrank(zsetName, userName);
		Double scoreDouble = jedisDB.zscore(zsetName, userName);
		if ( rankLong == null && scoreDouble == null ) {
			return null;
		} else {
			rankUser.setRank(rankLong.intValue()+1);
			rankUser.setScore((int)scoreDouble.doubleValue());
		}
		return rankUser;
	}
	
	/**
	 * Query the user's current rank in the given zset.
	 * @param user
	 * @param rankId TODO
	 * @param zsetName
	 * @return
	 */
	public final RankUser queryUserCurrentRankForSingleBoss(User user, String bossId) {
		RankUser rankUser = new RankUser();
		rankUser.setBasicUser(user);
		String userName = user.getUsername();
		String zsetName = getSingleBossRankSetName(user, bossId);
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long rankLong = jedisDB.zrevrank(zsetName, userName);
		Double scoreDouble = jedisDB.zscore(zsetName, userName);
		if ( rankLong == null && scoreDouble == null ) {
			return null;
		} else {
			rankUser.setRank(rankLong.intValue()+1);
			rankUser.setScore((int)scoreDouble.doubleValue());
		}
		return rankUser;
	}
	
	/**
	 * Query the last day or last month or total period last rank.
	 * @param filter
	 * @param user
	 * @return
	 */
	public final int queryUserPassDayRank(RankFilterType filter, 
			RankScoreType scoreType, String userName) {
		String userKey = getUserSpecificDataKeyName(userName);
		String member = null;
		switch ( filter ) {
			case DAILY:
				member = Field.LAST_DAY_RANK.name();
				break;
			case MONTHLY:
				member = Field.LAST_MONTH_RANK.name();
				break;
			case TOTAL:
				member = Field.TOTAL_RANK.name();
				break;
			default:
				logger.warn("Unsupport {} filter type now.", filter);
				break;
		}
		if ( member != null ) {
			String memberKey = StringUtil.concat(scoreType.name(), Constant.COLON, member);
			Jedis jedisDB = JedisFactory.getJedisDB();
			String value = jedisDB.hget(userKey, memberKey);
			return StringUtil.toInt(value, 0);
		}
		return 0;
	}
	
	/**
	 * Query the data rank from zset ( from highest to lowest ).
	 * 
	 * Note: the rank returned is 1 based (i.e. [1 to Infinite) )
	 * 
	 * @param zsetName
	 * @param member
	 * @param filterType
	 * @return
	 */
	public final int queryDataRankInZSet(String zsetName, String member) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long rankLong = jedisDB.zrevrank(zsetName, member);
		int rank = 0;
		if ( rankLong != null ) {
			rank = rankLong.intValue() + 1;
		}
		return rank;
	}
	
	/**
	 * This method is mainly used to store user's ranking data like yesterday's rank
	 * or last month rank etc.
	 * 
	 * @param user
	 * @param key
	 * @param value
	 */
	public final void storeUserSpecificData(String userName, RankScoreType scoreType, Field field, String value) {
		String key = getUserSpecificDataKeyName(userName);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String member = StringUtil.concat(scoreType.name(), Constant.COLON, field.name());
		jedisDB.hset(key, member, value);
		jedisDB.expire(key, EXPIRE_SECOND);
	}
	
	/**
	 * This method is used to query user specific ranking data.
	 * @param user
	 * @param field
	 * @return
	 */
	public final String queryUserSpecificData(String userName, RankScoreType scoreType, Field field) {
		String key = getUserSpecificDataKeyName(userName);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String member = StringUtil.concat(scoreType.name(), Constant.COLON, field.name());
		return jedisDB.hget(key, member);
	}
	
	/**
	 * Remove all user specific data from Redis
	 * @param user
	 */
	public final void clearUserSpecificData(String userName) {
		String key = getUserSpecificDataKeyName(userName);
		Jedis jedisDB = JedisFactory.getJedisDB();
		jedisDB.del(key);
	}
		
	/**
	 * Query all the RankUser data by their scores in Redis
	 * @param rankType
	 * @param filterType
	 * @param startRank
	 * @param endRank
	 * @param rankId TODO
	 * @param scoreTyp
	 * @return
	 */
	public final Collection<RankUser> getAllRankUsers(User user, 
			RankType rankType, RankFilterType filterType, 
			RankScoreType scoreType, int startRank, int endRank, String rankId) {
		
		String zsetName = null;
		/**
		 * For old ranking, the rankType is always GLOBAL. It 
		 * is a bug but I have to adopt it.
		 * 
		 * wangqi 2012-10-09
		 */
		if ( StringUtil.checkNotEmpty(rankId) ) {
			Boss boss = BossManager.getInstance().getBossInstance(user, rankId);
			if ( boss != null ) {
				if ( boss.getBossPojo().getBossType() == BossType.SINGLE ) {
					zsetName = getSingleBossRankSetName(user, rankId);
					return getAllRankUsers(user, rankType, filterType, scoreType, startRank,
							endRank, rankId, zsetName);
				} else {
					if ( StringUtil.checkNotEmpty(rankId) ) {
						zsetName = getRankSetName(user, rankType, filterType, scoreType, rankId);
					} else {
						if ( rankType == RankType.WORLD) {
							zsetName = getRankSetName(user, rankType, filterType, scoreType, null);
						} else if ( rankType == RankType.ONLINE ){
							zsetName = getGlobalRankSetName(user, RankType.WORLD, filterType, scoreType, null);
						} else {
							zsetName = getRankSetName(user, filterType, scoreType);
						}
					}
					return getAllRankUsers(user, rankType, filterType, scoreType, startRank,
							endRank, rankId, zsetName);
				}
			} else {
				/**
				 * 还没有挑战过
				 */
				return new ArrayList<RankUser>();
			}
		} else {
			if ( StringUtil.checkNotEmpty(rankId) ) {
				zsetName = getRankSetName(user, rankType, filterType, scoreType, rankId);
			} else {
				if ( rankType == RankType.WORLD) {
					zsetName = getRankSetName(user, rankType, filterType, scoreType, null);
				} else if ( rankType == RankType.ONLINE ){
					zsetName = getGlobalRankSetName(user, RankType.WORLD, filterType, scoreType, null);
				} else {
					zsetName = getRankSetName(user, filterType, scoreType);
				}
			}
			return getAllRankUsers(user, rankType, filterType, scoreType, startRank,
					endRank, rankId, zsetName);
		}
	}

	/**
	 * @param user
	 * @param rankType
	 * @param filterType
	 * @param scoreType
	 * @param startRank
	 * @param endRank
	 * @param rankId
	 * @param zsetName
	 * @return
	 */
	public Collection<RankUser> getAllRankUsers(User user, RankType rankType,
			RankFilterType filterType, RankScoreType scoreType, int startRank,
			int endRank, String rankId, String zsetName) {
		ArrayList<RankUser> userList = new ArrayList<RankUser>();
		Jedis jedisDB = JedisFactory.getJedisDB();
		if ( rankType == RankType.GLOBAL || rankType == RankType.WORLD ) {
			Set<Tuple> tuples = jedisDB.zrevrangeWithScores(zsetName, startRank, endRank);
			int rank = startRank+1;
			for ( Tuple member : tuples ) {
				RankUser rankUser = new RankUser();
				rankUser.setRank(rank++);
				rankUser.setScore((int)member.getScore());
				String userName = member.getElement();
				BasicUser basicUser = UserManager.getInstance().queryBasicUser(userName);
				if ( basicUser != null ) {
					rankUser.setBasicUser(basicUser);
					userList.add(rankUser);
				}
			}
			return userList;
		} else if ( rankType == RankType.FRIEND ) {
			TreeSet<RankUser> rankUserSet = new TreeSet<RankUser>();
			Collection<Relation> relations = user.getRelations();
			if ( relations != null ) {
				for ( Relation relation : relations ) {
					Collection<People> people = relation.listPeople();
					if ( people != null ) {
						for ( People p : people ) {
							Double scoreDouble = jedisDB.zscore(zsetName, p.getUsername());
							int score = 0;
							if ( scoreDouble != null ) {
								score = scoreDouble.intValue();
								RankUser rankUser = new RankUser();
								rankUser.setRank(0);
								rankUser.setScore(score);
								BasicUser basicUser = UserManager.getInstance().queryBasicUser(p.getUsername());
								rankUser.setBasicUser(basicUser);
								
								rankUserSet.add(rankUser);
							} else {
								//maybe it is an AI
								//ignore it.
							}
						}
					}
				}
			}
			//Add my ranking
			Double scoreDouble = jedisDB.zscore(zsetName, user.getUsername());
			int score = 0;
			if ( scoreDouble != null ) {
				score = scoreDouble.intValue();
			}
			RankUser rankUser = new RankUser();
			rankUser.setRank(0);
			rankUser.setScore(score);
			rankUser.setBasicUser(user);
			rankUserSet.add(rankUser);
			
			//Re-arrange the rank order
			int count = rankUserSet.size();
			int i=1;
			for ( RankUser ruser : rankUserSet ) {
				ruser.setRank(i++);
			}
			return rankUserSet;
		} else if ( rankType == RankType.GUILD ) {
			//TODO
		} else if ( rankType == RankType.ONLINE ) {
			TreeSet<RankUser> rankUserSet = new TreeSet<RankUser>();
			
			List<SessionKey> onlineSessions = GameContext.getInstance().
					getSessionManager().findAllOnlineUsers(1000);
			int number = 0;
			for ( SessionKey userSessionKey : onlineSessions ) {
				boolean isAI = GameContext.getInstance().getSessionManager().isSessionKeyFromAI(userSessionKey);
				if ( isAI ) continue;
				
				BasicUser basicUser = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
				if ( basicUser == null ) {
					UserId userId = GameContext.getInstance().findUserIdBySessionKey(userSessionKey);
					if ( userId != null ) {
						basicUser = UserManager.getInstance().queryBasicUser(userId);
					}
				}
				if ( basicUser != null ) {
					Double scoreDouble = jedisDB.zscore(zsetName, basicUser.getUsername());
					int score = 0;
					if ( scoreDouble != null ) {
						score = scoreDouble.intValue();
					} else {
						if ( scoreType == RankScoreType.POWER ) {
							score = basicUser.getPower();
						}
					}
					RankUser rankUser = new RankUser();
					rankUser.setRank(0);
					rankUser.setScore(score);
					rankUser.setBasicUser(basicUser);
					rankUserSet.add(rankUser);
					number++;
					if ( number > 100 ) {
						break;
					}
				} else {
					logger.info("Failed to find basic user {}", userSessionKey);
				}
			}
			
			//Re-arrange the rank order
			int count = rankUserSet.size();
			int i=1;
			for ( RankUser ruser : rankUserSet ) {
				ruser.setRank(i++);
			}
			return rankUserSet;
		} else if ( rankType == RankType.PVE ) {
			Set<Tuple> tuples = jedisDB.zrevrangeWithScores(zsetName, startRank, endRank);
			int rank = startRank+1;
			RankUser myRankUser = null;
			for ( Tuple member : tuples ) {
				RankUser rankUser = new RankUser();
				rankUser.setRank(rank++);
				rankUser.setScore((int)member.getScore());
				String userName = member.getElement();
				if ( user != null && user.getUsername().equals(userName) ) {
					myRankUser = rankUser;
					myRankUser.setBasicUser(user);
				} else {
					BasicUser basicUser = UserManager.getInstance().queryBasicUser(userName);
					rankUser.setBasicUser(basicUser);
					userList.add(rankUser);
				}
			}
			//Add myself data into the first cell
			ArrayList<RankUser> pveRankList = new ArrayList<RankUser>(userList.size()+1);
			if ( tuples.size() > 0 && user != null ) {
				if ( myRankUser == null ) {
					Boss boss = BossManager.getInstance().getBossInstance(user, rankId);
					if ( boss != null && boss.getBossPojo().getBossType() == BossType.SINGLE ) {
						myRankUser = queryUserCurrentRankForSingleBoss(user, rankId);
					} else {
						myRankUser = queryUserCurrentRank(user, rankType, filterType, scoreType, rankId);
					}
				}
				pveRankList.add(myRankUser);
			}
			pveRankList.addAll(userList);
			
			return pveRankList;
		}
		return userList;
	}
	
	/**
	 * Get the pve rank user list.
	 * @param bossId
	 * @param limit
	 * @return
	 */
	public ArrayList<RankUser> getPVERankUser(String bossId, int limit) {
		String zsetName = null;
		ArrayList<RankUser> userList = new ArrayList<RankUser>(); 
		/**
		 * For old ranking, the rankType is always GLOBAL. It 
		 * is a bug but I have to adopt it.
		 * 
		 * wangqi 2012-10-09
		 */
		if ( StringUtil.checkNotEmpty(bossId) ) {
			int startRank = 0;
			Jedis jedisDB = JedisFactory.getJedisDB();
			zsetName = getRankSetName(null, RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, bossId);
			Set<Tuple> tuples = jedisDB.zrevrangeWithScores(zsetName, 0, limit);
			int rank = startRank+1;
			for ( Tuple member : tuples ) {
				RankUser rankUser = new RankUser();
				rankUser.setRank(rank++);
				rankUser.setScore((int)member.getScore());
				String userName = member.getElement();
				BasicUser basicUser = UserManager.getInstance().queryBasicUser(userName);
				rankUser.setBasicUser(basicUser);
				userList.add(rankUser);
			}
		} else {
			logger.warn("bossId is null for getting ranking");
		}
		return userList;
	}
	
	/**
	 * Get the total number of ranking users in Redis
	 * @return
	 */
	public final int getTotalRankUserCount(User user, RankType rankType, RankFilterType filterType, 
			RankScoreType scoreType) {
		return getTotalRankUserCount(user, rankType, filterType, scoreType, null);
	}

	/**
	 * 
	 * @param rankType
	 * @param filterType
	 * @param scoreType
	 * @param rankId
	 * @return
	 */
	public final int getTotalRankUserCount(User user, RankType rankType, RankFilterType filterType, 
			RankScoreType scoreType, String rankId) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String zsetName = null;
		if ( StringUtil.checkNotEmpty(rankId) ) {
			Boss boss = BossManager.getInstance().getBossInstance(user, rankId);
			if ( boss.getBossPojo().getBossType() == BossType.SINGLE ) {
				zsetName = getSingleBossRankSetName(user, rankId);
			}
		}
		if ( zsetName == null ) {
			if ( StringUtil.checkNotEmpty(rankId) ) {
				zsetName = getRankSetName(user, rankType, filterType, scoreType, rankId);
			} else {
				zsetName = getRankSetName(user, filterType, scoreType);
			}
		}
		Long countLong = jedisDB.zcard(zsetName);
		if ( countLong != null ) {
			int count = countLong.intValue();
			int max = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_RANK_MAX, 100);
			return Math.min(count, max);
		}
		return 0;
	}
	
	
	/**
	 * Get current user's RankUser object
	 * @param user
	 * @param rankType
	 * @param filterType
	 * @param scoreType
	 * @return
	 */
	public final RankUser getCurrentRankUser(User user, 
			RankType rankType, RankFilterType filterType, RankScoreType scoreType) {
		
		String zsetName = getRankSetName(user, filterType, scoreType);
		int rank = queryUserCurrentRank(zsetName, user);
		//Always compare with yesterday's rank
		int lastRank = queryUserPassDayRank(filterType, scoreType, user.getUsername());
		int score = getUserScore(user, scoreType);

		RankUser rankUser = new RankUser();
		rankUser.setRank(rank);
		rankUser.setScore(score);
		rankUser.setBasicUser(user);
		/**
		 * 上一次的排名减去本次的排名
		 */
		rankUser.setRankChange( rank - lastRank );
		
		return rankUser;
	}
	
	/**
	 * Get the final Redis zset's name
	 * @param filterType
	 * @param scoreType
	 * 
	 * @return
	 */
	public static final String getRankSetName(User user, 
			RankFilterType filterType, RankScoreType scoreType ) {
		return getRankSetName(user, RankType.GLOBAL, filterType, scoreType, null);
	}

	/**
	 * Get the rank set name with given rank id.
	 * It is used for boss ranking.
	 * 
	 * @param filterType
	 * @param scoreType
	 * @param rankId
	 * @return
	 */
	public static final String getRankSetName(User user, RankType rankType, 
			RankFilterType filterType, RankScoreType scoreType, String rankId ) {
		String serverId = Constant.EMPTY;
		if ( user != null ) {
			ServerPojo server = user.getServerPojo();
			if ( server != null ) {
				serverId = server.getId();
			}
		}
		return getRankSetNameByServerId(serverId, rankType, filterType, scoreType, rankId);
	}
	
	/**
	 * @param user
	 * @param bossId
	 * @param userName
	 * @return
	 */
	public String getSingleBossRankSetName(User user, String bossId) {
		String zsetName = getRankSetName(user, RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, bossId);
		zsetName = StringUtil.concat(zsetName, Constant.COLON, user.getUsername());
		return zsetName;
	}
	
	/**
	 * 
	 * @param user
	 * @param rankType
	 * @param filterType
	 * @param scoreType
	 * @param rankId
	 * @return
	 */
	public static final String getGlobalRankSetName(User user, RankType rankType, 
			RankFilterType filterType, RankScoreType scoreType, String rankId ) {
		String serverId = Constant.EMPTY;
		return getRankSetNameByServerId(serverId, rankType, filterType, scoreType, rankId);
	}
	
	/**
	 * 
	 * @param serverId
	 * @param rankType
	 * @param filterType
	 * @param scoreType
	 * @param rankId
	 * @return
	 */
	public static final String getRankSetNameByServerId(String serverId, RankType rankType, 
			RankFilterType filterType, RankScoreType scoreType, String rankId ) {
		/**
		 * The world ranking will not consider serverId
		 */
		if ( rankType == RankType.WORLD || rankType == RankType.PVE ) {
			serverId = Constant.EMPTY;
		}
		String zsetName = Constant.EMPTY;
		if ( StringUtil.checkNotEmpty(rankId) ) {
			zsetName = StringUtil.concat( KEY_PREFIX, serverId, Constant.COLON, rankType, Constant.COLON,
				filterType.name(), Constant.COLON, scoreType.name(), Constant.COLON, rankId);
		} else {
			zsetName = StringUtil.concat( KEY_PREFIX, serverId, Constant.COLON, rankType, Constant.COLON,
					filterType.name(), Constant.COLON, scoreType.name());
		}
		return zsetName;
	}
	
	/**
	 * The Redis db will store all user specific data under "rank:<user_name>" hashtable.
	 * This method will return the key.
	 * 
	 * @param user
	 * @return
	 */
	public static final String getUserSpecificDataKeyName(String userName) {
		String keyName = StringUtil.concat( KEY_PREFIX, userName);
		return keyName;
	}
	
	/**
	 * Every rank will have a expire seconds, like a daily rank,
	 * weekly rank or monthly rank. We will calculate the rank expire
	 * time here.
	 * 
	 * @return
	 */
	public static final int calculateExpireSecond(RankFilterType filterType, Calendar currentTime) {
		int seconds = Integer.MAX_VALUE;
		if ( currentTime == null ) {
			currentTime = Calendar.getInstance();
		}
		switch (filterType) {
			case TOTAL:
				seconds = Integer.MAX_VALUE;
				break;
			case DAILY:
				seconds = DateUtil.getSecondsToNextDateUnit(DateUnit.DAILY, currentTime);
				break;
			case MONTHLY:
				seconds = DateUtil.getSecondsToNextDateUnit(DateUnit.MONTHLY, currentTime);
				break;
			case FIVE_SECONDS:
				seconds = 5;
				break;
		}
		
		logger.debug("The expire seconds will be {} for RankFilterType {}", seconds, filterType);
		return seconds;
	}
	
	/**
	 * If the user's power, level etc win a higher rank than before, 
	 * then the system will push a message to user.
	 * 
	 * @param user
	 * @param scoreType
	 * @param filterType
	 * @return
	 */
	public final void sendUserRankNotify(User user, int lastRank, RankScoreType scoreType, 
			RankFilterType filterType ) {
		if ( user.isAI() ) {
			return;
		}
		String zsetName = getRankSetName(user, filterType, scoreType);
		//int lastRank = queryUserPassDayRank(filterType, user);
		int currentRank = queryUserCurrentRank(zsetName, user);
		int rankChange = lastRank - currentRank;
		if ( rankChange < 0 ) {
			//Now I want to disable the ranking down message
			return;
		}
		//Send both up and down message
		if ( rankChange != 0 ) {
			String messageKey = null;
			String chatKey = null;
			switch ( scoreType ) {
				case POWER:
					messageKey = "ranking.total.power";
					chatKey		 = "notice.ranking.total.power";
					break;
				case KILL:
					messageKey = "ranking.total.level";
					chatKey		 = "notice.ranking.total.level";
					break;
				case WEALTH:
					messageKey = "ranking.total.yuanbao";
					chatKey		 = "notice.ranking.total.yuanbao";
					break;
				case ACHIEVEMENT:
					break;
				case MEDAL:
					messageKey = "ranking.total.medal";
					chatKey		 = "notice.ranking.total.medal";
					break;
			}
			if ( messageKey != null ) {
				int absRank = rankChange;
				if ( rankChange < 0 ) {
					absRank = -absRank;
					messageKey = messageKey.concat(Constant.DOWN);
				}
				String message = Text.text(messageKey, absRank, currentRank);
								
				//Send chat message to all other users.
				if ( rankChange > 0 && currentRank < 10) {
					String roleName = UserManager.getDisplayRoleName(user.getRoleName());
					String chat = Text.text(chatKey, roleName, rankChange, currentRank);
					ChatSender chatSender = ChatSender.getInstance();
					chatSender.sendSystemMessage(chat);
				}
				
				String weiboKey = null;
				if ( currentRank == 1 ) {
					weiboKey = "weibo.rank.no1";
				} else if ( currentRank <= 10 ) {
					weiboKey = "weibo.rank.no10";
				} else {
					weiboKey = StringUtil.concat("weibo.rank.", MathUtil.nextFakeInt(2));
				}
				String weibo = Text.text(weiboKey, DateUtil.formatDateTime(new Date()));
				
				//Send message to client
				SysMessageManager.getInstance().
					sendClientInfoWeiboMessage(user.getSessionKey(), message, weibo, Type.WEIBO);
//				return message;
			}
			//Call the TaskHook
			TaskManager.getInstance().processUserTasks(user, 
					TaskHook.RANK, scoreType, filterType, currentRank);
		}
//		return null;
	}
	
	/**
	 * Get user's score.
	 * @param user
	 * @param scoreType
	 * @return
	 */
	private int getUserScore(User user, RankScoreType scoreType) {
		int score = 0;
		switch ( scoreType ) {
			case POWER:
				score = user.getPower();
				break;
			case KILL:
				/**
				 * Change level to total kills
				 */
				/*
				int level = user.getLevel();
				LevelPojo levelPojo = LevelManager.getInstance().getLevel(level);
				score = levelPojo.getSumExp()+user.getExp();
				*/
				score = user.getTotalKills();
				break;
			case WEALTH:
				score = user.getYuanbao();
				break;
			case ACHIEVEMENT:
				score = user.getAchievement();
				break;
			case MEDAL:
				score = user.getMedal();
				break;
		}
		return score;
	}
	
	/**
	 * The field for storing user's specific data.
	 * 
	 * @author wangqi
	 *
	 */
	public static enum Field {
		//Yesterday's string format
		STR_YESTERDAY,
		//Last month's string format.
		STR_LAST_MONTH,
		//Yesterday's user rank
		LAST_DAY_RANK,
		//Last month's user rank
		LAST_MONTH_RANK,
		//Total rank.
		TOTAL_RANK,
	}
	
}
