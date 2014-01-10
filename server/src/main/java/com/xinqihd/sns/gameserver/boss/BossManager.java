package com.xinqihd.sns.gameserver.boss;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Transaction;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.boss.condition.LevelCondition;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.mongo.AbstractMongoManager;
import com.xinqihd.sns.gameserver.db.mongo.DailyLimitManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBossInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseBossList.BseBossList;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is the PVE boss manager
 * 
 * @author wangqi
 *
 */
public class BossManager extends AbstractMongoManager {
	
	//User's current roleaction
	private static final String FIELD_BOSS_LIMIT_KEY = "bosslimit";

	private static final String KEY_PREFIX = "boss:limit:";
	public static final String KEY_BOSS = "boss:id:";
	public static final String KEY_BOSS_SINGLE = "boss:single:";
	public static final String KEY_BOSS_USER = "boss:user:";
	//It indicates whether you take the boss reward
	public static final String FIELD_BOSS_USER_REWARD = "reward";
	public static final String FIELD_BOSS_REWARD_ID = "rewardid";
	//It indicates whether the yuanbao email is sending
	public static final String FIELD_BOSS_USER_RANK = "rank";

	public static final String BOSS_HARDMODE = "boss_hardmode";
	public static final String USER_ROLE_ATTACK = "user_role_attack";
	public static final String USER_ROLE_DEAD = "user_role_dead";
	public static final String USER_BOSS_POJO = "user_boss_pojo";
	public static final String USER_BOSS = "user_boss";
	public static final String USER_TOTAL_HURT = "user_total_hurt";
	public static final String USER_BOSS_ID = "user_boss_id";
	public static final String USER_BEGIN_TIME = "user_begin_time";
	public static final String USER_BOSS_MAP = "user_boss_map";

	private static Logger logger = LoggerFactory.getLogger(BossManager.class);

	private static final String COLL_NAME = "bosses";
	
	private static final String INDEX_NAME = "_id";
	
	private static ConcurrentHashMap<String, BossPojo> dataMap = 
			new ConcurrentHashMap<String, BossPojo>();

	private static BossManager instance = new BossManager();
	
	private DailyLimitManager manager = new DailyLimitManager(KEY_PREFIX);
	
	BossManager() {
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
		for ( DBObject obj : list ) {
			BossPojo boss = (BossPojo)MongoDBUtil.constructObject(obj);
			dataMap.put(boss.getId(), boss);
		}
				
		logger.debug("Load total {} bosses from database.", dataMap.size());
	}
	
	/**
	 * Get the singleton
	 * @return
	 */
	public static BossManager getInstance() {
		return instance;
	}
	
