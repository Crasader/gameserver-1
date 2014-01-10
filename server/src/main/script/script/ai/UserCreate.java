package script.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.UserLevelUpgrade;
import script.WeaponLevelUpgrade;

import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * Create an AI user according to a real user's properties.
 * 
 * 
 * @author wangqi
 *
 */
public class UserCreate {

	private static final Logger logger = LoggerFactory.getLogger(UserCreate.class);
	static double q = 3.0;
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		
		User realUser = (User)parameters[0];
		
		User aiUser = new User();
		aiUser.setUsername(AIManager.getRandomAIName());
		aiUser.setRoleName(aiUser.getUsername());
		aiUser.set_id(new UserId(aiUser.getUsername(), MathUtil.nextFakeInt(Integer.MAX_VALUE)));
		aiUser.setWinOdds(50+(int)(MathUtil.nextDouble()*50));
		aiUser.setAI(true);
		int genderIndex = (int)Math.round(MathUtil.nextDouble());
		Gender gender = Gender.values()[Gender.FEMALE.ordinal()+genderIndex];
		aiUser.setGender(gender);
		
		//Copy user's level and exp;
		int realUserLevel = realUser.getLevel();
		if ( realUserLevel < 60 ) {
			if ( MathUtil.nextDouble()<=0.4 ) {
				aiUser.setIsvip(true);
				aiUser.setViplevel((int)(MathUtil.nextDouble()*7));
			}
		} else {
			int vipLevel = MathUtil.nextGaussionInt(4, 7, q);
			aiUser.setIsvip(true);
			aiUser.setViplevel(vipLevel);
		}
		int userLevel = realUser.getLevel();
		if ( userLevel > 3 ) {
			int levelChange = (int)(MathUtil.nextDouble()*4);
			if ( MathUtil.nextDouble()<0.8 ) {
				levelChange *= -1;
			}
			userLevel += levelChange;
		} else {
			//针对小于等于3级的用户，只创建1级AI
			userLevel = 1;
		}
		if ( userLevel <= 1 ) userLevel = 1;
		if ( userLevel > LevelManager.MAX_LEVEL ) userLevel = LevelManager.MAX_LEVEL;
		aiUser.setLevelSimple(userLevel);
		UserLevelUpgrade.func(new Object[]{aiUser});

		//Copy user's wealth
		double percent = 1.2 - MathUtil.nextGaussionDouble(0.0, 3.0);
		aiUser.setGoldenSimple( (int)(realUser.getGolden() * percent) );
		aiUser.setYuanbaoFreeSimple( (int)(realUser.getYuanbaoFree() * percent) ); 
		//aiUser.setMedal( (int)(realUser.getMedal() * percent) ); 
		//aiUser.setVoucher( (int)(realUser.getVoucher() * percent) ); 

		//Copy the statistic
		//aiUser.setWins( (int)(realUser.getWins() * percent) );
		//aiUser.setFailcount( (int)(realUser.getFailcount() * percent) );
		//aiUser.setBattleCount(aiUser.getWins() + aiUser.getFailcount() );
		//if ( aiUser.getBattleCount() > 0 ) {
		//}
		aiUser.setWinOdds( (int)(MathUtil.nextDouble() * 50) + 20 );
		
		//Copy user's bag
		Bag realBag = realUser.getBag();
		EquipManager manager = EquipManager.getInstance();
		List propDatas = realBag.getWearPropDatas();
		List otherPropDatas = realBag.getOtherPropDatas();
		
