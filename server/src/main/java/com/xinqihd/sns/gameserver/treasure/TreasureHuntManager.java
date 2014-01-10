package com.xinqihd.sns.gameserver.treasure;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmCallback;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseTreasureHuntBuy.BseTreasureHuntBuy;
import com.xinqihd.sns.gameserver.proto.XinqiBseTreasureHuntQuery.BseTreasureHuntQuery;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
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
 * Every user will have an chance to call the Caishen.  
 * Caishen can give user a lot of golden everyday.
 * 
 * @author wangqi
 * 
 */
public class TreasureHuntManager {
	
	/**
	 * The key in user's userdata that store the user selected hunting mode
	 */
	public static final String USER_DATA_HUNT_FREE = "huntFree";
	
	/**
	 * The collection of gifts that treasure hunting will reward.
	 */
	public static final String USER_DATA_HUNT_TREASURE = "huntTreasure";

	private static final Logger logger = LoggerFactory
			.getLogger(TreasureHuntManager.class);

	private static TreasureHuntManager instance = new TreasureHuntManager();
	
	//The key's prefix used in Redis
	private static final String KEY_PREFIX = "treasure:";
	
	//The rewards separated by '|' that all rewards are refreshed.
	private static final String FIELD_HUNT_KEY = "hunt";

	//The last time in millis that the user do treasure hunting.
	private static final String FIELD_LASTTIME_KEY = "lasttime";
	
	//The refresh count that user have used.
	private static final String FIELD_REFRESH_KEY = "refresh";

	//当日的时间做KEY
	private static final String FIELD_TODAY_STR_KEY = "todaystr";

	//当天剩余的免费寻宝次数
	private static final String FIELD_TODAY_FREE_KEY = "todaytotal";
	

	TreasureHuntManager() {
	}
	
	public static final TreasureHuntManager getInstance() {
		return instance;
	}
	
