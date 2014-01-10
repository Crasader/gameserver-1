package com.xinqihd.sns.gameserver.db.mongo;

import static com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseGameDataKey.BseGameDataKey;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;

/**
 * It is used to store and read game configurable data to or from Mongo database.
 * The collection format in database is 
 * {
 * 	key:     <>
 *  value:   <>
 *  desc:    <>
 *  default: <> 
 * }
 * @author wangqi
 *
 */
public class GameDataManager extends AbstractMongoManager {
	
	private static final Logger logger = LoggerFactory.getLogger(GameDataManager.class);
	
	private static final String GAMEDATA_COLL_NAME = "gamedata";
	
	private static final String GAMEDATA_KEY_NAME     = "key";
	private static final String GAMEDATA_VALUE_NAME   = "value";
	private static final String GAMEDATA_DESC_NAME    = "desc";
	private static final String GAMEDATA_DEFAULT_NAME = "default";
	
	//It serves as a null in database.
	private static final Object NULL = new Object();  
	
	private static GameDataManager instance = new GameDataManager();
	
	private ConcurrentHashMap<GameDataKey, Object> dataMap = 
			new ConcurrentHashMap<GameDataKey, Object>();

	GameDataManager() {
		super(GAMEDATA_COLL_NAME, GAMEDATA_KEY_NAME);
		reload();
	}
	
	/**
	 * It's for the facility of testcase.
	 * 
	 * Please call the {@link UserManager#getInstance()}
	 * DO NOT call it directly or the system will be banded to Mongodb. 
	 */
	GameDataManager(String databaseName, String namespace, boolean isSafeWrite) {
		super(databaseName, namespace, isSafeWrite, GAMEDATA_COLL_NAME, GAMEDATA_KEY_NAME);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		dataMap.clear();
		
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				GAMEDATA_COLL_NAME, null);
		for ( DBObject result : list ) {
			Object obj = NULL;
			if ( result != null ) {
				String keyName = (String)result.get(GAMEDATA_KEY_NAME);
				GameDataKey key = null;
				if ( keyName != null ) {
					key = GameDataKey.fromKey(keyName);
				}
				if ( key != null ) {
					obj = result.get(GAMEDATA_VALUE_NAME);
					dataMap.put(key, obj);
				}
			}
		}
		
