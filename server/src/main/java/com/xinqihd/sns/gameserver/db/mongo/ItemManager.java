package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.proto.XinqiBseItem.BseItem;
import com.xinqihd.sns.gameserver.proto.XinqiBseZip.BseZip;
import com.xinqihd.sns.gameserver.util.IOUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class ItemManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(ItemManager.class);
	
	private static final String COLL_NAME = "items";
	
	private static final String INDEX_NAME = "_id";
	
	private static ConcurrentHashMap<String, ItemPojo> dataMap = 
			new ConcurrentHashMap<String, ItemPojo>();
	
	private static ConcurrentHashMap<String, ItemPojo> itemTypeLevelMap = 
			new ConcurrentHashMap<String, ItemPojo>();

	//The stone types 
	private static final HashSet<String> stoneTypes = new HashSet<String>(10);
	
	private static final ItemManager instance = new ItemManager();
	
	public static String luckyStone15 = "24002";
	public static String luckyStone25 = "24004";
	public static String winStone     = "24005";
	public static String godStoneId   = "24001";
	
	public static String luckStoneId     = "20001";
	public static String defendStoneId   = "20002";
	public static String agilityStoneId  = "20003";
	public static String attackStoneId   = "20004";
	public static String strengthStoneId = "20005";
	//黄钻石Lv1
	public static String diamondStoneId = "20031";
	//水晶石
	public static String crystalStoneId = "20041";
	/*
		<item id="21001" typeid="21001" lv="0" icon="Prop0012" name="水神石炼化符" info="炼化水神石的必需品。" />
		<item id="21002" typeid="21001" lv="0" icon="Prop0013" name="土神石炼化符" info="炼化土神石的必需品。" />
		<item id="21003" typeid="21001" lv="0" icon="Prop0010" name="风神石炼化符" info="炼化风神石的必需品。" />
		<item id="21004" typeid="21001" lv="0" icon="Prop0011" name="火神石炼化符" info="炼化火神石的必需品。" />
		<item id="21005" typeid="21001" lv="0" icon="Prop0017" name="强化石炼化符" info="炼化强化石的必需品，只有名称与等级完全相同的宝石才可以炼化。" />
	 */
	public static String luckyFuncId = "21001";
	public static String defendFuncId = "21002";
	public static String agilityFuncId = "21003";
	public static String attackFuncId = "21004";
	public static String strengthFuncId = "21005";
	public static String godFuncId = "21006";
	/*
		20000	26004	绿色熔炼符	   21001
		20001	26005	蓝色熔炼符	   21001
		20002	26006	粉色熔炼符	   21001
		20003	26007	橙色熔炼符	   21001
		20004	26008	武器熔炼符	   21001
		20005	26009	装备熔炼符	   21001
		20007	26011	精良装备熔炼符 21001
		20006	26010	精良武器熔炼符 21001
	 */
	public static String greenColorFuncId = "26004";
	public static String blueColorFuncId = "26005";
	public static String pinkColorFuncId = "26006";
	public static String orangeColorFuncId = "26007";
	public static String purpleColorFuncId = "30018";
	
	//26008	武器熔炼符
	public static String weaponFuncId = "26008";
	//26009	装备熔炼符
	public static String equipFuncId = "26009";
	//26010	精良武器熔炼符
	public static String weaponProFuncId = "26010";
	//26011	精良装备熔炼符
	public static String equipProFuncId = "26011";
	
	private HashMap<Locale, byte[]> compressLuaScriptMap = 
			new HashMap<Locale, byte[]>(); 

	/**
	 * Get the singleton instance
	 * @return
	 */
	public static ItemManager getInstance() {
		return instance;
	}
	
	ItemManager() {
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
		luckyStone15 = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 24002));
		luckyStone25 = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY2, 24004));
		godStoneId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_GOD, 24001));
		
		//We do not count godStone as a stone to reward
		//stoneTypes.add(godStoneId);
		strengthStoneId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_STRENGTH, 20005));
		stoneTypes.add(strengthStoneId);
		luckStoneId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCK, 20001));
		stoneTypes.add(luckStoneId);
		defendStoneId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_DEFEND, 20002));
		stoneTypes.add(defendStoneId);
		agilityStoneId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_AGILITY, 20003));
		stoneTypes.add(agilityStoneId);
		attackStoneId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_ATTACK, 20004));
		stoneTypes.add(attackStoneId);
		
		luckyFuncId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 21001));
		defendFuncId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 21002));
		agilityFuncId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 21003));
		attackFuncId = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 21004));
		strengthFuncId  = String.valueOf(GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 21005));
		
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);

		for ( Locale locale : GameResourceManager.getInstance().getAllLocales() ) {
			StringBuilder buf = new StringBuilder(26000);
			buf.append("items= {\n");
			for ( DBObject obj : list ) {
				ItemPojo item = (ItemPojo)MongoDBUtil.constructObject(obj);
				dataMap.put(item.getId(), item);

				String itemTypeAndLevel = StringUtil.concat(item.getTypeId(), item.getLevel());
				if ( !itemTypeLevelMap.contains(itemTypeAndLevel) ) {
					itemTypeLevelMap.put(itemTypeAndLevel, item);
				} else {
					logger.debug("The item {} has duplicate type and level.", item);
				}
				buf.append(item.toLuaString(locale));
				//logger.debug("Load item id {} name {} from database.", item.getId(), item.getName());
			}
			buf.append("}\n");
			byte[] compressedLuaScript = IOUtil.compressStringZlib(buf.toString());
			compressLuaScriptMap.put(locale, compressedLuaScript);
		}
						
		logger.debug("Load total {} items from database.", dataMap.size());
	}
	
	/**
	 * Get the given item by its id.
	 * @param id
	 * @return
	 */
	public ItemPojo getItemById(String id) {
		 return dataMap.get(id);
	}
	
	/**
	 * Get the ItemPojo by its typeid and level.
	 * @param typeId
	 * @param level
	 * @return
	 */
	public ItemPojo getItemByTypeIdAndLevel(String typeId, int level) {
		String itemTypeAndLevel = StringUtil.concat(typeId, level);
		return itemTypeLevelMap.get(itemTypeAndLevel);
	}
	
	/**
	 * Get the underlying item collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<ItemPojo> getItems() {
		return dataMap.values();
	}
	
	/**
	 * Get all the stone types.
	 * @return
	 */
	public Collection<String> getStoneTypes() {
		return stoneTypes;
	}
	
	/**
	 * Construct Protobuf's BseEquipment data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public BseItem toBseItem() {
		BseItem.Builder builder = BseItem.newBuilder();
		for ( ItemPojo itemPojo : dataMap.values() ) {
			builder.addItems(itemPojo.toItemData());
		}
		return builder.build();
	}
	
	/**
	 * Return task list as zip format 
	 * @return
	 */
	public BseZip toBseZip() {
		Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
		BseZip.Builder zipBuilder = BseZip.newBuilder();
		zipBuilder.setName("BseItem");
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
