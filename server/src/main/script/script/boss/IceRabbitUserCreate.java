package script.boss;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.UserLevelUpgrade;

import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
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
public class IceRabbitUserCreate {

	private static final Logger logger = LoggerFactory.getLogger(IceRabbitUserCreate.class);
	static double q = 3.0;

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}

		BossPojo bossPojo = (BossPojo)parameters[0];
		Boss boss = (Boss)parameters[1];
		User user = (User)parameters[2];

		int bossMode = bossPojo.getLevel();
		int bossLevel = boss.getProgress();
		float diffcultRatio = (float)Math.pow(1.1, bossLevel+1);

		User aiUser = new User();

		//Copy user's level and exp;
		int realUserLevel = user.getLevel();
		int bossUserLevel = realUserLevel;

		switch ( bossMode ) {
			case 0:
				bossUserLevel -= 5;
				break;
			case 1:
				break;
			case 2:
				bossUserLevel = (realUserLevel / 10 + 1)*10;
				if ( bossUserLevel - realUserLevel <= 5 ) {
					bossUserLevel += 5;
				}
				if ( bossUserLevel > LevelManager.MAX_LEVEL ) {
					bossUserLevel = LevelManager.MAX_LEVEL;
				}
				break;
		}
		aiUser.setLevelSimple(bossUserLevel);
		UserLevelUpgrade.func(new Object[]{aiUser});

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
		switch ( bossMode ) {
			case 0:
				maxWeaponLevel = 5;
				maxSuitLevel = 5;
				maxClothLevel = 5;
				maxOtherLevel = 5;
				maxColor = 3;
				minWeaponLevel = 5;
				minSuitLevel = 5;
				minClothLevel = 5;
				minOtherLevel = 5;
				minColor = 1;
				minQulity = maxQulity = 1;
				wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
						wearGrace1 = wearGrace2 = wearRing1 = wearRing2 = true;
				break;
			case 1:
				maxWeaponLevel = 8;
				maxSuitLevel = 8;
				maxClothLevel = 8;
				maxOtherLevel = 8;
				maxColor = 4;
				minWeaponLevel = 8;
				minSuitLevel = 8;
				minClothLevel = 8;
				minOtherLevel = 8;
				minColor = 3;
				minQulity =1; 
				maxQulity = 2;
				wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
						wearGrace1 = wearGrace2 = wearRing1 = wearRing2 = true;
				break;
			case 2:
				maxWeaponLevel = 10;
				maxSuitLevel = 10;
				maxClothLevel = 10;
				maxOtherLevel = 10;
				maxColor = 5;
				minWeaponLevel = 10;
				minSuitLevel = 10;
				minClothLevel = 10;
				minOtherLevel = 10;
				minColor = 4;
				minQulity = maxQulity = 2;
				wearHair = wearHat = wearCloth = wearSuit = wearNeck = wearGlass =
						wearGrace1 = wearGrace2 = wearRing1 = wearRing2 = true;
				break;
		}
		Bag bag = aiUser.getBag();
		//WearWeapon
		PropData weaponPropData = null;
		PropData suitPropData = null; 
		{
			String weaponId = bossPojo.getWeaponPropId();
			int wlevel = MathUtil.nextGaussionInt(minWeaponLevel, maxWeaponLevel+1, q);
			weaponPropData = generatePropDataByType(weaponId, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, wlevel, minStoneLevel, maxStoneLevel);
			wearPropData(bag, weaponPropData, PropDataEquipIndex.WEAPON);
		}
		{
			String suitId = bossPojo.getSuitPropId();
			int suitLevel = MathUtil.nextGaussionInt(minSuitLevel, maxSuitLevel+1, q);
			suitPropData = generatePropDataById(suitId, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, suitLevel, minStoneLevel, maxStoneLevel);
			wearPropData(bag, suitPropData, PropDataEquipIndex.SUIT);
		}
		if ( wearHair ) {
			int olevel = MathUtil.nextGaussionInt(minOtherLevel, maxOtherLevel+1, q);
			PropData propData = generatePropData(EquipType.HAIR, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, olevel, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.HAIR);
		}
		if ( wearHat ) {
			int olevel = MathUtil.nextGaussionInt(minOtherLevel, maxOtherLevel+1, q);
			PropData propData = generatePropData(EquipType.HAT, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, olevel, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.HAT);
		}
		if ( wearCloth ) {
			int clevel = MathUtil.nextGaussionInt(minClothLevel, maxClothLevel+1, q);
			PropData propData = generatePropData(EquipType.CLOTHES, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, clevel, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.CLOTH);
		}
		if ( wearNeck ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(EquipType.NECKLACE, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.NECKLACE);
		}
		if ( wearGrace1 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(EquipType.BRACELET, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.BRACELET1);
		}
		if ( wearGrace2 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(EquipType.BRACELET, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.BRACELET2);
		}
		if ( wearRing1 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(EquipType.RING, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.RING1);
		}
		if ( wearRing2 ) {
			int level = MathUtil.nextGaussionInt(maxSuitLevel, maxSuitLevel+1, q);
			PropData propData = generatePropData(EquipType.RING, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.RING2);
		}
		if ( wearGlass ) {
			int level = MathUtil.nextGaussionInt(minOtherLevel, maxOtherLevel, q);
			PropData propData = generatePropData(EquipType.GLASSES, bossUserLevel, 
					minColor, minQulity, maxColor, maxQulity, level, minStoneLevel, maxStoneLevel);
			wearPropData(bag, propData, PropDataEquipIndex.GLASS);
		}

		if ( diffcultRatio > 1.0 ) {
			aiUser.setAttack(Math.round(diffcultRatio * aiUser.getAttack()));
			aiUser.setDefend(Math.round(diffcultRatio * aiUser.getDefend()));
			aiUser.setLuck(Math.round(diffcultRatio * aiUser.getLuck()));
			aiUser.setAgility(Math.round(diffcultRatio * aiUser.getAgility()));
		}
		if ( aiUser.getAgility() < user.getAgility() ) {
			aiUser.setAgility( user.getAgility() + 1);
		}
		
