package com.xinqihd.sns.gameserver.entity.user;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.proto.XinqiPropData;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;

public class PropDataTest extends AbstractTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty("zookeeper.root", "/snsgame/babywar");
		//ZooKeeperFactory.getInstance(connectString);
		GameContext gameContext = GameContext.getTestInstance();
		gameContext.reloadContext();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetPojo() {
		//510	黑铁●小蝎子
		PropData propData = new PropData();
		propData.setItemId("510");
		propData.setName("夺命刀");
		propData.setBanded(true);
		propData.setValuetype(PropDataValueType.BONUS);
		propData.setAgilityLev(1000);
		propData.setAttackLev(1001);
		propData.setDefendLev(1002);
		propData.setDuration(1003);
		propData.setLuckLev(1004);
		propData.setSign(1005);
		Pojo pojo = propData.getPojo();
		assertTrue("Get a WeaponPojo", pojo instanceof WeaponPojo);
		WeaponPojo wpojo = (WeaponPojo)pojo;
		assertEquals("黑铁●小蝎子", wpojo.getName());
	}

	@Test
	public void testEnhanceMapSaveAndQuery() {
		UserManager manager = UserManager.getInstance();
		String username = "test-001";
		manager.removeUser(username);

		User user = manager.createDefaultUser();
		user.setUsername(username);
		user.set_id(new UserId(username));
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		//UserManager.getInstance().saveUserBag(user, false);
		
		Bag bag = user.getBag();
		PropData propData = EquipManager.getInstance().getWeaponById(
				UserManager.basicWeaponItemId).toPropData(10, WeaponColor.WHITE);
//		HashMap<PropDataEnhanceType, HashMap<PropDataEnhanceType.Field, Integer>> map = 
//				propData.getEnhanceMap();
		propData.setEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE, 30);
		propData.setEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.SKIN, 15);
		bag.addOtherPropDatas(propData);
		bag.wearPropData(20, 17);

		manager.saveUser(user, true);
		manager.saveUserBag(user, true);

		Bag actualBag = manager.queryUserBag(user);
		PropData actualPropData = actualBag.getWearPropDatas().get(17);
		assertEquals(propData.getEnhanceMap().size(), actualPropData.getEnhanceMap().size());
		HashMap<PropDataEnhanceField, Integer> expectMap = propData.getEnhanceMap().get(PropDataEnhanceType.STRENGTH);
		HashMap<PropDataEnhanceField, Integer> actualMap = actualPropData.getEnhanceMap().get(PropDataEnhanceType.STRENGTH);
		assertEquals(expectMap.get(PropDataEnhanceField.DAMAGE), 
				actualMap.get(PropDataEnhanceField.DAMAGE));
		assertEquals(expectMap.get(PropDataEnhanceField.SKIN), 
				actualMap.get(PropDataEnhanceField.SKIN));
	}
	
	@Test
	public void testEnhanceMapWithNull() {
		PropData propData = EquipManager.getInstance().getWeaponById(
				UserManager.basicWeaponItemId).toPropData(10, WeaponColor.WHITE);
		int value = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
		assertEquals(0, value);
	}
	
	@Test
	public void testEnhanceMapStrengthToLevel5() {
		PropData propData = EquipManager.getInstance().getWeaponById(
				UserManager.basicWeaponItemId).toPropData(10, WeaponColor.WHITE);
		
	  //Go to level 4
		EquipCalculator.weaponUpLevel(propData, 4);
		int oldDamage = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
		int oldSkin = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		System.out.println("oldDamage:"+oldDamage+", skin:"+oldSkin);
		
		//Go to level 12
		EquipCalculator.weaponUpLevel(propData, 12);
		int damage = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
		int skin = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(22, damage);
		assertEquals(0, skin);
		
		//Go back to level 4
		EquipCalculator.weaponUpLevel(propData, 4);
		damage = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
		skin = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(oldDamage, damage);
		assertEquals(0, skin);
	}
	
	@Test
	public void testEnhanceMapStrengthRingToLevel5() {
		//816	???????	Ring0004	??????,??????	2
		PropData propData = EquipManager.getInstance().getWeaponById(
				"816").toPropData(10, WeaponColor.WHITE);
		EquipCalculator.weaponUpLevel(propData, 4);
		int oldDamage = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
		int oldSkin = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		
		//Go to level 12
		EquipCalculator.weaponUpLevel(propData, 12);
		int damage = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
		int skin = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(0, damage);
		assertTrue(skin>oldSkin+8);
		
		//Go back to level 4
		EquipCalculator.weaponUpLevel(propData, 4);
		damage = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE);
		skin = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN);
		assertEquals(oldDamage, damage);
		assertEquals(oldSkin, skin);
	}
	
	@Test
	public void testEnhanceMapValueAfterStrengthAndForge() {
		String weaponId = UserManager.basicWeaponItemId.substring(0, 2).concat("9");
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		
		//Strength to level 2
		EquipCalculator.weaponUpLevel(propData, 2);
		//Forge with four types stone
		/*
				{ GameDataKey.CRAFT_STONE_LUCK, 20001},
				{ GameDataKey.CRAFT_STONE_DEFEND, 20002},
				{ GameDataKey.CRAFT_STONE_AGILITY, 20003},
				{ GameDataKey.CRAFT_STONE_ATTACK, 20004},
		 */
		String[] stones = {"20001", "20002", "20003", "20004"};
		for ( String stoneType : stones ) {
			PropDataSlot slot = new PropDataSlot();
			slot.setSlotType(PropDataEnhanceField.ATTACK);
			EquipCalculator.calculateForgeData(propData, 5, stoneType, slot);
		}
		
		com.xinqihd.sns.gameserver.proto.XinqiPropData.PropData xinqiPropData = 
				propData.toXinqiPropData();
		int level = xinqiPropData.getLevel();
		int attack = xinqiPropData.getAttackLev();
		int defend = xinqiPropData.getDefendLev();
		int agility = xinqiPropData.getAgilityLev();
		int lucky = xinqiPropData.getLuckLev();
		int damage = xinqiPropData.getDamageLev();
		int skin = xinqiPropData.getSkinLev();
		
		assertEquals(2, level);
//		System.out.println("base attack:" + weapon.getAddAttack() + " +"+attack+"="+propData.getAttackLev());
//		System.out.println("base defend:" + weapon.getAddDefend() + " +"+defend+"="+propData.getDefendLev());
//		System.out.println("base agility:" + weapon.getAddAgility() + " +"+agility+"="+propData.getAgilityLev());
//		System.out.println("base lucky:" + weapon.getAddLuck() + " +"+lucky+"="+propData.getLuckLev());
//		System.out.println("base damage:" + weapon.getAddDamage() + " +"+damage+"="+propData.getDamageLev());
//		System.out.println("base skin:" + weapon.getAddSkin() + " +"+skin+"="+propData.getSkinLev());
		
		assertTrue("base attack:" + weapon.getAddAttack() + " +"+attack+"="+propData.getAttackLev(),
				propData.getAttackLev()>weapon.getAddAttack());
		assertTrue("base defend:" + weapon.getAddDefend() + " +"+defend+"="+propData.getDefendLev(),
				propData.getDefendLev()>weapon.getAddDefend());
		assertTrue("base agility:" + weapon.getAddAgility() + " +"+agility+"="+propData.getAgilityLev(),
				propData.getAgilityLev()>weapon.getAddAgility());
		assertTrue("base lucky:" + weapon.getAddLuck() + " +"+lucky+"="+propData.getLuckLev(),
				propData.getLuckLev()>weapon.getAddLuck());
		assertTrue("base damage:" + weapon.getAddDamage() + " +"+damage+"="+propData.getDamageLev(),
				propData.getDamageLev()>weapon.getAddDamage());
		assertTrue("base skin:" + weapon.getAddSkin() + " +"+skin+"="+propData.getSkinLev(),
				propData.getSkinLev() == weapon.getAddSkin());

	}
	
	@Test
	public void testEnhanceMapValueForgeDataNotGrow() {
		String weaponId = UserManager.basicWeaponItemId.substring(0, 2).concat("9");
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		
		//Forge with four types stone
		/*
				{ GameDataKey.CRAFT_STONE_LUCK, 20001},
				{ GameDataKey.CRAFT_STONE_DEFEND, 20002},
				{ GameDataKey.CRAFT_STONE_AGILITY, 20003},
				{ GameDataKey.CRAFT_STONE_ATTACK, 20004},
		 */
		String[] stones = {"20001", "20002", "20003", "20004"};
		StringBuilder buf = new StringBuilder();
		buf.append("base attack: ").append(propData.getBaseAttack()).append("\t").
			append("attck: ").append(propData.getAttackLev()).append("\n");
		for ( int i=0; i<100; i++ ) {
			PropDataSlot slot = new PropDataSlot();
			slot.setSlotType(PropDataEnhanceField.ATTACK);
			double data = EquipCalculator.calculateForgeData(propData, 5, "20004", slot);
			buf.append("new attack:").append(data).append("\n");
		}
		System.out.println(buf.toString());
	}
	
	@Test
	public void testDuration100() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		XinqiPropData.PropData xp = propData.toXinqiPropData();
		assertEquals(100, xp.getDuration());
	}
	
	@Test
	public void testDuration50() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		propData.setPropUsedTime(5);
		XinqiPropData.PropData xp = propData.toXinqiPropData();
		assertEquals(50, xp.getDuration());
	}
	
	@Test
	public void testDuration1() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		propData.setPropUsedTime(propData.getPropIndate());
		XinqiPropData.PropData xp = propData.toXinqiPropData();
		assertEquals(1, xp.getDuration());
	}
	
	@Test
	public void testDuration0() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		propData.setExpire(true);
		XinqiPropData.PropData xp = propData.toXinqiPropData();
		assertEquals(0, xp.getDuration());
	}
	
	/**
	 * 颜色武器的基础战斗力应高于白色的武器
	 */
	@Test
	public void testColorPropDataPowerGreatThanWhite() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData white = weapon.toPropData(30, WeaponColor.WHITE);
		PropData orange = weapon.toPropData(30, WeaponColor.ORGANCE);
		assertTrue(orange.getPower()>white.getPower());
		assertEquals(orange.getBasePower(), orange.getPower());
		assertEquals(0, orange.toXinqiPropData().getPower());
	}
}
