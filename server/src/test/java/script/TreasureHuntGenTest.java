package script;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;

public class TreasureHuntGenTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGenerateReward() {
		User user = new User();
		user.setLevelSimple(10);
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.TREASURE_HUNT_GEN, user);
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		List list = result.getResult();
		for ( Object obj : list ) {
			System.out.println(obj);
		}
	}

	@Test
	public void testFindBestWeapon() {
		User user = createRandomUser(20, 20, 20, 2, 5, 1);
		PropData bestPropData = TreasureHuntGen.findBestWeaponInBag(user, EquipType.WEAPON);
		PropData improved = TreasureHuntGen.improveWeapon(user, bestPropData, 4, 1, 1.0);
		List<PropData> wears = user.getBag().getWearPropDatas();
		for ( PropData pd : wears ) {
			System.out.println(pd);
		}
		List<PropData> others = user.getBag().getOtherPropDatas();
		for ( PropData pd : others ) {
			System.out.println(pd);
		}
		System.out.println("best: " + bestPropData);
		System.out.println("improved: " + improved);
	}

	
	private User createRandomUser(int minLevel, int maxLevel,
			int lastUserLevel, int levelDiff, int lastStrengthLevel, int strengthDiff) {
		String userName = "test-001";
		User user = new User();
		user.set_id(new UserId("test-001"));
		
		Random random = new Random();
		int userLevel = minLevel;
		if ( maxLevel > minLevel ) {
			if ( lastUserLevel == -1 || levelDiff == -1 ) {
				userLevel = minLevel + random.nextInt(maxLevel - minLevel);
			} else if ( levelDiff > 0 ){
				int base = lastUserLevel - levelDiff;
				if ( base < 0 ) base = 0;
				userLevel = base + random.nextInt(levelDiff*2);
			} else {
				userLevel = lastUserLevel;
			}
		}
		
		user.setLevelSimple(userLevel);
		
		PropDataEquipIndex[] equips = PropDataEquipIndex.values();
		for ( int i=0; i<equips.length; i++ ) {
			EquipType type = null;
			switch ( equips[i] ) {
				case BRACELET1:
					type = EquipType.DECORATION;
					break;
				case BRACELET2:
					type = EquipType.DECORATION;
					break;
				case BUBBLE:
					type = EquipType.BUBBLE;
					break;
				case CLOTH:
					type = EquipType.CLOTHES;
					break;
				case EYE:
					type = EquipType.EXPRESSION;
					break;
				case FACE:
					type = EquipType.FACE;
					break;
				case GLASS:
					type = EquipType.GLASSES;
					break;
				case HAIR:
					type = EquipType.HAIR;
					break;
				case HAT:
					type = EquipType.HAT;
					break;
				case NECKLACE:
					type = EquipType.DECORATION;
					break;
				case RING1:
					type = EquipType.JEWELRY;
					break;
				case RING2:
					type = EquipType.JEWELRY;
					break;
				case SUIT:
					type = EquipType.SUIT;
					break;
				case WEAPON:
					type = EquipType.WEAPON;
					break;
				case WEDRING:
					type = EquipType.JEWELRY;
					break;
				case WING:
					type = EquipType.WING;
					break;
			}
			Collection<WeaponPojo> slot = EquipManager.getInstance().getWeaponsBySlot(type);
			if ( slot != null ) {
				Object[] weaponObjs = MathUtil.randomPick(slot, 1);
				PropData propData = null;
				WeaponPojo weapon = (WeaponPojo)weaponObjs[0];
				weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), user.getLevel());
				propData = weapon.toPropData(10, WeaponColor.WHITE);
				user.getBag().addOtherPropDatas(propData);
				user.getBag().wearPropData(propData.getPew(), equips[i].index());
			}
		}

		Collection<WeaponPojo> slot = EquipManager.getInstance().getWeaponsBySlot(EquipType.WEAPON);
		Object[] weaponObjs = MathUtil.randomPick(slot, 1);
		WeaponPojo weapon = (WeaponPojo)weaponObjs[0];
		weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weapon.getTypeName(), user.getLevel());
		WeaponColor color = WeaponColor.values()[(int)(MathUtil.nextDouble()*WeaponColor.values().length)];
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		user.getBag().wearPropData(PropDataEquipIndex.WEAPON.index(), -1);
		user.getBag().addOtherPropDatas(propData);
		user.getBag().wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());		
		
		if ( true ) {
			/**
			 * 对于强化，应该先卸载武器，强化后再装备武器，否则基础数值会被计算2次
			 */
			int strengthMin = 0;
			int strengthMax = 12;
			if ( strengthMin >= strengthMax ) {
				strengthMin -= 1;
			}
			if ( strengthMin < 1 ) {
				strengthMin = 1;
			}
			int strengthMinMaxDiff = strengthMax - strengthMin;
			if ( strengthMinMaxDiff <= 0 ) {
				strengthMin = 0;
				strengthMinMaxDiff = 12;
			}
			//强化武器
			int strengthLevel = strengthMin+random.nextInt(strengthMinMaxDiff);
			if ( lastStrengthLevel != -1 && strengthDiff != -1 ) {
				int base = lastStrengthLevel - strengthDiff;
				if ( base < 0 ) {
					base = 0;
				}
				strengthLevel = base + random.nextInt(lastStrengthLevel + strengthDiff- base);
			}

			Bag bag = user.getBag();
			List<PropData> props = bag.getWearPropDatas();
			for ( PropDataEquipIndex index : PropDataEquipIndex.values() ) {
				PropData prop = props.get(index.index());
				if ( prop != null ) {
					//卸载武器
					bag.wearPropData(index.index(), -1);
					script.WeaponLevelUpgrade.func(new Object[]{prop, strengthLevel});
					//装备武器
					bag.wearPropData(prop.getPew(), index.index());
				}
			}
		}
		
		int stoneLevel = 1;
		double luckyCardRatio = 0.0;
		/*
		"1级合成",
		"1级合成+15%",
		"1级合成+25%",
		"2级合成",
		"2级合成+15%",
		"2级合成+25%",
		"3级合成",
		"3级合成+15%",
		"3级合成+25%",
		"4级合成",
		"4级合成+15%",
		"4级合成+25%",
		"5级合成",
		"5级合成+15%",
		"5级合成+25%",
		 */
		luckyCardRatio = 0.25;
		
		Bag bag = user.getBag();
		String stoneTypeId = null;
		List<PropData> props = bag.getWearPropDatas();
		for ( PropDataEquipIndex index : PropDataEquipIndex.values() ) {
			PropData prop = props.get(index.index());
			if ( prop != null ) {
				//卸载武器
				bag.wearPropData(index.index(), -1);
				PropDataSlot slot1 = new PropDataSlot();
				slot1.setSlotType(PropDataEnhanceField.ATTACK);
				stoneTypeId = ItemManager.attackStoneId;
				EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId, slot1);
				stoneTypeId = ItemManager.defendStoneId;
				slot1 = new PropDataSlot();
				slot1.setSlotType(PropDataEnhanceField.DEFEND);
				EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId, slot1);
				stoneTypeId = ItemManager.agilityStoneId;
				slot1 = new PropDataSlot();
				slot1.setSlotType(PropDataEnhanceField.AGILITY);
				EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId, slot1);
				stoneTypeId = ItemManager.luckStoneId;
				slot1 = new PropDataSlot();
				slot1.setSlotType(PropDataEnhanceField.LUCKY);
				EquipCalculator.calculateForgeData(prop, stoneLevel, stoneTypeId, slot1);
				//装备武器
				bag.wearPropData(prop.getPew(), index.index());
			}
		}
		return user;
	}
}
