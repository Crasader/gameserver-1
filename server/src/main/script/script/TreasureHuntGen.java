package script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.treasure.TreasurePojo;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 生成三种寻宝模式的价格、物品和概率
 * 选择物品的原则
	上四格
		*. 首武器大于玩家当前主武器2个等级
		*. 次套装大于玩家当前套装2个等级
		*. 叁武器大于玩家当前主武器1个等级
		*. 肆套装大于玩家当前套装1个等级
	下四格
		* 其中强化石比玩家背包中最高等级的强化石大1个等级
		* 颜色熔炼符比玩家背包中最好的颜色熔炼符或者主武器的颜色大1个等级
		* 精良武器熔炼符固定出现
		*.幸运符15%和25%按照70%对30%的概率出现
	武器等级判断依据
		>=N8级
			下一等级蓝色武器
			下一等级粉色武器
			下一等级橙色武器
		<=N8级
	套装等级判断依据
		没有套装
			给予普通套装
		普通套装
			给予精致套装
		精致套装
			给予更高战斗力套装
			给予强化套装
			
 * 
 * @author wangqi
 *
 */
public class TreasureHuntGen {
	
	private static final Logger logger = LoggerFactory.getLogger(TreasureHuntGen.class);
		
	private static final String[] ITEM_IDS_1 = {
		//强化石Lv4
		"20024",
		//幸运符+15%
		"24002",
		//蓝色熔炼符
		"26005",
		//精良装备熔炼符
		"26011",
		//改名卡
		"30000",
		//双倍经验卡
		"30014",
		//水晶石Lv3
		"20043",
		//黄钻石Lv3
		"20033",
	};
	
	private static final String[] ITEM_IDS_2 = {
		//强化石Lv5  
		"20025",
		//神恩符
		"24001",
		//幸运符+25%
		"24004",
		//橙色熔炼符
		"26007",
		//精良武器熔炼符
		"26010",
		//双倍经验卡
		"30014",
		//体力卡
		"99999",
		//水晶石Lv4
		"20044",
		//黄钻石Lv4
		"20034",
	};
	
	/**
		30001	帽子碎片
		30002	套装碎片
		30003	武器残片
	 */
	private static final String[] ITEM_IDS_3 = {
		"30001",
		"30002",
		"30003",
	};
	
	/**
	 * 940	黑铁●火之心
	 * 960	黑铁●风的眼泪
   * 980	黑铁●水蓝之心
	 * 1000	黑铁●火焰之魂
	 * 1020	黑铁●神之泪
	 */
	private static final String[] JEWERY_IDS_1 = {
		"94",
		"96",
		"98",
		"100",
		"102",
	};
	
	/**
	 * 950	真火之心
	 * 970	真风的眼泪
   * 990	真水蓝之心
	 * 1010	真火焰之魂
	 * 1030	真神之泪
	 */
	private static final String[] JEWERY_IDS_2 = {
		"95",
		"97",
		"99",
		"101",
		"103",
	};
	
	/**
	 * 稀有物品
	 *  4059	神圣●圣诞帽
			4049	神圣●新年雪人
			4039	神圣●迫击炮
			4029	神圣●野牛冲锋
			4019	神圣●红龙火焰
			4009	神圣●战龙狙击
			2989	神圣●王者之冠
	 */
	private static final String[] RARE_IDS = {
		"405",
		"404",
		"403",
		"402",
		"401",
		"400",
		"298"
	};
	
	/**
	 * Fiveth reward type
	 */
	private static final EquipType[] equipTypes = new EquipType[]{
		EquipType.CLOTHES, EquipType.DECORATION, EquipType.FACE, EquipType.GLASSES, EquipType.HAIR,
		EquipType.HAT, EquipType.WING
	};
	
