package com.xinqihd.sns.gameserver.reward;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.DailyMarkPojo;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.RewardLevelPojo;
import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.RewardPojoType;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AbstractMongoManager;
import com.xinqihd.sns.gameserver.db.mongo.DailyMarkManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.GameResourceManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseOnlineReward.BseOnlineReward;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.DateUtil.DateUnit;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Manage the game's reward system.
 * 
 * @author wangqi
 *
 */
public class RewardManager extends AbstractMongoManager {
	
	private static final Logger logger = LoggerFactory.getLogger(RewardManager.class);
	
	public static final List FIELD_LIST = new ArrayList();
	static {
		FIELD_LIST.add(PropDataEnhanceField.ATTACK);
		FIELD_LIST.add(PropDataEnhanceField.DEFEND);
		FIELD_LIST.add(PropDataEnhanceField.AGILITY);
		FIELD_LIST.add(PropDataEnhanceField.LUCKY);
	}
	
	private static final String COLL_NAME = "rewards";
	private static final String COLL_REWARDLEVELS_NAME = "rewardlevels";
	
	private static final String INDEX_NAME = "_id";
	
	private static ConcurrentHashMap<RewardPojoType, TreeSet<RewardPojo>> dataMap = 
			new ConcurrentHashMap<RewardPojoType, TreeSet<RewardPojo>>(); 
	
	private static ConcurrentHashMap<String, RewardLevelPojo> dataLevelMap = 
			new ConcurrentHashMap<String, RewardLevelPojo>();
	
	private static ConcurrentHashMap<Integer, ArrayList<RewardLevelPojo>> minLevelMap = 
			new ConcurrentHashMap<Integer, ArrayList<RewardLevelPojo>>();
	
	private static final int MAX_COUNT = 1000;
	
	private static final String LOGIN_REWARD = "reward:login:";
	
	private static final String DAILY_REWARD = "reward:dailymark:";
	
	private static final String ONLINE_REWARD = "reward:online:";
	
	private static RewardManager instance = new RewardManager();
	
	//The user's login reward's entry default expire seconds is one week.
	private static final int LOGIN_REWARD_EXPIRE = 86400*6;
	
	private static final int DAILY_REWARD_EXPIRE = 86400*35;
	
	private static final int ONLINE_REWARD_EXPIRE = 86400;
	
	private static final Set<EquipType> includeSet = EnumSet.allOf(EquipType.class);
	/*
	static {
		includeSet.remove(EquipType.JEWELRY);
	}
	*/
	
	RewardManager() {
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

		for ( Locale locale : GameResourceManager.getInstance().getAllLocales() ) {
			for ( DBObject obj : list ) {
				RewardPojo rewardPojo = (RewardPojo)MongoDBUtil.constructObject(obj);
				Collection<RewardPojoType> types = rewardPojo.getIncludes();
				for ( RewardPojoType type : types ) {
					TreeSet<RewardPojo> dataSet = dataMap.get(type);
					if ( dataSet == null ) {
						dataSet = new TreeSet<RewardPojo>();
						dataMap.put(type, dataSet);
					}
					dataSet.add(rewardPojo);
				}
			}
		}
		
		list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_REWARDLEVELS_NAME, null);

		for ( DBObject obj : list ) {
			RewardLevelPojo rewardPojo = (RewardLevelPojo)MongoDBUtil.constructObject(obj);
			if ( rewardPojo.isEnabled() ) {
				dataLevelMap.put(rewardPojo.get_id(), rewardPojo);
				ArrayList<RewardLevelPojo> levelList = minLevelMap.get(rewardPojo.getMinLevel());
				if ( levelList == null ) {
					levelList = new ArrayList<RewardLevelPojo>();
					minLevelMap.put(rewardPojo.getMinLevel(), levelList);
				}
				levelList.add(rewardPojo);
			}
		}
						