//		aiUser.setUsername(bossPojo.getName());
//		aiUser.setRoleName(bossPojo.getName());
//		aiUser.set_id(new UserId(aiUser.getUsername(), MathUtil.nextFakeInt(Integer.MAX_VALUE)));
//		aiUser.setAI(true);
//		aiUser.setBoss(true);
//		aiUser.putUserData(BossManager.USER_ROLE_ATTACK, bossPojo.getRoleAttackScript());
//		aiUser.putUserData(BossManager.USER_ROLE_DEAD, bossPojo.getRoleDeadScript());
//		aiUser.putUserData(BossManager.USER_BOSS_POJO, bossPojo);
//		aiUser.putUserData(BossManager.USER_BOSS, boss);

		User bossUser = new User();
		bossUser.setUsername(bossPojo.getName());
		bossUser.setRoleName(bossPojo.getName());
		bossUser.set_id(new UserId(aiUser.getUsername(), MathUtil.nextFakeInt(Integer.MAX_VALUE)));
		bossUser.setAI(true);
		bossUser.setBoss(true);
		bossUser.putUserData(BossManager.USER_ROLE_ATTACK, bossPojo.getRoleAttackScript());
		bossUser.putUserData(BossManager.USER_ROLE_DEAD, bossPojo.getRoleDeadScript());
		bossUser.putUserData(BossManager.USER_BOSS_POJO, bossPojo);
		bossUser.putUserData(BossManager.USER_BOSS, boss);
		bossUser.getBag().addOtherPropDatas(weaponPropData);
		bossUser.getBag().addOtherPropDatas(suitPropData);
		bossUser.getBag().wearPropData(weaponPropData.getPew(), PropDataEquipIndex.WEAPON.index());
		bossUser.getBag().wearPropData(suitPropData.getPew(), PropDataEquipIndex.SUIT.index());
		bossUser.setBlood(aiUser.getBlood());
		bossUser.setAttack(aiUser.getAttack());
		bossUser.setDefend(aiUser.getDefend());
		bossUser.setLuck(aiUser.getLuck());
		bossUser.setAgility(aiUser.getAgility());
		bossUser.setTkew(aiUser.getTkew());
		bossUser.setPowerSimple(aiUser.getPower());
		bossUser.setConfigHideSuite(false);

		User[] bossUsers = new User[1];
		bossUsers[0] = bossUser;
		ArrayList list = new ArrayList();
		list.add(bossUsers);

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
	private static PropData generatePropData(EquipType equipType, int bossUserLevel, int minColor, int minQuality, 
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
			weapon = EquipManager.getInstance().getRandomWeaponWithoutCheckReward(Gender.MALE, bossUserLevel, equipType, quality+1);
		}
		if ( weapon == null ) {
			quality = 1;
		}
		tryTimes = 0;
		while ( weapon == null && tryTimes++ < 20 ) {
			weapon = EquipManager.getInstance().getRandomWeaponWithoutCheckReward(Gender.MALE, bossUserLevel, equipType, quality+1);
		}
		if ( weapon == null ) {
			logger.info("equipType: {}, quality:{}, realUserLevel:{}", new Object[]{
					equipType, quality+1, bossUserLevel
			});
		}
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
				PropDataSlot slot = new PropDataSlot();
				slot.setSlotType(PropDataEnhanceField.ATTACK);
				if ( equipType == EquipType.WEAPON ) {
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

	private static PropData generatePropDataByType(String equipId, int bossUserLevel, int minColor, int minQuality, 
			int maxColor, int maxQulity, int strLevel, int minStoneLevel, int maxStoneLevel) {
		int colorIndex = MathUtil.nextGaussionInt(minColor, maxColor+1, q);
		if ( colorIndex < 0 ) {
			colorIndex = 0;
		} else if ( colorIndex >= WeaponColor.values().length ) {
			colorIndex = WeaponColor.values().length-1;
		}
		WeaponColor color = WeaponColor.values()[colorIndex];
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipId);
		weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), bossUserLevel);
		
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
				PropDataSlot slot = new PropDataSlot();
				slot.setSlotType(PropDataEnhanceField.ATTACK);
				if ( weapon.getSlot() == EquipType.WEAPON ) {
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

	private static PropData generatePropDataById(String equipId, int bossUserLevel, int minColor, int minQuality, 
			int maxColor, int maxQulity, int strLevel, int minStoneLevel, int maxStoneLevel) {
		int colorIndex = MathUtil.nextGaussionInt(minColor, maxColor+1, q);
		if ( colorIndex < 0 ) {
			colorIndex = 0;
		} else if ( colorIndex >= WeaponColor.values().length ) {
			colorIndex = WeaponColor.values().length-1;
		}
		WeaponColor color = WeaponColor.values()[colorIndex];
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipId);
		
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
				PropDataSlot slot = new PropDataSlot();
				slot.setSlotType(PropDataEnhanceField.ATTACK);
				if ( weapon.getSlot() == EquipType.WEAPON ) {
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
}