	private static ArrayList itemList1 = new ArrayList(Arrays.asList(ITEM_IDS_1));
	private static ArrayList itemList2 = new ArrayList(Arrays.asList(ITEM_IDS_2));

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		ArrayList propDatas = (ArrayList)parameters[1];
		/**
		 * 如果上层传入了propDatas且长度不为空，则表明要使用上次保留的结果。
		 * 2013-3-1对这个功能进行了改进，为3种模式生成不同的物品列表，
		 * 为了保持程序接口的兼容，将propDatas的长度从8扩展为24，这样
		 * 3中模式的物品就都能够放入了。
		 */
		ArrayList simpleList = new ArrayList();
		ArrayList normalList = new ArrayList();
		ArrayList advanceList = new ArrayList();
		if ( propDatas == null || propDatas.size() <= 0 ) {
			simpleList = generatePropDataListForSimple(user, 1);
			normalList = generatePropDataListForNormal(user, 2);
			advanceList = generatePropDataListForAdvance(user, 2);
		} else {
			int length = propDatas.size();
			if ( length >= 8 ) {
				for ( int i=0; i<8; i++ ) {
					simpleList.add(propDatas.get(i));
				}
			}
			if ( length >= 16 ) {
				for ( int i=0; i<8; i++ ) {
					normalList.add(propDatas.get(8+i));
				}
			} else {
				normalList.addAll(simpleList);
			}
			if ( length >= 24 ) {
				for ( int i=0; i<8; i++ ) {
					advanceList.add(propDatas.get(16+i));
				}
			} else {
				advanceList.addAll(normalList);
			} 
		}
		//购买的价格
		int normalPrice = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TREASURE_HUNT_NORMAL_PRICE, 1);
		int advancePrice = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TREASURE_HUNT_ADVANCE_PRICE, 4);
		int proPrice = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TREASURE_HUNT_PRO_PRICE, 8);
		float baseRatio = 0.0f;
		if ( user.getGuildMember() != null ) {
			GuildFacility treasureFac = user.getGuildMember().getFacility(GuildFacilityType.ab_treasure);
			if ( treasureFac != null && treasureFac.getLevel()>=1 ) {
				double[] ratios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_ABILITY_TREASURE);
				baseRatio = (float)(ratios[treasureFac.getLevel()-1]);
			}
		}
		
		HashMap treasures = new HashMap();
		{
			TreasurePojo normal = new TreasurePojo();
			normal.setPrice(normalPrice);
			normal.setMode(0);
			normal.setGifts(simpleList);
			float ratio = 0.1f;
			int length = simpleList.size();
			List ratios = new ArrayList(length);
			for ( int i=1; i<=length; i++ ) {
				ratios.add(baseRatio+ratio*i*2);
			}
			normal.setRatios(ratios);
			treasures.put(normal.getMode(), normal);
		}
		{
			TreasurePojo advance = new TreasurePojo();
			advance.setPrice(advancePrice);
			advance.setMode(1);
			advance.setGifts(normalList);
			float ratio = 0.1f;
			int length = normalList.size();
			List ratios = new ArrayList(length);
			for ( int i=1; i<=length; i++ ) {
				ratios.add(baseRatio+ratio*i*2);
			}
			advance.setRatios(ratios);
			treasures.put(advance.getMode(), advance);
		}
		{
			TreasurePojo pro = new TreasurePojo();
			pro.setPrice(proPrice);
			pro.setMode(2);
			pro.setGifts(advanceList);
			float ratio = 0.1f;
			int length = advanceList.size();
			List ratios = new ArrayList(length);
			for ( int i=1; i<=length; i++ ) {
				ratios.add(baseRatio+ratio*i*2);
			}
			pro.setRatios(ratios);
			treasures.put(pro.getMode(), pro);
		}
		ArrayList list = new ArrayList();
		list.add(treasures);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

	/**
	 * Generate the reward propData list.
	 * 
	 * @param user
	 * @return
	 */
	private static ArrayList generatePropDataListForSimple(User user, int base) {
		int userLevel = user.getLevel();
		/**
		 * x0~x7之间出当前等级装备
		 * x8~x9出下一等级装备
		 */
		boolean needNextLevel = userLevel%10>=8;
		if ( needNextLevel ) {
			userLevel += 10;
		}
		
		PropData bestPropData = findBestWeaponInBag(user, EquipType.WEAPON);
		if ( bestPropData == null ) {
			WeaponPojo suit = EquipManager.getInstance().getRandomWeapon(user, EquipType.WEAPON, 1);
			bestPropData = suit.toPropData(100, WeaponColor.WHITE);
		}
		PropData bestSuitPropData = findBestWeaponInBag(user, EquipType.SUIT);
		if ( bestSuitPropData == null ) {
			WeaponPojo suit = EquipManager.getInstance().getRandomWeaponWithoutCheckReward(user.getGender(), userLevel, EquipType.SUIT, 1);
			bestSuitPropData = suit.toPropData(100, WeaponColor.WHITE);
		}
		String randomJeweryId = JEWERY_IDS_1[(int)(MathUtil.nextDouble()*JEWERY_IDS_1.length)];
		WeaponPojo jewery = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(randomJeweryId, userLevel);

		PropData firstReward = improveWeapon(user, bestPropData, base+2, 0, 0.1);
		PropData secondReward = improveWeapon(user, bestSuitPropData, base+2, 0, 0.1);
		//PropData thirdReward = improveWeapon(user, bestPropData, 1);
		PropData thirdReward = jewery.toPropData(30, WeaponColor.WHITE);
		
		ArrayList list = new ArrayList();
		list.add(RewardManager.getInstance().convertPropDataToReward(firstReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(secondReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(thirdReward));
		
		/**
		 * 暂时激活各种残片
		 */
		//PropData fourthReward = getRandomPropData(user, userLevel, base+2);
		PropData fourthReward = selectRandomItem(ITEM_IDS_3);
		PropData fivethReward = getRandomPropData(user, userLevel, base);
		PropData sixthReward = getRandomPropData(user, userLevel, base);
		list.add(RewardManager.getInstance().convertPropDataToReward(fourthReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(fivethReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(sixthReward));
		
		/**
		 * 固定提供幸运符25%
		 * 幸运符+25%: 24004
		 */
		//ItemPojo luckyStone = ItemManager.getInstance().getItemById("24004");
		//list.add(luckyStone.toPropData());
		/**
		 * 随机选择一种其他道具
		 */
		Object[] items1 = MathUtil.randomPick(itemList1, 2);
		addItemToList(items1, list);
		
		return list;
	}
	
	/**
	 * Generate the reward propData list.
	 * 
	 * @param user
	 * @return
	 */
	private static ArrayList generatePropDataListForNormal(User user, int base) {
		int userLevel = user.getLevel();
		/**
		 * x0~x7之间出当前等级装备
		 * x8~x9出下一等级装备
		 */
		boolean needNextLevel = userLevel%10>=8;
		if ( needNextLevel ) {
			userLevel += 10;
		}
		
		PropData bestPropData = findBestWeaponInBag(user, EquipType.WEAPON);
		if ( bestPropData == null ) {
			WeaponPojo suit = EquipManager.getInstance().getRandomWeapon(user, EquipType.WEAPON, 1);
			bestPropData = suit.toPropData(100, WeaponColor.WHITE);
		}
		PropData bestSuitPropData = findBestWeaponInBag(user, EquipType.SUIT);
		if ( bestSuitPropData == null ) {
			WeaponPojo suit = EquipManager.getInstance().getRandomWeaponWithoutCheckReward(user.getGender(), userLevel, EquipType.SUIT, 1);
			bestSuitPropData = suit.toPropData(100, WeaponColor.WHITE);
		}
		String randomJeweryId = JEWERY_IDS_1[(int)(MathUtil.nextDouble()*JEWERY_IDS_1.length)];
		WeaponPojo jewery = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(randomJeweryId, userLevel);

		PropData firstReward = improveWeapon(user, bestPropData, base+3, 3, 0.3);
		PropData secondReward = improveWeapon(user, bestSuitPropData, base+2, 3, 0.3);
		//PropData thirdReward = improveWeapon(user, bestPropData, 1);
		PropData thirdReward = jewery.toPropData(30, WeaponColor.WHITE);
		
		ArrayList list = new ArrayList();
		list.add(RewardManager.getInstance().convertPropDataToReward(firstReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(secondReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(thirdReward));
		
		/**
		 * 暂时激活各种残片
		 */
		//PropData fourthReward = getRandomPropData(user, userLevel, base+2);
		PropData fourthReward = selectRandomItem(ITEM_IDS_3);
		PropData fivethReward = getRandomPropData(user, userLevel, base);
		PropData sixthReward = getRandomPropData(user, userLevel, base);
		list.add(RewardManager.getInstance().convertPropDataToReward(fourthReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(fivethReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(sixthReward));
		
		/**
		 * 固定提供幸运符25%
		 * 幸运符+25%: 24004
		 */
		//ItemPojo luckyStone = ItemManager.getInstance().getItemById("24004");
		//list.add(luckyStone.toPropData());
		/**
		 * 随机选择一种其他道具
		 */
		Object[] items1 = MathUtil.randomPick(itemList1, 2);
		addItemToList(items1, list);
		
		return list;
	}
	
	/**
	 * Generate the reward propData list.
	 * 
	 * @param user
	 * @return
	 */
	private static ArrayList generatePropDataListForAdvance(User user, int base) {
		int userLevel = user.getLevel();
		/**
		 * x0~x7之间出当前等级装备
		 * x8~x9出下一等级装备
		 */
		boolean needNextLevel = userLevel%10>=8;
		if ( needNextLevel ) {
			userLevel += 10;
		}
		
		PropData bestPropData = findBestWeaponInBag(user, EquipType.WEAPON);
		if ( bestPropData == null ) {
			WeaponPojo suit = EquipManager.getInstance().getRandomWeapon(user, EquipType.WEAPON, 1);
			bestPropData = suit.toPropData(100, WeaponColor.WHITE);
		}
		PropData bestSuitPropData = findBestWeaponInBag(user, EquipType.SUIT);
		if ( bestSuitPropData == null ) {
			WeaponPojo suit = EquipManager.getInstance().getRandomWeaponWithoutCheckReward(user.getGender(), userLevel, EquipType.SUIT, 1);
			bestSuitPropData = suit.toPropData(100, WeaponColor.WHITE);
		}
		String randomJeweryId = JEWERY_IDS_2[(int)(MathUtil.nextDouble()*JEWERY_IDS_2.length)];
		WeaponPojo jewery = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(randomJeweryId, userLevel);

		PropData firstReward = improveWeapon(user, bestPropData, base+3, 5, 1.0);
		PropData secondReward = improveWeapon(user, bestPropData, base+4, 5, 1.0);
		PropData thirdReward = jewery.toPropData(30, WeaponColor.WHITE);
		String rareId = RARE_IDS[(int)(MathUtil.nextDouble()*RARE_IDS.length)];
		WeaponPojo rareWeapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(rareId, userLevel);
		PropData fourthReward = rareWeapon.toPropData(30, WeaponColor.WHITE);;
		
		ArrayList list = new ArrayList();
		list.add(RewardManager.getInstance().convertPropDataToReward(firstReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(secondReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(thirdReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(fourthReward));
		
		/**
		 * 暂时激活各种残片
		 */
		PropData fivethReward = getRandomPropData(user, userLevel, base);
		PropData sixthReward = getRandomPropData(user, userLevel, base);
		list.add(RewardManager.getInstance().convertPropDataToReward(fivethReward));
		list.add(RewardManager.getInstance().convertPropDataToReward(sixthReward));
		
		/**
		 * 固定提供幸运符25%
		 * 幸运符+25%: 24004
		 */
		//ItemPojo luckyStone = ItemManager.getInstance().getItemById("24004");
		//list.add(luckyStone.toPropData());
		/**
		 * 随机选择一种其他道具
		 */
		Object[] items1 = MathUtil.randomPick(itemList2, 2);
		addItemToList(items1, list);
		
		return list;
	}

	/**
	 * @param userLevel
	 * @param randomWeapon1
	 * @return
	 */
	private static PropData getRandomPropData(User user, int userLevel, int up) {
		WeaponPojo randomWeapon = null;
		while ( randomWeapon == null ) {
			EquipType equipType = equipTypes[(int)(MathUtil.nextDouble()*equipTypes.length)];
			randomWeapon = EquipManager.getInstance().getRandomWeapon(userLevel, equipType, 1);
		}
		PropData random = randomWeapon.toPropData(30, WeaponColor.WHITE);
		return improveWeapon(user, random, up, 0, 0.1);
	}
	
	public static final void addItemToList(Object[] items, ArrayList list) {
		for ( int i=0; i<items.length; i++ ) {
			Object obj = items[i];
			if ( obj != null ) {
				String id = obj.toString();
				ItemPojo item = ItemManager.getInstance().getItemById(id);
				if ( item != null ) {
					list.add(RewardManager.getInstance().convertPropDataToReward(item.toPropData()));
				} else {
					logger.warn("Not found itemId:{}", id);
				}
			}
		}
	}
	
	public static final PropData selectRandomItem(Object[] items) {
		Object obj = items[(int)(MathUtil.nextDouble()*items.length)];
		if ( obj != null ) {
			String id = obj.toString();
			ItemPojo item = ItemManager.getInstance().getItemById(id);
			if ( item != null ) {
				return item.toPropData();
			} else {
				logger.warn("Not found itemId:{}", id);
			}
		}
		return null;
	}

	/**
	 * 提升武器的品质
	 * @param referPropData
	 * @param up
	 * @return
	 */
	public static final PropData improveWeapon(User user, PropData referPropData, 
			int up, int maxStrength, double qualityRatio) {
		WeaponPojo referWeapon = (WeaponPojo)referPropData.getPojo();
		//等级提升
		int userLevel = user.getLevel()%10;
		int targetLevel = user.getLevel();
		if ( userLevel >= 5 && userLevel <= 7 ) {
			if ( MathUtil.nextDouble() < 0.5 ) {
				targetLevel += 5;
			}
		} else if ( userLevel > 7 ) {
			targetLevel += 3;
		}
		//品质提升
		int targetQuality = 1;
		/**
		 * 随机一个品质
		 */
		double r = MathUtil.nextDouble();
		if ( r < qualityRatio ) {
			targetQuality = 2;
		}
		//颜色提升
		WeaponColor targetColor = WeaponColor.WHITE;
		int referColorIndex = 0;
		if ( up > 1 ) {
			referColorIndex = referPropData.getWeaponColor().ordinal() + 1;
		}
		if ( up > 2 ) {
			referColorIndex = referPropData.getWeaponColor().ordinal() + 2;
		}
		if ( referColorIndex > WeaponColor.PINK.ordinal() ) {
			targetColor = WeaponColor.PINK;
		} else {
			targetColor = WeaponColor.values()[referColorIndex];
		}
		//强化提升
		int targetStrengthLevel = 0;
		/**
		 * 寻宝控制最高10级
		 */
		if ( up > 4 ) {
			if ( referPropData.getLevel() > 5 ) {
				targetStrengthLevel = referPropData.getLevel() + 1;
			} else {
				targetStrengthLevel = referPropData.getLevel() + 2; 
			}
		}
		if ( targetStrengthLevel>maxStrength ) {
			targetStrengthLevel = maxStrength;
		}
		//生成
		/*
		Reward reward = new Reward();
		reward.setId(targetWeapon.getId());
		reward.setType(RewardType.WEAPON);
		reward.setPropColor(targetColor.ordinal());
		reward.setPropIndate(30);
		reward.setPropCount(1);
		reward.setPropLevel(targetStrengthLevel);
		*/

		//生成武器
		WeaponPojo targetWeapon = targetWeapon = EquipManager.getInstance().
				getRandomWeaponWithoutCheckReward(user.getGender(), targetLevel, referWeapon.getSlot(), targetQuality);
		if ( targetWeapon == null ) {
			targetWeapon = targetWeapon = EquipManager.getInstance().
					getRandomWeaponWithoutCheckReward(user.getGender(), targetLevel, referWeapon.getSlot(), 1);
		}
		PropData targetPropData = targetWeapon.toPropData(30, targetColor);
		if ( targetStrengthLevel>0 ) {
			EquipCalculator.weaponUpLevel(targetPropData, targetStrengthLevel);
		}
		if ( targetPropData.getPower() < referPropData.getPower() ) {
			for ( int i=1; i<=5; i++ ) {
				PropDataSlot slot = new PropDataSlot();
				slot.setSlotType(PropDataEnhanceField.ATTACK);
				EquipCalculator.calculateForgeData(targetPropData, 2, ItemManager.attackStoneId, slot);
				if ( targetPropData.getPower() > referPropData.getPower() ) {
					break;
				} else {
					slot = new PropDataSlot();
					slot.setSlotType(PropDataEnhanceField.DEFEND);
					EquipCalculator.calculateForgeData(targetPropData, 2, ItemManager.defendStoneId, slot);
					if ( targetPropData.getPower() > referPropData.getPower() ) {
						break;
					} else {
						slot = new PropDataSlot();
						slot.setSlotType(PropDataEnhanceField.AGILITY);
						EquipCalculator.calculateForgeData(targetPropData, 2, ItemManager.agilityStoneId, slot);
						if ( targetPropData.getPower() > referPropData.getPower() ) {
							break;
						} else {
							slot = new PropDataSlot();
							slot.setSlotType(PropDataEnhanceField.LUCKY);
							EquipCalculator.calculateForgeData(targetPropData, 2, ItemManager.luckStoneId, slot);
							if ( targetPropData.getPower() > referPropData.getPower() ) {
								break;
							}
						}
					}
				}
			}
		}
		return targetPropData;
	}
		
	/**
	 * Find the best weapons in user's bag
	 * @return
	 */
	public static final PropData findBestWeaponInBag(User user, EquipType type) {
		int userLevel = user.getLevel();
		boolean needNextLevel = userLevel%10>=8;
		Bag bag = user.getBag();
		WeaponPojo bestWeapon = null;
		PropData bestPropData = null;
		Reward reward = new Reward();
		//比较身上的装备
		List wearList = bag.getWearPropDatas();
		for (Iterator iter = wearList.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if (propData == null) continue; 
			if ( propData.isWeapon() ) {
				WeaponPojo weapon = (WeaponPojo)propData.getPojo();
				if ( type == weapon.getSlot() ) {
					if ( bestWeapon == null ) {
						bestWeapon = weapon;
						reward.setPropColor(propData.getWeaponColor());
						reward.setPropLevel(propData.getLevel());
						reward.setPropIndate(propData.getPower());
						reward.setPropCount(weapon.getQuality());
					} else {
						if ( propData.getWeaponColor().ordinal() > reward.getPropColor().ordinal() ) {
							reward.setPropColor(propData.getWeaponColor());
						}
						if ( propData.getLevel() > reward.getLevel() ) {
							reward.setLevel(propData.getLevel());
						}
						if ( weapon.getQuality()>reward.getPropCount() ) {
						  //武器战斗力次要考虑
							reward.setPropCount(weapon.getQuality());
						}
					}
				}
			}
		}
		//再次比较背包
		List otherList = bag.getOtherPropDatas();
		for (Iterator iter = otherList.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if (propData == null) continue;
			if ( propData.isWeapon() ) {
				WeaponPojo weapon = (WeaponPojo)propData.getPojo();
				if ( type == weapon.getSlot() ) {
					if ( bestWeapon == null ) {
						bestWeapon = weapon;
						reward.setPropColor(propData.getWeaponColor());
						reward.setPropLevel(propData.getLevel());
						reward.setPropIndate(propData.getPower());
						reward.setPropCount(weapon.getQuality());
					} else {
						if ( propData.getWeaponColor().ordinal() > reward.getPropColor().ordinal() ) {
							reward.setPropColor(propData.getWeaponColor());
						}
						if ( propData.getLevel() > reward.getLevel() ) {
							reward.setLevel(propData.getLevel());
						}
						if ( weapon.getQuality()>reward.getPropCount() ) {
						  //武器战斗力次要考虑
							bestWeapon = weapon;
							reward.setPropCount(weapon.getQuality());
						}
					}
				}
			}
		}
		if ( bestWeapon != null ) {
			bestPropData = new PropData();
			bestPropData.setName(bestWeapon.getName());
			bestPropData.setItemId(bestWeapon.getId());
			bestPropData.setPojo(bestWeapon);
			bestPropData.setWeaponColor(reward.getPropColor());
			bestPropData.setLevel(reward.getPropLevel());
		}
		return bestPropData;
	}
}