	/**
	 * Add a new boss to the system. It is provided
	 * for GameAdmin to call.
	 * 
	 * @param newBoss
	 */
	public void addBoss(BossPojo newBoss) {
		DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, newBoss.getId());
		MapDBObject dbObject = new MapDBObject();
		dbObject.putAll(newBoss);
		MongoDBUtil.saveToMongo(query, dbObject, databaseName, 
				namespace, COLL_NAME, isSafeWrite);
		logger.debug("Add a new boss {}");
	}
	
	/**
	 * Delete a boss from database
	 * @param boss
	 */
	public void delBoss(BossPojo boss) {
		if ( boss != null ) {
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, boss.getId());
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_NAME, isSafeWrite);
		}
	}
	
	/**
	 * Get all bosses list from gameserver
	 * 
	 * @return
	 */
	public Collection<BossPojo> getAllBosses() {
		return dataMap.values();
	}
	
	/**
	 * Get the bossPojo by id.
	 * @param id
	 * @return
	 */
	public BossPojo getBossPojoById(String id) {
		return dataMap.get(id);
	}
	
	/**
	 * Get all available boss instances.
	 * @param user TODO
	 * @return
	 */
	public Set<Boss> getAllBossInstance(User user) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Set<String> bossKeys = jedisDB.keys(StringUtil.concat(KEY_BOSS, "*"));
		TreeSet<Boss> instances = new TreeSet<Boss>();
		int currentSecond = (int)(System.currentTimeMillis()/1000);
		if ( bossKeys != null && bossKeys.size()>0 ) {
			List<String> bossStrs = jedisDB.mget(bossKeys.toArray(new String[bossKeys.size()]));
			if ( bossStrs != null ) {
				for ( String bossStr : bossStrs ) {
					Boss instance = Boss.fromString(bossStr);
					if ( instance != null ) {
						int beginSecond = instance.getBeginSecond();
						int endSecond = instance.getEndSecond();
						if ( instance.getBossStatusType() == BossStatus.NEW ) {
							if ( beginSecond < currentSecond ) {
								instance.setBossStatusType(BossStatus.PROGRESS);
								String key = StringUtil.concat(KEY_BOSS, instance.getId());
								jedisDB.set(key, instance.toString());
							}
						} else if ( instance.getBossStatusType() == BossStatus.PROGRESS ) {
							if ( endSecond < currentSecond ) {
								instance.setBossStatusType(BossStatus.TIMEOUT);
								String key = StringUtil.concat(KEY_BOSS, instance.getId());
								jedisDB.set(key, instance.toString());
							}
						}
						if ( instance != null ) {
							//Check instance's level
							BossPojo bossPojo = instance.getBossPojo();
							if ( bossPojo != null && bossPojo.getBossType() != BossType.SINGLE ) {
								ArrayList<BossCondition> conds = bossPojo.getRequiredConditions();
								boolean hasLevelCondition = false;
								for ( BossCondition cond : conds ) {
									if ( cond instanceof LevelCondition ) {
										hasLevelCondition = true;
										if ( cond.checkRequiredCondition(user, instance.getBossPojo()) ) {
											instances.add(instance);
										}
									}
								}
								if ( !hasLevelCondition ) {
									instances.add(instance);
								}
							}
						}
					}
				}
			}
		}
		
		//Get all the single bosses
		Collection<BossPojo> allBosses = getAllBosses();
		ArrayList<BossPojo> singleBosses = new ArrayList<BossPojo>(); 
		for ( BossPojo bossPojo : allBosses ) {
			if ( bossPojo.getBossType() == BossType.SINGLE ) {
				singleBosses.add(bossPojo.clone());
			}
		}
		/**
		 * Redis Structure
		 * KEY_BOSS_SINGLE:username
		 *    bossid: bossstr
		 *    bossid: bossstr
		 *    bossid: bossstr
		 */
		String key = getSingleBossKey(user);
		Map<String, String> bossMap = jedisDB.hgetAll(key);
		for ( BossPojo bossPojo : singleBosses) {
			String id = bossPojo.getId();
			String bossStr = bossMap.get(id);
			Boss boss = null;
			if ( bossStr != null ) {
				boss = Boss.fromString(bossStr);
				if ( boss.getEndSecond() < currentSecond ) {
					logger.debug("Boss {} expires for user {}", bossStr, user.getRoleName());
					boss = null;
				}
			}
			if ( boss == null || boss.getProgress() == 0 || boss.getEndSecond() < currentSecond) {
				int expireSecond = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BOSS_SINGLE_EXPIRE, 3600);
				Calendar startCal = Calendar.getInstance();
				Calendar endCal = Calendar.getInstance();
				endCal.add(Calendar.SECOND, expireSecond);
				boss = createBossInstance(bossPojo.getBossId(), bossPojo, startCal, endCal);

				jedisDB.hset(key, id, boss.toString());

				String zsetName = RankManager.getInstance().getSingleBossRankSetName(user, boss.getId());
				jedisDB.del(zsetName);
			}
			/**
			 * 那么刷新副本的设定
			 */
			if ( bossPojo.getCreateBossScript() != null ) {
				ScriptHook hook = ScriptHook.getScriptHook(bossPojo.getCreateBossScript());
				boss = (Boss)ScriptManager.getInstance().runScriptForObject(hook, user, boss);
			}
			
			instances.add(boss);
			
			user.putUserData(BossManager.USER_BOSS_ID, boss);
			
			/**
			 * 检查奖励物品的设置
			 */
			ArrayList<Reward> rewards = retriveBossReward(user, jedisDB, boss, true);
			if ( rewards.size()>0 ) {
				HardMode hardMode = HardMode.simple;
				switch ( boss.getBossPojo().getLevel() ) {
					case 0:
						hardMode = HardMode.simple;
						break;
					case 1:
						hardMode = HardMode.normal;
						break;
					case 2:
						hardMode = HardMode.hard;
						break;
				}
				boss.getBossPojo().addRewards(rewards, hardMode);
			}
		}
		
		Map<String, Boss> map = (Map<String, Boss>)user.getUserData(USER_BOSS_MAP);
		if ( map == null ) {
			map = new HashMap<String, Boss>();
			user.putUserData(USER_BOSS_MAP, map);
		} else {
			map.clear();
		}
		for ( Boss boss : instances ) {
			map.put(boss.getId(), boss);
		}
		return instances;
	}

	/**
	 * @param user
	 * @param jedisDB
	 * @param boss
	 * @return
	 */
	public ArrayList<Reward> retriveBossReward(User user, Jedis jedisDB, Boss boss, boolean needCreate) {
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		if ( boss == null ) {
			return rewards;
		}
		String rewardKey = BossManager.getBossRewardUserKey(user, boss.getId());
		String rewardValue = jedisDB.hget(rewardKey, BossManager.FIELD_BOSS_REWARD_ID);

		if ( rewardValue != null ) {
			String[] rewardStrs = rewardValue.split(Constant.UNDERLINE);
			for ( String rewardStr : rewardStrs ) {
				Reward reward = Reward.fromString(rewardStr);
				rewards.add(reward);
			}
		} else {
			if ( needCreate ) {
				StringBuilder buf = new StringBuilder();
				ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.BOSS_ITEM_REWARD, 
						user, boss, 2);
				if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
					rewards.addAll(result.getResult());
				}
				result = ScriptManager.getInstance().runScript(ScriptHook.BOSS_WEAPON_REWARD, 
						user, boss, 2);
				if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
					rewards.addAll(result.getResult());
				}
				for (Iterator iter = rewards.iterator(); iter.hasNext();) {
					Reward reward = (Reward) iter.next();
					buf.append(reward.toString()).append(Constant.UNDERLINE);
				}
				if ( buf.length() > 0 ) {
					buf.deleteCharAt(buf.length()-1);
					jedisDB.hset(rewardKey, BossManager.FIELD_BOSS_REWARD_ID, buf.toString());
					//1 hour expire
					jedisDB.expire(rewardKey, 3600);
				}
			}
		}
		return rewards;
	}

	/**
	 * @param user
	 * @return
	 */
	private String getSingleBossKey(User user) {
		String key = StringUtil.concat(KEY_BOSS_SINGLE, user.getUsername());
		return key;
	}

	/**
	 * Remove all available boss instances.
	 * @return
	 */
	public int removeAllBossInstance() {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Set<String> bossKeys = jedisDB.keys(StringUtil.concat(KEY_BOSS, "*"));
		Long delCountLong = jedisDB.del(bossKeys.toArray(new String[bossKeys.size()]));
		int deleteCount = 0;
		if ( delCountLong != null ) {
			deleteCount = delCountLong.intValue();
		}
		return deleteCount;
	}
	
	/**
	 * Get given boss instances.
	 * @return
	 */
	public Boss getBossInstance(User user, String instanceId) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String key = StringUtil.concat(KEY_BOSS, instanceId);
		String bossStr = jedisDB.get(key);
		if ( bossStr != null ) {
			Boss instance = Boss.fromString(bossStr);
			return instance;
		}
		/**
		 * 尝试获取单人副本，可从缓冲中过去
		 */
		Map<String, Boss> bossMap = (Map<String, Boss>)user.getUserData(USER_BOSS_MAP);
		Boss boss = null;
		if ( bossMap != null ) {
			boss = bossMap.get(instanceId);
		}
		if ( boss == null ) {
			key = StringUtil.concat(KEY_BOSS_SINGLE, user.getUsername());
			Map<String, String> map = jedisDB.hgetAll(key);
			int currentSecond = (int)(System.currentTimeMillis()/1000);
			bossStr = map.get(instanceId);
			if ( bossStr != null ) {
				boss = Boss.fromString(bossStr);
				if ( boss.getEndSecond() < currentSecond ) {
					logger.debug("Boss {} expires for user {}", bossStr, user.getRoleName());
					boss = null;
				}
			}
			if ( boss != null && (boss.getProgress() == 0 || boss.getEndSecond() < currentSecond)) {
				int expireSecond = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BOSS_SINGLE_EXPIRE, 3600);
				Calendar startCal = Calendar.getInstance();
				Calendar endCal = Calendar.getInstance();
				endCal.add(Calendar.SECOND, expireSecond);
				BossPojo bossPojo = boss.getBossPojo();
				boss = createBossInstance(bossPojo.getBossId(), bossPojo, startCal, endCal);
				jedisDB.hset(key, instanceId, boss.toString());
				String zsetName = RankManager.getInstance().getSingleBossRankSetName(user, boss.getId());
				jedisDB.del(zsetName);
			}
			/**
			 * 那么刷新副本的设定
			 * /
			BossPojo bossPojo = boss.getBossPojo();
			if ( bossPojo.getCreateBossScript() != null ) {
				ScriptHook hook = ScriptHook.getScriptHook(bossPojo.getCreateBossScript());
				boss = (Boss)ScriptManager.getInstance().runScriptForObject(hook, user, boss);
			}
			*/
		}
		
		user.putUserData(BossManager.USER_BOSS_ID, boss);
		
		return boss;
	}
	
	/**
	 * Create a new BossStatus from given BossPojo
	 * @param bossPojo
	 * @return
	 */
	public Boss createBossInstance(String bossId, BossPojo bossPojo, 
			Calendar startCal, Calendar endCal) {
		Boss instance = new Boss();
		instance.setId(bossPojo.getId());
		instance.setBossId(bossId);
		instance.setBossPojo(bossPojo);
		instance.setLimit(bossPojo.getChallengeLimit());
		instance.setIncreasePerHour(bossPojo.getChallengeIncreasePerHour());
		long startMillis = startCal.getTimeInMillis();
		long endMillis = endCal.getTimeInMillis();
		long currentMillis = System.currentTimeMillis();
		instance.setBeginSecond((int)(startMillis/1000));
		instance.setEndSecond((int)(endMillis/1000));
		if ( startMillis > endMillis ) {
			instance.setBossStatusType(BossStatus.TIMEOUT);
		} else if ( startMillis > currentMillis ) {
			instance.setBossStatusType(BossStatus.NEW);
		} else if ( startMillis <= currentMillis && currentMillis < endMillis ) {
			instance.setBossStatusType(BossStatus.PROGRESS);
		} else if ( currentMillis > endMillis ) {
			instance.setBossStatusType(BossStatus.TIMEOUT);
		}

		BossWinType winType = bossPojo.getBossWinType();
		switch ( winType ) {
			case KILL_ONE:
				instance.setProgress(0);
				instance.setTotalProgress(bossPojo.getBlood());
				break;
			case KILL_MANY:
				instance.setProgress(0);
				instance.setTotalProgress(bossPojo.getTotalBosses());
				break;
			case COLLECT_DIAMOND:
				instance.setProgress(0);
				instance.setTotalProgress(bossPojo.getTotalBosses());
				break;
		}
		return instance;
	}
	
	/**
	 * Convert the Boss object to ai controlled user.
	 * @return
	 */
	public User[] convertToBossUsers(BossPojo bossPojo, Boss boss, User realUser) {
		if ( bossPojo == null ) return null;
		ScriptHook hook = null;
		if ( bossPojo.getCreateUserScript() != null ) {
			hook = ScriptHook.getScriptHook(bossPojo.getCreateUserScript());
		} else {
			hook = ScriptHook.CREATE_BOSS_USER;
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				hook, bossPojo, boss, realUser);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			List list = result.getResult();
			User[] bossUsers = (User[])list.get(0);
			return bossUsers;
		}
		return null;
	}

	/**
	 * The user starts to challenge the boss
	 * @param user
	 * @param boss
	 */
	public boolean challengeBoss(User user, Boss boss, long currentTimeMillis) {
		//Check boss status
		boolean success = true;
		BossPojo bossPojo = boss.getBossPojo();
		/**
		 * 单人副本没有体力不允许挑战
		 * 2013-02-03
		 */
		if ( bossPojo.getBossType() == BossType.SINGLE ) {
			if ( !RoleActionManager.getInstance().checkUserHasRoleActionPoint(user) ) {
				SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.challenge.noaction", Type.NORMAL);
				return false;
			}
		}
		success = checkBossStatus(user, boss);
		if ( success ) {
			//Remove reward key
			user.putUserData(boss.getId(), null);
//			String key = getBossRewardUserKey(user, boss.getId());
//			Jedis jedisDB = JedisFactory.getJedisDB();
//			jedisDB.del(key);
			
			int limit = bossPojo.getChallengeLimit();
			int increasePerHour = bossPojo.getChallengeIncreasePerHour();
			
			success = manager.consumeOpportunity(user, boss.getId(), 
					1, limit, increasePerHour, currentTimeMillis);
			if ( !success ) {
				logger.debug("no challenge count for given user {}", user.getRoleName());
				String info = Text.text("boss.nochall", increasePerHour);
				SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), info, 5000);
			}
		}
		return success;
	}

	/**
	 * Check if the boss is valid for challenge
	 * @param user
	 * @param boss
	 */
	public boolean checkBossStatus(User user, Boss boss) {
		if ( boss.getBossStatusType() == BossStatus.NEW ) {
			SysMessageManager.getInstance().sendClientInfoMessage(user, 
					"boss.challenge.new", Action.NOOP, new Object[]{boss.getBossPojo().getName()});
			return false;
		} else if ( boss.getBossStatusType() == BossStatus.TIMEOUT ) {
			SysMessageManager.getInstance().sendClientInfoMessage(user, 
					"boss.challenge.timeout", Action.NOOP, new Object[]{boss.getBossPojo().getName()});
			return false;
		} else if ( boss.getBossStatusType() == BossStatus.SUCCESS ) {
			SysMessageManager.getInstance().sendClientInfoMessage(user, 
					"boss.challenge.success", Action.NOOP, new Object[]{boss.getBossPojo().getName()});
			return false;
		}
		return true;
	}
	
	/**
	 * Take the reward of the boss.
	 * @param user
	 * @param boss
	 * @return
	 */
	public final boolean takeBossReward(User user, Boss boss) {
		boolean success = false;
		//check if the user challenge the boss
		BossPojo bossPojo = boss.getBossPojo();
		if ( bossPojo.getBossType() == BossType.WORLD ) {
			if ( boss.getBossStatusType() == BossStatus.SUCCESS ) {
				//48 hours
				int expireSecond = 172800;
				RankUser myRankUser = RankManager.getInstance().queryUserCurrentRank(
						user, RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, boss.getId());
				if ( myRankUser != null ) {
					Jedis jedisDB = JedisFactory.getJedisDB();
					String key = getBossRewardUserKey(user, boss.getId());
					String rewardTaken = jedisDB.hget(key, BossManager.FIELD_BOSS_USER_REWARD);
					if ( Constant.ONE.equals(rewardTaken) ) {
						SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.reward.taken", Type.NORMAL);
						success = false;
					} else {
						SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.reward.take", Type.NORMAL);

						ArrayList<Reward> rewards = boss.getBossPojo().getRewards();
						RewardManager.getInstance().pickReward(user, rewards, StatAction.PVETakeReward);

						jedisDB.hset(key, FIELD_BOSS_USER_REWARD, Constant.ONE);
						jedisDB.expire(key, expireSecond);
						success = true;
						
						//Sync status
						GameContext.getInstance().writeResponse(user.getSessionKey(), 
								boss.toXinqiBossSync(user, true));
					}
				} else {
					SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.reward.notwin", Type.NORMAL);
					StatClient.getIntance().sendDataToStatServer(user, StatAction.PVETakeNoRank, boss.getId());
					success = false;
				}
			} else {
				SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.reward.notsuccess", Type.NORMAL);
				StatClient.getIntance().sendDataToStatServer(user, StatAction.PVETakeNoWin, boss.getId());
				success = false;
			}
		} else {
			//First check the user's roleaction point
			if ( !RoleActionManager.getInstance().checkUserHasRoleActionPoint(user) ) {
				SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.reward.noaction", Type.NORMAL);
				return false;
			}
			//单人副本领取经验后，重新创建下一等级BOSS
//			Jedis jedisDB = JedisFactory.getJedisDB();
//			String key = getBossRewardUserKey(user, boss.getId());
//			String rewardTaken = jedisDB.hget(key, BossManager.FIELD_BOSS_USER_REWARD);
			Object rewardTaken = user.getUserData(boss.getId());
			if ( boss.getId().equals(rewardTaken) ) {
				SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.reward.taken", Type.NORMAL);
				success = false;
			} else {
//				jedisDB.hset(key, FIELD_BOSS_USER_REWARD, Constant.ONE);
//				jedisDB.expire(key, 10);
				success = true;
				
				//SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.reward.take", Type.NORMAL);

				Jedis jedisDB = JedisFactory.getJedisDB();
				ArrayList<Reward> rewards = retriveBossReward(user, jedisDB, boss, true);
				RewardManager.getInstance().displayRewardCards(user, rewards, boss.getId());

				success = true;
				user.putUserData(boss.getId(), boss.getId());
				//Sync status
				GameContext.getInstance().writeResponse(user.getSessionKey(), 
						boss.toXinqiBossSync(user, true));
			}
		}
		
		if ( bossPojo.getCreateBossScript() != null ) {
			boss.setProgress(0);
			ScriptHook hook = ScriptHook.getScriptHook(bossPojo.getCreateBossScript());
			boss = (Boss)ScriptManager.getInstance().runScriptForObject(hook, user, boss);
			Jedis jedisDB = JedisFactory.getJedisDB();

			String key = getBossRewardUserKey(user, boss.getId());
			jedisDB.hset(key, boss.getId(), boss.toString());
			//清除上一次的排名数据
			String zsetName = RankManager.getInstance().getSingleBossRankSetName(user, boss.getId());
			jedisDB.del(zsetName);
		}
		Set<Boss> bosses = BossManager.getInstance().getAllBossInstance(user);
		if ( bosses.size() > 0 ) {
			BseBossList.Builder bseBossList = BseBossList.newBuilder();
			for ( Boss b : bosses ) {
				XinqiBossInfo.BossInfo bossInfo = b.toXinqiBossInfo(user);
				if ( bossInfo != null ) {
					bseBossList.addBosslist(bossInfo);
				}
			}
			GameContext.getInstance().writeResponse(user.getSessionKey(), bseBossList.build());
		}
		return success;
	}

	/**
	 * @param user
	 * @return
	 */
	public static final String getBossRewardUserKey(BasicUser user, String bossId) {
		return StringUtil.concat(KEY_BOSS_USER, bossId, Constant.COLON, user.getUsername());
	}

	/**
	 * Check if the user meets the boss required conditions
	 * @param user
	 * @param bossPojo
	 * @return
	 */
	public final boolean checkBossRequirement(User user, BossPojo bossPojo) {
		boolean success = false;
		ArrayList<BossCondition> conditions = bossPojo.getRequiredConditions();
		if ( conditions != null ) {
			for ( BossCondition cond : conditions ) {
				success = cond.checkRequiredCondition(user, bossPojo);
				if ( !success ) {
					String info = cond.getErrorDescForUser(user, bossPojo);
					SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), info, 5000);
					break;
				}
			}
			if ( success ) {
				for ( BossCondition cond : conditions ) {
					cond.startChallenge(user, bossPojo);
				}
			}
		}
		return success;
	}
	
	/**
	 * Add new count for the user.
	 * 
	 * @param user
	 * @param boss
	 * @return 
	 */
	public boolean resetChallengeBossCount(User user, Boss boss, long currentTimeMillis) {
		boolean success = manager.resetDailyOpportunityCount(user, boss.getId(), 
				currentTimeMillis);
		return success;
	}
	
	/**
	 * Get current available challenge count.
	 * @param user
	 * @param boss
	 * @param currentTimeMillis
	 * @return
	 */
	public int getChallengeCount(User user, Boss boss, long currentTimeMillis) {
		int limit = boss.getLimit();
		int increasePerHour = boss.getIncreasePerHour();
		int count = limit - manager.getCurrentOpportunity(
				user, boss.getId(), limit, increasePerHour, currentTimeMillis);
		if ( count < 0 ) count = 0;
		return count;
	}
	
	/**
	 * Save a new boss instance
	 * @param instance
	 */
	public void saveBossInstance(Boss instance) {
		if ( instance != null ) {
			int endSecond = instance.getEndSecond();
			int currentSecond = (int)(System.currentTimeMillis()/1000);
			int expireSecond = endSecond - currentSecond + 1800;
			String str = instance.toString();
			if ( expireSecond > 0 ) {
				Jedis jedisDB = JedisFactory.getJedisDB();
				String key = StringUtil.concat(KEY_BOSS, instance.getId());
				jedisDB.set(key, str);
				jedisDB.expire(key, expireSecond);
				logger.debug("BossInstance {} is added to redis", str);
			} else {
				logger.debug("BossInstance {} already expired {}", str, expireSecond);
			}
		}
	}
	
	/**
	 * Update an existing boss instance
	 * @param instance
	 */
	public void updateBossInstance(Boss instance) {
		if ( instance != null ) {
			int endSecond = instance.getEndSecond();
			int currentSecond = (int)(System.currentTimeMillis()/1000);
			int expireSecond = endSecond - currentSecond;
			String str = instance.toString();
			if ( expireSecond > 0 ) {
				Jedis jedisDB = JedisFactory.getJedisDB();
				String key = StringUtil.concat(KEY_BOSS, instance.getId());
				if ( jedisDB.exists(key) ) {
					jedisDB.set(key, instance.toString());
					jedisDB.expire(key, expireSecond);
				} else {
					logger.debug("BossInstance {} doesnot exist in redis", str);
				}
			} else {
				logger.debug("Boss {} already expired {}", str, expireSecond);
			}
		}
	}
	
	/**
	 * Delete the boss instance.
	 * @param instance
	 */
	public void deleteBossInstance(Boss instance) {
		if ( instance != null ) {
			Jedis jedisDB = JedisFactory.getJedisDB();
			String str = instance.toString();
			String key = StringUtil.concat(KEY_BOSS, instance.getId());
			jedisDB.del(key);
			logger.debug("Remove BossInstance {} from redis", str);
		}
	}
	
	/**
	 * Sync the total progress of a boss instance after
	 * the battle is over.
	 * 
	 * @param instance
	 * @param addProgress The added progress
	 * @param addTotalUsers The added totalUsers
	 */
	public final Boss syncBossInstance(User user, String bossId, 
			int addProgress, int addTotalUsers) throws Exception {
				
		if ( StringUtil.checkNotEmpty(bossId) ) {
			String key = StringUtil.concat(KEY_BOSS, bossId);
			/**
			 * Check if the boss has been beaten
			 * wangqi 2012-11-29
			 */
			Jedis jedisDB = JedisFactory.getJedisDB();
			String bossStr = jedisDB.get(key);
			Boss boss = Boss.fromString(bossStr);
			if ( boss.getBossStatusType() == BossStatus.SUCCESS ) {
				logger.debug("Boss {} has been beaten", boss.getBossPojo().getTitle());
				/**
				 * Check if the user reach the top 10 rank.
				 */
				boss = storeProgressInRedis(addProgress, addTotalUsers, key, jedisDB,
						boss);
				RankUser myRankUser = RankManager.getInstance().queryUserCurrentRank(
						user, RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, bossId);
				int rank = -1;
				if ( myRankUser != null ) {
					rank = myRankUser.getRank();
				}
				if ( myRankUser.getRank() <= 11 ) {
					ArrayList<RankUser> rankUsers = new ArrayList<RankUser>();
					rankUsers.add(myRankUser);
					ScriptManager.getInstance().runScript(ScriptHook.BOSS_SEND_RANKING_REWARD, 
							new Object[]{boss, myRankUser});
				}
				StatClient.getIntance().sendDataToStatServer(user, StatAction.BossSync, bossId, 
						boss.getBossStatusType(), rank, addProgress, addTotalUsers);
			} else if ( boss.getBossStatusType() == BossStatus.TIMEOUT ) {
				logger.debug("Boss {} has been timeout", boss.getBossPojo().getTitle());
				String bossTimeoutMessage = Text.text("boss.challenge.timeout", boss.getBossPojo().getName());
				SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), bossTimeoutMessage, 3000);
				
				StatClient.getIntance().sendDataToStatServer(user, StatAction.BossSync, bossId, 
						boss.getBossStatusType(), -1, addProgress, addTotalUsers);
			} else if ( boss.getBossStatusType() == BossStatus.PROGRESS ) {
				boss = storeProgressInRedis(addProgress, addTotalUsers, key, jedisDB,
						boss);
				
				boolean success = true;
				if ( boss.getBossStatusType() == BossStatus.SUCCESS ) {
					//Notify the boss beaten message
					sendBossWinMessageToTop10(boss);
					StatClient.getIntance().sendDataToStatServer(user, StatAction.BossBeaten, 
							boss.getId(), boss.getBossPojo().getTitle());
					
					UserActionManager.getInstance().addUserAction(user.getRoleName(), 
							UserActionKey.BossBeaten);
				} else if ( boss.getBossStatusType() == BossStatus.TIMEOUT ) {
					//Notify the boss win message
					String bossTimeoutMessage = Text.text("boss.challenge.timeout", boss.getBossPojo().getName());
					ChatManager.getInstance().processChatToWorldAsyn(null, bossTimeoutMessage);
					success = false;
				}
				RankUser myRankUser = RankManager.getInstance().queryUserCurrentRank(
						user, RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, bossId);
				int rank = -1;
				if ( myRankUser != null ) {
					rank = myRankUser.getRank();
				}
				StatClient.getIntance().sendDataToStatServer(user, StatAction.BossSync, bossId, 
						boss.getBossStatusType(), rank, addProgress, addTotalUsers, success);
				
				//Sync status
				GameContext.getInstance().writeResponse(user.getSessionKey(), 
						boss.toXinqiBossSync(user, false));
			}
						
			return boss;
		} else {
			logger.warn("syncBossInstance the bossId is null");
		}
		return null;
	}
	
	/**
	 * 
	 * @param user
	 * @param bossId
	 * @param addProgress
	 * @param addTotalUsers
	 * @return
	 * @throws Exception
	 */
	public final Boss syncSingleBossInstance(User user, String bossId, 
			int addProgress, boolean winner) throws Exception {
				
		if ( StringUtil.checkNotEmpty(bossId) ) {
			String key = getSingleBossKey(user);
			/**
			 * Check if the boss has been beaten
			 * wangqi 2012-11-29
			 */
			Jedis jedisDB = JedisFactory.getJedisDB();
			String bossStr = jedisDB.hget(key, bossId);
			Boss boss = Boss.fromString(bossStr);
			if ( boss.getBossStatusType() == BossStatus.SUCCESS ) {
				logger.debug("Boss {} has been beaten", boss.getBossPojo().getTitle());
				/**
				 * Check if the user reach the top 10 rank.
				 */
				boss = storeSingleProgressInRedis(addProgress, key, jedisDB, boss, winner);
				StatClient.getIntance().sendDataToStatServer(user, StatAction.BossSingleSync, bossId, 
						boss.getBossStatusType(), addProgress);
			} else if ( boss.getBossStatusType() == BossStatus.TIMEOUT ) {
				logger.debug("Boss {} has been timeout", boss.getBossPojo().getTitle());
				String bossTimeoutMessage = Text.text("boss.challenge.timeout", boss.getBossPojo().getName());
				SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), bossTimeoutMessage, 3000);
				
				StatClient.getIntance().sendDataToStatServer(user, StatAction.BossSingleSync, bossId, 
						boss.getBossStatusType(), addProgress);
			} else if ( boss.getBossStatusType() == BossStatus.PROGRESS ) {
				boss = storeSingleProgressInRedis(addProgress, key, jedisDB, boss, winner);
								
				boolean success = true;
				if ( boss.getBossStatusType() == BossStatus.SUCCESS ) {
					StatClient.getIntance().sendDataToStatServer(user, StatAction.BossBeaten, 
							boss.getId(), boss.getBossPojo().getTitle());
					
					UserActionManager.getInstance().addUserAction(user.getRoleName(), 
							UserActionKey.BossBeaten);
				} else if ( boss.getBossStatusType() == BossStatus.TIMEOUT ) {
					success = false;
				}
				StatClient.getIntance().sendDataToStatServer(user, StatAction.BossSingleSync, bossId, 
						boss.getBossStatusType(), addProgress, success);
				
				//Sync status
				GameContext.getInstance().writeResponse(user.getSessionKey(), 
						boss.toXinqiBossSync(user, false));
			}
						
			return boss;
		} else {
			logger.warn("syncBossInstance the bossId is null");
		}
		return null;
	}

	/**
	 * @param addProgress
	 * @param addTotalUsers
	 * @param key
	 * @param jedisDB
	 * @param boss
	 * @return
	 */
	private Boss storeProgressInRedis(int addProgress, int addTotalUsers,
			String key, Jedis jedisDB, Boss boss) {
		boolean success = false;
		while ( !success ) {
			jedisDB.watch(key);
			String oldInstance = jedisDB.get(key);
			boss = Boss.fromString(oldInstance);
			int newProgress = boss.getProgress()+addProgress;
			if ( newProgress > boss.getTotalProgress() ) {
				newProgress = boss.getTotalProgress();
			}
			int newTotalUsers = boss.getTotalUsers()+addTotalUsers;
			boss.setProgress(newProgress);
			boss.setTotalUsers(newTotalUsers);
			if ( newProgress >= boss.getTotalProgress() ) {
				boss.setBossStatusType(BossStatus.SUCCESS);
				logger.debug("The boss is killed.");
			} else {
				int currentSeconds = (int)(System.currentTimeMillis()/1000);
				if ( currentSeconds > boss.getEndSecond() ) {
					logger.debug("The boss is timedout");
					boss.setBossStatusType(BossStatus.TIMEOUT);
				}
			}
			//System.in.read();
			//sync boss info
			Transaction trans = jedisDB.multi();
			trans.set(key, boss.toString());
			List<Object> status = trans.exec();
			if ( status != null && status.size()>=1) {
				success = true;
				//Reset the expire seconds
				int endSecond = boss.getEndSecond();
				int currentSecond = (int)(System.currentTimeMillis()/1000);
				int expireSecond = endSecond - currentSecond + 1800;
				jedisDB.expire(key, expireSecond);
				break;
			} else {
				success = false;
			}
		}//while...
		return boss;
	}
	
	/**
	 * 保存单人副本的进度
	 * @param addProgress
	 * @param addTotalUsers
	 * @param key
	 * @param jedisDB
	 * @param boss
	 * @return
	 */
	private Boss storeSingleProgressInRedis(int addProgress,
			String key, Jedis jedisDB, Boss boss, boolean winner) {
		boolean success = false;
		while ( !success ) {
			jedisDB.watch(key);
			String oldInstance = jedisDB.hget(key, boss.getId());
			boss = Boss.fromString(oldInstance);
			int newProgress = boss.getProgress();
			if ( winner ) {
				newProgress += addProgress;
				if ( newProgress > boss.getTotalProgress() ) {
					newProgress = boss.getTotalProgress();
				}
				boss.setProgress(newProgress);
			}
			if (newProgress <= 0) {
				return boss;
			}
			boss.setTotalUsers(1);
			if ( newProgress >= boss.getTotalProgress() ) {
				boss.setBossStatusType(BossStatus.SUCCESS);
				logger.debug("The boss is killed.");
			} else {
				int currentSeconds = (int)(System.currentTimeMillis()/1000);
				if ( currentSeconds > boss.getEndSecond() ) {
					logger.debug("The boss is timedout");
					boss.setBossStatusType(BossStatus.TIMEOUT);
				} else {
					if ( !winner ) {
						/**
						 * 单人副本如果失败了，则副本的闯关终止
						 */
						boss.setBossStatusType(BossStatus.SUCCESS);		
					}
				}
			}
			//System.in.read();
			//sync boss info
			Transaction trans = jedisDB.multi();
			trans.hset(key, boss.getId(), boss.toString());
			List<Object> status = trans.exec();
			if ( status != null && status.size()>=1) {
				success = true;
				//Reset the expire seconds
				int endSecond = boss.getEndSecond();
				int currentSecond = (int)(System.currentTimeMillis()/1000);
				int expireSecond = endSecond - currentSecond + 86400;
				jedisDB.expire(key, expireSecond);
				break;
			} else {
				success = false;
			}
		}//while...
		return boss;
	}

	/**
	 * Get the boss win message.
	 * @param boss
	 * @return
	 */
	public final synchronized void sendBossWinMessageToTop10(Boss boss) {
	  //Notify the boss beaten message
		String rankId = boss.getId();
		Collection<RankUser> rankUsers = RankManager.getInstance().getPVERankUser(rankId, 10);
		
		//Call script
		ScriptManager.getInstance().runScript(ScriptHook.BOSS_SEND_RANKING_REWARD, 
				new Object[]{boss, rankUsers});
	}
		
}