		/**
		 * 强化部分的设定
		 */
		int maxWeaponLevel = 0;
		int maxSuitLevel = 0;
		int maxClothLevel = 0;
		int maxOtherLevel = 0;
		int maxColor = 0;
		int maxQulity = 0;
		int maxStoneLevel = 0;
		int minWeaponLevel = 0;
		int minSuitLevel = 0;
		int minClothLevel = 0;
		int minOtherLevel = 0;
		int minColor = 0;
		int minQulity = 0;
		int minStoneLevel = 0;
		boolean wearHair, wearHat, wearCloth, wearSuit, wearNeck, wearGlass,
			wearGrace1, wearGrace2, wearRing1, wearRing2;
		wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
				wearGrace1 = wearGrace2 = wearRing1 = wearRing2 = false;
		if ( realUserLevel < 10 ) {
			maxWeaponLevel = 0;
			maxSuitLevel = 0;
			maxClothLevel = 0;
			maxOtherLevel = 0;
			maxColor = 0;
			wearHair = wearHat = true;
		} else if ( realUserLevel < 22 ) {
			maxWeaponLevel = 2;
			maxSuitLevel = 1;
			maxClothLevel = 3;
			maxOtherLevel = 0;
			maxColor = 1;
			maxStoneLevel = 1;
			wearHair = wearHat = wearCloth = wearGlass = true;
		} else if ( realUserLevel < 32 ) {
			maxWeaponLevel = 4;
			maxSuitLevel = 1;
			maxClothLevel = 3;
			maxOtherLevel = 0;
			maxColor = 2;
			maxStoneLevel = 3;
			wearHair = wearHat = wearCloth = wearNeck = wearGlass =
					wearRing1 = true;
		} else if ( realUserLevel < 42 ) {
			maxWeaponLevel = 5;
			maxSuitLevel = 2;
			maxClothLevel = 3;
			maxOtherLevel = 0;
			maxColor = 3;
			maxQulity = 1;
			maxStoneLevel = 3;
			minStoneLevel = 1;
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearRing1 = true;
		} else if ( realUserLevel < 52 ) {
			maxWeaponLevel = 6;
			maxSuitLevel = 3;
			maxClothLevel = 5;
			maxOtherLevel = 3;
			maxColor = 4;
			maxQulity = 1;
			maxStoneLevel = 4;
			minStoneLevel = 2;
			minWeaponLevel = 3;
			minSuitLevel = 2;
			minClothLevel = 2;
			minOtherLevel = 1;
			minColor = 2;
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearRing1 = true;
		} else if ( realUserLevel < 62 ) {
			maxWeaponLevel = 7;
			maxSuitLevel = 3;
			maxClothLevel = 5;
			maxOtherLevel = 5;
			maxColor = 4;
			maxQulity = 1;
			maxStoneLevel = 4;
			minStoneLevel = 2;
			minWeaponLevel = 5;
			minSuitLevel = 2;
			minClothLevel = 2;
			minOtherLevel = 1;
			minColor = 2;
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearRing1 = true;
		} else if ( realUserLevel < 72 ) {
			maxWeaponLevel = 6;
			maxSuitLevel = 6;
			maxClothLevel = 6;
			maxOtherLevel = 6;
			maxColor = 4;
			maxQulity = 1;
			maxStoneLevel = 4;
			minStoneLevel = 3;
			minWeaponLevel = 4;
			minSuitLevel = 4;
			minClothLevel = 4;
			minOtherLevel = 4;
			minColor = 2;
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearRing1 = true;
		} else if ( realUserLevel < 75 ) {
			maxWeaponLevel = 8;
			maxSuitLevel = 8;
			maxClothLevel = 8;
			maxOtherLevel = 8;
			maxColor = 5;
			maxQulity = 1;
			maxStoneLevel = 4;
			minStoneLevel = 3;
			minWeaponLevel = 4;
			minSuitLevel = 4;
			minClothLevel = 4;
			minOtherLevel = 4;
			minColor = 2;
			minQulity = 1; 
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearGrace1 = wearRing1 = true;
		} else if ( realUserLevel < 81 ) {
			maxWeaponLevel = 9;
			maxSuitLevel = 9;
			maxClothLevel = 9;
			maxOtherLevel = 9;
			maxColor = 5;
			maxStoneLevel = 5;
			minStoneLevel = 3;
			minWeaponLevel = 6;
			minSuitLevel = 6;
			minClothLevel = 6;
			minOtherLevel = 6;
			minColor = 2;
			minQulity = maxQulity = 1;
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearGrace1 = wearRing1 = true;
		} else if ( realUserLevel < 90 ) {
			maxWeaponLevel = 10;
			maxSuitLevel = 10;
			maxClothLevel = 10;
			maxOtherLevel = 10;
			maxColor = 5;
			maxStoneLevel = 5;
			minStoneLevel = 3;
			minWeaponLevel = 7;
			minSuitLevel = 7;
			minClothLevel = 7;
			minOtherLevel = 7;
			minColor = 2;
			minQulity = maxQulity = 1;
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearGrace1 = wearGrace2 = wearRing1 = wearRing2 = true;
		} else if ( realUserLevel <= LevelManager.MAX_LEVEL ) {
			maxWeaponLevel = 12;
			maxSuitLevel = 12;
			maxClothLevel = 12;
			maxOtherLevel = 12;
			maxColor = 5;
			minWeaponLevel = 8;
			minSuitLevel = 8;
			minClothLevel = 8;
			minOtherLevel = 8;
			minColor = 2;
			minQulity = maxQulity = 1;
			wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
					wearGrace1 = wearGrace2 = wearRing1 = wearRing2 = true;
		}
		Bag bag = aiUser.getBag();
		//WearWeapon
		int wlevel = MathUtil.nextGaussionInt(minWeaponLevel, maxWeaponLevel+1, q);
		PropData weaponPropData = generatePropData(gender, EquipType.WEAPON, realUserLevel, 
				minColor, minQulity, maxColor, maxQulity, wlevel, minStoneLevel, maxStoneLevel);
		if ( weaponPropData != null )
			wearPropData(bag, weaponPropData, PropDataEquipIndex.WEAPON);
		