		for ( GameDataKey key : GameDataKey.values() ) {
			//query from mongo database
			reload(key);
		}
		logger.debug("Load total {} GameDataKeys from database", GameDataKey.values().length);
	}	
	/**
	 * Reload given key's new value from database into memory.
	 */
	public void reload(GameDataKey key) {
		DBObject query = createDBObject(GAMEDATA_KEY_NAME, key.getKey());
		DBObject field = createDBObject(GAMEDATA_VALUE_NAME, Constant.ONE);
		DBObject result = queryFromMongo(query, databaseName, namespace, collectionName, field);
		Object obj = NULL;
		if ( result != null ) {
			obj = result.get(GAMEDATA_VALUE_NAME);
		}
		dataMap.put(key, obj);
		//logger.debug("Load GameDataKey {} value {}", key, obj.toString());
	}
	
	/**
	 * Get the default singleton instance
	 * @return
	 */
	public static GameDataManager getInstance() {
		return instance;
	}
	
	/**
	 * Get the game data from database as a double value.
	 * If it does not exist in database, or is illegal, return defaultValue.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public double getGameDataAsDouble(GameDataKey key, double defaultValue) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return defaultValue;
		}
		return (Double)obj;
	}
	
	/**
	 * Get the game data from database as a double value array.
	 * If it does not exist in database, or is illegal, return an empty double array.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public double[] getGameDataAsDoubleArray(GameDataKey key) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return new double[0];
		} else if ( obj instanceof double[] ) {
			return (double[])obj;
		} else {
			BasicDBList list = (BasicDBList)obj;
			double[] array = new double[list.size()];
			for ( int i=0; i<array.length; i++ ) {
				array[i] = (Double)list.get(i);
			}
			return array;
		}
	}
	
	/**
	 * Get the game data from database as a two dimension double value array.
	 * 
	 * @param key
	 * @return
	 */
	public double[][] getGameDataAsDoubleArrayArray(GameDataKey key) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return new double[0][0];
		} else if ( obj instanceof double[] ) {
			return (double[][])obj;
		} else {
			BasicDBList list = (BasicDBList)obj;
			double[][] array = new double[list.size()][];
			for ( int i=0; i<array.length; i++ ) {
				BasicDBList dbList = (BasicDBList)list.get(i);
				array[i] = new double[dbList.size()];
				for ( int j=0; j<array[i].length; j++ ) {
					array[i][j] = (Double)dbList.get(j);
				}
			}
			return array;
		}
	}
	
	/**
	 * Get the game data from database as a int value array.
	 * If it does not exist in database, or is illegal, return an empty double array.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int[] getGameDataAsIntArray(GameDataKey key) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return new int[0];
		} else if ( obj instanceof double[] ) {
			return (int[])obj;
		} else {
			BasicDBList list = (BasicDBList)obj;
			int[] array = new int[list.size()];
			for ( int i=0; i<array.length; i++ ) {
				array[i] = (Integer)list.get(i);
			}
			return array;
		}
	}
	
	/**
	 * Get the game data from database as a int value.
	 * If it does not exist in database, or is illegal, return defaultValue.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getGameDataAsInt(GameDataKey key, int defaultValue) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return defaultValue;
		}
		return (Integer)obj;
	}
	
	/**
	 * Get the game data from database as a two dimension double value array.
	 * 
	 * @param key
	 * @return
	 */
	public int[][] getGameDataAsIntArrayArray(GameDataKey key) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return new int[0][0];
		} else if ( obj instanceof double[] ) {
			return (int[][])obj;
		} else {
			BasicDBList list = (BasicDBList)obj;
			int[][] array = new int[list.size()][];
			for ( int i=0; i<array.length; i++ ) {
				BasicDBList dbList = (BasicDBList)list.get(i);
				array[i] = new int[dbList.size()];
				for ( int j=0; j<array[i].length; j++ ) {
					array[i][j] = (Integer)dbList.get(j);
				}
			}
			return array;
		}
	}
	
	/**
	 * Get the game data from database as a boolean value.
	 * If it does not exist in database, or is illegal, return defaultValue.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public boolean getGameDataAsBoolean(GameDataKey key, boolean defaultValue) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return defaultValue;
		}
		return (Boolean)obj;
	}
	
	/**
	 * Get the game data from database as a string. 
	 * If it does not exist in database, or is illegal, return null
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getGameDataAsString(GameDataKey key) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return null;
		}
		return obj.toString();
	}
	
	/**
	 * 
	 * @param key
	 * @param defaultStr
	 * @return
	 */
	public String getGameDataAsString(GameDataKey key, String defaultStr) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return defaultStr;
		}
		return obj.toString();
	}
	
	/**
	 * Get the game data from database as a double value array.
	 * If it does not exist in database, or is illegal, return an empty double array.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String[] getGameDataAsStringArray(GameDataKey key) {
		Object obj = getValueFromDatabase(key);
		if ( obj == NULL ) {
			return new String[0];
		} else if ( obj instanceof String[] ) {
			return (String[])obj;
		} else {
			BasicDBList list = (BasicDBList)obj;
			String[] array = new String[list.size()];
			for ( int i=0; i<array.length; i++ ) {
				array[i] = list.get(i).toString();
			}
			return array;
		}
	}
	
	/**
	 * Override the value at runtime. It is mainly used for test case.
	 * @param key
	 * @param value
	 */
	public void overrideRuntimeValue(GameDataKey key, int value) {
		dataMap.put(key, value);
	}
	
	/**
	 * Override the value at runtime. It is mainly used for test case.
	 * @param key
	 * @param value
	 */
	public void overrideRuntimeValue(GameDataKey key, double value) {
		dataMap.put(key, value);
	}
	
	/**
	 * Override the value at runtime. It is mainly used for test case.
	 * @param key
	 * @param value
	 */
	public void overrideRuntimeValue(GameDataKey key, int[] values) {
		dataMap.put(key, values);
	}
	
	/**
	 * Override the value at runtime. It is mainly used for test case.
	 * @param key
	 * @param value
	 */
	public void overrideRuntimeValue(GameDataKey key, double[] values) {
		dataMap.put(key, values);
	}
	
	/**
	 * Save the value into database.
	 * @param key
	 * @param value
	 */
	public void setValueToDatabase(GameDataKey key, Object value) {
		Object defaultValue = this.getValueFromDatabase(key);
		setValueToDatabase(key, value, defaultValue);
	}
	
	/**
	 * Save the value into database.
	 * @param key
	 * @param value
	 */
	public void setValueToDatabase(GameDataKey key, Object value, Object defaultValue) {
		if ( value == null || value == NULL ) return;
		
		DBObject query = createDBObject();
		query.put(GAMEDATA_KEY_NAME, key.getKey());
		
		DBObject dbObj = createDBObject();
		dbObj.put(GAMEDATA_KEY_NAME, key.getKey());
		dbObj.put(GAMEDATA_VALUE_NAME, value);
		dbObj.put(GAMEDATA_DESC_NAME,  key.getDesc());
		dbObj.put(GAMEDATA_DEFAULT_NAME, defaultValue);
		
		saveToMongo(query, dbObj, databaseName, namespace, collectionName, isSafeWrite);
		
		//Reload the database because it is changed.
		this.reload(key);
	}
	
	/**
	 * Get the data from database. If the data doest not exist,
	 * put Null into map.
	 * @param key
	 * @return
	 */
	private Object getValueFromDatabase(GameDataKey key) {
		Object obj = dataMap.get(key);
		return obj;
	}
	
	/**
	 * Get the underlying dataMap.
	 * @return
	 */
	public Map<GameDataKey, Object> getDataMap() {
		return dataMap;
	}
	
	/**
	 * Send required data to client by BseGameDataKey
	 * @return
	 */
	public BseGameDataKey toBseGameDataKey(int userLevel) {
		BseGameDataKey.Builder builder = BseGameDataKey.newBuilder();
	  //力度微调系数
	  builder.setBattleAttackK((int)(10000*getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 0.059081f)));
	  //风力微调系数
	  builder.setBattleAttackF((int)(10000*getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_F, 0.075f)));
	  //重力微调系数
	  builder.setBattleAttackG(getGameDataAsInt(GameDataKey.BATTLE_ATTACK_G, 760));
	  //玩家每移动1次减少的体力值
	  builder.setThewRoleMove(getGameDataAsInt(GameDataKey.THEW_ROLE_MOVE, 5));
	  //玩家使用附加攻击2次消耗的体力值
	  builder.setThewAttacktwomoretimes(getGameDataAsInt(GameDataKey.THEW_TOOL_AttackTwoMoreTimes, 210));
	  //玩家使用三叉戟攻击消耗的体力值
	  builder.setThewAttackthreebranch(getGameDataAsInt(GameDataKey.THEW_TOOL_AttackThreeBranch, 84));
	  //玩家使用附加攻击1次消耗的体力值
	  builder.setThewAttackonemoretimes(getGameDataAsInt(GameDataKey.THEW_TOOL_AttackOneMoreTimes, 190));
	  //玩家使用伤害50%消耗的体力值
	  builder.setThewHurtadd50(getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd50, 210));
	  //玩家使用伤害40%消耗的体力值
	  builder.setThewHurtadd40(getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd40, 196));
	  //玩家使用伤害30%消耗的体力值
	  builder.setThewHurtadd30(getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd30, 182));
	  //玩家使用伤害20%消耗的体力值
	  builder.setThewHurtadd20(getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd20, 168));
	  //玩家使用伤害10%消耗的体力值
	  builder.setThewHurtadd10(getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd10, 154));
	  //玩家使用加血技能消耗的体力值
	  builder.setThewToolRecover(getGameDataAsInt(GameDataKey.THEW_TOOL_RECOVER, 210));
	  //玩家使用团队加血技能消耗的体力值
	  builder.setThewToolAllrecover(getGameDataAsInt(GameDataKey.THEW_TOOL_ALLRECOVER, 210));
	  //玩家使用隐身技能消耗的体力值
	  builder.setThewToolHidden(getGameDataAsInt(GameDataKey.THEW_TOOL_HIDDEN, 50));
	  //玩家使用团队隐身技能消耗的体力值
	  builder.setThewToolAllhidden(getGameDataAsInt(GameDataKey.THEW_TOOL_ALLHIDDEN, 150));
	  //玩家使用改变风向技能消耗的体力值
	  builder.setThewToolWind(getGameDataAsInt(GameDataKey.THEW_TOOL_WIND, 50));
	  //玩家使用冰弹技能消耗的体力值
	  builder.setThewToolIce(getGameDataAsInt(GameDataKey.THEW_TOOL_ICE, 150));
	  //玩家使用传送技能消耗的体力值
	  builder.setThewToolFly(getGameDataAsInt(GameDataKey.THEW_TOOL_FLY, 150));
	  //玩家使用引导技能消耗的体力值
	  builder.setThewToolGuide(getGameDataAsInt(GameDataKey.THEW_TOOL_GUIDE, 120));
	  //玩家使用怒气技能消耗的体力值
	  builder.setThewToolEnergy(getGameDataAsInt(GameDataKey.THEW_TOOL_ENERGY, 120));
	  //玩家使用核弹技能消耗的体力值
	  builder.setThewToolAtom(getGameDataAsInt(GameDataKey.THEW_TOOL_ATOM, 110));
	  //玩家使用免坑技能消耗的体力值
	  builder.setThewToolNohole(getGameDataAsInt(GameDataKey.THEW_TOOL_NOHOLE, 50));
	  //与装备合成后提高幸运属性的道具类型ID
	  builder.setCraftStoneLuck(getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCK, 20001));
	  //与装备合成后提高防御属性的道具类型ID
	  builder.setCraftStoneDefend(getGameDataAsInt(GameDataKey.CRAFT_STONE_DEFEND, 20002));
	  //与装备合成后提高敏捷属性的道具类型ID
	  builder.setCraftStoneAgility(getGameDataAsInt(GameDataKey.CRAFT_STONE_AGILITY, 20003));
	  //与装备合成后提高攻击属性的道具类型ID
	  builder.setCraftStoneAttack(getGameDataAsInt(GameDataKey.CRAFT_STONE_ATTACK, 20004));
	  //用来强装备提高武器伤害或装备护甲的道具类型ID
	  builder.setCraftStoneStrength(getGameDataAsInt(GameDataKey.CRAFT_STONE_STRENGTH, 20005));
	  //幸运符+15%的道具类型ID
	  builder.setCraftStoneLucky1(getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 24002));
	  //幸运符+25%的道具类型ID
	  builder.setCraftStoneLucky2(getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY2, 24004));
	  //必成符
	  builder.setCraftStoneWin(getGameDataAsInt(GameDataKey.CRAFT_STONE_WIN, 24005));
	  //神恩符的道具类型ID
	  builder.setCraftStoneGod(getGameDataAsInt(GameDataKey.CRAFT_STONE_GOD, 24001));
	  
	  //玩家购买附加攻击2次的价格
	  builder.setPriceToolAttackTwoMoreTimes(getGameDataAsInt(GameDataKey.PRICE_TOOL_AttackTwoMoreTimes, 1200));
	  //玩家购买三叉戟攻击的价格
	  builder.setPriceToolAttackThreeBranch(getGameDataAsInt(GameDataKey.PRICE_TOOL_AttackThreeBranch, 1200));
	  //玩家购买附加攻击1次的价格
	  builder.setPriceToolAttackOneMoreTimes(getGameDataAsInt(GameDataKey.PRICE_TOOL_AttackOneMoreTimes, 1200));
	  //玩家购买伤害50%的价格
	  builder.setPriceToolHurtAdd50(getGameDataAsInt(GameDataKey.PRICE_TOOL_HurtAdd50, 1200));
	  //玩家购买伤害40%的价格
	  builder.setPriceToolHurtAdd40(getGameDataAsInt(GameDataKey.PRICE_TOOL_HurtAdd40, 1200));
	  //玩家购买伤害30%的价格
	  builder.setPriceToolHurtAdd30(getGameDataAsInt(GameDataKey.PRICE_TOOL_HurtAdd30, 1200));
	  //玩家购买伤害20%的价格
	  builder.setPriceToolHurtAdd20(getGameDataAsInt(GameDataKey.PRICE_TOOL_HurtAdd20, 1200));
	  //玩家购买伤害10%的价格
	  builder.setPriceToolHurtAdd10(getGameDataAsInt(GameDataKey.PRICE_TOOL_HurtAdd10, 1200));
	  
	  //玩家购买加血技能的价格
	  int bloodPriceUnit = getGameDataAsInt(GameDataKey.PRICE_TOOL_RECOVER, 18);
	  builder.setPriceToolRecover(EquipCalculator.calculateBloodPrice(userLevel, bloodPriceUnit));
	  //玩家购买团队加血技能的价格
	  bloodPriceUnit = getGameDataAsInt(GameDataKey.PRICE_TOOL_ALLRECOVER, 20);
	  builder.setPriceToolAllRecover(EquipCalculator.calculateBloodPrice(userLevel, bloodPriceUnit));
	  
	  //玩家购买隐身技能的价格
	  builder.setPriceToolHidden(getGameDataAsInt(GameDataKey.PRICE_TOOL_HIDDEN, 1200));
	  //玩家购买团队隐身技能的价格
	  builder.setPriceToolAllHidden(getGameDataAsInt(GameDataKey.PRICE_TOOL_ALLHIDDEN, 1200));
	  //玩家购买改变风向技能的价格
	  builder.setPriceToolWind(getGameDataAsInt(GameDataKey.PRICE_TOOL_WIND, 1200));
	  //玩家购买冰弹技能的价格
	  builder.setPriceToolIce(getGameDataAsInt(GameDataKey.PRICE_TOOL_ICE, 1200));
	  //玩家购买传送技能的价格
	  builder.setPriceToolFly(getGameDataAsInt(GameDataKey.PRICE_TOOL_FLY, 1200));
	  //玩家购买引导技能的价格
	  builder.setPriceToolGuide(getGameDataAsInt(GameDataKey.PRICE_TOOL_GUIDE, 1200));
	  //玩家购买怒气技能的价格
	  builder.setPriceToolEnergy(getGameDataAsInt(GameDataKey.PRICE_TOOL_ENERGY, 1200));
	  //玩家购买核弹技能的价格
	  builder.setPriceToolAtom(getGameDataAsInt(GameDataKey.PRICE_TOOL_ATOM, 1200));
	  //玩家购买免坑技能的价格
	  builder.setPriceToolNoHole(getGameDataAsInt(GameDataKey.PRICE_TOOL_NOHOLE, 1200));
	  
	  //合成高等级装备的金币价格
	  int priceUnit = getGameDataAsInt(GameDataKey.PRICE_CRAFT_COMPOSE, 2000);
	  builder.setPriceCraftCompose(EquipCalculator.
	  		calculateCraftPrice(userLevel, priceUnit));
	  //熔炼装备的金币价格
	  priceUnit = getGameDataAsInt(GameDataKey.PRICE_CRAFT_FORGE, 200);
	  builder.setPriceCraftForge(EquipCalculator.
	  		calculateCraftPrice(userLevel, priceUnit));
	  //转移装备属性的金币价格
	  priceUnit = getGameDataAsInt(GameDataKey.PRICE_CRAFT_TRANSFER, 500);
	  builder.setPriceCraftTransfer(EquipCalculator.
	  		calculateCraftPrice(userLevel, priceUnit));

	  //水神石typeid
	  builder.setStoneWaterTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCK, 20001));
	  //土神石typeid
	  builder.setStoneEarthTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_DEFEND, 20002));
	  //风神石typeid
	  builder.setStoneWingTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_AGILITY, 20003));
	  //火神石typeid
	  builder.setStoneFireTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_ATTACK, 20004));
	  //强化石typeid
	  builder.setStoneStrengthTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_STRENGTH, 20005));
	  //幸运符typeid
	  builder.setStoneLuckyTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY_TYPEID, 24002));
	  //神恩符typeid
	  builder.setStoneGodTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_GOD, 24001));
	  //熔炼公式typeid
	  builder.setStoneFuncTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_FUNC, 21001));
	  //熔炼手镯和戒指typeid
	  builder.setStoneRingTypeid(getGameDataAsInt(GameDataKey.CRAFT_STONE_RING, 21006));
	  //玩家使用加血道具每次恢复的血量
	  builder.setToolRecoverValue(getGameDataAsInt(GameDataKey.TOOL_RECOVER_VALUE, 500));
	  //玩家使用团队加血道具每次恢复的血量
	  builder.setToolAllRecoverValue(getGameDataAsInt(GameDataKey.TOOL_ALL_RECOVER_VALUE, 300));
	  //玩家使用蓄力道具每次增加的怒气值
	  builder.setToolEnergyValue(getGameDataAsInt(GameDataKey.TOOL_ENERGY_VALUE, 100));
	  //玩家使用冰冻道具冻结的回合数
	  builder.setToolIcedValue(getGameDataAsInt(GameDataKey.TOOL_ICED_VALUE, 3));
	  //玩家使用隐身道具隐藏的回合数
	  builder.setToolHiddenValue(getGameDataAsInt(GameDataKey.TOOL_HIDDEN_VALUE, 3));
	  //普通用户在商城中享受的折扣(0-100)
	  builder.setNormalShopDiscount(getGameDataAsInt(GameDataKey.NORMAL_SHOP_DISCOUNT, 100));
	  //VIP用户在商城中享受的折扣(0-100)
	  builder.setVipShopDiscount(getGameDataAsInt(GameDataKey.VIP_SHOP_DISCOUNT, 80));
	  //新手的引导线
	  builder.setNewbieBattleGuide(getGameDataAsInt(GameDataKey.NEWBIE_BATTLE_GUIDE, 10));
	  //黄钻石
	  builder.setCraftStoneDiamond(getGameDataAsInt(GameDataKey.CRAFT_STONE_DIAMOND, 20031));
	  //水晶
	  builder.setCraftStoneCrystal(getGameDataAsInt(GameDataKey.CRAFT_STONE_CRYSTAL, 20041));
		return builder.build();
	}
	
	/**
	 * Save default value to database.
	 */
	public static void saveDefaultValue() {
		Object[][] values = new Object[][]{
				{ GameDataKey.STANDARD_USER_EXP, 100 }, 
				{ GameDataKey.SESSION_TIMEOUT,   60},
				{ GameDataKey.BATTLE_ATTACK_K, 0.059081 }, 
				{ GameDataKey.BATTLE_ATTACK_F, 0.075}, 
				{ GameDataKey.BATTLE_ATTACK_G, 760}, 
				{ GameDataKey.BATTLE_USER_MAX_IDLE, 40 },
				{ GameDataKey.BATTLE_MAX_SECONDS, 300},
				{ GameDataKey.BATTLE_GUIDE_RANGE, 300 },
				{ GameDataKey.BATTLE_GUIDE_HURT_RATIO, 0.2 },
				{ GameDataKey.BATTLE_TREE_BRANCH_HURT_RATIO, 0.5 },
				{ GameDataKey.BATTLE_TWO_CONTINUE_HURT_RATIO, 0.65 },
				{ GameDataKey.BATTLE_ONE_CONTINUE_HURT_RATIO, 0.85 },
				{ GameDataKey.BATTLE_ROOM_MATCH_TIMEOUT,  300},
				
				{ GameDataKey.GAME_ATTACK_INDEX,  1.0},
				{ GameDataKey.GAME_SKIN_INDEX,    0.5},
				{ GameDataKey.GAME_DEFEND_INDEX,  1.5},
				{ GameDataKey.GAME_AGILITY_UNIT,  5 },
				{ GameDataKey.GAME_AGILITY_MAX,  3500.0 },
				{ GameDataKey.BATTLE_CRITICAL_MAX, 4000.0 },

				{ GameDataKey.USER_BODY_WIDTH, 100},
				{ GameDataKey.USER_BODY_HEIGHT, 90},

				{ GameDataKey.USER_DEFAULT_GOLDEN,  5000},
				{ GameDataKey.USER_DEFAULT_YUANBAO, 10},
				{ GameDataKey.USER_DEFAULT_VOUCHER, 0},
				{ GameDataKey.USER_DEFAULT_MEDAL,   0},

				/*
				{ GameDataKey.USER_DEFAULT_EXP,     0},
				{ GameDataKey.USER_DEFAULT_POWER,   60},
				{ GameDataKey.USER_DEFAULT_ATTACK,  0},
				{ GameDataKey.USER_DEFAULT_DEFEND,  0},
				{ GameDataKey.USER_DEFAULT_AGILITY, 0},
				{ GameDataKey.USER_DEFAULT_LUCK,    0},
				{ GameDataKey.USER_DEFAULT_THEW,    210},
				{ GameDataKey.USER_DEFAULT_ENERGY,  100},
				{ GameDataKey.USER_DEFAULT_DAMAGE,  0},
				{ GameDataKey.USER_DEFAULT_SKIN,    0},
				{ GameDataKey.USER_DEFAULT_BLOOD,   24},
				*/
				/*
				{ GameDataKey.USER_EXP_INDEX_A,    1},
				{ GameDataKey.USER_EXP_INDEX_B,    7},
				{ GameDataKey.USER_EXP_INDEX_C,    52},
				*/
				
				{ GameDataKey.USER_THEW_BASE,    210},
				
				{ GameDataKey.USER_BAG_MAX, 60},
				{ GameDataKey.USER_TOOL_MAX, 3},
				{ GameDataKey.USER_TASK_NUMBER, 5},
				{ GameDataKey.USER_ONLINE_REWARD_MAX, 7},
				
				{ GameDataKey.USER_RANK_MAX, 100},
				
				{ GameDataKey.WEAPON_INDATE_SIMPLE,  30},
				{ GameDataKey.WEAPON_INDATE_NORMAL,  100},
				{ GameDataKey.WEAPON_INDATE_SOLID,   200},
				{ GameDataKey.WEAPON_INDATE_ETERNAL, Integer.MAX_VALUE},
				
				{ GameDataKey.SHOP_DPR_TO_GOLDEN, 2.0},
				{ GameDataKey.SHOP_DPR_TO_YUANBAO, 0.006667},
				{ GameDataKey.SHOP_PRICE_SIMPLE_RATIO, 1},
				{ GameDataKey.SHOP_PRICE_NORMAL_RATIO, 3},
				{ GameDataKey.SHOP_PRICE_TOUGH_RATIO, 5},
				
				{ GameDataKey.STRENGTH_MAX_LEVEL, 15},
				{ GameDataKey.STRENGTH_BASE_RATIO, 1.2},
				{ GameDataKey.STRENGTH_NORMAL_RATIO, 2.0},
				{ GameDataKey.STRENGTH_ADVANCE_RATIO, 3.0},
				{ GameDataKey.STRENGTH_DEFEND_RATIO, 1.5},

				{ GameDataKey.STRENGTH_STONE_RATIO, 
					new double[][] {
						//lv1
						new double[] {
							0.4, 0.3, 0.15
						},
						//lv2
						new double[] {
							0.6, 0.4, 0.2, 0.1, 0.05,  
						},
						//lv3
						new double[] {
							0.8, 0.6, 0.4, 0.2, 0.1, 0.05, 0.03
						},
						//lv4
						new double[] {
							1.0, 0.8, 0.6, 0.4, 0.2,  0.1, 0.06, 0.04, 0.03, 0.02
						},
						//lv5
						new double[] {
							1.0, 1.0, 1.0, 0.8, 0.6,  0.4, 0.2, 0.1, 0.06, 0.03,  0.02,0.02, 0.01,0.01,0.01
						},
					}
				},
				{ GameDataKey.STRENGTH_GODSTONE_RANGE, new int[]{ 8, 10, 11, 12, 13 }
				},
				
				{ GameDataKey.WEAPON_LEVEL_RATIO, 
					new double[]{
						1.0, 1.34, 2.68, 4.02, 5.36, 6.70, 8.04, 9.38, 10.72, 12.06
					}
				},
				
//				{ GameDataKey.FORGE_STONE_RANGE,
//					new double[] {
//						//Level 1 stone
//						4,  10,
//						//Level 2 stone
//						8,  20,
//						//Level 3 stone
//						18,  50,
//						//Level 4 stone
//						28,  200,
//						//Level 5 stone
//						80,  400,
//					}
//				},
				//保证各等级融合具有一定的基础数值，
				//即5级合成石一定比4级合成石的范围更大
				{ GameDataKey.FORGE_SIGMA_RATIO,
					new double[] {
						0.2, 0.4, 0.6, 1.0, 1.6
					}
				},
				{ GameDataKey.FORGE_LUCKY_TIMES, 6.0 },

				//需要1-4块相同等级的强化石，熔炼下一等级强化石，基础成功率为40%，熔炼的武器每增加一把，几率提升15%，
				{ GameDataKey.COMPOSE_ITEM_BASE_RATIO, 0.0},
				{ GameDataKey.COMPOSE_ITEM_ADD_RATIO,  0.25},

				//熔炼绿色、蓝色、粉色、橙、紫色的累计概率
				{ GameDataKey.COMPOSE_WEAPON_COLOR_RATIO, new double[]{0.25, 0.25, 0.20, 0.10, 0.05}},
				
				{ GameDataKey.CRAFT_DIAMOND_RATIO, new double[]{0.1, 0.2, 0.3, 0.4, 0.5}},

				//将1-5级合成石合成到各个武器上的成功率
				{ GameDataKey.CRAFT_EQUIP_STONE_RATIO,
					new double[]{
						1.0, 0.8, 0.5, 0.2, 0.1
						//1.0, 1.0, 1.0, 1.0, 1.0
					}
				},

				{ GameDataKey.CRAFT_STONE_LUCK, 20001},
				{ GameDataKey.CRAFT_STONE_DEFEND, 20002},
				{ GameDataKey.CRAFT_STONE_AGILITY, 20003},
				{ GameDataKey.CRAFT_STONE_ATTACK, 20004},
				{ GameDataKey.CRAFT_STONE_STRENGTH, 20005},
				{ GameDataKey.CRAFT_STONE_LUCKY1, 24002},
				{ GameDataKey.CRAFT_STONE_LUCKY2, 24004},
				{ GameDataKey.CRAFT_STONE_GOD, 24001},
				{ GameDataKey.CRAFT_STONE_DIAMOND, 20031},
				{ GameDataKey.CRAFT_STONE_CRYSTAL, 20041},
				{ GameDataKey.BOX_LEVELUP_TYPEID, 25011},

				{ GameDataKey.CRAFT_FAILURE_LEVEL_DOWN, 5},
				{ GameDataKey.CRAFT_FAILURE_VIP_LEVEL_DOWN, 9},

				{ GameDataKey.CRAFT_STONE_LUCKY_TYPEID, 24002},
				{ GameDataKey.CRAFT_STONE_GOD, 24001},
				{ GameDataKey.CRAFT_STONE_FUNC, 21001},
				{ GameDataKey.CRAFT_STONE_FUNC_LUCK, 21001},
				{ GameDataKey.CRAFT_STONE_FUNC_DEFEND, 21002},
				{ GameDataKey.CRAFT_STONE_FUNC_AGILITY, 21003},
				{ GameDataKey.CRAFT_STONE_FUNC_ATTACK, 21004},
				{ GameDataKey.CRAFT_STONE_FUNC_STRENGTH, 21005},
				{ GameDataKey.CRAFT_STONE_RING, 21006},
				
				{ GameDataKey.ROOM_MATCH_POWER, 2000},
				{ GameDataKey.ROOM_MATCH_LEVEL, 5},
				{ GameDataKey.ROOM_MAX_USER, 4},
				
				//5 seconds is for test purpose
				{ GameDataKey.ROOM_READY_TIMEOUT,  5000},
				{ GameDataKey.ROOM_UNFULL_TIMEOUT, 10000},
				{ GameDataKey.ROOM_JOIN_TIMEOUT, 15000},
				
				{ GameDataKey.DELAY_AGLITY_BASE, 100},
				{ GameDataKey.DELAY_ROLE_ATTACK, 100},
				{ GameDataKey.DELAY_POWER,  0},
				{ GameDataKey.DELAY_ROLE_SAVE, 100},
				{ GameDataKey.DELAY_ROLE_FLY, 55},
				{ GameDataKey.DELAY_TOOL_AttackTwoMoreTimes, 40},
				{ GameDataKey.DELAY_TOOL_AttackThreeBranch, 40},
				{ GameDataKey.DELAY_TOOL_AttackOneMoreTimes, 40},
				{ GameDataKey.DELAY_TOOL_HurtAdd50, 25},
				{ GameDataKey.DELAY_TOOL_HurtAdd40, 20},
				{ GameDataKey.DELAY_TOOL_HurtAdd30, 15},
				{ GameDataKey.DELAY_TOOL_HurtAdd20, 10},
				{ GameDataKey.DELAY_TOOL_HurtAdd10, 5},
				{ GameDataKey.DELAY_TOOL_RECOVER, 55},
				{ GameDataKey.DELAY_TOOL_ALLRECOVER, 60},
				{ GameDataKey.DELAY_TOOL_HIDDEN, 20},
				{ GameDataKey.DELAY_TOOL_ALLHIDDEN, 55},
				{ GameDataKey.DELAY_TOOL_WIND, 20},
				{ GameDataKey.DELAY_TOOL_ICE, 55},
				{ GameDataKey.DELAY_TOOL_FLY, 55},
				{ GameDataKey.DELAY_TOOL_GUIDE, 45},
				{ GameDataKey.DELAY_TOOL_ENERGY, 45},
				{ GameDataKey.DELAY_TOOL_ATOM, 40},
				{ GameDataKey.DELAY_TOOL_NOHOLE, 20},
				{ GameDataKey.THEW_ROLE_MOVE, 5},
				{ GameDataKey.THEW_ROUND_PERCENT, 0.2},
				{ GameDataKey.THEW_POWER,  0},
				{ GameDataKey.THEW_TOOL_AttackTwoMoreTimes, 200},
				{ GameDataKey.THEW_TOOL_AttackThreeBranch,  80},
				{ GameDataKey.THEW_TOOL_AttackOneMoreTimes, 110},
				{ GameDataKey.THEW_TOOL_HurtAdd50, 200},
				{ GameDataKey.THEW_TOOL_HurtAdd40, 160},
				{ GameDataKey.THEW_TOOL_HurtAdd30, 120},
				{ GameDataKey.THEW_TOOL_HurtAdd20, 85},
				{ GameDataKey.THEW_TOOL_HurtAdd10, 45},
				{ GameDataKey.THEW_TOOL_RECOVER,   150},
				{ GameDataKey.THEW_TOOL_ALLRECOVER, 200},
				{ GameDataKey.THEW_TOOL_HIDDEN, 50},
				{ GameDataKey.THEW_TOOL_ALLHIDDEN, 150},
				{ GameDataKey.THEW_TOOL_WIND, 50},
				{ GameDataKey.THEW_TOOL_ICE, 150},
				//Fly cost zero thew like fire
				{ GameDataKey.THEW_TOOL_FLY, 0},
				{ GameDataKey.THEW_TOOL_GUIDE, 200},
				{ GameDataKey.THEW_TOOL_ENERGY, 120},
				{ GameDataKey.THEW_TOOL_ATOM, 110},
				{ GameDataKey.THEW_TOOL_NOHOLE, 50},
				{ GameDataKey.PRICE_TOOL_AttackTwoMoreTimes, 12},
				{ GameDataKey.PRICE_TOOL_AttackThreeBranch, 12},
				{ GameDataKey.PRICE_TOOL_AttackOneMoreTimes, 12},
				{ GameDataKey.PRICE_TOOL_HurtAdd50, 25},
				{ GameDataKey.PRICE_TOOL_HurtAdd40, 23},
				{ GameDataKey.PRICE_TOOL_HurtAdd30, 21},
				{ GameDataKey.PRICE_TOOL_HurtAdd20, 20},
				{ GameDataKey.PRICE_TOOL_HurtAdd10, 15},
				{ GameDataKey.PRICE_TOOL_RECOVER, 18},
				{ GameDataKey.PRICE_TOOL_ALLRECOVER, 20},
				{ GameDataKey.PRICE_TOOL_HIDDEN, 10},
				{ GameDataKey.PRICE_TOOL_ALLHIDDEN, 12},
				{ GameDataKey.PRICE_TOOL_WIND, 5},
				{ GameDataKey.PRICE_TOOL_ICE, 10},
				{ GameDataKey.PRICE_TOOL_FLY, 10},
				{ GameDataKey.PRICE_TOOL_GUIDE, 15},
				{ GameDataKey.PRICE_TOOL_ENERGY, 20},
				{ GameDataKey.PRICE_TOOL_ATOM, 12},
				{ GameDataKey.PRICE_TOOL_NOHOLE, 12},
				
				{ GameDataKey.TOOL_RECOVER_VALUE, 500},
				{ GameDataKey.TOOL_ALL_RECOVER_VALUE, 300},
				{ GameDataKey.TOOL_ENERGY_VALUE, 50},
				{ GameDataKey.TOOL_ICED_VALUE, 3},
				{ GameDataKey.TOOL_HIDDEN_VALUE, 3},
				{ GameDataKey.TOOL_POWER_VALUE, 1.5},
				
				{ GameDataKey.SELL_TOOL_DISCOUNT, 50},
				{ GameDataKey.SELL_GOOD_DISCOUNT, 50},
				
				{ GameDataKey.PRICE_CRAFT_COMPOSE,  200},
				{ GameDataKey.PRICE_CRAFT_FORGE,    200},
				{ GameDataKey.PRICE_CRAFT_TRANSFER, 500},
				
				{ GameDataKey.USER_ONLINE_REWARD_STEP,
					new String[] {
						"00:30", "07:30", "11:30", "14:30", "16:30", "18:30", "20:30", "22:30"
					}
				},
			  //VIP
				{ GameDataKey.NORMAL_SHOP_DISCOUNT, 100},
				{ GameDataKey.VIP_SHOP_DISCOUNT, 100},
				{ GameDataKey.VIP_CHARGE_DISCOUNT, 10.0},
				//Luck
				{ GameDataKey.LUCK_BASE, 10000},
				{ GameDataKey.AGILITY_BASE, 10000},
				//Reward
				{ GameDataKey.REWARD_GOLDEN_LIST, new int[]{50, 100, 150, 200, 250, 300, 350, 400, 450, 500}},
				{ GameDataKey.REWARD_YUANBAO_LIST, new int[]{1, 1, 2, 3, 4, 5}},

				{ GameDataKey.MAIL_MAX_COUNT, 150},
				{ GameDataKey.MAIL_EXPIRE_SECONDS, 86400*30},
				{ GameDataKey.MAIL_MAX_SUBJECT, 40},
				{ GameDataKey.MAIL_MAX_CONTENT, 2000},
				{ GameDataKey.CHAT_USER_COOLDOWN, 15000},
				{ GameDataKey.CHALLENGE_USER_COOLDOWN, 15000},
				{ GameDataKey.SMALL_SPEAKER_ID, "26001"},
				
			  //颜色武器的基础能力加成值
				{ GameDataKey.WEAPON_COLOR_GREEN_RATIO,  1.1f},
				{ GameDataKey.WEAPON_COLOR_BLUE_RATIO,   1.25f},
				{ GameDataKey.WEAPON_COLOR_PINK_RATIO,   1.5f},
				{ GameDataKey.WEAPON_COLOR_ORANGE_RATIO, 2.0f},
				{ GameDataKey.WEAPON_COLOR_PURPLE_RATIO, 3.0f},
				
				{ GameDataKey.EMAIL_REWARD_ITEMID, "26002"},
				{ GameDataKey.WEAPON_WARRANTY_UNIT, 5},
				
				//Secure Limit
				{ GameDataKey.SECURE_LIMIT_EXP_DAILY,    1000000},
				{ GameDataKey.SECURE_LIMIT_GOLDEN_DAILY,  100000000},
				{ GameDataKey.SECURE_LIMIT_YUANBAO_DAILY, 200000},
				
				//体力值上限
				{ GameDataKey.ROLE_ACTION_LIMIT, 200},
				{ GameDataKey.ROLE_ACTION_GAIN_HOURLY, 10},
				{ GameDataKey.ROLE_ACTION_GAIN_LEVELUP, 100},
				
				{ GameDataKey.TREASURE_HUNT_FREE_COUNT, 5},
				{ GameDataKey.TREASURE_HUNT_NORMAL_PRICE, 1},
				{ GameDataKey.TREASURE_HUNT_ADVANCE_PRICE, 4},
				{ GameDataKey.TREASURE_HUNT_PRO_PRICE, 8},
				{ GameDataKey.TREASURE_HUNT_FRESH_MILLIS, 300000},
				
				//VIP
				/**
					27000	VIP1专属礼包
					27001	VIP2专属礼包
					27002	VIP3专属礼包
					27003	VIP4专属礼包
					27004	VIP5专属礼包
					27005	VIP6专属礼包
					27006	VIP7专属礼包
					27007	VIP8专属礼包
					27008	VIP9专属礼包
					27009	VIP10专属礼包
				 */
				{ GameDataKey.VIP_GIFT_BOX_ID, 
					new String[]{"27000","27001","27002","27003","27004","27005","27006","27007","27008","27009"}
				}, 
				{
					GameDataKey.VIP_BAG_SPACE, 
					new int[]{68, 76, 84, 100, 124, 140, 160, 180, 260, 460}
				},
				{
					GameDataKey.VIP_OFFLINE_EXP, 
					new int[]{0, 0, 0, 0, 0, 300, 550, 800, 1000, 2000}
				},
				{
					GameDataKey.VIP_OFFLINE_MAX_EXP, 
					new int[]{0, 0, 0, 0, 0, 3000, 5500, 8000, 10000, 20000}
				},
				{
					GameDataKey.VIP_PVP_GOOD_PROP, 
					new double[]{0.03, 0.03, 0.03, 0.05, 0.05, 0.1, 0.1, 0.1, 0.15, 0.15}
				},
				{
					GameDataKey.VIP_PVE_GOOD_PROP, 
					new double[]{0.05, 0.05, 0.05, 0.08, 0.08, 0.15, 0.15, 0.15, 0.20, 0.20}
				},
				{
					GameDataKey.VIP_BUY_ROLEACTION,
					new int[]{0, 0, 0 , 6, 21, 21, 46, 46, 46, 46} 
				},
				{
					GameDataKey.VIP_BUY_CAISHEN,
					new int[]{30, 50, 70, 90, 300, 500, 1000, 2000, 3000, 6000} 
				},
				{
					GameDataKey.VIP_CAN_TREASURE_HUNT,
					new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1} 
				},
				{
					GameDataKey.VIP_CAN_TRANSFER_LEVEL,
					new int[]{30, 40, 50, 60, 70, 80, 90, 100, 100, 100} 
				},
				//绿色、蓝色、粉色、橙色、紫色
				{
					GameDataKey.VIP_CAN_TRANSFER_COLOR,
					new int[]{0, 0, 0, 0, 0, 3, 4, 4, 5, 5} 
				},
				{
					GameDataKey.VIP_BATTLE_EXP_RATIO,
					new double[]{0.1, 0.15, 0.2, 0.3, 0.4, 0.6, 0.8, 1.0, 1.0, 1.5},
				},
				{
					GameDataKey.VIP_STRENGTH_RATIO,
					new double[]{0, 0, 0, 0, 0, 0.05, 0.15, 0.25, 0.4, 0.5}
				},
				{
					GameDataKey.CRAFT_TRANSFER_RATIO,
					/**
					 *     VIP0	VIP1	VIP2	VIP3	VIP4	VIP5	VIP6	VIP7	VIP8	VIP9	VIP10
					 * 强1
					 * 强2
					 * ......
					 */
					new double[][]{
							new double[]{1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00,1.00},
							new double[]{0.91,0.93,0.94,0.95,0.96,0.97,0.98,1.00,1.00,1.00,1.00},
							new double[]{0.82,0.86,0.88,0.90,0.92,0.94,0.96,1.00,1.00,1.00,1.00},
							new double[]{0.73,0.79,0.82,0.85,0.88,0.91,0.94,1.00,1.00,1.00,1.00},
							new double[]{0.64,0.72,0.76,0.80,0.84,0.88,0.92,1.00,1.00,1.00,1.00},
							new double[]{0.55,0.65,0.70,0.75,0.80,0.85,0.90,1.00,1.00,1.00,1.00},
							new double[]{0.46,0.58,0.64,0.70,0.76,0.82,0.88,1.00,1.00,1.00,1.00},
							new double[]{0.37,0.51,0.58,0.65,0.72,0.79,0.86,1.00,1.00,1.00,1.00},
							new double[]{0.28,0.44,0.52,0.60,0.68,0.76,0.84,1.00,1.00,1.00,1.00},
							new double[]{0.19,0.37,0.46,0.55,0.64,0.73,0.82,1.00,1.00,1.00,1.00},
							new double[]{0.10,0.30,0.40,0.50,0.60,0.70,0.80,1.00,1.00,1.00,1.00},
							new double[]{0.01,0.23,0.34,0.45,0.56,0.67,0.78,1.00,1.00,1.00,1.00},
							new double[]{0.00,0.16,0.28,0.40,0.52,0.64,0.76,1.00,1.00,1.00,1.00},
							new double[]{0.00,0.09,0.22,0.35,0.48,0.61,0.74,1.00,1.00,1.00,1.00},
							new double[]{0.00,0.02,0.16,0.30,0.44,0.58,0.72,1.00,1.00,1.00,1.00},
					}
				},
				//货币
				{ GameDataKey.YUANBAO_TO_GOLDEN_RATIO, 100},
				
				{ GameDataKey.NEWBIE_BATTLE_GUIDE, 10},
				
				//计费渠道的KEY
				//Changyou
				{ GameDataKey.CHARGE_DANGLE_KEY, "8ChP7fj8" },
				{ GameDataKey.CHARGE_XIAOMI_KEY, "485e592a-a9f6-3905-6295-507fd0867b67" },
				//Xiaomi
				
				
				{ GameDataKey.ITEM_CHANGE_NAME_ID, "30000" },
				
				{ GameDataKey.EMAIL_SMTP, "xinqihd.com" },
				
				{ GameDataKey.LOGIN_MAJOR_VERSION, 1 },
				{ GameDataKey.LOGIN_MINOR_VERSION, 8 },
				
				{ GameDataKey.BOSS_SINGLE_EXPIRE, 3600 },
				
				//公会设置
				{ GameDataKey.GUILD_CREATE_GOLDEN, 500000 },
				{ 
					/**
					 * shop(0),
					 * craft(1),
					 * storage(2),
					 * guild(3),
					 * ability(4),
					 */
					GameDataKey.GUILD_LEVEL_WEALTH,  
					new int[][]{
							//shop(0),
							{3000, 8000, 50000, 150000, 300000},
							//craft
							{3000, 8000, 50000, 150000, 300000},
							//storage
							{3000, 8000, 50000, 150000, 300000},
						  //guild
							{0, 10000, 77000, 231000, 462000},
							//ability
							{3000, 8000, 50000, 150000, 300000},
							/*
							//ab_attack(10, 5),
							{3000, 8000, 50000, 150000, 300000},
							//ab_agility(11, 6),
							{3000, 8000, 50000, 150000, 300000},
							//ab_lucky(12, 7),
							{3000, 8000, 50000, 150000, 300000},
							//ab_defend(13, 8),
							{3000, 8000, 50000, 150000, 300000},
							//ab_blood(14, 9),
							{3000, 8000, 50000, 150000, 300000},
							//ab_treasure(15, 10),
							{3000, 8000, 50000, 150000, 300000},
							//ab_pray(16, 11);
							{3000, 8000, 50000, 150000, 300000},
							*/
					}
				},
				{
					GameDataKey.GUILD_ABILITY_CREDIT, new int[] {100, 600, 1155, 2310, 3850} 
				},
				{ 
					GameDataKey.GUILD_LEVEL_GOLDEN,  new int[]{500000, 800000, 1000000, 1500000, 2000000}
				},
				{ 
					GameDataKey.GUILD_LEVEL_MAXCOUNT,  new int[]{20, 40, 60, 80, 100}
				},
				{ 
					GameDataKey.GUILD_LEVEL_EXPRATIO,  
					new double[]{0.0, 0.0, 0.0, 0.0, 0.0},
					//new double[]{0.0, 0.1, 0.15, 0.2, 0.3},
				},
				{ 
					GameDataKey.GUILD_LEVEL_MANAGER,
					//会长	副会长	官员	精英	会员
					new int[][]{
							//Level 1
							{1, 1, 3, 10, Integer.MAX_VALUE},
						  //Level 2
							{1, 1, 3, 12, Integer.MAX_VALUE},
						  //Level 3
							{1, 1, 4, 15, Integer.MAX_VALUE},
						  //Level 4
							{1, 1, 4, 20, Integer.MAX_VALUE},
						  //Level 5
							{1, 2, 5, 30, Integer.MAX_VALUE},
					}
				},
				{ 
					GameDataKey.GUILD_OPFEE,  new int[]{7700, 15400, 23100, 30800, 38500}
				},
				{ 
					GameDataKey.GUILD_STORAGE_SIZE,  new int[]{40, 80, 140, 220, 320}
				},
				//设施各等级所需贡献度
				{
					GameDataKey.GUILD_FACILITY_MIN_CREDIT,  
					new int[][]{
							//公会商城
							{100, 770, 1540, 3850, 9625},
							//铁匠铺
							{120, 800, 2000, 4000, 10000},
							//公会仓库
							{85, 650, 1300, 3500, 8500},
					}
				},
				//公会设施升级后的冷却时间(秒)
				{ 
					GameDataKey.GUILD_FACILITY_COOLDOWN,  new int[]{24*3600, 48*3600, 120*3600, 192*3600, 360*3600}
				},
				//1小时折合10元宝，每秒折合0.0027778元宝
				{ GameDataKey.GUILD_FACILITY_COOLDOWN_YUANBAO, 60/3600.0 },
				//捐献1元宝=5贡献=10财富
				{ GameDataKey.GUILD_YUANBAO_CREDIT, 5.0 },
				{ GameDataKey.GUILD_YUANBAO_WEALTH, 10.0 },
				//捐献100000金币=1贡献=2财富
				{ GameDataKey.GUILD_GOLDEN_CREDIT, 0.00001 },
				{ GameDataKey.GUILD_GOLDEN_WEALTH, 0.00002 },

				//公会技能升级效果
				{
					GameDataKey.GUILD_CRAFT_STRENGTH,  new double[]{0.02, 0.04, 0.06, 0.08, 0.10}
				},
				{ GameDataKey.GUILD_ABILITY_ATTACK, new double[]{0.01, 0.02, 0.03, 0.04, 0.05}},
				{ GameDataKey.GUILD_ABILITY_DEFEND, new double[]{0.01, 0.02, 0.03, 0.04, 0.05}},
				{ GameDataKey.GUILD_ABILITY_AGILITY, new double[]{0.01, 0.02, 0.03, 0.04, 0.05}},
				{ GameDataKey.GUILD_ABILITY_LUCKY, new double[]{0.01, 0.02, 0.03, 0.04, 0.05}},
				{ GameDataKey.GUILD_ABILITY_BLOOD, new double[]{0.01, 0.02, 0.03, 0.04, 0.05}},
				{ GameDataKey.GUILD_ABILITY_TREASURE, new double[]{1, 2, 3, 4, 5}},
				{ GameDataKey.GUILD_ABILITY_PRAY, new int[]{10000, 20000, 30000, 40000, 50000}},
				
		};
		GameDataManager manager = GameDataManager.getInstance();
		int i = 0;
		for ( Object[] value : values ) {
			GameDataKey gameDataKey = (GameDataKey)value[0];
//			logger.debug("//{}", gameDataKey.getDesc());
//			logger.debug("optional int32 {} = {} [default = {}];", 
//					new Object[]{gameDataKey.getKey(), i++, value[1]});
			System.out.println(gameDataKey.getKey()+"\t"+gameDataKey.getDesc()+"\t"+manager.getGameDataAsString(gameDataKey));
			manager.setValueToDatabase((GameDataKey)value[0], value[1], value[1]);
		}
	}
	
	/**
	 * The installer save default config data into database.
	 * @param args
	 */
	public static void main(String ...args) {
		GameDataManager.getInstance().saveDefaultValue();
	}
}
