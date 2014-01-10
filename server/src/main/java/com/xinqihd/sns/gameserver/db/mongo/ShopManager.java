package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmCallback;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyProp.BceBuyProp;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyProp.BuyInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBceLengthenIndate.LengthenIndate;
import com.xinqihd.sns.gameserver.proto.XinqiBseBuyProp.BseBuyProp;
import com.xinqihd.sns.gameserver.proto.XinqiBseExpireEquipments.ExpireInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseLengthenIndate.BseLengthenIndate;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo.BseRoleBattleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseSellProp.BseSellProp;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop.BseShop;
import com.xinqihd.sns.gameserver.proto.XinqiBseToolList.BseToolList;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public final class ShopManager extends AbstractMongoManager {
	
	public static final String REDIS_HOT_YUANBAO = "hot:yuanbao";
	public static final String REDIS_HOT_GOLDEN  = "hot:golden";

	private static final Logger logger = LoggerFactory.getLogger(ShopManager.class);
	
	private static final String COLL_NAME = "shops_new";
	
	private static final String INDEX_NAME = "_id";
	private static final String PROP_INFO_ID_NAME = "propInfoId";
	
	private static ConcurrentHashMap<String, ShopPojo> dataMap = 
			new ConcurrentHashMap<String, ShopPojo>();

	private static HashMap<String, Set<ShopPojo>> typesMap = 
			new HashMap<String, Set<ShopPojo>>();
	
	private static EnumMap<ShopCatalog, Set<ShopPojo>> catalogMap = 
			new EnumMap<ShopCatalog, Set<ShopPojo>>(ShopCatalog.class);
	
	private static EnumMap<MoneyType, Set<ShopPojo>> moneyMap = 
			new EnumMap<MoneyType, Set<ShopPojo>>(MoneyType.class);
	
	private static EnumMap<Gender, Set<ShopPojo>> genderMap = 
			new EnumMap<Gender, Set<ShopPojo>>(Gender.class);
	
	private static EnumMap<BuffToolType, Integer> buffToolPrice = 
			new EnumMap<BuffToolType, Integer>(BuffToolType.class);
	
	//PropData id -- ShopPojos
	private static ConcurrentHashMap<String, Set<ShopPojo>> propIdMap = 
			new ConcurrentHashMap<String, Set<ShopPojo>>();
	
	private static float buffToolSellDiscount = 0.85f;
	private static float goodSellDiscount = 0.85f;
	
	private static final ShopManager instance = new ShopManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static ShopManager getInstance() {
		return instance;
	}
	
	ShopManager() {
		super(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		//Create an index on "PROP_INFO_ID_NAME" if it does not exist.
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_NAME, PROP_INFO_ID_NAME, false);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		
		synchronized ( dataMap ) {
			dataMap.clear();
			propIdMap.clear();
			//sell purpose
			catalogMap.clear();
			moneyMap.clear();
			buffToolPrice.clear();
			typesMap.clear();
			genderMap.clear();
			
			for ( DBObject obj : list ) {
				ShopPojo shop = (ShopPojo)MongoDBUtil.constructObject(obj);
				dataMap.put(shop.getId(), shop);
				
				//Load propInfoId
				Set<ShopPojo> idShopList = propIdMap.get(shop.getPropInfoId());
				if ( idShopList == null ) {
					idShopList = new HashSet<ShopPojo>(20);
					propIdMap.put(shop.getPropInfoId(), idShopList);
				}
				idShopList.add(shop);
				
				if ( shop.getSell() == 0 ) {
					continue;
				}
				//logger.debug("Load shop id {} name {} from database.", shop.getId(), shop.getInfo());
				
				//Check the type of this good
				if ( shop.isItem() ) {
					String itemId = shop.getPropInfoId();
					ItemPojo itemPojo = ItemManager.getInstance().getItemById(itemId);
					if ( itemPojo != null ) {
						String typeId = itemPojo.getTypeId();
						Set<ShopPojo> typeList = typesMap.get(typeId);
						if ( typeList == null ) { 
							typeList = new HashSet<ShopPojo>();
							typesMap.put(typeId, typeList);
						}
						typeList.add(shop);
						Set<ShopPojo> maleSet = genderMap.get(Gender.MALE);
						if ( maleSet == null ) {
							maleSet = new HashSet<ShopPojo>();
							genderMap.put(Gender.MALE, maleSet);
						}
						maleSet.add(shop);
						Set<ShopPojo> femaleSet = genderMap.get(Gender.FEMALE);
						if ( femaleSet == null ) {
							femaleSet = new HashSet<ShopPojo>();
							genderMap.put(Gender.FEMALE, femaleSet);
						}
						femaleSet.add(shop);
						//logger.debug("Put the shop id {} into type map {}", shop.getId(), typeId);
					}
				} else {
					String weaponId = shop.getPropInfoId();
					WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
					if ( weapon != null ) {
						Gender gender = weapon.getSex();
						if ( gender == null ) {
							weapon.setSex(Gender.ALL);
							gender = Gender.ALL;
						}
						if ( gender == Gender.ALL ) {
							Set<ShopPojo> maleSet = genderMap.get(Gender.MALE);
							if ( maleSet == null ) {
								maleSet = new HashSet<ShopPojo>();
								genderMap.put(Gender.MALE, maleSet);
							}
							maleSet.add(shop);
							Set<ShopPojo> femaleSet = genderMap.get(Gender.FEMALE);
							if ( femaleSet == null ) {
								femaleSet = new HashSet<ShopPojo>();
								genderMap.put(Gender.FEMALE, femaleSet);
							}
							femaleSet.add(shop);
						} else {
							Set<ShopPojo> sexSet = genderMap.get(gender);
							if ( sexSet == null ) {
								sexSet = new HashSet<ShopPojo>();
								genderMap.put(gender, sexSet);
							}
							sexSet.add(shop);
							//logger.debug("Put the shop id {} into gender map {}", shop.getId(), gender);
						}
					}
				}
				
				//Load the moenyMap
				Set<ShopPojo> shopList = moneyMap.get(shop.getMoneyType());
				if ( shopList == null ) {
					shopList = new HashSet<ShopPojo>(20);
					moneyMap.put(shop.getMoneyType(), shopList);
				}
				shopList.add(shop);
				
			} // for...
			logger.debug("Load total {} shop good data from database.", dataMap.size());
			
		  //Load ShopCatalog
			//Since one shopPojo may have multi catalogs, I use the query to construct it.
			for ( ShopCatalog catalog : ShopCatalog.values() ) {
				Set<ShopPojo> shopPojoList = new HashSet<ShopPojo>();
				DBObject query = MongoDBUtil.createDBObject(Constant.CATALOGS, catalog.name());
				query.put("sell", 1);
				DBObject fields = MongoDBUtil.createDBObject(Constant._ID, Constant.ONE);
				list = MongoDBUtil.queryAllFromMongo(
						query, databaseName, namespace, COLL_NAME, fields);
				for ( DBObject obj : list ) {
					String shopId = (String)obj.get(Constant._ID);
					if ( shopId != null ) {
						ShopPojo shopPojo = this.getShopById(shopId);
						if ( shopPojo != null ) {
							shopPojoList.add(shopPojo);
						} 
//						else {
//							logger.debug("ShopPojo is not selling: {}", shopPojo!=null?shopPojo.getInfo():"null");
//						}
					}
				}
				catalogMap.put(catalog, shopPojoList);
				logger.debug("Load {} shopPojo into catalog {}", shopPojoList.size(), catalog);
			}
			
			//Load BuffTool price
			for ( BuffToolType buffTool : BuffToolType.values() ) {
				String toolName = "price_tool_".concat(buffTool.name());
				GameDataKey key = GameDataKey.fromKey(toolName);
				if ( key != null ) { 
					int price = GameDataManager.getInstance().getGameDataAsInt(key, 1200);
					buffToolPrice.put(buffTool, price);
				} else {
					logger.info("Not found GameDataKey: {}", toolName);
				}
			}
			int percent = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.SELL_TOOL_DISCOUNT, 85);
			buffToolSellDiscount = percent / 100f;
			int goodPercent = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.SELL_GOOD_DISCOUNT, 85);
			goodSellDiscount = goodPercent / 100f;
		}
	}
	
	/**
	 * Get the given shop by its id.
	 * @param id
	 * @return
	 */
	public ShopPojo getShopById(String id) {
		 return dataMap.get(id);
	}
	
	/**
	 * Get the underlying shop collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<ShopPojo> getShops() {
		return dataMap.values();
	}
	
	/**
	 * Get the ShopPojo list by given PropInfoId ( Either WeaponPojo or 
	 * ItemPojo's id)
	 * 
	 * @return
	 */
	public Collection<ShopPojo> getShopsByPropInfoId(String propInfoId) {
		Set<ShopPojo> shopPojoList = propIdMap.get(propInfoId);
		if ( shopPojoList != null ) {
			return shopPojoList;
		} else {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propInfoId);
			if ( weapon != null ) {
				int typeIndex = StringUtil.toInt(weapon.getTypeName(), 0);
				int typeId = typeIndex*10;
				shopPojoList = propIdMap.get(String.valueOf(typeId));
				ArrayList<ShopPojo> shops = new ArrayList<ShopPojo>(); 
				shops.add(makeShop(weapon.getId()));
				return shops;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Get the shopPojo list by given ShopCatalog
	 * @param catalog
	 * @return
	 */
	public Collection<ShopPojo> getShopsByCatalog(ShopCatalog catalog) {
		return catalogMap.get(catalog);
	}
	
	/**
	 * Get the shopPojo list by given ShopCatalog
	 * @param catalog
	 * @return
	 */
	public Collection<ShopPojo> getShopsByMoneyType(MoneyType moneyType) {
		Set<ShopPojo> shops = moneyMap.get(moneyType);
		if ( shops != null ) {
			return shops;
		} else {
			return Constant.EMPTY_SET;
		}
	}
	
	/**
	 * Get the shopPojo list by given conditions.
	 * if gender == -1, it will be ignored.
	 * if money == -1, it will be ignored.
	 * if catalog == -1, it will be ignored.
	 * if types is null or empty, it will be ignored.
	 * 
	 * @param types
	 * @param catalogId
	 * @param money
	 * @param gender
	 * @param user
	 * @return
	 */
	public Collection<ShopPojo> getShopsByGenderMoneyCatalogOrType(
			String[] types, int catalogId, int money, int gender, User user) {
		
		MoneyType moneyType = MoneyType.fromType(money);
		ShopCatalog givenCatalog = ShopCatalog.fromCatalogId(catalogId);
		//Filter out the result by the gender type
		Gender sex = null;
		if ( gender>=0 && gender<Gender.values().length ) {
			sex = Gender.values()[gender];
		}
		/**
		 * Send the stat first
		 */
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.Shopping, sex!=null?sex.toString():Constant.EMPTY, 
						moneyType!=null?moneyType.toString():Constant.EMPTY, 
						givenCatalog!=null?givenCatalog.toString():Constant.EMPTY, 
								Arrays.toString(types));
		UserActionManager.getInstance().addUserAction(user.getRoleName(), 
				UserActionKey.Shopping);
		
		//Check the types first
		TreeSet<ShopPojo> shopPojos = new TreeSet<ShopPojo>();
		boolean conditionSet = false;

		if ( types != null && types.length > 0 ) {
			conditionSet = true;
			for ( String typeId : types ) {
				Set<ShopPojo> list = this.typesMap.get(typeId);
				if ( list != null && list.size()> 0 ) {
					shopPojos.addAll(list);
				}
				//logger.debug("Get {} total goods for type.", list.size());
			}
		}
		
		if ( conditionSet && shopPojos.size() == 0 ) {
			//No results at all
			return shopPojos;
		}
		
		//Check the catalog then
		if ( givenCatalog != null ) {
			conditionSet = true;
			Set<ShopPojo> list = null;
			if ( givenCatalog == ShopCatalog.HOT ) {
				Jedis jedisDB = JedisFactory.getJedisDB();
				String zsetName = ShopManager.REDIS_HOT_YUANBAO;
				if ( money == MoneyType.GOLDEN.ordinal() ) {
					zsetName = ShopManager.REDIS_HOT_GOLDEN;
				}
				Set<String> hotGoodIds = jedisDB.zrevrange(zsetName, 0, 1000);
				list = new TreeSet<ShopPojo>();
				for ( String goodId : hotGoodIds ) {
					ShopPojo shopPojo = this.getShopById(goodId);
					if ( shopPojo != null ) {
						list.add(shopPojo);
					}
				}
				if ( shopPojos.size() > 0 ) {
					if ( list != null && list.size()> 0 ) {
						shopPojos.retainAll(list);
					}
				} else {
					shopPojos.addAll(list);
				}
			} else {
				list = this.catalogMap.get(givenCatalog);
//				for ( ShopCatalog c : this.catalogMap.keySet() ) {
//					Set<ShopPojo> ss = this.catalogMap.get(c);
//					for ( ShopPojo s : ss ) {
//						System.out.println(s.getInfo());
//					}
//				}
				if ( shopPojos.size() > 0 ) {
					if ( list != null && list.size()> 0 ) {
						shopPojos.retainAll(list);
					}
				} else {
					shopPojos.addAll(list);
				}
			}
			logger.debug("Get {} total goods for catalog: {}.", list.size(), givenCatalog);
		}
		
		if ( conditionSet && shopPojos.size() == 0 ) {
			//No results at all
			return shopPojos;
		}
		
		//Filter out the result by the money type
		if ( moneyType != null ) {
			conditionSet = true;
			if ( shopPojos.size()>0 ) { 
				for (Iterator iter = shopPojos.iterator(); iter.hasNext();) {
					ShopPojo shopPojo = (ShopPojo) iter.next();
					if ( shopPojo.getMoneyType() != moneyType ) {
						iter.remove();
					}
				}
				logger.debug("Get {} total goods for money: {}.", shopPojos.size(), moneyType);
			} else {
				Collection<ShopPojo> list = this.getShopsByMoneyType(moneyType);
				shopPojos.addAll(list);
			}
		}
		
		if ( conditionSet && shopPojos.size() == 0 ) {
			//No results at all
			return shopPojos;
		}
		
		//Filter out the result by the gender type
		if ( sex!=null ) {
			conditionSet = true;
		}
//		if ( givenCatalog == ShopCatalog.ITEM ) {
//			sex = null;
//		}
		if ( sex != null && sex != Gender.ALL ) {
			Collection<ShopPojo> list = this.genderMap.get(sex);
			if ( shopPojos.size()>0 ) {
				ArrayList<ShopPojo> itemPojos = new ArrayList<ShopPojo>();
				for ( ShopPojo sp : shopPojos ) {
					if ( sp.isItem() ) {
						itemPojos.add(sp);
					}
				}
				shopPojos.retainAll(list);
				shopPojos.addAll(itemPojos);
			} else {
				shopPojos.addAll(list);
			}
			logger.debug("Get {} total goods for sex: {}.", shopPojos.size(), sex);
		}
		
		if ( !conditionSet && shopPojos.size() <= 0 ) {
			shopPojos.addAll( this.getShops() );
		}
				
		if ( user != null ) {
			int userLevel = user.getLevel();
			if ( userLevel == LevelManager.MAX_LEVEL ) {
				userLevel = LevelManager.MAX_LEVEL-1;
			}
			if ( userLevel < 10 ) {
				return shopPojos;	
			} else {
				TreeSet<ShopPojo> shops = new TreeSet<ShopPojo>();
				int levelIndex = userLevel/10;
				
				for ( ShopPojo shopPojo : shopPojos ) {
					WeaponPojo weapon = EquipManager.getInstance().getWeaponById(shopPojo.getPropInfoId());
					if ( weapon != null ) {
						int weaponId = StringUtil.toInt(weapon.getId(), 0)+levelIndex;
						shops.add(makeShop(String.valueOf(weaponId)));
					} else {
						shops.add(shopPojo);
					}
				}
				return shops;
			}
			
			/*
			if ( shops.size() > 40 ) {
				TreeSet<ShopPojo> randomShops = new TreeSet<ShopPojo>();
				Object[] shopObjs = MathUtil.randomPick(shops, 40);
				for ( Object shop : shopObjs ) {
					randomShops.add((ShopPojo)shop);
				}
				return randomShops;
			}
			*/
		} else {
			for (Iterator iterator = shopPojos.iterator(); iterator.hasNext();) {
				ShopPojo shopPojo = (ShopPojo) iterator.next();
				if ( shopPojo.getSell() <= 0 ) {
					iterator.remove();
				}
			}
			return shopPojos;
		}
		
	}
	
	/**
	 * Buy a good from shop
	 * @param buyProp
	 * @return
	 */
	public boolean buyGoodFromShop(User user, BceBuyProp buyProp) {
		boolean result = true;
		List<BuyInfo> buyInfos = buyProp.getBuyListList();
		
	  //All goods' price are found.
	  boolean checkResult = false;
	  int[] returnedPrices = new int[4];
	  String[] message = new String[1];

	  HashMap<ShopPojo.BuyInfo, ShopPojo.BuyInfo> goodMap = 
	  		new HashMap<ShopPojo.BuyInfo, ShopPojo.BuyInfo>();
	  /**
	   * Tide all the goods for merging the same ones.
	   */
	  for (BuyInfo goodInfo : buyInfos ) {
	  	String goodId = String.valueOf(goodInfo.getGoodsId());
	  	ShopPojo.BuyInfo buyInfo = new ShopPojo.BuyInfo();
	  	buyInfo.goodId = goodId;
	  	buyInfo.count = goodInfo.getCount();
	  	buyInfo.indateIndex = goodInfo.getLeftTimeType();
	  	buyInfo.color = goodInfo.getColor();
	  	buyInfo.level = goodInfo.getLevel();
	  	
	  	if ( goodMap.containsKey(buyInfo) ) {
	  		ShopPojo.BuyInfo sameGood = goodMap.get(buyInfo);
	  		sameGood.count += buyInfo.count;
	  	} else {
	  		goodMap.put(buyInfo, buyInfo);
	  	}
	  }
		for (ShopPojo.BuyInfo goodInfo : goodMap.values() ) {
			PropData propData = null;
			ShopPojo shopPojo = ShopManager.getInstance().getShopById(goodInfo.goodId);
			if ( shopPojo == null ) {
				int goodId = StringUtil.toInt(goodInfo.goodId, 0);
				if ( goodId > 100000 ) {
					/**
					 * 为了减少重复数据，我对商城的数据进行了清理，只保留黑铁数据，其他数据随机生成，
					 * ID为100000加上商品的ID
					 */
					String propDataId = String.valueOf(goodId-100000);
					shopPojo = makeShop(propDataId);
				}
			}
			if ( shopPojo.isItem() ) {
				propData = ItemManager.getInstance().getItemById(shopPojo.getPropInfoId()).toPropData();
				propData.setWeapon(false);
			} else {
				propData = EquipManager.getInstance().getWeaponById(shopPojo.getPropInfoId()).toPropData(30, WeaponColor.WHITE);
				propData.setWeapon(true);
			}
		  checkResult = checkPriceAndBag(user, goodInfo.goodId,
		  		goodInfo.count, goodInfo.indateIndex, returnedPrices, message, false, propData);
		  if ( !checkResult ) {
		  	break;
		  }
		  
		  logger.debug("buy message: {}", message[0]);
		  
			StatClient.getIntance().sendDataToStatServer(user, StatAction.BuyProp, goodInfo.goodId, 
					goodInfo.count, goodInfo.indateIndex, checkResult);
		}
	  
	  if ( !checkResult ) {
	  	result = false;
	  } else {
		  int goldenPrice = returnedPrices[0];
		  int voucherPrice = returnedPrices[1];
		  int medalPrice = returnedPrices[2];
		  int yuanbaoPrice = returnedPrices[3];
		  			
			for (ShopPojo.BuyInfo goodInfo : goodMap.values() ) {
			  String goodId = goodInfo.goodId;
			  int count = goodInfo.count;
			  int color = 0;//goodInfo.color;
			  int indateTypeIndex = goodInfo.indateIndex;
//			  int level = goodInfo.level;
			  
			  ShopPojo shopPojo = this.getShopById(goodId);
				if ( shopPojo == null ) {
					int goodIntId = StringUtil.toInt(goodInfo.goodId, 0);
					if ( goodIntId > 100000 ) {
						/**
						 * 为了减少重复数据，我对商城的数据进行了清理，只保留黑铁数据，其他数据随机生成，
						 * ID为100000加上商品的ID
						 */
						String propDataId = String.valueOf(goodIntId-100000);
						shopPojo = makeShop(propDataId);
					}
				}
			  List<BuyPrice> priceList = shopPojo.getBuyPrices();
			  //Note: checkPriceAndBag method guarantee the indateTypeIndex is valid
			  BuyPrice buyPrice = priceList.get(indateTypeIndex);
			  //Put the good into bag.
		  	String propDataId = shopPojo.getPropInfoId();
		  	PropData propData = null;
		  	if ( shopPojo.isItem() ) {
		  		ItemPojo item = ItemManager.getInstance().getItemById(propDataId);
		  		propData = item.toPropData();
		  		
		  		if ( logger.isDebugEnabled() ) {
		  			logger.debug("User {} buy the \"{}\"", user.getRoleName(), item.getName());
		  		}
		  	} else {
		  		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propDataId);
		  		if ( color >=0 && color < WeaponColor.values().length ) {
		  			propData = weapon.toPropData(buyPrice.validTimes, WeaponColor.values()[color], 5, null);
		  			//Set the weapon's warranty
		  			setPropDataWarranty(propData);
		  		} else {
		  			logger.warn("#buyGoodFromShop: invalid WeaponColor value: {}", color);
		  			propData = weapon.toPropData(indateTypeIndex, WeaponColor.WHITE);
		  		}
		  		if ( logger.isDebugEnabled() ) {
		  			logger.debug("User {} buy the \"{}\" with indate: {}", 
		  					new Object[]{user.getRoleName(), weapon.getName(), indateTypeIndex});
		  		}
		  	}
		  	propData.setCount(count);
		  	//Add the first good into bag
		  	user.getBag().addOtherPropDatas(propData);
		  	
		  	//Call script hook
		  	TaskManager.getInstance().processUserTasks(user, TaskHook.BUY_ITEM, 
		  			shopPojo, propData, indateTypeIndex);
		  	
		  	//Update the Hot catalog ranking
		  	Jedis jedisDB = JedisFactory.getJedisDB();
		  	String zsetName = null;
		  	if ( shopPojo.getMoneyType() == MoneyType.GOLDEN ) {
		  		zsetName = REDIS_HOT_GOLDEN;
		  	} else if ( shopPojo.getMoneyType() == MoneyType.YUANBAO ) {
		  		zsetName = REDIS_HOT_YUANBAO;
		  	}
		  	if ( zsetName != null ) {
		  		jedisDB.zincrby(zsetName, 1, goodId);
		  	}
	  		
	  		//Record user action
	  		StatClient.getIntance().sendDataToStatServer(user, StatAction.ConsumeBuyProp, shopPojo.getMoneyType(), buyPrice.price,
	  				propData.getName(), propData.getCount(), buyPrice.validTimes);
	  		
				UserActionManager.getInstance().addUserAction(user.getRoleName(), 
						UserActionKey.BuyProp, propData.getName());
		  }
	  	
		  //Finally checkout the bill
			//If there are errors occurred before, the money will not be substracted
			//from user's account, it is safe for users.
			user.setGolden(user.getGolden()-goldenPrice);
			user.setVoucher(user.getVoucher()-voucherPrice);
			//user.setMedal(user.getMedal()-medalPrice);
			if ( medalPrice > 0 ) {
				user.getGuildMember().setMedal(user.getGuildMember().getMedal()-medalPrice);
				GuildManager.getInstance().saveGuildMember(user.getGuildMember());
			}
			//user.setYuanbao(user.getYuanbao()-yuanbaoPrice);
			user.payYuanbao(yuanbaoPrice);
			
	  	//Store user status
	  	UserManager.getInstance().saveUser(user, false);
	  	UserManager.getInstance().saveUserBag(user, false);
	  	
	  	//Update user status
	  	XinqiMessage response = new XinqiMessage();
		  response.payload = user.toBseRoleInfo();
		  GameContext.getInstance().writeResponse(user.getSessionKey(), response);
	  }
	  
		//Send response
		if ( message[0] != null ) {
			XinqiMessage response = new XinqiMessage();
			BseBuyProp.Builder builder = BseBuyProp.newBuilder().setSuccess(checkResult);
			builder.setMessage(message[0]);
		  response.payload = builder.build();
		  GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		}
				
		return result;
	}
	
	/**
	 * Sell the PropData to system. The count is default to 1.
	 * @param user
	 * @param pew
	 * @return
	 */
	public boolean sellGoodToShop(final User user, final int pew) {
		return sellGoodToShop(user, pew, 1);
	}
	
	/**
	 * Sell a good to shop. The user can only get golden
	 * price * 0.85 for that good
	 * 
	 * @param buyProp
	 * @return
	 */
	public boolean sellGoodToShop(final User user, final int pew, final int count) {
		final Bag bag = user.getBag();
		final PropData propData = bag.getOtherPropData(pew);
		if ( propData == null) {
			logger.info("The pew {} in user '{}' bag is empty", pew, user.getRoleName());
			return false;
		}
		if ( count <= 0 || count > propData.getCount()) {
			String message = Text.text("shop.error.wrongcount", count);
			SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
			return false;
		}
		int level = propData.getLevel();
		final int realCount = Math.min(propData.getCount(), count);
		int price = findPriceForItemInBag(user, propData);
		final int finalPrice = price * realCount;
		
		ConfirmManager manager = ConfirmManager.getInstance();
		
		String message = null; 
		if ( level <= 0 || !propData.isWeapon() ) {
			message = Text.text("shop.sellconfirm", finalPrice, propData.getName());
		} else {
			message = Text.text("shop.sellconfirm.strength", level, finalPrice, propData.getName());
		}
		manager.sendConfirmMessage(user, message, "shop.sell", new ConfirmCallback() {
			
			@Override
			public void callback(User user, int selected) {
				if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
					user.setGolden(user.getGolden() + finalPrice);
										
				  //Call the task hook
					if ( propData != null ) {
						propData.setCount( propData.getCount()+1-realCount );
						bag.removeOtherPropDatas(pew);
				    //Send response to users
				    XinqiMessage response = new XinqiMessage();
						BseSellProp.Builder sellPropBuilder = BseSellProp.newBuilder();
						sellPropBuilder.setPropPew(pew);
						sellPropBuilder.setCount(realCount);

						response.payload = sellPropBuilder.build();
						GameContext.getInstance().writeResponse(user.getSessionKey(), response);

						TaskManager.getInstance().processUserTasks(user, TaskHook.SELL_GOOD, 
							propData, finalPrice);
					}
					logger.info("User '{}' selling good with final price: {}", user.getRoleName(), finalPrice);
					
			    //Update user status
			    XinqiMessage response = new XinqiMessage();
				  response.payload = user.toBseRoleInfo();
				  GameContext.getInstance().writeResponse(user.getSessionKey(), response);
				  
			    UserManager.getInstance().saveUser(user, false);
			    UserManager.getInstance().saveUserBag(user, false);
			    
			    /*
			    StatClient.getIntance().sendDataToStatServer(user, 
							StatAction.SellProp, propData.getName(), finalPrice);
			    */
		  		StatClient.getIntance().sendDataToStatServer(user, StatAction.ProduceSellProp, MoneyType.GOLDEN, finalPrice,
		  				propData.getName(), Constant.ONE, Constant.ZERO);
		  		UserActionManager.getInstance().addUserAction(user.getRoleName(), 
							UserActionKey.ProduceSellProp);
				}
			}
		});
		
		return true;
	}
	
	/**
	 * Find the price for an item in user's bag.
	 * @param user
	 * @param pew
	 * @return
	 */
	public int findPriceForItemInBag(User user, PropData propData) {
		int finalPrice = 0;
		if ( propData != null ) {
			logger.debug("User '{}' sells propData: {} to shop.", user.getRoleName(), propData.getName());
			Collection<ShopPojo> shops = this.getShopsByPropInfoId(propData.getItemId());
			if ( shops != null ) {
				ShopPojo goldenShop = null;
				ShopPojo notNullShop = null;
				for ( ShopPojo shop : shops ) {
					if ( shop != null ) {
						notNullShop = shop;
						if ( shop.getMoneyType() == MoneyType.GOLDEN ) {
							goldenShop = shop;
							break;
						}
					}
				}
				finalPrice = ScriptManager.getInstance().runScriptForInt(ScriptHook.SHOP_SELL_GOOD, 
						user, propData, goldenShop, notNullShop);

				if ( finalPrice < 0 ) {
					finalPrice = 0;
				}
			} else {
				logger.info("Failed to find price for propId {} in shop.", propData.getItemId());
				//Cannot sell binded items
//				SysMessageManager.getInstance().sendClientInfoMessage(user, "shop.sellbinded", 
//						Action.NOOP, new Object[]{propData.getName()});
				finalPrice = 0;
			}
		}
		finalPrice = Math.round(finalPrice * goodSellDiscount);
		return finalPrice;
	}
	
	/**
	 * Find given propData's price.
	 * 1. Scan all the ShopPojo for given equipment. 
	 * 2. Find the default one as a backup. Find the proper one as the primary use.
	 * 
	 * 
	 * @param user
	 * @param buyPrice TODO
	 * @param buyPriceMoneyType TODO
	 * @param considerColorStrength TODO
	 * @param pew
	 * @return
	 */
	public int findPriceForPropData(User user, PropData propData, MoneyType moneyType, 
			BuyPrice buyPrice, MoneyType buyPriceMoneyType, boolean considerColorStrength) {
		int finalPrice = 0;
		if ( propData != null ) {
			int yuanbaoToGolden = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.YUANBAO_TO_GOLDEN_RATIO, 100);
			
			if ( buyPrice == null ) {
				Collection<ShopPojo> shops = this.getShopsByPropInfoId(propData.getItemId());
				if ( shops != null ) {
					/**
					 * 未指定buyPrice，自动搜索匹配项目
					 */
					ShopPojo givenShop = null;
					BuyPrice givenBuyPrice = null;
					ShopPojo defaultShop = null;
					
					for ( ShopPojo shop : shops ) {
						if ( defaultShop == null && shop != null ) {
							defaultShop = shop;
						}
						if ( shop != null && shop.getMoneyType() == moneyType ) {
							givenShop = shop;
							break;
						}
					}
					if ( givenShop != null) {
						List<BuyPrice> prices = givenShop.getBuyPrices();
						for ( BuyPrice price : prices ) {
							if ( propData.getPropIndate() == Integer.MAX_VALUE 
									|| price.validTimes == propData.getPropIndate() ) {
								finalPrice = price.price;
								break;
							}
						}
					} else if ( defaultShop != null ) {
						List<BuyPrice> prices = defaultShop.getBuyPrices();
						if ( defaultShop.getMoneyType() == MoneyType.YUANBAO && moneyType == MoneyType.GOLDEN ) {
							for ( BuyPrice price : prices ) {
								if ( propData.getPropIndate() == Integer.MAX_VALUE 
										|| price.validTimes >= propData.getPropIndate() ) {
									finalPrice = price.price * yuanbaoToGolden;
									break;
								}
							}
						} else if ( defaultShop.getMoneyType() == MoneyType.GOLDEN && moneyType == MoneyType.YUANBAO ) {
							for ( BuyPrice price : prices ) {
								if ( propData.getPropIndate() == Integer.MAX_VALUE 
										|| price.validTimes >= propData.getPropIndate() ) {
									finalPrice = (int)Math.round(price.price * 1.0 / yuanbaoToGolden);
									break;
								}
							}
						}
					}
				} else {
					logger.info("Failed to find price for propId {} in shop.", propData.getItemId());
					finalPrice = 0;
				}
			} else {
				/**
				 * 已经指定了buyPrice，检查价格单位是否一致
				 */
				if ( moneyType == MoneyType.GOLDEN && buyPriceMoneyType == MoneyType.YUANBAO ) {
					finalPrice = buyPrice.price * yuanbaoToGolden;
				} else {
					finalPrice = buyPrice.price;
				}
			}
			
			if ( propData.isWeapon() && considerColorStrength ) {
				float strengthRatio = 0;
				float colorRatio = 0;
				if ( propData != null ) {
					if ( propData.getLevel() > 0 ) {
						strengthRatio = 0.1f * propData.getLevel();
					}
					int color = propData.getWeaponColor().ordinal();
					if ( color > 0 ) {
						colorRatio = 0.1f * color;
					}
				}
				int price = Math.round(finalPrice + finalPrice*strengthRatio + finalPrice*colorRatio);
				if ( price > 0 ) {
					finalPrice = price;
				}
			}
		}
		return finalPrice;
	}
	
	/**
	 * Buy a battle BuffTool and put it into use's bag.
	 * @return
	 */
	public boolean buyBuffTool(User user, BuffToolType toolType) {
		boolean result = true;
		
		//Buy the new bufftool
		int price = 0;
		if (toolType == BuffToolType.Recover || toolType == BuffToolType.AllRecover ) {
			String toolName = "price_tool_".concat(toolType.name());
			GameDataKey key = GameDataKey.fromKey(toolName);
			if ( key != null ) { 
				price = GameDataManager.getInstance().getGameDataAsInt(key, 1200);
			}
			price = EquipCalculator.calculateBloodPrice(user.getLevel(), price);
		} else {
			price = buffToolPrice.get(toolType);
		}
		/**
		 * The tutorial will not cost any money of users.
		 */
		/*
		if ( user.isTutorial() ) {
			result = true;
		} else {
			result = payForSomething(user, MoneyType.GOLDEN, price, 1, null);
		}
		*/
		result = payForSomething(user, MoneyType.GOLDEN, price, 1, null);
		if ( result ) {
			user.addTool(toolType);
			
			StatClient.getIntance().sendDataToStatServer(user, StatAction.ConsumeBuyTool, MoneyType.GOLDEN, price,
					toolType, Constant.ONE, Constant.ONE);
			
			UserActionManager.getInstance().addUserAction(user.getRoleName(), 
					UserActionKey.BuyTool);
		}
				
		//Sync with client weather succeed or not.
		BseToolList bseToolList = user.toBseToolList();
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = bseToolList;
		GameContext.getInstance().writeResponse(user.getSessionKey(), xinqi);
		
		//StatClient.getIntance().sendDataToStatServer(user, StatAction.BuyTool, toolType, price, result);
		
		return result;
	}
	
	/**
	 * User sell a BuffTool
	 * @return
	 */
	public boolean sellBuffTool(User user, int toolIndex) {
		boolean result = true;
		/**
		 * toolIndex should from 1 to start
		 */
		if ( toolIndex >0 && toolIndex <= user.getTools().size() ) {
			BuffToolType toolType = user.getTools().get(toolIndex-1);
			if ( toolType != null ) {
				int price = 0;
				if (toolType == BuffToolType.Recover || toolType == BuffToolType.AllRecover ) {
					String toolName = "price_tool_".concat(toolType.name());
					GameDataKey key = GameDataKey.fromKey(toolName);
					if ( key != null ) { 
						price = GameDataManager.getInstance().getGameDataAsInt(key, 1200);
					}
					price = EquipCalculator.calculateBloodPrice(user.getLevel(), price);
				} else {
					price = buffToolPrice.get(toolType);
				}
				int finalPrice = (int)(price * buffToolSellDiscount);
				if ( finalPrice < 0 ) {
					finalPrice = 0;
				}
				//Get the money
				user.setGolden(user.getGolden() + finalPrice);
				//Remove the tool
				user.removeTool(toolIndex-1);
				
				//Save the new user status.
				UserManager.getInstance().saveUser(user, false);
				//Notify client user's role data is changed.
				//Send the data back to client
				BseRoleInfo roleInfo = user.toBseRoleInfo();
				GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
				
				if ( StatClient.getIntance().isStatEnabled() ) {
					//StatClient.getIntance().sendDataToStatServer(user, StatAction.SellTool, toolType, toolIndex, finalPrice);
					StatClient.getIntance().sendDataToStatServer(user, StatAction.ProduceSellTool, MoneyType.GOLDEN, finalPrice, toolType, Constant.ONE, Constant.ONE);
					UserActionManager.getInstance().addUserAction(user.getRoleName(), 
							UserActionKey.ProduceSellTool);
				}
			} else {
				logger.debug("#sellBuffTool: The tool at index {} is empty.", toolIndex);
			}
		}
		
		//Sync with client weather succeed or not.
		BseToolList bseToolList = user.toBseToolList();
		GameContext.getInstance().writeResponse(user.getSessionKey(), bseToolList);
		
		return result;
	}
	
	/**
	 * Check if some of the equipments that user wears are expired,
	 * i.e, the propUsedTime >= propIndate. Those expired equipments
	 * are not unweared. But the user's power is to be recalculated.
	 *  
	 * @param user
	 * @return
	 */
	public Collection<PropData> checkEquipmentsExpire(User user) {
		Bag bag = user.getBag();
		ArrayList<PropData> expireSet = new ArrayList<PropData>();
		List<PropData> propDatas = bag.getWearPropDatas();
		for ( PropDataEquipIndex index : PropDataEquipIndex.values() ) {
			PropData propData = propDatas.get(index.index());
			if ( propData != null ) {
				if ( propData.getPropUsedTime() >= propData.getPropIndate() ) {
					propData.setExpire(true);
					expireSet.add(propData);
				} else {
					propData.setExpire(false);
				}
			}
		}
		if ( expireSet.size() > 0 ) {
			//Recalculate user's power
			//Call script to upgrade properties
			ScriptManager.getInstance().runScript(ScriptHook.USER_LEVEL_UPGRADE, user, 0 );
			
			StringBuilder buf = new StringBuilder(50);
			for ( PropData w : expireSet ) {
				buf.append(w.getName()).append(",");
			}
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.WeaponDamaged, expireSet.size(), buf.toString());
		}
		
		//Now check items in normal bag
		List<PropData> otherPropDatas = bag.getOtherPropDatas();
		for ( PropData propData : otherPropDatas ) {
			if ( propData != null ) {
				if ( EquipManager.getInstance().getWeaponById(propData.getItemId()) != null ) {
					if ( propData.getPropUsedTime() >= propData.getPropIndate() ) {
						propData.setExpire(true);
						expireSet.add(propData);
					} else {
						propData.setExpire(false);
					}
				}
			}
		}
		return expireSet;
	}
	
	/**
	 * Convert the propData list to expire info for client to use.
	 * @param pews
	 * @return
	 */
	public Collection<ExpireInfo> getExpireEquipInfos(User user, Collection<PropData> propDatas) {
		ArrayList<ExpireInfo> infos = new ArrayList<ExpireInfo>();
		int yuanbaoToGolden = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.YUANBAO_TO_GOLDEN_RATIO, 100);
		
		for ( PropData propData : propDatas ) {
			ExpireInfo.Builder builder = ExpireInfo.newBuilder();
			builder.setPew(propData.getPew());
			Collection<ShopPojo> shops = ShopManager.getInstance().getShopsByPropInfoId(propData.getItemId());
			ShopPojo defaultShopPojo = null;
			if ( shops != null ) {
				for ( ShopPojo shop : shops ) {
					defaultShopPojo = shop;
					if ( shop.getMoneyType() == MoneyType.GOLDEN ) {
						break;
					}
				}
			}
			if ( defaultShopPojo != null ) {
				builder.setShopid(StringUtil.toInt(defaultShopPojo.getId(), 0));
				builder.setGoldtype(MoneyType.GOLDEN.type());
				Collection<BuyPrice> prices = defaultShopPojo.getBuyPrices();
				for ( BuyPrice price : prices ) {
					//Set the duration to 100% at client
					int finalPrice = findPriceForPropData(user, propData, MoneyType.GOLDEN, 
							price, defaultShopPojo.getMoneyType(), true);
					builder.addIndate(100);
					builder.addPrice(finalPrice);
				}
			} else {
				logger.warn("Weapon {} does not has shoppojo.", propData.getName());
			}
			infos.add(builder.build());
		}
		return infos;
	}
	
	/**
	 * Everytime users join a battle, his wearing equipments's 
	 * usedTime will be added one until it is >= total duration.
	 * @param user
	 */
	public void reduceUserEquipmentDuration(User user) {
		reduceUserEquipmentDuration(user, System.currentTimeMillis());
	}
	
	/**
	 * Everytime users join a battle, his wearing equipments's 
	 * usedTime will be added one until it is >= total duration.
	 * @param user
	 */
	public void reduceUserEquipmentDuration(User user, long currentTimeMillis) {
		if ( user.isAI() ) return;
		Bag bag = user.getBag();
		EnumSet<PropDataEquipIndex> expireSet = EnumSet.noneOf(PropDataEquipIndex.class);
		boolean changed = false;
		List<PropData> propDatas = bag.getWearPropDatas();
		for ( PropDataEquipIndex index : PropDataEquipIndex.values() ) {
			PropData propData = propDatas.get(index.index());
			if ( propData != null && !propData.isExpire() ) {
				long timestamp = propData.getWarrantMillis();
				if ( timestamp > currentTimeMillis ) {
					//The propData is in warrant time.
					int oldPropUsedTime = propData.getPropUsedTime();
					long warrantyMillis = propData.getWarrantMillis();
					
					String todayStr = DateUtil.getToday(currentTimeMillis);
					String propDataTodayStr = propData.getWarrantDateKey();
					int limit = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_WARRANTY_UNIT, 15);
					if ( !todayStr.equals(propDataTodayStr) ) {
						propData.setWarrantDateKey(todayStr);
						propData.setWarrantDateLimit(Math.min(oldPropUsedTime+limit, propData.getPropIndate()));
					}
					int todayLimit = propData.getWarrantDateLimit();
					int usedTime = propData.getPropUsedTime() + 1;
					if ( usedTime <= todayLimit ) {
						propData.setPropUsedTime( usedTime );
						changed = true;
						logger.debug("change the weapon {} prop usedtime to {} with warranty", propData.getName(), usedTime);
					}
				} else {
					int usedTime = propData.getPropUsedTime() + 1;
					propData.setPropUsedTime( usedTime );
					changed = true;
					logger.debug("change the weapon {} prop usedtime to {}", propData.getName(), usedTime);
				}
				bag.setWearPropData(propData, index.index());
			}
		}
		if ( changed ) {
			UserManager.getInstance().saveUserBag(user, false);
		}
	}
	
	/**
	 * When a tool is invalid (expire or broken), 
	 * the user can pay for subscribe it again.
	 */
	public void resubscribePropData(User user, List<LengthenIndate> lengthIndateList) {
		int[] returnedPrices = new int[4];
		String[] message = new String[1];
		boolean checkResult = false;
		
		int count = 1;
		//Check price first
		for ( LengthenIndate lengthIndate : lengthIndateList ) {
			String id = lengthIndate.getId();
			String shopId = String.valueOf(lengthIndate.getShopid());
			int indateType = lengthIndate.getIndatetype();
			int pew = lengthIndate.getProppos();
			if ( logger.isDebugEnabled() ) {
				logger.debug("User {} lengthen indate for shopId {} indateType {} pew {}",
					new Object[]{user.getRoleName(), shopId, indateType, pew});
			}
		  PropData propData = null;
		  if ( pew >= 0 ) {
		  	Bag bag = user.getBag();
		  	if ( pew < Bag.BAG_WEAR_COUNT ) {
		  		propData = bag.getWearPropDatas().get(pew);
		  	} else {
		  		propData = bag.getOtherPropData(pew);
		  	}
		  }
		  checkResult = checkPriceAndBag(user, shopId, count, indateType, returnedPrices, message, true, propData);
		  if ( !checkResult ) {
		  	SysMessageManager.getInstance().sendClientInfoMessage(user, message[0], Type.NORMAL);
		  	StatClient.getIntance().sendDataToStatServer(user, StatAction.LengthenIndate, shopId, Constant.EMPTY, indateType, pew, false);
		  	break;
		  }
		}
		
		if ( checkResult ) {
		  int goldenPrice = returnedPrices[0];
		  int voucherPrice = returnedPrices[1];
		  int medalPrice = returnedPrices[2];
		  int yuanbaoPrice = returnedPrices[3];
			
		  boolean buySuccess = false;
			for ( LengthenIndate lengthIndate : lengthIndateList ) {
				String id = lengthIndate.getId();
				String shopId = String.valueOf(lengthIndate.getShopid());
				int indateType = lengthIndate.getIndatetype();
				int pew = lengthIndate.getProppos();
				
				PropData propData = null;
			  if ( pew >= 0 ) {
			  	Bag bag = user.getBag();
			  	if ( pew < Bag.BAG_WEAR_COUNT ) {
			  		propData = bag.getWearPropDatas().get(pew);
			  	} else {
			  		propData = bag.getOtherPropData(pew);
			  	}
			  }
			  ShopPojo shopPojo = this.getShopById(shopId);
			  if ( shopPojo == null ) {
			  	Collection<ShopPojo> shopPojos = this.getShopsByPropInfoId(propData.getItemId());
			  	if ( shopPojos != null && shopPojos.size() > 0 ) {
			  		shopPojo = shopPojos.iterator().next();
			  	}
			  }
		  	if ( shopPojo.isItem() ) {
		  		if ( logger.isDebugEnabled() ) {
		  			logger.warn("#resubscribePropData: Cannot resubscribe for ItemPojo, id={}", shopId);
		  		}
		  	} else {					
				  //Put the good into bag.
		  		Bag bag = user.getBag();
			  	String propDataId = shopPojo.getPropInfoId();
			  	if ( propData != null ) {
				  	BuyPrice buyPrice = shopPojo.getBuyPrices().get(indateType);
				  	propData.setPropIndate(buyPrice.validTimes);
				  	propData.setPropUsedTime(0);
				  	propData.setExpire(false);
				  	propData.setLengthenTimes(propData.getLengthenTimes()+1);
				  	setPropDataWarranty(propData);
				  	if ( pew < Bag.BAG_WEAR_COUNT ) {
				  		//Recalculate user's power
				  		bag.setWearPropData(propData, pew);
				  		UserCalculator.updateWeaponPropData(user, propData, true);
				  	} else {
				  		bag.setOtherPropDataAtPew(propData, pew);
				  	}
				  	/**
				  	 * After the resubscribe, should recaculate the 
				  	 * power
				  	 * 2012-12-16
				  	 */
						int power = (int)UserCalculator.calculatePower(user);
					  user.setPower(power);
				  	
			  		if ( logger.isDebugEnabled() ) {
			  			logger.debug("User {} re-subscribe the propData:\"{}\" with new indate value: {}", 
			  					new Object[]{user.getRoleName(), propDataId, propData.getPropUsedTime()});
			  		}
			  		XinqiMessage response = new XinqiMessage();
			  		BseLengthenIndate.Builder builder = BseLengthenIndate.newBuilder();
			  		builder.setPew(pew);
			  		//It means 100% good for a resubscribed weapon
			  		builder.setNewIndate(100);
			  		response.payload = builder.build();
			  		
			  		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
			  		
			  		SysMessageManager.getInstance().sendClientInfoMessage(user, 
			  				"shop.resubscribe", Action.NOOP, new Object[]{propData.getName()});

			  		buySuccess = true;
			  		
			  		//Update user's power ranking now
			  		user.updatePowerRanking();
			  		
			  		StatClient.getIntance().sendDataToStatServer(user, StatAction.LengthenIndate, shopId, propData.getName(), indateType, pew, buySuccess);
			  		
			  	} else {
			  		logger.warn("#resubscribePropData: Invalid pew {}. A null propData is in that position", pew);
			  	}
		  	}
			}
		  //Checkout the bill
			if ( buySuccess ) {
				user.setGolden(user.getGolden()-goldenPrice);
				user.setVoucher(user.getVoucher()-voucherPrice);
				user.setMedal(user.getMedal()-medalPrice);
				//user.setYuanbao(user.getYuanbao()-yuanbaoPrice);
				user.payYuanbao(yuanbaoPrice);
				
				UserManager.getInstance().saveUser(user, false);
				UserManager.getInstance().saveUserBag(user, false);
				
				//Notify client user's role data is changed.
				//Send the data back to client
				BseRoleInfo roleInfo = user.toBseRoleInfo();
				BseRoleBattleInfo battleRoleInfo = user.toBseRoleBattleInfo();
				GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
				GameContext.getInstance().writeResponse(user.getSessionKey(), battleRoleInfo);
				logger.debug("The new roleInfo data for user {} is sent to client.", user.getRoleName());

			} else {
				logger.warn("Failed to checkout the bill for user {}", user.getRoleName());
			}
		}
	}
	
	/**
	 * There are some virtual 'services' in game that will cost user's money. For example, all craft services 
	 * and bufftools need the gold. This method will check if the user can pay for the price. If he can,
	 * substract the money from his deposit and return true. Otherwise return false.
	 * 
	 * @param user
	 * @param moneyType
	 * @param price
	 * @param count
	 * @param message It is a one length string array used to collect message.
	 * @return
	 */
	public final boolean payForSomething(User user, MoneyType moneyType, int price, int count, String[] message) {
		return payForSomething(user, moneyType, price, count, message, true);
	}
	
	/**
	 * 
	 * @param user
	 * @param moneyType
	 * @param price
	 * @param count
	 * @param message
	 * @param saveUser
	 * @return
	 */
	public final boolean payForSomething(User user, MoneyType moneyType, int price, 
			int count, String[] message, boolean saveUser) {
	  int goldenPrice = 0;
	  int voucherPrice = 0;
	  int medalPrice = 0;
	  int yuanbaoPrice = 0;
	  String promptMessage = null;
	  
	  boolean hasEnoughMoney = true;

	  int finalPrice = price*count;
	  
		switch ( moneyType ) {
			case GOLDEN:
				goldenPrice = finalPrice;
				if ( user.getGolden() < goldenPrice ) {
					logger.debug("User {} does not have enough golden {} for the good", user.getRoleName(), goldenPrice);
					promptMessage = Text.text("shop.error.nogold");
					hasEnoughMoney = false;
				} else {
					logger.debug("User {} will pay golden {} for the good", user.getRoleName(), goldenPrice);
				}
				break;
			case VOUCHER:
				voucherPrice = finalPrice;
				if ( user.getVoucher() < voucherPrice ) {
					logger.debug("User {} does not have enough voucher {} for the good", user.getRoleName(), voucherPrice);
					promptMessage = Text.text("shop.error.novoucher");
					hasEnoughMoney = false;
				} else {
					logger.debug("User {} will pay voucher {} for the good", user.getRoleName(), voucherPrice);
				}
				break;
			case MEDAL:
				medalPrice = finalPrice;
				if ( user.getMedal() < medalPrice ) {
					logger.debug("User {} does not have enough medal {} for the good", user.getRoleName(), medalPrice);
					promptMessage = Text.text("shop.error.nomedal");
					hasEnoughMoney = false;
				} else {
					logger.debug("User {} will pay medal {} for the good", user.getRoleName(), medalPrice);
				}
				break;
			case YUANBAO:
				yuanbaoPrice = finalPrice;
				if ( !user.canPayYuanbao(yuanbaoPrice) ) {
					logger.debug("User {} does not have enough yuanbao {} for the good", user.getRoleName(), yuanbaoPrice);
					promptMessage = Text.text("shop.error.noyuanbao");
					hasEnoughMoney = false;
				} else {
					logger.debug("User {} will pay yuanbao {} for the good", user.getRoleName(), yuanbaoPrice);
				}
				break;
		}
		
		if ( hasEnoughMoney ) {
			user.setGolden(user.getGolden()-goldenPrice);
			user.setVoucher(user.getVoucher()-voucherPrice);
			user.setMedal(user.getMedal()-medalPrice);
			user.payYuanbao(yuanbaoPrice);
			
			if ( saveUser ) {
				//Update user's info
				UserManager.getInstance().saveUser(user, false);
				BseRoleInfo roleInfo = user.toBseRoleInfo();
				GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
			}
			return true;
		} else {
			if ( promptMessage != null ) {
				if ( message != null && message.length>0 ) {
					message[0] = promptMessage;
				} else {
					SysMessageManager.getInstance().sendClientInfoRawMessage(
							user, promptMessage, Action.NOOP, Type.NORMAL);
				}
			}
			return false;
		}
	}
	
	/**
	 * Set the propData's warranty according to its validtimes
	 * @param propData
	 */
	public static final void setPropDataWarranty(PropData propData) {
		int limit = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_WARRANTY_UNIT, 15);
		int todayLimit = Math.min(propData.getPropUsedTime() + limit, propData.getPropIndate());
		int totalDays = Math.round(propData.getPropIndate()/limit);
		if ( totalDays <= 0 ) totalDays = 1;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, totalDays);
		propData.setWarrantMillis(cal.getTimeInMillis());
		propData.setWarrantDateLimit(todayLimit);
		
		logger.debug("The '{}' will expire at {}", propData.getName(), cal.getTime());
	}
	
	/**
	 * Convert the weapon slot to proper shop catalog.
	 * @param slot
	 * @return
	 */
	public static final ShopCatalog convertSlotToCatalog(EquipType slot) {
		switch ( slot ) {
			case BUBBLE:
				return ShopCatalog.DECORATION;
			case CLOTHES:
				return ShopCatalog.DECORATION;
			case DECORATION:
				return ShopCatalog.DECORATION;
			case EXPRESSION:
				return ShopCatalog.DECORATION;
			case FACE:
				return ShopCatalog.DECORATION;
			case GIFT_PACK:
				return ShopCatalog.GIFTPACK;
			case GLASSES:
				return ShopCatalog.DECORATION;
			case HAIR:
				return ShopCatalog.DECORATION;
			case HAT:
				return ShopCatalog.DECORATION;
			case ITEM:
				return ShopCatalog.ITEM;
			case JEWELRY:
				return ShopCatalog.DECORATION;
			case OFFHANDWEAPON:
				return ShopCatalog.DECORATION;
			case OTHER:
				return ShopCatalog.DECORATION;
			case SUIT:
				return ShopCatalog.SUITE;
			case WEAPON:
				return ShopCatalog.WEAPON;
			case WING:
				return ShopCatalog.DECORATION;
		}
		return ShopCatalog.DECORATION;
	}
	
	/**
	 * Save a new shopPojo to database.
	 * @param shopPojo
	 * @return
	 */
	public boolean addShopPojo(ShopPojo shopPojo) {
		DBObject dbObj = MongoDBUtil.createMapDBObject(shopPojo);
		DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, shopPojo.getId());
		MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
		return true;
	}
		
	/**
	 * 
	 * @param user
	 * @param shopId
	 * @param count
	 * @param indateIndex
	 * @param returnedPrices
	 * @param message
	 * @param useGolden If ture, use golden to pay.
	 * @param propData TODO
	 * @return
	 */
	private boolean checkPriceAndBag(User user, String shopId, int count, int indateIndex, 
			int[] returnedPrices, String[] message, boolean useGolden, PropData propData) {
		
	  int goldenPrice = returnedPrices[0];
	  int voucherPrice = returnedPrices[1];
	  int medalPrice = returnedPrices[2];
	  int yuanbaoPrice = returnedPrices[3];
	  
	  boolean findAllPrice = true;
	  boolean hasEnoughMoney = true;

	  Bag bag = user.getBag();
	  if ( bag.getCurrentCount() + 1 > bag.getMaxCount() ) {
	  	logger.debug("User {} 's bag is full of items {}.", user.getRoleName(), bag.getCurrentCount());
	  	message[0] = Text.text("shop.error.fullbag");
	  	return false;
	  }
	  
	  ShopPojo shopPojo = this.getShopById(shopId);
	  if ( shopPojo == null ) {
	  	Collection<ShopPojo> shopPojos = this.getShopsByPropInfoId(propData.getItemId());
	  	if ( shopPojos != null && shopPojos.size() > 0 ) {
	  		shopPojo = shopPojos.iterator().next();
	  	}
	  }
	  if ( shopPojo != null ) {
		  //Find the proper price
	  	if ( shopPojo.isItem() ) {
	  		indateIndex = 0;
	  	}
		  List<BuyPrice> priceList = shopPojo.getBuyPrices();
		  if ( indateIndex >= 0 && indateIndex < priceList.size() ) {
		  	BuyPrice buyPrice = priceList.get(indateIndex);
		  	MoneyType buyPriceMoneyType = shopPojo.getMoneyType();
		  	int finalPrice = 0;
		  	if ( buyPriceMoneyType != MoneyType.MEDAL ) {
			  	MoneyType moneyType = MoneyType.YUANBAO;
			  	if ( useGolden ) {
			  		moneyType = MoneyType.GOLDEN;
			  	}
			  	finalPrice = findPriceForPropData(user, propData, moneyType, buyPrice, buyPriceMoneyType, true) * count;
			  	buyPriceMoneyType = moneyType;
		  	} else {
		  		finalPrice = buyPrice.price * count;
		  	}

		  	//Disable VIP discount
		  	/*
		  	boolean isVip = UserManager.getInstance().checkUserVipStatus(user);
		  	if ( isVip ) {
		  		float shopDiscount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.VIP_SHOP_DISCOUNT, 80)/100.0f;
		  		finalPrice = (int)(finalPrice*shopDiscount);
		  		if ( finalPrice <= 0 ) {
		  			finalPrice = 1;
		  		}
		  		logger.debug("User {} is VIP and the final price is: {}", user.getRoleName(), finalPrice);
		  	}
		  	*/
	  		switch ( buyPriceMoneyType ) {
	  			case GOLDEN:
	  				goldenPrice += finalPrice;
	  				if ( user.getGolden() < goldenPrice ) {
	  					logger.debug("User {} does not have enough golden {} for the good", user.getRoleName(), goldenPrice);
	  					message[0] = Text.text("shop.error.nogold");
	  					hasEnoughMoney = false;
	  				} else {
	  					logger.debug("User {} will pay golden {} for the good", user.getRoleName(), goldenPrice);
	  				}
	  				break;
	  			case VOUCHER:
	  				voucherPrice += finalPrice;
	  				if ( user.getVoucher() < voucherPrice ) {
	  					logger.debug("User {} does not have enough voucher {} for the good", user.getRoleName(), voucherPrice);
	  					message[0] = Text.text("shop.error.novoucher");
	  					hasEnoughMoney = false;
	  				} else {
	  					logger.debug("User {} will pay voucher {} for the good", user.getRoleName(), voucherPrice);
	  				}
	  				break;
	  			case MEDAL:
	  				medalPrice += finalPrice;
	  				if ( user.getMedal() < medalPrice ) {
	  					logger.debug("User {} does not have enough medal {} for the good", user.getRoleName(), medalPrice);
	  					message[0] = Text.text("shop.error.nomedal");
	  					hasEnoughMoney = false;
	  				} else {
	  					logger.debug("User {} will pay medal {} for the good", user.getRoleName(), medalPrice);
	  				}
	  				break;
	  			case YUANBAO:
	  				yuanbaoPrice += finalPrice;
	  				if ( !user.canPayYuanbao(yuanbaoPrice) ) {
	  					logger.debug("User {} does not have enough yuanbao {} for the good", user.getRoleName(), yuanbaoPrice);
	  					message[0] = Text.text("shop.error.noyuanbao");
	  					hasEnoughMoney = false;
	  				} else {
	  					logger.debug("User {} will pay yuanbao {} for the good", user.getRoleName(), yuanbaoPrice);
	  				}
	  				break;
	  		}
	  		StatClient.getIntance().sendDataToStatServer(user, StatAction.ConsumeRepair, buyPriceMoneyType, buyPrice.price, shopPojo.getInfo(), 1, buyPrice.validTimes);
		  } else {
		  	findAllPrice = false;
		  	logger.debug("#checkPrice: Cannot find BuyPrice by index: {} for good {}", indateIndex, shopId);
		  	message[0] = Text.text("shop.error.noprice");
		  }
	  } else {
	  	logger.warn("#checkPrice: Cannot find goodId {} ", shopId);
	  	findAllPrice = false;
	  }
	  
	  
	  if ( findAllPrice && hasEnoughMoney ) {
		  returnedPrices[0] = goldenPrice;
		  returnedPrices[1] = voucherPrice;
		  returnedPrices[2] = medalPrice;
		  returnedPrices[3] = yuanbaoPrice;
		  message[0] = Text.text("shop.success");
		  
		  return true;
	  }
	  return false;
	}
	
	/**
	 * Construct Protobuf's BseEquipment data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public BseShop toBseShop(User user) {
		BseShop.Builder builder = BseShop.newBuilder();
		int userLevel = user!=null?user.getLevel():1;
		if ( userLevel == LevelManager.MAX_LEVEL ) {
			userLevel = LevelManager.MAX_LEVEL-1;
		}
		int minLevel = userLevel/10*10;
		int maxLevel = (userLevel/10+1)*10;
		for ( ShopPojo shopPojo : dataMap.values() ) {
			if ( shopPojo.getLevel() == -1 || 
					(shopPojo.getLevel() >= minLevel && shopPojo.getLevel() < maxLevel) ) {
				builder.addShops(shopPojo.toShopData());
			}
		}
		return builder.build();
	}
	
	/**
	 * Construct Protobuf's BseEquipment data and 
	 * prepare to send to client with given discount
	 * 0 - 100
	 * 
	 * @return
	 */
	public BseShop toBseShop(User user, int discount) {
		BseShop.Builder builder = BseShop.newBuilder();
		int userLevel = user!=null?user.getLevel():1;
		if ( userLevel == LevelManager.MAX_LEVEL ) {
			userLevel = LevelManager.MAX_LEVEL-1;
		}
		int minLevel = userLevel/10*10;
		int maxLevel = (userLevel/10+1)*10;
		/**
		 * Note the 'discount' should not be upated to 'discount' field
		 * in ShopPojo because it is a global object. Instead, calculate
		 * the price everytime user buying it. 
		 */
		for ( ShopPojo shopPojo : dataMap.values() ) {
			if ( shopPojo.getLevel() == -1 || 
					(shopPojo.getLevel() >= minLevel && shopPojo.getLevel() < maxLevel) ) {
				builder.addShops(shopPojo.toShopData(discount));
			}
		}
		return builder.build();
	}
	
	/**
	 * Make the ShopPojo object for given weapon and user.
	 * @param user
	 * @return
	 */
	private ShopPojo makeShop(String weaponId) {
		double[] ratios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.WEAPON_LEVEL_RATIO);
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
		Collection<ShopPojo> shopPojoList = this.propIdMap.get(weaponId);

		int levelIndex = weapon.getUserLevel()/10;
		int power = weapon.getPower();
		
		//double goldenUnit = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.SHOP_DPR_TO_GOLDEN, 2.0);
		double yuanbaoUnit = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.SHOP_DPR_TO_YUANBAO, 0.006667);
		int simpleRatio = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.SHOP_PRICE_SIMPLE_RATIO, 1);
		int normalRatio = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.SHOP_PRICE_NORMAL_RATIO, 3);
		int toughRatio = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.SHOP_PRICE_TOUGH_RATIO, 5);
		
		List<BuyPrice> oPrices = new ArrayList<BuyPrice>();
		//元宝简陋
		BuyPrice simple = new BuyPrice();
		simple.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SIMPLE, 30);
		simple.price = (int)Math.round(power * simpleRatio * yuanbaoUnit * ratios[levelIndex]);
		
		//元宝普通
		BuyPrice normal = new BuyPrice();
		normal.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_NORMAL, 100);
		normal.price = (int)Math.round(power * normalRatio * yuanbaoUnit * ratios[levelIndex]);

		//元宝坚固
		BuyPrice solid = new BuyPrice();
		solid.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SOLID, 200);
		solid.price = (int)Math.round(power * toughRatio * yuanbaoUnit * ratios[levelIndex]);

		//元宝恒久
		//BuyPrice external = new BuyPrice();
		//external.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_ETERNAL, Integer.MAX_VALUE);

		ArrayList<BuyPrice> prices = new ArrayList<BuyPrice>();
		prices.add(simple);
		prices.add(normal);
		prices.add(solid);
		//prices.add(external);

		ShopPojo shopPojo = new ShopPojo();
		shopPojo.setBuyPrices(prices);
		shopPojo.setInfo(weapon.getName());
		shopPojo.setPropInfoId(weapon.getId());
		shopPojo.setMoneyType(MoneyType.YUANBAO);

		ArrayList<ShopCatalog> shopCatalogs = new ArrayList<ShopCatalog>();
		ShopCatalog catalog = ShopManager.convertSlotToCatalog(weapon.getSlot());
		shopCatalogs.add(catalog);
		shopPojo.setCatalogs(shopCatalogs);
		shopPojo.setDiscount(100);
		shopPojo.setLevel(weapon.getUserLevel());
		shopPojo.setBanded(1);
		
		return shopPojo;
	}
}
