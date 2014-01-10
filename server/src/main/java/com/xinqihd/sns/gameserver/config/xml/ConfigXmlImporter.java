package com.xinqihd.sns.gameserver.config.xml;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.CurrencyUnit;
import com.xinqihd.sns.gameserver.config.DailyMarkPojo;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.MapPojo;
import com.xinqihd.sns.gameserver.config.MapPojo.Enemy;
import com.xinqihd.sns.gameserver.config.MapPojo.Layer;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.config.TipPojo;
import com.xinqihd.sns.gameserver.config.VipPeriodPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardCondition;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Import "equipment_config.xml" into mongodb
 * @author wangqi
 *
 */
public class ConfigXmlImporter {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigXmlImporter.class);
	
	/**
	 * Import the item_config.xml into mongodb
	 * @param filePath
	 * @param database
	 * @param namespace
	 * @param collection
	 */
	public static void importItemXml(String filePath, String database, String namespace, String collection, HashSet<String> itemIdSet ) {
		List<ItemPojo> itemPojos = new ArrayList<ItemPojo>();
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_UTF8));
			XMLStreamReader xmlr = XMLInputFactory.newFactory().createXMLStreamReader(br);
			ItemPojo itemPojo = null;
			Reward reward = null;
			RewardCondition condition = null;
			String eleName = null;
			String type = null;
			while ( xmlr.hasNext() ) {
				switch ( xmlr.next() ) {
					case XMLEvent.START_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( CONSUMABLE.equals(eleName) ) {
							type = CONSUMABLE;
						} else if ( ITEM.equals(eleName)  ) {
							int count = xmlr.getAttributeCount();
							if ( count > 0 ) {
								itemPojo = new ItemPojo();
								itemPojo.setType(type);
								for ( int i=0; i<count; i++ ) {
									String attrName = xmlr.getAttributeLocalName(i);
									String attrValue = xmlr.getAttributeValue(i);
							    if (ID.equals(attrName) ) {
							    	//Remove the string prefix
							    	//wangqi 2012-02-07
							    	//itemPojo.setId(StringUtil.concat(Constant.ITEM, Constant.UNDERLINE, attrValue));
							    	itemPojo.setId(attrValue);
							    	itemIdSet.add(attrValue);
							    } else if (TYPEID.equals(attrName) ) {
							    	itemPojo.setTypeId(attrValue);
							    } else if (LV.equals(attrName) ) {
							    	//Remove the string prefix
							    	//wangqi 2012-02-07
//							    	itemPojo.setId(StringUtil.concat(itemPojo.getId(), Constant.UNDERLINE, attrValue));
							    	itemPojo.setLevel(StringUtil.toInt(attrValue, 0));
							    } else if (ICON.equals(attrName) ) {
							    	itemPojo.setIcon(attrValue);
							    } else if (NAME.equals(attrName) ) {
							    	itemPojo.setName(attrValue);
							    } else if (INFO.equals(attrName) ) {
							    	itemPojo.setInfo(attrValue);
							    	if ( attrValue.contains("礼包") ) {
							    		itemPojo.setEquipType(EquipType.GIFT_PACK);
							    	}
							    } else if (SCRIPT.equals(attrName) ) {
							    	String script = "script.box.".concat(attrValue);
							    	itemPojo.setScript(script);
							    } else if (COUNT.equals(attrName) ) {
							    	itemPojo.setCount(StringUtil.toInt(attrValue, 0));
							    } else if (Q.equals(attrName) ) {
							    	try {
											itemPojo.setQ(Double.parseDouble(attrValue));
										} catch (Exception e) {
											e.printStackTrace();
											itemPojo.setQ(1.0);
										}
							    }
								}
							}
						} else if ( REWARD.equals(eleName) ) {
							reward = new Reward();
							int count = xmlr.getAttributeCount();
							for ( int i=0; i<count; i++ ) {
								String attrName = xmlr.getAttributeLocalName(i);
								String attrValue = xmlr.getAttributeValue(i);
						    if (TYPE.equals(attrName) ) {
						    	RewardType rewardType = RewardType.valueOf(attrValue);
						    	reward.setType(rewardType);
						    } else if (ID.equals(attrName) ) {
						    	reward.setPropId(attrValue);
						    } else if (LEVEL.equals(attrName) ) {
						    	reward.setPropLevel(StringUtil.toInt(attrValue, 0));
						    } else if (COUNT.equals(attrName) ) {
						    	reward.setPropCount(StringUtil.toInt(attrValue, 1));
						    } else if (INDATE.equals(attrName) ) {
						    	reward.setPropIndate(StringUtil.toInt(attrValue, 0));
						    }
							}
						} else if ( "condition".equals(eleName) ) {
							condition = new RewardCondition();
							int count = xmlr.getAttributeCount();
							for ( int i=0; i<count; i++ ) {
								String attrName = xmlr.getAttributeLocalName(i);
								String attrValue = xmlr.getAttributeValue(i);
						    if (TYPE.equals(attrName) ) {
						    	RewardType rewardType = RewardType.valueOf(attrValue);
						    	condition.setRewardType(rewardType);
						    } else if (ID.equals(attrName) ) {
						    	condition.setId(attrValue);
						    } else if (COUNT.equals(attrName) ) {
						    	condition.setCount(StringUtil.toInt(attrValue, 1));
						    }
							}
						}
						break;
					case XMLEvent.END_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( ITEM.equals(eleName) ) {
							//Elements like 'clothes' has same name between parent and child. So
							//the weapon may be null.
							if ( itemPojo != null ) {
								itemPojos.add(itemPojo);
								itemPojo = null;
							}
						} else if ( ITEMS.equals(eleName) ) {
//							itemsPojos.add(itemsPojo);
						} else if ( REWARD.equals(eleName) ) {
							itemPojo.addReward(reward);
							reward = null;
						} else if ( "condition".equals(eleName) ) {
							itemPojo.addConditions(condition);
							condition = null;
						}
						eleName = null;
						break;
					case XMLEvent.CHARACTERS:
						break;
					default:
						break;
				}
			}
		} catch (Exception e) {
			logger.warn("Import errors: ", e);
		}
		
		for ( ItemPojo item : itemPojos ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(item);
			DBObject query = MongoDBUtil.createDBObject("_id", item.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * Parse the xml file to Pojo objects
	 * @param xmlFilePath
	 */
	public static void importMapXml(String filePath, String database, String namespace, String collection) {
		List<MapPojo> mapPojoList = new ArrayList<MapPojo>();
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_UTF8));
			XMLStreamReader xmlr = XMLInputFactory.newFactory().createXMLStreamReader(br);
			int elementCount = 0;
			MapPojo mapPojo = null;
			String eleName = null;
			int pointType = -1;
			int bossIndex = 0;
			int enemyIndex = 0;
			while ( xmlr.hasNext() ) {
				switch ( xmlr.next() ) {
					case XMLEvent.START_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( MAP.equals(eleName) ) {
							mapPojo = new MapPojo();
						} else if ( SCROLLAREA.equals(eleName) ) {
							int scrollAreaX = Integer.parseInt(xmlr.getAttributeValue(null, X_A));
							mapPojo.setScrollAreaX(scrollAreaX);
							int scrollAreaY = Integer.parseInt(xmlr.getAttributeValue(null, Y_A));
							mapPojo.setScrollAreaY(scrollAreaY);
							int scrollAreaWidth = Integer.parseInt(xmlr.getAttributeValue(null, WIDTH_A));
							mapPojo.setScrollAreaWidth(scrollAreaWidth);
							int scrollAreaHeight = Integer.parseInt(xmlr.getAttributeValue(null, HEIGHT_A));
							mapPojo.setScrollAreaHeight(scrollAreaHeight);
						} else if ( LAYER.equals(eleName) ) {
							String id = xmlr.getAttributeValue(null, ID);
							int num = Integer.parseInt(xmlr.getAttributeValue(null, NUM_A));
							int scrollRate = Integer.parseInt(xmlr.getAttributeValue(null, SCROLLRATE_A));
							int width = StringUtil.toInt(xmlr.getAttributeValue(null, W_A), 0);
							int height = StringUtil.toInt(xmlr.getAttributeValue(null, H_A), 0);
							MapPojo.Layer layer = new MapPojo.Layer();
							layer.id = id;
							layer.num = num;
							layer.width = width;
							layer.height = height;
							layer.scrollRate = scrollRate;
							mapPojo.getLayers().add(layer);
						} else if ( BOSSID.equals(eleName) ) {
							mapPojo.setBosses(new ArrayList<Enemy>(1));
						} else if ( BOSS.equals(eleName) ) {
							MapPojo.Enemy boss = new MapPojo.Enemy();
							boss.id = xmlr.getAttributeValue(null, ID);
							mapPojo.getBosses().add(boss);
						} else if ( ENEMYID.equals(eleName) ) {
							mapPojo.setEnemies(new ArrayList<Enemy>(1));
						} else if ( ENEMY.equals(eleName) ) {
							MapPojo.Enemy enemy = new MapPojo.Enemy();
							enemy.id = xmlr.getAttributeValue(null, ID);
							mapPojo.getEnemies().add(enemy);
						} else if ( BOSSPOINT.equals(eleName) ) {
							bossIndex = 0;
							pointType = POINT_BOSS;
						} else if ( ENEMYPOINT.equals(eleName) ) {
							enemyIndex = 0;
							pointType = POINT_ENEMY;
						} else if ( STARTPOINT.equals(eleName) ) {
							pointType = POINT_START;
							mapPojo.setStartPoints(new ArrayList<MapPojo.Point>(8));
						} else if ( POINT.equals(eleName) ) {
							int x = Integer.parseInt(xmlr.getAttributeValue(null, X_A));
							int y = Integer.parseInt(xmlr.getAttributeValue(null, Y_A));
							//Adjust the x,y point
							if ( mapPojo.getLayers().size() > 0 ) {
								Layer fgLayer = null;
								for ( Layer layer : mapPojo.getLayers() ) {
									if ( layer.id.endsWith("_bg") || layer.id.endsWith("_mid") ) {
										continue;
									}
									fgLayer = layer;
									break;
								}
								int height = fgLayer.height;
								y = y - (mapPojo.getScrollAreaHeight() - fgLayer.height);
							}
							MapPojo.Point point = new MapPojo.Point(x, y);
							switch(pointType) {
								case POINT_BOSS:
									Enemy boss = mapPojo.getBosses().get(bossIndex++);
									boss.x = point.x;
									boss.y = point.y;
									break;
								case POINT_ENEMY:
									Enemy enemy = mapPojo.getEnemies().get(enemyIndex++);
									enemy.x = point.x;
									enemy.y = point.y;
									break;
								case POINT_START:
									mapPojo.getStartPoints().add(point);
									break;
							}
						}
						int count = xmlr.getAttributeCount();
						for ( int i=0; i<count; i++ ) {
							String data = xmlr.getAttributeValue(i);
						}
						break;
					case XMLEvent.END_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( MAP.equals(eleName) ) {
							mapPojoList.add(mapPojo);
							mapPojo = null;
						}
						eleName = null;
						break;
					case XMLEvent.CHARACTERS:
						String data = xmlr.getText();
						String value = data.trim();
						if ( value.length() > 0 ) {
							if ( ID.equals(eleName) ) {
								mapPojo.setId(value);
								logger.debug(" id: " + value );
							} else if ( NAME.equals(eleName) ) {
								mapPojo.setName(value);
							} else if ( TYPE.equals(eleName) ) {
								mapPojo.setType(Integer.parseInt(value));
							} else if ( REQLV.equals(eleName) ) {
								mapPojo.setReqlv(Integer.parseInt(value));
							} else if ( LAYERNUM.equals(eleName) ) {
								int layerNum = Integer.parseInt(value);
								ArrayList<MapPojo.Layer> layerArray = new ArrayList<MapPojo.Layer>(layerNum);
								mapPojo.setLayers(layerArray);
							} else if ( BGM.equals(eleName) ) {
								mapPojo.setBgm(value);
							} else if ( DAMAGE.equals(eleName) ) {
								if ( "1".equals(value) ) {
									mapPojo.setDamage(true);
								} else {
									mapPojo.setDamage(false);
								}
							}
						}
						break;
					default:
						break;
				}
			}
		} catch (Exception e) {
			logger.warn("Import errors: ", e);
		}
		
		for ( MapPojo map : mapPojoList ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(map);
			DBObject query = MongoDBUtil.createDBObject("_id", map.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * Import the equipment_config.xml into mongo database.
	 * @param filePath
	 * @param database
	 * @param namespace
	 * @param collection
	 */
	public static void importEquipXml(String filePath, String database, String namespace, String collection) {
		List<WeaponPojo> weaponPojos = new ArrayList<WeaponPojo>();
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_UTF8));
			XMLStreamReader xmlr = XMLInputFactory.newFactory().createXMLStreamReader(br);
			WeaponPojo weaponPojo = null;
			String eleName = null;
			while ( xmlr.hasNext() ) {
				switch ( xmlr.next() ) {
					case XMLEvent.START_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( WEAPON.equals(eleName)  || OFFHANDWEAPON.equals(eleName)  || EXPRESSION.equals(eleName)  || 
								FACE.equals(eleName)  || DECORATION.equals(eleName)  || HAIR.equals(eleName)  || WING.equals(eleName)  || 
								CLOTHES.equals(eleName)  || HAT.equals(eleName)  || GLASSES.equals(eleName)  || JEWELRY.equals(eleName)  || 
								BUBBLE.equals(eleName)  || SUIT.equals(eleName)  ) {
							int count = xmlr.getAttributeCount();
							if ( count > 0 ) {
								weaponPojo = new WeaponPojo();
								for ( int i=0; i<count; i++ ) {
									String attrName = xmlr.getAttributeLocalName(i);
									String attrValue = xmlr.getAttributeValue(i);
							    if (BUBBLE.equals(attrName) ) {
							    	weaponPojo.setBubble(attrValue);
							    } else if (ADD_AGILITY.equals(attrName) ) {
							    	weaponPojo.setAddAgility(StringUtil.toInt(attrValue, 0));
							    } else if (ADD_ATTACK.equals(attrName) ) {
							    	weaponPojo.setAddAttack(StringUtil.toInt(attrValue, 0));
							    } else if (ADD_BLOOD.equals(attrName) ) {
							    	weaponPojo.setAddBlood(StringUtil.toInt(attrValue, 0));
							    } else if (ADD_DAMAGE.equals(attrName) ) {
							    	weaponPojo.setAddDamage(StringUtil.toInt(attrValue, 0));
							    } else if (ADD_DEFEND.equals(attrName) ) {
							    	weaponPojo.setAddDefend(StringUtil.toInt(attrValue, 0));
							    } else if (ADD_LUCK.equals(attrName) ) {
							    	weaponPojo.setAddLuck(StringUtil.toInt(attrValue, 0));
							    } else if (ADD_SKIN.equals(attrName) ) {
							    	weaponPojo.setAddSkin(StringUtil.toInt(attrValue, 0));
							    } else if (ADD_THEW.equals(attrName) ) {
							    	weaponPojo.setAddThew(StringUtil.toInt(attrValue, 0));
							    } else if (AUTODESTORY.equals(attrName) ) {
							    	weaponPojo.setAutoDestory(StringUtil.toInt(attrValue, 0));
							    } else if (AUTODIRECTION.equals(attrName) ) {
							    	weaponPojo.setAutoDirection(StringUtil.toInt(attrValue, 0));
							    } else if (BLOOD_PERCENT.equals(attrName) ) {
							    	weaponPojo.setAddBloodPercent(StringUtil.toInt(attrValue, 0));
							    } else if (BULLET.equals(attrName) ) {
							    	weaponPojo.setBullet(attrValue);
							    } else if (EQUIP_TYPE.equals(attrName) ) {
							    	weaponPojo.setEquipType(StringUtil.toInt(attrValue, 0));
							    } else if (EXPBLEND.equals(attrName) ) {
							    	weaponPojo.setExpBlend(attrValue);
							    } else if (EXPSE.equals(attrName) ) {
							    	weaponPojo.setExpSe(StringUtil.toInt(attrValue, 0));
							    } else if (ICON.equals(attrName) ) {
							    	weaponPojo.setIcon(attrValue);
							    } else if (ID.equals(attrName) ) {
							    	weaponPojo.setId(attrValue);
							    } else if (INDATE1.equals(attrName) ) {
							    	weaponPojo.setIndate1(StringUtil.toInt(attrValue, 0));
							    } else if (INDATE2.equals(attrName) ) {
							    	weaponPojo.setIndate2(StringUtil.toInt(attrValue, 0));
							    } else if (INDATE3.equals(attrName) ) {
							    	weaponPojo.setIndate3(StringUtil.toInt(attrValue, 0));
							    } else if (INDEX.equals(attrName) ) {
							    	weaponPojo.setIndate1(StringUtil.toInt(attrValue, 0));
							    } else if (INFO.equals(attrName) ) {
							    	weaponPojo.setInfo(attrValue);
							    } else if (LV.equals(attrName) ) {
							    	weaponPojo.setLv(StringUtil.toInt(attrValue, 0));
							    } else if (NAME.equals(attrName) ) {
							    	weaponPojo.setName(attrValue);
							    } else if (POWER.equals(attrName) ) {
							    	weaponPojo.setPower(StringUtil.toInt(attrValue, 0));
							    } else if (QUALITY.equals(attrName) ) {
							    	weaponPojo.setQuality(StringUtil.toInt(attrValue, 0));
							    } else if (RADIUS.equals(attrName) ) {
							    	weaponPojo.setRadius(StringUtil.toInt(attrValue, 0));
							    } else if (SAUTODIRECTION.equals(attrName) ) {
							    	weaponPojo.setsAutoDirection(StringUtil.toInt(attrValue, 0));
							    } else if (SRADIUS.equals(attrName) ) {
							    	weaponPojo.setsRadius(StringUtil.toInt(attrValue, 0));
							    } else if (S_NAME.equals(attrName) ) {
							    	weaponPojo.setsName(attrValue);
							    } else if (SEX.equals(attrName) ) {
							    	weaponPojo.setSex(Gender.values()[StringUtil.toInt(attrValue, 1)]);
							    } else if (SIGN.equals(attrName) ) {
							    	weaponPojo.setSign(StringUtil.toInt(attrValue, 0));
							    } else if (SLOT.equals(attrName) ) {
							    	weaponPojo.setSlot(EquipType.valueOf(attrValue.toUpperCase()));
							    } else if (SPECIALACTION.equals(attrName) ) {
							    	weaponPojo.setSpecialAction(StringUtil.toInt(attrValue, 0));
							    } else if (UNUSED1.equals(attrName) ) {
							    	weaponPojo.setUnused1(StringUtil.toInt(attrValue, -1));
							    } else if (UNUSED2.equals(attrName) ) {
							    	weaponPojo.setUnused2(StringUtil.toInt(attrValue, 0));
							    } else if (UNUSED3.equals(attrName) ) {
							    	weaponPojo.setUnused3(StringUtil.toInt(attrValue, 0));
							    }
								}
							}
						} else if ( AVATAR.equals(eleName) ) {
							weaponPojo.setAvatar(new ArrayList<WeaponPojo.Avatar>(2));
						} else if ( PART.equals(eleName) ) {
							String id = xmlr.getAttributeValue(null, ID);
							if ( !"null".equals(id) ) {
								WeaponPojo.Avatar avatar = new WeaponPojo.Avatar();
								avatar.id = id;
								avatar.layer = xmlr.getAttributeValue(null, LAYER);
								weaponPojo.getAvatar().add(avatar);
							}
						}
						break;
					case XMLEvent.END_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( WEAPON.equals(eleName)  || OFFHANDWEAPON.equals(eleName)  || EXPRESSION.equals(eleName)  || 
								FACE.equals(eleName)  || DECORATION.equals(eleName)  || HAIR.equals(eleName)  || WING.equals(eleName)  || 
								CLOTHES.equals(eleName)  || HAT.equals(eleName)  || GLASSES.equals(eleName)  || JEWELRY.equals(eleName)  || 
								BUBBLE.equals(eleName)  || SUIT.equals(eleName)  ) {
							//Elements like 'clothes' has same name between parent and child. So
							//the weapon may be null.
							if ( weaponPojo != null ) {
								weaponPojos.add(weaponPojo);
								weaponPojo = null;
							}
						}
						eleName = null;
						break;
					case XMLEvent.CHARACTERS:
						break;
					default:
						break;
				}
			}
		} catch (Exception e) {
			logger.warn("Import errors: ", e);
		}
		
		for ( WeaponPojo weapon : weaponPojos ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(weapon);
			DBObject query = MongoDBUtil.createDBObject("_id", weapon.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * Parse the dat file and generate all the ShopPojo objects.
	 */
	public static void importShopDatFile(String filePath, String database, String namespace, String collection, HashSet<String> itemIdSet) {
		List<ShopPojo> pojoList = new ArrayList<ShopPojo>();
		File file = new File(filePath);
		if ( file.exists() && file.isFile() ) {
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_GBK));
				String line = br.readLine();
				int lineNum = 0;
				while ( line != null ) {
					lineNum++;
					if ( lineNum <= 2 ) {
						line = br.readLine();
						continue;
					}
					String[] fields = line.split(TAB);
					ShopPojo shopPojo = new ShopPojo();
					shopPojo.setId(fields[0]);
					shopPojo.setType(toInt(fields[1], 0));
					shopPojo.setInfo(fields[2]);
					boolean isItem = false;
					if ( itemIdSet.contains(fields[3]) ) {
						//It is an item, change the id format
						/*
						if ( fields[4].length() == 0 ) {
							fields[4] = "0";
						}
						String id = "item_" + fields[3] + "_" + fields[4];
						*/
						String id = fields[3];
						shopPojo.setPropInfoId(id);
						isItem = true;
					} else {
						shopPojo.setPropInfoId(fields[3]);
					}
					shopPojo.setItem(isItem);
					shopPojo.setLevel(toInt(fields[4], 0));
					shopPojo.setMoneyType(MoneyType.fromType(toInt(fields[5], 0)));
					List<BuyPrice> buyPrices = new ArrayList<BuyPrice>(4);
					//price1
					BuyPrice buyPrice = new BuyPrice();
					buyPrice.price = toInt(fields[6], 0);
					buyPrice.validTimes = toInt(fields[10], 0);
					buyPrices.add(buyPrice);
					//price2
					buyPrice = new BuyPrice();
					buyPrice.price = toInt(fields[7], 0);
					buyPrice.validTimes = toInt(fields[11], 0);
					buyPrices.add(buyPrice);
					//price3
					buyPrice = new BuyPrice();
					buyPrice.price = toInt(fields[8], 0);
					buyPrice.validTimes = toInt(fields[12], 0);
					buyPrices.add(buyPrice);
					//price4
					buyPrice = new BuyPrice();
					buyPrice.price = toInt(fields[9], 0);
					buyPrice.validTimes = toInt(fields[13], 0);
					buyPrices.add(buyPrice);
					shopPojo.setBuyPrices(buyPrices);
					
					shopPojo.setBanded(toInt(fields[14],0));
					shopPojo.setDiscount(toInt(fields[15], 0));
					shopPojo.setSell(toInt(fields[16],0));
					int hot = toInt(fields[17], 0);
					if ( hot == 1 ) {
						shopPojo.addCatalog(ShopCatalog.HOT);
						shopPojo.addCatalog(ShopCatalog.RECOMMEND);
					}
					boolean isFound = true;
					if ( isItem ) {
						ItemPojo item = ItemManager.getInstance().getItemById(shopPojo.getPropInfoId());
						if ( item != null ) {
							shopPojo.addCatalog(ShopCatalog.getShopCatalogByEquipType(item.getEquipType()));
						} else {
							logger.debug(shopPojo + " is not found.");
							isFound = false;
						}
					} else {
						WeaponPojo item = EquipManager.getInstance().getWeaponById(shopPojo.getPropInfoId());
						if ( item != null ) {
							shopPojo.addCatalog(ShopCatalog.getShopCatalogByEquipType(item.getSlot()));
						} else {
							logger.debug(shopPojo + " is not found.");
							isFound = false;
						}
					}
					shopPojo.setShopId(toInt(fields[18],0));
					shopPojo.setLimitCount(toInt(fields[19],0));
					shopPojo.setLimitGroup(toInt(fields[20],0));
					if ( isFound ) {
						pojoList.add(shopPojo);
					}
					line = br.readLine();
				}
			} catch (Exception e) {
				logger.error("Failed to import shop data file", e);
			}
		} else {
			logger.warn("The shop data file " + file.getAbsolutePath() + " does not exist.");
		}
		
		for ( ShopPojo shop : pojoList ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(shop);
			DBObject query = MongoDBUtil.createDBObject("_id", shop.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * 
	 * @param filePath
	 * @param database
	 * @param namespace
	 * @param collection
	 */
	public static void importTaskXml(String filePath, String database, String namespace, String collection) {		
		List<TaskPojo> taskList = new ArrayList<TaskPojo>();
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_UTF8));
			XMLStreamReader xmlr = XMLInputFactory.newFactory().createXMLStreamReader(br);
			TaskPojo taskPojo = null;
			TaskType taskType = null;
			Award award = null;
			String eleName = null;
			while ( xmlr.hasNext() ) {
				switch ( xmlr.next() ) {
					case XMLEvent.START_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( TASKS.equals(eleName) ) {
							String attrValue = xmlr.getAttributeValue(null, ID);
							int index = toInt(attrValue, 1) - 1;
							//May be ArrayOutofRange
							taskType = TaskType.values()[index];
						} else if ( TASK.equals(eleName)  ) {
							int count = xmlr.getAttributeCount();
							if ( count > 0 ) {
								taskPojo = new TaskPojo();
								taskPojo.setType(taskType);
								for ( int i=0; i<count; i++ ) {
									String attrName = xmlr.getAttributeLocalName(i);
									String attrValue = xmlr.getAttributeValue(i);
							    if (ID.equals(attrName) ) {
							    	taskPojo.setId(attrValue);
							    } else if (NAME.equals(attrName) ) {
							    	taskPojo.setName(attrValue);
							    } else if (DESC.equals(attrName) ) {
							    	taskPojo.setDesc(attrValue);
							    } else if (TASKTARGET.equals(attrName) ) {
							    	taskPojo.setTaskTarget(attrValue);
							    } else if (STEP.equals(attrName) ) {
							    	taskPojo.setStep(StringUtil.toInt(attrValue, 0));
							    } else if (LEVEL.equals(attrName) ) {
							    	taskPojo.setCondition1(StringUtil.toInt(attrValue, 0));
							    } else if (PARENT.equals(attrName) ) {
							    	//Note, we do not use the value. Instead, 
							    	//use the parent element's (tasks) id.
//							    	taskPojo.setParent(taskType);
							    } else if (EXP.equals(attrName) ) {
							    	taskPojo.setExp(StringUtil.toInt(attrValue, 0));
							    } else if (GOLD.equals(attrName) ) {
							    	taskPojo.setGold(StringUtil.toInt(attrValue, 0));
							    } else if (TICKET.equals(attrName) ) {
							    	taskPojo.setTicket(StringUtil.toInt(attrValue, 0));
							    } else if (GONGXUN.equals(attrName) ) {
							    	taskPojo.setGongxun(StringUtil.toInt(attrValue, 0));
							    } else if (CAIFU.equals(attrName) ) {
							    	taskPojo.setCaifu(StringUtil.toInt(attrValue, 0));
							    } else if (SEQ.equals(attrName) ) {
							    	taskPojo.setSeq(StringUtil.toInt(attrValue, 0));
							    } else if (USER_LEVEL.equals(attrName) ) {
							    	taskPojo.setUserLevel(StringUtil.toInt(attrValue, 0));
							    } else if (SCRIPT.equals(attrName) ) {
							    	taskPojo.setScript(attrValue);
							    }
								}
							}
						} else if ( AWARD.equals(eleName)  ) {
							award = new Award();
							int count = xmlr.getAttributeCount();
							for ( int i=0; i<count; i++ ) {
								String attrName = xmlr.getAttributeLocalName(i);
								String attrValue = xmlr.getAttributeValue(i);
						    if (ID.equals(attrName) ) {
						    	award.typeId = toInt(attrValue, 0);
						    } else if (TYPE.equals(attrName) ) {
						    	award.type = attrValue;
						    } else if (LV.equals(attrName) ) {
						    	award.lv = toInt(attrValue, 0);
//						    	if ( award.typeId <= 20005 ) {
//						    		int id = 20000 + (award.typeId-20001)*5 + award.lv;
//						    		award.id = String.valueOf(id);
//						    	}
						    	if ( award.typeId > 20000 ) {
							    	String id = ItemPojo.toId(""+award.typeId, award.lv);
							    	if ( id != null ) {
							    		award.id = id;
							    	}
						    	}
						    	if ( award.id == null ) {
							    		award.id = String.valueOf(award.typeId);
						    	}
						    } else if (SEX.equals(attrName) ) {
						    	award.sex = Gender.values()[(toInt(attrValue, 0))];
						    } else if (COUNT.equals(attrName) ) {
						    	award.count = toInt(attrValue, 0);
						    } else if (INDATE.equals(attrName) ) {
						    	award.indate = toInt(attrValue, 0);
						    }
							}
						}
						break;
					case XMLEvent.END_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( TASK.equals(eleName) ) {
							taskList.add(taskPojo);
							taskPojo = null;
						} else if ( TASKS.equals(eleName) ) {
							taskType = null;
						} else if ( AWARD.equals(eleName)  ) {
							taskPojo.addAward(award);
						} else if ( TASKLIST.equals(eleName) ) {
//							taskList.add(taskListPojo);
						}
						eleName = null;
						break;
					case XMLEvent.CHARACTERS:
						break;
					default:
						break;
				}
			}
		} catch (Exception e) {
			logger.warn("Import errors: ", e);
		}
		
		for ( TaskPojo task : taskList ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(task);
			DBObject query = MongoDBUtil.createDBObject("_id", task.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}

	/**
	 * Parse the dat file and generate all the DailyMarkPojo objects.
	 */
	public static void importDailyMarkDatFile(String filePath, String database, String namespace, String collection) {
		List<DailyMarkPojo> pojoList = new ArrayList<DailyMarkPojo>();
		File file = new File(filePath);
		if ( file.exists() && file.isFile() ) {
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_GBK));
				String line = br.readLine();
				int lineNum = 0;
				while ( line != null ) {
					lineNum++;
					if ( lineNum <= 2 ) {
						line = br.readLine();
						continue;
					}
					String[] fields = line.split(TAB);
					DailyMarkPojo markPojo = new DailyMarkPojo();
					markPojo.setId(fields[0]);
					markPojo.setStep(StringUtil.toInt(fields[0], 1));
					markPojo.setDayNum(StringUtil.toInt(fields[1], 1));
					int count = (fields.length-2)/3;
					ArrayList<Reward> rewards = new ArrayList<Reward>(count);
					for ( int i=0; i<count; i++ ) {
						Reward reward = new Reward();
						String idStr = fields[i*3+2];
						reward.setPropId(idStr);
						reward.setPropLevel(StringUtil.toInt(fields[i*3+3], 1));
						reward.setPropCount(StringUtil.toInt(fields[i*3+4], 1));
						int id = StringUtil.toInt(idStr, -1);
						RewardType type = null;
					  //金币:-1
					  //礼券:-2
					  //元宝:-3
					  //勋章:-4
						switch ( id ) {
							case -1:
								type = RewardType.GOLDEN;
								break;
							case -2:
								type = RewardType.VOUCHER;
								break;
							case -3:
								type = RewardType.YUANBAO;
								break;
							case -4:
								type = RewardType.MEDAL;
								break;
							default:
								if (id>20000) {
									type = RewardType.ITEM;
								} else {
									type = RewardType.WEAPON;
								}
						}
						reward.setType(type);
						rewards.add(reward);
					}
					markPojo.setRewards(rewards);
					pojoList.add(markPojo);
					line = br.readLine();
				}
			} catch (Exception e) {
				logger.error("Failed to import dailyMark data file", e);
			}
		} else {
			logger.warn("The dailyMark data file " + file.getAbsolutePath() + " does not exist.");
		}
		
		for ( DailyMarkPojo dailyPojo : pojoList ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(dailyPojo);
			DBObject query = MongoDBUtil.createDBObject("_id", dailyPojo.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * Import the loginlottery_config.xml into mongodb
	 * @param filePath
	 * @param database
	 * @param namespace
	 * @param collection
	 */
	public static void importTipXml(String filePath, String database, String namespace, String collection ) {
		List<TipPojo> tipPojos = new ArrayList<TipPojo>();
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_UTF8));
			XMLStreamReader xmlr = XMLInputFactory.newFactory().createXMLStreamReader(br);
			TipPojo tipPojo = null;
			String eleName = null;
			String type = null;
			while ( xmlr.hasNext() ) {
				switch ( xmlr.next() ) {
					case XMLEvent.START_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( "tip".equals(eleName)  ) {
							int count = xmlr.getAttributeCount();
							if ( count > 0 ) {
								tipPojo = new TipPojo();
								for ( int i=0; i<count; i++ ) {
									String attrName = xmlr.getAttributeLocalName(i);
									String attrValue = xmlr.getAttributeValue(i);
							    if ("context".equals(attrName) ) {
							    	tipPojo.setTip(attrValue);
							    }
								}
							}
						}
						break;
					case XMLEvent.END_ELEMENT:
						eleName = xmlr.getLocalName();
						if ( "tip".equals(eleName) ) {
							if ( tipPojo != null ) {
								tipPojos.add(tipPojo);
								tipPojo = null;
							}
						}
						eleName = null;
						break;
					case XMLEvent.CHARACTERS:
						break;
					default:
						break;
				}
			}
		} catch (Exception e) {
			logger.warn("Import errors: ", e);
		}
		
		for ( TipPojo tip : tipPojos ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(tip);
			DBObject query = MongoDBUtil.createDBObject("tip", tip.getTip());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 *  0.99 : 75
	 *  1.99 : 160
	 *  2.99 : 250
	 *  4.99 :  450
	 *  9.99 : 900
	 * 19.99 : 2000
	 * 49.99 : 6500
	 * 99.99 : 18000
	 * 
	 * Import Charge data
	 * @param filePath
	 * @param database
	 * @param namespace
	 * @param collection
	 */
	public static void importChargePojo(String database, String namespace, String collection ) {
		ArrayList<ChargePojo> charges = new ArrayList<ChargePojo>();
		ChargePojo pojo = null;
		pojo = new ChargePojo();
		//0.99 : 75
		pojo.setId(0);
		pojo.setDiscount(0);
		pojo.setHotSale(false);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(0.99f);
		pojo.setYuanbao(75);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setId(1);
		pojo.setPrice(6);
		charges.add(pojo);
		
		//1.99 : 160
		pojo = new ChargePojo();
		pojo.setId(2);
		pojo.setDiscount(9.4f);
		pojo.setHotSale(false);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(1.99f);
		pojo.setYuanbao(160);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setId(3);
		pojo.setPrice(12);
		charges.add(pojo);
		
		//2.99 : 250
		pojo = new ChargePojo();
		pojo.setId(4);
		pojo.setDiscount(9.0f);
		pojo.setHotSale(false);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(2.99f);
		pojo.setYuanbao(250);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setId(5);
		pojo.setPrice(18);
		charges.add(pojo);
		
		//4.99 :  450
		pojo = new ChargePojo();
		pojo.setId(6);
		pojo.setDiscount(8.4f);
		pojo.setHotSale(true);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(4.99f);
		pojo.setYuanbao(450);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setId(7);
		pojo.setPrice(30);
		charges.add(pojo);
		
		//9.99 : 900
		pojo = new ChargePojo();
		pojo.setId(8);
		pojo.setDiscount(8.4f);
		pojo.setHotSale(false);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(9.99f);
		pojo.setYuanbao(900);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setId(9);
		pojo.setPrice(60);
		charges.add(pojo);
		
		//19.99 : 2000
		pojo = new ChargePojo();
		pojo.setId(10);
		pojo.setDiscount(7.5f);
		pojo.setHotSale(false);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(19.99f);
		pojo.setYuanbao(2000);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setId(11);
		pojo.setPrice(120);
		charges.add(pojo);
		
		//49.99 : 6500
		pojo = new ChargePojo();
		pojo.setId(12);
		pojo.setDiscount(5.8f);
		pojo.setHotSale(false);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(49.99f);
		pojo.setYuanbao(6500);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setId(13);
		pojo.setPrice(300);
		charges.add(pojo);
		
		//99.99 : 18000
		pojo = new ChargePojo();
		pojo.setId(14);
		pojo.setDiscount(4.2f);
		pojo.setHotSale(true);
		pojo.setCurrency(CurrencyUnit.US_DOLLOR);
		pojo.setPrice(99.99f);
		pojo.setYuanbao(18000);
		charges.add(pojo);
		//Chinese
		pojo = pojo.clone();
		pojo.setId(15);
		pojo.setCurrency(CurrencyUnit.CHINESE_YUAN);
		pojo.setPrice(600);
		charges.add(pojo);
		
		for ( ChargePojo charge : charges ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(charge);
			DBObject query = MongoDBUtil.createDBObject("_id", charge.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	public static void importVipPeriodList(String database, String namespace, String collection ) {
		ArrayList<VipPeriodPojo> list = new ArrayList<VipPeriodPojo>();
		VipPeriodPojo pojo = new VipPeriodPojo();
		pojo.setMonth(1);
		pojo.setYuanbaoPrice(70);
		pojo.setMedalPrice(140);
		pojo.setVoucherPrice(210);
		list.add(pojo);
		
		pojo = new VipPeriodPojo();
		pojo.setMonth(2);
		pojo.setYuanbaoPrice(132);
		pojo.setMedalPrice(264);
		pojo.setVoucherPrice(396);
		list.add(pojo);
		
		pojo = new VipPeriodPojo();
		pojo.setMonth(3);
		pojo.setYuanbaoPrice(189);
		pojo.setMedalPrice(378);
		pojo.setVoucherPrice(567);
		list.add(pojo);
		
		pojo = new VipPeriodPojo();
		pojo.setMonth(6);
		pojo.setYuanbaoPrice(353);
		pojo.setMedalPrice(706);
		pojo.setVoucherPrice(1058);
		list.add(pojo);
		
		pojo = new VipPeriodPojo();
		pojo.setMonth(12);
		pojo.setYuanbaoPrice(630);
		pojo.setMedalPrice(1260);
		pojo.setVoucherPrice(1890);
		list.add(pojo);
		
		pojo = new VipPeriodPojo();
		pojo.setMonth(24);
		pojo.setYuanbaoPrice(974);
		pojo.setMedalPrice(1949);
		pojo.setVoucherPrice(2923);
		list.add(pojo);
		
		for ( VipPeriodPojo vipPeriod : list ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(vipPeriod);
			DBObject query = MongoDBUtil.createDBObject("_id", vipPeriod.getMonth());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * Parse the dat file and generate all the levels objects.
	 */
	public static void importLevelsDatFile(String filePath, String database, String namespace, String collection) {
		List<LevelPojo> pojoList = new ArrayList<LevelPojo>();
		File file = new File(filePath);
		if ( file.exists() && file.isFile() ) {
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_GBK));
				String line = br.readLine();
				int lineNum = 0;
				while ( line != null ) {
					lineNum++;
					if ( lineNum <= 1 ) {
						line = br.readLine();
						continue;
					}
					String[] fields = line.split(TAB);
					LevelPojo level = new LevelPojo();
					level.set_id(StringUtil.toInt(fields[0],0));
					level.setLevel(StringUtil.toInt(fields[0],0));
					level.setExp(StringUtil.toInt(fields[1],0));
					level.setDpr(StringUtil.toInt(fields[2],0));
					level.setBlood(StringUtil.toInt(fields[3],0));
					level.setSkin(StringUtil.toInt(fields[4],0));
					level.setAttack(StringUtil.toInt(fields[5],0));
					level.setDefend(StringUtil.toInt(fields[6],0));
					level.setAgility(StringUtil.toInt(fields[7],0));
					level.setLucky(StringUtil.toInt(fields[8],0));
					pojoList.add(level);
					
					line = br.readLine();
				}
			} catch (Exception e) {
				logger.error("Failed to import shop data file", e);
			}
		} else {
			logger.warn("The shop data file " + file.getAbsolutePath() + " does not exist.");
		}
		
		for ( LevelPojo level : pojoList ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(level);
			DBObject query = MongoDBUtil.createDBObject("_id", level.get_id());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * Parse the dat file and generate all the levels objects.
	 */
	public static void importIconsFile(String filePath, String database, String namespace, String collection) {
		List<String> iconList = new ArrayList<String>();
		File file = new File(filePath);
		if ( file.exists() && file.isFile() ) {
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENC_GBK));
				String line = br.readLine();
				int lineNum = 0;
				while ( line != null ) {
					lineNum++;
					String icon = line.trim();
					iconList.add(icon);
					
					line = br.readLine();
				}
			} catch (Exception e) {
				logger.error("Failed to import icon file", e);
			}
		} else {
			logger.warn("The icon file " + file.getAbsolutePath() + " does not exist.");
		}
		
		for ( String icon : iconList ) {
			DBObject dbObject = MongoDBUtil.createDBObject("_id", icon);
			DBObject query = MongoDBUtil.createDBObject("_id", icon);
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
	
	/**
	 * Load sample xml data file into MongoDatabase
	 * @param args
	 */
	public static void main(String ... args ) {
		System.out.println("Config Import V1.0");
		String host = "mongos.babywar.xinqihd.com";
//		String host = "db.babywar.xinqihd.com";
		
		if ( args.length > 0 ) {
			host = args[0];
		}
		System.out.println("\thost:"+host);
		String database = "babywar";
		if ( args.length > 1 ) {
			database = args[1];
		}
		System.out.println("\tdatabase:"+database);
		String namespace = "server0001";
		if ( args.length > 2 ) {
			namespace = args[1];
		}
		System.out.println("\tnamespace:"+namespace);
		
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.mongo_configdb_host, host);
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.mongo_configdb_port, "27018");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.mongo_configdb_database, database);
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.mongo_configdb_namespace, namespace);
		
		int count = 0;
		
		ConfigXmlImporter.importMapXml("src/test/data/map_config_new.xml", database, namespace, "maps");
		count = (int)MongoDBUtil.countQueryResult(null, database, namespace, "maps");
		System.out.println("Total import maps: " + count);

		/*
//		ConfigXmlImporter.importEquipXml("src/test/data/equipment_config.xml", database, namespace, "equipments");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "equipments");
		System.out.println("Total import equipment: " + count);
		
//		ConfigXmlImporter.importMapXml("src/test/data/map_config.xml", database, namespace, "maps");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "maps");
		System.out.println("Total import maps: " + count);
		
		HashSet<String> itemIdSet = new HashSet<String>();
//		ConfigXmlImporter.importItemXml("src/test/data/item_config.xml", database, namespace, "items", itemIdSet);
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "items");
		System.out.println("Total import items: " + count);
		
//		ConfigXmlImporter.importShopDatFile("src/test/data/shop_multi_currency.dat", database, namespace, "shops", itemIdSet);
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "shops");
		System.out.println("Total import shops: " + count);
		
//		ConfigXmlImporter.importTaskXml("src/test/data/task_config.xml", database, namespace, "tasks");
//		count = (int)MongoUtil.countQueryResult(null, database, namespace, "tasks");
		count = 0;
		System.out.println("Total import tasks: " + count);
		
//		ConfigXmlImporter.importDailyMarkDatFile("src/test/data/dailymark_reward.dat", database, namespace, "dailymarks");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "dailymarks");
		System.out.println("Total import dailymarks: " + count);
		
//		ConfigXmlImporter.importTipXml("src/test/data/tips_config.xml", database, namespace, "tips");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "tips");
		System.out.println("Total import tips: " + count);
		
//		ConfigXmlImporter.importChargePojo(database, namespace, "charges");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "charges");
		System.out.println("Total import charges: " + count);

//		ConfigXmlImporter.importVipPeriodList(database, namespace, "vipperiods");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "vipperiods");
		System.out.println("Total import vipperiods: " + count);
		
//		ConfigXmlImporter.importLevelsDatFile("src/test/data/levels.txt", database, namespace, "levels");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "levels");
		System.out.println("Total import levels: " + count);

		ConfigXmlImporter.importIconsFile("src/test/data/icons.txt", database, namespace, "icons");
		count = (int)MongoUtil.countQueryResult(null, database, namespace, "icons");
		System.out.println("Total import icons: " + count);
		*/
	}
}