		logger.debug("Load total {} reward pojos from database.", dataMap.size());
	}
	
	/**
	 * Add a new reward pojo to database.
	 * 
	 * @param newRewardPojo
	 */
	public void addRewardPojo(RewardPojo newRewardPojo) {
		DBObject rewardObj = MongoDBUtil.createMapDBObject(newRewardPojo);
		DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, newRewardPojo.get_id());
		MongoDBUtil.saveToMongo(query, rewardObj, databaseName, namespace, COLL_NAME, isSafeWrite);
		
		Collection<RewardPojoType> types = newRewardPojo.getIncludes();
		for ( RewardPojoType type : types ) {
			TreeSet<RewardPojo> dataSet = dataMap.get(type);
			if ( dataSet == null ) {
				dataSet = new TreeSet<RewardPojo>();
				dataMap.put(type, dataSet);
			}
			dataSet.add(newRewardPojo);
		}
	}
	
	/**
	 * Get the RewardManager instance.
	 * @return
	 */
	public static RewardManager getInstance() {
		return instance;
	}
	
	/**
	 * User picks up battle rewards
	 * @param userSessionKey
	 * @param slot
	 */
	public ArrayList<Reward> generateRewardsFromScript(User user, int slotSize, ScriptHook hook) {		
		ScriptManager scriptManager = ScriptManager.getInstance();
		ScriptResult result = scriptManager.runScript(hook, user, slotSize);
		ArrayList<Reward> rewardList = (ArrayList<Reward>)result.getResult();
		
		return rewardList;
	}
	
	/**
	 * Get the rewardPojo for given id.
	 * @param weaponOrItemId
	 * @return
	 */
	public RewardLevelPojo getRewardLevelPojoByTypeId(String weaponOrItemId) {
		RewardLevelPojo rewardPojo = dataLevelMap.get(StringUtil.concat(weaponOrItemId, "0"));
		return rewardPojo;
	}
	
	/**
	 * Get the given rewardLevelPojo for user level.
	 * @param userLevel
	 * @return
	 */
	public Collection<RewardLevelPojo> getRewardLevelPojoByUserLevel(int userLevel) {
		return minLevelMap.get(userLevel);
	}
	
	/**
	 * Get all the available reward pojo for given pojo type
	 * 
	 * @param user
	 * @return
	 */
	public static TreeSet<RewardPojo> getRewardPojoForType(User user, RewardPojoType pojoType) {
		TreeSet<RewardPojo> dataSet = dataMap.get(pojoType);
		TreeSet<RewardPojo> filteredSet = null;
		boolean available = false;
		if ( dataSet != null ) {
			filteredSet = new TreeSet<RewardPojo>();
			for ( RewardPojo rewardPojo : dataSet ) {
				if ( rewardPojo.getServerIds().size()>0 ) {
					available = true;
				} else {
					available = true;
				}
				if ( available ) {
					long currentTimeMillis = System.currentTimeMillis();
					if ( rewardPojo.getStartMillis() > 0 ) {
						if ( currentTimeMillis >= rewardPojo.getStartMillis() ) {
							available = true;	
						} else {
							available = false;
						}
					} else {
						available = true;
					}
					if ( available ) {
						if ( rewardPojo.getEndMillis() > 0 ) {
							if ( currentTimeMillis < rewardPojo.getEndMillis() ) {
								available = true;	
							} else {
								available = false;
							}
						} else {
							available = true;
						}
					}
				}
				if ( available ) {
					filteredSet.add(rewardPojo);
				}
			}
		}
		return filteredSet;
	}
	
	/**
	 * Generate random rewards for users in combat or after battle.
	 * 
	 * @param user The user object.
	 * @param count The number of rewards need to generated.
	 * @param excludeRewards The RewardType map that should be excluded if not null.
	 * @return
	 */
	public static ArrayList generateRandomRewards(User user, int count, Set excludeRewards) {
		int userLevel = 1; 
		if ( user != null ) {
			userLevel = user.getLevel();
		}
		double q = userLevel/5;
		if ( q<= 0 ) {
			q = 1.0;
		}

		ArrayList finalRewardTypes = new ArrayList(count);
		for ( int i=0; i<count; ) {
			Object[] rewardTypes = MathUtil.randomPick(RewardType.TYPES, 1);
			RewardType rewardType = (RewardType)rewardTypes[0];
			if ( excludeRewards != null && excludeRewards.contains(rewardType) ) {
				if ( i < MAX_COUNT ) {
					continue;
				} else {
					break;
				}
			} else {
				finalRewardTypes.add(rewardType);
				i++;
			}
		}
		
		if ( finalRewardTypes.size() <= 0 ) {
			return finalRewardTypes;
		}
		
		ArrayList rewards = new ArrayList(count);
		
		/**
		 * ---奖品类型---
		 * 经验值：  EXP：0
		 * 金币：    GOLDEN：1
		 * 元宝：    YUANBAO：2
		 * 礼券：    VOUCHER：3
		 * 勋章:     MEDAL：4
		 * 便携道具:  TOOL: 5
		 * 背包道具:  ITEM: 6
		 * 武器：    WEAPON： 7
		 */
		for ( int i=0; i<count; i++ ) {
			RewardType rewardType = (RewardType)finalRewardTypes.get(i);
			if ( rewardType == RewardType.EXP ) {
				Reward reward = getRewardExp(user);
				rewards.add(reward);
			} else if ( rewardType == RewardType.GOLDEN ) {
				rewards.add(getRewardGolden(user));
			} else if ( rewardType == RewardType.YUANBAO ) {
				Reward reward = getRewardYuanbao();
				rewards.add(reward);
			} else if ( rewardType == RewardType.VOUCHER ) {
				Reward reward = getRewardVoucher();
				rewards.add(reward);
			} else if ( rewardType == RewardType.MEDAL ) {
				int[] medal = new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20};
				Reward reward = getRewardMedal(medal);
				rewards.add(reward);
//			} else if ( rewardType == RewardType.TOOL ) {
			} else if ( rewardType == RewardType.ITEM ) {
				//wangqi 2012-5-24
				Reward reward = generateRandomItem(user);
				rewards.add(reward);
			} else if ( rewardType == RewardType.STONE ) {	
				rewards.add(generateRandomStone(user));
			} else if ( rewardType == RewardType.WEAPON ) {
				rewards.add(generateRandomWeapon(user));
			}
		}

		return rewards;
	}

	/**
	 * @param user
	 * @return
	 */
	private static Reward generateRandomItem(User user) {
		Collection items = GameContext.getInstance().getItemManager().getItems();
		Object[] pickedItems = MathUtil.randomPick(items, 1);
		ItemPojo item = (ItemPojo)pickedItems[0];
		Reward reward = null;
		if ( item!=null && item.isCanBeRewarded() ) {
			reward = getRewardItem(item);
		} else {
			reward = generateRandomWeapon(user);
		}
		return reward;
	}

	/**
	 * @param user
	 * @param q
	 * @param rewards
	 */
	public static final Reward generateRandomStone(User user) {
		Collection stones = GameContext.getInstance().getItemManager().getStoneTypes();
		Object[] pickedStones = MathUtil.randomPick(stones, 1);
		String typeId = (String)pickedStones[0];
		//Level 1 to 3 [1, 4)
		int level = MathUtil.nextGaussionInt(1, 4);
		ItemPojo stonePojo = GameContext.getInstance().getItemManager().getItemByTypeIdAndLevel(typeId, level);
		Reward reward = null;
		if ( stonePojo != null && stonePojo.isCanBeRewarded() ) {
			reward = new Reward();
			reward.setPropId(stonePojo.getId());
			reward.setPropLevel(level);
			reward.setPropCount(1);
			reward.setType(RewardType.STONE);
		} else {
			reward = generateRandomWeapon(user);
		}
		return reward;
	}

	/**
	 * @param item
	 * @return
	 */
	public static Reward getRewardItem(ItemPojo item) {
		return getRewardItem(item.getId(), 1);
	}
	
	/**
	 * @param item
	 * @return
	 */
	public static Reward getRewardItem(String itemId, int count) {
		Reward reward = new Reward();
		reward.setPropId(itemId);
		reward.setPropLevel(-1);
		reward.setPropCount(count);
		reward.setType(RewardType.ITEM);
		return reward;
	}

	/**
	 * @param medal
	 * @return
	 */
	public static Reward getRewardMedal(int[] medal) {
		Reward reward = new Reward();
		reward.setPropId("-4");
		reward.setPropLevel(-1);
		reward.setType(RewardType.MEDAL);
		reward.setPropCount(medal[MathUtil.nextGaussionInt(0, medal.length)]);
		return reward;
	}

	/**
	 * @return
	 */
	public static Reward getRewardVoucher() {
		int[] voucher = new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50};
		Reward reward = new Reward();
		reward.setPropId("-2");
		reward.setPropLevel(-1);
		reward.setType(RewardType.VOUCHER);
		reward.setPropCount(voucher[MathUtil.nextGaussionInt(0, voucher.length)]);
		return reward;
	}

	/**
	 * @return
	 */
	public static Reward getRewardYuanbao() {
		int[] yunbao = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.REWARD_YUANBAO_LIST);
		int count = yunbao[MathUtil.nextGaussionInt(0, yunbao.length)];
		return getRewardYuanbao(count);
	}
	
	/**
	 * @return
	 */
	public static Reward getRewardYuanbao(int count) {
		Reward reward = new Reward();
		reward.setPropId("-3");
		reward.setPropLevel(-1);
		reward.setType(RewardType.YUANBAO);
		reward.setPropCount(count);
		return reward;
	}

	/**
	 * @return
	 */
	public static Reward getRewardExp(User user) {
		int standardExp = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.STANDARD_USER_EXP, 100);
		int[] exp = new int[]{standardExp, standardExp*2, standardExp*3, standardExp*4, standardExp*5, standardExp*6};
		Reward reward = new Reward();
		reward.setPropId("-5");
		reward.setPropLevel(-1);
		reward.setType(RewardType.EXP);
		reward.setPropCount(exp[MathUtil.nextGaussionInt(0, exp.length)]);
		return reward;
	}
	
	/**
	 * @return
	 */
	public static Reward getRewardExp(int count) {
		int standardExp = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.STANDARD_USER_EXP, 100);
		Reward reward = new Reward();
		reward.setPropId("-5");
		reward.setPropLevel(-1);
		reward.setType(RewardType.EXP);
		reward.setPropCount(count);
		return reward;
	}

	/**
	 * Generate a random equip for user. The default including set
	 * does not contain Jewery and Weapon.
	 * 
	 * @param user
	 * @return
	 */
	public static final Reward generateRandomWeapon(User user) {
		return generateRandomWeapon(user, includeSet);
	}
	
	/**
	 * Get a random weapon reward for given user.
	 * @param user
	 * @return
	 */
	public static final Reward generateRandomWeapon(User user, 
			Set<EquipType> includeSet) {
		int level = user.getLevel();
		if ( level >= LevelManager.MAX_LEVEL ) {
			level = user.getLevel();
		}
		/**
		 * 如果用户等级在x0-x5之间，掉落本等级装备
		 * 如果用户等级在x6-x7之间，混合下一等级装备
		 * 如果用户等级在x8-x9之间，下一等级装备
		 */
		int value = level % 10;
		if ( value <= 5 ) {
			//nothing
		} else if ( value <= 7 ) {
			if ( MathUtil.nextDouble() < 0.5 ) {
				level += 10;
			}
		} else {
			level += 10;
		}
		int max = 10;
		for ( int i=0; i<max; i++ ) {
			WeaponPojo weapon = null;
			//PVP战斗中掉落精良武器概率
			//3%	3%	3%	5%	5%	10%	10%	10%	15%	15%
			weapon = GameContext.getInstance().getEquipManager().
					getRandomWeaponByGenderAndLevel(user.getGender(), level);
			/*
			if ( user.isVip() ) {
				double[] ratios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.VIP_PVP_GOOD_PROP);
				int vipIndex = user.getViplevel()-1;
				if ( vipIndex <0 ) vipIndex = 0;
				if ( vipIndex >= ratios.length ) vipIndex = ratios.length-1;
				double ratio = ratios[vipIndex];
				double d = MathUtil.nextDouble();
				if ( d<=ratio ) {
					weapon = GameContext.getInstance().getEquipManager().
							getRandomWeaponByQualityAndLevel(level, 2);
				} else {
					weapon = GameContext.getInstance().getEquipManager().
							getRandomWeaponByGenderAndLevel(user.getGender(), level);
				}
			} else {
				/**
				 * Free user should not get quality = 2 weapons here.
				 * /
				weapon = GameContext.getInstance().getEquipManager().
						getRandomWeaponByGenderAndLevel(user.getGender(), level);
			}
			*/
			boolean canBeRewarded = false;
			if ( weapon != null && weapon.isCanBeRewarded() && !weapon.isUsedByBoss() ) {
				if ( includeSet != null ) {
					if ( includeSet.contains(weapon.getSlot()) ) {
						canBeRewarded = true;
					}
				} else {
					canBeRewarded = true;
				}
			}
			if ( canBeRewarded ) {
				/**
				 * 随机设置槽位和最大强化上限
				 */
				int slot = MathUtil.nextGaussionInt(0, 5, 3.0);
				int colorIndex = MathUtil.nextGaussionInt(0, WeaponColor.PINK.ordinal());
				int weaponMaxLevel = MathUtil.nextGaussionInt(5, 16);
				int weaponLevel = MathUtil.nextGaussionInt(0, 9);
				return getWeaponReward(weapon, weaponLevel, colorIndex, false, weaponMaxLevel);
			}
		}
		return getRewardGolden(user);
	}
	
	/**
	 * Convert a weapon object to reward
	 * 
	 * @param weapon
	 * @param level
	 * @param colorIndex
	 * @param typeRelated If true, the weapon type is used.
	 * @return
	 */
	public static final Reward getWeaponReward(WeaponPojo weapon, 
			int level, int colorIndex, boolean typeRelated) {
		int minLevel = Math.max(level, 5);
		int maxLevel = MathUtil.nextGaussionInt(minLevel, 16, 2.0);
		return getWeaponReward(weapon, level, colorIndex, typeRelated, maxLevel);
	}

	/**
	 * Convert a weapon object to reward
	 * 
	 * @param weapon
	 * @param level
	 * @param colorIndex
	 * @param typeRelated If true, the weapon type is used.
	 * @return
	 */
	public static final Reward getWeaponReward(WeaponPojo weapon, 
			int level, int colorIndex, boolean typeRelated, int maxLevel) {
		Reward reward = new Reward();
		reward.setPropId(weapon.getId());
		reward.setPropLevel(level);
		reward.setPropCount(1);
		reward.setType(RewardType.WEAPON);
		reward.setSlot(MathUtil.nextGaussionInt(0, 5, 3.0));
		if ( typeRelated ) {
			reward.setTypeId(weapon.getTypeName());
		}
		WeaponColor weaponColor = WeaponColor.values()[colorIndex];
		reward.setPropColor(weaponColor);
		/**
		 * 普通的装备有非常长的有效期
		 */
		int validTimes = 30;
		int propUsedTimes = 0;
		switch ( weaponColor ) {
			case WHITE:
				validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SOLID, 200);
				propUsedTimes = 0;
				break;
			case GREEN:
				validTimes = 30;
				propUsedTimes = 10;
				break;
			case BLUE:
				validTimes = 30;
				propUsedTimes = 15;
				break;
			case PINK:
				validTimes = 30;
				propUsedTimes = 20;
				break;
			case ORGANCE:
				validTimes = 30;
				propUsedTimes = 20;
				break;
		}
		reward.setPropIndate(validTimes);
		reward.setUsedTimes(propUsedTimes);
		return reward;
	}
	
	/**
	 * Create a new golden reward
	 * @return
	 */
	public static Reward getRewardGolden(User user) {
		float levelRatio = 1.0f;
		if ( user != null ) {
			levelRatio = 1 + user.getLevel() / 30f;
		}
		int[] golden = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.REWARD_GOLDEN_LIST);
		int index = (int)(MathUtil.nextDouble() * golden.length);
		int finalGolden = Math.round(golden[index] * levelRatio);
		
		return getRewardGolden(finalGolden);
	}
	
	/**
	 * Get the golden reward
	 * @param count
	 * @return
	 */
	public static Reward getRewardGolden(int count) {
		Reward reward = new Reward();
		reward.setPropId("-1");
		reward.setPropLevel(-1);
		reward.setType(RewardType.GOLDEN);
		reward.setPropCount(count);
		
		return reward;
	}
	
	/**
	 * When an user login game, he/she can take randomly login reward. 
	 * If user continuously login one day, he can get one reward.
	 * If user continuously login two days, he can get two rewards,
	 * If user continuously login three days, he can get three rewards.
	 * 
	 * Three rewards is the max number users can get.
	 * 
	 * Redis database:
	 * reward:login:<username>:
	 * 	lastday:"2012-02-15"
	 *  today:  "2012-02-16"
	 *  continue: 3
	 *  remaintimes: 1 
	 * 
	 * @param user
	 * @return The remain reward for today.
	 */
	public int processLoginReward(User user, long currentTimeMillis) {
		if ( user == null ) {
			logger.warn("#processLoginReward: null user");
			return 0;
		}
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userLoginRewardKey = getLoginRewardKeyName(user.getUsername());

		String lastDay = jedisDB.hget(userLoginRewardKey, LoginRewardField.lastday.name());
		boolean isNewLogin = false;
		if ( lastDay == null ) {
			logger.debug("It is the first time for user {} to login game.", user.getRoleName());
			isNewLogin = true;
		} else {
			//Get today and yesterday
			String today = DateUtil.getToday(currentTimeMillis);
			String yesterday = DateUtil.getYesterday(currentTimeMillis);
			if ( !today.equals(lastDay) ) {
				if ( yesterday.equals(lastDay) ) {
					//it is the first time user login today
					String continuousStr = jedisDB.hget(
							userLoginRewardKey, LoginRewardField.continuous.name());
					int continuous = StringUtil.toInt(continuousStr, 1) + 1;
					int max = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_ONLINE_REWARD_MAX, 3);
					if ( continuous >= max ) {
						continuous = max;
					}
					Pipeline pipeline = jedisDB.pipelined();
					pipeline.hset(
							userLoginRewardKey, LoginRewardField.continuous.name(), String.valueOf(continuous));
					pipeline.hset(
							userLoginRewardKey, LoginRewardField.remaintimes.name(), String.valueOf(continuous));
					pipeline.hset(
							userLoginRewardKey, LoginRewardField.lastday.name(), today
							);
					pipeline.expire(userLoginRewardKey, LOGIN_REWARD_EXPIRE);
					pipeline.sync();
					user.setRemainLotteryTimes(continuous);
					user.setContinuLoginTimes(continuous);
					if ( logger.isDebugEnabled() ) {
						logger.debug("User {} already logined yesterday. ContinuousTime:{}", 
								new Object[]{user.getRoleName(), continuous});
					}
					isNewLogin = false;
				} else {
					logger.debug("User {} broken his login reward continuous yesterday.", user.getRoleName());
					isNewLogin = true;
				}
			} else {
				logger.debug("User {}'s today login reward is already set. ignore", user.getRoleName());
			}
		}
		
		if ( isNewLogin ) {
			String today = DateUtil.getToday(currentTimeMillis);
			
			//Remove the old data
			jedisDB.del(userLoginRewardKey);
			
			Pipeline pipeline = jedisDB.pipelined();
			pipeline.hset(userLoginRewardKey, LoginRewardField.lastday.name(), today);
			pipeline.hset(userLoginRewardKey, LoginRewardField.continuous.name(), Constant.ONE);
			pipeline.hset(userLoginRewardKey, LoginRewardField.remaintimes.name(), Constant.ONE);
			pipeline.expire(userLoginRewardKey, LOGIN_REWARD_EXPIRE);
			pipeline.sync();
			user.setRemainLotteryTimes(1);
			user.setContinuLoginTimes(1);
		}
		
		return user.getRemainLotteryTimes();
	}
	
	/**
	 * Take today's login reward. The remainTimes will be subtracted 1
	 * 
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean takeLoginReward(User user, long currentTimeMillis) {
		boolean success = false;
		if ( user == null ) {
			logger.warn("#takeLoginReward: null user");
			return success;
		}
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userLoginRewardKey = getLoginRewardKeyName(user.getUsername());
		
		String lastDay = jedisDB.hget(userLoginRewardKey, LoginRewardField.lastday.name());
		//Get today 
		String today = DateUtil.getToday(currentTimeMillis);
		if ( !today.equals(lastDay) ) {
			processLoginReward(user, currentTimeMillis);
		}
		
		String remainTimesStr = jedisDB.hget(
				userLoginRewardKey, LoginRewardField.remaintimes.name());
		int remainTimes = StringUtil.toInt(remainTimesStr, 0);
		if ( remainTimes > 0 ) {
			remainTimes--;
			jedisDB.hset(
					userLoginRewardKey, LoginRewardField.remaintimes.name(), String.valueOf(remainTimes));
			user.setRemainLotteryTimes(remainTimes);
			success = true;
			logger.debug("User {}'s remain login reward times: {}", user.getRoleName(), remainTimes);
		} else {
			logger.debug("User {} has no remain login reward times", user.getRoleName());
			user.setRemainLotteryTimes(0);
			success = false;
		}
		return success;
	}
	
	/**
	 * 
	 * Redis database:
		 hgetall "reward:dailymark:test-001"
			1) "currentmonth"
			2) "2012-02"
			3) "currentdate"
			4) "2012-02-20"
			5) "totalcount"
			6) "6"
			7) "markarray"
			8) "0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1"
	 * 
	 * @param user
	 * @return The remain reward for today.
	 */
	public DailyMarkReward processDailyMarkReward(User user, long currentTimeMillis) {
		if ( user == null ) {
			logger.warn("#processDailyMarkReward: null user");
			return null;
		}
//		String userName = user.getUsername();
		String roleName = user.getRoleName();
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userDailyMarkRewardKey = getDailyMarkRewardKeyName(user.getUsername());

		String actualMonth= jedisDB.hget(userDailyMarkRewardKey, DailyMarkField.currentmonth.name());
		//Get today string
		String currentMonth = DateUtil.getCurrentMonth(currentTimeMillis);
		String today = DateUtil.getToday(currentTimeMillis);
		
		boolean isNewMark = false;
		if ( actualMonth == null ) {
			logger.debug("It is the first time for user {} to daily mark.", roleName);
			isNewMark = true;
		} else {
			if ( !currentMonth.equals(actualMonth) ) {
				logger.debug("It is the beginning of a new month for user {}", roleName);
				isNewMark = true;
			} else {
				isNewMark = false;
				String actualDay = jedisDB.hget(userDailyMarkRewardKey, DailyMarkField.currentdate.name());
				
				DailyMarkReward dailyMarkReward = new DailyMarkReward();
				dailyMarkReward.setCurrentMonth(currentMonth);
				dailyMarkReward.setToday(today);
				dailyMarkReward.setTodayMarked(false);
				
				//obtain mark array
				String markArrayStr = jedisDB.hget(
						userDailyMarkRewardKey, DailyMarkField.markarray.name());
				dailyMarkReward.addMarkArray( DailyMarkReward.toMarkArray(markArrayStr) );
				
				int todayValue = StringUtil.toInt(today.substring(today.length()-2), 1);
				dailyMarkReward.setDayOfMonth(todayValue);
				
				String totalCountStr = jedisDB.hget(
						userDailyMarkRewardKey, DailyMarkField.totalcount.name());
				int totalCount = StringUtil.toInt(totalCountStr, 1);
				dailyMarkReward.setTotalCount(totalCount);
				
				if ( today.equals(actualDay) ) {
					dailyMarkReward.setTodayMarked(true);
				}
				
				DailyMarkPojo dailyMark = DailyMarkManager.getInstance().getDailyMarkByDayNum(totalCount);
				
				if ( dailyMark != null ) {
					dailyMarkReward.setDailyMark(dailyMark);
				}
								
				return dailyMarkReward;
			}
		}
		
		if ( isNewMark ) {
			int actualDayValue = StringUtil.toInt(today.substring(today.length()-2), 1);
			
			//Calculate the expire seconds
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(currentTimeMillis);
			int expireSeconds = DateUtil.getSecondsToNextDateUnit(DateUnit.MONTHLY, cal);
			logger.debug("The user daily mark key {} will expire after {} seconds.", userDailyMarkRewardKey, expireSeconds);
			
			//Delete the old data if exist
			jedisDB.del(userDailyMarkRewardKey);
			
			Pipeline pipeline = jedisDB.pipelined();
			pipeline.hset(userDailyMarkRewardKey, DailyMarkField.currentmonth.name(), currentMonth);
			pipeline.hset(userDailyMarkRewardKey, DailyMarkField.totalcount.name(), Constant.ZERO);
  		//pipeline.hset(userDailyMarkRewardKey, DailyMarkField.currentdate.name(), today);
	  	//pipeline.hset(userDailyMarkRewardKey, DailyMarkField.markarray.name(), buf.toString());
			pipeline.expire(userDailyMarkRewardKey, expireSeconds);
			pipeline.sync();
			
			DailyMarkReward dailyMarkReward = new DailyMarkReward();
			dailyMarkReward.setCurrentMonth(currentMonth);
			dailyMarkReward.setToday(today);
			
			int todayValue = StringUtil.toInt(today.substring(today.length()-2), 1);
			dailyMarkReward.setDayOfMonth(todayValue);
			dailyMarkReward.setTotalCount(0);
			
			return dailyMarkReward;
		}
		
		return null;
	}
	
	/**
	 * Take today's login reward. The remainTimes will be subtracted 1
	 * 
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean takeDailyMarkReward(User user, long currentTimeMillis) {
		boolean success = false;
		if ( user == null ) {
			logger.warn("#takeDailyMarkReward: null user");
			return success;
		}
//		String userName = user.getUsername();
		String roleName = user.getRoleName();
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userDailyMarkRewardKey = getDailyMarkRewardKeyName(user.getUsername());

		String actualDay = jedisDB.hget(userDailyMarkRewardKey, DailyMarkField.currentdate.name());
		
		//Get today string
		String today = DateUtil.getToday(currentTimeMillis);

		DailyMarkReward dailyMark = null;
		if ( !today.equals(actualDay) ) {
			//Need to create the actualMonth fields in Redis
			processDailyMarkReward(user, currentTimeMillis);
			//Mark today
			String actualMonth= jedisDB.hget(userDailyMarkRewardKey, DailyMarkField.currentmonth.name());
			int todayValue = StringUtil.toInt(today.substring(today.length()-2), 1);
			int actualDayValue = 0;
			if ( actualDay != null && actualDay.length() >= 2 ) {
				actualDayValue= StringUtil.toInt(actualDay.substring(actualDay.length()-2), 1);
			}
			int dayDiff = todayValue - actualDayValue;
			//obtain mark array
			String markArrayStr = jedisDB.hget(
					userDailyMarkRewardKey, DailyMarkField.markarray.name());
			StringBuilder buf = new StringBuilder();
			if (markArrayStr != null){
				buf.append(markArrayStr).append(',');
			}
			for ( int i=0; i<dayDiff-1; i++ ) {
				buf.append('0').append(',');
			}
			//Set today's mark
			buf.append('1');
			markArrayStr = buf.toString();
			
			String totalCountStr = jedisDB.hget(
					userDailyMarkRewardKey, DailyMarkField.totalcount.name());
			int totalCount = StringUtil.toInt(totalCountStr, 1)+1;
			
			Pipeline pipeline = jedisDB.pipelined();
			pipeline.hset(
					userDailyMarkRewardKey, DailyMarkField.currentmonth.name(), actualMonth);
			pipeline.hset(
					userDailyMarkRewardKey, DailyMarkField.currentdate.name(), today);
			pipeline.hset(
					userDailyMarkRewardKey, DailyMarkField.markarray.name(), markArrayStr);
			pipeline.hset(
					userDailyMarkRewardKey, DailyMarkField.totalcount.name(), String.valueOf(totalCount));
			pipeline.sync();
			
			logger.debug("User {}'s daily mark record is updated.", roleName);
			
			dailyMark = processDailyMarkReward(user, currentTimeMillis);
			
			if ( dailyMark != null ) {
				DailyMarkPojo markPojo = dailyMark.getDailyMark();
				if ( markPojo != null ) {
					int dayNum = markPojo.getDayNum();
					String key = StringUtil.concat(DailyMarkField.taken.name(), dayNum);
					if ( !jedisDB.exists(key) ) {
						jedisDB.hset(userDailyMarkRewardKey, key, today);
						success = true;
						logger.debug("User {} takes its daily mark reward for dayNum: {}", roleName, dayNum);
					} else {
						success = false;
						logger.debug("User {} has already taken daily mark reward for dayNum: {}", roleName, dayNum);
					}
				} else {
					success = false;
					logger.debug("User {} dayNum {} does not reach the condition", 
							roleName, dailyMark.getTotalCount());
				}
			  //Call tasks
				TaskManager.getInstance().processUserTasks(user, TaskHook.LOGIN_DATE, dailyMark);
			}
		} else {
			logger.debug("User {} has already marked date:{}", roleName, today);
		}
		return success;
	}
	
	/**
	 * 
	 * Redis database:
		 hgetall "reward:dailymark:test-001"
			1) "currentdate"
			2) "2012-02-15"
			3) "stepid"
			4) "0"
			5) "timeclock"
			6) "14:30"
	 * 
	 * @param user
	 * @return The remain reward for today.
	 */
	public OnlineReward processOnlineReward(User user, long currentTimeMillis) {
		if ( user == null ) {
			logger.warn("#processOnlineReward: null user");
			return null;
		}
		String userName = user.getUsername();
		String roleName = user.getRoleName();
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userOnlineRewardKey = getDailyMarkRewardKeyName(userName);

		String currentDate = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.currentdate.name());
		
		String today = DateUtil.getToday(currentTimeMillis);
		
		boolean isNewMark = false;
		if ( !today.equals(currentDate) ) {
			logger.debug("It is the first step for user {} to get online reward", roleName);
			isNewMark = true;
		} else {
			isNewMark = false;
			
			String stepIdStr = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.stepid.name());
			int stepId = StringUtil.toInt(stepIdStr, 0);
			String timeClock = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.timeclock.name());
			int seconds = DateUtil.getSecondsToTimeClock(currentTimeMillis, timeClock);
			String taken = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.taken.name());
			boolean isTaken = false;
			if ( Constant.ONE.equals(taken) ) {
				isTaken = true;
				logger.debug("User {} already taken the rewards for step: {}", roleName, timeClock);
			} else {
				isTaken = false;
				logger.debug("User {} does not take the rewards for step: {}", roleName, timeClock);
			}
			OnlineReward onlineReward = new OnlineReward();
			onlineReward.setStepId(stepId);
			onlineReward.setRemainSeconds(seconds);
			onlineReward.setRewards(user.getOnlineRewards());
			onlineReward.setTaken(isTaken);
			onlineReward.setTimeClock(timeClock);
			
			return onlineReward;
		}
		
		if ( isNewMark ) {
			int seconds = -1;
			String timeClock = null;
			String[] timeclocks = GameDataManager.getInstance().getGameDataAsStringArray(GameDataKey.USER_ONLINE_REWARD_STEP);
			for ( int i = 0; i<timeclocks.length; i++ ) {
				String step = timeclocks[i];
				seconds = DateUtil.getSecondsToTimeClock(currentTimeMillis, step);
				if ( seconds > 0 ) {
					timeClock = step;
					break;
				}
			}
			
			//Delete the old data if exist
			jedisDB.del(userOnlineRewardKey);
			
			if ( seconds <= 0 ) {
				logger.info("All timeclocks in the whole day are finished by user {}", roleName);
				return null;
			}
			
			logger.info("User {} initialize the new timeclock for today at: {}", roleName, timeClock);
						
			Pipeline pipeline = jedisDB.pipelined();
			pipeline.hset(userOnlineRewardKey, OnlineRewardField.currentdate.name(), today);
			pipeline.hset(userOnlineRewardKey, OnlineRewardField.stepid.name(), Constant.ZERO);
			pipeline.hset(userOnlineRewardKey, OnlineRewardField.timeclock.name(), timeClock);
			pipeline.expire(userOnlineRewardKey, ONLINE_REWARD_EXPIRE);
			pipeline.sync();
			
			OnlineReward onlineReward = new OnlineReward();
			onlineReward.setStepId(0);
			onlineReward.setRemainSeconds(seconds);
			onlineReward.setTimeClock(timeClock);
			
			ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.USER_ONLINE_REWARD, user, 0);
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				ArrayList<Reward> rewards = (ArrayList<Reward>)result.getResult();
				user.setOnlineRewards(rewards);
				onlineReward.setRewards(rewards);
			}
			
			return onlineReward;
		}
		
		return null;
	}
	
	/**
	 * User takes the current available online reward.
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean takeOnlineReward(User user, long currentTimeMillis) {
		boolean success = false;
		if ( user == null ) {
			logger.warn("#takeOnlineReward: null user");
			return success;
		}
//		String userName = user.getUsername();
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userOnlineRewardKey = getDailyMarkRewardKeyName(user.getUsername());

		String currentDate = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.currentdate.name());
		String today = DateUtil.getToday(currentTimeMillis);
		
		if ( currentDate == null ) {
			processOnlineReward(user, currentTimeMillis);
			return false;
		} else if ( !today.equals(currentDate) ) {
			processOnlineReward(user, currentTimeMillis);
		}
		//Check again.
		currentDate = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.currentdate.name());
		if ( today.equals(currentDate) ) {
			String stepIdStr = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.stepid.name());
			String taken = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.taken.name());
			String timeClock = jedisDB.hget(userOnlineRewardKey, OnlineRewardField.timeclock.name());
			
			boolean isTaken = false;
			if ( taken != null ) {
				logger.info("User {} already take the reward for timeclock: ", user.getRoleName(), timeClock);
				isTaken = true;
			}
			
			int stepId = StringUtil.toInt(stepIdStr, 0);
			int seconds = DateUtil.getSecondsToTimeClock(currentTimeMillis, timeClock);
			if ( seconds <= 0 ) {
				logger.debug("It is available to take the reward for timeclock:{}", timeClock);
				
				if ( !isTaken ) {
					//Update next timeclock
					String[] timeclocks = GameDataManager.getInstance().getGameDataAsStringArray(GameDataKey.USER_ONLINE_REWARD_STEP);
					timeClock = null;
					for ( int i = 0; i<timeclocks.length; i++ ) {
						String step = timeclocks[i];
						seconds = DateUtil.getSecondsToTimeClock(currentTimeMillis, step);
						if ( seconds > 0 ) {
							timeClock = step;
							break;
						}
					}
					
					//Delete the old data if exist
					jedisDB.del(userOnlineRewardKey);
					if ( seconds <= 0 ) {
						logger.info("User {} already finished all timeclocks in today: {}", user.getRoleName());
					}
					
					Pipeline pipeline = jedisDB.pipelined();
					pipeline.hset(userOnlineRewardKey, OnlineRewardField.currentdate.name(), today);
					pipeline.hset(userOnlineRewardKey, OnlineRewardField.stepid.name(), String.valueOf(stepId+1));
					/**
					 * When the time is after 23:30 and before 0:0, there is no valid next timeclock
					 * so the timeclock will be null. However, we need the 'taken' field to record if
					 * users already take the reward before that time.
					 */
					if ( timeClock != null ) {
						pipeline.hset(userOnlineRewardKey, OnlineRewardField.timeclock.name(), timeClock);
					} else {
						pipeline.hset(userOnlineRewardKey, OnlineRewardField.taken.name(), Constant.ONE);
					}
					pipeline.expire(userOnlineRewardKey, ONLINE_REWARD_EXPIRE);
					pipeline.sync();
					
					//Generate the new rewards for next time clock.
					if ( timeClock != null ) {
						ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.USER_ONLINE_REWARD, user, 0);
						if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
							ArrayList<Reward> rewards = (ArrayList<Reward>)result.getResult();
							user.setOnlineRewards(rewards);
						}
					} else {
						user.setOnlineRewards(null);
					}
										
					success = true;
				}
			} else {
				logger.info("User {} cannot take reward because the timer does not reach: {}", 
						user.getRoleName(), timeClock);
				success = false;
			}
		}
		
		return success;
	}
	
	/**
	 * Clear user login reward data
	 */
	public void clearUserReward(String userName) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String userLoginRewardKey = getLoginRewardKeyName(userName);
		jedisDB.del(userLoginRewardKey);
		String userDailyMarkRewardKey = getDailyMarkRewardKeyName(userName);
		jedisDB.del(userDailyMarkRewardKey);
		String userOnlineRewardKey = getOnlineRewardKeyName(userName);
		jedisDB.del(userOnlineRewardKey);
	}
	
	/**
	 * For backward compitable.
	 * @param user
	 * @param rewards
	 * @return
	 */
	public boolean pickReward(User user, Collection<Reward> rewards, StatAction statAction) {
		PickRewardResult result = pickRewardWithResult(user, rewards, statAction);
		return result == PickRewardResult.SUCCESS;
	}
	
	/**
	 * Pick the reward and return the result.
	 * If the result is something other than SUCCESS, the user's status
	 * will not change.
	 * 
	 * @param user
	 * @param rewards
	 * @return
	 */
	public PickRewardResult pickRewardWithResult(User user, Collection<Reward> rewards, StatAction statAction) {
		PickRewardResult result = PickRewardResult.NOTHING;
		if ( rewards == null ) {
			return PickRewardResult.OTHER;
		}
		//Check the user's bag size
		int totalRewardItem = 0;
		for ( Reward reward : rewards ) {
			if ( reward.getType() == RewardType.ITEM || 
					reward.getType() == RewardType.WEAPON ) {
				totalRewardItem++;
			}
		}
		if ( user.getBag().getCurrentCount() + totalRewardItem >
				user.getBag().getMaxCount() ) {
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					user.getSessionKey(), Text.text("bag.pickreward.full"), Action.NOOP, XinqiSysMessage.Type.NORMAL);
			return PickRewardResult.BAG_FULL;
		}
		
		boolean bagChanged = false;
		for ( Reward reward : rewards ) {
			int count = reward.getPropCount();
			if (count<=0) { 
				count = 1;
			}
			switch ( reward.getType() ) {
				case EXP:
					user.setExp(user.getExp()+ reward.getPropCount());
					result = PickRewardResult.SUCCESS;
					
					StatClient.getIntance().sendDataToStatServer(user, 
							statAction, "Exp", reward.getPropCount(),
							Constant.EMPTY, Constant.EMPTY, Constant.EMPTY);
					
					String message = Text.text("pickreward.exp", reward.getPropCount());
					SysMessageManager.getInstance().sendClientInfoRawMessage(user, message,
							Action.NOOP,
							XinqiSysMessage.Type.CONFIRM);
					break;
				case GOLDEN:
					user.setGolden(user.getGolden() + reward.getPropCount());
					result = PickRewardResult.SUCCESS;
					
					StatClient.getIntance().sendDataToStatServer(user, 
							statAction, MoneyType.GOLDEN, reward.getPropCount(),
							Constant.EMPTY, Constant.EMPTY, Constant.EMPTY);
					
					message = Text.text("pickreward.golden", reward.getPropCount());
					SysMessageManager.getInstance().sendClientInfoRawMessage(user, message,
							Action.NOOP,
							XinqiSysMessage.Type.CONFIRM);
					break;
				case STONE:
					/**
					 * For stone, the propId is actually the typeId
					 * and the level is the stone's level
					 */
					String propId = reward.getPropId();
					ItemPojo stonePojo = GameContext.getInstance().getItemManager().getItemById(propId);
					if ( stonePojo != null ) {
						PropData propData = stonePojo.toPropData(count); 
						boolean success = user.getBag().addOtherPropDatas(propData);
						if ( success ) {
							bagChanged = true;
							result = PickRewardResult.SUCCESS;
							//Send Chat
							if ( reward.getPropLevel() >= 4 || ItemManager.godStoneId.equals(reward.getId()) ) {
								String roleName = UserManager.getDisplayRoleName(user.getRoleName());
								String content = Text.text("notice.openItemBox", roleName, propData.getName(), 
										reward.getPropCount());
								ChatManager.getInstance().processChatToWorldAsyn(null, content);
							}
							StatClient.getIntance().sendDataToStatServer(user, 
									statAction, "Stone", propData.getName(), reward.getPropLevel());
							
							message = Text.text("pickreward.stone", propData.getName());

							SysMessageManager.getInstance().sendClientInfoRawMessage(user, message,
									Action.NOOP,
									XinqiSysMessage.Type.NORMAL);

						} else {
							result = PickRewardResult.BAG_FULL;
							ArrayList<Reward> gifts = new ArrayList<Reward>();
							gifts.add(reward);
							sendMailIfBagFull(user, gifts);
						}
					} else {
						logger.warn("User picks up a null itemPojo. PropId {}", reward.getPropId());
					}
					break;
				case ITEM:
					ItemPojo itemPojo = GameContext.getInstance().getItemManager().
						getItemById(reward.getPropId());
					if ( itemPojo != null ) {
						PropData propData = itemPojo.toPropData(count); 
						boolean success = user.getBag().addOtherPropDatas(propData);
						if ( success ) {
							bagChanged = true;
							result = PickRewardResult.SUCCESS;
							
							//Send Chat
							if ( ItemManager.godStoneId.equals(reward.getId()) ) {
								String roleName = UserManager.getDisplayRoleName(user.getRoleName());
								String content = Text.text("notice.openItemBox", roleName, propData.getName(), 
										reward.getPropCount());
								ChatManager.getInstance().processChatToWorldAsyn(null, content);
							}
							
							StatClient.getIntance().sendDataToStatServer(user, 
									statAction, "Item", propData.getName(), reward.getPropLevel());
							
							message = Text.text("pickreward.item", propData.getName());

							SysMessageManager.getInstance().sendClientInfoRawMessage(user, message,
									Action.NOOP,
									XinqiSysMessage.Type.NORMAL);

						} else {
							StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.BagFull, "Item", propData.getName(), reward.getPropLevel());
							result = PickRewardResult.BAG_FULL;
							ArrayList<Reward> gifts = new ArrayList<Reward>();
							gifts.add(reward);
							sendMailIfBagFull(user, gifts);
						}
					} else {
						logger.warn("User picks up a null itemPojo. PropId {}", reward.getPropId());
					}
					break;
				case MEDAL:
					user.setMedal(user.getMedal() + reward.getPropCount());
					result = PickRewardResult.SUCCESS;
					break;
//				case TOOL:
					//TODO support BattleReward type Tool
//					break;
				case VOUCHER:
					user.setVoucher(user.getVoucher() + reward.getPropCount());
					result = PickRewardResult.SUCCESS;
					break;
				case WEAPON:
					//It is the typeName now
					PropData propData = convertRewardWeaponToPropData(reward, user);
					if ( propData != null ) {
						boolean success = false;
						for ( int i=0; i<count; i++ ) {
							success = user.getBag().addOtherPropDatas(propData);
							if ( !success ) {
								break;
							}
						}
						WeaponColor weaponColor = propData.getWeaponColor();
						if ( success ) {
							bagChanged = true;
							result = PickRewardResult.SUCCESS;

							//Send Chat
							String roleName = UserManager.getDisplayRoleName(user.getRoleName());
							if ( weaponColor == WeaponColor.PINK ) {
								String content = Text.text("notice.openEquipBox.pink", roleName, propData.getName());
								ChatManager.getInstance().processChatToWorldAsyn(null, content);								
							} else if ( weaponColor == WeaponColor.ORGANCE ) {
								String content = Text.text("notice.openEquipBox.orange", roleName, propData.getName());
								ChatManager.getInstance().processChatToWorldAsyn(null, content);
							} else if ( reward.getPropLevel() > 5 ) {
								String content = Text.text("notice.openEquipBox", roleName, reward.getPropLevel(), propData.getName());
								ChatManager.getInstance().processChatToWorldAsyn(null, content);
							}
							
							StatClient.getIntance().sendDataToStatServer(user, 
									statAction, "Weapon", propData.getName(), weaponColor, propData.getPropIndate(), reward.getPropLevel());
							
							/**
							 * The message will printed in Bag.addOtherPropDatas()
							 * 2013-03-04
							 */
							/*
							message = Text.text("pickreward.weapon", propData.getName());

							SysMessageManager.getInstance().sendClientInfoRawMessage(user, message,
									Action.NOOP,
									XinqiSysMessage.Type.NORMAL);
							*/
						} else {
							result = PickRewardResult.BAG_FULL;
							StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.BagFull, "Weapon", propData.getName(), 
									weaponColor, propData.getPropIndate(), reward.getPropLevel());
							ArrayList<Reward> gifts = new ArrayList<Reward>();
							gifts.add(reward);
							sendMailIfBagFull(user, gifts);
						}
					} else {
						logger.warn("User picks up a null weaponPojo. PropId {}", reward.getPropId());
					}
					break;
				case YUANBAO:
					user.setYuanbaoFree(user.getYuanbaoFree() + reward.getPropCount());
					result = PickRewardResult.SUCCESS;
					
					StatClient.getIntance().sendDataToStatServer(user, 
							statAction, MoneyType.YUANBAO, reward.getPropCount(),
							Constant.EMPTY, Constant.EMPTY, Constant.EMPTY);
					
					message = Text.text("pickreward.yuanbao", reward.getPropCount());
					SysMessageManager.getInstance().sendClientInfoRawMessage(user, message,
							Action.NOOP,
							XinqiSysMessage.Type.CONFIRM);
					break;
				case UNKNOWN:
					logger.warn("It reward type is unknown. Ignore it", reward.getType());
					break;
				default:
					logger.debug("Unknown reward type: {}", reward.getType());	
			}
		}
		if ( result == PickRewardResult.SUCCESS ) {
			//Save user and user's bag to database.
			GameContext.getInstance().getUserManager().saveUser(user, false);
			if ( bagChanged ) {
				GameContext.getInstance().getUserManager().saveUserBag(user, false);
			}
			
			//Notify client user's role data is changed.
			//Send the data back to client
			BseRoleInfo roleInfo = user.toBseRoleInfo();
			GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
			logger.debug("#pickReward: The new roleInfo data for user {} is sent to client.", user.getRoleName());
		} else {
			logger.debug("#pickReward: The user donot pick anything.");
		}
		return result;
	}

	/**
	 * @param user
	 */
	public final void sendMailIfBagFull(User user, Collection<Reward> gifts) { 
		String subject = Text.text("bag.pick.full.sub");
		String content = Text.text("bag.pick.full.content");
		MailMessageManager.getInstance().sendMail(null, user.get_id(), 
				subject, content, gifts, true);
	}
	
	/**
	 * Convert the reward to weapon.
	 * @param reward
	 * @return
	 */
	public PropData convertRewardWeaponToPropData(Reward reward, User user) {
		//It is the typeName now
		PropData propData = null;
		WeaponPojo weaponPojo = convertRewardToWeapon(reward, user.getLevel());
		propData = weaponPojo.toPropData(reward.getPropIndate(), reward.getPropColor(), 
				reward.getMaxStrength(),  makeWeaponSlot(reward.getSlot()));
		//Give it a warranty time.
		ShopManager.setPropDataWarranty(propData);
		
		//int upgradeLevel = reward.getPropLevel() - propData.getLevel();
		int upgradeLevel = reward.getPropLevel();
		//Recalculate the PropData's attack ... properties
		if ( upgradeLevel > 0 ) {
			ScriptResult scriptResult = GameContext.getInstance().getScriptManager().
					runScript(ScriptHook.WEAPON_LEVEL_UPGRADE, propData, upgradeLevel);
			if ( scriptResult.getType() == Type.SUCCESS_RETURN ) {
				propData = (PropData)scriptResult.getResult().get(0);
			}
		}
		return propData; 
	}

	/**
	 * @param reward
	 * @param user
	 * @return
	 */
	public WeaponPojo convertRewardToWeapon(Reward reward, int userLevel) {
		WeaponPojo weaponPojo = null;
		String id = reward.getPropId();
		if ( !StringUtil.checkNotEmpty(reward.getTypeId()) || Constant.ONE_NEGATIVE.equals(reward.getTypeId()) ) {
			weaponPojo = GameContext.getInstance().getEquipManager().
					getWeaponById(reward.getPropId());
		} else {
			String typeName = reward.getTypeId();
			if ( userLevel%10>=8 ) {
				if ( MathUtil.nextDouble() < 0.5 ) {
					userLevel += 2;
				}
			}
			weaponPojo = GameContext.getInstance().getEquipManager().
					getWeaponByTypeNameAndUserLevel(typeName, userLevel);
		}
		return weaponPojo;
	}
	
	/**
	 * Convert a reaward item to PropData
	 * @param reward
	 * @return
	 */
	public final PropData convertRewardItemToPropData(Reward reward) {
		ItemPojo itemPojo = GameContext.getInstance().
				getItemManager().getItemById(reward.getPropId());
		if ( itemPojo != null ) {
			PropData propData = itemPojo.toPropData(reward.getPropCount()); 
			return propData;
		} else {
			logger.warn("User picks up a null itemPojo. PropId {}", reward.getPropId());
		}
		return null;
	}
	
	/**
	 * Convert a propData to reward
	 * @param propData
	 * @return
	 */
	public final Reward convertPropDataToReward(PropData propData) {
		if ( propData != null ) {
			Reward reward = new Reward();
			reward.setId(propData.getItemId());
			reward.setLevel(propData.getLevel());
			reward.setPropColor(propData.getWeaponColor());
			reward.setPropCount(propData.getCount());
			reward.setPropIndate(propData.getPropIndate());
			reward.setUsedTimes(propData.getPropUsedTime());
			reward.setMaxStrength(propData.getMaxLevel());
			reward.setSlot(propData.getTotalSlot());
			if ( propData.isWeapon() ) {
				reward.setType(RewardType.WEAPON);
				/**
				 * It is incompatible with the slot system
				 * wangqi 2013-3-21
				 */
				/*
				int addAttack = propData.getSlotTotalValue(PropDataEnhanceField.ATTACK);
				reward.setAddAttack(addAttack);
				int addDefend = propData.getSlotTotalValue(PropDataEnhanceField.DEFEND);
				reward.setAddDefend(addDefend);
				int addAgility = propData.getSlotTotalValue(PropDataEnhanceField.AGILITY);
				reward.setAddAgility(addAgility);
				int addLuck = propData.getSlotTotalValue(PropDataEnhanceField.LUCKY);
				reward.setAddLucky(addLuck);
				*/
			} else {
				reward.setType(RewardType.ITEM);
			}
			return reward;
		}
		return null;
	}
	
	/**
	 * If the reward is a ITEM, STONE or WEAPON, get 
	 * its name.
	 * @param reward
	 * @return
	 */
	public final String getRewardName(Reward reward) {
		String propName = Constant.EMPTY;
		RewardType rewardType = reward.getType();
		switch ( rewardType ) {
			case ITEM:
			case STONE:
				propName = ItemManager.getInstance().getItemById(reward.getPropId()).getName();
				break;
			case WEAPON:
				propName = EquipManager.getInstance().getWeaponById(reward.getPropId()).getName();
				break;
		}
		return propName;
	}

	/**
	 * Open a treasure box in user's bag
	 *  
	 * @param user
	 * @param pew
	 */
	public PickRewardResult openItemBox(User user, int pew) {
		PickRewardResult pickResult = PickRewardResult.NOTHING;
		
		Bag bag = user.getBag();
		String itemId = Constant.EMPTY;
		String itemName = Constant.EMPTY;
		PropData propData = bag.getOtherPropData(pew);
		try {
			if ( propData != null ) {
				itemId = propData.getItemId();
				itemName = propData.getName();
				ItemPojo itemPojo = ItemManager.getInstance().getItemById(itemId);
				if ( itemPojo != null ) {					
					String script = itemPojo.getScript();
					if ( script != null ) {
						ScriptHook scriptHook = ScriptHook.getScriptHook(script);
						if ( scriptHook != null ) {
							ScriptResult result = ScriptManager.getInstance().runScript(
									scriptHook, user, itemPojo, pew);
							if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
								List list = result.getResult();
								pickResult = (PickRewardResult)list.get(0);
								String content = null;
								if ( list.size() > 1 ) {
									content = (String)list.get(1);
								}
								if ( pickResult == PickRewardResult.SUCCESS ) {									
									if ( content != null && itemPojo.isBroadcast()) {
										ChatManager.getInstance().processChatToWorldAsyn(null, content);
									}
								}
								return pickResult;
							} else if ( result.getType() == ScriptResult.Type.SUCCESS ) {
								return PickRewardResult.SUCCESS;
							} else {
								return PickRewardResult.OTHER;
							}
						} else {
							SysMessageManager.getInstance().sendClientInfoMessage(user, "box.not_a_box", Action.NOOP, new Object[]{itemPojo.getName()});
							pickResult = PickRewardResult.NOT_A_BOX;
							//logger.warn("#openItemBox: No ScriptHook for script: {}", script);
							pickResult = PickRewardResult.NO_SCRIPT;
						}
					} else {
						//logger.warn("#openItemBox: ItemPojo {} is not a box since no script binding.", itemPojo.getName());
						SysMessageManager.getInstance().sendClientInfoMessage(user, "box.not_a_box", Action.NOOP, new Object[]{itemPojo.getName()});
						pickResult = PickRewardResult.NOT_A_BOX;
					}
				} else {
					logger.warn("#openItemBox: Cannot find itemPojo by it: {}", itemId);
					pickResult = PickRewardResult.NO_ITEM;
				}
			} else {
				logger.debug("The user {} bag's pew {} is not a box.", user.getRoleName(), pew);
				pickResult = PickRewardResult.NOT_A_BOX;
			}
		} finally {
			StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.UseProp, itemId, itemName, pickResult);
			if ( pickResult == PickRewardResult.SUCCESS ) {
				UserActionManager.getInstance().addUserAction(user.getRoleName(), 
					UserActionKey.UseProp, itemName);
			}
		}
		return pickResult;
	}
	
	/**
	 * Send the online rewards to users.
	 * 
	 * @param reward
	 */
	public final void sendOnlineReward(User user, Reward[] rewards) {
		BseOnlineReward.Builder onlineRewardBuilder = BseOnlineReward.newBuilder();
		onlineRewardBuilder.setStepID(0);
		onlineRewardBuilder.setRemainTime(0);
		for ( Reward reward : rewards ) {
		  //金币:-1
		  //礼券:-2
		  //元宝:-3
		  //勋章:-4
			switch ( reward.getType() ) {
				case EXP:
					onlineRewardBuilder.addPropID(-5);
					onlineRewardBuilder.addPropLevel(0);
					onlineRewardBuilder.addPropCount(reward.getPropCount());
					break;
				case GOLDEN:
					onlineRewardBuilder.addPropID(-1);
					onlineRewardBuilder.addPropLevel(0);
					onlineRewardBuilder.addPropCount(reward.getPropCount());
					break;
				case YUANBAO:
					onlineRewardBuilder.addPropID(-3);
					onlineRewardBuilder.addPropLevel(0);
					onlineRewardBuilder.addPropCount(reward.getPropCount());
					break;
				default:
					onlineRewardBuilder.addPropID(StringUtil.toInt(reward.getPropId(), 0));
					onlineRewardBuilder.addPropLevel(reward.getPropLevel());
					onlineRewardBuilder.addPropCount(reward.getPropCount());
					break;
			}
		}
		onlineRewardBuilder.setType(0);
		
		XinqiMessage onlineRewardXinqi = new XinqiMessage();
		onlineRewardXinqi.payload = onlineRewardBuilder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), onlineRewardXinqi);
		
	}
	
	/**
	 * Get the login reward key name in Redis
	 * @param userName
	 * @return
	 */
	public static final String getLoginRewardKeyName(String userName) {
		String key = StringUtil.concat(LOGIN_REWARD, userName);
		return key;
	}
	
	/**
	 * Get the daily mark reward key name in Redis
	 * @param userName
	 * @return
	 */
	public static final String getDailyMarkRewardKeyName(String userName) {
		String key = StringUtil.concat(DAILY_REWARD, userName);
		return key;
	}
	
	/**
	 * Get the online reward key name in Redis
	 * @param userName
	 * @return
	 */
	public static final String getOnlineRewardKeyName(String userName) {
		String key = StringUtil.concat(ONLINE_REWARD, userName);
		return key;
	}
	
	/**
	 * Add and save the reward level pojo into database
	 * @param pojo
	 */
	public final void addRewardLevelPojo(RewardLevelPojo pojo) {
		DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, pojo.get_id());
		DBObject dbObj = MongoDBUtil.createMapDBObject(pojo);
		MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_REWARDLEVELS_NAME, isSafeWrite);
	}
	
	/**
	 * Display the reward selection card to users to choose.
	 * @param user
	 * @param fourRewards
	 */
	public final void displayRewardCards(User user, ArrayList<Reward> fourRewards, String id) {
		BseOnlineReward.Builder or = BseOnlineReward.newBuilder();
		or.setRemainTime(4);
		int count = fourRewards.size();
		if ( count > 4 ) count = 4;
		for ( int i=0; i<count; i++ ) {
			Reward reward = fourRewards.get(i);
			or.addPropLevel(1);
			or.addPropCount(1);
			or.addPropID(StringUtil.toInt(reward.getId(), 0));
		}
		or.setId(id);
		//可抽奖次数
		or.setRemainTime(1);
		GameContext.getInstance().writeResponse(user.getSessionKey(), or.build());
	}
	
	/**
	 * @return
	 */
	public ArrayList<PropDataSlot> makeWeaponSlot(int slot) {
		Object[] pickedSlots = null;
		if ( slot > 0 ) {
			ArrayList<PropDataSlot> slotList = new ArrayList<PropDataSlot>();
			int max = slot*3;
			for ( int i=0; i<max; i++ ) {
				PropDataSlot s = new PropDataSlot();
				/**
				 * 确定每个槽位能容纳的类型，最多四种类型
				 */
				int typeCount = MathUtil.nextGaussionInt(1, 5, 3.0);
				Object[] fieldObjs = MathUtil.randomPick(RewardManager.FIELD_LIST, typeCount);
				for (int j = 0; j < fieldObjs.length; j++) {
					PropDataEnhanceField field = (PropDataEnhanceField)fieldObjs[j];
					s.addAvailableTypes(field);
				}
				slotList.add(s);
			}
			pickedSlots = MathUtil.randomPick(slotList, slot);
			slotList.clear();
		}
		ArrayList<PropDataSlot> slotList = null;
		if ( pickedSlots != null ) {
			slotList = new ArrayList<PropDataSlot>();
			for (int i = 0; i < pickedSlots.length; i++) {
				slotList.add((PropDataSlot)pickedSlots[i]);
			}
		}
		return slotList;
	}
	
	/**
	 * The fields for HTABLE in Redis
	 * @author wangqi
	 *
	 */
	public static enum LoginRewardField {
		lastday,
		continuous,
		remaintimes,
	}
	
	public static enum DailyMarkField {
		currentmonth,
		currentdate,
		markarray,
		totalcount,
		taken,
	}
	
	public static enum OnlineRewardField {
		currentdate,
		stepid,
		timeclock,
		taken,
	}
}