	/**
	 * 查询当前的信息列表
	 * 
	 * @param user
	 * @param currentTimeMillis
	 * @param refresh TODO
	 */
	public void queryTreasureHuntInfo(User user, long currentTimeMillis, boolean refresh) {
		final String userName = user.getUsername();
		final Jedis jedis = JedisFactory.getJedisDB();
		final String keyName = getRedisLimitKeyName(userName);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		
		List<String> list = jedis.hmget(keyName, FIELD_TODAY_STR_KEY, 
				FIELD_TODAY_FREE_KEY, FIELD_LASTTIME_KEY, FIELD_HUNT_KEY, FIELD_REFRESH_KEY);
		final String dbTodayStr = list.get(0);
		final String dbTodayFreeCount = list.get(1);
		final String dbLastStr = list.get(2);
		final String dbHuntKey = list.get(3);
		final String dbRefreshStr = list.get(4);
		//判断日期值是否有效
		boolean isTodayValid = todayStr.equals(dbTodayStr);

	  //当日可用的寻宝次数
		int freeCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TREASURE_HUNT_FREE_COUNT, 5);
		final int freeRefreshMax = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TREASURE_HUNT_REFRESH_COUNT, 5);

		HashMap<Integer, TreasurePojo> treasures = null;
		boolean sendBse = true;
		
		if ( isTodayValid ) {
			freeCount = StringUtil.toInt(dbTodayFreeCount, freeCount);
			if ( !refresh ) {
				/**
				 * User do not want to refresh. But we should find the last records
				 */
				treasures = (HashMap<Integer, TreasurePojo>)user.getUserData(USER_DATA_HUNT_TREASURE);
				if ( treasures == null ) {
					/**
					 * User may logout. Find the records from Redis
					 */
					ArrayList<Reward> propDatas = new ArrayList<Reward>(24);
					if ( StringUtil.checkNotEmpty(dbHuntKey) ) {
						String[] rewardStrs = dbHuntKey.split(Constant.UNDERLINE);
						for ( String rewardStr : rewardStrs ) {
							Reward reward = Reward.fromString(rewardStr);
							propDatas.add(reward);
						}
					}
					ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.TREASURE_HUNT_GEN, user, propDatas);
					if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
						treasures = (HashMap<Integer, TreasurePojo>)result.getResult().get(0);
						user.putUserData(USER_DATA_HUNT_TREASURE,  treasures);
					}
				}
			} else {
				//用数据库中的购买次数值覆盖默认值
				final int refreshCount = StringUtil.toInt(dbRefreshStr, 0);
				long lastHuntMillis = dbLastStr==null? System.currentTimeMillis() : Long.parseLong(dbLastStr);
				int secondInterval = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TREASURE_HUNT_FRESH_MILLIS, 300000);
				final long currentMillis = System.currentTimeMillis();
				long diff = currentMillis - (lastHuntMillis + secondInterval);
				if ( diff>0 ) {
					//寻宝已经超时，可以刷新
					treasures = refreshTreasures(user, jedis, keyName,
							currentMillis, refreshCount);
				} else {
					if ( refreshCount < freeRefreshMax ) {
					  //每日的免费限次之内，可以随时刷新
						treasures = refreshTreasures(user, jedis, keyName,
								currentMillis, refreshCount);
					} else {
					  //使用上一次的寻宝结果
						String message = Text.text("notice.treasure.refresh", freeRefreshMax, -(diff/1000));
						sendBse = false;
						final int finalFreeCount = freeCount;
					  ConfirmManager.getInstance().sendConfirmMessage(user, message, "confirm.treasure.refresh", new ConfirmCallback() {
							
							@Override
							public void callback(User user, int selected) {
								// TODO Auto-generated method stub
								if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
									boolean success = ShopManager.getInstance().payForSomething(user, MoneyType.YUANBAO, 1, 1, null, true);
									if ( success ) {
										HashMap<Integer, TreasurePojo> treasures = refreshTreasures(user, jedis, keyName,
												currentMillis, refreshCount);
										BseTreasureHuntQuery.Builder bse = BseTreasureHuntQuery.newBuilder();
										bse.setCount(finalFreeCount);
										if ( treasures != null ) {
											for ( TreasurePojo treasure : treasures.values() ) {
												bse.addTreasures(treasure.toTreaureHunt(finalFreeCount>0));
											}
										}
										GameContext.getInstance().writeResponse(user.getSessionKey(), bse.build());
									}
								} else {
									//使用上次的抽奖结果
									String[] rewardStrs = dbHuntKey.split(Constant.UNDERLINE);
									ArrayList<PropData> propDatas = new ArrayList<PropData>(rewardStrs.length);
									for ( String rewardStr : rewardStrs ) {
										Reward reward = Reward.fromString(rewardStr);
										if ( reward.getType() == RewardType.ITEM ) {
											PropData propData = RewardManager.getInstance().convertRewardItemToPropData(reward);
											propDatas.add(propData);
										} else {
											PropData propData = RewardManager.getInstance().convertRewardWeaponToPropData(reward, user);
											propDatas.add(propData);
										}
									}
									jedis.hset(keyName, FIELD_REFRESH_KEY, String.valueOf(refreshCount+1));
									ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.TREASURE_HUNT_GEN, user, propDatas);
									if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
										HashMap<Integer, TreasurePojo> treasures = (HashMap<Integer, TreasurePojo>)result.getResult().get(0);
										user.putUserData(USER_DATA_HUNT_TREASURE,  treasures);
										BseTreasureHuntQuery.Builder bse = BseTreasureHuntQuery.newBuilder();
										bse.setCount(finalFreeCount);
										if ( treasures != null ) {
											for ( TreasurePojo treasure : treasures.values() ) {
												bse.addTreasures(treasure.toTreaureHunt(finalFreeCount>0));
											}
										}
										GameContext.getInstance().writeResponse(user.getSessionKey(), bse.build());
									}
								}
							}
						});
					}
				}
			}
		} else {
			//为当日初始化数据库的值
			String rewardString = null;
			ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.TREASURE_HUNT_GEN, user, null);
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				treasures = (HashMap<Integer, TreasurePojo>)result.getResult().get(0);
				rewardString = convertTreasureToString(treasures);
			} else {
				logger.warn("TREASURE_HUNT_GEN script error");
			}
			
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(FIELD_TODAY_STR_KEY, todayStr);
			map.put(FIELD_TODAY_FREE_KEY, String.valueOf(freeCount));
			map.put(FIELD_LASTTIME_KEY, String.valueOf(System.currentTimeMillis()));
			map.put(FIELD_HUNT_KEY, rewardString);
			map.put(FIELD_REFRESH_KEY, Constant.ZERO);
			jedis.hmset(keyName, map);
			jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
					DateUnit.DAILY, Calendar.getInstance()));
			user.putUserData(USER_DATA_HUNT_TREASURE,  treasures);
		}
		user.putUserData(USER_DATA_HUNT_FREE, freeCount);
		
		if ( sendBse ) {
			BseTreasureHuntQuery.Builder bse = BseTreasureHuntQuery.newBuilder();
			bse.setCount(freeCount);
			if ( treasures != null ) {
				for ( TreasurePojo treasure : treasures.values() ) {
					bse.addTreasures(treasure.toTreaureHunt(freeCount>0));
				}
			}
			GameContext.getInstance().writeResponse(user.getSessionKey(), bse.build());
		}
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.TreasureHuntQuery);
	}

	/**
	 * @param user
	 * @param jedis
	 * @param keyName
	 * @param treasures
	 * @param currentMillis
	 * @return
	 */
	private HashMap<Integer, TreasurePojo> refreshTreasures(User user,
			Jedis jedis, String keyName, long currentMillis, int refreshCount) {
		HashMap<Integer, TreasurePojo> treasures = null;
		String rewardString = null;
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.TREASURE_HUNT_GEN, user, null);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			treasures = (HashMap<Integer, TreasurePojo>)result.getResult().get(0);
			user.putUserData(USER_DATA_HUNT_TREASURE,  treasures);
			
			rewardString = convertTreasureToString(treasures);
		} else {
			logger.warn("TREASURE_HUNT_GEN script error");
		}
		
		if ( rewardString != null ) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(FIELD_LASTTIME_KEY, String.valueOf(currentMillis));
			map.put(FIELD_HUNT_KEY, rewardString);
			map.put(FIELD_REFRESH_KEY, String.valueOf(refreshCount+1));
			jedis.hmset(keyName, map);
			jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
					DateUnit.DAILY, Calendar.getInstance()));
		} else {
			logger.warn("The rewardString is null for user {}", user.getRoleName());
		}
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.TreasureHuntQuery);
		
		return treasures;
	}

	/**
	 * @param treasures
	 * @return
	 */
	private String convertTreasureToString(
			HashMap<Integer, TreasurePojo> treasures) {
		String rewardString;
		StringBuilder buf = new StringBuilder(200);
		TreasurePojo treasurePojo = treasures.get(0);
		List<Reward> propDatas = treasurePojo.getGifts();
		for ( Reward reward : propDatas ) {
			buf.append(reward.toString()).append(Constant.UNDERLINE);
		}
		treasurePojo = treasures.get(1);
		propDatas = treasurePojo.getGifts();
		for ( Reward reward : propDatas ) {
			buf.append(reward.toString()).append(Constant.UNDERLINE);
		}
		treasurePojo = treasures.get(2);
		propDatas = treasurePojo.getGifts();
		for ( Reward reward : propDatas ) {
			buf.append(reward.toString()).append(Constant.UNDERLINE);
		}
		rewardString = buf.deleteCharAt(buf.length()-1).toString();
		return rewardString;
	}
	
	/**
	 * Set the user's treasure hunt count to a new value
	 * @param freeCount
	 */
	public final void addFreeTreasureHuntCount(User user, int addFreeCount, 
			long currentTimeMillis) {
		
		String userName = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(userName);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		
		List<String> list = jedis.hmget(keyName, FIELD_TODAY_STR_KEY, 
				FIELD_TODAY_FREE_KEY);
		String dbTodayStr = list.get(0);
		String dbTodayFreeCount = list.get(1);
		//判断日期值是否有效
		boolean isTodayValid = todayStr.equals(dbTodayStr);
		int oldFreeCount = 0;
		if ( isTodayValid ) {
			//用数据库中的购买次数值覆盖默认值
			oldFreeCount = StringUtil.toInt(dbTodayFreeCount, oldFreeCount);
		} else {
			//为当日初始化数据库的值
			oldFreeCount = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.TREASURE_HUNT_FREE_COUNT, 5);
		}
		int freeCount = oldFreeCount + addFreeCount;
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(FIELD_TODAY_STR_KEY, todayStr);
		map.put(FIELD_TODAY_FREE_KEY, String.valueOf(freeCount));
		jedis.hmset(keyName, map);
		jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
				DateUnit.DAILY, Calendar.getInstance()));
		//Keep the user's data in sync.
		user.putUserData(this.USER_DATA_HUNT_FREE, freeCount);
		
		SysMessageManager.getInstance().sendClientInfoRawMessage(
				user.getSessionKey(), Text.text("treasure.hunt.card", freeCount), 5000);
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.TreasureHuntCard,
				oldFreeCount, addFreeCount, freeCount);
	}
	
	/**
	 * Query the user's current treasure hunt count.
	 * @param user
	 * @return
	 */
	public int getCurrentTreasureHuntFreeCount(User user, long currentMillis) {
		String userName = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(userName);
		String todayStr = DateUtil.formatDate(new Date(currentMillis));
		
		List<String> list = jedis.hmget(keyName, FIELD_TODAY_STR_KEY, 
				FIELD_TODAY_FREE_KEY);
		String dbTodayStr = list.get(0);
		String dbTodayFreeCount = list.get(1);
		//判断日期值是否有效
		boolean isTodayValid = todayStr.equals(dbTodayStr);
		int freeCount = 0;
		if ( isTodayValid ) {
			//用数据库中的购买次数值覆盖默认值
			freeCount = StringUtil.toInt(dbTodayFreeCount, freeCount);
		} else {
			//为当日初始化数据库的值
			freeCount = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.TREASURE_HUNT_FREE_COUNT, 5);
		}
		return freeCount;
	}
	
	/**
	 * The user do a treasure hunt.
	 * 
	 * 对应的抽奖模式
	 * 0: 普通寻宝
	 * 1: 高级寻宝
	 * 2: 专家寻宝
	 * 
	 * @param user
	 * @param mode
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean doTreasureHunt(User user, int mode, long currentTimeMillis) {
		boolean success = true;
		Reward reward = null;
		
		String userName = user.getUsername();
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = getRedisLimitKeyName(userName);
		String todayStr = DateUtil.formatDate(new Date(currentTimeMillis));
		Integer freeCountInt = (Integer)user.getUserData(USER_DATA_HUNT_FREE);
		int freeCount = 0;
		if ( freeCountInt != null ) {
			freeCount = freeCountInt.intValue();
		}
		
		HashMap<Integer, TreasurePojo> treasures = 
				(HashMap<Integer, TreasurePojo>)user.getUserData(USER_DATA_HUNT_TREASURE);
		String response = Text.text("treasure.hunt.success");
		int rewardIndex = 0;
		if ( treasures != null ) {
			TreasurePojo treasure = treasures.get(mode);
			if ( treasure != null ) {
				if ( mode != 0 ) {
					String[] message = new String[1];
					boolean canBuyTreasure = VipManager.getInstance().getVipLevelCanBuyTreasureHunt(user);
					if ( canBuyTreasure ) {
						success = ShopManager.getInstance().payForSomething(user, MoneyType.YUANBAO, 
							treasure.getPrice(), 1, message);
						if ( !success ) {
							response = message[0];
						}
					} else {
						success = false;
						response = Text.text("treasure.cannot.buy.advance");
					}
				} else if ( freeCount <= 0 ) {
					String[] message = new String[1];
					logger.debug("User {} has no chance to use free treasure hunting", userName);
					boolean canBuyTreasure = VipManager.getInstance().getVipLevelCanBuyTreasureHunt(user);
					if ( canBuyTreasure ) {
						success = ShopManager.getInstance().payForSomething(user, MoneyType.YUANBAO, 
							treasure.getPrice(), 1, message);
						if ( !success ) {
							response = message[0];
						}
					} else {
						success = false;
						response = Text.text("treasure.cannot.buy");
					}
				}
				if ( success ) {
					ScriptResult result = ScriptManager.getInstance().runScript(
							ScriptHook.TREASURE_HUNT_PICK, user, treasure);
					if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
						reward = (Reward)result.getResult().get(0);
						rewardIndex = (Integer)result.getResult().get(1);
					} else {
					  //给予默认奖励
						reward = RewardManager.getInstance().getRewardGolden(user);
					}
					ArrayList<Reward> rewards = new ArrayList<Reward>();
					rewards.add(reward);
					if ( reward.getType() == RewardType.ITEM || reward.getType() == RewardType.WEAPON ||
							reward.getType() == RewardType.STONE ) {
						if ( user.getBag().getCurrentCount() + 1 >=
								user.getBag().getMaxCount() ) {
							RewardManager.getInstance().sendMailIfBagFull(user, rewards);
							String propName = RewardManager.getInstance().getRewardName(reward);
							StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.TreasureHuntMail, reward.getType(), propName, reward.getPropColor(), reward.getPropIndate(), reward.getPropLevel());
						} else {
							RewardManager.getInstance().pickRewardWithResult(user, rewards, 
									StatAction.TreasureHuntPick);
							UserActionManager.getInstance().addUserAction(user.getRoleName(), 
									UserActionKey.TreasureHuntPick);
						}
					} else {
						RewardManager.getInstance().pickRewardWithResult(user, rewards, 
								StatAction.TreasureHuntPick);
						UserActionManager.getInstance().addUserAction(user.getRoleName(), 
								UserActionKey.TreasureHuntPick);
					}
					TaskManager.getInstance().processUserTasks(user, TaskHook.TREASURE_HUNT);
				}
			} else {
				logger.warn("There is no treasure mode {} in user {} treasures", 
						user.getRoleName(), mode);
				success = false;
			}
		} else {
			response = Text.text("treasure.hunt.fail");
			logger.warn("There is no treasures in user {} data", user.getRoleName());
			success = false;
		}

		BseTreasureHuntBuy.Builder builder = BseTreasureHuntBuy.newBuilder();
		builder.setResponse(response);
		if ( success ) {
			builder.setGift(reward.toGift());
			logger.debug("User {} hunt treasure: {}", userName, reward);
			
			//substract the freeCount
			if ( mode ==0 && freeCount > 0 ) {
				freeCount--;
				user.putUserData(USER_DATA_HUNT_FREE, freeCount);
				jedis.hset(keyName, FIELD_TODAY_FREE_KEY, String.valueOf(freeCount));
				jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
						DateUnit.DAILY, Calendar.getInstance()));
			}
		}
		builder.setRewardid(rewardIndex);
		builder.setCount(freeCount);
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		return success;
	}
	
	/**
	 * Get the Redis "mail" list name for inbox type
	 * @param user
	 * @return
	 */
	public static final String getRedisLimitKeyName(String userName) {
		return StringUtil.concat(KEY_PREFIX, userName);
	}
}
