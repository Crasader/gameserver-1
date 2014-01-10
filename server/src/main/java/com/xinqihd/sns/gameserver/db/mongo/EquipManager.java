package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.RewardLevelPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseEquipment.BseEquipment;
import com.xinqihd.sns.gameserver.proto.XinqiBseZip.BseZip;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.IOUtil;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class EquipManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(EquipManager.class);
	
	private static final String COLL_NAME = "equipments_new";
	
	private static final String INDEX_NAME = "_id";
	
	private ConcurrentHashMap<String, WeaponPojo> dataMap = 
			new ConcurrentHashMap<String, WeaponPojo>();

	private ArrayList<WeaponPojo> femaleWeapons = new ArrayList<WeaponPojo>();
	
	private ArrayList<WeaponPojo> maleWeapons = new ArrayList<WeaponPojo>();
	
	private HashMap<String, ArrayList<WeaponPojo>> typeWeapons = 
			new HashMap<String, ArrayList<WeaponPojo>>();
	
	private HashMap<EquipType, ArrayList<WeaponPojo>> slotWeapons =
			new HashMap<EquipType, ArrayList<WeaponPojo>>();
	
	private HashMap<Integer, ArrayList<WeaponPojo>> qualityWeapons =
			new HashMap<Integer, ArrayList<WeaponPojo>>();

	//The lua script version of all weapons for compressing.
	private HashMap<Locale, byte[]> compressLuaScriptMap = 
			new HashMap<Locale, byte[]>(); 
	
	private static final EquipManager instance = new EquipManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static EquipManager getInstance() {
		return instance;
	}
	
	EquipManager() {
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
		DBObject sortField = MongoDBUtil.createDBObject("_id", 1);
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(
				null, databaseName, namespace, COLL_NAME, null, sortField);
		dataMap.clear();
		femaleWeapons.clear();
		maleWeapons.clear();
		slotWeapons.clear();
		typeWeapons.clear();
		qualityWeapons.clear();
		
		for ( DBObject obj : list ) {
			WeaponPojo weapon = (WeaponPojo)MongoDBUtil.constructObject(obj);
			//Calculate the damage and skin dynamicly
			int damage = (int)EquipCalculator.calculateWeaponPower(weapon.getAddAttack(), 0, 0, 0, 0, 0, 0, 0);
			int skin = (int)EquipCalculator.calculateWeaponPower(0, weapon.getAddDefend(), 0, 0, 0, 0, 0, 0);
			weapon.setAddDamage(damage);
			weapon.setAddSkin(skin);
			dataMap.put(weapon.getId(), weapon);
			if ( weapon.getSex() == Gender.FEMALE ) {
				femaleWeapons.add(weapon);
			} else if ( weapon.getSex() == Gender.MALE ) {
				maleWeapons.add(weapon);
			} else if ( weapon.getSex() == Gender.ALL ) {
				femaleWeapons.add(weapon);
				maleWeapons.add(weapon);
			}
			ArrayList<WeaponPojo> weapons = slotWeapons.get(weapon.getSlot());
			if (weapons == null) {
				weapons = new ArrayList<WeaponPojo>();
				slotWeapons.put(weapon.getSlot(), weapons);
			}
			weapons.add(weapon);
			
			ArrayList<WeaponPojo> typeWeaponList = typeWeapons.get(weapon.getTypeName());
			if ( typeWeaponList == null ) {
				typeWeaponList = new ArrayList<WeaponPojo>(); 
			}
			typeWeaponList.add(weapon);
			typeWeapons.put(weapon.getTypeName(), typeWeaponList);
			//logger.debug("Load weapon id {} name {} from database.", weapon.getId(), weapon.getName());
			
			ArrayList<WeaponPojo> qualityWeaponList = qualityWeapons.get(weapon.getQuality());
			if ( qualityWeaponList == null ) {
				qualityWeaponList = new ArrayList<WeaponPojo>(); 
			}
			qualityWeaponList.add(weapon);
			qualityWeapons.put(weapon.getQuality(), qualityWeaponList);
		}
		
		for ( Locale locale : GameResourceManager.getInstance().getAllLocales() ) {
			StringBuilder luaBuffer = new StringBuilder(10000);
			luaBuffer.append("weapons = {\n");
			for ( WeaponPojo weapon: dataMap.values() ) {
				luaBuffer.append(weapon.toLuaString(locale));
			}
			luaBuffer.append("}\n");
			byte[] compressedLuaScript = IOUtil.compressStringZlib(luaBuffer.toString());
			compressLuaScriptMap.put(locale, compressedLuaScript);
		}
		logger.debug("Load total {} weapons from database.", dataMap.size());
	}
	
	/**
	 * Get the given weapon by its id.
	 * @param id
	 * @return
	 */
	public WeaponPojo getWeaponById(String id) {
		 return dataMap.get(id);
	}
		
	/**
	 * Get the underlying Weapon collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<WeaponPojo> getWeapons() {
		return dataMap.values();
	}
	
	/**
	 * Get the weapons list by its slot type ( EquipType ).
	 * @param equipType
	 * @return
	 */
	public Collection<WeaponPojo> getWeaponsBySlot(EquipType equipType) {
		return slotWeapons.get(equipType);
	}
	
	/**
	 * Get a random weapon with given quality and equipType(slot)
	 * 
	 * @param user
	 * @return
	 */
	public final WeaponPojo getRandomWeapon(User user, EquipType equipType, int quality) {
		if ( user != null ) {
			int userLevel = user.getLevel();
			return getRandomWeapon(userLevel, equipType, quality);
		}
		return null;
	}
	
	/**
	 * Get a random weapon with given quality and equipType(slot)
	 * 
	 * @param user
	 * @return
	 */
	public final WeaponPojo getRandomWeapon(int userLevel, EquipType equipType, int quality) {
		ArrayList coll = (ArrayList)getWeaponsBySlot(equipType);
		if ( coll != null ) {
			int size = coll.size();
			for ( int i=0; i<size; i++ ) {
				int index = (int)(MathUtil.nextDouble()*size);
				WeaponPojo equip = (WeaponPojo)coll.get(index);
				if ( equip.getSlot()==equipType 
						&& equip.getQuality() == quality 
						&& equip.isCanBeRewarded() 
						&& !equip.isUsedByBoss()) {
					String typeName = equip.getTypeName();
					return getWeaponByTypeNameAndUserLevel(typeName, userLevel);
				}
			}
		}
		return null;
	}
	
	/**
	 * This method is used for TreasureHuntGen and AI UserCreate logic.
	 * 
	 * @param user
	 * @param equipType
	 * @param quality
	 * @return
	 */
	public final WeaponPojo getRandomWeaponWithoutCheckReward(Gender gender, int userLevel, EquipType equipType, int quality) {
		ArrayList coll = (ArrayList)getWeaponsBySlot(equipType);
		if ( coll != null ) {
			int size = coll.size();
			for ( int i=0; i<size; i++ ) {
				int index = (int)(MathUtil.nextDouble()*size);
				WeaponPojo equip = (WeaponPojo)coll.get(index);
				if ( equip.getSlot()==equipType 
						&& (equip.getSex() == Gender.ALL || equip.getSex() == gender ) 
						&& equip.getQuality() == quality 
						&& !equip.isUsedByBoss()) {
					String typeName = equip.getTypeName();
					return getWeaponByTypeNameAndUserLevel(typeName, userLevel);
				}
			}
		}
		return null;
	}

	/**
	 * Get the weapons list for same type.
	 * @param typeName
	 * @return
	 */
	public List<WeaponPojo> getWeaponsByTypeName(String typeName) {
		return typeWeapons.get(typeName);
	}
	
	
	/**
	 * Get the weapons by typeName and user level.
	 * @param typeName
	 * @param userLevel
	 * @return
	 */
	public WeaponPojo getWeaponByTypeNameAndUserLevel(String typeName, int userLevel) {
		ArrayList<WeaponPojo> weapons = this.typeWeapons.get(typeName);
		if ( weapons == null ) {
			return null;
		}
		if ( userLevel < 0 ) {
			userLevel = 0;
		}
		/*
		WeaponPojo lastWeapon = null;
		for ( WeaponPojo weapon : weapons ) {
			if ( weapon.getUserLevel() > userLevel ) {
				break;
			} else {
				lastWeapon = weapon;
			}
		}*/
		//Use a fast algorithm.
		int index = userLevel / 10;
		if ( index >= weapons.size() ) {
			index = weapons.size() - 1;
		}
		WeaponPojo lastWeapon = null;
		try {
			lastWeapon = weapons.get(index);
		} catch (Exception e) {
			logger.warn("Failed to getWeaponByTypeNameAndUserLevel. {}", index);
		}
		return lastWeapon;
	}
	
	/**
	 * Get the weapon list by gender.
	 * @param gender
	 * @return
	 */
	/*
	public List<WeaponPojo> getWeaponsByGender(Gender gender) {
		if ( gender == Gender.FEMALE ) {
			return femaleWeapons;
		} if ( gender == Gender.MALE ) {
			return maleWeapons;
		}
		return null;
	}
	*/
	
	/**
	 * Get the most proper random weapon according to user's level and gender.
	 * 
	 * @param gender
	 * @param user
	 * @return
	 */
	public WeaponPojo getRandomWeaponByGenderAndLevel(Gender gender, User user) {
		return getRandomWeaponByGenderAndLevel(gender, user.getLevel());
	}
	
	/**
	 * Get the most proper random weapon according to user's level and gender.
	 * 
	 * @param gender
	 * @param userLevel
	 * @return
	 */
	public WeaponPojo getRandomWeaponByGenderAndLevel(Gender gender, int userLevel) {
		List<WeaponPojo> weapons = null;
		/**
		 * Make the gender has a little random
		 * 2013-03-06
		 */
		double d = MathUtil.nextDouble();
		if ( d < 0.3 ) {
			if ( d < 0.15 ) {
				weapons = femaleWeapons;
			} else {
				weapons = maleWeapons;
			}
		} else {
			switch ( gender ) {
				case FEMALE:
					weapons = femaleWeapons;
					break;
				case MALE:
					weapons = maleWeapons;
					break;
				default:
					weapons = maleWeapons;
					break;
			}
		}
		//weapons = dataMap.values();
		WeaponPojo weapon = null;
		int weaponCount = weapons.size();
		for ( int i=0; i<weaponCount; i++ ) {
			if ( weaponCount > 0 ) {
				int randomWeaponPsedoIndex = (int)(MathUtil.nextDouble() * weapons.size());
				WeaponPojo w = weapons.get(randomWeaponPsedoIndex%weaponCount);
 				RewardLevelPojo rewardPojo = RewardManager.getInstance().getRewardLevelPojoByTypeId(w.getTypeName());
				if ( rewardPojo != null && rewardPojo.isEnabled() ) {
					if ( rewardPojo.getMinLevel() <= userLevel ) {
						if ( rewardPojo.getRatio() < 1.0 ) {
							double r = MathUtil.nextDouble();
							if ( rewardPojo.getRatio() <= r ) {
								weapon = w;
								break;
							}
						} else {
							weapon = w;
							break;
						}
					}
				}
			}
		}
		if ( weapon != null ) {
			String typeName = weapon.getTypeName();
			weapons = this.getWeaponsByTypeName(typeName);
			for ( WeaponPojo w : weapons ) {
				if ( w.getUserLevel() > userLevel ) {
					break;
				} else {
					weapon = w;
				}
			}
		}
		return weapon;
	}
	
	/**
	 * Get the weapon by quality and user level
	 * @param gender
	 * @param userLevel
	 * @return
	 */
	public WeaponPojo getRandomWeaponByQualityAndLevel(int userLevel, int quality) {
		List<WeaponPojo> weapons = qualityWeapons.get(quality);
		WeaponPojo weapon = null;
		if ( weapons != null ) {
			int weaponCount = weapons.size();
			for ( int i=0; i<weaponCount; i++ ) {
				if ( weaponCount > 0 ) {
					int randomWeaponPsedoIndex = (int)(MathUtil.nextDouble() * weapons.size());
					WeaponPojo w = weapons.get(randomWeaponPsedoIndex%weaponCount);
					RewardLevelPojo rewardPojo = RewardManager.getInstance().getRewardLevelPojoByTypeId(w.getTypeName());
					if ( rewardPojo != null && rewardPojo.isEnabled() ) {
						if ( rewardPojo.getMinLevel() <= userLevel ) {
							if ( rewardPojo.getRatio() < 1.0 ) {
								double r = MathUtil.nextDouble();
								if ( rewardPojo.getRatio() <= r ) {
									weapon = w;
									break;
								}
							} else {
								weapon = w;
								break;
							}
						}
					}
				}
			}
			if ( weapon != null ) {
				String typeName = weapon.getTypeName();
				weapons = this.getWeaponsByTypeName(typeName);
				for ( WeaponPojo w : weapons ) {
					if ( w.getUserLevel() > userLevel ) {
						break;
					} else {
						weapon = w;
					}
				}
			}
		}
		return weapon;
	}
	
	/**
	 * Check if the user can equip the given Weapon.
	 * 
	 * @param user
	 * @param weaponId
	 * @return
	 */
	public boolean checkWeaponLevelForUser(User user, String weaponId) {
		WeaponPojo weapon = this.dataMap.get(weaponId);
		if ( weapon != null && user != null ) {
			if ( weapon.getUserLevel() <= user.getLevel() ) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Construct Protobuf's BseEquipment data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public BseEquipment toBseEquipment() {
		BseEquipment.Builder builder = BseEquipment.newBuilder();
		for ( WeaponPojo weaponPojo : dataMap.values() ) {
			builder.addWeapons(weaponPojo.toWeaponData());
		}
		return builder.build();
	}
	
	/**
	 * Compress this protocols
	 * @return
	 */
	public BseZip toBseZip() {
		Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
		BseZip.Builder zipBuilder = BseZip.newBuilder();
		zipBuilder.setName("BseEquipment");
		byte[] compressLuaScript = compressLuaScriptMap.get(locale);
		if ( compressLuaScript == null ) {
			logger.warn("Failed to find compressed lua script for locale:{}", locale);
			compressLuaScript = compressLuaScriptMap.get(GameResourceManager.DEFAULT_LOCALE);
		}
		ByteString bs = ByteString.copyFrom(compressLuaScript);
		zipBuilder.setPayload(bs);
		return zipBuilder.build();
	}
}