		if ( wearHair ) {
			int olevel = MathUtil.nextGaussionInt(minOtherLevel, maxOtherLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.HAIR, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, olevel, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.HAIR);
		}
		if ( wearHat ) {
			int olevel = MathUtil.nextGaussionInt(minOtherLevel, maxOtherLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.HAT, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, olevel, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.HAT);
		}
		if ( wearCloth ) {
			int clevel = MathUtil.nextGaussionInt(minClothLevel, maxClothLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.CLOTHES, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, clevel, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.CLOTH);
		}
		if ( wearSuit ) {
			int level = MathUtil.nextGaussionInt(minSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.SUIT, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.SUIT);
		}
		if ( wearNeck ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.NECKLACE, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.NECKLACE);
		}
		if ( wearGrace1 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.BRACELET, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.BRACELET1);
		}
		if ( wearGrace2 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.BRACELET, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.BRACELET2);
		}
		if ( wearRing1 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.RING, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.RING1);
		}
		if ( wearRing2 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(gender, EquipType.RING, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.RING2);
		}
		if ( wearGlass ) {
			int level = MathUtil.nextGaussionInt(minOtherLevel, maxOtherLevel, q);
			PropData propData = generatePropData(gender, EquipType.GLASSES, realUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			if ( weaponPropData != null )
				wearPropData(bag, propData, PropDataEquipIndex.GLASS);
		}

		if ( realUser.getLevel()<=3 ) {
			aiUser.setAttack(1);
			aiUser.setDefend(0);
		}
		aiUser.addTool(BuffToolType.Recover);
		aiUser.addTool(BuffToolType.Recover);
		aiUser.addTool(BuffToolType.Energy);
		aiUser.setConfigHideSuite(true);		

		ArrayList list = new ArrayList();
		list.add(aiUser);

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

	/**
	 * @param bag
	 * @param propData
	 */
	private static void wearPropData(Bag bag, PropData propData, PropDataEquipIndex index) {
		if ( propData != null ) {
			bag.addOtherPropDatas(propData);
			bag.wearPropData(propData.getPew(), index.index());
		}
	}

	/**
	 * @param realUserLevel
	 * @param maxColor
	 * @param maxQulity
	 * @param wlevel
	 */
	private static PropData generatePropData(Gender gender, EquipType equipType, int realUserLevel, int minColor, int minQuality, 
			int maxColor, int maxQulity, int strLevel, int minStoneLevel, int maxStoneLevel) {
		//quality: 1,2
		int quality = MathUtil.nextGaussionInt(minQuality, maxQulity+1, q);
		if ( quality < 0 ) {
			quality = 0;
		} else if ( quality > 1 ) {
			quality = 1;
		}
		int colorIndex = MathUtil.nextGaussionInt(minColor, maxColor+1, q);
		if ( colorIndex < 0 ) {
			colorIndex = 0;
		} else if ( colorIndex >= WeaponColor.values().length ) {
			colorIndex = WeaponColor.values().length-1;
		}
		WeaponColor color = WeaponColor.values()[colorIndex];
		WeaponPojo weapon = null;
		int tryTimes = 0;
		while ( weapon == null && tryTimes++ < 20 ) {
			weapon = EquipManager.getInstance().getRandomWeaponWithoutCheckReward(gender, realUserLevel, equipType, quality+1);
		}
		if ( weapon == null ) {
			quality = 1;
		}
		tryTimes = 0;
		while ( weapon == null && tryTimes++ < 20 ) {
			weapon = EquipManager.getInstance().getRandomWeaponWithoutCheckReward(gender, realUserLevel, equipType, quality+1);
		}
		if ( weapon == null ) {
			logger.info("equipType: {}, quality:{}, realUserLevel:{}", new Object[]{
					equipType, quality+1, realUserLevel
			});
		} else {
			PropData propData = weapon.toPropData(30, color);
			if ( strLevel > 0 ) {
				propData = EquipCalculator.weaponUpLevel(propData, strLevel);
			}
			if ( maxStoneLevel > 0 ) {
				int stoneLevel = MathUtil.nextGaussionInt(minStoneLevel+1, maxStoneLevel+1, q);
				if ( stoneLevel < 0 ) {
					stoneLevel = 0;
				} else if ( stoneLevel > maxStoneLevel ) {
					stoneLevel = maxStoneLevel;
				}
				if ( stoneLevel > 0 ) {
					PropDataSlot slot = null;
					if ( equipType == EquipType.WEAPON ) {
						slot = new PropDataSlot();
						slot.setSlotType(PropDataEnhanceField.ATTACK);
						EquipCalculator.calculateForgeData(propData, stoneLevel, ItemManager.attackStoneId, slot);
					}
					slot = new PropDataSlot();
					slot.setSlotType(PropDataEnhanceField.DEFEND);
					EquipCalculator.calculateForgeData(propData, stoneLevel, ItemManager.defendStoneId, slot);
					slot = new PropDataSlot();
					slot.setSlotType(PropDataEnhanceField.AGILITY);
					EquipCalculator.calculateForgeData(propData, stoneLevel, ItemManager.agilityStoneId, slot);
					slot = new PropDataSlot();
					slot.setSlotType(PropDataEnhanceField.LUCKY);
					EquipCalculator.calculateForgeData(propData, stoneLevel, ItemManager.luckStoneId, slot);
				}
			}
			return propData;
		}
		return null;
	}
}
